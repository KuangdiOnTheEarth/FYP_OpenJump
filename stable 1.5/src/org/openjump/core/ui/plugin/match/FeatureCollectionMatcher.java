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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import com.vividsolutions.jump.feature.Feature;

import org.openjump.core.ui.plugin.match.matcher.*;
import org.openjump.core.ui.plugin.match.util.text.Rule;
import org.openjump.core.ui.plugin.validate.SharedSpace;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Matcher iterating through two FeatureCollection to find matching features.
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class FeatureCollectionMatcher {
    
    private Collection<Feature> source;
    private Collection<Feature> target;
    private GeometryMatcher geometryMatcher;
    private StringMatcher attributeMatcher;
    private MatchMap matchMap;
    
    // set n_m = true to try to match source features to several target 
    // features in one shot.
    private boolean n_m = false;
    private static final Matcher OVERLAP = OverlapsMatcher.instance();
    
    /**
     * A high level matcher able to compare features from two feature 
     * collections. It is able to compare pairs of features or to pre-process
     * the feature collection in order to find N-M matches.
     * @param collection1 the first feature collection
     * @param collection2 the second feature collection
     * @param geometryMatcher the Matcher to evaluate geometric similarity
     * @param attributeMatcher the matcher to evaluate semantic similarity
     */
    public FeatureCollectionMatcher(Collection<Feature> source,
                                    Collection<Feature> target,
                                    GeometryMatcher geometryMatcher,
                                    StringMatcher attributeMatcher) {
        if (geometryMatcher == MatchAllMatcher.MATCH_ALL) {
            geometryMatcher = null;
        }
        if (attributeMatcher == MatchAllStringsMatcher.MATCH_ALL) {
            attributeMatcher = null;
        }
        assert geometryMatcher != null || attributeMatcher != null :
           "A FeatureCollectionMatcher must have at least one Matcher";
        this.source = source;
        this.target = target;
        this.geometryMatcher = geometryMatcher;
        this.attributeMatcher = attributeMatcher;
        matchMap = new MatchMap();
    }
    
    /**
     * Main method trying to match all features from both input feature
     * collections and returning the set of source features matching one or
     * several target features.
     * @param singleSource whether a target Feature can be matched by several
     * source features or not.
     * @param singleTarget whether a source feature can match several target 
     * features or not.
     */
    public Collection<Feature> matchAll(boolean singleSource, 
                                        boolean singleTarget) throws Exception {
        if (geometryMatcher != null) {
            System.out.println("Geometry Matching");
            matchMap = geometryMatching(singleSource, singleTarget);
        }
        if (attributeMatcher != null) {
            System.out.println("Semantic Matching");
            matchMap = attributeMatching(singleSource, singleTarget);
        }
        else {
            assert geometryMatcher != null || attributeMatcher != null : 
                   "Invalid params (both geometric and attribute matchers are null !)";
        }
        //System.out.println("MatchMap before filter : \n" + matchMap.toString().replaceAll(",","\n"));
        matchMap = matchMap.filter(singleSource, singleTarget);
        //System.out.println("MatchMap after filter : \n" + matchMap.toString().replaceAll(",","\n"));
        return matchMap.getSourceFeatures();
    }
    
    public MatchMap getMatchMap() {
        return matchMap;
    }
    
    public void clearMatchMap() {
        matchMap.clear();
    }
    
    
    /**
     * Returns a MatchMap representing all the match scores obtained by 
     * comparing source feature geometries with target feature geometries with
     * the GeometryMatcher.
     * @param singleSource whether a target Feature can be matched by several
     * source features or not.
     * @param singleTarget whether a source feature can match several target 
     * features or not.
     */
    public MatchMap geometryMatching(boolean singleSource, boolean singleTarget) throws Exception {
        double maxDistance = geometryMatcher.getMaximumDistance();
        if (Double.isNaN(maxDistance)) maxDistance = 0.0;
        //System.out.println("Geometry Matching " + geometryMatcher + " " + maxDistance);
        long t0 = System.currentTimeMillis();
        double minOverlapping = geometryMatcher.getMinimumOverlapping();
        //System.out.println("geometryMatcher.minOverlapping = " + minOverlapping);
        STRtree index = indexFeatureCollection(target);
        // For each feature of the source collection
        
        MatchList matchList = new MatchList();
        
        for (Feature f1 : source) {
            //System.out.println("Feature " + f1.getID());
            Geometry g1 = f1.getGeometry();
            Envelope env = new Envelope(g1.getEnvelopeInternal());
            env.expandBy(maxDistance);
            List<Feature> candidates = index.query(env);
            // if matching_layer = reference_layer don't try to match f1 with itself
            candidates.remove(f1);
            // This loop can select several target features for one source
            // feature, a singleTarget filter must be applied afterwards
            int count = 0;
            for (Feature f2 : candidates) {
                double score = geometryMatcher.match(f1, f2, null);
                if (score > 0.0) {
                    matchMap.add(new Match(f1, f2, score));
                    count++;
                    
                    //////////////////////////////////////////////////////////////////////////
                    // Store the matches, to be used in the validation process
                    //////////////////////////////////////////////////////////////////////////
                    System.out.println("source: " + f1.getID() + " -- target: " + f2.getID());
                    matchList.storeMatch(f1, f2);
                }
            }
            
            // If one source can match multiple target 
            // and several target candidates are available
            // and some candidates have not been individually matched
            
            // TODO : try to short-circuit this loop 
            // if transfer attributes is not required
            if (!singleTarget && candidates.size() > 1 && !(count == candidates.size())) {
                Geometry globalTarget = union(candidates);
                // if g1 matches union of candidates, we try to attribute 
                // a score to each g1/candidate pair
                if (geometryMatcher.match(g1, globalTarget, null) > 0) {
                    Geometry g1Buffer = g1.buffer(maxDistance, 4);
                    for (Feature f2 : candidates) {
                        Geometry g2Buffer = f2.getGeometry().buffer(maxDistance, 4);
                        Geometry intersection = g1Buffer.intersection(g2Buffer);
                        if (intersection.isEmpty()) continue;
                        double ratio1 = intersection.getArea()/g1Buffer.getArea();
                        double ratio2 = intersection.getArea()/g2Buffer.getArea();
                        if (ratio1 < 0.25 && ratio2 < 0.25) continue;
                        if (ratio2 < 0.5) continue;
                        matchMap.add(new Match(f1, f2, 2.0*ratio2-1.0));
                    }
                }
            }
        }
        
		//////////////////////////////////////////////////////////////////////////
		// Store the matchList into SharedSpace
		//////////////////////////////////////////////////////////////////////////
        SharedSpace sharedSpace = SharedSpace.getInstance();
        sharedSpace.storeMatchList(matchList);
        
        System.out.println("Direct Geometry Matching done in " + (System.currentTimeMillis()-t0) + " ms");
        return matchMap;
    }
    
    private STRtree indexFeatureCollection(Collection<Feature> collection) {
        STRtree index = new STRtree();
        for (Feature f : collection) {
            index.insert(f.getGeometry().getEnvelopeInternal(), f);
        }
        return index;
    }
    
    private SortedMap<String,Collection<Feature>> indexFeatureCollection(Collection<Feature> collection, String attribute) {
        SortedMap<String,Collection<Feature>> map = new TreeMap<String,Collection<Feature>>();
        for (Feature f : collection) {
            String value = f.getString(attribute);
            Collection coll = map.get(value);
            if (coll == null) {
                coll = new ArrayList<Feature>();
                map.put(value, coll);
            }
            coll.add(f);
        }
        return map;
    }
    
    private Geometry union(List<Feature> features) {
        List geom = new ArrayList();
        for (Feature f : features) geom.add(f.getGeometry());
        return UnaryUnionOp.union(geom);
    }
    
    private MatchMap attributeMatching(boolean singleSource, boolean singleTarget) throws Exception {
        String sourceAttribute = attributeMatcher.getSourceAttribute();
        String targetAttribute = attributeMatcher.getTargetAttribute();
        Rule sourceRule = attributeMatcher.getSourceRule();
        Rule targetRule = attributeMatcher.getTargetRule();
        // If geometryMatcher is null, a simple join will be done.
        if (geometryMatcher == null && attributeMatcher != null) {
            Index index = attributeMatcher.createIndex(target);
            for (Feature f1 : source) {
                String sourceValue = sourceRule.transform(f1.getString(sourceAttribute));
                //System.out.println("sourceValue : " + sourceValue);
                Set<Feature> candidates = 
                    index.query(sourceValue);
                if (candidates == null) continue;
                else if (Double.isNaN(attributeMatcher.getMaximumDistance())) {
                    for (Feature f2 : candidates) {
                        matchMap.add(new Match(f1, f2, 1.0));
                    }
                }
                // In the case where a BKTree is used, there is room for 
                // optimizition because distances are already computed by the
                // BKTree query method
                else {
                    for (Feature f2 : candidates) {
                        double d = attributeMatcher.match(f1, f2, null);
                        matchMap.add(new Match(f1, f2, d));
                    }
                }
            }            
            // index attribute data
        }
        // If a geometry matching has already been done, attribute matching
        // use the resulting MatchMap from the geometry matching process 
        else {
            List<Match> null_matches = new ArrayList<Match>();
            for (Match m : matchMap.getAllMatches()) {
                //System.out.println("      Attribute matching " + m.getSource().getID() + "-" + m.getTarget().getID() + " : " + attributeMatcher.match(m.getSource(), m.getTarget(), null));
                String srcA = sourceRule.transform(m.getSource().getString(sourceAttribute));
                String tgtA = targetRule.transform(m.getTarget().getString(targetAttribute));
                m = m.combineScore(attributeMatcher.match(srcA, tgtA, null));
                if (m.getScore() == 0.0) null_matches.add(m);
            }
            for (Match m : null_matches) {
                matchMap.removeMatch(m);
            }
        }
        return matchMap;
    }

}
