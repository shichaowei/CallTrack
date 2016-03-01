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
package demo.view.uml;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.router.polyline.EdgeRouter;
import y.util.DataProviderAdapter;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * A {@link y.view.ViewMode} that creates an edge with its own target node if a source node exists.
 * The target node can be dragged around to a desired position. When its center lies within another node at the time,
 * the edge is connected to that node and the target node is deleted.
 */
class UmlCreateEdgeMode extends MoveSelectionMode {
  private final EdgeRouter edgeRouter;
  private Node sourceNode;
  private Node targetNode;

  private Node draggedNode;
  private Drawable targetNodeIndicator;

  UmlCreateEdgeMode(final EdgeRouter edgeRouter) {
    this.edgeRouter = edgeRouter;
    // create a new drawable for the target node
    targetNodeIndicator = new Drawable() {
      public void paint(Graphics2D graphics) {
        if (targetNode != null) {
          drawTargetNodeIndicator(graphics, getGraph2D().getRealizer(targetNode));
        }
      }

      public Rectangle getBounds() {
        if (targetNode != null) {
          return getTargetNodeIndicatorBounds(getGraph2D().getRealizer(targetNode)).getBounds();
        } else {
          return new Rectangle(0, 0, -1, -1);
        }
      }
    };
  }

  /**
   * Overwritten to add an edge with a new target node to the current graph when a drag starts. Afterwards, this target
   * node will be dragged around.
   */
  public void mousePressedLeft(double x, double y) {
    if (sourceNode != null) {
      final Graph2D graph = getGraph2D();
      graph.firePreEvent();

      // create the target node and the new edge
      // the target node will be dragged around until a mouse release
      draggedNode = graph.createNode();
      UmlRealizerFactory.setNodeOpacity(graph.getRealizer(draggedNode), UmlRealizerFactory.TRANSPARENT);
      graph.setCenter(draggedNode, x, y);
      graph.createEdge(sourceNode, draggedNode);

      super.mousePressedLeft(x, y);
    }
  }

  /**
   * Overwritten to just move the target node of the new edge.
   *
   * @return a {@link NodeList} with the target node as the only element.
   */
  protected NodeList getNodesToBeMoved() {
    final NodeList nodesToBeMoved = new NodeList();
    if (draggedNode != null) {
      nodesToBeMoved.add(draggedNode);
    }
    return nodesToBeMoved;
  }

  /**
   * Overwritten to keep track of the nodes over which the new target node moves. Nodes that contain the center of the
   * target node are possible new target nodes for the edge and will be marked.
   */
  public void mouseDraggedLeft(final double x, final double y) {
    if (sourceNode != null) {
      // if the center of the dragged node lies within other nodes, the node in front gets selected as target node
      updateTargetNode();

      super.mouseDraggedLeft(x, y);
    }
  }

  /**
   * Updates the target node when the currently dragged node moves over other nodes. Nodes that contain the center of
   * the target node are possible new target nodes for the edge and will be marked.
   */
  private void updateTargetNode() {
    final Graph2D graph = getGraph2D();

    // find a node that contains the center of the dragged node
    // if there are several nodes the node in front is taken
    Node newTargetNode = null;
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (node != draggedNode) {
        final NodeRealizer realizer = graph.getRealizer(node);
        final Rectangle2D.Double bounds = realizer.getBoundingBox();
        if (bounds.contains(graph.getCenterX(draggedNode), graph.getCenterY(draggedNode))
            && (newTargetNode == null || newTargetNode.index() < node.index())) {
          newTargetNode = node;
        }
      }
    }

    // if the target node changed, the target node indicator gets updated
    if (newTargetNode != targetNode) {
      targetNode = newTargetNode;
      updateTargetNodeIndicator();
    }
  }

  /**
   * Updates the drawable that displays indication marks for the current target node.
   */
  private void updateTargetNodeIndicator() {
    view.removeDrawable(targetNodeIndicator);
    if (targetNode != null) {
      view.addDrawable(targetNodeIndicator);
    }
  }

  /**
   * Overwritten to properly end edge creation. If the node was dragged onto another node, this other node becomes the
   * new target node for the edge and the former target node gets removed.
   */
  public void mouseReleasedLeft(double x, double y) {
    if (sourceNode != null) {
      final Graph2D graph = getGraph2D();
      final Edge draggedEdge = draggedNode.lastInEdge();
      if (targetNode != null) {
        // if the dragged node is dropped on another node, the other node becomes the new target and the dragged node
        // is removed
        graph.changeEdge(draggedEdge, draggedEdge.source(), targetNode);
        graph.removeNode(draggedNode);
      } else {
        UmlRealizerFactory.setNodeOpacity(graph.getRealizer(draggedNode), UmlRealizerFactory.OPAQUE);
      }

      // clean up target node indicator
      view.removeDrawable(targetNodeIndicator);

      // end edge creation step for undo/redo
      graph.firePostEvent();

      edgeCreated(draggedEdge);

      draggedNode = null;
      targetNode = null;
      sourceNode = null;

      super.mouseReleasedLeft(x, y);
    }
  }

  /**
   * Overwritten to disable event handling from the right mouse button.
   */
  public void mouseDraggedRight(double x, double y) {
  }

  /**
   * Overwritten to disable event handling from the right mouse button.
   */
  public void mouseReleasedRight(double x, double y) {
  }

  /**
   * Overwritten to clean up this {@link y.view.ViewMode} in case edge creation was aborted.
   */
  public void cancelEditing() throws UnsupportedOperationException {
    // resets all nodes and drawables
    if (draggedNode != null) {
      getGraph2D().removeNode(draggedNode);
    }
    draggedNode = null;
    targetNode = null;
    sourceNode = null;
    view.removeDrawable(targetNodeIndicator);

    super.cancelEditing();
  }

  /**
   * Sets the source node for the newly created edge.
   */
  public void setSourceNode(Node sourceNode) {
    this.sourceNode = sourceNode;
  }

  /**
   * Callback to be able to react when edge creation is finished.
   */
  protected void edgeCreated(final Edge edge) {
    final Graph2D graph = getGraph2D();
    final DataProviderAdapter selectedEdges = new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        return dataHolder == edge;
      }
    };
    edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
    graph.addDataProvider(EdgeRouter.SELECTED_EDGES, selectedEdges);
    try {
      final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
      executor.getLayoutMorpher().setKeepZoomFactor(true);
      executor.doLayout(view, edgeRouter);
    } finally {
      graph.removeDataProvider(EdgeRouter.SELECTED_EDGES);
    }
  }
}
