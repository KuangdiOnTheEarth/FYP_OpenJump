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

/**
 * Interface for all simple matchers able to evaluate if a feature f matches
 * a reference feature ref and how well it matches.
 * Matcher is not symmetric. For example, IncludeMatcher will return 1 for
 * match(f, ref) if f is included in ref, and 0 for match(ref, f).
 * On the other hand, minimum distance should be symmetric.
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public interface Matcher {
    
    public static final String MAXIMUM_DISTANCE = 
        I18NPlug.getI18N("Maximum-distance");
    
    public static final String MINIMUM_OVERLAPPING = 
        I18NPlug.getI18N("Minimum-overlapping");
    
    /**
     * Returns a distance measuring the match quality of Feature f with a 
     * reference Feature ref.
     * The method returns 0 if f and ref do not match at all, and 1 if they
     * match perfectly.
     * If (f == ref), match should always return 1, but the return value of
     * match(f, ref, context) if f.equals(ref) depends of the exact semantic
     * of the matcher.
     * It is not required that match(f, ref, context) = match(ref, f, context). 
     *
     * @param f Feature to match from
     * @param ref reference Feature to match to
     * @param context object containing useful information to check if
     * Feature f effectively matches Feature ref
     *
     * @return a double in the range [0-1] representative of the match quality
     * between f and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
    public double match(Feature f, Feature ref, Object context) throws Exception;
    
    
    /**
     * Returns the maximum distance accepted between f1 and ref
     * Exact meaning highly depends on what distance is measured, but whatever 
     * the definition is (minimum distance, hausdorff distance, levenshtein
     * distance...), if distance between f and ref is over the maximum
     * distance, the value returned by match method will be 0.
     * <ul>
     * <li>
     * If 0 is returned, match will always return 0 except for two
     * identical features (identical meaning depends on matcher definition)
     * </li>
     * <li>
     * Returning NaN means that using a tolerance has no meaning for this 
     * matcher (example, getMaximumDistance of equals matchers returns NaN).
     * </li>
     * <li>
     * If Double.POSITIVE_INFINITY is returned, matches between f and ref will
     * alway return a non null value.
     * </li>
     * </ul>
     */
    public double getMaximumDistance();
    
    
    /**
     * Returns the minimum overlapping between f and ref, where overlapping is
     * generally expressed as a percentage, but not necessarily.
     * Overlapping may have different meanings as the ratio between common area
     * and f area or between the length of the longest common substring of two
     * attributes values and the length of the full string.<br>
     * Depending on the Matcher, overlapping between f and ref may be
     * directional (ex. common area / f area) or symmetric (intersection area
     * / union area).
     * <ul>
     * <li>
     * If 0 is returned, any pair of f and ref which intersects will return a
     * non null value.
     * </li>
     * <li>
     * NaN means that this criteria has no meaning for this matcher.
     * </li>
     * <li>
     * 100.0 generally means that f and ref must be equal, but the precise
     * definition may bary from a matcher to another.
     * </li>
     * </ul>
     */
    public double getMinimumOverlapping();
    
    /**
     * Sets the maximum distance returning a non null value.
     * @see #getMaximumDistance
     */
    public void setMaximumDistance(double max_dist);
    
    /**
     * Sets the minimum overlapping ratio returning a non null value.
     * @see #getMinimumOverlapping
     */
    public void setMinimumOverlapping(double min_overlap);

}
