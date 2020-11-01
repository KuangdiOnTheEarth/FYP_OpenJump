package org.openjump.core.ui.plugin.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class RandomBuffer extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	private String T1 ="buffers all features in a layer";
    private String T2 ="buffer radius";
    private String T3 ="object number";
    private String CLAYER = "select layer";    
    private Layer itemlayer = null;
    private double radius = 0;
    private int objNum = 1;

    /**
     * this method is called on the startup by JUMP/OpenJUMP.
     * We set here the menu entry for calling the function.
     */
    public void initialize(PlugInContext context) throws Exception {
    				
	    FeatureInstaller featureInstaller = new FeatureInstaller(context.getWorkbenchContext());
	    featureInstaller.addMainMenuItem(
	    	        this,				//exe
	                new String[] {"Kuangdi"}, 	//menu path
	                this.getName(), //name methode .getName recieved by AbstractPlugIn 
	                false,			//checkbox
	                null,			//icon
	                createEnableCheck(context.getWorkbenchContext())); //enable check
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

        return new MultiEnableCheck()
                        .add(checkFactory.createAtLeastNLayersMustExistCheck(1))
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
    	JComboBox addLayerComboBoxBuild = dialog.addLayerComboBox(this.CLAYER, context.getCandidateLayer(0), null, context.getLayerManager());
        dialog.addDoubleField(T2, this.radius, 6);
        dialog.addPositiveIntegerField(T3, this.objNum, 6);
	  }

    private void getDialogValues(MultiInputDialog dialog){
    	this.itemlayer = dialog.getLayer(this.CLAYER);
        this.radius = dialog.getDouble(T2);
        this.objNum = dialog.getInteger(T3);
	  }
	

    public void run(TaskMonitor monitor, PlugInContext context) throws Exception{
            //-- allow cancel
            monitor.allowCancellationRequests();
    	    this.bufferItems(context, monitor, this.radius, this.objNum);
    	    System.gc();    		
    	}

     private boolean bufferItems(PlugInContext context, TaskMonitor monitor, double radius, int number) throws Exception{
	    System.gc();
        final List features = this.itemlayer.getFeatureCollectionWrapper().getFeatures();
	    FeatureCollection myCollA = null;
        Feature firstF = (Feature)features.iterator().next();
        FeatureSchema fs = firstF.getSchema();
        myCollA = new FeatureDataset(fs);

        int numFeatures = features.size();
        int count=0;
        
        int n = Math.min(number, numFeatures);
        int[] indices = getRandomArray(n, numFeatures);
        
        for (int i = 0; i < n; i++) {
        	Feature f = (Feature)features.get(indices[i]);
        	Feature newF = f.clone(false); 
            Geometry geom = f.getGeometry();
            Geometry buffer = geom.buffer(radius);
            newF.setGeometry(buffer);
            myCollA.add(newF);
            count++;
            monitor.report("processed: "+count+" of " + numFeatures + " items");
            if (monitor.isCancelRequested()){
                return false;
            }
        }

		context.addLayer(StandardCategoryNames.WORKING, "buffers", myCollA);	    	
		return true;        
	}	  	
     
     private int[] getRandomArray(int n, int max) {
    	 int arr[] = new int[n];
    	 int min = 0;
    	 HashSet<Integer> set = new HashSet<Integer>();
    	 while (set.size() < n) {
    		 int num = (int) (Math.random() * (max - min)) + min;
    		 set.add(num);
    	 }
    	 int i = 0;
    	 for (Integer val : set) arr[i++] = val;
    	 return arr;
     }
     
}
