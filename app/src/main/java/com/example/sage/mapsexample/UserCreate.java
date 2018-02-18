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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UserCreate extends AppCompatActivity {

    private static final String TAG = "UserCreate";
    public String baseUrl = "http://192.168.2.4:8080/";
    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    FirebaseStorage firebaseStorage;
    StorageReference displayPictureReference;
    public RequestQueue requestQueue;

    public String userId;
    public String name;
    public String email;
    public String phone;
    public String displayPictureURL;

    public boolean waitingForEmailsDBUpdate = true;
    public boolean waitingForDisplayPicturesDBUpdate = true;

    public EditText nameET;
    public EditText emailET;
    public Button done;
    public Button chooseDisplayPicture;
    public ImageView displayPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_create);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        requestQueue = Volley.newRequestQueue(this);

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email);
        done = findViewById(R.id.done);
        chooseDisplayPicture = findViewById(R.id.chooseDisplayPicture);
        displayPicture = findViewById(R.id.displayPicture);
        displayPictureURL = "";

        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("user_display_picture");




        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = mAuth.getUid();
                name = nameET.getText().toString();
                email = emailET.getText().toString();
                phone = currentUser.getPhoneNumber();
                createUser();
            }
        });

        chooseDisplayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = mAuth.getUid();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
    }
    
    public void createUser(){
        String url = baseUrl+"users";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse - users: " + jsonObject);
                            if(email.length()>0){
                                createEmail();
                            } else {
                                waitingForEmailsDBUpdate = false;
                            }
                            if(displayPictureURL.length()>0){
                                createDisplayPicture();
                            } else {
                                waitingForDisplayPicturesDBUpdate = false;
                            }
                            goToUserProfile();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response - createuser", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("userId", userId);
                params.put("name", name);
                params.put("phone",phone);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    public void createEmail(){
        Log.d(TAG, "createEmail: " + "accessed");
        String url = baseUrl+"users/emails";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse - emails: " + jsonObject);
                            waitingForEmailsDBUpdate = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("userId", userId);
                params.put("email", email);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    public void createDisplayPicture(){
        Log.d(TAG, "createDisplayPicture: " + "accessed");
        String url = baseUrl+"users/display-pictures";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse - display-pictures : " + jsonObject);
                            waitingForDisplayPicturesDBUpdate = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("userId", userId);
                params.put("displayPictureURL", displayPictureURL);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByte = baos.toByteArray();
        UploadTask uploadTask = displayPictureReference.child(userId+".jpg").putBytes(imageByte);
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
                displayPictureURL = downloadUrl.toString();
                displayPicture.setImageBitmap(bitmap);
            }
        });

    }

    public void goToUserProfile(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!waitingForEmailsDBUpdate && !waitingForDisplayPicturesDBUpdate){
                Intent intent = new Intent(UserCreate.this, UserProfile.class);
                startActivity(intent);
                } else {
                    Log.d(TAG, "run: " + "some stuff");
                    handler.postDelayed(this, 500);
                }
            }
        });
    }


}
