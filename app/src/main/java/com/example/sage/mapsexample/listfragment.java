package com.example.sage.mapsexample;

/**
 * Created by sage on 13/1/18.
 */
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class listfragment extends Fragment{
    private ListView listGroups;
    String[] groupsArray;
    private String name;
    private String Uid;


    public TextView listcontent;


    public FirebaseDatabase db;
    private FirebaseAuth mAuth;



    private static final String TAG = "ListFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MakeLog("Fragment working ");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.list_fragment,container,false);
        listGroups = view.findViewById(R.id.listView2);


        //styling
        listcontent = (TextView) view.findViewById(R.id.list_content);
        int widthlist = view.getLayoutParams().width;
        Log.d(TAG, "onCreateView: " + widthlist);
        listGroups.setPadding(100,300,100,300);
        








//        name = ;
        Uid = mAuth.getUid();

        db = FirebaseDatabase.getInstance();
        db.getReference("Details/"+Uid+"Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.getValue(String.class);
                DatabaseReference groupsReference = db.getReference("Details/"+Uid+"/Group");
                groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final HashMap<String, String> groupsMap = new HashMap<>();
                        for(DataSnapshot d : dataSnapshot.getChildren()){
                            groupsMap.put(d.getValue(String.class), d.getKey());
                        }
                        groupsArray = new String[groupsMap.size()];
                        int i=0;
                        for(Map.Entry<String, String> a : groupsMap.entrySet()){
                            groupsArray[i] = a.getKey();
                            i++;
                        }
                        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, groupsArray){
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view =super.getView(position, convertView, parent);

                                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                                textView.setTextColor(Color.BLUE);
                                return view;
                            }
                        };
                        listGroups.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.list_fragment_item, R.id.list_content, groupsArray));

                        listGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                TextView tv = view.findViewById(R.id.list_content);
                                Log.d(TAG, "group name: "+tv.getText().toString());
                                Log.d(TAG, "groupID: "+groupsMap.get(tv.getText().toString()));
                                Intent maps = new Intent(getActivity(), MapsActivity.class);
                                maps.putExtra("UserID", Uid);
                                maps.putExtra("Name", name);
                                maps.putExtra("GroupID", groupsMap.get(tv.getText().toString()));
                                startActivity(maps);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });



        return view;
    }
    public void MakeLog(String mesaage){
        Log.d(TAG, "MakeLog: " +mesaage);
    }
}
