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
import y.layout.BendConverter;
import y.layout.CompositeLayoutStage;
import y.layout.LayoutStage;
import y.layout.SequentialLayouter;
import y.layout.grouping.GroupNodeHider;
import y.layout.organic.RemoveOverlapsLayoutStage;
import y.layout.router.OrganicEdgeRouter;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.view.Graph2D;
import y.view.Selections;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.router.OrganicEdgeRouter}.
 */
public class OrganicEdgeRouterModule extends LayoutModule {
  //// Module 'Organic Edge Router'
  protected static final String MODULE_ORGANIC_EDGE_ROUTER = "ORGANIC_EDGE_ROUTER";
  
  //// Section 'default' items
  protected static final String TITLE_LAYOUT_OPTIONS = "LAYOUT_OPTIONS";
  protected static final String ITEM_SELECTION_ONLY = "SELECTION_ONLY";
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_USE_BENDS = "USE_BENDS";
  protected static final String ITEM_ROUTE_ONLY_NECESSARY = "ROUTE_ONLY_NECESSARY";
  protected static final String ITEM_ALLOW_MOVING_NODES = "ALLOW_MOVING_NODES";

  /**
   * Creates an instance of this module.
   */
  public OrganicEdgeRouterModule() {
    super(MODULE_ORGANIC_EDGE_ROUTER);
  }
  
  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    // Defaults provider
    final OrganicEdgeRouter defaults = new OrganicEdgeRouter();
    
    // Group 'Layout'
    final OptionGroup layoutGroup = new OptionGroup();
    layoutGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_LAYOUT_OPTIONS);
    // Populate group
    layoutGroup.addItem(options.addBool(ITEM_SELECTION_ONLY, false));
    layoutGroup.addItem(options.addInt(ITEM_MINIMAL_NODE_DISTANCE, (int) defaults.getMinimalDistance(), 10, 300));
    layoutGroup.addItem(options.addBool(ITEM_USE_BENDS, defaults.isUsingBends()));
    layoutGroup.addItem(options.addBool(ITEM_ROUTE_ONLY_NECESSARY, !defaults.isRoutingAll()));
    layoutGroup.addItem(options.addBool(ITEM_ALLOW_MOVING_NODES, false));
    
    return options;
  }
  
  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final OrganicEdgeRouter organic = new OrganicEdgeRouter();
    
    final OptionHandler options = getOptionHandler();
    configure(organic, options);
    
    final SequentialLayouter sequential = new SequentialLayouter();
    if (options.getBool(ITEM_ALLOW_MOVING_NODES)) {
      //if we are allowed to move nodes, we can improve the routing results by temporarily enlarging nodes and removing overlaps
      //(this strategy ensures that there is enough space for the edges)
      final CompositeLayoutStage cls = new CompositeLayoutStage();
      cls.appendStage(organic.createNodeEnlargementStage());
      cls.appendStage(new RemoveOverlapsLayoutStage(0));
      sequential.appendLayouter(cls);

    }
    if (organic.isUsingBends()) {
      //we want to keep the original bends
      final BendConverter bendConverter = new BendConverter();
      bendConverter.setSelectedEdgesDpKey(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
      bendConverter.setAdoptSelectionEnabled(options.getBool(ITEM_SELECTION_ONLY));
      bendConverter.setCoreLayouter(organic);
      sequential.appendLayouter(bendConverter);
    } else {
      sequential.appendLayouter(organic);
    }

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(new GroupNodeHider(sequential));
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
    // register grouping relevant DataProviders
    if (options.getBool(ITEM_SELECTION_ONLY)) {
      // backup existing data providers to prevent loss of user settings
      final DataProvider selectedEdgesDP = Selections.createSelectionEdgeMap(graph);
      backupDataProvider(graph, BendConverter.SCOPE_DPKEY);
      graph.addDataProvider(BendConverter.SCOPE_DPKEY, selectedEdgesDP);
      backupDataProvider(graph, OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
      graph.addDataProvider(OrganicEdgeRouter.ROUTE_EDGE_DPKEY, selectedEdgesDP);
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // unregister grouping relevant DataProviders
    if (options.getBool(ITEM_SELECTION_ONLY)) {
      // remove the data providers set by this module by restoring the initial state
      restoreDataProvider(graph, OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
      restoreDataProvider(graph, BendConverter.SCOPE_DPKEY);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param organic the <code>OrganicEdgeRouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final OrganicEdgeRouter organic, final OptionHandler options) {
    organic.setMinimalDistance(options.getInt(ITEM_MINIMAL_NODE_DISTANCE));
    organic.setUsingBends(options.getBool(ITEM_USE_BENDS));
    organic.setRoutingAll(!options.getBool(ITEM_ROUTE_ONLY_NECESSARY));
  }
}
