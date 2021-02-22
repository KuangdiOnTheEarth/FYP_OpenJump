/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * DSVUtil offers utility methods for
 * <a href="http://en.wikipedia.org/wiki/Delimiter-separated_values">Delimiter
 * Separated Values</a> files.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
public final class DSVUtil {
    
    public final static int STRINGFIELD  = 0;
    public final static int NUMERICFIELD = 1;
    
    public final static Pattern TAB        = Pattern.compile("\t");
    public final static Pattern COMMA      = Pattern.compile(",");
    public final static Pattern SEMICOLON  = Pattern.compile(";");
    public final static Pattern PIPE       = Pattern.compile("\\|");
    public final static Pattern WHITESPACE = Pattern.compile(" ");

    private final static Pattern[] DELIMITERS = new Pattern[]{
        TAB,
        COMMA,
        SEMICOLON,
        PIPE,
        WHITESPACE
    };

    /** Constructor. */
    private DSVUtil() {}

   /**
    * Guess the delimiter used in a table-like text file.<br>
    * The delimiter must be one of \t , ; | or whitespace<br>
    * If the file contains an irregular number of fields
    * separated by the 'x' delimiter, the method will consider that
    * it contains only one field delimited by the first non-x
    * delimiter of DELIMITERS (e.g. \t)
    * If no delimiter is found, tabulation Pattern is returned.
    */
    public final static Pattern guessDelimiter(String file) throws FileNotFoundException, IOException {

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        int MAX = 5;
        String[] lines = new String[MAX];

        String line = "";
        int count = 0;
        // read 5 first significative lines
        while (null != (line = bufferedReader.readLine()) && count < MAX) {
            if (line.trim().length()==0) continue;
            if (line.startsWith("#")) continue;
            if (line.startsWith("//")) continue;
            lines[count++] = line;
        }
        bufferedReader.close();
        fileReader.close();

        // For each delimiter, split the line into fields
        // and keep the delimiter producing the more fields
        boolean constantFieldNumber = false;
        int fieldNumber = 0;
        int maxFieldNumber = 0;
        Pattern sep4MaxFieldNumber = null;
        for (Pattern sep : DELIMITERS) {
            fieldNumber = 0;
            constantFieldNumber = false;
            // Iterates through lines until a null line is found
            for (int i = 0 ; i < MAX && lines[i] != null ; i++) {
                if (i == 0) {
                    // -1 arguments, guarantees that all fields are counted,
                    // even an empty field located at the last position
                    fieldNumber = sep.split(lines[i], -1).length;
                    constantFieldNumber = true;
                }
                // stop if the delimiter produces a diffrent number of fields
                else if (sep.split(lines[i], -1).length != fieldNumber) {
                    constantFieldNumber = false;
                    break;
                }
            }
            // keep the delimiter if it gives a constant number of fields
            // and if the field number is greater than with other delimiters
            if (constantFieldNumber && fieldNumber > maxFieldNumber) {
                maxFieldNumber = fieldNumber;
                sep4MaxFieldNumber = sep;
            }
        }
        return sep4MaxFieldNumber;
    }
    
    /**
    * Same function a {@link #guessDelimiter} except it returns a char.
    *
    * @param file the filename of the file to be sorted
    */
    public char guessCharDelimiter(String file) throws FileNotFoundException, IOException {
        Pattern pattern = guessDelimiter(file);
        if (pattern == TAB) return '\t';
        else if (pattern == COMMA) return ',';
        else if (pattern == SEMICOLON) return ';';
        else if (pattern == PIPE) return '|';
        else if (pattern == WHITESPACE) return ' ';
        else return '\t';
    }
    
    public static void createRandomDSVFile(String fileName, char delimiter,
                                           int nbLines, int nbFields) 
                                      throws FileNotFoundException, IOException{
        String[] strings = new String[]{
            "eeeeeeeeeeeeeeeeeeeeeeeeeeee ssssssssssssssss aaaaaaaaaaaaaaa iiiiiiiiiiiiiii tttttttttttttt nnnnnnnnnnnnnn rrrrrrrrrrrrr uuuuuuuuuuuuu lllllllllll ooooooooooo ddddddd ccccccc pppppp mmmmmm éééé vvv qqq ff bb gg hh j à x y è ë z w ç ù k î œ ï ë",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            "0123456789ABCDEF"
        };
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        int sizeId = new String(""+nbLines).length();
        for (int i = 0 ; i < nbLines ; i++) {
            String id = "000000000000"+(i+1);
            bw.append(id.substring(id.length()-sizeId));
            for (int j = 1 ; j < nbFields ; j++) {
                bw.append(delimiter);
                if (j%4==1) bw.append(StringUtil.randomString(1, strings[1]) + StringUtil.randomString(8*(j/4+1) + (int)(16*(j/4+1)*Math.random()), strings[0]));
                if (j%4==2) bw.append("" + (int)(Math.random()*1000));
                if (j%4==3) bw.append(StringUtil.randomString(6 , strings[2]));
                if (j%4==0) bw.append("" + (Math.random()*nbLines));
            }
            bw.newLine();
        }
        bw.close();
    }

}