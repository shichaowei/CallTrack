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

import y.view.AbstractCustomNodePainter;
import y.view.NodeRealizer;
import y.view.GenericNodeRealizer;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.GradientPaint;

/**
 * Abstract painter class for flowchart symbols.
 */
public abstract class AbstractFlowchartPainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest, FlowchartRealizerConstants {
  /**
   * Paints outline shape and interior decoration of a flowchart symbol.
   * @param context the context node
   * @param graphics the graphics context to use
   * @param sloppy whether to draw the node sloppily
   */
  protected void paintNode(NodeRealizer context, Graphics2D graphics, boolean sloppy) {
    final Shape outline = newShape(context);
    if (initializeFill(context, graphics)) {
      graphics.fill(outline);
    }
    if (initializeLine(context, graphics)) {
      graphics.draw(outline);

      final Shape decoration = newDecoration(context);
      if (decoration != null) {
        graphics.draw(decoration);
      }
    }
  }

  protected Paint getFillPaint( final NodeRealizer context, final boolean selected ) {
    final Color fc1 = getFillColor(context, selected);
    if (fc1 != null) {
      final Color fc2 = getFillColor2(context, selected);
      if (fc2 != null) {
        final double x = context.getX();
        final double y = context.getY();
        return new GradientPaint(
                (float) x, (float) y, fc1,
                (float)(x + context.getWidth()), (float) (y + context.getHeight()), fc2,
                true);
      } else {
        return fc1;
      }
    } else {
      return null;
    }
  }

  /**
   * Calculates the outline shape for the specified node.
   * @param context The node context
   */
  protected abstract Shape newShape(NodeRealizer context);

  /**
   * Calculates the interior decoration for the specified node.
   * @param context The node context
   */
  protected Shape newDecoration(NodeRealizer context) {
    return null;
  }

  public boolean contains(NodeRealizer context, double x, double y) {
    return newShape(context).contains(x, y);
  }


  protected double getBorderDistance(NodeRealizer context, double defaultValue) {
    GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    Object value = gnr.getStyleProperty(PROPERTY_BORDER_DISTANCE);
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else {
      return defaultValue;
    }
  }

  protected double getRadius(NodeRealizer context, double defaultValue) {
    GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    Object value = gnr.getStyleProperty(PROPERTY_RADIUS);
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    } else {
      return defaultValue;
    }
  }
}
