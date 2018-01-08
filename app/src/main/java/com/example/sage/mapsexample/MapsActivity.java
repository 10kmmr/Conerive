
package com.example.sage.mapsexample;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.*;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback { /* can use LocationListener class */

    public GoogleMap mMap;
    public String groupID;
    public String userID;
    public String Name;


    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    LocationRequest mLocationRequest;

    FirebaseDatabase database;
    GeoFire ownerGeoFireObject;

    DatabaseReference groupReference;
    DatabaseReference ownerReference;

    HashMap<String, GroupMember> users = new HashMap<>();

    private static final String TAG = "MyActivity";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        groupID = getIntent().getStringExtra("GroupID");
        userID = getIntent().getStringExtra("UserID");
        Name = getIntent().getStringExtra("Name");
        //this is comment ok? yay you found it
        //getActionBar().setTitle(groupID);
        database = FirebaseDatabase.getInstance();
        groupReference = database.getReference("Root/"+groupID);
        ownerReference = database.getReference("Root/"+groupID+"/"+userID);
        ownerGeoFireObject = new GeoFire(ownerReference);

        //Location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);

        // batery shit
        // mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //error handling do later
//              if (ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_FINE_LOCATION)
//                        == PackageManager.PERMISSION_GRANTED) {
//              }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        // Log.d(TAG, "onCreate: Lat :" + Double.toString(mLastKnownLocation.getLatitude()) + " Log :" + Double.toString(mLastKnownLocation.getLongitude()));
    }

    //Button Code
    public void onClickBtn(View v) {
        Toast.makeText(this, "YENO", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, Groupselector.class);
        intent.putExtra("UserID", userID);
        intent.putExtra("Name",Name );
        intent.putExtra("GroupID",groupID);
        startActivity(intent);
    }
    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            // mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        //GETTING LOCATION AND WRITING TO DB
        @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {

                    mLastKnownLocation = task.getResult();
                    database.getReference("Root/"+groupID+"/"+userID+"/Name").setValue(Name);
                    ownerGeoFireObject.setLocation("Location", new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    googleMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15));
                    //retrieve all users in group
                    getUsers();
                } else {
                    ErrorT();
                }
            }
        });
    }

    public void getUsers() {

        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userID = dataSnapshot.getKey();
                users.put(userID, new GroupMember(userID, groupID, mMap, database));
                Log.d(TAG, "map :"+users.keySet());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                users.get(dataSnapshot.getKey()).releaseListener();
                users.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void ErrorT(){
        Toast.makeText(this, "Error ", Toast.LENGTH_LONG).show();
    }


    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                //Place current location marker
                mLastKnownLocation=location;
                ownerGeoFireObject.setLocation("Location", new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));

                //move map camera
                //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            }
        }
    };

}
