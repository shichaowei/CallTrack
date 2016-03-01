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
package demo.view.mindmap;

import y.base.Command;
import y.base.Edge;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.BendList;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericEdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DUndoManager;
import y.view.HitInfo;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.view.ViewMode;

import java.awt.event.MouseEvent;

/**
 * Handles mouse dragged events to rearrange the mind map.
 */
class MoveNodeMode extends EditMode {
  private final Graph2DUndoManager undoManager;

  MoveNodeMode( final Graph2DUndoManager undoManager ) {
    this.undoManager = undoManager;
  }

  public void mouseClicked( final MouseEvent e ) {
    super.mouseClicked(e);
  }

  /**
   * Called when the mouse is dragged outside the graph.
   * Empty method prevents multi-selection
   *
   * @param graph     the graph which resides in the canvas
   * @param x         the x coordinate where the mouse was clicked
   * @param y         the y coordinate where the mouse was clicked
   * @param firstDrag <code>true</code> if the previous mouse event captured by
   *                  this <code>ViewMode</code> was <em>not</em> a drag event;
   */
  protected void paperDragged(
      final Graph2D graph,
      final double x,
      final double y,
      final boolean firstDrag
  ) {

  }

  /**
   * Creates a view mode for interactive creation of cross-reference edges.
   */
  protected ViewMode createCreateEdgeMode() {
    return new MyCreateEdgeMode();
  }

  /**
   * use {@link MyCreateEdgeMode} to create a cross edge when the cross edge button was pressed.
   * @param startNode source for cross edge
   */
  public void startCrossEdgeCreation(Node startNode) {
    final NodeRealizer realizer = getGraph2D().getRealizer(startNode);
    //initialize edge creation
    setChild(createEdgeMode,null,null);
    //trigger start of edge creation
    createEdgeMode.mousePressedLeft(realizer.getCenterX(), realizer.getCenterY());
  }

  /**
   * Override to introduce own MoveSelectionMode
   *
   * @return own MoveSelectionMode
   */
  protected ViewMode createMoveSelectionMode() {
    return new MindMapSelectionMode();
  }

  /**
   * Cross edge is created between selected and clicked node
   *
   * @param x horizontal mouse position
   * @param y vertical mouse position
   */
  public void mouseShiftReleasedLeft(final double x, final double y) {
    final Graph2D graph2D = view.getGraph2D();
    final NodeCursor selectedNodes = graph2D.selectedNodes();
    if (selectedNodes.ok()) {
      final Node startNode = selectedNodes.node();
      final HitInfo hitInfo = getHitInfo(x, y);
      if (hitInfo.hasHitNodes()) {
        final Node endNode = hitInfo.getHitNode();
        if (endNode != startNode) {
          final Edge edge = graph2D.createEdge(startNode, endNode);
          ViewModel.instance.setCrossReference(edge);
          graph2D.setSelected(startNode, false);
        }
      }
    } else {
      super.mouseShiftPressedLeft(x, y);
    }
  }


  private class MindMapSelectionMode extends MoveSelectionMode {
    /**
     * The maximum distance to the next parent item.
     * If an item is dragged around and has a greater distance than this to his next parent,
     * it will be removed from the mind map
     */
    public static final int MAX_KEEP_DISTANCE = 200;
    /**
     * the item that is been dragged around
     */
    private Node node;
    /**
     * the last side of the node, true for left
     */
    private boolean oldSide;

    private MindMapSelectionMode() {
    }

    public void mouseShiftPressedLeft(final double x, final double y) {
      setChild(createEdgeMode, lastPressEvent, lastDragEvent);
    }

    /**
     * initialize node moving
     */
    protected void selectionMoveStarted(double x, double y) {
      if (node == null) {
        return;
      }
      final Graph2D graph2D = view.getGraph2D();
      //backup incoming edge to make port change undoable
      graph2D.backupRealizers();
      oldSide = ViewModel.instance.isLeft(node);
    }

    /**
     * Called when mouse is moved.
     * updates the side and the parent
     * @param dx the difference between the given x-coordinate and the
     * x-coordinate of the last mouse event handled by this mode.
     * @param dy the difference between the given y-coordinate and the
     * y-coordinate of the last mouse event handled by this mode.
     * @param x the x-coordinate of the triggering mouse event in the world
     * coordinate system.
     * @param y the y-coordinate of the triggering mouse event in the world
     */
    protected void selectionOnMove(double dx, double dy, double x, double y)  {
      if (node != null) {
        final Graph2D graph = view.getGraph2D();
        final ViewModel model = ViewModel.instance;
        final NodeRealizer rootRealizer = graph.getRealizer(model.getRoot());
        final boolean isRight = x > rootRealizer.getCenterX();
        final boolean wasLeft = model.isLeft(node);
        Edge inEdge = MindMapUtil.inEdge(node);
        //if mouse changed side of center item
        if (wasLeft == isRight) {
          // update visuals of subtree after a possible side change
          MindMapUtil.updateVisualsRecursive(graph, node, !wasLeft);

          LayoutUtil.layoutSubtree(graph, node);

          //inform the undo manager of a side change
          if (oldSide != model.isLeft(node)) {
            undoManager.push(new SideChangeAction(!oldSide, node));
          }

          //MoveSelectionMode keeps the relative position of all nodes
          //until the mouse button is released, layout changes didn't affect
          //MoveSelectionMode. Triggering mousePressedLeft forces MoveSelectionMode
          //to reinitialize the node positions.
          mousePressedLeft(x, y);
        }

        //determine (new) parent
        final Node parent = calcClosestParent(node);

        //if no parent is in range, delete edge to last parent,
        //showing the user there is currently no connection.
        //Item should not be deleted until mouse button is released
        if (parent == null) {
          if (inEdge != null) {
            graph.removeEdge(inEdge);
          }
        //change the parent item
        } else {
          final Node source = (inEdge != null) ? inEdge.source() : null;
          if (parent != source) {
            if (graph.containsEdge(parent, node)) {
              if (inEdge != null && graph.contains(inEdge)) {
                graph.removeEdge(inEdge);
              }
            } else {
              if (inEdge == null) {
                inEdge = graph.createEdge(
                        parent, node, new GenericEdgeRealizer("BezierGradientEdge"));
              } else {
                if (!graph.contains(inEdge)) {
                  graph.reInsertEdge(inEdge);
                }
                graph.changeEdge(inEdge, parent, node);
              }

              // make sure the edge connects at the bottom line of the node
              final EdgeRealizer er = graph.getRealizer(inEdge);
              er.clearBends();
              if (model.isRoot(parent)) {
                er.getSourcePort().setOffsets(0, 0);
              } else {
                final NodeRealizer src = graph.getRealizer(parent);
                final double srcX = src.getWidth() * 0.5 * (isRight ? 1 : -1);
                er.getSourcePort().setOffsets(srcX, src.getHeight() * 0.5);
              }
              final NodeRealizer tgt = graph.getRealizer(node);
              final double tgtX = tgt.getWidth() * 0.5 * (isRight ? -1 : 1);
              er.getTargetPort().setOffsets(tgtX, tgt.getHeight() * 0.5);
            }
          }
        }
      }
    }

    /**
     * Called after the left button was released.
     * Updates the node settings, depending where it was placed.
     * @param dx the difference between the given x-coordinate and the
     * x-coordinate of the last mouse event handled by this mode.
     * @param dy the difference between the given y-coordinate and the
     * y-coordinate of the last mouse event handled by this mode.
     * @param x the x-coordinate of the triggering mouse event in the world
     * coordinate system.
     * @param y the y-coordinate of the triggering mouse event in the world
     */
    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      final Node node = this.node;
      if (node != null) {
        final Graph2D graph = view.getGraph2D();
        final ViewModel model = ViewModel.instance;

        Node parent = null;
        if (!model.isRoot(node)) {
          //determine parent item, if there is any
          final Edge inEdge = MindMapUtil.inEdge(node);
          parent = inEdge == null ? null : inEdge.source();
          if (parent != null) {
            // update visuals of subtree after a possible side change
            final boolean isLeft = model.isLeft(node);
            MindMapUtil.updateVisualsRecursive(graph, node, isLeft);

            //make siblings visible
            //exclude this node from the undo collapse action
            if (model.isCollapsed(parent)) {
              MindMapUtil.expandNode(graph, parent);
            }
          }
        }

        //remove item when not connected to the mind map anymore
        if (parent == null && !model.isRoot(node)) {
          MindMapUtil.removeSubtree(graph, node);
        }

        LayoutUtil.layout(graph);

        this.node = null;
      }
    }

    /**
     * Returns the whole subtree of a node.
     * Ignores cross edges.
     * @return nodes subtree
     */
    protected NodeList getNodesToBeMoved() {
      NodeList nodes = new NodeList();
      final HitInfo lastHitInfo = getLastHitInfo();
      node = lastHitInfo.getHitNode();
      //root node should not be able to move
      if (ViewModel.instance.isRoot(node)) {
        node = null;
      }
      if (node != null) {
        NodeList stack = new NodeList(node);
        while (!stack.isEmpty()) {
          Node n = stack.popNode();
          for (EdgeList edges = MindMapUtil.outEdges(n); !edges.isEmpty();) {
            stack.push(edges.popEdge().target());
          }
          nodes.push(n);
        }
      } else {
        nodes = new NodeList();
      }
      return nodes;
    }

    /**
     * bends of nodes returned by {@link this.getNodesToBeMoved}
     * @return all bends
     */
    protected BendList getBendsToBeMoved() {
      if (node != null) {
        BendList bends = new BendList();
        final NodeList nodesToBeMoved = getNodesToBeMoved();
        while (!nodesToBeMoved.isEmpty()) {
          for (EdgeList edges = MindMapUtil.outEdges(nodesToBeMoved.popNode());!edges.isEmpty();) {
            bends.addAll(getGraph2D().getRealizer(edges.popEdge()).bends());
          }
        }
        return bends;
      } else {
        return super.getBendsToBeMoved();
      }
    }

    /**
     * Calc the nearest possible parent.
     * @param node node whose parents are calculated
     * @return nearest item, when distance is less then {@link this.MAX_KEEP_DISTANCE}
     */
    private Node calcClosestParent(Node node) {
      final NodeList possibleParents = calcPossibleParents(node);
      Node bestParent = possibleParents.popNode();
      final Graph2D graph2D = view.getGraph2D();
      final NodeRealizer nodeRealizer = graph2D.getRealizer(node);
      double dist = calcDist(nodeRealizer, graph2D.getRealizer(bestParent));
      while (!possibleParents.isEmpty()) {
        final Node otherParent = (Node) possibleParents.pop();
        final double otherDist = calcDist(nodeRealizer, graph2D.getRealizer(otherParent));
        if (otherDist < dist) {
          bestParent = otherParent;
          dist = otherDist;
        }
      }
      final NodeRealizer bestParentRealizer = graph2D.getRealizer(bestParent);
      //make distance independent from the width of the nodes
      dist -= ((bestParentRealizer.getWidth() + nodeRealizer.getWidth()) * 0.5);
      if (dist < MAX_KEEP_DISTANCE) {
        return bestParent;
      } else {
        return null;
      }
    }

    /**
     * Calculate the distance between two items
     * @param firstRealizer  one node realizer
     * @param secondRealizer second node realizer
     * @return euclidean distance between two items
     */
    private double calcDist(final NodeRealizer firstRealizer, final NodeRealizer secondRealizer) {
      final double dx = firstRealizer.getCenterX() - secondRealizer.getCenterX();
      final double dy = firstRealizer.getCenterY() - secondRealizer.getCenterY();
      return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate nodes that may be a valid parent.
     * Valid parent are nearer to the center item than this item
     *
     * @param node node whose parents are calculated
     * @return all valid parents
     */
    private NodeList calcPossibleParents(Node node) {
      final Graph2D graph2D = getGraph2D();
      final ViewModel model = ViewModel.instance;
      final NodeRealizer nodeRealizer = graph2D.getRealizer(node);
      final boolean nodeIsLeft = model.isLeft(node);

      final NodeList possibleParents = new NodeList();
      final NodeList stack = new NodeList();
      final Node root = model.getRoot();
      stack.add(root);
      //the center item is always valid
      possibleParents.add(root);
      while (!stack.isEmpty()) {
        Node n = stack.popNode();
        for (EdgeList edges = MindMapUtil.outEdges(n); !edges.isEmpty(); ) {
          n = edges.popEdge().target();
          final NodeRealizer nr = graph2D.getRealizer(n);
          if (nodeIsLeft) {
            //if both items on the left side and <code>n</code> "lefter" than <code>node</code>
            if (model.isLeft(n) &&
                nodeRealizer.getX() + nodeRealizer.getWidth() < nr.getX()) {
              possibleParents.add(n);
              stack.add(n);
            }
          //if both items on the right side and <code>n</code> "righter" than <code>node</code>
          } else if (!model.isLeft(n) && ((nr.getX() + nr.getWidth()) < nodeRealizer.getX())) {
            possibleParents.add(n);
            stack.add(n);
          }
        }
      }
      return possibleParents;
    }
  }

  /**
   * Action to make a side change of a node undo/redo able.
   */
  private class SideChangeAction implements Command {
    private final boolean newSide;
    private final Node node;

    private SideChangeAction(final boolean newSide, final Node node) {
      this.newSide = newSide;
      this.node = node;
    }

    public void execute() {}

    public void undo() {
      MindMapUtil.updateVisualsRecursive(view.getGraph2D(), node, !newSide);
    }

    public void redo() {
      MindMapUtil.updateVisualsRecursive(view.getGraph2D(), node, newSide);
    }
  }

  /**
   * Marks interactively created edges as cross-reference edges.
   */
  private static final class MyCreateEdgeMode extends CreateEdgeMode {
    /**
     * Marks all interactively created edges as cross-reference edges.
     * @param edge the newly created <code>Edge</code> instance.
     */
    protected void edgeCreated( final Edge edge ) {
      ViewModel.instance.setCrossReference(edge);
    }
  }
}
