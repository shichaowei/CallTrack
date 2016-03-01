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

import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import y.view.Graph2D;
import y.base.Node;
import y.base.EdgeCursor;
import y.base.Edge;

/**
 * The {@link y.view.GenericNodeRealizer.LayerHandler} implementation for flowchart annotation node.
 * The implementation puts the annotation symbol in the {@link y.view.Graph2DView#FG_LAYER foreground layer},
 * if the edges of the annotation node are located in the foreground layer. This ensures the repainting of the symbol,
 * if edges (or neighbour nodes) has been moved.
 */
public class FlowchartAnnotationLayerHandler implements GenericNodeRealizer.LayerHandler {

  private static final String LAYER_STYLE_PROPERTY_KEY = "LAYER_STYLE_PROPERTY_KEY";

  /**
   * Sets the logical graphical layer for this realizer. Layer information can be used by viewers to optimize redraws.
   *
   * @see y.view.Graph2DView#FG_LAYER
   * @see y.view.Graph2DView#BG_LAYER
   */
  public void setLayer(NodeRealizer context, byte l) {
    if (l == y.view.Graph2DView.FG_LAYER) {
      ((GenericNodeRealizer) context).removeStyleProperty(LAYER_STYLE_PROPERTY_KEY);
    } else {
      ((GenericNodeRealizer)context).setStyleProperty(LAYER_STYLE_PROPERTY_KEY, new Byte(l));
    }
  }

  /**
   * Returns the logical graphical layer for this realizer. Layer information can be used by viewers to optimize
   * redraws.
   *
   * @see y.view.Graph2DView#FG_LAYER
   * @see y.view.Graph2DView#BG_LAYER
   */
  public byte getLayer(NodeRealizer context) {
    final Node node = context.getNode();
    final Graph2D graph2D = (Graph2D) node.getGraph();
    for (EdgeCursor edgeCursor = node.edges(); edgeCursor.ok(); edgeCursor.next()) {
      Edge edge = edgeCursor.edge();
      if (graph2D.getRealizer(edge).getLayer() == y.view.Graph2DView.FG_LAYER) {
        return y.view.Graph2DView.FG_LAYER;
      }
    }
    if (context instanceof GenericNodeRealizer && ((GenericNodeRealizer)context).getStyleProperty(LAYER_STYLE_PROPERTY_KEY) instanceof Byte){
      Byte layer = (Byte) ((GenericNodeRealizer)context).getStyleProperty(LAYER_STYLE_PROPERTY_KEY);
      return layer.byteValue();
    } else {
      return y.view.Graph2DView.FG_LAYER;
    }
  }
}
