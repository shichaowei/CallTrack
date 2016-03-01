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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import demo.view.DemoBase;

import y.algo.Bfs;
import y.anim.AnimationEvent;
import y.anim.AnimationFactory;
import y.anim.AnimationListener;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.io.IOHandler;
import y.io.ZipGraphMLIOHandler;
import y.layout.LayoutTool;
import y.util.Comparators;
import y.util.D;
import y.util.DataProviderAdapter;
import y.util.DefaultMutableValue2D;
import y.util.GraphCopier;
import y.util.Maps;
import y.view.DefaultGraph2DRenderer;
import y.view.DropSupport;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DCopyFactory;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Graph2DTraversal;
import y.view.Graph2DUndoManager;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.ModelViewManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.hierarchy.AutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Demonstrates how to successively explore large graphs.
 * <p>
 * Things to try:
 * </p>
 * <ul>
 * <li>Double-click a node in the view to show connected nodes/edges.</li>
 * <li>Double-click an item in the tree to add the corresponding node to the
 * view.</li>
 * <li>Move a node around, then double-click the node while holding
 * <code>CTRL</code> to reposition its neighbors.</li>
 * </ul>
 *
 */
public class GraphExplorerDemo extends DemoBase {
  private static final int DURATION_ADD = 500;

  private final Graph2D baseModel;
  private final ModelViewManager manager;
  private final InclusionFilter inclusionFilter;
  private final GraphExplorerOptionHandler oh;
  private final Graph2DUndoManager undoManager;


  public GraphExplorerDemo() {
    this(null);
  }

  public GraphExplorerDemo( final String helpFilePath ) {
    baseModel = view.getGraph2D();
    new HierarchyManager(baseModel);

    manager = ModelViewManager.getInstance(baseModel);
    manager.setInnerGraphBindingAllowed(true);
    inclusionFilter = new InclusionFilter();


    final SearchableTreeViewPanel baseModelView =
            new SearchableTreeViewPanel(baseModel);
    final JTree jt = baseModelView.getTree();
    //add a navigational action to the tree
    jt.addMouseListener(new MyDoubleClickListener());
    jt.setCellRenderer(new MyTreeCellRenderer());


    view.setPreferredSize(new Dimension(480, 640));
    view.setGraph2D((Graph2D) manager.createViewGraph(new MyGraphCopyFactory(), inclusionFilter, false, false));
    view.setFitContentOnResize(true);
    view.setGraph2DRenderer(new MyGraph2DRenderer());
    new Graph2DViewMouseWheelZoomListener().addToCanvas(view);
    view.getGraph2D().addGraph2DSelectionListener(new SelectionTrigger(baseModel));
    view.getGraph2D().addDataProvider(
            EditMode.ORTHOGONAL_ROUTING_DPKEY,
            new DataProviderAdapter() {
              public boolean getBool( Object edge ) {
                final byte id = oh.getLayoutId();
                return id == GraphExplorerOptionHandler.ID_LAYOUT_HIERARCHIC ||
                       id == GraphExplorerOptionHandler.ID_LAYOUT_ORTHOGONAL;
              }
            });

    //add actions to view
    final Graph2DViewActions actions = new Graph2DViewActions(view);
    final ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.DELETE_SELECTION, new MyDeleteSelectionAction());
    final InputMap inputMap = actions.createDefaultInputMap(actionMap);
    view.getCanvasComponent().setActionMap(actionMap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, inputMap);

    //create and set customized edit mode
    final EditMode filteredViewMode = new EditMode() {
      public void mouseClicked(final double x, final double y) {
        if (SwingUtilities.isLeftMouseButton(lastClickEvent) &&
            lastClickEvent.getClickCount() == 2) {
          final HitInfo info = view.getHitInfoFactory().createHitInfo(x, y,
              Graph2DTraversal.NODES | Graph2DTraversal.EDGES, true);
          if (info.hasHitNodes()) {
            final Node modelNode = manager.getModelNode(info.getHitNode());
            if (baseModel.getHierarchyManager() != null && !baseModel.getHierarchyManager().isGroupNode(modelNode)) {
              final NodeList selectedNodes = new NodeList();
              final Graph2D graph = view.getGraph2D();
              for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
                selectedNodes.add(manager.getModelNode(nc.node()));
              }

              graph.firePreEvent();
              try {
                handleClick(selectedNodes, modelNode, !lastClickEvent.isControlDown());
              } finally {
                graph.firePostEvent();
              }

              baseModelView.repaint();
            }
          }
        }
      }
    };    
    filteredViewMode.setOrthogonalEdgeRouting(true);
    filteredViewMode.getMouseInputMode().setNodeSearchingEnabled(true);
    filteredViewMode.allowEdgeCreation(false);
    filteredViewMode.allowNodeCreation(false);
    view.addViewMode(filteredViewMode);

    final JToolBar jtb = getToolBar();
    if (jtb == null) {
      undoManager = null;
    } else {
      undoManager = new Graph2DUndoManager(view.getGraph2D());
      undoManager.setViewContainer(view);
      jtb.addSeparator();
      jtb.add(prepare(undoManager.getUndoAction(), "Undo", "resource/undo.png"));
      jtb.add(prepare(undoManager.getRedoAction(), "Redo", "resource/redo.png"));
    }


    //create help pane
    JComponent helpPane = null;
    if (helpFilePath != null) {
      final URL url = getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        helpPane = createHelpPane(url);
      }
    }

    //create panels
    final JPanel rightPanel = new JPanel(new BorderLayout());

    oh = new GraphExplorerOptionHandler();
    oh.getItem("General", "Layout").setAttribute(
            GraphExplorerOptionHandler.ATTRIBUTE_LAYOUT_CALLBACK,
            new ActionListener() {
              public void actionPerformed( final ActionEvent e ) {
                final NodeCursor nc = view.getGraph2D().selectedNodes();
                final Node node = nc.ok() ? nc.node() : null;
                doLayout(new LayoutContext(view, true, false, node, baseModel.getHierarchyManager().containsGroups()),
                    oh.getLayoutId());
              }
            });
    rightPanel.add(oh.createEditorComponent(), BorderLayout.NORTH);

    if (helpPane != null) {
      rightPanel.add(helpPane, BorderLayout.CENTER);
    }

    final JSplitPane splitPane = newSplitPane(view, rightPanel);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setResizeWeight(1);
    splitPane.setContinuousLayout(false);
    contentPane.add(newSplitPane(baseModelView, splitPane), BorderLayout.CENTER);

    //begin setup drag and drop support
    //nodes can be added to the filtered graph by dragging JTree nodes to the filtered view
    final DropSupport dropSupport = new DropSupport(view) {
      private Node createNodeImpl(
              final Graph2DView view,
              final NodeRealizer r,
              final double x,
              final double y
      ) {
        final Node modelNode = r.getNode();
        final HierarchyManager hierarchy = baseModel.getHierarchyManager();
        for (Node n = modelNode; n != null; n = hierarchy.getParentNode(n)) {
          inclusionFilter.includeNode(n);
        }

        final Graph2D filteredGraph = view.getGraph2D();

        AutoBoundsFeature abf = null;
        final Node modelParent = hierarchy.getParentNode(modelNode);
        if (modelParent != null) {
          final Node viewParent = manager.getViewNode(modelParent, filteredGraph);
          if (viewParent != null) {
            final NodeRealizer nr = filteredGraph.getRealizer(viewParent);
            abf = nr.getAutoBoundsFeature();
          }
        }
        final boolean oldABF = abf != null && abf.isAutoBoundsEnabled();
        if (oldABF) {
          abf.setAutoBoundsEnabled(false);
        }

        updateFilteredGraph(filteredGraph, true);

        final Node viewNode = manager.getViewNode(modelNode, filteredGraph);
        filteredGraph.setCenter(viewNode, x, y);

        if (oldABF) {
          abf.setAutoBoundsEnabled(true);
        }

        return viewNode;
      }

      protected Node createNode(
              final Graph2DView view,
              final NodeRealizer r,
              final double x,
              final double y
      ) {
        final Graph2D graph = view.getGraph2D();
        graph.firePreEvent();
        try {
          return createNodeImpl(view, r, x ,y);
        } finally {
          graph.firePostEvent();
        }
      }

      protected boolean dropNodeRealizer(
              final Graph2DView view,
              final NodeRealizer r,
              final double x,
              final double y
      ) {
        final boolean success = super.dropNodeRealizer(view, r, x, y);
        if (success) {
          view.requestFocus();
        }
        return success;
      }
    };
    dropSupport.setPreviewEnabled(true);

    final DragSource dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(jt, DnDConstants.ACTION_MOVE,
        new DragGestureListener() {
          public void dragGestureRecognized(final DragGestureEvent e) {
            final TreePath[] paths = jt.getSelectionPaths();
            if (paths != null && paths.length > 0) {
              final Object value = paths[0].getLastPathComponent();
              if (value instanceof Node) {
                final Node modelNode = (Node) value;
                final HierarchyManager hierarchy = baseModel.getHierarchyManager();
                if (!hierarchy.isGroupNode(modelNode)) {
                  dropSupport.startDrag(
                          dragSource,
                          baseModel.getRealizer(modelNode),
                          e,
                          DragSource.DefaultMoveDrop);
                }
              }
            }
          }
        });
  }

  /**
   * Calculates a new graph layout using the specified layout style.
   * @param context the graph to lay out.
   * @param layout one of
   * {@link GraphExplorerOptionHandler#ID_LAYOUT_ORTHOGONAL},
   * {@link GraphExplorerOptionHandler#ID_LAYOUT_ORGANIC},
   * {@link GraphExplorerOptionHandler#ID_LAYOUT_HIERARCHIC},
   * {@link GraphExplorerOptionHandler#ID_LAYOUT_BALLOON},
   * {@link GraphExplorerOptionHandler#ID_LAYOUT_CIRCULAR},
   */
  private static void doLayout( final LayoutContext context, final byte layout ) {
    switch (layout) {
      case GraphExplorerOptionHandler.ID_LAYOUT_ORGANIC:
        LayoutSupport.doOrganicLayout(context);
        break;
      case GraphExplorerOptionHandler.ID_LAYOUT_HIERARCHIC:
        LayoutSupport.doHierarchicLayout(context);
        break;
      case GraphExplorerOptionHandler.ID_LAYOUT_BALLOON:
        LayoutSupport.doBalloonLayout(context);
        break;
      case GraphExplorerOptionHandler.ID_LAYOUT_CIRCULAR:
        LayoutSupport.doCircularLayout(context);
        break;
      default:
        LayoutSupport.doOrthogonalLayout(context);
        break;
    }
  }

  /**
   * Updates the specified graph according to the inclusion settings
   * of the demo's inclusion filter.
   * @param graph the graph to be changed. The given graph instance has to be
   * registered as a view graph in the demo's model-view manager.
   * @param allowRemovals if <code>true</code> elements may be removed from
   * the specified graph.
   * 
   */
  private void updateFilteredGraph(
          final Graph2D graph, final boolean allowRemovals
  ) {
    final InclusionFilter filter = inclusionFilter;
    try {
      filter.setAllowRemovals(allowRemovals);
      manager.synchronizeModelToViewGraph(graph);
    } finally {
      filter.setAllowRemovals(true);
    }
  }

  /**
   * Returns the application tool bar component.
   * @return the application tool bar component or <code>null</code> if there
   * is no tool bar.
   */
  private JToolBar getToolBar() {
    final JPanel container = contentPane;
    for (int i = 0, n = container.getComponentCount(); i < n; ++i) {
      final Component c = container.getComponent(i);
      if (c instanceof JToolBar) {
        return (JToolBar) c;
      }
    }

    return null;
  }

  /**
   * Sets tool tip text and display icon for the specified action.
   * @param a the action to update.
   * @param tip the tool tip text for the specified action.
   * @param icon the resource path to the display icon for the specified action.
   * @return the specified action instance.
   */
  private Action prepare( final Action a, final String tip, final String icon ) {
    a.putValue(Action.SHORT_DESCRIPTION, tip);
    a.putValue(Action.SMALL_ICON, getIconResource(icon));
    return a;
  }

  /**
   * Creates the application help pane.
   *
   * @param helpURL the URL of the HTML help page to display.
   */
  protected JComponent createHelpPane(final URL helpURL) {
    try {
      JEditorPane editorPane = new JEditorPane(helpURL);
      editorPane.setEditable(false);
      editorPane.setPreferredSize(new Dimension(250, 250));
      return new JScrollPane(editorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Overwritten to load an initial sample graph <em>after</em> the
   * graphical user interface has been completely created.
   * @param rootPane the container to hold the demo's graphical user interface.
   */
  public void addContentTo( final JRootPane rootPane ) {
    super.addContentTo(rootPane);
    loadInitialGraph();
  }

  /**
   * Overwritten to create an action that loads a model graph in GraphML or
   * compressed GraphML format in a background thread.
   * @return an action that that loads a model graph in GraphML or compressed
   * GraphML format in a background thread. 
   */
  protected Action createLoadAction() {
    return new LoadAction();
  }

  /**
   * Overwritten to create an action that saves the model graph in GraphML or
   * compressed GraphML format.
   * @return an action that that saves the model graph in GraphML or compressed
   * GraphML format. 
   */
  protected Action createSaveAction() {
    return new SaveAction("Save Model Graph", baseModel);
  }

  /**
   * Creates an action the saves the displayed graph in GraphML or compressed
   * GraphML format.
   * @return an action the saves the displayed graph in GraphML or compressed
   * GraphML format.
   */
  protected Action createSaveFilteredGraphAction() {
    return new SaveAction("Save Filtered Graph", view.getGraph2D());
  }

  /**
   * Overwritten to add controls to save the model and the displayed graph
   * as well as controls to open sample graphs to the application menu bar.
   * @return the application menu bar.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();

    JMenu file = new JMenu("File");
    Action action;
    action = createLoadAction();
    if (action != null) {
      file.add(action);
    }
    action = createSaveAction();
    if (action != null) {
      file.add(action);
    }
    file.add(createSaveFilteredGraphAction());
    file.addSeparator();
    file.add(new PrintAction());
    file.addSeparator();
    file.add(new ExitAction());
    mb.add(file);

    //add valid sample graphs to the "sample graphs" menu
    JMenu sampleGraphs = new JMenu("Sample Graphs");
    for (Iterator it = getLoadSampleActions(); it.hasNext();) {
      sampleGraphs.add((Action) it.next());
    }
    mb.add(sampleGraphs);

    return mb;
  }

  private Iterator getLoadSampleActions() {
    final String key = "MultiPageLayoutDemo.samples";
    final Object samples = contentPane.getClientProperty(key);
    if (samples instanceof List) {
      return ((List) samples).iterator();
    } else {
      final ArrayList list = new ArrayList(3);
      list.add(createLoadSampleActions(
              "Pop Artists Relationships",
              "resource/pop-artists.graphmlz"));
      list.add(createLoadSampleActions(
              "yFiles Class Relationships",
              "resource/yfiles-classes.graphmlz"));
      list.add(createLoadSampleActions(
              "yFiles Class Relationships with Nested Packages",
              "resource/yfiles-classes-and-packages-nested.graphmlz"));
      contentPane.putClientProperty(key, list);
      return list.iterator();
    }
  }

  private Action createLoadSampleActions(final String name, final String resource) {
    if (isResourceValid(resource)) {
      return new LoadSampleGraphAction(name, resource);
    } else {
      throw new RuntimeException("Missing resource: " + resource);
    }
  }

  /**
   * Determines whether or not the specified resource can be resolved.
   * @param resource the name of the resource to check.
   * @return <code>true</code> if the resource can be resolved;
   * <code>false</code> otherwise.
   */
  private boolean isResourceValid(final String resource) {
    return getResource(resource) != null;
  }

  /**
   * Loads an initial sample graph.
   */
  protected void loadInitialGraph() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        final Iterator it = getLoadSampleActions();
        if (it.hasNext()) {
          final Action action = (Action) it.next();
          action.putValue("onLoaded", new Runnable() {
            public void run() {
              final Node mn = baseModel.firstNode();
              baseModel.unselectAll();
              baseModel.setSelected(mn, true);

              inclusionFilter.reset();
              inclusionFilter.includeNode(mn);

              final Graph2D v = view.getGraph2D();
              v.firePreEvent();
              try {
                updateFilteredGraph(v, true);
      
                final Node vn = manager.getViewNode(mn, v);
                final NodeRealizer vnr = v.getRealizer(vn);
      
                view.setZoom(1);
                view.setCenter(vnr.getCenterX(), vnr.getCenterY());
      
                handleClick(new NodeList(mn), mn, true);
              } finally {
                v.firePostEvent();
              }

              if (undoManager != null) {
                undoManager.resetQueue();
            }
            }
          });
          action.actionPerformed(null);
        }
      }
    });
  }

  /**
   * Overwritten to load a model graph in GraphML or compressed GraphML format.
   * @param resourceName the name of the graph resource to load. 
   */
  protected void loadGraph(String resourceName) {
    final Graph2D filteredGraph = view.getGraph2D();
    filteredGraph.clear();
    filteredGraph.updateViews();

    loadGraph(baseModel, resourceName);
  }

  /**
   * Loads the specified graph structure resource in GraphML of compressed
   * GraphML formant into the given graph instance.
   * @param graph the graph instance to store the loaded data.
   * @param resourceName the name of the graph resource to load.
   */
  private void loadGraph(final Graph2D graph, final String resourceName) {
    URL resource = null;
    final File file = new File(resourceName);
    if (file.exists()) {
      try {
        resource = file.toURI().toURL();
      } catch (MalformedURLException e) {
        D.showError(e.getMessage());
        return;
      }
    } else {
      resource = getResource(resourceName);
      if (resource == null) {
        return;
      }
    }

    try {
      final IOHandler ioh = resource.getFile().endsWith(".graphmlz") ? new ZipGraphMLIOHandler() : createGraphMLIOHandler();
      
      graph.clear();
      ioh.read(graph, resource);

      normalizeModel(graph);
    } catch (IOException ioe) {
      D.showError("Unexpected error while loading resource \"" + resource + "\" due to " + ioe.getMessage());
    }
    graph.setURL(resource);
  }

  /**
   * Disables default undo-/redo-support. This demo uses its own {@link Graph2DUndoManager}.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Disables the clipboart for this demo.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  //"open" folder nodes and reset node positions as well as edge paths
  private static void normalizeModel( final Graph2D graph ) {
    if (!graph.isEmpty()) {
      final GroupNodeRealizer.StateChangeListener listener =
              new GroupNodeRealizer.StateChangeListener();
      final HierarchyManager hierarchy = graph.getHierarchyManager();
      hierarchy.addHierarchyListener(listener);

      //open folder nodes
      final ArrayList stack = new ArrayList();
      stack.add(null);
      while (!stack.isEmpty()) {
        final Node root = (Node) stack.remove(stack.size() - 1);

        for (NodeCursor nc = hierarchy.getChildren(root); nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (hierarchy.isFolderNode(node)) {
            hierarchy.openFolder(node);
            stack.add(node);
          }
        }
      }
      hierarchy.removeHierarchyListener(listener);

      //reset node positions and edge paths
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node node = nc.node();
        graph.setCenter(node, YPoint.ORIGIN);
      }
      LayoutTool.resetPaths(graph, true);

      final Rectangle2D.Double r = new Rectangle2D.Double(0, 0, -1, -1);
      for (NodeCursor nc = hierarchy.getChildren(null); nc.ok(); nc.next()) {
        graph.getRealizer(nc.node()).calcUnionRect(r);
      }
    }
  }

  /**
   * Overwritten to prevent keyboard shortcuts from being registered multiple
   * times. The demo constructor registers the demo's shortcuts.
   */
  protected void registerViewActions() {
  }

  /**
   * Overwritten to prevent the default view listeners from being registered.
   * The demo constructor registers the demo's view listeners.
   */
  protected void registerViewListeners() {
  }

  /**
   * Overwritten to prevent the default view modes from being registered.
   * The demo constructor registers the demo's view modes.
   */
  protected void registerViewModes() {
  }

  /**
   * Called after clicking on a node in the graph view.
   *
   * @param selectedModelNodes all model nodes that are selected in the view.
   * @param clickedModelNode the clicked model node.
   * @param explore whether or not to explore the neighbors of selected nodes.
   */
  protected void handleClick(
          final NodeList selectedModelNodes,
          final Node clickedModelNode,
          final boolean explore
  ) {
    final InclusionFilter filter = inclusionFilter;
    final Graph2D graph = view.getGraph2D();

    final LayoutContext context = new LayoutContext(
            view, true, true, manager.getViewNode(clickedModelNode, graph), baseModel.getHierarchyManager().containsGroups());


    final byte edgeType = oh.getExplorationEdgeType();

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      context.addOldNode(nc.node());
    }
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      context.addOldEdge(ec.edge());
    }

    filter.reset();
    //include all selected model nodes
    for (NodeCursor nc = selectedModelNodes.nodes(); nc.ok(); nc.next()) {
      filter.includeNode(nc.node());
    }

    //include all edges of the induced subgraph
    for (NodeCursor nc = selectedModelNodes.nodes(); nc.ok(); nc.next()) {
      final Node modelNode = nc.node();
      for (EdgeCursor ec = getEdges(modelNode, edgeType); ec.ok(); ec.next()) {
        final Edge modelEdge = ec.edge();
        if (filter.acceptInsertion(modelEdge.opposite(modelNode))) {
          filter.includeEdge(modelEdge);
        }
      }
    }

    final byte direction;
    final byte oppositeEdgeType;
    switch (edgeType) {
      case GraphExplorerOptionHandler.EDGE_TYPE_ALL:
        direction = Bfs.DIRECTION_BOTH;
        oppositeEdgeType = GraphExplorerOptionHandler.EDGE_TYPE_ALL;
        break;
      case GraphExplorerOptionHandler.EDGE_TYPE_OUT:
        direction = Bfs.DIRECTION_SUCCESSOR;
        oppositeEdgeType = GraphExplorerOptionHandler.EDGE_TYPE_IN;
        break;
      case GraphExplorerOptionHandler.EDGE_TYPE_IN:
        direction = Bfs.DIRECTION_PREDECESSOR;
        oppositeEdgeType = GraphExplorerOptionHandler.EDGE_TYPE_OUT;
        break;
      default:
        throw new IllegalStateException("Unsupported edge type: " + edgeType);
    }

    final NodeList[] modelLayers = Bfs.getLayers(
            baseModel,
            selectedModelNodes,
            explore ? direction : Bfs.DIRECTION_BOTH,
            Maps.createHashedNodeMap(),
            2);

    if (modelLayers.length > 1) {
      if (explore) {
        int nodesToAdd = oh.getMaxNewNodes();
        final HashSet tmpModelLayer = new HashSet();
        tmpModelLayer.addAll(modelLayers[0]);
        for (NodeCursor nc = modelLayers[1].nodes(); nc.ok(); nc.next()) {
          final Node modelNode = nc.node();
          final Node viewNode = manager.getViewNode(modelNode, graph);
          if (viewNode == null) {
            if (nodesToAdd > 0) {
              filter.includeNode(modelNode);
              --nodesToAdd;
            }
          }
          // accept all edges from selected nodes to their direct neighbors
          for (EdgeCursor ec = getEdges(modelNode, oppositeEdgeType); ec.ok(); ec.next()) {
            final Edge modelEdge = ec.edge();
            if (tmpModelLayer.contains(modelEdge.opposite(modelNode))) {
              filter.includeEdge(modelEdge);
            }
          }
        }
      } else {
        for (NodeCursor nc = modelLayers[1].nodes(); nc.ok(); nc.next()) {
          final Node modelNode = nc.node();
          final Node viewNode = manager.getViewNode(modelNode, graph);
          if (viewNode != null) {
            context.addNewNode(viewNode);
          }
        }
      }
    }

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      filter.includeNode(manager.getModelNode(nc.node()));
    }
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      filter.includeEdge(manager.getModelEdge(ec.edge()));
    }

    final HierarchyManager mh = baseModel.getHierarchyManager();
    final HashSet parents = new HashSet();
    for (Iterator it = inclusionFilter.includedNodes.iterator(); it.hasNext();) {
      final Node next = (Node) it.next();
      for (Node p = mh.getParentNode(next); p != null; p = mh.getParentNode(p)) {
        if (!parents.add(p)) {
          break;
        }
      }
    }
    for (Iterator it = parents.iterator(); it.hasNext();) {
      inclusionFilter.includeNode((Node) it.next());
    }

    final NodeMap node2Predecessor = Maps.createHashedNodeMap();
    for (NodeCursor nc = selectedModelNodes.nodes(); nc.ok(); nc.next()) {
      final Node modelNode = nc.node();
      for (EdgeCursor ec = getEdges(modelNode, edgeType); ec.ok(); ec.next()) {
        final Edge modelEdge = ec.edge();
        final Node modelNeighbor = modelEdge.opposite(modelNode);
        if (node2Predecessor.get(modelNeighbor) == null) {
          node2Predecessor.set(modelNeighbor, modelNode);
        }
      }
    }

    updateFilteredGraph(graph, false);

    final NodeList viewNodes = new NodeList();
    for (NodeCursor nc = selectedModelNodes.nodes(); nc.ok(); nc.next()) {
      final Node viewNode = manager.getViewNode(nc.node(), graph);
      if (viewNode != null) {
        viewNodes.add(viewNode);
      }
    }
    final NodeMap viewDist = Maps.createHashedNodeMap();
    final int maxDist = oh.getMaxDist();
    Bfs.getLayers(
            graph,
            viewNodes,
            Bfs.DIRECTION_BOTH,
            viewDist,
            maxDist + 1);
    final HierarchyManager vh = graph.getHierarchyManager();
    if (maxDist > -1) {
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node viewNode = nc.node();
        final int dist = viewDist.getInt(viewNode);
        if ((dist < 0 || maxDist < dist) && !vh.isGroupNode(viewNode)) {
          filter.excludeNode(manager.getModelNode(viewNode));
          context.addRemovedNode(viewNode);
          for (EdgeCursor ec = viewNode.edges(); ec.ok(); ec.next()) {
            context.addRemovedEdge(ec.edge());
          }
        }
      }

      for (NodeCursor nc = vh.getChildren(null); nc.ok(); nc.next()) {
        checkEmptyGroups(vh, nc.node(), viewDist, maxDist, context);
      }
    } else {
      for (NodeCursor nc = vh.getChildren(null); nc.ok(); nc.next()) {
        checkEmptyGroups(vh, nc.node(), viewDist, Integer.MAX_VALUE, context);
      }
    }

    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      if (!context.isOldEdge(ec.edge())) {
        context.addNewEdge(ec.edge());
      }
    }

    if (explore) {
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node viewNode = nc.node();
        if (!context.isOldNode(viewNode)) {
          context.addNewNode(viewNode);

          final Node modelNode = manager.getModelNode(viewNode);  
          final Node predecessorNode = manager.getViewNode(
                  (Node) node2Predecessor.get(modelNode), graph);
          if (predecessorNode == null) {
            graph.setCenter(viewNode, 0, 0);
          } else if (!context.isOldNode(viewNode)) {
            graph.setCenter(viewNode, graph.getCenter(predecessorNode));
          }
        }
      }

      final Rectangle2D.Double r = new Rectangle2D.Double();
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        graph.getRealizer(nc.node()).calcUnionRect(r);
      }
    }

    doLayout(context, oh.getLayoutId());
  }

  /*
   * Marks group nodes for removal if they:
   * - do not contain (non-marked) elements,
   * - and the dist value of the group node is larger than max dist
   */
  private boolean checkEmptyGroups(
          final HierarchyManager vh,
          final Node viewNode,
          final DataProvider viewDist,
          final int maxDist,
          final LayoutContext context
  ) {
    if (vh.isGroupNode(viewNode)) {
      boolean keepGroup = false;
      for (NodeCursor nc = vh.getChildren(viewNode); nc.ok(); nc.next()) {
        if (checkEmptyGroups(vh, nc.node(), viewDist, maxDist, context)) {
          keepGroup = true;
        }
      }
      if (keepGroup) {
        return true;
      }

      final int dist = viewDist.getInt(viewNode);
      if (0 <= dist && dist <= maxDist) {
        return true;
      }
      context.addRemovedNode(viewNode);
      for (EdgeCursor ec = viewNode.edges(); ec.ok(); ec.next()) {
        context.addRemovedEdge(ec.edge());
      }
      return false;
    } else {
      return !context.isRemovedNode(viewNode);
    }
  }

  /**
   * Returns a cursor over all edges of the specified type that are connected to
   * the specified node.
   * @param node the node whose edges are retrieved.
   * @param edgeType one of {@link GraphExplorerOptionHandler#EDGE_TYPE_ALL},
   * {@link GraphExplorerOptionHandler#EDGE_TYPE_OUT}, and
   * {@link GraphExplorerOptionHandler#EDGE_TYPE_IN}.
   * @return a cursor over edges connected to the specified node.
   * @throws IllegalArgumentException if edge type does not equal one of the
   * mentioned symbolic constants. 
   */
  private static EdgeCursor getEdges( final Node node, final byte edgeType ) {
    switch (edgeType) {
      case GraphExplorerOptionHandler.EDGE_TYPE_ALL:
        return node.edges();
      case GraphExplorerOptionHandler.EDGE_TYPE_OUT:
        return node.outEdges();
      case GraphExplorerOptionHandler.EDGE_TYPE_IN:
        return node.inEdges();
      default:
        throw new IllegalArgumentException("Unsupported edge type: " + edgeType);
    }
  }


  /**
   * Creates a horizontally split <code>JSplitPane</code> instance.
   * @param left the component for the split pane's left compartment.
   * @param right  the component for the split pane's right compartment.
   * @return a horizontally split <code>JSplitPane</code> instance.
   */
  private static JSplitPane newSplitPane(
          final JComponent left, final JComponent right
  ) {
    return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
  }

  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new GraphExplorerDemo("resource/graphexplorerhelp.html")).start();
      }
    });
  }

  /**
   * Action that excludes deleted elements from the inclusion filter (and, thus, from the graph view).
   */
  final class MyDeleteSelectionAction extends Graph2DViewActions.DeleteSelectionAction {
    protected boolean acceptEdge(Graph2D graph, Edge edge) {
      final boolean accept = super.acceptEdge(graph, edge);
      if (accept) {
        final Edge modelEdge = manager.getModelEdge(edge);
        inclusionFilter.excludeEdge(modelEdge);
      }
      return accept;
    }
    
    protected boolean acceptNode(Graph2D graph, Node node) {
      final boolean accept = super.acceptNode(graph, node);
      if (accept) {
        final Node modelNode = manager.getModelNode(node);
        inclusionFilter.excludeNode(modelNode);
      }
      return accept;
    }
  }

  final class MyDoubleClickListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      final JTree tree = (JTree) e.getSource();
      if (e.getClickCount() == 2) {
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
          final Object last = path.getLastPathComponent();
          if (last instanceof Node) {
            final Node modelNode = (Node) last;
            final HierarchyManager hierarchy = baseModel.getHierarchyManager();
            if (!hierarchy.isGroupNode(modelNode)) {
              final Graph2D graph = view.getGraph2D();
              graph.firePreEvent();
              try {
                createViewNode(modelNode);
              } finally {
                graph.firePostEvent();
              }
            }
          }
        }
      }
    }

    private void createViewNode( final Node clickedModelNode ) {
      final NodeMap modelNode2IsNew = Maps.createHashedNodeMap();
      final NodeList selectedNodes = new NodeList(baseModel.selectedNodes());
      if (!selectedNodes.contains(clickedModelNode)) {
        selectedNodes.add(clickedModelNode);
      }
      collectParentNodes(selectedNodes);
      for (NodeCursor nc = selectedNodes.nodes(); nc.ok(); nc.next()) {
        Node node = nc.node();
        if (!inclusionFilter.isIncluded(node)) {
          modelNode2IsNew.setBool(node, true);
          inclusionFilter.includeNode(node);
        }
      }

      final Graph2D graph = view.getGraph2D();
      final boolean empty = graph.nodeCount() == 0;
      updateFilteredGraph(graph, true);

      final Node clickedViewNode = manager.getViewNode(clickedModelNode, graph);
      final LayoutContext context = new LayoutContext(view, false, true, null, baseModel.getHierarchyManager().containsGroups());
      for (NodeCursor nc = selectedNodes.nodes(); nc.ok(); nc.next()) {
        final Node modelNode = nc.node();
        if (modelNode2IsNew.getBool(modelNode)) {
          final Node viewNode = manager.getViewNode(modelNode, graph);
          if (viewNode.getGraph() == graph) {
            context.addNewNode(viewNode);
          }
        }
      }

      doLayout(context, oh.getLayoutId());
      
      final ViewAnimationFactory factory = new ViewAnimationFactory(view);
      factory.setQuality(ViewAnimationFactory.HIGH_PERFORMANCE);
      final CompositeAnimationObject composite = AnimationFactory.createConcurrency();
      for (Iterator it = orderByGroupDepth(graph, context.newNodes()); it.hasNext();) {
        final NodeRealizer nr = graph.getRealizer((Node) it.next());
        composite.addAnimation(factory.fadeIn(nr, DURATION_ADD));
      }

      if (empty) {
        view.setZoom(1);
        view.setCenter(graph.getX(clickedViewNode), graph.getY(clickedViewNode));
      } else {
        composite.addAnimation(factory.focusView(
                Math.max(view.getZoom(), 1),
                DefaultMutableValue2D.createView(graph.getLocation(clickedViewNode)),
                DURATION_ADD));
      }

      final AnimationPlayer player = factory.createConfiguredPlayer();
      player.addAnimationListener(new AnimationListener() {
        public void animationPerformed( final AnimationEvent e ) {
          if (AnimationEvent.END == e.getHint()) {
            view.requestFocus();
          }
        }
      });
      player.animate(composite);
    }

    private Iterator orderByGroupDepth( final Graph2D graph, final Iterator nodes ) {
      final HierarchyManager manager = graph.getHierarchyManager();
      if (manager == null) {
        return nodes;
      } else {
        final NodeMap depth = Maps.createHashedNodeMap();
        final ArrayList ordered = new ArrayList();
        while (nodes.hasNext()) {
          final Node node = (Node) nodes.next();
          depth.setInt(node, manager.getLocalGroupDepth(node));
          ordered.add(node);
        }
        Collections.sort(ordered, new Comparator() {
          public int compare( final Object o1, final Object o2 ) {
            return Comparators.compare(depth.getInt(o1), depth.getInt(o2));
          }
        });
        return ordered.iterator();
      }
    }

    private void collectParentNodes( final NodeList nodes ) {
      final HierarchyManager hierarchy = baseModel.getHierarchyManager();
      final NodeList newNodes = new NodeList();
      final HashSet marked = new HashSet();
      for (NodeCursor nc = nodes.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        for (Node p = hierarchy.getParentNode(node); p != null; p = hierarchy.getParentNode(p)) {
          if (marked.add(p)) {
            newNodes.add(p);
          } else {
            break;
          }
        }
      }
      marked.clear();
      marked.addAll(nodes);
      for (NodeCursor nc = newNodes.nodes(); nc.ok(); nc.next()) {
        final Node p = nc.node();
        if (marked.add(p)) {
          nodes.add(p);
        }
      }
    }
  }

  /**
   * Customized graph renderer that shows, for each node with hidden links, a yellow oval in the upper-right node corner
   * that contains the number of such links (i.e., the number of edges of the node which are currently not shown).
   */
  final class MyGraph2DRenderer extends DefaultGraph2DRenderer {
    private static final int OVAL_WIDTH = 25;
    private static final int OVAL_HEIGHT = 15;

    protected void paintSloppy(Graphics2D gfx, NodeRealizer nr) {
      super.paintSloppy(gfx, nr);
    }

    protected void paint(Graphics2D gfx, NodeRealizer nr) {
      super.paint(gfx, nr);
      final Color bkpColor = gfx.getColor();
      final Graph2D filteredGraph = view.getGraph2D();
      final Node viewNode = nr.getNode();
      final Node modelNode = manager.getModelNode(viewNode);
      final byte edgeType = oh.getExplorationEdgeType();
      final int hiddenLinks = getDegree(modelNode, edgeType) - getDegree(viewNode, edgeType); //calculate the number of hidden (non-displayed) links
      if (hiddenLinks > 0) {
        //draw the yellow oval containing the number of hidden links
        final YRectangle viewNodeRect = filteredGraph.getRectangle(viewNode);
        final YPoint ovalLocation = new YPoint(viewNodeRect.x + viewNodeRect.width - OVAL_WIDTH * 0.5,
            viewNodeRect.y - OVAL_HEIGHT * 0.5);
        gfx.setColor(Color.YELLOW);
        gfx.fillOval((int) ovalLocation.x, (int) ovalLocation.y, OVAL_WIDTH, OVAL_HEIGHT);
        gfx.setColor(Color.YELLOW.darker());
        gfx.drawOval((int) ovalLocation.x, (int) ovalLocation.y, OVAL_WIDTH, OVAL_HEIGHT);
        gfx.setColor(Color.BLACK);
        final String hiddenLinksString = hiddenLinks + "";
        final int stringWidth = gfx.getFontMetrics().stringWidth(hiddenLinksString);
        final int stringHeight = gfx.getFontMetrics().getAscent() - 2;
        gfx.drawString("" + hiddenLinks, (float) (ovalLocation.x + OVAL_WIDTH * 0.5 - stringWidth * 0.5),
            (float) (ovalLocation.y + OVAL_HEIGHT * 0.5 + stringHeight * 0.5));
        gfx.setColor(bkpColor);
      }
    }

    private int getDegree( final Node node, final byte edgeType ) {
      switch (edgeType) {
        case GraphExplorerOptionHandler.EDGE_TYPE_ALL:
          return node.degree();
        case GraphExplorerOptionHandler.EDGE_TYPE_OUT:
          return node.outDegree();
        case GraphExplorerOptionHandler.EDGE_TYPE_IN:
          return node.inDegree();
        default:
          throw new IllegalArgumentException("Unsupported edge type: " + edgeType);
      }
    }
  }

  final class MyGraphCopyFactory extends Graph2DCopyFactory.HierarchicGraph2DCopyFactory {
    private final DefaultHierarchyGraphFactory factory;

    MyGraphCopyFactory() {
      final GraphCopier.CopyFactory f = baseModel.getGraphCopyFactory();
      if (f instanceof DefaultHierarchyGraphFactory) {
        factory = (DefaultHierarchyGraphFactory) f;
      } else {
        factory = new DefaultHierarchyGraphFactory();
      }
    }

    public Graph createGraph() {
      final Graph2D g = new Graph2D();
      final HierarchyManager hierarchy = new HierarchyManager(g);
      hierarchy.setGraphFactory(factory);
      g.setGraphCopyFactory(this);
      return g;
    }

    public void postCopyGraphData(
            final Graph sourceGraph,
            final Graph targetGraph,
            final Map nodeMap,
            final Map edgeMap
    ) {
      final HashMap tmp = new HashMap();
      tmp.putAll(nodeMap);

      //update mapping between nodes of source and target graph
      for (NodeCursor nc = targetGraph.nodes(); nc.ok(); nc.next()) {
        final Node view = nc.node();
        final Node model = manager.getModelNode(view);
        if (!tmp.containsKey(model)) {
          tmp.put(model, view); 
        }
      }

      super.postCopyGraphData(sourceGraph, targetGraph, tmp, edgeMap);
    }
  }

  /**
   * Customized tree cell renderer that:
   * - greys out elements not shown in the graph view,
   * - uses a customized root element that shows the number of nodes in the model graph.
   */
  final class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
    ) {
      Component comp = super.getTreeCellRendererComponent(
              tree, value, sel, expanded, leaf, row, hasFocus);
      if (value instanceof Node && !inclusionFilter.isIncluded((Node) value)) {
        comp.setForeground(Color.GRAY);
      } else if(value instanceof  Graph) {
        comp = new JLabel("Nodes: " + ((Graph) value).nodeCount());
      }
      return comp;
    }
  }

  /** A <code>Graph2DSelectionListener</code> that triggers an update of the demo's local view upon selection changes. */
  final class SelectionTrigger implements Graph2DSelectionListener {
    private final Timer timer;
    private Graph2DSelectionEvent lastEvent;

    SelectionTrigger(final Graph2D modelGraph) {
      timer = new Timer(100, new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          if (lastEvent != null) {
            handleEvent(lastEvent);
          }
        }

        /**
         * Triggers the actual update for the demo's local view.
         */
        private void handleEvent(final Graph2DSelectionEvent e) {
          boolean modelIsSource = (e.getGraph2D() == modelGraph); //TODO always false?
          if (e.isNodeSelection()) {
            if (modelIsSource) {
              for (Iterator iter = manager.viewGraphs(); iter.hasNext();) {
                ((Graph2D) iter.next()).unselectAll();
              }
            } else {
              baseModel.unselectAll();
            }

            for (NodeCursor nc = e.getGraph2D().selectedNodes(); nc.ok(); nc.next()) {
              final Node n = nc.node();
              if (modelIsSource) {
                for (Iterator iter = manager.viewGraphs(); iter.hasNext();) {
                  final Graph2D viewGraph = ((Graph2D) iter.next());
                  Node viewNode = manager.getViewNode(n, viewGraph);
                  if (viewNode != null) {
                    viewGraph.setSelected(viewNode, true);
                  }
                }
              } else {
                Node orig = manager.getModelNode(n);
                if (orig != null) {
                  baseModel.setSelected(orig, true);
                }
              }
            }
          } else if (e.isEdgeSelection()) {
            if (modelIsSource) {
              for (Iterator iter = manager.viewGraphs(); iter.hasNext();) {
                ((Graph2D) iter.next()).unselectAll();
              }
            } else {
              baseModel.unselectAll();
            }

            for (EdgeCursor ec = e.getGraph2D().selectedEdges(); ec.ok(); ec.next()) {
              final Edge edge = ec.edge();
              if (modelIsSource) {
                for (Iterator iter = manager.viewGraphs(); iter.hasNext();) {
                  final Graph2D viewGraph = ((Graph2D) iter.next());
                  Edge viewEdge = manager.getViewEdge(edge, viewGraph);
                  if (viewEdge != null) {
                    viewGraph.setSelected(viewEdge, true);
                  }
                }
              } else {
                Edge orig = manager.getModelEdge(edge);
                if (orig != null) {
                  baseModel.setSelected(orig, true);
                }
              }
            }
          }
        }
      });
      timer.setRepeats(false);
    }

    public void onGraph2DSelectionEvent(final Graph2DSelectionEvent e) {
      if (e.isNodeSelection() || e.isEdgeSelection()) {
        lastEvent = e;
        timer.restart();
      }
    }
  }


  /**
   * Abstract base class for loading graphs in a background thread.
   */
  abstract class AbstractLoadAction extends AbstractAction {
    AbstractLoadAction( final String name ) {
      super(name);
    }

    /**
     * Loads a model graph in a background thread.
     * @param resource the graph resource to load as model graph.
     * @param name the display name for the graph resource.
     */
    void load( final String resource, final String name ) {
      final Object onLoaded = getValue("onLoaded");
      putValue("onLoaded", null);

      EventQueue.invokeLater(new Runnable() {
        public void run() {
          final JDialog pd = createProgressDialog(name);

          final Graph2D filteredGraph = view.getGraph2D();
          filteredGraph.clear();
          filteredGraph.updateViews();

          (new Thread(new Runnable() {
            public void run() {
              loadGraph(baseModel, resource);
              EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (undoManager != null) {
                    undoManager.resetQueue();
                  }

                  pd.setVisible(false);
                  pd.dispose();

                  if (onLoaded instanceof Runnable) {
                    ((Runnable) onLoaded).run();
                  }
                }
              });
            }
          })).start();

          pd.setVisible(true);
        }
      });
    }

    /**
     * Creates an progress dialog for loading graphs in
     * background threads.
     * @param name the display name of the graph resource that is loaded.
     * @return a dialog displaying a progress bar.
     */
    private JDialog createProgressDialog( final String name ) {
      final String title = "Loading " + name;
      final JDialog jd = new JDialog(getFrame(), title, true);

      final JProgressBar jpb = new JProgressBar(0, 1);
      jpb.setString(title);
      jpb.setIndeterminate(true);

      final JLabel lbl = new JLabel(title);
      final JPanel progressPane = new JPanel(new BorderLayout());
      progressPane.add(lbl, BorderLayout.NORTH);
      progressPane.add(jpb, BorderLayout.CENTER);
      final JPanel contentPane = new JPanel(new FlowLayout());
      contentPane.add(progressPane);

      jd.setContentPane(contentPane);
      jd.pack();
      jd.setLocationRelativeTo(null);
      jd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

      return jd;
    }

    private JFrame getFrame() {
      final Window ancestor = SwingUtilities.getWindowAncestor(contentPane);
      if (ancestor instanceof JFrame) {
        return (JFrame) ancestor;
      } else {
        return null;
      }
    }
  }

  /**
   * Loads a sample graph using the given resource.
   */
  final class LoadSampleGraphAction extends AbstractLoadAction {
    private final String name;
    private final String resource;

    LoadSampleGraphAction(final String name, final String resource) {
      super(name);
      this.name = name;
      this.resource = resource;
    }

    public void actionPerformed(ActionEvent e) {
      load(resource, name);
    }
  }

  /** Action that loads the current graph from a file in GraphML format. */
  protected class LoadAction extends AbstractLoadAction {
    JFileChooser chooser;

    public LoadAction() {
      super("Load Model Graph");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphmlz");
          }

          public String getDescription() {
            return "Zipped GraphML Format (.graphmlz)";
          }
        });
      }
      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          final URL resource = chooser.getSelectedFile().toURI().toURL();
          final String ln = resource.getFile();
          load(ln, (new File(ln)).getName());
        } catch (MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    }
  }

  /**
   * Action that saves the current graph to a file in GraphML or compressed
   * GraphML format.
   */
  protected class SaveAction extends AbstractAction {
    private JFileChooser chooser;
    private Graph2D graph;

    /*
      Graph should be either the model graph or a view graph of the model.
     */
    public SaveAction(final String name, final Graph2D graph) {
      super(name);
      chooser = null;
      this.graph = graph;
    }

    private void setFileChooser(final JFileChooser chooser, final File file) {
      FileFilter[] filters = chooser.getChoosableFileFilters();
      for (int i = 0; i < filters.length; i++) {
        if (filters[i].accept(file)) {
          chooser.setFileFilter(filters[i]);
          return;
        }
      }
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphmlz");
          }

          public String getDescription() {
            return "Zipped GraphML Format (.graphmlz)";
          }
        });
      }

      final URL url = view.getGraph2D().getURL();
      if (url != null && "file".equals(url.getProtocol())) {
        try {
          final File file = new File(new URI(url.toString()));
          chooser.setSelectedFile(file);
          setFileChooser(chooser, file);
        } catch (URISyntaxException e1) {
          // ignore
        }
      }

      if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        IOHandler ioh;
        String name = chooser.getSelectedFile().toString();
        final FileFilter filter = chooser.getFileFilter();
        if (filter.accept(new File("file.graphml"))) {
          if (!name.endsWith(".graphml")) {
            name += ".graphml";
          }
          ioh = createGraphMLIOHandler();
        } else {
          if (!name.endsWith(".graphmlz")) {
            name += ".graphmlz";
          }
          ioh = new ZipGraphMLIOHandler();
        }

        try {
          ioh.write(graph, name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
  }


  /**
   * Custom <code>ModelViewManager.Filter</code> filter implementation that rejects edge and node representatives from
   * being automatically created by a <code>ModelViewManager</code>, if the corresponding model element is not stored in
   * one of this filter's inclusion sets.
   */
  static final class InclusionFilter implements ModelViewManager.Filter {
    private final Set includedNodes;
    private final Set includedEdges;

    private boolean allowRemovals;

    InclusionFilter() {
      includedNodes = new HashSet();
      includedEdges = new HashSet();
      allowRemovals = true;
    }

    void reset() {
      includedNodes.clear();
      includedEdges.clear();
    }

    boolean getAllowRemovals() {
      return allowRemovals;
    }

    void setAllowRemovals(final boolean allowRemovals) {
      this.allowRemovals = allowRemovals;
    }

    boolean isIncluded(final Node modelNode) {
      return includedNodes.contains(modelNode);
    }

    boolean includeNode(final Node modelNode) {
      return includedNodes.add(modelNode);
    }

    boolean includeEdge(final Edge modelEdge) {
      return includedEdges.add(modelEdge);
    }

    boolean excludeNode(final Node modelNode) {
      for(EdgeCursor ec = modelNode.edges(); ec.ok(); ec.next()) {
        includedEdges.remove(ec.edge());
      }
      return includedNodes.remove(modelNode);
    }

    boolean excludeEdge(final Edge modelEdge) {
      return includedEdges.remove(modelEdge);
    }

    public boolean acceptInsertion(final Node node) {
      return includedNodes.contains(node);
    }

    public boolean acceptInsertion(final Edge edge) {
      return includedEdges.contains(edge);
    }

    public boolean acceptRemoval(final Node node) {
      return allowRemovals;
    }

    public boolean acceptRemoval(final Edge edge) {
      return allowRemovals;
    }

    public boolean acceptRetention(final Node node) {
      return !allowRemovals || includedNodes.contains(node);
    }

    public boolean acceptRetention(final Edge edge) {
      return !allowRemovals || includedEdges.contains(edge);
    }
  }
}
