package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;

import com.vividsolutions.jump.feature.Feature;


/**
 * Used to record the dependence between each candidate match and its supporting matches.
 * In backtracking, the algorithm look up the records in this class to identify the matches which are influenced by the invalid match.
 * @author Guangdi Hu
 *
 */
public class SupportingRelations {
	SharedSpace sharedSpace = SharedSpace.getInstance();
	private ArrayList<Feature> features = null;
	private ArrayList<ArrayList<Feature>> beSupportedBy = null;
	private ArrayList<ArrayList<Feature>> supports = null;
	
	public SupportingRelations() {
		beSupportedBy = new ArrayList<ArrayList<Feature>>();
		supports = new ArrayList<ArrayList<Feature>>();
		features = sharedSpace.getMatchList().getSourceList();
		for (int i = 0; i < features.size(); i++) {
			beSupportedBy.add(new ArrayList<Feature>());
			supports.add(new ArrayList<Feature>());
		}
	}
	
	/**
	 * Assign a new space to record the dependence for a detected missing match.
	 */
	public void addMatchSpace() {
		beSupportedBy.add(new ArrayList<Feature>());
		supports.add(new ArrayList<Feature>());
	}
	
	/**
	 * Record the dependence between one candidate match and its supporting matches.
	 * The dependence on each match composes of two parts: 
	 * which matches are the supporting match of this match, and this match acts as the supporting match of which matches 
	 * @param beSupportedFeature
	 * @param ss
	 * @param omittedMatch true if the current match is omitted match, so it can only be supported by others, but not support other matches
	 */
	public void addSupportingRelation(Feature beSupportedFeature, ArrayList<Feature> ss, boolean omittedMatch) {
		if (ss.isEmpty()) {
			return;
		}
		
		// Clean the previous supporting relation (let others forget they have support this match)
		int i = features.indexOf(beSupportedFeature);
		if (i == -1) {
			System.out.println("--SupportingRelations-- the center feature is not found in record id = " + beSupportedFeature.getID());
		}
		ArrayList<Feature> nn = (ArrayList<Feature>)beSupportedBy.get(i).clone();
		
		
		// Set new supporting relation
		if (!omittedMatch) {
			for (Feature f : nn) {
				if (!ss.contains(f)) { // if the context has been changed, let the origin context forget they have supported this match
					int index = features.indexOf(f);
					if (index == -1) {
						System.out.println("--SupportingRelations-- not found record of id = " + f.getID());
					} else {
						ArrayList<Feature> newSupports = new ArrayList<Feature>();
						for (Feature sup : supports.get(index)) {
							if ( sup != beSupportedFeature) {
								newSupports.add(sup);
							}
						}
						supports.set(index, newSupports);
					}
				}
			}
			for (Feature f : ss) {
				int index = features.indexOf(f);
				if (index == -1) {
					System.out.println("--SupportingRelations-- not found record of id = " + f.getID());
				} else {
					if (!supports.get(index).contains(beSupportedFeature)) {
						supports.get(index).add(beSupportedFeature);
					}
				}
			}
		}
		

		beSupportedBy.set(i, ss);
	}
	
	/**
	 * Identify all the matches whose supporting matches contains the input match
	 * @param f The source layer object involved in a match
	 * @return A list of matches influenced by the input match
	 */
	public ArrayList<Feature> getFeaturesSupportedBy(Feature f) {
		int index = features.indexOf(f);
		if (index == -1) {
			return null;
		} else {
			return supports.get(index);
		}
	}
	
	/**
	 * Find all the supporting matches of the input match
	 * @param f The source layer object involved in a match
	 * @return The list of supporting matches of the input match
	 */
	public ArrayList<Feature> getSupportingFeaturesOf(Feature f) {
		int index = features.indexOf(f);
		if (index == -1) {
			return null;
		} else {
			return beSupportedBy.get(index);
		}
	}
}
