package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;

public class StarContextCalculator extends AbstractContextCalculator{
	
	private int numSections;
	private int degreeRange;
	private String name = "Star Calculus Context Similarity";
	
	public StarContextCalculator(int degreeSectionRange) {
		this.numSections = 360 / degreeSectionRange;
		this.degreeRange = degreeSectionRange;
		System.out.println("Angle-Diffrence Context: angle tolerance is " + this.degreeRange);
	}
	
	public double calContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> srcSurr, boolean visualize) {
		if (srcSurr == null) {
			srcSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (srcSurr.size() == 0) {
			return 0.0;
		}
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		for (Feature f : srcSurr) {
//			if (f != sourceFeature && !matchList.isInvalid(f)) { // ignore the invalid matches & single objects
			if (f != sourceFeature) { // ignore the invalid matches & single objects
				sourceSurr.add(f);
			}
		}
		
//		for (int i = 0; i < sourceSurr.size(); i++) { // sometimes the center feature may also appears in surrounding feature list, then remove it
//			if (sourceSurr.get(i) == sourceFeature) {
//				sourceSurr.remove(i);
//				break;
//			}
//		}
		ArrayList<Feature> targetSurr = findCorrespondingFeatures(sourceSurr); // may exists null features
		
		int numSurroundingObjects = sourceSurr.size();
		
		// get the list of all matched surrounding objects, remove all the null features in source and target surr lists
		ArrayList<Feature> sourceMatchedFeatures = new ArrayList<Feature>();
		ArrayList<Feature> targetMatchedFeatures = new ArrayList<Feature>();
		for (int i = 0; i < numSurroundingObjects; i++) {
			if (sourceSurr.get(i) != null && targetSurr.get(i) != null) {
				sourceMatchedFeatures.add(sourceSurr.get(i));
				targetMatchedFeatures.add(targetSurr.get(i));
			}
		}
		
		ArrayList<Double> sourceDegrees = getDegreeList(sourceMatchedFeatures, sourceFeature);
		ArrayList<Double> targetDegrees = getDegreeList(targetMatchedFeatures, targetFeature);
		
		int sameSectionCount = 0;
		for (int i = 0; i < sourceDegrees.size(); i++) {
//			if (getSectionIndex(sourceDegrees.get(i)) == getSectionIndex(targetDegrees.get(i))) {
//				sameSectionCount++;
//			}
			double degreeDiff = Math.abs(sourceDegrees.get(i) - targetDegrees.get(i));
			if (degreeDiff > 180) {
				degreeDiff = 360 - degreeDiff;
			}
			if (degreeDiff <= degreeRange) {
				sameSectionCount++;
			} else {
//				System.out.println("------- Out of range! " + degreeDiff + " exceeds " + degreeRange);
			}
		}
		
		double res = (double)sameSectionCount / (double)numSurroundingObjects;
		if (!(res > 1) && !(res <= 1)) {
//			System.out.println(sameSectionCount + " / " + numSurroundingObjects + " = " + res);
		}
		return res;
	}
	
	
	public double checkContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> srcSurr) {
		if (srcSurr == null) {
			srcSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (srcSurr.size() == 0) {
			return 0.0;
		}
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		for (Feature f : srcSurr) {
//			if (f != sourceFeature && !matchList.isInvalid(f)) { // ignore the invalid matches & single objects
			if (f != sourceFeature) { // ignore the invalid matches & single objects
				sourceSurr.add(f);
			}
		}
		
//		for (int i = 0; i < sourceSurr.size(); i++) { // sometimes the center feature may also appears in surrounding feature list, then remove it
//			if (sourceSurr.get(i) == sourceFeature) {
//				sourceSurr.remove(i);
//				break;
//			}
//		}
		ArrayList<Feature> targetSurr = findCorrespondingFeatures(sourceSurr); // may exists null features
		
		int numSurroundingObjects = sourceSurr.size();
		
		// get the list of all matched surrounding objects, remove all the null features in source and target surr lists
		ArrayList<Feature> sourceMatchedFeatures = new ArrayList<Feature>();
		ArrayList<Feature> targetMatchedFeatures = new ArrayList<Feature>();
		
		ArrayList<Feature> sourceUnMatchedFeatures = new ArrayList<Feature>();
		ArrayList<Feature> targetUnMatchedFeatures = new ArrayList<Feature>();
		for (int i = 0; i < numSurroundingObjects; i++) {
			if (sourceSurr.get(i) != null && targetSurr.get(i) != null) {
				sourceMatchedFeatures.add(sourceSurr.get(i));
				targetMatchedFeatures.add(targetSurr.get(i));
			} else if (sourceSurr.get(i) == null) {
				targetUnMatchedFeatures.add(targetSurr.get(i));
			} else if (targetSurr.get(i) == null) {
				sourceUnMatchedFeatures.add(sourceSurr.get(i));
			}
		}
		
		ArrayList<Double> sourceDegrees = getDegreeList(sourceMatchedFeatures, sourceFeature);
		ArrayList<Double> targetDegrees = getDegreeList(targetMatchedFeatures, targetFeature);
		ArrayList<Feature> invalidSourceFeatures = new ArrayList<Feature>();
		ArrayList<Feature> invalidTargetFeatures = new ArrayList<Feature>();
		ArrayList<Integer> invalidFeatureIndices = new ArrayList<Integer>();
		
		int sameSectionCount = 0;
		for (int i = 0; i < sourceDegrees.size(); i++) {
//			if (getSectionIndex(sourceDegrees.get(i)) == getSectionIndex(targetDegrees.get(i))) {
//				sameSectionCount++;
//			}
			double degreeDiff = Math.abs(sourceDegrees.get(i) - targetDegrees.get(i));
			if (degreeDiff > 180) {
				degreeDiff = 360 - degreeDiff;
			}
			if (degreeDiff <= degreeRange) {
				sameSectionCount++;
			} else {
				invalidSourceFeatures.add(sourceMatchedFeatures.get(i));
				invalidTargetFeatures.add(targetMatchedFeatures.get(i));
				invalidFeatureIndices.add(i);
			}
		}
		
		double res = (double)sameSectionCount / (double)numSurroundingObjects;
		
		System.out.println(name + ": " + String.format("%.4f (%d/%d)", res, sameSectionCount, numSurroundingObjects));
		
		// print degree list
		System.out.println(sourceFeature.getID() + ": ");
		System.out.print("source: ");
		for (int i = 0; i < sourceDegrees.size(); i++) {
			System.out.print(sourceMatchedFeatures.get(i).getID() + " ");
		}
		System.out.print("\nsource: ");
		for (int i = 0; i < sourceDegrees.size(); i++) {
			System.out.print(String.format("%.4f ", sourceDegrees.get(i)));
		}
		System.out.print("\ntarget: ");
		for (int i = 0; i < targetDegrees.size(); i++) {
			System.out.print(String.format("%.4f ", targetDegrees.get(i)));
		}
		System.out.println("\n(" + sameSectionCount + " / " + numSurroundingObjects + ") = " + res);

		// print invalid supporting matches
		if (invalidSourceFeatures.size() > 0) {
			System.out.println("A match with degree difference smaller than " + sharedSpace.ANGLE_TOLERANCE + " is considered as valid supporting match");
			System.out.println("The following surrounding matches indicates a low context similarity:");
			for (int i = 0; i < invalidSourceFeatures.size(); i++) {
				double degreeDiff = Math.abs(sourceDegrees.get(invalidFeatureIndices.get(i)) - targetDegrees.get(invalidFeatureIndices.get(i)));
				System.out.println("\t" + invalidSourceFeatures.get(i).getID() + "--" + invalidTargetFeatures.get(i).getID() + ": degree difference: " + String.format("%.4f", degreeDiff));
			}
		}
		// print the unmatched surrounding features
		if (sourceUnMatchedFeatures.size() > 0 || targetUnMatchedFeatures.size() > 0) {
			System.out.println("The following surrounding object has no matching relations: ");
			for (int i = 0; i < sourceUnMatchedFeatures.size(); i++) {
				System.out.println("\tSource layer object id = " + sourceUnMatchedFeatures.get(i).getID());
			}
			for (int i = 0; i < targetUnMatchedFeatures.size(); i++) {
				System.out.println("\tTarget layer object id = " + targetUnMatchedFeatures.get(i).getID());
			}
		}
		 
		 
		sharedSpace.storeInvalidSurrMatchList(invalidSourceFeatures, invalidTargetFeatures);
		
		return res;
	}

	
	private ArrayList<Double> getDegreeList(ArrayList<Feature> features, Feature centerFeature) {
		ArrayList<Double> degrees = new ArrayList<Double>();
		
		Point centerCentroid = centerFeature.getGeometry().getCentroid();
		Double cX = centerCentroid.getX(); 
		Double cY = centerCentroid.getY();
		
		for (Feature f : features) {
			Point fCentroid = f.getGeometry().getCentroid();
			Double fX = fCentroid.getX();
			Double fY = fCentroid.getY();
			Double xDiff = fX - cX;
			Double yDiff = fY - cY;
			
			if (xDiff == 0 && yDiff == 0) {
				degrees.add(-1.0); // use negative degree value to represent the centroids overlay 
			} else {
				Double sin = yDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
				Double cos = xDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
				double degree = Math.toDegrees(Math.atan2(yDiff, xDiff)); // degree: [-180, 180] 
				if (degree < 0) {
					degree = degree + 360; // degree: [0, 360)
				}
				degrees.add(degree);
			}					
		}
		return degrees;
	}
	
	
	private int getSectionIndex(double degree) {
		return (int) (degree / degreeRange);
	}
	
	/**
	 * find the corresponding features (objects in target layer) of the surrounding objects of the being checked object in source layer
	 * @param sourceFeatures
	 * @return
	 */
	protected ArrayList<Feature> findCorrespondingFeatures(ArrayList<Feature> sourceFeatures) {
		ArrayList<Feature> targetFeatures = new ArrayList<Feature>();
		for (Feature f : sourceFeatures) {
			Feature targetF = matchList.getMatchedTargetFeature(f);
			targetFeatures.add(targetF);
		}
		return targetFeatures;
	}
}
