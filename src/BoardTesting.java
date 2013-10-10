import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import minesweeper.server.Board;
/*
 * Testing strategy
 * look- test look for every possible action dig, flag deflag, look
 * flag and deflag- flag/deflag a square, try to flag/deflag a dug square with number, try to flag/deflag a dug square with "  "
 * dig- dig a square with number/mine/nothing in it, try to re-dig , try to dig a flagged square  
 */


public class BoardTesting {
    File f= new File("files/test1");
    Board b = new Board(false,f);
    @Test
    public void lookEmptyBoard() {
    assertEquals("- - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void digANumber() {
    b.dig(0, 0);
    assertEquals("2 - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void redigANumber() {
    b.dig(0, 0);
    b.dig(0, 0);
    assertEquals("2 - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void Flag() {
    b.flag(0, 0);
    assertEquals("F - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void deflag() {
    b.flag(0, 0);
    b.deflag(0, 0);
    assertEquals("- - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }

    @Test
    public void tryToFlagaDug() {
    b.dig(0, 0);
    b.flag(0, 0);
    assertEquals("2 - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void tryToDigAFlag() {
    b.flag(0, 0);
    b.dig(0, 0);
    assertEquals("F - -" +String.format("%n")+"- - -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void digMineWithZerosAround() {
    b.dig(0, 1);
    assertEquals(b.getIsMineForTesting(1, 0),true);// there was a mine before we dug
    b.dig(1, 0);
    assertEquals(b.getIsMineForTesting(1, 0),false);// no longer a mine in this square
    b.deflag(1,0);
    b.dig(0, 0);
    b.flag(0, 0);// try to flag an empty square
    b.deflag(0, 0);
    assertEquals("  1 -" +String.format("%n")+"1 2 -"+String.format("%n") +"- - -"+String.format("%n"),b.look());
    }
    
    @Test
    public void help() {
        String message="Welcome to mine sweeper you can use one of the following options:"+String.format("%n")
                + "look- will present to you the board at its current state" + String.format("%n")
                + "dig x y - will dig a square in the board" +String.format("%n")
                + "flag x y - will mark a square on the board with F" +String.format("%n")
                + "deflag x y - will unmark a square on the board. square will be set to -" +String.format("%n")
                + "help- will print the help menu" + String.format("%n")
                + "bye - will close your session"+ String.format("%n");
        assertEquals(message,b.help());
    }
    
    //Invalid
    @Test
    public void tooBigOfAVal() {
    b.dig(110, 0);
    b.flag(0, 110);
    }

    }
