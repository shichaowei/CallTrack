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

import y.base.Edge;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Provides methods to determine the mind map root item,
 * cross-reference connections, the collapsed state of items, as well as the
 * location of items relative to the root item.
 * Additionally, items that are temporarily removed from the mind map
 * are stored here, too.
 */
public class ViewModel {
  /**
   * Singleton instance of the view model.
   */
  public static final ViewModel instance = new ViewModel();


  /**
   * The data structure that represents the mind map.
   */
  public final Graph2D graph;

  /**
   * Stores a mapping from collapsed node to the corresponding hidden,
   * non-cross-reference edges.
   * <p>
   * Note, edges that are removed from a graph still reference their source and
   * target nodes, which means that hidden nodes are stored implicitly as well. 
   * </p>
   */
  private final NodeMap hiddenEdges;
  /**
   * Determines which nodes are placed to the left and to the right of the root
   * node.
   */
  private final NodeMap leftOrRight;
  /**
   * Determines which edges represent cross-references.
   */
  private final EdgeMap crossReferences;
  /**
   * Stores hidden cross-reference edges.
   */
  private final Collection hiddenCrossReferences;

  /**
   * The root node of the mind map.
   */
  private Node root;

  private ViewModel() {
    graph = new Graph2D();
    hiddenEdges = graph.createNodeMap();
    leftOrRight = graph.createNodeMap();
    LayoutUtil.addLeftRightMap(graph, leftOrRight);
    crossReferences = graph.createEdgeMap();
    LayoutUtil.addCrossReferencesMap(graph, crossReferences);
    hiddenCrossReferences = new LinkedHashSet();

    LayoutUtil.addPlacersAndComparators(graph);
  }


  /**
   * Determines if the specified node represents the root item of the mind map.
   * @param node the node to be checked.
   * @return <code>true</code> if the specified node represents the root item
   * of the mind map; <code>false</code> otherwise.
   */
  public boolean isRoot( final Node node ) {
    return node == root;
  }

  /**
   * Returns the root node of the mind map.
   * @return the root node of the mind map.
   */
  public Node getRoot() {
    return root;
  }

  /**
   * Sets the root node of the mind map.
   * @param root the root node of the mind map.
   */
  public void setRoot( final Node root ) {
    this.root = root;
  }

  /**
   * Determines if the specified node is currently collapsed in the mind map.
   * @param node the node to be checked.
   * @return <code>true</code> if the specified node is currently collapsed in
   * the mind map; <code>false</code> otherwise.
   */
  public boolean isCollapsed( final Node node ) {
    final EdgeList edges = getHiddenEdges(node);
    return edges != null && !edges.isEmpty();
  }

  /**
   * Determines if the given node should be placed to the left or to the right
   * of the root node.
   * @param node the node whose relative placement is required.
   * @return <code>true</code> if the node should be placed to the left of the
   * root node; <code>false</code> otherwise.
   */
  public boolean isLeft( final Node node ) {
    return leftOrRight.getBool(node);
  }

  /**
   * Specifies if the given node should be placed to the left or to the right
   * of the root node. 
   * @param node the node whose placement is specified.
   * @param left if <code>true</code>, the node is placed to the left of the
   * root node, otherwise it is placed to the right of the root node.
   */
  public void setLeft( final Node node, final boolean left ) {
    leftOrRight.setBool(node, left);
  }

  /**
   * Retrieves and removes the non-cross-reference edges that were temporarily
   * removed from the mind map when the specified node was collapsed.
   * Note, the returned list implicitly specifies the subtree to re-insert
   * into the mind map when expanding the specified node because removed edges
   * still reference their original source and target nodes.
   * @param node a currently collapsed node.
   * @return an {@link EdgeList} with all non-cross-reference edges that were
   * temporarily removed when the specified node was collapsed or
   * <code>null</code> if there are no temporarily removed edges for the
   * specified node.
   * @see #setHiddenEdges(y.base.Node, y.base.EdgeList)
   */
  public EdgeList popHiddenEdges( final Node node ) {
    final EdgeList edges = getHiddenEdges(node);
    hiddenEdges.set(node, null);
    return edges;
  }

  /**
   * Stores the non-cross-reference edges that were temporarily removed from
   * the mind map when the specified node was collapsed.
   * Note, the specified list has to include all edges in the subtree rooted at
   * the specified node at the time of collapsing. 
   * @param node the node that is collapsed.
   * @param edges a list of temporarily removed non-cross-reference edges.
   * @see #popHiddenEdges(y.base.Node)
   */
  public void setHiddenEdges( final Node node, final EdgeList edges ) {
    hiddenEdges.set(node, edges);
  }

  /**
   * Returns the currently hidden, non-cross-reference edges that define
   * the subtree rooted at the specified node.
   * @param node a currently collapsed node.
   * @return an {@link EdgeList} with all the non-cross-reference edges that
   * define the subtree rooted at the specified node. 
   */
  private EdgeList getHiddenEdges( final Node node ) {
    return (EdgeList) hiddenEdges.get(node);
  }


  /**
   * Determines if the specified edge is a cross-reference in the mind map.
   * @param edge the edge to check.
   * @return <code>true</code> if the specified edge is a cross-reference in
   * the min map; <code>false</code> otherwise.
   * @see #setCrossReference(y.base.Edge)
   */
  public boolean isCrossReference( final Edge edge ) {
    return crossReferences.getBool(edge);
  }

  /**
   * Marks the specified edge as a cross-reference in the mind map.
   * @param edge the edge to mark as cross-reference.
   * @see #isCrossReference(y.base.Edge)
   */
  public void setCrossReference( final Edge edge ) {
    crossReferences.setBool(edge, true);
  }

  /**
   * Stores the specified, temporarily removed cross-reference edges for
   * later reinsertion.
   * @param edges a list of temporarily removed cross-reference edges.
   * @see #hiddenCrossReferences()
   * @see #clearHiddenCrossReferences()
   */
  public void addHiddenCrossReferences( final Collection edges ) {
    hiddenCrossReferences.addAll(edges);
  }

  /**
   * Iterates over the temporarily removed cross-reference edges.
   * @return an {@link Iterator} for the temporarily removed cross-reference
   * edges.
   */
  public Iterator hiddenCrossReferences() {
    return hiddenCrossReferences.iterator();
  }

  /**
   * Clears the cache of temporarily removed cross-reference edges.
   * @see #addHiddenCrossReferences(java.util.Collection)
   */
  public void clearHiddenCrossReferences() {
    hiddenCrossReferences.clear();
  }
}
