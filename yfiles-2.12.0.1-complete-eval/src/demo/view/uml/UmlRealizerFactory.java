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
package demo.view.uml;

import y.geom.YPoint;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.NodeScaledPortLocationModel;
import y.view.PolyLineEdgeRealizer;
import y.view.ShapePortConfiguration;
import y.view.SimpleUserDataHandler;
import y.view.YLabel;

import java.awt.Color;
import java.util.Map;

/**
 * This is a factory for uml class elements conforming to the UML diagrams.
 */
class UmlRealizerFactory {

  /** The name of the node configuration which represents an uml class. */
  private static final String CLASS_CONFIG_NAME = "com.yworks.umlDiagram.class";

  /** The name of the label configuration of an uml class. */
  public static final String LABEL_CONFIG_NAME = "com.yworks.umlDiagram.label";

  /** The name of the dummy node port configuration for edge animation. */
  private static final String DUMMY_NODE_PORT = "com.yworks.umlDiagram.dummyNodePort";

  /** The name of a style property used to check on which button the mouse currently is. */
  private static final String PROPERTY_MOUSE_OVER_BUTTON = "com.yworks.umlDiagram.style.mouseOverButton";

  /** Constant that is used to notify that the mouse is currently on no button. */
  public static final int BUTTON_NONE = -1;

  /** Constant that is used to notify that the mouse is currently on the button that opens/closes the attribute section. */
  public static final int BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION = 0;

  /** Constant that is used to notify that the mouse is currently on the button that opens/closes the operation section. */
  public static final int BUTTON_OPEN_CLOSE_OPERATION_SECTION = 1;

  /** Constant that is used to notify that the mouse is currently on the button that opens/closes the class details. */
  public static final int BUTTON_OPEN_CLOSE_CLASS_SECTIONS = 2;

  /** Constant that is used to notify that the mouse is currently on the button that adds an attribute. */
  public static final int BUTTON_ADD_ATTRIBUTE = 3;

  /** Constant that is used to notify that the mouse is currently on the button that adds an operation. */
  public static final int BUTTON_ADD_OPERATION = 4;

  /** Constant that is used to notify that the mouse is currently on the button that removes an attribute. */
  public static final int BUTTON_SUB_ATTRIBUTE = 5;

  /** Constant that is used to notify that the mouse is currently on the button that removes an operation. */
  public static final int BUTTON_SUB_OPERATION = 6;

  /** The name of a style property used to store the opacity of the attribute buttons. */
  private static final String PROPERTY_ATTRIBUTE_BUTTON_OPACITY = "com.yworks.umlDiagram.style.attributeButtonOpacity";

  /** The name of a style property used to store the opacity of the operation buttons. */
  private static final String PROPERTY_OPERATION_BUTTON_OPACITY = "com.yworks.umlDiagram.style.operationButtonOpacity";

  /** The name of a style property used to store the opacity of the selected item. */
  private static final String PROPERTY_SELECTION_OPACITY = "com.yworks.umlDiagram.style.selectionOpacity";

  /** The name of a style property used to store the opacity of the node. */
  private static final String PROPERTY_NODE_OPACITY = "com.yworks.umlDiagram.style.nodeOpacity";

  /** Constant that is used to specify that the currently selected item belongs to the attribute list. */
  public static final int LIST_ATTRIBUTES =  0;

  /** Constant that is used to specify that the currently selected item belongs to the operation list. */
  public static final int LIST_OPERATIONS =  1;

  /** Constant that is used to specify the alpha value of an opaque color. */
  public static final float OPAQUE = 1.0f;

  /** Constant that is used to specify the alpha value of an semi transparent color. */
  public static final float TRANSPARENT = 0.5f;

  private static final Color COLOR_GREEN = new Color(34,139,34);
  private static final Color COLOR_YELLOW = new Color(213, 255, 179);

  /** Color that is used to paint foreground areas of the class realizers. */
  public static final Color COLOR_FOREGROUND = COLOR_GREEN;

  /** Color that is used to paint background areas of the class realizers. */
  public static final Color COLOR_BACKGROUND = Color.WHITE;

  /** Color that is used to paint selected areas like button or list items. */
  public static final Color COLOR_SELECTION = COLOR_YELLOW;

  /** Color that is used to paint the background of buttons where the mouse is over. */
  public static final Color COLOR_BUTTON_BACKGROUND_ACTIVE = COLOR_YELLOW;

  /** Color that is used to paint the background of buttons where the mouse is not over. */
  public static final Color COLOR_BUTTON_BACKGROUND_BLANK = Color.WHITE;

  /** Color that is used to paint the foreground of buttons that are enabled. */
  public static final Color COLOR_BUTTON_FOREGROUND_ENABLED = COLOR_GREEN;

  /** Color that is used to paint the foreground of buttons that are disabled. */
  public static final Color COLOR_BUTTON_FOREGROUND_DISABLED = Color.LIGHT_GRAY;

  /** Line type for the outline of the edge creation buttons. */
  public static final LineType LINE_EDGE_CREATION_BUTTON_OUTLINE = LineType.LINE_2;

  /** Line type for edges that specify relation between classes. */
  public static final LineType LINE_TYPE_RELATION = LineType.LINE_1;

  /** Line type for edges that specify inheritance between classes. */
  public static final LineType LINE_TYPE_INHERITANCE = LineType.DASHED_1;

  /** Color that is used to paint edges. */
  public static final Color COLOR_EDGE = Color.DARK_GRAY;

  static {
    registerUmlClassConfiguration();
    registerUmlClassLabelConfiguration();
    registerDummyNodePortConfiguration();
  }

  private UmlRealizerFactory() {
  }

  private static void registerUmlClassConfiguration() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map implementations = factory.createDefaultConfigurationMap();
    final GenericNodeRealizer.Painter configuration = new UmlClassConfiguration();
    implementations.put(GenericNodeRealizer.Painter.class, configuration);
    implementations.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, configuration);
    implementations.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, configuration);
    implementations.put(GenericNodeRealizer.UserDataHandler.class, new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    factory.addConfiguration(CLASS_CONFIG_NAME, implementations);
  }

  private static void registerUmlClassLabelConfiguration() {
    final YLabel.Factory factory = NodeLabel.getFactory();
    final Map implementations = factory.createDefaultConfigurationMap();
    implementations.put(YLabel.Painter.class, new UmlClassLabelPainter());
    factory.addConfiguration(LABEL_CONFIG_NAME, implementations);
  }

  private static void registerDummyNodePortConfiguration() {
    final ShapePortConfiguration configuration = new ShapePortConfiguration();
    configuration.setSize(1, 1);
    final NodePort.Factory factory = NodePort.getFactory();
    final Map map = factory.createDefaultConfigurationMap();
    map.put(NodePort.Painter.class, configuration);
    map.put(NodePort.IntersectionTest.class, configuration);
    map.put(NodePort.ContainsTest.class, configuration);
    map.put(NodePort.BoundsProvider.class, configuration);
    map.put(NodePort.UnionRectCalculator.class, configuration);
    factory.addConfiguration(DUMMY_NODE_PORT, map);
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a class in UML. This realizer has a name label, an attribute
   * and operation caption and a list of attributes and operation labels.
   *
   * @return an UML class realizer
   * @see #CLASS_CONFIG_NAME
   */
  public static NodeRealizer createClassRealizer() {
    final GenericNodeRealizer realizer = new GenericNodeRealizer();
    realizer.setConfiguration(CLASS_CONFIG_NAME);
    realizer.setUserData(new UmlClassModel());
    realizer.setFillColor(COLOR_BACKGROUND);
    realizer.setFillColor2(COLOR_FOREGROUND);
    realizer.setLineColor(COLOR_SELECTION);
    realizer.setLineType(LineType.LINE_2);
    setAttributeButtonOpacity(realizer, 0);
    setOperationButtonOpacity(realizer, 0);
    setSelectionOpacity(realizer, 0);
    UmlClassLabelSupport.updateAllLabels(realizer);
    UmlClassLabelSupport.updateRealizerSize(realizer);
    return realizer;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents an association as defined in UML 2.0.
   */
  public static EdgeRealizer createAssociationRealizer() {
    final EdgeRealizer association = new PolyLineEdgeRealizer();
    association.setLineColor(COLOR_EDGE);
    association.setLineType(LINE_TYPE_RELATION);
    association.setSourceArrow(Arrow.NONE);
    association.setTargetArrow(Arrow.NONE);
    return association;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents a dependency as defined in UML 2.0.
   */
  public static EdgeRealizer createDependencyRealizer() {
    final EdgeRealizer dependency = new PolyLineEdgeRealizer();
    dependency.setLineColor(COLOR_EDGE);
    dependency.setLineType(LINE_TYPE_INHERITANCE);
    dependency.setSourceArrow(Arrow.NONE);
    dependency.setTargetArrow(Arrow.PLAIN);
    return dependency;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents a generalization as defined in UML 2.0.
   */
  public static EdgeRealizer createGeneralizationRealizer() {
    final EdgeRealizer generalization = new PolyLineEdgeRealizer();
    generalization.setLineColor(COLOR_EDGE);
    generalization.setLineType(LINE_TYPE_RELATION);
    generalization.setSourceArrow(Arrow.NONE);
    generalization.setTargetArrow(Arrow.WHITE_DELTA);
    return generalization;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents a realization as defined in UML 2.0.
   */
  public static EdgeRealizer createRealizationRealizer() {
    final EdgeRealizer realization = new PolyLineEdgeRealizer();
    realization.setLineColor(COLOR_EDGE);
    realization.setLineType(LINE_TYPE_INHERITANCE);
    realization.setSourceArrow(Arrow.NONE);
    realization.setTargetArrow(Arrow.WHITE_DELTA);
    return realization;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents an aggregation as defined in UML 2.0.
   */
  public static EdgeRealizer createAggregationRealizer() {
    final EdgeRealizer aggregation = new PolyLineEdgeRealizer();
    aggregation.setLineColor(COLOR_EDGE);
    aggregation.setLineType(LINE_TYPE_RELATION);
    aggregation.setSourceArrow(Arrow.WHITE_DIAMOND);
    aggregation.setTargetArrow(Arrow.NONE);
    return aggregation;
  }

  /**
   * Creates a {@link EdgeRealizer} that represents a composition as defined in UML 2.0.
   */
  public static EdgeRealizer createCompositionRealizer() {
    final EdgeRealizer composition = new PolyLineEdgeRealizer();
    composition.setLineColor(COLOR_EDGE);
    composition.setLineType(LINE_TYPE_RELATION);
    composition.setSourceArrow(Arrow.DIAMOND);
    composition.setTargetArrow(Arrow.NONE);
    return composition;
  }

  /**
   * Creates a {@link NodePort} that has minimal size.
   */
  public static NodePort createDummyNodePort(NodeRealizer currentRealizer, YPoint portLocation) {
    final NodePort port = new NodePort();
    port.setModelParameter(new NodeScaledPortLocationModel().createParameter(currentRealizer, portLocation));
    port.setConfiguration(DUMMY_NODE_PORT);
    return port;
  }

  /**
   * Checks whether or not the given {@link y.view.EdgeRealizer} visualizes a UML realization.
   */
  static boolean isRealization(EdgeRealizer er) {
    return er.getTargetArrow() == Arrow.WHITE_DELTA && er.getLineType() == LINE_TYPE_RELATION;
  }

  /**
   * Checks whether or not the given {@link y.view.EdgeRealizer} visualizes a UML realization or generalization.
   */
  static boolean isInheritance(EdgeRealizer er) {
    return er.getTargetArrow() == Arrow.WHITE_DELTA;
  }


  /**
   * Returns the button which the mouse is currently over. There are the following buttons:
   * <ul>
   *   <li>{@link #BUTTON_NONE}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_OPERATION_SECTION}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_CLASS_SECTIONS}</li>
   *   <li>{@link #BUTTON_ADD_ATTRIBUTE}</li>
   *   <li>{@link #BUTTON_ADD_OPERATION}</li>
   *   <li>{@link #BUTTON_SUB_ATTRIBUTE}</li>
   *   <li>{@link #BUTTON_SUB_OPERATION}</li>
   * </ul>
   */
  public static int getMouseOverButton(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    final Object button = gnr.getStyleProperty(PROPERTY_MOUSE_OVER_BUTTON);
    return button != null ? ((Integer)button).intValue() : BUTTON_NONE;
  }

  /**
   * Specifies the button which the mouse is currently over. There are the following buttons:
   * <ul>
   *   <li>{@link #BUTTON_NONE}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_OPERATION_SECTION}</li>
   *   <li>{@link #BUTTON_OPEN_CLOSE_CLASS_SECTIONS}</li>
   *   <li>{@link #BUTTON_ADD_ATTRIBUTE}</li>
   *   <li>{@link #BUTTON_ADD_OPERATION}</li>
   *   <li>{@link #BUTTON_SUB_ATTRIBUTE}</li>
   *   <li>{@link #BUTTON_SUB_OPERATION}</li>
   * </ul>
   */
  public static void setMouseOverButton(final NodeRealizer context, final int button) {
    switch (button) {
      case BUTTON_NONE:
      case BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION:
      case BUTTON_OPEN_CLOSE_OPERATION_SECTION:
      case BUTTON_OPEN_CLOSE_CLASS_SECTIONS:
      case BUTTON_ADD_ATTRIBUTE:
      case BUTTON_ADD_OPERATION:
      case BUTTON_SUB_ATTRIBUTE:
      case BUTTON_SUB_OPERATION:
        final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
        gnr.setStyleProperty(PROPERTY_MOUSE_OVER_BUTTON, new Integer(button));
        return;
      default:
        throw new IllegalArgumentException("Unknown button " + button);
    }
  }

  /**
   * Returns the opacity of the attribute buttons.
   */
  public static float getAttributeButtonOpacity(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    final Object opacity = gnr.getStyleProperty(PROPERTY_ATTRIBUTE_BUTTON_OPACITY);
    return opacity != null ? ((Float)opacity).floatValue() : OPAQUE;
  }

  /**
   * Specifies the opacity of the attribute buttons.
   */
  public static void setAttributeButtonOpacity(final NodeRealizer context, final float opacity) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    gnr.setStyleProperty(PROPERTY_ATTRIBUTE_BUTTON_OPACITY, new Float(opacity));
  }

  /**
   * Returns the opacity of the operation buttons.
   */
  public static float getOperationButtonOpacity(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    final Object opacity = gnr.getStyleProperty(PROPERTY_OPERATION_BUTTON_OPACITY);
    return opacity != null ? ((Float)opacity).floatValue() : OPAQUE;
  }

  /**
   * Specifies the opacity of the operation buttons.
   */
  public static void setOperationButtonOpacity(final NodeRealizer context, final float opacity) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    gnr.setStyleProperty(PROPERTY_OPERATION_BUTTON_OPACITY, new Float(opacity));
  }

  /**
   * Returns the opacity of the item selection.
   */
  public static float getSelectionOpacity(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    final Object opacity = gnr.getStyleProperty(PROPERTY_SELECTION_OPACITY);
    return opacity != null ? ((Float)opacity).floatValue() : OPAQUE;
  }

  /**
   * Specifies the opacity of the item selection.
   */
  public static void setSelectionOpacity(final NodeRealizer context, final float opacity) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    gnr.setStyleProperty(PROPERTY_SELECTION_OPACITY, new Float(opacity));
  }

  /**
   * Returns the opacity of the realizer.
   */
  public static float getNodeOpacity(final NodeRealizer context) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    final Object opacity = gnr.getStyleProperty(PROPERTY_NODE_OPACITY);
    return opacity != null ? ((Float) opacity).floatValue() : OPAQUE;
  }

  /**
   * Specifies the opacity of the realizer.
   */
  public static void setNodeOpacity(final NodeRealizer context, final float opacity) {
    final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
    gnr.setStyleProperty(PROPERTY_NODE_OPACITY, new Float(opacity));
  }
}
