package com.boardgame.miljac.grangla;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class SinglePlayerMenuActivity extends AppCompatActivity {

    private SeekBar levelSeekBar;
    private TextView levelTextView;
    Spinner spinnerPlayer1;
    Spinner spinnerPlayer2;
    ListItem itemOko = new ListItem();
    ListItem itemGumb = new ListItem();
    ListItem itemDjetelina = new ListItem();
    ListItem itemZvijezda = new ListItem();
    private SharedPreferences mPrefs;

    private void setPlayerImagesOnSpinners(){
        itemOko.setData("OKO", R.drawable.pin39);
        itemGumb.setData("GUMB", R.drawable.pin40);
        itemDjetelina.setData("DJETELINA", R.drawable.pin42);
        itemZvijezda.setData("ZVIJEZDA", R.drawable.pin43);


        spinnerPlayer1 = (Spinner) findViewById(R.id.spinner_player1);
        spinnerPlayer1.setAdapter(new MySpinnerAdapter(this, R.layout.row, getAllList()));
        spinnerPlayer1.setBackgroundColor(Color.TRANSPARENT);
        spinnerPlayer1.setDrawingCacheBackgroundColor(Color.TRANSPARENT);



        spinnerPlayer1.setOnTouchListener(


                (new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent m) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        {
                            getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                        return false;
                    }
                }));

        spinnerPlayer1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
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
        });

        spinnerPlayer2 = (Spinner) findViewById(R.id.spinner_player2);
        final MySpinnerAdapter adapterSpinner2 = new MySpinnerAdapter(this, R.layout.row, new ArrayList<ListItem>());
        spinnerPlayer2.setAdapter(adapterSpinner2);

        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        levelSeekBar.setProgress(mPrefs.getInt("mrm_LEVEL", 20));
        spinnerPlayer1.setSelection(mPrefs.getInt("mrm_PLAYER_1_IMG", 0));

        adapterSpinner2.clear();

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG", 0)==0))) {
            adapterSpinner2.add(itemOko);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG", 0)==1))) {
            adapterSpinner2.add(itemGumb);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG", 0)==2))) {
            adapterSpinner2.add(itemDjetelina);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG", 0)==3))) {
            adapterSpinner2.add(itemZvijezda);
        }

        spinnerPlayer2.setSelection(mPrefs.getInt("mrm_PLAYER_2_IMG", 0));

        spinnerPlayer1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                ListItem selected2;
                selected2 = (ListItem) spinnerPlayer2.getSelectedItem();
                adapterSpinner2.clear();
                if (!(arg2==0)) {
                    adapterSpinner2.add(itemOko);
                }
                if (!(arg2==1)) {
                    adapterSpinner2.add(itemGumb);
                }
                if (!(arg2==2)) {
                    adapterSpinner2.add(itemDjetelina);
                }
                if (!(arg2==3)) {
                    adapterSpinner2.add(itemZvijezda);
                }

                spinnerPlayer2.setSelection(adapterSpinner2.getPosition(selected2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                adapterSpinner2.clear();
                adapterSpinner2.add(itemOko);
                adapterSpinner2.add(itemGumb);
                adapterSpinner2.add(itemDjetelina);
                adapterSpinner2.add(itemZvijezda);
            }
        });

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        levelSeekBar = (SeekBar)findViewById(R.id.levelSeekBar);
        levelSeekBar.setThumbOffset(convertDipToPixels(8f));


        levelSeekBar.setProgress(20);
        levelTextView = (TextView) findViewById(R.id.level_text);
        levelSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                levelTextView.setText(String.valueOf(levelSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setPlayerImagesOnSpinners();

        if(savedInstanceState != null){
            levelSeekBar.setProgress(savedInstanceState.getInt("mrm_LEVEL"));
            spinnerPlayer1.setSelection(savedInstanceState.getInt("PLAYER_1_IMG"));
            spinnerPlayer2.setSelection(savedInstanceState.getInt("PLAYER_2_IMG"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mrm_LEVEL", levelSeekBar.getProgress());
        outState.putInt("PLAYER_1_IMG", spinnerPlayer1.getSelectedItemPosition());
        outState.putInt("PLAYER_2_IMG", spinnerPlayer2.getSelectedItemPosition());

        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mrm_LEVEL", levelSeekBar.getProgress());
        ed.putInt("mrm_PLAYER_1_IMG",spinnerPlayer1.getSelectedItemPosition());
        ed.putInt("mrm_PLAYER_2_IMG",spinnerPlayer2.getSelectedItemPosition());
        ed.commit();
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();

        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        levelSeekBar.setProgress(mPrefs.getInt("mrm_LEVEL", 20));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)// && hasFocus)
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


    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        intent.putExtra("mrm_LEVEL", levelSeekBar.getProgress());
        intent.putExtra("PLAYER1_IMG", ((ListItem) spinnerPlayer1.getSelectedItem()).logo);
        intent.putExtra("PLAYER2_IMG", ((ListItem) spinnerPlayer2.getSelectedItem()).logo);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public ArrayList<ListItem> getAllList() {
        ArrayList<ListItem> allList = new ArrayList<ListItem>();
        allList.add(itemOko);
        allList.add(itemGumb);
        allList.add(itemDjetelina);
        allList.add(itemZvijezda);

        return allList;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus)
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

    public void exit(View view) {
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
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

    private int convertDipToPixels(float dip) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = metrics.density;
        return (int)(dip * density);
    }


    public void decreaseLevel(View view) {
        int level = levelSeekBar.getProgress();
        level--;
        if((level<0) || (level >100)){
            return;
        }
        levelTextView.setText(String.valueOf(level));
        levelSeekBar.setProgress(level);
    }


    public void increaseLevel(View view) {
        int level = levelSeekBar.getProgress();
        level++;
        if((level<0) || (level >100)){
            return;
        }
        levelTextView.setText(String.valueOf(level));
        levelSeekBar.setProgress(level);
    }

}
