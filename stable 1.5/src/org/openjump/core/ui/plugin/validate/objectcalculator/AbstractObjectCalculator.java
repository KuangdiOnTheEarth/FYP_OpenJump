package org.openjump.core.ui.plugin.validate.objectcalculator;

import com.vividsolutions.jump.feature.Feature;

/**
 * The abstract class for object similarity calculators.
 * @author Guangdi Hu
 *
 */
public abstract class AbstractObjectCalculator {
	
	protected String name;
	
	/**
	 * Calculate and return the object similarity between the input objects.
	 * @param f1 One object involved in the candidate match.
	 * @param f2 The other object in this match.
	 * @return
	 */
	public abstract double calObjectSimilarity(Feature f1, Feature f2);

}
