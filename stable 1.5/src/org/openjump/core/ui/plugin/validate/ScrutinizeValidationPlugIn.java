package org.openjump.core.ui.plugin.validate;

import java.util.ArrayList;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.contextcalculator.AbstractContextCalculator;
import org.openjump.core.ui.plugin.validate.contextcalculator.StarContextCalculator;
import org.openjump.core.ui.plugin.validate.pojo.AntiClockwiseSequence;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;

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
import com.vividsolutions.jump.workbench.ui.AboutDialog;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

/**
 * This plugIn should be executed after the validation process
 * This plugIn will show the detail process of the validation, and explanation on the validation result to promote user understanding
 * @author Guangdi Hu
 *
 */
public class ScrutinizeValidationPlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	private String T1 = "Please enter the ID of a source layer object to scrutinize its validation process";
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
	                "Scrutinize",
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
        dialog.addPositiveIntegerField(T3, this.featureID, 6);
	  }

    private void getDialogValues(MultiInputDialog dialog){
        this.featureID = dialog.getInteger(T3);
	  }
	

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
        //-- allow cancel
        monitor.allowCancellationRequests();
	    System.gc();
	    matchList = sharedSpace.getMatchList();
	    
	    String result = new String();
	    result += "<html>";
	    
	    System.out.println("\n\n");

		// create a buffer surrounding the being checked feature
	    
		Feature sourceFeature = matchList.getSourceFeatureByID(featureID);
		if (sourceFeature == null) {
			result += "No feature of ID = " + featureID + " can be found</html>";
			showResult(context, result);
			return;
		}
		
		result += "---------------- Scrutinizing the validation process for id = " + sourceFeature.getID() + " --------------<br>";
		result += "Validation Result: " + (matchList.getConfidenceLevel(sourceFeature)>=matchList.getValidThreshold()?"Valid<br>":"Invalid<br>");
		result += "Confidence Level: " + String.format("%.4f", matchList.getConfidenceLevel(sourceFeature)) + " ( threshold: " + matchList.getValidThreshold() + " )<br>";
		result += "\tContext Similarity: " + String.format("%.4f", matchList.getContextSimilarity(sourceFeature)) + " ( weight: " + String.format("%.2f", matchList.getContextWeight()) + " )<br>"; 
		result += "\tObject Similarity: " + String.format("%.4f", matchList.getObjectSimilarity(sourceFeature)) + " ( weight: " + String.format("%.2f", 1-matchList.getContextWeight()) + " )<br></html>"; 
		result += "Details: ";
		Geometry sfGeom = sourceFeature.getGeometry();
		Geometry buffer = sfGeom.buffer(matchList.getBufferRadius(sourceFeature));
		
		// create a feature collection to visualize surrounding features in a new layer 
		FeatureCollection surrColl = null; // the feature collection of surrounding objects of a source layer feature
        FeatureSchema fs = sourceFeature.getSchema();
        surrColl = new FeatureDataset(fs);
        
        // add centroid of being checked match into collection
        Feature centroid = sourceFeature.clone(false);
        centroid.setGeometry(sourceFeature.getGeometry().getCentroid());
		surrColl.add(centroid);
		
        // add the buffer into feature collection
        Feature bufferFeature = sourceFeature.clone(false);
        bufferFeature.setGeometry(buffer);
//        surrColl.add(bufferFeature);
        
        // create lists to contain the surrounding objects in source layer
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
        }
        context.addLayer(StandardCategoryNames.WORKING, "check" + sourceFeature.getID(), surrColl);			

        // create list to contain the target objects from invalid matches
//        StarContextCalculator contextCalculator = new StarContextCalculator(sharedSpace.STAR_DEGREE_RANGE);
        AbstractContextCalculator contextCalculator = sharedSpace.getContextCalculator();
        contextCalculator.checkContextSimilarity(sourceFeature, matchList.getMatchedTargetFeature(sourceFeature), sourceSurr);
        ArrayList<Feature> invalidMatches = sharedSpace.getInvalidSurrMatchList().getValue();

        surrColl = new FeatureDataset(fs);
        for (Feature f : invalidMatches) {
        	surrColl.add(f.clone(false)); 
			Feature cp = f.clone(false);
			cp.setGeometry(f.getGeometry().getCentroid());
			surrColl.add(cp);
        }

        Feature targetFeature = matchList.getMatchedTargetFeature(sourceFeature);
        Feature targetCentroid = targetFeature.clone(false);
        targetCentroid.setGeometry(targetFeature.getGeometry().getCentroid());
        surrColl.add(targetCentroid); // add centroid of target object
        context.addLayer(StandardCategoryNames.WORKING, "invalid surr " + sourceFeature.getID(), surrColl);	
		
		
		System.out.println("done\n");
		showResult(context, result);
		
	}
    
    private void showResult(PlugInContext context, String info) {

    	MultiInputDialog dialog = new MultiInputDialog(
	            context.getWorkbenchFrame(), getName(), true);
		dialog.addLabel(info);
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
    }

}
