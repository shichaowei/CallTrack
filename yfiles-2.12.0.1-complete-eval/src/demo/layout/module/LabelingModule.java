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
import y.layout.EdgeLabelModel;
import y.layout.LabelRanking;
import y.layout.RotatedDiscreteEdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.labeling.SALabeling;
import y.option.OptionHandler;
import y.option.ConstraintManager;
import y.option.ConstraintManager.Condition;
import y.option.EnumOptionItem;
import y.util.DataProviderAdapter;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.YLabel;
import y.view.Graph2DLayoutExecutor;

/**
 * This module represents an interactive configurator and launcher for the
 * yFiles labeling algorithms.
 *
 */
public class LabelingModule extends YModule {

  //// Module 'Labeling'
  protected static final String MODULE_DIVERSE_LABELING = "DIVERSE_LABELING";
  
  //// Section 'Scope'
  protected static final String SECTION_SCOPE = "SCOPE";
  // Section 'Scope' items
  protected static final String ITEM_PLACE_NODE_LABELS = "PLACE_NODE_LABELS";
  protected static final String ITEM_PLACE_EDGE_LABELS = "PLACE_EDGE_LABELS";
  protected static final String ITEM_CONSIDER_SELECTED_FEATURES_ONLY = "CONSIDER_SELECTED_FEATURES_ONLY";
  protected static final String ITEM_CONSIDER_INVISIBLE_LABELS = "CONSIDER_INVISIBLE_LABELS";
  
  //// Section 'Quality'
  protected static final String SECTION_QUALITY = "QUALITY";
  // Section 'Quality' items
  protected static final String ITEM_USE_OPTIMIZATION = "USE_OPTIMIZATION";
  protected static final String ITEM_OPTIMIZATION_STRATEGY = "OPTIMIZATION_STRATEGY";
  protected static final String VALUE_OPTIMIZATION_BALANCED = "OPTIMIZATION_BALANCED";
  protected static final String VALUE_OPTIMIZATION_NONE = "OPTIMIZATION_NONE";
  protected static final String VALUE_OPTIMIZATION_EDGE_OVERLAP = "OPTIMIZATION_EDGE_OVERLAP";
  protected static final String VALUE_OPTIMIZATION_LABEL_OVERLAP = "OPTIMIZATION_LABEL_OVERLAP";
  protected static final String VALUE_OPTIMIZATION_NODE_OVERLAP = "OPTIMIZATION_NODE_OVERLAP";
  protected static final String ITEM_ALLOW_NODE_OVERLAPS = "ALLOW_NODE_OVERLAPS";
  protected static final String ITEM_ALLOW_EDGE_OVERLAPS = "ALLOW_EDGE_OVERLAPS";
  protected static final String ITEM_USE_POSTPROCESSING = "USE_POSTPROCESSING";
  
  //// Section 'Model'
  protected static final String SECTION_MODEL = "MODEL";
  // Section 'Model' items
  protected static final String ITEM_EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  protected static final String VALUE_CENTERED = "CENTERED";
  protected static final String VALUE_TWO_POS = "TWO_POS";
  protected static final String VALUE_SIX_POS = "SIX_POS";
  protected static final String VALUE_THREE_CENTER = "THREE_CENTER";
  protected static final String VALUE_FREE = "FREE";
  protected static final String VALUE_CENTER_SLIDER = "CENTER_SLIDER";
  protected static final String VALUE_SIDE_SLIDER = "SIDE_SLIDER";
  protected static final String VALUE_AS_IS = "AS_IS";
  protected static final String VALUE_BEST = "BEST";
  protected static final String ITEM_AUTO_ROTATE = "AUTO_ROTATE";

  // data provider keys, no i18n
  protected static final String LABEL_SELECTION_DP_KEY = "LABEL_SELECTION";

  /**
   * Creates an instance of this module.
   */
  public LabelingModule() {
    super(MODULE_DIVERSE_LABELING);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraint = new ConstraintManager(options);

    //// Section 'Scope'
    options.useSection(SECTION_SCOPE);
    // Populate section
    options.addBool(ITEM_PLACE_NODE_LABELS, true);
    options.addBool(ITEM_PLACE_EDGE_LABELS, true);
    options.addBool(ITEM_CONSIDER_SELECTED_FEATURES_ONLY, false);
    options.addBool(ITEM_CONSIDER_INVISIBLE_LABELS, false);

    //// Section 'Quality'
    options.useSection(SECTION_QUALITY);
    // Populate section
    options.addBool(ITEM_USE_OPTIMIZATION, false);
    options.addEnum(ITEM_OPTIMIZATION_STRATEGY, new String[]{
        VALUE_OPTIMIZATION_BALANCED,
        VALUE_OPTIMIZATION_NONE,
        VALUE_OPTIMIZATION_EDGE_OVERLAP,
        VALUE_OPTIMIZATION_LABEL_OVERLAP,
        VALUE_OPTIMIZATION_NODE_OVERLAP
    }, 0);
    options.addBool(ITEM_ALLOW_NODE_OVERLAPS, false);
    options.addBool(ITEM_ALLOW_EDGE_OVERLAPS, true);
    options.addBool(ITEM_USE_POSTPROCESSING, false);

    //// Section 'Model'
    options.useSection(SECTION_MODEL);
    // Populate section
    final EnumOptionItem itemEdgeLabelModel = options.addEnum(ITEM_EDGE_LABEL_MODEL, new String[]{
        VALUE_CENTERED,
        VALUE_TWO_POS,
        VALUE_SIX_POS,
        VALUE_THREE_CENTER,
        VALUE_FREE,
        VALUE_CENTER_SLIDER,
        VALUE_SIDE_SLIDER,
        VALUE_AS_IS,
        VALUE_BEST
    }, 8);
    options.addBool(ITEM_AUTO_ROTATE, false);
    // Enable/disable items depending on specific values
    // enable the auto rotate item for applicably label models only
    final Condition condition =
        optionConstraint.createConditionValueIs(itemEdgeLabelModel, new String[]{
            VALUE_AS_IS,
            VALUE_BEST,
            VALUE_FREE
        });
    optionConstraint.setEnabledOnCondition(condition.inverse(), options.getItem(ITEM_AUTO_ROTATE));

    return options;
  }

  /**
   * Translates the given optimization strategy string into the corresponding
   * byte constant of class {@link MISLabelingAlgorithm}.
   */
  private static byte translateOptimizationStrategy(final String optimizationStrategy) {
    if (VALUE_OPTIMIZATION_LABEL_OVERLAP.equals(optimizationStrategy)) {
      return MISLabelingAlgorithm.OPTIMIZATION_LABEL_OVERLAP;
    } else if (VALUE_OPTIMIZATION_BALANCED.equals(optimizationStrategy)) {
      return MISLabelingAlgorithm.OPTIMIZATION_BALANCED;
    } else if (VALUE_OPTIMIZATION_EDGE_OVERLAP.equals(optimizationStrategy)) {
      return MISLabelingAlgorithm.OPTIMIZATION_EDGE_OVERLAP;
    } else if (VALUE_OPTIMIZATION_NODE_OVERLAP.equals(optimizationStrategy)) {
      return MISLabelingAlgorithm.OPTIMIZATION_NODE_OVERLAP;
    } else {
      return MISLabelingAlgorithm.OPTIMIZATION_NONE;
    }
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final OptionHandler options = getOptionHandler();
    final MISLabelingAlgorithm al = options.getBool(ITEM_USE_OPTIMIZATION)
        ? (MISLabelingAlgorithm) new SALabeling()
        : new GreedyMISLabeling();

    al.setAutoFlippingEnabled(true);
    al.setOptimizationStrategy(translateOptimizationStrategy(options.getString(ITEM_OPTIMIZATION_STRATEGY)));
    if (al.getOptimizationStrategy() == MISLabelingAlgorithm.OPTIMIZATION_NONE) {
      al.setProfitModel(new LabelRanking());
    }
    al.setRemoveNodeOverlaps(!options.getBool(ITEM_ALLOW_NODE_OVERLAPS));
    al.setRemoveEdgeOverlaps(!options.getBool(ITEM_ALLOW_EDGE_OVERLAPS));
    al.setApplyPostprocessing(options.getBool(ITEM_USE_POSTPROCESSING));

    al.setSelection(LABEL_SELECTION_DP_KEY);

    final Graph2D graph = getGraph2D();

    prepareGraph(graph, options);
    try {
      final Graph2DView view = getGraph2DView();
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.UNBUFFERED);
      if (view == null) {
        layoutExecutor.doLayout(graph, al);
      } else {
        layoutExecutor.doLayout(view, al);
      }
    } finally {
      restoreGraph(graph, options);
    }

    graph.updateViews();
  }

  /**
   * Prepares a <code>graph</code> depending on the given options for the
   * module's labeling algorithm.
   * <br>
   * Additional resources created by this method have to be freed up by calling
   * {@link #restoreGraph(y.view.Graph2D, y.option.OptionHandler)} after
   * layout calculation.  
   * @param graph the graph to be prepared
   * @param options the options for the module's labeling algorithm
   */
  protected void prepareGraph(final Graph2D graph, final OptionHandler options) {
    final DataProvider labelSet = new LabelSetDP(
            graph,
            options.getBool(ITEM_CONSIDER_SELECTED_FEATURES_ONLY),
            options.getBool(ITEM_PLACE_NODE_LABELS),
            options.getBool(ITEM_PLACE_EDGE_LABELS),
            options.getBool(ITEM_CONSIDER_INVISIBLE_LABELS));
    graph.addDataProvider(LABEL_SELECTION_DP_KEY, labelSet);

    setupEdgeLabelModels(graph, options.getString(ITEM_EDGE_LABEL_MODEL), labelSet, options.getBool(ITEM_AUTO_ROTATE));
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's labeling algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    graph.removeDataProvider(LABEL_SELECTION_DP_KEY);
  }


  private static void setupEdgeLabelModels(
          final Graph2D graph,
          final String modelValue, final DataProvider labelFilter, final boolean autoRotate
  ) {
    if (VALUE_AS_IS.equals(modelValue)) {
      return;
    }

    final byte model;
    if (VALUE_CENTERED.equals(modelValue)) {
      model = EdgeLabel.CENTERED;
    } else if (VALUE_TWO_POS.equals(modelValue)) {
      model = EdgeLabel.TWO_POS;
    } else if (VALUE_SIX_POS.equals(modelValue)) {
      model = EdgeLabel.SIX_POS;
    } else if (VALUE_THREE_CENTER.equals(modelValue)) {
      model = EdgeLabel.THREE_CENTER;
    } else if (VALUE_CENTER_SLIDER.equals(modelValue)) {
      model = EdgeLabel.CENTER_SLIDER;
    } else if (VALUE_SIDE_SLIDER.equals(modelValue)) {
      model = EdgeLabel.SIDE_SLIDER;
    } else {
      // else if VALUE_FREE.equals(modelValue) or VALUE_BEST.equals(modelValue)
      model = EdgeLabel.FREE;
    }

    EdgeLabelModel labelModel = null;
    if (autoRotate) {
      labelModel = getEdgeLabelModel(model);
    }

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0; i < er.labelCount(); i++) {
        final EdgeLabel label = er.getLabel(i);
        if (labelFilter.getBool(label)) {
          if (labelModel != null) {
            label.setLabelModel(labelModel, labelModel.getDefaultParameter());
          } else {
            label.setModel(model);
            label.setModelParameter(label.getLabelModel().getDefaultParameter());
          }
        }
      }
    }
  }

  private static EdgeLabelModel getEdgeLabelModel(byte modelValue) {
    final EdgeLabelModel labelModel;
    if (EdgeLabel.CENTERED == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.CENTERED);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if (EdgeLabel.TWO_POS == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.TWO_POS);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if (EdgeLabel.SIX_POS == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.SIX_POS);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if (EdgeLabel.THREE_CENTER == modelValue) {
      labelModel = new RotatedDiscreteEdgeLabelModel(RotatedDiscreteEdgeLabelModel.THREE_CENTER);
      ((RotatedDiscreteEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if (EdgeLabel.CENTER_SLIDER == modelValue) {
      labelModel = new RotatedSliderEdgeLabelModel(RotatedSliderEdgeLabelModel.CENTER_SLIDER);
      ((RotatedSliderEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else if (EdgeLabel.SIDE_SLIDER == modelValue) {
      labelModel = new RotatedSliderEdgeLabelModel(RotatedSliderEdgeLabelModel.SIDE_SLIDER);
      ((RotatedSliderEdgeLabelModel) labelModel).setAutoRotationEnabled(true);
    } else {
      labelModel = null;
    }
    return labelModel;
  }

  /**
   * Selects the labels we want to set.
   */
  static class LabelSetDP extends DataProviderAdapter {
    private final boolean considerOnlySelected;
    private final Graph2D graph;
    private final boolean nodes;
    private final boolean edges;
    private final boolean invisible;

    LabelSetDP(Graph2D g,boolean sel,boolean n,boolean e,boolean uv) {
      considerOnlySelected = sel;
      graph = g;
      nodes = n;
      edges = e;
      invisible = uv;
    }

    public boolean getBool(Object o) {
      YLabel ylabel = (YLabel) o;
      if (!ylabel.isVisible() && !invisible) {
        return false;
      }
      if (o instanceof NodeLabel) {
        final NodeLabel l = (NodeLabel) o;
        if (l.getModel() == NodeLabel.INTERNAL) {
          return false;
        }
      }
      if (considerOnlySelected) {
        if ((o instanceof NodeLabel) && nodes) {
          final NodeLabel l = (NodeLabel) o;
          return graph.isSelected(l.getNode());
        }
        if ((o instanceof EdgeLabel) && edges) {
          final EdgeLabel l = (EdgeLabel) o;
          return graph.isSelected(l.getEdge());
        }
        return false;
      } else {
        if ((o instanceof NodeLabel) && nodes) {
          return true;
        }
        return (o instanceof EdgeLabel) && edges;
      }
    }
  }
}
