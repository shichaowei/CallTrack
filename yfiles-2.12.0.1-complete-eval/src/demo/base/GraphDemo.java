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
package demo.base;

import y.base.Graph;
import y.base.Node;
import y.base.Edge;
import y.base.NodeCursor;
import y.base.EdgeCursor;

import y.base.NodeMap;
import y.base.EdgeMap;


/**
 * Demonstrates how to use the directed graph data type Graph.
 * <p>
 * <b>usage:</b> java demo.base.GraphDemo
 * </p>
 *
 * <a href="http://docs.yworks.com/yfiles/doc/developers-guide/base.html#Creating Graphs and Graph Elements">Section Creating Graphs and Graph Elements</a> in the yFiles for Java Developer's Guide
 */
public class GraphDemo 
{
  public GraphDemo()
  {
    //instantiates an empty graph
    Graph graph = new Graph();
    
    //create a temporary node array for fast lookup
    Node[] tmpNodes = new Node[5];
    
    //create some nodes in the graph and store references in the array
    for(int i = 0; i < 5; i++)
    {
      tmpNodes[i] = graph.createNode();
    }
    
    //create some edges in the graph
    for(int i = 0; i < 5; i++)
    {
      for(int j = i+1; j < 5; j++)
      {
        //create an edge from node at index i to node at index j
        graph.createEdge(tmpNodes[i],tmpNodes[j]);
      }
    }
    
    
    //output the nodes of the graph 
    System.out.println("The nodes of the graph");
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      Node node = nc.node();
      System.out.println(node);
      System.out.println("in edges #" + node.inDegree());
      for(EdgeCursor ec = node.inEdges(); ec.ok(); ec.next())
      {
        System.out.println(ec.edge());
      }
      System.out.println("out edges #" + node.outDegree());
      for(EdgeCursor ec = node.outEdges(); ec.ok(); ec.next())
      {
        System.out.println(ec.edge());
      }
    }
    
      
    //output the edges of the graph 
    System.out.println("\nThe edges of the graph");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(ec.edge());
    }

    //reverse edges that have consecutive neighbors in graph
    //reversing means switching source and target node
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      if(Math.abs(ec.edge().source().index() - ec.edge().target().index()) == 1) 
        graph.reverseEdge(ec.edge());
    }
    
    System.out.println("\nthe edges of the graph after some edge reversal");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(ec.edge());
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Node- and EdgeMap handling   ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    //create a nodemap for the graph
    NodeMap nodeMap = graph.createNodeMap();
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      //get node at current cursor position
      Node node = nc.node();
      //associate descriptive String to the node via a nodemap 
      nodeMap.set(node,"this is node " + node.index());
    }
    
    //create an edgemap for the graph
    EdgeMap edgeMap = graph.createEdgeMap();
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      //get edge at current cursor position
      Edge edge = ec.edge();
      //associate descriptive String to the edge via an edgemap
      edgeMap.set(edge,"this is edge [" + 
                  nodeMap.get(edge.source()) + "," + 
                  nodeMap.get(edge.target()) + "]");
    }
    
    //output the nodemap values of the nodes
    System.out.println("\nThe node map values of the graph");
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      System.out.println(nodeMap.get(nc.node()));
    }
    
    //output the edges of the graph 
    System.out.println("\nThe edge map values of the graph");
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      System.out.println(edgeMap.get(ec.edge()));
    }
    
    //cleanup unneeded node and edge maps again (free resources)
    graph.disposeNodeMap(nodeMap);
    graph.disposeEdgeMap(edgeMap);

    ///////////////////////////////////////////////////////////////////////////
    // removing elements from the graph  //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
    {
      //remove node that has a edge degree > 2
      if(nc.node().degree() > 2)
      {
        //removed the node and all of its adjacent edges from the graph
        graph.removeNode(nc.node());
      }
    }
    System.out.println("\ngraph after some node removal");
    System.out.println(graph);
    
    
    
  }
  
  public static void main(String[] args)
  {
    new GraphDemo();
  }
}
