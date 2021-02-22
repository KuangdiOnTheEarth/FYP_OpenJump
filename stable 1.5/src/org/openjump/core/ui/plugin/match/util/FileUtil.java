/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Utility class for file manipulation.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
public final class FileUtil {

    /** Constructor. */
    private FileUtil() {}

    /** Return the name of the file without extension.*/
    public final static String getNameWithoutExtension(final File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        return pos > 0 ? name.substring(0, pos):name;
    }

    /** Return the extension of the file name (including dot character).*/
    public final static String getExtension(final File file) {
        String name = file.getName();
        int pos = name.lastIndexOf(".");
        return pos > 0 ? name.substring(pos, name.length()):"";
    }
    
    /** Return an Iterator to iterate through a text file lines.*/
    public final static Iterator<String> iterator(File file) {
           
        final String path = file.getPath();
           
        return new Iterator<String>() {
            BufferedReader in;
            String nextLine;
            {
                try {
                    in = new BufferedReader(new FileReader(path));
                    nextLine = in.readLine();
                }
                catch(IOException e) {throw new IllegalArgumentException(e);}
            }

            // If the next line is non-null, then we have a next line
            public boolean hasNext() { 
                return nextLine != null; 
            }

            // Return the next line, but first read the line that follows it.
            public String next() {
                try {
                    String result = nextLine;
                    // If we haven't reached EOF yet
                    if (nextLine != null) {  
                        nextLine = in.readLine(); // Read another line
                        if (nextLine == null) in.close(); // And close on EOF
                    }
                    // Return the line we read last time through.
                    return result;
                } catch(IOException e) {throw new IllegalArgumentException(e);}
            }

            // The file is read-only; we don't allow lines to be removed.
            public void remove() { 
                throw new UnsupportedOperationException(); 
            }
        };
    }

}