package com.boardgame.miljac.grangla;

import java.util.Random;

public class Note {

    Random rand = new Random();
    double rnd = rand.nextDouble();
    double soloFrBendFactor;
    double soloFrBendFr;
    int soloTimeFrameDeviation;
    private boolean keyChange = false;
    private int noteIndex;
    private int relativeIndex;
    private boolean slide = false;
    private int nextNoteIndex = -20;
    private int volume = 100;
    private boolean relativetoKey = false;
    private boolean relativetoPrevious = false;


    public double getSoloFrBendFactor() {
        return soloFrBendFactor;
    }

    public void setSoloFrBendFactor(double soloFrBendFactor) {
        this.soloFrBendFactor = soloFrBendFactor;
    }

    public void setSoloFrBendFr(double soloFrBendFr) {
        this.soloFrBendFr = soloFrBendFr;
    }

    public double getSoloFrBendFr() {
        return soloFrBendFr;
    }

    public int getSoloTimeFrameDeviation() {
        return soloTimeFrameDeviation;
    }

    public void setSoloTimeFrameDeviation(int soloTimeFrameDeviation) {
        this.soloTimeFrameDeviation = soloTimeFrameDeviation;
    }

    public Note(int i){

        this.noteIndex = i;
        this.relativeIndex = i;

        rnd = rand.nextDouble();
        this.soloFrBendFactor = (((18.0/17.0) - 1.0) / 14) * rnd*rnd*rnd*2.8;

        this.soloFrBendFr = 0.4 + rnd*rnd*rnd*11;

        rnd = rand.nextDouble();
        double rnd2 = rand.nextDouble();
        this.soloTimeFrameDeviation = (int)(((rnd*900) - 450)*rnd2*1.5);

    }

    public boolean isKeyChange() {
        return keyChange;
    }

    public void setKeyChange(boolean keyChange) {
        this.keyChange = keyChange;
    }

    public boolean isSlide() {
        return slide;
    }

    public void setSlide(boolean slide) {
        this.slide = slide;
    }

    public void setNextNoteIndex(int nextNoteIndex) {
        this.nextNoteIndex = nextNoteIndex;
    }

    public void setRelativetoKey(boolean relativetoKey) {
        this.relativetoKey = relativetoKey;
    }


    public void setRelativetoPrevious(boolean RelativetoPrevious) {
        this.relativetoPrevious = relativetoPrevious;
    }

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

    /**
     * Turns index relative to key to absolute value
     * @param key
     */
    public void applyKeyOnRelativeNote(int key){
        this.noteIndex = this.relativeIndex + key;
        this.nextNoteIndex += key;
    }

    /**
     * Sets index of this note to the previous in the given key
     * @param key
     * @return
     */
    public int downOneInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo soloNote negativnim brojevima ponasa
            case 0:
                noteIndex = noteIndex - 2;
                return noteIndex - 2;
            case 1:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 2:
                noteIndex = noteIndex - 2;
                return noteIndex - 2;
            case 3:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 4:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 5:
                noteIndex = noteIndex - 2;
                return noteIndex - 2;
            case 6:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 7:
                noteIndex = noteIndex - 2;
                return noteIndex - 2;
            case 8:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 9:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            case 10:
                noteIndex = noteIndex - 2;
                return noteIndex - 2;
            case 11:
                noteIndex = noteIndex - 1;
                return noteIndex - 1;
            default:
                return noteIndex;
        }
    }

    /**
     * Sets index of this note to the next in the given key
     * @param key
     * @return
     */

    public int upOneInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo soloNote negativnim brojevima ponasa
            case 0:
                noteIndex = noteIndex + 2;
                return noteIndex + 2;
            case 1:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 2:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 3:
                noteIndex = noteIndex + 2;
                return noteIndex + 2;
            case 4:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 5:
                noteIndex = noteIndex + 2;
                return noteIndex + 2;
            case 6:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 7:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 8:
                noteIndex = noteIndex + 2;
                return noteIndex + 2;
            case 9:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            case 10:
                noteIndex = noteIndex + 2;
                return noteIndex + 2;
            case 11:
                noteIndex = noteIndex + 1;
                return noteIndex + 1;
            default:
                return noteIndex;
        }
    }

    /**
     * Retrieves the index of the previous note in the given key
     * @param key
     * @return
     */
    public int getPreviousIndexInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo soloNote negativnim brojevima ponasa
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
                return noteIndex;
        }
    }

    /**
     * Retrieves the index of the next note in the given key
     * @param key
     * @return
     */
    public int getNextIndexInMinorKey(int key){
        switch((noteIndex - key) % 12) { //provjeri kak se ovo soloNote negativnim brojevima ponasa
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
                return noteIndex;
        }
    }
}
