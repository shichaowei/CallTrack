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
package demo.view.viewmode;

import demo.view.DemoBase;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YRectangle;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.HtmlLabelConfiguration;
import y.view.Mouse2DEvent;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.TooltipMode;
import y.view.YLabel;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Demonstrates how to use {@link HtmlLabelConfiguration} to trigger and process
 * hyperlink events with HTML formatted label text.
 * <p>
 * When clicking on an external link such as
 * <blockquote>
 * <code>&lt;a href="http://www.yworks.com/products/yfiles"&gt;yFiles for Java&lt;/a&gt;</code>,
 * </blockquote>
 * a dialog is opened that displays the link's destination in response to the
 * generated hyperlink event.
 * </p>
 * <p>
 * Additionally, a custom protocol <code>graph</code> is used to allow
 * in-graph navigation. E.g. clicking on
 * <blockquote>
 * <code>&lt;a href="graph://yfilesforjava"&gt;yFiles for Java&lt;/a&gt;</code>
 * </blockquote>
 * will navigate to the first node in the graph that has a corresponding
 * <blockquote>
 * <code>&lt;a name="yfilesforjava"&gt;&lt;/a&gt;</code>
 * </blockquote>
 * declaration in its label text.
 * </p>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizer_related.html#labels_config_hyperlink">Section Realizer-Related Features</a> in the yFiles for Java Developer's Guide
 */
public class HyperlinkDemo extends DemoBase {
  private static final String HTML_LABEL_CONFIG = "HtmlLabel";
  private boolean fractionMetricsForSizeCalculationEnabled;

  public HyperlinkDemo() {
    loadInitialGraph();
  }

  protected void loadInitialGraph() {
    loadGraph("resource/HyperlinkDemo.graphml");
  }

  protected void initialize() {
    super.initialize();

    // Ensures that text always fits into label bounds independent of zoom level.
    // Stores the value to be able to reset it when running the demo in the DemoBrowser,
    // so this setting cannot effect other demos.
    fractionMetricsForSizeCalculationEnabled = YLabel.isFractionMetricsForSizeCalculationEnabled();
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
    YLabel.setFractionMetricsForSizeCalculationEnabled(fractionMetricsForSizeCalculationEnabled);
  }

  /**
   * Overwritten to register a label configuration for HTML formatted label
   * text.
   */
  protected void configureDefaultRealizers() {
    final YLabel.Factory f = NodeLabel.getFactory();
    final HtmlLabelConfiguration impl = new HtmlLabelConfiguration();
    final Map impls = f.createDefaultConfigurationMap();
    impls.put(YLabel.Painter.class, impl);
    impls.put(YLabel.Layout.class, impl);
    impls.put(YLabel.BoundsProvider.class, impl);
    f.addConfiguration(HTML_LABEL_CONFIG, impls);

    super.configureDefaultRealizers();
  }

  /**
   * Overwritten to create an edit mode that triggers and processes hyperlink
   * events for labels.
   * @return a {@link HyperlinkEditMode} instance.
   */
  protected EditMode createEditMode() {
    return new HyperlinkEditMode();
  }

  /**
   * Overwritten to disable tooltips.
   */
  protected TooltipMode createTooltipMode() {
    return null;
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new HyperlinkDemo()).start();
      }
    });
  }

  /**
   * Triggers hyperlink events for mouse moved and mouse clicked events that
   * occur for hyperlinks in HTML formatted label text.
   */
  private static final class HyperlinkEditMode extends EditMode {
    HyperlinkEditMode() {
      allowResizeNodes(false);
      allowNodeCreation(false);
    }

    /**
     * Processes the specified hyperlink event.
     * @param e the hyperlink event to process.
     */
    protected void hyperlinkUpdate( final HyperlinkEvent e ) {
      (new EventHandler(view)).hyperlinkUpdate(e);
    }

    /**
     * Checks whether or not the node click actually occurred on one of the
     * node's label. If that is the case and the label uses a HTML configuration
     * for measuring and rendering its content, the configuration's
     * <code>handleLabelEvent</code> method is used to trigger a corresponding
     * hyperlink event.
     * @see HtmlLabelConfiguration#handleLabelEvent(y.view.YLabel, y.view.Mouse2DEvent, javax.swing.event.HyperlinkListener)
     */
    protected void nodeClicked(
            final Graph2D graph,
            final Node node,
            final boolean wasSelected,
            final double x,
            final double y,
            final boolean modifierSet
    ) {
      final NodeRealizer nr = graph.getRealizer(node);
      if (nr.labelCount() > 0) {
        for (int i = nr.labelCount(); i --> 0;) {
          final NodeLabel nl = nr.getLabel(i);
          if (nl.contains(x, y)) {
            if (labelClickedImpl(nl, x, y)) {
              return;
            }
          }
        }
      }
      super.nodeClicked(graph, node, wasSelected, x, y, modifierSet);
    }

    /**
     * Triggers hyperlink events for the specified label.
     */
    protected void labelClicked(
            final Graph2D graph,
            final YLabel label,
            final boolean wasSelected,
            final double x, final double y,
            final boolean modifierSet
    ) {
      if (labelClickedImpl(label, x, y)) {
        return;
      }
      super.labelClicked(graph, label, wasSelected, x, y, modifierSet);
    }

    /**
     * Synthesizes mouse events that may trigger hyperlink events for the
     * specified label.
     * @param label the label that has been clicked upon.
     * @param x the x-component of the mouse click's <em>world</em> coordinate.
     * @param y the y-component of the mouse click's <em>world</em> coordinate.
     * @return <code>true</code> if the click triggered a hyperlink event;
     * <code>false</code> otherwise.
     */
    private boolean labelClickedImpl(
            final YLabel label,
            final double x, final double y
    ) {
      if (HTML_LABEL_CONFIG.equals(label.getConfiguration())) {
        final HtmlLabelConfiguration htmlSupport = getHtmlConfiguration();
        final EventHolder callback = new EventHolder();
        // a "real" mouse click always results in a pressed, a released, and a
        // clicked event
        // because handleLabelEvent relies on the JComponent used for rendering
        // the label to trigger hyperlink event, the same event sequence
        // that would occur for a "real" mouse click is used as there is
        // no way to know whether the JComponent reacts to released or to
        // clicked events
        // e.g. using JEditorPane, the default HTMLEditorKit will fire
        // a hyperlink activated event in response to a mouse clicked event
        // however, JWebEngine's com.inet.html.InetHtmlEditorKit fires
        // hyperlink activated events in response to mouse release events
        htmlSupport.handleLabelEvent(
                label,
                createEvent(x, y, lastReleaseEvent, MouseEvent.MOUSE_PRESSED),
                null);
        htmlSupport.handleLabelEvent(
                label,
                createEvent(x, y, lastReleaseEvent, MouseEvent.MOUSE_RELEASED),
                callback);
        htmlSupport.handleLabelEvent(
                label,
                createEvent(x, y, lastReleaseEvent, MouseEvent.MOUSE_CLICKED),
                callback);
        final HyperlinkEvent e = callback.getEvent();
        if (e != null) {
          hyperlinkUpdate(e);
          return true;
        }
      }
      return false;
    }


    /**
     * Checks whether or not the mouse was moved over a hyperlink in the
     * HTML formatted text of a label.
     * @param x the x-component of the mouse event's world coordinate.
     * @param y the y-component of the mouse event's world coordinate.
     */
    public void mouseMoved( final double x, final double y ) {
      // first check whether or not the event happened over a label
      final HitInfo info = view.getHitInfoFactory().createHitInfo(
              x, y, Graph2DTraversal.NODE_LABELS, true);
      if (info.hasHitNodeLabels()) {
        final NodeLabel label = info.getHitNodeLabel();
        // now check whether the label uses a HTML configuration
        if (HTML_LABEL_CONFIG.equals(label.getConfiguration())) {
          final HtmlLabelConfiguration htmlSupport = getHtmlConfiguration();
          final EventHolder callback = new EventHolder();
          // finally check whether or not the mouse moved into or out of
          // a hyperlink
          htmlSupport.handleLabelEvent(
                  label,
                  createEvent(x, y, lastMoveEvent, MouseEvent.MOUSE_MOVED),
                  callback);
          final HyperlinkEvent e = callback.getEvent();
          if (e != null) {
            if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
              // change the cursor to let the user know that something will
              // happen if the mouse is clicked at the current location
              view.setViewCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
              view.setViewCursor(Cursor.getDefaultCursor());
            }
          }
          return;
        }
      }

      super.mouseMoved(x, y);
    }


    /**
     * Creates a <code>Mouse2DEvent</code> for the specified world coordinates
     * and triggering mouse event.
     * @param x the x-component of the event's world coordinate.
     * @param y the y-component of the event's world coordinate.
     * @param e the triggering mouse event.
     * @param id the type of <code>Mouse2DEvent</code> to create.
     * @return a <code>Mouse2DEvent</code> for the specified world coordinates
     * and triggering mouse event.
     */
    private Mouse2DEvent createEvent(
            final double x, final double y,
            final MouseEvent e,
            final int id
    ) {
      return new Mouse2DEvent(
              e.getSource(),
              this,
              id,
              e.getWhen(),
              e.getModifiersEx(),
              x,
              y,
              e.getButton(),
              e.getClickCount(),
              e.isPopupTrigger());
    }

    private static HtmlLabelConfiguration getHtmlConfiguration() {
      return (HtmlLabelConfiguration) NodeLabel.getFactory().getImplementation(
              HTML_LABEL_CONFIG, YLabel.Painter.class);
    }
  }

  /**
   * Caches hyperlink events.
   */
  private static class EventHolder implements HyperlinkListener {
    private HyperlinkEvent event;

    /**
     * Caches the specified hyperlink event.
     * @param e the event to cache.
     */
    public void hyperlinkUpdate( final HyperlinkEvent e ) {
      event = e;
    }

    /**
     * Returns the cached event. 
     * @return the cached event.
     */
    HyperlinkEvent getEvent() {
      return event;
    }
  }

  /**
   * Processes {@link HtmlLabelConfiguration.LabelHyperlinkEvent}s.
   */
  private static class EventHandler implements HyperlinkListener {
    private final Graph2DView view;

    EventHandler( final Graph2DView view ) {
      this.view = view;
    }

    /**
     * Processes the specified hyperlink event.
     * @param e the hyperlink event to process.
     */
    public void hyperlinkUpdate( final HyperlinkEvent e ) {
      // determine if it is a label hyperlink event
      if (e instanceof HtmlLabelConfiguration.LabelHyperlinkEvent) {
        // determine if the event is triggered from a link that uses the demo's
        // custom "graph" protocol that can be used to navigate the current
        // graph
        if (isGraphNavigationEvent(e)) {
          navigateTo((HtmlLabelConfiguration.LabelHyperlinkEvent) e);
        } else {
          displayExternalLink((HtmlLabelConfiguration.LabelHyperlinkEvent) e);
        }
      }
    }

    /**
     * Determines whether or not the specified event is a
     * <em>graph navigation</em> event, that is whether or not the event's link
     * uses the demo's custom <code>graph</code> protocol.
     * @param e the event to check.
     * @return <code>true</code> the event's link uses the demo's custom
     * <code>graph</code> protocol; <code>false</code> otherwise.
     */
    private boolean isGraphNavigationEvent( final HyperlinkEvent e ) {
      final URL url = e.getURL();
      if (url == null) {
        final String desc = e.getDescription();
        return desc != null && desc.startsWith("graph://");
      } else {
        return "graph".equals(url.getProtocol());
      }
    }

    /**
     * Displays a dialog with the specified event's hyperlink destination. 
     * @param e the event whose hyperlink destination has to be displayed.
     */
    private void displayExternalLink( final HtmlLabelConfiguration.LabelHyperlinkEvent e ) {
      final YLabel label = e.getLabel();
      final YRectangle lbox = label.getBox();
      final Point l = view.getLocationOnScreen();
      final int vx = l.x + view.toViewCoordX(lbox.getX());
      final int vy = l.y + view.toViewCoordY(lbox.getY() + lbox.getHeight());

      final String title = "External Link";
      final String message =
              title +
              "\nHref: " + e.getDescription();
      final JOptionPane jop = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
      final JDialog jd = jop.createDialog(view, title);
      jd.setLocation(vx, vy);
      jd.setVisible(true);
    }

    /**
     * Navigates to the node that is referenced in the specified event's
     * hyperlink destination.
     * @param e a hyperlink event whose hyperlink uses the demo's customs
     * <code>graph</code> protocol.
     */
    private void navigateTo( final HtmlLabelConfiguration.LabelHyperlinkEvent e ) {
      final String destination;
      final URL url = e.getURL();
      if (url == null) {
        destination = e.getDescription().substring(8);
      } else {
        destination = url.getPath();
      }

      // search for a node that has an anchor which corresponds to the
      // desired destination
      final Graph2D g = view.getGraph2D();
      for (NodeCursor nc = g.nodes(); nc.ok(); nc.next()) {
        final NodeRealizer nr = g.getRealizer(nc.node());
        if (nr.labelCount() > 0) {
          final String s = nr.getLabelText();
          if (s.indexOf("<a name=\"" + destination + "\">") > -1) {
            navigateTo(nr);
            break;
          }
        }
      }
    }

    /**
     * Focuses the specified node context in the demo's graph view.
     * @param realizer the node context.
     */
    private void navigateTo( final NodeRealizer realizer ) {
      view.setViewCursor(Cursor.getDefaultCursor());
      final double z = view.getZoom();
      final double cx = realizer.getCenterX();
      final double cy = realizer.getCenterY();
      view.focusView(z, new Point2D.Double(cx, cy), true);
    }
  }
}
