/*
 * (C) 2011 michael.michaud@free.fr
 */
 
package org.openjump.core.ui.plugin.match.util.text;

import org.openjump.core.ui.plugin.match.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
* Registry where {@link Rule}s can be easily set and retrieved.
 *
 * @author Michael Michaud
 * @version 0.3 (2011-04-25)
 */
// History
// 0.3 (2011-04-25) add javadoc
// 0.2 (2011-03-26) keep file extension for file-based rules 
// 0.1 (2009-09-27)

public final class RuleRegistry {
    
    // Some useful patterns
    //private static final Pattern SWITCH_PARENTHESIS = Pattern.compile("([^\\(\\)]*)( )?(?:\\(([^\\(\\)]*)\\))");
    
    /**
     * Pattern matching a french article placed after the name, between
     * a pair of parenthesis.
     * Writing articles after the name is sometimes done to facilitate
     * the lexicographical search of a geoname.
     */
    private static final Pattern ARTICLE_AFTER = Pattern.compile("^(?i)(.*) ?\\((LES?|LA|L'|UNE?|DE LA|DES?|DU|D'|AUX?)\\)$");
    
    /**
     * Pattern matching a french article placed in front of the name.
     */
    private static final Pattern ARTICLE_BEFORE = Pattern.compile("^(?i)(LES? |LA |L'|L |UNE? |DE LA |DES? |DU |D'|D |AUX? )(.*)$");
    
    /**
     * Pattern matching words SAINT and SAINTE in order to abreviate them.
     * Pattern is case insensitive.
     */
    private static final Pattern SAINT = Pattern.compile("(?i)\b(S)(AINT)(E?)\b");

    /**
     * HashMap containing predefined rules or rules added by the user.
     * Each rule is identified by a name. Different rules cannot have the same name.
     * By convention, pre-defined rules have an uppercase name,
     * while user defined file-based rules take the name of the file. 
     */
    private static final Map<String,Rule> REGISTRY = new HashMap<String,Rule>();

    // Some current rules
    public static final Rule NEUTRAL = new AbstractRule(){
        public String transform(String s, Object context) {return s;}
    };

    public static final Rule TO_UPPERCASE = new AbstractRule(){
        public String transform(String s, Object context) {
            return s==null?null:s.toUpperCase();
        }
    };

    public static final Rule TO_LOWERCASE = new AbstractRule(){
        public String transform(String s, Object context) {
            return s==null?null:s.toLowerCase();
        }
    };

    public static final Rule TO_ASCII = new AbstractRule(){
        public String transform(String s, Object context) {return StringUtil.cp1252toASCII(s);}
    };

    public static final Rule TO_ASCII38 = new AbstractRule(){
        public String transform(String s, Object context) {return StringUtil.cp1252toASCII38(s);}
    };

    public static final Rule NORMALIZE_SPACES = new AbstractRule(){
        public String transform(String s, Object context) {return StringUtil.normalize(s);}
    };

    public static final Rule REMOVE_PARENTHESES = new AbstractRule(){
        public String transform(String s, Object context) {
            if (s==null) return null;
            int start = s.indexOf("(");
            int end = s.indexOf(")");
            if (start >= 0 && end > start) {
                return s.substring(0,start) + s.substring(end, s.length());
            }
            else return s;
        }
    };
    
    public static final Rule REMOVE_INITIAL_ARTICLE = new AbstractRule(){
        public String transform(String s, Object context) {
            if (s==null) return null;
            return ARTICLE_BEFORE.matcher(s).replaceAll("$2");
        }
    };

    /** Switch parenthesis content in front of the String*/
    public static final Rule MOVE_ARTICLE_BEFORE = new AbstractRule(){
        public String transform(String s, Object context) {
            if (s==null) return null;
            Matcher m = ARTICLE_AFTER.matcher(s);
            if (m.matches()) {
                String word = m.group(1);
                String article = m.group(2);
                return article.endsWith("'") ? article+word : article+" "+word;
            }
            else return s;
        }
    };
    
    /** Switch parenthesis content in front of the String*/
    public static final Rule MOVE_ARTICLE_AFTER = new AbstractRule(){
        public String transform(String s, Object context) {
            if (s==null) return null;
            Matcher m = ARTICLE_BEFORE.matcher(s);
            if (m.matches()) {
                String article = m.group(1).trim();
                String word = m.group(2);
                return article.length() == 1 ? word + " (" + article + "')"
                                             : word + " (" + article + ")";
            }
            else return s;
        }
    };
    
    /** Switch parenthesis content in front of the String*/
    public static final Rule ABBREVIATE_SAINT = new AbstractRule(){
        public String transform(String s, Object context) {
            if (s==null) return null;
            Matcher m = SAINT.matcher(s);
            if (m.matches()) {
                return m.group(1) + m.group(2).charAt(3) + m.group(3);
            }
            else return s;
        }
    };


    /**
     * Remove all rules from the registry.
     */
    public static void clearRegistry() {
        REGISTRY.clear();
    }


    /**
     * Put rule in the registry with key "name".
     * If the key name already exists, the corresponding rule is replaced.
     */
    public static void put(String name, Rule rule) {
        REGISTRY.put(name, rule);
    }


    /**
     * Tests if the Registry already contains a rule called name.
     */
    public static boolean contains(String name) {
        return REGISTRY.containsKey(name);
    }
    

    /**
     * Gets the rule inserted with key "name".
     */
    public static Rule getRule(String name) throws RuleNotFoundException {
        Rule rule = REGISTRY.get(name);
        if (rule != null) return REGISTRY.get(name);
        else throw new RuleNotFoundException(name);
    }


    /**
     * Get the name of all the rules in this registry.
     */
    public static String[] getRules() {
        return REGISTRY.keySet().toArray(new String[]{});
    }
    
    
    /**
     * Transform a String using the rule with name "name".
     * @param s the String to transform
     * @param ruleName the name of the rule to be used
     */
     public static String transforms(String s, String ruleName) throws TransformationException {
         return REGISTRY.get(ruleName).transform(s);
     }
     
     
     /**
      * Transform a String using the rule with name "name".
      * @param s the String to transform
      * @param ruleName the name of the rule to be used
      * @param context the context to perform the transformation
      */
     public static String transforms(String s, String ruleName, Object context)
                                                throws TransformationException {
         return REGISTRY.get(ruleName).transform(s, context);
     }

    /**
     * Loads default rules and file based rules contained in dir folder.
     * @param dir the folder containing rules definition.
     */
    public static void loadRules(String dir) throws IOException,
                                                    RuleFormatException,
                                                    RuleNotFoundException,
                                                    PatternSyntaxException {
        put("", NEUTRAL);
        put("NEUTRAL", NEUTRAL);
        put("TO_LOWERCASE", TO_LOWERCASE);
        put("TO_UPPERCASE", TO_UPPERCASE);
        put("TO_ASCII", TO_ASCII);
        put("TO_ASCII38", TO_ASCII38);
        put("NORMALIZE_SPACES", NORMALIZE_SPACES);
        put("REMOVE_PARENTHESES", REMOVE_PARENTHESES);
        put("MOVE_ARTICLE_BEFORE", MOVE_ARTICLE_BEFORE);
        put("MOVE_ARTICLE_BEHIND", MOVE_ARTICLE_AFTER);
        put("REMOVE_ARTICLE", REMOVE_INITIAL_ARTICLE);
        put("ABBREVIATE_SAINT", ABBREVIATE_SAINT);
        File directory = new File (dir);
        if (!directory.isDirectory())
            throw new IllegalArgumentException(dir + " is not a valid directory");

        for (File f : directory.listFiles()) {
            try {
                if (f.getName().endsWith("prs")) {
                    //put(StringUtil.removeTail(f.getName(),".prs"),
                    put(f.getName(), new ReplacePatternRuleSet(f.getPath()));
                }
            }
            catch(RuleFormatException rfe) {
                throw rfe;
            }
        }
        try {
            put("AFNOR", new ReplacePatternRuleSet(new File(directory, "AFNOR.prs").getPath(), new RuleSet.MaxLengthCondition(38)));
        }
        catch(RuleFormatException rfe) {throw rfe;}
        catch(FileNotFoundException fnfe) {/*swallow it*/}
        catch(IOException ioe) {throw ioe;}
        catch(PatternSyntaxException pse) {throw pse;}
        for (File f : directory.listFiles()) {
            try {
                if (f.getName().endsWith("nrs")) {
                    //put(StringUtil.removeTail(f.getName(),".nrs"),
                    put(f.getName(), new NamedRuleSet(f.getPath()));
                }
            }
            catch(RuleNotFoundException rnfe) {
                // at first iteration, there may be missing named rule in Registry
            }
        }
        for (File f : directory.listFiles()) {
            try {
                if (f.getName().endsWith("nrs")) {
                    //put(StringUtil.removeTail(f.getName(),".nrs"),
                    put(f.getName(), new NamedRuleSet(f.getPath()));
                }
            }
            catch(RuleNotFoundException rnfe) {
                throw rnfe;
            }
        }
    }

}