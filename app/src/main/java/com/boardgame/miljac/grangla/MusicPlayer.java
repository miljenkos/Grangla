package com.boardgame.miljac.grangla;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;


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
    int bassLevel;
    int pom;
    int soloTimeFrameDeviation;
    double mutingFactor = 200.0;

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

    double chord1FrBendFr, chord2FrBendFr, chord3FrBendFr;
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
    int countBars = 0;

    int index;
    boolean firstTime = true;
    AtomicBoolean synthFlag = new AtomicBoolean(true);
    Synth synth = new Synth();
    Thread synthThread;


    public MusicPlayer() {
        int minBuffSize;

        minBuffSize = AudioTrack.getMinBufferSize(
                sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (minBuffSize < 20000) {
            minBuffSize = 20000;
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
                AudioTrack.MODE_STREAM);
    }

    public void setEndSong() {
        this.endSong = true;
    }

    public boolean isEndSong() {
        return endSong;
    }

    boolean endSong = false;

    public void mute() {
        mute = true;
    }

    public void run() {
        buffsize = 200;
        samplesToWrite = new short[20000];
        samples = new short[20000];
        samples2 = new short[20000];
        samplesChords1 = new short[20000];
        samplesChords2 = new short[20000];
        samplesChords3 = new short[20000];
        samplesOct = new short[20000];
        samplesSolo = new short[20000];
        samplesBassBase = new short[20000];

        // start audio
        audioTrack.play();

        start = System.currentTimeMillis();

        synthThread = new Thread(synth);
        synthThread.setPriority(Thread.MAX_PRIORITY);
        synthThread.start();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        long thisEndTime = 0;

        // synthesis loop
        while (isRunning) {
            if (mute) {
                isRunning = false;
            } else {
                //if the new buffer is ready, get the new samplesToWrite, otherwise, the old one is used
                if (!synthFlag.get()) {
                    samplesToWrite = samples2;
                    samples2 = new short[20000];
                    buffsizeToWrite = buffsize2;
                    noteDurationToWrite = noteDuration2;
                    synthFlag.set(true);
                }

                if (buffsizeToWrite == 0) continue;

                while (System.currentTimeMillis() < (thisEndTime)) {
                    LockSupport.parkNanos(1_000_000);
                }

                audioTrack.write(samplesToWrite, 0, buffsizeToWrite * 2);
                thisEndTime = System.currentTimeMillis() + buffsizeToWrite / 8 * 2;
            }
        }
        audioTrack.pause();
        audioTrack.release();
    }


    public void setNoteDuration(long duration) {
        this.noteDuration = duration;
    }

    public void setMeasure(int measure) {
        bassGenerator.setMeasure(measure);
    }

    private double indexToFrequency(int x) {
        if (x == 1) return 32.7;
        if (x > 12) return (2 * indexToFrequency(x - 12));//12 semitones make an octave
        return 1.059463094359 * indexToFrequency(x - 1); //12th root of 2 is the step for the next semitone
    }


    public double mySin(double x) {//speed up sin calculating
        index = (int) (phaseFactor * x);
        if (sinArray[index] == 0.0d) {
            sinArray[index] = Math.sin(x);
        }
        return sinArray[index];
    }



    public class Synth implements Runnable {
        public void run() {


            while (isRunning) {
                samples = new short[20000];

//BASS
//one iteration of this loop generates one bass beat, which contains two distinct Notes
                start = System.currentTimeMillis();
                noteStartTime = start;
                noteEndTime = noteStartTime + noteDuration;
                Note n = bassGenerator.getNextBassNote();
                Note n2 = bassGenerator.getNextBassNote();
                bassFr = n.getFrequency();

                if (n.isSlide()) {//slide effect
                    bassFrSlide = n.getNextNoteFrequency();
                } else {
                    bassFrSlide = bassFr;
                }

                amp = n.getVolume();
                if (n.isKeyChange()) chordAmp = 1350;

                //frequency calculations
                phaseStep = twopi * bassFr / sr;
                phaseStepSlide = twopi * bassFrSlide / sr;
                buffsize = (int) noteDuration * 8;

                phaseStepOct = twopi * bassFr * 2.001 / sr;
                phaseStepSlideOct = twopi * bassFrSlide * 2.001 / sr;

                //get key and calculate other chord tones
                int key1 = bassGenerator.getKey() + 12;
                int key2 = key1 + 3;
                if (key2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key2 -= 12;
                int key3 = key1 + 7;
                if (key3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) key3 -= 12;

                //maybe default minor chord should be turned to major
                double rnd = rand.nextDouble();
                if (rnd < 0.28) {
                    key1 -= 2;
                    major = true;
                } else {
                    major = false;
                }
                soloGenerator.setKey(key2 - 3);
                soloGenerator.setMajor(major);

                if (n.isKeyChange()) {//when the bar is ended
                    soloGenerator.setKeyChange(true);
                    if (countBars < 10) countBars++;

                    rnd = rand.nextDouble();
                    chord1FrBendFr = (((18.0 / 17.0) - 1.0) / 14) * rnd * rnd * 2.8;
                    rnd = rand.nextDouble();
                    chord1FrBendFactorFr = 2 + rnd * rnd * 11;
                    rnd = rand.nextDouble();
                    chord2FrBendFr = (((18.0 / 17.0) - 1.0) / 14) * rnd * 1.8;
                    rnd = rand.nextDouble();
                    chord2FrBendFactorFr = 0.4 + rnd * 5;
                    rnd = rand.nextDouble();
                    chord3FrBendFr = (((18.0 / 17.0) - 1.0) / 14) * rnd * 2;
                    rnd = rand.nextDouble();
                    chord3FrBendFactorFr = 0.5 + rnd * 7;
                }

                Note s = soloGenerator.getNextSoloNote();
                soloFr = s.getFrequency();
                soloTimeFrameDeviation = s.getSoloTimeFrameDeviation();


                //sample generation
                for (int i = 0; i < buffsize * 2; i++) {
//BASS
                    samples[i] = (short) (amp * mySin(ph) * (buffsize * 2 - i) / buffsize + amp / 2);

                    if (i < buffsize) {//first bass note
                        ph += (phaseStep - phaseStepSlide) * ((double) i / (double) buffsize) + phaseStepSlide;
                    } else {//second bass note
                        ph += phaseStep;
                    }

                    //
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


                    if (samples[i] > (amp * 0.8)) {
                        samples[i] = (short) (amp * 0.8 + (samples[i] - amp * 0.8) * 0.6);
                    }
                    if (samples[i] < (0 - amp * 0.8)) {
                        samples[i] = (short) (0 - amp * 0.8 + (samples[i] + amp * 0.8) * 0.6);
                    }

                    samples[i] = (short) (samples[i] * 0.7);


//BASS octave
                    if (true) {
                        samplesOct[i] = (short) ((amp * mySin(phOct) * (buffsize * 2 - i) / buffsize + amp / 2) / 20);

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

                    if (true) {


                        fr2 = indexToFrequency(key1);
                        fr3 = indexToFrequency(key2);
                        fr4 = indexToFrequency(key3);


                        //bending of first tone in a chord
                        chord1FrBendFactor = 1 + chord1FrBendFr * mySin(phChord1FrBendFactor);

                        phChord1FrBendFactor += twopi * chord1FrBendFactorFr / sr;
                        if (phChord1FrBendFactor > twopi) phChord1FrBendFactor -= twopi;


                        //first tone in a chord
                        samplesChords1[i] = (short) (chordAmp * mySin(ph2));
                        ph2 += twopi * fr2 / sr * chord1FrBendFactor;
                        if (ph2 > twopi) ph2 -= twopi;


                        //bending of second tone in a chord
                        chord2FrBendFactor = 1 + chord2FrBendFr * mySin(phChord2FrBendFactor);

                        phChord2FrBendFactor += twopi * chord2FrBendFactorFr / sr;
                        if (phChord2FrBendFactor > twopi) phChord2FrBendFactor -= twopi;

                        //second tone in a chord
                        samplesChords2[i] = (short) (chordAmp * mySin(ph3));

                        ph3 += twopi * fr3 / sr * chord2FrBendFactor;
                        if (ph3 > twopi) ph3 -= twopi;


                        //bending of third tone in a chord
                        chord3FrBendFactor = 1 + chord3FrBendFr * mySin(phChord3FrBendFactor);

                        phChord3FrBendFactor += twopi * chord3FrBendFactorFr / sr;
                        if (phChord3FrBendFactor > twopi) phChord3FrBendFactor -= twopi;
                        //third note in a chord
                        samplesChords3[i] = (short) (chordAmp * mySin(ph4));
                        ph4 += twopi * fr4 / sr * chord3FrBendFactor;
                        ;
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

                        if (endSong && n.isKeyChange()) {
                            samplesChords1[i] = 0;
                            samplesChords2[i] = 0;
                            samplesChords3[i] = 0;
                            samplesBassBase[i] *= 3;
                        }
                        if (endSong && !n.isKeyChange()) {
                            samplesBassBase[i] = 0;
                        }


                        if ((countBars == 1) || (countBars == 2) || (countBars == 3) || (countBars == 4)) {

                        } else {
                            samples[i] += samplesChords1[i];
                            samples[i] += samplesChords2[i];
                            samples[i] += samplesChords3[i];
                        }
                    }

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

                    soloFrBendFactor = 1 + s.getSoloFrBendFactor() * mySin(phSoloFrBendFactor);

                    phSoloFrBendFactor += twopi * soloFrBendFactorFr / sr;
                    if (phSoloFrBendFactor > twopi) phSoloFrBendFactor -= twopi;

                    samplesSolo[i] = (short) (s.getVolume() * mySin(phSolo));
                    phSolo += twopi * soloFr / sr * soloFrBendFactor;
                    if (phSolo > twopi) phSolo -= twopi;


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
                        samples[i] = 0;
                        if (mutingFactor > 0) mutingFactor = mutingFactor - 1;
                    }
                }//end of synth loop


                if (false) {
                    while (System.currentTimeMillis() < (noteEndTime + 200)) {
                        LockSupport.parkNanos(20_000_000);
                    }
                    firstTime = false;
                }

                if (mute) {
                    isRunning = false;
                }


                while (!synthFlag.get()) {
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                samples2 = samples;
                buffsize2 = buffsize;
                noteDuration2 = noteDuration;

                synthFlag.set(false);

            }
        }
    }
}

