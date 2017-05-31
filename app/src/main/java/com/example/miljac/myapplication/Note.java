package com.example.miljac.myapplication;

/**
 * Created by miljac on 23.5.2017..
 */

public class Note {
    private int noteIndex;



    private int volume;

    public Note(int i){
        this.noteIndex = i;
    }

    private double indexToFrequency(int x) {
        if (x == 1) return 32.7;
        if (x > 12) return (2 * indexToFrequency(x-12));
        return 1.059463094359 * indexToFrequency(x-1);
    }

    public double getFrequency(){
        return indexToFrequency(noteIndex);
    }

    public int  getIndex(){
        return noteIndex;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
    //kljuc je ova nota, i je 2- sekunda, 3 - terca itd.
    public int getNoteIndexInMinorKey(int i){
        if (i <= 0 ) return 0;
        switch(i) {
            case 1:
                return noteIndex;
            case 2:
                return noteIndex + 2;
            case 3:
                return noteIndex + 3;
            case 4:
                return noteIndex + 5;
            case 5:
                return noteIndex + 7;
            case 6:
                return noteIndex + 8;
            case 7:
                return noteIndex + 10;
            default:
                return getNoteIndexInMinorKey(noteIndex - 7);
        }
    }

    //kljuc je ova nota, i je 2- sekunda, 3 - terca itd.
    public Note getNoteInMinorKey(int i){
        return new Note(this.getNoteIndexInMinorKey(i));
    }



    public int getNextIndexInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo s negativnim brojevima ponasa
            case 0:
            case 1:
                return noteIndex + 2;
            case 2:
                return noteIndex + 3;
            case 3:
            case 4:
                return noteIndex + 5;
            case 5:
            case 6:
                return noteIndex + 7;
            case 7:
                return noteIndex + 8;
            case 8:
            case 9:
                return noteIndex + 10;
            case 10:
            case 11:
                return noteIndex + 12;
            default:
                return getNextIndexInMinorKey(key - 12);
        }
    }



}
