/*
Copyright (c) 2011, Michaël Michaud
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
import com.vividsolutions.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Matcher measuring hausdorff distance between geometries.
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class SemiHausdorffDistanceMatcher extends HausdorffDistanceMatcher {
    
    private static final SemiHausdorffDistanceMatcher SEMI_HAUSDORFF_DISTANCE =
        new SemiHausdorffDistanceMatcher(1.0);
    
    public static SemiHausdorffDistanceMatcher instance() {
        return SEMI_HAUSDORFF_DISTANCE;
    }
    
    protected SemiHausdorffDistanceMatcher(double max_dist) {
        super(max_dist);
    }
    
    /**
     * {@inheritDoc}.
     */
    public double match(Geometry source, Geometry target, Object context) 
                                                              throws Exception {
        DiscreteHausdorffDistance D = new DiscreteHausdorffDistance(source, target);
        double maxDxDy = maxDxDy(source);
        // If maxDxDy is "much greater than" max_dist densification the geometry
        if (max_dist < 0.75*maxDxDy) D.setDensifyFraction(max_dist/maxDxDy);
        double dist = D.orientedDistance();
        if (dist > max_dist) return 0.0;
        else return 1.0 - dist / max_dist;
    }
    
    /**
     * Sets the maximum Hausdorff distance accepted between two geometries.
     * @see #getMaximumDistance
     */
    public void setMaximumDistance(double max_dist) {
        this.max_dist = max_dist;
    }
    
}
