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
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.layout.EdgeLabelLayout;
import y.layout.EdgeLabelModel;
import y.layout.RotatedDiscreteEdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.labeling.AbstractLabelingAlgorithm;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.option.CompoundEditor;
import y.option.ConstraintManager;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EditorFactory;
import y.option.ItemEditor;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DViewActions;
import y.view.PopupMode;
import y.view.SmartEdgeLabelModel;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This demo shows how to configure edge labels and the corresponding edge label models as well as how to apply the
 * generic edge labeling algorithm.
 * <p/>
 * To add a new edge label right-click on an edge and choose item "Add Label". The properties of an existing edge label
 * (i.e., its label text and its preferred placement) can be changed by right-click on the label and choose item "Edit
 * Properties". Edge labels can be moved to another valid position according to the current label model by using drag
 * and drop.
 * <p/>
 * The demo allows to switch between two sample graphs (using the combo box in the toolbar), i.e., a graph drawn with an
 * orthogonal layout algorithm as well as a graph drawn with an organic layout algorithm. To manually start the generic
 * labeling algorithm click on the "Do Generic Labeling" button. Note: after changing one of the properties stated
 * below, the generic labeling algorithm is applied automatically.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/labeling.html#labeling">Section Automatic Label Placement</a> in the yFiles for Java Developer's Guide
 */
public class EdgeLabelingDemo extends DemoBase {
  //option handler texts
  private static final String PROPERTIES_GROUP = "Edge Label Properties";
  private static final String ROTATION_ANGLE_STRING = "Rotation Angle (Degrees)";
  private static final String LABELING_MODEL_STRING = "Labeling Model";
  private static final String ALLOW_90_DEGREE_DEVIATION_STRING = "Allow 90 Degree Deviation";
  private static final String AUTO_FLIPPING_STRING = "Auto Flipping";
  private static final String AUTO_ROTATE_STRING = "Auto Rotation";
  private static final String EDGE_TO_LABEL_DISTANCE_STRING = "Edge To Label Distance";

  //edge label model constants
  private static final String MODEL_CENTERED = "Centered";
  private static final String MODEL_TWO_POS = "2 Pos";
  private static final String MODEL_SIX_POS = "6 Pos";
  private static final String MODEL_THREE_POS_CENTER = "3 Pos Center";
  private static final String MODEL_CENTER_SLIDER = "Center Slider";
  private static final String MODEL_SIDE_SLIDER = "Side Slider";
  private static final String[] EDGE_LABEL_MODELS = {
      MODEL_CENTERED, MODEL_TWO_POS, MODEL_SIX_POS, MODEL_THREE_POS_CENTER, MODEL_CENTER_SLIDER, MODEL_SIDE_SLIDER
  };

  private static final String CUSTOM_LABELING_CONFIG_NAME = "CUSTOM_LABELING_CONFIG";
  private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
  static final Color LABEL_BACKGROUND_COLOR = Color.WHITE;
  private static final int TOOLS_PANEL_WIDTH = 350;

  private DefaultLabelConfiguration customLabelConfig;
  private Map label2Model;
  private final OptionHandler optionHandler;

  public EdgeLabelingDemo() {
    this(null);
  }

  public EdgeLabelingDemo(final String helpFilePath) {
    //set view size and create content pane
    view.setPreferredSize(new Dimension(650, 400));
    view.setWorldRect(0, 0, 650, 400);
    view.setFitContentOnResize(true);

    // create the labeling option handler and the content pane
    optionHandler = createOptionHandler();
    contentPane.add(createToolsPanel(helpFilePath), BorderLayout.EAST);

    //load initial graph
    loadGraph("resource/orthogonal.graphml");
  }

  protected void initialize() {
    label2Model = new HashMap();
    view.getGraph2D().addDataProvider(AbstractLabelingAlgorithm.LABEL_MODEL_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return label2Model.get(dataHolder);
      }
    });
  }

  /**
   * Does the label placement using the generic labeling algorithm.
   */
  void doLabelPlacement(final Object selectionKey) {
    //create a profit model that assigns higher profit to the given angle
    final double rotationAngle = Math.toRadians(optionHandler.getDouble(ROTATION_ANGLE_STRING));
    final DemoProfitModel profitModel = new DemoProfitModel(rotationAngle, 1.0, 0.5);

    //configure and run the layouter
    final GreedyMISLabeling labelLayouter = new GreedyMISLabeling();
    labelLayouter.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
    labelLayouter.setPlaceEdgeLabels(true);
    labelLayouter.setSelection(selectionKey);
    labelLayouter.setPlaceNodeLabels(false);
    labelLayouter.setProfitModel(profitModel);
    labelLayouter.setCustomProfitModelRatio(0.1);

    new Graph2DLayoutExecutor().doLayout(view, labelLayouter);

    view.updateView();
  }

  /**
   * Assigns the current settings of the option handler to all edge labels in the graph and the graph's default edge.
   */
  void updateEdgeLabels() {
    final Graph2D graph = view.getGraph2D();
    label2Model.clear();

    // update auto flipping on all existing labels
    customLabelConfig.setAutoFlippingEnabled(optionHandler.getBool(AUTO_FLIPPING_STRING));

    // configure the label models
    final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel(); //the model that specifies the dynamic behavior of the label
    edgeLabelModel.setAutoRotationEnabled(optionHandler.getBool(AUTO_ROTATE_STRING));
    final CompositeEdgeLabelModel labelingModel = getCurrentEdgeLabelModel(); //the model used by the labeling algorithm (can be different from the edge label model)

    // update the label of the default edge 
    final EdgeLabel defaultLabel = graph.getDefaultEdgeRealizer().getLabel();
    defaultLabel.setLabelModel(edgeLabelModel, edgeLabelModel.getDefaultParameter());

    //... and update the model of each edge label
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeLabelLayout[] labelLayouts = graph.getEdgeLabelLayout(edge);
      for (int i = 0; i < labelLayouts.length; i++) {
        final EdgeLabel label = (EdgeLabel) labelLayouts[i];
        label2Model.put(label, labelingModel);

        final Object parameter = edgeLabelModel.createModelParameter(label.getOrientedBox(),
            graph.getEdgeLayout(edge), graph.getNodeLayout(edge.source()), graph.getNodeLayout(edge.target())); //identify the parameter that fits the current position best
        label.setLabelModel(edgeLabelModel, parameter);
      }
    }
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    //customize label configuration
    final YLabel.Factory factory = EdgeLabel.getFactory();
    final Map defaultConfigImplementationsMap = factory.createDefaultConfigurationMap();
    customLabelConfig = new DefaultLabelConfiguration();
    customLabelConfig.setAutoFlippingEnabled(false);
    defaultConfigImplementationsMap.put(YLabel.Painter.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.Layout.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class, customLabelConfig);
    factory.addConfiguration(CUSTOM_LABELING_CONFIG_NAME, defaultConfigImplementationsMap);

    final EdgeLabel label = view.getGraph2D().getDefaultEdgeRealizer().getLabel();
    label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
  }

  /**
   * Creates an option handler with settings for label model and label size.
   */
  private OptionHandler createOptionHandler() {
    final OptionHandler oh = new OptionHandler("Options");
    oh.addDouble(ROTATION_ANGLE_STRING, 0.0, 0.0, 360.0);
    oh.addBool(AUTO_FLIPPING_STRING, true);
    oh.addEnum(LABELING_MODEL_STRING, EDGE_LABEL_MODELS, 4);
    oh.addBool(AUTO_ROTATE_STRING, true);
    oh.addBool(ALLOW_90_DEGREE_DEVIATION_STRING, true);
    oh.addDouble(EDGE_TO_LABEL_DISTANCE_STRING, 5.0, 1.0, 20.0);

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, PROPERTIES_GROUP);
    og.addItem(oh.getItem(ROTATION_ANGLE_STRING));
    og.addItem(oh.getItem(AUTO_FLIPPING_STRING));
    og.addItem(oh.getItem(EDGE_TO_LABEL_DISTANCE_STRING));
    og.addItem(oh.getItem(LABELING_MODEL_STRING));
    og.addItem(oh.getItem(AUTO_ROTATE_STRING));
    og.addItem(oh.getItem(ALLOW_90_DEGREE_DEVIATION_STRING));

    ConstraintManager cm = new ConstraintManager(oh);
    //only enable item EDGE_TO_LABEL_DISTANCE_STRING for models that do not place labels on the edge segments
    final String[] nonCenteredModels = {MODEL_TWO_POS, MODEL_SIX_POS, MODEL_SIDE_SLIDER};
    cm.setEnabledOnCondition(cm.createConditionValueIs(LABELING_MODEL_STRING, nonCenteredModels),
        oh.getItem(EDGE_TO_LABEL_DISTANCE_STRING));

    cm.setEnabledOnValueEquals(AUTO_ROTATE_STRING, Boolean.TRUE, ALLOW_90_DEGREE_DEVIATION_STRING);

    oh.addChildPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateEdgeLabels();
        doLabelPlacement(null);
      }
    });

    return oh;
  }

  /**
   * Returns a label model for the current option handler settings.
   */
  private CompositeEdgeLabelModel getCurrentEdgeLabelModel() {
    final byte modelId = getModel(optionHandler.getEnum(LABELING_MODEL_STRING));
    final double angle = Math.toRadians(optionHandler.getDouble(ROTATION_ANGLE_STRING));

    final CompositeEdgeLabelModel compositeEdgeLabelModel = new CompositeEdgeLabelModel();
    compositeEdgeLabelModel.add(getEdgeLabelModel(modelId, optionHandler.getBool(AUTO_ROTATE_STRING),
        optionHandler.getDouble(EDGE_TO_LABEL_DISTANCE_STRING), angle));

    if (optionHandler.getBool(ALLOW_90_DEGREE_DEVIATION_STRING)) {
      //add model that creates label candidates for the alternative angle
      final double rotatedAngle = (angle + Math.PI * 0.5) % (2.0 * Math.PI);
      compositeEdgeLabelModel.add(getEdgeLabelModel(modelId, optionHandler.getBool(AUTO_ROTATE_STRING),
          optionHandler.getDouble(EDGE_TO_LABEL_DISTANCE_STRING), rotatedAngle));
    }

    return compositeEdgeLabelModel;
  }

  /**
   * Returns the model type for the specified index.
   */
  private static byte getModel(int index) {
    if (index < 0 || index >= EDGE_LABEL_MODELS.length) {
      return EdgeLabel.SIDE_SLIDER;
    }

    final String modelString = EDGE_LABEL_MODELS[index];
    if (MODEL_CENTERED.equals(modelString)) {
      return EdgeLabel.CENTERED;
    } else if (MODEL_TWO_POS.equals(modelString)) {
      return EdgeLabel.TWO_POS;
    } else if (MODEL_SIX_POS.equals(modelString)) {
      return EdgeLabel.SIX_POS;
    } else if (MODEL_THREE_POS_CENTER.equals(modelString)) {
      return EdgeLabel.THREE_CENTER;
    } else if (MODEL_CENTER_SLIDER.equals(modelString)) {
      return EdgeLabel.CENTER_SLIDER;
    } else {
      return EdgeLabel.SIDE_SLIDER;
    }
  }

  /**
   * Creates and configures an edge label model using the given parameter.
   */
  private static EdgeLabelModel getEdgeLabelModel(byte modelId, boolean autoRotationEnabled, double distance,
                                                  double angle) {
    if (modelId == EdgeLabel.CENTER_SLIDER || modelId == EdgeLabel.SIDE_SLIDER) {
      final byte mode = (modelId == EdgeLabel.CENTER_SLIDER) ?
          RotatedSliderEdgeLabelModel.CENTER_SLIDER :
          RotatedSliderEdgeLabelModel.SIDE_SLIDER;
      RotatedSliderEdgeLabelModel elm = new RotatedSliderEdgeLabelModel(mode);
      elm.setAutoRotationEnabled(autoRotationEnabled);
      if (distance < 1.0 && modelId == EdgeLabel.SIDE_SLIDER) {
        elm.setDistance(1.0); //setting distance to 0 would automatically switch to CENTER_SLIDER model
      } else {
        elm.setDistance(distance);
      }
      elm.setAngle(angle);
      elm.setDistanceRelativeToEdge(true);
      return elm;
    } else {
      final int mode;
      if (modelId == EdgeLabel.TWO_POS) {
        mode = RotatedDiscreteEdgeLabelModel.TWO_POS;
      } else if (modelId == EdgeLabel.CENTERED) {
        mode = RotatedDiscreteEdgeLabelModel.CENTERED;
      } else if (modelId == EdgeLabel.THREE_CENTER) {
        mode = RotatedDiscreteEdgeLabelModel.THREE_CENTER;
      } else {
        mode = RotatedDiscreteEdgeLabelModel.SIX_POS; //default value
      }
      RotatedDiscreteEdgeLabelModel elm = new RotatedDiscreteEdgeLabelModel(mode);
      elm.setAutoRotationEnabled(autoRotationEnabled);
      elm.setAngle(angle);
      elm.setDistance(distance);
      elm.setPositionRelativeToSegment(true);
      return elm;
    }
  }

  /**
   * Creates the tools panel containing the settings and the help panel.
   */
  private JPanel createToolsPanel(String helpFilePath) {
    JPanel toolsPanel = new JPanel(new BorderLayout());
    toolsPanel.add(createOptionHandlerComponent(optionHandler), BorderLayout.NORTH);

    if (helpFilePath != null) {
      final URL url = getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        JComponent helpPane = createHelpPane(url);
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
   * Create a menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);
    fileMenu.add(new ExitAction());

    createExamplesMenu(menuBar);

    return menuBar;
  }

  protected String[] getExampleResources() {
    return new String[]{
        "resource/orthogonal.graphml",
        "resource/organic.graphml"
    };
  }

  /**
   * Creates an EditMode and adds a popup mode that displays the demo context menu.
   */
  protected void registerViewModes() {
    EditMode mode = new EditMode();
    mode.setPopupMode(new DemoPopupMode());
    view.addViewMode(mode);
  }

  /**
   * Creates the default view actions but removes the mnemonic for label editing since it is complicated to update the
   * model if a new label is created by such an edit.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    view.getCanvasComponent().getActionMap().remove(Graph2DViewActions.EDIT_LABEL);
  }

  /**
   * Creates the default tool bar and adds additional buttons for label placement.
   */
  protected JToolBar createToolBar() {
    final JToolBar bar = super.createToolBar();
    bar.addSeparator();

    //the layout button
    bar.add(createActionControl(new AbstractAction(
            "Place Labels", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        doLabelPlacement(null);
      }
    }));

    return bar;
  }

  /**
   * Loads a graph and applies the label configuration to the existing labels.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    DemoDefaults.applyRealizerDefaults(view.getGraph2D());
    final Graph2D graph = view.getGraph2D();
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final EdgeLabelLayout[] labelLayouts = graph.getEdgeLabelLayout(ec.edge());
      for (int i = 0; i < labelLayouts.length; i++) {
        final EdgeLabel label = (EdgeLabel) labelLayouts[i];
        label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
      }
    }

    updateEdgeLabels();
    doLabelPlacement(null);
  }

  class DemoPopupMode extends PopupMode {

    public JPopupMenu getEdgePopup(final Edge edge) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Add Label") {

        public void actionPerformed(ActionEvent e) {
          if (edge == null) {
            return;
          }

          final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel(); //the model that specifies the dynamic behavior of the label
          edgeLabelModel.setAutoRotationEnabled(optionHandler.getBool(AUTO_ROTATE_STRING));
          final EdgeLabel label = new EdgeLabel("Label");
          label2Model.put(label, getCurrentEdgeLabelModel()); //sets the model used by the labeling algorithm
          label.setConfiguration(CUSTOM_LABELING_CONFIG_NAME);
          label.setLineColor(LABEL_LINE_COLOR);
          label.setBackgroundColor(LABEL_BACKGROUND_COLOR);
          label.setLabelModel(edgeLabelModel, edgeLabelModel.getDefaultParameter());
          view.getGraph2D().getRealizer(edge).addLabel(label);

          //place the new label
          final Graph graph = edge.getGraph();
          if (graph != null) {
            graph.addDataProvider("SELECTED_LABELS", new DataProviderAdapter() {
              public boolean getBool(Object dataHolder) {
                return label == dataHolder;
              }
            });
            doLabelPlacement("SELECTED_LABELS");
            graph.removeDataProvider("SELECTED_LABELS");
          }

          view.getGraph2D().updateViews();
        }
      });
      return pm;
    }

    public JPopupMenu getEdgeLabelPopup(final EdgeLabel label) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Edit Label") {

        public void actionPerformed(ActionEvent e) {
          if (label != null) {
            EdgeLabelPropertyHandler ph = new EdgeLabelPropertyHandler(label, view);
            ph.showEditor(null, OptionHandler.OK_APPLY_CANCEL_BUTTONS);
          }
        }
      });
      return pm;
    }
  }

  /**
   * Creates a component for the specified option handler using the default editor factory and sets all of its items to
   * auto adopt and auto commit.
   */
  private static JComponent createOptionHandlerComponent(OptionHandler oh) {
    final EditorFactory defaultEditorFactory = new DefaultEditorFactory();
    final Editor editor = defaultEditorFactory.createEditor(oh);

    //propagate auto adopt and auto commit to editor and its children
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

    //build and return component
    JComponent optionComponent = editor.getComponent();
    optionComponent.setMinimumSize(new Dimension(200, 50));
    return optionComponent;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new EdgeLabelingDemo("resource/edgelabelingdemohelp.html")).start("Edge Labeling Demo");
      }
    });
  }
}