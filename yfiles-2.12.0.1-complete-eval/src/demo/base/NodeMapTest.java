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

import java.util.HashMap;
import java.util.Map;
import y.util.Maps;
import y.util.Timer;
import y.util.D;
import y.base.Graph;
import y.base.NodeMap;
import y.base.NodeCursor;
import y.base.Node;


/**
 * This class compares the performance of different 
 * mechanisms to bind extra data to the nodes of a graph.
 * In scenarios where the indices of the nodes do not change
 * while the extra node data is needed, it is best to use array based
 * mechanisms that use the index of a node to access the data.
 * <br>
 * In scenarios where the indices of the nodes will change
 * while the extra node data is needed, it is necessary to
 * use {@link y.base.NodeMap} implementations that do not depend on the indices
 * of the nodes (see {@link Node#index()}) or {@link java.util.Map}
 * implementations like {@link java.util.HashMap} provided by the java
 * collections framework.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/data_accessors.html#Maps and Data Providers">Section Maps and Data Providers</a> in the yFiles for Java Developer's Guide
 */
public class NodeMapTest
{
  
  static Timer t1 = new Timer(false);
  static Timer t2 = new Timer(false);
  static Timer t3 = new Timer(false);
  static Timer t4 = new Timer(false);
  static Timer t5 = new Timer(false);
  
  public static void main(String[] args)
  {
    test1();
  }
  
  static void test1()
  {
    Graph graph = new Graph();
    for(int i = 0; i < 20000; i++) graph.createNode();
    
    for(int loop = 0; loop < 10; loop++)
    {
      D.bu(".");
      
      t1.start();
      NodeMap map = graph.createNodeMap();
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v,i);
          i = map.getInt(v);
        }
      }
      graph.disposeNodeMap(map);
      t1.stop();
      
      
      t2.start();
      map = Maps.createIndexNodeMap(new int[graph.N()]);
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v,i);
          map.getInt(v);
        }
      }
      t2.stop();
      
      
      t3.start();
      map = Maps.createHashedNodeMap(); 
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          map.setInt(v, i);
          i = map.getInt(v);
        }
      }
      t3.stop();
      
      t4.start();
      int[] array = new int[graph.N()];
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          int vid = nc.node().index();
          array[vid] = i;
          i = array[vid];
        }
      }
      t4.stop();

    
      t5.start();
      Map jmap = new HashMap(2*graph.N()+1); //use hash map with good initial size
      for(int i = 0; i < 10; i++)
      {
        for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
        {
          Node v = nc.node();
          jmap.put(v, new Integer(i));
          i = ((Integer)jmap.get(v)).intValue();
        }
      }
      t5.stop();
      
    }
    
    D.bug("");
    D.bug("TIME:  standard NodeMap: " + t1);
    D.bug("TIME:  index    NodeMap: " + t2);
    D.bug("TIME:  hashed   NodeMap: " + t3);
    D.bug("TIME:  plain array     : " + t4);
    D.bug("TIME:  HashMap         : " + t5);
  }
}

    
    
    
