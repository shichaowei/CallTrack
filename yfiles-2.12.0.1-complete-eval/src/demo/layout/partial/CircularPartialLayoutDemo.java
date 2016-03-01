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
package demo.layout.partial;

import y.layout.Layouter;
import y.layout.circular.CircularLayouter;
import y.layout.partial.PartialLayouter;
import y.option.IntOptionItem;
import y.option.OptionHandler;

import java.awt.EventQueue;
import java.util.Locale;

/**
 * This demo shows how to apply the partial layouter to circular layouts. The partial layouter changes the coordinates
 * for a given set of graph elements (called partial elements). The location or size of the remaining elements (called
 * fixed elements) is not allowed to be changed. The layout algorithm tries to place the partial elements such that the
 * resulting drawing (including the fixed elements) has a good quality with respect to common graph drawing aesthetics.
 * <p/>
 * Partial node elements can be assigned to so called subgraph components. During the layout process each subgraph
 * induced by the nodes of a component is first laid out using the specified subgraph layouter. Then, the different
 * components are placed one-by-one onto the drawing area such that the number of overlaps among graph elements is
 * small. The user can specify different objectives (placement strategies) for finding 'good' positions for subgraph
 * components.
 * <p/>
 * The demo allows to specify fixed and partial elements. Fixed elements are drawn grey and partial elements orange. To
 * change the fixed/partial state of elements, select the corresponding elements and click on the "Lock Selected
 * Elements" or "Unlock Selected Elements" button. The current state of selected elements can be toggled with a
 * mouse-double-click. To start the partial layouter click on the "Apply Partial Layout" button.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/partial_layout.html">Section Partial Layout</a> in the yFiles for Java Developer's Guide
 */
public class CircularPartialLayoutDemo extends PartialLayoutBase {

  public CircularPartialLayoutDemo() {
    this(null);
  }

  public CircularPartialLayoutDemo(final String helpFilePath) {
    super(helpFilePath);
  }

  /**
   * Loads a graph, which contains fix nodes and nodes, which should be integrated into this graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/graphCircular.graphml");
  }


  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    layoutOptionHandler.addEnum("Subgraph Layout",
        new Object[]{"Circular Layout", "Unchanged"}, 0);
    layoutOptionHandler.addEnum("Component Assignment",
        new Object[]{"Single Nodes", "Connected Graphs"}, 1);
    layoutOptionHandler.addEnum("Placement Strategy",
        new Object[]{"Barycenter", "From Sketch"}, 0);
    layoutOptionHandler.addEnum("Edge Routing Style",
        new Object[]{"Automatic", "Straight Line", "Organic"}, 0);
    layoutOptionHandler.addBool("Allow Mirroring", false);
    layoutOptionHandler.addInt("Minimum Node Distance", 10);
    layoutOptionHandler.getItem("Minimum Node Distance").setAttribute(
        IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(0));

    return layoutOptionHandler;
  }

  protected Layouter createConfiguredPartialLayouter() {
    final PartialLayouter partialLayouter = new PartialLayouter();

    if (optionHandler != null) {
      final int minNodeDist = optionHandler.getInt("Minimum Node Distance");
      partialLayouter.setMinimalNodeDistance(minNodeDist);
      switch (optionHandler.getEnum("Subgraph Layout")) {
        default:
        case 0:
          CircularLayouter layouter = new CircularLayouter();
          layouter.getSingleCycleLayouter().setMinimalNodeDistance(minNodeDist);
          partialLayouter.setCoreLayouter(layouter);
          break;
        case 1:
          // is null per default
      }
      switch (optionHandler.getEnum("Component Assignment")) {
        default:
        case 0:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE);
          break;
        case 1:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED);
          break;
      }
      switch (optionHandler.getEnum("Placement Strategy")) {
        default:
        case 0:
          partialLayouter.setPositioningStrategy(PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER);
          break;
        case 1:
          partialLayouter.setPositioningStrategy(PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH);
          break;
      }
      switch (optionHandler.getEnum("Edge Routing Style")) {
        default:
        case 0:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_AUTOMATIC);
          break;
        case 1:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_STRAIGHTLINE);
          break;
        case 2:
          partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORGANIC);
          break;
      }
      partialLayouter.setMirroringAllowed(optionHandler.getBool("Allow Mirroring"));
    }
    return partialLayouter;
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new CircularPartialLayoutDemo("resource/circularlayouthelp.html"))
            .start("Circular Partial Layouter Demo");
      }
    });
  }
}