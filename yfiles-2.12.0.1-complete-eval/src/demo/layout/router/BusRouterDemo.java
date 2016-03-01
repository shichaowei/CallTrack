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
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeList;
import y.geom.YPoint;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.view.Arrow;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.HotSpotMode;
import y.view.MovePortMode;
import y.view.MoveSelectionMode;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.SelectionBoxMode;
import y.view.ShapeNodePainter;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

/**
 * Shows the capabilities of the yFiles {@link y.layout.router.BusRouter} and demonstrates specific <em>hub</em> nodes
 * to ease the usage in an interactive environment.
 * <p/>
 * Typically, in a bus, every member node is connected to every other member which results in a large number of edges in
 * the graph. To disburden users from entering all these edges manually, this application introduces a specific type of
 * nodes, so-called <em>hubs</em>, which act as interchange points of the bus. A bus consists of all its interconnected
 * hubs, and all edges and regular nodes connected to them. For convenience, all connectors and edges of the same bus
 * are drawn in a common color.
 * <p/>
 * Regular nodes, hubs and edges can be interactively added and deleted, and snap lines are provided to ease the
 * editing, see the related help page.
 * <p/>
 * There are a number of classes related for this demo:
 * <ul>
 *   <li>{@link BusDyer} governs the coloring of the buses</li>
 *   <li>{@link BusRouterDemoModule} is used as a means to configure the router. If grid is enabled, the router
 *   calculates grid routes and the view highlights the grid points.</li>
 *   <li>{@link BusRouterDemoTools} governs the left-side option panel</li>
 *   <li>{@link HubRoutingSupport} extends the BusRouter for graphs in hub representation</li>
 * </ul>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/orthogonal_bus_router.html">Section Orthogonal Bus-style Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class BusRouterDemo extends DemoBase {
  static final String HUB_CONFIGURATION = "BusHub";
  static final Object HUB_MARKER_DPKEY = "demo.layout.router.BusRouterDemo.HUB_MARKER_DPKEY";

  static final int MODE_ALL = 0;
  static final int MODE_SELECTED = 1;
  static final int MODE_PARTIAL = 2;

  private final BusDyer busDyer;
  private JComponent glassPane;
  private NodeRealizer hubRealizer;
  private HubRoutingSupport hubRoutingSupport;

  /**
   * Creates a new instance of this demo.
   */
  public BusRouterDemo() {
    this(null);
  }

  /**
   * Creates a new instance of this demo and adds a help pane for the specified file.
   */
  public BusRouterDemo(final String helpFilePath) {
    // create support for colored buses
    busDyer = createBusDyer();
    view.getGraph2D().addGraphListener(busDyer);

    hubRoutingSupport = createHubRoutingSupport();

    // add and prepare the tool pane
    BusRouterDemoTools demoTools = new BusRouterDemoTools();
    demoTools.setViewAndRouter(view, hubRoutingSupport.getModule());
    demoTools.updateGrid();
    demoTools.updateSnapping();

    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, demoTools.createOptionComponent(), view);
    mainSplitPane.setBorder(null);
    contentPane.add(mainSplitPane, BorderLayout.CENTER);

    addHelpPane(helpFilePath);

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        loadInitialGraph();
      }
    });
  }

  protected void loadInitialGraph() {
    loadGraph("resource/threeBuses.graphml");
  }

  protected boolean isClipboardEnabled() {
    return false;
  }

  /**
   * Does the bus routing by delegating to {@link HubRoutingSupport#doLayout(y.view.Graph2D, int)}.
   */
  public void doLayout(final int mode) {
    final Graph2D graph = view.getGraph2D();
    final NodeRealizer oldNodeRealizer = graph.getDefaultNodeRealizer();

    if (glassPane == null) {
      // creates a glass pane to lock the GUI during layout
      glassPane = new JPanel();
      final JRootPane rootPane = view.getRootPane();
      rootPane.setGlassPane(glassPane);
    }
    glassPane.setEnabled(true);

    graph.firePreEvent();
    try {
      // backup the graph state for undo
      graph.backupRealizers();

      // disable bridges during the layout animation and set the hub realizer for the new hub node
      setBridgeCalculatorEnabled(false);
      graph.setDefaultNodeRealizer(getHubRealizer());

      hubRoutingSupport.doLayout(graph, mode);
      busDyer.colorize(null);
    } finally {
      graph.setDefaultNodeRealizer(oldNodeRealizer);
      setBridgeCalculatorEnabled(true);

      glassPane.setEnabled(false);
      graph.updateViews();
      graph.firePostEvent();
    }
  }

  /**
   * Initialize this demo.
   */
  protected void initialize() {
    super.initialize();
    hubRealizer = createHubRealizer();

    setBridgeCalculatorEnabled(true);

    // create a data provider that specifies whether a node is a hub
    view.getGraph2D().addDataProvider(HUB_MARKER_DPKEY, new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        return dataHolder instanceof Node && isHub((Node) dataHolder);
      }
    });

    // create the data provider needed for the Orthogonal Mode
    view.getGraph2D().addDataProvider(EditMode.ORTHOGONAL_ROUTING_DPKEY,
        DataProviders.createConstantDataProvider(Boolean.TRUE));
  }

  /**
   * Registers the default actions, and replaces the delete and select all actions with this class's custom
   * implementations.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    // register keyboard actions
    ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      if (isDeletionEnabled()) {
        amap.put(Graph2DViewActions.DELETE_SELECTION, createDeleteSelectionAction());
      }

      // Prevents the selection of hubs by the select all action
      amap.put(Graph2DViewActions.SELECT_ALL, new Graph2DViewActions.SelectAllAction(view) {
        protected void setSelected(Graph2D graph, Node node, boolean flag) {
          if (!isHub(node)) {
            super.setSelected(graph, node, flag);
          }
        }
      });
    }
  }

  /**
   * Creates a delete action which deletes selected elements and remaining stub bus parts.
   */
  protected Action createDeleteSelectionAction() {
    final Action oldAction = super.createDeleteSelectionAction();
    final Action newAction = new HubDeleteSelectionAction();
    newAction.putValue(Action.SHORT_DESCRIPTION, oldAction.getValue(Action.SHORT_DESCRIPTION));
    newAction.putValue(Action.SMALL_ICON, oldAction.getValue(Action.SMALL_ICON));
    return newAction;
  }

  /**
   * Creates the default toolbar and adds the routing actions.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.setFloatable(false);

    toolBar.addSeparator();

    // add bus router to toolbar
    Action routeAllAction = new AbstractAction("Route All") {
      public void actionPerformed(ActionEvent e) {
        doLayout(MODE_ALL);
      }
    };
    routeAllAction.putValue(Action.SHORT_DESCRIPTION, "Route all buses");
    routeAllAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);
    toolBar.add(createActionControl(routeAllAction));

    // add bus router to toolbar
    Action routeSelectedAction = new AbstractAction("Route Selected") {
      public void actionPerformed(ActionEvent e) {
        doLayout(MODE_SELECTED);
      }
    };
    routeSelectedAction.putValue(Action.SHORT_DESCRIPTION, "Route selected buses");
    routeSelectedAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);
    toolBar.add(createActionControl(routeSelectedAction, true));

    // add settings for bus router to toolbar
    Action propertiesAction = new AbstractAction("Settings...") {
      public void actionPerformed(ActionEvent e) {
        OptionSupport.showDialog(hubRoutingSupport.getModule(), view.getGraph2D(), false, view.getFrame());
      }
    };
    propertiesAction.putValue(Action.SHORT_DESCRIPTION, "Configure the bus router");
    propertiesAction.putValue(Action.SMALL_ICON, getIconResource("resource/properties.png"));
    toolBar.add(createActionControl(propertiesAction));

    return toolBar;
  }

  /**
   * Creates the default menu bar and adds an additional menu of examples graphs.
   */
  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = super.createMenuBar();
    JMenu menu = new JMenu("Sample Graphs");
    menuBar.add(menu);

    menu.add(new EmptyGraphAction("Empty Graph"));

    menu.add(new AbstractAction("One Bus") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/oneBus.graphml");
      }
    });

    menu.add(new AbstractAction("Three Buses") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/threeBuses.graphml");
      }
    });

    return menuBar;
  }

  /**
   * Creates a modified edit mode for this demo.
   */
  protected EditMode createEditMode() {
    EditMode editMode = new HubEditMode();

    // copied from DemoBase
    if (editMode.getMovePortMode() instanceof MovePortMode) {
      ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
    }

    //allow moving view port with right drag gesture
    editMode.allowMovingWithPopup(true);
    return editMode;
  }

  /**
   * Creates the BusDyer used by this demo.
   */
  protected BusDyer createBusDyer() {
    return new BusDyer(view.getGraph2D());
  }

  /**
   * Creates the HubRoutingSupport used by this demo.
   */
  protected HubRoutingSupport createHubRoutingSupport() {
    return new HubRoutingSupport();
  }

  /**
   * Creates the default realizers but with thick edges which do not display arrow heads.
   */
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    EdgeRealizer er = view.getGraph2D().getDefaultEdgeRealizer();
    er.setTargetArrow(Arrow.NONE);
    view.getGraph2D().setDefaultEdgeRealizer(er);
  }

  /**
   * Call-back for loading a graph. Overwritten to reset the undo queue.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        doLayout(MODE_ALL);
      }
    });
  }

  /**
   * Specifies whether bridges are shown and configures a {@link y.view.BridgeCalculator} accordingly.
   */
  private void setBridgeCalculatorEnabled(boolean enable) {
    if (enable) {
      // create the BridgeCalculator
      BridgeCalculator bridgeCalculator = new BridgeCalculator();
      ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);
      bridgeCalculator.setCrossingMode(BridgeCalculator.CROSSING_MODE_ORDER_INDUCED);
      bridgeCalculator.setCrossingStyle(BridgeCalculator.CROSSING_STYLE_ARC);
      bridgeCalculator.setOrientationStyle(BridgeCalculator.ORIENTATION_STYLE_UP);
    } else {
      ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(null);
    }
  }

  NodeRealizer getHubRealizer() {
    return hubRealizer;
  }

  /**
   * Returns whether the specified node is a hub. A node is a hub if its realizer is the one of hubs.
   */
  static boolean isHub(final Node node) {
    final NodeRealizer realizer = ((Graph2D) node.getGraph()).getRealizer(node);
    return realizer instanceof GenericNodeRealizer
        && HUB_CONFIGURATION.equals(((GenericNodeRealizer) realizer).getConfiguration());
  }

  /**
   * Creates the realizer which is used for hubs.
   */
  static NodeRealizer createHubRealizer() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map map = GenericNodeRealizer.getFactory().createDefaultConfigurationMap();

    map.put(GenericNodeRealizer.Painter.class, new ShapeNodePainter(ShapeNodePainter.RECT));
    factory.addConfiguration(HUB_CONFIGURATION, map);

    NodeRealizer nr = new GenericNodeRealizer(HUB_CONFIGURATION);
    nr.setFillColor(Color.BLACK);
    nr.setLineColor(null);
    nr.setSize(5.0, 5.0);
    nr.removeLabel(nr.getLabel(0));
    return nr;
  }

  /**
   * A modified edit mode for this demo. After each node movement and node resizing, the connections of the affected
   * nodes to their buses are rerouted while the other parts remain fixed. If the source of an edge creation is a hub,
   * the edge is colored in the bus' color. If a new edge is a singleton bus, that is, it connects two regular nodes, it
   * is instantly routed.
   */
  protected class HubEditMode extends EditMode {
    protected HubEditMode() {
      setCreateEdgeMode(new AutoRoutingCreateEdgeMode(getHubRealizer()));
    }

    /**
     * After each node movement, the connections of the affected nodes to their buses are rerouted while the other parts
     * remain fixed.
     */
    protected ViewMode createMoveSelectionMode() {
      return new MoveSelectionMode() {
        protected void selectionMovedAction(double dx, double dy, double x, double y) {
          super.selectionMovedAction(dx, dy, x, y);
          doLayout(MODE_PARTIAL);
        }
      };
    }

    protected ViewMode createCreateEdgeMode() {
      return null;
    }

    protected ViewMode createMovePortMode() {
      return null;
    }

    /**
     * Does a partial layout after resizing of nodes.
     */
    protected ViewMode createHotSpotMode() {
      return new HotSpotMode() {
        private boolean dirty;

        public void mousePressedLeft(double x, double y) {
          dirty = false;
          super.mousePressedLeft(x, y);
        }

        public void mouseReleasedLeft(double x, double y) {
          super.mouseReleasedLeft(x, y);
          if (dirty) {
            doLayout(MODE_PARTIAL);
          }
          dirty = false;
        }

        protected void updateNodeRealizerBounds(NodeRealizer vr, double x, double y, double w, double h) {
          super.updateNodeRealizerBounds(vr, x, y, w, h);
          dirty = true;
        }
      };
    }

    /**
     * Prevents the selection of hubs by selection boxes.
     */
    protected ViewMode createSelectionBoxMode() {
      return new SelectionBoxMode() {
        protected void setSelected(Graph2D graph, Node n, boolean state) {
          if (!isHub(n)) {
            super.setSelected(graph, n, state);
          }
        }
      };
    }

    /**
     * Prevents the selection of hubs by left mouse clicks.
     */
    protected void setSelected(Graph2D graph, Node node, boolean state) {
      if (!isHub(node)) {
        super.setSelected(graph, node, state);
      }
    }

    /**
     * Selects on a click on a hub its edges.
     */
    protected void nodeClicked(Graph2D graph, Node node, boolean wasSelected, double x, double y,
                               boolean modifierSet) {
      if (isHub(node)) {
        if (!modifierSet) {
          graph.unselectAll();
        }

        if (!modifierSet || graph.isSelectionEmpty() || graph.selectedEdges().ok()) {
          for (EdgeCursor edgeCursor = node.edges(); edgeCursor.ok(); edgeCursor.next()) {
            final Edge edge = edgeCursor.edge();
            setSelected(graph, edge, true);
          }
        }
        graph.updateViews();
      } else {
        super.nodeClicked(graph, node, wasSelected, x, y, modifierSet);
      }
    }

    /**
     * Enables edge creation for modifier left click on an edge.
     */
    public void mouseDraggedLeft(double x, double y) {
      if (isModifierPressed(lastPressEvent)) {
        double px = translateX(lastPressEvent.getX());
        double py = translateY(lastPressEvent.getY());
        Edge edge = getHitInfo(px, py).getHitEdge();
        if (edge != null) {
          setChild(getCreateEdgeMode(), lastPressEvent, lastDragEvent);
          return;
        }
      }
      super.mouseDraggedLeft(x, y);
    }

  }

  /**
   * Routes each new edge automatically. If it connects two previously independent buses, the whole resulting bus is
   * routed anew. If it connects a new node to an existing bus, the existing bus is kept fixed and only the new edge is
   * routed. If it establishes a bus of its own, that is, it connects two regular nodes which are not associated with a
   * bus, it is routed as single-edge bus.
   * <p/>
   * Additionally, takes care to split a new edge which connects two regular nodes since this demo requires each bus
   * edge to connect to at least on hub.
   */
  protected class AutoRoutingCreateEdgeMode extends HubCreateEdgeMode {

    protected AutoRoutingCreateEdgeMode(NodeRealizer hubRealizer) {
      super(hubRealizer);
    }

    protected Edge createEdge(Graph2D graph, Node startNode, Node targetNode, EdgeRealizer realizer) {
      // fire event to mark start of edge creation for undo/redo
      graph.firePreEvent();
      return super.createEdge(graph, startNode, targetNode, realizer);
    }

    protected void edgeCreated(final Edge edge) {
      super.edgeCreated(edge);
      //noinspection IfStatementWithIdenticalBranches
      if (!isHub(edge.source()) && !isHub(edge.target())) {
        // both end nodes are regular nodes -> this is a single-edge bus
        final Edge edge2 = splitSingleBusEdge(edge);
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            getGraph2D().unselectAll();
            getGraph2D().setSelected(edge, true);
            getGraph2D().setSelected(edge2, true);
            doLayout(MODE_SELECTED);

            // fire event to mark start of edge creation for undo/redo
            getGraph2D().firePostEvent();
          }
        });
      } else if (isHub(edge.source()) && isHub(edge.target())) {
        // both end nodes are hubs -> route the complete bus since partial mode can be used for end-edges only
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            getGraph2D().unselectAll();
            getGraph2D().setSelected(edge, true);
            doLayout(MODE_SELECTED);

            // fire event to mark start of edge creation for undo/redo
            getGraph2D().firePostEvent();
          }
        });
      } else {
        // exactly one end node is a hub -> route the new edge and keep the existing bus fixed
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            getGraph2D().unselectAll();
            getGraph2D().setSelected(edge, true);
            doLayout(MODE_PARTIAL);

            // fire event to mark end of edge creation for undo/redo
            getGraph2D().firePostEvent();
          }
        });
      }
    }

    /**
     * In this demo, every bus edge should connect to at least one hub. Therefore, we split an edge which connect two
     * regular nodes into two parts by adding a new hub.
     *
     * @param edge the edge to split. This edge is changed to connect its source to the new hub.
     * @return the new edge which connects the new hub two the original edge's target.
     */
    protected Edge splitSingleBusEdge(Edge edge) {
      final Graph2D graph = getGraph2D();
      final Node oldSource = edge.source();
      final Node oldTarget = edge.target();

      graph.firePreEvent();
      try {
        // Note: the calculates a reasonable middle point only if the edge is straight-line;
        //       otherwise we have to split its path and distribute the bends correctly

        final Node hub = graph.createNode(getHubRealizer().createCopy());
        graph.setCenter(hub, YPoint.midPoint(graph.getSourcePointAbs(edge), graph.getTargetPointAbs(edge)));

        graph.changeEdge(edge, oldSource, hub);
        graph.setTargetPointRel(edge, YPoint.ORIGIN);

        final EdgeRealizer edgeRealizer = graph.getRealizer(edge);
        final EdgeRealizer edgeRealizer2 = edgeRealizer.createCopy();
        final Edge edge2 = graph.createEdge(hub, oldTarget, edgeRealizer2);
        graph.setSourcePointRel(edge2, YPoint.ORIGIN);

        final NodePort targetPort = NodePort.getTargetPort(edgeRealizer);
        if (isNodePortAware() && targetPort != null) {
          NodePort.bindTargetPort(targetPort, edgeRealizer2);
        }

        return edge2;
      } finally {
        graph.firePostEvent();
      }
    }
  }

  /**
   * Deletes selected graph elements and, additionally, all remaining bus stubs. A bus stub is a part of a bus which is
   * connected to only one regular node after the deletion of the selected elements.
   */
  protected class HubDeleteSelectionAction extends Graph2DViewActions.DeleteSelectionAction {

    protected HubDeleteSelectionAction() {
      super(BusRouterDemo.this.view);
    }

    public void delete(Graph2DView view) {
      final Graph2D graph = view.getGraph2D();
      try {
        graph.firePreEvent();
        deleteImpl(view);
      } finally {
        graph.firePostEvent();
      }
    }

    /**
     * Does the deletion. First, deletes the selected elements. Then, deletes iteratively each neighbor of any deleted
     * edge if it is a hub and has degree lesser than 2. These are exactly the hubs of bus stubs.
     */
    private void deleteImpl(Graph2DView view) {
      final NodeList hubsToDelete = new NodeList();

      GraphListener listener = new GraphListener() {
        public void onGraphEvent(GraphEvent e) {
          if (e.getType() == GraphEvent.PRE_EDGE_REMOVAL) {
            final Edge edge = (Edge) e.getData();
            if (isHubToDelete(edge.source())) {
              hubsToDelete.add(edge.source());
            }
            if (isHubToDelete(edge.target())) {
              hubsToDelete.add(edge.target());
            }
          }
        }

        private boolean isHubToDelete(Node node) {
          // edge not deleted yet, therefore test for degree < 3
          return isHub(node) && node.degree() < 3;
        }
      };

      final Graph2D graph = view.getGraph2D();
      try {
        graph.addGraphListener(listener);
        super.delete(view);

        while (!hubsToDelete.isEmpty()) {
          final Node node = hubsToDelete.popNode();
          if (node.getGraph() != null) {
            graph.removeNode(node);
          }
        }
      } finally {
        graph.removeGraphListener(listener);
      }
    }
  }

  /**
   * Clears the graph and its undo queue, and sets the view to the initial zoom factor.
   */
  protected class EmptyGraphAction extends AbstractAction {

    protected EmptyGraphAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().clear();
      view.getGraph2D().setURL(null);
      view.fitContent();
      view.updateView();
      getUndoManager().resetQueue();
    }
  }

  /**
   * Runs this demo.
   *
   * @param args unused
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new BusRouterDemo("resource/busrouterhelp.html").start("Bus Router Demo");
      }
    });
  }
}
