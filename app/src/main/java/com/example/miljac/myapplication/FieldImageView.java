package com.example.miljac.myapplication;

//import com.survivingwithandroid.pegboard.R;
//import com.survivingwithandroid.pegboard.TableConfig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.util.Random;


public class FieldImageView extends ImageView  {

    private int row;
    private int col;
    private int xSize;
    private int ySize;
    private int stato = -1;
    private Context ctx;
    private int currentPinId;

    public FieldImageView(Context context, int row, int col) {
        super(context);
        this.ctx = context;
        this.row = row;
        this.col = col;
        //this.parent = parent;

        // Load image
        Drawable d  = getResources().getDrawable(TableConfig.pinBackground);
        setImageDrawable(d);
        xSize = this.getWidth();
        ySize = this.getHeight();
        this.currentPinId = TableConfig.pinBackground;

    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getxSize() {
        return xSize;
    }

    public void setxSize(int xSize) {
        this.xSize = xSize;
    }

    public int getySize() {
        return ySize;
    }

    public void setySize(int ySize) {
        this.ySize = ySize;
    }


    public void setPinColor(int resId) {

        this.currentPinId = resId;
        final Drawable d = getResources().getDrawable(resId);



        /*Random r = new Random();
        int i1 = r.nextInt(4);
        Log.i("AAA", Integer.toString(i1));

        Bitmap bmpOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.pin39);
        Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);
        tempCanvas.rotate(90*i1, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);
        tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);

        //mImageView.setImageBitmap(bmResult);
        d.draw(tempCanvas);*/
        
        Log.d("PROBA", "PROBA");




        if (resId != R.drawable.pin39) {
            Animation fadeInAnim = TableConfig.getFadeInAnim(ctx);
            AnimView av = new AnimView(fadeInAnim, d);
            //setImageDrawable(d); KAD JE OVO ZAKOMENTIRANO TITRA KOD MICANJA ZNAKOVA S PLOCE
            //this.setVisibility(View.GONE);
            this.post(av);
        }
        else {
            Animation fadeInAnim = TableConfig.getFadeInAnim(ctx);
            AnimView av = new AnimView(fadeInAnim, d);
            this.post(av);
        }

        //this.invalidate();

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
                    //TableConfig.playSound(FieldImageView.this.ctx);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    setImageDrawable(d);
                    FieldImageView.this.setVisibility(View.VISIBLE);
                }
            });

            FieldImageView.this.startAnimation(anim);
        }

    }

    public void reset() {
        //Log.d("Pin", "Reset");
        stato = -1;
        Drawable d = getResources().getDrawable(TableConfig.pinBackground);
        setImageDrawable(d);
    }




    public int getStato() {
        return stato;
    }

    public void setStato(int stato) {
        this.stato = stato;
    }


}