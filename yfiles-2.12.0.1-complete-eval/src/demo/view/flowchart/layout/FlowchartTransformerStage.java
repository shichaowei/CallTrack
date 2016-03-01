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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.GraphInterface;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YList;
import y.geom.YPoint;
import y.layout.AbstractLayoutStage;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.LayoutStage;
import y.layout.Layouter;
import y.layout.NodeLayout;
import y.layout.PortCandidate;
import y.layout.PortCandidateSet;
import y.layout.PortConstraintKeys;
import y.layout.RemoveColinearBendsStage;
import y.layout.grouping.Grouping;
import y.layout.grouping.GroupingKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.util.Comparators;
import y.util.DataProviderAdapter;
import y.util.GraphHider;
import y.util.Maps;
import y.util.WrappedObjectDataProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Transforms the graph for the flowchart layout algorithm and creates related port candidates and edge groupings.
 * <p/>
 * This class expects to find an IncrementalHierarchicLayouter in its core layouters. It does its transformation,
 * invokes the core layout and finally restores the original graph.
 */
class FlowchartTransformerStage extends AbstractLayoutStage {
  static final Object LAYER_ID_DP_KEY = "FlowchartTransformerStage.LAYER_ID_DP_KEY";
  static final Object GROUPING_NODES_DP_KEY = "FlowchartTransformerStage.GROUPING_NODES_DP_KEY";

  static final int NODE_TYPE_PRECEDING_LAYER = 1;
  static final int NODE_TYPE_SUCCEEDING_LAYER = 2;

  private static final double DUMMY_NODE_SIZE = 2.0;

  private byte layoutOrientation;

  private EdgeMap sourceGroupIds;
  private EdgeMap targetGroupIds;
  private EdgeMap sourceCandidates;
  private EdgeMap targetCandidates;
  private NodeMap groupingDummiesMap;
  private NodeMap dummyLayerIds;
  private HashedDataProviderWrapper groupNodeIdWrapper;
  private final PortCandidate northCandidate;
  private final PortCandidate eastCandidate;
  private final PortCandidate southCandidate;
  private final PortCandidate westCandidate;

  /**
   * Creates a new FlowchartTransformerStage.
   */
  FlowchartTransformerStage() {
    northCandidate = PortCandidate.createCandidate(PortCandidate.NORTH, 0.0);
    eastCandidate = PortCandidate.createCandidate(PortCandidate.EAST, 0.0);
    southCandidate = PortCandidate.createCandidate(PortCandidate.SOUTH, 0.0);
    westCandidate = PortCandidate.createCandidate(PortCandidate.WEST, 0.0);
  }

  /**
   * Returns {@link #canLayoutCore(y.layout.LayoutGraph)}.
   *
   * @param graph the graph.
   * @return {@link #canLayoutCore(y.layout.LayoutGraph)}.
   */
  public boolean canLayout(LayoutGraph graph) {
    return canLayoutCore(graph);
  }

  /**
   * Runs this layout algorithm on the given graph.
   *
   * @param graph the graph.
   */
  public void doLayout(LayoutGraph graph) {
    final IncrementalHierarchicLayouter ihl = getIHLCoreLayouter(this);
    if (ihl == null) {
      return;
    }

    layoutOrientation = ihl.getLayoutOrientation();

    if (Grouping.isGrouped(graph)) {
      groupNodeIdWrapper = new HashedDataProviderWrapper(new HashMap(),
          graph.getDataProvider(GroupingKeys.NODE_ID_DPKEY));
      graph.addDataProvider(GroupingKeys.NODE_ID_DPKEY, groupNodeIdWrapper);
    }

    // Backup all data provide this class may overwrite
    final DataProvider backupNodeIdDP = graph.getDataProvider(Layouter.NODE_ID_DPKEY);
    final DataProvider backupNodePcDP = graph.getDataProvider(PortCandidateSet.NODE_DP_KEY);
    final DataProvider backupSourcePcDP = graph.getDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY);
    final DataProvider backupTargetPcDP = graph.getDataProvider(PortCandidate.TARGET_PCLIST_DPKEY);
    final DataProvider backupSourceGroupDP = graph.getDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
    final DataProvider backupTargetGroupDP = graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
    final DataProvider backupSourceConstraintsDP = graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
    final DataProvider backupTargetConstraintsDP = graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
    graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
    graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);

    final DataProviderAdapter provider = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return dataHolder;
      }
    };
    graph.addDataProvider(Layouter.NODE_ID_DPKEY, backupNodeIdDP != null ?
        (DataProvider) new WrappedObjectDataProvider(backupNodeIdDP, provider) : provider);

    try {
      // Don't register the new data providers before the configuration is done
      // since the old data might be needed
      sourceCandidates = Maps.createHashedEdgeMap();
      targetCandidates = Maps.createHashedEdgeMap();

      configurePreferredEdgeDirections(graph);

      if (graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY) != null) {
        dummyLayerIds = Maps.createHashedNodeMap();
        groupingDummiesMap = Maps.createHashedNodeMap();
        sourceGroupIds = Maps.createHashedEdgeMap();
        targetGroupIds = Maps.createHashedEdgeMap();

        configureInEdgeGrouping(graph);

        graph.addDataProvider(GROUPING_NODES_DP_KEY, groupingDummiesMap);
        graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sourceGroupIds);
        graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, targetGroupIds);
      }

      graph.removeDataProvider(PortCandidateSet.NODE_DP_KEY);
      graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, sourceCandidates);
      graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, targetCandidates);

      doLayoutCore(graph);

    } finally {
      dummyLayerIds = null;
      groupingDummiesMap = null;
      sourceCandidates = null;
      targetCandidates = null;
      sourceGroupIds = null;
      targetGroupIds = null;

      if (groupNodeIdWrapper != null) {
        FlowchartLayouter.restoreDataProvider(
            graph, groupNodeIdWrapper.getFallback(), GroupingKeys.NODE_ID_DPKEY);
        groupNodeIdWrapper = null;
      }

      FlowchartLayouter.restoreDataProvider(graph, backupSourceConstraintsDP,
          PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      FlowchartLayouter.restoreDataProvider(graph, backupTargetConstraintsDP,
          PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      FlowchartLayouter.restoreDataProvider(graph, backupNodePcDP, PortCandidateSet.NODE_DP_KEY);
      FlowchartLayouter.restoreDataProvider(graph, backupSourcePcDP, PortCandidate.SOURCE_PCLIST_DPKEY);
      FlowchartLayouter.restoreDataProvider(graph, backupTargetPcDP, PortCandidate.TARGET_PCLIST_DPKEY);
      FlowchartLayouter.restoreDataProvider(graph, backupSourceGroupDP, PortConstraintKeys.SOURCE_GROUPID_KEY);
      FlowchartLayouter.restoreDataProvider(graph, backupTargetGroupDP, PortConstraintKeys.TARGET_GROUPID_KEY);
      FlowchartLayouter.restoreDataProvider(graph, backupNodeIdDP, Layouter.NODE_ID_DPKEY);

      restoreOriginalGraph(graph);
      removeCollinearBends(graph);
    }
  }

  /**
   * Configures the in-edge grouping.
   *
   * @see InEdgeGroupingConfigurator
   */
  private void configureInEdgeGrouping(LayoutGraph graph) {
    final boolean hasLayerIds = graph.getDataProvider(LAYER_ID_DP_KEY) != null;

    final InEdgeGroupingConfigurator precedingGroupingConfigurator = new InEdgeGroupingConfigurator();
    final SucceedingLayersInEdgeGroupingConfigurator succeedingGroupingConfigurator =
        new SucceedingLayersInEdgeGroupingConfigurator();

    final EdgeList edgesToReverse = new EdgeList();
    final EdgeList[] groupingLists = getGroupingLists(graph);

    for (int i = 0; i < groupingLists.length; i++) {
      final EdgeList groupingList = groupingLists[i];

      if (groupingList == null || groupingList.isEmpty()) {
        continue;
      }

      if (hasLayerIds) {
        final EdgeList[][] layers = getInEdgesByLayer(graph, groupingList);
        precedingGroupingConfigurator.doGrouping(layers[0], graph);
        succeedingGroupingConfigurator.doGrouping(layers[1], graph, edgesToReverse);
      } else {
        final Node target = groupingList.firstEdge().target();
        for (EdgeCursor ec = groupingList.edges(); ec.ok(); ec.next()) {
          targetGroupIds.set(ec.edge(), target);
        }
      }
    }

    for (EdgeCursor ec = edgesToReverse.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      graph.reverseEdge(edge);

      // Reverse the port candidate data if an original edge was reversed
      if (groupingDummiesMap.getInt(edge.source()) == 0 || groupingDummiesMap.getInt(edge.target()) == 0) {
        final Object spc = sourceCandidates.get(edge);
        sourceCandidates.set(edge, targetCandidates.get(edge));
        targetCandidates.set(edge, spc);
      }
    }
  }

  /**
   * Creates the configuration for the preferred edge directions. This method creates source port candidates according
   * to the directions defined by the data provider for the key {@link FlowchartLayouter#PREFERRED_DIRECTION_KEY}.
   */
  void configurePreferredEdgeDirections(final LayoutGraph graph) {
    final DataProvider directions = graph.getDataProvider(FlowchartLayouter.PREFERRED_DIRECTION_KEY);

    if (directions == null) {
      return;
    }

    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      final Node node = nodeCursor.node();

      int leftCount = 0;
      int rightCount = 0;
      for (Edge edge = node.firstOutEdge(); edge != null; edge = edge.nextOutEdge()) {
        final int dir = directions.getInt(edge);
        if (dir == FlowchartLayouter.DIRECTION_LEFT_IN_FLOW) {
          leftCount++;
        } else if (dir == FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW) {
          rightCount++;
        }
        sourceCandidates.set(edge, getPortCandidateCollection(dir));
      }

      if (leftCount <= 1 && rightCount <= 1) {
        continue;
      }

      // If there is more than one edge to the left or right side,
      // set less restrictive candidates to allow nicer images.
      for (Edge edge = node.firstOutEdge(); edge != null; edge = edge.nextOutEdge()) {
        final int dir = directions.getInt(edge);
        if (dir == FlowchartLayouter.DIRECTION_LEFT_IN_FLOW || dir == FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW) {
          sourceCandidates.set(edge, getPortCandidateCollection(FlowchartLayouter.DIRECTION_FLATWISE));
        }
      }
    }
  }

  /**
   * Returns an array of edge lists, each of which contains all edges with the same group Id and the same target node.
   */
  private static EdgeList[] getGroupingLists(LayoutGraph graph) {
    final DataProvider groupIdDP = graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);

    // Partition edges according to group Id
    final Map idToListsMap = new HashMap();
    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
      final Edge edge = edgeCursor.edge();
      final Object id = groupIdDP.get(edge);

      if (id != null) {
        if (idToListsMap.containsKey(id)) {
          ((Collection) idToListsMap.get(id)).add(edge);
        } else {
          final EdgeList list = new EdgeList(edge);
          idToListsMap.put(id, list);
        }
      }
    }

    // Divide the group Id partitions according to edge target nodes
    final Collection targetGroupLists = new ArrayList();
    for (Iterator iterator = idToListsMap.values().iterator(); iterator.hasNext(); ) {
      final EdgeList groupList = (EdgeList) iterator.next();

      // Sort the edges according to target nodes such that edges with the same target have consecutive indices
      groupList.sort(new Comparator() {
        public int compare(Object o1, Object o2) {
          return Comparators.compare(((Edge) o1).target().index(), ((Edge) o2).target().index());
        }
      });

      // Add edges to lists and start a new list whenever a new target is found
      EdgeList targetGroupList = null;
      for (EdgeCursor edgeCursor = groupList.edges(); edgeCursor.ok(); edgeCursor.next()) {
        final Edge edge = edgeCursor.edge();

        if (targetGroupList == null || !edge.target().equals(targetGroupList.firstEdge().target())) {
          targetGroupList = new EdgeList();
          targetGroupLists.add(targetGroupList);
        }

        targetGroupList.add(edge);
      }
    }

    return (EdgeList[]) targetGroupLists.toArray(new EdgeList[targetGroupLists.size()]);
  }

  /**
   * Returns the in-edges of the given node grouped by layer.
   *
   * @return the in-edges of the given node grouped by layer. the first array contains edges from preceding layers, the
   *         second array edges from succeeding layers.
   */
  private EdgeList[][] getInEdgesByLayer(LayoutGraph graph, final EdgeList groupedInEdges) {
    final boolean hasLayerIds = graph.getDataProvider(LAYER_ID_DP_KEY) != null;

    final Comparator layerComparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        final Node n1 = ((Edge) o1).source();
        final Node n2 = ((Edge) o2).source();
        return hasLayerIds ? Comparators.compare(getLayerId(n1), getLayerId(n2)) : 0;
      }
    };

    groupedInEdges.sort(layerComparator);

    final int referenceLayer = getLayerId(groupedInEdges.firstEdge().target());
    final List precedingLayers = new ArrayList();
    final List succeedingLayers = new ArrayList();

    int previousLayer = -1;
    for (EdgeCursor ec = groupedInEdges.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final int layer = getLayerId(edge.source());
      final List layers = layer <= referenceLayer ? precedingLayers : succeedingLayers;
      if (layer != previousLayer) {
        layers.add(new EdgeList());
        previousLayer = layer;
      }
      ((Collection) layers.get(layers.size() - 1)).add(edge);
    }

    Collections.reverse(succeedingLayers);
    final EdgeList[][] separatedLayers = new EdgeList[2][];
    separatedLayers[0] = (EdgeList[]) precedingLayers.toArray(new EdgeList[precedingLayers.size()]);
    separatedLayers[1] = (EdgeList[]) succeedingLayers.toArray(new EdgeList[succeedingLayers.size()]);

    return separatedLayers;
  }

  /**
   * Restores the original graph by changing all edges to their original nodes and removing all dummy nodes.
   */
  private static void restoreOriginalGraph(LayoutGraph graph) {
    final DataProvider groupingDummiesDP = graph.getDataProvider(GROUPING_NODES_DP_KEY);

    if (groupingDummiesDP == null) {
      return;
    }

    graph.removeDataProvider(GROUPING_NODES_DP_KEY);

    for (NodeCursor nc = new NodeList(graph.nodes()).nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();

      final int groupingDummyId = groupingDummiesDP.getInt(node);

      if (groupingDummyId == NODE_TYPE_PRECEDING_LAYER) {
        final Edge outEdge = node.firstOutEdge();
        final YList outPath = graph.getPathList(outEdge);
        outPath.set(0, graph.getCenter(node));

        for (EdgeCursor ec = new EdgeList(node.inEdges()).edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          final YList inPath = graph.getPathList(edge);
          inPath.popLast();

          graph.changeEdge(edge, edge.source(), outEdge.target());
          graph.setPath(edge, createCombinedList(inPath, outPath));
        }

        graph.removeNode(node);
      } else if (groupingDummyId == NODE_TYPE_SUCCEEDING_LAYER) {
        final Edge inEdge = node.firstInEdge();
        final boolean inEdgeFromOriginal = groupingDummiesDP.getInt(inEdge.source()) == 0;
        final YList inPath = graph.getPathList(inEdge);
        inPath.set(inPath.size() - 1, graph.getCenter(node));

        for (EdgeCursor ec = new EdgeList(node.outEdges()).edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          final boolean outEdgeFromOriginal = groupingDummiesDP.getInt(edge.target()) == 0;
          final YList outPath = graph.getPathList(edge);
          outPath.pop();

          graph.changeEdge(edge, inEdge.source(), edge.target());
          final YList combinedPath = createCombinedList(inPath, outPath);

          if (inEdgeFromOriginal && outEdgeFromOriginal) {
            // change the edge to its original targets -> reverse the edge direction
            graph.reverseEdge(edge);
            combinedPath.reverse();
          }

          makeOrthogonal(combinedPath);
          graph.setPath(edge, combinedPath);
        }

        graph.removeNode(node);
      }
    }
  }

  /**
   * Fixes the orthogonality first and last segment of the edge path.
   */
  private static void makeOrthogonal(final YList combinedPath) {
    if (combinedPath.size() < 2) {
      return;
    }

    final ListCell firstCell = combinedPath.firstCell();
    final YPoint p1 = (YPoint) firstCell.getInfo();
    final YPoint p2 = (YPoint) firstCell.succ().getInfo();
    if (!isOrthogonal(p1, p2)) {
      final YPoint p3 = makeOrthogonal(p2, p1);
      combinedPath.insertAfter(p3, firstCell);
    }

    final ListCell lastCell = combinedPath.lastCell();
    final YPoint q1 = (YPoint) lastCell.pred().getInfo();
    final YPoint q2 = (YPoint) lastCell.getInfo();
    if (!isOrthogonal(q1, q2)) {
      final YPoint q3 = makeOrthogonal(q1, q2);
      combinedPath.insertBefore(q3, lastCell);
    }
  }

  private static YPoint makeOrthogonal(final YPoint p1, final YPoint p2) {
    return Math.abs(p1.getX() - p2.getX()) < Math.abs(p1.getY() - p2.getY()) ?
        new YPoint(p2.getX(), p1.getY()) :
        new YPoint(p1.getX(), p2.getY());
  }

  private static boolean isOrthogonal(final YPoint p1, final YPoint p2) {
    return Math.abs(p1.getX() - p2.getX()) < 0.01 || Math.abs(p1.getY() - p2.getY()) < 0.01;
  }

  /**
   * Removes all collinear bends.
   */
  private static void removeCollinearBends(final LayoutGraph graph) {
    // do not remove bends of self-loops
    GraphHider selfLoopHider = new GraphHider(graph);
    selfLoopHider.hideSelfLoops();

    final RemoveColinearBendsStage collinearBendsStage = new RemoveColinearBendsStage();
    collinearBendsStage.setRemoveStraightOnly(false);
    collinearBendsStage.doLayout(graph);

    selfLoopHider.unhideAll();
  }

  /**
   * Returns the layer Id for the given node, either from the registered data provider or from the internal dummy node
   * layer Id map.
   */
  private int getLayerId(Node node) {
    return groupingDummiesMap.getInt(node) != 0 ?
        dummyLayerIds.getInt(node) :
        node.getGraph().getDataProvider(LAYER_ID_DP_KEY).getInt(node);
  }

  /**
   * Returns whether or not the given node is a grouping dummy created by this class.
   */
  static boolean isGroupingDummy(GraphInterface graph, Node node) {
    final DataProvider provider = graph.getDataProvider(GROUPING_NODES_DP_KEY);
    return provider != null && provider.getInt(node) > 0;
  }

  /**
   * Returns a collection of port candidate for the given direction.
   *
   * @param direction one of hte direction constants in {@FlowchartLayouter}.
   * @return a collection of port candidate for the given direction.
   */
  private Collection getPortCandidateCollection(final int direction) {
    final Collection collection = new ArrayList(4);

    if ((direction & FlowchartLayouter.DIRECTION_WITH_THE_FLOW) != 0) {
      collection.add(getPortCandidateSingleton(PortCandidate.WITH_THE_FLOW));
    }
    if ((direction & FlowchartLayouter.DIRECTION_AGAINST_THE_FLOW) != 0) {
      collection.add(getPortCandidateSingleton(PortCandidate.AGAINST_THE_FLOW));
    }
    if ((direction & FlowchartLayouter.DIRECTION_LEFT_IN_FLOW) != 0) {
      collection.add(getPortCandidateSingleton(PortCandidate.LEFT_IN_FLOW));
    }
    if ((direction & FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW) != 0) {
      collection.add(getPortCandidateSingleton(PortCandidate.RIGHT_IN_FLOW));
    }
    return collection;
  }

  private PortCandidate getPortCandidateSingleton(final int direction) {
    switch (getDirectionForLayoutOrientation(layoutOrientation, direction)) {
      case PortCandidate.NORTH:
        return northCandidate;
      case PortCandidate.SOUTH:
        return southCandidate;
      case PortCandidate.EAST:
        return eastCandidate;
      case PortCandidate.WEST:
        return westCandidate;
      default:
        return null;
    }
  }

  /**
   * Returns the absolute port candidate direction for the given direction with respect to the layout orientation of
   * this layout stage.
   */
  static int getDirectionForLayoutOrientation(final byte layoutOrientation, final int direction) {
    if ((int) layoutOrientation == (int) LayoutOrientation.TOP_TO_BOTTOM) {
      switch (direction) {
        case PortCandidate.AGAINST_THE_FLOW:
          return PortCandidate.NORTH;
        case PortCandidate.WITH_THE_FLOW:
          return PortCandidate.SOUTH;
        case PortCandidate.LEFT_IN_FLOW:
          return PortCandidate.EAST;
        case PortCandidate.RIGHT_IN_FLOW:
          return PortCandidate.WEST;
        default:
          return -1;
      }
    } else {
      switch (direction) {
        case PortCandidate.AGAINST_THE_FLOW:
          return PortCandidate.WEST;
        case PortCandidate.WITH_THE_FLOW:
          return PortCandidate.EAST;
        case PortCandidate.LEFT_IN_FLOW:
          return PortCandidate.NORTH;
        case PortCandidate.RIGHT_IN_FLOW:
          return PortCandidate.SOUTH;
        default:
          return -1;
      }
    }
  }

  /**
   * Returns the IHL that is set as core layouter of the given layout stage or <code>null</code> if none is set.
   */
  private static IncrementalHierarchicLayouter getIHLCoreLayouter(final LayoutStage stage) {
    final Layouter coreLayouter = stage.getCoreLayouter();
    if (coreLayouter instanceof IncrementalHierarchicLayouter) {
      return (IncrementalHierarchicLayouter) coreLayouter;
    } else if (coreLayouter instanceof LayoutStage) {
      return getIHLCoreLayouter((LayoutStage) coreLayouter);
    } else {
      return null;
    }
  }

  /**
   * Returns the last element of the given array.
   */
  static EdgeList getLast(final EdgeList[] edgeLists) {
    return edgeLists[edgeLists.length - 1];
  }

  /**
   * Returns a new list that contains the elements of <code>c1.addAll(c2)</code>.
   */
  static YList createCombinedList(Collection c1, Collection c2) {
    final YList yList = new YList(c1);
    yList.addAll(c2);
    return yList;
  }

  /**
   * Creates the grouping dummy structure.
   */
  class InEdgeGroupingConfigurator {

    /**
     * Creates the complete grouping dummy structure.
     *
     * @see #createBus(y.layout.LayoutGraph, y.base.EdgeList[])
     * @see #createGrouping(y.base.EdgeList, y.base.Node, y.layout.LayoutGraph)
     */
    public void doGrouping(EdgeList[] layers, LayoutGraph graph) {
      if (layers.length > 0) {
        final Node neighborLayerNode = getLast(layers).firstEdge().source();
        final EdgeList nonBusEdges = createBus(graph, layers);

        if (nonBusEdges.size() == 1) {
          handleSingleEdgeGrouping(nonBusEdges.firstEdge(), graph);
        } else if (nonBusEdges.size() > 1) {
          createGrouping(nonBusEdges, neighborLayerNode, graph);
        }
      }
    }

    /**
     * Returns the grouping type of this class.
     *
     * @return {@link #NODE_TYPE_PRECEDING_LAYER}.
     */
    int getGroupingType() {
      return NODE_TYPE_PRECEDING_LAYER;
    }

    /**
     * Changes the given edge to the given nodes, and allows subclasses to reverses its direction if required.
     */
    void changeEdge(LayoutGraph graph, Edge edge, Node source, Node target) {
      graph.changeEdge(edge, source, target);
    }

    /**
     * Sets the grouping Id of the given edge to the appropriate grouping Id data acceptor. By default, this are target
     * group Ids.
     */
    void setGroupId(Edge edge, Object id) {
      targetGroupIds.set(edge, id);
    }

    /**
     * Creates a port candidates for an edge connecting two bus dummy nodes.
     */
    void createBusPortCandidate(Edge edge, LayoutGraph graph) {
      sourceCandidates.set(edge, createStrongPortCandidate(edge, true, PortCandidate.WITH_THE_FLOW, graph));
    }

    /**
     * Creates a bus structure to group incoming edges of a single node <code>t</code>. These edges have to come from
     * different layers which are all either in preceding layers or succeeding layers.
     * <p/>
     * The bus is created iteratively from the most distant to the nearest layer as long as there is at most one
     * neighbor in the layer. For edges from preceding layers, in each layer except the most distant one, the bus
     * consists of a new dummy node <code>d</code>, the original edge which is changed from <code>(v, t)</code> to
     * <code>(v, d)</code>, one incoming edge from the previous more distant layer and a new dummy edge to the next less
     * layer or <code>t</code>. For succeeding layers, the direction of the dummy edges is reversed, that is, the edge
     * direction is always from lower layer index to higher layer index.
     *
     * @param graph  the graph.
     * @param layers all relevant edges grouped by source layer and sorted from distant layer to near.
     */
    EdgeList createBus(LayoutGraph graph, EdgeList[] layers) {
      final Node target = layers[0].firstEdge().target();
      final EdgeList nonSingletonLayerEdges = new EdgeList();

      EdgeList unfinishedEdges = new EdgeList();
      for (int i = 0; i < layers.length; i++) {
        final EdgeList layer = layers[i];

        // maybe we should also check if a singleton node is connected to too many such buses
        if (nonSingletonLayerEdges.isEmpty() && layer.size() == 1) {
          final Edge edge = layer.firstEdge();

          if (unfinishedEdges.isEmpty()) {
            unfinishedEdges.add(edge);
          } else {
            final Node layerDummy = createDummyNode(graph, getGroupingType(), getLayerId(edge.source()));

            // Change unfinished edges to the dummy node
            for (EdgeCursor ec = unfinishedEdges.edges(); ec.ok(); ec.next()) {
              final Edge e = ec.edge();
              changeEdge(graph, e, e.source(), layerDummy);
              if (unfinishedEdges.size() > 1) {
                setGroupId(e, layerDummy);
              }

            }
            unfinishedEdges.clear();

            // Create a new edge from the dummy to the target
            final Edge e = graph.createEdge(layerDummy, target);
            unfinishedEdges.add(e);
            createBusPortCandidate(e, graph);

            // Handle this layer's edge
            if (FlowchartLayouter.isStraightBranch(graph, edge)) {
              unfinishedEdges.add(edge);
            } else {
              changeEdge(graph, edge, edge.source(), layerDummy);
            }
          }

        } else {
          nonSingletonLayerEdges.addAll(layer);
        }
      }

      if (!unfinishedEdges.isEmpty()) {
        nonSingletonLayerEdges.addAll(unfinishedEdges);
      }

      return nonSingletonLayerEdges;
    }

    /**
     * Handles the grouping of only one edge.
     */
    void handleSingleEdgeGrouping(Edge edge, LayoutGraph graph) {
      targetCandidates.set(edge, createStrongPortCandidate(edge, false, PortCandidate.AGAINST_THE_FLOW, graph));
    }

    /**
     * Creates an edge grouping for the given <code>nonBusEdges</code>. Since grouping works best if the sources of all
     * nonBusEdges are in the neighboring layer, this method splits edges from more distant layers by adding dummy nodes
     * in the neighboring layer.
     */
    void createGrouping(EdgeList nonBusEdges, Node neighborLayerNode, LayoutGraph graph) {
      final DataProvider nodeIds = graph.getDataProvider(Layouter.NODE_ID_DPKEY);
      final Object groupId = nodeIds.get(nonBusEdges.firstEdge().target());

      for (EdgeCursor ec = nonBusEdges.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        setGroupId(edge, groupId);
        targetCandidates.set(edge, createStrongPortCandidate(edge, false, PortCandidate.AGAINST_THE_FLOW, graph));
      }
    }

    /**
     * Creates a dummy node, sets its layer Id and registers it in the dummy marker map.
     */
    Node createDummyNode(final LayoutGraph graph, int groupingType, int layerId) {
      final Node dummyNode = graph.createNode();
      groupingDummiesMap.setInt(dummyNode, groupingType);
      dummyLayerIds.setInt(dummyNode, layerId);

      if (groupNodeIdWrapper != null) {
        groupNodeIdWrapper.getMap().put(dummyNode, dummyNode);
      }

      graph.setSize(dummyNode, DUMMY_NODE_SIZE, DUMMY_NODE_SIZE);

      return dummyNode;
    }

    /**
     * Creates a singleton collection containing one port candidate for the specified end node of the given edge.
     */
    Collection createStrongPortCandidate(final Edge edge, final boolean source, final int dir, LayoutGraph graph) {
      NodeLayout nl = graph.getLayout(source ? edge.source() : edge.target());
      final int direction = getDirectionForLayoutOrientation(layoutOrientation, dir);
      final YPoint p;
      switch (direction) {
        case PortCandidate.NORTH:
        default:
          p = new YPoint(0.0, -0.5 * nl.getHeight());
          break;
        case PortCandidate.SOUTH:
          p = new YPoint(0.0, 0.5 * nl.getHeight());
          break;
        case PortCandidate.EAST:
          p = new YPoint(0.5 * nl.getWidth(), 0.0);
          break;
        case PortCandidate.WEST:
          p = new YPoint(-0.5 * nl.getWidth(), 0.0);
          break;
      }

      return Collections.singleton(PortCandidate.createCandidate(p.getX(), p.getY(), direction));
    }
  }

  /**
   * An {@link InEdgeGroupingConfigurator} for edges to succeeding layers. Its main difference is the creation of a same
   * layer dummy node. Apart from that, this class has to set other port candidate directions and reverses some edges.
   */
  class SucceedingLayersInEdgeGroupingConfigurator extends InEdgeGroupingConfigurator {
    private EdgeList edgesToReverse;

    SucceedingLayersInEdgeGroupingConfigurator() {
    }

    /**
     * Creates the complete grouping dummy structure. This class stores all edges that must be reversed after the
     * creation of all dummy structures in the given list.
     * <p/>
     * Use this method instead of {@link #doGrouping(y.base.EdgeList[], y.layout.LayoutGraph)}.
     *
     * @see #createBus(y.layout.LayoutGraph, y.base.EdgeList[])
     * @see #createGrouping(y.base.EdgeList, y.base.Node, y.layout.LayoutGraph)
     */
    public void doGrouping(EdgeList[] layers, LayoutGraph graph, EdgeList edgesToReverse) {
      try {
        this.edgesToReverse = edgesToReverse;
        super.doGrouping(layers, graph);
      } finally {
        this.edgesToReverse = null;
      }
    }

    /**
     * This method must not be called directly since it omits the required list for edges to reverse.
     */
    public void doGrouping(EdgeList[] layers, LayoutGraph graph) {
      if (edgesToReverse == null) {
        throw new IllegalStateException("Collection of edges to reverse is not set.");
      }
      super.doGrouping(layers, graph);
    }

    /**
     * Returns the grouping type of this class.
     *
     * @return {@link #NODE_TYPE_SUCCEEDING_LAYER}.
     */
    int getGroupingType() {
      return NODE_TYPE_SUCCEEDING_LAYER;
    }

    /**
     * Changes the given edge to the given nodes and reverses its direction.
     */
    void changeEdge(LayoutGraph graph, Edge edge, Node source, Node target) {
      super.changeEdge(graph, edge, source, target);
      edgesToReverse.add(edge);
    }

    /**
     * Sets the grouping Id of the given edge to the appropriate grouping Id data acceptor. This are source group Ids.
     */
    void setGroupId(Edge edge, Object id) {
      sourceGroupIds.set(edge, id);
    }

    /**
     * Creates a port candidate for an edge connecting two bus dummy nodes.
     */
    void createBusPortCandidate(Edge edge, LayoutGraph graph) {
      targetCandidates.set(edge, createStrongPortCandidate(edge, true, PortCandidate.AGAINST_THE_FLOW, graph));
    }

    /**
     * Creates a strong North candidate and reverses the edge if it comes from a dummy.
     */
    void handleSingleEdgeGrouping(Edge edge, LayoutGraph graph) {
      if (groupingDummiesMap.getInt(edge.source()) > 0) {
        edgesToReverse.add(edge);
      }
      targetCandidates.set(edge, createStrongPortCandidate(edge, false, PortCandidate.AGAINST_THE_FLOW, graph));
    }

    /**
     * Creates an edge grouping for the given <code>nonBusEdges</code>. Since grouping works best if the sources of all
     * nonBusEdges are in the neighboring layer, this method splits edges from more distant layers by adding dummy nodes
     * in the neighboring layer.
     */
    void createGrouping(EdgeList nonBusEdges, Node neighborLayerNode, LayoutGraph graph) {
      prepareForGrouping(nonBusEdges, graph);

      final Node target = nonBusEdges.firstEdge().target();
      final Object groupId = graph.getDataProvider(Layouter.NODE_ID_DPKEY).get(target);
      final int neighborLayerIndex = getLayerId(neighborLayerNode);

      for (EdgeCursor ec = nonBusEdges.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();

        final Edge groupingEdge;
        if (neighborLayerIndex == getLayerId(edge.source())) {
          groupingEdge = edge;
        } else {
          final Node layerDummy = createDummyNode(graph, getGroupingType(), neighborLayerIndex);
          changeEdge(graph, edge, edge.source(), layerDummy);

          groupingEdge = graph.createEdge(layerDummy, target);
        }

        edgesToReverse.add(groupingEdge);
        setGroupId(groupingEdge, groupId);
        sourceCandidates.set(groupingEdge,
            createStrongPortCandidate(groupingEdge, false, PortCandidate.WITH_THE_FLOW, graph));
      }
    }

    /**
     * Creates a same layer dummy node for nicer grouping.
     */
    void prepareForGrouping(EdgeList nonBusEdges, LayoutGraph graph) {
      final Node originalTarget = nonBusEdges.firstEdge().target();
      final Node target = createDummyNode(graph, getGroupingType(), getLayerId(originalTarget));

      final Edge sameLayerEdge = graph.createEdge(originalTarget, target);
      sourceCandidates.set(sameLayerEdge,
          createStrongPortCandidate(sameLayerEdge, true, PortCandidate.AGAINST_THE_FLOW, graph));

      for (EdgeCursor ec = nonBusEdges.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        graph.changeEdge(edge, edge.source(), target);
      }
    }

  }

  /**
   * A two-stage data provider which returns the value of <code>map.get(key)</code> if the key is contained in the given
   * map, and <code>fallback.get(key)</code> otherwise.
   */
  static class HashedDataProviderWrapper implements DataProvider {
    private final Map map;
    private final DataProvider fallback;

    HashedDataProviderWrapper(Map map, DataProvider fallback) {
      this.map = map;
      this.fallback = fallback;
    }

    Map getMap() {
      return map;
    }

    public DataProvider getFallback() {
      return fallback;
    }

    public Object get(Object dataHolder) {
      return map.containsKey(dataHolder) ? map.get(dataHolder) : fallback.get(dataHolder);
    }

    public int getInt(Object dataHolder) {
      return map.containsKey(dataHolder) ?
          ((Number) map.get(dataHolder)).intValue() :
          fallback.getInt(dataHolder);
    }

    public double getDouble(Object dataHolder) {
      return map.containsKey(dataHolder) ?
          ((Number) map.get(dataHolder)).doubleValue() :
          fallback.getDouble(dataHolder);
    }

    public boolean getBool(Object dataHolder) {
      return map.containsKey(dataHolder) ?
          ((Boolean) map.get(dataHolder)).booleanValue() :
          fallback.getBool(dataHolder);
    }
  }
}
