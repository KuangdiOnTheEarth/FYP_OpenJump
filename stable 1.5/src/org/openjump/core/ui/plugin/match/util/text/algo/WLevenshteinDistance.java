// This library is covered by the GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
// http://www.gnu.org/licenses/lgpl-2.1.html
// (c) Harald Kirsch
// http://approdictio.berlios.de/

package org.openjump.core.ui.plugin.match.util.text.algo;

/**
 * <p> Implementation of the Levenshtein edit distance (or metric). Objects of
 * this class can be parameterized by implementations of {@link LevenshteinCosts}
 *  to tune the costs of the edit operations.</p>
 * <p> W of WLevenshtein is for weighted Levenshtein distance.</p>
 *
 * @author Harald Kirsch
 * @author Micha&euml;l Michaud
 */
public class WLevenshteinDistance implements StringDistance, EditDistance {
  private final LevenshteinCosts costs;

  // +********************************************************************
  /**
   * <p>
   * provides a metric with the default costs as provided by
   * {@link CostFunctions#defaultCosts}.
   * </p>
   */
  public WLevenshteinDistance() {
    this.costs = CostFunctions.defaultCosts;
  }
  /**
   * <p>
   * creates a metric based on the given costs for edit operations. Example cost
   * function implementations can be found in {@link CostFunctions}.
   * </p>
   *
   * @param c
   *          a cost function provider
   */
  public WLevenshteinDistance(LevenshteinCosts c) {
    this.costs = c;
  }
  // +********************************************************************
  /**
   * <p> Computes the Levenshtein edit distance between the given strings 
   * according to the cost function provided at construction of this object.</p>
   */
  public int editDistance(String v1, String v2) {
    int m = v1.length() + 1;
    int n = v2.length() + 1;
    int[][] d = new int[m][n];

    for(int i = 0; i < m; i++)
      d[i][0] = i;
    for(int i = 1; i < n; i++)
      d[0][i] = i;

    for(int i = 1; i < m; i++) {
      char ch1 = v1.charAt(i - 1);
      for(int j = 1; j < n; j++) {
        char ch2 = v2.charAt(j - 1);
        int subst = d[i - 1][j - 1] + costs.substCost(ch1, ch2);
        int delete = d[i - 1][j] + costs.insDelCost(ch1);
        int insert = d[i][j - 1] + costs.insDelCost(ch2);
        d[i][j] = Math.min(Math.min(subst, delete), insert);
      }
    }
    //System.out.printf("%s--%s: %d%n", v1, v2, d[m-1][n-1]);
    return d[m - 1][n - 1];
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
        return (float)editDistance(s,t)/(float)Math.max(s.length()*10, t.length()*10);
    }
  
  /**
   * <p>for testing only.</p>
   */
  public static void main(String[] argv) {
    WLevenshteinDistance lm = new WLevenshteinDistance(CostFunctions.caseIgnore);
    for(int i = 0; i < argv.length - 1; i += 2) {
      int d = lm.editDistance(argv[i], argv[i + 1]);
      System.out.printf("%s--%s: %d%n", argv[i], argv[i + 1], d);
    }
  }
}
