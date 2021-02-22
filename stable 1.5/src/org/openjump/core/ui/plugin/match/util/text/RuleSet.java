/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A set of rules transforming a String into another String.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class RuleSet extends AbstractRule {

    Rule[] rules;

    StopCondition stopCondition;

    /**
     * Creates a RuleSet from a list of {@link Rule}s.
     */
    public RuleSet(Rule...rules) {
        this.rules = rules;
    }

    /**
     * Creates a RuleSet from a list of {@link Rule}s.
     * This constructor accept a StopCondition to stop applying rules to the
     * input String when this condition is satisfied.
     *
     * @param stopCondition condition to stop applying the rules
     * @param rules the rules to apply until the stopCondition is satisfied
     */
    public RuleSet(StopCondition stopCondition, Rule...rules) {
        this.rules = rules;
        this.stopCondition = stopCondition;
    }

    /**
     * Returns an Iterator to iterate through this RuleSet rules.
     */
    public Iterator<Rule> getRuleIterator() {
        return Arrays.asList(rules).iterator();
    }

    /**
     * Implements {@link Rule#transform(String s, Object context)}.
     */
    public String transform(String s, Object context) throws TransformationException {
        for (Rule rule : rules) {
            s = rule.transform(s, context);
            if (null != stopCondition && stopCondition.valid(s)) return s;
        }
        return s;
    }
    
    /**
     * Interface defining a condition to stop to transform a String when
     * the partial transformation fulfill a certain condition.
     */
    public static interface StopCondition {
        public boolean valid(String s);
    }
    
    /**
     * Implementation of the StopCondition Interface checking the length
     * of the resulting String.
     */
    public static class MaxLengthCondition implements StopCondition {
        int len;
        public MaxLengthCondition(int len) {
            this.len = len;
        }
        public boolean valid(String s) {
            return s.length() <= len;
        }
    }

}