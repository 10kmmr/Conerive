package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripCreateActivity extends AppCompatActivity {
    private static final String TAG = "TripCreateActivity";

    // View objects
    private SeekBar notificationRadiusSB;
    private TextView notificationRadiusTV;
    private EditText tripNameET;
    private Button startTrip;
    private ListView userListView;

    // Volley stuff
    private RequestQueue requestQueue;

    // Member variables
    private String groupId;
    private String tripName;
    private int notificationRadius;
    private String tripId;
    private ArrayList<UserListDataModel> usersList;
    private UserListAdapter userListAdapter;

    // Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;

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
        userListAdapter = new UserListAdapter(getApplicationContext(), R.layout.user_list_item, usersList);
        userListView.setAdapter(userListAdapter);

        requestQueue = Volley.newRequestQueue(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();

        dbGetUsersList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        notificationRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notificationRadiusTV.setText(String.valueOf(progress / 10) + " Km");
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
                notificationRadius = notificationRadiusSB.getProgress() / 10;
                dbCreateTrip();
            }
        });
    }

    public void dbCreateTrip() {

        final Map<String, Object> trip = new HashMap<>();
        trip.put("Trip_name", tripName);
        if(notificationRadius>0)
            trip.put("Notification_radius", notificationRadius);

        ArrayList<String> tripMembers = new ArrayList<>();
        for(UserListDataModel user : usersList){
            if(user.selected)
                tripMembers.add(user.userId);
        }
        trip.put("Trip_members", tripMembers);

        firestoreDB.collection("GROUPS").document(groupId)
                .collection("TRIPS")
                .add(trip)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("tripId", tripId);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });
    }

    void dbGetUsersList() {

        firestoreDB.collection("GROUPS").document(groupId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> userIds = (ArrayList<String>) documentSnapshot.get("Users");
                        for (String userId : userIds) {

                            firestoreDB.collection("USERS").document(userId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            String userId = documentSnapshot.getId();
                                            String Name = documentSnapshot.getString("Name");
                                            String Phone = documentSnapshot.getString("Phone");
                                            String userDisplayPictureURL = null;
                                            if (documentSnapshot.contains("ImageURL")) {
                                                userDisplayPictureURL = documentSnapshot.getString("ImageURL");
                                            }
                                            usersList.add(
                                                    new UserListDataModel(
                                                            userId,
                                                            Name,
                                                            Phone,
                                                            userDisplayPictureURL
                                                    )
                                            );
                                            userListAdapter.notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e);
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });
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
                        Log.d(TAG, "onCheckedChanged: " + userListDataModelItem.userName + " : " + userListDataModelItem.selected);
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
