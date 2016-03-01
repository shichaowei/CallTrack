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
package demo.layout.radial;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.radial.RadialLayouter;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Drawable to visualize the circles and sectors nodes are placed on/in by the RadialLayouter.
 * The necessary render information are provided by RadialLayouter.NodeInfo objects filled by the RadialLayouter.
 *
 */
public final class SectorDrawable implements Drawable {

  private final Graph2DView view;
  private final NodeMap nodeInfoMap;
  private Rectangle bounds;

  private final Stroke stroke;
  private Color[][] colors;
  private boolean drawingCircles;
  private boolean drawingSectors;

  private double[] radii;
  private YPoint center;
  private ArrayList nodeInfos;

  /**
   * Creates a new drawable for circles and sectors.
   *
   * @param view The view containing the {@link Graph2D}.
   * @param nodeInfoMap A node map containing {@link RadialLayouter.NodeInfo} objects for the nodes.
   */
  public SectorDrawable(final Graph2DView view, final NodeMap nodeInfoMap
  ) {
    this.view = view;
    this.nodeInfoMap = nodeInfoMap;
    this.bounds = new Rectangle(0, 0, 0, 0);

    this.drawingCircles = true;
    this.drawingSectors = false;

    this.stroke = new BasicStroke(1.25f);
    this.colors = new Color[][]{
            {new Color(180, 200, 255), new Color(120, 150, 220)},
            {new Color(180, 255, 200), new Color(120, 220, 150)}};
  }

  /**
   * Returns whether the circles shall be drawn.
   * <p>
   * Default is <code>true</code>.
   * </p>
   *
   * @return <code>true</code>, iff the circles shall be drawn.
   */
  public boolean isDrawingCircles() {
    return drawingCircles;
  }

  /**
   * Sets whether the circles shall be drawn.
   *
   * @return <code>true</code>, iff the circles shall be drawn.
   */
  public void setDrawingCircles(boolean drawingCircles) {
    this.drawingCircles = drawingCircles;
  }

  /**
   * Returns whether the sectors shall be drawn.
   * <p>
   * Default is <code>false</code>.
   * </p>
   *
   * @return <code>true</code>, iff the sectors shall be drawn.
   */
  public boolean isDrawingSectors() {
    return drawingSectors;
  }

  /**
   * Sets whether the sectors shall be drawn.
   *
   * @return <code>true</code>, iff the sectors shall be drawn.
   */
  public void setDrawingSectors(boolean drawingSectors) {
    this.drawingSectors = drawingSectors;
  }

  /**
   * Returns the bounding box of the largest circle.
   * @return The bounding box of the largest circle.
   */
  public Rectangle getBounds() {
    return bounds;
  }

  /**
   * Updates the radii and sector information based on the nodes' NodeInfo objects.
   */
  public void updateSectors() {
    final Graph2D g = view.getGraph2D();

    if (g.N() < 1) {
      return;
    }

    // the common center of the circles
    center = null;
    // a list of RadialLayouter.NodeInfo objects drawn in the paint method
    nodeInfos = new ArrayList(g.N());

    // collect all RadialLayouter.NodeInfo objects as well as the common center and radii of the circles
    HashSet radiiSet = new HashSet();
    for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()) {
      Node node = nc.node();
      RadialLayouter.NodeInfo info = (RadialLayouter.NodeInfo) nodeInfoMap.get(node);
      if (info != null) {
        // a layout with NodeInfos has been calculated for the current graph
        if (center == null) { // only calculate the center once
          YPoint nodeCenter = g.getCenter(node);
          // RadialLayouter.NodeInfo contains the offset from the center of the circle the
          // node is placed on to the center of the node.
          center = new YPoint(info.getCenterOffset().getX() - nodeCenter.getX(), info.getCenterOffset().getY() - nodeCenter.getY());
        }
        nodeInfos.add(info);

        // we collect the radii of all circles the  nodes are placed on
        radiiSet.add(new Double(info.getRadius()));
      }
    }
    radii = new double[radiiSet.size()];

    if (center == null) {
      // no node data is available
      bounds.setFrame(0, 0, 0, 0);
    } else {
      // fill radii array and sort it ascending
      int index = 0;
      for (Iterator it = radiiSet.iterator(); it.hasNext(); index++) {
        radii[index] = ((Double) it.next()).doubleValue();
      }
      Arrays.sort(radii);

      // sort NodeInfos by descending circleIndex and ascending wedgeStart
      Collections.sort(nodeInfos, new Comparator() {
        public int compare(Object o1, Object o2) {
          RadialLayouter.NodeInfo info1 = (RadialLayouter.NodeInfo) o1;
          RadialLayouter.NodeInfo info2 = (RadialLayouter.NodeInfo) o2;
          if (info1.getCircleIndex() != info2.getCircleIndex()) {
            return info2.getCircleIndex() - info1.getCircleIndex();
          } else {
            double dWedgeStart = info1.getSectorStart() - info2.getSectorStart();
            return dWedgeStart < 0 ? -1 : dWedgeStart > 0 ? 1 : 0;
          }
        }
      });

      // update bounds to include the out-most circle
      double maxRadius = radii[radii.length - 1];
      bounds.setFrame(center.getX() - maxRadius, center.getY() - maxRadius, 2 * maxRadius, 2 * maxRadius);
    }
    g.updateViews();
  }

  public void paint(final Graphics2D g) {
    if (radii == null || radii.length == 0) {
      // no layout has been calculated for the current graph
      return;
    }

    final Color oldColor = g.getColor();
    final Stroke oldStroke = g.getStroke();

    g.setStroke(stroke);
    if (isDrawingSectors()) {
      int colorIndex = 0;
      for (int i = 0; i < nodeInfos.size(); i++) {

        RadialLayouter.NodeInfo info = (RadialLayouter.NodeInfo) nodeInfos.get(i);
        double radius = info.getRadius();

        // the fill colors of the sectors alternate between two color sets depending on the circle index
        // sectors on the same circle toggle their fill color between the two colors in their set
        g.setColor(colors[info.getCircleIndex() % 2][colorIndex]);
        colorIndex = (colorIndex + 1) % 2;

        g.fill(new Arc2D.Double(center.getX() - radius, center.getY() - radius, 2 * radius, 2 * radius,
                info.getSectorStart(), info.getSectorSize(), Arc2D.PIE));
      }
    }

    if (isDrawingCircles()) {
      g.setColor(colors[0][0]);

      for (int i = 0; i < radii.length; i++) {
        double radius = radii[i];

        g.draw(new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, 2 * radius, 2 * radius));
      }
    }

    g.setStroke(oldStroke);
    g.setColor(oldColor);
  }
}
