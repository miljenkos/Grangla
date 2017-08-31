package com.example.miljac.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.view.Display;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {link TableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link TableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableFragment extends Fragment implements OnTouchListener {
    OnFieldSelectedListener fieldSelectedListener;

    public TableView tableView;
    private int currentFieldDraw = R.drawable.pin41;
    public int pinSize;

    public interface OnFieldSelectedListener {
        public void onFieldSelected(int x, int y);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        try {
            fieldSelectedListener = (OnFieldSelectedListener) context;
            //System.out.println("\n\nmislim da sam se ataciral\n\n");
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFieldSelectedListener");
        }

        Display d = getActivity().getWindowManager().getDefaultDisplay();
        pinSize = d.getWidth() / TableConfig.TABLE_SIZE;
        pinSize = ((d.getHeight() / TableConfig.TABLE_SIZE) < pinSize) ? (d.getHeight() / TableConfig.TABLE_SIZE) : pinSize;
        pinSize = pinSize;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View v = inflater.inflate(R.layout.fragment_table, container, true);
        tableView = (TableView) v.findViewById(R.id.Table);
        tableView.setOnTouchListener(this);
        //pinSize = tableView.getHeight()/TableConfig.TABLE_SIZE;

        tableView.disposePins(pinSize);

        tableView.setCurrentColor(currentFieldDraw);


        this.setRetainInstance(true);

        return v;
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX() ;
        int y = (int) event.getY() ;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //Log.d("koordinate:",  "x:" + x + "  y:" + y + "\n");
            fieldSelectedListener.onFieldSelected(tableView.getColumn(x),tableView.getRow(y));
        }

        return false;

    }



}
