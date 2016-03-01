/****************************************************************************
 * This demo file is part of yFiles for Java 2.12.0.1.
 * Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 * 72070 Tuebingen, Germany. All rights reserved.
 * 
 * yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 * of demo files in source code or binary form, with or without
 * modification, is not permitted.
 * 
 * Owners of a valid software license for a yFiles for Java version that this
 * demo is shipped with are allowed to use the demo source code as basis
 * for their own yFiles for Java powered applications. Use of such programs is
 * governed by the rights and conditions as set out in the yFiles for Java
 * license agreement.
 * 
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***************************************************************************/
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.ComponentLayouter;
import y.layout.organic.OutputRestriction;
import y.layout.organic.SmartOrganicLayouter;
import y.option.ConstraintManager;
import y.option.ConstraintManager.Condition;
import y.option.DefaultEditorFactory;
import y.option.DoubleOptionItem;
import y.option.EnumOptionItem;
import y.option.IntOptionItem;
import y.option.OptionHandler;
import y.option.OptionGroup;
import y.option.OptionItem;
import y.util.Maps;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;

import java.awt.Rectangle;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.organic.SmartOrganicLayouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_advanced_features.html#layout_advanced_features">Section Advanced Layout Concepts</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/smart_organic_layouter.html#smart_organic_layouter">Section Organic Layout Style</a> in the yFiles for Java Developer's Guide
 */
public class SmartOrganicLayoutModule extends LayoutModule {
  //// Module 'Smart Organic Layout'
  protected static final String MODULE_SMARTORGANIC = "SMARTORGANIC";
  
  //// Section 'Visual'
  protected static final String SECTION_VISUAL = "VISUAL";
  // Section 'Visual' items
  protected static final String ITEM_SCOPE = "SCOPE";
  protected static final String VALUE_SCOPE_ALL = "ALL";
  protected static final String VALUE_SCOPE_MAINLY_SUBSET = "MAINLY_SUBSET";
  protected static final String VALUE_SCOPE_SUBSET = "SUBSET";
  protected static final String ITEM_PREFERRED_EDGE_LENGTH = "PREFERRED_EDGE_LENGTH";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  protected static final String ITEM_ALLOW_NODE_OVERLAPS = "ALLOW_NODE_OVERLAPS";
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_AVOID_NODE_EDGE_OVERLAPS = "AVOID_NODE_EDGE_OVERLAPS";
  protected static final String ITEM_COMPACTNESS = "COMPACTNESS";
  protected static final String ITEM_USE_AUTO_CLUSTERING = "USE_AUTO_CLUSTERING";
  protected static final String ITEM_AUTO_CLUSTERING_QUALITY = "AUTO_CLUSTERING_QUALITY";
  
  //// Section 'Restrictions'
  protected static final String SECTION_RESTRICTIONS = "RESTRICTIONS";
  // Section 'Restrictions' items
  protected static final String ITEM_RESTRICT_OUTPUT = "RESTRICT_OUTPUT";
  protected static final String VALUE_NONE = "NONE";
  protected static final String VALUE_OUTPUT_CAGE = "OUTPUT_CAGE";
  protected static final String VALUE_OUTPUT_CIRCULAR_CAGE = "OUTPUT_CIRCULAR_CAGE";
  protected static final String VALUE_OUTPUT_AR = "OUTPUT_AR";
  protected static final String VALUE_OUTPUT_ELLIPTICAL_CAGE = "OUTPUT_ELLIPTICAL_CAGE";
  protected static final String TITLE_OUTPUT_CAGE = "OUTPUT_CAGE";
  protected static final String ITEM_RECT_CAGE_USE_VIEW = "RECT_CAGE_USE_VIEW";
  protected static final String ITEM_CAGE_X = "CAGE_X";
  protected static final String ITEM_CAGE_Y = "CAGE_Y";
  protected static final String ITEM_CAGE_WIDTH = "CAGE_WIDTH";
  protected static final String ITEM_CAGE_HEIGHT = "CAGE_HEIGHT";
  protected static final String TITLE_OUTPUT_CIRCULAR_CAGE = "OUTPUT_CIRCULAR_CAGE";
  protected static final String ITEM_CIRC_CAGE_USE_VIEW = "CIRC_CAGE_USE_VIEW";
  protected static final String ITEM_CAGE_CENTER_X = "CAGE_CENTER_X";
  protected static final String ITEM_CAGE_CENTER_Y = "CAGE_CENTER_Y";
  protected static final String ITEM_CAGE_RADIUS = "CAGE_RADIUS";
  protected static final String TITLE_OUTPUT_AR = "OUTPUT_AR";
  protected static final String ITEM_AR_CAGE_USE_VIEW = "AR_CAGE_USE_VIEW";
  protected static final String ITEM_CAGE_RATIO = "CAGE_RATIO";
  protected static final String TITLE_OUTPUT_ELLIPTICAL_CAGE = "OUTPUT_ELLIPTICAL_CAGE";
  protected static final String ITEM_ELL_CAGE_USE_VIEW = "ELL_CAGE_USE_VIEW";
  protected static final String ITEM_ELLIPTICAL_CAGE_X = "ELLIPTICAL_CAGE_X";
  protected static final String ITEM_ELLIPTICAL_CAGE_Y = "ELLIPTICAL_CAGE_Y";
  protected static final String ITEM_ELLIPTICAL_CAGE_WIDTH = "ELLIPTICAL_CAGE_WIDTH";
  protected static final String ITEM_ELLIPTICAL_CAGE_HEIGHT = "ELLIPTICAL_CAGE_HEIGHT";
  
  //// Section 'Grouping'
  protected static final String SECTION_GROUPING = "GROUPING";
  // Section 'Grouping' items
  protected static final String ITEM_GROUP_LAYOUT_POLICY = "GROUP_LAYOUT_POLICY";
  protected static final String VALUE_LAYOUT_GROUPS = "LAYOUT_GROUPS";
  protected static final String VALUE_FIX_GROUP_CONTENTS = "FIX_GROUP_CONTENTS";
  protected static final String VALUE_FIX_GROUP_BOUNDS = "FIX_GROUP_BOUNDS";
  protected static final String VALUE_IGNORE_GROUPS = "IGNORE_GROUPS";
  protected static final String ITEM_USE_AUTOMATIC_GROUP_NODE_COMPACTION = "USE_AUTOMATIC_GROUP_NODE_COMPACTION";
  protected static final String ITEM_GROUP_COMPACTNESS = "GROUP_COMPACTNESS";
  
  //// Section 'Algorithm'
  protected static final String SECTION_ALGORITHM = "ALGORITHM";
  // Section 'Algorithm' items
  protected static final String ITEM_QUALITY_TIME_RATIO = "QUALITY_TIME_RATIO";
  protected static final String ITEM_MAXIMAL_DURATION = "MAXIMAL_DURATION";
  protected static final String ITEM_ACTIVATE_DETERMINISTIC_MODE = "ACTIVATE_DETERMINISTIC_MODE";
  protected static final String ITEM_ALLOW_MULTI_THREADING = "ALLOW_MULTI_THREADING";


  private boolean isNodeDPAddedByModule;

  /**
   * Creates an instance of this module.
   */
  public SmartOrganicLayoutModule() {
    super(MODULE_SMARTORGANIC);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Defaults provider
    final SmartOrganicLayouter defaults = new SmartOrganicLayouter();

    //// Section 'Visual'
    options.useSection(SECTION_VISUAL);
    // Populate section
    options.addEnum(ITEM_SCOPE, new String[]{
        VALUE_SCOPE_ALL,
        VALUE_SCOPE_MAINLY_SUBSET,
        VALUE_SCOPE_SUBSET
    }, defaults.getScope());
    options.addInt(ITEM_PREFERRED_EDGE_LENGTH, (int)defaults.getPreferredEdgeLength(), 5, 500);
    final OptionItem itemConsiderNodeLabels =
        options.addBool(ITEM_CONSIDER_NODE_LABELS,defaults.isConsiderNodeLabelsEnabled());
    final OptionItem itemAllowNodeOverlaps = options.addBool(ITEM_ALLOW_NODE_OVERLAPS, defaults.isNodeOverlapsAllowed());
    final OptionItem itemMinimalNodeDistance =
            options.addDouble(ITEM_MINIMAL_NODE_DISTANCE, defaults.getMinimalNodeDistance(), 0, 100, 0);
    options.addBool(ITEM_AVOID_NODE_EDGE_OVERLAPS, false);
    options.addDouble(ITEM_COMPACTNESS, defaults.getCompactness(),0,1);
    final OptionItem itemUseAutoClustering =
        options.addBool(ITEM_USE_AUTO_CLUSTERING, defaults.isAutoClusteringEnabled());
    final OptionItem itemAutoClusteringQuality =
        options.addDouble(ITEM_AUTO_CLUSTERING_QUALITY, defaults.getAutoClusteringQuality(), 0, 1);
    // Enable/disable items depending on specific values
    Condition condition =
        optionConstraints.createConditionValueEquals(itemAllowNodeOverlaps, Boolean.FALSE).or(
            optionConstraints.createConditionValueEquals(itemConsiderNodeLabels, Boolean.TRUE));
    optionConstraints.setEnabledOnCondition(condition, itemMinimalNodeDistance);
    optionConstraints.setEnabledOnValueEquals(itemConsiderNodeLabels, Boolean.FALSE, itemAllowNodeOverlaps);
    optionConstraints.setEnabledOnValueEquals(itemUseAutoClustering, Boolean.TRUE, itemAutoClusteringQuality);
    
    //// Section 'Restrictions'
    options.useSection(SECTION_RESTRICTIONS);
    // Populate section
    final Object ctrId = new Object();
    final EnumOptionItem itemRestrictOutput = options.addEnum(ITEM_RESTRICT_OUTPUT, new String[]{
        VALUE_NONE,
        VALUE_OUTPUT_CAGE,
        VALUE_OUTPUT_CIRCULAR_CAGE,
        VALUE_OUTPUT_AR,
        VALUE_OUTPUT_ELLIPTICAL_CAGE
    }, 0);
    itemRestrictOutput.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId);
    
    // Group 'Rectangular'
    final OptionGroup cageGroup = new OptionGroup();
    cageGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_OUTPUT_CAGE);
    // Populate group
    cageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId);
    cageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, VALUE_OUTPUT_CAGE);
    final OptionItem itemRectCageUseView = cageGroup.addItem(options.addBool(ITEM_RECT_CAGE_USE_VIEW, true));
    final OptionItem itemCageX = cageGroup.addItem(options.addDouble(ITEM_CAGE_X, 0.0d));
    final OptionItem itemCageY = cageGroup.addItem(options.addDouble(ITEM_CAGE_Y, 0.0d));
    final OptionItem itemCageWidth = cageGroup.addItem(options.addDouble(ITEM_CAGE_WIDTH, 1000.0d));
    itemCageWidth.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    final OptionItem itemCageHeight = cageGroup.addItem(options.addDouble(ITEM_CAGE_HEIGHT, 1000.0d));
    itemCageHeight.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemRestrictOutput, VALUE_OUTPUT_CAGE, cageGroup);
    condition = optionConstraints.createConditionValueEquals(itemRestrictOutput, VALUE_OUTPUT_CAGE).and(
        optionConstraints.createConditionValueEquals(itemRectCageUseView, Boolean.FALSE));
    optionConstraints.setEnabledOnCondition(condition, itemCageX);
    optionConstraints.setEnabledOnCondition(condition, itemCageY);
    optionConstraints.setEnabledOnCondition(condition, itemCageWidth);
    optionConstraints.setEnabledOnCondition(condition, itemCageHeight);

    // Group 'Circular'
    final OptionGroup circularCageGroup = new OptionGroup();
    circularCageGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_OUTPUT_CIRCULAR_CAGE);
    // Populate group
    circularCageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId);
    circularCageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, VALUE_OUTPUT_CIRCULAR_CAGE);
    final OptionItem itemCircCageUseView = circularCageGroup.addItem(options.addBool(ITEM_CIRC_CAGE_USE_VIEW, true));
    final OptionItem itemCageCenterX = circularCageGroup.addItem(options.addDouble(ITEM_CAGE_CENTER_X, 0.0d));
    final OptionItem itemCageCenterY = circularCageGroup.addItem(options.addDouble(ITEM_CAGE_CENTER_Y, 0.0d));
    final OptionItem itemCageRadius = circularCageGroup.addItem(options.addDouble(ITEM_CAGE_RADIUS, 1000.0d));
    itemCageRadius.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemRestrictOutput, VALUE_OUTPUT_CIRCULAR_CAGE, circularCageGroup);
    condition = optionConstraints.createConditionValueEquals(itemRestrictOutput, VALUE_OUTPUT_CIRCULAR_CAGE).and(
        optionConstraints.createConditionValueEquals(itemCircCageUseView, Boolean.FALSE));
    optionConstraints.setEnabledOnCondition(condition, itemCageCenterX);
    optionConstraints.setEnabledOnCondition(condition, itemCageCenterY);
    optionConstraints.setEnabledOnCondition(condition, itemCageRadius);

    // Group 'Aspect Ratio'
    final OptionGroup arGroup = new OptionGroup();
    arGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_OUTPUT_AR);
    // Populate group
    arGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId);
    arGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, VALUE_OUTPUT_AR);
    final OptionItem itemArCageUseView = arGroup.addItem(options.addBool(ITEM_AR_CAGE_USE_VIEW, true));
    optionConstraints.setEnabledOnValueEquals(itemRestrictOutput, VALUE_OUTPUT_AR, arGroup);
    condition = optionConstraints.createConditionValueEquals(itemRestrictOutput, VALUE_OUTPUT_AR).and(
        optionConstraints.createConditionValueEquals(itemArCageUseView, Boolean.FALSE));
    optionConstraints.setEnabledOnCondition(condition, arGroup.addItem(options.addDouble(ITEM_CAGE_RATIO, 1.0d)));

    // Group 'Elliptical'
    final OptionGroup ellipticalCageGroup = new OptionGroup();
    ellipticalCageGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_OUTPUT_ELLIPTICAL_CAGE);
    // Populate group
    ellipticalCageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CONTROLLER_ID, ctrId);
    ellipticalCageGroup.setAttribute(DefaultEditorFactory.ATTRIBUTE_CARD_ID, VALUE_OUTPUT_ELLIPTICAL_CAGE);
    final OptionItem itemEllCageUseView = ellipticalCageGroup.addItem(options.addBool(ITEM_ELL_CAGE_USE_VIEW, true));
    final OptionItem itemEllipticalCageX = ellipticalCageGroup.addItem(options.addDouble(ITEM_ELLIPTICAL_CAGE_X, 0.0d));
    final OptionItem itemEllipticalCageY = ellipticalCageGroup.addItem(options.addDouble(ITEM_ELLIPTICAL_CAGE_Y, 0.0d));
    final OptionItem itemEllipticalCageWidth = ellipticalCageGroup.addItem(
        options.addDouble(ITEM_ELLIPTICAL_CAGE_WIDTH, 1000.0d));
    itemEllipticalCageWidth.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    final OptionItem itemEllipticalCageHeight = ellipticalCageGroup.addItem(
            options.addDouble(ITEM_ELLIPTICAL_CAGE_HEIGHT, 1000.0d));
    itemEllipticalCageHeight.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(Double.MIN_VALUE));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemRestrictOutput, VALUE_OUTPUT_ELLIPTICAL_CAGE, ellipticalCageGroup);
    condition = optionConstraints.createConditionValueEquals(itemRestrictOutput, VALUE_OUTPUT_ELLIPTICAL_CAGE).and(
        optionConstraints.createConditionValueEquals(itemEllCageUseView, Boolean.FALSE));
    optionConstraints.setEnabledOnCondition(condition, itemEllipticalCageX);
    optionConstraints.setEnabledOnCondition(condition, itemEllipticalCageY);
    optionConstraints.setEnabledOnCondition(condition, itemEllipticalCageWidth);
    optionConstraints.setEnabledOnCondition(condition, itemEllipticalCageHeight);
    
    //// Section 'Grouping'
    options.useSection(SECTION_GROUPING);
    // Populate section
    options.addEnum(ITEM_GROUP_LAYOUT_POLICY, new String[]{
        VALUE_LAYOUT_GROUPS,
        VALUE_FIX_GROUP_CONTENTS,
        VALUE_FIX_GROUP_BOUNDS,
        VALUE_IGNORE_GROUPS
    }, 0);
    final OptionItem itemUseAutomaticGroupNodeCompaction =
        options.addBool(ITEM_USE_AUTOMATIC_GROUP_NODE_COMPACTION, defaults.isAutomaticGroupNodeCompactionEnabled());
    final OptionItem itemGroupCompactness =
        options.addDouble(ITEM_GROUP_COMPACTNESS, defaults.getGroupNodeCompactness(), 0, 1);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseAutomaticGroupNodeCompaction, Boolean.FALSE, itemGroupCompactness);

    //// Section 'Algorithm'
    options.useSection(SECTION_ALGORITHM);
    // Populate section
    final OptionItem itemQualityTimeRatio =
        options.addDouble(ITEM_QUALITY_TIME_RATIO, defaults.getQualityTimeRatio(), 0, 1);
    itemQualityTimeRatio.setAttribute(DefaultEditorFactory.ATTRIBUTE_MIN_VALUE_LABEL_TEXT, "SPEED");
    itemQualityTimeRatio.setAttribute(DefaultEditorFactory.ATTRIBUTE_MAX_VALUE_LABEL_TEXT, "QUALITY");
    options.addInt(ITEM_MAXIMAL_DURATION, (int)(defaults.getMaximumDuration()/1000))
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(0));
    options.addBool(ITEM_ACTIVATE_DETERMINISTIC_MODE, defaults.isDeterministic());
    options.addBool(ITEM_ALLOW_MULTI_THREADING, true);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final SmartOrganicLayouter organic = new SmartOrganicLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(organic, options);

    final Graph2D graph = getGraph2D();

    final Graph2DLayoutExecutor layoutExecutor = getLayoutExecutor();
    final boolean wasConfiguringGrouping = layoutExecutor.isConfiguringGrouping();
    layoutExecutor.setConfiguringGrouping(isGroupingNodes(graph, options.getString(ITEM_GROUP_LAYOUT_POLICY)));

    prepareGraph(graph, options);
    try {
      launchLayouter(organic);
    } finally {
      restoreGraph(graph, options);
      layoutExecutor.setConfiguringGrouping(wasConfiguringGrouping);
    }
  }

  /**
   * Prepares a <code>graph</code> depending on the given options for the
   * module's layout algorithm.
   * <br>
   * Additional resources created by this method have to be freed up by calling
   * {@link #restoreGraph(y.view.Graph2D, y.option.OptionHandler)} after
   * layout calculation.  
   * @param graph the graph to be prepared
   * @param options the options for the module's layout algorithm
   */
  protected void prepareGraph(final Graph2D graph, final OptionHandler options) {
    isNodeDPAddedByModule = false;
    final String policy = options.getString(ITEM_GROUP_LAYOUT_POLICY);
    final boolean grouping = isGroupingNodes(graph, policy);
    if (VALUE_FIX_GROUP_BOUNDS.equals(policy) && grouping) {
      final NodeMap nodeMap = Maps.createHashedNodeMap();
      for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        final Node node = nodeCursor.node();
        if (HierarchyManager.getInstance(graph).isGroupNode(node)) {
          nodeMap.set(node, SmartOrganicLayouter.GROUP_NODE_MODE_FIX_BOUNDS);
        }
      }
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, SmartOrganicLayouter.GROUP_NODE_MODE_DATA);
      graph.addDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA, nodeMap);
      isNodeDPAddedByModule = true;
    } else if (VALUE_FIX_GROUP_CONTENTS.equals(policy) && grouping) {
      final NodeMap nodeMap = Maps.createHashedNodeMap();
      for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
        final Node node = nodeCursor.node();
        if (HierarchyManager.getInstance(graph).isGroupNode(node)) {
          nodeMap.set(node, SmartOrganicLayouter.GROUP_NODE_MODE_FIX_CONTENTS);
        }
      }
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, SmartOrganicLayouter.GROUP_NODE_MODE_DATA);
      graph.addDataProvider(SmartOrganicLayouter.GROUP_NODE_MODE_DATA, nodeMap);
      isNodeDPAddedByModule = true;
    }
    // backup existing data providers to prevent loss of user settings
    backupDataProvider(graph, SmartOrganicLayouter.NODE_SUBSET_DATA);
    graph.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA, Selections.createSelectionNodeMap(graph));
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // remove the data providers set by this module by restoring the initial state
    restoreDataProvider(graph, SmartOrganicLayouter.NODE_SUBSET_DATA);

    if (isNodeDPAddedByModule) {
      isNodeDPAddedByModule = false;
      restoreDataProvider(graph, SmartOrganicLayouter.GROUP_NODE_MODE_DATA);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param organic the <code>SmartOrganicLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final SmartOrganicLayouter organic, final OptionHandler options) {
    organic.setPreferredEdgeLength(options.getInt(SECTION_VISUAL, ITEM_PREFERRED_EDGE_LENGTH));
    
    boolean considerNodeLabels = options.getBool(SECTION_VISUAL, ITEM_CONSIDER_NODE_LABELS);
    organic.setConsiderNodeLabelsEnabled(considerNodeLabels);
    organic.setNodeOverlapsAllowed(options.getBool(SECTION_VISUAL, ITEM_ALLOW_NODE_OVERLAPS) && !considerNodeLabels);
    
    organic.setMinimalNodeDistance(options.getDouble(SECTION_VISUAL, ITEM_MINIMAL_NODE_DISTANCE));
    
    final String is = options.getString(SECTION_VISUAL, ITEM_SCOPE);
    if (VALUE_SCOPE_SUBSET.equals(is)) {
      organic.setScope(SmartOrganicLayouter.SCOPE_SUBSET);
    } else if (VALUE_SCOPE_MAINLY_SUBSET.equals(is)) {
      organic.setScope(SmartOrganicLayouter.SCOPE_MAINLY_SUBSET);
    } else {
      // else if VALUE_SCOPE_ALL.equals(is)
      organic.setScope(SmartOrganicLayouter.SCOPE_ALL);
    }
    
    organic.setCompactness(options.getDouble(SECTION_VISUAL, ITEM_COMPACTNESS));
    organic.setAutomaticGroupNodeCompactionEnabled(options.getBool(ITEM_USE_AUTOMATIC_GROUP_NODE_COMPACTION));
    organic.setGroupNodeCompactness(options.getDouble(ITEM_GROUP_COMPACTNESS));
    //Doesn't really make sense to ignore node sizes (for certain configurations, this setting
    //doesn't have an effect anyway)
    organic.setNodeSizeAware(true);
    organic.setAutoClusteringEnabled(options.getBool(ITEM_USE_AUTO_CLUSTERING));
    organic.setAutoClusteringQuality(options.getDouble(ITEM_AUTO_CLUSTERING_QUALITY));
    organic.setNodeEdgeOverlapAvoided(options.getBool(ITEM_AVOID_NODE_EDGE_OVERLAPS));
    organic.setDeterministic(options.getBool(SECTION_ALGORITHM, ITEM_ACTIVATE_DETERMINISTIC_MODE));
    organic.setMultiThreadingAllowed(options.getBool(SECTION_ALGORITHM, ITEM_ALLOW_MULTI_THREADING));
    organic.setMaximumDuration(1000L * options.getInt(SECTION_ALGORITHM, ITEM_MAXIMAL_DURATION));
    organic.setQualityTimeRatio(options.getDouble(SECTION_ALGORITHM, ITEM_QUALITY_TIME_RATIO));
    
    ((ComponentLayouter) organic.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    
    final String restrictOutput = options.getString(ITEM_RESTRICT_OUTPUT);
    if (VALUE_NONE.equals(restrictOutput)) {
      organic.setComponentLayouterEnabled(true);
      organic.setOutputRestriction(OutputRestriction.NONE);
    } else if (VALUE_OUTPUT_CAGE.equals(restrictOutput)) {
      final double x;
      final double y;
      final double w;
      final double h;
      if (options.getBool(ITEM_RECT_CAGE_USE_VIEW) && getGraph2DView() != null) {
        Rectangle visibleRect = getGraph2DView().getVisibleRect();
        x = visibleRect.x;
        y = visibleRect.y;
        w = visibleRect.width;
        h = visibleRect.height;
      } else {
        x = options.getDouble(ITEM_CAGE_X);
        y = options.getDouble(ITEM_CAGE_Y);
        w = options.getDouble(ITEM_CAGE_WIDTH);
        h = options.getDouble(ITEM_CAGE_HEIGHT);
      }
      
      organic.setOutputRestriction(
          OutputRestriction.createRectangularCageRestriction(x, y, w, h));
      organic.setComponentLayouterEnabled(false);
    } else if (VALUE_OUTPUT_CIRCULAR_CAGE.equals(restrictOutput)) {
      final double x;
      final double y;
      final double radius;
      if (options.getBool(ITEM_CIRC_CAGE_USE_VIEW) && getGraph2DView() != null) {
        Rectangle visibleRect = getGraph2DView().getVisibleRect();
        x = visibleRect.getCenterX();
        y = visibleRect.getCenterY();
        radius = Math.min(visibleRect.width, visibleRect.height) * 0.5d;
      } else {
        x = options.getDouble(ITEM_CAGE_CENTER_X);
        y = options.getDouble(ITEM_CAGE_CENTER_Y);
        radius = options.getDouble(ITEM_CAGE_RADIUS);
      }
      
      organic.setOutputRestriction(OutputRestriction.createCircularCageRestriction(x, y, radius));
      organic.setComponentLayouterEnabled(false);
    } else if (VALUE_OUTPUT_AR.equals(restrictOutput)) {
      final double ratio;
      if (options.getBool(ITEM_AR_CAGE_USE_VIEW) && getGraph2DView() != null) {
        Rectangle visibleRect = getGraph2DView().getVisibleRect();
        ratio = visibleRect.getWidth()/visibleRect.getHeight();
      } else {
        ratio = options.getDouble(ITEM_CAGE_RATIO);
      }
      
      organic.setOutputRestriction(OutputRestriction.createAspectRatioRestriction(ratio));
      organic.setComponentLayouterEnabled(true);
      ((ComponentLayouter) organic.getComponentLayouter()).setPreferredLayoutSize(ratio * 100, 100);
    } else if (VALUE_OUTPUT_ELLIPTICAL_CAGE.equals(restrictOutput)) {
      final double x;
      final double y;
      final double w;
      final double h;
      if (options.getBool(ITEM_ELL_CAGE_USE_VIEW) && getGraph2DView() != null) {
        Rectangle visibleRect = getGraph2DView().getVisibleRect();
        x = visibleRect.x;
        y = visibleRect.y;
        w = visibleRect.width;
        h = visibleRect.height;
      } else {
        x = options.getDouble(ITEM_ELLIPTICAL_CAGE_X);
        y = options.getDouble(ITEM_ELLIPTICAL_CAGE_Y);
        w = options.getDouble(ITEM_ELLIPTICAL_CAGE_WIDTH);
        h = options.getDouble(ITEM_ELLIPTICAL_CAGE_HEIGHT);
      }
      
      organic.setOutputRestriction(OutputRestriction.createEllipticalCageRestriction(x, y, w, h));
      organic.setComponentLayouterEnabled(false);
    }
  }

  private boolean isGroupingNodes(Graph2D graph, String policy) {
    return !VALUE_IGNORE_GROUPS.equals(policy) && HierarchyManager.containsGroupNodes(graph);
  }
}
