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
package demo.view.realizer;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.ViewMode;
import y.io.GraphMLIOHandler;

import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * This demo shows how the custom node realizer {@link StateNodeRealizer}
 * can be used within an application.
 * The demo allows to create nodes that have different state. 
 * Additionally it is possible to change the state of a node by either right clicking
 * or left double clicking on it.
 * A graph with its custom node realizers can be saved and loaded using the GraphML
 * format.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#custom_edit_mode">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class StateNodeRealizerDemo extends DemoBase
{

  public StateNodeRealizerDemo()
  {
    Graph2D graph = view.getGraph2D();

    view.addViewMode(new StateChangeViewMode());

    StateNodeRealizer svr = new StateNodeRealizer();
    svr.setSize(70,70);
    svr.setState(StateNodeRealizer.FINAL_STATE);
    svr.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
    
    graph.setDefaultNodeRealizer(svr);

    //for each node that will be created use a reconfigured
    //default node realizer. 
    graph.addGraphListener(new GraphListener() {
      public void onGraphEvent(GraphEvent ev)
      {
        if(ev.getType() == GraphEvent.NODE_CREATION)
        {
          applyNextState(((Graph2D)ev.getGraph()).getDefaultNodeRealizer());
        }
      }
    });

    loadGraph( "resource/stateNodeRealizer.graphml" );
    DemoDefaults.applyFillColor(graph, DemoDefaults.DEFAULT_NODE_COLOR);
  }


  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();
    ioHandler.addNodeRealizerSerializer(new StateNodeRealizer.StateNodeRealizerSerializer());
    return ioHandler;
  }

  /**
   * This method changes state and shape of a StateNodeRealizer.
   */
  private void applyNextState(NodeRealizer vr)
  {
    if(vr instanceof StateNodeRealizer)
    {
      StateNodeRealizer svr = (StateNodeRealizer)vr;
      switch(svr.getState()) {
        case StateNodeRealizer.INITIAL_STATE:
         svr.setState(StateNodeRealizer.TRANSITION_STATE);
         break;
        case StateNodeRealizer.TRANSITION_STATE:
         svr.setState(StateNodeRealizer.FINAL_STATE);
         break;
        case StateNodeRealizer.FINAL_STATE:
         svr.setState(StateNodeRealizer.INITIAL_STATE);
         break;
      }
      if(svr.getShapeType() == ShapeNodeRealizer.ELLIPSE)
      {
        svr.setShapeType(StateNodeRealizer.CUSTOM_SHAPE);
      }
      else
      {
        svr.setShapeType(ShapeNodeRealizer.ELLIPSE);
      }
    }
  }

  /**
   * ViewMode that changes state and shape of a node when it
   * gets right-clicked or double-clicked.
   */
  private class StateChangeViewMode extends ViewMode
  {

    public void mousePressedRight(double x, double y)
    {
      Node hitNode = getHitInfo(x,y).getHitNode();
      if(hitNode != null)
      {
        applyNextState(getGraph2D().getRealizer(hitNode));
        getGraph2D().updateViews();
      }
    }

    public void mouseClicked(MouseEvent ev)
    {
      if(ev.getClickCount() == 2)
      {
        double x = translateX(ev.getX());
        double y = translateY(ev.getY());
        Node hitNode = getHitInfo(x,y).getHitNode();
        if(hitNode != null)
        {
          applyNextState(getGraph2D().getRealizer(hitNode));
          getGraph2D().updateViews();
        }
      }
    }
  }


  public static void main(String[] args)
  {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new StateNodeRealizerDemo()).start();
      }
    });
  }

}
