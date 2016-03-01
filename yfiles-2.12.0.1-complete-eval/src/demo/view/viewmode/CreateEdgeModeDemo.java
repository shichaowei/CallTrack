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
import y.base.Edge;
import y.base.Node;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2D;

import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * Demonstrates how to customize CreateEdgeMode in order to control
 * the creation of edges and to provide feedback whether
   creating an edge to the node the mouse is hovering over is possible.
 * <br>
 * This demo does only allow the creation of edges that start from nodes labeled
 * "start" and end at nodes labeled "end".
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#custom_edit_mode">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class CreateEdgeModeDemo extends DemoBase {
  // whether or not to display a message box when edge creation 
  // is not allowed.
  boolean showMessage = true;

  protected void registerViewModes() {
    EditMode editMode = new EditMode() {
      //Alternately, create nodes labeled "start" and "end"
      protected void configureNode(final Graph2D graph, final Node node) {
          graph.setLabelText(node, node.index() % 2 == 0 ? "start" : "end");
      }
    };
    view.addViewMode( editMode );
    //set a custom CreateEdgeMode for the edge mode
    editMode.setCreateEdgeMode( new DemoCreateEdgeMode() );
    loadGraph("resource/popup.graphml");
  }

  protected Action createLoadAction() {
    //Overridden method to disable the Load menu in the demo
    return null;
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo
    return null;
  }

  class DemoCreateEdgeMode extends CreateEdgeMode {

    public void edgeMoved( double x, double y ) {
      super.edgeMoved( x, y );
      updateDummy( x, y );
    }

    public void edgeCreated( Edge e ) {
      getGraph2D().getRealizer( e ).setLineColor( getGraph2D().getDefaultEdgeRealizer().getLineColor() );
    }



    private void updateDummy( double x, double y ) {
      Node hitNode = DemoBase.checkNodeHit(view, x, y).getHitNode();
      if ( hitNode != null ) {
        if ( acceptTargetNode( hitNode, x, y ) ) {
          getDummyEdgeRealizer().setLineColor( Color.green );
        } else {
          getDummyEdgeRealizer().setLineColor( Color.red );
        }
      } else {
        getDummyEdgeRealizer().setLineColor( getGraph2D().getDefaultEdgeRealizer().getLineColor() );
      }
    }

    protected boolean acceptSourceNode( Node source, double x, double y ) {
      return getGraph2D().getLabelText( source ).equals("start");
    }

    protected void sourceNodeDeclined( Node source, double x, double y ) {
      if ( showMessage ) {
        cancelEdgeCreation();
        JOptionPane.showMessageDialog( this.view,
                                       "Edges may only start from nodes marked as start nodes.",
                                       "Forbidden!",
                                       JOptionPane.ERROR_MESSAGE );
      }
    }

    protected boolean acceptTargetNode( Node target, double x, double y ) {
      return getGraph2D().getLabelText( target ).equals("end");
    }

    protected void targetNodeDeclined( Node target, double x, double y ) {
      if ( showMessage ) {
        cancelEdgeCreation();
        JOptionPane.showMessageDialog( this.view,
                                       "Edges may only end at nodes marked as end nodes.",
                                       "Forbidden!",
                                       JOptionPane.ERROR_MESSAGE );

      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new CreateEdgeModeDemo()).start("Create Edge Mode Demo");
      }
    });
  }
}


      
