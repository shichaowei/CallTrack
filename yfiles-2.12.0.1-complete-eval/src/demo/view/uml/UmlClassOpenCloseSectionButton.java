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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * An {@link UmlClassButton} that opens or closes the section of attributes or operations on mouse click.
 */
class UmlClassOpenCloseSectionButton extends UmlClassButton {
  private static final double ICON_SIZE = 12;
  private static final double GAP = 5;

  private final boolean isAttributeSection;

  /**
   * Creates an open/close button for the given section.
   */
  public UmlClassOpenCloseSectionButton(final boolean attributeSection) {
    isAttributeSection = attributeSection;
  }

  /**
   * Paints the button with its icon. Anti-aliasing is temporary disabled for nicer visualization of the icon.
   */
  public void paint(final NodeRealizer context, final Graphics2D graphics) {
    if (isVisible(context)) {
      final Object orgRenderingHint = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      try {
        final Rectangle2D area = getButtonArea(context);
        paintButton(context, graphics, area);
        paintIcon(context, graphics, area);
      } finally {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, orgRenderingHint);
      }
    }
  }

  /**
   * Checks whether or not the button is currently visible. The button is not visible if the class details is not
   * opened.
   */
  protected boolean isVisible(final NodeRealizer context) {
    return UmlClassLabelSupport.getModel(context).areSectionsVisible();
  }

  /**
   * Returns the area where to paint the button with its icon.
   */
  protected Rectangle2D getButtonArea(final NodeRealizer context) {
    final NodeLabel label = getCaptionLabel(context);
    return new Rectangle2D.Double(
        context.getX() + GAP,
        label.getLocation().getY() + (label.getHeight() - ICON_SIZE) * 0.5,
        ICON_SIZE,
        ICON_SIZE);
  }

  /** Returns the caption label of the section that corresponds to this button. */
  private NodeLabel getCaptionLabel(final NodeRealizer context) {
    if (isAttributeSection) {
      return UmlClassLabelSupport.getAttributeCaptionLabel(context);
    } else {
      return UmlClassLabelSupport.getOperationCaptionLabel(context);
    }
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
    final int button = UmlRealizerFactory.getMouseOverButton(context);
    return ((button == UmlRealizerFactory.BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION) && isAttributeSection) ||
           ((button == UmlRealizerFactory.BUTTON_OPEN_CLOSE_OPERATION_SECTION) && !isAttributeSection);
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

  /** Returns the shape of the icon. */
  private Shape getIconShape(final NodeRealizer context, final Rectangle2D area) {
    if (isAttributeSection) {
      return UmlClassLabelSupport.getModel(context).areAttributesVisible() ? getMinusShape(area) : getPlusShape(area);
    } else {
      return UmlClassLabelSupport.getModel(context).areOperationsVisible() ? getMinusShape(area) : getPlusShape(area);
    }
  }

  /**
   * Returns the shape of the icon of the closed state.
   */
  private Shape getMinusShape(final Rectangle2D area) {
    final float minX = (float) (area.getX() + area.getWidth() * 0.25);
    final float maxX = (float) (area.getX() + area.getWidth() * 0.75);
    final float midY = (float) (area.getY() + area.getHeight() * 0.5);

    final GeneralPath icon = new GeneralPath();
    icon.moveTo(minX, midY);
    icon.lineTo(maxX, midY);
    return icon;
  }

  /**
   * Returns the shape of the icon of the opened state.
   */
  private Shape getPlusShape(final Rectangle2D area) {
    final float minX = (float) (area.getX() + area.getWidth() * 0.25);
    final float midX = (float) (area.getX() + area.getWidth() * 0.5);
    final float maxX = (float) (area.getX() + area.getWidth() * 0.75);
    final float minY = (float) (area.getY() + area.getHeight() * 0.25);
    final float midY = (float) (area.getY() + area.getHeight() * 0.5);
    final float maxY = (float) (area.getY() + area.getHeight() * 0.75);

    final GeneralPath icon = new GeneralPath();
    icon.moveTo(minX, midY);
    icon.lineTo(maxX, midY);
    icon.moveTo(midX, minY);
    icon.lineTo(midX, maxY);
    return icon;
  }

  /**
   * Toggles the visibility of the attribute or operation section on button click.
   */
  protected void buttonClicked(final NodeRealizer context, final Graph2DView view) {
    final Graph2D graph = view.getGraph2D();
    graph.firePreEvent();
    try {
      graph.backupRealizers(new NodeList(context.getNode()).nodes());
      graph.backupRealizers(context.getNode().edges());
      if (isAttributeSection) {
        final boolean areAttributesOpened = UmlClassLabelSupport.getModel(context).areAttributesVisible();
        final UmlClassAnimation attributeAnimation = new OpenCloseAttributeSectionAnimation(
            view,
            context,
            areAttributesOpened);
        attributeAnimation.play();
      } else {
        final boolean areOperationsOpened = UmlClassLabelSupport.getModel(context).areOperationsVisible();
        final UmlClassAnimation operationAnimation = new OpenCloseOperationSectionAnimation(
            view,
            context,
            areOperationsOpened);
        operationAnimation.play();
      }
    } finally {
      graph.firePostEvent();
    }
  }

  /** Sets an appropriate style property of the given context to notify that the mouse is currently over this button. */
  protected void buttonEntered(final NodeRealizer context, final Graph2DView view) {
    if (isAttributeSection) {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_OPEN_CLOSE_ATTRIBUTE_SECTION);
    } else {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_OPEN_CLOSE_OPERATION_SECTION);
    }
    view.updateView();
  }

  /**
   * Animates the opening and closing of the attributes section.
   */
  private static class OpenCloseAttributeSectionAnimation extends UmlClassAnimation  {
    private OpenCloseAttributeSectionAnimation(
        final Graph2DView view,
        final NodeRealizer context,
        final boolean isClosing
    ) {
      super(view, context, isClosing);
    }

    /**
     * Closes the attribute section.
     */
    protected void close() {
      UmlClassLabelSupport.getModel(context).setAttributesVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Opens the attribute section.
     */
    protected void open() {
      UmlClassLabelSupport.getModel(context).setAttributesVisible(true);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Returns the size of the realizer when the attribute section is closed.
     */
    protected YDimension getClosedSize() {
      final boolean isVisible = UmlClassLabelSupport.getModel(context).areAttributesVisible();
      UmlClassLabelSupport.getModel(context).setAttributesVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
      final YDimension dimension = new YDimension(context.getWidth(), context.getHeight());
      UmlClassLabelSupport.getModel(context).setAttributesVisible(isVisible);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
      return dimension;
    }

    /**
     * The lower y-coordinate of the fixed part of the realizer is the bottom of its attribute caption label.
     */
    protected double getFixedY() {
      final NodeLabel label = UmlClassLabelSupport.getAttributeCaptionLabel(context);
      return label.getLocation().getY() + label.getHeight();
    }

    /**
     * The upper y-coordinate of the moving part of the realizer is the top of its operation caption label.
     */
    protected double getMovingY() {
      final NodeLabel label = UmlClassLabelSupport.getOperationCaptionLabel(context);
      return label.getLocation().getY() - UmlClassLabelSupport.SECTION_GAP;
    }

    /**
     * Sets the opacity of the attribute buttons depending on the given state.
     *
     * @param state the state of the animation between <code>0</code> and <code>1</code>
     */
    protected void stateUpdated(final double state) {
      UmlRealizerFactory.setAttributeButtonOpacity(context, (float) state);
    }
  }

  /**
   * Animates the opening and closing of the operations section.
   */
  private static class OpenCloseOperationSectionAnimation extends UmlClassAnimation  {
    private OpenCloseOperationSectionAnimation(
        final Graph2DView view,
        final NodeRealizer context,
        final boolean isClosing
    ) {
      super(view, context, isClosing);
    }

    /**
     * Closes the operation section.
     */
    protected void close() {
      UmlClassLabelSupport.getModel(context).setOperationsVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Opens the operation section.
     */
    protected void open() {
      UmlClassLabelSupport.getModel(context).setOperationsVisible(true);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
    }

    /**
     * Returns the size of the realizer when the attribute section is closed.
     */
    protected YDimension getClosedSize() {
      final boolean isVisible = UmlClassLabelSupport.getModel(context).areOperationsVisible();
      UmlClassLabelSupport.getModel(context).setOperationsVisible(false);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
      final YDimension dimension = new YDimension(context.getWidth(), context.getHeight());
      UmlClassLabelSupport.getModel(context).setOperationsVisible(isVisible);
      UmlClassLabelSupport.updateAllLabels(context);
      UmlClassLabelSupport.updateRealizerSize(context);
      return dimension;
    }

    /**
     * The lower y-coordinate of the fixed part of the realizer is the bottom of its operation caption label.
     */
    protected double getFixedY() {
      final NodeLabel label = UmlClassLabelSupport.getOperationCaptionLabel(context);
      return label.getLocation().getY() + label.getHeight();
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

    /**
     * Sets the opacity of the operation buttons depending on the given state.
     *
     * @param state the state of the animation between <code>0</code> and <code>1</code>
     */
    protected void stateUpdated(final double state) {
      UmlRealizerFactory.setOperationButtonOpacity(context, (float) state);
    }
  }
}