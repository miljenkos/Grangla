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


    public synchronized void setPinColor(int resId) {

        if ((this.currentPinId == resId) && !removing) return;
        if (animRunning) return;


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



        if ((resId == TableConfig.pinBackground) && (this.lastPinId != TableConfig.pinRock)) {
            if (!removing){
                removing = true;
                Drawable lastD = getResources().getDrawable(this.lastPinId);
                setImageDrawable(lastD);


            } else {
                removing = false;
                this.lastPinId = resId;


                //if(!(getAlpha() == 0.3f)) {
                    animRunning = true;
                    Animation fadeInAnim = TableConfig.getFadeInAnim(ctx);
                    AnimView av = new AnimView(fadeInAnim, d);
                    this.post(av);
                    //this.
                //}
            }
        }
        else {
            /*Animation fadeInAnim = TableConfig.getFadeInAnim(ctx);
            AnimView av = new AnimView(fadeInAnim, d);
            this.post(av);*/
            this.lastPinId = resId;
            setImageDrawable(d);
        }

        this.invalidate();

    }


    public synchronized void
    remove() {

        int resId = TableConfig.pinBackground;

        //System.out.println("AAAA");

        if (animRunning) return;


        this.currentPinId = resId;
        final Drawable d = getResources().getDrawable(resId);


        this.lastPinId = resId;
        setImageDrawable(d);

        this.invalidate();
        //System.out.println("BBBBb");

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
                    //FieldImageView.this.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    setImageDrawable(d);
                    animRunning = false;
                    //FieldImageView.this.setVisibility(View.VISIBLE);
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