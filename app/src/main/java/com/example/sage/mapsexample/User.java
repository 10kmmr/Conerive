package com.example.sage.mapsexample;
import android.util.Log;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
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
    String name = "randomName";
    String groupID;
    FirebaseDatabase database;
    DatabaseReference userReference;
    GoogleMap googleMap;
    Marker marker;

    public User(String mobileNumber, String groupID, final GoogleMap googleMap){
        this.mobileNumber = mobileNumber;
        this.groupID = groupID;
        this.googleMap = googleMap;
        database = FirebaseDatabase.getInstance();

        Log.d(TAG, "user-path :"+"Root/"+this.groupID+"/"+this.mobileNumber);
        userReference = database.getReference("Root/"+this.groupID+"/"+this.mobileNumber);
        userReference.addValueEventListener(new ValueEventListener() {
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



}
