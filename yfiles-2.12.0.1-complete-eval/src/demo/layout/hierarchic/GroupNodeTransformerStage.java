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

import y.base.DataProvider;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YInsets;
import y.geom.YRectangle;
import y.layout.AbstractLayoutStage;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.grid.ColumnDescriptor;
import y.layout.grid.PartitionCellId;
import y.layout.grid.PartitionGrid;
import y.layout.grid.RowDescriptor;
import y.layout.grouping.Grouping;
import y.layout.grouping.GroupingKeys;
import y.util.Maps;
import y.util.WrappedObjectDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * LayoutStage for class {@link y.layout.hierarchic.IncrementalHierarchicLayouter}
 * that transforms the group nodes directly contained in a table group node
 * (in the remainder we call such nodes <code>direct group nodes</code>) into a multi-cell
 * (see api-doc of {@link y.layout.grid.PartitionGrid}).
 * Therefore, for each direct group node, this stage creates a multi-cell that covers all partition cells
 * that overlap with the group's bounding box.
 * The corresponding multi-cell identifier is assigned to the direct group node as well as
 * to all non-group nodes assigned to a covered partition cell.
 * The inner group nodes are handled like common group nodes.
 * <br>
 * Note that this layout stage throws an <code>IllegalArgumentException</code>
 * if multiple direct group nodes overlap with the same partition cell or
 * if a direct group node doesn't overlap with any partition cell
 * (i.e., if it is placed outside the partition grid structure).
 * Furthermore, this stage does not restore the original grid structure, i.e., after applying this stage, the
 * nodes are still mapped to the created multi-cell identifiers.
 *
 */
public class GroupNodeTransformerStage extends AbstractLayoutStage {
  /**
   * Initializes a new <code>GroupNodeTransformerStage</code>.
   * @param core the layout algorithm that is decorated by this stage.
   */
  public GroupNodeTransformerStage(Layouter core) {
    super(core);
  }

  /**
   * Determines whether or not this stage can arrange the given graph.
   * @param graph the graph to check.
   * @return <code>true</code> if this stage can arrange the given graph;
   * <code>false</code> otherwise.
   */
  public boolean canLayout(LayoutGraph graph) {
    return canLayoutCore(graph);
  }

  /**
   * Prepares the given graph for partition grid layout with multi-cells.
   * This method transforms group nodes directly contained in a table group
   * node into multi-cells.   
   * @param graph the graph to be arranged.
   * @throws IllegalArgumentException if multiple direct group nodes overlap
   * with the same partition cell or if a direct group node does not overlap
   * with any partition cell (i.e., if it is placed outside the partition grid
   * structure).
   */
  public void doLayout(LayoutGraph graph) {
    final PartitionGrid grid = PartitionGrid.getPartitionGrid(graph);
    if (grid == null || !Grouping.isGrouped(graph)) {
      //nothing special to do
      doLayoutCore(graph);
      return;
    }

    //determine the direct group nodes and the associated cell span information
    final Grouping grouping = new Grouping(graph);
    //the top level group node represents the table
    final Node tableGroupNode = grouping.getChildren(grouping.getRoot()).firstNode();
    final ArrayList directGroupCellSpans = new ArrayList();
    for (NodeCursor nc = grouping.getChildren(tableGroupNode).nodes(); nc.ok(); nc.next()) {
      final Node n = nc.node();
      if (grouping.isGroupNode(n)) {
        directGroupCellSpans.add(new GroupCellSpan(n, graph));
      }
    }

    //check if each partition cell is covered by at most one direct group node
    if (!isConsistent(directGroupCellSpans)) {
      throw new IllegalArgumentException("Found partition cell that is covered by multiple direct group nodes!");
    }

    //remap partition cell identifier, i.e., map simple cells (column/row-pairs) to the new multi-cells
    final DataProvider origNode2CellIdDP = graph.getDataProvider(PartitionGrid.PARTITION_CELL_DPKEY);
    final NodeMap newNode2CellId = Maps.createHashedNodeMap();
    graph.addDataProvider(PartitionGrid.PARTITION_CELL_DPKEY,
        new WrappedObjectDataProvider(newNode2CellId, origNode2CellIdDP));
    final HashMap pair2GroupCellSpan = new HashMap();
    for (int i = 0; i < directGroupCellSpans.size(); i++) {
      //map direct groups to the corresponding multi-cells
      final GroupCellSpan groupCellSpan = (GroupCellSpan) directGroupCellSpans.get(i);
      final PartitionCellId cellId = groupCellSpan.getCellId();
      newNode2CellId.set(groupCellSpan.getGroup(), cellId);

      //store mapping between simple cells (column/row-pairs) and multi-cells
      for (Iterator iter = cellId.getCells().iterator(); iter.hasNext(); ) {
        pair2GroupCellSpan.put(iter.next(), cellId);
      }
    }
    //update identifier of common nodes
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node n = nc.node();
      if (grouping.isGroupNode(n)) {
        continue;
      }

      //check if we have to remap this node
      final PartitionCellId cellId = (PartitionCellId) origNode2CellIdDP.get(n);
      if (cellId == null) {
        continue;
      }
      final PartitionCellId.Pair pair = (PartitionCellId.Pair) cellId.getCells().iterator().next();
      if (pair2GroupCellSpan.containsKey(pair)) {
        newNode2CellId.set(n, pair2GroupCellSpan.get(pair));
      }
    }
    grouping.dispose();

    //apply core layout
    doLayoutCore(graph);

    //restore original partition cell mapping
    graph.addDataProvider(PartitionGrid.PARTITION_CELL_DPKEY, origNode2CellIdDP);
  }

  /**
   * Checks if the cell span objects are consistent.
   * They are consistent if there is no pair of overlapping cell spans.
   *
   * @param groupCellSpans an array list of cell span objects
   * @return <code>true</code> if the cell span objects are consistent; <code>false</code> otherwise.
   */
  private static boolean isConsistent(final ArrayList groupCellSpans) {
    for (int i = 0; i < groupCellSpans.size(); i++) {
      final GroupCellSpan span1 = (GroupCellSpan) groupCellSpans.get(i);
      for (int j = i + 1; j < groupCellSpans.size(); j++) {
        if (GroupCellSpan.doOverlap(span1, (GroupCellSpan) groupCellSpans.get(j))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Class that provides the cell span information of a given group node.
   * The cell span of a group node encodes all partition cells that overlap with the group's bounding box.
   */
  private static class GroupCellSpan {
    private final Node group;
    private final LayoutGraph graph;
    private final PartitionGrid grid;
    private final PartitionCellId cellId;
    //the topmost row (row with the smallest index) that overlaps with the group.
    private RowDescriptor minRow;
    //the bottommost row (row with the largest index) that overlaps with the group.
    private RowDescriptor maxRow;
    //the leftmost column (column with the smallest index) that overlaps with the group.
    private ColumnDescriptor minColumn;
    //the rightmost column (column with the largest index) that overlaps with the group.
    private ColumnDescriptor maxColumn;

    GroupCellSpan(final Node group, final LayoutGraph graph) {
      this.group = group;
      this.graph = graph;
      this.grid = PartitionGrid.getPartitionGrid(graph);
      determineMinMaxRowAndColumn();
      this.cellId = grid.createCellSpanId(minRow, minColumn, maxRow, maxColumn);
      updateCellInsets();
    }

    /**
     * Ensures that the min/max column/row insets are greater or equal to the corresponding group node insets.
     */
    private void updateCellInsets() {
      final DataProvider insetsDP = graph.getDataProvider(GroupingKeys.GROUP_NODE_INSETS_DPKEY);
      final YInsets insets = (insetsDP == null) ? null : (YInsets) insetsDP.get(group);
      if (insets == null || minRow == null || minColumn == null) {
        return;
      }

      if (insets.left > minColumn.getLeftInset()) {
        minColumn.setLeftInset(insets.left);
      }
      if (insets.right > maxColumn.getRightInset()) {
        maxColumn.setRightInset(insets.right);
      }
      if (insets.top > minRow.getTopInset()) {
        minRow.setTopInset(insets.top);
      }
      if (insets.bottom > maxRow.getBottomInset()) {
        maxRow.setBottomInset(insets.bottom);
      }
    }

    /**
     * Determines the min/max row/column that overlaps with the bounding box of the given group node.
     *
     * @throws IllegalArgumentException if the group node does not overlap with any partition cell.
     */
    private void determineMinMaxRowAndColumn() {
      final YRectangle groupBounds = graph.getRectangle(group);

      final double groupLeftX = groupBounds.getX();
      final double groupRightX = groupBounds.getX() + groupBounds.getWidth();
      for (Iterator iter = grid.getColumns().iterator(); iter.hasNext(); ) {
        final ColumnDescriptor column = (ColumnDescriptor) iter.next();
        if (column.getOriginalPosition() < groupRightX
            && groupLeftX < column.getOriginalPosition() + column.getOriginalWidth()) {
          //group overlaps with column
          if (minColumn == null || minColumn.getIndex() > column.getIndex()) {
            minColumn = column;
          }
          if (maxColumn == null || maxColumn.getIndex() < column.getIndex()) {
            maxColumn = column;
          }
        }
      }

      final double groupTopY = groupBounds.getY();
      final double groupBottomY = groupBounds.getY() + groupBounds.getHeight();
      for (Iterator iter = grid.getRows().iterator(); iter.hasNext(); ) {
        final RowDescriptor row = (RowDescriptor) iter.next();
        if (row.getOriginalPosition() < groupBottomY
            && groupTopY < row.getOriginalPosition() + row.getOriginalHeight()) {
          //group overlaps with row
          if (minRow == null || minRow.getIndex() > row.getIndex()) {
            minRow = row;
          }
          if (maxRow == null || maxRow.getIndex() < row.getIndex()) {
            maxRow = row;
          }
        }
      }

      if (minRow == null || minColumn == null) {
        //group does not overlap with the partition cell structure
        throw new IllegalArgumentException();
      }
    }

    /**
     * Returns the group node associated with this instance.
     *
     * @return the group node associated with this instance.
     */
    public Node getGroup() {
      return group;
    }

    /**
     * Returns the partition cell id that encodes all partition cells that overlap with the bounding box of the
     * associated group node (see method {@link #getGroup()}).
     *
     * @return the partition cell id that encodes all partition cells that overlap with the bounding box of the
     * associated group node.
     * @see #getGroup()
     */
    public PartitionCellId getCellId() {
      return cellId;
    }

    /**
     * Returns <code>true</code> if the two given group cell span objects overlap with each other;
     * <code>false</code> otherwise.
     * Two cell span objects overlap, if they have at least one partition cell in common
     * (see method {@link #getCellId()}).
     *
     * @param span1 the first group cell span object.
     * @param span2 the second group cell span object.
     * @return <code>true</code> if the two given group cell span object overlap with each other;
     * <code>false</code> otherwise.
     * @see #getCellId()
     */
    public static boolean doOverlap(final GroupCellSpan span1, final GroupCellSpan span2) {
      return span1.minRow.getIndex() <= span2.maxRow.getIndex()
          && span2.minRow.getIndex() <= span1.maxRow.getIndex()
          && span1.minColumn.getIndex() <= span2.maxColumn.getIndex()
          && span2.minColumn.getIndex() <= span1.maxColumn.getIndex();
    }
  }
}
