package com.example.sage.mapsexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserCreateActivity extends AppCompatActivity {

    private static final String TAG = "UserCreateActivity";

    // Firebase objects
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference displayPictureReference;

    // Member variables
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String imageURL;

    // View objects
    private EditText nameET;
    private EditText emailET;
    private Button done;
    private Button chooseDisplayPicture;
    private ImageView displayPicture;

    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_create);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email);
        done = findViewById(R.id.done);
        chooseDisplayPicture = findViewById(R.id.chooseDisplayPicture);
        displayPicture = findViewById(R.id.displayPicture);

        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("user_display_picture");

    }

    @Override
    protected void onStart() {
        super.onStart();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = currentUser.getUid();
                name = nameET.getText().toString();
                email = emailET.getText().toString();
                phone = currentUser.getUid();
                dbCreateUser();
            }
        });

        chooseDisplayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = currentUser.getUid();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    public void dbCreateUser() {
        Map<String, String> user = new HashMap<>();
        user.put("Name", name);
        user.put("Phone", phone);
        user.put("Token",FirebaseInstanceId.getInstance().getToken());
        if (email != null && email.length() > 0)
            user.put("Email", email);
        if (imageURL != null && imageURL.length() > 0)
            user.put("ImageURL", imageURL);

        firestoreDB.collection("USERS").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(UserCreateActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        firestoreDB.collection("GENERAL").document("ALLUSERS").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> listOfUserInApp;
                        listOfUserInApp = (ArrayList<String>) documentSnapshot.get("PhoneNumbers");
                        listOfUserInApp.add(phone);
                        firestoreDB.collection("GENERAL").document("ALLUSERS")
                                .update("PhoneNumbers", listOfUserInApp)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.w(TAG, "wrote to db");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //      PHOTO METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByte = baos.toByteArray();
        //fireBase updating dp
        UploadTask uploadTask = displayPictureReference.child(userId + ".jpg").putBytes(imageByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "onSuccess: " + downloadUrl);
                imageURL = downloadUrl.toString();
                displayPicture.setImageBitmap(bitmap);
            }
        });

    }

}
