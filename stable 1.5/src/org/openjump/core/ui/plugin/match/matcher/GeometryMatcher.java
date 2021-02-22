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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;

/**
 * Interface for all simple geometry based matchers.
 * Inherits Matcher methods.
 *
 * @see Matcher
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public abstract class GeometryMatcher extends AbstractMatcher {
    
    /**
     * Returns a distance measuring the match quality of Geometry g with a 
     * reference Geometry ref.
     * The method returns 0 if g and ref do not match at all, and 1 if they
     * match perfectly.
     * If (g == ref), match should always return 1, but the return value of
     * match(g, ref, context) if g.equals(ref) depends of the exact semantic
     * of the matcher.
     * It is not required that match(g, ref, context) = match(ref, g, context). 
     *
     * @param source Geometry to match from
     * @param target Geometry to match to
     * @param context object containing useful information to check if
     * Geometry g effectively matches Geometry ref
     *
     * @return a double in the range [0-1] representative of the match quality
     * between g and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
    public abstract double match(Geometry source, Geometry target, Object context) 
                                                               throws Exception;
    
    /**
     * {@inheritDoc}.
     */
     public double match(Feature source, Feature target, Object context) throws Exception {
         return match(source.getGeometry(), target.getGeometry(), context);
     }

}
