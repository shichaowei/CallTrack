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
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.ComponentLayouter;
import y.layout.LabelLayoutConstants;
import y.layout.LabelRanking;
import y.layout.LayoutGraph;
import y.layout.OrientationLayouter;
import y.layout.PortConstraintKeys;
import y.layout.PreferredPlacementDescriptor;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.orthogonal.DirectedOrthogonalLayouter;
import y.layout.orthogonal.EdgeLayoutDescriptor;
import y.option.ArrowCellRenderer;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.StrokeCellRenderer;
import y.util.DataProviderAdapter;
import y.util.pq.BHeapIntNodePQ;
import y.view.Arrow;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;

import java.awt.Color;


/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.orthogonal.DirectedOrthogonalLayouter}.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/directed_orthogonal_layouter.html#directed_orthogonal_layouter">Section Directed Orthogonal Layout</a> in the yFiles for Java Developer's Guide
 */
public class DirectedOrthogonalLayoutModule extends LayoutModule {
  //// Module 'Directed Orthogonal Layout' / 'UML Style'
  protected static final String MODULE_DIRECTED_ORTHOGONAL_LAYOUTER = "DIRECTED_ORTHOGONAL_LAYOUTER";
  
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT = "LAYOUT";
  // Section 'Layout' items
  protected static final String ITEM_GRID = "GRID";
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String ITEM_USE_EXISTING_DRAWING_AS_SKETCH = "USE_EXISTING_DRAWING_AS_SKETCH";
  protected static final String TITLE_IDENTIFY_DIRECTED_EDGES = "IDENTIFY_DIRECTED_EDGES";
  protected static final String ITEM_USE_AS_CRITERIA = "USE_AS_CRITERIA";
  protected static final String ITEM_LINE_COLOR = "LINE_COLOR";
  protected static final String ITEM_TARGET_ARROW = "TARGET_ARROW";
  protected static final String ITEM_LINE_TYPE = "LINE_TYPE";
  protected static final String ITEM_AUTO_GROUP_DIRECTED_EDGES = "AUTO_GROUP_DIRECTED_EDGES";
  protected static final String ITEM_MINIMUM_FIRST_SEGMENT_LENGTH = "MINIMUM_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_LAST_SEGMENT_LENGTH = "MINIMUM_LAST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_SEGMENT_LENGTH = "MINIMUM_SEGMENT_LENGTH";
  protected static final String ITEM_PERCEIVED_BENDS_POSTPROCESSING = "PERCEIVED_BENDS_POSTPROCESSING";
  protected static final String ITEM_ALIGN_DEGREE_ONE_NODES = "ALIGN_DEGREE_ONE_NODES";
  protected static final String ITEM_POSTPROCESSING_ENABLED = "POSTPROCESSING_ENABLED";
  
  //// Section 'Labeling'
  protected static final String SECTION_LABELING = "LABELING";
  // Section 'Labeling' items
  protected static final String ITEM_EDGE_LABELING = "EDGE_LABELING";
  protected static final String VALUE_NONE = "NONE";
  protected static final String VALUE_INTEGRATED = "INTEGRATED";
  protected static final String VALUE_GENERIC = "GENERIC";
  protected static final String ITEM_EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  protected static final String VALUE_BEST = "BEST";
  protected static final String VALUE_AS_IS = "AS_IS";
  protected static final String VALUE_CENTER_SLIDER = "CENTER_SLIDER";
  protected static final String VALUE_SIDE_SLIDER = "SIDE_SLIDER";
  protected static final String VALUE_FREE = "FREE";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  
  // data provider delete flag
  private boolean isEdgeDPAddedByModule = false;

  /**
   * Creates an instance of this module.
   */
  public DirectedOrthogonalLayoutModule() {
    super(MODULE_DIRECTED_ORTHOGONAL_LAYOUTER);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);

    //// Section 'Layout'
    options.useSection(SECTION_LAYOUT);
    // Populate section
    options.addInt(ITEM_GRID,25).setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    options.addEnum(ITEM_ORIENTATION, new String[]{
        VALUE_TOP_TO_BOTTOM,
        VALUE_LEFT_TO_RIGHT,
        VALUE_BOTTOM_TO_TOP,
        VALUE_RIGHT_TO_LEFT
    }, 0);
    final OptionItem itemUseDrawingAsSketch = options.addBool(ITEM_USE_EXISTING_DRAWING_AS_SKETCH, false);

    // Group 'Identify Direct Edges'
    final OptionGroup directEdgesGroup = new OptionGroup();
    directEdgesGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_IDENTIFY_DIRECTED_EDGES);
    // Populate group
    final OptionItem itemUseAsCriteria = directEdgesGroup.addItem(
        options.addEnum(ITEM_USE_AS_CRITERIA, new String[]{
            ITEM_LINE_COLOR,
            ITEM_TARGET_ARROW,
            ITEM_LINE_TYPE
        }, 0));
    final OptionItem itemLineColor = directEdgesGroup.addItem(options.addColor(ITEM_LINE_COLOR, Color.red, true));
    final EnumOptionItem itemTargetArrow = new EnumOptionItem(ITEM_TARGET_ARROW,
        Arrow.availableArrows().toArray(), Arrow.STANDARD);
    itemTargetArrow.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, new ArrowCellRenderer());
    itemTargetArrow.setUsingIntegers(true);
    directEdgesGroup.addItem(options.addItem(itemTargetArrow));
    final EnumOptionItem itemLineType = new EnumOptionItem(ITEM_LINE_TYPE,
        LineType.availableLineTypes().toArray(), LineType.LINE_2);
    itemLineType.setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER, new StrokeCellRenderer());
    itemLineType.setUsingIntegers(true);
    directEdgesGroup.addItem(options.addItem(itemLineType));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseAsCriteria, ITEM_LINE_COLOR, itemLineColor);
    optionConstraints.setEnabledOnValueEquals(itemUseAsCriteria, ITEM_TARGET_ARROW, itemTargetArrow);
    optionConstraints.setEnabledOnValueEquals(itemUseAsCriteria, ITEM_LINE_TYPE, itemLineType);

    // Edge settings
    // Populate section
    final OptionItem itemAutoGroupDirectedEdges = options.addBool(ITEM_AUTO_GROUP_DIRECTED_EDGES, true);
    options.addDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH, 10);
    options.addDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH, 10);
    options.addDouble(ITEM_MINIMUM_SEGMENT_LENGTH, 10);
    options.addBool(ITEM_PERCEIVED_BENDS_POSTPROCESSING, true);
    options.addBool(ITEM_ALIGN_DEGREE_ONE_NODES, true);
    options.addBool(ITEM_POSTPROCESSING_ENABLED, true);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseDrawingAsSketch, Boolean.FALSE,
        itemAutoGroupDirectedEdges);

    //// Section 'Labeling'
    options.useSection(SECTION_LABELING);
    // Populate section
    final EnumOptionItem itemEdgeLabeling = options.addEnum(ITEM_EDGE_LABELING, new String[]{
        VALUE_NONE,
        VALUE_INTEGRATED,
        VALUE_GENERIC
    }, 0);
    final OptionGroup labelingGroup = new OptionGroup();
    final OptionItem itemEdgeLabelModel = labelingGroup.addItem(
        options.addEnum(ITEM_EDGE_LABEL_MODEL, new String[]{
            VALUE_BEST,
            VALUE_AS_IS,
            VALUE_CENTER_SLIDER,
            VALUE_SIDE_SLIDER,
            VALUE_FREE,
        }, 0));
    labelingGroup.addItem(options.addBool(ITEM_CONSIDER_NODE_LABELS, false));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemEdgeLabeling, VALUE_NONE, itemEdgeLabelModel, true);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final DirectedOrthogonalLayouter orthogonal = new DirectedOrthogonalLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(orthogonal, options);
    
    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(orthogonal, true);
    } finally {
      restoreGraph(graph, options);
    }
  }

  /**
   * Prepares a <code>graph</code> depending on the given options for the
   * module's layout algorithm.
   * <br>
   * Additional resources created by this method have to be freed up by calling
   * {@link #restoreGraph(y.view.Graph2D, y.option.OptionHandler)} after
   * layout calculation.  
   * @param graph the graph to be prepared
   * @param options the options for the module's layout algorithm
   */
  protected void prepareGraph(final Graph2D graph, final OptionHandler options) {
    final String el = options.getString(ITEM_EDGE_LABELING);
    if (!el.equals(VALUE_NONE)) {
      setupEdgeLabelModel(graph, el, options.getString(ITEM_EDGE_LABEL_MODEL));
    }
    
    DataProvider upwardDP = graph.getDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY);
    if (upwardDP == null) {
      //determine upward edges if not already marked.
      upwardDP = new DataProviderAdapter() {
        public boolean getBool(Object o) {
          final EdgeRealizer er = graph.getRealizer((Edge)o);
          if (options.get(ITEM_USE_AS_CRITERIA).equals(ITEM_LINE_COLOR)) {
            final Color c1 = (Color) options.get(ITEM_LINE_COLOR);
            final Color c2 = er.getLineColor();
            return c1 != null && c1.equals(c2);
          } else if (options.get(ITEM_USE_AS_CRITERIA).equals(ITEM_TARGET_ARROW)) {
            final Arrow a1 = (Arrow) options.get(ITEM_TARGET_ARROW);
            final Arrow a2 = er.getTargetArrow();
            return a1 != null && a1.equals(a2);
          } else if (options.get(ITEM_USE_AS_CRITERIA).equals(ITEM_LINE_TYPE)) {
            final LineType l1 = (LineType) options.get(ITEM_LINE_TYPE);
            final LineType l2 = er.getLineType();
            return l1 != null && l1.equals(l2);
          }
          return false;
        }
      };
      graph.addDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY, upwardDP);
      isEdgeDPAddedByModule = true;
    }

    if (options.getBool(ITEM_AUTO_GROUP_DIRECTED_EDGES)) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, PortConstraintKeys.SOURCE_GROUPID_KEY);
      backupDataProvider(graph, PortConstraintKeys.TARGET_GROUPID_KEY);
      
      final EdgeMap sgMap = graph.createEdgeMap();
      final EdgeMap tgMap = graph.createEdgeMap();
      graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sgMap);
      graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tgMap);
      autoGroupEdges(graph, sgMap, tgMap, upwardDP);
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    if (options.getBool(ITEM_AUTO_GROUP_DIRECTED_EDGES)) {
      // release resources
      final EdgeMap sgMap = (EdgeMap) graph.getDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
      final EdgeMap tgMap = (EdgeMap) graph.getDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
      if (sgMap != null) {
        graph.disposeEdgeMap(sgMap);
      }
      if (tgMap != null) {
        graph.disposeEdgeMap(tgMap);
      }
      
      // remove the data providers set by this module by restoring the initial state
      restoreDataProvider(graph, PortConstraintKeys.SOURCE_GROUPID_KEY);
      restoreDataProvider(graph, PortConstraintKeys.TARGET_GROUPID_KEY);
    }
    if (isEdgeDPAddedByModule) {
      isEdgeDPAddedByModule = false;
      graph.removeDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param orthogonal the <code>DirectedOrthogonalLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final DirectedOrthogonalLayouter orthogonal, final OptionHandler options) {
    final EdgeLayoutDescriptor layoutDescriptor = orthogonal.getEdgeLayoutDescriptor();
    layoutDescriptor.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH));
    layoutDescriptor.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH));
    layoutDescriptor.setMinimumSegmentLength(options.getDouble(ITEM_MINIMUM_SEGMENT_LENGTH));

    orthogonal.setGrid(options.getInt(ITEM_GRID));
    orthogonal.setPerceivedBendsOptimizationEnabled(options.getBool(ITEM_PERCEIVED_BENDS_POSTPROCESSING));
    orthogonal.setUsePostprocessing(options.getBool(ITEM_POSTPROCESSING_ENABLED));
    orthogonal.setAlignDegreeOneNodesEnabled(options.getBool(ITEM_ALIGN_DEGREE_ONE_NODES));
    orthogonal.setUseSketchDrawing(options.getBool(ITEM_USE_EXISTING_DRAWING_AS_SKETCH));
    ((ComponentLayouter) orthogonal.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    final Object orientation = options.get(ITEM_ORIENTATION);
    if (VALUE_TOP_TO_BOTTOM.equals(orientation)) {
      orthogonal.setLayoutOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    } else if (VALUE_LEFT_TO_RIGHT.equals(orientation)) {
      orthogonal.setLayoutOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    } else if (VALUE_BOTTOM_TO_TOP.equals(orientation)) {
      orthogonal.setLayoutOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    } else if (VALUE_RIGHT_TO_LEFT.equals(orientation)) {
      orthogonal.setLayoutOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Labels
    ////////////////////////////////////////////////////////////////////////////

    if (options.getBool(ITEM_CONSIDER_NODE_LABELS)) {
      orthogonal.setConsiderNodeLabelsEnabled(true);
    }

    final String el = options.getString(ITEM_EDGE_LABELING);
    orthogonal.setIntegratedEdgeLabelingEnabled(el.equals(VALUE_INTEGRATED));
    orthogonal.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS));
    
    if (el.equals(VALUE_GENERIC)) {
      GreedyMISLabeling la = new GreedyMISLabeling();
      la.setPlaceNodeLabels(false);
      la.setPlaceEdgeLabels(true);
      la.setAutoFlippingEnabled(true);
      la.setProfitModel(new LabelRanking());
      orthogonal.appendStage(la);
    }
  }

  private void setupEdgeLabelModel(final Graph2D graph, final String edgeLabeling, final String edgeLabelModel) {
    if (edgeLabeling.equals(VALUE_NONE) || edgeLabelModel.equals(VALUE_AS_IS)) {
      return; //nothing to do
    }

    byte model = EdgeLabel.SIDE_SLIDER;
    if (edgeLabelModel.equals(VALUE_CENTER_SLIDER)) {
      model = EdgeLabel.CENTER_SLIDER;
    } else if (edgeLabelModel.equals(VALUE_FREE) || edgeLabelModel.equals(VALUE_BEST)) {
      model = EdgeLabel.FREE;
    }

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        final EdgeLabel el = er.getLabel(i);
        el.setModel(model);
        final byte prefOnSide = el.getPreferredPlacementDescriptor().getSideOfEdge();
        if (model == EdgeLabel.CENTER_SLIDER && prefOnSide != LabelLayoutConstants.PLACE_ON_EDGE) {
          setPreferredSide(el, LabelLayoutConstants.PLACE_ON_EDGE);
        } else if (model == EdgeLabel.SIDE_SLIDER && prefOnSide == LabelLayoutConstants.PLACE_ON_EDGE) {
          setPreferredSide(el, LabelLayoutConstants.PLACE_RIGHT_OF_EDGE);
        }
      }
    }
  }

  /**
   * Automatically groups edges either on their source or target side, but never on
   * both sides at the same time.
   * @param graph input graph
   * @param sgMap source group id map
   * @param tgMap target group id map
   */
  private void autoGroupEdges(LayoutGraph graph, EdgeMap sgMap, EdgeMap tgMap, DataProvider positiveDP) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      sgMap.set(ec.edge(), null);
      tgMap.set(ec.edge(), null);
    }

    BHeapIntNodePQ sourceGroupPQ = new BHeapIntNodePQ(graph);
    BHeapIntNodePQ targetGroupPQ = new BHeapIntNodePQ(graph);
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node n = nc.node();
      int outDegree = 0;
      for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
        if (positiveDP.getBool(ec.edge()) && !ec.edge().isSelfLoop()) {
          outDegree++;
        }
      }
      sourceGroupPQ.add(n, -outDegree);
      int inDegree = 0;
      for (EdgeCursor ec = n.inEdges(); ec.ok(); ec.next()) {
        if (positiveDP.getBool(ec.edge()) && !ec.edge().isSelfLoop()) {
          inDegree++;
        }
      }
      targetGroupPQ.add(n, -inDegree);
    }

    while (!sourceGroupPQ.isEmpty() && !targetGroupPQ.isEmpty()) {
      int bestIn = 0, bestOut = 0;
      if (!sourceGroupPQ.isEmpty()) {
        bestOut = -sourceGroupPQ.getMinPriority();
      }
      if (!targetGroupPQ.isEmpty()) {
        bestIn = -targetGroupPQ.getMinPriority();
      }
      if (bestIn > bestOut) {
        final Node n = targetGroupPQ.removeMin();
        for (EdgeCursor ec = n.inEdges(); ec.ok(); ec.next()) {
          final Edge e = ec.edge();
          if (sgMap.get(e) == null && positiveDP.getBool(e) && !e.isSelfLoop()) {
            tgMap.set(e, n);
            sourceGroupPQ.changePriority(e.source(), sourceGroupPQ.getPriority(e.source()) + 1);
          }
        }
      } else {
        final Node n = sourceGroupPQ.removeMin();
        for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
          final Edge e = ec.edge();
          if (tgMap.get(e) == null && positiveDP.getBool(e) && !e.isSelfLoop()) {
            sgMap.set(e, n);
            targetGroupPQ.increasePriority(e.target(), targetGroupPQ.getPriority(e.target()) + 1);
          }
        }
      }
    }
  }


  private static void setPreferredSide(final EdgeLabel el, final byte preferredSide) {
    final PreferredPlacementDescriptor oldDescriptor =
            el.getPreferredPlacementDescriptor();
    if (oldDescriptor.getSideOfEdge() != preferredSide) {
      final PreferredPlacementDescriptor newDescriptor =
              new PreferredPlacementDescriptor(oldDescriptor);
      newDescriptor.setSideOfEdge(preferredSide);
      el.setPreferredPlacementDescriptor(newDescriptor);
    }
  }
}
