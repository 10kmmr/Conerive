package com.example.sage.mapsexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    ListView notificationsListView;
    ArrayList<NotificationsListDataModel> notificationsList;

    public RequestQueue requestQueue;
    public String baseUrl;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        requestQueue = Volley.newRequestQueue(this);
        baseUrl = getString(R.string.api_url);
        notificationsListView = findViewById(R.id.notifications_list);
        mAuth = FirebaseAuth.getInstance();
        notificationsList = new ArrayList<>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        dbGetNotifications();
    }


    class NotificationsListAdapter extends ArrayAdapter<NotificationsListDataModel> {

        public NotificationsListAdapter(Context context, int resource, ArrayList<NotificationsListDataModel> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            Log.d(TAG, "accessed getView" + "position : " + position);
            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.notification_list_item, null);
            }

            final NotificationsListDataModel notificationsListDataModelItem = getItem(position);

            if (notificationsListDataModelItem != null) {

                TextView groupNameTV = view.findViewById(R.id.group_name);
                TextView senderNameTV = view.findViewById(R.id.sender_name);
                NetworkImageView groupImageNIV = view.findViewById(R.id.group_image);
                Button acceptBTN = view.findViewById(R.id.accept);
                Button rejectBTN = view.findViewById(R.id.reject);

                groupNameTV.setText(notificationsListDataModelItem.getGroupName());
                senderNameTV.setText(notificationsListDataModelItem.getSenderName());

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
                groupImageNIV.setImageUrl(notificationsListDataModelItem.getGroupDisplayPictureURL(), imageLoader);

                acceptBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbCreateGroupMember(notificationsListDataModelItem.getGroupId(), mAuth.getCurrentUser().getUid());
                    }
                });

                rejectBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            return view;
        }
    }

    void dbGetNotifications() {
        String url = baseUrl + "group-members/notifications/" + mAuth.getUid();
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String groupId = jsonObject.getString("Group_id");
                                String groupName = jsonObject.getString("Group_name");
                                String groupDisplayPictureURL = null;
                                if (!jsonObject.getString("Image_url").equalsIgnoreCase("null")) {
                                    groupDisplayPictureURL = jsonObject.getString("Image_url");
                                }
                                String notificationId = jsonObject.getString("Notification_id");
                                String senderId = jsonObject.getString("Sender_id");
                                String senderName = jsonObject.getString("Sender_name");

                                notificationsList.add(
                                        new NotificationsListDataModel(
                                                notificationId,
                                                groupId,
                                                groupName,
                                                senderId,
                                                senderName,
                                                groupDisplayPictureURL
                                        )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        NotificationsListAdapter notificationsListAdapter = new NotificationsListAdapter(getApplicationContext(), R.layout.notification_list_item, notificationsList);
                        notificationsListView.setAdapter(notificationsListAdapter);
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

    void dbDeleteNotification(String notificationId){
        String url = baseUrl + "group-members/notifications/" + notificationId;
        JsonArrayRequest deleteRequest = new JsonArrayRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        requestQueue.add(deleteRequest);
    }

    public void dbCreateGroupMember(final String groupId, final String userId){
        String url = baseUrl+"group-members";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse - group-members: " + jsonObject);
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
                params.put("userId", userId);
                params.put("groupId", groupId);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    public class NotificationsListDataModel{
        String notificationId;
        String groupId;
        String groupName;
        String senderId;
        String senderName;
        String groupDisplayPictureURL;

        public NotificationsListDataModel(String notificationId, String groupId, String groupName, String senderId, String senderName, String groupDisplayPictureURL) {
            this.notificationId = notificationId;
            this.groupId = groupId;
            this.groupName = groupName;
            this.senderId = senderId;
            this.senderName = senderName;
            this.groupDisplayPictureURL = groupDisplayPictureURL;
        }

        public String getNotificationId() {
            return notificationId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getSenderId() {
            return senderId;
        }

        public String getSenderName() {
            return senderName;
        }

        public String getGroupDisplayPictureURL() {
            return groupDisplayPictureURL;
        }
    }
}
