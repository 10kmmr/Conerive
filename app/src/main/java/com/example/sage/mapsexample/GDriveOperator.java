package com.example.sage.mapsexample;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;

import com.google.android.gms.tasks.Task;

/**
 * Created by hvpri on 10-06-2018.
 */

public class GDriveOperator {

    //Google Account api
    public GoogleSignInClient mGoogleSignInClient;
    public GoogleSignInAccount account=null;
    private String serverClientId;
    public String authCode;


    //Android related objects
    private Context ActivityContext;

    //Google Drive
    public DriveClient mDriveClient;
    public DriveResourceClient mDriveResourceClient;


    GDriveOperator(Context mcontext,String mServerClientId){
        this.ActivityContext = mcontext;
        this.serverClientId = mServerClientId;
//        mGoogleSignInClient = buildGoogleSignInClient();
        mGoogleSignInClient = buildGoogleSignInClient();
    }

    public void HandleActivityOnResult(Task<GoogleSignInAccount> completedTask){
        try {
            this.account = completedTask.getResult(ApiException.class);
            this.authCode = account.getServerAuthCode();
            /*
            * write the authCode to the Db of the current user in another object
            * */
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDriveClient = Drive.getDriveClient(ActivityContext, GoogleSignIn.getLastSignedInAccount(ActivityContext));
        mDriveResourceClient =
                Drive.getDriveResourceClient(ActivityContext, GoogleSignIn.getLastSignedInAccount(ActivityContext));
    }

    private GoogleSignInClient buildGoogleSignInClient() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE,Drive.SCOPE_APPFOLDER)
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        return  GoogleSignIn.getClient(ActivityContext,gso);
    }

    public Intent GetIntent(){
        return mGoogleSignInClient.getSignInIntent();
    }


}
