package com.example.sage.mapsexample;

import android.*;
import android.Manifest;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //list of permission
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE};


    private static final String TAG = "MainActivity";

    Intent loggedin;

    //fireBase Auth
    private FirebaseAuth mAuth;

    //random values
    private boolean isLoggedin = false;
    int PERMISSION_ALL = 1;

    //View Declarations
    public Button login;
    public EditText Name;
    public EditText pass;
    public EditText Email;
    public EditText PhoneNumber;
    public Button signUp;
    public Button getStarted;

    //Database
    FirebaseDatabase database;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        getLocationPermission();
        mAuth = FirebaseAuth.getInstance();
        //view Declarations
        login = (Button) findViewById(R.id.Login);
        Name = (EditText) findViewById(R.id.Name);
        Email = (EditText) findViewById(R.id.Email);
        pass = (EditText) findViewById(R.id.Password);
        signUp = (Button) findViewById(R.id.signUp);
        PhoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        getStarted = (Button) findViewById(R.id.getStarted);

        database = FirebaseDatabase.getInstance();

        loggedin = new Intent(this, Groupselector.class);





        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String passW = pass.getText().toString();
                signIn(email, passW);
            }
        });
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: hello world");
                String email = Email.getText().toString();
                String passW = pass.getText().toString();
                String PhoneNo = PhoneNumber.getText().toString();
                String nameString = Name.getText().toString();
                SignupButton(email, passW, nameString, PhoneNo);
            }
        });

    }

    //firebase Auth methords
    //----------------------------------------------------------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onStart: logged in ");
            isLoggedin = true;
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


    private void updateUI() {

        login.setVisibility(View.INVISIBLE);
        signUp.setVisibility(View.INVISIBLE);

        Name.setVisibility(View.VISIBLE);
        PhoneNumber.setVisibility(View.VISIBLE);
        getStarted.setVisibility(View.VISIBLE);
    }


    public void SignupButton(final String email, String passW, final String nameString, final String PhoneNumber) {
        if (mAuth.getCurrentUser() == null) {
            mAuth.createUserWithEmailAndPassword(email, passW)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                database.getReference("Details/" + mAuth.getUid() + "/Name").setValue(nameString);
                                database.getReference("Details/" + mAuth.getUid() + "/phonenumber").setValue(PhoneNumber);
                                database.getReference("Details/" + mAuth.getUid() + "/Email").setValue(email);
                                NextActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        }
                    });
        }
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
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    //    public void done() {
//        String name=Name.getText().toString();
//        String uniid = mAuth.getCurrentUser().getUid();
//        dbref= database.getReference("Details/"+uniid);
//        database.getReference("Details/"+uniid+"/Name").setValue(name);
//        //database.getReference("Details/"+uniid+"/Group").push().setValue(grpName);
//        Intent intent = new Intent(this, MapsActivity.class);
//        intent.putExtra("Name",name );
//        intent.putExtra("GroupID",grpName);
//        intent.putExtra("UserID", uniid);
//        startActivity(intent);
//    }
    public void NextActivity() {
        database.getReference("Details").child(mAuth.getUid()).child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loggedin.putExtra("UserID", mAuth.getUid());
                Log.d(TAG, "what is name here: " + dataSnapshot.getValue().toString());
                loggedin.putExtra("Name", dataSnapshot.getValue().toString());
                loggedin.putExtra("GroupID", "NULL");
                startActivity(loggedin);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void MakeLog(String mesaage) {
        Log.d(TAG, " MakeLog  : MainActivity : " + mesaage);
    }
}