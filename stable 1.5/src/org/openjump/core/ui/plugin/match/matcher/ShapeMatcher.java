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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;


/**
 * ShapeMatcher compares two geometries :
 * - centroid of first geometry is moved to the centroid of second geometry
 * - intersection of aligned geometries is computed
 *   (for lineal geometries, a buffer of half the max_dist is computed first)
 * - worst ratio of intersection and both geometry area is computed
 * - matching value is evaluated from 0 (50% overlapping) to 1 (100% overlapping)
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class ShapeMatcher extends GeometryMatcher {
    
    private static final ShapeMatcher SHAPE =
        new ShapeMatcher(1.0, 50.0);
    
    public static final ShapeMatcher instance() {
        return SHAPE;
    }
    
    private ShapeMatcher(double max_dist, double min_overlap) {
        this.max_dist = max_dist;
        this.min_overlap = min_overlap;
    }
    
    public double match(Geometry source, Geometry target, Object context) 
                                                              throws Exception {
        Coordinate c1 = source.getCentroid().getCoordinate();
        Coordinate c2 = target.getCentroid().getCoordinate();
        if (c1.distance(c2) > max_dist) return 0.0; // short-circuit
        AffineTransformation trans = AffineTransformation.translationInstance(-c1.x, -c1.y);
        trans.translate(c2.x, c2.y);
        source = (Geometry)source.clone();
        source.apply(trans);
        if (source.getDimension() == 1) source = source.buffer(max_dist/2);
        if (target.getDimension() == 1) target = target.buffer(max_dist/2);
        double overlappingArea = source.intersection(target).getArea();
        double overlapping = 100.0 * Math.min(overlappingArea/source.getArea(),
                                              overlappingArea/target.getArea());
        return (overlapping-min_overlap)/(100.0-min_overlap);
    }
    
    /**
     * Sets the maximum distance returning a non null match value.
     * @see #getMaximumDistance
     */
    public void setMaximumDistance(double max_dist) {
        this.max_dist = max_dist;
    }
    
    
    /**
     * Sets the minimum overlapping returning a non null match value.
     * @see #getMinimumOverlapping
     */
    public void setMinimumOverlapping(double min_overlap) {
        this.min_overlap = min_overlap;
    }
    
}
