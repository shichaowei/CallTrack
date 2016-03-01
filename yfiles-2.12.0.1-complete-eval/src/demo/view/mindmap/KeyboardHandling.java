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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.ViewMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * This class provides several actions for keyboard interaction and
 * a method to register the actions
 */
class KeyboardHandling {
  /**
   * Prevent instantiation of utility class.
   */
  private KeyboardHandling() {
  }


  static void setKeyActions( final Graph2DView view ) {
    final ActionMap actionMap = view.getActionMap();
    final InputMap inputMap = view.getInputMap();
    //register actions
    actionMap.put("INSERT_CHILD", new AddChildAction(view));
    actionMap.put("INSERT_SIBLING", new AddSiblingAction(view));
    actionMap.put("RIGHT", new CursorAction(CursorAction.RIGHT, view));
    actionMap.put("LEFT", new CursorAction(CursorAction.LEFT, view));
    actionMap.put("UP", new CursorAction(CursorAction.UP, view));
    actionMap.put("DOWN", new CursorAction(CursorAction.DOWN, view));
    actionMap.put("DELETE", new DeleteSelection(view.getGraph2D()));
    actionMap.put("PLUS", new CollapseExpandAction(true, view.getGraph2D()));
    actionMap.put("MINUS", new CollapseExpandAction(false, view.getGraph2D()));
    actionMap.put("EDIT", new EditAction(view));
    //connect keys and registered actions
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "INSERT_CHILD");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "INSERT_SIBLING");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0),"DELETE");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "PLUS");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0), "PLUS");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "MINUS");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "MINUS");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "EDIT");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0), "EDIT");
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, inputMap);
    view.getCanvasComponent().setActionMap(actionMap);
  }

  /**
   * Used by {@see MindMapDemo.createDeleteSelectionAction} to register the delete action for the toolbar.
   * @param view the current Graph2DView
   * @return {@link DeleteSelection}
   */
  static Action createDeleteSelectionAction( final Graph2DView view ) {
    final DeleteSelection ds = new DeleteSelection(view.getGraph2D());
    final ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.DELETE_SELECTION, ds);
    return ds;
  }

  private static class DeleteSelection extends AbstractAction {
    private final Graph2D graph2D;

    public DeleteSelection( final Graph2D graph2D ) {
      super("Delete Selection");
      this.graph2D = graph2D;
      this.putValue(Action.SMALL_ICON, MindMapUtil.getIcon("delete.png"));
      this.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    }

    public void actionPerformed(final ActionEvent e) {
      //Deletion of cross edges
      if (graph2D.selectedEdges().size() > 0) {
        final Edge edge = graph2D.selectedEdges().edge();
        if (ViewModel.instance.isCrossReference(edge)) {
          graph2D.removeEdge(edge);
          graph2D.getCurrentView().updateView();
        }
      //Deletion of items, except the center item
      } else if (!graph2D.isSelectionEmpty()) {
        final Node node = graph2D.selectedNodes().node();
        if (!ViewModel.instance.isRoot(node)) {
          graph2D.firePreEvent();
          final Edge inEdge = MindMapUtil.inEdge(node);
          MindMapUtil.removeSubtree(graph2D, node);
          if (inEdge != null) {
            graph2D.setSelected(inEdge.source(), true);
          }
          LayoutUtil.layout(graph2D);
          graph2D.firePostEvent();
        }
      }
    }
  }

  /**
   * Edits labels of selected nodes and selected cross-reference edges.
   */
  private static class EditAction extends Graph2DViewActions.EditLabelAction {
    EditAction( final Graph2DView view ) {
      super(view);
    }

    /**
     * Determines the label instance to edit.
     * @param view the view whose graph is searched for a label to edit.
     * @return the label instance to edit.
     */
    protected YLabel findLabel( final Graph2DView view ) {
      final Graph2D graph = view.getGraph2D();
      final NodeCursor nc = graph.selectedNodes();
      if (nc.ok()) {
        return graph.getRealizer(nc.node()).getLabel();
      } else {
        for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (ViewModel.instance.isCrossReference(edge)) {
            return graph.getRealizer(edge).getLabel();
          }
        }
      }
      return null;
    }

    /**
     * Sets the text of the specified label.
     * In case of a node label, the size of the corresponding node is adjusted
     * to match the new label text and a new layout is calculated for the mind
     * map.
     * @param label the label whose text content is set.
     * @param text the new text content.
     */
    protected void setText( final YLabel label, final String text ) {
      if (label instanceof NodeLabel) {
        final NodeLabel nl = (NodeLabel) label;
        final Graph2D graph = nl.getGraph2D();
        // backupRealizers is necessary here to ensure undo is working properly
        // (the latter call to MindMapUtil.layout may change properties of
        // *all* node and edge realizers)
        graph.backupRealizers();
        nl.setText(text);
        MindMapUtil.updateWidth(graph, nl.getNode());
        LayoutUtil.layout(graph);
      } else {
        label.setText(text);
      }
    }
  }

  /**
   * Edits labels when double-clicking on nodes, cross-reference edges, and/or
   * labels.
   */
  static class LabelChangeViewMode extends ViewMode {
    /**
     * Starts label editing for double-clicks on nodes, cross-reference edges,
     * and/or labels.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseClicked( final double x, final double y ) {
      if (lastClickEvent != null && lastClickEvent.getClickCount() == 2) {
        final HitInfo hitInfo = getHitInfo(x, y);
        if (hitInfo.hasHitNodeLabels()) {
          editLabel(hitInfo.getHitNodeLabel());
        } else if (hitInfo.hasHitNodes()) {
          final Graph2D graph2D = getGraph2D();
          editLabel(graph2D.getRealizer(hitInfo.getHitNode()).getLabel());
        } else if (hitInfo.hasHitEdges()) {
          final Edge edge = hitInfo.getHitEdge();
          // only cross-reference edges should have labels
          // (to explain why there is a connection)
          if (ViewModel.instance.isCrossReference(edge)) {
            final Graph2D graph2D = getGraph2D();
            editLabel(graph2D.getRealizer(edge).getLabel());
          }
        } else if (hitInfo.hasHitEdgeLabels()) {
          editLabel(hitInfo.getHitEdgeLabel());
        }
      }
    }

    private void editLabel( final YLabel label ) {
      KeyboardHandling.editLabel(view, label);
    }
  }

  /**
   * Displays an inline text editor for the specified label's text.
   * @param view the view that displays the inline editor.
   * @param label the label whose text is edited.
   */
  static void editLabel( final Graph2DView view, final YLabel label ) {
    final EditAction helper = new EditAction(view) {
      protected YLabel findLabel( final Graph2DView view ) {
        return label;
      }
    };
    helper.editLabel(view);
  }

  /**
   * Collapse or expand an items children.
   */
  private static class CollapseExpandAction extends AbstractAction {
    /**
     * Distinguish between expand and collapse. if true, expand an item
     */
    private final boolean expand;
    private final Graph2D graph2D;

    public CollapseExpandAction(final boolean expand, final Graph2D graph2D) {
      this.expand = expand;
      this.graph2D = graph2D;
    }

    public void actionPerformed(ActionEvent e) {
      final NodeCursor nodeCursor = graph2D.selectedNodes();
      if (nodeCursor.size() > 0) {
        graph2D.firePreEvent();
        final Node n = nodeCursor.node();
        if (expand) {
          MindMapUtil.expandNode(graph2D, n);
        } else {
          MindMapUtil.collapseNode(graph2D, n);
        }
        LayoutUtil.layout(graph2D);
        graph2D.firePostEvent();
      }
    }
  }

  /**
   * Adds a sibling for a given item.
   * If the given item is the root item, a new child item is added to the root
   * item instead.
   */
  private static class AddSiblingAction extends AbstractAction {
    private final Graph2DView view;

    AddSiblingAction( final Graph2DView view ) {
      this.view = view;
    }

    public void actionPerformed(final ActionEvent e) {
      final NodeCursor nc = view.getGraph2D().selectedNodes();
      if (nc.ok()) {
        final Node node = nc.node();
        if (ViewModel.instance.isRoot(node)) {
          MindMapUtil.addNode(view, node);
        } else {
          final Node parent = MindMapUtil.inEdge(node).source();
          MindMapUtil.addNode(view, parent, ViewModel.instance.isLeft(node));
        }
      }
    }
  }

  /**
   * Adds a child item for a given item.
   */
  private static class AddChildAction extends AbstractAction {
    private final Graph2DView view;

    AddChildAction( final Graph2DView view ) {
      this.view = view;
    }

    public void actionPerformed( final ActionEvent e ) {
      final NodeCursor nc = view.getGraph2D().selectedNodes();
      if (nc.ok()) {
        MindMapUtil.addNode(view, nc.node());
      }
    }
  }

  /**
   * Navigate through the mind map
   */
  private static class CursorAction extends AbstractAction {
    private final int cursorMode;
    private final Graph2DView view;

    static final int UP = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;
    static final int RIGHT = 4;

    public CursorAction(final int cursorMode, final Graph2DView view) {
      this.cursorMode = cursorMode;
      this.view = view;
    }

    public void actionPerformed(ActionEvent e) {
      final Graph2D graph2D = view.getGraph2D();
      final NodeCursor nodeCursor = graph2D.selectedNodes();
      if (nodeCursor.size() > 0) {
        final Node node = nodeCursor.node();
        Node target = null;
        final ViewModel model = ViewModel.instance;
        switch (cursorMode) {
          case DOWN:
            if (!model.isRoot(node)) {
              final Node parent = MindMapUtil.inEdge(node).source();
              //only care for items on the same side (important if parent is center item)
              final boolean side = model.isLeft(node);
              final EdgeList outEdges = new EdgeList(parent.outEdges(), getSameSidePredicate(side, model));
              //sort edges according to their y-coordinate which is the order of navigating down
              outEdges.sort(new LayoutUtil.YCoordComparator());
              // find and choose successor edge of the current edge
              final ListCell currentCell = outEdges.findCell(MindMapUtil.inEdge(node));
              final ListCell succCell = outEdges.cyclicSucc(currentCell);
              target = ((Edge) succCell.getInfo()).target();
            }
            break;
          case UP:
            if (!model.isRoot(node)) {
              final Node parent = MindMapUtil.inEdge(node).source();
              //only care for items on the same side (important if parent is center item)
              final boolean side = model.isLeft(node);
              final EdgeList outEdges = new EdgeList(parent.outEdges(), getSameSidePredicate(side, model));
              //sort edges according to their y-coordinate which is the order of navigating up
              outEdges.sort(new LayoutUtil.YCoordComparator());
              //find and choose successor edge of the current edge
              final ListCell currentCell = outEdges.findCell(MindMapUtil.inEdge(node));
              final ListCell succCell = outEdges.cyclicPred(currentCell);
              target = ((Edge) succCell.getInfo()).target();
            }
            break;
          case LEFT:
            if (!model.isRoot(node)) {
              //depending on the side of an item, move to its parent or the first children
              if (model.isLeft(node)) {
                final EdgeList edgeList = MindMapUtil.outEdges(node);
                if (!edgeList.isEmpty()) {
                  target = edgeList.popEdge().target();
                }
              } else {
                target = MindMapUtil.inEdge(node).source();
              }
            } else {
              //if item is root, move to the first left item, if there's any
              if (node.outDegree() > 0) {
                for (EdgeList edges = MindMapUtil.outEdges(node);!edges.isEmpty();) {
                  final Edge edge = edges.popEdge();
                  if (model.isLeft(edge.target())) {
                    target = edge.target();
                    break;
                  }
                }
              }
            }
            break;
          case RIGHT:
            if (!model.isRoot(node)) {
              //depending on the side of an item, move to its parent or the first children
              if (model.isLeft(node)) {
                target = MindMapUtil.inEdge(node).source();
              } else {
                final EdgeList edgeList = MindMapUtil.outEdges(node);
                if (!edgeList.isEmpty()) {
                  target = edgeList.popEdge().target();
                }
              }
            } else {
              //if item is root, move to the first right item, if there's any
              if (node.outDegree() > 0) {
                for (EdgeList edges = MindMapUtil.outEdges(node); !edges.isEmpty(); ) {
                  final Edge edge = edges.popEdge();
                  if (!model.isLeft(edge.target())) {
                    target = edge.target();
                    break;
                  }
                }
              }
            }
            break;
        }
        if (target != null) {
          graph2D.setSelected(node, false);
          view.updateView();
          graph2D.setSelected(target, true);
        }
      }
    }

    /**
     * Creates a {@link y.base.DataProvider} that returns <code>true</code> for every edge that goes to the specified
     * side and is not a cross reference.
     * 
     * @param side  The side of the graph (<code>true</code> -> left, <code>false</code> -> right). 
     * @param model The view model which is used to determine the side of the graph.
     * @return a data provider that returns if the checked edge goes to the specified side.
     */
    private DataProvider getSameSidePredicate(final boolean side, final ViewModel model) {
      return new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          final Edge edge = (Edge) dataHolder;
          return side == model.isLeft(edge.target()) && !model.isCrossReference(edge);
        }
      };
    }
  }
}
