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

import demo.view.flowchart.painters.FlowchartProcessPainter;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * This is a painter to display small entity node for entity relationship diagrams (ERD).
 *
 * A small entity is used in the Chen notation and is displayed as a rectangle with a label.
 * Weak entities are drawn with a double border.
 */
public class ErdSmallEntityNodePainter extends FlowchartProcessPainter {
  /**
   * Calculates the interior shape for the specified node.
   * @param context The node context
   */
  protected Shape newDecoration( final NodeRealizer context ) {
    if (hasDoubleBorder(context)) {
      final LineType lineType = context.getLineType();
      final float lw = lineType.getLineWidth();

      final double offset = 2 + lw;

      final double x = context.getX();
      final double y = context.getY();
      final double width = context.getWidth();
      final double height = context.getHeight();

      if (offset + lw < width * 0.5 && offset + lw < height * 0.5) {
        return new Rectangle2D.Double(x + offset, y + offset, width - 2 * offset, height - 2 * offset);
      } else {
        return new Rectangle2D.Double(x, y, 0, 0);
      }
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
