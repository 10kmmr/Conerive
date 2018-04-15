package com.example.sage.mapsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class AddFriendsActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendsActivity";

    //View Variable
    public Button SendReqBT;
    public EditText phoneNumberET;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String recieverPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        firestoreDB = FirebaseFirestore.getInstance();
        phoneNumberET = findViewById(R.id.phone_number);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        SendReqBT = findViewById(R.id.send_request);
        SendReqBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recieverPhone = phoneNumberET.getText().toString();

                firestoreDB.collection("USERS").whereEqualTo("Phone", recieverPhone).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                               String recieverId = queryDocumentSnapshots.getDocuments().get(0).getId();

                               Map<String, Object> notification = new HashMap<>();
                               notification.put("Sender_id", currentUser.getUid());
                               notification.put("Type", "FRIEND_REQUEST");

                               firestoreDB.collection("USERS").document(recieverId)
                                       .collection("NOTIFICATIONS")
                                       .add(notification)
                                       .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                           @Override
                                           public void onSuccess(DocumentReference documentReference) {
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
                                Log.d(TAG, "onFailure: " + e);
                            }
                        });

            }
        });
    }

}
