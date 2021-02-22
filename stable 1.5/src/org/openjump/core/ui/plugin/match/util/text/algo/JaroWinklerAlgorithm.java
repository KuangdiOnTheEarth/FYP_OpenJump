/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text.algo;

/**
 * JaroWinklerAlgorithm is described in 
 * <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * Jaro-Winkler distance</a> article of wikipedia or its equivalent
 * <a href="http://fr.wikipedia.org/wiki/Distance_de_Jaro-Winkler"> in french </a>
 * Source code is taken from <a href="http://www.dcs.shef.ac.uk/~sam/simmetrics.html">
 * SimMetrics library</a>.<br>
 * Jaro distance metric measure how s and t compare each other by counting
 * letters of s matching letters of t (with a certain tolerance) and letters
 * of t matching letters os s. <br>
 * Jaro-Winkler refine this concept by introducing a prefix adjustment scale
 * which will give more favourable ratings to strings that match from the
 * begining for a set prefix length.<br>
 * The distance is given as a number in the [0;1] interval, 0 means strings
 * does not match and 1 means they are equal.
 */
public final class JaroWinklerAlgorithm implements StringDistance {
    
    public static final JaroWinklerAlgorithm JARO_WINKLER = new JaroWinklerAlgorithm();

    /**
     * maximum prefix length to use.
     */
    private static final int MINPREFIXTESTLENGTH = 6;

    /**
     * prefix adjustment scale.
     */
    private static final float PREFIXADUSTMENTSCALE = 0.1f;


    /** Constructor. */
    private JaroWinklerAlgorithm() {}

    /**
     * gets the similarity measure of the JaroWinkler metric for the given strings.
     *
     * @param string1
     * @param string2
     * @return 0-1 similarity measure of the JaroWinkler metric
     */
    public float jaroWinklerSimilarity(final String string1, final String string2) {
        //gets normal Jaro Score
        final float dist = jaroSimilarity(string1, string2);

        // This extension modifies the weights of poorly matching pairs string1, string2 which share a common prefix
        final int prefixLength = getPrefixLength(string1, string2);
        return dist + ((float) prefixLength * PREFIXADUSTMENTSCALE * (1.0f - dist));
    }
    
    public float distance(String s1, String s2) {
        return 1.0f - jaroWinklerSimilarity(s1, s2);
    }


   /**
     * gets the similarity of the two strings using Jaro distance.
     *
     * @param string1 the first input string
     * @param string2 the second input string
     * @return a value between 0-1 of the similarity
     * algo décrit dans http://fr.wikipedia.org/wiki/Distance_de_Jaro-Winkler
     * src disponibles dans http://www.dcs.shef.ac.uk/~sam/simmetrics.html
     */
    public float jaroSimilarity(final String string1, final String string2) {

        //get half the length of the string rounded up - (this is the distance used for acceptable transpositions)
        //final int halflen = ((Math.min(string1.length(), string2.length())) / 2) + ((Math.min(string1.length(), string2.length())) % 2);
        final int halflength = Math.max(string1.length(), string2.length())/2 - 1;

        //get common characters
        final StringBuffer common1 = getCommonCharacters(string1, string2, halflength);
        final StringBuffer common2 = getCommonCharacters(string2, string1, halflength);

        //check for zero in common
        if (common1.length() == 0 || common2.length() == 0) {
            return 0.0f;
        }

        //check for same length common strings returning 0.0f is not the same
        if (common1.length() != common2.length()) {
            return 0.0f;
        }

        //get the number of transpositions
        int transpositions = 0;
        for (int i = 0; i < common1.length(); i++) {
            if (common1.charAt(i) != common2.charAt(i))
                transpositions++;
        }
        transpositions /= 2.0f;

        //calculate jaro metric
        return (common1.length() / ((float) string1.length()) +
                common2.length() / ((float) string2.length()) +
                (common1.length() - transpositions) / ((float) common1.length())) / 3.0f;
    }

    /**
     * returns a string buffer of characters from string1 within string2 if they are of a given
     * distance seperation from the position in string1.
     *
     * @param string1
     * @param string2
     * @param distanceSep
     * @return a string buffer of characters from string1 within string2 if they are of a given
     *         distance seperation from the position in string1
     */
    private final static StringBuffer getCommonCharacters(final String string1, final String string2, final int distanceSep) {
        //create a return buffer of characters
        final StringBuffer returnCommons = new StringBuffer();
        //create a copy of string2 for processing
        final StringBuffer copy = new StringBuffer(string2);
        //iterate over string1
        for (int i = 0; i < string1.length(); i++) {
            final char ch = string1.charAt(i);
            //set boolean for quick loop exit if found
            boolean foundIt = false;
            //compare char with range of characters to either side
            //for (int j = Math.max(0, i - distanceSep); !foundIt && j < Math.min(i + distanceSep, string2.length() - 1); j++) {
            for (int j = Math.max(0, i - distanceSep); !foundIt && j < Math.min(i + distanceSep, string2.length()); j++) {
                //check if found
                if (copy.charAt(j) == ch) {
                    foundIt = true;
                    //append character found
                    returnCommons.append(ch);
                    //alter copied string2 for processing
                    copy.setCharAt(j, (char)0);
                }
            }
        }
        return returnCommons;
    }

    /**
     * gets the prefix length found of common characters at the begining of the strings.
     *
     * @param string1
     * @param string2
     * @return the prefix length found of common characters at the begining of the strings
     */
    private final static int getPrefixLength(final String string1, final String string2) {
        final int n = Math.min(MINPREFIXTESTLENGTH, Math.min(string1.length(), string2.length()));
        //check for prefix similarity of length n
        for (int i = 0; i < n; i++) {
            //check the prefix is the same so far
            if (string1.charAt(i) != string2.charAt(i)) {
                //not the same so return as far as got
                return i;
            }
        }
        return n; // first n characters are the same
    }

   public static void main(String[] args) throws Exception {
      new JaroWinklerAlgorithm();
   }

}