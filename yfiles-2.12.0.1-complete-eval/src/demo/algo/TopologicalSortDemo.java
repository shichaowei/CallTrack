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
package demo.algo;

import y.algo.Dfs;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

import demo.base.RandomGraphGenerator;

/**
 * This class demonstrates how to sort the node set of an acyclic graph
 * topologically. 
 * A topological node order <CODE>S</CODE> of an 
 * acyclic graph <CODE>G</CODE>
 * has the property that for each node <CODE>v</CODE> of <CODE>G</CODE> 
 * all of its successors have a higher rank than <CODE>v</CODE> in
 * <CODE>S</CODE>.
 * <br>
 * The main purpose of this demo is to show how the generic Depth First Search
 * class ({@link y.algo.Dfs}) can be utilized to implement more sophisticated 
 * graph algorithms.
 *
 */

public class TopologicalSortDemo
{
  /**
   * Main method:
   * <p>
   * Usage: java demo.algo.TopologicalSortDemo &lt;nodeCount&gt; &lt;edgeCount&gt;
   * </p><p>
   * the first argument gives the desired node count of the graph 
   * and the second argument gives the desired edge count of the 
   * graph.
   * </p>
   */
  public static void main(String[] args)
  {
    int nodeCount = 30;
    int edgeCount = 60;
    
    if(args.length == 2) {
      try {
        nodeCount = Integer.parseInt(args[0]);
        edgeCount = Integer.parseInt(args[1]);
      } catch(NumberFormatException ex) {
        usage();
      }
    }
    
    // Create a random acyclic graph with the given edge and node count
    RandomGraphGenerator randomGraph = new RandomGraphGenerator(0L);
    randomGraph.setNodeCount(nodeCount);
    randomGraph.setEdgeCount(edgeCount);
    randomGraph.allowCycles( false ); //create a DAG
    Graph graph = randomGraph.generate();
    
    final NodeList tsOrder = new NodeList();
    
    if(!graph.isEmpty())
    {
      // find start node with indegree 0
      Node startNode = graph.firstNode();
      for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
      {
        if(nc.node().inDegree() == 0)
        {
          startNode = nc.node();
          break;
        }
      }
      
      // specialize DFS algorithm to collect topological information
      Dfs dfs = new Dfs() {
        protected void postVisit(Node v, int dfsNum, int compNum)
          {
            tsOrder.addFirst(v);
          }
      };
      
      // put dfs in directed mode
      dfs.setDirectedMode(true);
      // start specialized dfs
      dfs.start(graph, startNode);
      
    }
    
    System.out.println("Topological Order:");
    int index = 0;
    for(NodeCursor nc = tsOrder.nodes(); nc.ok(); nc.next(), index++)
    {
      System.out.println("" + index + ". " + nc.node());
    }
    
  }

  static void usage()
  {
    System.err.println("Usage: java demo.algo.TopologicalSortDemo <nodeCount> <edgeCount>");
    System.err.println("Usage: Both <nodeCount> and <edgeCount> must be integral values.");
    System.exit(1);
  }
}
