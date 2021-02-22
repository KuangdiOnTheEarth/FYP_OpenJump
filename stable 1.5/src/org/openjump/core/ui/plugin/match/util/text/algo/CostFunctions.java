// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation;
// version 2.1 of the License.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
package org.openjump.core.ui.plugin.match.util.text.algo;

/**
 * <p>Provides several implementations of {@link LevenshteinCosts}.</p>
 *
 * @author harald
 *
 */
public class CostFunctions {
  // Nothing to instantiate here.
  private CostFunctions() {}

    /**
     * <p>Is a {@link LevenshteinCosts} implementation that ignores character case.
     * The cost of replacing an uppercase character with its lowercase equivalent
     * or vice versa has zero cost.</p>
     */
    public static final LevenshteinCosts caseIgnore = new LevenshteinCosts() {
        
        public int insDelCost(char c) {
            return 1;
        }

        public int substCost(char c1, char c2) {
            if( Character.toLowerCase(c1) == Character.toLowerCase(c2) ) return 0;
            return 1;
        }
        
        public int maxCost() {
            return 1;
        }
    };

  
    /**
     * <p>Provides the default implementation of {@link LevenshteinCosts} where
     * all operations have cost 1.</p>
     */
    public static final LevenshteinCosts defaultCosts = new LevenshteinCosts() {
        public int insDelCost(char c) {
            return 1;
        }
        public int substCost(char c1, char c2) {
            if( c1 == c2 ) return 0;
            return 1;
        }
        public int maxCost() {
            return 1;
        }
    };
    
    /**
     * <p>Provides a function giving specific weights to each edit operation</p>
     */
    public static final LevenshteinCosts frenchCosts = new LevenshteinCosts() {
        public int insDelCost(char c) {
            c = Character.toUpperCase(c);
            if (c == 'R' || c == 'S' || c == 'T') return 3;
            else if (c == 'B' || c == 'C' || c == 'E' || c == 'F' || c == 'H' || c == 'L' || c == 'M' || c == 'N' || c == 'P') return 4;
            else if (Character.isLetterOrDigit(c)) return 5;
            else return 3;
        }
        public int substCost(char c1, char c2) {
            if( c1 == c2 ) return 0;
            
            c1 = Character.toLowerCase(c1);
            c2 = Character.toLowerCase(c2);
            if( c1 == c2 ) return 0;
            
            // swap to have c1 < c2
            if (c1 > c2) {
                c1 ^= c2;
                c2 ^= c1;
                c1 ^= c2;
            }
            if (c1 == 'a' && "������".indexOf(c2) > -1) return 1;
            if (c1 == 'c' && c2 == '\u00E7') return 1;
            if (c1 == 'e' && "����".indexOf(c2) > -1) return 1;
            if (c1 == 'i' && "����".indexOf(c2) > -1) return 1;
            if (c1 == 'o' && "������".indexOf(c2) > -1) return 1;
            if (c1 == 'u' && "����".indexOf(c2) > -1) return 1;
            if (c1 == 'y' && "��".indexOf(c2) > -1) return 1;
            if (c1 == 'n' && c2 == '\u00F1') return 1;
            if (c1 == 's' && c2 == '\u00DF') return 1;
            if (c1 == '0' && c2 == 'o') return 1;
            if (c1 == 'i' && c2 == '1') return 1;
            
            if (" -'".indexOf(c1) > -1      && "'-".indexOf(c2) > -1) return 1;
            if ("a������".indexOf(c1) > -1  && "������".indexOf(c2) > -1) return 1;
            if ("e����".indexOf(c1) > -1    && "����".indexOf(c2) > -1) return 1;
            if ("1i����".indexOf(c1) > -1   && "i����".indexOf(c2) > -1) return 1;
            if ("0o������".indexOf(c1) > -1 && "o������".indexOf(c2) > -1) return 1;
            if ("u����".indexOf(c1) > -1    && "u����".indexOf(c2) > -1) return 1;
            if ("y��".indexOf(c1) > -1      && "��".indexOf(c2) > -1) return 1;
            
            if ("a������e����1i����0o������u����y��".indexOf(c1) > -1 &&
                "a������e����1i����0o������u����y��".indexOf(c2) > -1) return 2;
            if (c1 == 'b' && c2 == 'p') return 2;
            if (c1 == 'c' && c2 == 'k') return 2;
            if (c1 == 'c' && c2 == 'q') return 2;
            if (c1 == 'c' && c2 == 's') return 2;
            if (c1 == 'd' && c2 == 't') return 2;
            if (c1 == 'm' && c2 == 'n') return 2;
            if (c1 == 's' && c2 == 'z') return 2;
            if (!Character.isLetterOrDigit(c1) && !Character.isLetterOrDigit(c2)) {
                if (Character.getType(c1) == Character.getType(c2)) return 2;
                else return 3;
            }            
            return 5;
        }
        public int maxCost() {
            return 5;
        }
    };
  
}
  
