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

import demo.view.DemoDefaults;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.util.DefaultMutableValue2D;
import y.util.Value2D;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeRealizer;
import y.view.ViewAnimationFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Demonstrates usage and effects of ease in and/or ease out for animation
 * effects.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/animation.html">Section Animations for Graph Elements</a> in the yFiles for Java Developer's Guide
 */
public class EaseInEaseOutDemo {
  private static final int PREFERRED_DURATION = 2000;

  private final Graph2DView view;
  private Value2D[][] positions;
  private Timer timer;

  AnimationPlayer player;
  ViewAnimationFactory factory;

  /**
   * Creates a new EaseInEaseOutDemo and initializes a Timer that triggers the
   * animation effects.
   */
  public EaseInEaseOutDemo() {
    this.view = new Graph2DView();
    DemoDefaults.configureDefaultRealizers(view);
    init();    
  }
 
  /**
   * Initializes the start and end points for the animated movement effects.
   * Creates nodes to demonstrate animated movement.
   */
  private void init() {
    positions = new Value2D[4][2];
    for (int i = 0, n = positions.length; i < n; ++i) {
      positions[i][0] = DefaultMutableValue2D.create(70, 110 + i * 60);
      positions[i][1] = DefaultMutableValue2D.create(410, 110 + i * 60);
    }

    final String[] labels = {
        "Normal", "Ease In", "Ease In, Ease Out", "Ease Out"
    };

    final Graph2D graph = view.getGraph2D();

    for (int i = 0, n = positions.length; i < n; ++i) {
      final Value2D pos = positions[i][0];
      final NodeRealizer nr = graph.getRealizer(
          graph.createNode(pos.getX(), pos.getY()));
      nr.setSize(120, 30);
      nr.setLabelText(labels[i]);
    }


    timer = new Timer(PREFERRED_DURATION + 500,
        new ActionListener() {
          private boolean invert;

          public void actionPerformed(final ActionEvent e) {
            play(invert);
            invert = !invert;
          }
        });
    timer.setInitialDelay(1000);
    timer.start();
  }

  /**
   * Plays the movement animation for the nodes in the graph.
   * Four different kinds of movement animations are created:
   * <ul>
   * <li> normal (i.e. no ease effect) </li>
   * <li> ease in </li>
   * <li> ease in and ease out </li>
   * <li> ease out </li>
   * </ul>
   *
   * @param invert   if <code>true</code> the nodes move from right to left;
   *                 otherwise the nodes move from left to right.
   */
  private void play(final boolean invert) {
    final Graph2D graph = view.getGraph2D();

    if (factory == null) {
      factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
    }

    // we want to play all four animations at the same time
    final CompositeAnimationObject moves = AnimationFactory.createConcurrency();

    for (int i = 0, n = positions.length; i < n; ++i) {
      final Value2D dest = positions[i][invert ? 0 : 1];
      final NodeRealizer nr = graph.getRealizer(graph.getNodeArray()[i]);

      // create a movement effect from the realizer's current position to
      // the specified destination
      AnimationObject move =
          factory.move(nr, dest, ViewAnimationFactory.APPLY_EFFECT,
              PREFERRED_DURATION);

      switch (i) {
        case 1:
          // create an ease in effect
          move = AnimationFactory.createEasedAnimation(move, 1, 1);
          break;
        case 2:
          // create an ease in and ease out effect
          move = AnimationFactory.createEasedAnimation(move);
          break;
        case 3:
          // create an ease out effect
          move = AnimationFactory.createEasedAnimation(move, 0, 0);
          break;
      }

      // register the individual animations for concurrent processing
      moves.addAnimation(move);
    }

    if (player == null) {
      player = factory.createConfiguredPlayer();
    }
    // play the animations
    player.animate(moves);
  }


  /**
   * Creates an application frame for this demo
   * and displays it. The given string is the title of
   * the displayed frame.
   */
  private void start(final String title) {
    final JFrame frame = new JFrame(title);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo(final JRootPane rootPane) {
    final JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(view, BorderLayout.CENTER);

    rootPane.setContentPane(contentPane);
  }

  public void dispose() {
    if (timer != null) {
      if (timer.isRunning()) {
        timer.stop();
      }
      timer = null;
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        (new EaseInEaseOutDemo()).start("Ease Demo");
      }
    });
  }
}
