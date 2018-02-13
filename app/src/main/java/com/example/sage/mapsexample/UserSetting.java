package com.example.sage.mapsexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

public class UserSetting extends AppCompatActivity {

    private static final String TAG = "UserSetting";
    String baseUrl = "http://192.168.2.5:8080/";
    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    public RequestQueue requestQueue;
    String userId;
    String name;
    String email;
    String  phone;
    public EditText nameET;
    public EditText emailET;
    public Button done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        nameET = findViewById(R.id.name);
        emailET = findViewById(R.id.email);
        done = findViewById(R.id.done);
        requestQueue = Volley.newRequestQueue(this);

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


}
