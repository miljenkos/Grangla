package com.boardgame.miljac.grangla;


import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class TableConfig {
    public static int TABLE_SIZE = 6;
    public static int MAX_WAITING_TIME = 2200;
    public static int MIN_WAITING_TIME = 750;
    public static int MAX_PIECES = 9;
    public static double RESULT_FACTOR = 1.2;
    public static int HALF_LIFE = 240000;
    public static int NO_OF_ROCKS = 6;
    public static int ROCK_MOVEMENT_PROBABILITY = 10000;//8000;
    public static int THINKING_TIME_MIN_LEVEL = 6400;
    public static int THINKING_TIME_MAX_LEVEL = 200;
    public static float NOTE_DURATION_FACTOR = 0.12F;


    public static double BASSS_TONES_DISPERSION = 0.6;
    public static int BASS_NOTE_LOWER_BOUNDARY = 23;
    public static int BASS_NOTE_UPPER_BOUNDARY = 35;
    public static int SOLO_NOTE_LOWER_BOUNDARY = 53;
    public static int SOLO_NOTE_UPPER_BOUNDARY = 80;

    public static int OKO_COLOR = 0xFF5BE1CA;
    public static int GUMB_COLOR = 0xFFEB68C9 ;
    public static int DJETELINA_COLOR = 0xFFA4F80F;
    public static int ZVIJEZDA_COLOR = 0xFFED9D1D;

    public static int OKO_COLOR_DESATURATED = 0xFF9e9e9e;
    public static int GUMB_COLOR_DESATURATED = 0xFF9a9a9a;
    public static int DJETELINA_COLOR_DESATURATED = 0xFF838383;
    public static int ZVIJEZDA_COLOR_DESATURATED = 0xFF858585;

    public static int pinBackground = R.drawable.pin41;
    public static int pinRock = R.drawable.pin20;


    public static Animation getFadeInAnim(Context ctx) {
        return  AnimationUtils.loadAnimation(ctx.getApplicationContext(), R.anim.fadein);
    }
}