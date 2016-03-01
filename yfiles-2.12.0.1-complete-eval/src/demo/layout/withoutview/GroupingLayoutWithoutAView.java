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

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;

import y.layout.BufferedLayouter;
import y.layout.DefaultLayoutGraph;
import y.layout.PortConstraintKeys;
import y.layout.grouping.Grouping;
import y.layout.hierarchic.HierarchicGroupLayouter;

import y.util.D;

import java.awt.EventQueue;

/**
 * This class shows how to use layout and grouping algorithms without using classes 
 * that are only present in the yFiles Viewer Distribution. Therefore this demo
 * only outputs the calculated coordinates of the graph layout to the console
 * and displays it inside a simple preview panel.
 * <br>
 * In this demo HierarchicGroupLayouter is used to layout a small graph. 
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_advanced_features.html">Section Advanced Layout Concepts</a> in the yFiles for Java Developer's Guide
 */
public class GroupingLayoutWithoutAView
{
  
  /**
   * Launcher
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        GroupingLayoutWithoutAView lwv = new GroupingLayoutWithoutAView();
        lwv.doit();
      }
    });
  }

  /**
   * Creates a small graph and applies a hierarchic group layout to it.
   * <p>
   * The output of the calculated coordinates will be displayed in the
   * console.
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
    Node v4 = graph.createNode();
    graph.setSize(v4,30,30);
    
    Node groupNode = graph.createNode();
    graph.setSize(groupNode, 100,100);
    
    Edge e1 = graph.createEdge(v1,v2);
    Edge e2 = graph.createEdge(v4, groupNode);
    Edge e3 = graph.createEdge(v1,v3);
    Edge e4 = graph.createEdge(v1, v1);
    Edge e5 = graph.createEdge(v2, groupNode);
    Edge e6 = graph.createEdge(groupNode, v2);
 
    //optionally setup some edge groups
    EdgeMap spg = graph.createEdgeMap();
    EdgeMap tpg = graph.createEdgeMap();
    
    graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, spg);
    graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tpg);
    
    spg.set(e1, "SGroup1");
    spg.set(e3, "SGroup1");
    tpg.set(e1, "TGroup1");
    tpg.set(e3, "TGroup1");

    //optionally setup the node grouping
    NodeMap nodeId = graph.createNodeMap();
    NodeMap parentNodeId = graph.createNodeMap();
    NodeMap groupKey = graph.createNodeMap();
    
    graph.addDataProvider(Grouping.NODE_ID_DPKEY, nodeId);
    graph.addDataProvider(Grouping.PARENT_NODE_ID_DPKEY, parentNodeId);
    graph.addDataProvider(Grouping.GROUP_DPKEY, groupKey);
    
    //mark a node as a group node
    groupKey.setBool(groupNode, true);
    
    // add ids for each node
    nodeId.set(v1, "v1");
    nodeId.set(v2, "v2");
    nodeId.set(v3, "v3");
    nodeId.set(v4, "v4");
    nodeId.set(groupNode, "groupNode");
    
    // set the parent for each grouped node
    parentNodeId.set(v2, "groupNode");
    parentNodeId.set(v3, "groupNode");
    
    HierarchicGroupLayouter layouter = new HierarchicGroupLayouter();
    
    layouter.setMinimalLayerDistance(0.0d);
    layouter.setMinimalEdgeDistance(10.0d);
  
    new BufferedLayouter(layouter).doLayout(graph);
    
    //display result
    LayoutPreviewPanel lpp = new LayoutPreviewPanel(graph);
    lpp.createFrame("Hierarchical Group Layout").setVisible(true);

    D.bug("\n\nGRAPH LAID OUT USING HIERACHIC GROUP LAYOUT");
    D.bug("v1 center position = " + graph.getCenter(v1));
    D.bug("v2 center position = " + graph.getCenter(v2));
    D.bug("v3 center position = " + graph.getCenter(v3));
    D.bug("v4 center position = " + graph.getCenter(v4));
    D.bug("group center position = " + graph.getCenter(groupNode));
    D.bug("group size = " + graph.getSize(groupNode));
    D.bug("e1 path = " + graph.getPath(e1));
    D.bug("e2 path = " + graph.getPath(e2));
    D.bug("e3 path = " + graph.getPath(e3));
    D.bug("e4 path = " + graph.getPath(e4));
    D.bug("e5 path = " + graph.getPath(e5));
    D.bug("e6 path = " + graph.getPath(e4));
  }
}
