package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;


/**
 * This context similarity calculator evaluates the context similarity using sequence-order measure.
 * @author Guangdi Hu
 *
 */
public class RouteContextCalculator extends AbstractContextCalculator{
	
	private String name = "Sequence Order Context Similarity";
	
	/**
	 * Calculate the context similarity using angle difference measure.
	 */
	public double calContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> srcSurr) {
		// if equals null, use the recorded surrounding object set
		if (srcSurr == null) {
			srcSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (srcSurr.size() == 0) {
			return 0.0;
		}
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		for (Feature f : srcSurr) {
			if (f != sourceFeature && !matchList.isInvalid(f)) { // ignore the invalid matches & single objects
				sourceSurr.add(f);
			}
		}
		AntiClockwiseSequence sourceSeq = orderFeaturesClockwise(sourceSurr, sourceFeature);
		AntiClockwiseSequence targetSeq = orderFeaturesClockwise(findCorrespondingFeatures(sourceSurr), targetFeature);
		return sourceSeq.calContextSimilarityWith(targetSeq);
	}
	
	
	
	/**
	 * Record and report the detailed process in context similarity computation.
	 */
	@Override
	public double checkContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> srcSurr) {
		if (srcSurr == null) {
			srcSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (srcSurr.size() == 0) {
			return 0.0;
		}
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		for (Feature f : srcSurr) {
			if (f != sourceFeature && !matchList.isInvalid(f)) { // ignore the invalid matches & single objects
				sourceSurr.add(f);
			}
		}
		System.out.println("Checking " + this.name);
		AntiClockwiseSequence sourceSeq = orderFeaturesClockwise(sourceSurr, sourceFeature);
		AntiClockwiseSequence targetSeq = orderFeaturesClockwise(findCorrespondingFeatures(sourceSurr), targetFeature);
		return sourceSeq.checkContextSimilarityWith(targetSeq, true);
	}
	
	/**
	 * Order the supporting features in anti-clockwise order with the center of the candidate object
	 * @param surrFeatures The list of supporting matches
	 * @param center the candidate object
	 * @return
	 */
	private AntiClockwiseSequence orderFeaturesClockwise(ArrayList<Feature> surrFeatures, Feature center) {
		Point centerCentroid = center.getGeometry().getCentroid();
		Double cX = centerCentroid.getX(); 
		Double cY = centerCentroid.getY();
		
		AntiClockwiseSequence sequence = new AntiClockwiseSequence();
		for (Feature f : surrFeatures) {
			if (f == center) { // sometimes the center feature also appears in surrounding feature list
				continue;
			}
			Point fCentroid = f.getGeometry().getCentroid();
			Double fX = fCentroid.getX();
			Double fY = fCentroid.getY();

			Double xDiff = fX - cX;
			Double yDiff = fY - cY;
			Double sin = yDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			Double cos = xDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			
			RelativePosition p = new RelativePosition(f, sin, cos);
			sequence.add(p);
		}
		return sequence;
	}
	
	
	/**
	 * Find the corresponding features (objects in target layer) of the surrounding objects of the being checked object in source layer
	 * @param sourceFeatures
	 * @return
	 */
	protected ArrayList<Feature> findCorrespondingFeatures(ArrayList<Feature> sourceFeatures) {
		ArrayList<Feature> targetFeatures = new ArrayList<Feature>();
		for (Feature f : sourceFeatures) {
			Feature targetF = matchList.getMatchedTargetFeature(f);
			if (targetF != null) {
				targetFeatures.add(targetF);
			}
		}
		return targetFeatures;
	}

}
