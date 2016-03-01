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

import demo.view.DemoBase;
import y.io.GraphMLIOHandler;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.MoveSelectionMode;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.Table;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Demonstrates {@link y.layout.hierarchic.IncrementalHierarchicLayouter}'s
 * support for multi-cells in {@link y.layout.grid.PartitionGrid}s.
 * <p>
 * Multi-cells impose less restrictions on node placement than normal cells:
 * A node that belongs to a multi-cell may be placed in each of the multi-cell's
 * columns and rows.
 * </p><p>
 * A new cell span may be created by dragging the mouse across the cells to
 * combine while holding down <code>CTRL</code>.
 * An existing span may be removed by dragging the mouse across the combined
 * cells while holding down <code>CTRL</code> and <code>ALT</code>.
 * </p>
 * 
 */
public class CellSpanLayoutDemo extends DemoBase {
  // registers the configuration used for cell designer table nodes
  static {
    CellSpanRealizerFactory.initConfigurations();
  }

  /**
   * Initializes a new <code>CellSpanLayoutDemo</code> instance.
   * Displays a sample diagram by default.
   */
  public CellSpanLayoutDemo() {
    this(null);
  }

  /**
   * Initializes a new <code>CellSpanLayoutDemo</code> instance.
   * Displays sample 1 and the documentation referenced by the given file path.
   * @param helpFilePath the file path for the HTML documentation to display. 
   */
  public CellSpanLayoutDemo( final String helpFilePath ) {
    initGraph(view.getGraph2D());
    addHelpPane(helpFilePath);
  }

  /**
   * Creates a new {@link HierarchyManager} for the displayed graph.
   */
  protected void initialize() {
    new HierarchyManager(view.getGraph2D());
  }

  /**
   * Creates a custom delete selection action that prevents table nodes from
   * being deleted.
   */
  protected Action createDeleteSelectionAction() {
    return CellSpanActionFactory.newDeleteSelection(view);
  }

  /**
   * Registers keyboard actions.
   * Overwritten to remove grouping related keyboard shortcuts.
   * This demo requires exactly two hierarchy levels with a single top-level
   * table node and an arbitrary number of normal child nodes.
   * With grouping keyboard shortcuts, this assumption would be easily violated.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    final ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      final Object[] keys = {
          Graph2DViewActions.CLOSE_GROUPS,
          Graph2DViewActions.OPEN_FOLDERS,
          Graph2DViewActions.GROUP_SELECTION,
          Graph2DViewActions.FOLD_SELECTION,
          Graph2DViewActions.UNGROUP_SELECTION,
          Graph2DViewActions.UNFOLD_SELECTION,
      };
      for (int i = 0; i < keys.length; ++i) {
        amap.remove(keys[i]);
      }
    }
  }

  /**
   * Creates a custom mode for interactive editing.
   * This custom mode ...
   * <ul>
   * <li>
   *   ... uses marquee selection with <code>CTRL</code> pressed to color
   *   table cells
   * </li><li>
   *   ... uses marquee selection with <code>CTRL</code> and <code>ALT</code>
   *   pressed to reset the color of table cells
   * </li><li>
   *   ... provides a custom context menu with actions for creating and
   *   removing table columns and rows 
   * </li><li>
   *   ... prevents node creation outside of group/table nodes
   * </li>
   * </ul>
   */
  protected EditMode createEditMode() {
    final EditMode editMode = configureEditMode(
            CellSpanControllerFactory.newCellEditMode());

    // prevents nodes from being moved out of the demo's table node
    final MoveSelectionMode msm = new MoveSelectionMode();
    msm.setGroupReassignmentEnabled(false);
    editMode.setMoveSelectionMode(msm);

    // enable resizing table columns and rows
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);

    // enable node creation when clicking on a table/group node
    // necessary because CellEditMode prevents node creation when clicking
    // on empty space
    editMode.setChildNodeCreationEnabled(true);

    return editMode;
  }

  /**
   * Creates a {@link GraphMLIOHandler} that supports reading/writing
   * the individual background colors of table nodes stored in
   * {@link CellColorManager} instances.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    return CellSpanIoSupport.configure(super.createGraphMLIOHandler());
  }

  /**
   * Turns off clipboard support.
   * This demo assumes a single top-level table node. With clipboard support,
   * this assumption would be easily violated.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  /**
   * Provides controls for displaying different sample diagrams.
   */
  protected JMenuBar createMenuBar() {
    final JMenu jm = new JMenu("Samples");
    jm.add(CellSpanActionFactory.newSampleAction(
            "Sample 1", this, "resource/CellSpanLayoutDemoS01.graphml"));
    jm.add(CellSpanActionFactory.newSampleAction(
            "Sample 2", this, "resource/CellSpanLayoutDemoS02.graphml"));
    jm.add(CellSpanActionFactory.newSampleAction(
            "Sample 3", this, "resource/CellSpanLayoutDemoS03.graphml"));
    jm.add(CellSpanActionFactory.newSampleAction(
            "Sample 4", this, "resource/CellSpanLayoutDemoS04.graphml"));
    jm.add(CellSpanActionFactory.newSampleAction(
            "Sample 5", this, "resource/CellSpanLayoutDemoS05.graphml"));

    final JMenuBar jmb = super.createMenuBar();
    jmb.add(jm);
    return jmb;
  }

  /**
   * Provides controls for switching from design to diagram and vice versa.
   * In design mode, the tabular cell structure of the diagram may be modified.
   * In diagram mode, the previously defined cell structure is laid out. 
   */
  protected JToolBar createToolBar() {
    final JToolBar jtb = super.createToolBar();
    jtb.addSeparator();

    // use two toggle buttons which enable/disable each other to signal clearly
    // that it is possible to switch between laid out diagram and design view
    final JToggleButton tb1 = new JToggleButton();
    final JToggleButton tb2 = new JToggleButton();
    tb1.setAction(CellSpanActionFactory.newSwitchViewStateAction("Diagram", view, tb2));
    jtb.add(tb1);
    tb2.setSelected(true);
    tb2.setAction(CellSpanActionFactory.newSwitchViewStateAction("Design", view, tb1));
    tb2.setEnabled(false);
    jtb.add(tb2);
    return jtb;
  }

  /**
   * Loads graph data from the resource with the given name.
   * Overwritten for access from {@link CellSpanActionFactory}.
   * @param resource the path name of the resource to load.
   */
  protected void loadGraph( final String resource ) {
    super.loadGraph(resource);
  }

  /**
   * Returns the main diagram view.
   * Exists for access from {@link CellSpanActionFactory}.
   * @return the main diagram view.
   */
  protected Graph2DView getView() {
    return view;
  }

  /**
   * Displays a sample diagram.
   */
  private void initGraph(final Graph2D graph) {
    graph.clear();
    loadGraph("resource/CellSpanLayoutDemoS01.graphml");
    getUndoManager().resetQueue();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new CellSpanLayoutDemo("resource/cellspanlayouthelp.html")).start();
      }
    });
  }


  /**
   * Returns the graph holding the given table structure.
   * @return the graph holding the given table structure.
   */
  static Graph2D getGraph2D( final Table table ) {
    return (Graph2D) table.getRealizer().getNode().getGraph();
  }


  /**
   * Stores the background color for each table cell.
   * Provides methods for managing stored colors and choosing new colors.
   * Background colors are mapped to partition cell spans in class
   * {@link CellSpanActionFactory.CellLayoutConfigurator}.
   * <p>
   * Instances of this class are stored as
   * {@link y.view.GenericNodeRealizer#setUserData(Object) user data} in
   * {@link TableGroupNodeRealizer} instances and are copied from one realizer
   * instance to another on undo/redo, clipboard operations, etc. Since
   * <code>TableGroupNodeRealizer</code>'s user data copying is inherited from
   * its super class {@link y.view.GenericNodeRealizer}, user data copying
   * happens before table structure copying. Consequently, storing references
   * to columns and rows of a <code>TableGroupNodeRealizer</code> does not work
   * for copy operations. For this reason, cell to background mappings in this
   * class are based on column and row indices.
   * </p>
   */
  static final class CellColorManager {
    /** All possible background colors. */
    private static final Color[] COLORS = newColors();


    /**
     * Stores the background color for each cell.
     * Cells are stored as pairs of column index and row index. This is
     * necessary, to be able to copy <code>CellColorManager</code> instances
     * from one {@link TableGroupNodeRealizer} instance to another.
     */
    private final Map data;

    /**
     * Initializes a new <code>CellColorManager</code> instance with no
     * background colors.
     */
    CellColorManager() {
      data = new HashMap();
    }

    /**
     * Initializes a new <code>CellColorManager</code> instance as copy of
     * the given prototype instance.
     */
    CellColorManager( final CellColorManager prototype ) {
      data = new HashMap(prototype.data);
    }

    /**
     * Returns a background color that is not yet used in this instance.
     * @return a background color that is not yet used in this instance or
     * <code>null</code> if all background colors are already in use.
     */
    Color nextUnused() {
      final HashSet used = new HashSet(data.values());
      Color color = null;
      for (int i = 0; i < COLORS.length; ++i) {
        if (!used.contains(COLORS[i])) {
          color = COLORS[i];
          break;
        }
      }
      return color;
    }

    /**
     * Returns the background color for the given cell.
     * @param col the horizontal position of the cell.
     * @param row the vertical position of the cell.
     * @return the background color for the given cell or <code>null</code>
     * if the given cell has no background color.
     */
    Color getCellColor( final Column col, final Row row ) {
      if (col != null && row != null) {
        return (Color) data.get(new CellKey(col, row));
      } else {
        return null;
      }
    }

    /**
     * Sets the background color for the given cell.
     * @param col the horizontal position of the cell.
     * @param row the vertical position of the cell.
     * @param color the new background color for the given cell. May be
     * <code>null</code>.
     */
    void setCellColor( final Column col, final Row row, final Color color ) {
      if (col != null && row != null) {
        if (color == null) {
          data.remove(new CellKey(col, row));
        } else {
          data.put(new CellKey(col, row), color);
        }
      }
    }

    /**
     * Sets the background color for the given cell span.
     * @param span the cell span for which to set the background color.
     * @param color the new background color for the given cell. May be
     * <code>null</code>.
     */
    void setCellColor( final Span span, final Color color ) {
      if (color == null) {
        for (int i = span.minCol, n = span.maxCol + 1; i < n; ++i) {
          for (int j = span.minRow, m = span.maxRow + 1; j < m; ++j) {
            data.remove(new CellKey(i, j));
          }
        }
      } else {
        for (int i = span.minCol, n = span.maxCol + 1; i < n; ++i) {
          for (int j = span.minRow, m = span.maxRow + 1; j < m; ++j) {
            data.put(new CellKey(i, j), color);
          }
        }
      }
    }

    /**
     * Adjusts the column indices in this manager's cell to color mappings
     * for added/removed columns.
     * @param column the column that was added or will be removed.
     * @param up if <code>true</code>, indices will be adjusted for added
     * columns otherwise for removed columns.
     */
    void shift( final Column column, final boolean up ) {
      shiftImpl(column.getIndex(), false, up);
    }

    /**
     * Adjusts the row indices in this manager's cell to color mappings
     * for added/removed rows.
     * @param row the row that was added or will be removed.
     * @param up if <code>true</code>, indices will be adjusted for added
     * rows otherwise for removed rows.
     */
    void shift( final Row row, final boolean up ) {
      shiftImpl(row.getIndex(), true, up);
    }

    /**
     * Adjusts the indices in this manager's cell to color mappings
     * for added/removed columns or rows.
     * @param idx the index of the column or row that was added or will be
     * removed.
     * @param row if <code>true</code>, indices will be adjusted for rows;
     * otherwise for columns.
     * @param up if <code>true</code> indices will be adjusted for added
     * columns/rows; otherwise for removed columns/rows.
     */
    private void shiftImpl( final int idx, final boolean row, final boolean up ) {
      final ArrayList keys = new ArrayList(data.keySet());
      Collections.sort(keys, new CellComparator(row, up));
      final int offset = up ? 1 : -1;
      for (Iterator it = keys.iterator(); it.hasNext();) {
        final CellKey key = (CellKey) it.next();
        final int value = row ? key.row : key.column;
        if (value >= idx) {
          final Object color = data.remove(key);
          if (up || value > idx) {
            if (row) {
              data.put(new CellKey(key.column, key.row + offset), color);
            } else {
              data.put(new CellKey(key.column + offset, key.row), color);
            }
          }
        }
      }
    }

    /**
     * Returns the <code>CellColorManager</code> instance that stores the
     * background colors for the cells in the given table. 
     */
    static CellColorManager getInstance( final Table table ) {
      final TableGroupNodeRealizer tgnr = table.getRealizer();
      return (CellColorManager) tgnr.getUserData();
    }

    /**
     * Creates a small set of colors for use as cell background colors.
     */
    private static Color[] newColors() {
      return new Color[] {
        new Color(192,   0,   0),
        new Color(255, 102,   0),
        new Color(251, 176,   7),
        new Color(  0, 153,  57),
        new Color(  0, 137, 205),

        new Color(238,  53,  81),
        new Color(255, 134,  56),
        new Color(242, 228,  21),
        new Color(149, 209,  39),
        new Color( 17, 175, 252),
      };

      // alternative color set
//      return new Color[] {
//        new Color(181, 137,   0),
//        new Color(203,  75,  22),
//        new Color(220,  50,  47),
//        new Color(211,  54, 130),
//        new Color(108, 113, 196),
//        new Color( 38, 139, 210),
//        new Color( 42, 161, 152),
//        new Color(133, 153,   0),
//      };      
    }
  }

  /**
   * Orders {@link CellKey} instances by their column or row index.
   */
  private static final class CellComparator implements Comparator {
    private final boolean row;
    private final int lessThan;
    private int greaterThan;

    CellComparator( final boolean row, final boolean descending ) {
      this.row = row;
      lessThan = descending ? 1 : -1;
      greaterThan = descending ? -1 : 1;
    }

    public int compare( final Object o1, final Object o2 ) {
      final int v1 = getValue((CellKey) o1);
      final int v2 = getValue((CellKey) o2);
      if (v1 < v2) {
        return lessThan;
      } else if (v1 > v2) {
        return greaterThan;
      } else {
        return 0;
      }
    }

    int getValue( final CellKey key ) {
      return row ? key.row : key.column;
    }
  }

  /**
   * Represents a table cell by storing the cell's column and row indices.
   */
  private static final class CellKey {
    // The horizontal position of the cell.
    private final int column;
    // The vertical position of the cell.
    private final int row;

    CellKey( final Column column, final Row row ) {
      this(column.getIndex(), row.getIndex());
    }

    CellKey( final int column, final int row ) {
      this.column = column;
      this.row = row;
    }

    public boolean equals( final Object o ) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final CellKey cellKey = (CellKey) o;

      if (column != cellKey.column) return false;
      if (row != cellKey.row) return false;

      return true;
    }

    public int hashCode() {
      int result = column;
      result = 31 * result + row;
      return result;
    }

    public String toString() {
      return "[c=" + column + ";" + "r=" + row + "]";
    }
  }

  /**
   * Represents a table cell by storing the cell's column and row instances.
   */
  static final class Cell {
    // The horizontal position of the cell.
    private final Column column;
    // The vertical position of the cell.
    private final Row row;

    Cell( final Column column, final Row row ) {
      this.column = column;
      this.row = row;
    }

    public boolean equals( final Object o ) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final Cell cell = (Cell) o;

      if (!column.equals(cell.column)) return false;
      if (!row.equals(cell.row)) return false;

      return true;
    }

    public int hashCode() {
      int result = column.hashCode();
      result = 31 * result + row.hashCode();
      return result;
    }

    public String toString() {
      return "[c=" + column.getIndex() + ";" + "r=" + row.getIndex() + "]";
    }
  }

  /**
   * Represents a "rectangular", two-dimensional cell range.
   */
  static final class Span {
    // The index of the left-most column in this span. 
    final int minCol;
    // The index of the right-most column in this span. 
    final int maxCol;
    // The index of the top-most row in this span. 
    final int minRow;
    // The index of the bottom-most row in this span. 
    final int maxRow;

    private Span(
            final int minCol, final int maxCol,
            final int minRow, final int maxRow
    ) {
      this.minCol = minCol;
      this.maxCol = maxCol;
      this.minRow = minRow;
      this.maxRow = maxRow;
    }

    /**
     * Determines whether or not the given cell is included in this cell span.
     * @param col the column (index) of the cell to check.
     * @param row the row (index) of the cell to check.
     * @return <code>true</code> if the given cell is included in this cell
     * span; <code>false</code> otherwise.
     */
    boolean contains( final Column col, final Row row ) {
      final int cIdx = col.getIndex();
      final int rIdx = row.getIndex();
      return minCol <= cIdx && cIdx <= maxCol &&
             minRow <= rIdx && rIdx <= maxRow;
    }

    /**
     * Determines whether or not this cell span contains all the cells of the
     * given span.
     * @param span the cell span to check.
     * @return <code>true</code> if this cell span contains all the cells of the
     * given span; <code>false</code> otherwise.
     */
    boolean contains( final Span span ) {
      return minCol <= span.minCol && span.maxCol <= maxCol &&
             minRow <= span.minRow && span.maxRow <= maxRow;
    }

    /**
     * Creates a new cell span instance by finding the greatest cell span that
     * includes the given cell and has only cells of the given color.
     * Note, the color of the given cell is ignored and may differ from the
     * given color.
     * @param table the table structure to check.
     * @param col the column (index) of the starting cell.
     * @param row the row (index) of the starting cell.
     * @param color the color of the cells to be included in the span.
     * @return a new cell span instance.
     */
    static Span find(
            final Table table, final Column col, final Row row,
            final Color color
    ) {
      final CellColorManager manager = CellColorManager.getInstance(table);

      int minCIdx = col.getIndex();
      int maxCIdx = minCIdx;
      int minRIdx = row.getIndex();
      int maxRIdx = minRIdx;

      // view the table as graph structure with each cell representing a node
      // and consider cell A to be connected to cell B if 
      //  B is directly above/below/to the left/to the right of A and
      //  B has the given color
      // with this assumption the below code is essentially the well-known
      // graph algorithm depth first search 
      final HashSet seen = new HashSet();
      final ArrayList stack = new ArrayList();
      stack.add(new Cell(col, row));
      while (!stack.isEmpty()) {
        final Cell cell = (Cell) stack.remove(stack.size() - 1);
        if (seen.add(cell)) {
          final Column c = cell.column;
          final int cIdx = c.getIndex();
          final Row r = cell.row;
          final int rIdx = r.getIndex();

          if (rIdx > 0) {
            final Row north = table.getRow(rIdx - 1);
            if (color.equals(manager.getCellColor(c, north))) {
              stack.add(new Cell(c, north));

              if (minRIdx > rIdx - 1) {
                minRIdx = rIdx - 1;
              }
            }
          }
          if (cIdx > 0) {
            final Column west = table.getColumn(cIdx - 1);
            if (color.equals(manager.getCellColor(west, r))) {
              stack.add(new Cell(west, r));

              if (minCIdx > cIdx - 1) {
                minCIdx = cIdx - 1;
              }
            }
          }
          if (rIdx + 1 < table.rowCount()) {
            final Row south = table.getRow(rIdx + 1);
            if (color.equals(manager.getCellColor(c, south))) {
              stack.add(new Cell(c, south));

              if (maxRIdx < rIdx + 1) {
                maxRIdx = rIdx + 1;
              }
            }
          }
          if (cIdx + 1 < table.columnCount()) {
            final Column east = table.getColumn(cIdx + 1);
            if (color.equals(manager.getCellColor(east, r))) {
              stack.add(new Cell(east, r));

              if (maxCIdx < cIdx + 1) {
                maxCIdx = cIdx + 1;
              }
            }
          }
        }
      }

      return new Span(minCIdx, maxCIdx, minRIdx, maxRIdx);
    }

    /**
     * Creates a new cell span instance that includes the given cells.
     * @param cells a collection of {@link Cell} instances.
     * @return a new cell span instance.
     */
    static Span span( final Collection cells ) {
      final Iterator it = cells.iterator();
      final Cell first = (Cell) it.next();

      int minCIdx = first.column.getIndex();
      int maxCIdx = minCIdx;
      int minRIdx = first.row.getIndex();
      int maxRIdx = minRIdx;

      while (it.hasNext()) {
        final Cell cell = (Cell) it.next();

        final int cIdx = cell.column.getIndex();
        final int rIdx = cell.row.getIndex();
        if (minCIdx > cIdx) {
          minCIdx = cIdx;
        }
        if (maxCIdx < cIdx) {
          maxCIdx = cIdx;
        }
        if (minRIdx > rIdx) {
          minRIdx = rIdx;
        }
        if (maxRIdx < rIdx) {
          maxRIdx = rIdx;
        }
      }

      return new Span(minCIdx, maxCIdx, minRIdx, maxRIdx);
    }

    /**
     * Creates a new cell span instance.
     * @param minCol the index of the left-most column in this span. 
     * @param maxCol the index of the right-most column in this span.
     * @param minRow the index of the top-most row in this span.
     * @param maxRow the index of the bottom-most row in this span.
     * @return a new cell span instance.
     */
    static Span manual(
            final int minCol, final int maxCol,
            final int minRow, final int maxRow
    ) {
      return new Span(minCol, maxCol, minRow, maxRow);
    }
  }
}
