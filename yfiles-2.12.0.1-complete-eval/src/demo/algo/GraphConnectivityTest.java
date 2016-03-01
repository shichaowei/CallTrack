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
import y.algo.GraphConnectivity;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.util.D;
import y.util.Maps;
import y.util.Timer;
import y.util.YRandom;

/**
 * Demonstrates how to use connectivity and biconnectivity algorithms.
 * Tests the performance of these algorithms.
 */
public class GraphConnectivityTest {
  private Timer t1 = new Timer( false );
  private Timer t2 = new Timer( false );

  public static void main( String[] args ) {
    (new GraphConnectivityTest()).doIt();
  }

  private void doIt() {
    for ( int i = 0; i < 400; i++ ) {
      D.bug( "test " + i );
      test( i );
    }

    D.bug( "connectivity timer: " + t1 + "    biconnectivity timer: " + t2 );
  }

  private void test( int seed ) {
    //create random graph
    RandomGraphGenerator rg = new RandomGraphGenerator( seed );
    YRandom random = new YRandom( seed );
    rg.setNodeCount( random.nextInt( 400 ) );
    rg.setEdgeCount( random.nextInt( 800 ) );

    Graph graph = rg.generate();

    t1.start();
    //calculate the connected components of the graph
    NodeList[] comps = GraphConnectivity.connectedComponents( graph );
    t1.stop();

    //add edges to the graph until the whole graph is connected.
    //this is done by connecting the first nodes of the first 
    //two connected components. this operation reduces the 
    //component count by one.
    int oldCompCount = comps.length + 1;
    while ( comps.length > 1 ) {
      oldCompCount = comps.length;
      //connect first two components
      graph.createEdge( comps[0].firstNode(), comps[1].firstNode() );
      comps = GraphConnectivity.connectedComponents( graph );
      if ( comps.length != oldCompCount - 1 ) {
        error( "connected components yields wrong result!" );
      }
    }

    //next fetch biconnected components. 
    //be sure that the precondition is valid  
    GraphConnectivity.makeConnected( graph );

    //use a static node map in case the node indices do not
    //change while the map is needed. static maps are
    //generally faster than dynamic maps
    NodeMap aMap = Maps.createIndexNodeMap( new boolean[graph.N()] );
    //create dynamic edge map in case the edge indices or edge set
    //changes while the map is needed.
    EdgeMap cMap = graph.createEdgeMap();


    GraphConnectivity.makeConnected( graph );

    t2.start();
    int bicompCount = GraphConnectivity.biconnectedComponents( graph, cMap, aMap );
    t2.stop();

    //for the sake of demonstration we remove all edges that connect
    //to an articulation point. 
    for ( NodeCursor nc = graph.nodes(); nc.ok(); nc.next() ) {
      if ( aMap.getBool( nc.node() ) ) {
        Node v = nc.node();
        //v is an articulation point of graph.
        //remove all edges around an articulation point
        for ( EdgeCursor ec = v.edges(); ec.ok(); ec.next() )
          graph.removeEdge( ec.edge() );
      }
    }

    //dispose dynamic nodemap since it is not needed anymore.
    graph.disposeEdgeMap( cMap );

  }

  private static void error( String msg ) {
    D.bug( msg );
    System.exit( 666 );
  }
}
