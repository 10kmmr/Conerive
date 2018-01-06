package com.example.sage.mapsexample;
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
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    //list of permission
    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};



    private static final String TAG = "MainActivity" ;



    //fireBase Auth
    private FirebaseAuth mAuth;

    //random values
    private boolean isLoggedin=false;
    int PERMISSION_ALL = 1;

    //View Declarations
    public Button submit;
    public EditText Name;
    public EditText pass;
    public EditText Email;
    public EditText groupName;
    public TextView emailD;
    public Button goMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getLocationPermission();
        mAuth = FirebaseAuth.getInstance();
        //view Declarations
        submit=(Button)findViewById(R.id.Login);
        Name=(EditText)findViewById(R.id.Name);
        groupName=(EditText)findViewById(R.id.groupName);
        Email=(EditText)findViewById(R.id.Email);
        pass=(EditText)findViewById(R.id.Password);
        emailD=(TextView)findViewById(R.id.EmailD);
        goMap=(Button)findViewById(R.id.goMaps);
    }
    //firebase Auth methords
    //----------------------------------------------------------------------------------------------//
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            Log.d(TAG, "onStart: logged in ");
            isLoggedin=true;
            updateUI(currentUser);
        }

    }


    // ALL THE PERMISSION CRAP ---------------------------------------------------------------------//

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: ");
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        /*else
            startActivity(intent);*/
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
        Log.d(TAG, "onRequestPermissionsResult: requestcode"+requestCode);
        Log.d(TAG, "onRequestPermissionsResult: permission"+permissions.toString());
        Log.d(TAG, "onRequestPermissionsResult: grantResult"+grantResults.toString());
        if (requestCode == PERMISSION_ALL) {
            if(grantResults.length == 3) {
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


    private void updateUI(FirebaseUser currentUser){
        Email.setVisibility(View.INVISIBLE);
        pass.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);
        Name.setVisibility(View.VISIBLE);
        groupName.setVisibility(View.VISIBLE);
        emailD.setVisibility(View.VISIBLE);
        emailD.setText(currentUser.getEmail());
        goMap.setVisibility(View.VISIBLE);
    }


    public void SignButton(View v){
        String email=  Email.getText().toString();
        String passW = pass.getText().toString();
        if(mAuth.getCurrentUser()==null){
            mAuth.createUserWithEmailAndPassword(email, passW)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            }
                        }
                    });

        }else{
            signIn(email,passW);
        }
    }
    private void signIn(String email,String passW){
        mAuth.signInWithEmailAndPassword(email, passW)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }
    public void done(View v){
        //WRITE INTENT HERE
        Intent intent=new Intent(this,MapsActivity.class);
        intent.putExtra("Name", Name.getText().toString());
        intent.putExtra("GroupID", groupName.getText().toString());
        intent.putExtra("UserID", mAuth.getCurrentUser().getUid());
        startActivity(intent);
    }
}