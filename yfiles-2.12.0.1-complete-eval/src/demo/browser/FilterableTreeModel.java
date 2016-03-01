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
package demo.browser;

import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A tree model that supports filtering its tree nodes.
 *
 */
class FilterableTreeModel implements TreeModel {
  EventListenerList listenerList;

  private final FilterableTreeNode root;

  FilterableTreeModel( final FilterableTreeNode root ) {
    this.root = root;
    listenerList = new EventListenerList();
  }

  public void addTreeModelListener( final TreeModelListener l ) {
    listenerList.add(TreeModelListener.class, l);
  }

  public void removeTreeModelListener( final TreeModelListener l ) {
    listenerList.remove(TreeModelListener.class, l);
  }

  public Object getRoot() {
    return root;
  }

  public int getChildCount( final Object parent ) {
    return ((FilterableTreeNode) parent).getChildCount();
  }

  public boolean isLeaf( final Object node ) {
    return ((FilterableTreeNode) node).isLeaf();
  }

  public Object getChild( final Object parent, final int index ) {
    return ((FilterableTreeNode) parent).getChildAt(index);
  }

  public int getIndexOfChild( final Object parent, final Object child ) {
    return ((FilterableTreeNode) parent).getIndex((FilterableTreeNode) child);
  }

  public void valueForPathChanged( final TreePath path, final Object newValue ) {
    // not supported
  }

  void filterPre( final Filter filter ) {
    root.filterPre(filter);
    fireTreeStructureChanged();
  }

  void filterPost( final Filter filter ) {
    root.filterPost(filter);
    fireTreeStructureChanged();
  }

  FilterableTreeNode find( final Filter filter ) {
    final FilterableTreeNode node = findUnfiltered(filter, root);
    if (node != null) {
      // the node corresponding to the specified qualified name may
      // not be part of the model due o filtering
      // if this is the case, reinsert the node and all its ancestors
      TreeNode filtered = null;
      for (TreeNode child = node, parent = child.getParent();
           parent != null;
           child = parent, parent = child.getParent()) {
        if (parent.getIndex(child) < 0) {
          ((FilterableTreeNode) parent).unfilter((FilterableTreeNode) child);
          filtered = child;
        }
      }
      if (filtered != null) {
        fireTreeNodesInserted(createPath(filtered.getParent()), filtered);
      }
    }
    return node;
  }

  private static FilterableTreeNode findUnfiltered(
          final Filter filter, final FilterableTreeNode node
  ) {
    if (filter.accept(node.getUserObject())) {
      return node;
    }

    FilterableTreeNode result = null;
    for (Iterator it = node.unfiltered(); it.hasNext();) {
      result = findUnfiltered(filter, (FilterableTreeNode) it.next());
      if (result != null) {
        return result;
      }
    }
    return result;
  }

  static TreeNode[] createPath( final TreeNode node ) {
    final LinkedList list = new LinkedList();
    for (TreeNode tn = node; tn != null; tn = tn.getParent()) {
      list.addFirst(tn);
    }
    final TreeNode[] array = new TreeNode[list.size()];
    list.toArray(array);
    return array;
  }

  private void fireTreeNodesInserted( final TreeNode[] path, final TreeNode newNode ) {
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        if (e == null) {
          final TreeNode parent = newNode.getParent();
          e = new TreeModelEvent(
                  this,
                  path,
                  new int[]{parent.getIndex(newNode)},
                  new Object[]{newNode});
        }
        ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
      }
    }
  }

  private void fireTreeStructureChanged() {
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        if (e == null) {
          final int[] idcs = new int[root.getChildCount()];
          for (int j = 0; j < idcs.length; ++j) {
            idcs[j] = j;
          }
          e = new TreeModelEvent(this, new Object[]{root}, idcs, root.toArray());
        }
        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
      }
    }
  }
}
