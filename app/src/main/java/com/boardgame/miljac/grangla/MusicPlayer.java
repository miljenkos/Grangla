package com.boardgame.miljac.grangla;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by miljac on 10.5.2017..
 */


class MusicPlayer implements Runnable {
    int sr = 8000;//44100;
    boolean isRunning = true;
    double bassFr = 440.f;
    double soloFr = 440.f;
    double bassFrSlide = 440.f;
    double fr2, fr3, fr4, frBassCh = 440.f;
    double sinArray[] = new double[1000];
    double twopi = 8. * Math.atan(1.);
    long start;
    long noteStartTime = 0;
    long noteEndTime = 0;
    long noteDuration = 1000;
    long noteDuration2 = 1000;
    long noteDurationToWrite = 1000;
    boolean major = false;

    int amp = 30000;
    int chordAmp = 750;
    int soloAmp = 200;
    int bassLevel;
    int pom;
    int soloTimeFrameDeviation;
    double mutingFactor = 200.0;

    //double fr = 440.f;
    double ph = 0.0;
    double phOct = 0.0;
    double ph2 = 0.0;
    double ph3 = 0.0;
    double ph4 = 0.0;
    double phSolo = 0.0;
    double phSoloFrBendFactor = 0.0;
    double chord1FrBendFactorFr = 0.0;
    double phChord1FrBendFactor = 0.0;
    double chord2FrBendFactorFr = 0.0;
    double phChord2FrBendFactor = 0.0;
    double chord3FrBendFactorFr = 0.0;
    double phChord3FrBendFactor = 0.0;
    double soloFrBendFactorFr = 0.0;
    double phBassCh = 0.0;
    double phBassCh2 = 0.0;

    double chord1FrBendFr,chord2FrBendFr, chord3FrBendFr;
    double phaseFactor = 1000. / twopi;
    double phaseStep;
    double phaseStepSlide;
    double phaseStepOct;
    double phaseStepSlideOct;

    BassGenerator bassGenerator = new BassGenerator();
    SoloGenerator soloGenerator = new SoloGenerator();

    AudioTrack audioTrack;
    short samples[], samples2[];
    short samplesChords1[], samplesChords2[], samplesChords3[], samplesBassBase[], samplesToWrite[];
    short samplesSolo[], samplesOct[];
    double soloFrBendFactor;
    double chord1FrBendFactor, chord2FrBendFactor, chord3FrBendFactor;
    int buffsize, buffsize2, buffsizeToWrite;
    Random rand = new Random();
    volatile boolean mute = false;
    boolean dieFinally = false;
    int countBars = 0;



    int index;
    boolean beat = false;
    boolean firstTime = true;
    AtomicBoolean synthFlag = new AtomicBoolean(true);
    Synth synth = new Synth();
    Thread synthThread;


    public MusicPlayer() {
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 100000/*buffsize*/,
                AudioTrack.MODE_STREAM);
    }

    public void setEndSong() {
        this.endSong = true;
    }

    public boolean isEndSong() {
        return endSong;
    }

    boolean endSong = false;

    public void mute(){
        //System.out.println("MUTE");
        mute = true;
    }
    public void unmute(){
        //System.out.println("UNMUTE");
        mute = false;
    }

    public void run() {

        buffsize = 200;

        // create an audiotrack object
        /*audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 50000/*buffsize*/ /*,
                AudioTrack.MODE_STREAM);*/

        samplesToWrite = new short[20000];

        samples = new short[20000];
        samples2 = new short[20000];
        samplesChords1 = new short[20000];
        samplesChords2 = new short[20000];
        samplesChords3 = new short[20000];
        //samplesChordsBass1 = new short[20000];
        samplesOct = new short[20000];
        samplesSolo = new short[20000];
        samplesBassBase = new short[20000];
        // start audio
        audioTrack.play();

        //int index;
        start = System.currentTimeMillis();
        //boolean beat = false;
        //boolean firstTime = true;

        synthThread = new Thread(synth);
        synthThread.setPriority(Thread.MAX_PRIORITY);
        synthThread.start();

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        long thisEndTime = 0;

        // synthesis loop
        int junkCount = 0;

        while (isRunning) {


            if (mute) {/* && dieFinally){*/
                isRunning = false;
            } else {
                if(!synthFlag.get()) {
                    samplesToWrite = samples2;
                    samples2 = new short[20000];
                    buffsizeToWrite = buffsize2;
                    noteDurationToWrite = noteDuration2;

                    synthFlag.set(true);
                    if(junkCount>0) junkCount--;
                }


                if (buffsizeToWrite == 0) continue;


                while (System.currentTimeMillis() < (thisEndTime)) {
                    LockSupport.parkNanos(1_000_000);
                    //Thread.yield();
                }

                audioTrack.write(samplesToWrite, 0, buffsizeToWrite * 2);
                thisEndTime = System.currentTimeMillis() + buffsizeToWrite/8*2;

            }


        }
        audioTrack.pause();
        audioTrack.release();
    }



    public void stop() {
        isRunning = false;
    }


    public void setNoteDuration(long duration) {
        this.noteDuration = duration;
    }
    public void twiceNoteDuration() {this.noteDuration *= 2;}

    public void setMeasure(int measure){
        bassGenerator.setMeasure(measure);
    }

    public void getMeasure(int measure){
        bassGenerator.getMeasure();
    }

    private double indexToFrequency(int x) {
        if (x == 1) return 32.7;
        if (x > 12) return (2 * indexToFrequency(x-12));
        return 1.059463094359 * indexToFrequency(x-1);
    }



















    public class Synth implements Runnable {
        public void run() {


            while (isRunning) {
                samples = new short[20000];

                //





                    /*if (!synthFlag.get()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }*/


//BASS
//one iteration of this loop generates one bass beat, which contains two distinct Notes
                    start = System.currentTimeMillis();
                    //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr " + (start));
                    noteStartTime = start;
                    noteEndTime = noteStartTime + noteDuration;
                    if (beat) {//first note in a bar?
                        bassLevel = amp;
                        beat = false;
                    } else {
                        beat = true;
                    }
                    Note n = bassGenerator.getNextBassNote();
                    Note n2 = bassGenerator.getNextBassNote();
                    bassFr = n.getFrequency();

                    if (n.isSlide()) {//slide effect
                        //System.out.println(n.getNextNoteIndex());
                        bassFrSlide = n.getNextNoteFrequency();
                    } else {
                        bassFrSlide = bassFr;
                    }

                    amp = n.getVolume();
                    if (n.isKeyChange()) chordAmp = 1350;
                    //System.out.println(n.isKeyChange());

                    //frequency calculations
                    phaseStep = twopi * bassFr / sr;
                    phaseStepSlide = twopi * bassFrSlide / sr;
                    buffsize = (int) noteDuration * 8;

                    phaseStepOct = twopi * bassFr *2.001 / sr;
                    phaseStepSlideOct = twopi * bassFrSlide *2.001 / sr;

                    //get key and calculate other chord tones
                    int key1 = bassGenerator.getKey() + 12;
                    int key2 = key1 + 3;
                    if (key2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key2 -= 12;
                    int key3 = key1 + 7;
                    if (key3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key3 -= 12;

                    //maybe default minor chord should be turned to major


                    double rnd = rand.nextDouble();
                    if (rnd < 0.28) {
                        //key1 += 10;
                        //if (key1 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key1 -= 12;
                        key1 -= 2;
                        //System.out.println("dur!");
                        major = true;
                    } else {
                        major = false;
                    }

                    soloGenerator.setKey(key2 - 3);
                    soloGenerator.setMajor(major);

                    if (n.isKeyChange()) {
                        //System.out.println("KEYCHANGE");
                        //System.out.println("KEYCHANGE: " + countBars);
                        soloGenerator.setKeyChange(true);
                        if (countBars < 10) countBars++;



                        rnd = rand.nextDouble();
                        chord1FrBendFr = (((18.0/17.0) - 1.0) / 14) * rnd*rnd*2.8;
                        rnd = rand.nextDouble();
                        chord1FrBendFactorFr = 2 + rnd*rnd*11;
                        rnd = rand.nextDouble();
                        chord2FrBendFr = (((18.0/17.0) - 1.0) / 14) * rnd*1.8;
                        rnd = rand.nextDouble();
                        chord2FrBendFactorFr = 0.4 + rnd*5;
                        rnd = rand.nextDouble();
                        chord3FrBendFr = (((18.0/17.0) - 1.0) / 14) * rnd*2;
                        rnd = rand.nextDouble();
                        chord3FrBendFactorFr = 0.5 + rnd*7;

                    }
                    Note s = soloGenerator.getNextSoloNote();
                    soloFr = s.getFrequency();

                    rnd = rand.nextDouble();
                    soloTimeFrameDeviation = s.getSoloTimeFrameDeviation();




                //sample generation
                    for (int i = 0; i < buffsize * 2; i++) {
//BASS
                        index = (int) (phaseFactor * ph);//speed up sin calculating
                        if (sinArray[index] == 0.0d) {
                            sinArray[index] = Math.sin(ph);
                        }
                        samples[i] = (short) (amp * sinArray[index] * (buffsize * 2 - i) / buffsize + amp / 2);


                        if (i < buffsize) {//first bass note
                            ph += (phaseStep - phaseStepSlide) * ((double) i / (double) buffsize) + phaseStepSlide;
                        } else {//second bass note
                            ph += phaseStep;
                        }

                        if (samples[i] < 0) {
                            samples[i] -= 35 * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize);
                        } else {
                            samples[i] += 35 * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize);
                        }


                        if (ph > twopi) ph -= twopi;
                        if (ph < 0) ph += twopi;

                        //fade out i fade in zbog krcanja
                        if (i < 120) {
                            pom = samples[i] * i;
                            samples[i] = (short) (pom / 120.0);
                        }
                        if (i > (2 * buffsize - 80)) {
                            pom = samples[i] * (2 * buffsize - i);
                            samples[i] = (short) (pom / 80.0);
                        }

                        //switch to second bass note
                        if (i == (buffsize)) {
                            bassFr = n2.getFrequency();
                            phaseStep = twopi * bassFr / sr;
                            phaseStepSlide = phaseStep;

                            phaseStepOct = twopi * bassFr * 2.001 / sr;
                            phaseStepSlideOct = phaseStepOct;
                        }


                        if(samples[i] > (amp* 0.8)){
                            samples[i] = (short)(amp* 0.8 + (samples[i] - amp* 0.8)*0.6);
                        }
                        if(samples[i] < (0-amp* 0.8)){
                            samples[i] = (short)(0-amp* 0.8 + (samples[i] + amp* 0.8)*0.6);
                        }

                        samples[i] = (short)(samples[i] * 0.7);



//BASS octave
                if(true) {
                    index = (int) (phaseFactor * phOct);//speed up sin calculating
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(phOct);
                    }
                    samplesOct[i] = (short)((amp * sinArray[index] * (buffsize * 2 - i) / buffsize + amp / 2)/20);

                    if (i < buffsize) {//first bass note
                        phOct += (phaseStepOct - phaseStepSlideOct) * ((double) i / (double) buffsize) + phaseStepSlideOct;
                    } else {//second bass note
                        phOct += phaseStepOct;
                    }

                    if (phOct > twopi) phOct -= twopi;
                    if (phOct < 0) phOct += twopi;

                    //fade out i fade in zbog krcanja
                    if (i < 120) {
                        pom = samplesOct[i] * i;
                        samplesOct[i] = (short) (pom / 120);
                    }
                    if (i > (2 * buffsize - 60)) {
                        pom = samplesOct[i] * (2 * buffsize - i);
                        samplesOct[i] = (short) (pom / 60);
                    }

                    samplesBassBase[i] = samples[i];
                    samples[i] += samplesOct[i];
                }


//CHORDS
                /*frBassCh = indexToFrequency(major ?
                        bassGenerator.getKey() + 3 - 12 :
                        bassGenerator.getKey() - 12
                );

                //bass part of the chord
                index = (int) (phaseFactor * phBassCh);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(phBassCh);
                }
                samplesChordsBass1[i] = (short) (chordAmp * sinArray[index]);
                phBassCh += twopi * frBassCh / sr;
                if (phBassCh > twopi) phBassCh -= twopi;*/


                        if (true) {


                            fr2 = indexToFrequency(key1);
                            fr3 = indexToFrequency(key2);
                            fr4 = indexToFrequency(key3);



/*
                    index = (int) (phaseFactor * phBassCh2);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(phBassCh2);
                    }
                    samplesChordsBass1[i] += (short) (chordAmp * sinArray[index]);
                    phBassCh2 += twopi * frBassCh * 1.5001 / sr;
                    if (phBassCh2 > twopi) phBassCh2 -= twopi;

                    if (samplesChordsBass1[i] > 0) {
                        samplesChordsBass1[i] = (short) (230);
                    } else {
                        samplesChordsBass1[i] = (short) (-230);

                    }*/
                            //System.out.println("GETKEYYYYYYY::  " + bassGenerator.getKey());


                            //bending of first tone in a chord
                            index = (int) (phaseFactor * phChord1FrBendFactor);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(phChord1FrBendFactor);
                            }
                            chord1FrBendFactor = 1 + chord1FrBendFr * sinArray[index];

                            phChord1FrBendFactor += twopi * chord1FrBendFactorFr / sr;
                            if (phChord1FrBendFactor > twopi) phChord1FrBendFactor -= twopi;


                            //first tone in a chord
                            index = (int) (phaseFactor * ph2);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(ph2);
                            }
                            samplesChords1[i] = (short) (chordAmp * sinArray[index]);
                            ph2 += twopi * fr2 / sr * chord1FrBendFactor;
                            if (ph2 > twopi) ph2 -= twopi;










                            //bending of second tone in a chord
                            index = (int) (phaseFactor * phChord2FrBendFactor);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(phChord2FrBendFactor);
                            }
                            chord2FrBendFactor = 1 + chord2FrBendFr * sinArray[index];

                            phChord2FrBendFactor += twopi * chord2FrBendFactorFr / sr;
                            if (phChord2FrBendFactor > twopi) phChord2FrBendFactor -= twopi;

                            //second tone in a chord
                            index = (int) (phaseFactor * ph3);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(ph3);
                            }

                            samplesChords2[i] = (short) (chordAmp * sinArray[index]);

                            ph3 += twopi * fr3 / sr * chord2FrBendFactor;
                            if (ph3 > twopi) ph3 -= twopi;





                            //bending of third tone in a chord
                            index = (int) (phaseFactor * phChord3FrBendFactor);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(phChord3FrBendFactor);
                            }
                            chord3FrBendFactor = 1 + chord3FrBendFr * sinArray[index];

                            phChord3FrBendFactor += twopi * chord3FrBendFactorFr / sr;
                            if (phChord3FrBendFactor > twopi) phChord3FrBendFactor -= twopi;
                            //third note in a chord
                            index = (int) (phaseFactor * ph4);
                            if (sinArray[index] == 0.0d) {
                                sinArray[index] = Math.sin(ph4);
                            }

                            samplesChords3[i] = (short) (chordAmp * sinArray[index]);
                            ph4 += twopi * fr4 / sr * chord3FrBendFactor;;
                            if (ph4 > twopi) ph4 -= twopi;

                            //long fading out
                            if (chordAmp > 10) {
                                if ((i % 70) == 0)
                                    chordAmp *= 0.99;

                            } else chordAmp = 0;

                            //fade in
                            if ((i < 100) && (n.isKeyChange() || endSong)) {
                                pom = samplesChords1[i] * i;
                                samplesChords1[i] = (short) (pom / 100);
                            }
                            //second tone is moved later a bit
                            if ((i < 200) && (n.isKeyChange() || endSong)) {
                                pom = samplesChords2[i] * (i - 100);
                                if (i > 100)
                                    samplesChords2[i] = (short) (pom / 100);
                                else
                                    samplesChords2[i] = 0;
                            }
                            //third note is moved a little more
                            if ((i < 300) && (n.isKeyChange() || endSong)) {
                                pom = samplesChords3[i] * (i - 200);
                                if (i > 200)
                                    samplesChords3[i] = (short) (pom / 100);
                                else
                                    samplesChords3[i] = 0;
                            }

//DDDDDDDD
                    /*if(i > (2*buffsize - 80)) {
                        pom = samplesChords1[i] * (2*buffsize-i);
                        samplesChords1[i] = (short)(pom / 80.0);
                    }
                    if(i > (2*buffsize - 80)) {
                        pom = samplesChords2[i] * (2*buffsize-i);
                        samplesChords2[i] = (short)(pom / 80.0);
                    }
                    if(i > (2*buffsize - 80)) {
                        pom = samplesChords3[i] * (2*buffsize-i);
                        samplesChords3[i] = (short)(pom / 80.0);
                    }
                    if(i > (2*buffsize - 80)) {
                        pom = samplesChordsBass1[i] * (2*buffsize-i);
                        samplesChordsBass1[i] = (short)(pom / 80.0);
                    }*/


                            if (endSong && n.isKeyChange()) {
                                samplesChords1[i] = 0;
                                samplesChords2[i] = 0;
                                samplesChords3[i] = 0;
                                samplesBassBase[i] *= 3;
                            }
                            if (endSong && !n.isKeyChange()) {
                                //samplesChordsBass1[i] = 0;
                                samplesBassBase[i] = 0;
                            }


                            if ((countBars == 1) || (countBars == 2) || (countBars == 3) || (countBars == 4)) {

                /*} else if (
                        (countBars == 5) ||
                        (countBars == 6)){
                    samples[i] += ((samplesChords1[i]
                            + samplesChords2[i]
                            + samplesChords3[i]
                            + samplesChordsBass1[i]
                            + samplesBassBase[i] / 3) > 0) ?
                            170 : -170;
                */
                            } else {
                                samples[i] += samplesChords1[i];
                                samples[i] += samplesChords2[i];
                                samples[i] += samplesChords3[i];
                                //samples[i] += samplesChordsBass1[i];

                        /*samples[i] += ((samplesChords1[i]
                                + samplesChords2[i]
                                + samplesChords3[i]
                                + samplesChordsBass1[i]
                                + samplesBassBase[i] / 3) > 0) ?
                                170 : -170;*/
                            }
                        } /*else {
                    if (!((countBars == 1) || (countBars == 2) || (countBars == 3) || (countBars == 4))) {
                        samples[i] += (short) (samplesChordsBass1[i] * 2.5);
                    }
                }*/


//SOLO


                        //switch to second solo note
                        if (i == (buffsize + soloTimeFrameDeviation)) {
                            if (n.isKeyChange()) {
                                soloGenerator.setKeyChange(true);
                            }
                            s = soloGenerator.getNextSoloNote();
                            soloFr = s.getFrequency();
                        }


                        //BENDING
                        soloFrBendFactorFr = s.getSoloFrBendFr();

                        index = (int) (phaseFactor * phSoloFrBendFactor);
                        if (sinArray[index] == 0.0d) {
                            sinArray[index] = Math.sin(phSoloFrBendFactor);
                        }
                        soloFrBendFactor = 1 + s.getSoloFrBendFactor() * sinArray[index];

                        phSoloFrBendFactor += twopi * soloFrBendFactorFr / sr;
                        if (phSoloFrBendFactor > twopi) phSoloFrBendFactor -= twopi;

                        //System.out.println("\n\nSOLOFRBENDFACTOR:::   " + soloFrBendFactor);


                        index = (int) (phaseFactor * phSolo);
                        if (sinArray[index] == 0.0d) {
                            sinArray[index] = Math.sin(phSolo);
                        }
                        samplesSolo[i] = (short) (s.getVolume() * (/*
                        (sinArray[index] > 0.3) ? 0.5 : sinArray[index]*/
                                (Math.sqrt(Math.abs(sinArray[index]))) * Math.signum(sinArray[index])// * sinArray[index]
                        ));
                        phSolo += twopi * soloFr / sr * soloFrBendFactor;
                        if (phSolo > twopi) phSolo -= twopi;


//DDDDD
                /*if(i < 80) {
                    pom = samplesSolo[i] * i;
                    samplesSolo[i] = (short)(pom / 80.0);
                }
                if(i > (2*buffsize - 80)) {
                    pom = samplesSolo[i] * (2*buffsize-i);
                    samplesSolo[i] = (short)(pom / 80.0);
                }*/

                        samplesSolo[i] *= 0.7;

                        if (endSong) {
                            samplesSolo[i] *= 0.7;
                        }

                        if (!((countBars == 1) ||
                                (countBars == 2)
                        )) {
                            samples[i] += samplesSolo[i] * 2.9;
                        } else {
                            soloGenerator.setBeginning(false);
                        }


                        samples[i] *= 2.2;


                        if (mute) {
                            //System.out.println(" : " + samples[i]);
                            samples[i] = 0;//(short) (mutingFactor/200.0*(double)samples[i]);
                            if (mutingFactor > 0) mutingFactor = mutingFactor - 1;

                            //System.out.println("MMMMMUTINGFFFGFGG: " + mutingFactor);
                            //System.out.println(" : " + samples[i]);
                        }
                        //System.out.println(" : " + samples[i]);
                    }//end of synth loop


                    //long start55  = System.currentTimeMillis();

                    if (false) {
                        while (System.currentTimeMillis() < (noteEndTime + 200)) {
                            LockSupport.parkNanos(20_000_000);
                /*try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                        }
                        firstTime = false;
                    }

                    //System.out.println("MusicPlayer: vrijeme cekanja " + (System.currentTimeMillis() - start55));

                    if (mute) {/* && dieFinally){*/
                        isRunning = false;
                    } else {
                        //audioTrack.write(samples, 0, buffsize * 2);
                    }

                    //if (mutingFactor < 2) dieFinally = true;

                    //System.out.println("MusicPlayer: vrijeme nakon pisanja buffera  " + (System.currentTimeMillis() - start55));
                    //System.out.println(mute);


                    while (!synthFlag.get()) {
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Thread.yield();
                    }

                    samples2 = samples;
                    buffsize2 = buffsize;
                    noteDuration2 = noteDuration;

                    synthFlag.set(false);



            }
        }
    }












}

