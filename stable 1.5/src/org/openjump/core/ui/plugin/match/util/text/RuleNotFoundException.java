/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

/**
 * Exception thrown when a {@link Rule} cannot be found.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class RuleNotFoundException extends Exception {

    public RuleNotFoundException(String message) {
        super("\"" + message + "\"");
    }

}