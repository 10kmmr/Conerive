
package com.example.sage.mapsexample;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public String groupID;
    public String userID;
    public String Name;
    Spinner spinner;
    ArrayAdapter<String> adapter;

    private PopupWindow mPopupWindow;

    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    LocationRequest mLocationRequest;

    FirebaseDatabase database;
    GeoFire ownerGeoFireObject;

    DatabaseReference groupReference;
    DatabaseReference ownerReference;

    Circle limitCircle;
    Circle inclusiveCircle;

    double limitRadius = 4000;
    double inclusiveRadius = 0;


    HashMap<String, GroupMember> users = new HashMap<>();

    private static final String TAG = "MyActivity";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        groupID = getIntent().getStringExtra("GroupID");
        Log.d(TAG, "groupID is : " + groupID);
        userID = getIntent().getStringExtra("UserID");
        Name = getIntent().getStringExtra("Name");
        database = FirebaseDatabase.getInstance();
        groupReference = database.getReference("Root/"+groupID);
        ownerReference = database.getReference("Root/"+groupID+"/"+userID);
        ownerGeoFireObject = new GeoFire(ownerReference);
        spinner = (Spinner)findViewById(R.id.groupMembers);
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, new ArrayList<String>());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               try{
                GroupMember temp = users.get(spinner.getSelectedItem().toString());
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(temp.userLocation.latitude, temp.userLocation.longitude), 15));
               } catch (Exception e){ e.printStackTrace(); }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //GETTING LOCATION AND WRITING TO DB
        @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {

                    mLastKnownLocation = task.getResult();
                    ownerGeoFireObject.setLocation("Location", new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    googleMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 15));
                    //retrieve all users in group
                    getUsers();
                } else { ErrorT(); }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "onMarkerClick: "+marker.getTag().toString());
                HashMap<String, String> tag = (HashMap<String, String>)marker.getTag();

                String NameCurrent = tag.get("Name");
                final String NumberCurrent = tag.get("MobileNumber");
                String EmailCurrent = tag.get("Email");

                Toast.makeText(MapsActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();

                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.user_data_popup,null);
                mPopupWindow = new PopupWindow(
                        customView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                if(Build.VERSION.SDK_INT>=21){
                    mPopupWindow.setElevation(5.0f);
                }

                Button close = customView.findViewById(R.id.Close);
                Button call = customView.findViewById(R.id.Call);
                call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        NextDialer(NumberCurrent);
                    }
                });
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPopupWindow.dismiss();
                    }
                });

                TextView name = customView.findViewById(R.id.Name);
                name.setText(NameCurrent);
                TextView email = customView.findViewById(R.id.Email);
                email.setText(EmailCurrent);
                TextView mobileNumber = customView.findViewById(R.id.MobileNumber);
                mobileNumber.setText(NumberCurrent);
                /*
                mPopupWindow.showAtLocation(findViewById(R.id.MapsActivity), Gravity.CENTER,0,0);
                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mPopupWindow.setOutsideTouchable(true);
                */

                return false;
            }
        });

        resetCircle();

    }

    public void getUsers() {

        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userID = dataSnapshot.getKey();
                users.put(userID, new GroupMember(userID, groupID, mMap, database, adapter));
                Log.d(TAG, "map :"+users.keySet());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                users.get(dataSnapshot.getKey()).releaseListener();
                users.remove(dataSnapshot.getKey());
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void resetCircle() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                try {
                    ArrayList<Location> tempArray = new ArrayList<>();
                    double latSum = 0.0, lngSum = 0.0;

                    if (users != null && users.size() > 0) {
                        for (Map.Entry<String, GroupMember> u : users.entrySet()) {
                            Location temp = new Location(LocationManager.GPS_PROVIDER);
                            temp.setLongitude(u.getValue().userLocation.longitude);
                            temp.setLatitude(u.getValue().userLocation.latitude);
                            tempArray.add(temp);
                            latSum += temp.getLatitude();
                            lngSum += temp.getLongitude();
                        }
                    }
                    Location midPoint = new Location(LocationManager.GPS_PROVIDER);
                    midPoint.setLatitude(latSum/users.size());
                    midPoint.setLongitude(lngSum/users.size());
                    double inclusiveRadius = 0;
                    if (users != null && users.size() > 0) {
                        for (Map.Entry<String, GroupMember> u : users.entrySet()) {
                            Location temp = new Location(LocationManager.GPS_PROVIDER);
                            temp.setLongitude(u.getValue().userLocation.longitude);
                            temp.setLatitude(u.getValue().userLocation.latitude);
                            double distanceFromMid = midPoint.distanceTo(temp);
                            if(distanceFromMid>inclusiveRadius)
                                inclusiveRadius = distanceFromMid;
                        }
                    }

                    if(limitCircle == null) {
                        limitCircle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(midPoint.getLatitude(), midPoint.getLongitude()))
                                .radius(limitRadius)
                                .strokeColor(Color.GREEN));
                    } else {
                        limitCircle.setCenter(new LatLng(midPoint.getLatitude(), midPoint.getLongitude()));
                    }

                    if(inclusiveCircle == null) {
                        inclusiveCircle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(midPoint.getLatitude(), midPoint.getLongitude()))
                                .radius(inclusiveRadius)
                                .strokeColor(Color.BLUE));
                    } else {
                        inclusiveCircle.setCenter(new LatLng(midPoint.getLatitude(), midPoint.getLongitude()));
                        inclusiveCircle.setRadius(inclusiveRadius+ 100);
                        if(inclusiveRadius>limitRadius){
                            inclusiveCircle.setStrokeColor(Color.RED);
                        } else {
                            inclusiveCircle.setStrokeColor(Color.BLUE);
                        }
                    }

                } catch (Exception e){
                    handler.postDelayed(this, 80);
                } finally {
                    handler.postDelayed(this, 160);
                }
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

            }
        }
    };
    @SuppressLint("MissingPermission") //check if this throws an error later
    public void NextDialer(String number){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(intent);
    }


}
