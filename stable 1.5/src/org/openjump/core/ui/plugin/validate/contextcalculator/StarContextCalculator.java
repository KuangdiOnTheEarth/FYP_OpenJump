package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;

public class StarContextCalculator extends AbstractContextCalculator{
	
	private int numSections;
	private int degreeRange;
	
	public StarContextCalculator(int degreeSectionRange) {
		this.numSections = 360 / degreeSectionRange;
		this.degreeRange = degreeSectionRange;
	}
	
	public double calContextSimilarity(Feature sourceFeature, ArrayList<Feature> sourceSurr, boolean visualize) {
		if (sourceSurr == null) {
			sourceSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (sourceSurr.size() == 0) {
			return 0.0;
		}
		for (int i = 0; i < sourceSurr.size(); i++) { // sometimes the center feature may also appears in surrounding feature list, then remove it
			if (sourceSurr.get(i) == sourceFeature) {
				sourceSurr.remove(i);
				break;
			}
		}
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
		ArrayList<Double> targetDegrees = getDegreeList(targetMatchedFeatures, matchList.getMatchedTargetFeature(sourceFeature));
		
		int sameSectionCount = 0;
		for (int i = 0; i < sourceDegrees.size(); i++) {
//			if (getSectionIndex(sourceDegrees.get(i)) == getSectionIndex(targetDegrees.get(i))) {
//				sameSectionCount++;
//			}
			if (Math.abs(sourceDegrees.get(i) - targetDegrees.get(i)) <= degreeRange) {
				sameSectionCount++;
			}
		}
		
		double res = (double)sameSectionCount / (double)numSurroundingObjects;
		if (res < 0.8) {
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
			System.out.println();
		}
		
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
