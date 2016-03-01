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
package demo.view.rendering;

import demo.view.DemoBase;
import y.geom.YRectangle;
import y.view.BevelNodePainter;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.PortConfigurationAdapter;
import y.view.SelectionPortPainter;
import y.view.ShadowNodePainter;
import y.view.ShapeNodeRealizer;
import y.view.ShapePortConfiguration;
import y.view.ShinyPlateNodePainter;
import y.view.YLabel;
import y.view.YRenderingHints;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.GroupNodePainter;
import y.view.hierarchy.HierarchyManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;

/**
 * Demonstrates how to control the level of detail for graph visualization
 * using rendering hints.
 * The following hints can be set (or unset):
 * <ul>
 * <li>{@link YRenderingHints#KEY_GROUP_STATE_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_EDGE_LABEL_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_NODE_LABEL_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_NODE_PORT_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_GRADIENT_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_SELECTION_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_SHADOW_PAINTING}</li>
 * <li>{@link ShapeNodeRealizer#KEY_SLOPPY_RECT_PAINTING}</li>
 * <li>{@link YRenderingHints#KEY_SLOPPY_POLYLINE_PAINTING}</li>
 * </ul>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_view.html#rendering_hints">Section View Implementations</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizers.html#cls_ShapeNodeRealizer">Section Bringing Graph Elements to Life: The Realizer Concept</a> in the yFiles for Java Developer's Guide
 */
public class LevelOfDetailDemo extends DemoBase {
  /**
   * Painter implementation that demonstrates how to query rendering hints.
   * The implementation respects the following rendering hints:
   * <ul>
   * <li>{@link YRenderingHints#KEY_NODE_LABEL_PAINTING}</li>
   * <li>{@link YRenderingHints#KEY_NODE_PORT_PAINTING}</li>
   * <li>{@link YRenderingHints#KEY_GRADIENT_PAINTING}</li>
   * <li>{@link YRenderingHints#KEY_SELECTION_PAINTING}</li>
   * <li>{@link ShapeNodeRealizer#KEY_SLOPPY_RECT_PAINTING}</li>
   * </ul>
   */
  static final class HeaderNodePainter implements GenericNodeRealizer.Painter {
    static final Color BACKGROUND = new Color(153, 204, 255);

    /**
     * Checks whether or not sloppy painting should use "nice" shapes.
     * @param graphics the graphics context to check for rendering hints.
     * @return <code>true</code> if "nice" shapes should be used;
     * <code>false</code> otherwise.
     */
    static boolean isSloppyShapePaintingEnabled( final Graphics2D graphics ) {
      return ShapeNodeRealizer.VALUE_SLOPPY_RECT_PAINTING_OFF.equals(
              graphics.getRenderingHint(ShapeNodeRealizer.KEY_SLOPPY_RECT_PAINTING));
    }

    /**
     * Checks whether or not node ports should be painted.
     * The default behavior is to paint node ports for detail rendering but not
     * for sloppy rendering.
     * @param graphics the graphics context to check for rendering hints.
     * @param sloppy the rendering mode.
     * @return <code>true</code> if node ports should be painted;
     * <code>false</code> otherwise.
     */
    static boolean shouldPaintPorts( final Graphics2D graphics, final boolean sloppy ) {
      if (sloppy) {
        return YRenderingHints.VALUE_NODE_PORT_PAINTING_ON.equals(
                graphics.getRenderingHint(YRenderingHints.KEY_NODE_PORT_PAINTING));
      } else {
        return !YRenderingHints.VALUE_NODE_PORT_PAINTING_OFF.equals(
                graphics.getRenderingHint(YRenderingHints.KEY_NODE_PORT_PAINTING));
      }
    }

    /**
     * Checks whether or not node labels should be painted.
     * The default behavior is to paint node labels for detail rendering but not
     * for sloppy rendering.
     * @param graphics the graphics context to check for rendering hints.
     * @param sloppy the rendering mode.
     * @return <code>true</code> if labels should be painted;
     * <code>false</code> otherwise.
     */
    static boolean shouldPaintLabels( final Graphics2D graphics, final boolean sloppy ) {
      if (sloppy) {
        return YRenderingHints.VALUE_NODE_LABEL_PAINTING_ON.equals(
                graphics.getRenderingHint(YRenderingHints.KEY_NODE_LABEL_PAINTING));
      } else {
        return !YRenderingHints.VALUE_NODE_LABEL_PAINTING_OFF.equals(
                graphics.getRenderingHint(YRenderingHints.KEY_NODE_LABEL_PAINTING));
      }
    }


    public void paint( final NodeRealizer context, final Graphics2D graphics ) {
      paint(context, graphics, false);
    }

    public void paintSloppy( final NodeRealizer context, final Graphics2D graphics ) {
      paint(context, graphics, true);
    }

    /**
     * Paints the node in detail and sloppy rendering mode.
     * @param context the node context.
     * @param graphics the graphics context to paint upon.
     * @param sloppy the rendering mode.
     */
    void paint(
            final NodeRealizer context,
            final Graphics2D graphics,
            final boolean sloppy
    ) {
      if (!context.isVisible()) {
        return;
      }

      if (!sloppy &&
          context.isSelected() &&
          // check whether or not selection state should be respected for
          // rendering
          YRenderingHints.isSelectionPaintingEnabled(graphics)) {
        context.paintHotSpots(graphics);
      }

      final Shape shape = createShape(context, graphics, sloppy);
      paintFilledShape(context, graphics, shape, sloppy);
      paintShapeBorder(context, graphics, shape);

      // check whether or not node ports should be painted
      if (shouldPaintPorts(graphics, sloppy)) {
        context.paintPorts(graphics);
      }

      // check whether or not node labels should be painted
      if (shouldPaintLabels(graphics, sloppy)) {
        context.paintText(graphics);
      }
    }

    /**
     * Creates the shape that represents the node.
     * @param context the node context. Provides geometry for the node's shape.
     * @param graphics the graphics context to check for rendering hints.
     * @param sloppy the rendering mode.
     * @return the shape that represents the node.
     */
    Shape createShape(
            final NodeRealizer context,
            final Graphics2D graphics,
            final boolean sloppy
    ) {
      // check whether or not a "nice" shape should be used for sloppy rendering
      if (sloppy && !isSloppyShapePaintingEnabled(graphics)) {
        // use "simply" shape
        return new Rectangle2D.Double(
                context.getX(), context.getY(),
                context.getWidth(), context.getHeight());
      } else {
        // use "nice" shape
        return new RoundRectangle2D.Double(
                context.getX(), context.getY(),
                context.getWidth(), context.getHeight(),
                8, 8);
      }
    }

    /**
     * Paints the node interior.
     * @param context the node context. Provides color/paint for the node's
     * interior.
     * @param graphics the graphics context to paint upon.
     * @param shape the node's shape.
     * @param sloppy the rendering mode.
     */
    void paintFilledShape(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape,
            final boolean sloppy
    ) {
      if (context.isTransparent()) {
        return;
      }

      // check whether or not gradient paints should be used
      if (sloppy || !YRenderingHints.isGradientPaintingEnabled(graphics)) {
        // use flat colors (no gradient paints)
        final Color fc = context.getFillColor();
        if (fc != null) {
          final Color oldColor = graphics.getColor();

          graphics.setColor(fc);
          graphics.fill(shape);

          graphics.setColor(oldColor);
        }
      } else {
        // use gradient paints
        Color fc1 = context.getFillColor();
        Color fc2 = context.getFillColor2();
        if (fc1 != null || fc2 != null) {
          final Paint oldPaint = graphics.getPaint();

          if (fc1 == null) {
            fc1 = new Color(fc2.getRed(), fc2.getGreen(), fc2.getBlue(), 0);
          }
          if (fc2 == null) {
            fc2 = new Color(fc1.getRed(), fc1.getGreen(), fc1.getBlue(), 0);
          }

          final float y = (float) context.getY();
          final double x = context.getX();
          final double w = context.getWidth();
          graphics.setPaint(new GradientPaint(
                  (float) (x + w * 0.33), y, fc1,
                  (float) (x + w), y, fc2));
          graphics.fill(shape);

          graphics.setPaint(oldPaint);
        }
      }

      // paint the header
      if (context.labelCount() > 0) {
        final Shape oldClip = graphics.getClip();
        final Color oldColor = graphics.getColor();

        final YRectangle r = context.getLabel().getBox();
        if (oldClip == null) {
          final double x = context.getX();
          final double w = context.getWidth();
          final double minX = Math.min(r.x, x);
          final double maxX = Math.max(r.x + r.width, x + w);
          graphics.clip(new Rectangle2D.Double(minX, r.getY(), maxX - minX, r.getHeight()));
        } else {
          final Rectangle2D cb = oldClip.getBounds2D();
          graphics.clip(new Rectangle2D.Double(cb.getX(), r.getY(), cb.getWidth(), r.getHeight()));
        }
        graphics.setColor(BACKGROUND);
        graphics.fill(shape);

        graphics.setColor(oldColor);
        graphics.setClip(oldClip);
      }
    }

    /**
     * Paints the node's border.
     * @param context the node context. Provides color and stroke for the node's
     * border.
     * @param graphics the graphics context to paint upon.
     * @param shape the node's shape.
     */
    void paintShapeBorder(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape
    ) {
      final Color lc = context.getLineColor();
      if (lc != null) {
        final LineType lt = context.getLineType();
        if (lt != null) {
          final Color oldColor = graphics.getColor();
          final Stroke oldStroke = graphics.getStroke();

          graphics.setColor(lc);
          graphics.setStroke(lt);
          graphics.draw(shape);

          graphics.setStroke(oldStroke);
          graphics.setColor(oldColor);
        }
      }
    }
  }



  /**
   * {@link GenericNodeRealizer} configuration name for group nodes.
   */
  private static final String CONFIGURATION_GROUP = "GroupingDemo_GROUP_NODE";
  /**
   * {@link GenericNodeRealizer} configuration name for nodes with a bevel
   * border.
   */
  private static final String CONFIGURATION_BEVEL = "LevelOfDetailDemo#BevelNode";
  /**
   * {@link NodePort} configuration name.
   */
  private static final String CONFIGURATION_PORT = "LevelOfDetailDemo#Port";

  /**
   * {@link y.view.Graph2DView#getPaintDetailThreshold() paintDetailThreshold}
   * value used in the demo
   */
  private static final double PAINT_DETAIL_THRESHOLD = 0.5;

  /**
   * Store this setting for demo use in the DemoBrowser to be able to reset it when
   * stopping the demo or starting the next demo.
   */
  private boolean fractionMetricsForSizeCalculationEnabled;

  public LevelOfDetailDemo() {
    this(null);
  }

  public LevelOfDetailDemo( final String helpFilePath ) {
    configureDefaultGroupNodeRealizers();
    configureDefaultPorts();


    view.setPaintDetailThreshold(PAINT_DETAIL_THRESHOLD);
    view.getRenderingHints().put(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    // Stores the value to be able to reset it when running the demo in the DemoBrowser,
    // so this setting cannot effect other demos.
    fractionMetricsForSizeCalculationEnabled = YLabel.isFractionMetricsForSizeCalculationEnabled();
    YLabel.setFractionMetricsForSizeCalculationEnabled(true);


    // create gui
    final GridBagConstraints gbc = new GridBagConstraints();
    final JPanel controls = new JPanel(new GridBagLayout());
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.weighty = 0;
    controls.add(createOptionPane(), gbc);
    gbc.fill = GridBagConstraints.BOTH;
    ++gbc.gridy;
    gbc.weightx = 1;
    gbc.weighty = 1;
    controls.add(new JPanel(), gbc);

    contentPane.remove(view);
    contentPane.add(new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            controls,
            view), BorderLayout.CENTER);

    addHelpPane(helpFilePath);


    loadInitialGraph();
  }

  protected void initialize() {
    // create hierarchy manager with root graph before undo manager
    // is created (for view actions) to ensure undo/redo works for grouped graphs
    new HierarchyManager(view.getGraph2D());
  }

  /**
   * Cleans up.
   * This method is called by the demo browser when the demo is stopped or another demo starts.
   */
  public void dispose() {
    YLabel.setFractionMetricsForSizeCalculationEnabled(fractionMetricsForSizeCalculationEnabled);
  }

  /**
   * Loads an initial sample graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/LevelOfDetailDemo.graphml");
  }

  /**
   * Creates a control to enable/disable various rendering hints.
   * @return a control to enable/disable various rendering hints.
   */
  private Component createOptionPane() {
    final GridBagConstraints gbc = new GridBagConstraints();
    final JPanel optionPane = new JPanel(new GridBagLayout());
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 10, 0);
    gbc.weightx = 1;
    gbc.weighty = 0;

    // control for coarse grained level of detail
    final StateEditor se = new StateEditor();
    se.controls[0].setText("Depends on zoom");
    se.controls[1].setText("Always detailed");
    se.controls[2].setText("Always sloppy");
    se.setAction(new AbstractAction("Level of Detail") {
      public void actionPerformed( final ActionEvent e ) {
        if (Boolean.TRUE.equals(se.getState())) {
          // always use realizer's paint method
          view.setPaintDetailThreshold(0);
        } else if (Boolean.FALSE.equals(se.getState())) {
          // always use realizer's paintSloppy method
          view.setPaintDetailThreshold(Double.MAX_VALUE);
        } else {
          // if zoom < PAINT_DETAIL_THRESHOLD use paintSloppy
          // else use paint
          view.setPaintDetailThreshold(PAINT_DETAIL_THRESHOLD);
        }
        view.updateView();
      }
    });
    optionPane.add(se, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    ++gbc.gridy;
    optionPane.add(createOnOffDefaultControl(
            YRenderingHints.KEY_GROUP_STATE_PAINTING,
            YRenderingHints.VALUE_GROUP_STATE_PAINTING_ON,
            YRenderingHints.VALUE_GROUP_STATE_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffDefaultControl(
            YRenderingHints.KEY_EDGE_LABEL_PAINTING,
            YRenderingHints.VALUE_EDGE_LABEL_PAINTING_ON,
            YRenderingHints.VALUE_EDGE_LABEL_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffDefaultControl(
            YRenderingHints.KEY_NODE_LABEL_PAINTING,
            YRenderingHints.VALUE_NODE_LABEL_PAINTING_ON,
            YRenderingHints.VALUE_NODE_LABEL_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffDefaultControl(
            YRenderingHints.KEY_NODE_PORT_PAINTING,
            YRenderingHints.VALUE_NODE_PORT_PAINTING_ON,
            YRenderingHints.VALUE_NODE_PORT_PAINTING_OFF
    ), gbc);

    ++gbc.gridy;
    optionPane.add(createOnOffControl(
            YRenderingHints.KEY_GRADIENT_PAINTING,
            YRenderingHints.VALUE_GRADIENT_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffControl(
            YRenderingHints.KEY_SELECTION_PAINTING,
            YRenderingHints.VALUE_SELECTION_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffControl(
            YRenderingHints.KEY_SHADOW_PAINTING,
            YRenderingHints.VALUE_SHADOW_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffControl(
            ShapeNodeRealizer.KEY_SLOPPY_RECT_PAINTING,
            ShapeNodeRealizer.VALUE_SLOPPY_RECT_PAINTING_OFF
    ), gbc);
    ++gbc.gridy;
    optionPane.add(createOnOffControl(
        YRenderingHints.KEY_SLOPPY_POLYLINE_PAINTING,
        YRenderingHints.VALUE_SLOPPY_POLYLINE_PAINTING_OFF
    ), gbc);

    return optionPane;
  }

  /**
   * Creates a control to set one of three states for a rendering hint,
   * <code>on</code>, <code>off</code>, or not used.
   * @param key the rendering hint key to set.
   * @param valueOn the appropriate <code>on</code> value for the rendering
   * hint.
   * @param valueOff the appropriate <code>off</code> value for the rendering
   * hint.
   * @return a control to set one of three states for a rendering hint,
   * <code>on</code>, <code>off</code>, or not used.
   */
  private Component createOnOffDefaultControl(
          final RenderingHints.Key key,
          final Object valueOn,
          final Object valueOff
  ) {
    String name = key.toString();
    if (name.endsWith(" key")) {
      name = name.substring(0, name.length() - 4);
    }
    if (name.endsWith(" enable")) {
      name = name.substring(0, name.length() - 7);
    }

    final StateEditor se = new StateEditor();
    se.setAction(new AbstractAction(name) {
      public void actionPerformed( final ActionEvent e ) {
        final RenderingHints hints = view.getRenderingHints();
        if (Boolean.TRUE.equals(se.getState())) {
          hints.put(key, valueOn);
        } else if (Boolean.FALSE.equals(se.getState())) {
          hints.put(key, valueOff);
        } else {
          hints.remove(key);
        }
        view.updateView();
      }
    });
    return se;
  }

  /**
   * Creates a control to set or unset a rendering hint.
   *
   * @param key the rendering hint key to set.
   * @param value the value to be set for the rendering hint.
   * @return a control to set or unset a rendering hint.
   */
  private JComponent createOnOffControl(
          final RenderingHints.Key key,
          final Object value
  ) {
    String name = key.toString();
    if (name.endsWith(" key")) {
      name = name.substring(0, name.length() - 4);
    }
    if (name.endsWith(" enable")) {
      name = name.substring(0, name.length() - 7);
    }

    final JCheckBox jcb = new JCheckBox();
    jcb.setSelected(true);
    jcb.setAction(new AbstractAction(name) {
      public void actionPerformed( final ActionEvent e ) {
        final RenderingHints hints = view.getRenderingHints();
        if (jcb.isSelected()) {
          hints.remove(key);
        } else {
          hints.put(key, value);
        }
        view.updateView();
      }
    });

    return jcb;
  }

  /**
   * Overwritten to register a configuration for nodes with bevel borders.
   * @see #CONFIGURATION_BEVEL
   */
  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map map = factory.createDefaultConfigurationMap();
    final BevelNodePainter painter = new BevelNodePainter();
    painter.setDrawShadow(true);
    map.put(GenericNodeRealizer.Painter.class, painter);
    factory.addConfiguration(CONFIGURATION_BEVEL, map);
  }

  /**
   * Configures the default group (and folder) nodes.
   * @see #CONFIGURATION_GROUP
   */
  private void configureDefaultGroupNodeRealizers() {
    //Create additional configuration for default group node realizers
    final Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();

    final GroupNodePainter gnp = new GroupNodePainter(new HeaderNodePainter());
    map.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(gnp));
    map.put(GenericNodeRealizer.ContainsTest.class, gnp);
    map.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, gnp);
    map.put(GenericNodeRealizer.Initializer.class, gnp);

    final DefaultGenericAutoBoundsFeature abf = new DefaultGenericAutoBoundsFeature();
    abf.setConsiderNodeLabelSize(true);
    map.put(GenericGroupNodeRealizer.GenericAutoBoundsFeature.class, abf);
    map.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, abf);
    map.put(GenericNodeRealizer.LabelBoundsChangedHandler.class, abf);

    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    factory.addConfiguration(CONFIGURATION_GROUP, map);


    final GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();

    //Register first, since this will also configure the node label
    gnr.setConfiguration(CONFIGURATION_GROUP);

    //Nicer colors
    gnr.setFillColor(new Color(228, 245, 255));
    gnr.setFillColor2(Color.WHITE);
    gnr.setLineColor(new Color(102, 102, 153));
    final NodeLabel label = gnr.getLabel();
    label.setBackgroundColor(null);
    label.setTextColor(Color.BLACK);
    label.setFontSize(15);


    //Set default group and folder node realizers
    final DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory)
            view.getGraph2D().getHierarchyManager().getGraphFactory();

    hgf.setProxyNodeRealizerEnabled(true);

    hgf.setDefaultGroupNodeRealizer(gnr.createCopy());
    hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
  }

  /**
   * Registers a configuration for node ports that are represented by a
   * small blue ball.
   * @see #CONFIGURATION_PORT
   */
  private void configureDefaultPorts() {
    // there is only one very simple dedicated port painter/port contains test
    // implementation available (ShapePortConfiguration)
    // luckily node painters/node contains tests can be reused

    // step 1: create a node configuration to be used as port configuration
    final ShinyPlateNodePainter impl = new ShinyPlateNodePainter();
    impl.setDrawShadow(false);
    final HashMap nodeImpls = new HashMap();
    nodeImpls.put(GenericNodeRealizer.ContainsTest.class, impl);
    nodeImpls.put(GenericNodeRealizer.Painter.class, impl);

    // step 2: create an adapter for the node configuration
    final PortConfigurationAdapter adapter =
            new PortConfigurationAdapter(nodeImpls);
    adapter.setFillColor(new Color(51, 102, 255));
    adapter.setFillColor2(null);
    adapter.setLineColor(new Color(0, 102, 255));

    // step 3: register the adapted node configuration as a port configuration
    final ShapePortConfiguration boundsProvider = new ShapePortConfiguration();
    boundsProvider.setSize(16, 16);

    final NodePort.Factory factory = NodePort.getFactory();
    final Map portImpls = factory.createDefaultConfigurationMap();
    portImpls.put(NodePort.ContainsTest.class, adapter);
    final SelectionPortPainter painter = new SelectionPortPainter(adapter);
    painter.setStyle(SelectionPortPainter.STYLE_SMOOTHED);
    portImpls.put(NodePort.Painter.class, painter);
    portImpls.put(NodePort.BoundsProvider.class, boundsProvider);
    factory.addConfiguration(CONFIGURATION_PORT, portImpls);
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new LevelOfDetailDemo("resource/levelofdetailhelp.html")).start();
      }
    });
  }


  /**
   * An implementation of a tri-state component.
   */
  private static final class StateEditor extends JComponent {
    private final AbstractButton[] controls;
    private Action action;
    private Boolean state;
    private String name;

    StateEditor() {
      setLayout(new GridLayout(3, 1));
      name = "";
      final ButtonGroup group = new ButtonGroup();
      controls = new AbstractButton[3];
      for (int i = 0; i < controls.length; ++i) {
        controls[i] = new JRadioButton();
        group.add(controls[i]);
        add(controls[i]);
      }
      controls[0].setSelected(true);
      controls[0].setAction(new AbstractAction("Default") {
        public void actionPerformed( final ActionEvent e ) {
          state = null;
          fireActionPerformed();
        }
      });
      controls[1].setAction(new AbstractAction("On") {
        public void actionPerformed( final ActionEvent e ) {
          state = Boolean.TRUE;
          fireActionPerformed();
        }
      });
      controls[2].setAction(new AbstractAction("Off") {
        public void actionPerformed( final ActionEvent e ) {
          state = Boolean.FALSE;
          fireActionPerformed();
        }
      });
      setBorder(BorderFactory.createTitledBorder(""));
    }

    /**
     * Returns the state of the component.
     * @return either {@link Boolean#TRUE}, {@link Boolean#FALSE}, or
     * <code>null</code>.
     */
    public Boolean getState() {
      return state;
    }

    /**
     * Sets the state of the component.
     * @param state either {@link Boolean#TRUE}, {@link Boolean#FALSE}, or
     * <code>null</code>.
     */
    public void setState( final Boolean state ) {
      if (state == null) {
        if (this.state != state) {
          controls[0].setSelected(true);
        }
      } else if (Boolean.TRUE == state) {
        if (this.state != state) {
          controls[1].setSelected(true);
        }
      } else if (Boolean.FALSE == state) {
        if (this.state != state) {
          controls[2].setSelected(true);
        }
      }
    }

    /**
     * Returns the currently set <code>Action</code> that is triggered upon
     * state changes or <code>null</code> if no <code>Action</code> is set.
     * @return the currently set <code>Action</code> that is triggered upon
     * state changes or <code>null</code> if no <code>Action</code> is set.
     */
    public Action getAction() {
      return action;
    }

    /**
     * Sets the <code>Action</code> that is triggered upon state changes.
     * @param action the <code>Action</code> that is triggered upon state
     * changes.
     */
    public void setAction( final Action action ) {
      this.action = action;
      String name = "";
      if (action != null) {
        final Object value = action.getValue(Action.NAME);
        if (value instanceof String) {
          name = (String) value;
        }
      }
      setBorder(BorderFactory.createTitledBorder(name));
      this.name = name;
    }

    private void fireActionPerformed() {
      final Action action = this.action;
      if (action != null) {
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name));
      }
    }
  }
}
