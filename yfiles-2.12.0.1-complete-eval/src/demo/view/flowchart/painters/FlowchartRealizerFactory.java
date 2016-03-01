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
package demo.view.flowchart.painters;

import y.view.EdgeLabel;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.EdgeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.Arrow;
import y.view.ShadowNodePainter;


import java.awt.Color;
import java.util.Map;

/**
 * This is a factory for elements conforming to the Flowchart diagrams.
 * <p> Realizers for the different kinds of flowchart elements can be created (see e.g. {@link #createCard()},
 * {@link #createData()}, {@link #createProcess()}...).</p>
 */
public class FlowchartRealizerFactory implements FlowchartRealizerConstants{

  static {

    // Process
    register(FLOWCHART_PROCESS_CONFIG_NAME, new FlowchartProcessPainter());

    // DirectDataPainter
    register(FLOWCHART_DIRECT_DATA_CONFIG_NAME, new FlowchartDirectDataPainter());

    // DataBasePainter
    register(FLOWCHART_DATABASE_CONFIG_NAME, new FlowchartDataBasePainter());

    // DecisionPainter
    register(FLOWCHART_DECISION_CONFIG_NAME, new FlowchartDecisionPainter());

    // DocumentPainter
    register(FLOWCHART_DOCUMENT_CONFIG_NAME, new FlowchartDocumentPainter());

    // DataPainter
    register(FLOWCHART_DATA_CONFIG_NAME, new FlowchartDataPainter());

    // Start1
    register(FLOWCHART_START1_CONFIG_NAME, new FlowchartStart1Painter());

    // Start2
    register(FLOWCHART_START2_CONFIG_NAME, new FlowchartStart2Painter());

    // predefinedProcess
    register(FLOWCHART_PREDEFINED_PROCESS_CONFIG_NAME, new FlowchartPredefinedProcessPainter());

    // Stored Data
    register(FLOWCHART_STORED_DATA_CONFIG_NAME, new FlowchartStoredDataPainter());

    // Internal storage
    register(FLOWCHART_INTERNAL_STORAGE_CONFIG_NAME, new FlowchartInternalStoragePainter());

    // SequentialData
    register(FLOWCHART_SEQUENTIAL_DATA_CONFIG_NAME, new FlowchartSequentialDataPainter());

    // ManualInput
    register(FLOWCHART_MANUAL_INPUT_CONFIG_NAME, new FlowchartManualInputPainter());

    // Card
    register(FLOWCHART_CARD_CONFIG_NAME, new FlowchartCardPainter());

    // Paper tape
    register(FLOWCHART_PAPER_TYPE_CONFIG_NAME, new FlowchartPaperTapePainter());

    // Cloud
    register(FLOWCHART_CLOUD_TYPE_CONFIG_NAME, new FlowchartCloudPainter());

    // Delay
    register(FLOWCHART_DELAY_CONFIG_NAME, new FlowchartDelayPainter());

    // Display
    register(FLOWCHART_DISPLAY_CONFIG_NAME, new FlowchartDisplayPainter());

    // Manual operation
    register(FLOWCHART_MANUAL_OPERATION_CONFIG_NAME, new FlowchartManualOperationPainter());

    // Preparation
    register(FLOWCHART_PREPARATION_CONFIG_NAME, new FlowchartPreparationPainter());

    // Loop limit
    register(FLOWCHART_LOOP_LIMIT_CONFIG_NAME, new FlowchartLoopLimitPainter());

    // Loop limit end
    register(FLOWCHART_LOOP_LIMIT_END_CONFIG_NAME, new FlowchartLoopLimitPainter(false));

    // Terminator
    register(FLOWCHART_TERMINATOR_CONFIG_NAME, new FlowchartTerminatorPainter());

    // On page reference
    register(FLOWCHART_ON_PAGE_REFERENCE_CONFIG_NAME, new FlowchartOnPageReferencePainter());

    // Off page reference
    register(FLOWCHART_OFF_PAGE_REFERENCE_CONFIG_NAME, new FlowchartOffPageReferencePainter());

    // Annotation
    final GenericNodeRealizer.Painter painterImpl = new FlowchartAnnotationPainter();
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(painterImpl));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, painterImpl);
    implementationsMap.put(GenericNodeRealizer.LayerHandler.class, new FlowchartAnnotationLayerHandler());
    factory.addConfiguration(FLOWCHART_ANNOTATION_CONFIG_NAME, implementationsMap);

    // User Message
    register(FLOWCHART_USER_MESSAGE_CONFIG_NAME, new FlowchartMessagePainter(true));

    // Network Message
    register(FLOWCHART_NETWORK_MESSAGE_CONFIG_NAME, new FlowchartMessagePainter(false));
  }

  private FlowchartRealizerFactory() {
  }


  /**
   * Creates a node realizer, that represents a "Direct Data" symbol. The realizer is not bound to a node.
   * @return a flowchart "Direct Data" node realizer.
   */
  public static NodeRealizer createDirectData() {
    return createConfigured(FLOWCHART_DIRECT_DATA_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Data Base" symbol. The realizer is not bound to a node.
   * @return a flowchart "Data Base" node realizer.
   */
  public static NodeRealizer createDataBase() {
    GenericNodeRealizer nodeRealizer = createConfigured(FLOWCHART_DATABASE_CONFIG_NAME);
    nodeRealizer.setSize(60,40);
    return nodeRealizer;
  }

  /**
   * Creates a node realizer, that represents a "Process" symbol. The realizer is not bound to a node.
   * @return a flowchart "Process" node realizer.
   */
  public static NodeRealizer createProcess() {
    return createConfigured(FLOWCHART_PROCESS_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Decision" symbol. The realizer is not bound to a node.
   * @return a flowchart "Decision" node realizer.
   */
  public static NodeRealizer createDecision() {
    return createConfigured(FLOWCHART_DECISION_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Document" symbol. The realizer is not bound to a node.
   * Creates a flowchart "Document" node realizer. The realizer is not bound to a node.
   * @return a flowchart "Document" node realizer.
   */
  public static NodeRealizer createDocument() {
    return createConfigured(FLOWCHART_DOCUMENT_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Data" symbol. The realizer is not bound to a node.
   * @return a flowchart "Data" node realizer.
   */
  public static NodeRealizer createData() {
    return createConfigured(FLOWCHART_DATA_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Start1" symbol. The realizer is not bound to a node.
   * @return a flowchart "Start1" node realizer.
   */
  public static NodeRealizer createStart1() {
    return createConfigured(FLOWCHART_START1_CONFIG_NAME);
  }

  /**
   *  Creates a node realizer, that represents a "Start2" symbol. The realizer is not bound to a node.
   * @return a flowchart "Start2" node realizer.
   */
  public static NodeRealizer createStart2() {
    return createConfigured(FLOWCHART_START2_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Predefined Process" symbol. The realizer is not bound to a node.
   * @return a flowchart "Predefined Process" node realizer.
   */
  public static NodeRealizer createPredefinedProcess() {
    return createConfigured(FLOWCHART_PREDEFINED_PROCESS_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Stored Data" symbol. The realizer is not bound to a node.
   * @return a flowchart "Stored Data" node realizer.
   */
  public static NodeRealizer createStoredData() {
    return createConfigured(FLOWCHART_STORED_DATA_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents an "Internal Storage" symbol. The realizer is not bound to a node.
   * @return a flowchart "Internal Storage" node realizer.
   */
  public static NodeRealizer createInternalStorage() {
    return createConfigured(FLOWCHART_INTERNAL_STORAGE_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Sequential Data" symbol. The realizer is not bound to a node.
   * @return a flowchart "Sequential Data" node realizer.
   */
  public static NodeRealizer createSequentialData() {
    return createConfigured(FLOWCHART_SEQUENTIAL_DATA_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Manual Input" symbol. The realizer is not bound to a node.
   * @return a flowchart "Manual Input" node realizer.
   */
  public static NodeRealizer createManualInput() {
    return createConfigured(FLOWCHART_MANUAL_INPUT_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Card" symbol. The realizer is not bound to a node.
   * @return a flowchart "Card" node realizer.
   */
  public static NodeRealizer createCard() {
    return createConfigured(FLOWCHART_CARD_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Paper Tape" symbol. The realizer is not bound to a node.
   * @return a flowchart "Paper Tape" node realizer.
   */
  public static NodeRealizer createPaperTape() {
    return createConfigured(FLOWCHART_PAPER_TYPE_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Cloud" symbol. The realizer is not bound to a node.
   * @return a flowchart "Cloud" node realizer.
   */
  public static NodeRealizer createCloud() {
    GenericNodeRealizer nodeRealizer = createConfigured(FLOWCHART_CLOUD_TYPE_CONFIG_NAME);
    nodeRealizer.setSize(80,50);
    return nodeRealizer;
  }

  /**
   * Creates a node realizer, that represents a "Delay" symbol. The realizer is not bound to a node.
   * @return a flowchart "Delay" node realizer.
   */
  public static NodeRealizer createDelay() {
    return createConfigured(FLOWCHART_DELAY_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Display" symbol. The realizer is not bound to a node.
   * @return a flowchart "Display" node realizer.
   */
  public static NodeRealizer createDisplay() {
    return createConfigured(FLOWCHART_DISPLAY_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Manual Operation" symbol. The realizer is not bound to a node.
   * @return a flowchart "Manual Operation" node realizer.
   */
  public static NodeRealizer createManualOperation() {
    return createConfigured(FLOWCHART_MANUAL_OPERATION_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Preparation" symbol. The realizer is not bound to a node.
   * @return a flowchart "Preparation" node realizer.
   */
  public static NodeRealizer createPreparation() {
    return createConfigured(FLOWCHART_PREPARATION_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Loop Limit" symbol. The realizer is not bound to a node.
   * @return a flowchart "Loop Limit" node realizer.
   */
  public static NodeRealizer createLoopLimit() {
    return createConfigured(FLOWCHART_LOOP_LIMIT_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Loop Limit End" symbol. The realizer is not bound to a node.
   * @return a flowchart "Loop Limit End" node realizer
   */
  public static NodeRealizer createLoopLimitEnd() {
    return createConfigured(FLOWCHART_LOOP_LIMIT_END_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Terminator" symbol. The realizer is not bound to a node.
   * @return a flowchart "Terminator" node realizer.
   */
  public static NodeRealizer createTerminator() {
    return createConfigured(FLOWCHART_TERMINATOR_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "On Page Reference" symbol. The realizer is not bound to a node.
   * @return a flowchart "On Page Reference" node realizer.
   */
  public static NodeRealizer createOnPageReference() {
    return createConfigured(FLOWCHART_ON_PAGE_REFERENCE_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Off Page Reference" symbol. The realizer is not bound to a node.
   * @return a flowchart "Off Page Reference" node realizer.
   */
  public static NodeRealizer createOffPageReference() {
    return createConfigured(FLOWCHART_OFF_PAGE_REFERENCE_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents an "Annotation" symbol. The realizer is not bound to a node.
   * @param orientation The orientation of the bracket. Possible values:
   * <ul>
   * <li> {@link FlowchartRealizerConstants#PROPERTY_ORIENTATION_VALUE_AUTO} </li>
   * <li> {@link FlowchartRealizerConstants#PROPERTY_ORIENTATION_VALUE_LEFT} </li>
   * <li> {@link FlowchartRealizerConstants#PROPERTY_ORIENTATION_VALUE_RIGHT} </li>
   * <li> {@link FlowchartRealizerConstants#PROPERTY_ORIENTATION_VALUE_TOP} </li>
   * <li> {@link FlowchartRealizerConstants#PROPERTY_ORIENTATION_VALUE_DOWN} </li>
   * </ul>
   * @return a flowchart "Annotation" node realizer.
   */
  public static NodeRealizer createAnnotation(byte orientation) {
    GenericNodeRealizer nodeRealizer = createConfigured(FLOWCHART_ANNOTATION_CONFIG_NAME);
    nodeRealizer.setLineType(LineType.LINE_2);
    nodeRealizer.setStyleProperty(PROPERTY_ORIENTATION, new Byte(orientation));
    return nodeRealizer;
  }

  /**
   * Creates a node realizer, that represents a "User Message" symbol. The realizer is not bound to a node.
   * @return a flowchart "User Message" node realizer.
   */
  public static NodeRealizer createUserMessage(){
    return createConfigured(FLOWCHART_USER_MESSAGE_CONFIG_NAME);
  }

  /**
   * Creates a node realizer, that represents a "Network Message" symbol. The realizer is not bound to a node.
   * @return a flowchart "Network Message" node realizer.
   */
  public static NodeRealizer createNetworkMessage(){
    return createConfigured(FLOWCHART_NETWORK_MESSAGE_CONFIG_NAME);
  }

  /**
   * Creates an edge realizer, that represents a default connection between two flowchart nodes. The realizer is not bound to an edge.
   * @return a flowchart "Default Connection" edge realizer.
   */
  public static EdgeRealizer createDefaultConnection() {
    final PolyLineEdgeRealizer pel = new PolyLineEdgeRealizer();
    pel.setSmoothedBends(true);
    pel.setTargetArrow(Arrow.STANDARD);
    pel.getLabel().setModel(EdgeLabel.SIX_POS);
    pel.getLabel().setPosition(EdgeLabel.STAIL);
    return pel;
  }

  /**
   * Creates an edge realizer, that represents a connection between two flowchart nodes. The connection is labeled as "No". The realizer is not bound to an edge.
   * @return a flowchart "No-Connection" edge realizer.
   */
  public static EdgeRealizer createNoConnection() {
    final PolyLineEdgeRealizer pel = new PolyLineEdgeRealizer();
    pel.setSmoothedBends(true);
    pel.setTargetArrow(Arrow.STANDARD);
    pel.setLabelText("No");
    pel.getLabel().setModel(EdgeLabel.SIX_POS);
    pel.getLabel().setPosition(EdgeLabel.STAIL);
    return pel;
  }

  /**
   * Creates an edge realizer, that represents a connection between two flowchart nodes. The connection is labeled as "Yes". The realizer is not bound to an edge.
   * @return a flowchart "Yes-Connection" edge realizer.
   */
  public static EdgeRealizer createYesConnection() {
    final PolyLineEdgeRealizer pel = new PolyLineEdgeRealizer();
    pel.setSmoothedBends(true);
    pel.setTargetArrow(Arrow.STANDARD);
    pel.setLabelText("Yes");
    pel.getLabel().setModel(EdgeLabel.SIX_POS);
    pel.getLabel().setPosition(EdgeLabel.STAIL);
    return pel;
  }

  /**
   * This method registers a {@link y.view.GenericNodeRealizer.Painter painter} implementation in the configuration map of the {@link y.view.GenericNodeRealizer}
   * @param configName The name of the configuration
   * @param impl The {@link y.view.GenericNodeRealizer.Painter painter} implementation
   */
  private static void register(final String configName, final GenericNodeRealizer.Painter impl) {
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(impl));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, impl);
    factory.addConfiguration(configName, implementationsMap);
  }

  /**
   * Creates a node realizer by given configuration name.
   * @param configName The configuration name.
   * @return A GenericNodeRealizer
   */
  private static GenericNodeRealizer createConfigured(String configName) {
    GenericNodeRealizer nodeRealizer = new GenericNodeRealizer();
    nodeRealizer.setConfiguration(configName);
    nodeRealizer.setFillColor2(new Color(183, 201, 227));
    nodeRealizer.setFillColor(new Color(232, 238, 247));
    nodeRealizer.setSize(80, 40);
    return nodeRealizer;
  }
}
