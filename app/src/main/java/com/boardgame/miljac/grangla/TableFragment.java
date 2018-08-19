package com.boardgame.miljac.grangla;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.view.Display;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;



public class TableFragment extends Fragment implements OnTouchListener {
    OnFieldSelectedListener fieldSelectedListener;

    public TableView tableView;
    public int pinSize;

    public interface OnFieldSelectedListener {
        public void onFieldSelected(int x, int y);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            fieldSelectedListener = (OnFieldSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFieldSelectedListener");
        }

        Display d = getActivity().getWindowManager().getDefaultDisplay();
        pinSize = d.getWidth() / TableConfig.TABLE_SIZE;
        pinSize = ((d.getHeight() / TableConfig.TABLE_SIZE) < pinSize) ? (d.getHeight() / TableConfig.TABLE_SIZE) : pinSize;

        if((d.getWidth() < d.getHeight()*1.3) &&
                (d.getHeight() < d.getWidth()*1.3)) {
            pinSize = pinSize * 85 / 100 * 85 / 100;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_table, container, true);
        tableView = (TableView) v.findViewById(R.id.Table);
        tableView.setOnTouchListener(this);
        tableView.disposePins();

        this.setRetainInstance(true);

        return v;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX() ;
        int y = (int) event.getY() ;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            fieldSelectedListener.onFieldSelected(tableView.getColumn(x),tableView.getRow(y));
        }

        return false;
    }
}
