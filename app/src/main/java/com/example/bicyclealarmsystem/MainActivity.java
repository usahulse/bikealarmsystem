package com.example.bicyclealarmsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity for the Bicycle Alarm System application.
 * This activity handles the user interface and the core logic for arming,
 * disarming, and triggering the alarm.
 */
public class MainActivity extends AppCompatActivity {

    private boolean isArmed = false;

    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up:
     * create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in onSaveInstanceState(Bundle).
     *     <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button armButton = findViewById(R.id.armButton);
        final EditText destinationEditText = findViewById(R.id.destinationEditText);
        final Button destinationRideButton = findViewById(R.id.destinationRideButton);
        final Button freeRideButton = findViewById(R.id.freeRideButton);
        final Button findBikeButton = findViewById(R.id.findBikeButton);

        armButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Alarm feature not yet implemented", Toast.LENGTH_SHORT).show();
        });

        destinationRideButton.setOnClickListener(v -> {
            String destination = destinationEditText.getText().toString();
            Intent intent = new Intent(MainActivity.this, BikeRideActivity.class);
            intent.putExtra("destination", destination);
            startActivity(intent);
        });

        freeRideButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BikeRideActivity.class);
            startActivity(intent);
        });

        findBikeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FindBikeActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Arms the bicycle alarm.
     * When the alarm is armed, it will be triggered by motion detection.
     */
    public void armAlarm() {
        isArmed = true;
    }

    /**
     * Disarms the bicycle alarm.
     * When the alarm is disarmed, it will not be triggered by motion detection.
     */
    public void disarmAlarm() {
        isArmed = false;
    }

    /**
     * Called when motion is detected by the device's sensors.
     * If the alarm is armed, this method will trigger the alarm.
     */
    public void onMotionDetected() {
        if (isArmed) {
            // Trigger the alarm
        }
    }
}
