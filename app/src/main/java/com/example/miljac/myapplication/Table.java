package com.example.miljac.myapplication;

/**
 * Created by miljac on 24.1.2017..
 */

//import java.util.List;
import java.util.ArrayList;
import java.util.Random;
/**
 * This is where the actual game happens.
 * @author miljac
 *
 */
public class Table // igraca tabla
{
    private final int TABLE_SIZE=10;
    private final ArrayList<ArrayList<Space>> table;
    private final double weights[][] = new double[TABLE_SIZE+1][TABLE_SIZE+1];

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

    private final int RECURSION_DEPTH=2;

    private int level;

    Random rn = new Random();


    /**
     * The default constructor. Initializes an empty board.
     */

    public Table(int l)
    {
        this.table = new ArrayList<ArrayList<Space>>();
        this.level = l;
        for (int i=1; i<=TABLE_SIZE+1; i++)
        {
            ArrayList<Space> temporaryVector= new ArrayList<Space>(); //ova varijabla je novi redak table
            for (int j=1; j<=TABLE_SIZE+1; j++)
            {
                Space temporarySpace =new Space();  //ovo je novo polje u tabli
                temporaryVector.add( temporarySpace );
            }
            //temporaryVector.trimToSize();
            this.table.add( temporaryVector );
        }
    }

    /**
     * Checks if a field (space) is too distant to be considered
     * to put a mark on it. Used to prevent the recursion from running
     * wild across the board.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return is it too distant
     */
    private boolean distant(int i, int j)
    {
        int count =0;
        for ( int x=0; x<7; x++)
            for (int y=0; y<7; y++)
            {
                if ((this.get( i-3+x, j-3+y )==State.circle)||(this.get( i-3+x, j-3+y )==State.cross))
                    count++;
                if (count>2) return false;
            }
        return true;



    }
    /**
     * Puts a mark on the board.
     * @param givenState a mark to be put.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     */

    public void put(State givenState, int i, int j)  // stavlja stanje givenState na polje koordinata (i,j)
    {
        this.table.get( i ).get( j ).setState( givenState );
    }

    /**
     * Finds out which mark is on the specific field on the board.
     * @param i x-coordinate of a field (space)
     * @param j y-coordinate of a field (space)
     * @return a mark on the field with given coordinates
     */
    public State get(int i,int j)
    {
        if ((i<1)||(i>TABLE_SIZE)||(j<1)||(j>TABLE_SIZE))
            return null;
        return this.table.get( i ).get( j ).getState();
    }


    /**
     * Marks every field with a number which should describe how
     * smart it is to put a mark on that field.
     * @param me
     */

    private void markWeights(State me)
    {
        for (int i=1; i<=TABLE_SIZE;i++)
            for (int j=1;j<=TABLE_SIZE;j++)
                weights[i][j]+=this.evaluateSpaceWeight( i, j, me ,RECURSION_DEPTH);
        printOut();
    }

    /**
     * zprinta tablu
     */
    private void printOut(){
        System.out.println("\n\n\n");
        for (int i=1; i<=TABLE_SIZE;i++)
        {
            for (int j=1;j<=TABLE_SIZE;j++)
            {
                System.out.format((double)((int)(weights[j][i]*100))/100 +"\t");
            }
            System.out.println("\n");
        }
        System.out.println("Nivo je: " + this.level);
    }

    /**
     * Used when a computer is playing to make it make a move.
     * @param me
     */
    public Coordinates putAutomatic(State me)
    {
        State enemy;
        if (me==State.cross)
            enemy= State.circle;
        else
            enemy= State.cross;

        for (int i=1; i<=TABLE_SIZE;i++)
            for (int j=1;j<=TABLE_SIZE;j++)
                this.weights[i][j]=0;

        double biggestWeight=-1;
        int bWICoor=1,bWJCoor=1;

        this.markWeights( me );

        for (int i=1; i<=TABLE_SIZE;i++)
            for (int j=1;j<=TABLE_SIZE;j++)
            {
                this.weights[i][j]*=3;
                this.weights[i][j]+= rn.nextDouble()/10000;
                if (this.weights[i][j]>=0)
                    this.weights[i][j]+= rn.nextDouble()*1200*(5-level);
            }



        this.markWeights( enemy );



        for (int i=1; i<=TABLE_SIZE;i++)
            for (int j=1;j<=TABLE_SIZE;j++)
            {
                if (this.weights[i][j]>biggestWeight)
                {
                    bWICoor=i;
                    bWJCoor=j;
                    biggestWeight=this.weights[i][j];
                }

            }

        this.put(me,bWICoor,bWJCoor);
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

    private int detectSequence(int i, int j, State me, int[] sequence)
    {
        if (this.get( i, j )!=State.empty) return 0;
        int result=0;
        this.put( me, i, j );

        for (int begin=0; begin<sequence.length; begin++)
        {
            int count[]={0,0,0,0};
            for (int x=0; x<sequence.length; x++)
            {
                if (  ((this.get( i-(sequence.length-1)+begin+x, j )==me) && (sequence[x]==1)) ||
                        (this.get( i-(sequence.length-1)+begin+x, j )==State.empty) && (sequence[x]==0))    //vodoravno
                    count[0]++;

                if (  ((this.get(  i, j-(sequence.length-1)+begin+x )==me) && (sequence[x]==1)) ||
                        (this.get( i, j-(sequence.length-1)+begin+x )==State.empty) && (sequence[x]==0)) //okomito
                    count[1]++;

                if (  ((this.get( i-(sequence.length-1)+begin+x, j-(sequence.length-1)+begin+x )==me) && (sequence[x]==1)) ||
                        (this.get( i-(sequence.length-1)+begin+x, j-(sequence.length-1)+begin+x )==State.empty) && (sequence[x]==0))
                    count[2]++;

                if (  ((this.get( i-(sequence.length-1)+begin+x, j+(sequence.length-1)-begin-x )==me) && (sequence[x]==1)) ||
                        (this.get( i-(sequence.length-1)+begin+x, j+(sequence.length-1)-begin-x )==State.empty) && (sequence[x]==0))
                    count[3]++;


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
     * @param me a player about who's move it is been thinked.
     * @param depth recursion depth
     * @return Weight
     */

    private double evaluateSpaceWeight(int i, int j, State me, int depth)
    {

        if (this.get( i, j )!=State.empty) return -1;
        double result=0;

        if ((this.detectSequence( i, j, me, win )) != 0)
            return 10000;
        else
        {
            result += 800*this.detectSequence( i, j, me, probablyMust );

            result += 80*this.detectSequence( i, j, me, three1 );
            result += 80*this.detectSequence( i, j, me, three2 );
            result += 80*this.detectSequence( i, j, me, three3 );
            result += 80*this.detectSequence( i, j, me, three4 );

            result += 10*this.detectSequence( i, j, me, two1 );
            result += 10*this.detectSequence( i, j, me, two2 );
            result += 10*this.detectSequence( i, j, me, two3 );

            if (this.level>2){
                result += this.detectSequence( i, j, me, shittierTwo1 );
                result += this.detectSequence( i, j, me, shittierTwo2 );
                result += this.detectSequence( i, j, me, shittierTwo3 );
                result += this.detectSequence( i, j, me, shittierTwo4 );
                result += this.detectSequence( i, j, me, shittierTwo5 );
                result += this.detectSequence( i, j, me, shittierTwo6 );
            }
        }

        if (depth>0){
            this.put( me, i, j );
            double maxWeight1=0;
            double maxWeight2=0;

            for (int x=0; x<5;x++)//od okolnih polja uzima dva s najvecom tezinom i dodaje desetinu njihovog prosjeka
                for (int y=0; y<5;y++)
                    if (!this.distant( i-2+x, j-2+y ))
                    {
                        double value=this.evaluateSpaceWeight( i-2+x, j-2+y, me, depth-1 );
                        if (value>maxWeight1)
                            maxWeight1=value;
                        else
                        if (value>maxWeight2)
                            maxWeight2=value;
                    }
            result+=(maxWeight1+maxWeight2)/20;
        }
        this.put( State.empty, i, j );
        return result;

    }





    /**
     * Checks if someone has won.
     * @return A winner or empty.
     */

    public State end()
    {
        for (int i=1; i<=TABLE_SIZE-3; i++)    //koso prema dolje lijevo
            for (int j=1; j<=TABLE_SIZE-3; j++)
            {
                if ( (this.get(i,j)== State.cross) && (this.get(i+1,j+1)==State.cross) && (this.get( i+2, j+2 )==State.cross) && (this.get( i+3, j+3 )==State.cross) )
                    return State.cross;
                if ( (this.get(i,j)== State.circle) && (this.get(i+1,j+1)==State.circle) && (this.get( i+2, j+2 )==State.circle) && (this.get( i+3, j+3 )==State.circle) )
                    return State.circle;
            }

        for (int i=4; i<=TABLE_SIZE; i++)    //koso prema dolje desno
            for (int j=1; j<=TABLE_SIZE-3; j++)
            {
                if ( (this.get(i,j)== State.cross) && (this.get(i-1,j+1)==State.cross) && (this.get( i-2, j+2 )==State.cross) && (this.get( i-3, j+3 )==State.cross) )
                    return State.cross;
                if ( (this.get(i,j)== State.circle) && (this.get(i-1,j+1)==State.circle) && (this.get( i-2, j+2 )==State.circle) && (this.get( i-3, j+3 )==State.circle) )
                    return State.circle;
            }

        for (int i=1; i<=TABLE_SIZE-3; i++)  //vodoravno
            for (int j=1; j<=TABLE_SIZE; j++)
            {
                if ( (this.get(i,j)== State.cross) && (this.get(i+1,j)==State.cross) && (this.get( i+2, j )==State.cross) && (this.get( i+3, j)==State.cross) )
                    return State.cross;
                if ( (this.get(i,j)== State.circle) && (this.get(i+1,j)==State.circle) && (this.get( i+2, j )==State.circle) && (this.get( i+3, j )==State.circle) )
                    return State.circle;
            }

        for (int i=1; i<=TABLE_SIZE; i++)  //okomito
            for (int j=1; j<=TABLE_SIZE-3; j++)
            {
                if ( (this.get(i,j)== State.cross) && (this.get(i,j+1)==State.cross) && (this.get( i, j+2 )==State.cross) && (this.get( i, j+3)==State.cross) )
                    return State.cross;
                if ( (this.get(i,j)== State.circle) && (this.get(i,j+1)==State.circle) && (this.get( i, j+2 )==State.circle) && (this.get( i, j+3 )==State.circle) )
                    return State.circle;
            }

        return State.empty;
    }

    public void setLevel(int l){
        this.level = l;
        System.out.println("Stavio sam nivo: " + l + "     " + this.level);
    }

}



