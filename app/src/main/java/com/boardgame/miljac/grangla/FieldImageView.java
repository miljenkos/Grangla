package com.boardgame.miljac.grangla;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.widget.ImageView;



public class FieldImageView extends ImageView  {

    private int row;
    private int col;
    private int xSize;
    private int ySize;
    private int stato = -1;
    private Context ctx;
    private int currentPinId;
    private int lastPinId;
    private Boolean removing = false;
    private Boolean animRunning = false;

    public FieldImageView(Context context, int row, int col) {
        super(context);
        this.ctx = context;
        this.row = row;
        this.col = col;
        Drawable d  = getResources().getDrawable(TableConfig.pinBackground);
        setImageDrawable(d);
        xSize = this.getWidth();
        ySize = this.getHeight();
        this.currentPinId = TableConfig.pinBackground;

    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public synchronized void setPinColor(int resId) {

        if ((this.currentPinId == resId) && !removing) return;
        if (animRunning) return;


        this.currentPinId = resId;
        final Drawable d = getResources().getDrawable(resId);

        if ((resId == TableConfig.pinBackground) && (this.lastPinId != TableConfig.pinRock)) {
            if (!removing){
                removing = true;
                Drawable lastD = getResources().getDrawable(this.lastPinId);
                setImageDrawable(lastD);


            } else {
                removing = false;
                this.lastPinId = resId;
                animRunning = true;
                Animation fadeInAnim = TableConfig.getFadeInAnim(ctx);
                AnimView av = new AnimView(fadeInAnim, d);
                this.post(av);
            }
        }
        else {
            this.lastPinId = resId;
            setImageDrawable(d);
        }

        this.invalidate();

    }


    public synchronized void
    remove() {

        int resId = TableConfig.pinBackground;

        if (animRunning) return;


        this.currentPinId = resId;
        final Drawable d = getResources().getDrawable(resId);


        this.lastPinId = resId;
        setImageDrawable(d);

        this.invalidate();
    }


    class AnimView implements Runnable {

        Animation anim;
        Drawable d;

        public AnimView(Animation anim, Drawable d) {
            this.anim = anim;
            this.d = d;
        }
        @Override
        public void run() {
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    setImageDrawable(d);
                    animRunning = false;
                }
            });

            FieldImageView.this.startAnimation(anim);
        }

    }
}