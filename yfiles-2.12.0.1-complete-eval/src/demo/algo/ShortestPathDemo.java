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

import y.base.Graph;
import y.base.Edge;
import y.base.Node;
import y.base.EdgeList;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.NodeMap;
import y.util.D;
import y.util.YRandom;

import demo.base.RandomGraphGenerator;

import y.algo.ShortestPaths;



/**
 * Demonstrates the usage of the {@link ShortestPaths} class that
 * provides easy to use algorithms for finding shortest paths 
 * within weighted graphs.
 *
 */
public class ShortestPathDemo
{
  /**
   * Usage: java demo.algo.ShortestPathDemo &lt;nodeCount&gt; &lt;edgeCount&gt;
   * <p>
   * The first argument gives the desired node count of the graph 
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
    
    // Create a random graph with the given edge and node count
    RandomGraphGenerator randomGraph = new RandomGraphGenerator(0L);
    randomGraph.setNodeCount(nodeCount);
    randomGraph.setEdgeCount(edgeCount);
    Graph graph = randomGraph.generate();
    
    // Create an edgemap and assign random double weights to 
    // the edges of the graph.
    EdgeMap weightMap = graph.createEdgeMap();
    YRandom random = new YRandom(0L);
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      Edge e = ec.edge();
      weightMap.setDouble(e,100.0*random.nextDouble());
    }
    
    
    
    // Calculate the shortest path from the first to the last node
    // within the graph
    if(!graph.isEmpty())
    {
      
      Node from = graph.firstNode();
      Node to   = graph.lastNode();
      EdgeList path;
      double sum = 0.0;
      
      // The undirected case first, i.e. edges of the graph and the
      // resulting shortest path are considered to be undirected
      
      path = ShortestPaths.singleSourceSingleSink(graph, from, to, false, weightMap);
      for(EdgeCursor ec = path.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        double weight = weightMap.getDouble( e );
        D.bug( e + " weight = " + weight );
        sum += weight;
      }
      if(sum == 0.0)
        D.bug("NO UNDIRECTED PATH");
      else
        D.bug("UNDIRECTED PATH LENGTH = " + sum);
      
      
      // Next the directed case, i.e. edges of the graph and the
      // resulting shortest path are considered to be directed.
      // Note that this shortest path can't be shorter than the one
      // for the undirected case
      
      path = ShortestPaths.singleSourceSingleSink(graph, from, to, true, weightMap );
      sum = 0.0;
      for(EdgeCursor ec = path.edges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        double weight = weightMap.getDouble( e );
        D.bug( e + " weight = " + weight );
        sum += weight;
      }
      if(sum == 0.0)
        D.bug("NO DIRECTED PATH");
      else
        D.bug("DIRECTED PATH LENGTH = " + sum);
      
      
      D.bug("\nAuxiliary distance test\n");
      
      NodeMap distanceMap = graph.createNodeMap();
      NodeMap predMap     = graph.createNodeMap();
      ShortestPaths.singleSource(graph, from, true, weightMap, distanceMap, predMap);
      if(distanceMap.getDouble(to) == Double.POSITIVE_INFINITY)
        D.bug("Distance from first to last node is infinite");
      else
        D.bug("Distance from first to last node is " + distanceMap.getDouble(to));
      
      // Dispose distanceMap since it is not needed anymore
      graph.disposeNodeMap(distanceMap);
      
    }
    
    // Dispose weightMap since it is not needed anymore
    graph.disposeEdgeMap( weightMap );
    
  }
  
  static void usage()
  {
    System.err.println("Usage: java demo.algo.ShortestPathDemo <nodeCount> <edgeCount>");
    System.err.println("Usage: Both <nodeCount> and <edgeCount> must be integral values.");
    System.exit(1);
  }
  
}
