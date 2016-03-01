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

import y.geom.YRectangle;
import y.layout.NodeLabelModel;
import y.view.GenericNodeRealizer;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodePainter;
import y.view.YRenderingHints;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * This painter draws a node in ERD style that is used in Crow's Foot Notation.
 *
 * The node consists of a name label and an attributes label separated by a line
 * and different background fillings. The painter assumes that an <code>ErdAttributesNodeLabelModel</code>
 * is used for the attribute label.
 * @see ErdAttributesNodeLabelModel
 */
public class ErdNodePainter implements GenericNodeRealizer.ContainsTest, GenericNodeRealizer.Painter {
  /** Shape type constant. Specifies a rectangular shape. */
  public static final byte RECT = 0;

  /** Shape type constant. Specifies a rectangular shape whose corners are rounded. */
  public static final byte ROUND_RECT = 1;

  /** Implementation for an ErdNodePainter */
  private final ErdNodePainterImpl painter;

  /** Creates a new <code>ErdNodePainter</code> with rounded corners */
  public ErdNodePainter() {
    this(ROUND_RECT);
  }

  /**
   * Creates a new <code>ErdNodePainter</code>
   * @param type The type of the rectangle
   * @throws IllegalArgumentException If type is not <code>RECT</code> or <code>ROUND_RECT</code>
   * @see #RECT
   * @see #ROUND_RECT
   */
  public ErdNodePainter(final byte type) {
    switch (type) {
      case RECT:
      case ROUND_RECT:
        break;
      default:
        throw new IllegalArgumentException("Illegal type: " + type);
    }

    painter = new ErdNodePainterImpl(type);
  }

  /**
   * Paints the ERD entity node.
   * @param context The context node
   * @param graphics The graphics object of the component
   */
  public void paint( final NodeRealizer context, final Graphics2D graphics ) {
    painter.paint(context, graphics);
  }

  /**
   * Paints the ERD entity node in a sloppy way.
   * @param context The context node
   * @param graphics The graphics object of the component
   */
  public void paintSloppy( final NodeRealizer context, final Graphics2D graphics ) {
    painter.paintSloppy(context, graphics);
  }


  /**
   * Checks if the coordinates <code>(x,y)</code> are within the node borders.
   * @param context The context node
   * @param x The x-coordinate
   * @param y The y-coordinate
   * @return <code>true</code> if node contains (x,y), <code>false</code> otherwise
   */
  public boolean contains( final NodeRealizer context, final double x, final double y ) {
    return painter.contains(context, x, y);
  }

  /**
   * Calculates the y-coordinate of the line that separates the name and attributes label.
   * @param label Name label that is above the line
   * @return y-coordinate of the separator line
   */
  static double separator( final NodeLabel label ) {
    final YRectangle lr = label.getBox();
    return lr.getY() + lr.getHeight() + Math.max(0, label.getDistance());
  }

  /**
   * Checks if ERD style should be used.
   * That means the background color of the default label is used to paint the name compartment.
   * Additionally, a separator line between the name and the attributes compartment is drawn.
   * @param context The context node
   * @return <code>true</code> if there is a label on top of the node, <code>false</code> otherwise
   */
  static boolean useErdStyle( final NodeRealizer context ) {
    if (context.labelCount() > 0) {
      final NodeLabel nl = context.getLabel();
      if (NodeLabel.INTERNAL == nl.getModel()) {
        final byte p = nl.getPosition();
        return NodeLabel.TOP == p ||
               NodeLabel.TOP_LEFT == p  ||
               NodeLabel.TOP_RIGHT == p;
      }
    }

    return false;
  }


  /**
   * A customized {@link ShapeNodePainter} implementation for ERD nodes.
   */
  private static final class ErdNodePainterImpl extends ShapeNodePainter {

    /**
     * Creates a new <code>ErdNodePainterImpl</code>.
     * @param type The type shape for the node
     * @see #RECT
     * @see #ROUND_RECT
     */
    ErdNodePainterImpl( final byte type ) {
      super(type);
    }

    /**
     * Paints the node shape with the two labels.
     * @param context the node context
     * @param graphics the graphics object
     */
    public void paint( final NodeRealizer context, final Graphics2D graphics ) {

      // workaround for the fact that the position of the attributes label depends
      // on the position of the name label.
      if (context.labelCount() > 0) {
        for (int i = 0, n = context.labelCount(); i < n; ++i) {
          final NodeLabel nl = context.getLabel(i);
          final NodeLabelModel model = nl.getLabelModel();
          if (model instanceof ErdAttributesNodeLabelModel) {
            nl.setOffsetDirty();
          }
        }
      }

      super.paint(context, graphics);
    }

    /**
     * Paints the interior of the node
     * @param context the node context
     * @param graphics the graphics object
     * @param shape the shape to be drawn
     */
    protected void paintFilledShape(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape
    ) {
      if (useErdStyle(context)) {
        // paint in ERD style
        final NodeLabel label = context.getLabel();
        final double y = separator(label);

        final Shape oldClip = graphics.getClip();

        final Rectangle2D cb;
        if (oldClip != null) {
          cb = oldClip.getBounds2D();
        } else {
          cb = new Rectangle2D.Double(context.getX() - 10, context.getY() - 10,
              context.getWidth() + 20, context.getHeight() + 20);
        }

        // clip area for attributes on bottom and fill it with paint
        final Rectangle2D.Double rectangle = new Rectangle2D.Double();
        rectangle.setFrame(
                cb.getX(),
                y,
                cb.getWidth(),
                cb.getY() + cb.getHeight() - y);
        graphics.clip(rectangle);
        super.paintFilledShape(context, graphics, shape);
        graphics.setClip(oldClip);

        final Color oldColor = graphics.getColor();

        // clip area for entity name on top and fill it with paint
        rectangle.setFrame(
            cb.getX(),
            cb.getY(),
            cb.getWidth(),
            y - cb.getY());
        graphics.clip(rectangle);
        graphics.setColor(label.getBackgroundColor());
        graphics.fill(shape);

        graphics.setColor(oldColor);
        graphics.setClip(oldClip);
      } else {
        // if no ERD node, paint shape
        super.paintFilledShape(context, graphics, shape);
      }
    }

    /**
     * Paints border and separator of the entity node.
     * @param context  the node realizer that this painter is associated with.
     * @param graphics the graphics context.
     * @param shape    the shape that shall be painted.
     */
    protected void paintShapeBorder(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape
    ) {
      // Paint the border of the node
      super.paintShapeBorder(context, graphics, shape);

      // If ERD node, draw separator line
      if (useErdStyle(context)) {
        final double y = separator(context.getLabel());

        // Only draw line, if separator lies within node
        final double min = context.getY();
        if (min < y && y < min + context.getHeight()) {
          final double x = context.getX();

          // Draw line with line type of context
          Color lc = getLineColor(context, useSelectionStyle(context, graphics));
          if (lc != null) {
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(context.getLineType());
            graphics.setColor(lc);

            final Line2D.Double line = new Line2D.Double();
            line.setLine(x, y, x + context.getWidth(), y);
            graphics.draw(line);

            graphics.setStroke(oldStroke);
          }
        }
      }
    }

    /**
     * Retrieves the paint to fill the interior of the node.
     * @param context the context node
     * @param selected whether the node is currently selected
     * @return the current <code>Paint</code>
     */
    protected Paint getFillPaint(
            final NodeRealizer context,
            final boolean selected
    ) {
      // If there are two fill colors set, create a <code>GradientPaint</code>
      final Color fc1 = getFillColor(context, selected);
      if (fc1 != null) {
        final Color fc2 = getFillColor2(context, selected);
        if (fc2 != null) {
          final double x = context.getX();
          final double y = context.getY();

          final double _y =
                  useErdStyle(context)
                  ? separator(context.getLabel())
                  : y;

          return new GradientPaint(
                  (float) x,
                  (float) _y,
                  fc1,
                  (float) (x + context.getWidth()),
                  (float) (y + context.getHeight()),
                  fc2,
                  true);
        } else {
          return fc1;
        }
      } else {
        return null;
      }
    }

    /**
     * Determines if the selection state should be respected while painting.
     * @param context The context node
     * @param gfx graphics object
     * @return <code>true</code> if selection style is successfully applied, <code>false</code> otherwise
     */
    static boolean useSelectionStyle(
            final NodeRealizer context,
            final Graphics2D gfx
    ) {
      return context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
    }
  }
}
