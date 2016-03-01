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
package demo.view.hierarchy;

import y.view.Graph2D;
import y.view.hierarchy.DefaultNodeChangePropagator;
import y.view.hierarchy.HierarchyJTree;
import y.view.hierarchy.HierarchyManager;
import y.view.hierarchy.HierarchyTreeModel;
import y.view.hierarchy.HierarchyTreeTransferHandler;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * This demo shows how to use class {@link y.view.hierarchy.HierarchyJTree} to display the hierarchical structure.
 *
 * HierarchyJTree provides a different view on the graph structure, as well as (optionally) navigational actions
 * and support for changes in the hierarchical structure.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/hier_mvc_view.html">Section View Implementations</a> in the yFiles for Java Developer's Guide
 */
public class HierarchyJTreeDemo extends GroupingDemo {

  /**
   * Instantiates this demo. Builds the GUI.
   */
  public HierarchyJTreeDemo() {
    JTree tree = configureHierarchyJTree();

    //plug the gui elements together and add them to the pane
    JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setPreferredSize(new Dimension(150, 0));
    scrollPane.setMinimumSize(new Dimension(150, 0));
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, view);
    view.fitContent();
    contentPane.add(splitPane, BorderLayout.CENTER);
  }

  protected JTree configureHierarchyJTree() {
    Graph2D rootGraph = view.getGraph2D();

    //propagates text label changes on nodes as change events
    //on the hierarchy.
    rootGraph.addGraph2DListener(new DefaultNodeChangePropagator());

    //create a TreeModel, that represents the hierarchy of the nodes.
    HierarchyManager hierarchy = getHierarchyManager();
    HierarchyTreeModel htm = new HierarchyTreeModel(hierarchy);

    //use a convenience comparator that sorts the elements in the tree model
    //folder/group nodes will come before normal nodes
    htm.setChildComparator(HierarchyTreeModel.createNodeStateComparator(hierarchy));

    //display the graph hierarchy in a special JTree using the given TreeModel
    JTree tree = new HierarchyJTree(hierarchy, htm);

    //add a navigational action to the tree - when double clicking a node in the tree,
    //it will be centered in the view (if necessary navigating into an inner graph of a folder node)
    tree.addMouseListener(new HierarchyJTreeDoubleClickListener(view));

    //add drag and drop functionality to HierarchyJTree. The drag and drop gesture
    //will allow to reorganize the group structure using HierarchyJTree.
    tree.setDragEnabled(true);
    tree.setTransferHandler(new HierarchyTreeTransferHandler(hierarchy));
    return tree;
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new HierarchyJTreeDemo()).start();
      }
    });
  }
}
