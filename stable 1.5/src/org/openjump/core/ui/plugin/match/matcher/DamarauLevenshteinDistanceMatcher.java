/*
Copyright (c) 2011, Micha&euml;l Michaud
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of its authors nor the names of its contributors may
      be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.openjump.core.ui.plugin.match.matcher;

import com.vividsolutions.jump.feature.Feature;
import org.openjump.core.ui.plugin.match.Index;
import fr.michaelm.util.text.Rule;
import fr.michaelm.util.text.RuleRegistry;
import fr.michaelm.util.text.TransformationException;
import fr.michaelm.util.text.algo.BKTree;
import fr.michaelm.util.text.algo.EditDistance;
import fr.michaelm.util.text.algo.DamarauLevenshteinDistance;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * A matcher based on the Damarau Levenshtein distance metric.
 * Damarau Leveshtein metric count the number of edit operation necessary to
 * transform a string S1 into a String S2. An edit operation can be one of
 * Add a letter, remove a letter, change a letter or switch two contiguous 
 * letters. Note that the switch operation is counted as 2 distinct operations
 * in the classic Levenshtein distance. 
 *
 * @see Matcher
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class DamarauLevenshteinDistanceMatcher extends StringMatcher {
    
    private static final DamarauLevenshteinDistanceMatcher DAMARAU_LEVENSHTEIN =
        new DamarauLevenshteinDistanceMatcher("","", 2.0);
    
    public static DamarauLevenshteinDistanceMatcher instance() {
        return DAMARAU_LEVENSHTEIN;
    }
    
    DamarauLevenshteinDistanceMatcher(String sourceAttribute, String targetAttribute, double max_dist) {
        super(sourceAttribute, targetAttribute);
        this.max_dist = max_dist;
    }
    
    /**
     * Returns 1.0 if source.equals(target), 0.0 if Levenstein distance
     * between source and target is more than max_dist.
     * @param source attribute to match from
     * @param target attribute to match to
     * @param context object containing useful information to check if
     * Attribute a effectively matches Attribute ref
     *
     * @return a double in the range [0-1] representative of the match quality
     * between a and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
     public double match(String source, String target, Object context) 
                                                              throws Exception {
         if (source == null || target == null) return 0.0;
         double distance = (double)DamarauLevenshteinDistance
                 .damarauLevenshtein(source.toString(), target.toString(), (int)max_dist);
         return distance > max_dist ? 0.0 : (1.0 - (distance/max_dist));
     }
     
     /**
     * Sets the maximum Levenshtein distance accepted between two strings.
     * @see #getMaximumDistance
     */
    public void setMaximumDistance(double max_dist) {
        this.max_dist = max_dist;
    }
    
    /**
     * Create an index following the Index interface.
     * This index does not accelearate queries as it returns the whole set for
     * any query.
     * @param features features to index
     * @throws TransformationException if the targetRule could not be applied
     *         to target features to build the index.
     */
    public Index createIndex(final Collection<Feature> features) 
                                                throws TransformationException {
        // As BKTree contains Strings, not Features, we build an normal index
        // along with the BKTree to retrieve features from attribute values
        final int limit = (int)max_dist;
        final BKTree tree = new BKTree(new EditDistance() {
            public int editDistance(String s, String t) {
                return DamarauLevenshteinDistance
                    .damarauLevenshtein(s, t, limit);
            }
        });
        final TreeMap<String,Set<Feature>> index = 
            new TreeMap<String,Set<Feature>>(collator);
        for (Feature f : features) {
            String value = targetRule.transform(f.getString(getTargetAttribute()));
            tree.add(value);
            Set set = index.get(value);
            if (set == null) {
                set = new HashSet();
                index.put(value, set);
            }
            set.add(f);
        }
        return new Index() {
            public Set<Feature> query(Object value) {
                Set<Feature> candidates = new HashSet<Feature>();
                // Get candidate strings from the BKTree
                HashMap<String,Integer> map = 
                    tree.query(value.toString(), (int)getMaximumDistance());
                // Get candidate features from the index
                for (String s : map.keySet()) {
                    candidates.addAll(index.get(s));
                }
                return candidates;
            }
        };
    }

}
