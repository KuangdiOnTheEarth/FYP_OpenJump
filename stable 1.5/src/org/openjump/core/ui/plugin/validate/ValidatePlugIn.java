package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.contextcalculator.AbstractContextCalculator;
import org.openjump.core.ui.plugin.validate.contextcalculator.RouteContextCalculator;
import org.openjump.core.ui.plugin.validate.contextcalculator.StarContextCalculator;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

import javafx.util.Pair;

public class ValidatePlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	private final double VALID_THRESHOLD = 0.8; // if confidence level exceeds or equal to threshold, the match is considered as valid
	private final int MIN_SURR_OBJ_NEEDED = 5; // the minimal number of surrounding objects needed
	private final double BUFFER_INC_RATE = 1.1; // if too less surrounding object is found, the radius of buffer will increase at this rate
	
	private SharedSpace sharedSpace;
	private MatchList matchList;
	private SupportingRelations supportingRelations;
	
//	private final double bufferRadius = 0.5;
	private boolean VISUALIZE_VALIDATION_PROCESS = false;
	private AbstractContextCalculator contextSimilarityCalculator;
	
	private String pluginName = "ValidateAbstractPlugIn";
	private String contextSimilarityType = "sequence"; // star || sequence
	private String objectSimilarityType = "overlay";
	
	public void initialize(PlugInContext context) throws Exception {
	    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
	    featureInstaller.addMainMenuItem(
	    	        this,
	                new String[] {"Kuangdi"}, 	//menu path
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
	

	@Override
	public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
		sharedSpace.setSimilarityType(contextSimilarityType, objectSimilarityType);
		contextSimilarityCalculator = sharedSpace.getContextCalculator();
		this.matchList = sharedSpace.getMatchList();
		matchList.clear();
		System.gc();
		System.out.println("\n------\nStart ValidationPlugIn ...\n------");
		
		Queue<Feature> queue = new LinkedList<Feature>(); // a queue storing matches waiting to be validated
		Random random = new  Random();
		int numFeatures = matchList.numberOfFeatures();
		if (numFeatures < 0) {
			System.out.println("Feature numbers in MatchList are not aligned");
			return;
		}
		Feature startingFeature = matchList.getSourceFeatureByIndex(random.nextInt(numFeatures));
		queue.offer(startingFeature);
		matchList.setAsInQueue(startingFeature);
		
		final List<Feature> sourceFeatures = sharedSpace.getSourceLayer().getFeatureCollectionWrapper().getFeatures();
		final List<Feature> targetFeatures = sharedSpace.getTargetLayer().getFeatureCollectionWrapper().getFeatures();
		supportingRelations = new SupportingRelations();
		sharedSpace.storeSupportingRelations(supportingRelations);
		contextSimilarityCalculator.refreshSupportingRelation();
		int matchCount = 0;
				
		///////////////////////////////////////////
		// Validate Existing Matches
		///////////////////////////////////////////
		
		while (!queue.isEmpty()) {
			// get the object from the match being checked
			Feature sourceFeature = queue.poll();
			matchCount++;
			
			// find the surrounding matches
			ArrayList<Feature> sourceSurr = findSurroundingMatch(sourceFeature, sourceFeatures, queue, true);
			
			// calculate context similarity
			double contextSimilarity = calContextSimilarity(sourceFeature, matchList.getMatchedTargetFeature(sourceFeature), sourceSurr, false);
			matchList.setContextSimilarity(sourceFeature, contextSimilarity);
			// backtrack if the match is considered as invalid
			if (contextSimilarity >= VALID_THRESHOLD) {
				matchList.setAsValid(sourceFeature);
			} else {
				matchList.setAsInvalid(sourceFeature);
				backtrack(sourceFeature);
			}
			
		}
		
		///////////////////////////////////////////
		// Detect Omitted Matches
		///////////////////////////////////////////
		ArrayList<Feature> unmatchedSourceFeatures = matchList.getUnmatchedSourceFeatures();
		ArrayList<Feature> unmatchedTargetFeatures = matchList.getUnmatchedTargetFeatures();

		// went through single objects in source layer
		for (Feature singleF : unmatchedSourceFeatures) {
			ArrayList<Feature> tempMatch = new ArrayList<Feature>();
			Geometry buffer = singleF.getGeometry();
			// find potential match object from target matched list
			for (Feature f : targetFeatures) {
				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
					tempMatch.add(f);
			}
			// find potential match object from single target layer objects
			for (Feature f : matchList.getUnmatchedTargetFeatures()) {
				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
					tempMatch.add(f);
			}
			// check the similarity with each potential match object
			for (Feature potentialMatchedObject : tempMatch) {
				ArrayList<Feature> sourceSurr = findSurroundingMatch(singleF, sourceFeatures, null, false);
				// calculate context similarity
				double contextSimilarity = calContextSimilarity(singleF, potentialMatchedObject, sourceSurr, false);
				double confidenceLevel = contextSimilarity;
				if (confidenceLevel < VALID_THRESHOLD) {
					// confidenceLevel + object similarity
				}
				if (confidenceLevel >= VALID_THRESHOLD) {
					matchList.storeMatch(singleF, potentialMatchedObject);
					calContextSimilarity(singleF, matchList.getMatchedTargetFeature(singleF), sourceSurr, true);
					matchList.setContextSimilarity(singleF, contextSimilarity);
					matchList.setAsNew(singleF);
				}
			}
		}
		// went through single objects in target layer
//		for (Feature singleF : unmatchedTargetFeatures) {
//			ArrayList<Feature> tempMatch = new ArrayList<Feature>();
//			Geometry buffer = singleF.getGeometry();
//			// find potential match object from source matched list
//			for (Feature f : sourceFeatures) {
//				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
//					tempMatch.add(f);
//			}
//			// find potential match object from single source layer objects
//			for (Feature f : matchList.getUnmatchedSourceFeatures()) {
//				if (buffer.intersects(f.getGeometry()) && !tempMatch.contains(f))
//					tempMatch.add(f);
//			}
//			// check the similarity with each potential match object
//			for (Feature f : tempMatch) {
//				ArrayList<Feature> targetSurr = findSurroundingMatch(singleF, targetFeatures, null, false);
//				// calculate context similarity
//				double contextSimilarity = calContextSimilarity(singleF, matchList.getMatchedTargetFeature(singleF), targetSurr, false);
//				double confidenceLevel = contextSimilarity;
//				if (confidenceLevel < VALID_THRESHOLD) {
//					// confidenceLevel + object similarity
//				}
//				if (confidenceLevel >= VALID_THRESHOLD) {
//					calContextSimilarity(singleF, matchList.getMatchedTargetFeature(singleF), sourceSurr, true);
//					matchList.setContextSimilarity(singleF, contextSimilarity);
//					matchList.setAsNew(singleF);
//				}
//			}
//		}
		
		showResult(context);
		System.out.println("Validation Finished \n");
	}
	
	/**
	 * 
	 * @param sourceFeature
	 * @param sourceFeatures
	 * @param queue
	 * @param recordProcess false: the buffer radius & supporting relation will not be recorded (used in testing potential matches for single objects)
	 * @return
	 */
	private ArrayList<Feature> findSurroundingMatch(Feature sourceFeature, List<Feature> sourceFeatures, Queue<Feature> queue, boolean recordProcess) {
		// create a buffer of center object
		Geometry sfGeom = sourceFeature.getGeometry();
		Point sfCentroid =  sfGeom.getCentroid();
		ArrayList<Feature> sourceSurr = new ArrayList<Feature>();
		
		double radius = sfGeom.getLength() / 4 / 4;
//					double radius = Math.pow(sfGeom.getArea(), 1/2) / 4; // when area < 1, square root of it will become bigger
		Geometry buffer = sfGeom.buffer(radius);
		while (sourceSurr.size() < MIN_SURR_OBJ_NEEDED) {
			sourceSurr.clear();
			buffer = sfGeom.buffer(radius);
//						Geometry buffer = sfCentroid.buffer(radius);
	        // create lists to contain the surrounding objects
			for (Feature f : sourceFeatures) {
				if (buffer.intersects(f.getGeometry()) && f != sourceFeature) {
					sourceSurr.add(f);
				}
			}
			radius = radius * BUFFER_INC_RATE;
		}
		if (recordProcess) {
			matchList.setBufferRadius(sourceFeature, radius);
		}
		// add surrounding features (matches) into queue
		if (queue != null) {
			for (Feature f : sourceSurr) {
				if (matchList.shouldBeQueued(f)) {
					queue.add(f);
					matchList.setAsInQueue(f);
				}
			}
		}
		
		// display the surrounding objects in a new layer
//					showSurrObjectsInLayer(context, sourceFeature, buffer, sourceSurr);
		
		// record the dependency
		if (recordProcess) {
			supportingRelations.addSupportingRelation(sourceFeature, sourceSurr);
		}
		return sourceSurr;
	}
	
	private void backtrack(Feature invalidFeature) {
		System.out.println("--BackTrack: " + invalidFeature.getID() + "--");
//		if (true) {
//			return;
//		}
		Queue<Feature> currentLayerQueue = new LinkedList<Feature>();
		currentLayerQueue.offer(invalidFeature);
		int layerCount = 0;
		
		ArrayList<Feature> previousLayerFeatures = new ArrayList<Feature>();// the features have been recalculated in this backtrack, no need to be changed again
		
		while (!currentLayerQueue.isEmpty()) {
			System.out.println("layer " + layerCount);
			layerCount++;
			Queue<Feature> nextLayerQueue = new LinkedList<Feature>();
			
			// the following loop go through the recalculation of one layer
			while (!currentLayerQueue.isEmpty()) {
				Feature cFeature = currentLayerQueue.poll();
				previousLayerFeatures.add(cFeature);
				System.out.println("\tChecking the matches " + cFeature.getID() + " supports");
				// get the matches the invalid match supports / influenced by the invalid match
				ArrayList<Feature> supports = supportingRelations.getFeaturesSupportedBy(cFeature);
				if (supports == null) {
					System.out.println("No supporting match found for id = " + cFeature.getID());
				} else {
					// recalculate the context similarity for the features which use the current invalid feature as supporting match
					for (Feature f : supports) {
						if (matchList.isInvalid(f)) { // not recalculate the already invalid matches
							continue;
						}
						if (previousLayerFeatures.contains(f) || currentLayerQueue.contains(f) || nextLayerQueue.contains(f)) {
							// in previous layers              | not influence same layer       | has already marked as invalid in next layer
							continue;
						}
						double preCS = matchList.getContextSimilarity(f); // context similarity
						double preOS = matchList.getObjectSimilarity(f); // object similarity
						double preCL = matchList.getContextSimilarity(f); // confidence level
						double contextSimilarity = calContextSimilarity(f, matchList.getMatchedTargetFeature(f), null, true);
						matchList.setContextSimilarity(f, contextSimilarity);
						if (matchList.getConfidenceLevel(f) < VALID_THRESHOLD) {
							System.out.println(String.format("\t\t%d: (cs;os;cl) %.4f;%.4f;%.4f --> %.4f;%.4f;%.4f", f.getID(), preCS, preOS, preCL, matchList.getContextSimilarity(f), matchList.getObjectSimilarity(f), matchList.getConfidenceLevel(f)));
							matchList.setAsInvalid(f);
							nextLayerQueue.add(f);
						} else {
							System.out.println(String.format("\t\t%d: %.4f --> %.4f", f.getID(), preCS, matchList.getContextSimilarity(f)));
						}
					}
				}
			}
			currentLayerQueue = nextLayerQueue;
		}
	}
	
	
	private double calContextSimilarity(Feature sourceFeature, Feature targetFeature, ArrayList<Feature> sourceSurr, boolean isBackTrack) {
		return contextSimilarityCalculator.calContextSimilarity(sourceFeature, targetFeature, sourceSurr, isBackTrack);
	}
	
	
	private double calObjectSimilarity(Feature f) {
		return 0.0;
	}
	
	
	private void showSurrObjectsInLayer(PlugInContext context, Feature centerFeature, Geometry buffer, ArrayList<Feature> surrList) {
		FeatureCollection surrColl = null;
		FeatureSchema fs = centerFeature.getSchema();
		surrColl = new FeatureDataset(fs);
		
		Feature centroid = centerFeature.clone(false);
		centroid.setGeometry(centerFeature.getGeometry().getCentroid());
		surrColl.add(centroid);
		
        Feature bufferFeature = centerFeature.clone(false);
        bufferFeature.setGeometry(buffer);
        surrColl.add(bufferFeature);
		
		for (Feature f : surrList) {
			surrColl.add(f.clone(false)); 
			
			Feature centerPoint = f.clone(false);
			centerPoint.setGeometry(f.getGeometry().getCentroid());
			surrColl.add(centerPoint);
		}
		
		context.addLayer(StandardCategoryNames.WORKING, "Surr " + centerFeature.getID(), surrColl);
		
	}
	
	
	private void showResult(PlugInContext context) {		
		Pair<FeatureCollection, FeatureCollection> pair = matchList.getValidationResult();
		context.addLayer(StandardCategoryNames.WORKING, "Valid Matches", pair.getKey());
		context.addLayer(StandardCategoryNames.WORKING, "Invalid Matches", pair.getValue());
		Pair<FeatureCollection, FeatureCollection> npair = matchList.getNewMatches();
		context.addLayer(StandardCategoryNames.WORKING, "New Matches -- source layer", npair.getKey());
		context.addLayer(StandardCategoryNames.WORKING, "New Matches -- target layer", npair.getValue());
	}

}
