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
package demo.view.networkmonitoring;

import demo.view.DemoBase;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.DataMap;
import y.util.DefaultMutableValue2D;
import y.util.Maps;
import y.view.BackgroundRenderer;
import y.view.DefaultGraph2DRenderer;
import y.view.EditMode;
import y.view.NavigationMode;
import y.view.ViewAnimationFactory;
import y.view.YRenderingHints;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

/**
 * This demo shows a simple network monitoring tool. You can watch the traffic flow through the network
 * and even influence the network.
 * The network consists of PCs, Laptops, Tablets, Switches, Servers, Databases and W-LANS. The color of the edges
 * change from green to yellow to red depending on its traffic load. The traffic load of nodes is shown on their
 * info label. Active edges are marked through an animation.
 *
 * Things to try
 * <ul>
 * <li>Open/Hide Info Label: Every node has an info label. It can be shown and hidden by clicking on the node. It shows
 * the name, IP address and traffic load. You can also close it by clicking on the x in the top right corner.
 * Remember that info labels are only visible at a detailed zoom level.</li>
 * <li>Disable nodes: The info label also contains a power button in the bottom right corner. Clicking on it
 * (de)activates the node, preventing it from processing data. This way you can influence the data flow and watch
 * what happens.</li>
 * <li>Enable Failures: By Clicking on "Enable Failures" you allow failures to happen randomly. Nodes and edges get
 * broken and can't process data anymore. Broken elements are marked with a stop sign. If a failure happens on a node
 * or edge outside the current viewport, then the viewport will be moved to focus the broken element.
 * You can repair elements by clicking on the stop sign. To repair a node, you can also
 * open the info label and then click on the green arc arrow at the bottom right corner.</li>
 * </ul>
 */
public class NetworkMonitoringDemo extends DemoBase {
  static final double MAX_ZOOM = 3;
  static final double MIN_ZOOM = 0.15;
  static final double PAINT_DETAIL_THRESHOLD = 0.25;
  static final double LABEL_HIDE_ZOOM_LEVEL = 0.55;

  private NetworkModel model;
  private DataMap view2model;

  public NetworkMonitoringDemo() {
    this(null);
  }

  public NetworkMonitoringDemo(final String helpFilePath) {
    addHelpPane(helpFilePath);
    // start with a bigger view than usual
    view.setPreferredSize(new Dimension(1200, 900));

    final NetworkView networkView = new NetworkView(model, view, view2model);
    model.addObserver(networkView);

    addBackgroundRenderer();
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setDrawEdgesFirst(true);
    addZoomThreshold();

    // initially zoom in to avoid sloppy view in the beginning
    final Timer timer = new Timer(500, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final ViewAnimationFactory factory = new ViewAnimationFactory(view);
        final Rectangle graphBounds = view.getGraph2D().getBoundingBox();
        final DefaultMutableValue2D newCenter =
            DefaultMutableValue2D.create(graphBounds.getWidth() * 0.4, graphBounds.getHeight() * 0.4);
        final AnimationObject focus = factory.focusView(0.5, newCenter, 1000);
        final AnimationPlayer player = factory.createConfiguredPlayer();
        player.animate(focus);
      }
    });
    timer.setRepeats(false);
    timer.start();
  }

  /**
   * Overwritten to make sure that the model and the model-view-mapping are initialized before
   * {@link NetworkInteractionMode} is created and added to the view.
   */
  protected void initialize() {
    view2model = Maps.createHashedDataMap();
    model = new NetworkModelImpl(getResource("resource/network.graphml"));
  }

  /**
   * Adds a {@link BackgroundRenderer} that paints a gradient background to the view.
   */
  private void addBackgroundRenderer() {
    view.setBackgroundRenderer(new BackgroundRenderer() {
        public void paint(Graphics2D gfx, int x, int y, int w, int h) {
          final Rectangle visibleRect = view.getVisibleRect();
          //Store old paint
          final Paint oldPaint = gfx.getPaint();
          //Draw a rectangle in the visible area, using the GradientPainter.
          GradientPaint gradientPaint = new GradientPaint(visibleRect.x, visibleRect.y, Color.WHITE,
                        visibleRect.x + visibleRect.width, visibleRect.y + visibleRect.height, new Color(195, 211, 238));
          gfx.setPaint(gradientPaint);
          gfx.fillRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
          //Reset old paint
          gfx.setPaint(oldPaint);
        }
      });
  }

  /**
   * Adds thresholds to paint different details of the graph depending on the zoom level.
   */
  private void addZoomThreshold() {
    view.setPaintDetailThreshold(PAINT_DETAIL_THRESHOLD);
    view.getCanvasComponent().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Zoom")) {
          final double zoom = ((Double) evt.getNewValue()).doubleValue();
          if (zoom <= LABEL_HIDE_ZOOM_LEVEL) {
            view.getRenderingHints().put(
                YRenderingHints.KEY_NODE_LABEL_PAINTING,
                YRenderingHints.VALUE_NODE_LABEL_PAINTING_OFF);
          } else {
            view.getRenderingHints().put(
                YRenderingHints.KEY_NODE_LABEL_PAINTING,
                YRenderingHints.VALUE_NODE_LABEL_PAINTING_ON);
          }
          if (zoom < MIN_ZOOM) {
            view.setZoom(MIN_ZOOM);
          } else if (zoom > MAX_ZOOM) {
            view.setZoom(MAX_ZOOM);
          }
        }
      }
    });
  }

  /**
   * Overwritten because this demo is not editable.
   */
  protected EditMode createEditMode() {
    return null;
  }

  /**
   * Overwritten to register {@link NavigationMode} and a custom view mode that provides possible interactions for this
   * demo.
   */
  protected void registerViewModes() {
    view.addViewMode(new NavigationMode());
    view.addViewMode(new NetworkInteractionMode(model, view2model));
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          Locale.setDefault(Locale.ENGLISH);
          initLnF();
          (new NetworkMonitoringDemo("resource/networkhelp.html")).start("Network Monitoring Demo");
        }
      });
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
   * Overwritten to disable loading a graph because this demo is not editable and should only use the initial graph.
   */
  protected Action createLoadAction() {
    return null;
  }

  /**
   * Overwritten to disable saving a graph because this demo is not editable and the initial graph does not change.
   */
  protected Action createSaveAction() {
    return null;
  }

  /**
   * Overwritten to customize the toolbar for this demo. A button is added to enable/disable failures in the network.
   */
  protected JToolBar createToolBar() {
    final JToolBar toolBar = super.createToolBar();
    toolBar.add(new AbstractAction("Enable Failures") {
      public void actionPerformed(ActionEvent e) {
        if (model.isNetworkErrorsEnabled()) {
          putValue(Action.NAME,"Enable Failures");
          model.setNetworkErrorsEnabled(false);
        } else {
          putValue(Action.NAME,"Disable Failures");
          model.setNetworkErrorsEnabled(true);
        }
      }
    });
    return toolBar;
  }
}
