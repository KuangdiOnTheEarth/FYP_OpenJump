package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import com.vividsolutions.jump.feature.Feature;

/**
 * This class is used to store the matches to be validated
 * @author Kuangdi
 *
 */
public class MatchList {
	
	private ArrayList<Feature> sourceFeatureList = null; 
	private ArrayList<Feature> targetFeatureList = null; 
	private ArrayList<Double> contextSimilarties = null;
	private ArrayList<Double> objectSimilarities = null;
	
	private ArrayList<Integer> validationStatuses = null;
	private final int UNDISCOVERED = 0;
	private final int INQUEUE      = 1;
	private final int INVALID      = 2;
	private final int VALID        = 3;
	
	
	public MatchList() {
		sourceFeatureList = new ArrayList<Feature>();
		targetFeatureList = new ArrayList<Feature>();
		validationStatuses = new ArrayList<Integer>();
		contextSimilarties = new ArrayList<Double>();
		objectSimilarities = new ArrayList<Double>();
	}
	
	public void storeMatch(Feature sourceFeature, Feature targetFeature) {
		sourceFeatureList.add(sourceFeature);
		targetFeatureList.add(targetFeature);
		validationStatuses.add(UNDISCOVERED);
		contextSimilarties.add(1.0);
		objectSimilarities.add(1.0);
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
	
	
	public int numberOfFeatures() {
		if (sourceFeatureList.size() == targetFeatureList.size() && sourceFeatureList.size() == validationStatuses.size()) {
			return sourceFeatureList.size();
		} else {
			return -1;
		}
	}
	
	
	public void setAsInQueue(int index) {
		validationStatuses.set(index, INQUEUE);
	}
	
	public void setAsInvalid(int index) {
		validationStatuses.set(index, INVALID);
	}
	
	public void setAsValid(int index) {
		validationStatuses.set(index, VALID);
	}
	
	
	public void setContextSimilarity(Feature srcFeature, Double similarity) {
		contextSimilarties.set(sourceFeatureList.indexOf(srcFeature), similarity);
	}
	public void setObjectSimilarity(Feature srcFeature, Double similarity) {
		objectSimilarities.set(sourceFeatureList.indexOf(srcFeature), similarity);
	}
}
