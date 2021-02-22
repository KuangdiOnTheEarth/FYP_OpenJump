package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;

import com.vividsolutions.jts.geom.Geometry;
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
			Feature sourceFeature = matchList.getSourceFeatureByIndex(queue.poll());
			Geometry sfGeom = sourceFeature.getGeometry();
			Point sfCentroid =  sfGeom.getCentroid();
			Geometry buffer = sfCentroid.buffer(bufferRadius);
			
			FeatureCollection surrColl = null; // the feature collection of surrounding objects of a source layer feature
	        FeatureSchema fs = sourceFeature.getSchema();
	        surrColl = new FeatureDataset(fs);
	        
	        Feature bufferFeature = sourceFeature.clone(false);
	        bufferFeature.setGeometry(buffer);
	        surrColl.add(bufferFeature);
	        
			for (Feature f : sourceFeatures) {
				if (buffer.overlaps(f.getGeometry())) {
					surrColl.add(f.clone(false));
				}
			}
			
			context.addLayer(StandardCategoryNames.WORKING, "surrounding", surrColl);
			System.out.println(selfMark + "new layer: surrounding objects of " + sourceFeature.getID());
		}
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
	 * order the surrounding features (or their corresponding features in target layer) clockwise
	 * @param surroundingFeatures
	 * @param center
	 * @return
	 */
	private ArrayList<Feature> orderFeaturesClockwise(ArrayList<Feature> surroundingFeatures, Feature center) {
		ArrayList<Feature> orderedFeatures = new ArrayList<Feature>();
		
		// TODO
		
		return orderedFeatures;
	}
	
	
	/**
	 * compare the similarity of two queues of ordered features, the return value is used to calculate the confidence level
	 * @param fs1
	 * @param fs2
	 * @return
	 */
	private int compareOrderedFeatures(ArrayList<Feature> fs1, ArrayList<Feature> fs2) {
		int difference = 0;
		
		// TODO
		
		return difference;
	}
	
}
