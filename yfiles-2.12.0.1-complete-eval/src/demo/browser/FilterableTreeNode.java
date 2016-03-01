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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.tree.TreeNode;

/**
 * A tree node that supports filtering its children.
 *
 */
class FilterableTreeNode implements TreeNode {
  private static final FilterableTreeNode[] EMTPY = new FilterableTreeNode[0];

  private final Displayable data;
  private final boolean allowsChildren;

  private final List children;
  private final List filtered;

  private FilterableTreeNode parent;

  FilterableTreeNode( final Displayable data ) {
    this.data = data;
    this.allowsChildren = !(data instanceof Demo);

    if (allowsChildren) {
      children = new ArrayList(4);
      filtered = new ArrayList(4);
    } else {
      children = null;
      filtered = null;
    }
  }

  Displayable getUserObject() {
    return data;
  }

  public Enumeration children() {
    if (allowsChildren) {
      return new IteratorAdaptor(filtered.iterator());
    } else {
      return new Enumeration() {
        public boolean hasMoreElements() {
          return false;
        }

        public Object nextElement() {
          throw new NoSuchElementException();
        }
      };
    }
  }

  public int getChildCount() {
    if (allowsChildren) {
      return filtered.size();
    } else {
      return 0;
    }
  }

  public boolean getAllowsChildren() {
    return allowsChildren;
  }

  public boolean isLeaf() {
    return getChildCount() == 0;
  }

  public TreeNode getParent() {
    return parent;
  }

  public TreeNode getChildAt( final int childIndex ) {
    if (allowsChildren) {
      return (TreeNode) filtered.get(childIndex);
    } else {
      throw new ArrayIndexOutOfBoundsException("node has no children");
    }
  }

  public int getIndex( final TreeNode node ) {
    if (node == null) {
      return -1;
    } else {
      if (allowsChildren) {
        if (node.getParent() == this) {
          return filtered.indexOf(node);
        } else {
          return -1;
        }
      } else {
        return -1;
      }
    }
  }

  void add( final FilterableTreeNode child ) {
    child.parent = this;
    children.add(child);
    filtered.add(child);
  }

  void removeAllChildren() {
    for (Iterator it = children.iterator(); it.hasNext();) {
      final FilterableTreeNode next = (FilterableTreeNode) it.next();
      next.parent = null;
    }
    children.clear();
    filtered.clear();
  }

  boolean filterPost( final Filter filter ) {
    if (allowsChildren) {
      if (children.isEmpty()) {
        return filter == null || filter.accept(data);
      } else {
        filtered.clear();
        for (Iterator it = children.iterator(); it.hasNext();) {
          final FilterableTreeNode next = (FilterableTreeNode) it.next();
          if (next.filterPost(filter)) {
            filtered.add(next);
          }
        }
        return !filtered.isEmpty();
      }
    } else {
      return filter == null || filter.accept(data);
    }
  }

  boolean filterPre( final Filter filter ) {
    final boolean accepted = filter == null || filter.accept(data);
    if (allowsChildren) {
      if (children.isEmpty()) {
        return accepted;
      } else {
        filtered.clear();
        final Filter tmp = accepted ? null : filter;
        for (Iterator it = children.iterator(); it.hasNext();) {
          final FilterableTreeNode next = (FilterableTreeNode) it.next();
          if (next.filterPre(tmp)) {
            filtered.add(next);
          }
        }
        return accepted || !filtered.isEmpty();
      }
    } else {
      return accepted;
    }
  }

  FilterableTreeNode[] toArray() {
    if (allowsChildren) {
      final FilterableTreeNode[] array = new FilterableTreeNode[filtered.size()];
      filtered.toArray(array);
      return array;
    } else {
      return EMTPY;
    }
  }

  Iterator unfiltered() {
    if (allowsChildren) {
      return new ViewIterator(children.iterator());
    } else {
      return new Iterator() {
        public void remove() {
          throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
          return false;
        }

        public Object next() {
          throw new NoSuchElementException();
        }
      };
    }
  }

  void unfilter( final FilterableTreeNode node ) {
    if (filtered.isEmpty()) {
      filtered.add(node);
    } else {
      final HashMap idx = new HashMap();
      int i = 0;
      for (Iterator it = children.iterator(); it.hasNext(); ++i) {
        idx.put(it.next(), new Integer(i));
      }

      final int cIdx = ((Integer) idx.get(node)).intValue();
      int add = 0;
      for (Iterator it = filtered.iterator(); it.hasNext(); ++add) {
        final int tmp = ((Integer) idx.get(it.next())).intValue();
        if (tmp > cIdx) {
          break;
        }
      }
      filtered.add(add, node);
    }
  }

  private static final class IteratorAdaptor implements Enumeration {
    private final Iterator it;

    IteratorAdaptor( final Iterator it ) {
      this.it = it;
    }

    public boolean hasMoreElements() {
      return it.hasNext();
    }

    public Object nextElement() {
      return it.next();
    }
  }

  private static final class ViewIterator implements Iterator {
    private final Iterator it;

    ViewIterator( final Iterator it ) {
      this.it = it;
    }

    public boolean hasNext() {
      return it.hasNext();
    }

    public Object next() {
      return it.next();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
