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

import demo.layout.hierarchic.CellSpanLayoutDemo.CellColorManager;

import y.geom.YInsets;
import y.view.GenericNodeRealizer;
import y.view.MultiplexingNodeEditor;
import y.view.NodeRealizer;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.Table;
import y.view.tabular.TableNodePainter;
import y.view.tabular.TableSizeEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Provides the visualization for cell designer table nodes.
 * This visualization supports individual background colors for each table cell.
 *
 */
class CellSpanRealizerFactory {
  /** Configuration ID for cell span designer table nodes. */
  private static final String GRID_CONFIGURATION = "GRID_CONFIGURATION";


  /**
   * Prevents instantiation of factory class.
   */
  private CellSpanRealizerFactory() {
  }


  /**
   * Registers the configuration used for cell designer table nodes.
   */
  static void initConfigurations() {
    // configure the visualization of cell designer table nodes
    final TableNodePainter painter = TableNodePainter.newDefaultInstance();
    painter.setSubPainter(TableNodePainter.PAINTER_COLUMN_FOREGROUND, null);
    painter.setSubPainter(TableNodePainter.PAINTER_COLUMN_BACKGROUND, null);
    painter.setSubPainter(TableNodePainter.PAINTER_ROW_FOREGROUND, new CellPainter(true));
    painter.setSubPainter(TableNodePainter.PAINTER_ROW_BACKGROUND, new CellPainter(false));
    painter.setSubPainter(TableNodePainter.PAINTER_TABLE_BACKGROUND, new Background());

    final Map map = TableGroupNodeRealizer.createDefaultConfigurationMap();
    map.put(GenericNodeRealizer.Painter.class, painter);
    map.put(GenericNodeRealizer.UserDataHandler.class, new CellColorsDataHandler());

    // restrict interactive editing of cell designer table nodes to resizing
    // the table's columns and rows
    // especially re-ordering columns and rows is not supported preventing
    // the possibility to nest columns in columns and/or rows in rows
    final TableSizeEditor tableSizeEditor = new TableSizeEditor();
    tableSizeEditor.setResizePolicy(TableSizeEditor.RESIZE_POLICY_IGNORE_CONTENT);
    final MultiplexingNodeEditor editor = new MultiplexingNodeEditor();
    editor.addNodeEditor(tableSizeEditor);
    map.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, editor);


    // register the configuration
    GenericNodeRealizer.getFactory().addConfiguration(GRID_CONFIGURATION, map);
  }


  /**
   * Paints the background of a cell designer table node.
   * The main cell area, i.e. the intersection of the union of all columns and
   * the union of all rows, is filled with the node's main fill color.
   * The border area, i.e. column top and bottom insets and row left and right
   * insets, is fill with the node's secondary fill color.
   */
  private static final class Background implements GenericNodeRealizer.Painter {
    /**
     * Paints the table area represented by the given node realizer in
     * high-detail mode. Delegates painting to
     * {@link #paintImpl(y.view.NodeRealizer, java.awt.Graphics2D)}.
     */
    public void paint(final NodeRealizer dummy, final Graphics2D gfx) {
      paintImpl(dummy, gfx);
    }

    /**
     * Paints the table area represented by the given node realizer in
     * low-detail mode. Delegates painting to
     * {@link #paintImpl(y.view.NodeRealizer, java.awt.Graphics2D)}.
     */
    public void paintSloppy(final NodeRealizer dummy, final Graphics2D gfx) {
      paintImpl(dummy, gfx);
    }

    /**
     * Paints the table area represented by the given node realizer.
     */
    private void paintImpl(final NodeRealizer dummy, final Graphics2D gfx) {
      final Color oldColor = gfx.getColor();

      final Rectangle2D.Double bnds = new Rectangle2D.Double(
              dummy.getX(), dummy.getY(), dummy.getWidth(), dummy.getHeight());

      // at this point, the dummy geometry should match the whole table
      // node geometry
      final Color fc2 = dummy.getFillColor2();
      if (fc2 != null) {
        gfx.setColor(fc2);
        gfx.fill(bnds);
      }

      // paint the main cell area
      final Color fc = dummy.getFillColor();
      if (fc != null) {
        final Table table = TableNodePainter.getTable(dummy);

        // all the sample diagrams that can be used in CellSpanLayoutDemo
        // used uniform insets (i.e. all columns and all rows have the same
        // inset on all sides)
        // moreover, there are no columns in columns or rows in rows in the
        // aforementioned samples
        // therefore, the main cell area is simply the bounds of the table
        // node minus those insets
        final YInsets cInsets = table.getColumn(0).getInsets();
        final YInsets rInsets = table.getRow(0).getInsets();

        bnds.setFrame(
                bnds.getX() + rInsets.left,
                bnds.getY() + cInsets.top,
                bnds.getWidth() - rInsets.left - rInsets.right,
                bnds.getHeight() - cInsets.top - cInsets.bottom);
        gfx.setColor(fc);
        gfx.fill(bnds);
      }

      gfx.setColor(oldColor);
    }
  }

  /**
   * Paints individual cells in a cell designer table node.
   * This painter has to be used as {@link TableNodePainter} subordinate
   * painter for rows.
   */
  private static final class CellPainter implements GenericNodeRealizer.Painter {
    /**
     * Determines whether to paint cell foregrounds or colored cell background.
     */
    private final boolean foreground;

    /**
     * Initializes a new <code>CellPainter</code> instance.
     */
    CellPainter( final boolean foreground ) {
      this.foreground = foreground;
    }

    /**
     * Paints table cells in high-detail mode. Delegates painting to
     * {@link #paintImpl(y.view.NodeRealizer, java.awt.Graphics2D)}.
     */
    public void paint(final NodeRealizer dummy, final Graphics2D gfx) {
      paintImpl(dummy, gfx);
    }

    /**
     * Paints table cells in low-detail mode. Delegates painting to
     * {@link #paintImpl(y.view.NodeRealizer, java.awt.Graphics2D)}.
     */
    public void paintSloppy(final NodeRealizer dummy, final Graphics2D gfx) {
      paintImpl(dummy, gfx);
    }

    /**
     * Paints table cells. This method assumes to be used as a
     * {@link TableNodePainter} subordinate painter for rows.
     */
    private void paintImpl(final NodeRealizer dummy, final Graphics2D gfx) {
      final Row row = TableNodePainter.getRow(dummy);

      double x = dummy.getX();
      final double y = dummy.getY();
      final double h = dummy.getHeight();
      final Rectangle2D.Double bnds = new Rectangle2D.Double(
              x, y, dummy.getWidth(), h);
      final YInsets insets = row.getInsets();
      x += insets.left;

      final Color oldColor = gfx.getColor();

      final Table table = TableNodePainter.getTable(dummy);
      final Color fg = dummy.getLineColor();
      final CellColorManager manager = CellColorManager.getInstance(table);
      for (int i = 0, n = table.columnCount(); i < n; ++i) {
        final Column col = table.getColumn(i);
        final Color color = foreground ? fg : manager.getCellColor(col, row);
        final double w = col.getWidth();
        if (color != null) {
          bnds.setFrame(x, y, w, h);
          gfx.setColor(color);
          if (foreground) {
            gfx.draw(bnds);
          } else {
            gfx.fill(bnds);
          }
        }
        x += w;
      }

      gfx.setColor(oldColor);
    }
  }

  /**
   * Copies user data of type {@link CellColorManager} from one
   * realizer to another.
   */
  private static final class CellColorsDataHandler
          implements GenericNodeRealizer.UserDataHandler {
    /**
     * Writes user data in <code>YGF</code> file format.
     * @throws UnsupportedOperationException <code>YGF</code> is not supported. 
     */
    public void storeUserData(
            final NodeRealizer context, final Object userData, final ObjectOutputStream oos
    ) throws IOException {
      throw new UnsupportedOperationException();
    }

    /**
     * Reads user data from <code>YGF</code> file format.
     * @throws UnsupportedOperationException <code>YGF</code> is not supported. 
     */
    public Object readUserData(
            final NodeRealizer context, final ObjectInputStream ois
    ) throws IOException {
      throw new UnsupportedOperationException();
    }

    /**
     * Copies user data of type {@link CellColorManager} from one
     * realizer to another.
     */
    public Object copyUserData(
            final NodeRealizer srcContext,
            final Object srcData,
            final NodeRealizer targetContext
    ) {
      return new CellColorManager((CellColorManager) srcData);
    }
  }
}
