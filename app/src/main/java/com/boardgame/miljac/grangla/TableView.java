package com.boardgame.miljac.grangla;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;



public class TableView extends ViewGroup  {
    private int dotSize;
    private int numRow;
    private int numCol;

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int[] disposePins() {
        numRow = TableConfig.TABLE_SIZE;
        numCol =  TableConfig.TABLE_SIZE;

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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = 0;
        int width =  MeasureSpec.getSize(widthMeasureSpec) ;
        int height = MeasureSpec.getSize(heightMeasureSpec) ;

        if (width > height) {
            size = height;
        } else {
            size = width;
        }

        if((width < height*1.3) &&
                (height < width*1.3)) {
            size = size*85/100;
        }
        setMeasuredDimension(size, size);

        dotSize = size / TableConfig.TABLE_SIZE;
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        int childCount = getChildCount();

        for (int i=0; i < childCount; i++) {
            FieldImageView pinImg = (FieldImageView) getChildAt(i);

            int left = pinImg.getCol() * dotSize;
            int top = pinImg.getRow()  * dotSize;
            int right = left + dotSize ;
            int bottom = top + dotSize ;

            pinImg.layout(left, top, right, bottom);
        }

    }

    public void  changePinColor(int x, int y, int color, float alpha) {
        FieldImageView pinImg = (FieldImageView) getChildAt(x + (y*numCol));

        if (pinImg != null) {
            pinImg.setPinColor(color);
            pinImg.setAlpha(alpha);
        }
    }

    public void removeImediately(int x, int y) {
        FieldImageView pinImg = (FieldImageView) getChildAt(x + (y*numCol));

        if (pinImg != null) {
            pinImg.remove();
        }
    }

    public int getRow(int y) {
        return (int) Math.ceil(y / dotSize);
    }

    public int getColumn(int x) {
        return (int) Math.ceil( x / dotSize);
    }

}