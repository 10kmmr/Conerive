package com.example.sage.mapsexample;

/**
 * Created by sage on 13/1/18.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.FirebaseDatabase;


public class listfragment extends Fragment{
    private ListView listGroups;
    private ListView SubjectsListV;

    FirebaseDatabase db;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.listfregment,container,false);

        //listGroups =(ListView)view.findViewById(R.id.list);

        //listGroups.setAdapter();

        //db.getReference("/Details/")
        return view;
    }

}
