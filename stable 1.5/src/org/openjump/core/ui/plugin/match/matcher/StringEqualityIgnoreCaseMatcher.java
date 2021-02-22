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
import org.openjump.core.ui.plugin.match.Index;
import java.text.Collator;
import java.util.Locale;

/**
 * String equality matcher to check if two attributes are equals ignoring case.
 *
 * @see Matcher
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class StringEqualityIgnoreCaseMatcher extends StringMatcher {
    
    private static final StringEqualityIgnoreCaseMatcher STRING_EQUALITY_IGNORE_CASE =
        new StringEqualityIgnoreCaseMatcher("","");
    
    public static StringEqualityIgnoreCaseMatcher instance() {
        return STRING_EQUALITY_IGNORE_CASE;
    }
    
    StringEqualityIgnoreCaseMatcher(String sourceAttribute, String targetAttribute) {
        super(sourceAttribute, targetAttribute);
        collator.setStrength(Collator.SECONDARY);
    }
    
    /**
     * Returns 1.0 if attribute A.equalsIgnoreCase(AttributeB), 0.0 otherwise.
     * @param source attribute to match from
     * @param target attribute to match to
     * @param context object containing useful information to check if
     * Attribute a effectively matches Attribute ref
     *
     * @return a double in the range [0-1] representative of the match quality
     * between a and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
     public double match(String source, String target, Object context) 
                                                              throws Exception {
         if (source == null || target == null) return 0.0;
         return source.equalsIgnoreCase(target) ? 1.0 : 0.0;
     }

}
