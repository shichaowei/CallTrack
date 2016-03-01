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
package demo.layout.router;

import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.LayoutGraph;
import y.layout.router.BusRepresentations;
import y.util.Maps;
import y.util.pq.IntObjectPQ;
import y.view.Graph2D;
import y.view.NodeRealizer;

import java.awt.Color;
import java.util.Random;

/**
 * Governs the coloring of the buses. It maintains a list of predefined nice colors and reclaims colors which are no
 * longer in use.
 */
public class BusDyer implements GraphListener {
  private final Graph2D graph;
  private final DataProvider hubMarker;
  private final IntObjectPQ availableColorsPQ;
  private final int predefinedColorsCount;
  private int eventCount;

  /**
   * Creates a new instance for the given graph and its hubs.
   *
   * @param graph the graph
   */
  public BusDyer(Graph2D graph) {
    this.graph = graph;
    this.hubMarker = graph.getDataProvider(BusRouterDemo.HUB_MARKER_DPKEY);

    this.eventCount = 0;
    this.predefinedColorsCount = 50;
    DataMap backingStore = Maps.createHashedDataMap();
    this.availableColorsPQ = new IntObjectPQ(predefinedColorsCount, backingStore, backingStore);
    resetColors();
  }

  /**
   * Restores a valid bus coloring for the graph with respect to the current appearence.
   */
  public void colorize() {
    colorize(null);
  }

  /**
   * Restores a valid bus coloring for the graph with respect to the specified color provider and the current
   * appearance. First, to each bus is assigned any color provided for one of its edges. If no such color exists, a
   * color from the current appearance is chosen.
   *
   * @param colorProvider a data provider which provides the color of an edge
   */
  public void colorize(DataProvider colorProvider) {
    resetColors();

    // store the isolated hubs since they are not covered by the edge lists below
    final NodeList isolatedHubList = new NodeList();
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      if (node.degree() == 0 && hubMarker.getBool(node)) {
        isolatedHubList.add(node);
      }
    }
    final Node[] isolatedHubs = isolatedHubList.toNodeArray();

    // get an edge list for each bus and check if a valid bus color is already set
    final EdgeList[] edgeLists = calculateBusComponents(graph);
    final Color[] busColors = new Color[edgeLists.length + isolatedHubs.length];
    for (int i = 0; i < edgeLists.length; i++) {
      final EdgeList edgeList = edgeLists[i];
      Color descriptorColor = null;
      Color presentColor = null;
      for (EdgeCursor ec = edgeList.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (colorProvider != null && colorProvider.get(edge) != null) {
          descriptorColor = (Color) colorProvider.get(edge);
          break;
        }
        final Color lineColor = graph.getRealizer(edge).getLineColor();
        if (presentColor == null && !lineColor.equals(Color.BLACK)) {
          presentColor = lineColor;
        }
        Color sourceFill = graph.getRealizer(edge.source()).getFillColor();
        if (presentColor == null && !sourceFill.equals(Color.BLACK)) {
          presentColor = sourceFill;
        }
        Color targetFill = graph.getRealizer(edge.target()).getFillColor();
        if (presentColor == null && !targetFill.equals(Color.BLACK)) {
          presentColor = targetFill;
        }
        if (presentColor != null && colorProvider == null) {
          break;
        }
      }

      if (descriptorColor != null && isAvailable(descriptorColor)) {
        busColors[i] = descriptorColor;
        use(descriptorColor);
      } else if (presentColor != null && isAvailable(presentColor)) {
        busColors[i] = presentColor;
        use(presentColor);
      }
    }

    // check for each isolated hub if a valid bus color is already set
    for (int i = 0; i < isolatedHubs.length; i++) {
      final Node isolatedHub = isolatedHubs[i];
      final Color fillColor = graph.getRealizer(isolatedHub).getFillColor();
      if (!fillColor.equals(Color.BLACK) && isAvailable(fillColor)) {
        busColors[edgeLists.length + i] = fillColor;
        use(fillColor);
      }
    }

    // get colors for all uncolored buses and isolated hubs
    for (int i = 0; i < busColors.length; i++) {
      if (busColors[i] == null) {
        busColors[i] = useNextColor();
      }
    }

    // set the colors to the buses
    for (int i = 0; i < edgeLists.length; i++) {
      final EdgeList edgeList = edgeLists[i];
      final Color color = busColors[i];
      for (EdgeCursor ec = edgeList.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        graph.getRealizer(edge).setLineColor(color);
        colorizeHub(edge.source(), color);
        colorizeHub(edge.target(), color);
      }
    }

    // set the colors to the isolated hubs
    for (int i = 0; i < isolatedHubs.length; i++) {
      colorizeHub(isolatedHubs[i], busColors[edgeLists.length + i]);
    }
  }

  /**
   * Listens to node and edge creation and removal and executes the coloring if not inside of a pre-post-block. Note
   * that this implementation does a coloring of the complete graph which is not recommended for larger graphs.
   */
  public void onGraphEvent(GraphEvent e) {
    switch (e.getType()) {
      case GraphEvent.PRE_EVENT:
        eventCount++;
        break;
      case GraphEvent.POST_EVENT:
        eventCount--;
        break;
      case GraphEvent.NODE_CREATION:
      case GraphEvent.NODE_REINSERTION:
      case GraphEvent.PRE_NODE_REMOVAL:
      case GraphEvent.POST_NODE_REMOVAL:
        return; // the coloring can only change on edge events
      default:
    }

    if (eventCount == 0) {
      colorize();
    }
  }

  /**
   * Call-back which provides the collection of the bus edges of each bus.
   */
  protected EdgeList[] calculateBusComponents(LayoutGraph graph) {
    return BusRepresentations.toEdgeLists(graph, graph.getDataProvider(BusRouterDemo.HUB_MARKER_DPKEY));
  }

  /**
   * Sets the color of the given node.
   *
   * @param node     the node
   * @param newColor the color to set
   */
  private void colorizeHub(Node node, Color newColor) {
    if (!hubMarker.getBool(node)) {
      return;
    }
    final NodeRealizer realizer = graph.getRealizer(node);
    realizer.setFillColor(newColor);
    realizer.setLineColor(newColor);
  }

  /**
   * Sets all colors to unused.
   */
  private void resetColors() {
    availableColorsPQ.clear();

    Color[] colors = Colors.getColors(predefinedColorsCount + 1);
    for (int i = 1; i < colors.length; i++) { // discard black
      final Color color = colors[i];
      availableColorsPQ.add(color, i - 1);
    }
  }

  /**
   * Returns the next available color and marks it as used.
   *
   * @return the next unused color
   */
  private Color useNextColor() {
    if (availableColorsPQ.isEmpty()) {
      return Colors.getRandomColor();
    } else {
      return (Color) availableColorsPQ.removeMin();
    }
  }

  /**
   * Sets the color to <code>used</code>.
   *
   * @param color the color
   */
  private void use(final Color color) {
    if (isAvailable(color)) {
      availableColorsPQ.remove(color);
    }
  }

  /**
   * Returns whether the color is available or not.
   *
   * @param color the color
   * @return <true> if the color is not in use.
   */
  private boolean isAvailable(final Color color) {
    return availableColorsPQ.contains(color);
  }

  /**
   * Provides sets of distinct colors.
   */
  static class Colors {

    // some nice predefined colors
    private static final Color[] COLORS = {new Color(0x000000), new Color(0xBF0404), new Color(0x009EFF),
        new Color(0x1B8C48), new Color(0xB300C2), new Color(0xFF6405), new Color(0x2B4BFA), new Color(0x8C6048),
        new Color(0xFAAFE8), new Color(0xB1D95B), new Color(0xBBB082), new Color(0xFAEE00), new Color(0xAAAAAA),
        new Color(0x00FFC9), new Color(0x5B519C), new Color(0x666666)};

    private static final Random RANDOM = new Random(1234L);

    /**
     * Do not instantiate this class
     */
    private Colors() {
    }

    /**
     * Returns an array of distinct colors. The first 16 colors are predefined and span the complete color range. If
     * more colors are required, these are created as intermediate shades of the predefined colors.
     *
     * @param count the number of colors to return
     * @return an array of colors
     */
    static Color[] getColors(int count) {
      Color[] r = new Color[count];
      final int numColors = COLORS.length;
      System.arraycopy(COLORS, 0, r, 0, Math.min(count, numColors));
      if (count > numColors) {
        double div = Math.ceil((double) (count / numColors)) + 1.0;
        for (int i = numColors; i < count; i++) {
          int j = i % numColors;
          int k = (j + 1) % numColors;
          Color c1 = COLORS[j];
          Color c2 = COLORS[k];
          double f = 1.0 / div * Math.ceil((double) (i / numColors));
          r[i] = blendColors(c1, c2, f);
        }
      }
      return r;
    }

    /**
     * Returns a random color.
     *
     * @return a random color
     */
    static Color getRandomColor() {
      return new Color(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat());
    }

    /**
     * Returns a color that lays between the tow specified colors.
     *
     * @return an intermediate color
     */
    private static Color blendColors(Color c1, Color c2, double div) {
      int dr = (int) ((double) (c2.getRed() - c1.getRed()) * div);
      int dg = (int) ((double) (c2.getGreen() - c1.getGreen()) * div);
      int db = (int) ((double) (c2.getBlue() - c1.getBlue()) * div);
      return new Color(c1.getRed() + dr, c1.getGreen() + dg, c1.getBlue() + db);
    }

  }

}
