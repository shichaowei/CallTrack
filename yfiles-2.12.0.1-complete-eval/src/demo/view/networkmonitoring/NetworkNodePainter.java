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

import y.view.GenericNodeRealizer;
import y.view.ImageNodePainter;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.net.URL;

/**
* A {@link GenericNodeRealizer.Painter painter} that paints a network node either in enabled or disabled state.
*/
public class NetworkNodePainter implements GenericNodeRealizer.Painter, GenericNodeRealizer.ContainsTest {
  static final int SIDE_WARNING_SIGN = 20;

  private final ImageNodePainter enabledNetworkNodePainter;
  private final ImageNodePainter disabledNetworkNodePainter;

  public NetworkNodePainter(URL enabledImage, URL disableImage) {
    // image node painter delegates for the enabled and the disabled state
    enabledNetworkNodePainter = new ImageNodePainter(enabledImage);
    disabledNetworkNodePainter = new ImageNodePainter(disableImage);
  }

  public void paint(NodeRealizer context, Graphics2D graphics) {
    final NetworkData networkData = NetworkMonitoringFactory.getNetworkData(context);
    if (networkData.isOK()) {
      enabledNetworkNodePainter.paint(context, graphics);
    } else {
      disabledNetworkNodePainter.paint(context, graphics);
    }

    // paint state icon above the image if the network node is broken
    if (networkData.isBroken()) {
      NetworkMonitoringFactory.paintWarningSign(context.getX(),
          context.getY() + context.getHeight() - SIDE_WARNING_SIGN, SIDE_WARNING_SIGN, SIDE_WARNING_SIGN, graphics);
    }
  }

  public void paintSloppy(NodeRealizer context, Graphics2D graphics) {
    final Color oldColor = graphics.getColor();
    try {
      // paint rectangle in current status color
      final NetworkData data = NetworkMonitoringFactory.getNetworkData(context);
      graphics.setColor(NetworkMonitoringFactory.getStatusColor(data));
      graphics.fill(context.getBoundingBox());
    } finally {
      graphics.setColor(oldColor);
    }
  }

  public boolean contains(NodeRealizer context, double x, double y) {
    final NetworkData networkData = NetworkMonitoringFactory.getNetworkData(context);
    if (networkData.isOK()) {
      return enabledNetworkNodePainter.contains(context, x, y);
    } else {
      // also include hit test for warning sign in case alpha image is used and the warning sign lies in the transparent
      // area of the image
      return disabledNetworkNodePainter.contains(context, x, y) || hitWarningSign(context, x, y);
    }
  }

  /**
   * Determines whether the {@link #contains(NodeRealizer,double,double)} method should use the alpha transparency of
   * the image to determine whether this realizer "contains" points. This influences hit testing and edge intersection
   * calculation.
   *
   * @param useAlphaImage whether to use the alpha transparency of the image
   */
  public void setAlphaImageUsed(boolean useAlphaImage) {
    enabledNetworkNodePainter.setAlphaImageUsed(useAlphaImage);
    disabledNetworkNodePainter.setAlphaImageUsed(useAlphaImage);
  }

  /**
   * Checks if the warning sign of the realizer contains the given coordinates.
   */
  public static boolean hitWarningSign(NodeRealizer context, double x, double y) {
    final NetworkData networkData = NetworkMonitoringFactory.getNetworkData(context);
    return networkData.isBroken()
        && x > context.getX() && x < context.getX() + SIDE_WARNING_SIGN
        && y > context.getY() + context.getHeight() - SIDE_WARNING_SIGN && y < context.getY() + context.getHeight();
  }
}
