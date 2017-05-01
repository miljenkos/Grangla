package com.example.miljac.myapplication;

import android.app.WallpaperInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.util.LinkedList;
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
    private Coordinates lastMoveO;
    private Coordinates lastMoveX;
    private EndStruct endStruct;
    private double result = 50;
    private long waitingTimeCircle = 3000;
    private long waitingMomentCircle = 0;
    private boolean allowCircle = true;
    private long waitingTimeCross = 3000;
    private long waitingMomentCross = 0;
    private long gameStartTime, currentTime;
    private boolean allowCross = true;
    private int level;


    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    ProgressBar resultBar, resultBar2, circleBar, crossBar;

    Thread opThread;
    Thread refreshThread;
    Thread uIPutThread;

    LinkedList movesO = new LinkedList();
    LinkedList movesX = new LinkedList();

    class UIPut implements Runnable {

        public void run() {
            //synchronized (table) {


            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    if (table.publicGet(i, j) == State.circle) {
                        if ((movesO.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (movesO.get(TableConfig.MAX_PIECES - 2).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin39, 0.3f);
                        }
                        else if ((movesO.size() >= (TableConfig.MAX_PIECES-2)) &&
                                   (movesO.get(TableConfig.MAX_PIECES - 3).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin39, 0.65f);
                        }
                        else {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin39, 1f);
                        }
                    }
                    if (table.publicGet(i, j) == State.cross){
                        if ((movesX.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (movesX.get(TableConfig.MAX_PIECES - 2).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin40, 0.3f);
                        }
                        else if ((movesX.size() >= (TableConfig.MAX_PIECES-2)) &&
                                (movesX.get(TableConfig.MAX_PIECES - 3).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin40, 0.65f);
                        }
                        else {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin40, 1f);
                        }

                    }
                    if (table.publicGet(i, j) == State.empty) {




                        /*if ((movesO.size() >= (TableConfig.MAX_PIECES)) &&
                                (movesO.get(TableConfig.MAX_PIECES - 1).equals(new Coordinates(i,j)))) {
                        //if(!(movesO.contains(new Coordinates(i,j)))){
                            tableView.removeImediately(i, j);

                        } else
                        if ((movesX.size() >= (TableConfig.MAX_PIECES)) &&
                                (movesX.get(TableConfig.MAX_PIECES - 1).equals(new Coordinates(i,j)))) {
                        //if(!(movesX.contains(new Coordinates(i,j)))){
                            tableView.removeImediately(i, j);

                        } else {*/
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin41, 1f);
                            System.out.println("NIJE IMEDIATELYYYYYYYYYYYYYYYYYY");

                        //}

                        movesO.remove(new Coordinates(i, j));
                        movesX.remove(new Coordinates(i, j));
                    }

                    if (table.publicGet(i, j) == State.rock) {
                        tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin20, 1f);
                    }
                }
            }
            tableView.invalidate();


            if (lastMoveO != null) {
                double r = 0;
                r = table.end2(lastMoveO.x, lastMoveO.y);
                result += r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesO.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesO.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        result -= 3 * TableConfig.RESULT_FACTOR;
                    }

                }
                lastMoveO = null;

            }

            if (lastMoveX != null) {
                double r = 0;
                r = table.end2(lastMoveX.x, lastMoveX.y);
                result -= r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesX.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesX.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        result += 3 * TableConfig.RESULT_FACTOR;
                    }

                }
                lastMoveX = null;
            }









            /*if(resultBar.getProgress() < result) {
                resultBar.setProgress(resultBar.getProgress() - 1);
            }
            else if(resultBar.getProgress() > result) {
                resultBar.setProgress(resultBar.getProgress() + 1);*/

            currentTime = System.currentTimeMillis();

            if ((result<=0) ||
                    (result >=100) /*||
                    ((currentTime - gameStartTime) >= TableConfig.GAME_DURATION)*/){

                gameDone = true;
                AlertDialog alertDialog = new AlertDialog.Builder(GamePlayActivity.this).create();
                alertDialog.setTitle("Alert");
                if(result>=50) {
                    alertDialog.setMessage("YOU WIN THIS TIME");
                } else {
                    alertDialog.setMessage("YOU FUCKING LOOSE");
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }


            resultBar.setProgress(100-(int)result);
            resultBar2.setProgress((int)result);

            if(currentTime < waitingMomentCircle){
                circleBar.setProgress((int)((waitingMomentCircle - currentTime) * 100 / waitingTimeCircle ));

            } else {
                allowCircle = true;
                circleBar.setProgress(0);
            }

            if(currentTime < waitingMomentCross){
                crossBar.setProgress((int)((waitingMomentCross - currentTime ) * 100 / waitingTimeCross ));

            } else {
                allowCross = true;
                crossBar.setProgress(0);
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
                    Thread.sleep(100);
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

        Intent intent = getIntent();
        level = intent.getIntExtra("LEVEL", 50);
        System.out.println("LEVEL: " + level);

        this.table = new Table(level);

        resultBar = (ProgressBar)findViewById(R.id.result_bar);
        resultBar.setProgress(50);
        resultBar2 = (ProgressBar)findViewById(R.id.result_bar2);
        resultBar2.setProgress(50);

        circleBar = (ProgressBar)findViewById(R.id.circle_time_bar);
        circleBar.setProgress(0);
        circleBar.invalidate();
        crossBar = (ProgressBar)findViewById(R.id.cross_time_bar);
        crossBar.setProgress(0);
        crossBar.invalidate();

        tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = tableFragment.tableView;

        opThread = new Thread(otherPlayer);
        opThread.start();

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

        gameStartTime = System.currentTimeMillis();

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
                    double a = Math.random();
                    a*= (0.7 + 0.42/50 * Math.abs(50 - (int)result));
                    Thread.sleep(waitingTimeCross + (long)(a*( (6400 - 200) * (101-level) / 100 + 200)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //ww.lock();
                synchronized (table) {

                    c = table.putAutomatic(State.cross);
                    lastMoveX = new Coordinates(c.x, c.y);

                    currentTime = System.currentTimeMillis();
                    waitingTimeCross = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)/50 * Math.abs(50 - (int)result);
                    waitingTimeCross -= TableConfig.MIN_WAITING_TIME;
                    waitingTimeCross = (long)((double)waitingTimeCross/ (1 + (double)(currentTime-gameStartTime)/(double)TableConfig.HALF_LIFE));
                    waitingTimeCross += TableConfig.MIN_WAITING_TIME;
                    waitingMomentCross = System.currentTimeMillis() + waitingTimeCross;

                    allowCross = false;

                    movesX.push(lastMoveX);
                }

            }

        }
    }

    public void onFieldSelected(int x,int y) {

        synchronized (table) {
            if(allowCircle) {
                if(this.table.publicPut(State.circle, x, y)) {
                    lastMoveO = new Coordinates(x, y);

                    currentTime = System.currentTimeMillis();
                    waitingTimeCircle = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)/50 * Math.abs(50 - (int)result);
                    waitingTimeCircle -= TableConfig.MIN_WAITING_TIME;
                    waitingTimeCircle = (long)((double)waitingTimeCircle/ (1 + (double)(currentTime-gameStartTime)/(double)TableConfig.HALF_LIFE));
                    waitingTimeCircle += TableConfig.MIN_WAITING_TIME;
                    waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;

                    allowCircle = false;

                    movesO.push(lastMoveO);
                }
            }

        }
    }
}
