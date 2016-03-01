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
package demo.view.advanced.ports;

import y.geom.YPoint;
import y.geom.YRectangle;
import y.view.GenericNodeRealizer;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.PortConfigurationAdapter;
import y.view.SelectionPortPainter;
import y.view.ShapeNodePainter;
import y.view.ShapePortConfiguration;
import y.view.ShinyPlateNodePainter;

import java.awt.Color;
import java.util.HashMap;

/**
 * Registers the different {@link y.view.NodePort} configurations that are
 * showcased in the demo application.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizer_related.html">Section Realizer-Related Features</a> in the yFiles for Java Developer's Guide
 */
class PortConfigurations {
  private static final Color PORT_COLOR = new Color(255, 153, 0);

  private static final String PORT_CONFIG_RECTANGLE = "PORT_RECTANGLE";
  private static final String PORT_CONFIG_DYNAMIC = "PORT_DYNAMIC";
  private static final String PORT_CONFIG_ELLIPSE = "PORT_ELLIPSE";

  static {
    registerConfigurations();
  }

  public static final PortConfigurations INSTANCE = new PortConfigurations();


  /**
   * Registers the different {@link y.view.NodePort} configurations that can be
   * used when adding a new port.
   */
  private static void registerConfigurations() {
    {
      final ShinyPlateNodePainter impl = new ShinyPlateNodePainter();
      final HashMap nodeImpls = new HashMap();
      nodeImpls.put(GenericNodeRealizer.ContainsTest.class, impl);
      nodeImpls.put(GenericNodeRealizer.Painter.class, impl);

      final PortConfigurationAdapter adapter =
              new PortConfigurationAdapter(nodeImpls);
      adapter.setFillColor(PORT_COLOR);
      adapter.setFillColor2(null);
      adapter.setLineColor(null);

      final HashMap portImpls = new HashMap();
      portImpls.put(NodePort.ContainsTest.class, adapter);
      portImpls.put(NodePort.Painter.class, new SelectionPortPainter(adapter));
      final ShapePortConfiguration boundsProvider = new ShapePortConfiguration();
      boundsProvider.setSize(20, 10);
      portImpls.put(NodePort.BoundsProvider.class, boundsProvider);
      NodePort.getFactory().addConfiguration(PORT_CONFIG_RECTANGLE, portImpls);
    }


    {
      final HashMap nodeImpls = new HashMap();
      nodeImpls.put(
              GenericNodeRealizer.Painter.class,
              new ShapeNodePainter(ShapeNodePainter.RECT));

      final PortConfigurationAdapter adapter =
              new PortConfigurationAdapter(nodeImpls);
      adapter.setFillColor(PORT_COLOR);
      adapter.setFillColor2(null);
      adapter.setLineColor(Color.DARK_GRAY);

      final DynamicBounds impl = new DynamicBounds();
      final HashMap portImpls = new HashMap();
      portImpls.put(NodePort.ContainsTest.class, impl);
      portImpls.put(NodePort.Painter.class, new SelectionPortPainter(adapter));
      portImpls.put(NodePort.BoundsProvider.class, impl);
      NodePort.getFactory().addConfiguration(PORT_CONFIG_DYNAMIC, portImpls);
    }


    {
      final ShapeNodePainter impl = new ShapeNodePainter(ShapeNodePainter.ELLIPSE);
      final HashMap nodeImpls = new HashMap();
      nodeImpls.put(GenericNodeRealizer.ContainsTest.class, impl);
      nodeImpls.put(GenericNodeRealizer.Painter.class, impl);

      final PortConfigurationAdapter adapter =
              new PortConfigurationAdapter(nodeImpls);
      adapter.setFillColor(PORT_COLOR);
      adapter.setFillColor2(null);
      adapter.setLineColor(Color.DARK_GRAY);

      final HashMap portImpls = new HashMap();
      portImpls.put(NodePort.ContainsTest.class, adapter);
      portImpls.put(NodePort.Painter.class, new SelectionPortPainter(adapter));
      final ShapePortConfiguration boundsProvider = new ShapePortConfiguration();
      boundsProvider.setSize(10, 10);
      portImpls.put(NodePort.BoundsProvider.class, boundsProvider);
      NodePort.getFactory().addConfiguration(PORT_CONFIG_ELLIPSE, portImpls);
    }
  }



  final String portConfigRectangle;
  final String portConfigDynamic;
  final String portConfigEllipse;

  private PortConfigurations() {
    portConfigRectangle = PORT_CONFIG_RECTANGLE;
    portConfigDynamic = PORT_CONFIG_DYNAMIC;
    portConfigEllipse = PORT_CONFIG_ELLIPSE;
  }


  /**
   * <code>BoundsProvider</code> implementation that determines node port
   * bounds dynamically according to the location of the node port in relation
   * to its owner node.
   */
  private static final class DynamicBounds
          implements NodePort.BoundsProvider, NodePort.ContainsTest {
    private static final double EPS = 1e-8;
    private static final double SIZE = 10;

    public YRectangle getBounds( final NodePort port ) {
      final YPoint p = port.getLocation();
      final NodeRealizer owner = port.getRealizer();

      final double x = owner.getX();
      final double y = owner.getY();
      final double w = owner.getWidth();
      final double h = owner.getHeight();

      final double px = p.getX();
      final double py = p.getY();

      if (Math.abs(px - x) < EPS) {
        if (y <= py && py <= y + h) {
          // left
          return new YRectangle(px - SIZE, py - SIZE*0.5, SIZE, SIZE);
        }
      } else if (Math.abs(px - x - w) < EPS) {
        if (y <= py && py <= y + h) {
          // right
          return new YRectangle(px, py - SIZE*0.5, SIZE, SIZE);
        }
      } else if (Math.abs(py - y) < EPS) {
        if (x <= px && px <= x + w) {
          // top
          return new YRectangle(px - SIZE*0.5, py - SIZE, SIZE, SIZE);
        }
      } else if (Math.abs(py - y - h) < EPS) {
        if (x <= px && px <= x + w) {
          // bottom
          return new YRectangle(px - SIZE*0.5, py, SIZE, SIZE);
        }
      }

      return new YRectangle(px - SIZE*0.5, py - SIZE*0.5, SIZE, SIZE);
    }

    public boolean contains( final NodePort port, final double x, final double y ) {
      final YRectangle bnds = port.getBounds();
      return bnds.getX() <= x && x <= bnds.getX() + bnds.getWidth() &&
             bnds.getY() <= y && y <= bnds.getY() + bnds.getHeight();
    }
  }
}
