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
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserCreateActivity extends AppCompatActivity {
    private static final String TAG = "UserCreateActivity";
    private static final int CAMERA_RETURN = 1;
    private static final int GALLERY_RETURN = 2;

    // Firebase objects
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;

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
    private CircleImageView displayPicture;


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
        displayPicture = findViewById(R.id.display_picture);

        firebaseStorage = FirebaseStorage.getInstance();

        done.setOnClickListener(new CreateUserOnclickListener());

        chooseDisplayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = currentUser.getUid();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_RETURN);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + "Dont do anything");
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    public void dbCreateUser() {

    }

    //----------------------------------------------------------------------------------------------
    //      PHOTO METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == CAMERA_RETURN) {
            super.onActivityResult(requestCode, resultCode, data);
            final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageByte = baos.toByteArray();
            displayPicture.setImageBitmap(bitmap);

            //fireBase updating dp
            StorageReference storageReference = firebaseStorage.getReference()
                    .child("user_display_picture")
                    .child(userId + ".png");
            storageReference.putBytes(imageByte)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageURL = uri.toString();

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserCreateActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class CreateUserOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            userId = currentUser.getUid();
            name = nameET.getText().toString();
            email = emailET.getText().toString();
            phone = currentUser.getUid();

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

            // TODO - Handle general in transaction
//        firestoreDB.collection("GENERAL").document("ALLUSERS").get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        ArrayList<String> listOfUserInApp;
//                        if(documentSnapshot.contains("Phone"))
//                            listOfUserInApp = (ArrayList<String>) documentSnapshot.get("Phone");
//                        else
//                            listOfUserInApp = new ArrayList<>();
//                        listOfUserInApp.add(phone);
//                        firestoreDB.collection("GENERAL").document("ALLUSERS")
//                                .update("Phone", listOfUserInApp)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Log.w(TAG, "wrote to db");
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.w(TAG, "Error writing document", e);
//                                    }
//                                });
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(TAG, "onFailure: " + e);
//            }
//        });
        }
    }

}
