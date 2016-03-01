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
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.layout.AbstractLayoutStage;
import y.layout.CanonicMultiStageLayouter;
import y.layout.ComponentLayouter;
import y.layout.EdgeLabelLayout;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.grouping.Grouping;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.StraightLineEdgeRouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.tree.ARTreeLayouter;
import y.layout.tree.HVTreeLayouter;
import y.layout.tree.TreeLayouter;
import y.layout.tree.TreeReductionStage;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.util.GraphHider;
import y.util.Maps;
import y.view.Graph2D;
import y.view.Graph2DView;

import java.awt.Dimension;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.tree.TreeLayouter},
 * {@link y.layout.tree.ARTreeLayouter} and {@link y.layout.tree.HVTreeLayouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/tree_layouter.html#tree_layouter">Section Tree Layout</a> in the yFiles for Java Developer's Guide
 */
public class TreeLayoutModule extends LayoutModule {

  //// Module 'Treelike Layout'
  protected static final String MODULE_TREE = "TREE";
  
  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_LAYOUT_STYLE = "LAYOUT_STYLE";
  protected static final String VALUE_DIRECTED = "DIRECTED";
  protected static final String VALUE_HV = "HV";
  protected static final String VALUE_AR = "AR";
  protected static final String ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES = "ROUTING_STYLE_FOR_NON_TREE_EDGES";
  protected static final String VALUE_ROUTE_ORGANIC = "ROUTE_ORGANIC";
  protected static final String VALUE_ROUTE_ORTHOGONAL = "ROUTE_ORTHOGONAL";
  protected static final String VALUE_ROUTE_STRAIGHTLINE = "ROUTE_STRAIGHTLINE";
  protected static final String ITEM_ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  
  //// Section 'Directed'
  protected static final String SECTION_DIRECTED = "DIRECTED";
  // Section 'Directed' items
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_MINIMAL_LAYER_DISTANCE = "MINIMAL_LAYER_DISTANCE";
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String ITEM_PORT_STYLE = "PORT_STYLE";
  protected static final String VALUE_NODE_CENTER_PORTS = "NODE_CENTER";
  protected static final String VALUE_BORDER_CENTER_PORTS = "BORDER_CENTER";
  protected static final String VALUE_BORDER_DISTRIBUTED_PORTS = "BORDER_DISTRIBUTED";
  protected static final String VALUE_PORT_CONSTRAINTS_AWARE = "PORT_CONSTRAINTS_AWARE";
  protected static final String ITEM_INTEGRATED_NODE_LABELING = "INTEGRATED_NODE_LABELING";
  protected static final String ITEM_INTEGRATED_EDGE_LABELING = "INTEGRATED_EDGE_LABELING";
  protected static final String ITEM_ORTHOGONAL_EDGE_ROUTING = "ORTHOGONAL_EDGE_ROUTING";
  protected static final String ITEM_BUS_ALIGNMENT = "BUS_ALIGNMENT";
  protected static final String ITEM_VERTICAL_ALIGNMENT = "VERTICAL_ALIGNMENT";
  protected static final String VALUE_ALIGNMENT_TOP = "ALIGNMENT_TOP";
  protected static final String VALUE_ALIGNMENT_CENTER = "ALIGNMENT_CENTER";
  protected static final String VALUE_ALIGNMENT_BOTTOM = "ALIGNMENT_BOTTOM";
  protected static final String ITEM_CHILD_PLACEMENT_POLICY = "CHILD_PLACEMENT_POLICY";
  protected static final String VALUE_SIBLINGS_ON_SAME_LAYER = "SIBLINGS_ON_SAME_LAYER";
  protected static final String VALUE_ALL_LEAVES_ON_SAME_LAYER = "ALL_LEAVES_ON_SAME_LAYER";
  protected static final String VALUE_LEAVES_STACKED = "LEAVES_STACKED";
  protected static final String VALUE_LEAVES_STACKED_LEFT = "LEAVES_STACKED_LEFT";
  protected static final String VALUE_LEAVES_STACKED_RIGHT = "LEAVES_STACKED_RIGHT";
  protected static final String ITEM_ENFORCE_GLOBAL_LAYERING = "ENFORCE_GLOBAL_LAYERING";
  
  //// Section 'Horizontal-Vertical'
  protected static final String SECTION_HV = "HV";
  // Section 'Horizontal-Vertical' items
  protected static final String ITEM_HORIZONTAL_SPACE_HV = "HORIZONTAL_SPACE";
  protected static final String ITEM_VERTICAL_SPACE_HV = "VERTICAL_SPACE";
  
  //// Section 'Compact'
  protected static final String SECTION_AR = "AR";
  // Section 'Compact' items
  protected static final String ITEM_HORIZONTAL_SPACE_AR = "HORIZONTAL_SPACE";
  protected static final String ITEM_VERTICAL_SPACE_AR = "VERTICAL_SPACE";
  protected static final String ITEM_BEND_DISTANCE = "BEND_DISTANCE";
  protected static final String ITEM_USE_VIEW_ASPECT_RATIO = "USE_VIEW_ASPECT_RATIO";
  protected static final String ITEM_ASPECT_RATIO = "ASPECT_RATIO";

  /**
   * Creates an instance of this module.
   */
  public TreeLayoutModule() {
    super(MODULE_TREE);
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
    final TreeLayouter defaultsTree = new TreeLayouter();
    final HVTreeLayouter defaultsHv = new HVTreeLayouter();
    final ARTreeLayouter defaultsAr = new ARTreeLayouter();
    ((ComponentLayouter) defaultsHv.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    ((ComponentLayouter) defaultsAr.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate section
    options.addEnum(ITEM_LAYOUT_STYLE, new String[]{
            VALUE_DIRECTED,
            VALUE_HV,
            VALUE_AR
    }, 0);
    options.addEnum(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES, new String[]{
        VALUE_ROUTE_ORGANIC,
        VALUE_ROUTE_ORTHOGONAL,
        VALUE_ROUTE_STRAIGHTLINE
    }, 0);
    options.addBool(ITEM_ACT_ON_SELECTION_ONLY, false);

    //// Section 'Directed'
    options.useSection(SECTION_DIRECTED);
    // Populate section
    options.addInt(ITEM_MINIMAL_NODE_DISTANCE, (int) defaultsTree.getMinimalNodeDistance(), 1, 100);
    options.addInt(ITEM_MINIMAL_LAYER_DISTANCE, (int) defaultsTree.getMinimalLayerDistance(), 10, 300);
    options.addEnum(ITEM_ORIENTATION, new String[]{
            VALUE_TOP_TO_BOTTOM,
            VALUE_LEFT_TO_RIGHT,
            VALUE_BOTTOM_TO_TOP,
            VALUE_RIGHT_TO_LEFT
    }, 0);
    options.addEnum(ITEM_PORT_STYLE, new String[]{
            VALUE_NODE_CENTER_PORTS,
            VALUE_BORDER_CENTER_PORTS,
            VALUE_BORDER_DISTRIBUTED_PORTS,
            VALUE_PORT_CONSTRAINTS_AWARE,
    }, 0);
    options.addBool(ITEM_INTEGRATED_NODE_LABELING, false);
    options.addBool(ITEM_INTEGRATED_EDGE_LABELING, false);
    final OptionItem itemEdgeRouting = options.addBool(ITEM_ORTHOGONAL_EDGE_ROUTING, false);
    final OptionItem itemBusAlignment = options.addEnum(ITEM_BUS_ALIGNMENT, new String[]{
        VALUE_ALIGNMENT_TOP,
        VALUE_ALIGNMENT_CENTER,
        VALUE_ALIGNMENT_BOTTOM,
    }, 1);
    options.addEnum(ITEM_VERTICAL_ALIGNMENT, new String[]{
        VALUE_ALIGNMENT_TOP,
        VALUE_ALIGNMENT_CENTER,
        VALUE_ALIGNMENT_BOTTOM,
    }, 1);
    final OptionItem itemChildPlacement = options.addEnum(ITEM_CHILD_PLACEMENT_POLICY, new String[]{
        VALUE_SIBLINGS_ON_SAME_LAYER,
        VALUE_ALL_LEAVES_ON_SAME_LAYER,
        VALUE_LEAVES_STACKED,
        VALUE_LEAVES_STACKED_LEFT,
        VALUE_LEAVES_STACKED_RIGHT
    }, 0);
    final OptionItem itemGlobalLayering = options.addBool(ITEM_ENFORCE_GLOBAL_LAYERING, false);
    // Enable/disable items depending on specific values
    final ConstraintManager.Condition conditionBusAlignment = 
        optionConstraints.createConditionValueEquals(itemEdgeRouting, Boolean.TRUE)
            .and(optionConstraints.createConditionValueEquals(itemGlobalLayering, Boolean.TRUE)
                .or(optionConstraints.createConditionValueEquals(itemChildPlacement, VALUE_ALL_LEAVES_ON_SAME_LAYER)));
    optionConstraints.setEnabledOnCondition(conditionBusAlignment, itemBusAlignment);

    //// Section 'Horizontal-Vertical'
    options.useSection(SECTION_HV);
    // Populate section
    options.addInt(ITEM_HORIZONTAL_SPACE_HV, (int) defaultsHv.getHorizontalSpace());
    options.addInt(ITEM_VERTICAL_SPACE_HV, (int) defaultsHv.getVerticalSpace());

    //// Section 'Compact'
    options.useSection(SECTION_AR);
    // Populate section
    options.addInt(ITEM_HORIZONTAL_SPACE_AR, (int) defaultsAr.getHorizontalSpace());
    options.addInt(ITEM_VERTICAL_SPACE_AR, (int) defaultsAr.getVerticalSpace());
    options.addInt(ITEM_BEND_DISTANCE, (int) defaultsAr.getBendDistance());
    final OptionItem itemUseViewAspectRatio = options.addBool(ITEM_USE_VIEW_ASPECT_RATIO, true);
    final OptionItem itemAspectRatio = options.addDouble(ITEM_ASPECT_RATIO, defaultsAr.getAspectRatio());
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseViewAspectRatio, Boolean.FALSE, itemAspectRatio);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final CanonicMultiStageLayouter layouter;
    
    final OptionHandler options = getOptionHandler();
    final Graph2D graph = getGraph2D();

    String style = options.getString(ITEM_LAYOUT_STYLE);
    if (VALUE_HV.equals(style)) {
      final HVTreeLayouter hv = new HVTreeLayouter();
      configure(hv, options);
      layouter = hv;
    } else if (VALUE_AR.equals(style)) {
      final ARTreeLayouter ar = new ARTreeLayouter();
      configure(ar, options);
      layouter = ar;
    } else {
      // VALUE_DIRECTED.equals(style)
      final TreeLayouter tree = new TreeLayouter();
      configure(tree, options);
      layouter = tree;
    }
    
    final boolean placeLabels = options.getBool(ITEM_INTEGRATED_EDGE_LABELING);

    final MyTreeReductionStage trs = new MyTreeReductionStage(placeLabels);
    configure(trs, options);

    layouter.appendStage(trs);
    layouter.prependStage(new HandleEdgesBetweenGroupsStage()); //required to prevent WrongGraphStructure-Exception which may be thrown by TreeLayouter if there are edges between group nodes

    // This stage is necessary because the label placement should occur at the end, after all nodes are placed on their 
    // final positions, otherwise the labels of the edges that are hidden by MyTreeReductionStage are not placed close 
    // to their associated edges when the orientation of the layout is other than TOP_TO_BOTTOM.
    layouter.prependStage(new LabelPlacementStage(placeLabels));

    prepareGraph(graph, options);
    try {
      launchLayouter(layouter);
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
    String style = options.getString(ITEM_LAYOUT_STYLE);
    if (VALUE_HV.equals(style)) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, HVTreeLayouter.SUBTREE_ORIENTATION);
      graph.addDataProvider(HVTreeLayouter.SUBTREE_ORIENTATION, new DataProviderAdapter() {
        public Object get(Object node) {
          if (graph.isSelected((Node) node)) {
            return HVTreeLayouter.VERTICAL_SUBTREE;
          } else {
            return HVTreeLayouter.HORIZONTAL_SUBTREE;
          }
        }
      });
      
    } else if (VALUE_AR.equals(style)) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, ARTreeLayouter.ROUTING_POLICY);
      graph.addDataProvider(ARTreeLayouter.ROUTING_POLICY, new DataProviderAdapter() {
        public Object get( Object node ) {
          if (graph.isSelected((Node) node)) {
            return ARTreeLayouter.ROUTING_HORIZONTAL;
          } else {
            return ARTreeLayouter.ROUTING_VERTICAL;
          }
        }
      });
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
    String style = options.getString(ITEM_LAYOUT_STYLE);
    if (VALUE_HV.equals(style)) {
      restoreDataProvider(graph, HVTreeLayouter.SUBTREE_ORIENTATION);
    } else if (VALUE_AR.equals(style)) {
      restoreDataProvider(graph, ARTreeLayouter.ROUTING_POLICY);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param tree the <code>TreeLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(TreeLayouter tree, final OptionHandler options) {
    ((ComponentLayouter) tree.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    tree.setMinimalNodeDistance(options.getInt(SECTION_DIRECTED, ITEM_MINIMAL_NODE_DISTANCE));
    tree.setMinimalLayerDistance(options.getInt(SECTION_DIRECTED, ITEM_MINIMAL_LAYER_DISTANCE));

    final String orientation = options.getString(ITEM_ORIENTATION);
    if (VALUE_TOP_TO_BOTTOM.equals(orientation)) {
      tree.setLayoutOrientation(LayoutOrientation.TOP_TO_BOTTOM);
    } else if (VALUE_BOTTOM_TO_TOP.equals(orientation)) {
      tree.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
    } else if (VALUE_RIGHT_TO_LEFT.equals(orientation)) {
      tree.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    } else {
      tree.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    }

    if (options.getBool(ITEM_ORTHOGONAL_EDGE_ROUTING)) {
      tree.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
    } else {
      tree.setLayoutStyle(TreeLayouter.PLAIN_STYLE);
    }

    final String leafLayoutPolicyStr = options.getString(ITEM_CHILD_PLACEMENT_POLICY);
    if (VALUE_SIBLINGS_ON_SAME_LAYER.equals(leafLayoutPolicyStr)) {
      tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_SIBLINGS_ON_SAME_LAYER);
    } else if (VALUE_LEAVES_STACKED_LEFT.equals(leafLayoutPolicyStr)) {
      tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED_LEFT);
    } else if (VALUE_LEAVES_STACKED_RIGHT.equals(leafLayoutPolicyStr)) {
      tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED_RIGHT);
    } else if (VALUE_LEAVES_STACKED.equals(leafLayoutPolicyStr)) {
      tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_LEAVES_STACKED);
    } else if (VALUE_ALL_LEAVES_ON_SAME_LAYER.equals(leafLayoutPolicyStr)) {
      tree.setChildPlacementPolicy(TreeLayouter.CHILD_PLACEMENT_POLICY_ALL_LEAVES_ON_SAME_LAYER);
    }

    if (options.getBool(ITEM_ENFORCE_GLOBAL_LAYERING)) {
      tree.setEnforceGlobalLayering(true);
    } else {
      tree.setEnforceGlobalLayering(false);
    }

    final String portStyle = options.getString(ITEM_PORT_STYLE);
    if (VALUE_NODE_CENTER_PORTS.equals(portStyle)) {
      tree.setPortStyle(TreeLayouter.NODE_CENTER_PORTS);
    } else if (VALUE_BORDER_CENTER_PORTS.equals(portStyle)) {
      tree.setPortStyle(TreeLayouter.BORDER_CENTER_PORTS);
    } else if (VALUE_BORDER_DISTRIBUTED_PORTS.equals(portStyle)) {
      tree.setPortStyle(TreeLayouter.BORDER_DISTRIBUTED_PORTS);
    } else {
      tree.setPortStyle(TreeLayouter.PORT_CONSTRAINTS_AWARE);
    }

    tree.setIntegratedNodeLabelingEnabled(options.getBool(SECTION_DIRECTED, ITEM_INTEGRATED_NODE_LABELING));
    tree.setIntegratedEdgeLabelingEnabled(options.getBool(SECTION_DIRECTED, ITEM_INTEGRATED_EDGE_LABELING));

    final String verticalAlignment = options.getString(ITEM_VERTICAL_ALIGNMENT);
    if (VALUE_ALIGNMENT_TOP.equals(verticalAlignment)) {
      tree.setVerticalAlignment(0);
    } else if (VALUE_ALIGNMENT_BOTTOM.equals(verticalAlignment)) {
      tree.setVerticalAlignment(1);
    } else {
      tree.setVerticalAlignment(0.5); //center aligned
    }

    final String busAlignment = options.getString(ITEM_BUS_ALIGNMENT);
    if (VALUE_ALIGNMENT_TOP.equals(busAlignment)) {
      tree.setBusAlignment(0.1);
    } else if (VALUE_ALIGNMENT_BOTTOM.equals(busAlignment)) {
      tree.setBusAlignment(0.9);
    } else {
      tree.setBusAlignment(0.5); //center aligned
    }

    tree.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param ar the <code>ARTreeLayouter</code> to be configured
   * @param options the options for the module's layout algorithm
   */
  protected void configure(final ARTreeLayouter ar, final OptionHandler options) {
    if (options.getBool(ITEM_USE_VIEW_ASPECT_RATIO)) {
      Graph2DView view = getGraph2DView();
      if (view != null) {
        Dimension dim = view.getSize();
        ar.setAspectRatio(dim.getWidth() / (double) dim.getHeight());
      } else {
        ar.setAspectRatio(1);
      }
    } else {
      ar.setAspectRatio(options.getDouble(ITEM_ASPECT_RATIO));
    }
    ar.setHorizontalSpace(options.getInt(SECTION_AR, ITEM_HORIZONTAL_SPACE_AR));
    ar.setVerticalSpace(options.getInt(SECTION_AR, ITEM_VERTICAL_SPACE_AR));
    ar.setBendDistance(options.getInt(SECTION_AR, ITEM_BEND_DISTANCE));
    ar.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));
  }

  /**
   * This method configures the modules underlying algorithm
   * with options found in the given <code>OptionHandler</code>.
   * @param hv the <code>HVTreeLayouter</code> to be configured
   * @param options the options for the module's layout algorithm
   */
  protected void configure(HVTreeLayouter hv, OptionHandler options) {
    hv.setHorizontalSpace(options.getInt(SECTION_HV, ITEM_HORIZONTAL_SPACE_HV));
    hv.setVerticalSpace(options.getInt(SECTION_HV, ITEM_VERTICAL_SPACE_HV));
    hv.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));
  }


  /**
   * Configures the module's layout algorithm according to the given options.
   * @param reduction the <code>TreeReductionStage</code> to be configured
   * @param options the options for the module's layout algorithm
   */
  protected void configure(final TreeReductionStage reduction, final OptionHandler options) {
    //configure tree reduction state and non-tree edge routing
    reduction.setMultiParentAllowed(
            VALUE_DIRECTED.equals(options.getString(ITEM_LAYOUT_STYLE)) &&
            !options.getBool(ITEM_ENFORCE_GLOBAL_LAYERING) &&
            !VALUE_ALL_LEAVES_ON_SAME_LAYER.equals(options.getString(ITEM_CHILD_PLACEMENT_POLICY)));

    final Object routingStyle = options.get(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES);
    if (VALUE_ROUTE_ORGANIC.equals(routingStyle)) {
      OrganicEdgeRouter organic = new OrganicEdgeRouter();
      reduction.setNonTreeEdgeRouter(organic);
      reduction.setNonTreeEdgeSelectionKey(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
    } else if (VALUE_ROUTE_ORTHOGONAL.equals(routingStyle)) {
      EdgeRouter orthogonal = new EdgeRouter();
      orthogonal.setReroutingEnabled(true);
      orthogonal.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);

      reduction.setNonTreeEdgeSelectionKey(orthogonal.getSelectedEdgesDpKey());
      reduction.setNonTreeEdgeRouter(orthogonal);
    } else if (VALUE_ROUTE_STRAIGHTLINE.equals(routingStyle)) {
      reduction.setNonTreeEdgeRouter(reduction.createStraightlineRouter());
    }
  }

  /**
   * This stage is responsible for placing the labels of a set of marked edges. 
   * It is needed because the labels of the edges that are hidden by MyTreeReductionStage should be placed only at the 
   * end, after all nodes are placed on their final positions and any possible change of the layout orientation has 
   * been completed.
   * 
   * The stage should be prepended to the layout algorithm.
   */
  private static class LabelPlacementStage extends AbstractLayoutStage {

    //whether or not the stage should place the labels of the marked edges
    private boolean placeLabels;

    LabelPlacementStage( final boolean placeLabels) {
      this.placeLabels = placeLabels;
    }

    /**
     * Accepts all graphs that are accepted by the {@link #getCoreLayouter() core layouter}. If the core layouter is 
     * <code>null</code>, every graph gets accepted.
     */
    public boolean canLayout(final LayoutGraph graph ) {
      return canLayoutCore(graph);
    }

    /**
     * Places the labels of the marked edges.
     */
    public void doLayout( final LayoutGraph graph ) {
      doLayoutCore(graph);

      if (placeLabels) {
        final Object selectionKey = "selectionDPKey";

        //place marked labels
        final GreedyMISLabeling labeling = new GreedyMISLabeling();
        labeling.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
        labeling.setPlaceNodeLabels(false);
        labeling.setPlaceEdgeLabels(true);
        labeling.setSelection(selectionKey);
        labeling.doLayout(graph);

        //dispose selection marker
        graph.removeDataProvider(selectionKey);
      }
    }
  }

  /**
   * A custom tree reduction stage that marks the edge labels of the non-tree edges.
   */
  private static class MyTreeReductionStage extends TreeReductionStage {
    //whether or not the stage should place the labels of non-tree edges
    private boolean placeLabels;

    MyTreeReductionStage(boolean placeLabels) {
      this.placeLabels = placeLabels;
    }

    protected void routeNonTreeEdges(final LayoutGraph graph, final EdgeMap nonTreeEdgeMap) {
      super.routeNonTreeEdges(graph, nonTreeEdgeMap);

      if (placeLabels) {
        //all labels of non-tree edges should be marked
        final Object selectionKey = "selectionDPKey";

        final DataMap edgeLayoutMap = Maps.createHashedDataMap();

        for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
          final Edge edge = edgeCursor.edge();

          final EdgeLabelLayout[] ell = graph.getEdgeLabelLayout(edge);
          final boolean nonTreeEdge = nonTreeEdgeMap.getBool(edge);

          for (int i = 0; i < ell.length; i++) {
            edgeLayoutMap.setBool(ell[i], nonTreeEdge);
          }
        }
        // add selection marker
        graph.addDataProvider(selectionKey, edgeLayoutMap);
      }
    }
  }

  /**
   * This stage temporarily removes edges that are incident to group nodes.
   * <p>
   *   The stage must be prepended to the layout algorithm and applies the following three steps:
   *   <ul>
   *     <li>Removes from the graph edges that are incident to group nodes.</li>
   *     <li>Invokes the core layout algorithm on the reduced graph.</li>
   *     <li>Re-inserts all previously removed edges and optionally places their labels.</li>
   *   </ul>
   * </p>
   * <p>
   *   Typical usage:
   *   <pre>
   *     GenericTreeLayouter tl = new GenericTreeLayouter();
   *     HandleEdgesBetweenGroupsStage trs = new HandleEdgesBetweenGroupsStage();
   *
   *     tl.prependStage(trs);
   *     new BufferedLayouter(tl).doLayout(graph);
   *     tl.removeStage(trs);
   *   </pre>
   * </p>
   * <p>
   *   This stage can be useful for layout algorithms or stages that cannot handle edges between group nodes e.g.,
   *   {@link TreeReductionStage}. Optionally, {@link HandleEdgesBetweenGroupsStage} can also place the labels of
   *   the edges that were temporarily removed right after they are restored back to the graph.
   * </p>
   * <p>
   *   The routing of the temporarily hidden edges can be customized by specifying an
   *   {@link #getMarkedEdgeRouter() edge routing algorithm} for those edges.
   * </p>
   */
  private static class HandleEdgesBetweenGroupsStage extends AbstractLayoutStage {

    private boolean considerEdgeLabels = true;

    private Layouter markedEdgeRouter;
    private Object edgeSelectionKey;

    /**
     * Creates a new instance of {@link HandleEdgesBetweenGroupsStage} with default settings.
     */
    public HandleEdgesBetweenGroupsStage() {
      markedEdgeRouter = new StraightLineEdgeRouter();
      edgeSelectionKey = StraightLineEdgeRouter.SELECTED_EDGES;
    }

    /**
     * Returns whether or not the {@link y.layout.LayoutStage} should place the labels of the edges that have been temporarily
     * hidden, when these edges will be restored back.
     *
     * @return <code>true</code> if the stage should also place the labels of previously hidden edges,
     * <code>false</code> otherwise
     *
     * @see #setConsiderEdgeLabelsEnabled(boolean)
     *
     */
    public boolean isConsiderEdgeLabelsEnabled() {
      return considerEdgeLabels;
    }

    /**
     * Specifies whether or not the {@link y.layout.LayoutStage} should place the labels of the edges that have been temporarily
     * hidden, when these edges will be restored back.
     * <p>
     *   By default, labels of previously hidden edges will be placed by the stage at the time they get restored.
     * </p>
     *
     * @param considerEdgeLabels <code>true</code> if the stage should also place the labels of the previously removed edges,
     * <code>false</code> otherwise
     *
     */
    public void setConsiderEdgeLabelsEnabled( final boolean considerEdgeLabels ) {
      this.considerEdgeLabels = considerEdgeLabels;
    }

    /**
     * Returns the key to register a {@link DataProvider} that will be used by the
     * {@link #getMarkedEdgeRouter() edge routing algorithm} to determine the edges that need to be routed.
     *
     * @return the {@link DataProvider} key
     *
     * @see #setMarkedEdgeRouter(Layouter)
     * @see #setEdgeSelectionKey(Object)
     */
    public Object getEdgeSelectionKey() {
      return edgeSelectionKey;
    }


    /**
     * Specifies the key to register a {@link DataProvider} that will be used by the
     * {@link #getMarkedEdgeRouter() edge routing algorithm} to determine the edges that need to be routed.
     *
     * @param edgeSelectionKey the {@link DataProvider} key
     *
     * @see #setMarkedEdgeRouter(Layouter)
     */
    public void setEdgeSelectionKey( final Object edgeSelectionKey ) {
      this.edgeSelectionKey = edgeSelectionKey;
    }

    /**
     * Returns the edge routing algorithm that is applied to the set of marked edges.
     *
     * Note that, it is required that a suitable {@link #setEdgeSelectionKey(Object) edge selection key} is specified
     *         and the router's scope is reduced to the selected edges.
     *
     * @return the edge routing algorithm used for marked edges
     *
     * @see #setMarkedEdgeRouter(Layouter)
     * @see #setEdgeSelectionKey(Object)
     */
    public Layouter getMarkedEdgeRouter() {
      return markedEdgeRouter;
    }

    /**
     * Specifies the edge routing algorithm that is applied to the set of marked edges.
     *
     * Note that, it is required that a suitable {@link #setEdgeSelectionKey(Object) edge selection key} is specified
     *         and the router's scope is reduced to the selected edges.
     *
     * @param markedEdgeRouter the edge routing algorithm used for marked edges
     *
     * @see #setMarkedEdgeRouter(Layouter)
     */
    public void setMarkedEdgeRouter( final Layouter markedEdgeRouter ) {
      this.markedEdgeRouter = markedEdgeRouter;
    }

    /**
     * Accepts all general graphs without exception.
     *
     * @param graph the input graph
     *
     * @return <code>true</code> for all input graphs
     */
    public boolean canLayout(LayoutGraph graph) {
      return true;
    }

    /**
     * Removes all edges that are incident to group nodes and passes it to the {@link #getCoreLayouter() core layout
     * algorithm}.
     * <p>
     *   This {@link y.layout.LayoutStage} removes some edges from the graph such that no edges incident to group nodes
     *   exist. Then, it applies the {@link #getCoreLayouter() core layout algorithm} to the reduced graph.
     *   After it produces the result, it re-inserts the previously removed edges and routes them.
     * </p>
     *
     * @param graph the input graph
     */
    public void doLayout( final LayoutGraph graph ) {

      final boolean existGroups = !Grouping.isFlat(graph);

      if (!existGroups){
        // if no group exist, invoke the core layout algorithm
        doLayoutCore(graph);
      } else {
        final Grouping grouping = new Grouping(graph);
        final GraphHider edgeHider = new GraphHider(graph);

        // marks hidden edges
        final EdgeMap hiddenEdgesMap = graph.createEdgeMap();

        boolean existHiddenEdges = false;
        // hides edges between group nodes
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();

          if (grouping.isGroupNode(edge.source()) || grouping.isGroupNode(edge.target())){
            hiddenEdgesMap.setBool(edge, true);
            edgeHider.hide(edge);
            if (!existHiddenEdges){
              existHiddenEdges = true;
            }
          }
        }

        // invokes the core layout algorithm
        doLayoutCore(graph);

        if (existHiddenEdges) {
          // re-inserts all previously hidden edges
          edgeHider.unhideAll();

          // routes the marked edges
          routeSelectedEdges(graph, hiddenEdgesMap);

          if (considerEdgeLabels) {
            //all labels of hidden edges should be marked
            final Object selectionKey = "SELECTED_LABELS";
            graph.addDataProvider(selectionKey, new DataProviderAdapter() {
              public boolean getBool( Object dataHolder ) {
                if (dataHolder instanceof EdgeLabelLayout) {
                  final EdgeLabelLayout labelLayout = (EdgeLabelLayout) dataHolder;
                  final Edge relatedEdge = graph.getFeature(labelLayout);
                  return hiddenEdgesMap.getBool(relatedEdge);
                } else {
                  return false;
                }
              }
            });

            //place marked labels
            final GreedyMISLabeling labeling = new GreedyMISLabeling();
            labeling.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
            labeling.setPlaceNodeLabels(false);
            labeling.setPlaceEdgeLabels(true);
            labeling.setSelection(selectionKey);
            labeling.doLayout(graph);

            //dispose selection key
            graph.removeDataProvider(selectionKey);
          }
        }

        graph.disposeEdgeMap(hiddenEdgesMap);
        grouping.dispose();
      }
    }

    /**
     * Routes all edges that are temporarily hidden by this {@link y.layout.LayoutStage}.
     *
     * <p>
     *   This method is called by {@link #doLayout(LayoutGraph)} after the reduced graph was arranged by the
     *   {@link #getCoreLayouter() core layout algorithm}. It may be overridden to apply custom edge routes.
     * </p>
     *
     * Note that, this method will do nothing if no {@link #getMarkedEdgeRouter() edge routing algorithm}
     *            was specified (i.e., if it is <code>null</code>).
     *
     * @param graph the graph that contains the hidden edges
     * @param markedEdgeMap an {@link EdgeMap} that returns boolean value <code>true</code>
     *                      for all hidden by the stage edges of the graph
     */
    protected void routeSelectedEdges(LayoutGraph graph, EdgeMap markedEdgeMap) {
      if (markedEdgeRouter == null) return;

      DataProvider backupDP = null;
      if (edgeSelectionKey != null) {
        backupDP = graph.getDataProvider(edgeSelectionKey);
        graph.addDataProvider(edgeSelectionKey, markedEdgeMap);
      }
      if (markedEdgeRouter instanceof StraightLineEdgeRouter) {
        final StraightLineEdgeRouter router = (StraightLineEdgeRouter) markedEdgeRouter;
        router.setSphereOfAction(StraightLineEdgeRouter.ROUTE_SELECTED_EDGES);
        router.setSelectedEdgesDpKey(edgeSelectionKey);
      }

      markedEdgeRouter.doLayout(graph);

      graph.removeDataProvider(edgeSelectionKey);
      if (backupDP != null) {
        graph.addDataProvider(edgeSelectionKey, backupDP);
      }
    }
  }
}
