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

import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.YLabel;
import y.view.YRenderingHints;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * A label painter that doesn't paint the label text but instead visualizes the label bounds by painting the
 * up-vector and anchor point as well as indicators for the width and height of the label bounds.
 */
class VisualizingBoundsLabelConfiguration extends DefaultLabelConfiguration {

  private static final Color COLOR_HEIGHT_INDICATOR = new Color(51, 102, 153);
  private static final Color COLOR_WIDTH_INDICATOR = new Color(102, 204, 51);
  private static final Color COLOR_UP_VECTOR = new Color(204, 153, 51);
  private static final Color COLOR_ANCHOR_POINT = new Color(153, 51, 51);
  private static final Color COLOR_LABEL_BACKGROUND = new Color(210, 210, 210);
  private static final Color COLOR_LABEL_SELECTED = Color.RED;

  private static final Stroke STROKE_UNSELECTED = new BasicStroke(0.5f);
  private static final Stroke STROKE_SELECTED = new BasicStroke(1f);

  private static final double DIAGONAL_THIN_STROKE_OFFSET = Math.sqrt(2) * 0.25;

  private static Font tinyFont;


  public void paintContent(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    // we don't want to paint the label text
  }

  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
    if (label instanceof EdgeLabel) {
      final Stroke oldStroke = gfx.getStroke();
      final Color oldColor = gfx.getColor();
      final Font oldFont = gfx.getFont();

      try {
        final double x2 = x + width;
        final double y2 = y + height;
        final double cx = x + width / 2;

        // we don't want to label our bounds description upside-down, so we check
        // if we have to flip vertical and/or horizontal text
        final boolean flipVerticalText = label.getOrientedBox().getUpX() < 0;
        final boolean flipHorizontalText = label.getOrientedBox().getUpY() > 0;

        // fill background of the complete label bounds
        gfx.setColor(COLOR_LABEL_BACKGROUND);
        gfx.fill(new Rectangle2D.Double(x, y, width, height));

        gfx.setStroke(STROKE_UNSELECTED);
        gfx.setFont(getTinyFont(gfx));

        // paint up vector
        gfx.setColor(COLOR_UP_VECTOR);
        gfx.draw(new Line2D.Double(x, y + 0.5, x, y2 - 0.25));
        gfx.draw(new Line2D.Double(x, y + DIAGONAL_THIN_STROKE_OFFSET, x - 2, y + 2 + DIAGONAL_THIN_STROKE_OFFSET));
        gfx.draw(new Line2D.Double(x, y + DIAGONAL_THIN_STROKE_OFFSET, x + 2, y + 2 + DIAGONAL_THIN_STROKE_OFFSET));

        // paint anchor point
        gfx.setColor(COLOR_ANCHOR_POINT);
        gfx.fill(new Ellipse2D.Double(x - 1, y2 - 1, 2, 2));

        // paint height indicator
        gfx.setColor(COLOR_HEIGHT_INDICATOR);
        gfx.draw(new Line2D.Double(cx, y + 0.5, cx, y2 - 0.5));

        gfx.draw(new Line2D.Double(cx, y + DIAGONAL_THIN_STROKE_OFFSET, cx - 2, y + 2 + DIAGONAL_THIN_STROKE_OFFSET));
        gfx.draw(new Line2D.Double(cx, y + DIAGONAL_THIN_STROKE_OFFSET, cx + 2, y + 2 + DIAGONAL_THIN_STROKE_OFFSET));

        gfx.draw(new Line2D.Double(cx, y2 - DIAGONAL_THIN_STROKE_OFFSET, cx - 2, y2 - 2 - DIAGONAL_THIN_STROKE_OFFSET));
        gfx.draw(new Line2D.Double(cx, y2 - DIAGONAL_THIN_STROKE_OFFSET, cx + 2, y2 - 2 - DIAGONAL_THIN_STROKE_OFFSET));

        if (flipVerticalText) {
          gfx.rotate(Math.PI / 2, x, y);
          gfx.drawString("height", (float) (x + 3), (float) (y - width / 2 + 4));
          gfx.rotate(-Math.PI / 2, x, y);

        } else {
          gfx.rotate(-Math.PI / 2, x, y);
          gfx.drawString("height", (float) (x - height + 4), (float) (y + width / 2 - 2));
          gfx.rotate(Math.PI / 2, x, y);
        }

        // paint width indicator
        gfx.setColor(COLOR_WIDTH_INDICATOR);
        gfx.draw(new Line2D.Double(x + 0.5, y2, x2 - 0.5, y2));

        gfx.draw(new Line2D.Double(x + DIAGONAL_THIN_STROKE_OFFSET, y2, x + 2 + DIAGONAL_THIN_STROKE_OFFSET, y2 - 2));
        gfx.draw(new Line2D.Double(x + DIAGONAL_THIN_STROKE_OFFSET, y2, x + 2 + DIAGONAL_THIN_STROKE_OFFSET, y2 + 2));

        gfx.draw(new Line2D.Double(x2 - DIAGONAL_THIN_STROKE_OFFSET, y2, x2 - 2 - DIAGONAL_THIN_STROKE_OFFSET, y2 - 2));
        gfx.draw(new Line2D.Double(x2 - DIAGONAL_THIN_STROKE_OFFSET, y2, x2 - 2 - DIAGONAL_THIN_STROKE_OFFSET, y2 + 2));

        if (flipHorizontalText) {
          gfx.rotate(-Math.PI, x + width / 2, y + height);
          gfx.drawString("width", (float) (x + width / 2 - 5), (float) (y + height));
          gfx.rotate(Math.PI, x + width / 2, y + height);
        } else {
          gfx.drawString("width", (float) (x + width / 2 - 5), (float) (y + height + 3));
        }

        // draw selection box
        if (useSelectionStyle(label, gfx)) {
          gfx.setStroke(STROKE_SELECTED);
          gfx.setColor(COLOR_LABEL_SELECTED);
          gfx.draw(new Rectangle2D.Double(x, y, width, height));
        }
      } finally {
        gfx.setFont(oldFont);
        gfx.setStroke(oldStroke);
        gfx.setColor(oldColor);
      }
    }
  }

  private boolean useSelectionStyle(final YLabel label, final Graphics2D gfx) {
    return label.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
  }

  private static Font getTinyFont(final Graphics2D gfx) {
    if (tinyFont == null) {
      final Font font = gfx.getFont();
      tinyFont = new Font(font.getName(), font.getStyle(), 4);
    }
    return tinyFont;
  }
}
