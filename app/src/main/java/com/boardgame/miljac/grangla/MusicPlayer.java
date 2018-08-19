package com.boardgame.miljac.grangla;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;


class MusicPlayer implements Runnable {
    AudioTrack audioTrack;
    int SR = 8000;//44100;
    int buffsizeToWrite;
    short samplesToWrite[];
    AtomicBoolean synthFlag = new AtomicBoolean(true);
    Synth synth = new Synth(synthFlag);
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
        synth.endSong = true;
    }

    public boolean isEndSong() {
        return synth.endSong;
    }


    public void mute() {
        synth.mute = true;
    }

    public void run() {
        samplesToWrite = new short[20000];
        audioTrack.play();

        synthThread = new Thread(synth);
        synthThread.setPriority(Thread.MAX_PRIORITY);
        synthThread.start();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        long thisEndTime = 0;

        while (!synth.mute) {

            //if the new buffer is ready, get the new samplesToWrite, otherwise, the old one is used
            if (!synthFlag.get()) {
                samplesToWrite = synth.getSamplestoWrite();
                buffsizeToWrite = synth.getBuffsizeToWrite();
                synthFlag.set(true);
            }

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
        synth.setNoteDuration(duration);
    }

    public void setMeasure(int measure) {
        synth.setMeasure(measure);
    }


}

