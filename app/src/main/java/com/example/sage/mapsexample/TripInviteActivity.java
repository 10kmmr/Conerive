package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TripInviteActivity extends AppCompatActivity {

    private static final String TAG = "TripInviteActivity";

    public Button sendInviteBTN;
    public EditText phoneNumberET;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String tripId;
    private String tripName;

    private String name;
    private String imageURL;
    private String recieverId;
    private String recieverPhone;
    private String recieverName;

    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_invite);

        tripId = getIntent().getStringExtra("tripId");

        firestoreDB = FirebaseFirestore.getInstance();
        phoneNumberET = findViewById(R.id.phone_number);
        sendInviteBTN = findViewById(R.id.send_invite);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        requestQueue = Volley.newRequestQueue(this);

        sendInviteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recieverPhone = phoneNumberET.getText().toString();

                firestoreDB.collection("USERS").whereEqualTo("Phone", recieverPhone)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.size() > 0) {
                                    recieverId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    recieverName = queryDocumentSnapshots.getDocuments().get(0).getString("Name");

                                    firestoreDB.collection("USERS").document(currentUser.getUid())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    name = documentSnapshot.getString("Name");
                                                    imageURL = documentSnapshot.getString("ImageURL");

                                                    firestoreDB.collection("TRIPS").document(tripId)
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    tripName = documentSnapshot.getString("Name");
                                                                    ArrayList userIds = (ArrayList<String>) documentSnapshot.get("Users");
                                                                    if (userIds.contains(recieverId)) {
                                                                        Toast.makeText(TripInviteActivity.this, recieverName + " is already part of the trip", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        dbSendTripInvite();
                                                                        //ServerSendTripInvite();
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
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure - 2: " + e);
                                                }
                                            });
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
        });
    }

    void dbSendTripInvite(){
        Map<String, String> tripInvite= new HashMap<>();
        tripInvite.put("Sender_id", currentUser.getUid());
        tripInvite.put("Sender_name", name);
        tripInvite.put("Trip_id", tripId);
        tripInvite.put("Trip_name", tripName);
        tripInvite.put("Type", "TRIP_INVITE");

        firestoreDB.collection("USERS").document(recieverId)
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
}
