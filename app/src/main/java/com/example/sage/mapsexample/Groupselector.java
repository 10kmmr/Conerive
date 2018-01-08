package com.example.sage.mapsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Groupselector extends AppCompatActivity {

    //Views
    public Button newGroup;
    public ListView listGroups;
    public EditText gName;
    public EditText gPassword;
    public Button join;

    //firebase
    FirebaseAuth mAuth;
    FirebaseDatabase db;

    //array list to store
    List<String> Groups=new ArrayList<String>();

    Intent goback;

    String userID;
    String groupID;
    String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupselector);

        //Intent receive da
        groupID = getIntent().getStringExtra("GroupID");
        userID = getIntent().getStringExtra("UserID");
        Name = getIntent().getStringExtra("Name");


        //Intent to send
        goback= new Intent(this, MapsActivity.class);


        db=FirebaseDatabase.getInstance();

        //View controllers
        newGroup = (Button)findViewById(R.id.newGroup);
        //listGroups = (ListView)findViewById(R.id.listGroups);
        gName = (EditText)findViewById(R.id.Name);
        gPassword= (EditText)findViewById(R.id.gPassword);
        join = (Button)findViewById(R.id.join);




        //listGroups.setVisibility(View.VISIBLE);
        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newGroup=gName.getText().toString();
                String newgPass =gPassword.getText().toString();

                String pushID =db.getReference("Groups/").push().getKey();
                db.getReference("Groups/"+pushID+"/Name").setValue(newGroup);
                db.getReference("Groups/"+pushID+"/Admin").setValue(userID);
                db.getReference("Groups/"+pushID+"/Password").setValue(newgPass);
                db.getReference("Groups/"+pushID+"/NoOfpeople").setValue(0);
                db.getReference("Details/"+userID+"/Group/"+pushID).setValue(newGroup);


                goback.putExtra("UserID", userID);
                goback.putExtra("Name",Name );
                goback.putExtra("GroupID",pushID);
                startActivity(goback);
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    public void updateUI(){
        newGroup.setVisibility(View.INVISIBLE);
        //listGroups.setVisibility(View.INVISIBLE);
    }


}
