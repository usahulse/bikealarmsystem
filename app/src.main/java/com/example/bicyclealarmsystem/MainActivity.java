package com.example.bicyclealarmsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button armButton;
    private Button disarmButton;
    private Button testTheftButton;
    private SurfaceView cameraPreview;
    private TextView gpsLocation;

    private boolean isArmed = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float[] lastAcceleration = new float[3];
    private static final int SHAKE_THRESHOLD = 800;

    private Handler handler = new Handler();
    private boolean isFlashing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        armButton = findViewById(R.id.armButton);
        disarmButton = findViewById(R.id.disarmButton);
        testTheftButton = findViewById(R.id.testTheftButton);
        cameraPreview = findViewById(R.id.cameraPreview);
        gpsLocation = findViewById(R.id.gpsLocation);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        armButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isArmed = true;
                sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(MainActivity.this, "Alarm Armed", Toast.LENGTH_SHORT).show();
            }
        });

        disarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isArmed = false;
                sensorManager.unregisterListener(MainActivity.this);
                stopFlashingLight();
                Toast.makeText(MainActivity.this, "Alarm Disarmed", Toast.LENGTH_SHORT).show();
            }
        });

        testTheftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isArmed) {
                    triggerAlarm();
                } else {
                    Toast.makeText(MainActivity.this, "Alarm is not armed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            startCamera();
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        gpsLocation.setText("GPS Location: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }


    private void triggerAlarm() {
        Toast.makeText(this, "Theft Detected!", Toast.LENGTH_SHORT).show();
        startFlashingLight();
    }

    private void startFlashingLight() {
        if (!isFlashing) {
            isFlashing = true;
            handler.post(flashRunnable);
        }
    }

    private void stopFlashingLight() {
        if (isFlashing) {
            isFlashing = false;
            handler.removeCallbacks(flashRunnable);
            try {
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String cameraId = manager.getCameraIdList()[0];
                manager.setTorchMode(cameraId, false); // Turn off flashlight
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable flashRunnable = new Runnable() {
        private boolean torchOn = false;

        @Override
        public void run() {
            try {
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String cameraId = manager.getCameraIdList()[0];
                torchOn = !torchOn;
                manager.setTorchMode(cameraId, torchOn);
                handler.postDelayed(this, 500); // Flash every 500ms
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float[] values = event.values;
                float x = values[0];
                float y = values[1];
                float z = values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (isArmed) {
                        triggerAlarm();
                    }
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
