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

import y.layout.router.EdgeGroupRouterStage;
import y.layout.router.GroupNodeRouterStage;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.router.PatchRouterStage;
import y.layout.router.ReducedSphereOfActionStage;
import y.option.ConstraintManager;
import y.option.IntOptionItem;
import y.option.OptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;


/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.router.OrthogonalEdgeRouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_edge_router.html#orthogonal_edge_router">Section Orthogonal Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class OrthogonalEdgeRouterModule extends LayoutModule {
  //// Module 'Orthogonal Edge Router'
  private static final String ORTHOGONAL_EDGE_ROUTER = "ORTHOGONAL_EDGE_ROUTER";
  
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT_OPTIONS = "LAYOUT_OPTIONS";
  // Section 'Layout' items
  protected static final String ITEM_SCOPE = "SCOPE";
  protected static final String VALUE_SCOPE_ALL_EDGES = "ALL_EDGES";
  protected static final String VALUE_SCOPE_SELECTED_EDGES = "SELECTED_EDGES";
  protected static final String VALUE_SCOPE_AT_SELECTED_NODES = "AT_SELECTED_NODES";
  protected static final String ITEM_MONOTONIC_RESTRICTION = "MONOTONIC_RESTRICTION";
  protected static final String VALUE_MONOTONIC_NONE = "MONOTONIC_NONE";
  protected static final String VALUE_MONOTONIC_HORIZONTAL = "MONOTONIC_HORIZONTAL";
  protected static final String VALUE_MONOTONIC_VERTICAL = "MONOTONIC_VERTICAL";
  protected static final String VALUE_MONOTONIC_BOTH = "MONOTONIC_BOTH";
  protected static final String ITEM_ENFORCE_MONOTONIC_RESTRICTIONS = "ENFORCE_MONOTONIC_RESTRICTIONS";
  protected static final String ITEM_MINIMUM_DISTANCE_TO_EDGE = "MINIMUM_DISTANCE_TO_EDGE";
  protected static final String ITEM_USE_CUSTOM_MINIMUM_DISTANCE_TO_NODE = "USE_CUSTOM_MINIMUM_DISTANCE_TO_NODE";
  protected static final String ITEM_CUSTOM_MINIMUM_DISTANCE_TO_NODE = "CUSTOM_MINIMUM_DISTANCE_TO_NODE";
  protected static final String ITEM_ROUTE_ON_GRID = "ROUTE_ON_GRID";
  protected static final String ITEM_GRID_SPACING = "GRID_SPACING";
  protected static final String ITEM_SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH = "SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  
  //// Section 'Crossing Minimization'
  protected static final String SECTION_SECTION_CROSSING_MINIMIZATION = "CROSSING_MINIMIZATION";
  // Section 'Crossing Minimization' items
  protected static final String ITEM_LOCAL_CROSSING_MINIMIZATION = "LOCAL_CROSSING_MINIMIZATION";
  protected static final String ITEM_CROSSING_COST = "CROSSING_COST";
  protected static final String ITEM_REROUTING_ENABLED = "REROUTING_ENABLED";

  /**
   * Creates an instance of this module.
   */
  public OrthogonalEdgeRouterModule() {
    super(ORTHOGONAL_EDGE_ROUTER);
    setPortIntersectionCalculatorEnabled(true);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    options.clear();
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Default provider
    final OrthogonalEdgeRouter orthogonal = new OrthogonalEdgeRouter();

    // Group 'Layout'
    final OptionGroup layoutGroup = new OptionGroup();
    layoutGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, SECTION_LAYOUT_OPTIONS);
    // Populate group
    final byte sphereOfAction;
    switch (orthogonal.getSphereOfAction()) {
      case OrthogonalEdgeRouter.ROUTE_ALL_EDGES:
        sphereOfAction = 0;
        break;
      case OrthogonalEdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES:
        sphereOfAction = 1;
        break;
      default:
        sphereOfAction = 2;
        break;
    }
    layoutGroup.addItem(
        options.addEnum(ITEM_SCOPE, new String[]{
            VALUE_SCOPE_ALL_EDGES,
            VALUE_SCOPE_SELECTED_EDGES,
            VALUE_SCOPE_AT_SELECTED_NODES
        }, sphereOfAction));
    layoutGroup.addItem(
        options.addEnum(ITEM_MONOTONIC_RESTRICTION, new String[]{
            VALUE_MONOTONIC_NONE,
            VALUE_MONOTONIC_HORIZONTAL,
            VALUE_MONOTONIC_VERTICAL,
            VALUE_MONOTONIC_BOTH
        }, 0));
    layoutGroup.addItem(
        options.addBool(ITEM_ENFORCE_MONOTONIC_RESTRICTIONS, orthogonal.isEnforceMonotonicPathRestrictions()));
    // The value given for 'minimum distance' denotes a halo to the left and
    // right of an edge segment.
    final OptionItem itemMinimumDistanceEdge = layoutGroup.addItem(
            options.addInt(ITEM_MINIMUM_DISTANCE_TO_EDGE, orthogonal.getMinimumDistance()));
    itemMinimumDistanceEdge.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(4));
    final OptionItem itemUseCustomMinimumDistanceNode = layoutGroup.addItem(
        options.addBool(ITEM_USE_CUSTOM_MINIMUM_DISTANCE_TO_NODE, !orthogonal.getCoupledDistances()));
    final OptionItem itemCustomMinimumDistanceNode = layoutGroup.addItem(
        options.addInt(ITEM_CUSTOM_MINIMUM_DISTANCE_TO_NODE, orthogonal.getMinimumDistanceToNode()));
    itemCustomMinimumDistanceNode.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(2));
    final OptionItem itemRouteOnGrid = layoutGroup.addItem(
        options.addBool(ITEM_ROUTE_ON_GRID, orthogonal.isGridRoutingEnabled()));
    final OptionItem itemGridSpacing = layoutGroup.addItem(
        options.addInt(ITEM_GRID_SPACING, orthogonal.getGridSpacing()));
    itemGridSpacing.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(2));
    layoutGroup.addItem(
        options.addDouble(ITEM_SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH, orthogonal.getCenterToSpaceRatio(), 0.0, 1.0));
    layoutGroup.addItem(
        options.addBool(ITEM_CONSIDER_NODE_LABELS, orthogonal.isConsiderNodeLabelsEnabled()));

    // Group 'Crossing Minimization'
    final OptionGroup sectionCrossingGroup = new OptionGroup();
    sectionCrossingGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, SECTION_SECTION_CROSSING_MINIMIZATION);
    // Populate group
    sectionCrossingGroup.addItem(options.addBool(ITEM_LOCAL_CROSSING_MINIMIZATION,
        orthogonal.isLocalCrossingMinimizationEnabled()));
    sectionCrossingGroup.addItem(options.addDouble(ITEM_CROSSING_COST, orthogonal.getCrossingCost()));
    sectionCrossingGroup.addItem(options.addBool(ITEM_REROUTING_ENABLED, orthogonal.isReroutingEnabled()));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemRouteOnGrid, Boolean.TRUE, itemGridSpacing);
    optionConstraints.setEnabledOnValueEquals(itemUseCustomMinimumDistanceNode,
        Boolean.TRUE,
        itemCustomMinimumDistanceNode);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final OrthogonalEdgeRouter orthogonal = new OrthogonalEdgeRouter();

    final OptionHandler options = getOptionHandler();
    configure(orthogonal, options);

    launchLayouter(
        new EdgeGroupRouterStage(
            new GroupNodeRouterStage(
                new ReducedSphereOfActionStage(
                    new PatchRouterStage(orthogonal)))));
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param orthogonal the <code>OrthogonalEdgeRouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final OrthogonalEdgeRouter orthogonal, final OptionHandler options) {    
    String choice = options.getString(ITEM_SCOPE);
    if (choice.equals(VALUE_SCOPE_AT_SELECTED_NODES)) {
      orthogonal.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
    } else if (choice.equals(VALUE_SCOPE_SELECTED_EDGES)) {
      orthogonal.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
    } else {
      orthogonal.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_ALL_EDGES);
    }
    
    orthogonal.setMinimumDistance(options.getInt(ITEM_MINIMUM_DISTANCE_TO_EDGE));
    orthogonal.setCoupledDistances(!options.getBool(ITEM_USE_CUSTOM_MINIMUM_DISTANCE_TO_NODE));
    orthogonal.setMinimumDistanceToNode(options.getInt(ITEM_CUSTOM_MINIMUM_DISTANCE_TO_NODE));
    orthogonal.setGridRoutingEnabled(options.getBool(ITEM_ROUTE_ON_GRID));
    orthogonal.setGridSpacing(options.getInt(ITEM_GRID_SPACING));
    orthogonal.setCenterToSpaceRatio(options.getDouble(ITEM_SPACE_DRIVEN_VS_CENTER_DRIVEN_SEARCH));
    orthogonal.setLocalCrossingMinimizationEnabled(options.getBool(ITEM_LOCAL_CROSSING_MINIMIZATION));
    
    if (VALUE_MONOTONIC_BOTH.equals(options.getString(ITEM_MONOTONIC_RESTRICTION))) {
      orthogonal.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_BOTH);
    } else if (VALUE_MONOTONIC_HORIZONTAL.equals(options.getString(ITEM_MONOTONIC_RESTRICTION))) {
      orthogonal.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_HORIZONTAL);
    } else if (VALUE_MONOTONIC_VERTICAL.equals(options.getString(ITEM_MONOTONIC_RESTRICTION))) {
      orthogonal.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_VERTICAL);
    } else {
      orthogonal.setMonotonicPathRestriction(OrthogonalEdgeRouter.MONOTONIC_NONE);
    }
    
    orthogonal.setEnforceMonotonicPathRestrictions(options.getBool(ITEM_ENFORCE_MONOTONIC_RESTRICTIONS));
    orthogonal.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS));
    orthogonal.setCrossingCost(options.getDouble(ITEM_CROSSING_COST));
    orthogonal.setReroutingEnabled(options.getBool(ITEM_REROUTING_ENABLED));
  }
}
