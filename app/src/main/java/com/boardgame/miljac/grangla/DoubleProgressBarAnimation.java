package com.boardgame.miljac.grangla;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

public class DoubleProgressBarAnimation extends Animation {
    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private float from;
    private float  to;

    public DoubleProgressBarAnimation(ProgressBar progressBar1, ProgressBar progressBar2, float to) {
        super();
        this.progressBar1 = progressBar1;
        this.progressBar2 = progressBar2;
        this.from = progressBar1.getProgress();
        this.to = to;
        this.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        progressBar1.setProgress((int) value);
        progressBar2.setProgress(100 - (int) value);
    }

}