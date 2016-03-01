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
package demo.layout.mixed;

import demo.view.hierarchy.GroupingDemo;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.layout.LayoutGraph;
import y.layout.LayoutTool;
import y.layout.Layouter;
import y.layout.ParallelEdgeLayouter;
import y.layout.circular.CircularLayouter;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.router.polyline.EdgeLayoutDescriptor;
import y.layout.router.polyline.EdgeRouter;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/**
 * Shows the Recursive Group Layouter. The content of each group node is recursively laid out with the specified
 * layouter, i.e., the layouter is applied to each group node separately. Note that the
 * {@link y.layout.grouping.RecursiveGroupLayouter}
 * also supports to specify different layout algorithms for different group nodes, see {@link MixedLayoutDemo}.
 */
public class RecursiveLayoutDemo extends GroupingDemo {
  private static final int TYPE_ORTHOGONAL = 0;
  private static final int TYPE_HIERARCHIC = 1;
  private static final int TYPE_CIRCULAR = 2;
  private static final int TYPE_ORGANIC = 3;

  private int layoutType;
  private boolean useInterEdgeRouter;

  public RecursiveLayoutDemo() {
    this(null);
  }

  public RecursiveLayoutDemo(final String helpFilePath) {
    super();
    addHelpPane(helpFilePath);
  }

  protected void loadInitialGraph() {
    loadGraph("resource/recursive.graphml");
  }

  /**
   * Adds an extra layout action to the toolbar
   */
  protected JToolBar createToolBar() {
    layoutType = TYPE_ORTHOGONAL;
    final JComboBox layoutTypeSelection = new JComboBox(
        new String[]{"Orthogonal Style", "Hierarchic Style", "Circular Style", "Organic Style"});
    layoutTypeSelection.setSelectedIndex(layoutType);
    layoutTypeSelection.setMaximumSize(layoutTypeSelection.getPreferredSize());
    layoutTypeSelection.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switch (layoutTypeSelection.getSelectedIndex()) {
          default:
          case 0:
            layoutType = TYPE_ORTHOGONAL;
            break;
          case 1:
            layoutType = TYPE_HIERARCHIC;
            break;
          case 2:
            layoutType = TYPE_CIRCULAR;
            break;
          case 3:
            layoutType = TYPE_ORGANIC;
            break;
        }
        doLayout();
      }
    });

    useInterEdgeRouter = true;
    final JToggleButton toggleRouteInterEdges = new JToggleButton("Inter-Edge Routing", useInterEdgeRouter);
    toggleRouteInterEdges.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useInterEdgeRouter = toggleRouteInterEdges.isSelected();
        doLayout();
      }
    });

    final Action layoutAction = new AbstractAction(
            "Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        doLayout();
      }
    };

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(layoutAction));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(layoutTypeSelection);
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(toggleRouteInterEdges);
    return toolBar;
  }

  /**
   * Register key bindings for our custom actions.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.CLOSE_GROUPS, new MyCloseGroupsAction(view));
    actionMap.put(Graph2DViewActions.OPEN_FOLDERS, new MyOpenFoldersAction(view));
  }

  /**
   * Populates the "Grouping" menu with grouping specific actions.
   */
  protected void populateGroupingMenu(JMenu hierarchyMenu) {
    // Predefined actions for open/close groups
    registerAction(hierarchyMenu, Graph2DViewActions.CLOSE_GROUPS, true);
    registerAction(hierarchyMenu, Graph2DViewActions.OPEN_FOLDERS, true);

    hierarchyMenu.addSeparator();

    // Predefined actions for group/fold/ungroup
    registerAction(hierarchyMenu, Graph2DViewActions.GROUP_SELECTION, true);
    registerAction(hierarchyMenu, Graph2DViewActions.UNGROUP_SELECTION, true);
    registerAction(hierarchyMenu, Graph2DViewActions.FOLD_SELECTION, true);
  }

  /**
   * Performs the common behavior and applies a layout afterwards.
   */
  class MyCloseGroupsAction extends Graph2DViewActions.CloseGroupsAction {

    MyCloseGroupsAction(Graph2DView view) {
      super(view);
    }

    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  /**
   * Performs the common behavior and applies a layout afterwards.
   */
  class MyOpenFoldersAction extends Graph2DViewActions.OpenFoldersAction {

    MyOpenFoldersAction(Graph2DView view) {
      super(view);
    }

    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  void doLayout() {
    final RecursiveGroupLayouter rgl;
    final Layouter coreLayout;

    if (layoutType == TYPE_ORTHOGONAL) {
      coreLayout = new OrthogonalGroupLayouter();
      rgl = createOrthogonalRecursiveGroupLayout(coreLayout, EdgeLayoutDescriptor.MONOTONIC_NONE);

    } else if (layoutType == TYPE_HIERARCHIC) {
      final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
      ihl.setOrthogonallyRouted(true);
      coreLayout = ihl;
      rgl = createOrthogonalRecursiveGroupLayout(coreLayout, EdgeLayoutDescriptor.MONOTONIC_VERTICAL);

    } else if (layoutType == TYPE_ORGANIC) {
      final SmartOrganicLayouter sol = new SmartOrganicLayouter();
      sol.setMultiThreadingAllowed(true);
      coreLayout = sol;
      rgl = createRecursiveGroupLayout(coreLayout);
      rgl.setAutoAssignPortCandidatesEnabled(true);

    } else {
      final CircularLayouter cl = new CircularLayouter();
      cl.setParallelEdgeLayouter(createParallelEdgeLayouter());

      coreLayout = cl;
      rgl = createRecursiveGroupLayout(coreLayout);
    }

    final Graph2D graph = view.getGraph2D();
    try {
      // map each group node to its corresponding layout algorithm
      graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
        public Object get(Object dataHolder) {
          return coreLayout;
        }
      });

      new Graph2DLayoutExecutor().doLayout(view, rgl);
      view.fitContent();

    } finally {
      graph.removeDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY);
    }
  }

  RecursiveGroupLayouter createOrthogonalRecursiveGroupLayout(Layouter coreLayout, final byte monotonicPathType) {
    final RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(coreLayout) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          DataProvider selectedEdges = graph.getDataProvider(Layouter.SELECTED_EDGES); //backup selected edges

          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for (EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            edge2IsInterEdge.setBool(ec.edge(), true);
          }
          graph.addDataProvider(Layouter.SELECTED_EDGES, edge2IsInterEdge);

          //route inter-edges
          EdgeRouter oer = createOrthogonalEdgeRouter();
          if (monotonicPathType != EdgeLayoutDescriptor.MONOTONIC_NONE) {
            oer.getDefaultEdgeLayoutDescriptor().setMonotonicPathRestriction(monotonicPathType);
          }
          oer.doLayout(graph);

          //restore originally selected edges
          if (selectedEdges != null) {
            graph.addDataProvider(Layouter.SELECTED_EDGES, selectedEdges);
          } else {
            graph.removeDataProvider(Layouter.SELECTED_EDGES);
          }
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };

    rgl.setConsiderSketchEnabled(true);

    return rgl;
  }

  RecursiveGroupLayouter createRecursiveGroupLayout(Layouter coreLayout) {
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(coreLayout) {
      protected void routeInterEdges(LayoutGraph graph, EdgeList interEdges) {
        if (useInterEdgeRouter) {
          //reset paths of inter-edges
          EdgeMap edge2IsInterEdge = graph.createEdgeMap();
          for (EdgeCursor ec = interEdges.edges(); ec.ok(); ec.next()) {
            Edge e = ec.edge();
            edge2IsInterEdge.setBool(e, true);
            LayoutTool.resetPath(graph, e);
          }

          //layout parallel edges
          graph.addDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY, edge2IsInterEdge);
          createParallelEdgeLayouter().doLayout(graph);
          graph.removeDataProvider(ParallelEdgeLayouter.SCOPE_DPKEY);
          graph.disposeEdgeMap(edge2IsInterEdge);
        } else {
          super.routeInterEdges(graph, interEdges);
        }
      }
    };

    rgl.setConsiderSketchEnabled(true);

    return rgl;
  }

  static EdgeRouter createOrthogonalEdgeRouter() {
    EdgeRouter oer = new EdgeRouter();
    oer.setReroutingEnabled(true);
    oer.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
    return oer;
  }

  static ParallelEdgeLayouter createParallelEdgeLayouter() {
    ParallelEdgeLayouter pel = new ParallelEdgeLayouter();
    pel.setLineDistance(10.0);
    pel.setJoinEndsEnabled(true);
    return pel;
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new RecursiveLayoutDemo("resource/recursivelayouthelp.html").start();
      }
    });
  }
}
