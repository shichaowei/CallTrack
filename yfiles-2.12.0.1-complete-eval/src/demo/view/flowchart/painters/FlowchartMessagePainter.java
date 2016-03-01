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
 * Draws the message symbols of flowchart diagrams.
 * There are two types of messages: user messages and network messages.
 */
public class FlowchartMessagePainter extends AbstractFlowchartPainter{

  /**
   * Determines if this painter will paint a flowchart user message symbol
   * or a flowchart network message symbol.
   */
  private final boolean isUserMessage;

  /**
   * Initializes a new <code>FlowchartMessagePainter</code> instance for
   * user message or network message visualization.
   * @param type if <code>true</code>, this painter will paint the user message
   * symbol; otherwise it will paint the network message symbol.
   */
  public FlowchartMessagePainter( boolean type ) {
    isUserMessage = type;
  }

  protected Shape newShape( NodeRealizer context ) {
    double x = context.getX();
    double y = context.getY();
    double height = context.getHeight();
    double width = context.getWidth();
    double borderDistance = Math.min(FLOWCHART_DEFAULT_MESSAGE_INCLINATION *height, FLOWCHART_DEFAULT_MESSAGE_INCLINATION *width);

    GeneralPath shapePath = new GeneralPath();
    if (isUserMessage) {
      shapePath.moveTo((float) x, (float) y);
      shapePath.lineTo((float) (x + width - borderDistance), (float) y);
      shapePath.lineTo((float) (x + width), (float) (y + height*0.5));
      shapePath.lineTo((float) (x + width - borderDistance), (float)(y + height));
      shapePath.lineTo((float) x, (float) (y + height));
      shapePath.closePath();
    } else { // is network message
      shapePath.moveTo((float) x, (float) y);
      shapePath.lineTo((float) (x + width), (float) y);
      shapePath.lineTo((float) (x + width), (float) (y + height));
      shapePath.lineTo((float) x, (float) (y + height));
      shapePath.lineTo((float) (x + borderDistance), (float) (y + height*0.5));
      shapePath.closePath();
    }
    return shapePath;
  }
}
