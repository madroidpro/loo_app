package com.relief.madroid.looapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


/**
 * Created by madroid on 21-11-2015.
 */
public class Splash extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {

                finish();
                Intent intent = new Intent(Splash.this, MapsActivity.class);
                Splash.this.startActivity(intent);
            }
        }, 2000);
    }

}
