package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import com.vividsolutions.jump.feature.Feature;

/**
 * This class is used to store the matches to be validated
 * @author Kuangdi
 *
 */
public class MatchList {
	
	private ArrayList<Feature> sourceFeatureList; 
	private ArrayList<Feature> targetFeatureList; 
	
	public MatchList() {
		sourceFeatureList = new ArrayList<Feature>();
		targetFeatureList = new ArrayList<Feature>();
	}
	
	public void storeMatch(Feature sourceFeature, Feature targetFeature) {
		sourceFeatureList.add(sourceFeature);
		targetFeatureList.add(targetFeature);
	}
	
	public Feature getSourceFeatureByIndex(int i) {
		return sourceFeatureList.get(i);
	}
	
	public Feature getTargetFeatureByIndex(int i) {
		return targetFeatureList.get(i);
	}
	
	/**
	 * Return the Feature object of corresponding geographic object in target layer (according to the stored matches)
	 * @param sourceFeature an object in source layer 
	 * @return the corresponding object in target layer 
	 */
	public Feature getMatchedTargetFeature(Feature sourceFeature) {
		return targetFeatureList.get(sourceFeatureList.indexOf(sourceFeature));
	}

	/**
	 * Return the Feature object of corresponding geographic object in source layer (according to the stored matches)
	 * @param targetFeature an object in target layer 
	 * @return the corresponding object in source layer 
	 */
	public Feature getMatchedSourceFeature(Feature targetFeature) {
		return sourceFeatureList.get(targetFeatureList.indexOf(targetFeature));
	}
	
}
