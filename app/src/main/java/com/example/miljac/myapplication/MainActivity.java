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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SeekBar levelSeekBar;
    private TextView levelTextView;
    Spinner spinnerPlayer1;
    Spinner spinnerPlayer2;
    ListItem itemOko = new ListItem();
    ListItem itemGumb = new ListItem();
    ListItem itemDjetelina = new ListItem();
    ListItem itemZvijezda = new ListItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        itemOko.setData("OKO", "Onaj koji vidi sve,pa i tebe", R.drawable.pin39);
        itemGumb.setData("GUMB", "Statican, ali pouzdan", R.drawable.pin40);
        itemDjetelina.setData("DJETELINA", "I to s cetiri lista", R.drawable.pin42);
        itemZvijezda.setData("ZVIJEZDA", "U tunelu usred mraka", R.drawable.pin43);

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

        spinnerPlayer1 = (Spinner) findViewById(R.id.spinner_player1);
        spinnerPlayer1.setAdapter(new MyAdapter(this, R.layout.row, getAllList()));

        spinnerPlayer2 = (Spinner) findViewById(R.id.spinner_player2);
        final MyAdapter adapterSpinner2 = new MyAdapter(this, R.layout.row, new ArrayList<ListItem>());
        spinnerPlayer2.setAdapter(adapterSpinner2);

        spinnerPlayer1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

//                spinnerPlayer2.getse
                ListItem selected2;
                selected2 = (ListItem) spinnerPlayer2.getSelectedItem();

                adapterSpinner2.clear();
                System.out.println("SELECT ITEM: " + arg2);

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

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, GamePlayActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();
        intent.putExtra("LEVEL", levelSeekBar.getProgress());
        intent.putExtra("PLAYER1_IMG", ((ListItem) spinnerPlayer1.getSelectedItem()).logo);
        intent.putExtra("PLAYER2_IMG", ((ListItem) spinnerPlayer2.getSelectedItem()).logo);
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

        allList.add(itemOko);
        allList.add(itemGumb);
        allList.add(itemDjetelina);
        allList.add(itemZvijezda);

        return allList;
    }


}
