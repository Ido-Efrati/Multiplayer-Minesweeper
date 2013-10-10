package minesweeper.server;

public class square {
    private boolean mine;
    private int value;
    private String state;
    public square(boolean hasMine){
        mine=hasMine;
        value=0;
        state="untouched";
    }
    
    /**
     * a getter to get the status of a square
     * @return true if there is a mine , false if there isn't
     */
    public boolean getMine(){
        return mine;
    }
    /**
     * set mine value to false after digging a mine
     */
    public void removeMine(){
        mine=false;
    }
    
    /**
     * a setter to set the value of a square based on the mines around it
     * @param valueOfSquare- number of mines around a square
     */
    public void setValue(int valueOfSquare){
        value= valueOfSquare;
    }
    /**
     * get the value of a square
     * @return value
     */
    public int getValue(){
        return value;
    }
    /**
     * 
     * @return the state of each square
     */
    public String getState(){
        return state;
    }
    /**
     * change the state of a square after it was flagged
     */
    public void flag(){
        state="flagged";
    }
    /**
     * change the state of a square after it was deflagged
     */
    public void deflag(){
        state="untouched";
    }
    /**
     * change the state of the square after it was dug.
     */
    public void dig(){
        state="dug";
    }

}
