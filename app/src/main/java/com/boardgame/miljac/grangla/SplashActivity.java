package com.boardgame.miljac.grangla;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by miljac on 4.10.2017..
 */

public class SplashActivity extends AppCompatActivity {
    private int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.splash_layout);


        /*LockSupport.parkNanos(2_000_000_000);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();*/
    }

    public void enter(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
    }

    public void help(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void exit(View view) {
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)// && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)// && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)// && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }


    }
}