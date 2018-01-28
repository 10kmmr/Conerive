package com.example.sage.mapsexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Groupselector extends AppCompatActivity {

    private int switchView = 1;
    listfragment list = new listfragment();
    JoinRegGroupFragment joinReg = new JoinRegGroupFragment();
    public Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupselector);
        View frag = findViewById(R.id.fragment_container);
        if (frag != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, list).commit();
            button2 = (Button) findViewById(R.id.button2);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (switchView == 1) {
                        joinReg.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, joinReg).commit();
                        button2.setText("Back");
                        switchView = 0;
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, list).commit();
                        button2.setText("ADD GROUP");
                        switchView = 1;
                    }
                }
            });
        }
    }
}
