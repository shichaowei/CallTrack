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
import y.base.DataMap;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.util.Maps;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.hierarchy.HierarchyManager;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * This simple demo shows how to use the {@link IncrementalHierarchicLayouter}
 * to either calculate a new layout or calculate a new layout given the current
 * sketch or incrementally layout selected nodes to an already existing graph whose
 * layout is read from the current sketch. The layout algorithm uses an octilinear edge routing style,
 * i.e., the slope of each edge segment is a multiple of 45 degrees.
 * <br>
 * <br>
 * Things to try:
 * <br>
 * Create a graph and use the <b>Layout</b> button to lay it out from scratch.
 * Modify the graph (move nodes and or bends), deselect all elements and
 * choose <b>Layout From Sketch</b> to recalculate the layout using the given sketch
 * Add some nodes and connect them to the graph, select the newly added nodes
 * and choose <b>Layout Incrementally</b> to incrementally "add" the selected
 * elements optimally into the existing graph.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html#incremental_hierarchical_layouter">Section Hierarchical Layout Style</a> in the yFiles for Java Developer's Guide
 */
public class SimpleIncrementalHierarchicLayouterDemo extends DemoBase
{
  private DataMap hintMap;

  private IncrementalHierarchicLayouter hierarchicLayouter;
  private IncrementalHintsFactory hintsFactory;

  public SimpleIncrementalHierarchicLayouterDemo()
  {
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);

    // create a map to store the hints for the incremental layout mechanism
    hintMap = Maps.createHashedDataMap();
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, hintMap);

    // create the layouter
    hierarchicLayouter = new IncrementalHierarchicLayouter();

    // set some defaults
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumFirstSegmentLength(15);
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumLastSegmentLength(20);
    hierarchicLayouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_OCTILINEAR));
    hierarchicLayouter.getEdgeLayoutDescriptor().setMinimumDistance(10.0d);

    hierarchicLayouter.getNodeLayoutDescriptor().setLayerAlignment(0.5d);
    hierarchicLayouter.setMinimumLayerDistance(30.0d);
    hierarchicLayouter.getNodeLayoutDescriptor().setNodeLabelMode(NodeLayoutDescriptor.NODE_LABEL_MODE_CONSIDER_FOR_DRAWING);

    hierarchicLayouter.setConsiderNodeLabelsEnabled(true);

    ((SimplexNodePlacer) hierarchicLayouter.getNodePlacer()).setBaryCenterModeEnabled(true);

    // get a reference to a hints factory
    hintsFactory = hierarchicLayouter.createIncrementalHintsFactory();

    // disable the component layouter (optional)
    hierarchicLayouter.setComponentLayouterEnabled(false);

    loadGraph("resource/simple.graphml");
  }

  protected void initialize() {
    // create hierarchy manager before undo manager (for view actions)
    // to ensure undo/redo works for grouped graphs
    new HierarchyManager(view.getGraph2D());
  }

  class LayoutFromSketchAction extends AbstractAction
  {
    LayoutFromSketchAction()
    {
      super("Layout From Sketch", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcIncrementalLayout(new NodeList().nodes());
    }
  }

  class LayoutIncrementallyAction extends AbstractAction
  {
    LayoutIncrementallyAction()
    {
      super("Layout Incrementally", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcIncrementalLayout(view.getGraph2D().selectedNodes());
    }
  }

  class LayoutAction extends AbstractAction
  {
    LayoutAction()
    {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent ev)
    {
      calcFreshLayout();
    }
  }

  protected JToolBar createToolBar()
  {
    JToolBar tb = super.createToolBar();
    tb.addSeparator();
    tb.add(createActionControl(new LayoutAction()));
    tb.add(createActionControl(new LayoutFromSketchAction()));
    tb.add(createActionControl(new LayoutIncrementallyAction()));
    return tb;
  }

  public void calcFreshLayout()
  {
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    calcLayout(hierarchicLayouter);
  }

  public void calcIncrementalLayout(NodeCursor incrementalNodes)
  {
    try
    {
      // mark nodes as "new"
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next())
      {
        hintMap.set(incrementalNodes.node(), hintsFactory.createLayerIncrementallyHint(incrementalNodes.node()));
      }
      // read the old nodes from the sketch
      hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
      // calculate the layout incrementally
      calcLayout(hierarchicLayouter);
    }
    finally
    {
      // reset the marks
      for (incrementalNodes.toFirst(); incrementalNodes.ok(); incrementalNodes.next())
      {
        hintMap.set(incrementalNodes.node(), null);
      }
    }
  }

  protected void calcLayout(Layouter layouter)
  {
    Graph2D graph = view.getGraph2D();
    if (!graph.isEmpty())
    {
      Cursor oldCursor = view.getViewCursor();
      try
      {
        view.applyLayoutAnimated(layouter);
      }
      finally
      {
        view.setViewCursor(oldCursor);
      }
    }
    view.fitContent();
    view.updateView();
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SimpleIncrementalHierarchicLayouterDemo()).start("Simple IncrementalHierarchicLayouter Demo");
      }
    });
  }
}
