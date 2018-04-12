package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class GroupInviteActivity extends AppCompatActivity {
    private static final String TAG = "GroupInviteActivity";

    // Member variables
    private String receiverPhoneNumber;
    private String groupId;

    // View objects
    private Button inviteButton;
    private EditText receiverPhoneNumberET;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;

    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invite);

        groupId = getIntent().getStringExtra("groupId");

        inviteButton = findViewById(R.id.invite);
        receiverPhoneNumberET = findViewById(R.id.phone_number);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiverPhoneNumber = receiverPhoneNumberET.getText().toString();
                Log.d(TAG, "onClick: clicked");
                inviteButton.setOnClickListener(null);
                dbCreateNotification();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    void dbCreateNotification() {

        final Map<String, String> notification = new HashMap<>();
        notification.put("Sender_id", currentUser.getUid());
        notification.put("Group_id", groupId);
        notification.put("Type", "GROUP_INVITE");

        firestoreDB.collection("USERS")
                .whereEqualTo("Phone", receiverPhoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                firestoreDB.collection("USERS")
                                        .document(document.getId())
                                        .collection("NOTIFICATIONS")
                                        .add(notification)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Intent intent = new Intent(GroupInviteActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}
