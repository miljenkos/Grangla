package com.boardgame.miljac.grangla;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by miljac on 4.10.2017..
 */

public class ZeroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LockSupport.parkNanos(2_000_000_000);

        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();


    }


    }