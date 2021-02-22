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

package org.openjump.core.ui.plugin.match;

import com.vividsolutions.jump.feature.Feature;

/**
 * A match between two features. A match is oriented from a source feature
 * to a target feature.
 * 
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class Match implements Comparable<Match> {
    
    private Feature source;
    private Feature target;
    private double score;

    /**
     * Create a Match object.
     * @param source the source Feature to match from
     * @param target the target Feature to match to
     * @param score the score of the match
     */
    public Match(Feature source, Feature target, double score) {
        this.source = source;
        this.target = target;
        this.score = score;
    }
    
    public Feature getSource() {
        return source;
    }
    
    public Feature getTarget() {
        return target;
    }
    
    public double getScore() {
        return score;
    }
    
    /**
     * Combine score with another score so that 
     * - one of to scores is 0 -> final score is 0
     * - both score are 1 -> final score is 1
     */
    public Match combineScore(double otherScore) {
        score *= otherScore;
        return this;
    }
    
    /**
     * Compare two matches by comparing their matching score first, then, in
     * case of matching score equality, their source feature ID, and in case
     * of source feature ID equality, their target ID.
     */
     public int compareTo(Match m) {
        if (getScore() > m.getScore()) return -1;
        else if (getScore() < m.getScore()) return 1;
        else {
            if (getSource().getID() < m.getSource().getID()) return -1;
            else if (getSource().getID() > m.getSource().getID()) return 1;
            else {
                if (getTarget().getID() < m.getTarget().getID()) return -1;
                else if (getTarget().getID() > m.getTarget().getID()) return 1;
                else return 0;
            }
        }
    } 
    
    /**
     * Two matches are equal iff their source feature ID, their target feature 
     * ID and their matching score are equal.
     */
    public boolean equals(Object o) {
        if (o instanceof Match) {
            Match other = (Match)o;
            return source.getID() == other.getSource().getID() && 
                   target.getID() == other.getTarget().getID() && 
                   getScore() == other.getScore();
        }
        return false;
    }
    
    public String toString() {
        return "Match " + source.getID() + " and " + target.getID() + " with score " + score; 
    }

}
