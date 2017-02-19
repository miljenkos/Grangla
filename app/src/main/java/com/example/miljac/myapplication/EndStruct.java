package com.example.miljac.myapplication;

/**
 * Created by miljac on 19.2.2017..
 */

public class EndStruct {

    public Coordinates first, second, third, fourth;
    public State winner;

    public EndStruct(State w, Coordinates f, Coordinates s, Coordinates t, Coordinates fo){
        this.first = f;
        this.second = s;
        this.third = t;
        this.fourth = fo;
        this.winner = w;
    }

}
