package com.example.sage.mapsexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splashScreen extends AppCompatActivity {
    private TextView tv;
    private ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        tv = (TextView) findViewById(R.id.splashtxt);
        iv = (ImageView) findViewById(R.id.splashimg);
        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.splashscreen);
        tv.startAnimation(myanim);
        iv.startAnimation(myanim);
        final Intent main = new Intent(this,MainActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(main);
                    finish();
                }
            }
        };
        timer.start();
    }

}
