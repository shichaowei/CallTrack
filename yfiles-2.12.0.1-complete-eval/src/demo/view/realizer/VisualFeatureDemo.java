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
package demo.view.realizer;

import demo.view.DemoBase;
import y.base.Node;
import y.geom.OrientedRectangle;
import y.view.ArcEdgeRealizer;
import y.view.Arrow;
import y.view.Bend;
import y.view.BevelNodePainter;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.Drawable;
import y.view.EdgeLabel;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.ImageNodeRealizer;
import y.view.InterfacePort;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.PolyLineEdgeRealizer;
import y.view.QuadCurveEdgeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.ShinyPlateNodePainter;
import y.view.SmartEdgeLabelModel;
import y.view.SmartNodeLabelModel;
import y.view.YLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Demonstrates visual features and editor behavior.
 * <p/>
 * The following features are demonstrated:
 * <ol>
 * <li>EdgeLabels that display icons and text</li>
 * <li>Rotated Labels</li>
 * <li>Auto rotating EdgeLabels</li>
 * <li>Transparent colors</li>
 * <li>Gradients</li>
 * <li>Bridges for crossing PolyLine Edges</li>
 * <li>InterfacePorts that display icons. (A port defines the logical and visual endpoint of and edge path)</li>
 * <li>In edit mode you can reposition an edge label by pressing on it with the left mouse button and then by dragging
 * the label around. For better orientation some edges provide snap lines with suitable label positions. The snap lines
 * disappear, if the shift key is pressed down. Then the placement of labels at arbitrary positions is possible.</li>
 * <li>In edit mode you can interactively change the offsets of edge ports. Select the edge that should have different
 * ports. A little black dot will appear at the point where the port has its logical location. You can drag the black
 * dot around. If you release the mouse again the offset of the port will be changed.</li>
 * <li>In edit mode you can create an edge that has non-zero port offsets by starting edge creation with the shift key
 * pressed down. The point where you press will become the source port location of the edge. If you have the shift key
 * down when you finish edge creation (by releasing the mouse over a node) that the release point will become the offset
 * of the target port of the edge.</li>
 * </ol>
 */
public class VisualFeatureDemo extends DemoBase {
  private static final String SHINY_NODE_CONFIGURATION = "ShinyNodeConfig";
  private static final String BEVEL_NODE_CONFIGURATION = "BevelNodeConfig";

  public VisualFeatureDemo() {

    final Graph2D graph = view.getGraph2D();


    // show bridges
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(new BridgeCalculator());

    ShapeNodeRealizer defaultNodeRealizer = new ShapeNodeRealizer();
    // configure a drop shadow
    defaultNodeRealizer.setDropShadowColor(new Color(0, 0, 0, 64));
    defaultNodeRealizer.setDropShadowOffsetX((byte) 5);
    defaultNodeRealizer.setDropShadowOffsetY((byte) 5);
    defaultNodeRealizer.setSize(50, 50);
    configureNodeLabel(defaultNodeRealizer.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
    //set to graph as default
    graph.setDefaultNodeRealizer(defaultNodeRealizer);



    //Node 1 to show the line type of the node
    ShapeNodeRealizer node1Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node1Realizer.setCenter(50, 50);
    node1Realizer.setLineColor(Color.BLUE);
    node1Realizer.setLineType(LineType.DASHED_1);
    node1Realizer.setFillColor(Color.ORANGE);
    node1Realizer.setShapeType(ShapeNodeRealizer.DIAMOND);
    final Node node1 = graph.createNode(node1Realizer);


    //Node 2
    ShapeNodeRealizer node2Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node2Realizer.setCenter(250, 50);
    node2Realizer.setLineColor(Color.GRAY);
    node2Realizer.setLineType(LineType.LINE_1);
    node2Realizer.setFillColor2(Color.CYAN);
    node2Realizer.setFillColor(Color.WHITE);
    node2Realizer.setShapeType(ShapeNodeRealizer.DIAMOND);
    final Node node2 = graph.createNode(node2Realizer);


    //Node 3 to show the line type of the node
    ShapeNodeRealizer node3Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node3Realizer.setCenter(400, 50);
    node3Realizer.setLineColor(Color.BLACK);
    node3Realizer.setLineType(LineType.LINE_1);
    node3Realizer.setFillColor(new Color(192, 192, 192, 255));
    node3Realizer.setFillColor2(null);
    node3Realizer.setShapeType(ShapeNodeRealizer.ROUND_RECT);
    final Node node3 = graph.createNode(node3Realizer);

    //Node 4
    ShapeNodeRealizer node4Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node4Realizer.setCenter(600, 50);
    node4Realizer.setLineColor(Color.GRAY);
    node4Realizer.setLineType(LineType.LINE_1);
    node4Realizer.setFillColor(new Color(255, 102, 0, 255));
    node4Realizer.setFillColor2(Color.ORANGE);
    node4Realizer.setShapeType(ShapeNodeRealizer.TRAPEZOID_2);
    final Node node4 = graph.createNode(node4Realizer);

    //Instance of edge realizer that will be default
    PolyLineEdgeRealizer defaultEdgeRealizer = new PolyLineEdgeRealizer();
    defaultEdgeRealizer.setTargetArrow(Arrow.STANDARD);
    graph.setDefaultEdgeRealizer(defaultEdgeRealizer);

    //add an edge between 1 und 2
    PolyLineEdgeRealizer edge1_2Realizer = new PolyLineEdgeRealizer();
    edge1_2Realizer.setLineType(LineType.DASHED_1);
    edge1_2Realizer.setSourceArrow(Arrow.STANDARD);
    edge1_2Realizer.setTargetArrow(Arrow.WHITE_DIAMOND);
    graph.createEdge(node1, node2, edge1_2Realizer);

    //add an edge between 2 und 3
    PolyLineEdgeRealizer edge2_3Realizer = new PolyLineEdgeRealizer();
    edge2_3Realizer.setLineType(LineType.DASHED_DOTTED_2);
    edge2_3Realizer.setSourceArrow(Arrow.NONE);
    edge2_3Realizer.setTargetArrow(Arrow.NONE);
    graph.createEdge(node2, node3,edge2_3Realizer);

    //add an edge between 3 und 4
    //setup source arrow drawable
    Drawable drawable = new Drawable() {
      public void paint(Graphics2D g) {
        Color color = g.getColor();
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.yellow);
        Ellipse2D.Double ellipse = new Ellipse2D.Double(-19, -9, 18, 18);
        g.fill(ellipse);
        g.setColor(Color.orange);
        g.draw(ellipse);
        g.setColor(Color.black);
        GeneralPath path = new GeneralPath();
        path.moveTo(-14, -4);
        path.lineTo(-5, 0);
        path.lineTo(-14, 4);
        g.draw(path);
        g.setStroke(stroke);
        g.setColor(color);
      }

      public Rectangle getBounds() {
        return new Rectangle(-20, -20, 20, 20);
      }
    };

    PolyLineEdgeRealizer edge3_4Realizer = new PolyLineEdgeRealizer();
    edge3_4Realizer.setLineType(LineType.LINE_1);
    edge3_4Realizer.setSourceArrow(Arrow.addCustomArrow("coolArrow", drawable));
    edge3_4Realizer.setTargetArrow(Arrow.DIAMOND);
    Bend bend1 = edge3_4Realizer.createBend(470.0, 70.0, null, Graph2D.AFTER);
    Bend bend2 = edge3_4Realizer.createBend(530.0, 30.0, bend1, Graph2D.AFTER);
    graph.createEdge(node3, node4,edge3_4Realizer);

    //Node 5
    ShapeNodeRealizer node5Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node5Realizer.setCenter(50, 150);
    node5Realizer.setLineColor(Color.BLACK);
    node5Realizer.setLineType(LineType.LINE_1);
    node5Realizer.setFillColor(Color.ORANGE);
    node5Realizer.setFillColor2(null);
    node5Realizer.setShapeType(ShapeNodeRealizer.TRIANGLE);
    final Node node5 = graph.createNode(node5Realizer);

    //Node 6
    ShapeNodeRealizer node6Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node6Realizer.setCenter(250, 150);
    node6Realizer.setLineColor(Color.BLACK);
    node6Realizer.setLineType(LineType.LINE_1);
    node6Realizer.setFillColor(new Color(255, 204, 0, 255));
    node6Realizer.setShapeType(ShapeNodeRealizer.HEXAGON);
    final Node node6 = graph.createNode(node6Realizer);


    //Configure  new node realizers with specific painters
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map configurationMap = factory.createDefaultConfigurationMap();

    // ShinyPlateNodePainter has an option to draw a drop shadow that is more efficient
    // than wrapping it in a ShadowNodePainter.
    ShinyPlateNodePainter shinyPlateNodePainter = new ShinyPlateNodePainter();
    shinyPlateNodePainter.setRadius(10);
    shinyPlateNodePainter.setDrawShadow(true);
    configurationMap.put(GenericNodeRealizer.Painter.class, shinyPlateNodePainter);
    configurationMap.put(GenericNodeRealizer.ContainsTest.class, shinyPlateNodePainter);
    factory.addConfiguration(SHINY_NODE_CONFIGURATION, configurationMap);

    BevelNodePainter bevelNodePainter = new BevelNodePainter();
    bevelNodePainter.setDrawShadow(true);
    configurationMap.put(GenericNodeRealizer.Painter.class, bevelNodePainter);
    configurationMap.put(GenericNodeRealizer.ContainsTest.class, bevelNodePainter);
    factory.addConfiguration(BEVEL_NODE_CONFIGURATION, configurationMap);


    //Node 7
    GenericNodeRealizer gnr_shiny = new GenericNodeRealizer(SHINY_NODE_CONFIGURATION);
    gnr_shiny.setLineColor(new Color(255, 153, 0, 255));
    gnr_shiny.setFillColor(new Color(255, 153, 0, 255));
    gnr_shiny.setSize(50, 50);
    gnr_shiny.setCenter(400, 150);
    configureNodeLabel(gnr_shiny.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
    Node node7 = graph.createNode(gnr_shiny);

    //Node 8
    GenericNodeRealizer gnr_bevel = new GenericNodeRealizer(BEVEL_NODE_CONFIGURATION);
    gnr_bevel.setLineColor(new Color(255, 153, 0, 255));
    gnr_bevel.setFillColor(new Color(255, 153, 0, 255));
    gnr_bevel.setSize(50, 50);
    gnr_bevel.setCenter(600, 150);
    configureNodeLabel(gnr_bevel.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
    Node node8 = graph.createNode(gnr_bevel);


    //add an edge between 1 und 5
    PolyLineEdgeRealizer edge1_5Realizer = new PolyLineEdgeRealizer();
    edge1_5Realizer.setSourceArrow(Arrow.NONE);
    edge1_5Realizer.setTargetArrow(Arrow.SKEWED_DASH);
    graph.createEdge(node1, node5, edge1_5Realizer);

    //add an edge between 5 und 6
    PolyLineEdgeRealizer edge5_6Realizer = new PolyLineEdgeRealizer();
    edge5_6Realizer.setSourceArrow(Arrow.WHITE_DELTA);
    edge5_6Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node5, node6, edge5_6Realizer);

    //add an edge between 6 und 6 (itself)
    PolyLineEdgeRealizer edge6_6Realizer = new PolyLineEdgeRealizer();
    edge6_6Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node6, node6, edge6_6Realizer);

    //add an edge between 6 und 7
    PolyLineEdgeRealizer edge6_7Realizer = new PolyLineEdgeRealizer();
    edge6_7Realizer.setSourceArrow(Arrow.NONE);
    edge6_7Realizer.setTargetArrow(Arrow.NONE);
    graph.createEdge(node6, node7, edge6_7Realizer);

    //add an edge between 7 und 8
    PolyLineEdgeRealizer edge7_8Realizer = new PolyLineEdgeRealizer();
    edge7_8Realizer.setSourceArrow(Arrow.WHITE_DELTA);
    edge7_8Realizer.setTargetArrow(Arrow.WHITE_DIAMOND);
    EdgeLabel labelFor7_8_Edge = edge7_8Realizer.getLabel();
    labelFor7_8_Edge.setText("EDGE LABEL");
    configureEdgeLabel(labelFor7_8_Edge, SmartEdgeLabelModel.POSITION_RIGHT);
    graph.createEdge(node7, node8,edge7_8Realizer);

    //Node 9
    ShapeNodeRealizer node9Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node9Realizer.setCenter(50, 250);
    node9Realizer.setLineColor(Color.BLACK);
    node9Realizer.setLineType(LineType.LINE_1);
    node9Realizer.setFillColor(new Color(153, 204, 255, 255));
    node9Realizer.setFillColor2(null);
    node9Realizer.setShapeType(ShapeNodeRealizer.OCTAGON);
    final Node node9 = graph.createNode(node9Realizer);

    //Node 10 (image node realizer)
    // display an ImageNodeRealizer
    ImageNodeRealizer imageNodeRealizer = new ImageNodeRealizer();
    imageNodeRealizer.setImageURL(getSharedResource("resource/yicon.png"));
    imageNodeRealizer.setAlphaImageUsed(true);
    imageNodeRealizer.setCenter(250, 250);
    imageNodeRealizer.setToImageSize();
    imageNodeRealizer.setSize(40,50);
    configureNodeLabel(imageNodeRealizer.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
    final Node node10 = graph.createNode(imageNodeRealizer);


    // Edge between 9 and 10
    PolyLineEdgeRealizer edge9_10Realizer = new PolyLineEdgeRealizer();
    edge9_10Realizer.setSourceArrow(Arrow.STANDARD);
    // choose smooth bends
    edge9_10Realizer.setSmoothedBends(true);
    // choose a thicker line
    edge9_10Realizer.setLineType(LineType.LINE_2);

    //setup edge label
    EdgeLabel labelForEdge9_10 = edge9_10Realizer.getLabel();
    labelForEdge9_10.setText("rotated edge label");
    configureEdgeLabel(labelForEdge9_10, SmartEdgeLabelModel.POSITION_RIGHT);
    labelForEdge9_10.setRotationAngle(15);
    labelForEdge9_10.setIcon(getIconResource("resource/Start24.png"));

    //setup visual source port
    InterfacePort p = new InterfacePort();
    p.setIcon(getIconResource("resource/Delete24.png"));
    edge9_10Realizer.setSourcePort(p);

    //setup visual target port
    p = new InterfacePort();
    p.setIcon(getIconResource("resource/Properties24.png"));
    edge9_10Realizer.setTargetPort(p);

    //add an edge between 9 und 10
    graph.createEdge(node9, node10, edge9_10Realizer);


    //Node 11
    GenericNodeRealizer gnr_shiny2 = new GenericNodeRealizer(SHINY_NODE_CONFIGURATION);
    gnr_shiny2.setLineColor(new Color(255, 153, 0, 255));
    gnr_shiny2.setFillColor(new Color(255, 153, 0, 255));
    gnr_shiny2.setSize(15, 50);
    gnr_shiny2.setCenter(400, 250);
    configureNodeLabel(gnr_shiny2.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
    Node node11 = graph.createNode(gnr_shiny2);

    //Edge 10 11
    PolyLineEdgeRealizer edge10_11Realizer = new PolyLineEdgeRealizer();
    edge10_11Realizer.setTargetArrow(Arrow.CONVEX);
    graph.createEdge(node10, node11, edge10_11Realizer);

    //Node 12
    ShapeNodeRealizer node12Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node12Realizer.setCenter(600, 250);
    node12Realizer.setLineColor(Color.BLACK);
    node12Realizer.setLineType(LineType.LINE_1);
    node12Realizer.setFillColor(new Color(255, 102, 0, 255));
    node12Realizer.setFillColor2(new Color(255, 153, 0, 255));
    node12Realizer.setShapeType(ShapeNodeRealizer.PARALLELOGRAM);
    final Node node12 = graph.createNode(600, 250);

    //Quadratic curve edge for nodes 11 und 12
    QuadCurveEdgeRealizer quadCurveEdgeRealizer = new QuadCurveEdgeRealizer();
    quadCurveEdgeRealizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node11, node12, quadCurveEdgeRealizer);
    //add symmetrical bends on the edge
    double bendStartX = quadCurveEdgeRealizer.getSourceIntersection().getX();
    double bendEndX = quadCurveEdgeRealizer.getTargetIntersection().getX();
    //calculate the y - middle between source and target to alternate the position of the bends
    double bendStartEndY = (quadCurveEdgeRealizer.getSourceIntersection().getY() + quadCurveEdgeRealizer.getTargetIntersection().getY())/2;
    Bend lastBend = null;
    int numberOfBends = 10;
    double deltaX = (bendEndX - bendStartX) / numberOfBends;
    double x = bendStartX;
    double y = 0;
    for (int i = 0; i < numberOfBends; i++) {
      x = x + deltaX;
      if (i % 2 == 0) {
        y = bendStartEndY - 25;
      } else {
        y = bendStartEndY + 25;
      }
      lastBend = quadCurveEdgeRealizer.createBend(x, y, lastBend, Graph2D.AFTER);
    }

    //Node 13
    // reconfigure the default NodeRealizer
    ShapeNodeRealizer node13Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node13Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node13Realizer.setCenter(50, 350);
    node13Realizer.setFillColor(Color.YELLOW);
    final NodeLabel nodeLabel = node13Realizer.getLabel();
    nodeLabel.setText("<html><b><font color=\"red\">HTML</b><br/>labels!</html>");
    configureNodeLabel(nodeLabel, SmartNodeLabelModel.POSITION_SOUTH);
    Node node13 = graph.createNode(node13Realizer);

    //Node 14
    ShapeNodeRealizer node14Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node14Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node14Realizer.setSize(50, 50);
    node14Realizer.setCenter(250,350);
    node14Realizer.setFillColor(Color.red);
    node14Realizer.setFillColor2(Color.yellow);
    final NodeLabel node14Label = node14Realizer.getLabel();
    node14Label.setFontSize(8);
    node14Label.setText("Transparency! and automatically cropped text for custom label size!.");
    Set configurations = NodeLabel.getFactory().getAvailableConfigurations();
    // set a custom configuration for the label
    if (configurations.contains("CroppingLabel")) {
      node14Label.setConfiguration("CroppingLabel");
      node14Label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      node14Label.setContentSize(40, 40);
    }
    node14Label.setRotationAngle(45);
    node14Label.setBackgroundColor(new Color(255, 255, 255, 128));
    node14Label.setLineColor(Color.GRAY);
    Node node14 = graph.createNode(node14Realizer);

    // add an edge from node 13 to 14
    final ArcEdgeRealizer arcEdgeRealizer = new ArcEdgeRealizer();
    arcEdgeRealizer.setTargetArrow(Arrow.STANDARD);
    arcEdgeRealizer.setLineType(LineType.DOTTED_2);
    EdgeLabel edge13_14Label = arcEdgeRealizer.getLabel();
    edge13_14Label.setText("Arc edge");
    edge13_14Label.setModel(EdgeLabel.FREE);
    graph.createEdge(node14, node13, arcEdgeRealizer);
    final OrientedRectangle labelBounds = edge13_14Label.getOrientedBox();
    final Object newParam = edge13_14Label.getBestModelParameterForBounds(labelBounds.getMovedInstance(-80, 40));
    edge13_14Label.setModelParameter(newParam);


     //Node 15
    ShapeNodeRealizer node15Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node15Realizer.setShapeType(ShapeNodeRealizer.RECT_3D);
    node15Realizer.setSize(50, 50);
    node15Realizer.setCenter(400,350);
    node15Realizer.setFillColor(Color.red);
    node15Realizer.setFillColor2(Color.yellow);
    final NodeLabel node15Label = node15Realizer.getLabel();
    node15Label.setFontSize(8);
    node15Label.setText("Transparency! and automatically cropped text for custom label size!.");
    
    // set a custom configuration for the label
    if (configurations.contains("CroppingLabel")) {
      node15Label.setConfiguration("CroppingLabel");
      node15Label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      node15Label.setContentSize(50, 50);
    }
    node15Label.setRotationAngle(45);
    node15Label.setBackgroundColor(new Color(255, 255, 255, 128));
    node15Label.setLineColor(Color.GRAY);
    Node node15 = graph.createNode(node15Realizer);

    //Node 16
    // reconfigure the default NodeRealizer
    ShapeNodeRealizer node16Realizer = new ShapeNodeRealizer(graph.getDefaultNodeRealizer());
    node16Realizer.setCenter(600, 350);
    node16Realizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
    node16Realizer.setFillColor(Color.WHITE);
    node16Realizer.setLineColor(new Color(255,80,0,255));
    node16Realizer.setLineType(LineType.LINE_4);
    final NodeLabel node16Label = node16Realizer.getLabel();
    node16Label.setText("<html><b>1,2,3...</b></html>");
    Node node16 = graph.createNode(node16Realizer);

    //Edge 14 15
    PolyLineEdgeRealizer edge14_15Realizer = new PolyLineEdgeRealizer();
    edge14_15Realizer.setTargetArrow(Arrow.addCustomArrow("offsetArrow", Arrow.T_SHAPE, 20));
    graph.createEdge(node14, node15, edge14_15Realizer);

    //Edge 15 16
    PolyLineEdgeRealizer edge15_16Realizer = new PolyLineEdgeRealizer();
    edge15_16Realizer.setTargetArrow(Arrow.STANDARD);
    graph.createEdge(node15, node16, edge15_16Realizer);

    //Edge 4 8
    PolyLineEdgeRealizer edge4_8Realizer = new PolyLineEdgeRealizer();
    edge4_8Realizer.setLineType(LineType.DOTTED_2);
    EdgeLabel edge4_8Label = edge4_8Realizer.getLabel();
    edge4_8Label.setText("Dotted");
    configureEdgeLabel(edge4_8Label, SmartEdgeLabelModel.POSITION_LEFT);
    graph.createEdge(node4, node8, edge4_8Realizer);

    // add an edge from node 16 to 12
    final ArcEdgeRealizer arcEdge16_12Realizer = new ArcEdgeRealizer();
    arcEdge16_12Realizer.setTargetArrow(Arrow.STANDARD);
    arcEdge16_12Realizer.setLineType(LineType.DOTTED_1);
    graph.createEdge(node16, node12, arcEdge16_12Realizer);
  }

  private void configureEdgeLabel(EdgeLabel label, int position) {
    final SmartEdgeLabelModel model = new SmartEdgeLabelModel();
    label.setLabelModel(model, model.createDiscreteModelParameter(position));
  }

  private void configureNodeLabel(NodeLabel label, int position) {
    SmartNodeLabelModel model = new SmartNodeLabelModel();
    label.setLabelModel(model, model.createDiscreteModelParameter(position));
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new VisualFeatureDemo()).start("Visual Feature Demo");
      }
    });
  }
}
