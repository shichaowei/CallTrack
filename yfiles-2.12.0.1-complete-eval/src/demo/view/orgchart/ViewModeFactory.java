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
package demo.view.orgchart;

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YList;
import y.geom.YPoint;
import y.geom.YVector;
import y.layout.NormalizingGraphElementOrderStage;
import y.util.GraphHider;
import y.util.Maps;
import y.view.BendList;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.LineType;
import y.view.MoveSelectionMode;
import y.view.NavigationMode;
import y.view.NodeRealizer;
import y.view.Scroller;
import y.view.ViewCoordDrawableAdapter;
import y.view.ViewMode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * Provides specialised view modes for interactively editing the tree
 * structure or panning and collapsing and expanding group and folder nodes.
 */
public class ViewModeFactory {
  private ViewModeFactory() {
  }


  /**
   * Creates a new view mode for panning and collapsing and expanding group and
   * folder nodes. Panning is done by left- or right-dragging the mouse.
   * Collapsing and expanding is done by double-clicking group or folder nodes.
   * @param view the tree chart for which the view mode is created.
   * @return a new {@link JTreeChartViewMode} instance.
   */
  static ViewMode newNavigationMode( final JTreeChart view ) {
    final JTreeChartViewMode viewMode = new JTreeChartViewMode();
    Scroller scroller = viewMode.getScroller();
    scroller.setScrollSpeedFactor(2.0);
    scroller.setDrawable(new ScrollerDrawable(view, scroller));
    return viewMode;
  }

  /**
   * <code>NavigationMode</code> that provides custom single and double mouse
   * click handling.
   * <p>
   * Single clicking a node representing business data (and not a business unit)
   * will select said node; single clicking anything else will unselect any
   * selected node.
   * </p><p>
   * Double clicking a node representing business data will invoke
   * {@link JTreeChart#performNodeAction(y.base.Node)} for that node, double
   * clicking a node representing a business unit will toggle the node's
   * collapsed/expanded state, and finally double clicking anything but a node
   * will trigger an animated fit content operation
   * (see {@link demo.view.orgchart.JTreeChart#fitContent(boolean)}).
   * </p>
   */
  public static class JTreeChartViewMode extends NavigationMode {
    public JTreeChart getJTreeChart() {
      return (JTreeChart) view;
    }

    public void mouseClicked(double x, double y) {
      if(lastClickEvent.getClickCount() > 1) {
        mouseDoubleClicked(x, y);
      }
      else {
        mouseSingleClicked(x,y);
      }
    }

    /**
     * Handles single mouse clicks for the specified world coordinates.
     * Single clicking a normal node (as opposed to a group or folder node)
     * will select said node (exclusively). Single clicking anything else will
     * unselect all previously selected items.
     * @param x   the x-coordinate in the associated view's world coordinate
     * system.
     * @param y   the y-coordinate in the associated view's world coordinate
     * system.
     * @see y.view.hierarchy.HierarchyManager#isFolderNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isGroupNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isNormalNode(y.base.Node)
     */
    protected void mouseSingleClicked(double x, double y) {
      view.getCanvasComponent().requestFocus();
      HitInfo info = getHitInfo(x,y);
      Node node = info.getHitNode();
      Graph2D graph = getGraph2D();
      if (node != null && getGraph2D().getHierarchyManager().isNormalNode(node)) {
        if (!graph.isSelected(node)) {
          graph.unselectAll();
          graph.setSelected(node, true);
        }
      } else {
        getGraph2D().unselectAll();
      }
      getGraph2D().updateViews();
    }

    /**
     * Handles double mouse clicks for the specified world coordinates.
     * Double clicking a normal node will invoke
     * {@link demo.view.orgchart.JTreeChart#performNodeAction(y.base.Node)} for
     * that node; double clicking a group node will collapse or close the group
     * (i.e. hide its content); double clicking a folder node will expand or
     * open the folder (i.e. unhide its content).
     * @param x   the x-coordinate in the associated view's world coordinate
     * system.
     * @param y   the y-coordinate in the associated view's world coordinate
     * system.
     * @see y.view.hierarchy.HierarchyManager#isFolderNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isGroupNode(y.base.Node)
     * @see y.view.hierarchy.HierarchyManager#isNormalNode(y.base.Node)
     */
    protected void mouseDoubleClicked(double x, double y) {
      if (lastClickEvent.getClickCount() == 2) {
        HitInfo info = getHitInfo(x,y);
        Node node = info.getHitNode();
        if (node != null) {
          if (getGraph2D().getHierarchyManager().isGroupNode(node)) {
            getJTreeChart().collapseGroup(node);
          } else if (getGraph2D().getHierarchyManager().isFolderNode(node)) {
            getJTreeChart().expandGroup(node);
          } else {
            getJTreeChart().performNodeAction(node);
          }
        } else {
          getJTreeChart().fitContent(true);
        }
      }
    }
  }

  /**
   * Drawable implementation used by the NavigationMode Scroller. 
   * The appearance of the Scroller Drawable is zoom invariant. To accomplish this
   * it is drawn in view coordinate space.  
   */
  static class ScrollerDrawable extends ViewCoordDrawableAdapter {
    Scroller scroller;

    public ScrollerDrawable(Graph2DView view, Scroller scroller) {
      super(view, null);
      this.scroller = scroller;
    }

    protected void paintViewCoordinateDrawable(Graphics2D gfx) {
      gfx = (Graphics2D)gfx.create();
      YVector dir = scroller.getScrollDirection();
      YPoint p = scroller.getScrollStart();
      p = new YPoint(view.toViewCoordX(p.x), view.toViewCoordY(p.y));
      Ellipse2D circle = new Ellipse2D.Double(p.x-15, p.y-15,30,30);
      gfx.setColor(new Color(204,204,204,100));
      gfx.fill(circle);
      gfx.setColor(new Color(100,100,100,100));
      gfx.setStroke(LineType.LINE_1);
      AffineTransform trans = new AffineTransform(dir.getX(), dir.getY(),-dir.getY(), dir.getX(),p.x,p.y);
      GeneralPath arrow = new GeneralPath(GeneralPath.WIND_NON_ZERO,6);
      arrow.moveTo(15,0);
      arrow.lineTo(0,5);
      arrow.lineTo(0,-5);
      gfx.fill(trans.createTransformedShape(arrow));
      gfx.setStroke(LineType.LINE_2);
      gfx.draw(circle);
      gfx.dispose();
    }

    protected Rectangle getViewCoordinateDrawableBounds() {
      YPoint p = scroller.getScrollStart();
      p = new YPoint(view.toViewCoordX(p.x), view.toViewCoordY(p.y));
      return (new Rectangle2D.Double(p.x-20,p.y-20,40,40)).getBounds();
    }
  }


  /**
   * Creates a new view mode for interactively editing the tree structure.
   * Left-dragging a node to another node relocates the dragged node.
   * Additionally, left-clicking a node opens controls for 
   * hiding and un-hiding the node's children, relocating the node together
   * with some or all of its children, as well as deleting the node. 
   * @param view the tree chart for which the view mode is created.
   * @return a new {@link JTreeChartEditMode} instance.
   */
  static ViewMode newEditMode( final JTreeChart view ) {
    final JTreeChartEditMode editMode = new JTreeChartEditMode(new HoverButton(view));
    editMode.getMouseInputMode().setDrawableSearchingEnabled(true);
    editMode.allowNodeCreation(false);
    editMode.allowResizeNodes(false);
    editMode.allowEdgeCreation(false);
    return editMode;
  }

  /**
   * <code>EditMode</code> that provides custom mouse single  click handling as
   * well as custom mouse drag handling.
   * <p>
   * Single clicking a node representing an employee (and not a business unit)
   * will display a set of controls for re-assigning or deleting said employee,
   * for adding a subordinate employee, and for collapsing or expanding all
   * of the employee's subordinates or superiors.
   * </p><p>
   * Double clicking a node representing business data will invoke
   * {@link JTreeChart#performNodeAction(y.base.Node)} for that node and double
   * clicking anything but a node will trigger an animated fit content operation
   * (see {@link demo.view.orgchart.JTreeChart#fitContent(boolean)}).
   * </p><p>
   * Dragging a node representing an employee will re-assign the employee to a
   * new superior.
   * </p><p>
   * Note, this implementation assumes that no business units are displayed.
   * (I.e. there are no group nodes in the displayed graph.)
   * </p>
   */
  public static class JTreeChartEditMode extends EditMode {
    private final HoverButton hoverButton;
    private long lastClickWhen;

    public JTreeChartEditMode( final HoverButton hoverButton ) {
      this.hoverButton = hoverButton;
    }

    public JTreeChart getJTreeChart() {
      return (JTreeChart) view;
    }

    protected ViewMode createMoveSelectionMode() {
      return new JTreeChartMoveSelectionMode();
    }

    /**
     * Start moving a node.
     * Used by buttons to set moveSelectionMode active.
     * @param node node to move
     * @param moveMode the selected move mode
     */
    public void startMovement(final Node node, final int moveMode) {
      final Graph2D graph = getGraph2D();
      graph.setSelected(node, true);

      ((JTreeChartMoveSelectionMode) getMoveSelectionMode()).setMoveMode(moveMode);

      final NodeRealizer realizer = graph.getRealizer(node);
      final int x = view.toViewCoordX(realizer.getCenterX());
      final int y = view.toViewCoordY(realizer.getCenterY());
      setChild(moveSelectionMode, newPressedEvent(x, y), null);
    }

    private MouseEvent newPressedEvent( final int viewX, final int viewY ) {
      return new MouseEvent(
              view.getCanvasComponent(),
              MouseEvent.MOUSE_PRESSED,
              System.currentTimeMillis(),
              0, //modifiers
              viewX, viewY,
              0, //click count
              false,
              MouseEvent.BUTTON1);
    }

    /**
     * Disable multi selection box
     * @return null to disable multi selection box
     */
    public ViewMode getSelectionBoxMode() {
      return null;
    }

    /**
     * Check double clicking to collapse/expand group nodes.
     * @param x   the x-coordinate in the associated view's world coordinate
     * system.
     * @param y   the y-coordinate in the associated view's world coordinate
     * system.
     */
    public void mouseClicked(final double x, final double y) {
      final MouseEvent ce = lastClickEvent;
      if (ce.getClickCount() == 2) {
        final HitInfo info = getHitInfo(x, y);
        final Node node = info.getHitNode();
        if (node != null) {
          getJTreeChart().performNodeAction(node);
        } else {
          getJTreeChart().fitContent(true);
        }
      } else {
        // when one of HoverButton's move controls is clicked,
        // JTreeChartMoveSelectionMode consumes mousePressed and mouseReleased
        // and immediately returns control to JTreeChartEditMode
        // which means JTreeChartEditMode receives the click event
        // in this case, do not update HoverButton because the hitNode might
        // not be the currently selected node
        final MouseEvent pe = lastPressEvent;
        if (pe != null) {
          final long lastPressWhen = pe.getWhen();
          if (lastPressWhen > lastClickWhen) {
            hoverButton.setNode(getHitInfo(x, y).getHitNode());
            getJTreeChart().updateView();
          }
        }
      }
      lastClickWhen = ce.getWhen();
    }

    /**
     * Checks if this <code>JTreeChartEditMode</code> can be used with the
     * specified model.
     * @param model the model to check.
     * @return <code>true</code> if this <code>JTreeChartEditMode</code> can be
     * used with the specified model; <code>false</code> otherwise.
     */
    public static boolean isCompatibleModel( final TreeModel model ) {
      return model instanceof DefaultTreeModel;
    }
  }


  /**
   * <code>MoveSelectionMode</code> that provides mouse drag handling for
   * interactive changes of the displayed organization structure.
   * <p>
   * Note, this implementation assumes that no business units are displayed.
   * (I.e. there are no group nodes in the displayed graph.)
   * </p>
   */
  public static class JTreeChartMoveSelectionMode extends MoveSelectionMode {
    /**
     * Movement policy to move only a single selected node.
     */
    public static final int MOVE_MODE_SINGLE = 0;
    /**
     * Movement policy to move a selected node and all predecessors that are assistants.
     */
    public static final int MOVE_MODE_ASSISTANT = 1;
    /**
     * Movement policy to move the whole subtree.
     */
    public static final int MOVE_MODE_ALL = 2;


    private int moveMode;
    private GraphHider hider;
    private YPoint center;
    private NodeList nodesToBeMoved;

    public int getMoveMode() {
      return moveMode;
    }

    public void setMoveMode(final int moveMode) {
      this.moveMode = moveMode;
    }

    /**
     * Overwritten to automatically reassign employees to new business units
     * if business units are displayed.
     */
    public void mousePressedLeft( final double x, final double y ) {
      mouseShiftPressedLeft(x, y);
    }

    /**
     * Overwritten to calculate the nodes to be moved.
     */
    public void mouseShiftPressedLeft( final double x, final double y ) {
      initNodesToBeMoved(x, y);
      super.mouseShiftPressedLeft(x, y);
    }

    /**
     * Overwritten to automatically reassign employees to new business units
     * if business units are displayed.
     */
    public void mouseReleasedLeft( final double x, final double y ) {
      super.mouseShiftReleasedLeft(x, y);
    }

    /**
     * Caches the nodes to be moved.
     */
    private void initNodesToBeMoved( final double x, final double y ) {
      nodesToBeMoved = getNodesToBeMovedImpl(x, y);
    }

    /**
     * Calculates the nodes to be moved depending on the specified location
     * the current movement mode policy.
     * @see #getMoveMode()
     */
    private NodeList getNodesToBeMovedImpl( final double x, final double y ) {
      final HitInfo hitInfo = getHitInfo(x, y);
      final NodeList objects = new NodeList();
      if (hitInfo.hasHitNodes() && !getJTreeChart().isLocalViewEnabled()) {
        final Node hitNode = hitInfo.getHitNode();
        if (getHierarchyManager().isNormalNode(hitNode)) {
          final OrgChartTreeModel.Employee hitEmployee = getEmployee(hitNode);
          if (!hitEmployee.isRoot()) {
            if (hitEmployee.vacant) {
              return objects;
            }
            final int moveMode = getMoveMode();
            if (MOVE_MODE_ALL == moveMode) {
              final NodeList stack = new NodeList();
              stack.add(hitNode);
              while(!stack.isEmpty()) {
                final Node node = stack.popNode();
                objects.add(node);
                stack.addAll(node.successors());
              }
              return objects;
            } else if (MOVE_MODE_SINGLE == moveMode) {
              objects.add(hitNode);
              return objects;
            } else if (MOVE_MODE_ASSISTANT == moveMode) {
              objects.add(hitNode);
              for (NodeCursor nc = hitNode.successors(); nc.ok(); nc.next()) {
                final Node node = nc.node();
                final OrgChartTreeModel.Employee employee = getEmployee(node);
                if (employee.assistant) {
                  objects.add(node);
                }
              }
              return objects;
            }
          }
        }
      }
      return objects;
    }

    /**
     * Returns the cached nodes to be moved.
     * @return nodes to be moved.
     */
    protected NodeList getNodesToBeMoved() {
      return nodesToBeMoved;
    }

    /**
     * Returns bends of all edges connected to nodes returned by {@link #getNodesToBeMoved()}.
     * @return bends to be moved
     */
    protected BendList getBendsToBeMoved() {
      final BendList bends = new BendList();
      final Graph2D graph = getGraph2D();
      final int moveMode = getMoveMode();
      if (MOVE_MODE_ALL == moveMode) {
        for (NodeCursor nc = getNodesToBeMoved().nodes(); nc.ok(); nc.next()) {
          for (EdgeCursor ec = nc.node().outEdges(); ec.ok(); ec.next()) {
            bends.addAll(graph.getRealizer(ec.edge()).bends());
          }
        }
      } else if (MOVE_MODE_ASSISTANT == moveMode) {
        for (NodeCursor nc = getNodesToBeMoved().nodes(); nc.ok(); nc.next()) {
          final Node node = nc.node();
          final OrgChartTreeModel.Employee employee = getEmployee(node);
          if (employee.assistant) {
            for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
              bends.addAll(graph.getRealizer(ec.edge()).bends());
            }
          }
        }
      }
      return bends;
    }

    /**
     * Get nodes that will be marked, to show user where node will be added.
     * Returns one node, if node will be added as child of this node.
     * Returns many nodes, if node will be added as sibling to these nodes.
     * @return nodes to be marked
     */
    private NodeList nodesToBeMarked( final Graph2D graph, final Node subject ) {
      final NodeList result = new NodeList();

      final Node root = getJTreeChart().getRootNode();

      final Rectangle2D r = graph.getRealizer(subject).getBoundingBox();
      final NodeList queue = new NodeList();
      queue.add(root);
      while (!queue.isEmpty()) {
        final Node node = queue.popNode();
        queue.addAll(node.successors());

        if (node != subject) {
          final NodeRealizer nr = graph.getRealizer(node);
          if (r.intersects(nr.getX(), nr.getY(), nr.getWidth(), nr.getHeight())) {
            result.add(node);
          }
        }
      }

      if (result.size() > 1) {
        final Node first = result.firstNode();
        if (root == first) {
          result.clear();
          result.add(first);
        } else {
          boolean commonParent = true;
          final Node parent = first.firstInEdge().source();
          for (NodeCursor nc = result.nodes(); nc.ok(); nc.next()) {
            final Node node = nc.node();
            if (node.inDegree() < 1 || node.firstInEdge().source() != parent) {
              commonParent = false;
              break;
            }
          }

          if (!commonParent) {
            result.clear();
            result.add(first);
          }
        }
      }

      return result;
    }

    /**
     * Called by {@link MoveSelectionMode} when moving of node just started.
     * Shows nodes returned by {@link #nodesToBeMoved} atop other nodes
     * and hides edges connected between moved and unmoved nodes.
     * @param x mouse x-coordinate
     * @param y mouse y-coordinate
     */
    protected void selectionMoveStarted(final double x, final double y) {
      view.setDrawingMode(JTreeChart.NORMAL_MODE);
      final NodeMap atopMap = Maps.createHashedNodeMap();
      final NodeList nodesToBeMoved = getNodesToBeMoved();
      if (nodesToBeMoved.isEmpty()) {
        cancelEditing();
        return;
      }
      for (NodeCursor nc = nodesToBeMoved.nodes(); nc.ok(); nc.next()) {
        atopMap.setBool(nc.node(), true);
      }
      final Graph2D graph = getGraph2D();
      graph.addDataProvider(JTreeChart.ATOP_DPKEY, atopMap);

      final Node subject = nodesToBeMoved.firstNode();
      center = new YPoint(graph.getCenterX(subject), graph.getCenterY(subject));

      final GraphHider hider = new GraphHider(graph);
      hider.hide(subject.firstInEdge());
      final int moveMode = getMoveMode();
      if (MOVE_MODE_SINGLE == moveMode) {
        hider.hide(subject.outEdges());
      } else if(MOVE_MODE_ASSISTANT == moveMode) {
        for (final EdgeCursor ec = subject.outEdges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          final OrgChartTreeModel.Employee employee = getEmployee(edge.target());
          if (!employee.assistant) {
            hider.hide(edge);
          }
        }
      }
      this.hider = hider;
    }

    /**
     * Called when node was moved.
     * Updates marked nodes.
     * @param dx the difference between the given x-coordinate and the
     * x-coordinate of the last mouse event handled by this mode.
     * @param dy the difference between the given y-coordinate and the
     * y-coordinate of the last mouse event handled by this mode.
     * @param x the x-coordinate of the triggering mouse event in the world
     * coordinate system.
     * @param y the y-coordinate of the triggering mouse event in the world
     */
    protected void selectionOnMove(final double dx, final double dy, final double x, final double y) {
      if (!isEditing()) {
        return;
      }

      final Graph2D graph = getGraph2D();
      final Node subject = getSubject();
      if (subject == null) {
        removeDataProvider(graph, JTreeChart.MARKED_NODES_DPKEY);
      } else {
        final DataMap map = Maps.createHashedNodeMap();
        final NodeList nodes = nodesToBeMarked(view.getGraph2D(), subject);
        for (NodeCursor nc = nodes.nodes(); nc.ok(); nc.next()) {
          map.setBool(nc.node(), true);
        }
        graph.addDataProvider(JTreeChart.MARKED_NODES_DPKEY, map);
      }
    }

    /**
     * Called after moving was finished.
     * Update the graph depending on where the node was placed
     * @param dx the difference between the given x-coordinate and the
     * x-coordinate of the last mouse event handled by this mode.
     * @param dy the difference between the given y-coordinate and the
     * y-coordinate of the last mouse event handled by this mode.
     * @param x the x-coordinate of the triggering mouse event in the world
     * coordinate system.
     * @param y the y-coordinate of the triggering mouse event in the world
     */
    protected void selectionMovedAction(final double dx, final double dy, final double x, final double y) {
      super.selectionMovedAction(dx, dy, x, y);

      if (!isEditing()) {
        return;
      }

      final Node subject = getSubject();
      if (subject != null) {
        hider.unhideEdges();
        final Graph2D graph = getGraph2D();
        removeDataProvider(graph, JTreeChart.ATOP_DPKEY);
        removeDataProvider(graph, JTreeChart.MARKED_NODES_DPKEY);

        final NodeList nodesToBeMarked = nodesToBeMarked(graph, subject);

        Node newParent = null;
        //determine new parent
        if (nodesToBeMarked.size() == 1) {
          newParent = nodesToBeMarked.popNode();
        } else if (nodesToBeMarked.size() > 1) {
          newParent = nodesToBeMarked.popNode().firstInEdge().source();
        }

        final Node oldParent = subject.firstInEdge().source();
        final int moveMode = getMoveMode();
        if (newParent != null) {
          final OrgChartTreeModel.Employee parentEmployee = getEmployee(newParent);
          //only change when parent changed
          if (newParent != oldParent || parentEmployee.vacant) {
            if (MOVE_MODE_ALL == moveMode ||
                (MOVE_MODE_SINGLE == moveMode && subject.outDegree() == 0)) {
              moveWholeSubtree(subject, graph, newParent);
            } else if (MOVE_MODE_SINGLE == moveMode) {
              moveLeavingVacant(subject, graph, newParent);
            } else if (MOVE_MODE_ASSISTANT == moveMode) {
              //Collect assistants
              final EdgeList assistantEdges = new EdgeList();
              final YList assistants = new YList();
              for (EdgeCursor ec = subject.outEdges(); ec.ok(); ec.next()) {
                final Edge edge = ec.edge();
                final OrgChartTreeModel.Employee employee = getEmployee(edge.target());
                if (employee != null && employee.assistant) {
                  assistantEdges.add(edge);
                  assistants.add(employee);
                }
              }
              if (assistantEdges.size() < subject.outDegree()) {
                //employee has subordinates that are not assistants
                //-> move only assistants with him
                final Node node = moveLeavingVacant(subject, graph, newParent);
                final OrgChartTreeModel.Employee nodeEmployee = getEmployee(node);
                if (node != newParent) {
                  nodeEmployee.removeAllChildren();
                }

                changeBusinessUnit(assistants, nodeEmployee.businessUnit);

                final DataMap comparableEdgeMap = (DataMap) graph.getDataProvider(
                        NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY);
                final DefaultTreeModel treeModel = getMutableTreeModel();
                for (EdgeCursor ec = assistantEdges.edges(); ec.ok(); ec.next()) {
                  final Edge edge = ec.edge();
                  final Node child = edge.target();
                  final Edge newEdge = graph.createEdge(node, child);
                  graph.removeEdge(edge);
                  comparableEdgeMap.set(newEdge, new Integer(newEdge.index()));

                  //transfer assistants to employee
                  treeModel.insertNodeInto(
                          getEmployee(child),
                          nodeEmployee,
                          nodeEmployee.getChildCount());
                }
              } else {
                moveWholeSubtree(subject, graph, newParent);
              }
            }
          }
        }

        setMoveMode(MOVE_MODE_SINGLE);

        getJTreeChart().layoutGraph(true);
      }
    }

    /**
     * Returns the node that represents the employee that is actually moved.
     * @return the node that represents the employee that is actually moved.
     */
    private Node getSubject() {
      final NodeList list = nodesToBeMoved;
      return list == null || list.isEmpty() ? null : list.firstNode();
    }

    /**
     * Whole subtree was moved, so only the parent of the subtrees root must be changed.
     * @param hitNode node that was moved
     * @param graph2D current <code>Graph2D</code>
     * @param newParent new parent for <code>hitNode</code>
     */
    private void moveWholeSubtree(final Node hitNode, final Graph2D graph2D, final Node newParent) {
      final OrgChartTreeModel.Employee parentEmployee = getEmployee(newParent);
      final DataMap comparableEdgeMap = (DataMap) graph2D.getDataProvider(
              NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY);
      final DefaultTreeModel treeModel = getMutableTreeModel();

      final YList movedEmployees = new YList();
      for (NodeCursor nc = getNodesToBeMoved().nodes(); nc.ok(); nc.next()) {
        movedEmployees.add(getEmployee(nc.node()));
      }

      final OrgChartTreeModel.Employee employee = getEmployee(hitNode);
      if (parentEmployee.vacant) {
        employee.adoptStructuralData(parentEmployee);

        //modify graph
        final GenericNodeRealizer gnr = (GenericNodeRealizer) graph2D.getRealizer(newParent);
        gnr.setUserData(employee);
        getJTreeChart().configureNodeRealizer(newParent);

        for (final EdgeCursor ec = hitNode.outEdges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          final Edge newEdge = graph2D.createEdge(newParent, edge.target());
          comparableEdgeMap.set(newEdge,new Integer(newEdge.index()));
          graph2D.removeEdge(edge);
        }

        //Modify tree structure
        getJTreeChart().updateUserObject(newParent, employee);
        final MutableTreeNode grandfather = (MutableTreeNode) parentEmployee.getParent();
        if (grandfather != null) {
          treeModel.insertNodeInto(employee, grandfather,grandfather.getIndex(parentEmployee));
        }

        //transfer children to employee
        while(parentEmployee.getChildCount() > 0) {
          treeModel.insertNodeInto((MutableTreeNode) parentEmployee.getChildAt(0), employee, employee.getChildCount());
        }
        if (!parentEmployee.isRoot()) {
          treeModel.removeNodeFromParent(parentEmployee);
        }
        gnr.setUserData(employee);
        final boolean state = graph2D.isSelected(hitNode);
        graph2D.removeNode(hitNode);
        graph2D.setSelected(newParent, state);
        getJTreeChart().configureNodeRealizer(newParent);
      } else {
        //change parent
        graph2D.removeEdge(hitNode.firstInEdge());
        final Edge edge = graph2D.createEdge(newParent, hitNode);
        comparableEdgeMap.set(edge, new Integer(edge.index()));
        treeModel.removeNodeFromParent(employee);
        treeModel.insertNodeInto(employee, parentEmployee, parentEmployee.getChildCount());
        employee.businessUnit = parentEmployee.businessUnit;
      }
      changeBusinessUnit(movedEmployees, employee.businessUnit);
    }

    /**
     * Changes the business unit of the given employees to the specified new
     * business unit.
     * @param employees a list of {@link OrgChartTreeModel.Employee} instances.
     * @param businessUnit the new business unit.
     */
    private void changeBusinessUnit( final YList employees, final String businessUnit ) {
      for (ListCell lc = employees.firstCell(); lc != null; lc = lc.succ()) {
        final OrgChartTreeModel.Employee employee = (OrgChartTreeModel.Employee) lc.getInfo();
        employee.businessUnit = businessUnit;
      }
    }

    /**
     * Move node, leaving a vacant position.
     * @param hitNode node that was moved
     * @param graph2D current <code>Graph2D</code>
     * @param newParent new parent for <code>hitNode</code>
     * @return the node, where hitNode was moved to. Either a new node or <code>newParent</code>, if node
     * was moved to a vacant position
     */
    private Node moveLeavingVacant(final Node hitNode, final Graph2D graph2D, final Node newParent) {
      final DataMap comparableEdgeMap = (DataMap) graph2D.getDataProvider(
              NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY);
      final DataMap comparableNodeMap = (DataMap) graph2D.getDataProvider(
              NormalizingGraphElementOrderStage.COMPARABLE_NODE_DPKEY);
      final OrgChartTreeModel.Employee parentEmployee = getEmployee(newParent);

      final Node node;
      final OrgChartTreeModel.Employee employee = getEmployee(hitNode);
      final OrgChartTreeModel.Employee newEmployee = (OrgChartTreeModel.Employee) employee.clone();
      employee.vacate();
      getJTreeChart().configureNodeRealizer(hitNode);
      newEmployee.businessUnit = parentEmployee.businessUnit;

      final DefaultTreeModel treeModel = getMutableTreeModel();
      if (parentEmployee.vacant) {
        node = newParent;
        final MutableTreeNode grandfather = (MutableTreeNode) parentEmployee.getParent();
        if (grandfather != null) {
          treeModel.insertNodeInto(newEmployee, grandfather, grandfather.getIndex(parentEmployee));
        }
        while(parentEmployee.getChildCount() > 0) {
          treeModel.insertNodeInto((MutableTreeNode) parentEmployee.getChildAt(0), newEmployee,
                                   newEmployee.getChildCount());
        }
        if (!parentEmployee.isRoot()) {
          treeModel.removeNodeFromParent(parentEmployee);
        }
        newEmployee.adoptStructuralData(parentEmployee);
      } else {
        node = graph2D.createNode();
        final Edge edge = graph2D.createEdge(newParent, node);
        comparableEdgeMap.set(edge, new Integer(edge.index()));
        comparableNodeMap.set(node, new Integer(node.index()));
        treeModel.insertNodeInto(newEmployee,parentEmployee,parentEmployee.getChildCount());
      }
      final GenericNodeRealizer gnr = (GenericNodeRealizer) graph2D.getRealizer(node);
      gnr.setCenter(graph2D.getCenterX(hitNode), graph2D.getCenterY(hitNode));
      graph2D.setCenter(hitNode, center);
      gnr.setUserData(newEmployee);
      getJTreeChart().updateUserObject(node, newEmployee);
      graph2D.firePreEvent();
      graph2D.setSelected(hitNode, false);
      graph2D.setSelected(node, true);
      graph2D.firePostEvent();
      getJTreeChart().configureNodeRealizer(node);
      return node;
    }

    /**
     * Overwritten to free resources.
     */
    public void reactivateParent() {
      hider = null;
      center = null;
      nodesToBeMoved = null;
      super.reactivateParent();
    }

    /**
     * Removes the {@link y.base.DataProvider} instances registered for the
     * specified key from the given graph.
     * @param graph the graph from which to remove a {@link y.base.DataProvider}
     * instance.
     * @param key the key for which the {@link y.base.DataProvider} instance was
     * registered.
     */
    private void removeDataProvider( final Graph2D graph, final Object key ) {
      if (graph.getDataProvider(key) != null) {
        graph.removeDataProvider(key);
      }
    }

    public JTreeChart getJTreeChart() {
      return (JTreeChart) view;
    }

    public DefaultTreeModel getMutableTreeModel() {
      return (DefaultTreeModel) getJTreeChart().getModel();
    }

    /**
     * Retrieves the {@link OrgChartTreeModel.Employee} instance represented by
     * the given node.
     * @return the {@link OrgChartTreeModel.Employee} instance represented by
     * the given node.
     */
    private OrgChartTreeModel.Employee getEmployee( final Node hitNode ) {
      return (OrgChartTreeModel.Employee) getJTreeChart().getUserObject(hitNode);
    }
  }
}
