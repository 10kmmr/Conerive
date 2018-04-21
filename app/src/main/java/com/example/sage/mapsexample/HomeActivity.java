package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private ArrayList<Trip> ArrayTrips;

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

        tripCreateButton = findViewById(R.id.trip_create);
        friendsButton = findViewById(R.id.friends);
        userSettingsButton = findViewById(R.id.user_settings);
        notificationsButton = findViewById(R.id.notifications);
        firestoreDB = FirebaseFirestore.getInstance();

        ArrayTrips = new ArrayList<Trip>();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ownerReference.child("Location").addValueEventListener(new LocationValueEventListener());

        firestoreDB.collection("USERS").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> UsersTrips;
                        if (documentSnapshot.contains("Trips")){
                            UsersTrips = (ArrayList<String>) documentSnapshot.get("Trips");
                            for(String trip : UsersTrips){
                                firestoreDB.collection("TRIPS").document(trip)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                ArrayTrips.add(new Trip(mMap,documentSnapshot.getId(),documentSnapshot.getString("Name"),documentSnapshot.getGeoPoint("Destination")
                                                        ,documentSnapshot.getString("AdmingID"),documentSnapshot.getDouble("Radius"),((ArrayList<String>)documentSnapshot.get("Users")).size()));
                                            }
                                        });
                            }
                        }
                    }
                });

    }


    public class LocationValueEventListener implements ValueEventListener{

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LatLng location = new LatLng(dataSnapshot.child("Latitude").getValue(double.class),
                    dataSnapshot.child("Longitude").getValue(double.class));

            if(ownerMarker ==null){
                ownerMarker = mMap.addMarker(new MarkerOptions().
                        position(location).
                        title("Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

            } else {
                ownerMarker.setPosition(location);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError);
        }
    }
}
class Trip {
    String Name;
    GeoPoint Destination;
    String AdminId;
    double Radius;
    int NoOfPeopleOnTrip;
    String TripId;
    GoogleMap GM;
    Marker selfM;
    Trip(GoogleMap gm,String Docid,String N,GeoPoint d,String Aid,double R,int gg){
        this.GM = gm;
        this.TripId = Docid;
        this.NoOfPeopleOnTrip = gg;
        this.Name=N;
        this.Destination= d;
        this.AdminId=Aid;
        this.Radius=R;
        selfM = GM.addMarker(new MarkerOptions().
                position(new LatLng(Destination.getLatitude(),Destination.getLongitude())).
                title(Name));


    }
}

