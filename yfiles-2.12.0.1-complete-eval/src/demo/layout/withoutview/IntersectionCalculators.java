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
package demo.layout.withoutview;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.geom.YPoint;
import y.layout.IntersectionCalculator;
import y.layout.LayoutGraph;
import y.layout.NodeLayout;
import y.util.DataProviderAdapter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Provides utilities for calculating intersection points of edges at nodes.
 */
public class IntersectionCalculators {
  /**
   * Data provider key to register a data provider that may be queried for a
   * node's shape type.
   * This data provider is necessary to be able to determine a node's shape
   * when visualizing a graph in
   * {@link IntersectionCalculatorDemo.MyLayoutPreviewPanel}.
   */
  static final Object SHAPE_DPKEY = "demo.layout.withoutview.IntersectionCalculator.SHAPE_DPKEY";

  /**
   * Shape type specifier for nodes that should be considered rectangular.
   */
  public static final int SHAPE_RECTANGLE = 0;
  /**
   * Shape type specifier for nodes that should be considered elliptical.
   */
  public static final int SHAPE_ELLIPSE = 1;
  /**
   * Shape type specifier for nodes that should be considered diamond shaped.
   */
  public static final int SHAPE_DIAMOND = 2;

  private IntersectionCalculators() {
  }

  /**
   * Registers a data provider that returns intersection calculators for
   * nodes based on the node shape type returned by the given shape data
   * provider.
   * @param graph the graph on which the data provider is registered.
   * @param shapeDp the data provider that is queried for the nodes' shapes.
   * @param source <code>true</code> if intersection calculators for source
   * nodes of edges are needed and <code>false</code> if intersection
   * calculators for target nodes of edges are needed.
   */
  public static void addIntersectionCalculator(
          final LayoutGraph graph, final DataProvider shapeDp, final boolean source
  ) {
    if (source) {
      final Object key = IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY;
      graph.addDataProvider(key, new IntersectionCalculatorProvider(shapeDp, true));
    } else {
      final Object key = IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY;
      graph.addDataProvider(key, new IntersectionCalculatorProvider(shapeDp, false));
    }
  }

  /**
   * Data provider that returns {@link IntersectionCalculator}s for endpoints
   * of edges depending on node shape.
   */
  private static class IntersectionCalculatorProvider extends DataProviderAdapter {
    private final DataProvider shapeDp;
    private final boolean source;
    private final DiamondCalculator diamondCalculator;
    private final EllipseCalculator ellipseCalculator;

    /**
     * Initializes a new <code>IntersectionCalculatorProvider</code>.
     * @param shapeDp a data provider that can be queried for a node's shape.
     * @param source <code>true</code> if intersection calculators for
     * source nodes should be returned or <code>false</code> if intersection
     * calculators for target nodes should be returned.
     */
    IntersectionCalculatorProvider(
            final DataProvider shapeDp,
            final boolean source
    ) {
      this.shapeDp = shapeDp;
      this.source = source;
      diamondCalculator = new DiamondCalculator();
      ellipseCalculator = new EllipseCalculator();
    }

    /**
     * Returns an intersection calculator for the given edge's source or target
     * node.
     * @param dataHolder the edge for which source or target intersection has
     * to be calculated.
     * @return an intersection calculator suitable for the given edge's source
     * or target node or <code>null</code> if the node should be handled
     * as a rectangle. 
     */
    public Object get( final Object dataHolder ) {
      final Edge edge = (Edge) dataHolder;
      switch (getShape(source ? edge.source() : edge.target())) {
        case SHAPE_DIAMOND:
          return diamondCalculator;
        case SHAPE_ELLIPSE:
          return ellipseCalculator;
        default:
          return null;
      }
    }

    /**
     * Returns the shape type for the specified node.
     * @return one of <ul>
     * <li>{@link IntersectionCalculators#SHAPE_RECTANGLE},</li>
     * <li>{@link IntersectionCalculators#SHAPE_ELLIPSE}, and</li>
     * <li>{@link IntersectionCalculators#SHAPE_DIAMOND}.</li>
     * </ul>
     */
    private int getShape( final Node node ) {
      return shapeDp == null ? SHAPE_RECTANGLE : shapeDp.getInt(node);
    }
  }

  /**
   * Intersection calculator for diamond shaped nodes whose symmetry axes
   * are paraxial (i.e. the tips of the diamond are assumed to be horizontally
   * or vertically centered in the node's bounding box).
   */
  private static final class DiamondCalculator implements IntersectionCalculator {
    private static final double EPS = 1e-10;

    /**
     * Calculates the intersection point by intersecting the affine line that
     * is given by the specified offset and direction with each of the four
     * sides of a diamond.
     */
    public YPoint calculateIntersectionPoint(
            final NodeLayout nl,
            final double xOffset, final double yOffset,
            final double dx, final double dy
    ) {
      final double w = nl.getWidth();
      final double w2 = w * 0.5;
      final double minX = nl.getX();
      final double maxX = minX + w;
      final double cx = minX + w2;

      final double h = nl.getHeight();
      final double h2 = h * 0.5;
      final double minY = nl.getY();
      final double maxY = minY + h;
      final double cy = minY + h2;

      // one point on the line that is test for intersection with the diamond's
      // sides
      final double qx = cx + xOffset;
      final double qy = cy + yOffset;

      // another point on the line that is test for intersection with the
      // diamond's sides
      // this second point is constructed such that it lies outside the given
      // node's bounding box
      double px = qx - dx;
      double py = qy - dy;
      if (dx > 0) {
        if (qx > minX) {
          px = minX - 10;
          py = qy + (px - qx)/dx * dy;
        }
      } else if (dx < 0) {
        if (qx < maxX) {
          px = maxX + 10;
          py = qy + (px - qx)/dx * dy;
        }
      } else {
        if (dy > 0) {
          if (qy > minY) {
            px = qx;
            py = minY - 10;
          }
        } else if (dy < 0) {
          if (qy < maxY) {
            px = qx;
            py = maxY + 10;
          }
        } else {
          // dx == 0 and dy == 0 means q + t*d == q for all t
          // in other words, there is no intersection point because the
          // given data does not define an affine line but a single point
          return null;
        }
      }

      final ArrayList intersections = new ArrayList();

      final double inf = -EPS;
      final double sup = w2 + EPS;

      // check if the given line intersects the diamond's upper left side
      final YPoint p1 = calcIntersection(px, py, qx, qy, cx, minY, minX, cy);
      if (p1 != null) {
        // check if the intersection lies on the line segment that makes up
        // the diamond's upper left side
        final double tmp = p1.getX() - minX;
        // fuzzy check to compensate double rounding errors
        if (inf < tmp && tmp < sup) {
          intersections.add(p1);
        }
      }

      // check if the given line intersects the diamond's lower left side
      final YPoint p2 = calcIntersection(px, py, qx, qy, minX, cy, cx, maxY);
      if (p2 != null) {
        // check if the intersection lies on the line segment that makes up
        // the diamond's lower left side
        final double tmp = p2.getX() - minX;
        // fuzzy check to compensate double rounding errors
        if (inf < tmp && tmp < sup) {
          intersections.add(p2);
        }
      }

      // check if the given line intersects the diamond's lower right side
      final YPoint p3 = calcIntersection(px, py, qx, qy, cx, maxY, maxX, cy);
      if (p3 != null) {
        // check if the intersection lies on the line segment that makes up
        // the diamond's lower right side
        final double tmp = maxX - p3.getX();
        // fuzzy check to compensate double rounding errors
        if (inf < tmp && tmp < sup) {
          intersections.add(p3);
        }
      }

      // check if the given line intersects the diamond's upper right side
      final YPoint p4 = calcIntersection(px, py, qx, qy, maxX, cy, cx, minY);
      if (p4 != null) {
        // check if the intersection lies on the line segment that makes up
        // the diamond's upper right side
        final double tmp = maxX - p4.getX();
        // fuzzy check to compensate double rounding errors
        if (inf < tmp && tmp < sup) {
          intersections.add(p4);
        }
      }

      if (intersections.isEmpty()) {
        return null;
      } else if (intersections.size() == 1) {
        final YPoint p = (YPoint) intersections.get(0);
        // transform the intersection point into a point relative
        // to the node's center
        return new YPoint(p.getX() - cx, p.getY() - cy);
      } else {
        // since p lies outside the node's bounding box by construction
        // the correct intersection point can be chosen as the intersection
        // point closest to p
        Iterator it = intersections.iterator();

        YPoint result = (YPoint) it.next();
        double distX = result.getX() - px;
        double distY = result.getY() - py;
        double distSqr = distX * distX + distY * distY;
        while (it.hasNext()) {
          final YPoint next = (YPoint) it.next();
          distX = next.getX() - px;
          distY = next.getY() - py;
          final double tmp = distX * distX + distY * distY;
          if (distSqr > tmp) {
            distSqr = tmp;
            result = next;
          }
        }

        // transform the chosen intersection point into a point relative
        // to the node's center
        return new YPoint(result.getX() - cx, result.getY() - cy);
      }
    }

    private static YPoint calcIntersection(
            final double x1, final double y1,
            final double x2, final double y2,
            final double x3, final double y3,
            final double x4, final double y4
    ) {
      final double a1 = y2 - y1;
      final double b1 = x1 - x2;

      final double a2 = y4 - y3;
      final double b2 = x3 - x4;

      final double det = a1 * b2 - a2 * b1;
      if (det == 0) {
        return null;
      } else {
        final double c1 = a1 * x1 + b1 * y1;
        final double c2 = a2 * x3 + b2 * y3;
        final double x = (b2 * c1 - b1 * c2) / det;
        final double y = (a1 * c2 - a2 * c1) / det;
        return new YPoint(x, y);
      }
    }
  }

  /**
   * Intersection calculator for elliptical nodes.
   */
  private static final class EllipseCalculator implements IntersectionCalculator {
    /**
     * Calculates the intersection point by intersecting the affine line that
     * is given by the specified offset and direction with the ellipse
     * <code>[(x - c_x)/r_x]^2 + [(y - c_y)/r_y]^2 = 1</code> with
     * <code>(r_x, r_y) = (c_x, c_y) - (x, y)</code>, <code>(c_x, c_y)</code>
     * the center, and <code>(x, y)</code> the upper left corner of the given
     * node layout.
     */
    public YPoint calculateIntersectionPoint(
            final NodeLayout nl,
            final double xOffset, final double yOffset,
            final double dx, final double dy
    ) {
      final double rx = nl.getWidth() * 0.5;
      final double ry = nl.getHeight() * 0.5;

      final double v1x = dx / rx;
      final double v1y = dy / ry;
      final double v2x = xOffset / rx;
      final double v2y = yOffset / ry;

      final double a = v1x*v1x + v1y*v1y;
      final double b = 2 * v1x * v2x + 2 * v1y * v2y;
      final double c = v2x*v2x + v2y*v2y - 1;

      final double det = b * b - 4 * a * c;
      if (det < 0) {
        return null;
      } else if (det > 0) {
        final double sqrt = Math.sqrt(det);
        final double m = (-b - sqrt) / (2 * a);
        return new YPoint(xOffset + m * dx, yOffset + m * dy);
      } else {
        final double m = -b / (2 * a);
        return new YPoint(xOffset + m * dx, yOffset + m * dy);
      }
    }
  }
}
