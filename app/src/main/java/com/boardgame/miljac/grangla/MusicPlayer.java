package com.boardgame.miljac.grangla;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;


class MusicPlayer implements Runnable {
    int SR = 8000;//44100;
    double bassFrequency = 440.f;
    double soloFrequency = 440.f;
    double bassSlideFrequency = 440.f;
    double chordFrequency1, chordFrequency2, chordFrequency3 = 440.f;
    double sinArray[] = new double[1000];
    double TWOPI = 8. * Math.atan(1.);
    long noteStartTime = 0;
    long noteEndTime = 0;
    long noteDuration = 1000;
    long noteDuration2 = 1000;
    long noteDurationToWrite = 1000;
    boolean isMajor = false;

    int bassAmp = 30000;
    int chordAmp = 750;
    int pom;
    int soloTimeFrameDeviation;

    int i = 0;
    double bassPhase = 0.0;
    double bassOctavePhase = 0.0;
    double chord1Phase = 0.0;
    double chord2Phase = 0.0;
    double chord3Phase = 0.0;
    double soloPhase = 0.0;
    double phSoloFrBendFactor = 0.0;
    double chord1FrBendFactorFr = 0.0;
    double phChord1FrBendFactor = 0.0;
    double chord2FrBendFactorFr = 0.0;
    double phChord2FrBendFactor = 0.0;
    double chord3FrBendFactorFr = 0.0;
    double phChord3FrBendFactor = 0.0;
    double soloFrBendFactorFr = 0.0;

    double chord1FrBendFr, chord2FrBendFr, chord3FrBendFr;
    double PHASEFACTOR = 1000. / TWOPI;
    double phaseStep;
    double phaseStepSlide;
    double phaseStepOct;
    double phaseStepSlideOct;
    Note firstBassNote, secondBassNote, soloNote;
    int chordTone1, chordTone2, chordTone3;
    double rnd;

    BassGenerator bassGenerator = new BassGenerator();
    SoloGenerator soloGenerator = new SoloGenerator();

    AudioTrack audioTrack;
    short samples[], samples2[];
    short samplesChords1[], samplesChords2[], samplesChords3[], samplesToWrite[];
    short samplesSolo[], samplesOct[];
    double soloFrBendFactor;
    double chord1FrBendFactor, chord2FrBendFactor, chord3FrBendFactor;
    int buffsize, buffsize2, buffsizeToWrite;
    Random rand = new Random();
    volatile boolean mute = false;
    int countBars = 0;

    int index;
    AtomicBoolean synthFlag = new AtomicBoolean(true);
    Synth synth = new Synth();
    Thread synthThread;


    public MusicPlayer() {
        int minBuffSize;

        minBuffSize = AudioTrack.getMinBufferSize(
                SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (minBuffSize < 20000) {
            minBuffSize = 20000;
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SR, AudioFormat.CHANNEL_OUT_MONO,
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

        // start audio
        audioTrack.play();

        synthThread = new Thread(synth);
        synthThread.setPriority(Thread.MAX_PRIORITY);
        synthThread.start();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        long thisEndTime = 0;

        // synthesis loop
        while (!mute) {

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
        index = (int) (PHASEFACTOR * x);
        if (sinArray[index] == 0.0d) {
            sinArray[index] = Math.sin(x);
        }
        return sinArray[index];
    }


    public class Synth implements Runnable {

        public void initBassValues() {
            samples = new short[20000];
            firstBassNote = bassGenerator.getNextBassNote();
            secondBassNote = bassGenerator.getNextBassNote();

            bassFrequency = firstBassNote.getFrequency();
            if (firstBassNote.isSlide()) {//slide effect
                bassSlideFrequency = firstBassNote.getNextNoteFrequency();
            } else {
                bassSlideFrequency = bassFrequency;
            }

            bassAmp = firstBassNote.getVolume();
            calculateBassFrequencyValues(bassFrequency, bassSlideFrequency);
        }

        public void calculateNextNoteBassFrequencyValues() {
            calculateBassFrequencyValues(bassFrequency, bassFrequency);
        }

        public void calculateBassFrequencyValues(double bassFr, double bassFrSlide) {
            phaseStep = TWOPI * bassFr / SR;
            phaseStepSlide = TWOPI * bassFrSlide / SR;

            phaseStepOct = TWOPI * bassFr * 2.001 / SR;
            phaseStepSlideOct = TWOPI * bassFrSlide * 2.001 / SR;
        }

        public void calculateBassPhaseValues() {
            bassPhase += (phaseStep - phaseStepSlide) * ((double) i / (double) buffsize) + phaseStepSlide;
            if (bassPhase > TWOPI) bassPhase -= TWOPI;
            if (bassPhase < 0) bassPhase += TWOPI;

            bassOctavePhase += (phaseStepOct - phaseStepSlideOct) * ((double) i / (double) buffsize) + phaseStepSlideOct;
            if (bassOctavePhase > TWOPI) bassOctavePhase -= TWOPI;
            if (bassOctavePhase < 0) bassOctavePhase += TWOPI;
        }

        public void calculateBassModulations() {
            //fading rectangular distorsion
            if (samples[i] < 0) {
                samples[i] -= 35 * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize);
            } else {
                samples[i] += 35 * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize) * ((buffsize * 2 - i) / buffsize);
            }

            //fade out i fade in zbog krcanja
            if (i < 120) {
                pom = samples[i] * i;
                samples[i] = (short) (pom / 120.0);
            }
            if (i > (2 * buffsize - 80)) {
                pom = samples[i] * (2 * buffsize - i);
                samples[i] = (short) (pom / 80.0);
            }

            //partially cutting off tops of sine waves for distorsion
            if (samples[i] > (bassAmp * 0.8)) {
                samples[i] = (short) (bassAmp * 0.8 + (samples[i] - bassAmp * 0.8) * 0.6);
            }
            if (samples[i] < (0 - bassAmp * 0.8)) {
                samples[i] = (short) (0 - bassAmp * 0.8 + (samples[i] + bassAmp * 0.8) * 0.6);
            }

            //
            samples[i] = (short) (samples[i] * 0.7);


            //fade out i fade in zbog krcanja
            if (i < 120) {
                pom = samplesOct[i] * i;
                samplesOct[i] = (short) (pom / 120);
            }
            if (i > (2 * buffsize - 60)) {
                pom = samplesOct[i] * (2 * buffsize - i);
                samplesOct[i] = (short) (pom / 60);
            }
        }

        public void calculateKeysFromBass() {

            //get key and calculate other chord tones
            chordTone1 = bassGenerator.getCurrentKey() + 12;
            chordTone2 = chordTone1 + 3;
            if (chordTone2 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) chordTone2 -= 12;
            chordTone3 = chordTone1 + 7;
            if (chordTone3 > (TableConfig.BASS_NOTE_UPPER_BOUNDARY + 15)) chordTone3 -= 12;

            //maybe default minor chord should be turned to isMajor
            rnd = rand.nextDouble();
            if (rnd < 0.28) {
                chordTone1 -= 2;
                isMajor = true;
            } else {
                isMajor = false;
            }
            soloGenerator.setKey(chordTone2 - 3);
            soloGenerator.setMajor(isMajor);

            if (firstBassNote.isKeyChange()) {//when the bar is ended
                soloGenerator.setKeyChange(true);
            }

        }

        public void initChordsValues() {
            if (firstBassNote.isKeyChange()) chordAmp = 1350;

            if (firstBassNote.isKeyChange()) {//when the bar is ended
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

                chordFrequency1 = indexToFrequency(chordTone1);
                chordFrequency2 = indexToFrequency(chordTone2);
                chordFrequency3 = indexToFrequency(chordTone3);
            }

        }

        public void calculateChordsPhaseValues() {
            //bending of first tone in a chord
            chord1FrBendFactor = 1 + chord1FrBendFr * mySin(phChord1FrBendFactor);

            phChord1FrBendFactor += TWOPI * chord1FrBendFactorFr / SR;
            if (phChord1FrBendFactor > TWOPI) phChord1FrBendFactor -= TWOPI;

            chord1Phase += TWOPI * chordFrequency1 / SR * chord1FrBendFactor;
            if (chord1Phase > TWOPI) chord1Phase -= TWOPI;


            //bending of second tone in a chord
            chord2FrBendFactor = 1 + chord2FrBendFr * mySin(phChord2FrBendFactor);

            phChord2FrBendFactor += TWOPI * chord2FrBendFactorFr / SR;
            if (phChord2FrBendFactor > TWOPI) phChord2FrBendFactor -= TWOPI;

            chord2Phase += TWOPI * chordFrequency2 / SR * chord2FrBendFactor;
            if (chord2Phase > TWOPI) chord2Phase -= TWOPI;


            //bending of third tone in a chord
            chord3FrBendFactor = 1 + chord3FrBendFr * mySin(phChord3FrBendFactor);

            phChord3FrBendFactor += TWOPI * chord3FrBendFactorFr / SR;
            if (phChord3FrBendFactor > TWOPI) phChord3FrBendFactor -= TWOPI;

            chord3Phase += TWOPI * chordFrequency3 / SR * chord3FrBendFactor;
            if (chord3Phase > TWOPI) chord3Phase -= TWOPI;
        }

        public void calculateChordsModulations() {
            //long fading out
            if (chordAmp > 10) {
                if ((i % 70) == 0)
                    chordAmp *= 0.99;

            } else chordAmp = 0;

            //fade in
            if ((i < 100) && (firstBassNote.isKeyChange() || endSong)) {
                pom = samplesChords1[i] * i;
                samplesChords1[i] = (short) (pom / 100);
            }
            //second tone is moved later a bit
            if ((i < 200) && (firstBassNote.isKeyChange() || endSong)) {
                pom = samplesChords2[i] * (i - 100);
                if (i > 100)
                    samplesChords2[i] = (short) (pom / 100);
                else
                    samplesChords2[i] = 0;
            }
            //third note is moved a little more
            if ((i < 300) && (firstBassNote.isKeyChange() || endSong)) {
                pom = samplesChords3[i] * (i - 200);
                if (i > 200)
                    samplesChords3[i] = (short) (pom / 100);
                else
                    samplesChords3[i] = 0;
            }


            if (endSong && firstBassNote.isKeyChange()) {
                samplesChords1[i] = 0;
                samplesChords2[i] = 0;
                samplesChords3[i] = 0;
            }
        }

        public void initSoloValues() {
            soloNote = soloGenerator.getNextSoloNote();
            soloFrequency = soloNote.getFrequency();
            soloTimeFrameDeviation = soloNote.getSoloTimeFrameDeviation();
        }

        public void calculateSoloPhaseValues() {
            //BENDING
            soloFrBendFactorFr = soloNote.getSoloFrBendFr();

            soloFrBendFactor = 1 + soloNote.getSoloFrBendFactor() * mySin(phSoloFrBendFactor);

            phSoloFrBendFactor += TWOPI * soloFrBendFactorFr / SR;
            if (phSoloFrBendFactor > TWOPI) phSoloFrBendFactor -= TWOPI;


            soloPhase += TWOPI * soloFrequency / SR * soloFrBendFactor;
            if (soloPhase > TWOPI) soloPhase -= TWOPI;
        }

        public void calculateSoloModulations() {
            samplesSolo[i] *= 0.7;

            if (endSong) {
                samplesSolo[i] *= 0.7;
            }
        }


        public void calculateAllSinWaves() {
            //there are two bass sine waves, one is the base, and the other is an octave higher
            samples[i] = (short) (bassAmp * mySin(bassPhase) * (buffsize * 2 - i) / buffsize + bassAmp / 2);
            samplesOct[i] = (short) ((bassAmp * mySin(bassOctavePhase) * (buffsize * 2 - i) / buffsize + bassAmp / 2) / 20);

            samplesChords1[i] = (short) (chordAmp * mySin(chord1Phase));
            samplesChords2[i] = (short) (chordAmp * mySin(chord2Phase));
            samplesChords3[i] = (short) (chordAmp * mySin(chord3Phase));

            samplesSolo[i] = (short) (soloNote.getVolume() * mySin(soloPhase));
        }


        public void checkIfNextNotes() {
            //switch to second bass note
            if (i == buffsize) {
                bassFrequency = secondBassNote.getFrequency();
                calculateNextNoteBassFrequencyValues();
            }

            //switch to second solo note
            if (i == (buffsize + soloTimeFrameDeviation)) {
                soloNote = soloGenerator.getNextSoloNote();
                soloFrequency = soloNote.getFrequency();
            }
        }


        public void addAllSamples() {

            samples[i] += samplesOct[i];

            //no chords in the beginning
            if (countBars > 4) {
                samples[i] += samplesChords1[i];
                samples[i] += samplesChords2[i];
                samples[i] += samplesChords3[i];
            }


            if (!((countBars == 1) ||
                    (countBars == 2)
            )) {
                samples[i] += samplesSolo[i] * 2.9;
            } else {
                soloGenerator.setBeginning(false);
            }

            samples[i] *= 2.2;
        }

        public void run() {
            while (!mute) {
                noteStartTime = System.currentTimeMillis();
                noteEndTime = noteStartTime + noteDuration;
                buffsize = (int) noteDuration * 8;

                initBassValues();
                calculateKeysFromBass();
                initChordsValues();
                initSoloValues();

                if (firstBassNote.isKeyChange()) {//when the bar is ended
                    if (countBars < 10) countBars++;
                }

                //sample generation
                for (i = 0; i < buffsize * 2; i++) {
                    calculateBassPhaseValues();
                    calculateChordsPhaseValues();
                    calculateSoloPhaseValues();

                    calculateAllSinWaves();

                    calculateBassModulations();
                    calculateChordsModulations();
                    calculateSoloModulations();

                    addAllSamples();

                    checkIfNextNotes();

                    if (mute) {
                        samples[i] = 0;
                    }
                }//end of synth loop

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

