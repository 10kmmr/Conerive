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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    boolean isEmailValid;
    boolean isPassValid;
    boolean isNameValid;
    boolean isPhoneNumberValid;

    //Database
    FirebaseDatabase database;
    DatabaseReference dbref;

//    CallbackManager mcallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //facebook login crap
//        mcallbackManager = CallbackManager.Factory.create();
//        LoginButton loginButton = findViewById(R.id.login_button);
//        loginButton.setReadPermissions("email", "public_profile","pages_messaging_phone_number ");
//        loginButton.registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "facebook:onSuccess:" + loginResult);
//                handleFacebookAccessToken(loginResult.getAccessToken());
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG, "facebook:onCancel");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.d(TAG, "facebook:onError", error);
//            }
//        });

        isEmailValid = false;
        isPassValid = false;
        isNameValid = false;
        isPhoneNumberValid = false;


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


        Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp != null) {
                    Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
                    Matcher mat = pattern.matcher(temp);
                    if (mat.matches()) {
                        isEmailValid = true;
                    } else {
                        isEmailValid = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp.length() > 0) {
                    isPassValid = true;
                } else {
                    isPassValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = charSequence.toString();
                if (temp.length() > 0) {
                    isNameValid = true;
                } else {
                    isNameValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

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

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //no need validations
//                if(!isEmailValid){
//                    Toast.makeText(MainActivity.this, " invalid email id ", Toast.LENGTH_SHORT).show();
//                } else if(!isPassValid){
//                    Toast.makeText(MainActivity.this, " invalid password", Toast.LENGTH_SHORT).show();
//                } else
                {
                    updateUI();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEmailValid){
                    Toast.makeText(MainActivity.this, " invalid email id ", Toast.LENGTH_SHORT).show();
                } else if(!isPassValid){
                    Toast.makeText(MainActivity.this, " invalid password", Toast.LENGTH_SHORT).show();
                } else {
                    String email = Email.getText().toString();
                    String passW = pass.getText().toString();
                    signIn(email, passW);
                }
            }
        });

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNameValid) {
                    Toast.makeText(MainActivity.this, "name is invalid", Toast.LENGTH_SHORT).show();
                } else if (!isPhoneNumberValid) {
                    Toast.makeText(MainActivity.this, "Phone number is invalid", Toast.LENGTH_SHORT).show();
                } else {
                    String email = Email.getText().toString();
                    String passW = pass.getText().toString();
                    String PhoneNo = PhoneNumber.getText().toString();
                    String nameString = Name.getText().toString();
                    SignupButton(email, passW, nameString, PhoneNo);
                }
            }
        });

    }


    /*------------------------------------------FACEBOOK --------------------------------------------------------------------*/
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Pass the activity result back to the Facebook SDK
//        mcallbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void handleFacebookAccessToken(AccessToken token) {
//        Log.d(TAG, "handleFacebookAccessToken:" + token);
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//
//        //token.getSource()
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Log.d(TAG, "onComplete: " +mAuth.getCurrentUser().getPhoneNumber());
//                            //SignupButton
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(MainActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            //SignupButton
//                        }
//
//                        // ...
//                    }
//                });
//    }
    /*------------------------------------------FACEBOOK --------------------------------------------------------------------*/




    //firebase Auth methords
    //----------------------------------------------------------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            isLoggedin = true;
            NextActivity();
        }
    }


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
                                FirebaseUser user = mAuth.getCurrentUser();
                                database.getReference("Details/" + mAuth.getUid() + "/Name").setValue(nameString);
                                database.getReference("Details/" + mAuth.getUid() + "/phonenumber").setValue(PhoneNumber);
                                database.getReference("Details/" + mAuth.getUid() + "/Email").setValue(email);
                                NextActivity();
                            } else {
                                try{
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(MainActivity.this, "User with this email id already exists", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthWeakPasswordException e){
                                    Toast.makeText(MainActivity.this, "Password should have at least 6 characters", Toast.LENGTH_SHORT).show();
                                } catch (Exception otherExcetption){
                                    otherExcetption.printStackTrace();
                                }
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
                            //FirebaseUser user = mAuth.getCurrentUser();
                            NextActivity();
                        } else {
                            try{
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                Toast.makeText(MainActivity.this, "User with this email does not exist", Toast.LENGTH_SHORT).show();
                            } catch (Exception otherException){
                                otherException.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void NextActivity() {
        database.getReference("Details").child(mAuth.getUid()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loggedin.putExtra("UserID", mAuth.getUid());
                loggedin.putExtra("Name", dataSnapshot.getValue().toString());
                loggedin.putExtra("GroupID", "NULL");
                Email.addTextChangedListener(null);
                pass.addTextChangedListener(null);
                Name.addTextChangedListener(null);
                PhoneNumber.addTextChangedListener(null);
                signUp.setOnClickListener(null);
                login.setOnClickListener(null);
                getStarted.setOnClickListener(null);
                startActivity(loggedin);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }
}