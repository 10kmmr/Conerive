package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Email_auth extends AppCompatActivity {

    private static final String TAG = "Email_auth";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);
        mAuth = FirebaseAuth.getInstance();
        getLocationPermission();
        Email = (EditText) findViewById(R.id.EmailE);
        pass = (EditText) findViewById(R.id.PasswordE);
        signUp = (Button) findViewById(R.id.signUp);
        loginE = (Button)findViewById(R.id.loginE);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignupButton(Email.getText().toString(),pass.getText().toString());
            }
        });
        loginE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(Email.getText().toString(),pass.getText().toString());
            }
        });
    }

    private void signIn(String email, String passW) {
        mAuth.signInWithEmailAndPassword(email, passW)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            NextActivity();
                        }
                    }
                });
    }

    public FirebaseAuth mAuth;

    // Permission Objects
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CALL_PHONE};
    private int PERMISSION_ALL = 1;
    public EditText pass;
    public EditText Email;
    public Button signUp;
    public Button loginE;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: logged in ");
            NextActivity();
        }
    }

    // ALL THE PERMISSION CRAP ---------------------------------------------------------------------//

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
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
        Log.d(TAG, "onRequestPermissionsResult: requestcode" + requestCode);
        Log.d(TAG, "onRequestPermissionsResult: permission" + permissions.toString());
        Log.d(TAG, "onRequestPermissionsResult: grantResult" + grantResults.toString());
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length == 3) {
                // We can now safely use the API we requested access to
                Log.d(TAG, "onRequestPermissionsResult: if condition");
                //startActivity(intent);

            } else {
                // Permission was denied or request was cancelled
                Log.d(TAG, "onRequestPermissionsResult: else");
            }
        }
    }
// ALL THE PERMISSION CRAP ---------------------------------------------------------------------//

    public void NextActivity(){
        Intent intent = new Intent(getApplicationContext(), UserCreateActivity.class);
        startActivity(intent);
    }
    public void SignupButton(final String email, String passW) {
        if (mAuth.getCurrentUser() == null) {
            mAuth.createUserWithEmailAndPassword(email, passW)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: " + "firebase ");
                                NextActivity();
                            }
                        }
                    });
        }
    }
}
