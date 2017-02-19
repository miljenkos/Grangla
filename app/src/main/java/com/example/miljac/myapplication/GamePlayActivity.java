package com.example.miljac.myapplication;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class GamePlayActivity extends AppCompatActivity implements TableFragment.OnFieldSelectedListener {

    private TableView tableView;
    private OtherPlayer otherPlayer = new OtherPlayer();
    private Table table = new Table(3);
    private TableFragment tableFragment;
    private Boolean gameDone = false;

    private Coordinates c;
    private EndStruct endStruct;

    Thread opThread;


    class OtherPlayerCollects implements Runnable {
        public void run() {

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            tableView.changePinColor(endStruct.first.x*tableFragment.pinSize +1, endStruct.first.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(endStruct.second.x*tableFragment.pinSize +1, endStruct.second.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(endStruct.third.x*tableFragment.pinSize +1, endStruct.third.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(endStruct.fourth.x*tableFragment.pinSize +1, endStruct.fourth.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.invalidate();
        }
    }


    class OtherPlayerDraws implements Runnable {
        public void run() {
            tableView.changePinColor(c.x*tableFragment.pinSize +1, c.y*tableFragment.pinSize +1, R.drawable.pin40);
            tableView.invalidate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        this.table = new Table(3);



        tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = (TableView) tableFragment.tableView;

        opThread = new Thread(otherPlayer);
        //runOnUiThread(otherPlayer);
        opThread.start();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameDone = true;
        /*try {
            opThread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
    }


    private class OtherPlayer implements Runnable {
        public void run() {

            while(!gameDone){
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                c = table.putAutomatic(State.cross);

                OtherPlayerDraws otherPlayerDraws = new OtherPlayerDraws();
                runOnUiThread(otherPlayerDraws);


                endStruct = table.end();
                if (endStruct.winner != State.empty){
                    OtherPlayerCollects otherPlayerCollects = new OtherPlayerCollects();
                    runOnUiThread(otherPlayerCollects);
                    table.publicEmpty(endStruct.first.x, endStruct.first.y);
                    table.publicEmpty(endStruct.second.x, endStruct.second.y);
                    table.publicEmpty(endStruct.third.x, endStruct.third.y);
                    table.publicEmpty(endStruct.fourth.x, endStruct.fourth.y);
                }

                //postDelayed(tableView, DELAY_TIME_MILLIS);
            }

        }
    }

    public void onFieldSelected(int x,int y) {

        if (this.table.publicPut(State.circle, x, y))
            tableView.changePinColor(x*tableFragment.pinSize +1, y*tableFragment.pinSize +1, R.drawable.pin39);
    }
}
