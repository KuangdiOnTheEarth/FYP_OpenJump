package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;

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
	private Pair<ArrayList<Feature>, ArrayList<Feature>> invalidSurrMatches = null;
	
	private SharedSpace() {}
	
	public final int STAR_DEGREE_RANGE = 10;
	
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
		System.out.println("--SharedSpace-- match list has been stored");
	}
	
	public MatchList getMatchList() {
		return this.matchList;
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
	
	public void storeInvalidSurrMatchList(ArrayList<Feature> sourceFeatures, ArrayList<Feature> targetFeatures) {
		this.invalidSurrMatches = new Pair<>(sourceFeatures, targetFeatures);
	}
	
	public Pair<ArrayList<Feature>, ArrayList<Feature>> getInvalidSurrMatchList() {
		return this.invalidSurrMatches;
	}
}
