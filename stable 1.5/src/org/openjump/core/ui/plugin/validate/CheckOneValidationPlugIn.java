package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComboBox;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;
import org.openjump.core.ui.plugin.validate.pojo.RelativePosition;
import org.openjump.core.ui.plugin.validate.pojo.SupportingRelations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class CheckOneValidationPlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	
	private String T1 = "enter the id of a feature from the match to check the context (surrounding objects) used in its validation";
    private String T2 = "select layer";
    private String T3 = "object ID";

    private Layer itemlayer = null;
    private int featureID = 0;
    
	
	private SharedSpace sharedSpace;
	private MatchList matchList;
	
	
	public void initialize(PlugInContext context) throws Exception {
	    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
	    featureInstaller.addMainMenuItem(
	    	        this,
	                new String[] {"Kuangdi"}, 	//menu path
	                "Check Validation",
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
	    dialog.setSideBarDescription(T1);
//    	JComboBox addLayerComboBoxBuild = dialog.addLayerComboBox(T2, context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addPositiveIntegerField(T3, this.featureID, 6);
	  }

    private void getDialogValues(MultiInputDialog dialog){
//    	this.itemlayer = dialog.getLayer(this.T2);
        this.featureID = dialog.getInteger(T3);
	  }
	

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        //-- allow cancel
        monitor.allowCancellationRequests();
	    System.gc();
	    matchList = sharedSpace.getMatchList();

		// create a buffer surrounding the being checked feature
		Feature sourceFeature = matchList.getSourceFeatureByID(featureID);
		if (sourceFeature == null) {
			System.out.println("No feature of ID = " + featureID + " can be found");
		}
		Geometry sfGeom = sourceFeature.getGeometry();
		Point sfCentroid =  sfGeom.getCentroid();
		Geometry buffer = sfCentroid.buffer(matchList.getBufferRadius(sourceFeature));
		
		// create a feature collection to visualize surrounding features in a new layer 
		FeatureCollection surrColl = null; // the feature collection of surrounding objects of a source layer feature
        FeatureSchema fs = sourceFeature.getSchema();
        surrColl = new FeatureDataset(fs);
        // add the buffer into feature collection
        Feature bufferFeature = sourceFeature.clone(false);
        bufferFeature.setGeometry(buffer);
        surrColl.add(bufferFeature);
        
        // create lists to contain the surrounding objects
        ArrayList<Feature> sourceSurr = sharedSpace.getSupportingRelations().getSupportingFeaturesOf(sourceFeature);
        if (sourceSurr == null) {
        	System.out.println("No surruonding objects detected");
        	return;
        }
        for (Feature f : sourceSurr) {
        	surrColl.add(f.clone(false)); 
			Feature cp = f.clone(false);
			cp.setGeometry(f.getGeometry().getCentroid());
			surrColl.add(cp);
//				sourceSurr.add(f);
        }

		context.addLayer(StandardCategoryNames.WORKING, "check" + sourceFeature.getID(), surrColl);			
		AntiClockwiseSequence sourceSeq = orderFeaturesClockwise(sourceSurr, sourceFeature);
		AntiClockwiseSequence targetSeq = orderFeaturesClockwise(findCorrespondingFeatures(sourceSurr), matchList.getMatchedTargetFeature(sourceFeature));
		
		System.out.print("Context Similarity:" + sourceFeature.getID() + " & " + matchList.getMatchedTargetFeature(sourceFeature).getID() + ": ");
		double contextSimilarity = compareOrderedFeatures(sourceSeq, targetSeq);
		matchList.setContextSimilarity(sourceFeature, contextSimilarity);
		
		System.out.println("\n");
	    
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
//		System.out.println("Number of surrounding objects: " + surrFeatures.size());
		Point centerCentroid = center.getGeometry().getCentroid();
		Double cX = centerCentroid.getX(); 
		Double cY = centerCentroid.getY();
		
		AntiClockwiseSequence sequence = new AntiClockwiseSequence();
		for (Feature f : surrFeatures) {
//			System.out.println("Calculating RelativePosition of " + f.getID());
			Point fCentroid = f.getGeometry().getCentroid();
			Double fX = fCentroid.getX();
			Double fY = fCentroid.getY();

			Double xDiff = fX - cX;
			Double yDiff = fY - cY;
			Double sin = yDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
			Double cos = xDiff / Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5);
//			System.out.println(String.format("%d: xDiff: %.4f; yDiff: %.4f; line: %.4f; sin: %.4f; cos: %.4f", f.getID(), xDiff, yDiff, Math.pow( Math.pow(xDiff,2)+Math.pow(yDiff,2), 0.5), sin, cos));
			RelativePosition p = new RelativePosition(f, sin, cos);
			sequence.add(p);
//			System.out.println("Calculated RelativePosition of " + f.getID());
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
