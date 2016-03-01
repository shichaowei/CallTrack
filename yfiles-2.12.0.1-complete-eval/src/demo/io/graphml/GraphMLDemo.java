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
package demo.io.graphml;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import demo.view.flowchart.painters.FlowchartRealizerFactory;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GraphMLIOHandler;
import y.io.graphml.output.GraphElementIdProvider;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.WriteEvent;
import y.io.graphml.output.WriteEventListenerAdapter;
import y.util.D;
import y.view.BevelNodePainter;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DEvent;
import y.view.Graph2DListener;
import y.view.Graph2DUndoManager;
import y.view.Selections;
import y.view.Selections.SelectionStateObserver;
import y.view.ViewMode;
import y.view.hierarchy.HierarchyManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This shows the basic usage of GraphMLIOHandler to load and save in the GraphML file format.
 *
 * In addition, it shows the graphml representation of the current graph in the lower text pane.
 * This representation is updated dynamically. Also, edits in the graphml text can be applied
 * to the current graph by pressing the "Apply GraphML" button.
 *
 * A small list of predefined GraphML files can be accessed from the combobox in the toolbar.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_extension">Section Reading and Writing Additional Data</a> in the yFiles for Java Developer's Guide
 */
public class GraphMLDemo extends DemoBase {
  private static final String BEVEL_NODE_CONFIGURATION = "BevelNodeConfig";

  protected GraphMLPane graphMLPane;
  private Graph2DUndoManager undoManager;

  /**
   * Creates a new instance of GraphMLDemo
   */
  public GraphMLDemo() {

    graphMLPane = new GraphMLPane();
    graphMLPane.setPreferredSize(new Dimension(600, 350));
    graphMLPane.setMinimumSize(new Dimension(0, 100));

    //plug the gui elements together and add them to the pane

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, view, graphMLPane);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    view.setPreferredSize(new Dimension(600, 350));
    view.setMinimumSize(new Dimension(0, 200));
    loadInitialGraph();
    view.fitContent();
    contentPane.add(splitPane, BorderLayout.CENTER);
  }

  protected void initialize() {
    final Graph2D graph = view.getGraph2D();

    //Create a hierarchy manager that allows us to read/write hierarchically structured graphs.
    new HierarchyManager(graph);
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    final GenericNodeRealizer.Factory f = GenericNodeRealizer.getFactory();
    if (!f.getAvailableConfigurations().contains(BEVEL_NODE_CONFIGURATION)) {
      BevelNodePainter bevelNodePainter = new BevelNodePainter();
      bevelNodePainter.setDrawShadow(true);
      Map impls = f.createDefaultConfigurationMap();
      impls.put(GenericNodeRealizer.Painter.class, bevelNodePainter);
      impls.put(GenericNodeRealizer.ContainsTest.class, bevelNodePainter);
      f.addConfiguration(BEVEL_NODE_CONFIGURATION, impls);
    }

    // registers all flowchart configurations
    FlowchartRealizerFactory.createData();
  }

  protected void loadInitialGraph() {
    loadGraph("resources/ygraph/visual_features.graphml");
  }

  /**
   * Create a customized undo manager which triggers updates of the GraphML section.
   */
  protected Graph2DUndoManager getUndoManager() {
    if (undoManager == null) {
      final Graph2D graph = view.getGraph2D();
      undoManager = new Graph2DUndoManager(graph) {
        /**
         * Stores all edge realizer so their changes can be undone/redone and and informs the GraphML section about a
         * potential change.
         * @param graph The graph where the edges belong to.
         * @param ec    The edges which realizers are stored.
         */
        public void backupRealizers(Graph2D graph, EdgeCursor ec) {
          super.backupRealizers(graph, ec);
          graphMLPane.firePotentialChange();
        }

        /**
         * Stores all node realizer so their changes can be undone/redone and and informs the GraphML section about a
         * potential change.
         * @param graph The graph where the nodes belong to.
         * @param nc    The nodes which realizers are stored.
         */
        public void backupRealizers(Graph2D graph, NodeCursor nc) {
          super.backupRealizers(graph, nc);
          graphMLPane.firePotentialChange();
        }
      };
      undoManager.setViewContainer(view);

      graph.setBackupRealizersHandler(undoManager);
    }

    return undoManager;
  }

  /**
   * Returns the list of sample files of this demo.
   */
  protected String[] getExampleResources() {
    return new String[]{
        "resources/ygraph/visual_features.graphml",
        "resources/ygraph/problemsolving.graphml",
        "resources/ygraph/simple.graphml",
        "resources/ygraph/grouping.graphml",
    };
  }

  protected void loadGraph(Class aClass, String resourceString) {
    try {
      graphMLPane.setUpdating(true);
      super.loadGraph(aClass, resourceString);
      graphMLPane.setUpdating(false);
      graphMLPane.showGraphMLText(view.getGraph2D());
    }
    finally {
      graphMLPane.setUpdating(false);
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new GraphMLDemo().start();
      }
    });
  }

  class GraphMLPane extends JPanel {

    /**
     * Re-entrance lock
     */
    protected boolean updating;
    private boolean editable;

    private final JButton applyButton;
    private Map elementIdMap;
    private final JTextArea graphMLTextPane;
    private int lastStartIndex;
    Timer timer;

    GraphMLPane() {
      editable = true;

      JPanel graphMLHeader = new JPanel();
      JLabel graphMLLabel = new JLabel("GraphML representation");
      graphMLLabel.setFont(graphMLLabel.getFont().deriveFont(Font.BOLD));
      graphMLLabel.setFont(graphMLLabel.getFont().deriveFont(12.0f));
      graphMLLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setLayout(new BorderLayout());

      graphMLTextPane = new JTextArea();

      JScrollPane scrollPane = new JScrollPane(graphMLTextPane);
      scrollPane.setPreferredSize(new Dimension(0, 350));

      graphMLHeader.setLayout(new BorderLayout());
      graphMLHeader.add(graphMLLabel, BorderLayout.WEST);
      applyButton = new JButton("Apply GraphML");

      applyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          applyGraphMLText(view.getGraph2D());
        }
      });

      graphMLHeader.add(applyButton, BorderLayout.EAST);

      add(graphMLHeader, BorderLayout.NORTH);
      add(scrollPane, BorderLayout.CENTER);
      setEditable(editable);
      registerView();
    }


    public boolean isEditable() {
      return editable;
    }

    public void setEditable(boolean editable) {
      this.editable = editable;
      graphMLTextPane.setEditable(editable);
      applyButton.setVisible(editable);
    }

    void registerView() {

      for(Iterator iter = view.getViewModes(); iter.hasNext();) {
        ViewMode mode = (ViewMode) iter.next();
        if(mode instanceof EditMode) {
          EditMode editMode = (EditMode) mode;
          PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
              if (evt.getPropertyName().equals(ViewMode.EDITING_PROPERTY) && evt.getNewValue().equals(Boolean.FALSE)) {
                firePotentialChange();                
              }
            }
          };
          editMode.addPropertyChangeListener(pcl);
          editMode.getHotSpotMode().addPropertyChangeListener(pcl);
          editMode.getMovePortMode().addPropertyChangeListener(pcl);
          editMode.getMoveLabelMode().addPropertyChangeListener(pcl);
          editMode.getMoveSelectionMode().addPropertyChangeListener(pcl);
          editMode.getMoveSelectionMode().addPropertyChangeListener(pcl);
          break;
        }
      }

      SelectionStateObserver sto = new SelectionStateObserver() {
        protected void updateSelectionState(Graph2D graph) {
          firePotentialChange();
        }
      };
      view.getGraph2D().addGraphListener(sto);
      view.getGraph2D().addGraph2DSelectionListener(sto);

      view.getGraph2D().addGraph2DListener(new Graph2DListener() {
        public void onGraph2DEvent(Graph2DEvent e) {
          firePotentialChange();
        }
      });      
    }

    
    public void firePotentialChange() {
      if(!isUpdating()) {
        if(timer == null) {
          timer = new Timer(100, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
              updateGraphMLText(view.getGraph2D());
            }
          });
          timer.setRepeats(false);
        }
        timer.restart();
      }
    }
    
    private void setUpdating(boolean updating) {
      this.updating = updating;
    }

    private boolean isUpdating() {
      return updating;
    }

    public void updateGraphMLText(Graph2D graph) {
      // show graphml text
      showGraphMLText(view.getGraph2D());

      // scroll to selected node/edge in graphml text
      if (!Selections.isNodeSelectionEmpty(graph)) {
        scrollGraphMLTextTo(view.getGraph2D().selectedNodes().node());
      } else if (!Selections.isEdgeSelectionEmpty(graph)) {
        scrollGraphMLTextTo(view.getGraph2D().selectedEdges().edge());
      }
    }

    private void scrollGraphMLTextTo(Node node) {
      if (elementIdMap != null && elementIdMap.get(node) instanceof String) {
        scrollGraphMLTextTo("node", elementIdMap.get(node).toString());
      }
    }

    private void scrollGraphMLTextTo(Edge edge) {
      if (elementIdMap != null && elementIdMap.get(edge) instanceof String) {
        scrollGraphMLTextTo("edge", elementIdMap.get(edge).toString());
      }
    }

    private void scrollGraphMLTextTo(String tag, String elementId) {
      String text = graphMLTextPane.getText();
      Pattern pattern = Pattern.compile("<" + tag + " .*id=\"" + elementId + "\"");
      int startIndex = 0;
      Matcher matcher = pattern.matcher(text);
      if (matcher.find()) {
        startIndex = matcher.start();
      }
      int endIndex = text.indexOf("</" + tag + ">", startIndex) + (tag.length() + 3);

      DefaultHighlighter highlighter = new DefaultHighlighter();
      DefaultHighlightPainter painter = new DefaultHighlightPainter(DemoDefaults.DEFAULT_CONTRAST_COLOR);
      graphMLTextPane.setHighlighter(highlighter);
      try {
        highlighter.addHighlight(startIndex, endIndex, painter);
        lastStartIndex = startIndex;
      } catch (BadLocationException e1) {
        lastStartIndex = 0;
        e1.printStackTrace();
      }

      graphMLTextPane.requestFocus();
      graphMLTextPane.setCaretPosition(startIndex);
      graphMLTextPane.moveCaretPosition(endIndex);
      view.getCanvasComponent().requestFocus();

      try {
        graphMLTextPane.scrollRectToVisible(graphMLTextPane.modelToView(startIndex));
      } catch (BadLocationException e) {
        e.printStackTrace();
      }

    }

    private String createGraphMLTextAndUpdateGraphElementIdMap(final Graph2D graph) {
      StringWriter buffer = new StringWriter();
      if (graph != null) {
        GraphMLIOHandler ioh = createGraphMLIOHandler();
        ioh.getGraphMLHandler().addWriteEventListener(
            new WriteEventListenerAdapter() {
              public void onDocumentWritten(WriteEvent event)
                  throws GraphMLWriteException {
                GraphElementIdProvider idProvider = (GraphElementIdProvider) event
                    .getContext().lookup(GraphElementIdProvider.class);
                elementIdMap = new HashMap();
                for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
                  Node n = nc.node();
                  elementIdMap.put(n, idProvider.getNodeId(n, event
                      .getContext()));
                }
                for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
                  Edge e = ec.edge();
                  elementIdMap.put(e, idProvider.getEdgeId(e, event
                      .getContext()));
                }
              }
            });
        try {
          ioh.write(graph, buffer);
        }catch(IOException ex) {
         ex.printStackTrace();
         return "";
        }
      }
      buffer.flush();
      return buffer.toString();
    }
    
    /**
     * Helper method that serializes the current graph content into a string which is shown in the graphml text pane.
     */
    protected void showGraphMLText(final Graph2D graph) {
      if (!isUpdating()) {
        setUpdating(true);
        try {
          graphMLTextPane.setText(createGraphMLTextAndUpdateGraphElementIdMap(graph));
          graphMLTextPane.setCaretPosition(Math.min(lastStartIndex, graphMLTextPane.getText().length()));
        }finally {
          setUpdating(false);
        }       
      }
    }

    /**
     * Helper method that applies the text content of the graphml text pane to the current graph.
     */
    protected void applyGraphMLText(final Graph2D graph) {
      if (graph != null) {

        Graph2D testGraph = (Graph2D) graph.createGraph();
        new HierarchyManager(testGraph);
        try {
          byte[] input = graphMLTextPane.getText().getBytes("UTF-8");
          
          {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
            createGraphMLIOHandler().read(testGraph, byteStream);
          }

          //seems to work well. try it with original graph then.          
          {
            setUpdating(true);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(input);
            GraphMLIOHandler ioh = createGraphMLIOHandler();                                              
            ioh.read(graph, byteStream);
            
            createGraphMLTextAndUpdateGraphElementIdMap(graph); 
            
            setUpdating(false);            
           
          }
        } catch (Exception e) {
          D.show(e);
        }
        finally {
          graph.updateViews(); 
        }
      }
    }

  }
}
