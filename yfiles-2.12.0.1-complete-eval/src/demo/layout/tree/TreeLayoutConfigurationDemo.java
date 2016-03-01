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
package demo.layout.tree;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Node;
import y.base.NodeList;
import y.layout.tree.GenericTreeLayouter;
import y.view.Graph2D;
import y.view.NavigationMode;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Locale;

/**
 * This demo shows the usage of the TreeLayoutConfiguration.
 * The TreeLayoutConfiguration offers examples how to configure and run the {@link GenericTreeLayouter}.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/cls_GenericTreeLayouter.html">Section Generic Tree Layout</a> in the yFiles for Java Developer's Guide
 **/
public class TreeLayoutConfigurationDemo extends DemoBase {
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new TreeLayoutConfigurationDemo().start();
      }
    });    
  }

  private void layout(TreeLayoutConfiguration configuration) {
    GenericTreeLayouter genericTreeLayouter = new GenericTreeLayouter();
    configuration.layout(genericTreeLayouter, view.getGraph2D());
    view.fitContent();
    view.updateView();
  }

  protected void initialize() {
    loadGraph("resource/dfb2004.graphml");
    layout(TreeLayoutConfiguration.PLAYOFFS);
  }

  protected void registerViewModes() {
    view.addViewMode(new NavigationMode());
  }
  
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);
    DemoDefaults.applyRealizerDefaults(view.getGraph2D());
  }
  
  protected JToolBar createToolBar() {
    final AbstractAction[] actions = {
        new AbstractAction("Playoffs") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            loadGraph("resource/dfb2004.graphml");
            layout(TreeLayoutConfiguration.PLAYOFFS);
          }
        },
        new AbstractAction("Playoffs Double") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            loadGraph("resource/dfb2004.graphml");
            layout(TreeLayoutConfiguration.PLAYOFFS_DOUBLE);
          }
        },
        new AbstractAction("Double Line") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            createTree(view.getGraph2D(), new int[]{1, 4, 6, 8});
            layout(TreeLayoutConfiguration.DOUBLE_LINE);
          }
        },
        new AbstractAction("Bus") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            createTree(view.getGraph2D(), new int[]{1, 4, 3, 8});
            layout(TreeLayoutConfiguration.BUS);
          }
        },
        new AbstractAction("Layered Tree") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            createTree(view.getGraph2D(), new int[]{1, 4, 4, 4});
            layout(TreeLayoutConfiguration.LAYERED_TREE);
          }
        },
        new AbstractAction("Default Delegating") {
          public void actionPerformed(ActionEvent e) {
            view.getGraph2D().clear();
            createTree(view.getGraph2D(), new int[]{1, 4, 3, 8});
            layout(TreeLayoutConfiguration.DEFAULT_DELEGATING);
          }
        },
    };

    final JToolBar jtb = super.createToolBar();
    jtb.addSeparator();
    final ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < actions.length; ++i) {
      final JToggleButton jb = new JToggleButton(actions[i]);
      jb.setSelected(i == 0);
      jtb.add(jb);
      group.add(jb);
    }
    return jtb;
  }

  /**
   * Overwritten to disable undo/redo because this is not an editable demo.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Overwritten to disable clipboard because this is not an editable demo.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  /**
   * Creates a tree with specified number of children per parent and layer.
   */
  public static Graph2D createTree(Graph2D graph, int[] childrenCountPerLayer) {
    if (childrenCountPerLayer.length == 0) {
      return graph;
    }
    if (childrenCountPerLayer[0] != 1) {
      throw new IllegalArgumentException("The first layer must contain 1 node");
    }

    NodeList lastLayerContent = new NodeList();

    //First layer
    Node node = graph.createNode();
    lastLayerContent.add(node);

    for (int i = 1; i < childrenCountPerLayer.length; i++) {
      int childrenCount = childrenCountPerLayer[i];

      NodeList newLayerContent = new NodeList();
      for (int j = 0; j < lastLayerContent.size(); j++) {
        Node parent = (Node) lastLayerContent.get(j);

        for (int k = 0; k < childrenCount; k++) {
          Node child = graph.createNode();
          newLayerContent.add(child);
          graph.setLabelText(child, String.valueOf(graph.N()));
          graph.createEdge(parent, child);
        }
      }

      lastLayerContent = newLayerContent;
    }
    return graph;
  }

}
