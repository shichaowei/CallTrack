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
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.TopologicalLayerer;
import y.layout.hierarchic.incremental.LayerConstraintFactory;
import y.layout.hierarchic.incremental.ConstraintIncrementalLayerer;
import y.util.GraphCopier;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DClipboard;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Demo that shows how to apply layer constraints when calculating hierarchical layouts.
 * <p> With the buttons on the left side of the GUI,
 * various constraints can be set on the currently selected nodes (either absolute top/bottom level or relative layering
 * constraints). The "Top-most"/"Bottom-most" buttons set absolute layering constraints, whereas the other buttons assign
 * relative layering constraints. The top button ("Remove constraints") clears all constraints from the currently
 * selected nodes.
 * </p>
 * <p>
 * Additionally, a DataProvider is registered under the key <code>ConstraintIncrementalLayerer.EDGE_WEIGHTS_DPKEY</code>, and if a numeric edge label is set, that label gets set as
 * value for that DataProvider.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html#incremental_hierarchical_layer_assignment_constraints">Section Constrained Layer Assignment</a> in the yFiles for Java Developer's Guide
 */
public class LayerConstraintsDemo extends DemoBase {

  public static final int TOP_LEVEL = 1;
  public static final int BOTTOM_LEVEL = 5;

  private NodeMap levels;
  private EdgeMap weights;

  public LayerConstraintsDemo() {
    super();    
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);

    levels = graph.createNodeMap();

    //additionally, register an edge weight map
    weights = graph.createEdgeMap();
    graph.addDataProvider(ConstraintIncrementalLayerer.EDGE_WEIGHTS_DPKEY, weights);

    JPanel left = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;

    JPanel groupSpec;
    groupSpec = new JPanel(new GridBagLayout());
    groupSpec.setBorder(BorderFactory.createTitledBorder("Layering constraints"));

    // build the grouping mechanism

    Color[] groupColors = new Color[]{DemoDefaults.DEFAULT_CONTRAST_COLOR, Color.RED, Color.ORANGE, Color.yellow, new Color(204, 255, 0), Color.GREEN};
    String[] groupLabels = new String[]{"Remove constraint", "Top-most level", "Above medium level", "Medium level",
        "Below medium level",
        "Bottom-most level"};
    gbc.insets = new Insets(5, 0, 5, 0);
    for (int i = 0; i < groupColors.length; i++) {
      if (i > 1 && i < 5) {
        gbc.insets = new Insets(0, 0, 0, 0);
      } else {
        gbc.insets = new Insets(5, 0, 5, 0);
      }
      GroupButton groupButton = new GroupButton(groupColors[i], groupLabels[i], i);
      groupSpec.add(groupButton, gbc);
    }
    gbc.weightx = gbc.weighty = 1;
    groupSpec.add(new JPanel(), gbc);
    gbc.weightx = gbc.weighty = 0;
    gbc.gridwidth = 2;
    left.add(groupSpec, gbc);
    gbc.gridwidth = 1;

    gbc.weighty = 1.0d;
    left.add(new JPanel(), gbc);

    contentPane.add(new JScrollPane(left), BorderLayout.WEST);
    
    loadGraph("resource/LayerConstraintsDemo.graphml");
  }
    
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioh = super.createGraphMLIOHandler();
    ioh.getGraphMLHandler().addOutputDataProvider("level", levels, KeyScope.NODE, KeyType.INT);
    ioh.getGraphMLHandler().addInputDataAcceptor("level", levels, KeyScope.NODE, KeyType.INT);
    return ioh;
  }
  
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    view.getGraph2D().getDefaultNodeRealizer().setFillColor(DemoDefaults.DEFAULT_CONTRAST_COLOR);
  }

  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(new LayoutAction()));
    return toolBar;
  }

  protected Graph2DClipboard getClipboard() {
    final Graph2DClipboard clipboard = super.getClipboard();
    clipboard.setCopyFactory(new LayerConstraintGraphCopyFactory(clipboard.getCopyFactory()));
    return clipboard;
  }

  /** this method assigns the level id and the corresponding color hint to the currently selected nodes */
  protected void assignLevel(Color color, int index) {
    Graph2D graph = view.getGraph2D();

    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      Node n = nc.node();
      // set the color hint
      if (color != null) {
        graph.getRealizer(n).setFillColor(color);
      } else {
        graph.getRealizer(n).setFillColor(graph.getDefaultNodeRealizer().getFillColor());
      }
      levels.setInt(n, index);
    }
    graph.updateViews();
  }

  
  // helper class
  class GroupButton extends JButton implements ActionListener {
    Color color;
    int index;

    GroupButton(Color color, String groupLabel, int index) {
      super("");
      setText(groupLabel);
      this.color = color;
      this.index = index;
      setBackground(color);
      this.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
      LayerConstraintsDemo.this.assignLevel(color, index);
    }
  }

  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev) {
      Graph2D graph = view.getGraph2D();


      doLayout(graph);
      graph.updateViews();
      view.fitContent();
    }
  }

  private void doLayout(Graph2D graph) {
    IncrementalHierarchicLayouter hl = new IncrementalHierarchicLayouter();
    hl.setFromScratchLayeringStrategy(IncrementalHierarchicLayouter.LAYERING_STRATEGY_HIERARCHICAL_TOPMOST);
    hl.setOrthogonallyRouted(true);
    LayerConstraintFactory lcf = hl.createLayerConstraintFactory(graph);
    createConstraints(graph, lcf);

    TopologicalLayerer topologicalLayerer = new TopologicalLayerer();
    topologicalLayerer.setRankingPolicy(TopologicalLayerer.NO_RERANKING);
    hl.setFromScratchLayerer(topologicalLayerer);

    view.applyLayoutAnimated(hl);
  }

  /**
   * Assign constraints to nodes and edges
   *
   * @param graph
   * @param cf
   */
  private void createConstraints(Graph2D graph, LayerConstraintFactory cf) {
    Node flr, slr, thrdlr;
    flr = slr = thrdlr = null;
    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      int index = levels.getInt(node);
      switch (index) {
        case TOP_LEVEL:
          cf.addPlaceNodeAtTopConstraint(node);
          break;
        case BOTTOM_LEVEL:
          cf.addPlaceNodeAtBottomConstraint(node);
          break;
        case TOP_LEVEL + 1:
          if (flr == null) {
            flr = node;
          } else {
            cf.addPlaceNodeInSameLayerConstraint(flr, node);
          }
          break;
        case TOP_LEVEL + 2:
          if (slr == null) {
            slr = node;
          } else {
            cf.addPlaceNodeInSameLayerConstraint(slr, node);
          }
          break;
        case TOP_LEVEL + 3:
          if (thrdlr == null) {
            thrdlr = node;
          } else {
            cf.addPlaceNodeInSameLayerConstraint(thrdlr, node);
          }
          break;
        default:
          break;
      }
      if (flr != null && slr != null) {
        //place second layer below first layer
        cf.addPlaceNodeBelowConstraint(flr, slr);
      }
      if (slr != null && thrdlr != null) {
        //place thrd layer below second layer
        cf.addPlaceNodeBelowConstraint(slr, thrdlr);
      }
      if (flr != null && thrdlr != null) {
        //place first layer above 3rd layer
        cf.addPlaceNodeAboveConstraint(thrdlr, flr);
      }
    }

    //assign weights from edge labels
    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
      Edge edge = edgeCursor.edge();
      if (graph.getRealizer(edge).labelCount() > 0) {
        String str = graph.getLabelText(edge);
        try {
          weights.setInt(edge, Integer.parseInt(str));
        }
        catch (NumberFormatException e) {
          weights.setInt(edge, 1);
        }
      }
      else {
        weights.setInt(edge, 1);
      }
    }
  }


  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new LayerConstraintsDemo()).start("Layer Constraints Demo");
      }
    });
  }

  /**
   * This {@link y.util.GraphCopier.CopyFactory} handles the level of the nodes in the graphs for cut/copy/paste.
   */
  private class LayerConstraintGraphCopyFactory implements GraphCopier.CopyFactory {
    private final GraphCopier.CopyFactory copyFactory;
    private final HashMap node2level;

    public LayerConstraintGraphCopyFactory(GraphCopier.CopyFactory copyFactory) {
      this.copyFactory = copyFactory;
      node2level = new HashMap();
    }

    public Node copyNode(Graph targetGraph, Node originalNode) {
      return copyFactory.copyNode(targetGraph, originalNode);
    }

    public Edge copyEdge(Graph targetGraph, Node newSource, Node newTarget, Edge originalEdge) {
      return copyFactory.copyEdge(targetGraph, newSource, newTarget, originalEdge);
    }

    public Graph createGraph() {
      return copyFactory.createGraph();
    }

    public void preCopyGraphData(Graph sourceGraph, Graph targetGraph) {
      copyFactory.preCopyGraphData(sourceGraph, targetGraph);
    }

    /**
     * After copying the (sub-)graph, also the levels need to be stored/updated. That way, copies of nodes that
     * have a certain layer constraint will get the same layer constraint.
     */
    public void postCopyGraphData(Graph sourceGraph, Graph targetGraph, Map nodeMap, Map edgeMap) {
      copyFactory.postCopyGraphData(sourceGraph, targetGraph, nodeMap, edgeMap);

      // check if the source graph is the graph in the current view to see if it is a cut/copy or paste action
      if (sourceGraph == view.getGraph2D()) {
        // cut/copy
        // store level information from the source nodes for the nodes in the copied subgraph
        node2level.clear();
        for (NodeCursor nodeCursor = sourceGraph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
          final Node sourceNode = nodeCursor.node();
          final Node targetNode = (Node) nodeMap.get(sourceNode);
          if (targetNode != null) {
            node2level.put(targetNode, levels.get(sourceNode));
          }
        }
      } else {
        // paste
        // store level information of the source nodes for the nodes in the copied subgraph
        for (NodeCursor nodeCursor = sourceGraph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
          final Node sourceNode = nodeCursor.node();
          final Node targetNode = (Node) nodeMap.get(sourceNode);
          levels.set(targetNode, node2level.get(sourceNode));
        }
      }
    }
  }

}