package com.example.sage.mapsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Groupselector extends AppCompatActivity {

    private static final String TAG = "Groupselector";

    public Button button2;
    public void MakeLog(String mesaage){
        Log.d(TAG, "MakeLog: " + mesaage);
    }
    public void MakeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupselector);
        MakeLog("OnCreate : GroupSelector");
        View frag = findViewById(R.id.fragment_container);
        if (frag != null) {
            MakeLog("Started making Fragment");
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                MakeLog("null saved instace state");
                // return;
            }

            // Create a new Fragment to be placed in the activity layout

            JoinRegGroupFragment joinReg = new JoinRegGroupFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            joinReg.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, joinReg).commit();
            MakeLog("Done making everything");
        } else {
            MakeLog("NULL");
            MakeToast("else part NULL");
        }
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: hellow world");
                listfragment list = new listfragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, list).commit() ;
            }
        });
    }
}
