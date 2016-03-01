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
package demo.view.graphexplorer;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Encapsulates information about old/removed and new elements for
 * partial/incremental and complete re-layout.
 *
 */
final class LayoutContext {
  private final Graph2DView view;
  private final boolean animated;
  private final boolean fromSketch;
  private final Node fixedNode;
  private boolean groupingMode ;
  private final Set newNodes;
  private final Set newEdges;
  private final Set oldNodes;
  private final Set oldEdges;
  private final Set removedNodes;
  private final Set removedEdges;

  /**
   * Initializes a new context object for the specified view.
   * @param view the view whose associated graph is laid out.
   * @param animated if <code>true</code> the calculated layout will be applied
   * in an animated fashion.
   * @param fromSketch if <code>true</code> an incremental layout should be
   * calculated.
   * @param fixedNode specifies a node whose coordinates may not change.
   * @param groupingMode specifies a node whose coordinates may not change.
   */
  LayoutContext(
          final Graph2DView view,
          final boolean animated,
          final boolean fromSketch,
          final Node fixedNode,
          final boolean groupingMode
  ) {
    this.view = view;
    this.animated = animated;
    this.fromSketch = fromSketch;
    this.fixedNode = fixedNode;
    this.groupingMode = groupingMode;
    this.newNodes = new LinkedHashSet();
    this.newEdges = new LinkedHashSet();
    this.oldNodes = new HashSet();
    this.oldEdges = new HashSet();
    this.removedNodes = new LinkedHashSet();
    this.removedEdges = new LinkedHashSet();
    this.groupingMode = groupingMode;
  }

  /**
   * Returns the view whose graph is laid out.
   * @return the view whose graph is laid out.
   */
  Graph2DView getView() {
    return view;
  }

  /**
   * Returns the graph that is laid out.
   * @return the graph that is laid out.
   */
  Graph2D getGraph2D() {
    return view.getGraph2D();
  }

  /**
   * Returns a node whose coordinates may not change.
   * @return a node whose coordinates may not change.
   */
  Node getFixedNode() {
    return fixedNode;
  }

  /**
   * Determines whether or not the calculated layout should be applied in an
   * animated fashion.
   * @return <code>true</code> if the calculated layout should be applied in an
   * animated fashion; <code>false</code> otherwise.
   */
  public boolean isAnimated() {
    return animated;
  }

  /**
   * Determines whether or not an incremental layout should be calculated.
   * @return <code>true</code> if an incremental layout should be calculated;
   * <code>false</code> otherwise.
   */
  boolean isFromSketch() {
    return fromSketch;
  }

  /**
   * Marks a node as <em>new</em> for the purpose of layout calculation.
   * <p>
   * New nodes are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @param node the node to mark as new.
   * @see #newNodes()
   * @see #isNewNode(y.base.Node)
   */
  void addNewNode( final Node node ) {
    newNodes.add(node);
  }

  /**
   * Returns an iterator over all nodes marked as <em>new</code>.
   * <p>
   * New nodes are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @return an iterator over all nodes marked as <em>new</code>.
   * @see #addNewNode(y.base.Node)
   * @see #isNewNode(y.base.Node)
   */
  Iterator newNodes() {
    return newNodes.iterator();
  }

  /**
   * Returns whether or not the specified node is marked as <em>new</em>.
   * <p>
   * New nodes are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @param node the node to check.
   * @return <code>true</code> if the specified node is marked as <em>new</em>;
   * <code>false</code> otherwise.
   * @see #addNewNode(y.base.Node)
   * @see #newNodes()
   */
  boolean isNewNode( final Node node ) {
    return newNodes.contains(node);
  }

  /**
   * Marks an edge as <em>new</em> for the purpose of layout calculation.
   * <p>
   * New edges are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @param edge the edge to mark as new.
   * @see #newEdges()
   */
  void addNewEdge( final Edge edge ) {
    newEdges.add(edge);
  }

  /**
   * Returns an iterator over all edges marked as <em>new</code>.
   * <p>
   * New edges are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @return an iterator over all edges marked as <em>new</code>.
   * @see #addNewEdge(y.base.Edge)
   */
  Iterator newEdges() {
    return newEdges.iterator();
  }

  /**
   * Returns whether or not the specified edge is marked as <em>new</em>.
   * <p>
   * New edges are laid out in an optimal way when performing an incremental
   * layout calculation.
   * </p>
   * @param edge the edge to check.
   * @return <code>true</code> if the specified edge is marked as <em>new</em>;
   * <code>false</code> otherwise.
   * @see #addNewEdge(y.base.Edge)
   * @see #newEdges()
   */
  boolean isNewEdge( final Edge edge ) {
    return newEdges.contains(edge);
  }


  /**
   * Marks a node as <em>old</em> for the purpose of layout calculation.
   * <p>
   * If possible, old nodes are not changed when performing an incremental
   * layout calculation.
   * </p>
   * @param node the node to mark as old.
   * @see #isOldNode(y.base.Node)
   */
  void addOldNode( final Node node ) {
    oldNodes.add(node);
  }

  /**
   * Returns whether or not the specified node is marked as <em>old</em>.
   * <p>
   * If possible, old nodes are not changed when performing an incremental
   * layout calculation.
   * </p>
   * @param node the node to check.
   * @return <code>true</code> if the specified node is marked as <em>old</em>;
   * <code>false</code> otherwise.
   * @see #addOldNode(y.base.Node)
   */
  boolean isOldNode( final Node node ) {
    return oldNodes.contains(node);
  }

  /**
   * Marks an edge as <em>old</em> for the purpose of layout calculation.
   * <p>
   * If possible, old edges are not changed when performing an incremental
   * layout calculation.
   * </p>
   * @param edge the edge to mark as old.
   * @see #isOldEdge(y.base.Edge)
   */
  void addOldEdge( final Edge edge ) {
    oldEdges.add(edge);
  }

  /**
   * Returns whether or not the specified edge is marked as <em>old</em>.
   * <p>
   * If possible, old edges are not changed when performing an incremental
   * layout calculation.
   * </p>
   * @param edge the edge to check.
   * @return <code>true</code> if the specified edge is marked as <em>old</em>;
   * <code>false</code> otherwise.
   * @see #addOldEdge(y.base.Edge)
   */
  boolean isOldEdge( final Edge edge ) {
    return oldEdges.contains(edge);
  }


  /**
   * Marks a node for removal after layout calculation.
   * <p>
   * Nodes marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @param node the node to mark for removal.
   * @see #removedNodes()
   * @see #isRemovedNode(y.base.Node)
   */
  void addRemovedNode( final Node node ) {
    removedNodes.add(node);
  }

  /**
   * Returns an iterator over all nodes marked for removal.
   * <p>
   * Nodes marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @return an iterator over all nodes marked for removal.
   * @see #addRemovedNode(y.base.Node)
   * @see #isRemovedNode(y.base.Node)
   */
  Iterator removedNodes() {
    return removedNodes.iterator();
  }

  /**
   * Returns whether or not the specified node is marked for removal.
   * <p>
   * Nodes marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @param node the node to check.
   * @return <code>true</code> if the specified node is marked for removal;
   * <code>false</code> otherwise.
   * @see #addRemovedNode(y.base.Node)
   * @see #removedNodes()
   */
  boolean isRemovedNode( final Node node ) {
    return removedNodes.contains(node);
  }

  /**
   * Marks an edge for removal after layout calculation.
   * <p>
   * Edges marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @param edge the edge to mark for removal.
   * @see #removedEdges()
   * @see #isRemovedEdge(y.base.Edge)
   */
  void addRemovedEdge( final Edge edge ) {
    removedEdges.add(edge);
  }

  /**
   * Returns an iterator over all edges marked for removal.
   * <p>
   * Edges marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @return an iterator over all edges marked for removal.
   * @see #addRemovedEdge(y.base.Edge)
   * @see #isRemovedEdge(y.base.Edge)
   */
  Iterator removedEdges() {
    return removedEdges.iterator();
  }

  /**
   * Returns whether or not the specified edge is marked for removal.
   * <p>
   * Edge marked for removal are ignored when performing layout calculation
   * and are faded out during layout change animation.
   * </p>
   * @param edge the edge to check.
   * @return <code>true</code> if the specified edge is marked for removal;
   * <code>false</code> otherwise.
   * @see #addRemovedEdge(y.base.Edge)
   * @see #removedEdges()
   */
  boolean isRemovedEdge( final Edge edge ) {
    return removedEdges.contains(edge);
  }

  /**
   * Signals whether or not a layout algorithm shall treat the layout graph as
   * being grouped.
   * @return <code>true</code> if a layout algorithm shall treat the layout
   * graph as being grouped; <code>false</code> otherwise.
   */
  public boolean isGroupingMode() {
    return groupingMode;
  }
}
