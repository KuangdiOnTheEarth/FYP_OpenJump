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
import org.openjump.core.ui.plugin.match.I18NPlug;
import org.openjump.core.ui.plugin.match.Matcher;

/**
 * Abstract Matcher implementing common methods
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public abstract class AbstractMatcher implements Matcher {
    
    protected double max_dist = Double.NaN;
    
    protected double min_overlap = Double.NaN;
    
    /**
     * {@inheritDoc}.
     */
    public abstract double match(Feature f, Feature ref, Object context) throws Exception;
    
    /**
     * The {@link #match(Feature,Feature,Object)} method without context.
     */
     public double match(Feature f, Feature ref) throws Exception {
         return match(f, ref, null);
     }
    
    /**
     * Returns the name of this Matcher
     */
     public String toString() {
         return I18NPlug.getI18N("matcher." + getClass().getSimpleName());
     }
     
    /**
     * Returns NaN means that this criteria has no meaning for this matcher.
     */
     public double getMaximumDistance() {
         return max_dist;
     }
    
    /**
     * Returns NaN means that this criteria has no meaning for this matcher.
     */
     public double getMinimumOverlapping() {
         return min_overlap;
     }
     
    /**
     * In this main abstract implementation, setMaximumDistance has no effect.
     */
     public void setMaximumDistance(double max_dist) {
         this.max_dist = Double.NaN;
     }
     
    /**
     * In this main abstract implementation, setMinimumOverlapping has no effect.
     */
     public void setMinimumOverlapping(double min_overlap) {
         this.min_overlap = Double.NaN;
     }

}
