package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class splashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private TextView tv;
    private ImageView iv;
    private boolean isLoggedin =false;
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

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            isLoggedin = true;
            NextActivity();
        }else{
            Intent notLoggedin = new Intent(this,MainActivity.class);
            startActivity(notLoggedin);
        }
    }

    public void NextActivity() {
        final Intent loggedin = new Intent(this, Groupselector.class);
        database.getReference("Details").child(mAuth.getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loggedin.putExtra("UserID", mAuth.getUid());
                loggedin.putExtra("Name", dataSnapshot.getValue().toString());
                loggedin.putExtra("GroupID", "NULL");
                startActivity(loggedin);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
