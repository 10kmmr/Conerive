package com.example.sage.mapsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    boolean isGNameValid = false;
    boolean isGPasswordValid = false;

    //firebase
    FirebaseDatabase db;

    String userID;
    String groupID;
    String Name;
    String newGroupName;
    String passW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db= FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jandrgroupfragment,container,false);

        //Intent receive da                         getArguments().getStringExtra("GroupID");
        groupID =getArguments().getString("GroupID");
        userID = getArguments().getString("UserID");
        Name = getArguments().getString("Name");

        //View controllers
        newGroup = (Button)view.findViewById(R.id.newGroup);
        gName = (EditText)view.findViewById(R.id.Name);
        gPassword= (EditText)view.findViewById(R.id.gPassword);
        join = (Button)view.findViewById(R.id.join);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGNameValid){
                    if(isGPasswordValid){
                        newGroupName=gName.getText().toString();
                        final String gPass =gPassword.getText().toString();
                        String GroupID ;
                        groupID=null;
                        method2(newGroupName , gPass);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "invalid group password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "invalid group name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        newGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isGNameValid){
                    if(isGPasswordValid){
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
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "invalid group password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "invalid group name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        gName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp.length() > 0) {
                    isGNameValid = true;
                } else {
                    isGNameValid = false;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        gPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp.length() > 0) {
                    isGPasswordValid = true;
                } else {
                    isGPasswordValid = false;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        return view;
   }

    public void  method2(final String gGroup, final String gPass){
        db.getReference("GroupIDTable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot names : dataSnapshot.getChildren()){
                    if(names.getKey().equals(gGroup)) {
                        groupID = names.getValue().toString();
                        Getpassword_handleAsyc(gPass);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
    public void Getpassword_handleAsyc(final String gPass){
        db.getReference("Groups/"+groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                passW=dataSnapshot.child("Password").getValue().toString();
                GroupLogin(gPass);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    public void GroupLogin(String pass){
        if(!passW.isEmpty()){
            if(passW.equals(pass)){
                NextActivity();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "invalid group password", Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(getActivity().getApplicationContext(), "no password", Toast.LENGTH_SHORT).show();
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
