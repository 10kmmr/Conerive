package com.example.sage.mapsexample;

import android.app.Activity;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroupCreateActivity extends AppCompatActivity {

    //Views
    Button GroupButton;
    Button OpenGalley;
    EditText GroupNameET;
    EditText GroupDescET;
    ImageView GroupDp;

    //data
    String groupId ;
    String GroupDesc;
    String GroupName;
    Uri downloadUrl;
    public String displayPictureURL;

    int GET_FROM_GALLERY = 3;

    //volley stuff
    public String baseUrl = "http://192.168.1.113:8080/";
    public RequestQueue requestQueue;

    //firebase
    FirebaseAuth mAuth;
    StorageReference displayPictureReference;
    FirebaseStorage firebaseStorage;

    //delete this later
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this,"die da",Toast.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        GroupButton = (Button)findViewById(R.id.newGroup);
        OpenGalley = (Button)findViewById(R.id.OpenGalley);
        GroupNameET = (EditText) findViewById(R.id.GName);
        GroupDescET = (EditText)findViewById(R.id.GroupDescET);
        GroupDp = (ImageView)findViewById(R.id.GroupDp);

        //firebase stuff
        firebaseStorage = FirebaseStorage.getInstance();
        displayPictureReference = firebaseStorage.getReference().child("Group_display_picture");

        //Event listener
        OpenGalley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY;
                startActivityForResult(intent, 0);
            }
        });

        GroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupName = GroupNameET.getText().toString();
                GroupDesc = GroupDescET.getText().toString();
                //write code for getting image here
                dbCreategroup();
            }
        });
    }

    public void  dbCreategroup(){
        String url = baseUrl+"groups";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            groupId = jsonObject.get("insertId").toString();
                            if(displayPictureURL.length()>0){
                                dbCreateDisplayPicture();
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
                        Log.d("Error.Response - createuser", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("groupName",GroupName);
                params.put("adminId",mAuth.getCurrentUser().getUid());
                return params;
            }
        };
        requestQueue.add(postRequest);
    }
    public void dbCreateDisplayPicture(){
        String url = baseUrl+"/groups/display-pictures/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        NextActivity();
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
                params.put("displayPictureURL",displayPictureURL);
                params.put("adminId",mAuth.getCurrentUser().getUid());
                return params;
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByte = baos.toByteArray();
        //fireBase updating dp
        UploadTask uploadTask = displayPictureReference.child(groupId+".jpg").putBytes(imageByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                displayPictureURL = downloadUrl.toString();
                GroupDp.setImageBitmap(bitmap);
            }
        });
    }

    public void NextActivity(){
        //write intent to go to the Group's Home page
        Intent intent = new Intent(this, Home_GroupListActivity.class);
        startActivity(intent);
    }
}
