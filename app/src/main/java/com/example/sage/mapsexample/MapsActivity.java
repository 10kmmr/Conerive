package com.example.sage.mapsexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

//
import android.location.*;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    public Button button;

    //objects
    ArrayList<User> users;
    ArrayList<String> phoneNos; //get list of users from

    private static final String TAG = "MyActivity";

    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;

    //firebase
    FirebaseDatabase database;
    //GeoQuery geoQuery;
        GeoFire geoFire;

    //
    String CurrentUser="";

    //references to DB
    DatabaseReference myRef;
    DatabaseReference UserRef;
    DatabaseReference LocationRef;
    DatabaseReference refGeoData;
    DatabaseReference UserObjRef;

    //Controller variables
    public  String usr = "User1";
    public boolean usrSwitch=false;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_PHONE_CALL = 100;
    private static final int PERMISSIONS_REQUEST_SMS_MESSAGE = 100;




    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ask permissions
        getPhonePermission();
        getLocationPermission();


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        button =(Button)findViewById(R.id.button3);

        final Toast toast = Toast.makeText(this, "location optained", Toast.LENGTH_LONG);

        //IME NUMBER OBTAINING
        TelephonyManager  tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        CurrentUser =tm.getImei();

        //Location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        //firebase testing--------------------------------------------------------------------------//


        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("/path/to/geofire/User2");
        UserObjRef = database.getReference("User");//Abhijeeth's code
        LocationRef = database.getReference("Location");
        refGeoData=database.getReference("Location");

        //Writing geo location
        geoFire = new GeoFire(LocationRef);


        //initialization of A
        phoneNos= new ArrayList<String>();
        users = new ArrayList<User>();

        //add IME number
        UserRef=database.getReference("User/"+CurrentUser+"/Name");
        UserRef.setValue("Prithvi");

        //get List of user object
        UserObjRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    String mobileNumber = data.getKey();
                    phoneNos.add(mobileNumber);
                    String name = "";
                    for(DataSnapshot d : data.getChildren()){
                        name = d.getValue(String.class);
                    }
                    users.add(new User(mobileNumber, name));
                }
                //logging data
                for(User u : users)
                    Log.d(TAG, "user is: " + u);

                Log.d(TAG, "phoneNos : in onCreate :  "+phoneNos.toString());
                setMarkers();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        //checking objects list


//        geoFire.setLocation(usr, new GeoLocation(37.7853889, -122.4056973));

        //------------------------------------------------------------------------------------------//



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
//        if(!usrSwitch)
//            usr="User2";
//        else
//            usr="User1";
//        try {
//            if (mLocationPermissionGranted) {
//                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
//                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful()) {
//                            mLastKnownLocation = task.getResult();
//                            /* SEND FIREBASE THE DATA*/
//                            geoFire.setLocation(usr, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//                            Log.d(TAG, "onComplete: Wrote to DB");
////                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
//                        } else {
//                            ErrorT();
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }

        Toast.makeText(this, "YENO", Toast.LENGTH_LONG).show();


    }




    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_PHONE_CALL);
            mLocationPermissionGranted = true;}
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    1);
//        }
    }
    private void getPhonePermission(){
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_PHONE_CALL);
            //mLocationPermissionGranted = true;
        }
//        else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE},
//                    1);
//        }

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

        //GETTING LOCATION AND WRITING TO DB
        @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
                    /* SEND FIREBASE THE DATA*/
                    geoFire.setLocation(CurrentUser, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    Log.d(TAG, "onComplete: Wrote to DB: onMapReady ");
                } else {
                    //Log.e(TAG, "Exception: %s", task.getException());
                    ErrorT();
                }
            }
        });
        Log.d(TAG, "phoneNos : in onMapReady() :  "+phoneNos.toString());
        setMarkers();


        //real time rendering

        refGeoData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setMarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                ErrorT();
            }
        });

    }


    public void setMarkers(){

        //BAD CODE (WET)

//        geoFire.getLocation("User1", new LocationCallback() {
//            @Override
//            public void onLocationResult(String key, GeoLocation location) {
//                mMap.moveCamera(CameraUpdateFactory
//                        .newLatLngZoom(new LatLng(location.latitude, location.longitude), 15));
//                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("User1"));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                mMap.moveCamera(CameraUpdateFactory
//                        .newLatLngZoom(new LatLng(-34, 151), 15));
//            }
//        });
//
//
//
//        geoFire.getLocation("User2", new LocationCallback() {
//            @Override
//            public void onLocationResult(String key, GeoLocation location) {
//                mMap.moveCamera(CameraUpdateFactory
//                        .newLatLngZoom(new LatLng(location.latitude, location.longitude), 15));
//                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("User2"));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                mMap.moveCamera(CameraUpdateFactory
//                        .newLatLngZoom(new LatLng(-38, 151), 15));
//            }
//        });

        Log.d(TAG,"Starting to set markers");
        Log.d(TAG, "phoneNos : in setmarker() :  "+phoneNos.toString());
        for(String IME : phoneNos){
            geoFire.getLocation(IME, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.latitude, location.longitude), 15));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("User :)"));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ErrorT();
                }
            });
            Log.d(TAG, "Setting marker for "+ IME);
        }








    }
    public void ErrorT(){
        Toast.makeText(this, "Error ", Toast.LENGTH_LONG).show();
    }


    //Asyc function handling
//    public interface OnGetDataListener {
//        public void onStart();
//        public void onSuccess(DataSnapshot data);
//        public void onFailed(DatabaseError databaseError);
//    }
//    public void mReadDataOnce(DatabaseReference R,OnGetDataListener listener) {
//        listener.onStart();
//        R.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                listener.onSuccess(dataSnapshot);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                listener.onFailed(databaseError);
//            }
//        });
//    }
//
//











}
//default marker
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
//private void writeDB() {
//    try {
//        if (mLocationPermissionGranted) {
//            Task<Location> locationResult = mFusedLocationClient.getLastLocation();
//            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                @Override
//                public void onComplete(@NonNull Task<Location> task) {
//                    if (task.isSuccessful()) {
//                        mLastKnownLocation = task.getResult();
//                            /* SEND FIREBASE THE DATA*/
//                        geoFire.setLocation(CurrentUser, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//                        Log.d(TAG, "onComplete: Wrote to DB");
////                          mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Marker in Sydney"));
//                    } else {
//                        Log.d(TAG, "Current location is null. Using defaults.");
//                        Log.e(TAG, "Exception: %s", task.getException());
//                        mMap.moveCamera(CameraUpdateFactory
//                                .newLatLngZoom(new LatLng(-34, 151), 15));
//                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
//                    }
//                }
//            });
//        }
//    } catch (SecurityException e)  {
//        Log.e("Exception: %s", e.getMessage());
//    }
//}