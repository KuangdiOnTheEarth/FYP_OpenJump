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
import org.openjump.core.ui.plugin.match.matcher.*;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Matcher Registry
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class MatcherRegistry {
    
    //public static MatcherRegistry EQUALS_GEOMETRY_MATCHERS = new MatcherRegistry(
    //    EqualsExactGeom3dMatcher.instance(),
    //    EqualsNormalizedGeom3dMatcher.instance(),
    //    EqualsExactGeom2dMatcher.instance(),
    //    EqualsNormalizedGeom2dMatcher.instance(),
    //    EqualsTopologicalGeomMatcher.instance(),
    //    EqualsWithCoordinateToleranceMatcher.instance()
    //);
    
    //public static MatcherRegistry INTERSECTS_GEOMETRY_MATCHERS = new MatcherRegistry(
    //    IsWithinMatcher.instance(),
    //    OverlapsMatcher.instance(),
    //    OverlappedByMatcher.instance(),
    //
    //    IntersectsMatcher.instance(),
    //    Intersects0DMatcher.instance(),
    //    Intersects1DMatcher.instance(),
    //    Intersects2DMatcher.instance()
    //);
    
    //public static MatcherRegistry DISTANCE_GEOMETRY_MATCHERS = new MatcherRegistry(
    //    CentroidDistanceMatcher.instance(),
    //    MinimumDistanceMatcher.instance(),
    //    HausdorffDistanceMatcher.instance(),
    //    SemiHausdorffDistanceMatcher.instance(),
    //    ShapeMatcher.instance()
    //);
    
    
    public static MatcherRegistry GEOMETRY_MATCHERS = new MatcherRegistry (
        MatchAllMatcher.instance(),
        EqualsExactGeom3dMatcher.instance(),
        EqualsNormalizedGeom3dMatcher.instance(),
        EqualsExactGeom2dMatcher.instance(),
        EqualsNormalizedGeom2dMatcher.instance(),
        EqualsTopologicalGeomMatcher.instance(),
        EqualsWithCoordinateToleranceMatcher.instance(),

        IsWithinMatcher.instance(),
        OverlapsMatcher.instance(),
        OverlappedByMatcher.instance(),

        IntersectsMatcher.instance(),
        Intersects0DMatcher.instance(),
        Intersects1DMatcher.instance(),
        Intersects2DMatcher.instance(),

        MinimumDistanceMatcher.instance(),
        CentroidDistanceMatcher.instance(),
        HausdorffDistanceMatcher.instance(),
        SemiHausdorffDistanceMatcher.instance(),
        ShapeMatcher.instance()
    );
    
    public static MatcherRegistry STRING_MATCHERS = new MatcherRegistry (
        MatchAllStringsMatcher.instance(),
        StringEqualityMatcher.instance(),
        StringEqualityIgnoreCaseMatcher.instance(),
        StringEqualityIgnoreCaseAndAccentMatcher.instance(),
        LevenshteinDistanceMatcher.instance(),
        DamarauLevenshteinDistanceMatcher.instance()
    );
    
    private Map<String,Matcher> map = new LinkedHashMap<String,Matcher>();
    
    public void register(Matcher matcher) {
        map.put(matcher.toString(), matcher);
    }
    
    MatcherRegistry(Matcher... matchers) {
        for (Matcher matcher : matchers) register(matcher);
    }
    
    public Matcher get(String name) {
        return map.get(name);
    }
    
    public Map<String,Matcher> getMap() {
        return map;
    }
    
    public static Matcher getMatcher(MatcherRegistry registry, String name) {
        return registry.map.get(name);
    }
    
}
