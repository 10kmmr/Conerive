package com.example.sage.mapsexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //list of permission
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE};
    private static final String TAG = "MainActivity";


    //FireBase
    private FirebaseAuth mAuth;
    FirebaseDatabase database;

    //View Declarations
    private ImageView iv;
    public Button login;
    public EditText Name;
    public EditText PhoneNumber;
    public EditText OTP;
    public Button OTPlogin;

    //Intents
    Intent loggedin;

    //random values
    private boolean isLoggedin = false;
    int PERMISSION_ALL = 1;
    boolean isPhoneNumberValid;

    String mVerificationId;
    int mResendToken ;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loggedin = new Intent(this, Groupselector.class);

        //firebase
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //view Declarations
        iv = (ImageView) findViewById(R.id.splashimg2);
        OTP = (EditText) findViewById(R.id.OTP);
        OTPlogin = (Button)findViewById(R.id.OTPlogin);
        login = (Button) findViewById(R.id.Login);
        Name = (EditText) findViewById(R.id.GName);
        PhoneNumber = (EditText) findViewById(R.id.PhoneNumber);


        //------------------------------------------OPT START------------------------------------------------//
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken=token.describeContents();

                //mResendToken = token;

                // ...
            }
        };


        OTPlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Method",Toast.LENGTH_LONG).show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, OTP.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });

        //------------------------------------------OPT  END------------------------------------------------//

        //------------------------------------------Text-Watcher START------------------------------------------------//
        PhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp.length() > 0) {
                    isPhoneNumberValid = true;
                } else {
                    isPhoneNumberValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,Integer.toString(mResendToken) ,Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this,PhoneNumber.getText().toString(),Toast.LENGTH_LONG).show();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        PhoneNumber.getText().toString(),        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        MainActivity.this,               // Activity (for callback binding)
                        mCallbacks);
            }
        });
        //------------------------------------------Text-Watcher START------------------------------------------------//
        getLocationPermission();
    }
    //-----------------------------------------------Firebase Auth Methords START-----------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            isLoggedin = true;
            Intent intent = new Intent(this,UserCreate.class);
            startActivity(intent);
        }
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    //-----------------------------------------------Firebase Auth Methords END  -----------------------------------------------//

    // ALL THE PERMISSION CRAP ---------------------------------------------------------------------//

    private void getLocationPermission() {
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
                // We can now safely use the API we requested access to
//                Log.d(TAG, "onRequestPermissionsResult: if condition");
                //startActivity(intent);

            } else {
                // Permission was denied or request was cancelled
//                Log.d(TAG, "onRequestPermissionsResult: else");
            }
        }
    }
// ALL THE PERMISSION CRAP ---------------------------------------------------------------------//


//    public void NextActivity() {
//        database.getReference("Details").child(mAuth.getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                loggedin.putExtra("UserID", mAuth.getUid());
//                loggedin.putExtra("Name", dataSnapshot.getValue().toString());
//                loggedin.putExtra("GroupID", "NULL");
//                //getStarted.setOnClickListener(null);
//                startActivity(loggedin);
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) { }
//        });
//
//    }
}