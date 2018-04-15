package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {
    private static final String TAG = "FriendsActivity";

    private FloatingActionButton addFriendButton;
    private ListView friendsListView;
    private RequestQueue requestQueue;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ArrayList<UserListDataModel> friendsList;
    private UserListAdapter friendsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        addFriendButton = findViewById(R.id.add_friends);
        friendsListView = findViewById(R.id.friends_list);

        friendsList = new ArrayList<>();
        friendsListAdapter = new UserListAdapter(getApplicationContext(), R.layout.friends_list_item, friendsList);
        friendsListView.setAdapter(friendsListAdapter);

        requestQueue = Volley.newRequestQueue(this);
        firestoreDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        dbGetFriendsList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendsActivity.class);
                startActivity(intent);
            }
        });
    }

    void dbGetFriendsList(){
        firestoreDB.collection("USERS").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> userIds = (ArrayList<String>) documentSnapshot.get("Friends");
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
                                            friendsList.add(
                                                    new UserListDataModel(
                                                            userId,
                                                            Name,
                                                            Phone,
                                                            userDisplayPictureURL
                                                    )
                                            );
                                            friendsListAdapter.notifyDataSetChanged();
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
            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.friends_list_item, null);
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
