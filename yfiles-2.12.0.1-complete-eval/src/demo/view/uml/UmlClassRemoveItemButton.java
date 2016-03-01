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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * An {@link UmlClassButton} that removes a selected attribute or operation on mouse click.
 */
class UmlClassRemoveItemButton extends UmlClassButton {
  public static final double ICON_SIZE = 12;
  public static final double ICON_GAP = 5;

  private final boolean isAttributeSection;

  /**
   * Creates a remove button for the given section.
   */
  public UmlClassRemoveItemButton(final boolean attributeSection) {
    isAttributeSection = attributeSection;
  }

  /**
   * Paints the button with its icon.
   */
  public void paint(final NodeRealizer context, final Graphics2D graphics) {
    if (isVisible(context)) {
      // Set the opacity of the button.
      final Composite orgComposite = graphics.getComposite();
      final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getButtonOpacity(context));
      graphics.setComposite(composite);

      final Rectangle2D area = getButtonArea(context);
      paintBackground(context, graphics, area);
      paintIcon(context, graphics, area);

      graphics.setComposite(orgComposite);
    }
  }

  /**
   * Checks whether or not the button is currently visible. The button is not visible if the corresponding section is
   * not opened.
   */
  protected boolean isVisible(final NodeRealizer context) {
    final boolean areClassDetailsVisible = UmlClassLabelSupport.getModel(context).areSectionsVisible();
    final boolean isSectionVisible =
        isAttributeSection && UmlClassLabelSupport.getModel(context).areAttributesVisible() ||
        !isAttributeSection && UmlClassLabelSupport.getModel(context).areOperationsVisible();
    return isSectionVisible && areClassDetailsVisible;
  }

  /**
   * Returns the area where to paint the button with its icon.
   */
  protected Rectangle2D getButtonArea(final NodeRealizer context) {
    final NodeLabel label = getCaptionLabel(context);
    return new Rectangle2D.Double(
        context.getX() + context.getWidth() - ICON_SIZE - ICON_GAP,
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
  private void paintBackground(final NodeRealizer context, final Graphics2D graphics, final Rectangle2D area) {
    final Color color = isMouseOverButton(context) && isEnabled(context) ?
        UmlRealizerFactory.COLOR_BUTTON_BACKGROUND_ACTIVE :
        UmlRealizerFactory.COLOR_BUTTON_BACKGROUND_BLANK;
    final Shape shape = new Ellipse2D.Double(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    graphics.setColor(color);
    graphics.fill(shape);
  }

  /** Returns the opacity of this button. */
  private float getButtonOpacity(final NodeRealizer context) {
    if (isAttributeSection) {
      return UmlRealizerFactory.getAttributeButtonOpacity(context);
    } else {
      return UmlRealizerFactory.getOperationButtonOpacity(context);
    }
  }

  /**
   * Checks whether or not the mouse is currently over this button. The button where the mouse is currently over is
   * stored as an appropriate style property of the given context.
   */
  private boolean isMouseOverButton(final NodeRealizer context) {
    final int button = UmlRealizerFactory.getMouseOverButton(context);
    return ((button == UmlRealizerFactory.BUTTON_SUB_ATTRIBUTE) && isAttributeSection) ||
           ((button == UmlRealizerFactory.BUTTON_SUB_OPERATION) && !isAttributeSection);
  }

  /**
   * Paints the icon into the given area. Anti-aliasing is temporary disabled for nicer visualization of the icon.
   */
  private void paintIcon(final NodeRealizer context, final Graphics2D graphics, final Rectangle2D area) {
    final Object orgRenderingHint = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    try {
      final Shape shape = getIconShape(area);
      final Color color = isEnabled(context) ?
          UmlRealizerFactory.COLOR_BUTTON_FOREGROUND_ENABLED :
          UmlRealizerFactory.COLOR_BUTTON_FOREGROUND_DISABLED;
      graphics.setColor(color);
      graphics.setStroke(LineType.LINE_1);
      graphics.draw(shape);
    } finally {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, orgRenderingHint);
    }
  }

  /**
   * Returns whether or not this button is currently enabled. The button is enabled when one item of its corresponding
   * list is currently selected.
   */
  private boolean isEnabled(final NodeRealizer context) {
    return (isAttributeSection && UmlClassLabelSupport.isAttributeSelected(context)) ||
           (!isAttributeSection && UmlClassLabelSupport.isOperationSelected(context));
  }

  /**
   * Returns the shape of the icon.
   */
  private Shape getIconShape(final Rectangle2D area) {
    final float minX = (float) (area.getX() + area.getWidth() * 0.25);
    final float maxX = (float) (area.getX() + area.getWidth() * 0.75);
    final float midY = (float) (area.getY() + area.getHeight() * 0.5);

    final GeneralPath icon = new GeneralPath();
    icon.moveTo(minX, midY);
    icon.lineTo(maxX, midY);
    return icon;
  }

  /**
   * Removes the currently selected label on button click.
   */
  protected void buttonClicked(final NodeRealizer context, final Graph2DView view) {
    if (isEnabled(context) && getButtonOpacity(context) == UmlRealizerFactory.OPAQUE) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      try {
        graph.backupRealizers(new NodeList(context.getNode()).nodes());
        graph.backupRealizers(context.getNode().edges());
        final UmlClassAnimation animation = new RemoveItemAnimation(view, context, true);
        animation.play();
      } finally {
        graph.firePostEvent();
      }
      view.updateView();
    }
  }

  /** Sets an appropriate style property of the given context to notify that the mouse is currently over this button. */
  protected void buttonEntered(final NodeRealizer context, final Graph2DView view) {
    if (isAttributeSection) {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_SUB_ATTRIBUTE);
    } else {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_SUB_OPERATION);
    }
    view.updateView(getButtonArea(context));
  }

  /**
   * Animates the removing of an attribute or an operation.
   */
  static class RemoveItemAnimation extends UmlClassAnimation  {

    RemoveItemAnimation(final Graph2DView view, final NodeRealizer context, final boolean isClosing) {
      super(view, context, isClosing);
    }

    /**
     * Opening is not possible.
     */
    protected void open() {
    }

    /**
     * Closing means removing the selected label.
     */
    protected void close() {
      UmlClassLabelSupport.removeSelectedLabel(context);
    }

    /**
     * Returns the size of the realizer without the selected label.
     */
    protected YDimension getClosedSize() {
      final NodeLabel selectedLabel = UmlClassLabelSupport.getSelectedLabel(context);
      return new YDimension(context.getWidth(), context.getHeight() - selectedLabel.getHeight());
    }

    /**
     * Returns the upper y-coordinate of the selected label.
     */
    protected double getFixedY() {
      final NodeLabel label = UmlClassLabelSupport.getSelectedLabel(context);
      return label.getLocation().getY();
    }

    /**
     * Returns the lower y-coordinate of the selected label.
     */
    protected double getMovingY() {
      final NodeLabel label = UmlClassLabelSupport.getSelectedLabel(context);
      return label.getLocation().getY() + label.getHeight();
    }
  }
}
