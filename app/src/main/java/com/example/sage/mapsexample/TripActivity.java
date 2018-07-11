package com.example.sage.mapsexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "TripActivity";
    private GoogleMap mMap;

    FirebaseDatabase firebaseDB;
    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth;
    DocumentSnapshot documentSnapshot;
    ListenerRegistration documentListener;

    MenuBar menuBar;
    CommentBar commentBar;
    MembersBar membersBar;
    Settings settings;
    Centroid centroid;
    Destination destination;
    HashMap<String, Member> members;
    HashMap<String, Image> images;
    HashMap<String, Comment> comments;
    Owner owner;

    Object currentPopup;
    Object currentBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.trip_map);
        mapFragment.getMapAsync(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDB = FirebaseDatabase.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        members = new HashMap<>();
        images = new HashMap<>();
        comments = new HashMap<>();

        findViewById(R.id.expand_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentBar == null)
                    menuBar.show();
                else if (currentBar == menuBar)
                    menuBar.close();
                else if (currentBar == membersBar)
                    membersBar.close();
                else if (currentBar == commentBar)
                    commentBar.close();

            }
        });

        menuBar = new MenuBar();
        commentBar = new CommentBar();
        membersBar = new MembersBar();

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

        centroid = new Centroid();
        destination = new Destination();

        documentListener = firestoreDB.collection("TRIPS")
                .document(getIntent().getStringExtra("tripId"))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                        }

                        if (TripActivity.this.documentSnapshot == null) {
                            TripActivity.this.documentSnapshot = documentSnapshot;
                            if (documentSnapshot.getString("AdminId").equals(firebaseAuth.getCurrentUser().getUid()))
                                settings = new AdminSettings();
                            else
                                settings = new Settings();
                            destination.update();
                            settings.update();
                            owner = new Owner();
                            ArrayList<String> userIDs = (ArrayList<String>) documentSnapshot.get("Users");
                            userIDs.remove(firebaseAuth.getCurrentUser().getUid());
                            for (String userID : userIDs)
                                members.put(userID, new Member(userID, false));
                            return;
                        }
                        if (documentSnapshot.getData() == null) {
                            Toast.makeText(TripActivity.this, "This trip has been deleted", Toast.LENGTH_SHORT).show();
                            documentListener.remove();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        TripActivity.this.documentSnapshot = documentSnapshot;
                        switch (documentSnapshot.getString("Event_one")) {
                            case "USER_ADDED": {
                                members.put(
                                        documentSnapshot.getString("Stack"),
                                        new Member(
                                                documentSnapshot.getString("Stack"),
                                                true
                                        )
                                );
                                break;
                            }
                            case "USER_REMOVED": {
                                String userId = documentSnapshot.getString("Stack");
                                if (userId.equals(firebaseAuth.getCurrentUser().getUid())) {
                                    Toast.makeText(
                                            TripActivity.this,
                                            "You have been kicked from the trip",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Member removedUser = members.get(userId);
                                    removedUser.delete();
                                    boolean one = documentSnapshot.getString("Event_two").equals("ADMIN_CHANGED");
                                    boolean two = documentSnapshot.getString("AdminId").equals(firebaseAuth.getCurrentUser().getUid());
                                    if (one && two) {
                                        Toast.makeText(TripActivity.this, "You are now admin", Toast.LENGTH_SHORT).show();
                                        settings = new AdminSettings();
                                    }
                                }
                                break;
                            }
                            case "IMAGE_ADDED": {
                                break;
                            }
                            case "IMAGE_REMOVED": {
                                break;
                            }
                            case "COMMENT_ADDED": {
                                break;
                            }
                            case "COMMENT_REMOVED": {
                                break;
                            }
                        }


                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageByte = baos.toByteArray();
        // TODO - upload image to drive
    }

    public class Centroid {
        Circle centroidCircle;

        Centroid() {
            LatLng latLng = new LatLng(0, 0);
            centroidCircle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(5)
                    .strokeWidth(1)
                    .strokeColor(Color.YELLOW)
            );
        }

        LatLng calculateCentroid() {
            double latSum = owner.ownerMarker.marker.getPosition().latitude;
            double lngSum = owner.ownerMarker.marker.getPosition().longitude;
            for (Map.Entry<String, Member> u : members.entrySet()) {
                latSum += u.getValue().imageMarker.marker.getPosition().latitude;
                lngSum += u.getValue().imageMarker.marker.getPosition().longitude;
            }
            return new LatLng(
                    latSum / (members.size() + 1),
                    lngSum / (members.size() + 1)
            );
        }

        void update() {
            LatLng latLng = calculateCentroid();
            centroidCircle.setCenter(latLng);
        }


    }

    class MenuBar {
        LinearLayout menuLL;

        MenuBar() {
            menuLL = findViewById(R.id.menu_bar);
            findViewById(R.id.menu_home).setOnClickListener(new MenuHomeOnClickListener());
            findViewById(R.id.menu_settings).setOnClickListener(new MenuSettingsOnClickListener());
            findViewById(R.id.menu_camera).setOnClickListener(new MenuCameraOnClickListener());
            findViewById(R.id.menu_members).setOnClickListener(new MenuMembersOnClickListener());
            findViewById(R.id.menu_comments).setOnClickListener(new MenuCommentsOnClickListener());
        }

        void show() {
            ViewGroup.LayoutParams params = menuLL.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            menuLL.setLayoutParams(params);
            currentBar = this;
        }

        void close() {
            ViewGroup.LayoutParams params = menuLL.getLayoutParams();
            params.height = 0;
            menuLL.setLayoutParams(params);
            currentBar = null;
        }

        class MenuHomeOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }

        class MenuSettingsOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if (currentPopup != null && currentPopup != settings)
                    ((Member.Popup) currentPopup).close();
                settings.show();
            }
        }

        class MenuCameraOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        }

        class MenuMembersOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                menuBar.close();
                membersBar.show();
            }
        }

        class MenuCommentsOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                menuBar.close();
                commentBar.show();
            }
        }
    }

    class CommentBar {
        RelativeLayout commentRL;

        CommentBar() {
            commentRL = findViewById(R.id.comment_bar);
            findViewById(R.id.comment_bar_button).setOnClickListener(new CommentOnClickListener());
        }

        class CommentOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                String commentText = ((EditText) findViewById(R.id.comment_bar_edittext)).getText().toString();
                Toast.makeText(TripActivity.this, "Comment : " + commentText, Toast.LENGTH_SHORT).show();
                // TODO - upload comment to DB
            }
        }

        void show() {
            ViewGroup.LayoutParams params = commentRL.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            commentRL.setLayoutParams(params);
            currentBar = this;
        }

        void close() {
            ViewGroup.LayoutParams params = commentRL.getLayoutParams();
            params.height = 0;
            commentRL.setLayoutParams(params);
            currentBar = null;
        }
    }

    class MembersBar {
        LinearLayout membersLL;
        HorizontalScrollView membersSV;

        MembersBar() {
            membersLL = findViewById(R.id.members_bar_linear_layout);
            membersSV = findViewById(R.id.members_bar_scroll_view);
            findViewById(R.id.members_bar_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MembersBar.this.close();
                    menuBar.show();
                }
            });
        }

        void show() {
            ViewGroup.LayoutParams params = membersSV.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            membersSV.setLayoutParams(params);
            currentBar = this;
        }

        void close() {
            ViewGroup.LayoutParams params = membersSV.getLayoutParams();
            params.height = 0;
            membersSV.setLayoutParams(params);
            currentBar = null;
        }
    }

    class AdminSettings extends Settings {

        AdminSettings() {
            popupView = getLayoutInflater().inflate(R.layout.popup_admin_settings_activity_trip, null);
            popupView.findViewById(R.id.invite_member).setOnClickListener(new InviteMemberOnClickListener());
            popupView.findViewById(R.id.leave_trip).setOnClickListener(new LeaveTripOnClickListener());
            popupView.findViewById(R.id.delete_trip).setOnClickListener(new DeleteTripOnClickListener());
            popupView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
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

        }

        private class LeaveTripOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                firestoreDB.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference tripReference = firestoreDB.collection("TRIPS").document(documentSnapshot.getId());
                        DocumentReference userReference = firestoreDB.collection("USERS").document(firebaseAuth.getCurrentUser().getUid());

                        DocumentSnapshot tripSnapshot = transaction.get(tripReference);
                        DocumentSnapshot userSnapshot = transaction.get(userReference);

                        ArrayList<String> users = (ArrayList<String>)tripSnapshot.get("Users");
                        ArrayList<String> trips = (ArrayList<String>)userSnapshot.get("Trips");

                        users.remove(firebaseAuth.getCurrentUser().getUid());
                        trips.remove(documentSnapshot.getId());

                        if (users.size() > 0) {
                            HashMap<String, Object> tripMap = new HashMap<>();
                            tripMap.put("Users", users);
                            tripMap.put("Event_one", "USER_REMOVED");
                            tripMap.put("Event_two", "ADMIN_CHANGED");
                            tripMap.put("AdminId", users.get(0));
                            tripMap.put("Stack", firebaseAuth.getCurrentUser().getUid());

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("Trips", trips);
                            userMap.put("Event_one", "TRIP_REMOVED");
                            userMap.put("Stack", documentSnapshot.getId());

                            transaction.update(tripReference, tripMap);
                            transaction.update(userReference, userMap);
                        } else {
                            transaction.delete(tripReference);
                        }
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + "Failed to quit " + e.toString());
                    }
                });
            }
        }

        private class InviteMemberOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TripInviteActivity.class);
                intent.putExtra("tripId", documentSnapshot.getId());
                startActivity(intent);
            }
        }

        private class DeleteTripOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                firestoreDB.collection("TRIPS").document(documentSnapshot.getId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // TODO - kill all the listeners
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                    }
                });
            }
        }
    }

    class Settings {
        View popupView;
        PopupWindow popupWindow;

        Settings() {
            popupView = getLayoutInflater().inflate(R.layout.popup_settings_activity_trip, null);
            popupView.findViewById(R.id.leave_trip).setOnClickListener(new LeaveTripOnClickListener());
            popupView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
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

        }

        void update() {
            ((TextView) popupView.findViewById(R.id.trip_name)).setText(documentSnapshot.getString("Name"));
            ((TextView) popupView.findViewById(R.id.trip_radius)).setText(
                    Double.toString(documentSnapshot.getDouble("Radius") / 1000.0) + "Km"
            );
            ((Button) popupView.findViewById(R.id.member_count)).setText(
                    Integer.toString(
                            ((ArrayList<String>) documentSnapshot.get("Users")).size()
                    )
            );
            ((Button) popupView.findViewById(R.id.image_count)).setText(
                    Integer.toString(
                            ((ArrayList<String>) documentSnapshot.get("Images")).size()
                    )
            );
            ((Button) popupView.findViewById(R.id.comment_count)).setText(
                    Integer.toString(
                            ((ArrayList<String>) documentSnapshot.get("Comments")).size()
                    )
            );
        }

        void show() {
            popupWindow.showAtLocation(findViewById(R.id.trip_relative_layout), Gravity.CENTER, 0, 0);
            currentPopup = this;
        }

        void close() {
            popupWindow.dismiss();
            currentPopup = null;
        }

        private class LeaveTripOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                firestoreDB.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference tripReference = firestoreDB.collection("TRIPS").document(documentSnapshot.getId());
                        DocumentReference userReference = firestoreDB.collection("USERS").document(firebaseAuth.getCurrentUser().getUid());

                        DocumentSnapshot tripDocumentSnapshot = transaction.get(tripReference);
                        DocumentSnapshot userDocumentSnapshot = transaction.get(userReference);

                        ArrayList<String> users = (ArrayList<String>)tripDocumentSnapshot.get("Users");
                        ArrayList<String> trips = (ArrayList<String>)userDocumentSnapshot.get("Trips");

                        users.remove(firebaseAuth.getCurrentUser().getUid());
                        trips.remove(documentSnapshot.getId());

                        HashMap<String, Object> tripMap = new HashMap<>();
                        tripMap.put("Users", users);
                        tripMap.put("Event_one", "USER_REMOVED");
                        tripMap.put("Stack", firebaseAuth.getCurrentUser().getUid());

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("Trips", trips);
                        userMap.put("Event_one", "TRIP_REMOVED");
                        userMap.put("Stack", documentSnapshot.getId());

                        transaction.update(tripReference, tripMap);
                        transaction.update(userReference, userMap);
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + "Failed to quit");
                    }
                });
            }
        }
    }

    class Destination {
        Marker marker;

        Destination() {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }

        void update() {
            GeoPoint geoPoint = (GeoPoint) documentSnapshot.get("Destination");
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            marker.setPosition(latLng);
        }
    }

    class Owner {
        DocumentSnapshot documentSnapshot;
        ListenerRegistration documentListener;
        ValueEventListener locationListener;
        OwnerMarker ownerMarker;
        CentralLine centralLine;
        BarItem barItem;

        Owner() {
            ownerMarker = new OwnerMarker();
            centralLine = new CentralLine();
            barItem = new BarItem();

            documentListener = firestoreDB.collection("USERS")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            Owner.this.documentSnapshot = documentSnapshot;
                            update();
                        }
                    });

            locationListener = firebaseDB.getReference("USERS/" + firebaseAuth.getCurrentUser().getUid())
                    .child("Location")
                    .addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LatLng location = new LatLng(
                                    dataSnapshot.child("Latitude").getValue(double.class),
                                    dataSnapshot.child("Longitude").getValue(double.class)
                            );
                            ownerMarker.marker.setPosition(location);
                            centroid.update();
                            centralLine.update();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: " + databaseError);
                        }
                    });
        }

        void update() {
            barItem.update();
        }

        class OwnerMarker {
            Marker marker;

            OwnerMarker() {
                LatLng location = new LatLng(0, 0);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }

        class CentralLine {
            Circle circle;
            Polyline line;

            CentralLine() {
                // TODO - change colour of line and circle
                line = mMap.addPolyline(new PolylineOptions()
                        .width(1)
                        .color(Color.RED));

                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(0, 0))
                        .radius(5)
                        .strokeWidth(1)
                        .strokeColor(Color.RED));
            }

            void update() {
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(ownerMarker.marker.getPosition());
                points.add(centroid.centroidCircle.getCenter());
                line.setPoints(points);
                circle.setCenter(ownerMarker.marker.getPosition());
            }
        }

        class BarItem {
            View barView;

            BarItem() {
                barView = getLayoutInflater().inflate(R.layout.member_activity_trip, membersBar.membersLL, false);
                ImageView imageView = barView.findViewById(R.id.image);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLng(ownerMarker.marker.getPosition()),
                                500,
                                null
                        );
                    }
                });
                membersBar.membersLL.addView(barView, 1);
            }

            void update() {
                ((TextView) barView.findViewById(R.id.name)).setText(documentSnapshot.getString("Name"));
                if (documentSnapshot.getString("ImageURL") != null) {
                    Picasso.get()
                            .load(documentSnapshot.getString("ImageURL"))
                            .into((ImageView) barView.findViewById(R.id.image));
                }
            }
        }

    }

    class Member {
        DocumentSnapshot documentSnapshot;
        ListenerRegistration documentListener;
        ValueEventListener locationListener;
        ImageMarker imageMarker;
        CentralLine centralLine;
        Popup popup;
        BarItem barItem;

        Member(String memberID, Boolean realtimeUpdate) {
            // TODO - Handle real time update better
            if (realtimeUpdate)
                Toast.makeText(TripActivity.this, "Now Tracking new user", Toast.LENGTH_SHORT).show();

            popup = new Popup();
            barItem = new BarItem();
            centralLine = new CentralLine();
            imageMarker = new ImageMarker();

            documentListener = firestoreDB.collection("USERS")
                    .document(memberID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            Member.this.documentSnapshot = documentSnapshot;
                            update();
                        }
                    });

            locationListener = firebaseDB.getReference("USERS/" + memberID)
                    .child("Location")
                    .addValueEventListener(new ValueEventListener() {
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            LatLng location = new LatLng(
                                    dataSnapshot.child("Latitude").getValue(double.class),
                                    dataSnapshot.child("Longitude").getValue(double.class)
                            );
                            imageMarker.marker.setPosition(location);
                            centroid.update();
                            centralLine.update();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: " + databaseError);
                        }
                    });

        }

        void delete() {
            Toast.makeText(TripActivity.this,
                    documentSnapshot.getString("Name") + "has left the trip",
                    Toast.LENGTH_SHORT
            ).show();
            if (currentPopup == popup)
                popup.close();
            if (locationListener != null)
                firebaseDB.getReference("USERS/" + documentSnapshot.getId())
                        .child("Location")
                        .removeEventListener(locationListener);
            if (documentListener != null)
                documentListener.remove();
            barItem.delete();
            imageMarker.delete();
            centralLine.delete();
            members.remove(documentSnapshot.getId());
        }

        void update() {
            popup.update();
            barItem.update();
            imageMarker.update();
        }

        class ImageMarker {
            Marker marker;
            View markerView;

            public ImageMarker() {
                markerView = getLayoutInflater().inflate(R.layout.marker_member, null);
                markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
                markerView.buildDrawingCache();
                LatLng location = new LatLng(0, 0);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView))));
            }

            void update() {
                if (documentSnapshot.getString("ImageURL") != null) {
                    Picasso.get()
                            .load(documentSnapshot.getString("ImageURL"))
                            .into(markerView.findViewById(R.id.image), new Callback() {
                                @Override
                                public void onSuccess() {
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)));
                                }

                                @Override
                                public void onError(Exception e) {
                                    // TODO - handle image loading error
                                }
                            });
                }
            }

            void delete() {
                marker.remove();
            }

            private Bitmap getMarkerBitmapFromView(View view) {
                Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(returnedBitmap);
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                Drawable drawable = view.getBackground();
                if (drawable != null)
                    drawable.draw(canvas);
                view.draw(canvas);
                return returnedBitmap;
            }

        }

        class CentralLine {
            Circle circle;
            Polyline line;

            CentralLine() {
                // TODO - change colour of line and circle
                line = mMap.addPolyline(new PolylineOptions()
                        .width(1)
                        .color(Color.RED));

                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(0, 0))
                        .radius(5)
                        .strokeWidth(1)
                        .strokeColor(Color.RED));
            }

            void update() {
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(imageMarker.marker.getPosition());
                points.add(centroid.centroidCircle.getCenter());
                line.setPoints(points);
                circle.setCenter(imageMarker.marker.getPosition());
            }

            void delete() {
                circle.remove();
                line.remove();
            }
        }

        class Popup {
            View popupView;
            PopupWindow popupWindow;

            public Popup() {
                popupView = getLayoutInflater().inflate(R.layout.popup_member_activity_trip, null);
                popupView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        close();
                    }
                });
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
            }

            void update() {
                if (documentSnapshot.getString("ImageURL") != null) {
                    Picasso.get()
                            .load(documentSnapshot.getString("ImageURL"))
                            .into((ImageView) popupView.findViewById(R.id.display_picture));
                }
                ArrayList<String> memberFriends;
                if (documentSnapshot.contains("Friends"))
                    memberFriends = (ArrayList<String>) documentSnapshot.get("Friends");
                else
                    memberFriends = new ArrayList<>();
                if (memberFriends.contains(firebaseAuth.getCurrentUser().getUid()))
                    popupView.findViewById(R.id.friend_status).setBackgroundResource(R.color.wierdRed);
                else
                    popupView.findViewById(R.id.friend_status).setBackgroundResource(R.color.wierdGreen);
                ((TextView) popupView.findViewById(R.id.name)).setText(documentSnapshot.getString("Name"));
                ((TextView) popupView.findViewById(R.id.phone_number)).setText(documentSnapshot.getString("Phone"));
                ((TextView) popupView.findViewById(R.id.email_id)).setText(documentSnapshot.getString("Email"));
                popupView.findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + documentSnapshot.getString("Phone")));
                        startActivity(intent);
                    }
                });
            }

            void show() {
                popupWindow.showAtLocation(findViewById(R.id.trip_relative_layout), Gravity.CENTER, 0, 0);
                currentPopup = this;
            }

            void close() {
                popupWindow.dismiss();
                currentPopup = null;
            }
        }

        class BarItem {
            View barView;

            public BarItem() {
                barView = getLayoutInflater().inflate(R.layout.member_activity_trip, membersBar.membersLL, false);
                ImageView imageView = barView.findViewById(R.id.image);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLng(imageMarker.marker.getPosition()),
                                500,
                                null
                        );
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (currentPopup == null || currentPopup == popup)
                            return false;
                        else if (currentPopup == settings)
                            settings.close();
                        else
                            ((Member.Popup) currentPopup).close();
                        popup.show();
                        return false;
                    }
                });
                membersBar.membersLL.addView(barView, 2);
            }

            void update() {
                ((TextView) barView.findViewById(R.id.name)).setText(documentSnapshot.getString("Name"));
                if (documentSnapshot.getString("ImageURL") != null) {
                    Picasso.get()
                            .load(documentSnapshot.getString("ImageURL"))
                            .into((ImageView) barView.findViewById(R.id.image));
                }
            }

            void delete() {
                membersBar.membersLL.removeView(barView);
            }
        }

    }

    class Image {
        // TODO - build image upload and download
        LatLng location;
        String imageURL;
        String comment;
    }

    class Comment {
        // TODO - build comment upload and download
        LatLng location;
        String commentText;

    }

}
