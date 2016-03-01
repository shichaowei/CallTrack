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

import y.view.BendList;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

/**
 * Implementation of {@link y.view.GenericEdgeRealizer.Painter} interface.
 * Draws edge with gradient from source fill color to target fill color.
 * If source line width and target line width differs, it smoothly changes the edge width
 */
class GradientEdgePainter implements GenericEdgeRealizer.Painter {

  /**
   * We don't want to change the appearance, paintSloppy just calls paint
   * @param context the EdgeRealizer context that is used for the painting
   * @param bends the list of bends that the current context holds
   * @param path the GeneralPath instance that is associated with the current context
   * @param gfx current <code>Graphics2D</code>
   * @param selected whether the edge should be painted in selected state
   */
  public void paintSloppy(final EdgeRealizer context, final BendList bends, final GeneralPath path,
                          final Graphics2D gfx, final boolean selected) {
    paintImpl(context, path, gfx, 2);
  }

  /**
   * Paint an edge with gradient and changing line width.
   * Fill color of source and target used for start and endcolor,
   * LineType of source and target used for line width
   * @param context the EdgeRealizer context that is used for the painting
   * @param bends the list of bends that the current context holds
   * @param path the GeneralPath instance that is associated with the current context
   * @param gfx current <code>Graphics2D</code>
   * @param selected whether the edge should be painted in selected state
   */
  public void paint(final EdgeRealizer context, final BendList bends, final GeneralPath path, final Graphics2D gfx,
                    final boolean selected) {
    paintImpl(context, path, gfx, 30);
  }

  private void paintImpl(final EdgeRealizer context, final GeneralPath path, final Graphics2D gfx, final int totalParts) {
    final boolean isRoot = ViewModel.instance.isRoot(context.getSourceRealizer().getNode());

    //determine start color, end color, start line width and end line width
    Color currentColor;
    NodeRealizer nodeRealizer = context.getTargetRealizer();
    final Color endColor = nodeRealizer.getFillColor();
    final double endLineWidth = nodeRealizer.getLineType().getLineWidth();
    double currentLineWidth;
    if (!isRoot) {
      nodeRealizer = context.getSourceRealizer();
      currentColor = nodeRealizer.getFillColor();
      currentLineWidth = nodeRealizer.getLineType().getLineWidth();
    } else {
      currentColor = endColor;
      currentLineWidth = 15.0;
    }

    gfx.setColor(currentColor);

    //determine number of segments this edge contains of and the distance between source and target
    double dist = 0;//calcDist(start, last);
    double[] seg = new double[6];
    int count = 0;
    PathIterator pi = path.getPathIterator(null);
    pi.currentSegment(seg);
    pi.next();
    for (; !pi.isDone(); pi.next()) {
      final double oldX = seg[0];
      final double oldY = seg[1];
      pi.currentSegment(seg);
      dist += calcDist(oldX,oldY,seg[0],seg[1]);
      count++;
    }
    //a bezier curve consists of some very small segments and a few very large segments.
    //to make the gradient smooth, large segments are divided into smaller parts.
    //this way, the painter could also be used for straight edges or any other type of edges.
    final double maxDist = dist / totalParts;
    //determine the necessary change of color and line width for each step
    final int greenStep = (endColor.getGreen() - currentColor.getGreen()) / count;
    final int redStep = (endColor.getRed() - currentColor.getRed()) / count;
    final int blueStep = (endColor.getBlue() - currentColor.getBlue()) / count;
    final double lineWidthStep = (currentLineWidth - endLineWidth) / count;
    //draw every segment as an rectangle, where the line segment is the middle line of the rectangle.
    //xpos and ypos are the horizontal and vertical offset to get from the start and endpoint of the line
    //segment to the corners of the rectangle
    double angle = 0;
    boolean first = true;
    double[] start = new double[6];
    double[] end = new double[2];
    pi = path.getPathIterator(null);
    pi.currentSegment(start);
    pi.next();
    final GeneralPath gp = new GeneralPath();
    for (; !pi.isDone(); pi.next()) {
      //calculate length of current segment
      pi.currentSegment(seg);
      double xdiff = seg[0] - start[0];
      double ydiff = seg[1] - start[1];
      dist = calcDist(seg[0],seg[1], start[0],start[1]);
      double oldAngle = angle;
      angle = Math.atan2(ydiff, xdiff);
      double midAngle = (oldAngle+angle) / 2;
      double xpos = (Math.sin(angle) * currentLineWidth * 0.5);
      double ypos = (Math.cos(angle) * currentLineWidth * 0.5);
      //determine if segment is to large
      if (dist > maxDist) {
        //Due to clipping and thick edges, the connection at the center item doesn't look
        //good when it is very small or very large.
        //In this case, an additional rectangle is drawn
        if (first && isRoot) {
          gfx.setColor(currentColor);
          end[0] = start[0] - (xdiff/dist) * currentLineWidth;
          end[1] = start[1] - (ydiff/dist) * currentLineWidth;
          gfx.fill(calcLineSegment(start, end, xpos, ypos, 0, 0, 0, gp));
        }
        //calculate in how many parts this segment has to be split
        final int parts = (int) Math.ceil(dist / maxDist) + 1;
        //adjust step width
        xdiff /= parts;
        ydiff /= parts;
        if (!first) {
          //draw transition line segment, to make the curve smoother
          double midXPos = (Math.sin(midAngle) * currentLineWidth * 0.5);
          double midYPos = (Math.cos(midAngle) * currentLineWidth * 0.5);
          gfx.fill(calcLineSegment(start, start, midXPos, midYPos, xdiff, ydiff, 0.5, gp));
          gfx.setColor(currentColor);
        }
        for (int i = 1; i <= parts; i++) {
          //draw line segment
          gfx.fill(calcLineSegment(start, start, xpos, ypos, xdiff, ydiff, i, gp));
          //adjust color and line width
          currentColor = getNewColor(currentColor, greenStep, redStep, blueStep, parts);
          gfx.setColor(currentColor);
          currentLineWidth = Math.max(currentLineWidth - (lineWidthStep / parts), endLineWidth);
          //as line width may changed, the position of the corners have slightly changed
          xpos = (Math.sin(angle) * currentLineWidth * 0.5);
          ypos = (Math.cos(angle) * currentLineWidth * 0.5);
        }
      } else {
        gfx.setColor(currentColor);
        if (!first) {
          //draw transition line segment, to make the curve smoother
          double midXPos = (Math.sin(midAngle) * currentLineWidth * 0.5);
          double midYPos = (Math.cos(midAngle) * currentLineWidth * 0.5);
          gfx.fill(calcLineSegment(start, start, midXPos, midYPos, xdiff, ydiff, 0.5, gp));
        } else if (isRoot) {
          //Due to clipping and thick edges, the connection at the center item doesn't look
          //good when it is very small or very large.
          //In this case, an additional rectangle is drawn
          end[0] = start[0] - (xdiff/dist) * currentLineWidth;
          end[1] = start[1] - (ydiff/dist) * currentLineWidth;
          gfx.fill(calcLineSegment(start, end, xpos, ypos, 0, 0, 0, gp));
        }
        //draw line segment
        gfx.fill(calcLineSegment(start, seg, xpos, ypos, 0, 0, 0, gp));
        //adjust color and line width
        currentColor = getNewColor(currentColor, greenStep, redStep, blueStep, 1);
        currentLineWidth = Math.max(currentLineWidth - lineWidthStep, endLineWidth);
      }
      //end point is now start point
      start[0] = seg[0];
      start[1] = seg[1];
      first = false;
    }
  }
  /**
   * Calculate distance between two points
   * @param x1 x coordinate of first point
   * @param y1 y coordinate of first point
   * @param x2 x coordinate of second point
   * @param y2 y coordinate of second point
   * @return euclidean distance between two points
   */
  private double calcDist(final double x1,final double y1,final double x2,final double y2) {
    final double dx = x1 - x2;
    final double dy = y1 - y2;
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Calculate a line segment.
   * A line segment is represented by a polygon that is a rotated rectangle.
   * @param start start point of the middle line of the resulting rectangle
   * @param end end point of the middle line of the resulting rectangle
   * @param xpos horizontal offset to get from start/end point to an rectangle corner
   * @param ypos vertical offset to get from start/end point to an rectangle corner
   * @param xstep used for split line segments. Move along the line segment in horizontal direction
   * @param ystep used for split line segments. Move along the line segment in vertical direction
   * @param i index of current part of a split line segment.
   * @return rotated rectangle
   */
  private GeneralPath calcLineSegment(final double[] start, final double[] end, final double xpos, final double ypos,
                                      final double xstep, final double ystep, final double i, final GeneralPath gp) {
    gp.reset();
    gp.moveTo((float) ((start[0] + (i - 1) * xstep) - xpos), (float) ((start[1] + (i - 1) * ystep) + ypos));
    gp.lineTo((float) ((start[0] + (i - 1) * xstep) + xpos), (float) ((start[1] + (i - 1) * ystep) - ypos));
    gp.lineTo((float) ((end[0] + (i + 0.5) * xstep) + xpos), (float) ((end[1] + (i + 0.5) * ystep) - ypos));
    gp.lineTo((float) ((end[0] + (i + 0.5) * xstep) - xpos), (float) ((end[1] + (i + 0.5) * ystep) + ypos));
    return gp;
  }

  /**
   * Calculate the next color of a gradient edge.
   * @param startColor the old color
   * @param greenStep change of green part
   * @param redStep change of red part
   * @param blueStep change of blue part
   * @param parts make gradient smoother
   * @return new color
   */
  private Color getNewColor(final Color startColor, final int greenStep, final int redStep, final int blueStep,
                            final int parts) {
    return new Color(
        Math.max(0, Math.min(255, startColor.getRed() + (redStep / parts))),
        Math.max(0, Math.min(255, startColor.getGreen() + (greenStep / parts))),
        Math.max(0, Math.min(255, startColor.getBlue() + (blueStep / parts))));
  }
}
