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
import y.layout.ComponentLayouter;
import y.layout.CompositeLayoutStage;
import y.layout.LabelLayoutConstants;
import y.layout.LabelLayoutDataRefinement;
import y.layout.LabelLayoutTranslator;
import y.layout.LabelRanking;
import y.layout.OrientationLayouter;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.PreferredPlacementDescriptor;
import y.layout.grouping.FixedGroupLayoutStage;
import y.layout.grouping.GroupNodeHider;
import y.layout.hierarchic.BFSLayerer;
import y.layout.hierarchic.ClassicLayerSequencer;
import y.layout.hierarchic.HierarchicGroupLayouter;
import y.layout.hierarchic.HierarchicLayouter;
import y.layout.hierarchic.LayerSequencer;
import y.layout.labeling.GreedyMISLabeling;
import y.option.ConstraintManager;
import y.option.EnumOptionItem;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Selections;
import y.view.SmartEdgeLabelModel;
import y.view.hierarchy.HierarchyManager;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.hierarchic.HierarchicLayouter}
 * and {@link y.layout.hierarchic.HierarchicGroupLayouter}.
 *
 */
public class HierarchicLayoutModule extends LayoutModule {
  //// Module 'Hierarchic Layout'
  protected static final String MODULE_HIERARCHIC = "HIERARCHIC";
  
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT = "LAYOUT";
  // Section 'Layout' items
  protected static final String ITEM_MINIMAL_LAYER_DISTANCE = "MINIMAL_LAYER_DISTANCE";
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_MINIMAL_EDGE_DISTANCE = "MINIMAL_EDGE_DISTANCE";
  protected static final String ITEM_MINIMAL_FIRST_SEGMENT_LENGTH = "MINIMAL_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MAXIMAL_DURATION = "MAXIMAL_DURATION";
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String ITEM_NODE_PLACEMENT = "NODE_PLACEMENT";
  protected static final String VALUE_PENDULUM = "PENDULUM";
  protected static final String VALUE_LINEAR_SEGMENTS = "LINEAR_SEGMENTS";
  protected static final String VALUE_POLYLINE = "POLYLINE";
  protected static final String VALUE_TREE = "TREE";
  protected static final String VALUE_SIMPLEX = "SIMPLEX";
  protected static final String VALUE_MEDIAN_SIMPLEX = "MEDIAN_SIMPLEX";
  protected static final String ITEM_EDGE_ROUTING = "EDGE_ROUTING";
  protected static final String VALUE_POLYLINE_EDGE = "POLYLINE";
  protected static final String VALUE_ORTHOGONAL = "ORTHOGONAL";
  protected static final String ITEM_BACKLOOP_ROUTING = "BACKLOOP_ROUTING";
  protected static final String ITEM_ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  
  //// Section 'Node Rank'
  protected static final String SECTION_NODE_RANK = "NODE_RANK";
  // Section 'Node Rank' items
  protected static final String ITEM_RANKING_POLICY = "RANKING_POLICY";
  protected static final String VALUE_NO_RERANKING = "NO_RERANKING";
  protected static final String VALUE_DOWNSHIFT_NODES = "DOWNSHIFT_NODES";
  protected static final String VALUE_TIGHT_TREE = "TIGHT_TREE";
  protected static final String VALUE_SIMPLEX_RANK = "SIMPLEX";
  protected static final String VALUE_AS_IS_RANK = "AS_IS";
  protected static final String VALUE_BFS = "BFS";
  
  //// Section 'Node Order'
  protected static final String SECTION_NODE_ORDER = "NODE_ORDER";
  // Section 'Node Order' items
  protected static final String ITEM_WEIGHT_HEURISTIC = "WEIGHT_HEURISTIC";
  protected static final String VALUE_BARYCENTER = "BARYCENTER";
  protected static final String VALUE_MEDIAN = "MEDIAN";
  protected static final String ITEM_USE_TRANSPOSITION = "USE_TRANSPOSITION";
  protected static final String ITEM_REMOVE_FALSE_CROSSINGS = "REMOVE_FALSE_CROSSINGS";
  protected static final String ITEM_RANDOMIZATION_ROUNDS = "RANDOMIZATION_ROUNDS";
  
  //// Section 'Labeling'
  protected static final String SECTION_LABELING = "LABELING";
  //-
  protected static final String ITEM_EDGE_LABELING = "EDGE_LABELING";
  protected static final String VALUE_NONE = "NONE";
  protected static final String VALUE_HIERARCHIC = "HIERARCHIC";
  protected static final String VALUE_GENERIC = "GENERIC";
  protected static final String ITEM_EDGE_LABEL_MODEL = "EDGE_LABEL_MODEL";
  protected static final String VALUE_BEST = "BEST";
  protected static final String VALUE_AS_IS = "AS_IS";
  protected static final String VALUE_CENTER_SLIDER = "CENTER_SLIDER";
  protected static final String VALUE_SIDE_SLIDER = "SIDE_SLIDER";
  protected static final String VALUE_FREE = "FREE";
  
  //// Section 'Grouping'
  protected static final String SECTION_GROUPING = "GROUPING";
  // Section 'Grouping' items
  protected static final String ITEM_GROUP_POLICY = "GROUP_LAYOUT_POLICY";
  protected static final String VALUE_LAYOUT_GROUPS = "LAYOUT_GROUPS";
  protected static final String VALUE_FIX_GROUPS = "FIX_GROUPS";
  protected static final String VALUE_IGNORE_GROUPS = "IGNORE_GROUPS";
  protected static final String ITEM_ENABLE_GLOBAL_SEQUENCING = "ENABLE_GLOBAL_SEQUENCING";
  
  /**
   * Creates an instance of this module.
   */
  public HierarchicLayoutModule() {
    super(MODULE_HIERARCHIC);
    setPortIntersectionCalculatorEnabled(true);
  }
  
  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Defaults provider
    final HierarchicGroupLayouter defaults = new HierarchicGroupLayouter();
    final ClassicLayerSequencer defaultsSequencer = (ClassicLayerSequencer) defaults.getLayerSequencer();

    //// Section 'Layout'
    options.useSection(SECTION_LAYOUT);
    // Populate items
    options.addInt(ITEM_MINIMAL_LAYER_DISTANCE, (int)defaults.getMinimalLayerDistance());
    options.addInt(ITEM_MINIMAL_NODE_DISTANCE, (int)defaults.getMinimalNodeDistance());
    options.addInt(ITEM_MINIMAL_EDGE_DISTANCE, (int)defaults.getMinimalEdgeDistance());
    options.addInt(ITEM_MINIMAL_FIRST_SEGMENT_LENGTH, (int) defaults.getMinimalFirstSegmentLength());
    options.addInt(ITEM_MAXIMAL_DURATION, 5);
    options.addEnum(ITEM_ORIENTATION, new String[]{
        VALUE_TOP_TO_BOTTOM,
        VALUE_LEFT_TO_RIGHT,
        VALUE_BOTTOM_TO_TOP,
        VALUE_RIGHT_TO_LEFT
    }, 0);
    options.addEnum(ITEM_NODE_PLACEMENT, new String[]{
        VALUE_PENDULUM,
        VALUE_LINEAR_SEGMENTS,
        VALUE_POLYLINE,
        VALUE_TREE,
        VALUE_SIMPLEX,
        VALUE_MEDIAN_SIMPLEX
    }, defaults.getLayoutStyle());
    options.addEnum(ITEM_EDGE_ROUTING, new String[]{
        VALUE_POLYLINE_EDGE,
        VALUE_ORTHOGONAL
    }, defaults.getRoutingStyle());
    options.addBool(ITEM_BACKLOOP_ROUTING, false);
    options.addBool(ITEM_ACT_ON_SELECTION_ONLY, false);

    //// Section 'Node Rank'
    options.useSection(SECTION_NODE_RANK);
    // Populate items
    options.addEnum(ITEM_RANKING_POLICY, new String[]{
        VALUE_NO_RERANKING,
        VALUE_DOWNSHIFT_NODES,
        VALUE_TIGHT_TREE,
        VALUE_SIMPLEX_RANK,
        VALUE_AS_IS_RANK,
        VALUE_BFS
    }, 2);

    //// Section 'Node Order'
    options.useSection(SECTION_NODE_ORDER);
    // Populate items
    options.addEnum(ITEM_WEIGHT_HEURISTIC, new String[]{
        VALUE_BARYCENTER,
        VALUE_MEDIAN
    }, defaultsSequencer.getWeightHeuristic());
    options.addBool(ITEM_USE_TRANSPOSITION, defaultsSequencer.getUseTransposition());
    options.addBool(ITEM_REMOVE_FALSE_CROSSINGS, defaults.getRemoveFalseCrossings());
    options.addInt(ITEM_RANDOMIZATION_ROUNDS, defaultsSequencer.getRandomizationRounds());

    //// Section 'Labeling'
    options.useSection(SECTION_LABELING);
    // Populate items
    final EnumOptionItem itemEdgeLabeling = options.addEnum(ITEM_EDGE_LABELING, new String[]{
        VALUE_NONE,
        VALUE_HIERARCHIC,
        VALUE_GENERIC
    }, 0);
    final EnumOptionItem itemEdgeLabelModel = options.addEnum(ITEM_EDGE_LABEL_MODEL, new String[]{
        VALUE_BEST,
        VALUE_AS_IS,
        VALUE_CENTER_SLIDER,
        VALUE_SIDE_SLIDER,
        VALUE_FREE
    }, 0);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemEdgeLabeling, VALUE_NONE, itemEdgeLabelModel, true);

    //// Section 'Grouping'
    options.useSection(SECTION_GROUPING);
    // Populate items
    options.addEnum(ITEM_GROUP_POLICY, new String[]{
        VALUE_LAYOUT_GROUPS,
        VALUE_FIX_GROUPS,
        VALUE_IGNORE_GROUPS
    }, 0);
    options.addBool(ITEM_ENABLE_GLOBAL_SEQUENCING, true);
    
    return options;
  }
  
  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final HierarchicGroupLayouter hierarchic = new HierarchicGroupLayouter();

    final OptionHandler options = getOptionHandler();
    configure(hierarchic, options);

    final Graph2D graph = getGraph2D();

    prepareGraph(graph, options);
    try {
      launchLayouter(hierarchic);
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
    String el = options.getString(ITEM_EDGE_LABELING);
    if (!el.equals(VALUE_NONE)) {
      setupEdgeLabelModel(graph, el, options.getString(ITEM_EDGE_LABEL_MODEL));
    }
    
    if (options.getString(ITEM_RANKING_POLICY).equals(VALUE_BFS)) {
      // backup existing data providers to prevent loss of user settings
      backupDataProvider(graph, BFSLayerer.CORE_NODES);
      graph.addDataProvider(BFSLayerer.CORE_NODES, Selections.createSelectionNodeMap(graph));
    }
    
    if (options.getBool(ITEM_BACKLOOP_ROUTING)) {
      final DataProvider oldSdp = graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      final DataProvider oldTdp = graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      PortConstraint spc = null, tpc = null;
      if (options.get(ITEM_ORIENTATION).equals(VALUE_TOP_TO_BOTTOM)) {
        spc = PortConstraint.create(PortConstraint.SOUTH);
        tpc = PortConstraint.create(PortConstraint.NORTH);
      } else if (options.get(ITEM_ORIENTATION).equals(VALUE_LEFT_TO_RIGHT)) {
        spc = PortConstraint.create(PortConstraint.EAST);
        tpc = PortConstraint.create(PortConstraint.WEST);
      } else if (options.get(ITEM_ORIENTATION).equals(VALUE_BOTTOM_TO_TOP)) {
        spc = PortConstraint.create(PortConstraint.NORTH);
        tpc = PortConstraint.create(PortConstraint.SOUTH);
      } else if (options.get(ITEM_ORIENTATION).equals(VALUE_RIGHT_TO_LEFT)) {
        spc = PortConstraint.create(PortConstraint.WEST);
        tpc = PortConstraint.create(PortConstraint.EAST);
      }
      final DataProvider sdp = new BackloopConstraintDP(spc, oldSdp);
      final DataProvider tdp = new BackloopConstraintDP(tpc, oldTdp);
      
      // Re-register (overwrite) the keys with the new data providers
      // Note oldSdp, oldTdp are contained in the new dps, no information is lost
      graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sdp);
      graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tdp);
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(Graph2D graph, OptionHandler options) {
    if (options.getBool(ITEM_BACKLOOP_ROUTING)) {
      final BackloopConstraintDP sdp, tdp;
      sdp = (BackloopConstraintDP) graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      tdp = (BackloopConstraintDP) graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);

      // If there is an old data provider re-register it
      // else deregister data providers registered by this module
      final DataProvider oldSdp = sdp.delegate;
      if (oldSdp != null) {
        graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, oldSdp);
      } else {

        graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      }

      final DataProvider oldTdp = tdp.delegate;
      if (oldTdp != null) {
        graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, oldTdp);
      } else {
        graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
      }
    }

    //cleanup BFSLayerer key if present
    if (options.getString(ITEM_RANKING_POLICY).equals(VALUE_BFS)) {
      // remove the data providers set by this module by restoring the initial state
      restoreDataProvider(graph, BFSLayerer.CORE_NODES);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2D</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param hierarchic the <code>HierarchicGroupLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(HierarchicGroupLayouter hierarchic, OptionHandler options) {
    ((ComponentLayouter) hierarchic.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    hierarchic.setRemoveFalseCrossings(options.getBool(ITEM_REMOVE_FALSE_CROSSINGS));
    hierarchic.setMaximalDuration(options.getInt(ITEM_MAXIMAL_DURATION) * 1000);
    hierarchic.setMinimalNodeDistance(options.getInt(ITEM_MINIMAL_NODE_DISTANCE));
    hierarchic.setMinimalEdgeDistance(options.getInt(ITEM_MINIMAL_EDGE_DISTANCE));
    hierarchic.setMinimalFirstSegmentLength(options.getInt(ITEM_MINIMAL_FIRST_SEGMENT_LENGTH));
    hierarchic.setMinimalLayerDistance(options.getInt(ITEM_MINIMAL_LAYER_DISTANCE));

    if (options.get(ITEM_ORIENTATION).equals(VALUE_TOP_TO_BOTTOM)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.TOP_TO_BOTTOM);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_LEFT_TO_RIGHT)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.LEFT_TO_RIGHT);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_BOTTOM_TO_TOP)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.BOTTOM_TO_TOP);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_RIGHT_TO_LEFT)) {
      hierarchic.setLayoutOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    }

    hierarchic.setGlobalSequencingActive(options.getBool(SECTION_GROUPING, ITEM_ENABLE_GLOBAL_SEQUENCING));

    configureLabeling(hierarchic, options);

    String ls = options.getString(ITEM_NODE_PLACEMENT);
    if (ls.equals(VALUE_PENDULUM)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.PENDULUM);
    } else if (ls.equals(VALUE_POLYLINE)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.POLYLINE);
    } else if (ls.equals(VALUE_LINEAR_SEGMENTS)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.LINEAR_SEGMENTS);
    } else if (ls.equals(VALUE_TREE)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.TREE);
    } else if (ls.equals(VALUE_SIMPLEX)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.SIMPLEX);
    } else if (ls.equals(VALUE_MEDIAN_SIMPLEX)) {
      hierarchic.setLayoutStyle(HierarchicLayouter.MEDIAN_SIMPLEX);
    }
    String rs = options.getString(ITEM_EDGE_ROUTING);
    if (rs.equals(VALUE_POLYLINE)) {
      hierarchic.setRoutingStyle(HierarchicLayouter.ROUTE_POLYLINE);
    } else if (rs.equals(VALUE_ORTHOGONAL)) {
      hierarchic.setRoutingStyle(HierarchicLayouter.ROUTE_ORTHOGONAL);
    }

    hierarchic.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));

    String rp = options.getString(ITEM_RANKING_POLICY);
    if (rp.equals(VALUE_AS_IS_RANK)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_FROM_SKETCH);
    } else if (rp.equals(VALUE_SIMPLEX_RANK)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_OPTIMAL);
    } else if (rp.equals(VALUE_NO_RERANKING)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_TOPMOST);
    } else if (rp.equals(VALUE_DOWNSHIFT_NODES)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_DOWNSHIFT);
    } else if (rp.equals(VALUE_TIGHT_TREE)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_HIERARCHICAL_TIGHT_TREE);
    } else if (rp.equals(VALUE_BFS)) {
      hierarchic.setLayeringStrategy(HierarchicLayouter.LAYERING_BFS);
    }

    String  wh = options.getString(ITEM_WEIGHT_HEURISTIC);

    LayerSequencer layerSequencer = hierarchic.getLayerSequencer();
    if (layerSequencer instanceof ClassicLayerSequencer) {
      ClassicLayerSequencer cls = (ClassicLayerSequencer)layerSequencer;
      if (wh.equals(VALUE_MEDIAN)) {
        cls.setWeightHeuristic(ClassicLayerSequencer.MEDIAN_HEURISTIC);
      } else {
        cls.setWeightHeuristic(ClassicLayerSequencer.BARYCENTER_HEURISTIC);
      }
      cls.setUseTransposition(options.getBool(ITEM_USE_TRANSPOSITION));
      cls.setRandomizationRounds(options.getInt(SECTION_NODE_ORDER, ITEM_RANDOMIZATION_ROUNDS));
      hierarchic.setLayerSequencer(cls);
    }
    
    if (HierarchyManager.containsGroupNodes(getGraph2D())) {
      if (options.get(ITEM_GROUP_POLICY).equals(VALUE_IGNORE_GROUPS)) {
        hierarchic.prependStage(new GroupNodeHider());
      } else {
        if (options.get(ITEM_GROUP_POLICY).equals(VALUE_FIX_GROUPS)) {
          final FixedGroupLayoutStage fixedGroupLayoutStage = new FixedGroupLayoutStage();
          if (options.get(ITEM_EDGE_ROUTING).equals(VALUE_ORTHOGONAL)) {
            fixedGroupLayoutStage.setInterEdgeRoutingStyle(FixedGroupLayoutStage.ROUTING_STYLE_ORTHOGONAL);
          }
          hierarchic.prependStage(fixedGroupLayoutStage);
        }
      }
    }
  }

  private void configureLabeling(final HierarchicGroupLayouter hierarchic, final OptionHandler options) {
    final String el = options.getString(ITEM_EDGE_LABELING);
    if (!el.equals(VALUE_NONE)) {
      if (el.equals(VALUE_GENERIC)) {
        final GreedyMISLabeling la = new GreedyMISLabeling();
        la.setPlaceNodeLabels(false);
        la.setPlaceEdgeLabels(true);
        la.setAutoFlippingEnabled(true);
        la.setProfitModel(new LabelRanking());
        hierarchic.setLabelLayouter(la);
        hierarchic.setLabelLayouterEnabled(true);
      } else if (el.equals(MODULE_HIERARCHIC)) {
        CompositeLayoutStage ll = new CompositeLayoutStage();
        ll.appendStage(new LabelLayoutTranslator());
        ll.appendStage(new LabelLayoutDataRefinement());
        hierarchic.setLabelLayouter(ll);
        hierarchic.setLabelLayouterEnabled(true);
      }
    } else {
      hierarchic.setLabelLayouterEnabled(false);
    }
  }

  static final class BackloopConstraintDP extends DataProviderAdapter {
    private final PortConstraint pc;
    private final DataProvider delegate;
    private static final PortConstraint anySide = PortConstraint.create(PortConstraint.ANY_SIDE);
    BackloopConstraintDP(PortConstraint pc, DataProvider delegate) {
      this.pc = pc;
      this.delegate = delegate;
    }
    
    public Object get(Object o) {
      if (delegate != null) {
        final Object delegateResult = delegate.get(o);
        if (delegateResult != null) {
          return delegateResult;
        }
      } 
      final Edge e = (Edge)o;
      if (e.isSelfLoop()) {
        return anySide;
      } else {
        return pc;
      }
    }
  }
  
  private void setupEdgeLabelModel(final Graph2D graph, final String edgeLabeling, String edgeLabelModel) {
    if (edgeLabeling.equals(VALUE_NONE) || edgeLabelModel.equals(VALUE_AS_IS)) {
      return; //nothing to do
    }
    
    if (edgeLabelModel.equals(VALUE_BEST)) {
      if (edgeLabeling.equals(VALUE_GENERIC)) {
        edgeLabelModel = VALUE_SIDE_SLIDER;
      } else if (edgeLabeling.equals(MODULE_HIERARCHIC)) {
        edgeLabelModel = VALUE_FREE;
      }
    }
    
    byte model = EdgeLabel.SIDE_SLIDER;
    byte preferredSide = LabelLayoutConstants.PLACE_RIGHT_OF_EDGE;
    if (edgeLabelModel.equals(VALUE_CENTER_SLIDER)) {
      model = EdgeLabel.CENTER_SLIDER;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    } else if (edgeLabelModel.equals(VALUE_FREE)) {
      model = EdgeLabel.FREE;
      preferredSide = LabelLayoutConstants.PLACE_ON_EDGE;
    }
    
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      Edge e = ec.edge();
      EdgeRealizer er = graph.getRealizer(e);
      for(int i = 0; i < er.labelCount(); i++) {
        EdgeLabel el = er.getLabel(i);
        if (EdgeLabel.FREE != model || !(el.getLabelModel() instanceof SmartEdgeLabelModel)) {
          // SmartEdgeLabelModel is an enhanced free model,
          // therefore it is ok to stick with it instead of
          // replacing it with FreeEdgeLabelModel
          el.setModel(model);
        }
        setPreferredSide(el, preferredSide);
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
