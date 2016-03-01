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
package demo.view.flowchart.painters;

import demo.view.flowchart.layout.FlowchartElements;
import demo.view.flowchart.layout.FlowchartLayouter;
import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.layout.IntersectionCalculator;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.NodeRealizerIntersectionCalculator;
import y.view.YLabel;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;

import java.util.Collection;
import java.util.HashSet;

/**
 * Prepares flowchart specific layout hints for the realizers created by {@link FlowchartRealizerFactory} and configures
 * the preferred direction of outgoing edges of decision nodes ('branch' edges). These hints are interpreted by {@link
 * demo.view.flowchart.layout.FlowchartLayouter}.
 */
public class FlowchartLayoutConfigurator {
  private static final Object LABEL_LAYOUT_DPKEY = FlowchartLayouter.LABEL_LAYOUT_DPKEY;


  private final Collection nodeActivityElements;
  private final Collection nodeAnnotationElements;
  private final Collection nodeDataElements;
  private final Collection nodeEndElements;
  private final Collection nodeEventElements;
  private final Collection nodeGatewayElements;
  private final Collection nodeReferenceElements;
  private final Collection nodeStartElements;

  private boolean portIntersectionDataProviderCreationEnabled;
  private int preferredPositiveBranchDirection;
  private int preferredNegativeBranchDirection;
  private int adjustedPositiveBranchDirection;
  private int adjustedNegativeBranchDirection;
  private String negativeBranchLabel;
  private String positiveBranchLabel;

  /**
   * Creates a new <code>FlowchartLayoutConfigurator</code>.
   */
  public FlowchartLayoutConfigurator() {
    portIntersectionDataProviderCreationEnabled = false;
    setPositiveBranchLabel("Yes");
    setNegativeBranchLabel("No");
    setPreferredPositiveBranchDirection(FlowchartLayouter.DIRECTION_WITH_THE_FLOW);
    setPreferredNegativeBranchDirection(FlowchartLayouter.DIRECTION_FLATWISE);

    nodeActivityElements = new HashSet();
    nodeActivityElements.add(FlowchartRealizerConstants.FLOWCHART_PROCESS_CONFIG_NAME);
    nodeActivityElements.add(FlowchartRealizerConstants.FLOWCHART_PREDEFINED_PROCESS_CONFIG_NAME);
    nodeActivityElements.add(FlowchartRealizerConstants.FLOWCHART_LOOP_LIMIT_CONFIG_NAME);
    nodeActivityElements.add(FlowchartRealizerConstants.FLOWCHART_LOOP_LIMIT_END_CONFIG_NAME);

    nodeAnnotationElements = new HashSet();
    nodeAnnotationElements.add(FlowchartRealizerConstants.FLOWCHART_ANNOTATION_CONFIG_NAME);

    nodeDataElements = new HashSet();
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_CARD_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_CLOUD_TYPE_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_DATA_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_DATABASE_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_DIRECT_DATA_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_DOCUMENT_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_INTERNAL_STORAGE_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_MANUAL_INPUT_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_PAPER_TYPE_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_STORED_DATA_CONFIG_NAME);
    nodeDataElements.add(FlowchartRealizerConstants.FLOWCHART_SEQUENTIAL_DATA_CONFIG_NAME);

    nodeGatewayElements = new HashSet();
    nodeGatewayElements.add(FlowchartRealizerConstants.FLOWCHART_DECISION_CONFIG_NAME);

    nodeEndElements = new HashSet();
    nodeEndElements.add(FlowchartRealizerConstants.FLOWCHART_TERMINATOR_CONFIG_NAME);

    nodeEventElements = new HashSet();
    nodeEventElements.add(FlowchartRealizerConstants.FLOWCHART_DELAY_CONFIG_NAME);
    nodeEventElements.add(FlowchartRealizerConstants.FLOWCHART_DISPLAY_CONFIG_NAME);
    nodeEventElements.add(FlowchartRealizerConstants.FLOWCHART_MANUAL_OPERATION_CONFIG_NAME);
    nodeEventElements.add(FlowchartRealizerConstants.FLOWCHART_PREPARATION_CONFIG_NAME);

    nodeReferenceElements = new HashSet();
    nodeReferenceElements.add(FlowchartRealizerConstants.FLOWCHART_ON_PAGE_REFERENCE_CONFIG_NAME);
    nodeReferenceElements.add(FlowchartRealizerConstants.FLOWCHART_OFF_PAGE_REFERENCE_CONFIG_NAME);

    nodeStartElements = new HashSet();
    nodeStartElements.add(FlowchartRealizerConstants.FLOWCHART_START1_CONFIG_NAME);
    nodeStartElements.add(FlowchartRealizerConstants.FLOWCHART_START2_CONFIG_NAME);
  }

  /**
   * Returns the label text that defines a negative branch.
   *
   * @return the label text that defines a negative branch.
   */
  public String getNegativeBranchLabel() {
    return negativeBranchLabel;
  }

  /**
   * Sets the label text that defines a negative branch.
   *
   * @param label the label text.
   */
  public void setNegativeBranchLabel(String label) {
    this.negativeBranchLabel = label;
  }

  /**
   * Returns the label text that defines a positive branch.
   *
   * @return the label text that defines a positive branch.
   */

  public String getPositiveBranchLabel() {
    return positiveBranchLabel;
  }

  /**
   * Sets the label text that defines a positive branch.
   *
   * @param label the label text.
   */
  public void setPositiveBranchLabel(String label) {
    this.positiveBranchLabel = label;
  }

  /**
   * Returns the preferred direction for negative branches.
   *
   * @return the preferred direction for negative branches.
   */
  public int getPreferredNegativeBranchDirection() {
    return preferredNegativeBranchDirection;
  }

  /**
   * Sets the preferred direction for negative branches.
   *
   * @param direction the preferred direction for negative branches.
   */
  public void setPreferredNegativeBranchDirection(int direction) {
    preferredNegativeBranchDirection = direction;
    adjustedPositiveBranchDirection = calculateAdjustedPositiveBranchDirection();
    adjustedNegativeBranchDirection = calculateAdjustedNegativeBranchDirection();
  }

  /**
   * Returns the preferred direction for positive branches.
   *
   * @return the preferred direction for positive branches.
   */
  public int getPreferredPositiveBranchDirection() {
    return preferredPositiveBranchDirection;
  }

  /**
   * Sets the preferred direction for positive branches.
   *
   * @param direction the preferred direction for positive branches.
   */
  public void setPreferredPositiveBranchDirection(int direction) {
    preferredPositiveBranchDirection = direction;
    adjustedPositiveBranchDirection = calculateAdjustedPositiveBranchDirection();
    adjustedNegativeBranchDirection = calculateAdjustedNegativeBranchDirection();
  }

  /**
   * Returns the adjusted direction that is set to negative branches. If the preferred positive and negative branches
   * interfere, this class adjusts them.
   *
   * @return the adjusted direction that is set to negative branches.
   */
  protected int getAdjustedNegativeBranchDirection() {
    return adjustedNegativeBranchDirection;
  }

  /**
   * Returns the adjusted direction that is set to positive branches. If the preferred positive and negative branches
   * interfere, this class adjusts them.
   *
   * @return the adjusted direction that is set to positive branches.
   */
  protected int getAdjustedPositiveBranchDirection() {
    return adjustedPositiveBranchDirection;
  }

  /**
   * Returns whether the creation of data providers for the {@link IntersectionCalculator} is enabled or disabled. By
   * default, this property is set to <code>false</code>.
   *
   * @return <code>true</code> if he creation of data providers for the IntersectionCalculator is enabled.
   */
  public boolean isPortIntersectionDataProviderCreationEnabled() {
    return portIntersectionDataProviderCreationEnabled;
  }

  /**
   * Specifies whether the creation of data providers for the {@link IntersectionCalculator} is enabled or disabled.
   *
   * @param enabled specifies whether or not data providers for the {@link IntersectionCalculator} are created.
   */
  public void setPortIntersectionDataProviderCreationEnabled(boolean enabled) {
    this.portIntersectionDataProviderCreationEnabled = enabled;
  }

  /**
   * Performs all necessary preparations for the specified graph.
   *
   * @param graph the <code>Graph2D</code> instance that is prepared for automated layout calculation.
   */
  public void prepareAll(final Graph2D graph) {
    if (portIntersectionDataProviderCreationEnabled) {
      graph.addDataProvider(IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY,
          new NodeRealizerIntersectionCalculator(graph, true));
      graph.addDataProvider(IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY,
          new NodeRealizerIntersectionCalculator(graph, false));
    }

    final DataMap branchMap = Maps.createHashedDataMap();
    graph.addDataProvider(FlowchartLayouter.PREFERRED_DIRECTION_KEY, branchMap);

    final NodeMap nodeTypeMap = Maps.createHashedNodeMap();
    graph.addDataProvider(FlowchartLayouter.NODE_TYPE_DPKEY, nodeTypeMap);

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      nodeTypeMap.setInt(node, (int) getType(graph.getRealizer(node)));
    }

    final EdgeMap edgeTypeMap = Maps.createHashedEdgeMap();
    graph.addDataProvider(FlowchartLayouter.EDGE_TYPE_DPKEY, edgeTypeMap);

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeRealizer realizer = graph.getRealizer(edge);

      edgeTypeMap.setInt(edge, (int) getType(realizer));
      branchMap.setInt(edge, getBranchType(realizer));
    }

    final HierarchyManager hm = graph.getHierarchyManager();
    if (hm != null) {
      graph.addDataProvider(LABEL_LAYOUT_DPKEY, new DefaultGroupLabelExclusion(hm));
    }
  }

  /**
   * Performs all necessary resource cleanup and data translation after a layout calculation.
   *
   * @param graph the <code>Graph2D</code> instance that was previously prepared for automated layout calculation.
   */
  public void restoreAll(final Graph2D graph) {
    final HierarchyManager hm = graph.getHierarchyManager();
    if (hm != null) {
      graph.removeDataProvider(LABEL_LAYOUT_DPKEY);
    }

    if (portIntersectionDataProviderCreationEnabled) {
      graph.removeDataProvider(IntersectionCalculator.SOURCE_INTERSECTION_CALCULATOR_DPKEY);
      graph.removeDataProvider(IntersectionCalculator.TARGET_INTERSECTION_CALCULATOR_DPKEY);
    }
    graph.removeDataProvider(FlowchartLayouter.NODE_TYPE_DPKEY);
    graph.removeDataProvider(FlowchartLayouter.EDGE_TYPE_DPKEY);
    graph.removeDataProvider(FlowchartLayouter.PREFERRED_DIRECTION_KEY);
  }

  /**
   * Returns the flowchart element type of the given edge realizer.
   *
   * @return one of the edge type constants in {@link FlowchartElements}.
   */
  protected byte getType(EdgeRealizer realizer) {
    final NodeRealizer sourceRealizer = realizer.getSourceRealizer();
    final NodeRealizer targetRealizer = realizer.getTargetRealizer();
    if (sourceRealizer != null && getType(sourceRealizer) == FlowchartElements.NODE_TYPE_ANNOTATION
        || targetRealizer != null && getType(targetRealizer) == FlowchartElements.NODE_TYPE_ANNOTATION) {
      return FlowchartElements.EDGE_TYPE_MESSAGE_FLOW;
    } else {
      return FlowchartElements.EDGE_TYPE_SEQUENCE_FLOW;
    }
  }

  /**
   * Returns the flowchart element type of the given node realizer.
   *
   * @return one of the node type constants in {@link FlowchartElements}.
   */
  protected byte getType(NodeRealizer realizer) {
    if (realizer instanceof TableGroupNodeRealizer) {
      return FlowchartElements.NODE_TYPE_POOL;
    } else if (realizer instanceof GroupNodeRealizer) {
      return FlowchartElements.NODE_TYPE_GROUP;
    } else if (realizer instanceof GenericNodeRealizer) {
      final String configuration = ((GenericNodeRealizer) realizer).getConfiguration();
      if (nodeActivityElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_PROCESS;
      } else if (nodeDataElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_DATA;
      } else if (nodeAnnotationElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_ANNOTATION;
      } else if (nodeGatewayElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_DECISION;
      } else if (nodeEndElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_END_EVENT;
      } else if (nodeEventElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_EVENT;
      } else if (nodeReferenceElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_PROCESS;
      } else if (nodeStartElements.contains(configuration)) {
        return FlowchartElements.NODE_TYPE_START_EVENT;
      }
    }

    return FlowchartElements.TYPE_INVALID;
  }

  /**
   * Returns the branch type of the given edge realizer.
   *
   * @return one of the direction constants in {@link FlowchartLayouter}.
   */
  protected int getBranchType(EdgeRealizer realizer) {
    if (isPositiveBranch(realizer)) {
      return getAdjustedPositiveBranchDirection();
    } else if (isNegativeBranch(realizer)) {
      return getAdjustedNegativeBranchDirection();
    } else {
      return FlowchartLayouter.DIRECTION_UNDEFINED;
    }
  }

  /**
   * Returns whether or not the given edge realizer is a positive branch. This default implementation considers an edge
   * as positive branch if its source is a decision and if its label text equals 'Yes' (ignoring case considerations).
   *
   * @param realizer the realizer to consider.
   * @return whether or not the given edge realizer is a positive branch.
   */
  protected boolean isPositiveBranch(EdgeRealizer realizer) {
    return (int) getType(realizer.getSourceRealizer()) == (int) FlowchartElements.NODE_TYPE_DECISION
        && realizer.labelCount() > 0 && isMatchingLabelText(realizer.getLabel(0), positiveBranchLabel);
  }

  /**
   * Returns whether or not the given edge realizer is a positive branch. This default implementation considers an edge
   * as negative branch if its source is a decision and if its label text equals 'No' (ignoring case considerations).
   *
   * @param realizer the realizer to consider.
   * @return whether or not the given edge realizer is a negative branch.
   */
  protected boolean isNegativeBranch(EdgeRealizer realizer) {
    return (int) getType(realizer.getSourceRealizer()) == (int) FlowchartElements.NODE_TYPE_DECISION
        && realizer.labelCount() > 0 && isMatchingLabelText(realizer.getLabel(0), negativeBranchLabel);
  }

  /**
   * Returns <code>true</code> if the given label is not null and its text equals, case ignored, the given text.
   */
  private static boolean isMatchingLabelText(YLabel label, String text) {
    final String labelText = label != null ? label.getText() : null;
    return labelText != null && labelText.equalsIgnoreCase(text);
  }

  /**
   * @see #getAdjustedNegativeBranchDirection()
   * @see #getAdjustedPositiveBranchDirection()
   */
  private int calculateAdjustedNegativeBranchDirection() {
    final int positiveDir = getAdjustedPositiveBranchDirection();
    final int negativeDir = preferredNegativeBranchDirection;

    switch (negativeDir) {
      case FlowchartLayouter.DIRECTION_STRAIGHT:
        return positiveDir != FlowchartLayouter.DIRECTION_WITH_THE_FLOW ?
            FlowchartLayouter.DIRECTION_WITH_THE_FLOW : FlowchartLayouter.DIRECTION_FLATWISE;

      case FlowchartLayouter.DIRECTION_FLATWISE:
        if (positiveDir == FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW) {
          return FlowchartLayouter.DIRECTION_LEFT_IN_FLOW;
        } else if (positiveDir == FlowchartLayouter.DIRECTION_LEFT_IN_FLOW) {
          return FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW;
        } else {
          return negativeDir;
        }

      default:
      case FlowchartLayouter.DIRECTION_AGAINST_THE_FLOW:
        return FlowchartLayouter.DIRECTION_UNDEFINED;

      case FlowchartLayouter.DIRECTION_WITH_THE_FLOW:
        return positiveDir != negativeDir ? negativeDir : FlowchartLayouter.DIRECTION_FLATWISE;

      case FlowchartLayouter.DIRECTION_LEFT_IN_FLOW:
        return positiveDir != negativeDir ? negativeDir : FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW;

      case FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW:
        return positiveDir != negativeDir ? negativeDir : FlowchartLayouter.DIRECTION_LEFT_IN_FLOW;
    }
  }

  /**
   * @see #getAdjustedNegativeBranchDirection()
   * @see #getAdjustedPositiveBranchDirection()
   */
  private int calculateAdjustedPositiveBranchDirection() {
    switch (preferredPositiveBranchDirection) {
      case FlowchartLayouter.DIRECTION_STRAIGHT:
        return FlowchartLayouter.DIRECTION_WITH_THE_FLOW;
      case FlowchartLayouter.DIRECTION_AGAINST_THE_FLOW:
        return FlowchartLayouter.DIRECTION_UNDEFINED;
      case FlowchartLayouter.DIRECTION_FLATWISE:
        if (preferredNegativeBranchDirection == FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW) {
          return FlowchartLayouter.DIRECTION_LEFT_IN_FLOW;
        } else if (preferredNegativeBranchDirection == FlowchartLayouter.DIRECTION_LEFT_IN_FLOW) {
          return FlowchartLayouter.DIRECTION_RIGHT_IN_FLOW;
        } else {
          return preferredPositiveBranchDirection;
        }
      default:
        return preferredPositiveBranchDirection;
    }
  }


  private static class DefaultGroupLabelExclusion extends DataProviderAdapter {
    private final HierarchyManager hm;

    DefaultGroupLabelExclusion( final HierarchyManager hm ) {
      this.hm = hm;
    }

    public boolean getBool( final Object dataHolder ) {
      if (dataHolder instanceof NodeLabel) {
        final NodeRealizer nr = ((NodeLabel) dataHolder).getRealizer();
        if (nr != null && nr.labelCount() > 0 && dataHolder == nr.getLabel(0)) {
          final Node n = nr.getNode();
          if (n != null) {
            final Graph g = n.getGraph();
            if (g != null && hm.contains(g)) {
              return !hm.isGroupNode(n);
            }
          }
        }
      }
      return true;
    }
  }
}
