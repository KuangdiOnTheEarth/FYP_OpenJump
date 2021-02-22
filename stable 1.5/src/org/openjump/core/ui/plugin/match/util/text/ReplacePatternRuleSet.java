/*
 * (C) 2011 michael.michaud@free.fr
 */

package org.openjump.core.ui.plugin.match.util.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Set of replacement rules based on regular expressions.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class ReplacePatternRuleSet extends RuleSet {


    /**
     * Creates a ReplacePatternRuleSet from a list of {@link ReplacePatternRule}s.
     */
    public ReplacePatternRuleSet(ReplacePatternRule...rules) {
        super(rules);
    }

    /**
     * Creates a ReplacePatternRuleSet from a list of {@link ReplacePatternRule}s.
     * Accept a StopCondition to stop the process if a condition is 
     */
    public ReplacePatternRuleSet(StopCondition stopCondition, Rule...rules) {
        super(stopCondition, rules);
    }

    /**
     * Creates a Rule set from an array of array where each rule is defined as 
     * a pattern/replacement pair of Strings.
     *
     * @param replacementRules an array made of small two string array, each
     * the pattern and the replacement string
     */
    public ReplacePatternRuleSet(String[][] replacementRules)
                       throws IllegalArgumentException, PatternSyntaxException {
        List<ReplacePatternRule> ruleSet = new ArrayList<ReplacePatternRule>();
        int count = 0;
        for (String[] rule : replacementRules) {
            if (rule.length < 2) {
                throw new IllegalArgumentException("Array " + Arrays.toString(rule) + " must be a pair of strings to define a rule");
            }
            if (rule[0]==null || rule[0].length()==0) continue;
            if (rule.length==1 || rule[1]==null) rule[1]="";
            ruleSet.add(new ReplacePatternRule(rule[0], rule[1]));
        }
        rules = ruleSet.toArray(new ReplacePatternRule[count]);
    }

    /**
     * Creates a ReplacePatternRuleSet from a file and a StopCondition.
     * See in {@link #ReplacePatternRuleSet(String)} the definition of the file format.
     *
     * @param file the file where the pattern / replacement strings are listed
     * @param stopCondition the optional {@link RuleSet.StopCondition}
     */
    public ReplacePatternRuleSet(String file, StopCondition stopCondition) throws
                                              IOException,
                                              FileNotFoundException,
                                              RuleFormatException,
                                              PatternSyntaxException {
        this(file);
        super.stopCondition = stopCondition;
    }

    /**
     * Creates a new ReplacePatternRuleSet from a file.<br>
     * The file is parsed as follow :<ul>
     * <li>Every line is trimmed (expression starting or ending by a white
     * space must be quoted)</li>
     * <li>Lines starting by # or // are interpreted as comment lines </li>
     * <li>Empty lines (or lines filled by spaces) are ignored</li>
     * <li>Lines are broken where = sign surrounded by double quotes are founded (" = ")</li>
     * <li>Otherwise, a single = sign is used as the delimiter</li>
     * </ul>
     * That means you must quote search and replace strings as soon as one of
     * them contains whitespaces or = sign. 
     */
    public ReplacePatternRuleSet(String file) throws IOException,
                                              FileNotFoundException,
                                              RuleFormatException,
                                              PatternSyntaxException {
        InputStream is = null;
        try {
            is = ReplacePatternRuleSet.class.getClassLoader().getResourceAsStream(file);
            if (is == null) {
                //try {
                    is = new FileInputStream(file);
                //} catch(FileNotFoundException fnfe) {throw fnfe;}
            }
            List<Rule> ruleList = new ArrayList<Rule>();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean comment = false;
            int line_number = 0;
            while (null != (line = br.readLine())) {
                line_number++;
                line = line.trim();
                if (line.startsWith("//") || line.startsWith("#") || line.length()==0) {
                    continue;
                }
                else {
                    try {
                        String[] tokens = line.split("\"[ ]*=[ ]*\"");
                        if (tokens.length == 2) {
                            tokens[0] = tokens[0] + "\"";
                            tokens[1] = "\"" + tokens[1];
                        }
                        else {
                            tokens = line.split("=");
                            if (tokens.length != 2) {
                                throw new RuleFormatException(file + ":" + line_number + " :\nthe PatternRule '" + line +"' does not follow one of the convention :\nPattern=Replacement or\n\"Pattern\"=\"Replacement\"\n(quotes must be used for empty strings or for strings containing the sign '='))");
                            }
                        }
                        tokens[0] = tokens[0].trim();
                        tokens[1] = tokens[1].trim();
                        if (tokens[0].startsWith("\"") && tokens[0].endsWith("\"")) {
                            tokens[0] = tokens[0].substring(1,tokens[0].length()-1);
                        }
                        if (tokens[1].startsWith("\"") && tokens[1].endsWith("\"")) {
                            tokens[1] = tokens[1].substring(1,tokens[1].length()-1);
                        }
                        ruleList.add(new ReplacePatternRule(Pattern.compile(tokens[0]), tokens[1]));
                    } catch(PatternSyntaxException pse) {
                        pse.printStackTrace();
                    }
                }
            }
            rules = ruleList.toArray(new Rule[ruleList.size()]);
            System.out.println("New ReplacePatternRuleSet " + file + " : " + rules.length + "rule(s)");
            is.close();
        } catch(IOException ioe) {
            if (is != null) {
                try {is.close();}
                catch(IOException e) {}
            }
            throw ioe;
        } finally {
            if (is != null) {
                try {is.close();}
                catch(IOException e) {}
            }
        }
    }

}