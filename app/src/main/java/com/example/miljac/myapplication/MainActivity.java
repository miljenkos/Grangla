package com.example.miljac.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SeekBar levelSeekBar;
    private TextView levelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        levelSeekBar = (SeekBar)findViewById(R.id.levelSeekBar);
        levelSeekBar.setProgress(30);
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

        System.out.println("TU SAMM");
        Spinner spinnerPlayer1 = (Spinner) findViewById(R.id.spinner_player1);
        spinnerPlayer1.setAdapter(new MyAdapter(this, R.layout.row, getAllList()));

        Spinner spinnerPlayer2 = (Spinner) findViewById(R.id.spinner_player2);
        spinnerPlayer2.setAdapter(new MyAdapter(this, R.layout.row, getAllList()));
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, GamePlayActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();
        intent.putExtra("LEVEL", levelSeekBar.getProgress());
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public ArrayList<ListItem> getAllList() {

        ArrayList<ListItem> allList = new ArrayList<ListItem>();

        ListItem item = new ListItem();
        item.setData("OKO", "Onaj koji vidi sve,pa i tebe", R.drawable.pin39);
        allList.add(item);

        item = new ListItem();
        item.setData("GUMB", "Statican, ali pouzdan", R.drawable.pin40);
        allList.add(item);

        return allList;
    }

}
