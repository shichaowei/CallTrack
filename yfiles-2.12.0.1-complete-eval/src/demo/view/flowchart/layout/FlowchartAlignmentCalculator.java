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

import y.algo.Cycles;
import y.algo.GraphConnectivity;
import y.algo.Paths;
import y.algo.RankAssignments;
import y.base.DataAcceptor;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.GraphInterface;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YCursor;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.PortConstraint;
import y.layout.grouping.GroupingKeys;
import y.layout.hierarchic.incremental.EdgeData;
import y.layout.hierarchic.incremental.LayoutDataProvider;
import y.layout.hierarchic.incremental.NodeData;
import y.layout.grid.PartitionGrid;
import y.util.Comparators;
import y.util.DataProviderAdapter;
import y.util.GraphHider;
import y.util.GraphPartitionManager;
import y.util.Maps;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class FlowchartAlignmentCalculator {
  private byte layoutOrientation;

  FlowchartAlignmentCalculator() {
    this.layoutOrientation = LayoutOrientation.TOP_TO_BOTTOM;
  }

  public byte getLayoutOrientation() {
    return layoutOrientation;
  }

  public void setLayoutOrientation(byte layoutOrientation) {
    this.layoutOrientation = layoutOrientation;
  }

  public void determineAlignment(LayoutGraph graph, LayoutDataProvider ldp, NodeMap nodeAlignment, EdgeMap edgeLength) {
    graph.sortEdges(new FlowchartPortOptimizer.PositionEdgeComparator(true, ldp),
        new FlowchartPortOptimizer.PositionEdgeComparator(false, ldp));

    final DataProvider edgeIsAlignable = determineAlignableEdges(graph, ldp);
    determineEdgeLengths(graph, ldp, edgeIsAlignable, edgeLength);
    final DataProvider edgePriority = determineEdgePriorities(graph, edgeIsAlignable, edgeLength);

    final NodeAlignmentCalculator nodeAlignmentCalculator = new NodeAlignmentCalculator(layoutOrientation);
    nodeAlignmentCalculator.calculateAlignment(graph, ldp, edgeIsAlignable, edgePriority, nodeAlignment);

    graph.addDataProvider(FlowchartPortOptimizer.NODE_TO_ALIGN_DP_KEY, nodeAlignment);

//    // TODO remove debug code
//    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
//      final Node node = nodeCursor.node();
//      final Object o = nodeAlignment.get(node);
//      if (o != null) {
//        System.out.println("Align '" + node + "' with '" + o + '\'');
//      }
//    }
  }

  boolean isSpecialNode(Graph graph, Node n, LayoutDataProvider ldp) {
    return (ldp.getNodeData(n).getType() == NodeData.TYPE_NORMAL)
        && !FlowchartElements.isAnnotation(n.getGraph(), n)
        && !FlowchartTransformerStage.isGroupingDummy(graph, n);
  }

  boolean isRelevant(Graph graph, Edge e, LayoutDataProvider ldp) {
    return !FlowchartElements.isMessageFlow(graph, FlowchartPortOptimizer.getOriginalEdge(e, ldp));
  }

  /**
   * Returns true if the given edge can be aligned, that is its port constraints are not flatwise, and its end nodes are
   * not in different swimlanes and do not belong to different groups.
   */
  private boolean isAlignable(GraphInterface graph, LayoutDataProvider ldp, Edge edge) {
    final EdgeData edgeData = ldp.getEdgeData(edge);

    if (hasFlatwisePortConstraint(edgeData) || hasFlatwiseCandidateCollection(edgeData, getLayoutOrientation())) {
      return false;
    }

    final Node source = edge.source();
    final Node target = edge.target();
    final int laneId1 = FlowchartPortOptimizer.getSwimlaneId(source, ldp);
    final int laneId2 = FlowchartPortOptimizer.getSwimlaneId(target, ldp);
    if (laneId1 != -1 && laneId1 != laneId2) {
      return false;
    }

    final DataProvider node2Parent = graph.getDataProvider(GroupingKeys.PARENT_NODE_ID_DPKEY);
    return node2Parent == null || node2Parent.get(source) == null ||
        node2Parent.get(source).equals(node2Parent.get(target));
  }

  private static boolean isRealEdge(Edge edge, LayoutDataProvider layoutData) {
    return layoutData.getNodeData(edge.source()).getType() == NodeData.TYPE_NORMAL
        && layoutData.getNodeData(edge.target()).getType() == NodeData.TYPE_NORMAL;
  }

  private static boolean isStraightBranch(LayoutGraph graph, Edge edge, LayoutDataProvider ldp) {
    return FlowchartLayouter.isStraightBranch(graph, FlowchartPortOptimizer.getOriginalEdge(edge, ldp));
  }

  private DataProvider determineAlignableEdges(LayoutGraph graph, LayoutDataProvider ldp) {
    EdgeMap edgeIsAlignable = Maps.createHashedEdgeMap();

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      edgeIsAlignable.setBool(e, isAlignable(graph, ldp, e) && isRelevant(graph, e, ldp));
    }

    return edgeIsAlignable;
  }

  /**
   * Determines edge lengths such that, in longest paths, edges are preferred that guarantee a suitable port
   * assignment.
   */
  private void determineEdgeLengths(LayoutGraph graph, LayoutDataProvider layoutData,
                                    DataProvider edgeIsAlignable, EdgeMap edgeLength) {
    final int ZERO_LENGTH = 0;
    final int BASIC_DUMMY_EDGE_LENGTH = 1;
    final int BASIC_EDGE_LENGTH = 5;
    final int PENALTY_LENGTH = BASIC_EDGE_LENGTH + graph.nodeCount();
    final int HIGH_PENALTY_LENGTH = PENALTY_LENGTH * 8;

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();

      if (hasFlatwisePortConstraint(layoutData.getEdgeData(e))) {
        edgeLength.setInt(e, ZERO_LENGTH);
      } else if (isRealEdge(e, layoutData)) {
        edgeLength.setInt(e, BASIC_EDGE_LENGTH);
      } else {
        edgeLength.setInt(e, BASIC_DUMMY_EDGE_LENGTH);
      }
    }

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();

      final NodeData nodeData = layoutData.getNodeData(node);
      final byte type = nodeData.getType();
      if (type == NodeData.TYPE_SOURCE_GROUP_NODE || type == NodeData.TYPE_TARGET_GROUP_NODE) {
        // assign higher length to inner edges
        final Edge[] edges = type == NodeData.TYPE_SOURCE_GROUP_NODE ?
            new EdgeList(node.outEdges()).toEdgeArray() :
            new EdgeList(node.inEdges()).toEdgeArray();
        for (int i = 1; i < edges.length - 1; i++) {
          edgeLength.setInt(edges[i], edgeLength.getInt(edges[i]) + BASIC_EDGE_LENGTH);
        }
      }

      if (!isSpecialNode(graph, node, layoutData) || node.degree() < 3) {
        continue;
      }

      if (node.outDegree() == 2 && node.inDegree() == 2) {
        final Edge firstIn = node.firstInEdge();
        final Edge lastOut = node.lastOutEdge();
        final Edge lastIn = node.lastInEdge();
        final Edge firstOut = node.firstOutEdge();
        final boolean preventFirstIn = !edgeIsAlignable.getBool(firstIn) || !edgeIsAlignable.getBool(lastOut);
        final boolean preventFirstOut = !edgeIsAlignable.getBool(firstOut) || !edgeIsAlignable.getBool(lastIn);

        if (!preventFirstOut || !preventFirstIn) {
          if (preventFirstIn) {
            edgeLength.setInt(firstIn, ZERO_LENGTH);
            edgeLength.setInt(lastOut, ZERO_LENGTH);
          }

          if (preventFirstOut) {
            edgeLength.setInt(firstOut, ZERO_LENGTH);
            edgeLength.setInt(lastIn, ZERO_LENGTH);
          }

          if (edgeLength.getInt(firstIn) + edgeLength.getInt(lastOut) >
              edgeLength.getInt(lastIn) + edgeLength.getInt(firstOut)) {
            edgeLength.setInt(firstIn, edgeLength.getInt(firstIn) + HIGH_PENALTY_LENGTH);
            edgeLength.setInt(lastOut, edgeLength.getInt(lastOut) + HIGH_PENALTY_LENGTH);
          } else {
            edgeLength.setInt(lastIn, edgeLength.getInt(lastIn) + HIGH_PENALTY_LENGTH);
            edgeLength.setInt(firstOut, edgeLength.getInt(firstOut) + HIGH_PENALTY_LENGTH);
          }
          continue;
        }
      }

      boolean hasStraightBranch = false;

      for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (isStraightBranch(graph, edge, layoutData)) {
          hasStraightBranch = true;
          edgeLength.setInt(edge, edgeLength.getInt(edge) + PENALTY_LENGTH);
        }
      }

      if (!hasStraightBranch) {
        final Edge[] edges = node.outDegree() >= node.inDegree() ?
            new EdgeList(node.outEdges()).toEdgeArray() : new EdgeList(node.inEdges()).toEdgeArray();

        //assign high length to inner edges (the two non-inner edges should be attached to the side ports)
        for (int i = 1; i < edges.length - 1; i++) {
          edgeLength.setInt(edges[i], edgeLength.getInt(edges[i]) + PENALTY_LENGTH);
        }
      }
    }

//    // TODO remove debug code
//    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
//      final Edge edge = edgeCursor.edge();
//      System.out.println(edge + " l:" + edgeLength.getDouble(edge));
//    }
  }

  private static DataProvider determineEdgePriorities(LayoutGraph graph, DataProvider edgeIsAlignable,
                                                      DataProvider edgeLength) {
    final EdgeMap edgePriority = Maps.createHashedEdgeMap();

    final GraphHider hider = new GraphHider(graph);
    try {
      // hide irrelevant edges
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        Edge e = ec.edge();
        edgePriority.setInt(e, FlowchartPortOptimizer.PRIORITY_BASIC);

        if (!edgeIsAlignable.getBool(e)) {
          hider.hide(e);
        }
      }

      // for each connected component we iteratively find a longest path that is used as critical path
      NodeMap node2CompId = Maps.createHashedNodeMap();
      int compCount = GraphConnectivity.connectedComponents(graph, node2CompId);
      GraphPartitionManager gpm = new GraphPartitionManager(graph, node2CompId);

      try {
        gpm.hideAll();

        for (int i = 0; i < compCount; i++) {
          gpm.displayPartition(new Integer(i));
          GraphHider localHider = new GraphHider(graph);
          try {
            EdgeList path = Paths.findLongestPath(graph, edgeLength);
            while (!path.isEmpty()) {
              // TODO remove debug code
//              System.out.println("** PATH **");
              for (EdgeCursor ec = path.edges(); ec.ok(); ec.next()) {
                edgePriority.setInt(ec.edge(), FlowchartPortOptimizer.PRIORITY_HIGH);
//                System.out.println("* " + ec.edge() + " l:" + edgeLength.getDouble(ec.edge()));
              }
              localHider.hide(Paths.constructNodePath(path));
              path = Paths.findLongestPath(graph, edgeLength);
            }
          } finally {
            localHider.unhideAll();
          }
        }

      } finally {
        gpm.unhideAll();
      }

    } finally {
      hider.unhideAll();
    }

    return edgePriority;
  }

  private static boolean hasFlatwisePortConstraint(final EdgeData edgeData) {
    return FlowchartPortOptimizer.isFlatwisePortConstraint(
        edgeData.getSPC()) || FlowchartPortOptimizer.isFlatwisePortConstraint(edgeData.getTPC());
  }

  private static boolean hasFlatwiseCandidateCollection(final EdgeData edgeData, final byte layoutOrientation) {
    return FlowchartPortOptimizer.isFlatwiseCandidateCollection(edgeData.getSourceCandidates(), layoutOrientation)
        || FlowchartPortOptimizer.isFlatwiseCandidateCollection(edgeData.getTargetCandidates(), layoutOrientation);
  }

  /**
   * @noinspection ImplicitNumericConversion
   */
  static class NodeAlignmentCalculator {

    private final byte layoutOrientation;

    NodeAlignmentCalculator(byte layoutOrientation) {
      this.layoutOrientation = layoutOrientation;
    }

    boolean isHorizontalOrientation() {
      return (int) layoutOrientation == (int) LayoutOrientation.LEFT_TO_RIGHT;
    }

    void calculateAlignment(LayoutGraph graph, final LayoutDataProvider ldp, DataProvider edgeAlignable,
                            DataProvider edgePriority, DataAcceptor nodeAlignment) {
      final PartitionGrid grid = PartitionGrid.getPartitionGrid(graph);

      if (grid == null) {
        calculateAlignmentImpl(graph, ldp, edgeAlignable, edgePriority, nodeAlignment);

      } else {
        final GraphPartitionManager columnPartitionManager = new GraphPartitionManager(graph,
            new DataProviderAdapter() {
              public Object get(Object dataHolder) {
                final int swimlaneID = FlowchartPortOptimizer.getSwimlaneId((Node) dataHolder, ldp);
                return swimlaneID < 0 ? dataHolder : new Integer(swimlaneID);
              }
            });

        try {
          columnPartitionManager.hideAll();
          for (int i = 0; i < grid.getColumns().size(); i++) {
            columnPartitionManager.displayPartition(new Integer(i));
            if (graph.nodeCount() > 1) {
              calculateAlignmentImpl(graph, ldp, edgeAlignable, edgePriority, nodeAlignment);
            }
          }
        } finally {
          columnPartitionManager.unhideAll();
        }
      }
    }

    private void calculateAlignmentImpl(LayoutGraph graph, LayoutDataProvider ldp, DataProvider edgeAlignable,
                                        DataProvider edgePriority, DataAcceptor node2AlignWith) {
      final NodeMap node2LaneAlignment = createLaneAlignmentMap(graph, ldp);

      EdgeMap edgeMinLength = Maps.createHashedEdgeMap();
      EdgeMap edgeWeight = Maps.createHashedEdgeMap();

      NodeMap node2NetworkRep = Maps.createHashedNodeMap();
      Map groupNode2BeginRep = new HashMap();
      Map groupNode2EndRep = new HashMap();
      Graph network = new Graph();

      //create network nodes
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        NodeData data = ldp.getNodeData(n);
        Node nRep;
        if (data != null && data.getType() == NodeData.TYPE_GROUP_BEGIN) {
          //all group begin dummies of the same group node are mapped to the same network node
          nRep = (Node) groupNode2BeginRep.get(data.getGroupNode());
          if (nRep == null) {
            nRep = network.createNode();
            groupNode2BeginRep.put(data.getGroupNode(), nRep);
          }
        } else if (data != null && data.getType() == NodeData.TYPE_GROUP_END) {
          //all group end dummies of the same group node are mapped to the same network node
          nRep = (Node) groupNode2EndRep.get(data.getGroupNode());
          if (nRep == null) {
            nRep = network.createNode();
            groupNode2EndRep.put(data.getGroupNode(), nRep);
          }
        } else {
          nRep = network.createNode();
        }
        node2NetworkRep.set(n, nRep);
      }

      //consider edges
      final EdgeList nonAlignableEdges = new EdgeList();

      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge e = ec.edge();
        if (e.isSelfLoop() || (isGroupNodeBorder(e.source(), ldp) && isGroupNodeBorder(e.target(), ldp))) {
          continue;
        }

        if (!edgeAlignable.getBool(e)) {
          nonAlignableEdges.add(e);
          continue;
        }

        Node absNode = network.createNode();
        int priority = edgePriority.getInt(e);
        Node sRep = (Node) node2NetworkRep.get(e.source());
        Node tRep = (Node) node2NetworkRep.get(e.target());
        Edge sConnector = network.createEdge(sRep, absNode);
        edgeMinLength.setInt(sConnector, 0);
        edgeWeight.setInt(sConnector, priority);
        Edge tConnector = network.createEdge(tRep, absNode);
        edgeMinLength.setInt(tConnector, 0);
        edgeWeight.setInt(tConnector, priority);
      }

      //also consider same layer edges
      for (EdgeCursor ec = FlowchartPortOptimizer.getSameLayerEdges(graph, ldp).edges(); ec.ok(); ec.next()) {
        Edge e = ec.edge();
        if (!e.isSelfLoop() && (!isGroupNodeBorder(e.source(), ldp) || !isGroupNodeBorder(e.target(), ldp))) {
          Node sRep = (Node) node2NetworkRep.get(e.source());
          Node tRep = (Node) node2NetworkRep.get(e.target());
          Edge connector = ldp.getNodeData(e.source()).getPosition() < ldp.getNodeData(e.target()).getPosition() ?
              network.createEdge(sRep, tRep) : network.createEdge(tRep, sRep);
          edgeMinLength.setInt(connector, 1);
          edgeWeight.setInt(connector, FlowchartPortOptimizer.PRIORITY_BASIC);
        }
      }

      Node[] nodes = graph.getNodeArray();
      Arrays.sort(nodes, new NodePositionComparator(ldp));
      Node last = null;
      for (int i = 0; i < nodes.length; i++) {
        Node n = nodes[i];
        if (last != null && areInSameLayer(last, n, ldp)) {
          Node nRep = (Node) node2NetworkRep.get(n);
          Node lastRep = (Node) node2NetworkRep.get(last);
          if (!network.containsEdge(lastRep, nRep)) {
            Edge connector = network.createEdge(lastRep, nRep); //guarantees that last is placed to the left of n
            int minLength = calcMinLength(last, n, graph, ldp);
            edgeMinLength.setInt(connector, minLength);
            edgeWeight.setInt(connector, 0);
          }
        }
        last = n;
      }

      // For each non-alignable edge, we create a connector with min length 1,
      // but only it has no other alignable in-edge.
      final EdgeMap nonAlignableConnectorMap = Maps.createHashedEdgeMap();
      for (EdgeCursor edgeCursor = nonAlignableEdges.edges(); edgeCursor.ok(); edgeCursor.next()) {
        final Edge edge = edgeCursor.edge();
        final boolean hasAlignableInEdge = checkPredicate(edge.target().inEdges(), edgeAlignable);
        if (hasAlignableInEdge) {
          continue;
        }

        final Node sRep = (Node) node2NetworkRep.get(edge.source());
        final Node tRep = (Node) node2NetworkRep.get(edge.target());
        final EdgeData edgeData = ldp.getEdgeData(edge);

        final Edge connector;
        if (hasLeftConstraint(edgeData, true) || hasRightConstraint(edgeData, false)) {
          connector = network.createEdge(tRep, sRep);
        } else if (hasRightConstraint(edgeData, true) || hasLeftConstraint(edgeData, false)) {
          connector = network.createEdge(sRep, tRep);
        } else {
          continue;
        }

        nonAlignableConnectorMap.setBool(connector, true);
        edgeMinLength.setInt(connector, 1);
        edgeWeight.setInt(connector, FlowchartPortOptimizer.PRIORITY_BASIC);
      }

      // Afterwards, we ensure that the network is still acyclic.
      for (EdgeList cycle = Cycles.findCycle(network, true);
           !cycle.isEmpty(); cycle = Cycles.findCycle(network, true)) {
        boolean removed = false;
        for (EdgeCursor ec = cycle.edges(); ec.ok() && !removed; ec.next()) {
          final Edge edge = ec.edge();
          if (nonAlignableConnectorMap.getBool(edge)) {
            network.removeEdge(edge);
            removed = true;
          }
        }

        if (!removed) {
          network.removeEdge(cycle.firstEdge());
        }
      }

      //connect nodes to global source/sink
      Node globalSource = network.createNode();
      Node globalSink = network.createNode();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        Node nRep = (Node) node2NetworkRep.get(n);
        int nLaneAlignment = node2LaneAlignment.getInt(n);
        if (!network.containsEdge(nRep, globalSink)) {
          Edge globalSinkConnector = network.createEdge(nRep, globalSink);
          edgeWeight.setInt(globalSinkConnector, nLaneAlignment == FlowchartPortOptimizer.LANE_ALIGNMENT_RIGHT ?
              FlowchartPortOptimizer.PRIORITY_LOW : 0);
          edgeMinLength.setInt(globalSinkConnector, 0);
        }
        if (!network.containsEdge(globalSource, nRep)) {
          Edge globalSourceConnector = network.createEdge(globalSource, nRep);
          edgeWeight.setInt(globalSourceConnector, nLaneAlignment == FlowchartPortOptimizer.LANE_ALIGNMENT_LEFT ?
              FlowchartPortOptimizer.PRIORITY_LOW : 0);
          edgeMinLength.setInt(globalSourceConnector, 0);
        }
      }

      //apply simplex to each connected component of the network
      NodeMap networkNode2AlignmentLayer = Maps.createHashedNodeMap();
      RankAssignments.simplex(network, networkNode2AlignmentLayer, edgeWeight, edgeMinLength);

      //transfer results to original nodes
      NodeMap node2AlignmentLayer = Maps.createHashedNodeMap();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        Node nRep = (Node) node2NetworkRep.get(n);
        node2AlignmentLayer.setDouble(n, networkNode2AlignmentLayer.getInt(nRep));
      }

      //we do not want to align bend nodes with common nodes except if the (chain of) dummy nodes can be aligned with the corresponding common node
      NodeMap seenBendMap = Maps.createHashedNodeMap();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        if (isBendNode(n, ldp) && !seenBendMap.getBool(n)) {
          adjustAlignmentLayer(n, node2AlignmentLayer, seenBendMap, ldp);
        }
      }

      //add alignment constraints
      Arrays.sort(nodes, new AlignedNodePositionComparator(ldp, node2AlignmentLayer));
      last = null;
      for (int i = 0; i < nodes.length; i++) {
        Node n = nodes[i];
        if (!isGroupNodeBorder(n, ldp) && !isGroupNodeProxy(n, ldp)) {
          if (last != null && node2AlignmentLayer.getDouble(last) == node2AlignmentLayer.getDouble(n)) {
            node2AlignWith.set(n, last); //node n should be aligned with last
          }
          last = n;
        }
      }
    }

    private static boolean hasLeftConstraint(EdgeData edgeData, boolean source) {
      final PortConstraint pc = source ? edgeData.getSPC() : edgeData.getTPC();
      return pc != null && pc.isAtWest();
    }

    private static boolean hasRightConstraint(EdgeData edgeData, boolean source) {
      final PortConstraint pc = source ? edgeData.getSPC() : edgeData.getTPC();
      return pc != null && pc.isAtEast();
    }

    private static int calcMinLength(Node n1, Node n2, LayoutGraph graph, LayoutDataProvider ldp) {
      DataProvider node2Parent = graph.getDataProvider(GroupingKeys.PARENT_NODE_ID_DPKEY);
      if (isGroupNodeBorder(n1, ldp) && isGroupNodeBorder(n2, ldp)) {
        Node n1GroupNode = ldp.getNodeData(n1).getGroupNode();
        Node n2GroupNode = ldp.getNodeData(n2).getGroupNode();
        if (n1GroupNode != n2GroupNode && node2Parent.get(n1) != n2GroupNode && node2Parent.get(n2) != n1GroupNode) {
          return 1;
        } else {
          return 0;
        }
      } else if (isGroupNodeBorder(n1, ldp)) {
        Node n1GroupNode = ldp.getNodeData(n1).getGroupNode();
        Node n2GroupNode = isGroupNodeProxy(n2, ldp) ? ldp.getNodeData(n2).getGroupNode() : (Node) node2Parent.get(n2);
        if (n2GroupNode == n1GroupNode) {
          return 0;
        } else {
          return 1;
        }
      } else if (isGroupNodeBorder(n2, ldp)) {
        Node n1GroupNode = isGroupNodeProxy(n1, ldp) ? ldp.getNodeData(n1).getGroupNode() : (Node) node2Parent.get(n1);
        Node n2GroupNode = ldp.getNodeData(n2).getGroupNode();
        if (n1GroupNode == n2GroupNode) {
          return 0;
        } else {
          return 1;
        }
      } else {
        return 1;
      }
    }

    private static void adjustAlignmentLayer(Node dummy, NodeMap node2AlignmentLayer, DataAcceptor seenBendMap,
                                             LayoutDataProvider ldp) {
      final double dummyAlignmentLayer = node2AlignmentLayer.getDouble(dummy);
      NodeList seenDummyNodes = new NodeList(dummy);
      boolean alignsWithCommonNode = false;

      Edge inEdge = dummy.firstInEdge();
      while (inEdge != null && isBendNode(inEdge.source(), ldp)
          && dummyAlignmentLayer == node2AlignmentLayer.getDouble(inEdge.source())) {
        seenDummyNodes.add(inEdge.source());
        inEdge = inEdge.source().firstInEdge();
      }
      if (inEdge != null && !isBendNode(inEdge.source(), ldp)) {
        alignsWithCommonNode = dummyAlignmentLayer == node2AlignmentLayer.getDouble(inEdge.source());
      }

      Edge outEdge = dummy.firstOutEdge();
      while (outEdge != null && isBendNode(outEdge.target(), ldp)
          && dummyAlignmentLayer == node2AlignmentLayer.getDouble(outEdge.target())) {
        seenDummyNodes.add(outEdge.target());
        outEdge = outEdge.target().firstOutEdge();
      }
      if (!alignsWithCommonNode && outEdge != null && !isBendNode(outEdge.target(), ldp)) {
        alignsWithCommonNode = dummyAlignmentLayer == node2AlignmentLayer.getDouble(outEdge.target());
      }

      for (NodeCursor nc = seenDummyNodes.nodes(); nc.ok(); nc.next()) {
        seenBendMap.setBool(nc.node(), true);
        if (!alignsWithCommonNode) {
          node2AlignmentLayer.setDouble(nc.node(), dummyAlignmentLayer - 0.5); //assign dummy nodes to a separate layer
        }
      }
    }

    private NodeMap createLaneAlignmentMap(LayoutGraph graph, LayoutDataProvider ldp) {
      NodeMap node2LaneAlignment = Maps.createHashedNodeMap();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        node2LaneAlignment.setInt(n, getLaneAlignment(n, ldp));
      }
      return node2LaneAlignment;
    }

    private byte getLaneAlignment(Node n, LayoutDataProvider ldp) {
      int toLeftCount = 0;
      int toRightCount = 0;
      EdgeList nEdges = new EdgeList(n.edges());
      nEdges.splice(FlowchartPortOptimizer.getSameLayerEdges(n, true, ldp));
      nEdges.splice(FlowchartPortOptimizer.getSameLayerEdges(n, false, ldp));
      for (EdgeCursor ec = nEdges.edges(); ec.ok(); ec.next()) {
        Edge e = ec.edge();
        if (FlowchartPortOptimizer.isToLeftPartition(n, e.opposite(n), ldp)) {
          toLeftCount++;
        } else if (FlowchartPortOptimizer.isToRightPartition(n, e.opposite(n), ldp)) {
          toRightCount++;
        }
      }
      if (toLeftCount > toRightCount) {
        return FlowchartPortOptimizer.LANE_ALIGNMENT_LEFT;
      } else if (toLeftCount < toRightCount) {
        return FlowchartPortOptimizer.LANE_ALIGNMENT_RIGHT;
      } else if (isHorizontalOrientation()) {
        return FlowchartPortOptimizer.LANE_ALIGNMENT_RIGHT;
      } else {
        return FlowchartPortOptimizer.LANE_ALIGNMENT_LEFT;
      }
    }

    private static boolean checkPredicate(YCursor cursor, DataProvider predicate) {
      for (cursor.toFirst(); cursor.ok(); cursor.next()) {
        if (predicate.getBool(cursor.current())) {
          return true;
        }
      }
      return false;
    }

    private static boolean areInSameLayer(Node n1, Node n2, LayoutDataProvider ldp) {
      return ldp.getNodeData(n1).getLayer() == ldp.getNodeData(n2).getLayer();
    }

    private static boolean isBendNode(Node n, LayoutDataProvider ldp) {
      final NodeData data = ldp.getNodeData(n);
      return data != null && (data.getType() == NodeData.TYPE_BEND);
    }

    private static boolean isGroupNodeBorder(Node n, LayoutDataProvider ldp) {
      final NodeData data = ldp.getNodeData(n);
      return data != null && (data.getType() == NodeData.TYPE_GROUP_BEGIN || data.getType() == NodeData.TYPE_GROUP_END);
    }

    private static boolean isGroupNodeProxy(Node n, LayoutDataProvider ldp) {
      final NodeData data = ldp.getNodeData(n);
      return data != null && (data.getType() == NodeData.TYPE_PROXY_FOR_EDGE_AT_GROUP);
    }

    /**
     *
     */
    static class AlignedNodePositionComparator implements Comparator {
      private final LayoutDataProvider ldp;
      private final DataProvider node2AlignmentLayer;

      AlignedNodePositionComparator(LayoutDataProvider ldp, DataProvider node2AlignmentLayer) {
        this.ldp = ldp;
        this.node2AlignmentLayer = node2AlignmentLayer;
      }

      public int compare(Object o1, Object o2) {
        final double al1 = node2AlignmentLayer.getDouble(o1);
        final double al2 = node2AlignmentLayer.getDouble(o2);
        if (al1 < al2) {
          return -1;
        } else if (al1 > al2) {
          return 1;
        } else {
          return Comparators.compare(ldp.getNodeData((Node) o1).getLayer(), ldp.getNodeData((Node) o2).getLayer());
        }
      }
    }

    /**
     *
     */
    static class NodePositionComparator implements Comparator {
      private final LayoutDataProvider ldp;

      NodePositionComparator(LayoutDataProvider ldp) {
        this.ldp = ldp;
      }

      public int compare(Object o1, Object o2) {
        final NodeData nd1 = ldp.getNodeData((Node) o1);
        final NodeData nd2 = ldp.getNodeData((Node) o2);
        final int l1 = nd1.getLayer();
        final int l2 = nd2.getLayer();
        if (l1 < l2) {
          return -1;
        } else if (l1 > l2) {
          return 1;
        } else {
          return Comparators.compare(nd1.getPosition(), nd2.getPosition());
        }
      }
    }

  }
}
