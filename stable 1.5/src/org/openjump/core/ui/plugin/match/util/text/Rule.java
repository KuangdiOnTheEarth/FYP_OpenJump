/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

/**
 * Interface defining a method to transform a String into another String.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public interface Rule {

   /**
    * Transforms a String into another String, according to context information.
    * @param s the String to transform
    * @param context the transformation context
    * @return the transformed String
    */
    public String transform(String s, Object context) throws TransformationException;


   /**
    * Transforms a String into another String.
    * @param s the String to transform
    * @return the transformed String
    */
    public String transform(String s) throws TransformationException;

}