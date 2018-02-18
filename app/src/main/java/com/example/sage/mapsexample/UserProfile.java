package com.example.sage.mapsexample;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile extends AppCompatActivity {

    private static final String TAG = "UserProfile";
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

    public TextView nameTV;
    public TextView emailTV;
    public TextView phoneTV;
    public ImageView displayPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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

    }

}
