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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.NodeMap;
import y.base.YCursor;
import y.geom.YDimension;
import y.layout.DiscreteEdgeLabelModel;
import y.layout.DiscreteNodeLabelModel;
import y.layout.EdgeLabelLayout;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.NodeLabelLayout;
import y.layout.grid.ColumnDescriptor;
import y.layout.grid.PartitionGrid;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.HierarchicLayouter;
import y.layout.grid.RowDescriptor;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.labeling.AbstractLabelingAlgorithm;
import y.layout.labeling.GreedyMISLabeling;
import y.util.DataProviderAdapter;
import y.util.Maps;

/**
 * An automatic layout algorithm for Flowchart diagrams. The different type of elements have to be marked with the
 * DataProvider keys {@link #EDGE_TYPE_DPKEY} and {@link #NODE_TYPE_DPKEY}.
 */
public class FlowchartLayouter implements Layouter {

  /**
   * {@link y.base.DataProvider} key used to specify the flowchart specific type of each node. Valid are all node type
   * constants specified by class {@link FlowchartElements}.
   */
  public static final Object NODE_TYPE_DPKEY =
      "demo.view.flowchart.layout.FlowchartLayouter.NODE_TYPE_DPKEY";

  /**
   * {@link y.base.DataProvider} key used to specify the flowchart specific type of each edge. Valid are all edge type
   * constants specified by class {@link FlowchartElements}.
   */
  public static final Object EDGE_TYPE_DPKEY =
      "demo.view.flowchart.layout.FlowchartLayouter.EDGE_TYPE_DPKEY";

  /**
   * {@link y.base.DataProvider} key used to specify the preferred source port direction of an edge. Valid are direction
   * type constants specified in this class.
   */
  public static final Object PREFERRED_DIRECTION_KEY =
      "demo.view.flowchart.layout.FlowchartLayouter.DIRECTION_KEY";

  /**
   * {@link DataProvider} key used to specify the node and edge labels that
   * may be placed by the algorithm. The data provider's
   * {@link DataProvider#getBool(Object) getBool} method has to return
   * <code>true</code> for labels that should be placed and <code>false</code>
   * for all other labels. If no data provider is registered for this key, all
   * labels are placed by the algorithm.
   */
  public static final Object LABEL_LAYOUT_DPKEY =
      "demo.view.flowchart.layout.FlowchartLayouter.LABEL_LAYOUT_DPKEY";


  // TODO replace with PortCandidates constants or add documentation
  public static final int DIRECTION_UNDEFINED = 0x0;
  public static final int DIRECTION_WITH_THE_FLOW = 0x1;
  public static final int DIRECTION_AGAINST_THE_FLOW = 0x2;
  public static final int DIRECTION_LEFT_IN_FLOW = 0x4;
  public static final int DIRECTION_RIGHT_IN_FLOW = 0x8;
  public static final int DIRECTION_STRAIGHT = DIRECTION_WITH_THE_FLOW | DIRECTION_AGAINST_THE_FLOW;
  public static final int DIRECTION_FLATWISE = DIRECTION_LEFT_IN_FLOW | DIRECTION_RIGHT_IN_FLOW;

  private boolean allowFlatwiseEdges;
  private byte layoutOrientation;
  private double laneInsets;
  private double minimumEdgeLength;
  private double minimumEdgeDistance;
  private double minimumNodeDistance;
  private double minimumPoolDistance;
  private final double minimumLabelDistance;

  private FlowchartTransformerStage transformerStage;

  public FlowchartLayouter() {
    transformerStage = new FlowchartTransformerStage();
    allowFlatwiseEdges = true;
    layoutOrientation = LayoutOrientation.TOP_TO_BOTTOM;
    laneInsets = 10.0;
    minimumEdgeDistance = 15.0;
    minimumEdgeLength = 30.0;
    minimumLabelDistance = 20.0;
    minimumNodeDistance = 30.0;
    minimumPoolDistance = 30.0;
  }

  /**
   * Returns whether or not flatwise edges are allowed.
   *
   * @return whether or not flatwise edges are allowed.
   */
  public boolean isAllowFlatwiseEdges() {
    return allowFlatwiseEdges;
  }

  /**
   * Sets whether or not flatwise edges are allowed.
   *
   * @param allow whether or not flatwise edges are allowed.
   */
  public void setAllowFlatwiseEdges(boolean allow) {
    this.allowFlatwiseEdges = allow;
  }

  /**
   * Returns the insets used for swimlanes.
   * <p/>
   * Defaults to <code>10.0</code>.
   *
   * @see #setLaneInsets(double)
   */
  public double getLaneInsets() {
    return laneInsets;
  }

  /**
   * Sets the insets for swimlanes, that is the distance between a graph element and the border of its enclosing
   * swimlane.
   * <p/>
   * Defaults to <code>10.0</code>.
   *
   * @param laneInsets the distance between graph elements and the border of their enclosing swimlanes.
   * @see #getLaneInsets()
   */
  public void setLaneInsets(double laneInsets) {
    this.laneInsets = laneInsets;
  }

  /**
   * Returns the minimum distance between two node elements.
   * <p/>
   * Defaults to <code>30.0</code>.
   *
   * @see #setMinimumNodeDistance(double)
   */
  public double getMinimumNodeDistance() {
    return minimumNodeDistance;
  }

  /**
   * Sets the minimum distance between two node elements.
   * <p/>
   * Defaults to <code>30.0</code>.
   *
   * @see #getMinimumNodeDistance()
   */
  public void setMinimumNodeDistance(double minimumNodeDistance) {
    this.minimumNodeDistance = minimumNodeDistance;
  }

  /**
   * Returns the minimum distance between two edge elements.
   * <p/>
   * Defaults to <code>30.0</code>.
   *
   * @see #setMinimumEdgeDistance(double)
   */
  public double getMinimumEdgeDistance() {
    return minimumEdgeDistance;
  }

  /**
   * Sets the minimum distance between two edge elements.
   * <p/>
   * Defaults to <code>30.0</code>.
   *
   * @see #getMinimumEdgeDistance()
   */
  public void setMinimumEdgeDistance(double minimumEdgeDistance) {
    this.minimumEdgeDistance = minimumEdgeDistance;
  }

  /**
   * Returns the minimum length of edges.
   * <p/>
   * Defaults to <code>20.0</code>.
   *
   * @see #setMinimumEdgeLength(double)
   */
  public double getMinimumEdgeLength() {
    return minimumEdgeLength;
  }

  /**
   * Sets the minimum length of edges.
   * <p/>
   * Defaults to <code>20.0</code>.
   *
   * @see #getMinimumEdgeLength()
   */
  public void setMinimumEdgeLength(double minimumEdgeLength) {
    this.minimumEdgeLength = minimumEdgeLength;
  }

  /**
   * Returns the used minimum distance between two pool elements.
   * <p/>
   * Defaults to <code>50.0</code>.
   *
   * @see #setMinimumPoolDistance(double)
   */
  public double getMinimumPoolDistance() {
    return minimumPoolDistance;
  }

  /**
   * Sets the minimum distance between two pool elements.
   * <p/>
   * Defaults to <code>50.0</code>.
   *
   * @see #getMinimumPoolDistance()
   */
  public void setMinimumPoolDistance(double distance) {
    this.minimumPoolDistance = distance;
  }

  /**
   * Returns the layout orientation.
   * <p/>
   * Defaults to {@link LayoutOrientation#TOP_TO_BOTTOM}
   *
   * @see #setLayoutOrientation(byte)
   */
  public byte getLayoutOrientation() {
    return layoutOrientation;
  }

  /**
   * Specifies the layout orientation.
   * <p/>
   * Defaults to {@link LayoutOrientation#TOP_TO_BOTTOM}.
   *
   * @param layoutOrientation one of {@link LayoutOrientation#TOP_TO_BOTTOM} and {@link LayoutOrientation#LEFT_TO_RIGHT}
   * @throws IllegalArgumentException if the specified orientation does not match any of the layout orientation
   *                                  constants defined in this class.
   * @see #getLayoutOrientation()
   */
  public void setLayoutOrientation(byte layoutOrientation) {
    switch (layoutOrientation) {
      case LayoutOrientation.TOP_TO_BOTTOM:
      case LayoutOrientation.LEFT_TO_RIGHT:
        this.layoutOrientation = layoutOrientation;
        break;
      default:
        throw new IllegalArgumentException("Invalid layout orientation: " + layoutOrientation);
    }
  }

  /**
   * Returns <code>true</code>. This method does not check whether the specified graph can be laid out by this algorithm
   * at all.
   */
  public boolean canLayout(LayoutGraph graph) {
    return true;
  }

  /**
   * Layouts the specified graph.
   */
  public void doLayout(LayoutGraph graph) {
    if (graph.isEmpty()) {
      return;
    }

    PartitionGrid grid = PartitionGrid.getPartitionGrid(graph);
    if (grid != null) {
      //adjust insets
      for (YCursor cur = grid.getColumns().cursor(); cur.ok(); cur.next()) {
        ColumnDescriptor column = (ColumnDescriptor) cur.current();
        column.setLeftInset(laneInsets);
        column.setRightInset(laneInsets);
      }
      for (YCursor cur = grid.getRows().cursor(); cur.ok(); cur.next()) {
        RowDescriptor row = (RowDescriptor) cur.current();
        row.setTopInset(laneInsets);
        row.setBottomInset(laneInsets);
      }
    }

    try {
      final IncrementalHierarchicLayouter ihl = configureHierarchicLayouter();

      transformerStage = new FlowchartTransformerStage();
      transformerStage.setCoreLayouter(ihl);

      final NodeMap layerIds = Maps.createHashedNodeMap();
      try {
        graph.addDataProvider(HierarchicLayouter.LAYER_VALUE_HOLDER_DPKEY, layerIds);

        transformerStage.doLayout(graph);
      } finally {
        graph.removeDataProvider(HierarchicLayouter.LAYER_VALUE_HOLDER_DPKEY);
      }

      final EdgeMap edge2LayoutDescriptor = Maps.createHashedEdgeMap();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        edge2LayoutDescriptor.set(ec.edge(), createEdgeLayoutDescriptor(ec.edge(), graph,
            ihl.getEdgeLayoutDescriptor(), isHorizontalOrientation()));
      }

      // do core layout
      try {
        graph.addDataProvider(FlowchartTransformerStage.LAYER_ID_DP_KEY, layerIds);
        graph.addDataProvider(HierarchicLayouter.EDGE_LAYOUT_DESCRIPTOR_DPKEY, edge2LayoutDescriptor);

        transformerStage.doLayout(graph);
      } finally {
        graph.removeDataProvider(FlowchartTransformerStage.LAYER_ID_DP_KEY);
        graph.removeDataProvider(HierarchicLayouter.EDGE_LAYOUT_DESCRIPTOR_DPKEY);
      }

    } finally {
      // remove key set by the FlowchartPortOptimizer
      graph.removeDataProvider(FlowchartPortOptimizer.NODE_TO_ALIGN_DP_KEY);
    }

    doLabelPlacement(graph);
  }

  /**
   * Returns an IncrementalHierarchicLayouter that is configured to fit this layouter's needs.
   */
  private IncrementalHierarchicLayouter configureHierarchicLayouter() {
    IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setOrthogonallyRouted(true);
    ihl.setRecursiveGroupLayeringEnabled(false);
    ihl.setComponentLayouterEnabled(false);
    ihl.setMinimumLayerDistance(minimumNodeDistance);
    ihl.setNodeToNodeDistance(minimumNodeDistance);
    ihl.setEdgeToEdgeDistance(minimumEdgeDistance);
    ihl.setBackloopRoutingEnabled(false);
    ihl.setLayoutOrientation(isHorizontalOrientation() ?
        LayoutOrientation.LEFT_TO_RIGHT : LayoutOrientation.TOP_TO_BOTTOM);
    ihl.setIntegratedEdgeLabelingEnabled(false);
    ihl.setConsiderNodeLabelsEnabled(true);

    final EdgeLayoutDescriptor descriptor = new EdgeLayoutDescriptor();
    descriptor.setMinimumDistance(minimumEdgeDistance);
    descriptor.setMinimumLength(15.0);
    descriptor.setMinimumFirstSegmentLength(15.0);
    descriptor.setMinimumLastSegmentLength(15.0);
    descriptor.setOrthogonallyRouted(true);
    ihl.setEdgeLayoutDescriptor(descriptor);

    ihl.getHierarchicLayouter().setPortConstraintOptimizer(new FlowchartPortOptimizer(getLayoutOrientation()));

    final FlowchartLayerer layerer = new FlowchartLayerer();
    layerer.setAllowFlatwiseDefaultFlow(isAllowFlatwiseEdges());
    ihl.setFromScratchLayerer(layerer);

    SimplexNodePlacer nodePlacer = (SimplexNodePlacer) ihl.getNodePlacer();
    nodePlacer.setBaryCenterModeEnabled(true);
    nodePlacer.setEdgeStraighteningOptimizationEnabled(true);

    return ihl;
  }

  /**
   * Creates a descriptor that has a minimum edge length that is long enough for a proper placement of all of the edge's
   * labels.
   */
  private EdgeLayoutDescriptor createEdgeLayoutDescriptor(Edge e, LayoutGraph g, EdgeLayoutDescriptor defaultDescriptor,
                                                          boolean horizontal) {
    final EdgeLabelLayout[] ell = g.getEdgeLabelLayout(e);

    double minLength = 0.0;
    for (int i = 0; i < ell.length; i++) {
      final YDimension labelSize = ell[i].getBox();
      if (FlowchartElements.isRegularEdge(g, e)) {
        minLength += horizontal ? labelSize.getWidth() : labelSize.getHeight();
      } else {
        minLength += horizontal ? labelSize.getHeight() : labelSize.getWidth();
      }
    }

    // add distance between labels and to the end-nodes
    if (ell.length > 0) {
      minLength += minimumNodeDistance + (double) (ell.length - 1) * minimumLabelDistance;
    }

    EdgeLayoutDescriptor descriptor = new EdgeLayoutDescriptor();
    descriptor.setMinimumDistance(defaultDescriptor.getMinimumDistance());
    descriptor.setMinimumLength(Math.max(minLength, defaultDescriptor.getMinimumLength()));
    descriptor.setMinimumFirstSegmentLength(defaultDescriptor.getMinimumFirstSegmentLength());
    descriptor.setMinimumLastSegmentLength(defaultDescriptor.getMinimumLastSegmentLength());
    descriptor.setOrthogonallyRouted(defaultDescriptor.isOrthogonallyRouted());

    return descriptor;
  }

  /**
   * Does the label placement.
   */
  private static void doLabelPlacement(final LayoutGraph graph) {
    final GreedyMISLabeling labeling = new GreedyMISLabeling();
    labeling.setSelection(LABEL_LAYOUT_DPKEY);
    labeling.setPlaceNodeLabels(true);
    labeling.setPlaceEdgeLabels(true);
    labeling.setProfitModel(new FlowchartLabelProfitModel(graph));
    labeling.setCustomProfitModelRatio(0.25);

    try {
      graph.addDataProvider(AbstractLabelingAlgorithm.LABEL_MODEL_DPKEY, new DataProviderAdapter() {
        public Object get(final Object dataHolder) {
          if (dataHolder instanceof NodeLabelLayout) {
            return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.CENTER);
          } else if (dataHolder instanceof EdgeLabelLayout) {
            return new DiscreteEdgeLabelModel(DiscreteEdgeLabelModel.SIX_POS);
          } else {
            return null;
          }
        }
      });

      labeling.doLayout(graph);
    } finally {
      graph.removeDataProvider(AbstractLabelingAlgorithm.LABEL_MODEL_DPKEY);
    }
  }

  private boolean isHorizontalOrientation() {
    return (int) layoutOrientation == (int) LayoutOrientation.LEFT_TO_RIGHT;
  }

  static boolean isFlatwiseBranch(DataProvider branchTypes, Object dataHolder) {
    return branchTypes != null && isFlatwiseBranchType(branchTypes.getInt(dataHolder));
  }

  static boolean isStraightBranch(DataProvider branchTypes, Object dataHolder) {
    return branchTypes != null && isStraightBranchType(branchTypes.getInt(dataHolder));
  }

  static boolean isStraightBranch(Graph graph, Object dataHolder) {
    return isStraightBranch(graph.getDataProvider(FlowchartLayouter.PREFERRED_DIRECTION_KEY), dataHolder);
  }

  static boolean isFlatwiseBranchType(int type) {
    return (type & DIRECTION_FLATWISE) != 0;
  }

  static boolean isStraightBranchType(int type) {
    return (type & DIRECTION_STRAIGHT) != 0;
  }

  static void restoreDataProvider(LayoutGraph graph, DataProvider dataProvider, Object key) {
    graph.removeDataProvider(key);
    if (dataProvider != null) {
      graph.addDataProvider(key, dataProvider);
    }
  }

}
