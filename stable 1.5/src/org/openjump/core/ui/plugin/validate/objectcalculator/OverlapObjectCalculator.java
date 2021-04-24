package org.openjump.core.ui.plugin.validate.objectcalculator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;

/**
 * Calculate the object similarity using the overlap proportion measure.
 * @author Guangdi Hu
 *
 */
public class OverlapObjectCalculator extends AbstractObjectCalculator{
	
	private String name = "Area Overlap Object Similarity";

	@Override
	public double calObjectSimilarity(Feature f1, Feature f2) {
		Geometry f1Geom = f1.getGeometry(); 
		Geometry f2Geom = f2.getGeometry(); 
		Geometry overlap = f1Geom.intersection(f2Geom);
		double baseArea = f1Geom.getArea() + f2Geom.getArea();
		return (2 * overlap.getArea()) / baseArea;
	}

}
