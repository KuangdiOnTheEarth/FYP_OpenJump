package org.openjump.core.ui.plugin.validate;

import java.util.HashSet;
import java.util.List;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.core.ui.plugin.validate.pojo.MatchList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class ValidatePlugIn extends AbstractUiPlugIn implements ThreadedPlugIn {
	
	private SharedSpace sharedSpace;
	
	
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
		System.gc();
		MatchList matchList = sharedSpace.getMatchList();
		if (matchList == null) {
			System.out.println("--ValidatePlugIn-- MatchList is null");
		} else {
	        int[] indices = getRandomArray(10, 100);
	        for (int i = 0; i < 10; i++) {
	        	System.out.println(matchList.getSourceFeatureByIndex(indices[i]).getID() + " -- " + matchList.getMatchedTargetFeature(matchList.getSourceFeatureByIndex(indices[i])).getID() );
	        }
		}
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
