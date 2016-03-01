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

import y.base.DataProvider;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.layout.BufferedLayouter;
import y.layout.DefaultLayoutGraph;
import y.layout.LayoutGraph;
import y.layout.LayoutMultiplexer;
import y.layout.LayoutOrientation;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.grouping.GroupingKeys;
import y.layout.grouping.RecursiveGroupLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;

import java.awt.EventQueue;

/**
 * This class shows how to layout the contents of group nodes
 * each with different layout style. In this example,
 * the graph induced by the grouped nodes labeled 0 to 5 will be laid out
 * by HierarchicLayouter using BOTTOM_TO_TOP orientation,
 * while the remaining nodes will be laid out by
 * HierarchicLayouter using LEFT_TO_RIGHT orientation.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/layout_stages.html">Section Layout Stages</a> in the yFiles for Java Developer's Guide
 */
public class RecursiveGroupLayouterDemo
{
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        LayoutGraph graph = new DefaultLayoutGraph();

        //create graph structure
        Node[] v = new Node[10];
        for (int i = 0; i < v.length; i++) {
          v[i] = graph.createNode();
          graph.setSize(v[i], 30, 30);
        }
        int[][] e = {{0, 1}, {0, 2}, {0, 3}, {4, 0}, {5, 0}, {0, 7}, {6, 0}, {6, 8}, {8, 7}};
        for (int i = 0; i < e.length; i++) {
          Node s = v[e[i][0]];
          Node t = v[e[i][1]];
          graph.createEdge(s, t);
        }

        //set up fixed port constraints for edges that connect at v[0]
        EdgeMap spcMap = graph.createEdgeMap();
        EdgeMap tpcMap = graph.createEdgeMap();
        graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, spcMap);
        graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, tpcMap);
        spcMap.set(v[0].getEdgeTo(v[7]), PortConstraint.create(PortConstraint.EAST, false));
        tpcMap.set(v[6].getEdgeTo(v[0]), PortConstraint.create(PortConstraint.WEST, false));

        //set up edge group information (optional)
        EdgeMap sgMap = graph.createEdgeMap();
        EdgeMap tgMap = graph.createEdgeMap();
        graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, sgMap);
        graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, tgMap);
        sgMap.set(v[0].getEdgeTo(v[1]), "G1");
        sgMap.set(v[0].getEdgeTo(v[2]), "G1");
        sgMap.set(v[0].getEdgeTo(v[3]), "G1");

        tgMap.set(v[0].getEdgeFrom(v[4]), "G2");
        tgMap.set(v[0].getEdgeFrom(v[5]), "G2");

        //set up grouping information
        NodeMap groupMap = graph.createNodeMap();
        NodeMap pidMap = graph.createNodeMap();
        NodeMap idMap = graph.createNodeMap();
        graph.addDataProvider(GroupingKeys.GROUP_DPKEY, groupMap);
        graph.addDataProvider(GroupingKeys.NODE_ID_DPKEY, idMap);
        graph.addDataProvider(GroupingKeys.PARENT_NODE_ID_DPKEY, pidMap);
        groupMap.setBool(v[9], true);
        for (int i = 0; i < 6; i++) {
          pidMap.set(v[i], v[9]);
        }
        for (int i = 0; i < v.length; i++) {
          idMap.set(v[i], v[i]);
        }

        //configure layout algorithm
        IncrementalHierarchicLayouter innerHL = new IncrementalHierarchicLayouter();
        innerHL.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
        IncrementalHierarchicLayouter outerHL = new IncrementalHierarchicLayouter();
        outerHL.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
        LayoutMultiplexer lm = new LayoutMultiplexer();
        NodeMap layoutMap = graph.createNodeMap();
        graph.addDataProvider(LayoutMultiplexer.LAYOUTER_DPKEY, layoutMap);
        for (int i = 0; i < 6; i++) {
          layoutMap.set(v[i], innerHL);
        }
        for (int i = 7; i < v.length; i++) {
          layoutMap.set(v[i], outerHL);
        }

        //launch layout algorithm
        RecursiveGroupLayouter rgl = new RecursiveGroupLayouter(lm);
        new BufferedLayouter(rgl).doLayout(graph);

        //remove group node
        graph.removeNode(v[9]);

        //  display result
        LayoutPreviewPanel lpp = new LayoutPreviewPanel(graph);
        lpp.createFrame("RecursiveGroupLayouterDemo").setVisible(true);

        //remove all registered DataProviders, NodeMap and EdgeMaps
        Object[] key = graph.getDataProviderKeys();
        for (int i = 0; i < key.length; i++) {
          DataProvider dp = graph.getDataProvider(key[i]);
          graph.removeDataProvider(key[i]);
          if (dp instanceof NodeMap) {
            graph.disposeNodeMap((NodeMap) dp);
          } else if (dp instanceof EdgeMap) {
            graph.disposeEdgeMap((EdgeMap) dp);
          }
        }
      }
    });

  }

}
