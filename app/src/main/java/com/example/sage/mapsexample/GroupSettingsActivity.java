package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupSettingsActivity extends AppCompatActivity {

    private static final String TAG = "GroupSettingsActivity";
    String groupId;
    String groupName;
    String groupDisplayPictureURL;

    Toolbar toolbar;
    FloatingActionButton addUserFAB;
    TextView groupNameTV;
    NetworkImageView groupDisplayPictureNIV;
    ListView userListView;

    ArrayList<UserListDataModel> usersList;
    public String baseUrl;
    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseUrl = getString(R.string.api_url);
        setContentView(R.layout.activity_group_setting);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addUserFAB = (FloatingActionButton) findViewById(R.id.fab);
        groupNameTV = findViewById(R.id.groupName);
        groupDisplayPictureNIV = findViewById(R.id.groupDisplayPicture);
        userListView = findViewById(R.id.userList);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupDisplayPictureURL = getIntent().getStringExtra("groupDisplayPictureURL");
        usersList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        dbGetUsersList();

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameTV.setText(groupName);
        if(groupDisplayPictureURL != null){
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
            groupDisplayPictureNIV.setImageUrl(groupDisplayPictureURL, imageLoader);
        }

        addUserFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GroupInviteActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
            }
        });
    }

    void dbGetUsersList() {
        String url = baseUrl + "users/userList/" + groupId;
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String userId = jsonObject.getString("User_id");
                                String Name = jsonObject.getString("Name");
                                String Phone = jsonObject.getString("Phone");
                                String userDisplayPictureURL = null;
                                if (!jsonObject.getString("Image_url").equalsIgnoreCase("null")) {
                                    groupDisplayPictureURL = jsonObject.getString("Image_url");
                                }
                                usersList.add(
                                        new UserListDataModel(
                                                userId,
                                                Name,
                                                Phone,
                                                userDisplayPictureURL
                                        )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        UserListAdapter userListAdapter = new UserListAdapter(getApplicationContext(), R.layout.user_list_item, usersList);
                        userListView.setAdapter(userListAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        requestQueue.add(getRequest);
    }

    class UserListAdapter extends ArrayAdapter<UserListDataModel> {

        public UserListAdapter(Context context, int resource, ArrayList<UserListDataModel> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            Log.d(TAG, "accessed getView" + "position : " + position);
            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.user_list_item, null);
            }

            UserListDataModel userListDataModelItem = getItem(position);

            if (userListDataModelItem != null) {

                TextView userNameTV = view.findViewById(R.id.userName);
                TextView phoneNumberTV = view.findViewById(R.id.phone_number);
                NetworkImageView userImageNIV = view.findViewById(R.id.userImage);

                userNameTV.setText(userListDataModelItem.getUserName());
                phoneNumberTV.setText(userListDataModelItem.getPhoneNumber());

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
                userImageNIV.setImageUrl(userListDataModelItem.getUserDisplayPictureURL(), imageLoader);

            }
            return view;
        }
    }

    public class UserListDataModel {
        String userId;
        String userName;
        String phoneNumber;
        String userDisplayPictureURL;

        public UserListDataModel(String userId, String userName, String phoneNumber, String userDisplayPictureURL) {
            this.userId = userId;
            this.userName = userName;
            this.phoneNumber = phoneNumber;
            this.userDisplayPictureURL = userDisplayPictureURL;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getUserDisplayPictureURL() {
            return userDisplayPictureURL;
        }
    }

}
