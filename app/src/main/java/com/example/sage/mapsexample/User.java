package com.example.sage.mapsexample;
import android.util.Log;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static android.content.ContentValues.TAG;

public class User {
    String mobileNumber;
    String name;
    String groupID;
    DatabaseReference userReference;

    /*
        GeoQueryDataEventListener Data;
        GeoQuery g;
        DataSnapshot d;
        GeoLocation lowlow;
    */
    GoogleMap googleMap;
    Marker marker;
    ValueEventListener listener;

    public User(String mobileNumber, String groupID, final GoogleMap googleMap , FirebaseDatabase Db){
        this.mobileNumber = mobileNumber;
        this.groupID = groupID;
        this.googleMap = googleMap;

       /* g.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "onKeyEntered: ");
                marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(name));
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        */ // OLD CODE
        Log.d(TAG, "user-path :"+"Root/"+this.groupID+"/"+this.mobileNumber);
        userReference = Db.getReference("Root/"+this.groupID+"/"+this.mobileNumber);
        listener = userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GeoFire geoFire = new GeoFire(userReference);
                name = dataSnapshot.child("Name").getValue(String.class);
                geoFire.getLocation("Location", new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        Log.d(TAG, "location of  "+name+" has changed");
                        if(marker!=null)
                            marker.remove();
                        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(name));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public String toString(){
        return groupID+ " : " +mobileNumber + " : " + name;
    }

    public void releaseListener(){
        userReference.removeEventListener(listener);
    }
}