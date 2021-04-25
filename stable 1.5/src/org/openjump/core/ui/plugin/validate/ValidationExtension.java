package org.openjump.core.ui.plugin.validate;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * This extension class is used to load the plug-ins into the OpenJump
 * @author Guangdi Hu
 *
 */
public class ValidationExtension extends Extension {

    public String getName() {
        return "Validation Extension (Guangdi Hu)";
    }

    public String getVersion() {
        return "0.1";
    }

    public void configure(PlugInContext context) throws Exception {
        new ValidatePlugIn().initialize(context);
        new ScrutinizeValidationPlugIn().initialize(context);
    }

}