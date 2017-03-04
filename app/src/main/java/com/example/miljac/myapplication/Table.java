package com.example.miljac.myapplication;

/**
 * Created by miljac on 24.1.2017..
 */

//import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
/**
 * This is where the actual game happens.
 * @author miljac
 *
 */
public class Table // igraca tabla
{
    public static int TABLE_SIZE = 9;
    private final Space[][] table = new Space[10][10];

    private final int[] no=           {0,0,0,1,0,0,0};
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

    private int level;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    private Coordinates lastMove = new Coordinates(5,5);

    Random rn = new Random();


    /**
     * The default constructor. Initializes an empty board.
     */

    public Table(int l)
    {
        //this.table = new ArrayList<ArrayList<Space>>();
        this.level = l;
        for (int i=0; i<TABLE_SIZE; i++)
        {
            for (int j=0; j<TABLE_SIZE; j++){
                (this.table[i][j]) = new Space();
                /*System.out.println("TTTTT");
                System.out.println((this.table[i][j]).getState().toString());*/
                //System.out.println(this.get(i,j).toString());
                //System.out.println();

            }
        }
    }

    /**
     * Puts a mark on the board.
     * @param givenState a mark to be put.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     */

    private void put(State givenState, int i, int j)  // stavlja stanje givenState na polje koordinata (i,j)
    {
        i = (i + TABLE_SIZE*2) % TABLE_SIZE;
        j = (j + TABLE_SIZE*2) % TABLE_SIZE;
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
            System.out.println("JESAM");
            return true;
        }
        System.out.println("NISAM");
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
            System.out.println("JESAM");
            return true;
        }
        System.out.println("NISAM");
        return false;
    }

    /**
     * Finds out which mark is on the specific field on the board.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return a mark on the field with given coordinates
     */
    private State get(int i,int j)
    {
        /*System.out.println(i);
        System.out.println(j);*/
        int i2 = (i + TABLE_SIZE*2) % TABLE_SIZE;
        int j2 = (j + TABLE_SIZE*2) % TABLE_SIZE;
        try {
            return (this.table[i2][j2]).getState();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(i);
            System.out.println(i2);
            System.out.println(j);
            System.out.println(j2);
            return null;
        }
    }


    public State publicGet(int i,int j)
    {
        w.lock();
        try {
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


            for (int i = 0; i < TABLE_SIZE; i++) {
                //s = "";
                for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {//for (int j = 0; j < TABLE_SIZE; j++) {
                    r = rn.nextDouble();
                    r = r*r*r * 13;
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


            for (int i = 0; i < TABLE_SIZE; i++) {
                //s = "";
                for (int j = (lastMove.y-3); j < (lastMove.y+4); j++) {//for (int j = 0; j < TABLE_SIZE; j++) {
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

        if (this.get( i, j )!=State.empty) return -1;


        for (int x = 0; x<4; x++) {
            if ((this.detectSequence(i, j, me, no, x)) != 0) ;
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



    /**
     * Checks if someone has won.
     * @return A winner or empty.
     */

    public EndStruct end()
    {
        r.lock();
        try {
            for (int i=-4; i<TABLE_SIZE; i++)    //koso prema dolje lijevo
                for (int j=-4; j<TABLE_SIZE; j++)
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

            for (int i=-4; i<TABLE_SIZE; i++)    //koso prema dolje desno
                for (int j=-4; j<TABLE_SIZE; j++)
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

            for (int i=-4; i<TABLE_SIZE; i++)  //vodoravno
                for (int j=-4; j<TABLE_SIZE; j++)
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

            for (int i=-4; i<TABLE_SIZE; i++)  //okomito
                for (int j=-4; j<TABLE_SIZE; j++)
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
        System.out.println("Stavio sam nivo: " + l + "     " + this.level);
    }

}





