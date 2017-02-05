package com.example.miljac.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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


    private TableView tableView;
    private int currentFieldDraw = R.drawable.pin41;

    private int pinSize;


    private Table table;

    private OtherPlayer otherPlayer;

    private class OtherPlayer implements Runnable {
        public void run() {
            Log.d("AAAAAAAAAAAAAAAAAAAAA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
            /*while(true){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Coordinates c = table.putAutomatic(State.cross);
                tableView.changePinColor(c.x*pinSize +1, c.y*pinSize +1, R.drawable.pin40);
                tableView.invalidate();
                //postDelayed(tableView, DELAY_TIME_MILLIS);
            }*/

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        table = new Table(1);

        View v = inflater.inflate(R.layout.fragment_table, container, true);
        tableView = (TableView) v.findViewById(R.id.Table);
        tableView.setOnTouchListener(this);
        createBoard();

        tableView.setCurrentColor(currentFieldDraw);
        Log.println(Log.DEBUG, "tag", "string");
        this.setRetainInstance(true);
        return v;
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX() ;
        int y = (int) event.getY() ;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tableView.changePinColor(x, y, R.drawable.pin39);
            Log.d("koordinate:",  "x:" + x + "  y:" + y + "\n");
            table.put(State.circle, tableView.getColumn(x), tableView.getRow(y));
            tableView.invalidate();


            /*try{
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                Log.d("nesto", "nest");
            }*/

            Thread opThread = new Thread(otherPlayer);
            opThread.start();
            System.out.println(opThread.getState().toString());
            //opThread.notify();
            System.out.println(opThread.getStackTrace());
            System.out.println("A SAD ?!?!?!?");
            return true;
        }
        /*else if (event.getAction() == MotionEvent.ACTION_UP) {
            Coordinates c = table.putAutomatic(State.cross);
            tableView.changePinColor(c.x*pinSize +1, c.y*pinSize +1, R.drawable.pin40);
            tableView.invalidate();
            return true;
        }*/

        return false;

    }




    private void createBoard() {
        // We start defining the table grid
        Display d = getActivity().getWindowManager().getDefaultDisplay();

        //int pinSize = (int) (TableConfig.convertDpToPixel(TableConfig.DEFAULT_PIN_SIZE, getActivity()));
        pinSize = d.getWidth() / TableConfig.TABLE_SIZE;
        pinSize = ((d.getHeight() / TableConfig.TABLE_SIZE) < pinSize) ? (d.getHeight() / TableConfig.TABLE_SIZE) : pinSize;

        int[] vals = tableView.disposePins(d.getWidth(), d.getHeight(), pinSize);

        tableView.setTable(table);
        tableView.setPinSize(pinSize);



    }



}
