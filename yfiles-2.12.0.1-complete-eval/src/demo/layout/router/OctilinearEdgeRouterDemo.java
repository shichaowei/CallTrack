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
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.base.YList;
import y.geom.YPoint;
import y.layout.LayoutGraph;
import y.layout.PortConstraintKeys;
import y.layout.router.polyline.EdgeRouter;
import y.option.Editor;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.TableEditorFactory;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.util.Tuple;
import y.util.pq.BHeapIntNodePQ;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BridgeCalculator;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.MovePortMode;
import y.view.PopupMode;
import y.view.Port;
import y.view.PortAssignmentMoveSelectionMode;
import y.view.Selections;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This demo shows yFiles' octilinear edge routing capabilities. An edge routing algorithm routes edges without changing
 * the current node positions. While an orthogonal edge routing algorithm only produces horizontal and vertical edge
 * segments, this router also allows octilinear edge segments, i.e., it produces edge routes where the slope of each
 * segment is a multiple of 45 degrees. Besides the basic octilinear edge routing capabilities, this class also
 * demonstrates the edge grouping feature.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/polyline_edge_router.html">Section Polyline Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class OctilinearEdgeRouterDemo extends DemoBase {
  //mode constants that specify which edges should be routed
  protected static final byte MODE_ROUTE_ALL_EDGES = 0;
  protected static final byte MODE_ROUTE_SELECTED_EDGES = 1;
  protected static final byte MODE_ROUTE_EDGES_OF_SELECTED_NODES = 2;

  private OptionHandler optionHandler;
  private boolean automaticRoutingEnabled; //if this option is enabled, the edge routing is automatically triggered when creating new edges, changing edges or moving/resizing nodes

  private EdgeMap sourceGroupID;
  private EdgeMap targetGroupID;
  private Color[] groupColors;
  private YList availableColors;
  private HashMap groupId2Color;

  /** Creates a new instance of this demo. */
  public OctilinearEdgeRouterDemo() {
    this(null);
  }

  /** Creates a new instance of this demo and adds a help pane for the specified file. */
  public OctilinearEdgeRouterDemo(final String helpFilePath) {
    this.automaticRoutingEnabled = true;
    this.groupId2Color = new HashMap();
    sourceGroupID = Maps.createHashedEdgeMap();
    targetGroupID = Maps.createHashedEdgeMap();
    this.availableColors = new YList();
    resetColors(10);

    //Init GUI components:
    final JPanel propertiesPanel = new JPanel(new BorderLayout());
    optionHandler = createOptionHandler();
    propertiesPanel.add(createOptionTable(optionHandler), BorderLayout.NORTH);

    if (helpFilePath != null) {
      final URL url = getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        final JComponent helpPane = createHelpPane(url);
        if (helpPane != null) {
          propertiesPanel.add(helpPane);
        }
      }
    }

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, propertiesPanel, view);
    splitPane.setBorder(null);
    splitPane.setResizeWeight(0.05);
    splitPane.setContinuousLayout(false);
    contentPane.add(splitPane, BorderLayout.CENTER);

    // show bridges
    BridgeCalculator bridgeCalculator = new BridgeCalculator();
    bridgeCalculator.setCrossingMode(BridgeCalculator.CROSSING_MODE_HORIZONTAL_CROSSES_VERTICAL);
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);

    //load initial graph
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        loadInitialGraph();
      }
    });
  }

  private void resetColors(int colorCount) {
    groupId2Color.clear();
    groupColors = BusDyer.Colors.getColors(colorCount);
    availableColors.clear();
    availableColors.addAll(Arrays.asList(groupColors));
    availableColors.remove(Color.BLACK);
  }

  /** Releases colors that are not longer required */
  private void releaseUnusedColors() {
    final Graph2D graph = view.getGraph2D();
    availableColors.addAll(Arrays.asList(groupColors));
    availableColors.remove(Color.BLACK);
    for (EdgeCursor cur = graph.edges(); cur.ok() && !availableColors.isEmpty(); cur.next()) {
      final Edge edge = cur.edge();
      final Object edgeGroupID = (sourceGroupID.get(edge) != null) ? sourceGroupID.get(edge) : targetGroupID.get(edge);
      final Color edgeGroupColor = (Color) groupId2Color.get(edgeGroupID);
      if (availableColors.contains(edgeGroupColor)) {
        availableColors.remove(edgeGroupColor);
      }
    }
  }

  /** Assign colors to edge groups, i.e., each edge group has a unique color. Non-grouped edges are drawn black */
  private void colorizeGroups(final EdgeCursor ec) {
    final Graph2D graph = view.getGraph2D();
    for(; ec.ok(); ec.next()) {
      final Edge e = ec.edge();
      final EdgeRealizer eRealizer = graph.getRealizer(e);
      final Object eGroupID = (sourceGroupID.get(e) != null) ? sourceGroupID.get(e) : targetGroupID.get(e);
      if(eGroupID != null) {
        Color groupColor = (Color) groupId2Color.get(eGroupID);
        if(groupColor == null) {
          if (availableColors.isEmpty()) {
            releaseUnusedColors();
            if (availableColors.isEmpty()) {
              //still empty => not enough colors => we first add new colors and then re-assign the edge group colors
              resetColors(groupColors.length * 2);
              colorizeGroups(graph.edges());
              return;
            }
          }
          groupColor = (Color) availableColors.pop();
          groupId2Color.put(eGroupID, groupColor);
        }
        eRealizer.setLineColor(groupColor);
      } else {
        eRealizer.setLineColor(Color.BLACK);
      }
    }
    view.updateView();
  }

  /** Creates a table editor component for the specified option handler. */
  private JComponent createOptionTable(OptionHandler oh) {
    oh.setAttribute(TableEditorFactory.ATTRIBUTE_INFO_POSITION, TableEditorFactory.InfoPosition.NONE);

    TableEditorFactory tef = new TableEditorFactory();
    Editor editor = tef.createEditor(oh);

    JComponent optionComponent = editor.getComponent();
    optionComponent.setPreferredSize(new Dimension(330, 150));
    optionComponent.setMaximumSize(new Dimension(330, 150));
    return optionComponent;
  }

  /** Creates an option handler. */
  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    layoutOptionHandler.useSection("Edge Routing");
    final OptionItem automaticRoutingItem = layoutOptionHandler.addBool("Automatic Routing", true);
    automaticRoutingItem.addPropertyChangeListener("value", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        automaticRoutingEnabled = layoutOptionHandler.getBool("Automatic Routing");
      }
    });
    layoutOptionHandler.addBool("Octilinear Routing", true);
    layoutOptionHandler.addInt("Preferred Octilinear Segment Length", 30);

    layoutOptionHandler.useSection("Edge Grouping");
    layoutOptionHandler.addBool("Ignore Edge Groups", false);

    layoutOptionHandler.useSection("Minimum Distances");
    layoutOptionHandler.addInt("Minimum Edge Distance", 3);
    layoutOptionHandler.addInt("Minimum Node Distance", 5);

    return layoutOptionHandler;
  }

  protected void loadInitialGraph() {
    loadGraph("resource/octilinearEdgeRouting.graphml");
  }

  /** Does the edge routing. */
  protected void routeEdges() {
    routeEdges(MODE_ROUTE_ALL_EDGES, null);
  }

  /**
   * Does the edge routing.
   *
   * @param mode             specifies which edges should be routed. Possible values are {@link #MODE_ROUTE_ALL_EDGES},
   *                         {@link #MODE_ROUTE_EDGES_OF_SELECTED_NODES} and {@link #MODE_ROUTE_SELECTED_EDGES}.
   * @param selectedElements a DataProvider that returns true for each selected element.
   */
  protected void routeEdges(final byte mode, final DataProvider selectedElements) {
    final Graph2D graph = view.getGraph2D();

    //configure the edge router
    final EdgeRouter edgeRouter = new EdgeRouter();
    edgeRouter.setReroutingEnabled(false);
    edgeRouter.setPolylineRoutingEnabled(optionHandler.getBool("Octilinear Routing"));
    edgeRouter.setPreferredPolylineSegmentLength(optionHandler.getInt("Preferred Octilinear Segment Length"));
    edgeRouter.getDefaultEdgeLayoutDescriptor().setMinimalEdgeToEdgeDistance(optionHandler.getInt("Minimum Edge Distance"));
    edgeRouter.setMinimalNodeToEdgeDistance(optionHandler.getInt("Minimum Node Distance"));

    //if edge groups should be ignored, we temporarily remove the corresponding data providers
    final boolean ignoreEdgeGroups = optionHandler.getBool("Ignore Edge Groups");
    if (!ignoreEdgeGroups) {
      graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sourceGroupID);
      graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, targetGroupID);
    }

    //configure edge router, if only a subset of edges should be routed
    final Object selectionEdgesKey = edgeRouter.getSelectedEdgesDpKey();
    if (mode == MODE_ROUTE_SELECTED_EDGES) {
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      final DataProvider selectedElementsDP = addFixedGroupMembersToSelection(selectedElements);
      graph.addDataProvider(selectionEdgesKey, selectedElementsDP);
    } else if (mode == MODE_ROUTE_EDGES_OF_SELECTED_NODES) {
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      final DataProvider selectedElementsDP = addFixedGroupMembersToSelection(selectedElements);
      graph.addDataProvider(selectionEdgesKey, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return selectedElementsDP.getBool((((Edge) dataHolder).source()))
              || selectedElementsDP.getBool(((Edge) dataHolder).target());
        }
      });
    }

    //do the layout
    final Cursor oldCursor = view.getCanvasComponent().getCursor();
    try {
      contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      view.applyLayoutAnimated(edgeRouter);
    } finally {
      contentPane.setCursor(oldCursor);
    }

    if (mode == MODE_ROUTE_SELECTED_EDGES || mode == MODE_ROUTE_EDGES_OF_SELECTED_NODES) {
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_ALL_EDGES);
      graph.removeDataProvider(selectionEdgesKey);
    }

    //restore the original data providers
    if (!ignoreEdgeGroups) {
      graph.removeDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
      graph.removeDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY);
    }
  }

  /** Adds all edges to the selected elements that are are assigned to the same edge group as a selected element */
  private DataProvider addFixedGroupMembersToSelection(final DataProvider selectedElements) {
    if(selectedElements == null) {
      return null;
    }

    final List selectedEdges = new ArrayList();
    final List fixedEdges = new ArrayList();
    final Set groupIDs = new HashSet();
    final Graph2D graph = view.getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      if (selectedElements.getBool(edge)) {
        selectedEdges.add(edge);
        if (sourceGroupID.get(edge) != null) {
          groupIDs.add(sourceGroupID.get(edge));
        } else if (targetGroupID.get(edge) != null) {
          groupIDs.add(targetGroupID.get(edge));
        }
      } else {
        fixedEdges.add(edge);
      }
    }

    final int numberOfSelectedEdges = selectedEdges.size();
    for (Iterator it = fixedEdges.iterator(); it.hasNext(); ) {
      final Edge fixedEdge = (Edge) it.next();
      if (sourceGroupID.get(fixedEdge) != null && groupIDs.contains(sourceGroupID.get(fixedEdge))) {
        selectedEdges.add(fixedEdge);
      } else if (targetGroupID.get(fixedEdge) != null && groupIDs.contains(targetGroupID.get(fixedEdge))) {
        selectedEdges.add(fixedEdge);
      }
    }

    if (selectedEdges.size() > numberOfSelectedEdges) {
      final EdgeMap selectedElementsDP = Maps.createHashedEdgeMap();
      for (Iterator iterator = selectedEdges.iterator(); iterator.hasNext(); ) {
        final Edge selectedEdge = (Edge) iterator.next();
        selectedElementsDP.set(selectedEdge, Boolean.TRUE);
      }
      return selectedElementsDP;
    }

    return selectedElements;
  }

  /**
   * Adds a specially configured EditMode that will automatically route all newly created edges orthogonally. The edge
   * router will also be activated on some edges, when nodes get resized or a node selection gets moved.
   */
  protected void registerViewModes() {
    final EditMode mode = createEditMode();
    mode.allowBendCreation(false);
    mode.setMoveSelectionMode(new MyMoveSelectionMode());
    mode.setCreateEdgeMode(new MyCreateEdgeMode());
    mode.setHotSpotMode(new MyHotSpotMode());
    mode.setPopupMode(new MyPopupMode());
    mode.setMovePortMode(new MyMovePortMode());
    view.addViewMode(mode);
  }

  /** Provides popup menus for all kinds of actions */
  final class MyPopupMode extends PopupMode {
    public JPopupMenu getNodePopup(final Node v) {
      JPopupMenu pm = new JPopupMenu();
      addNodeActions(pm, new NodeList(v).nodes());
      return pm;
    }

    private void addNodeActions(JPopupMenu pm, NodeCursor nc) {
      pm.add(new GroupEdgesOnNodeAction("Group In-Edges", nc, true));
      pm.add(new UngroupEdgesOnNodeAction("Ungroup In-Edges", nc, true));
      pm.addSeparator();
      pm.add(new GroupEdgesOnNodeAction("Group Out-Edges", nc, false));
      pm.add(new UngroupEdgesOnNodeAction("Ungroup Out-Edges", nc, false));
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      final JPopupMenu pm = new JPopupMenu();
      final NodeCursor snc = getGraph2D().selectedNodes();
      if (snc.ok()) {
        addNodeActions(pm, snc);
      } else {
        final EdgeCursor sec = getGraph2D().selectedEdges();
        if (sec.ok()) {
          addEdgeActions(pm, sec);
        } else {
          return null;
        }
      }
      return pm;
    }

    public JPopupMenu getEdgePopup(Edge e) {
      final JPopupMenu pm = new JPopupMenu();
      pm.add(new UngroupSelectedEdgesAction("Ungroup Selected Edges", new EdgeList(e).edges()));
      return pm;
    }

    private void addEdgeActions(JPopupMenu pm, EdgeCursor sec) {
      pm.add(new GroupSelectedEdgesAction("Group Selected Edges", sec));
      pm.add(new UngroupSelectedEdgesAction("Ungroup Selected Edges", sec));
    }
  }

  /** Provides an action to ungroup all selected edges. */
  final class UngroupSelectedEdgesAction extends AbstractAction {
      private EdgeCursor sec;

      public UngroupSelectedEdgesAction(String name, EdgeCursor sec) {
        super(name);
        this.sec = sec;
      }

      public void actionPerformed(ActionEvent ae) {
        if(sec == null || !sec.ok()) {
          return;
        }

        final EdgeMap edge2IsUngrouped = Maps.createHashedEdgeMap();
        final EdgeList ungroupedEdges = new EdgeList();
        for(; sec.ok(); sec.next()) {
          final Edge e = sec.edge();
          ungroupedEdges.add(e);
          edge2IsUngrouped.setBool(e, true);
          sourceGroupID.set(e, null);
          targetGroupID.set(e, null);
        }
        colorizeGroups(ungroupedEdges.edges());
        routeEdges(MODE_ROUTE_SELECTED_EDGES, edge2IsUngrouped);
      }
  }

  /** Provides an action to group all selected edges. */
  final class GroupSelectedEdgesAction extends AbstractAction {
    private EdgeCursor sec;

    public GroupSelectedEdgesAction(String name, EdgeCursor sec) {
      super(name);
      this.sec = sec;
    }

    public void actionPerformed(ActionEvent ae) {
      if(sec == null || !sec.ok()) {
        return;
      }

      //we construct the subgraph that contains all selected edges and split each original node into one node
      //for incoming edges and one node for outgoing edges
      final Graph2D origGraph = view.getGraph2D();
      final Graph subgraph = new Graph();
      final NodeMap origNode2SubgraphIncomingNode = Maps.createHashedNodeMap();
      final NodeMap origNode2SubgraphOutgoingNode = Maps.createHashedNodeMap();
      for (NodeCursor nc = origGraph.nodes(); nc.ok(); nc.next()) {
        final Node origNode = nc.node();

        //create the two nodes of the subgraph (one for incoming and one for outgoing edges)
        final Node subgraphIncomingNode = subgraph.createNode();
        origNode2SubgraphIncomingNode.set(origNode, subgraphIncomingNode);
        final Node subgraphOutgoingNode = subgraph.createNode();
        origNode2SubgraphOutgoingNode.set(origNode, subgraphOutgoingNode);
      }
      final EdgeMap subgraphEdge2OrigEdge = Maps.createHashedEdgeMap();
      for(; sec.ok(); sec.next()) {
        final Edge origEdge = sec.edge();

        //insert edge into the subgraph
        final Node subgraphSource = (Node) origNode2SubgraphOutgoingNode.get(origEdge.source());
        final Node subgraphTarget = (Node) origNode2SubgraphIncomingNode.get(origEdge.target());
        final Edge copy = subgraph.createEdge(subgraphSource, subgraphTarget);
        subgraphEdge2OrigEdge.set(copy, origEdge);
      }

      final int MAX_DEGREE = origGraph.edgeCount();
      final BHeapIntNodePQ pq = new BHeapIntNodePQ(subgraph);
      for (NodeCursor nc = subgraph.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        pq.add(node, MAX_DEGREE - node.degree()); //nodes with high degree should be processed first
      }

      //we iteratively take the node with highest degree and assign group ids to its edges
      final EdgeList groupedEdges = new EdgeList();
      final EdgeMap edge2IsGrouped = Maps.createHashedEdgeMap();
      while(!pq.isEmpty()) {
        final Node node = pq.removeMin();
        final boolean incomingNode = node.inDegree() > 0;
        final Object groupId = new Tuple(node, node.edges().edge());
        for(EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
          final Edge subgraphEdge = ec.edge();

          //assign group id to original edge
          final Edge origEdge = (Edge) subgraphEdge2OrigEdge.get(subgraphEdge);
          groupedEdges.add(origEdge);
          edge2IsGrouped.setBool(origEdge, true);
          sourceGroupID.set(origEdge, incomingNode ? null : groupId);
          targetGroupID.set(origEdge, incomingNode ? groupId : null);

          //remove the edge from the subgraph and decrease the degree of the neighbor by one (i.e., increase the priority by one)
          final Node neighbor = subgraphEdge.opposite(node);
          subgraph.removeEdge(subgraphEdge);
          pq.increasePriority(neighbor, pq.getPriority(neighbor) + 1);
        }
      }
      colorizeGroups(groupedEdges.edges());
      routeEdges(MODE_ROUTE_SELECTED_EDGES, edge2IsGrouped);
    }
  }

  /** Provides an action to ungroup all in-/out-edges of a node. */
  final class UngroupEdgesOnNodeAction extends AbstractAction {
    private NodeCursor nc;
    private boolean incomingEdges;

    public UngroupEdgesOnNodeAction(String name, NodeCursor nc, boolean incomingEdges) {
      super(name);
      this.nc = nc;
      this.incomingEdges = incomingEdges;
    }

    public void actionPerformed(ActionEvent ae) {
      final EdgeList ungroupedEdges = new EdgeList();
      final EdgeMap edge2IsUngrouped = Maps.createHashedEdgeMap();
      for (; nc.ok(); nc.next()) {
        final Node node = nc.node();
        if(incomingEdges) {
          for(EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
            final Edge e = ec.edge();
            if(targetGroupID.get(e) != null) {
              targetGroupID.set(e, null);
              ungroupedEdges.add(e);
              edge2IsUngrouped.setBool(e, true);
            }
          }
        } else {
          for(EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            final Edge e = ec.edge();
            if(sourceGroupID.get(e) != null) {
              sourceGroupID.set(e, null);
              ungroupedEdges.add(e);
              edge2IsUngrouped.setBool(e, true);
            }
          }
        }
      }
      colorizeGroups(ungroupedEdges.edges());
      routeEdges(MODE_ROUTE_SELECTED_EDGES, edge2IsUngrouped);
    }
  }

  /** Provides an action to group all in-/out-edges of a node. */
  final class GroupEdgesOnNodeAction extends AbstractAction {
    private NodeCursor nc;
    private boolean inEdges;

    public GroupEdgesOnNodeAction(String name, NodeCursor nc, boolean inEdges) {
      super(name);
      this.nc = nc;
      this.inEdges = inEdges;
    }

    public void actionPerformed(ActionEvent ae) {
      final EdgeList groupedEdges = new EdgeList();
      final EdgeMap edge2IsGrouped = Maps.createHashedEdgeMap();
      for (; nc.ok(); nc.next()) {
        final Node node = nc.node();
        
        //group all in-edges (out-edges) of the given node
        final EdgeCursor groupedEdgesCur = inEdges ? node.inEdges() : node.outEdges();
        if (groupedEdgesCur.ok()) {
          final Edge firstEdge = groupedEdgesCur.edge();
          final Object groupId = new Tuple(inEdges ? firstEdge.target() : firstEdge.source(), firstEdge);
          for (; groupedEdgesCur.ok(); groupedEdgesCur.next()) {
            final Edge e = groupedEdgesCur.edge();
            groupedEdges.add(e);
            edge2IsGrouped.setBool(e, true);
            targetGroupID.set(e, inEdges ? groupId : null);
            sourceGroupID.set(e, inEdges ? null : groupId);
          }
        }
      }
      colorizeGroups(groupedEdges.edges());
      routeEdges(MODE_ROUTE_SELECTED_EDGES, edge2IsGrouped);
    }
  }

  /** A special mode for creating edges. */
  class MyCreateEdgeMode extends CreateEdgeMode {
    MyCreateEdgeMode() {
      super();
      allowSelfloopCreation(false);
    }

    protected Edge createEdge(Graph2D graph, Node startNode, Node targetNode, EdgeRealizer realizer) {
      graph.firePreEvent();
      return super.createEdge(graph, startNode, targetNode, realizer);
    }

    protected void edgeCreated(final Edge e) {
      super.edgeCreated(e);

      if (automaticRoutingEnabled) {
        //trigger layout
        final DataProvider activeEdges = new DataProviderAdapter() {
          public boolean getBool(Object o) {
            return e == o;
          }
        };
        routeEdges(MODE_ROUTE_SELECTED_EDGES, activeEdges);
      }

      view.getGraph2D().firePostEvent();
    }
  }

  /** A special mode for moving edge ports. */
  class MyMovePortMode extends MovePortMode {
    MyMovePortMode() {
      setChangeEdgeEnabled(true);
      setIndicatingTargetNode(true);
    }

    protected void portMoved(Port port, double x, double y) {
      super.portMoved(port, x, y);

      if (automaticRoutingEnabled) {
        //trigger layout
        final Edge edge = port.getOwner().getEdge();
        final DataProvider activeEdges = new DataProviderAdapter() {
          public boolean getBool(Object o) {
            return edge == o;
          }
        };
        routeEdges(MODE_ROUTE_SELECTED_EDGES, activeEdges);
      }
    }
  }

  /** A special mode for moving a selection of the graph. */
  class MyMoveSelectionMode extends PortAssignmentMoveSelectionMode {
    MyMoveSelectionMode() {
      super(null, null);
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      super.selectionMovedAction(dx, dy, x, y);

      final Graph2D graph = view.getGraph2D();
      if (automaticRoutingEnabled && (graph.selectedNodes().ok() || graph.selectedBends().ok())) {
        //trigger layout
        final EdgeMap selectedEdges = Maps.createHashedEdgeMap();
        for (BendCursor bc = graph.selectedBends(); bc.ok(); bc.next()) {
          final Bend bend = bc.bend();
          selectedEdges.setBool(bend.getEdge(), true);
        }
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (graph.isSelected(edge.source()) ^ graph.isSelected(edge.target())) {
            selectedEdges.setBool(edge, true);
          }
        }
        routeEdges(MODE_ROUTE_SELECTED_EDGES, selectedEdges);
      }
    }
  }

  /** A special mode for resizing nodes. */
  class MyHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);

      if (automaticRoutingEnabled) {
        //trigger layout
        final DataProvider selectedNodesDP = Selections.createSelectionDataProvider(view.getGraph2D());
        routeEdges(MODE_ROUTE_EDGES_OF_SELECTED_NODES, selectedNodesDP);
      }
    }
  }

  /** Creates the default toolbar and adds the routing actions. */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.setFloatable(false);

    toolBar.addSeparator();

    // add edge router to toolbar
    Action routeAllAction = new AbstractAction("Route") {
      public void actionPerformed(ActionEvent e) {
        routeEdges();
      }
    };
    routeAllAction.putValue(Action.SHORT_DESCRIPTION, "Route all edges");
    routeAllAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);
    toolBar.add(createActionControl(routeAllAction));

    return toolBar;
  }

  /** Call-back for loading a graph. Overwritten to reset the undo queue. */
  protected void loadGraph(URL resource) {
    sourceGroupID = Maps.createHashedEdgeMap();
    targetGroupID = Maps.createHashedEdgeMap();
    super.loadGraph(resource);
    defineGroupsFromSketch(view.getGraph2D());
  }

  /** Defines source/target groups for edges with same source/target port */
  private void defineGroupsFromSketch(final LayoutGraph graph) {
    //fill providers
    final Map port2EdgeMap = new HashMap();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      port2EdgeMap.clear();
      for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
        final Edge e = ec.edge();
        final YPoint p = graph.getSourcePointRel(e);
        if (!p.equals(YPoint.ORIGIN)) {
          final Edge prevEdge = (Edge) port2EdgeMap.get(p);
          if (prevEdge != null) {
            final Object groupId = new Tuple(node, p);
            sourceGroupID.set(prevEdge, groupId);
            sourceGroupID.set(e, groupId);
          }
          port2EdgeMap.put(p, e);
        }
      }
      port2EdgeMap.clear();
      for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
        final Edge e = ec.edge();
        final YPoint p = graph.getTargetPointRel(e);
        if (!p.equals(YPoint.ORIGIN)) {
          final Edge prevEdge = (Edge) port2EdgeMap.get(p);
          if (prevEdge != null) {
            final Object groupId = new Tuple(node, p);
            targetGroupID.set(prevEdge, groupId);
            targetGroupID.set(e, groupId);
          }
          port2EdgeMap.put(p, e);
        }
      }
    }
    colorizeGroups(graph.edges());
  }

  /** Runs this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new OctilinearEdgeRouterDemo("resource/octilinearedgerouterhelp.html").start("Octilinear Edge Router Demo");
      }
    });
  }
}
