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
package demo.layout.router;

import demo.view.DemoBase;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.Layouter;
import y.layout.PortConstraintConfigurator;
import y.layout.PortConstraintKeys;
import y.layout.router.ChannelEdgeRouter;
import y.layout.router.OrthogonalEdgeRouter;
import y.layout.router.polyline.EdgeRouter;
import demo.layout.module.ChannelEdgeRouterModule;
import demo.layout.module.OrthogonalEdgeRouterModule;
import demo.layout.module.PolylineEdgeRouterModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BendList;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.MovePortMode;
import y.view.PolyLineEdgeRealizer;
import y.view.Port;
import y.view.PortAssignmentMoveSelectionMode;
import y.view.Selections;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


/**
 * A demo that shows some of the capabilities of the yFiles Orthogonal Edge Router implementations.
 * <br> The following aspects of using the edge routers are demonstrated.
 * <ol>
 *   <li>
 *     How to use OrthogonalEdgeRouterModule, ChannelEdgeRouterModule or PolylineEdgeRouterModule
 *     as a convenient means to launch and configure the edge routers.
 *   </li>
 *   <li>
 *     How to modify the yFiles EditMode in order to trigger the orthogonal edge router whenever
 *     <ul>
 *       <li>new edges get created</li>
 *       <li>nodes get resized</li>
 *       <li>selected nodes will be moved</li>
 *     </ul>
 *   </li>
 *   <li>
 *     How to specify port constraints for the edge router. With the help of port constraints it
 *     is possible to tell the orthogonal edge router on which side of a node or on which exact
 *     coordinate a start or endpoint of an edge should connect to a node.
 *   </li>
 * </ol>
 * <br>
 * Usage: Create nodes. Create edges crossing other nodes. The edges will be routed immediately.
 * To reroute all edges use the toolbar button "Route Edges".
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/polyline_edge_router.html">Section Polyline Edge Routing</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_edge_router.html">Section Orthogonal Edge Routing</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/channel_edge_router.html">Section Channel Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class EdgeRouterDemo extends DemoBase {
  RouterStrategy strategy;
  PortAssignmentMoveSelectionMode paMode;

  // three available strategies
  private OrthogonalEdgeRouterStrategy orthogonalEdgeRouterStrategy;
  private ChannelEdgeRouterStrategy channelEdgeRouterStrategy;
  private PolyLineEdgeRouterStrategy polylineEdgeRouterStrategy;

  public EdgeRouterDemo() {
    Graph2D graph = view.getGraph2D();
    EdgeMap sourcePortMap = graph.createEdgeMap();
    EdgeMap targetPortMap = graph.createEdgeMap();
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);
    paMode.setSpc(sourcePortMap);
    paMode.setTpc(targetPortMap);
    
    createInitialGraph();
  }

  protected void initialize() {
    channelEdgeRouterStrategy = new ChannelEdgeRouterStrategy();
    orthogonalEdgeRouterStrategy = new OrthogonalEdgeRouterStrategy();
    polylineEdgeRouterStrategy = new PolyLineEdgeRouterStrategy();
  }

  protected void createInitialGraph() {
    Graph2D graph = view.getGraph2D();
    graph.createEdge(graph.createNode(100,100,"1"), graph.createEdge(graph.createNode(200,200,"2"), graph.createNode(300,100,"3")).source());
  }

  /**
   * Returns ViewActionDemo toolbar plus actions to trigger some layout algorithms
   */
  protected JToolBar createToolBar() {
    final JComboBox comboBox = new JComboBox(new Object[]{"Polyline Edge Router", "Orthogonal Edge Router", "Channel Edge Router"});
    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (comboBox.getSelectedIndex()) {
          case 0:
            strategy = polylineEdgeRouterStrategy;
            setSmoothedBends(false);
            break;
          case 1:
            strategy = orthogonalEdgeRouterStrategy;
            setSmoothedBends(true);
            break;
          case 2:
            strategy = channelEdgeRouterStrategy;
            setSmoothedBends(true);
        }
        strategy.getModule().start(view.getGraph2D());
      }
    });
    strategy = polylineEdgeRouterStrategy;

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(new LayoutAction()));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(comboBox);
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(createActionControl(new OptionAction()));

    return toolBar;
  }

  private void setSmoothedBends(boolean smoothBends) {
    Graph2D graph = view.getGraph2D();

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      EdgeRealizer realizer = graph.getRealizer(edge);
      ((PolyLineEdgeRealizer) realizer).setSmoothedBends(smoothBends);
    }

    // also adjust the default edge realizer for newly created edges
    ((PolyLineEdgeRealizer) graph.getDefaultEdgeRealizer()).setSmoothedBends(smoothBends);
  }


  /**
   * Provides configuration options for the edge router.
   */
  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Settings...", getIconResource("resource/properties.png"));
    }

    public void actionPerformed(ActionEvent e) {
      OptionSupport.showDialog(strategy.getModule(), view.getGraph2D(), false, view.getFrame());
    }
  }

  /**
   * Launches the Orthogonal Edge Router.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Route Edges", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent e) {
      strategy.getModule().start(view.getGraph2D());
    }
  }

  /**
   * Adds a specially configured EditMode that will automatically route all newly created edges orthogonally. The
   * orthogonal edge router will also be activated on some edges, when nodes get resized or a node selection gets
   * moved.
   */
  protected void registerViewModes() {
    EditMode mode = new EditMode();
    mode.setMoveSelectionMode(paMode = new MyMoveSelectionMode());
    mode.setCreateEdgeMode(new MyCreateEdgeMode());
    mode.setHotSpotMode(new MyHotSpotMode());
    mode.setMovePortMode(new MyMovePortMode());
    view.addViewMode(mode);
  }

  /** A special mode for creating edges. */
  class MyCreateEdgeMode extends CreateEdgeMode {
    MyCreateEdgeMode() {
      super();
      allowSelfloopCreation(false);
    }

    protected void edgeCreated(final Edge e) {
      final Graph2D graph = view.getGraph2D();

      strategy.routeEdge(e);

      graph.updateViews();
    }
  }


  /** A special mode for resizing nodes. */
  class MyHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);

      final Graph2D graph = view.getGraph2D();

      DataProvider selectedNodes = Selections.createSelectionDataProvider(graph);
      strategy.rerouteAdjacentEdges(selectedNodes, graph);
      graph.updateViews();
    }
  }

  /** A special mode for moving a selection of the graph. */
  class MyMoveSelectionMode extends PortAssignmentMoveSelectionMode {

    MyMoveSelectionMode() {
      super(null, null);
    }

    private boolean routeEdgesOnMove = true;

    protected BendList getBendsToBeMoved() {
      BendList bends = super.getBendsToBeMoved();

      //add all bends from edges, whose source and target nodes are selected, since they will not be routed. 
      for (NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        for(EdgeCursor edgeCursor = node.outEdges(); edgeCursor.ok(); edgeCursor.next()) {
          Edge edge = edgeCursor.edge();
          if(getGraph2D().isSelected(edge.target())){
            for(BendCursor bendCursor = getGraph2D().getRealizer(edge).bends(); bendCursor.ok(); bendCursor.next()){
              Bend bend = bendCursor.bend();
              bends.add(bend);
            }
          }
        }
      }
      return bends;
    }

    protected void selectionOnMove(double dx, double dy, double x, double y) {
      super.selectionOnMove(dx, dy, x, y);
      if (routeEdgesOnMove) {
        routeEdgesToSelection(false);
      }
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      super.selectionMovedAction(dx, dy, x, y);
      routeEdgesToSelection(true);
    }

    void routeEdgesToSelection(boolean includeBends) {
      final Graph2D graph = view.getGraph2D();
      if (graph.selectedNodes().ok() || (includeBends && graph.selectedBends().ok())) {
        strategy.routeEdgesToSelection(graph);
        graph.updateViews();
      }
    }
  }

  class MyMovePortMode extends MovePortMode {
    MyMovePortMode() {
      setChangeEdgeEnabled(true);
      setIndicatingTargetNode(true);
    }

    protected void portMoved(Port port, double x, double y) {
      super.portMoved(port, x, y);
      final Edge edge = port.getOwner().getEdge();
      strategy.routeEdge(edge);
    }
  }


  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new EdgeRouterDemo()).start("Orthogonal Edge Router Demo");
      }
    });
  }

  abstract static class RouterStrategy {
    abstract YModule getModule();

    abstract void routeEdge(final Edge e);

    abstract void rerouteAdjacentEdges(final DataProvider selectedNodes, final Graph2D graph);

    abstract void routeEdgesToSelection(final Graph2D graph);

    abstract void route(final Graph2D graph);

    protected void routeEdge(final Edge e, final Graph2D graph) {
      EdgeMap spc = (EdgeMap) graph.getDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
      EdgeMap tpc = (EdgeMap) graph.getDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);

      PortConstraintConfigurator pcc = new PortConstraintConfigurator();
      if (spc != null && tpc != null) {
        spc.set(e, pcc.createPortConstraintFromSketch(graph, e, true, false));
        tpc.set(e, pcc.createPortConstraintFromSketch(graph, e, false, false));
        route(graph);
        spc.set(e, null);
        tpc.set(e, null);
      } else {
        route(graph);
      }
    }

    protected void routeEdgesToSelection(final Graph2D graph, final Object affectedEdgesKey) {
      final Set selectedEdges = new HashSet();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (graph.isSelected(edge.source()) ^ graph.isSelected(edge.target())) {
          selectedEdges.add(edge);
          continue;
        }
        for (BendCursor bc = graph.selectedBends(); bc.ok(); bc.next()) {
          final Bend bend = (Bend) bc.current();
          if (bend.getEdge() == edge) {
            selectedEdges.add(edge);
            break;
          }
        }
      }
      graph.addDataProvider(affectedEdgesKey, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedEdges.contains(dataHolder);
        }
      });
      route(graph);
      graph.removeDataProvider(affectedEdgesKey);
    }

    protected void routeEdge(final Edge e, final Graph2D graph, final Object selectedEdgesKey) {
      graph.addDataProvider(selectedEdgesKey, new DataProviderAdapter() {
        public boolean getBool(Object o) {
          return e == o;
        }
      });
      routeEdge(e, graph);
      graph.removeDataProvider(selectedEdgesKey);
    }
  }

  static class OrthogonalEdgeRouterStrategy extends RouterStrategy {
    private final MyOrthogonalEdgeRouterModule module;

    public OrthogonalEdgeRouterStrategy() {
      module = new MyOrthogonalEdgeRouterModule();
      // disables the routing animation
      module.setMorphingEnabled(false);
    }
    
    public YModule getModule() {
      return module;
    }

    public void routeEdge(final Edge e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      module.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
      routeEdge(e, graph, Layouter.SELECTED_EDGES);
      module.resetSphereOfAction();
    }

    public void rerouteAdjacentEdges(final DataProvider selectedNodes, final Graph2D graph) {
      module.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
      graph.addDataProvider(Layouter.SELECTED_NODES, selectedNodes);
      this.route(graph);
      graph.removeDataProvider(Layouter.SELECTED_NODES);
      module.resetSphereOfAction();
    }

    public void routeEdgesToSelection(final Graph2D graph) {
      module.setSphereOfAction(OrthogonalEdgeRouter.ROUTE_SELECTED_EDGES);
      routeEdgesToSelection(graph, Layouter.SELECTED_EDGES);
      module.resetSphereOfAction();
    }

    void route(final Graph2D graph) {
      module.start(graph);
    }
  }

  /**
   *  Overwrites (if necessary) the scope (or sphere of action) of the routing
   *  algorithm.
   */
  private static class MyOrthogonalEdgeRouterModule extends OrthogonalEdgeRouterModule {
    private final byte UNDEFINED = -1;
    private byte sphereOfAction = UNDEFINED;

    protected void configure(final OrthogonalEdgeRouter orthogonal, final OptionHandler options) {
      super.configure(orthogonal, options);

      if (sphereOfAction != UNDEFINED) {
        orthogonal.setSphereOfAction(sphereOfAction);
      }
    }

    public void setSphereOfAction(final byte sphereOfAction) {
      this.sphereOfAction = sphereOfAction;
    }

    public void resetSphereOfAction() {
      sphereOfAction = UNDEFINED;
    }
  }

  static class ChannelEdgeRouterStrategy extends RouterStrategy {
    private final ChannelEdgeRouterModule module;
    
    public ChannelEdgeRouterStrategy() {
      module = new ChannelEdgeRouterModule();
      // disables the routing animation
      module.setMorphingEnabled(false);
    }

    public YModule getModule() {
      return module;
    }

    public void routeEdge(final Edge e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      routeEdge(e, graph, ChannelEdgeRouter.AFFECTED_EDGES);
    }

    public void rerouteAdjacentEdges(final DataProvider selectedNodes, final Graph2D graph) {
      graph.addDataProvider(ChannelEdgeRouter.AFFECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedNodes.getBool((((Edge) dataHolder).source())) || selectedNodes.getBool(((Edge) dataHolder).target());
        }
      });
      this.route(graph);
      graph.removeDataProvider(ChannelEdgeRouter.AFFECTED_EDGES);
    }

    public void routeEdgesToSelection(final Graph2D graph) {
      routeEdgesToSelection(graph, ChannelEdgeRouter.AFFECTED_EDGES);
    }

    void route(final Graph2D graph) {
      module.start(graph);
    }
  }

  static class PolyLineEdgeRouterStrategy extends RouterStrategy {
    private final MyPolylineEdgeRouterModule module;

    PolyLineEdgeRouterStrategy() {
      module = new MyPolylineEdgeRouterModule();
      // disables routing animation
      module.setMorphingEnabled(false);
    }

    YModule getModule() {
      return module;
    }

    void routeEdge(final Edge e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      module.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      routeEdge(e, graph, Layouter.SELECTED_EDGES);
      module.resetSphereOfAction();
    }

    void rerouteAdjacentEdges(final DataProvider selectedNodes, final Graph2D graph) {
      module.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      graph.addDataProvider(Layouter.SELECTED_EDGES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedNodes.getBool(((Edge) dataHolder).source())
              || selectedNodes.getBool(((Edge) dataHolder).target());
        }
      });
      this.route(graph);
      graph.removeDataProvider(Layouter.SELECTED_EDGES);
      module.resetSphereOfAction();
    }

    void routeEdgesToSelection(final Graph2D graph) {
      module.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      routeEdgesToSelection(graph, Layouter.SELECTED_EDGES);
      module.resetSphereOfAction();
    }

    void route(final Graph2D graph) {
      module.start(graph);
    }
  }

  /**
   * Sets custom initial option values and (if necessary) overwrites the scope
   * (or sphere of action) for the routing algorithm.
   */
  private static class MyPolylineEdgeRouterModule extends PolylineEdgeRouterModule {
    private static final byte UNDEFINED = -1;
    private byte sphereOfAction = UNDEFINED;

    protected OptionHandler createOptionHandler() {
      final OptionHandler options = super.createOptionHandler();
      options.set(ITEM_ENABLE_POLYLINE_ROUTING, Boolean.TRUE);
      return options;
    }

    protected void configure(EdgeRouter router, OptionHandler options) {
      super.configure(router, options);

      if (sphereOfAction != UNDEFINED) {
        router.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      }
    }

    public void setSphereOfAction(final byte sphereOfAction) {
      this.sphereOfAction = sphereOfAction;
    }

    public void resetSphereOfAction() {
      sphereOfAction = UNDEFINED;
    }
  }
}


      
