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

import y.base.Edge;
import y.base.Node;
import y.layout.Layouter;
import y.layout.router.polyline.EdgeLayoutDescriptor;
import y.layout.router.polyline.EdgeRouter;
import y.layout.router.polyline.Grid;
import y.layout.router.polyline.PenaltySettings;
import y.option.ConstraintManager;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.view.Graph2D;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.router.polyline.EdgeRouter}.
 *
 */
public class PolylineEdgeRouterModule extends LayoutModule {

  //// Module 'Orthogonal-' / 'Polyline Edge Router'
  private static final String POLYLINE_EDGE_ROUTER = "POLYLINE_EDGE_ROUTER";
  
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT = "LAYOUT";
  // Section 'Layout' items
  protected static final String ITEM_SCOPE = "SCOPE";
  protected static final String VALUE_SCOPE_ALL_EDGES = "SCOPE_ALL_EDGES";
  protected static final String VALUE_SCOPE_SELECTED_EDGES = "SCOPE_SELECTED_EDGES";
  protected static final String VALUE_SCOPE_EDGES_AT_SELECTED_NODES = "SCOPE_EDGES_AT_SELECTED_NODES";
  protected static final String ITEM_OPTIMIZATION_STRATEGY = "OPTIMIZATION_STRATEGY";
  protected static final String VALUE_STRATEGY_BALANCED = "STRATEGY_BALANCED";
  protected static final String VALUE_STRATEGY_MINIMIZE_BENDS = "STRATEGY_MINIMIZE_BENDS";
  protected static final String VALUE_STRATEGY_MINIMIZE_CROSSINGS = "STRATEGY_MINIMIZE_CROSSINGS";
  protected static final String ITEM_MONOTONIC_RESTRICTION = "MONOTONIC_RESTRICTION";
  protected static final String VALUE_MONOTONIC_NONE = "MONOTONIC_NONE";
  protected static final String VALUE_MONOTONIC_HORIZONTAL = "MONOTONIC_HORIZONTAL";
  protected static final String VALUE_MONOTONIC_VERTICAL = "MONOTONIC_VERTICAL";
  protected static final String VALUE_MONOTONIC_BOTH = "MONOTONIC_BOTH";
  protected static final String TITLE_MINIMAL_DISTANCES = "MINIMAL_DISTANCES";
  protected static final String ITEM_MINIMAL_EDGE_TO_EDGE_DISTANCE = "MINIMAL_EDGE_TO_EDGE_DISTANCE";
  protected static final String ITEM_MINIMAL_NODE_TO_EDGE_DISTANCE = "MINIMAL_NODE_TO_EDGE_DISTANCE";
  protected static final String ITEM_MINIMAL_NODE_CORNER_DISTANCE = "MINIMAL_NODE_CORNER_DISTANCE";
  protected static final String ITEM_MINIMAL_FIRST_SEGMENT_LENGTH = "MINIMAL_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMAL_LAST_SEGMENT_LENGTH = "MINIMAL_LAST_SEGMENT_LENGTH";
  protected static final String TITLE_GRID_SETTINGS = "GRID_SETTINGS";
  protected static final String ITEM_GRID_ENABLED = "GRID_ENABLED";
  protected static final String ITEM_GRID_SPACING = "GRID_SPACING";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  protected static final String ITEM_CONSIDER_EDGE_LABELS = "CONSIDER_EDGE_LABELS";
  protected static final String ITEM_ENABLE_REROUTING = "ENABLE_REROUTING";
  protected static final String ITEM_MAXIMAL_DURATION = "MAXIMAL_DURATION";
  
  //// Section 'Octilinear Routing'
  protected static final String SECTION_POLYLINE_ROUTING = "POLYLINE_ROUTING";
  // Section 'Octilinear Routing' items
  protected static final String ITEM_ENABLE_POLYLINE_ROUTING = "ENABLE_POLYLINE_ROUTING";
  protected static final String ITEM_PREFERRED_POLYLINE_SEGMENT_LENGTH = "PREFERRED_POLYLINE_SEGMENT_LENGTH";
  
  // data provider delete flags
  private boolean isEdgesDPAddedByModule;
  private boolean isNodesDPAddedByModule;

  /**
   * Creates an instance of this module.
   */
  public PolylineEdgeRouterModule() {
    super(POLYLINE_EDGE_ROUTER);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Default providers
    final EdgeRouter polyline = new EdgeRouter();
    final EdgeLayoutDescriptor descriptor = polyline.getDefaultEdgeLayoutDescriptor();
    final Grid grid = polyline.getGrid();

    //// Section 'Layout'
    options.useSection(SECTION_LAYOUT);
    // Populate section
    byte scope = polyline.getSphereOfAction();
    options.addEnum(ITEM_SCOPE, new String[]{
        VALUE_SCOPE_ALL_EDGES,
        VALUE_SCOPE_SELECTED_EDGES,
        VALUE_SCOPE_EDGES_AT_SELECTED_NODES
    }, scope == EdgeRouter.ROUTE_ALL_EDGES ? 0 : 1);
    options.addEnum(ITEM_OPTIMIZATION_STRATEGY, new String[]{
        VALUE_STRATEGY_BALANCED,
        VALUE_STRATEGY_MINIMIZE_BENDS,
        VALUE_STRATEGY_MINIMIZE_CROSSINGS
    }, 0);
    options.addEnum(ITEM_MONOTONIC_RESTRICTION, new String[]{
        VALUE_MONOTONIC_NONE,
        VALUE_MONOTONIC_HORIZONTAL,
        VALUE_MONOTONIC_VERTICAL,
        VALUE_MONOTONIC_BOTH
    }, descriptor.getMonotonicPathRestriction());

    // Group 'Minimum Distances'
    final OptionGroup distancesGroup = new OptionGroup();
    distancesGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_MINIMAL_DISTANCES);
    // Populate group
    distancesGroup.addItem(
        options.addDouble(ITEM_MINIMAL_EDGE_TO_EDGE_DISTANCE, descriptor.getMinimalEdgeToEdgeDistance()));
    distancesGroup.addItem(
        options.addDouble(ITEM_MINIMAL_NODE_TO_EDGE_DISTANCE, polyline.getMinimalNodeToEdgeDistance()));
    distancesGroup.addItem(
        options.addDouble(ITEM_MINIMAL_NODE_CORNER_DISTANCE, descriptor.getMinimalNodeCornerDistance()));
    distancesGroup.addItem(
        options.addDouble(ITEM_MINIMAL_FIRST_SEGMENT_LENGTH, descriptor.getMinimalFirstSegmentLength()));
    distancesGroup.addItem(
        options.addDouble(ITEM_MINIMAL_LAST_SEGMENT_LENGTH, descriptor.getMinimalLastSegmentLength()));

    // Group 'Grid Settings'
    final OptionGroup gridGroup = new OptionGroup();
    gridGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_GRID_SETTINGS);
    // Populate group
    final OptionItem itemGridEnabled =
        gridGroup.addItem(options.addBool(ITEM_GRID_ENABLED, grid != null));
    final OptionItem itemGridSpacing =
        gridGroup.addItem(options.addDouble(ITEM_GRID_SPACING, grid != null ? grid.getSpacing() : 10));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemGridEnabled, Boolean.TRUE, itemGridSpacing);

    // Ungrouped settings
    options.addBool(ITEM_CONSIDER_NODE_LABELS, polyline.isConsiderNodeLabelsEnabled());
    options.addBool(ITEM_CONSIDER_EDGE_LABELS, polyline.isConsiderEdgeLabelsEnabled());
    options.addBool(ITEM_ENABLE_REROUTING, polyline.isReroutingEnabled());
    options.addInt(ITEM_MAXIMAL_DURATION, 0);

    //// Section 'Octilinear Routing'
    options.useSection(SECTION_POLYLINE_ROUTING);
    // Populate section
    final OptionItem itemEnablePolylineRouting = options.addBool(ITEM_ENABLE_POLYLINE_ROUTING, true);
    final OptionItem itemPreferredPolylineSegmentLength =
        options.addDouble(ITEM_PREFERRED_POLYLINE_SEGMENT_LENGTH, polyline.getPreferredPolylineSegmentLength());
    // Enable/disable items depending on specific values\
    optionConstraints.setEnabledOnValueEquals(itemEnablePolylineRouting,
        Boolean.TRUE, itemPreferredPolylineSegmentLength);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final EdgeRouter edgeRouter = new EdgeRouter();

    final OptionHandler options = getOptionHandler();
    configure(edgeRouter, options);
    
    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(edgeRouter);
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
    if (VALUE_SCOPE_SELECTED_EDGES.equals(options.getString(ITEM_SCOPE))) {
      // rather use an external registered data provider than registering an own
      // e.g. EdgeRouterDemo does provide an own data provider
      isEdgesDPAddedByModule = graph.getDataProvider(Layouter.SELECTED_EDGES) == null;
      if (isEdgesDPAddedByModule) {
        graph.addDataProvider(Layouter.SELECTED_EDGES, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return graph.isSelected((Edge) dataHolder);
          }
        });
      }
    } else if (VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(options.getString(ITEM_SCOPE))) {
      // rather use an external registered data provider than registering an own
      isNodesDPAddedByModule = graph.getDataProvider(Layouter.SELECTED_NODES) == null;
      if (isNodesDPAddedByModule) {
        graph.addDataProvider(Layouter.SELECTED_NODES, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return graph.isSelected((Node) dataHolder);
          }
        });
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
    // remove the data providers if they were added by this module
    if(VALUE_SCOPE_SELECTED_EDGES.equals(options.getString(ITEM_SCOPE))) {
      if (isEdgesDPAddedByModule) {
        isEdgesDPAddedByModule = false;
        graph.removeDataProvider(Layouter.SELECTED_EDGES);
      }
    } else if (VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(options.getString(ITEM_SCOPE))) {
      if (isNodesDPAddedByModule) {
        isNodesDPAddedByModule = false;
        graph.removeDataProvider(Layouter.SELECTED_NODES);
      }
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param router the <code>EdgeRouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final EdgeRouter router, final OptionHandler options) {
    final String scope = options.getString(ITEM_SCOPE);
    if (VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(scope)) {
      router.setSphereOfAction(EdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
    } else if (VALUE_SCOPE_ALL_EDGES.equals(scope)) {
      router.setSphereOfAction(EdgeRouter.ROUTE_ALL_EDGES);
    } else {
      router.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
    }

    final EdgeLayoutDescriptor descriptor = router.getDefaultEdgeLayoutDescriptor();
    
    final String strategy = options.getString(ITEM_OPTIMIZATION_STRATEGY);
    final PenaltySettings penaltySettings = descriptor.getPenaltySettings();
    if (strategy.equals(VALUE_STRATEGY_BALANCED)) {
      penaltySettings.setBendPenalty(3);
      penaltySettings.setEdgeCrossingPenalty(1);
    } else if (strategy.equals(VALUE_STRATEGY_MINIMIZE_BENDS)) {
      penaltySettings.setBendPenalty(3);
      penaltySettings.setEdgeCrossingPenalty(0);
    } else {
      penaltySettings.setBendPenalty(3);
      penaltySettings.setEdgeCrossingPenalty(5);
    }

    final String monotonyFlag = options.getString(ITEM_MONOTONIC_RESTRICTION);
    if (monotonyFlag.equals(VALUE_MONOTONIC_BOTH)) {
      descriptor.setMonotonicPathRestriction(EdgeLayoutDescriptor.MONOTONIC_BOTH);
    } else if (monotonyFlag.equals(VALUE_MONOTONIC_HORIZONTAL)) {
      descriptor.setMonotonicPathRestriction(EdgeLayoutDescriptor.MONOTONIC_HORIZONTAL);
    } else if (monotonyFlag.equals(VALUE_MONOTONIC_VERTICAL)) {
      descriptor.setMonotonicPathRestriction(EdgeLayoutDescriptor.MONOTONIC_VERTICAL);
    } else {
      descriptor.setMonotonicPathRestriction(EdgeLayoutDescriptor.MONOTONIC_NONE);
    }

    descriptor.setMinimalEdgeToEdgeDistance(options.getDouble(ITEM_MINIMAL_EDGE_TO_EDGE_DISTANCE));
    router.setMinimalNodeToEdgeDistance(options.getDouble(ITEM_MINIMAL_NODE_TO_EDGE_DISTANCE));
    descriptor.setMinimalNodeCornerDistance(options.getDouble(ITEM_MINIMAL_NODE_CORNER_DISTANCE));
    descriptor.setMinimalFirstSegmentLength(options.getDouble(ITEM_MINIMAL_FIRST_SEGMENT_LENGTH));
    descriptor.setMinimalLastSegmentLength(options.getDouble(ITEM_MINIMAL_LAST_SEGMENT_LENGTH));

    if (options.getBool(ITEM_GRID_ENABLED)) {
      final double gridSpacing = options.getDouble(ITEM_GRID_SPACING);
      router.setGrid(new Grid(0, 0, gridSpacing));
    } else {
      router.setGrid(null);
    }

    router.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS));
    router.setConsiderEdgeLabelsEnabled(options.getBool(ITEM_CONSIDER_EDGE_LABELS));
    router.setReroutingEnabled(options.getBool(ITEM_ENABLE_REROUTING));
    router.setPolylineRoutingEnabled(options.getBool(ITEM_ENABLE_POLYLINE_ROUTING));
    router.setPreferredPolylineSegmentLength(options.getDouble(ITEM_PREFERRED_POLYLINE_SEGMENT_LENGTH));
    
    int maximalDuration = options.getInt(ITEM_MAXIMAL_DURATION);
    if (maximalDuration == 0) {
      router.setMaximumDuration(Long.MAX_VALUE);
    } else {
      router.setMaximumDuration(maximalDuration * 1000);
    }
  }
}
