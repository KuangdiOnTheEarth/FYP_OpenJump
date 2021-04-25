package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jump.feature.Feature;


/**
 * The abstract class for context similarity calculators.
 * @author Guangdi Hu
 *
 */
public abstract class AbstractContextCalculator {
	
	protected SharedSpace sharedSpace;
	protected MatchList matchList;
	protected SupportingRelations supportingRelations;
	
	public abstract String getName();
	
	public AbstractContextCalculator() {
		sharedSpace = SharedSpace.getInstance();
		matchList = sharedSpace.getMatchList();
	}
	
	/**
	 * Calculate and return the context similarity
	 * @param sourceFeature The source layer object involved in the candidate match
	 * @param targetFeature The target layer (reference layer) object involved in the candidate match
	 * @param sourceSurr The source layer objects involved in supporting matches
	 * @return context similarity
	 */
	public abstract double calContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> sourceSurr);
	
	/**
	 * Called by the scrutinize plug-in, record and report the detailed process in context similarity computation.
	 * @param sourceFeature The source layer object involved in the candidate match
	 * @param targetFeature The target layer (reference layer) object involved in the candidate match
	 * @param sourceSurr The source layer objects involved in supporting matches
	 * @return Information about detailed process in context similarity computation
	 */
	public abstract String checkContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> srcSurr);

	/**
	 * Refresh and update the supporting relation list
	 */
	public void refreshSupportingRelation() {
		supportingRelations = sharedSpace.getSupportingRelations();
	}
}
