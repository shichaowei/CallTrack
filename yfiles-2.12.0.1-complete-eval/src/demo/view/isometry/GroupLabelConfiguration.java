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

import y.base.Node;
import y.view.DefaultLabelConfiguration;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.YLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A painter that paints group node labels in an isometric fashion.
 */
public class GroupLabelConfiguration extends DefaultLabelConfiguration {

  /**
   * Paints the box of the label with the height of the label and the width of its group node at the bottom of the group
   * node.
   */
  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    // Save the original graphics context and restore it at the end of the method.
    final AffineTransform oldTransform = gfx.getTransform();
    final Color oldColor = gfx.getColor();

    // Calculate the corners of the node in the view space.
    final double[] corners = new double[16];
    final Node group = ((NodeLabel) label).getNode();
    final NodeRealizer realizer = ((Graph2D) group.getGraph()).getRealizer(group);
    final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(realizer);
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(realizer.getX(), realizer.getY(), corners);

    // The lower corner is the anchor of the label.
    // Note: this label configuration does not take label models into account
    final double anchorX = corners[IsometryData.C3_X];
    final double anchorY = corners[IsometryData.C3_Y];

    // Set the transformation from the layout space into the view space on the graphics context.
    gfx.translate(anchorX, anchorY);
    gfx.transform(
        new AffineTransform(
            new double[]{
                IsometryData.M_TO_VIEW_11,
                IsometryData.M_TO_VIEW_21,
                IsometryData.M_TO_VIEW_12,
                IsometryData.M_TO_VIEW_22}));
    gfx.translate(-anchorX, -anchorY);

    // Calculate the box of the label in the layout space. It uses the whole width of the node.
    final Rectangle2D.Double rect = new Rectangle2D.Double(
        anchorX,
        anchorY - height,
        isometryData.getWidth(),
        height);

    // Paint the box of the label with the transformed graphics context.
    gfx.setColor(label.getBackgroundColor());
    gfx.fill(rect);
    gfx.setColor(label.getLineColor());
    gfx.draw(rect);

    // Restore the original graphics context.
    gfx.setTransform(oldTransform);
    gfx.setColor(oldColor);
  }

  /**
   * Paints the text of the label at the lower left corner of the group node.
   */
  public void paintContent(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    // Save the original graphics context and restore it at the end of the method.
    final AffineTransform oldTransform = gfx.getTransform();
    final Font oldFont = gfx.getFont();
    final Color oldColor = gfx.getColor();

    // Calculate the corners of the node in the view space.
    final double[] corners = new double[16];
    final Node group = ((NodeLabel) label).getNode();
    final NodeRealizer realizer = ((Graph2D) group.getGraph()).getRealizer(group);
    final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(realizer);
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(realizer.getX(), realizer.getY(), corners);

    // The lower corner is the anchor of the label.
    // Note: this label configuration does not take label models into account
    final double anchorX = corners[IsometryData.C3_X];
    final double anchorY = corners[IsometryData.C3_Y];

    // Set the transformation from the layout space into the view space on the graphics context.
    gfx.translate(anchorX, anchorY);
    gfx.transform(new AffineTransform(
        new double[]{
            IsometryData.M_TO_VIEW_11,
            IsometryData.M_TO_VIEW_21,
            IsometryData.M_TO_VIEW_12,
            IsometryData.M_TO_VIEW_22}));
    gfx.translate(-anchorX, -anchorY);

    // Draw the label text with the transformed graphics context.
    // It is placed on the bottom right side of the node.
    if (label.getTextColor() != null && !"".equals(label.getText())) {
      gfx.setFont(label.getFont());
      gfx.setColor(label.getTextColor());
      final Insets insets = label.getInsets() == null ? YLabel.defaultInsets : label.getInsets();
      final float descent = new TextLayout(label.getText(), label.getFont(), gfx.getFontRenderContext()).getDescent();
      gfx.drawString(label.getText(), (float) (anchorX + isometryData.getWidth() - label.getContentWidth() - insets.right), (float) anchorY - insets.bottom - descent);
    }

    // Restore the original graphics context.
    gfx.setTransform(oldTransform);
    gfx.setFont(oldFont);
    gfx.setColor(oldColor);
  }
}
