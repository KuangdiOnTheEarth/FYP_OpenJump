/*
 * (C) 2011 michael.michaud@free.fr
 */
 
package org.openjump.core.ui.plugin.match.util.text.algo;

/**
 * The interface EditDistance has a single method returning the number of
 * deletions, insertions, or substitutions required to transform a String into
 * another String. It is used to measure similarity between two Strings.
 *
 * @author Michael Michaud
 * @version 0.1 (2011-12-01)
 */
public interface EditDistance {
    
   /**
    * Number of deletions, insertions, or substitutions required to transform a
    * String s into a String t
    * @param s String to compare from
    * @param t String to compare to
    * @return a float value representing a normalized distance.
    */
    public int editDistance(String s, String t);
    
}