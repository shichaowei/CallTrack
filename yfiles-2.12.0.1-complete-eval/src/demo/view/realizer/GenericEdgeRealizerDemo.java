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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.ListCell;
import y.geom.YPoint;
import y.geom.YVector;
import y.option.RealizerCellRenderer;
import y.view.Arrow;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BendList;
import y.view.EdgeRealizer;
import y.view.FramedEdgePainter;
import y.view.GenericEdgePainter;
import y.view.GenericEdgeRealizer;
import y.view.LineType;
import y.view.NodeRealizer;
import y.view.PolyLinePathCalculator;
import y.view.Port;
import y.view.TooltipMode;
import y.view.YRenderingHints;

/**
 * This class demonstrates various usages of the {@link y.view.GenericEdgeRealizer} class. <br/> Usage: Try adding new
 * edges and adding bends to existing edges. The list on the left side allows to switch between the possible edge types
 * for creating new edges.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/custom_realizers.html#customization_ger">Section Writing Customized Realizers</a> in the yFiles for Java Developer's Guide
 */
public class GenericEdgeRealizerDemo extends DemoBase {
  private static final Color defaultEdgeColor = new Color(202, 227, 255);
  private static final LineType defaultEdgeLineType = LineType.LINE_3;

  /**
   * Creates the GenericEdgeRealizer demo.
   */
  public GenericEdgeRealizerDemo() {
    super();

    //Add a list with available edge realizers to the panel.
    TitledBorder border = new TitledBorder("Edge Types:");
    border.setTitlePosition(TitledBorder.BELOW_TOP);
    JScrollPane scrollPane = new JScrollPane(createEdgeRealizerList());
    scrollPane.setBorder(border);
    contentPane.add(scrollPane, BorderLayout.WEST);

    loadGraph("resource/genericEdgeRealizer.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();

    // Get the factory to register custom styles/configurations.
    GenericEdgeRealizer.Factory factory = GenericEdgeRealizer.getFactory();

    // Retrieve a map that holds the default GenericEdgeRealizer configuration.
    // The implementations contained therein can be replaced one by one in order
    // to create custom configurations...
    Map implementationsMap = factory.createDefaultConfigurationMap();

    // The edge path is painted 3D-ish and with a drop shadow.
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new CustomEdgePainter());
    // The path is calculated to be undulating.
    implementationsMap.put(GenericEdgeRealizer.PathCalculator.class, new UndulatingPathCalculator());

    // Add the first configuration to the factory.
    factory.addConfiguration("Undulating", implementationsMap);

    implementationsMap.put(GenericEdgeRealizer.PathCalculator.class, new MyPathCalculator());
    // Add the second configuration to the factory.
    // NB: It uses the same type of painter as the previous configuration.
    factory.addConfiguration("QuadCurve", implementationsMap);

    // Special behavior for an otherwise normal poly-line edge path calculator:
    // first and last segment of the edge path are kept axes-parallel.
    implementationsMap.put(GenericEdgeRealizer.PathCalculator.class,
        new PortMoverPathCalculator(new PolyLinePathCalculator()));
    factory.addConfiguration("PolyLineAxesParallel", implementationsMap);

    // Default edge painter implementation.

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new GenericEdgePainter());
    implementationsMap.put(GenericEdgeRealizer.PathCalculator.class, new UndulatingPathCalculator());
    // Bends are rendered differently depending on their selection state.
    // - normal rendering: blue ellipse (height is half of width)
    // - rendering when bend is selected: red ellipse
    implementationsMap.put(GenericEdgeRealizer.BendPainter.class,
        new CustomBendPainter(new Ellipse2D.Double(0, 0, 10, 5), new Ellipse2D.Double(0, 0, 10, 10), Color.blue,
            Color.red));
    factory.addConfiguration("UndulatingCustomBends", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.ArrowPainter.class, new CenterArrowPainter());
    implementationsMap.put(GenericEdgeRealizer.PathCalculator.class, new UnclippedPathCalculator());
    factory.addConfiguration("Unclipped", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    MultiArrowPainter arrowPainter = new MultiArrowPainter();
    implementationsMap.put(GenericEdgeRealizer.ArrowPainter.class, arrowPainter);
    factory.addConfiguration("MultiArrow", implementationsMap);


    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new MultiColorEdgePainter());
    factory.addConfiguration("MultiColorSegments", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new SignatureEdgePainter("foo", 50));
    factory.addConfiguration("EdgeSignature", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new DoubleStrokedPainter());
    factory.addConfiguration("DoubleStroked", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new FramedEdgePainter());
    factory.addConfiguration("Framed", implementationsMap);

    implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericEdgeRealizer.Painter.class, new DashedFrameEdgePainter());
    factory.addConfiguration("FramedDashed", implementationsMap);

    // Initialize the GenericEdgeRealizer instance to one of the types we just
    // registered with the factory.

    // Take a default GenericEdgeRealizer...
    GenericEdgeRealizer ger = new GenericEdgeRealizer();
    // ... and make it real flashy.
    ger.setLineType(defaultEdgeLineType);
    ger.setLineColor(defaultEdgeColor);
    ger.setConfiguration("Framed");
    ger.setUserData("This is my own userData object.");
    view.getGraph2D().setDefaultEdgeRealizer(ger);
  }

  protected TooltipMode createTooltipMode() {
    TooltipMode tooltipMode = new TooltipMode() {
      /**
       * Overwrites {@link TooltipMode#getNodeTip(y.base.Node)} to set a tooltip text with the configuration of the
       * {@link GenericEdgeRealizer} associated with the edge.
       * @param edge the edge for which the tooltip is set.
       * @return a tooltip text with the realizers configuration if the edge has a <code>GenericNodeRealizer</code>,
       * <code>null</code> otherwise.
       */
      protected String getEdgeTip(Edge edge) {
        String tipText = null;
        if (getGraph2D().getRealizer(edge) instanceof GenericEdgeRealizer) {
          GenericEdgeRealizer realizer = (GenericEdgeRealizer) getGraph2D().getRealizer(edge);
          tipText = realizer.getConfiguration();
        }
        return tipText;
      }
    };
    tooltipMode.setNodeTipEnabled(false);
    return tooltipMode;
  }

  /**
   * Creates a JList with different edge realizers.
   *
   * @return a JList with edge realizers.
   */
  protected JList createEdgeRealizerList() {
    final List configurations = new ArrayList(GenericEdgeRealizer.getFactory().getAvailableConfigurations());
    Collections.sort(configurations);

    // Create a list with different edge realizers.
    final List edgeRealizers = new ArrayList();
    for (Iterator iterator = configurations.iterator(); iterator.hasNext(); ) {
      final String configuration = (String) iterator.next();
      final GenericEdgeRealizer realizer = new GenericEdgeRealizer(configuration);
      if ("DoubleStroked".equals(configuration)) {
        realizer.setLineType(LineType.LINE_2);
      } else if (configuration.startsWith("Framed")) {
        realizer.setLineType(LineType.LINE_3);
        realizer.setTargetArrow(Arrow.STANDARD);
      } else if (configuration.startsWith("Undulating")) {
        realizer.setStyleProperty("MyFunnyPathCalculator.Wavelength", new Integer(10));
      } else {
        realizer.setLineType(defaultEdgeLineType);
      }
      realizer.setLineColor(defaultEdgeColor);
      edgeRealizers.add(realizer);
    }

    // Create a JList with the edge realizers.
    final JList result = new JList(edgeRealizers.toArray()) {
      public String getToolTipText(MouseEvent evt) {
        int index = locationToIndex(evt.getPoint());
        return (String) configurations.get(index);
      }
    };

    // A special cell renderer is used here to show the edge realizers and its connecting nodes as well.
    final int CELL_WIDTH = 150;
    final int CELL_HEIGHT = 60;
    result.setCellRenderer(new RealizerCellRenderer(CELL_WIDTH, CELL_HEIGHT) {
      protected Icon createEdgeRealizerIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        RealizerCellRenderer.EdgeRealizerIcon icon = new RealizerCellRenderer.EdgeRealizerIcon(realizer, iconWidth,
            iconHeight) {
          protected void paintRealizer(EdgeRealizer edgeRealizer, Graphics2D gfx) {
            edgeRealizer.getPath();

            NodeRealizer sourceNodeRealizer = edgeRealizer.getSourceRealizer();
            sourceNodeRealizer.setSize(15.0, 15.0);
            sourceNodeRealizer.setCenter(15.0, calculateSourceBend(edgeRealizer, CELL_WIDTH, CELL_HEIGHT).getY());
            sourceNodeRealizer.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
            sourceNodeRealizer.setLineColor(DemoDefaults.DEFAULT_NODE_LINE_COLOR);
            sourceNodeRealizer.setVisible(true);
            sourceNodeRealizer.paint(gfx);

            NodeRealizer targetNodeRealizer = edgeRealizer.getTargetRealizer();
            targetNodeRealizer.setSize(15.0, 15.0);
            targetNodeRealizer.setCenter(CELL_WIDTH - 15.0,
                calculateTargetBend(edgeRealizer, CELL_WIDTH, CELL_HEIGHT).getY());
            targetNodeRealizer.setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
            targetNodeRealizer.setLineColor(DemoDefaults.DEFAULT_NODE_LINE_COLOR);
            targetNodeRealizer.setVisible(true);
            targetNodeRealizer.paint(gfx);

            edgeRealizer.paint(gfx);
          }
        };
        icon.setDrawingBends(false);
        return icon;
      }
    });

    // Use the selected list item as edge realizer.
    result.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        EdgeRealizer edgeRealizer = (EdgeRealizer) result.getSelectedValue();
        view.getGraph2D().setDefaultEdgeRealizer(edgeRealizer);
      }
    });

    // Allow only single selection.
    result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final int idx = indexOf(edgeRealizers, view.getGraph2D().getDefaultEdgeRealizer());
    if (-1 < idx && idx < edgeRealizers.size()) {
      result.setSelectedIndex(idx);
    } else {
      result.setSelectedIndex(0);
    }
    return result;
  }

  /**
   * Finds the first occurrence of a edge realizer with the same configuration
   * as the specified edge realizer. 
   * @param haystack the list of (generic) edge realizer to search in.
   * @param needle the edge realizer to search for.
   * @return the index of the first occurrence of a matching edge realizer or
   * <code>-1</code> if no such occurrence can be found.
   */
  private static int indexOf( final List haystack, final EdgeRealizer needle ) {
    final String c =
            needle instanceof GenericEdgeRealizer
            ? ((GenericEdgeRealizer) needle).getConfiguration()
            : null;

    int i = 0;
    for (Iterator it = haystack.iterator(); it.hasNext(); ++i) {
      final GenericEdgeRealizer next = (GenericEdgeRealizer) it.next();
      if (c == null ? null == next.getConfiguration() : c.equals(next.getConfiguration())) {
        return i;
      }
    }
    return -1;
  }

  /**
   * A custom EdgePainter implementation that draws the edge path 3D-ish and adds a drop shadow also.
   */
  static final class CustomEdgePainter extends GenericEdgePainter {
    protected void paintPath(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      Stroke s = gfx.getStroke();
      Color oldColor = gfx.getColor();
      if (s instanceof BasicStroke) {
        Color c;
        if (selected) {
          initializeSelectionLine(context, gfx, selected);
          c = gfx.getColor();
        } else {
          initializeLine(context, gfx, selected);
          c = gfx.getColor();
          gfx.setColor(new Color(0, 0, 0, 64));
          gfx.translate(4, 4);
          gfx.draw(path);
          gfx.translate(-4, -4);
        }
        Color newC = selected ? Color.RED : c;
        gfx.setColor(new Color(128 + newC.getRed() / 2, 128 + newC.getGreen() / 2, 128 + newC.getBlue() / 2));
        gfx.translate(-1, -1);
        gfx.draw(path);
        gfx.setColor(new Color(newC.getRed() / 2, newC.getGreen() / 2, newC.getBlue() / 2));
        gfx.translate(2, 2);
        gfx.draw(path);
        gfx.translate(-1, -1);
        gfx.setColor(c);
        gfx.draw(path);
        gfx.setColor(oldColor);
      } else {
        gfx.draw(path);
      }
    }
  }

  static final class MultiColorEdgePainter extends GenericEdgePainter {
    protected void paintPath(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      Color oldColor = gfx.getColor();
      if (selected) {
        initializeSelectionLine(context, gfx, selected);
      } else {
        initializeLine(context, gfx, selected);
      }
      double[] segmentCoords = new double[6];
      PathIterator iterator = path.getPathIterator(new AffineTransform());
      double lastX = 0;
      double lastY = 0;
      final Line2D.Double doubleLine = new Line2D.Double();
      while (!iterator.isDone()) {
        int type = iterator.currentSegment(segmentCoords);
        switch (type) {
          case PathIterator.SEG_MOVETO:
            lastX = segmentCoords[0];
            lastY = segmentCoords[1];
            break;
          case PathIterator.SEG_LINETO:
            doubleLine.x1 = lastX;
            doubleLine.y1 = lastY;
            doubleLine.x2 = segmentCoords[0];
            doubleLine.y2 = segmentCoords[1];
            gfx.draw(doubleLine);
            lastX = segmentCoords[0];
            lastY = segmentCoords[1];
            gfx.setColor(getNextColor(gfx.getColor()));
            break;
          default:
            break;
        }
        iterator.next();
      }
      gfx.setColor(oldColor);
    }

    /**
     * Calculates the color for the next edge segment.
     * <br/>
     * This implementation returns the next darker version of the current color.
     * <br/>
     * If the color shall change in a different way, this method needs to be overwritten.
     *
     * @param currentColor the color of the current segment
     * @return the color of the next segment
     */
    protected Color getNextColor(Color currentColor) {
      float[] colorInHsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);
      final float step = 0.07f;
      return Color.getHSBColor(colorInHsb[0], colorInHsb[1], Math.max(0, colorInHsb[2] - step));
    }
  }

  /**
   * Simple painter that displays a double stroked poly-line edge.
   */
  static final class DoubleStrokedPainter extends GenericEdgePainter {
    protected void renderPath(
            final EdgeRealizer context,
            final Graphics2D gfx,
            final GeneralPath path,
            final boolean selected
    ) {
      final PathIterator it = path.getPathIterator(null);
      if (!it.isDone()) {
        final Line2D.Double line = new Line2D.Double();
        final double[] buffer = new double[6];

        while (!it.isDone()) {
          switch (it.currentSegment(buffer)) {
            case PathIterator.SEG_MOVETO:
              line.x2 = buffer[0];
              line.y2 = buffer[1];
              break;
            case PathIterator.SEG_LINETO:
              line.x2 = buffer[0];
              line.y2 = buffer[1];
              renderSegment(gfx, line);
              break;
            case PathIterator.SEG_QUADTO:
              // ignore curve, draw a line segment to the curve's endpoint
              line.x2 = buffer[2];
              line.y2 = buffer[3];
              renderSegment(gfx, line);
              break;
            case PathIterator.SEG_CUBICTO:
              // ignore curve, draw a line segment to the curve's endpoint
              line.x2 = buffer[4];
              line.y2 = buffer[5];
              renderSegment(gfx, line);
              break;
//            case PathIterator.SEG_CLOSE:
//              // path should not be closed
//              break;
          }

          it.next();
          line.x1 = line.x2;
          line.y1 = line.y2;
        }
      }
    }

    /**
     * Renders a single line segment in a double stroked way (if possible).
     * @param gfx the graphics context to draw upon.
     * @param line the line segment to draw.
     */
    private void renderSegment( final Graphics2D gfx, final Line2D.Double line ) {
      final double x1 = line.x1;
      final double y1 = line.y1;
      final double x2 = line.x2;
      final double y2 = line.y2;

      try {
        final double ox = y2 - y1;
        final double oy = x1 - x2;
        final double ol = Math.sqrt(ox * ox + oy * oy);

        if (ol > 0) {
          final double xOffset = (2 * ox) / ol;
          final double yOffset = (2 * oy) / ol;
          line.x1 = x1 + xOffset;
          line.y1 = y1 + yOffset;
          line.x2 = x2 + xOffset;
          line.y2 = y2 + yOffset;
          gfx.draw(line);
          line.x1 = x1 - xOffset;
          line.y1 = y1 - yOffset;
          line.x2 = x2 - xOffset;
          line.y2 = y2 - yOffset;
          gfx.draw(line);
        } else {
          gfx.draw(line);
        }
      } finally {
        line.x1 = x1;
        line.y1 = y1;
        line.x2 = x2;
        line.y2 = y2;
      }
    }
  }
  
  static final class SignatureEdgePainter extends GenericEdgePainter {
    private String signature;
    private double distance;

    public SignatureEdgePainter(String signature, double distance) {
      this.signature = signature;
      if (distance <= 0d) {
        throw new IllegalArgumentException("distance <= 0");
      }
      this.distance = distance;
    }

    protected void paintPath(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      // Draw path "normally".
      super.paintPath(context, bends, path, gfx, selected);

      // Add signatures.
      Color oldColor = gfx.getColor();
      gfx.setColor(Color.BLACK);
      double length = getPathLength(path);
      int numberOfSignatures = (int) Math.floor(length / distance);
      if (numberOfSignatures < 1) {
        return;
      }
      double slack = length - numberOfSignatures * distance;
      double distToGo = 0.5 * (slack + distance);
      double currentLength = 0.0;
      for (CustomPathIterator pi = new CustomPathIterator(path, 1.0); pi.ok(); pi.next()) {
        YPoint segmentStart = pi.segmentStart();
        YVector segmentDirection = pi.segmentDirection();
        double segmentLength = segmentDirection.length();
        while (distToGo < segmentLength && currentLength + distToGo < length) {
          segmentDirection.scale(distToGo / segmentLength);
          YPoint location = segmentStart.moveBy(segmentDirection.getX(), segmentDirection.getY());
          AffineTransform oldTransform = gfx.getTransform();
          AffineTransform newTransform = gfx.getTransform();
          double theta = Math.atan2(segmentDirection.getY(), segmentDirection.getX());
          newTransform.rotate(theta, location.getX(), location.getY());
          gfx.setTransform(newTransform);
          gfx.drawString(signature, (float) location.getX(), (float) location.getY());
          gfx.setTransform(oldTransform);
          segmentDirection = pi.segmentDirection();
          distToGo += distance;
        }
        distToGo -= segmentLength;
        currentLength += segmentLength;
      }
      gfx.setColor(oldColor);
    }
  }

  /**
   * Displays a framed edge with a dash pattern along the edge path.
   */
  public static final class DashedFrameEdgePainter extends FramedEdgePainter {
    protected void paintForeground(
            final EdgeRealizer context,
            final Graphics2D gfx,
            final GeneralPath path, final GeneralPath outline,
            final boolean selected
    ) {
      final LineType s = context.getLineType();
      gfx.setStroke(createInnerPattern(s.getLineWidth()));
      gfx.draw(path);

      super.paintForeground(context, gfx, path, outline, selected);
    }

    /**
     * Creates a dashed stroke with the specified width. The dash pattern
     * depends on the stroke width.
     * @param w the width of the new stroke.
     * @return a dashed stroke with the specified width.
     */
    protected BasicStroke createInnerPattern( final float w ) {
      final float dash = w * 1.5f;
      return new BasicStroke(
              w,
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER,
              2,
              new float[]{dash, w*2},
              dash);
    }
  }

  /**
   * A custom PathCalculator implementation that keeps the first and last segment of an edge path axes-parallel. To
   * achieve this behavior, the edge's source port and target port are moved to match any movement of the bend at the
   * opposite of the respective segment.
   * <p/>
   * If the edge path has only a single segment, it is drawn axes-parallel as soon as the projections of the two nodes
   * overlap on either x-axis or y-axis.
   */
  static final class PortMoverPathCalculator implements GenericEdgeRealizer.PathCalculator {
    private GenericEdgeRealizer.PathCalculator innerCalculator;

    PortMoverPathCalculator(GenericEdgeRealizer.PathCalculator innerCalculator) {
      this.innerCalculator = innerCalculator;
    }

    public byte calculatePath(EdgeRealizer context, BendList bends, GeneralPath path,
                              Point2D sourceIntersectionPointOut,
                              Point2D targetIntersectionPointOut) {
      final Port sp = context.getSourcePort();
      final Port tp = context.getTargetPort();
      final NodeRealizer snr = context.getSourceRealizer();
      final NodeRealizer tnr = context.getTargetRealizer();
      if (bends.size() > 0) {
        adjustPort(bends.firstBend(), snr, sp);
        adjustPort((Bend) bends.last(), tnr, tp);
      } else {
        double minx = Math.max(snr.getX(), tnr.getX());
        double maxx = Math.min(snr.getX() + snr.getWidth(), tnr.getX() + tnr.getWidth());
        if (maxx >= minx) {
          double pos = (minx + maxx) * 0.5d;
          sp.setOffsetX(pos - snr.getCenterX());
          tp.setOffsetX(pos - tnr.getCenterX());
        }
        double miny = Math.max(snr.getY(), tnr.getY());
        double maxy = Math.min(snr.getY() + snr.getHeight(), tnr.getY() + tnr.getHeight());
        if (maxy >= miny) {
          double pos = (miny + maxy) * 0.5d;
          sp.setOffsetY(pos - snr.getCenterY());
          tp.setOffsetY(pos - tnr.getCenterY());
        }
      }
      return innerCalculator.calculatePath(context, bends, path, sourceIntersectionPointOut,
          targetIntersectionPointOut);
    }

    private void adjustPort(Bend b, NodeRealizer realizer, Port port) {
      double x = b.getX();
      double y = b.getY();
      boolean inXRange = x >= realizer.getX() && x <= realizer.getX() + realizer.getWidth();
      boolean inYRange = y >= realizer.getY() && y <= realizer.getY() + realizer.getHeight();
      if (inXRange && !inYRange) {
        port.setOffsetX(x - realizer.getCenterX());
      }
      if (inYRange && !inXRange) {
        port.setOffsetY(y - realizer.getCenterY());
      }
    }
  }

  /**
   * A custom PathCalculator implementation that draws a quad curve edge path.
   */
  static final class MyPathCalculator extends PolyLinePathCalculator implements GenericEdgeRealizer.PathCalculator {
    private final GeneralPath scratch = new GeneralPath();


    public byte calculatePath(EdgeRealizer context, BendList bends, GeneralPath path,
                              Point2D sourceIntersectionPointOut,
                              Point2D targetIntersectionPointOut) {
      if (bends.size() == 0) {
        return super.calculatePath(context, bends, path, sourceIntersectionPointOut, targetIntersectionPointOut);
      } else {
        final int npoints = bends.size();

        path.reset();
        scratch.reset();

        NodeRealizer nr = context.getSourceRealizer();
        Port pp = context.getSourcePort();
        float lastPointx;
        float lastPointy;
        float secondLastPointx;
        float secondLastPointy;
        scratch.moveTo(lastPointx = (float) pp.getX(nr), lastPointy = (float) pp.getY(nr));

        int index = 0;

        secondLastPointx = lastPointx;
        secondLastPointy = lastPointy;

        BendCursor bc = bends.bends();

        {
          Bend b = bc.bend();
          lastPointx = (float) b.getX();
          lastPointy = (float) b.getY();
          bc.next();
          index++;
        }

        for (; index < npoints; bc.next(), index++) {
          Bend b = bc.bend();
          float nextPointx = (float) b.getX();
          float nextPointy = (float) b.getY();
          {
            final float sx = 0.5f * lastPointx + secondLastPointx * 0.5f;
            final float sy = 0.5f * lastPointy + secondLastPointy * 0.5f;
            scratch.lineTo(sx, sy);
          }
          {
            final float sx = 0.5f * nextPointx + lastPointx * 0.5f;
            final float sy = 0.5f * nextPointy + lastPointy * 0.5f;
            scratch.quadTo(lastPointx, lastPointy, sx, sy);
            secondLastPointx = lastPointx;
            secondLastPointy = lastPointy;
            lastPointx = nextPointx;
            lastPointy = nextPointy;
          }
        }

        nr = context.getTargetRealizer();
        pp = context.getTargetPort();

        {
          float nextPointx = (float) pp.getX(nr);
          float nextPointy = (float) pp.getY(nr);
          {
            final float sx = 0.5f * lastPointx + secondLastPointx * 0.5f;
            final float sy = 0.5f * lastPointy + secondLastPointy * 0.5f;
            scratch.lineTo(sx, sy);
          }
          {
            final float sx = 0.5f * nextPointx + lastPointx * 0.5f;
            final float sy = 0.5f * nextPointy + lastPointy * 0.5f;
            scratch.quadTo(lastPointx, lastPointy, sx, sy);
          }
          scratch.lineTo(nextPointx, nextPointy);
        }
        path.append(scratch.getPathIterator(null, 1.0), false);
      }
      return EdgeRealizer.calculateClippingAndIntersection(context, path, path, sourceIntersectionPointOut,
          targetIntersectionPointOut);
    }
  }

  /**
   * A custom PathCalculator implementation that draws an undulating edge path.
   */
  static final class UndulatingPathCalculator extends PolyLinePathCalculator implements GenericEdgeRealizer.PathCalculator {
    private final GeneralPath scratch = new GeneralPath();

    public byte calculatePath(EdgeRealizer context, BendList bends, GeneralPath path,
                              Point2D sourceIntersectionPointOut,
                              Point2D targetIntersectionPointOut) {
      scratch.reset();

      NodeRealizer nr = context.getSourceRealizer();
      Port pp = context.getSourcePort();
      float lastPointX;
      float lastPointY;
      scratch.moveTo(lastPointX = (float) pp.getX(nr), lastPointY = (float) pp.getY(nr));

      int wobbleCount = 0;
      for (BendCursor bc = bends.bends(); bc.ok(); bc.next()) {
        Bend b = bc.bend();
        float nextPointX = (float) b.getX();
        float nextPointY = (float) b.getY();
        float dx = nextPointX - lastPointX;
        float dy = nextPointY - lastPointY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
          int count = (int) (len / getWavelength(context)) + 1;
          for (int i = 0; i < count; i++) {
            final float height = wobbleCount % 2 == 0 ? 10 : -10;
            wobbleCount++;
            scratch.quadTo(lastPointX + (i + 0.5f) / ((float) count) * dx + dy * height / len,
                lastPointY + (i + 0.5f) / ((float) count) * dy - dx * height / len,
                lastPointX + (i + 1) / ((float) count) * dx, lastPointY + (i + 1) / ((float) count) * dy);
          }
        } else {
          scratch.lineTo(nextPointX, nextPointY);
        }
        lastPointX = nextPointX;
        lastPointY = nextPointY;
      }

      nr = context.getTargetRealizer();
      pp = context.getTargetPort();

      {
        float nextPointX = (float) pp.getX(nr);
        float nextPointY = (float) pp.getY(nr);
        float dx = nextPointX - lastPointX;
        float dy = nextPointY - lastPointY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
          int count = (int) (len / getWavelength(context)) + 1;
          for (int i = 0; i < count; i++) {
            final float height = wobbleCount % 2 == 0 ? 10 : -10;
            wobbleCount++;
            scratch.quadTo(lastPointX + (i + 0.5f) / ((float) count) * dx + dy * height / len,
                lastPointY + (i + 0.5f) / ((float) count) * dy - dx * height / len,
                lastPointX + (i + 1) / ((float) count) * dx, lastPointY + (i + 1) / ((float) count) * dy);
          }
        } else {
          scratch.lineTo(nextPointX, nextPointY);
        }
      }
      path.reset();
      return EdgeRealizer.calculateClippingAndIntersection(context, scratch, path, sourceIntersectionPointOut,
          targetIntersectionPointOut);
    }

    protected double getWavelength(EdgeRealizer context) {
      Object o = ((GenericEdgeRealizer) context).getStyleProperty("MyFunnyPathCalculator.Wavelength");
      if (o instanceof Number) {
        return ((Number) o).doubleValue();
      }
      return 30;
    }
  }


  /**
   * A custom BendPainter implementation that renders bends differently depending on the bend's selection state, but
   * also the edge's selection state.
   */
  static final class CustomBendPainter implements GenericEdgeRealizer.BendPainter {
    private RectangularShape shape;
    private RectangularShape selectedShape;
    private Color fillColor;
    private Color selectedFillColor;

    CustomBendPainter(RectangularShape shape, RectangularShape selectedShape, Color fillColor,
                      Color selectedFillColor) {
      this.selectedShape = selectedShape;
      this.shape = shape;
      this.fillColor = fillColor;
      this.selectedFillColor = selectedFillColor;
    }

    public void paintBends(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      if (!bends.isEmpty()) {
        final boolean useSelectionStyle = YRenderingHints.isSelectionPaintingEnabled(gfx);

        final Color oldColor = gfx.getColor();
        for (BendCursor bendCursor = bends.bends(); bendCursor.ok(); bendCursor.next()) {
          Bend b = bendCursor.bend();
          gfx.setColor(((selected || b.isSelected()) && useSelectionStyle) ? this.selectedFillColor : this.fillColor);
          final double x = b.getX();
          final double y = b.getY();
          RectangularShape shape = selected ? this.selectedShape : this.shape;
          shape.setFrame(x - shape.getWidth() / 2, y - shape.getHeight() / 2, shape.getWidth(), shape.getHeight());
          gfx.fill(shape);
        }
        gfx.setColor(oldColor);
      }
    }
  }

  /**
   * A simple ArrowPainter implementation that paints the arrow at the center of the segment in the middle of the
   * poly-line control path.
   */
  public static final class CenterArrowPainter implements GenericEdgeRealizer.ArrowPainter {
    public void paintArrows(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx) {
      Arrow targetArrow = context.getTargetArrow();
      if (targetArrow != null) {

        Point2D sourceIntersection = context.getSourceIntersection();
        Point2D targetIntersection = context.getTargetIntersection();

        if (bends.size() > 0) {
          int mid = bends.size() / 2;
          if (mid > 0) {
            Bend bend = context.getBend(mid - 1);
            sourceIntersection.setLocation(bend.getX(), bend.getY());
          }
          {
            Bend bend = context.getBend(mid);
            targetIntersection.setLocation(bend.getX(), bend.getY());
          }
        }

        double centerX = (targetIntersection.getX() + sourceIntersection.getX()) * 0.5d;
        double centerY = (targetIntersection.getY() + sourceIntersection.getY()) * 0.5d;
        double dx = (targetIntersection.getX() - sourceIntersection.getX());
        double dy = (targetIntersection.getY() - sourceIntersection.getY());
        double l = Math.sqrt(dx * dx + dy * dy);
        double arrowScaleFactor = context.getArrowScaleFactor();
        if (l > 0) {
          targetArrow.paint(gfx, centerX, centerY, arrowScaleFactor * dx / l, arrowScaleFactor * dy / l);
        }
      }
    }
  }

  /**
   * An ArrowPainter implementation that paints an arrow at the center of each long enough segment of an edge. The edge
   * is assumed to be a straight line edge.
   */
  public static final class MultiArrowPainter implements GenericEdgeRealizer.ArrowPainter {
    private double threshold = 50d;
    private Arrow arrow = Arrow.DELTA;
    private Color color = Color.LIGHT_GRAY;

    /**
     * returns the arrow used for drawing
     * 
     * @return the arrow used for drawing
     */
    public Arrow getArrow() {
      return arrow;
    }

    /**
     * sets the arrow used for drawing
     *
     * @param arrow an arrow
     */
    public void setArrow(Arrow arrow) {
      this.arrow = arrow;
    }


    /**
     * returns the color used for drawing the arrows
     *
     * @return the color of the arrows
     */
    public Color getColor() {
      return color;
    }

    /**
     * sets the color used for drawing the arrows
     *
     * @param color a color
     */
    public void setColor(Color color) {
      this.color = color;
    }

    /**
     * paints arrows at each segment of an edge, which is long enough
     *
     * @param context the realizer of the edge
     * @param bends   the bends of the edge
     * @param path    the path of the edge
     * @param gfx     the graphics to paint on
     * @see #setColor(Color)
     * @see #setArrow(Arrow)
     */
    public void paintArrows(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx) {
      if (arrow != null) {

        PathIterator iter = path.getPathIterator(null, 1);
        double[] curSeg = new double[2];
        if (!iter.isDone()) {
          iter.currentSegment(curSeg);
          Point2D p1 = new Point2D.Double(curSeg[0], curSeg[1]);
          Point2D p0 = new Point2D.Double();
          for (iter.next(); !iter.isDone(); iter.next()) {
            p0.setLocation(p1);
            iter.currentSegment(curSeg);
            p1.setLocation(curSeg[0], curSeg[1]);
            paintArrow(p1, p0, context, gfx);
          }
        }
      }
    }

    private void paintArrow(Point2D p1, Point2D p0, EdgeRealizer context, Graphics2D gfx) {
      if (arrow != null) {
        double centerX = (p1.getX() + p0.getX()) * 0.5d;
        double centerY = (p1.getY() + p0.getY()) * 0.5d;
        double dx = (p1.getX() - p0.getX());
        double dy = (p1.getY() - p0.getY());
        double l = Math.sqrt(dx * dx + dy * dy);
        double dxNormalized = dx / l;
        double dyNormalized = dy / l;
        if (l > threshold) {
          double arrowScaleFactor = context.getArrowScaleFactor();
          double offset = arrowScaleFactor * (arrow.getArrowLength() + arrow.getClipLength()) * 0.5d;
          double x = centerX + offset * dxNormalized;
          double y = centerY + offset * dyNormalized;
          Color oldColor = gfx.getColor();
          gfx.setColor(color);
          arrow.paint(gfx, x, y, arrowScaleFactor * dxNormalized, arrowScaleFactor * dyNormalized);
          gfx.setColor(oldColor);
        }
      }
    }
  }

  /**
   * A simple custom PathCalculator implementation that performs no clipping of the ends at the adjacent nodes.
   */
  public static final class UnclippedPathCalculator implements GenericEdgeRealizer.PathCalculator {
    public byte calculatePath(EdgeRealizer context, BendList bends, GeneralPath path,
                              Point2D sourceIntersectionPointOut,
                              Point2D targetIntersectionPointOut) {
      sourceIntersectionPointOut.setLocation(context.getSourcePort().getX(context.getSourceRealizer()),
          context.getSourcePort().getY(context.getSourceRealizer()));
      targetIntersectionPointOut.setLocation(context.getTargetPort().getX(context.getTargetRealizer()),
          context.getTargetPort().getY(context.getTargetRealizer()));
      path.reset();
      path.moveTo((float) sourceIntersectionPointOut.getX(), (float) sourceIntersectionPointOut.getY());
      for (ListCell cell = bends.firstCell(); cell != null; cell = cell.succ()) {
        Bend b = (Bend) cell.getInfo();
        path.lineTo((float) b.getX(), (float) b.getY());
      }
      path.lineTo((float) targetIntersectionPointOut.getX(), (float) targetIntersectionPointOut.getY());

      return EdgeRealizer.PATH_CLIPPED_AT_SOURCE_AND_TARGET;
    }
  }

  /**
   * Launcher method. Execute this class to see sample instantiations of {@link GenericEdgeRealizer} in action.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new GenericEdgeRealizerDemo().start("GenericEdgeRealizer Demo");
      }
    });
  }

  private static double getPathLength(GeneralPath path) {
    double length = 0.0;
    for (CustomPathIterator pi = new CustomPathIterator(path, 1.0); pi.ok(); pi.next()) {
      length += pi.segmentDirection().length();
    }
    return length;
  }

  /**
   * This class iterates over the segments in a flattened general path.
   */
  static class CustomPathIterator {
    private double[] cachedSegment;
    private boolean moreToGet;
    private PathIterator pathIterator;

    public CustomPathIterator(GeneralPath path, double flatness) {
      // copy the path, thus the original may safely change during iteration
      pathIterator = (new GeneralPath(path)).getPathIterator(new AffineTransform(), flatness);
      cachedSegment = new double[4];
      getFirstSegment();
    }

    public boolean ok() {
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
      if (moreToGet) {
        return new YPoint(cachedSegment[0], cachedSegment[1]);
      } else {
        return null;
      }
    }

    public YPoint segmentEnd() {
      if (moreToGet) {
        return new YPoint(cachedSegment[2], cachedSegment[3]);
      } else {
        return null;
      }
    }

    public YVector segmentDirection() {
      if (moreToGet) {
        return new YVector(segmentEnd(), segmentStart());
      } else {
        return null;
      }
    }

    public void next() {
      if (!pathIterator.isDone()) {
        float[] curSeg = new float[6];
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
      float[] curSeg = new float[6];
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
}
