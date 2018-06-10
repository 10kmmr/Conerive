package com.example.sage.mapsexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    GoogleSignInClient mGoogleSignInClient;

    private boolean zoomedIn;
    private ArrayList<Trip> trips;

    private Bitmap mBitmapToSave;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;


    /** Build a Google SignIn client. */
    private GoogleSignInClient buildGoogleSignInClient() {
        String serverClientId = getString(R.string.server_client_id);
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .requestServerAuthCode(serverClientId)
//                        .build();
//        return GoogleSignIn.getClient(this, signInOptions);
//
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        return  GoogleSignIn.getClient(this,gso);
    }
    private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        Log.i(TAG, "New contents created.");
        // Get an output stream for the contents.
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e) {
            Log.w(TAG, "Unable to write file contents.", e);
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle("Android Photo.png")
                        .build();
        // Set up options to configure and display the create file activity.
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                            return null;
                        });
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                writeAuthcode( task );
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.
                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Start camera.
                    //writeAuthcode(mGoogleSignInClient.getSignInIntent());
//                    startActivityForResult(
//                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
            case REQUEST_CODE_CAPTURE_IMAGE:
                Log.i(TAG, "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    saveFileToDrive();
                }
                break;
            case REQUEST_CODE_CREATOR:
                Log.i(TAG, "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    startActivityForResult(
                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task -> createFileIntentSender(task.getResult(), image))
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new contents.", e));
    }
    public void writeAuthcode(Task<GoogleSignInAccount> completedTask){

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String authCode = account.getServerAuthCode();
            Log.d(TAG, "writeAuthcode: got authcode :=" + authCode);

        } catch (Exception e) {
            Log.w(TAG, "Sign-in failed", e);
        }
//        GoogleSignInAccount account2 =GoogleSignIn.getLastSignedInAccount(this);
//        String authCode = account2.getServerAuthCode();
//        Log.d(TAG, "writeAuthcode: got authcode :=" + authCode);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        //startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);


//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if(account!=null){
//            Log.d(TAG, "onCreate: Havnt handled it yet");
//        }else{
//            Log.d(TAG, "Testing: not signed into google");
//            mGoogleSignInClient = buildGoogleSignInClient();
//            startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//        }
        Log.d(TAG, "Testing: not signed into google");
        mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);


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
        mMap.getUiSettings().setMapToolbarEnabled(false);

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
                            for(final String trip : tripIds){
                                firestoreDB.collection("TRIPS").document(trip)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>(){
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if(documentSnapshot.getData() != null) {
                                                    trips.add(new Trip(
                                                            documentSnapshot.getId(),
                                                            documentSnapshot.getString("Name"),
                                                            documentSnapshot.getGeoPoint("Destination"),
                                                            documentSnapshot.getString("AdmingID"),
                                                            documentSnapshot.getDouble("Radius"),
                                                            ((ArrayList<String>) documentSnapshot.get("Users")).size()));
                                                } else {
                                                    dbRemoveTrip(trip);
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

    void dbRemoveTrip(final String tripId){
        firestoreDB.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference reference = firestoreDB.collection("USERS").document(mAuth.getCurrentUser().getUid());
                DocumentSnapshot snapshot = transaction.get(reference);
                ArrayList<String> tripIds = (ArrayList<String>)snapshot.get("Trips");
                tripIds.remove(tripId);
                transaction.update(reference, "Trips", tripIds);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: " + "removed trip " + tripId);
            }
        }).addOnFailureListener(new OnFailureListener() {
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



