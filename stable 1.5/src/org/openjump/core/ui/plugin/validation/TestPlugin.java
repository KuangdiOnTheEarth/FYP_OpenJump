package org.openjump.core.ui.plugin.validation;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class TestPlugin extends AbstractUiPlugIn {
	public TestPlugin() {}
    private String sName = "testPlugin";
    
    public void initialize(PlugInContext context) throws Exception {
    	sName = I18N.get("org.openjump.core.ui.plugin.validation.TestPlugin");
    	
    	context.getFeatureInstaller().addMainMenuItem(this,
    			new String[] { "Validation", "Test" }, getName(), false, null, null);
    	
//    	WorkbenchContext workbenchContext = context.getWorkbenchContext();
//    	FeatureInstaller featureInstaller = new FeatureInstaller(workbenchContext);
//
//        // Add File Menu
//        featureInstaller.addMainMenuItem(new String[] {
//          MenuNames.FILE
//        }, this, 7);
    }
    
    public boolean execute(PlugInContext context) throws Exception { 
    	context.getWorkbenchFrame().getOutputFrame().createNewDocument(); 
    	context.getWorkbenchFrame().getOutputFrame().addText("Hello, World!"); 
    	context.getWorkbenchFrame().getOutputFrame().surface();
    	return true;
    }
    
    public ImageIcon getIcon() {
        //return IconLoaderFamFam.icon("arrow_undo.png");
        return IconLoader.icon("Undo.gif");
    }
    
    @Override
    public String getName() {
    	return sName;
    }
}
