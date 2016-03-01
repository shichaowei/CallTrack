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
package demo.layout.withoutview;

import y.base.DataProvider;
import y.base.Node;
import y.base.NodeMap;
import y.layout.BufferedLayouter;
import y.layout.DefaultLayoutGraph;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.NodeLayout;
import y.layout.PortCalculator;
import y.layout.grouping.GroupingKeys;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.util.Maps;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * Demonstrates how to use {@link PortCalculator} and
 * {@link y.layout.IntersectionCalculator} to make sure that edge connection
 * points lie on the visual outline of nodes. Because {@link LayoutGraph} and
 * layout algorithms only support rectangular nodes, non-rectangular visual
 * outlines are defined by associating a symbolic shape type identifier to each
 * node (see {@link IntersectionCalculators#SHAPE_DPKEY}).
 */
public class IntersectionCalculatorDemo {
  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        (new IntersectionCalculatorDemo()).doit();
      }
    });
  }

  /**
   * Creates a sample graph, arranges the sample graph, and finally visualizes
   * the sample graph.
   */
  private void doit() {
    LayoutGraph graph = createSampleGraph();

    layoutGraph(graph);

    displayGraph(graph);
  }

  /**
   * Arranges the specified graph.
   * @param graph the graph to be arranged.
   */
  private void layoutGraph( final LayoutGraph graph ) {
    // IncrementalHierarchicLayouter is good choice here because by default it
    // connects edges to the border of the bounding box of the corresponding
    // nodes.
    final IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    layouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);

    // IMPORTANT:
    // register PortCalculator as a post-processing step
    // PortCalculator corrects IHL's edge connection points such that edges
    // no longer connect to the border of the bounding box but the visual
    // outline of the node
    layouter.prependStage(new PortCalculator());

    (new BufferedLayouter(layouter)).doLayout(graph);
  }

  /**
   * Displays the specified graph in a viewer component that supports elliptical
   * and diamond shaped nodes as well as rectangular nodes.
   * @param graph the graph to be displayed.
   */
  private void displayGraph( final LayoutGraph graph ) {
    (new MyLayoutPreviewPanel(graph)).createFrame("").setVisible(true);
  }

  /**
   * Creates a sample graph. The graph created by this method comes with
   * data providers that hold the shape type for each node as well as
   * intersection calculators for source and target nodes of the graph's edges.
   * @return a sample graph.
   */
  private LayoutGraph createSampleGraph() {
    final DefaultLayoutGraph graph = new DefaultLayoutGraph();

    // create some nodes
    //
    // VERY IMPORTANT:
    // assign sizes to nodes - most layout algorithms break on zero width
    // or zero height nodes!
    Node v0 = graph.createNode();
    graph.setSize(v0, 60, 30);
    Node v1 = graph.createNode();
    graph.setSize(v1, 30, 60);
    Node v2 = graph.createNode();
    graph.setSize(v2, 90, 90);
    Node v3 = graph.createNode();
    graph.setSize(v3, 30, 30);
    Node v4 = graph.createNode();
    graph.setSize(v4, 30, 30);
    Node v5 = graph.createNode();
    graph.setSize(v5, 60, 40);
    Node v6 = graph.createNode();
    graph.setSize(v6, 30, 30);

    // create some edges...
    graph.createEdge(v0, v1);
    graph.createEdge(v0, v2);
    graph.createEdge(v1, v3);
    graph.createEdge(v3, v4);
    graph.createEdge(v1, v5);
    graph.createEdge(v2, v5);
    graph.createEdge(v6, v2);


    // associate a specific shape to each node
    // if there is no explicit mapping for a node, it is assumed to be
    // rectangular
    final NodeMap shapes = Maps.createHashedNodeMap();
    shapes.setInt(v0, IntersectionCalculators.SHAPE_DIAMOND);
    shapes.setInt(v1, IntersectionCalculators.SHAPE_ELLIPSE);
    shapes.setInt(v2, IntersectionCalculators.SHAPE_DIAMOND);
    shapes.setInt(v5, IntersectionCalculators.SHAPE_ELLIPSE);
    graph.addDataProvider(IntersectionCalculators.SHAPE_DPKEY, shapes);

    // associate node shape type based intersection calculators to the
    // source nodes of the graph's edges
    IntersectionCalculators.addIntersectionCalculator(graph, shapes, true);
    // associate node shape type based intersection calculators to the
    // target nodes of the graph's edges
    IntersectionCalculators.addIntersectionCalculator(graph, shapes, false);
    return graph;
  }


  
  /**
   * Visualizes layout graph instances.
   * This extension adds support for elliptical and diamond shaped nodes.
   */
  public static class MyLayoutPreviewPanel extends LayoutPreviewPanel {
    final Ellipse2D.Double ellipse;
    final GeneralPath diamond;
  
    public MyLayoutPreviewPanel( final LayoutGraph graph ) {
      super(graph);
      ellipse = new Ellipse2D.Double();
      diamond = new GeneralPath();
    }

    protected void paint(
            final Graphics2D g,
            final LayoutGraph graph,
            final Node node
    ) {
      final NodeLayout nl = graph.getNodeLayout(node);
      final DataProvider dp = graph.getDataProvider(GroupingKeys.GROUP_DPKEY);
      if (dp != null && dp.getBool(node)) {
        rectangle.setFrame(nl.getX(), nl.getY(), nl.getWidth(), nl.getHeight());
        g.draw(rectangle);
      } else {
        switch (getShape(graph, node)) {
          case IntersectionCalculators.SHAPE_ELLIPSE:
            ellipse.setFrame(nl.getX(), nl.getY(), nl.getWidth(), nl.getHeight());
            g.fill(ellipse);
            break;
          case IntersectionCalculators.SHAPE_DIAMOND:
            setDiamondFrame(diamond, nl.getX(), nl.getY(), nl.getWidth(), nl.getHeight());
            g.fill(diamond);
            break;
          default:
            rectangle.setFrame(nl.getX(), nl.getY(), nl.getWidth(), nl.getHeight());
            g.fill(rectangle);
            break;
        }
      }
  
      paintNodeLabel(g, graph, node, Integer.toString(node.index()));
    }
  
    protected void paintNodeLabel(
            final Graphics2D g,
            final LayoutGraph graph,
            final Node node,
            final String text
    ) {
      final Color oldColor = g.getColor();
      g.setColor(Color.white);
  
      final TextLayout tl = new TextLayout(text, g.getFont(), g.getFontRenderContext());
      final Rectangle2D box = tl.getBounds();
      final NodeLayout nl = graph.getNodeLayout(node);
      final double tx = nl.getX() + (nl.getWidth() - box.getWidth())*0.5;
      final double ty = nl.getY() + box.getHeight() + (nl.getHeight() - box.getHeight())*0.5;
      tl.draw(g, (float) (tx), (float) (ty));
  
      g.setColor(oldColor);
    }

    private static void setDiamondFrame(
            final GeneralPath path,
            final double x, final double y,
            final double width, final double height
    ) {
      final double w2 = width * 0.5;
      final double h2 = height * 0.5;
      final double x2 = x + w2;
      final double y2 = y + h2;
  
      path.reset();
      path.moveTo((float) x2, (float) y);
      path.lineTo((float) (x + width), (float) y2);
      path.lineTo((float) x2, (float) (y + height));
      path.lineTo((float) x, (float) y2);
      path.closePath();
    }

    private static int getShape( final LayoutGraph graph, final Node node ) {
      final DataProvider dp = graph.getDataProvider(IntersectionCalculators.SHAPE_DPKEY);
      if (dp != null) {
        return dp.getInt(node);
      }
      return IntersectionCalculators.SHAPE_RECTANGLE;
    }
  }
}
