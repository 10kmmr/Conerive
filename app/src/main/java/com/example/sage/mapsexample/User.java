package com.example.sage.mapsexample;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by rkinabhi on 29-12-2017.
 */

public class User {
    String mobileNumber;
    String name;


    public User(String mobileNumber, String name){
        this.mobileNumber = mobileNumber;
        this.name = name;
    }
    public String toString(){
        return mobileNumber + " : " + name;
    }



}
