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
import y.geom.YPoint;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An {@link UmlClassButton} that adds a new attribute or operation on mouse click.
 */
class UmlClassAddItemButton extends UmlClassButton{
  public static final double ICON_SIZE = 12;
  public static final double ICON_GAP = 5;

  private final boolean isAttributeSection;

  /**
   * Creates an add button for the given section.
   */
  public UmlClassAddItemButton(final boolean attributeSection) {
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
      paintIcon(graphics, area);

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
        context.getX() + context.getWidth() - 2* ICON_SIZE - 2* ICON_GAP,
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
    final Color color = isMouseOverButton(context) ?
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
    return ((button == UmlRealizerFactory.BUTTON_ADD_ATTRIBUTE) && isAttributeSection) ||
           ((button == UmlRealizerFactory.BUTTON_ADD_OPERATION) && !isAttributeSection);
  }

  /**
   * Paints the icon into the given area. Anti-aliasing is temporary disabled for nicer visualization of the icon.
   */
  private void paintIcon(final Graphics2D graphics, final Rectangle2D area) {
    final Object orgRenderingHint = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    try {
      final Shape shape = getIconShape(area);
      final Color color = UmlRealizerFactory.COLOR_BUTTON_FOREGROUND_ENABLED;
      graphics.setColor(color);
      graphics.setStroke(LineType.LINE_1);
      graphics.draw(shape);
    } finally {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, orgRenderingHint);
    }
  }

  /**
   * Returns the shape of the icon.
   */
  private Shape getIconShape(final Rectangle2D area) {
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
   * Adds a new list item at the end of the attribute or operation list on button click.
   */
  protected void buttonClicked(final NodeRealizer context, final Graph2DView view) {
    if (getButtonOpacity(context) == UmlRealizerFactory.OPAQUE) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      try {
        // Add a new label.
        graph.backupRealizers(new NodeList(context.getNode()).nodes());
        graph.backupRealizers(context.getNode().edges());
        final UmlClassAnimation animation = new AddItemAnimation(view, context, false);
        animation.play();

        // Open the new label in an editor.
        final NodeLabel label = UmlClassLabelSupport.getSelectedLabel(context);
        final YPoint location = label.getTextLocation();
        view.openLabelEditor(label, location.getX(), location.getY(), new TextChangeHandler(view), true, true);

      } finally {
        graph.firePostEvent();
      }
    }
  }

  /** Sets an appropriate style property of the given context to notify that the mouse is currently over this button. */
  protected void buttonEntered(final NodeRealizer context, final Graph2DView view) {
    if (isAttributeSection) {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_ADD_ATTRIBUTE);
    } else {
      UmlRealizerFactory.setMouseOverButton(context, UmlRealizerFactory.BUTTON_ADD_OPERATION);
    }
    view.updateView(getButtonArea(context));
  }

  /**
   * This handler listens for text changes and adjusts the node size to
   * the label size.
   */
  private class TextChangeHandler implements PropertyChangeListener {
    private final Graph2DView view;

    public TextChangeHandler(final Graph2DView view) {
      this.view = view;
    }

    /**
     * After the label editor has been closed the changed text of the label must be updated in the {@link UmlClassModel
     * model}. Empty labels will be removed.
     */
    public void propertyChange(PropertyChangeEvent e) {
      final Object source = e.getSource();
      if (source instanceof NodeLabel) {
        final NodeLabel label = (NodeLabel) source;
        final NodeRealizer realizer = view.getGraph2D().getRealizer(label.getNode());

        // Update model.
        UmlClassLabelSupport.updateLabelText(realizer, label);
        UmlClassLabelSupport.selectLabel(realizer, label);

        // Remove empty label.
        if ("".equals(label.getText())) {
          label.setText(" ");
          final UmlClassAnimation animation = new UmlClassRemoveItemButton.RemoveItemAnimation(view, realizer, true);
          animation.play();
        } else {
          UmlClassLabelSupport.updateRealizerSize(realizer);
        }
      }
    }
  }

  /**
   * Animates the adding of an attribute or operation.
   */
  private class AddItemAnimation extends UmlClassAnimation  {
    final double closedHeight;

    private AddItemAnimation(final Graph2DView view, final NodeRealizer context, final boolean isClosing) {
      super(view, context, isClosing);
      closedHeight = context.getHeight();
    }

    /** Opening means adding an additional attribute or operation label. */
    protected void open() {
      if (isAttributeSection) {
        UmlClassLabelSupport.addAttribute(context);
      } else {
        UmlClassLabelSupport.addOperation(context);
      }
    }

    /**
     * Closing is not possible.
     */
    protected void close() {
    }

    /**
     * Returns the size of the realizer without the additional label.
     */
    protected YDimension getClosedSize() {
      return new YDimension(context.getWidth(), closedHeight);
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
