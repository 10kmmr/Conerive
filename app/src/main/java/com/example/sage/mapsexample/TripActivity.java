package com.example.sage.mapsexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TripActivity extends AppCompatActivity {

    private String tripId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        tripId = getIntent().getStringExtra("tripId");
    }
}
