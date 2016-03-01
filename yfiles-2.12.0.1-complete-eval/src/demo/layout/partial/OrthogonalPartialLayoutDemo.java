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
import y.layout.orthogonal.OrthogonalGroupLayouter;
import y.layout.partial.PartialLayouter;
import y.option.OptionHandler;
import y.view.Graph2DLayoutExecutor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * This demo shows how to apply the partial layouter to orthogonal layouts. The partial layouter changes the coordinates
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
public class OrthogonalPartialLayoutDemo extends PartialLayoutBase {

  public OrthogonalPartialLayoutDemo() {
    this(null);
  }

  public OrthogonalPartialLayoutDemo(final String helpFilePath) {
    super(helpFilePath);
  }

  /**
   * Loads a graph, which contains fix nodes and nodes, which should be integrated into this graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/orthogonal.graphml");
  }

  /**
   * Adds an action for orthogonal layout to the default toolbar.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = super.createToolBar();

    final OrthogonalLayoutAction orthogonalLayoutAction = new OrthogonalLayoutAction("Orthogonal Layout");
    orthogonalLayoutAction.putValue(Action.SHORT_DESCRIPTION, "Orthogonal Layout");
    toolBar.add(orthogonalLayoutAction);

    return toolBar;
  }

  protected OptionHandler createOptionHandler() {
    final OptionHandler layoutOptionHandler = new OptionHandler("Option Table");

    layoutOptionHandler.addEnum("Subgraph Layout",
        new Object[]{"Orthogonal Layout", "Unchanged"}, 0);
    layoutOptionHandler.addInt("Grid Size", 10, 1, 50);
    layoutOptionHandler.addEnum("Component Assignment",
        new Object[]{"Single Nodes", "Connected Graphs", "Same Component"}, 0);
    layoutOptionHandler.addBool("Use Snapping", true);
    layoutOptionHandler.addBool("Use Sketch", false);
    layoutOptionHandler.addBool("Resize Fixed Groups", true);

    return layoutOptionHandler;
  }

  protected Layouter createConfiguredPartialLayouter() {
    final PartialLayouter partialLayouter = new PartialLayouter();

    if (optionHandler != null) {
      switch (optionHandler.getEnum("Subgraph Layout")) {
        default:
        case 0:
          partialLayouter.setCoreLayouter(getOrthogonalLayouter());
          break;
        case 1:
          // is null per default
      }
      partialLayouter.setPositioningStrategy(
          optionHandler.getBool(
              "Use Sketch") ? PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_FROM_SKETCH : PartialLayouter.SUBGRAPH_POSITIONING_STRATEGY_BARYCENTER);

      switch (optionHandler.getEnum("Component Assignment")) {
        default:
        case 0:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_SINGLE);
          break;
        case 1:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CONNECTED);
          break;
        case 2:
          partialLayouter.setComponentAssignmentStrategy(PartialLayouter.COMPONENT_ASSIGNMENT_STRATEGY_CUSTOMIZED);
          break;
      }
      partialLayouter.setConsiderNodeAlignment(optionHandler.getBool("Use Snapping"));
      partialLayouter.setFixedGroupResizingEnabled(optionHandler.getBool("Resize Fixed Groups"));
      partialLayouter.setMinimalNodeDistance(optionHandler.getInt("Grid Size"));
    }
    partialLayouter.setEdgeRoutingStrategy(PartialLayouter.EDGE_ROUTING_STRATEGY_ORTHOGONAL);
    partialLayouter.setLayoutOrientation(PartialLayouter.ORIENTATION_NONE);
    return partialLayouter;
  }

  /**
   * This method configures and returns the OrthogonalGroupLayouter
   * @return an instance of OrthogonalGroupLayouter
   */
  private OrthogonalGroupLayouter getOrthogonalLayouter() {
    OrthogonalGroupLayouter layouter = new OrthogonalGroupLayouter();
    if (optionHandler != null) {
      layouter.setGrid(optionHandler.getInt("Grid Size"));
    }
    return layouter;
  }

  /**
   * Launches the OrthogonalLayouter.
   */
  class OrthogonalLayoutAction extends AbstractAction {
    OrthogonalLayoutAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
      executor.getLayoutMorpher().setEasedExecution(true);
      executor.getLayoutMorpher().setSmoothViewTransform(true);
      executor.doLayout(view, getOrthogonalLayouter());
      view.updateView();
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new OrthogonalPartialLayoutDemo("resource/orthogonallayouthelp.html"))
            .start("Orthogonal Partial Layouter Demo");
      }
    });
  }
}