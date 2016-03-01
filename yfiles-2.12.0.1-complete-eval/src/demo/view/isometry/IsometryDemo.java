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
package demo.view.isometry;

import demo.view.DemoBase;
import org.w3c.dom.Element;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.base.NodeList;
import y.geom.YPoint;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.io.graphml.NamespaceConstants;
import y.io.graphml.graph2d.EdgeLabelDeserializer;
import y.io.graphml.graph2d.EdgeLabelSerializer;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.io.graphml.graph2d.NodeLabelDeserializer;
import y.io.graphml.graph2d.NodeLabelSerializer;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.XPathUtils;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.XmlWriter;
import y.layout.FixNodeLayoutStage;
import y.layout.LabelLayoutTranslator;
import y.layout.LayoutGraph;
import y.layout.LayoutTool;
import y.layout.Layouter;
import y.layout.NodeLayout;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.util.D;
import y.util.DataProviderAdapter;
import y.view.DefaultBackgroundRenderer;
import y.view.DefaultOrderRenderer;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.HitInfo;
import y.view.NavigationMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.NodeStateChangeHandler;
import y.view.ProxyShapeNodeRealizer;
import y.view.ViewMode;
import y.view.YLabel;
import y.view.YRenderingHints;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * This demo displays graphs in an isometric fashion to create an impression of a 3-dimensional view.
 * <p>
 *   It shows how to:
 *   <ul>
 *    <li>
 *      create a layout stage ({@link IsometryTransformationLayoutStage}) that transforms the graph into a layout space
 *      before layout and retransforms it into view space afterwards. So any {@link Layouter} can be used to
 *      calculate the layout which is then transformed into an isometric view.
 *    </li>
 *    <li>write custom label configurations that display the labels isometrically transformed.</li>
 *    <li>write custom node painter that use custom user data with 3D-information.</li>
 *    <li>adjust the rendering order to paint objects that are further away behind.</li>
 *   </ul>
 * </p>
 */
public class IsometryDemo extends DemoBase {
  private static final double PAINT_DETAIL_THRESHOLD = 0.4;
  private static final double MAX_ZOOM = 4.0;
  private static final double MIN_ZOOM = 0.05;

  private static final int TYPE_INCREMENTAL_HIERARCHIC_LAYOUT = 0;
  private static final int TYPE_ORTHOGONAL_GROUP_LAYOUT = 1;

  private int layoutType;
  private boolean fractionMetricsEnabled;

  private IncrementalHierarchicLayouter ihl;
  private OrthogonalGroupLayouter ogl;

  public IsometryDemo() {
    this(null);
  }

  public IsometryDemo(String helpFile) {
    addHelpPane(helpFile);

    final Graph2D graph = view.getGraph2D();
    new HierarchyManager(graph);

    // add data provider to make transformation data stored in the user data of the realizers available during layout
    graph.addDataProvider(
        IsometryTransformationLayoutStage.TRANSFORMATION_DATA_DPKEY,
        new TransformationDataProvider(graph));

    // as the edge labels shall be painted at a different point in the rendering order than the according edges, edge
    // label painting is disabled for edges and the graph renderer will deal with that
    view.getRenderingHints().put(YRenderingHints.KEY_EDGE_LABEL_PAINTING, YRenderingHints.VALUE_EDGE_LABEL_PAINTING_OFF);
    final IsometryGraphTraversal graphTraversal = new IsometryGraphTraversal();
    view.setGraph2DRenderer(new DefaultOrderRenderer(graphTraversal, graphTraversal) {
      public void paint(Graphics2D gfx, Graph2D graph) {
        Rectangle clip = gfx.getClipBounds();
        if (clip == null) {
          clip = graph.getBoundingBox();
        }

        final Graph2DTraversal paintOrder = getPaintOrder();
        final int types = Graph2DTraversal.NODES | Graph2DTraversal.EDGES | Graph2DTraversal.EDGE_LABELS;
        for (Iterator it = paintOrder.firstToLast(graph, types); it.hasNext();) {
          final Object element = it.next();
          if (element instanceof Edge) {
            final EdgeRealizer er = graph.getRealizer((Edge) element);
            if (intersects(er, clip)) {
              er.paint(gfx);
            }
          } else if (element instanceof Node) {
            final NodeRealizer nr = graph.getRealizer((Node) element);
            if (intersects(nr, clip)) {
              nr.paint(gfx);
            }
          } else if (element instanceof EdgeLabel) {
            final EdgeLabel label = (EdgeLabel) element;
            if (label.intersects(clip.getX(), clip.getY(), clip.getWidth(), clip.getHeight())) {
              label.paint(gfx);
            }
          }
        }
      }
    });

    IsometryRealizerFactory.initializeConfigurations();
    configureLayouter();
    configureBackgroundRenderer();
    configureLabelRendering();
    configureZoomThreshold();

    loadGraph("resource/iso_sample_1.graphml");
  }

  /**
   * Creates and configures the two possible layouters ({@link IncrementalHierarchicLayouter hierarchic} and
   * {@link OrthogonalGroupLayouter orthogonal}).
   */
  private void configureLayouter() {
    ihl = new IncrementalHierarchicLayouter();
    ihl.setOrthogonallyRouted(true);
    ihl.setNodeToEdgeDistance(50);
    ihl.setMinimumLayerDistance(40);
    ihl.setLabelLayouterEnabled(false);
    ihl.setIntegratedEdgeLabelingEnabled(true);
    ihl.setConsiderNodeLabelsEnabled(true);

    // this label layout translator does nothing because the TransformationLayoutStage prepares the labels for layout
    // but OrthogonalGroupLayouter needs a label layout translator for integrated edge labeling and node label consideration
    final LabelLayoutTranslator llt = new LabelLayoutTranslator() {
      public void doLayout(LayoutGraph graph) {
        final Layouter coreLayouter = getCoreLayouter();
        if (coreLayouter != null) {
          coreLayouter.doLayout(graph);
        }
      }
    };

    ogl = new OrthogonalGroupLayouter();
    ogl.setIntegratedEdgeLabelingEnabled(true);
    ogl.setConsiderNodeLabelsEnabled(true);
    ogl.setLabelLayouter(llt);
  }

  /**
   * Adds a isometric grid as demo background.
   */
  private void configureBackgroundRenderer() {
    final DefaultBackgroundRenderer bgRenderer = new FoggyFrameBackgroundRenderer(view);
    bgRenderer.setImageResource(getResource("resource/grid.png"));
    bgRenderer.setMode(DefaultBackgroundRenderer.CENTERED);
    bgRenderer.setColor(Color.WHITE);
    view.setBackgroundRenderer(bgRenderer);
  }

  /**
   * Ensures that text always fits into label bounds independent of zoom level. Stores the value to be able to reset it
   * when running the demo in the DemoBrowser, so this setting cannot effect other demos.
   */
  private void configureLabelRendering() {
    fractionMetricsEnabled = YLabel.isFractionMetricsForSizeCalculationEnabled();
    YLabel.setFractionMetricsForSizeCalculationEnabled(true);
    view.getRenderingHints().put(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  /**
   * Cleans up.
   * This method is called by the demo browser when the demo is stopped or another demo starts.
   */
  public void dispose() {
    YLabel.setFractionMetricsForSizeCalculationEnabled(fractionMetricsEnabled);
  }

  /**
   * Limits the range of possible zoom factors.
   */
  private void configureZoomThreshold() {
    // set threshold for sloppy painting
    view.setPaintDetailThreshold(PAINT_DETAIL_THRESHOLD);
    // limit zooming in and out
    view.getCanvasComponent().addPropertyChangeListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            if ("Zoom".equals(evt.getPropertyName())) {
              final double zoom = ((Double) evt.getNewValue()).doubleValue();
              if (zoom > MAX_ZOOM) {
                view.setZoom(MAX_ZOOM);
              } else if (zoom < MIN_ZOOM) {
                view.setZoom(MIN_ZOOM);
              }
            }
          }
        });
  }

  /**
   * Overwritten to replace all realizers and configurations after graph loading and before layout.
   */
  protected void loadGraph(URL resource) {
    if (resource == null) {
      String message = "Resource \"" + resource + "\" not found in classpath";
      D.showError(message);
      throw new RuntimeException(message);
    }

    try {
      IOHandler ioh = createGraphMLIOHandler();
      view.getGraph2D().clear();
      ioh.read(view.getGraph2D(), resource);
    } catch (IOException e) {
      String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
      D.bug(message);
      throw new RuntimeException(message, e);
    }

    view.getGraph2D().setURL(resource);

    // set default configurations of this demo and add user data to nodes and labels
    IsometryRealizerFactory.applyIsometryRealizerDefaults(view.getGraph2D());

    // calculate a new layout
    runLayout(false);
  }

  /**
   * Overwritten to add a hierarchic and an orthogonal layout button to the toolbar.
   *
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(createLayoutAction("Hierarchic", TYPE_INCREMENTAL_HIERARCHIC_LAYOUT), true));
    toolBar.add(createActionControl(createLayoutAction("Orthogonal", TYPE_ORTHOGONAL_GROUP_LAYOUT), true));
    return toolBar;
  }

  /**
   * Overwritten to disable deletion of graph elements because this demo is not editable.
   */
  protected boolean isDeletionEnabled() {
    return false;
  }

  /**
   * Overwritten to disable undo and redo because this demo is not editable.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Overwritten to disable clipboard because this demo is not editable.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  /**
   * Creates an {@link Action} that starts the layout after setting its layout type (hierarchic, incremental).
   */
  private Action createLayoutAction(final String text, final int type) {
    final AbstractAction action = new AbstractAction(text) {
      public void actionPerformed(ActionEvent e) {
        layoutType = type;
        runLayout(false);
      }
    };
    action.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);
    return action;
  }

  /**
   * Overwritten to create a {@link GraphMLIOHandler} that can handle {@link IsometryData} and serialize
   * {@link y.view.YLabel#getUserData() user data} for labels.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    final GraphMLIOHandler graphMLIOHandler = super.createGraphMLIOHandler();
    final Graph2DGraphMLHandler graphMLHandler = graphMLIOHandler.getGraphMLHandler();
    final IsometryDataIOHandler dataHandler = new IsometryDataIOHandler();
    graphMLHandler.addSerializationHandler(dataHandler);
    graphMLHandler.addDeserializationHandler(dataHandler);
    graphMLHandler.addSerializationHandler(new IsometryEdgeLabelSerializer());
    graphMLHandler.addDeserializationHandler(new IsometryEdgeLabelDeserializer());
    graphMLHandler.addSerializationHandler(new IsometryNodeLabelSerializer());
    graphMLHandler.addDeserializationHandler(new IsometryNodeLabelDeserializer());
    return graphMLIOHandler;
  }

  /**
   * Overwritten to disable {@link EditMode} because there is no interactive graph editing available.
   *
   * @see #registerViewModes()
   */
  protected EditMode createEditMode() {
    return null;
  }

  /**
   * Overwritten to register {@link NavigationMode} a view mode that opens folders/closes groups.
   */
  protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new NavigationMode());
    view.addViewMode(new GroupingViewMode());
  }

  /**
   * Runs either a {@link IncrementalHierarchicLayouter hierarchic} or an {@link OrthogonalGroupLayouter orthogonal}.
   * layout.
   */
  private void runLayout(final boolean fromSketch) {
    final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
    executor.getLayoutMorpher().setKeepZoomFactor(fromSketch);

    if (layoutType == TYPE_INCREMENTAL_HIERARCHIC_LAYOUT) {
      if (fromSketch) {
        ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
        executor.doLayout(view, new FixGroupStateIconLayoutStage(new IsometryTransformationLayoutStage(ihl, fromSketch)));
      } else {
        ihl.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
        executor.doLayout(view, new IsometryTransformationLayoutStage(ihl, fromSketch));
      }
    } else if (layoutType == TYPE_ORTHOGONAL_GROUP_LAYOUT) {
      if (fromSketch) {
        executor.doLayout(view, new FixGroupStateIconLayoutStage(new IsometryTransformationLayoutStage(ogl, fromSketch)));
      } else {
        executor.doLayout(view, new IsometryTransformationLayoutStage(ogl, fromSketch));
      }
    }
  }

  /**
   * Starts the demo.
   */
  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new IsometryDemo("resource/iso_help.html")).start("Isometry Demo");
      }
    });
  }

  /**
   * A {@link y.view.BackgroundRenderer} that displays an image or a plain color as background of {@link Graph2DView}
   * and adds a foggy frame to this background.
   */
  private static class FoggyFrameBackgroundRenderer extends DefaultBackgroundRenderer {

    private static final Color COLOR_BLANK = new Color(255, 255, 255, 0);

    private Color bgColor;

    public FoggyFrameBackgroundRenderer(Graph2DView view) {
      super(view);
      bgColor = Color.WHITE;
    }

    public void paint(Graphics2D graphics, int x, int y, int w, int h) {
      super.paint(graphics, x, y, w, h);

      paintFoggyFrame(graphics);
    }

    private void paintFoggyFrame(Graphics2D graphics) {
      final Paint oldPaint = graphics.getPaint();
      final AffineTransform oldTransform = graphics.getTransform();

      undoWorldTransform(graphics);

      final float viewX = (float) 0;
      final float viewY = (float) 0;
      final float viewW = (float) view.getWidth();
      final float viewH = (float) view.getHeight();

      final float halfWidth = viewW * 0.5f;
      final float halfHeight = viewH * 0.5f;
      graphics.setPaint(
          new GradientPaint(viewX + halfWidth, viewY, bgColor, viewX + halfWidth, viewY + viewH * 0.2f, COLOR_BLANK));
      graphics.fillRect((int) viewX, (int) viewY, (int) viewW, (int) (viewH * 0.2));
      graphics.setPaint(
          new GradientPaint(viewX + halfWidth, viewY + viewH * 0.8f, COLOR_BLANK, viewX + halfWidth, viewY + viewH,
              bgColor));
      graphics.fillRect((int) viewX, (int) (viewY + viewH * 0.8), (int) viewW, (int) (viewH * 0.2));
      graphics.setPaint(
          new GradientPaint(viewX, viewY + halfHeight, bgColor, viewX + viewW * 0.2f, viewY + halfHeight, COLOR_BLANK));
      graphics.fillRect((int) viewX, (int) viewY, (int) (viewW * 0.2), (int) viewH);
      graphics.setPaint(
          new GradientPaint(viewX + viewW * 0.8f, viewY + halfHeight, COLOR_BLANK, viewX + viewW, viewY + halfHeight,
              bgColor));
      graphics.fillRect((int) (viewX + viewW * 0.8), (int) viewY, (int) (viewW * 0.2), (int) viewH);

      graphics.setPaint(oldPaint);
      graphics.setTransform(oldTransform);
    }
  }

  /**
   * Provides an {@link IsometryData} instance for each node.
   */
  private static class TransformationDataProvider extends DataProviderAdapter {
    private final Graph2D graph;

    public TransformationDataProvider(final Graph2D graph) {
      this.graph = graph;
    }

    public Object get(final Object dataHolder) {
      if (dataHolder instanceof Node) {
        NodeRealizer realizer = graph.getRealizer((Node) dataHolder);
        if (realizer instanceof ProxyShapeNodeRealizer) {
          realizer = ((ProxyShapeNodeRealizer) realizer).getRealizerDelegate();
        }
        if (realizer instanceof GenericNodeRealizer) {
          return ((GenericNodeRealizer) realizer).getUserData();
        }
      } else if (dataHolder instanceof EdgeLabel) {
        return ((EdgeLabel) dataHolder).getUserData();
      }
      return null;
    }
  }

  /**
   * IOHandler that serializes and deserializes {@link IsometryData}.
   */
  private class IsometryDataIOHandler implements SerializationHandler, DeserializationHandler {
    private static final String ELEMENT_NAME = "IsometryData";
    private static final String ELEMENT_WIDTH = "width";
    private static final String ELEMENT_HEIGHT = "height";
    private static final String ELEMENT_DEPTH = "depth";
    private static final String ELEMENT_HORIZONTAL = "horizontal";

    public void onHandleSerialization(SerializationEvent event) throws GraphMLWriteException {
      final Object item = event.getItem();
      if (item instanceof IsometryData) {
        IsometryData isometryData = (IsometryData) item;
        final XmlWriter writer = event.getWriter();
        writer.writeStartElement(ELEMENT_NAME, NamespaceConstants.YFILES_JAVA_NS);
        writer.writeAttribute(ELEMENT_WIDTH, isometryData.getWidth());
        writer.writeAttribute(ELEMENT_HEIGHT, isometryData.getHeight());
        writer.writeAttribute(ELEMENT_DEPTH, isometryData.getDepth());
        writer.writeAttribute(ELEMENT_HORIZONTAL, isometryData.isHorizontal());
        writer.writeEndElement();
        event.setHandled(true);
      }
    }

    public void onHandleDeserialization(DeserializationEvent event) throws GraphMLParseException {
      final org.w3c.dom.Node xmlNode = event.getXmlNode();
      if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && NamespaceConstants.YFILES_JAVA_NS.equals(xmlNode.getNamespaceURI())
          && ELEMENT_NAME.equals(xmlNode.getLocalName())) {
        final Element element = (Element) xmlNode;
        String attribute = element.getAttribute(ELEMENT_WIDTH);
        final double width = Double.parseDouble(attribute);
        attribute = element.getAttribute(ELEMENT_HEIGHT);
        final double height = Double.parseDouble(attribute);
        attribute = element.getAttribute(ELEMENT_DEPTH);
        final double depth = Double.parseDouble(attribute);
        attribute = element.getAttribute(ELEMENT_HORIZONTAL);
        final boolean horizontal = Boolean.getBoolean(attribute);
        event.setResult(new IsometryData(width, depth, height, horizontal));
      }
    }
  }

  /**
   * {@link EdgeLabelSerializer} that additionally serializes the {@link y.view.YLabel#getUserData() user data} of an
   * {@link EdgeLabel}.
   */
  private static class IsometryEdgeLabelSerializer extends EdgeLabelSerializer {
    protected void serializeContent(EdgeLabel label, XmlWriter writer, GraphMLWriteContext context) throws
        GraphMLWriteException {
      super.serializeContent(label, writer, context);
      if (label.getUserData() != null) {
        writer.writeStartElement("UserData", NamespaceConstants.YFILES_JAVA_NS);
        context.serialize(label.getUserData());
        writer.writeEndElement();
      }
    }
  }

  /**
   * {@link EdgeLabelSerializer} that additionally deserializes the {@link y.view.YLabel#getUserData() user data} of an
   * {@link EdgeLabel}.
   */
  private static class IsometryEdgeLabelDeserializer extends EdgeLabelDeserializer {
    protected void parseEdgeLabel(GraphMLParseContext context, org.w3c.dom.Node root, EdgeLabel label) throws
        GraphMLParseException {
      super.parseEdgeLabel(context, root, label);
      final org.w3c.dom.Node userDataNode = XPathUtils.selectFirstChildElement(root, "UserData",
          NamespaceConstants.YFILES_JAVA_NS);
      Object userData = null;
      if (userDataNode != null) {
        final org.w3c.dom.Node isoData = XPathUtils.selectFirstChildElement(userDataNode, "IsometryData",
                  NamespaceConstants.YFILES_JAVA_NS);
        if (isoData != null) {
          userData = context.deserialize(isoData);
        }
      }
      if (userData != null) {
        label.setUserData(userData);
      }
    }
  }

  /**
   * {@link NodeLabelSerializer} that additionally serializes the {@link y.view.YLabel#getUserData() user data} of a
   * {@link NodeLabel}.
   */
  private static class IsometryNodeLabelSerializer extends NodeLabelSerializer {
    protected void serializeContent(NodeLabel label, XmlWriter writer, GraphMLWriteContext context) throws
        GraphMLWriteException {
      super.serializeContent(label, writer, context);
      if (label.getUserData() != null) {
        writer.writeStartElement("UserData", NamespaceConstants.YFILES_JAVA_NS);
        context.serialize(label.getUserData());
        writer.writeEndElement();
      }
    }
  }

  /**
   * {@link NodeLabelSerializer} that additionally deserializes the {@link y.view.YLabel#getUserData() user data} of a
   * {@link NodeLabel}.
   */
  private static class IsometryNodeLabelDeserializer extends NodeLabelDeserializer {
    protected void parseNodeLabel(GraphMLParseContext context, org.w3c.dom.Node root, NodeLabel label) throws
        GraphMLParseException {
      super.parseNodeLabel(context, root, label);
      final org.w3c.dom.Node userDataNode = XPathUtils.selectFirstChildElement(root, "UserData",
          NamespaceConstants.YFILES_JAVA_NS);
      Object userData = null;
      if (userDataNode != null) {
        final org.w3c.dom.Node isoData = XPathUtils.selectFirstChildElement(userDataNode, "IsometryData",
            NamespaceConstants.YFILES_JAVA_NS);
        if (isoData != null) {
          userData = context.deserialize(isoData);
        }
      }
      if (userData != null) {
        label.setUserData(userData);
      }
    }
  }

  /**
   * A {@link ViewMode} that handles opening folders and closing groups that use {@link IsometryGroupPainter}.
   */
  private class GroupingViewMode extends ViewMode {
    public void mouseMoved(double x, double y) {
      if (hitsGroupStateIcon(x, y)) {
        view.setViewCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      } else {
        view.setViewCursor(Cursor.getDefaultCursor());
      }
    }

    public void mouseClicked(double x, double y) {
      if (hitsGroupStateIcon(x, y)) {
        final HitInfo hit = getHitInfo(x, y);
        final Node hitNode = hit.getHitNode();
        final HierarchyManager hm = view.getGraph2D().getHierarchyManager();
        view.getGraph2D().addDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY, new DataProviderAdapter() {
          public boolean getBool(Object dataHolder) {
            return dataHolder instanceof Node && hitNode == dataHolder;
          }
        });
        if (hm.isFolderNode(hitNode)) {
          openFolder(hitNode);
        } else {
          closeGroup(hitNode);
        }

        // run layout incrementally
        runLayout(true);

        view.getGraph2D().removeDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY);
      }
    }

    /**
     * Determines whether or not a group state icon is hit by the given coordinates.
     */
    private boolean hitsGroupStateIcon(double x, double y) {
      final HitInfo hit = getHitInfo(x, y);
      if (hit.hasHitNodes()) {
        final Node hitNode = hit.getHitNode();
        NodeRealizer realizer = view.getGraph2D().getRealizer(hitNode);
        if (realizer instanceof ProxyShapeNodeRealizer) {
          ProxyShapeNodeRealizer proxy = (ProxyShapeNodeRealizer) realizer;
          realizer = proxy.getRealizerDelegate();
        }
        if (realizer instanceof GenericGroupNodeRealizer
            && IsometryGroupPainter.hitsGroupStateIcon(realizer, x, y)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Closes the specified group node.
     * @param node    the group node that has to be converted to a folder node.
     */
    protected void closeGroup(final Node node) {
      Graph2DViewActions.CloseGroupsAction helper = new Graph2DViewActions.CloseGroupsAction(view) {
        protected boolean acceptNode(final Graph2D graph, final Node groupNode) {
          return groupNode == node;
        }
      };
      helper.setNodeStateChangeHandler(new GroupNodeStateChangeHandler(helper.getNodeStateChangeHandler()));
      helper.closeGroups(view);
    }

    /**
     * Opens the specified folder node.
     * @param node    the folder node that has to be converted to a group node.
     */
    protected void openFolder(final Node node) {
      Graph2DViewActions.OpenFoldersAction helper = new Graph2DViewActions.OpenFoldersAction(view) {
        protected boolean acceptNode(final Graph2D graph, final Node groupNode) {
          return groupNode == node;
        }
      };
      helper.setNodeStateChangeHandler(new GroupNodeStateChangeHandler(helper.getNodeStateChangeHandler()));
      helper.openFolders(view);
    }
  }

  /**
   * Normally, the state icon of a group node is placed at the upper left corner of the group node. If the group node is
   * opened or closed the state icon remains on the same place. But the state icon of the isometric group node is placed
   * at the lower left corner. This {@link NodeStateChangeHandler} moves the group node so, that the state icon also
   * remains on the same place
   */
  private static final class GroupNodeStateChangeHandler implements NodeStateChangeHandler {
    private final NodeStateChangeHandler handler;
    private final Map state;

    GroupNodeStateChangeHandler(final NodeStateChangeHandler handler) {
      this.handler = handler;
      state = new HashMap();
    }

    /**
     * Overwritten to store the lower left corner of the group node before it is closed/opened.
     */
    public void preNodeStateChange(final Node node) {
      state.put(node, calculateFixPoint(node));

      if (handler != null) {
        handler.postNodeStateChange(node);
      }
    }

    private Object calculateFixPoint(final Node node) {
      final NodeRealizer realizer = ((Graph2D) node.getGraph()).getRealizer(node);
      final double[] corners = new double[16];
      final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(realizer);
      isometryData.calculateCorners(corners);
      IsometryData.moveTo(realizer.getX(), realizer.getY(), corners);

      return new YPoint(corners[IsometryData.C3_X], corners[IsometryData.C3_Y]);
    }

    /**
     * Overwritten to move the group node to the stored point after it has been closed/opened.
     */
    public void postNodeStateChange(final Node node) {
      final YPoint fixPoint = (YPoint) state.remove(node);
      if (fixPoint != null) {
        restoreFixPoint(node, fixPoint);
      }

      if (handler != null) {
        handler.postNodeStateChange(node);
      }
    }

    private void restoreFixPoint(final Node node, final YPoint fixPoint) {
      final Graph2D graph = (Graph2D) node.getGraph();
      NodeRealizer realizer = graph.getRealizer(node);
      if (realizer instanceof ProxyShapeNodeRealizer) {
        realizer = ((ProxyShapeNodeRealizer) realizer).getRealizerDelegate();
      }
      final double[] corners = new double[16];
      final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(realizer);
      isometryData.calculateCorners(corners);
      IsometryData.moveTo(realizer.getX(), realizer.getY(), corners);

      final double newCornerX = corners[IsometryData.C3_X];
      final double newCornerY = corners[IsometryData.C3_Y];

      final double dx = fixPoint.getX() - newCornerX;
      final double dy = fixPoint.getY() - newCornerY;

      final HierarchyManager hm = graph.getHierarchyManager();
      if (hm.isGroupNode(node)) {
        final NodeList subGraph = new NodeList(hm.getChildren(node));
        subGraph.add(node);
        LayoutTool.moveSubgraph(graph, subGraph.nodes(), dx, dy);
      } else {
        LayoutTool.moveNode(graph, node, dx, dy);
      }
    }
  }

  /**
   * When the user opens/closes a folder/group node by clicking its state icon, the layouter calculates a new layout.
   * This {@link y.layout.LayoutStage} moves the graph afterwards so, that the state icon of the group/folder node
   * remains under the mouse cursor.
   */
  private class FixGroupStateIconLayoutStage extends FixNodeLayoutStage {
    private FixGroupStateIconLayoutStage(final Layouter core) {
      super(core);
    }

    /**
     * Overwritten to fix the lower left corner (where the state icon is placed) of the isometric painted folder/group
     * node.
     */
    protected YPoint calculateFixPoint(final LayoutGraph graph, final NodeList fixed) {
      final Node node = fixed.firstNode();
      final DataProvider provider = graph.getDataProvider(IsometryTransformationLayoutStage.TRANSFORMATION_DATA_DPKEY);
      final IsometryData isometryData = (IsometryData) provider.get(node);

      final double[] corners = new double[16];
      isometryData.calculateCorners(corners);
      final NodeLayout nodeLayout = graph.getNodeLayout(node);
      IsometryData.moveTo(nodeLayout.getX(), nodeLayout.getY(), corners);
      return new YPoint(corners[IsometryData.C3_X], corners[IsometryData.C3_Y]);
    }
  }
}
