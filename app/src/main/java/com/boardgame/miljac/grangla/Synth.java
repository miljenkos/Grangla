package com.boardgame.miljac.grangla;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;



public class Synth implements Runnable {
    AtomicBoolean synthFlag;
    int i = 0;
    int index;
    double bassPhase = 0.0;
    double bassOctavePhase = 0.0;
    double chord1Phase = 0.0;
    double chord2Phase = 0.0;
    double chord3Phase = 0.0;
    double soloPhase = 0.0;
    double soloBendFactorPhase = 0.0;
    double chord1BendFactorFrequency = 0.0;
    double chord1BendFactorPhase = 0.0;
    double chord2BendFactorFrequency = 0.0;
    double chord2BendFactorPhase = 0.0;
    double chord3BendFactorFrequency = 0.0;
    double chord3BendFactorPhase = 0.0;
    double soloBendFactorFrequency = 0.0;

    double TWOPI = 8. * Math.atan(1.);
    long noteStartTime = 0;
    long noteEndTime = 0;

    long noteDuration2 = 1000;
    boolean isMajor = false;

    int bassAmp = 30000;
    int chordAmp = 750;
    int pom;
    int soloTimeFrameDeviation;


    double chord1BendFactorAmp, chord2BendFactorAmp, chord3BendFactorAmp;
    double PHASEFACTOR = 1000. / TWOPI;
    double bassPhaseStep;
    double bassPhaseStepSlide;
    double bassOctavePhaseStep;
    double bassOctavePhaseStepSlide;
    Note firstBassNote, secondBassNote, soloNote;
    int chordTone1, chordTone2, chordTone3;
    double rnd;

    int SR = 8000;//44100;
    double bassFrequency = 440.f;
    double soloFrequency = 440.f;
    double bassSlideFrequency = 440.f;
    double chordFrequency1, chordFrequency2, chordFrequency3 = 440.f;
    double sinArray[] = new double[1000];


    short samples[], samples2[];
    short samplesChords1[], samplesChords2[], samplesChords3[];
    short samplesSolo[], samplesOct[];
    double soloBendFactor;
    double chord1BendFactor, chord2BendFactor, chord3BendFactor;
    int buffsize, buffsize2;
    Random rand = new Random();
    public volatile boolean mute = false;
    private long noteDuration = 1000;
    int countBars = 0;
    public boolean endSong = false;


    private BassGenerator bassGenerator = new BassGenerator();
    private SoloGenerator soloGenerator = new SoloGenerator();


    public Synth(AtomicBoolean synthFlagIn) {
        this.synthFlag = synthFlagIn;
    }

    public void setMeasure(int m) {
        bassGenerator.setMeasure(m);
    }

    public void setNoteDuration(long noteDuration) {
        this.noteDuration = noteDuration;
    }

    public int getBuffsizeToWrite() {
        return this.buffsize2;
    }

    public short[] getSamplestoWrite() {
        return samples2;
    }

    private double indexToFrequency(int x) {
        if (x == 1) return 32.7;
        if (x > 12) return (2 * indexToFrequency(x - 12));//12 semitones make an octave
        return 1.059463094359 * indexToFrequency(x - 1); //12th root of 2 is the step for the next semitone
    }

    private double mySin(double x) {//speed up sin calculating
        index = (int) (PHASEFACTOR * x);
        if (sinArray[index] == 0.0d) {
            sinArray[index] = Math.sin(x);
        }
        return sinArray[index];
    }

    private void initBassValues() {
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

    private void calculateNextNoteBassFrequencyValues() {
        calculateBassFrequencyValues(bassFrequency, bassFrequency);
    }

    private void calculateBassFrequencyValues(double bassFr, double bassFrSlide) {
        bassPhaseStep = TWOPI * bassFr / SR;
        bassPhaseStepSlide = TWOPI * bassFrSlide / SR;

        bassOctavePhaseStep = TWOPI * bassFr * 2.001 / SR;
        bassOctavePhaseStepSlide = TWOPI * bassFrSlide * 2.001 / SR;
    }

    private void calculateBassPhaseValues() {
        bassPhase += (bassPhaseStep - bassPhaseStepSlide) * ((double) i / (double) buffsize) + bassPhaseStepSlide;
        if (bassPhase > TWOPI) bassPhase -= TWOPI;
        if (bassPhase < 0) bassPhase += TWOPI;

        bassOctavePhase += (bassOctavePhaseStep - bassOctavePhaseStepSlide) * ((double) i / (double) buffsize) + bassOctavePhaseStepSlide;
        if (bassOctavePhase > TWOPI) bassOctavePhase -= TWOPI;
        if (bassOctavePhase < 0) bassOctavePhase += TWOPI;
    }

    private void calculateBassModulations() {
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

    private void calculateKeysFromBass() {

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

    private void initChordsValues() {
        if (firstBassNote.isKeyChange()) chordAmp = 1350;

        if (firstBassNote.isKeyChange()) {//when the bar is ended
            rnd = rand.nextDouble();
            chord1BendFactorAmp = (((18.0 / 17.0) - 1.0) / 14) * rnd * rnd * 2.8;
            rnd = rand.nextDouble();
            chord1BendFactorFrequency = 2 + rnd * rnd * 11;
            rnd = rand.nextDouble();
            chord2BendFactorAmp = (((18.0 / 17.0) - 1.0) / 14) * rnd * 1.8;
            rnd = rand.nextDouble();
            chord2BendFactorFrequency = 0.4 + rnd * 5;
            rnd = rand.nextDouble();
            chord3BendFactorAmp = (((18.0 / 17.0) - 1.0) / 14) * rnd * 2;
            rnd = rand.nextDouble();
            chord3BendFactorFrequency = 0.5 + rnd * 7;

            chordFrequency1 = indexToFrequency(chordTone1);
            chordFrequency2 = indexToFrequency(chordTone2);
            chordFrequency3 = indexToFrequency(chordTone3);
        }

    }

    private double nextPhase(double currentPhase, double frequency) {
        currentPhase += TWOPI * frequency / SR;
        if (currentPhase > TWOPI) currentPhase -= TWOPI;
        return currentPhase;
    }

    private void calculateChordsPhaseValues() {
        //a tone in a chord with bending (more like tremolo)
        chord1BendFactor = 1 + chord1BendFactorAmp * mySin(chord1BendFactorPhase);
        chord1BendFactorPhase = nextPhase(chord1BendFactorPhase, chord1BendFactorFrequency);
        chord1Phase = nextPhase(chord1Phase, chordFrequency1 * chord1BendFactor);

        chord2BendFactor = 1 + chord2BendFactorAmp * mySin(chord2BendFactorPhase);
        chord2BendFactorPhase = nextPhase(chord2BendFactorPhase, chord2BendFactorFrequency);
        chord2Phase = nextPhase(chord2Phase, chordFrequency2 * chord2BendFactor);

        //bending of third tone in a chord
        chord3BendFactor = 1 + chord3BendFactorAmp * mySin(chord3BendFactorPhase);
        chord3BendFactorPhase = nextPhase(chord3BendFactorPhase, chord3BendFactorFrequency);
        chord3Phase = nextPhase(chord3Phase, chordFrequency3 * chord3BendFactor);
    }

    private void calculateChordsModulations() {
        //long fading out
        if (chordAmp > 10) {
            if ((i % 70) == 0)
                chordAmp *= 0.99;

        } else chordAmp = 0;

        //if it is a moment to hit another chord
        if (firstBassNote.isKeyChange() || endSong) {
            //fade in
            if (i < 100) {
                pom = samplesChords1[i] * i;
                samplesChords1[i] = (short) (pom / 100);
            }
            //second tone is moved later a bit
            if (i < 200) {
                pom = samplesChords2[i] * (i - 100);
                if (i > 100)
                    samplesChords2[i] = (short) (pom / 100);
                else
                    samplesChords2[i] = 0;
            }
            //third note is moved a little more
            if (i < 300) {
                pom = samplesChords3[i] * (i - 200);
                if (i > 200)
                    samplesChords3[i] = (short) (pom / 100);
                else
                    samplesChords3[i] = 0;
            }
        }

        if (endSong && firstBassNote.isKeyChange()) {
            samplesChords1[i] = 0;
            samplesChords2[i] = 0;
            samplesChords3[i] = 0;
        }
    }

    private void initSoloValues() {
        soloNote = soloGenerator.getNextSoloNote();
        soloFrequency = soloNote.getFrequency();
        soloTimeFrameDeviation = soloNote.getSoloTimeFrameDeviation();
    }

    private void calculateSoloPhaseValues() {
        //bending/tremolo
        soloBendFactorFrequency = soloNote.getBendFactorFrequency();

        soloBendFactor = 1 + soloNote.getSoloFrBendFactor() * mySin(soloBendFactorPhase);
        soloBendFactorPhase = nextPhase(soloBendFactorPhase, soloBendFactorFrequency);
        soloPhase = nextPhase(soloPhase, soloFrequency * soloBendFactor);
    }

    private void calculateSoloModulations() {
        samplesSolo[i] *= 0.7;

        if (endSong) {
            samplesSolo[i] *= 0.7;
        }
    }


    private void calculateAllSinWaves() {
        //there are two bass sine waves, one is the base, and the other is an octave higher
        samples[i] = (short) (bassAmp * mySin(bassPhase) * (buffsize * 2 - i) / buffsize + bassAmp / 2);
        samplesOct[i] = (short) ((bassAmp * mySin(bassOctavePhase) * (buffsize * 2 - i) / buffsize + bassAmp / 2) / 20);

        samplesChords1[i] = (short) (chordAmp * mySin(chord1Phase));
        samplesChords2[i] = (short) (chordAmp * mySin(chord2Phase));
        samplesChords3[i] = (short) (chordAmp * mySin(chord3Phase));

        samplesSolo[i] = (short) (soloNote.getVolume() * mySin(soloPhase));
    }


    private void checkIfNextNotes() {
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


    private void addAllSamples() {

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
        buffsize = 200;
        samples = new short[20000];
        samples2 = new short[20000];
        samplesChords1 = new short[20000];
        samplesChords2 = new short[20000];
        samplesChords3 = new short[20000];
        samplesOct = new short[20000];
        samplesSolo = new short[20000];


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
