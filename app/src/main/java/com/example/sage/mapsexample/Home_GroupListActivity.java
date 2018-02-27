package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Home_GroupListActivity extends AppCompatActivity {

    ListView groupList;
    Button createGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_group_list);

        groupList = findViewById(R.id.groupList);
        createGroup = (Button)findViewById(R.id.createGroup);

        ListView yourListView =findViewById(R.id.groupList);




        ArrayList<GroupListDataModel> list = new ArrayList<>();

        GroupListAdapter groupListAdapter = new GroupListAdapter(this, R.layout.group_list_item, list);
        yourListView.setAdapter(groupListAdapter);
    }

    public void NextGroupSelector(){
        Intent mintent = new Intent();
        startActivity(mintent);
    }

    class GroupListAdapter extends ArrayAdapter<GroupListDataModel> {
        public GroupListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public GroupListAdapter(Context context, int resource, ArrayList<GroupListDataModel> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.group_list_item, null);
            }

            GroupListDataModel groupListDataModelItem = getItem(position);

            if (groupListDataModelItem != null) {
                ImageView groupImageTV = view.findViewById(R.id.groupImage);
                TextView groupNameTV = view.findViewById(R.id.groupName);
                TextView memberCountTV = view.findViewById(R.id.memberCount);
                TextView tripCountTV = view.findViewById(R.id.tripCount);
                TextView imageCountTV = view.findViewById(R.id.imageCount);

                groupNameTV.setText(groupListDataModelItem.getGroupName());
            }

            return view;
        }

    }



}
