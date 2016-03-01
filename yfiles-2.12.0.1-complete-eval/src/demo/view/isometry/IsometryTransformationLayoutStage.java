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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.OrientedRectangle;
import y.geom.YDimension;
import y.geom.YInsets;
import y.geom.YPoint;
import y.layout.AbstractLayoutStage;
import y.layout.EdgeLabelLayout;
import y.layout.EdgeLayout;
import y.layout.LabelLayoutData;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.NodeLabelLayout;
import y.layout.NodeLayout;
import y.layout.grouping.GroupingKeys;
import y.util.DataProviderAdapter;
import y.util.Maps;

import java.awt.geom.Rectangle2D;

/**
 * A {@link y.layout.LayoutStage} that transforms the graph to layout space before layout calculation is done and
 * transforms the graph back to the view space afterwards. The layout space is base area of the isometric space. The
 * view space contains the projection of the isometric space.
 */
class IsometryTransformationLayoutStage extends AbstractLayoutStage {

  /**
   * {@link DataProvider} key used to store {@link IsometryData  transformation data} to transform sizes and positions
   * of nodes to the layout space and the view space.
   */
  public static final String TRANSFORMATION_DATA_DPKEY = "com.yworks.isometry.transformation_data_dpkey";

  private static final int GROUP_NODE_INSET = 20;

  private boolean fromSketchMode;

  public IsometryTransformationLayoutStage(final Layouter coreLayouter) {
    this(coreLayouter, false);
  }

  public IsometryTransformationLayoutStage(final Layouter coreLayouter, final boolean fromSketchMode) {
    super(coreLayouter);
    this.fromSketchMode = fromSketchMode;
  }

  public boolean canLayout(final LayoutGraph graph) {
    return canLayoutCore(graph);
  }

  /**
   * Transforms the graph to the layout space, lay it out using the core layouter and transforms the result back into
   * the view space.
   *
   * @param graph the graph to lay out
   */
  public void doLayout(final LayoutGraph graph) {
    // Since our group node configuration does not provide an AutoBoundsFeature instance, the group node insets will not
    // be passed to the layout graph by the Graph2DLayoutExecutor. Therefore, we are set here an appropriate data
    // provider manually.
    final DataProvider oldGroupNodeInsets = graph.getDataProvider(GroupingKeys.GROUP_NODE_INSETS_DPKEY);
    graph.addDataProvider(
        GroupingKeys.GROUP_NODE_INSETS_DPKEY, new DataProviderAdapter() {
      public Object get(final Object dataHolder) {
        double labelHeight = 0;
        if (dataHolder instanceof Node) {
          final Node node = (Node) dataHolder;
          final NodeLabelLayout[] labels = graph.getNodeLabelLayout(node);
          labelHeight = labels[0].getBox().getHeight();
        }
        return new YInsets(GROUP_NODE_INSET, GROUP_NODE_INSET, GROUP_NODE_INSET + labelHeight, GROUP_NODE_INSET);
      }
    });

    /**
     * To assure that a group node is always wide enough to contain its label and group state icon a minimum node size
     * is calculated for each group node.
     */
    final DataProvider oldMinimumNodeSizes = graph.getDataProvider(GroupingKeys.MINIMUM_NODE_SIZE_DPKEY);
    graph.addDataProvider(GroupingKeys.MINIMUM_NODE_SIZE_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        if (dataHolder instanceof Node) {
          Node node = (Node) dataHolder;
          final NodeLabelLayout label = graph.getNodeLabelLayout(node)[0];
          return new YDimension(
              (int) label.getBox().getWidth() + IsometryGroupPainter.ICON_WIDTH + IsometryGroupPainter.ICON_GAP * 2, 0);
        }
        return null;
      }
    });

    // Transform the graph to the layout space.
    transformGraph(graph, false, isFromSketchMode());

    // Calculate the layout.
    doLayoutCore(graph);

    // Transform the graph back to the view space.
    transformGraph(graph, true, isFromSketchMode());

    // Restore the original group node insets and minimum size provider.
    graph.removeDataProvider(GroupingKeys.GROUP_NODE_INSETS_DPKEY);
    if (oldGroupNodeInsets != null) {
      graph.addDataProvider(GroupingKeys.GROUP_NODE_INSETS_DPKEY, oldGroupNodeInsets);
    }
    graph.removeDataProvider(GroupingKeys.MINIMUM_NODE_SIZE_DPKEY);
    if (oldMinimumNodeSizes != null) {
      graph.addDataProvider(GroupingKeys.MINIMUM_NODE_SIZE_DPKEY, oldMinimumNodeSizes);
    }
  }

  /**
   * Transforms the all edge points, node positions and sizes to the view or layout space.
   *
   * @param graph  the graph to transform
   * @param toView <code>true</code> to transform the given point to the view space, <code>false</code> to the layout
   *               space
   */
  private static void transformGraph(final LayoutGraph graph, final boolean toView, final boolean fromSketchMode) {
    // The transformation changes the size of the nodes. To avoid that this changes the position of the source and
    // target points of the edges, they are stored before the transformation and restored afterwards.
    final EdgeMap sourcePoints = Maps.createHashedEdgeMap();
    final EdgeMap targetPoints = Maps.createHashedEdgeMap();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      sourcePoints.set(edge, graph.getSourcePointAbs(edge));
      targetPoints.set(edge, graph.getTargetPointAbs(edge));
    }

    final double[] corners = new double[16];
    final Rectangle2D.Double bounds = new Rectangle2D.Double();

    // Transform the node sizes and locations.
    final DataProvider transformationData = graph.getDataProvider(TRANSFORMATION_DATA_DPKEY);
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      final NodeLayout nodeLayout = graph.getNodeLayout(node);
      final IsometryData data = (IsometryData) transformationData.get(node);

      if (toView) {
        final double oldWidth = nodeLayout.getWidth();
        final double oldHeight = nodeLayout.getHeight();
        final double oldCenterX = nodeLayout.getX() + oldWidth * 0.5;
        final double oldCenterY = nodeLayout.getY() + oldHeight * 0.5;
        // Store the width and height calculated by the core layouter. This is necessary for group nodes!
        data.setWidth(oldWidth);
        data.setDepth(oldHeight);

        data.calculateCorners(corners);
        IsometryData.calculateViewBounds(corners, bounds);
        final double newWidth = bounds.getWidth();
        final double newHeight = bounds.getHeight();
        final double newCenterX = IsometryData.toViewX(oldCenterX, oldCenterY);
        final double newCenterY = IsometryData.toViewY(oldCenterX, oldCenterY) - data.getHeight() * 0.5;
        nodeLayout.setSize(newWidth, newHeight);
        nodeLayout.setLocation(newCenterX - newWidth * 0.5, newCenterY - newHeight * 0.5);
      } else {
        final double oldCenterX = nodeLayout.getX() + nodeLayout.getWidth() * 0.5;
        final double oldCenterY = nodeLayout.getY() + nodeLayout.getHeight() * 0.5 + data.getHeight() * 0.5;
        final double newCenterX = IsometryData.toLayoutX(oldCenterX, oldCenterY);
        final double newCenterY = IsometryData.toLayoutY(oldCenterX, oldCenterY);
        final double newWidth = data.getWidth();
        final double newHeight = data.getDepth();
        nodeLayout.setSize(newWidth, newHeight);
        if (fromSketchMode) {
          nodeLayout.setLocation(newCenterX - newWidth * 0.5, newCenterY - newHeight * 0.5);
        }
      }
    }

    // Transform bends and end points for all edges in the graph.
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeLayout edgeLayout = graph.getEdgeLayout(edge);
      for (int i = 0; i < edgeLayout.pointCount(); i++) {
        final YPoint point = edgeLayout.getPoint(i);
        final YPoint transformedPoint = transformPoint(point, toView, fromSketchMode);
        edgeLayout.setPoint(i, transformedPoint.getX(), transformedPoint.getY());
      }

      // Restore the position of the source and target points of the edges.
      graph.setSourcePointAbs(edge, transformPoint((YPoint) sourcePoints.get(edge), toView, fromSketchMode));
      graph.setTargetPointAbs(edge, transformPoint((YPoint) targetPoints.get(edge), toView, fromSketchMode));
    }

    if (toView) {
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        final DataProvider edgeLabelMap = graph.getDataProvider(LabelLayoutData.EDGE_LABEL_LAYOUT_KEY);
        final EdgeLabelLayout[] labels = graph.getEdgeLabelLayout(edge);
        final LabelLayoutData[] labelLayoutData = (LabelLayoutData[]) edgeLabelMap.get(edge);
        final EdgeLayout edgeLayout = graph.getEdgeLayout(edge);
        final NodeLayout sourceLayout = graph.getNodeLayout(edge.source());
        final NodeLayout targetLayout = graph.getNodeLayout(edge.target());
        if (labelLayoutData != null) {
          for (int i = 0; i < labels.length; i++) {
            final EdgeLabelLayout label = labels[i];
            final LabelLayoutData labelData = labelLayoutData[i];
            final double oldWidth = labelData.getWidth();
            final double oldHeight = labelData.getHeight();
            final double oldCenterX = labelData.getX() + oldWidth * 0.5;
            final double oldCenterY = labelData.getY() + oldHeight * 0.5;
            // Store the width and height calculated by the core layouter. This is necessary for group nodes!
            final IsometryData data = (IsometryData) transformationData.get(label);
            final OrientedRectangle labelBounds = labelData.getBounds();
            data.setHorizontal(labelBounds.getUpY() == -1 || labelBounds.getUpY() == 1);
            data.setWidth(oldWidth);
            data.setDepth(oldHeight);

            data.calculateCorners(corners);
            IsometryData.calculateViewBounds(corners, bounds);
            final double newWidth = bounds.getWidth();
            final double newHeight = bounds.getHeight();
            final double newCenterX = IsometryData.toViewX(oldCenterX, oldCenterY);
            final double newCenterY = IsometryData.toViewY(oldCenterX, oldCenterY) - data.getHeight() * 0.5;

            final OrientedRectangle newBounds = new OrientedRectangle(newCenterX - newWidth * 0.5,
                newCenterY + newHeight * 0.5, newWidth, newHeight);
            final Object parameter = label.getLabelModel().createModelParameter(newBounds, edgeLayout, sourceLayout, targetLayout);
            final OrientedRectangle labelPlacement = label.getLabelModel().getLabelPlacement(newBounds.getSize(), edgeLayout, sourceLayout, targetLayout, parameter);
            label.setModelParameter(parameter);
            label.getOrientedBox().adoptValues(labelPlacement);
          }
        }
      }
      graph.removeDataProvider(LabelLayoutData.EDGE_LABEL_LAYOUT_KEY);
    } else {
      final EdgeMap edgeLabelMap = Maps.createHashedEdgeMap();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        final EdgeLabelLayout[] labels = graph.getEdgeLabelLayout(edge);
        final LabelLayoutData[] labelLayoutData = new LabelLayoutData[labels.length];
        for (int i = 0; i < labels.length; i++) {
          final EdgeLabelLayout label = labels[i];
          final IsometryData data = (IsometryData) transformationData.get(label);
          if (!data.isHorizontal()) {
            labelLayoutData[i] = new LabelLayoutData(data.getDepth(), data.getWidth());
          } else {
            labelLayoutData[i] = new LabelLayoutData(data.getWidth(), data.getDepth());
          }
          labelLayoutData[i].setPreferredPlacementDescriptor(label.getPreferredPlacementDescriptor());
        }
        edgeLabelMap.set(edge, labelLayoutData);
      }
      graph.addDataProvider(LabelLayoutData.EDGE_LABEL_LAYOUT_KEY, edgeLabelMap);
    }
  }

  /**
   * Transforms the given point to the view or layout space.
   *
   * @param point  the point to transform
   * @param toView <code>true</code> to transform the given point to the view space, <code>false</code> to the layout
   *               space
   */
  private static YPoint transformPoint(final YPoint point, final boolean toView, final boolean fromSketchMode) {
    final double x = point.getX();
    final double y = point.getY();

    if (toView) {
      return new YPoint(IsometryData.toViewX(x, y), IsometryData.toViewY(x, y));
    } else {
      if (fromSketchMode) {
        return new YPoint(IsometryData.toLayoutX(x, y), IsometryData.toLayoutY(x, y));
      } else {
        return point;
      }
    }
  }

  /**
   * Determines whether or not this layout stage transforms the coordinates of the graph elements before calculating
   * layout. This is important for incremental layout.
   *
   * @return <code>true</code> if graph elements get transformed before layout, <code>false</code> otherwise.
   *
   * @see #setFromSketchMode(boolean)
   */
  public boolean isFromSketchMode() {
    return fromSketchMode;
  }

  /**
   * Specifies whether or not this layout stage transforms the coordinates of the graph elements before calculating
   * layout. This is important for incremental layout.
   *
   * @param fromSketchMode <code>true</code> if graph elements get transformed before layout, <code>false</code>
   *                       otherwise.
   *
   * @see #setFromSketchMode(boolean)
   */
  public void setFromSketchMode(boolean fromSketchMode) {
    this.fromSketchMode = fromSketchMode;
  }
}
