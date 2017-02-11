package com.example.miljac.myapplication;
//import com.survivingwithandroid.pegboard.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Toast;



public class TableView extends ViewGroup  {

    private int parentWidth;
    private int parentHeight;
    private int dotSize;
    private int currentColor;
    private int numRow;
    private int numCol;
    private int pinSize;
    //private Table table;

    private Context context;



    public TableView(Context context) {
        super(context);
        this.context = context;

    }

    public TableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }



    public void setPinSize(int s){
        pinSize = s;
    }

    public int[] disposePins(int width, int height, int dotSize) {
        //Log.d("Pin", "Dispose pins. ["+width+"x" +height+  "]");
        this.dotSize = dotSize;

        numRow = TableConfig.TABLE_SIZE;// height / dotSize + 1;
        numCol =  TableConfig.TABLE_SIZE;//width / dotSize + 1;

        //Log.d("Pin", "Col x Row ["+numCol+"]x["+numRow+"]");


        //int[] dotColors = Pin.createDotArray(dotSize, true);

        for (int r=0; r < numRow ; r++) {
            for (int c=0; c < numCol; c++) {
                FieldImageView pinImg = new FieldImageView(getContext(), r, c);
                this.addView(pinImg);
            }
        }

        return new int[]{numRow, numCol};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        parentWidth  = MeasureSpec.getSize(widthMeasureSpec) ;
        parentHeight = MeasureSpec.getSize(heightMeasureSpec) ;

        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        int childCount = getChildCount();

        for (int i=0; i < childCount; i++) {
            FieldImageView pinImg = (FieldImageView) getChildAt(i);

            //int left = pinImg.getCol() * dotSize + dotSize * (pinImg.getType() == PinImageView.COLOR_COMMANDS || pinImg.getType() == PinImageView.DELETE ? 0 : 1);
            int left = pinImg.getCol() * dotSize;
            //int top = pinImg.getRow()  * dotSize + dotSize * (pinImg.getType() == PinImageView.COLOR_COMMANDS || pinImg.getType() == PinImageView.DELETE ? 0 : 1);
            int top = pinImg.getRow()  * dotSize;
            int right = left + dotSize ;
            int bottom = top + dotSize ;

            pinImg.layout(left, top, right, bottom);
        }

    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }



    public void changePinColor(int x, int y, int color) {
        int row = getRow(y);
        int col = getColumn(x);

        FieldImageView pinImg = (FieldImageView) getChildAt(col + ( row  * numCol ));
        //Log.d("Pin", "Is Reset ["+isReset+"]");

        if (pinImg != null) {
            pinImg.setPinColor(color);
            pinImg.invalidate();

        }
    }

    public int verify(int x, int y) {
        int x1 = x ;
        int y1 = y;

        int col = getColumn(x1);
        int row = getRow(y1);

        //Log.d("Pin", "X ["+x1+"] - Y ["+y1+"] : Row ["+row+"] - Col ["+col+"]");

        if (x1 < 0 || y1 < 0)
            return -1;


        // Calc board size
        int width = numCol * dotSize;
        int height = numRow * dotSize;

        if (x1 > width)
            return -1;

        if (y1 > height)
            return 0;

        return 1;

    }

    public int getRow(int y) {
        return (int) Math.ceil(y / dotSize);
    }

    public int getColumn(int x) {
        return (int) Math.ceil( x / dotSize);
    }



    public Bitmap createBitmap() {
        //Log.d("Pin", "Image W ["+this.getWidth()+"] x H ["+this.getHeight()+"]");
        Bitmap b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);

        this.draw(c);

        return b;
    }



}