/*
 * (C) 2011 Josh Clemm (GNU Lesser GPL)
 */

package org.openjump.core.ui.plugin.match.util.text.algo;

import java.util.HashMap;

/**
 * This class in an implementation of a Burkhard-Keller tree in Java.  
 * The BK-Tree is a tree structure to quickly finding close matches to
 * any defined object.
 * 
 * The BK-Tree was first described in the paper:
 * "Some Approaches to Best-Match File Searching" by W. A. Burkhard and R. M. Keller
 * It is available in the ACM archives.
 * 
 * Another good explanation can be found here:
 * http://blog.notdot.net/2007/4/Damn-Cool-Algorithms-Part-1-BK-Trees
 * 
 * Searching the tree yields O(logn), which is a huge upgrade over brute force
 * 
 * @author Josh Clemm
 *
 */
public class BKTree {

    private Node root;
    private HashMap<String, Integer> matches;
    private EditDistance distance;
    private String bestTerm;
    
    public BKTree(EditDistance distance) {
        root = null;
        this.distance = distance;
    }
    
    public void add(String term) {
        if(root != null) {
            root.add(term);
        }
        else {
            root = new Node(term);
        }
    }
    
    /**
     * This method will find all the close matching Objects within
     * a certain threshold.  For instance, for search for similar
     * strings, threshold set to 1 will return all the strings that
     * are off by 1 edit distance.
     * @param searchObject
     * @param threshold
     * @return a map of strings closer than the threshold and their distance to
     * the searchObject
     */
    public HashMap<String, Integer> query(String searchObject, int threshold) {
        matches = new HashMap<String,Integer>();
        root.query(searchObject, threshold, matches);
        return matches;
    }
    
    /**
     * Attempts to find the closest match to the search term.
     * @param term 
     * @return the edit distance of the best match
     */
    public int find(String term) {
        return root.findBestMatch(term, Integer.MAX_VALUE);
    }
    
    /**
     * Attempts to find the closest match to the search term.
     * @param term
     * @return a match that is within the best edit distance of the search term.
     */
    public String findBestWordMatch(String term) {
        root.findBestMatch(term, Integer.MAX_VALUE);
        return root.getBestTerm();
    }
    
    /**
     * Attempts to find the closest match to the search term.
     * @param term
     * @return a match that is within the best edit distance of the search term.
     */
    public HashMap<String,Integer> findBestWordMatchWithDistance(String term) {
        int distance = root.findBestMatch(term, Integer.MAX_VALUE);
        HashMap<String, Integer> returnMap = new HashMap<String, Integer>();
        returnMap.put(root.getBestTerm(), distance);
        return returnMap;
    }
    
    private class Node {
    
        String term;
        HashMap<Integer, Node> children;
        
        public Node(String term) {
            this.term = term;
            children = new HashMap<Integer, Node>();
        }
        
        public void add(String term) {
            int score = distance.editDistance(term, this.term);
            
            Node child = children.get(score);
            if(child != null) {
                    child.add(term);
            }
            else {
                    children.put(score, new Node(term));
            }
        }
        
        public int findBestMatch(String term, int bestDistance) {
            int distanceAtNode = distance.editDistance(term, this.term);

//          System.out.println("term = " + term + ", this.term = " + this.term + ", distance = " + distanceAtNode);
            
//          if(distanceAtNode == 1) {
//                  return distanceAtNode;
//          }
            
            if(distanceAtNode < bestDistance) {
                bestDistance = distanceAtNode;
                bestTerm = this.term;
            }
            
            int possibleBest = bestDistance;
            
            for (Integer score : children.keySet()) {
                if(score < distanceAtNode + bestDistance ) {
                    possibleBest = children.get(score).findBestMatch(term, bestDistance);
                    if(possibleBest < bestDistance) {
                            bestDistance = possibleBest;
                    }
                }
            }
            return bestDistance;
        }
        
        public String getBestTerm() {
            return bestTerm;
        }
        
        public void query(String term, int threshold, HashMap<String, Integer> collected) {
            int distanceAtNode = distance.editDistance(term, this.term);
            
            if(distanceAtNode == threshold) {
                collected.put(this.term, distanceAtNode);
                return;
            }
            
            if(distanceAtNode < threshold) {
                collected.put(this.term, distanceAtNode);
            }
            
            for (int score = distanceAtNode-threshold; score <= threshold+distanceAtNode; score++) {
                Node child = children.get(score);
                if(child != null) {
                    child.query(term, threshold, collected);
                }
            }
        }
    }
}
