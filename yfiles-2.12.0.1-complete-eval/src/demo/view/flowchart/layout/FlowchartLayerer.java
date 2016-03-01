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
package demo.view.flowchart.layout;

import y.algo.Bfs;
import y.algo.Cycles;
import y.algo.RankAssignments;
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
import y.base.NodeMap;
import y.layout.LayoutGraph;
import y.layout.PortConstraintKeys;
import y.layout.grouping.GroupingKeys;
import y.layout.hierarchic.incremental.EdgeData;
import y.layout.hierarchic.incremental.Layer;
import y.layout.hierarchic.incremental.Layerer;
import y.layout.hierarchic.incremental.Layers;
import y.layout.hierarchic.incremental.LayoutDataProvider;
import y.util.Comparators;
import y.util.GraphHider;

import java.util.Arrays;

/**
 * Customized layering for flowcharts.
 */
class FlowchartLayerer implements Layerer {
  private static final int WEIGHT_DEFAULT_EDGE = 3;
  private static final int WEIGHT_DEFAULT_EDGE_IN_SUBPROCESS = 5;
  private static final int WEIGHT_MESSAGE_FLOW = 3;
  private static final int WEIGHT_ASSOCIATION = 2;

  private static final int MIN_LENGTH_DEFAULT_EDGE = 1;
  private static final int MIN_LENGTH_FLATWISE_BRANCH = 0;
  private static final int MIN_LENGTH_MESSAGE_FLOW = 0;
  private static final int MIN_LENGTH_ASSOCIATION = 0;

  private static final double CYCLE_WEIGHT_BACKEDGE = 1.0;
  private static final double CYCLE_WEIGHT_NON_BACKEDGE = 5.0;

  private boolean assignStartNodesToLeftOrTop;
  private boolean allowFlatwiseDefaultFlow;

  FlowchartLayerer() {
    assignStartNodesToLeftOrTop = false;
  }

  public boolean isAssignStartNodesToLeftOrTop() {
    return assignStartNodesToLeftOrTop;
  }

  public void setAssignStartNodesToLeftOrTop(boolean assignStartNodesToLeftOrTop) {
    this.assignStartNodesToLeftOrTop = assignStartNodesToLeftOrTop;
  }

  public boolean isAllowFlatwiseDefaultFlow() {
    return allowFlatwiseDefaultFlow;
  }

  public void setAllowFlatwiseDefaultFlow(boolean allowFlatwiseDefaultFlow) {
    this.allowFlatwiseDefaultFlow = allowFlatwiseDefaultFlow;
  }

  public void assignLayers(LayoutGraph graph, Layers layers, LayoutDataProvider ldp) {
    final EdgeList reversedEdges = reverseCycles(graph);

    //assign weights/min length to edges
    GraphHider hider = new GraphHider(graph);
    EdgeMap minLength = graph.createEdgeMap();
    NodeMap node2Layer = graph.createNodeMap();
    EdgeMap weight = graph.createEdgeMap();

    try {
      // transform graph
      final NodeList dummies = insertDummyEdges(graph, hider, weight, minLength);
      dummies.add(insertSuperRoot(graph, weight, minLength));

      // assign layers
      RankAssignments.simplex(graph, node2Layer, weight, minLength);

      // undo graph transformation
      for (NodeCursor nc = dummies.nodes(); nc.ok(); nc.next()) {
        graph.removeNode(nc.node());
      }
      hider.unhideAll();
      for (EdgeCursor ec = reversedEdges.edges(); ec.ok(); ec.next()) {
        graph.reverseEdge(ec.edge());
      }

      // special handling for some degree 1 nodes (draw the incident edge as same layer edge)
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        if (isDegreeOneNode(node, ldp)) {
          handleDegreeOneNode(node, graph, node2Layer, ldp);
        }
      }

      // build result data structure
      int layerCount = normalize(graph, node2Layer);
      for (int i = 0; i < layerCount; i++) {
        layers.insert(Layer.TYPE_NORMAL, i);
      }
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node node = nc.node();
        int layer = node2Layer.getInt(node);
        layers.getLayer(layer).add(node);
      }
    } finally {
      //dispose
      graph.disposeEdgeMap(weight);
      graph.disposeEdgeMap(minLength);
      graph.disposeNodeMap(node2Layer);
    }
  }

  private NodeList insertDummyEdges(LayoutGraph graph, GraphHider hider, DataAcceptor weight,
                                    DataAcceptor minLength) {
    final boolean nodeTypeSet = graph.getDataProvider(FlowchartLayouter.NODE_TYPE_DPKEY) != null;

    final DataProvider parentNodeIdDP = graph.getDataProvider(GroupingKeys.PARENT_NODE_ID_DPKEY);
    final DataProvider preferredDirectionDP = graph.getDataProvider(FlowchartLayouter.PREFERRED_DIRECTION_KEY);
    final DataProvider groupingNodesDP = graph.getDataProvider(FlowchartTransformerStage.GROUPING_NODES_DP_KEY);
    final DataProvider targetGroupIdDP = graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);

    final NodeMap outEdgeBranchTypes = graph.createNodeMap();
    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      final Node node = nodeCursor.node();
      int type = 0;
      for (EdgeCursor edgeCursor = node.outEdges(); edgeCursor.ok(); edgeCursor.next()) {
        type |= preferredDirectionDP.getInt(edgeCursor.edge());
      }
      outEdgeBranchTypes.setInt(node, type);
    }

    final NodeList dummies = new NodeList();

    final Edge[] edges = graph.getEdgeArray();
    for (int i = 0; i < edges.length; i++) {
      final Edge edge = edges[i];

      switch (getType(graph, edge)) {
        case FlowchartElements.EDGE_TYPE_MESSAGE_FLOW: {
          final Node dummyNode = graph.createNode();
          dummies.add(dummyNode);

          Edge dummyEdge1 = graph.createEdge(edge.source(), dummyNode);
          weight.setInt(dummyEdge1, WEIGHT_MESSAGE_FLOW);
          minLength.setInt(dummyEdge1, MIN_LENGTH_MESSAGE_FLOW);

          Edge dummyEdge2 = graph.createEdge(edge.target(), dummyNode);
          weight.setInt(dummyEdge2, WEIGHT_MESSAGE_FLOW);
          minLength.setInt(dummyEdge2, MIN_LENGTH_MESSAGE_FLOW);

          hider.hide(edge);
          break;
        }
        case FlowchartElements.EDGE_TYPE_ASSOCIATION: {
          final Node dummyNode = graph.createNode();
          dummies.add(dummyNode);

          Edge dummyEdge1 = graph.createEdge(edge.source(), dummyNode);
          weight.setInt(dummyEdge1, WEIGHT_ASSOCIATION);
          minLength.setInt(dummyEdge1, MIN_LENGTH_ASSOCIATION);

          Edge dummyEdge2 = graph.createEdge(edge.target(), dummyNode);
          weight.setInt(dummyEdge2, WEIGHT_ASSOCIATION);
          minLength.setInt(dummyEdge2, MIN_LENGTH_ASSOCIATION);

          hider.hide(edge);
          break;
        }
        default: {
          weight.setInt(edge, isContainedInSubProcess(edge, graph, parentNodeIdDP, nodeTypeSet) ?
              WEIGHT_DEFAULT_EDGE_IN_SUBPROCESS : WEIGHT_DEFAULT_EDGE);

          if (isFlatwiseConnectorGroupingEdge(groupingNodesDP, edge) &&
              !FlowchartLayouter.isStraightBranch(preferredDirectionDP, edge)) {
            minLength.setInt(edge, MIN_LENGTH_FLATWISE_BRANCH);

          } else if (isFirstGroupingEdgeToSucceedingLayers(groupingNodesDP, edge)) {
            minLength.setInt(edge, MIN_LENGTH_FLATWISE_BRANCH);

          } else if (!allowFlatwiseDefaultFlow
              || !FlowchartLayouter.isFlatwiseBranch(preferredDirectionDP, edge)
              || containsOnlyFlatwise(outEdgeBranchTypes.getInt(edge.target()))
              || isValueSet(targetGroupIdDP, edge)) {
            minLength.setInt(edge, MIN_LENGTH_DEFAULT_EDGE);

          } else {
            minLength.setInt(edge, MIN_LENGTH_FLATWISE_BRANCH);
          }
          break;
        }
      }
    }

    return dummies;
  }

  /**
   * Inserts a super root to guarantee that graph is connected.
   */
  private Node insertSuperRoot(LayoutGraph graph, DataAcceptor weight, DataAcceptor minLength) {
    final Node superRoot = graph.createNode();

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node v = nc.node();
      if (!v.equals(superRoot) && v.inDegree() == 0) {
        final Edge dummyEdge = graph.createEdge(superRoot, v);
        weight.setInt(dummyEdge, assignStartNodesToLeftOrTop && FlowchartElements.isStartEvent(graph, v) ? 100 : 0);
        minLength.setInt(dummyEdge, 0);
      }
    }

    return superRoot;
  }

  private static EdgeList reverseCycles(LayoutGraph graph) {
    //we only consider edges of type sequence flow
    final GraphHider hider = new GraphHider(graph);
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      if ((int) getType(graph, e) != (int) FlowchartElements.EDGE_TYPE_SEQUENCE_FLOW) {
        hider.hide(e);
      }
    }

    final EdgeMap edge2Weight = graph.createEdgeMap();
    final EdgeMap cyclingEdges = graph.createEdgeMap();
    final EdgeList reversedEdges;
    try {
      // try to identify backedges and assign lower weights to them
      NodeList coreNodes = new NodeList();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        if (n.inDegree() == 0) {
          coreNodes.add(n);
        }
      }

      final NodeMap node2Depth = graph.createNodeMap();
      try {
        Bfs.getLayers(graph, coreNodes, true, node2Depth);
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          Edge e = ec.edge();
          if (node2Depth.getInt(e.source()) > node2Depth.getInt(e.target())) {
            //likely to be a backedge
            edge2Weight.setDouble(e, CYCLE_WEIGHT_BACKEDGE);
          } else {
            edge2Weight.setDouble(e, CYCLE_WEIGHT_NON_BACKEDGE);
          }
        }
      } finally {
        graph.disposeNodeMap(node2Depth);
      }

      // find and remove cycles
      reversedEdges = new EdgeList();

      Cycles.findCycleEdges(graph, cyclingEdges, edge2Weight);
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        Edge e = ec.edge();
        if (cyclingEdges.getBool(e)) {
          graph.reverseEdge(e);
          reversedEdges.add(e);
        }
      }
    } finally {
      graph.disposeEdgeMap(cyclingEdges);
      graph.disposeEdgeMap(edge2Weight);
      hider.unhideAll();
    }

    return reversedEdges;
  }

  private static byte getType(LayoutGraph g, Edge e) {
    if (!FlowchartElements.isUndefined(g, e)) {
      return FlowchartElements.getType(g, e);
    } else {
      //special handling if constraint incremental layerer calls this layerer
      final String originalEdgeDpKey = "y.layout.hierarchic.incremental.ConstraintIncrementalLayerer.ORIG_EDGES";

      final DataProvider edge2OrigEdge = g.getDataProvider(originalEdgeDpKey);
      if (edge2OrigEdge != null && edge2OrigEdge.get(e) != null) {
        final Edge realEdge = (Edge) edge2OrigEdge.get(e);
        if (!FlowchartElements.isUndefined(realEdge.getGraph(), realEdge)) {
          return FlowchartElements.getType(realEdge.getGraph(), realEdge);
        }
      }

      return FlowchartElements.isAnnotation(g, e.source()) || FlowchartElements.isAnnotation(g, e.target()) ?
          FlowchartElements.EDGE_TYPE_ASSOCIATION : FlowchartElements.EDGE_TYPE_SEQUENCE_FLOW;
    }
  }

  private static boolean isContainedInSubProcess(Edge e, LayoutGraph g, DataProvider node2Parent,
                                                 boolean considerNodeType) {
    if (node2Parent == null) {
      return false;
    }

    final Node sourceParent = (Node) node2Parent.get(e.source());
    final Node targetParent = (Node) node2Parent.get(e.target());
    return sourceParent != null && sourceParent.equals(targetParent) &&
        (!considerNodeType || FlowchartElements.isGroup(g, sourceParent));
  }

  //Be careful: due to the handling of edges attaching to group nodes the degree of "degree-one" nodes may be > 1.
  //We are interested in nodes with degree one in the initial graph.
  private static boolean isDegreeOneNode(Node n, LayoutDataProvider ldp) {
    int realDegree = 0;
    for (EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
      if (isRealEdge(ldp.getEdgeData(ec.edge()))) {
        realDegree++;
        if (realDegree > 1) {
          return false;
        }
      }
    }
    return realDegree == 1;
  }

  private static void handleDegreeOneNode(Node n, LayoutGraph g, NodeMap node2Layer, LayoutDataProvider ldp) {
    if (!FlowchartElements.isEvent(g, n) || FlowchartElements.isStartEvent(g, n)) {
      return;
    }

    final Edge realEdge = findIncidentRealEdge(ldp, n);
    if (realEdge == null) {
      return;
    }

    final Node opposite = realEdge.opposite(n);
    int sameLayerEdgeCount = 0;
    int oppositeOutDegree = 0;
    int oppositeInDegree = 0;
    for (EdgeCursor ec = opposite.outEdges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      if (!e.equals(realEdge) && isRealEdge(ldp.getEdgeData(e))) {
        final int layerDiff = node2Layer.getInt(e.source()) - node2Layer.getInt(e.target());
        if (layerDiff > 0) {
          oppositeInDegree++;
        } else if (layerDiff == 0) {
          sameLayerEdgeCount++;
        } else {
          oppositeOutDegree++;
        }
      }
    }
    for (EdgeCursor ec = opposite.inEdges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      if (!e.equals(realEdge) && isRealEdge(ldp.getEdgeData(e))) {
        final int layerDiff = node2Layer.getInt(e.source()) - node2Layer.getInt(e.target());
        if (layerDiff > 0) {
          oppositeOutDegree++;
        } else if (layerDiff == 0) {
          sameLayerEdgeCount++;
        } else {
          oppositeInDegree++;
        }
      }
    }

    if ((realEdge.target().equals(n) && sameLayerEdgeCount < 2 && oppositeOutDegree >= 1 && oppositeInDegree <= 2)
        || (realEdge.source().equals(n) && sameLayerEdgeCount < 2 && oppositeInDegree >= 1 && oppositeOutDegree <= 2)) {
      node2Layer.setInt(n, node2Layer.getInt(opposite));
    }
  }

  private static boolean isRealEdge(final EdgeData eData) {
    return (eData != null) && ((int) eData.getType() == (int) EdgeData.TYPE_NORMAL);
  }

  private static Edge findIncidentRealEdge(LayoutDataProvider ldp, Node n) {
    for (EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (isRealEdge(ldp.getEdgeData(edge))) {
        return edge;
      }
    }

    return null;
  }

  private static boolean containsOnlyFlatwise(int branchType) {
    return branchType != 0 && (branchType & FlowchartLayouter.DIRECTION_STRAIGHT) == 0;
  }

  private static boolean isFlatwiseConnectorGroupingEdge(DataProvider groupingDummies, Edge edge) {
    return groupingDummies != null &&
        ((edge.target().inDegree() > 1 && groupingDummies.getInt(edge.source()) == 0 &&
            groupingDummies.getInt(edge.target()) == FlowchartTransformerStage.NODE_TYPE_PRECEDING_LAYER) ||
            (edge.source().outDegree() > 1 && groupingDummies.getInt(edge.target()) == 0 &&
                groupingDummies.getInt(edge.source()) == FlowchartTransformerStage.NODE_TYPE_SUCCEEDING_LAYER));
  }

  private static boolean isFirstGroupingEdgeToSucceedingLayers(DataProvider groupingDummies, Edge edge) {
    return groupingDummies != null
        && groupingDummies.getInt(edge.source()) == 0
        && groupingDummies.getInt(edge.target()) == FlowchartTransformerStage.NODE_TYPE_SUCCEEDING_LAYER;
  }

  private static boolean isValueSet(DataProvider dataProvider, Edge edge) {
    return dataProvider != null && dataProvider.get(edge) != null;
  }

  /**
   * Removes empty layers and ensures that the smallest layer has value 0.
   */
  private static int normalize(Graph g, NodeMap layer) {
    if (g.isEmpty()) {
      return 0;
    }

    Node[] nodes = g.getNodeArray();
    Arrays.sort(nodes, Comparators.createIntDataComparator(layer));
    int lastLayer = layer.getInt(nodes[0]);
    int realLayer = 0;
    for (int i = 0; i < nodes.length; i++) {
      int currentLayer = layer.getInt(nodes[i]);
      if (currentLayer != lastLayer) {
        realLayer++;
        lastLayer = currentLayer;
      }
      layer.setInt(nodes[i], realLayer);
    }
    return realLayer + 1;
  }

}

