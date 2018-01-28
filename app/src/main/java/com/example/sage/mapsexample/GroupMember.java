package com.example.sage.mapsexample;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class GroupMember extends User{

    String groupID;
    ValueEventListener listener;
    GeoLocation userLocation;

    public GroupMember(final String userID, final String groupID, final GoogleMap googleMap , final FirebaseDatabase database,final ArrayAdapter<String> adapter){
        this.userID = userID;
        this.groupID = groupID;
        this.googleMap = googleMap;
        userReference = database.getReference("Root/"+this.groupID+"/"+this.userID);
        geoFire = new GeoFire(userReference);

        database.getReference("Details/"+userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                name =  dataSnapshot.child("Name").getValue(String.class);
                adapter.add(name);
                mobileNumber = dataSnapshot.child("phonenumber").getValue(String.class);
                emailID = dataSnapshot.child("Email").getValue(String.class);

                listener = userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        geoFire.getLocation("Location", new LocationCallback() {
                            @Override
                            public void onLocationResult(String key, GeoLocation location) {
                                userLocation = location;
                                try{
                                    if(marker!=null) {
                                        marker.setPosition(new LatLng(location.latitude, location.longitude));
                                    } else {
                                        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                                .title(name)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));

                                    }
                                    HashMap<String, String> userData = new HashMap<>();
                                    userData.put("Name", name);
                                    userData.put("Email", emailID);
                                    userData.put("MobileNumber", mobileNumber);
                                    marker.setSnippet("mobile: "+mobileNumber);
                                    marker.setTitle(name);
                                    marker.setTag(userData);
                                } catch (NullPointerException e){
                                    e.printStackTrace();
                                    releaseListener();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public String toString(){
        return groupID+ " : " +userID + " : " + name;
    }

    public void releaseListener(){
        if (marker!=null)
            marker.remove();
        userReference.removeEventListener(listener);
    }
}