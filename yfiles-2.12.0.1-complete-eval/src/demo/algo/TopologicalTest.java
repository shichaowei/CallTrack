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

import y.algo.NodeOrders;

import y.util.YRandom;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.NodeCursor;
import y.base.Node;

import demo.base.RandomGraphGenerator;

/**
 * This class compares different methods that calculate a topological
 * node ordering on the nodes of an acyclic graph.
 **/
public class TopologicalTest
{
  private Timer timerA = new Timer(false);
  private Timer timerB = new Timer(false);
  private Timer timerC = new Timer(false);
  private Timer timerD = new Timer(false);

  public static void main(String[] args)
  {
    (new TopologicalTest()).testTOP();
  }
  

  
  private void testTOP()
  {
    timerA.reset();
    timerB.reset();
    timerC.reset();
    timerD.reset();
    
    timerD.start();
    for(int i = 0; i < 1000; i++)
      testTOP(i);
    timerD.stop();
    D.bug(
      "overall = "    + timerD + 
      "\ngenerate = " + timerC + 
      "\ntopological   = "      + timerA + 
      "\ndfs completion = "      + timerB
      );
  }
  
  private void testTOP(int loop)
  {
    D.bug("test TOP " + loop);
    
    YRandom random = new YRandom(loop);
    
    RandomGraphGenerator rg = new RandomGraphGenerator(loop);
    
    rg.allowCycles(loop % 2 == 0);
    rg.setNodeCount(100);
    rg.setEdgeCount(1000);
    
    
    timerC.start();
    Graph G = rg.generate();
    timerC.stop();
    
    
    int[] topOrderA = new int[G.N()];
    int[] topOrderB = new int[G.N()];
    
    timerA.start();
    boolean resultA = NodeOrders.topological(G,topOrderA);
    timerA.stop();

    if(resultA)
    {
      check("topological", G, topOrderA);
    }
    
    timerB.start();
    NodeOrders.dfsCompletion(G,topOrderB);
    timerB.stop();
    if(resultA)
    {
      check("dfs completion", G, reverse(topOrderB));
    }
  }
  
  
  private int[] reverse(int[] order)
  {
    int[] reverse = new int[order.length];
    for(int i = 0; i < order.length; i++)
    {
      reverse[i] = order.length-1-order[i];
    }
    return reverse;
  }
  
  private void check(String desc, Graph G, int[] topOrder)
  {
    boolean[] tag = new boolean[G.N()];
    for(NodeCursor nc = G.nodes(); nc.ok(); nc.next())
    {
      Node v = nc.node();
      int vid = v.index();
      int order = topOrder[vid];
      if(order < 0 || order >= G.N())
        error(desc + " : order number for " + v + " out of bounds: " + order);
      if(tag[order]) 
        error(desc + " : order number for " + v + " already assigned: " + order);
      for(NodeCursor ncc = v.successors(); ncc.ok(); ncc.next())
      {
        Node u = ncc.node();
        int uid = u.index();
        if(topOrder[uid] <= order)
          error(desc + " : nodes in wrong order!");
      }
      tag[order] = true;
    }
  }

  private void error(String msg)
  {
    D.bug(msg);
    System.exit(1);
  }
}
