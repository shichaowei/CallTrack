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
package demo.layout.hierarchic;

import demo.view.DemoBase;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2D;
import y.view.EdgeRealizer;
import y.view.ViewMode;
import y.view.LineType;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.Layouter;
import y.layout.LayoutGraph;
import y.base.EdgeCursor;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeList;
import y.base.EdgeList;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.TableEditorFactory;
import y.option.Editor;
import y.util.Maps;
import y.util.GraphHider;
import y.algo.ShortestPaths;
import y.algo.Paths;
import y.algo.Cycles;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Locale;

/**
 * This demo presents the critical path feature of the hierarchic layouter. The layouter tries to vertically align each node pair
 * that is connected by an edge marked as "critical". This feature can be utilized to highlight different edge paths that are relevant for a user.
 * <p>
 * The demo allows to manually mark/unmark critical edges by selecting some edges and, then, pressing button "Mark Selected Edges"/"Unmark Selected Edges".
 * Critical edges are colored red, common edges are colored black. The current state of selected edges can be toggled by double-clicking.
 * </p><p>
 * Pressing the "Apply Layout" button calculates a new layout of the current graph.
 * </p><p>
 * Pressing button "Mark Longest Path" allows to automatically select all edges that belong to a longest path of the graph.
 * If two nodes of the graph are marked as selected, pressing button "Mark Path Between Two Nodes" selects all edges
 * of the shortest-path between this nodes.
 * </p>
 *

 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html#incremental_hierarchical_critical_paths">Section Emphasizing Critical Paths</a> in the yFiles for Java Developer's Guide
 */
public class CriticalPathDemo extends DemoBase {
  private static final Color COLOR_CRITICAL_EDGE = Color.RED;
  private static final Color COLOR_COMMON_EDGE = Color.BLACK;

  private boolean backloopRoutingEnabled;
  private boolean edgeStraighteningOptimizationEnabled;
  private boolean useOrthogonalEdgeRoutes;
  private int minimalNodeDistance;
  private int minimalLayerDistance;

  public CriticalPathDemo() {
    this(null);
  }

  public CriticalPathDemo(final String helpFilePath) {
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createOptionTable(createOptionHandler()),
        view);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    contentPane.add(splitPane, BorderLayout.CENTER);
    addHelpPane(helpFilePath);
    loadInitialGraph();
  }

  /** Adds an extra layout action to the toolbar */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();   
    bar.addSeparator();
    bar.add(createActionControl(new LayoutAction()));
    bar.addSeparator();
    bar.add(new MarkSelectionAction());
    bar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    bar.add(new UnmarkSelectionAction());
    bar.addSeparator();
    bar.add(new MarkLongestPath());
    bar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    bar.add(new MarkShortestPathBetweenNodes());
    return bar;
  }

  private void loadInitialGraph() {
    loadGraph("resource/critical_path.graphml");    
  }


  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    minimalLayerDistance = 60;
    OptionItem minimalLayerDistanceItem = layoutOptionHandler.addInt("Minimal Layer Distance", minimalLayerDistance);
    minimalLayerDistanceItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        minimalLayerDistance = layoutOptionHandler.getInt("Minimal Layer Distance");
      }
    });

    minimalNodeDistance = 30;
    OptionItem minimalNodeDistanceItem = layoutOptionHandler.addInt("Minimal Node Distance", minimalNodeDistance);
    minimalNodeDistanceItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        minimalNodeDistance = layoutOptionHandler.getInt("Minimal Node Distance");
      }
    });

    useOrthogonalEdgeRoutes = true;
    OptionItem useOrthogonalEdgeRoutesItem = layoutOptionHandler.addBool("Use Orthogonal Edge Routes", useOrthogonalEdgeRoutes);
    useOrthogonalEdgeRoutesItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        useOrthogonalEdgeRoutes = layoutOptionHandler.getBool("Use Orthogonal Edge Routes");
      }
    });

    backloopRoutingEnabled = true;
    OptionItem backloopRoutingEnabledItem = layoutOptionHandler.addBool("Enable Backloop Routing", backloopRoutingEnabled);
    backloopRoutingEnabledItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        backloopRoutingEnabled = layoutOptionHandler.getBool("Enable Backloop Routing");
      }
    });

    edgeStraighteningOptimizationEnabled = true;
    OptionItem edgeStraighteningOptimizationEnabledItem = layoutOptionHandler.addBool("Enable Edge Straightening", edgeStraighteningOptimizationEnabled);
    edgeStraighteningOptimizationEnabledItem.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        edgeStraighteningOptimizationEnabled = layoutOptionHandler.getBool("Enable Edge Straightening");
      }
    });

    return layoutOptionHandler;
  }

  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent e) {
      //determine critical edges
      Graph2D g = view.getGraph2D();
      EdgeMap edge2CriticalValue = Maps.createHashedEdgeMap();
      for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();        
        if (isCritical(edge, g)) {
          edge2CriticalValue.setDouble(edge, 1.0);
        } else {
          edge2CriticalValue.setDouble(edge, 0.0);
        }
      }

      //register critical edges
      g.addDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY, edge2CriticalValue);

      try {
        Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        executor.getLayoutMorpher().setSmoothViewTransform(true);
        executor.getLayoutMorpher().setEasedExecution(true);
        executor.doLayout(view, getHierarchicLayouter());
      } finally {

        g.removeDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);
      }
    }
  }

  private void markAsCriticalEdge(Edge e, Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    eRealizer.setLineColor(COLOR_CRITICAL_EDGE);
    eRealizer.setLineType(LineType.LINE_2);
  }

  private void unmarkEdge(Edge e,Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    eRealizer.setLineColor(COLOR_COMMON_EDGE);
    eRealizer.setLineType(LineType.LINE_1);
  }

  private boolean isCritical(Edge e, Graph2D g) {
    EdgeRealizer eRealizer = g.getRealizer(e);
    return (eRealizer.getLineColor() == COLOR_CRITICAL_EDGE);
  }

  class MarkSelectionAction extends AbstractAction {
    MarkSelectionAction() {
      super(" Mark Selected Edges ");
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      g.firePreEvent();
      g.backupRealizers();
      try {
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
          Edge edge = ec.edge();
          if (g.isSelected(edge)) {
            markAsCriticalEdge(edge, g);
          }
        }
        g.updateViews();
      } finally {
        g.firePostEvent();
      }
    }
  }

  class UnmarkSelectionAction extends AbstractAction {
    UnmarkSelectionAction() {
      super(" Unmark Selected Edges ");
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      g.firePreEvent();
      g.backupRealizers();
      try {
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
          Edge edge = ec.edge();
          if (g.isSelected(edge)) {
            unmarkEdge(edge, g);
          }
        }
        g.updateViews();
      } finally {
        g.firePostEvent();
      }
    }
  }

  class MarkShortestPathBetweenNodes extends AbstractAction {
    MarkShortestPathBetweenNodes() {
      super(" Mark Path Between Two Nodes ");
    }

    public void actionPerformed(ActionEvent ae) {
      Graph2D g = view.getGraph2D();
      g.firePreEvent();
      g.backupRealizers();
      try {
        NodeList selectedNodes = new NodeList(g.selectedNodes());
        if (!selectedNodes.isEmpty()) {
          EdgeMap path = Maps.createHashedEdgeMap();
          Node n1 = selectedNodes.firstNode();
          Node n2 = selectedNodes.lastNode();
          ShortestPaths.findShortestUniformPaths(g, n1, n2, true, path);
          if (!foundPath(g, path)) {
            ShortestPaths.findShortestUniformPaths(g, n2, n1, true, path);
          }
          for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
            Edge e = ec.edge();
            if (path.getBool(e)) {
              markAsCriticalEdge(e, g);
            } else {
              unmarkEdge(e, g);
            }
          }
          g.updateViews();
        }
      } finally {
        g.firePostEvent();
      }
    }
  }

  class MarkLongestPath extends AbstractAction {
    MarkLongestPath() {
      super(" Mark Longest Path ");
    }

    public void actionPerformed(ActionEvent ae) {
      Graph2D g = view.getGraph2D();
      g.firePreEvent();
      g.backupRealizers();
      try {
        //reset marks
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
          unmarkEdge(ec.edge(), g);
        }

        //make acyclic
        final EdgeMap cycleEdges = Maps.createIndexEdgeMap(new boolean[g.E()]);
        Cycles.findCycleEdges(g, cycleEdges);
        GraphHider hider = new GraphHider(g);
        hider.hide(new EdgeList(g.edges(), cycleEdges));

        //mark edges of longest path
        for (EdgeCursor ec = Paths.findLongestPath(g).edges(); ec.ok(); ec.next()) {
          markAsCriticalEdge(ec.edge(), g);
        }
        hider.unhideAll();
        g.updateViews();
      } finally {
        g.firePostEvent();
      }
    }
  }

  private boolean foundPath(LayoutGraph g, EdgeMap path) {
    for(EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
      if(path.getBool(ec.edge())) {
        return true;
      }
    }
    return false;
  }
  
  private JComponent createOptionTable(OptionHandler oh) {
    //Create editor and add associate Option Handler with the editor
    TableEditorFactory tef = new TableEditorFactory();
    oh.setAttribute(TableEditorFactory.ATTRIBUTE_INFO_POSITION, TableEditorFactory.InfoPosition.NONE);
    final Editor editor = tef.createEditor(oh);

    JComponent optionPane = editor.getComponent();
    optionPane.setPreferredSize(new Dimension(200, 100));
    optionPane.setMinimumSize(new Dimension(200, 100));
    optionPane.setMaximumSize(new Dimension(250, 100));
    return optionPane;
  }

  private Layouter getHierarchicLayouter() {
    IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setBackloopRoutingEnabled(backloopRoutingEnabled);
    if(layouter.getNodePlacer() instanceof SimplexNodePlacer) {
      ((SimplexNodePlacer) layouter.getNodePlacer()).setEdgeStraighteningOptimizationEnabled(edgeStraighteningOptimizationEnabled);
    }
    layouter.setOrthogonallyRouted(useOrthogonalEdgeRoutes);
    layouter.setMinimumLayerDistance(minimalLayerDistance);
    layouter.setNodeToNodeDistance(minimalNodeDistance);
    return layouter;
  }

   protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new ViewMode() {
      /** A mouse button get clicked */
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Graph2D g = view.getGraph2D();
        if (e.getClickCount() == 2) {
          final EdgeCursor selectedEdges = g.selectedEdges();
          if (selectedEdges != null && selectedEdges.size() > 0) {
            //Toggle color for all selected edges
            for (; selectedEdges.ok(); selectedEdges.next()) {              
              if(isCritical(selectedEdges.edge(), g)) {
                unmarkEdge(selectedEdges.edge(), g);
              } else {
                markAsCriticalEdge(selectedEdges.edge(), g);
              }
            }
          }
          view.updateView();
        }
      }
    });
  }

  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new CriticalPathDemo("resource/criticalpathhelp.html")).start("Critical Path Demo");
      }
    });
  }
}