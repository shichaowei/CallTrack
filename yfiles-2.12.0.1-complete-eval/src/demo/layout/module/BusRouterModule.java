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

import y.algo.GraphConnectivity;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.router.BusDescriptor;
import y.layout.router.BusRouter;
import y.option.ConstraintManager;
import y.option.DoubleOptionItem;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.Graph2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Module for the {@link y.layout.router.BusRouter}.
 * <p>
 * There are more scopes in this module than in {@link BusRouter}. Each additional scope is mapped to an appropriate
 * combinations of scope and fixed edges in BusRouter.
 * </p>
 * <dl>
 * <dt>ALL</dt>
 * <dd>Maps to <code>BusRouter.SCOPE_ALL</code>. All edges are in scope, and all of them are movable.</dd>
 * <dt>SUBSET</dt>
 * <dd>Maps to <code>BusRouter.SCOPE_SUBSET</code>. Selected edges are in scope, and all of them are movable.</dd>
 * <dt>SUBSET_BUS</dt>
 * <dd>Each bus with at least one selected edge is in scope, and all of their edges are movable.</dd>
 * <dt>PARTIAL</dt>
 * <dd>Each bus with at least one selected node is in scope, and only the adjacent edges of the selected nodes are
 * movable.</dd>
 * <dt>EDGES_AT_SELECTED_NODES</dt>
 * <dd> Maps to <code>BusRouter.SCOPE_SUBSET</code>. Edges connected to selected nodes are in scope, and all of them are movable.
 * </dd>
 * </dl>
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_bus_router.html#orthogonal_bus_router">Section Orthogonal Bus-style Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class BusRouterModule extends LayoutModule {
  //// Module 'Orthogonal Bus-style Edge Router'
  protected static final String MODULE_BUS_ROUTER = "BUS_ROUTER";
  
  //// Section 'default' items
  protected static final String TITLE_LAYOUT = "GROUP_LAYOUT";
  protected static final String ITEM_SCOPE = "SCOPE";
  protected static final String VALUE_SCOPE_ALL = "ALL";
  protected static final String VALUE_SCOPE_SUBSET = "SUBSET";
  protected static final String VALUE_SCOPE_EDGES_AT_SELECTED_NODES = "EDGES_AT_SELECTED_NODES";
  protected static final String VALUE_SCOPE_SUBSET_BUS = "SUBSET_BUS";
  protected static final String VALUE_SCOPE_PARTIAL = "PARTIAL";
  protected static final String ITEM_BUSES = "BUSES";
  protected static final String VALUE_SINGLE = "SINGLE";
  protected static final String VALUE_COLOR = "COLOR";
  protected static final String VALUE_CONNECTED_COMPONENT = "CONNECTED_COMPONENT";
  protected static final String ITEM_GRID_ENABLED = "GRID_ENABLED";
  protected static final String ITEM_GRID_SPACING = "GRID_SPACING";
  protected static final String ITEM_MIN_DISTANCE_TO_NODES = "MIN_DISTANCE_TO_NODES";
  protected static final String ITEM_MIN_DISTANCE_TO_EDGES = "MIN_DISTANCE_TO_EDGES";
  protected static final String TITLE_SELECTION = "GROUP_SELECTION";
  protected static final String ITEM_PREFERRED_BACKBONE_COUNT = "PREFERRED_BACKBONE_COUNT";
  protected static final String ITEM_MINIMUM_BACKBONE_LENGTH = "MINIMUM_BACKBONE_LENGTH";
  protected static final String TITLE_ROUTING = "GROUP_ROUTING";
  protected static final String ITEM_CROSSING_COST = "CROSSING_COST";
  protected static final String ITEM_CROSSING_REROUTING = "CROSSING_REROUTING";
  protected static final String ITEM_MINIMUM_CONNECTIONS_COUNT = "MINIMUM_CONNECTIONS_COUNT";

  /**
   * Specifies whether the options of group layout are used.
   */
  protected boolean optionsLayout;
  /**
   * Specifies whether the options for initial backbone selection are used.
   */
  protected boolean optionsSelection;
  /**
   * Specifies whether the options for routing and recombination are used.
   */
  protected boolean optionsRouting;

  /**
   * Creates an instance of this module.
   */
  public BusRouterModule() {
    super(MODULE_BUS_ROUTER);
    optionsLayout = true;
    optionsSelection = true;
    optionsRouting = true;
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    addOptionItems(new BusRouter(), options);
    return options;
  }

  /**
   * Adds the option items used by this module to the given <code>OptionHandler</code>.
   * @param defaults a <code>BusRouter</code> instance that provides default option values.
   * @param options the <code>OptionHandler</code> to add the items to
   */
  protected void addOptionItems(final BusRouter defaults, final OptionHandler options) {
    final ConstraintManager optionConstraints = new ConstraintManager(options);

    if (optionsLayout) {
      // Group 'Layout'
      final OptionGroup layoutGroup = new OptionGroup();
      layoutGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_LAYOUT);
      // Populate group
      layoutGroup.addItem(options.addEnum(ITEM_SCOPE, new String[]{
          VALUE_SCOPE_ALL,
          VALUE_SCOPE_SUBSET,
          VALUE_SCOPE_EDGES_AT_SELECTED_NODES,
          VALUE_SCOPE_SUBSET_BUS,
          VALUE_SCOPE_PARTIAL
      }, (int) defaults.getScope()));
      layoutGroup.addItem(options.addEnum(ITEM_BUSES, new String[]{
          VALUE_SINGLE,
          VALUE_COLOR,
          VALUE_CONNECTED_COMPONENT
      }, 0));
      layoutGroup.addItem(options.addBool(ITEM_GRID_ENABLED, defaults.isGridRoutingEnabled()));
      final OptionItem itemGridSpacing = layoutGroup.addItem(
          options.addInt(ITEM_GRID_SPACING, defaults.getGridSpacing()));
      itemGridSpacing.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      final OptionItem itemMinDistanceNodes = layoutGroup.addItem(
          options.addInt(ITEM_MIN_DISTANCE_TO_NODES, defaults.getMinimumDistanceToNode()));
      itemMinDistanceNodes.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      final OptionItem itemMinDistanceEdges = layoutGroup.addItem(
          options.addInt(ITEM_MIN_DISTANCE_TO_EDGES, defaults.getMinimumDistanceToEdge()));
      itemMinDistanceEdges.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      // Enable/disable items depending on specific values
      optionConstraints.setEnabledOnValueEquals(ITEM_GRID_ENABLED, Boolean.TRUE, ITEM_GRID_SPACING);
    }

    if (optionsSelection) {
      // Group 'Backbone Selection'
      final OptionGroup selectionGroup = new OptionGroup();
      selectionGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_SELECTION);
      // Populate group
      final OptionItem itemPreferredBackboneCount = selectionGroup.addItem(
          options.addInt(ITEM_PREFERRED_BACKBONE_COUNT, defaults.getPreferredBackboneSegmentCount()));
      itemPreferredBackboneCount.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
      final OptionItem itemMinBackboneLength = selectionGroup.addItem(
          options.addDouble(ITEM_MINIMUM_BACKBONE_LENGTH, defaults.getMinimumBackboneSegmentLength()));
      itemMinBackboneLength.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(1.0));      
    }

    if (optionsRouting) {
      // Group 'Routing and Recombination'
      final OptionGroup routingGroup = new OptionGroup();
      routingGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_ROUTING);
      // Populate group
      final OptionItem itemCrossingCost = routingGroup.addItem(
          options.addDouble(ITEM_CROSSING_COST, defaults.getCrossingCost()));
      itemCrossingCost.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
      final OptionItem itemCrossingRerouting = routingGroup.addItem(
          options.addBool(ITEM_CROSSING_REROUTING, defaults.isReroutingEnabled()));
      itemCrossingRerouting.setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(0.0));
      final OptionItem itemMinimumConnectionCount = routingGroup.addItem(
          options.addInt(ITEM_MINIMUM_CONNECTIONS_COUNT, defaults.getMinimumBusConnectionsCount()));
      itemMinimumConnectionCount.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    }
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final BusRouter bus = new BusRouter();
    
    final OptionHandler options = getOptionHandler();
    configure(bus, options);

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(bus);
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
    if (!optionsLayout) {
      // nothing to do
      return;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Bus assignment
    ////////////////////////////////////////////////////////////////////////////
    
    // backup existing data providers to prevent loss of user settings
    backupDataProvider(graph, BusRouter.EDGE_DESCRIPTOR_DPKEY);

    // Explicit variables for ITEM_BUSES and ITEM_SCOPE since these are often queried
    final String busType = options.getString(ITEM_BUSES);
    final Object scope = options.get(ITEM_SCOPE);

    // The following creates bus descriptors according to the set options for
    // ITEM_SCOPE and ITEM_BUSES taking the current selection into account.
    final EdgeMap descriptorMap = Maps.createHashedEdgeMap();
    graph.addDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY, descriptorMap);
    final NodeMap node2CompId = Maps.createHashedNodeMap();
    GraphConnectivity.connectedComponents(graph, node2CompId);
    // Create the bus descriptors. For scopes SUBSET_BUS and PARTIAL, this is done for all edges since the bus IDs
    // are required to determine which edges belong to the final scope.
    if (VALUE_SCOPE_SUBSET.equals(scope)) {
      for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        descriptorMap.set(edge, new BusDescriptor(getBusId(graph, edge, node2CompId, busType), false));
      }
    } else if (VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(scope)) {
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (graph.isSelected(edge.source()) || graph.isSelected(edge.target())) {
          descriptorMap.set(edge, new BusDescriptor(getBusId(graph, edge, node2CompId, busType), false));
        }
      }
    } else if (VALUE_SCOPE_PARTIAL.equals(scope)) {
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        final boolean fixed = !graph.isSelected(edge.source()) && !graph.isSelected(edge.target());
        descriptorMap.set(edge, new BusDescriptor(getBusId(graph, edge, node2CompId, busType), fixed));
      }
    } else {
      // else if VALUE_SCOPE_SUBSET_BUS.equals(scope) or VALUE_SCOPE_ALL.equals(scope)
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        descriptorMap.set(edge, new BusDescriptor(getBusId(graph, edge, node2CompId, busType), false));
      }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Edge selection
    ////////////////////////////////////////////////////////////////////////////

    // backup existing data providers to prevent loss of user settings
    backupDataProvider(graph, BusRouter.EDGE_SUBSET_DPKEY);
    
    if (VALUE_SCOPE_SUBSET.equals(scope)) {
      // The selected edges are in scope, and all of them are movable
      graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return dataHolder instanceof Edge && graph.isSelected((Edge) dataHolder);
        }
      });
    } else if (VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(scope)) {
      graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          if (dataHolder instanceof Edge) {
            final Edge edge = (Edge) dataHolder;
            return graph.isSelected(edge.source()) || graph.isSelected(edge.target());
          }
          return false;
        }
      });
    } else if (VALUE_SCOPE_SUBSET_BUS.equals(scope)) {
      // Each bus with at least one selected edge is in scope, and all of their edges are movable.
      final Set selectedIDs = new HashSet();
      for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
        selectedIDs.add(((BusDescriptor) descriptorMap.get(ec.edge())).getID());
      }
      graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedIDs.contains(((BusDescriptor) descriptorMap.get(dataHolder)).getID());
        }
      });
    } else if (VALUE_SCOPE_PARTIAL.equals(scope)) {
      // Each bus with at least one selected node is in scope, and the adjacent edges of the selected
      // nodes are movable.
      final Set selectedIDs = new HashSet();
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
          selectedIDs.add(((BusDescriptor) descriptorMap.get(ec.edge())).getID());
        }
      }
      graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedIDs.contains(((BusDescriptor) descriptorMap.get(dataHolder)).getID());
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
    if (optionsLayout) {
      // remove the data providers set by this module by restoring the initial state
      restoreDataProvider(graph, BusRouter.EDGE_DESCRIPTOR_DPKEY);
      restoreDataProvider(graph, BusRouter.EDGE_SUBSET_DPKEY);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param bus the <code>BusRouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final BusRouter bus, final OptionHandler options) {

    if (optionsLayout) {
      bus.setScope(toBusRouterScope(options.get(ITEM_SCOPE)));
      bus.setGridRoutingEnabled(options.getBool(ITEM_GRID_ENABLED));
      bus.setGridSpacing(options.getInt(ITEM_GRID_SPACING));
      bus.setMinimumDistanceToNode(options.getInt(ITEM_MIN_DISTANCE_TO_NODES));
      bus.setMinimumDistanceToEdge(options.getInt(ITEM_MIN_DISTANCE_TO_EDGES));
    }

    if (optionsSelection) {
      bus.setPreferredBackboneSegmentCount(options.getInt(ITEM_PREFERRED_BACKBONE_COUNT));
      bus.setMinimumBackboneSegmentLength(options.getDouble(ITEM_MINIMUM_BACKBONE_LENGTH));
    }

    if (optionsRouting) {
      bus.setCrossingCost(options.getDouble(ITEM_CROSSING_COST));
      bus.setReroutingEnabled(options.getBool(ITEM_CROSSING_REROUTING));
      bus.setMinimumBusConnectionsCount(options.getInt(ITEM_MINIMUM_CONNECTIONS_COUNT));
    }
  }

  private static Object getBusId(final Graph2D graph, final Edge edge, final DataProvider component, final String busType) {
    if (VALUE_COLOR.equals(busType)) {
      return graph.getRealizer(edge).getLineColor();
    } else if (VALUE_CONNECTED_COMPONENT.equals(busType)) {
      return component.get(edge.source());
    }
    // else if VALUE_SINGLE.equals(busType)
    return VALUE_SINGLE;
  }

  private static byte toBusRouterScope(final Object scopeName) {
    if (VALUE_SCOPE_ALL.equals(scopeName)) {
      return BusRouter.SCOPE_ALL;
    } else if (VALUE_SCOPE_SUBSET.equals(scopeName) ||
               VALUE_SCOPE_SUBSET_BUS.equals(scopeName) ||
               VALUE_SCOPE_PARTIAL.equals(scopeName) ||
               VALUE_SCOPE_EDGES_AT_SELECTED_NODES.equals(scopeName)) {
      return BusRouter.SCOPE_SUBSET;
    } else {
      return BusRouter.SCOPE_ALL;
    }
  }
}
