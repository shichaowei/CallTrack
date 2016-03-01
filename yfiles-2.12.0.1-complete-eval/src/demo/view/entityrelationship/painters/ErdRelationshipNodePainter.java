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

import demo.view.flowchart.painters.FlowchartDecisionPainter;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * This is a painter to display a relationship node for entity relationship diagrams (ERD).
 *
 * A relationship is represented by a diamond shape. It is possible to display a weak
 * relationship by drawing a double border.
 */
public class ErdRelationshipNodePainter extends FlowchartDecisionPainter {
  private static final double EPSILON = 1.0e-12;

  /**
   * Calculates the interior shape for the specified node.
   * @param context The node context
   */
  protected Shape newDecoration( final NodeRealizer context ) {
    if (!hasDoubleBorder(context)) {
      return null;
    }

    final double width = context.getWidth();
    final double height = context.getHeight();
    if (Math.abs(width) < EPSILON || Math.abs(height) < EPSILON) {
      return null;
    }

    final double x = context.getX();
    final double y = context.getY();

    final LineType lineType = context.getLineType();
    final float lw = lineType.getLineWidth();

    final double offset = 2 + lw;

    final double w2 = width * 0.5;
    final double h2 = height * 0.5;

    // slope vector  s = (w / 2,   h / 2)
    // normal vector n = (h / 2, - w / 2)
    // length of normal vector
    final double nl = Math.sqrt(w2 * w2 + h2 * h2);

    // origin of line parallel to 0 + t*s with distance offset
    final double ox =  offset * h2 / nl;
    final double oy = -offset * w2 / nl;

    // intersection of line o + t*s with y == 0
    final double ix = ox + (-oy / h2) * w2;
    // intersection of line o + t*s with x == w / 2
    final double iy = oy + (1 -ox / w2) * h2;

    final double offsetX = ix;
    final double offsetY = h2 - iy;

    if (offsetX + lw < w2 && offsetY + lw < h2) {
      final GeneralPath shapePath = new GeneralPath();
      shapePath.moveTo((float)(x + w2), (float)(y + offsetY));
      shapePath.lineTo((float)(x + width - offsetX), (float)(y + h2));
      shapePath.lineTo((float)(x + w2), (float)(y + height - offsetY));
      shapePath.lineTo((float)(x + offsetX) , (float)(y + h2));
      shapePath.closePath();
      return shapePath;
    } else {
      return null;
    }
  }

  /**
   * Tests if the style property {@link ErdRealizerFactory#DOUBLE_BORDER} is set for the context realizer.
   * @param context The context node
   * @return <code>true</code>, if style property border is set, <code>false</code> otherwise
   */
  protected boolean hasDoubleBorder(NodeRealizer context) {
    return Boolean.TRUE.equals(((GenericNodeRealizer) context).getStyleProperty(ErdRealizerFactory.DOUBLE_BORDER));
  }
}
