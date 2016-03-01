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
package demo.layout.hierarchic;

import demo.layout.hierarchic.CellSpanLayoutDemo.Cell;
import demo.layout.hierarchic.CellSpanLayoutDemo.CellColorManager;
import demo.layout.hierarchic.CellSpanLayoutDemo.Span;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.YInsets;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HitInfo;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.SelectionBoxMode;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.Table;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPopupMenu;

/**
 * Provides custom user interaction for cell designer table nodes.
 *
 */
class CellSpanControllerFactory {
  /**
   * Prevents instantiation of factory class.
   */
  private CellSpanControllerFactory() {
  }


  /**
   * Creates a new instance of {@link CellEditMode}. The returned
   * instance will use {@link CellPopupMode} for custom context menus and
   * {@link CellColorMode} for custom marquee selection behavior.
   * @return an instance of {@link CellEditMode}.
   */
  static EditMode newCellEditMode() {
    final CellEditMode editMode = new CellEditMode();
    editMode.setPopupMode(new CellPopupMode());
    editMode.setSelectionBoxMode(new CellColorMode());
    return editMode;
  }

  /**
   * Determines whether or not the <code>CTRL</code> key was pressed when
   * the given mouse event was fired.
   * @param e the mouse event to check.
   * @return <code>true</code> if the <code>CTRL</code> key was pressed when
   * the given mouse event was fired; <code>false</code> otherwise.
   */
  static boolean isCtrlDown( final MouseEvent e ) {
    final int mask = MouseEvent.CTRL_DOWN_MASK;
    return e != null && (e.getModifiersEx() & mask) == mask;
  }


  /**
   * Prevents edge creation while <code>CTRL</code> is pressed by triggering
   * marquee selection instead and prevents creation of "free" nodes, that is
   * nodes without a parent group/table node.
   */
  private static final class CellEditMode extends EditMode {
    /**
     * Prevents edge creation while <code>CTRL</code> is pressed.
     * With this customization, the selection box subordinate mode is triggered
     * when dragging the mouse over a node while pressing <code>CTRL</code>.
     * @param lastPress the last press event
     * @param lastDrag the last drag event
     * @return <code>false</code> if <code>CTRL</code> is pressed or
     * the super implementation result if <code>CTRL</code> is not pressed.
     * @see CellColorMode
     */
    protected boolean isCreateEdgeGesture(
            final MouseEvent lastPress, final MouseEvent lastDrag
    ) {
      if (isCtrlDown(lastPress) || isCtrlDown(lastDrag)) {
        return false;
      } else {
        return super.isCreateEdgeGesture(lastPress, lastDrag);
      }
    }

    /**
     * Prevents node creation if the specified parent node is <code>null</code>.
     * @param graph the graph which resided in the canvas
     * @param x the x coordinate where the mouse was clicked
     * @param y the y coordinate where the mouse was clicked
     * @param parent the parent group node for the newly created node.
     * If <code>null</code>, the new node will be a top level node.
     * @return <code>null</code> if the specified parent node is
     * <code>null</code> or the super implementation result if the specified
     * parent node is not <code>null</code>. 
     */
    protected Node createNode(
            final Graph2D graph, final double x, final double y, final Node parent
    ) {
      if (parent == null) {
        return null;
      } else {
        return super.createNode(graph, x, y, parent);
      }
    }
  }

  /**
   * Provides actions for adding and removing columns and rows in a table
   * nodes.
   */
  private static class CellPopupMode extends PopupMode {
    /**
     * Initializes a new <code>CellPopupMode</code> instance.
     * This mode does not select elements when opening a context menu.
     */
    CellPopupMode() {
      setSelectSubject(false);
    }

    /**
     * Returns the context menu to be opened by this mode.
     * Overwritten to take the world (graph) coordinates of the triggering
     * mouse event into account when populating the context menu for table
     * nodes.
     * @see #getNodePopup(y.base.Node, double, double) 
     */
    protected JPopupMenu getPopup(
            final HitInfo hitInfo,
            final double x, final double y,
            final int popupType
    ) {
      if (POPUP_TYPE_NODE == popupType) {
        return getNodePopup(hitInfo.getHitNode(), x, y);
      } else {
        return super.getPopup(hitInfo, x, y, popupType);
      }
    }

    /**
     * Returns the context menu for nodes.
     * For table nodes, the context menu will provide actions for adding and
     * removing columns and rows.
     * For other (normal) nodes, the context menu will be empty (and will not
     * be displayed).
     */
    JPopupMenu getNodePopup(
            final Node node, final double x, final double y
    ) {
      final JPopupMenu jpm = super.getNodePopup(node);

      final Graph2D graph = getGraph2D();
      final NodeRealizer nr = graph.getRealizer(node);
      if (nr instanceof TableGroupNodeRealizer) {
        final TableGroupNodeRealizer tgnr = (TableGroupNodeRealizer) nr;
        final Table table = tgnr.getTable();
        final Column col = table.columnAt(x, y);
        if (col != null) {
          final Row row = table.rowAt(x, y);
          if (row != null) {
            jpm.add(CellSpanActionFactory.newAddBefore(view, table, col));
            jpm.add(CellSpanActionFactory.newAddAfter(view, table, col));
            jpm.add(CellSpanActionFactory.newRemoveColumn(graph, table, col));
            jpm.addSeparator();
            jpm.add(CellSpanActionFactory.newAddBefore(view, table, row));
            jpm.add(CellSpanActionFactory.newAddAfter(view, table, row));
            jpm.add(CellSpanActionFactory.newRemoveRow(graph, table, row));
          }
        }
      }

      return jpm;
    }
  }

  /**
   * Colors the background of table cells when pressing <code>CTRL</code> while
   * using marquee selection.
   * Removes the background color table cells when pressing <code>CTRL</code>
   * and <code>ALT</code> while using marquee selection.
   */
  private static final class CellColorMode extends SelectionBoxMode {
    /**
     * Handles marquee selection.
     * Overwritten to check whether or not <code>CTRL</code> and
     * <code>ALT</code> were pressed when ending the marquee selection
     * to trigger background coloring instead of element selection.
     * @param sb The position and size of the selection box.
     * @param shiftMode <code>true</code> if shift was pressed when
     */
    protected void selectionBoxAction(
            final Rectangle2D.Double sb, final boolean shiftMode
    ) {
      final MouseEvent e = lastReleaseEvent;
      if (isCtrlDown(e)) {
        final int mask = MouseEvent.ALT_DOWN_MASK;
        final boolean clear = (e.getModifiersEx() & mask) == mask;

        final Graph2D graph = getGraph2D();
        final HierarchyManager hm = graph.getHierarchyManager();
        for (NodeCursor nc = hm.getChildren(null); nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (hm.isGroupNode(node)) {
            final NodeRealizer nr = graph.getRealizer(node);
            if (nr instanceof TableGroupNodeRealizer) {
              setColor((TableGroupNodeRealizer) nr, sb, clear);
              break;
            }
          }
        }
        graph.updateViews();
      } else {
        super.selectionBoxAction(sb, shiftMode);
      }
    }

    /**
     * Sets a new background color for the table cells that intersect the
     * specified selection box rectangle.
     * @param tgnr the realizer holding the table structure.
     * @param sb the selection box rectangle.
     * @param clear if <code>true</code>, the background color of the cells
     * in the selection box is cleared; otherwise an new background color is
     * set. 
     */
    private void setColor(
            final TableGroupNodeRealizer tgnr, final Rectangle2D sb, final boolean clear
    ) {
      final Table table = tgnr.getTable();

      // get a background color that was not yet used
      final Color color = CellColorManager.getInstance(table).nextUnused();
      if (!clear && color == null) {
        return;
      }


      // determine all cells that intersect the given selection box rectangle
      // this code relies on the fact that CellSpanLayoutDemo does not provide
      // a way to created columns in columns or rows in rows
      final Rectangle2D.Double tmp = new Rectangle2D.Double();

      double x = tgnr.getX();
      final YInsets insets = table.getInsets();
      x += insets.left;

      final ArrayList cells = new ArrayList();
      for (int i = 0, n = table.columnCount(); i < n; ++i) {
        final Column col = table.getColumn(i);
        final double w = col.getWidth();

        double y = tgnr.getY();
        y += insets.top;
        for (int j = 0, m = table.rowCount(); j < m; ++j) {
          final Row row = table.getRow(j);
          final double h = row.getHeight();

          tmp.setFrame(x, y, w, h);
          if (sb.intersects(tmp)) {
            cells.add(new Cell(col, row));
          }

          y += h;
        }
        x += w;
      }

      // now set or erase the background color for the cells
      if (!cells.isEmpty()) {
        getGraph2D().backupRealizers((new NodeList(tgnr.getNode())).nodes());
        if (clear) {
          setColor(table, cells, null);
        } else {
          setColor(table, cells, color);
        }
      }
    }

    /**
     * Sets the given background color for the given table cells.
     * This method ensures that the resulting coloring defines valid
     * rectangular cell spans.
     * <p>
     * E.g. suppose cells <code>(c1, r2)</code>, <code>(c2, r2)</code>, and
     * <code>(c3, r2)</code> are colored red and the cells <code>(c2, r1)</code>
     * and <code>(c2, r2)</code> should be newly colored blue, this method will
     * erase the background color from cell <code>(c1, r2)</code> to prevent two
     * disjoint red cell spans.
     * </p><p>
     * Alternatively, suppose cells <code>(c1, r1)</code>,
     * <code>(c2, r1)</code>, <code>(c1, r2)</code>, and <code>(c2, r2)</code>
     * are colored red and cell <code>(c2, r2)</code> should be newly colored
     * blue, this method will erase the background color from cell
     * <code>(c1, r2)</code> to prevent a non-rectangular red cell span. 
     * </p>
     * @param table the table holding the cells whose background color is set.
     * @param cells the cells whose background color is set.
     * @param newColor the new background color.
     */
    private void setColor(
            final Table table, final Collection cells, final Color newColor
    ) {
      final Span newSpan = Span.span(cells);

      final CellColorManager manager = CellColorManager.getInstance(table);
      for (int i = newSpan.minCol, n = newSpan.maxCol + 1; i < n; ++i) {
        for (int j = newSpan.minRow, m = newSpan.maxRow + 1; j < m; ++j) {
          final Column col = table.getColumn(i);
          final Row row = table.getRow(j);
          final Color oldColor = manager.getCellColor(col, row);
          // if the oldColor is null, cell (i,j) does not belong to another
          // cell span and may be colored with no adverse effects
          // otherwise, cell (i,j) belongs to a cell span that may need to
          // be adjusted to remain valid
          if (oldColor != null) {
            final Span oldSpan = Span.find(table, col, row, oldColor);
            if (newSpan.contains(oldSpan)) {
              // replace the color of the entire span
              manager.setCellColor(oldSpan, null);
            } else {
              // determine the cells whose background has to be erased
              // for the old cell span to remain a valid
              final Span cut = cut(newSpan, oldSpan);
              manager.setCellColor(cut, null);
            }
          }
          manager.setCellColor(col, row, newColor);
        }
      }
    }

    /**
     * Determines the cells in <code>oldSpan</code> whose background
     * has to be erased when coloring the cells in <code>newSpan</code>
     * with a different background color.
     */
    private static Span cut( final Span newSpan, final Span oldSpan ) {
      if (oldSpan.contains(newSpan)) {
        if (newSpan.contains(oldSpan)) {
          return oldSpan;
        } else {
          // top cut
          int minSize = size(newSpan, oldSpan, CUT_T);
          Span min = newCut(newSpan, oldSpan, CUT_T);
          // bottom cut
          final int bSize = size(newSpan, oldSpan, CUT_B);
          if (minSize > bSize) {
            minSize = bSize;
            min = newCut(newSpan, oldSpan, CUT_B);
          }
          // left cut
          final int lSize = size(newSpan, oldSpan, CUT_L);
          if (minSize > lSize) {
            minSize = lSize;
            min = newCut(newSpan, oldSpan, CUT_L);
          }
          // right cut
          final int rSize = size(newSpan, oldSpan, CUT_R);
          if (minSize > rSize) {
            minSize = rSize;
            min = newCut(newSpan, oldSpan, CUT_R);
          }

          return min;
        }
      }


      // whether or not the new span starts vertically inside the old span
      final boolean inT = oldSpan.minRow < newSpan.minRow;
      // whether or not the new span ends vertically inside the old span
      final boolean inB = newSpan.maxRow < oldSpan.maxRow;
      // whether or not the new span starts horizontally inside the old span
      final boolean inL = oldSpan.minCol < newSpan.minCol;
      // whether or not the new span ends horizontally inside the old span
      final boolean inR = newSpan.maxCol < oldSpan.maxCol;
      // whether or not the new span is completely inside the old span's
      // horizontal range
      final boolean inH = inL && inR;
      // whether or not the new span is completely inside the old span's
      // vertical range
      final boolean inV = inT && inB;

      // whether or not the new span starts vertically outside the old span
      final boolean outT = newSpan.minRow <= oldSpan.minRow;
      // whether or not the new span ends vertically outside the old span
      final boolean outB = oldSpan.maxRow <= newSpan.maxRow;
      // whether or not the new span starts horizontally outside the old span
      final boolean outL = newSpan.minCol <= oldSpan.minCol;
      // whether or not the new span ends horizontally outside the old span
      final boolean outR = oldSpan.maxCol <= newSpan.maxCol;
      // whether or not the new span completely covers the old span's
      // horizontal range
      final boolean outH = outL && outR;
      // whether or not the new span completely covers the old span's
      // vertical range
      final boolean outV = outT && outB;

      // whether or not the new span starts outside and ends inside the old
      // span's vertical range
      final boolean cutT = outT && inB;
      // whether or not the new span starts inside and ends outside the old
      // span's vertical range
      final boolean cutB = inT && outB;
      // whether or not the new span starts outside and ends inside the old
      // span's horizontal range
      final boolean cutL = outL && inR;
      // whether or not the new span starts inside and ends outside the old
      // span's horizontal range
      final boolean cutR = inL && outR;


      // case 1:
      //    +-----------+
      //    |           |
      // ---+-----------+---
      // ###################
      // ---+-----------+---
      //    |           |
      //    +-----------+
      if (outH && inV) {
        return newCut(newSpan, oldSpan, CUT_T, CUT_B);
      }

      // case 2:
      //    +-----------+             ##########|
      //    |           |             ##########|
      // ---+-----+     |             ##########+----+
      // #########|                   ##########|    |
      // ---+-----+     |             ##########|    |
      //    |           |             ##########|    |
      //    +-----------+             ##########|    |
      //                              ##########+----+
      //                              ##########|
      //                              ##########|
      if (cutL && (inV || outV)) {
        return newCut(newSpan, oldSpan, CUT_L);
      }

      // case 3:
      //    +-----------+                  |##########
      //    |           |                  |##########
      //    |     +-----+---          +----+##########
      //    |     |#########          |    |##########
      //    |     +-----+---          |    |##########
      //    |           |             |    |##########
      //    +-----------+             |    |##########
      //                              +----+##########
      //                                   |##########
      //                                   |##########
      if (cutR && (inV || outV)) {
        return newCut(newSpan, oldSpan, CUT_R);
      }

      // case 4:
      //         |#|
      //         |#|
      //    +----+#+----+
      //    |    |#|    |
      //    |    |#|    |
      //    |    |#|    |
      //    |    |#|    |
      //    +----+#+----+
      //         |#|
      //         |#|
      if (inH && outV) {
        return newCut(newSpan, oldSpan, CUT_L, CUT_R);
      }

      // case 5:
      //         |#|                  ###################
      //         |#|                  ###################
      //    +----+#+----+             ###################
      //    |    |#|    |             ###################
      //    |    |#|    |             ###################
      //    |    +-+    |             ---+-----------+---
      //    |           |                |           |
      //    +-----------+                +-----------+
      //
      //
      if ((inH || outH) && cutT) {
        return newCut(newSpan, oldSpan, CUT_T);
      }

      // case 6:
      //
      //
      //    +-----------+                +-----------+
      //    |           |                |           |
      //    |    +-+    |             ---+-----------+---
      //    |    |#|    |             ###################
      //    |    |#|    |             ###################
      //    +----+#+----+             ###################
      //         |#|                  ###################
      //         |#|                  ###################
      if ((inH || outH) && cutB) {
        return newCut(newSpan, oldSpan, CUT_B);
      }


      // case 7:
      // ##########|
      // ##########|
      // ##########+----+
      // ##########|    |
      // ##########|    |
      // ---+------+    |
      //    |           |
      //    +-----------+
      //
      //
      if (cutL && cutT) {
        return newCut(newSpan, oldSpan, CUT_T, CUT_L);
      }

      // case 8:
      //         |##########
      //         |##########
      //    +----+##########
      //    |    |##########
      //    |    |##########
      //    |    +------+---
      //    |           |
      //    +-----------+
      //
      //
      if (cutR && cutT) {
        return newCut(newSpan, oldSpan, CUT_T, CUT_R);
      }

      // case 9:
      //
      //
      //    +-----------+
      //    |           |
      //    |    +------+---
      //    |    |##########
      //    |    |##########
      //    +----+##########
      //         |##########
      //         |##########
      if (cutR && cutB) {
        return newCut(newSpan, oldSpan, CUT_B, CUT_R);
      }

      // case 10:
      //
      //
      //    +-----------+
      //    |           |
      // ---+------+    |
      // ##########|    |
      // ##########|    |
      // ##########+----+
      // ##########|
      // ##########|
      if (cutL && cutB) {
        return newCut(newSpan, oldSpan, CUT_B, CUT_L);
      }


      // should never happen
      return oldSpan;
    }


    /** Top cut. */
    private static final byte CUT_T = 1;
    /** Bottom cut. */
    private static final byte CUT_B = 2;
    /** Left cut. */
    private static final byte CUT_L = 3;
    /** Right cut. */
    private static final byte CUT_R = 4;

    /**
     * Calculates the number of cells in a directional intersection of
     * the two given cell spans. 
     * @param newSpan the new cell span to which the direction applies.
     * @param oldSpan the old cell span.
     * @param cut the direction of the intersection. Has to be one of
     * <ul>
     * <li>{@link #CUT_T},</li>
     * <li>{@link #CUT_B},</li>
     * <li>{@link #CUT_L}, or</li>
     * <li>{@link #CUT_R}.</li>
     * </ul>
     * @return the number of cells in a directional intersection of
     * the two given cell spans.
     */
    private static int size( final Span newSpan, final Span oldSpan, final byte cut ) {
      final int maxCol = CUT_L == cut ? newSpan.maxCol : oldSpan.maxCol;
      final int minCol = CUT_R == cut ? newSpan.minCol : oldSpan.minCol;
      final int maxRow = CUT_T == cut ? newSpan.maxRow : oldSpan.maxRow;
      final int minRow = CUT_B == cut ? newSpan.minRow : oldSpan.minRow;
      return (maxCol - minCol + 1) * (maxRow - minRow + 1);
    }

    /**
     * Calculates the cells in a directional intersection of the two given cell
     * spans. 
     * @param newSpan the new cell span to which the direction applies.
     * @param oldSpan the old cell span.
     * @param cut the direction of the intersection. Has to be one of
     * <ul>
     * <li>{@link #CUT_T},</li>
     * <li>{@link #CUT_B},</li>
     * <li>{@link #CUT_L}, or</li>
     * <li>{@link #CUT_R}.</li>
     * </ul>
     * @return the cells in a directional intersection of the two given cell
     * spans.
     */
    private static Span newCut( final Span newSpan, final Span oldSpan, final byte cut ) {
      final int maxCol = CUT_L == cut ? newSpan.maxCol : oldSpan.maxCol;
      final int minCol = CUT_R == cut ? newSpan.minCol : oldSpan.minCol;
      final int maxRow = CUT_T == cut ? newSpan.maxRow : oldSpan.maxRow;
      final int minRow = CUT_B == cut ? newSpan.minRow : oldSpan.minRow;
      return Span.manual(minCol, maxCol, minRow, maxRow);
    }

    /**
     * Calculates the smaller of the two desired directional intersections
     * of the two given cell spans.
     * @param newSpan the new cell span to which the direction applies.
     * @param oldSpan the old cell span.
     * @param cut1 the first possible cut direction. Has to be one of
     * {@link #CUT_T}, {@link #CUT_B}, {@link #CUT_L}, or {@link #CUT_R}.
     * @param cut2 the second possible cut direction. Has to be one of
     * {@link #CUT_T}, {@link #CUT_B}, {@link #CUT_L}, or {@link #CUT_R}.
     * @return the smaller of the two desired directional intersections
     * of the two given cell spans.
     */
    private static Span newCut(
            final Span newSpan, final Span oldSpan,
            final byte cut1, final byte cut2
    ) {
      return size(newSpan, oldSpan, cut2) < size(newSpan, oldSpan, cut1)
             ? newCut(newSpan, oldSpan, cut2)
             : newCut(newSpan, oldSpan, cut1);
    }
  }
}
