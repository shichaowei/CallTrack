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
package demo.view.advanced.ports;

import y.base.Node;
import y.view.CreateEdgeMode;

/**
 * Custom <code>CreateEdgeMode</code> implementation that restricts edge
 * creation to nodes with node ports. 
 *
 */
class PortCreateEdgeMode extends CreateEdgeMode {
  /**
   * Overwritten to accept only nodes with node ports.
   * @param source the node to check.
   * @param x the x-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @param y the y-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @return <code>true<code> if the specified node has node ports;
   * <code>false</code> otherwise.
   */
  protected boolean acceptSourceNode( final Node source, final double x, final double y ) {
    return hasPorts(source);
  }

  /**
   * Overwritten to accept only nodes with node ports.
   * @param target the node to check.
   * @param x the x-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @param y the y-coordinate of the mouse event that triggered edge creation
   * for the specified node.
   * @return <code>true<code> if the specified node has node ports;
   * <code>false</code> otherwise.
   */
  protected boolean acceptTargetNode( final Node target, final double x, final double y ) {
    return hasPorts(target);
  }

  private boolean hasPorts( final Node node ) {
    return view.getGraph2D().getRealizer(node).portCount() > 0;
  }
}
