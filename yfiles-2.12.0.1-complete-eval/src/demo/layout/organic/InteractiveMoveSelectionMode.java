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
package demo.layout.organic;

import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2DView;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.layout.organic.InteractiveOrganicLayouter;

/**
 * This moveSelection mode allows the user to easily drag nodes around.
 */
public class InteractiveMoveSelectionMode extends MoveSelectionMode {
  private InteractiveOrganicLayouter layouter;

  public InteractiveMoveSelectionMode( InteractiveOrganicLayouter layouter ) {
    if ( layouter == null ) throw new IllegalArgumentException( "layouter must not be null" );
    this.layouter = layouter;
  }

  /**
   * Called when the dragging has started.
   * The node is locked and the position is updated.
   */
  protected void selectionMoveStarted( double x, double y ) {
    view.setDrawingMode( Graph2DView.NORMAL_MODE );

    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );

      layouter.setInertia( node, 1 );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * Called while the node is dragged.
   */
  protected void selectionOnMove( double dx, double dy, double x, double y ) {
    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * When the dragging ends.
   * The lock on the node is removed
   */
  protected void selectionMovedAction( double dx, double dy, double x, double y ) {
    for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
      Node node = nodeCursor.node();
      NodeRealizer realizer = getGraph2D().getRealizer( node );
      layouter.setCenter( node, realizer.getCenterX(), realizer.getCenterY() );

      layouter.setInertia( node, 0 );
      increaseNeighborsHeat( node );
    }
    layouter.wakeUp();
  }

  /**
   * Increases the neighbors heat
   * @param originalNode
   */
  protected void increaseNeighborsHeat( Node originalNode ) {
    //Increase Heat of neighbors
    for ( NodeCursor cursor = originalNode.neighbors(); cursor.ok(); cursor.next() ) {
      Node neighbor = cursor.node();

      double oldStress = layouter.getStress( neighbor );
      layouter.setStress( neighbor, Math.min( 1, oldStress + 0.5 ) );
    }
  }
}
