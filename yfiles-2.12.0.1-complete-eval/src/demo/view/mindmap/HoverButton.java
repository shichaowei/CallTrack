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
package demo.view.mindmap;

import demo.view.mindmap.StateIconProvider.StateIcon;

import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.Node;
import y.base.NodeList;
import y.geom.Geom;
import y.view.AbstractMouseInputEditor;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.MouseInputEditorProvider;
import y.view.NodeRealizer;
import y.view.ViewMode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.Icon;

/**
 * Provides inline controls for adding icons, changing colors, adding edges,
 * adding, child nodes, and deleting nodes.
 */
class HoverButton implements Drawable, MouseInputEditorProvider, AnimationObject {
  private static final Color PANEL_BACKGROUND = new Color(28, 181, 255, 85);
  private static final Color GREEN = new Color(21, 186, 7);
  private static final Color YELLOW = new Color(255, 237, 0);


  /**
   * The target node for edit actions.
   */
  private Node currentNode;

  /**
   * The visible state of the inline editing controls.
   */
  private boolean visible;

  /**
   * true, if menu to choose color or icon is open.
   */
  private boolean showsChooser;

  /**
   * offset to open the hover button at the mouse position.
   */
  private int mouseX;

  /**
   * vertical offset during the animation
   */
  private double animationYOffset;
  private final AnimationPlayer player;

  private final Graph2DView view;

  private final DeleteButton delete;
  private final ColorButton color;
  private final IconButton icon;
  private final AddButton add;
  private final CrossReferenceButton reference;

  /**
   * Initializes a new <code>HoverButton</code> instance for the specified view.
   */
  HoverButton( final Graph2DView view ) {
    this.view = view;
    delete = new DeleteButton();
    color = new ColorButton(this);
    icon = new IconButton(this);
    add = new AddButton();
    reference = new CrossReferenceButton();
    visible = false;
    player = new AnimationPlayer();
    Graph2DViewRepaintManager manager = new Graph2DViewRepaintManager(view);
    manager.add(this);
    player.addAnimationListener(manager);
    view.addViewMode(new HoverViewMode());
  }

  /**
   * Returns the control implementations for the current node.
   * @return the control implementations for the current node.
   */
  private Iterator activeButtons() {
    return new ControlsIterator(ViewModel.instance.isRoot(currentNode));
  }

  /**
   * Specifies the target node for edit actions.
   * @param node the target node for edit actions.
   */
  public void setNode( final Node node, final double mouseOffset ) {
    //prevent hover button from appearing at other items when choosing new icon/color
    if (!color.isVisible() && !icon.isVisible()) {
      //mouse moved away from item
      if (node == null) {
        hideButtons();
        currentNode = null;
        view.updateView();
      } else if (currentNode != node) {
        if (!visible) {
          view.addDrawable(this);
          visible = true;
          final double zoom = 1 / view.getZoom();
          mouseX = (int) (mouseOffset - view.getGraph2D().getRealizer(node).getX() - (80*zoom));
          if (ViewModel.instance.isRoot(node)) {
            mouseX = 0;
          }
        }
        for (Iterator it = new ControlsIterator(false); it.hasNext();) {
          ((InlineControl) it.next()).setNode(node);
        }
        currentNode = node;
        player.animate(this);
      }
    }
  }

  /**
   * Removes the editing controls from the associated view.
   */
  private void hideButtons() {
    player.stop();
    view.removeDrawable(this);
    visible = false;
  }

  /**
   * Paints the editing controls.
   * This method delegates painting to each specific editing control.
   * @param g Graphics2D to paint on
   */
  public void paint(final Graphics2D g) {
    if (isValid()) {
      Color oldColor = g.getColor();
      g.setColor(PANEL_BACKGROUND);
      g.fill(getBounds());
      g.setColor(oldColor);

      for (Iterator it = activeButtons(); it.hasNext();) {
        ((InlineControl) it.next()).paint(g);
      }
    }
  }

  /**
   * Returns the bounds of the editing controls.
   * @return the bounds of the editing controls.
   */
  public Rectangle getBounds() {
    Rectangle r = new Rectangle(0,0,-1,-1);
    if (isValid()) {
      for (final Iterator it = activeButtons(); it.hasNext(); ) {
        Geom.calcUnion(r, ((InlineControl) it.next()).getBounds(), r);
      }
    }
    return r;
  }

  /**
   * Determines if the current target node for edit actions is a valid node. 
   * @return <code>true</code> if edit action may be executed;
   * <code>false</code> otherwise.
   */
  private boolean isValid() {
    //graph may changed while button visible, e.g. when loading new graph
    if (currentNode != null && currentNode.getGraph() != null) {
      return true;
    }
    currentNode = null;
    return false;
  }

  /**
   * Returns an editor for the edit action that is appropriate for the specified
   * mouse position.
   * @param view the view that will host the editor
   * @param x the x coordinate of the mouse event
   * @param y the y coordinate of the mouse event
   * @param hitInfo the HitInfo that may be used to determine what instance to return or <code>null</code>
   * @return an editor that is appropriate for the specified mouse position
   * or <code>null</code> if the specified mouse position cannot trigger
   * any edit action.
   */
  public MouseInputEditor findMouseInputEditor(Graph2DView view, double x, double y, HitInfo hitInfo) {
    if (isValid()) {
      for (Iterator it = activeButtons(); it.hasNext();) {
        final InlineControl button = (InlineControl) it.next();
        if (button.getBounds().contains(x, y)) {
          return button;
        }
      }
    }
    return null;
  }

  /**
   * Closes secondary editing controls for choosing colors or icons.
   */
  public void closeAll() {
    if (color.isVisible()) {
      color.toggleVisibility();
    }
    if (icon.isVisible()) {
      icon.toggleVisibility();
    }
  }

  /**
   * Initializes the start position of the editing controls for the controls'
   * animated fade-in effect.
   */
  public void initAnimation() {
    animationYOffset = 10;
  }

  /**
   * Calculates the appropriate position of the editing controls for the
   * controls' animated fade-in effect.
   */
  public void calcFrame(final double time) {
    animationYOffset = (1 - time) * 10;
  }

  /**
   * Ensures the correct final position of the editing controls after the
   * controls' animated fade-in effect.
   */
  public void disposeAnimation() {
    animationYOffset = 0;
  }

  /**
   * Returns the preferred duration  of the editing controls after the
   * controls' animated fade-in effect in milliseconds.
   * @return the preferred duration  of the editing controls after the
   * controls' animated fade-in effect in milliseconds.
   */
  public long preferredDuration() {
    return 150;
  }

  String getToolTipText( final double x, final double y ) {
    if (isValid()) {
      for (Iterator it = activeButtons(); it.hasNext();) {
        final InlineControl button = (InlineControl) it.next();
        if (button.getBounds().contains(x, y)) {
          return button.getToolTipText();
        }
      }
    }
    return null;
  }


  /**
   * Initiates interactive creation of a cross-reference edge for existing
   * nodes.
   */
  private class CrossReferenceButton extends InlineControl {
    CrossReferenceButton() {
      super(90);
    }

    /**
     * Returns <em>Add a cross reference.</em>
     * @return <em>Add a cross reference.</em>
     */
    String getToolTipText() {
      return "Add a cross reference.";
    }

    /**
     * Initiates interactive creation of a cross-reference edge for existing
     * nodes.
     */
    void action() {
      for(final Iterator viewModes = view.getViewModes();viewModes.hasNext();) {
        final ViewMode viewMode = (ViewMode) viewModes.next();
        if (viewMode instanceof MoveNodeMode) {
          MoveNodeMode moveMode = (MoveNodeMode) viewMode;
          moveMode.startCrossEdgeCreation(node);
          view.updateView();
        }
      }
    }

    /**
     * Paints a blue cross-reference symbol.
     */
    void paint(Graphics2D g) {
      g = newZoomInvariant(g);

      g.scale(1.5, 1.5);

      g.setColor(MindMapUtil.CROSS_EDGE_COLOR);
      g.fillOval(0, 0, 16, 16);

      g.setColor(Color.WHITE);
      GeneralPath gp = new GeneralPath();
      gp.moveTo(8, 3);
      gp.lineTo(12, 8);
      gp.lineTo(10, 8);
      gp.lineTo(10, 12);
      gp.lineTo(6, 12);
      gp.lineTo(6, 8);
      gp.lineTo(4, 8);
      g.fill(gp);

      g.dispose();
    }
  }

  /**
   * Adds a new child node to the current target node.
   */
  private class AddButton extends InlineControl {
    AddButton() {
      super(120);
    }

    /**
     * Returns <em>Add a new child item.</em>
     * @return <em>Add a new child item.</em>
     */
    String getToolTipText() {
      return "Add a new child item.";
    }

    /**
     * Adds a new child node.
     */
    void action() {
      MindMapUtil.addNode(view, node);
    }

    /**
     * Paints a add symbol in a green circle.
     */
    void paint(Graphics2D g) {
      g = newZoomInvariant(g);

      g.scale(1.5,1.5);
      g.setColor(GREEN);
      g.fillOval(0, 0, 16, 16);
      g.setColor(Color.WHITE);
      g.fillRect(7, 4, 2, 8);
      g.fillRect(4, 7, 8, 2);

      g.dispose();
    }
  }

  /**
   * Removes the current target node and all its tree successors.
   */
  private class DeleteButton extends InlineControl {
    DeleteButton() {
      super(150);
    }

    /**
     * Returns <em>Remove this item and all of its children.</em>
     * @return <em>Remove this item and all of its children.</em>
     */
    String getToolTipText() {
      return "Remove this item and all of its children.";
    }

    /**
     * Removes the current target node and all its tree successors.
     */
    void action() {
      hideButtons();

      final Graph2D graph2D = view.getGraph2D();
      graph2D.firePreEvent();
      MindMapUtil.removeSubtree(graph2D, node);
      LayoutUtil.layout(graph2D);
      graph2D.firePostEvent();
    }

    /**
     * Paints a remove symbol in a red circle.
     */
    void paint( Graphics2D g ) {
      g = newZoomInvariant(g);

      g.scale(1.5, 1.5);
      g.setColor(Color.RED);
      g.fillOval(0, 0, 16, 16);
      g.setColor(Color.WHITE);
      g.fillRect(4, 7, 8, 2);

      g.dispose();
    }
  }

  /**
   * Displays an inline icon chooser.
   */
  private class IconButton extends InlineControl {
    private final Icon icon;
    private final IconChooser iconChooser;
    private final HoverButton parent;
    private boolean visible;

    IconButton( final HoverButton parent ) {
      super(30);
      this.icon = MindMapUtil.getIcon("smiley-happy-24.png");
      this.parent = parent;
      iconChooser = new IconChooser(this);
      visible = false;
    }

    /**
     * Returns <em>Choose a state icon.</em>
     * @return <em>Choose a state icon.</em>
     */
    String getToolTipText() {
      return "Choose a state icon.";
    }

    /**
     * Paints a smiley symbol.
     */
    void paint( Graphics2D g ) {
      if (icon != null) {
        g = newZoomInvariant(g);

        icon.paintIcon(view.getRootPane(), g, 0, 0);

        g.dispose();
      }
    }

    /**
     * Sets the target node for this control's edit action.
     * @param node the node being edited.
     */
    void setNode( final Node node ) {
      super.setNode(node);
      iconChooser.setNode(node);
    }

    /**
     * Toggles visibility of the inline icon chooser.
     */
    void action() {
      toggleVisibility();
    }

    /**
     * Toggles visibility of the inline icon chooser.
     */
    void toggleVisibility() {
      if (visible) {
        view.removeDrawable(iconChooser);
      } else if (!parent.showsChooser) {
        view.addDrawable(iconChooser);
      } else {
        return;
      }
      parent.showsChooser = !parent.showsChooser;
      visible = !visible;
      view.updateView();
    }

    /**
     * Returns <code>true</code> if the inline icon chooser is currently visible
     * and <code>false</code> otherwise.
     * @return <code>true</code> if the inline icon chooser is currently visible
     * and <code>false</code> otherwise.
     */
    boolean isVisible() {
      return visible;
    }
  }

  /**
   * Lets a user choose one of several different icons.
   */
  private class IconChooser extends InlineControl implements Drawable, MouseInputEditorProvider {
    private static final int V_OFFSET = 2;
    private static final int H_OFFSET = 2;

    private final IconButton parent;
    private final StateIcon[] icons;

    IconChooser( final IconButton parent ) {
      super(36, 0);
      this.parent = parent;
      final StateIconProvider provider = StateIconProvider.instance;
      icons = new StateIcon[] {
              StateIconProvider.NULL_ICON,
              provider.getIcon("smiley-happy"),
              provider.getIcon("smiley-not-amused"),
              provider.getIcon("smiley-grumpy"),
              provider.getIcon("abstract-green"),
              provider.getIcon("abstract-red"),
              provider.getIcon("abstract-blue"),
              provider.getIcon("questionmark"),
              provider.getIcon("exclamationmark"),
              provider.getIcon("delete"),
              provider.getIcon("checkmark"),
              provider.getIcon("star"),
      };

      int maxW = 0;
      int maxH = 0;
      int w = 0;
      int h = 0;
      final int l = icons.length;
      for (int i = 0; i < l; ++i) {
        final Icon icon = icons[i];
        w += icon.getIconWidth();
        final int ih = icon.getIconHeight();
        if (maxH < ih) {
          maxH = ih;
        }

        if (i == (l - 1)/2) {
          maxW = w;
          w = 0;
          h = maxH;
          maxH = 0;
        }
      }
      w = Math.max(w, maxW) + (l > 0 ? ((l - 1) / 2) * H_OFFSET : 0);
      h += maxH + V_OFFSET;
      this.yOffset = -h - parent.height;
      this.width = w;
      this.height = h;
    }

    /**
     * Returns <code>null</code>.
     * @return <code>null</code>.
     */
    String getToolTipText() {
      return null;
    }

    /**
     * Unsupported.
     */
    void action() {
      throw new UnsupportedOperationException();
    }

    public Rectangle getBounds() {
      return super.getBounds();
    }

    /**
     * Paints the available icons in this icon chooser.
     */
    public void paint( Graphics2D g ) {
      if (parent.isVisible()) {
        g = newZoomInvariant(g);

        int x = 0;
        int y = 0;
        int maxH = 0;
        final Graph2DView view = HoverButton.this.view;
        for (int i = 0, l = icons.length; i < l; ++i) {
          final Icon icon = icons[i];
          icon.paintIcon(view, g, x, y);

          x += icon.getIconWidth() + H_OFFSET;
          final int h = icon.getIconHeight();
          if (maxH < h) {
            maxH = h;
          }

          if (i == (l - 1)/2) {
            x = 0;
            y = maxH + V_OFFSET;
          }
        }
        g.dispose();
      }
    }

    /**
     * Returns an editor for interactively choosing one of this icon chooser's
     * icons.
     * @param view the view that will host the editor
     * @param x the x coordinate of the mouse event
     * @param y the y coordinate of the mouse event
     * @param hitInfo the HitInfo that may be used to determine what instance to return or <code>null</code>
     * @return an editor for interactively choosing one of this icon chooser's
     * icons.
     */
    public MouseInputEditor findMouseInputEditor(
            final Graph2DView view, final double x, final double y, final HitInfo hitInfo
    ) {
      return getBounds().contains(x, y) ? this : null;
    }

    /**
     * Chooses the icon at the specified relative location in this icon chooser.
     * @param relativeX the x-coordinate of the triggering mouse event relative
     * to this icon chooser's current location.
     * @param relativeY the y-coordinate of the triggering mouse event relative
     * to this icon chooser's current location.
     */
    void action( final double relativeX, final double relativeY ) {
      final StateIcon icon = findIcon(relativeX, relativeY);

      final Node node = this.node;
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      graph.backupRealizers();
      MindMapNodePainter.setStateIcon(graph.getRealizer(node), icon);
      MindMapUtil.updateWidth(graph, node);
      LayoutUtil.layout(graph);
      graph.firePostEvent();

      closeAll();
      view.updateView();
    }

    /**
     * Determines the icon at the specified relative location in this icon
     * chooser.
     * @param relativeX the x-coordinate of the triggering mouse event relative
     * to this icon chooser's current location.
     * @param relativeY the y-coordinate of the triggering mouse event relative
     * to this icon chooser's current location.
     * @return the icon at the specified relative location in this icon
     * chooser.
     */
    private StateIcon findIcon( final double relativeX, final double relativeY ) {
      final double z = 1 / view.getZoom();
      double x = 0;
      double y = 0;
      int maxH = 0;
      for (int i = 0, l = icons.length; i < l; ++i) {
        final StateIcon icon = icons[i];
        final int w = icon.getIconWidth();
        final int h = icon.getIconHeight();
        if (x <= relativeX && relativeX <= x + w * z &&
            y <= relativeY && relativeY <= y + h * z) {
          return icon;
        }

        if (maxH < h) {
          maxH = h;
        }

        if (i == (l - 1)/2) {
          x = 0;
          y = (maxH + V_OFFSET) * z;
        } else {
          x += (w + H_OFFSET) * z;
        }
      }
      return StateIconProvider.NULL_ICON;
    }
  }

  /**
   * Displays an inline color chooser.
   */
  private class ColorButton extends InlineControl {
    private final ColorChooser colorChooser;
    private boolean visible;
    private HoverButton parent;

    ColorButton( final HoverButton parent ) {
      super(60);
      this.parent = parent;
      this.colorChooser = new ColorChooser(this);
      this.visible = false;
    }

    /**
     * Returns <em>Choose a color.</em>
     * @return <em>Choose a color.</em>
     */
    String getToolTipText() {
      return "Choose a color.";
    }

    /**
     * Paints a multi-colored circle.
     */
    void paint(Graphics2D g) {
      g = newZoomInvariant(g);

      g.scale(1.5, 1.5);
      g.setColor(Color.RED);
      g.fillArc(0, 0, 16, 16, 180, -90);
      g.setColor(GREEN);
      g.fillArc(0, 0, 16, 16, 270, -90);
      g.setColor(Color.BLUE);
      g.fillArc(0, 0, 16, 16, 0, -90);
      g.setColor(YELLOW);
      g.fillArc(0,0,16,16,90,-90);

      g.dispose();
    }

    /**
     * Sets the target node for this control's edit action.
     * @param node the node being edited.
     */
    void setNode(final Node node) {
      super.setNode(node);
      colorChooser.setNode(node);
    }

    /**
     * Toggles visibility of the inline color chooser.
     */
    void action() {
      toggleVisibility();
    }

    /**
     * Toggles visibility of the inline color chooser.
     */
    void toggleVisibility() {
      if (visible) {
        view.getGraph2D().removeDrawable(colorChooser);
      } else if (!parent.showsChooser) {
        view.getGraph2D().addDrawable(colorChooser);
      } else {
        return;
      }
      parent.showsChooser = !parent.showsChooser;
      visible = !visible;
      view.updateView();
    }

    /**
     * Returns <code>true</code> if the inline color chooser is currently
     * visible and <code>false</code> otherwise.
     * @return <code>true</code> if the inline color chooser is currently
     * visible and <code>false</code> otherwise.
     */
    boolean isVisible() {
      return visible;
    }
  }

  /**
   * Lets a user choose one of several different colors.
   */
  private class ColorChooser extends InlineControl implements Drawable, MouseInputEditorProvider {
    private static final int WIDTH = 60;
    private static final int HEIGHT = 15;
    private static final int V_OFFSET = 2;

    private final ColorButton parent;
    private final Color[] colors;

    ColorChooser( final ColorButton parent ) {
      super(36, 0);
      this.parent = parent;
      colors = new Color[] {
              MindMapUtil.ORANGE,
              MindMapUtil.RED,
              MindMapUtil.MAGENTA,
              MindMapUtil.GREEN,
              MindMapUtil.DARK_GREEN,
              MindMapUtil.LIGHT_BLUE,
              MindMapUtil.BLUE,
              MindMapUtil.BROWN,
              MindMapUtil.BLACK,
      };
      final int l = colors.length;
      final int w = WIDTH;
      final int h = l > 0 ? l * HEIGHT + (l - 1) * V_OFFSET : 0;
      this.yOffset = -h - parent.height;
      this.width = w;
      this.height = h;
    }

    /**
     * Returns <code>null</code>.
     * @return <code>null</code>.
     */
    String getToolTipText() {
      return null;
    }

    /**
     * Unsupported.
     */
    void action() {
      throw new UnsupportedOperationException();
    }

    public Rectangle getBounds() {
      return super.getBounds();
    }

    /**
     * Paints the available colors in this color chooser.
     */
    public void paint( Graphics2D g ) {
      if (parent.isVisible()) {
        g = newZoomInvariant(g);

        int y = 0;
        for (int i = 0, l = colors.length; i < l; ++i) {
          g.setColor(colors[i]);
          g.fillRect(0, y, WIDTH, HEIGHT);
          y += HEIGHT + V_OFFSET;
        }

        g.dispose();
      }
    }

    /**
     * Returns an editor for interactively choosing one of this color chooser's
     * colors.
     * @param view the view that will host the editor
     * @param x the x coordinate of the mouse event
     * @param y the y coordinate of the mouse event
     * @param hitInfo the HitInfo that may be used to determine what instance to return or <code>null</code>
     * @return an editor for interactively choosing one of this color chooser's
     * colors.
     */
    public MouseInputEditor findMouseInputEditor(
            final Graph2DView view, final double x, final double y, final HitInfo hitInfo
    ) {
      return getBounds().contains(x, y) ? this : null;
    }

    /**
     * Chooses the color at the specified relative location in this color
     * chooser.
     * @param relativeX the x-coordinate of the triggering mouse event relative
     * to this color chooser's current location.
     * @param relativeY the y-coordinate of the triggering mouse event relative
     * to this color chooser's current location.
     */
    void action( final double relativeX, final double relativeY ) {
      final Color color = findColor(relativeY);
      if (color != null) {
        final Graph2D graph = view.getGraph2D();
        final Node node = this.node;
        graph.backupRealizers(new NodeList(node).nodes());
        final NodeRealizer nr = graph.getRealizer(node);
        nr.setFillColor(color);
      }

      closeAll();
      view.updateView();
    }

    /**
     * Determines the color at the specified relative location in this color
     * chooser.
     * @param relativeY the y-coordinate of the triggering mouse event relative
     * to this color chooser's current location.
     * @return the color at the specified relative location in this color
     * chooser or <code>null</code> if there is no corresponding color. 
     */
    private Color findColor( final double relativeY ) {
      final double z = 1 / view.getZoom();
      double y = 0;
      for (int i = 0, l = colors.length; i < l; ++i) {
        if (y <= relativeY && relativeY <= y + HEIGHT * z) {
          return colors[i];
        }
        y += (HEIGHT + V_OFFSET) * z;
      }
      return null;
    }
  }

  /**
   * Abstract base class for inline editing controls.
   */
  private abstract class InlineControl extends AbstractMouseInputEditor {
    Node node;

    final int xOffset;
    int yOffset;
    int width;
    int height;

    InlineControl( final int xOffset ) {
      this(xOffset, -22);
    }

    InlineControl( final int xOffset, final int yOffset ) {
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.width = 24;
      this.height = 24;
    }

    /**
     * Returns a short description for this control.
     * @return a short description for this control.
     */
    abstract String getToolTipText();

    /**
     * Paints the control.
     */
    abstract void paint( Graphics2D g );

    /**
     * Edits the control's current target node in response to mouse clicks.
     */
    abstract void action();

    /**
     * Edits the control's current target node in response to mouse clicks.
     * The default implementation calls {@link #action()}.
     * @param relativeX the x-coordinate of the mouse position relative to the
     * control's location.
     * @param relativeY the x-coordinate of the mouse position relative to the
     * control's location.
     */
    void action( final double relativeX, final double relativeY ) {
      action();
    }

    /**
     * Creates a {@link Graphics2D} instance configured for zoom-invariant
     * painting. {@link Graphics2D} instances created by this method have to be
     * {@link java.awt.Graphics2D#dispose() disposed} after use.
     * @param g the original graphics context. 
     * @return a {@link Graphics2D} instance configured for zoom-invariant
     * painting.
     */
    Graphics2D newZoomInvariant( final Graphics2D g ) {
      final Graphics2D gfx = (Graphics2D) g.create();

      final Rectangle bnds = getBounds();
      gfx.translate(bnds.x, bnds.y);

      final double invZoom = 1 / view.getZoom();
      gfx.scale(invZoom, invZoom);

      return gfx;
    }

    /**
     * Specifies the current target node for the control's edit action.
     * @param node the node to be edited.
     */
    void setNode(final Node node) {
      this.node = node;
    }

    /**
     * Returns the zoom-invariant bounds of this control in the graph (world)
     * coordinate space.
     * @return the zoom-invariant bounds of this control in the graph (world)
     * coordinate space.
     */
    Rectangle getBounds() {
      final Rectangle r = new Rectangle(width, height);
      final NodeRealizer realizer = view.getGraph2D().getRealizer(node);
      final double z2 = 1 / view.getZoom();
      r.x = (int) (realizer.getX() + (xOffset * z2 + mouseX) );
      r.width *= z2;
      r.height *= z2;
      r.y = (int) (realizer.getY() + (yOffset * z2) + animationYOffset);
      return r;
    }

    /**
     * Determines if this control should be activated for the given event.
     * @param event the event that happened
     * @return <code>true</code> if the event position lies inside the bounds
     * of this control and <code>false</code> otherwise.
     */
    public boolean startsEditing( final Mouse2DEvent event ) {
      return getBounds().contains(event.getX(), event.getY());
    }

    /**
     * Handles mouse events while this control is active.
     * The default implementation calls {@link #action(double, double)} for
     * mouse clicks.
     * @param event the event that happened
     */
    public void mouse2DEventHappened( final Mouse2DEvent event ) {
      final double evtX = event.getX();
      final double evtY = event.getY();
      final Rectangle bnds = getBounds();
      if (bnds.contains(evtX, evtY)) {
        if (event.getId() == Mouse2DEvent.MOUSE_CLICKED) {
          action(evtX - bnds.x, evtY- bnds.y);
        }
      } else {
        stopEditing();
      }
    }
  }

  /**
   * ViewMode to handle HoverButton related Mouse Actions.
   * Register a mouse click on free plane and updates the position of the
   * hover button when mouse is moved
   */
  class HoverViewMode extends ViewMode {
    public void mouseClicked( final double x, final double y ) {
      final HitInfo hitInfo = getHitInfo(x, y);
      if (!hitInfo.hasHits()) {
        setNode(null, 0);
        closeAll();
      }
      super.mouseClicked(x, y);
    }

    public void mouseMoved( final double x, final double y ) {
      view.setToolTipText(null);

      final HoverButton controls = HoverButton.this;
      final Node lastNode = controls.currentNode;
      if (lastNode != null) {
        if (controls.getBounds().contains(x, y)) {
          view.setToolTipText(controls.getToolTipText(x, y));
          return;
        }
        if (contains(getGraph2D().getRealizer(lastNode), x, y)) {
          return;
        }
      }

      final HitInfo hitInfo = getHitInfo(x, y);
      if (hitInfo.hasHitNodes()) {
        setNode(hitInfo.getHitNode(), x);
      } else if (lastNode != null) {
        setNode(null, 0);
      }
    }

    private boolean contains( final NodeRealizer nr, final double x, final double y ) {
      return nr.getX() <= x && x <= nr.getX() + nr.getWidth() &&
             nr.getY() <= y && y <= nr.getY() + nr.getHeight();
    }
  }

  /**
   * Iterates over the inline controls provided by the enclosing
   * {@link HoverButton} instance.
   */
  private final class ControlsIterator implements Iterator {
    private int index;

    ControlsIterator( final boolean root ) {
      index = root ? 3 : 0;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
      return index < 5;
    }

    public Object next() {
      if (hasNext()) {
        switch (index++) {
          case 0:
            return delete;
          case 1:
            return color;
          case 2:
            return icon;
          case 3:
            return add;
          case 4:
            return reference;
          default:
            throw new IllegalStateException();
        }
      } else {
        throw new NoSuchElementException();
      }
    }
  }
}
