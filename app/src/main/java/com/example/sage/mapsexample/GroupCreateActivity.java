package com.example.sage.mapsexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;

public class GroupCreateActivity extends AppCompatActivity {

    //Views
    Button GroupButton;
    Button OpenGalley;
    EditText GroupNameET;
    EditText GroupDescET;

    int GET_FROM_GALLERY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_create);
        GroupButton = (Button)findViewById(R.id.newGroup);
        OpenGalley = (Button)findViewById(R.id.OpenGalley);
        GroupNameET = (EditText) findViewById(R.id.GName);
        GroupDescET = (EditText)findViewById(R.id.GroupDescET);

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
                String GroupName = GroupNameET.getText().toString();
                String GroupDesc = GroupDescET.getText().toString();
                //write code for getting image here

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void NextActivity(){
        //write intent to go to the Group's Home page
    }
}
