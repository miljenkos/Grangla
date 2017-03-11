package com.example.miljac.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private Coordinates lastMoveX;
    private Coordinates lastMoveY;
    private EndStruct endStruct;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();

    Thread opThread;
    Thread refreshThread;
    Thread uIPutThread;
    class UIPut implements Runnable {

        public void run() {
            //synchronized (table) {


                for (int i = 0; i < table.TABLE_SIZE; i++) {
                    for (int j = 0; j < table.TABLE_SIZE; j++) {
                        if (table.publicGet(i, j) == State.circle)
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin39);
                        if (table.publicGet(i, j) == State.cross)
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin40);
                        if (table.publicGet(i, j) == State.empty)
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin41);
                    }
                }
                tableView.invalidate();


                if (lastMoveX != null) {
                    table.end2(lastMoveX.x, lastMoveX.y);
                    lastMoveX = null;
                }

                if (lastMoveY != null) {
                    table.end2(lastMoveY.x, lastMoveY.y);
                    lastMoveY = null;
                }

            /*
                endStruct = table.end();
                if (endStruct.winner != State.empty) {
                    table.publicEmpty(endStruct.first.x, endStruct.first.y);
                    table.publicEmpty(endStruct.second.x, endStruct.second.y);
                    table.publicEmpty(endStruct.third.x, endStruct.third.y);
                    table.publicEmpty(endStruct.fourth.x, endStruct.fourth.y);
                }*/

                //}

        }
    }

    private class TableViewRefreshing implements Runnable {
        public void run() {

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
        tableView = tableFragment.tableView;

        opThread = new Thread(otherPlayer);
        opThread.start();

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameDone = true;
    }


    private class OtherPlayer implements Runnable {
        public void run() {

            while(!gameDone){
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //ww.lock();
                synchronized (table) {

                    c = table.putAutomatic(State.cross);
                    lastMoveY = new Coordinates(c.x, c.y);
                }

            }

        }
    }

    public void onFieldSelected(int x,int y) {

        synchronized (table) {
            this.table.publicPut(State.circle, x, y);
            lastMoveX = new Coordinates(x,y);
        }
    }
}
