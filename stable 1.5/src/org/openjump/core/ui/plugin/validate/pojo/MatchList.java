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
 * This class stores the matches to be validated.
 * In validation process, the context similarity, object similarity and status of match will also be recorded in this class
 * @author Guangdi Hu
 *
 */
public class MatchList {
	
	private double CONTEXT_SIMILARITY_WEIGHT = 0.8;
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
	private ArrayList<Double> bufferRadiusList = null;

	private ArrayList<Integer> validationStatuses = null;
	private final int UNDISCOVERED = 0;
	private final int INQUEUE      = 1;
	private final int INVALID      = 2;
	private final int VALID        = 3;
	private final int NEW          = 4;
	
	/**
	 * The following fields are used for detecting missing matches
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
		unmatchedSourceFeatures = new ArrayList<Feature>();
		unmatchedTargetFeatures = new ArrayList<Feature>();
	}
	
	public ArrayList<Feature> getSourceList() {
		return sourceFeatureList;
	}
	
	public void clear() {
		for (int i = 0; i < sourceFeatureList.size(); i++) {
			contextSimilarties.set(i, 1.0);
			objectSimilarities.set(i, 0.0);
			validationStatuses.set(i, UNDISCOVERED);
		}
	}
	
	
	/**
	 * Store a new match into the list.
	 * @param sourceFeature The source layer object involved in the new match
	 * @param targetFeature The target layer object involved in the new match
	 */
	public void storeMatch(Feature sourceFeature, Feature targetFeature) {
		int sourceIndex = sourceFeatureList.indexOf(sourceFeature);
		int targetIndex = targetFeatureList.indexOf(targetFeature);
		if (sourceIndex < 0  && targetIndex < 0) {
			// none of the pair has been stored
			storeNonDuplicatedMatch(sourceFeature, targetFeature);
		} else if (sourceIndex >= 0) {
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
			}
		} else if (targetIndex >= 0) {
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
			}
		} else {
			System.out.println("//////////////////////////");
			System.out.println("// None of the cases is evoked in storeMatch(): " + sourceFeature.getID() + "--" + targetFeature.getID());
			System.out.println("//////////////////////////");
		}
	}
	
	/**
	 * Check whether a match can act as the supporting match for others
	 * @param f the match to be checked
	 * @return true if this match is not invalid; false otherwise
	 */
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
	
	/**
	 * Find and return an unprocessed match. This method is called to detect the spatially separated objects 
	 * @param queue The queue in forward traversal, the unprocessed match will be pushed onto this queue
	 * @return false if all matches have been processed; true otherwise
	 */
	public boolean supplementSingleMatch(Queue<Feature> queue) {
		for (int i = 0 ; i < validationStatuses.size(); i++) {
			if (shouldBeQueued(sourceFeatureList.get(i))) {
				queue.offer(sourceFeatureList.get(i));
				validationStatuses.set(i, INQUEUE);
				System.out.println("Supplement Match: " + sourceFeatureList.get(i).getID());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Auxiliary method for add a match into record, and initialize its statuses
	 */
	private void storeNonDuplicatedMatch(Feature sourceFeature, Feature targetFeature) {
		sourceFeatureIDs.add(sourceFeature.getID());
		targetFeatureIDs.add(targetFeature.getID());
		sourceFeatureList.add(sourceFeature);
		targetFeatureList.add(targetFeature);
		validationStatuses.add(UNDISCOVERED);
		contextSimilarties.add(1.0);
		objectSimilarities.add(0.0); // object similarity will be considered only if context similarity lower than threshold, so it is initiated to 0
		bufferRadiusList.add(0.0);
	}
	
	/**
	 * Check whether a source layer object in alone, record in in relative list if it is alone
	 * @param sourceFeature the being checked source layer object
	 */
	public void tryAddUnmatchedSourceFeature(Feature sourceFeature) {
		if (!sourceFeatureList.contains(sourceFeature) && !unmatchedSourceFeatures.contains(sourceFeature)) {
			unmatchedSourceFeatures.add(sourceFeature);
		}
	}
	
	/**
	 * Check whether a target layer object in alone, record in in relative list if it is alone
	 * @param sourceFeature the being checked target layer object
	 */
	public void tryAddUnmatchedTargetFeature(Feature targetFeature) {
		if (!targetFeatureList.contains(targetFeature) && !unmatchedTargetFeatures.contains(targetFeature)) {
			unmatchedTargetFeatures.add(targetFeature);
		}
	}
	
	/**
	 * Find a source layer object by its id
	 * @param id ID of the object
	 * @return The source layer object specified by the input id
	 */
	public Feature getSourceFeatureByID(int id) {
		int index = sourceFeatureIDs.indexOf(id);
		if (index == -1) {
			return null;
		}
		return sourceFeatureList.get(index);
	}
	
	/**
	 * Find a target layer object by its id
	 * @param id ID of the object
	 * @return The target layer object specified by the input id
	 */
	public Feature getTargetFeatureByID(int id) {
		int index = targetFeatureIDs.indexOf(id);
		if (index == -1) {
			return null;
		}
		return targetFeatureList.get(index);
	}
	
	/**
	 * Find a source layer object by its index in record
	 * @param i index of the object
	 * @return The source layer object specified by its index
	 */
	public Feature getSourceFeatureByIndex(int i) {
		return sourceFeatureList.get(i);
	}
	
	/**
	 * Find a target layer object by its index in record
	 * @param i index of the object
	 * @return The target layer object specified by its index
	 */
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
	
	/**
	 * Return the number of recorded matches
	 */
	public int numberOfFeatures() {
		if (sourceFeatureList.size() == targetFeatureList.size() && sourceFeatureList.size() == validationStatuses.size()) {
			return sourceFeatureList.size();
		} else {
			return -1;
		}
	}
	
	/**
	 * Check whether a match should be pushed onto the queue of forward traversal.
	 * @param sourceFeature the source layer object involved in the being checked match.
	 * @return true if it has not been discovered
	 */
	public boolean shouldBeQueued(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i == -1) {
			return false;
		}
		if (validationStatuses.get(i) == UNDISCOVERED) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Change the status of a match as InQueue
	 * @param sourceFeature the target match
	 */
	public void setAsInQueue(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, INQUEUE);
		}
	}
	
	/**
	 * Change the status of a match as Invalid
	 * @param sourceFeature the target match
	 */
	public void setAsInvalid(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, INVALID);
		}
	}
	
	/**
	 * Change the status of a match as Valid
	 * @param sourceFeature the target match
	 */
	public void setAsValid(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, VALID);
		}
	}
	
	/**
	 * Change the status of a match as New, this marks the match as a detected missing match
	 * @param sourceFeature the target match
	 */
	public void setAsNew(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			validationStatuses.set(i, NEW);
		}
	}
	
	/**
	 * Check whether a match is marked as Invalid; before searching in record, the existence of this match should be checked
	 * @param sourceFeature the source layer object involved in the being checked match.
	 * @return true if the match is invalid
	 */
	public boolean isInvalid(Feature sourceFeature) {
		int i = sourceFeatureList.indexOf(sourceFeature);
		if (i >= 0) {
			return validationStatuses.get(i) == INVALID || validationStatuses.get(i) == NEW; // ignore newly detected omitted matches
		} else {
			return true;
		}
	}
	
	
	/**
	 * Record the context similarity value of a match
	 * @param srcFeature the source layer object involved in the being checked match
	 * @param similarity context similarity
	 */
	public void setContextSimilarity(Feature srcFeature, Double similarity) {
		int i = sourceFeatureList.indexOf(srcFeature);
		if (i == -1) {
			System.out.println("--setContextSimilarity-- feature not found id = " + srcFeature.getID());
			return;
		}
		contextSimilarties.set(i, similarity);
	}
	
	/**
	 * Record the object similarity value of a match
	 * @param srcFeature the source layer object involved in the being checked match
	 * @param similarity object similarity
	 */
	public void setObjectSimilarity(Feature srcFeature, Double similarity) {
		int i = sourceFeatureList.indexOf(srcFeature);
		if (i == -1) {
			System.out.println("--setObjectSimilarity-- feature not found id = " + srcFeature.getID());
			return;
		}
		objectSimilarities.set(i, similarity);
	}
	
	/**
	 * Return the context similarity of a match
	 * @param f the source layer object involved in the being checked match
	 * @return context similarity of this match
	 */
	public double getContextSimilarity(Feature f) {
		int i = sourceFeatureList.indexOf(f);
		if (i >= 0) {
			return contextSimilarties.get(i);
		}
		System.out.println("--getContextSimilarity-- Feature Not Found id = " + f.getID());
		return 0;
	}
	
	/**
	 * Return the object similarity of a match
	 * @param f the source layer object involved in the being checked match
	 * @return object similarity of this match
	 */
	public double getObjectSimilarity(Feature f) {
		int i = sourceFeatureList.indexOf(f);
		if (i >= 0) {
			return objectSimilarities.get(i);
		}
		System.out.println("--getObjectSimilarity-- Feature Not Found id = " + f.getID());
		return 0;
	}
	
	/**
	 * Calculate and return the confidence level of a match
	 * @param f the source layer object involved in the being checked match
	 * @return the confidence level of this match
	 */
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
	
	public void setContextWeight(Double w) {
		this.CONTEXT_SIMILARITY_WEIGHT = w;
	}
	
	public double getValidThreshold() {
		return VALID_THRESHOLD;
	}
	
	public void setValidThreshold(Double t) {
		this.VALID_THRESHOLD = t;
	}
	
	/**
	 * Get the validation result on the input matches, i.e. the valid match set and invalid match set
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
				validColl.add(sourceFeatureList.get(i).clone(true));
			} else if (validationStatuses.get(i) == INVALID) {
				invalidColl.add(sourceFeatureList.get(i).clone(true));
			} else {
				if (validationStatuses.get(i) != NEW)
					System.out.println("Neither Valid nor Invalid: " + validationStatuses.get(i));
			}
		}
		return new Pair<FeatureCollection, FeatureCollection>(validColl, invalidColl);
	}
	
	/**
	 * Get the result of detecting missing matches
	 * @return two sets of source layer & target layer objects involved in missing matches
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
	
	/**
	 * Record the buffer radius applied on a match, the buffer radius should larger than 0
	 * @param feature the source layer object involved in the being checked match
	 * @param r buffer radius
	 */
	public void setBufferRadius(Feature feature, double r) {
		int i = sourceFeatureList.indexOf(feature);
		if (i >= 0) {
			bufferRadiusList.set(i, r);
		}
	}
	
	/**
	 * Get the buffer radius of a match, this may be used to visualize the process of search for supporting matches
	 * @param feature the source layer object involved in the being checked match
	 * @return the buffer radius of the match; -1 if the match not exists
	 */
	public double getBufferRadius(Feature feature) {
		int i = sourceFeatureList.indexOf(feature);
		if (i == -1) {
			return 0;		
		}
		return bufferRadiusList.get(i);
	}

	/**
	 * Get the list of alone objects in source layer
	 * @return the list of alone objects in source layer
	 */
	public ArrayList<Feature> getUnmatchedSourceFeatures() {
		return this.unmatchedSourceFeatures;
	}
	
	/**
	 * Get the list of alone objects in target layer
	 * @return the list of alone objects in target layer
	 */
	public ArrayList<Feature> getUnmatchedTargetFeatures() {
		return this.unmatchedTargetFeatures;
	}
}
