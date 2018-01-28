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

    private int switchView = 1;

    listfragment list = new listfragment();
    JoinRegGroupFragment joinReg = new JoinRegGroupFragment();

    public Button button2;
    public void MakeLog(String mesaage){

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
            Log.d(TAG, "MakeLog: " + "Started making Fragment");
            if (savedInstanceState != null) {
                Log.d(TAG, "MakeLog: " + "null save instance state");
            }

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, list).commit();
            button2 = (Button) findViewById(R.id.button2);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (switchView == 1) {
                        joinReg.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, joinReg).commit();
                        button2.setText("Back");
                        switchView = 0;
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, list).commit();
                        button2.setText("ADD GROUP");
                        switchView = 1;
                    }


                }
            });
        }
    }
}
