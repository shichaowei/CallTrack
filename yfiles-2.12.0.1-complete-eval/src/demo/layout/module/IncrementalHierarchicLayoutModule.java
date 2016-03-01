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

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YPoint;
import y.layout.LabelLayoutConstants;
import y.layout.LabelRanking;
import y.layout.OrientationLayouter;
import y.layout.PreferredPlacementDescriptor;
import y.layout.hierarchic.AsIsLayerer;
import y.layout.hierarchic.BFSLayerer;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.HierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.OldLayererWrapper;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.hierarchic.incremental.TopLevelGroupToSwimlaneStage;
import y.layout.labeling.GreedyMISLabeling;
import y.option.ConstraintManager;
import y.option.ConstraintManager.Condition;
import y.option.DoubleOptionItem;
import y.option.EnumOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.hierarchic.IncrementalHierarchicLayouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html#incremental_hierarchical_stages">Section Applicable Layout Stages</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_advanced_features.html#layout_advanced_features">Section Advanced Layout Concepts</a> in the yFiles for Java Developer's Guide
 */
public class IncrementalHierarchicLayoutModule extends LayoutModule {
  //// Module 'Incremental Hierarchic Layout'
  protected static final String MODULE_INCREMENTAL_HIERARCHIC = "INCREMENTAL_HIERARCHIC";

  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String TITLE_INTERACTION = "INTERACTION";
  protected static final String ITEM_SELECTED_ELEMENTS_INCREMENTALLY = "SELECTED_ELEMENTS_INCREMENTALLY";
  protected static final String ITEM_USE_DRAWING_AS_SKETCH = "USE_DRAWING_AS_SKETCH";
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String ITEM_LAYOUT_COMPONENTS_SEPARATELY = "LAYOUT_COMPONENTS_SEPARATELY";
  protected static final String ITEM_SYMMETRIC_PLACEMENT = "SYMMETRIC_PLACEMENT";
  protected static final String ITEM_MAXIMAL_DURATION = "MAXIMAL_DURATION";
  protected static final String TITLE_MINIMUM_DISTANCES = "MINIMUM_DISTANCES";
  protected static final String ITEM_NODE_TO_NODE_DISTANCE = "NODE_TO_NODE_DISTANCE";
  protected static final String ITEM_NODE_TO_EDGE_DISTANCE = "NODE_TO_EDGE_DISTANCE";
  protected static final String ITEM_EDGE_TO_EDGE_DISTANCE = "EDGE_TO_EDGE_DISTANCE";
  protected static final String ITEM_MINIMUM_LAYER_DISTANCE = "MINIMUM_LAYER_DISTANCE";

  //// Section 'Edges'
  protected static final String SECTION_EDGE_SETTINGS = "EDGE_SETTINGS";
  //-
  protected static final String ITEM_EDGE_ROUTING = "EDGE_ROUTING";
  protected static final String VALUE_EDGE_ROUTING_ORTHOGONAL = "EDGE_ROUTING_ORTHOGONAL";
  protected static final String VALUE_EDGE_ROUTING_POLYLINE = "EDGE_ROUTING_POLYLINE";
  protected static final String VALUE_EDGE_ROUTING_OCTILINEAR = "EDGE_ROUTING_OCTILINEAR";
  protected static final String ITEM_BACKLOOP_ROUTING = "BACKLOOP_ROUTING";
  protected static final String ITEM_BACKLOOP_ROUTING_SELFLOOPS = "BACKLOOP_ROUTING_SELFLOOPS";
  protected static final String ITEM_AUTOMATIC_EDGE_GROUPING_ENABLED = "AUTOMATIC_EDGE_GROUPING_ENABLED";
  protected static final String ITEM_MINIMUM_FIRST_SEGMENT_LENGTH = "MINIMUM_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_LAST_SEGMENT_LENGTH = "MINIMUM_LAST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_EDGE_LENGTH = "MINIMUM_EDGE_LENGTH";
  protected static final String ITEM_MINIMUM_EDGE_DISTANCE = "MINIMUM_EDGE_DISTANCE";
  protected static final String ITEM_MINIMUM_SLOPE = "MINIMUM_SLOPE";
  protected static final String ITEM_PC_OPTIMIZATION_ENABLED = "PC_OPTIMIZATION_ENABLED";
  protected static final String ITEM_EDGE_STRAIGHTENING_OPTIMIZATION_ENABLED = "EDGE_STRAIGHTENING_OPTIMIZATION_ENABLED";

  //// Section 'Layers'
  protected static final String SECTION_RANKS = "RANKS";
  // Section 'Layers' items
  protected static final String ITEM_RANKING_POLICY = "RANKING_POLICY";
  protected static final String VALUE_HIERARCHICAL_OPTIMAL = "HIERARCHICAL_OPTIMAL";
  protected static final String VALUE_HIERARCHICAL_TIGHT_TREE_HEURISTIC = "HIERARCHICAL_TIGHT_TREE_HEURISTIC";
  protected static final String VALUE_BFS_LAYERS = "BFS_LAYERS";
  protected static final String VALUE_FROM_SKETCH = "FROM_SKETCH";
  protected static final String VALUE_HIERARCHICAL_TOPMOST = "HIERARCHICAL_TOPMOST";
  protected static final String ITEM_LAYER_ALIGNMENT = "LAYER_ALIGNMENT";
  protected static final String VALUE_TOP = "TOP";
  protected static final String VALUE_CENTER = "CENTER";
  protected static final String VALUE_BOTTOM = "BOTTOM";
  protected static final String ITEM_COMPONENT_ARRANGEMENT_POLICY = "COMPONENT_ARRANGEMENT_POLICY";
  protected static final String VALUE_POLICY_COMPACT = "POLICY_COMPACT";
  protected static final String VALUE_POLICY_TOPMOST = "POLICY_TOPMOST";
  protected static final String TITLE_FROM_SKETCH_PROPERTIES = "FROM_SKETCH_PROPERTIES";
  protected static final String ITEM_SCALE = "SCALE";
  protected static final String ITEM_HALO = "HALO";
  protected static final String ITEM_MINIMUM_SIZE = "MINIMUM_SIZE";
  protected static final String ITEM_MAXIMUM_SIZE = "MAXIMUM_SIZE";
  protected static final String ITEM_NODE_COMPACTION_ENABLED = "NODE_COMPACTION_ENABLED";

  //// Section 'Labeling'
  protected static final String SECTION_LABELING = "LABELING";
  // Section 'Labeling' items
  protected static final String TITLE_NODE_PROPERTIES = "NODE_PROPERTIES";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  protected static final String TITLE_EDGE_PROPERTIES = "EDGE_PROPERTIES";
  protected static final String ITEM_EDGE_LABELING = "EDGE_LABELING";
  protected static final String VALUE_EDGE_LABELING_NONE = "EDGE_LABELING_NONE";
  protected static final String VALUE_EDGE_LABELING_GENERIC = "EDGE_LABELING_GENERIC";
  protected static final String VALUE_EDGE_LABELING_HIERARCHIC = "EDGE_LABELING_HIERARCHIC";
  protected static final String ITEM_EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  protected static final String VALUE_EDGE_LABEL_MODEL_BEST = "EDGE_LABEL_MODEL_BEST";
  protected static final String VALUE_EDGE_LABEL_MODEL_AS_IS = "EDGE_LABEL_MODEL_AS_IS";
  protected static final String VALUE_EDGE_LABEL_MODEL_CENTER_SLIDER = "EDGE_LABEL_MODEL_CENTER_SLIDER";
  protected static final String VALUE_EDGE_LABEL_MODEL_SIDE_SLIDER = "EDGE_LABEL_MODEL_SIDE_SLIDER";
  protected static final String VALUE_EDGE_LABEL_MODEL_FREE = "EDGE_LABEL_MODEL_FREE";
  protected static final String ITEM_COMPACT_EDGE_LABEL_PLACEMENT = "COMPACT_EDGE_LABEL_PLACEMENT";

  //// Section 'Grouping'
  protected static final String SECTION_GROUPING = "GROUPING";
  // Section 'Grouping' items
  protected static final String ITEM_GROUP_LAYERING_STRATEGY = "GROUP_LAYERING_STRATEGY";
  protected static final String VALUE_GLOBAL_LAYERING = "GLOBAL_LAYERING";
  protected static final String VALUE_RECURSIVE_LAYERING = "RECURSIVE_LAYERING";
  protected static final String ITEM_GROUP_ENABLE_COMPACTION = "GROUP_ENABLE_COMPACTION";
  protected static final String ITEM_GROUP_ALIGNMENT = "GROUP_ALIGNMENT";
  protected static final String VALUE_GROUP_ALIGN_TOP = "GROUP_ALIGN_TOP";
  protected static final String VALUE_GROUP_ALIGN_CENTER = "GROUP_ALIGN_CENTER";
  protected static final String VALUE_GROUP_ALIGN_BOTTOM = "GROUP_ALIGN_BOTTOM";
  protected static final String ITEM_GROUP_HORIZONTAL_COMPACTION = "GROUP_HORIZONTAL_COMPACTION";
  protected static final String VALUE_GROUP_HORIZONTAL_COMPACTION_NONE = "GROUP_HORIZONTAL_COMPACTION_NONE";
  protected static final String VALUE_GROUP_HORIZONTAL_COMPACTION_MAX = "GROUP_HORIZONTAL_COMPACTION_MAX";
  
  //// Section 'Swimlanes'
  protected static final String SECTION_SWIMLANES = "SWIMLANES";
  // Section 'Swimlanes' items
  protected static final String ITEM_TREAT_ROOT_GROUPS_AS_SWIMLANES = "TREAT_ROOT_GROUPS_AS_SWIMLANES";
  protected static final String ITEM_USE_ORDER_FROM_SKETCH = "USE_ORDER_FROM_SKETCH";
  protected static final String ITEM_SWIMLANE_SPACING = "SWIMLANE_SPACING";

  //// Section 'Grid'
  private static final String SECTION_GRID = "GRID";
  // Section 'Grid' items
  private static final String ITEM_GRID_ENABLED = "GRID_ENABLED";
  private static final String ITEM_GRID_SPACING = "GRID_SPACING";
  private static final String ITEM_GRID_PORT_ASSIGNMENT = "GRID_PORT_ASSIGNMENT";
  private static final String VALUE_GRID_PORT_ASSIGNMENT_DEFAULT = "GRID_PORT_ASSIGNMENT_DEFAULT";
  private static final String VALUE_GRID_PORT_ASSIGNMENT_ON_GRID = "GRID_PORT_ASSIGNMENT_ON_GRID";
  private static final String VALUE_GRID_PORT_ASSIGNMENT_ON_SUBGRID = "GRID_PORT_ASSIGNMENT_ON_SUBGRID";
  
  /**
   * Creates an instance of this module.
   */
  public IncrementalHierarchicLayoutModule() {
    super(MODULE_INCREMENTAL_HIERARCHIC);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Group 'Interactive Settings'
    final OptionGroup interactionGroup = new OptionGroup();
    interactionGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_INTERACTION);
    // Populate group
    final OptionItem itemSelectedElementsIncrementally = interactionGroup.addItem(
        options.addBool(ITEM_SELECTED_ELEMENTS_INCREMENTALLY, false));
    final OptionItem itemUseDrawingAsSketch = interactionGroup.addItem(
        options.addBool(ITEM_USE_DRAWING_AS_SKETCH, false));

    // General settings
    // Populate section
    options.addEnum(ITEM_ORIENTATION, new String[]{
        VALUE_TOP_TO_BOTTOM,
        VALUE_LEFT_TO_RIGHT,
        VALUE_BOTTOM_TO_TOP,
        VALUE_RIGHT_TO_LEFT
    }, 0);
    options.addBool(ITEM_LAYOUT_COMPONENTS_SEPARATELY, false);
    final OptionItem itemSymmetricPlacement = options.addBool(ITEM_SYMMETRIC_PLACEMENT, true);
    options.addInt(ITEM_MAXIMAL_DURATION, 5);

    // Group 'Minimum Distances'
    final OptionGroup distancesGroup = new OptionGroup();
    distancesGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_MINIMUM_DISTANCES);
    // Populate group
    distancesGroup.addItem(options.addDouble(ITEM_NODE_TO_NODE_DISTANCE, 30.0d));
    distancesGroup.addItem(options.addDouble(ITEM_NODE_TO_EDGE_DISTANCE, 15.0d));
    distancesGroup.addItem(options.addDouble(ITEM_EDGE_TO_EDGE_DISTANCE, 15.0d));
    distancesGroup.addItem(options.addDouble(ITEM_MINIMUM_LAYER_DISTANCE, 10.0d));

    //// Section 'Edges'
    options.useSection(SECTION_EDGE_SETTINGS);
    // Populate section
    final EnumOptionItem itemEdgeRouting = options.addEnum(ITEM_EDGE_ROUTING, new String[]{
        VALUE_EDGE_ROUTING_ORTHOGONAL,
        VALUE_EDGE_ROUTING_POLYLINE,
        VALUE_EDGE_ROUTING_OCTILINEAR
    }, 0);
    final OptionItem itemBackloopRouting = options.addBool(ITEM_BACKLOOP_ROUTING, false);
    final OptionItem itemBackloopRoutingSelfloops = options.addBool(ITEM_BACKLOOP_ROUTING_SELFLOOPS, false);
    options.addBool(ITEM_AUTOMATIC_EDGE_GROUPING_ENABLED, false);
    options.addDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH, 10.0d);
    options.addDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH, 15.0d);
    options.addDouble(ITEM_MINIMUM_EDGE_LENGTH, 20.0d);
    options.addDouble(ITEM_MINIMUM_EDGE_DISTANCE, 15.0d);
    options.addBool(ITEM_PC_OPTIMIZATION_ENABLED, false);
    final OptionItem itemEdgeStraighteningOptimization =
        options.addBool(ITEM_EDGE_STRAIGHTENING_OPTIMIZATION_ENABLED, false);
    final OptionItem itemMinimumSlope = options.addDouble(ITEM_MINIMUM_SLOPE, 0.25d, 0.0d, 5.0d, 2);
    // Enable items on specific settings
    optionConstraints.setEnabledOnValueEquals(itemSymmetricPlacement, Boolean.FALSE,
        itemEdgeStraighteningOptimization);
    optionConstraints.setEnabledOnValueEquals(itemBackloopRouting, Boolean.TRUE, itemBackloopRoutingSelfloops);
    optionConstraints.setEnabledOnValueEquals(itemEdgeRouting, VALUE_EDGE_ROUTING_POLYLINE, itemMinimumSlope);
 
    //// Section 'Layers'
    options.useSection(SECTION_RANKS);
    // Populate section
    final EnumOptionItem itemRankingPolicy = options.addEnum(ITEM_RANKING_POLICY, new String[]{
        VALUE_HIERARCHICAL_OPTIMAL,
        VALUE_HIERARCHICAL_TIGHT_TREE_HEURISTIC,
        VALUE_BFS_LAYERS,
        VALUE_FROM_SKETCH,
        VALUE_HIERARCHICAL_TOPMOST
    }, 0);
    options.addEnum(ITEM_LAYER_ALIGNMENT, new String[]{
        VALUE_TOP,
        VALUE_CENTER,
        VALUE_BOTTOM
    }, 1);
    options.addBool(ITEM_NODE_COMPACTION_ENABLED, false);
    options.addEnum(ITEM_COMPONENT_ARRANGEMENT_POLICY, new String[]{
        VALUE_POLICY_COMPACT,
        VALUE_POLICY_TOPMOST
    }, 1);
    
    // Group 'From Sketch Settings'
    final OptionGroup sketchPropertyGroup = new OptionGroup();
    sketchPropertyGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_FROM_SKETCH_PROPERTIES);
    // Populate group
    sketchPropertyGroup.addItem(options.addDouble(ITEM_SCALE, 1.0d, 0.0d, 5.0d, 1));
    sketchPropertyGroup.addItem(options.addDouble(ITEM_HALO, 0.0d));
    final OptionItem itemMinimumSize = sketchPropertyGroup.addItem(options.addDouble(ITEM_MINIMUM_SIZE, 0.0d));
    itemMinimumSize.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
    final OptionItem itemMaximumSize = sketchPropertyGroup.addItem(options.addDouble(ITEM_MAXIMUM_SIZE, 1000.0d));
    itemMaximumSize.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
    // Enable/disable items depending on specific values
    Condition c =
        optionConstraints.createConditionValueEquals(itemUseDrawingAsSketch, Boolean.FALSE).and(
            optionConstraints.createConditionValueEquals(itemSelectedElementsIncrementally, Boolean.FALSE));
    optionConstraints.setEnabledOnCondition(c, itemRankingPolicy);
    c = c.inverse().or(optionConstraints.createConditionValueEquals(itemRankingPolicy, VALUE_FROM_SKETCH));
    optionConstraints.setEnabledOnCondition(c, sketchPropertyGroup);

    //// Section 'Labeling'
    options.useSection(SECTION_LABELING);
    // Group 'Node settings'
    final OptionGroup nodePropertyGroup = new OptionGroup();
    nodePropertyGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_NODE_PROPERTIES);
    // Populate group
    nodePropertyGroup.addItem(options.addBool(ITEM_CONSIDER_NODE_LABELS, true));
    
    // Group 'Edge settings'
    final OptionGroup edgePropertyGroup = new OptionGroup();
    edgePropertyGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_EDGE_PROPERTIES);
    // Populate group
    final EnumOptionItem itemEdgeLabeling = options.addEnum(ITEM_EDGE_LABELING, new String[]{
        VALUE_EDGE_LABELING_NONE,
        VALUE_EDGE_LABELING_GENERIC,
        VALUE_EDGE_LABELING_HIERARCHIC
    }, 0);
    edgePropertyGroup.addItem(itemEdgeLabeling);
    final OptionItem itemEdgeLabelModel = edgePropertyGroup.addItem(
        options.addEnum(ITEM_EDGE_LABEL_MODEL, new String[]{
            VALUE_EDGE_LABEL_MODEL_BEST,
            VALUE_EDGE_LABEL_MODEL_AS_IS,
            VALUE_EDGE_LABEL_MODEL_CENTER_SLIDER,
            VALUE_EDGE_LABEL_MODEL_SIDE_SLIDER,
            VALUE_EDGE_LABEL_MODEL_FREE,
        }, 0));
    final OptionItem itemEdgeLabelPlacement = options.addBool(ITEM_COMPACT_EDGE_LABEL_PLACEMENT, true);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemEdgeLabeling, VALUE_EDGE_LABELING_NONE, itemEdgeLabelModel, true);
    optionConstraints.setEnabledOnValueEquals(itemEdgeLabeling, VALUE_EDGE_LABELING_HIERARCHIC,
        edgePropertyGroup.addItem(itemEdgeLabelPlacement));

    //// Section 'Grouping'
    options.useSection(SECTION_GROUPING);
    // Populate section
    final EnumOptionItem itemGroupLayeringStrategy = options.addEnum(ITEM_GROUP_LAYERING_STRATEGY, new String[]{
        VALUE_GLOBAL_LAYERING,
        VALUE_RECURSIVE_LAYERING
    }, 0);
    final OptionItem itemGroupEnableCompaction = options.addBool(ITEM_GROUP_ENABLE_COMPACTION, true);
    final EnumOptionItem itemGroupAlignment = options.addEnum(ITEM_GROUP_ALIGNMENT, new String[]{
        VALUE_GROUP_ALIGN_TOP,
        VALUE_GROUP_ALIGN_CENTER,
        VALUE_GROUP_ALIGN_BOTTOM
    }, 0);
    options.addEnum(ITEM_GROUP_HORIZONTAL_COMPACTION, new String[]{
        VALUE_GROUP_HORIZONTAL_COMPACTION_NONE,
        VALUE_GROUP_HORIZONTAL_COMPACTION_MAX
    }, 1);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseDrawingAsSketch, Boolean.FALSE, itemGroupLayeringStrategy);
    optionConstraints.setEnabledOnValueEquals(itemGroupLayeringStrategy, VALUE_RECURSIVE_LAYERING, itemGroupEnableCompaction);
    optionConstraints.setEnabledOnValueEquals(itemGroupLayeringStrategy, VALUE_RECURSIVE_LAYERING, itemGroupAlignment);
    optionConstraints.setEnabledOnCondition(
        optionConstraints.createConditionValueEquals(itemGroupLayeringStrategy, VALUE_RECURSIVE_LAYERING).and(
            optionConstraints.createConditionValueEquals(itemGroupEnableCompaction, Boolean.TRUE).inverse()
        ), itemGroupAlignment);
    optionConstraints.setEnabledOnCondition(
        optionConstraints.createConditionValueEquals(itemGroupLayeringStrategy, VALUE_RECURSIVE_LAYERING).and(
            optionConstraints.createConditionValueEquals(itemUseDrawingAsSketch, Boolean.FALSE)
        ), itemGroupEnableCompaction);
    
    //// Section 'Swimlanes'
    options.useSection(SECTION_SWIMLANES);
    // Populate section
    final OptionItem swimlaneOption = options.addBool(ITEM_TREAT_ROOT_GROUPS_AS_SWIMLANES, false);
    final OptionItem fromSketchOption = options.addBool(ITEM_USE_ORDER_FROM_SKETCH, false);
    final OptionItem spacingOption = options.addDouble(ITEM_SWIMLANE_SPACING, 0.0d);
    spacingOption.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(swimlaneOption, Boolean.TRUE, fromSketchOption);
    optionConstraints.setEnabledOnValueEquals(swimlaneOption, Boolean.TRUE, spacingOption);

    //// Section 'Grid'
    options.useSection(SECTION_GRID);
    // Populate section
    final OptionItem grid = options.addBool(ITEM_GRID_ENABLED, false);
    final OptionItem gridSpacing = options.addDouble(ITEM_GRID_SPACING, 10, 1, 100, 1);
    final OptionItem portAssignment = options.addEnum(ITEM_GRID_PORT_ASSIGNMENT, new String[]{
        VALUE_GRID_PORT_ASSIGNMENT_DEFAULT,
        VALUE_GRID_PORT_ASSIGNMENT_ON_GRID,
        VALUE_GRID_PORT_ASSIGNMENT_ON_SUBGRID,
    }, 0);
    optionConstraints.setEnabledOnValueEquals(grid, Boolean.TRUE, gridSpacing);
    optionConstraints.setEnabledOnValueEquals(grid, Boolean.TRUE, portAssignment);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final IncrementalHierarchicLayouter hierarchic = new IncrementalHierarchicLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(hierarchic, options);

    final Graph2D graph = getGraph2D();

    final boolean incrementalLayout = options.getBool(ITEM_SELECTED_ELEMENTS_INCREMENTALLY);
    final boolean selectedElements = graph.selectedEdges().ok() || graph.selectedNodes().ok();
    if (incrementalLayout && selectedElements) {
      // mark incremental elements if required
      // create storage for both nodes and edges
      final DataMap incrementalElements = Maps.createHashedDataMap();
      
      final IncrementalHintsFactory ihf = hierarchic.createIncrementalHintsFactory();

      final HierarchyManager hm = graph.getHierarchyManager();
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        if (hm != null && hm.isGroupNode(n)) {
          incrementalElements.set(n, ihf.createIncrementalGroupHint(n));
        } else {
          incrementalElements.set(n, ihf.createLayerIncrementallyHint(n));
        }
      }

      for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
        incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
      }
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
      graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
    }

    final boolean gridEnabled = options.getBool(ITEM_GRID_ENABLED);
    if (gridEnabled) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY);

      final String portAssignment = options.getString(ITEM_GRID_PORT_ASSIGNMENT);
      final byte gridPortAssignment;
      if (VALUE_GRID_PORT_ASSIGNMENT_ON_GRID.equals(portAssignment)) {
        gridPortAssignment = NodeLayoutDescriptor.PORT_ASSIGNMENT_ON_GRID;
      } else if (VALUE_GRID_PORT_ASSIGNMENT_ON_SUBGRID.equals(portAssignment)) {
        gridPortAssignment = NodeLayoutDescriptor.PORT_ASSIGNMENT_ON_SUBGRID;
      } else {
        gridPortAssignment = NodeLayoutDescriptor.PORT_ASSIGNMENT_DEFAULT;
      }

      final NodeLayoutDescriptor nld = hierarchic.getNodeLayoutDescriptor();
      graph.addDataProvider(HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY, new DataProviderAdapter() {
        public Object get(Object dataHolder) {
          if (dataHolder instanceof Node) {
            final Node node = (Node) dataHolder;
            // copy descriptor to keep all settings for this node
            final NodeLayoutDescriptor descriptor = new NodeLayoutDescriptor();
            descriptor.setLayerAlignment(nld.getLayerAlignment());
            descriptor.setMinimumDistance(nld.getMinimumDistance());
            descriptor.setMinimumLayerHeight(nld.getMinimumLayerHeight());
            descriptor.setNodeLabelMode(nld.getNodeLabelMode());
            // anchor nodes on grid according to their alignment within the layer
            descriptor.setGridReference(new YPoint(0.0, (nld.getLayerAlignment() - 0.5) * graph.getHeight(node)));
            descriptor.setPortAssignment(gridPortAssignment);
            return descriptor;
          }
          return null;
        }
      });
    }
    
    prepareGraph(graph, options);
    try {
      // launch layouter in buffered mode
      launchLayouter(hierarchic);
    } finally {
      restoreGraph(graph, options);

      if (gridEnabled) {
        // remove the data providers set by this module by restoring the initial state
        restoreDataProvider(graph, HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY);
      }

      if (incrementalLayout && selectedElements) {
        // remove the data providers set by this module by restoring the initial state
        restoreDataProvider(graph, IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
      }
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
    if (options.getString(ITEM_RANKING_POLICY).equals(VALUE_BFS_LAYERS)) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, BFSLayerer.CORE_NODES);
      graph.addDataProvider(BFSLayerer.CORE_NODES, Selections.createSelectionNodeMap(graph));
    }
    final String el = options.getString(ITEM_EDGE_LABELING);
    if (!el.equals(VALUE_EDGE_LABELING_NONE)) {
      setupEdgeLabelModel(graph, el, options.getString(ITEM_EDGE_LABEL_MODEL));
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    if (options.getString(ITEM_RANKING_POLICY).equals(VALUE_BFS_LAYERS)) {
      // remove the data providers set by this module by restoring the initial state
      restoreDataProvider(graph, BFSLayerer.CORE_NODES);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2D</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param hierarchic the <code>IncrementalHierarchicLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final IncrementalHierarchicLayouter hierarchic, final OptionHandler options) {
    final Graph2D graph = getGraph2D();

    boolean incrementalLayout = options.getBool(ITEM_SELECTED_ELEMENTS_INCREMENTALLY);
    boolean selectedElements = graph.selectedEdges().ok() || graph.selectedNodes().ok();
    // configure the mode
    if ((incrementalLayout && selectedElements) || options.getBool(ITEM_USE_DRAWING_AS_SKETCH)) {
      hierarchic.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    } else {
      hierarchic.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    }
    
    // cast to implementation simplex
    final SimplexNodePlacer np = (SimplexNodePlacer) hierarchic.getNodePlacer();
    np.setBaryCenterModeEnabled(options.getBool(ITEM_SYMMETRIC_PLACEMENT));
    np.setEdgeStraighteningOptimizationEnabled(
        options.getBool(ITEM_EDGE_STRAIGHTENING_OPTIMIZATION_ENABLED));

    if (VALUE_GROUP_HORIZONTAL_COMPACTION_NONE.equals(options.getString(ITEM_GROUP_HORIZONTAL_COMPACTION))) {
      np.setGroupCompactionStrategy(SimplexNodePlacer.GROUP_COMPACTION_NONE);
    } else if (VALUE_GROUP_HORIZONTAL_COMPACTION_MAX.equals(options.getString(ITEM_GROUP_HORIZONTAL_COMPACTION))) {
      np.setGroupCompactionStrategy(SimplexNodePlacer.GROUP_COMPACTION_MAX);
    }

    hierarchic.setComponentLayouterEnabled(options.getBool(ITEM_LAYOUT_COMPONENTS_SEPARATELY));
    hierarchic.setMinimumLayerDistance(options.getDouble(ITEM_MINIMUM_LAYER_DISTANCE));
    hierarchic.setNodeToEdgeDistance(options.getDouble(ITEM_NODE_TO_EDGE_DISTANCE));
    hierarchic.setNodeToNodeDistance(options.getDouble(ITEM_NODE_TO_NODE_DISTANCE));
    hierarchic.setEdgeToEdgeDistance(options.getDouble(ITEM_EDGE_TO_EDGE_DISTANCE));
    hierarchic.setAutomaticEdgeGroupingEnabled(options.getBool(ITEM_AUTOMATIC_EDGE_GROUPING_ENABLED));

    final EdgeLayoutDescriptor eld = hierarchic.getEdgeLayoutDescriptor();
    if (options.get(ITEM_EDGE_ROUTING) == VALUE_EDGE_ROUTING_OCTILINEAR) {
      eld.setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_OCTILINEAR));
    } else if (options.get(ITEM_EDGE_ROUTING) == VALUE_EDGE_ROUTING_POLYLINE) {
      eld.setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_POLYLINE));
    } else {
      eld.setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));
    }

    eld.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH));
    eld.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH));
    eld.setMinimumDistance(options.getDouble(ITEM_MINIMUM_EDGE_DISTANCE));
    eld.setMinimumLength(options.getDouble(ITEM_MINIMUM_EDGE_LENGTH));
    eld.setMinimumSlope(options.getDouble(ITEM_MINIMUM_SLOPE));
    eld.setSourcePortOptimizationEnabled(options.getBool(ITEM_PC_OPTIMIZATION_ENABLED));
    eld.setTargetPortOptimizationEnabled(options.getBool(ITEM_PC_OPTIMIZATION_ENABLED));

    final NodeLayoutDescriptor nld = hierarchic.getNodeLayoutDescriptor();
    nld.setMinimumDistance(Math.min(hierarchic.getNodeToNodeDistance(), hierarchic.getNodeToEdgeDistance()));
    nld.setMinimumLayerHeight(0);
    if (options.get(ITEM_LAYER_ALIGNMENT).equals(VALUE_TOP)) {
      nld.setLayerAlignment(0.0);
    } else if (options.get(ITEM_LAYER_ALIGNMENT).equals(VALUE_CENTER)) {
      nld.setLayerAlignment(0.5);
    } else if (options.get(ITEM_LAYER_ALIGNMENT).equals(VALUE_BOTTOM)) {
      nld.setLayerAlignment(1.0);
    }

    if (options.get(ITEM_ORIENTATION).equals(VALUE_TOP_TO_BOTTOM)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_LEFT_TO_RIGHT)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_BOTTOM_TO_TOP)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_RIGHT_TO_LEFT)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    }

    configureLabeling(hierarchic, options);

    if (options.getBool(ITEM_CONSIDER_NODE_LABELS)) {
      hierarchic.setConsiderNodeLabelsEnabled(true);
      hierarchic.getNodeLayoutDescriptor().setNodeLabelMode(NodeLayoutDescriptor.NODE_LABEL_MODE_CONSIDER_FOR_DRAWING);
    } else {
      hierarchic.setConsiderNodeLabelsEnabled(false);
    }

    final String rp = options.getString(ITEM_RANKING_POLICY);
    if (rp.equals(VALUE_FROM_SKETCH)) {
      hierarchic.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_FROM_SKETCH);
    } else if (rp.equals(VALUE_HIERARCHICAL_OPTIMAL)) {
      hierarchic.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_OPTIMAL);
    } else if (rp.equals(VALUE_HIERARCHICAL_TIGHT_TREE_HEURISTIC)) {
      hierarchic.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TIGHT_TREE);
    } else if (rp.equals(VALUE_HIERARCHICAL_TOPMOST)) {
      hierarchic.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST);
    } else if (rp.equals(VALUE_BFS_LAYERS)) {
      hierarchic.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_BFS);
    }

    if (options.getString(ITEM_COMPONENT_ARRANGEMENT_POLICY).equals(VALUE_POLICY_COMPACT)) {
      hierarchic.setComponentArrangementPolicy(IncrementalHierarchicLayouter.COMPONENT_ARRANGEMENT_COMPACT);
    } else {
      hierarchic.setComponentArrangementPolicy(IncrementalHierarchicLayouter.COMPONENT_ARRANGEMENT_TOPMOST);
    }

    ((SimplexNodePlacer) hierarchic.getNodePlacer()).setNodeCompactionEnabled(options.getBool(ITEM_NODE_COMPACTION_ENABLED));

    //configure AsIsLayerer
    Object layerer = (hierarchic.getLayoutMode() == IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH) ?
        hierarchic.getFromScratchLayerer() : hierarchic.getFixedElementsLayerer();
    if (layerer instanceof OldLayererWrapper) {
      layerer = ((OldLayererWrapper) layerer).getOldLayerer();
    }
    if (layerer instanceof AsIsLayerer) {
      AsIsLayerer ail = (AsIsLayerer) layerer;
      ail.setNodeHalo(options.getDouble(ITEM_HALO));
      ail.setNodeScalingFactor(options.getDouble(ITEM_SCALE));
      ail.setMinimumNodeSize(options.getDouble(ITEM_MINIMUM_SIZE));
      ail.setMaximumNodeSize(options.getDouble(ITEM_MAXIMUM_SIZE));
    }

    if (!options.getBool(ITEM_USE_DRAWING_AS_SKETCH) &&
        options.getString(ITEM_GROUP_LAYERING_STRATEGY).equals(VALUE_RECURSIVE_LAYERING)) {
      byte alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP;
      if (options.getString(ITEM_GROUP_ALIGNMENT).equals(VALUE_GROUP_ALIGN_CENTER)) {
        alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER;
      } else if (options.getString(ITEM_GROUP_ALIGNMENT).equals(VALUE_GROUP_ALIGN_BOTTOM)) {
        alignmentPolicy = IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM;
      }
      hierarchic.setGroupCompactionEnabled(options.getBool(ITEM_GROUP_ENABLE_COMPACTION));
      hierarchic.setGroupAlignmentPolicy(alignmentPolicy);
      hierarchic.setRecursiveGroupLayeringEnabled(true);
    } else {
      hierarchic.setRecursiveGroupLayeringEnabled(false);
    }

    if (options.getBool(SECTION_SWIMLANES, ITEM_TREAT_ROOT_GROUPS_AS_SWIMLANES)) {
      final TopLevelGroupToSwimlaneStage stage = new TopLevelGroupToSwimlaneStage();
      stage.setFromSketchSwimlaneOrderingEnabled(options.getBool(SECTION_SWIMLANES, ITEM_USE_ORDER_FROM_SKETCH));
      stage.setSpacing(options.getDouble(SECTION_SWIMLANES, ITEM_SWIMLANE_SPACING));
      hierarchic.appendStage(stage);
    }

    hierarchic.setBackloopRoutingEnabled(options.getBool(ITEM_BACKLOOP_ROUTING));
    hierarchic.setBackloopRoutingForSelfloopsEnabled(options.getBool(ITEM_BACKLOOP_ROUTING_SELFLOOPS));
    hierarchic.setMaximalDuration(options.getInt(ITEM_MAXIMAL_DURATION) * 1000);


    final boolean gridEnabled = options.getBool(ITEM_GRID_ENABLED);
    if (gridEnabled) {
      hierarchic.setGridSpacing(options.getDouble(ITEM_GRID_SPACING));
    }
  }

  private void configureLabeling(final IncrementalHierarchicLayouter hierarchic, final OptionHandler options) {
    final String el = options.getString(ITEM_EDGE_LABELING);
    if (!el.equals(VALUE_EDGE_LABELING_NONE)) {
      if (el.equals(VALUE_EDGE_LABELING_GENERIC)) {
        final GreedyMISLabeling la = new GreedyMISLabeling();
        la.setPlaceNodeLabels(false);
        la.setPlaceEdgeLabels(true);
        la.setAutoFlippingEnabled(true);
        la.setProfitModel(new LabelRanking());
        //cannot be set as label layouter (see note on method setConsiderNodeLabelsEnabled of IncrementalHierarchicLayouter)
        hierarchic.prependStage(la);
      } else if (el.equals(VALUE_EDGE_LABELING_HIERARCHIC)) {
        final boolean compactEdgeLabelPlacement = options.getBool(ITEM_COMPACT_EDGE_LABEL_PLACEMENT);
        if (hierarchic.getNodePlacer() instanceof SimplexNodePlacer) {
          ((SimplexNodePlacer) hierarchic.getNodePlacer()).setLabelCompactionEnabled(compactEdgeLabelPlacement);
        }
        hierarchic.setIntegratedEdgeLabelingEnabled(true);
      }
    } else {
      hierarchic.setIntegratedEdgeLabelingEnabled(false);
    }
  }

  private void setupEdgeLabelModel(final Graph2D graph, final String edgeLabeling, String edgeLabelModel) {
    if (edgeLabeling.equals(VALUE_EDGE_LABELING_NONE) || edgeLabelModel.equals(VALUE_EDGE_LABEL_MODEL_AS_IS)) {
      return; //nothing to do
    }

    if (edgeLabelModel.equals(VALUE_EDGE_LABEL_MODEL_BEST)) {
      if (edgeLabeling.equals(VALUE_EDGE_LABELING_GENERIC)) {
        edgeLabelModel = VALUE_EDGE_LABEL_MODEL_SIDE_SLIDER;
      } else if (edgeLabeling.equals(VALUE_EDGE_LABELING_HIERARCHIC)) {
        edgeLabelModel = VALUE_EDGE_LABEL_MODEL_FREE;
      }
    }

    byte model = EdgeLabel.SIDE_SLIDER;
    byte preferredSide = LabelLayoutConstants.PLACE_RIGHT_OF_EDGE;
    if (edgeLabelModel.equals(VALUE_EDGE_LABEL_MODEL_CENTER_SLIDER)) {
      model = EdgeLabel.CENTER_SLIDER;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    } else if (edgeLabelModel.equals(VALUE_EDGE_LABEL_MODEL_FREE)) {
      model = EdgeLabel.FREE;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    }

    assignLabelModel(graph, model, preferredSide, !edgeLabelModel.equals(VALUE_EDGE_LABEL_MODEL_FREE));
  }

  private void assignLabelModel(
      final Graph2D graph,
      final byte model,
      final byte preferredSide,
      final boolean setPreferredSize
  ) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        final EdgeLabel el = er.getLabel(i);
        el.setModel(model);
        if (setPreferredSize) {
          setPreferredSide(el, preferredSide);
        }
      }
    }
  }


  private void setPreferredSide(final EdgeLabel el, final byte preferredSide) {
    final PreferredPlacementDescriptor oldDescriptor =
            el.getPreferredPlacementDescriptor();
    if (oldDescriptor.getSideOfEdge() != preferredSide) {
      final PreferredPlacementDescriptor newDescriptor =
              new PreferredPlacementDescriptor(oldDescriptor);
      newDescriptor.setSideOfEdge(preferredSide);
      el.setPreferredPlacementDescriptor(newDescriptor);
    }
  }
}
