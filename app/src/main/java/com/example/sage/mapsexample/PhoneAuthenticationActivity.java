package com.example.sage.mapsexample;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONArray;

import java.util.concurrent.TimeUnit;

public class PhoneAuthenticationActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    String[] PERMISSIONS = { android.Manifest.permission.ACCESS_FINE_LOCATION,
                             android.Manifest.permission.ACCESS_COARSE_LOCATION,
                             android.Manifest.permission.CALL_PHONE };

    private String baseUrl = "http://192.168.2.2:8080/";
    private int PERMISSION_ALL = 1;
    private EditText phoneNumberEditText;
    private EditText otpEditText;
    private Button sendOtpButton;
    private Button verifyOtpButton;
    private String phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private int mResendToken ;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RequestQueue requestQueue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_authentication);

        phoneNumberEditText = findViewById(R.id.phone_number);
        otpEditText = findViewById(R.id.otp);
        sendOtpButton = findViewById(R.id.send_otp);
        verifyOtpButton = findViewById(R.id.verify_otp);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);
        getPermissions();

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d(TAG, "onVerificationFailed", e);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token.describeContents();
                phoneNumberEditText.setVisibility(View.INVISIBLE);
                sendOtpButton.setVisibility(View.INVISIBLE);
                otpEditText.setVisibility(View.VISIBLE);
                verifyOtpButton.setVisibility(View.VISIBLE);
            }
        };

        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phoneNumberEditText.getText().toString();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber ,
                        60 ,                                                                       // Timeout duration
                        TimeUnit.SECONDS ,                                                           // Unit of timeout
                        PhoneAuthenticationActivity.this,                                     // Activity (for callback binding)
                        mCallbacks);
            }
        });

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpEditText.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            currentUser = task.getResult().getUser();
                            checkUserProfileExists();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void getPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length == 3) {
                Log.d(TAG, "onRequestPermissionsResult: " + "All Permissions Granted");
            } else {
                Log.d(TAG, "onRequestPermissionsResult: " + "All Not Granted");
            }
        }
    }

    void checkUserProfileExists() {
        String url = baseUrl+"users/" + currentUser.getUid();
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        if(response.length()>0){
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(getApplicationContext(),UserCreateActivity.class);
                            startActivity(intent);
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
}
