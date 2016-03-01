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
package demo.view.flowchart.painters;

import y.view.NodeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.base.Node;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Draws the annotation symbol of flowchart diagrams.
 */
public class FlowchartAnnotationPainter extends AbstractFlowchartPainter {
  /**
   * Paints a flowchart annotation symbol.
   * @param context the context node
   * @param graphics the graphics context to use
   * @param sloppy whether to draw the node sloppily
   */
  protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
    if (initializeFill(context, graphics)) {
      graphics.fill(newShape(context));
    }
    if (initializeLine(context, graphics)) {
      graphics.draw(newDecoration(context));
    }
  }

  /**
   * Calculates the annotation outline for the specified node.
   * @param context The node context
   */
  protected Shape newShape(NodeRealizer context) {
    double x = context.getX();
    double y = context.getY();
    double width = context.getWidth();
    double height = context.getHeight();
    return new Rectangle2D.Double(x, y, width, height);
  }

  /**
   * Calculates the annotation bracket for the specified node.
   * @param context The node context
   */
  protected Shape newDecoration(NodeRealizer context) {
    byte orientation = getOrientation(context, PROPERTY_ORIENTATION_VALUE_LEFT);
    if (orientation == PROPERTY_ORIENTATION_VALUE_AUTO) {
      orientation = determineBracketOrientation(context);
    }

    switch (orientation) {
      case PROPERTY_ORIENTATION_VALUE_DOWN:
        return createDownBracket(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      case PROPERTY_ORIENTATION_VALUE_RIGHT:
        return createRightBracket(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      case PROPERTY_ORIENTATION_VALUE_TOP:
        return createTopBracket(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      case PROPERTY_ORIENTATION_VALUE_LEFT:
        return createLeftBracket(context.getX(), context.getY(), context.getWidth(), context.getHeight());
      default:
        return createLeftBracket(context.getX(), context.getY(), context.getWidth(), context.getHeight());
    }
  }

  protected byte getOrientation( NodeRealizer context, byte defaultValue ) {
    GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    Object value = gnr.getStyleProperty(PROPERTY_ORIENTATION);
    if (value instanceof Byte) {
      return ((Byte) value).byteValue();
    } else {
      return defaultValue;
    }
  }

  private Shape createLeftBracket(double x, double y, double width, double height) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo((float)(x + 0.125 * width), (float) y);
    shape.lineTo((float)x, (float)y);
    shape.lineTo((float)x, (float)(y + height));
    shape.lineTo((float)(x + 0.125 * width), (float)(y + height));
    return shape;
  }

  private Shape createRightBracket(double x, double y, double width, double height) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo((float)(x + 0.875 * width),(float) y);
    shape.lineTo((float)(x + width), (float)y);
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)(x + 0.875 * width), (float)(y + height));
    return shape;
  }

  private Shape createTopBracket(double x, double y, double width, double height) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo((float)x, (float)(y + 0.125 * height));
    shape.lineTo((float)x, (float)y);
    shape.lineTo((float)(x + width), (float)y);
    shape.lineTo((float)(x + width), (float)(y + 0.125 * height));
    return shape;
  }

  private Shape createDownBracket(double x, double y, double width, double height) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo((float)x, (float)(y + 0.875 * height));
    shape.lineTo((float)x, (float)(y + height));
    shape.lineTo((float)(x + width), (float)(y + height));
    shape.lineTo((float)(x + width), (float)(y + 0.875 * height));
    return shape;
  }

  /**
   * Returns a constant representing the orientation/placement of the
   * annotation's bracket. One of
   * <ul>
   *   <li>{@link #PROPERTY_ORIENTATION_VALUE_DOWN}</li>
   *   <li>{@link #PROPERTY_ORIENTATION_VALUE_RIGHT}</li>
   *   <li>{@link #PROPERTY_ORIENTATION_VALUE_TOP}</li>
   *   <li>{@link #PROPERTY_ORIENTATION_VALUE_LEFT}</li>
   * </ul>
   * @param context the context node
   * @return one of {@link #PROPERTY_ORIENTATION_VALUE_DOWN},
   * {@link #PROPERTY_ORIENTATION_VALUE_RIGHT},
   * {@link #PROPERTY_ORIENTATION_VALUE_TOP}, and
   * {@link #PROPERTY_ORIENTATION_VALUE_LEFT}.
   */
  private byte determineBracketOrientation(NodeRealizer context) {
    final Node node = context.getNode();
    if (node != null && node.degree() == 1) {
      final Graph2D graph = (Graph2D) node.getGraph();
      
      final Point2D intersection =
              node.inDegree() == 1
              ? graph.getRealizer(node.firstInEdge()).getTargetIntersection()
              : graph.getRealizer(node.firstOutEdge()).getSourceIntersection();

      final double x = intersection.getX();
      final double y = intersection.getY();

      final double epsilon = 0.1;

      final double minX = context.getX();
      if ((x + epsilon) > minX && (x - epsilon) < minX) {
        return PROPERTY_ORIENTATION_VALUE_LEFT;
      } else {
        final double maxX = minX + context.getWidth();
        if (((x + epsilon) > maxX && ((x - epsilon) < maxX))) {
          return PROPERTY_ORIENTATION_VALUE_RIGHT;
        } else {
          final double minY = context.getY();
          if ((y + epsilon) > minY && (y - epsilon) < minY) {
            return PROPERTY_ORIENTATION_VALUE_TOP;
          } else {
            final double maxY = minY + context.getHeight();
            if (((y + epsilon) > maxY && ((y - epsilon) < maxY))) {
              return PROPERTY_ORIENTATION_VALUE_DOWN;
            }
          }
        }
      }
    }

    return PROPERTY_ORIENTATION_VALUE_LEFT;
  }
}
