package com.example.miljac.myapplication;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class GamePlayActivity extends AppCompatActivity implements TableFragment.OnFieldSelectedListener {

    private TableView tableView;
    private OtherPlayer otherPlayer = new OtherPlayer();
    private Table table;
    private TableFragment tableFragment;

    private Coordinates c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        table = new Table(1);



        tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = (TableView) tableFragment.tableView;

        Thread opThread = new Thread(otherPlayer);
        //runOnUiThread(otherPlayer);
        opThread.start();

    }


    private class OtherPlayer implements Runnable {
        public void run() {
            Log.d("AAjajajajjajajajajA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
            while(true){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                Log.d("AAjajajajjajajajajA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");


                c = table.putAutomatic(State.cross);

                System.out.println(c.x);
                System.out.println(c.y);


                class OtherPlayerDraws implements Runnable {
                    public void run() {
                        tableView.changePinColor(c.x*tableFragment.pinSize +1, c.y*tableFragment.pinSize +1, R.drawable.pin40);
                        tableView.invalidate();
                    }
                }
                OtherPlayerDraws otherPlayerDraws = new OtherPlayerDraws();
                runOnUiThread(otherPlayerDraws);

                //postDelayed(tableView, DELAY_TIME_MILLIS);
            }

        }
    }

    public void onFieldSelected(int x,int y) {
        System.out.println("\njeldaaa\n");

        tableView.changePinColor(x*tableFragment.pinSize +1, y*tableFragment.pinSize +1, R.drawable.pin39);
        table.put(State.circle, x, y);



    }
}
