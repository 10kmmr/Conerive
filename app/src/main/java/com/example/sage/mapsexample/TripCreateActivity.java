package com.example.sage.mapsexample;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;


public class TripCreateActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText tripNameET;
    private SeekBar notifRadiusSB;
    private TextView notifRadiusDisplayTV;
    private FloatingActionButton createTripFAB;

    private Marker tripDestinationMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

         // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_create_map);
        mapFragment.getMapAsync(this);

        tripNameET=findViewById(R.id.trip_name);
        notifRadiusSB=findViewById(R.id.notification_radius);
        notifRadiusDisplayTV=findViewById(R.id.notification_radius_display);
        createTripFAB=findViewById(R.id.create_trip);

    }

    @Override
    protected void onStart() {
        super.onStart();


        notifRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notifRadiusDisplayTV.setText(String.valueOf(progress / 10) + " Km");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        createTripFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tripName = tripNameET.getText().toString();
                int notifRadius = notifRadiusSB.getProgress()/10;
            }
        });


    }

    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
