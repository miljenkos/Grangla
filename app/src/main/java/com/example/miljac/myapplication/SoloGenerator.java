package com.example.miljac.myapplication;

import java.util.Random;

/**
 * Created by miljac on 30.7.2017..
 */

public class SoloGenerator {


    Random rand = new Random();
    Note[] solo1;
    Note[] solo2;
    Note[] solo3;
    Note[] currentSolo;
    Note a;
    Note lastNote = new Note(TableConfig.SOLO_NOTE_LOWER_BOUNDARY + 10);
    Note nextNote;
    int currentKey;
    int soloIndex = 0;
    double rnd;


    public SoloGenerator() {
        System.out.println("usel sam u konstruktor!");
        solo1 = generateSolo();
        solo2 = generateSolo();
        solo3 = generateSolo();

        currentSolo = solo1;
    }

    public void setKey(int key){currentKey = key;}

    private Note[] generateSolo(){
        int length = 10 + (int)(rand.nextDouble() * 20);
        Note[] solo = new Note[length];

        int up = (int)(rand.nextDouble() * 3) + 1;
        int down = 0 - (int)(rand.nextDouble() * 3) - 1;


        for(int x=0; x<length; x++){
            if (x%2 == 0){
                a = new Note(up);
            } else {
                a = new Note(down);
            }

            rnd = rand.nextDouble();
            if (rnd<0.4) {
                a.setIndex(0);
            }

            solo[x] = a;
        }


        a.setRelativetoPrevious(true);

        return solo;
    }


    public Note getNextSoloNote(){
        soloIndex++;
        if (soloIndex >= (currentSolo.length)){
            soloIndex = 0;

            int f =(int)(Math.ceil(3 - rand.nextDouble() * 2.6));
            switch(f){
                case 1:
                    currentSolo = solo1;
                    break;
                case 2:
                    currentSolo = solo2;
                    break;
                case 3:
                    currentSolo = solo3;
                    break;
            }
        }

        for(int x=0; x<solo1.length; x++){
            System.out.print(solo1[x] + " ");
        }

        for(int x=0; x<currentSolo.length; x++){
            System.out.print(currentSolo[x] + " ");
        }

        nextNote = new Note(lastNote.getIndex());
        if (currentSolo[soloIndex].getIndex() > 0){
            for (int x=0; x<currentSolo[soloIndex].getIndex(); x++){
                nextNote.upOneInMinorKey(currentKey);
            }
        } else {
            for (int x=0; x<Math.abs(currentSolo[soloIndex].getIndex()); x++){
                nextNote.downOneInMinorKey(currentKey);
            }
        }

        if (nextNote.getIndex() > TableConfig.SOLO_NOTE_UPPER_BOUNDARY)
            nextNote.setIndex(nextNote.getIndex()-12);
        if (nextNote.getIndex() < TableConfig.SOLO_NOTE_LOWER_BOUNDARY)
            nextNote.setIndex(nextNote.getIndex()+12);

        lastNote = new Note(nextNote.getIndex());

        return nextNote;
    }
}
