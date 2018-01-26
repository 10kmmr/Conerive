package com.example.sage.mapsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hvpri on 14-01-2018.
 */

public class JoinRegGroupFragment extends Fragment{

    private static final String TAG = "JoinRegGroupFragment";


    //Views
    public Button newGroup;
    public EditText gName;
    public EditText gPassword;
    public Button join;


    //firebase
    FirebaseDatabase db;

    String userID;
    String groupID;
    String Name;
    String newGroupName;


    //DONT TOUCH
    String passW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db= FirebaseDatabase.getInstance();
    }
    public void MakeToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void MakeLog(String mesaage){
        Log.d(TAG, "MakeLog: " + mesaage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jandrgroupfragment,container,false);

        //Intent receive da                         getArguments().getStringExtra("GroupID");
        groupID =getArguments().getString("GroupID");
        userID = getArguments().getString("UserID");
        Name = getArguments().getString("Name");
        MakeLog("From fragment  Name :" +Name + " UserID : " + userID + " groupID : " + groupID);


        //View controllers
        newGroup = (Button)view.findViewById(R.id.newGroup);
        gName = (EditText)view.findViewById(R.id.Name);
        gPassword= (EditText)view.findViewById(R.id.gPassword);
        join = (Button)view.findViewById(R.id.join);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGroupName=gName.getText().toString();
                final String gPass =gPassword.getText().toString();
                String GroupID ;

                //METHORD 1 querry the inside the Structure (nested querrying)
                    //methord1();


                //Method 2 querry the new Structure
                //First find the GroupID for that name
                //then get password
                //then pass to next activity
                    //Set 1
                groupID=null;
                method2(newGroupName , gPass);
            }
        });

        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newGroupName = gName.getText().toString();
                db.getReference("GroupIDTable").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isPresent = false;
                        for(DataSnapshot d : dataSnapshot.getChildren()){
                            if(newGroupName.equals(d.getKey())){
                                isPresent = true;
                                break;
                            }
                        }
                        if(!isPresent){
                            String newgPass = gPassword.getText().toString();
                            String GeneratedGroupID =db.getReference("Groups/").push().getKey();

                            //Writing to the Groups tabs in DB
                            db.getReference("Groups/"+GeneratedGroupID+"/Name").setValue(newGroupName);
                            db.getReference("Groups/"+GeneratedGroupID+"/Admin").setValue(userID);
                            db.getReference("Groups/"+GeneratedGroupID+"/Password").setValue(newgPass);
                            db.getReference("Groups/"+GeneratedGroupID+"/NoOfpeople").setValue(0);

                            //GroupIDTable
                            db.getReference("GroupIDTable/"+newGroupName).setValue(GeneratedGroupID);

                            //writing to the userID group

                            groupID=GeneratedGroupID;
                            NextActivity();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "choose different group name", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        });

        return view;
   }

//        //listGroups.setVisibility(View.VISIBLE);

//    public void updateUI(){
//        newGroup.setVisibility(View.INVISIBLE);

//        //listGroups.setVisibility(View.INVISIBLE);
//    }

    public void methord1(){
        final String Group=gName.getText().toString();
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
    }
    public void  method2(final String gGroup, final String gPass){
        db.getReference("GroupIDTable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot names : dataSnapshot.getChildren()){
                    if(names.getKey().equals(gGroup)) {
                        groupID = names.getValue().toString();
                        MakeLog("in getting groupID :" + groupID);
                        //Set 2
                        Getpassword_handleAsyc(gPass);
                        //Set 3 Security is handled inside that functions only
                    }//fix error handling
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                MakeLog("intent to" + groupID);
                //db.getReference("Details/"+userID+"/Group/"+groupID).setValue(newGroup);
                NextActivity();
            }else{
                MakeToast("HMMMMMM :/ ");
            }else
            MakeToast("no pass");
    }
    public void NextActivity(){
        if(groupID!=null) {
            db.getReference("Details/"+userID+"/Group").child(groupID).setValue(newGroupName, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Intent maps = new Intent(getActivity(), MapsActivity.class);
                    maps.putExtra("UserID", userID);
                    maps.putExtra("Name", Name);
                    maps.putExtra("GroupID", groupID);
                    startActivity(maps);
                }
            });

        }
    }

}
