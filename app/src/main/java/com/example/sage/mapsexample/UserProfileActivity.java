package com.example.sage.mapsexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    public String baseUrl;
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

    public TextView nameTV;
    public TextView emailTV;
    public TextView phoneTV;
    public ImageView displayPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        baseUrl = getString(R.string.api_url);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        requestQueue = Volley.newRequestQueue(this);
        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("user_display_picture");
        displayPictureURL = "";

        nameTV = findViewById(R.id.name);
        emailTV = findViewById(R.id.email);
        phoneTV = findViewById(R.id.phone);
        displayPicture = findViewById(R.id.displayPicture);
        getUserDetails();
    }

    void getUserDetails(){
        userId = mAuth.getUid();
        String url = baseUrl+"users/"+userId;
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            name = jsonObject.getString("Name");
                            nameTV.setText(name);
                            phone = jsonObject.getString("Phone");
                            phoneTV.setText(phone);
                            if(!jsonObject.isNull("Email_id")){
                                email = jsonObject.getString("Email_id");
                                emailTV.setText(email);
                            } else {
                                emailTV.setText("no email");
                            }
                            if(!jsonObject.isNull("Image_url")){
                                displayPictureURL = jsonObject.getString("Image_url");
                                downloadDisplayPicture();
                            }
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
        );
        requestQueue.add(getRequest);
    }

    void downloadDisplayPicture(){

        StorageReference ref = displayPictureReference.child(userId+".jpg");
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: "+"some stuff");
                    Bitmap image;
                    image= BitmapFactory.decodeFile(localFile.getAbsolutePath());
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
