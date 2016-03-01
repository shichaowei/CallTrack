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

import demo.view.DemoBase;
import y.base.Command;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.layout.FreeNodeLabelModel;
import y.util.Cursors;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DUndoManager;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This class provides static methods to manipulate the mind map
 */
class MindMapUtil {
  static final Color CROSS_EDGE_COLOR = new Color(126, 192, 200);
  static final Color BLACK = new Color(50, 50, 50);
  static final Color RED = new Color(216, 38, 34);
  static final Color GREEN = new Color(128, 255, 128);
  static final Color DARK_GREEN = new Color(87, 173, 87);
  static final Color BLUE = new Color(80, 80, 255);
  static final Color LIGHT_BLUE = new Color(44, 174, 212);
  static final Color MAGENTA = new Color(255, 145, 255);
  static final Color ORANGE = new Color(255, 101, 2);
  static final Color BROWN = new Color(139,69,19);


  private static final int MINIMUM_NODE_WIDTH = 20;


  /**
   * Prevent instantiation of utility class.
   */
  private MindMapUtil() {
  }


  /**
   * Creates a new and configured item and automatically starts the inline
   * text editor for the new item's label.
   * @param parent the parent of the new item
   */
  static void addNode( final Graph2DView view, final Node parent ) {
    addNodeImpl(view, parent, ViewModel.instance.isLeft(parent));
  }

  /**
   * Creates a new and configured item and automatically starts the inline
   * text editor for the new item's label.
   * @param parent the parent of the new item
   * @param placeLeft if <code>true</code>, the new item will be placed to the
   * left of the root item; otherwise it will be placed to the right of the
   * root item. This parameter is ignored if the specified parent is the root
   * item.
   */
  static void addNode( final Graph2DView view, final Node parent, final boolean placeLeft ) {
    addNodeImpl(view, parent, placeLeft);
  }

  private static void addNodeImpl(
          final Graph2DView view,
          final Node parent,
          final boolean placeLeft
  ) {
    final Graph2D graph = view.getGraph2D();
    final Node child = addNodeImpl(graph, parent, "", placeLeft, false);
    KeyboardHandling.editLabel(view, graph.getRealizer(child).getLabel());
  }

  /**
   * Creates a new and configured item.
   * @param graph the current <code>Graph2D</code>
   * @param parent the parent of the new item
   * @param name the text of the new item
   * @param placeLeft if <code>true</code>, the new item will be placed to the
   * left of the root item; otherwise it will be placed to the right of the
   * root item. This parameter is ignored if the specified parent is the root
   * item.
   * @return the new item
   */
  static Node addNode(
          final Graph2D graph,
          final Node parent,
          final String name,
          final boolean placeLeft
  ) {
    return addNodeImpl(graph, parent, name, placeLeft, true);
  }

  private static Node addNodeImpl(
          final Graph2D graph,
          final Node parent,
          final String name,
          final boolean placeLeft,
          final boolean loadFromFile
  ) {
    final ViewModel model = ViewModel.instance;

    graph.firePreEvent(parent);
    final Node node = graph.createNode(0, 0, name);
    final Edge edge = graph.createEdge(parent, node);
    graph.setRealizer(edge, new GenericEdgeRealizer("BezierGradientEdge"));

    //make siblings visible
    if (!loadFromFile && model.isCollapsed(parent)) {
      expandNode(graph, parent);
    }

    final NodeRealizer realizer = graph.getRealizer(node);
    realizer.setFillColor(
            ViewModel.instance.isRoot(parent)
            ? MindMapUtil.BLUE
            : graph.getRealizer(parent).getFillColor());
    final NodeLabel label = realizer.getLabel();
    label.setFontSize(16);
    realizer.setWidth(Math.max(20, label.getWidth()));
    
    boolean isLeftSide = placeLeft;
    if (model.isRoot(parent)) {
      int left = 0;
      int right = 0;
      for (EdgeCursor ec = parent.outEdges(); ec.ok(); ec.next()) {
        if (model.isLeft(ec.edge().target())) {
          ++left;
        } else {
          ++right;
        }
      }
      isLeftSide = left < right;
    }

    updateVisuals(graph, node, isLeftSide);
    //to put the new item at the end of the outEdges, move it to the bottom
    final EdgeList edgeList = outEdges(parent);
    if (edgeList.size() > 1 && !loadFromFile) {
      //depending on the side, the old bottom item is the first or the last
      final NodeRealizer firstRealizer = graph.getRealizer(edgeList.firstEdge().target());
      //the last is the new item, so take the second last
      final NodeRealizer lastRealizer = graph.getRealizer(((Edge)edgeList.get(edgeList.size()-2)).target());
      final double max = Math.max(firstRealizer.getY(), lastRealizer.getY());
      graph.getRealizer(node).setY(max+1);
    }
    if (!loadFromFile) {
      LayoutUtil.layout(graph);
    }
    graph.firePostEvent();
    return node;
  }

  /**
   * Removes the specified item and all of its descendants.
   * @param graph the mind map that contains the items to be removed.
   * @param node the root item to be removed.
   */
  static void removeSubtree( final Graph2D graph, final Node node ) {
    final ViewModel model = ViewModel.instance;
    for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      //only "real" children should be deleted, but not cross-referenced items
      if (!model.isCrossReference(edge)) {
        removeSubtree(graph, edge.target());
      //if the cross reference was connected to one of the item's descendants,
      //it may already be deleted at this point
      } else if (graph.contains(edge)) {
        graph.removeEdge(edge);
      }
    }
    graph.removeNode(node);
  }

  /**
   * Sets the default root item visualization for the specified node.
   * @param graph the mind map.
   * @param node the mind map's root item.
   * @param nodeText the label text for the root item.
   */
  static void setRootRealizer(
          final Graph2D graph, final Node node, final String nodeText
  ) {
    final ShapeNodeRealizer nr = new ShapeNodeRealizer(ShapeNodeRealizer.ELLIPSE);
    nr.setLocation(0, 0);
    nr.setLineType(LineType.LINE_4);
    nr.setFillColor(Color.WHITE);
    nr.setLineColor(MindMapUtil.BLACK);
    final NodeLabel nl = nr.getLabel();
    nl.setFontSize(30);
    nl.setText(nodeText);
    nr.setWidth(Math.max(20, nl.getWidth() * 1.3));
    nr.setHeight(nl.getHeight() * 2.5);

    graph.setRealizer(node, nr);
  }

  /**
   * Switches the specified item from collapsed to expanded state or vice versa. 
   * @param graph the mind map.
   * @param node the item whose descendant have to be collapsed or expanded.
   */
  static void toggleCollapseState( final Graph2D graph, final Node node ) {
    graph.firePreEvent();

    if (ViewModel.instance.isCollapsed(node)) {
      MindMapUtil.expandNode(graph, node);
    } else {
      MindMapUtil.collapseNode(graph, node);
    }
    LayoutUtil.layout(graph);

    graph.firePostEvent();
  }

  /**
   * Collapses the specified item, that means the item's descendants are
   * temporarily removed from the mind map.
   * @param graph the mind map.
   * @param root the item whose descendants are temporarily removed.
   * @see #expandNode(y.view.Graph2D, y.base.Node)
   */
  static void collapseNode( final Graph2D graph, final Node root ) {
    final ViewModel model = ViewModel.instance;

    // determine the nodes and edges which have to be removed from the graph
    final LinkedHashSet nodesToHide = new LinkedHashSet();
    final EdgeList edgesToHide = new EdgeList();
    final LinkedHashSet crossReferences = new LinkedHashSet();
    for (EdgeCursor ec = root.outEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (!model.isCrossReference(edge)) {
        final Node node = edge.target();
        edgesToHide.add(edge);
        nodesToHide.add(node);
        collectSubgraph(node, nodesToHide, edgesToHide, crossReferences);
      }
    }

    // remove the previously collected nodes and edges
    graph.firePreEvent();
    graph.backupRealizers(Cursors.createEdgeCursor(crossReferences));
    graph.backupRealizers(edgesToHide.edges());
    graph.backupRealizers(Cursors.createNodeCursor(nodesToHide));

    for (Iterator it = crossReferences.iterator(); it.hasNext(); ) {
      graph.removeEdge((Edge) it.next());
    }
    for (EdgeCursor ec = edgesToHide.edges(); ec.ok(); ec.next()) {
      graph.removeEdge(ec.edge());
    }

    // cache the removed edges to be able to reinsert them on a later expand
    model.addHiddenCrossReferences(crossReferences);
    model.setHiddenEdges(root, edgesToHide);

    // make sure undo/redo will properly update ViewModel's caches for
    // temporarily removed edges
    getUndoManager(graph).push(new Collapse(root, edgesToHide, crossReferences));

    for (Iterator it = nodesToHide.iterator(); it.hasNext(); ) {
      final Node node = (Node) it.next();

      // store relative location to root
      final double dx = graph.getCenterX(node) - graph.getCenterX(root);
      final double dy = graph.getCenterY(node) - graph.getCenterY(root);
      graph.getRealizer(node).setLocation(dx, dy);
      
      graph.removeNode(node);
    }
    graph.firePostEvent();
  }

  /**
   * Collects all nodes and edges in the subtree rooted at the specified node.
   * Additionally, all cross-references connected to nodes in the subtree are
   * collected as well.
   * @param root the root of the subtree to traverse.
   * @param nodesToHide output parameter to store the subtree nodes.
   * @param edgesToHide output parameter to store the (non-cross-reference)
   * subtree edges.
   * @param cfsToHide output parameter to store all cross-references connected
   * to nodes in the subtree.
   */
  private static void collectSubgraph(
          final Node root,
          final Collection nodesToHide,
          final Collection edgesToHide,
          final Collection cfsToHide
  ) {
    final ViewModel model = ViewModel.instance;
    for (EdgeCursor ec = root.inEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (model.isCrossReference(edge)) {
        // assumes a collection that discards duplicates (i.e. a set)
        cfsToHide.add(edge);
      }
    }
    for (EdgeCursor ec = root.outEdges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (model.isCrossReference(edge)) {
        // assumes a collection that discards duplicates (i.e. a set)
        cfsToHide.add(edge);
      } else {
        final Node node = edge.target();
        nodesToHide.add(node);
        edgesToHide.add(edge);
        collectSubgraph(node, nodesToHide, edgesToHide, cfsToHide);
      }
    }
  }

  /**
   * Expands the specified item, that means the item's descendants that were
   * previously removed by {@link #collapseNode(y.view.Graph2D, y.base.Node)}
   * are inserted into the mind map again.
   * @param graph the mind map.
   * @param root the item whose descendants are added again.
   * @see #collapseNode(y.view.Graph2D, y.base.Node)
   */
  static void expandNode( final Graph2D graph, final Node root ) {
    final ViewModel model = ViewModel.instance;
    final EdgeList hiddenEdges = model.popHiddenEdges(root);
    if (hiddenEdges != null) {
      graph.firePreEvent();

      for (EdgeCursor ec = hiddenEdges.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();

        if (!graph.contains(edge.source())) {
          graph.reInsertNode(edge.source());
          graph.setLocation(edge.source(),
                            graph.getX(root) + graph.getX(edge.source()),
                            graph.getY(root) + graph.getY(edge.source()));
        }

        if (!graph.contains(edge.target())) {
          graph.reInsertNode(edge.target());
          graph.setLocation(edge.target(),
                            graph.getX(root) + graph.getX(edge.target()),
                            graph.getY(root) + graph.getY(edge.target()));
        }

        graph.reInsertEdge(edge);

        //cosmetics
        graph.getRealizer(edge).clearBends();
      }

      // handle cross-references
      final LinkedHashSet crossReferences = new LinkedHashSet();
      for (Iterator it = model.hiddenCrossReferences(); it.hasNext();) {
        final Edge edge = (Edge) it.next();
        if (graph.contains(edge)) {
          it.remove();
          continue;
        }

        if (graph.contains(edge.source()) && graph.contains(edge.target())) {
          it.remove();
          crossReferences.add(edge);
          graph.reInsertEdge(edge);
          //cosmetics
          graph.getRealizer(edge).clearBends();
        }
      }

      // make sure undo/redo will properly update ViewModel's caches for
      // temporarily removed edges
      getUndoManager(graph).push(new Expand(root, hiddenEdges, crossReferences));

      //maybe the item was moved in collapsed state, preventing the children to get updated properly
      if (!model.isRoot(root)) {
        updateVisualsRecursive(graph, root, model.isLeft(root));
      }

      graph.firePostEvent();
    }
  }

  /**
   * Retrieves the icon resource identified by the given filename.
   * @param iconName the filename of the icon resource.
   */
  static Icon getIcon( final String iconName ) {
    return DemoBase.getIconResource("resource/" + iconName);
  }

  /**
   * Calls {@link #updateVisuals(y.view.Graph2D, y.base.Node, boolean)}
   * for the specified item and all of its descendants.
   * @param graph the mind map.
   * @param node the item to be updated.
   * @param left if <code>true</code>, the item relative location is to the left
   * otherwise to the right of the root item.
   */
  static void updateVisualsRecursive(
          final Graph2D graph, final Node node, final boolean left
  ) {
    updateVisuals(graph, node, left);
    for (EdgeCursor ec = outEdges(node).edges(); ec.ok(); ec.next()) {
      //only follow non cross edges
      updateVisualsRecursive(graph, ec.edge().target(), left);
    }
  }

  /**
   * Updates the specified node's visualization according to its current level
   * in the mind map and sets its location relative to the root item.
   * @param graph the mind map.
   * @param node the item to be updated.
   * @param left if <code>true</code>, the item relative location is to the left
   * otherwise to the right of the root item.
   */
  static void updateVisuals(
          final Graph2D graph, final Node node, final boolean left
  ) {
    final NodeRealizer nr = graph.getRealizer(node);

    final ViewModel model = ViewModel.instance;
    final boolean notRoot = !model.isRoot(node);
    final boolean updateVisuals = notRoot && isMindMapRealizer(nr);

    //starting at the center item, the lines should get thinner
    final Edge inEdge = inEdge(node);
    if (inEdge != null && updateVisuals) {
      if (model.isRoot(inEdge.source())) {
        nr.setLineType(LineType.LINE_6);
      } else {
        nr.setLineType(LineType.LINE_3);
      }

      //Adjust Font
      final NodeLabel label = nr.getLabel();
      label.setFontStyle(Font.BOLD);
      if (model.isRoot(inEdge.source())) {
        label.setFontSize(16);
      } else  {
        label.setFontSize(14);
      }
    }

    //Specify appropriate placement
    if (notRoot) {
      model.setLeft(node, left);
    }

    //Set width depending on icon
    if (updateVisuals) {
      updateWidth(graph, node);
    }
  }

  /**
   * Determines whether or not the specified realizer is an instance of the
   * default realizer type used for mind maps.
   * @param nr the node realizer instance to check.
   * @return <code>true</code> if the specified realizer is an instance of the
   * default realizer type used for mind maps; <code>false</code> otherwise.
   */
  private static boolean isMindMapRealizer( final NodeRealizer nr ) {
    return nr instanceof GenericNodeRealizer &&
           "MindMapUnderline".equals(((GenericNodeRealizer) nr).getConfiguration());
  }

  /**
   * Changes the specified node's width and label position depending on the
   * node's state icon.
   * @param graph the mind map.
   * @param node the item to change.
   */
  static void updateWidth( final Graph2D graph, final Node node ) {
    if (!ViewModel.instance.isRoot(node)) {
      NodeRealizer nr = graph.getRealizer(node);
      NodeLabel nl = nr.getLabel();

      int xoffset = 0;
      final Icon icon = MindMapNodePainter.getStateIcon(nr);
      if (icon == null) {
        nr.setWidth(Math.max(MINIMUM_NODE_WIDTH, nl.getWidth()));
      } else {
        final int reserved = icon.getIconWidth() + 4;
        if (!ViewModel.instance.isLeft(node)) {
          xoffset = reserved;
        }
        nr.setWidth(Math.max(MINIMUM_NODE_WIDTH, nl.getWidth() + reserved));
      }

      final FreeNodeLabelModel m = new FreeNodeLabelModel();
      nl.setLabelModel(m, new FreeNodeLabelModel.ModelParameter(xoffset, 0));
    } else {
      final NodeRealizer realizer = graph.getRealizer(node);
      realizer.setWidth(Math.max(MINIMUM_NODE_WIDTH, realizer.getLabel().getWidth() * 1.3));
    }
  }

  /**
   * Returns all non-cross-reference outgoing edges.
   * @param node the source node of the out edges
   * @return all non-cross-reference outgoing edges.
   */
  static EdgeList outEdges( final Node node ) {
    final ViewModel model = ViewModel.instance;
    final EdgeList edges = new EdgeList();
    for (EdgeCursor ec = node.outEdges();ec.ok();ec.next()) {
      if (!model.isCrossReference(ec.edge())) {
        edges.add(ec.edge());
      }
    }
    return edges;
  }

  /**
   * Returns the one non-cross-reference incoming edge.
   * @param node the target node
   * @return the one non-cross-reference incoming edge or <code>null</code>
   * if there is no such edge.
   */
  static Edge inEdge( final Node node ) {
    final ViewModel model = ViewModel.instance;
    for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
      if (!model.isCrossReference(ec.edge())) {
        return ec.edge();
      }
    }
    return null;
  }

  /**
   * Returns the undo queue for the specified graph.
   */
  private static Graph2DUndoManager getUndoManager( final Graph2D graph ) {
    return (Graph2DUndoManager) graph.getBackupRealizersHandler();
  }

  /**
   * Updates {@link ViewModel}'s caches for temporarily removed edges
   * after a collapse or expand operation.
   */
  private static class ChangeState {
    private final Node root;
    private final EdgeList edges;
    private final Collection refs;

    /**
     * Initializes a new <code>ChangeState</code> instance for the
     * given subtree root and the given edges.
     * @param root the node that is either collapsed or expanded.
     * @param edges the non-cross-reference edges that are temporarily removed.
     * @param refs the cross-reference edges that are temporarily removed.
     */
    ChangeState( final Node root, final EdgeList edges, final Collection refs ) {
      this.root = root;
      this.edges = edges;
      this.refs = refs;
    }

    public void execute() {
    }

    /**
     * Updates {@link ViewModel}'s caches for temporarily removed edges
     * after a collapse operation.
     */
    void collapse() {
      final ViewModel model = ViewModel.instance;
      model.addHiddenCrossReferences(refs);
      model.setHiddenEdges(root, edges);
    }

    /**
     * Updates {@link ViewModel}'s caches for temporarily removed edges
     * after an expand operation.
     */
    void expand() {
      final ViewModel model = ViewModel.instance;
      model.popHiddenEdges(root);
      for (Iterator it = model.hiddenCrossReferences(); it.hasNext();) {
        if (refs.contains(it.next())) {
          it.remove();
        }
      }
    }
  }

  /**
   * Updates {@link ViewModel}'s caches for temporarily removed edges
   * when undoing or redoing a collapse operation.
   */
  private static class Collapse extends ChangeState implements Command {
    Collapse( final Node root, final EdgeList edges, final Collection refs ) {
      super(root, edges, refs);
    }

    public void undo() {
      expand();
    }

    public void redo() {
      collapse();
    }
  }

  /**
   * Updates {@link ViewModel}'s caches for temporarily removed edges
   * when undoing or redoing an expand operation.
   */
  private static class Expand extends ChangeState implements Command {
    Expand( final Node root, final EdgeList edges, final Collection refs ) {
      super(root, edges, refs);
    }

    public void undo() {
      collapse();
    }

    public void redo() {
      expand();
    }
  }
}
