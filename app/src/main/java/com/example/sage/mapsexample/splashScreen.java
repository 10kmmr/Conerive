package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class splashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private TextView tv;
    private ImageView iv;
    private boolean isLoggedin =false;

    LocationManager lm;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    TextView gpsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        tv = (TextView) findViewById(R.id.splashtxt);
        iv = (ImageView) findViewById(R.id.splashimg);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.splashscreen);
        tv.startAnimation(myanim);
        iv.startAnimation(myanim);
        lm = (LocationManager)getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        gpsText = findViewById(R.id.gpsDisabledText);

    }

    private void checkLocationAndInternetOnAndProceed(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception ex) {}

                if(!gps_enabled){
                    gpsText.setVisibility(View.VISIBLE);
                    handler.postDelayed(this, 100);
                } else {
                    gpsText.setVisibility(View.INVISIBLE);
                    // Check if user is signed in (non-null) and update UI accordingly.
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        isLoggedin = true;
                        NextActivity();
                    } else{
                        Intent notLoggedin = new Intent(getApplication().getApplicationContext(),MainActivity.class);
                        startActivity(notLoggedin);
                    }
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        checkLocationAndInternetOnAndProceed();
    }

    public void NextActivity() {
        final Intent loggedin = new Intent(this, UserCreate.class);
                startActivity(loggedin);

//        final Intent loggedin = new Intent(this, Groupselector.class);
//        database.getReference("Details").child(mAuth.getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                loggedin.putExtra("UserID", mAuth.getUid());
//                loggedin.putExtra("Name", dataSnapshot.getValue().toString());
//                loggedin.putExtra("GroupID", "NULL");
//                startActivity(loggedin);
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) { }
//        });
    }

}
