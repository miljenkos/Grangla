package com.boardgame.miljac.grangla;

import java.util.Random;

public class SoloGenerator {
    Random rand = new Random();
    Note[] solo1;
    Note[] solo2;
    Note[] solo3;
    Note[] solo4;
    Note[] currentSolo;
    Note a;
    Note lastNote = new Note(TableConfig.SOLO_NOTE_LOWER_BOUNDARY + 10);
    Note nextNote;
    int currentKey;
    int soloIndex = 0;
    double rnd;
    boolean keyChange = false;

    public void setBeginning(boolean beginning) {
        this.beginning = beginning;
    }

    boolean beginning = true;

    public void setMajor(boolean major) {
        this.major = major;
    }

    boolean major = false;


    public void setKeyChange(boolean keyChange) {
        this.keyChange = keyChange;
    }


    public SoloGenerator() {
        solo1 = generateSolo2();
        solo2 = generateSolo();
        solo3 = generateSolo();
        solo4 = generateSolo2();

        currentSolo = solo1;
    }

    public void setKey(int key){currentKey = key;}

    /**
     * Generates a solo sequence of random length. Notes in a solo sequence firsat alternately go up and down for two predefined random values.
     * After that some randomly picked notes get set to the next higher or lower note, or equalized with the last note in the sequence.
     * Volume also get randomized.
     * Pitch index is relative to the last note, it gets set to apsolute later.
     */
    private Note[] generateSolo(){
        int length = 14 + (int)(rand.nextDouble() * 11);
        Note[] solo = new Note[length];

        int up = (int)(rand.nextDouble() * 6) -2;
        int down = -(int)(rand.nextDouble() * 6) +2;


        for(int x=0; x<length; x++){
            rnd = rand.nextDouble();
            if (rnd < 0.1){
                up++;
            }
            if (rnd > 0.9){
                down--;
            }

            if (x%2 == 0){
                a = new Note(up);
            } else {
                a = new Note(down);
            }

            rnd = rand.nextDouble();
            if (rnd < (0.1 + x*0.025)) {
                a.setIndex(0);
            }

            rnd = rand.nextDouble();
            a.setVolume(190 + (int)(55*rnd) - x*5);

            rnd = rand.nextDouble();
            if (rnd < 0.1){
                a.setIndex(a.getIndex()+1);
            }
            if (rnd > 0.9){
                a.setIndex(a.getIndex()-1);
            }

            solo[x] = a;
        }

        a.setRelativetoPrevious(true);
        return solo;
    }


/**
 * Generates a solo with some different parameters. Completely new function for complete freedom over playing with all parameters.
 **/
    private Note[] generateSolo2(){
        int length = 5 + (int)(rand.nextDouble() * 10);
        Note[] solo = new Note[length];

        int up = (int)(rand.nextDouble() * 6) -2;
        int down = -(int)(rand.nextDouble() * 6) +2;

        for(int x=0; x<length; x++){
            rnd = rand.nextDouble();
            if (rnd < 0.1){
                up++;
            }
            if (rnd > 0.9){
                down--;
            }

            if (x%2 == 0){
                a = new Note(up);
            } else {
                a = new Note(down);
            }

            rnd = rand.nextDouble();
            if (rnd < (0.3 + x*0.03)) {
                a.setIndex(0);
            }

            rnd = rand.nextDouble();
            a.setVolume(190 + (int)(55*rnd) - x*7);

            rnd = rand.nextDouble();
            if (rnd < 0.2){
                a.setIndex(a.getIndex()+1);
            }
            if (rnd > 0.8){
                a.setIndex(a.getIndex()-1);
            }

            solo[x] = a;
        }

        a.setRelativetoPrevious(true);

        return solo;
    }



    public Note getNextSoloNote(){
        soloIndex++;
        if (soloIndex >= (currentSolo.length)){
            if(keyChange) {//every solo begins with the begining of a bar
                soloIndex = 0;

                int f = (int) (Math.ceil(3 - rand.nextDouble() * 3.7));
                switch (f) {
                    case 1:
                        currentSolo = solo1;
                        break;
                    case 2:
                        currentSolo = solo2;
                        break;
                    case 3:
                        currentSolo = solo3;
                        break;
                    case 4:
                        currentSolo = solo4;
                        break;
                }
            } else {//if the solo is ended and the bar isn't, the last note is prolonged
                Note a = new Note(lastNote.getIndex());
                a.setVolume(lastNote.getVolume()/2);
                a.setSoloFrBendFr(lastNote.getSoloFrBendFr());
                a.setSoloFrBendFactor(lastNote.getSoloFrBendFactor());
                a.setSoloTimeFrameDeviation(lastNote.getSoloTimeFrameDeviation());

                lastNote.setVolume(lastNote.getVolume());
                return a;
            }
        }

        nextNote = new Note(lastNote.getIndex());
        nextNote.setVolume(currentSolo[soloIndex].getVolume());
        nextNote.setSoloFrBendFr(currentSolo[soloIndex].getSoloFrBendFr());
        nextNote.setSoloFrBendFactor(currentSolo[soloIndex].getSoloFrBendFactor());
        nextNote.setSoloTimeFrameDeviation(currentSolo[soloIndex].getSoloTimeFrameDeviation());

        //note index has to be set from relative to last note to absolute to be played
        if (currentSolo[soloIndex].getIndex() > 0){
            for (int x=0; x<currentSolo[soloIndex].getIndex(); x++){
                nextNote.upOneInMinorKey(currentKey);
            }
        } else {
            for (int x=0; x<Math.abs(currentSolo[soloIndex].getIndex()); x++){
                nextNote.downOneInMinorKey(currentKey);
            }
        }

        if  ((soloIndex == 0) || beginning){
            if(!major) {
                nextNote.setIndex(currentKey);
            } else {
                nextNote.setIndex(currentKey+3);//minor scale contains the same notes as major + 3 semitones
            }

            if(!beginning) {
                if ((lastNote.getIndex() - nextNote.getIndex()) > 6) {
                    nextNote.setIndex(nextNote.getIndex() + 12);
                }
                if ((lastNote.getIndex() - nextNote.getIndex()) > 6) {
                    nextNote.setIndex(nextNote.getIndex() + 12);
                }
            }
        }

        if (nextNote.getIndex() > TableConfig.SOLO_NOTE_UPPER_BOUNDARY)
            nextNote.setIndex(nextNote.getIndex()-12);
        if (nextNote.getIndex() < TableConfig.SOLO_NOTE_LOWER_BOUNDARY)
            nextNote.setIndex(nextNote.getIndex()+12);

        lastNote = new Note(nextNote.getIndex());
        lastNote.setVolume(nextNote.getVolume());
        lastNote.setSoloFrBendFr(nextNote.getSoloFrBendFr());
        lastNote.setSoloFrBendFactor(nextNote.getSoloFrBendFactor());
        lastNote.setSoloTimeFrameDeviation(nextNote.getSoloTimeFrameDeviation());

        keyChange = false;

        return nextNote;
    }
}
