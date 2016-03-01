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
package demo.view.networkmonitoring;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.labeling.SALabeling;
import y.util.Maps;
import y.view.Drawable;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

/**
 * The network view creates a view graph and updates it every time the state of elements in the model graph changes.
 */
class NetworkView implements NetworkModelObserver {
  private static final TrafficMarker TRAFFIC_MARKER = new TrafficMarker();

  private final NetworkModel model;
  private final Graph2DView view;
  private final DataMap view2model;
  private final HashSet brokenElements;
  private final AnimationPlayer zoomDependentPlayer;

  public NetworkView(NetworkModel model, Graph2DView view, DataMap view2model) {
    this.model = model;
    this.view = view;
    this.view2model = view2model;

    final Graph2D modelGraph = model.getNetworkModel();
    final Graph2D viewGraph = view.getGraph2D();
    final DataProvider nodeTypes = modelGraph.getDataProvider(NetworkModel.NODE_TYPE_DPKEY);
    final DataProvider elementIds = modelGraph.getDataProvider(NetworkModel.ELEMENT_ID_DPKEY);
    final DataProvider edgeCapacities = modelGraph.getDataProvider(NetworkModel.ELEMENT_CAPACITY_DPKEY);
    final DataProvider nodeInfos = modelGraph.getDataProvider(NetworkModel.NODE_INFO_DPKEY);

    brokenElements = new HashSet(modelGraph.nodeCount() + modelGraph.edgeCount());

    zoomDependentPlayer = createZoomDependentPlayer(view);

    // create view nodes that have different configurations for different network node types
    final DataMap model2view = Maps.createHashedDataMap();
    for (NodeCursor nc = modelGraph.nodes(); nc.ok(); nc.next()) {
      final Node modelNode = nc.node();
      final Node viewNode = viewGraph.createNode();
      if (nodeTypes != null && nodeTypes.get(modelNode) != null) {
        NodeRealizer realizer;
        switch (nodeTypes.getInt(modelNode)) {
          case NetworkModel.PC:
            realizer = NetworkMonitoringFactory.createWorkstation();
            break;
          case NetworkModel.LAPTOP:
            realizer = NetworkMonitoringFactory.createLaptop();
            break;
          case NetworkModel.SMARTPHONE:
            realizer = NetworkMonitoringFactory.createSmartphone();
            break;
          case NetworkModel.SWITCH:
            realizer = NetworkMonitoringFactory.createSwitch();
            break;
          case NetworkModel.WLAN:
            realizer = NetworkMonitoringFactory.createWLan();
            break;
          case NetworkModel.DATABASE:
            realizer = NetworkMonitoringFactory.createDatabase();
            break;
          case NetworkModel.SERVER:
            realizer = NetworkMonitoringFactory.createServer();
            break;
          default:
            realizer = new GenericNodeRealizer();
        }
        final NodeLabel infoLabel = realizer.getLabel();
        if (infoLabel != null) {
          final NetworkNodeInfo nodeInfo = (NetworkNodeInfo) nodeInfos.get(modelNode);
          infoLabel.setText(nodeInfo.getName() + "\n" + nodeInfo.getIpAddress());

          // make a collection of info labels visible to show that info labels exist
          if (viewNode.index() % 9 == 0) {
            infoLabel.setVisible(true);
          }
        }
        realizer.setCenter(modelGraph.getCenterX(modelNode), modelGraph.getCenterY(modelNode));
        viewGraph.setRealizer(viewNode, realizer);
      }
      if (elementIds != null && elementIds.get(modelNode) != null) {
        final Object id = elementIds.get(modelNode);
        view2model.set(viewNode, id);
        model2view.set(id, viewNode);
      }
    }

    // determine highest capacity in graph to be able to divide them into three groups with different line types
    int maxCapacity = 0;
    if (edgeCapacities != null) {
      for (EdgeCursor ec = modelGraph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        final int capacity = edgeCapacities.getInt(edge);
        if (capacity > maxCapacity) {
          maxCapacity = capacity;
        }
      }
    }
    // create view edges with line thickness modeling capacity.
    for (EdgeCursor ec = modelGraph.edges(); ec.ok(); ec.next()) {
      final Edge modelEdge = ec.edge();
      if (elementIds != null) {
        final Edge viewEdge = viewGraph.createEdge((Node) model2view.get(elementIds.get(modelEdge.source())),
            (Node) model2view.get(elementIds.get(modelEdge.target())));
        final EdgeRealizer realizer = NetworkMonitoringFactory.createConnection();
        viewGraph.setRealizer(viewEdge, realizer);
        if (edgeCapacities != null) {
          final int capacity = edgeCapacities.getInt(modelEdge);
          if (capacity < maxCapacity * 0.33) {
            realizer.setLineType(LineType.LINE_3);
          } else if (capacity < maxCapacity * 0.66) {
            realizer.setLineType(LineType.LINE_5);
          } else {
            realizer.setLineType(LineType.LINE_7);
          }
        }
        final Object id = elementIds.get(modelEdge);
        view2model.set(viewEdge, id);
        model2view.set(id, viewEdge);
      }
    }

    // position info labels
    final SALabeling labeling = new SALabeling();
    labeling.setPlaceNodeLabels(true);
    labeling.setPlaceEdgeLabels(false);
    labeling.setRemoveNodeOverlaps(true);
    labeling.setDeterministicModeEnabled(true);
    new Graph2DLayoutExecutor().doLayout(viewGraph, labeling);
  }

  /**
   * Creates an {@link AnimationPlayer} that will be stopped if the view changes to sloppy mode.
   */
  private AnimationPlayer createZoomDependentPlayer(Graph2DView view) {
    final ViewAnimationFactory factory = new ViewAnimationFactory(view);
    final AnimationPlayer player = factory.createConfiguredPlayer();

    view.getCanvasComponent().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if ("Zoom".equals(evt.getPropertyName())) {
          final double zoom = ((Double) evt.getNewValue()).doubleValue();
          if (zoom <= NetworkMonitoringDemo.PAINT_DETAIL_THRESHOLD
              && player.isPlaying()) {
            player.stop();
          }
        }
      }
    });

    return player;
  }

  /**
   * Updates the view graph with new information of the given {@link DataMap}.
   */
  public void update(DataMap dataMap) {
    final Graph2D viewGraph = view.getGraph2D();
    Rectangle2D focusRect = null;
    for (NodeCursor nodeCursor = viewGraph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      final Node viewNode = nodeCursor.node();
      if (dataMap.get(view2model.get(viewNode)) != null) {
        final double workload = ((Double) dataMap.get(view2model.get(viewNode))).doubleValue();
        final NodeRealizer realizer = viewGraph.getRealizer(viewNode);
        if (realizer instanceof GenericNodeRealizer) {
          final GenericNodeRealizer gnr = (GenericNodeRealizer) realizer;
          final NetworkData networkData = (NetworkData) gnr.getUserData();
          networkData.setWorkload(workload);
          if (networkData.isBroken() && !brokenElements.contains(viewNode)) {
            brokenElements.add(viewNode);
            // network node just broke
            // => update area (node) that gets focused if it is not contained in the current view
            if (focusRect == null) {
              focusRect = new Rectangle2D.Double(0, 0, -1, -1);
            }
            focusRect.setFrame(realizer.getX(), realizer.getY(), realizer.getWidth(), realizer.getHeight());
          } else if (!networkData.isBroken() && brokenElements.contains(viewNode)) {
            brokenElements.remove(viewNode);
          }
        }
      }
    }
    for (EdgeCursor ec = viewGraph.edges(); ec.ok(); ec.next()) {
      final Edge viewEdge = ec.edge();
      final EdgeRealizer realizer = viewGraph.getRealizer(viewEdge);
      if (realizer instanceof GenericEdgeRealizer) {
        final GenericEdgeRealizer ger = (GenericEdgeRealizer) realizer;
        final NetworkData networkData = (NetworkData) ger.getUserData();
        if (dataMap.get(view2model.get(viewEdge)) != null) {
          final double workload = ((Double) dataMap.get(view2model.get(viewEdge))).doubleValue();
          networkData.setWorkload(workload);
          final EdgeLabel errorLabel = realizer.getLabel();
          if (networkData.isBroken() && !brokenElements.contains(viewEdge)) {
            brokenElements.add(viewEdge);
            // connection just broke
            // => update area (edge + source and target) that gets focused if it is not contained in the current view
            if (focusRect == null) {
              focusRect = new Rectangle2D.Double(0, 0, -1, -1);
            }
            final Node source = viewEdge.source();
            final Node target = viewEdge.target();
            final double sourceX = viewGraph.getX(source);
            final double targetX = viewGraph.getX(target);
            final double sourceY = viewGraph.getY(source);
            final double targetY = viewGraph.getY(target);
            final double minX = Math.min(sourceX, targetX);
            final double minY = Math.min(sourceY, targetY);
            final double maxX = Math.max(sourceX + viewGraph.getWidth(source), targetX + viewGraph.getWidth(target));
            final double maxY = Math.max(sourceY + viewGraph.getHeight(source), targetY + viewGraph.getHeight(target));
            focusRect.setFrameFromDiagonal(minX, minY, maxX, maxY);
            errorLabel.setVisible(true);
          } else if (!networkData.isBroken() && brokenElements.contains(viewEdge)) {
            brokenElements.remove(viewEdge);
            errorLabel.setVisible(false);
          }
        }
      }
    }

    // animate changes
    final int stepDuration = model.getUpdateCycle();

    // change view port only if there just broke a network element
    if (focusRect != null && !view.getVisibleRect().contains(focusRect)
        && (focusRect.getCenterX() != view.getCenter().getX() || focusRect.getCenterY() != view.getCenter().getY())) {
      final AnimationPlayer player = new ViewAnimationFactory(view).createConfiguredPlayer();
      player.animate(createFocusViewAnimation(focusRect, stepDuration));
    }
    if (view.getZoom() > NetworkMonitoringDemo.PAINT_DETAIL_THRESHOLD) {
      zoomDependentPlayer.animate(createConnectionAnimation(stepDuration, dataMap));
    } else {
      view.updateView();
    }
  }

  /**
   * Creates an animation that focuses the given focus rect if it lies (partly) outside the current view port.
   */
  private AnimationObject createFocusViewAnimation(Rectangle2D focusRect, int duration) {
    // in case a network element outside the current view just broke, focus view on it
    final Point2D newCenter = new Point2D.Double(focusRect.getCenterX(), focusRect.getCenterY());

    final int newZoom = 1;
    final double intermediateZoom = Math.max(NetworkMonitoringDemo.MIN_ZOOM, Math.min(view.getZoom(), newZoom) - 0.4);
    return AnimationFactory.createEasedAnimation(
        new FocusViewAnimation(newCenter, newZoom, intermediateZoom, duration));
  }

  /**
   * Creates an animation that visualizes data activity on network connections.
   */
  private AnimationObject createConnectionAnimation(int duration, DataMap dataMap) {
    final Graph2D graph = view.getGraph2D();
    final ViewAnimationFactory factory = new ViewAnimationFactory(view);
    final CompositeAnimationObject concurrency = AnimationFactory.createConcurrency();

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeRealizer realizer = graph.getRealizer(edge);
      final NetworkData networkData = NetworkMonitoringFactory.getNetworkData(realizer);
      if (dataMap.get(view2model.get(edge)) != null && networkData != null && networkData.getWorkload() > 0) {
        final GeneralPath path = realizer.getPath();
        concurrency.addAnimation(factory.traversePath(path, false, TRAFFIC_MARKER, duration));
        concurrency.addAnimation(factory.traversePath(path, true, TRAFFIC_MARKER, duration));
      }
    }

    return concurrency;
  }

  /**
   * Marker that can be moved along an edge to visualize data activity.
   */
  private static class TrafficMarker implements Drawable {
    private static final int WIDTH = 6;
    private static final int HEIGHT = 6;

    public void paint(final Graphics2D g) {
      final Rectangle bounds = getBounds();
      g.drawOval(0, 0, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
      return new Rectangle(0, 0, WIDTH, HEIGHT);
    }
  }

  /**
   * {@link y.anim.AnimationObject Animation} that moves the view's current center to a new center while zooming to a
   * new zoom level passing an intermediate zoom level.
   */
  private class FocusViewAnimation implements AnimationObject {
    private final Point2D newCenter;
    private final double intermediateZoom;
    private final double newZoom;
    private final long preferredDuration;
    private Point2D oldCenter;
    private double oldZoom;

    public FocusViewAnimation(Point2D newCenter, double newZoom, double intermediateZoom, long preferredDuration) {
      this.newCenter = newCenter;
      this.newZoom = newZoom;
      this.intermediateZoom = intermediateZoom;
      this.preferredDuration = preferredDuration;
    }

    public void initAnimation() {
      oldCenter = view.getCenter();
      oldZoom = view.getZoom();
    }

    public void calcFrame(double time) {
      // move center from old center to new center
      view.setCenter(
          (newCenter.getX() - oldCenter.getX()) * time + oldCenter.getX(),
          (newCenter.getY() - oldCenter.getY()) * time + oldCenter.getY());

      // Bezier interpolation for a smooth zoom change
      view.setZoom((1 - time) * (1 - time) * oldZoom + 2 * time * (1 - time) * intermediateZoom + time * time * newZoom);
    }

    public void disposeAnimation() {
    }

    public long preferredDuration() {
      return preferredDuration;
    }
  }
}
