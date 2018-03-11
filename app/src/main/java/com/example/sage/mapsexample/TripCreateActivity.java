package com.example.sage.mapsexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class TripCreateActivity extends AppCompatActivity {

    private static final String TAG = "TripCreateActivity";
    SeekBar notificationRadiusSB;
    TextView notificationRadiusTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        notificationRadiusSB = findViewById(R.id.notification_radius_selector);
        notificationRadiusTV = findViewById(R.id.notification_radius_display);
    }

    @Override
    protected void onStart() {
        super.onStart();

        notificationRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notificationRadiusTV.setText(String.valueOf(progress/10) + " Km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
