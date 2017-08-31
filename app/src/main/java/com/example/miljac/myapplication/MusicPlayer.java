package com.example.miljac.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;
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
    boolean major = false;

    int amp = 30000;
    int chordAmp = 750;
    int soloAmp = 200;
    int bassLevel;
    int pom;
    int soloTimeFrameDeviation;

    //double fr = 440.f;
    double ph = 0.0;
    double phOct = 0.0;
    double ph2 = 0.0;
    double ph3 = 0.0;
    double ph4 = 0.0;
    double phSolo = 0.0;
    double phSoloFrBendFactor = 0.0;
    double soloFrBendFactorFr = 0.0;
    double phBassCh = 0.0;
    double phBassCh2 = 0.0;


    double phaseFactor = 1000. / twopi;
    double phaseStep;
    double phaseStepSlide;
    double phaseStepOct;
    double phaseStepSlideOct;

    BassGenerator bassGenerator = new BassGenerator();
    SoloGenerator soloGenerator = new SoloGenerator();

    AudioTrack audioTrack;
    short samples[];
    short samplesChords1[], samplesChords2[], samplesChords3[], samplesChordsBass1[], samplesOct[], samplesBassBase[];
    short samplesSolo[];
    double soloFrBendFactor;
    int buffsize;
    Random rand = new Random();

    public void run() {

        buffsize = 200;

        // create an audiotrack object
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 50000/*buffsize*/,
                AudioTrack.MODE_STREAM);

        samples = new short[20000];
        samplesChords1 = new short[20000];
        samplesChords2 = new short[20000];
        samplesChords3 = new short[20000];
        samplesChordsBass1 = new short[20000];
        samplesOct = new short[20000];
        samplesSolo = new short[20000];
        samplesBassBase = new short[20000];
        // start audio
        audioTrack.play();

        int index;
        start = System.currentTimeMillis();
        boolean beat = false;

        // synthesis loop
        while (isRunning) {

//BASS
//one iteration of this loop generates one bass beat, which contains two distinct Notes
            start = System.currentTimeMillis();
            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr " + (start));
            noteStartTime = start;
            noteEndTime = noteStartTime + noteDuration;
            if(beat){//first note in a bar?
                bassLevel = amp;
                beat = false;
            } else {
                beat = true;
            }
            Note n = bassGenerator.getNextBassNote();
            Note n2 = bassGenerator.getNextBassNote();
            bassFr = n.getFrequency();

            if(n.isSlide()) {//slide effect
                //System.out.println(n.getNextNoteIndex());
                bassFrSlide = n.getNextNoteFrequency();
            } else {
                bassFrSlide = bassFr;
            }

            amp = n.getVolume();
            if (n.isKeyChange()) chordAmp = 1600;
            //System.out.println(n.isKeyChange());

            //frequency calculations
            phaseStep = twopi * bassFr / sr;
            phaseStepSlide = twopi * bassFrSlide / sr;
            buffsize = (int)noteDuration*8;

            phaseStepOct = twopi * bassFr * 2 / sr;
            phaseStepSlideOct = twopi * bassFrSlide * 2 / sr;

            //get key and calculate other chord tones
            int key1 = bassGenerator.getKey() + 12;
            int key2 = key1 + 3;
            if (key2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key2 -= 12;
            int key3 = key1 + 7;
            if (key3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key3 -= 12;

            //maybe default minor chord should be turned to major


            double rnd = rand.nextDouble();
            if (rnd<0.25) {
                //key1 += 10;
                //if (key1 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key1 -= 12;
                key1-=2;
                //System.out.println("dur!");
                major = true;
            } else {
                major = false;
            }

            soloGenerator.setKey(key2-3);

            if (n.isKeyChange()){
                //System.out.println("KEYCHANGE");
                soloGenerator.setKeyChange(true);
            }
            Note s = soloGenerator.getNextSoloNote();
            soloFr = s.getFrequency();

            rnd = rand.nextDouble();
            soloTimeFrameDeviation = (int)(rnd*450) - 225;

            //sample generation
            for (int i = 0; i < buffsize*2; i++) {
//BASS
                index = (int) (phaseFactor * ph);//speed up sin calculating
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(ph);
                }
                samples[i] = (short) (amp * sinArray[index] * (buffsize*2-i) / buffsize  + amp/2);

                if(i<buffsize) {//first bass note
                    ph += (phaseStep - phaseStepSlide)*((double)i/(double)buffsize) + phaseStepSlide;
                } else {//second bass note
                    ph += phaseStep;
                }

                if (ph > twopi) ph -= twopi;
                if (ph < 0) ph += twopi;

                //fade out i fade in zbog krcanja
                if(i < 60) {
                    pom = samples[i] * i;
                    samples[i] = (short)(pom / 60);
                }
                if(i > (2*buffsize - 60)) {
                    pom = samples[i] * (2*buffsize-i);
                    samples[i] = (short)(pom / 60);
                }

                //switch to second bass note
                if(i==(buffsize)) {
                    bassFr = n2.getFrequency();
                    phaseStep = twopi * bassFr / sr;
                    phaseStepSlide = phaseStep;

                    phaseStepOct = twopi * bassFr * 2 / sr;
                    phaseStepSlideOct = phaseStepOct;
                }


//BASS octave
                index = (int) (phaseFactor * phOct);//speed up sin calculating
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(phOct);
                }
                samplesOct[i] = (short) (amp/55 * (
                        (sinArray[index] > 0) ? 1 : -1
                                                        )* (buffsize*2-i) / buffsize  + amp/55/2);

                if(i<buffsize) {//first bass note
                    phOct += (phaseStepOct - phaseStepSlideOct)*((double)i/(double)buffsize) + phaseStepSlideOct;
                } else {//second bass note
                    phOct += phaseStepOct;
                }

                if (phOct > twopi) phOct -= twopi;
                if (phOct < 0) phOct += twopi;

                //fade out i fade in zbog krcanja
                if(i < 60) {
                    pom = samplesOct[i] * i;
                    samplesOct[i] = (short)(pom / 60);
                }
                if(i > (2*buffsize - 60)) {
                    pom = samplesOct[i] * (2*buffsize-i);
                    samplesOct[i] = (short)(pom / 60);
                }

                samplesBassBase[i] = samples[i];
                samples[i] += samplesOct[i];


//CHORDS


                frBassCh = new Note(major?
                        bassGenerator.getKey()+3-12:
                        bassGenerator.getKey()-12).getFrequency();
                fr2 = new Note(key1).getFrequency();
                fr3 = new Note(key2).getFrequency();
                fr4 = new Note(key3).getFrequency();

                //bass part of the chord
                index = (int) (phaseFactor * phBassCh);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(phBassCh);
                }
                samplesChordsBass1[i] = (short) ( chordAmp * sinArray[index] );
                phBassCh += twopi * frBassCh / sr;
                if(phBassCh > twopi) phBassCh -= twopi;


                index = (int) (phaseFactor * phBassCh2);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(phBassCh2);
                }
                samplesChordsBass1[i] += (short) ( chordAmp / 3 * sinArray[index] );
                phBassCh2 += twopi * frBassCh * 1.5001 / sr;
                if(phBassCh2 > twopi) phBassCh2 -= twopi;

                if(samplesChordsBass1[i] > 0){
                    samplesChordsBass1[i] = (short) (230);
                } else {
                    samplesChordsBass1[i] = (short) (-230);

                }
                //System.out.println("GETKEYYYYYYY::  " + bassGenerator.getKey());


                //first tone in a chord
                index = (int) (phaseFactor * ph2);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(ph2);
                }
                samplesChords1[i] = (short) ( chordAmp * sinArray[index]);
                ph2 += twopi * fr2 / sr;
                if(ph2 > twopi) ph2 -= twopi;

                //second tone in a chord
                index = (int) (phaseFactor * ph3);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(ph3);
                }

                samplesChords2[i] = (short) ( chordAmp * sinArray[index]);

                ph3 += twopi * fr3 / sr;
                if(ph3 > twopi) ph3 -= twopi;

                //third note in a chord
                index = (int) (phaseFactor * ph4);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(ph4);
                }

                samplesChords3[i] = (short) ( chordAmp * sinArray[index]);
                ph4 += twopi * fr4 / sr;
                if(ph4 > twopi) ph4 -= twopi;

                //long fading out
                if (chordAmp>10) {
                    if ((i%70)==0)
                        chordAmp*= 0.99;

                } else chordAmp = 0;

                //fade in
                if((i < 100) && n.isKeyChange()) {
                    pom = samplesChords1[i] * i;
                    samplesChords1[i] = (short)(pom / 100);
                }
                //second tone is moved later a bit
                if((i < 200) && n.isKeyChange()) {
                    pom = samplesChords2[i] * (i-100);
                    if (i>100)
                        samplesChords2[i] = (short)(pom / 100);
                    else
                        samplesChords2[i] = 0;
                }
                //third note is moved a little more
                if((i < 300) && n.isKeyChange()) {
                    pom = samplesChords3[i] * (i-200);
                    if (i>200)
                        samplesChords3[i] = (short)(pom / 100);
                    else
                        samplesChords3[i] = 0;
                }

                samples[i] += samplesChords1[i];
                samples[i] += samplesChords2[i];
                samples[i] += samplesChords3[i];
                samples[i] += samplesChordsBass1[i];

                samples[i] += ((samplesChords1[i]
                        + samplesChords2[i]
                        + samplesChords3[i]
                        + samplesChordsBass1[i]
                        + samplesBassBase[i] /3) > 0) ?
                        170 : - 170;
                /*samples[i] +=
                        ((samplesChords1[i] + samplesChords2[i] + samplesChords3[i]) > 0) ?
                                chordAmp: -chordAmp;*/

//SOLO


                //switch to second solo note
                if(i == (buffsize + soloTimeFrameDeviation)) {
                    if (n.isKeyChange()){
                        //System.out.println("KEYCHANGE");
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
                if(phSoloFrBendFactor > twopi) phSoloFrBendFactor -= twopi;

                //System.out.println("\n\nSOLOFRBENDFACTOR:::   " + soloFrBendFactor);





                index = (int) (phaseFactor * phSolo);
                if (sinArray[index] == 0.0d) {
                    sinArray[index] = Math.sin(phSolo);
                }
                samplesSolo[i] = (short) ( s.getVolume() * (
                        (sinArray[index] > 0.3) ? 0.5 : sinArray[index]
                        //sinArray[index]
                ));
                phSolo += twopi * soloFr / sr * soloFrBendFactor;
                if(phSolo > twopi) phSolo -= twopi;

                samples[i] += samplesSolo[i]*2.9;


                samples[i] *= 2;
            }//end of synth loop



            long start55  = System.currentTimeMillis();

            while(System.currentTimeMillis()<(noteEndTime-6)) {
                LockSupport.parkNanos(2_000_000);
                /*try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

            System.out.println("MusicPlayer: vrijeme cekanja " + (System.currentTimeMillis() - start55));

            audioTrack.write(samples, 0, buffsize*2);

            System.out.println("MusicPlayer: vrijeme nakon pisanja buffera  " + (System.currentTimeMillis() - start55));

        }
        audioTrack.stop();
        audioTrack.release();
    }



    public void stop() {
        isRunning = false;
    }


    public void setNoteDuration(long duration) {
        this.noteDuration = duration;
    }

    public void setMeasure(int measure){
        bassGenerator.setMeasure(measure);
    }

}
