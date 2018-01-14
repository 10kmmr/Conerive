package com.example.sage.mapsexample;

/**
 * Created by sage on 13/1/18.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;


public class listfragment extends Fragment{
    private ListView listGroups;
    private Button switchButon;

    FirebaseDatabase db;

    private static final String TAG = "ListFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MakeLog("Fragment working ");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.listfregment,container,false);



        switchButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //listGroups.setAdapter();

        //db.getReference("/Details/")
        return view;
    }
    public void MakeLog(String mesaage){
        Log.d(TAG, "MakeLog: " +mesaage);
    }
}
