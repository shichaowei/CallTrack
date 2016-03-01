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

import demo.view.hierarchy.GroupingDemo;
import y.base.DataMap;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.Maps;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;


/**
 * This demo showcases how IncrementalHierarchicLayouter can be used to fully or incrementally
 * layout hierarchically nested graphs. The demo supports automatic relayout after expanding folder nodes,
 * collapsing group nodes. Furthermore it provides toolbar buttons that
 * trigger full layout and incremental relayout. A settings dialog for group layout options is provided as well.
 * In incremental layout mode all selected elements are added incrementally to the existing layout.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html#ihl_hierarchically_organized_graphs">Section Layout of Grouped Graphs</a> in the yFiles for Java Developer's Guide
 */
public class IncrementalHierarchicGroupDemo extends GroupingDemo {

  IncrementalHierarchicLayouter layouter;
  OptionHandler groupLayoutOptions;

  public IncrementalHierarchicGroupDemo() {

    //configure layout algorithm
    layouter = new IncrementalHierarchicLayouter();
    layouter.setOrthogonallyRouted(true);
    layouter.setRecursiveGroupLayeringEnabled(false);

    //prepare option handler for group layout options
    Object[] groupStrategyEnum = {"Global Layering", "Recursive Layering"};
    Object[] groupAlignmentEnum = {"Top", "Center", "Bottom"};
    groupLayoutOptions = new OptionHandler("Layout Properties");
    ConstraintManager cm = new ConstraintManager(groupLayoutOptions);
    OptionItem gsi = groupLayoutOptions.addEnum("Group Layering Strategy", groupStrategyEnum, 0);
    OptionItem eci = groupLayoutOptions.addBool("Enable Compact Layering", true);
    OptionItem gai = groupLayoutOptions.addEnum("Group Alignment", groupAlignmentEnum, 0);
    cm.setEnabledOnValueEquals(gsi, "Recursive Layering", eci);
    cm.setEnabledOnValueEquals(gsi, "Recursive Layering", gai);
    cm.setEnabledOnCondition(cm.createConditionValueEquals(gsi, "Recursive Layering").and(
        cm.createConditionValueEquals(eci, Boolean.TRUE).inverse()), gai);

    view.fitContent();
  }

  /**
   * Register custom open folder and close group actions that adjust the layout of the graph.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    view.getCanvasComponent().getActionMap().put(Graph2DViewActions.CLOSE_GROUPS, new CloseGroupsAndLayoutAction());
    view.getCanvasComponent().getActionMap().put(Graph2DViewActions.OPEN_FOLDERS, new OpenFoldersAndLayoutAction());
  }

  /**
   * Expand a folder node. After expanding the folder node, an incremental layout is automatically triggered.
   * For this, the expanded node and all of its descendants will be treated as incremental elements.
   */
  class OpenFoldersAndLayoutAction extends Graph2DViewActions.OpenFoldersAction {

    OpenFoldersAndLayoutAction() {
      super(IncrementalHierarchicGroupDemo.this.view);
    }

    public void openFolder(Node folderNode, Graph2D graph) {
      NodeList children = new NodeList(graph.getHierarchyManager().getInnerGraph(folderNode).nodes());
      super.openFolder(folderNode, graph);

      // create storage for both nodes and edges
      DataMap incrementalElements = Maps.createHashedDataMap();
      // configure the mode
      final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

      for (NodeCursor nc = children.nodes(); nc.ok(); nc.next()) {
        incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
      }

      layoutIncrementally(incrementalElements);

      graph.setSelected(folderNode, true);
      graph.updateViews();
     
    }
  }

  /**
   * Collapse a group node. After collapsing the group node, an incremental layout is automatically triggered.
   * For this, the collapsed node is treated as an incremental element.
   */
  class CloseGroupsAndLayoutAction extends Graph2DViewActions.CloseGroupsAction {

    CloseGroupsAndLayoutAction() {
      super(IncrementalHierarchicGroupDemo.this.view);
    }

    public void closeGroup(Node groupNode, Graph2D graph) {
      super.closeGroup(groupNode, graph);

      // create storage for both nodes and edges
      DataMap incrementalElements = Maps.createHashedDataMap();
      // configure the mode
      final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

      for (EdgeCursor ec = groupNode.edges(); ec.ok(); ec.next()) {
        incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
      }

      layoutIncrementally(incrementalElements);

      graph.updateViews();      
    }
  }
  
  
  /**
   * Loads the initial graph
   */
  protected void loadInitialGraph() {
    loadGraph("resource/IncrementalHierarchicGroupDemo.graphml");
  }

  /**
   * Creates the toolbar for the demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();    
    addLayoutActions(toolBar);    
    return toolBar;
  }

  protected void addLayoutActions(JToolBar toolBar) {
    final Action incrementalLayoutAction = new AbstractAction(
            "Incremental", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        layoutIncrementally();
      }
    };

    final Action layoutAction = new AbstractAction(
            "Complete", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        layout();
      }
    };

    final Action propertiesAction = new AbstractAction(
            "Settings...", getIconResource("resource/properties.png")) {
      public void actionPerformed(ActionEvent e) {
        final ActionListener layoutListener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            layout();
          }
        };
        OptionSupport.showDialog(groupLayoutOptions, layoutListener, false, view.getFrame());
        configureGroupLayout();
      }
    };

    toolBar.addSeparator();
    toolBar.add(new JLabel("Layout: "));
    toolBar.add(createActionControl(incrementalLayoutAction));
    toolBar.add(createActionControl(layoutAction));
    toolBar.add(createActionControl(propertiesAction));
  }

  /**
   * Configures the layouter options relevant for grouping.
   */
  void configureGroupLayout() {
    Object gsi = groupLayoutOptions.get("Group Layering Strategy");
    if ("Recursive Layering".equals(gsi)) {
      layouter.setRecursiveGroupLayeringEnabled(true);
    } else if ("Global Layering".equals(gsi)) {
      layouter.setRecursiveGroupLayeringEnabled(false);
    }

    layouter.setGroupCompactionEnabled(groupLayoutOptions.getBool("Enable Compact Layering"));

    Object gai = groupLayoutOptions.get("Group Alignment");
    if ("Top".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_TOP);
    } else if ("Center".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_CENTER);
    }
    if ("Bottom".equals(gai)) {
      layouter.setGroupAlignmentPolicy(IncrementalHierarchicLayouter.POLICY_ALIGN_GROUPS_BOTTOM);
    }
  }

  /**
   * Performs incremental layout. All selected elements will be treated incrementally.
   */
  void layoutIncrementally() {
    Graph2D graph = view.getGraph2D();

    // create storage for both nodes and edges
    DataMap incrementalElements = Maps.createHashedDataMap();
    // configure the mode
    final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();

    for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
      incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
    }

    for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
      incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
    }

    layoutIncrementally(incrementalElements);
  }

  /**
   * Performs incremental layout. The given data map indicates the elements to treat incrementally.
   */
  void layoutIncrementally(DataMap incrementalElements) {
    Graph2D graph = view.getGraph2D();

    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);

    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
    try {
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
      layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
      layoutExecutor.doLayout(view, layouter);
    } finally {
      graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
    }
  }


  /**
   * Performs global layout. The new layout can strongly differ from the existing layout.
   */
  void layout() {
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
    layoutExecutor.doLayout(view, layouter);
  }

  /**
   * Launches this demo.
   * @param args ignored.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new IncrementalHierarchicGroupDemo()).start();
      }
    });
  }
}
