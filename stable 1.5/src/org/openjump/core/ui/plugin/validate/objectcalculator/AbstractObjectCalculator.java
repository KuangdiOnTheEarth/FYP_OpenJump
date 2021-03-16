package org.openjump.core.ui.plugin.validate.objectcalculator;

import com.vividsolutions.jump.feature.Feature;

public abstract class AbstractObjectCalculator {
	
	protected String name;
	
	public abstract double calObjectSimilarity(Feature f1, Feature f2);

}
