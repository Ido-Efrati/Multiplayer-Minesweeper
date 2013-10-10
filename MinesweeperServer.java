package minesweeper.server;

import java.net.*;
import java.io.*;
/*Thread Safety Argument:
 * minesweeper.server is a server in which each user can play minesweeper.  
 * Clients make requests of the form serve. which creates a new socket connection for the client.
 * and for each request, the server creates a specific thread for the user. so no two threads can interleave
 * In addition the client is create and not stored so we can't mutate it.
 * 
 * debug is a final so it immutable
 * 
 * the counter for the amount of people on the server is synchronized so we won't try to increment or decrement 
 * the number at the same time.
 * 
 *  all of the board methods are synchronized (the board is thread safe). By calling any of them from handleRequest won't cause concurrency issues,
 *  because it is not possible for two invocations of synchronized methods on the same object to interleave. When one thread is executing a synchronized method for an object, all other threads that invoke synchronized methods for the same object block (suspend execution) until the first thread is done with the object.
 */


public class MinesweeperServer {
    private final ServerSocket serverSocket;
    /** True if the server should _not_ disconnect a client after a BOOM message. */
    private final boolean debug;
    public static Board MineSweeperBoard ;
    private int numberOfThreads=0;

    /**
     * Make a MinesweeperServer that listens for connections on port.
     * @param port port number, requires 0 <= port <= 65535.
     */
    public MinesweeperServer(int port, boolean debug) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
    }

    /**
     * Run the server, listening for client connections and handling them.  
     * Never returns unless an exception is thrown.
     * @throws IOException if the main server socket is broken
     * (IOExceptions from individual clients do *not* terminate serve()).
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            updatePlayers(1);
            Thread t = new Thread(new Runnable() {
                public void run() {
                    Thread.yield();  // give the other threads a chance to start too, so it's a fair race
            try {
                handleConnection(socket);
            } catch (IOException e) {
                e.printStackTrace(); // but don't terminate serve()
            } finally {
                try {
                    socket.close();
                    //MAYBE STOP and remove
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }            
            }});
            t.start();// don't forget to start the thread!
        }
    }

    /**
     * Handle a single client connection.  Returns when client disconnects.
     * @param socket socket where the client is connected
     * @throws IOException if connection has an error or terminates unexpectedly
     */
    // Do we need to sync this
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            out.println("Welcome to Minesweeper. " + Integer.toString(numberOfThreads) + 
                    " people are playing including you. Type 'help' for help."+ String.format("%n"));
            for (String line =in.readLine(); line!=null; line=in.readLine()) {
                String output = handleRequest(line);
                if(output.equals("BOOM!")&&!debug){
                    out.println(output);
                    break;
                }
                if(output.equals("bye")){
                    break;
                }
                if(output != null) {
                    out.println(output);
                }
                out.flush();
            }
        } finally {   
            updatePlayers(-1);
            out.close();
            in.close();
        }
    }


    /**
     * handler for client input
     * 
     * make requested mutations on game state if applicable, then return 
     * appropriate message to the user.
     * 
     * @param input
     * @return
     */
    private static String handleRequest(String input) {
        String regex = "(look)|(dig \\d+ \\d+)|(flag \\d+ \\d+)|" +
                "(deflag \\d+ \\d+)|(help)|(bye)";
        if(!input.matches(regex)) {
            //invalid input
            //For any server input which does not match the user-to-server grammar, do nothing
            return "";
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            return MineSweeperBoard.look();
        } else if (tokens[0].equals("help")) {
            // 'help' request
            return MineSweeperBoard.help();
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            return MineSweeperBoard.bye();
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                return MineSweeperBoard.dig(x,y);
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                return MineSweeperBoard.flag(x, y);
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                return MineSweeperBoard.deflag(x, y);
            }
        }
        //For any server input which does not match the user-to-server grammar, do nothing
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }

    /**
     * Start a MinesweeperServer running on the default port (4444).
     * 
     * Usage: MinesweeperServer [DEBUG [(-s SIZE | -f FILE)]]
     * 
     * The DEBUG argument should be either 'true' or 'false'. The server should disconnect a client
     * after a BOOM message if and only if the DEBUG flag is set to 'false'.
     * 
     * SIZE is an optional integer argument specifying that a random board of size SIZE*SIZE should
     * be generated. E.g. "MinesweeperServer false -s 15" starts the server initialized with a
     * random board of size 15*15.
     * 
     * FILE is an optional argument specifying a file pathname where a board has been stored. If
     * this argument is given, the stored board should be loaded as the starting board. E.g.
     * "MinesweeperServer false -f boardfile.txt" starts the server initialized with the board
     * stored in boardfile.txt, however large it happens to be (but the board may be assumed to be
     * square).
     * 
     * The board file format, for use by the "-f" option, is specified by the following grammar:
     * 
     * FILE :== LINE+
     * LINE :== (VAL SPACE)* VAL NEWLINE
     * VAL :== 0 | 1
     * SPACE :== " "
     * NEWLINE :== "\n" 
     * 
     * If neither FILE nor SIZE is given, generate a random board of size 10x10. If no arguments are
     * specified, do the same and additionally assume DEBUG is 'false'. FILE and SIZE may not be
     * specified simultaneously, and if one is specified, DEBUG must also be specified.
     * 
     * The system property minesweeper.customport may be used to specify a listening port other than
     * the default (used by the autograder only).
     */
    public static void main(String[] args) {
        // We parse the command-line arguments for you. Do not change this method.
        boolean debug = false;
        File file = null;
        Integer size = 10; // Default size.
        try {
            if (args.length != 0 && args.length != 1 && args.length != 3)
              throw new IllegalArgumentException();
            if (args.length >= 1) {
                if (args[0].equals("true")) {
                    debug = true;
                } else if (args[0].equals("false")) {
                    debug = false;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            if (args.length == 3) {
                if (args[1].equals("-s")) {
                    try {
                        size = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                    if (size < 0)
                        throw new IllegalArgumentException();
                } else if (args[1].equals("-f")) {
                    file = new File(args[2]);
                    if (!file.isFile()) {
                        System.err.println("file not found: \"" + file + "\"");
                        return;
                    }
                    size = null;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("usage: MinesweeperServer DEBUG [(-s SIZE | -f FILE)]");
            return;
        }
        // Allow the autograder to change the port number programmatically.
        final int port;
        String portProp = System.getProperty("minesweeper.customport");
        if (portProp == null) {
            port = 4444; // Default port; do not change.
        } else {
            port = Integer.parseInt(portProp);
        }
        try {
            runMinesweeperServer(debug, file, size, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file. Either the file or the size argument must be null, but not both.
     * 
     * @param debug The server should disconnect a client after a BOOM message if and only if this
     *        argument is false.
     * @param size If this argument is not null, start with a random board of size size * size.
     * @param file If this argument is not null, start with a board loaded from the specified file,
     *        according to the input file format defined in the JavaDoc for main().
     * @param port The network port on which the server should listen.
     */
    public static void runMinesweeperServer(boolean debug, File file, Integer size, int port)
            throws IOException
    {
        if(file==null&& size==null){
         MineSweeperBoard= new Board(debug);}
        else if(file==null&& size>0){
             MineSweeperBoard= new Board(debug,size);}
        else if(file!=null){
             MineSweeperBoard= new Board(debug, file);}
        else{
            throw new RuntimeException("bad input");
        }
        MinesweeperServer server = new MinesweeperServer(port, debug);
        server.serve();

    }
    /**
     * increment or decrement the number of players
     * @param one
     */
    public synchronized void updatePlayers(int one){
        numberOfThreads+=one;
    }
}
