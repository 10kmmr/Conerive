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

    String userID;
    String groupID;
    String Name;




    //DONT TOUCH
    String passW;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupselector);

        //Intent receive da
        groupID = getIntent().getStringExtra("GroupID");
        userID = getIntent().getStringExtra("UserID");
        Name = getIntent().getStringExtra("Name");


        db=FirebaseDatabase.getInstance();

        //View controllers
        newGroup = (Button)findViewById(R.id.newGroup);
        //listGroups = (ListView)findViewById(R.id.listGroups);
        gName = (EditText)findViewById(R.id.Name);
        gPassword= (EditText)findViewById(R.id.gPassword);
        join = (Button)findViewById(R.id.join);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Group=gName.getText().toString();
                final String gPass =gPassword.getText().toString();

                String GroupID ;
                //METHORD 1 querry the inside the Structure
/*
                db.getReference("Groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot GroupIDs:dataSnapshot.getChildren())
                           for(DataSnapshot GroupIDelements:GroupIDs.getChildren()){
                                String keyValue=GroupIDelements.getKey();
                                if(keyValue.equals("Name"))
                                    if(GroupIDelements.getValue().toString().equals(Group))
                                        MakeLog("FOUND " + GroupIDelements.getValue().toString());
                           }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
*/



                //Method 2 querry the new Structure
                //First find the GroupID for that name
                //then get password
                //then pass to next activity
                //Set 1
                groupID="NULL";
                db.getReference("GroupIDTable").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot names : dataSnapshot.getChildren()){
                            if(names.getKey().equals(Group)) {
                                groupID = names.getValue().toString();
                                MakeLog("in getting groupID :" + groupID);
                                //Set 2
                                Getpassword_handleAsyc(gPass);
                                //Set 3 Security is handled inside that functions only
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        //listGroups.setVisibility(View.VISIBLE);
        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newGroup=gName.getText().toString();
                String newgPass =gPassword.getText().toString();

                String GeneratedGroupID =db.getReference("Groups/").push().getKey();
                db.getReference("Groups/"+GeneratedGroupID+"/Name").setValue(newGroup);
                db.getReference("GroupIDTable/"+newGroup).setValue(GeneratedGroupID);
                db.getReference("Groups/"+GeneratedGroupID+"/Admin").setValue(userID);
                db.getReference("Groups/"+GeneratedGroupID+"/Password").setValue(newgPass);
                db.getReference("Groups/"+GeneratedGroupID+"/NoOfpeople").setValue(0);
                db.getReference("Details/"+userID+"/Group/"+GeneratedGroupID).setValue(newGroup);
                groupID=newGroup;
                NextActivity();
            }
        });
    }
    public void updateUI(){
        newGroup.setVisibility(View.INVISIBLE);
        //listGroups.setVisibility(View.INVISIBLE);
    }
    public void NextActivity(){
        Intent maps=new Intent(this,MapsActivity.class);
        maps.putExtra("UserID", userID);
        maps.putExtra("Name",Name );
        maps.putExtra("GroupID",groupID);
        startActivity(maps);
    }
    public void MakeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    public void MakeLog(String mesaage){
        Log.d(TAG, "MakeLog: " +mesaage);
    }
    public void Getpassword_handleAsyc(final String gPass){
            MakeLog("before asyc"+groupID);
            db.getReference("Groups/"+groupID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MakeLog("insdie "+dataSnapshot.toString());
                    passW=dataSnapshot.child("Password").getValue().toString();
                    //Set 3 Security
                    GroupLogin(gPass);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }
    public void GroupLogin(String pass){
       if(!passW.isEmpty())
            if(passW.equals(pass)){
                NextActivity();
            }else{
                MakeToast("HMMMMMM :/ ");
            }
        else
            MakeToast("no pass");
    }
}
