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
package demo.view.anim;

import demo.view.DemoBase;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;

import javax.swing.Action;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * Demonstrates how to visually fade-in newly created nodes and
 * fade-out deleted nodes. This nice animation effect is triggered by a
 * special <code>GraphListener</code> implementation.
 * Note that this demo makes use of the yFiles class <code>
 * Graph2DViewRepaintManager</code> to increase the speed
 * of the animation effect.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/animation.html">Section Animations for Graph Elements</a> in the yFiles for Java Developer's Guide
 */
public class FadeInFadeOutDemo extends DemoBase {
  private static final long PREFERRED_DURATION = 500;

  /**
   * Creates a new FadeInFadeOutDemo and initializes the AnimationPlayer for
   * the fade effects.
   */
  public FadeInFadeOutDemo() {
    view.getGraph2D().addGraphListener(new FadeHandler(view));
  }

  protected Action createLoadAction() {
    //Overridden method to disable the Load menu in the demo
    return null;
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo
    return null;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new FadeInFadeOutDemo()).start("Fade Demo");
      }
    });
  }

  /**
   * Triggers fading effects on node creation and node removal.
   */
  private static final class FadeHandler implements GraphListener {
    private AnimationPlayer player;
    private ViewAnimationFactory factory;

    public FadeHandler(Graph2DView view) {
      factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
      player = factory.createConfiguredPlayer();
    }

    public void onGraphEvent(final GraphEvent e) {
      final Graph2D graph = (Graph2D) e.getGraph();
      switch (e.getType()) {
        case GraphEvent.NODE_CREATION:
        case GraphEvent.NODE_REINSERTION: {
          final NodeRealizer nr = graph.getRealizer((Node) e.getData());
          nr.setVisible(false);
          player.animate(factory.fadeIn(nr, PREFERRED_DURATION));
          break;
        }
        case GraphEvent.PRE_NODE_REMOVAL: {
          final NodeRealizer nr = graph.getRealizer((Node) e.getData());

          // let's create a drawable, so the animation can run no matter
          // if the node is in the graph or not
          final Drawable dnr = ViewAnimationFactory.createDrawable(nr);
          nr.setVisible(false);

          final AnimationObject fadeOut = factory.fadeOut(dnr, PREFERRED_DURATION);

          // let's start the animation with some delay so edges "vanish"
          // before nodes
          player.animate(
              AnimationFactory.createSequence(
                  AnimationFactory.createPause(100), fadeOut));
          break;
        }
      }
    }
  }
}
