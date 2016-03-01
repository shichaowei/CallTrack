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
import y.layout.Layouter;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.partial.PartialLayouter;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Selections;
import y.view.tabular.TableLayoutConfigurator;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.partial.PartialLayouter}.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/partial_layout.html#partial_layout">Section Partial Layout </a> in the yFiles for Java Developer's Guide
 */
public class PartialLayoutModule extends LayoutModule {
  //// Module 'Partial Layout'
  protected static final String MODULE_PARTIAL = "PARTIAL";
  
  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_ROUTING_TO_SUBGRAPH = "ROUTING_TO_SUBGRAPH";
  protected static final String VALUE_ROUTING_TO_SUBGRAPH_AUTO = "ROUTING_TO_SUBGRAPH_AUTO";
  protected static final String VALUE_ROUTING_TO_SUBGRAPH_STRAIGHT_LINE = "ROUTING_TO_SUBGRAPH_STRAIGHT_LINE";
  protected static final String VALUE_ROUTING_TO_SUBGRAPH_POLYLINE = "ROUTING_TO_SUBGRAPH_POLYLINE";
  protected static final String VALUE_ROUTING_TO_SUBGRAPH_ORTHOGONALLY = "ROUTING_TO_SUBGRAPH_ORTHOGONALLY";
  protected static final String VALUE_ROUTING_TO_SUBGRAPH_ORGANIC = "ROUTING_TO_SUBGRAPH_ORGANIC";
  protected static final String ITEM_MODE_COMPONENT_ASSIGNMENT = "MODE_COMPONENT_ASSIGNMENT";
  protected static final String VALUE_MODE_COMPONENT_CONNECTED = "MODE_COMPONENT_CONNECTED";
  protected static final String VALUE_MODE_COMPONENT_SINGLE = "MODE_COMPONENT_SINGLE";
  protected static final String VALUE_MODE_COMPONENT_CLUSTERING = "MODE_COMPONENT_CLUSTERING";
  protected static final String VALUE_MODE_COMPONENT_CUSTOMIZED = "MODE_COMPONENT_CUSTOMIZED";
  protected static final String ITEM_SUBGRAPH_LAYOUTER = "SUBGRAPH_LAYOUTER";
  protected static final String VALUE_SUBGRAPH_LAYOUTER_IHL = "SUBGRAPH_LAYOUTER_IHL";
  protected static final String VALUE_SUBGRAPH_LAYOUTER_ORGANIC = "SUBGRAPH_LAYOUTER_ORGANIC";
  protected static final String VALUE_SUBGRAPH_LAYOUTER_CIRCULAR = "SUBGRAPH_LAYOUTER_CIRCULAR";
  protected static final String VALUE_SUBGRAPH_LAYOUTER_ORTHOGONAL = "SUBGRAPH_LAYOUTER_ORTHOGONAL";
  protected static final String VALUE_SUBGRAPH_LAYOUTER_NO_LAYOUT = "SUBGRAPH_LAYOUTER_NO_LAYOUT";
  protected static final String ITEM_SUBGRAPH_POSITION_STRATEGY = "SUBGRAPH_POSITION_STRATEGY";
  protected static final String VALUE_SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER = "SUBGRAPH_POSITION_STRATEGY_BARYCENTER";
  protected static final String VALUE_SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH = "SUBGRAPH_POSITION_STRATEGY_FROM_SKETCH";
  protected static final String ITEM_MIN_NODE_DIST = "MIN_NODE_DIST";
  protected static final String ITEM_ORIENTATION_MAIN_GRAPH = "ORIENTATION_MAIN_GRAPH";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_AUTO_DETECT = "ORIENTATION_MAIN_GRAPH_AUTO_DETECT";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN = "ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP = "ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT = "ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT = "ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT";
  protected static final String VALUE_ORIENTATION_MAIN_GRAPH_NONE = "ORIENTATION_MAIN_GRAPH_NONE";
  protected static final String ITEM_CONSIDER_SNAPLINES = "CONSIDER_SNAPLINES";
  protected static final String ITEM_CONSIDER_EDGE_DIRECTION = "CONSIDER_EDGE_DIRECTION";

  /**
   * Creates an instance of this module.
   */
  public PartialLayoutModule() {
    super(MODULE_PARTIAL);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    
    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate items
    options.addEnum(ITEM_ROUTING_TO_SUBGRAPH, new String[]{
        VALUE_ROUTING_TO_SUBGRAPH_AUTO,
        VALUE_ROUTING_TO_SUBGRAPH_STRAIGHT_LINE,
        VALUE_ROUTING_TO_SUBGRAPH_POLYLINE,
        VALUE_ROUTING_TO_SUBGRAPH_ORTHOGONALLY,
        VALUE_ROUTING_TO_SUBGRAPH_ORGANIC
    }, 0);
    options.addEnum(ITEM_MODE_COMPONENT_ASSIGNMENT, new String[]{
        VALUE_MODE_COMPONENT_CONNECTED,
        VALUE_MODE_COMPONENT_SINGLE,
        VALUE_MODE_COMPONENT_CLUSTERING,
        VALUE_MODE_COMPONENT_CUSTOMIZED
    }, 0);
    options.addEnum(ITEM_SUBGRAPH_LAYOUTER, new String[]{
        VALUE_SUBGRAPH_LAYOUTER_IHL,
        VALUE_SUBGRAPH_LAYOUTER_ORGANIC,
        VALUE_SUBGRAPH_LAYOUTER_CIRCULAR,
        VALUE_SUBGRAPH_LAYOUTER_ORTHOGONAL,
        VALUE_SUBGRAPH_LAYOUTER_NO_LAYOUT
    }, 0);
    options.addEnum(ITEM_SUBGRAPH_POSITION_STRATEGY, new String[]{
        VALUE_SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER,
        VALUE_SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH
    }, 0);
    options.addInt(ITEM_MIN_NODE_DIST, 30, 1, 100);
    options.addEnum(ITEM_ORIENTATION_MAIN_GRAPH, new String[]{
        VALUE_ORIENTATION_MAIN_GRAPH_AUTO_DETECT,
        VALUE_ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN,
        VALUE_ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP,
        VALUE_ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT,
        VALUE_ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT,
        VALUE_ORIENTATION_MAIN_GRAPH_NONE
    }, 0);
    options.addBool(ITEM_CONSIDER_SNAPLINES, true);
    options.addBool(ITEM_CONSIDER_EDGE_DIRECTION, false);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final Graph2D graph = getGraph2D();
    if (graph.selectedNodes().size() + graph.selectedEdges().size() == 0) {
      return; //nothing to do
    }

    final PartialLayouter partial = new PartialLayouter();

    final OptionHandler options = getOptionHandler();
    configure(partial, options);

    final Graph2DLayoutExecutor layoutExecutor = getLayoutExecutor();
    final TableLayoutConfigurator tableLayoutConf = layoutExecutor.getTableLayoutConfigurator();
    final boolean wasConfTableNodeRealizer = layoutExecutor.isConfiguringTableNodeRealizers();
    final boolean wasHorizontalLayoutConf = tableLayoutConf.isHorizontalLayoutConfiguration();
    layoutExecutor.setConfiguringTableNodeRealizers(true);
    tableLayoutConf.setHorizontalLayoutConfiguration(isHorizontalLayoutConf(options));
    
    prepareGraph(graph, options);
    try {
      launchLayouter(partial);
    } finally {
      restoreGraph(graph, options);

      layoutExecutor.setConfiguringTableNodeRealizers(wasConfTableNodeRealizer);
      tableLayoutConf.setHorizontalLayoutConfiguration(wasHorizontalLayoutConf);
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
  protected void prepareGraph(final Graph2D graph, OptionHandler options) {
    // backup existing data providers to prevent loss of user settings
    backupDataProvider(graph, PartialLayouter.PARTIAL_NODES_DPKEY);
    backupDataProvider(graph, PartialLayouter.PARTIAL_EDGES_DPKEY);
    //register dp for selected nodes/edges
    graph.addDataProvider(PartialLayouter.PARTIAL_NODES_DPKEY, Selections.createSelectionNodeMap(graph));
    graph.addDataProvider(PartialLayouter.PARTIAL_EDGES_DPKEY, Selections.createSelectionEdgeMap(graph));

    if (options.getBool(ITEM_CONSIDER_EDGE_DIRECTION)
        && graph.getDataProvider(PartialLayouter.DIRECTED_EDGES_DPKEY) == null) {
      graph.addDataProvider(PartialLayouter.DIRECTED_EDGES_DPKEY, new ModuleDataProvider() {
        public boolean getBool(Object dataHolder) {
          if (!(dataHolder instanceof Edge)) {
            return false;
          }

          final EdgeRealizer realizer = graph.getRealizer((Edge) dataHolder);
          // directed => exactly one endpoint has an arrow
          return (realizer.getSourceArrow() == Arrow.NONE) == (realizer.getTargetArrow() != Arrow.NONE); 
        }
      });
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // remove the data providers set by this module by restoring the initial state
    if (graph.getDataProvider(PartialLayouter.DIRECTED_EDGES_DPKEY) instanceof ModuleDataProvider) {
      // so options.getBool(ITEM_CONSIDER_EDGE_DIRECTION) is also true 
      graph.removeDataProvider(PartialLayouter.DIRECTED_EDGES_DPKEY);
    }

    restoreDataProvider(graph, PartialLayouter.PARTIAL_NODES_DPKEY);
    restoreDataProvider(graph, PartialLayouter.PARTIAL_EDGES_DPKEY);
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param partial the <code>PartialLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final PartialLayouter partial, final OptionHandler options) {
    partial.setMinimalNodeDistance(options.getInt(ITEM_MIN_NODE_DIST));
    partial.setConsiderNodeAlignment(options.getBool(ITEM_CONSIDER_SNAPLINES));

    final byte subgraphPositioningStrategy =
        subgraphPositioningStrategyAsByte(options.getString(ITEM_SUBGRAPH_POSITION_STRATEGY));
    partial.setPositioningStrategy(subgraphPositioningStrategy);

    final byte componentAssignment = componentAssignmentAsByte(options.getString(ITEM_MODE_COMPONENT_ASSIGNMENT));
    partial.setComponentAssignmentStrategy(componentAssignment);

    final byte mainGraphOrientation = graphOrientationAsByte(options.getString(ITEM_ORIENTATION_MAIN_GRAPH));
    partial.setLayoutOrientation(mainGraphOrientation);

    final byte routingToSubGraph = routingToSubGraphAsByte(options.getString(ITEM_ROUTING_TO_SUBGRAPH));
    partial.setEdgeRoutingStrategy(routingToSubGraph);

    partial.setCoreLayouter(defineSubgraphLayouter(options, routingToSubGraph));
  }

  private boolean isHorizontalLayoutConf(OptionHandler options) {
    final byte mainGraphOrientation = graphOrientationAsByte(options.getString(ITEM_ORIENTATION_MAIN_GRAPH));
    return (mainGraphOrientation == PartialLayouter.ORIENTATION_LEFT_TO_RIGHT)
        || (mainGraphOrientation == PartialLayouter.ORIENTATION_RIGHT_TO_LEFT);
  }

  
  private Layouter defineSubgraphLayouter(final OptionHandler options, final byte routingToSubGraph) {
    final String subgraphLayouterString = options.getString(ITEM_SUBGRAPH_LAYOUTER);
    if (VALUE_SUBGRAPH_LAYOUTER_IHL.equals(subgraphLayouterString)) {
      final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
      ihl.setIntegratedEdgeLabelingEnabled(true);
      if (PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL == routingToSubGraph) {
        ihl.setOrthogonallyRouted(true);
      } else {
        ihl.setOrthogonallyRouted(false);
      }
      return ihl;
    } else if (VALUE_SUBGRAPH_LAYOUTER_ORGANIC.equals(subgraphLayouterString)) {
      final SmartOrganicLayouter sol = new SmartOrganicLayouter();
      sol.setDeterministic(true);
      return sol;
    } else if (VALUE_SUBGRAPH_LAYOUTER_CIRCULAR.equals(subgraphLayouterString)) {
      return new CircularLayouter();
    } else if (VALUE_SUBGRAPH_LAYOUTER_ORTHOGONAL.equals(subgraphLayouterString)) {
      return new OrthogonalLayouter();
    } else {
      // else if VALUE_MODE_COMPONENT_SINGLE.equals(options.getString(ITEM_MODE_COMPONENT_ASSIGNMENT)
      // or VALUE_SUBGRAPH_LAYOUTER_ORTHOGONAL.equals(subgraphLayouterString)
      // or VALUE_SUBGRAPH_LAYOUTER_NO_LAYOUT.equals(subgraphLayouterString)
      
      // trivial case: no subgraph layouter
      return null;
    }
  }

  /** Determines the routing of inter edges (main graph to subgraph). */
  private static byte routingToSubGraphAsByte(final String routingToSubGraphString) {
    if (VALUE_ROUTING_TO_SUBGRAPH_ORTHOGONALLY.equals(routingToSubGraphString)) {
      return PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL;
    } else if (VALUE_ROUTING_TO_SUBGRAPH_AUTO.equals(routingToSubGraphString)) {
      return PartialLayouter.EDGE_ROUTING_STRATEGY_AUTOMATIC;
    } else if (VALUE_ROUTING_TO_SUBGRAPH_ORGANIC.equals(routingToSubGraphString)) {
      return PartialLayouter.EDGE_ROUTING_STRATEGY_ORGANIC;
    } else if (VALUE_ROUTING_TO_SUBGRAPH_POLYLINE.equals(routingToSubGraphString)) {
      return PartialLayouter.EDGE_ROUTING_STRATEGY_OCTILINEAR;
    } else {
      // else if VALUE_ROUTING_TO_SUBGRAPH_STRAIGHT_LINE.equals(routingToSubGraphString)
      return PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE;
    }
  }

  /** Determines the subgraph position strategy. */
  private static byte subgraphPositioningStrategyAsByte(final String subgraphPositioningStrategyString) {
    if (VALUE_SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH.equals(subgraphPositioningStrategyString)) {
      return PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH;
    } else {
      return PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER;
    }
  }

  /** Determines component by: {Clustering, Connected graph, Particular}. */
  private static byte componentAssignmentAsByte(final String componentAssignmentString) {
    if (VALUE_MODE_COMPONENT_SINGLE.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE;
    } else if (VALUE_MODE_COMPONENT_CONNECTED.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED;
    } else if (VALUE_MODE_COMPONENT_CLUSTERING.equals(componentAssignmentString)) {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CLUSTERING;
    } else {
      return PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CUSTOMIZED;
    }
  }

  /** Determines the main graph Orientation. */
  private static byte graphOrientationAsByte(final String graphOrientationString) {
    if (VALUE_ORIENTATION_MAIN_GRAPH_AUTO_DETECT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_AUTO_DETECTION;
    } else if (VALUE_ORIENTATION_MAIN_GRAPH_TOP_TO_DOWN.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_TOP_TO_BOTTOM;
    } else if (VALUE_ORIENTATION_MAIN_GRAPH_DOWN_TO_TOP.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_BOTTOM_TO_TOP;
    } else if (VALUE_ORIENTATION_MAIN_GRAPH_LEFT_TO_RIGHT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_LEFT_TO_RIGHT;
    } else if (VALUE_ORIENTATION_MAIN_GRAPH_RIGHT_TO_LEFT.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_RIGHT_TO_LEFT;
    } else if (VALUE_ORIENTATION_MAIN_GRAPH_NONE.equals(graphOrientationString)) {
      return PartialLayouter.ORIENTATION_NONE;
    } else {
      return PartialLayouter.ORIENTATION_AUTO_DETECTION;
    }
  }

  private class ModuleDataProvider extends DataProviderAdapter {
  }
}
