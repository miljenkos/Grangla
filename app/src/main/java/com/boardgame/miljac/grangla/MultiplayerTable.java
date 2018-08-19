package com.boardgame.miljac.grangla;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is where the actual game state is stored
 */
public class MultiplayerTable // igraca tabla
{
    private final Space[][] table = new Space[10][10];

    private double level;
    private double mistakeFactor;

    private long lastEventTime = 0;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock w = rwl.writeLock();

    private Coordinates lastMove = new Coordinates(5, 5);
    private long lastRockMove = 0;

    Random rn = new Random();

    private boolean isServer = true;

    public void setServer(boolean server) {
        isServer = server;
        if (!isServer) return;

        Boolean rockPut;
        for (int i = 0; i < (TableConfig.NO_OF_ROCKS); i++) {
            rockPut = false;
            while (!rockPut) {
                int x = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
                int y = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
                if (get(x, y) == State.empty) {
                    put(State.rock, x, y);
                    rockPut = true;
                }
            }

        }
    }

    /**
     * The default constructor. Initializes an empty board.
     */

    public MultiplayerTable(int l) {
        this.level = l;
        this.mistakeFactor = (1.2 * (level / 10 - 10) * (level / 10 - 10) * (level / 10 - 10) * (level / 10 - 10));

        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                (this.table[i][j]) = new Space();

            }
        }
    }

    /**
     * Puts a mark on the board.
     *
     * @param givenState a mark to be put.
     * @param i          x-coordinate of a field (space)
     * @param j          y-coordinate of a field (space)
     */

    public void put(State givenState, int i, int j)
    {
        i = (i + TableConfig.TABLE_SIZE * 2) % TableConfig.TABLE_SIZE;
        j = (j + TableConfig.TABLE_SIZE * 2) % TableConfig.TABLE_SIZE;
        (this.table[i][j]).setState(givenState);
    }

    public boolean publicPut(State givenState, int i, int j)
    {
        if (this.get(i, j) == State.empty) {
            w.lock();
            try {
                this.put(givenState, i, j);
            } finally {
                w.unlock();
            }
            lastMove = new Coordinates(i, j);
            return true;
        }
        return false;
    }


    public boolean publicEmpty(int i, int j)
    {
        if (this.get(i, j) != State.empty) {
            w.lock();
            try {
                this.put(State.empty, i, j);
            } finally {
                w.unlock();
            }
            lastMove = new Coordinates(i, j);
            return true;
        }
        return false;
    }

    /**
     * Finds out which mark is on the specific field on the board.
     *
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return a mark on the field with given coordinates
     */
    public State get(int i, int j) {
        int i2 = (i + TableConfig.TABLE_SIZE * 2) % TableConfig.TABLE_SIZE;
        int j2 = (j + TableConfig.TABLE_SIZE * 2) % TableConfig.TABLE_SIZE;
        try {
            return (this.table[i2][j2]).getState();
        } catch (Exception e) {
            return null;
        }
    }


    public State publicGet(int i, int j) {
        w.lock();
        try {

            int rr = (int) (rn.nextDouble() * TableConfig.ROCK_MOVEMENT_PROBABILITY);
            if ((rr == 1) &&
                    !((System.currentTimeMillis() - lastEventTime) < 200) &&
                    !((System.currentTimeMillis() - lastRockMove) < 2000)) {
                moveRock();
                lastRockMove = System.currentTimeMillis();
            }


            return this.get(i, j);

        } finally {
            w.unlock();
        }
    }

    /**
     * Detects how many instances of a specific sequence
     * on a board could be made by putting a mark on the
     * field with given coordinates.
     *
     * @param i        x-coordinate of a field (space)
     * @param j        y-coordinate of a field (space)
     * @param me       a mark being put on the board and tested
     * @param sequence an array containing the sequence, 1 represents a mark, and 0 represents an empty space
     * @return
     */

    private int detectSequence(int i, int j, State me, int[] sequence, int direction) {
        if (this.get(i, j) != State.empty) return 0;
        int result = 0;
        this.put(me, i, j);

        for (int begin = 0; begin < sequence.length; begin++) {
            int count[] = {0, 0, 0, 0};
            for (int x = 0; x < sequence.length; x++) {
                switch (direction) {
                    case 0:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j) == State.empty) && (sequence[x] == 0))    //vodoravno
                            count[0]++;
                        break;
                    case 1:
                        if (((this.get(i, j - (sequence.length - 1) + begin + x) == me) && (sequence[x] == 1)) ||
                                (this.get(i, j - (sequence.length - 1) + begin + x) == State.empty) && (sequence[x] == 0)) //okomito
                            count[1]++;
                        break;
                    case 2:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j - (sequence.length - 1) + begin + x) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j - (sequence.length - 1) + begin + x) == State.empty) && (sequence[x] == 0))
                            count[2]++;
                        break;
                    case 3:
                        if (((this.get(i - (sequence.length - 1) + begin + x, j + (sequence.length - 1) - begin - x) == me) && (sequence[x] == 1)) ||
                                (this.get(i - (sequence.length - 1) + begin + x, j + (sequence.length - 1) - begin - x) == State.empty) && (sequence[x] == 0))
                            count[3]++;
                        break;
                }
            }

            for (int x = 0; x < 4; x++)
                if (count[x] == sequence.length)
                    result++;
        }

        this.put(State.empty, i, j);
        return result;
    }


    /**
     * Checks if someone has collected a sequence, and removes it from the table
     * @return A number of points
     */
    public int getScore(int iC, int jC, long lastEventT) {
        w.lock();

        int result = 0;
        lastEventTime = lastEventT;
        try {

            State state = this.get(iC, jC);
            Boolean found = false;

            for (int counter = 0; counter < 4; counter++) {
                int k1, k2;
                switch (counter) {
                    case 0:
                        k1 = 1;
                        k2 = 0;
                        break;
                    case 1:
                        k1 = 0;
                        k2 = 1;
                        break;
                    case 2:
                        k1 = 1;
                        k2 = 1;
                        break;
                    default:
                        k1 = 1;
                        k2 = -1;
                        break;
                }

                int k22 = k2;
                if (k2 == 0) k22 = 1;
                int k11 = k1;
                if (k1 == 0) k11 = 1;

                for (int i = iC - 3 * k1; i != iC + k11; i = i + k11) {
                    for (int j = jC - 3 * k2; j != jC + k22; j = j + k22) {

                        if ((this.get(i, j).equals(state)) &&
                                (this.get(i + 1 * k1, j + 1 * k2).equals(state)) &&
                                (this.get(i + 2 * k1, j + 2 * k2).equals(state)) &&
                                (this.get(i + 3 * k1, j + 3 * k2).equals(state))) {

                            this.put(State.empty, i, j);
                            this.put(State.empty, i + 1 * k1, j + 1 * k2);
                            this.put(State.empty, i + 2 * k1, j + 2 * k2);
                            this.put(State.empty, i + 3 * k1, j + 3 * k2);
                            if (result == 0)
                                result += 9;
                            else
                                result += 10;

                            if (this.get(i + 4 * k1, j + 4 * k2).equals(state)) {
                                this.put(State.empty, i + 4 * k1, j + 4 * k2);
                                result += 4;
                                if (this.get(i + 5 * k1, j + 5 * k2).equals(state)) {
                                    this.put(State.empty, i + 5 * k1, j + 5 * k2);
                                    result += 4;
                                    if (this.get(i + 6 * k1, j + 6 * k2).equals(state)) {
                                        this.put(State.empty, i + 6 * k1, j + 6 * k2);
                                        result += 4;
                                    }
                                }
                            }
                            found = true;
                            this.put(state, iC, jC);
                        }
                    }
                }
            }

            if (found == true) {
                this.put(State.empty, iC, jC);
            }

        } finally {
            w.unlock();
        }
        return result;

    }

    void moveRock() {
        if (!isServer) {
            return;
        }

        int rockNo = (int) (Math.ceil(rn.nextDouble() * TableConfig.NO_OF_ROCKS) - 1);
        int c = 0;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if (this.get(i, j) == State.rock) {
                    if (c == rockNo) {
                        this.put(State.empty, i, j);
                    }
                    c++;
                }
            }
        }

        Boolean rockPut = false;
        while (!rockPut) {
            int x = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
            int y = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
            if (get(x, y) == State.empty) {
                put(State.rock, x, y);
                rockPut = true;
            }
        }
    }


    /**
     * Generates message data with the table state
     *
     */
    public byte[] getMsgBuff() {
        byte[] msgBuff = new byte[TableConfig.TABLE_SIZE * TableConfig.TABLE_SIZE + 6];
        int c = 0;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if (this.get(i, j) == State.rock) {
                    msgBuff[c] = 5;
                } else if (this.get(i, j) == State.empty) {
                    msgBuff[c] = 0;
                } else if (this.get(i, j) == State.circle) {
                    msgBuff[c] = 1;
                } else if (this.get(i, j) == State.cross) {
                    msgBuff[c] = 2;
                }
                c++;
            }
        }
        Log.d("grangla", "getMsgBuff:   " + new String(msgBuff));

        return msgBuff;
    }

    /**
     * Recieves the data from the message
     * Returns coordinates if the enemy has made a move.
     *
     */

    public Coordinates applyMsgBuff(byte[] msgBuff) {

        if (isServer) {
            Log.d("grangla", "JESAM SERVER");
        } else {
            Log.d("grangla", "NISAM SERVER");
        }

        int c = 0;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {

                if ((msgBuff[c] == 5) && (!isServer)) { //server sets rocks
                    this.put(State.rock, i, j);
                }

                if (this.get(i, j) == State.rock) {//on the rock enemy can put his figure or empty it only if is server
                    if (!isServer) {
                        if (msgBuff[c] == 1) {
                            this.put(State.cross, i, j);
                        }
                        if (msgBuff[c] == 0) {
                            this.put(State.empty, i, j);
                        }
                    }
                } else if (this.get(i, j) == State.empty) {//on an empty field enemy can put his own
                    if (msgBuff[c] == 1) {
                        this.put(State.cross, i, j);
                        return new Coordinates(i, j);
                    }
                } else if (this.get(i, j) == State.circle) {//putting on a taken filed - conflict!
                    if ((((i + j) % 2) == 0) ^ isServer) {//who has the advantage oin tihs field?
                        if (msgBuff[c] == 1) {
                            this.put(State.cross, i, j);
                            Coordinates co = new Coordinates(i, j);
                            co.conflict = true;
                            return co;
                        }
                    }
                } else if (this.get(i, j) == State.cross) {//enemy can remove his own figures
                    if (msgBuff[c] == 0) {
                        this.put(State.empty, i, j);
                    }
                }
                c++;
            }
        }
        Log.d("grangla", "applyMsgBuff: " + new String(msgBuff));

        return null;
    }
}





