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
package demo.view.isometry;

import y.view.AbstractCustomNodePainter;
import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

/**
 * A painter that paints a node as block in an isometric fashion.
 */
class IsometryNodePainter extends AbstractCustomNodePainter implements GenericNodeRealizer.ContainsTest {

  protected void paintNode(final NodeRealizer context, final Graphics2D graphics, final boolean sloppy) {
    final IsometryData data = (IsometryData) ((GenericNodeRealizer) context).getUserData();
    final double[] corners = new double[16];
    calculateCorners(context, corners);

    // fill faces
    if (initializeFill(context, graphics)) {
      final GeneralPath path = new GeneralPath();
      path.moveTo((float) corners[IsometryData.C4_X], (float) corners[IsometryData.C4_Y]);
      path.lineTo((float) corners[IsometryData.C5_X], (float) corners[IsometryData.C5_Y]);
      path.lineTo((float) corners[IsometryData.C6_X], (float) corners[IsometryData.C6_Y]);
      path.lineTo((float) corners[IsometryData.C7_X], (float) corners[IsometryData.C7_Y]);
      path.closePath();
      graphics.fill(path);
      path.reset();

      if (data.getHeight() > 0) {
        path.moveTo((float) corners[IsometryData.C0_X], (float) corners[IsometryData.C0_Y]);
        path.lineTo((float) corners[IsometryData.C4_X], (float) corners[IsometryData.C4_Y]);
        path.lineTo((float) corners[IsometryData.C7_X], (float) corners[IsometryData.C7_Y]);
        path.lineTo((float) corners[IsometryData.C3_X], (float) corners[IsometryData.C3_Y]);
        path.closePath();
        Color color = getFillColor(context, false).darker();
        graphics.setColor(color);
        graphics.fill(path);
        path.reset();

        path.moveTo((float) corners[IsometryData.C3_X], (float) corners[IsometryData.C3_Y]);
        path.lineTo((float) corners[IsometryData.C7_X], (float) corners[IsometryData.C7_Y]);
        path.lineTo((float) corners[IsometryData.C6_X], (float) corners[IsometryData.C6_Y]);
        path.lineTo((float) corners[IsometryData.C2_X], (float) corners[IsometryData.C2_Y]);
        path.closePath();
        graphics.setColor(color.darker());
        graphics.fill(path);
      }
    }

    // draw lines
    if (initializeLine(context, graphics)) {
      final Shape outline = createOutline(corners);
      graphics.draw(outline);
      if (data.getHeight() > 0) {
        final Line2D line = new Line2D.Double();
        line.setLine(corners[IsometryData.C7_X], corners[IsometryData.C7_Y],
            corners[IsometryData.C4_X], corners[IsometryData.C4_Y]);
        graphics.draw(line);
        line.setLine(corners[IsometryData.C7_X], corners[IsometryData.C7_Y],
            corners[IsometryData.C3_X], corners[IsometryData.C3_Y]);
        graphics.draw(line);
        line.setLine(corners[IsometryData.C7_X], corners[IsometryData.C7_Y],
            corners[IsometryData.C6_X], corners[IsometryData.C6_Y]);
        graphics.draw(line);
      }
    }
  }

  public boolean contains(final NodeRealizer context, final double x, final double y) {
    final double[] corners = new double[16];
    calculateCorners(context, corners);
    final Shape outline = createOutline(corners);
    return outline.contains(x, y);
  }

  /**
   * Created the outline shape out of the corners of the node in the view space.
   *
   * @see #calculateCorners(y.view.NodeRealizer, double[])
   */
  private Shape createOutline(final double[] corners) {
    final GeneralPath outline = new GeneralPath();
    outline.moveTo((float) corners[IsometryData.C0_X], (float) corners[IsometryData.C0_Y]);
    outline.lineTo((float) corners[IsometryData.C3_X], (float) corners[IsometryData.C3_Y]);
    outline.lineTo((float) corners[IsometryData.C2_X], (float) corners[IsometryData.C2_Y]);
    outline.lineTo((float) corners[IsometryData.C6_X], (float) corners[IsometryData.C6_Y]);
    outline.lineTo((float) corners[IsometryData.C5_X], (float) corners[IsometryData.C5_Y]);
    outline.lineTo((float) corners[IsometryData.C4_X], (float) corners[IsometryData.C4_Y]);
    outline.closePath();
    return outline;
  }

  /**
   * Calculates the corners of the node in the view space. The size of the node is stored in its {@link IsometryData
   * user data}. This method transforms the corners into the view space and moves them to the location of the realizer.
   */
  private void calculateCorners(final NodeRealizer context, final double[] corners) {
    if (context instanceof GenericNodeRealizer) {
      final IsometryData data = (IsometryData) ((GenericNodeRealizer) context).getUserData();
      data.calculateCorners(corners);
      IsometryData.moveTo(context.getX(), context.getY(), corners);
    }
  }
}
