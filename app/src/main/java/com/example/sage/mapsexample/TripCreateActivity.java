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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripCreateActivity extends AppCompatActivity {

    private static final String TAG = "TripCreateActivity";
    SeekBar notificationRadiusSB;
    TextView notificationRadiusTV;
    EditText tripNameET;
    Button startTrip;
    ListView userListView;

    //volley stuff
    public String baseUrl;
    public RequestQueue requestQueue;

    public String groupId;
    public String tripName;
    public int notificationRadius;
    public String tripId;

    ArrayList<UserListDataModel> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        groupId = getIntent().getStringExtra("groupId");
        notificationRadiusSB = findViewById(R.id.notification_radius_selector);
        notificationRadiusTV = findViewById(R.id.notification_radius_display);
        tripNameET = findViewById(R.id.trip_name);
        startTrip = findViewById(R.id.start_trip);
        userListView = findViewById(R.id.user_list);

        usersList = new ArrayList<>();

        baseUrl = getString(R.string.api_url);
        requestQueue = Volley.newRequestQueue(this);

        dbGetUsersList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        notificationRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notificationRadiusTV.setText(String.valueOf(progress/10) + " Km");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripName = tripNameET.getText().toString();
                notificationRadius = notificationRadiusSB.getProgress()/10;
                dbCreateTrip();
            }
        });
    }

    public void dbCreateTrip(){
        String url = baseUrl+"trips";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            tripId = jsonObject.get("insertId").toString();
                            if(notificationRadius>0){
                                dbCreateNotificationRadius();
                                dbCreateTripMembers();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                intent.putExtra("tripId", tripId);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response - createuser", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("tripName",tripName);
                params.put("groupId",groupId);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    public void dbCreateNotificationRadius(){
        String url = baseUrl+"trips/notification-radius";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " +response);
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("tripId", tripId);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response - createuser", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("tripId",tripId);
                params.put("notificationRadius", String.valueOf(notificationRadius));
                return params;
            }
        };
        requestQueue.add(postRequest);
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
                                    userDisplayPictureURL = jsonObject.getString("Image_url");
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
                view = vi.inflate(R.layout.trip_create_user_list_item, null);
            }

            final UserListDataModel userListDataModelItem = getItem(position);

            if (userListDataModelItem != null) {

                TextView userNameTV = view.findViewById(R.id.userName);
                NetworkImageView userImageNIV = view.findViewById(R.id.userImage);
                CheckBox tripMemberCB = view.findViewById(R.id.tripMemberCheckBox);

                tripMemberCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        userListDataModelItem.selected = isChecked;
                    }
                });

                userNameTV.setText(userListDataModelItem.getUserName());

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
                Log.d(TAG, "getView: " + userListDataModelItem.getUserDisplayPictureURL());
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
        boolean selected = true;

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

        public boolean getSelected() {
            return selected;
        }
    }
}
