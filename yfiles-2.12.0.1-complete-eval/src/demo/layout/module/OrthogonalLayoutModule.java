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

import y.base.Edge;
import y.base.EdgeCursor;
import y.layout.CanonicMultiStageLayouter;
import y.layout.ComponentLayouter;
import y.layout.LabelLayoutConstants;
import y.layout.LabelRanking;
import y.layout.PreferredPlacementDescriptor;
import y.layout.grouping.FixedGroupLayoutStage;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.orthogonal.EdgeLayoutDescriptor;
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.option.IntOptionItem;
import y.option.OptionHandler;
import y.option.ConstraintManager.Condition;
import y.option.OptionItem;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;


/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.orthogonal.OrthogonalLayouter}
 * and {@link y.layout.orthogonal.OrthogonalGroupLayouter} respectively.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_advanced_features.html#layout_advanced_features">Section Advanced Layout Concepts</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_stages.html#layout_stages">Section Layout Stages</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_group_layouter.html#orthogonal_group_layouter">Section Orthogonal Layout of Grouped Graphs</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_layouter.html#orthogonal_layouter">Section Orthogonal Layout</a> in the yFiles for Java Developer's Guide
 */
public class OrthogonalLayoutModule extends LayoutModule {
  //// Module 'Orthogonal Layout'
  protected static final String MODULE_ORTHOGONAL = "ORTHOGONAL_LAYOUTER";
  
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT = "LAYOUT";
  // Section 'Layout' items
  protected static final String ITEM_STYLE = "STYLE";
  protected static final String VALUE_NORMAL = "NORMAL";
  protected static final String VALUE_NORMAL_TREE = "NORMAL_TREE";
  protected static final String VALUE_UNIFORM_NODES = "UNIFORM_NODES";
  protected static final String VALUE_BOX_NODES = "BOX_NODES";
  protected static final String VALUE_MIXED = "MIXED";
  protected static final String VALUE_FIXED_MIXED = "FIXED_MIXED";
  protected static final String VALUE_FIXED_BOX_NODES = "FIXED_BOX_NODES";
  protected static final String ITEM_GRID = "GRID";
  protected static final String ITEM_LENGTH_REDUCTION = "LENGTH_REDUCTION";
  protected static final String ITEM_USE_EXISTING_DRAWING_AS_SKETCH = "USE_EXISTING_DRAWING_AS_SKETCH";
  protected static final String ITEM_CROSSING_POSTPROCESSING = "CROSSING_POSTPROCESSING";
  protected static final String ITEM_PERCEIVED_BENDS_POSTPROCESSING = "PERCEIVED_BENDS_POSTPROCESSING";
  protected static final String ITEM_ALIGN_DEGREE_ONE_NODES = "ALIGN_DEGREE_ONE_NODES";
  protected static final String ITEM_USE_RANDOMIZATION = "USE_RANDOMIZATION";
  protected static final String ITEM_USE_FACE_MAXIMIZATION = "USE_FACE_MAXIMIZATION";
  protected static final String ITEM_ROUTE_MULTI_EDGES_IN_PARALLEL = "ROUTE_MULTI_EDGES_IN_PARALLEL";
  protected static final String ITEM_MINIMUM_FIRST_SEGMENT_LENGTH = "MINIMUM_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_LAST_SEGMENT_LENGTH = "MINIMUM_LAST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_SEGMENT_LENGTH = "MINIMUM_SEGMENT_LENGTH";
  
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
  
  //// Section 'Grouping'
  protected static final String SECTION_GROUPING = "GROUPING";
  // Section 'Grouping' items
  protected static final String ITEM_GROUP_POLICY = "GROUP_LAYOUT_POLICY";
  protected static final String VALUE_LAYOUT_GROUPS = "LAYOUT_GROUPS";
  protected static final String VALUE_FIX_GROUPS = "FIX_GROUPS";
  protected static final String VALUE_IGNORE_GROUPS = "IGNORE_GROUPS";
  protected static final String ITEM_GROUP_LAYOUT_QUALITY = "GROUP_LAYOUT_QUALITY";

  /**
   * Creates an instance of this module.
   */
  public OrthogonalLayoutModule() {
    super(MODULE_ORTHOGONAL);
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
    options.addEnum(ITEM_STYLE, new String[]{
        VALUE_NORMAL,
        VALUE_NORMAL_TREE,
        VALUE_UNIFORM_NODES,
        VALUE_BOX_NODES,
        VALUE_MIXED,
        VALUE_FIXED_MIXED,
        VALUE_FIXED_BOX_NODES
    }, 0);
    options.addInt(ITEM_GRID,25)
      .setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    options.addBool(ITEM_LENGTH_REDUCTION, true);
    options.addBool(ITEM_USE_EXISTING_DRAWING_AS_SKETCH, false);
    options.addBool(ITEM_CROSSING_POSTPROCESSING, true);
    options.addBool(ITEM_PERCEIVED_BENDS_POSTPROCESSING, true);
    options.addBool(ITEM_ALIGN_DEGREE_ONE_NODES, true);
    options.addBool(ITEM_USE_RANDOMIZATION, true);
    options.addBool(ITEM_USE_FACE_MAXIMIZATION,false);
    options.addBool(ITEM_ROUTE_MULTI_EDGES_IN_PARALLEL, false);
    options.addDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH, 10);
    options.addDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH, 10);
    options.addDouble(ITEM_MINIMUM_SEGMENT_LENGTH, 10);
    
    //// Section 'Labeling'
    options.useSection(SECTION_LABELING);
    // Populate section
    options.addEnum(ITEM_EDGE_LABELING, new String[]{
        VALUE_NONE,
        VALUE_INTEGRATED,
        VALUE_GENERIC
    }, 0);
    options.addEnum(ITEM_EDGE_LABEL_MODEL, new String[]{
        VALUE_BEST,
        VALUE_AS_IS,
        VALUE_CENTER_SLIDER,
        VALUE_SIDE_SLIDER,
        VALUE_FREE
    }, 0);
    options.addBool(ITEM_CONSIDER_NODE_LABELS, false);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(ITEM_EDGE_LABELING, VALUE_NONE, ITEM_EDGE_LABEL_MODEL, true);    
    final Condition c =
        optionConstraints.createConditionValueEquals(ITEM_USE_EXISTING_DRAWING_AS_SKETCH, Boolean.FALSE);
    optionConstraints.setEnabledOnCondition(c, options.getItem(ITEM_CROSSING_POSTPROCESSING));
    optionConstraints.setEnabledOnCondition(c, options.getItem(ITEM_PERCEIVED_BENDS_POSTPROCESSING));
    optionConstraints.setEnabledOnCondition(c, options.getItem(ITEM_ALIGN_DEGREE_ONE_NODES));
    optionConstraints.setEnabledOnCondition(c, options.getItem(ITEM_STYLE));
    optionConstraints.setEnabledOnCondition(c, options.getItem(ITEM_USE_RANDOMIZATION));

    //// Section 'Grouping'
    options.useSection(SECTION_GROUPING);
    // Populate section
    final EnumOptionItem itemGroupPolicy = options.addEnum(ITEM_GROUP_POLICY, new String[]{
        VALUE_LAYOUT_GROUPS,
        VALUE_FIX_GROUPS,
        VALUE_IGNORE_GROUPS
    }, 0);
    final OptionItem itemGroupLayoutQuality = options.addDouble(ITEM_GROUP_LAYOUT_QUALITY, 1.0, 0.0, 1.0);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemGroupPolicy, VALUE_LAYOUT_GROUPS, itemGroupLayoutQuality);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final CanonicMultiStageLayouter canonic;
    
    final OptionHandler options = getOptionHandler();
    
    if (HierarchyManager.containsGroupNodes(getGraph2D())
        && !options.get(ITEM_GROUP_POLICY).equals(VALUE_IGNORE_GROUPS)
        && !options.get(ITEM_GROUP_POLICY).equals(VALUE_FIX_GROUPS)) {
      final OrthogonalGroupLayouter orthogonalGroup = new OrthogonalGroupLayouter();
      configure(orthogonalGroup, options);
      canonic = orthogonalGroup;
    } else {
      final OrthogonalLayouter orthogonal = new OrthogonalLayouter();
      configure(orthogonal, options);
      canonic = orthogonal;
    }

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    
    launchLayouter(canonic);
  }

  /**
   * Sets edge label models depending on the given options.
   * @param graph the graph whose label models may be changed
   * @param options the layout options that determine whether or not to
   * change label models
   */
  private void prepareGraph(final Graph2D graph, final OptionHandler options) {
    final boolean normalStyle = VALUE_NORMAL.equals(options.getString(ITEM_STYLE));
    final String el = options.getString(ITEM_EDGE_LABELING);
    if (el.equals(VALUE_GENERIC) || (el.equals(VALUE_INTEGRATED) && normalStyle)) {
      setupEdgeLabelModel(graph, el, options.getString(ITEM_EDGE_LABEL_MODEL));
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2D</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param orthogonal the <code>OrthogonalLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final OrthogonalLayouter orthogonal, final OptionHandler options) {
    ((ComponentLayouter) orthogonal.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    ////////////////////////////////////////////////////////////////////////////
    // Layout
    ////////////////////////////////////////////////////////////////////////////

    final String styleValue = options.getString(ITEM_STYLE);
    if (VALUE_NORMAL_TREE.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.NORMAL_TREE_STYLE);
    } else if (VALUE_UNIFORM_NODES.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.UNIFORM_STYLE);
    } else if (VALUE_BOX_NODES.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.BOX_STYLE);
    } else if (VALUE_MIXED.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.MIXED_STYLE);
    } else if (VALUE_FIXED_MIXED.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.FIXED_MIXED_STYLE);
    } else if (VALUE_FIXED_BOX_NODES.equals(styleValue)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.FIXED_BOX_STYLE);
    } else {
      // if VALUE_NORMAL.equals(styleValue)
      orthogonal.setLayoutStyle(OrthogonalLayouter.NORMAL_STYLE);
    }

    final EdgeLayoutDescriptor layoutDescriptor = orthogonal.getEdgeLayoutDescriptor();
    layoutDescriptor.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH));
    layoutDescriptor.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH));
    layoutDescriptor.setMinimumSegmentLength(options.getDouble(ITEM_MINIMUM_SEGMENT_LENGTH));

    orthogonal.setGrid(options.getInt(ITEM_GRID));
    orthogonal.setUseLengthReduction(
        options.getBool(ITEM_LENGTH_REDUCTION));
    orthogonal.setUseCrossingPostprocessing(
        options.getBool(ITEM_CROSSING_POSTPROCESSING));
    orthogonal.setPerceivedBendsOptimizationEnabled(
        options.getBool(ITEM_PERCEIVED_BENDS_POSTPROCESSING));
    orthogonal.setAlignDegreeOneNodesEnabled(options.getBool(ITEM_ALIGN_DEGREE_ONE_NODES));
    orthogonal.setUseRandomization(
        options.getBool(ITEM_USE_RANDOMIZATION));
    orthogonal.setUseFaceMaximization(
        options.getBool(ITEM_USE_FACE_MAXIMIZATION));
    orthogonal.setUseSketchDrawing(options.getBool(ITEM_USE_EXISTING_DRAWING_AS_SKETCH));
    orthogonal.setParallelEdgeLayouterEnabled(options.getBool(ITEM_ROUTE_MULTI_EDGES_IN_PARALLEL));


    ////////////////////////////////////////////////////////////////////////////
    // Labels
    ////////////////////////////////////////////////////////////////////////////

    final boolean normalStyle = (orthogonal.getLayoutStyle() == OrthogonalLayouter.NORMAL_STYLE);
    final String el = options.getString(ITEM_EDGE_LABELING);
    orthogonal.setIntegratedEdgeLabelingEnabled(el.equals(VALUE_INTEGRATED) && normalStyle);
    orthogonal.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS) && normalStyle);

    if (HierarchyManager.containsGroupNodes(getGraph2D()) && !options.get(ITEM_GROUP_POLICY).equals(VALUE_IGNORE_GROUPS)) {
      if (options.get(ITEM_GROUP_POLICY).equals(VALUE_FIX_GROUPS)) {
        final FixedGroupLayoutStage fgl = new FixedGroupLayoutStage();
        fgl.setInterEdgeRoutingStyle(FixedGroupLayoutStage.ROUTING_STYLE_ORTHOGONAL);
        orthogonal.prependStage(fgl);
      }
    }
    
    if (options.getString(ITEM_EDGE_LABELING).equals(VALUE_GENERIC)) {
      final GreedyMISLabeling la = new GreedyMISLabeling();
      la.setPlaceNodeLabels(false);
      la.setPlaceEdgeLabels(true);
      la.setAutoFlippingEnabled(true);
      la.setProfitModel(new LabelRanking());

      // the greedy labeling algorithm is appended to the main algorithm instead
      // of being set as the main algorithm's label layouter because node label
      // handling (see considerNodeLabelsEnabled above) requires and sets a 
      // y.layout.LabelLayoutTranslator instance as label layouter internally
      // and replacing said LabelLayoutTranslator instance would *silently*
      // turn off node label handling
      orthogonal.appendStage(la);
    }
  }
  
  /**
   * This method configures the modules underlying algorithm
   * with options found in the given <code>OptionHandler</code>.
   * @param orthogonalGroup the <code>OrthogonalGroupLayouter</code> to be configured
   * @param options an <code>OptionHandler</code> providing the option-values referred to
   */
  protected void configure(final OrthogonalGroupLayouter orthogonalGroup, final OptionHandler options) {
    final boolean normalStyle = (VALUE_NORMAL.equals(options.getString(ITEM_STYLE)));
    final String el = options.getString(ITEM_EDGE_LABELING);

    ((ComponentLayouter) orthogonalGroup.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    final EdgeLayoutDescriptor descriptor = orthogonalGroup.getEdgeLayoutDescriptor();
    descriptor.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH));
    descriptor.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH));
    descriptor.setMinimumSegmentLength(options.getDouble(ITEM_MINIMUM_SEGMENT_LENGTH));
    orthogonalGroup.setIntegratedEdgeLabelingEnabled(el.equals(VALUE_INTEGRATED) && normalStyle);
    orthogonalGroup.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS) && normalStyle);
    orthogonalGroup.setAlignDegreeOneNodesEnabled(options.getBool(ITEM_ALIGN_DEGREE_ONE_NODES));
    orthogonalGroup.setPerceivedBendsOptimizationEnabled(options.getBool(ITEM_PERCEIVED_BENDS_POSTPROCESSING));

    orthogonalGroup.setGrid(options.getInt(ITEM_GRID));
    orthogonalGroup.setLayoutQuality(options.getDouble(ITEM_GROUP_LAYOUT_QUALITY));
    orthogonalGroup.setParallelEdgeLayouterEnabled(options.getBool(ITEM_ROUTE_MULTI_EDGES_IN_PARALLEL));
  }

  private static void setupEdgeLabelModel(final Graph2D graph,  final String edgeLabeling, final String edgeLabelModel) {
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
