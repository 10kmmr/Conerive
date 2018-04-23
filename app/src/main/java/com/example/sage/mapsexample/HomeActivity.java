package com.example.sage.mapsexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "HomeActivity";
    private static final double ZOOM_THRESHOLD = 16;

    private GoogleMap mMap;
    private Marker ownerMarker;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference ownerReference;
    private FirebaseDatabase database;
    private FirebaseFirestore firestoreDB;

    private FloatingActionButton tripCreateButton;
    private Button friendsButton;
    private Button notificationsButton;
    private Button userSettingsButton;

    private boolean zoomedIn;
    private ArrayList<Trip> trips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.home_map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        ownerReference = database.getReference("USERS/" + currentUser.getUid());
        firestoreDB = FirebaseFirestore.getInstance();


        tripCreateButton = findViewById(R.id.trip_create);
        friendsButton = findViewById(R.id.friends);
        userSettingsButton = findViewById(R.id.user_settings);
        notificationsButton = findViewById(R.id.notifications);

        trips = new ArrayList<>();
        zoomedIn = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        tripCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TripCreateActivity.class);
                startActivity(intent);
            }
        });

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
                startActivity(intent);
            }
        });

        userSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserSettingsActivity.class);
                startActivity(intent);
            }
        });

        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ownerReference.child("Location").addValueEventListener(new LocationValueEventListener());

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(!zoomedIn){
                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                    marker.getPosition(),
                                    (float) (ZOOM_THRESHOLD+0.1)),
                            500,
                            null);
                    return true;
                }else if(marker.getTag()!=null) {
                    Intent intent = new Intent(getApplicationContext(), TripActivity.class);
                    intent.putExtra("tripId", marker.getTag().toString());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if(zoomedIn && (mMap.getCameraPosition().zoom < ZOOM_THRESHOLD)){
                    zoomedIn = false;
                    for(Trip t : trips){
                        t.destination.setIcon(t.zoomedOutIcon);
                    }
                }
                if(!zoomedIn && mMap.getCameraPosition().zoom >= ZOOM_THRESHOLD){
                    zoomedIn = true;
                    for(Trip t : trips){
                        t.destination.setIcon(t.zoomedInIcon);
                    }
                }
            }
        });

        firestoreDB.collection("USERS").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains("Trips")){
                            ArrayList<String> tripIds = (ArrayList<String>) documentSnapshot.get("Trips");
                            for(String trip : tripIds){
                                firestoreDB.collection("TRIPS").document(trip)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                trips.add(new Trip(
                                                        documentSnapshot.getId(),
                                                        documentSnapshot.getString("Name"),
                                                        documentSnapshot.getGeoPoint("Destination"),
                                                        documentSnapshot.getString("AdmingID"),
                                                        documentSnapshot.getDouble("Radius"),
                                                        ((ArrayList<String>)documentSnapshot.get("Users")).size()));
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });

    }

    public class LocationValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LatLng location = new LatLng(
                    dataSnapshot.child("Latitude").getValue(double.class),
                    dataSnapshot.child("Longitude").getValue(double.class));

            if(ownerMarker ==null){
                ownerMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("You"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
            } else {
                ownerMarker.setPosition(location);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError);
        }
    }

    public class Trip {

        public String name;
        public String adminId;
        public double Radius;
        public int userCount;
        public String tripId;
        public Marker destination;
        public BitmapDescriptor zoomedInIcon;
        public BitmapDescriptor zoomedOutIcon;

        Trip(String tripId,String name,GeoPoint location,String adminId,double radius,int userCount){
            this.tripId = tripId;
            this.userCount = userCount;
            this.name=name;
            this.adminId=adminId;
            this.Radius=radius;

            // CODE FOR CUSTOM MARKER
            View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_home_trip, null);
            TextView tripNameTV = view.findViewById(R.id.marker_trip_name);
            tripNameTV.setText(name);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            (HomeActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            zoomedInIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
            zoomedOutIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

            destination = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            location.getLatitude(),
                            location.getLongitude()))
                    .icon(zoomedOutIcon)
                    .title(name));
            destination.setTag(tripId);


        }
    }

}



