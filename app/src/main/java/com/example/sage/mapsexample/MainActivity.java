package com.example.sage.mapsexample;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_PHONE_STATE};

    int PERMISSION_ALL = 1;
    private static final int reqLoc=1;
    private static final int reqPho=1;
    private static final String TAG = "MainActivity" ;
    Intent intent;
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        else
            startActivity(intent);

//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    1);
//        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void getPhonePermission(){
        Log.d(TAG, "getPhonePermission: ");
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, reqPho);
            //mLocationPermissionGranted = true;
        }
//        else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_PHONE_STATE},
//                    1);
//        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent=new Intent(this,MapsActivity.class);
        getLocationPermission();


        


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: requestcode"+requestCode);
        Log.d(TAG, "onRequestPermissionsResult: permission"+permissions.toString());
        Log.d(TAG, "onRequestPermissionsResult: grantResult"+grantResults.toString());
        if (requestCode == PERMISSION_ALL) {
            if(grantResults.length == 3) {
                // We can now safely use the API we requested access to
                Log.d(TAG, "onRequestPermissionsResult: if condition");
                startActivity(intent);
                
            } else {
                // Permission was denied or request was cancelled
                Log.d(TAG, "onRequestPermissionsResult: else");
            }
        }
    }

}
