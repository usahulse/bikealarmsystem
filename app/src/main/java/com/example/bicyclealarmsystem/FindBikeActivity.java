package com.example.bicyclealarmsystem;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.common.util.concurrent.ListenableFuture;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindBikeActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final float SHAKE_THRESHOLD = 3.25f; // m/s^2

    private MapView map;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Marker bikeMarker;
    private TextView addressTextView;
    private Button soundButton, switchCameraButton;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPulseAlternate;
    private Drawable bikeIcon;
    private Drawable bikePulseIcon;
    private boolean isMoving;
    private boolean isSoundOn;
    private ToneGenerator toneGenerator;
    private PreviewView cameraPreview;
    private FrameLayout cameraContainer;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bike);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(18.0);

        addressTextView = findViewById(R.id.addressTextView);
        soundButton = findViewById(R.id.soundButton);
        switchCameraButton = findViewById(R.id.switchCameraButton);
        cameraPreview = findViewById(R.id.cameraPreview);
        cameraContainer = findViewById(R.id.cameraContainer);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        toneGenerator = new ToneGenerator(0, 100);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        bikeIcon = ContextCompat.getDrawable(this, R.drawable.ic_bike);
        bikePulseIcon = ContextCompat.getDrawable(this, R.drawable.ic_bike_pulse);

        bikeMarker = new Marker(map);
        bikeMarker.setIcon(bikeIcon);
        map.getOverlays().add(bikeMarker);

        startMarkerAnimation();

        soundButton.setOnClickListener(v -> {
            isSoundOn = !isSoundOn;
            if (isSoundOn) {
                startBeeping();
            } else {
                stopBeeping();
            }
        });

        switchCameraButton.setOnClickListener(v -> {
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                lensFacing = CameraSelector.LENS_FACING_FRONT;
            } else {
                lensFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startBeeping() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSoundOn) {
                    if (isMoving) {
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
                        handler.postDelayed(this, 500);
                    } else {
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200);
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }

    private void stopBeeping() {
        handler.removeCallbacksAndMessages(null);
        toneGenerator.stopTone();
    }

    private void startMarkerAnimation() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isPulseAlternate) {
                    bikeMarker.setIcon(bikeIcon);
                } else {
                    bikeMarker.setIcon(bikePulseIcon);
                }
                isPulseAlternate = !isPulseAlternate;
                map.invalidate();
                handler.postDelayed(this, 1000);
            }
        });
    }

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

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint newPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        bikeMarker.setPosition(newPoint);
        map.getController().setCenter(newPoint);
        reverseGeocode(newPoint);

        if (location.getSpeed() > 1) { // 1 m/s
            isMoving = true;
        } else {
            isMoving = false;
        }
    }

    private void reverseGeocode(GeoPoint point) {
        executorService.execute(() -> {
            GeocoderNominatim geocoder = new GeocoderNominatim(getApplicationContext().getPackageName());
            try {
                List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    handler.post(() -> addressTextView.setText("Address: " + address.getThoroughfare()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        executorService.shutdown();
        toneGenerator.release();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double acceleration = Math.sqrt(x * x + y * y + z * z);
            if (acceleration > SHAKE_THRESHOLD) {
                isMoving = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
