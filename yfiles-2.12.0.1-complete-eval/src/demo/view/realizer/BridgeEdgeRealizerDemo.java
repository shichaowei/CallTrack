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
import demo.view.DemoDefaults;

import y.base.EdgeCursor;
import y.view.Arrow;
import y.view.BendList;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.GenericEdgePainter;
import y.view.GenericEdgeRealizer;
import y.view.Graph2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import java.awt.Graphics2D;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Locale;
import java.util.Map;

/**
 * This class demonstrates how to utilize {@link y.view.BridgeCalculator} to draw bridges/gaps for crossing edges with
 * custom {@link EdgeRealizer}s.
 * It demonstrates how to wrap a {@link y.view.GenericEdgeRealizer.Painter} implementation of a customized
 * {@link y.view.GenericEdgeRealizer} and use the current {@link BridgeCalculator} instance
 * from the {@link DefaultGraph2DRenderer}
 * to incorporate the calculation of bridges into the rendering.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_view.html#renderer">Section View Implementations</a> in the yFiles for Java Developer's Guide
 */
public class BridgeEdgeRealizerDemo extends DemoBase {
  BridgeCalculator bridgeCalculator;

  public BridgeEdgeRealizerDemo() {
    super();
  
    loadGraph("resource/bridgeEdgeRealizer.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    // get the factory to register our own styles
    GenericEdgeRealizer.Factory factory = GenericEdgeRealizer.getFactory();

    // Retrieve a map that holds the default GenericEdgeRealizer configuration.
    // The implementations contained therein can be replaced one by one in order 
    // to create custom configurations... 
    Map implementationsMap = factory.createDefaultConfigurationMap();

    // notice that the painter instance is wrapped using BridgedEdgePainter
    // which modifies the GeneralPath instance and provides the necessary BridgeCalculatorHandler
    // if the BridgeCalculator's mode should be set to two pass rendering (modes other than
    // CROSSING_MODE_ORDER_INDUCED)
    final BridgedEdgePainter painter = new BridgedEdgePainter(
        new GenericEdgePainter(), BridgeCalculator.CROSSING_STYLE_GAP);
    implementationsMap.put(GenericEdgeRealizer.Painter.class, painter);
    // used only when the bridgeCalculator is set to two pass rendering - otherwise not needed
    implementationsMap.put(GenericEdgeRealizer.BridgeCalculatorHandler.class, painter);

    // finally add the configuration to the factory
    factory.addConfiguration("bridgetype1", implementationsMap);

    // and another style
    final BridgedEdgePainter painter2 = new BridgedEdgePainter(
        new GenericEdgePainter(), BridgeCalculator.CROSSING_STYLE_ARC);
    implementationsMap.put(GenericEdgeRealizer.Painter.class, painter2);
    // used only when the bridgeCalculator is set to two pass rendering - otherwise not needed
    implementationsMap.put(GenericEdgeRealizer.BridgeCalculatorHandler.class, painter2);

    // finally add the configuration to the factory
    factory.addConfiguration("bridgetype2", implementationsMap);

    // Create a default EdgeRealizer
    GenericEdgeRealizer ger = new GenericEdgeRealizer();

    // initialize the default edge realizer to the type we just registered...
    ger.setConfiguration("bridgetype1");
    ger.setTargetArrow(Arrow.STANDARD);

    // set the realizer...
    final Graph2D graph = view.getGraph2D();
    graph.setDefaultEdgeRealizer(ger);

    // set an appropriate graph2drenderer that resets the bridge calculator initially for each painting
    bridgeCalculator = new BridgeCalculator();
    // optionally set a different crossing mode
    // (triggers usage of BridgeCalculatorHandler implementation)
    // bridgeCalculator.setCrossingMode(BridgeCalculator.CROSSING_MODE_HORIZONTAL_CROSSES_VERTICAL);
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);
  }

  protected JToolBar createToolBar() {
    final AbstractAction assignGapStyleAction = new AbstractAction("Gap Style") {
      public void actionPerformed(ActionEvent e) {
        setBridgeType("bridgetype1");
      }
    };
    assignGapStyleAction.putValue(Action.SHORT_DESCRIPTION, "Draw 'Gap' style bridges on the selected edges");
    assignGapStyleAction.putValue(Action.SMALL_ICON, getIconResource("resource/bridge_gap.png"));

    final AbstractAction assignArcStyleAction = new AbstractAction("Arc Style") {
      public void actionPerformed(ActionEvent e) {
        setBridgeType("bridgetype2");
      }
    };
    assignArcStyleAction.putValue(Action.SHORT_DESCRIPTION, "Draw 'Arc' style bridges on the selected edges");
    assignArcStyleAction.putValue(Action.SMALL_ICON, getIconResource("resource/bridge_arc.png"));

    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(new JButton(assignGapStyleAction));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(new JButton(assignArcStyleAction));
    return toolBar;
  }

  private void setBridgeType(String bridgeType) {
    final Graph2D graph = view.getGraph2D();
    // fire event to mark start of bridge type change for undo/redo
    graph.firePreEvent();
    // backup realizers to be able to restore the former edge realizer on undo
    graph.backupRealizers();
    try {
      EdgeCursor ec = graph.selectedEdges();
      if (ec.size() == 0) {
        ec = graph.edges();
      }
      for (; ec.ok(); ec.next()) {
        if (graph.getRealizer(ec.edge()) instanceof GenericEdgeRealizer) {
          ((GenericEdgeRealizer) graph.getRealizer(ec.edge())).setConfiguration(bridgeType);
        }
      }

      graph.setDefaultEdgeRealizer(new GenericEdgeRealizer(bridgeType));
      graph.updateViews();
    } finally {
      // fire event to mark end of bridge type change for undo/redo
      graph.firePostEvent();
    }
  }

  /**
   * Wrapping GenericEdgeRealizer.Painter implementation that modifies the given
   * GeneralPath to incorporate bridges. Then delegates the actual painting to
   * the given instance.
   */
  static final class BridgedEdgePainter implements GenericEdgeRealizer.Painter, GenericEdgeRealizer.BridgeCalculatorHandler {
    private final GenericEdgeRealizer.Painter painter;
    private final short bridgeStyle;

    BridgedEdgePainter(GenericEdgeRealizer.Painter painter, short bridgeStyle) {
      this.painter = painter;
      this.bridgeStyle = bridgeStyle;
    }

    public void paint(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      // modify the GeneralPath
      BridgeCalculator bridgeCalculator = DefaultGraph2DRenderer.getBridgeCalculator(context, gfx);
      if (bridgeCalculator != null) {
        GeneralPath p = new GeneralPath();
        // remember old style
        final short crossingStyle = bridgeCalculator.getCrossingStyle();
        try {
          bridgeCalculator.setCrossingStyle(bridgeStyle);
          PathIterator pathIterator = bridgeCalculator.insertBridges(path.getPathIterator(null, 1.0d));
          p.append(pathIterator, true);
          // and delegate the painting
          painter.paint(context, bends, p, gfx, selected);
        } finally {
          bridgeCalculator.setCrossingStyle(crossingStyle);
        }
      } else {
        painter.paint(context, bends, path, gfx, selected);
      }
    }

    public void paintSloppy(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      painter.paintSloppy(context, bends, path, gfx, selected);
    }

    // necessary for two-pass rendering only - the obstacles produced by this realizer have to be
    // registered with the BridgeCalculator
    public void registerObstacles(EdgeRealizer context, BendList bends, GeneralPath path, BridgeCalculator calculator) {
      calculator.registerObstacles(path.getPathIterator(null));
    }
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new BridgeEdgeRealizerDemo().start("Bridge EdgeRealizer Demo");
      }
    });
  }
}
