package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TripCreateActivity extends AppCompatActivity {

    private static final String TAG = "TripCreateActivity";
    SeekBar notificationRadiusSB;
    TextView notificationRadiusTV;
    EditText tripNameET;
    Button startTrip;

    //volley stuff
    public String baseUrl;
    public RequestQueue requestQueue;

    public String groupId;
    public String tripName;
    public int notificationRadius;
    public String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        groupId = getIntent().getStringExtra("groupId");
        notificationRadiusSB = findViewById(R.id.notification_radius_selector);
        notificationRadiusTV = findViewById(R.id.notification_radius_display);
        tripNameET = findViewById(R.id.trip_name);
        startTrip = findViewById(R.id.start_trip);

        baseUrl = getString(R.string.api_url);
        requestQueue = Volley.newRequestQueue(this);
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
