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
package demo.view.entityrelationship.painters;

import y.geom.OrientedRectangle;
import y.view.NodeLabel;
import y.view.YLabel;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.util.Map;

/**
 * This configuration paints ERD attribute labels for detailed entities.
 *
 * The label uses the whole space of the lower compartment. So the size is retrieved from
 * the oriented box instead of the content box. Also the the contains test is using this size.
 */
public class ErdAttributesLabelConfiguration implements YLabel.Layout, YLabel.Painter {
  private final YLabel.Layout layout;
  private final YLabel.Painter painter;

  public ErdAttributesLabelConfiguration() {
    this(defaultLayout(), ErdNameLabelPainter.defaultPainter());
  }

  /**
   * Creates a new <code>ErdAttributesLabelConfiguration</code>.
   * @param layout the layout of the label
   * @param painter the painter of the label
   */
  public ErdAttributesLabelConfiguration(
          final YLabel.Layout layout,
          final YLabel.Painter painter
  ) {
    this.layout = layout;
    this.painter = painter;
  }

  /**
   * Paints the ERD attributes label.
   * @param label the label context
   * @param gfx the graphics object
   */
  public void paint( final YLabel label, final Graphics2D gfx ) {
    if (painter != null) {
      final OrientedRectangle cb = label.getContentBox();
      if (cb.getUpY() == -1) {
        // Use the oriented box size to paint the box,
        // so the label is as wide as the node
        final OrientedRectangle ob = label.getOrientedBox();
        final double h = ob.getHeight();
        final double x = ob.getAnchorX();
        final double y = ob.getAnchorY() - h;
        paintBox(label, gfx, x, y, ob.getWidth(), h);
        paintContent(label, gfx, x, y, cb.getWidth(), cb.getHeight());
      } else {
        painter.paint(label, gfx);
      }
    }
  }

  /**
   * Draws the content of the attributes label. In this case it is a string containing the attributes.
   * @param label the label context
   * @param gfx the graphics object
   * @param x the x-coordinate of the label
   * @param y the y-coordinate of the label
   * @param width the width of the label
   * @param height the height of the label
   */
  public void paintContent(
          final YLabel label,
          final Graphics2D gfx,
          final double x,
          final double y,
          final double width,
          final double height
  ) {
    if (painter != null) {
      painter.paintContent(label, gfx, x, y, width, height);
    }
  }

  /**
   * Paints the background of the attributes label.
   * @param label the label context
   * @param gfx the graphics object
   * @param x the x-coordinate of the label
   * @param y the y-coordinate of the label
   * @param width the width of the label
   * @param height the height of the label
   */
  public void paintBox(
          final YLabel label,
          final Graphics2D gfx,
          final double x,
          final double y,
          final double width,
          final double height
  ) {
    if (painter != null) {
      painter.paintBox(label, gfx, x, y, width, height);
    }
  }

  /**
   * Returns the text box of the current painter.
   * @param label the label context
   * @return a rectangle with the size and position of the text box
   */
  public OrientedRectangle getTextBox( final YLabel label ) {
    if (painter != null) {
      return painter.getTextBox(label);
    } else {
      return null;
    }
  }

  /**
   * Returns the icon box of the current painter.
   * @param label the label context
   * @return a rectangle with the size and position of the icon box
   */
  public OrientedRectangle getIconBox( final YLabel label ) {
    if (painter != null) {
      return painter.getIconBox(label);
    } else {
      return null;
    }
  }

  /**
   * Calculates the size of the label content.
   * @param label the label context
   * @param frc the font render context.
   */
  public void calculateContentSize(
          final YLabel label,
          final FontRenderContext frc
  ) {
    if (layout != null) {
      layout.calculateContentSize(label, frc);
    }
  }

  /**
   * Determines if the coordinates <code>(x,y)</code> lie within the label box.
   * @param label the label context.
   * @param x x-coordinate
   * @param y y-coordinate
   * @return <code>true</code> if the label box contains the coordinates (x,y), <code>false</code> otherwise
   */
  public boolean contains(
          final YLabel label,
          final double x,
          final double y
  ) {
    // Use the oriented box instead of the content box, so one can
    // click on the whole compartment to select the label
    return label.getOrientedBox().contains(x, y, true);
  }


  /**
   * Retrieves a suitable default layout for this configuration.
   * @return the default layout or <code>null</code> if no default layout is set
   */
  static YLabel.Layout defaultLayout() {
    final YLabel.Factory factory = NodeLabel.getFactory();
    final Map c = factory.createDefaultConfigurationMap();
    final Object layout = c.get(YLabel.Layout.class);
    if (layout instanceof YLabel.Layout) {
      return (YLabel.Layout) layout;
    } else {
      return null;
    }
  }
}

