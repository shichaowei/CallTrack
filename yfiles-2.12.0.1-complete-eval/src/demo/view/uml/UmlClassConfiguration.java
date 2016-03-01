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

import y.geom.YDimension;
import y.view.AbstractCustomNodePainter;
import y.view.GenericNodeRealizer;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.MouseInputEditor;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.YRenderingHints;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * This configuration paints the UML class realizer.
 */
class UmlClassConfiguration extends AbstractCustomNodePainter
    implements GenericNodeRealizer.GenericMouseInputEditorProvider,
      GenericNodeRealizer.GenericSizeConstraintProvider {
   private UmlClassButton[] buttons;

  public UmlClassConfiguration() {
    buttons = new UmlClassButton[] {
        new UmlClassOpenCloseSectionButton(true),
        new UmlClassOpenCloseSectionButton(false),
        new UmlClassOpenCloseClassButton(),
        new UmlClassAddItemButton(true),
        new UmlClassAddItemButton(false),
        new UmlClassRemoveItemButton(true),
        new UmlClassRemoveItemButton(false)
    };
  }

  /**
   * Paints the realizer with its name area, attribute and operation sections.
   */
  protected void paintNode(final NodeRealizer context, final Graphics2D graphics, final boolean sloppy) {
    final Color fillColor = context.getFillColor();
    final Color fillColor2 = context.getFillColor2();
    final Color lineColor = context.getLineColor();
    final boolean selected = useSelectionStyle(context, graphics);

    // Set the opacity of the realizer.
    final Composite orgComposite = graphics.getComposite();
    final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, UmlRealizerFactory.getNodeOpacity(context));
    graphics.setComposite(composite);

    // Paint background.
    final Rectangle2D.Double rect = context.getBoundingBox();
    final Color backgroundColor = selected && sloppy ? createSelectionColor(fillColor) : fillColor;
    graphics.setColor(backgroundColor);
    graphics.fill(rect);

    // Paint area of the name.
    final NodeLabel nameLabel = UmlClassLabelSupport.getNameLabel(context);
    UmlClassLabelSupport.getLabelArea(context, nameLabel, rect);
    final Color nameColor = selected && sloppy ? createSelectionColor(fillColor2) : fillColor2;
    graphics.setColor(nameColor);
    graphics.fill(rect);

    if (!sloppy) {
      if (UmlClassLabelSupport.getModel(context).areSectionsVisible()) {

        // Paint area of the attribute caption.
        final NodeLabel attributeCaptionLabel = UmlClassLabelSupport.getAttributeCaptionLabel(context);
        UmlClassLabelSupport.getLabelArea(context, attributeCaptionLabel, rect);
        graphics.setColor(fillColor2);
        graphics.fill(rect);

        // Paint area of the operation caption.
        final NodeLabel operationCaptionLabel = UmlClassLabelSupport.getOperationCaptionLabel(context);
        UmlClassLabelSupport.getLabelArea(context, operationCaptionLabel, rect);
        graphics.setColor(fillColor2);
        graphics.fill(rect);

        // Paint area of the selected label.
        final NodeLabel selectedLabel = UmlClassLabelSupport.getSelectedLabel(context);
        if (selectedLabel != null) {
          UmlClassLabelSupport.getLabelArea(context, selectedLabel, rect);
          final Color selectedLabelColor = new Color(
              lineColor.getRed(),
              lineColor.getGreen(),
              lineColor.getBlue(),
              (int) (UmlRealizerFactory.getSelectionOpacity(context) * 255));
          graphics.setColor(selectedLabelColor);
          graphics.fill(rect);
        }
      }

      // Paint buttons.
      for (int i = 0; i < buttons.length; i++) {
        final UmlClassButton button = buttons[i];
        button.paint(context, graphics);
      }
    }

    // Paint outline.
    rect.setFrame(context.getX(), context.getY(), context.getWidth(), context.getHeight());
    final Color outlineColor = selected && sloppy ? createSelectionColor(fillColor2) : fillColor2;
    graphics.setColor(outlineColor);
    graphics.setStroke(context.getLineType());
    graphics.draw(rect);

    graphics.setComposite(orgComposite);
  }

  /**
   * Paints the node in an animated state. The state of the animation is defined by the <code>state</code> that is an
   * value between <code>0</code> and <code>1</code>. The value <code>0</code> means the section is completely closed,
   * <code>1</code> means the section is completely open.
   *
   * @param context      the realizer to animate
   * @param graphics     the graphics context
   * @param fixedOffset  the lower y-coordinate of the part of the realizer that is fixed during the animation
   * @param movingOffset the upper y-coordinate of the part of the realizer that moves during the animation
   * @param state        the state of the animation. The value <code>0</code> means the section is completely closed,
   *                     <code>1</code> means the section is completely open.
   */
  public void paintAnimatedNode(
      final NodeRealizer context,
      final Graphics2D graphics,
      final double fixedOffset,
      final double movingOffset,
      final double state
  ) {
    final Shape clip = graphics.getClip();
    final Color color = graphics.getColor();
    final Stroke stroke = graphics.getStroke();
    final AffineTransform transform = graphics.getTransform();
    try {
      paintAnimatedNodeImpl(context, graphics, fixedOffset, movingOffset, state);
    } finally {
      graphics.setClip(clip);
      graphics.setColor(color);
      graphics.setStroke(stroke);
      graphics.setTransform(transform);
    }
  }

  private void paintAnimatedNodeImpl(
          final NodeRealizer context,
          final Graphics2D graphics,
          final double fixedOffset,
          final double movingOffset,
          final double state
  ) {
    final double offset = fixedOffset + (movingOffset - fixedOffset) * state;
    final double lineWidth = UmlRealizerFactory.LINE_EDGE_CREATION_BUTTON_OUTLINE.getLineWidth();

    // Paint the fix (upper) part of the node.
    final Rectangle2D clipRect = new Rectangle2D.Double(
        context.getX() - lineWidth * 0.5,
        context.getY() - lineWidth * 0.5,
        context.getWidth() + lineWidth,
        offset - (context.getY() - lineWidth * 0.5));
    graphics.setClip(clipRect);
    paintNode(context, graphics, false);
    paintText(context, graphics);

    // Paint the moving (lower) part of the node.
    clipRect.setFrame(
        context.getX() - lineWidth * 0.5,
        offset,
        context.getWidth() + lineWidth,
        context.getY() + context.getHeight() + lineWidth * 0.5 - movingOffset);
    graphics.setClip(clipRect);

    // Shift the lower part by offset.
    graphics.translate(0, -(movingOffset - offset));
    paintNode(context, graphics, false);
    paintText(context, graphics);
  }

  /**
   * Checks whether or not selection style is used.
   */
  private static boolean useSelectionStyle(
      final NodeRealizer context,
      final Graphics2D gfx
  ) {
    return context.isSelected() && YRenderingHints.isSelectionPaintingEnabled(gfx);
  }

  /**
   * Returns an {@link MouseInputEditor} to handle mouse events for buttons of UML class nodes.
   * */
  public MouseInputEditor findMouseInputEditor(
      final NodeRealizer context,
      final Graph2DView view,
      final double x,
      final double y,
      final HitInfo hitInfo
  ) {
    if (context instanceof GenericNodeRealizer && isDetailed(view)) {
      for (int i = 0; i < buttons.length; i++) {
        final UmlClassButton button = buttons[i];
        if (button.contains(context, x, y)) {
          return button.getMouseInputEditor(context, view);
        }
      }
    }
    return null;
  }

  /**
   * Checks whether or not sloppy painting is enabled.
   */
  private static boolean isDetailed(final Graph2DView view) {
    return view.getZoom() > view.getPaintDetailThreshold();
  }

  /**
   * Returns the minimum size of the realizer. The minimum size contains all visible labels and buttons.
   */
  public YDimension getMinimumSize(final NodeRealizer context) {
    double minY = context.getY();
    double minX = context.getX();

    double maxY = getMaxYOfLabel(UmlClassLabelSupport.getNameLabel(context));
    double maxX = getNameLabelMaxX(context);
    if (UmlClassLabelSupport.getModel(context).areSectionsVisible()) {
      maxX = Math.max(maxX, getCaptionLabelMaxX(UmlClassLabelSupport.getAttributeCaptionLabel(context)));
      maxX = Math.max(maxX, getCaptionLabelMaxX(UmlClassLabelSupport.getOperationCaptionLabel(context)));
      for (int i = 1; i < context.labelCount(); i++) {
        final NodeLabel label = context.getLabel(i);
        maxX = Math.max(maxX, getMaxXOfLabel(label));
        maxY = Math.max(maxY, getMaxYOfLabel(label));
      }
    }
    final double INSET_RIGHT = 10;
    final double INSET_BOTTOM = UmlClassLabelSupport.getModel(context).areSectionsVisible() ? 5 : 0;
    return new YDimension(maxX - minX + INSET_RIGHT, maxY - minY + INSET_BOTTOM);
  }

  /**
   * Returns the maximum x-coordinate of the given label.
   */
  private double getMaxXOfLabel(final NodeLabel label) {
    return label.getLocation().getX() + label.getWidth();
  }

  /**
   * Returns the maximum y-coordinate of the given label.
   */
  private double getMaxYOfLabel(final NodeLabel label) {
    return label.getLocation().getY() + label.getHeight();
  }

  /**
   * Returns the maximum x-coordinate of the name label including the open/close button.
   */
  private double getNameLabelMaxX(final NodeRealizer context) {
    final NodeLabel label = UmlClassLabelSupport.getNameLabel(context);
    return label.getLocation().getX() +
           label.getWidth() +
           UmlClassOpenCloseClassButton.ICON_SIZE +
           UmlClassOpenCloseClassButton.ICON_GAP;
  }

  /**
   * Returns the maximum x-coordinate of the given caption label including the add/remove button.
   */
  private double getCaptionLabelMaxX(final NodeLabel label) {
    return label.getLocation().getX() +
           label.getWidth() +
           UmlClassAddItemButton.ICON_SIZE +
           UmlClassAddItemButton.ICON_GAP +
           UmlClassRemoveItemButton.ICON_SIZE +
           UmlClassRemoveItemButton.ICON_GAP;
  }

  /**
   * Returns the maximum size of the realizer.
   */
  public YDimension getMaximumSize(final NodeRealizer context) {
    return new YDimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

}
