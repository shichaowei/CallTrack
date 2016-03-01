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

import java.util.*;

import y.base.Node;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;

import y.layout.EdgeLayout;
import y.layout.LayoutGraph;
import y.layout.CanonicMultiStageLayouter;

import y.geom.YPoint;

/**
 * This class demonstrates how to write a 
 * custom layouter for the yFiles framework.
 * <br>
 * This class lays out a graph in the following style:
 * <br>
 * The nodes of each graph component will be placed on a 
 * diagonal line.
 * Edges will be routed with exactly one bend, so that no 
 * edges that share a common terminal node will cross.
 * <br>
 * See {@link demo.layout.module.DiagonalLayoutModule} for a module wrapper
 * and {@link demo.layout.module.LayoutModuleDemo} for the diagonal layouter in action. 
 */
public class DiagonalLayouter extends CanonicMultiStageLayouter
{
  double minimalNodeDistance = 40;
  
  /**
   * Creates a new instance of DiagonalLayouter
   */
  public DiagonalLayouter()
  {
    //do not use defualt behaviour. we handle parallel edge routing ourselves.  
    setParallelEdgeLayouterEnabled(false);
  }
  
  /**
   * Sets the minimal distance between nodes.
   */
  public void setMinimalNodeDistance(double d)
  {
    minimalNodeDistance = d;
  }
  
  /**
   * Returns the minimal distance between nodes.
   */
  public double getMinimalNodeDistance()
  {
    return minimalNodeDistance;
  }
  
  /**
   * Returns always <code>true</code>, because every graph can be
   * laid out.
   */
  protected boolean canLayoutCore(LayoutGraph graph)
  {
    return true;
  }
  
  /**
   * Perform the layout.
   */
  protected void doLayoutCore(LayoutGraph graph)
  {

    //place the nodes on a diagonal line
    Node[] nodes = graph.getNodeArray();
    double offset = 0.0;
    for(int i = 0; i < nodes.length; i++)
    {
      Node v = nodes[i];
      graph.setLocation(v,offset,offset);
      offset += minimalNodeDistance + Math.max(graph.getWidth(v),graph.getHeight(v));
    }
    
    //comparator used to sort edges by the
    //index of their target node
    Comparator outComp = new Comparator() {
      public int compare(Object a, Object b) {
        Node va = ((Edge)a).target();
        Node vb = ((Edge)b).target();
        if(va != vb) 
          return va.index() - vb.index();
        else
          return ((Edge)a).index() - ((Edge)b).index();
      }
    };
    
    //comparator used to sort edges by the
    //index of their source node.
    Comparator inComp = new Comparator() {
      public int compare(Object a, Object b) {
        Node va = ((Edge)a).source();
        Node vb = ((Edge)b).source();
        if(va != vb) 
          return va.index() - vb.index();
        else
          return ((Edge)b).index() - ((Edge)a).index();
      }
    };
    
    //prepare edge layout. use exactly one bend per edge
    for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
    {
      EdgeLayout el = graph.getLayout(ec.edge());
      el.clearPoints();
      el.addPoint(0,0);
    }
    
    //route the edges
    for(int i = 0; i < nodes.length; i++)
    {
      Node v = nodes[i];

      
      EdgeList rightSide  = new EdgeList();
      EdgeList leftSide   = new EdgeList();
      
      //assign x coodinates to all outgoing edges of v
      v.sortOutEdges(outComp);
      for(EdgeCursor ec = v.outEdges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        Node w = e.target();
        
        if(w.index() < v.index())
          rightSide.addLast(e);
        else
          leftSide.addLast(e);
      }
      
      if(!rightSide.isEmpty())
      {
        double space  = graph.getWidth(v)/rightSide.size();
        double xcoord = graph.getX(v) + graph.getWidth(v) - space/2.0;
        for(EdgeCursor ec = rightSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, xcoord, p.getY());
          graph.setSourcePointAbs(e, new YPoint(xcoord, graph.getCenterY(v)));
          xcoord -= space;
        }
      }
      
      if(!leftSide.isEmpty())
      {
        double space  = graph.getWidth(v)/leftSide.size();
        double xcoord = graph.getX(v) + graph.getWidth(v) - space/2.0;
        for(EdgeCursor ec = leftSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, xcoord, p.getY());
          graph.setSourcePointAbs(e, new YPoint(xcoord,graph.getCenterY(v)));
          xcoord -= space;
        }
      }
      
      //assign y coodinates to all ingoing edges of v
      rightSide.clear();
      leftSide.clear();
      v.sortInEdges(inComp);
      for(EdgeCursor ec = v.inEdges(); ec.ok(); ec.next())
      {
        Edge e = ec.edge();
        Node w = e.source();
        
        if(w.index() < v.index())
          leftSide.addLast(e);
        else
          rightSide.addLast(e);
      }
      
      if(!rightSide.isEmpty())
      {
        double space  = graph.getHeight(v)/rightSide.size();
        double ycoord = graph.getY(v) + graph.getHeight(v) - space/2.0;
        for(EdgeCursor ec = rightSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, p.getX(), ycoord);
          graph.setTargetPointAbs(e, new YPoint(graph.getCenterX(v), ycoord));
          ycoord -= space;
        }
      }
      
      if(!leftSide.isEmpty())
      {
        double space  = graph.getHeight(v)/leftSide.size();
        double ycoord = graph.getY(v) + graph.getHeight(v) - space/2.0;
        for(EdgeCursor ec = leftSide.edges(); ec.ok(); ec.next())
        {
          Edge e = ec.edge();
          EdgeLayout el = graph.getLayout(e);
          YPoint p = el.getPoint(0);
          el.setPoint(0, p.getX(), ycoord);
          graph.setTargetPointAbs(e, new YPoint(graph.getCenterX(v), ycoord));
          ycoord -= space;
        }
      }
    }
  }
}
