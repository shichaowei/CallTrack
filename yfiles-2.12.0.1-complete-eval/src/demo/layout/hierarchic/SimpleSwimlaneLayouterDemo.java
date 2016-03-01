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

import y.base.DataMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.hierarchic.incremental.SwimLaneDescriptor;
import y.util.Maps;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.PopupMode;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * <p>
 * This simple demo shows how to use the swim lane feature of the 
 * new {@link y.layout.hierarchic.IncrementalHierarchicLayouter}.
 * </p>
 * <p>
 * It can either calculate a new layout or calculate a new layout given the current 
 * sketch or incrementally layout selected nodes to an already existing graph whose
 * layout is read from the current sketch.  
 * </p>
 * <p>
 * Things to try:<br/>
 * Create a graph and assign nodes to layers by editing the label of the nodes.
 * Nodes with the same layer will be placed into the same swim lane. Swim lanes
 * are sorted from left to right in ascending label order.
 * <br/>
 * Use the <b>Layout</b> button to lay it out from scratch.
 * Modify the graph (move nodes and or bends), deselect all elements and 
 * choose <b>Layout from Sketch</b> to recalculate the layout using the given sketch
 * Add some nodes and connect them to the graph, select the newly added nodes
 * and choose <b>Layout Incrementally</b> to incrementally "add" the selected 
 * elements optimally into the existing graph.
 * </p>
 */
public class SimpleSwimlaneLayouterDemo extends DemoBase {
  /** Used to store the hints for the incremental layout */
  private DataMap hintMap;

  /** Used to store the swim lane information for each node */
  private NodeMap swimLaneMap;

  /** the layouter and the hints factory */
  private IncrementalHierarchicLayouter hierarchicLayouter;
  private IncrementalHintsFactory hintsFactory;

  /** the drawable for the swimlanes */
  private SwimlaneDrawable swimLaneDrawable;

  public SimpleSwimlaneLayouterDemo() {
    this (null);
  }

  public SimpleSwimlaneLayouterDemo(String helpFilePath) {
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);

    // create a map to store the hints for the incremental layout mechanism
    hintMap = Maps.createHashedDataMap();
    swimLaneMap = graph.createNodeMap();
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, hintMap);
    graph.addDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY, swimLaneMap);

    // create the layouter
    hierarchicLayouter = new IncrementalHierarchicLayouter();

    // set some defaults
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumFirstSegmentLength(15);
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumLastSegmentLength(20);
    hierarchicLayouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_OCTILINEAR));
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumDistance(10.0d);

    hierarchicLayouter.getNodeLayoutDescriptor().setLayerAlignment(0.5d);
    hierarchicLayouter.setMinimumLayerDistance(30.0d);
    hierarchicLayouter.getNodeLayoutDescriptor().setNodeLabelMode(
        NodeLayoutDescriptor.NODE_LABEL_MODE_CONSIDER_FOR_DRAWING);
    hierarchicLayouter.setConsiderNodeLabelsEnabled(true);
    hierarchicLayouter.setBackloopRoutingEnabled(true);

    // set the node placer to barycenter mode so that the results are centered
    // nicely in the swimlanes if there is more room 
    ((SimplexNodePlacer) hierarchicLayouter.getNodePlacer()).setBaryCenterModeEnabled(false);

    // get a reference to a hints factory
    hintsFactory = hierarchicLayouter.createIncrementalHintsFactory();

    // disable the component layouter (optional)
    hierarchicLayouter.setComponentLayouterEnabled(false);

    // add a drawable to visualize the swim lane geometry
    view.addBackgroundDrawable(swimLaneDrawable = new SwimlaneDrawable(view.getGraph2D(), swimLaneMap));

    loadGraph("resource/swimlane.graphml");    
    DemoDefaults.applyRealizerDefaults(graph);
    
    addHelpPane(helpFilePath);

    calcLayout(hierarchicLayouter);
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    view.getGraph2D().getDefaultNodeRealizer().setSize(30, 30);
  }
  
  class LayoutFromSketchAction extends AbstractAction {
    LayoutFromSketchAction() {
      super("Layout From Sketch", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev) {
      calcIncrementalLayout(new NodeList().nodes());
    }
  }

  class LayoutIncrementallyAction extends AbstractAction {
    LayoutIncrementallyAction() {
      super("Layout Incrementally", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev) {
      calcIncrementalLayout(view.getGraph2D().selectedNodes());
    }
  }

  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev) {
      calcFreshLayout();
    }
  }

  protected JToolBar createToolBar() {
    JToolBar tb = super.createToolBar();
    tb.addSeparator();
    tb.add(createActionControl(new LayoutAction()));
    tb.add(createActionControl(new LayoutFromSketchAction()));
    tb.add(createActionControl(new LayoutIncrementallyAction()));
    return tb;
  }

  public void calcFreshLayout() {
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    calcLayout(hierarchicLayouter);
  }

  public void updateSwimLanes() {
    for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next()) {
      SwimLaneDescriptor sld = new SwimLaneDescriptor(view.getGraph2D().getLabelText(nc.node()));
      sld.setLeftLaneInset(5);
      sld.setRightLaneInset(5);
      sld.setMinimumLaneWidth(100);
      swimLaneMap.set(nc.node(), sld);
    }
  }

  public void calcIncrementalLayout(NodeCursor incrementalNodes) {
    try {
      // mark nodes as "new"
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next()) {
        hintMap.set(incrementalNodes.node(), hintsFactory.createLayerIncrementallyHint(incrementalNodes.node()));
      }
      // read the old nodes from the sketch
      hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
      // calculate the layout incrementally
      calcLayout(hierarchicLayouter);
    } finally {
      // reset the marks
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next()) {
        hintMap.set(incrementalNodes.node(), null);
      }
    }
  }

  protected void calcLayout(Layouter layouter) {
    Graph2D graph = view.getGraph2D();
    if (!graph.isEmpty()) {
      Cursor oldCursor = view.getViewCursor();
      try {
        // associate swim lane descriptors with each node...
        updateSwimLanes();
        view.applyLayoutAnimated(layouter);
      } finally {
        view.setViewCursor(oldCursor);
      }
    }
    swimLaneDrawable.updateLanes();
    view.fitContent();
    view.updateView();
  }

  protected EditMode createEditMode() {
    EditMode editMode = super.createEditMode();
    editMode.setPopupMode(new PopupMode() {
      public JPopupMenu getNodePopup(Node v) {
        return createPopup();
      }

      private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenu laneMenu = new JMenu("Move to lane");
        menu.add(laneMenu);
        laneMenu.add(new MoveSelectedNodesToLayerAction(1));
        laneMenu.add(new MoveSelectedNodesToLayerAction(2));
        laneMenu.add(new MoveSelectedNodesToLayerAction(3));
        laneMenu.add(new MoveSelectedNodesToLayerAction(4));
        laneMenu.add(new MoveSelectedNodesToLayerAction(5));
        laneMenu.add(new MoveSelectedNodesToLayerAction(6));
        return menu;
      }

      public JPopupMenu getSelectionPopup(double x, double y) {
        return createPopup();
      }
    });
    return editMode;
  }


  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SimpleSwimlaneLayouterDemo("resource/simpleswimlanelayouterdemohelp.html")).start("Simple Swimlane Layout Demo");
      }
    });
  }

  private class MoveSelectedNodesToLayerAction extends AbstractAction {
    private int layer;

    MoveSelectedNodesToLayerAction(int layer) {
      super(String.valueOf(layer));
      this.layer = layer;
    }

    public void actionPerformed(ActionEvent e) {
      for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        view.getGraph2D().setLabelText(node, String.valueOf(layer));
      }
      calcIncrementalLayout(view.getGraph2D().selectedNodes());
    }
  }
}