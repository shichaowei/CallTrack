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
package demo.view.flowchart;

import demo.view.flowchart.layout.FlowchartLayouter;
import demo.view.flowchart.painters.FlowchartLayoutConfigurator;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.LayoutOrientation;
import y.layout.PortConstraintKeys;
import y.module.LayoutModule;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.util.Maps;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;

/**
 * Provides a graphical settings component for {@link FlowchartLayouter}.
 */
public class FlowchartLayoutModule extends LayoutModule {
  private static final String FLOWCHART_LAYOUT = "FLOWCHART_LAYOUT";
  private static final String GROUP_NEGATIVE_BRANCH = "GROUP_NEGATIVE_BRANCH";
  private static final String GROUP_POSITIVE_BRANCH = "GROUP_POSITIVE_BRANCH";

  private static final String ALLOW_FLATWISE_EDGES = "ALLOW_FLATWISE_EDGES";
  private static final String LANE_INSETS = "LANE_INSETS";
  private static final String MINIMUM_EDGE_DISTANCE = "MINIMUM_EDGE_DISTANCE";
  private static final String MINIMUM_NODE_DISTANCE = "MINIMUM_NODE_DISTANCE";
  private static final String MINIMUM_POOL_DISTANCE = "MINIMUM_POOL_DISTANCE";

  private static final String IN_EDGE_GROUPING = "IN_EDGE_GROUPING";
  private static final String GROUPING_ALL = "GROUPING_ALL";
  private static final String GROUPING_OPTIMIZED = "GROUPING_OPTIMIZED";
  private static final String GROUPING_NONE = "GROUPING_NONE";

  private static final String ORIENTATION = "ORIENTATION";
  private static final String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  private static final String TOP_TO_BOTTOM = "TOP_TO_BOTTOM";

  private static final String NEGATIVE_BRANCH_LABEL = "NEGATIVE_BRANCH_LABEL";
  private static final String NEGATIVE_BRANCH_DEFAULT = "No";
  private static final String NEGATIVE_BRANCH_DIRECTION = "NEGATIVE_BRANCH_DIRECTION";

  private static final String POSITIVE_BRANCH_LABEL = "POSITIVE_BRANCH_LABEL";
  private static final String POSITIVE_BRANCH_DEFAULT = "Yes";
  private static final String POSITIVE_BRANCH_DIRECTION = "POSITIVE_BRANCH_DIRECTION";

  private static final String DIRECTION_WITH_THE_FLOW = "DIRECTION_WITH_THE_FLOW";
  private static final String DIRECTION_FLATWISE = "DIRECTION_FLATWISE";
  private static final String DIRECTION_LEFT_IN_FLOW = "DIRECTION_LEFT_IN_FLOW";
  private static final String DIRECTION_RIGHT_IN_FLOW = "DIRECTION_RIGHT_IN_FLOW";
  private static final String DIRECTION_UNDEFINED = "DIRECTION_UNDEFINED";

  private static final String[] DIRECTION_ENUM = {
      DIRECTION_WITH_THE_FLOW, DIRECTION_FLATWISE,
      DIRECTION_LEFT_IN_FLOW, DIRECTION_RIGHT_IN_FLOW,
      DIRECTION_UNDEFINED
  };

  private static final String[] GROUPING_ENUM = {
      GROUPING_ALL, GROUPING_OPTIMIZED, GROUPING_NONE
  };

  private static final String[] ORIENTATION_ENUM = {
      TOP_TO_BOTTOM, LEFT_TO_RIGHT
  };

  /**
   * Creates a new FlowchartLayoutModule.
   */
  public FlowchartLayoutModule() {
    super(FLOWCHART_LAYOUT);

    // PortIntersectionCalculator is enabled since there are many non-rectangular symbols in the flowchart palette
    setPortIntersectionCalculatorEnabled(true);
  }


  /**
   * Creates the option handler for this module.
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler op = new OptionHandler(FLOWCHART_LAYOUT);

    op.addEnum(ORIENTATION, ORIENTATION_ENUM, 0);

    op.addString(POSITIVE_BRANCH_LABEL, POSITIVE_BRANCH_DEFAULT);
    op.addEnum(POSITIVE_BRANCH_DIRECTION, DIRECTION_ENUM, 0);
    op.addString(NEGATIVE_BRANCH_LABEL, NEGATIVE_BRANCH_DEFAULT);
    op.addEnum(NEGATIVE_BRANCH_DIRECTION, DIRECTION_ENUM, 1);

    op.addEnum(IN_EDGE_GROUPING, GROUPING_ENUM, 1);
    op.addBool(ALLOW_FLATWISE_EDGES, true);

    op.addDouble(MINIMUM_NODE_DISTANCE, 30.0);
    op.addDouble(MINIMUM_EDGE_DISTANCE, 15.0);
    op.addDouble(MINIMUM_POOL_DISTANCE, 30.0);
    op.addDouble(LANE_INSETS, 10.0);


    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_POSITIVE_BRANCH);
    og.addItem(op.getItem(POSITIVE_BRANCH_LABEL));
    og.addItem(op.getItem(POSITIVE_BRANCH_DIRECTION));

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_NEGATIVE_BRANCH);
    og.addItem(op.getItem(NEGATIVE_BRANCH_LABEL));
    og.addItem(op.getItem(NEGATIVE_BRANCH_DIRECTION));

    // Ensure that the initial settings of this option handler and class FlowchartLayoutConfigurator are the same
    adoptSettings(op, new FlowchartLayoutConfigurator());
    // Ensure that the initial settings of this option handler and class FlowchartLayouter are the same
    adoptSettings(op, new FlowchartLayouter());

    return op;
  }

  /**
   * Configures and runs the flowchart layout algorithm.
   */
  protected void mainrun() {
    final OptionHandler op = getOptionHandler();

    final FlowchartLayouter layouter = new FlowchartLayouter();
    final FlowchartLayoutConfigurator layoutConfigurator = new FlowchartLayoutConfigurator();

    configureLayouter(op, layouter);
    configureLayoutConfigurator(op, layoutConfigurator);
    configureLayoutExecutor();

    final Graph2D graph = getGraph2D();

    try {
      configureInEdgeGrouping();
      layoutConfigurator.prepareAll(graph);

      launchLayouter(layouter, true);

    } finally {
      layoutConfigurator.restoreAll(graph);
      graph.removeDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
    }
  }

  /**
   * Configures the given layouter according to the settings of the given option handler.
   */
  static void configureLayouter(OptionHandler op, FlowchartLayouter layouter) {
    layouter.setAllowFlatwiseEdges(op.getBool(ALLOW_FLATWISE_EDGES));
    layouter.setLayoutOrientation(convertEnumToLayoutOrientation(op.getEnum(ORIENTATION)));

    layouter.setLaneInsets(op.getDouble(LANE_INSETS));
    layouter.setMinimumEdgeLength(op.getDouble(MINIMUM_EDGE_DISTANCE));
    layouter.setMinimumEdgeDistance(op.getDouble(MINIMUM_EDGE_DISTANCE));
    layouter.setMinimumNodeDistance(op.getDouble(MINIMUM_NODE_DISTANCE));
    layouter.setMinimumPoolDistance(op.getDouble(MINIMUM_POOL_DISTANCE));
  }

  /**
   * Adopts the settings of the given layouter to the given option handler.
   */
  static void adoptSettings(OptionHandler oh, FlowchartLayouter layouter) {
    oh.set(ORIENTATION, isHorizontalOrientation(layouter) ? ORIENTATION_ENUM[1] : ORIENTATION_ENUM[0]);
    oh.set(ALLOW_FLATWISE_EDGES, Boolean.valueOf(layouter.isAllowFlatwiseEdges()));

    oh.set(LANE_INSETS, new Double(layouter.getLaneInsets()));
    oh.set(MINIMUM_EDGE_DISTANCE, new Double(layouter.getMinimumEdgeDistance()));
    oh.set(MINIMUM_NODE_DISTANCE, new Double(layouter.getMinimumNodeDistance()));
    oh.set(MINIMUM_POOL_DISTANCE, new Double(layouter.getMinimumPoolDistance()));
  }

  /**
   * Configures the given {@link FlowchartLayoutConfigurator} according to the settings of the given option handler.
   */
  static void configureLayoutConfigurator(OptionHandler op, FlowchartLayoutConfigurator configurator) {
    configurator.setPositiveBranchLabel(op.getString(POSITIVE_BRANCH_LABEL));
    configurator.setNegativeBranchLabel(op.getString(NEGATIVE_BRANCH_LABEL));
    configurator.setPreferredNegativeBranchDirection(
        convertEnumToBranchDirection(op.getEnum(NEGATIVE_BRANCH_DIRECTION)));
    configurator.setPreferredPositiveBranchDirection(
        convertEnumToBranchDirection(op.getEnum(POSITIVE_BRANCH_DIRECTION)));
  }

  /**
   * Adopts the settings of the given {@link FlowchartLayoutConfigurator} to the given option handler.
   */
  static void adoptSettings(OptionHandler oh, FlowchartLayoutConfigurator configurator) {
    oh.set(POSITIVE_BRANCH_LABEL, configurator.getPositiveBranchLabel());
    oh.set(NEGATIVE_BRANCH_LABEL, configurator.getNegativeBranchLabel());
    oh.set(NEGATIVE_BRANCH_DIRECTION,
        DIRECTION_ENUM[convertBranchDirectionToEnum(configurator.getPreferredNegativeBranchDirection())]);
    oh.set(POSITIVE_BRANCH_DIRECTION,
        DIRECTION_ENUM[convertBranchDirectionToEnum(configurator.getPreferredPositiveBranchDirection())]);
  }

  /**
   * Configures the layout executor of this module.
   */
  private void configureLayoutExecutor() {
    final Graph2DLayoutExecutor layoutExecutor = getLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setPreferredDuration(500L);
  }

  private void configureInEdgeGrouping() {
    final OptionHandler oh = getOptionHandler();

    if (oh.get(IN_EDGE_GROUPING).equals(GROUPING_NONE)) {
      return;
    }

    final int inDegreeThreshold;
    final int degreeThreshold;
    if (oh.get(IN_EDGE_GROUPING).equals(GROUPING_ALL)) {
      inDegreeThreshold = 0;
      degreeThreshold = 0;
    } else {
      inDegreeThreshold = 3;
      degreeThreshold = 4;
    }

    final Graph2D graph = getGraph2D();
    final EdgeMap map = Maps.createHashedEdgeMap();
    graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, map);

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (node.inDegree() < 2 || node.inDegree() < inDegreeThreshold || node.degree() < degreeThreshold) {
        continue;
      }

      for (EdgeCursor edgeCursor = node.inEdges(); edgeCursor.ok(); edgeCursor.next()) {
        final Edge edge = edgeCursor.edge();
        map.set(edge, node);
      }
    }
  }

  /**
   * Returns the branch direction that corresponds to the given index of the branch direction settings array.
   */
  private static int convertEnumToBranchDirection(int enumIndex) {
    switch (enumIndex) {
      case 0:
        return FlowchartLayouter.DIRECTION_WITH_THE_FLOW;
      case 1:
        return FlowchartLayouter.DIRECTION_FLATWISE;
      case 2:
        return FlowchartLayouter.DIRECTION_LEFT_IN_FLOW;
      case 3:
        return FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW;
      case 4:
      default:
        return FlowchartLayouter.DIRECTION_UNDEFINED;
    }
  }

  /**
   * Returns the index of the branch direction settings array that corresponds to the given branch direction.
   */
  private static int convertBranchDirectionToEnum(int direction) {
    switch (direction) {
      case FlowchartLayouter.DIRECTION_WITH_THE_FLOW:
        return 0;
      case FlowchartLayouter.DIRECTION_FLATWISE:
        return 1;
      case FlowchartLayouter.DIRECTION_LEFT_IN_FLOW:
        return 2;
      case FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW:
        return 3;
      case FlowchartLayouter.DIRECTION_UNDEFINED:
      default:
        return 4;
    }
  }

  /**
   * Returns the layout orientation that corresponds to the given index of the orientation settings array.
   */
  private static byte convertEnumToLayoutOrientation(int enumIndex) {
    return enumIndex == 0 ? LayoutOrientation.TOP_TO_BOTTOM : LayoutOrientation.LEFT_TO_RIGHT;
  }

  private static boolean isHorizontalOrientation(FlowchartLayouter layouter) {
    final int layoutOrientation = (int) layouter.getLayoutOrientation();
    return layoutOrientation == (int) LayoutOrientation.LEFT_TO_RIGHT
        || layoutOrientation == (int) LayoutOrientation.RIGHT_TO_LEFT;
  }

}
