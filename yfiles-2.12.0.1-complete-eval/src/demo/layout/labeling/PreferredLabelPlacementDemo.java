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
package demo.layout.labeling;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.layout.AbstractLayoutStage;
import y.layout.CanonicMultiStageLayouter;
import y.layout.EdgeLabelLayout;
import y.layout.FixNodeLayoutStage;
import y.layout.LabelLayoutTranslator;
import y.layout.LayoutGraph;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.labeling.AbstractLabelingAlgorithm;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.TreeReductionStage;
import y.option.CompoundEditor;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EditorFactory;
import y.option.ItemEditor;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.util.DataProviders;
import y.view.EdgeLabel;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;
import y.view.PopupMode;
import y.view.Selections;
import y.view.SmartEdgeLabelModel;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This demo shows how to configure the <code>PreferredPlacementDescriptor</code> of labels and how this effects the
 * label placement using different layouters.
 * <p>
 *   The descriptor settings can be changed for all or only for a subset of the labels and a new layout is calculated
 *   after each change to visualize the impact of the corresponding descriptor setting.
 * </p>
 * <p>
 *   Two different label painters are used to either visualize the descriptor settings of a label or its oriented bounds.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/labeling.html#label_preferred_placement">Section Preferred Placement of Edge Labels</a> in the yFiles for Java Developer's Guide
 */
public class PreferredLabelPlacementDemo extends DemoBase {
  //option handler texts
  private static final String VISUALIZING_DESCRIPTOR_LABELING_CONFIG_NAME = "VISUALIZING_DESCRIPTOR_LABELING_CONFIG";
  private static final String VISUALIZING_BOUNDS_LABELING_CONFIG_NAME = "VISUALIZING_BOUNDS_LABELING_CONFIG";
  private static final int TOOLS_PANEL_WIDTH = 350;

  private final PreferredPlacementOptionHandler optionHandler;
  private boolean isSelectionFinished;
  private Layouter layouter;
  private String labelVisualization;

  public PreferredLabelPlacementDemo() {
    this(null);
  }

  public PreferredLabelPlacementDemo(final String helpFilePath) {
    //set view size and create content pane
    view.setPreferredSize(new Dimension(650, 400));
    view.setWorldRect(0, 0, 650, 400);
    view.setFitContentOnResize(true);

    // create the labeling option handler and the content pane
    optionHandler = new PreferredPlacementOptionHandler();
    optionHandler.addChildPropertyChangeListener(
      new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent evt) {
          if (isSelectionFinished && !hasUndefinedChanged(evt)) {
            // Prevents the option handler to commit values if only the value of "valueUndefined"
            // changes from "TRUE" to to "FALSE" but not the value itself.
            optionHandler.commitValues(view.getGraph2D());
            runLayout(false);
          }
        }
      });
    contentPane.add(createToolsPanel(helpFilePath), BorderLayout.EAST);

    // update the option handler when the selection of the edge labels has been changed
    final Selections.SelectionStateObserver sso = new Selections.SelectionStateObserver() {
      protected void updateSelectionState(Graph2D graph) {
        isSelectionFinished = false;
        optionHandler.updateValues(graph);
        isSelectionFinished = true;
      }
    };
    view.getGraph2D().addGraph2DSelectionListener(sso);
    view.getGraph2D().addGraphListener(sso);

    // fix node port stage is used to keep the bounding box of the graph in the view port
    view.getGraph2D().addDataProvider(
      FixNodeLayoutStage.FIXED_NODE_DPKEY,
      DataProviders.createConstantDataProvider(Boolean.TRUE));

    //load initial graph
    loadGraph("resource/labeledgraph.graphml");
  }

  private static boolean hasUndefinedChanged(final PropertyChangeEvent evt) {
    return  "valueUndefined".equals(evt.getPropertyName()) &&
            Boolean.TRUE.equals((Boolean)evt.getOldValue()) &&
            Boolean.FALSE.equals((Boolean)evt.getNewValue());
  }

  /**
   * Creates an edit mode that does not allow to change the graph except the edge labels.
   */
  protected EditMode createEditMode() {
    final EditMode editMode = new EditMode();
    editMode.allowEdgeCreation(false);
    editMode.allowNodeCreation(false);
    editMode.allowBendCreation(false);
    editMode.allowNodeEditing(false);
    editMode.allowResizeNodes(false);
    editMode.allowMoveSelection(false);
    return editMode;
  }

  protected void initialize() {
    initializeLabelRendering();
  }

  protected boolean isClipboardEnabled() {
    return false;
  }

  private void initializeLabelRendering() {
    // Workaround that better keeps the label text inside its node for different zoom levels.
    final RenderingHints rh = view.getRenderingHints();
    rh.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    view.setRenderingHints(rh);
    YLabel.setFractionMetricsForSizeCalculationEnabled(true);
  }

  private static AbstractLabelingAlgorithm createGreedyMISLabeling() {
    // pure labeling algorithm - nodes and edges are not moved
    final GreedyMISLabeling labeling = new GreedyMISLabeling();
    labeling.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
    labeling.setPlaceEdgeLabels(true);
    labeling.setPlaceNodeLabels(false);
    return labeling;
  }

  private Layouter createIncrementalHierarchicLayouter(final byte layoutOrientation) {
    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    ihl.setIntegratedEdgeLabelingEnabled(true);
    ihl.setLayoutOrientation(layoutOrientation);
    disableAutoFlipping(ihl);
    return ihl;
  }

  private Layouter createGenericTreeLayouter() {
    final TreeReductionStage reductionStage = new TreeReductionStage();
    reductionStage.setNonTreeEdgeRouter(new NonTreeEdgeRouterStage());
    reductionStage.setNonTreeEdgeSelectionKey(Layouter.SELECTED_EDGES);

    final GenericTreeLayouter genericTreeLayouter = new GenericTreeLayouter();
    genericTreeLayouter.setIntegratedEdgeLabeling(true);
    genericTreeLayouter.prependStage(reductionStage);
    disableAutoFlipping(genericTreeLayouter);
    return genericTreeLayouter;
  }

  private Layouter createOrthogonalLayouter() {
    final OrthogonalLayouter orthogonalLayouter = new OrthogonalLayouter();
    orthogonalLayouter.setIntegratedEdgeLabelingEnabled(true);
    disableAutoFlipping(orthogonalLayouter);
    return orthogonalLayouter;
  }

  private void disableAutoFlipping(final CanonicMultiStageLayouter canonicMultiStageLayouter) {
    final LabelLayoutTranslator labelLayouter = (LabelLayoutTranslator) canonicMultiStageLayouter.getLabelLayouter();
    labelLayouter.setAutoFlippingEnabled(false);
  }

  /**
   * Does the label placement or a complete layout depending on the selected layout algorithm.
   *
   * @param fitViewToContent whether or not fit the view to the content after layout has been calculated
   */
  void runLayout(final boolean fitViewToContent) {
    //configure and run the layouter
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setKeepZoomFactor(!fitViewToContent);
    layoutExecutor.doLayout(view, new FixNodeLayoutStage(layouter));
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    //customize label configuration
    final YLabel.Factory factory = EdgeLabel.getFactory();

    final Map visualizingDescriptorConfigurationMap = factory.createDefaultConfigurationMap();
    VisualizingDescriptorLabelConfiguration visualizingDescriptorLabelConfig = new VisualizingDescriptorLabelConfiguration();
    visualizingDescriptorLabelConfig.setAutoFlippingEnabled(false);
    visualizingDescriptorConfigurationMap.put(YLabel.Painter.class, visualizingDescriptorLabelConfig);
    visualizingDescriptorConfigurationMap.put(YLabel.Layout.class, visualizingDescriptorLabelConfig);
    visualizingDescriptorConfigurationMap.put(YLabel.BoundsProvider.class, visualizingDescriptorLabelConfig);
    factory.addConfiguration(VISUALIZING_DESCRIPTOR_LABELING_CONFIG_NAME, visualizingDescriptorConfigurationMap);

    final Map visualizeBoundsConfigurationMap = factory.createDefaultConfigurationMap();
    VisualizingBoundsLabelConfiguration visualizeBoundsLabelConfig = new VisualizingBoundsLabelConfiguration();
    visualizeBoundsLabelConfig.setAutoFlippingEnabled(false);
    visualizeBoundsConfigurationMap.put(YLabel.Painter.class, visualizeBoundsLabelConfig);
    visualizeBoundsConfigurationMap.put(YLabel.Layout.class, visualizeBoundsLabelConfig);
    visualizeBoundsConfigurationMap.put(YLabel.BoundsProvider.class, visualizeBoundsLabelConfig);
    factory.addConfiguration(VISUALIZING_BOUNDS_LABELING_CONFIG_NAME, visualizeBoundsConfigurationMap);
  }

  protected EdgeLabel createEdgeLabel() {
    final EdgeLabel label = new EdgeLabel("Label");
    final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
    label.setLabelModel(edgeLabelModel, edgeLabelModel.getDefaultParameter());
    label.setConfiguration(labelVisualization);
    return label;
  }

  /**
   * Creates the tools panel containing the settings and the help panel.
   */
  private JPanel createToolsPanel(final String helpFilePath) {
    final JPanel toolsPanel = new JPanel(new BorderLayout());
    toolsPanel.add(createOptionHandlerComponent(optionHandler), BorderLayout.NORTH);

    if (helpFilePath != null) {
      final URL url = getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        final JComponent helpPane = createHelpPane(url);
        if (helpPane != null) {
          helpPane.setMinimumSize(new Dimension(200, 200));
          helpPane.setPreferredSize(new Dimension(TOOLS_PANEL_WIDTH, 400));
          toolsPanel.add(helpPane, BorderLayout.CENTER);
        }
      }
    }
    return toolsPanel;
  }

  /**
   * Creates an EditMode and adds a popup mode that displays the demo context menu.
   */
  protected void registerViewModes() {
    final EditMode mode = createEditMode();
    mode.setPopupMode(new DemoPopupMode());
    view.addViewMode(mode);
  }

  /**
   * Creates the default view actions but removes the mnemonic for label editing since it is complicated to update the
   * model if a new label is created by such an edit.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    final ActionMap actionMap = view.getCanvasComponent().getActionMap();
    actionMap.remove(Graph2DViewActions.EDIT_LABEL);
  }

  /**
   * Creates a {@link DeleteSelection} that only deletes edge labels.
   */
  protected Action createDeleteSelectionAction() {
    final ActionMap actionMap = view.getCanvasComponent().getActionMap();
    final Graph2DViewActions.DeleteSelectionAction deleteAction =
      (Graph2DViewActions.DeleteSelectionAction) actionMap.get(Graph2DViewActions.DELETE_SELECTION);
    deleteAction.putValue(Action.SMALL_ICON, getIconResource("resource/delete.png"));
    deleteAction.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    deleteAction.setDeletionMask(Graph2DViewActions.DeleteSelectionAction.TYPE_EDGE_LABEL);
    return deleteAction;
  }

  /**
   * Creates the default tool bar and adds a button for label placement.
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createLayoutersComboBox());
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(createLayoutButton());
    toolBar.addSeparator();
    toolBar.add(createLabelVisualizationsComboBox());
    return toolBar;
  }

  private JComboBox createLayoutersComboBox() {
    final JComboBox comboBox = new JComboBox(
      new Object[]{
        "Generic Label Placement",
        "Hierarchic Layout, Top-Down",
        "Hierarchic Layout, Left-Right",
        "Generic Tree Layout",
        "Orthogonal Layout"});
    final Layouter[] layouters = {
      createGreedyMISLabeling(),
      createIncrementalHierarchicLayouter(LayoutOrientation.TOP_TO_BOTTOM),
      createIncrementalHierarchicLayouter(LayoutOrientation.LEFT_TO_RIGHT),
      createGenericTreeLayouter(),
      createOrthogonalLayouter()};

    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          layouter = layouters[comboBox.getSelectedIndex()];
          runLayout(true);
        }
      });
    layouter = layouters[0];
    return comboBox;
  }

  private JComponent createLayoutButton() {
    return createActionControl(
        new AbstractAction(
          "Layout", SHARED_LAYOUT_ICON) {
          public void actionPerformed(ActionEvent e) {
            runLayout(true);
          }
        });
  }

  private JComboBox createLabelVisualizationsComboBox() {
    final JComboBox comboBox = new JComboBox( new Object[]{"Visualize Label Descriptor", "Visualize Label Bounds"});
    final String[] visualizations = {VISUALIZING_DESCRIPTOR_LABELING_CONFIG_NAME, VISUALIZING_BOUNDS_LABELING_CONFIG_NAME};

    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          labelVisualization = visualizations[comboBox.getSelectedIndex()];
          setSelectedLabelVisualization();
          view.updateView();
        }
      });
    labelVisualization = visualizations[0];
    return comboBox;
  }

  /**
   * Loads a graph and applies the label configuration to the existing labels.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    DemoDefaults.applyRealizerDefaults(view.getGraph2D());
    setSelectedLabelVisualization();

    runLayout(true);
    getUndoManager().resetQueue();
  }

  private void setSelectedLabelVisualization() {
    final Graph2D graph = view.getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final EdgeLabelLayout[] labelLayouts = graph.getEdgeLabelLayout(ec.edge());
      for (int i = 0; i < labelLayouts.length; i++) {
        final EdgeLabel label = (EdgeLabel) labelLayouts[i];
        label.setConfiguration(labelVisualization);
      }
    }
  }

  /**
   * As our demo graph is no tree, we need a TreeReductionStage.
   * To layout the non-tree edges and labels we combine an EdgeRouter for the edges with the GreedyMISLabeling
   * layouter for the labels
   */
  private static class NonTreeEdgeRouterStage extends AbstractLayoutStage {
    private final EdgeRouter nonTreeEdgeRouter;
    private final AbstractLabelingAlgorithm nonTreeEdgeLabelLayouter;

    public NonTreeEdgeRouterStage() {
      this.nonTreeEdgeRouter = new EdgeRouter();
      this.nonTreeEdgeLabelLayouter = createGreedyMISLabeling();
    }

    public boolean canLayout(LayoutGraph graph) {
      return nonTreeEdgeRouter.canLayout(graph) && nonTreeEdgeLabelLayouter.canLayout(graph);
    }

    public void doLayout(final LayoutGraph graph) {
      // first layout the non-tree edges
      nonTreeEdgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      nonTreeEdgeRouter.doLayout(graph);

      // the tree reduction stage only prepares a data provider to mark the non-tree edges but we need
      // a provider to mark the labels of all non-tree edges:
      // for t
      final DataProvider nonTreeEdgeDP = graph.getDataProvider(Layouter.SELECTED_EDGES);
      final DataProvider labelsOfNonTreeEdgesDP = new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          final Edge edge = graph.getFeature((EdgeLabelLayout) dataHolder);
          return nonTreeEdgeDP.getBool(edge);
        }
      };
      graph.addDataProvider("nonTreeLabels", labelsOfNonTreeEdgesDP);
      nonTreeEdgeLabelLayouter.setSelection("nonTreeLabels");
      nonTreeEdgeLabelLayouter.doLayout(graph);
      graph.removeDataProvider("nonTreeLabels");
    }

  }

  /**
   * A {@link PopupMode} that adds labels to edges and calculates a new layout.
   */
  class DemoPopupMode extends PopupMode {
    public JPopupMenu getEdgePopup(final Edge edge) {
      final JPopupMenu popupMenu = new JPopupMenu();
      popupMenu.add(
        new AbstractAction("Add Label") {
          public void actionPerformed(ActionEvent e) {
            // Insert backup for correct undo.
            final Graph2D graph2D = view.getGraph2D();
            graph2D.firePreEvent();
            try {
              graph2D.backupRealizers(graph2D.edges());
              graph2D.backupRealizers(graph2D.nodes());

              // Create and add a new edge label to the given edge.
              final EdgeLabel label = createEdgeLabel();
              graph2D.getRealizer(edge).addLabel(label);

              // place the new label by running a new layout calculation
              runLayout(true);
            } finally {
              graph2D.firePostEvent();
            }
          }
        });
      return popupMenu;
    }
  }

  /**
   * Creates a component for the specified option handler using the default editor factory and sets all of its items to
   * auto adopt and auto commit.
   */
  private static JComponent createOptionHandlerComponent(final OptionHandler oh) {
    final EditorFactory defaultEditorFactory = new DefaultEditorFactory();
    final Editor editor = defaultEditorFactory.createEditor(oh);

    // Propagate auto adopt and auto commit to editor and its children.
    final List stack = new ArrayList();
    stack.add(editor);
    while (!stack.isEmpty()) {
      Object editorObj = stack.remove(stack.size() - 1);
      if (editorObj instanceof ItemEditor) {
        ((ItemEditor) editorObj).setAutoAdopt(true);
        ((ItemEditor) editorObj).setAutoCommit(true);
      }
      if (editorObj instanceof CompoundEditor) {
        for (Iterator iter = ((CompoundEditor) editorObj).editors(); iter.hasNext();) {
          stack.add(iter.next());
        }
      }
    }

    // Build and return component.
    final JComponent optionComponent = editor.getComponent();
    optionComponent.setMinimumSize(new Dimension(200, 50));
    return optionComponent;
  }

  /**
   * Run this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new PreferredLabelPlacementDemo("resource/preferredlabelplacementdemohelp.html")).start("Preferred Label Placement Demo");
      }
    });
  }
}