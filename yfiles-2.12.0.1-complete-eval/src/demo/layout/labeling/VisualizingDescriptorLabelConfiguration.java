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
package demo.layout.labeling;

import y.geom.YPoint;
import y.geom.YVector;
import y.layout.PreferredPlacementDescriptor;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.YLabel;
import y.view.YRenderingHints;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

/**
 * A label painter for edge labels that visualizes the settings of the {@link PreferredPlacementDescriptor} of the label.
 */
class VisualizingDescriptorLabelConfiguration extends DefaultLabelConfiguration {

  private static final Color COLOR_ROTATION = new Color(51, 102, 153);
  private static final Color COLOR_DISTANCE = new Color(102,204,51);
  private static final Color COLOR_PREFERRED_SIDE = new Color(204, 153, 51);
  private static final Color COLOR_ADDITIONAL_ROTATION = new Color(153, 51, 51);
  private static final double TWO_PI = Math.PI * 2;

  private static Font smallFont;

  public void paintContent(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    // We don't want the label text to be upside down, so we flip the text in case it would be
    final boolean flipText = label.getOrientedBox().getUpY() > 0;
    try {
      if (flipText) {
        gfx.rotate(Math.PI, x + width/2-2, y + height/2);
      }

      super.paintContent(label, gfx, x, y, width, height);
    } finally {
      if (flipText) {
        gfx.rotate(Math.PI, x + width/2-2, y + height/2);
      }
    }
  }

  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    if (label instanceof EdgeLabel) {
      final Color oldColor = gfx.getColor();
      final Paint oldPaint = gfx.getPaint();
      final Font oldFont = gfx.getFont();

      try {
        final double x2 = x + width;
        final double y2 = y + height;
        final double cx = x + width / 2;
        final double cy = y + height / 2;

        final PreferredPlacementDescriptor placementDescriptor = ((EdgeLabel) label).getPreferredPlacementDescriptor();
        final double dist = Math.max(1, placementDescriptor.getDistanceToEdge());

        // check if the label flow and/or the angle rotation is flipped and calculate the real rotation angle
        final boolean labelFlowIsFlipped =
          placementDescriptor.isRightOfEdge() && placementDescriptor.isAngleOffsetOnRightSide180();
        final boolean angleRotationIsFlipped =
          placementDescriptor.isRightOfEdge() && placementDescriptor.isAngleOnRightSideCounterRotating();

        double angle = placementDescriptor.getAngle();
        if (angleRotationIsFlipped) {
          angle = -angle;
        }

        final double angleWithoutOffset = angle;
        if (labelFlowIsFlipped) {
          angle += Math.PI;
        }

        // add a triangle as arrow head to our label bounds to visualize the direction the label points to
        final GeneralPath labelShapePath = new GeneralPath();
        labelShapePath.moveTo((float) x, (float) y);
        labelShapePath.lineTo((float) (x2 - height / 4), (float) y);
        labelShapePath.lineTo((float) x2, (float) cy);
        labelShapePath.lineTo((float) (x2 - height / 4), (float) y2);
        labelShapePath.lineTo((float) x, (float) y2);

        // use a gradient as background of this shape
        gfx.setPaint(
          new GradientPaint(
            (float) x,
            (float) y,
            Color.white,
            (float) (x + width + height / 2),
            (float) y, COLOR_ROTATION, true));
        gfx.fill(labelShapePath);

        YVector rightVector = new YVector(width, 0);
        rightVector = rightVector.rotate(-angle);

        // visualize the distance between the edge segment and the center of the closest label border side
        if (placementDescriptor.isAngleRelativeToEdgeFlow() && !placementDescriptor.isOnEdge()) {
          // only if the angle is relative to the edge we are able to recalculate the edge orientation
          // if the label is placed on the edge, there is no distance to the edge

          // we calculate the orientation of the edge to decide on which side of the edge the label is placed
          double edgeAngle = -label.getOrientedBox().getAngle() - angle;
          edgeAngle = normalizeAngle(edgeAngle);

          final boolean labelIsLeftOfEdgeInFlow = isLabelLeftOfEdgeInFlow(placementDescriptor, edgeAngle);
          // with this information we can determine if the bottom or upper side of the label is closer to the edge
          final boolean bottomSideIsCloserToEdge = placementDescriptor.isLeftOfEdge()
                  ? labelIsLeftOfEdgeInFlow
                  : labelIsLeftOfEdgeInFlow ^ labelFlowIsFlipped;

          final double yValue = bottomSideIsCloserToEdge ? y + height : y;
          // center of the label bound side that is closest to the edge
          final YPoint cLabelBorder = new YPoint(cx, yValue);

          final double distToEdge =
            dist // distance from the closest corner of the label to the edge
            + Math.abs(rightVector.getY() / 2) // distance the center of the closest side is further away from the edge then the closest corner
            - 0.5; // half the edge width
          YVector lineToEdge = new YVector(0, labelIsLeftOfEdgeInFlow ? distToEdge : -distToEdge);
          lineToEdge = lineToEdge.rotate(-angle);

          // foot of a dropped perpendicular from cLabelBorder on the edge
          final YPoint cEdge = new YPoint(cLabelBorder.getX() + lineToEdge.getX(), cLabelBorder.getY() + lineToEdge.getY());

          gfx.setColor(COLOR_DISTANCE);
          gfx.fill(new Arc2D.Double(cLabelBorder.getX() - 2, cLabelBorder.getY() - 2, 4, 4, bottomSideIsCloserToEdge ? 0 : 180, -180, Arc2D.CHORD));

          gfx.draw(new Line2D.Double(cLabelBorder.getX(), cLabelBorder.getY(), cEdge.getX(), cEdge.getY()));

          gfx.fill(new Arc2D.Double(cEdge.getX() - 2, cEdge.getY() - 2, 4, 4, Math.toDegrees(angle), labelIsLeftOfEdgeInFlow ? 180 : -180, Arc2D.CHORD));
        }

        // visualize how the angle was used to rotate the label
        if (angleWithoutOffset != 0) {
          // paint a blue line indicating where the upper bound of the label
          // would be if the label would not be rotated
          gfx.setColor(COLOR_ROTATION);
          if (labelFlowIsFlipped) {
            gfx.draw(new Line2D.Double(x, y, x - rightVector.getX(), y - rightVector.getY()));
          } else {
            gfx.draw(new Line2D.Double(x, y, x + rightVector.getX(), y + rightVector.getY()));
          }
          // indicate the angle by a little arc between this line and the upper label bound
          gfx.draw(new Arc2D.Double(x - 10, y - 10, 20, 20, 0, angleWithoutOffset, Arc2D.OPEN));
        }

        // visualize how the 180 degree angle offset affects the label orientation
        if (labelFlowIsFlipped) {
          // paint a red line indicating where the upper bounds of the label
          // would be if the label would neither be rotated nor the label flow would be flipped
          gfx.setColor(COLOR_ADDITIONAL_ROTATION);
          gfx.draw(new Line2D.Double(x, y, x + rightVector.getX()/2, y + rightVector.getY()/2));
          // indicate the 180 degree angle offset for label flow flipping using a little red arc
          // between the red and blue lines
          gfx.draw(new Arc2D.Double(x - 5, y - 5, 10, 10, Math.toDegrees(angle), -180, Arc2D.OPEN));
        }

        // mark the label with the preferred side of the label
        gfx.setColor(COLOR_PREFERRED_SIDE);
        gfx.setFont(getSmallFont(gfx));
        gfx.rotate(-Math.PI/2, x, y);
        final String position = isOnAnySide(placementDescriptor) ? "Any"
            : placementDescriptor.isLeftOfEdge() ? "Left"
            : placementDescriptor.isRightOfEdge() ? "Right"
            : "OnEdge";
        gfx.drawString(position, (float) (x - height + 1), (float) (y - 1));
        gfx.rotate(Math.PI/2, x, y);

        // draw selection box
        if (useSelectionStyle(label, gfx)) {
          gfx.setColor(Color.RED);
          gfx.draw(labelShapePath);
        }
      } finally {
        // restore old context
        gfx.setFont(oldFont);
        gfx.setColor(oldColor);
        gfx.setPaint(oldPaint);
      }
    }
  }

  private static double normalizeAngle(final double angle) {
    return angle - TWO_PI * Math.floor(angle / TWO_PI);
  }


  private boolean isOnAnySide(final PreferredPlacementDescriptor placementDescriptor) {
    return placementDescriptor.getSideOfEdge() ==
           (PreferredPlacementDescriptor.PLACE_LEFT_OF_EDGE |
            PreferredPlacementDescriptor.PLACE_ON_EDGE |
            PreferredPlacementDescriptor.PLACE_RIGHT_OF_EDGE);
  }

  private boolean isLabelLeftOfEdgeInFlow(final PreferredPlacementDescriptor placementDescriptor, final double edgeAngle) {
    if (placementDescriptor.isSideRelativeToEdgeFlow()) {
      return placementDescriptor.isLeftOfEdge();
    } else if (0 < edgeAngle && edgeAngle < Math.PI) {
      // edge points downwards
      return placementDescriptor.isRightOfEdge();
    } else if (Math.PI < edgeAngle && edgeAngle < 2 * Math.PI) {
      // edge points upwards
      return placementDescriptor.isLeftOfEdge();
    } else if (edgeAngle == 0) {
      // edge points to the right
      return placementDescriptor.isLeftOfEdge() ^ placementDescriptor.isSideAbsoluteWithRightInNorth();
    } else {
      // edge points to the left
      return placementDescriptor.isLeftOfEdge() ^ placementDescriptor.isSideAbsoluteWithLeftInNorth();
    }
  }

  private boolean useSelectionStyle(final YLabel label, final Graphics2D gfx) {
    return label.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
  }

  private Font getSmallFont(final Graphics2D gfx) {
    if (smallFont == null) {
      final Font font = gfx.getFont();
      smallFont = new Font(font.getName(), font.getStyle(), 6);
    }
    return smallFont;
  }
}
