package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "TripActivity";
    private GoogleMap mMap;
    private Marker ownerMarker;
    private Marker destination;

    private HorizontalScrollView scrollview;
    private LinearLayout membersListLL;
    private Button homeBT;
    private Button scrollViewExpandBT;
    private CircleImageView inviteBT;
    private View inviteView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseFirestore firestoreDB;
    private DatabaseReference ownerReference;

    private HashMap<String, Member> members;
    private String tripId;
    private String tripName;
    private double radius;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_map);
        mapFragment.getMapAsync(this);

        tripId = getIntent().getStringExtra("tripId");
        members = new HashMap<>();

        scrollview = findViewById(R.id.members_list_scroll_view);
        membersListLL = findViewById(R.id.members_list);
        homeBT = findViewById(R.id.home);
        scrollViewExpandBT = findViewById(R.id.scroll_view_expand);

        inviteView = getLayoutInflater().inflate(R.layout.invite_activity_trip, membersListLL, false);
        inviteBT = inviteView.findViewById(R.id.go_to_trip_invite);
        membersListLL.addView(inviteView);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        ownerReference = database.getReference("USERS/" + currentUser.getUid());

    }

    @Override
    protected void onStart() {
        super.onStart();

        ViewGroup.LayoutParams params = scrollview.getLayoutParams();
        params.height = 0;
        scrollview.setLayoutParams(params);

        inviteBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TripInviteActivity.class);
                intent.putExtra("tripId", tripId);
                startActivity(intent);
            }
        });

        homeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        scrollViewExpandBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup.LayoutParams params = scrollview.getLayoutParams();
                if(params.height == 0){
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                } else {
                    params.height = 0;
                }
                scrollview.setLayoutParams(params);
            }
        });

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        ownerReference.child("Location").addValueEventListener(new OwnerLocationValueEventListener());

        firestoreDB.collection("TRIPS").document(tripId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        tripName = documentSnapshot.getString("Name");
                        radius = documentSnapshot.getDouble("Radius");
                        GeoPoint geoPoint = (GeoPoint)documentSnapshot.get("Destination");
                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        destination = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .title("Destination"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        ArrayList<String> userIDs = (ArrayList<String>)documentSnapshot.get("Users");
                        userIDs.remove(currentUser.getUid());
                        Log.d(TAG, "after removal : " +  userIDs);
                        for(String userID : userIDs)
                            members.put(userID, new Member(userID));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    public class OwnerLocationValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LatLng location = new LatLng(
                    dataSnapshot.child("Latitude").getValue(double.class),
                    dataSnapshot.child("Longitude").getValue(double.class));

            if(ownerMarker ==null){
                ownerMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title("You"));
            } else {
                ownerMarker.setPosition(location);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled: " + databaseError);
        }
    }


    public class Member{
        String memberName;
        String memberID;
        String memberPhone;
        String memberEmail;
        String memberImageURL;
        Marker memberMarker;
        View memberViewItem;

        public Member(final String memberID) {
            this.memberID = memberID;

            firestoreDB.collection("USERS").document(memberID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            memberEmail = documentSnapshot.getString("Email");
                            memberName = documentSnapshot.getString("Name");
                            memberPhone = documentSnapshot.getString("Phone");
                            memberImageURL = documentSnapshot.getString("ImageURL");

                            memberViewItem = getLayoutInflater().inflate(R.layout.member_activity_trip, membersListLL, false);
                            ((TextView)memberViewItem.findViewById(R.id.name)).setText(memberName);
                            ImageView imageView = memberViewItem.findViewById(R.id.image);

                            if(memberImageURL!=null){
                                Picasso.get()
                                        .load(memberImageURL)
                                        .into(imageView);
                            }
                            membersListLL.addView(memberViewItem);


                            database.getReference("USERS/"+memberID).child("Location")
                                    .addValueEventListener(new ValueEventListener() {

                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            LatLng location = new LatLng(
                                                    dataSnapshot.child("Latitude").getValue(double.class),
                                                    dataSnapshot.child("Longitude").getValue(double.class));

                                            if(memberMarker ==null){
                                                memberMarker = mMap.addMarker(new MarkerOptions()
                                                        .position(location)
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                                        .title(memberName));
                                            } else {
                                                memberMarker.setPosition(location);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: " + databaseError);
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
    }
}
