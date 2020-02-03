package com.max_belleville.messaging_app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //On app open, setup device fragment(find bluetooth device)

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
    }

}
