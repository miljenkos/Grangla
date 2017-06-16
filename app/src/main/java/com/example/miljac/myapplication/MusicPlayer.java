package com.example.miljac.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

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
    short samplesChords[];
    int buffsize;

    boolean writing = false;


    public void run() {


        // set process priority
        //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);




        // set the buffer size
        //buffsize = AudioTrack.getMinBufferSize(sr,
        //        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        buffsize = 200;


        // create an audiotrack object
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 50000/*buffsize*/,
                AudioTrack.MODE_STREAM);

        samples = new short[20000];
        samplesChords = new short[20000];
        // start audio
        audioTrack.play();

        int index;
        double ph1 = 0;
        start = System.currentTimeMillis();
        boolean beat = false;



        /*Thread thread = new Thread(){
            public void run() {
                //synchronized (samples) {
                    // set the buffer size
                    //buffsize = AudioTrack.getMinBufferSize(sr,
                    //        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                    buffsize = 200;

                    // create an audiotrack object

                    AudioTrack audioTrack2;
                    audioTrack2 = new AudioTrack(AudioManager.STREAM_MUSIC,
                            sr, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, buffsize,
                            AudioTrack.MODE_STREAM);

                    // start audio
                    //audioTrack2.play();

                    while (isRunning) {

                        audioTrack.write(samples, 0, buffsize);

                        while(writing) {
                            try {
                                Thread.sleep(0,1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
           //}
        };
        thread.start();*/



        // synthesis loop
        while (isRunning) {


            start = System.currentTimeMillis();
            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr " + (start));
            //if (start > (noteStartTime + noteDuration)) {
                noteStartTime = start;
                noteEndTime = noteStartTime + noteDuration;
                if(beat){
                    bassLevel = amp;
                    beat = false;
                } else {
                    beat = true;
                }
                Note n = bassGenerator.getNextBassNote();
                Note n2 = bassGenerator.getNextBassNote();
                //System.out.println(n.getIndex());
                bassFr = n.getFrequency();

                //if(n.getNextNoteIndex() != -20) {
                if(n.isSlide()) {
                    System.out.println(n.getNextNoteIndex());
                    bassFrSlide = n.getNextNoteFrequency();
                } else {
                    bassFrSlide = bassFr;
                }


            System.out.println("BASSFRSLIDE " + bassFrSlide + " BASSFR " + bassFr);

                amp = n.getVolume();
            if (n.isKeyChange()) chordAmp = 1000;
            System.out.println(n.isKeyChange());


            phaseStep = twopi * bassFr / sr;
            phaseStepSlide = twopi * bassFrSlide / sr;
            //}
            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr2 " + (start - System.currentTimeMillis()));

            //synchronized (samples) {
            buffsize = (int)noteDuration*8; //*8;
            //System.out.println(noteDuration);
            //writing = true;

            int key1 = bassGenerator.getKey() + 12;
            int key2 = key1 + 3;
            if (key2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key2 -= 12;
            int key3 = key1 + 7;
            if (key3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key3 -= 12;

            fr2 = new Note(key1).getFrequency();
            fr3 = new Note(key2).getFrequency();
            fr4 = new Note(key3).getFrequency();

                for (int i = 0; i < buffsize*2; i++) {
                    index = (int) (phaseFactor * ph);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(ph);
                    }
                    samples[i] = (short) (/*bassLevel*/ amp * sinArray[index] * (buffsize*2-i) / buffsize  + amp/2); //Math.sin(ph));

                    if(i<buffsize) {
                        ph += (phaseStep - phaseStepSlide)*((double)i/(double)buffsize) + phaseStepSlide;
                    } else {
                        ph += phaseStep;
                    }//ph += phaseStep;

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

                    if(i==(buffsize)) {
                        bassFr = n2.getFrequency();
                        phaseStep = twopi * bassFr / sr;
                        phaseStepSlide = phaseStep;
                    }


                //SAMO PROBNO DA VIDIM KAK SE AKORDI MIJENJAJU LAKSE
                    //ovo je drugi ton, bez sinusa
                    index = (int) (phaseFactor * ph2);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(ph2);
                    }
                    samplesChords[i] = (short) ( chordAmp * sinArray[index]); //Math.sin(ph));
                //samples[i] += (short) (5000 * (ph2 - twopi/2)/30);
                ph2 += twopi * fr2 / sr;
                if(ph2 > twopi) ph2 -= twopi;

                    //SAMO PROBNO DA VIDIM KAK SE AKORDI MIJENJAJU LAKSE
                    //ovo je drugi ton, bez sinusa
                    index = (int) (phaseFactor * ph3);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(ph3);
                    }
                    samplesChords[i] += (short) ( chordAmp * sinArray[index]); //Math.sin(ph));
                    //samples[i] += (short) (5000 * (ph2 - twopi/2)/30);
                    ph3 += twopi * fr3 / sr;
                    if(ph3 > twopi) ph3 -= twopi;

                    //SAMO PROBNO DA VIDIM KAK SE AKORDI MIJENJAJU LAKSE
                    //ovo je drugi ton, bez sinusa
                    index = (int) (phaseFactor * ph4);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(ph4);
                    }
                    samplesChords[i] += (short) ( chordAmp * sinArray[index]); //Math.sin(ph));
                    //samples[i] += (short) (5000 * (ph2 - twopi/2)/30);
                    ph4 += twopi * fr4 / sr;
                    if(ph4 > twopi) ph4 -= twopi;

                    if (chordAmp>10) {
                        if ((i%70)==0)
                        chordAmp*= 0.99;

                    } else chordAmp = 0;

                    //fade in
                    if((i < 900) && n.isKeyChange()) {
                        pom = samplesChords[i] * i;
                        samplesChords[i] = (short)(pom / 900);
                    }


                    samples[i] += samplesChords[i];
                }

//            //drugi ton, da budu po dva spojena
//
//
//            bassFr = n2.getFrequency();
//            amp = n2.getVolume(); amp=0;
//            for (int i = buffsize; i < buffsize*2; i++) {
//                index = (int) (phaseFactor * ph);
//                if (sinArray[index] == 0.0d) {
//                    sinArray[index] = Math.sin(ph);
//                }
//                samples[i] = (short) (/*bassLevel*/ amp * sinArray[index] /* * (buffsize*2-i) / buffsize*/); //Math.sin(ph));
//                ph += phaseStep;
//                if (ph > twopi) ph -= twopi;
//
//                if(i > (buffsize - 60)) {
//                    pom = samples[i] * (2*buffsize-i);
//                    samples[i] = (short)(pom / 60);
//                }
//
//
//
//                //ovo je drugi ton, bez sinusa
//                /*samples[i] += (short) (amp * (ph2 - twopi/2));
//                ph2 += twopi * fr2 / sr;
//                if(ph2 > twopi) ph2 -= twopi;*/
//            }
            //writing = false;
            //}
            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr3 " + (start - System.currentTimeMillis()));
            //bassLevel = (int) (amp - amp * (start - noteStartTime) / noteDuration / 2);

            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr5 " + (start - System.currentTimeMillis()));


            /*audioTrack.pause();
            audioTrack.flush();
            audioTrack.play();*/






            while(System.currentTimeMillis()<noteEndTime) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            audioTrack.write(samples, 0, buffsize*2);

            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr4 " + (start - System.currentTimeMillis()));

        }
        audioTrack.stop();
        audioTrack.release();
    }



    public void stop() {
        isRunning = false;
    }


    public void setNoteDuration(long duration) {
        this.noteDuration = duration;
        //System.out.println("DURATION: " + noteDuration);
    }

    public void setMeasure(int measure){
        bassGenerator.setMeasure(measure);
    }

}

