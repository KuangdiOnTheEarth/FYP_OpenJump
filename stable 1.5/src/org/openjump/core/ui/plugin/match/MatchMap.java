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

package org.openjump.core.ui.plugin.match;

import com.vividsolutions.jump.feature.Feature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Map accumulating information about matches between two sets of features.
 *
 * The MatchMap will store every single Match in a Tree Map ordering all 
 * possible matches from the best score to the worst score. For matches
 * returning the same score, ordering is determined by the 
 * {@link Match.compareTo(Object other)} method.
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class MatchMap {
    
    private final SortedSet<Match> EMPTY_SET = Collections.unmodifiableSortedSet(new TreeSet<Match>());
        
    private final TreeSet<Match> matches = new TreeSet<Match>();
    private final Map<Feature,TreeSet<Match>> sourceMap = new HashMap<Feature,TreeSet<Match>>();
    private final Map<Feature,TreeSet<Match>> targetMap = new HashMap<Feature,TreeSet<Match>>();
    
    //TODO : to improve performance, instead of maintaining 3 ordered map during 
    // the feeding (at each add call), sort the map on demand, keeping track of 
    // the ordering state (sorted after a get* or a filter call, unsorted after 
    // a add call.
    boolean sorted;

    /**
     * Construct a new MatchMap.
     */
    public MatchMap() {}
    
    /**
     * Add a match to this MatchMap.
     */
    public void add(Match m) {
        SortedSet<Match> previousMatches = getMatches(m.getSource(), m.getTarget());
        // If matches already contains a Match from f1 to f2
        if (!previousMatches.isEmpty()) {
            // previousMatch is not as good as new one : remove it !
            Match previousMatch = previousMatches.first();
            if (previousMatch.getScore() < m.getScore()) {
                removeMatch(previousMatch);
            }
            // previousMatch is better than new one : don't add the new one !
            else return;
        }   
        if (matches.add(m)) {
            TreeSet<Match> set = sourceMap.get(m.getSource());
            if (set == null) {
                set = new TreeSet<Match>();
                sourceMap.put(m.getSource(), set);
            }
            set.add(m);
            set = targetMap.get(m.getTarget());
            if (set == null) {
                set = new TreeSet<Match>();
                targetMap.put(m.getTarget(), set);
            }
            set.add(m);
        }
    }
    
    /**
     * Get the whole match Set.
     */
    public Set<Match> getAllMatches() {
        return matches;
    }
    
    /**
     * Get the set of features matching one or more features.
     */
    public Set<Feature> getSourceFeatures() {
        return sourceMap.keySet();
    }
    
    /**
     * Get the set of features being matched by one or more features.
     */
    public Set<Feature> getTargetFeatures() {
        return targetMap.keySet();
    }
    
    /**
     * Get Matches recorded for this source Feature.
     */
    public SortedSet<Match> getMatchesForSourceFeature(Feature f) {
        SortedSet<Match> matches = sourceMap.get(f);
        return matches == null ? EMPTY_SET : matches;
    }
    
    /**
     * Get Matches recorded for this target Feature.
     */
    public SortedSet<Match> getMatchesForTargetFeature(Feature f) {
        SortedSet<Match> matches = targetMap.get(f);
        return matches == null ? EMPTY_SET : matches;
    }
    
    /**
     * Get Features matching source Feature f.
     */
    public List<Feature> getMatchedFeaturesFromSource(Feature f) {
        TreeSet<Match> matchedFeatures = sourceMap.get(f);
        List<Feature> list = new ArrayList<Feature>();
        if (matchedFeatures == null) return list;
        for (Match m : matchedFeatures) {
            list.add(m.getTarget());
        }
        return list;
    }
    
    /**
     * Get Features matching target Feature f.
     */
    public List<Feature> getMatchedFeaturesFromTarget(Feature f) {
        TreeSet<Match> matchedFeatures = targetMap.get(f);
        List<Feature> list = new ArrayList<Feature>();
        if (matchedFeatures == null) return list;
        for (Match m : matchedFeatures) {
            list.add(m.getSource());
        }
        return list;
    }
    
    /**
     * Return Match from source to target. Usually, the result contains 0
     * or 1 Match, but nothing prevent insertion of several matches per couple
     * of features.
     */
    public SortedSet<Match> getMatches(Feature source, Feature target) {
        // Set of matches from f1
        TreeSet<Match> set1 = sourceMap.get(source);
        // Set of matches to f2
        TreeSet<Match> set2 = targetMap.get(target);
        // Intersection of both sets = Match:f1->f2
        if (set1 != null && set2 != null) {
            SortedSet<Match> set = (TreeSet<Match>)set1.clone();
            set.retainAll(set2);
            return set;
        }
        else return EMPTY_SET;
    }
    
    private boolean removeMatchesForSourceFeature(Feature f) {
        TreeSet<Match> set = sourceMap.remove(f);
        return matches.removeAll(set);
    }
    
    private boolean removeMatchesForTargetFeature(Feature f) {
        TreeSet<Match> set = targetMap.remove(f);
        return matches.removeAll(set);
    }
    
    /**
     * Remove a match from the map.
     */
    public void removeMatch(Match m, boolean singleSource, boolean singleTarget) {
        if (singleTarget) removeMatchesForSourceFeature(m.getSource());
        if (singleSource) removeMatchesForTargetFeature(m.getTarget());
    }
    
    /**
     * Remove a match from the map.
     */
    public void removeMatch(Match m) {
        matches.remove(m);
        sourceMap.get(m.getSource()).remove(m);
        if (sourceMap.get(m.getSource()).size() == 0) sourceMap.remove(m.getSource());
        targetMap.get(m.getTarget()).remove(m);
        if (targetMap.get(m.getTarget()).size() == 0) targetMap.remove(m.getTarget());
    }
    
    /**
     * Filter the matchMap so that each source feature has only one target match
     * and/or each target feature has only one source match.
     */
    public MatchMap filter(boolean singleSource, boolean singleTarget) {
        if (!singleSource && !singleTarget) return this;
        TreeSet<Match> filteredMatches = new TreeSet<Match>();
        while (matches.size() > 0) {
            Match m = matches.first();
            // put the best match pair in the new filteredMatches TreeSet
            filteredMatches.add(m);
            //System.out.println("   New Set " + Arrays.toString(filteredMatches.toArray()));
            // remove all sources features matching m.target and/or 
            // all target features matching m.source from matches
            removeMatch(m, singleSource, singleTarget);
            //System.out.println("   Original Map " + this);
        }
        MatchMap matchMap = new MatchMap();
        for (Match m : filteredMatches) matchMap.add(m);
        System.out.println("   New Match Map " + matchMap);
        return matchMap;
    }
    
    public int size() {
        return matches.size();
    }
    
    public void clear() {
        matches.clear();
        sourceMap.clear();
        targetMap.clear();
    }
    
    public String toString() {
        return "MatchMap : " + Arrays.toString(matches.toArray());
    }
}
