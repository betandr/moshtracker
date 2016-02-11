package io.andr.moshpitfinder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private Boolean running = false;

    LocationManager locationManager;

    private Handler handler;

    private double force;

    Runnable updater = new Runnable() {
        @Override
        public void run() {
            try {
                Location location = getLastBestLocation();
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                TextView textbox = (TextView) findViewById(R.id.mosh_text);
                String update =
                        "POSITION     [" + lat + " / " + lon + "]\n" +
                        "ACCELERATION [" + force + "]\n\n" +
                        textbox.getText();

                textbox.setText(update);

                force = 0;

            } finally {
                handler.postDelayed(updater, 5000);
            }
        }
    };

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {

        double netForce= event.values[0] * event.values[0];
        netForce += event.values[1] * event.values[1];
        netForce += (event.values[2]) * (event.values[2]);

        netForce = Math.sqrt(netForce) - SensorManager.GRAVITY_EARTH;

        if (netForce > force) { force = netForce; }
    }

    private Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        final ProgressBar spinner = (ProgressBar) findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        spinner.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ff00ff"), PorterDuff.Mode.SRC_IN);

        final Button startButton = (Button) findViewById(R.id.start_button);

        final TextView textbox = (TextView) findViewById(R.id.mosh_text);
        textbox.setMovementMethod(new ScrollingMovementMethod());

        handler = new Handler();

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (running) {
                    String text = "STOPPING [" + timestamp() + "]\n\n" + textbox.getText();
                    textbox.setText(text);

                    spinner.setVisibility(View.GONE);

                    handler.removeCallbacks(updater);
                    running = false;
                    startButton.setText("MOSH!");
                } else {
                    String text = "STARTING [" + timestamp() + "]\n\n" + textbox.getText();
                    textbox.setText(text);

                    spinner.setVisibility(View.VISIBLE);
                    updater.run();
                    running = true;
                    startButton.setText("STOP");
                }

            }
        });


    }

    private String timestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Date date = new Date();

        return sdf.format(date).toString();
    }
}


