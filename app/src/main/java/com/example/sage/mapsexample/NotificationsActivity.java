package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

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


    void dbCreateFriendship(final String firstUserId, final String secondUserId, final FriendRequest friendRequest){

        firestoreDB.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference firstUserReference =  firestoreDB.collection("USERS").document(firstUserId);
                DocumentReference secondUserReference =  firestoreDB.collection("USERS").document(secondUserId);

                DocumentSnapshot firstUserSnapshot = transaction.get(firstUserReference);
                DocumentSnapshot secondUserSnapshot = transaction.get(secondUserReference);

                ArrayList<String> firstUserFriends;
                if (firstUserSnapshot.contains("Friends"))
                    firstUserFriends = (ArrayList<String>) firstUserSnapshot.get("Friends");
                else
                    firstUserFriends = new ArrayList<>();
                firstUserFriends.add(secondUserId);

                ArrayList<String> secondUserFriends;
                if (secondUserSnapshot.contains("Friends"))
                    secondUserFriends = (ArrayList<String>) secondUserSnapshot.get("Friends");
                else
                    secondUserFriends = new ArrayList<>();
                secondUserFriends.add(firstUserId);

                transaction.update(firstUserReference, "Friends", firstUserFriends);
                transaction.update(secondUserReference, "Friends", secondUserFriends);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                friendRequest.stopLoading(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                friendRequest.stopLoading(false);
            }
        });


    }

    void dbCreateTripMembership(final String tripId, final TripInvite tripInvite){

        firestoreDB.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference tripReference = firestoreDB.collection("TRIPS").document(tripId);
                DocumentReference userReference = firestoreDB.collection("USERS").document(currentUser.getUid());

                DocumentSnapshot tripSnapshot = transaction.get(tripReference);
                DocumentSnapshot userSnapshot = transaction.get(userReference);

                ArrayList<String> users = (ArrayList<String>) tripSnapshot.get("Users");
                users.add(currentUser.getUid());

                ArrayList<String> trips;
                if (userSnapshot.contains("Trips"))
                    trips = (ArrayList<String>) userSnapshot.get("Trips");
                else
                    trips = new ArrayList<>();
                trips.add(tripId);

                HashMap<String, Object> tripMap = new HashMap<>();
                tripMap.put("Users", users);
                tripMap.put("Event_one", "USER_ADDED");
                tripMap.put("Stack", mAuth.getCurrentUser().getUid());

                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("Trips", trips);
                userMap.put("Event_one", "TRIP_ADDED");
                userMap.put("Stack", tripId);

                transaction.update(tripReference,"Users", users);
                transaction.update(userReference,"Trips", trips);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                tripInvite.stopLoading(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tripInvite.stopLoading(false);
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

        LinearLayout idleLL;
        ProgressBar progressBar;
        TextView successTV;


        public FriendRequest(final String notificationId, final String senderId, String senderName, String senderImageURL) {
            this.notificationId = notificationId;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderImageURL = senderImageURL;

            notificationView = getLayoutInflater().inflate(R.layout.notification_friend_request, notificationsLL, false);
            TextView senderNameTV = notificationView.findViewById(R.id.sender_name);
            Button acceptBTN = notificationView.findViewById(R.id.accept);
            Button ignoreBTN = notificationView.findViewById(R.id.ignore);
            idleLL = notificationView.findViewById(R.id.idle);
            progressBar = notificationView.findViewById(R.id.progress_bar);
            successTV = notificationView.findViewById(R.id.accept_success);
            NetworkImageView senderImageNIV = notificationView.findViewById(R.id.sender_image);

            senderNameTV.setText(senderName);
            senderImageNIV.setImageUrl(senderImageURL, imageLoader);

            acceptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoading();
                    dbCreateFriendship(senderId, mAuth.getCurrentUser().getUid(), FriendRequest.this);
                    dbDeleteNotification(notificationId);
                }
            });

            ignoreBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotification();
                    dbDeleteNotification(notificationId);
                    Toast.makeText(NotificationsActivity.this, "request ignored", Toast.LENGTH_SHORT).show();
                }
            });
            notificationsLL.addView(notificationView);
        }

        void startLoading(){
            idleLL.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        void stopLoading(boolean success){
            progressBar.setVisibility(View.INVISIBLE);
            if(success){
                successTV.setVisibility(View.VISIBLE);
            } else {
                idleLL.setVisibility(View.VISIBLE);
                Toast.makeText(NotificationsActivity.this, "failed to send friend request", Toast.LENGTH_SHORT).show();
            }
        }

        void deleteNotification(){
            notificationsLL.removeView(notificationView);
        }
    }

    public class TripInvite {
        String notificationId;
        String senderId;
        String senderName;
        String tripId;
        String tripName;
        View notificationView;

        LinearLayout idleLL;
        ProgressBar progressBar;

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
            idleLL = notificationView.findViewById(R.id.idle);
            progressBar = notificationView.findViewById(R.id.progress_bar);

            senderNameTV.setText(senderName);
            tripNameTV.setText(tripName);

            acceptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoading();
                    dbCreateTripMembership(tripId, TripInvite.this);
                    dbDeleteNotification(notificationId);
                }
            });

            ignoreBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotification();
                    dbDeleteNotification(notificationId);
                    Toast.makeText(NotificationsActivity.this, "Trip invite ignored", Toast.LENGTH_SHORT).show();
                }
            });
            notificationsLL.addView(notificationView);

        }

        void startLoading(){
            idleLL.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        void stopLoading(boolean success){
            progressBar.setVisibility(View.INVISIBLE);
            if(success){
                Toast.makeText(NotificationsActivity.this, "You have joined the trip", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), TripActivity.class);
                intent.putExtra("tripId", tripId);
                startActivity(intent);
            } else {
                idleLL.setVisibility(View.VISIBLE);
                Toast.makeText(NotificationsActivity.this, "failed to join trip", Toast.LENGTH_SHORT).show();
            }
        }

        void deleteNotification(){
            notificationsLL.removeView(notificationView);
        }
    }

}