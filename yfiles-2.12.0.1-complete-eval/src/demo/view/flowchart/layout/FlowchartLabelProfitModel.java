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
package demo.view.flowchart.layout;

import y.base.Edge;
import y.base.NodeCursor;
import y.geom.LineSegment;
import y.geom.YLineSegmentCursor;
import y.geom.YPoint;
import y.geom.YPointPath;
import y.geom.YRectangle;
import y.layout.DiscreteNodeLabelModel;
import y.layout.EdgeLabelLayout;
import y.layout.LabelCandidate;
import y.layout.LayoutGraph;
import y.layout.NodeLabelLayout;
import y.layout.NodeLabelModel;
import y.layout.ProfitModel;

import java.util.HashMap;
import java.util.Map;

/**
 * A label profit model for the {@link FlowchartLayouter}.
 */
class FlowchartLabelProfitModel implements ProfitModel {
  private static final double MIN_PREFERRED_PLACEMENT_DISTANCE = 3.0;
  private static final double MAX_PREFERRED_PLACEMENT_DISTANCE = 40.0;

  private final LayoutGraph graph;
  private final Map label2OriginalBox;

  FlowchartLabelProfitModel(LayoutGraph graph) {
    this.graph = graph;
    this.label2OriginalBox = new HashMap();

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final NodeLabelLayout[] nll = graph.getLabelLayout(nc.node());

      for (int i = 0; i < nll.length; i++) {
        final NodeLabelModel nlm = nll[i].getLabelModel();
        if (nlm instanceof DiscreteNodeLabelModel) {
          label2OriginalBox.put(nll[i], nll[i].getModelParameter());
        }
      }
    }
  }

  public double getProfit(LabelCandidate candidate) {
    return candidate.getOwner() instanceof EdgeLabelLayout ?
        calcEdgeLabelProfit(graph, candidate) :
        calcNodeLabelProfit(candidate);
  }

  private double calcNodeLabelProfit(LabelCandidate candidate) {
    final NodeLabelLayout nl = (NodeLabelLayout) candidate.getOwner();

    if (nl.getLabelModel() instanceof DiscreteNodeLabelModel) {
      final int pos = ((Number) candidate.getParameter()).intValue();
      final int originalPos = ((Number) label2OriginalBox.get(nl)).intValue();

      if (pos == originalPos) {
        return 1.0;
      } else {
        switch (pos) {
          case DiscreteNodeLabelModel.NORTH:
          case DiscreteNodeLabelModel.SOUTH:
          case DiscreteNodeLabelModel.WEST:
          case DiscreteNodeLabelModel.EAST:
            return 0.95;
          case DiscreteNodeLabelModel.NORTH_EAST:
          case DiscreteNodeLabelModel.NORTH_WEST:
          case DiscreteNodeLabelModel.SOUTH_EAST:
          case DiscreteNodeLabelModel.SOUTH_WEST:
            return 0.9;
          default:
            return 0.0;
        }
      }
    } else {
      return 0.0;
    }
  }

  private static double calcEdgeLabelProfit(LayoutGraph g, LabelCandidate candidate) {
    final Edge e = g.getFeature((EdgeLabelLayout) candidate.getOwner());

    if (FlowchartElements.isRegularEdge(g, e)) {
      final double eLength = calcPathLength(g.getPath(e));
      final double maxPreferredPlacementDistance = Math.max(MAX_PREFERRED_PLACEMENT_DISTANCE, eLength * 0.2);
      final double minDistToSource = getDistance(candidate.getBoundingBox(), g.getSourcePointAbs(e));

      if (minDistToSource > maxPreferredPlacementDistance) {
        return 0.0;
      } else if (minDistToSource < MIN_PREFERRED_PLACEMENT_DISTANCE) {
        return 0.5;
      } else {
        return 1.0 - (minDistToSource / maxPreferredPlacementDistance);
      }
    } else {
      return 0.0;
    }
  }

  static double calcPathLength(final YPointPath path) {
    double length = 0.0;
    for (YLineSegmentCursor cur = path.lineSegments(); cur.ok(); cur.next()) {
      length += cur.lineSegment().length();
    }
    return length;
  }

  static double getDistance(YRectangle r, YPoint q) {
    if (r.contains(q)) {
      return 0.0;
    } else {
      //determine corners of the rectangle
      YPoint upperLeft = r.getLocation();
      YPoint lowerLeft = new YPoint(upperLeft.x, upperLeft.y + r.getHeight());
      YPoint lowerRight = new YPoint(lowerLeft.x + r.getWidth(), lowerLeft.y);
      YPoint upperRight = new YPoint(lowerRight.x, upperLeft.y);

      //determine minDist to one of the four border segments
      double minDist = Double.MAX_VALUE;
      LineSegment rLeftSeg = new LineSegment(upperLeft, lowerLeft);
      minDist = Math.min(minDist, getDistance(rLeftSeg, q));
      LineSegment rRightSeg = new LineSegment(upperRight, lowerRight);
      minDist = Math.min(minDist, getDistance(rRightSeg, q));
      LineSegment rTopSeg = new LineSegment(upperLeft, upperRight);
      minDist = Math.min(minDist, getDistance(rTopSeg, q));
      LineSegment rBottomSeg = new LineSegment(lowerLeft, lowerRight);
      minDist = Math.min(minDist, getDistance(rBottomSeg, q));
      return minDist;
    }
  }

  static double getDistance(LineSegment line, YPoint q) {
    final double x1 = line.getFirstEndPoint().x;
    final double y1 = line.getFirstEndPoint().y;

    //adjust vectors relative to first endpoints of line
    final double x2 = line.getSecondEndPoint().x - x1;
    final double y2 = line.getSecondEndPoint().y - y1;
    double pX = q.getX() - x1;
    double pY = q.getY() - y1;

    //calculate distance
    final double projSquaredDist;
    if ((pX * x2 + pY * y2) <= 0.0) {
      projSquaredDist = 0.0;
    } else {
      pX = x2 - pX;
      pY = y2 - pY;
      final double tmp = pX * x2 + pY * y2;
      projSquaredDist = tmp <= 0.0 ? 0.0 : tmp * tmp / (x2 * x2 + y2 * y2);
    }

    final double squaredDist = pX * pX + pY * pY - projSquaredDist;
    return squaredDist < 0.0 ? 0.0 : Math.sqrt(squaredDist);
  }
}
