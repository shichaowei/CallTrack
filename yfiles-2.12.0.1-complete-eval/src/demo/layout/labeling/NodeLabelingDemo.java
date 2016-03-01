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
import y.base.DataMap;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.AbstractLayoutStage;
import y.layout.DiscreteNodeLabelModel;
import y.layout.LabelCandidate;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.NodeLabelModel;
import y.layout.NodeLayout;
import y.layout.ProfitModel;
import y.layout.labeling.AbstractLabelingAlgorithm;
import y.layout.labeling.GreedyMISLabeling;
import y.layout.labeling.MISLabelingAlgorithm;
import y.layout.NodeLabelLayout;
import y.layout.BufferedLayouter;
import y.option.EditorFactory;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.ItemEditor;
import y.option.CompoundEditor;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.DefaultBackgroundRenderer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.SmartNodeLabelModel;
import y.view.YLabel;
import y.view.DefaultLabelConfiguration;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.geom.LineSegment;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.geom.Line2D;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 * This demo shows how to configure node labels and the corresponding label models as well as how to apply the
 * generic node label placement algorithm.
 * <p>
 * A new city (node) can be added by left-clicking on the corresponding map location. To edit a node label right-click
 * on the label or the corresponding node and choose item "Edit Label". Node labels can be moved to an arbitrary
 * position using drag and drop.
 * </p><p>
 * To manually start the generic labeling algorithm click on the "Do Generic Labeling" button. Note: after changing one
 * of the node label properties, the generic labeling algorithm is applied automatically.
 * </p><p>
 * <b>Third Party Licenses:</b><br/>
 * The USA map that is used as background in this demo is based on
 * <a href="http://commons.wikimedia.org/wiki/File:Blank_US_Map.svg">Blank_US_Map.svg by Theshibboleth</a>
 * and licensed under the
 * <a href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-ShareAlike 3.0 Unported</a>
 * license.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/labeling.html#labeling">Section Automatic Label Placement</a> in the yFiles for Java Developer's Guide
 */
public class NodeLabelingDemo extends DemoBase {
  private static final String LABELING_MODEL_STRING = "Labeling Model";
  private static final String LABEL_SIZE_STRING = "Font Size";
  private static final String PROPERTIES_GROUP = "Node Label Properties";

  //node label model constants
  private static final String MODEL_CORNERS = "Corners";  
  private static final String MODEL_SANDWICH = "Sandwich";
  private static final String MODEL_SIDE = "Side";
  private static final String MODEL_FREE = "Free";
  private static final String MODEL_EIGHT_POS = "8 Pos";
  private static final String[] NODE_LABEL_MODELS = {
      MODEL_CORNERS, MODEL_SANDWICH, MODEL_SIDE, MODEL_FREE, MODEL_EIGHT_POS
  };

  private static final int TOOLS_PANEL_WIDTH = 350;

  private Map label2Model; //stores (for each label) the label model used by the labeling algorithm
  private MyGenericNodeLabelingAlgorithm labelLayouter;
  private OptionHandler optionHandler;

  public NodeLabelingDemo() {
    this(null);
  }

  public NodeLabelingDemo(final String helpFilePath) {
    // render a map of the USA in the background
    DefaultBackgroundRenderer renderer = new DefaultBackgroundRenderer(view);

    // resource/usamap.png licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported license
    URL bgImage = getSharedResource("resource/usamap.png");

    renderer.setImageResource(bgImage);
    renderer.setMode(DefaultBackgroundRenderer.DYNAMIC);
    renderer.setColor(Color.white);
    view.setBackgroundRenderer(renderer);
    view.setPreferredSize(new Dimension(650, 400));
    view.setWorldRect(0, 0, 650, 400);

    contentPane.add(createToolsPanel(helpFilePath), BorderLayout.EAST);

    loadGraph("resource/uscities.graphml");

    //after creating a new node the label should be placed accordingly
    view.getGraph2D().addGraphListener(new GraphListener() {
      public void onGraphEvent(GraphEvent e) {
        if (e.getType() == GraphEvent.NODE_CREATION) {
          final Graph2D graph = view.getGraph2D();
          final Node node = (Node) e.getData();
          final NodeLabelLayout[] nll = graph.getNodeLabelLayout(node);

          //set the label model and size
          final int labelSize = optionHandler.getInt(LABEL_SIZE_STRING);
          final NodeLabelModel labelingModel = getModel(
              optionHandler.getEnum(LABELING_MODEL_STRING)); //the model used by the labeling algorithm
          for (int i = 0; i < nll.length; i++) {
            label2Model.put(nll[i], labelingModel);
            ((NodeLabel) nll[i]).setFontSize(labelSize);
          }

          //mark the new labels and place them
          final Set newLabels = new HashSet(Arrays.asList(nll));
          graph.addDataProvider("SELECTED_LABELS", new DataProviderAdapter() {
            public boolean getBool(Object dataHolder) {
              return newLabels.contains(dataHolder);
            }
          });
          final Object oldSelectionKey = labelLayouter.getSelectionKey();
          labelLayouter.setSelection("SELECTED_LABELS");
          new BufferedLayouter(labelLayouter).doLayout(graph);
          labelLayouter.setSelection(oldSelectionKey);
          graph.removeDataProvider("SELECTED_LABELS");
        }
      }
    });

    // do initial label placement
    doLabelPlacement();

    getUndoManager().resetQueue();
  }

  protected void initialize() {
    optionHandler = createOptionHandler();

    labelLayouter = new MyGenericNodeLabelingAlgorithm();

    //register the data-provider that stores for each node label the label model that is used by the labeling algorithm
    label2Model = new HashMap();
    view.getGraph2D().addDataProvider(AbstractLabelingAlgorithm.LABEL_MODEL_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        return label2Model.get(dataHolder);
      }
    });
  }

  /**
   * Customized labeling algorithm that prefers node labels that are placed at the top of the corresponding node.
   * The algorithm therefore wraps the GreedyMISLabeling algorithm and adds a corresponding profit model to it.
   */
  static class MyGenericNodeLabelingAlgorithm extends AbstractLayoutStage {
    private final GreedyMISLabeling coreLabeling;

    public MyGenericNodeLabelingAlgorithm() {
      this(null);
    }

    public MyGenericNodeLabelingAlgorithm(Layouter core) {
      super(core);
      this.coreLabeling = new GreedyMISLabeling();
      coreLabeling.setOptimizationStrategy(MISLabelingAlgorithm.OPTIMIZATION_BALANCED);
      coreLabeling.setPlaceEdgeLabels(false);
      coreLabeling.setPlaceNodeLabels(true);
      coreLabeling.setApplyPostprocessing(true);
    }

    /**
     * Sets the <code>DataProvider</code> key, under which the labeling selection can be retrieved. The data provider
     * registered with this key has to return <code>true</code> for labels that should be placed and
     * <code>false</code> for all other labels.
     *
     * @param key The key for a <code>DataProvider</code>.
     */
    public void setSelection(Object key) {
      coreLabeling.setSelection(key);
    }

    /**
     * Returns the labeling selection <code>DataProvider</code> key.
     */
    public Object getSelectionKey() {
      return coreLabeling.getSelectionKey();
    }

    public boolean canLayout(LayoutGraph graph) {
      return canLayoutCore(graph);
    }

    public void doLayout(LayoutGraph graph) {
      doLayoutCore(graph);

      //add profit model that prefers labels at the north of the corresponding node
      coreLabeling.setProfitModel(new PreferNorthLabelsProfitModel(graph));

      //call core labeling algorithm
      coreLabeling.doLayout(graph);
    }

    /**
     * Profit model that assigns larger profits to labels that are placed at the north (outside) of the corresponding
     * node.
     */
    private static class PreferNorthLabelsProfitModel implements ProfitModel {
      private static final double MAX_DISTANCE_WITH_PROFIT = 10;
      private static final double MAX_HORIZONTAL_CENTER_DISTANCE_WITH_PROFIT = 10;
      private DataMap label2NodeLayout;

      public PreferNorthLabelsProfitModel(final LayoutGraph graph) {
        updateLabel2NodeLayoutMap(graph);
      }

      /**
       * Creates a mapping between the labels and the corresponding node layout. It is used to determine
       * the profit of the labels.
       */
      private void updateLabel2NodeLayoutMap(final LayoutGraph graph) {
        this.label2NodeLayout = Maps.createHashedDataMap();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          final NodeLayout nLayout = graph.getNodeLayout(nc.node());
          final NodeLabelLayout[] nll = graph.getLabelLayout(nc.node());
          for (int i = 0; i < nll.length; i++) {
            label2NodeLayout.set(nll[i], nLayout);
          }
        }
      }

      private static boolean isLabelAboveNode(final YRectangle labelBox, final NodeLayout nodeLayout) {
        return nodeLayout.getY() > labelBox.getY() + labelBox.getHeight();
      }

      private static double calcHorizontalCenterDistance(final YRectangle labelBox, final NodeLayout nodeLayout) {
        return Math.abs((nodeLayout.getX() + nodeLayout.getWidth() * 0.5) - (labelBox.getX() + labelBox.getWidth() * 0.5));
      }

      private static double calcDistance(YRectangle r1, YRectangle r2) {
        if (YRectangle.intersects(r1, r2)) {
          return 0.0;
        } else {
          final double distVertical = calculateOrthogonalDifference(r1, r2, false);
          final double distHoriztonal = calculateOrthogonalDifference(r1, r2, true);
          return Math.sqrt(distVertical * distVertical + distHoriztonal * distHoriztonal);
        }
      }

      private static double calculateOrthogonalDifference(YRectangle rect1, YRectangle rect2, boolean horizontal) {
        final double rect1Min = horizontal ? rect1.getX() : rect1.getY();
        final double rect1Max = horizontal ? rect1.getX() + rect1.getWidth() : rect1.getY() + rect1.getHeight();
        final double rect2Min = horizontal ? rect2.getX() : rect2.getY();
        final double rect2Max = horizontal ? rect2.getX() + rect2.getWidth() : rect2.getY() + rect2.getHeight();

        if (rect2Max < rect1Min) {
          // complete rectangle at lower coordinate
          return rect2Max - rect1Min;
        } else if (rect1Max < rect2Min) {
          // complete rectangle at higher coordinate
          return rect2Min - rect1Max;
        } else {
          // intersection of elements
          return 0.0;
        }
      }

      private static YRectangle getBox(final NodeLayout nodeLayout) {
        return new YRectangle(nodeLayout.getX(), nodeLayout.getY(), nodeLayout.getWidth(), nodeLayout.getHeight());
      }

      /**
       * The profit value of all labels (i.e., labelBoxes) that are placed above the given node box (i.e., nodeLayout)
       * lies in [0.5,1]. The profit of the other labels is 0.
       * For all labels above the given node box, the precise value depends on the distance between the label and the
       * node box.
       */
      private static double calcProfit(final YRectangle labelBox, final NodeLayout nodeLayout) {
        if (!isLabelAboveNode(labelBox, nodeLayout)) {
          return 0;
        }

        double profit = 0.5;
        final double dist = calcDistance(labelBox, getBox(nodeLayout));
        if (dist < MAX_DISTANCE_WITH_PROFIT) {
          //we prefer candidates that are close to the given node box
          profit += 0.25 * (MAX_DISTANCE_WITH_PROFIT - dist) / MAX_DISTANCE_WITH_PROFIT;
        }
        final double horizontalCenterDist = calcHorizontalCenterDistance(labelBox, nodeLayout);
        if (horizontalCenterDist < MAX_HORIZONTAL_CENTER_DISTANCE_WITH_PROFIT) {
          //we prefer candidates with small horizontal center offset
          profit += 0.25 * (MAX_HORIZONTAL_CENTER_DISTANCE_WITH_PROFIT - horizontalCenterDist) / MAX_HORIZONTAL_CENTER_DISTANCE_WITH_PROFIT;
        }
        return profit;
      }

      public double getProfit(LabelCandidate candidate) {
        final NodeLayout nodeLayout = (NodeLayout) label2NodeLayout.get(candidate.getOwner());
        return (nodeLayout == null) ? 0 : calcProfit(candidate.getBoundingBox(), nodeLayout);
      }
    }
  }

  /**
   * Does the label placement using the generic labeling algorithm. Before this, the model and size of the labels is
   * set according to the option handlers settings.
   */
  private void doLabelPlacement() {
    // update node label model as well as node label size
    final Graph2D graph = view.getGraph2D();

    //update the label size and the labeling model
    final int labelSize = optionHandler.getInt(LABEL_SIZE_STRING);
    final NodeLabelModel labelingModel = getModel(optionHandler.getEnum(LABELING_MODEL_STRING)); //the model used by the labeling algorithm
    label2Model.clear();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      NodeLabelLayout[] nll = graph.getNodeLabelLayout(nc.node());
      for (int i = 0; i < nll.length; i++) {
        label2Model.put(nll[i], labelingModel);
        ((NodeLabel) nll[i]).setFontSize(labelSize);
      }
    }

    // update the default node realizer
    final NodeRealizer defaultNodeRealizer = graph.getDefaultNodeRealizer();
    final NodeLabel nl = defaultNodeRealizer.getLabel();
    nl.setFontSize(labelSize);
    final SmartNodeLabelModel model = new SmartNodeLabelModel();
    nl.setLabelModel(model, model.getDefaultParameter()); //the model that specifies the dynamic behavior of the label

    new Graph2DLayoutExecutor().doLayout(view, labelLayouter);

    view.updateView();
  }

  /**
   * Creates an option handler with settings for label model and label size.
   */
  private OptionHandler createOptionHandler() {
    final OptionHandler oh = new OptionHandler("Options");
    oh.addEnum(LABELING_MODEL_STRING, NODE_LABEL_MODELS, 2);
    oh.addInt(LABEL_SIZE_STRING, 12, 10, 25);

    OptionGroup og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, PROPERTIES_GROUP);
    og.addItem(oh.getItem(LABELING_MODEL_STRING));
    og.addItem(oh.getItem(LABEL_SIZE_STRING));

    oh.addChildPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        //apply generic labeling after each change
        doLabelPlacement();
      }
    });

    return oh;
  }

  /**
   * Returns the label model for the specified index.
   */
  private static NodeLabelModel getModel(int index) {
    if (index < 0 || index >= NODE_LABEL_MODELS.length) {
      return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.SANDWICH_MASK);
    }

    final String modelString = NODE_LABEL_MODELS[index];
    if (MODEL_CORNERS.equals(modelString)) {
      return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.CORNER_MASK);
    } else if (MODEL_EIGHT_POS.equals(modelString)) {
      return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.EIGHT_POS_MASK);
    } else if (MODEL_FREE.equals(modelString)) {
      return new SmartNodeLabelModel();
    } else if (MODEL_SIDE.equals(modelString)) {
      return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.SIDES_MASK);
    } else {
      return new DiscreteNodeLabelModel(DiscreteNodeLabelModel.SANDWICH_MASK);
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

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    //customize label configuration
    final YLabel.Factory factory = NodeLabel.getFactory();
    final Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(YLabel.Painter.class, new MyPainter());
    factory.addConfiguration("Customized", implementationsMap);

    //set customized configuration as default
    NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
    nodeRealizer.setSize(10.0, 10.0);
    nodeRealizer.getLabel().setText("City");
    nodeRealizer.getLabel().setConfiguration("Customized");
  }

  protected EditMode createEditMode() {
    //configure edit mode
    final EditMode mode = super.createEditMode();
    mode.allowEdgeCreation(false);
    mode.allowMoveSelection(false);
    mode.setSnappingEnabled(false);
    mode.allowResizeNodes(false);
    mode.setPopupMode(new DemoPopupMode());
    return mode;
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(createActionControl(new LayoutAction()));
    return bar;
  }

  /**
   * Loads a graph and applies the label configuration to the existing labels.
   */
  protected void loadGraph(URL resource) {
    super.loadGraph(resource);

    final Graph2D graph2D = view.getGraph2D();
    DemoDefaults.applyRealizerDefaults(graph2D);
    for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
      final NodeLabelLayout[] nll = graph2D.getNodeLabelLayout(nc.node());
      for (int i = 0; i < nll.length; i++) {
        ((NodeLabel) nll[i]).setConfiguration("Customized");
      }
    }
  }

  /**
   * Performs the generic labeling.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Place Labels", SHARED_LAYOUT_ICON);
      putValue(Action.SHORT_DESCRIPTION, "Place labels");
    }

    public void actionPerformed(ActionEvent e) {
      doLabelPlacement();
    }
  }

  /**
   * Customized popup mode.
   */
  class DemoPopupMode extends PopupMode {
    /**
     * Popup menu for a hit node
     */
    public JPopupMenu getNodePopup(Node v) {
      JPopupMenu pm = new JPopupMenu();
      NodeRealizer r = this.view.getGraph2D().getRealizer(v);
      YLabel label = r.getLabel();
      pm.add(new EditLabel(label));
      return pm;
    }

    /**
     * Popup menu for a hit node label
     */
    public JPopupMenu getNodeLabelPopup(NodeLabel label) {
      JPopupMenu pm = new JPopupMenu();      
      pm.add(new EditLabel(label));
      return pm;
    }
  }

  /**
   * Opens a text editor for the specified label.
   */
  class EditLabel extends AbstractAction {
    YLabel label;

    EditLabel(YLabel l) {
      super("Edit Label");
      label = l;
    }

    public void actionPerformed(ActionEvent e) {
      view.openLabelEditor(label, label.getTextLocation().getX(), label.getTextLocation().getY());
    }
  }

  /**
   * A simple YLabel.Painter implementation that reuses most of the default painting behavior from
   * DefaultLabelConfiguration and additionally draws a line between the node and its label.
   */
  static final class MyPainter extends DefaultLabelConfiguration {
    /** Overwrite the painting of the background only. */
    public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {
      super.paintBox(label, gfx, x, y, width, height);
      if (label instanceof NodeLabel) {
        //determine the line connecting the node center with the center of the corresponding node label
        final Node node = ((NodeLabel) label).getNode();
        final Graph2D graph2D = (Graph2D) node.getGraph();
        final LineSegment connectingLine = new LineSegment(new YPoint(x + width * 0.5d, y + height * 0.5d),
            graph2D.getCenter(node));

        //determine start/end point of the line (project the connecting line onto the label/node box)
        final YRectangle labelBox = new YRectangle(x, y, width, height);
        YPoint startPoint = calcBorderIntersectionPoints(labelBox, connectingLine);
        final YRectangle nodeBox = graph2D.getRectangle(node);
        YPoint endPoint = calcBorderIntersectionPoints(nodeBox, connectingLine);

        //draw the line
        if (startPoint != null && endPoint != null) {
          Line2D line = new Line2D.Double(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
          gfx.setColor(new Color(0, 0, 0, 150));
          gfx.draw(line);
        }
      }
    }
  }

  /**
   * Creates a component for the specified option handler using the default editor factory and sets all of its items
   * to auto adopt and auto commit.
   */
  private static JComponent createOptionHandlerComponent(OptionHandler oh) {
    final EditorFactory defaultEditorFactory = new DefaultEditorFactory();
    final Editor editor = defaultEditorFactory.createEditor(oh);

    //propagate auto adopt and auto commit to editor and its children
    final List stack = new ArrayList();
    stack.add(editor);
    while(!stack.isEmpty()) {
      Object editorObj = stack.remove(stack.size() - 1);
      if(editorObj instanceof ItemEditor) {
        ((ItemEditor) editorObj).setAutoAdopt(true);
        ((ItemEditor) editorObj).setAutoCommit(true);
      }
      if(editorObj instanceof CompoundEditor) {
        for (Iterator iter = ((CompoundEditor) editorObj).editors(); iter.hasNext(); ) {
          stack.add(iter.next());
        }
      }
    }

    //build and return component
    JComponent optionComponent = editor.getComponent();
    optionComponent.setMinimumSize(new Dimension(200, 50));
    return optionComponent;
  }

  /**
   * Calculates the intersection point between the given line segment l and the given rectangle r.
   * We assume that at least one endpoint lies inside r -> at most one intersection point.
   */
  private static YPoint calcBorderIntersectionPoints(YRectangle r, LineSegment l) {
    if(!r.contains(l.getFirstEndPoint()) && !r.contains(l.getSecondEndPoint())) {
      throw new RuntimeException("Input no valid!");
    }    

    //check if l intersects a side of r
    final YPoint[] rCorners = new YPoint[4]; 
    rCorners[0] = r.getLocation();
    rCorners[1] = new YPoint(rCorners[0].x, rCorners[0].y + r.getHeight());
    rCorners[2] = new YPoint(rCorners[1].x + r.getWidth(), rCorners[1].y);
    rCorners[3] = new YPoint(rCorners[2].x, rCorners[0].y);
    for(int i = 0; i < rCorners.length; i++) {
      final LineSegment rSide = new LineSegment(rCorners[i], rCorners[(i + 1) % 4]);
      YPoint intersectionPoint = LineSegment.getIntersection(rSide, l);
      if(intersectionPoint != null) {
        return intersectionPoint; //found the intersection
      }
    }
    
    //check special case were l intersects a corner of the rectangle
    for(int i = 0; i < rCorners.length; i++) {
      if(l.intersects(rCorners[i])) {
        return rCorners[i];
      }
    }

    return null; //no intersection
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new NodeLabelingDemo("resource/nodelabelingdemohelp.html")).start("Labeling Demo");
      }
    });
  }
}


      
