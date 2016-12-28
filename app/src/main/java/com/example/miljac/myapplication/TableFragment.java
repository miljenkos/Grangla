package com.example.miljac.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link TableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment implements OnTouchListener {


    private TableView table;
    private int currentFieldDraw = R.drawable.pin40;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View v = inflater.inflate(R.layout.fragment_table, container, true);
        table = (TableView) v.findViewById(R.id.Table);
        table.setOnTouchListener(this);
        createBoard();

        table.setCurrentColor(currentFieldDraw);
        Log.println(Log.DEBUG, "tag", "string");
        this.setRetainInstance(true);
        return v;
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX() ;
        int y = (int) event.getY() ;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            table.changePinColor(x, y, R.drawable.pin41);
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            table.changePinColor(x, y, R.drawable.pin41);
            return true;
        }

        return false;

    }




    private void createBoard() {
        // We start defining the table grid
        Display d = getActivity().getWindowManager().getDefaultDisplay();

        //int pinSize = (int) (TableConfig.convertDpToPixel(TableConfig.DEFAULT_PIN_SIZE, getActivity()));
        int pinSize = d.getWidth() / TableConfig.TABLE_SIZE;
        pinSize = ((d.getHeight() / TableConfig.TABLE_SIZE) < pinSize) ? (d.getHeight() / TableConfig.TABLE_SIZE) : pinSize;

        int[] vals = table.disposePins(d.getWidth(), d.getHeight(), pinSize);

    }



}
