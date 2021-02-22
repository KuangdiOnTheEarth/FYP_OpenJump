/*
 * (c) Michael Gilleland (http://www.merriampark.com/ld.htm), no licence
 * (c) 2003 Chas Emerick
 * (c) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text.algo;

/**
 * The LevenshteinDistance is the number of changes needed to change one String
 * into another, where each change is a single character modification (deletion,
 * insertion or substitution).
 *
 * <p>This implementation, is the one proposed by Chas Emerick for the Apache
 * Commons Lang library.</p>
 * <p>It is based on Michael Gilleland implementation, but it is more memory 
 * efficient for large strings.</p>
 * <p>See also <a href="http://www.merriampark.com/ldjava.htm">
 * http://www.merriampark.com/ldjava.htm</a></p>
 * 
 * <p>The difference between this implementation and the classical one is
 * that, rather than creating and retaining a matrix of size s.length()+1 by
 * t.length()+1, we maintain two single-dimensional arrays of length s.length()+1.
 * The first, d, is the 'current working' distance array that maintains the
 * newest distance cost counts as we iterate through the characters of String s.
 * Each time we increment the index of String t we are comparing, d is copied to
 * p, the second int[]. Doing so allows us to retain the previous cost counts as
 * required by the algorithm (taking the minimum of the cost count to the left,
 * up one, and diagonally up and to the left of the current cost count being
 * calculated). (Note that the arrays aren't really copied anymore, just
 * switched...this is clearly much better than cloning an array or doing a 
 * System.arraycopy() each time  through the outer loop.)</p>
 * 
 * <p>
 * In order to normalize the result, distance returns the ratio between the
 * edit number and the longest string.
 * </p>
 * @author Michael Michaud
 * @version 0.1 (2009-04-20)
 */

public class LevenshteinDistance implements StringDistance, EditDistance {
    
    public static final LevenshteinDistance LEVENSHTEIN_DISTANCE = new LevenshteinDistance();
    
    private LevenshteinDistance() {}

   /**
    * Levenshtein distance between two Strings.
    *
    * <p>This is the number of changes needed to change one String into
    * another, where each change is a single character modification (deletion,
    * insertion or substitution).</p>
    *
    * @param s String to compare from
    * @param t String to compare to
    * @return the number of changes needed to change one string into another.
    */
    public int editDistance(String s, String t) {
        
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        
        int n = s.length(); // length of s
        int m = t.length(); // length of t
		
        if (n == 0)      return m;
        else if (m == 0) return n;

        int p[] = new int[n+1]; //'previous' cost array, horizontally
        int d[] = new int[n+1]; // cost array, horizontally
        int _d[];               //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i;    // iterates through s
        int j;    // iterates through t
        
        char t_j; // jth character of t
        int cost; // cost

        for (i = 0; i<=n; i++) {
            p[i] = i;
        }
		
        for (j = 1; j<=m; j++) {
            t_j = t.charAt(j-1);
            d[0] = j;
            for (i=1; i<=n; i++) {
                cost = s.charAt(i-1)==t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                // if the hereabove inline min function takes 1300 ms
                // inline Math.min(Math.min(a,b),c) takes 1350 ms
                // call to a static final min function takes 1530 ms 
                int a = d[i-1]+1;
                int b = p[i]+1;
                int c = p[i-1]+cost;
                int min = a;
                if (b < min) min = b;
                if (c < min) min = c;
                d[i] = min;
            }
            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        } 		
       // our last action in the above loop was to switch d and p, so p now 
       // actually has the most recent cost counts
       return p[n];
    }

   /**
    * Normalized Levenshtein distance between two Strings.
    *
    * <p>The Levenshtein distance measure the number of edit operations
    * (change, add or remove a letter) to change a String into another one.</p>
    *
    * <p>Here, the distance is normalized in order to return a value included in
    * [0,1] interval. To achieve that, Levenshtein distance is divided by the
    * number of characters of the longest string.</p>
    *
    * <p>Implementation of the algorithm has been taken from <link
    * href="http://www.merriampark.com/ld.htm" />.</p>
    *
    * @param s String to compare from
    * @param t String to compare to
    * @return a float value representing a normalized distance.
    */
    public float distance (String s, String t) {
        return (float)editDistance(s,t)/(float)Math.max(s.length(), t.length());
    }
    
}