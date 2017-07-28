package com.example.miljac.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;

/**
 * Created by miljac on 10.5.2017..
 */


class MusicPlayer implements Runnable {
    int sr = 8000;//44100;
    boolean isRunning = true;
    double bassFr = 440.f;
    double bassFrSlide = 440.f;
    double fr2, fr3, fr4 = 440.f;
    double sinArray[] = new double[1000];
    double twopi = 8. * Math.atan(1.);
    long start;
    long noteStartTime = 0;
    long noteEndTime = 0;
    long noteDuration = 1000;

    int amp = 30000;
    int chordAmp = 500;
    int bassLevel;
    int pom;

    //double fr = 440.f;
    double ph = 0.0;
    double ph2 = 0.0;
    double ph3 = 0.0;
    double ph4 = 0.0;

    double phaseFactor = 1000. / twopi;
    double phaseStep;
    double phaseStepSlide;

    BassGenerator bassGenerator = new BassGenerator();

    AudioTrack audioTrack;
    short samples[];
    short samplesChords1[], samplesChords2[], samplesChords3[];
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
                System.out.println(n.getNextNoteIndex());
                bassFrSlide = n.getNextNoteFrequency();
            } else {
                bassFrSlide = bassFr;
            }

            amp = n.getVolume();
            if (n.isKeyChange()) chordAmp = 1000;
            System.out.println(n.isKeyChange());

            //frequency calculations
            phaseStep = twopi * bassFr / sr;
            phaseStepSlide = twopi * bassFrSlide / sr;
            buffsize = (int)noteDuration*8;

            //get key and calculate other chord tones
            int key1 = bassGenerator.getKey() + 12;
            int key2 = key1 + 3;
            if (key2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key2 -= 12;
            int key3 = key1 + 7;
            if (key3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key3 -= 12;

            //maybe default minor chord should be turned to major


            double rnd = rand.nextDouble();
            if (rnd<0.2) {
                //key1 += 10;
                //if (key1 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key1 -= 12;
                key1-=2;
                //System.out.println("dur!");
            }

            //sample generation for bass
            for (int i = 0; i < buffsize*2; i++) {
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
                }

//CHORDS

                fr2 = new Note(key1).getFrequency();
                fr3 = new Note(key2).getFrequency();
                fr4 = new Note(key3).getFrequency();


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
                if((i < 600) && n.isKeyChange()) {
                    pom = samplesChords1[i] * i;
                    samplesChords1[i] = (short)(pom / 600);
                }
                //second tone is moved later a bit
                if((i < 800) && n.isKeyChange()) {
                    pom = samplesChords2[i] * (i-200);
                    if (i>200)
                        samplesChords2[i] = (short)(pom / 600);
                    else
                        samplesChords2[i] = 0;
                }
                //third note is moved a little more
                if((i < 1000) && n.isKeyChange()) {
                    pom = samplesChords3[i] * (i-400);
                    if (i>400)
                        samplesChords3[i] = (short)(pom / 600);
                    else
                        samplesChords3[i] = 0;
                }

                samples[i] += samplesChords1[i];
                samples[i] += samplesChords2[i];
                samples[i] += samplesChords3[i];
            }



            long start55  = System.currentTimeMillis();

            while(System.currentTimeMillis()<(noteEndTime-5)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

