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
package demo.view.uml;

import y.base.Node;
import y.geom.YPoint;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A {@link y.view.Drawable} that draws a set of edge creation buttons.
 * <p>
 *   It is possible to show/hide the buttons with an animation that fans the buttons out or rolls them behind the node.
 *   To change the positions of the buttons during animation the {@link #setProgress(double) progress} must be set.
 * </p>
 */
class UmlEdgeCreationButtons implements Drawable {
  private static final int BUTTON_COUNT = 6;
  private static final int RADIUS = 15;
  private static final int DIAMETER = RADIUS * 2;
  private static final int GAP = 20;

  static final int TYPE_ASSOCIATION = 0;
  static final int TYPE_DEPENDENCY = 1;
  static final int TYPE_GENERALIZATION = 2;
  static final int TYPE_REALIZATION = 3;
  static final int TYPE_AGGREGATION = 4;
  static final int TYPE_COMPOSITION = 5;

  private final Graph2DView view;
  private final Graph2D graph;
  private final Node node;
  private final YPoint startOffset;
  private final double[] angles;
  private int selectedIndex;
  private double progress;

  UmlEdgeCreationButtons(Graph2DView view, final Node node) {
    this.view = view;
    this.node = node;
    this.graph = view.getGraph2D();

    // initialize start position and angles between start and end positions for the buttons
    // start at 22.5 degrees inside the node and then add 45 degrees more to every next end position
    startOffset = new YPoint(DIAMETER * -1.35, DIAMETER * 0.6);
    angles = new double[BUTTON_COUNT];
    for (int i = 0; i < BUTTON_COUNT; i++) {
      angles[i] = (i + 1) * 0.7853;
    }

    selectedIndex = -1;
    progress = 1;
  }

  public void paint(final Graphics2D graphics) {
    final Graphics2D gfx = (Graphics2D) graphics.create();

    try {
      // set a clip to avoid painting the buttons when they are behind the node
      gfx.clip(createClip(graphics));

      // scale graphics context for drawing the zoom-invariant buttons
      final double zoom = 1 / view.getZoom();
      gfx.scale(zoom, zoom);

      paintButtons(gfx);

    } finally {
      gfx.dispose();
    }
  }

  /**
   * Creates a {@link java.awt.Shape} that covers the whole area except the size and location of the associated node.
   * That way, the buttons are first covered by the node and seem to appear from behind the node.
   */
  private Shape createClip(final Graphics2D graphics) {
    final float outline = (UmlRealizerFactory.LINE_EDGE_CREATION_BUTTON_OUTLINE.getLineWidth()) / 2;
    final GeneralPath clip = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    final Rectangle2D.Double bounds = graph.getRealizer(node).getBoundingBox();
    clip.append(graphics.getClip(), true);
    final double x = bounds.getX();
    final double y = bounds.getY();
    final double w = bounds.getWidth();
    final double height = bounds.getHeight();
    final double buttonHeight = (DIAMETER + GAP) / view.getZoom();
    final double h = height < buttonHeight ? buttonHeight : height;

    clip.moveTo((float) (x - outline), (float) (y - outline));
    clip.lineTo((float) (x + w + outline), (float) (y - outline));
    clip.lineTo((float) (x + w + outline), (float) (y + h + outline));
    clip.lineTo((float) (x - outline), (float) (y + h + outline));
    clip.closePath();
    return clip;
  }

  /**
   * Paints the circular buttons to their current position depending on the node's position and the progress value.
   */
  private void paintButtons(final Graphics2D graphics) {
    final GeneralPath path = new GeneralPath();
    final Point2D position = new Point2D.Double(0, 0);
    for (int i = 0; i < BUTTON_COUNT; i++) {
      calcPosition(i, position);
      if (i != selectedIndex) {
        graphics.setColor(UmlRealizerFactory.COLOR_BACKGROUND);
      } else {
        graphics.setColor(UmlRealizerFactory.COLOR_SELECTION);
      }
      graphics.fillOval((int) position.getX(), (int) position.getY(), DIAMETER, DIAMETER);

      graphics.setColor(Color.DARK_GRAY);
      graphics.setStroke(LineType.LINE_2);
      graphics.drawOval((int) position.getX(), (int) position.getY(), DIAMETER, DIAMETER);

      paintIcon(graphics, position, i, path);
    }
  }

  /**
   * Calculates the positions of the buttons depending on the current time step. The result position is returned in the
   * passed point.
   */
  private void calcPosition(final int buttonIndex, final Point2D position) {
    final int part = buttonIndex / BUTTON_COUNT;
    final Point2D anchor = getAnchor();
    if (progress >= part) {
      final double angle = angles[buttonIndex] * (progress - part);
      final double offsetX = startOffset.getX() * Math.cos(angle) - startOffset.getY() * Math.sin(angle);
      final double offsetY = startOffset.getX() * Math.sin(angle) + startOffset.getY() * Math.cos(angle);

      position.setLocation(anchor.getX() + offsetX - RADIUS, anchor.getY() + offsetY - RADIUS);
    } else {
      position.setLocation(anchor.getX() + startOffset.getX() - RADIUS, anchor.getY() + startOffset.getY() - RADIUS);
    }
  }

  /**
   * Returns the upper-right corner of the associated node as anchor point for the buttons.
   */
  private Point2D getAnchor() {
    final NodeRealizer realizer = graph.getRealizer(node);
    final double zoom = view.getZoom();
    final double outline = (UmlRealizerFactory.LINE_EDGE_CREATION_BUTTON_OUTLINE.getLineWidth() * zoom) * 0.5;
    final double x = (realizer.getX() + realizer.getWidth()) * zoom + outline;
    final double y = realizer.getY() * zoom - outline;
    return new Point2D.Double(x, y);
  }

  /**
   * Paints the passed type of edge icon on the at the given position.
   *
   * @param graphics the current graphics context.
   * @param position the position of the icon.
   * @param type which icon to paint.
   * @param path a path object that can be used to paint the icon and also can be reused for the next icon.
   */
  private void paintIcon(final Graphics2D graphics, final Point2D position, final int type, final GeneralPath path) {
    path.reset();
    graphics.setColor(Color.DARK_GRAY);
    switch (type) {
      case TYPE_ASSOCIATION:
        graphics.setStroke(LineType.LINE_1);
        graphics.drawLine((int) (position.getX() + DIAMETER * 0.25), (int) (position.getY() + DIAMETER * 0.75),
            (int) (position.getX() + DIAMETER * 0.75), (int) (position.getY() + DIAMETER * 0.25));
        break;
      case TYPE_DEPENDENCY:
        graphics.setStroke(LineType.DASHED_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.25), (float) (position.getY() + DIAMETER * 0.75));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        graphics.draw(path);

        path.reset();
        graphics.setStroke(LineType.LINE_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.375));
        path.moveTo((float) (position.getX() + DIAMETER * 0.625), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        graphics.draw(path);

        break;
      case TYPE_GENERALIZATION:
        graphics.setStroke(LineType.LINE_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.25), (float) (position.getY() + DIAMETER * 0.75));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5625), (float) (position.getY() + DIAMETER * 0.4375));
        path.moveTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.375));
        path.lineTo((float) (position.getX() + DIAMETER * 0.625), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        graphics.draw(path);

        break;
      case TYPE_REALIZATION:
        graphics.setStroke(LineType.DASHED_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.25), (float) (position.getY() + DIAMETER * 0.75));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5625), (float) (position.getY() + DIAMETER * 0.4375));
        graphics.draw(path);

        path.reset();
        graphics.setStroke(LineType.LINE_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.375));
        path.lineTo((float) (position.getX() + DIAMETER * 0.625), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.625), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));
        graphics.draw(path);

        break;
      case TYPE_AGGREGATION:
        graphics.setStroke(LineType.LINE_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.3125), (float) (position.getY() + DIAMETER * 0.5625));
        path.lineTo((float) (position.getX() + DIAMETER * 0.25), (float) (position.getY() + DIAMETER * 0.75));
        path.lineTo((float) (position.getX() + DIAMETER * 0.4375), (float) (position.getY() + DIAMETER * 0.6875));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));

        graphics.draw(path);
        break;
      case TYPE_COMPOSITION:
        graphics.setStroke(LineType.LINE_1);
        path.moveTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.3125), (float) (position.getY() + DIAMETER * 0.5625));
        path.lineTo((float) (position.getX() + DIAMETER * 0.25), (float) (position.getY() + DIAMETER * 0.75));
        path.lineTo((float) (position.getX() + DIAMETER * 0.4375), (float) (position.getY() + DIAMETER * 0.6875));
        path.lineTo((float) (position.getX() + DIAMETER * 0.5), (float) (position.getY() + DIAMETER * 0.5));
        path.lineTo((float) (position.getX() + DIAMETER * 0.75), (float) (position.getY() + DIAMETER * 0.25));

        graphics.draw(path);
        graphics.fill(path);
    }
  }

  /**
   * Returns a rectangle that contains all buttons and some space around them.
   */
  public Rectangle getBounds() {
    final double zoom = view.getZoom();
    final Point2D position = new Point2D.Double(0, 0);
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (int i = 0; i < BUTTON_COUNT; i++) {
      calcPosition(i, position);

      // update bounds
      final int diameter = (int) (DIAMETER / zoom);
      final int gap = (int) (GAP / zoom);
      minX = Math.min(minX, position.getX() / zoom - gap);
      minY = Math.min(minY, position.getY() / zoom - gap);
      maxX = Math.max(maxX, position.getX() / zoom + gap + diameter);
      maxY = Math.max(maxY, position.getY() / zoom + gap + diameter);
    }

    final int x1 = (int) Math.floor(minX);
    final int y1 = (int) Math.floor(minY);
    final int x2 = (int) Math.ceil(maxX);
    final int y2 = (int) Math.ceil(maxY);
    return new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  /**
   * Returns the progress on the way from the start positions to the end positions.
   *
   * @return a number in the interval [0,1] that specifies the progress on the way from the start positions to the end
   *         positions.
   */
  public double getProgress() {
    return progress;
  }

  /**
   * Specifies the progress on the way from the start positions to the end positions.
   *
   * @param progress a number in the interval [0,1] that specifies the progress on the way from the start positions to
   *                 the end positions
   */
  public void setProgress(final double progress) {
    this.progress = progress;
  }

  /**
   * Returns the node that is associated with the buttons.
   *
   * @return the node that is associated with the buttons.
   */
  public Node getNode() {
    return node;
  }

  /**
   * Selects the button at the given coordinates.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   */
  public void selectButtonAt(final double x, final double y) {
    final int index = calculateButtonIndexAt(x, y);
    setSelectedIndex(index);
  }

  /**
   * Returns the index of the currently selected button.
   *
   * @return the index of the currently selected button.
   */
  public int getSelectedButtonIndex() {
    return selectedIndex;
  }

  /**
   * Selects the button at the given index.
   */
  public void setSelectedIndex(final int index) {
    this.selectedIndex = index;
  }

  /**
   * Checks whether or not there is a button at the given coordinates.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   *
   * @return <code>true</code> if there is a button at the given coordinates, <code>false</code> otherwise.
   */
  public boolean hasButtonAt(final double x, final double y) {
    final int index = calculateButtonIndexAt(x, y);
    return index >= 0;
  }

  /**
   * Determines the index of the button at the given coordinates.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   *
   * @return the index of the button at the given coordinates or <code>-1</code> if there is no button at that location.
   */
  private int calculateButtonIndexAt(final double x, final double y) {
    final double zoom = view.getZoom();

    final Point2D.Double position = new Point2D.Double(0, 0);
    for (int i = 0; i < BUTTON_COUNT; i++) {
      calcPosition(i, position);
      final double posX = (position.getX() + RADIUS) / zoom;
      final double posY = (position.getY() + RADIUS) / zoom;
      final double radius = RADIUS / zoom;
      if (radius > Math.sqrt((posX - x) * (posX - x) + (posY - y) * (posY - y))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Determines whether or not these edge creation buttons contain the given coordinates.
   */
  public boolean contains(final double x, final double y) {
    return getBounds().contains(x, y);
  }
}
