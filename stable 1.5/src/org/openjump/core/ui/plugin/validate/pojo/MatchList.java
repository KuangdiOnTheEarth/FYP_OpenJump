package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import javafx.util.Pair;

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
//	private HashMap<Integer, Integer> hMap = null;
	
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
//		hMap = new HashMap<Integer, Integer>();
	}
	
	public void clear() {
		for (int i = 0; i < sourceFeatureList.size(); i++) {
			contextSimilarties.set(i, 1.0);
			objectSimilarities.set(i, 1.0);
			validationStatuses.set(i, UNDISCOVERED);
		}
//		hMap.clear();
	}
	
	public void storeMatch(Feature sourceFeature, Feature targetFeature) {
		sourceFeatureList.add(sourceFeature);
		targetFeatureList.add(targetFeature);
		validationStatuses.add(UNDISCOVERED);
		contextSimilarties.add(1.0);
		objectSimilarities.add(1.0);
//		hMap.put(sourceFeature.getID(), UNDISCOVERED);
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
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i == -1) {
			return null;
		} else {
			return targetFeatureList.get(i);
		}
	}
	/**
	 * Return the Feature object of corresponding geographic object in source layer (according to the stored matches)
	 * @param targetFeature an object in target layer 
	 * @return the corresponding object in source layer 
	 */
	public Feature getMatchedSourceFeature(Feature targetFeature) {
		int i = targetFeatureList.indexOf(targetFeature);
		if ( i == -1 ) {
			return null;
		} else {
			return sourceFeatureList.get(i);
		}
	}
	
	
	public int numberOfFeatures() {
		if (sourceFeatureList.size() == targetFeatureList.size() && sourceFeatureList.size() == validationStatuses.size()) {
			return sourceFeatureList.size();
		} else {
			return -1;
		}
	}
	
	public boolean shouldBeQueued(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i == -1) {
//			System.out.println("This feature in not found in match records");
			return false;
		}
//		if (!hMap.containsKey(sourceFeature.getID()) && hMap.get(sourceFeature.getID()) == UNDISCOVERED) {
		if (validationStatuses.get(i) == UNDISCOVERED) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setAsInQueue(Feature sourceFeature) {
//		hMap.put(sourceFeature.getID(), INQUEUE);
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, INQUEUE);
		}
	}
	
	public void setAsInvalid(Feature sourceFeature) {
//		hMap.put(sourceFeature.getID(), INVALID);
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, INVALID);
		}
	}
	
	public void setAsValid(Feature sourceFeature) {
//		hMap.put(sourceFeature.getID(), VALID);
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, VALID);
		}
	}
	
	
	public void setContextSimilarity(Feature srcFeature, Double similarity) {
		contextSimilarties.set(sourceFeatureList.indexOf(srcFeature), similarity);
	}
	public void setObjectSimilarity(Feature srcFeature, Double similarity) {
		objectSimilarities.set(sourceFeatureList.indexOf(srcFeature), similarity);
	}
	
	
	public Pair<FeatureCollection, FeatureCollection> getValidationResult() {
		FeatureCollection validColl = null;
		FeatureCollection invalidColl = null;
		FeatureSchema fs = sourceFeatureList.get(0).getSchema();
		validColl = new FeatureDataset(fs);
		invalidColl = new FeatureDataset(fs);
		for (int i = 0; i < sourceFeatureList.size(); i++) {
			if (validationStatuses.get(i) == VALID) {
				validColl.add(sourceFeatureList.get(i).clone(false));
			} else if (validationStatuses.get(i) == INVALID) {
				invalidColl.add(sourceFeatureList.get(i).clone(false));
			}
		}
		return new Pair<FeatureCollection, FeatureCollection>(validColl, invalidColl);
	}
}
