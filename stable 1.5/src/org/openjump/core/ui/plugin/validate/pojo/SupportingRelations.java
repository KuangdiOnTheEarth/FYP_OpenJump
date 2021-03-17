package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;

import com.vividsolutions.jump.feature.Feature;


/**
 * Used to record which matches are used to validate the being supported match
 * @author Kuangdi
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
	
	public void addMatchSpace() {
		beSupportedBy.add(new ArrayList<Feature>());
		supports.add(new ArrayList<Feature>());
	}
	
	/**
	 * 
	 * @param beSupportedFeature
	 * @param ss
	 * @param omittedMatch true if the current match is omitted match, so it can only be supported by others, but not support other matches
	 */
	public void addSupportingRelation(Feature beSupportedFeature, ArrayList<Feature> ss, boolean omittedMatch) {
		if (ss.isEmpty()) {
//			System.out.println("No supporting relation need to be recorded for id = " + beSupportedFeature.getID());
			return;
		}
		if (!omittedMatch) {
			for (Feature f : ss) {
				int index = features.indexOf(f);
				if (index == -1) {
					System.out.println("--SupportingRelations-- not found record of id = " + f.getID());
				} else {
					supports.get(index).add(beSupportedFeature);
				}
			}
		}
		
		int index = features.indexOf(beSupportedFeature);
		if (index == -1) {
			System.out.println("--SupportingRelations-- the center feature is not found in record id = " + beSupportedFeature.getID());
		}
		beSupportedBy.set(index, ss);
	}
	
	public ArrayList<Feature> getFeaturesSupportedBy(Feature f) {
		int index = features.indexOf(f);
		if (index == -1) {
			return null;
		} else {
			return supports.get(index);
		}
	}
	
	public ArrayList<Feature> getSupportingFeaturesOf(Feature f) {
		int index = features.indexOf(f);
		if (index == -1) {
			return null;
		} else {
			return beSupportedBy.get(index);
		}
	}
}
