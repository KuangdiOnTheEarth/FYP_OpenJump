package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class ValidatePlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	private SharedSpace sharedSpace;
	private MatchList matchList;
	
	private final double bufferRadius = 0.5;
	
	private final String selfMark = "--ValidatePlugin-- ";
	
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
		this.matchList = sharedSpace.getMatchList();
		System.gc();
		
		Queue<Integer> queue = new LinkedList<Integer>(); // a queue storing the index of matches waiting to be validated
		Random random = new  Random();
		int numFeatures = matchList.numberOfFeatures();
		if (numFeatures < 0) {
			System.out.println(selfMark + "Feature numbers in MatchList are not aligned");
			return;
		}
		int startingIndex = random.nextInt(numFeatures);
		queue.offer(startingIndex);
		matchList.setAsInQueue(startingIndex);
		
		final List<Feature> sourceFeatures = sharedSpace.getSourceLayer().getFeatureCollectionWrapper().getFeatures();
		final List<Feature> targetFeatures = sharedSpace.getTargetLayer().getFeatureCollectionWrapper().getFeatures();
		
		while (!queue.isEmpty()) {
			// create a buffer surrounding the being checked feature
			Feature sourceFeature = matchList.getSourceFeatureByIndex(queue.poll());
			
			double contextSimilarity = calContextSimilarityFor(sourceFeature, sourceFeatures, targetFeatures, true, context);
			matchList.setContextSimilarity(sourceFeature, contextSimilarity);
			
			
			// update the queue: add new matches into the queue and find the next match of being checked
			
			
			System.out.println("\n");
		}
	}
	
	
	/**
	 * calculate the context similarity of a match
	 * @param sourceFeature a feature from source layer, whose match will be checked
	 * @param visible set as 'true' then the surrounding object will be visualized in a new layer
	 * @return context similarity
	 */
	private double calContextSimilarityFor(Feature sourceFeature, List<Feature> sourceFeatures, List<Feature> targetFeatures, boolean visible, PlugInContext context) {
		Geometry sfGeom = sourceFeature.getGeometry();
		Point sfCentroid =  sfGeom.getCentroid();
		Geometry buffer = sfCentroid.buffer(bufferRadius);
		
        // create lists to contain the surrounding objects
        ArrayList<Feature> sourceSurr = new ArrayList<Feature>();	    
        
		for (Feature f : sourceFeatures) {
			if (buffer.intersects(f.getGeometry()) && f != sourceFeature) {
				sourceSurr.add(f);
			}
		}
		
		
		AntiClockwiseSequence sourceSeq = orderFeaturesClockwise(sourceSurr, sourceFeature);
		AntiClockwiseSequence targetSeq = orderFeaturesClockwise(findCorrespondingFeatures(sourceSurr), matchList.getMatchedTargetFeature(sourceFeature));
		
		if (visible) {
			showSurrObjectsInLayer(context, matchList.getMatchedTargetFeature(sourceFeature), buffer, sourceSurr);
		}
		
		System.out.print("Context Similarity:" + sourceFeature.getID() + " & " + matchList.getMatchedTargetFeature(sourceFeature).getID() + ": ");
		return compareOrderedFeatures(sourceSeq, targetSeq);
	}
	
	
	/**
	 * Display the surrounding objects of a center objects (in target layer by default) in a new layer
	 * @param context
	 * @param centerFeature
	 * @param buffer
	 * @param surrList
	 */
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


	/**
	 * find the corresponding features (objects in target layer) of the surrounding objects of the being checked object in source layer
	 * @param sourceFeatures
	 * @return
	 */
	private ArrayList<Feature> findCorrespondingFeatures(ArrayList<Feature> sourceFeatures) {
		ArrayList<Feature> targetFeatures = new ArrayList<Feature>();
		for (Feature f : sourceFeatures) {
			targetFeatures.add(matchList.getMatchedTargetFeature(f));
		}
		return targetFeatures;
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
			Point fCentroid = f.getGeometry().getCentroid();
			Double fX = fCentroid.getX();
			Double fY = fCentroid.getY();

			Double xDiff = fX - cX;
			Double yDiff = fY - cY;
			Double sin = yDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			Double cos = xDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			RelativePosition p = new RelativePosition(f, sin, cos);
			sequence.add(p);
		}
		return sequence;
	}
	
	
	/**
	 * compare the similarity of two queues of ordered features, the return value is used to calculate the confidence level
	 * @param fs1
	 * @param fs2
	 * @return
	 */
	private double compareOrderedFeatures(AntiClockwiseSequence fs1, AntiClockwiseSequence fs2) {
		return fs1.calContextSimilarityWith(fs2);
	}
	
}
