package org.openjump.core.ui.plugin.validate.pojo;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;

import com.vividsolutions.jump.feature.Feature;

/**
 * Automatically order the input relative positions in anti-clockwise order, 
 * supports the sequence-order context similarity calculation.
 * @author Guangdi Hu
 *
 */
public class AntiClockwiseSequence {
	
	private ArrayList<RelativePosition> sequence = null;
	private SharedSpace sharedSpace = null;
	
	
	public AntiClockwiseSequence() {
		sequence = new ArrayList<RelativePosition>();
		sharedSpace = SharedSpace.getInstance();
	}
	
	/**
	 * Insert a new object into correct place of the sequence (ordered by the angle of the items)
	 * @param p The new relative position of a supporting obejct
	 */
	public void add(RelativePosition p) {
		if (sequence.size() == 0) {
			sequence.add(p);
		} else {
			for (int index = 0; index < sequence.size(); index++) {
				if (!p.isAntiClockwiseThan(sequence.get(index))) {
					sequence.add(index, p);
					return;
				}
			}
			sequence.add(p);
		}
	}
	
	/**
	 * Return the number of objects in the sequence.
	 */
	public int size() {
		return sequence.size();
	}
	
	/**
	 * Get the list of objects of the recorded relative positions.
	 * @return
	 */
	public ArrayList<Feature> getFeatureList() {
		ArrayList<Feature> list = new ArrayList<Feature>();
		for (RelativePosition rp : sequence) {
			list.add(rp.feature());
		}
		return list;
	}
	
	/**
	 * Compare two anti-clockwise sequences, try to make two sequence matched by removing the minimal number of objects
	 * @param target Another anti-clockwise sequence to be compared with
	 * @return The context similarity between the owners of these two sequences (contexts)
	 */
	public Double calContextSimilarityWith(AntiClockwiseSequence target, boolean visualize) {
		
		MatchList matchList = sharedSpace.getMatchList();
		ArrayList<Feature> sourceFeatures = new ArrayList<Feature>();
		
		for (Feature f : this.getFeatureList()) {
			if (!matchList.isInvalid(f)) { // consider not-invalid matches AND not matched objects
				sourceFeatures.add(f);
			}
		}

		ArrayList<Feature> targetFeatures = target.getFeatureList();
		
		double smallerLength = Math.min(sourceFeatures.size(), targetFeatures.size());
		
		// the matched indices of corresponding objects in target sequence for the non-single objects in source sequence
		ArrayList<Integer> corrIndices = new ArrayList<Integer>(); 
		// remove the single objects from source feature sequence
		for (Feature sf : sourceFeatures) {
			for (int i = 0; i < targetFeatures.size() ; i++) {
				if (matchList.getMatchedTargetFeature(sf) == targetFeatures.get(i)) {
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
		if (visualize) {
			System.out.print(String.format(("(%d/%d, %.3f): "), (int)maxInOrder, (int)smallerLength, (maxInOrder/smallerLength)));
			for (Feature f : target.getFeatureList()) {
				System.out.print(f.getID() + " ");
			}
			System.out.println();
		}
		return maxInOrder / smallerLength;
	}
	
	
	
	/**
	 * Called by the scrutinize plugIn, print out the invalid surrounding matches & store them into sharedSpace for later visualization
	 * @param target Another sequence to be compared with
	 * @param visualize
	 * @return
	 */
	public Double checkContextSimilarityWith(AntiClockwiseSequence target, boolean visualize) {
		
		MatchList matchList = sharedSpace.getMatchList();
		ArrayList<Feature> sourceFeatures = new ArrayList<Feature>();
		
		for (Feature f : this.getFeatureList()) {
			if (!matchList.isInvalid(f)) { // consider not-invalid matches AND not matched objects
				sourceFeatures.add(f);
			} 
		}
		ArrayList<Feature> targetFeatures = target.getFeatureList();
		
		double smallerLength = Math.min(sourceFeatures.size(), targetFeatures.size());
		
		// the matched indices of corresponding objects in target sequence for the non-single objects in source sequence
		ArrayList<Integer> corrIndices = new ArrayList<Integer>();
		// the corresponding indices of source list
		ArrayList<Integer> sourceIndices = new ArrayList<Integer>();
		// remove the single objects from source feature sequence
		for (int j = 0; j < sourceFeatures.size(); j++) {
			for (int i = 0; i < targetFeatures.size() ; i++) {
				if (matchList.getMatchedTargetFeature(sourceFeatures.get(j)) == targetFeatures.get(i)) {
					corrIndices.add(i);
					sourceIndices.add(j);
					break;
				}
			}
		}
		if (corrIndices.size() == 0) {
			return 0.0;
		}
		
		ArrayList<Feature> invalidSourceFeatures = new ArrayList<Feature>();
		ArrayList<Feature> invalidTargetFeatures = new ArrayList<Feature>();
		
		// find the max in-order numbers in corrIndices
		double maxInOrder = 1;
		int startIndex = 0;
		while (startIndex + maxInOrder < corrIndices.size()) {
			int nextStartIndex = -1;
			int count = 1;
			int temp = corrIndices.get(startIndex);
			ArrayList<Integer> tempSourceNotInorderIndices = new ArrayList<Integer>();
			ArrayList<Integer> tempTargetNotInorderIndices = new ArrayList<Integer>();
			for (int i = 1; i + startIndex < corrIndices.size(); i++) {
				if (corrIndices.get(startIndex + i) >= temp) {
					temp = corrIndices.get(startIndex + i);
					count++;
				} else {
					tempSourceNotInorderIndices.add(sourceIndices.get(startIndex + i));
					tempTargetNotInorderIndices.add(corrIndices.get(startIndex + i));
					if (nextStartIndex == -1) {
						nextStartIndex = startIndex + i;
					}
				}
			}

			if (count > maxInOrder) {
				maxInOrder = count;
				invalidSourceFeatures.clear();
				invalidTargetFeatures.clear();
				for (int i : tempSourceNotInorderIndices) {
					invalidSourceFeatures.add(sourceFeatures.get(i));
				}
				for (int i : tempTargetNotInorderIndices) {
					invalidTargetFeatures.add(targetFeatures.get(i));
				}
				
				System.out.println(invalidSourceFeatures.size() + " " + invalidTargetFeatures.size());
			}
			if (startIndex < nextStartIndex) {
				startIndex = nextStartIndex;
			} else { // all possibilities have been checked
				break;
			}
		}
		sharedSpace.storeInvalidSurrMatchList(invalidSourceFeatures, invalidTargetFeatures);
		if (visualize) {
			System.out.println(String.format(("(%d/%d, %.3f): "), (int)maxInOrder, (int)smallerLength, (maxInOrder/smallerLength)));

			System.out.println("The following surrounding matches are not inorder amonge the neighbouring sequence:");
			System.out.println(invalidSourceFeatures.size() + " " + invalidTargetFeatures.size());

			for (int i = 0; i < invalidSourceFeatures.size(); i++) {
				System.out.println("\t" + invalidSourceFeatures.get(i).getID() + " -- " + invalidTargetFeatures.get(i).getID());
			}
		}
		return maxInOrder / smallerLength;
	}
}
