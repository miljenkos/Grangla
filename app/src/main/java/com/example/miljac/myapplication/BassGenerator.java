package com.example.miljac.myapplication;

/**
 * Created by miljac on 28.5.2017..
 */

public class BassGenerator {

    private Note currentKey;

    private int lastRiff = 0;
    private int lastNoteInRiff = -1;


    private Note[] keys;
    private int measure = 3;
    public void setKey(int inkey){this.currentKey = new Note(inkey);}
    public Note getKey(){return this.currentKey;}
    public void setMeasure (int m){this.measure = m;}
    public int getMeasure(){return this.measure;}

    private Note[][] bassRiffs = new Note[5][10];

    public BassGenerator(){

        Note a, a2;
        for(int x=0; x<5; x++){
            currentKey = new Note((int)(Math.random() * 11 + 30));
            for (int j = 0; j<10; j=j+2){
                a = mainBeatNoteGenerate(currentKey, 0.1);
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
        }

    }

    public Note getNextBassNote(){
        lastNoteInRiff++;
        if (lastNoteInRiff >= (measure*2)){
            lastNoteInRiff = 0;
            lastRiff++;
            if (lastRiff >= 5){
                lastRiff = 0;
            }
        }
        System.out.println("LL " + lastRiff);
        System.out.println("KK " + lastNoteInRiff);
        System.out.println(bassRiffs[lastRiff][lastNoteInRiff]);
        return bassRiffs[lastRiff][lastNoteInRiff];
    }

    private Note mainBeatNoteGenerate(Note key, double rndFactor) {
        double r = Math.random();
        if (r > rndFactor) {
            return (key.getNoteInMinorKey(1));
        }

        r = Math.random();
        if (r > rndFactor) {
            return (key.getNoteInMinorKey(3));
        }

        r = Math.random();
        if (r > rndFactor) {
            return (key.getNoteInMinorKey(5));
        }

        r = Math.random();
        if (r > rndFactor) {
            return (key.getNoteInMinorKey(7));
        }

        return (key.getNoteInMinorKey(2));
    }
}
