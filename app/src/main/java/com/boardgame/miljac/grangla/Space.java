package com.boardgame.miljac.grangla;


/**
 * A single field (space) on a game board
 *
 */
public class Space
{
    /**
     * The default constructor, sets the field (space) to be empty.
     */
    private State state;
    public Boolean removing = false;

    public Space()
    {
        this.state=State.empty;
    }
    /**
     *
     * @return A mark on this field (space), or empty.
     */
    public State getState()
    {
        return this.state;
    }
    /**
     * Puts a mark on this field (space), or removes it, by
     * making it empty.
     * @param givenState A mark to put on the field (space)
     */

    public void setState(State givenState)
    {
        this.state=givenState;
    }
    /**
     * Compares marks on the field (space).
     */
    public boolean equals(Object o)
    {
        if (!( o instanceof Space ) )
            return false;
        Space that = (Space) o;
        return (this.state==that.state);
    }

}
