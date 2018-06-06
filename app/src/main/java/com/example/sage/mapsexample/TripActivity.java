package com.example.sage.mapsexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "TripActivity";
    private GoogleMap mMap;
    private Marker ownerMarker;
    private Marker destination;
    private Circle tripCircle;

    private HorizontalScrollView scrollview;
    private LinearLayout membersListLL;
    private LinearLayout popupLL;
    private Button scrollViewExpandBT;
    private Button leaveTripBT;
    private Button goHomeBT;
    private Button closePopBT;
    private Button trackUserBT;
    private TextView tripNameTV;
    private TextView tripRadiusTV;
    private CircleImageView settingsBT;
    private View settingsView;
    private View popupView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseFirestore firestoreDB;
    private DatabaseReference ownerReference;
    private ListenerRegistration listener;

    private PopupWindow popupWindow;

    private HashMap<String, Member> members;
    private ArrayList<String> userIDs;
    private String tripId;
    private String tripName;
    private String tripAdminId;
    private boolean adminMode;
    private double tripRadius;
    private boolean trackUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_map);
        mapFragment.getMapAsync(this);

        adminMode = false;
        trackUser = false;
        tripId = getIntent().getStringExtra("tripId");
        members = new HashMap<>();

        scrollview = findViewById(R.id.members_list_scroll_view);
        membersListLL = findViewById(R.id.members_list);
        scrollViewExpandBT = findViewById(R.id.scroll_view_expand);
        trackUserBT = findViewById(R.id.track_user);
        popupView = getLayoutInflater().inflate(R.layout.popup_settings_activity_trip, null);
        leaveTripBT = popupView.findViewById(R.id.leave_trip);

        popupLL = popupView.findViewById(R.id.popup_settings_linear_layout);
        goHomeBT = popupView.findViewById(R.id.go_home);
        closePopBT = popupView.findViewById(R.id.close_popup);

        tripNameTV = popupView.findViewById(R.id.trip_name);
        tripRadiusTV = popupView.findViewById(R.id.trip_radius);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(false);
        if (Build.VERSION.SDK_INT >= 21) {
            popupWindow.setElevation(50.0f);
        }

        ViewGroup.LayoutParams params = scrollview.getLayoutParams();
        params.height = 0;
        scrollview.setLayoutParams(params);
        settingsView = getLayoutInflater().inflate(R.layout.settings_activity_trip, membersListLL, false);
        settingsBT = settingsView.findViewById(R.id.settings);
        membersListLL.addView(settingsView);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        ownerReference = database.getReference("USERS/" + currentUser.getUid());

    }

    @Override
    protected void onStart() {
        super.onStart();

        closePopBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        goHomeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        settingsBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    popupWindow.showAtLocation(findViewById(R.id.trip_relative_layout), Gravity.CENTER, 0, 0);
            }
        });

        scrollViewExpandBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup.LayoutParams params = scrollview.getLayoutParams();
                if (params.height == 0) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    params.height = 0;
                }
                scrollview.setLayoutParams(params);
            }
        });

        trackUserBT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Log.d(TAG, "onTouch: pressed");
                    trackUser = true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "onTouch: released");
                    trackUser = false;
                }
                return false;
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
        resetCircle();
        resetCamera();
        ownerReference.child("Location").addValueEventListener(new OwnerLocationValueEventListener());

        firestoreDB.collection("TRIPS").document(tripId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        tripName = documentSnapshot.getString("Name");
                        tripRadius = documentSnapshot.getDouble("Radius");
                        tripAdminId = documentSnapshot.getString("AdminId");
                        GeoPoint geoPoint = (GeoPoint) documentSnapshot.get("Destination");
                        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        destination = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .title("Destination"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        userIDs = (ArrayList<String>) documentSnapshot.get("Users");
                        userIDs.remove(currentUser.getUid());
                        Log.d(TAG, "after removal : " + userIDs);
                        for (String userID : userIDs)
                            members.put(userID, new Member(userID, false));

                        if (currentUser.getUid().equals(tripAdminId)) {
                            adminMode = true;
                            View adminView = getLayoutInflater().inflate(R.layout.admin_activity_trip, popupLL, false);
                            Button inviteBT = adminView.findViewById(R.id.go_to_trip_invite);
                            inviteBT.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), TripInviteActivity.class);
                                    intent.putExtra("tripId", tripId);
                                    startActivity(intent);
                                }
                            });
                            Button deleteTripBT = adminView.findViewById(R.id.delete_trip);
                            deleteTripBT.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteTrip();
                                }
                            });
                            popupLL.addView(adminView, 2);
                        }

                        leaveTripBT.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(userIDs.size() == 0){
                                    deleteTrip();
                                } else {
                                    final Map<String, Object> updateMap = new HashMap<>();
                                    if (adminMode) {
                                        updateMap.put("AdminId", userIDs.get(0));
                                    }

                                    firestoreDB.collection("TRIPS").document(tripId)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    ArrayList<String> currentUsers = (ArrayList<String>) documentSnapshot.get("Users");
                                                    currentUsers.remove(currentUser.getUid());
                                                    updateMap.put("Users", currentUsers);
                                                    firestoreDB.collection("TRIPS").document(tripId)
                                                            .update(updateMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                }
                                            });

                                }
                            }
                        });

                        tripNameTV.setText(tripName);
                        tripRadiusTV.setText(Double.toString(tripRadius/1000.0) + "Km");

                        listener = firestoreDB.collection("TRIPS").document(tripId)
                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                        if (e != null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }

                                        if(documentSnapshot.getData() == null){
                                            Toast.makeText(TripActivity.this, "This trip has been deleted", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                        try {
                                            ArrayList<String> newUserIds = (ArrayList<String>) documentSnapshot.get("Users");
                                            newUserIds.remove(currentUser.getUid());
                                            if (newUserIds.size() > userIDs.size()) {
                                                newUserIds.removeAll(userIDs);
                                                String newUserId = newUserIds.get(0);
                                                userIDs.add(newUserId);
                                                members.put(newUserId, new Member(newUserId, true));

                                            } else if (newUserIds.size() < userIDs.size()) {
                                                ArrayList<String> temp = new ArrayList<>(userIDs);
                                                temp.removeAll(newUserIds);
                                                String toDeleteUserID = temp.get(0);
                                                members.get(toDeleteUserID).delete();
                                            }

                                            if(documentSnapshot.getString("AdminId").equals(currentUser.getUid()) && !adminMode){
                                                Toast.makeText(TripActivity.this, "You are now the admin", Toast.LENGTH_SHORT).show();
                                                Intent intent = getIntent();
                                                finish();
                                                startActivity(intent);
                                            }
                                        } catch (Exception ex){
                                            Log.d(TAG, "onEvent: " + ex);
                                        }
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

    public class OwnerLocationValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LatLng location = new LatLng(
                    dataSnapshot.child("Latitude").getValue(double.class),
                    dataSnapshot.child("Longitude").getValue(double.class));

            if (ownerMarker == null) {
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

    void resetCamera(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    if(trackUser){
                        LatLng location = ownerMarker.getPosition();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, 150);
                }
            }
        });
    }
    void resetCircle(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    // calculate midpoint
                    double latSum = ownerMarker.getPosition().latitude;
                    double lngSum = ownerMarker.getPosition().longitude;
                    for(Map.Entry<String, Member> u : members.entrySet()){
                        latSum += u.getValue().memberMarker.getPosition().latitude;
                        lngSum += u.getValue().memberMarker.getPosition().longitude;
                    }
                    Location midPoint = new Location(LocationManager.GPS_PROVIDER);
                    midPoint.setLatitude(latSum/(members.size()+1));
                    midPoint.setLongitude(lngSum/(members.size()+1));

                    // calculate radius
                    Location ownerLocation = new Location(LocationManager.GPS_PROVIDER);
                    ownerLocation.setLongitude(ownerMarker.getPosition().longitude);
                    ownerLocation.setLatitude(ownerMarker.getPosition().latitude);
                    double radius = midPoint.distanceTo(ownerLocation);
                    for(Map.Entry<String, Member> u : members.entrySet()){
                        Location temp = new Location(LocationManager.GPS_PROVIDER);
                        temp.setLongitude(u.getValue().memberMarker.getPosition().longitude);
                        temp.setLatitude(u.getValue().memberMarker.getPosition().latitude);
                        double distance = midPoint.distanceTo(temp);
                        if(distance>radius)
                            radius = distance;
                    }

                    if(tripCircle==null){
                        tripCircle = mMap.addCircle(new CircleOptions()
                            .center(new LatLng(
                                    midPoint.getLatitude(),
                                    midPoint.getLongitude()
                            ))
                        );
                    }

                    tripCircle.setCenter(new LatLng(
                            midPoint.getLatitude(),
                            midPoint.getLongitude()
                            )
                    );

                    tripCircle.setRadius(radius+100);
                    if(radius>tripRadius){
                        tripCircle.setStrokeColor(Color.RED);
                    } else {
                        tripCircle.setStrokeColor(Color.BLUE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, 500);
                }
            }
        });
    }


    void deleteTrip(){
        firestoreDB.collection("TRIPS").document(tripId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
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

    public class Member {
        String memberName;
        String memberID;
        String memberPhone;
        String memberEmail;
        String memberImageURL;
        Marker memberMarker;
        View memberViewItem;
        ValueEventListener valueEventListener;

        public Member(final String memberID, final boolean realtimeUpdate) {
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
                            ((TextView) memberViewItem.findViewById(R.id.name)).setText(memberName);
                            if (realtimeUpdate)
                                Toast.makeText(TripActivity.this, "Now tracking " + memberName, Toast.LENGTH_SHORT).show();
                            ImageView imageView = memberViewItem.findViewById(R.id.image);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.animateCamera(
                                            CameraUpdateFactory.newLatLng(memberMarker.getPosition()),
                                            500,
                                            null
                                    );
                                }
                            });

                            if (memberImageURL != null) {
                                Picasso.get()
                                        .load(memberImageURL)
                                        .into(imageView);
                            }
                            membersListLL.addView(memberViewItem);

                            valueEventListener = database.getReference("USERS/" + memberID).child("Location")
                                    .addValueEventListener(new ValueEventListener() {

                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            LatLng location = new LatLng(
                                                    dataSnapshot.child("Latitude").getValue(double.class),
                                                    dataSnapshot.child("Longitude").getValue(double.class));

                                            if (memberMarker == null) {
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

        void delete() {
            Toast.makeText(TripActivity.this, memberName + "has left the trip", Toast.LENGTH_SHORT).show();
            membersListLL.removeView(memberViewItem);
            if (valueEventListener != null)
                database.getReference("USERS/" + memberID)
                        .child("Location")
                        .removeEventListener(valueEventListener);
            if (memberMarker != null)
                memberMarker.remove();

            members.remove(memberID);

        }
    }
}
