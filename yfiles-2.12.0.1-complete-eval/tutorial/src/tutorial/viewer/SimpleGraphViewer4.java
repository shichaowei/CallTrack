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
package tutorial.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import y.base.Node;
import y.view.Arrow;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeRealizer;

public class SimpleGraphViewer4 {
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;
  /** The yFiles graph type. */
  Graph2D graph;

  public SimpleGraphViewer4(Dimension size, String title) {
    view = createGraph2DView();
    graph = view.getGraph2D();
    frame = createApplicationFrame(size, title, view);
    configureDefaultRealizers(graph);
  }

  public SimpleGraphViewer4() {
    this(new Dimension(400, 300), "");
    frame.setTitle(getClass().getName());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView();
    view.setAntialiasedPainting(true);
    return view;
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(size);
    // Add the given view to the panel.
    panel.add(view, BorderLayout.CENTER);
    // Add a toolbar with some actions to the panel, too.
    panel.add(createToolBar(), BorderLayout.NORTH);
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getRootPane().setContentPane(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  /** Creates a simple graph structure. */
  private void populateGraph(Graph2D graph) {
    // In the given graph, create two nodes...
    Node hello = graph.createNode(100, 50, "Hello");
    Node world = graph.createNode(100, 100, "World!");
    // ...and an edge between.
    graph.createEdge(hello, world);
  }

  protected void configureDefaultRealizers(Graph2D graph) {
    // Add an arrowhead decoration to the target side of the edges.
    graph.getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);
    // Set the node size and some other graphical properties.
    NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
    defaultNodeRealizer.setSize(80, 30);
    defaultNodeRealizer.setFillColor(Color.ORANGE);
    defaultNodeRealizer.setLineType(LineType.DASHED_1);
  }

  /** Creates a toolbar for this demo. */
  protected JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.add(new FitContent(getView()));
    toolbar.add(new Zoom(getView(), 1.25));
    toolbar.add(new Zoom(getView(), 0.8));
    return toolbar;
  }

  public void show() {
    frame.setVisible(true);
  }

  public Graph2DView getView() {
    return view;
  }

  public Graph2D getGraph() {
    return graph;
  }

  /** Action that fits the content nicely inside the view. */
  protected static class FitContent extends AbstractAction {
    Graph2DView view;
    
    public FitContent(Graph2DView view) {
      super("Fit Content");
      this.view = view;
      this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }
    
    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }

  /** Action that applies a specified zoom level to the given view. */
  protected static class Zoom extends AbstractAction {
    Graph2DView view;
    double factor;

    public Zoom(Graph2DView view, double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      this.view = view;
      this.factor = factor;
      this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      // Adjusts the size of the view's world rectangle. The world rectangle 
      // defines the region of the canvas that is accessible by using the 
      // scrollbars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);
      view.updateView();
    }
  }

  public static void main(String[] args) {
    SimpleGraphViewer4 sgv = 
      new SimpleGraphViewer4(new Dimension(400, 300), SimpleGraphViewer4.class.getName());
    sgv.populateGraph(sgv.getGraph());
    sgv.show();
  }
}
