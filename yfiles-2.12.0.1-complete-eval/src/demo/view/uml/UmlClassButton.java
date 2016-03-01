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
package demo.view.uml;

import y.view.AbstractMouseInputEditor;
import y.view.Graph2DView;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.NodeRealizer;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Base class for UML class button implementations.
 * Subclasses must implement the method {@link #getButtonArea(y.view.NodeRealizer)} to specify the size and the position
 * of the button. The implementation of {@link #paint(y.view.NodeRealizer, java.awt.Graphics2D)} draws the button in
 * that area.
 * To react on mouse interactions subclasses can implement the following methods:
 * <ul>
 *   <li>{@link #buttonClicked(y.view.NodeRealizer, y.view.Graph2DView)}</li> is called when the mouse clicks within the button.
 *   <li>{@link #buttonEntered(y.view.NodeRealizer, y.view.Graph2DView)}</li> is called when the mouse enters the button.
 *   <li>{@link #buttonExited(y.view.NodeRealizer, y.view.Graph2DView)}</li> is called when the mouse leaves the button.
 * </ul>
 */
abstract class UmlClassButton {
  /**
   * Paints the button.
   */
  public abstract void paint(final NodeRealizer context, final Graphics2D graphics);

  /**
   * Checks whether or not the button is visible.
   */
  protected abstract boolean isVisible(NodeRealizer context);

  /**
   * Checks if the given point is within the button.
   */
  public boolean contains(final NodeRealizer context, final double x, final double y) {
    return isVisible(context) && getButtonArea(context).contains(x, y);
  }

  /**
   * Returns the size and the position of the button.
   */
  protected abstract Rectangle2D getButtonArea(NodeRealizer context);

  /**
   * Could be overwritten to react when the mouse clicks within the button.
   */
  protected void buttonClicked(NodeRealizer context, Graph2DView view) {}

  /**
   * Could be overwritten to react when the mouse enters the button.
   */
  protected void buttonEntered(NodeRealizer context, Graph2DView view) {}

  /**
   * Resets an appropriate style property of the given context to notify that the mouse is currently not over this
   * button.
   */
  protected void buttonExited(NodeRealizer context, Graph2DView view) {
    UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_NONE);
    view.updateView(getButtonArea(context));
  }

  /**
   * Returns an {@link MouseInputEditor} to handle mouse events for the button.
   */
  public MouseInputEditor getMouseInputEditor(final NodeRealizer context, final Graph2DView view) {
    return new ButtonMouseInputEditor(context, view);
  }

  /**
   * An {@link MouseInputEditor} to handle mouse events for the button. The editor gets mouse events and forwards them
   * to the following methods:
   * <ul>
   *   <li>{@link #buttonClicked(y.view.NodeRealizer, y.view.Graph2DView)}</li>
   *   <li>{@link #buttonEntered(y.view.NodeRealizer, y.view.Graph2DView)}</li>
   *   <li>{@link #buttonExited(y.view.NodeRealizer, y.view.Graph2DView)}</li>
   * </ul>
   * when the mouse event is within the button.
   */
  private class ButtonMouseInputEditor extends AbstractMouseInputEditor {
    private final NodeRealizer context;
    private final Graph2DView view;

    private ButtonMouseInputEditor(final NodeRealizer context, final Graph2DView view) {
      this.context = context;
      this.view = view;
    }

    public boolean startsEditing(final Mouse2DEvent event) {
      return isDetailed() && contains(context, event.getX(), event.getY());
    }

    /**
     * Overridden to notify when the mouse enters the button.
     */
    public void startEditing() {
      if (isDetailed()) {
        buttonEntered(context, view);
        super.startEditing();
      }
    }

    /**
     * Overridden to notify when the mouse leaves the button.
     */
    public void stopEditing() {
      if (isDetailed()) {
        buttonExited(context, view);
        super.stopEditing();
      }
    }

    /**
     * Overridden to notify when the mouse clicks the button.
     */
    public void mouse2DEventHappened(final Mouse2DEvent event) {
      if (!contains(context, event.getX(), event.getY())) {
        stopEditing();
        return;
      }

      if ((event.getId() == Mouse2DEvent.MOUSE_CLICKED) && isDetailed()) {
        buttonClicked(context, view);
        stopEditing();
      }
    }

    private boolean isDetailed() {
      return view.getZoom() > view.getPaintDetailThreshold();
    }
  }
}
