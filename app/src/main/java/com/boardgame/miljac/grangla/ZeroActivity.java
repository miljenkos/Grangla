package com.boardgame.miljac.grangla;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by miljac on 4.10.2017..
 */

public class ZeroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LockSupport.parkNanos(2_000_000_000);

        Intent intent = new Intent(ZeroActivity.this, SplashActivity.class);
        startActivity(intent);
        //finish();


    }

    @Override
    public void onRestart(){


        super.onRestart();
        finish();

    }

    @Override
    protected void onDestroy() {
        //Process.killProcess(Process.myPid());

        super.onDestroy();
        System.exit(0);

    }


    }