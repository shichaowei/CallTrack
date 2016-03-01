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

import y.view.LineType;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

/**
 * Central repository for state icons.
 */
public class StateIconProvider {
  /**
   * Singleton instance of the icon repository.
   */
  public static final StateIconProvider instance = new StateIconProvider();

  /**
   * State icon representing the "icon-less" state.
   */
  public static final StateIcon NULL_ICON = new StateIcon(null, new NullIconImpl());


  /**
   * Stores the available state icons.
   */
  private final Map icons;
  
  private StateIconProvider() {
    icons = new HashMap();
    add("smiley-happy");
    add("smiley-not-amused");
    add("smiley-grumpy");
    add("abstract-green");
    add("abstract-red");
    add("abstract-blue");
    add("questionmark");
    add("exclamationmark");
    add("delete");
    add("checkmark");
    add("star");
  }

  /**
   * Adds an image based state icon.
   * @param name symbolic icon name as well as file name prefix. 
   */
  private void add( final String name ) {
    icons.put(name, new StateIcon(name, MindMapUtil.getIcon(name + "-16.png")));
  }

  /**
   * Returns the state icon for the specified name.
   * @param iconName one of
   * <ul>
   * <li><code>smiley-happy</code></li>
   * <li><code>smiley-not-amused</code></li>
   * <li><code>smiley-grumpy</code></li>
   * <li><code>abstract-green</code></li>
   * <li><code>abstract-red</code></li>
   * <li><code>abstract-blue</code></li>
   * <li><code>questionmark</code></li>
   * <li><code>exclamationmark</code></li>
   * <li><code>delete</code></li>
   * <li><code>checkmark</code></li>
   * <li><code>star</code></li>
   * </ul>
   * @return the state icon for the specified name or <code>null</code>
   * if there is no such icon.
   */
  public StateIcon getIcon(String iconName) {
    return (StateIcon) icons.get(iconName);
  }


  /**
   * Associates a name to an icon.
   */
  public static final class StateIcon implements Icon {
    private final String name;
    private final Icon icon;

    /**
     * Initializes a new <code>StateIcon</code> instance with the given name.
     * @param name the name of the state icon.
     * @param icon the actual visual representation of the state icon.
     */
    public StateIcon( final String name, final Icon icon ) {
      this.name = name;
      this.icon = icon;
    }

    /**
     * Returns this icon's name.
     * @return this icon's name.
     */
    public String getName() {
      return name;
    }

    public int getIconHeight() {
      return icon.getIconHeight();
    }

    public int getIconWidth() {
      return icon.getIconWidth();
    }

    public void paintIcon(
            final Component c, final Graphics g, final int x, final int y
    ) {
      icon.paintIcon(c, g, x, y);
    }
  }

  /**
   * Displays a black, crossed-out square.
   */
  private static final class NullIconImpl implements Icon {
    private static final int ICON_SIZE = 16;

    public int getIconHeight() {
      return ICON_SIZE;
    }

    public int getIconWidth() {
      return ICON_SIZE;
    }

    public void paintIcon( final Component c, final Graphics g, final int x, final int y ) {
      final Graphics2D gfx = (Graphics2D) g;

      final Color oldColor = gfx.getColor();
      gfx.setColor(Color.BLACK);
      final Stroke oldStroke = gfx.getStroke();
      gfx.setStroke(LineType.LINE_1);

      final int offset = 2;
      final int w = getIconWidth();
      final int h = getIconHeight();
      final int innerWidth = w - offset;
      final int innerHeight = h - offset;
      gfx.drawRect(x, y, x + w, y + h);
      gfx.drawLine(x + offset, y + offset, x + innerWidth, y + innerHeight);
      gfx.drawLine(x + offset, y + innerHeight, x + innerWidth, y + offset);

      gfx.setStroke(oldStroke);
      gfx.setColor(oldColor);
    }
  }
}
