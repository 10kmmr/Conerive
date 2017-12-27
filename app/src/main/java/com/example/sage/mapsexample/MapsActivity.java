package com.example.sage.mapsexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

//
import android.location.*;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
//import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    public Button button;


    private static final String TAG = "MyActivity";

    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;

    //firebase
    FirebaseDatabase database;
    GeoFire geoFire;
    //GeoQuery geoQuery;
        //references to DB
        DatabaseReference myRef;
        DatabaseReference ref;

    //Controller variables
    public  String usr= "User1";
    public boolean usrSwitch=false;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        final Toast toast = Toast.makeText(this, "location optained", Toast.LENGTH_LONG);
        button =(Button)findViewById(R.id.button3);




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        //firebase testing--------------------------------------------------------------------------//


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        myRef.setValue("Yeno");

        //Writing geo location
        ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        geoFire = new GeoFire(ref);
//        geoFire.setLocation(usr, new GeoLocation(37.7853889, -122.4056973));


        //------------------------------------------------------------------------------------------//
        getLocationPermission();



        //this is just show toast that fusedlocationClient is returning a location
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            toast.show();
//                        }
//                    }
//                });
    }


    //Button Code
    public void onClickBtn(View v)
    {
        if(!usrSwitch)
            usr="User2";
        else
            usr="User1";
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
//                          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    new LatLng(mLastKnownLocation.getLatitude(),
//                                    mLastKnownLocation.getLongitude()), 15));
                            /* SEND FIREBASE THE DATA*/
                            geoFire.setLocation(usr, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                            Log.d(TAG, "onComplete: Wrote to DB");
//                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(-34, 151), 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
        Toast.makeText(this, usr, Toast.LENGTH_LONG).show();

        setMarkers();
    }




    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }







    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //default marker
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getLocationPermission();
        @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
//                          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    new LatLng(mLastKnownLocation.getLatitude(),
//                                    mLastKnownLocation.getLongitude()), 15));
                            /* SEND FIREBASE THE DATA*/
                    geoFire.setLocation("User2", new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    Log.d(TAG, "onComplete: Wrote to DB");
//                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.");
                    Log.e(TAG, "Exception: %s", task.getException());
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(-34, 151), 15));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }
        });

        setMarkers();

    }

    private void writeDB() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
//                          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    new LatLng(mLastKnownLocation.getLatitude(),
//                                    mLastKnownLocation.getLongitude()), 15));
                            /* SEND FIREBASE THE DATA*/
                            geoFire.setLocation("User1", new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                            Log.d(TAG, "onComplete: Wrote to DB");
//                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(-34, 151), 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void setMarkers(){

        //BAD CODE (WET)

        geoFire.getLocation("User1", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.latitude, location.longitude), 15));
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("User1"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(-34, 151), 15));
            }
        });



        geoFire.getLocation("User2", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.latitude, location.longitude), 15));
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("User2"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(-38, 151), 15));
            }
        });
    }

}
