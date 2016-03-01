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
package demo.view.isometry;

import java.awt.geom.Rectangle2D;

/**
 * The class stores the height, width and depth of a solid figure. This data is used to get its bounds in the view space
 * and the bounds of its base area used in the layout space.
 */
public class IsometryData implements Cloneable {
  // Matrix to transform points from the layout space into the view space.
  public static final double M_TO_VIEW_11 = Math.sqrt(3) * 0.5;
  public static final double M_TO_VIEW_12 = M_TO_VIEW_11;
  public static final double M_TO_VIEW_21 = -0.5;
  public static final double M_TO_VIEW_22 = 0.5;

  // Matrix to transform points from the view space into the layout space.
  public static final double M_TO_LAYOUT_11 = 1 / Math.sqrt(3);
  public static final double M_TO_LAYOUT_12 = -1;
  public static final double M_TO_LAYOUT_21 = M_TO_LAYOUT_11;
  public static final double M_TO_LAYOUT_22 = -M_TO_LAYOUT_12;

  // Indices for the corners of the bounding box.
  public static final int C0_X = 0; // lower left
  public static final int C0_Y = 1;
  public static final int C1_X = 2; // lower front
  public static final int C1_Y = 3;
  public static final int C2_X = 4; // lower right
  public static final int C2_Y = 5;
  public static final int C3_X = 6; // lower back
  public static final int C3_Y = 7;
  public static final int C4_X = 8; // upper left
  public static final int C4_Y = 9;
  public static final int C5_X = 10; // upper front
  public static final int C5_Y = 11;
  public static final int C6_X = 12; // upper right
  public static final int C6_Y = 13;
  public static final int C7_X = 14; // upper back
  public static final int C7_Y = 15;

  private double width;
  private double height;
  private double depth;

  private boolean isHorizontal;

  /**
   * Creates an instance with the given dimensions.
   */
  public IsometryData(final double width, final double depth, final double height, final boolean isHorizontal) {
    this.width = width;
    this.depth = depth;
    this.height = height;
    this.isHorizontal = isHorizontal;
  }

  /**
   * Returns the width of the solid figure.
   */
  public double getWidth() {
    return width;
  }

  /**
   * Sets the width of the solid figure.
   */
  public void setWidth(final double width) {
    this.width = width;
  }

  /**
   * Returns the depth of the solid figure.
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Sets the depth of the solid figure.
   */
  public void setDepth(final double depth) {
    this.depth = depth;
  }

  /**
   * Returns the height of the solid figure.
   */
  public double getHeight() {
    return height;
  }

  /**
   * Sets the height of the solid figure.
   */
  public void setHeight(final double height) {
    this.height = height;
  }

  /**
   * Determines whether or no the base of the solid figure is horizontal in layout space.
   * This is important for labels that may be rotated during layout.
   */
  public boolean isHorizontal() {
    return isHorizontal;
  }

  /**
   * Specifies whether or no the base of the solid figure is horizontal in layout space.
   * This is important for labels that may be rotated during layout.
   */
  public void setHorizontal(boolean horizontal) {
    isHorizontal = horizontal;
  }

  /**
   * Calculates the bounds of the solid figure in the view space.
   *
   * @param bounds the calculated bounds
   */
  public void calculateViewBounds(final Rectangle2D bounds) {
    final double[] corners = new double[16];
    calculateCorners(corners);
    calculateViewBounds(corners, bounds);
  }

  /**
   * Calculates the bounds of the solid figure in the view space.
   *
   * @param corners the corners of the projection of the bounds of solid figure into the view space
   * @param bounds  the calculated bounds
   */
  public static void calculateViewBounds(final double[] corners, final Rectangle2D bounds) {
    double minX = corners[C0_X];
    double minY = corners[C0_Y];
    double maxX = corners[C0_X];
    double maxY = corners[C0_Y];
    for (int i = 2; i < corners.length; i += 2) {
      minX = Math.min(minX, corners[i]);
      minY = Math.min(minY, corners[i + 1]);
      maxX = Math.max(maxX, corners[i]);
      maxY = Math.max(maxY, corners[i + 1]);
    }
    bounds.setFrame(minX, minY, maxX - minX, maxY - minY);
  }

  /**
   * Calculates the corners of the projection of the bounds of solid figure into the view space.
   *
   * @param corners the calculated corners.
   */
  public void calculateCorners(final double[] corners) {
    corners[C0_X] = 0;
    corners[C0_Y] = 0;

    corners[C1_X] = toViewX(getWidth(), 0);
    corners[C1_Y] = toViewY(getWidth(), 0);

    corners[C2_X] = toViewX(getWidth(), getDepth());
    corners[C2_Y] = toViewY(getWidth(), getDepth());

    corners[C3_X] = toViewX(0, getDepth());
    corners[C3_Y] = toViewY(0, getDepth());

    for (int i = 0; i < 8; i += 2) {
      corners[i + 8] = corners[i];
      corners[i + 9] = corners[i + 1] - getHeight();
    }
  }

  /**
   * Transforms the given point from the layout space into the view space.
   *
   * @param layoutX x-coordinate in layout space
   * @param layoutY y-coordinate in layout space
   * @return x-coordinate in view space
   */
  static double toViewX(final double layoutX, final double layoutY) {
    return M_TO_VIEW_11 * layoutX + M_TO_VIEW_12 * layoutY;
  }

  /**
   * Transforms the given point from the layout space into the view space.
   *
   * @param layoutX x-coordinate in layout space
   * @param layoutY y-coordinate in layout space
   * @return y-coordinate in view space
   */
  static double toViewY(final double layoutX, final double layoutY) {
    return M_TO_VIEW_21 * layoutX + M_TO_VIEW_22 * layoutY;
  }

  /**
   * Transforms the given point from the view space into the layout space.
   *
   * @param viewX x-coordinate in view space
   * @param viewY y-coordinate in view space
   * @return x-coordinate in layout space
   */
  static double toLayoutX(final double viewX, final double viewY) {
    return M_TO_LAYOUT_11 * viewX + M_TO_LAYOUT_12 * viewY;
  }

  /**
   * Transforms the given point from the view space into the layout space.
   *
   * @param viewX x-coordinate in view space
   * @param viewY y-coordinate in view space
   * @return y-coordinate in layout space
   */
  static double toLayoutY(final double viewX, final double viewY) {
    return M_TO_LAYOUT_21 * viewX + M_TO_LAYOUT_22 * viewY;
  }

  /**
   * Translates the given corner to the given location, so that the upper left location of the bounds of the given
   * corners is on the given location.
   *
   * @param x       x-coordinate of the location where the corners should be moved to
   * @param y       y-coordinate of the location where the corners should be moved to
   * @param corners corners to be moved
   */
  public static void moveTo(final double x, final double y, final double[] corners) {
    // Calculate the upper left location of the bounds of the given corners.
    double minX = corners[C0_X];
    double minY = corners[C0_Y];
    for (int i = 2; i < corners.length; i += 2) {
      minX = Math.min(minX, corners[i]);
      minY = Math.min(minY, corners[i + 1]);
    }

    // Move the corners to the given location.
    final double dx = x - minX;
    final double dy = y - minY;
    for (int i = 0; i < corners.length; i += 2) {
      corners[i] += dx;
      corners[i + 1] += dy;
    }
  }

  public Object clone () throws CloneNotSupportedException {
    return super.clone();
  }
}
