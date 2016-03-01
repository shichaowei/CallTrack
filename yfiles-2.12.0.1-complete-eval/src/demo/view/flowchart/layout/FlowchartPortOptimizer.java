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
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.LayoutGraph;
import y.layout.PortCandidate;
import y.layout.PortConstraint;
import y.layout.hierarchic.incremental.AbstractPortConstraintOptimizer;
import y.layout.hierarchic.incremental.EdgeData;
import y.layout.hierarchic.incremental.ItemFactory;
import y.layout.hierarchic.incremental.Layers;
import y.layout.hierarchic.incremental.LayoutDataProvider;
import y.layout.hierarchic.incremental.NodeData;
import y.layout.hierarchic.incremental.PCListOptimizer;
import y.layout.hierarchic.incremental.SwimLaneDescriptor;
import y.util.Comparators;
import y.util.Maps;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @noinspection ImplicitNumericConversion, ObjectEquality
 */
class FlowchartPortOptimizer extends AbstractPortConstraintOptimizer {
  static final byte LANE_ALIGNMENT_LEFT = 0;
  static final byte LANE_ALIGNMENT_RIGHT = 1;

  static final int PRIORITY_LOW = 1;
  static final int PRIORITY_BASIC = 3;
  static final int PRIORITY_HIGH = 5000;

  static final String NODE_TO_ALIGN_DP_KEY = "y.layout.hierarchic.incremental.SimlexNodePlacer.NODE_TO_ALIGN_WITH";

  private final FlowchartAlignmentCalculator alignmentCalculator;
  private final PCListOptimizer pcListOptimizer;

  FlowchartPortOptimizer(byte layoutOrientation) {
    this.alignmentCalculator = new FlowchartAlignmentCalculator();
    this.pcListOptimizer = new PCListOptimizer();

    setLayoutOrientation(layoutOrientation);
  }

  public void setLayoutOrientation(final byte layoutOrientation) {
    super.setLayoutOrientation(layoutOrientation);

    alignmentCalculator.setLayoutOrientation(layoutOrientation);
    pcListOptimizer.setLayoutOrientation(layoutOrientation);
  }

  public void optimizeAfterLayering(LayoutGraph graph, Layers layers, LayoutDataProvider ldp, ItemFactory itemFactory) {
    pcListOptimizer.optimizeAfterLayering(graph, layers, ldp, itemFactory);
  }

  public void optimizeAfterSequencing(LayoutGraph graph, Layers layers, LayoutDataProvider ldp,
                                      ItemFactory itemFactory) {
    super.optimizeAfterSequencing(graph, layers, ldp, itemFactory);

    final EdgeMap edgePriority = Maps.createHashedEdgeMap();
    final NodeMap nodeAlignment = Maps.createHashedNodeMap();

    alignmentCalculator.determineAlignment(graph, ldp, nodeAlignment, edgePriority);

    optimizeForAlignment(graph, ldp, itemFactory, nodeAlignment, edgePriority);

    optimizeMessageNodes(graph, ldp, itemFactory);
  }

  protected void optimizeAfterSequencing(Node node, Comparator inEdgeOrder, Comparator outEdgeOrder, LayoutGraph graph,
                                         LayoutDataProvider ldp, ItemFactory itemFactory) {
    // set EAST or WEST temporary constraints for the same layer edges
    for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();

      if (isTemporarySameLayerEdge(edge, ldp)) {
        final byte preferredSide = getPreferredSideForTemporarySameLayerEdge(edge, ldp);
        itemFactory.setTemporaryPortConstraint(edge, node.equals(edge.source()), PortConstraint.create(preferredSide));
      }
    }

    // choose final temporary constraint for all non-assigned flatwise edges
    optimizeFlatwiseEdges(node, true, outEdgeOrder, ldp, itemFactory);
    optimizeFlatwiseEdges(node, false, inEdgeOrder, ldp, itemFactory);
  }

  /**
   * Chooses the final port constraint for all non-assigned flatwise edges.
   */
  private void optimizeFlatwiseEdges(final Node node, final boolean source, final Comparator edgeOrder,
                                     final LayoutDataProvider ldp, final ItemFactory itemFactory) {
    final Collection flatwiseEdges = new HashSet();
    final EdgeList centralEdges = new EdgeList();

    for (EdgeCursor ec = source ? node.outEdges() : node.inEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeData edgeData = ldp.getEdgeData(edge);
      final PortConstraint constraint = source ? edgeData.getSPC() : edgeData.getTPC();
      final Collection candidates = source ? edgeData.getSourceCandidates() : edgeData.getTargetCandidates();

      if (constraint != null && (constraint.isAtEast() || constraint.isAtWest())) {
        continue;
      }

      if (isFlatwiseCandidateCollection(candidates, getLayoutOrientation())) {
        flatwiseEdges.add(edge);
      } else {
        centralEdges.add(edge);
      }
    }

    if (flatwiseEdges.isEmpty()) {
      return;
    }

    centralEdges.addAll(flatwiseEdges);
    Comparators.sort(centralEdges, edgeOrder);

    int i = 0;
    for (EdgeCursor edgeCursor = centralEdges.edges(); edgeCursor.ok(); edgeCursor.next(), i++) {
      final Edge edge = edgeCursor.edge();
      if (flatwiseEdges.contains(edge)) {
        final byte side = i < centralEdges.size() / 2 ? PortConstraint.WEST : PortConstraint.EAST;
        itemFactory.setTemporaryPortConstraint(edge, source, PortConstraint.create(side));
      }
    }
  }

  static boolean isTemporarySameLayerEdge(Edge edge, LayoutDataProvider ldp) {
    return isTemporarySameLayerNode(edge.target(), ldp);
  }

  static boolean isTemporarySameLayerNode(Node node, LayoutDataProvider ldp) {
    return node.inDegree() == 2 && node.outDegree() == 0 && ldp.getNodeData(node) == null;
  }

  static byte getPreferredSideForTemporarySameLayerEdge(Edge edge, LayoutDataProvider ldp) {
    final Edge originalEdge = ldp.getEdgeData(edge).getAssociatedEdge();
    final boolean source = originalEdge.source().equals(edge.source());
    final NodeData sData = ldp.getNodeData(originalEdge.source());
    final NodeData tData = ldp.getNodeData(originalEdge.target());

    if (sData.getPosition() < tData.getPosition()) {
      return source ? PortConstraint.EAST : PortConstraint.WEST;
    } else {
      return !source ? PortConstraint.EAST : PortConstraint.WEST;
    }
  }

  private static boolean isAtPreferredPort(Edge edge, boolean source, LayoutDataProvider ldp) {
    final Edge e = getOriginalEdge(edge, ldp);
    final EdgeData edgeData = ldp.getEdgeData(e);
    final PortConstraint pc = source ? edgeData.getSPC() : edgeData.getTPC();
    return pc != null && (pc.isStrong() || pc.isAtEast() || pc.isAtWest());
  }

  private void optimizeForAlignment(LayoutGraph graph, LayoutDataProvider ldp, ItemFactory itemFactory,
                                    DataProvider node2AlignWith, DataProvider edge2Length) {
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (!alignmentCalculator.isSpecialNode(graph, node, ldp) || node.degree() < 2) {
        continue;
      }

      node.sortOutEdges(new PositionEdgeComparator(false, ldp));
      node.sortInEdges(new PositionEdgeComparator(true, ldp));

      final Edge criticalInEdge = getCriticalInEdge(node, node2AlignWith, edge2Length);
      final Edge criticalOutEdge = getCriticalOutEdge(node, node2AlignWith, edge2Length);

      if (criticalInEdge != null || criticalOutEdge != null) {
        optimizeWithCriticalEdges(node, ldp, itemFactory, criticalInEdge, criticalOutEdge);
      } else if (node.degree() > 2) {
        optimizeWithoutCriticalEdges(node, ldp, itemFactory);
      }

      // Parallel edges of the critical edges which have a port constraints at the left or right side must have a
      // port constraint for the same side at the opposite end, too. Otherwise, such an edge gets many bends and
      // may even destroy the alignment.
      if (criticalInEdge != null) {
        for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (criticalInEdge != edge && criticalInEdge.source() == edge.source()) {
            final PortConstraint pc = ldp.getEdgeData(edge).getTPC();
            if (isFlatwisePortConstraint(pc)) {
              itemFactory.setTemporaryPortConstraint(edge, true, PortConstraint.create(pc.getSide()));
            }
          }
        }
      }
      if (criticalOutEdge != null) {
        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (criticalOutEdge != edge && criticalOutEdge.target() == edge.target()) {
            final PortConstraint pc = ldp.getEdgeData(edge).getSPC();
            if (isFlatwisePortConstraint(pc)) {
              itemFactory.setTemporaryPortConstraint(edge, true, PortConstraint.create(pc.getSide()));
            }
          }
        }
      }
    }
  }

  private static void optimizeWithoutCriticalEdges(Node node, LayoutDataProvider ldp, ItemFactory factory) {
    if (node.outDegree() > node.inDegree()) {
      final Edge firstOut = node.firstOutEdge();
      final Edge lastOut = node.lastOutEdge();

      if (!hasSameLayerEdge(node, true, ldp) && !isAtPreferredPort(firstOut, true, ldp) &&
          (node.outDegree() != 2 || !isToRightPartition(firstOut.source(), firstOut.target(), ldp)
              || hasSameLayerEdge(node, false, ldp))) {
        factory.setTemporaryPortConstraint(firstOut, true, PortConstraint.create(PortConstraint.WEST));

      } else if (!hasSameLayerEdge(node, false, ldp) && !isAtPreferredPort(lastOut, true, ldp) &&
          (node.outDegree() != 2 || !isToLeftPartition(lastOut.source(), lastOut.target(), ldp))) {
        factory.setTemporaryPortConstraint(lastOut, true, PortConstraint.create(PortConstraint.EAST));
      }

    } else {
      final Edge firstIn = node.firstInEdge();
      final Edge lastIn = node.lastInEdge();

      if (!hasSameLayerEdge(node, true, ldp) && !isAtPreferredPort(firstIn, false, ldp) &&
          (node.degree() != 3 || !isToRightPartition(firstIn.target(), firstIn.source(), ldp)
              || hasSameLayerEdge(node, false, ldp))) {
        factory.setTemporaryPortConstraint(firstIn, false, PortConstraint.create(PortConstraint.WEST));

      } else if (!hasSameLayerEdge(node, false, ldp) && !isAtPreferredPort(lastIn, false, ldp) &&
          (node.degree() != 3 || !isToLeftPartition(lastIn.target(), lastIn.source(), ldp))) {
        factory.setTemporaryPortConstraint(lastIn, false, PortConstraint.create(PortConstraint.EAST));
      }
    }
  }

  private static void optimizeWithCriticalEdges(Node node, LayoutDataProvider ldp, ItemFactory factory,
                                                Edge criticalInEdge, Edge criticalOutEdge) {
    final Edge firstIn = node.firstInEdge();
    final Edge firstOut = node.firstOutEdge();
    final Edge lastIn = node.lastInEdge();
    final Edge lastOut = node.lastOutEdge();

    if (node.degree() == 3 && node.outDegree() == 2 && criticalOutEdge == null) {
      // Special case: the only in-edge is critical and there are two free out-edges
      if ((!isToRightPartition(firstOut.source(), firstOut.target(), ldp) && isBackEdge(firstOut, ldp))
          || isToLeftPartition(firstOut.source(), firstOut.target(), ldp)) {
        setOptimizedPortConstraint(firstOut, true, PortConstraint.WEST, ldp, factory);
        if ((!isToLeftPartition(lastOut.source(), lastOut.target(), ldp) && isBackEdge(lastOut, ldp))
            || isToRightPartition(lastOut.source(), lastOut.target(), ldp)) {
          setOptimizedPortConstraint(lastOut, true, PortConstraint.EAST, ldp, factory);
        }
      } else {
        setOptimizedPortConstraint(lastOut, true, PortConstraint.EAST, ldp, factory);
      }

    } else {

      if (node.degree() == 3 && node.inDegree() == 2 && criticalInEdge == null) {
        // Special case: the only out-edge is critical and there are two free in-edges
        if ((!isToRightPartition(firstIn.target(), firstIn.source(), ldp) && isBackEdge(firstIn, ldp))
            || isToLeftPartition(firstIn.target(), firstIn.source(), ldp)) {
          setOptimizedPortConstraint(firstIn, false, PortConstraint.WEST, ldp, factory);
          if ((!isToRightPartition(lastIn.target(), lastIn.source(), ldp) && isBackEdge(lastIn, ldp))
              || isToLeftPartition(lastIn.target(), lastIn.source(), ldp)) {
            setOptimizedPortConstraint(lastIn, true, PortConstraint.EAST, ldp, factory);
          }
        } else {
          setOptimizedPortConstraint(lastIn, true, PortConstraint.EAST, ldp, factory);
        }

      } else if (criticalInEdge == null || (node.outDegree() > node.inDegree() && criticalOutEdge != null)) {

        if (!hasSameLayerEdge(node, true, ldp)) {
          if (firstOut != criticalOutEdge) {
            setOptimizedPortConstraint(firstOut, true, PortConstraint.WEST, ldp, factory);
          } else if (firstIn != null && firstIn != criticalInEdge && node.inDegree() > 1) {
            setOptimizedPortConstraint(firstIn, false, PortConstraint.WEST, ldp, factory);
          }
        }

        if (!hasSameLayerEdge(node, false, ldp)) {
          if (lastOut != criticalOutEdge) {
            setOptimizedPortConstraint(lastOut, true, PortConstraint.EAST, ldp, factory);
          } else if (lastIn != null && lastIn != criticalInEdge && node.inDegree() > 1) {
            setOptimizedPortConstraint(lastIn, false, PortConstraint.EAST, ldp, factory);
          }
        }

      } else {
        if (!hasSameLayerEdge(node, true, ldp)) {
          if (firstIn != criticalInEdge) {
            setOptimizedPortConstraint(firstIn, false, PortConstraint.WEST, ldp, factory);
          } else if (firstOut != null && firstOut != criticalOutEdge && node.outDegree() > 1) {
            setOptimizedPortConstraint(firstOut, true, PortConstraint.WEST, ldp, factory);
          }
        }
        if (!hasSameLayerEdge(node, false, ldp)) {
          if (lastIn != criticalInEdge) {
            setOptimizedPortConstraint(lastIn, false, PortConstraint.EAST, ldp, factory);
          } else if (lastOut != null && lastOut != criticalOutEdge && node.outDegree() > 1) {
            setOptimizedPortConstraint(lastOut, true, PortConstraint.EAST, ldp, factory);
          }
        }
      }
    }
  }

  private static void setOptimizedPortConstraint(Edge edge, boolean source, byte direction, LayoutDataProvider ldp,
                                                 ItemFactory factory) {
    if (!isAtPreferredPort(edge, source, ldp)) {
      factory.setTemporaryPortConstraint(edge, source, PortConstraint.create(direction));
    }
  }

  /**
   * Special handling for messages (always attach them to the side of nodes)
   */
  private static void optimizeMessageNodes(LayoutGraph graph, LayoutDataProvider ldp, ItemFactory factory) {
    final EdgeList edges = new EdgeList(graph.edges());
    edges.splice(getSameLayerEdges(graph, ldp));

    for (EdgeCursor ec = edges.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      final Edge original = getOriginalEdge(e, ldp);
      final int sourceLaneId = getSwimlaneId(original.source(), ldp);
      final int targetLaneId = getSwimlaneId(original.target(), ldp);

      if (FlowchartElements.isMessageFlow(graph, e) && sourceLaneId != targetLaneId) {
        if (ldp.getNodeData(e.source()).getType() == NodeData.TYPE_NORMAL
            && FlowchartElements.isActivity(graph, e.source())) {
          factory.setTemporaryPortConstraint(e, true, PortConstraint.create(
              sourceLaneId < targetLaneId ? PortConstraint.EAST : PortConstraint.WEST));
        }

        if (ldp.getNodeData(e.target()).getType() == NodeData.TYPE_NORMAL
            && FlowchartElements.isActivity(graph, e.target())) {
          factory.setTemporaryPortConstraint(e, false, PortConstraint.create(
              sourceLaneId < targetLaneId ? PortConstraint.WEST : PortConstraint.EAST));
        }
      }
    }
  }

  static EdgeList getSameLayerEdges(LayoutGraph graph, LayoutDataProvider ldp) {
    EdgeList sameLayerEdges = new EdgeList();
    EdgeMap edge2Seen = Maps.createHashedEdgeMap();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      NodeData nData = ldp.getNodeData(n);
      for (ListCell cell = nData.getFirstSameLayerEdgeCell(); cell != null; cell = cell.succ()) {
        Edge sameLayerEdge = (Edge) cell.getInfo();
        Node opposite = sameLayerEdge.opposite(n);
        if (!edge2Seen.getBool(sameLayerEdge) && graph.contains(opposite)) {
          sameLayerEdges.add(sameLayerEdge);
          edge2Seen.setBool(sameLayerEdge, true);
        }
      }
    }
    return sameLayerEdges;
  }

  static EdgeList getSameLayerEdges(Node n, boolean left, LayoutDataProvider ldp) {
    NodeData nData = ldp.getNodeData(n);
    int nPos = nData.getPosition();
    EdgeList result = new EdgeList();
    for (ListCell cell = nData.getFirstSameLayerEdgeCell(); cell != null; cell = cell.succ()) {
      Edge sameLayerEdge = (Edge) cell.getInfo();
      Node other = sameLayerEdge.opposite(n);
      int otherPos = ldp.getNodeData(other).getPosition();
      if ((left && otherPos < nPos) || (!left && otherPos > nPos)) {
        result.add(sameLayerEdge);
      }
    }
    return result;
  }

  static boolean hasSameLayerEdge(Node n, boolean left, LayoutDataProvider ldp) {
    return !getSameLayerEdges(n, left, ldp).isEmpty();
  }

  static boolean isFlatwisePortConstraint(PortConstraint pc) {
    return pc != null && (pc.isAtEast() || pc.isAtWest());
  }

  static boolean isFlatwiseCandidateCollection(Collection pcs, byte layoutOrientation) {
    if (pcs == null) {
      return false;
    }

    boolean containsEast = false;
    boolean containsWest = false;
    for (Iterator iterator = pcs.iterator(); iterator.hasNext(); ) {
      final PortCandidate pc = (PortCandidate) iterator.next();
      final int direction = pc.getDirectionForLayoutOrientation(layoutOrientation);
      if (!containsEast && (PortCandidate.EAST & direction) != 0) {
        containsEast = true;
      }
      if (!containsWest && (PortCandidate.WEST & direction) != 0) {
        containsWest = true;
      }
    }

    return containsEast && containsWest;
  }

  static boolean isBackEdge(Edge edge, LayoutDataProvider ldp) {
    return ldp.getEdgeData(edge).isReversed();
  }

  /**
   * Returns an in-edge for the given node which comes from the node it is aligned with. If several such edges exist,
   * the edge with the highest length is returned.
   */
  private static Edge getCriticalInEdge(Node node, DataProvider node2AlignWith, DataProvider edge2Length) {
    Edge bestEdge = null;
    for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (node2AlignWith.get(node) == edge.source() &&
          (bestEdge == null || edge2Length.getDouble(bestEdge) < edge2Length.getInt(edge))) {
        bestEdge = edge;
      }
    }

    return bestEdge;
  }

  /**
   * Returns an out-edge for the given node which goes to the node it is aligned with. If several such edges exist, the
   * edge with the highest length is returned.
   */
  private static Edge getCriticalOutEdge(Node node, DataProvider node2AlignWith, DataProvider edge2Length) {
    Edge bestEdge = null;
    for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (node2AlignWith.get(edge.target()) == node &&
          (bestEdge == null || edge2Length.getDouble(bestEdge) < edge2Length.getInt(edge))) {
        bestEdge = edge;
      }
    }

    return bestEdge;
  }

  static Edge getOriginalEdge(Edge e, LayoutDataProvider ldp) {
    NodeData sData = ldp.getNodeData(e.source());
    if (sData.getType() == NodeData.TYPE_BEND && sData.getAssociatedEdge() != null) {
      return sData.getAssociatedEdge();
    }
    NodeData tData = ldp.getNodeData(e.target());
    if (tData.getType() == NodeData.TYPE_BEND && tData.getAssociatedEdge() != null) {
      return tData.getAssociatedEdge();
    }

//    final EdgeData edgeData = ldp.getEdgeData(e);
//    if (edgeData.getAssociatedEdge() != null) {
//      return edgeData.getAssociatedEdge();
//    }

    return e;
  }

  static int getSwimlaneId(Node n, LayoutDataProvider ldp) {
    SwimLaneDescriptor laneDesc = ldp.getNodeData(n).getSwimLaneDescriptor();
    return laneDesc == null ? -1 : laneDesc.getComputedLaneIndex();
  }

  static boolean isToLeftPartition(Node source, Node target, LayoutDataProvider layoutData) {
    SwimLaneDescriptor sourceDesc = layoutData.getNodeData(source).getSwimLaneDescriptor();
    SwimLaneDescriptor targetDesc = layoutData.getNodeData(target).getSwimLaneDescriptor();
    return sourceDesc != targetDesc && sourceDesc != null && targetDesc != null
        && sourceDesc.getComputedLaneIndex() > targetDesc.getComputedLaneIndex();
  }

  static boolean isToRightPartition(Node source, Node target, LayoutDataProvider layoutData) {
    SwimLaneDescriptor sourceDesc = layoutData.getNodeData(source).getSwimLaneDescriptor();
    SwimLaneDescriptor targetDesc = layoutData.getNodeData(target).getSwimLaneDescriptor();
    return sourceDesc != targetDesc && sourceDesc != null && targetDesc != null
        && sourceDesc.getComputedLaneIndex() < targetDesc.getComputedLaneIndex();
  }

  /**
   * Compares edges between the same layers according to the position of the end nodes of the specified end. Ties are
   * broken by the direction of the port constraints at the specified end, then at the opposite end, where WEST is first
   * and EAST is last.
   * <p/>
   * Can be used, for example, to sort in- or out-edges of a specific node in the typical best way.
   */
  static class PositionEdgeComparator implements Comparator {
    private final boolean source;
    private final LayoutDataProvider ldp;
    private final SameLayerNodePositionComparator sameLayerNodePositionComparator;
    private final SingleSidePortConstraintComparator portConstraintComparator;

    PositionEdgeComparator(boolean source, LayoutDataProvider ldp) {
      sameLayerNodePositionComparator = new SameLayerNodePositionComparator(ldp);
      portConstraintComparator = new SingleSidePortConstraintComparator();
      this.source = source;
      this.ldp = ldp;
    }

    public int compare(Object o1, Object o2) {
      final Edge e1 = (Edge) o1;
      final Edge e2 = (Edge) o2;

      // compare positions at specified end
      final int comparePos = sameLayerNodePositionComparator.compare(
          source ? e1.source() : e1.target(), source ? e2.source() : e2.target());
      if (comparePos != 0) {
        return comparePos;
      }

      // compare constraints at specified end
      final int compareConstraints = portConstraintComparator.compare(
          source ? ldp.getEdgeData(e1).getSPC() : ldp.getEdgeData(e1).getTPC(),
          source ? ldp.getEdgeData(e2).getSPC() : ldp.getEdgeData(e2).getTPC());
      if (compareConstraints != 0) {
        return compareConstraints;
      }

      // compare constraints at opposite end
      return portConstraintComparator.compare(
          source ? ldp.getEdgeData(e1).getTPC() : ldp.getEdgeData(e1).getSPC(),
          source ? ldp.getEdgeData(e2).getTPC() : ldp.getEdgeData(e2).getSPC());
    }
  }

  /**
   * Compares port constraints with respect to the upper or lower side of a node, that is WEST is first, EAST is last,
   * and NORTH and SOUTH are neutral elements in the middle.
   */
  static class SingleSidePortConstraintComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      final PortConstraint pc1 = (PortConstraint) o1;
      final PortConstraint pc2 = (PortConstraint) o2;

      // we use NORTH as neutral element since we care only about EST and WEST
      final byte b1 = pc1 != null ? pc1.getSide() : PortConstraint.NORTH;
      final byte b2 = pc2 != null ? pc2.getSide() : PortConstraint.NORTH;
      if (b1 == b2) {
        return 0;
      } else if (b1 == PortConstraint.WEST || b2 == PortConstraint.EAST) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  /**
   * Compares nodes in the same layer according to their positions.
   */
  static class SameLayerNodePositionComparator implements Comparator {
    private final LayoutDataProvider ldp;

    SameLayerNodePositionComparator(LayoutDataProvider ldp) {
      this.ldp = ldp;
    }

    public int compare(Object o1, Object o2) {
      return Comparators.compare(ldp.getNodeData((Node) o1).getPosition(), ldp.getNodeData((Node) o2).getPosition());
    }
  }

}
