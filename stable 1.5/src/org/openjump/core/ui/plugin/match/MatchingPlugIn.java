/*
Copyright (c) 2011, Micha&euml;l Michaud
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of its authors nor the names of its contributors may
      be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.openjump.core.ui.plugin.match;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.AttributeTypeFilter;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiTabInputDialog;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.RingVertexStyle;

import org.openjump.core.ui.plugin.match.matcher.*;
import org.openjump.core.ui.plugin.match.util.text.RuleRegistry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * PlugIn to find features from a layer matching features of another layer. 
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public class MatchingPlugIn extends ThreadedBasePlugIn {
    
    private final String MATCHING                     = I18NPlug.getI18N("Matching");
    private final String MATCHING_OPTIONS             = I18NPlug.getI18N("Matching-options");
    
    // Source layer
    private final String SOURCE_LAYER                 = I18NPlug.getI18N("Source-layer");
    private final String SOURCE_LAYER_TOOLTIP         = I18NPlug.getI18N("Source-layer-tooltip");
    private final String SINGLE_SOURCE                = I18NPlug.getI18N("Single-source");
    private final String SINGLE_SOURCE_TOOLTIP        = I18NPlug.getI18N("Single-source-tooltip");
    
    // Target layer
    private final String TARGET_LAYER                 = I18NPlug.getI18N("Target-layer");
    private final String TARGET_LAYER_TOOLTIP         = I18NPlug.getI18N("Target-layer-tooltip");
    private final String SINGLE_TARGET                = I18NPlug.getI18N("Single-target");
    private final String SINGLE_TARGET_TOOLTIP        = I18NPlug.getI18N("Single-target-tooltip");
    
    // Geometry matcher
    private final String GENERAL_OPTIONS              = I18NPlug.getI18N("General-options");
    private final String GEOMETRIC_OPTIONS            = I18NPlug.getI18N("Geometric-options");    
    private final String GEOMETRY_MATCHER             = I18NPlug.getI18N("Geometry-matcher");
    private final String MAXIMUM_DISTANCE             = I18NPlug.getI18N("Maximum-distance");
    private final String MINIMUM_OVERLAPPING          = I18NPlug.getI18N("Minimum-overlapping");
    
    // Output options
    private final String OUTPUT_OPTIONS               = I18NPlug.getI18N("Output-options");
    //private final String SELECT_MATCHING_FEATURES     = I18NPlug.getI18N("Select-matching-features");
    //private final String SELECT_NOT_MATCHING_FEATURES = I18NPlug.getI18N("Select-not-matching-features");
    private final String COPY_MATCHING_FEATURES       = I18NPlug.getI18N("Copy-matching-features");
    private final String COPY_NOT_MATCHING_FEATURES   = I18NPlug.getI18N("Copy-not-matching-features");
    private final String DISPLAY_LINKS                = I18NPlug.getI18N("Display-links"); 
    
    // Attributes options
    private final String ATTRIBUTE_OPTIONS            = I18NPlug.getI18N("Attribute-options");
    private final String USE_ATTRIBUTES               = I18NPlug.getI18N("Use-attributes");
    private final String SOURCE_LAYER_ATTRIBUTE       = I18NPlug.getI18N("Source-layer-attribute");
    private final String SOURCE_ATT_PREPROCESSING     = I18NPlug.getI18N("Source-att-preprocessing");
    private final String TARGET_LAYER_ATTRIBUTE       = I18NPlug.getI18N("Target-layer-attribute");
    private final String TARGET_ATT_PREPROCESSING     = I18NPlug.getI18N("Target-att-preprocessing");
    private final String ATTRIBUTE_MATCHER            = I18NPlug.getI18N("Attribute-matcher");
    private final String MAXIMUM_STRING_DISTANCE      = I18NPlug.getI18N("Maximum-string-distance");
    private final String MINIMUM_STRING_OVERLAPPING   = I18NPlug.getI18N("Minimum-string-overlapping");
    
    // Attribute transfer / aggregation
    private final String TRANSFER_OPTIONS               = I18NPlug.getI18N("Transfer-options");
    private final String TRANSFER_TO_REFERENCE_LAYER    = I18NPlug.getI18N("Transfer-to-reference-layer");
    private final String TRANSFER_BEST_MATCH_ONLY       = I18NPlug.getI18N("Transfer-best-match-only");
    
    private final String STRING_AGGREGATION             = I18NPlug.getI18N("String-aggregation");
    private final String INTEGER_AGGREGATION            = I18NPlug.getI18N("Integer-aggregation");
    private final String DOUBLE_AGGREGATION             = I18NPlug.getI18N("Double-aggregation");
    private final String DATE_AGGREGATION               = I18NPlug.getI18N("Date-aggregation");

    // Processing and Error messages
    private final String SEARCHING_MATCHES              = I18NPlug.getI18N("Searching-matches");
    private final String MISSING_INPUT_LAYER            = I18NPlug.getI18N("Missing-input-layer");
    private final String CHOOSE_MATCHER                 = I18NPlug.getI18N("Choose-geometry-or-attribute-matcher");
    private final String MISSING_DIRECTORY              = I18NPlug.getI18N("Missing-directory");
    
    // Parameters : source layer and cardinality
    private String source_layer_name;
    private boolean single_source = false;
    // Parameters : target layer and cardinality
    private String target_layer_name;
    private boolean single_target = false;
    // Parameters : geometry parameters
    private GeometryMatcher geometry_matcher = CentroidDistanceMatcher.instance();
    private double max_distance = geometry_matcher.getMaximumDistance();
    private boolean set_max_distance = !Double.isNaN(max_distance);
    private double min_overlapping = geometry_matcher.getMinimumOverlapping();
    private boolean set_min_overlapping = !Double.isNaN(min_overlapping);
    // Parameters : output options
    //private boolean select_matching_features;
    //private boolean select_not_matching_features;
    private boolean copy_matching_features = true;
    private boolean copy_not_matching_features;
    private boolean transfer_option;
    private boolean display_links = false;

    // Parameters : attribute parameters
    private boolean use_attributes = false;
    private String source_att_preprocessing = "";
    private String source_layer_attribute;
    private String target_att_preprocessing = "";
    private String target_layer_attribute;
    private StringMatcher attribute_matcher = 
        StringEqualityIgnoreCaseAndAccentMatcher.instance();
    private double max_string_distance = attribute_matcher.getMaximumDistance();
    private boolean set_max_string_distance = !Double.isNaN(max_string_distance);
    private double min_string_overlapping = attribute_matcher.getMinimumOverlapping();
    private boolean set_min_string_overlapping = !Double.isNaN(min_string_overlapping);

    // Parameters : transfer and aggregation
    private boolean transfer = true;
    private boolean transfer_best_match_only = false;
    private boolean ignore_null = true;
    private Aggregator<String> string_aggregator   = Aggregator.CONCATENATE_UNIQUE;
    private Aggregator<Integer> integer_aggregator = Aggregator.SUM_INTEGER;
    private Aggregator<Double> double_aggregator   = Aggregator.MEAN_DOUBLE;
    private Aggregator<Date> date_aggregator       = Aggregator.MEAN_DATE;
    
    public MatchingPlugIn() {
    }
    
    public String getName() {
        return MATCHING;
    }

    public void initialize(PlugInContext context) throws Exception {
        
        context.getFeatureInstaller().addMainMenuItem(
          this, new String[]{"Kuangdi"},
          MATCHING + "...",
          false, null, new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(1)));
    }

    /**
     * Execute method initialize the plugin interface and get all the
     * parameters from the user.
     */
    public boolean execute(PlugInContext context) throws Exception {
        
        try {
            RuleRegistry.loadRules(
                context.getWorkbenchContext().getWorkbench().getPlugInManager().getPlugInDirectory().getPath() + "\\Rules"
            );
        } catch (IllegalArgumentException iae) {
            context.getWorkbenchFrame().log(iae.getMessage());
            context.getWorkbenchFrame().warnUser(I18NPlug.getMessage("Missing-directory", 
                new String[]{
                    context.getWorkbenchContext().getWorkbench()
                    .getPlugInManager().getPlugInDirectory().getName() + 
                    "/Rules"
                }
            ));
        }
        ////////////////////////////////////////////////////////////////////////
        // UI : CREATE MULTITAB INPUT DIALOG
        ////////////////////////////////////////////////////////////////////////
                
        final MultiTabInputDialog dialog = new MultiTabInputDialog(
            context.getWorkbenchFrame(), MATCHING_OPTIONS, GEOMETRIC_OPTIONS, true);
        initDialog(dialog, context);

        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        
        if (dialog.wasOKPressed()) {
            // Get source layer parameters
            Layer source_layer = dialog.getLayer(SOURCE_LAYER);
            source_layer_name  = source_layer.getName();
            single_source      = dialog.getBoolean(SINGLE_SOURCE);
            // Get target layer parameters
            Layer target_layer = dialog.getLayer(TARGET_LAYER);
            target_layer_name  = target_layer.getName();
            single_target      = dialog.getBoolean(SINGLE_TARGET);
            // Get geometry matcher and set its parameters
            geometry_matcher   = (GeometryMatcher)MatcherRegistry
                                    .GEOMETRY_MATCHERS
                                    .get(dialog.getText(GEOMETRY_MATCHER));
            max_distance       = dialog.getDouble(MAXIMUM_DISTANCE);
            min_overlapping    = dialog.getDouble(MINIMUM_OVERLAPPING);
            geometry_matcher.setMaximumDistance(max_distance);
            geometry_matcher.setMinimumOverlapping(min_overlapping);
            
            // Get output options
            //select_matching_features     = dialog.getBoolean(SELECT_MATCHING_FEATURES);
            //select_not_matching_features = dialog.getBoolean(SELECT_NOT_MATCHING_FEATURES);
            copy_matching_features       = dialog.getBoolean(COPY_MATCHING_FEATURES);
            copy_not_matching_features   = dialog.getBoolean(COPY_NOT_MATCHING_FEATURES);
            display_links                = dialog.getBoolean(DISPLAY_LINKS);
            
            // get attribute options
            use_attributes               = dialog.getBoolean(USE_ATTRIBUTES);
            source_layer_attribute       = dialog.getText(SOURCE_LAYER_ATTRIBUTE);
            source_att_preprocessing     = dialog.getText(SOURCE_ATT_PREPROCESSING);
            target_layer_attribute       = dialog.getText(TARGET_LAYER_ATTRIBUTE);
            target_att_preprocessing     = dialog.getText(TARGET_ATT_PREPROCESSING);
            attribute_matcher            = (StringMatcher)MatcherRegistry
                                           .STRING_MATCHERS
                                           .get(dialog.getText(ATTRIBUTE_MATCHER));
            max_string_distance          = dialog.getDouble(MAXIMUM_STRING_DISTANCE);
            min_string_overlapping       = dialog.getDouble(MINIMUM_STRING_OVERLAPPING);
            if (!use_attributes) attribute_matcher = 
                MatchAllStringsMatcher.MATCH_ALL;
            else attribute_matcher.setAttributes(source_layer_attribute, 
                                                 target_layer_attribute);
            attribute_matcher.setMaximumDistance(max_string_distance);
            attribute_matcher.setMinimumOverlapping(min_string_overlapping);
            attribute_matcher.setSourceRule(RuleRegistry.getRule(source_att_preprocessing));
            attribute_matcher.setTargetRule(RuleRegistry.getRule(target_att_preprocessing));
            
            // get transfer options
            transfer                 = dialog.getBoolean(TRANSFER_TO_REFERENCE_LAYER);
            transfer_best_match_only = dialog.getBoolean(TRANSFER_BEST_MATCH_ONLY);
            string_aggregator        = (Aggregator)dialog.getComboBox(STRING_AGGREGATION).getSelectedItem();
            integer_aggregator       = (Aggregator)dialog.getComboBox(INTEGER_AGGREGATION).getSelectedItem();
            double_aggregator        = (Aggregator)dialog.getComboBox(DOUBLE_AGGREGATION).getSelectedItem();
            date_aggregator          = (Aggregator)dialog.getComboBox(DATE_AGGREGATION).getSelectedItem();
            if ((geometry_matcher instanceof MatchAllMatcher) && !use_attributes) {
                context.getWorkbenchFrame().warnUser(CHOOSE_MATCHER);
                return false;
            }
            return true;
        }
        else return false;
        
    }
    
    private void initDialog(final MultiTabInputDialog dialog, final PlugInContext context) {
        
        ////////////////////////////////////////////////////////////////////////
        // UI : INITIALIZE LAYERS FROM LAST ONES OR FROM CONTEXT
        ////////////////////////////////////////////////////////////////////////
        
        Layer source_layer;
        Layer target_layer;
        source_layer = context.getLayerManager().getLayer(source_layer_name);
        if (source_layer == null) source_layer = context.getCandidateLayer(0);
        
        target_layer = context.getLayerManager().getLayer(target_layer_name);
        int layerNumber = context.getLayerManager().getLayers().size();
        if (target_layer == null) target_layer = context.getCandidateLayer(layerNumber>1?1:0);
        
        ////////////////////////////////////////////////////////////////////////
        // UI : CHOOSE SOURCE LAYER AND SOURCE CARDINALITY
        ////////////////////////////////////////////////////////////////////////

        dialog.addLabel("<html><b>"+GEOMETRIC_OPTIONS+"</b></html>");
        
        final JComboBox jcb_layer = dialog.addLayerComboBox(SOURCE_LAYER, 
            source_layer, SOURCE_LAYER_TOOLTIP, context.getLayerManager());
        jcb_layer.setPreferredSize(new Dimension(220,20));
        jcb_layer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {updateDialog(dialog);}
        });
        final JCheckBox singleSourceFeatureCheckBox = dialog.addCheckBox(
            SINGLE_SOURCE, single_source, SINGLE_SOURCE_TOOLTIP);

        ////////////////////////////////////////////////////////////////////////
        // UI : CHOOSE GEOMETRY MATCHER
        ////////////////////////////////////////////////////////////////////////
        List geomMatcherList = new ArrayList();
        for (Iterator it = MatcherRegistry.GEOMETRY_MATCHERS.getMap().values().iterator() ; it.hasNext() ; ) {
            geomMatcherList.add(((Matcher)it.next()).toString());
        }
        final JComboBox jcb_geom_operation = dialog.addComboBox(GEOMETRY_MATCHER, geometry_matcher.toString(), geomMatcherList, null);

        final JTextField jtf_dist = dialog.addDoubleField(MAXIMUM_DISTANCE, max_distance, 12, null);
        jtf_dist.setEnabled(set_max_distance);
        
        final JTextField jtf_overlap = dialog.addDoubleField(MINIMUM_OVERLAPPING, min_overlapping, 12, null);
        jtf_overlap.setEnabled(set_min_overlapping);

        ////////////////////////////////////////////////////////////////////////
        // UI : CHOOSE TARGET LAYER AND SOURCE CARDINALITY
        ////////////////////////////////////////////////////////////////////////
        
        final JComboBox jcb_layer_tgt = dialog.addLayerComboBox(TARGET_LAYER, 
            target_layer, TARGET_LAYER_TOOLTIP, context.getLayerManager());
        jcb_layer_tgt.setPreferredSize(new Dimension(220,20));
        jcb_layer_tgt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {updateDialog(dialog);}
        });
        final JCheckBox singleTargetFeatureCheckBox = dialog.addCheckBox(
            SINGLE_TARGET, single_target, SINGLE_TARGET_TOOLTIP);

        jcb_geom_operation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateDialog(dialog);
                //MatcherRegistry.GEOMETRY_MATCHERS.get(jcb_geom_operation.getSelectedItem());
                geometry_matcher = (GeometryMatcher)MatcherRegistry.GEOMETRY_MATCHERS.get(jcb_geom_operation.getSelectedItem().toString());
                jtf_dist.setText(""+geometry_matcher.getMaximumDistance());
                jtf_overlap.setText(""+geometry_matcher.getMinimumOverlapping());
            }
        });

        ////////////////////////////////////////////////////////////////////////
        // UI : CHOOSE OUTPUT OPTIONS
        ////////////////////////////////////////////////////////////////////////
        dialog.addSeparator();
        dialog.addLabel("<html><b>"+OUTPUT_OPTIONS+"</b></html>");

        //final JCheckBox jcb_select_match    = dialog.addCheckBox(SELECT_MATCHING_FEATURES, select_matching_features, null);
        //final JCheckBox jcb_select_diff     = dialog.addCheckBox(SELECT_NOT_MATCHING_FEATURES, select_not_matching_features, null);
        final JCheckBox jcb_new_layer_match = dialog.addCheckBox(COPY_MATCHING_FEATURES, copy_matching_features, null);
        final JCheckBox jcb_new_layer_diff  = dialog.addCheckBox(COPY_NOT_MATCHING_FEATURES, copy_not_matching_features, null);
        final JCheckBox jcb_display_links   = dialog.addCheckBox(DISPLAY_LINKS, display_links, null);

        ////////////////////////////////////////////////////////////////////////
        // UI : CHOOSE ATTRIBUTE OPTIONS
        ////////////////////////////////////////////////////////////////////////
        dialog.addPane(ATTRIBUTE_OPTIONS);

        final JCheckBox jcb_use_attributes = dialog.addCheckBox(USE_ATTRIBUTES, use_attributes, null);
        
        final JComboBox jcb_src_att_preprocessing = dialog.addComboBox(SOURCE_ATT_PREPROCESSING,
            source_att_preprocessing, Arrays.asList(RuleRegistry.getRules()), null);
        final JComboBox jcb_src_attribute = dialog.addAttributeComboBox(
            SOURCE_LAYER_ATTRIBUTE, SOURCE_LAYER, AttributeTypeFilter.STRING_FILTER, null);
        //jcb_src_attribute.setEnabled(matching_layer_has_attributes && reference_layer_has_attributes);
        
        // Initialize string matching options
        List stringMatcherList = new ArrayList();
        for (Iterator it = MatcherRegistry.STRING_MATCHERS.getMap().values().iterator() ; it.hasNext() ; ) {
            stringMatcherList.add(((Matcher)it.next()).toString());
        }
        final JComboBox jcb_attr_operation = dialog.addComboBox(
            ATTRIBUTE_MATCHER, attribute_matcher.toString(), stringMatcherList, null);
        
        final JTextField jtf_string_dist = dialog.addDoubleField(MAXIMUM_STRING_DISTANCE, max_string_distance, 12, null);
        jtf_string_dist.setEnabled(set_max_string_distance);
        
        final JTextField jtf_string_overlap = dialog.addDoubleField(MINIMUM_STRING_OVERLAPPING, min_string_overlapping, 12, null);
        jtf_string_overlap.setEnabled(set_min_string_overlapping);
        
        final JComboBox jcb_tgt_att_preprocessing = dialog.addComboBox(TARGET_ATT_PREPROCESSING,
            target_att_preprocessing, Arrays.asList(RuleRegistry.getRules()), null);
        final JComboBox jcb_tgt_attribute = dialog.addAttributeComboBox(
            TARGET_LAYER_ATTRIBUTE, TARGET_LAYER, AttributeTypeFilter.STRING_FILTER, null);
        
        jcb_attr_operation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {updateDialog(dialog);}
        });
        
        jcb_use_attributes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {updateDialog(dialog);}
        });

        ////////////////////////////////////////////////////////////////////////
        // UI : TRANSFER ATTRIBUTE / AGGREGATION
        ////////////////////////////////////////////////////////////////////////
        dialog.addSeparator();
        dialog.addPane(TRANSFER_OPTIONS);
        dialog.addSubTitle(TRANSFER_OPTIONS);
        
        final JCheckBox jcb_transfer = dialog.addCheckBox(TRANSFER_TO_REFERENCE_LAYER, transfer, null);
        final JCheckBox jcb_transfer_best_match_only = dialog.addCheckBox(TRANSFER_BEST_MATCH_ONLY, transfer_best_match_only, null);
        final JComboBox jcb_string_aggregator = dialog.addComboBox(
                STRING_AGGREGATION, string_aggregator,
                Arrays.asList(new Aggregator[]{
                    Aggregator.CONCATENATE, 
                    Aggregator.CONCATENATE_UNIQUE,
                    Aggregator.MOST_FREQUENT}), 
                null
        );
        final JComboBox jcb_integer_aggregator = dialog.addComboBox(
                INTEGER_AGGREGATION, integer_aggregator,
                Arrays.asList(new Aggregator[]{
                    Aggregator.SUM_INTEGER, 
                    Aggregator.MEAN_INTEGER, 
                    Aggregator.MAX_INTEGER, 
                    Aggregator.MIN_INTEGER}), 
                null
        );
        final JComboBox jcb_double_aggregator = dialog.addComboBox(
                DOUBLE_AGGREGATION, double_aggregator,
                Arrays.asList(new Aggregator[]{
                    Aggregator.SUM_DOUBLE, 
                    Aggregator.MEAN_DOUBLE, 
                    Aggregator.MAX_DOUBLE, 
                    Aggregator.MIN_DOUBLE}), 
                null
        );
        final JComboBox jcb_date_aggregator = dialog.addComboBox(
                DATE_AGGREGATION, double_aggregator,
                Arrays.asList(new Aggregator[]{
                    Aggregator.MEAN_DATE, 
                    Aggregator.MAX_DATE, 
                    Aggregator.MIN_DATE}), 
                null
        );
        
        updateDialog(dialog);
    }
    
    private void updateDialog(MultiTabInputDialog dialog) {
        // Update related to a geometry_matcher change
        String sMatcher = dialog.getText(GEOMETRY_MATCHER);
        Matcher matcher = MatcherRegistry.GEOMETRY_MATCHERS.get(sMatcher);
        double dmax = ((GeometryMatcher)matcher).getMaximumDistance();
        double omin = ((GeometryMatcher)matcher).getMinimumOverlapping();
        boolean set_max_distance = !Double.isNaN(dmax);
        boolean set_min_overlapping = !Double.isNaN(omin);
        dialog.setFieldEnabled(MAXIMUM_DISTANCE, set_max_distance);
        dialog.setFieldEnabled(MINIMUM_OVERLAPPING, set_min_overlapping);
        
        // Update related to a layer change
        Layer srcLayer      = dialog.getLayer(SOURCE_LAYER);
        Layer tgtLayer      = dialog.getLayer(TARGET_LAYER);
        boolean srcLayer_has_attributes = 
            AttributeTypeFilter.STRING_FILTER.filter(srcLayer.getFeatureCollectionWrapper().getFeatureSchema()).size() > 0;
        boolean tgtLayer_has_attributes = 
            AttributeTypeFilter.STRING_FILTER.filter(tgtLayer.getFeatureCollectionWrapper().getFeatureSchema()).size() > 0;
        dialog.setFieldEnabled(USE_ATTRIBUTES, srcLayer_has_attributes && tgtLayer_has_attributes);
        dialog.setTabEnabled(ATTRIBUTE_OPTIONS, srcLayer_has_attributes && 
                                                tgtLayer_has_attributes);
        if (!srcLayer_has_attributes || !tgtLayer_has_attributes) {
            attribute_matcher = MatchAllStringsMatcher.MATCH_ALL;
            ((JCheckBox)dialog.getCheckBox(USE_ATTRIBUTES)).setSelected(false);
        }
        
        // Updates related to attribute transfer
        dialog.setTabEnabled(TRANSFER_OPTIONS, srcLayer_has_attributes);
        dialog.setFieldEnabled(TRANSFER_BEST_MATCH_ONLY, transfer);
        dialog.setFieldEnabled(STRING_AGGREGATION, !transfer_best_match_only);
        dialog.setFieldEnabled(INTEGER_AGGREGATION, !transfer_best_match_only);
        dialog.setFieldEnabled(DOUBLE_AGGREGATION, !transfer_best_match_only);
        dialog.setFieldEnabled(DATE_AGGREGATION, !transfer_best_match_only);
        
        // Updates related to attribute matching
        use_attributes = dialog.getBoolean(USE_ATTRIBUTES);
        dialog.setFieldEnabled(SOURCE_LAYER_ATTRIBUTE, use_attributes);
        dialog.setFieldEnabled(SOURCE_ATT_PREPROCESSING, use_attributes);
        dialog.setFieldEnabled(TARGET_LAYER_ATTRIBUTE, use_attributes);
        dialog.setFieldEnabled(TARGET_ATT_PREPROCESSING, use_attributes);
        dialog.setFieldEnabled(MAXIMUM_STRING_DISTANCE, use_attributes);
        dialog.setFieldEnabled(MINIMUM_STRING_OVERLAPPING, use_attributes);
        dialog.setFieldEnabled(ATTRIBUTE_MATCHER, use_attributes);
        String aMatcher = dialog.getText(ATTRIBUTE_MATCHER);
        attribute_matcher = (StringMatcher)MatcherRegistry.STRING_MATCHERS.get(aMatcher);
        double sdmax = ((AttributeMatcher)attribute_matcher).getMaximumDistance();
        double somin = ((AttributeMatcher)attribute_matcher).getMinimumOverlapping();
        boolean set_max_string_distance = !Double.isNaN(sdmax);
        boolean set_min_string_overlapping = !Double.isNaN(somin);
        dialog.setFieldEnabled(MAXIMUM_STRING_DISTANCE, set_max_string_distance);
        dialog.setFieldEnabled(MINIMUM_STRING_OVERLAPPING, set_min_string_overlapping);
    }
    
    /**
     * Run executes the main process, looping through matching layer, and
     * looking for candidates in the reference layer.
     */
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        
        Layer source_layer = context.getLayerManager().getLayer(source_layer_name);
        Layer target_layer = context.getLayerManager().getLayer(target_layer_name);
        FeatureCollection source_fc = source_layer.getFeatureCollectionWrapper();
        FeatureCollection target_fc = target_layer.getFeatureCollectionWrapper();
        if (source_layer == null || target_layer ==  null) {
            context.getWorkbenchFrame().warnUser(MISSING_INPUT_LAYER);
            return;
        }
        monitor.allowCancellationRequests();
        monitor.report(SEARCHING_MATCHES);
        FeatureCollectionMatcher matcher = new FeatureCollectionMatcher(
                source_fc.getFeatures(), target_fc.getFeatures(), 
                geometry_matcher, attribute_matcher);
        Collection<Feature> features = matcher.matchAll(single_source, single_target);
        //if (select_matching_features) {}
        //if (select_not_matching_features) {}
        if (copy_matching_features) {
            Layer lyr = createLayer(
                features, context,
                source_layer.getName() + "-" + I18NPlug.getI18N("matched"));
            if (lyr != null) setMatchingStyle(lyr);
        }
        if (copy_not_matching_features) {
            Layer lyr = createLayer(
                inverse(source_layer.getFeatureCollectionWrapper(), features), 
                context, 
                source_layer.getName() + "-" + I18NPlug.getI18N("un-matched"));
            if (lyr != null) setNotMatchingStyle(lyr);
        }
        if (display_links) {
            Layer lyr = createLayer(createLinks(matcher.getMatchMap()), context,
                I18NPlug.getI18N("un-matched") + " " + source_layer.getName() + " - " + target_layer.getName());
            if (lyr != null) setLinkStyle(lyr);
        }
        if (transfer) {
            FeatureSchema target_schema = target_fc.getFeatureSchema();
            FeatureSchema new_schema = (FeatureSchema)target_schema.clone();
            if (!new_schema.hasAttribute("X_COUNT")) {
                new_schema.addAttribute("X_COUNT", AttributeType.INTEGER);
            }
            FeatureSchema source_schema = source_fc.getFeatureSchema();
            for (int i = 0 ; i < source_schema.getAttributeCount() ; i++) {
                if (source_schema.getAttributeType(i) != AttributeType.GEOMETRY &&
                    source_schema.getAttributeType(i) != AttributeType.OBJECT) {
                    new_schema.addAttribute(
                        "X_" + source_schema.getAttributeName(i),
                        source_schema.getAttributeType(i));
                }
            }
            FeatureCollection new_dataset = new FeatureDataset(new_schema);
            MatchMap matchMap = matcher.getMatchMap();
            // If user wants to transfer attributes from the best match only
            // and MatchMap has not yet been filtered by single_source option
            if (transfer_best_match_only && !single_source) {
                matchMap = matchMap.filter(true, false);
            }
            for (Object o : target_fc.getFeatures()) {
                Feature f = (Feature)o;
                Feature bf = new BasicFeature(new_schema);
                Object[] attributes = new Object[new_schema.getAttributeCount()];
                System.arraycopy(f.getAttributes(), 0, attributes, 0, target_schema.getAttributeCount());
                bf.setAttributes(attributes);
                List<Feature> matches = matchMap.getMatchedFeaturesFromTarget(f);
                bf.setAttribute("X_COUNT", matches.size());
                for (int i = 0 ; i < source_schema.getAttributeCount() ; i++) {
                    String name = source_schema.getAttributeName(i);
                    AttributeType type = source_schema.getAttributeType(i);
                    if (type == AttributeType.GEOMETRY) continue;
                    else if (type == AttributeType.OBJECT) continue;
                    else if (type == AttributeType.STRING) {
                        bf.setAttribute("X_" + name, 
                            string_aggregator.aggregate(matches, name, ignore_null));
                    }
                    else if (type == AttributeType.INTEGER) {
                        bf.setAttribute("X_" + name, 
                            integer_aggregator.aggregate(matches, name, ignore_null));
                    }
                    else if (type == AttributeType.DOUBLE) {
                        bf.setAttribute("X_" + name, 
                            double_aggregator.aggregate(matches, name, ignore_null));
                    }
                    else if (type == AttributeType.DATE) {
                        bf.setAttribute("X_" + name, 
                            date_aggregator.aggregate(matches, name, ignore_null));
                    }
                }
                new_dataset.add(bf);
            }
            Layer lyr = createLayer(new_dataset.getFeatures(), context, target_layer.getName());
        }
    }
    
    private Layer createLayer(Collection<Feature> features, PlugInContext context, String name) {
        if (features.size()>0) {
            FeatureSchema schema = ((Feature)features.iterator().next()).getSchema();
            FeatureCollection fc = new FeatureDataset(schema);
            fc.addAll(features);
            return context.getLayerManager()
                          .addLayer(StandardCategoryNames.RESULT, name, fc);
        }
        return null;
    }
    
    private Collection<Feature> inverse(FeatureCollection fc, 
                                        Collection<Feature> features) {
        Map<Integer,Feature> map = new HashMap<Integer,Feature>();
        for (Feature f : features) map.put(f.getID(), f);
        List<Feature> inverse = new ArrayList<Feature>();
        for (Object o : fc.getFeatures()) {
            if (!map.containsKey(((Feature)o).getID())) inverse.add((Feature)o);    
        }
        return inverse;
    }
    
    public Collection<Feature> createLinks(MatchMap map) {
        List<Feature> links = new ArrayList<Feature>();
        GeometryFactory gf = new GeometryFactory();
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        schema.addAttribute("SOURCE", AttributeType.INTEGER);
        schema.addAttribute("TARGET", AttributeType.INTEGER);
        schema.addAttribute("SCORE", AttributeType.DOUBLE);
        for (Match match : map.getAllMatches()) {
            BasicFeature f = new BasicFeature(schema);
            Coordinate[] coords = new Coordinate[2];
            if (geometry_matcher instanceof MinimumDistanceMatcher) {
                coords = DistanceOp.closestPoints(
                    match.getSource().getGeometry(), 
                    match.getTarget().getGeometry());
            } else {
                coords[0] = match.getSource().getGeometry().getInteriorPoint().getCoordinate();
                coords[1] = match.getTarget().getGeometry().getInteriorPoint().getCoordinate();
            }
            Geometry g = coords[0].equals(coords[1]) ? 
                         gf.createPoint(coords[0]):
                         gf.createLineString(coords);
            f.setGeometry(g);
            f.setAttribute("SOURCE", match.getSource().getID());
            f.setAttribute("TARGET", match.getTarget().getID());
            f.setAttribute("SCORE",  match.getScore());
            links.add(f);
        }
        return links;
    }
    
    public void setMatchingStyle(Layer layer) {
        BasicStyle style = layer.getBasicStyle();
        style.setLineColor(Color.GREEN);
        style.setLineWidth(5);
        style.setAlpha(200);
        style.setFillColor(Color.LIGHT_GRAY);
    }
    
    public void setNotMatchingStyle(Layer layer) {
        BasicStyle style = layer.getBasicStyle();
        style.setLineColor(Color.ORANGE);
        style.setLineWidth(5);
        style.setAlpha(200);
        style.setFillColor(Color.LIGHT_GRAY);
    }
    
    public void setLinkStyle(Layer layer) {
        BasicStyle style = layer.getBasicStyle();
        style.setLineColor(Color.RED);
        style.setLineWidth(2);
        style.setAlpha(255);
        style.setRenderingFill(false);
        layer.addStyle(new MyRingVertexStyle());
        layer.getStyle(MyRingVertexStyle.class).setEnabled(true);
    }
    
    public static class MyRingVertexStyle extends RingVertexStyle {
    
        public MyRingVertexStyle() {super();}
    
        public int getSize() {return 25;}
        
        public void paint(Feature f, Graphics2D g, Viewport viewport) throws Exception {
            if (f.getGeometry() instanceof Point) {
                Coordinate coord = f.getGeometry().getCoordinate();
                paint(g, viewport.toViewPoint(new Point2D.Double(coord.x, coord.y)));
            }
        }
    
        protected void render(java.awt.Graphics2D g) {
            g.setStroke(new java.awt.BasicStroke(2.5f));
            g.setColor(Color.RED);
            g.draw(shape);
        }
    }

}
