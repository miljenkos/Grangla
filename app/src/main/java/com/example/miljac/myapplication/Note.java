package com.example.miljac.myapplication;

/**
 * Created by miljac on 23.5.2017..
 */

public class Note {

    public Note(int i){

        this.noteIndex = i;
        this.relativeIndex = i;
    }

    public boolean isKeyChange() {
        return keyChange;
    }

    public void setKeyChange(boolean keyChange) {
        this.keyChange = keyChange;
    }

    private boolean keyChange = false;

    private int noteIndex;

    public int getRelativeIndex() {
        return relativeIndex;
    }

    public void setRelativeIndex(int relativeIndex) {
        this.relativeIndex = relativeIndex;
    }

    private int relativeIndex;

    private boolean slide = false;

    public boolean isSlide() {
        return slide;
    }

    public void setSlide(boolean slide) {
        this.slide = slide;
    }

    public int getNextNoteIndex() {
        return nextNoteIndex;
    }

    public void setNextNoteIndex(int nextNoteIndex) {
        this.nextNoteIndex = nextNoteIndex;
    }

    private int nextNoteIndex = -20;

    private int volume;

    public boolean isRelativetoKey() {
        return relativetoKey;
    }

    public void setRelativetoKey(boolean relativetoKey) {
        this.relativetoKey = relativetoKey;
    }

    private boolean relativetoKey = false;

    private double indexToFrequency(int x) {
        if (x == 1) return 32.7;
        if (x > 12) return (2 * indexToFrequency(x-12));
        return 1.059463094359 * indexToFrequency(x-1);
    }


    public double getFrequency(){
        return indexToFrequency(noteIndex);
    }

    public double getNextNoteFrequency(){
        return indexToFrequency(nextNoteIndex);
    }

    public double getSlideFrequency(){
        return indexToFrequency(getNextNoteIndex());
    }



    public int  getIndex(){
        return noteIndex;
    }

    public void  setIndex(int noteIndex){ this.noteIndex = noteIndex; }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }


    public void setIndexToFifth(){
        noteIndex += 7;
    }

    public void setIndexToFourth(){
        noteIndex += 5;
    }

    public void setUpperBoundary(int boundary){
        while (noteIndex>boundary){
            noteIndex -= 12;
        }
        while (nextNoteIndex>boundary){
            nextNoteIndex -= 12;
        }
    }


    public void applyKeyOnRelativeNote(int key){
        this.noteIndex = this.relativeIndex + key;
        this.nextNoteIndex += key;
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


    /*public int getPreviousIndexInMinorKey(int key){
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
    }*/

    public int getPreviousIndexInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo s negativnim brojevima ponasa
            case 0:
                return noteIndex - 2;
            case 1:
                return noteIndex - 1;
            case 2:
                return noteIndex - 2;
            case 3:
                return noteIndex - 1;
            case 4:
                return noteIndex - 1;
            case 5:
                return noteIndex - 2;
            case 6:
                return noteIndex - 1;
            case 7:
                return noteIndex - 2;
            case 8:
                return noteIndex - 1;
            case 9:
                return noteIndex - 1;
            case 10:
                return noteIndex - 2;
            case 11:
                return noteIndex - 1;
            default:
                return getNextIndexInMinorKey(key - 12);
        }
    }


    public int getNextIndexInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo s negativnim brojevima ponasa
            case 0:
                return noteIndex + 2;
            case 1:
                return noteIndex + 1;
            case 2:
                return noteIndex + 1;
            case 3:
                return noteIndex + 2;
            case 4:
                return noteIndex + 1;
            case 5:
                return noteIndex + 2;
            case 6:
                return noteIndex + 1;
            case 7:
                return noteIndex + 1;
            case 8:
                return noteIndex + 2;
            case 9:
                return noteIndex + 1;
            case 10:
                return noteIndex + 2;
            case 11:
                return noteIndex + 1;
            default:
                return getNextIndexInMinorKey(key - 12);
        }
    }



}
