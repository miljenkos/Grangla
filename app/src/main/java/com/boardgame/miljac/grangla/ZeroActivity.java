package com.boardgame.miljac.grangla;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * First activity to run, contains splash screen
 */

public class ZeroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(ZeroActivity.this, MainMenuActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}