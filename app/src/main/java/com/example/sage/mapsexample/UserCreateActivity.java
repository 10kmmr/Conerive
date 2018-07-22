package com.example.sage.mapsexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArraySet;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
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
    private static final int GOOGLE_RETURN = 3;

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
    private byte[] imageByte;
    private boolean nameValid;
    private boolean emailValid;

    // View objects
    private EditText nameET;
    private EditText emailET;
    private Button done;

    private FloatingActionButton chooseDisplayPicture;
    private FloatingActionButton googleBT;
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

        nameValid = false;
        emailValid = true;

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email);
        done = findViewById(R.id.sign_up);
        chooseDisplayPicture = findViewById(R.id.chooseDisplayPicture);
        googleBT = findViewById(R.id.google);
        displayPicture = findViewById(R.id.display_picture);

        firebaseStorage = FirebaseStorage.getInstance();

        googleBT.setOnClickListener(new GoogleSignInOnClickListener());
        done.setOnClickListener(new CreateUserOnclickListener());

        chooseDisplayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = currentUser.getUid();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_RETURN);
            }
        });

        // TODO - implement text watchers to check if email and name are valid
        nameET.addTextChangedListener(new NameTextWatcher());
        emailET.addTextChangedListener(new EmailTextWatcher());
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + "Dont do anything");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + "Result code : " + resultCode + "\n Request code : "+requestCode);
        switch (requestCode) {
            case CAMERA_RETURN: {
                final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                imageByte = baos.toByteArray();
                displayPicture.setImageBitmap(bitmap);
                break;
            }
            case GOOGLE_RETURN: {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                if (TextUtils.isEmpty(nameET.getText().toString()))
                    nameET.setText(account.getDisplayName());
                if (TextUtils.isEmpty(emailET.getText().toString()))
                    emailET.setText(account.getEmail());
                break;
            }
            case GALLERY_RETURN : {
                // TODO - implement getting image from gallery
                break;
            }
        }
    }

    void startLoading() {
        done.setVisibility(View.INVISIBLE);
        chooseDisplayPicture.setEnabled(false);
    }

    void stopLoading() {
        done.setVisibility(View.VISIBLE);
        chooseDisplayPicture.setEnabled(true);
    }

    class CreateUserOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startLoading();

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
                                    userId = currentUser.getUid();
                                    name = nameET.getText().toString();
                                    email = emailET.getText().toString();
                                    phone = currentUser.getPhoneNumber();

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("Name", name);
                                    user.put("Phone", phone);
                                    user.put("Token", FirebaseInstanceId.getInstance().getToken());
                                    if (email != null && email.length() > 0)
                                        user.put("Email", email);
                                    if (imageURL != null && imageURL.length() > 0)
                                        user.put("ImageURL", imageURL);
                                    user.put("Friends", new ArrayList<>());
                                    user.put("Trips", new ArrayList<>());
                                    user.put("PendingFriends", new ArrayList<>());

                                    firestoreDB.runTransaction(new Transaction.Function<Void>() {
                                        @Nullable
                                        @Override
                                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                            DocumentReference userReference = firestoreDB.collection("USERS").document(currentUser.getUid());
                                            DocumentReference generalReference = firestoreDB.collection("GENERAL").document("AllUsers");

                                            DocumentSnapshot generalSnapshot = transaction.get(generalReference);
                                            ArrayList<String> appUsers = (ArrayList<String>) generalSnapshot.get("Phone");
                                            appUsers.add(phone);
                                            transaction.update(generalReference, "Phone", appUsers);
                                            transaction.set(userReference, user);
                                            return null;
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e);
                                            stopLoading();
                                            Toast.makeText(UserCreateActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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

    class GoogleSignInOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            if (account == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_RETURN);
            } else {
                if (TextUtils.isEmpty(nameET.getText().toString()))
                    nameET.setText(account.getDisplayName());
                if (TextUtils.isEmpty(emailET.getText().toString()))
                    emailET.setText(account.getEmail());
            }
        }
    }

    class NameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    class EmailTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
