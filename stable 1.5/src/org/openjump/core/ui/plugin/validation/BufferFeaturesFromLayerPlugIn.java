package org.openjump.core.ui.plugin.validation;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

/**
 * @description:
 *	Example plugin which creates buffers from the input
 *  features. The Plugin ask for the layer with the features
 *  and the radius for the buffer. The output will be 
 *  the buffered features containing the attributes from the
 *  source features.
 *     
 * @author sstein
 *
 **/
public class BufferFeaturesFromLayerPlugIn extends AbstractPlugIn implements ThreadedPlugIn{

    private String T1 ="buffers all features in a layer";
    private String T2 ="buffer radius";
    private String CLAYER = "select layer";    
    private Layer itemlayer = null;
    private double radius = 0; 

    /**
     * this method is called on the startup by JUMP/OpenJUMP.
     * We set here the menu entry for calling the function.
     */
    public void initialize(PlugInContext context) throws Exception {
    				
	    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
	    featureInstaller.addMainMenuItem(
	    	        this,				//exe
	                new String[] {"SSTools"}, 	//menu path
	                this.getName(), //name methode .getName recieved by AbstractPlugIn 
	                false,			//checkbox
	                null,			//icon
	                createEnableCheck(context.getWorkbenchContext())); //enable check
    }
    
    /**
     * This method is used to define when the menu entry is activated or
     * disabled. In this example we allow the menu entry to be usable only
     * if one layer exists.
     * @param workbenchContext
     * @return
     */
    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck()
                        .add(checkFactory.createAtLeastNLayersMustExistCheck(1))
                        .add(checkFactory.createTaskWindowMustBeActiveCheck());
    }
    
    /**
     * this function is called by JUMP/OpenJUMP if one clicks on the menu entry.
     * It is called before the "run" method and useful to do all the GUI /user-input things
     * In this example we call two additional methods {@link #setDialogValues(MultiInputDialog, PlugInContext)}
     * and {@link #getDialogValues(MultiInputDialog)} to obtain the Layer and the buffer radius by the user. 
     */
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
    	JComboBox addLayerComboBoxBuild = dialog.addLayerComboBox(this.CLAYER, context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addDoubleField(T2, this.radius, 6);
	  }

    private void getDialogValues(MultiInputDialog dialog){
    	this.itemlayer = dialog.getLayer(this.CLAYER);
        this.radius = dialog.getDouble(T2);
	  }
	
    /**
     * This method is called by JUMP/OpenJUMP after {@link #execute(PlugInContext)},
     * It is usefull to do some longterm calculations.  
     */
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
            //-- allow cancel
            monitor.allowCancellationRequests();
    	    this.bufferItems(context, monitor, this.radius);
    	    System.gc();    		
    	}

     /**
     * This is a method which calculates buffers from the items in a layer. 
     * @param context
     * @param monitor 
     * @param radius used for the buffer
     * @return
     * @throws Exception
     */
     private boolean bufferItems(PlugInContext context, TaskMonitor monitor, double radius) throws Exception{
	System.gc(); //flush garbage collector
	// --------------------------	    
	//-- get items
        final Collection features = this.itemlayer.getFeatureCollectionWrapper().getFeatures();
        //-- create the FeatureCollection which will contain the buffers
	    FeatureCollection myCollA = null;
        // get the first feature to determine the FeatureSchema of the collection
        Feature firstF = (Feature)features.iterator().next();
        // get FeatureSchema
        FeatureSchema fs = firstF.getSchema();
        // create the new FeatureCollection (containing the output of the buffer operation)
        myCollA = new FeatureDataset(fs);
        // get every feature(clone it!) - get the geometry - calculate the buffer
        // and set as new Geometry the buffer geometry
        // so the buffers have the same attributes as the original features
        int numFeatures = features.size();
        int count=0;
	    for (Iterator iter = features.iterator(); iter.hasNext();) {
            Feature f = (Feature) iter.next();
            //-- clone the feature - so we can add it to a new FC
            Feature newF = f.clone(false); 
            Geometry geom = f.getGeometry();
            //-- calculate the buffer
            Geometry buffer = geom.buffer(radius);
            //-- set as new geometry the buffer geometry
            newF.setGeometry(buffer);
            //-- add the new (cloned) feature to the collection 
            myCollA.add(newF);
            count++;
            //-- give the user some info : how far the calculation is
            monitor.report("processed: "+count+" of " + numFeatures + " items");
            //-- stop here if the user whishes
            //   we will not display the buffers calculated so far
            if (monitor.isCancelRequested()){
                return false;
            }
        }
        //-- display the resulting buffer features
		context.addLayer(StandardCategoryNames.WORKING, "buffers", myCollA);	    	
		return true;        
	}	  	
}