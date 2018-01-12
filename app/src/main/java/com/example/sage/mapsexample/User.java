package com.example.sage.mapsexample;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;

public class User {
    String userID;
    String name;
    GoogleMap googleMap;
    Marker marker;
    DatabaseReference userReference;
    GeoFire geoFire;

    @Override
    public String toString(){
        return userID + " : " + name;
    }
}
