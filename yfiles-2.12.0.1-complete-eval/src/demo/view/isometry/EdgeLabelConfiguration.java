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
package demo.view.isometry;

import y.geom.YRectangle;
import y.view.DefaultLabelConfiguration;
import y.view.LineType;
import y.view.YLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * A configuration for {@link y.view.EdgeLabel}s that paints the label text and box standing up on the isometric plane.
 */
public class EdgeLabelConfiguration extends DefaultLabelConfiguration {

  /**
   * Paints the background of an isometrically transformed label.
   */
  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    final Stroke oldStroke = gfx.getStroke();
    final Color oldColor = gfx.getColor();

    // get all corners of the 3-dimensional box that has the labels width and height
    // and move them to the labels coordinates
    final IsometryData isometryData = (IsometryData) label.getUserData();
    final double[] corners = new double[16];
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(x, y, corners);

    // only draw the side of the 3-dimensional label box as background for the label
    // the correct side depends on the labels orientation
    if (isHorizontal(label)) {
      gfx.setStroke(LineType.LINE_1);
      final GeneralPath path = new GeneralPath();
      if (label.getLineColor() != null) {
        gfx.setColor(label.getLineColor());
        path.moveTo((float) corners[IsometryData.C0_X], (float) corners[IsometryData.C0_Y]);
        path.lineTo((float) corners[IsometryData.C1_X], (float) corners[IsometryData.C1_Y]);
        path.lineTo((float) corners[IsometryData.C5_X], (float) corners[IsometryData.C5_Y]);
        path.lineTo((float) corners[IsometryData.C4_X], (float) corners[IsometryData.C4_Y]);
        path.closePath();
        gfx.draw(path);
      }
      if (label.getBackgroundColor() != null) {
        gfx.setColor(label.getBackgroundColor());
        gfx.fill(path);
      }
    } else {
      gfx.setStroke(LineType.LINE_1);
      final GeneralPath path = new GeneralPath();
      if (label.getLineColor() != null) {
        gfx.setColor(label.getLineColor());
        path.moveTo((float) corners[IsometryData.C2_X], (float) corners[IsometryData.C2_Y]);
        path.lineTo((float) corners[IsometryData.C1_X], (float) corners[IsometryData.C1_Y]);
        path.lineTo((float) corners[IsometryData.C5_X], (float) corners[IsometryData.C5_Y]);
        path.lineTo((float) corners[IsometryData.C6_X], (float) corners[IsometryData.C6_Y]);
        path.closePath();
        gfx.draw(path);
      }
      if (label.getBackgroundColor() != null) {
        gfx.setColor(label.getBackgroundColor());
        gfx.fill(path);
      }
    }

    gfx.setStroke(oldStroke);
    gfx.setColor(oldColor);
  }

  /**
   * Paints the text of an isometrically transformed label.
   */
  public void paintContent(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    final AffineTransform oldTransform = gfx.getTransform();
    final Font oldFont = gfx.getFont();
    final Color oldColor = gfx.getColor();

    // get the corners of the 3-dimensional label box to determine the anchor point of the label text
    final IsometryData isometryData = (IsometryData) label.getUserData();
    final double[] corners = new double[16];
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(x, y, corners);
    final double anchorX;
    final double anchorY;
    if (isHorizontal(label)) {
      anchorX = corners[IsometryData.C0_X];
      anchorY = corners[IsometryData.C0_Y];

      // transformation to the left backside of the 3-dimensional label box
      gfx.translate(anchorX, anchorY);
      gfx.transform(new AffineTransform(new double[]{0.87, -0.5, 0, 1}));
      gfx.translate(-anchorX, -anchorY);
    } else {
      anchorX = corners[IsometryData.C1_X];
      anchorY = corners[IsometryData.C1_Y];

      // transformation to the right backside of the 3-dimensional label box
      gfx.translate(anchorX, anchorY);
      gfx.transform(new AffineTransform(new double[]{0.87, 0.5, 0, 1}));
      gfx.translate(-anchorX, -anchorY);
    }

    // paint the text with transformed graphics context
    if (label.getTextColor() != null && !"".equals(label.getText())) {
      final Insets insets = label.getInsets() == null ? YLabel.defaultInsets : label.getInsets();
      final float descent = new TextLayout(label.getText(), label.getFont(), gfx.getFontRenderContext()).getDescent();
      gfx.setFont(label.getFont());
      gfx.setColor(label.getTextColor());
      gfx.drawString(label.getText(), (float) anchorX + insets.left, (float) anchorY - insets.bottom - descent);
    }

    gfx.setTransform(oldTransform);
    gfx.setFont(oldFont);
    gfx.setColor(oldColor);
  }

  /**
   * Calculates the bounding box of the isometric projection of the 3-dimensional label box
   * and updates the given rectangle.
   * <p>
   *   As this demo does not allow selecting or changing labels, it is not important to use the actual bounds of the
   *   isometric label.
   * </p>
   */
  public void calcUnionRect(final YLabel label, final Rectangle2D rectangle) {
    final IsometryData isometryData = (IsometryData) label.getUserData();
    final double[] corners = new double[16];
    isometryData.calculateCorners(corners);
    final YRectangle box = label.getBox();
    IsometryData.moveTo(box.getX(), box.getY(), corners);
    IsometryData.calculateViewBounds(corners, rectangle);
  }

  /**
   * Calculates and sets the content size of the label using the bounds of the isometric projection of the 3-dimensional
   * label box.
   * <p>
   *   As this demo does not allow selecting or changing labels, it is not important to use the actual bounds of the
   *   isometric label.
   * </p>
   */
  public void calculateContentSize(YLabel label, FontRenderContext frc) {
    IsometryData isometryData = (IsometryData) label.getUserData();
    final Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, -1, -1);
    isometryData.calculateViewBounds(bounds);
    label.setContentSize(bounds.getWidth(), bounds.getHeight());
  }

  /**
   * Returns whether or not the given coordinates lie inside the bounds of the isometric projection of the 3-dimensional
   * label box.
   * <p>
   *   As this demo does not allow selecting or changing labels, it is not important to check the actual bounds of the
   *   isometric label.
   * </p>
   */
  public boolean contains(YLabel label, double x, double y) {
    final Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, -1, -1);
    calcUnionRect(label, rectangle);
    return rectangle.contains(x, y);
  }

  /**
   * Determines if the label is horizontal in layout space.
   */
  private boolean isHorizontal(YLabel label) {
    final Object userData = label.getUserData();
    return !(userData instanceof IsometryData) || ((IsometryData) userData).isHorizontal();
  }
}
