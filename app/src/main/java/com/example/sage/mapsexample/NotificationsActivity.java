package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";

    // Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;

    // View objects
    private LinearLayout notificationsLL;

    // Member variables
    private RequestQueue requestQueue;
    private ArrayList<FriendRequest> friendRequests;
    private ArrayList<TripInvite> tripInvites;
    ImageLoader imageLoader;


    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifications);
        requestQueue = Volley.newRequestQueue(this);

        notificationsLL = findViewById(R.id.notifications_linear_layout);

        friendRequests = new ArrayList<>();
        tripInvites = new ArrayList<>();

        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(10);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
        dbGetNotifications();
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    void dbGetNotifications() {

        firestoreDB.collection("USERS").document(currentUser.getUid())
                .collection("NOTIFICATIONS")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot notification : queryDocumentSnapshots){
                            NotificationType notificationType = NotificationType.valueOf(notification.getString("Type"));
                            switch (notificationType){
                                case FRIEND_REQUEST: friendRequests.add(new FriendRequest(
                                        notification.getId(),
                                        notification.getString("Sender_id"),
                                        notification.getString("Sender_name"),
                                        notification.getString("Sender_image_url")
                                ));
                                break;

                                case TRIP_INVITE: tripInvites.add(new TripInvite(
                                        notification.getId(),
                                        notification.getString("Sender_id"),
                                        notification.getString("Sender_name"),
                                        notification.getString("Trip_id"),
                                        notification.getString("Trip_name")
                                ));
                                break;

                                default: break;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });

    }

    void dbDeleteNotification(String notificationId) {

        firestoreDB.collection("USERS").document(currentUser.getUid())
                .collection("NOTIFICATIONS")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error deleting document", e);
            }
        });
    }


    void dbCreateFriendship(final String firstUserId, final String secondUserId){

        firestoreDB.collection("USERS").document(firstUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        ArrayList<String> firstUserFriends;
                        if (documentSnapshot.contains("Friends"))
                            firstUserFriends = (ArrayList<String>) documentSnapshot.get("Friends");
                        else
                            firstUserFriends = new ArrayList<>();
                        firstUserFriends.add(secondUserId);

                        firestoreDB.collection("USERS").document(firstUserId)
                                .update("Friends", firstUserFriends)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        firestoreDB.collection("USERS").document(secondUserId)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        ArrayList<String> secondUserFriends;
                                                        if (documentSnapshot.contains("Friends"))
                                                            secondUserFriends = (ArrayList<String>) documentSnapshot.get("Friends");
                                                        else
                                                            secondUserFriends = new ArrayList<>();
                                                        secondUserFriends.add(firstUserId);

                                                        firestoreDB.collection("USERS").document(secondUserId)
                                                                .update("Friends", secondUserFriends)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
                                                                        startActivity(intent);

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "onFailure: " + e);
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    void dbCreateTripMembership(final String tripId){
        firestoreDB.collection("TRIPS").document(tripId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        ArrayList<String> users = (ArrayList<String>) documentSnapshot.get("Users");
                        users.add(currentUser.getUid());

                        firestoreDB.collection("TRIPS").document(tripId)
                                .update("Users", users)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        firestoreDB.collection("USERS").document(currentUser.getUid())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        ArrayList<String> trips;
                                                        if (documentSnapshot.contains("Trips"))
                                                            trips = (ArrayList<String>) documentSnapshot.get("Trips");
                                                        else
                                                            trips = new ArrayList<>();
                                                        trips.add(tripId);

                                                        firestoreDB.collection("USERS").document(currentUser.getUid())
                                                                .update("Trips", trips)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Intent intent = new Intent(getApplicationContext(), TripActivity.class);
                                                                        intent.putExtra("tripId", tripId);
                                                                        startActivity(intent);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "onFailure: " + e);
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    enum NotificationType {
        FRIEND_REQUEST, TRIP_INVITE
    }

    public class FriendRequest {
        String notificationId;
        String senderId;
        String senderName;
        String senderImageURL;
        View notificationView;

        public FriendRequest(final String notificationId, final String senderId, String senderName, String senderImageURL) {
            this.notificationId = notificationId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderImageURL = senderImageURL;

            notificationView = getLayoutInflater().inflate(R.layout.notification_friend_request, notificationsLL, false);
            TextView senderNameTV = notificationView.findViewById(R.id.sender_name);
            Button acceptBTN = notificationView.findViewById(R.id.accept);
            Button ignoreBTN = notificationView.findViewById(R.id.ignore);
            NetworkImageView senderImageNIV = notificationView.findViewById(R.id.sender_image);

            senderNameTV.setText(senderName);
            senderImageNIV.setImageUrl(senderImageURL, imageLoader);

            acceptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbCreateFriendship(senderId, mAuth.getCurrentUser().getUid());
                    dbDeleteNotification(notificationId);
                }
            });

            ignoreBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbDeleteNotification(notificationId);
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            });
            notificationsLL.addView(notificationView);
        }
    }

    public class TripInvite {
        String notificationId;
        String senderId;
        String senderName;
        String tripId;
        String tripName;
        View notificationView;

        public TripInvite(final String notificationId, final String senderId, String senderName, final String tripId, String tripName) {
            this.notificationId = notificationId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.tripId = tripId;
            this.tripName = tripName;

            notificationView = getLayoutInflater().inflate(R.layout.notification_trip_invite, notificationsLL, false);
            TextView senderNameTV = notificationView.findViewById(R.id.sender_name);
            TextView tripNameTV = notificationView.findViewById(R.id.trip_name);
            Button acceptBTN = notificationView.findViewById(R.id.accept);
            Button ignoreBTN = notificationView.findViewById(R.id.ignore);

            senderNameTV.setText(senderName);
            tripNameTV.setText(tripName);

            acceptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbCreateTripMembership(tripId);
                    dbDeleteNotification(notificationId);
                }
            });

            ignoreBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbDeleteNotification(notificationId);
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
            });
            notificationsLL.addView(notificationView);

        }
    }

}