package com.darshita.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager deviceSensorManager;
    private Sensor accelSensor;
    private Sensor lightSensor;
    private Sensor proxSensor;

    private TextView accelDisplay;
    private TextView lightDisplay;
    private TextView proxDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accelDisplay = findViewById(R.id.accelValue);
        lightDisplay = findViewById(R.id.lightValue);
        proxDisplay = findViewById(R.id.proxValue);

        deviceSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (deviceSensorManager != null) {
            accelSensor = deviceSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            lightSensor = deviceSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proxSensor = deviceSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceSensorManager != null) {
            if (accelSensor != null) {
                deviceSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                accelDisplay.setText("Sensor not supported on this device");
            }

            if (lightSensor != null) {
                deviceSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                lightDisplay.setText("Sensor not supported on this device");
            }

            if (proxSensor != null) {
                deviceSensorManager.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                proxDisplay.setText("Sensor not supported on this device");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceSensorManager != null) {
            deviceSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int currentSensorType = event.sensor.getType();

        if (currentSensorType == Sensor.TYPE_ACCELEROMETER) {
            String xVal = String.format("%.2f", event.values[0]);
            String yVal = String.format("%.2f", event.values[1]);
            String zVal = String.format("%.2f", event.values[2]);

            accelDisplay.setText("X: " + xVal + "\nY: " + yVal + "\nZ: " + zVal);
        }
        else if (currentSensorType == Sensor.TYPE_LIGHT) {
            lightDisplay.setText(event.values[0] + " lx");
        }
        else if (currentSensorType == Sensor.TYPE_PROXIMITY) {
            proxDisplay.setText(event.values[0] + " cm");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}