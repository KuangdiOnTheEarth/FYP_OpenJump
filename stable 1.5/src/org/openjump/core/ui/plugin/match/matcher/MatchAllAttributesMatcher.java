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
import org.openjump.core.ui.plugin.match.Index;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract Matcher implementing common methods
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class MatchAllAttributesMatcher extends AttributeMatcher {
    
    public static final MatchAllAttributesMatcher MATCH_ALL = 
        new MatchAllAttributesMatcher();
    
    public static final MatchAllAttributesMatcher instance() {
        return MATCH_ALL;
    }
    
    private MatchAllAttributesMatcher() {
        super("","");
    }
    
    /**
     * {@inheritDoc}.
     */
    public double match(Object source, Object target, Object context) throws Exception {
        return 1.0;
    }
    
    public Index createIndex(final Collection<Feature> features) throws Exception {
        return new Index() {
            public Set<Feature> query(Object value) {
                return new HashSet(features);
            }
        };   
    }
    
}
