package com.boardgame.miljac.grangla;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GamePlayActivity extends AppCompatActivity implements TableFragment.OnFieldSelectedListener {

    private int currentApiVersion;
    private TableView tableView;
    private OtherPlayer otherPlayer = new OtherPlayer();
    private TableViewRefreshing tableViewRefreshing = new TableViewRefreshing();
    private UIPut uIPut = new UIPut();
    private Table table = new Table(3);
    private TableFragment tableFragment;
    private Boolean gameDone = false;
    private Boolean gamePaused = false;
    private Boolean muted = false;
    private boolean firstTimeAnimatedProgress = true;
    private FullscreenDialog endDialog;

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
    private int player1Color;
    private int player2Color;
    private int player1ColorDesaturated;
    private int player2ColorDesaturated;
    private SharedPreferences mPrefs;
    private boolean startCircleTime = false;
    private boolean startCrossTime = false;

    ToggleButton soundToggle;
    ImageButton imageButton;
    ImageButton endImageButton;

    DoubleProgressBarAnimation animResult;


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
            //System.out.println("ISCRTAVAMMM::: " + System.currentTimeMillis());


            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    if (table.publicGet(i, j) == State.circle) {
                        if ((movesO.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (new Coordinates(i,j).equals(movesO.get(TableConfig.MAX_PIECES - 2)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 0.32f);
                        }
                        else if ((movesO.size() >= (TableConfig.MAX_PIECES-2)) &&
                                   (new Coordinates(i,j).equals(movesO.get(TableConfig.MAX_PIECES - 3)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 0.6f);
                        }
                        else {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player1Image, 1f);
                        }
                    }
                    if (table.publicGet(i, j) == State.cross){
                        if ((movesX.size() >= (TableConfig.MAX_PIECES-1)) &&
                                (new Coordinates(i,j).equals(movesX.get(TableConfig.MAX_PIECES - 2)))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 0.32f);
                        }
                        else if ((movesX.size() >= (TableConfig.MAX_PIECES-2)) &&
                                (new Coordinates(i,j)).equals(movesX.get(TableConfig.MAX_PIECES - 3))) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 0.6f);
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


                musicPlayer.setMeasure(2);
                musicPlayer.setEndSong();

                endDialog = new FullscreenDialog(GamePlayActivity.this, R.style.EndDialog);
                endDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                endDialog.setCanceledOnTouchOutside(false);

                endDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)// && hasFocus)
                {
                    endDialog.getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }




                if(result<=0){
                    if(player2Image == R.drawable.pin39) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_oko, null));
                    }
                    if(player2Image == R.drawable.pin40) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_gumb, null));
                    }
                    if(player2Image == R.drawable.pin42) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_djetelina, null));
                    }
                    if(player2Image == R.drawable.pin43) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_zvijezda, null));
                    }
                } else {
                    if(player1Image == R.drawable.pin39) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_oko, null));
                    }
                    if(player1Image == R.drawable.pin40) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_gumb, null));
                    }
                    if(player1Image == R.drawable.pin42) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_djetelina, null));
                    }
                    if(player1Image == R.drawable.pin43) {
                        endDialog.setContentView(getLayoutInflater().inflate(R.layout.end_dialog_zvijezda, null));
                    }
                }


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        endDialog.show();
                    }
                }, 300);

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        endDialog.dismiss();
                        finish();
                    }
                }, 9000);

                /*Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {
                    public void run() {
                        finish();
                    }
                }, 7500);*/


                //endDialog.show();

                /*endImageButton = (ImageButton) findViewById(R.id.endButton);
                endImageButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        //dialog.dismiss();
                        saveSharedPreferences();
                        finish();

                    }
                });*/




            }


            /*resultBar.setProgress(100-(int)result);
            resultBar2.setProgress(0);//((int)result);*/




            if(!firstTimeAnimatedProgress)
                if ((animResult.hasEnded()) && (resultBar2.getProgress() != (int)result)) {
                    animResult = new DoubleProgressBarAnimation(resultBar2, resultBar, (float) result);
                    animResult.setDuration(800);
                    resultBar.startAnimation(animResult);
                }

            if(firstTimeAnimatedProgress) {
                animResult = new DoubleProgressBarAnimation(resultBar, resultBar2, (float) result);
                animResult.setDuration(100);
                resultBar.startAnimation(animResult);

                firstTimeAnimatedProgress = false;
            }



            if(currentTime < waitingMomentCircle){
                //circleBar.setProgress((int)((waitingMomentCircle - currentTime) * 100 / waitingTimeCircle ));

            } else {
                allowCircle = true;
                //
                // circleBar.setProgress(0);
            }

            if(currentTime < waitingMomentCross){
                //crossBar.setProgress((int)((waitingMomentCross - currentTime ) * 100 / waitingTimeCross ));

            } else {
                allowCross = true;
                //crossBar.setProgress(0);
            }

            if(startCircleTime) {
                ProgressBarAnimation anim = new ProgressBarAnimation(circleBar,
                        player1Color & 0xC5FFFFFF,
                        player1ColorDesaturated & 0x80FFFFFF);
                anim.setDuration(waitingMomentCircle - currentTime);
                circleBar.startAnimation(anim);
                startCircleTime = false;
            }

            if(startCrossTime) {
                ProgressBarAnimation anim2 = new ProgressBarAnimation(crossBar,
                        player2Color & 0xC5FFFFFF,
                        player2ColorDesaturated & 0x80FFFFFF);
                anim2.setDuration(waitingMomentCross - currentTime);
                crossBar.startAnimation(anim2);
                startCrossTime = false;
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



            if(!musicPlayer.isEndSong()) {
                musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));

                if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * 10 / 16 + TableConfig.MIN_WAITING_TIME)) {// *12/16
                    musicPlayer.setMeasure(3);
                    //System.out.println("TRI");
                } else if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * 4 / 16 + TableConfig.MIN_WAITING_TIME)) {// 8/16
                    musicPlayer.setMeasure(4);
                    //System.out.println("CETIRI");
                } else if (waitingTimeCross > ((TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) * 1.6 / 16 + TableConfig.MIN_WAITING_TIME)) {// 4/16
                    musicPlayer.setMeasure(2);
                    //System.out.println("DVA");
                } else {
                    musicPlayer.setMeasure(5);
                    //System.out.println("PET");
                }
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
                //System.out.println(System.currentTimeMillis());
                runOnUiThread(uIPutThread);

            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        musicPlayer.mute();
        gamePaused = true;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(musicPlayer == null){
            musicPlayer = new MusicPlayer();
        }

        if(!muted && !musicPlayer.isEndSong()) {
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            musicPlayerThread.start();
        }

        gamePaused = false;
    }


    private void saveSharedPreferences(){
        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean("mrm_SOUND", soundToggle.isChecked());
        ed.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        saveSharedPreferences();

        super.onSaveInstanceState(outState);
        //finish();
    }

    @Override
    public void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
        //System.out.println("  JAHA JAHAJA!");

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_game_play);

        Intent intent = getIntent();
        level = intent.getIntExtra("LEVEL", 50);
        player1Image = intent.getIntExtra("PLAYER1_IMG", R.drawable.pin39);
        player2Image = intent.getIntExtra("PLAYER2_IMG", R.drawable.pin40);


        this.table = new Table(level);


        if(player1Image == R.drawable.pin39) {
            player1Color = TableConfig.OKO_COLOR;
            player1ColorDesaturated = TableConfig.OKO_COLOR_DESATURATED;
        }
        if(player1Image == R.drawable.pin40) {
            player1Color = TableConfig.GUMB_COLOR;
            player1ColorDesaturated = TableConfig.GUMB_COLOR_DESATURATED;
        }
        if(player1Image == R.drawable.pin42) {
            player1Color = TableConfig.DJETELINA_COLOR;
            player1ColorDesaturated = TableConfig.DJETELINA_COLOR_DESATURATED;
        }
        if(player1Image == R.drawable.pin43) {
            player1Color = TableConfig.ZVIJEZDA_COLOR;
            player1ColorDesaturated = TableConfig.ZVIJEZDA_COLOR_DESATURATED;
        }

        if(player2Image == R.drawable.pin39) {
            player2Color = TableConfig.OKO_COLOR;
            player2ColorDesaturated = TableConfig.OKO_COLOR_DESATURATED;
        }
        if(player2Image == R.drawable.pin40) {
            player2Color = TableConfig.GUMB_COLOR;
            player2ColorDesaturated = TableConfig.GUMB_COLOR_DESATURATED;
        }
        if(player2Image == R.drawable.pin42) {
            player2Color = TableConfig.DJETELINA_COLOR;
            player2ColorDesaturated = TableConfig.DJETELINA_COLOR_DESATURATED;
        }
        if(player2Image == R.drawable.pin43) {
            player2Color = TableConfig.ZVIJEZDA_COLOR;
            player2ColorDesaturated = TableConfig.ZVIJEZDA_COLOR_DESATURATED;
        }



        resultBar = (ProgressBar)findViewById(R.id.result_bar);
        resultBar.setProgress(50);
        resultBar.getProgressDrawable().setColorFilter(player2Color,
                android.graphics.PorterDuff.Mode.SRC_IN);
        resultBar2 = (ProgressBar)findViewById(R.id.result_bar2);
        resultBar2.setProgress(50);
        resultBar2.getProgressDrawable().setColorFilter(player1Color,
                android.graphics.PorterDuff.Mode.SRC_IN);



        circleBar = (ProgressBar)findViewById(R.id.circle_time_bar);
        circleBar.setProgress(100);
        circleBar.invalidate();
        circleBar.getProgressDrawable().setColorFilter(
                player1Color & 0xA0FFFFFF,
                android.graphics.PorterDuff.Mode.SRC_IN);
        crossBar = (ProgressBar)findViewById(R.id.cross_time_bar);
        crossBar.setProgress(100);
        crossBar.invalidate();
        crossBar.getProgressDrawable().setColorFilter(
                player2Color & 0xA0FFFFFF,
                android.graphics.PorterDuff.Mode.SRC_IN);

        tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = tableFragment.tableView;


        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }

        soundToggle = (ToggleButton) findViewById(R.id.toggle_sound_button);

        /*ViewGroup.LayoutParams params = soundToggle.getLayoutParams();
        params.width = 200;
        soundToggle.setLayoutParams(params);*/

        /*ImageSpan imageSpan = new ImageSpan(this, R.drawable.sound_on);
        SpannableString content = new SpannableString("X");
        content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        soundToggle.setText(content);
        soundToggle.setTextOn(content);
        soundToggle.setTextOff(content);*/




        if((mPrefs.getBoolean("mrm_SOUND", true))) {
            soundToggle.setChecked(true);
        } else {
            //musicPlayer.mute();
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            muted = true;
        }

        soundToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicPlayer = new MusicPlayer();
                    musicPlayerThread = new Thread(musicPlayer);
                    musicPlayer.setNoteDuration((long)(TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
                    musicPlayerThread.start();
                    muted = false;
                } else {
                    musicPlayer.mute();
                    muted = true;
                }
            }
        });


        imageButton = (ImageButton) findViewById(R.id.discard_button);

        imageButton.setOnClickListener(new View.OnClickListener() {

                                           @Override
                                           public void onClick(View arg0) {
                                               saveSharedPreferences();
                                               finish();

                                           }
                                       });


        opThread = new Thread(otherPlayer);
        opThread.start();

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

        gameStartTime = System.currentTimeMillis();
        lastEventTime = gameStartTime;
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)// && hasFocus)
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

    @Override
    protected void onDestroy(){
        super.onDestroy();

        saveSharedPreferences();

        if (endDialog!=null)
            endDialog.dismiss();

        gameDone = true;
        musicPlayer.mute();

        try {
            opThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        opThread = null;

        try {
            refreshThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        refreshThread = null;


        try {
            musicPlayerThread.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
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
                    //e.printStackTrace();
                }

                if ((System.currentTimeMillis() - lastEventTime) < 90){
                    try {
                        Thread.sleep(90);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }

                if(gamePaused) continue;


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

                    startCrossTime = true;

                    /*ProgressBarAnimation anim = new ProgressBarAnimation(crossBar, 100, 0);
                    anim.setDuration(1000);
                    crossBar.startAnimation(anim);*/

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

                    startCircleTime = true;
                    /*ProgressBarAnimation anim = new ProgressBarAnimation(circleBar, 100, 0);
                    anim.setDuration(1000);
                    circleBar.startAnimation(anim);*/

                    allowCircle = false;
                    movesO.add(0, lastMoveO);
                }
            }

        }
    }



}
