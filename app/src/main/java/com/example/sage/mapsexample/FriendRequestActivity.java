package com.example.sage.mapsexample;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

public class FriendRequestActivity extends AppCompatActivity {
    private static final String TAG = "FriendRequestActivity";

    public Button sendRequestBTN;
    public EditText phoneNumberET;
    private ProgressBar progressBar;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String name;
    private String imageURL;
    private String recieverPhone;

    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        firestoreDB = FirebaseFirestore.getInstance();
        phoneNumberET = findViewById(R.id.contact_number);
        sendRequestBTN = findViewById(R.id.send_request);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        requestQueue = Volley.newRequestQueue(this);

        sendRequestBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoading();
                recieverPhone = phoneNumberET.getText().toString();

                firestoreDB.collection("USERS").whereEqualTo("Phone", recieverPhone)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(queryDocumentSnapshots.size()>0) {
                                    final String recieverId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    final String recieverName = queryDocumentSnapshots.getDocuments().get(0).getString("Name");
                                    final String recieverToken = queryDocumentSnapshots.getDocuments().get(0).getString("Token");

                                    firestoreDB.collection("USERS").document(currentUser.getUid())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    name = documentSnapshot.getString("Name");
                                                    imageURL = documentSnapshot.getString("ImageURL");

                                                    if (documentSnapshot.contains("Friends")) {
                                                        ArrayList friendIds = (ArrayList<String>) documentSnapshot.get("Friends");
                                                        if (friendIds.contains(recieverId)) {
                                                            stopLoading();
                                                            Toast.makeText(FriendRequestActivity.this, recieverName + " is already your friend", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            dbSendFriendRequest(recieverId, recieverName);
                                                            ServerSendFriendRequest(name, imageURL, recieverToken);
                                                        }
                                                    } else {
                                                        dbSendFriendRequest(recieverId, recieverName);
                                                        ServerSendFriendRequest(name, imageURL, recieverToken);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    stopLoading();
                                                    Log.d(TAG, "onFailure - 2: " + e);
                                                }
                                            });
                                } else {
                                    stopLoading();
                                    Toast.makeText(FriendRequestActivity.this, "This user does not have the application! ", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopLoading();
                        Log.d(TAG, "onFailure - 1: " + e);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
        startActivity(intent);
        finish();
    }

    void startLoading(){
        sendRequestBTN.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    void stopLoading(){
        sendRequestBTN.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    void dbSendFriendRequest(String recieverId, final String recieverName){
        Map<String, String> friendRequest= new HashMap<>();
        friendRequest.put("Sender_id", currentUser.getUid());
        friendRequest.put("Sender_name", name);
        friendRequest.put("Sender_image_url", imageURL);
        friendRequest.put("Type", "FRIEND_REQUEST");

        firestoreDB.collection("USERS").document(recieverId)
                .collection("NOTIFICATIONS")
                .add(friendRequest)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        stopLoading();
                        Toast.makeText(FriendRequestActivity.this, "Friend request sent to " + recieverName, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopLoading();
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    void ServerSendFriendRequest(final String senderName, final String senderImage, final String recieverToken){
        String url = "https://conerive-fcm.herokuapp.com/sendFriendRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("SenderName", senderName);
                params.put("SenderImage", senderImage);
                params.put("Token", recieverToken);

                return params;
            }
        };
        requestQueue.add(postRequest);
    }

}
