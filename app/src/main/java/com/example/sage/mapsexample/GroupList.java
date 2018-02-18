package com.example.sage.mapsexample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GroupList extends AppCompatActivity {

    ListView groupList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        groupList = findViewById(R.id.groupList);
        ListView yourListView =findViewById(R.id.groupList);

        ArrayList<GroupListDataModel> list = new ArrayList<>();

        GroupListAdapter groupListAdapter = new GroupListAdapter(this, R.layout.group_list_item, list);
        yourListView.setAdapter(groupListAdapter);
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
