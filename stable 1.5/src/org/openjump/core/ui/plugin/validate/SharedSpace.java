package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.validate.contextcalculator.AbstractContextCalculator;
import org.openjump.core.ui.plugin.validate.contextcalculator.RouteContextCalculator;
import org.openjump.core.ui.plugin.validate.contextcalculator.StarContextCalculator;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;

import javafx.util.Pair;

public class SharedSpace {

	private static SharedSpace singleSharedSpace;
	
	private Layer sourceLayer = null;
	private Layer targetLayer = null;
	
	private MatchList matchList = null;
	private SupportingRelations supportingRelations = null;
	private Pair<ArrayList<Feature>, ArrayList<Feature>> invalidSurrMatches = null; // source invalid features, matched target layer objects
	
	/**
	 * The following are for checking omitted matches
	 */
	private MatchList omittedMatchList = null;
	private SupportingRelations omittedSupportingRelations = null;
	private Pair<ArrayList<Feature>, ArrayList<Feature>> omittedInvalidSurrMatches = null; // source invalid features, matched target layer objects
	
	
	private SharedSpace() {}
	
	private final String CS_STAR = "star";
	private final String CS_SEQ = "sequence";
	private final String OS_OVERLAY = "overlay";
	
	private String currentContextSimilarityType = CS_STAR;
	private String currentObjectSimilarityType = OS_OVERLAY;
	
	public final int STAR_DEGREE_RANGE = 5;
	
	/**
	 * The instance getter of Single Pattern
	 * @return the single SharedSpaceInstance
	 */
	public static synchronized SharedSpace getInstance() {
        if (singleSharedSpace == null) {
        	singleSharedSpace = new SharedSpace ();
        }
		return singleSharedSpace;
	}
	
	
	public void storeMatchList(MatchList matchList) {
		this.matchList = matchList;
//		System.out.println("--SharedSpace-- match list has been stored");
	}
	
	public MatchList getMatchList() {
		return this.matchList;
	}
	
	public void storeOmittedMatchList(MatchList matchList) {
		this.omittedMatchList = matchList;
//		System.out.println("--SharedSpace-- omittedMatchList has been stored");
	}
	
	public MatchList getOmittedMatchList() {
		return this.omittedMatchList;
	}
	
	
	public void storeLayers(Layer sourceLayer, Layer targetLayer) {
		this.sourceLayer = sourceLayer;
		this.targetLayer = targetLayer;
	}
	
	public Layer getSourceLayer() {
		return sourceLayer;
	}
	
	public Layer getTargetLayer() {
		return targetLayer;
	}
	
	public void storeSupportingRelations(SupportingRelations sr) {
		this.supportingRelations = sr;
	}
	
	public SupportingRelations getSupportingRelations() {
		return this.supportingRelations;
	}
	
	public void storeOmittedSupportingRelations(SupportingRelations sr) {
		this.omittedSupportingRelations = sr;
	}
	
	public SupportingRelations getOmittedSupportingRelations() {
		return this.omittedSupportingRelations;
	}
	
	public void storeInvalidSurrMatchList(ArrayList<Feature> sourceFeatures, ArrayList<Feature> targetFeatures) {
		this.invalidSurrMatches = new Pair<>(sourceFeatures, targetFeatures);
	}
	
	public Pair<ArrayList<Feature>, ArrayList<Feature>> getInvalidSurrMatchList() {
		return this.invalidSurrMatches;
	}
	
	public void storeOmittedInvalidSurrMatchList(ArrayList<Feature> sourceFeatures, ArrayList<Feature> targetFeatures) {
		this.omittedInvalidSurrMatches = new Pair<>(sourceFeatures, targetFeatures);
	}
	
	public Pair<ArrayList<Feature>, ArrayList<Feature>> getOmittedInvalidSurrMatchList() {
		return this.omittedInvalidSurrMatches;
	}
	
	public void setSimilarityType(String contextSimilarityType, String objectSimilarityType) {
		if (contextSimilarityType == CS_STAR) {
			currentContextSimilarityType = CS_STAR;
		} else if (contextSimilarityType == CS_SEQ) {
			currentContextSimilarityType = CS_SEQ;
		}
		
		if (objectSimilarityType == OS_OVERLAY) {
			currentObjectSimilarityType = OS_OVERLAY;
		}
	}
	
	public AbstractContextCalculator getContextCalculator() {
		if (currentContextSimilarityType == CS_STAR) {
			return new StarContextCalculator(this.STAR_DEGREE_RANGE);
		} else if (currentContextSimilarityType == CS_SEQ) {
			return new RouteContextCalculator();
		} 
		
		
		else {
			return new StarContextCalculator(this.STAR_DEGREE_RANGE);
		}
	}
	
}
