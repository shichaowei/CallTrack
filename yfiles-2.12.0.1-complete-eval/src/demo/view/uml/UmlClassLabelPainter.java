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

import y.geom.OrientedRectangle;
import y.view.NodeLabel;
import y.view.YLabel;

import java.awt.Graphics2D;
import java.util.Map;

/**
 * Decorates a label painter to prevent selection box painting.
 */
class UmlClassLabelPainter implements YLabel.Painter {
  private final YLabel.Painter painter;

  public UmlClassLabelPainter() {
    this(defaultPainter());
  }

  public UmlClassLabelPainter(final YLabel.Painter painter) {
    this.painter = painter;
  }

  public void paint(final YLabel label, final Graphics2D gfx) {
    if (painter != null) {
      painter.paint(label, gfx);
    }
  }

  public void paintContent(
      final YLabel label,
      final Graphics2D gfx,
      final double x,
      final double y,
      final double width,
      final double height
  ) {
    if (painter != null) {
      painter.paintContent(label, gfx, x, y, width, height);
    }
  }

  public void paintBox(
      final YLabel label,
      final Graphics2D gfx,
      final double x,
      final double y,
      final double width,
      final double height
  ) {
    // Do not draw the selection box.
  }

  public OrientedRectangle getTextBox(final YLabel label) {
    if (painter != null) {
      return painter.getTextBox(label);
    } else {
      return null;
    }
  }

  public OrientedRectangle getIconBox(final YLabel label) {
    if (painter != null) {
      return painter.getIconBox(label);
    } else {
      return null;
    }
  }

  static YLabel.Painter defaultPainter() {
    final YLabel.Factory factory = NodeLabel.getFactory();
    final Map configuration = factory.createDefaultConfigurationMap();
    final Object painter = configuration.get(YLabel.Painter.class);
    if (painter instanceof YLabel.Painter) {
      return (YLabel.Painter) painter;
    } else {
      return null;
    }
  }
}
