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
import demo.layout.hierarchic.CellSpanLayoutDemo.Span;

import demo.view.DemoBase;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.util.GraphCopier;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import y.view.hierarchy.AutoBoundsFeature;
import y.view.hierarchy.HierarchyManager;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Row;
import y.view.tabular.TableGroupNodeRealizer.Table;
import y.view.tabular.TableLayoutConfigurator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToggleButton;

/**
 * Creates all user actions for the {@link CellSpanLayoutDemo}.
 * Provides actions for adding/removing columns and rows, switching
 * from design view to diagram view and vice versa, showing sample diagrams,
 * and deleting selected elements.
 *
 */
class CellSpanActionFactory {
  /**
   * Client property key to store the design view graph while in diagram mode
   * @see #switchViewState(y.view.Graph2DView)
   */
  private static final String KEY_DESIGN_VIEW = "CellSpanLayoutDemo.designView";


  /**
   * Prevents instantiation of factory class.
   */
  private CellSpanActionFactory() {
  }


  /**
   * Creates an action for inserting new columns right before the given
   * reference column.
   */
  static Action newAddBefore(
          final Graph2DView view, final Table table, final Column col
  ) {
    return new AddColumn(view, table, col, true);
  }

  /**
   * Creates an action for inserting new columns right after the given
   * reference column.
   */
  static Action newAddAfter(
          final Graph2DView view, final Table table, final Column col
  ) {
    return new AddColumn(view, table, col, false);
  }

  /**
   * Creates an action for removing the given column.
   */
  static Action newRemoveColumn(
          final Graph2D graph, final Table table, final Column col
  ) {
    return new RemoveColumn(graph, table, col);
  }

  /**
   * Creates an action for inserting new rows right before the given
   * reference row.
   */
  static Action newAddBefore(
          final Graph2DView view, final Table table, final Row row
  ) {
    return new AddRow(view, table, row, true);
  }

  /**
   * Creates an action for inserting new rows right after the given
   * reference row.
   */
  static Action newAddAfter(
          final Graph2DView view, final Table table, final Row row
  ) {
    return new AddRow(view, table, row, false);
  }

  /**
   * Creates an action for removing the given row.
   */
  static Action newRemoveRow(
          final Graph2D graph, final Table table, final Row row
  ) {
    return new RemoveRow(graph, table, row);
  }

  /**
   * Creates an action for deleting selected elements in the graph
   * associated to the given view.
   * This action prevents top-level table nodes from being deleted.
   * Additionally, this action ensures that top-level table nodes
   * are not shrunk due to removing elements.
   */
  static Action newDeleteSelection( final Graph2DView view ) {
    return new CellDeleteSelection(view);
  }

  /**
   * Creates an action that replaces the currently displayed diagram with a
   * sample diagram.
   * @param name the display name of the action in the application menu bar.
   * @param parent the <code>CellSpanLayoutDemo</code> instance to display the
   * sample diagram.
   * @param resource the path name referencing the GraphML document with
   * the sample diagram to display.
   */
  static Action newSampleAction(
          final String name, final CellSpanLayoutDemo parent, final String resource
  ) {
    return new SampleAction(name, parent, resource);
  }

  /**
   * Creates an action for switching between design view and laid out diagram
   * view.
   * @param name the display name of the action in the application tool bar.
   * @param view the view that displays either the cell span designer or the
   * laid out diagram.
   * @param other the toggle button to enable for the switching back to the
   * previous view state.
   */
  static Action newSwitchViewStateAction(
          final String name, final Graph2DView view, final JToggleButton other
  ) {
    return new SwitchViewStateAction(name, view, other);
  }


  /**
   * Backs up the visual state of the specified node.
   */
  static void backupRealizer( final Graph2D graph, final Node node ) {
    graph.backupRealizers((new NodeList(node)).nodes());
  }


  /**
   * Switches the view state from cell span design mode to diagram mode and
   * vice versa.
   * @param view the view that displays either the cell span designer or the
   * laid out diagram.
   */
  static void switchViewState( final Graph2DView view ) {
    final Object value = view.getClientProperty(KEY_DESIGN_VIEW);
    if (value instanceof Graph2D) {
      // view is in diagram mode
      // switch to design mode

      view.putClientProperty(KEY_DESIGN_VIEW, null);
      view.setGraph2D((Graph2D) value);
    } else {
      // view is in design mode
      // switch to laid out diagram

      // the graph holding the table node with cell spans
      final Graph2D graph = view.getGraph2D();

      // create a copy of the designer graph for the laid out diagram
      // this is done because the single top-level cell span designer table node
      // is replaced with several top-level group nodes representing each cell
      // span
      final Graph2D target = new Graph2D();
      target.setDefaultNodeRealizer(graph.getDefaultNodeRealizer());
      target.setDefaultEdgeRealizer(graph.getDefaultEdgeRealizer());
      final GraphCopier copier = new GraphCopier(graph.getGraphCopyFactory());
      copier.copy(graph, target);

      // set up the layout algorithm used for arranging the diagram
      final IncrementalHierarchicLayouter algorithm = new IncrementalHierarchicLayouter();
      algorithm.setConsiderNodeLabelsEnabled(true);
      final EdgeLayoutDescriptor eld = algorithm.getEdgeLayoutDescriptor();
      eld.setMinimumFirstSegmentLength(25);
      eld.setMinimumLastSegmentLength(25);

      // run the layout algorithm
      final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
      // class CellLayoutConfigurator is TableLayoutConfigurator subclass
      // that models the cell spans in the top-level table node by
      // introducing additional group nodes
      executor.setTableLayoutConfigurator(new CellLayoutConfigurator());
      executor.setConfiguringTableNodeRealizers(true);
      executor.setPortIntersectionCalculatorEnabled(true);
      // class GroupNodeTransformerStage re-configures partition grid data
      // by interpreting second-level group nodes as cell spans
      executor.doLayout(target, new GroupNodeTransformerStage(algorithm));

      // store the original cell span designer to be able to switch back to
      // design mode later on
      view.putClientProperty(KEY_DESIGN_VIEW, graph);
      view.setGraph2D(target);
    }

    view.fitContent();
    view.updateView();
  }


  /**
   * Action that displays a sample diagram.
   */
  private static final class SampleAction extends AbstractAction {
    private final CellSpanLayoutDemo parent;
    private final String resource;

    /**
     * Initializes a new <code>SampleAction</code> instance.
     * @param name the display name of the action in the application menu bar.
     * @param parent the <code>CellSpanLayoutDemo</code> instance to display the
     * sample diagram.
     * @param resource the path name referencing the GraphML document with
     * the sample diagram to display.
     */
    SampleAction(
            final String name, final CellSpanLayoutDemo parent, final String resource
    ) {
      super(name);
      this.parent = parent;
      this.resource = resource;
    }

    /**
     * Displays the sampe diagram referenced by {@link #resource}.
     */
    public void actionPerformed( final ActionEvent e ) {
      final Graph2DView view = parent.getView();

      // all sample diagrams are stored in design mode
      // if the view is currently in diagram mode, temporarily switch to
      // design mode for loading the diagram and switch back to diagram mode
      // later
      final Object value = view.getClientProperty(KEY_DESIGN_VIEW);
      final boolean inDiagramMode = value instanceof Graph2D;
      if (inDiagramMode) {
        view.putClientProperty(KEY_DESIGN_VIEW, null);
        view.setGraph2D((Graph2D) value);
      }

      final Graph2D graph = view.getGraph2D();
      graph.clear();

      // actually load the sample diagram
      parent.loadGraph(resource);

      // switch back to the correct display mode if necessary
      if (inDiagramMode) {
        switchViewState(view);
      }
    }
  }

  /**
   * Action that switches the view state from design mode to laid out diagram
   * mode or vice versa.
   */
  private static final class SwitchViewStateAction extends AbstractAction {
    private final JToggleButton other;
    private final Graph2DView view;

    /**
     * Initializes a new <code>SwitchViewStateAction</code> instance.
     * @param name the display name of the action in the application tool bar.
     * @param view the view that displays either the cell span designer or the
     * laid out diagram.
     * @param other the toggle button to enable for the switching back to the
     * previous view state.
     */
    SwitchViewStateAction(
            final String name, final Graph2DView view, final JToggleButton other
    ) {
      super(name);
      this.other = other;
      this.view = view;
    }

    /**
     * Switches the view state from design mode to laid out diagram mode or
     * vice versa.
     */
    public void actionPerformed( final ActionEvent e ) {
      final JToggleButton src = (JToggleButton) e.getSource();
      if (src.isSelected()) {
        src.setEnabled(false);
        other.setEnabled(true);
        other.setSelected(false);
        CellSpanActionFactory.switchViewState(view);
      }
    }
  }

  /**
   * Action that deletes selected graph elements but prevents top-level table
   * nodes from being deleted. Additionally, this action ensures that top-level
   * table nodes are not shrunken due to removing elements.
   */
  private static final class CellDeleteSelection extends DemoBase.DeleteSelection {
    CellDeleteSelection( final Graph2DView view ) {
      super(view);

      // prevents all table node from being deleted
      // there should be only one table node for CellSpanLayoutDemo (i.e. the
      // cell span designer), preventing the deletion of all table nodes should
      // be fine, too
      setDeletionMask(getDeletionMask() & ~TYPE_TABLE_NODE);
    }

    /**
     * Deletes the selected elements in graph associated to the given view.
     * Ensures that top-level table nodes are not shrunken due to removing
     * elements.
     * @param view the view in which to delete graph elements.
     */
    public void delete( final Graph2DView view ) {
      final Graph2D graph = view.getGraph2D();
      final HierarchyManager hm = graph.getHierarchyManager();
      if (hm == null) {
        super.delete(view);
      } else {
        // find all top-level table nodes
        final NodeList tables = new NodeList();
        for (NodeCursor nc = hm.getChildren(null); nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (hm.isGroupNode(node) &&
              graph.getRealizer(node) instanceof TableGroupNodeRealizer) {
            tables.add(node);
          }
        }
        if (tables.isEmpty()) {
          super.delete(view);
        } else {
          graph.firePreEvent();
          // notify the undo manager of the table nodes' current sizes
          // (actually, this step copies the complete visual states of the
          // table nodes, even though only their sizes matter here) 
          graph.backupRealizers(tables.nodes());

          // now, turn off the table nodes' AutoBoundsFeatures
          // (AutoBoundsFeature is responsible for automatic size changes)
          final NodeList disabled = new NodeList();
          for (NodeCursor nc = tables.nodes(); nc.ok(); nc.next()) {
            final Node node = nc.node();
            final AutoBoundsFeature abf =
                    graph.getRealizer(node).getAutoBoundsFeature();
            if (abf != null && abf.isAutoBoundsEnabled()) {
              abf.setAutoBoundsEnabled(false);
              disabled.add(node);
            }
          }

          try {
            super.delete(view);
          } finally {

            // finally, turn on the previously disabled AutoBoundsFeatures
            // otherwise the table nodes are no longer automatically enlarged
            // when child nodes are moved or added
            if (!disabled.isEmpty()) {
              for (NodeCursor nc = disabled.nodes(); nc.ok(); nc.next()) {
                graph.getRealizer(nc.node())
                        .getAutoBoundsFeature()
                        .setAutoBoundsEnabled(true);
              }
            }
          }

          graph.firePostEvent();
        }
      }
    }
  }

  /**
   * Action that adds a new column before or after a reference column.
   */
  private static final class AddColumn extends AbstractAction {
    private final Graph2DView view;
    private final Table table;
    private final Column column;
    private final boolean before;

    /**
     * Initializes a new <code>AddColumn</code> instance.
     */
    AddColumn(
            final Graph2DView view,
            final Table table, final Column column,
            final boolean before
    ) {
      super("Add Column " + (before ? "Before" : "After"));
      this.view = view;
      this.table = table;
      this.column = column;
      this.before = before;
    }

    /**
     * Adds a new column to the referenced <code>table</code>.
     */
    public void actionPerformed( final ActionEvent e ) {
      final TableGroupNodeRealizer tgnr = table.getRealizer();

      // notify the undo manager of the current table structure
      backupRealizer(view.getGraph2D(), tgnr.getNode());


      final double oldWidth = tgnr.getWidth();

      final int newIdx = column.getIndex() + (before ? 0 : 1);
      final Column newCol = table.addColumn(newIdx);

      final double dx = tgnr.getWidth() - oldWidth;
      // adjust table position when inserting a column before the reference
      // column to achieve a "prepend" effect rather than an "append" effect
      if (before && dx > 0) {
        // AutoBoundsFeature needs to be disabled when changing the table node
        // position, otherwise the feature's internal logic will prevent
        // the position change
        final AutoBoundsFeature abf = tgnr.getAutoBoundsFeature();
        final boolean oldEnabled = abf != null && abf.isAutoBoundsEnabled();
        if (oldEnabled) {
          abf.setAutoBoundsEnabled(false);
        }
        try {
          tgnr.setLocation(tgnr.getX() - dx, tgnr.getY());
        } finally {
          if (oldEnabled) {
            abf.setAutoBoundsEnabled(true);
          }
        }
      }


      // update the cell to color mappings accordingly
      // adding the new column above has increased the indices of all
      // subsequent columns and consequently the index-based cell to color
      // mappings have to be adjusted accordingly
      final CellColorManager manager = CellColorManager.getInstance(table);
      manager.shift(newCol, true);

      // color the appropriate cells in the new column such that no cell spans
      // are split in two
      final int nextIdx = before ? newIdx - 1 : newIdx + 1;
      if (-1 < nextIdx && nextIdx < table.columnCount()) {
        final Column next = table.getColumn(nextIdx);
        for (int i = 0, n = table.rowCount(); i < n; ++i) {
          final Row row = table.getRow(i);
          final Color color = manager.getCellColor(column, row);
          if (color != null && color.equals(manager.getCellColor(next, row))) {
            manager.setCellColor(newCol, row, color);
          }
        }
      }

      // ensures that the new column is completely reachable by scroll bar
      view.updateWorldRect();

      // trigger a repaint to visualize the change
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Action that removes an existing column.
   */
  private static final class RemoveColumn extends AbstractAction {
    private final Graph2D graph;
    private final Table table;
    private final Column column;

    /**
     * Initializes a new <code>RemoveColumn</code> instance.
     */
    RemoveColumn( final Graph2D graph, final Table table, final Column column ) {
      super("Remove Column");
      this.graph = graph;
      this.table = table;
      this.column = column;
    }

    /**
     * Removes the referenced <code>column</code>.
     */
    public void actionPerformed( final ActionEvent e ) {
      // notify the undo manager of the current table structure
      backupRealizer(graph, table.getRealizer().getNode());

      // update the cell to color mappings accordingly
      // removing a column will decrease the indices of all subsequent columns
      // and consequently the index-based cell to color mappings have to be
      // adjusted accordingly
      CellColorManager.getInstance(table).shift(column, false);

      column.remove();

      // trigger a repaint to visualize the change
      graph.updateViews();
    }
  }

  /**
   * Action that adds a new row before or after a reference row.
   */
  private static final class AddRow extends AbstractAction {
    private final Graph2DView view;
    private final Table table;
    private final Row row;
    private final boolean before;

    /**
     * Initializes a new <code>AddRow</code> instance.
     */
    AddRow(
            final Graph2DView view,
            final Table table, final Row row,
            final boolean before
    ) {
      super("Add Row " + (before ? "Before" : "After"));
      this.view = view;
      this.table = table;
      this.row = row;
      this.before = before;
    }

    /**
     * Adds a new row to the referenced <code>table</code>.
     */
    public void actionPerformed( final ActionEvent e ) {
      final TableGroupNodeRealizer tgnr = table.getRealizer();

      // notify the undo manager of the current table structure
      backupRealizer(view.getGraph2D(), tgnr.getNode());


      final double oldHeight = tgnr.getHeight();

      final int newIdx = row.getIndex() + (before ? 0 : 1);
      final Row newRow = table.addRow(newIdx);

      final double dy = tgnr.getHeight() - oldHeight;
      // adjust table position when inserting a row before the reference row
      // to achieve a "prepend" effect rather than an "append" effect
      if (before && dy > 0) {
        // AutoBoundsFeature needs to be disabled when changing the table node
        // position, otherwise the feature's internal logic will prevent
        // the position change
        final AutoBoundsFeature abf = tgnr.getAutoBoundsFeature();
        final boolean oldEnabled = abf != null && abf.isAutoBoundsEnabled();
        if (oldEnabled) {
          abf.setAutoBoundsEnabled(false);
        }
        try {
          tgnr.setLocation(tgnr.getX(), tgnr.getY() - dy);
        } finally {
          if (oldEnabled) {
            abf.setAutoBoundsEnabled(true);
          }
        }
      }

      // update the cell to color mappings accordingly
      // adding the new row above has increased the indices of all
      // subsequent row and consequently the index-based cell to color
      // mappings have to be adjusted accordingly
      final CellColorManager manager = CellColorManager.getInstance(table);
      manager.shift(newRow, true);

      // color the appropriate cells in the new row such that no cell spans
      // are split in two
      final int nextIdx = before ? newIdx - 1 : newIdx + 1;
      if (-1 < nextIdx && nextIdx < table.rowCount()) {
        final Row next = table.getRow(nextIdx);
        for (int i = 0, n = table.columnCount(); i < n; ++i) {
          final Column col = table.getColumn(i);
          final Color color = manager.getCellColor(col, row);
          if (color != null && color.equals(manager.getCellColor(col, next))) {
            manager.setCellColor(col, newRow, color);
          }
        }
      }

      // ensures that the new row is completely reachable by scroll bar
      view.updateWorldRect();

      // trigger a repaint to visualize the change
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Action that removes an existing row.
   */
  private static final class RemoveRow extends AbstractAction {
    private final Graph2D graph;
    private final Table table;
    private final Row row;

    /**
     * Initializes a new <code>RemoveRow</code> instance.
     */
    RemoveRow( final Graph2D graph, final Table table, final Row row ) {
      super("Remove Row");
      this.graph = graph;
      this.table = table;
      this.row = row;
    }

    /**
     * Removes the referenced <code>row</code>.
     */
    public void actionPerformed( final ActionEvent e ) {
      // notify the undo manager of the current table structure
      backupRealizer(graph, table.getRealizer().getNode());

      // update the cell to color mappings accordingly
      // removing a row will decrease the indices of all subsequent rows
      // and consequently the index-based cell to color mappings have to be
      // adjusted accordingly
      CellColorManager.getInstance(table).shift(row, false);

      row.remove();

      // trigger a repaint to visualize the change
      graph.updateViews();
    }
  }


  /**
   * Converts the cell spans defined by background colors into group nodes. 
   */
  private static final class CellLayoutConfigurator extends TableLayoutConfigurator {
    /**
     * Performs all necessary layout preparations for the specified graph.
     * Invokes
     * <blockquote>
     * <code>prepareCellSpans(graph);<code>
     * <code>super.prepareAll(graph);<code>
     * </blockquote>
     * @param graph the <code>Graph2D</code> instance that is prepared for
     * automated layout calculation.
     */
    public void prepareAll( final Graph2D graph ) {
      prepareCellSpans(graph);
      super.prepareAll(graph);
    }

    /**
     * Adds groups nodes to all top-level table nodes representing the cell
     * spans in the table structures.
     * @param graph the <code>Graph2D</code> instance that is prepared for
     * automated layout calculation.
     */
    void prepareCellSpans( final Graph2D graph ) {
      final HierarchyManager hm = graph.getHierarchyManager();
      if (hm != null) {
        for (NodeCursor nc = hm.getChildren(null); nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (hm.isGroupNode(node)) {
            final NodeRealizer nr = graph.getRealizer(node);
            if (nr instanceof TableGroupNodeRealizer) {
              prepareTable((TableGroupNodeRealizer) nr);
            }
          }
        }
      }
    }

    /**
     * Adds a group node for each cell span in the given realizer's associated
     * table structure. Cells spans are defined by cell background color.
     * @param tgnr the table realizer to process.
     */
    void prepareTable( final TableGroupNodeRealizer tgnr ) {
      // disable AutoBoundsFeature to prevent the addition of group nodes
      // from changing the table node size thereby destroying the
      // coordinates-based child node to table cell associations
      final AutoBoundsFeature abf = tgnr.getAutoBoundsFeature();
      final boolean oldEnabled = abf != null && abf.isAutoBoundsEnabled();
      if (oldEnabled) {
        abf.setAutoBoundsEnabled(false);
      }
      try {
        prepareTableImpl(tgnr);
      } finally {
        if (oldEnabled) {
          abf.setAutoBoundsEnabled(true);
        }
      }
    }

    /**
     * Adds a group node for each cell span in the given realizer's associated
     * table structure. Cells spans are defined by cell background color.
     * This method assumes that each background color is used only for one cell
     * span. Moreover, this method assumes that there are no nested columns or
     * rows in the realizer's table structure.
     * @param tgnr the table realizer to process.
     */
    private void prepareTableImpl( final TableGroupNodeRealizer tgnr ) {
      final Node node = tgnr.getNode();
      final Graph2D graph = (Graph2D) node.getGraph();
      final HierarchyManager hm = graph.getHierarchyManager();

      final Table table = tgnr.getTable();

      final CellColorManager manager = CellColorManager.getInstance(table);
      final HashSet used = new HashSet();
      used.add(null);
      for (int i = 0, n = table.columnCount(); i < n; ++i) {
        final Column column = table.getColumn(i);
        for (int j = 0, m = table.rowCount(); j < m; ++j) {
          final Row row = table.getRow(j);
          final Color color = manager.getCellColor(column, row);
          if (used.add(color)) {
            final Span span = Span.find(table, column, row, color);

            // determine the size of the group node representing the cell span
            // defined by the current color
            // the group is actually slightly smaller the union of the cells
            // to prevent ambiguities in GroupNodeTransformerStage when
            // translating the group nodes to partition grid cell spans
            final double eps = 2;
            final double minX = table.getColumn(span.minCol).calculateBounds().getX() + eps;
            final double maxX = table.getColumn(span.maxCol).calculateBounds().getMaxX() - eps;
            final double minY = table.getRow(span.minRow).calculateBounds().getY() + eps;
            final double maxY = table.getRow(span.maxRow).calculateBounds().getMaxY() - eps;

            final Node group = hm.createGroupNode(node);
      
            final NodeRealizer nr = graph.getRealizer(group);
            nr.setFrame(minX, minY, maxX - minX, maxY - minY);
            nr.setFillColor(color);
            for (int l = nr.labelCount() - 1; l > -1; --l) {
              nr.removeLabel(l);
            }
          }
        }
      }
    }


    /**
     * Performs all necessary resource cleanup and data translation after
     * a layout calculation. Invokes
     * <blockquote>
     * <code>super.restoreAll(graph);<code>
     * <code>restoreCellSpans(graph);<code>
     * </blockquote>
     * @param graph the <code>Graph2D</code> instance that was previously
     * prepared for automated layout calculation.
     */
    public void restoreAll( final Graph2D graph ) {
      super.restoreAll(graph);
      restoreCellSpans(graph);
    }

    /**
     * Assigns the non-group node children of top-level table nodes to the
     * appropriate cell span representing group nodes and removes said table
     * nodes.
     * @param graph the <code>Graph2D</code> instance that was previously
     * prepared for automated layout calculation.
     */
    void restoreCellSpans( final Graph2D graph ) {
      final HierarchyManager hm = graph.getHierarchyManager();
      if (hm != null) {
        final NodeList tables = new NodeList();

        // find all top-level table nodes (should be exactly one)
        // and assign the non-group node children to the appropriate
        // cell span representing group node
        for (NodeCursor nc = hm.getChildren(null); nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (hm.isGroupNode(node) &&
              graph.getRealizer(node) instanceof TableGroupNodeRealizer) {
            restoreCellSpanGroups(graph, hm, node);
            tables.add(node);
          }
        }

        // remove the now unnecessary table nodes
        // table nodes are meant only as means for defining cell spans
        // now that the all cell spans have been translated to group nodes
        // the tables are superfluous
        for (NodeCursor nc = tables.nodes(); nc.ok(); nc.next()) {
          graph.removeNode(nc.node());
        }
      }
    }

    /**
     * Assigns the non-group node children of a table node to the appropriate
     * cell span representing group node.
     * @param graph the <code>Graph2D</code> instance that was previously
     * prepared for automated layout calculation.
     * @param hm the graph's associated hierarchy information.
     * @param node the table node.
     */
    void restoreCellSpanGroups(
            final Graph2D graph, final HierarchyManager hm, final Node node
    ) {
      // find and separate group nodes from other (normal) nodes
      // since there is no way to interactively create group nodes in
      // CellSpanLayoutDemo, these groups are assumed to correspond to
      // the previously defined cell spans
      final NodeList groups = new NodeList();
      final ArrayList others = new ArrayList();
      for (NodeCursor nc = hm.getChildren(node); nc.ok(); nc.next()) {
        final Node child = nc.node();
        if (hm.isGroupNode(child)) {
          groups.add(child);
        } else {
          others.add(child);
        }
      }

      // order nodes by x- and y-coordinates of their center points
      Collections.sort(others, new NodeCenterOrder(graph));

      // for each group node determine all other (normal) nodes that lie inside
      // the group's bounds and change those nodes to child nodes of the group
      //
      // assigning the other (normal) nodes to the cell span representing group
      // nodes created in prepareTableImpl is done here, because at this point
      // (i.e. after the layout calculation) the groups are already sized to
      // encompass the bounds of all other (normal) nodes belonging to the
      // cells of the groups' spans
      //
      // this approach relies again on the fact that it is not possible
      // to create nested structures in the CellSpanLayoutDemo
      for (NodeCursor nc = groups.nodes(); nc.ok(); nc.next()) {
        final Node group = nc.node();
        final NodeRealizer nr = graph.getRealizer(group);
        final double minX = nr.getX();
        final double maxX = minX + nr.getWidth();
        final double minY = nr.getY();
        final double maxY = minY + nr.getHeight();

        // disable AutoBoundsFeature to prevent the addition of nodes
        // from changing the group node size thereby destroying the
        // calculated layout of the group nodes
        final AutoBoundsFeature abf = nr.getAutoBoundsFeature();
        final boolean oldEnabled = abf != null && abf.isAutoBoundsEnabled();
        if (oldEnabled) {
          abf.setAutoBoundsEnabled(false);
        }

        // determine which other (normal) nodes lie inside the current group
        // node bounds
        // since the diagram is already arranged, partition grid cells
        // will encompass the bounds of their associated nodes completely
        // moreover, the cell span representing groups will be sized to
        // match the bounds of the corresponding cell spans
        // consequently, nodes lie either completely inside or completely
        // outside the group bounds
        // therefore it suffices to check the center coordinates of a node
        // to decide whether or not it should be assigned to the current group
        boolean active = false;
        for (Iterator it = others.iterator(); it.hasNext(); ) {
          final Node child = (Node) it.next();
          final double cx = graph.getCenterX(child);
          if (minX < cx && cx < maxX) {
            active = true;
            final double cy = graph.getCenterY(child);
            if (minY < cy && cy < maxY) {
              hm.setParentNode(child, group);
            }
          } else if (active) {
            // because the other (normal) nodes are ordered by x-coordinate
            // there are no more nodes that are inside the group bounds
            break;
          }
        }

        if (oldEnabled) {
          abf.setAutoBoundsEnabled(true);
        }
      }
    }

    /**
     * Orders nodes according to their center coordinates.
     */
    private static class NodeCenterOrder implements Comparator {
      private final Graph2D graph;

      /**
       * Initializes a new <code>NodeCenterOrder</code> instance.
       */
      NodeCenterOrder( final Graph2D graph ) {
        this.graph = graph;
      }

      /**
       * Compares the given nodes according to their center coordinates.
       */
      public int compare( final Object o1, final Object o2 ) {
        final double cx1 = graph.getCenterX((Node) o1);
        final double cx2 = graph.getCenterX((Node) o2);
        final int order = Double.compare(cx1, cx2);
        if (order == 0) {
          final double cy1 = graph.getCenterY((Node) o1);
          final double cy2 = graph.getCenterY((Node) o2);
          return Double.compare(cy1, cy2);
        } else {
          return order;
        }
      }
    }
  }
}
