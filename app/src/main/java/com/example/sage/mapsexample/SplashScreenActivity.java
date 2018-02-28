package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private String baseUrl = "http://192.168.2.2:8080/";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView tv;
    private ImageView iv;
    private LocationManager lm;
    private boolean gps_enabled = false;
    private TextView gpsText;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        tv = findViewById(R.id.splashtxt);
        iv = findViewById(R.id.splashimg);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splashscreen);
        requestQueue = Volley.newRequestQueue(this);
        tv.startAnimation(animation);
        iv.startAnimation(animation);
        lm = (LocationManager)getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        gpsText = findViewById(R.id.gpsDisabledText);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkLocationAndInternetOnAndProceed();
    }

    private void checkLocationAndInternetOnAndProceed(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception e) {
                    Log.d(TAG, "run: " + e.toString());
                }
                if(!gps_enabled){
                    gpsText.setVisibility(View.VISIBLE);
                    handler.postDelayed(this, 100);
                } else {
                    gpsText.setVisibility(View.INVISIBLE);
                    checkIsLoggedIn();
                }
            }
        });
    }

    void checkIsLoggedIn() {
        if (currentUser == null) {
            Intent intent = new Intent(this,PhoneAuthenticationActivity.class);
            startActivity(intent);
        } else{
            checkUserProfileExists();
        }
    }

    void checkUserProfileExists() {
        String url = baseUrl+"users/" + currentUser.getUid();
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        if(response.length()>0){
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(getApplicationContext(),UserCreateActivity.class);
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        requestQueue.add(getRequest);
    }

}
