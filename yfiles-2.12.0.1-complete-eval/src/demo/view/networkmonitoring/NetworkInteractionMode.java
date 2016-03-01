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

import y.base.DataMap;
import y.base.Edge;
import y.base.Node;
import y.view.EdgeLabel;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ViewMode;

import java.awt.Cursor;

/**
 * {@link ViewMode} to disable, enable or repair network nodes and edges and to show/hide info labels.
 */
public class NetworkInteractionMode extends ViewMode {
  private final DataMap ids;
  private final NetworkModel model;

  public NetworkInteractionMode(NetworkModel model, DataMap ids) {
    this.model = model;
    this.ids = ids;
  }

  /**
   * Overwritten to change the show/hide info labels when a nodes was clicked or to change the state of network elements
   * according to the buttons that where clicked.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mouseClicked(double x, double y) {
    final Graph2D graph = getGraph2D();
    final HitInfo hit = getHitInfo(x, y);
    if (hitErrorSign(hit, x, y)) {
      // hit sign was clicked so repair the belonging network node or connection respectively.
      final Node hitNode = hit.getHitNode();
      if (hitNode != null) {
        final NetworkData data = NetworkMonitoringFactory.getNetworkData(graph.getRealizer(hitNode));
        if (data.isBroken()) {
          model.repairNetworkNode(ids.get(hitNode));
        }
      }
      final EdgeLabel hitLabel = hit.getHitEdgeLabel();
      if (hitLabel != null) {
        final Edge edge = hitLabel.getEdge();
        final NetworkData data = NetworkMonitoringFactory.getNetworkData(graph.getRealizer(edge));
        if (data.isBroken()) {
          model.repairEdge(ids.get(edge));
        }
      }
    } else if (view.getZoom() > NetworkMonitoringDemo.LABEL_HIDE_ZOOM_LEVEL) {
      // info labels are visible
      if (hit.hasHitNodeLabels()) {
        final NodeLabel hitNodeLabel = hit.getHitNodeLabel();
        if (NetworkInfoLabelPainter.hitsCloseIcon(hitNodeLabel, x, y)) {
          // close icon got hit => hide info label
          hitNodeLabel.setVisible(false);
        } else if (NetworkInfoLabelPainter.hitsStateChangeIcon(hitNodeLabel, x, y)) {
          // state change icon got hit => update the state of the according network node
          final Node node = hitNodeLabel.getNode();
          final NetworkData data = NetworkMonitoringFactory.getNetworkData(graph.getRealizer(node));
          if (data.isBroken()) {
            model.repairNetworkNode(ids.get(node));
          } else if (data.isDisabled()) {
            model.enableNetworkNode(ids.get(node));
          } else if (data.isOK()) {
            model.disableNetworkNode(ids.get(node));
          }
        }
      } else if (hit.hasHitNodes()) {
        // node was hit => toggle visibility of the belonging info label
        final Node hitNode = hit.getHitNode();
        final NodeRealizer realizer = graph.getRealizer(hitNode);
        final NodeLabel infoLabel = realizer.getLabel();
        infoLabel.setVisible(!infoLabel.isVisible());
      }
    }
  }

  /**
   * Overwritten to indicate a possible interaction when moving the mouse over a node, an edge or an edge label by
   * changing the mouse cursor visualization.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mouseMoved(double x, double y) {
    changeCursor(x, y, Cursor.getDefaultCursor());
  }

  /**
   * Overwritten to show panning cursor when moving the view port by changing the mouse cursor visualization.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mouseDraggedLeft(double x, double y) {
    view.setViewCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  /**
   * Overwritten to avoid panning cursor when clicking on a node, an edge or an edge label by changing the mouse cursor
   * visualization. Sadly, there is no closed hand cursor in AWT, so the normal hand is used
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mousePressedLeft(double x, double y) {
    changeCursor(x, y, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  /**
   * Overwritten to indicate a possible interaction when mouse is still over a node, an edge or an edge label by
   * changing the mouse cursor visualization.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mouseReleasedLeft(double x, double y) {
    changeCursor(x, y, Cursor.getDefaultCursor());
  }

  private void changeCursor(double x, double y, Cursor defaultCursor) {
    final HitInfo hit = getHitInfo(x, y);
    if (hitsButton(hit, x, y) || hit.hasHitNodes()) {
      view.setViewCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    } else {
      view.setViewCursor(defaultCursor);
    }
  }

  /**
   * Determines whether or not any kind of button is hit by the given coordinates.
   *
   * @param hit current hit information.
   * @param x   x-coordinate of the location to be checked in world-coordinates.
   * @param y   y-coordinate of the location to be checked in world-coordinates.
   *
   * @return <code>true</code> when a button is hit, <code>false</code> otherwise.
   */
  private boolean hitsButton(HitInfo hit, double x, double y) {
    if (hitErrorSign(hit, x, y)) {
      return true;
    } else if (hit.hasHitNodeLabels() && view.getZoom() > NetworkMonitoringDemo.LABEL_HIDE_ZOOM_LEVEL) {
      final NodeLabel hitNodeLabel = hit.getHitNodeLabel();
      if (NetworkInfoLabelPainter.hitsCloseIcon(hitNodeLabel, x, y)
            || NetworkInfoLabelPainter.hitsStateChangeIcon(hitNodeLabel, x, y)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether or not a warning sign either on a network node or connection is hit by the given coordinates.
   *
   * @param hit current hit information.
   * @param x   x-coordinate of the location to be checked in world-coordinates.
   * @param y   y-coordinate of the location to be checked in world-coordinates.
   *
   * @return <code>true</code> when a warning sign is hit, <code>false</code> otherwise.
   */
  private boolean hitErrorSign(HitInfo hit, double x, double y) {
    if (view.getZoom() > view.getPaintDetailThreshold()) {
      if (hit.hasHitNodes()) {
        final NodeRealizer realizer = getGraph2D().getRealizer(hit.getHitNode());
        return NetworkNodePainter.hitWarningSign(realizer, x, y);
      } else if (hit.hasHitEdgeLabels()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Overwritten to get a {@link HitInfo} with all possible graph elements at the given coordinates.
   */
  protected HitInfo getHitInfo(double x, double y) {
    return view.getHitInfoFactory().createHitInfo(x, y, Graph2DTraversal.ALL, false);
  }
}
