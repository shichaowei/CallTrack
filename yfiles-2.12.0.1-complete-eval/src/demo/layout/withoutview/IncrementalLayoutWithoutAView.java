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

package demo.layout.withoutview;

import java.util.HashMap;
import java.awt.EventQueue;

import y.base.DataMap;
import y.geom.YPoint;
import y.layout.*;
import y.layout.DefaultLayoutGraph;
import y.layout.BufferedLayouter;
import y.util.D;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IncrementalHintsFactory;
import y.util.Maps;

/**
 * This demo shows how to use the incremental hierarchical layout algorithm
 * without using classes that are only present in the yFiles Viewer Distribution. 
 * In this demo, first a graph will be laid out from scratch. Then new graph elements
 * will be added to the graph structure. Finally, an updated layout for the
 * grown graph structure will be calculated. The updated layout
 * will still look similar to the original layout. This feature
 * is called incremental layout.
 * <br>
 * This demo displays the calculated coordinates before and
 * after the incremental layout step in a simple graph viewer.
 * Additionally it outputs the calculated coordinates of the graph layout to
 * the console.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/incremental_hierarchical_layouter.html">Section Hierarchical Layout Style</a> in the yFiles for Java Developer's Guide
 */
public class IncrementalLayoutWithoutAView
{
  
  /**
   * Launcher
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        IncrementalLayoutWithoutAView lwv = new IncrementalLayoutWithoutAView();
        lwv.doit();
      }
    });
  }
  
  /**
   * Uses IncrementalHierarchicLayouter to perform an incremental layout of a graph.
   */
  public void doit()
  {
    DefaultLayoutGraph graph = new DefaultLayoutGraph();
    
    //construct graph. assign sizes to nodes
    Node v1 = graph.createNode();
    graph.setSize(v1,30,30);
    Node v2 = graph.createNode();
    graph.setSize(v2,30,30);
    Node v3 = graph.createNode();
    graph.setSize(v3,30,30);
    
    Edge e1 = graph.createEdge(v1,v2);
    Edge e2 = graph.createEdge(v1,v3);
 
    //optionally setup some port constraints for HierarchicLayouter
    EdgeMap spc = graph.createEdgeMap();
    EdgeMap tpc = graph.createEdgeMap();
    //e1 shall leave and enter the node on the right side
    spc.set(e1, PortConstraint.create(PortConstraint.EAST));
    //additionally set a strong port constraint on the target side. 
    tpc.set(e1, PortConstraint.create(PortConstraint.EAST, true));
    //ports with strong port constraints will not be reset by the 
    //layouter.  So we specify the target port right now to connect 
    //to the upper left corner of the node 
    graph.setTargetPointRel(e1, new YPoint(15, -15));
    
    //e2 shall leave and enter the node on the top side
    spc.set(e2, PortConstraint.create(PortConstraint.NORTH));
    tpc.set(e2, PortConstraint.create(PortConstraint.NORTH));
    
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, spc);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tpc);
    
    IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
    
    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp1 = new LayoutPreviewPanel(new CopiedLayoutGraph(graph));
    lpp1.createFrame("Hierarchical").setVisible(true);
    
    D.bug("\n\nGRAPH LAID OUT HIERARCHICALLY FROM SCRATCH");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));

    //now add a node and two edges incrementally...
    Node v4 = graph.createNode();
    graph.setSize(v4,30,30);
    
    Edge e3 = graph.createEdge(v1,v4);
    Edge e4 = graph.createEdge(v4,v2);
    
    IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();
    DataMap map = Maps.createDataMap(new HashMap());
    
    map.set(v4, ihf.createLayerIncrementallyHint(v4));
    map.set(e3, ihf.createSequenceIncrementallyHint(e3));
    map.set(e4, ihf.createSequenceIncrementallyHint(e4));
    
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, map);
    layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    
    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp2 = new LayoutPreviewPanel(graph);
    lpp2.createFrame("Hierarchical with incrementally added elements").setVisible(true);

    D.bug("\n\nGRAPH AFTER ELEMENTS HAVE BEEN ADDED INCREMENTALLY");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("v4 center position = " + graph.getCenter(v4));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));
    D.bug("e3 path = " + graph.getPath(e3));
    D.bug("e4 path = " + graph.getPath(e4));
  }
}
