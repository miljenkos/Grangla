package com.example.miljac.myapplication;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
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
    private double result = 50;
    private long waitingTimeCircle = 3000;
    private long waitingMomentCircle = 0;
    private boolean allowCircle = true;
    private long waitingTimeCross = 3000;
    private long waitingMomentCross = 0;
    private long gameStartTime, currentTime, lastEventTime;
    private boolean allowCross = true;
    private int level;
    private int player1Image;
    private int player2Image;


    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    ProgressBar resultBar, resultBar2, circleBar, crossBar;

    Thread opThread;
    Thread refreshThread;
    Thread uIPutThread;

    CopyOnWriteArrayList movesO = new CopyOnWriteArrayList();//LinkedList();
    CopyOnWriteArrayList movesX = new CopyOnWriteArrayList();//LinkedList();


    Thread musicPlayerThread;
    MusicPlayer musicPlayer;



    class UIPut implements Runnable {

        public void run() {
            //synchronized (table) {
            System.out.println("ISCRTAVAMMM::: " + System.currentTimeMillis());


            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    if (table.publicGet(i, j) == State.circle) {
                        if ((movesO.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (movesO.get(TableConfig.MAX_PIECES - 2).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 0.33f);
                        }
                        else if ((movesO.size() >= (TableConfig.MAX_PIECES-2)) &&
                                   (movesO.get(TableConfig.MAX_PIECES - 3).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 0.65f);
                        }
                        else {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 1f);
                        }
                    }
                    if (table.publicGet(i, j) == State.cross){
                        if ((movesX.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (movesX.get(TableConfig.MAX_PIECES - 2).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 0.33f);
                        }
                        else if ((movesX.size() >= (TableConfig.MAX_PIECES-2)) &&
                                (movesX.get(TableConfig.MAX_PIECES - 3).equals(new Coordinates(i,j)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 0.65f);
                        }
                        else {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 1f);
                        }

                    }
                    if (table.publicGet(i, j) == State.empty) {

                        tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin41, 1f);

                        movesO.remove(new Coordinates(i, j));
                        movesX.remove(new Coordinates(i, j));
                    }

                    if (table.publicGet(i, j) == State.rock) {
                        tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, R.drawable.pin20, 1f);
                    }
                }
            }
            //tableView.invalidate();


            if (lastMoveO != null) {
                double r = 0;
                r = table.end2(lastMoveO.x, lastMoveO.y, lastEventTime);
                result += r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesO.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesO.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        result -= 3 * TableConfig.RESULT_FACTOR;
                    }
                } else {
                    lastEventTime = System.currentTimeMillis();
                }
                lastMoveO = null;

            }

            if (lastMoveX != null) {
                double r = 0;
                r = table.end2(lastMoveX.x, lastMoveX.y, lastEventTime);
                result -= r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesX.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesX.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        result += 3 * TableConfig.RESULT_FACTOR;
                    } else {
                        lastEventTime = System.currentTimeMillis();
                    }
                }
                lastMoveX = null;
            }



            currentTime = System.currentTimeMillis();

            if ((result<=0) ||
                    (result >=100) /*||
                    ((currentTime - gameStartTime) >= TableConfig.GAME_DURATION)*/){

                gameDone = true;
                AlertDialog alertDialog = new AlertDialog.Builder(GamePlayActivity.this).create();
                alertDialog.setTitle("Alert");
                if(result>=50) {
                    alertDialog.setMessage("YOU WIN");
                } else {
                    alertDialog.setMessage("YOU LOSE");
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



            //currentTime = System.currentTimeMillis();
            waitingTimeCircle = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)/50 * Math.abs(50 - (int)result);
            waitingTimeCircle -= TableConfig.MIN_WAITING_TIME;
            waitingTimeCircle = (long)((double)waitingTimeCircle/ (1 + (double)(currentTime-gameStartTime)/(double)TableConfig.HALF_LIFE));
            waitingTimeCircle += TableConfig.MIN_WAITING_TIME;
            //waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;
            //musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCircle));

            //currentTime = System.currentTimeMillis();
            waitingTimeCross = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)/50 * Math.abs(50 - (int)result);
            waitingTimeCross -= TableConfig.MIN_WAITING_TIME;
            waitingTimeCross = (long)((double)waitingTimeCross/ (1 + (double)(currentTime-gameStartTime)/(double)TableConfig.HALF_LIFE));
            waitingTimeCross += TableConfig.MIN_WAITING_TIME;
            //waitingMomentCross = System.currentTimeMillis() + waitingTimeCross;

            /*System.out.println((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME + TableConfig.MIN_WAITING_TIME*3/4));
            System.out.println((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME + TableConfig.MIN_WAITING_TIME*6/16));
            System.out.println((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME + TableConfig.MIN_WAITING_TIME/4));
            System.out.println(waitingTimeCross);*/


            musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)*10/16 + TableConfig.MIN_WAITING_TIME)) {// *12/16
                musicPlayer.setMeasure(3);
                //System.out.println("TRI");
            } else if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)*4/16 + TableConfig.MIN_WAITING_TIME)) {// 8/16
                musicPlayer.setMeasure(4);
                //System.out.println("CETIRI");
            } else if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)*1.6/16 + TableConfig.MIN_WAITING_TIME)) {// 4/16
                musicPlayer.setMeasure(2);
                //System.out.println("DVA");
            } else {
                musicPlayer.setMeasure(5);
                //System.out.println("PET");
            }



        }
    }

    private class TableViewRefreshing implements Runnable {
        public void run() {

            while (!gameDone) {
                LockSupport.parkNanos(70_000_000);
                /*try {
                    Thread.sleep(50);
                    //Thread.yield();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                uIPutThread = new Thread(uIPut);
                System.out.println(System.currentTimeMillis());
                runOnUiThread(uIPutThread);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);



        /*musicPlayer = new MusicPlayer();
        musicPlayerThread = new Thread(musicPlayer);
        musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
        musicPlayerThread.start();*/



        Intent intent = getIntent();
        level = intent.getIntExtra("LEVEL", 50);
        player1Image = intent.getIntExtra("PLAYER1_IMG", R.drawable.pin39);
        player2Image = intent.getIntExtra("PLAYER2_IMG", R.drawable.pin40);


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

        ToggleButton soundToggle = (ToggleButton) findViewById(R.id.toggle_sound_button);
        soundToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicPlayer = new MusicPlayer();
                    musicPlayerThread = new Thread(musicPlayer);
                    musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
                    musicPlayerThread.start();
                } else {
                    musicPlayer.mute();
                }
            }
        });
        soundToggle.setChecked(true);

        opThread = new Thread(otherPlayer);
        opThread.start();

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

        gameStartTime = System.currentTimeMillis();
        lastEventTime = gameStartTime;
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        gameDone = true;
        musicPlayer.mute();

        try {
            opThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        opThread = null;

        try {
            refreshThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        refreshThread = null;


        try {
            musicPlayerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        musicPlayerThread = null;

    }


    private class OtherPlayer implements Runnable {
        public void run() {


            while(!gameDone){
                try {
                    double a = Math.random();
                    a*= (0.7 + 0.42/50 * Math.abs(50 - (int)result));
                    Thread.sleep(waitingTimeCross +
                            (long)(a*( (TableConfig.THINKING_TIME_MIN_LEVEL - TableConfig.THINKING_TIME_MAX_LEVEL) * (101-level) / 100 + TableConfig.THINKING_TIME_MAX_LEVEL)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if ((System.currentTimeMillis() - lastEventTime) < 90){
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //ww.lock();
                synchronized (table) {

                    c = table.putAutomatic(State.cross);
                    lastMoveX = new Coordinates(c.x, c.y);

//                    currentTime = System.currentTimeMillis();
//                    waitingTimeCross = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME)/50 * Math.abs(50 - (int)result);
//                    waitingTimeCross -= TableConfig.MIN_WAITING_TIME;
//                    waitingTimeCross = (long)((double)waitingTimeCross/ (1 + (double)(currentTime-gameStartTime)/(double)TableConfig.HALF_LIFE));
//                    waitingTimeCross += TableConfig.MIN_WAITING_TIME;
                    waitingMomentCross = System.currentTimeMillis() + waitingTimeCross;
//
//                    musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
//                    if (waitingTimeCross > (TableConfig.MAX_WAITING_TIME + TableConfig.MIN_WAITING_TIME)/2) {
//                        musicPlayer.setMeasure(3);
//                        System.out.println("TRI");
//                    } else {
//                        musicPlayer.setMeasure(4);
//                        System.out.println("CETIRI");
//                    }

                    allowCross = false;

                    movesX.add(0, lastMoveX);
                }

            }

        }
    }

    public void onFieldSelected(int x,int y) {


        synchronized (table) {
            if(allowCircle) {
                if(this.table.publicPut(State.circle, x, y)) {
                    lastMoveO = new Coordinates(x, y);
                    waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;
                    allowCircle = false;
                    movesO.add(0, lastMoveO);
                }
            }

        }
    }
}
