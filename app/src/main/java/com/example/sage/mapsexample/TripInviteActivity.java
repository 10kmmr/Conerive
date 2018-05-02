package com.example.sage.mapsexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TripInviteActivity extends AppCompatActivity {

    private static final String TAG = "TripInviteActivity";

    public Button sendInviteBTN;
    public EditText phoneNumberET;
    private LinearLayout friendsLL;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String tripId;
    private String tripName;
    private ArrayList<String> tripMembers;

    private String name;
    private String imageURL;
    private ArrayList<String> friends;

    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_invite);

        tripId = getIntent().getStringExtra("tripId");
        friends = new ArrayList<>();

        firestoreDB = FirebaseFirestore.getInstance();
        phoneNumberET = findViewById(R.id.phone_number);
        sendInviteBTN = findViewById(R.id.send_invite);
        friendsLL = findViewById(R.id.friends_list);
        sendInviteBTN.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        requestQueue = Volley.newRequestQueue(this);

        dbGetUserDocument();


    }

    @Override
    protected void onStart() {
        super.onStart();

        sendInviteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String receiverPhone = phoneNumberET.getText().toString();
                dbGetReceiverDocument(receiverPhone);
            }
        });

    }

    void dbGetUserDocument(){
        firestoreDB.collection("USERS").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        name = documentSnapshot.getString("Name");
                        imageURL = documentSnapshot.getString("ImageURL");
                        if(documentSnapshot.contains("Friends"))
                            friends = (ArrayList<String>) documentSnapshot.get("Friends");
                        dbGetTripDocument();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure - 2: " + e);
                    }
                });
    }

    void dbGetTripDocument(){

        firestoreDB.collection("TRIPS").document(tripId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        tripName = documentSnapshot.getString("Name");
                        tripMembers = (ArrayList<String>) documentSnapshot.get("Users");
                        sendInviteBTN.setVisibility(View.VISIBLE);
                        dbGetFriends();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    void dbGetFriends(){
        for(String friendId : friends){
            if(!tripMembers.contains(friendId)){
                firestoreDB.collection("USERS")
                        .document(friendId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                new Friend(
                                        documentSnapshot.getId(),
                                        documentSnapshot.getString("Name"),
                                        documentSnapshot.getString("Phone"),
                                        documentSnapshot.getString("ImageURL")
                                );
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }
    }

    void dbGetReceiverDocument(String receiverPhone){
        firestoreDB.collection("USERS").whereEqualTo("Phone", receiverPhone)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            String receiverId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            String receiverName = queryDocumentSnapshots.getDocuments().get(0).getString("Name");
                            if(tripMembers.contains(receiverId)){
                                Toast.makeText(TripInviteActivity.this, receiverName + "is already part of this trip!", Toast.LENGTH_SHORT).show();
                            } else {
                                dbSendTripInvite(receiverId);
                            }
                        } else {
                            Toast.makeText(TripInviteActivity.this, "This user does not have the application! ", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure - 1: " + e);
            }
        });
    }

    void dbSendTripInvite(String receiverId){
        Map<String, String> tripInvite= new HashMap<>();
        tripInvite.put("Sender_id", currentUser.getUid());
        tripInvite.put("Sender_name", name);
        tripInvite.put("Trip_id", tripId);
        tripInvite.put("Trip_name", tripName);
        tripInvite.put("Type", "TRIP_INVITE");

        firestoreDB.collection("USERS").document(receiverId)
                .collection("NOTIFICATIONS")
                .add(tripInvite)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
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

    public class Friend{
        String friendId;
        String friendName;
        String friendPhone;
        String friendImageURL;
        View view;

        public Friend(final String friendId, String friendName, String friendPhone, String friendImageURL) {
            this.friendId = friendId;
            this.friendName = friendName;
            this.friendPhone = friendPhone;
            this.friendImageURL = friendImageURL;

            view = getLayoutInflater().inflate(R.layout.friend_trip_invite, friendsLL, false);
            TextView nameTV = view.findViewById(R.id.name);
            ImageView imageIV = view.findViewById(R.id.image);
            Button invite = view.findViewById(R.id.invite);

            nameTV.setText(friendName);
            if(friendImageURL!=null)
                Picasso.get().load(friendImageURL).into(imageIV);

            invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbSendTripInvite(friendId);
                }
            });

            friendsLL.addView(view);
        }
    }
}
