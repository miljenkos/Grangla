package com.example.miljac.myapplication;

/**
 * Created by miljac on 28.5.2017..
 */

public class BassGenerator {



    //private int lastRiff = 0;
    private int lastNoteInRiff = -1;
    private int lastChordNo = -1;

    int chordKey1;
    int chordKey2;


    private Note[] keys;
    private int measure = 3;
    private int currentMeasure = 3;
    public void setKey(int inkey){this.currentKey = inkey;}
    public int getKey(){return this.currentKey;}
    public void setMeasure (int m){this.measure = m;}
    public int getMeasure(){return this.measure;}

    private Note[][] bassRiffs = new Note[5][10];

    Note[] currentChordSet;
    Note[] currentRiff;

    Note[] chordSet1;
    Note[] chordSet2;
    Note[] chordSet3;

    Note[] riff1;
    Note[] riff2;
    Note[] riff3;

    private int currentKey;

    private Note[] lastRiff;



    private Note[] generateChordset(){
        int numberOfChords = (int) (1.4 + Math.ceil(3* Math.random()));
        Note[] chordSet = new Note[numberOfChords];
        for (int x=0; x<chordSet.length; x++) {
            int r = (int)(Math.ceil(2* Math.random()));
            if (r<2)
                chordSet[x] = new Note(chordKey1);
            else
                chordSet[x] = new Note(chordKey2);
        }
        for(int j =0; j<3; j++){
            for (int x = 0; x < numberOfChords; x++) {
                int r = (int) (Math.ceil(2 * Math.random()));
                if (r < 2)
                    chordSet[x].setIndexToFifth();
                else
                    chordSet[x].setIndexToFourth();

                chordSet[x].setUpperBoundary(TableConfig.BASS_NOTE_UPPER_BOUNDARY);
            }
        }

        System.out.print("chordset: ");
        for (int x=0; x<chordSet.length; x++) {
            System.out.print(chordSet[x].getIndex() + " ");
        }
        System.out.println();

        return chordSet;
    }



    private Note[] generateRiff() {
        Note[] riff = new Note[10];

        for (int j = 0; j < 10; j = j + 2) {
            Note a = mainBeatNoteGenerate(TableConfig.BASSS_TONES_DISPERSION);
            Note a2 = new Note(a.getIndex());
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

        System.out.print("riff: ");
        for (int x=0; x<riff.length; x++) {
            System.out.print(riff[x].getIndex() + " ");
        }
        System.out.println();

        return riff;
    }


    public BassGenerator(){

        Note a, a2;

        chordKey1 = (int)(Math.ceil(Math.random() * 12) + TableConfig.BASS_NOTE_LOWER_BOUNDARY);
        chordKey2 = (int)(Math.ceil(Math.random() * 12) + TableConfig.BASS_NOTE_LOWER_BOUNDARY);


        System.out.println(chordKey1);
        System.out.println(chordKey2);


        /*chordSet1 = generateChordset();
        chordSet2 = generateChordset();
        chordSet3 = generateChordset();

        riff1 = generateRiff();
        riff2 = generateRiff();
        riff3 = generateRiff();*/

        chordSet1 = generateChordset();
        chordSet2 = generateChordset();
        chordSet3 = generateChordset();

        riff1 = generateRiff();
        riff2 = generateRiff();
        riff3 = generateRiff();


        currentChordSet = chordSet1;
        currentRiff = riff1;
        currentKey = getNextChord();




        /*for(int x=0; x<5; x++){
            currentKey = new Note((int)(Math.ceil(Math.random() * 11) + 30));
            for (int j = 0; j<10; j=j+2){
                a = mainBeatNoteGenerate(0.2);
                a2 = new Note(a.getIndex());
                if(j == 0) {
                    a.setVolume(10000);
                    a2.setVolume(5000);
                }else {
                    a.setVolume(6000);
                    a2.setVolume(3000);
                }
                bassRiffs[x][j] = a;
                bassRiffs[x][j+1] = a2;

                System.out.println("AAAAAAAAAAAAA" + x+ "   " + j);
                System.out.println(bassRiffs[x][j].getIndex());
                System.out.println(bassRiffs[x][j].getFrequency());
            }
        }*/

    }

    public int getNextChord(){
        lastChordNo++;
        if (lastChordNo >= (currentChordSet.length)){
            lastChordNo = 0;


            int f =(int)(Math.ceil(3 - Math.random() * 2.6));
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

        /*System.out.println("LL " + lastRiff);
        System.out.println("KK " + lastNoteInRiff);
        System.out.println(bassRiffs[lastRiff][lastNoteInRiff]);*/

        System.out.println("next Chord " + currentChordSet[lastChordNo].getIndex());
        return currentChordSet[lastChordNo].getIndex();
    }


    public Note getNextBassNote(){
        lastNoteInRiff++;
        if (lastNoteInRiff >= (currentMeasure*2)){
            currentMeasure = measure;
            lastNoteInRiff = 0;

            currentKey = getNextChord();

            int f =(int)(Math.ceil(3 - Math.random() * 2.6));
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

        System.out.println("next relative note: " + currentRiff[lastNoteInRiff].getIndex() + " on key " + currentKey);

        Note newNote = currentRiff[lastNoteInRiff];
        newNote.applyKeyOnRelativeNote(currentKey);
        newNote.setUpperBoundary(TableConfig.BASS_NOTE_UPPER_BOUNDARY);

        System.out.println("next note: " + newNote.getIndex());

        return newNote;
    }

    private Note mainBeatNoteGenerate(double rndFactor) {
        Note result;


        double r = Math.random();

        if (r > rndFactor) {
            result = new Note(0);
            result.setRelativetoKey(true);
            return result;
        }
        r = Math.random();
        if (r > rndFactor) {
            result = new Note(3);
            result.setRelativetoKey(true);
            return result;
        }

        r = Math.random();
        if (r > rndFactor) {
            result = new Note(7);
            result.setRelativetoKey(true);
            return result;
        }

        r = Math.random();
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
