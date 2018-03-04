package com.example.sage.mapsexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupInviteActivity extends AppCompatActivity {

    private static final String TAG = "GroupInviteActivity";
    String receiverPhoneNumber;
    String userId;
    String groupId;
    String groupName;

    Button inviteButton;
    EditText receiverPhoneNumberET;

    private FirebaseAuth mAuth;
    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invite);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        inviteButton = findViewById(R.id.invite);
        receiverPhoneNumberET = findViewById(R.id.phone_number);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiverPhoneNumber = receiverPhoneNumberET.getText().toString();
                Log.d(TAG, "onClick: clicked");
                inviteButton.setOnClickListener(null);
                dbCreateNotification();
            }
        });
    }

    void dbCreateNotification() {
        String url = getString(R.string.fcm_url)+"fcm/notification/GroupInvitation/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response - createnotification", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("senderId", userId);
                params.put("receiverPhoneNumber", receiverPhoneNumber);
                params.put("groupId", groupId);
                params.put("groupName", groupName);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

}
