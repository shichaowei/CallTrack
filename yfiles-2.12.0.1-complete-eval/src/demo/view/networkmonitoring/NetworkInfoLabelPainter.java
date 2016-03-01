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

import y.geom.YRectangle;
import y.view.DefaultLabelConfiguration;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.YLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paints an info label that displays a label text and additional buttons on a bubble shaped background.
 */
public class NetworkInfoLabelPainter extends DefaultLabelConfiguration {
  private static final BasicStroke STROKE_ROUNDED_CAP = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);

  private static final double STATUS_ICON_WIDTH = 20;
  private static final double STATUS_ICON_HEIGHT = 20;
  private static final double CLOSE_ICON_WIDTH = 10;
  private static final double CLOSE_ICON_HEIGHT = 10;
  private static final double STATE_CHANGE_ICON_WIDTH = 15;
  private static final double STATE_CHANGE_ICON_HEIGHT = 15;
  private static final double GAP = 5;

  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    final Color oldColor = gfx.getColor();
    final Stroke oldStroke = gfx.getStroke();


    if (label instanceof NodeLabel) {
      // calculate the bubble
      Shape shape = new RoundRectangle2D.Double(x, y, width, height, Math.min(width / 3, 10), Math.min(height / 3, 10));

      // calculate a wedge connecting the node and the rounded rectangle around the label text
      final NodeRealizer realizer = ((NodeLabel) label).getRealizer();
      final double nodeCenterX = realizer.getCenterX();
      final double nodeCenterY = realizer.getCenterY();
      final double labelCenterX = x + width * 0.5d;
      final double labelCenterY = y + height * 0.5d;

      // calculate the tip of the wedge
      final Point2D intersection = new Point2D.Double();
      realizer.findIntersection(nodeCenterX, nodeCenterY, labelCenterX, labelCenterY, intersection);
      final double intersectionX = intersection.getX();
      final double intersectionY = intersection.getY();

      // add the wedge to the bubble shape
      final double dx = labelCenterX - intersectionX;
      final double dy = labelCenterY - intersectionY;
      final double l = Math.sqrt(dx * dx + dy * dy);
      if (l > 0) {
        final double size = Math.min(width, height) * 0.25;
        final GeneralPath p = new GeneralPath();
        p.moveTo((float) intersectionX, (float) intersectionY);
        p.lineTo((float) (labelCenterX + dy * size / l), (float) (labelCenterY - dx * size / l));
        p.lineTo((float) (labelCenterX - dy * size / l), (float) (labelCenterY + dx * size / l));
        p.closePath();
        final Area area = new Area(shape);
        area.add(new Area(p));
        shape = area;
      }

      // paint the bubble using the colors of the label
      final Color backgroundColor = label.getBackgroundColor();
      if (backgroundColor != null) {
        // and background
        gfx.setColor(backgroundColor);
        gfx.fill(shape);
      }

      // line
      final Color lineColor = label.getLineColor();
      if (lineColor != null) {
        gfx.setColor(lineColor);
        gfx.draw(shape);
      }
    }

    gfx.setColor(oldColor);
    gfx.setStroke(oldStroke);
  }

  public void paintContent(YLabel label, Graphics2D graphics, double x, double y, double width, double height) {
    final Color oldColor = graphics.getColor();
    final Shape oldClip = graphics.getClip();
    final Stroke oldStroke = graphics.getStroke();
    final Color oldTextColor = label.getTextColor();

    try {
      if (label instanceof NodeLabel) {
        final NodeLabel nodeLabel = (NodeLabel) label;
        final NetworkData networkData = NetworkMonitoringFactory.getNetworkData(
            nodeLabel.getGraph2D().getRealizer(nodeLabel.getNode()));
        final Insets insets = getInsets(nodeLabel);

        // paint text
        if (!networkData.isOK()) {
          label.setTextColor(Color.GRAY);
        }
        super.paintContent(label, graphics, x, y, width, height);


        // paint close icon
        final double closeX = x + width - CLOSE_ICON_WIDTH - insets.right;
        final double closeY = y + insets.top;
        final Ellipse2D ellipse = new Ellipse2D.Double(closeX, closeY, CLOSE_ICON_WIDTH, CLOSE_ICON_HEIGHT);
        graphics.setColor(label.getBackgroundColor().darker());
        graphics.fill(ellipse);
        final Line2D line = new Line2D.Double(closeX + CLOSE_ICON_WIDTH * 0.25, closeY + CLOSE_ICON_HEIGHT * 0.25,
            closeX + CLOSE_ICON_WIDTH * 0.75, closeY + CLOSE_ICON_HEIGHT * 0.75);
        graphics.setColor(Color.WHITE);
        graphics.draw(line);
        line.setLine(closeX + CLOSE_ICON_WIDTH * 0.25, closeY + CLOSE_ICON_HEIGHT * 0.75,
            closeX + CLOSE_ICON_WIDTH * 0.75, closeY + CLOSE_ICON_HEIGHT * 0.25);
        graphics.draw(line);

        // paint status icon (color-coded workload or broken symbol)
        final double iconX = x + insets.left;
        final double iconY = y + height - STATUS_ICON_HEIGHT - insets.bottom;
        if (networkData.isBroken()) {
          NetworkMonitoringFactory.paintWarningSign(iconX, iconY, STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT, graphics);
        } else {
          ellipse.setFrame(iconX, iconY, STATUS_ICON_WIDTH, STATUS_ICON_HEIGHT);
          graphics.setColor(NetworkMonitoringFactory.getStatusColor(networkData));
          graphics.fill(ellipse);
          graphics.setColor(Color.WHITE);
          graphics.draw(ellipse);
        }

        // paint state change icon (on/off button or restore sign)
        if (networkData.isBroken()) {
          graphics.setStroke(STROKE_ROUNDED_CAP);
          graphics.setColor(NetworkMonitoringFactory.COLOR_WORKLOAD_NONE);
          final double stateChangeX = x + width - STATE_CHANGE_ICON_WIDTH - insets.right;
          final double stateChangeY = y + height - STATE_CHANGE_ICON_HEIGHT - insets.bottom;
          final Arc2D arc = new Arc2D.Double(stateChangeX, stateChangeY, STATE_CHANGE_ICON_WIDTH,
              STATE_CHANGE_ICON_WIDTH, 495, 270, Arc2D.OPEN);
          graphics.draw(arc);
          final GeneralPath path = new GeneralPath();
          path.moveTo((float) (stateChangeX + STATE_CHANGE_ICON_WIDTH * 0.6), (float) stateChangeY);
          path.lineTo((float) (stateChangeX + STATE_CHANGE_ICON_WIDTH * 0.95), (float) stateChangeY);
          path.lineTo((float) (stateChangeX + STATE_CHANGE_ICON_WIDTH * 0.75),
              (float) (stateChangeY + STATE_CHANGE_ICON_HEIGHT * 0.3));
          path.closePath();
          graphics.fill(path);
        } else {
          if (networkData.isOK()) {
            graphics.setColor(Color.RED);
          } else {
            graphics.setColor(NetworkMonitoringFactory.COLOR_WORKLOAD_NONE);
          }
          graphics.setStroke(STROKE_ROUNDED_CAP);
          final double stateChangeX = x + width - STATE_CHANGE_ICON_WIDTH - insets.right;
          final double stateChangeY = y + height - STATE_CHANGE_ICON_HEIGHT - insets.bottom;
          final Arc2D arc = new Arc2D.Double(stateChangeX, stateChangeY, STATE_CHANGE_ICON_WIDTH, STATE_CHANGE_ICON_WIDTH, 495, 270, Arc2D.OPEN);
          graphics.draw(arc);
          line.setLine(stateChangeX + STATE_CHANGE_ICON_WIDTH * 0.5, stateChangeY,
              stateChangeX + STATE_CHANGE_ICON_WIDTH * 0.5, stateChangeY + STATE_CHANGE_ICON_HEIGHT * 0.5);
          graphics.draw(line);
        }
      }
    } finally {
      graphics.setColor(oldColor);
      graphics.setClip(oldClip);
      graphics.setStroke(oldStroke);
      label.setTextColor(oldTextColor);
    }
  }

  /**
   * Overwritten to include the space for the buttons into the labels size.
   */
  public void calculateContentSize(YLabel label, FontRenderContext frc) {
    super.calculateContentSize(label, frc);
    if (label instanceof NodeLabel) {
      final NodeLabel nodeLabel = (NodeLabel) label;
      final Insets insets = getInsets(nodeLabel);
      final double contentWidth = Math.max(nodeLabel.getContentWidth(),
          STATUS_ICON_WIDTH + insets.left + insets.right + STATE_CHANGE_ICON_WIDTH + GAP) + GAP + CLOSE_ICON_WIDTH;
      final double contentHeight = label.getContentHeight() + GAP + Math.max(STATUS_ICON_HEIGHT, STATE_CHANGE_ICON_HEIGHT);
      label.setContentSize(contentWidth, contentHeight);
    }
  }

  /**
   * Determines whether or not the given coordinates are contained in the area of the label's close icon.
   */
  public static boolean hitsCloseIcon(NodeLabel label, double x, double y) {
    final YRectangle box = label.getBox();
    final Insets insets = getInsets(label);
    final double closeX = box.getX() + box.getWidth() - CLOSE_ICON_WIDTH - insets.right;
    final double closeY = box.getY() + insets.top;
    return x > closeX && x < closeX + CLOSE_ICON_WIDTH
        && y > closeY && y < closeY + CLOSE_ICON_HEIGHT;
  }

  /**
   * Determines whether or not the given coordinates are contained in the area of the label's state change icon.
   */
  public static boolean hitsStateChangeIcon(NodeLabel label, double x, double y) {
    final YRectangle box = label.getBox();
    final Insets insets = getInsets(label);
    final double closeX = box.getX() + box.getWidth() - STATE_CHANGE_ICON_WIDTH - insets.right;
    final double closeY = box.getY() + box.getHeight() - STATE_CHANGE_ICON_HEIGHT - insets.bottom;
    return x > closeX && x < closeX + STATE_CHANGE_ICON_WIDTH
        && y > closeY && y < closeY + STATE_CHANGE_ICON_HEIGHT;
  }

  /**
   * Returns the insets of the given label or {@link YLabel#defaultInsets default insets} if there are no insets set for
   * this label.
   */
  private static Insets getInsets(NodeLabel label) {
    return label.getInsets() != null ? label.getInsets() : YLabel.defaultInsets;
  }
}
