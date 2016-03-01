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
package demo.view.graphexplorer;

import y.anim.AnimationObject;
import y.anim.CompositeAnimationObject;
import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.BufferedLayouter;
import y.layout.CompositeLayoutStage;
import y.layout.CompositeLayouter;
import y.layout.FixNodeLayoutStage;
import y.layout.GraphLayout;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.ParallelEdgeLayouter;
import y.layout.SelfLoopLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.organic.GroupedShuffleLayouter;
import y.layout.organic.ShuffleLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.partial.PartialLayouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.router.polyline.PenaltySettings;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.TreeReductionStage;
import y.util.Comparators;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.util.GraphHider;
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.LayoutMorpher;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.hierarchy.HierarchyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides several layout algorithms for partial/incremental and complete
 * re-layout.
 *
 */
class LayoutSupport {

  private static final int DURATION_EXPLORE = 1000;

  /**
   * Specialized partial orthogonal Layout for grouped graphs.
   * @param context specifies new and old elements.
   */
  static void doGroupedOrthogonalLayout(final LayoutContext context) {
    final OrthogonalGroupLayouter ol = new OrthogonalGroupLayouter();
    ol.setGrid(10);
    if (context.isFromSketch()) {
      final PartialLayouter pl = new PartialLayouter(ol);
      pl.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL);
      pl.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
      pl.setConsiderNodeAlignment(false);
      pl.setMinimalNodeDistance(20);
      pl.setFixedGroupResizingEnabled(false);

      if (pl.getEdgeRouter() instanceof EdgeRouter) {
        final EdgeRouter edgeRouter = (EdgeRouter) pl.getEdgeRouter();
        final PenaltySettings penaltySettings = edgeRouter.getDefaultEdgeLayoutDescriptor().getPenaltySettings();
        penaltySettings.setEdgeCrossingPenalty(0);
      }

      final LayoutGraph filteredGraph = context.getGraph2D();
      filteredGraph.addDataProvider(
              PartialLayouter.PARTIAL_NODES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isNewNode((Node) dataHolder);
        }
              });

      filteredGraph.addDataProvider(
              PartialLayouter.PARTIAL_EDGES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isNewEdge((Edge) dataHolder);
                }
              });


      final GroupedShuffleLayouter gsl = new GroupedShuffleLayouter();
      gsl.setCoreLayouter(pl);
      ((ShuffleLayouter)gsl.getShuffleLayouter()).setMinimalNodeDistance(20);

      Layouter layouter = new Layouter() {
        public boolean canLayout(LayoutGraph graph) {
          return true;
        }
        public void doLayout(LayoutGraph graph) {
          //use BufferedLayout to keep graph element order for subsequent edge routing phase.
          BufferedLayouter bl = new BufferedLayouter(gsl);
          bl.doLayout(graph);

          graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, DataProviders.createConstantDataProvider(Boolean.TRUE));
          graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, DataProviders.createConstantDataProvider(Boolean.FALSE));
          pl.doLayout(graph);
        }
      };

      doLayout(layouter, context);

      //cleanup
      filteredGraph.removeDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
      filteredGraph.removeDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
    } else {
      doLayout(ol, context);
    }
  }

  /**
   * Arranges the graph in an orthogonal fashion.
   * <p>
   * This layout calculation is animated. Moreover, the layout animation
   * is combined with fade in animations for new elements and fade out
   * animations for old elements (which are automatically removed at animation
   * end).
   * </p>
   * @param context specifies new and old elements.
   */
  static void doOrthogonalLayout( final LayoutContext context ) {

    if(containsGroups(context.getGraph2D())) {
      doGroupedOrthogonalLayout(context);
      return;
    }
    
    final OrthogonalLayouter ol = new OrthogonalLayouter();
    ol.setGrid(10);

    if (context.isFromSketch()) {
      ol.setLayoutStyle(OrthogonalLayouter.NORMAL_TREE_STYLE);

      final PartialLayouter pl = new PartialLayouter(ol);
      pl.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL);
      pl.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
      pl.setConsiderNodeAlignment(false);
      pl.setMinimalNodeDistance(20);
      if (pl.getEdgeRouter() instanceof EdgeRouter) {
        final EdgeRouter edgeRouter = (EdgeRouter) pl.getEdgeRouter();
        final PenaltySettings penaltySettings = edgeRouter.getDefaultEdgeLayoutDescriptor().getPenaltySettings();
        penaltySettings.setEdgeCrossingPenalty(5);
      }

      final LayoutGraph filteredGraph = context.getGraph2D();
      filteredGraph.addDataProvider(
              PartialLayouter.PARTIAL_NODES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isNewNode((Node) dataHolder);
                }
              });

      filteredGraph.addDataProvider(
              PartialLayouter.PARTIAL_EDGES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isNewEdge((Edge) dataHolder);
                }
              });

      doLayout(pl, context);

      filteredGraph.removeDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
      filteredGraph.removeDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY);
    } else {
      doLayout(ol, context);
    }
  }    

  /**
   * Arranges the graph in a hierarchical fashion.
   * <p>
   * This layout calculation is animated. Moreover, the layout animation
   * is combined with fade in animations for new elements and fade out
   * animations for old elements (which are automatically removed at animation
   * end).
   * </p>
   * @param context specifies new and old elements.
   */
  static void doHierarchicLayout( final LayoutContext context ) {
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);    
    ihl.setOrthogonallyRouted(true);
   
    final LayoutGraph filteredGraph = context.getGraph2D();
    if (context.isFromSketch()) {
      final DataMap obj2Hint = Maps.createHashedDataMap();
      filteredGraph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, obj2Hint);
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
      final IncrementalHintsFactory ihf = ihl.createIncrementalHintsFactory();
      for (Iterator it = context.newNodes(); it.hasNext();) {
        final Object viewNode = it.next();
        obj2Hint.set(viewNode, ihf.createLayerIncrementallyHint(viewNode));
      }
      for (Iterator it = context.newEdges(); it.hasNext();) {
        final Object viewEdge = it.next();
        obj2Hint.set(viewEdge, ihf.createSequenceIncrementallyHint(viewEdge));
      }
    }

    doLayout(ihl, context);

    if (context.isFromSketch()) {
      filteredGraph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
    }
  }

  /**
   * Arranges the graph in a balloon-like fashion.
   * <p>
   * <b>Note:</b> The graph that is laid out has to be a <em>tree</em>.
   * </p><p>
   * This layout calculation is animated. Moreover, the layout animation
   * is combined with fade in animations for new elements and fade out
   * animations for old elements (which are automatically removed at animation
   * end).
   * </p>
   * @param context specifies new and old elements.
   */
  static void doBalloonLayout( final LayoutContext context ) {

    final CompositeLayoutStage cls = new CompositeLayoutStage();
 
    final BalloonLayouter bl = new BalloonLayouter();
    bl.setFromSketchModeEnabled(context.isFromSketch());
    bl.setAllowOverlaps(false);

    final SelfLoopLayouter sll = new SelfLoopLayouter();
    sll.setSmartSelfloopPlacementEnabled(true);
    sll.setLayoutStyle(SelfLoopLayouter.STYLE_ROUNDED);
    cls.appendStage(sll);
 
    final ParallelEdgeLayouter pl = new ParallelEdgeLayouter();    
    pl.setUsingAdaptiveLineDistances(true);
    cls.appendStage(pl);
    
    final TreeReductionStage trs = new TreeReductionStage();
    cls.appendStage(trs);
    
    cls.setCoreLayouter(bl);

    if(context.isGroupingMode()) {
      doLayout(new RecursiveGroupLayouter(cls), context);
    }
    else {
      doLayout(cls, context);
    }
  }

  /**
   * Arranges the graph in a circular fashion.
   * <p>
   * This layout calculation is animated. Moreover, the layout animation
   * is combined with fade in animations for new elements and fade out
   * animations for old elements (which are automatically removed at animation
   * end).
   * </p>
   * @param context specifies new and old elements.
   */
  static void doCircularLayout( final LayoutContext context ) {

    final CircularLayouter cl = new CircularLayouter();
    cl.setFromSketchModeEnabled(context.isFromSketch());

    cl.setPlaceChildrenOnCommonRadiusEnabled(false);
    cl.getBalloonLayouter().setMinimalNodeDistance(20);
    
    final SelfLoopLayouter sll = (SelfLoopLayouter) cl.getSelfLoopLayouter();
    sll.setSmartSelfloopPlacementEnabled(true);
    sll.setLayoutStyle(SelfLoopLayouter.STYLE_ROUNDED);

    if(context.isGroupingMode()) {
      doLayout(new RecursiveGroupLayouter(cl), context);
    }
    else {
      doLayout(cl, context);
    }
  }

  /**
   * Arranges the graph in organic layout style.
   * <p>
   * This layout calculation is animated. Moreover, the layout animation
   * is combined with fade in animations for new elements and fade out
   * animations for old elements (which are automatically removed at animation
   * end).
   * </p>
   * @param context specifies new and old elements.
   */
  static void doOrganicLayout( final LayoutContext context ) {
    final SmartOrganicLayouter sol = new SmartOrganicLayouter();
    sol.setDeterministic(true);
    sol.setMinimalNodeDistance(30);
    sol.setNodeOverlapsAllowed(false);
    sol.setPreferredEdgeLength(50);
    sol.setSmartComponentLayoutEnabled(true);
    sol.setMultiThreadingAllowed(true);

    if (context.isFromSketch()) {
      final PartialLayouter pl = new PartialLayouter(sol);
      pl.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE);
      pl.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
      pl.setConsiderNodeAlignment(false);
      pl.setMinimalNodeDistance(20);

      final LayoutGraph filteredGraph = context.getGraph2D();
      filteredGraph.addDataProvider(
              PartialLayouter.PARTIAL_NODES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isNewNode((Node) dataHolder);
                }
              });

      final SelfLoopLayouter ssl = new SelfLoopLayouter();
      ssl.setLayoutStyle(SelfLoopLayouter.STYLE_ROUNDED);
      ssl.setSmartSelfloopPlacementEnabled(true);

      if(context.isGroupingMode()) {
        final GroupedShuffleLayouter gsl = new GroupedShuffleLayouter();
        gsl.setCoreLayouter(pl);
        ((ShuffleLayouter)gsl.getShuffleLayouter()).setMinimalNodeDistance(20);
        doLayout(new CompositeLayouter(ssl, gsl), context);
      }
      else {
        doLayout(new CompositeLayouter(ssl, pl), context);
      }
      filteredGraph.removeDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY);
    } else {
      doLayout(sol, context);
    }
  }

  /**
   * Arranges the graph using the specified layout algorithm.
   * @param layouter the layout algorithm to arrange the graph.
   * @param context specifies the new and old elements.
   */
  private static void doLayout( final Layouter layouter, final LayoutContext context ) {
    if (context.isAnimated()) {
      doLayoutAnimated(layouter, context);
    } else {
      final Graph2D filteredGraph = context.getGraph2D();

      FilteringLayouter.markElements(filteredGraph, context);

      (new Graph2DLayoutExecutor()).doLayout(filteredGraph, new FilteringLayouter(layouter));

      FilteringLayouter.unmarkElements(filteredGraph);
    }
  }

  /**
   * Arranges the graph using the specified layout algorithm.
   * The layout calculation is animated. The layout animation is combined with
   * fade in animations for new elements and fade out animations for old
   * elements (which are automatically removed at animation end).
   * @param layouter the layout algorithm to arrange the graph.
   * @param context specifies the new and old elements.
   */
  private static void doLayoutAnimated( final Layouter layouter, final LayoutContext context ) {
    final Graph2D filteredGraph = context.getGraph2D();

    FilteringLayouter.markElements(filteredGraph, context);

    final Graph2DLayoutExecutor graph2DLayoutExecutor =
            new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED) {
              protected AnimationObject createAnimation(
                      final Graph2DView view,
                      final Graph2D graph,
                      final GraphLayout graphLayout
              ) {
                // guarantees that fade out animations which are added to the
                // composite are disposed in the order of addition
                // therefore, all edges which are faded out are removed
                // before any of the nodes which are faded out are removed
                // (otherwise edges connected to nodes which have been faded out
                // would already be removed when their animation is disposed
                // resulting in IllegalArgumentExceptions)
                final CompositeLayoutAnimation composite =
                        new CompositeLayoutAnimation(
                                graph,
                                super.createAnimation(view, graph, graphLayout));
                final long pd = composite.preferredDuration();

                final ViewAnimationFactory factory = new ViewAnimationFactory(view);
                factory.setQuality(ViewAnimationFactory.HIGH_PERFORMANCE);
                for (Iterator it = context.newEdges(); it.hasNext();) {
                  final Edge edge = (Edge) it.next();
                  if (!context.isOldEdge(edge)) {
                    final EdgeRealizer er = graph.getRealizer(edge);
                    composite.addAnimation(factory.fadeIn(er, pd));
                  }
                }
                for (Iterator it = context.removedEdges(); it.hasNext();) {
                  final EdgeRealizer er = graph.getRealizer((Edge) it.next());
                  composite.addAnimation(factory.fadeOut(
                          er, ViewAnimationFactory.APPLY_EFFECT, pd));
                }
                for (Iterator it = context.newNodes(); it.hasNext();) {
                  final Node node = (Node) it.next();
                  if (!context.isOldNode(node)) {
                    final NodeRealizer nr = graph.getRealizer(node);
                    composite.addNodeAnimation(node, factory.fadeIn(nr, pd));
                  }
                }
                for (Iterator it = context.removedNodes(); it.hasNext();) {
                  final Node node = (Node) it.next();
                  final NodeRealizer nr = graph.getRealizer(node);
                  composite.addNodeAnimation(node, factory.fadeOut(
                          nr, ViewAnimationFactory.APPLY_EFFECT, pd));
                }

                return composite;
              }
            };
    final LayoutMorpher morpher = graph2DLayoutExecutor.getLayoutMorpher();
    if (context.isFromSketch()) {
      morpher.setKeepZoomFactor(true);
      morpher.setSmoothViewTransform(false);
      morpher.setFocusNode(null);
    } else {
      morpher.setSmoothViewTransform(true);
    }
    morpher.setPreferredDuration(DURATION_EXPLORE);
    graph2DLayoutExecutor.doLayout(context.getView(), new FilteringLayouter(layouter));

    FilteringLayouter.unmarkElements(filteredGraph);
  }


  /**
   * Decorator for layout algorithms that hides specifically marked elements
   * from its core layout algorithm.
   * @see #IGNORED_EDGES_DPKEY
   * @see #IGNORED_NODES_DPKEY
   */
  static final class FilteringLayouter implements Layouter {
    /**
     * Key to register a data provider that marks edges to hide from the
     * core layout algorithm. For each edge in the graph, the data provider's
     * {@link DataProvider#getBool(Object)} method determines whether or not
     * the edge has to be hidden.
     * A data provider registered with this key <b>is required</b>.
     */
    static final String IGNORED_EDGES_DPKEY =
            "FilteringLayouter.IGNORED_EDGES_DPKEY";
    /**
     * Key to register a data provider that marks nodes to hide from the
     * core layout algorithm. For each node in the graph, the data provider's
     * {@link DataProvider#getBool(Object)} method determines whether or not
     * the node has to be hidden.
     * A data provider registered with this key <b>is required</b>.
     */
    static final String IGNORED_NODES_DPKEY =
            "FilteringLayouter.IGNORED_NODES_DPKEY";

    private final Layouter core;

    FilteringLayouter( final Layouter core ) {
      this.core = new FixNodeLayoutStage(core);
    }

    public boolean canLayout( final LayoutGraph graph ) {
      return core.canLayout(graph);
    }

    /**
     * Calculates a new layout for the specified graph after hiding marked. 
     * elements.
     * @param graph the graph to be laid out.
     * @see #IGNORED_EDGES_DPKEY
     * @see #IGNORED_NODES_DPKEY
     */
    public void doLayout( final LayoutGraph graph ) {
      final GraphHider hider = new GraphHider(graph);

      // hide marked edges
      final DataProvider edp = graph.getDataProvider(IGNORED_EDGES_DPKEY);
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (edp.getBool(edge)) {
          hider.hide(edge);
        }
      }
      // hide marked nodes
      final DataProvider ndp = graph.getDataProvider(IGNORED_NODES_DPKEY);
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        if (ndp.getBool(node)) {
          hider.hide(node);
        }
      }

      try {
        core.doLayout(graph);
      } finally {
        // restore original graph structure
        hider.unhideAll();
      }
    }

    /**
     * Marks the elements to be removed as ignored for layout calculation and
     * the fix node as fixed.
     * @param graph the graph whose elements are marked.
     * @param context specifies the elements to be removed as well as the
     * fix node.
     * @see #unmarkElements(y.layout.LayoutGraph)
     * @see #IGNORED_EDGES_DPKEY
     * @see #IGNORED_NODES_DPKEY
     * @see FixNodeLayoutStage#FIXED_NODE_DPKEY
     * @see LayoutContext#isRemovedEdge(y.base.Edge)
     * @see LayoutContext#isRemovedNode(y.base.Node)
     */
    static void markElements(
            final LayoutGraph graph, final LayoutContext context
    ) {
      graph.addDataProvider(
              FixNodeLayoutStage.FIXED_NODE_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return dataHolder == context.getFixedNode();
                }
              });

      graph.addDataProvider(
              IGNORED_NODES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isRemovedNode((Node) dataHolder);
                }
              });
      graph.addDataProvider(
              IGNORED_EDGES_DPKEY, new DataProviderAdapter() {
                public boolean getBool( final Object dataHolder ) {
                  return context.isRemovedEdge((Edge) dataHolder);
                }
              });
    }

    /**
     * Removes the markers for ignored and fixed elements.
     * @param graph the graph whose elements were marked.
     * @see #markElements(y.layout.LayoutGraph, LayoutContext)
     * @see #IGNORED_EDGES_DPKEY
     * @see #IGNORED_NODES_DPKEY
     * @see FixNodeLayoutStage#FIXED_NODE_DPKEY
     */
    static void unmarkElements( final LayoutGraph graph ) {
      graph.removeDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY);
      graph.removeDataProvider(IGNORED_EDGES_DPKEY);
      graph.removeDataProvider(IGNORED_NODES_DPKEY);
    }
  }


  /**
   * Composite animation that guarantees that animations are disposed in the
   * order they are/were added.
   */
  static final class CompositeLayoutAnimation implements CompositeAnimationObject {
    private final Graph2D graph;
    private final AnimationObject morpher;
    private final List animations;
    private final Map elements;

    CompositeLayoutAnimation( final Graph2D graph, final AnimationObject morpher ) {
      this.graph = graph;
      this.morpher = morpher;
      animations = new ArrayList();
      elements = new HashMap();
    }

    void addNodeAnimation( final Node node, final AnimationObject ao ) {
      elements.put(ao, node);
      addAnimation(ao);
    }

    /*
     * ###################################################################
     * CompositeAnimationObject
     * ###################################################################
     */

    public void addAnimation( final AnimationObject ao ) {
      animations.add(ao);
    }

    public void removeAnimation( final AnimationObject ao ) {
      animations.remove(ao);
    }

    public boolean isEmpty() {
      return false;
    }

    /*
     * ###################################################################
     * AnimationObject
     * ###################################################################
     */

    public void disposeAnimation() {
      morpher.disposeAnimation();

      for (Iterator it = animations.iterator(); it.hasNext();) {
        ((AnimationObject) it.next()).disposeAnimation();
      }
    }

    public void calcFrame( final double time ) {
      for (Iterator it = animations.iterator(); it.hasNext();) {
        ((AnimationObject) it.next()).calcFrame(time);
      }

      morpher.calcFrame(time);
    }

    public void initAnimation() {
      final HierarchyManager hierarchy = graph.getHierarchyManager();
      if (hierarchy == null) {
        for (Iterator it = animations.iterator(); it.hasNext();) {
          ((AnimationObject) it.next()).initAnimation();
        }
      } else {
        // the order of initialization determines the paint order of the
        // animation objects
        // for nodes, it is important that animation effects of group nodes
        // are painted *before* the animation effects of the child nodes
        final HashMap depth = new HashMap();
        final Integer edgeDepth = new Integer(Integer.MAX_VALUE);
        final AnimationObject[] order = new AnimationObject[animations.size()];
        int i = 0;
        for (Iterator it = animations.iterator(); it.hasNext(); ++i) {
          final AnimationObject ao = (AnimationObject) it.next();
          order[i] = ao;
          final Object e = elements.get(ao);
          if (e == null) {
            depth.put(ao, edgeDepth);
          } else {
            depth.put(ao, new Integer(hierarchy.getLocalGroupDepth((Node) e)));
          }
        }
        Arrays.sort(order, new Comparator() {
          public int compare( final Object o1, final Object o2 ) {
            final int d1 = ((Integer) depth.get(o1)).intValue();
            final int d2 = ((Integer) depth.get(o2)).intValue();
            return Comparators.compare(d1, d2);
          }
        });
        for (int j = 0; j < order.length; ++j) {
          order[j].initAnimation();
        }
      }

      morpher.initAnimation();
    }

    public long preferredDuration() {
      return morpher.preferredDuration();
    }
  }

  static boolean containsGroups(Graph2D graph) {
    HierarchyManager hm = graph.getHierarchyManager();
    return hm != null && hm.containsGroups();
  }
}
