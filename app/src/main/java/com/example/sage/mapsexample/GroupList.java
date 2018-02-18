package com.example.sage.mapsexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class GroupList extends AppCompatActivity {

    ListView groupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        groupList = findViewById(R.id.groupList);

    }
}
