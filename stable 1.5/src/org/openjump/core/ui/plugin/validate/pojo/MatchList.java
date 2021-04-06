package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

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
	
	private final double CONTEXT_SIMILARITY_WEIGHT = 0;
	private double VALID_THRESHOLD = 0;
	
	private ArrayList<Integer> sourceFeatureIDs = null;
	private ArrayList<Integer> targetFeatureIDs = null;
	
	/**
	 * The following feature lists only contain the matched features, not all the features in the source and target layers
	 */
	private ArrayList<Feature> sourceFeatureList = null; 
	private ArrayList<Feature> targetFeatureList = null; 
	private ArrayList<Double> contextSimilarties = null;
	private ArrayList<Double> objectSimilarities = null;
//	private HashMap<Integer, Integer> hMap = null;
	private ArrayList<Double> bufferRadiusList = null;

	private ArrayList<Integer> validationStatuses = null;
	private final int UNDISCOVERED = 0;
	private final int INQUEUE      = 1;
	private final int INVALID      = 2;
	private final int VALID        = 3;
	private final int NEW          = 4;
	
	/**
	 * The following fields are used for detecting omitted matches
	 */
	private ArrayList<Feature> unmatchedSourceFeatures = null; 
	private ArrayList<Feature> unmatchedTargetFeatures = null; 
	
	
	public MatchList() {
		sourceFeatureIDs = new ArrayList<Integer>();
		targetFeatureIDs = new ArrayList<Integer>();
		sourceFeatureList = new ArrayList<Feature>();
		targetFeatureList = new ArrayList<Feature>();
		validationStatuses = new ArrayList<Integer>();
		contextSimilarties = new ArrayList<Double>();
		objectSimilarities = new ArrayList<Double>();
		bufferRadiusList = new ArrayList<Double>();
//		hMap = new HashMap<Integer, Integer>();
		unmatchedSourceFeatures = new ArrayList<Feature>();
		unmatchedTargetFeatures = new ArrayList<Feature>();
	}
	
	public ArrayList<Feature> getSourceList() {
		return sourceFeatureList;
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
		int sourceIndex = sourceFeatureList.indexOf(sourceFeature);
		int targetIndex = targetFeatureList.indexOf(targetFeature);
		if (sourceIndex < 0  && targetIndex < 0) {
			// none of the pair has been stored
			storeNonDuplicatedMatch(sourceFeature, targetFeature);
		} else if (sourceIndex > 0) {
			// multiple matches are assigned to this source feature, check: if this match has been stored, ignore it
			boolean duplicated = false;
			for (int i = sourceIndex; i < sourceFeatureList.size(); i++) {
				if (sourceFeatureList.get(i) == sourceFeature && targetFeatureList.get(i) == targetFeature) {
					duplicated = true;
					break;
				}
			}
			if (!duplicated) {
				storeNonDuplicatedMatch(sourceFeature, targetFeature);
//				System.out.println("\t ----------- source f multi-match");
			}
		} else if (targetIndex > 0) {
			// multiple matches are assigned to this target feature, check: if this match has been stored, ignore it
			boolean duplicated = false;
			for (int i = targetIndex; i < targetFeatureList.size(); i++) {
				if (sourceFeatureList.get(i) == sourceFeature && targetFeatureList.get(i) == targetFeature) {
					duplicated = true;
					break;
				}
			}
			if (!duplicated) {
				storeNonDuplicatedMatch(sourceFeature, targetFeature);
//				System.out.println("\t target f multi-match -----------");
			}
		} else {
			System.out.println("//////////////////////////");
			System.out.println("// None of the cases is evoked in storeMatch(): " + sourceFeature.getID() + "--" + targetFeature.getID());
			System.out.println("//////////////////////////");
		}
	}
	
	// return true if the input object is contained by a match, and that match is not invalid
	public boolean isSuitableSupportingMatch(Feature f) {
		int index = sourceFeatureList.indexOf(f);
		if( index == -1) {
			return false; // no match
		} else {
			if (validationStatuses.get(index) == INVALID) {
				return false;
			}
			return true;
		}
	}
	
	public boolean supplementSingleMatch(Queue<Feature> queue) {
		for (int i = 0 ; i < validationStatuses.size(); i++) {
//			if (validationStatuses.get(i) == UNDISCOVERED) {
			if (shouldBeQueued(sourceFeatureList.get(i))) {
				queue.offer(sourceFeatureList.get(i));
//				setAsInQueue(sourceFeatureList.get(i));
				validationStatuses.set(i, INQUEUE);
				System.out.println("Supplement Match: " + sourceFeatureList.get(i).getID());
				return true;
			}
		}
		return false;
	}
	
	private void storeNonDuplicatedMatch(Feature sourceFeature, Feature targetFeature) {
		sourceFeatureIDs.add(sourceFeature.getID());
		targetFeatureIDs.add(targetFeature.getID());
		sourceFeatureList.add(sourceFeature);
		targetFeatureList.add(targetFeature);
		validationStatuses.add(UNDISCOVERED);
		contextSimilarties.add(1.0);
		objectSimilarities.add(0.0); // object similarity will be considered only if context similarity lower than threshold, so it is initiated to 0
		bufferRadiusList.add(0.0);
//		hMap.put(sourceFeature.getID(), UNDISCOVERED);
	}
	
	public void tryAddUnmatchedSourceFeature(Feature sourceFeature) {
		if (!sourceFeatureList.contains(sourceFeature) && !unmatchedSourceFeatures.contains(sourceFeature)) {
			unmatchedSourceFeatures.add(sourceFeature);
		}
	}
	
	public void tryAddUnmatchedTargetFeature(Feature targetFeature) {
		if (!targetFeatureList.contains(targetFeature) && !unmatchedTargetFeatures.contains(targetFeature)) {
			unmatchedTargetFeatures.add(targetFeature);
		}
	}
	
	public Feature getSourceFeatureByID(int id) {
		int index = sourceFeatureIDs.indexOf(id);
		if (index == -1) {
			return null;
		}
		return sourceFeatureList.get(index);
	}
	
	public Feature getTargetFeatureByID(int id) {
		int index = targetFeatureIDs.indexOf(id);
		if (index == -1) {
			return null;
		}
		return targetFeatureList.get(index);
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
	
	public void setAsNew(Feature sourceFeature) {
//		hMap.put(sourceFeature.getID(), VALID);
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, NEW);
		}
	}
	
	public boolean isInvalid(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			return validationStatuses.get(i) == INVALID || validationStatuses.get(i) == NEW; // ignore newly detected omitted matches
		} else {
//			System.out.println("--isInvalid-- Not found id = " + sourceFeature.getID());
			return true;
		}
	}
	
	
	public void setContextSimilarity(Feature srcFeature, Double similarity) {
		int i = sourceFeatureList.indexOf(srcFeature);
		if (i == -1) {
			System.out.println("--setContextSimilarity-- feature not found id = " + srcFeature.getID());
			return;
		}
		contextSimilarties.set(i, similarity);
	}
	
	public void setObjectSimilarity(Feature srcFeature, Double similarity) {
		int i = sourceFeatureList.indexOf(srcFeature);
		if (i == -1) {
			System.out.println("--setObjectSimilarity-- feature not found id = " + srcFeature.getID());
			return;
		}
		objectSimilarities.set(i, similarity);
	}
	
	public double getContextSimilarity(Feature f) {
		int i = sourceFeatureList.indexOf(f);
		if (i >= 0) {
			return contextSimilarties.get(i);
		}
		System.out.println("--getContextSimilarity-- Feature Not Found id = " + f.getID());
		return 0;
	}
	
	public double getObjectSimilarity(Feature f) {
		int i = sourceFeatureList.indexOf(f);
		if (i >= 0) {
			return objectSimilarities.get(i);
		}
		System.out.println("--getObjectSimilarity-- Feature Not Found id = " + f.getID());
		return 0;
	}
	
	public double getConfidenceLevel(Feature f) {
		int i = sourceFeatureList.indexOf(f);
		if (i >= 0) {
			return contextSimilarties.get(i) * CONTEXT_SIMILARITY_WEIGHT + objectSimilarities.get(i) * (1 - CONTEXT_SIMILARITY_WEIGHT);
		}
		System.out.println("--getConfidenceLevel-- Feature Not Found id = " + f.getID());
		return 0;
	}
	
	public double getContextWeight() {
		return CONTEXT_SIMILARITY_WEIGHT;
	}
	
	public double getValidThreshold() {
		return VALID_THRESHOLD;
	}
	
	public void setValidThreshold(Double t) {
		this.VALID_THRESHOLD = t;
	}
	/**
	 * 
	 * @return a set of all VALID matches and a set of all INVALID matches
	 */
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
	
	/**
	 * 
	 * @return the newly discovered matches for single objects
	 */
	public Pair<FeatureCollection, FeatureCollection> getNewMatches() {
		FeatureCollection sourceColl = null;
		FeatureCollection targetColl = null;
		FeatureSchema fs = sourceFeatureList.get(0).getSchema();
		sourceColl = new FeatureDataset(fs);
		targetColl = new FeatureDataset(fs);
		for (int i = 0; i < sourceFeatureList.size(); i++) {
			if (validationStatuses.get(i) == NEW) {
				sourceColl.add(sourceFeatureList.get(i).clone(false));
				targetColl.add(targetFeatureList.get(i).clone(false));
			}
		}
		return new Pair<FeatureCollection, FeatureCollection>(sourceColl, targetColl);
	}
	
	public void setBufferRadius(Feature feature, double r) {
		int i = sourceFeatureList.indexOf(feature);
		if (i >= 0) {
			bufferRadiusList.set(i, r);
		}
	}
	
	public double getBufferRadius(Feature feature) {
		int i = sourceFeatureList.indexOf(feature);
		if (i == -1) {
			return 0;		
		}
		return bufferRadiusList.get(i);
	}

	public ArrayList<Feature> getUnmatchedSourceFeatures() {
		return this.unmatchedSourceFeatures;
	}
	
	public ArrayList<Feature> getUnmatchedTargetFeatures() {
		return this.unmatchedTargetFeatures;
	}
}
