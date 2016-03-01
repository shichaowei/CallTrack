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
package demo.layout.multipage;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Graph;
import y.base.GraphFactory;
import y.base.Node;
import y.base.NodeMap;
import y.geom.YDimension;
import y.geom.YPoint;
import y.geom.YPointCursor;
import y.layout.EdgeLabelLayout;
import y.layout.LayoutGraph;
import y.layout.NodeLabelLayout;
import y.layout.multipage.EdgeInfo;
import y.layout.multipage.EdgeLabelInfo;
import y.layout.multipage.ElementInfoManager;
import y.layout.multipage.MultiPageLayout;
import y.layout.multipage.NodeInfo;
import y.layout.multipage.NodeLabelInfo;
import y.util.GraphCopier;
import y.util.Maps;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.HierarchyManager;

import java.awt.Color;
import java.util.Map;

/**
 * Builds {@link Graph2D} page graphs from a {@link MultiPageLayout} instance.
 *
 * @see y.layout.multipage.MultiPageLayout
 */
public class MultiPageGraph2DBuilder {
  /**
   * DataProvider-Key used to store the ids of the referencing node elements, see
   * {@link y.layout.multipage.NodeInfo#getReferencingNode()}.
   * The DataProvider is automatically added to the Graph2D page representation.
   */
  private static final Object REFERENCING_NODE_ID_DPKEY =
          "demo.layout.multipage.MultiPageGraph2DBuilder.REFERENCING_NODE_ID_DPKEY";

  private static final Object NODE_INFO_DPKEY =
          "demo.layout.multipage.MultiPageGraph2DBuilder.NODE_INFO_DPKEY";


  private Graph2D model;
  private MultiPageLayout layout;
  private GraphCopier.CopyFactory copyFactory;

  /**
   * Creates a new instance.
   *
   * @param model the underlying graph.
   * @param layout the result of the page layout applied to the model.
   */
  public MultiPageGraph2DBuilder(Graph2D model, MultiPageLayout layout) {
    this.model = model;
    this.layout = layout;
  }

  public void reset(Graph2D model, MultiPageLayout layout) {
    this.model = model;
    this.layout = layout;
    this.copyFactory = null;
  }

  /**
   * Returns the copy factory that is used to create the Graph2D elements.
   *
   * @return the copy factory used to create the Graph2D elements.
   */
  private GraphCopier.CopyFactory getCopyFactory() {
    return copyFactory;
  }

  /**
   * Returns the result of the page layout applied to the model.
   *
   * @return the page layout.
   * @see #getModel()
   */
  public MultiPageLayout getLayout() {
    return layout;
  }

  /**
   * Returns the underlying graph.
   * 
   * @return the underlying graph.
   */
  public Graph2D getModel() {
    return model;
  }

  /**
   * Returns the ID of the specified node.
   * 
   * @param node the node whose ID is retrieved.
   * @return the ID of the specified node or <code>null</code> if the node
   * does not belong to a page graph created by
   * <code>MultiPageGraph2DBuilder</code>.
   */
  public Object getNodeId( final Node node ) {
    final Graph graph = node.getGraph();
    if (graph != null) {
      final DataProvider dp = graph.getDataProvider(NODE_INFO_DPKEY);
      if (dp != null) {
        final NodeInfo info = (NodeInfo) dp.get(node);
        if (info != null) {
          return info.getId();
        }
      }
    }
    return null;
  }

  public byte getNodeType( final Node node ) {
    final Graph graph = node.getGraph();
    if (graph != null) {
      final DataProvider dp = graph.getDataProvider(NODE_INFO_DPKEY);
      if (dp != null) {
        final NodeInfo info = (NodeInfo) dp.get(node);
        if (info != null) {
          return info.getType();
        }
      }
    }
    return -1;
  }

  public int getPageNo( final Node node ) {
    final Graph graph = node.getGraph();
    if (graph != null) {
      final DataProvider dp = graph.getDataProvider(NODE_INFO_DPKEY);
      if (dp != null) {
        final NodeInfo info = (NodeInfo) dp.get(node);
        if (info != null) {
          return info.getPageNo();
        }
      }
    }
    return -1;
  }

  public int getReferencedPageNo( final Node node ) {
    final Graph graph = node.getGraph();
    if (graph != null) {
      final DataProvider dp = graph.getDataProvider(NODE_INFO_DPKEY);
      if (dp != null) {
        final NodeInfo info = (NodeInfo) dp.get(node);
        if (info != null) {
          final Node mpRef = info.getReferencingNode();
          if (mpRef != null) {
            final NodeInfo mpRefInfo = layout.getNodeInfo(mpRef);
            return mpRefInfo.getPageNo();
          }
        }
      }
    }
    return -1;
  }

  public Object getReferencingNodeId( final Node node ) {
    final Graph graph = node.getGraph();
    if (graph != null) {
      final DataProvider dp = graph.getDataProvider(REFERENCING_NODE_ID_DPKEY);
      if (dp != null) {
        return dp.get(node);
      }
    }
    return null;
  }

  /**
   * Creates a <code>Graph2D</code> representation for the specified page.
   *
   * @param page the <code>Graph2D</code> to store the specified page.
   * @param pageNo the number of the page that should be transformed to a Graph2D
   * @return a <code>Graph2D</code> that represents the specified page.
   */
  public Graph2D createPageView( final Graph2D page, final int pageNo ) {
    copyFactory = page.getGraphCopyFactory();
    final GraphCopier graphCopier = new GraphCopier(new MyGraphCopyFactory());
    graphCopier.copy(getLayout().getPage(pageNo), page);
    return page;
  }

  /**
   * Callback method for adding a connector node (a node of type {@link NodeInfo#TYPE_CONNECTOR})
   * to the Graph2D (including the configuration of its realizer).
   *
   * @param sourceNode the source node (a node on a page of the MultiPageLayout) that should be replicated.
   * @param targetGraph the new node should be added to this graph.
   * @return the added connector node.
   *
   * @see NodeInfo#TYPE_CONNECTOR
   * @see #getLayout()
   */
  protected Node createConnectorNode( final Node sourceNode, final Graph2D targetGraph ) {
    final NodeInfo info = getLayout().getNodeInfo(sourceNode);
    final Node connector = getCopyFactory().copyNode(targetGraph, sourceNode);
    final YPoint center = targetGraph.getCenter(connector);
    final YDimension size = targetGraph.getSize(connector);

    targetGraph.setRealizer(connector, new ShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT));
    targetGraph.getRealizer(connector).setFillColor(Color.YELLOW);
    final Object referencingNodeID = getLayout().getNodeInfo(info.getReferencingNode()).getId();
    ((NodeMap) targetGraph.getDataProvider(REFERENCING_NODE_ID_DPKEY)).set(connector, referencingNodeID);
    addLabels(sourceNode, targetGraph.getRealizer(connector), getLayout());
    targetGraph.setSize(connector, size);
    targetGraph.setCenter(connector, center);

    return connector;
  }

  /**
   * Callback method for adding a
   * {@link NodeInfo#TYPE_PROXY_REFERENCE proxy reference} node
   * to the graph (including the configuration of its realizer).
   *
   * @param sourceNode the source node (a node on a page of the MultiPageLayout) that should be replicated.
   * @param targetGraph the new node should be added to this graph.
   * @return the newly added proxy reference node.
   *
   * @see NodeInfo#TYPE_PROXY_REFERENCE
   * @see NodeInfo#TYPE_PROXY
   * @see #createProxyNode(y.base.Node, y.view.Graph2D)
   * @see #getLayout()
   */
  protected Node createProxyReferenceNode( final Node sourceNode, final Graph2D targetGraph ) {
    final NodeInfo info = getLayout().getNodeInfo(sourceNode);
    final Node pageNode = getCopyFactory().copyNode(targetGraph, sourceNode);
    final YPoint center = targetGraph.getCenter(pageNode);
    final YDimension size = targetGraph.getSize(pageNode);

    final NodeInfo referencingNodeInfo = getLayout().getNodeInfo(
        info.getReferencingNode());
    targetGraph.setRealizer(pageNode, new ShapeNodeRealizer(ShapeNodeRealizer.ELLIPSE));
    targetGraph.getRealizer(pageNode).setFillColor(Color.GREEN);
    targetGraph.setLabelText(pageNode, "p " + (referencingNodeInfo.getPageNo() + 1)); 
    final Object referencingNodeID = getLayout().getNodeInfo(info.getReferencingNode()).getId();
    ((NodeMap) targetGraph.getDataProvider(REFERENCING_NODE_ID_DPKEY)).set(pageNode, referencingNodeID);
    targetGraph.setSize(pageNode, size);
    targetGraph.setCenter(pageNode, center);

    return pageNode;
  }

  /**
   * Callback method for adding a {@link NodeInfo#TYPE_PROXY proxy} node
   * to the graph (including the configuration of its realizer).
   *
   * @param sourceNode the source node (a node on a page of the MultiPageLayout) that should be replicated.
   * @param targetGraph the new node should be added to this graph.
   * @return the newly added proxy node.
   *
   * @see NodeInfo#TYPE_PROXY
   * @see NodeInfo#TYPE_PROXY_REFERENCE
   * @see #createProxyReferenceNode(y.base.Node, y.view.Graph2D)
   * @see #getLayout()
   */
  protected Node createProxyNode( final Node sourceNode, final Graph2D targetGraph ) {
    final NodeInfo info = getLayout().getNodeInfo(sourceNode);
    final Node proxy = getCopyFactory().copyNode(targetGraph, sourceNode);
    final YPoint center = targetGraph.getCenter(proxy);
    final YDimension size = targetGraph.getSize(proxy);

    targetGraph.setRealizer(proxy, new ShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT));
    targetGraph.getRealizer(proxy).setFillColor(Color.LIGHT_GRAY);
    final Object referencingNodeID = getLayout().getNodeInfo(info.getReferencingNode()).getId();
    ((NodeMap) targetGraph.getDataProvider(REFERENCING_NODE_ID_DPKEY)).set(proxy, referencingNodeID);
    addLabels(sourceNode, targetGraph.getRealizer(proxy), getLayout());
    targetGraph.setSize(proxy, size);
    targetGraph.setCenter(proxy, center);

    return proxy;
  }

  /**
   * Callback method for adding a group node to the Graph2D (including the configuration of its realizer).
   *
   * @param sourceNode the source node (a node on a page of the MultiPageLayout) that should be replicated.
   * @param targetGraph the new node should be added to this graph.
   * @return the added group node.
   *
   * @see NodeInfo#TYPE_GROUP
   * @see #getLayout()
   */
  protected Node createGroupNode(final Node sourceNode, final Graph2D targetGraph) {
    final NodeInfo info = getLayout().getNodeInfo(sourceNode);
    final Node groupNode = getCopyFactory().copyNode(targetGraph, sourceNode);
    final YPoint center = targetGraph.getCenter(groupNode);
    final YDimension size = targetGraph.getSize(groupNode);

    final NodeInfo representingNodeInfo = getLayout().getNodeInfo(info.getRepresentedNode());
    targetGraph.setRealizer(groupNode, getRealizer((Node) representingNodeInfo.getId()));
    targetGraph.setSize(groupNode, size);
    targetGraph.setCenter(groupNode, center);

    return groupNode;
  }

  /**
   * Callback method for adding a normal node (a node of type {@link NodeInfo#TYPE_NORMAL})
   * to the Graph2D (including the configuration of its realizer).
   *
   * @param sourceNode the source node (a node on a page of the MultiPageLayout) that should be replicated.
   * @param targetGraph the new node should be added to this graph.
   * @return the added node.
   *
   * @see NodeInfo#TYPE_NORMAL
   * @see #getLayout()
   */
  protected Node createNormalNode(final Node sourceNode, final Graph2D targetGraph) {
    final NodeInfo info = getLayout().getNodeInfo(sourceNode);
    final Node node = getCopyFactory().copyNode(targetGraph, sourceNode);
    final YPoint center = targetGraph.getCenter(node);
    final YDimension size = targetGraph.getSize(node);

    final NodeInfo representingNodeInfo = getLayout().getNodeInfo(info.getRepresentedNode());
    targetGraph.setRealizer(node, getRealizer((Node) representingNodeInfo.getId()));
    addLabels(sourceNode, targetGraph.getRealizer(node), getLayout());
    targetGraph.setSize(node, size);
    targetGraph.setCenter(node, center);

    return node;
  }

  /**
   * Callback method for adding a normal edge (an edge of type {@link y.layout.multipage.EdgeInfo#TYPE_NORMAL})
   * to the Graph2D (including the configuration of its realizer).
   *
   * @param sourceEdge the source edge (an edge on a page of the MultiPageLayout) that should be replicated.
   * @param newSource the source of the new edge.
   * @param newTarget the target of the new edge.
   * @param targetGraph the new edge should be added to this graph.
   * @return the added edge.
   *
   * @see EdgeInfo#TYPE_NORMAL
   * @see #getLayout()
   */
  protected Edge createNormalEdge(final Edge sourceEdge, final Node newSource, final Node newTarget,
                                  final Graph2D targetGraph) {
    final EdgeInfo edgeInfo = getLayout().getEdgeInfo(sourceEdge);
    final Edge newEdge = getCopyFactory().copyEdge(targetGraph, newSource, newTarget, sourceEdge);

    final EdgeRealizer realizer = createCopyOfRealizer((Edge) edgeInfo.getId());
    if (realizer != null) {
      addLabels(sourceEdge, realizer, getLayout());
      applyRealizer(newEdge, targetGraph, realizer);
    }

    return newEdge;
  }

  /**
   * Callback method for adding a
   * {@link EdgeInfo#TYPE_PROXY_REFERENCE proxy reference} edge
   * to the graph (including the configuration of its realizer).
   *
   * @param sourceEdge the source edge (an edge on a page of the MultiPageLayout) that should be replicated.
   * @param newSource the source of the new edge.
   * @param newTarget the target of the new edge.
   * @param targetGraph the new edge should be added to this graph.
   * @return the newly added proxy reference edge.
   *
   * @see EdgeInfo#TYPE_PROXY_REFERENCE
   * @see #getLayout()
   */
  protected Edge createProxyReferenceEdge( final Edge sourceEdge, final Node newSource, final Node newTarget,
                                           final Graph2D targetGraph ) {
    final EdgeInfo edgeInfo = getLayout().getEdgeInfo(sourceEdge);
    final Edge newEdge = getCopyFactory().copyEdge(targetGraph, newSource, newTarget, sourceEdge);

    final EdgeRealizer realizer = createCopyOfRealizer((Edge) edgeInfo.getId());
    if (realizer != null) {
      addLabels(sourceEdge, realizer, getLayout());
      applyRealizer(newEdge, targetGraph, realizer);
    }

    return newEdge;
  }

  /**
   * Callback method for adding a {@link EdgeInfo#TYPE_CONNECTOR connector} edge
   * to the graph (including the configuration of its realizer).
   *
   * @param sourceEdge the source edge (an edge on a page of the MultiPageLayout) that should be replicated.
   * @param newSource the source of the new edge.
   * @param newTarget the target of the new edge.
   * @param targetGraph the new edge should be added to this graph.
   * @return the newly added connector edge.
   *
   * @see EdgeInfo#TYPE_CONNECTOR
   * @see #getLayout()
   */
  protected Edge createConnectorEdge(final Edge sourceEdge, final Node newSource, final Node newTarget,
                                     final Graph2D targetGraph) {
    final EdgeInfo edgeInfo = getLayout().getEdgeInfo(sourceEdge);
    final Edge newEdge = getCopyFactory().copyEdge(targetGraph, newSource, newTarget, sourceEdge);

    final Object representingEdgeId = getLayout().getEdgeInfo(edgeInfo.getRepresentedEdge()).getId();
    final Edge origEdge = (Edge) representingEdgeId;
    final EdgeRealizer origRealizerCopy = createCopyOfRealizer(origEdge);
    addLabels(sourceEdge, origRealizerCopy, getLayout());
    applyRealizer(newEdge, targetGraph, origRealizerCopy);
    return newEdge;
  }

  /**
   * Callback method for adding a {@link EdgeInfo#TYPE_PROXY proxy} edge
   * to the graph (including the configuration of its realizer).
   *
   * @param sourceEdge the source edge (an edge on a page of the MultiPageLayout) that should be replicated.
   * @param newSource the source of the new edge.
   * @param newTarget the target of the new edge.
   * @param targetGraph the new edge should be added to this graph.
   * @return the newly added proxy edge.
   *
   * @see EdgeInfo#TYPE_PROXY
   * @see #getLayout()
   */
  protected Edge createProxyEdge( final Edge sourceEdge, final Node newSource, final Node newTarget,
                                  final Graph2D targetGraph ) {
    final EdgeInfo edgeInfo = getLayout().getEdgeInfo(sourceEdge);
    final Edge newEdge = getCopyFactory().copyEdge(targetGraph, newSource, newTarget, sourceEdge);

    final EdgeRealizer realizer = createCopyOfRealizer((Edge) edgeInfo.getId());
    if (realizer != null) {
      addLabels(sourceEdge, realizer, getLayout());
      applyRealizer(newEdge, targetGraph, realizer);
    }

    return newEdge;
  }

  private static NodeRealizer getRealizer(final Node origNode) {
    final Graph2D graph = (Graph2D) origNode.getGraph();
    final NodeRealizer realizer = graph.getRealizer(origNode);
    return realizer.createCopy();
  }

  private static void addLabels(final Node source, final NodeRealizer targetRealizer, final ElementInfoManager infoManager) {
    //remove existing labels
    for (int i = targetRealizer.labelCount(); i --> 0;) {
      targetRealizer.removeLabel(i);
    }

    //add labels of source to target realizer
    final LayoutGraph sourceGraph = (LayoutGraph) source.getGraph();
    final NodeLabelLayout[] nll = sourceGraph.getNodeLabelLayout(source);
    for (int i = 0; i < nll.length; i++) {
      final NodeLabelInfo labelInfo = infoManager.getNodeLabelInfo(nll[i]);
      final NodeLabel origLabel = (NodeLabel) labelInfo.getId();
      final NodeLabel newLabel = (NodeLabel) origLabel.clone();
      newLabel.setModelParameter(nll[i].getModelParameter());
      targetRealizer.setLabel(newLabel);
    }
  }

  private static void applyRealizer(final Edge e, final Graph2D g, final EdgeRealizer realizer) {
    if (realizer == null) {
      return;
    }

    realizer.clearPoints();
    for (YPointCursor cur = g.getPoints(e).points(); cur.ok(); cur.next()) {
      final YPoint p = cur.point();
      realizer.addPoint(p.x, p.y);
    }
    realizer.setSourcePoint(g.getSourcePointRel(e));
    realizer.setTargetPoint(g.getTargetPointRel(e));
    g.setRealizer(e, realizer);
  }

  private static EdgeRealizer createCopyOfRealizer(Edge e) {
    if (e.getGraph() != null && e.getGraph() instanceof Graph2D) {
      final Graph2D origGraph = (Graph2D) e.getGraph();
      final EdgeRealizer realizer = origGraph.getRealizer(e);
      return realizer.createCopy();
    } else {
      return null;
    }
  }

  private static void addLabels(final Edge source, final EdgeRealizer targetRealizer, final ElementInfoManager infoManager) {
    //remove existing labels
    while (targetRealizer.labelCount() > 0) {
      targetRealizer.removeLabel(targetRealizer.getLabel());
    }

    //add labels of source to target realizer
    final LayoutGraph sourceGraph = (LayoutGraph) source.getGraph();
    final EdgeLabelLayout[] ell = sourceGraph.getEdgeLabelLayout(source);
    for (int i = 0; i < ell.length; i++) {
      final EdgeLabelInfo labelInfo = infoManager.getEdgeLabelInfo(ell[i]);
      final EdgeLabel origLabel = (EdgeLabel) labelInfo.getId();
      final EdgeLabel newLabel = (EdgeLabel) origLabel.clone();
      newLabel.setModelParameter(ell[i].getModelParameter());
      targetRealizer.addLabel(newLabel);
    }
  }

  private class MyGraphCopyFactory implements GraphCopier.CopyFactory {
    public Node copyNode(Graph targetGraph, Node originalNode) {
      final NodeInfo info = getLayout().getNodeInfo(originalNode);
      final Graph2D targetGraph2D = (Graph2D) targetGraph;

      Node graph2DNode;
      if (info.getType() == NodeInfo.TYPE_NORMAL) {
        graph2DNode = createNormalNode(originalNode, targetGraph2D);
      } else if (info.getType() == NodeInfo.TYPE_GROUP) {
        graph2DNode = createGroupNode(originalNode, targetGraph2D);
      } else if (info.getType() == NodeInfo.TYPE_CONNECTOR) {
        graph2DNode = createConnectorNode(originalNode, targetGraph2D);
      } else if (info.getType() == NodeInfo.TYPE_PROXY) {
        graph2DNode = createProxyNode(originalNode, targetGraph2D);
      } else {
        graph2DNode = createProxyReferenceNode(originalNode, targetGraph2D);
      }
      ((NodeMap) targetGraph.getDataProvider(NODE_INFO_DPKEY)).set(graph2DNode, info);

      return graph2DNode;
    }

    public Edge copyEdge(Graph targetGraph, Node newSource, Node newTarget, Edge sourceEdge) {
      final EdgeInfo edgeInfo = getLayout().getEdgeInfo(sourceEdge);
      final Graph2D targetGraph2D = (Graph2D) targetGraph;

      Edge newEdge;
      if (edgeInfo.getType() == EdgeInfo.TYPE_CONNECTOR) {
        newEdge = createConnectorEdge(sourceEdge, newSource, newTarget, targetGraph2D);
      } else  if (edgeInfo.getType() == EdgeInfo.TYPE_PROXY_REFERENCE) {
        newEdge = createProxyReferenceEdge(sourceEdge, newSource, newTarget, targetGraph2D);
      } else if (edgeInfo.getType() == EdgeInfo.TYPE_PROXY) {
        newEdge = createProxyEdge(sourceEdge, newSource, newTarget, targetGraph2D);
      } else {
        newEdge = createNormalEdge(sourceEdge, newSource, newTarget, targetGraph2D);
      }

      return newEdge;
    }

    public Graph createGraph() {
      final Graph2D graph = (Graph2D) getCopyFactory().createGraph();
      if (graph.getHierarchyManager() == null) {
        final HierarchyManager hm = new HierarchyManager(graph);
        final GraphFactory baseFactory = getModel().getHierarchyManager().getGraphFactory();
        if (baseFactory instanceof DefaultHierarchyGraphFactory) {
          hm.setGraphFactory(baseFactory);
        }
      }

      return graph;
    }

    public void preCopyGraphData(Graph sourceGraph, Graph targetGraph) {
      targetGraph.addDataProvider(NODE_INFO_DPKEY, Maps.createHashedNodeMap());
      targetGraph.addDataProvider(REFERENCING_NODE_ID_DPKEY, Maps.createHashedNodeMap());
      getCopyFactory().preCopyGraphData(sourceGraph, targetGraph);
    }

    public void postCopyGraphData(Graph sourceGraph, Graph targetGraph, Map nodeMap, Map edgeMap) {
      getCopyFactory().postCopyGraphData(sourceGraph, targetGraph, nodeMap, edgeMap);
    }
  }
}
