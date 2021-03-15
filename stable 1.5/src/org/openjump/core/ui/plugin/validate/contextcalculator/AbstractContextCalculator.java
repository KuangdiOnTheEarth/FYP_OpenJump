package org.openjump.core.ui.plugin.validate.contextcalculator;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.SharedSpace;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jump.feature.Feature;

public abstract class AbstractContextCalculator {
	
	protected SharedSpace sharedSpace;
	protected MatchList matchList;
	protected SupportingRelations supportingRelations;
	protected String name;
	
	public AbstractContextCalculator() {
		sharedSpace = SharedSpace.getInstance();
		matchList = sharedSpace.getMatchList();
	}
	
	public abstract double calContextSimilarity(Feature sourceFeature, ArrayList<Feature> sourceSurr, boolean isBackTrack);
	
	public abstract double checkContextSimilarity(Feature sourceFeature, ArrayList<Feature> srcSurr);

	public void refreshSupportingRelation() {
		supportingRelations = sharedSpace.getSupportingRelations();
	}
}
