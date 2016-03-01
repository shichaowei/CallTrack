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

import y.layout.ComponentLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.circular.SingleCycleLayouter;
import y.layout.tree.BalloonLayouter;
import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.option.OptionItem;
import y.view.Graph2D;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.circular.CircularLayouter}.
 * 
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/circular_layouter.html#circular_layouter">Section Circular Layout</a> in the yFiles for Java Developer's Guide
 */
public class CircularLayoutModule extends LayoutModule {
  //// Module 'Circular Layout'
  protected static final String MODULE_CIRCULAR = "CIRCULAR";
  
  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_LAYOUT_STYLE = "LAYOUT_STYLE";
  protected static final String VALUE_BCC_COMPACT = "BCC_COMPACT";
  protected static final String VALUE_BCC_ISOLATED = "BCC_ISOLATED";
  protected static final String VALUE_CIRCULAR_CUSTOM_GROUPS = "CIRCULAR_CUSTOM_GROUPS";
  protected static final String VALUE_SINGLE_CYCLE = "SINGLE_CYCLE";
  protected static final String ITEM_ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  protected static final String ITEM_FROM_SKETCH = "FROM_SKETCH";
  protected static final String ITEM_HANDLE_NODE_LABELS = "HANDLE_NODE_LABELS";
  
  //// Section 'Partition'
  protected static final String SECTION_CYCLE = "CYCLE";
  // Section 'Partition' items
  private static final String ITEM_PARTITION_LAYOUT_STYLE = "PARTITION_LAYOUT_STYLE";
  private static final String VALUE_PARTITION_LAYOUTSTYLE_CYCLIC = "PARTITION_LAYOUTSTYLE_CYCLIC";
  private static final String VALUE_PARTITION_LAYOUTSTYLE_DISK = "PARTITION_LAYOUTSTYLE_DISK";
  private static final String VALUE_PARTITION_LAYOUTSTYLE_ORGANIC = "PARTITION_LAYOUTSTYLE_ORGANIC";
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_CHOOSE_RADIUS_AUTOMATICALLY = "CHOOSE_RADIUS_AUTOMATICALLY";
  protected static final String ITEM_FIXED_RADIUS = "FIXED_RADIUS";
  
  //// Section 'Tree'
  protected static final String SECTION_TREE = "TREE";
  // Section 'Tree' items
  protected static final String ITEM_PREFERRED_CHILD_WEDGE = "PREFERRED_CHILD_WEDGE";
  protected static final String ITEM_MINIMAL_EDGE_LENGTH = "MINIMAL_EDGE_LENGTH";
  protected static final String ITEM_MAXIMAL_DEVIATION_ANGLE = "MAXIMAL_DEVIATION_ANGLE";
  protected static final String ITEM_COMPACTNESS_FACTOR = "COMPACTNESS_FACTOR";
  protected static final String ITEM_MINIMAL_TREE_NODE_DISTANCE = "MINIMAL_TREE_NODE_DISTANCE";
  protected static final String ITEM_ALLOW_OVERLAPS = "ALLOW_OVERLAPS";
  protected static final String ITEM_PLACE_CHILDREN_ON_COMMON_RADIUS = "PLACE_CHILDREN_ON_COMMON_RADIUS";
  
  /**
   * Creates an instance of this module.
   */
  public CircularLayoutModule() {
    super(MODULE_CIRCULAR);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Defaults provider
    final CircularLayouter defaults = new CircularLayouter();
    final SingleCycleLayouter defaultsSC = defaults.getSingleCycleLayouter();
    final BalloonLayouter defaultsB = defaults.getBalloonLayouter();

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate section
    options.addEnum(ITEM_LAYOUT_STYLE, new String[]{
        VALUE_BCC_COMPACT,
        VALUE_BCC_ISOLATED,
        VALUE_CIRCULAR_CUSTOM_GROUPS,
        VALUE_SINGLE_CYCLE
    }, defaults.getLayoutStyle());
    options.addBool(ITEM_ACT_ON_SELECTION_ONLY, false);
    options.addBool(ITEM_FROM_SKETCH, false);
    options.addBool(ITEM_HANDLE_NODE_LABELS, false);

    //// Section 'Partition'
    options.useSection(SECTION_CYCLE);
    // Populate section
    final EnumOptionItem itemLayoutStyle = options.addEnum(ITEM_PARTITION_LAYOUT_STYLE, new String[]{
        VALUE_PARTITION_LAYOUTSTYLE_CYCLIC,
        VALUE_PARTITION_LAYOUTSTYLE_DISK,
        VALUE_PARTITION_LAYOUTSTYLE_ORGANIC
    }, defaults.getPartitionLayoutStyle());
    final OptionItem itemChooseRadiusAutomatically =
        options.addBool(ITEM_CHOOSE_RADIUS_AUTOMATICALLY, defaultsSC.getAutomaticRadius());
    final OptionItem itemFixedRadius = options.addInt(ITEM_FIXED_RADIUS, (int) defaultsSC.getFixedRadius(), 50, 800);
    final OptionItem itemMinimalNodeDistance =
        options.addInt(ITEM_MINIMAL_NODE_DISTANCE, (int) defaultsSC.getMinimalNodeDistance(), 0, 999);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemChooseRadiusAutomatically, Boolean.FALSE, itemFixedRadius);
    optionConstraints.setEnabledOnValueEquals(itemChooseRadiusAutomatically, Boolean.TRUE, itemMinimalNodeDistance);

    //// Section 'Tree'
    options.useSection(SECTION_TREE);
    // Populate section
    final OptionItem itemPreferredChildWedge =
        options.addInt(ITEM_PREFERRED_CHILD_WEDGE, defaultsB.getPreferredChildWedge(), 1, 359);
    final OptionItem itemMinimalEdgeLength =
        options.addInt(ITEM_MINIMAL_EDGE_LENGTH, defaultsB.getMinimalEdgeLength(), 5, 400);
    final OptionItem itemMaximalDeviationAngle =
        options.addInt(ITEM_MAXIMAL_DEVIATION_ANGLE, defaults.getMaximalDeviationAngle(), 10, 360);
    final OptionItem itemCompactnessFactor =
        options.addDouble(ITEM_COMPACTNESS_FACTOR, defaultsB.getCompactnessFactor(), 0.1, 0.9);
    final OptionItem itemMinimalTreeNodeDistance =
        options.addInt(ITEM_MINIMAL_TREE_NODE_DISTANCE, defaultsB.getMinimalNodeDistance(), 0, 100);
    final OptionItem itemAllowOverlaps = options.addBool(ITEM_ALLOW_OVERLAPS, defaultsB.getAllowOverlaps());
    final OptionItem itemPlaceChildrenCommonRadius = options.addBool(ITEM_PLACE_CHILDREN_ON_COMMON_RADIUS, true);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemMinimalTreeNodeDistance, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemPlaceChildrenCommonRadius, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemPreferredChildWedge, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemMinimalEdgeLength, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemMaximalDeviationAngle, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemCompactnessFactor, true);
    optionConstraints.setEnabledOnValueEquals(itemLayoutStyle, VALUE_SINGLE_CYCLE, itemAllowOverlaps, true);
    
    return options;
  }


  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final CircularLayouter circular = new CircularLayouter();

    final OptionHandler options = getOptionHandler();
    configure(circular, options);

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(circular);
    } finally {
      restoreGraph(graph, options);
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
    if (options.getString(ITEM_LAYOUT_STYLE).equals(VALUE_CIRCULAR_CUSTOM_GROUPS)) {
      //Set up grouping key for custom layout style
      //This acts as an adapter for grouping structure to circular grouping keys
      if (graph.getHierarchyManager() != null) {
        // backup existing data providers to prevent loss of user settings
        backupDataProvider(graph, CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY);
        graph.addDataProvider(CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY,
            graph.getHierarchyManager().getParentNodeIdDataProvider());
      }
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // remove the data providers set by this module by restoring the initial state
    if (options.getString(ITEM_LAYOUT_STYLE).equals(VALUE_CIRCULAR_CUSTOM_GROUPS) &&
        graph.getHierarchyManager() != null) {
      restoreDataProvider(graph, CircularLayouter.CIRCULAR_CUSTOM_GROUPS_DPKEY);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param circular the <code>CircularLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final CircularLayouter circular, final OptionHandler options) {
    ((ComponentLayouter) circular.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    if (options.getString(ITEM_LAYOUT_STYLE).equals(VALUE_BCC_COMPACT)) {
      circular.setLayoutStyle(CircularLayouter.BCC_COMPACT);
    } else if (options.getString(ITEM_LAYOUT_STYLE).equals(VALUE_BCC_ISOLATED)) {
      circular.setLayoutStyle(CircularLayouter.BCC_ISOLATED);
    } else if (options.getString(ITEM_LAYOUT_STYLE).equals(VALUE_CIRCULAR_CUSTOM_GROUPS)) {
      circular.setLayoutStyle(CircularLayouter.CIRCULAR_CUSTOM_GROUPS);
    } else {
      circular.setLayoutStyle(CircularLayouter.SINGLE_CYCLE);
    }

    circular.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));
    circular.setMaximalDeviationAngle(options.getInt(ITEM_MAXIMAL_DEVIATION_ANGLE));
    circular.setFromSketchModeEnabled(options.getBool(ITEM_FROM_SKETCH));
    circular.setPlaceChildrenOnCommonRadiusEnabled(options.getBool(ITEM_PLACE_CHILDREN_ON_COMMON_RADIUS));
    circular.setConsiderNodeLabelsEnabled(options.getBool(ITEM_HANDLE_NODE_LABELS));

    if (options.getString(ITEM_PARTITION_LAYOUT_STYLE).equals(VALUE_PARTITION_LAYOUTSTYLE_CYCLIC)) {
      circular.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_CYCLIC);
    } else if (options.getString(ITEM_PARTITION_LAYOUT_STYLE).equals(VALUE_PARTITION_LAYOUTSTYLE_DISK)) {
      circular.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_DISK);
    } else if (options.getString(ITEM_PARTITION_LAYOUT_STYLE).equals(VALUE_PARTITION_LAYOUTSTYLE_ORGANIC)) {
      circular.setPartitionLayoutStyle(CircularLayouter.PARTITION_LAYOUTSTYLE_ORGANIC);
    }

    final SingleCycleLayouter cl = circular.getSingleCycleLayouter();
    cl.setMinimalNodeDistance(options.getInt(ITEM_MINIMAL_NODE_DISTANCE));
    cl.setAutomaticRadius(options.getBool(ITEM_CHOOSE_RADIUS_AUTOMATICALLY));
    cl.setFixedRadius(options.getInt(ITEM_FIXED_RADIUS));

    final BalloonLayouter bl = circular.getBalloonLayouter();
    bl.setPreferredChildWedge(options.getInt(ITEM_PREFERRED_CHILD_WEDGE));
    bl.setMinimalEdgeLength(options.getInt(ITEM_MINIMAL_EDGE_LENGTH));
    bl.setCompactnessFactor(options.getDouble(ITEM_COMPACTNESS_FACTOR));
    bl.setAllowOverlaps(options.getBool(ITEM_ALLOW_OVERLAPS));
    bl.setMinimalNodeDistance(options.getInt(ITEM_MINIMAL_TREE_NODE_DISTANCE));
  }
}