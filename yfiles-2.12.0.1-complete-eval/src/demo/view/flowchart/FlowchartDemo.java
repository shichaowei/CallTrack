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
package demo.view.flowchart;

import demo.view.DemoBase;
import org.w3c.dom.Element;
import y.base.Graph;
import y.io.GraphMLIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.io.graphml.graph2d.PostprocessorInputHandler;
import y.io.graphml.graph2d.PostprocessorOutputHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.GraphMLParser;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.QueryInputHandlersEvent;
import y.module.YModule;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Overview;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A viewer and editor for flowchart diagrams. It shows how to <ul> <li>integrate and configure an adjusted view, the
 * {@link FlowchartView}</li> <li>add a palette of flowchart symbols, the {@link FlowchartPalette}, to ease the creation
 * of diagrams</li> <li>add the specific properties panel</li> <li>implement a {@link
 * y.view.GenericNodeRealizer.Painter} tailored for the drawing of flowchart symbols</li> </ul>
 */
public class FlowchartDemo extends DemoBase {

  private static final Map EXAMPLES_FILE_NAMES;

  static {
    EXAMPLES_FILE_NAMES = new LinkedHashMap();
    EXAMPLES_FILE_NAMES.put("problemsolving.graphml", "Problem Solving");
    EXAMPLES_FILE_NAMES.put("studentRegistration.graphml", "Student Registration");
    EXAMPLES_FILE_NAMES.put("E-commerce.graphml", "E-commerce");
    EXAMPLES_FILE_NAMES.put("ComputingFactorial.graphml", "Computing the Factorial");
    EXAMPLES_FILE_NAMES.put("FindingLargestNumber.graphml", "Finding the Largest Number");
    EXAMPLES_FILE_NAMES.put("FindingLargestNumber2.graphml", "Finding the Largest Number 2");
    EXAMPLES_FILE_NAMES.put("flowchart.graphml", "Finding a Flow Chart Diagram");
  }

  FlowchartLayoutModule layoutModule;                                                                 
  FlowchartPalette palette;

  /**
   * Instantiates this demo. Builds the GUI.
   */
  public FlowchartDemo() {
    super();

    final JComponent workBench = createWorkBench();
    if (workBench != null) {
      contentPane.add(workBench, BorderLayout.CENTER);
    }

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        loadInitialGraph();
      }
    });
  }

  protected void loadInitialGraph() {
    loadGraph("resource/graphs/problemsolving.graphml");
  }

  /**
   * Overwritten to register no view mode at all. {@link demo.view.flowchart.FlowchartView}, the editor component that
   * is used to edit flowchart diagrams, registers all required view modes upon its instantiation.
   *
   * @see demo.view.flowchart.FlowchartView#registerViewModes()
   */
  protected void registerViewModes() {
  }

  /**
   * Creates a {@link FlowchartView}.
   *
   * @return a <code>FlowchartView</code>
   */
  protected Graph2DView createGraphView() {
    return new FlowchartView();
  }

  /**
   * Initializes the Flowchart palette.
   */
  protected void initialize() {
    palette = new FlowchartPalette(view);
    palette.setSnapMode(true);

    layoutModule = new FlowchartLayoutModule();                                                       

    // Register a DataProvider that returns the layout module. This dataprovider is used by           
    // PostprocessorOutputHandler to lookup the postprocessors it should serialize.                   
    view.getGraph2D().addDataProvider(PostprocessorOutputHandler.PROCESSORS_DPKEY,                    
        new DataProviderAdapter() {                                                                   
          public Object get(Object dataHolder) {                                                      
            return layoutModule;                                                                      
          }                                                                                           
        });                                                                                           
  }

  /**
   * Adds menu items for example graphs to the default menu bar.
   *
   * @return the menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenu examplesMenu = new JMenu("Examples");
    for (Iterator iterator = EXAMPLES_FILE_NAMES.entrySet().iterator(); iterator.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) iterator.next();
      examplesMenu.add(new JMenuItem(new AbstractAction((String) entry.getValue()) {
        public void actionPerformed(ActionEvent e) {
          loadGraph("resource/graphs/" + entry.getKey());
        }
      }));
    }

    JMenuBar menuBar = super.createMenuBar();
    menuBar.add(examplesMenu);
    return menuBar;
  }

  /**
   * Adds layout actions to the default toolbar.
   *
   * @return the toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();

    toolBar.addSeparator();                                                                           
    toolBar.add(createActionControl(createLayoutAction()));                                           
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);                                                    
    toolBar.add(createActionControl(createLayoutSettingsAction()));                                   

    return toolBar;
  }

  /**
   * Creates a GraphMLIOHandler that has additional input and output support for GraphML postprocessors.
   * <p/>
   * Note that input support for PostProcessors is registered by default. It is disabled by registering a customized
   * input handler which additionally sets this demo's layout module to the parsed module.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    final GraphMLIOHandler ioh = new PostprocessorGraphMLIOHandler(new PostprocessorInputHandler() {
      protected void startModule(YModule module, Graph2D graph, GraphMLParseContext context)
          throws GraphMLParseException {
//        super.startModule(graph, module, context);

        if (module instanceof FlowchartLayoutModule) {                                                
          layoutModule = (FlowchartLayoutModule) module;                                              
        }                                                                                             
      }
    });
    ioh.getGraphMLHandler().addOutputHandlerProvider(new PostprocessorOutputHandler());

    return ioh;
  }

  /**
   * Creates four panels which form the workbench of this demo: a Flowchart view, an overview, a palette and a property
   * window.
   *
   * @return a JComponent containing the view, overview, palette and property panel.
   */
  JComponent createWorkBench() {
    JPanel titledOverview = createTitledPanel(createOverview(), "Overview");
    JPanel titledPalette = createTitledPanel(this.palette, "Palette");
    JSplitPane overviewPaletteSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, titledOverview, titledPalette);
    return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewPaletteSplit, view);
  }

  /**
   * Callback method that creates and configures the layout action.
   *
   * @return the layout action.
   */
  protected Action createLayoutAction() {                                                             
    final Action action = new AbstractAction("Layout") {                                              
      public void actionPerformed(ActionEvent e) {                                                    
        layoutModule.start(view.getGraph2D());                                                        
      }                                                                                               
    };                                                                                                
    action.putValue(Action.SHORT_DESCRIPTION, "Run the flowchart layout");                            
    action.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);                                           

    return action;                                                                                    
  }                                                                                                   

  /**
   * Callback method that creates and configures the layout settings action.
   *
   * @return the layout settings action.
   */
  protected Action createLayoutSettingsAction() {                                                     
    final Action action = new AbstractAction("Settings...") {                                         
      public void actionPerformed(ActionEvent e) {                                                    
        OptionSupport.showDialog(layoutModule, view.getGraph2D(), false, view.getFrame());            
      }                                                                                               
    };                                                                                                
    action.putValue(Action.SHORT_DESCRIPTION, "Configure and run the flowchart layout");              
    action.putValue(Action.SMALL_ICON, getIconResource("resource/properties.png"));                   

    return action;                                                                                    
  }                                                                                                   

  /**
   * Creates a pre-configured {@link y.view.Overview}.
   *
   * @return the pre-configured overview.
   */
  protected Overview createOverview() {
    final Overview overview = new Overview(view);
    //blurs the part of the graph which can currently not be seen
    overview.putClientProperty("Overview.PaintStyle", "Funky");
    //allows zooming from within the overview
    overview.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
    //provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation with arrow keys)
    overview.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
    //determines how to differ between the part of the graph that can currently be seen, and the rest
    overview.putClientProperty("Overview.Inverse", Boolean.TRUE);

    overview.setPreferredSize(new Dimension((int) (0.2 * (double) contentPane.getWidth()), 200));
    return overview;
  }

  /**
   * Creates a panel which contains the specified component and a title on top of it.
   */
  protected JPanel createTitledPanel(JComponent content, String title) {
    JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    label.setBackground(new Color(231, 219, 182));
    label.setOpaque(true);
    label.setForeground(Color.DARK_GRAY);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setFont(label.getFont().deriveFont(13.0f));

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(label, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        final FlowchartDemo demo = new FlowchartDemo();
        demo.start("Flowchart Editor Demo");
      }
    });
  }

  /**
   * This GraphMLIOHandler registers the given {@link InputHandlerProvider} for post-processor data before the default
   * {@link PostprocessorInputHandler} is registered. This disables the default handler.
   */
  static class PostprocessorGraphMLIOHandler extends GraphMLIOHandler {
    private final PostprocessorInputHandler postprocessorInputHandler;

    PostprocessorGraphMLIOHandler(PostprocessorInputHandler postprocessorInputHandler) {
      this.postprocessorInputHandler = postprocessorInputHandler;
    }

    protected Graph2DGraphMLHandler createGraphMLHandler() {
      return new Graph2DGraphMLHandler() {

        protected void configureInputHandlers(Graph graph, GraphMLParser parser) {
          if (graph instanceof Graph2D) {
            parser.addInputHandlerProvider(new InputHandlerProvider() {

              public void onQueryInputHandler(QueryInputHandlersEvent event) throws GraphMLParseException {
                final Element keyDefinition = event.getKeyDefinition();
                if (GraphMLHandler.matchesScope(keyDefinition, KeyScope.GRAPH)
                    && postprocessorInputHandler.acceptKey(keyDefinition.getAttributes())) {
                  event.addInputHandler(postprocessorInputHandler);
                }
              }
            });

          }
          super.configureInputHandlers(graph, parser);
        }
      };

    }
  }

}
