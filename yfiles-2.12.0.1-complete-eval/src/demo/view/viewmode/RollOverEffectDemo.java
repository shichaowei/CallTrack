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
import demo.view.DemoDefaults;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.Node;
import y.base.NodeMap;
import y.view.EditMode;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewRepaintManager;
import y.view.HitInfo;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.AutoDragViewMode;
import y.view.DefaultGraph2DRenderer;
import y.util.DefaultMutableValue2D;
import y.util.Value2D;
import y.view.YRenderingHints;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Locale;


/**
 * Demonstrates how to create a custom <code>ViewMode</code> that uses yFiles'
 * Animation Framework to produce a roll over effect for nodes under the mouse
 * cursor.
 *
 */
public class RollOverEffectDemo extends DemoBase {

  public RollOverEffectDemo() {
    final DefaultGraph2DRenderer g2dr = new DefaultGraph2DRenderer();
    g2dr.setDrawEdgesFirst(true);
    view.setGraph2DRenderer(g2dr);
    view.setPreferredSize(new Dimension(800, 600));
    view.getRenderingHints().put(ShapeNodeRealizer.KEY_SLOPPY_RECT_PAINTING,
        ShapeNodeRealizer.VALUE_SLOPPY_RECT_PAINTING_OFF);
    view.getRenderingHints().put(YRenderingHints.KEY_SLOPPY_POLYLINE_PAINTING,
        YRenderingHints.VALUE_SLOPPY_POLYLINE_PAINTING_OFF);
    loadInitialGraph();
  }

  protected void configureDefaultRealizers() {
    // painting shadows is expensive and therefore not well suited for animations
    DemoDefaults.registerDefaultNodeConfiguration(false);
    DemoDefaults.configureDefaultRealizers(view);
  }

  /**
   * Overwritten to register a roll over effect producing view mode.
   */
  protected void registerViewModes() {
    final EditMode editMode = createEditMode();
    if (editMode != null) {
      view.addViewMode(editMode);
    }
    view.addViewMode(new AutoDragViewMode());
    view.addViewMode(new RollOverViewMode());

    // disable label editing shortcut
    final Graph2DViewActions actions = new Graph2DViewActions(view);
    ActionMap amap = view.getCanvasComponent().getActionMap();
    amap.remove(Graph2DViewActions.EDIT_LABEL);
    InputMap imap = actions.createDefaultInputMap(amap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
  }

  /**
   * Loads a sample graph.
   */
  protected void loadInitialGraph() {
    loadGraph("resource/rollover.graphml");
  }


  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new RollOverEffectDemo()).start();
      }
    });
  }

  /**
   * A <code>ViewMode</code> that produces a roll over effect for nodes
   * under the mouse cursor.
   */
  private static final class RollOverViewMode extends ViewMode {
    /** Animation state constant */
    private static final int NONE = 0;
    /** Animation state constant */
    private static final int MARKED = 1;
    /** Animation state constant */
    private static final int UNMARK = 2;


    /** Preferred duration for roll over effect animations */
    private static final int PREFERRED_DURATION = 350;

    /** Scale factor for the roll over effect animations */
    private static final Value2D SCALE_FACTOR =
            DefaultMutableValue2D.create(3, 3);


    /** Stores the last node that was marked with the roll over effect */
    private Node lastHitNode;
    /** Stores the original size of nodes */
    private NodeMap size;
    /** Stores the animation state of nodes */
    private NodeMap state;

    private ViewAnimationFactory factory;
    private AnimationPlayer player;

    /**
     * Triggers a rollover effect for the first node at the specified location.
     */
    public void mouseMoved( final double x, final double y ) {
      final HitInfo hi = getHitInfo(x, y);
      if (hi.hasHitNodes()) {
        final Node node = (Node) hi.hitNodes().current();
        if (node != lastHitNode) {
          unmark(lastHitNode);
        }
        if (state.getInt(node) == NONE) {
          mark(node);
          lastHitNode = node;
        }
      } else {
        unmark(lastHitNode);
        lastHitNode = null;
      }
    }

    /**
     * Overwritten to initialize/dispose this <code>ViewMode</code>'s
     * helper data.
     */
    public void activate( final boolean b ) {
      if (b) {
        factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
        player = factory.createConfiguredPlayer();
        size = view.getGraph2D().createNodeMap();
        state = view.getGraph2D().createNodeMap();
      } else {
        view.getGraph2D().disposeNodeMap(state);
        view.getGraph2D().disposeNodeMap(size);
        state = null;
        size = null;
        player = null;
        factory = null;
      }
      super.activate(b);
    }

    /**
     * Overwritten to take only nodes into account for hit testing.
     */
    protected HitInfo getHitInfo( final double x, final double y ) {
      final HitInfo hi = DemoBase.checkNodeHit(view, x, y);
      setLastHitInfo(hi);
      return hi;
    }

    /**
     * Triggers a <em>mark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>MARKED</em>.
     */
    protected void mark( final Node node ) {
      // only start a mark animation if no other animation is playing
      // for the given node
      if (state.getInt(node) == NONE) {
        state.setInt(node, MARKED);

        final NodeRealizer nr = getGraph2D().getRealizer(node);
        size.set(node, DefaultMutableValue2D.create(nr.getWidth(), nr.getHeight()));
        final AnimationObject ao = factory.scale(
                nr,
                SCALE_FACTOR,
                ViewAnimationFactory.APPLY_EFFECT,
                PREFERRED_DURATION);
        player.animate(AnimationFactory.createEasedAnimation(ao));
      }
    }

    /**
     * Triggers an <em>unmark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>UNMARKED</em>.
     */
    protected void unmark( final Node node ) {
      if (node == null) {
        return;
      }

      // only start an unmark animation if the node is currently marked
      // (or in the process of being marked)
      if (state.getInt(node) == MARKED) {
        state.setInt(node, UNMARK);

        final Value2D oldSize = (Value2D) size.get(node);
        final NodeRealizer nr = getGraph2D().getRealizer(node);
        final AnimationObject ao = factory.resize(
                nr,
                oldSize,
                ViewAnimationFactory.APPLY_EFFECT,
                PREFERRED_DURATION);
        final AnimationObject eao = AnimationFactory.createEasedAnimation(ao);
        player.animate(new Reset(eao, node, nr, oldSize));
      }
    }

    /**
     * Custom animation object that resets node size and state upon disposal.
     */
    private final class Reset implements AnimationObject {
      private AnimationObject ao;
      private final Node node;
      private final NodeRealizer nr;
      private final Value2D oldSize;

      Reset(
              final AnimationObject ao,
              final Node node,
              final NodeRealizer nr,
              final Value2D size
      ) {
        this.ao = ao;
        this.node = node;
        this.nr = nr;
        this.oldSize = size;
      }

      public void initAnimation() {
        ao.initAnimation();
      }

      public void calcFrame( final double time ) {
        ao.calcFrame(time);
      }

      /**
       * Resets the target node to its original size and its animation state
       * to <em>NONE</em>.
       */
      public void disposeAnimation() {
        ao.disposeAnimation();
        nr.setSize(oldSize.getX(), oldSize.getY());
        size.set(node, null);
        state.setInt(node, NONE);
      }

      public long preferredDuration() {
        return ao.preferredDuration();
      }
    }
  }
}
