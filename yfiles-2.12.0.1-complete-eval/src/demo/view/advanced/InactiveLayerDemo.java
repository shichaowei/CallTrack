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
package demo.view.advanced;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.DefaultGraph2DRenderer;
import y.view.Drawable;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DRenderer;
import y.view.NodeRealizer;
import y.view.PopupMode;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;


/**
 * Demonstrates how to put a part of a graph in an inactive background layer of the view.
 * When parts of the graph are selected, then a right mouse click brings up a menu that offers to
 * put the selected part of the graph to the inactive background.  
 * If none is selected then a right mouse click brings up a popup menu that allows to bring 
 * the inactive graph part back to life.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class InactiveLayerDemo extends DemoBase {
  Graph2D inactiveGraph;
  EdgeList hiddenEdges;
  
  protected void initialize() { 
    super.initialize();
    loadGraph("resource/InactiveLayerDemo.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
    inactiveGraph = new Graph2D();
    hiddenEdges = new EdgeList();
    view.addBackgroundDrawable(new Graph2DDrawable(inactiveGraph));
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    editMode.setPopupMode(new MyPopupMode());
    view.addViewMode(editMode);
  }

  class MyPopupMode extends PopupMode {
 
    public JPopupMenu getNodePopup(final Node v) {
      JPopupMenu pm = new JPopupMenu();            
      pm.add(new AbstractAction("Deactivate") {
        public void actionPerformed(ActionEvent e) {
          deactivateSelected();          
        }
      });
      return pm;
    }
    
    public JPopupMenu getSelectionPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();      
      pm.add(new AbstractAction("Deactivate") {
        public void actionPerformed(ActionEvent e) {
          deactivateSelected();          
        }
      });      
      return pm;
    }

    void deactivateSelected() {
      Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      graph.backupRealizers(graph.selectedNodes());
      try {
        NodeList selectedNodes = new NodeList(graph.selectedNodes());
        for (NodeCursor nc = selectedNodes.nodes(); nc.ok(); nc.next()) {
          NodeRealizer r = graph.getRealizer(nc.node());
          r.setSelected(false);
          r.setFillColor(Color.lightGray);
          r.setLineColor(Color.gray);
        }
        hiddenEdges.addAll(graph.moveSubGraph(selectedNodes, inactiveGraph));
        view.updateView();
      } finally {
        graph.firePostEvent();
      }
    }
    
    public JPopupMenu getPaperPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Activate All") {
        public void actionPerformed(ActionEvent e) {
          Graph2D graph = view.getGraph2D();
          graph.firePreEvent();
          graph.backupRealizers(inactiveGraph.nodes());
          try {
            NodeList selectedNodes = new NodeList(inactiveGraph.nodes());
            for (NodeCursor nc = selectedNodes.nodes(); nc.ok(); nc.next()) {
              NodeRealizer r = graph.getRealizer(nc.node());
              r.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
              r.setLineColor(DemoDefaults.DEFAULT_NODE_LINE_COLOR);
            }
            inactiveGraph.moveSubGraph(selectedNodes, graph);
            while(!hiddenEdges.isEmpty()) {
              Edge edge = hiddenEdges.popEdge();
              if(graph.contains(edge.source()) && graph.contains(edge.target())) {
                graph.reInsertEdge(edge);
              }
            }
            view.updateView();
          } finally {
            graph.firePostEvent();
          }
        }
      });
      return pm;
    }
  }

  static class Graph2DDrawable implements Drawable {
    Graph2D graph;
    Graph2DRenderer renderer;

    Graph2DDrawable(Graph2D g) {
      this.graph = g;
      renderer = new DefaultGraph2DRenderer();
    }

    public void paint(Graphics2D gfx) {
      renderer.paint(gfx, graph);
    }

    public Rectangle getBounds() {
      return graph.getBoundingBox();
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new InactiveLayerDemo()).start("Inactive Layer Demo");
      }
    });
  }

}

    

      
