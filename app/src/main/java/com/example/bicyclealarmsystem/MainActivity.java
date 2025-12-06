package com.example.bicyclealarmsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The main activity for the Bicycle Alarm System application.
 * This activity handles the user interface and the core logic for arming,
 * disarming, and triggering the alarm.
 */
public class MainActivity extends AppCompatActivity {

    private boolean isArmed = false;
    private Button armButton;

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

        armButton = findViewById(R.id.armButton);
        armButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the arm/disarm button is clicked.
             * Toggles the armed state of the alarm.
             *
             * @param v The view that was clicked.
             */
            public void onClick(View v) {
                if (isArmed) {
                    disarmAlarm();
                } else {
                    armAlarm();
                }
            }
        });
    }

    /**
     * Arms the bicycle alarm.
     * When the alarm is armed, it will be triggered by motion detection.
     */
    public void armAlarm() {
        isArmed = true;
        armButton.setText("Disarm");
    }

    /**
     * Disarms the bicycle alarm.
     * When the alarm is disarmed, it will not be triggered by motion detection.
     */
    public void disarmAlarm() {
        isArmed = false;
        armButton.setText("Arm");
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
