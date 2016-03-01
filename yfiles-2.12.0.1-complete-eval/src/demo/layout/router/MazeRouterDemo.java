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
import demo.view.DemoDefaults;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeList;
import y.io.IOHandler;
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
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.Drawable;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.HotSpotMode;
import y.view.MovePortMode;
import y.view.MoveSelectionMode;
import y.view.Port;
import y.view.Selections;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A demo that shows how Orthogonal Edge Router and Channel Edge Router can be used to find routes through a maze.
 * Not only will it find a way but also one with fewest possible changes in direction.
 * <br>
 * The following aspects of using the edge routers are demonstrated.
 * <ol>
 * <li>How to use OrthogonalEdgeRouterModule, ChannelEdgeRouterModules or PolylineEdgeRouterModule, respectively as
 *     a convenient means to launch and configure the edge routers.</li>
 * <li>How to modify the yFiles EditMode in order to trigger the
 *     orthogonal edge router whenever
 *     <ul>
 *     <li>new edges get created</li>
 *     <li>nodes get resized</li>
 *     <li>selected nodes will be moved</li>
 *     </ul></li>
 * </ol>
 * Additionally this demo shows how non-editable background-layer graphs can be displayed inside
 * the graph view.
 * <br/>
 * Usage: Create nodes and edges. The edges will be routed immediately. To reroute all edges use
 * the toolbar button "Route Edges".
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/polyline_edge_router.html">Section Polyline Edge Routing</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_edge_router.html">Section Orthogonal Edge Routing</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/channel_edge_router.html">Section Channel Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class MazeRouterDemo extends DemoBase {
  private RouterStrategy strategy;
  private Graph2D mazeG;
  private Drawable mazeD;
  private NodeList mazeNodes;
  private OrthogonalEdgeRouterStrategy orthogonalEdgeRouterStrategy;
  private ChannelEdgeRouterStrategy channelEdgeRouterStrategy;
  private PolylineEdgeRouterStrategy polylineEdgeRouterStrategy;

  public MazeRouterDemo() {
    initializeMaze();

    initializeGraph();
  }

  protected void initialize() {
    channelEdgeRouterStrategy = new ChannelEdgeRouterStrategy();
    orthogonalEdgeRouterStrategy = new OrthogonalEdgeRouterStrategy();
    polylineEdgeRouterStrategy = new PolylineEdgeRouterStrategy();
    view.setContentPolicy(Graph2DView.CONTENT_POLICY_BACKGROUND_DRAWABLES);
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    view.getGraph2D().getDefaultNodeRealizer().setSize(30, 30);
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
            break;
          case 1:
            strategy = orthogonalEdgeRouterStrategy;
            break;
          case 2:
            strategy = channelEdgeRouterStrategy;
        }
        doLayout();
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

  /**
   * Provides configuration options for the edge router.
   */
  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Settings...", getIconResource("resource/properties.png"));
    }

    public void actionPerformed(ActionEvent e) {
      final ActionListener listener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doLayout();
        }
      };
      OptionSupport.showDialog(strategy.getModule().getOptionHandler(), listener, false, view.getFrame());
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
      doLayout();
    }
  }

  /**
   * Modified action to fit the content nicely inside the view.
   */
  class FitContent extends AbstractAction {
    FitContent() {
      super("Fit Content");
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D graph = view.getGraph2D();

      Rectangle r = graph.getBoundingBox();
      r.add(mazeD.getBounds());
      view.fitRectangle(r);
      graph.updateViews();
    }
  }

  void doLayout() {
    Graph2D graph = view.getGraph2D();
    graph.firePreEvent();
    try {
      addMazeGraph();
      // Start the module.
      strategy.getModule().start(graph);
      subtractMazeGraph();
    } finally {
      graph.firePostEvent();
    }
  }

  /**
   * Adds a specially configured EditMode that will automatically route all
   * newly created edges orthogonally. The orthogonal edge router will also
   * be activated on some edges, when nodes get resized or a node selection gets
   * moved.
   */
  protected void registerViewModes() {
    EditMode mode = new EditMode();
    view.addViewMode(mode);

    mode.setMoveSelectionMode(new MyMoveSelectionMode());
    mode.setCreateEdgeMode(new MyCreateEdgeMode());
    mode.setHotSpotMode(new MyHotSpotMode());
    mode.setMovePortMode(new MyMovePortMode());
  }

  /**
   * A special mode for creating edges.
   */
  class MyCreateEdgeMode extends CreateEdgeMode {
    private Node source;

    protected boolean acceptSourceNode(Node s, double x, double y) {
      source = s;
      return true;
    }

    protected boolean acceptTargetNode(Node t, double x, double y) {
      return (source != t);
    }

    protected Edge createEdge(Graph2D graph, Node startNode, Node targetNode, EdgeRealizer realizer) {
      graph.firePreEvent();
      return super.createEdge(graph, startNode, targetNode, realizer);
    }

    protected void edgeCreated(final Edge e) {
      routeEdge(e);
      getGraph2D().firePostEvent();
    }
  }

  void routeEdge(Edge e) {
    final Graph2D graph = view.getGraph2D();
    addMazeGraph();
    strategy.routeEdge(e);
    subtractMazeGraph();
    graph.updateViews();
  }

  /**
   * A special mode for resizing nodes.
   */
  class MyHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);

      final Graph2D graph = view.getGraph2D();

      DataProvider selectedNodes = Selections.createSelectionDataProvider(graph);
      addMazeGraph();
      strategy.rerouteAdjacentEdges(selectedNodes, graph);
      subtractMazeGraph();
      graph.updateViews();
    }
  }

  /**
   * A special mode for moving a selection of the graph.
   */
  class MyMoveSelectionMode extends MoveSelectionMode {
    private static final boolean ROUTE_EDGES_ON_MOVE = false;

    protected void selectionOnMove(double dx, double dy, double x, double y) {
      if (ROUTE_EDGES_ON_MOVE) {
        routeEdgesToSelection(false);
      }
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      routeEdgesToSelection(true);
    }

    void routeEdgesToSelection(boolean includeBends) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      graph.backupRealizers();
      try {
        if (graph.selectedNodes().ok() || (includeBends && graph.selectedBends().ok())) {
          addMazeGraph();
          strategy.routeEdgesToSelection(graph);
          subtractMazeGraph();
          graph.updateViews();
        }
      } finally {
        graph.firePostEvent();
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

  /**
   * Adds the maze to the user-given graph, so that the edge router can lay
   * Ariadne's thread...
   */
  private void addMazeGraph() {
    mazeNodes = new NodeList(mazeG.nodes());
    mazeG.moveSubGraph(mazeNodes, view.getGraph2D());
  }

  /**
   * The maze gets removed from the user-given graph again.
   **/
  private void subtractMazeGraph() {
    view.getGraph2D().moveSubGraph(mazeNodes, mazeG);
  }

  /**
   * Initializes the maze the first time.
   */
  private void initializeMaze() {
    mazeG = new Graph2D();
    try {
      IOHandler ioHandler = createGraphMLIOHandler();
      ioHandler.read(mazeG, getResource("resource/maze.graphml"));
      DemoDefaults.applyFillColor(mazeG, DemoDefaults.DEFAULT_CONTRAST_COLOR);
      DemoDefaults.applyLineColor(mazeG, DemoDefaults.DEFAULT_CONTRAST_COLOR);
      
    } catch (IOException e) {
      System.out.println("Could not initialize maze!");
      e.printStackTrace();
      System.exit(-1);
    }
    // Create a drawable and add it to the graph as a visual representation
    // of the maze. This way it is not possible to move the maze's walls.
    mazeD = new MazeDrawable(mazeG);
    view.addBackgroundDrawable(mazeD);
    view.fitRectangle(mazeD.getBounds());
  }

  /**
   * Creates an initial graph and initially routes the contained edge through the maze
   */
  private void initializeGraph() {
    loadGraph("resource/mazeRouterDemo.graphml");
  }
  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new MazeRouterDemo()).start();
      }
    });
  }

  /**
   * To transform the whole maze graph into a maze drawable.
   */
  static class MazeDrawable implements Drawable {
    private Graph2D mazeG;
    private DefaultGraph2DRenderer render;

    public MazeDrawable(Graph2D g) {
      mazeG = g;
      render = new DefaultGraph2DRenderer();
    }

    public Rectangle getBounds() {
      return mazeG.getBoundingBox();
    }

    public void paint(Graphics2D gfx) {
      render.paint(gfx, mazeG);
    }
  }

  abstract static class RouterStrategy {
    abstract YModule getModule();

    abstract void routeEdge(Edge e);

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

    protected void routeEdgesToSelection(final Graph2D graph, Object affectedEdgesKey) {
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

    protected void routeEdge(final Edge e, Graph2D graph, Object selectedEdgesKey) {
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

    OrthogonalEdgeRouterStrategy() {
      module = new MyOrthogonalEdgeRouterModule();
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
    private final MyChannelEdgeRouterModule module;

    public ChannelEdgeRouterStrategy() {
      module = new MyChannelEdgeRouterModule();
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
          return selectedNodes.getBool((((Edge) dataHolder).source())) || selectedNodes.getBool(
              ((Edge) dataHolder).target());
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

  /**
   * Sets custom initial option values for the routing algorithm.
   */
  private static class MyChannelEdgeRouterModule extends ChannelEdgeRouterModule {
    protected OptionHandler createOptionHandler() {
      final OptionHandler options = super.createOptionHandler();
      options.set(ITEM_PATHFINDER, VALUE_ORTHOGONAL_SHORTESTPATH_PATH_FINDER);
      return options;
    }
  }

  static class PolylineEdgeRouterStrategy extends RouterStrategy {
    private final MyPolylineEdgeRouterModule module;

    PolylineEdgeRouterStrategy() {
      module = new MyPolylineEdgeRouterModule();
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
    private final byte UNDEFINED = -1;
    private byte sphereOfAction = UNDEFINED;

    protected OptionHandler createOptionHandler() {
      final OptionHandler options = super.createOptionHandler();
      options.set(ITEM_ENABLE_POLYLINE_ROUTING, Boolean.TRUE);
      return options;
    }

    protected void configure(final EdgeRouter router, final OptionHandler options) {
      super.configure(router, options);

      if (sphereOfAction != UNDEFINED) {
        router.setSphereOfAction(sphereOfAction);
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
