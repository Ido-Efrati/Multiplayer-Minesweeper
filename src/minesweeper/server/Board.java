package minesweeper.server;

import java.io.*;
import java.util.Random;

/*Thread Safety Argument:
 * The board class generate the board for the user and keep track of each square in the game
 * 
 * My constructor does not need to be synchronize because it is making a new object that is confined. 
 * Therefore, Java won't let you synchronize it. 
 * 
 * all of the methods that create the initial board are private and cannot be accessed from outside this class.
 * So there is no thread risk in those private methods.
 * 
 * by confinement - each square is contained in the board, and only one thread can access the board at each time
 * so the square is thread safe.
 * 
 * Finally all of the public methods that are mutating the board/squares and are being call by the threads are synchronized.
 * and since it is not possible for two invocations of synchronized methods on the same object to interleave. When one thread is executing a synchronized method for an object, all other threads that invoke synchronized methods for the same object block (suspend execution) until the first thread is done with the object.
 * we can't have a case that two synchronized methods will interleave.
 */
public class Board {
    private boolean debug = false;
    private int space = 10;
    private File file;
    private square[][] board;
    private Random random = new Random();
    simpleReader sr = new simpleReader();
    private String stringForBoard;

    // overloading the constructor to support optional fields
    // first constructor debug and space
    public Board(boolean DEBUG, int SIZE) {
        debug = DEBUG;
        space = SIZE;
        initialBoard(space);
    }

    // second constructor debug and FILE
    public Board(boolean DEBUG, File FILE) {
        debug = DEBUG;
        file = FILE;
        initialBoard(space);

    }

    // finally constructor with just debug
    public Board(boolean DEBUG) {
        debug = DEBUG;
        initialBoard(space);
    }

    /**
     * take a file and generate an empty board with mines in it
     * 
     * @param file
     *            - a valid file path
     */
    private void fileEvaluator(File file) {
        stringForBoard = sr.FileToString(file);
        String[] splits = stringForBoard.split(String.format("%n"));
        for (int k = 0; k < splits.length; k++) {
            splits[k] = splits[k].replace(" ", "");
        }
        int numOfColms = splits.length;
        for (String str : splits) {
            if (str.length() != numOfColms) {
                throw new RuntimeException(
                        "the is an invalid number of rows or columns");
            }
        }
        board = new square[numOfColms][numOfColms];
        space = numOfColms;
        for (int i = 0; i < numOfColms; i++) {
            for (int j = 0; j < splits[i].length(); j++) {
                board[i][j] = new square(splits[i].charAt(j) == '1');
            }
        }
    }

    /**
     * a method to generate a board
     * 
     * @param space
     *            - integer for the the area of the board space*space
     */
    private void createBoardNoFile(int space) {
        board = new square[space][space];
        for (int i = 0; i < space; i++) {
            for (int j = 0; j < space; j++) {
                int value = random.nextInt(100);
                board[i][j] = new square(value <= 25);// 25% of having a mine in
                                                      // a board;
            }
        }
    }

    /**
     * 
     * @param x
     * @param y
     * @param space
     * @return true if the neighbouring square exists on the board, false
     *         otherwise.
     */
    private boolean isValid(int x, int y, int space) {
        if (x >= space || x < 0 || y >= space || y < 0) {
            return false;
        }

        return true;
    }

    /**
     * 
     * @param i
     *            - the ith location on the board
     * @param j
     *            - the jth location on the board
     * @param space
     *            - the size of the board
     * @return how many mines are around a give square
     */
    private int checkAround(int i, int j, int space) {
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (isValid(i + x, j + y, space)
                        && board[i + x][j + y].getMine()) { // if the square
                                                            // around me is
                    // valid and has a mine
                    count++;
                }
            }
        }
        if (count <= 8) {
            return count;
        } else {
            throw new RuntimeException("illegal number of mines");
        }
    }

    /**
     * update the values of each square based on the mines around it
     */
    private void addNumbersBasedOnMines() {
        for (int i = 0; i < space; i++) {
            for (int j = 0; j < space; j++) {
                if (board[i][j].getValue() != -1) {
                    board[i][j].setValue(checkAround(i, j, space));
                }
            }
        }
    }

    /**
     * create the new board in the constructor
     * 
     * @param space
     */
    public void initialBoard(Integer space) {
        if (file == null) {
            createBoardNoFile(space);
        } else {
            fileEvaluator(file);
        }
        addNumbersBasedOnMines();
    }

    /**
     * a method to print the board
     */
    public synchronized String look() {
        String boardToPrint = "";
        for (int i = 0; i < space; i++) {
            for (int j = 0; j < space; j++) {
                String state = board[i][j].getState();
                if (state.equals("untouched")) {
                    boardToPrint += "- ";
                } else if (state.equals("flagged")) {
                    boardToPrint += "F ";
                } else if (state.equals("dug")) {
                    int value = board[i][j].getValue();
                    if (value == 0) {
                        boardToPrint += "  ";
                    } else {
                        boardToPrint += Integer.toString(value) + " ";
                    }
                }
            }
            boardToPrint += String.format("%n");// end line to print new lines
                                                // in the board
        }
        boardToPrint = boardToPrint.replace(String.format(" %n"),
                String.format("%n"));
        return boardToPrint;
    }

    /**
     * allow the user to flag a square
     * 
     * @param i
     * @param j
     * @return a new look
     */
    public synchronized String flag(int i, int j) {
        if (i >= space || i < 0 || j >= space || j < 0) {
            return look();
        }
        if (board[i][j].getState().equals("untouched")) {
            board[i][j].flag();
        }
        return look();
    }

    /**
     * allow the user to deflag a square
     * 
     * @param i
     * @param j
     * @return a new look
     */
    public synchronized String deflag(int i, int j) {
        if (i >= space || i < 0 || j >= space || j < 0) {
            return look();
        }
        if (board[i][j].getState().equals("flagged")) {
            board[i][j].deflag();
        }
        return look();

    }

    /**
     * allow the user to dig a square
     * 
     * @param i
     * @param j
     * @return a new look
     */
    public synchronized String dig(int i, int j) {

        if ((i < 0 || j < 0) || i >= space || j >= space
                || board[i][j].getState().equals("flagged")) {
            return look();
        } else {
            if (board[i][j].getMine()) {
                board[i][j].setValue(0);// indicate that we hit a mine
                board[i][j].removeMine();
                board[i][j].dig();
                addNumbersBasedOnMines();
                for (int k = -1; k < 2; k++) {
                    for (int w = -1; w < 2; w++) {
                        if (isValid(i + k, j + w, space)) {
                            if (board[i + k][j + w].getValue() == 0
                                    && board[i + k][j + w].getState().equals(
                                            "dug")) {
                                board[i + k][j + w].dig();
                                dig(i + k, j + w); // new to re-run

                            }
                        }
                    }
                }

                return boom();
            }

            else {
                if (board[i][j].getValue() > 0 && board[i][j].getValue() <= 8) {
                    board[i][j].dig();
                } else if (board[i][j].getValue() == 0) {
                    for (int x = -1; x < 2; x++) {
                        for (int y = -1; y < 2; y++) {
                            if (isValid(i + x, j + y, space)) {
                                if (board[i + x][j + y].getValue() != 0
                                        && !board[i + x][j + y].getState()
                                                .equals("dug")) {
                                    board[i + x][j + y].dig();
                                }// if just a number reveal it;
                                else if (board[i + x][j + y].getValue() == 0
                                        && !board[i + x][j + y].getState()
                                                .equals("dug")) {
                                    board[i + x][j + y].dig();
                                    dig(i + x, j + y); // new to re-run

                                }
                            }
                        }
                    }
                }
            }
            return look();
        }
    }

    /**
     * help function
     * 
     * @return a string - help for the game
     */
    public synchronized String help() {
        String message = "Welcome to mine sweeper you can use one of the following options:"
                + String.format("%n")
                + "look- will present to you the board at its current state"
                + String.format("%n")
                + "dig x y - will dig a square in the board"
                + String.format("%n")
                + "flag x y - will mark a square on the board with F"
                + String.format("%n")
                + "deflag x y - will unmark a square on the board. square will be set to -"
                + String.format("%n")
                + "help- will print the help menu"
                + String.format("%n")
                + "bye - will close your session"
                + String.format("%n");
        return message;
    }

    private synchronized String boom() {
        return "BOOM!";
    }

    public synchronized String bye() {
        return "bye";
    }

    // For Testing
    public synchronized boolean getIsMineForTesting(int x, int y) {
        return board[x][y].getMine();
    }

}
