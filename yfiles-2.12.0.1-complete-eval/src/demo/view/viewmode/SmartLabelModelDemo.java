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
package demo.view.viewmode;

import demo.view.DemoBase;
import y.base.Edge;
import y.base.Node;
import y.base.YCursor;
import y.base.YList;
import y.geom.OrientedRectangle;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.layout.LabelCandidate;
import y.view.Graph2D;
import y.view.HitInfo;
import y.view.HitInfoFactories;
import y.view.SmartEdgeLabelModel;
import y.view.SmartNodeLabelModel;
import y.option.OptionHandler;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.LabelSnapContext;
import y.view.MoveLabelMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.Map;


/**
 * Demonstrates how to use {@link y.view.SmartNodeLabelModel} and
 * {@link y.view.SmartEdgeLabelModel}.
 * <p>
 * Both models allow for labels to be placed freely at every position.
 * By default, node labels using <code>SmartNodeLabelModel</code> snap to the
 * borders and the center of the node as well as the node's other labels.
 * Edge labels using <code>SmartEdgeLabelModel</code> snap to the edge itself as
 * well as an imaginary snap segment to the left and an imaginary snap segment
 * to the right of the edge.
 * </p>
 * <p>
 * Snapping can be disabled by pressing the control key while dragging a label.
 * </p>
 * <p>
 * Method {@link #createEditMode()} shows how to configure {@link MoveLabelMode}
 * for snapping.
 * </p>
 * @see #createEditMode()
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#cls_MoveLabelMode">Section User Interaction</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizer_related.html#node_label_model">Section Realizer-Related Features: Node Label Models</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizer_related.html#edge_label_model">Section Realizer-Related Features: Edge Label Models</a> in the yFiles for Java Developer's Guide
 */
public class SmartLabelModelDemo extends DemoBase {
  private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
  private static final Color LABEL_BACKGROUND_COLOR = Color.WHITE;
  private static final String AUTO_FLIPPING_CONFIG = "AutoFlipConfig";
  private boolean fractionMetricsForSizeCalculationEnabled;

  public SmartLabelModelDemo() {
    this(null);
  }

  public SmartLabelModelDemo( final String helpFilePath ) {
    loadInitialGraph();
    addHelpPane(helpFilePath);
  }

  /**
   * Loads a sample graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/SmartLabelModelDemo.graphml");
  }

  protected void initialize() {
    super.initialize();
    view.setPreferredSize(new Dimension(700, 650));
    view.setHitInfoFactory(new HitInfoFactories.DefaultHitInfoFactory(view) {
      public HitInfo createHitInfo(
              final double x, final double y,
              final int types,
              final boolean firstHitOnly
      ) {
        return createHitInfo(getView(), getGraph(), x, y, types, firstHitOnly);
      }
    });

    // Ensures that text always fits into label bounds independent of zoom level.
    view.getRenderingHints().put(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    // Store value to be able to reset it when running the demo in the DemoBrowser,
    // so this setting cannot effect other demos.
    fractionMetricsForSizeCalculationEnabled = YLabel.isFractionMetricsForSizeCalculationEnabled();
    YLabel.setFractionMetricsForSizeCalculationEnabled(true);
  }

  /**
   * Cleans up.
   * This method is called by the demo browser when the demo is stopped or another demo starts.
   */
  public void dispose() {
    YLabel.setFractionMetricsForSizeCalculationEnabled(fractionMetricsForSizeCalculationEnabled);
  }

  /**
   * Configures the default realizers to use labels with
   * {@link y.view.SmartNodeLabelModel} and
   * {@link y.view.SmartEdgeLabelModel} as appropriate.
   */
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
    nodeRealizer.setSize(200, 100);
    final NodeLabel nodeLabel = nodeRealizer.getLabel();
    nodeLabel.setText("Smart Node Label");
    nodeLabel.setLineColor(LABEL_LINE_COLOR);
    nodeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
    final SmartNodeLabelModel nodeLabelModel = new SmartNodeLabelModel();
    nodeLabel.setLabelModel(nodeLabelModel, nodeLabelModel.getDefaultParameter());

    final YLabel.Factory factory = EdgeLabel.getFactory();
    final Map defaultConfigImplementationsMap = factory.createDefaultConfigurationMap();
    DefaultLabelConfiguration customLabelConfig = new DefaultLabelConfiguration();
    customLabelConfig.setAutoFlippingEnabled(true);
    defaultConfigImplementationsMap.put(YLabel.Painter.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.Layout.class, customLabelConfig);
    defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class, customLabelConfig);
    factory.addConfiguration(AUTO_FLIPPING_CONFIG, defaultConfigImplementationsMap);

    EdgeRealizer edgeRealizer = view.getGraph2D().getDefaultEdgeRealizer();
    final EdgeLabel edgeLabel = edgeRealizer.getLabel();
    edgeLabel.setText("Smart Edge Label");
    edgeLabel.setLineColor(LABEL_LINE_COLOR);
    edgeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
    final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
    edgeLabel.setLabelModel(edgeLabelModel, edgeLabelModel.createDiscreteModelParameter(SmartEdgeLabelModel.POSITION_CENTER));
    edgeLabel.setConfiguration(AUTO_FLIPPING_CONFIG);
  }

  /**
   * Creates a edit mode that is configured to use snapping for label movements
   * and to provide custom context menus for edge, nodes, and their labels.
   * @return an appropriately configured {@link EditMode} instance.
   */
  protected EditMode createEditMode() {
    final EditMode mode = super.createEditMode();
    mode.setCyclicSelectionEnabled(true);   // To be able to select labels in front of a node
    mode.setPopupMode(new DemoPopupMode());
    final SnappingConfiguration snappingConfiguration = DemoBase.createDefaultSnappingConfiguration();
    snappingConfiguration.configureView(view);
    snappingConfiguration.configureEditMode(mode);

    // configure snap context for label snapping
    final LabelSnapContext snapContext = new LabelSnapContext(view);
    ((MoveLabelMode) mode.getMoveLabelMode()).setSnapContext(snapContext);
    snapContext.setNodeLabelSnapDistance(10);
    snapContext.setEdgeLabelSnapDistance(30);
    snapContext.setSnapLineColor(Color.BLUE);

    return mode;
  }

  /**
   * Launches <code>SmartLabelModelDemo</code>.
   * @param args not used
   */
  public static void main( final String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SmartLabelModelDemo("resource/smartlabelmodeldemo.html")).start();
      }
    });
  }


  /**
   * A PopupMode for adding or editing node labels and edge labels.
   */
  private static final class DemoPopupMode extends PopupMode {
    /**
     * Creates a context menu for nodes that allows for adding labels.
     * @param node the node whose context actions have to be displayed.
     * @return a context menu for nodes that allows for adding labels.
     */
    public JPopupMenu getNodePopup( final Node node ) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Add Label") {
        public void actionPerformed(ActionEvent e) {
          if (node == null) {
            return;
          }

          final SmartNodeLabelModel nodeLabelModel = new SmartNodeLabelModel();
          final NodeLabel label = new NodeLabel("Smart Node Label");
          label.setLineColor(LABEL_LINE_COLOR);
          label.setBackgroundColor(LABEL_BACKGROUND_COLOR);
          label.setLabelModel(nodeLabelModel, nodeLabelModel.getDefaultParameter());
          getGraph2D().getRealizer(node).addLabel(label);
          getGraph2D().updateViews();
        }
      });
      return pm;
    }

    /**
     * Creates a context menu for node labels that allows for editing label
     * properties such as text, font size, and rotation.
     * @param label the label whose context actions have to be displayed.
     * @return a context menu for node labels that allows for editing label
     * properties.
     */
    public JPopupMenu getNodeLabelPopup( final NodeLabel label ) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Edit Label") {
        public void actionPerformed(ActionEvent e) {
          if (label == null) {
            return;
          }

          LabelOptionHandler oh = new LabelOptionHandler(label, getGraph2D());
          oh.showEditor(null, OptionHandler.OK_CANCEL_BUTTONS);
        }
      });
      return pm;
    }

    /**
     * Creates a context menu for edges that allows for adding labels.
     * @param edge the edge whose context actions have to be displayed.
     * @return a context menu for edges that allows for adding labels.
     */
    public JPopupMenu getEdgePopup( final Edge edge ) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Add Label") {
        public void actionPerformed(ActionEvent e) {
          if (edge == null) {
            return;
          }

          final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
          final EdgeLabel label = new EdgeLabel("Smart Edge Label");
          label.setConfiguration(AUTO_FLIPPING_CONFIG);
          label.setLineColor(LABEL_LINE_COLOR);
          label.setBackgroundColor(LABEL_BACKGROUND_COLOR);
          label.setLabelModel(edgeLabelModel, edgeLabelModel.getDefaultParameter());
          getGraph2D().getRealizer(edge).addLabel(label);

          final Object parameter = calculateBestParameter(edge, label);
          if (parameter != null) {
            label.setModelParameter(parameter);
          }
          getGraph2D().updateViews();
        }
      });
      return pm;
    }

    /**
     * Creates a context menu for edge labels that allows for editing label
     * properties such as text, font size, and rotation.
     * @param label the label whose context actions have to be displayed.
     * @return a context menu for edge labels that allows for editing label
     * properties.
     */
    public JPopupMenu getEdgeLabelPopup( final EdgeLabel label ) {
      JPopupMenu pm = new JPopupMenu();
      pm.add(new AbstractAction("Edit Label") {
        public void actionPerformed(ActionEvent e) {
          if (label == null) {
            return;
          }

          LabelOptionHandler oh = new LabelOptionHandler(label, getGraph2D());
          oh.showEditor(null, OptionHandler.OK_CANCEL_BUTTONS);
        }
      });
      return pm;
    }

    /**
     * Determines a model parameter that represents a label position that is not
     * already occupied by another label and lies near the current mouse
     * position.
     * @param e The edge where the label is set
     * @param label The current label layout
     * @return A model parameter with the new label position
     */
    private Object calculateBestParameter( final Edge e, final EdgeLabel label ) {
      if (lastPressEvent == null) {
        return null;
      }

      //identify occupied boxes
      YList occupiedRectList = new YList();
      Graph2D graph = view.getGraph2D();
      EdgeRealizer er = graph.getRealizer(e);
      for (int i = 0, n = er.labelCount(); i < n; ++i) {
        EdgeLabel el = er.getLabel(i);
        if (el != label) {
          occupiedRectList.add(el.getOrientedBox());
        }
      }

      //find label candidates with non-occupied boxes and low distance to point "mousePressedEventLocation"
      final YPoint mousePressedEventLocation = new YPoint(
              view.toWorldCoordX(lastPressEvent.getX()),
              view.toWorldCoordY(lastPressEvent.getY()));
      double minDist = Double.MAX_VALUE;
      LabelCandidate chosen = null;
      YList candidates = label.getLabelModel().getLabelCandidates(
              label, er, er.getSourceRealizer(), er.getTargetRealizer());
      for (YCursor cu = candidates.cursor(); cu.ok(); cu.next()) {
        LabelCandidate lc = (LabelCandidate) cu.current();

        boolean isOccupied = false;
        YRectangle bBox = lc.getBox().getBoundingBox();
        for (YCursor cur = occupiedRectList.cursor(); cur.ok() && !isOccupied; cur.next()) {
          if (OrientedRectangle.intersects((OrientedRectangle) cur.current(), bBox, 0)) {
            isOccupied = true;
            break;
          }
        }

        if (!isOccupied) {
          double dist = YPoint.distance(mousePressedEventLocation, lc.getBox().getCenter());
          if (dist < minDist) {
            minDist = dist;
            chosen = lc;
          }
        }
      }

      return chosen == null ? null : chosen.getModelParameter();
    }
  }

  /**
   * An option handler for editing node or edge labels.
   * It is possible to edit the label text, the font size and
   * the rotation angle.
   */
  private static final class LabelOptionHandler extends OptionHandler {
    static final String LABEL_TEXT = "Label Text";
    static final String FONT_SIZE = "Font Size";
    static final String ROTATION_ANGLE = "Rotation Angle";
    static final String AUTO_ROTATION = "Auto Rotation";

    private final YLabel label;
    private final Graph2D graph;
    private final boolean isEdgeLabel;

    /**
     * Initializes a new <code>LabelOptionHandler</code> for the specified
     * label and view.
     * @param label the label to edit.
     * @param graph the label's associated graph.
     */
    LabelOptionHandler( final YLabel label, final Graph2D graph ) {
      super("Label Options");
      this.label = label;
      this.graph = graph;
      this.isEdgeLabel = label instanceof EdgeLabel;
      addString(LABEL_TEXT, label.getText(), 5);
      addInt(FONT_SIZE, label.getFontSize());
      int value = 0;
      if (isEdgeLabel) {
        final EdgeLabel el = (EdgeLabel) label;
        if (el.getLabelModel() instanceof SmartEdgeLabelModel) {
          // OrientedRectangle interprets angles in counter-clockwise direction
          // but label models and their parameters interpret angles in
          // clockwise direction therefore the parameter angle has to be
          // translated from clockwise to counter-clockwise here
          value = toDegrees(2*Math.PI - SmartEdgeLabelModel.getAngle(el.getModelParameter()));
        }
      } else {
        value = toDegrees(label.getOrientedBox().getAngle());
      }
      while (value < 0) {
        value += 360;
      }
      addInt(ROTATION_ANGLE, value, 0, 360);
      if (isEdgeLabel) {
        final EdgeLabel el = (EdgeLabel) label;
        if (el.getLabelModel() instanceof SmartEdgeLabelModel) {
          SmartEdgeLabelModel model = (SmartEdgeLabelModel) el.getLabelModel();
          addBool(AUTO_ROTATION, model.isAutoRotationEnabled());
        }
      }
    }

    public void commitValues() {
      super.commitValues();
      label.setText(getString(LABEL_TEXT));
      label.setFontSize(getInt(FONT_SIZE));

      final OrientedRectangle box = label.getOrientedBox();
      final YPoint center = box.getCenter();
      box.setAngle(Math.toRadians(getInt(ROTATION_ANGLE)));
      box.setCenter(center);

      if (isEdgeLabel && ((EdgeLabel) label).getLabelModel() instanceof SmartEdgeLabelModel) {
        final EdgeLabel el = (EdgeLabel) label;
        final EdgeRealizer er = el.getRealizer();
        final SmartEdgeLabelModel model = (SmartEdgeLabelModel) el.getLabelModel();
        model.setAutoRotationEnabled(getBool(AUTO_ROTATION));
        el.setModelParameter(model.createRelativeModelParameter(
            box,
            er,
            er.getSourceRealizer(),
            er.getTargetRealizer()));
      } else {
        label.setModelParameter(label.getBestModelParameterForBounds(box));
      }

      graph.updateViews();
    }

    private static int toDegrees( final double angle ) {
      return ((int) Math.rint(Math.toDegrees(angle))) % 360;
    }
  }
}