package minesweeper.server;
import java.io.*;

public class simpleReader {
        private static BufferedReader b;
        /**
         * @param takes a valid file path
         * @return a string of the file
         */
        public static String FileToString(File file) {
            String outValue = "";
            try {
                String eol = System.getProperty( "line.separator");
                FileReader fin = new FileReader(file);
                b = new BufferedReader(fin);
                String currentLine; 
                while ((currentLine = b.readLine()) != null) {
                    outValue= outValue + currentLine + eol;}
            }
            catch (FileNotFoundException ef) {
                System.out.println("File not found");}
            catch (IOException ei) {
                System.out.println("IO Exception"); }
            return outValue;
        }  
    }
