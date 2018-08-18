package com.boardgame.miljac.grangla;

import java.util.Random;

public class BassGenerator {

    private int lastNoteInRiff = -1;
    private int lastChordNo = -1;

    int chordKey1;
    int chordKey2;

    private int measure = 3;
    private int currentMeasure = 3;
    public int getCurrentKey(){return this.currentKey;}
    public void setMeasure (int m){this.measure = m;}

    Note[] currentChordSet;
    Note[] currentRiff;

    Note[] chordSet1;
    Note[] chordSet2;
    Note[] chordSet3;

    Note[] riff1;
    Note[] riff2;
    Note[] riff3;

    private int currentKey;
    Random rand = new Random();

    /**
     * Generates a chord progression
     * two random keys and their fourths and fifths,
     * and their fourths and fifths,
     * and their fourths and fifths.
     * @return
     */
    private Note[] generateChordset(){
        int numberOfChords = (int) (1.4 + Math.ceil(3* rand.nextDouble()));
        Note[] chordSet = new Note[numberOfChords];

        for (int x=0; x<chordSet.length; x++) {
            int r = (int)(Math.ceil(2* rand.nextDouble()));
            if (r<2)
                chordSet[x] = new Note(chordKey1);
            else
                chordSet[x] = new Note(chordKey2);
        }

        for(int j =0; j<3; j++){
            for (int x = 0; x < numberOfChords; x++) {
                int r = (int) (Math.ceil(2 * rand.nextDouble()));
                if (r < 2)
                    chordSet[x].setIndexToFifth();
                else
                    chordSet[x].setIndexToFourth();

                chordSet[x].setUpperBoundary(TableConfig.BASS_NOTE_UPPER_BOUNDARY);
            }
        }

        return chordSet;
    }


    /**
     * Generates a bass riff. Mostly the main chord tones will be used, with the bigger chances for lower notes.
     * @return
     */
    private Note[] generateRiff() {
        Note[] riff = new Note[10];
        Note a2 = new Note(5);
        Note a;
        double rnd;
        int lastIndex;

        for (int j = 0; j < 10; j = j + 2) {
            lastIndex = a2.getIndex();
            a = mainBeatNoteGenerate(TableConfig.BASSS_TONES_DISPERSION);
            rnd = rand.nextDouble();

            if (rnd<0.25) {
                a2 = new Note(a.getNextIndexInMinorKey(0));
            } else {
                a2 = new Note(a.getIndex());
            }

            rnd = rand.nextDouble();
            if (rnd<0.2) {
                a2.setIndex(a.getPreviousIndexInMinorKey(0));
            }

            rnd = rand.nextDouble();
            if (rnd<0.12) {
                a.setSlide(true);
                a.setIndex(lastIndex);
                a.setNextNoteIndex(a2.getIndex());
            }


            if (j == 0) {
                a.setVolume(5000);
                a.setKeyChange(true);
                a2.setVolume(5000);
            } else {
                a.setVolume(4000);
                a2.setVolume(3000);
            }
            riff[j] = a;
            riff[j + 1] = a2;
        }

        return riff;
    }


    public BassGenerator(){

        chordKey1 = (int)(Math.ceil(rand.nextDouble() * 12) + TableConfig.BASS_NOTE_LOWER_BOUNDARY);
        chordKey2 = (int)(Math.ceil(rand.nextDouble() * 12) + TableConfig.BASS_NOTE_LOWER_BOUNDARY);

        chordSet1 = generateChordset();
        chordSet2 = generateChordset();
        chordSet3 = generateChordset();

        riff1 = generateRiff();
        riff2 = generateRiff();
        riff3 = generateRiff();

        currentChordSet = chordSet1;
        currentRiff = riff1;
        currentKey = getNextChord();
    }

    public int getNextChord(){
        lastChordNo++;
        if (lastChordNo >= (currentChordSet.length)){
            lastChordNo = 0;

            int f =(int)(Math.ceil(3 - rand.nextDouble() * 2.6));
            switch(f){
                case 1:
                    currentChordSet = chordSet1;
                    break;
                case 2:
                    currentChordSet = chordSet2;
                    break;
                case 3:
                    currentChordSet = chordSet3;
                    break;
            }
        }

        return currentChordSet[lastChordNo].getIndex();
    }


    public Note getNextBassNote(){
        lastNoteInRiff++;
        if (lastNoteInRiff >= (currentMeasure*2)){
            currentMeasure = measure;
            lastNoteInRiff = 0;

            currentKey = getNextChord();

            int f =(int)(Math.ceil(3 - rand.nextDouble() * 2.6));
            switch(f){
                case 1:
                    currentRiff = riff1;
                    break;
                case 2:
                    currentRiff = riff2;
                    break;
                case 3:
                    currentRiff = riff3;
                    break;
            }
        }

        Note newNote = currentRiff[lastNoteInRiff];
        newNote.applyKeyOnRelativeNote(currentKey);
        newNote.setUpperBoundary(TableConfig.BASS_NOTE_UPPER_BOUNDARY);

        return newNote;
    }

    /**
     * Generates a note from a chord.
     * @param rndFactor
     * @return
     */
    private Note mainBeatNoteGenerate(double rndFactor) {
        Note result;
        double r = rand.nextDouble();

        if (r > rndFactor) {
            result = new Note(0);
            result.setRelativetoKey(true);
            return result;
        }
        r = rand.nextDouble();
        if (r > rndFactor) {
            result = new Note(3);
            result.setRelativetoKey(true);
            return result;
        }

        r = rand.nextDouble();
        if (r > rndFactor) {
            result = new Note(7);
            result.setRelativetoKey(true);
            return result;
        }

        r = rand.nextDouble();
        if (r > rndFactor) {
            result = new Note(10);
            result.setRelativetoKey(true);
            return result;
        }

        result = new Note(2);
        result.setRelativetoKey(true);
        return result;
    }
}
