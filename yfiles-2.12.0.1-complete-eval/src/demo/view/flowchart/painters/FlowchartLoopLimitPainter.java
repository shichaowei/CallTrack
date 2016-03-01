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

import java.awt.Shape;
import java.awt.geom.GeneralPath;

/**
 * Draws the loop limit symbol of flowchart diagrams.
 */
public class FlowchartLoopLimitPainter extends AbstractFlowchartPainter {

  /**
   * Determines if this painter will paint a flowchart loop start symbol
   * or a flowchart loop end symbol.
   */
  private final boolean isLoopStart;

  /**
   * Initializes a new <code>FlowchartLoopLimitPainter</code> instance
   * for loop start visualization.
   */
  public FlowchartLoopLimitPainter() {
    this(true);
  }

  /**
   * Initializes a new <code>FlowchartLoopLimitPainter</code> instance
   * for loop start or loop end visualization.
   * @param isStart if <code>true</code>, this painter will paint the flowchart
   * loop start symbol; otherwise it will paint the flowchart loop end symbol.
   */
  public FlowchartLoopLimitPainter(boolean isStart) {
    this.isLoopStart = isStart;
  }

  protected Shape newShape( NodeRealizer context ) {
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    double borderDistance = Math.min(Math.min(10, width/2), height/2);

    GeneralPath shape = new GeneralPath();
    if (isLoopStart) {
      shape.moveTo((float)(x + borderDistance), (float)y);
      shape.lineTo((float)(x + width - borderDistance), (float)y);
      shape.lineTo((float)(x+ width), (float)(y + borderDistance));
      shape.lineTo((float)(x + width), (float)(y + height));
      shape.lineTo((float)x, (float)(y + height));
      shape.lineTo((float)x, (float)(y + borderDistance));
      shape.closePath();
    } else { //is loop end
      shape.moveTo((float) x, (float) y);
      shape.lineTo((float)(x + width), (float) y);
      shape.lineTo((float)(x + width), (float)(y + height - borderDistance));
      shape.lineTo((float)(x + width - borderDistance), (float)(y + height));
      shape.lineTo((float)(x + borderDistance), (float)(y + height));
      shape.lineTo((float) x, (float)(y + height -borderDistance));
      shape.closePath();
    }
    return shape;
  }
}
