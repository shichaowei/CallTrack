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
package demo.view.hierarchy;

import org.w3c.dom.Document;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GraphMLIOHandler;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DTraversal;
import y.view.Graph2DViewRepaintManager;
import y.view.HitInfo;
import y.view.NodeRealizer;
import y.view.ProxyShapeNodeRealizer;
import y.view.ViewMode;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GroupNodePainter;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Demonstrates how to use a custom {@link y.view.ViewMode} in order to fade in the group state icon when the mouse is
 * placed over a group/folder node and fade it out when the mouse leaves a group/folder node.
 * <p/>
 * This <code>ViewMode</code> determines whether the mouse moves over a group/folder node and then starts an animation
 * that changes a style property of the group node realizer which contains the opacity value for the icon.
 */
public class FadingGroupStateIconDemo extends GroupingDemo {

  /**
   * Overwritten to register a custom view mode that shows/hides the group state icon.
   */
  protected void registerViewModes() {
    super.registerViewModes();
    view.addViewMode(new FadingIconViewMode());
  }

  /**
   * Handles the deserialization of the {@link GroupNodePainter#GROUP_STATE_STYLE_ID} by setting
   * the opacity to zero for all nodes when loading the graphML file.
   * @return the <code>GraphMLIOHandler</code>
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    return new GraphMLIOHandler(){
      public void read(Graph2D graph, InputStream in) throws IOException {
        super.read(graph, in);
        setTransparentGroupState(graph);
      }

      public void read(Graph2D graph, Document documentElement) throws IOException {
        super.read(graph, documentElement);
        setTransparentGroupState(graph);
      }

      private void setTransparentGroupState(Graph2D graph) {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          setOpacity(graph.getRealizer(nc.node()), 0);
        }
      }
    };
  }

  /**
   * Configures the default group node realizers with an opacity value of 0.
   */
  protected void configureDefaultGroupNodeRealizers() {
    // If the opacity value would be set for the default group node realizer,
    // all added group nodes would share the same style property instance.
    // As the default group node realizer here does not have the style property, a
    // new style property is set for every added node.
    getHierarchyManager().setGraphFactory(new DefaultHierarchyGraphFactory() {
      public NodeRealizer createNodeRealizer(Object hint) {
        final NodeRealizer nr = super.createNodeRealizer(hint);
        setOpacity(nr, 0);
        return nr;
      }
    });
    super.configureDefaultGroupNodeRealizers();
  }

  /**
   * Sets the opacity of the {@link GroupNodePainter#GROUP_STATE_STYLE_ID} and creates a new style property when
   * needed. If the realizer is a <code>ProxyShapeNodeRealizer</code> the opacity is set for all its delegates.
   * @param realizer the realizer the property is set for
   * @param opacity the opacity
   */
  private static void setOpacity(NodeRealizer realizer, float opacity) {
    if (realizer instanceof ProxyShapeNodeRealizer) {
      // If the realizer is a proxy the style property is set recursively for all delegates.
      final ProxyShapeNodeRealizer proxyRealizer = (ProxyShapeNodeRealizer) realizer;
      for (int i = 0; i < proxyRealizer.realizerCount(); i++) {
        setOpacity(proxyRealizer.getRealizer(i), opacity);
      }
    } else {
      // Set or create the FadingIconValue
      GroupNodePainter.GroupStateStyle style = (GroupNodePainter.GroupStateStyle) ((GenericNodeRealizer) realizer).getStyleProperty(
          GroupNodePainter.GROUP_STATE_STYLE_ID);
      if (style == null) {
        style = new GroupNodePainter.GroupStateStyle();
        ((GenericNodeRealizer) realizer).setStyleProperty(GroupNodePainter.GROUP_STATE_STYLE_ID, style);
      }
      style.setOpacity(opacity);
    }
  }

  /**
   * Launches the <code>CustomGroupViewModeDemo</code>.
   * @param args not used
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new FadingGroupStateIconDemo()).start();
      }
    });
  }

  /**
   * A custom {@link y.view.ViewMode} which fades in the group state icon when the mouse moves over a group/folder node
   * and fades out the group state icon when the mouse when the mouse leaves the group/folder node.
   */
  public class FadingIconViewMode extends ViewMode {

    /** Player to animate the fading. */
    private AnimationPlayer player;

    /** Last seen node to be able to unset its property when leaving. */
    private Node lastSeenNode;

    /** Repaint manager that executes repaints during animations. */
    private Graph2DViewRepaintManager repaintManager;

    /** Last fade in animation to be able to abort the fading */
    private FadingIconAnimation lastFadeInAnimation;

    /**
     * Creates a <code>FadingIconViewMode</code>.
     */
    public FadingIconViewMode() {
      player = new AnimationPlayer(false);

      // As view is not set at creation time listening to the appropriate property change events
      // allows us to create a repaint manager as soon as the necessary view instance is available.
      addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          if (ACTIVE_VIEW_PROPERTY.equals(e.getPropertyName())) {
            if (repaintManager != null) {
              player.removeAnimationListener(repaintManager);
            }
            repaintManager = new Graph2DViewRepaintManager(view);
            player.addAnimationListener(repaintManager);
          }
        }
      });
    }

    /**
     * Determines if the mouse moves over a group or folder node and starts the animation to fade
     * in/out the group state icon.
     */
    public void mouseMoved(double x, double y) {
      final Graph2D graph = view.getGraph2D();
      if (graph.getHierarchyManager() != null) {
        final HitInfo hitInfo =
            view.getHitInfoFactory().createHitInfo(x, y, Graph2DTraversal.NODES, true);

        // determine whether the mouse moves over a group/folder node
        Node n = null;
        if (hitInfo.hasHitNodes()) {
          n = hitInfo.getHitNode();
          if (graph.getHierarchyManager().isNormalNode(n)) {
            if (graph.getHierarchyManager().getParentNode(n) == lastSeenNode) {
              n = lastSeenNode;
            } else {
              n = null;
            }
          }
        }

        //fade out
        if (lastSeenNode != n && lastSeenNode != null && lastSeenNode.getGraph() != null) {
          if (lastFadeInAnimation != null) {
            lastFadeInAnimation.setActive(false);
          }

          final NodeRealizer lastSeenNodeRealizer = ((Graph2D) lastSeenNode.getGraph()).getRealizer(lastSeenNode);
          player.animate(new FadingIconAnimation(lastSeenNodeRealizer, false));

          lastSeenNode = null;
        }

        //fade in
        if (lastSeenNode != n && n != null) {
          final NodeRealizer hitNodeRealizer = ((Graph2D) n.getGraph()).getRealizer(n);

          final FadingIconAnimation animation = new FadingIconAnimation(hitNodeRealizer, true);
          player.animate(animation);

          lastFadeInAnimation = animation;
          lastSeenNode = n;
        }
      }
    }

    /**
     * An {@link AnimationObject} that animates fading of the group state icon. It sets the transparency value of
     * the icon using a style property.
     */
    private final class FadingIconAnimation implements AnimationObject {

      /** Maximum duration of the fading animation */
      private static final int FADE_DURATION = 500;

      /** Start value of the {@link GroupNodePainter#GROUP_STATE_STYLE_ID} value. */
      private float startValue;

      /** Realizer of the group/folder node whose state icon fades. */
      private NodeRealizer realizer;

      /** Determines if it is a fade in or a fade out animation */
      private boolean fadeIn;

      /** Determines if the animation is still active */
      private boolean active;

      /**
       * Creates a <code>FadingIconAnimation</code> for a given realizer.
       * @param realizer the realizer
       * @param fadeIn   <code>true</code> if it is a fade in animation, <code>false</code> otherwise.
       */
      public FadingIconAnimation(NodeRealizer realizer, boolean fadeIn) {
        this.realizer = realizer;
        this.fadeIn = fadeIn;
        this.active = true;
      }

      /**
       * Initializes fading either with transparency zero (fade in) or with the last transparency value (fade out).
       */
      public void initAnimation() {
        if (repaintManager != null) {
          repaintManager.add(realizer);
        }

        if (fadeIn) {
          // Set a new fading icon property with start value 0 = fully transparent
          setOpacity(realizer, 0);
        } else {
          // Start at the transparency value where the last fade in animation ended
          startValue = getOpacity(realizer);
        }
      }

      /**
       * Calculates the transparency value for the group state icon.
       * @param time a point in [0.0, 1.0]
       */
      public void calcFrame(double time) {
        if (active) {
          if (fadeIn) {
            setOpacity(realizer, (float) time);
          } else {
            setOpacity(realizer, startValue * (1 - (float) time));
          }
        }
      }

      public void disposeAnimation() {
        if (repaintManager != null) {
          repaintManager.remove(realizer);
        }
      }

      /**
       * Returns the preferred duration of the animation which is shorter if the fade in animation was aborted early.
       * @return the preferred duration of the animation
       */
      public long preferredDuration() {
        if (fadeIn) {
          return FADE_DURATION;
        } else {
          if (startValue > 0) {
            // if the fade in animation was aborted, shorten the fade out animation
            return (long) (FADE_DURATION * startValue);
          } else {
            return 0;
          }
        }
      }

      /**
       * Specifies whether the animation is active or not. If the animation is not active, {@link #calcFrame(double)}
       * will do nothing.
       * @param active the state of this animation
       */
      public void setActive(boolean active) {
        this.active = active;
      }

      /**
       * Retrieves the value of the {@link GroupNodePainter#GROUP_STATE_STYLE_ID} for the given realizer.
       * @param realizer the realizer
       * @return the current value of the style property
       */
      private float getOpacity(NodeRealizer realizer) {
        NodeRealizer r = getDelegateRealizer(realizer);
        return ((GroupNodePainter.GroupStateStyle) ((GenericNodeRealizer) r).getStyleProperty(
            GroupNodePainter.GROUP_STATE_STYLE_ID)).getOpacity();
      }

      /**
       * Gets the current delegate realizer of the <code>ProxyShapeNodeRealizer</code> of the node or the node's realizer
       * if no proxy is used.
       * @param realizer the <code>NodeRealizer</code> that could be a <code>ProxyShapeNodeRealizer</code>
       * @return the delegate realizer or the given realizer itself
       */
      private GenericNodeRealizer getDelegateRealizer(NodeRealizer realizer) {
        if (realizer instanceof ProxyShapeNodeRealizer) {
          return (GenericNodeRealizer) ((ProxyShapeNodeRealizer) realizer).getRealizerDelegate();
        } else {
          return (GenericNodeRealizer) realizer;
        }
      }
    }
  }
}