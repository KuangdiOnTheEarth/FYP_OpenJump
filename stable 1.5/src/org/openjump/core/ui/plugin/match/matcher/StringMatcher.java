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
import org.openjump.core.ui.plugin.match.util.text.Rule;
import org.openjump.core.ui.plugin.match.util.text.RuleRegistry;
import org.openjump.core.ui.plugin.match.util.text.TransformationException;
import java.text.Collator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/**
 * Interface for all attribute matchers.
 * Inherits Matcher methods.
 *
 * @see Matcher
 *
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public abstract class StringMatcher extends AttributeMatcher {
    
    Collator collator = Collator.getInstance(Locale.getDefault());
    Rule sourceRule = RuleRegistry.NEUTRAL;
    Rule targetRule = RuleRegistry.NEUTRAL;
    
    StringMatcher(String sourceAttribute, String targetAttribute) {
        super(sourceAttribute, targetAttribute);
        collator.setStrength(Collator.IDENTICAL);
    }
    
    /**
     * Returns a distance measuring the match between two character strings.
     * The method returns 0 if source and target do not match at all, and 1 if 
     * they match perfectly.
     * If (source == target), match should always return 1, but the return value
     * of match(source, target, context) if source.equals(target) depends of the
     * exact semantic of the matcher.
     * It is not required that 
     * match(source, target, context) = match(target, source, context). 
     *
     * @param source feature to match from
     * @param target feature to match to
     * @param context object containing useful information to check if
     * Attribute source effectively matches Attribute target
     *
     * @return a double in the range [0-1] representative of the match quality
     * between a and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
    public abstract double match(String source, String target, Object context)
                                                               throws Exception;
    
    /**
     * {@inheritDoc}.
     */
    public double match(Object source, Object target, Object context) 
                                                              throws Exception {
        return match(source.toString(), target.toString(), context);
    }
    
    /**
     * {@inheritDoc}.
     */
    public double match(Feature source, Feature target, Object context) 
                                                              throws Exception {
        return match(source.getString(sourceAttribute), 
                      target.getString(targetAttribute), context);
    }
    
    public void setSourceRule(Rule sourceRule) {
        this.sourceRule = sourceRule;
    }
     
    public void setTargetRule(Rule targetRule) {
        this.targetRule = targetRule;
    }
    
    public Rule getSourceRule() {
        return sourceRule;
    }
     
    public Rule getTargetRule() {
        return targetRule;
    }
    
    /**
     * The default index for StringMatcher maps each possible target 
     * attribute value to the features having this value.
     * @param features features to index
     * @throws TransformationException if the targetRule could not be applied
     *         to target features to build the index.
     */
    public Index createIndex(final Collection<Feature> features) 
                                                 throws TransformationException {
        final TreeMap<String,Set<Feature>> index = 
            new TreeMap<String,Set<Feature>>(collator);
        for (Feature f : features) {
            String value = targetRule.transform(f.getString(getTargetAttribute()));
            Set set = index.get(value);
            if (set == null) {
                set = new HashSet();
                index.put(value, set);
            }
            set.add(f);
        }
        return new Index() {
            public Set<Feature> query(Object value) {
                return index.get(value.toString());
            }
        };
    }

}
