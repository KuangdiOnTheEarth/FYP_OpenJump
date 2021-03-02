package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import com.vividsolutions.jump.feature.Feature;


/**
 * Used to record which matches are used to validate the being supported match
 * @author Kuangdi
 *
 */
public class SupportingRelations {
	
	private ArrayList<Feature> beSupportedFeatures = null;
	private ArrayList<ArrayList<Feature>> supportingFeatures = null;
	
	
	public SupportingRelations() {
		beSupportedFeatures = new ArrayList<Feature>();
		supportingFeatures = new ArrayList<ArrayList<Feature>>();
	}
	
	
	public void addSupportingRelation(Feature f, ArrayList<Feature> ss) {
		beSupportedFeatures.add(f);
		supportingFeatures.add(ss);
	}
	
	
	public ArrayList<Feature> getSupportingFeaturesOf(Feature f) {
		int index = beSupportedFeatures.indexOf(f);
		if (index == -1) {
			return null;
		} else {
			return supportingFeatures.get(index);
		}
	}
}
