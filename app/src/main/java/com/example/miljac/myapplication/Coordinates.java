package com.example.miljac.myapplication;

/**
 * Created by miljac on 24.1.2017..
 */

import com.example.miljac.myapplication.Table;

public class Coordinates {

    public int x;
    public int y;

    public Coordinates(int ix, int iy) {
        int i2 = (ix + Table.TABLE_SIZE*2) % Table.TABLE_SIZE;
        int j2 = (iy + Table.TABLE_SIZE*2) % Table.TABLE_SIZE;


        this.x = i2;
        this.y = j2;
    }

}
