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
package demo.option;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.Node;
import y.base.NodeCursor;
import y.option.DoubleOptionItem;
import y.option.OptionHandler;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.Selections;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * <p>
 * Demonstrates how to create a node property editor for nodes.
 * This demo makes use of the "value-undefined" state of option items.
 * <p>
 * A node property editor can either be displayed for a single node
 * by double-clicking on the node or for multiple nodes by first
 * selecting the nodes and then clicking on the "Edit Node Properties" 
 * toolbar button.
 * <p>
 * The property editor will be initialized by the current settings
 * of the selected nodes. If the value of a specific property differs for two
 * selected nodes the editor will display the value as undefined. 
 * Upon closing the editor dialog, only well-defined values will be 
 * committed to the selected nodes.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/option_basic.html">Section Basic Functionality</a> in the yFiles for Java Developer's Guide
 */
public class NodePropertyEditorDemo extends DemoBase
{
  NodePropertyEditorAction nodePropertyEditorAction;
  
  public NodePropertyEditorDemo() {
    
    //open property editor upon double-clicking on a node
    view.addViewMode(new ViewMode() {
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
          Node v = getHitInfo(e).getHitNode();
          if(v != null) {
            nodePropertyEditorAction.actionPerformed(null);
          }
        }
      }
    });
  }
  
  protected JToolBar createToolBar() {
    nodePropertyEditorAction = new NodePropertyEditorAction();

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(nodePropertyEditorAction));
    return toolBar;
  }
  
  class NodePropertyEditorAction extends AbstractAction {

    NodePropertyHandler nodePropertyHandler;
    
    NodePropertyEditorAction() {
      super("Node Properties", getIconResource("resource/properties.png"));
      putValue(Action.SHORT_DESCRIPTION, "Edit the properties of selected nodes");
   
      Selections.SelectionStateObserver sso = new Selections.SelectionStateObserver() {
        protected void updateSelectionState(Graph2D graph) 
        {
          setEnabled(view.getGraph2D().selectedNodes().ok());
        } 
      };
      
      view.getGraph2D().addGraph2DSelectionListener(sso);
      view.getGraph2D().addGraphListener(sso);
  
      setEnabled(false);
    
      nodePropertyHandler = new NodePropertyHandler();
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      graph.backupRealizers();
      try {
        if (!Selections.isNodeSelectionEmpty(graph)) {
          nodePropertyHandler.updateValuesFromSelection(graph);

          final ActionListener nodePropertyListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              final Graph2D graph = view.getGraph2D();
              nodePropertyHandler.commitNodeProperties(graph);
              graph.updateViews();
            }
          };
          OptionSupport.showDialog(nodePropertyHandler, nodePropertyListener, true, view.getFrame());
        }
      } finally {
        graph.firePostEvent();
      }
    }
  }
  
  public static class NodePropertyHandler extends OptionHandler 
  {
    static final String ITEM_LABEL_TEXT = "Label Text";
    static final String ITEM_COLOR = "Color";
    static final String ITEM_WIDTH = "Width";
    static final String ITEM_HEIGHT = "Height";

    public NodePropertyHandler() {
      super("Node Properties");
      addString(ITEM_LABEL_TEXT, "").setValueUndefined(true);
      addColor(ITEM_COLOR, DemoDefaults.DEFAULT_NODE_COLOR, false, true, true, true).setValueUndefined(true);
      addDouble(ITEM_WIDTH, 1.0).setValueUndefined(true);
      addDouble(ITEM_HEIGHT, 1.0).setValueUndefined(true);

      getItem(ITEM_WIDTH).setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(1.0));
      getItem(ITEM_HEIGHT).setAttribute(DoubleOptionItem.ATTRIBUTE_MIN_VALUE, new Double(1.0));
    }

    /**
     * Retrieves the values from the set of selected nodes (actually node 
     * realizers) and stores them in the respective option items. 
     */
    public void updateValuesFromSelection(Graph2D graph)
    {
      NodeCursor nc = graph.selectedNodes();
      NodeRealizer nr = graph.getRealizer(nc.node());
      
      // Get the initial values from the first selected node. 
      String label = nr.getLabelText();
      boolean sameLabels = true;
      Color color = nr.getFillColor();
      boolean sameColor = true;
      double width = nr.getWidth();
      boolean sameWidth = true;
      double height = nr.getHeight();
      boolean sameHeight = true;
      
      // Get all further values from the remaining set of selected node 
      // realizers. 
      if (nc.size() > 1)
      {
        for (nc.next(); nc.ok(); nc.next())
        {
          nr = graph.getRealizer(nc.node());
          
          if (sameLabels && !label.equals(nr.getLabelText()))
            sameLabels = false;
          if (sameColor && color != nr.getFillColor())
            sameColor = false;
          if (sameWidth && width != nr.getWidth())
            sameWidth = false;
          if (sameHeight && height != nr.getHeight())
            sameHeight = false;
          
          if (!(sameLabels | sameColor | sameWidth | sameHeight))
            break;
        }
      }
      
      // If, for a single property, there are multiple values present in the set 
      // of selected node realizers, then the respective option item is set to 
      // indicate an "undefined value" state. 
      // Note that property "valueUndefined" for an option item is set *after* 
      // its value has actually been modified! 
      set(ITEM_LABEL_TEXT, label);
      getItem(ITEM_LABEL_TEXT).setValueUndefined(!sameLabels);
      
      set(ITEM_COLOR, color);
      getItem(ITEM_COLOR).setValueUndefined(!sameColor);
      
      set(ITEM_WIDTH, new Double(width));
      getItem(ITEM_WIDTH).setValueUndefined(!sameWidth);
      
      set(ITEM_HEIGHT, new Double(height));
      getItem(ITEM_HEIGHT).setValueUndefined(!sameHeight);
    }
   
    public void commitNodeProperties(Graph2D graph) 
    {
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
      {
        Node n = nc.node();
        NodeRealizer nr = graph.getRealizer(n);
        
        if (!getItem(ITEM_LABEL_TEXT).isValueUndefined())
          nr.setLabelText(getString(ITEM_LABEL_TEXT));
        if (!getItem(ITEM_COLOR).isValueUndefined())
          nr.setFillColor((Color)get(ITEM_COLOR));
        if (!getItem(ITEM_WIDTH).isValueUndefined())
          nr.setWidth(getDouble(ITEM_WIDTH));
        if (!getItem(ITEM_HEIGHT).isValueUndefined())
          nr.setHeight(getDouble(ITEM_HEIGHT));
      }
    }
  }
  
  /** Launches this demo. */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new NodePropertyEditorDemo().start("Node Property Editor Demo");
      }
    });
  }
}
