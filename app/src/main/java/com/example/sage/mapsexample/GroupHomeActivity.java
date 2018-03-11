package com.example.sage.mapsexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.sage.mapsexample.GroupSettingsActivity;
import com.example.sage.mapsexample.R;

public class GroupHomeActivity extends AppCompatActivity {

    private static final String TAG = "GroupHomeActivity";

    String groupId;
    String groupName;
    String groupDisplayPictureURL;

    Toolbar toolbar;
    FloatingActionButton createTripFAB;
    TextView groupNameTV;
    NetworkImageView groupDisplayPictureNIV;

    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createTripFAB = (FloatingActionButton) findViewById(R.id.fab);
        groupNameTV = findViewById(R.id.groupName);
        groupDisplayPictureNIV = findViewById(R.id.groupDisplayPicture);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        groupDisplayPictureURL = getIntent().getStringExtra("groupDisplayPictureURL");

        requestQueue = Volley.newRequestQueue(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameTV.setText(groupName);
        if(groupDisplayPictureURL != null){
            ImageLoader imageLoader;
            imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap> mCache = new LruCache<>(10);
                public void putBitmap(String url, Bitmap bitmap) {
                    mCache.put(url, bitmap);
                }
                public Bitmap getBitmap(String url) {
                    return mCache.get(url);
                }
            });
            groupDisplayPictureNIV.setImageUrl(groupDisplayPictureURL, imageLoader);
        }

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GroupSettingsActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupDisplayPictureURL", groupDisplayPictureURL);
                startActivity(intent);
            }
        });

        createTripFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TripCreateActivity.class);
                startActivity(intent);
            }
        });
    }
}
