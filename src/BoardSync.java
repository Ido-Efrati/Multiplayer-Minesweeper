import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;

import minesweeper.server.Board;

import org.junit.Test;
/*
* Testing strategy:
* Test to see if after an action we return the right board to the player,or a wrong board. 
* Test 1: can return a digging in process due to a dig of a zero.
* Test 2 : can return a combination of - and " ", because the entire board is empty.
*/

public class BoardSync {
    File f = new File("files/test2");
    File f2= new File("files/testempty");
    Board b;
    private String look_number1 = "";
    private String look_number2 = "";
    private String forempty="";
    private String forempty2="";
    private String nonempty="";
    private String empty="";

    @Test
    public void DigTwice() {
        String option1 = "- - - - -" + String.format("%n") + "- - - - -"
                + String.format("%n") + "- - - - -" + String.format("%n")
                + "- - - 2 1" + String.format("%n") + "- - - 1  "
                + String.format("%n");
        String option2 = "2 - - - -" + String.format("%n") + "- - - - -"
                + String.format("%n") + "- - - - -" + String.format("%n")
                + "- - - 2 1" + String.format("%n") + "- - - 1  "
                + String.format("%n");
        String option3 = "2 - - - -" + String.format("%n") + "- - - - -"
                + String.format("%n") + "- - - - -" + String.format("%n")
                + "- - - - -" + String.format("%n") + "- - - - -"
                + String.format("%n");

        int numOfTries = 100;
        for (int i = 0; i < numOfTries; i++) {
            boolean tfLook = false;
            b = new Board(false, f);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    Thread.yield();// give the other threads a chance to start
                                   // too, so it's a fair race
                    look_number1 = b.dig(0, 0);

                }
            });

            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    Thread.yield();// give the other threads a chance to start
                                   // too, so it's a fair race

                    look_number2 = b.dig(4, 4);
                }
            });
            t.start();
            t2.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        if (!(look_number1.equals(option3) || look_number1.equals(option2))) {
            tfLook = true;
        } else if (!(look_number2.equals(option1) || look_number2
                .equals(option2))) {
            tfLook = true;
        }
        assertFalse(tfLook);
        }
    }

@Test
public void empty() {
    for (int j=0; j<15; j++){
        nonempty+="- - - - - - - - - - - - - - -"+String.format("%n");
        empty+="                             "+String.format("%n");
    }
    int numOfTries = 100;
    for (int i = 0; i < numOfTries; i++) {
        boolean tfLook = false;
        b = new Board(false, f2);

        Thread t = new Thread(new Runnable() {
            public void run() {
                Thread.yield();// give the other threads a chance to start
                               // too, so it's a fair race
                b.dig(0, 0);

            }
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                Thread.yield();// give the other threads a chance to start
                               // too, so it's a fair race

                forempty = b.look();
            }
        });
        t.start();
        t2.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
    if (!(forempty.equals(empty) || forempty.equals(nonempty))) {
        tfLook = true;
        System.out.println(forempty);
    }
    assertFalse(tfLook);
    }
}
}