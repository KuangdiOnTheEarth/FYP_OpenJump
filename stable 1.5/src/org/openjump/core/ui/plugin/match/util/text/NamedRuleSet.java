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
 * Set of named rules registered in the RuleRegistry.
 *
 * @author Micha&euml;l Michaud
 * @version 0.1 (2011-05-01)
 */
// History
// 0.1 (2011-05-01)
public class NamedRuleSet extends RuleSet {

    public NamedRuleSet(String...namedRules) throws RuleNotFoundException {
        super(new Rule[namedRules.length]);
        for (int i = 0 ; i < namedRules.length ; i++) {
            rules[i] = RuleRegistry.getRule(namedRules[i]);
        }
    }

    public NamedRuleSet(String file) throws IOException,
                                            FileNotFoundException,
                                            RuleNotFoundException,
                                            PatternSyntaxException {
        InputStream is = null;
        try {
            is = NamedRuleSet.class.getClassLoader().getResourceAsStream("/fr/m3/util/text/" + file);
            if (is == null) {
                is = new FileInputStream(file);
            }
            //if (is == null) throw new FileNotFoundException ("The file "
            //    + file + " has not been found");
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
                        ruleList.add(RuleRegistry.getRule(line));
                    } catch(RuleNotFoundException rnfe) {
                        rnfe.printStackTrace();
                    }
                }
            }
            rules = ruleList.toArray(new Rule[ruleList.size()]);
            System.out.println("New NamedRuleSet " + file + " : " + rules.length + "rule(s)");
            is.close();
        } catch(IOException ioe) {
            try {is.close();} catch(IOException e) {}
            throw ioe;
        } finally {
            try {is.close();} catch(IOException e) {}
        }
    }

}