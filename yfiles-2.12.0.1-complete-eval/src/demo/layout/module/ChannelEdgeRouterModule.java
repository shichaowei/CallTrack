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
import y.layout.Layouter;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.ChannelEdgeRouter.OrthogonalShortestPathPathFinder;
import y.layout.router.OrthogonalPatternEdgeRouter;
import y.layout.router.OrthogonalSegmentDistributionStage;
import y.option.ConstraintManager;
import y.option.DoubleOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.view.Graph2D;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.router.ChannelEdgeRouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/channel_edge_router.html#channel_edge_router">Section Channel Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class ChannelEdgeRouterModule extends LayoutModule {
  //// Module 'Channel Edge Router'
  protected static final String MODULE_CHANNEL_EDGE_ROUTER = "CHANNEL_EDGE_ROUTER";

  //// Section 'default' items
  protected static final String TITLE_LAYOUT_OPTIONS = "LAYOUT_OPTIONS";
  protected static final String ITEM_PATHFINDER = "PATHFINDER";
  protected static final String VALUE_ORTHOGONAL_PATTERN_PATH_FINDER = "ORTHOGONAL_PATTERN_PATH_FINDER";
  protected static final String VALUE_ORTHOGONAL_SHORTESTPATH_PATH_FINDER = "ORTHOGONAL_SHORTESTPATH_PATH_FINDER";
  protected static final String ITEM_SCOPE = "SCOPE";
  protected static final String VALUE_SCOPE_ALL_EDGES = "SCOPE_ALL_EDGES";
  protected static final String VALUE_SCOPE_SELECTED_EDGES = "SCOPE_SELECTED_EDGES";
  protected static final String VALUE_SCOPE_AT_SELECTED_NODES = "SCOPE_AT_SELECTED_NODES";
  protected static final String ITEM_MINIMUM_DISTANCE = "MINIMUM_DISTANCE";
  protected static final String ITEM_ACTIVATE_GRID_ROUTING = "ACTIVATE_GRID_ROUTING";
  protected static final String ITEM_GRID_SPACING = "GRID_SPACING";
  protected static final String TITLE_COST = "COST";
  protected static final String ITEM_BEND_COST = "BEND_COST_FACTOR";
  protected static final String ITEM_EDGE_CROSSING_COST = "EDGE_CROSSING_COST";
  protected static final String ITEM_NODE_CROSSING_COST = "NODE_CROSSING_COST";
  private boolean isEdgeDPAddedByModule;

  /**
   * Creates an instance of this module.
   */
  public ChannelEdgeRouterModule() {
    super(MODULE_CHANNEL_EDGE_ROUTER);
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
    final ChannelEdgeRouter defaults = new ChannelEdgeRouter();
    // Default values
    final double minimumDistance, gridSpacing, bendCost, edgeCrossingCost, nodeCrossingCost;
    final boolean isGridRoutingEnabled;
    if (defaults.getPathFinderStrategy() instanceof OrthogonalPatternEdgeRouter) {
      // Defaults provider
      final OrthogonalPatternEdgeRouter defaultsOper = (OrthogonalPatternEdgeRouter) defaults.getPathFinderStrategy();
      // Set defaults
      minimumDistance = defaultsOper.getMinimumDistance();
      isGridRoutingEnabled = defaultsOper.isGridRoutingEnabled();
      gridSpacing = defaultsOper.getGridWidth();
      bendCost = defaultsOper.getBendCost();
      edgeCrossingCost = defaultsOper.getEdgeCrossingCost();
      nodeCrossingCost = defaultsOper.getNodeCrossingCost();
    } else if (defaults.getPathFinderStrategy() instanceof ChannelEdgeRouter.OrthogonalShortestPathPathFinder) {
      // Defaults provider
      final ChannelEdgeRouter.OrthogonalShortestPathPathFinder defaultsOsppf =
          (ChannelEdgeRouter.OrthogonalShortestPathPathFinder) defaults.getPathFinderStrategy();
      // Set defaults
      minimumDistance = defaultsOsppf.getMinimumDistance();
      isGridRoutingEnabled = defaultsOsppf.isGridRoutingEnabled();
      gridSpacing = defaultsOsppf.getGridSpacing();
      bendCost = 1;
      edgeCrossingCost = 5;
      nodeCrossingCost = 50;
    } else {
      // Set defaults
      minimumDistance = 10;
      isGridRoutingEnabled = true;
      gridSpacing = 20;
      bendCost = 1;
      edgeCrossingCost = 5;
      nodeCrossingCost = 20;
    }

    final boolean isOrthogonalShortestPath =
        defaults.getPathFinderStrategy() instanceof ChannelEdgeRouter.OrthogonalShortestPathPathFinder;
    
    // Group 'Layout'
    final OptionGroup layoutGroup = new OptionGroup();
    layoutGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_LAYOUT_OPTIONS);
    // Populate group
    final OptionItem itemPathfinder = layoutGroup.addItem(
        options.addEnum(ITEM_PATHFINDER, new String[]{
            VALUE_ORTHOGONAL_PATTERN_PATH_FINDER,
            VALUE_ORTHOGONAL_SHORTESTPATH_PATH_FINDER
        }, isOrthogonalShortestPath ? 1 : 0));
    layoutGroup.addItem(
        options.addEnum(ITEM_SCOPE, new String[]{
            VALUE_SCOPE_ALL_EDGES,
            VALUE_SCOPE_SELECTED_EDGES,
            VALUE_SCOPE_AT_SELECTED_NODES
        }, 0));
    final OptionItem itemMinimumDistance = layoutGroup.addItem(
        options.addDouble(ITEM_MINIMUM_DISTANCE, minimumDistance));
    if (isOrthogonalShortestPath) {
      itemMinimumDistance.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(4.0));
    }
    final OptionItem itemActivateGridRouting = layoutGroup.addItem(
        options.addBool(ITEM_ACTIVATE_GRID_ROUTING, isGridRoutingEnabled));
    final OptionItem itemGridSpacing =layoutGroup.addItem(
        options.addDouble(ITEM_GRID_SPACING, gridSpacing));
    itemGridSpacing.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(2.0));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemActivateGridRouting, Boolean.TRUE, itemGridSpacing);
    
    // Group 'Cost'
    final OptionGroup costGroup = new OptionGroup();
    costGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_COST);
    // Populate group
    final OptionItem itemBendCost = costGroup.addItem(options.addDouble(ITEM_BEND_COST, bendCost));
    final OptionItem itemEdgeCrossingCost = costGroup.addItem(
        options.addDouble(ITEM_EDGE_CROSSING_COST, edgeCrossingCost));
    final OptionItem itemNodeCrossingCost = costGroup.addItem(
        options.addDouble(ITEM_NODE_CROSSING_COST, nodeCrossingCost));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemPathfinder, VALUE_ORTHOGONAL_PATTERN_PATH_FINDER, itemBendCost);
    optionConstraints.setEnabledOnValueEquals(itemPathfinder, VALUE_ORTHOGONAL_PATTERN_PATH_FINDER, itemEdgeCrossingCost);
    optionConstraints.setEnabledOnValueEquals(itemPathfinder, VALUE_ORTHOGONAL_PATTERN_PATH_FINDER, itemNodeCrossingCost);
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final ChannelEdgeRouter channel = new ChannelEdgeRouter();

    final OptionHandler options = getOptionHandler();
    configure(channel, options);

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(channel);
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
  protected void prepareGraph(final Graph2D graph, OptionHandler options) {
    isEdgeDPAddedByModule = graph.getDataProvider(ChannelEdgeRouter.AFFECTED_EDGES) == null;
    if (isEdgeDPAddedByModule) {
      //set affected edges
      if (options.get(ITEM_SCOPE).equals(VALUE_SCOPE_ALL_EDGES)) {
        graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return true;
          }
        });
      } else if (options.get(ITEM_SCOPE).equals(VALUE_SCOPE_SELECTED_EDGES)) {
        graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return graph.isSelected((Edge) dataHolder);
          }
        });
      } else {
        graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return graph.isSelected(((Edge) dataHolder).source()) || graph.isSelected(((Edge) dataHolder).target());
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
    if (isEdgeDPAddedByModule) {
      isEdgeDPAddedByModule = false;
      graph.removeDataProvider(ChannelEdgeRouter.AFFECTED_EDGES);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param channel the <code>ChannelEdgeRouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final ChannelEdgeRouter channel, final OptionHandler options) {
    final Layouter pathFinder;
    if (options.get(ITEM_PATHFINDER).equals(VALUE_ORTHOGONAL_PATTERN_PATH_FINDER)) {
      final OrthogonalPatternEdgeRouter oper = new OrthogonalPatternEdgeRouter();
      configure(oper, options);
      pathFinder = oper;
    } else {
      final OrthogonalShortestPathPathFinder ospf = new OrthogonalShortestPathPathFinder();
      configure(ospf, options);
      pathFinder = ospf;
    }
    channel.setPathFinderStrategy(pathFinder);

    OrthogonalSegmentDistributionStage segmentDistributionStage = new OrthogonalSegmentDistributionStage();
    segmentDistributionStage.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
    segmentDistributionStage.setPreferredDistance(options.getDouble(ITEM_MINIMUM_DISTANCE));
    segmentDistributionStage.setGridEnabled(options.getBool(ITEM_ACTIVATE_GRID_ROUTING));
    segmentDistributionStage.setGridWidth(options.getDouble(ITEM_GRID_SPACING));

    channel.setEdgeDistributionStrategy(segmentDistributionStage);
  }

  private void configure(final OrthogonalShortestPathPathFinder ospf, final OptionHandler options) {
    ospf.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
    ospf.setMinimumDistance((int) options.getDouble(ITEM_MINIMUM_DISTANCE));

    ospf.setGridRoutingEnabled(options.getBool(ITEM_ACTIVATE_GRID_ROUTING));
    ospf.setGridSpacing((int) options.getDouble(ITEM_GRID_SPACING));

    ospf.setCrossingCost(options.getDouble(ITEM_EDGE_CROSSING_COST));
  }

  private void configure(final OrthogonalPatternEdgeRouter oper, OptionHandler options) {
    oper.setAffectedEdgesDPKey(ChannelEdgeRouter.AFFECTED_EDGES);
    oper.setMinimumDistance(options.getDouble(ITEM_MINIMUM_DISTANCE));

    oper.setGridRoutingEnabled(options.getBool(ITEM_ACTIVATE_GRID_ROUTING));
    oper.setGridWidth(options.getDouble(ITEM_GRID_SPACING));

    oper.setBendCost(options.getDouble(ITEM_BEND_COST));
    oper.setEdgeCrossingCost(options.getDouble(ITEM_EDGE_CROSSING_COST));
    oper.setNodeCrossingCost(options.getDouble(ITEM_NODE_CROSSING_COST));

    //disable edge overlap costs when Edge distribution will run afterwards anyway
    oper.setEdgeOverlapCost(0.0);
  }
}
