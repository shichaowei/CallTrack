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
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.Geom;
import y.util.DefaultMutableValue2D;
import y.util.MutableValue2D;
import y.util.Value2DSettable;
import y.view.Arrow;
import y.view.BezierEdgeRealizer;
import y.view.Drawable;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.SmartEdgeLabelModel;
import y.view.ViewAnimationFactory;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;


/**
 * Demonstrates how to animate label movement along an edge.
 */
public class LabelAnimationDemo {
  private static final int PREFERRED_DURATION = 10000;

  private final Graph2DView view;
  private EdgeLabel[] labels;
  private Timer timer;

  /**
   * Creates a new LabelAnimationDemo and initializes a Timer that triggers the
   * animation effects.
   */
  public LabelAnimationDemo() {
    this.view = new Graph2DView();
    this.view.setFitContentOnResize(true);
    init();
  }

  /**
   * Initializes a <code>Graph2D</code> to hold edges along whose sides labels
   * should be moved.
   * Creates the labels to be moved.
   */
  private void init() {
    final Graph2D graph = view.getGraph2D();

    // self loop
    Node node;
    node = graph.createNode(225, 125);
    graph.getRealizer(node).setSize(0, 0);

    EdgeRealizer er;
    er = graph.getRealizer(graph.createEdge(node, node));
    er.setLineColor(Color.LIGHT_GRAY);
    er.clearBends();
    er.appendBend(225, 25);


    final BezierEdgeRealizer ber = new BezierEdgeRealizer();
    ber.appendBend(100, 0);
    ber.appendBend(200, 400);
    ber.appendBend(300, 100);
    ber.setTargetArrow(Arrow.DELTA);
    ber.setLineColor(Color.LIGHT_GRAY);
    graph.createEdge(graph.createNode(50, 200), graph.createNode(400, 200),
        ber.createCopy());

    ber.clearBends();
    ber.appendBend(300, 250);
    ber.appendBend(200, 550);
    ber.appendBend(100, 250);
    graph.createEdge(graph.createNode(400, 350), graph.createNode(50, 350),
        ber.createCopy());


    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      graph.getRealizer(nc.node()).setVisible(false);
    }

    final String[] labelTexts = {
        "Selfloop",
        "A Label",
        "Another Label"
    };

    labels = new EdgeLabel[labelTexts.length];

    final EdgeCursor ec = graph.edges();
    for (int i = 0, n = labelTexts.length; i < n; ++i, ec.next()) {
      labels[i] = new EdgeLabel(labelTexts[i]);
      final SmartEdgeLabelModel model = new SmartEdgeLabelModel();
      labels[i].setLabelModel(model, model.getDefaultParameter());
      labels[i].bindRealizer(graph.getRealizer(ec.edge()));
    }

    node = graph.createNode(10, 10);
    graph.getRealizer(node).setSize(0, 0);

    er = graph.getRealizer(graph.createEdge(node, node));
    er.setLineColor(Color.LIGHT_GRAY);
    er.clearBends();
    er.appendBend(470, 10);
    er.appendBend(470, 470);
    er.appendBend(10, 470);


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
   * Plays the movement animation.
   *
   * @param invert   if <code>true</code> the labels move from target to source;
   *                 otherwise the labels move from source to target.
   */
  private void play(final boolean invert) {
    final Graph2D graph = view.getGraph2D();
    EdgeRealizer er;

    final EdgeCursor ec = graph.edges();
    int i = 0;

    final ViewAnimationFactory factory = new ViewAnimationFactory(view);

    // let's start with a simple variant:
    // move a label along an edge with the center of the label's
    // bounding box being centered on the edge path

    // get the realizer of the edge along which we want to move a label
    er = graph.getRealizer(ec.edge());
    ec.next();

    // create a Drawable representation of the label we want to move.
    final Drawable selfloopLabel = ViewAnimationFactory.createDrawable(labels[i++]);

    // create an animation that moves the previously created Drawable along
    // the chosen edge path
    final AnimationObject selfloopAnimation =
        factory.traversePath(er.getPath(), false, selfloopLabel, true, false, PREFERRED_DURATION);

    // get the realizer of the edge along which we want to move a label
    er = graph.getRealizer(ec.edge());
    ec.next();

    // create a Drawable representation of the label we want to move.
    final Drawable standardLabel = ViewAnimationFactory.createDrawable(labels[i++]);

    // create an animation that moves the previously created Drawable along
    // the chosen edge path
    final AnimationObject standardAnimation =
        factory.traversePath(er.getPath(), invert, standardLabel, true, true, PREFERRED_DURATION);

    // now we want to create a slightly more complex animation:
    // we want the animation to start with the label's left end at the
    // path's start point;
    // we want the animation to stop, when the label's right end reaches
    // the path's end point;
    // and finally, we do not want the label to move on the edge path,
    // but alongside it

    // get the realizer that provides the edge path
    er = graph.getRealizer(ec.edge());
    ec.next();
    final GeneralPath path = er.getPath();

    // create a Drawable representation of the label we want to move.
    final AnimationDrawable offsetLabel = new AnimationDrawable(ViewAnimationFactory.createDrawable(labels[i]));

    // create a custom AnimationObject that updates the x offset of the
    // previously created Drawable to match out start/stop requirements
    final AnimationObject offsetAnimation = new AnimationObject() {
      private final AnimationObject delegate =
          factory.traversePath(path, invert,
              offsetLabel.getPositionMutable(),
              offsetLabel.getDirectionMutable(),
              PREFERRED_DURATION);
      
      private final Value2DSettable internalOffset =
          offsetLabel.getOffsetMutable();

      public void initAnimation() {
        graph.addDrawable(offsetLabel);
        delegate.initAnimation();
      }

      public void calcFrame(final double time) {
        // the inverted label animation on the offsetLabel starts from left
        // therefore, the offset also has to start with its maximum
        internalOffset.setX(invert ? time : 1.0 - time);
        delegate.calcFrame(time);
      }

      public void disposeAnimation() {
        delegate.disposeAnimation();
        graph.removeDrawable(offsetLabel);
      }

      public long preferredDuration() {
        return delegate.preferredDuration();
      }
    };


    er = graph.getRealizer(ec.edge());
    final GeneralPath border = er.getPath();

    final CompositeAnimationObject arrows = AnimationFactory.createConcurrency();

    final AnimationObject NO_TIME = AnimationFactory.createPause(0);
    final long pause = PREFERRED_DURATION / 19;
    final long rest = PREFERRED_DURATION % 19;
    final long duration = pause * 10 + rest;
    for (int j = 0; j < 10; ++j) {
      final Drawable arrowDrawable = new Drawable() {
        private final Arrow arrow = Arrow.STANDARD;

        public void paint(final Graphics2D gfx) {
          final Color oldColor = gfx.getColor();
          gfx.setColor(Color.GRAY);
          arrow.paint(gfx, 0, 0, 1, 0);
          gfx.setColor(oldColor);
        }

        public Rectangle getBounds() {
          return arrow.getShape().getBounds();
        }
      };

      final CompositeAnimationObject sequence = AnimationFactory.createLazySequence();
      sequence.addAnimation(AnimationFactory.createPause(j * pause));
      sequence.addAnimation(
          factory.traversePath(border, false, arrowDrawable, true, false,
              duration));
      sequence.addAnimation(NO_TIME);
      arrows.addAnimation(sequence);
    }

    final CompositeAnimationObject concurrency = AnimationFactory.createConcurrency();
    concurrency.addAnimation(selfloopAnimation);
    concurrency.addAnimation(standardAnimation);
    concurrency.addAnimation(offsetAnimation);
    concurrency.addAnimation(arrows);

    final AnimationPlayer player = factory.createConfiguredPlayer();
    player.animate(concurrency);
  }

  /**
   * Creates an application  frame for this demo
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
        (new LabelAnimationDemo()).start("Label Animations");
      }
    });
  }


  /**
   * A <code>Drawable</code> implementation that supports movement and
   * rotation.
   */
  private static final class AnimationDrawable implements Drawable {
    private final Drawable wrappee;
    private final Rectangle bounds;

    /** externally specifiable position */
    private MutableValue2D position;

    /**
     * determines the relative offset of the position with regards to the
     * left side of the drawable's bounding box
     * its value on x and y ranges from 0 to 1
     */
    private MutableValue2D offset;

    /** vector governing the rotation */
    private MutableValue2D direction;

    AnimationDrawable(final Drawable wrappee) {
      this.wrappee = wrappee;
      final Rectangle wrappeeBounds = wrappee.getBounds();
      this.position = DefaultMutableValue2D.create(wrappeeBounds.getCenterX(),
          wrappeeBounds.getCenterY());
      this.offset = DefaultMutableValue2D.create(0, 1);
      this.direction = DefaultMutableValue2D.create();
      this.bounds = new Rectangle(wrappeeBounds);
    }

    public Rectangle getBounds() {
      final Rectangle wrappeeBounds = wrappee.getBounds();
      final double w = Math.ceil(wrappeeBounds.getWidth());
      final double h = Math.ceil(wrappeeBounds.getHeight());

      final double x = position.getX();
      final double y = position.getY();

      // The transformation is a rotation around the point (x,y)
      final AffineTransform transform = new AffineTransform();
      transform.translate(x, y);
      transform.rotate(radians());
      transform.translate(-x, -y);

      Geom.calcTransformedBounds(x - w * offset.getX(),
          y - h * offset.getY(),
          w, h, transform, bounds);
      return bounds;
    }

    public void paint(final Graphics2D gfx) {
      final AffineTransform oldAt = gfx.getTransform();
      final Rectangle wrappeeBounds = wrappee.getBounds();
      // rotate the graph around the location of the calculated animated label
      gfx.translate(position.getX(), position.getY());
      gfx.rotate(radians());
      // After the above two steps, one could draw the wrappee at (0,0).
      // However, with (wrappeeBounds.x, wrappeeBounds.y) the label has a different position from (0,0),
      // therefore, we translate the graph by the negative location of wrappee.
      gfx.translate(-wrappeeBounds.x, -wrappeeBounds.y);
      // And add an offset to additionally have the wrappee's new location
      // moved left by the maximal amount of its width if offset.getX() is 1
      // and moved up by the maximum amount of its height if offset.getY() is 1
      gfx.translate(-wrappeeBounds.getWidth() * offset.getX(), -wrappeeBounds.getHeight() * offset.getY());
      wrappee.paint(gfx);
      gfx.setTransform(oldAt);
    }

    Value2DSettable getPositionMutable() {
      return position;
    }

    Value2DSettable getDirectionMutable() {
      return direction;
    }

    Value2DSettable getOffsetMutable() {
      return offset;
    }

    private static final double PI_HALF = Math.PI * 0.5;

    private double radians() {
      double radians = Math.atan2(direction.getY(), direction.getX());
      if (radians < -PI_HALF) {
        radians += Math.PI;
      } else if (radians > PI_HALF) {
        radians -= Math.PI;
      }
      return radians;
    }
  }
}
