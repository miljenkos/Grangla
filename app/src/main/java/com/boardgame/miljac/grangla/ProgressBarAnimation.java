package com.boardgame.miljac.grangla;

import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float  to;
    private int from1;
    private int  to1;
    private int from2;
    private int  to2;
    private int from3;
    private int  to3;
    private int from4;
    private int  to4;

    public ProgressBarAnimation(ProgressBar progressBar,
                                int fromColor, int toColor) {
        super();
        this.progressBar = progressBar;
        this.from = 100;
        this.to = 0;

        this.from1 = fromColor % 256;
        this.to1 = toColor % 256;

        this.from2 = (fromColor/256) % 256;
        this.to2 = (toColor/256) % 256;

        this.from3 = (fromColor/256/256) % 256;
        this.to3 = (toColor/256/256) % 256;

        this.from4 = (fromColor/256/256/256) % 256;
        this.to4 = (toColor/256/256/256) % 256;

        progressBar.getProgressDrawable().setColorFilter(fromColor, android.graphics.PorterDuff.Mode.SRC_IN);

        this.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        float value = from + (to - from) * interpolatedTime;
        value = 100 - value;
        progressBar.setProgress((int) value);

        if(value < 65) {
            //progressBar.getProgressDrawable().setAlpha(30 + 225 * (int) value / 65);

            int value1 = from1 + (to1 - from1) * (int)(65-value)/65;
            int value2 = from2 + (to2 - from2) * (int)(65-value)/65;
            int value3 = from3 + (to3 - from3) * (int)(65-value)/65;
            int value4 = from4 + (to4 - from4) * (int)(65-value)/65;
            int color = value4 *256*256*256+
                        value3 *256*256 +
                        value2 *256 +
                        value1;

            progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.getProgressDrawable().setAlpha(255-65+(int)value);

        } else if (value > 99) {
            progressBar.getProgressDrawable().setAlpha(255);
        }


    }

}