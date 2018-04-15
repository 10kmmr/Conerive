package com.example.sage.mapsexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class UserSettingsActivity extends AppCompatActivity {
    private static final String TAG = "UserSettingsActivity";

    // Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference displayPictureReference;
    private FirebaseFirestore firestoreDB;

    // Member variables
    private String name;
    private String email;
    private String phone;

    // View objects
    private TextView nameTV;
    private TextView emailTV;
    private TextView phoneTV;
    private ImageView displayPicture;

    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("user_display_picture");

        nameTV = findViewById(R.id.name);
        emailTV = findViewById(R.id.email);
        phoneTV = findViewById(R.id.phone);
        displayPicture = findViewById(R.id.displayPicture);

        getUserDetails();
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    void getUserDetails() {

        firestoreDB.collection("USERS").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                name = document.getString("Name");
                                nameTV.setText(name);
                                phone = document.getString("Phone");
                                phoneTV.setText(phone);
                                if (document.contains("Email")) {
                                    email = document.getString("Email");
                                    emailTV.setText(email);
                                } else {
                                    emailTV.setText("----------");

                                }
                                if (document.contains("ImageURL")) {
                                    downloadDisplayPicture();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    //----------------------------------------------------------------------------------------------
    //      PHOTO METHODS
    //----------------------------------------------------------------------------------------------

    void downloadDisplayPicture() {

        StorageReference ref = displayPictureReference.child(currentUser.getUid() + ".jpg");
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: " + "some stuff");
                    Bitmap image;
                    image = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    displayPicture.setImageBitmap(image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
