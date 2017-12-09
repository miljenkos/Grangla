package com.boardgame.miljac.grangla;

/**
 * Created by miljac on 24.1.2017..
 */

//import java.util.List;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is where the actual game happens.
 * @author miljac
 *
 */
public class MultiplayerTable // igraca tabla
{
    private final Space[][] table = new Space[10][10];

    private final int[] no=           {0,0,0,1,0,0,0};
    private final int[] win6=         {1,1,1,1,1,1};
    private final int[] win5=         {1,1,1,1,1,1};
    private final int[] win=          {1,1,1,1};

    private final int[] probablyMust= {0,1,1,1,0};

    private final int[] three1=       {0,1,1,1};
    private final int[] three2=       {1,0,1,1};
    private final int[] three3=       {1,1,0,1};
    private final int[] three4=       {1,1,1,0};

    private final int[] two1=          {0,1,1,0,0};
    private final int[] two2=          {0,0,1,1,0};
    private final int[] two3=          {0,1,0,1,0};

    private final int[] shittierTwo1= {0,1,0,1};
    private final int[] shittierTwo2= {1,0,1,0};
    private final int[] shittierTwo3= {1,1,0,0};
    private final int[] shittierTwo4= {0,1,1,0};
    private final int[] shittierTwo5= {0,0,1,1};
    private final int[] shittierTwo6= {1,0,0,1};

    private double level;
    private double mistakeFactor;

    private long lastEventTime = 0;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private Coordinates lastMove = new Coordinates(5,5);
    private long lastRockMove = 0;

    Random rn = new Random();

    private boolean isServer = true;

    public boolean isServer() {
        return isServer;
    }

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

    public MultiplayerTable(int l)
    {
        //this.table = new ArrayList<ArrayList<Space>>();
        this.level = l;
        this.mistakeFactor = (1.2 * (level/10 - 10)*(level/10 - 10)*(level/10 - 10)*(level/10 - 10));

        for (int i=0; i<TableConfig.TABLE_SIZE; i++)
        {
            for (int j=0; j<TableConfig.TABLE_SIZE; j++){
                (this.table[i][j]) = new Space();
                /*System.out.println("TTTTT");
                System.out.println((this.table[i][j]).getState().toString());*/
                //System.out.println(this.get(i,j).toString());
                //System.out.println();

            }
        }

        /*Boolean rockPut;
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

        }*/

    }

    /**
     * Puts a mark on the board.
     * @param givenState a mark to be put.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     */

    private void put(State givenState, int i, int j)  // stavlja stanje givenState na polje koordinata (i,j)
    {
        i = (i + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        j = (j + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        (this.table[i][j]).setState(givenState);
    }

    public boolean publicPut(State givenState, int i, int j)  // stavlja stanje givenState na polje koordinata (i,j)
    {
        if(this.get(i,j) == State.empty) {
            w.lock();
            try {
                this.put(givenState, i, j);
            }
            finally {
                w.unlock();
            }
            lastMove = new Coordinates(i, j);
            //System.out.println("JESAM");
            return true;
        }
        //System.out.println("NISAM");
        return false;
    }


    public boolean publicEmpty(int i, int j)  // stavlja stanje givenState na polje koordinata (i,j)
    {
        if(this.get(i,j) != State.empty) {
            w.lock();
            try {
                this.put(State.empty, i, j);
            }
            finally {
                w.unlock();
            }
            lastMove = new Coordinates(i, j);
            //System.out.println("JESAM");
            return true;
        }
        //System.out.println("NISAM");
        return false;
    }

    /**
     * Finds out which mark is on the specific field on the board.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return a mark on the field with given coordinates
     */
    public State get(int i,int j)
    {
        /*System.out.println(i);
        System.out.println(j);*/
        int i2 = (i + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        int j2 = (j + TableConfig.TABLE_SIZE*2) % TableConfig.TABLE_SIZE;
        try {
            return (this.table[i2][j2]).getState();
        }
        catch (Exception e) {
            //e.printStackTrace();
            /*System.out.println(i);
            System.out.println(i2);
            System.out.println(j);
            System.out.println(j2);*/
            return null;
        }
    }


    public State publicGet(int i,int j)
    {
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
     * Used when a computer is playing to make it make a move.
     * @param me
     */
    public Coordinates putAutomatic(State me)
    {
        Double weight = 0.0;
        Double r = 0.0;
        //String s;
        State enemy;
        double biggestWeight=-1;
        int bWICoor=1,bWJCoor=1;

        w.lock();
        try {
            if (me==State.cross)
                enemy= State.circle;
            else
                enemy= State.cross;


            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                //s = "";
                for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {//for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    r = rn.nextDouble();
                    r = r*r*r * mistakeFactor;

                    /*System.out.println("LLLL");
                    System.out.println(level);
                    System.out.println(mistakeFactor);*/

                    weight = this.evaluateSpaceWeight(i, j, me) + r;
                    //s += String.format("%6s", weight);
                    if (weight > biggestWeight) {
                        bWICoor = i;
                        bWJCoor = j;
                        biggestWeight = weight;
                    }
                }
                //System.out.println(i + " " + s);
            }


            for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
                //s = "";
                for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {//for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                    weight = 0.8 * this.evaluateSpaceWeight(i, j, enemy);
                    //s += String.format("%6s", weight);
                    if (weight > biggestWeight) {
                        bWICoor = i;
                        bWJCoor = j;
                        biggestWeight = weight;
                    }
                }
                //System.out.println("a" + i + " " + s);
            }

            this.put(me,bWICoor,bWJCoor);
            lastMove = new Coordinates(bWICoor,bWJCoor);

        }
        finally { w.unlock(); }




        return (new Coordinates(bWICoor, bWJCoor));

    }


    /**
     * Detects how many instances of a specific sequence
     * on a board could be made by putting a mark on the
     * field with given coordinates.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @param me a mark being put on the board and tested
     * @param sequence an array containing the sequence, 1 represents a mark, and 0 represents an empty space
     * @return
     */

    private int detectSequence(int i, int j, State me, int[] sequence, int direction)
    {
        if (this.get( i, j )!=State.empty) return 0;
        int result=0;
        this.put( me, i, j );
        //System.out.println("DIRECTION " + direction);

        for (int begin=0; begin<sequence.length; begin++)
        {
            int count[]={0,0,0,0};
            for (int x=0; x<sequence.length; x++)
            {
                switch(direction) {
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

            for (int x=0; x<4; x++)
                if (count[x]==sequence.length)
                    result++;

        }
        this.put( State.empty, i, j );
        return result;
    }



    /**
     * Evaluates a weight of a single field.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @param me a player about who's move it is been thought.
     * @return Weight
     */

    private double evaluateSpaceWeight(int i, int j, State me)
    {
        double result=0;

        if (this.get( i, j )!=State.empty) return -100000;


        for (int x = 0; x<4; x++) {
            if ((this.detectSequence(i, j, me, no, x)) != 0) ;
            else if ((this.detectSequence(i, j, me, win6, x)) != 0)
                result += 60000;
            else if ((this.detectSequence(i, j, me, win5, x)) != 0)
                result += 30000;
            else if ((this.detectSequence(i, j, me, win, x)) != 0)
                result += 10000;
            else if ((this.detectSequence(i, j, me, probablyMust, x)) != 0)
                result += 1000;
            else if ((this.detectSequence(i, j, me, three1, x) != 0) ||
                    (this.detectSequence(i, j, me, three2, x) != 0) ||
                    (this.detectSequence(i, j, me, three3, x) != 0) ||
                    (this.detectSequence(i, j, me, three4, x) != 0))
                result += 100;
            else if ((this.detectSequence(i, j, me, two1, x) != 0) ||
                    (this.detectSequence(i, j, me, two2, x) != 0) ||
                    (this.detectSequence(i, j, me, two3, x) != 0))
                result += 10;
            else if ((this.detectSequence(i, j, me, shittierTwo1, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo2, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo3, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo4, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo5, x) != 0) ||
                    (this.detectSequence(i, j, me, shittierTwo6, x) != 0))
                result += 1;
        }
        return result;

    }




    public int end2(int iC, int jC, long lastEventT)
    {
        w.lock();

        int result = 0;
        lastEventTime = lastEventT;
        try {


            State state = this.get(iC, jC);
            //System.out.println(state.toString());
            Boolean found = false;


            for (int counter = 0; counter < 4; counter++) {
                int k1, k2;
                switch (counter) {
                    case 0:
                        k1 = 1; k2 = 0;
                        break;
                    case 1:
                        k1 = 0; k2 = 1;
                        break;
                    case 2:
                        k1 = 1; k2 = 1;
                        break;
                    default:
                        k1 = 1; k2 = -1;
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

                            //System.out.println(this.get(i, j));

                            this.put(State.empty, i, j);
                            this.put(State.empty, i + 1 * k1, j + 1 * k2);
                            this.put(State.empty, i + 2 * k1, j + 2 * k2);
                            this.put(State.empty, i + 3 * k1, j + 3 * k2);
                            if(result == 0)
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

        }
        finally { w.unlock(); }
        return result;

    }

    void moveRock(){
        if  (!isServer) {
            return;
        }

        int rockNo = (int) (Math.ceil(rn.nextDouble() * TableConfig.NO_OF_ROCKS) -1);
        int c = 0;
        for (int i=0; i<TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if (this.get(i, j) == State.rock){
                    if(c == rockNo){
                        this.put(State.empty, i, j);
                    }
                    c++;
                }
            }
        }

        Boolean rockPut = false;
        while(!rockPut) {
            int x = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
            int y = (int) (rn.nextDouble() * TableConfig.TABLE_SIZE);
            if (get(x, y) == State.empty) {
                put(State.rock, x, y);
                rockPut = true;
            }
        }


    }

    /**
     * Checks if someone has won.
     * @return A winner or empty.
     */

    public EndStruct end()
    {
        r.lock();
        try {
            for (int i=-4; i<TableConfig.TABLE_SIZE; i++)    //koso prema dolje lijevo
                for (int j=-4; j<TableConfig.TABLE_SIZE; j++)
                {
                    if ( (this.get(i,j)== State.cross) && (this.get(i+1,j+1)==State.cross) && (this.get( i+2, j+2 )==State.cross) && (this.get( i+3, j+3 )==State.cross) )
                        return new EndStruct(State.cross,
                                new Coordinates(i,j),
                                new Coordinates(i+1,j+1),
                                new Coordinates(i+2,j+2),
                                new Coordinates(i+3,j+3));
                    if ( (this.get(i,j)== State.circle) && (this.get(i+1,j+1)==State.circle) && (this.get( i+2, j+2 )==State.circle) && (this.get( i+3, j+3 )==State.circle) )
                        return new EndStruct(State.circle,
                                new Coordinates(i,j),
                                new Coordinates(i+1,j+1),
                                new Coordinates(i+2,j+2),
                                new Coordinates(i+3,j+3));
                }

            for (int i=-4; i<TableConfig.TABLE_SIZE; i++)    //koso prema dolje desno
                for (int j=-4; j<TableConfig.TABLE_SIZE; j++)
                {
                    if ( (this.get(i,j)== State.cross) && (this.get(i-1,j+1)==State.cross) && (this.get( i-2, j+2 )==State.cross) && (this.get( i-3, j+3 )==State.cross) )
                        return new EndStruct(State.cross,
                                new Coordinates(i,j),
                                new Coordinates(i-1,j+1),
                                new Coordinates(i-2,j+2),
                                new Coordinates(i-3,j+3));
                    if ( (this.get(i,j)== State.circle) && (this.get(i-1,j+1)==State.circle) && (this.get( i-2, j+2 )==State.circle) && (this.get( i-3, j+3 )==State.circle) )
                        return new EndStruct(State.circle,
                                new Coordinates(i,j),
                                new Coordinates(i-1,j+1),
                                new Coordinates(i-2,j+2),
                                new Coordinates(i-3,j+3));
                }

            for (int i=-4; i<TableConfig.TABLE_SIZE; i++)  //vodoravno
                for (int j=-4; j<TableConfig.TABLE_SIZE; j++)
                {
                    if ( (this.get(i,j)== State.cross) && (this.get(i+1,j)==State.cross) && (this.get( i+2, j )==State.cross) && (this.get( i+3, j)==State.cross) )
                        return new EndStruct(State.cross,
                                new Coordinates(i,j),
                                new Coordinates(i+1,j),
                                new Coordinates(i+2,j),
                                new Coordinates(i+3,j));
                    if ( (this.get(i,j)== State.circle) && (this.get(i+1,j)==State.circle) && (this.get( i+2, j )==State.circle) && (this.get( i+3, j )==State.circle) )
                        return new EndStruct(State.circle,
                                new Coordinates(i,j),
                                new Coordinates(i+1,j),
                                new Coordinates(i+2,j),
                                new Coordinates(i+3,j));
                }

            for (int i=-4; i<TableConfig.TABLE_SIZE; i++)  //okomito
                for (int j=-4; j<TableConfig.TABLE_SIZE; j++)
                {
                    if ( (this.get(i,j)== State.cross) && (this.get(i,j+1)==State.cross) && (this.get( i, j+2 )==State.cross) && (this.get( i, j+3)==State.cross) )
                        return new EndStruct(State.cross,
                                new Coordinates(i,j),
                                new Coordinates(i,j+1),
                                new Coordinates(i,j+2),
                                new Coordinates(i,j+3));
                    if ( (this.get(i,j)== State.circle) && (this.get(i,j+1)==State.circle) && (this.get( i, j+2 )==State.circle) && (this.get( i, j+3 )==State.circle) )
                        return new EndStruct(State.circle,
                                new Coordinates(i,j),
                                new Coordinates(i,j+1),
                                new Coordinates(i,j+2),
                                new Coordinates(i,j+3));
                }
        }

        finally { r.unlock(); }

        return new EndStruct(State.empty,
                new Coordinates(0,0),
                new Coordinates(0,0),
                new Coordinates(0,0),
                new Coordinates(0,0));
    }

    public void setLevel(int l){
        this.level = l;
        //System.out.println("Stavio sam nivo: " + l + "     " + this.level);
    }


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
                //Log.d("ButtonClickerrrrr", "getMsgBuff: " + msgBuff[c]);
                c++;
            }
        }
        Log.d("ButtonClickerrrrr", "getMsgBuff:   " + new String(msgBuff));
        return msgBuff;
    }

    public Coordinates applyMsgBuff(byte[] msgBuff) {//vraca koordinate ako je protivnik stavio svog
        if(isServer) {
            Log.d("ButtonClickerrrrr", "JESAM SERVER");
        } else {
            Log.d("ButtonClickerrrrr", "NISAM SERVER");
        }


        int c = 0;
        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {

                if ((msgBuff[c] == 5) && (!isServer)) { //server postavlja rockove
                    this.put(State.rock, i, j);
                }

                if (this.get(i, j) == State.rock) {//na rock on smije stavit svog ili ga ispraznit samo ako je server
                    //msgBuff[c] = 5;
                    if (!isServer) {
                        if(msgBuff[c] == 1) {
                            this.put(State.cross, i, j);
                        }
                        if(msgBuff[c] == 0) {
                            this.put(State.empty, i, j);
                        }
                    }


                } else if (this.get(i, j) == State.empty) {//na prazno polje on smije stavit svoga
                    //msgBuff[c] = 0;
                    if(msgBuff[c] == 1) {
                        this.put(State.cross, i, j);
                        return new Coordinates(i, j);
                    }


                } else if (this.get(i, j) == State.circle) {//na zauzeto polje stavlja - konflikt!
                    //msgBuff[c] = 1;
                    if(( ((i+j) % 2) == 0) ^ isServer) {//ko ima prednost na ovom polju?
                        if (msgBuff[c] == 1) {
                            this.put(State.cross, i, j);
                            return new Coordinates(i, j);
                        }
                    }



                } else if (this.get(i, j) == State.cross) {//on smije micat svoje
                    //msgBuff[c] = 2;
                    if(msgBuff[c] == 0) {
                        this.put(State.empty, i, j);
                    }


                }


                //Log.d("ButtonClickerrrrr", "applyMsgBuff: " + msgBuff[c]);
                c++;
            }
        }
        Log.d("ButtonClickerrrrr", "applyMsgBuff: " + new String(msgBuff));

        return null;

    }

}





