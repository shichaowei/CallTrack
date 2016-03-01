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
package demo.layout.radial;

import demo.view.DemoBase;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.NodeMap;
import y.layout.radial.RadialLayouter;
import demo.layout.module.RadialLayoutModule;
import y.option.OptionHandler;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.Bend;
import y.view.BendCursor;
import y.view.BezierEdgeRealizer;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.PolyLineEdgeRealizer;
import y.view.QuadCurveEdgeRealizer;
import y.view.SplineEdgeRealizer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

/**
 * Showcases the {@link RadialLayouter} and the effect of its configuration settings.
 * <p>
 * The demo offers to calculate a radial layout for a default or customized graph using different settings
 * of its layout module.
 * </p>
 * <p>
 * The circles the nodes are placed on as well as their sectors are visualized on demand. A combo box offers different
 * {@link EdgeRealizer edge realizers} suitable for a radial layout.
 * </p>
 *
 */
public class RadialLayouterDemo extends DemoBase {

  /* The layout module to configure the RadialLayouter.*/
  private RadialLayoutModule module;

  /* A node map the RadialLayouter fills with RadialLayouter.NodeInfo objects which are used by the SectorDrawable.*/
  private NodeMap nodeInfoMap;

  /* The SectorDrawable to visualize the circles and/or sectors the nodes are placed on/in.*/
  private SectorDrawable sectorDrawable;

  /* The edge realizers offered by the edge realizer combo box.*/
  private EdgeRealizer[] edgeRealizers;

  public RadialLayouterDemo() {
    // load and layout sample graph
    loadGraph("resource/radial.graphml");
    doLayout();
  }

  protected void initialize() {
    super.initialize();

    initializeEdgeRealizer();

    // add node map to hold NodeInfo data crated by the RadialLayouter
    nodeInfoMap = view.getGraph2D().createNodeMap();
    view.getGraph2D().addDataProvider(RadialLayouter.NODE_INFO_DPKEY, new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        RadialLayouter.NodeInfo nodeInfo = (RadialLayouter.NodeInfo) nodeInfoMap.get(dataHolder);
        if (nodeInfo == null) {
          nodeInfo = new RadialLayouter.NodeInfo();
          nodeInfoMap.set(dataHolder, nodeInfo);
        }
        return nodeInfo;
      }
    });

    // add drawable for the circles and sectors and only draw circles initially
    sectorDrawable = new SectorDrawable(view, nodeInfoMap);
    view.addBackgroundDrawable(sectorDrawable);
  }

  private void initializeEdgeRealizer() {
    //use a delta arrow to make edge directions clear
    SplineEdgeRealizer splineEdgeRealizer = new SplineEdgeRealizer();
    splineEdgeRealizer.setArrow(Arrow.DELTA);

    QuadCurveEdgeRealizer quadCurveEdgeRealizer = new QuadCurveEdgeRealizer();
    quadCurveEdgeRealizer.setArrow(Arrow.DELTA);

    BezierEdgeRealizer bezierEdgeRealizer = new BezierEdgeRealizer();
    bezierEdgeRealizer.setArrow(Arrow.DELTA);

    PolyLineEdgeRealizer polyLineEdgeRealizer = new PolyLineEdgeRealizer();
    polyLineEdgeRealizer.setArrow(Arrow.DELTA);

    view.getGraph2D().setDefaultEdgeRealizer(polyLineEdgeRealizer);
    edgeRealizers = new EdgeRealizer[]{splineEdgeRealizer, quadCurveEdgeRealizer, bezierEdgeRealizer, polyLineEdgeRealizer};
  }

  /**
   * Creates a toolbar for configuring and running the radial layouter.
   */
  protected JToolBar createToolBar() {
    module = new RadialLayoutModule();

    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(new AbstractAction(
            "Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(final ActionEvent e) {
        doLayout();
      }
    }));

    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(createActionControl(new AbstractAction(
            "Settings...", getIconResource("resource/properties.png")) {
      public void actionPerformed(final ActionEvent e) {
        final OptionHandler settings = module.getOptionHandler();
        if (settings != null) {
          final ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              doLayout();
            }
          };

          OptionSupport.showDialog(settings, listener, false, view.getFrame());
        }
      }
    }));

    toolBar.addSeparator();
    final JToggleButton showCirclesBtn = new JToggleButton(new AbstractAction("Show Circles") {
      public void actionPerformed(ActionEvent e) {
        sectorDrawable.setDrawingCircles(((AbstractButton) e.getSource()).isSelected());
        view.updateView();
      }
    });
    showCirclesBtn.setSelected(sectorDrawable.isDrawingCircles());
    toolBar.add(showCirclesBtn);
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    final JToggleButton showSectorsBtn = new JToggleButton(new AbstractAction("Show Sectors") {
      public void actionPerformed(ActionEvent e) {
        sectorDrawable.setDrawingSectors(((AbstractButton) e.getSource()).isSelected());
        view.updateView();
      }
    });
    showSectorsBtn.setSelected(sectorDrawable.isDrawingSectors());
    toolBar.add(showSectorsBtn);
    toolBar.addSeparator();

    final JComboBox comboBox = new JComboBox(new Object[]{"Spline Routing", "Bezier Routing", "Quad Curve Routing", "Polygonal Routing"});
    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.setSelectedIndex(3);
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EdgeRealizer selectedRealizer = edgeRealizers[comboBox.getSelectedIndex()];
        if (view.getGraph2D().getDefaultEdgeRealizer() != selectedRealizer) {
          view.getGraph2D().setDefaultEdgeRealizer(selectedRealizer);
          updateEdgeRealizers();
        }
      }
    });
    toolBar.add(comboBox);

    return toolBar;
  }

  private void doLayout() {
    module.start(view.getGraph2D());
    sectorDrawable.updateSectors();
    view.fitContent();
    view.updateView();
  }

  protected void loadGraph(final URL resource) {
    super.loadGraph(resource);

    // clear sector drawable as it is outdated
    sectorDrawable.updateSectors();

    updateEdgeRealizers();
  }

  /**
   * Update all edges to use (a copy of) the default edge realizer.
   * <p>
   * Existing bends of transferred to the new realizers.
   * </p>
   */
  private void updateEdgeRealizers() {
    final Graph2D graph2D = view.getGraph2D();

    for (EdgeCursor ec = graph2D.edges(); ec.ok(); ec.next()) {
      Edge edge = ec.edge();
      EdgeRealizer oldRealizer = graph2D.getRealizer(edge);
      EdgeRealizer newRealizer = graph2D.getDefaultEdgeRealizer().createCopy();
      for (BendCursor bc = oldRealizer.bends(); bc.ok(); bc.next()) {
        Bend bend = bc.bend();
        newRealizer.addPoint(bend.getX(), bend.getY());
      }
      graph2D.setRealizer(edge, newRealizer);
    }
    view.updateView();
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new RadialLayouterDemo().start();
      }
    });
  }
}
