/*
 * (C) 2011 Micha&euml;l Michaud
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * michael.michaud@free.fr
 *
 */

package org.openjump.core.ui.plugin.match;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * Extension containing matching processing also known as join.
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */ 
// History
// 0.5.2    (2012-01-03) : small fixes in i18n
// 0.5.1    (2011-12-04) : small fix in i18n
// 0.5      (2011-12-01) : initial version
public class MatchingExtension extends Extension {

    public String getName() {
        return "Matching Extension (Micha\u00EBl Michaud)";
    }

    public String getVersion() {
        return "0.5.2 (2012-01-03)";
    }

    public void configure(PlugInContext context) throws Exception {
        new MatchingPlugIn().initialize(context);
    }

}