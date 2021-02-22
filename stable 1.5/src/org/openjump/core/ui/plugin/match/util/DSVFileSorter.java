/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class able to sort large DSV files based on 
 * <a href="http://en.wikipedia.org/wiki/Delimiter-separated_values">
 * Delimiter Separated Values</a> using an
 * <a href="http://en.wikipedia.org/wiki/External_sort">external sorting</a>
 * algorithm.<p>
 * @see <a href="http://www.codeodor.com/index.cfm/2007/5/14/Re-Sorting-really-BIG-files---the-Java-source-code/1208"/>
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
public final class DSVFileSorter {

    private final static Logger logger = Logger.getLogger("fr.m3.util.DSVFileSorter");
    
    public final static int STRING = 0;
    public final static int NUMBER = 1;
    
    private int field = 0;
    private int fieldType = 0;
    private char delimiter = '\t';
    private int chunkSize = 10000;
    private boolean header = true;
    private String fileName;
    
    /** Creates a new FileSorter.*/
    public DSVFileSorter(String fileName) {
        this.fileName = fileName;
    }
    
    /** Set the zero-based column number to be used for sorting (default is 0).*/
    public DSVFileSorter setField(int field) {
        this.field = field;
        return this;
    }
    
    /** Set the field type which can be STRING or NUMBER (default is STRING).*/
    public DSVFileSorter setFieldType(int fieldType) {
        this.fieldType = fieldType;
        return this;
    }
    
    /** Set the delimiter of this Delimiter Separated Values file (default is tab).*/
    public DSVFileSorter setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }
    
    /** Set the chunk size (default is 10000).*/
    public DSVFileSorter setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }
    
    /** Set wether the file has a header line or not (default is true).*/ 
    public DSVFileSorter setHeader(boolean header) {
        this.header = header;
        return this;
    }
    
    /** Sorts the file and return the sorted file name.*/
    public String sort() throws FileNotFoundException, IOException {
        
        // Analyse file name
        int dotIndex = fileName.lastIndexOf('.');
        String fileNameWithoutExtension = dotIndex>0?fileName.substring(0, dotIndex):fileName;
        String dotExtension             = dotIndex>0?fileName.substring(dotIndex, fileName.length()):"";
        
        // comparator to compare two String arrays using a particular field
        Comparator<String[]> comparator =
            fieldType == NUMBER ? getNumberComparator() : getStringComparator();
            
        int numFiles = 0;
        
        try {
            // Open file, and creates Pattern
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            Pattern delimiterPattern = Pattern.compile(new String(new char[]{delimiter}));
            
            // Read header if any, and init first row
            String[] headerRow = null;
            // -1 guarantees empty fields at the end of the row are also returned
            if (header) headerRow = delimiterPattern.split(bufferedReader.readLine(), -1);
            String[] row = header ? headerRow : new String[0];
            ArrayList<String[]> kRows = new ArrayList<String[]>();

            while (row!=null) {
                for(int i = 0; i < chunkSize; i++) {
                    String line = bufferedReader.readLine();
                    if (line==null) {
                        row = null;
                        break;
                    }
                    else if (line.trim().length() == 0) {
                        continue;
                    }
                    else {
                        row = delimiterPattern.split(line, -1);
                        kRows.add(row);
                    }
                }

                // sort the rows
                Collections.sort(kRows, comparator);

                // write to disk
                FileWriter fw = new FileWriter(fileNameWithoutExtension + "_chunk" + numFiles);
                BufferedWriter bw = new BufferedWriter(fw);
                if (header) {
                    bw.write(flattenArray(headerRow));
                    bw.newLine();
                }
                for(int i = 0; i < kRows.size(); i++) {
                    bw.append(flattenArray(kRows.get(i)));
                    bw.newLine();
                }
                bw.close();
                numFiles++;
                kRows.clear();
            }
            bufferedReader.close();
            fileReader.close();
        }
        catch(FileNotFoundException fnfe) {
            logger.warning("Le fichier " + fileName + " n'a pas Ã©tÃ© trouvÃ©");
            logger.throwing("DSVFileSorter", "sort", fnfe);
            throw fnfe;
        }
        catch(IOException ioe) {
            logger.warning("Une erreur d'entrÃ©e sortie est survenue pendant le traitement de " + fileName);
            logger.throwing("DSVFileSorter", "sort", ioe);
            throw ioe;
        }
        catch (Exception ex) {
            logger.warning("Une erreur inconnue est survenue pendant le tri de " + fileName);
            logger.throwing("DSVFileSorter", "sort", ex);
            System.exit(-1);
        }
        return mergeFiles(fileNameWithoutExtension, numFiles, dotExtension);
    }
    
    
    /**
     * Merge several sorted files to make one big file sorted using the field parameter.
     *
     * @param fileNameWithoutExtension the file to sort
     * @param numFiles number of chunks
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String mergeFiles(String fileNameWithoutExtension,
                              int numFiles, String dotExtension)
                                     throws FileNotFoundException, IOException {
        BufferedWriter bw = null;
        List<FileReader> fileReaderList         = new ArrayList<FileReader>();
        List<BufferedReader> bufferedReaderList = new ArrayList<BufferedReader>();
        
        // comparator to compare two String arrays using a particular field
        Comparator<String[]> comparator =
            fieldType == NUMBER ? getNumberComparator() : getStringComparator();
        
        try {

            ArrayList<String[]> fileRows = new ArrayList<String[]>();

            bw = new BufferedWriter(new FileWriter(fileNameWithoutExtension + "_sorted" + dotExtension));
            Pattern delimiterPattern = Pattern.compile(new String(new char[]{delimiter}));
            String[] headerRow = null;

            boolean someFileStillHasRows = false;

            for (int i = 0; i < numFiles; i++) {
                
                fileReaderList.add(new FileReader(fileNameWithoutExtension + "_chunk" + i));
                bufferedReaderList.add(new BufferedReader(fileReaderList.get(i)));
                
                // get each one past the header
                if (header) headerRow = delimiterPattern.split(bufferedReaderList.get(i).readLine(), -1);

                if (i==0 && header) {
                    bw.write(flattenArray(headerRow));
                    bw.newLine();
                }

                // get the first row of each file
                String line = bufferedReaderList.get(i).readLine();
                if (line != null) {
                    fileRows.add(delimiterPattern.split(line, -1));
                    someFileStillHasRows = true;
                }
                else {
                    fileRows.add(null);
                }
            }

            String[] row;
            int cnt = 0;
            while (someFileStillHasRows) {
                // initialize row with min value and its index
                String[] minRow = null;
                int minIndex = 0;
                row = fileRows.get(0);
                if (row != null) {
                    minRow = row;
                    minIndex = 0;
                }
                else {
                    minRow = null;
                    minIndex = -1;
                }

                // search the min field through fileRows 
                for(int i = 1; i < fileRows.size(); i++) {
                    row = fileRows.get(i);
                    if (minRow != null) {
                        if(row != null && comparator.compare(row, minRow) < 0) {
                            minIndex = i;
                            minRow = fileRows.get(i);
                        }
                    }
                    else {
                        if(row != null) {
                            minRow = row;
                            minIndex = i;
                        }
                    }
                }

                if (minIndex < 0) {
                    someFileStillHasRows=false;
                }
                else {
                    // write to the sorted file
                    bw.append(flattenArray(fileRows.get(minIndex)));
                    bw.newLine();

                    // get another row from the file that had the min
                    String line = bufferedReaderList.get(minIndex).readLine();
                    if (line != null) {
                        fileRows.set(minIndex, delimiterPattern.split(line, -1));
                    }
                    else {
                        fileRows.set(minIndex, null);
                    }
                }
                // check if one file still has rows
                for(int i = 0; i < fileRows.size(); i++) {
                    someFileStillHasRows = false;
                    if(fileRows.get(i) != null) {
                        if (minIndex < 0) {
                            System.out.println("min Index is 0 but the following non null row has been found : " + flattenArray(fileRows.get(i)));
                            System.exit(-1);
                        }
                        someFileStillHasRows = true;
                        break;
                    }
                }

                // check the actual files one more time
                if (!someFileStillHasRows) {
                    //write the last one not covered above
                    for(int i = 0; i < fileRows.size(); i++) {
                        if (fileRows.get(i) == null) {
                            String line = bufferedReaderList.get(i).readLine();
                            if (line!=null) {
                                someFileStillHasRows=true;
                                fileRows.set(i, delimiterPattern.split(line, -1));
                            }
                        }
                    }
                }
            }
            
        }
        catch (FileNotFoundException fnfe) {
            logger.warning("Une erreur d'entrée sortie est survenue pendant le traitement de " + fileName);
            logger.throwing("DSVFileSorter", "mergeFiles", fnfe);
            throw fnfe;
        }
        catch (IOException ioe) {
            logger.warning("Une erreur d'entrée sortie est survenue pendant le traitement de " + fileName);
            logger.throwing("DSVFileSorter", "mergeFiles", ioe);
            throw ioe;
        }
        catch (Exception ex) {
            logger.warning("Une erreur inconnue est survenue pendant le tri de " + fileName);
            logger.throwing("DSVFileSorter", "mergeFiles", ex);
            System.exit(-1);
        }
        finally {
            if (bw != null) bw.close();
            for(int i = 0; i < bufferedReaderList.size(); i++) {
                if (bufferedReaderList != null) {
                    BufferedReader br = bufferedReaderList.get(i);
                    if (br != null) br.close();
                }
            }
            for (int i = 0; i < numFiles; i++) {
                new File(fileNameWithoutExtension + "_chunk" + i).delete();
            }
        }
        return fileNameWithoutExtension + "_sorted" + dotExtension;
    }


    /** Flatten an array of String into a single String using del as a delimiter*/
    private String flattenArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        for (String string : array) sb.append(string).append(delimiter);
        return sb.substring(0, sb.length()-1);
    }

    
    private Comparator<String[]> getStringComparator() {
        return new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                if (o1.length>field && o2.length>field) {
                    return (o1[field].compareTo(o2[field]));
                }
                else if (o1.length<=field) return -1;
                else if (o2.length<=field) return 1;
                else return 0;
            }
        };
    }
    
    private Comparator<String[]> getNumberComparator() {
        return new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                if (o1.length > field && o2.length > field) {
                    Double n1=Double.NaN, n2 = Double.NaN;
                    try {
                        n1 = new Double(o1[field]);
                    } catch(NumberFormatException nfe) {/*n1 = NaN*/}
                    try {
                        n2 = new Double(o2[field]);
                    } catch(NumberFormatException nfe) {/*n2 = NaN*/}
                    return (n1.compareTo(n2));
                }
                else if (o1.length<=field) return -1;
                else if (o2.length<=field) return 1;
                else return 0;
            }
        };
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            DSVFileSorter fs = new DSVFileSorter(args[0]).setField(Integer.parseInt(args[1]));
            fs.sort();
        }
        else {
            System.out.println("Usage is :");
            System.out.println("java -cp . org.jeocode.util.DSVFileSorter filename column");
            System.out.println("    - filename must be a valid path");
            System.out.println("    - column is a 0-based column indice");
        }
    }

}