package com.example.miljac.myapplication;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GamePlayActivity extends AppCompatActivity implements TableFragment.OnFieldSelectedListener {

    private TableView tableView;
    private OtherPlayer otherPlayer = new OtherPlayer();
    private TableViewRefreshing tableViewRefreshing = new TableViewRefreshing();
    private UIPut uIPut = new UIPut();
    private Table table = new Table(3);
    private TableFragment tableFragment;
    private Boolean gameDone = false;

    private Coordinates c;
    private EndStruct endStruct;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    //private final Lock ww = rwl.writeLock();

    Thread opThread;
    Thread refreshThread;
    Thread uIPutThread;


    /*class OtherPlayerCollects implements Runnable {
        private EndStruct es;

        public OtherPlayerCollects(EndStruct e){
            this.es = e;
        }

        public void run() {

            tableView.changePinColor(this.es.first.x*tableFragment.pinSize +1, this.es.first.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(this.es.second.x*tableFragment.pinSize +1, this.es.second.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(this.es.third.x*tableFragment.pinSize +1, this.es.third.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.changePinColor(this.es.fourth.x*tableFragment.pinSize +1, this.es.fourth.y*tableFragment.pinSize +1, R.drawable.pin41);
            tableView.invalidate();
        }
    }*/


    /*class OtherPlayerDraws implements Runnable {
        public void run() {
            tableView.changePinColor(c.x*tableFragment.pinSize +1, c.y*tableFragment.pinSize +1, R.drawable.pin40);
            tableView.invalidate();
        }
    }*/


    class UIPut implements Runnable {

        public void run() {
            //synchronized (table) {


                for (int i = 0; i < table.TABLE_SIZE; i++) {
                    for (int j = 0; j < table.TABLE_SIZE; j++) {
                        if (table.publicGet(i, j) == State.circle)
                            tableView.changePinColor(i * tableFragment.pinSize + 1, j * tableFragment.pinSize + 1, R.drawable.pin39);
                        if (table.publicGet(i, j) == State.cross)
                            tableView.changePinColor(i * tableFragment.pinSize + 1, j * tableFragment.pinSize + 1, R.drawable.pin40);
                        if (table.publicGet(i, j) == State.empty)
                            tableView.changePinColor(i * tableFragment.pinSize + 1, j * tableFragment.pinSize + 1, R.drawable.pin41);
                    }
                }
                tableView.invalidate();

                endStruct = table.end();
                if (endStruct.winner != State.empty) {
                    table.publicEmpty(endStruct.first.x, endStruct.first.y);
                    table.publicEmpty(endStruct.second.x, endStruct.second.y);
                    table.publicEmpty(endStruct.third.x, endStruct.third.y);
                    table.publicEmpty(endStruct.fourth.x, endStruct.fourth.y);
                }

                //}

        }
    }

    private class TableViewRefreshing implements Runnable {
        public void run() {
            System.out.println("PRVIII");

            while (!gameDone) {
                try {
                    Thread.sleep(60);
                    Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            uIPutThread = new Thread(uIPut);
            runOnUiThread(uIPutThread);

            }
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

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

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


                //ww.lock();
                synchronized (table) {

                    c = table.putAutomatic(State.cross);

                    //OtherPlayerDraws otherPlayerDraws = new OtherPlayerDraws();
                    //runOnUiThread(otherPlayerDraws);


                    /*endStruct = table.end();
                    if (endStruct.winner != State.empty) {
                        table.publicEmpty(endStruct.first.x, endStruct.first.y);
                        table.publicEmpty(endStruct.second.x, endStruct.second.y);
                        table.publicEmpty(endStruct.third.x, endStruct.third.y);
                        table.publicEmpty(endStruct.fourth.x, endStruct.fourth.y);

                        //OtherPlayerCollects otherPlayerCollects = new OtherPlayerCollects(endStruct);
                        //runOnUiThread(otherPlayerCollects);


                    }*/
                }

            }

        }
    }

    public void onFieldSelected(int x,int y) {

        synchronized (table) {

            if (this.table.publicPut(State.circle, x, y)) {
                //tableView.changePinColor(x * tableFragment.pinSize + 1, y * tableFragment.pinSize + 1, R.drawable.pin39);


                /*endStruct = table.end();
                if (endStruct.winner != State.empty) {
                    table.publicEmpty(endStruct.first.x, endStruct.first.y);
                    table.publicEmpty(endStruct.second.x, endStruct.second.y);
                    table.publicEmpty(endStruct.third.x, endStruct.third.y);
                    table.publicEmpty(endStruct.fourth.x, endStruct.fourth.y);
                    //OtherPlayerCollects otherPlayerCollects = new OtherPlayerCollects(endStruct);
                    //runOnUiThread(otherPlayerCollects);

                }*/
            }
        }
    }
}
