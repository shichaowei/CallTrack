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

import demo.view.DemoBase;
import y.base.DataProvider;
import y.base.Edge;
import y.io.GraphMLIOHandler;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.layout.LayoutOrientation;
import y.layout.Layouter;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.orthogonal.DirectedOrthogonalLayouter;
import y.layout.router.polyline.EdgeRouter;
import y.util.DataProviderAdapter;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.TooltipMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

/**
 * This demo allows you to visualize and edit UML class diagrams. It shows how to
 * <ul>
 *  <li>create a configuration for {@link y.view.hierarchy.GroupNodeRealizer} that provides interactive buttons to show
 *  or hide parts of the node and to add or remove labels.</li>
 *  <li>load and save the customized node realizer.</li>
 *  <li>use {@link y.view.Drawable}s to display interactive buttons that invoke edge creation.</li>
 *  <li>animate the appearance and disappearance of the buttons.</li>
 *  <li>write a customized {@link EditMode} that handles mouse interactions with buttons.</li>
 *  <li>add a layout action that calculates a layout well-suited for UML diagrams.</li>
 *  <li>route edges incrementally after graph changes.</li>
 * </ul>
 */
public class UmlDemo extends DemoBase {
  private static final double PAINT_DETAIL_THRESHOLD = 0.4;
  private static final double MAX_ZOOM = 4.0;
  private static final double MIN_ZOOM = 0.05;

  private Layouter layouter;
  private boolean fractionMetricsEnabled;

  public UmlDemo() {
    this(null);
  }

  public UmlDemo(String helpFilePath) {
    addHelpPane(helpFilePath);

    configureLabelRendering();
    configureZoomThreshold();
    layouter = createLayouter();

    loadGraph("resource/shopping.graphml");
    addPortConstraints(view.getGraph2D());
  }

  /**
   * Adds {@link PortConstraint}s to the edges which are inheritance relations. Inheritance relations always start on
   * top of the source node and end at the bottom of the target node. This behaviour is enforced by adding these
   * port constraints.
   */
  private void addPortConstraints(final Graph2D graph) {
    // add source port constraints to the inheritance edges so they have to connect to the north of a node
    final DataProvider sourcePorts = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        final Edge edge = (Edge) dataHolder;
        final EdgeRealizer er = graph.getRealizer(edge);
        return UmlRealizerFactory.isInheritance(er) ? PortConstraint.create(PortConstraint.NORTH) : null;
      }
    };
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePorts);

    // add target port constraints to the inheritance edges so they have to connect to the south of a node
    final DataProvider targetPorts = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        final Edge edge = (Edge) dataHolder;
        final EdgeRealizer er = graph.getRealizer(edge);
        return UmlRealizerFactory.isInheritance(er) ? PortConstraint.create(PortConstraint.SOUTH) : null;
      }
    };
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPorts);
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
   * Creates the layouter for UML layout.
   */
  private Layouter createLayouter() {
    // create a directed orthogonal layouter with reversed layout orientation to place the target nodes of the directed
    // edges above their source nodes
    final DirectedOrthogonalLayouter dol = new DirectedOrthogonalLayouter();
    dol.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);

    // mark all inheritance edges (generalization, realization) as directed so their target nodes
    // will be placed above their source nodes
    // all other edges are treated as undirected
    final Graph2D graph = view.getGraph2D();
    final DataProvider directedEdges = new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        final Edge edge = (Edge) dataHolder;
        return UmlRealizerFactory.isInheritance(graph.getRealizer(edge));
      }
    };
    graph.addDataProvider(DirectedOrthogonalLayouter.DIRECTED_EDGE_DPKEY, directedEdges);

    // combine all edges with a white delta as target arrow (generalization, realization) in edge groups according to
    // their line type
    // do not group the other edges
    final DataProvider targetGroups = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        final Edge edge = (Edge) dataHolder;
        final EdgeRealizer er = graph.getRealizer(edge);
        return UmlRealizerFactory.isRealization(er) ? er.getLineType() : null;
      }
    };
    graph.addDataProvider(PortConstraintKeys.TARGET_GROUPID_KEY, targetGroups);

    return dol;
  }

  /**
   * Overwritten to set the UML graph elements as default.
   */
  protected void configureDefaultRealizers() {
    final Graph2D graph = view.getGraph2D();
    graph.setDefaultNodeRealizer(UmlRealizerFactory.createClassRealizer());
    graph.setDefaultEdgeRealizer(UmlRealizerFactory.createAssociationRealizer());
  }

  /**
   * Overwritten to create and configure an {@link EditMode} that is customized for this demo.
   */
  protected EditMode createEditMode() {
    final UmlEditMode editMode = new UmlEditMode(view, new EdgeRouter());
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    editMode.setSnappingEnabled(true);
    return editMode;
  }

  /**
   * Registers customized {@link y.view.ViewMode}s that handles clicks on labels.
   */
  protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new UmlClassLabelEditMode());
  }

  /**
   * Overwritten to add a layout button to the default toolbar.
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();

    final AbstractAction layoutAction = new AbstractAction("Layout") {
      public void actionPerformed(ActionEvent e) {
        runLayout();
      }
    };
    layoutAction.putValue(Action.SHORT_DESCRIPTION, "Layout");
    layoutAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);
    toolBar.add(createActionControl(layoutAction, true));

    return toolBar;
  }

  /**
   * Overwritten to add (de-)serialization handling of UML class nodes.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    final GraphMLIOHandler graphMLIOHandler = super.createGraphMLIOHandler();
    final Graph2DGraphMLHandler graphMLHandler = graphMLIOHandler.getGraphMLHandler();
    final UmlClassModelIOHandler modelHandler = new UmlClassModelIOHandler();
    graphMLHandler.addSerializationHandler(modelHandler);
    graphMLHandler.addDeserializationHandler(modelHandler);
    return graphMLIOHandler;
  }

  /**
   * Calculates a layout for the current graph.
   */
  private void runLayout() {
    new Graph2DLayoutExecutor().doLayout(view, layouter);
  }

  /**
   * Overwritten to turn off tooltips for graph elements.
   */
  protected TooltipMode createTooltipMode() {
    return null;
  }

  /**
   * Starts the {@link UmlDemo}.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            Locale.setDefault(Locale.ENGLISH);
            initLnF();
            final UmlDemo demo = new UmlDemo("resource/umlhelp.html");
            demo.start("UML Diagram Demo");
          }
        });
  }
}
