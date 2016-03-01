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

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YDimension;
import y.io.IOHandler;
import y.io.ZipGraphMLIOHandler;
import y.layout.LayoutTool;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.multipage.LayoutCallback;
import y.layout.multipage.MultiPageLayout;
import y.layout.multipage.MultiPageLayouter;
import y.layout.multipage.NodeInfo;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.CompactOrthogonalLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.option.Editor;
import y.option.OptionHandler;
import y.option.TableEditorFactory;
import y.util.D;
import y.util.DataProviderAdapter;
import y.util.GraphCopier;
import y.view.Drawable;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.LineType;
import y.view.NavigationMode;
import y.view.NodeRealizer;
import y.view.ViewMode;
import y.view.hierarchy.HierarchyManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Demonstrates how to use {@link MultiPageLayouter} to divide a large model
 * graph into several smaller page graphs.
 * <p>
 * Method {@link #doMultiPageLayout()} demonstrates how to prepare
 * the model graph for multi-page layout, how to configure and how to run
 * the multi-page layout algorithm.
 * </p><p>
 * Class {@link MultiPageGraph2DBuilder} demonstrates how to create
 * displayable {@link Graph2D} instances from a {@link MultiPageLayout}
 * that is the result of a multi-page layout calculation.
 * </p><p>
 * Moreover, the demo shows different methods to navigate through the page
 * graphs:
 * </p>
 * <ul>
 * <li>
 * Clicking on a connector, proxy, or proxy reference node will switch to
 * the page graph holding the referenced node.
 * </li>
 * <li>
 * Clicking on a page in the demo's overview component will switch to the
 * corresponding page graph.
 * </li>
 * <li>
 * Using the toolbar arrow controls it is possible to navigate sequentially
 * through the page graphs.
 * </li>
 * </ul>
 * @see NodeInfo#TYPE_CONNECTOR
 * @see NodeInfo#TYPE_PROXY
 * @see NodeInfo#TYPE_PROXY_REFERENCE
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/multipage_layout.html#multipage_layout">Section Multi-page Layout</a> in the yFiles for Java Developer's Guide
 */
public class MultiPageLayoutDemo extends DemoBase {
  private static final Color PAGE_BACKGROUND = new Color(230, 230, 230);

  private final Graph2D baseModel;
  private final MultiPageGraph2DBuilder pageBuilder;
  private final MultiPageLayoutOptionHandler oh;
  private JTextField pageNumberTextField;

  private List pageList;
  private Map id2LocationInfo;

  private int previousPageIndex;
  private int currentPageIndex;

  public MultiPageLayoutDemo() {
    this(null);
  }

  public MultiPageLayoutDemo( final String helpFilePath ) {
    oh = new MultiPageLayoutOptionHandler();
    oh.addDrawPageChangeListener(new PropertyChangeListener() {
      public void propertyChange( final PropertyChangeEvent e ) {
        view.updateView();
      }
    });
    baseModel = view.getGraph2D();
    new HierarchyManager(baseModel);
    pageBuilder = new MultiPageGraph2DBuilder(null, null);
    id2LocationInfo = new HashMap();

    //configure the main view that displays calculated page graphs
    view.setGraph2D(new Graph2D());
    view.setContentPolicy(Graph2DView.CONTENT_POLICY_BACKGROUND_DRAWABLES);
    view.addBackgroundDrawable(new PageBorderDrawer());
    setPageGraph(0);
    view.setFitContentOnResize(true);
    Graph2DViewMouseWheelZoomListener mouseWheelZoomListener = new Graph2DViewMouseWheelZoomListener();
    mouseWheelZoomListener.setCenterZooming(false);
    mouseWheelZoomListener.addToCanvas(view);

    final MultiPageOverview overview = new MultiPageOverview(view, pageBuilder);
    overview.addViewMode(new OverviewViewMode());

    view.addViewMode(new PageViewMode() {
      void setActive( final Graph2D graph, final Node node, final boolean active ) {
        super.setActive(graph, node, active);
        highlight(node, active);
      }

      private void highlight( final Node node, final boolean active ) {
        final int refPageNo = pageBuilder.getReferencedPageNo(node);
        if (refPageNo > -1) {
          final Graph2D g = overview.getGraph2D();
          final Node page = find(g, Integer.toString(refPageNo + 1));
          if (page != null) {
            g.getRealizer(page).setFillColor(
                    active
                    ? DemoDefaults.DEFAULT_CONTRAST_COLOR
                    : PAGE_BACKGROUND);
            overview.updateView();
          }
        }
      }

      private Node find( final Graph2D g, final String label ) {
        for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()) {
          final NodeRealizer nr = g.getRealizer(nc.node());
          if (nr.labelCount() > 0 && label.equals(nr.getLabelText())) {
            return nc.node();
          }
        }
        return null;
      }
    });
    view.addViewMode(new NavigationMode());

    
    //create help pane
    JComponent helpPane = null;
    if (helpFilePath != null) {
      final URL url = getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        helpPane = createHelpPane(url);
      }
    }

    //create demo gui
    final JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setMinimumSize(new Dimension(180, 240));
    rightPanel.add(createOptionTable(oh), BorderLayout.NORTH);   

    if (helpPane != null) {
      rightPanel.add(helpPane, BorderLayout.CENTER);
    }

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view, rightPanel);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setResizeWeight(1);
    splitPane.setContinuousLayout(false);
    contentPane.add(splitPane, BorderLayout.CENTER);

    final SearchableTreeViewPanel baseModelView = new SearchableTreeViewPanel(baseModel);
    final JTree jt = baseModelView.getTree();
    //add a navigational action to the tree
    jt.addMouseListener(new MyDoubleClickListener());
    jt.setCellRenderer(new MyTreeCellRenderer());

    final JPanel navPane = new JPanel(new BorderLayout());
    navPane.add(baseModelView, BorderLayout.CENTER);
    navPane.add(overview, BorderLayout.NORTH);

    final JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navPane, splitPane);
    splitPane2.setBorder(BorderFactory.createEmptyBorder());
    contentPane.add(splitPane2, BorderLayout.CENTER);
  }

  /**
   * Overwritten to prevent keyboard shortcuts from being registered multiple
   * times. The demo constructor registers the demo's shortcuts.
   */
  protected void registerViewActions() {
  }

  /**
   * Overwritten to prevent the default view listeners from being registered.
   * The demo constructor registers the demo's view listeners.
   */
  protected void registerViewListeners() {
  }

  /**
   * Overwritten to prevent the default view modes from being registered.
   * The demo constructor registers the demo's view modes.
   */
  protected void registerViewModes() {
  }

  /**
   * Overwritten to prevent deletion of graph elements.
   * @return <code>false</code>.
   */
  protected boolean isDeletionEnabled() {
    return false;
  }

  private JTextField getPageNumberTextField() {
    if (pageNumberTextField == null) {
      final JButton dummy = new JButton(new FirstPageAction());
      final Dimension size = dummy.getPreferredSize();
      pageNumberTextField = new JTextField();
      pageNumberTextField.setHorizontalAlignment(JTextField.CENTER);
      pageNumberTextField.setEditable(false);
      pageNumberTextField.setColumns(11);
      pageNumberTextField.setMaximumSize(new Dimension(80, size.height));
      pageNumberTextField.setPreferredSize(new Dimension(80, size.height));
    }
    return pageNumberTextField;
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(new FirstPageAction());
    toolBar.add(new PreviousPageAction());
    toolBar.add(getPageNumberTextField());
    toolBar.add(new NextPageAction());
    toolBar.add(new LastPageAction());
    toolBar.addSeparator(new Dimension(10, 0));
    toolBar.add(new GoBackAction());
    toolBar.addSeparator();
    toolBar.add(createActionControl(new AbstractAction("Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(final ActionEvent e) {
        doLayoutInBackground();
      }
    }));
    return toolBar;
  }

  /**
   * Overwritten to disable undo/redo because this is not an editable demo.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Overwritten to disable clipboard because this is not an editable demo.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  protected JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();

    JMenu file = new JMenu("File");
    file.add(createLoadAction());
    file.add(createSaveAction());
    file.add(new SaveAction("Save Page Graph", view));
    file.addSeparator();
    file.add(new PrintAction());
    file.addSeparator();
    file.add(new ExitAction());
    mb.add(file);

    JMenu sampleGraphs = new JMenu("Sample Graphs");
    for (Iterator it = getLoadSampleActions(); it.hasNext();) {
      sampleGraphs.add((Action) it.next());
    }
    mb.add(sampleGraphs);

    JMenu sampleSettings = new JMenu("Sample Settings");
    sampleSettings.add(new SetOptionsAction(MultiPageLayoutOptionHandler.OPTIONS_NETWORK_SMALL_DISPLAY));
    sampleSettings.add(new SetOptionsAction(MultiPageLayoutOptionHandler.OPTIONS_NETWORK_LARGE_DISPLAY));
    sampleSettings.add(new SetOptionsAction(MultiPageLayoutOptionHandler.OPTIONS_CLASS_DIAGRAM_SMALL_DISPLAY));
    sampleSettings.add(new SetOptionsAction(MultiPageLayoutOptionHandler.OPTIONS_CLASS_DIAGRAM_LARGE_DISPLAY));
    mb.add(sampleSettings);

    return mb;
  }

  private Iterator getLoadSampleActions() {
    final String key = "MultiPageLayoutDemo.samples";
    final Object samples = contentPane.getClientProperty(key);
    if (samples instanceof List) {
      return ((List) samples).iterator();
    } else {
      final ArrayList list = new ArrayList(3);
      list.add(createLoadSampleActions(
              "Pop Artists",
              "resource/pop-artists.graphmlz"));
      list.add(createLoadSampleActions(
              "yFiles Classes",
              "resource/yfiles-classes.graphmlz"));
      list.add(createLoadSampleActions(
              "yFiles Classes and Packages",
              "resource/yfiles-classes-and-packages-nested.graphmlz"));
      contentPane.putClientProperty(key, list);
      return list.iterator();
    }
  }

  private Action createLoadSampleActions(final String name, final String resource) {
    if (isResourceValid(resource)) {
      return new LoadSampleGraphAction(name, resource);
    } else {
      throw new RuntimeException("Missing resource: " + resource);
    }
  }

  /**
   * Determines whether or not the specified resource can be resolved.
   * @param resource the name of the resource to check.
   * @return <code>true</code> if the resource can be resolved;
   * <code>false</code> otherwise.
   */
  private boolean isResourceValid(final String resource) {
    return getResource(resource) != null;
  }

  /**
   * Creates a table control for the specified options.
   * @param oh the options to display.
   * @return a table control for the specified options.
   */
  private JComponent createOptionTable(OptionHandler oh) {
    oh.setAttribute(
            TableEditorFactory.ATTRIBUTE_INFO_POSITION,
            TableEditorFactory.InfoPosition.NONE);
    oh.setAttribute(
            TableEditorFactory.ATTRIBUTE_USE_ITEM_NAME_AS_TOOLTIP_FALLBACK,
            Boolean.TRUE);

    TableEditorFactory tef = new TableEditorFactory();
    Editor editor = tef.createEditor(oh);

    JComponent optionComponent = editor.getComponent();
    optionComponent.setPreferredSize(new Dimension(400, 240));
    optionComponent.setMaximumSize(new Dimension(400, 240));
    return optionComponent;
  }

  /**
   * Creates the application help pane.
   *
   * @param helpURL the URL of the HTML help page to display.
   */
  protected JComponent createHelpPane(final URL helpURL) {
    try {
      JEditorPane editorPane = new JEditorPane(helpURL);
      editorPane.setEditable(false);
      editorPane.setPreferredSize(new Dimension(250, 250));
      return new JScrollPane(editorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Creates an progress dialog for loading graphs in
   * background threads.
   * @param name the display name of the graph resource that is loaded.
   * @return a dialog displaying a progress bar.
   */
  private JDialog createProgressDialog( final String name ) {
    final JDialog jd = new JDialog(getFrame(), name, true);

    final JProgressBar jpb = new JProgressBar(0, 1);
    jpb.setString(name);
    jpb.setIndeterminate(true);

    final JLabel lbl = new JLabel(name);
    final JPanel progressPane = new JPanel(new BorderLayout());
    progressPane.add(lbl, BorderLayout.NORTH);
    progressPane.add(jpb, BorderLayout.CENTER);
    final JPanel contentPane = new JPanel(new FlowLayout());
    contentPane.add(progressPane);

    jd.setContentPane(contentPane);
    jd.pack();
    jd.setLocationRelativeTo(null);
    jd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    return jd;
  }

  /**
   * Retrieves the frame that displays the dmo GUI.
   * @return the frame that displays the dmo GUI or <code>null</code> if
   * no frame can be found.
   */
  private JFrame getFrame() {
    final Window ancestor = SwingUtilities.getWindowAncestor(contentPane);
    if (ancestor instanceof JFrame) {
      return (JFrame) ancestor;
    } else {
      return null;
    }
  }

  /**
   * Overwritten to load an initial multi-page graph <em>after</em> the
   * graphical user interface has been completely created.
   * @param rootPane the container to hold the demo's graphical user interface.
   */
  public void addContentTo( final JRootPane rootPane ) {
    super.addContentTo(rootPane);
    loadInitialGraph();
  }

  /**
   * Displays the page graph at the specified position in the list of pages.
   * @param page the index of the page graph to display.
   */
  private void setPageGraph( final int page ) {
    if (pageList != null && !pageList.isEmpty()) {
      previousPageIndex = currentPageIndex;
      currentPageIndex = Math.min(Math.max(page, 0), pageList.size() - 1);
      final Graph2D currentPageGraph = (Graph2D) pageList.get(currentPageIndex);
      getPageNumberTextField().setText((currentPageIndex + 1) + " / " + pageList.size());
      view.setGraph2D(currentPageGraph);
    } else {
      view.setGraph2D(new Graph2D());
    }
    view.fitContent();
    view.updateView();
  }

  /**
   * Jumps to the page graph that hold the node represented by the specified
   * target ID.
   * @param target the ID of a node in the destination page graph.
   * @param focus the label of a node in the destination page graph that should
   * be focused after the jump.
   */
  private void jump( final Object target, final String focus ) {
    final LocationInfo locationInfo = getLocationInfo(target);
    if (locationInfo != null) {
      final double zoomLevel = view.getZoom();
      final Graph2D oldGraph = view.getGraph2D();
      oldGraph.setSelected(oldGraph.nodes(), false);
      setPageGraph(locationInfo.pageNo);
      view.getGraph2D().setSelected(locationInfo.node, true);

      //center matching node
      final Graph2D newGraph = view.getGraph2D();
      Node matchingNode = locationInfo.node; //default node
      if (focus != null) {
        for (NodeCursor nc = locationInfo.node.neighbors(); nc.ok(); nc.next()) {
          final Node neighbor = nc.node();
          if (focus.equals(newGraph.getLabelText(neighbor))) {
            matchingNode = neighbor;
            break;
          }
        }
      }

      view.setCenter(newGraph.getCenterX(matchingNode), newGraph.getCenterY(matchingNode));
      view.setZoom(zoomLevel);
    }
  }

  private LocationInfo getLocationInfo( final Object id ) {
    return (LocationInfo) id2LocationInfo.get(id);
  }

  /**
   * Loads an initial multi-page graph.
   */
  protected void loadInitialGraph() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        final Iterator it = getLoadSampleActions();
        if (it.hasNext()) {
          ((Action) it.next()).actionPerformed(null);
        }
      }
    });
  }

  protected void loadGraph(String resourceString) {
    loadGraph(baseModel, resourceString);
  }

  /**
   * Loads the specified graph structure resource in GraphML of compressed
   * GraphML formant into the given graph instance.
   * @param graph the graph instance to store the loaded data.
   * @param resourceName the name of the graph resource to load.
   */
  private void loadGraph(final Graph2D graph, final String resourceName) {
    URL resource = null;
    final File file = new File(resourceName);
    if (file.exists()) {
      try {
        resource = file.toURI().toURL();
      } catch (MalformedURLException e) {
        D.showError(e.getMessage());
        return;
      }
    } else {
      resource = getResource(resourceName);
      if (resource == null) {
        return;
      }
    }

    try {
      final IOHandler ioh = resource.getFile().endsWith(".graphmlz") ? new ZipGraphMLIOHandler() : createGraphMLIOHandler();
      
      graph.clear();
      ioh.read(graph, resource);
    } catch (IOException ioe) {
      D.showError("Unexpected error while loading resource \"" + resource + "\" due to " + ioe.getMessage());
    }
    graph.setURL(resource);
  }

  /**
   * Invokes the page layout and updates the view.
   */
  private void doLayoutInBackground() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        final JDialog pd = createProgressDialog("Do Layout");

        (new Thread(new Runnable() {
          public void run() {
            doLayout();

            EventQueue.invokeLater(new Runnable() {
              public void run() {
                pd.setVisible(false);
                pd.dispose();

                //reset page indices and set view to first page
                previousPageIndex = 0;
                setPageGraph(0);
              }
            });
          }
        })).start();

        pd.setVisible(true);
      }
    });
  }

  private void doLayout() {
    id2LocationInfo.clear();
    if (oh.isUseSinglePageLayout()) {
      (new Graph2DLayoutExecutor()).doLayout(baseModel, createCoreLayouter());
      pageList = new ArrayList();
      pageList.add((new GraphCopier(baseModel.getGraphCopyFactory())).copy(baseModel));
    } else {
      doMultiPageLayout();
    }
  }

  /**
   * Configures and applies the multi-page layouter.
   */
  private void doMultiPageLayout() {
    //map elements to ids
    //multi-page layout requires unique, user-specified IDs for nodes, edges,
    //node labels, and edge labels
    final DataProvider idProvider = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return dataHolder;
      }
    };
    baseModel.addDataProvider(MultiPageLayouter.NODE_ID_DPKEY, idProvider);
    baseModel.addDataProvider(MultiPageLayouter.EDGE_ID_DPKEY, idProvider);
    baseModel.addDataProvider(MultiPageLayouter.NODE_LABEL_ID_DPKEY, idProvider);
    baseModel.addDataProvider(MultiPageLayouter.EDGE_LABEL_ID_DPKEY, idProvider);

    final MultiPageLayouter mpl = new MultiPageLayouter(createCoreLayouter());

    //configure the layout algorithm
    mpl.setLabelLayouterEnabled(oh.isLayout(MultiPageLayoutOptionHandler.LAYOUT_ORGANIC));
    mpl.setPreferredMaximalDuration(oh.getMaximalDuration() * 1000);
    mpl.setGroupMode(oh.getGroupingMode());
    mpl.setEdgeBundleModeMask(oh.getSeparationMask());
    final boolean addEdgeTypeDp =
            (mpl.getEdgeBundleModeMask() &
             MultiPageLayouter.EDGE_BUNDLE_DISTINGUISH_TYPES) != 0;
    if (addEdgeTypeDp) {
      baseModel.addDataProvider(
              MultiPageLayouter.EDGE_TYPE_DPKEY,
              new DataProviderAdapter() {
        public Object get(Object dataHolder) {
          return new EdgeType(baseModel.getRealizer((Edge) dataHolder));
        }
      });
    }

    mpl.setMaxPageSize(new YDimension(oh.getMaximumWidth(), oh.getMaximumHeight()));

    final SimpleLayoutCallback callback = new SimpleLayoutCallback();
    mpl.setLayoutCallback(callback);

    try {
      //calculate a new multi-page layout
      (new Graph2DLayoutExecutor()).doLayout(baseModel, mpl);

      //transform the layout result into a list of Graph2D instances
      pageList = createPageViews(callback.pop());
    } finally {
      //clean-up: remove previously registered data providers
      if (addEdgeTypeDp) {
        baseModel.removeDataProvider(MultiPageLayouter.EDGE_TYPE_DPKEY);
      }

      baseModel.removeDataProvider(MultiPageLayouter.EDGE_LABEL_ID_DPKEY);
      baseModel.removeDataProvider(MultiPageLayouter.NODE_LABEL_ID_DPKEY);
      baseModel.removeDataProvider(MultiPageLayouter.EDGE_ID_DPKEY);
      baseModel.removeDataProvider(MultiPageLayouter.NODE_ID_DPKEY);
    }
  }

  /**
   * Creates a configured layout algorithm to be used as core layout strategy
   * in a multi-page layout calculation.
   * @return a ready-to-use layout algorithm.
   */
  private Layouter createCoreLayouter() {
    if (oh.isLayout(MultiPageLayoutOptionHandler.LAYOUT_HIERARCHIC)) {
      final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
      ihl.setConsiderNodeLabelsEnabled(true);
      ihl.setIntegratedEdgeLabelingEnabled(true);
      ihl.setOrthogonallyRouted(true);
      ihl.setConsiderNodeLabelsEnabled(true);
      return ihl;
    } else if (oh.isLayout(MultiPageLayoutOptionHandler.LAYOUT_ORGANIC)) {
      final SmartOrganicLayouter sol = new SmartOrganicLayouter();
      sol.setMinimalNodeDistance(10);
      sol.setDeterministic(true);
      sol.setMultiThreadingAllowed(true);
      return sol;
    } else if (oh.isLayout(MultiPageLayoutOptionHandler.LAYOUT_COMPACT_ORTHOGONAL)) {
      return new CompactOrthogonalLayouter();
    } else {
      return new OrthogonalLayouter();
    }
  }

  /**
   * Creates a page view for each specified page layout.
   * @param layout the page layouts.
   * @return the page views, a list of {@link Graph2D} instances.
   */
  private List createPageViews( final MultiPageLayout layout ) {
    final ArrayList newPageList = new ArrayList();
    pageBuilder.reset(baseModel, layout);
    for (int i = 0, pc = layout.pageCount(); i < pc; ++i) {
      final Graph2D subgraph = pageBuilder.createPageView(new Graph2D(), i);
      for (NodeCursor nc = subgraph.nodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        id2LocationInfo.put(pageBuilder.getNodeId(n), new LocationInfo(i, n));
      }
      newPageList.add(subgraph);
    }
    return newPageList;
  }

  protected Action createLoadAction() {
    return new LoadAction();
  }

  protected Action createSaveAction() {
    return new SaveAction("Save Model Graph", baseModel);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new MultiPageLayoutDemo("resource/multipagelayouthelp.html")).start("Multi-Page Layout Demo");
      }
    });
  }



  /**
   * Class that represents the type of an edge.
   * The edge type of two edges is equal if the corresponding realizers have
   * the same line type, line color and source/target arrow type.
   */
  static final class EdgeType {
    byte sourceArrowType;
    byte targetArrowType;
    Color lineColor;
    LineType lineType;

    EdgeType(final EdgeRealizer realizer) {
      sourceArrowType = realizer.getSourceArrow().getType();
      targetArrowType = realizer.getTargetArrow().getType();
      lineColor = realizer.getLineColor();
      lineType = realizer.getLineType();
    }

    public boolean equals( Object o ) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      EdgeType edgeType = (EdgeType) o;

      if (sourceArrowType != edgeType.sourceArrowType) {
        return false;
      }
      if (targetArrowType != edgeType.targetArrowType) {
        return false;
      }
      if (lineColor != null ? !lineColor.equals(edgeType.lineColor) : edgeType.lineColor != null) {
        return false;
      }
      if (lineType != null ? !lineType.equals(edgeType.lineType) : edgeType.lineType != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = (int) sourceArrowType;
      result = 31 * result + (int) targetArrowType;
      result = 31 * result + (lineColor != null ? lineColor.hashCode() : 0);
      result = 31 * result + (lineType != null ? lineType.hashCode() : 0);
      return result;
    }
  }



  /**
   * Customized click listener for the tree view.
   * A click on an element of the tree view causes a jump to the page that contains the clicked element.
   */
  class MyDoubleClickListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      final JTree tree = (JTree) e.getSource();
      if (e.getClickCount() == 2) {
        final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
          final Object last = path.getLastPathComponent();
          if (last instanceof Node) {
            jump(last, baseModel.getLabelText((Node) last));
          }
        }
      }
    }
  }

  class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
    ) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if (value instanceof Graph) {
        setIcon(null);
        setText("Nodes: " + ((Graph) value).nodeCount() + "\t Edges: " + ((Graph) value).edgeCount());
      }
      return this;
    }
  }


  /**
   * Abstract base class for loading graphs in a background thread.
   */
  abstract class AbstractLoadAction extends AbstractAction {
    AbstractLoadAction( final String name ) {
      super(name);
    }

    /**
     * Loads a model graph in a background thread.
     * @param resource the graph resource to load as model graph.
     * @param name the display name for the graph resource.
     */
    void load( final String resource, final String name ) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          final JDialog pd = createProgressDialog("Loading " + name);

          view.setGraph2D(new Graph2D());
          view.fitContent();
          view.updateView();

          (new Thread(new Runnable() {
            public void run() {
              loadGraph(baseModel, resource);
              EventQueue.invokeLater(new Runnable() {
                public void run() {
                  pd.setVisible(false);
                  pd.dispose();

                  doLayoutInBackground();
                }
              });
            }
          })).start();

          pd.setVisible(true);
        }
      });
    }
  }

  /**
   * Loads a sample graph using the given resource.
   */
  protected class LoadSampleGraphAction extends AbstractLoadAction {
    private final String name;
    private final String resource;

    LoadSampleGraphAction(final String name, final String resource) {
      super(name);
      this.name = name;
      this.resource = resource;
    }

    public void actionPerformed(ActionEvent e) {
      load(resource, name);
    }
  }

  /** Action that loads the current graph from a file in GraphML format. */
  protected class LoadAction extends AbstractLoadAction {
    JFileChooser chooser;

    public LoadAction() {
      super("Load Model Graph");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphmlz");
          }

          public String getDescription() {
            return "Zipped GraphML Format (.graphmlz)";
          }
        });
      }
      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          final URL resource = chooser.getSelectedFile().toURI().toURL();
          final String ln = resource.getFile();
          load(ln, (new File(ln)).getName());
        } catch (MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    }
  }

  /** Action that saves the current graph to a file in GraphML format. */
  protected class SaveAction extends AbstractAction {
    private JFileChooser chooser;
    private Graph2D graph;
    private Graph2DView graphView;

    /**
     * Initializes a new <code>SaveAction</code> instance.
     * @param name the display name of the action.
     * @param graph the graph to save.
     */
    public SaveAction( final String name, final Graph2D graph ) {
      super(name);
      this.graph = graph;
      this.graphView = null;
    }

    /**
     * Initializes a new <code>SaveAction</code> instance.
     * @param name the display name of the action.
     * @param graphView the view whose graph is saved.
     */
    public SaveAction( final String name, final Graph2DView graphView ) {
      super(name);
      this.graph = null;
      this.graphView = graphView;
    }

    private Graph2D getGraph() {
      if(graph != null) {
        return graph;
      } else if(graphView != null) {
        return graphView.getGraph2D();
      } else {
        return null;
      }
    }

    private void setFileFilter( final JFileChooser chooser, final File file ) {
      FileFilter[] filters = chooser.getChoosableFileFilters();
      for (int i = 0; i < filters.length; i++) {
        if (filters[i].accept(file)) {
          chooser.setFileFilter(filters[i]);
          return;
        }
      }
    }

    public void actionPerformed( final ActionEvent e ) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphmlz");
          }

          public String getDescription() {
            return "Zipped GraphML Format (.graphmlz)";
          }
        });
      }

      final URL url = view.getGraph2D().getURL();
      if (url != null && "file".equals(url.getProtocol())) {
        try {
          final File file = new File(new URI(url.toString()));
          chooser.setSelectedFile(file);
          setFileFilter(chooser, file);
        } catch (URISyntaxException use) {
          // ignore
        }
      }

      if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        IOHandler ioh;
        String name = chooser.getSelectedFile().toString();
        final FileFilter filter = chooser.getFileFilter();
        if (filter.accept(new File("file.graphml"))) {
          if (!name.endsWith(".graphml")) {
            name += ".graphml";
          }
          ioh = createGraphMLIOHandler();
        } else {
          if (!name.endsWith(".graphmlz")) {
            name += ".graphmlz";
          }
          ioh = new ZipGraphMLIOHandler();
        }

        try {
          ioh.write(getGraph(), name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
  }

  /**
   * Action that switches the view to the first page
   */
  private final class FirstPageAction extends AbstractAction {
    public FirstPageAction() {
      super("<<");
      putValue(SHORT_DESCRIPTION, "Go to first page");
    }

    public void actionPerformed(ActionEvent e) {
      setPageGraph(0);
    }
  }

  /**
   * Action that switches the view to the last page
   */
  private final class LastPageAction extends AbstractAction {
    public LastPageAction() {
      super(">>");
      putValue(SHORT_DESCRIPTION, "Go to last page");
    }

    public void actionPerformed(ActionEvent e) {
      setPageGraph(pageList == null ? 0 : pageList.size()-1);
    }
  }

  /**
   * Action that switches the view to the next page
   */
  private final class NextPageAction extends AbstractAction {
    public NextPageAction() {
      super(">");
      putValue(SHORT_DESCRIPTION, "Go to next page");
    }

    public void actionPerformed(ActionEvent e) {
      setPageGraph(currentPageIndex + 1);
    }
  }

  /**
   * Action that switches the view to the previous page
   */
  private final class PreviousPageAction extends AbstractAction {
    public PreviousPageAction() {
      super("<");
      putValue(SHORT_DESCRIPTION, "Go to previous page");
    }

    public void actionPerformed(ActionEvent e) {
      setPageGraph(currentPageIndex - 1);
    }
  }

  /**
   * Action that switches the view to the last visited page
   */
  private final class GoBackAction extends AbstractAction {
    public GoBackAction() {
      super("Go Back");
      putValue(SHORT_DESCRIPTION, "Go to last visited page");
    }

    public void actionPerformed(ActionEvent e) {
      setPageGraph(previousPageIndex);
    }
  }

  /**
   * Action that applies a pre-defined set of options for multi-page layout.
   */
  private final class SetOptionsAction extends AbstractAction {
    private final MultiPageLayoutOptionHandler.OptionSet set;

    SetOptionsAction( final MultiPageLayoutOptionHandler.OptionSet set ) {
      super(set.getName());
      this.set = set;
    }

    public void actionPerformed( final ActionEvent e ) {
      set.apply(oh);
      doLayoutInBackground();
    }
  }



  /**
   * Background-Drawable that draws the page (a filled rectangle with size equals to the maximum page size).
   */
  class PageBorderDrawer implements Drawable {
    public Rectangle getBounds() {
      final Rectangle2D bnds = getPageBounds();
      if (oh.isDrawingPage()) {
        final int margin = 5;
        bnds.setFrame(
                bnds.getX() - margin,
                bnds.getY() - margin,
                bnds.getWidth() + 2*margin,
                bnds.getHeight() + 2*margin);
        return bnds.getBounds();
      } else {
        return new Rectangle(
                (int) Math.floor(bnds.getCenterX()),
                (int) Math.floor(bnds.getCenterY()),
                1,
                1);
      }
    }

    public void paint(Graphics2D g) {
      if (oh.isDrawingPage()) {
        final Rectangle2D bBoxGraph = getPageBounds();
        final Color colorBkp = g.getColor();
        g.setColor(PAGE_BACKGROUND);
        g.fill(bBoxGraph);
        g.setColor(Color.DARK_GRAY);
        g.draw(bBoxGraph);
        g.setColor(colorBkp);
      }
    }

    private Rectangle2D getPageBounds() {
      final Graph2D graph = view.getGraph2D();
      final Rectangle2D bBoxGraph = LayoutTool.getBoundingBox(graph, graph.nodes(), graph.edges(), false);
      final double cx = bBoxGraph.getCenterX();
      final double cy = bBoxGraph.getCenterY();
      final int maxPageWidth = oh.getMaximumWidth();
      final int maxPageHeight = oh.getMaximumHeight();
      bBoxGraph.setFrame(
              cx - maxPageWidth * 0.5,
              cy - maxPageHeight * 0.5,
              maxPageWidth,
              maxPageHeight);
      return bBoxGraph;
    }
  }



  /**
   * Used to store the location (page number) for node elements.
   */
  private static class LocationInfo {
    int pageNo;
    Node node;

    LocationInfo(final int pageNo, final Node node) {
      this.pageNo = pageNo;
      this.node = node;
    }
  }


  /**
   * Stores the result of a multi-page layout calculation.
   */
  private static class SimpleLayoutCallback implements LayoutCallback {
    private MultiPageLayout result;

    public void layoutDone( final MultiPageLayout result ) {
      this.result = result;
    }

    MultiPageLayout pop() {
      final MultiPageLayout result = this.result;
      this.result = null;
      return result;
    }
  }


  /**
   * Abstract base class for {@link ViewMode}s that supports jumping to other
   * page graphs when clicking a linked node. 
   */
  abstract static class LinkViewMode extends ViewMode {
    private Node hitNode;

    /**
     * Provides visual feedback similar to hyperlink activation in web browsers
     * when the mouse is moved over a node that can be clicked to jump to
     * another page graph.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseMoved( final double x, final double y ) {
      final Graph2DView view = this.view;
      final Graph2D graph = view.getGraph2D();
      final HitInfo hitInfo = getHitInfo(x, y);
      final Node oldHitNode = hitNode;
      hitNode = hitInfo.getHitNode();
      if (hitNode != oldHitNode) {
        if (oldHitNode != null) {
          setActive(graph, oldHitNode, false);
        }

        if (hitNode != null && isLink(hitNode)) {
          setToolTipText(hitNode);
          setActive(graph, hitNode, true);
          view.setViewCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
          reset(view);
        }
        view.updateView();
      }
    }

    public void mouseExited() {
      final Node oldHitNode = hitNode;
      if (oldHitNode != null) {
        hitNode = null;
        setActive(view.getGraph2D(), oldHitNode, false);
        reset(view);
        view.updateView();
      }
    }

    /**
     * Marks the specified node as active in a sense similar to hyperlink
     * activation in web browsers.
     * @param graph the graph that holds the node to mark.
     * @param node the node to mark as active.
     * @param active the node's new active state.
     */
    void setActive( final Graph2D graph, final Node node, final boolean active ) {
      final NodeRealizer nr = graph.getRealizer(node);
      if (nr.labelCount() > 0) {
        nr.getLabel().setUnderlinedTextEnabled(active);
      }
    }

    void reset( final Graph2DView view ) {
      view.setViewCursor(Cursor.getDefaultCursor());
      view.setToolTipText(null);
    }

    /**
     * Checks the specified coordinates for a node hit.
     * @param x the x-coordinate of the location to check.
     * @param y the y-coordinate of the location to check.
     * @return the node hit information corresponding to the specified location.
     */
    public HitInfo getHitInfo( final double x, final double y ) {
      final HitInfo info = view.getHitInfoFactory().createHitInfo(x, y, Graph2DTraversal.NODES, true);
      setLastHitInfo(info);
      return info;
    }

    /**
     * Determines whether or not the specified node may be clicked to jump
     * to another page graph.
     * @param node the node to check.
     * @return <code>true</code> if the specified node may be clicked to jump
     * to another page graph; <code>false</code> otherwise.
     */
    abstract boolean isLink( Node node );

    /**
     * Sets the tool tip text of the graph view that is associated to this view
     * mode to display detail information for the specified node.
     * @param node the node whose details are displayed.
     */
    abstract void setToolTipText( Node node );
  }

  /**
   * Supports jumping to page graphs from the multi-page overview component.
   */
  class OverviewViewMode extends LinkViewMode {
    /**
     * Jumps to another page graph if a linked node is clicked.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseClicked( final double x, final double y ) {
      final int page = getPage(x, y);
      if (page > 0 && page - 1 != currentPageIndex) {
        reset(view);
        setPageGraph(page - 1);
      }
    }

    /**
     * Determines whether or not the specified node is a link to another page
     * graph.
     * @param node the node to check.
     * @return <code>true</code> if the specified node's default label is
     * a valid page number; <code>false</code> otherwise.
     */
    boolean isLink( final Node node ) {
      final int page = getPage(node);
      return page > 0 && page - 1 != currentPageIndex;
    }

    /**
     * Sets the tool tip text <em>Go to page x</em> where <code>x</code> is
     * the index of the page graph that is linked by the specified node.
     * @param node the node whose details are displayed.
     */
    void setToolTipText( final Node node ) {
      view.setToolTipText("Go to page " + getPage(node));
    }

    /**
     * Determines whether or not the specified location lies on a node that
     * links to another page graph.
     * @param x the x-coordinate of the hit location.
     * @param y the y-coordinate of the hit location.
     * @return the index of a page graph that is linked from the specified
     * location or <code>-1</code> if the specified location does not link to
     * another page graph.
     */
    private int getPage( final double x, final double y ) {
      final HitInfo hitInfo = getHitInfo(x, y);
      if (hitInfo.hasHitNodes()) {
        return getPage(hitInfo.getHitNode());
      } else {
        return -1;
      }
    }

    /**
     * Determines whether or not the specified node links to another page graph.
     * @param node the node to check.
     * @return the index of a page graph that is linked to the specified
     * node or <code>-1</code> if the specified node does not link to
     * another page graph.
     */
    private int getPage( final Node node ) {
      final NodeRealizer nr = getGraph2D().getRealizer(node);
      if (nr.labelCount() > 0) {
        try {
          return Integer.parseInt(nr.getLabelText());
        } catch (NumberFormatException e) {
          return -1;
        }
      }
      return -1;
    }
  }

  /**
   * Supports jumping to page graphs for
   * {@link NodeInfo#TYPE_CONNECTOR connector},
   * {@link NodeInfo#TYPE_PROXY proxy}, and
   * {@link NodeInfo#TYPE_PROXY_REFERENCE proxy reference} nodes in the demo's
   * main view component.
   */
  class PageViewMode extends LinkViewMode {
    /**
     * Jumps to another page graph if a
     * {@link NodeInfo#TYPE_CONNECTOR connector},
     * {@link NodeInfo#TYPE_PROXY proxy}, or
     * {@link NodeInfo#TYPE_PROXY_REFERENCE proxy reference} node is clicked.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseClicked( final double x, final double y ) {
      if (lastClickEvent.getButton() == MouseEvent.BUTTON1) {
        final Graph2DView view = this.view;
        final HitInfo info = view.getHitInfoFactory().createHitInfo(x, y,
            Graph2DTraversal.NODES, true);
        if (info.hasHitNodes()) {
          reset(view);
          final Node hitNode = info.getHitNode();
          jump(pageBuilder.getReferencingNodeId(hitNode),
               view.getGraph2D().getLabelText(hitNode));
        }
      }
    }

    /**
     * Determines whether or not the specified node is a link to another page
     * graph.
     * @param node the node to check.
     * @return <code>true</code> if the specified node references another node;
     * <code>false</code> otherwise.
     */
    boolean isLink( final Node node ) {
      return pageBuilder.getReferencingNodeId(node) != null;
    }

    /**
     * Sets the tool tip text of the graph view that is associated to this view
     * mode to display detail information for the specified node.
     * @param node the node whose details are displayed.
     */
    void setToolTipText( final Node node ) {
      final Graph2DView view = this.view;
      final int pageNo = getLocationInfo(
              pageBuilder.getReferencingNodeId(node)).pageNo + 1;
      switch (pageBuilder.getNodeType(node)) {
        case NodeInfo.TYPE_PROXY:
          view.setToolTipText(
                  "<html>" +
                  "<h3>Proxy</h3>" +
                  "<p>Transfers to the original node on page " + pageNo +
                  ".</p></html>");
          break;
        case NodeInfo.TYPE_PROXY_REFERENCE:
          view.setToolTipText(
                  "<html>" +
                  "<h3>Proxy Reference</h3>" +
                  "<p>Transfers to the proxy on page " + pageNo +
                  ".</p></html>");
          break;
        case NodeInfo.TYPE_CONNECTOR:
          view.setToolTipText(
                  "<html>" +
                  "<h3>Connector</h3>" +
                  "<p>Transfers to the opposite node of the connecting edge" +
                  " on page " + pageNo + ".</p></html>");
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }
}
