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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO - restructure push notifications

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    // View objects
    private ListView groupsListView;
    private Button createGroupButton;
    private Button notificationsButton;
    private Button userProfileButton;

    // Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;

    // Member variables
    private ArrayList<GroupListDataModel> groupsList;

    // Volley objects
    public RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        groupsListView = findViewById(R.id.groupList);
        createGroupButton = findViewById(R.id.createGroup);
        notificationsButton = findViewById(R.id.notifications);
        userProfileButton = findViewById(R.id.user_profile);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
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

        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
                startActivity(intent);
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), GroupHomeActivity.class);
                intent.putExtra("groupId", groupsList.get(position).groupId);
                intent.putExtra("groupName", groupsList.get(position).groupName);
                intent.putExtra("groupDisplayPictureURL", groupsList.get(position).groupDisplayPictureURL);
                intent.putExtra("lastTripDate", groupsList.get(position).lastTripDate);
                startActivity(intent);
            }
        });


    }


    void dbGetGroupList() {

        firestoreDB.collection("USERS").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    ArrayList<String> groupIds = (ArrayList<String>)documentSnapshot.get("Groups");
                    for(String groupId : groupIds){

                        firestoreDB.collection("GROUPS").document(groupId)
                                .get()

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });

//        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        for (int i = 0; i < response.length(); i++) {
//                            try {
//                                JSONObject jsonObject = response.getJSONObject(i);
//                                String groupId = jsonObject.getString("Group_id");
//                                String groupName = jsonObject.getString("Group_name");
//                                String groupDisplayPictureURL = null;
//                                if (!jsonObject.getString("Group_Display_picture").equalsIgnoreCase("null")) {
//                                    groupDisplayPictureURL = jsonObject.getString("Group_Display_picture");
//                                }
//                                int memberCount = jsonObject.getInt("Member_count");
//                                int tripCount = jsonObject.getInt("Trip_count");
//                                int imageCount = jsonObject.getInt("Image_count");
//                                groupsList.add(
//                                        new GroupListDataModel(
//                                                groupId,
//                                                groupName,
//                                                groupDisplayPictureURL,
//                                                "69/69/69",                 //get actual trip date after db query fix
//                                                memberCount,
//                                                tripCount,
//                                                imageCount
//                                        )
//                                );
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        GroupListAdapter groupListAdapter = new GroupListAdapter(getApplicationContext(), R.layout.group_list_item, groupsList);
//                        groupsListView.setAdapter(groupListAdapter);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("Error.Response", error.toString());
//                    }
//                }
//        );
//        requestQueue.add(getRequest);
    }

    class GroupListAdapter extends ArrayAdapter<GroupListDataModel> {

        public GroupListAdapter(Context context, int resource, ArrayList<GroupListDataModel> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            Log.d(TAG, "accessed getView" + "position : " + position);
            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.group_list_item, null);
            }

            GroupListDataModel groupListDataModelItem = getItem(position);

            if (groupListDataModelItem != null) {

                TextView groupNameTV = view.findViewById(R.id.groupName);
                TextView memberCountTV = view.findViewById(R.id.member_count);
                TextView tripCountTV = view.findViewById(R.id.tripCount);
                TextView imageCountTV = view.findViewById(R.id.imageCount);
                TextView lastTripDateTV = view.findViewById(R.id.lastTripDate);
                NetworkImageView groupImageNIV = view.findViewById(R.id.groupImage);

                groupNameTV.setText(groupListDataModelItem.getGroupName());
                memberCountTV.setText("member count : " + Integer.toString(groupListDataModelItem.getMemberCount()));
                tripCountTV.setText("trip count : " + Integer.toString(groupListDataModelItem.getTripCount()));
                imageCountTV.setText("image count : " + Integer.toString(groupListDataModelItem.getImageCount()));
                lastTripDateTV.setText(groupListDataModelItem.getLastTripDate());


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

            }
            return view;
        }
    }

    public class GroupListDataModel {
        String groupId;
        String groupName;
        String groupDisplayPictureURL;
        String lastTripDate;
        int memberCount;
        int tripCount;
        int imageCount;

        public GroupListDataModel(String groupId, String groupName, String groupDisplayPictureURL, String lastTripDate, int memberCount, int tripCount, int imageCount) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.groupDisplayPictureURL = groupDisplayPictureURL;
            this.lastTripDate = lastTripDate;
            this.memberCount = memberCount;
            this.tripCount = tripCount;
            this.imageCount = imageCount;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getGroupDisplayPictureURL() {
            return groupDisplayPictureURL;
        }

        public String getLastTripDate() {
            return lastTripDate;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public int getTripCount() {
            return tripCount;
        }

        public int getImageCount() {
            return imageCount;
        }
    }
}
