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
package demo.view.viewmode;


import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Edge;
import y.base.Node;
import y.base.YCursor;
import y.base.YList;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Port;

import java.awt.EventQueue;
import java.util.Locale;

/**
 * Demonstrates how {@link CreateEdgeMode} can be customized in order to
 * control automatic assignments of ports for edges.
 * Edges are created in such a way, that the source port is always on
 * the top side of the source node and the target port is always on the bottom
 * side of the target node.
 * <p>
 * Usage: Create some nodes and edges. Select an edge to check its source
 * and target ports.
 * </p>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#custom_edit_mode">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class PortCreateEdgeModeDemo extends DemoBase
{
  protected void initialize() {
    super.initialize();
    loadGraph("resource/PortCreateEdgeModeDemo.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D());
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode( editMode );
    //set a custom CreateEdgeMode for the edge mode
    editMode.setCreateEdgeMode( new PortCreateEdgeMode() );
  }

  public static class PortCreateEdgeMode extends CreateEdgeMode
  {
    private Edge edge; // need this for the hook

    /**
     * If a node was hit at the given coordinates, that node
     * will be used as target node for the newly created edge.
     *
     */
    public void mouseReleasedLeft(double x, double y)
    {
      // simulate a pressed shift...
      // this will trigger CreateEdgeMode, to preassign offset
      // to source and target ports
      super.mouseShiftReleasedLeft(x, y);

      if (edge != null){ // the edge has just been created
        Graph2D graph = (Graph2D) edge.getGraph();
        EdgeRealizer er = graph.getRealizer(edge);

        // get a list of port candidates
        YList ports = getPorts(edge.source(), edge);
        Port p = er.getSourcePort();
        // snap to one of them
        snap(er, true, p.getOffsetX(), p.getOffsetY(), ports);

        // get a list of port candidates
        ports = getPorts(edge.target(), edge);
        p = er.getTargetPort();
        // snap to one of them
        snap(er, false, p.getOffsetX(), p.getOffsetY(), ports);

        // do some clean up
        edge = null;
        graph.updateViews();
      }

    }

    /**
     * Initiates the creation of an edge.
     * 
     */
    public void mousePressedLeft(double x, double y)
    {
      // simulate a pressed shift...
      // this will trigger CreateEdgeMode, to preassign offset
      // to source and target ports
      super.mouseShiftPressedLeft(x, y);
    }


    public void edgeCreated(Edge e){
      //remember the edge...
      this.edge = e;
    }

    /**
     * This method finds a list of Port objects for a specific edge/node pair
     *
     * @param onNode  the node
     * @param forEdge the edge
     * @return a list of Port objects
     */
    public YList getPorts(Node onNode, Edge forEdge) {
      YList list = new YList();
      Graph2D graph = (Graph2D) onNode.getGraph();
      NodeRealizer nr = graph.getRealizer(onNode);
      EdgeRealizer er = graph.getRealizer(forEdge);

      if (onNode == forEdge.source()) {
        // source ports are centered on bottom of the node
        list.add(new Port(0, nr.getHeight() / 2));
      } else {
        // target ports are centered at the top of the node
        list.add(new Port(0, -nr.getHeight() / 2));
      }
      return list;
    }

    /**
     * This method calculates a metric for ports and points
     *
     * @param x    the initial x offset
     * @param y    the initial y offset
     * @param port the port
     * @return the distance between the point (x,y) and the port
     */
    public static double getDistance(double x, double y, Port port) {
      return Math.sqrt((x - port.getOffsetX()) * (x - port.getOffsetX())
          + (y - port.getOffsetY()) * (y - port.getOffsetY()));
    }

    /**
     * This method chooses from a list of given ports for an edge
     * a suitable port, given an initial placement.
     *
     * @param edge   the affected edge
     * @param source whether we look at the source node
     * @param x      the initial x offset
     * @param y      the initial y offset
     * @param ports  a list of Port objects (candidates)
     */
    public void snap(EdgeRealizer edge, boolean source, double x, double y, YList ports) {
      if (ports == null || ports.size() < 1) {
        return; // do nothing
      }

      // find the closest port with regards to the getDistance function
      Port closest = (Port) ports.first();
      double dist = getDistance(x, y, closest);

      for (YCursor cursor = ports.cursor(); cursor.ok(); cursor.next()) {
        Port p = (Port) cursor.current();
        double d2 = getDistance(x, y, p);
        if (d2 < dist) {
          dist = d2;
          closest = p;
        }
      }

      // assign the port
      if (source) {
        edge.setSourcePort(closest);
      } else {
        edge.setTargetPort(closest);
      }
    }

  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new PortCreateEdgeModeDemo()).start();
      }
    });
  }
}


      
