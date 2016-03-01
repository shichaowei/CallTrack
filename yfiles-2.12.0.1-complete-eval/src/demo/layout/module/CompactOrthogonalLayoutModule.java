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

import y.layout.ComponentLayouter;
import y.layout.Layouter;
import y.layout.PartitionLayouter;
import y.layout.PartitionLayouter.ComponentPartitionPlacer;
import y.layout.grouping.GroupNodeHider;
import y.layout.orthogonal.CompactOrthogonalLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.OrthogonalPatternEdgeRouter;
import y.layout.router.polyline.EdgeLayoutDescriptor;
import y.layout.router.polyline.EdgeRouter;
import y.layout.router.polyline.PenaltySettings;
import y.option.ConstraintManager;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.view.Graph2DView;

import java.awt.Dimension;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.orthogonal.CompactOrthogonalLayouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/compact_orthogonal_layouter.html#compact_orthogonal_layouter">Section Compact Orthogonal Layout</a> in the yFiles for Java Developer's Guide
 */
public class CompactOrthogonalLayoutModule extends LayoutModule {
  //// Module 'Compact Orthogonal Layout'
  protected static final String MODULE_COMPACT_ORTHOGONAL = "COMPACT_ORTHOGONAL";

  //// Section 'default' items
  protected static final String ITEM_ORTHOGONAL_LAYOUT_STYLE = "ORTHOGONAL_LAYOUT_STYLE";
  protected static final String VALUE_NORMAL = "NORMAL";
  protected static final String VALUE_NORMAL_TREE = "NORMAL_TREE";
  protected static final String VALUE_FIXED_MIXED = "FIXED_MIXED";
  protected static final String VALUE_FIXED_BOX_NODES = "FIXED_BOX_NODES";
  protected static final String ITEM_PLACEMENT_STRATEGY = "PLACEMENT_STRATEGY";
  protected static final String VALUE_STYLE_ROWS = "STYLE_ROWS";
  protected static final String VALUE_STYLE_PACKED_COMPACT_RECTANGLE = "STYLE_PACKED_COMPACT_RECTANGLE";
  protected static final String ITEM_USE_VIEW_ASPECT_RATIO = "USE_VIEW_ASPECT_RATIO";
  protected static final String ITEM_ASPECT_RATIO = "ASPECT_RATIO";
  protected static final String ITEM_GRID = "GRID";
  protected static final String TITLE_INTER_EDGE_ROUTER = "INTER_EDGE_ROUTER";
  protected static final String ITEM_EDGE_ROUTER = "EDGE_ROUTER";
  protected static final String VALUE_EDGE_ROUTER_CHANNEL_FAST = "EDGE_ROUTER_CHANNEL_FAST";
  protected static final String VALUE_EDGE_ROUTER_CHANNEL_HQ = "EDGE_ROUTER_CHANNEL_HQ";
  //for orthogonal edge routing we take the PartitionLayouter.PolylineInterEdgeRouter
  protected static final String VALUE_EDGE_ROUTER_ORTHOGONAL = "EDGE_ROUTER_ORTHOGONAL";
  protected static final String ITEM_ROUTE_ALL_EDGES = "ROUTE_ALL_EDGES";
  protected static final String ITEM_BEND_COST = "BEND_COST";
  protected static final String ITEM_NODE_CROSSING_COST = "NODE_CROSSING_COST";
  protected static final String ITEM_MINIMUM_DISTANCE = "MINIMUM_DISTANCE";
  protected static final String ITEM_EDGE_CROSSING_COST = "EDGE_CROSSING_COST";
  protected static final String ITEM_CENTER_TO_SPACE_RATIO = "SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH";

  /**
   * Creates an instance of this module.
   */
  public CompactOrthogonalLayoutModule() {
    super(MODULE_COMPACT_ORTHOGONAL);
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
    final CompactOrthogonalLayouter defaults = new CompactOrthogonalLayouter();
    if (false) {
        defaults.setInterEdgeRouter(new PartitionLayouter.PolylineInterEdgeRouter());
    } else {
        defaults.setInterEdgeRouter(new PartitionLayouter.ChannelInterEdgeRouter());
    }
    defaults.setPartitionPlacer(new ComponentPartitionPlacer());
    defaults.setCoreLayouter(new OrthogonalLayouter());
    final OrthogonalPatternEdgeRouter defaultsOper = new OrthogonalPatternEdgeRouter();
    final ChannelEdgeRouter.OrthogonalShortestPathPathFinder defaultsPsppf =
        new ChannelEdgeRouter.OrthogonalShortestPathPathFinder();

    // Populate default section
    final int styleIndex;
    switch (((OrthogonalLayouter) defaults.getCoreLayouter()).getLayoutStyle()) {
      case OrthogonalLayouter.NORMAL_STYLE:
      default:
        styleIndex = 0;
        break;
      case OrthogonalLayouter.NORMAL_TREE_STYLE:
        styleIndex = 1;
        break;
      case OrthogonalLayouter.FIXED_MIXED_STYLE:
        styleIndex = 2;
        break;
      case OrthogonalLayouter.FIXED_BOX_STYLE:
        styleIndex = 3;
        break;
    }
    options.addEnum(ITEM_ORTHOGONAL_LAYOUT_STYLE, new String[]{
        VALUE_NORMAL,
        VALUE_NORMAL_TREE,
        VALUE_FIXED_MIXED,
        VALUE_FIXED_BOX_NODES
    }, styleIndex);

    int compStyleIndex;
    switch (((ComponentPartitionPlacer) defaults.getPartitionPlacer()).getComponentLayouter().getStyle()) {
      case ComponentLayouter.STYLE_ROWS:
      default:
        compStyleIndex = 0;
        break;
      case ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE:
        compStyleIndex = 1;
        break;
    }
    options.addEnum(ITEM_PLACEMENT_STRATEGY, new String[]{
        VALUE_STYLE_ROWS,
        VALUE_STYLE_PACKED_COMPACT_RECTANGLE
    }, compStyleIndex);

    final OptionItem itemUseViewAspectRation = options.addBool(ITEM_USE_VIEW_ASPECT_RATIO, true);
    final OptionItem itemAspectRatio = options.addDouble(ITEM_ASPECT_RATIO, defaults.getAspectRatio());
    options.addInt(ITEM_GRID, defaults.getGridSpacing()).setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseViewAspectRation, Boolean.FALSE, itemAspectRatio);

    // Group 'Inter Edge Router'
    final OptionGroup interEdgeGroup = new OptionGroup();
    interEdgeGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_INTER_EDGE_ROUTER);
    // Populate group
    final OptionItem itemEdgeRouter = interEdgeGroup.addItem(
        options.addEnum(ITEM_EDGE_ROUTER, new String[]{
            VALUE_EDGE_ROUTER_CHANNEL_FAST,
            VALUE_EDGE_ROUTER_CHANNEL_HQ,
            VALUE_EDGE_ROUTER_ORTHOGONAL
        }, 1));
    interEdgeGroup.addItem(options.addBool(ITEM_ROUTE_ALL_EDGES,
        !((PartitionLayouter.ChannelInterEdgeRouter) defaults.getInterEdgeRouter()).isRouteInterEdgesOnly()));

    // Path finding strategies
    final OptionItem itemBendCost = interEdgeGroup.addItem(
        options.addDouble(ITEM_BEND_COST, defaultsOper.getBendCost()));
    final OptionItem itemNodeCrossingCost = interEdgeGroup.addItem(
        options.addDouble(ITEM_NODE_CROSSING_COST, defaultsOper.getNodeCrossingCost()));
    final OptionItem itemMinimumDistance =interEdgeGroup.addItem(
        options.addInt(ITEM_MINIMUM_DISTANCE, defaultsPsppf.getMinimumDistance()));
    itemMinimumDistance.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(4));
    interEdgeGroup.addItem(options.addDouble(ITEM_EDGE_CROSSING_COST, defaultsPsppf.getCrossingCost()));
    final OptionItem itemCenterSpaceRatio = interEdgeGroup.addItem(
        options.addDouble(ITEM_CENTER_TO_SPACE_RATIO, defaultsPsppf.getCenterToSpaceRatio(), 0, 1));

    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnCondition(
        optionConstraints.createConditionValueEquals(itemEdgeRouter, VALUE_EDGE_ROUTER_CHANNEL_FAST).or(
            optionConstraints.createConditionValueEquals(itemEdgeRouter, VALUE_EDGE_ROUTER_ORTHOGONAL)
        ), itemBendCost);
    optionConstraints.setEnabledOnValueEquals(itemEdgeRouter, VALUE_EDGE_ROUTER_CHANNEL_FAST, itemNodeCrossingCost);
    optionConstraints.setEnabledOnValueEquals(itemEdgeRouter, VALUE_EDGE_ROUTER_CHANNEL_HQ, itemCenterSpaceRatio);
    
    return options;
  }


  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final CompactOrthogonalLayouter orthogonal = new CompactOrthogonalLayouter();

    final OptionHandler options = getOptionHandler();
    configure(orthogonal, options);

    // launch layouter in buffered mode
    final GroupNodeHider gnh = new GroupNodeHider(orthogonal);
    gnh.setHidingEmptyGroupNodes(false);
    launchLayouter(gnh);
  }



  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2DView</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param orthogonal the <code>CompactOrthogonalLayouter</code> to be configured
   * @param options an <code>OptionHandler</code> providing the option-values referred to
   */
  protected void configure(final CompactOrthogonalLayouter orthogonal, final OptionHandler options) {
    final boolean useOrthogonalEdgeRouter = VALUE_EDGE_ROUTER_ORTHOGONAL.equals(options.get(ITEM_EDGE_ROUTER));
    final PartitionLayouter.InterEdgeRouter ier;
    if (useOrthogonalEdgeRouter) {
      ier = new PartitionLayouter.PolylineInterEdgeRouter();
      orthogonal.setInterEdgeRouter(ier);
    } else {
      ier = new PartitionLayouter.ChannelInterEdgeRouter();
      orthogonal.setInterEdgeRouter(ier);
    }
    configure(ier, options);

    final ComponentPartitionPlacer pp = new ComponentPartitionPlacer();
    orthogonal.setPartitionPlacer(pp);
    configure(pp, options);

    final OrthogonalLayouter cl = new OrthogonalLayouter();
    orthogonal.setCoreLayouter(cl);
    configure(cl, options);
    
    orthogonal.setGridSpacing(options.getInt(ITEM_GRID));

    final double ar;
    final Graph2DView view = getGraph2DView();
    if (options.getBool(ITEM_USE_VIEW_ASPECT_RATIO) && view != null) {
      final Dimension dim = view.getSize();
      ar = dim.getWidth()/dim.getHeight();
    } else {
      ar = options.getDouble(ITEM_ASPECT_RATIO);
    }
    // this needs to be done as a final step since it will reconfigure
    // layout stages which support aspect ratio accordingly
    orthogonal.setAspectRatio(ar);
  }

  private void configure(final PartitionLayouter.InterEdgeRouter router, final OptionHandler options) {
    if (router instanceof PartitionLayouter.PolylineInterEdgeRouter) {
      final PartitionLayouter.PolylineInterEdgeRouter ier = (PartitionLayouter.PolylineInterEdgeRouter) router;
      ier.setRouteInterEdgesOnly(!options.getBool(ITEM_ROUTE_ALL_EDGES));

      final EdgeRouter er = ier.getEdgeRouter();
      er.setMinimalNodeToEdgeDistance(options.getInt(ITEM_MINIMUM_DISTANCE));
      final EdgeLayoutDescriptor eld = er.getDefaultEdgeLayoutDescriptor();
      eld.setMinimalEdgeToEdgeDistance(options.getInt(ITEM_MINIMUM_DISTANCE));
      
      final PenaltySettings ps = eld.getPenaltySettings();
      ps.setEdgeCrossingPenalty((int) Math.ceil(options.getDouble(ITEM_EDGE_CROSSING_COST)));
      ps.setBendPenalty((int) Math.ceil(options.getDouble(ITEM_BEND_COST)));
    } else if (router instanceof PartitionLayouter.ChannelInterEdgeRouter) {
      final PartitionLayouter.ChannelInterEdgeRouter ier = (PartitionLayouter.ChannelInterEdgeRouter) router;
      ier.setRouteInterEdgesOnly(!options.getBool(ITEM_ROUTE_ALL_EDGES));
      
      if (VALUE_EDGE_ROUTER_CHANNEL_FAST.equals(options.getString(ITEM_EDGE_ROUTER))) {
        final OrthogonalPatternEdgeRouter oper = new OrthogonalPatternEdgeRouter();
        oper.setMinimumDistance(options.getInt(ITEM_MINIMUM_DISTANCE));
        oper.setEdgeCrossingCost(options.getDouble(ITEM_EDGE_CROSSING_COST));
        oper.setNodeCrossingCost(options.getDouble(ITEM_NODE_CROSSING_COST));
        oper.setBendCost(options.getDouble(ITEM_BEND_COST));
        ier.getChannelEdgeRouter().setPathFinderStrategy(oper);
      } else {
        final ChannelEdgeRouter.OrthogonalShortestPathPathFinder osppf = new ChannelEdgeRouter.OrthogonalShortestPathPathFinder();
        osppf.setMinimumDistance(options.getInt(ITEM_MINIMUM_DISTANCE));
        osppf.setCrossingCost(options.getDouble(ITEM_EDGE_CROSSING_COST));
        osppf.setCenterToSpaceRatio(options.getDouble(ITEM_CENTER_TO_SPACE_RATIO));
        ier.getChannelEdgeRouter().setPathFinderStrategy(osppf);
      }
    }
  }

  private void configure(final PartitionLayouter.ComponentPartitionPlacer placer, final OptionHandler options) {
    if (VALUE_STYLE_PACKED_COMPACT_RECTANGLE.equals(options.get(ITEM_PLACEMENT_STRATEGY))) {
      placer.getComponentLayouter().setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE);
    } else if (VALUE_STYLE_ROWS.equals(options.get(ITEM_PLACEMENT_STRATEGY))) {
      placer.getComponentLayouter().setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    }
  }

  private void configure(final OrthogonalLayouter orthogonal, final OptionHandler options) {
    final String ols = options.getString(ITEM_ORTHOGONAL_LAYOUT_STYLE);
    if (VALUE_NORMAL_TREE.equals(ols)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.NORMAL_TREE_STYLE);
    } else if (VALUE_FIXED_MIXED.equals(ols)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.FIXED_MIXED_STYLE);
    } else if (VALUE_FIXED_BOX_NODES.equals(ols)) {
      orthogonal.setLayoutStyle(OrthogonalLayouter.FIXED_BOX_STYLE);
    } else {
      // else if VALUE_NORMAL.equals(ols)
      orthogonal.setLayoutStyle(OrthogonalLayouter.NORMAL_STYLE);
    }
  }

}
