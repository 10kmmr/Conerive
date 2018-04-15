package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class FriendsActivity extends AppCompatActivity {

    FloatingActionButton addFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        addFriendButton = findViewById(R.id.add_friends);
    }

    @Override
    protected void onStart() {
        super.onStart();

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendsActivity.class);
                startActivity(intent);
            }
        });
    }
}
