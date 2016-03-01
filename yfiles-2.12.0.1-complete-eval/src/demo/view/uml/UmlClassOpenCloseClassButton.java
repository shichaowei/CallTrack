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

import y.base.NodeList;
import y.geom.YDimension;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * An {@link UmlClassButton} that opens or closes the class on mouse click.
 */
class UmlClassOpenCloseClassButton extends UmlClassButton {
  public static final double ICON_SIZE = 16;
  public static final double ICON_GAP = 5;

  /**
   * Checks whether or not the button is currently visible. The button is always visible.
   */
  protected boolean isVisible(final NodeRealizer context) {
    return true;
  }

  /**
   * Paints the button with its icon.
   */
  public void paint(final NodeRealizer context, final Graphics2D graphics) {
    if (isVisible(context)) {
      final Rectangle2D area = getButtonArea(context);
      paintButton(context, graphics, area);
      paintIcon(context, graphics, area);
    }
  }

  /**
   * Returns the area where to paint the button with its icon.
   */
  protected Rectangle2D getButtonArea(final NodeRealizer context) {
    return new Rectangle2D.Double(
        context.getX() + context.getWidth() - ICON_SIZE - ICON_GAP,
        context.getY() + ICON_GAP,
        ICON_SIZE,
        ICON_SIZE);
  }

  /**
   * Paints the button into the given area.
   */
  private void paintButton(final NodeRealizer context, final Graphics2D graphics, final Rectangle2D area) {
    final Color color = getButtonColor(context);
    graphics.setColor(color);
    graphics.fill(area);
  }

  /**
   * Returns the color of the button.
   */
  private Color getButtonColor(final NodeRealizer context) {
    return isMouseOverButton(context) ?
        UmlRealizerFactory.COLOR_BUTTON_BACKGROUND_ACTIVE :
        UmlRealizerFactory.COLOR_BUTTON_BACKGROUND_BLANK;
  }

  /**
   * Checks whether or not the mouse is currently over this button. The button where the mouse is currently over is
   * stored as an appropriate style property of the given context.
   */
  private boolean isMouseOverButton(final NodeRealizer context) {
    return UmlRealizerFactory.getMouseOverButton(context) == UmlRealizerFactory.BUTTON_OPEN_CLOSE_CLASS_SECTIONS;
  }

  /**
   * Paints the icon into the given area.
   */
  private void paintIcon(final NodeRealizer context, final Graphics2D graphics, final Rectangle2D area) {
    final Shape shape = getIconShape(context, area);
    graphics.setColor(UmlRealizerFactory.COLOR_BUTTON_FOREGROUND_ENABLED);
    graphics.setStroke(LineType.LINE_1);
    graphics.draw(shape);
  }

  /**
   * Returns the shape of the icon.
   */
  private Shape getIconShape(final NodeRealizer context, final Rectangle2D area) {
    if (UmlClassLabelSupport.getModel(context).areSectionsVisible()) {
      return getCloseShape(area);
    } else {
      return getOpenShape(area);
    }
  }

  /**
   * Returns the shape of the icon of the closed state.
   */
  private Shape getCloseShape(final Rectangle2D area) {
    final float minX = (float) (area.getX() + area.getWidth() * 0.25);
    final float midX = (float) (area.getX() + area.getWidth() * 0.5);
    final float maxX = (float) (area.getX() + area.getWidth() * 0.75);
    final float minY = (float) (area.getY() + area.getHeight() * 0.25);
    final float midY = (float) (area.getY() + area.getHeight() * 0.5);
    final float maxY = (float) (area.getY() + area.getHeight() * 0.75);

    final GeneralPath icon = new GeneralPath();
    icon.moveTo(minX, midY);
    icon.lineTo(midX, minY);
    icon.lineTo(maxX, midY);
    icon.moveTo(minX, maxY);
    icon.lineTo(midX, midY);
    icon.lineTo(maxX, maxY);

    return icon;
  }

  /**
   * Returns the shape of the icon of the opened state.
   */
  private Shape getOpenShape(final Rectangle2D area) {
    final float minX = (float) (area.getX() + area.getWidth() * 0.25);
    final float midX = (float) (area.getX() + area.getWidth() * 0.5);
    final float maxX = (float) (area.getX() + area.getWidth() * 0.75);
    final float minY = (float) (area.getY() + area.getHeight() * 0.25);
    final float midY = (float) (area.getY() + area.getHeight() * 0.5);
    final float maxY = (float) (area.getY() + area.getHeight() * 0.75);

    final GeneralPath icon = new GeneralPath();
    icon.moveTo(minX, minY);
    icon.lineTo(midX, midY);
    icon.lineTo(maxX, minY);
    icon.moveTo(minX, midY);
    icon.lineTo(midX, maxY);
    icon.lineTo(maxX, midY);
    return icon;
  }

  /**
   * Toggles the visibility of the class sections on button click.
   */
  protected void buttonClicked(final NodeRealizer context, final Graph2DView view) {
    final Graph2D graph = view.getGraph2D();
    graph.firePreEvent();
    try {
      graph.backupRealizers(new NodeList(context.getNode()).nodes());
      graph.backupRealizers(context.getNode().edges());
      final boolean isOpened = UmlClassLabelSupport.getModel(context).areSectionsVisible();
      final UmlClassAnimation animation = new OpenCloseClassAnimation(view, context, isOpened);
      animation.play();
    } finally {
      graph.firePostEvent();
    }
  }

  /**
   * Sets an appropriate style property of the given context to notify that the mouse is currently over this button.
   */
  protected void buttonEntered(final NodeRealizer context, final Graph2DView view) {
    UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_OPEN_CLOSE_CLASS_SECTIONS);
    view.updateView();
  }

  /**
   * Animates the opening and closing of the details of the class.
   */
  private static class OpenCloseClassAnimation extends UmlClassAnimation  {
    private OpenCloseClassAnimation(final Graph2DView view, final NodeRealizer context, final boolean isClosing) {
      super(view, context, isClosing);
    }

    /**
     * Closes the class details.
     */
    protected void close() {
      UmlClassLabelSupport.getModel(context).setSectionsVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Opens the class details.
     */
    protected void open() {
      UmlClassLabelSupport.getModel(context).setSectionsVisible(true);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Returns the size of the realizer when the class details are closed.
     */
    protected YDimension getClosedSize() {
      final boolean isVisible = UmlClassLabelSupport.getModel(context).areSectionsVisible();
      UmlClassLabelSupport.getModel(context).setSectionsVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);

      final YDimension dimension = new YDimension(context.getWidth(), context.getHeight());
      UmlClassLabelSupport.getModel(context).setSectionsVisible(isVisible);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
      return dimension;
    }

    /**
     * The lower y-coordinate of the fixed part of the realizer is the bottom of its name label.
     */
    protected double getFixedY() {
      final NodeLabel nameLabel = UmlClassLabelSupport.getNameLabel(context);
      return nameLabel.getLocation().getY() + nameLabel.getHeight();
    }

    /**
     * The upper y-coordinate of the moving part of the realizer is the bottom of its last label.
     */
    protected double getMovingY() {
      double fixedY = getFixedY();
      for (int i = 0; i < context.labelCount(); i++) {
        final NodeLabel label = context.getLabel(i);
        fixedY = Math.max(fixedY, label.getLocation().getY() + label.getHeight());
      }
      return fixedY;
    }
  }
}
