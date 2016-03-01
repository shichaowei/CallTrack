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

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.tree.BalloonLayouter;
import y.view.Arrow;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LocalViewCreator;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.BorderFactory;


/**
 * Overview component that displays all referenced pages for a given page graph.
 *
 */
class MultiPageOverview extends Graph2DView {
  MultiPageOverview(
          final Graph2DView pageView,
          final MultiPageGraph2DBuilder pageBuilder
  ) {
    setPreferredSize(new Dimension(200, 200));
    setBorder(BorderFactory.createLineBorder(Color.GRAY));

    // disable sloppy painting to ensure that labels are always painted
    // regardless of current zoom level
    setPaintDetailThreshold(0);

    getGraph2D().getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);

    // the MultiPageOverviewCreator rebuilds this component's graph each
    // time the graph of the specified page view is replaced with a new one
    final MultiPageOverviewCreator creator =
            new MultiPageOverviewCreator(pageView, this, pageBuilder);
    pageView.addPropertyChangeListener("Graph2D", new PropertyChangeListener() {
      public void propertyChange( final PropertyChangeEvent e ) {
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            creator.updateViewGraph();
          }
        });
      }
    });
  }

  /**
   * Creates a graph that displays all referenced pages for a given page graph. 
   */
  private static final class MultiPageOverviewCreator extends LocalViewCreator {
    private static final Color PAGE_BACKGROUND = new Color(230, 230, 230);
    private final Graph2DView source;
    private final Graph2DView target;
    private final MultiPageGraph2DBuilder pageBuilder;

    MultiPageOverviewCreator(
            final Graph2DView source,
            final Graph2DView target,
            final MultiPageGraph2DBuilder pageBuilder
    ) {
      this.source = source;
      this.target = target;
      this.pageBuilder = pageBuilder;

      // layout algorithm to lay out the multi-page overview graph in a
      // star-shaped fashion
      final BalloonLayouter layouter = new BalloonLayouter();
      layouter.setComparator(new Comparator() {
        public int compare( final Object o1, final Object o2 ) {
          final int i1 = ((Edge) o1).index();
          final int i2 = ((Edge) o2).index();
          if (i1 < i2) {
            return 1;
          } else if (i1 > i2) {
            return -1;
          } else {
            return 0;
          }
        }
      });
      setLayouter(layouter);
    }

    protected void buildViewGraph() {
      final Graph2D model = getModel();
      if (model.isEmpty()) {
        return;
      }

      // determine the page represented by the given page graph
      // as well as all referenced pages
      final TreeSet pageNos = new TreeSet();
      int currentPageNo = -1;
      for (NodeCursor nc = model.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        final int refPageNo = pageBuilder.getReferencedPageNo(node);
        if (refPageNo > -1) {
          pageNos.add(new Integer(refPageNo));
        }
        if (currentPageNo < 0) {
          currentPageNo = pageBuilder.getPageNo(node);
        }
      }

      // create nodes for all referenced pages
      final Graph2D view = getViewGraph();
      final ShapeNodeRealizer snr = new ShapeNodeRealizer();
      snr.getLabel().setFontSize(18);
      snr.setFillColor(PAGE_BACKGROUND);
      snr.setSize(42, 60);
      for (Iterator it = pageNos.iterator(); it.hasNext();) {
        final NodeRealizer page = snr.createCopy();
        final Integer next = (Integer) it.next();
        page.setLabelText(Integer.toString(next.intValue() + 1));
        view.createNode(page);
      }

      // create a node for the current page
      snr.setSize(56, 80);
      final NodeRealizer currentPage = snr.createCopy();
      if (currentPageNo > -1) {
        currentPage.setLabelText(Integer.toString(currentPageNo + 1));
      }

      // connect the current page to all referenced pages
      final Node current = view.createNode(currentPage);
      for (NodeCursor nc = view.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        if (node != current) {
          view.createEdge(current, node);
        }
      }
    }

    public Graph2D getModel() {
      return source.getGraph2D();
    }

    public Graph2D getViewGraph() {
      return target.getGraph2D();
    }

    public Node getModelNode( final Node view ) {
      return null;
    }

    public Node getViewNode( final Node model ) {
      return null;
    }

    public Edge getModelEdge( final Edge view ) {
      return null;
    }

    public Edge getViewEdge( final Edge model ) {
      return null;
    }
  }
}
