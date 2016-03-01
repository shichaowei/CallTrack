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
package demo.io.graphml;


import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.option.OptionHandler;
import y.view.EditMode;
import y.view.PopupMode;
import y.view.TooltipMode;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * This demo shows how to configure GraphMLIOHandler to be able to handle
 * extra node and edge data of simple type.
 * Additional data for a node or an edge can be edited by right-clicking on the
 * corresponding element.
 * The element tool tip will show the currently set data values for each element.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_extension">Section Reading and Writing Additional Data</a> in the yFiles for Java Developer's Guide
 */
public class SimpleAttributesDemo extends GraphMLDemo {

  /** stores a boolean value for each node **/
  private NodeMap node2BoolMap;

  /** stores an int value for each edge **/
  private EdgeMap edge2IntMap;


  protected void loadInitialGraph() {
    loadGraph("resources/custom/simple-attributes.graphml");
  }

  protected TooltipMode createTooltipMode() {
    TooltipMode tooltipMode = new TooltipMode() {
      /**
       * Overwrites {@link TooltipMode#getNodeTip(y.base.Node)} to set a customized tooltip text.
       * @param node the node for which the tooltip is set
       * @return the tooltip string that is used by the <code>TooltipMode</code>
       */
      protected String getNodeTip(Node node) {
        return "Node:BooleanValue=" + node2BoolMap.getBool(node);
      }

      /**
       * Overwrites {@link TooltipMode#getEdgeTip(y.base.Edge)} to set a customized tooltip text.
       * @param edge the edge for which the tooltip is set
       * @return the tooltip string that is used by the <code>TooltipMode</code>
       */
      protected String getEdgeTip(Edge edge) {
        return "Edge:IntValue=" + edge2IntMap.getInt(edge);
      }
    };
    return tooltipMode;
  }

  /**
   * Configures GraphMLIOHandler to read and write additional node and edge data
   * of a simple type.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
      //Create maps that store the attributes
    if (node2BoolMap == null) {
      node2BoolMap = view.getGraph2D().createNodeMap();
    }
    if (edge2IntMap == null) {
      edge2IntMap = view.getGraph2D().createEdgeMap();
    }

    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();

    //  <key id="d1" for="node" attr.name="BooleanValue" attr.type="boolean"/>
    ioHandler.getGraphMLHandler().addInputDataAcceptor("BooleanValue", node2BoolMap, KeyScope.NODE, KeyType.BOOLEAN);
    ioHandler.getGraphMLHandler().addOutputDataProvider("BooleanValue", node2BoolMap, KeyScope.NODE, KeyType.BOOLEAN);

    //  <key id="d3" for="edge" attr.name="IntValue" attr.type="int"/>
    ioHandler.getGraphMLHandler().addInputDataAcceptor("IntValue", edge2IntMap, KeyScope.EDGE, KeyType.INT);
    ioHandler.getGraphMLHandler().addOutputDataProvider("IntValue", edge2IntMap, KeyScope.EDGE, KeyType.INT);
    return ioHandler;
  }


  protected String[] getExampleResources() {
    return null;
  }

  /**
   * Create an edit mode that displays a context-sensitive popup-menu when
   * right-clicking on a node or an edge.
   */
  protected EditMode createEditMode() {
    EditMode mode = super.createEditMode();

    mode.setPopupMode(new PopupMode() {
      public JPopupMenu getNodePopup(Node v) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Node Attribute...", v, node2BoolMap, KeyType.BOOLEAN));
        return pm;
      }

      public JPopupMenu getEdgePopup(Edge e) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Edge Attribute...", e, edge2IntMap, KeyType.INT));
        return pm;
      }
    });

    return mode;
  }


  /**
   * Editor action for the additional node and edge attributes.
   */
  class EditAttributeAction extends AbstractAction {
    private Object object;
    private DataMap dataMap;
    private KeyType dataType;

    private OptionHandler op;

    EditAttributeAction(String name, Object object, DataMap dataMap, KeyType dataType) {
      super(name);
      this.object = object;
      this.dataMap = dataMap;
      this.dataType = dataType;
      op = new OptionHandler(name);
      if (dataType == KeyType.BOOLEAN) {
        op.addBool("Boolean Value", dataMap.getBool(object));
      } else if (dataType == KeyType.INT) {
        op.addInt("Integer Value", dataMap.getInt(object));
      }
    }

    public void actionPerformed(ActionEvent actionEvent) {
      if (op.showEditor()) {
        if (dataType == KeyType.BOOLEAN) {
          dataMap.setBool(object, op.getBool("Boolean Value"));
        } else if (dataType == KeyType.INT) {
          dataMap.setInt(object, op.getInt("Integer Value"));          
        }        
        graphMLPane.updateGraphMLText(view.getGraph2D());
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SimpleAttributesDemo()).start();
      }
    });
  }
}
