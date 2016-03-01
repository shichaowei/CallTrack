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
package demo.view.mindmap;

import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.AbstractMouseInputEditor;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.MouseInputEditorProvider;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.WeakHashMap;

/**
 * Hides or unhides children of an item.
 */
class CollapseButton extends AbstractMouseInputEditor implements Drawable, MouseInputEditorProvider, MouseInputEditor {
  /**
   * The target node for this collapse button's edit action.
   */
  private final Node node;
  /**
   * The graph data structure for this collapse button's associated node.
   */
  private final Graph2D graph;

  /**
   * Initializes a new <code>CollapseButton</code> instance.
   */
  CollapseButton( final Node node, Graph2D graph ) {
    this.graph = graph;
    this.node = node;
  }

  /**
   * Returns the bounds of this collapse button in graph (world) coordinates.
   * @return the bounds of this collapse button in graph (world) coordinates.
   */
  public Rectangle getBounds() {
    Rectangle r = new Rectangle(-1,-1);
    if (isVisible()) {
      r = new Rectangle(16, 16);
      final NodeRealizer realizer = graph.getRealizer(node);
      //place the collapse button for the center item to the bottom center
      if (ViewModel.instance.isRoot(node)) {
        r.x = (int) realizer.getCenterX() - 8;
        r.y = (int) (realizer.getY() + realizer.getHeight());
        //place the collapse button depending on the side of an item
      } else {
        if (ViewModel.instance.isLeft(node)) {
          r.x = (int) (realizer.getX() - 13);
        } else {
          r.x = (int) (realizer.getX() + realizer.getWidth());
        }
        r.y = (int) (realizer.getY() + realizer.getHeight()/2+3);
      }
    }
    return r;
  }

  /**
   * Paints a white arrow in a light blue circle to represent this collapse
   * button.
   */
  public void paint(Graphics2D g) {
    final double x = this.getBounds().x;
    final double y = this.getBounds().y;
    if (isVisible()) {
      //make sure to point the arrow to the right direction
      g = (Graphics2D) g.create();
      g.translate(x,y);
      GeneralPath gp = new GeneralPath();
      //Right arrow
      if (!ViewModel.instance.isCollapsed(node) == ViewModel.instance.isLeft(node)) {
        gp.moveTo(4, 6);
        gp.lineTo(4, 10);
        gp.lineTo(8, 10);
        gp.lineTo(8, 12);
        gp.lineTo(13, 8);
        gp.lineTo(8, 4);
        gp.lineTo(8, 6);
      //Left arrow
      } else {
        gp.moveTo(12, 6);
        gp.lineTo(12, 10);
        gp.lineTo(8, 10);
        gp.lineTo(8, 12);
        gp.lineTo(3, 8);
        gp.lineTo(8, 4);
        gp.lineTo(8, 6);
      }
      final Color circleBlue = new Color(6, 164, 255);
      g.setColor(circleBlue);
      g.fillOval(0, 0, 16, 16);
      g.setColor(Color.WHITE);
      g.fill(gp);
      g.dispose();
    }
  }

  /**
   * Determines if this collapse button should be visible.
   * @return <code>true</code> if this collapse button is visible;
   * <code>false</code> otherwise.
   */
  private boolean isVisible() {
    return graph.contains(node) && 
           (ViewModel.instance.isCollapsed(node) ||
            !MindMapUtil.outEdges(node).isEmpty());
  }

  /**
   * Collapses or expands the subtree rooted at this collapse button's
   * associated node.
   */
  private void handleClick() {
    if (isVisible()) {
      MindMapUtil.toggleCollapseState(graph, node);
      graph.updateViews();
    }
  }

  /**
   * Determines if this control should be activated for the given event.
   * @param event the event that happened
   * @return <code>true</code> if the event position lies inside the bounds
   * of this control and <code>false</code> otherwise.
   */
  public boolean startsEditing(final Mouse2DEvent event) {
    return getBounds().contains(event.getX(), event.getY());
  }

  /**
   * Handles mouse events while this control is active.
   * The default implementation calls {@link #handleClick()} for mouse clicks.
   * @param event the event that happened
   */
  public void mouse2DEventHappened(final Mouse2DEvent event) {
    if (getBounds().contains(event.getX(), event.getY())) {
      if (event.getId() == Mouse2DEvent.MOUSE_CLICKED) {
        handleClick();
        stopEditing();
      }
    } else {
      stopEditing();
    }
  }

  /**
   * Returns this <code>CollapseButton</code> instance.
   * @param view the view that will host the editor
   * @param x the x-coordinate of the mouse event
   * @param y the y-coordinate of the mouse event
   * @param hitInfo the HitInfo that may be used to determine what instance to return or <code>null</code>
   * @return this <code>CollapseButton</code> instance or <code>null</code> if
   * the specified coordinates do not lie within this collapse button's bounds.
   */
  public MouseInputEditor findMouseInputEditor(Graph2DView view, double x, double y, HitInfo hitInfo) {
    return getBounds().contains(x, y) ? this : null;
  }


  /**
   * Adds and removes collapse buttons to the view whenever a node is
   * created or deleted.
   */
  static class Handler implements GraphListener {
    private final Graph2DView view;
    private final WeakHashMap node2button;

    /**
     * Initializes a new <code>Handler</code> instance for the given view.
     * @param view the view displaying the mind map.
     */
    Handler( final Graph2DView view ) {
      this.view = view;
      this.node2button = new WeakHashMap();

      view.getGraph2D().addGraphListener(this);
    }

    /**
     * Adds collapse buttons on node created/reinserted events and
     * removes collapse buttons on node removed events.
     */
    public void onGraphEvent( final GraphEvent e ) {
      switch (e.getType()) {
        case GraphEvent.NODE_CREATION:
        case GraphEvent.NODE_REINSERTION:
          onNodeCreated((Node) e.getData());
          break;
        case GraphEvent.PRE_NODE_REMOVAL:
          onNodeDeleted((Node) e.getData());
          break;
        case GraphEvent.SUBGRAPH_INSERTION:
          for (NodeCursor nc = ((NodeList) e.getData()).nodes(); nc.ok(); nc.next()) {
            onNodeCreated(nc.node());
          }
          break;
        case GraphEvent.SUBGRAPH_REMOVAL:
          for (NodeCursor nc = ((NodeList) e.getData()).nodes(); nc.ok(); nc.next()) {
            onNodeDeleted(nc.node());
          }
          break;
      }
    }

    /**
     * Adds a collapse button for the specified node to this handler's
     * associated view.
     */
    private void onNodeCreated( final Node node ) {
      final CollapseButton button = new CollapseButton(node, view.getGraph2D());
      node2button.put(node, button);
      view.addDrawable(button);
    }

    /**
     * Removes the collapse button associated to the specified node from this
     * handler's associated view.
     */
    private void onNodeDeleted( final Node node ) {
      final Object button = node2button.get(node);
      if (button instanceof CollapseButton) {
        view.removeDrawable(((CollapseButton) button));
      }
    }
  }
}
