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
package demo.view.advanced;

import java.awt.Graphics2D;
import java.awt.EventQueue;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

import org.w3c.dom.Element;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Graph;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.YList;
import y.geom.Geom;
import y.geom.YDimension;
import y.geom.YPoint;
import y.geom.YVector;
import y.io.GraphMLIOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.NameBasedDeserializer;
import y.io.graphml.input.ParseEventListenerAdapter;
import y.io.graphml.output.AbstractOutputHandler;
import y.io.graphml.output.GraphElementIdProvider;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.XmlWriter;
import y.util.DataAcceptorAdapter;
import y.util.GraphCopier;
import y.util.Maps;
import y.util.Tuple;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BendList;
import y.view.CreateEdgeMode;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DClipboard;
import y.view.Graph2DTraversal;
import y.view.Graph2DViewActions;
import y.view.HitInfo;
import y.view.MovePortMode;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.view.Port;
import y.view.ShapeNodePainter;

/**
 * Class that shows how to mimic node-to-edge and edge-to-edge connections. In this demo an edge that connects
 * to a node or to another edge is modeled as a normal edge that has a special node as its end point. That special
 * node is located on the path of the edge. When moving the edge path the special node will also be moved. Thus,
 * it looks and feels like a proper edge connection to an edge.
 * <p>
 * Usage: to create an edge that starts at another edge, shift-press on the edge to initiate the
 * edge creation gesture, then drag the mouse. To create an edge that ends at another edge,
 * shift-release the mouse on the edge.
 * </p>
 */
public class EdgeConnectorDemo extends DemoBase {

  /**
   * Create a GenericNodeRealizer configuration for nodes that represent edge connectors
   */
  static {
    Map configurationMap = GenericNodeRealizer.getFactory().createDefaultConfigurationMap();
    ShapeNodePainter painter = new ShapeNodePainter();
    painter.setShapeType(ShapeNodePainter.ELLIPSE);
    configurationMap.put(y.view.GenericNodeRealizer.Painter.class, painter);

    // Size constraint to prevent resizing of edge connectors
    GenericNodeRealizer.GenericSizeConstraintProvider scp = new GenericNodeRealizer.GenericSizeConstraintProvider() {
      public YDimension getMaximumSize(NodeRealizer context) {
        return new YDimension(5,5);
      }

      public YDimension getMinimumSize(NodeRealizer context) {
        return new YDimension(5,5);
      }
    };
    configurationMap.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, scp);
    GenericNodeRealizer.getFactory().addConfiguration("EdgeConnector", configurationMap);
  }

  protected void initialize() {
    super.initialize();
    view.setAntialiasedPainting(true);
    EdgeConnectorGraph2DRenderer r = new EdgeConnectorGraph2DRenderer();
    r.setDrawEdgesFirst(true);
    view.setGraph2DRenderer(r);
    view.getGraph2D().addGraphListener(new EdgeConnectorListener());
    loadGraph("resource/EdgeConnectorDemo.graphml");
  }

  protected void registerViewModes() {
    EditMode editMode = new EdgeConnectorEditMode();
    editMode.setCreateEdgeMode(new CreateEdgeConnectorMode());
    editMode.setMoveSelectionMode(new EdgeConnectorMoveSelectionMode());
    editMode.setMovePortMode(new EdgeConnectorMovePortMode());
    view.addViewMode(editMode);
  }

  /**
   * Special Graph2DRenderer that updates the edge connector locations before graph elements
   * are rendered to the view.
   */
  static class EdgeConnectorGraph2DRenderer extends DefaultGraph2DRenderer {
    public void paint(final Graphics2D gfx, final Graph2D graph) {
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        if(EdgeConnectorManager.isEdgeConnector(n)) {
          updateEdgeConnectorLocation(n);
        }
      }
      super.paint(gfx, graph);
    }

    public void updateEdgeConnectorLocation(Node node) {
      if(node != null) {
        Edge edge = EdgeConnectorManager.getEdgeConnection(node);
        if(edge != null) {
          Graph2D graph = (Graph2D) node.getGraph();
          double ratio = EdgeConnectorManager.getEdgeConnectionRatio(node);
          Point2D point = PointPathProjector.getPointForGlobalRatio(graph.getRealizer(edge), ratio);
          NodeRealizer nr = graph.getRealizer(node);
          nr.setCenter(point.getX(), point.getY());
        }
      }
    }
  }

  /**
   * Create a GraphMLIOHandler that will serialize and deserialize data that is associated with edge connector nodes.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();

    ioHandler.getGraphMLHandler().addOutputHandlerProvider(new AbstractOutputHandler("edgeConnectingData", KeyScope.NODE, KeyType.COMPLEX) {
      protected void writeValueCore(GraphMLWriteContext context, Object data)
          throws GraphMLWriteException {
        if(data != null) {
          Tuple tuple = (Tuple) data;
          Edge edge = (Edge) tuple.o1;
          double ratio = ((Double)tuple.o2).doubleValue();
          XmlWriter writer = context.getWriter();
          GraphElementIdProvider idProvider = (GraphElementIdProvider) context.lookup(GraphElementIdProvider.class);
          String edgeId = idProvider.getEdgeId(edge, context);
          writer.writeStartElement("connectorData", "demo");
          writer.writeAttribute("edgeId", edgeId);
          writer.writeAttribute("ratio", ratio);
          writer.writeEndElement();
        }
      }

      protected Object getValue(GraphMLWriteContext context, Object key)
          throws GraphMLWriteException {
        return EdgeConnectorManager.map.get(key);
      }
    });

    ioHandler.getGraphMLHandler().addOutputHandlerProvider(new AbstractOutputHandler("edgeId", KeyScope.EDGE, KeyType.STRING) {
      protected void writeValueCore(GraphMLWriteContext context, Object data)
          throws GraphMLWriteException {
        if(data != null) {
          XmlWriter writer = context.getWriter();
          writer.writeText(data.toString());
        }
      }

      protected Object getValue(GraphMLWriteContext context, Object key)
          throws GraphMLWriteException {
        GraphElementIdProvider idProvider = (GraphElementIdProvider) context.lookup(GraphElementIdProvider.class);
        return idProvider.getEdgeId((Edge) key, context);
      }
    });

    final DataMap edgeIdMap = Maps.createHashedDataMap();
    ioHandler.getGraphMLHandler().addInputDataAcceptor("edgeId",
        new DataAcceptorAdapter() {
          public void set(Object dataHolder, Object value) {
            edgeIdMap.set(value, dataHolder);
          }
        },
        KeyScope.EDGE, KeyType.STRING);


    final DataMap tempConnectorMap = Maps.createHashedDataMap();

    ioHandler.getGraphMLHandler().addInputDataAcceptor("edgeConnectingData", tempConnectorMap, KeyScope.NODE, new NameBasedDeserializer() {
      public Object deserializeNode(org.w3c.dom.Node xmlNode,
          GraphMLParseContext context) throws GraphMLParseException {
        Element xmlElem = (Element) xmlNode;
        String edgeId = xmlElem.getAttribute("edgeId");
        String doubleStr = xmlElem.getAttribute("ratio");
        return new Tuple(edgeId, doubleStr);
      }

      public String getNodeName(GraphMLParseContext context) {
        return "connectorData";
      }

      public String getNamespaceURI(GraphMLParseContext context) {
        return "demo";
      }
    });

    ioHandler.getGraphMLHandler().addParseEventListener(new ParseEventListenerAdapter() {
      public void onGraphMLParsed(y.io.graphml.input.ParseEvent event) {
        Graph2D graph = (Graph2D) event.getContext().getGraph();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          Node n = nc.node();
          Tuple tuple = (Tuple) tempConnectorMap.get(n);
          if(tuple != null) {
            Edge edge = (Edge) edgeIdMap.get(tuple.o1);
            Double ratio = Double.valueOf(tuple.o2.toString());
            EdgeConnectorManager.map.put(n, new Tuple(edge, ratio));
          }
        }
      }
    });
    return ioHandler;
  }

  /**
   * Overwritten to decorate the clipboard's copy factory with an {@link EdgeConnectorGraphCopyFactory} that also
   * handles copying the edge connector information.
   */
  protected Graph2DClipboard getClipboard() {
    final Graph2DClipboard clipboard = super.getClipboard();
    clipboard.setCopyFactory(new EdgeConnectorGraphCopyFactory(clipboard.getCopyFactory()));
    return clipboard;
  }

  protected void registerViewActions() {
    //register keyboard actions
    super.registerViewActions();
    ActionMap amap = view.getCanvasComponent().getActionMap();
    InputMap imap = view.getCanvasComponent().getInputMap();
    if (!isDeletionEnabled()) {
      amap.remove(Graph2DViewActions.DELETE_SELECTION);
    }
    view.getCanvasComponent().setActionMap(amap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
  }

  /**
   * Manages edge-to-edge dependency information.
   */
  static class EdgeConnectorManager {
    static final Map map = new WeakHashMap();

    private EdgeConnectorManager() {
    }

    static boolean isEdgeConnector(Node n) {
      return map.containsKey(n);
    }

    static void addEdgeConnection(Node connector, Edge edge, double pathRatio) {
      map.put(connector, Tuple.create(edge, new Double(pathRatio)));
    }

    static Edge getEdgeConnection(Node connector) {
      Tuple tuple = (Tuple) map.get(connector);
      if (tuple != null) {
        return (Edge) tuple.o1;
      }
      return null;
    }

    static double getEdgeConnectionRatio(Node connector) {
      Tuple tuple = (Tuple) map.get(connector);
      if (tuple != null) {
        return ((Double) tuple.o2).doubleValue();
      }
      return 0.0;  //should throw an exception
    }

    static NodeList getConnectorNodes(Edge edge) {
      NodeList result = new NodeList();
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry) iter.next();
        Tuple value = (Tuple) entry.getValue();
        if (value.o1 == edge) {
          result.add(entry.getKey());
        }
      }
      return result;
    }

    static NodeRealizer createEdgeConnectorRealizer() {
      GenericNodeRealizer gnr = new GenericNodeRealizer("EdgeConnector");
      gnr.setSize(5,5);
      gnr.setFillColor(DemoDefaults.DEFAULT_CONTRAST_COLOR);
      return gnr;
    }

  }

  /**
   * Graph listener that automatically removes edges that connect to edges that
   * are to be removed.
   * This implementation assumes that all edge removal operations are triggered
   * through user interaction, i.e. that <em>all</em> edge removal events are
   * bracketed in <code>PRE</code> and <code>POST</code> events.
   */
  static class EdgeConnectorListener implements GraphListener {
    /** The current event block */
    private int block;
    /** Stores edges by event block */
    private Map block2edges;
    /** Stores the active/inactive state of this listener */
    private boolean armed;

    EdgeConnectorListener() {
      armed = true;
    }

    public void onGraphEvent(final GraphEvent e) {
      if (!armed) {
        return;
      }

      switch (e.getType()) {
        case GraphEvent.PRE_EVENT:
          ++block;
          break;
        case GraphEvent.POST_EVENT:
          handleBlock();
          --block;
          break;
        case GraphEvent.POST_EDGE_REMOVAL:
          storeForHandleBlock((Edge) e.getData());
          break;
      }
    }

    /**
     * Stores the specified edge for later processing upon completion of the
     * current event block.
     */
    private void storeForHandleBlock( final Edge e ) {
      if (block2edges == null) {
        block2edges = new HashMap();
      }
      final Integer key = new Integer(block);
      EdgeList edges = (EdgeList) block2edges.get(key);
      if (edges == null) {
        edges = new EdgeList();
        block2edges.put(key, edges);
      }
      edges.add(e);
    }

    /**
     * Handles cleanup of the edge-to-edge connection data upon completion
     * of the current event block.
     */
    private void handleBlock() {
      if (block2edges == null) {
        return;
      }

      final EdgeList el = (EdgeList) block2edges.remove(new Integer(block));
      if (el == null) {
        return;
      }

      armed = false;
      handleRecursive(el);
      armed = true;

      if (block2edges.isEmpty()) {
        block2edges = null;
      }
    }

    private void handleRecursive( final EdgeList el ) {
      final EdgeList cascade = new EdgeList();
      for (EdgeCursor ec = el.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        Node node;
        node = edge.source();
        if (EdgeConnectorManager.getEdgeConnection(node) != null) {
          final Graph graph = node.getGraph();
          if (graph != null && node.degree() == 0) {
            graph.removeNode(node);
          }
        }
        node = edge.target();
        if (EdgeConnectorManager.getEdgeConnection(node) != null) {
          final Graph graph = node.getGraph();
          if (graph != null && node.degree() == 0) {
            graph.removeNode(node);
          }
        }
        final NodeList connectors = EdgeConnectorManager.getConnectorNodes(edge);
        if (connectors != null) {
          for (NodeCursor nc = connectors.nodes(); nc.ok(); nc.next()) {
            node = nc.node();
            final Graph graph = node.getGraph();
            if (graph != null) {
              for (EdgeCursor nec = node.edges(); nec.ok(); nec.next()) {
                cascade.add(nec.edge());
              }
              graph.removeNode(node);
            }
          }
        }
      }

      if (!cascade.isEmpty()) {
        handleRecursive(cascade);
      }
    }
  }
//
//  /**
//   * Represents the end point of an edge that connects to another edge. Note that
//   * with this implementation a call to updateLocation enforces that the location
//   * of the node will be on the corresponding edge path. In this demo the call to
//   * updateLocation is performed by the Graph2DRenderer implementation EdgeConnectorGraph2DRenderer.
//   */
//  static class EdgeConnectorRealizer extends ShapeNodeRealizer {
//    public EdgeConnectorRealizer() {
//      setShapeType(ELLIPSE);
//      setSize(5,5);
//      setFillColor(Color.yellow);
//    }
//
//    public EdgeConnectorRealizer(NodeRealizer nr) {
//      super(nr);
//    }
//    public NodeRealizer createCopy(NodeRealizer nr) {
//      return new EdgeConnectorRealizer(nr);
//    }
//
//
////    public void calcUnionRect(Rectangle2D r) {
////      updateLocation();
////      super.calcUnionRect(r);
////    }
//
////    public void paint(Graphics2D gfx) {
////      updateLocation();
////      super.paintNode(gfx);
////    }
//  }

  /**
   * Extends MoveSelectionMode to also handle edge-to-edge connections.
   */
  static class EdgeConnectorMoveSelectionMode extends MoveSelectionMode {
    protected NodeList getNodesToBeMoved() {
      NodeList result = super.getNodesToBeMoved();
      for(NodeCursor nc = result.nodes(); nc.ok(); nc.next()) {
        Node n = nc.node();
        for(EdgeCursor ec = n.edges(); ec.ok(); ec.next()) {
          Edge edge = ec.edge();
          NodeList connectors = EdgeConnectorManager.getConnectorNodes(edge);
          result.splice(connectors);
        }
      }
      BendList bends = getBendsToBeMoved();
      for (BendCursor bc = bends.bends(); bc.ok(); bc.next()) {
        Bend b = bc.bend();
        NodeList connectors = EdgeConnectorManager.getConnectorNodes(b.getEdge());
        result.splice(connectors);
      }
      return result;
    }
  }

  /**
   * Extends CreateEdgeMode to also handle edge-to-edge connections.
   */
  static class CreateEdgeConnectorMode extends CreateEdgeMode {
    private Node startNode;

    public void mousePressedLeft(double x, double y) {
      // fire event to mark start of edge creation for undo/redo
      final Node hitNode = getHitInfo(x, y).getHitNode();
      if (hitNode != null) {
        getGraph2D().firePreEvent();
      }
      super.mousePressedLeft(x, y);
    }

    public void mouseShiftPressedLeft(double x, double y) {
      // fire event to mark start of edge creation for undo/redo
      final Node hitNode = getHitInfo(x, y).getHitNode();
      if (hitNode != null) {
        getGraph2D().firePreEvent();
      }
      if(isEditing()) {
        super.mouseShiftPressedLeft(x,y);
      }
      else {
        Graph2D graph = getGraph2D();
        Edge edge = getHitInfo(x,y).getHitEdge();
        if (edge != null) {
          // fire event to mark start of edge creation for undo/redo
          getGraph2D().firePreEvent();
          NodeRealizer ecNR = EdgeConnectorManager.createEdgeConnectorRealizer();
          Point2D p = new Point2D.Double(x, y);
          double[] result = PointPathProjector.calculateClosestPathPoint(graph.getRealizer(edge).getPath(), p);
          ecNR.setCenter(result[0], result[1]);
          //ecNR.setCenter(x,y);
          startNode = getGraph2D().createNode(ecNR);
          view.updateView();
          super.mouseShiftPressedLeft(result[0], result[1]);
          EdgeConnectorManager.addEdgeConnection(startNode, edge, result[5]);
        }
        else {
          startNode = null;
          super.mouseShiftPressedLeft(x, y);
        }
      }
    }

    public void mouseReleasedLeft(double x, double y) {
      // fire event to mark start of edge creation for undo/redo
      super.mouseReleasedLeft(x, y);
      final Node hitNode = getHitInfo(x, y).getHitNode();
      if (hitNode != null) {
        getGraph2D().firePostEvent();
      }
    }

    public void mouseShiftReleasedLeft(double x, double y) {
      Graph2D graph = getGraph2D();
      Edge edge = getHitInfo(x, y).getHitEdge();
      if (edge != null) {
        NodeRealizer ecNR = EdgeConnectorManager.createEdgeConnectorRealizer();
        Point2D p = new Point2D.Double(x, y);
        double[] result = PointPathProjector.calculateClosestPathPoint(graph.getRealizer(edge).getPath(), p);
        ecNR.setCenter(result[0], result[1]);
        Node endNode = getGraph2D().createNode(ecNR);
        view.updateView();
        super.mouseShiftReleasedLeft(result[0], result[1]);
        EdgeConnectorManager.addEdgeConnection(endNode, edge, result[5]);
        // fire event to mark start of edge creation for undo/redo
        getGraph2D().firePostEvent();
      } else {
        super.mouseShiftReleasedLeft(x, y);
      }
      // fire event to mark start of edge creation for undo/redo
      final Node hitNode = getHitInfo(x, y).getHitNode();
      if (hitNode != null && !EdgeConnectorManager.isEdgeConnector(hitNode)) {
        getGraph2D().firePostEvent();
      }
    }

    public HitInfo getHitInfo(double x, double y) {
      final HitInfo info = view.getHitInfoFactory()
              .createHitInfo(x, y, Graph2DTraversal.ALL, false);
      setLastHitInfo(info);
      return info;
    }

    protected void cancelEdgeCreation() {
      if(startNode != null) {
        final Node tmp = startNode;
        startNode = null;
        getGraph2D().removeNode(tmp);
        // fire event to mark start of edge creation for undo/redo
        getGraph2D().firePostEvent();
      }
      super.cancelEdgeCreation();
    }

    public void setEditing(boolean active) {
      if (!active) {
        startNode = null;
      }
      super.setEditing(active);
    }
  }

  static class EdgeConnectorEditMode extends EditMode {
    public void mouseDraggedLeft(double x, double y) {
      if(isModifierPressed(lastPressEvent)) {
        double px = translateX(lastPressEvent.getX());
        double py = translateY(lastPressEvent.getY());
        Edge edge = getHitInfo(px,py).getHitEdge();
        if(edge != null) {
          setChild(getCreateEdgeMode(), lastPressEvent, lastDragEvent);
          return;
        }
      }
      super.mouseDraggedLeft(x, y);
    }
  }

  /**
   * Special MovePortMode that will allow to move the port of an edge that connects to
   * another edge to be moved along the edge path.
   */
  static class EdgeConnectorMovePortMode extends MovePortMode {

    protected YList getPortCandidates(Node v, Edge e, double gridSpacing) {
      Edge connectedEdge = EdgeConnectorManager.getEdgeConnection(v);
      if(connectedEdge != null) {
        Graph2D graph = getGraph2D();
        //v is a connector point
        YList result = new YList();
        YPoint yport = e.source() == v ? graph.getSourcePointAbs(e) : graph.getTargetPointAbs(e);
        Point2D p = new Point2D.Double(yport.x, yport.y);
        double[] pppResult = PointPathProjector.calculateClosestPathPoint(getGraph2D().getRealizer(connectedEdge).getPath(), p);
        result.add(new YPoint(pppResult[0], pppResult[1]));
        return result;
      }
      return super.getPortCandidates(v,e,gridSpacing);
    }

    public void mouseReleasedLeft(double x, double y) {
      Port p = this.port;
      if(p != null) {
        Edge e = p.getOwner().getEdge();
        Node v = null;
        if(p == p.getOwner().getTargetPort()) {
          v = e.target();
        }
        else {
          v = e.source();
        }
        Edge connectedEdge = EdgeConnectorManager.getEdgeConnection(v);
        if(connectedEdge == null) {
          super.mouseReleasedLeft(x,y);
          return;
        }
        else {
          double[] result = PointPathProjector.calculateClosestPathPoint(getGraph2D().getRealizer(connectedEdge).getPath(),  x, y);
          double ratio = result[5];
          EdgeConnectorManager.addEdgeConnection(v, connectedEdge, ratio);
          super.mouseReleasedLeft(x,y);
          getGraph2D().setCenter(v, result[0], result[1]);
          p.setOffsets(0,0);
        }
        getGraph2D().updateViews();
      }
    }
  }

  /**
   * Helper class that provides diverse services related to working with points on a path.
   */
  public static class PointPathProjector {

    private PointPathProjector() {
    }

    public static double[] calculateClosestPathPoint(GeneralPath path, double px, double py) {
      return calculateClosestPathPoint(path, new Point2D.Double(px,py));
    }

    /**
     * Calculates the point on the path which is closest to the given point.
     * Ties are broken arbitrarily.
     * @param path where to look for the closest point
     * @param p to this point
     * @return double[6]
     * <ul>
     *   <li>x coordinate of the closest point</li>
     *   <li>y coordinate of the closest point</li>
     *   <li>distance of the closest point to given point</li>
     *   <li>index of the segment of the path including the closest point
     *       (as a double starting with 0.0, segments are computed with a
     *       path iterator with flatness 1.0)</li>
     *   <li>ratio of closest point on the the including segment (between 0.0 and 1.0)</li>
     *   <li>ratio of closest point on the entire path (between 0.0 and 1.0)</li>
     * </ul>
     */
    public static double[] calculateClosestPathPoint(GeneralPath path, Point2D p) {
      double[] result = new double[6];
      double px = p.getX();
      double py = p.getY();
      YPoint point = new YPoint(px, py);
      double pathLength = 0;

      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      double[] curSeg = new double[4];
      double minDist;
      if (pi.ok()) {
        curSeg = pi.segment();
        minDist = YPoint.distance(px, py, curSeg[0], curSeg[1]);
        result[0] = curSeg[0];
        result[1] = curSeg[1];
        result[2] = minDist;
        result[3] = 0.0;
        result[4] = 0.0;
        result[5] = 0.0;
      } else {
        // no points in GeneralPath: should not happen in this context
        throw new IllegalStateException("path without any coordinates");
      }

      int segmentIndex = 0;
      double lastPathLength = 0.0;
      do {
        YPoint segmentStart = new YPoint(curSeg[0], curSeg[1]);
        YPoint segmentEnd = new YPoint(curSeg[2], curSeg[3]);
        YVector segmentDirection = new YVector(segmentEnd, segmentStart);
        double segmentLength = segmentDirection.length();
        pathLength += segmentLength;
        segmentDirection.norm();

        YPoint crossing = Geom.calcIntersection(segmentStart, segmentDirection, point, YVector.orthoNormal(segmentDirection));
        YVector crossingVector = new YVector(crossing, segmentStart);

        YVector segmentVector = new YVector(segmentEnd, segmentStart);
        double indexEnd = YVector.scalarProduct(segmentVector, segmentDirection);
        double indexCrossing = YVector.scalarProduct(crossingVector, segmentDirection);

        double dist;
        double segmentRatio;
        YPoint nearestOnSegment;
        if (indexCrossing <= 0.0) {
          dist = YPoint.distance(point, segmentStart);
          nearestOnSegment = segmentStart;
          segmentRatio = 0.0;
        } else if (indexCrossing >= indexEnd) {
          dist = YPoint.distance(point, segmentEnd);
          nearestOnSegment = segmentEnd;
          segmentRatio = 1.0;
        } else {
          dist = YPoint.distance(point, crossing);
          nearestOnSegment = crossing;
          segmentRatio = indexCrossing / indexEnd;
        }

        if (dist < minDist) {
          minDist = dist;
          result[0] = nearestOnSegment.getX();
          result[1] = nearestOnSegment.getY();
          result[2] = minDist;
          result[3] = segmentIndex;
          result[4] = segmentRatio;
          result[5] = segmentLength * segmentRatio + lastPathLength;
        }

        segmentIndex++;
        lastPathLength = pathLength;
        pi.next();
      } while (pi.ok());

      if(pathLength > 0) {
        result[5] = result[5] / pathLength;
      } else {
        result[5] = 0.0;
      }
      return result;
    }

    static Point2D getPointForGlobalRatio(EdgeRealizer er, double globalRatio) {
      GeneralPath path = er.getPath();
      if(globalRatio > 1.0 || globalRatio < 0.0) {
        throw new IllegalArgumentException("globalRatio outside of [0,1]");
      }
      double totalPathLength = getPathLength(path);
      double targetPathLength = totalPathLength * globalRatio;
      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      YPoint segmentStart = null, segmentEnd = null;
      if (pi.isDone()) {
        // no points in GeneralPath: source and target node overlap
        // => set the connector point in the middle between their centers
        return getPointFromEndpoints(er);
      } else {
        segmentStart = pi.segmentStart();
        segmentEnd = pi.segmentEnd();
      }

      double currentPathLength = 0.0;
      double lastPathLength = 0.0;
      while (pi.ok()) {
        YVector segmentDirection = new YVector(segmentEnd, segmentStart);
        double segmentLength = segmentDirection.length();
        currentPathLength += segmentLength;
        if(currentPathLength / totalPathLength >= globalRatio) {
          double remainingLength = targetPathLength - lastPathLength;
          double localRatio = remainingLength / segmentLength;
          segmentDirection.scale(localRatio);
          YPoint targetPoint = YVector.add(segmentStart, segmentDirection);
          return new Point2D.Double(targetPoint.getX(),targetPoint.getY());
        }

        lastPathLength = currentPathLength;
        pi.next();
        segmentStart = pi.segmentStart();
        segmentEnd = pi.segmentEnd();
      }

      // we ran past the last point of the path (numeric problems?), return last point
      return new Point2D.Double(segmentStart.getX(), segmentStart.getY());
    }

    static Point2D getPointForLocalRatio(EdgeRealizer er, int segmentIndex, double segmentRatio) {
      GeneralPath path = er.getPath();
      if (segmentRatio > 1.0 || segmentRatio < 0.0) {
        throw new IllegalArgumentException("segmentRatio outside of [0,1]");
      }
      CustomPathIterator pi = new CustomPathIterator(path, 1.0);
      if (pi.isDone()) {
        // no points in GeneralPath: source and target node overlap
        // => set the connector point in the middle between their centers
        return getPointFromEndpoints(er);
      }
      int currentIndex = 0;
      while (pi.ok() && currentIndex < segmentIndex) {
        pi.next();
        currentIndex++;
      }
      if(currentIndex < segmentIndex)
      {
        throw new IllegalArgumentException("found no segment for given segmentIndex");
      }

      YPoint segmentStart = pi.segmentStart();
      YPoint segmentEnd = pi.segmentEnd();
      YVector segmentDirection = new YVector(segmentEnd, segmentStart);
      segmentDirection.scale(segmentRatio);
      YPoint targetPoint = YVector.add(segmentStart, segmentDirection);
      return new Point2D.Double(targetPoint.getX(), targetPoint.getY());
    }

    private static Point2D getPointFromEndpoints(EdgeRealizer er) {
      final NodeRealizer sourceRealizer = er.getSourceRealizer();
      double sourceX = sourceRealizer.getCenterX();
      double sourceY = sourceRealizer.getCenterY();
      final NodeRealizer targetRealizer = er.getTargetRealizer();
      double targetX = targetRealizer.getCenterX();
      double targetY = targetRealizer.getCenterY();
      return new Point2D.Double((sourceX + targetX) * 0.5, (sourceY + targetY) * 0.5);
    }

    private static double getPathLength(GeneralPath path) {
      double length = 0.0;
      for(CustomPathIterator pi = new CustomPathIterator(path, 1.0); pi.ok(); pi.next()) {
        length += pi.segmentDirection().length();
      }
      return length;
    }
  }

  /**
   * Helper class used by PointPathProjector.
   */
  static class CustomPathIterator {
    private double[] cachedSegment;
    private boolean moreToGet;
    private PathIterator pathIterator;

    public CustomPathIterator(GeneralPath path, double flatness) {
      // copy the path, thus the original may safely change during iteration
      pathIterator = (new GeneralPath(path)).getPathIterator(null, flatness);
      cachedSegment = new double[4];
      getFirstSegment();
    }

    public boolean ok()
    {
      return moreToGet;
    }

    public boolean isDone() {
      return !moreToGet;
    }

    public final double[] segment() {
      if (moreToGet) {
        return cachedSegment;
      } else {
        return null;
      }
    }

    public YPoint segmentStart() {
      if(moreToGet) {
        return new YPoint(cachedSegment[0], cachedSegment[1]);
      } else {
        return null;
      }
    }

    public YPoint segmentEnd() {
      if(moreToGet) {
        return new YPoint(cachedSegment[2], cachedSegment[3]);
      } else {
        return null;
      }
    }

    public YVector segmentDirection() {
      if(moreToGet) {
        return new YVector(segmentEnd(), segmentStart());
      } else {
        return null;
      }
    }

    public void next() {
      if (!pathIterator.isDone()) {
        float[] curSeg = new float[2];
        cachedSegment[0] = cachedSegment[2];
        cachedSegment[1] = cachedSegment[3];
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
      } else {
        moreToGet = false;
      }
    }

    private void getFirstSegment() {
      float[] curSeg = new float[2];
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[0] = curSeg[0];
        cachedSegment[1] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
    }
  }

  /**
   * This {@link GraphCopier.CopyFactory} handles edge connectors for cut/copy/paste.
   */
  private class EdgeConnectorGraphCopyFactory implements GraphCopier.CopyFactory {
    private final GraphCopier.CopyFactory copyFactory;
    private final HashMap node2connector;

    private EdgeConnectorGraphCopyFactory(GraphCopier.CopyFactory copyFactory) {
      this.copyFactory = copyFactory;
      node2connector = new HashMap();
    }

    public Node copyNode(Graph targetGraph, Node originalNode) {
      return copyFactory.copyNode(targetGraph, originalNode);
    }

    public Edge copyEdge(Graph targetGraph, Node newSource, Node newTarget, Edge originalEdge) {
      return copyFactory.copyEdge(targetGraph, newSource, newTarget, originalEdge);
    }

    public Graph createGraph() {
      return copyFactory.createGraph();
    }

    public void preCopyGraphData(Graph sourceGraph, Graph targetGraph) {
      copyFactory.preCopyGraphData(sourceGraph, targetGraph);
    }

    /**
     * After copying the (sub-) graph, also the edge connector information needs to be stored/updated. That way,
     * copies of edge connector nodes still behave like connectors.
     */
    public void postCopyGraphData(Graph sourceGraph, Graph targetGraph, Map nodeMap, Map edgeMap) {
      copyFactory.postCopyGraphData(sourceGraph, targetGraph, nodeMap, edgeMap);

      // check if the source graph is the graph in the current view to see if it is a cut/copy or paste action
      if (sourceGraph == view.getGraph2D()) {
        // cut/copy
        // store the connector information from the source nodes for the nodes in the copied subgraph
        node2connector.clear();
        for (NodeCursor nc = sourceGraph.nodes(); nc.ok(); nc.next()) {
          final Node sourceNode = nc.node();
          if (EdgeConnectorManager.isEdgeConnector(sourceNode)) {
            final Node targetNode = (Node) nodeMap.get(sourceNode);
            if (targetNode != null) {
              final Edge sourceEdge = EdgeConnectorManager.getEdgeConnection(sourceNode);
              final Edge targetEdge = (Edge) edgeMap.get(sourceEdge);
              final double ratio = EdgeConnectorManager.getEdgeConnectionRatio(sourceNode);
              node2connector.put(targetNode, Tuple.create(targetEdge, new Double(ratio)));
            }
          }
        }

        // make sure only edge connectors on existing edges and with incoming and outgoing edges are copied
        for (NodeCursor nc = targetGraph.nodes(); nc.ok(); nc.next()) {
          final Node targetNode = nc.node();
          if (node2connector.containsKey(targetNode)
              && (targetNode.degree() == 0 || ((Tuple) node2connector.get(targetNode)).o1 == null)) {
            targetGraph.removeNode(targetNode);
          }
        }
      } else {
        // paste
        // apply the stored connector information of the copied subgraph to the target nodes in the graph of the view
        for (NodeCursor nc = sourceGraph.nodes(); nc.ok(); nc.next()) {
          final Node sourceNode = nc.node();
          if (node2connector.containsKey(sourceNode)) {
            final Node targetNode = (Node) nodeMap.get(sourceNode);
            final Tuple connector = (Tuple) node2connector.get(sourceNode);
            final Edge sourceEdge = (Edge) connector.o1;
            final Edge targetEdge = (Edge) edgeMap.get(sourceEdge);
            EdgeConnectorManager.addEdgeConnection(targetNode, targetEdge, ((Double) connector.o2).doubleValue());
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new EdgeConnectorDemo()).start();
      }
    });
  }
}
