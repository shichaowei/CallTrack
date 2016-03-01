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
package demo.layout.router;

import y.algo.Trees;
import y.base.DataAcceptor;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.YList;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.layout.router.BusDescriptor;
import y.layout.router.BusRepresentations;
import y.layout.router.BusRouter;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.util.GraphHider;
import y.util.Maps;
import y.view.Graph2D;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Bus Routing for graphs with hubs. The {@link BusRouter} requires a graph without hubs but with edges connecting
 * regular nodes. This class provides methods to calculate this intermediate graph for BusRouter from the original graph
 * and to restore hubs and the original edges of regular nodes after the routing. Note that since the hubs and inner
 * edges of the buses depend on the routing, it is not possible the restore these elements as well.
 */
public class HubRoutingSupport {
  private final BusRouterDemoModule module;

  public HubRoutingSupport() {
    // a slightly customized router module for this demo
    module = new BusRouterDemoModule();
    module.getLayoutExecutor().setLockingView(true);
    module.getLayoutExecutor().getNodePortConfigurator().setAutomaticPortConstraintsEnabled(true);
    // the backup of the realizers is done before the placement of the hubs in method {@link BusRouterDemo#doLayout}
    module.getLayoutExecutor().setBackupRealizersEnabled(false);
  }

  BusRouterDemoModule getModule() {
    return module;
  }

  /**
   * Does the bus routing. This requires three steps: first, create the intermediate graph in which the hubs of each
   * effected bus are replaced by a complete subgraph; secondly, do the routing of the resulting graph; and finally,
   * convert the intermediate graph back to hub representation.
   */
  public void doLayout(final Graph2D graph, final int mode) {
    graph.firePreEvent();
    try {
      doLayoutImpl(graph, mode);
    } finally {
      graph.firePostEvent();
    }
  }

  /**
   * Does the actual bus routing.
   *
   * @see #doLayout(y.view.Graph2D, int)
   */
  protected void doLayoutImpl(final Graph2D graph, final int mode) {
    final EdgeMap edgeDescriptors = graph.createEdgeMap();
    final EdgeMap edgeIdMap = graph.createEdgeMap();

    //noinspection CatchGenericClass
    try {
      storeOriginalEdges(graph);

      // 1) Replace the hubs of each bus in scope by an intermediate complete subgraph.
      EdgeList scopeList;
      try {
        scopeList = replaceHubs(graph, edgeDescriptors, mode);
      } catch (IllegalArgumentException e) {
        String message = "Warning: " + e.getMessage() + "\n" +
            "The routing of only the moved or new elements failed since the remainder is\n" +
            "not a valid bus. All selected buses will be routed completely anew instead.";
        JOptionPane.showMessageDialog((Component) graph.getCurrentView(), message, "Automatic Routing",
            JOptionPane.INFORMATION_MESSAGE);
        doLayoutImpl(graph, BusRouterDemo.MODE_SELECTED);
        return;
      }

      // 2) Do the layout on the intermediate graph. Create required data providers first.
      switch (mode) {
        case BusRouterDemo.MODE_SELECTED:
        case BusRouterDemo.MODE_PARTIAL:
          module.setScope(BusRouter.SCOPE_SUBSET);
          graph.addDataProvider(BusRouter.EDGE_SUBSET_DPKEY, new DataProviderAdapter() {
            public boolean getBool(Object dataHolder) {
              return edgeDescriptors.get(dataHolder) != null;
            }
          });
          break;
        case BusRouterDemo.MODE_ALL:
        default:
          module.setScope(BusRouter.SCOPE_ALL);
          break;
      }
      graph.addDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY, edgeDescriptors);
      module.start(graph);

      // 3) Restore the hubs and the original edges of regular nodes for the new layout.
      BusRepresentations.replaceSubgraphByHubs(graph, scopeList.edges(), edgeDescriptors, edgeIdMap);
      restoreOriginalEdges(graph);
    } finally {
      graph.removeDataProvider(BusRouter.EDGE_DESCRIPTOR_DPKEY);
      graph.removeDataProvider(BusRouter.EDGE_SUBSET_DPKEY);

      graph.disposeEdgeMap(edgeDescriptors);
      graph.disposeEdgeMap(edgeIdMap);
    }
  }

  /**
   * Sets the Id data provider of class {@link BusRepresentations#SOURCE_ID_DPKEY BusRepresentations} for each edge that
   * is connected to a regular node to this edge. This allows to restore the original edges of the regular nodes after
   * the routing.
   *
   * @see #restoreOriginalEdges(y.layout.LayoutGraph)
   */
  protected void storeOriginalEdges(Graph graph) {
    EdgeMap sourcePointIdMap = Maps.createHashedEdgeMap();
    graph.addDataProvider(BusRepresentations.SOURCE_ID_DPKEY, sourcePointIdMap);
    EdgeMap targetPointIdMap = Maps.createHashedEdgeMap();
    graph.addDataProvider(BusRepresentations.TARGET_ID_DPKEY, targetPointIdMap);

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!BusRouterDemo.isHub(edge.source())) {
        sourcePointIdMap.set(edge, edge);
      }
      if (!BusRouterDemo.isHub(edge.target())) {
        targetPointIdMap.set(edge, edge);
      }
    }
  }

  /**
   * Restores the original edges of each regular node from its associated Id data provider of class {@link
   * BusRepresentations#SOURCE_ID_DPKEY BusRepresentations}. Takes care to set the new edge path to the restored edges.
   * Finally, removes the said data providers.
   */
  protected void restoreOriginalEdges(LayoutGraph graph) {
    final DataProvider sourceDataProvider = graph.getDataProvider(BusRepresentations.SOURCE_ID_DPKEY);
    final DataProvider targetDataProvider = graph.getDataProvider(BusRepresentations.TARGET_ID_DPKEY);

    try {
      // iterate over a new list since the graph is modified
      for (EdgeCursor ec = new EdgeList(graph.edges()).edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        final Edge sourceEdge = (Edge) sourceDataProvider.get(edge);
        final Edge targetEdge = (Edge) targetDataProvider.get(edge);
        if (sourceEdge != null && sourceEdge.getGraph() == null && !BusRouterDemo.isHub(edge.source())) {
          // if sourceEdge is already in the graph, the related bus was not part of the routing
          graph.changeEdge(sourceEdge, edge.source(), edge.target());
          graph.reInsertEdge(sourceEdge);
          graph.setPath(sourceEdge, graph.getPath(edge));
          graph.removeEdge(edge);
        }
        if (targetEdge != null && targetEdge.getGraph() == null && !BusRouterDemo.isHub(edge.target())) {
          // if targetEdge is already in the graph, the related bus was not part of the routing
          graph.changeEdge(targetEdge, edge.source(), edge.target());
          graph.reInsertEdge(targetEdge);
          graph.setPath(targetEdge, graph.getPath(edge));
          graph.removeEdge(edge);
        }
      }
    } finally {
      graph.removeDataProvider(BusRepresentations.SOURCE_ID_DPKEY);
      graph.removeDataProvider(BusRepresentations.TARGET_ID_DPKEY);
    }
  }

  /**
   * Converts the buses defined by the hub nodes into the respective complete subgraph in which there are only
   * connections between the regular bus nodes as required by the {@link y.layout.router.BusRouter}. Also, creates
   * appropriate {@link y.layout.router.BusDescriptor}s. If the mode is set to MODE_SELECTED, only buses which contain
   * at least one selected edge or hub are converted. If the mode is set to MODE_SELECTED_PARTS, only buses which
   * contain at least one moveable edge are converted.
   * <p/>
   * This method sets the bus IDs of all edges to the bus color. This is not required for the conversion of the layout
   * but simplifies the restoration of the correct bus color after the layout execution.
   *
   * @param busDescriptors an edge map that will be filled with {@link y.layout.router.BusDescriptor}s
   * @param mode           the mode to use
   * @throws IllegalArgumentException if the fixed subgraph is not a connected orthogonal tree
   */
  protected EdgeList replaceHubs(Graph2D graph, EdgeMap busDescriptors, final int mode) {
    final DataProvider hubDP = graph.getDataProvider(BusRouterDemo.HUB_MARKER_DPKEY);

    // a map which marks fixed and movable edges
    EdgeMap movableMarker = graph.createEdgeMap();

    // 1.) Identify the buses which belong to the scope of the given mode.
    //     Therefore, check for selected edges and end-nodes.
    GraphHider hider = new GraphHider(graph);
    List selectedBuses = new YList();

    //noinspection CatchGenericClass
    try {
      final EdgeList[] busEdgesArray = calculateBusComponents(graph);
      hider.hideAll();

      // iteratively un-hide one bus and its connected nodes
      for (int i = 0; i < busEdgesArray.length; i++) {
        final EdgeList busEdges = busEdgesArray[i];
        final NodeList busNodes = new NodeList();
        for (EdgeCursor ec = busEdges.edges(); ec.ok(); ec.next()) {
          busNodes.add(ec.edge().source());
          busNodes.add(ec.edge().target());
        }
        hider.unhideNodes(busNodes, false);
        hider.unhideEdges(busEdges);

        if (mode == BusRouterDemo.MODE_SELECTED) {
          for (EdgeCursor ec = busEdges.edges(); ec.ok(); ec.next()) {
            final Edge edge = ec.edge();
            if (graph.isSelected(edge) || graph.isSelected(edge.source()) || graph.isSelected(edge.target())) {
              selectedBuses.add(busEdges);
              break;
            }
          }
        } else if (mode == BusRouterDemo.MODE_PARTIAL) {
          if (markMoveableEdges(graph, movableMarker)) {
            selectedBuses.add(busEdges);
          }
        } else {
          selectedBuses.add(busEdges);
        }

        hider.hideAll();
      }
    } catch (RuntimeException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
    } finally {
      hider.unhideAll();
    }

    // Store the bus color of the original edges to set it to the intermediate edges.
    // This is not required but looks good during the layout animation.
    Map idToColor = new HashMap();
    for (Iterator listIter = selectedBuses.iterator(); listIter.hasNext(); ) {
      final EdgeList edgeList = (EdgeList) listIter.next();
      idToColor.put(new Integer(idToColor.size()), graph.getRealizer(edgeList.firstEdge()).getLineColor());
    }

    // 2.a) Remove singleton hubs since they are not handled by BusRepresentations.replaceHubsBySubgraph(..)
    NodeList singletonHubs = new NodeList();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (hubDP.getBool(node) && node.degree() == 0) {
        singletonHubs.add(node);
      }
    }
    for (NodeCursor nc = singletonHubs.nodes(); nc.ok(); nc.next()) {
      graph.removeNode(nc.node());
    }

    // 2.b) Replace the hubs by intermediate edges
    final EdgeList[] selectedBusesArray = (EdgeList[]) selectedBuses.toArray(new EdgeList[selectedBuses.size()]);
    final EdgeList subGraphEdges = BusRepresentations.replaceHubsBySubgraph(graph, selectedBusesArray,
        hubDP,
        mode == BusRouterDemo.MODE_PARTIAL ?
            DataProviders.createNegatedDataProvider(movableMarker) :
            DataProviders.createConstantDataProvider(Boolean.FALSE),
        busDescriptors);

    // Set the bus color to the intermediate edges.
    // This is not required but looks good during the layout animation.
    for (EdgeCursor ec = subGraphEdges.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final BusDescriptor descriptor = (BusDescriptor) busDescriptors.get(edge);
      graph.getRealizer(edge).setLineColor((Color) idToColor.get(descriptor.getID()));
    }

    return subGraphEdges;
  }

  /**
   * Returns whether there is at least one moveable edge in the bus and marks all moveable edges in the provided edge
   * map. This is required only for MODE_SELECTED_PARTS.
   * <p/>
   * This method expects that the graph contains only edges that belong to a common bus. All other edges must be removed
   * or hidden before calling this method.
   *
   * @param moveableMarker a data acceptor in which the movable edges are marked
   * @return <code>true</code> if the bus contains a movable edge
   * @throws IllegalArgumentException if a bus contains movable edges and its fixed subgraph is not a connected
   *                                  orthogonal tree
   */
  protected boolean markMoveableEdges(Graph2D graph, DataAcceptor moveableMarker) {
    final DataProvider hubDP = graph.getDataProvider(BusRouterDemo.HUB_MARKER_DPKEY);

    boolean containsMoveable = false;

    // Iteratively, do a search starting from each selected regular node along paths of degree-2 hubs
    // and hide the discovered edges. These edges are moveable.
    GraphHider hider = new GraphHider(graph);
    for (EdgeCursor ec = new EdgeList(graph.edges()).edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!graph.contains(edge) || (hubDP.getBool(edge.source()) && hubDP.getBool(edge.target()))
          || !(graph.isSelected(edge) || (graph.isSelected(edge.source()) && !hubDP.getBool(edge.source()))
          || (graph.isSelected(edge.target()) && !hubDP.getBool(edge.target())))) {
        // start a search at a regular node either if it is selected by itself or if its edge is selected
        continue;
      }

      containsMoveable = true;
      moveableMarker.setBool(edge, true);
      Node node = hubDP.getBool(edge.source()) ? edge.source() : edge.target();
      hider.hide(edge);

      while (node != null && node.degree() == 1) {
        final Edge chainEdge = node.inDegree() > 0 ? node.firstInEdge() : node.firstOutEdge();
        final Node opposite = chainEdge.opposite(node);

        if (hubDP.getBool(opposite)) {
          moveableMarker.setBool(chainEdge, true);
          hider.hide(chainEdge);
          node = opposite;
        } else {
          node = null;
        }
      }
    }

    if (!containsMoveable) {
      hider.unhideAll();
      return false;
    }

    // Everything that is not hidden is fixed and should be a tree and orthogonal.
    // Find (multi-)edges to regular nodes and hide them for the tree check.
    EdgeList nodeLinks = new EdgeList();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!hubDP.getBool(edge.source()) || !hubDP.getBool(edge.target())) {
        nodeLinks.add(edge);
      }
    }
    hider.hide(nodeLinks);
    if (!Trees.isForest(graph)) {
      hider.unhideAll();
      throw new IllegalArgumentException("Fixed subgraph is not a connected tree.");
    }
    hider.unhideEdges(nodeLinks);

    if (!isOrthogonal(graph)) {
      hider.unhideAll();
      throw new IllegalArgumentException("Fixed subgraph is not orthogonal.");
    }

    hider.unhideAll();
    return true;
  }

  /**
   * Call-back which provides the collection of the bus edges of each bus.
   */
  protected EdgeList[] calculateBusComponents(LayoutGraph graph) {
    return BusRepresentations.toEdgeLists(graph, graph.getDataProvider(BusRouterDemo.HUB_MARKER_DPKEY));
  }

  /**
   * Checks whether all edge paths of a graph are orthogonal.
   *
   * @param graph the graph
   * @return <code>true</code> if all paths are orthogonal.
   */
  private static boolean isOrthogonal(final LayoutGraph graph) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final YPoint[] path = graph.getPath(ec.edge()).toArray();
      for (int i = 1; i < path.length; i++) {
        final YPoint p1 = path[i - 1];
        final YPoint p2 = path[i];
        if (Math.abs(p1.x - p2.x) > 1.0e-5 && Math.abs(p1.y - p2.y) > 1.0e-5) {
          return false;
        }
      }
    }
    return true;
  }
}
