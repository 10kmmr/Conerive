package com.example.sage.mapsexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sage.mapsexample.HomeActivity;
import com.example.sage.mapsexample.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class GroupCreateActivity extends AppCompatActivity {
    private static final String TAG = "GroupCreateActivity";

    // Firebase objects
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference displayPictureReference;

    // Member variables
    private String groupId;
    private String GroupDesc;
    private String GroupName;
    private String displayPictureURL;
    private byte[] imageByte;

    // View objects
    private Button GroupButton;
    private Button OpenGalley;
    private EditText GroupNameET;
    private EditText GroupDescET;
    private ImageView GroupDp;

    //----------------------------------------------------------------------------------------------
    //      ACTIVITY LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        GroupButton = findViewById(R.id.newGroup);
        OpenGalley = findViewById(R.id.OpenGalley);
        GroupNameET = findViewById(R.id.GName);
        GroupDescET = findViewById(R.id.GroupDescET);
        GroupDp = findViewById(R.id.GroupDp);

        displayPictureURL = "";

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("Group_display_picture");
    }

    @Override
    protected void onStart() {
        super.onStart();

        OpenGalley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

        GroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupName = GroupNameET.getText().toString();
                GroupDesc = GroupDescET.getText().toString();
                dbCreategroup();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //      MEMBER METHODS
    //----------------------------------------------------------------------------------------------

    public void dbCreategroup() {

        Map<String, String> group = new HashMap<>();
        group.put("groupName", GroupName);
        group.put("groupDescription", GroupDesc);
        group.put("adminId", currentUser.getUid());

        firestoreDB.collection("GROUPS")
                .add(group)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        groupId = documentReference.getId();
                        if (imageByte != null) {
                            dbCreateDisplayPicture();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void dbCreateDisplayPicture() {
        UploadTask uploadTask = displayPictureReference.child(groupId + ".jpg").putBytes(imageByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(TAG, "Error adding image", exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                displayPictureURL = downloadUrl.toString();

                firestoreDB.collection("GROUPS").document(groupId)
                        .update("ImageURL", displayPictureURL)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
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
        GroupDp.setImageBitmap(bitmap);
        imageByte = baos.toByteArray();
    }

}
