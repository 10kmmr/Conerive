package com.example.sage.mapsexample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Groupselector extends AppCompatActivity {

    //Views
    public Button newGroup;
    public ListView listGroups;

    //firebase
    FirebaseAuth mAuth;
    FirebaseDatabase db;

    //array list to store
    List<String> Groups=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupselector);

        db=FirebaseDatabase.getInstance();

        newGroup = (Button)findViewById(R.id.newGroup);
        listGroups = (ListView)findViewById(R.id.listGroups);






        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
    }
    public void updateUI(){
        newGroup.setVisibility(View.INVISIBLE);
        listGroups.setVisibility(View.INVISIBLE);
    }

}
