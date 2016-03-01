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


import demo.base.RandomGraphGenerator;
import y.algo.Cycles;
import y.algo.GraphChecker;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.EdgeMap;
import y.base.Graph;
import y.util.D;
import y.util.Maps;
import y.util.Timer;

/**
 * Tests consistency and performance of two different cycle detection mechanisms
 * in yFiles.
 */
public class CyclesTest {

  private Timer t1 = new Timer( false );
  private Timer t2 = new Timer( false );

  private int akku1 = 0;
  private int akku2 = 0;


  public static void main( String[] args ) {
    CyclesTest cyclesTest = new CyclesTest();
    cyclesTest.doIt();
  }

  private void doIt() {
    for ( int i = 0; i < 1000; i++ ) {
      D.bug( "test " + i );
      test( i );
    }

    D.bug( "overall reversed edges (default method) " + akku1 + "    time: " + t1 );
    D.bug( "overall reversed edges (dfs     method) " + akku2 + "    time: " + t2 );
  }

  private void test( int seed ) {
    RandomGraphGenerator rg = new RandomGraphGenerator( seed );
    rg.setNodeCount( 100 );
    rg.setEdgeCount( 300 );
    rg.allowCycles( true );

    Graph graph1 = rg.generate();

    EdgeMap cycleEdge = Maps.createIndexEdgeMap( new boolean[graph1.E()] );

    //find a set of edges whose reversal make the given graph
    //acyclic.  reverse whose edges
    t1.start();
    Cycles.findCycleEdges( graph1, cycleEdge );
    int count1 = 0;
    for ( EdgeCursor ec = graph1.edges(); ec.ok(); ec.next() ) {
      if ( cycleEdge.getBool( ec.edge() ) ) {
        graph1.reverseEdge( ec.edge() );
        count1++;
      }
    }
    t1.stop();

    //check acyclicity of graph
    if ( GraphChecker.isCyclic( graph1 ) ) {
      D.bug( "graph1 still contains cycles!!!" );
      EdgeList cycle = Cycles.findCycle( graph1, true );
      error( "cycle = " + cycle );
    }


    rg.setSeed( seed );
    Graph graph2 = rg.generate();

    //use alternative DFS based method to detect
    //with a set of cyclicity edges. 
    t2.start();
    Cycles.findCycleEdgesDFS( graph2, cycleEdge );
    int count2 = 0;
    for ( EdgeCursor ec = graph2.edges(); ec.ok(); ec.next() ) {
      if ( cycleEdge.getBool( ec.edge() ) ) {
        graph2.reverseEdge( ec.edge() );
        count2++;
      }
    }
    t2.stop();

    if ( GraphChecker.isCyclic( graph2 ) ) {
      D.bug( "graph2 still contains cycles!!!" );
      EdgeList cycle = Cycles.findCycle( graph2, true );
      error( "cycle = " + cycle );
    }

    akku1 += count1;
    akku2 += count2;
  }

  private void error( String msg ) {
    D.bug( msg );
    System.exit( 666 );
  }
}
