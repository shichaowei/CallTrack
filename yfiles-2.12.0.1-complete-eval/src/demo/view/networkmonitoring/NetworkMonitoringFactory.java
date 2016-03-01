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
package demo.view.networkmonitoring;

import demo.view.DemoBase;
import y.layout.DiscreteNodeLabelModel;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.SimpleUserDataHandler;
import y.view.SmartEdgeLabelModel;
import y.view.YLabel;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.GeneralPath;
import java.net.URL;
import java.util.Map;

/**
 * Factory class that provides realizer configurations for network nodes and connections.
 * Also provides color constants and a warning icon.
 */
public class NetworkMonitoringFactory {
  private static final String CONFIGURATION_WORKSTATION = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_WORKSTATION";
  private static final String CONFIGURATION_LAPTOP = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_LAPTOP";
  private static final String CONFIGURATION_SMARTPHONE = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_SMARTPHONE";
  private static final String CONFIGURATION_SWITCH = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_SWITCH";
  private static final String CONFIGURATION_WLAN = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_WLAN";
  private static final String CONFIGURATION_DATABASE = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_DATABASE";
  private static final String CONFIGURATION_SERVER = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_SERVER";
  private static final String CONFIGURATION_CONNECTION = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_CONNECTION";
  private static final String CONFIGURATION_NODE_INFO_LABEL = "demo.view.networkmonitoring.NetworkModelImpl.CONFIGURATION_NODE_INFO_LABEL";

  static final Color COLOR_DISABLED = Color.GRAY;
  static final Color COLOR_BROKEN = Color.DARK_GRAY;
  static final Color COLOR_WORKLOAD_FULL = new Color(255, 51, 0);
  static final Color COLOR_WORKLOAD_HALF = new Color(255, 204, 0);
  static final Color COLOR_WORKLOAD_NONE = new Color(0, 153, 0);
  static final Color COLOR_BACKGROUND_INFO_LABEL = new Color(113, 200, 255);
  static final Icon WARNING_ICON = new WarningIcon(20);

  static final double SQRT_2 = Math.sqrt(2);

  static final String[] NODE_TYPES = {
      "workstation",
      "laptop",
      "smartphone",
      "switch",
      "wlan",
      "database",
      "server"
  };
  static final String[] NODE_CONFIGURATIONS = {
      CONFIGURATION_WORKSTATION,
      CONFIGURATION_LAPTOP,
      CONFIGURATION_SMARTPHONE,
      CONFIGURATION_SWITCH,
      CONFIGURATION_WLAN,
      CONFIGURATION_DATABASE,
      CONFIGURATION_SERVER
  };

  static {
    // register different types of configurations for network nodes
    for (int i = 0; i < NODE_TYPES.length; i++) {
      final GenericNodeRealizer.Factory nodeFactory = GenericNodeRealizer.getFactory();
      Map configMap = nodeFactory.createDefaultConfigurationMap();
      URL enabledImage = DemoBase.getResource(NetworkMonitoringDemo.class, "resource/" + NODE_TYPES[i] + "_on.png");
      URL disableImage = DemoBase.getResource(NetworkMonitoringDemo.class, "resource/" + NODE_TYPES[i] + "_off.png");
      NetworkNodePainter configuration = new NetworkNodePainter(enabledImage, disableImage);
      configuration.setAlphaImageUsed(true);
      configMap.put(GenericNodeRealizer.Painter.class, configuration);
      configMap.put(GenericNodeRealizer.ContainsTest.class, configuration);
      configMap.put(GenericNodeRealizer.UserDataHandler.class,
          new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
      nodeFactory.addConfiguration(NODE_CONFIGURATIONS[i], configMap);
    }

    // register configuration for network connections
    final GenericEdgeRealizer.Factory edgeConfigFactory = GenericEdgeRealizer.getFactory();
    final Map edgeImplMap = edgeConfigFactory.createDefaultConfigurationMap();
    edgeImplMap.put(GenericEdgeRealizer.Painter.class, new NetworkConnectionPainter());
    edgeImplMap.put(GenericEdgeRealizer.UserDataHandler.class, new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    edgeConfigFactory.addConfiguration(CONFIGURATION_CONNECTION, edgeImplMap);

    // register configuration for info labels
    final YLabel.Factory labelFactory = NodeLabel.getFactory();
    final Map labelConfigMap = labelFactory.createDefaultConfigurationMap();
    final NetworkInfoLabelPainter infoLabelPainter = new NetworkInfoLabelPainter();
    labelConfigMap.put(YLabel.Painter.class, infoLabelPainter);
    labelConfigMap.put(YLabel.Layout.class, infoLabelPainter);
    labelFactory.addConfiguration(CONFIGURATION_NODE_INFO_LABEL, labelConfigMap);
  }

  /**
   * Creates a configured node realizer that represents a workstation.
   */
  public static NodeRealizer createWorkstation() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_WORKSTATION);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a laptop.
   */
  public static NodeRealizer createLaptop() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_LAPTOP);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a smartphone.
   */
  public static NodeRealizer createSmartphone() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_SMARTPHONE);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a switch.
   */
  public static NodeRealizer createSwitch() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_SWITCH);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a w-lan router.
   */
  public static NodeRealizer createWLan() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_WLAN);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a database.
   */
  public static NodeRealizer createDatabase() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_DATABASE);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a server.
   */
  public static NodeRealizer createServer() {
    final GenericNodeRealizer realizer = createNodeRealizer();
    realizer.setConfiguration(CONFIGURATION_SERVER);
    return realizer;
  }

  /**
   * Creates a configured node realizer that represents a network node.
   */
  private static GenericNodeRealizer createNodeRealizer() {
    final GenericNodeRealizer realizer = new GenericNodeRealizer();
    realizer.setUserData(new NetworkData(0));
    realizer.setSize(64, 64);

    final NodeLabel infoLabel = realizer.getLabel();
    infoLabel.setText("text");
    infoLabel.setAlignment(YLabel.ALIGN_LEFT);
    infoLabel.setInsets(new Insets(5, 5, 5, 5));
    infoLabel.setBackgroundColor(COLOR_BACKGROUND_INFO_LABEL);
    final DiscreteNodeLabelModel model = new DiscreteNodeLabelModel(DiscreteNodeLabelModel.EIGHT_POS_MASK);
    model.setDistance(20);
    infoLabel.setLabelModel(model, model.getDefaultParameter());
    infoLabel.setConfiguration(CONFIGURATION_NODE_INFO_LABEL);
    infoLabel.setVisible(false);
    return realizer;
  }

  /**
   * Creates a configured edge realizer that represents a network connection.
   */
  public static EdgeRealizer createConnection() {
    final GenericEdgeRealizer realizer = new GenericEdgeRealizer();
    realizer.setConfiguration(CONFIGURATION_CONNECTION);
    realizer.setUserData(new NetworkData(0));
    final EdgeLabel label = realizer.getLabel();
    label.setIcon(getWarningSign());
    SmartEdgeLabelModel model = new SmartEdgeLabelModel();
    label.setLabelModel(model, model.createDiscreteModelParameter(SmartEdgeLabelModel.POSITION_CENTER));
    label.setVisible(false);
    return realizer;
  }

  /**
   * Returns the {@link NetworkData} instance that belongs to the given edge realizer.
   */
  public static NetworkData getNetworkData(final EdgeRealizer context) {
    if (context instanceof GenericEdgeRealizer) {
      final GenericEdgeRealizer realizer = (GenericEdgeRealizer) context;
      final Object userData = realizer.getUserData();
      if (userData instanceof NetworkData) {
        return (NetworkData) userData;
      }
    }
    return null;
  }

  /**
   * Returns the {@link NetworkData} instance that belongs to the given node realizer.
   */
  public static NetworkData getNetworkData(final NodeRealizer context) {
    if (context instanceof GenericNodeRealizer) {
      final GenericNodeRealizer realizer = (GenericNodeRealizer) context;
      final Object userData = realizer.getUserData();
      if (userData instanceof NetworkData) {
        return (NetworkData) userData;
      }
    }
    return null;
  }

  /**
   * Returns a color that encodes the current status of the given network data.
   */
  static Color getStatusColor(NetworkData data) {
    final Color displayColor;
    if (data.isBroken()) {
      return COLOR_BROKEN;
    } else if (data.isDisabled()) {
      return COLOR_DISABLED;
    } else {
      final double workload = data.getWorkload();
      if (workload < 0.5) {
        // the according color between green and yellow
        final int red = (int) ((1 - 2 * workload) * COLOR_WORKLOAD_NONE.getRed() + 2 * workload * COLOR_WORKLOAD_HALF.getRed());
        final int green = (int) ((1 - 2 * workload) * COLOR_WORKLOAD_NONE.getGreen() + 2 * workload * COLOR_WORKLOAD_HALF.getGreen());
        final int blue = (int) ((1 - 2 * workload) * COLOR_WORKLOAD_NONE.getBlue() + 2 * workload * COLOR_WORKLOAD_HALF.getBlue());
        displayColor = new Color(red, green, blue);
      } else {
        // the according color between yellow and red
        final int red = (int) ((2 - 2 * workload) * COLOR_WORKLOAD_HALF.getRed() + (2 * workload - 1) * COLOR_WORKLOAD_FULL.getRed());
        final int green = (int) ((2 - 2 * workload) * COLOR_WORKLOAD_HALF.getGreen() + (2 * workload - 1) * COLOR_WORKLOAD_FULL.getGreen());
        final int blue = (int) ((2 - 2 * workload) * COLOR_WORKLOAD_HALF.getBlue() + (2 * workload - 1) * COLOR_WORKLOAD_FULL.getBlue());
        displayColor = new Color(red, green, blue);
      }
      return displayColor;
    }
  }

  /**
   * Paints a warning sign.
   *
   * @param x        the x-coordinate of the upper left corner of the signs bounding box in world-coordinates.
   * @param y        the y-coordinate of the upper left corner of the signs bounding box in world-coordinates.
   * @param width    the width of the signs bounding box.
   * @param height   the height of the signs bounding box.
   * @param graphics the current graphics context.
   */
  public static void paintWarningSign(double x, double y, double width, double height, Graphics2D graphics) {
    final double a = height / (SQRT_2 + 1);
    final double b = (height / (SQRT_2 + 1)) / SQRT_2;

    // paint octagon
    final GeneralPath sign = new GeneralPath();
    sign.moveTo((float) (x + width / 2 - a / 2), (float) y);
    sign.lineTo((float) (x + width / 2 + a / 2), (float) y);
    sign.lineTo((float) (x + width / 2 + a / 2 + b), (float) (y + b));
    sign.lineTo((float) (x + width / 2 + a / 2 + b), (float) (y + b + a));
    sign.lineTo((float) (x + width / 2 + a / 2), (float) (y + height));
    sign.lineTo((float) (x + width / 2 - a / 2), (float) (y + height));
    sign.lineTo((float) (x + width / 2 - a / 2 - b), (float) (y + b + a));
    sign.lineTo((float) (x + width / 2 - a / 2 - b), (float) (y + b));
    sign.closePath();

    graphics.setColor(Color.RED);
    graphics.fill(sign);

    // paint upper part of exclamation point
    sign.reset();
    sign.moveTo((float) (x + width / 2 - a / 4), (float) (y + height * 0.1));
    sign.lineTo((float) (x + width / 2 + a / 4), (float) (y + height * 0.1));
    sign.lineTo((float) (x + width / 2 + b / 4), (float) (y + height * 0.7));
    sign.lineTo((float) (x + width / 2 - b / 4), (float) (y + height * 0.7));
    sign.closePath();

    graphics.setColor(Color.WHITE);
    graphics.fill(sign);

    // paint lower part of exclamation point
    sign.reset();
    sign.moveTo((float) (x + width / 2 - b / 4), (float) (y + height * 0.85));
    sign.quadTo((float) (x + width / 2 - b / 4), (float) (y + height * 0.85 - b / 4),
        (float) (x + width / 2), (float) (y + height * 0.85 - b / 4));
    sign.quadTo((float) (x + width / 2 + b / 4), (float) (y + height * 0.85 - b / 4),
        (float) (x + width / 2 + b / 4), (float) (y + height * 0.85));
    sign.quadTo((float) (x + width / 2 + b / 4), (float) (y + height * 0.85 + b / 4),
        (float) (x + width / 2), (float) (y + height * 0.85 + b / 4));
    sign.quadTo((float) (x + width / 2 - b / 4), (float) (y + height * 0.85 + b / 4),
        (float) (x + width / 2 - b / 4), (float) (y + height * 0.85));
    graphics.fill(sign);
  }

  /**
   * Returns an icon that shows a warning sign.
   */
  public static Icon getWarningSign() {
    return WARNING_ICON;
  }

  /**
   * An {@link Icon} that shows a warning sign (exclamation point on an octagonal area).
   */
  private static class WarningIcon implements Icon {
    private final int sideLength;

    /**
     * Creates an instance of a warning sign.
     *
     * @param sideLength width or height of the quadratic base area.
     */
    public WarningIcon(int sideLength) {
      this.sideLength = sideLength;
    }

    /**
     * Returns the height of the icon.
     */
    public int getIconHeight() {
      return sideLength;
    }

    /**
     * Returns the width of the icon.
     */
    public int getIconWidth() {
      return sideLength;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      final Graphics2D graphics = (Graphics2D) g.create();
      paintWarningSign(x, y, getIconWidth(), getIconHeight(), graphics);
      graphics.dispose();
    }
  }
}
