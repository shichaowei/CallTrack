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

import y.view.GenericNodeRealizer;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupFeature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

/**
 * A painter that paints group nodes in an isometric fashion.
 */
public class IsometryGroupPainter implements GenericNodeRealizer.Painter, GenericNodeRealizer.ContainsTest {
  static final int ICON_GAP = 2;
  static final int ICON_HEIGHT = 16;
  static final int ICON_WIDTH = 16;

  private final IsometryNodePainter painterDelegate;

  public IsometryGroupPainter(IsometryNodePainter painter) {
    painterDelegate = painter;
  }

  public void paint(NodeRealizer context, Graphics2D graphics) {
    // delegate node painting
    painterDelegate.paint(context, graphics);

    // paint group state icon
    // save the original graphics context
    final AffineTransform oldTransform = graphics.getTransform();
    final Font oldFont = graphics.getFont();
    final Color oldColor = graphics.getColor();

    // calculate the corners of the node in the view space.
    final double[] corners = new double[16];
    final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(context);
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(context.getX(), context.getY(), corners);

    // the lower corner is the anchor of the label.
    final double anchorX = corners[IsometryData.C3_X];
    final double anchorY = corners[IsometryData.C3_Y];

    // set the transformation from the layout space into the view space on the graphics context.
    graphics.translate(anchorX, anchorY);
    graphics.transform(new AffineTransform(
        new double[]{
            IsometryData.M_TO_VIEW_11,
            IsometryData.M_TO_VIEW_21,
            IsometryData.M_TO_VIEW_12,
            IsometryData.M_TO_VIEW_22}));
    graphics.translate(-anchorX, -anchorY);

    // determine position of the icon
    final int x = (int) anchorX + ICON_GAP;
    final int y = (int) (anchorY - ICON_HEIGHT - ICON_GAP);

    // paint icon border
    graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
    graphics.setColor(Color.WHITE);
    graphics.fillRect(x, y, ICON_WIDTH, ICON_HEIGHT);
    graphics.setColor(Color.BLACK);
    graphics.drawRect(x, y, ICON_WIDTH, ICON_HEIGHT);

    // paint "+" (folder) or "-" (group)
    final Line2D line = new Line2D.Double(x + ICON_WIDTH * 0.25, y + ICON_HEIGHT * 0.5,
        x + ICON_WIDTH * 0.75, y + ICON_HEIGHT * 0.5);
    graphics.draw(line);
    if (context instanceof GroupFeature && ((GroupFeature) context).isGroupClosed()) {
      line.setLine(x + ICON_WIDTH * 0.5, y + ICON_HEIGHT * 0.25,
          x + ICON_WIDTH * 0.5, y + ICON_HEIGHT * 0.75);
      graphics.draw(line);
    }

    // Restore the original graphics context.
    graphics.setTransform(oldTransform);
    graphics.setFont(oldFont);
    graphics.setColor(oldColor);
  }

  public void paintSloppy(NodeRealizer context, Graphics2D graphics) {
    painterDelegate.paintSloppy(context, graphics);
  }

  public boolean contains(NodeRealizer context, double x, double y) {
    return painterDelegate.contains(context, x, y);
  }

  /**
   * Checks whether or not the given coordinates lie within the group state icon.
   */
  public static boolean hitsGroupStateIcon(NodeRealizer context, double x, double y) {
    // calculate the corners of the node in the view space.
    final double[] corners = new double[16];
    final IsometryData isometryData = IsometryRealizerFactory.getIsometryData(context);
    isometryData.calculateCorners(corners);
    IsometryData.moveTo(context.getX(), context.getY(), corners);

    // the lower corner is the anchor of the label.
    final double anchorX = corners[IsometryData.C3_X];
    final double anchorY = corners[IsometryData.C3_Y];

    // move the given mouse coordinates by anchor and transform them into layout space
    // that way, the hit test can use a non-transformed rectangle
    final double mouseX = IsometryData.toLayoutX(x - anchorX, y - anchorY);
    final double mouseY = IsometryData.toLayoutY(x - anchorX, y - anchorY);

    // return whether or not the mouse is located in the icons rectangle
    return mouseX > ICON_GAP && mouseX < ICON_WIDTH + ICON_GAP
        && mouseY < -ICON_GAP && mouseY > -ICON_HEIGHT - ICON_GAP;
  }
}
