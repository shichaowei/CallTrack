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

import y.base.Node;
import y.base.NodeMap;
import y.base.YList;
import y.base.NodeCursor;
import y.util.Comparators;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;
import y.view.hierarchy.GroupLayoutConfigurator;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.grouping.Grouping;
import y.layout.Layouter;
import y.layout.PortCandidate;
import y.layout.LayoutOrientation;
import y.layout.LayoutMultiplexer;
import y.layout.LayoutGraph;
import y.layout.tree.TreeReductionStage;
import y.layout.tree.TreeLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.GivenLayersLayerer;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.HierarchicLayouter;
import y.layout.hierarchic.incremental.LayerConstraintFactory;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.util.Maps;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import demo.view.hierarchy.GroupingDemo;

/**
 * Shows how to use the Recursive Group Layouter to apply distinct layout styles to different group nodes.
 * <p/>
 * Table-like Example. A table-like structure: each group represents a table, the regular nodes represent the table
 * rows, and edges are connected to specific rows. The rows are sorted according to their y-coordinate in the initial
 * drawing.
 * <p/>
 * Three-Tier Example. Distinct layouts of elements assigned to different tiers. Each group can be assigned to either
 * the left, right or middle tier (depending on a group's label). '<code>left</code>' groups are drawn using a
 * TreeLayouter with orientation left-to-right. Analogously, '<code>right</code>' groups are drawn using a TreeLayouter
 * with orientation right-to-left. Elements not labeled 'left' or 'right' are laid out in the middle using a hierarchic
 * layout with orientation left-to-right. Note that groups not labeled 'left' or 'right' are handled non-recursively.
 */
public class MixedLayoutDemo extends GroupingDemo {
  static final int TABLE_MODE = 0;
  static final int THREE_TIER_MODE = 1;

  private static final byte COMMON_NODE = 0;
  private static final byte LEFT_TREE_GROUP_NODE = 1;
  private static final byte LEFT_TREE_CONTENT_NODE = 2;
  private static final byte RIGHT_TREE_GROUP_NODE = 3;
  private static final byte RIGHT_TREE_CONTENT_NODE = 4;

  int mode;
  boolean fromSketch;

  public MixedLayoutDemo() {
    this(null);
  }

  public MixedLayoutDemo( final String helpFilePath ) {
    super();
    addHelpPane(helpFilePath);
  }

  protected void loadInitialGraph() {
    loadGraph("resource/threetier.graphml");
  }

  protected String[] getExampleResources() {
    return new String[]{"resource/threetier.graphml", "resource/table.graphml"};
  }

  protected JToolBar createToolBar() {
    final AbstractAction fromSketchAction = new AbstractAction("From Sketch") {
      public void actionPerformed(ActionEvent e) {
        fromSketch = ((AbstractButton) e.getSource()).isSelected();
      }
    };
    fromSketchAction.putValue(Action.SHORT_DESCRIPTION, "Toggles the 'From Sketch' mode of the layout");

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(new LayoutAction()));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(new JToggleButton(fromSketchAction));
    return toolBar;
  }

  protected void loadGraph(String resourceString) {
    mode = "resource/threetier.graphml".equals(resourceString) ? THREE_TIER_MODE : TABLE_MODE;
    super.loadGraph(resourceString);
  }

  /**
   * Register key bindings for our custom actions.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.put(Graph2DViewActions.CLOSE_GROUPS, new MyCloseGroupsAction());
    actionMap.put(Graph2DViewActions.OPEN_FOLDERS, new MyOpenFoldersAction());
  }

  //action performs common behavior and applies a layout afterwards
  class MyCloseGroupsAction extends Graph2DViewActions.CloseGroupsAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  //action performs common behavior and applies a layout afterwards
  class MyOpenFoldersAction extends Graph2DViewActions.OpenFoldersAction {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      doLayout();
    }
  }

  /**
   * Layout action that configures and launches a layout algorithm.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent e) {
      doLayout();
    }
  }

  private void doLayout() {
    if (mode == THREE_TIER_MODE) {
      applyThreeTierLayout();
    } else {
      applyTableLayout();
    }
  }

  /** Configures and invokes the table layout algorithm */
  void applyTableLayout() {
    Graph2D graph = view.getGraph2D();

    //set up port candidates for edges (edges should be attached to the left/right side of the corresponding nodes)
    YList candidates = new YList();
    candidates.add(PortCandidate.createCandidate(PortCandidate.WEST));
    candidates.add(PortCandidate.createCandidate(PortCandidate.EAST));
    graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));
    graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));

    //configure layout algorithms
    final RowLayouter rowLayouter = new RowLayouter(); //used for layouting the nodes (rows) within the group nodes (tables)

    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter(); //used for the core layout
    ihl.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    if (fromSketch) {
      ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    }
    ihl.setOrthogonallyRouted(true);

    //map each group node to its corresponding layout algorithm
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return rowLayouter;
      }
    });

    //prepare grouping information
    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph);
    try {
      glc.prepareAll();

      //do layout
      RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(ihl);
      rgl.setAutoAssignPortCandidatesEnabled(true);
      rgl.setConsiderSketchEnabled(true);
      new Graph2DLayoutExecutor().doLayout(view, rgl);
    } finally {
      //dispose
      glc.restoreAll();
      graph.removeDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY);
      graph.removeDataProvider(PortCandidate.TARGET_PCLIST_DPKEY);
      graph.removeDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY);
    }

    view.updateView();
    view.fitContent();
  }

  /**
   * Determines the type of a node (used for the subgraph layout demo).
   */
  private static byte getType(Node n, Grouping grouping, Graph2D graph) {
    if (grouping.isGroupNode(n)) {
      NodeRealizer realizer = graph.getRealizer(n);
      if ("left".equals(realizer.getLabelText())) {
        return LEFT_TREE_GROUP_NODE;
      } else if ("right".equals(realizer.getLabelText())) {
        return RIGHT_TREE_GROUP_NODE;
      } else {
        return COMMON_NODE;
      }
    } else {
      Node groupNode = grouping.getParent(n);
      if (groupNode != null) {
        NodeRealizer realizer = graph.getRealizer(groupNode);
        if ("left".equals(realizer.getLabelText())) {
          return LEFT_TREE_CONTENT_NODE;
        } else if ("right".equals(realizer.getLabelText())) {
          return RIGHT_TREE_CONTENT_NODE;
        } else {
          return COMMON_NODE;
        }
      } else {
        NodeRealizer realizer = graph.getRealizer(n);
        if ("left".equals(realizer.getLabelText())) {
          return LEFT_TREE_GROUP_NODE;
        } else if ("right".equals(realizer.getLabelText())) {
          return RIGHT_TREE_GROUP_NODE;
        } else {
          return COMMON_NODE;
        }
      }
    }
  }

  /**
   * Configures and invokes a layout algorithm
   */
  void applyThreeTierLayout() {
    final Graph2D graph = view.getGraph2D();

    //configure the different layout settings
    final TreeReductionStage leftToRightTreeLayouter = new TreeReductionStage();
    leftToRightTreeLayouter.setNonTreeEdgeRouter(leftToRightTreeLayouter.createStraightlineRouter());
    TreeLayouter tl1 = new TreeLayouter();
    tl1.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    tl1.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
    leftToRightTreeLayouter.setCoreLayouter(tl1);

    final TreeReductionStage rightToLeftTreeLayouter = new TreeReductionStage();
    rightToLeftTreeLayouter.setNonTreeEdgeRouter(rightToLeftTreeLayouter.createStraightlineRouter());
    TreeLayouter tl2 = new TreeLayouter();
    tl2.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    tl2.setLayoutStyle(TreeLayouter.ORTHOGONAL_STYLE);
    rightToLeftTreeLayouter.setCoreLayouter(tl2);

    final IncrementalHierarchicLayouter partitionLayouter = new IncrementalHierarchicLayouter(); //configure the core layout
    partitionLayouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    if (fromSketch) {
      partitionLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    }

    GroupLayoutConfigurator glc = new GroupLayoutConfigurator(graph); //prepare the grouping information
    glc.prepareAll();
    final Grouping grouping = new Grouping(graph);

    if (!fromSketch) {
      //insert layer constraints to guarantee the desired placement for "left" and "right" group nodes
      LayerConstraintFactory lcf = partitionLayouter.createLayerConstraintFactory(graph);
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        byte type = getType(n, grouping, graph);
        if (type == LEFT_TREE_GROUP_NODE) {
          lcf.addPlaceNodeAtTopConstraint(n);
        } else if (type == RIGHT_TREE_GROUP_NODE) {
          lcf.addPlaceNodeAtBottomConstraint(n);
        }
      }
    }

    //align tree group nodes within their layer
    NodeMap node2LayoutDescriptor = Maps.createHashedNodeMap();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      byte type = getType(n, grouping, graph);
      if (type == LEFT_TREE_GROUP_NODE) {
        NodeLayoutDescriptor nld = new NodeLayoutDescriptor();
        nld.setLayerAlignment(1.0d);
        node2LayoutDescriptor.set(n, nld);
      } else if (type == RIGHT_TREE_GROUP_NODE) {
        NodeLayoutDescriptor nld = new NodeLayoutDescriptor();
        nld.setLayerAlignment(0.0d);
        node2LayoutDescriptor.set(n, nld);
      }
    }
    graph.addDataProvider(HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY, node2LayoutDescriptor);

    //map each group node to the layout algorithm that should be used for its content
    graph.addDataProvider(RecursiveGroupLayouter.GROUP_NODE_LAYOUTER_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        byte type = getType((Node) dataHolder, grouping, graph);
        if (type == LEFT_TREE_GROUP_NODE) {
          return leftToRightTreeLayouter;
        } else if (type == RIGHT_TREE_GROUP_NODE) {
          return rightToLeftTreeLayouter;
        } else {
          return null; //handled non-recursive
        }
      }
    });

    //each edge should be attached to the left or right side of the corresponding node
    final YList candidates = new YList();
    candidates.add(PortCandidate.createCandidate(PortCandidate.WEST));
    candidates.add(PortCandidate.createCandidate(PortCandidate.EAST));
    graph.addDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));
    graph.addDataProvider(PortCandidate.TARGET_PCLIST_DPKEY, DataProviders.createConstantDataProvider(candidates));

    //launch layout algorithm
    RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(partitionLayouter);

    try {
      rgl.setAutoAssignPortCandidatesEnabled(true);
      rgl.setConsiderSketchEnabled(true);

      new Graph2DLayoutExecutor().doLayout(view, rgl);

    } finally {
      //dispose
      grouping.dispose();
      glc.restoreAll();
      graph.removeDataProvider(PortCandidate.SOURCE_PCLIST_DPKEY);
      graph.removeDataProvider(PortCandidate.TARGET_PCLIST_DPKEY);
      graph.removeDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY);
      graph.removeDataProvider(HierarchicLayouter.NODE_LAYOUT_DESCRIPTOR_DPKEY);
      graph.removeDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
    }

    view.updateView();
    view.fitContent();
  }

  /**
   * Layouts the nodes (rows) within the group nodes (tables).
   */
  static class RowLayouter implements Layouter {
    private static final double DISTANCE = 5.0;

    public boolean canLayout(LayoutGraph graph) {
      return graph.edgeCount() == 0;
    }

    public void doLayout(final LayoutGraph graph) {
      Node[] rows = graph.getNodeArray();
      Arrays.sort(rows, new Comparator() {
        public int compare(Object o1, Object o2) {
          return Comparators.compare(graph.getCenterY((Node) o1), graph.getCenterY((Node) o2));
        }
      });

      double currentY = 0.0;
      for (int i = 0; i < rows.length; i++) {
        //set layout of row
        graph.setLocation(rows[i], 0.0, currentY);
        currentY += graph.getHeight(rows[i]) + DISTANCE;
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new MixedLayoutDemo("resource/mixedlayouthelp.html")).start();
      }
    });
  }
}
