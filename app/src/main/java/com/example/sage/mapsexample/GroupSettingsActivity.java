package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupSettingsActivity extends AppCompatActivity {
    private static final String TAG = "GroupSettingsActivity";

    // Firebase objects
    private FirebaseFirestore firestoreDB;

    // Member variables
    private String groupId;
    private String groupName;
    private String groupDisplayPictureURL;
    private ArrayList<UserListDataModel> usersList;
    private UserListAdapter userListAdapter;

    // View objects
    private Toolbar toolbar;
    private FloatingActionButton addUserFAB;
    private TextView groupNameTV;
    private NetworkImageView groupDisplayPictureNIV;
    private ListView userListView;

    // Volley objects
    private RequestQueue requestQueue;

    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_setting);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addUserFAB = findViewById(R.id.fab);
        groupNameTV = findViewById(R.id.groupName);
        groupDisplayPictureNIV = findViewById(R.id.groupDisplayPicture);
        userListView = findViewById(R.id.userList);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupDisplayPictureURL = getIntent().getStringExtra("groupDisplayPictureURL");
        usersList = new ArrayList<>();
        userListAdapter = new UserListAdapter(getApplicationContext(), R.layout.user_list_item, usersList);
        userListView.setAdapter(userListAdapter);

        requestQueue = Volley.newRequestQueue(this);
        firestoreDB = FirebaseFirestore.getInstance();

        dbGetUsersList();

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameTV.setText(groupName);
        if (groupDisplayPictureURL != null) {
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
                startActivity(intent);
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------
    //      USERS LIST VIEW OBJECTS AND METHODS
    //----------------------------------------------------------------------------------------------

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
