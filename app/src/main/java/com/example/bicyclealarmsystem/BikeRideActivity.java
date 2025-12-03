package com.example.bicyclealarmsystem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This activity displays the bike ride screen, which includes a map, a trip computer,
 * a weather component, and a live camera view.
 */
public class BikeRideActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private MapView map;
    private LocationManager locationManager;
    private Marker bikeMarker;
    private Marker destinationMarker;
    private Polyline path;
    private Polyline route;
    private List<GeoPoint> geoPoints = new ArrayList<>();
    private TextView avgSpeedTextView, distanceTextView, timeTextView, caloriesTextView, temperatureTextView, windSpeedTextView;
    private Button endRideButton, pauseButton, restartButton, cameraButton;
    private long startTime;
    private float distance;
    private boolean isPaused;
    private RequestQueue requestQueue;
    private PreviewView cameraPreview;
    private FrameLayout cameraContainer;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private long lastWeatherFetchTime;

    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_ride);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(18.0);

        avgSpeedTextView = findViewById(R.id.avgSpeedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);
        endRideButton = findViewById(R.id.endRideButton);
        pauseButton = findViewById(R.id.pauseButton);
        restartButton = findViewById(R.id.restartButton);
        cameraButton = findViewById(R.id.cameraButton);
        cameraPreview = findViewById(R.id.cameraPreview);
        cameraContainer = findViewById(R.id.cameraContainer);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        bikeMarker = new Marker(map);
        bikeMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bike));
        map.getOverlays().add(bikeMarker);

        path = new Polyline();
        map.getOverlays().add(path);

        startTime = SystemClock.uptimeMillis();

        endRideButton.setOnClickListener(v -> finish());
        pauseButton.setOnClickListener(v -> {
            isPaused = !isPaused;
            if (isPaused) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        });
        restartButton.setOnClickListener(v -> {
            geoPoints.clear();
            path.setPoints(geoPoints);
            distance = 0;
            startTime = SystemClock.uptimeMillis();
            map.invalidate();
        });
        cameraButton.setOnClickListener(v -> {
            if (cameraContainer.getVisibility() == View.VISIBLE) {
                cameraContainer.setVisibility(View.GONE);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraContainer.setVisibility(View.VISIBLE);
                    startCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("destination")) {
            String destination = intent.getStringExtra("destination");
            geocodeDestination(destination);
        }
    }

    /**
     * Initializes the camera and binds it to the lifecycle of this activity.
     */
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Binds the camera preview to the PreviewView.
     *
     * @param cameraProvider The ProcessCameraProvider instance.
     */
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    /**
     * Called when the location has changed.
     *
     * @param location The new location.
     */
    @Override
    public void onLocationChanged(Location location) {
        if (!isPaused) {
            GeoPoint newPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            if (!geoPoints.isEmpty()) {
                Location lastLocation = new Location("");
                lastLocation.setLatitude(geoPoints.get(geoPoints.size() - 1).getLatitude());
                lastLocation.setLongitude(geoPoints.get(geoPoints.size() - 1).getLongitude());
                distance += location.distanceTo(lastLocation);
            }
            geoPoints.add(newPoint);
            path.setPoints(geoPoints);
            bikeMarker.setPosition(newPoint);
            map.getController().setCenter(newPoint);

            long elapsedTime = SystemClock.uptimeMillis() - startTime;
            float avgSpeed = 0;
            if (elapsedTime > 0) {
                avgSpeed = (distance / (elapsedTime / 1000.0f)) * 3.6f;
            }
            // Simplified calorie calculation based on distance.
            float calories = distance * 0.05f;

            avgSpeedTextView.setText(String.format("Avg Speed: %.2f km/h", avgSpeed));
            distanceTextView.setText(String.format("Distance: %.2f km", distance / 1000));
            timeTextView.setText(String.format("Time: %ds", elapsedTime / 1000));
            caloriesTextView.setText(String.format("Calories: %.2f", calories));

            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - lastWeatherFetchTime > 600000) { // 10 minutes
                fetchWeatherData(location.getLatitude(), location.getLongitude());
                lastWeatherFetchTime = currentTime;
            }
        }
    }

    /**
     * Fetches the weather data from the OpenWeatherMap API.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     */
    private void fetchWeatherData(double lat, double lon) {
        String apiKey = getString(R.string.openweathermap_api_key);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main = response.getJSONObject("main");
                            JSONObject wind = response.getJSONObject("wind");
                            double temp = main.getDouble("temp");
                            double windSpeed = wind.getDouble("speed");
                            temperatureTextView.setText(String.format("Temp: %.1f°C", temp));
                            windSpeedTextView.setText(String.format("Wind: %.1f m/s", windSpeed));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BikeRideActivity.this, "Error getting weather data", Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Geocodes the destination address in a background thread.
     *
     * @param destination The destination address.
     */
    private void geocodeDestination(String destination) {
        executorService.execute(() -> {
            GeocoderNominatim geocoder = new GeocoderNominatim(getApplicationContext().getPackageName());
            try {
                List<Address> addresses = geocoder.getFromLocationName(destination, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    handler.post(() -> onGeocodeSuccess(address));
                } else {
                    handler.post(() -> onGeocodeFailure());
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> onGeocodeFailure());
            }
        });
    }

    /**
     * Called when the geocoding is successful.
     *
     * @param address The address of the destination.
     */
    private void onGeocodeSuccess(Address address) {
        GeoPoint destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
        destinationMarker = new Marker(map);
        destinationMarker.setPosition(destinationPoint);
        destinationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_flag));
        map.getOverlays().add(destinationMarker);

        if (ActivityCompat.checkSelfPermission(BikeRideActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                getRoute(new GeoPoint(lastKnownLocation), destinationPoint);
            }
        }
    }

    /**
     * Called when the geocoding fails.
     */
    private void onGeocodeFailure() {
        Toast.makeText(BikeRideActivity.this, "Destination not found", Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets the route between two points in a background thread.
     *
     * @param start The starting point.
     * @param end   The ending point.
     */
    private void getRoute(GeoPoint start, GeoPoint end) {
        executorService.execute(() -> {
            RoadManager roadManager = new OSRMRoadManager(BikeRideActivity.this, getApplicationContext().getPackageName());
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(start);
            waypoints.add(end);
            Road road = roadManager.getRoad(waypoints);
            handler.post(() -> {
                if (road != null) {
                    route = RoadManager.buildRoadOverlay(road);
                    map.getOverlays().add(route);
                    map.invalidate();
                } else {
                    Toast.makeText(BikeRideActivity.this, "Error getting route", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    /**
     * Callback for the result from requesting permissions.
     *
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraContainer.setVisibility(View.VISIBLE);
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
