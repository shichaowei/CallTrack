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
package demo.layout.router;

import demo.view.advanced.EdgeConnectorDemo;
import y.base.Edge;
import y.base.Node;
import y.geom.YPoint;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.HitInfo;
import y.view.NodeRealizer;

import java.awt.Color;

/**
 * Extends {@link y.view.CreateEdgeMode} to introduce new hubs automatically at existing edges. This is similar to
 * {@link demo.view.advanced.EdgeConnectorDemo.CreateEdgeConnectorMode}
 * <p/>
 * Each new edge is colored in the bus' color if the source of an edge creation is a hub.
 * <p/>
 * This class prevents the creation of edges which would connect hubs of the same bus.
 * <p/>
 * Usage: to create an edge that starts at another edge, shift-press on the edge to initiate the edge creation
 * gesture, then drag the mouse. To create an edge that ends at another edge, shift-release the mouse on the edge.
 */
public class HubCreateEdgeMode extends CreateEdgeMode {
  private Node sourceNode;
  private Node targetNode;
  private Edge sourceSplitEdge;
  private Edge targetSplitEdge;
  private Color color;
  private final NodeRealizer hubRealizer;

  public HubCreateEdgeMode(NodeRealizer hubRealizer) {
    this.hubRealizer = hubRealizer;
    allowSelfloopCreation(false);
    setIndicatingTargetNode(true);
  }

  public NodeRealizer getHubRealizer() {
    return hubRealizer;
  }

  public void mouseShiftPressedLeft(double x, double y) {
    if (isEditing()) {
      super.mouseShiftPressedLeft(x, y);
    } else {
      final Graph2D graph = getGraph2D();
      final Edge edge = getHitInfo(x, y).getHitEdge();

      if (edge != null) {
        final EdgeRealizer edgeRealizer = graph.getRealizer(edge);
        final double[] pathPoint = EdgeConnectorDemo.PointPathProjector.calculateClosestPathPoint(
            edgeRealizer.getPath(), x, y);
        color = edgeRealizer.getLineColor();

        final NodeRealizer sourceRealizer = hubRealizer.createCopy();
        sourceRealizer.setCenter(pathPoint[0], pathPoint[1]);
        sourceRealizer.setFillColor(color);

        sourceNode = graph.createNode(sourceRealizer);
        sourceSplitEdge = edge;
        view.updateView();
        super.mouseShiftPressedLeft(pathPoint[0], pathPoint[1]);
      } else {
        sourceNode = null;
        super.mouseShiftPressedLeft(x, y);
      }
    }
  }

  public void mouseShiftReleasedLeft(double x, double y) {
    final Graph2D graph = getGraph2D();
    final Edge edge = getHitInfo(x, y).getHitEdge();

    if (edge != null) {
      final double[] pathPoint = EdgeConnectorDemo.PointPathProjector.calculateClosestPathPoint(
          graph.getRealizer(edge).getPath(), x, y);

      final NodeRealizer targetRealizer = hubRealizer.createCopy();
      targetRealizer.setCenter(pathPoint[0], pathPoint[1]);
      targetRealizer.setFillColor(color);

      targetNode = graph.createNode(targetRealizer);
      targetSplitEdge = edge;
      view.updateView();
      super.mouseShiftReleasedLeft(pathPoint[0], pathPoint[1]);
    } else {
      super.mouseShiftReleasedLeft(x, y);
    }
  }

  public HitInfo getHitInfo(double x, double y) {
    final HitInfo info = view.getHitInfoFactory().createHitInfo(x, y, Graph2DTraversal.ALL, false);
    setLastHitInfo(info);
    return info;
  }

  protected void edgeCreated(Edge edge) {
    if (sourceNode != null) {
      splitEdge(sourceSplitEdge, sourceNode);
    }
    if (targetNode != null) {
      splitEdge(targetSplitEdge, targetNode);
    }
    super.edgeCreated(edge);
  }

  /**
   * Splits the edge into two parts by introducing the given node as intermediate.
   */
  private void splitEdge(Edge edge, Node node) {
    final Graph2D graph = getGraph2D();
    final Node oldSource = edge.source();
    final Node oldTarget = edge.target();

    graph.firePreEvent();
    // Notes: - pre and post events should frame the node creation also
    //        - the following works only if the edge is straight-line;
    //          otherwise we have to split its path and distribute the bends correctly

    // Do not change the edge which connects to a regular node since for example a NodePort is bound to it.
    // Note that at least one of oldSource and oldTarget is a hub since otherwise this method is not called.
    final EdgeRealizer originalRealizer = graph.getRealizer(edge).createCopy();
    if (!BusRouterDemo.isHub(oldSource)) {
      graph.changeEdge(edge, oldSource, node);

      final Edge edgeToTarget = graph.createEdge(node, oldTarget);
      graph.setRealizer(edgeToTarget, originalRealizer);
      graph.setSourcePointRel(edgeToTarget, YPoint.ORIGIN);
    } else {
      graph.changeEdge(edge, node, oldTarget);

      final Edge edgeFromSource = graph.createEdge(oldSource, node);
      graph.setRealizer(edgeFromSource, originalRealizer);
      graph.setTargetPointRel(edgeFromSource, YPoint.ORIGIN);
    }

    graph.firePostEvent();
  }

  protected void cancelEdgeCreation() {
    if (sourceNode != null) {
      Node tmp = sourceNode;
      sourceNode = null;
      getGraph2D().removeNode(tmp);
    }
    if (targetNode != null) {
      Node tmp = targetNode;
      targetNode = null;
      getGraph2D().removeNode(tmp);
    }

    super.cancelEdgeCreation();
  }

  public void setEditing(boolean editing) {
    if (!editing) {
      sourceNode = null;
      sourceSplitEdge = null;
      targetNode = null;
      targetSplitEdge = null;
      color = null;
    }

    super.setEditing(editing);
  }

  /**
   * Accepts a source node if it is a hub or if it is a regular node and has node ports. Additionally, queries the bus
   * color from a hub node if not already set.
   */
  protected boolean acceptSourceNode(Node source, double x, double y) {
    if (color == null) {
      color = BusRouterDemo.isHub(source) ? view.getGraph2D().getRealizer(source).getFillColor() : Color.BLACK;
    }
    return BusRouterDemo.isHub(source) || super.acceptSourceNode(source, x, y);
  }

  /**
   * Accepts a target node if it is a hub of a different bus or if it is a regular node with node ports.
   */
  protected boolean acceptTargetNode(Node target, double x, double y) {
    return (BusRouterDemo.isHub(target) && color != null && !color.equals(
        getGraph2D().getRealizer(target).getFillColor())) || super.acceptTargetNode(target, x, y);
  }


  /**
   * Returns a dummy EdgeRealizer which has the current bus color set.
   */
  protected EdgeRealizer getDummyEdgeRealizer() {
    final EdgeRealizer realizer = super.getDummyEdgeRealizer();
    if (color != null) {
      realizer.setLineColor(color);
    }
    return realizer;
  }
}
