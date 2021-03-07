package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;

public class RouteContextCalculator extends AbstractContextCalculator{
	
	public double calContextSimilarity(Feature sourceFeature, ArrayList<Feature> sourceSurr, boolean visualize) {
		// if equals null, use the recorded surrounding object set
		if (sourceSurr == null) {
			sourceSurr = supportingRelations.getSupportingFeaturesOf(sourceFeature);
		}
		if (sourceSurr.size() == 0) {
			return 0.0;
		}
		AntiClockwiseSequence sourceSeq = orderFeaturesClockwise(sourceSurr, sourceFeature);
		AntiClockwiseSequence targetSeq = orderFeaturesClockwise(findCorrespondingFeatures(sourceSurr), matchList.getMatchedTargetFeature(sourceFeature));
		return sourceSeq.calContextSimilarityWith(targetSeq, visualize);
	}
	
	/**
	 * order the surrounding features (or their corresponding features in target layer) anti-clockwise
	 * @param surrFeatures
	 * @param center
	 * @return
	 */
	private AntiClockwiseSequence orderFeaturesClockwise(ArrayList<Feature> surrFeatures, Feature center) {
		Point centerCentroid = center.getGeometry().getCentroid();
		Double cX = centerCentroid.getX(); 
		Double cY = centerCentroid.getY();
		
		AntiClockwiseSequence sequence = new AntiClockwiseSequence();
		for (Feature f : surrFeatures) {
			if (f == center) { // sometimes the center feature also appears in surrounding feature list
//				System.out.println("f == center " + center.getID() + " " + f.getID());
				continue;
			}
			Point fCentroid = f.getGeometry().getCentroid();
			Double fX = fCentroid.getX();
			Double fY = fCentroid.getY();

			Double xDiff = fX - cX;
			Double yDiff = fY - cY;
			Double sin = yDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			Double cos = xDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			
//			if (sin >= 0 || sin < 0 ) {
//			} else {
//				System.out.println("sin = " + yDiff + " / " + Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5) + "(c:" + center.getID() + "--" +f.getID());
//			}
			
			RelativePosition p = new RelativePosition(f, sin, cos);
			sequence.add(p);
		}
		return sequence;
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
			if (targetF != null) {
				targetFeatures.add(targetF);
			}
		}
		return targetFeatures;
	}
	
}
