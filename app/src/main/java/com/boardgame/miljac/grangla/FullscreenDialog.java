package com.boardgame.miljac.grangla;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;



public class FullscreenDialog extends Dialog {
    public FullscreenDialog(Context context, int i)
    {
        super(context, i);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)// && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }*/



        super.onCreate(savedInstanceState);
    }


}
