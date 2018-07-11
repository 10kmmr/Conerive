package com.example.sage.mapsexample;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Transaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TripCreateActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "TripCreateActivity";
    private GoogleMap mMap;

    private FirebaseFirestore firestoreDB;
    private EditText tripNameET;
    private SeekBar notifRadiusSB;
    private TextView notifRadiusDisplayTV;
    private FloatingActionButton createTripFAB;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference ownerReference;
    private FirebaseDatabase database;

    private Marker tripDestinationMarker;

    String tripName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_create_map);
        mapFragment.getMapAsync(this);

        tripNameET = findViewById(R.id.trip_name);
        notifRadiusSB = findViewById(R.id.notification_radius);
        notifRadiusDisplayTV = findViewById(R.id.notification_radius_display);
        createTripFAB = findViewById(R.id.create_trip);
        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        ownerReference = database.getReference("USERS/" + currentUser.getUid());
        firestoreDB = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();


        notifRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notifRadiusDisplayTV.setText(String.valueOf(progress /10.0 ) + " Km");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        createTripFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tripDestinationMarker!=null) {
                    startLoading();
                    tripName = tripNameET.getText().toString();
                    double notifRadius = notifRadiusSB.getProgress()*100;
                    LatLng destinationLocation = tripDestinationMarker.getPosition();
                    GeoPoint geoPoint = new GeoPoint(destinationLocation.latitude, destinationLocation.longitude);

                    final Map<String, Object> trip= new HashMap<>();
                    trip.put("Name", tripName);
                    trip.put("Radius", notifRadius);
                    trip.put("Destination", geoPoint);
                    trip.put("AdminId", currentUser.getUid());
                    trip.put("Status", "TRIP_RUNNING");
                    ArrayList<String> users = new ArrayList<>();
                    users.add(currentUser.getUid());
                    trip.put("Users",users);
                    trip.put("Images", new ArrayList<>());
                    trip.put("Comments", new ArrayList<>());

                    firestoreDB.collection("TRIPS").add(trip)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    final String tripId = documentReference.getId();
                                    firestoreDB.collection("USERS").document(currentUser.getUid())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                    ArrayList<String> trips;
                                                    if (documentSnapshot.contains("Trips"))
                                                        trips = (ArrayList<String>) documentSnapshot.get("Trips");
                                                    else
                                                        trips = new ArrayList<>();
                                                    trips.add(tripId);

                                                    firestoreDB.collection("USERS").document(currentUser.getUid())
                                                            .update("Trips", trips)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Intent intent = new Intent(getApplicationContext(), TripActivity.class);
                                                                    intent.putExtra("tripId", tripId);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.d(TAG, "onFailure: " + e);
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: " + e);
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e);
                                    stopLoading();
                                }
                            });
                } else {
                    Toast.makeText(TripCreateActivity.this, "Select a destination", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onMapReady(com.google.android.gms.maps.GoogleMap googleMap) {
        mMap = googleMap;

        ownerReference.child("Location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng location = new LatLng(dataSnapshot.child("Latitude").getValue(double.class),
                        dataSnapshot.child("Longitude").getValue(double.class));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(tripDestinationMarker == null ) {
                    tripDestinationMarker = mMap.addMarker(new MarkerOptions().
                            position(latLng).
                            title("Destination"));
                } else {
                    tripDestinationMarker.setPosition(latLng);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }

    void startLoading(){
        createTripFAB.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    void stopLoading(){
        createTripFAB.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
