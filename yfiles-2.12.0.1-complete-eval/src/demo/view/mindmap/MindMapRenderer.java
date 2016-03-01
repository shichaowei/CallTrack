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
package demo.view.mindmap;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.YList;
import y.util.Cursors;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.OrderRenderer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Renders the elements of a mind map in a specific order. This specific
 * rendering order is
 * <ol>
 * <li>normal edges,</li>
 * <li>nodes,</li>
 * <li>cross-reference edges, and</li>
 * <li>node labels</li>
 * </ol>
 */
class MindMapRenderer implements OrderRenderer {
  /**
   * The element order for hit-testing and rendering the mind map.
   */
  private final Graph2DTraversal order;

  /**
   * Initializes a new <code>MindMapRenderer</code> instance.
   */
  MindMapRenderer() {
    this.order = new MindMapTraversal();
  }

  /**
   * Returns the element order for hit-testing and rendering the mind map in
   * high-detail mode.
   */
  public Graph2DTraversal getPaintOrder() {
    return order;
  }

  /**
   * Returns the element order for hit-testing and rendering the mind map in
   * low-detail mode.
   */
  public Graph2DTraversal getSloppyPaintOrder() {
    return order;
  }

  /**
   * Renders the given graph in high-detail mode.
   * @param gfx the graphics context to paint upon.
   * @param graph the mind map to paint.
   */
  public void paint( final Graphics2D gfx, final Graph2D graph) {
    paintCore(gfx, graph, false);
  }

  /**
   * Renders the given graph in low-detail mode.
   * @param gfx the graphics context to paint upon.
   * @param graph the mind map to paint.
   */
  public void paintSloppy( final Graphics2D gfx, final Graph2D graph ) {
    paintCore(gfx, graph, true);
  }

  /**
   * Renders the given graph.
   * @param gfx the graphics context to paint upon.
   * @param graph the mind map to paint.
   * @param sloppy if <code>true</code> low-detail rendering is used; otherwise
   * high-detail rendering is used.
   */
  private void paintCore(
          final Graphics2D gfx, final Graph2D graph, final boolean sloppy
  ) {
    Rectangle clip = gfx.getClipBounds();
    if (clip == null) {
      clip = graph.getBoundingBox();
    }
    final double cx = clip.getX();
    final double cy = clip.getY();
    final double cw = clip.getWidth();
    final double ch = clip.getHeight();

    final Rectangle2D.Double tmp = new Rectangle2D.Double();
    final int types =
            Graph2DTraversal.NODES |
            Graph2DTraversal.EDGES |
            Graph2DTraversal.NODE_LABELS;
    for (Iterator it = order.firstToLast(graph, types); it.hasNext();) {
      final Object o = it.next();

      if (o instanceof Node) {
        final NodeRealizer nr = graph.getRealizer((Node) o);
        // check if the node is in the visible region before painting
        tmp.setFrame(0, 0, -1, -1);
        nr.calcUnionRect(tmp);
        if (tmp.intersects(cx, cy, cw, ch)) {
          // node is in the visible region, so paint it
          if (sloppy) {
            nr.paintSloppy(gfx);
          } else {
            nr.paint(gfx);
          }
        }
      } else if (o instanceof Edge) {
        final EdgeRealizer er = graph.getRealizer((Edge) o);
        // check if the edge is in the visible region before painting
        if (er.intersects(clip)) {
          // edge is in the visible region, so paint it
          if (sloppy) {
            er.paintSloppy(gfx);
          } else {
            er.paint(gfx);
          }
        }
      } else if (o instanceof NodeLabel) {
        final NodeLabel nl = (NodeLabel) o;
        // check if the label is in the visible region before painting
        if (nl.intersects(cx, cy, cw, ch)) {
          // label is in the visible region, so paint it
          nl.paint(gfx);
        }
      }
    }
  }


  /**
   * Specifies the element order for hit-testing and rendering a mind map.
   */
  private static final class MindMapTraversal implements Graph2DTraversal {
    /**
     * Returns the element order for rendering the mind map.
     */
    public Iterator firstToLast(Graph2D graph, int elementTypes) {
      return collectElements(graph, elementTypes).iterator();
    }

    /**
     * Returns the element order for hit testing in the mind map.
     */
    public Iterator lastToFirst(Graph2D graph, int elementTypes) {
      final YList list = collectElements(graph, elementTypes);
      return Cursors.createReverseIterator(list.cursor());
    }

    //collect elements in firstToLast order
    private YList collectElements( final Graph2D graph, final int types ) {
      final YList elements = new YList();
      final YList crossReferences = new YList();
      final YList bends = new YList();

      //first add edges
      final boolean addEdges = (Graph2DTraversal.EDGES & types) != 0;
      final boolean addBends = (Graph2DTraversal.BENDS & types) != 0;
      if (addEdges || addBends) {
        final ViewModel model = ViewModel.instance;
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          //save cross references to add them later
          if (model.isCrossReference(ec.edge())) {
            if (addEdges) {
              crossReferences.add(ec.edge());
            }
            final EdgeRealizer er = graph.getRealizer(ec.edge());
            if (addBends) {
              bends.addAll(er.bends());
            }
          } else {
            if (addEdges) {
              elements.add(ec.edge());
            }
          }
        }
      }


      final YList nodeLabels = new YList();

      //next, add nodes
      final boolean addNodes = (Graph2DTraversal.NODES & types) != 0;
      final boolean addNodeLabels = (Graph2DTraversal.NODE_LABELS & types) != 0;
      if (addNodes || addNodeLabels) {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          //save node labels to add them later
          if (addNodeLabels) {
            final NodeRealizer nr = graph.getRealizer(nc.node());
            for (int i = 0, n = nr.labelCount(); i < n; ++i) {
              nodeLabels.add(nr.getLabel(i));
            }
          }
          if (addNodes) {
            elements.add(nc.node());
          }
        }
      }

      //add cross edges
      elements.splice(crossReferences);
      //add bends
      elements.splice(bends);
      //finally add node labels
      elements.splice(nodeLabels);
      return elements;
    }
  }
}
