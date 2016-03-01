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

import demo.view.DemoBase;
import demo.view.orgchart.ViewModeFactory.JTreeChartMoveSelectionMode;
import demo.view.orgchart.ViewModeFactory.JTreeChartEditMode;
import y.algo.GraphConnectivity;
import y.algo.Paths;
import y.algo.Trees;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.Geom;
import y.layout.NormalizingGraphElementOrderStage;
import y.util.Maps;
import y.view.AbstractMouseInputEditor;
import y.view.Drawable;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.MouseInputEditorProvider;
import y.view.NodeRealizer;
import y.view.ViewMode;
import y.view.hierarchy.HierarchyManager;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Shows and hides controls for re-assigning or deleting an employee,
 * for adding a subordinate employee, and for collapsing or expanding all
 * subordinates or superiors.
 * <p>
 * Note, all corresponding implementations assume that no business units
 * are displayed. (I.e. there are no group nodes in the displayed graph.)
 * </p>
 */
public class HoverButton implements MouseInputEditorProvider, Drawable {
  static final int VERTICAL_CONTROL_OFFSET = 25;


  private Node node;
  private final List buttons;
  private final OrgChartTreeModel treeModel;
  private final JTreeChart treeChart;
  private final NodeMap hiddenNodesChild;
  private final NodeMap hiddenEdgesChild;
  private final NodeMap hiddenNodesParent;
  private final NodeMap hiddenEdgesParent;

  public HoverButton( final JTreeChart treeChart ) {
    this.treeChart = treeChart;
    this.treeModel = (OrgChartTreeModel) treeChart.getModel();
    buttons = new ArrayList();
    hiddenNodesChild = Maps.createHashedNodeMap();
    hiddenEdgesChild = Maps.createHashedNodeMap();
    hiddenNodesParent = Maps.createHashedNodeMap();
    hiddenEdgesParent = Maps.createHashedNodeMap();
    buttons.add(new MoveButton("move.png", "move_disabled.png", 0, JTreeChartMoveSelectionMode.MOVE_MODE_SINGLE));
    buttons.add(new MoveButton("move_plus.png", "move_plus_disabled.png", 1, JTreeChartMoveSelectionMode.MOVE_MODE_ASSISTANT));
    buttons.add(new MoveButton("move_star.png", "move_star_disabled.png", 2, JTreeChartMoveSelectionMode.MOVE_MODE_ALL));
    buttons.add(new ExpandButton("down.png", "down_disabled.png", -1, false));
    buttons.add(new CollapseButton("up.png", "up_disabled.png", -1, false));
    buttons.add(new AddButton("plus.png", null, 3));
    buttons.add(new DeleteButton("trash.png", "trash_disabled.png", 4));
    buttons.add(new ExpandButton("down.png", "down_disabled.png", 5, true));
    buttons.add(new CollapseButton("up.png", "up_disabled.png", 5, true));
    treeChart.addDrawable(this);
  }

  public MouseInputEditor findMouseInputEditor(final Graph2DView view, final double x, final double y, final HitInfo hitInfo) {
    if (isVisible()) {
      for (final Iterator iterator = buttons.iterator(); iterator.hasNext(); ) {
        final NodeButton nb = (NodeButton) iterator.next();
        if (nb.getBounds().contains(x, y)) {
          return nb;
        }
      }
    }
    return null;
  }

  private boolean isVisible() {
    return node != null;
  }

  public void paint(final Graphics2D g) {
    if (isVisible()) {
      if (node.getGraph() == null) {
        //Graph changed while HoverButton still shown
        node = null;
      } else {
        for (final Iterator iterator = buttons.iterator(); iterator.hasNext(); ) {
          final NodeButton nb = (NodeButton) iterator.next();
          nb.paint(g);
        }
      }
    }
  }

  public Rectangle getBounds() {
    final Rectangle r = new Rectangle(-1, -1, -1, -1);
    if (isVisible()) {
      for (final Iterator iterator = buttons.iterator(); iterator.hasNext(); ) {
        final NodeButton nb = (NodeButton) iterator.next();
        Geom.calcUnion(r, nb.getBounds(), r);
      }
    }
    return r;
  }

  /**
   * Returns the tool tip text describing the control at the specified location.
   * @param x the x-coordinates of the mouse position in world coordinates.
   * @param y the x-coordinates of the mouse position in world coordinates.
   * @return the tool tip text describing the control at the specified location.
   */
  public String getToolTipText( final double x, final double y ) {
    if (isVisible()) {
      for (Iterator it = buttons.iterator(); it.hasNext(); ) {
        final NodeButton nb = (NodeButton) it.next();
        if (nb.contains(x, y)) {
          return nb.getToolTipText();
        }
      }
    }
    return null;
  }

  /**
   * Sets a new node active and changes all preferences.
   * @param node the new node.
   */
  public void setNode(final Node node) {
    if (node == null) {
      this.node = node;
    } else {
      final HierarchyManager hm = treeChart.getGraph2D().getHierarchyManager();
      if (hm.isNormalNode(node)) {
        this.node = node;
      } else {
        this.node = null;
      }
    }
  }

  static ImageIcon newIcon( final String path ) {
    return path == null ? null : new ImageIcon(DemoBase.getResource(HoverButton.class, "resources/icons/" + path));
  }


  /**
   * Button for adding a new employee.
   */
  private class AddButton extends NodeButton {
    AddButton(final String enabled, final String disabled, final int position) {
      super(enabled, disabled, position);
    }

    /**
     * Add a new employee as a child of current node.
     */
    void action() {
      final Graph2D graph = treeChart.getGraph2D();
      final DataMap comparableEdgeMap = (DataMap) graph.getDataProvider(
          NormalizingGraphElementOrderStage.COMPARABLE_EDGE_DPKEY);
      final DataMap comparableNodeMap = (DataMap) graph.getDataProvider(
          NormalizingGraphElementOrderStage.COMPARABLE_NODE_DPKEY);
      final DataMap graph2Tree = (DataMap) graph.getDataProvider(JTreeChart.GRAPH_2_TREE_MAP_DPKEY);
      final DataMap tree2Graph = (DataMap) graph.getDataProvider(JTreeChart.TREE_2_GRAPH_MAP_DPKEY);

      final GenericNodeRealizer gnr = (GenericNodeRealizer) graph.getRealizer(node);
      final OrgChartTreeModel.Employee employee = (OrgChartTreeModel.Employee) gnr.getUserData();
      final OrgChartTreeModel.Employee newEmployee = new OrgChartTreeModel.Employee();

      newEmployee.icon = employee.icon;
      newEmployee.businessUnit = employee.businessUnit;
      newEmployee.status = employee.status;
      treeModel.insertNodeInto(newEmployee, employee, employee.getChildCount());

      final Node newNode = graph.createNode();
      graph.setCenter(newNode, gnr.getCenterX(), gnr.getCenterY());
      final Edge edge = graph.createEdge(node, newNode);
      graph2Tree.set(newNode, newEmployee);
      tree2Graph.set(newEmployee,newNode);
      comparableEdgeMap.set(edge, new Integer(edge.index()));
      comparableNodeMap.set(newNode, new Integer(node.index()));

      final HierarchyManager hm = graph.getHierarchyManager();
      final Node businessUnit = hm.getParentNode(node);
      if (businessUnit != null) {
        hm.setParentNode(newNode, businessUnit);
      }

      setNode(null);

      graph.firePreEvent();
      graph.unselectAll();
      graph.setSelected(newNode, true);
      graph.firePostEvent();

      treeChart.configureNodeRealizer(newNode);
      treeChart.layoutGraph(true);
    }

    /**
     * Check if button should be active.
     * @return always true, as a child could be added to every employee
     */
    boolean isActive() {
      return true;
    }

    String getToolTipText() {
      return "Adds a new subordinate employee for the selected employee.";
    }
  }

  /**
   * Button for deleting an employee.
   */
  private class DeleteButton extends NodeButton {
    DeleteButton(final String enabled, final String disabled, final int position) {
      super(enabled, disabled, position);
    }

    /**
     * Delete current node.
     */
    void action() {
      final Graph2D graph = treeChart.getGraph2D();
      final GenericNodeRealizer gnr = (GenericNodeRealizer) graph.getRealizer(node);
      final OrgChartTreeModel.Employee employee = (OrgChartTreeModel.Employee) gnr.getUserData();
      if (!employee.isRoot()) {
        if (node.outDegree() > 0) {
          employee.vacate();
          treeChart.configureNodeRealizer(node);
          // hack to update properties table because there is no property
          // change support for employees
          final boolean state = graph.isSelected(node);
          graph.setSelected(node, !state);
          graph.setSelected(node, state);
        } else {
          // IMPORTANT:
          // remove the user object from the tree model first otherwise
          // JOrgChart's tree selection listener trigger a global rebuild
          treeModel.removeNodeFromParent(employee);
          graph.removeNode(node);
        }
        setNode(null);
        treeChart.layoutGraph(true);
      }
    }

    String getToolTipText() {
      return "Removes the selected employee.";
    }
  }

  /**
   * Button for moving a node.
   */
  private class MoveButton extends NodeButton {
    private final int moveMode;

    MoveButton(final String enabled, final String disabled, final int position, final int moveMode) {
      super(enabled, disabled, position);
      this.moveMode = moveMode;
    }

    /**
     * Start the movement.
     * Search for moveViewMode and trigger movement start.
     */
    void action() {
      final Iterator it = treeChart.getViewModes();
      if (it.hasNext()) {
        // the first registered view mode is a dummy that does nothing by itself
        // but serves as convenient way to switch between edit mode and
        // navigation mode
        final ViewMode masterMode = (ViewMode) it.next();
        final ViewMode activeMode = masterMode.getChild();
        if (activeMode instanceof JTreeChartEditMode) {
          final JTreeChartEditMode editMode = (JTreeChartEditMode) activeMode;
          editMode.startMovement(node, moveMode);
          treeChart.updateView();
        }
      }
      setNode(null);
    }

    boolean isActive() {
      return super.isActive() && !treeChart.isLocalViewEnabled() &&
             !super.isExpandable(true) && !super.isExpandable(false);
    }

    boolean acceptEvent( final Mouse2DEvent event ) {
      return event.getId() == Mouse2DEvent.MOUSE_PRESSED &&
             event.getButton() == 1;
    }

    String getToolTipText() {
      switch (moveMode) {
        case JTreeChartMoveSelectionMode.MOVE_MODE_SINGLE:
          return "Moves the selected employee.";
        case JTreeChartMoveSelectionMode.MOVE_MODE_ASSISTANT:
          return "Moves the selected employee and all of its assistants.";
        case JTreeChartMoveSelectionMode.MOVE_MODE_ALL:
          return "Moves the selected employee and all of its subordinate employees.";
        default:
          throw new IllegalStateException("Invalid movement mode: " + moveMode);
      }
    }
  }

  /**
   * Button for expanding children or parents.
   * Children and parents are expanded layer by layer.
   */
  private class ExpandButton extends NodeButton {
    private final boolean expandChildren;

    ExpandButton(final String enabled, final String disabled, final int position, final boolean expandChildren) {
      super(enabled, disabled, position);
      yOffset = expandChildren ? VERTICAL_CONTROL_OFFSET : 0;
      this.expandChildren = expandChildren;
    }

    public Rectangle getBounds() {
      final Rectangle bounds = super.getBounds();
      bounds.width *= 0.5;
      bounds.height *= 0.5;
      return bounds;
    }

    /**
     * Expand the next layer.
     */
    void action() {
      final Graph2D graph2D = treeChart.getGraph2D();
      if (expandChildren) {
        int depth = graph2D.N();
        if (hiddenNodesChild.get(node) != null) {
          //node itself is collapsed => expand here
          depth = 0;
        } else {
          //otherwise determine lowest depth where nodes are collapsed
          final NodeList successors = GraphConnectivity.getSuccessors(graph2D, new NodeList(node), depth);
          for (final NodeList leaves = Trees.getLeafNodes(graph2D, true);!leaves.isEmpty();) {
            final Node n = leaves.popNode();
            //is leaf of nodes subtree and has hidden nodes
            if (successors.contains(n) && hiddenNodesChild.get(n) != null) {
              depth = Math.min(depth, Paths.findPath(graph2D,node,n,false).size());
            }
          }
        }
        expandAtDepth(node, depth);
      } else {
        //search for farthest ancestor
        Node currentNode = node;
        while(currentNode.inDegree() >0) {
          currentNode = currentNode.firstInEdge().source();
        }
        //show hidden parent and children, if there is any
        final NodeList nodes = (NodeList) hiddenNodesParent.get(currentNode);
        if (nodes != null) {
          while(!nodes.isEmpty()) {
            graph2D.reInsertNode(nodes.popNode());
          }
          final EdgeList edges = (EdgeList) hiddenEdgesParent.get(currentNode);
          while(!edges.isEmpty()) {
            graph2D.reInsertEdge(edges.popEdge());
          }
          hiddenNodesParent.set(currentNode,null);
          hiddenEdgesParent.set(currentNode,null);
        }
      }
      treeChart.layoutGraph(true);
    }

    /**
     * Expands children at a given depth.
     * @param node node where to start
     * @param depth the given depth
     */
    private void expandAtDepth(final Node node, final int depth) {
      if (depth == 0) {
        //reached destiny, show collapsed children
        final NodeList nodes = (NodeList) hiddenNodesChild.get(node);
        if (nodes != null) {
          while(!nodes.isEmpty()) {
            treeChart.getGraph2D().reInsertNode(nodes.popNode());
          }
          for (final EdgeList edges = (EdgeList) hiddenEdgesChild.get(node);!edges.isEmpty();) {
            treeChart.getGraph2D().reInsertEdge(edges.popEdge());
          }
          hiddenNodesChild.set(node,null);
          hiddenEdgesChild.set(node,null);
          treeChart.updateView();
        }
      } else if (depth > 0) {
        //not at the right depth => visit children
        for (final EdgeCursor edgeCursor = node.outEdges(); edgeCursor.ok(); edgeCursor.next()) {
          final Edge edge = edgeCursor.edge();
          expandAtDepth(edge.target(),depth-1);
        }
      }
    }

    /**
     * Checks if expand button should be active.
     * Button should be active, if it is possible to expand.
     * @return Is there any parent/child that is collapsed
     */
    boolean isActive() {
      return isExpandable(expandChildren);
    }

    String getToolTipText() {
      if (expandChildren) {
        return "Displays previously hidden subordinates of the selected employee.";
      } else {
        return "Displays previously hidden superiors of the selected employee.";
      }
    }
  }

  /**
   * Button for collapsing children or parents.
   * Children and parents are collapsed layer by layer
   */
  private class CollapseButton extends NodeButton {
    private final boolean collapseChildren;

    CollapseButton(final String enabled, final String disabled, final int position, final boolean collapseChildren) {
      super(enabled, disabled, position);
      yOffset = collapseChildren ? 0 : VERTICAL_CONTROL_OFFSET;
      this.collapseChildren = collapseChildren;
    }

    public Rectangle getBounds() {
      final Rectangle bounds = super.getBounds();
      bounds.width *= 0.5;
      bounds.height *= 0.5;
      return bounds;
    }

    /**
     * Collapses the next layer.
     */
    void action() {
      final Graph2D graph = treeChart.getGraph2D();

      if (collapseChildren) {
        final NodeMap map = Maps.createHashedNodeMap();
        Trees.getSubTreeDepths(graph, map);
        hideAtDepth(graph, node, map.getInt(node));
      } else {
        //collapse the farthest ancestor
        Node currentNode = node;
        Node lastNode = null;
        //search for the second farthest ancestor
        while (currentNode.inDegree() > 0) {
          lastNode = currentNode;
          currentNode = currentNode.firstInEdge().source();
        }
        //if node has an ancestor to collapse
        if (lastNode != null) {
          final NodeList hideNodes = new NodeList(currentNode.successors());
          final EdgeList hideEdges = new EdgeList();
          //hide ancestor and its subtrees that not contains current node
          hideNodes.remove(lastNode);
          hideNodes.addAll(GraphConnectivity.getSuccessors(graph, hideNodes, graph.N()));
          hideNodes.add(currentNode);
          for (NodeCursor nc = hideNodes.nodes(); nc.ok(); nc.next()) {
            final Node n = nc.node();
            hideEdges.addAll(n.edges());
            graph.removeNode(n);
          }
          hiddenEdgesParent.set(lastNode, hideEdges);
          hiddenNodesParent.set(lastNode, hideNodes);
        }
      }
      treeChart.layoutGraph(true);
    }

    /**
     * Collapses children at a given depth.
     * @param root node where to start
     * @param depth depth where to collapse
     */
    private void hideAtDepth( final Graph2D graph, final Node root, final int depth ) {
      if (depth == 2) {
        //if depth 2 is reach, children should be collapsed
        final EdgeList edgesToHide = new EdgeList();
        final NodeList nodesToHide = new NodeList(root.successors());

        for (NodeCursor nc = nodesToHide.nodes(); nc.ok(); nc.next()) {
          final Node child = nc.node();
          edgesToHide.add(child.firstInEdge());
          graph.removeNode(child);
        }

        final EdgeList oldHiddenEdges = (EdgeList) hiddenEdgesChild.get(root);
        if (oldHiddenEdges != null) {
          edgesToHide.splice(oldHiddenEdges);
        }
        hiddenEdgesChild.set(root, edgesToHide);
        final NodeList oldHiddenNodes = (NodeList) hiddenNodesChild.get(root);
        if (oldHiddenNodes != null) {
          nodesToHide.splice(oldHiddenNodes);
        }
        hiddenNodesChild.set(root, nodesToHide);
      } else if (depth > 2) {
        //if depth is greater than 2, visit children
        for (NodeCursor nc = root.successors(); nc.ok(); nc.next()) {
          hideAtDepth(graph, nc.node(), depth - 1);
        }
      }
    }

    /**
     * Checks if collapse button should be active.
     * Collapse button should be active, if there are nodes to hide.
     * @return is in/out degree greater than zero
     */
    boolean isActive() {
      return (collapseChildren ? node.outDegree() > 0 : node.inDegree() > 0);
    }

    String getToolTipText() {
      if (collapseChildren) {
        return "Hides subordinates of the selected employee.";
      } else {
        return "Hides superiors of the selected employee.";
      }
    }
  }

  /**
   * Abstract class that provides button functionality.
   */
  private abstract class NodeButton extends AbstractMouseInputEditor {
    private static final int BUTTON_RADIUS = 50;

    int xOffset;
    int yOffset;
    final Icon icon;
    final Icon iconDisabled;

    NodeButton(final String enabled, final String disabled, final int position) {
      this.icon = newIcon(enabled);
      this.iconDisabled = newIcon(disabled);
      if (position == -1) {
        xOffset = (int) ((position-1.6) * BUTTON_RADIUS);
      } else {
        xOffset = (position-2) * BUTTON_RADIUS;
      }
    }

    /**
     * Paints button zoom invariant.
     * @param g current <code>Graphics2D</code>
     */
    public void paint(Graphics2D g) {
      final Rectangle bounds = getBounds();
      if (icon != null) {
        //Make buttons zoom invariant
        g = (Graphics2D) g.create();
        g.translate(bounds.x, bounds.y);
        final double z2 = 1 / treeChart.getZoom();
        g.scale(z2, z2);
        if (isActive()) {
          icon.paintIcon(treeChart.getRootPane(), g, 0, 0);
        } else {
          iconDisabled.paintIcon(treeChart.getRootPane(), g, 0, 0);
        }
        g.dispose();
      }
    }

    /**
     * Returns button bounds.
     * @return button bounds
     */
    public Rectangle getBounds() {
      final Rectangle r = new Rectangle(-1, -1, -1, -1);
      if (node != null) {
        final NodeRealizer realizer = treeChart.getGraph2D().getRealizer(node);
        final double z2 = 1 / treeChart.getZoom();
        r.x = (int) (realizer.getCenterX() + z2 * (xOffset - BUTTON_RADIUS * 0.5));
        r.y = (int) (realizer.getY() + realizer.getHeight() + z2 * (10 + yOffset));
        r.width = (int) (BUTTON_RADIUS * z2);
        r.height = (int) (BUTTON_RADIUS * z2);
      }
      return r;
    }

    /**
     * Gets called when the button was pressed. Must be overwritten by subclasses.
     */
    void action() {
    }

    /**
     * Determines if the action of the button is possible for this node.
     * Subclasses may want to change the behaviour.
     * @return if node is not root
     */
    boolean isActive() {
      final GenericNodeRealizer gnr = (GenericNodeRealizer) treeChart.getGraph2D().getRealizer(node);
      final OrgChartTreeModel.Employee employee = (OrgChartTreeModel.Employee) gnr.getUserData();
      return !employee.isRoot();
    }

    /**
     * Determines whether or not this button's associated <code>node</code>
     * has collapsed successors or predecessors that may be expanded.
     * @param successors if <code>true</code>, this method determines if there
     * are collapsed successors; otherwise this method determines if there
     * are collapsed predecessors.
     */
    boolean isExpandable( final boolean successors ) {
      //check if node itself has children/parents to expand
      final NodeMap hiddenNodes = successors
              ? hiddenNodesChild : hiddenNodesParent;
      if (hiddenNodes.get(node) != null) {
        return true;
      }

      //check if successors/predecessors have children to expand
      final Graph2D graph = treeChart.getGraph2D();
      final NodeList nl = new NodeList(node);
      final NodeList neighbors = successors
              ? GraphConnectivity.getSuccessors(graph, nl, graph.N())
              : GraphConnectivity.getPredecessors(graph, nl, graph.N());
      for (NodeCursor nc = neighbors.nodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        if (hiddenNodes.get(n) != null) {
          return true;
        }
      }

      return false;
    }

    public boolean startsEditing(final Mouse2DEvent event) {
      return contains(event.getX(), event.getY());
    }

    public void mouse2DEventHappened(final Mouse2DEvent event) {
      if (contains(event.getX(), event.getY())) {
        final String text = "You're over it";
        treeChart.setToolTipText(text);
        if (acceptEvent(event) && isActive()) {
          action();
        }
      } else {
        stopEditing();
      }
    }

    boolean acceptEvent( final Mouse2DEvent event ) {
      return event.getId() == Mouse2DEvent.MOUSE_CLICKED &&
             event.getButton() == 1;
    }

    boolean contains( final double x, final double y ) {
      return getBounds().contains(x, y);
    }

    String getToolTipText() {
      return null;
    }
  }
}
