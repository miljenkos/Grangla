package com.example.miljac.myapplication;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;



public class TableView extends ViewGroup  {

    private int parentWidth;
    private int parentHeight;
    private int dotSize;
    private int currentColor;
    private int numRow;
    private int numCol;

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




    public int[] disposePins(int dotSize) {
        this.dotSize = dotSize;

        numRow = TableConfig.TABLE_SIZE;// height
        numCol =  TableConfig.TABLE_SIZE;//width


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

            int left = pinImg.getCol() * dotSize;
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

        FieldImageView pinImg = (FieldImageView) getChildAt(x + (y*numCol));

        if (pinImg != null) {
            pinImg.setPinColor(color);
            pinImg.invalidate();

        }
    }



    public int getRow(int y) {
        return (int) Math.ceil(y / dotSize);
    }

    public int getColumn(int x) {
        return (int) Math.ceil( x / dotSize);
    }

}