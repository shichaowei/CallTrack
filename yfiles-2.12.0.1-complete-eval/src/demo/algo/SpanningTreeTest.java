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

import y.algo.SpanningTrees;
import y.algo.GraphConnectivity;

import y.util.YRandom;
import y.util.DataProviders;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.Edge;
import y.base.DataProvider;
import y.base.EdgeList;
import y.base.EdgeCursor;

import demo.base.RandomGraphGenerator;

/**
 * This class compares the performance of different minimum spanning tree algorithms.
 */
public class SpanningTreeTest
{
  private static long seed = 0;

  /**
   * Program launcher. Accepts a random seed on the command line.
   */  
  public static void main(String[] args)
  {
    try 
    {
      seed = Long.parseLong(args[0]);
    }
    catch(Exception ex) {}
    
    (new SpanningTreeTest()).testMST();
  }
  

  
  private void testMST()
  {
    D.bug(">>> testMST");
    Timer timerA = new Timer(false);
    Timer timerB = new Timer(false);
    timerA.reset();
    timerB.reset();
    
    YRandom random = new YRandom(seed);
    
    RandomGraphGenerator rg = new RandomGraphGenerator(seed);
    rg.allowCycles(true);
    
    for(int size = 100; size <= 100000; size *= 10)
    {
      for(int trial = 0; trial < 100; trial++)
      {
        if(trial % 60 == 59) D.bug("."); else D.bu(".");
        
        rg.setNodeCount(random.nextInt(1000,2000));
        rg.setEdgeCount(random.nextInt(size/10,size));
        
        Graph G = rg.generate();
        int eCount = GraphConnectivity.makeConnected(G).size();
        
        double[] cost = new double[G.E()];
        
        for(EdgeCursor ec = G.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          int eid = e.index();
          cost[eid] = random.nextInt(100000);
        }
        
        DataProvider c = DataProviders.createEdgeDataProvider(cost);
        
        timerA.start();
        EdgeList resultA = SpanningTrees.kruskal(G,c);
        double costA = SpanningTrees.cost(resultA,c);
        timerA.stop();
        
        timerB.start();
        EdgeList resultB = SpanningTrees.prim(G,c);
        double costB = SpanningTrees.cost(resultB,c);
        timerB.stop();
        
        if(costA != costB)
        {
          D.bug("\ncost mismatch: trial = " + trial);
          D.bug("costA = " + costA + "   costBi = " + costB);
        }
      }
      D.bug("\nsize=" + size + "\nkruskal " + timerA + "\nprim    " + timerB);
      timerA.reset();
      timerB.reset();
    }
    
    D.bug("<<< testMST\n\n");
  }
}
