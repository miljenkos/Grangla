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
    double fr2 = 440.f;
    double sinArray[] = new double[1000];
    double twopi = 8. * Math.atan(1.);
    long start;
    long noteStartTime = 0;
    long noteEndTime = 0;
    long noteDuration = 1000;

    int amp = 30000;
    int bassLevel;
    int pom;

    //double fr = 440.f;
    double ph = 0.0;
    double ph2 = 0.0;

    double phaseFactor = 1000. / twopi;
    double phaseStep;

    BassGenerator bassGenerator = new BassGenerator();

    AudioTrack audioTrack;
    short samples[];
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
            phaseStep = twopi * bassFr / sr;

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
                System.out.println(n.getIndex());
                bassFr = n.getFrequency();
                amp = n.getVolume();
            //}
            System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr2 " + (start - System.currentTimeMillis()));

            //synchronized (samples) {
            buffsize = (int)noteDuration*8; //*8;
            System.out.println(noteDuration);
            //writing = true;
                for (int i = 0; i < buffsize*2; i++) {
                    index = (int) (phaseFactor * ph);
                    if (sinArray[index] == 0.0d) {
                        sinArray[index] = Math.sin(ph);
                    }
                    samples[i] = (short) (/*bassLevel*/ amp * sinArray[index] * (buffsize*2-i) / buffsize  + amp/2); //Math.sin(ph));
                    ph += phaseStep;
                    if (ph > twopi) ph -= twopi;


                    if(i < 60) {
                        pom = samples[i] * i;
                        samples[i] = (short)(pom / 60);
                    }
                    if(i > (2*buffsize - 60)) {
                        pom = samples[i] * (2*buffsize-i);
                        samples[i] = (short)(pom / 60);
                    }

                    if(i==(buffsize)) bassFr = n.getFrequency();
                    //ovo je drugi ton, bez sinusa
                /*samples[i] += (short) (amp * (ph2 - twopi/2));
                ph2 += twopi * fr2 / sr;
                if(ph2 > twopi) ph2 -= twopi;*/
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
            System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr3 " + (start - System.currentTimeMillis()));
            //bassLevel = (int) (amp - amp * (start - noteStartTime) / noteDuration / 2);

            //System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr5 " + (start - System.currentTimeMillis()));


            /*audioTrack.pause();
            audioTrack.flush();
            audioTrack.play();*/


            audioTrack.write(samples, 0, buffsize*2);




            /*System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr6 " + (start - System.currentTimeMillis()));
            long streamRefreshTime = (long)(((double)buffsize)/((double)sr) * 500);//250 bi bila cetvrtina vremena potrebnog za odsvirat buffer
            System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr7 " + (start - System.currentTimeMillis()));
            if (streamRefreshTime > TableConfig.DEFAULT_STREAM_REFRESH_TIME) streamRefreshTime = TableConfig.DEFAULT_STREAM_REFRESH_TIME;

            streamRefreshTime = 200;
            System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr6 srtr " + streamRefreshTime);*/

            while(System.currentTimeMillis()<noteEndTime) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("QWQWQWQWQWQWQWQWQWQWQWQWQWQWtfdjztdjdrdhtrdstartstarttstarsrstatsr4 " + (start - System.currentTimeMillis()));

        }
        audioTrack.stop();
        audioTrack.release();
    }



    public void stop() {
        isRunning = false;
    }

    public void setBassFrequency(double frequency) {
        this.bassFr = frequency;
    }

    public double getBassFrequency() {return bassFr;}

    public void setNoteDuration(long duration) {
        this.noteDuration = duration;
        System.out.println("DURATION: " + noteDuration);
    }

}

