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
		
//		System.out.print("Source Surrounding: ");
//		for (Feature f : this.getFeatureList()) {
//			System.out.print(f.getID() + " ");
//		}

//		System.out.print("\nTarget Surrounding: ");
//		for (Feature f : target.getFeatureList()) {
//			System.out.print(f.getID() + " ");
//		}
//		System.out.print("\n sin: ");
//		for (RelativePosition rp : target.sequence) {
//			System.out.print(String.format("%.4f ", rp.sin()));
//		}
//		System.out.print("\n cos: ");
//		for (RelativePosition rp : target.sequence) {
//			System.out.print(String.format("%.4f ", rp.cos()));
//		}
//		System.out.println();
		
		
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
		
		System.out.print("(" + (int)maxInOrder + "/" + (int)smallerLength + "): ");
		for (Feature f : target.getFeatureList()) {
			System.out.print(f.getID() + " ");
		}
		System.out.println();
		
		return maxInOrder / smallerLength;
	}
}
