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
package demo.view.isometry;

import y.algo.NodeOrders;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YList;
import y.view.Bend;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.NodeLabel;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.Port;
import y.view.hierarchy.HierarchyManager;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link Graph2DTraversal} for isometric graphs.
 *
 * As isometric (3-dimensional) objects can overlap with each other, it is important to paint objects that are behind
 * other objects first. That way, the objects in front are painted above and seem to be in front of the others.
 * <p>
 *   To get the right order for all graph elements, this <code>Graph2DTraversal</code> holds the following order:
 *   <ul>
 *     <li>Group and folder nodes (with their labels and node ports)</li>
 *     <li>Edges (with their bends and ports)</li>
 *     <li>
 *       Normal nodes (with their labels and node ports) and edge labels, where nodes/labels with a lower y-coordinate
 *       come first
 *     </li>
 *   </ul>
 * </p>
 */
public class IsometryGraphTraversal implements Graph2DTraversal {
  public Iterator firstToLast(Graph2D graph, int elementTypes) {
    final List allElements = collectAllGraphElements(graph);
    final List filteredElements = filterGraphElements(allElements, elementTypes);
    return filteredElements.iterator();
  }

  public Iterator lastToFirst(Graph2D graph, int elementTypes) {
    final List allElements = collectAllGraphElements(graph);
    final List filteredElements = filterGraphElements(allElements, elementTypes);
    Collections.reverse(filteredElements);
    return filteredElements.iterator();
  }

  /**
   * Collects all elements in the given graph in the order they will be traversed.
   */
  private static List collectAllGraphElements(final Graph2D graph) {
    final ArrayList graphElements = new ArrayList();
    final ArrayList normalNodesAndEdgeLabels = new ArrayList();

    // add group/folder nodes with their labels and node ports
    // and collect normal nodes to add them later
    final HierarchyManager hierarchyManager = graph.getHierarchyManager();
    if (hierarchyManager != null) {
      for (Iterator it = hierarchyManager.preTraversal(); it.hasNext(); ) {
        final Node node = (Node) it.next();
        if (hierarchyManager.isNormalNode(node)) {
          normalNodesAndEdgeLabels.add(node);
        } else {
          graphElements.add(node);
          final NodeRealizer realizer = graph.getRealizer(node);
          for (int i = 0; i < realizer.portCount(); i++) {
            graphElements.add(realizer.getPort(i));
          }
          for (int i = 0; i < realizer.labelCount(); i++) {
            graphElements.add(realizer.getLabel(i));
          }
        }
      }
    }

    // add edges with their bends, ports and labels
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      graphElements.add(edge);
      final EdgeRealizer realizer = graph.getRealizer(edge);
      for (int i = 0; i < realizer.bendCount(); i++) {
        graphElements.add(realizer.getBend(i));
      }
      graphElements.add(realizer.getSourcePort());
      graphElements.add(realizer.getTargetPort());
      for (int i = 0; i < realizer.labelCount(); i++) {
        normalNodesAndEdgeLabels.add(realizer.getLabel(i));
      }
    }

    // sort normal nodes and edge labels according to their position in layout space
    sort(normalNodesAndEdgeLabels);

    // add edge labels and normal nodes with their labels and node ports
    for (Iterator it = normalNodesAndEdgeLabels.iterator(); it.hasNext(); ) {
      final Object element = it.next();
      graphElements.add(element);
      if (element instanceof Node) {
        final NodeRealizer realizer = graph.getRealizer((Node) element);
        for (int i = 0; i < realizer.portCount(); i++) {
          graphElements.add(realizer.getPort(i));
        }
        for (int i = 0; i < realizer.labelCount(); i++) {
          graphElements.add(realizer.getLabel(i));
        }
      }
    }

    return graphElements;
  }

  /**
   * Filters the given list of graph elements so it contains only elements of the allowed types.
   */
  private static List filterGraphElements(final List graphElements, final int elementTypes) {
    if (elementTypes == ALL) {
      // all types of elements are allowed, so we don't have to filter
      return new ArrayList(graphElements);
    }

    final ArrayList filteredElements = new ArrayList();
    for (Iterator it = graphElements.iterator(); it.hasNext(); ) {
      final Object element = it.next();

      // only add element if its type is allowed
      if ((element instanceof Node && (elementTypes & NODES) != 0)
          || (element instanceof Edge && (elementTypes & EDGES) != 0)
          || (element instanceof NodeLabel && (elementTypes & NODE_LABELS) != 0)
          || (element instanceof EdgeLabel && (elementTypes & EDGE_LABELS) != 0)
          || (element instanceof Bend && (elementTypes & BENDS) != 0)
          || (element instanceof Port && (elementTypes & PORTS) != 0)
          || (element instanceof NodePort && (elementTypes & NODE_PORTS) != 0)) {
        filteredElements.add(element);
      }
    }
    return filteredElements;
  }

  /**
   * Sorts the given elements according to their positions in layout space.
   * <p>
   *   This implementation uses a sweepline (slope -1) to build a constraint graph. An edge will be created in this
   *   graph, when the source is behind the target. After that the graph nodes get a topological order that is assigned
   *   to the given elements.
   * </p>
   */
  private static void sort(List elements) {
    final Graph constraintsGraph = new Graph();
    final NodeMap constraints2graph = constraintsGraph.createNodeMap();
    final NodeMap node2rectangle = constraintsGraph.createNodeMap();
    final ArrayList events = new ArrayList();

    // create constraint graph and events
    for (Iterator it = elements.iterator(); it.hasNext(); ) {
      final Object element = it.next();
      final Rectangle2D bounds = getBounds(element);
      final Node constraintsNode = constraintsGraph.createNode();
      constraints2graph.set(constraintsNode, element);
      node2rectangle.set(constraintsNode, bounds);
      final double boundsX = bounds.getX();
      final double boundsY = bounds.getY();
      events.add(new SweeplineEvent(boundsX, boundsY, constraintsNode, true));
      events.add(new SweeplineEvent(boundsX + bounds.getWidth(), boundsY + bounds.getHeight(), constraintsNode, false));
    }

    // events have to be sorted according to their distance orthogonal to the sweepline
    Collections.sort(events);

    // sweep through the events
    final YList currentElements = new YList();
    for (Iterator it = events.iterator(); it.hasNext(); ) {
      final SweeplineEvent event = (SweeplineEvent) it.next();
      if (event.open) {
        // an element is opened and must be inserted into the list of currently opened elements
        // its position is in front of the first element where the sweepline hits above the event coordinates
        final ListCell successor = findSuccessor(currentElements, event.coordX, event.coordY, node2rectangle);
        ListCell cell;
        if (successor != null) {
          cell = currentElements.insertBefore(event.node, successor);
          constraintsGraph.createEdge((Node) successor.getInfo(), event.node);
        } else {
          cell = currentElements.addLast(event.node);
        }
        if (cell.pred() != null) {
          constraintsGraph.createEdge(event.node, (Node) cell.pred().getInfo());
        }
      } else {
        // an element is closed and must be removed from the list of currently opened elements
        // its neighbors get updated as they become direct neighbors now
        final ListCell cell = currentElements.findCell(event.node);
        if (cell.pred() != null && cell.succ() != null) {
          constraintsGraph.createEdge((Node) cell.succ().getInfo(), (Node) cell.pred().getInfo());
        }
        currentElements.removeCell(cell);
      }
    }

    // sort the nodes of the constraint graph topologically and assign the new order to the given elements
    elements.clear();
    final NodeList topological = NodeOrders.topological(constraintsGraph);
    for (NodeCursor nc = topological.nodes(); nc.ok(); nc.next()) {
      final Node constraintNode = nc.node();
      if (constraints2graph.get(constraintNode) != null) {
        elements.add(constraints2graph.get(constraintNode));
      }
    }

    // clean up the node maps
    constraintsGraph.disposeNodeMap(constraints2graph);
    constraintsGraph.disposeNodeMap(node2rectangle);
  }

  /**
   * Returns the first list cell that contains the node that is hit by the sweepline above the given coordinates.
   */
  private static ListCell findSuccessor(YList list, double coordX, double coordY, NodeMap node2rectangle) {
    if (!list.isEmpty()) {
      for (ListCell cell = list.firstCell(); cell != null; cell = cell.succ()) {
        final Node node = (Node) cell.getInfo();
        final Rectangle2D rect = (Rectangle2D) node2rectangle.get(node);

        // calculate intersection of sweepline with extended right rect border (rect.getX() + rect.getWidth()),
        // where coordX + coordY == (rect.getX() + rect.getWidth()) + rect.getY()
        final double intersectionY = coordX + coordY - (rect.getX() + rect.getWidth());

        double refY;
        if (intersectionY < rect.getY()) {
          // intersection is outside rect
          // --> rectangle intersects the sweepline with its upper side
          refY = rect.getY();
        } else {
          // rectangle intersects the sweepline with its right side
          // --> take intersection as reference
          refY = intersectionY;
        }

        if (refY < coordY) {
          // reference is behind coordY
          return cell;
        }
      }
    }
    return null;
  }

  /**
   * Returns the element's bounds in layout space.
   */
  private static Rectangle2D getBounds(Object element) {
    if (element instanceof Node) {
      final Node node = (Node) element;
      final Graph2D graph = (Graph2D) node.getGraph();
      final NodeRealizer realizer = graph.getRealizer(node);
      if (realizer instanceof GenericNodeRealizer) {
        final IsometryData isometryData = (IsometryData) ((GenericNodeRealizer) realizer).getUserData();

        final double[] corners = new double[16];
        isometryData.calculateCorners(corners);
        IsometryData.moveTo(realizer.getX(), realizer.getY(), corners);
        final double x = IsometryData.toLayoutX(corners[IsometryData.C0_X], corners[IsometryData.C0_Y]);
        final double y = IsometryData.toLayoutY(corners[IsometryData.C0_X], corners[IsometryData.C0_Y]);
        final double w = isometryData.getWidth();
        final double h = isometryData.getDepth();
        return new Rectangle2D.Double(x, y, w, h);
      }
    } else if (element instanceof EdgeLabel) {
      final EdgeLabel label = (EdgeLabel) element;
      final Graph2D graph = (Graph2D) label.getEdge().getGraph();
      final Point2D sourceIntersection = graph.getRealizer(label.getEdge()).getSourceIntersection();
      final IsometryData isometryData = (IsometryData) label.getUserData();
      final double[] corners = new double[16];
      isometryData.calculateCorners(corners);
      IsometryData.moveTo(label.getOffsetX() + sourceIntersection.getX(),
          label.getOffsetY() + sourceIntersection.getY(), corners);

      double x, y, w, h;
      if (isometryData.isHorizontal()) {
        x = IsometryData.toLayoutX(corners[IsometryData.C0_X], corners[IsometryData.C0_Y]);
        y = IsometryData.toLayoutY(corners[IsometryData.C0_X], corners[IsometryData.C0_Y]);
        w = isometryData.getWidth();
        h = 0;
      } else {
        x = IsometryData.toLayoutX(corners[IsometryData.C1_X], corners[IsometryData.C1_Y]);
        y = IsometryData.toLayoutY(corners[IsometryData.C1_X], corners[IsometryData.C1_Y]);
        w = 0;
        h = isometryData.getDepth();
      }
      return new Rectangle2D.Double(x, y, w, h);
    }

    return null;
  }

  /**
   * Event for a sweepline with slope -1.
   */
  private static class SweeplineEvent implements Comparable {
    double coordX;
    double coordY;
    Node node;
    boolean open;

    private SweeplineEvent(double coordX, double coordY, Node node, boolean open) {
      this.coordX = coordX;
      this.coordY = coordY;
      this.node = node;
      this.open = open;
    }

    public int compareTo(Object o) {
      final SweeplineEvent other = (SweeplineEvent) o;

      final double thisCoord = coordX + coordY;
      final double otherCoord = other.coordX + other.coordY;

      if (thisCoord < otherCoord) {
        return -1;
      } else if (thisCoord > otherCoord) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
