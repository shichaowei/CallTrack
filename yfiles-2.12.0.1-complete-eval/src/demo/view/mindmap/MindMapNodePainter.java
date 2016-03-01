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

import demo.view.mindmap.StateIconProvider.StateIcon;

import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.YRenderingHints;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

/**
 * Implementation of {@link y.view.GenericNodeRealizer.Painter} and {@link y.view.GenericNodeRealizer.ContainsTest}
 * for mind map items.
 */
class MindMapNodePainter implements GenericNodeRealizer.Painter{
  private static final String KEY_STATE_ICON = "MindMapNodePainter.StateIcon";

  /**
   * Paints a node with just an underline and its label on top of this line
   * @param context current <code>NodeRealizer</code>
   * @param graphics current <code>Graphics2D</code>
   */
  public void paint(final NodeRealizer context, Graphics2D graphics) {
    paintImpl(context, graphics, false);
  }

  /**
   * Paints a node with few details.
   * @param context current <code>NodeRealizer</code>
   * @param graphics current <code>Graphics2D</code>
   */
  public void paintSloppy(final NodeRealizer context, final Graphics2D graphics) {
    paintImpl(context, graphics, true);
  }

  private void paintImpl(final NodeRealizer context, Graphics2D graphics, final boolean sloppy) {
    graphics = (Graphics2D) graphics.create();

    final Color fc = context.getFillColor();
    if (fc != null) {
      final double x = context.getX();
      final double y = context.getY() + context.getHeight();
      graphics.setColor(fc);
      graphics.setStroke(context.getLineType());
      graphics.draw(new Line2D.Double(x, y, x + context.getWidth(), y));
    }

    final Color lc = context.getLineColor();
    if (lc != null && context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(graphics)) {
      graphics.setColor(lc);
      graphics.setStroke(LineType.LINE_1);
      graphics.draw(context.getBoundingBox());
    }

    if (!sloppy) {
      final StateIcon icon = getStateIcon(context);
      if (icon != null) {
        int xoffset = 0;
        if (ViewModel.instance.isLeft(context.getNode())) {
          xoffset = (int) context.getWidth() - 16;
        }
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.translate(context.getX() + xoffset, context.getY());
        icon.paintIcon(null, graphics, 0, 0);
      }
    }
    graphics.dispose();
  }

  /**
   * Returns the state icon for the specified node realizer.
   * @param nr the visual representation of a node in the mind map. 
   * @return the state icon for the specified node realizer or <code>null</code>
   * if the specified node realizer does not display a state icon.
   */
  static StateIcon getStateIcon( final NodeRealizer nr ) {
    if (nr instanceof GenericNodeRealizer) {
      final Object name = ((GenericNodeRealizer) nr).getStyleProperty(KEY_STATE_ICON);
      if (name instanceof String) {
        return StateIconProvider.instance.getIcon((String) name);
      }
    }
    return null;
  }

  /**
   * Specifies the state icon for the specified node realizer.
   * Does nothing if the specified node realizer is not an instance of
   * {@link GenericNodeRealizer}.
   * @param nr the visual representation of a node in the mind map.
   * @param icon the state icon to display.
   */
  static void setStateIcon( final NodeRealizer nr, final StateIcon icon ) {
    if (nr instanceof GenericNodeRealizer) {
      if (icon == null || icon.getName() == null) {
        ((GenericNodeRealizer) nr).removeStyleProperty(KEY_STATE_ICON);
      } else {
        ((GenericNodeRealizer) nr).setStyleProperty(KEY_STATE_ICON, icon.getName());
      }
    }
  }
}
