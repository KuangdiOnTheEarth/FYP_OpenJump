/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

import java.util.regex.Pattern;

/**
 * A rule transforming a string into another string, based on a regular
 * expression and a replacement String.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class ReplacePatternRule extends AbstractRule {

    private Pattern _pattern;
    
    private String _replacement;

    /**
     * Creates a new Pattern rule from a pattern String and a replacement String.
     *
     * @param pattern the string representation of the regular expression pattern
     * @param replacement the replacement string
     */
    public ReplacePatternRule(String pattern, String replacement) {
        this._pattern = Pattern.compile(pattern);
        this._replacement = replacement;
    }

    /**
     * Creates a new ReplacePatternRule rule from a compiled Pattern and a
     * replacement String.
     *
     * @param pattern the regular expression Pattern
     * @param replacement the replacement string
     */
    public ReplacePatternRule(Pattern pattern, String replacement) {
        this._pattern = pattern;
        this._replacement = replacement;
    }

   /**
    * Transforms a String into another String.
    * @param s the String to transform
    * @param context a context object or null if no context information is available
    * @return the transformed String
    */
    public String transform(String s, Object context) throws TransformationException {
        try {
            return s == null ? null:_pattern.matcher(s).replaceAll(_replacement);
        } catch(IndexOutOfBoundsException e) {
            throw new TransformationException(
                "Exception using pattern " + _pattern.toString() +
                ": the replacement string \"" + _replacement +
                "\" refers to a capturing group that does not exist in the pattern");
        }
    }

    /**
     * String representation of this ReplacePatternRule.
     */
    public String toString() {
        return "Changes \"" + _pattern.toString() + "\" into \"" + _replacement + "\"";
    }

}