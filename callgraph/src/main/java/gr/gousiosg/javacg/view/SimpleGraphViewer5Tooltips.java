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
package gr.gousiosg.javacg.view;

import gr.gousiosg.javacg.stat.ClassVisitor;
import gr.gousiosg.javacg.stat.JCallGraph;
import gr.gousiosg.javacg.view.modes.TooltipMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.io.IOHandler;
import y.io.TGFIOHandler;
import y.layout.organic.OrganicLayouter;
import y.layout.tree.TreeComponentLayouter;
import y.layout.tree.TreeLayouter;
import y.module.io.TGFInput;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DEvent;
import y.view.Graph2DListener;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.LineType;
import y.view.MoveSelectionMode;
import y.view.NavigationMode;
import y.view.NodeRealizer;

public class SimpleGraphViewer5Tooltips {
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;
  /** The yFiles graph type. */
  Graph2D graphClasses;

  JCallGraph cg;
  
  Map<String, Node> nodeCache = new HashMap<String, Node>();
  
  public SimpleGraphViewer5Tooltips(Dimension size, String title) {
    view = createGraph2DView();
    graphClasses = view.getGraph2D();
    frame = createApplicationFrame(size, title, view);
    configureDefaultRealizers(graphClasses);
    
    this.cg = new JCallGraph("C:\\Users\\walter\\workspace-estagio\\saff2015\\extractor\\target\\extractor-3.7.1.jar" ,"extractor");
  }

  public SimpleGraphViewer5Tooltips() {
    this(new Dimension(400, 300), "");
    frame.setTitle(getClass().getName());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView();
    view.setAntialiasedPainting(true);
    // Add a mouse wheel listener to zoom in and out of the view.
    new Graph2DViewMouseWheelZoomListener().addToCanvas(view);
    // Add a view mode for convenient mouse navigation.
    view.addViewMode(new EditMode());
   // view.addViewMode(new TooltipMode());
    return view;
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
	  //Trying set scroll pane
	  JPanel panel1 = new JPanel();
	  JScrollPane scrollPane = new JScrollPane(panel1);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      scrollPane.setBounds(50, 30, 300, 500);
	  
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
  private void populateGraphClasses(Graph2D graph) {
	  
	 
	  
//    // In the given graph, create two nodes...
//    Node hello = graph.createNode(100, 50, "Hello");
//    Node world = graph.createNode(100, 100, "World!");
//    Node node2 = graph.createNode(100, 100, "Node2");
//    Node node3 = graph.createNode(100, 100, "Node3");
//    Node node4 = graph.createNode(100, 100, "Node4");
//    
//    
//
//    // ...and an edge between.
//    graph.createEdge(hello, world);
//    graph.createEdge(hello, node2);
//    graph.createEdge(node3, node2);
//    graph.createEdge(node4, hello);
    
	  IOHandler io = new TGFIOHandler();
    
	  try {
		//io.read(graph, "C:\\Users\\walter\\workspace\\callgraph\\Exemplo_grafo-test2.tgf");
		  io.read(graph, "C:\\Users\\walter\\workspace\\callgraph\\Exemplo_grafo-methods.tgf");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
    
  }
  
  
  public void createGraphs(){
	  cg.prepare();
	  cg.processInput();
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

  public Graph2D getGraphClasses() {
    return graphClasses;
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

  
  public void setGraph(Graph2D g){
	  this.graphClasses = g;
  }
  
  public JCallGraph getGc(){
	  return this.cg;
  }
  
  
  public static void main(String[] args) {
	  System.out.println(8 | 7);
    SimpleGraphViewer5Tooltips sgv = 
      new SimpleGraphViewer5Tooltips(new Dimension(400, 300), "SimpleGraphViewer");
    sgv.createGraphs();
    sgv.populateGraphClasses(sgv.getGraphClasses());
//    sgv.populateGraph();
//    sgv.setGraph(sgv.getGc().getGraph());
    System.out.println("Amount of edges: " + sgv.getGraphClasses().edgeCount());
    sgv.getGraphClasses().addDataProvider(
      TooltipMode.NODE_TOOLTIP_DPKEY, new DataProviderAdapter() {
        public Object get(Object actuallyNode) {
          Node node = (Node)actuallyNode;
          Graph2D graph = (Graph2D)node.getGraph();
          return "<html>width x heigh of <b> " + graph.getLabelText(node) + "</b>: <b>" + 
                 graph.getWidth(node) + "</b> x <b>" + graph.getHeight(node) + 
                 "</b></html>" ;
        }
    });
    
    
    //sgv.getGraph().addDataProvider(arg0, arg1);
    
    sgv.getGraphClasses().addGraph2DSelectionListener(new Graph2DSelectionListener() {
		
		@Override
		public void onGraph2DSelectionEvent(Graph2DSelectionEvent event) {
			if(event.isNodeSelection()){
				System.out.println("is Node");
			}
			
		}
	});
    
    Graph2DView view = sgv.getView();
    view.setAutoscrolls(true);
    
    view.applyLayout(new OrganicLayouter());
    //view.addViewMode();
    sgv.show();
    
    
    
  }
  
  
  
}
