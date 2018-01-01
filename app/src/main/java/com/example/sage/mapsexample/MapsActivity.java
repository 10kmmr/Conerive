package com.example.sage.mapsexample;
import android.annotation.SuppressLint;
import android.os.Build;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;

    public String groupID = "g1";
    public String mobileNumber = "12345";
    public String Name = "Prithvi";

    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;

    FirebaseDatabase database;
    GeoFire ownerGeoFireObject;

    DatabaseReference groupReference;
    DatabaseReference ownerReference;

    ArrayList<User> users;

    private static final String TAG = "MyActivity";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        database = FirebaseDatabase.getInstance();
        groupReference = database.getReference("Root/"+groupID);
        ownerReference = database.getReference("Root/"+groupID+"/"+mobileNumber);
        ownerGeoFireObject = new GeoFire(ownerReference);

        //Location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //Button Code
    public void onClickBtn(View v) {
        Toast.makeText(this, "YENO", Toast.LENGTH_LONG).show();
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
                    database.getReference("Root/"+groupID+"/"+mobileNumber+"/Name").setValue(Name);
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

    public void getUsers(){
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "retrievedData: "+dataSnapshot);
                int i=0;
                users = new ArrayList<>();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    String mobileNumber = userSnapshot.getKey();
                    Log.d(TAG, "user number : "+(i++));
                    users.add(new User(mobileNumber, groupID, mMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void ErrorT(){
        Toast.makeText(this, "Error ", Toast.LENGTH_LONG).show();
    }
}
