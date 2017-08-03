package com.example.miljac.myapplication;


import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class TableConfig {
    public static int TABLE_SIZE = 8;
    public static int MAX_WAITING_TIME = 2200;
    public static int MIN_WAITING_TIME = 750;
    public static int MAX_PIECES = 9;
    public static double RESULT_FACTOR = 1.2;
    public static int HALF_LIFE = 240000;
    public static int NO_OF_ROCKS = 9;
    public static int ROCK_MOVEMENT_PROBABILITY = 8000;
    public static int THINKING_TIME_MIN_LEVEL = 6400;
    public static int THINKING_TIME_MAX_LEVEL = 200;
    public static int DEFAULT_STREAM_REFRESH_TIME = 25;
    public static float NOTE_DURATION_FACTOR = 0.12F;


    public static double BASSS_TONES_DISPERSION = 0.6;
    public static int BASS_NOTE_LOWER_BOUNDARY = 23;
    public static int BASS_NOTE_UPPER_BOUNDARY = 35;
    public static int SOLO_NOTE_LOWER_BOUNDARY = 53;
    public static int SOLO_NOTE_UPPER_BOUNDARY = 80;


    private static MediaPlayer player ;

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static int[] pinIdList = new int[] {
           /* R.drawable.black_pin40,
            R.drawable.blue_pin40,
            R.drawable.brown_pin40,
            R.drawable.green_pin40,
            R.drawable.lightblue_pin40,
            R.drawable.orange_pin40,
            R.drawable.pink_pin40,
            R.drawable.red_pin40,
            R.drawable.yellow_pin40,
            R.drawable.violet_pin40,
            R.drawable.pin40*/
    };


    public static int pinBackground = R.drawable.pin41;
    public static int pinRock = R.drawable.pin20;




    public static Animation getFadeScaleOutAnim(Context ctx) {

        return AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.fadeout_scaleout);

    }

    public static Animation getFadeScaleInAnim(Context ctx) {

        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.fadein_scalein);

    }

    public static Animation getFadeInAnim(Context ctx) {

        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.fadein);

    }

    public static Animation getRotateAnim(Context ctx) {

        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.rotate);


    }



   /* public static void playSound(Context ctx) {
        if (player != null) {
            if (player.isPlaying()) {
                return ;
            }
        }
        else
            player = MediaPlayer.create(ctx, R.raw.pinsound);

        _play();
    }*/

    private static void _play() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                player.start();
            }
        });
        t.start();
    }


}