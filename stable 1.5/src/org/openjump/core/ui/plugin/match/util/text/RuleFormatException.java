/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

/**
 * Exception thrown when a Rule definition (ex. a PatternRule based on a
 * regular expression) does not fit the Rule format.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class RuleFormatException extends Exception {

    public RuleFormatException(String message) {
        super(message);
    }

}