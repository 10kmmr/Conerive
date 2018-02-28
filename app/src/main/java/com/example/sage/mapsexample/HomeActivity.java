package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    ListView groupsListView;
    Button createGroupButton;
    ArrayList<GroupListDataModel> groupsList;
    public String baseUrl = "http://192.168.2.2:8080/";
    private FirebaseAuth mAuth;
    public RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        groupsListView = findViewById(R.id.groupList);
        createGroupButton = findViewById(R.id.createGroup);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        groupsList = new ArrayList<>();
        dbGetGroupList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GroupCreateActivity.class);
                startActivity(intent);
            }
        });
    }

    void dbGetGroupList(){
        String url = baseUrl +"groups/groupList/"+mAuth.getUid();
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        for (int i=0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String groupName = jsonObject.getString("Group_name");
                                String groupDisplayPictureURL = jsonObject.getString("Group_Display_picture");
                                int memberCount = jsonObject.getInt("Member_count");
                                int tripCount = jsonObject.getInt("Trip_count");
                                int imageCount = jsonObject.getInt("Image_count");
                                groupsList.add(
                                        new GroupListDataModel(
                                                groupName,
                                                groupDisplayPictureURL,
                                                "69",
                                                memberCount,
                                                tripCount,
                                                imageCount
                                        )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        GroupListAdapter groupListAdapter = new GroupListAdapter(getApplicationContext(), R.layout.group_list_item, groupsList);
                        groupsListView.setAdapter(groupListAdapter);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        requestQueue.add(getRequest);
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
                Log.d(TAG, "getView: ");
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.group_list_item, null);
            }

            GroupListDataModel groupListDataModelItem = getItem(position);

            if (groupListDataModelItem != null) {
                NetworkImageView groupImageNIV = view.findViewById(R.id.groupImage);
                TextView groupNameTV = view.findViewById(R.id.groupName);
                TextView memberCountTV = view.findViewById(R.id.member_count);
                TextView tripCountTV = view.findViewById(R.id.tripCount);
                TextView imageCountTV = view.findViewById(R.id.imageCount);

                ImageLoader imageLoader;
                imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> mCache = new LruCache<>(10);
                    public void putBitmap(String url, Bitmap bitmap) {
                        mCache.put(url, bitmap);
                    }
                    public Bitmap getBitmap(String url) {
                        return mCache.get(url);
                    }
                });
                groupImageNIV.setImageUrl(groupListDataModelItem.getGroupDisplayPictureURL(), imageLoader);
                groupNameTV.setText(groupListDataModelItem.getGroupName());
                memberCountTV.setText("member count : "+Integer.toString(groupListDataModelItem.getMemberCount()));
                tripCountTV.setText("trip count : "+Integer.toString(groupListDataModelItem.getTripCount()));
                imageCountTV.setText("image count : "+Integer.toString(groupListDataModelItem.getImageCount()));
            }
            return view;
        }
    }
}
