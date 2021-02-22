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
import com.vividsolutions.jts.geom.Geometry;


/**
 * Matcher checking if source geometry overlaps more than xx% of target geometry.
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class OverlapsMatcher extends GeometryMatcher {
    
    private static final OverlapsMatcher OVERLAPS =
        new OverlapsMatcher(50.0);
    
    public static final OverlapsMatcher instance() {
        return OVERLAPS;
    }
    
    public OverlapsMatcher(double min_overlap) {
        this.min_overlap = min_overlap;
    }
    
    public double match(Geometry g, Geometry ref, Object context) throws Exception {
        Geometry intersection = g.intersection(ref);
        double score = (100.0*intersection.getArea()/ref.getArea() - min_overlap)/(100-min_overlap);
        return Math.max(0, score);
    }
    
    /**
     * Sets the minimum overlapping returning a non null match value.
     * @see #getMinimumOverlapping
     */
    public void setMinimumOverlapping(double min_overlap) {
        this.min_overlap = min_overlap;
    }
    
}