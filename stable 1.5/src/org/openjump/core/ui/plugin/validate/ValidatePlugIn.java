package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.contextcalculator.AbstractContextCalculator;
import org.openjump.core.ui.plugin.validate.objectcalculator.AbstractObjectCalculator;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import javafx.util.Pair;

/**
 * The plug-in for validating input matches. This plug-in works on the matches generated {@link MatchMap}, 
 * classify the input matches into valid and invalid match sets, then detect the missing match from the two matched object sets.
 * Finally, the valid match set, invalid match set and missing match set will be visualized in different layers.
 * @author Guangdi Hu
 *
 */
public class ValidatePlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	// Labels in UI
	private final String T_Context_Measure = "context similarity measure";
	private final String T_Threshold = "validation threshold";
	private final String T_Context_Weight = "weight of context similarity";
	private final String T_Context_NUM = "mininal neighbouring objects";
	private final String T_Angle_Tolerance = "angle tolerance";
	
	// Parameters (set by user in dialog window)
	private double VALID_THRESHOLD = 0.8;
	private double CONTEXT_WEIGHT = 0.8;
	private String CONTEXT_MEASURE = "";
	private int ANGLE_TOLERANCE = 5;
	
	/**
	 * The minimal number of supporting objects needed in context similarity calculation
	 */
	private int MIN_SURR_OBJ_NEEDED = 5;
	
	
	/**
	 * Control the growth rate of the buffer. 
	 * If the current buffer radius fails to capture enough supporting matches, 
	 * the buffer will be extended in this rate to search for supporting matches again.
	 */
	private final double BUFFER_INC_RATE = 1.1;
	
	private SharedSpace sharedSpace;
	private MatchList matchList;
	private SupportingRelations supportingRelations;
	
	private AbstractContextCalculator contextSimilarityCalculator;
	private AbstractObjectCalculator objectSimilarityCalculator;
	
	private String contextSimilarityType = "star"; // star || sequence
	private String objectSimilarityType = "overlay";
	
	
	
	public void initialize(PlugInContext context) throws Exception {
	    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
	    featureInstaller.addMainMenuItem(
	    	        this,
	                new String[] {"ValidateMatches"}, 	//menu path
	                this.getName(),
	                false,			//checkbox
	                null,			//icon
	                createEnableCheck(context.getWorkbenchContext())); //enable check
	    sharedSpace = SharedSpace.getInstance();
    }

	public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck()
                        .add(checkFactory.createAtLeastNLayersMustExistCheck(2))
                        .add(checkFactory.createTaskWindowMustBeActiveCheck());
    }
	
	public boolean execute(PlugInContext context) throws Exception{
	    this.reportNothingToUndoYet(context);
	    MultiInputDialog dialog = new MultiInputDialog(
	            context.getWorkbenchFrame(), getName(), true);
        this.setDialogValues(dialog, context);
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (! dialog.wasOKPressed()) { return false; }
        this.getDialogValues(dialog);
        return true;
	}
	
    private void setDialogValues(MultiInputDialog dialog, PlugInContext context){
    	ArrayList<String> contextSimilarityMeasures = new ArrayList<String>();
        contextSimilarityMeasures.add("angle difference");
        contextSimilarityMeasures.add("sequence order");
        dialog.addComboBox(T_Context_Measure, contextSimilarityMeasures.get(0), contextSimilarityMeasures, null);
        dialog.addPositiveDoubleField(T_Threshold, this.VALID_THRESHOLD, 4, null);
        dialog.addPositiveDoubleField(T_Context_Weight, this.CONTEXT_WEIGHT, 4);
        dialog.addPositiveIntegerField(T_Context_NUM, MIN_SURR_OBJ_NEEDED, 4);
        dialog.addPositiveIntegerField(T_Angle_Tolerance, this.ANGLE_TOLERANCE, 4, null);
	  }

    private void getDialogValues(MultiInputDialog dialog){
        this.VALID_THRESHOLD = dialog.getDouble(T_Threshold);
        this.CONTEXT_WEIGHT = dialog.getDouble(T_Context_Weight);
        this.CONTEXT_MEASURE = dialog.getText(T_Context_Measure);
        this.MIN_SURR_OBJ_NEEDED = dialog.getInteger(T_Context_NUM);
        this.ANGLE_TOLERANCE = dialog.getInteger(T_Angle_Tolerance);
	  }
    
    
    
	
    /**
     * The main body of the validation algorithm
     */
	@Override
	public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
		
		/*
		 * Call factory to generate required context & object similarity calculator.
		 */
		if (CONTEXT_MEASURE == "angle difference") {
			this.contextSimilarityType = "star";
		} else if (CONTEXT_MEASURE == "sequence order") {
			this.contextSimilarityType = "sequence";
		}
		sharedSpace.setSimilarityType(contextSimilarityType, objectSimilarityType);
		sharedSpace.setAngleTolerance(ANGLE_TOLERANCE);
		contextSimilarityCalculator = sharedSpace.getContextCalculator();
		objectSimilarityCalculator = sharedSpace.getObjectCalculator();
		
		/*
		 * Fetch the match set, initialize the status of input matches.
		 */
		this.matchList = sharedSpace.getMatchList();
		matchList.clear();
		matchList.setValidThreshold(VALID_THRESHOLD);
		matchList.setContextWeight(CONTEXT_WEIGHT);
		
		System.gc();
		System.out.println("\n------\nStart ValidationPlugIn ...\n------");
		System.out.println("Validation Threshold: " + VALID_THRESHOLD + "; Context Similarity Weight: " + CONTEXT_WEIGHT + "; Measure: " + contextSimilarityType);
		System.out.println("Angle tolerance: " + sharedSpace.getAngleTolerance());
		System.out.println("Input dataset contains " + matchList.getUnmatchedSourceFeatures().size() + " alone source objects");
		System.out.println("Checking input matches: " + matchList.getSourceList().size());
		
		/*
		 * Initialize a queue to contain the discovered matches in the forward traversal.
		 */
		Queue<Feature> queue = new LinkedList<Feature>();
		Random random = new  Random();
		int numFeatures = matchList.numberOfFeatures();
		if (numFeatures < 0) {
			System.out.println("Feature numbers in MatchList are not aligned");
			return;
		}
		Feature startingFeature = matchList.getSourceFeatureByIndex(random.nextInt(numFeatures));
		queue.offer(startingFeature);
		matchList.setAsInQueue(startingFeature);
		
		/*
		 * Fetch the source & reference geographic object sets.
		 */
		final List<Feature> sourceFeatures = sharedSpace.getSourceLayer().getFeatureCollectionWrapper().getFeatures();
		final List<Feature> targetFeatures = sharedSpace.getTargetLayer().getFeatureCollectionWrapper().getFeatures();
		
		/*
		 * Initialize the record of supporting relations.
		 */
		supportingRelations = new SupportingRelations();
		sharedSpace.storeSupportingRelations(supportingRelations);
		contextSimilarityCalculator.refreshSupportingRelation();
		
		
				
		///////////////////////////////////////////
		// Validate Input Matches
		///////////////////////////////////////////
		while (!queue.isEmpty()) {
			// Get the match to be validated
			Feature sourceFeature = queue.poll();
			
			// Find the surrounding matches
			ArrayList<Feature> sourceSurr = findSurroundingMatch(sourceFeature, sourceFeatures, queue, true, false);
			
			/*
			 * Calculate context similarity & object similarity.
			 */
			double contextSimilarity = calContextSimilarity(sourceFeature, matchList.getMatchedTargetFeature(sourceFeature), sourceSurr);
			matchList.setContextSimilarity(sourceFeature, contextSimilarity);
			double objectSimilarity = objectSimilarityCalculator.calObjectSimilarity(sourceFeature, matchList.getMatchedTargetFeature(sourceFeature));
			matchList.setObjectSimilarity(sourceFeature, objectSimilarity);
			
			/*
			 * Backtrack if the match is considered as invalid.
			 */
			if (matchList.getConfidenceLevel(sourceFeature) >= VALID_THRESHOLD) {
				matchList.setAsValid(sourceFeature);
			} else {
				matchList.setAsInvalid(sourceFeature);
				backtrackRecursion(sourceFeature, "");
			}
			
			/*
			 * Check and pick up the undiscovered matches caused by gap between object clusters
			 */
			if (queue.isEmpty()) {
				int c = 0;
				for (Feature f : sourceFeatures) {
					if (matchList.shouldBeQueued(f)) {
						c++;
					}
				}
				System.out.println("Current UNDISCOVERED objects: " + c);
				matchList.supplementSingleMatch(queue);
			}
		}
		int validatedMatchNum = matchList.getSourceList().size();
		System.out.println("Complete validating input matches: " + validatedMatchNum);
		
		
		
		///////////////////////////////////////////
		// Detect Missing Matches
		///////////////////////////////////////////

		/*
		 * Find potential matches for all the alone objects in the source layer
		 */
		ArrayList<Feature> unmatchedSourceFeatures = matchList.getUnmatchedSourceFeatures();
		for (Feature singleF : unmatchedSourceFeatures) {
			ArrayList<Feature> tempMatch = new ArrayList<Feature>();
			Geometry buffer = singleF.getGeometry();
			
			/*
			 *  Find potential match object from target matched list
			 */
			for (Feature f : targetFeatures) {
				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
					tempMatch.add(f);
			}
			/*
			 *  Find potential match object from single target layer objects
			 */
			for (Feature f : matchList.getUnmatchedTargetFeatures()) {
				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
					tempMatch.add(f);
			}
			
			/*
			 *  Calculate the confidence level for each potential match
			 */
			for (Feature potentialMatchedObject : tempMatch) {
				ArrayList<Feature> sourceSurr = findSurroundingMatch(singleF, sourceFeatures, null, false, false);
				double contextSimilarity = calContextSimilarity(singleF, potentialMatchedObject, sourceSurr);
				double objectSimilarity = objectSimilarityCalculator.calObjectSimilarity(singleF, potentialMatchedObject);
				double confidenceLevel = contextSimilarity*matchList.getContextWeight() + objectSimilarity*(1-matchList.getContextWeight());

				if (confidenceLevel >= VALID_THRESHOLD) {
					matchList.storeMatch(singleF, potentialMatchedObject);
					matchList.setAsNew(singleF);
					supportingRelations.addMatchSpace();
					findSurroundingMatch(singleF, sourceFeatures, null, true, true);
					calContextSimilarity(singleF, potentialMatchedObject, sourceSurr);
					matchList.setContextSimilarity(singleF, contextSimilarity);
					matchList.setObjectSimilarity(singleF, objectSimilarity);
				}
			}
		}
		System.out.println("Detected " + (matchList.getSourceList().size()-validatedMatchNum) + " missing matches");
		
		showResult(context);
		System.out.println("Validation Finished \n");
	}
	

	/**
	 * Find the supporting matches / Define the context for a candidate match.
	 * @param sourceFeature The source layer object involved in the being checked candidate match
	 * @param sourceFeatures Source geographic object set
	 * @param queue Used to recored the discovered matches to support the forward traversal
	 * @param recordProcess Set as true if the supporting matches should be recorded in supporting relations; this field is false when testing the potential matches 
	 * @param isMissingMatch Set as true if the current being checked match is a missing match, this will leads to difference when recording the dependencies
	 * @return
	 */
	private ArrayList<Feature> findSurroundingMatch(Feature sourceFeature, List<Feature> sourceFeatures, Queue<Feature> queue, boolean recordProcess, boolean isMissingMatch) {
		/*
		 *  Create a buffer for the source layer object involved in the current match 
		 */
		Geometry sfGeom = sourceFeature.getGeometry();
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		
		double radius = sfGeom.getLength() / 4 / 4;
		
		// An alternative way to initialize buffer radius. But not applicable when area < 1, since square root of area will become bigger
//		double radius = Math.pow(sfGeom.getArea(), 1/2) / 4;
		
		Geometry buffer = sfGeom.buffer(radius);
		while (sourceSurr.size() < MIN_SURR_OBJ_NEEDED) {
			sourceSurr.clear();
			buffer = sfGeom.buffer(radius);
			
	        /*
	         *  Create lists to contain the surrounding objects.
	         */
			for (Feature f : sourceFeatures) {
				if (buffer.intersects(f.getGeometry()) && f != sourceFeature && matchList.isSuitableSupportingMatch(f)) {
					sourceSurr.add(f);
				}
			}
			radius = radius * BUFFER_INC_RATE;
		}
		if (recordProcess) {
			matchList.setBufferRadius(sourceFeature, radius);
		}
		
		/*
		 *  Add surrounding features (matches) into queue
		 */
		if (queue != null) {
			for (Feature f : sourceSurr) {
				if (matchList.shouldBeQueued(f)) {
					queue.add(f);
					matchList.setAsInQueue(f);
				}
			}
		}
		
		/*
		 *  Record the dependencies between candidate match and its supporting matches
		 */
		if (recordProcess) {
			supportingRelations.addSupportingRelation(sourceFeature, sourceSurr, isMissingMatch);
		}
		
		return sourceSurr;
	}
	
	
	/**
	 * Backtrack the and remedy the influence of an invalid match in a recursive way, 
	 * recalculate the context similarity for all matches this invalid match supports.
	 * @param invalidFeature The invalid match.
	 * @param pre A prefix of the printed information, used to control the style
	 */
	private void backtrackRecursion(Feature invalidFeature, String pre) {
		System.out.println(pre + "start backtrack " + invalidFeature.getID());
		
		/*
		 * Used to control the style of the information printed out
		 */
		pre += "\t";
		int round = 1;
		
		ArrayList<Feature> supports = supportingRelations.getFeaturesSupportedBy(invalidFeature);
		for (Feature f : supports) {

			double preCL = matchList.getContextSimilarity(f); // confidence level before re-calculation
			
			ArrayList<Feature> sourceSurr = findSurroundingMatch(f, matchList.getSourceList(), null, true, false);
			double contextSimilarity = calContextSimilarity(f, matchList.getMatchedTargetFeature(f), sourceSurr);
			matchList.setContextSimilarity(f, contextSimilarity);
			
			if (preCL >= VALID_THRESHOLD && matchList.getConfidenceLevel(f) < VALID_THRESHOLD) {
				System.out.println(pre + round + " Discover new invalid " + f.getID() + "  (" + preCL + "-->"+ matchList.getConfidenceLevel(f));
				matchList.setAsInvalid(f);
				// Use recursion to deal with chain effect occurs
				backtrackRecursion(f, pre);
			} else if (preCL < VALID_THRESHOLD && matchList.getConfidenceLevel(f) >= VALID_THRESHOLD) {
				System.out.println(pre + round + " Recover as valid " + f.getID() + "  (" + preCL + "-->"+ matchList.getConfidenceLevel(f));
				matchList.setAsValid(f);
			}
			round++;
		}
	}
	
	
	/**
	 * An auxiliary method for calculating context similarity. 
	 */
	private double calContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> sourceSurr) {
		return contextSimilarityCalculator.calContextSimilarity(sourceFeature, targetFeature, sourceSurr);
	}
	
	/**
	 * Display the result of validation, visualize the three output sets in new layers  
	 * @param context
	 */
	private void showResult(PlugInContext context) {		
		Pair<FeatureCollection, FeatureCollection> pair = matchList.getValidationResult();
		context.addLayer(StandardCategoryNames.WORKING, "Valid Matches", pair.getKey());
		context.addLayer(StandardCategoryNames.WORKING, "Invalid Matches", pair.getValue());
		Pair<FeatureCollection, FeatureCollection> npair = matchList.getNewMatches();
		context.addLayer(StandardCategoryNames.WORKING, "Missing Matches", npair.getKey());
//		context.addLayer(StandardCategoryNames.WORKING, "New Matches -- target layer", npair.getValue());
		System.out.println("result\nvalid matches: " + pair.getKey().size() +"; invalide matches: " + pair.getValue().size() + "; new matches: " + npair.getKey().size());
	}

}
