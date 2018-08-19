package com.boardgame.miljac.grangla;
/***
 * this still needs a lot of refactoring
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MultiplayerActivity extends AppCompatActivity implements
        View.OnClickListener, TableFragment.OnFieldSelectedListener {
    Handler handler2 = null;
    Runnable hb = null;

    Spinner spinnerPlayer1;
    Spinner spinnerPlayer2;
    ListItem itemOko = new ListItem();
    ListItem itemGumb = new ListItem();
    ListItem itemDjetelina = new ListItem();
    ListItem itemZvijezda = new ListItem();


    private boolean isServer = true;

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     *
     * DEKLARACIJE IZ BATNKLIKERA
     */

    final static String TAG = "grangla777";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Holds the configuration of the current room.
    RoomConfig mRoomConfig;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];



    /*
    DEKLARACIJE IZ GEJMAKTIVITIJA
     */



    private boolean multiplayerGameStarted = false;



    private int currentApiVersion;
    private TableView tableView;
    //private MultiplayerGamePlayActivity.OtherPlayer otherPlayer = new MultiplayerGamePlayActivity.OtherPlayer();
    private MultiplayerActivity.TableViewRefreshing tableViewRefreshing = new MultiplayerActivity.TableViewRefreshing();
    private MultiplayerActivity.UIPut uIPut = new MultiplayerActivity.UIPut();
    private MultiplayerTable table = new MultiplayerTable(3);
    private TableFragment tableFragment;
    private Boolean gameDone = false;
    private Boolean gamePaused = false;
    private Boolean muted = false;
    private boolean firstTimeAnimatedProgress = true;
    private EndDialog endDialog;

    private Coordinates c;
    private Coordinates lastMoveO;
    private Coordinates lastMoveX;
    private double result = 50;
    private double myResult = 0;
    private double hisResult = 0;
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

    ResultBarAnimation animResult;


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

    private Coordinates lastXmultiplayer1;
    private Coordinates lastXmultiplayer2;
    private Coordinates lastRemovedO = null;




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
                        if (new Coordinates(i,j).equals(lastXmultiplayer1)) {
                            tableView.changePinColor(i/* * tableFragment.pinSize + 1*/, j/* * tableFragment.pinSize + 1*/, player2Image, 0.32f);
                        }
                        else if (new Coordinates(i,j).equals(lastXmultiplayer2)) {
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


            if ((lastMoveO != null) && (table != null) && (movesO != null)) {
                double r = 0;
                r = table.end2(lastMoveO.x, lastMoveO.y, lastEventTime);
                myResult += r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesO.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesO.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        lastRemovedO = c;
                        myResult -= 3 * TableConfig.RESULT_FACTOR;
                    }
                } else {
                    lastEventTime = System.currentTimeMillis();
                }
                lastMoveO = null;

            }

            if ((lastMoveX != null) && (table != null) && (movesX != null)) {
                double r = 0;
                r = table.end2(lastMoveX.x, lastMoveX.y, lastEventTime);
                //result -= r * TableConfig.RESULT_FACTOR;
                if (r == 0){
                    if(movesX.size() >= TableConfig.MAX_PIECES) {
                        Coordinates c = (Coordinates) movesX.remove(TableConfig.MAX_PIECES - 1);
                        table.publicEmpty(c.x, c.y);
                        tableView.removeImediately(c.x, c.y);
                        //result += 3 * TableConfig.RESULT_FACTOR;
                    } else {
                        lastEventTime = System.currentTimeMillis();
                    }
                }
                lastMoveX = null;
            }


            result = 50 + myResult - hisResult;


            currentTime = System.currentTimeMillis();

            if ((result <= 0) ||
                    (result  >= 100) /*||
                    ((currentTime - gameStartTime) >= TableConfig.GAME_DURATION)*/){

                gameDone = true;


                musicPlayer.setMeasure(2);
                musicPlayer.setEndSong();

                endDialog = new EndDialog(MultiplayerActivity.this, R.style.EndDialog);
                endDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                endDialog.setCanceledOnTouchOutside(false);

                endDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)// && hasFocus)
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

                handler2 = new Handler();
                hb = new Runnable() {
                    public void run() {
                        endDialog.dismiss();
                        saveInstanceState();
                        leaveRoom();//recreate();//finish();
                    }
                };

                handler2.postDelayed(hb, 9000);

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
                    animResult = new ResultBarAnimation(resultBar2, resultBar, (float) result);
                    animResult.setDuration(800);
                    resultBar.startAnimation(animResult);
                }

            if(firstTimeAnimatedProgress) {
                animResult = new ResultBarAnimation(resultBar, resultBar2, (float) result);
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
                TimerAnimation anim = new TimerAnimation(circleBar,
                        player1Color & 0xC5FFFFFF,
                        player1ColorDesaturated & 0x80FFFFFF);
                anim.setDuration(waitingMomentCircle - currentTime);
                circleBar.startAnimation(anim);
                startCircleTime = false;
            }

            if(startCrossTime) {
                TimerAnimation anim2 = new TimerAnimation(crossBar,
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

    public void exit(View v) {
        //endDialog.cancel();

        //saveSharedPreferences();
        //System.out.println("EXITEXITEXITEXIT\firstBassNote\firstBassNote");

        finish();


    }

    private class TableViewRefreshing implements Runnable {
        boolean doUiput = false;

        public void run() {

            while (!gameDone) {
                //LockSupport.parkNanos(70_000_000);
                LockSupport.parkNanos(30_000_000);

                if(doUiput) {
                    uIPutThread = new Thread(uIPut);
                    runOnUiThread(uIPutThread);
                    doUiput = false;
                } else {
                    doUiput = true;
                }

                sendTableInfo();
            }
        }
    }


    public void saveInstanceState(){
        if((soundToggle == null) ||
                (spinnerPlayer1 == null) ||
                (spinnerPlayer1 == null)
                ){
            return;
        }

        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean("mrm_SOUND", soundToggle.isChecked());
        ed.putInt("mrm_PLAYER_1_IMG_MLTPL",spinnerPlayer1.getSelectedItemPosition());
        ed.putInt("mrm_PLAYER_2_IMG_MLTPL",spinnerPlayer2.getSelectedItemPosition());
        ed.commit();        
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        saveInstanceState();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
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

        saveInstanceState();

        if (endDialog!=null)
            endDialog.dismiss();

        gameDone = true;
        musicPlayer.mute();


        try {
            if(refreshThread != null) {
                refreshThread.join();
            }
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


        // if we're in a room, leave it.
        //leaveRoom();

        // stop trying to keep the screen on


        //switchToMainScreen();

        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }

        //Process.killProcess(Process.myPid());

    }


    public void onFieldSelected(int x,int y) {


        synchronized (table) {
            if(allowCircle) {
                if(this.table.publicPut(State.circle, x, y)) {
                    lastMoveO = new Coordinates(x, y);
                    waitingMomentCircle = System.currentTimeMillis() + waitingTimeCircle;

                    startCircleTime = true;
                    /*TimerAnimation anim = new TimerAnimation(circleBar, 100, 0);
                    anim.setDuration(1000);
                    circleBar.startAnimation(anim);*/

                    allowCircle = false;
                    movesO.add(0, lastMoveO);
                }
            }

        }
    }



    /*
    FUNKCIJE SA STVARIMA IZ BATNKLIKERA I IZ GRANGLE
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        currentApiVersion = Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_multiplayer);

        //STVARI IZ MAINACTIVITY


        itemOko.setData("OKO", R.drawable.pin39);
        itemGumb.setData("GUMB",R.drawable.pin40);
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
                        return false;
                    }
                }));

        spinnerPlayer1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
        });

        spinnerPlayer2 = (Spinner) findViewById(R.id.spinner_player2);
        final MySpinnerAdapter adapterSpinner2 = new MySpinnerAdapter(this, R.layout.row, new ArrayList<ListItem>());
        spinnerPlayer2.setAdapter(adapterSpinner2);



        if(mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        spinnerPlayer1.setSelection(mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0));



        //System.out.println("        get    mrm_PLAYER_1_IMG_MLTPL  " + mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0));

        ListItem selected2;
        selected2 = (ListItem) spinnerPlayer2.getSelectedItem();

        adapterSpinner2.clear();
        //System.out.println("SELECT ITEM: " + (mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0)));

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0)==0))) {
            adapterSpinner2.add(itemOko);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0)==1))) {
            adapterSpinner2.add(itemGumb);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0)==2))) {
            adapterSpinner2.add(itemDjetelina);
        }

        if (!((mPrefs.getInt("mrm_PLAYER_1_IMG_MLTPL", 0)==3))) {
            adapterSpinner2.add(itemZvijezda);
        }

        spinnerPlayer2.setSelection(mPrefs.getInt("mrm_PLAYER_2_IMG_MLTPL", 0));


        //System.out.println("        get    mrm_PLAYER_2_IMG_MLTPL  " + mPrefs.getInt("mrm_PLAYER_2_IMG_MLTPL", 0));






        spinnerPlayer1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

//                spinnerPlayer2.getse
                ListItem selected2;
                selected2 = (ListItem) spinnerPlayer2.getSelectedItem();

                adapterSpinner2.clear();
                //System.out.println("SELECT ITEM: " + arg2);

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

        /*if(savedInstanceState != null){
            spinnerPlayer1.setSelection(savedInstanceState.getInt("mrm_PLAYER_1_IMG_MLTPL"));
            spinnerPlayer2.setSelection(savedInstanceState.getInt("mrm_PLAYER_2_IMG_MLTPL"));
        }*/










        //BATNKLIKER
        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }

        //switchToMainScreen();


        //GRANGLA


        //switchToMainScreen();


        //signInSilently();

        if(!(mPrefs.getString("mrm_MULTPL_ACTIVITY_MESSAGE", "").equals(""))) {

            new AlertDialog.Builder(MultiplayerActivity.this)
                    .setMessage(mPrefs.getString("mrm_MULTPL_ACTIVITY_MESSAGE", ""))
                    .setNeutralButton(android.R.string.ok, null)
                    .show();

            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putString("mrm_MULTPL_ACTIVITY_MESSAGE", "");

            ed.commit();
        }



        if(!(mPrefs.getString("mrm_MULTPL_ACTIVITY_MESSAGE_RECREATED", "").equals("YES"))){
            startSignInIntent();
        } else {
            signInSilently();
        }

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("mrm_MULTPL_ACTIVITY_MESSAGE_RECREATED", "NO");
        ed.commit();
    }






    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        //signInSilently();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(musicPlayer == null){
            musicPlayer = new MusicPlayer();
        }

        if(!muted && !musicPlayer.isEndSong()) {
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            if (multiplayerGameStarted) {
                musicPlayerThread.start();
            }
        }

        gamePaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        /*if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }*/

        musicPlayer.mute();
        gamePaused = true;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }




    //DALJE SAMO BATNKLIKER
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.button_single_player:
            case R.id.button_single_player_2:
                // play a single-player game
                resetGameVars();
                startGame(false);
                break;*/
            /*case R.id.button_sign_in:
                // user wants to sign in
                // Check to see the developer who'soloNote running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                startSignInIntent();
                break;*/
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                signOut();
                finish();
                //switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                switchToScreen(R.id.screen_wait);

                // show list of invitable players
                mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 1).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_see_invitations:
                switchToScreen(R.id.screen_wait);

                // show list of pending invitations
                mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
            case R.id.button_click_me:
                // (gameplay) user clicked the "click me" button
                scoreOnePoint();
                break;
        }
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity'soloNote onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    public void signOut() {
        Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(MultiplayerActivity.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                //if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                //}

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int i) {
                                MultiplayerActivity.this.finish();
                                finish();
                            }
                        })
                        .show();
                //finish();//AKO NE USPIJE SIGN IN TREIZAC IZ ;ULTIPLEJER; NADAM SE DA JE TO JEDINI SLUCAJ KAD SE OVO DESAVA
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                startGame(true);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.getInvitationId());
        }
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invitationId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invitationId);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();

        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRealTimeMultiplayerClient.join(mRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");


        //OVO SELIM U ONDESTROJ JER OCU MOC PAuzirat igrui
        /*
        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToMainScreen();*/

        super.onStop();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game2) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {


        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mRoomConfig = null;
                        }
                    });
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }


        if (mPrefs == null) {
            mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("mrm_MULTPL_ACTIVITY_MESSAGE_RECREATED", "YES");
        ed.commit();

        recreate();
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    invitation.getInviter().getDisplayName() + " " +
                            getString(R.string.is_inviting_you));
            switchToScreen(mCurScreen); // This will show the invitation popup
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                switchToScreen(mCurScreen); // This will hide the invitation popup
            }
        }
    };

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private String mPlayerId;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(MultiplayerActivity.this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer().addOnSuccessListener(
                    new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();
                        }
                    }
            );
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(MultiplayerActivity.this, googleSignInAccount);
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d(TAG, "onConnected: connection hint has a room invite!");
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));

        switchToMainScreen();
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;

        switchToMainScreen();
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);
            Log.d(TAG, "MY ID" + mMyId);

            for (Participant p : mParticipants) {

                Log.d(TAG, " ID" + p.getParticipantId());
                if ((p.getParticipantId().compareTo(mMyId)) > 0) {
                    isServer = false;
                }

            }

            if(isServer) {
                Log.d(TAG, "JESAM SERVER");
            } else {
                Log.d(TAG, "NISAM SERVER");
            }

            table.setServer(isServer);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            Log.d(TAG, "onDisconnectedFromRoom");
            /*mRoomId = null;
            mRoomConfig = null;
            recreate();//leaveRoom();//showGameError();*/

            if (mPrefs == null) {
                mPrefs = getSharedPreferences("mrm", MODE_PRIVATE);
            }
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putString("mrm_MULTPL_ACTIVITY_MESSAGE_RECREATED", "YES");

            if(!gameDone) {
                ed.putString("mrm_MULTPL_ACTIVITY_MESSAGE", getString(R.string.opponent_left));
            }

            ed.commit();

            if(handler2 != null) {
                handler2.removeCallbacks(hb);
            }


            recreate();//leaveRoom();


        }



        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            Log.d(TAG, "onPeerDeclined");
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            Log.d(TAG, "onPeerInvitedToRoom");
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
            Log.d(TAG, "onP2PDisconnected");
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
            Log.d(TAG, "onPeerDeclined");
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            Log.d(TAG, "onPeerJoined");
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            Log.d(TAG, "onPeerLeft");
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            Log.d(TAG, "onPeersDisconnected");
            updateRoom(room);
        }
    };

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        ///BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        new AlertDialog.Builder(this)
                .setMessage(R.string.game_problem)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int i) {
                        MultiplayerActivity.this.finish();
                        finish();
                    }
                })
                .show();
        switchToMainScreen();
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onJoinedRoom, status " + statusCode);
                showGameError();
                return;
            }

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            switchToMainScreen();
        }
    };

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            updatePeerScoresDisplay();
        }
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game'soloNote rules.
     */

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    int mScore = 0; // user'soloNote current score

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        player1Image = ((ListItem) spinnerPlayer1.getSelectedItem()).logo;
        player2Image = ((ListItem) spinnerPlayer2.getSelectedItem()).logo;

        multiplayerGameStarted = true;

        /*Intent intent = new Intent(this, SinglePlayerGamePlayActivity.class);
        startActivity(intent);*/


        mMultiplayer = multiplayer;
        updateScoreDisplay();
        broadcastScore(false);
        switchToScreen(R.id.screen_game2);





        currentApiVersion = Build.VERSION.SDK_INT;
        //setContentView(R.layout.activity_game_play);



        this.table = new MultiplayerTable(level);
        for (Participant p : mParticipants) {

            Log.d(TAG, " ID" + p.getParticipantId());
            if ((p.getParticipantId().compareTo(mMyId)) > 0) {
                isServer = false;
            }

        }

        if(isServer) {
            Log.d(TAG, "JESAM SERVER");
        } else {
            Log.d(TAG, "NISAM SERVER");
        }

        table.setServer(isServer);


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




        if((mPrefs.getBoolean("mrm_SOUND", true))) {
            soundToggle.setChecked(true);
        } else {
            if(musicPlayer != null) {
                musicPlayer.mute();
            }
            musicPlayer = new MusicPlayer();
            musicPlayerThread = new Thread(musicPlayer);
            musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * waitingTimeCross));
            muted = true;
        }

        soundToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(musicPlayer != null) {
                        musicPlayer.mute();
                    }

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
                //saveSharedPreferences();
                leaveRoom();
                saveInstanceState();
                //finish();

            }
        });


        /*otherPlayerThread = new Thread(otherPlayer);
        otherPlayerThread.start();*/

        refreshThread = new Thread(tableViewRefreshing);
        refreshThread.start();

        gameStartTime = System.currentTimeMillis();
        lastEventTime = gameStartTime;






        findViewById(R.id.button_click_me).setVisibility(View.VISIBLE);

        // run the gameTick() method every second to update the game.
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSecondsLeft <= 0) {
                    return;
                }
                gameTick();
                h.postDelayed(this, 1000);
            }
        }, 1000);
    }

    // Game tick -- update countdown, check if game ended.
    void gameTick() {
        if (mSecondsLeft > 0) {
            --mSecondsLeft;
        }

        // update countdown
        ((TextView) findViewById(R.id.countdown)).setText("0:" +
                (mSecondsLeft < 10 ? "0" : "") + String.valueOf(mSecondsLeft));

        if (mSecondsLeft <= 0) {
            // finish game
            findViewById(R.id.button_click_me).setVisibility(View.GONE);
            broadcastScore(true);
        }
    }

    // indicates the player scored one point
    void scoreOnePoint() {
        if (mSecondsLeft <= 0) {
            return; // too late!
        }
        ++mScore;
        updateScoreDisplay();
        updatePeerScoresDisplay();

        // broadcast our new score to our peers
        broadcastScore(false);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game'soloNote network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it'soloNote a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();

            if(buf[0] == 77){
                myResult -= 50;
                return;
            }

            Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

            synchronized (table) {
                Coordinates c = table.applyMsgBuff(buf);
                if(c != null) {
                    lastMoveX = c;
                    waitingMomentCross = System.currentTimeMillis() + waitingTimeCross;
                    startCrossTime = true;

                    if (c.conflict) {
                        startCircleTime = false;
                        circleBar.clearAnimation();
                        circleBar.setProgress(100);
                        circleBar.getProgressDrawable().setColorFilter(
                                player1Color & 0xA0FFFFFF,
                                android.graphics.PorterDuff.Mode.SRC_IN);
                        waitingMomentCircle = System.currentTimeMillis();

                        movesO.remove(c);
                        if(movesO.size() >= (TableConfig.MAX_PIECES-1)) {
                            if (lastRemovedO != null) {
                                movesO.add(lastRemovedO);
                                table.put(State.circle, lastRemovedO.x, lastRemovedO.y);
                            }
                        }

                        //tu bi jos trebalo vratit kruzic ako je maknut jer je bio visak
                        //za sada ostavljam to kao mali bag
                    }
                }






            }


            hisResult = buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE] * 128 +
                    buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 1];

            if(buf[TableConfig.TABLE_SIZE*TableConfig.TABLE_SIZE + 2] == 30){
                lastXmultiplayer1 = null;
            } else {
                lastXmultiplayer1 = new Coordinates(buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 2],
                        buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 3]);
            }

            if(buf[TableConfig.TABLE_SIZE*TableConfig.TABLE_SIZE + 4] == 30){
                lastXmultiplayer2 = null;
            } else {
                lastXmultiplayer2 = new Coordinates(buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 4],
                        buf[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 5]);
            }


            /*if (buf[0] == 'F' || buf[0] == 'U') {
                // score update.
                int existingScore = mParticipantScore.containsKey(sender) ?
                        mParticipantScore.get(sender) : 0;
                int thisScore = (int) buf[1];
                if (thisScore > existingScore) {
                    // this check is necessary because packets may arrive out of
                    // order, so we
                    // should only ever consider the highest score we received, as
                    // we know in our
                    // game there is no way to lose points. If there was a way to
                    // lose points,
                    // we'd have to add a "serial number" to the packet.
                    mParticipantScore.put(sender, thisScore);
                }

                // update the scores on the screen
                updatePeerScoresDisplay();

                // if it'soloNote a final score, mark this participant as having finished
                // the game
                if ((char) buf[0] == 'F') {
                    mFinishedParticipants.add(realTimeMessage.getSenderParticipantId());
                }
            }*/
        }
    };


    void sendTableInfo(){
        byte[] msgBuff;

        if(gameDone && (result>70) ){
            msgBuff = new byte[1];
            msgBuff[0] = 77;

            // Send to every other participant.
            for (Participant p : mParticipants) {
                if (p.getParticipantId().equals(mMyId)) {
                    continue;
                }
                if (p.getStatus() != Participant.STATUS_JOINED) {
                    continue;
                }


                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient.sendReliableMessage(msgBuff,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "Pobijedio sam! i poslao poruku o tome");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Napravio sam poruku o svojoj pobjedi: " + tokenId);
                            }
                        });
            }


            return;
        }

        synchronized (table) {
            msgBuff = table.getMsgBuff();

            //if(msgBuff[0] == 98) leaveRoom();


            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE] = (byte) (((int) myResult) / 128);
            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 1] = (byte) ((int) myResult % 128);


            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 2] = 30;
            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 3] = 30;
            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 4] = 30;
            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 5] = 30;
            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    if (table.get(i, j) == State.circle) {
                        if ((movesO.size() >= (TableConfig.MAX_PIECES - 1)) &&
                                (new Coordinates(i, j).equals(movesO.get(TableConfig.MAX_PIECES - 2)))) {

                            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 2] = (byte) i;
                            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 3] = (byte) j;

                        } else if ((movesO.size() >= (TableConfig.MAX_PIECES - 2)) &&
                                (new Coordinates(i, j).equals(movesO.get(TableConfig.MAX_PIECES - 3)))) {

                            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 4] = (byte) i;
                            msgBuff[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 5] = (byte) j;

                        }
                    }
                }
            }
        }


        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            // it'soloNote an interim score notification, so we can use unreliable
            mRealTimeMultiplayerClient.sendUnreliableMessage(msgBuff, mRoomId,
                    p.getParticipantId());
        }

    }


    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer) {
            // playing single-player mode
            return;
        }

        // First byte in message indicates whether it'soloNote a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            /*if (finalScore) {
                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "RealTime message sent");
                                Log.d(TAG, "  statusCode: " + statusCode);
                                Log.d(TAG, "  tokenId: " + tokenId);
                                Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
                            }
                        });
            } else {
                // it'soloNote an interim score notification, so we can use unreliable
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        p.getParticipantId());
            }*/
        }
    }

    /*
     * UI SECTION. Methods that implement the game'soloNote UI.
     */

    // This array lists everything that'soloNote clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations,/* R.id.button_sign_in,*/
            R.id.button_sign_out, R.id.button_click_me, /*R.id.button_single_player,
            R.id.button_single_player_2*/
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game2, R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            System.out.println( Integer.toHexString(id));
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mRealTimeMultiplayerClient != null) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    // updates the label that shows my score
    void updateScoreDisplay() {
        ((TextView) findViewById(R.id.my_score)).setText(formatScore(mScore));
    }

    // formats a score as a three-digit number
    String formatScore(int i) {
        if (i < 0) {
            i = 0;
        }
        String s = String.valueOf(i);
        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
    }

    // updates the screen with the scores from our peers
    void updatePeerScoresDisplay() {
        ((TextView) findViewById(R.id.score0)).setText(
                getString(R.string.score_label, formatScore(mScore)));
        int[] arr = {
                R.id.score1, R.id.score2, R.id.score3
        };
        int i = 0;

        if (mRoomId != null) {
            for (Participant p : mParticipants) {
                String pid = p.getParticipantId();
                if (pid.equals(mMyId)) {
                    continue;
                }
                if (p.getStatus() != Participant.STATUS_JOINED) {
                    continue;
                }
                int score = mParticipantScore.containsKey(pid) ? mParticipantScore.get(pid) : 0;
                ((TextView) findViewById(arr[i])).setText(formatScore(score) + " - " +
                        p.getDisplayName());
                ++i;
            }
        }

        for (; i < arr.length; ++i) {
            ((TextView) findViewById(arr[i])).setText("");
        }
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It'soloNote recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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