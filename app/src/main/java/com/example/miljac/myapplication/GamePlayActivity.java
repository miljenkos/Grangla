package com.example.miljac.myapplication;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class GamePlayActivity extends AppCompatActivity implements TableFragment.OnFieldSelectedListener {

    private TableView tableView;
    private OtherPlayer otherPlayer = new OtherPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);


        TableFragment tableFragment = (TableFragment)
                getSupportFragmentManager().findFragmentById(R.id.Table);
        tableView = (TableView) tableFragment.tableView;

        Thread opThread = new Thread(otherPlayer);
        //runOnUiThread(otherPlayer);
        opThread.start();

    }


    private class OtherPlayer implements Runnable {
        public void run() {
            Log.d("AAjajajajjajajajajA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");
            while(true){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d("AAjajajajjajajajajA", "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaa");


                //Coordinates c = table.putAutomatic(State.cross);

                /*changePinColor(c.x*pinSize +1, c.y*pinSize +1, R.drawable.pin40);
                invalidate();*/
                //postDelayed(tableView, DELAY_TIME_MILLIS);
            }

        }
    }

    public void onFieldSelected(int x,int y) {
        System.out.println("\njeldaaa\n");
    }
}
