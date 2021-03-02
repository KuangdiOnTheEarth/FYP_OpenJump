package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;

import com.vividsolutions.jump.feature.Feature;

/**
 * Automatically order the input relative positions for surrounding objects anti-clockwise
 * this is used to support the context similarity calculation
 * @author Kuangdi
 *
 */
public class AntiClockwiseSequence {
	
	private ArrayList<RelativePosition> sequence = null;
	private SharedSpace sharedSpace = null;
	
	
	public AntiClockwiseSequence() {
		sequence = new ArrayList<RelativePosition>();
		sharedSpace = SharedSpace.getInstance();
	}
	
	
	public void add(RelativePosition p) {
		if (sequence.size() == 0) {
			sequence.add(p);
//			System.out.println("Added " + p.feature().getID());
		} else {
			for (int index = 0; index < sequence.size(); index++) {
				if (!p.isAntiClockwiseThan(sequence.get(index))) {
					sequence.add(index, p);
//					System.out.println("Added " + p.feature().getID());
					return;
				}
			}
			sequence.add(p);
//			System.out.println("Added " + p.feature().getID());
		}
	}
	
	public int size() {
		return sequence.size();
	}
	
	public ArrayList<Feature> getFeatureList() {
		ArrayList<Feature> list = new ArrayList<Feature>();
		for (RelativePosition rp : sequence) {
			list.add(rp.feature());
		}
		return list;
	}
	
	
	public Double calContextSimilarityWith(AntiClockwiseSequence target) {
		double smallerLength = Math.min(this.size(), target.size());
		
		ArrayList<Feature> tarFeatures = target.getFeatureList();
		
		// the matched indices of corresponding objects in target sequence for the non-single objects in source sequence
		ArrayList<Integer> corrIndices = new ArrayList<Integer>(); 
		
		MatchList matchList = sharedSpace.getMatchList();
		// remove the single objects from source feature sequence
		for (Feature sf : this.getFeatureList()) {
			for (int i = 0; i < tarFeatures.size() ; i++) {
				if (matchList.getMatchedTargetFeature(sf) == tarFeatures.get(i)) {
					corrIndices.add(i);
					break;
				}
			}
		}
		if (corrIndices.size() == 0) {
			return 0.0;
		}
		
		// find the max in-order numbers in corrIndices
		double maxInOrder = 1;
		int startIndex = 0;
		while (startIndex + maxInOrder < corrIndices.size()) {
			int nextStartIndex = -1;
			int count = 1;
			int temp = corrIndices.get(startIndex);
			for (int i = 1; i + startIndex < corrIndices.size(); i++) {
				if (corrIndices.get(startIndex + i) > temp) {
					temp = corrIndices.get(startIndex + i);
					count++;
				} else {
					if (nextStartIndex == -1) {
						nextStartIndex = startIndex + i;
					}
				}
			}
			maxInOrder = Math.max(maxInOrder, count);
			if (startIndex < nextStartIndex) {
				startIndex = nextStartIndex;
			} else { // all possibilities have been checked
				break;
			}
		}
		
//		System.out.print(String.format(("(%d/%d, %.3f): "), (int)maxInOrder, (int)smallerLength, (maxInOrder/smallerLength)));
//		for (Feature f : target.getFeatureList()) {
//			System.out.print(f.getID() + " ");
//		}
//		System.out.println();
		
		return maxInOrder / smallerLength;
	}
	
	public double recalContextSimilarityWith(AntiClockwiseSequence tarSequence, Feature newInvalidFeature) {
		double smallerLength = Math.min(this.size(), tarSequence.size());
		
		ArrayList<Feature> tarFeatures = tarSequence.getFeatureList();
		// if a feature in source sequence (this) has matched feature in target layer sequence, the index of the target sequence will be recorded
		ArrayList<Integer> corrTarIndices = new ArrayList<Integer>(); 
		ArrayList<Feature> matchedSourFeatures = new ArrayList<Feature>();
		
		MatchList matchList = sharedSpace.getMatchList();
		// remove the single objects from source feature sequence
		for (Feature sf : this.getFeatureList()) {
			for (int i = 0; i < tarFeatures.size() ; i++) {
				if (matchList.getMatchedTargetFeature(sf) == tarFeatures.get(i)) {
					corrTarIndices.add(i);
					matchedSourFeatures.add(sf);
					break;
				}
			}
		}
		if (corrTarIndices.size() == 0) {
			return 0.0;
		}
		
		// find the max in-order numbers in corrIndices, record them in ArrayList
		ArrayList<Feature> maxInorderSurr = null;
		// the corresponding not in-ordered surrounding matches 
		ArrayList<Feature> notInorderSurr = null;
		
		double maxInOrder = 1;
		int startIndex = 0;
		while (startIndex + maxInOrder < corrTarIndices.size()) {
			ArrayList<Feature> tempInorderSurr = new ArrayList<Feature>();
			ArrayList<Feature> tempNotInorderSurr = new ArrayList<Feature>();
			int nextStartIndex = -1;
			int count = 1;
			int temp = corrTarIndices.get(startIndex);
			for (int i = 1; i + startIndex < corrTarIndices.size(); i++) {
				if (corrTarIndices.get(startIndex + i) > temp) {
					temp = corrTarIndices.get(startIndex + i);
					tempInorderSurr.add(matchedSourFeatures.get(i));
					count++;
				} else {
					tempNotInorderSurr.add(matchedSourFeatures.get(i));
					if (nextStartIndex == -1) {
						nextStartIndex = startIndex + i;
					}
				}
			}
			if (count > maxInOrder) {
				maxInOrder = count;
				maxInorderSurr = tempInorderSurr;
			}
			if (startIndex < nextStartIndex) {
				startIndex = nextStartIndex;
			} else { // all possibilities have been checked
				break;
			}
		}
		
		double total = maxInOrder;
//		for (Feature f : maxInorderSurr) {
//			if (matchList.isInvalid(f)) {
//				total += matchList.getConfidenceLevel(f);
//			} else {
//				total += 1;
//			}
//		}
		if (maxInorderSurr.contains(newInvalidFeature)) {
			total = total - 1 + matchList.getConfidenceLevel(newInvalidFeature);
		}
		
		double contextSimilarity = total / smallerLength;

//		System.out.print(String.format(("(%d/%d, %.3f): "), (int)maxInOrder, (int)smallerLength, contextSimilarity));
//		for (Feature f : target.getFeatureList()) {
//			System.out.print(f.getID() + " ");
//		}
//		System.out.println();
		
		return contextSimilarity;
	}
}
