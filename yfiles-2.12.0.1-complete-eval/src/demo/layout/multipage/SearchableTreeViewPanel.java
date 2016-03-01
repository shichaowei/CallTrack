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
package demo.layout.multipage;

import y.view.Graph2D;
import y.view.hierarchy.DefaultNodeChangePropagator;
import y.view.hierarchy.HierarchyJTree;
import y.view.hierarchy.HierarchyManager;
import y.view.hierarchy.HierarchyTreeModel;
import y.view.hierarchy.HierarchyTreeTransferHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A panel that contains a tree view for the given graph model as well as a search field to search for specific nodes.
 */
class SearchableTreeViewPanel extends JPanel {
  private JTree jTree;
  private TreePath lastMatch;

  SearchableTreeViewPanel( final Graph2D model ) {
    setLayout(new BorderLayout());
    jTree = configureHierarchyJTree(model);
    jTree.setExpandsSelectedPaths(true);
    jTree.setDragEnabled(false);

    final JScrollPane scrollPane = new JScrollPane(jTree);
    scrollPane.setPreferredSize(new Dimension(200, 0));
    scrollPane.setMinimumSize(new Dimension(200, 0));
    add(addSearchPanel(jTree), BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
  }

  JTree getTree() {
    return jTree;
  }

  private JPanel addSearchPanel(final JTree currentTree) {
    final JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
    searchPanel.setBorder(new EmptyBorder(2, 5, 2, 2));
    searchPanel.add(new JLabel("Search"), BorderLayout.WEST);
    final JTextField searchField = new JTextField();

    // A document listener handles changes of the search string.
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        updateSelection(searchField, currentTree);
      }

      public void removeUpdate(DocumentEvent e) {
        updateSelection(searchField, currentTree);
      }

      public void changedUpdate(DocumentEvent e) {
        updateSelection(searchField, currentTree);
      }
    });

    // A key listener handles the special keys ENTER and ESCAPE.
    // They do not change the document in any way.
    searchField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
          // Clear search field, if ESCAPE is pressed.
          clearSearchField(searchField);

          // Stop further event handling.
          // -> Do not remove the focus.
          e.consume();
        } else if (keyCode == KeyEvent.VK_ENTER) {
          // Scroll next match into view, if ENTER is pressed.
          String searchString = searchField.getText();
          if (currentTree != null && currentTree.getModel() != null && searchString != null && searchString.length() > 0) {
            TreePath match = findNextMatch(searchString, new TreePath(currentTree.getModel().getRoot()), currentTree);
            if (match != null) {
              selectMatch(match, currentTree);
            } else {
              lastMatch = null;
              TreePath wrappedMatch = findNextMatch(searchString, new TreePath(currentTree.getModel().getRoot()),
                  currentTree);
              if (wrappedMatch != null) {
                selectMatch(wrappedMatch, currentTree);
              } else {
                noMatchExists(searchField);
              }
            }
          }
        }
      }
    });
    searchPanel.add(searchField, BorderLayout.CENTER);
    return searchPanel;
  }

  private void selectMatch(final TreePath match, final JTree actualTree) {
    actualTree.setSelectionPath(match);
    actualTree.scrollPathToVisible(match);
    lastMatch = match;
  }

  private void clearSearchField(final JTextField searchField) {
    searchField.setText("");
    searchField.setBackground(Color.WHITE);
    searchField.setForeground(Color.BLACK);
    lastMatch = null;
  }

  private void noMatchExists(final JTextField searchField) {
    searchField.setBackground(Color.RED);
    searchField.setForeground(Color.WHITE);
    lastMatch = null;
  }

  private void updateSelection(final JTextField searchField, final JTree actualTree) {
    searchField.setBackground(Color.WHITE);
    searchField.setForeground(Color.BLACK);
    lastMatch = null;
    if (actualTree != null && actualTree.getModel() != null) {
      actualTree.clearSelection();
      String searchString = searchField.getText();
      if (searchString != null && searchString.length() > 0) {
        TreePath match = findNextMatch(searchString, new TreePath(actualTree.getModel().getRoot()), actualTree);
        if (match != null) {
          selectMatch(match, actualTree);
        } else {
          noMatchExists(searchField);
        }
      }
    }
  }

  //find the next matching element in the tree that matches with the searchString
  private TreePath findNextMatch(String searchString, TreePath parent, final JTree actualTree) {
    TreeModel model = actualTree.getModel();
    Object treeRoot = parent.getLastPathComponent();
    for (int i = 0; i < model.getChildCount(treeRoot); i++) {
      Object treeNode = model.getChild(treeRoot, i);
      TreePath path = parent.pathByAddingChild(treeNode);
      if (isMatch(treeNode, searchString)) {
        if (lastMatch != null) {
          if (path.equals(lastMatch)) {
            lastMatch = null;
          }
        } else {
          return path;
        }
      }
      TreePath match = findNextMatch(searchString, path, actualTree);
      if (match != null) {
        return match;
      }
    }
    return null;
  }

  private boolean isMatch(Object treeNode, String searchString) {
    // currently case insensitive prefix match
    // This can be customized/made configurable at a later stage.
    String text = treeNode.toString().toUpperCase();
    return text.startsWith(searchString.toUpperCase());
  }

  private JTree configureHierarchyJTree(Graph2D model) {
    //propagates text label changes on nodes as change events on the hierarchy.
    model.addGraph2DListener(new DefaultNodeChangePropagator());

    //create a TreeModel, that represents the hierarchy of the nodes.
    HierarchyManager hierarchy = model.getHierarchyManager();
    HierarchyTreeModel treeModel = new HierarchyTreeModel(hierarchy);

    //use a convenience comparator that sorts the elements in the tree model
    //folder/group nodes will come before normal nodes
    treeModel.setChildComparator(HierarchyTreeModel.createNodeStateComparator(hierarchy));

    //display the graph hierarchy in a special JTree using the given TreeModel
    final JTree tree = new HierarchyJTree(hierarchy, treeModel);
    tree.setEditable(false);

    //add drag and drop functionality to HierarchyJTree. The drag and drop gesture
    //will allow to reorganize the group structure using HierarchyJTree.
    tree.setDragEnabled(true);
    tree.setTransferHandler(new HierarchyTreeTransferHandler(hierarchy));
    return tree;
  }
}
