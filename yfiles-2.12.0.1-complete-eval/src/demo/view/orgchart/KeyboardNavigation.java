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
package demo.view.orgchart;

import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.util.DefaultMutableValue2D;
import y.view.Graph2DView;
import y.view.ViewAnimationFactory;

import javax.swing.Timer;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;

/**
 * Factory class that provides {@link java.awt.event.KeyListener} implementations suitable for
 * navigating within Graph2DView.
 */
public class KeyboardNavigation {

  private Graph2DView view;
  
  /**
   * Creates a KeyboardNavigation for the given view.
   * @param view given view
   */
  public KeyboardNavigation(final Graph2DView view) {
    this.view = view;
  }
  
  /**
   * Creates and returns a KeyListener that zooms into the view
   * while keyCode1 or keyCode2 is being pressed.
   */
  public KeyListener createZoomInKeyListener(final int keyCode1, final int keyCode2 ) 
  {
    return new ZoomTrigger(true, keyCode1, keyCode2);
  }


  /**
   * Creates and returns a KeyListener that zooms out of the view
   * while keyCode1 or keyCode2 is being pressed.
   */
  public KeyListener createZoomOutKeyListener(final int keyCode1, final int keyCode2 ) 
  {
    return new ZoomTrigger(false, keyCode1, keyCode2);
  }

  /**
   * Creates and returns a KeyListener that moves the view port up
   * while keyCode is being pressed.
   */
  public KeyListener createMoveViewportUpKeyListener(final int keyCode) 
  {
    return new MoveViewportTrigger(MoveViewportTrigger.UP, keyCode);        
  }

  /**
   * Creates and returns a KeyListener that moves the view port down
   * while keyCode is being pressed.
   */
  public KeyListener createMoveViewportDownKeyListener(final int keyCode) 
  {
    return new MoveViewportTrigger(MoveViewportTrigger.DOWN, keyCode);
  }
  

  /**
   * Creates and returns a KeyListener that moves the view port to the left
   * while keyCode is being pressed.
   */
  public KeyListener createMoveViewportLeftKeyListener(final int keyCode) 
  {
    return new MoveViewportTrigger(MoveViewportTrigger.LEFT, keyCode);
  }

  /**
   * Creates and returns a KeyListener that moves the view port to the right
   * while keyCode is being pressed.
   */
  public KeyListener createMoveViewportRightKeyListener(final int keyCode) 
  {
    return new MoveViewportTrigger(MoveViewportTrigger.RIGHT, keyCode);
  }
  
  /**
   * KeyListener base used for all implementation provided by this class.
   * It uses the yFiles animation framework to perform smooth navigation
   * effects.
   */
  private abstract class AnimationTrigger extends KeyAdapter {
    private AnimationPlayer player;
    private DisarmableAnimationWrapper wrapper;

    private final int keyCode1;
    private final int keyCode2;
    private final Timer timer;

    AnimationTrigger( final int keyCode1, final int keyCode2  ) {
      this.keyCode1 = keyCode1;
      this.keyCode2 = keyCode2;
      this.timer = new Timer(0, new ActionListener() {
        public void actionPerformed( final ActionEvent e ) {
          if (player != null && player.isPlaying()) {
            wrapper.disarm();
            player.stop();
          }
        }
      });
      this.timer.setInitialDelay(50);
      this.timer.setRepeats(false);
    }

    public void keyPressed( final KeyEvent e ) {
      if (player == null) {
        player = new AnimationPlayer(false);
        player.addAnimationListener(view);
      }

      final int keyCode = e.getKeyCode();
      if ((keyCode1 == keyCode || keyCode2 == keyCode) &&
          (0 == e.getModifiersEx())) {
        timer.stop();
        if (!player.isPlaying()) {
          wrapper = new DisarmableAnimationWrapper(createAnimation());
          if (wrapper.preferredDuration() > 0) {
            player.animate(wrapper);
          }
        }
      }
    }

    public void keyReleased( final KeyEvent e ) {
      final int keyCode = e.getKeyCode();
      if ((keyCode1 == keyCode || keyCode2 == keyCode) &&
          (0 == e.getModifiersEx())) {
        if (!timer.isRunning()) {
          timer.restart();
        }
      }
    }

    abstract AnimationObject createAnimation();
  }

  /**
   * KeyListener implementation that performs zooming
   */
  class ZoomTrigger extends AnimationTrigger {
    private ViewAnimationFactory factory;
    private final boolean zoomIn;

    ZoomTrigger( final boolean zoomIn, final int keyCode1, final int keyCode2 ) {
      super(keyCode1, keyCode2);
      this.zoomIn = zoomIn;
    }

    AnimationObject createAnimation() {
      final double newZoom = calculateZoom();
      if ((zoomIn && newZoom > view.getZoom()) || (!zoomIn && newZoom < view.getZoom())) {
        if (factory == null) {
          factory = new ViewAnimationFactory(view);
        }
        return factory.zoom(newZoom, ViewAnimationFactory.APPLY_EFFECT, 1000);
      } else {
        return null;
      }
    }

    private double calculateZoom() {
      if (zoomIn) {
        return 4;
      } else {
        final Point2D oldP = view.getViewPoint2D();
        final double oldZoom = view.getZoom();
        view.fitContent();
        final double fitContentZoom = view.getZoom();
        view.setZoom(oldZoom);
        view.setViewPoint2D(oldP.getX(), oldP.getY());
        return fitContentZoom;
      }
    }
  }


  /**
   * KeyListener implementation that moves the view port of the view
   */
  private class MoveViewportTrigger extends AnimationTrigger {
    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int UP = 2;
    static final int DOWN = 3;

    private int direction;
    private ViewAnimationFactory factory;

    MoveViewportTrigger( final int direction, final int keyCode ) {
      super(keyCode, -1);
      this.direction = direction;
    }

    AnimationObject createAnimation() {
      final Rectangle bx = view.getGraph2D().getBoundingBox();
      final double dx = Math.max(bx.getWidth(), 10000);
      final double dy = Math.max(bx.getHeight(), 10000);

      final Point2D oldCenter = view.getCenter();
      final DefaultMutableValue2D newCenter =
              DefaultMutableValue2D.create(oldCenter.getX(), oldCenter.getY());

      double dist = 0;
      switch (direction) {
        case LEFT:
          newCenter.setX(bx.getX() - dx);
          dist = oldCenter.getX() - newCenter.getX();
          break;
        case RIGHT:
          newCenter.setX(bx.getX() + bx.getWidth() + dx);
          dist = newCenter.getX() - oldCenter.getX();
          break;
        case UP:
          newCenter.setY(bx.getY() - dy);
          dist = oldCenter.getY() - newCenter.getY();
          break;
        case DOWN:
          newCenter.setY(bx.getY() + bx.getHeight() + dy);
          dist = newCenter.getY() - oldCenter.getY();
          break;
      }

      if (dist > 1e-4) {
        if (factory == null) {
          factory = new ViewAnimationFactory(view);
        }
        return factory.moveCamera(newCenter, (long) Math.rint(dist * view.getZoom()));
      } else {
        return null;
      }
    }
  }
  
  /**
   * Animation object wrapper that can be used to disarm an AnimationObject during execution.
   */
  private static final class DisarmableAnimationWrapper implements AnimationObject {
    private AnimationObject animation;

    private boolean armed;

    DisarmableAnimationWrapper( final AnimationObject animation ) {
      this.animation = animation;
      this.armed = true;
    }

    public void initAnimation() {
      if (armed && animation != null) {
        animation.initAnimation();
      }
    }

    public void calcFrame( final double time ) {
      if (armed && animation != null) {
        animation.calcFrame(time);
      }
    }

    public void disposeAnimation() {
      if (armed && animation != null) {
        animation.disposeAnimation();
      }
    }

    public long preferredDuration() {
      return animation != null ? animation.preferredDuration() : 0;
    }

    void disarm() {
      armed = false;
    }
  }

}
