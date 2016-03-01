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

import y.base.EdgeCursor;
import y.base.NodeCursor;
import y.layout.ComponentLayouter;
import y.layout.EdgeLabelModel;
import y.layout.FreeEdgeLabelModel;
import y.layout.FreeNodeLabelModel;
import y.layout.NodeLabelModel;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.TreeReductionStage;
import y.option.OptionHandler;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.SmartEdgeLabelModel;
import y.view.SmartNodeLabelModel;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.tree.BalloonLayouter}.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/tree_layouter.html#tree_layouter">Section Tree
 *      Layout</a> in the yFiles for Java Developer's Guide
 */
public class BalloonLayoutModule extends LayoutModule {
  //// Module 'Balloon Layout'
  protected static final String MODULE_BALLOON = "BALLOON";

  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_ROOT_NODE_POLICY = "ROOT_NODE_POLICY";
  protected static final String VALUE_DIRECTED_ROOT = "DIRECTED_ROOT";
  protected static final String VALUE_CENTER_ROOT = "CENTER_ROOT";
  protected static final String VALUE_WEIGHTED_CENTER_ROOT = "WEIGHTED_CENTER_ROOT";
  protected static final String ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES = "ROUTING_STYLE_FOR_NON_TREE_EDGES";
  protected static final String VALUE_ROUTE_ORGANIC = "ROUTE_ORGANIC";
  protected static final String VALUE_ROUTE_ORTHOGONAL = "ROUTE_ORTHOGONAL";
  protected static final String VALUE_ROUTE_STRAIGHTLINE = "ROUTE_STRAIGHTLINE";
  protected static final String ITEM_ACT_ON_SELECTION_ONLY = "ACT_ON_SELECTION_ONLY";
  protected static final String ITEM_PREFERRED_CHILD_WEDGE = "PREFERRED_CHILD_WEDGE";
  protected static final String ITEM_PREFERRED_ROOT_WEDGE = "PREFERRED_ROOT_WEDGE";
  protected static final String ITEM_MINIMAL_EDGE_LENGTH = "MINIMAL_EDGE_LENGTH";
  protected static final String ITEM_COMPACTNESS_FACTOR = "COMPACTNESS_FACTOR";
  protected static final String ITEM_ALLOW_OVERLAPS = "ALLOW_OVERLAPS";
  protected static final String ITEM_BALLOON_FROM_SKETCH = "FROM_SKETCH";
  protected static final String ITEM_PLACE_CHILDREN_INTERLEAVED = "PLACE_CHILDREN_INTERLEAVED";
  protected static final String ITEM_STRAIGHTEN_CHAINS = "STRAIGHTEN_CHAINS";
  
  //// Section 'Labeling'
  protected static final String SECTION_LABELING = "LABELING";
  // Section 'Labeling' items
  protected static final String ITEM_NODE_LABELING_STYLE = "NODE_LABELING_STYLE";
  protected static final String VALUE_NODE_LABELING_STYLE_NONE = "NODE_LABELING_STYLE_NONE";
  protected static final String VALUE_NODE_LABELING_STYLE_HORIZONTAL = "NODE_LABELING_STYLE_HORIZONTAL";
  protected static final String VALUE_NODE_LABELING_STYLE_RAYLIKE_LEAVES = "NODE_LABELING_STYLE_RAYLIKE_LEAVES";
  protected static final String VALUE_NODE_LABELING_STYLE_CONSIDER_CURRENT_POSITION = "NODE_LABELING_STYLE_CONSIDER_CURRENT_POSITION";
  protected static final String ITEM_INTEGRATED_EDGE_LABELING = "INTEGRATED_EDGE_LABELING";

  /**
   * Creates an instance of this module.
   */
  public BalloonLayoutModule() {
    super(MODULE_BALLOON);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    // Defaults provider
    final BalloonLayouter defaults = new BalloonLayouter();

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate section
    options.addEnum(ITEM_ROOT_NODE_POLICY, new String[]{
        VALUE_DIRECTED_ROOT,
        VALUE_CENTER_ROOT,
        VALUE_WEIGHTED_CENTER_ROOT
    }, 0);
    options.addEnum(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES, new String[]{
        VALUE_ROUTE_ORGANIC,
        VALUE_ROUTE_ORTHOGONAL,
        VALUE_ROUTE_STRAIGHTLINE
    }, 0);
    options.addBool(ITEM_ACT_ON_SELECTION_ONLY, false);
    options.addInt(ITEM_PREFERRED_CHILD_WEDGE, defaults.getPreferredChildWedge(), 1, 359);
    options.addInt(ITEM_PREFERRED_ROOT_WEDGE, defaults.getPreferredRootWedge(), 1, 360);
    options.addInt(ITEM_MINIMAL_EDGE_LENGTH, defaults.getMinimalEdgeLength(), 10, 400);
    options.addDouble(ITEM_COMPACTNESS_FACTOR, defaults.getCompactnessFactor(), 0.1, 0.9);
    options.addBool(ITEM_ALLOW_OVERLAPS, defaults.getAllowOverlaps());
    options.addBool(ITEM_BALLOON_FROM_SKETCH, defaults.isFromSketchModeEnabled());
    options.addBool(ITEM_PLACE_CHILDREN_INTERLEAVED,
        defaults.getInterleavedMode() == BalloonLayouter.INTERLEAVED_MODE_ALL_NODES);
    options.addBool(ITEM_STRAIGHTEN_CHAINS, defaults.isChainStraighteningModeEnabled());

    //// Section 'Labeling'
    options.useSection(SECTION_LABELING);
    // Populate section
    options.addBool(ITEM_INTEGRATED_EDGE_LABELING, true);
    options.addEnum(ITEM_NODE_LABELING_STYLE, new String[]{
        VALUE_NODE_LABELING_STYLE_NONE,
        VALUE_NODE_LABELING_STYLE_HORIZONTAL,
        VALUE_NODE_LABELING_STYLE_RAYLIKE_LEAVES,
        VALUE_NODE_LABELING_STYLE_CONSIDER_CURRENT_POSITION
    }, 3);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final BalloonLayouter balloon = new BalloonLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(balloon, options);

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    launchLayouter(balloon);
  }

  /**
   * Sets edge and node label models depending on the given options.
   * @param graph the graph whose label models may be changed
   * @param options the layout options that determine whether or not to
   * change label models
   */
  private void prepareGraph(final Graph2D graph, final OptionHandler options) {
    if (options.getBool(ITEM_INTEGRATED_EDGE_LABELING)) {
      setupEdgeLabelModel(graph);
    }
    if (VALUE_NODE_LABELING_STYLE_RAYLIKE_LEAVES.equals(options.getString(ITEM_NODE_LABELING_STYLE))) {
      setupNodeLabelModel(graph);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param balloon the <code>BalloonLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final BalloonLayouter balloon, final OptionHandler options) {
    ((ComponentLayouter) balloon.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);

    if (options.get(ITEM_ROOT_NODE_POLICY).equals(VALUE_DIRECTED_ROOT)) {
      balloon.setRootNodePolicy(BalloonLayouter.DIRECTED_ROOT);
    } else if (options.get(ITEM_ROOT_NODE_POLICY).equals(VALUE_CENTER_ROOT)) {
      balloon.setRootNodePolicy(BalloonLayouter.CENTER_ROOT);
    } else {
      balloon.setRootNodePolicy(BalloonLayouter.WEIGHTED_CENTER_ROOT);
    }

    balloon.setPreferredChildWedge(options.getInt(ITEM_PREFERRED_CHILD_WEDGE));
    balloon.setPreferredRootWedge(options.getInt(ITEM_PREFERRED_ROOT_WEDGE));
    balloon.setMinimalEdgeLength(options.getInt(ITEM_MINIMAL_EDGE_LENGTH));
    balloon.setCompactnessFactor(options.getDouble(ITEM_COMPACTNESS_FACTOR));
    balloon.setAllowOverlaps(options.getBool(ITEM_ALLOW_OVERLAPS));
    balloon.setFromSketchModeEnabled(options.getBool(ITEM_BALLOON_FROM_SKETCH));
    
    if (options.getBool(ITEM_INTEGRATED_EDGE_LABELING)) {
      balloon.setIntegratedEdgeLabelingEnabled(true);
    } else {
      balloon.setIntegratedEdgeLabelingEnabled(false);
    }
    
    balloon.setChainStraighteningModeEnabled(options.getBool(ITEM_STRAIGHTEN_CHAINS));
    balloon.setInterleavedMode(options.getBool(ITEM_PLACE_CHILDREN_INTERLEAVED)
        ? BalloonLayouter.INTERLEAVED_MODE_ALL_NODES : BalloonLayouter.INTERLEAVED_MODE_OFF);
    balloon.setIntegratedNodeLabelingEnabled(false);
    balloon.setConsiderNodeLabelsEnabled(false);
    
    final String nodeLabelingStyle = options.getString(ITEM_NODE_LABELING_STYLE);
    if (VALUE_NODE_LABELING_STYLE_RAYLIKE_LEAVES.equals(nodeLabelingStyle)) {
      balloon.setIntegratedNodeLabelingEnabled(true);
      balloon.setNodeLabelingPolicy(BalloonLayouter.NODE_LABELING_MIXED);
    } else if (VALUE_NODE_LABELING_STYLE_CONSIDER_CURRENT_POSITION.equals(nodeLabelingStyle)) {
      balloon.setConsiderNodeLabelsEnabled(true);
    } else if (VALUE_NODE_LABELING_STYLE_HORIZONTAL.equals(nodeLabelingStyle)) {
      balloon.setIntegratedNodeLabelingEnabled(true);
      balloon.setNodeLabelingPolicy(BalloonLayouter.NODE_LABELING_HORIZONTAL);
    }

    balloon.setSubgraphLayouterEnabled(options.getBool(ITEM_ACT_ON_SELECTION_ONLY));

    //configure tree reduction stage
    final TreeReductionStage trs = new TreeReductionStage();
    balloon.appendStage(trs);
    if (VALUE_ROUTE_ORGANIC.equals(options.get(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
      trs.setNonTreeEdgeRouter(new OrganicEdgeRouter());
      trs.setNonTreeEdgeSelectionKey(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
    } else if (VALUE_ROUTE_ORTHOGONAL.equals(options.get(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
      final EdgeRouter orthogonal = new EdgeRouter();
      orthogonal.setReroutingEnabled(true);
      orthogonal.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      trs.setNonTreeEdgeSelectionKey(orthogonal.getSelectedEdgesDpKey());
      trs.setNonTreeEdgeRouter(orthogonal);
    } else if (VALUE_ROUTE_STRAIGHTLINE.equals(options.get(ITEM_ROUTING_STYLE_FOR_NON_TREE_EDGES))) {
      trs.setNonTreeEdgeRouter(trs.createStraightlineRouter());
    }
  }

  /**
   * Guarantees that all edge labels have a free edge label model.
   */
  private void setupEdgeLabelModel(final Graph2D graph) {
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final EdgeRealizer er = graph.getRealizer(ec.edge());
      for (int i = 0; i < er.labelCount(); i++) {
        final EdgeLabel el = er.getLabel(i);
        final EdgeLabelModel labelModel = el.getLabelModel();
        if (!isFreeModel(labelModel)) {
          //the free model that is set if an edge label has a non-free model
          final SmartEdgeLabelModel defaultLabelModel = new SmartEdgeLabelModel(); 
          el.setLabelModel(defaultLabelModel, defaultLabelModel.getDefaultParameter());
        }
      }
    }
  }

  private static boolean isFreeModel(final EdgeLabelModel model) {
    return (model instanceof FreeEdgeLabelModel) || (model instanceof SmartEdgeLabelModel);
  }

  /** Guarantees that all node labels have a free node label model. */
  private void setupNodeLabelModel(final Graph2D graph) {
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final NodeRealizer nr = graph.getRealizer(nc.node());
      for (int i = 0; i < nr.labelCount(); i++) {
        final NodeLabel nl = nr.getLabel(i);
        final NodeLabelModel labelModel = nl.getLabelModel();
        if (!isFreeModel(labelModel)) {
          //the free model that is set if a node label has a non-free model
          final SmartNodeLabelModel defaultLabelModel = new SmartNodeLabelModel();
          nl.setLabelModel(defaultLabelModel, defaultLabelModel.getDefaultParameter());
        }
      }
    }
  }

  private static boolean isFreeModel(final NodeLabelModel model) {
    return (model instanceof FreeNodeLabelModel) || (model instanceof SmartNodeLabelModel);
  }
}
