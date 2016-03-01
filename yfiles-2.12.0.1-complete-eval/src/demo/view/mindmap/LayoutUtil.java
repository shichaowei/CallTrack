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

import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.AbstractLayoutStage;
import y.layout.FixNodeLayoutStage;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.tree.AbstractRotatableNodePlacer;
import y.layout.tree.DefaultPortAssignment;
import y.layout.tree.DelegatingNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.LayeredNodePlacer;
import y.layout.tree.TreeReductionStage;
import y.util.DataProviderAdapter;
import y.util.GraphHider;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.NodeRealizer;

import java.util.Comparator;

/**
 * Provides utility methods for automatically arranging a mind map.
 */
class LayoutUtil {
  /**
   * Prevent instantiation of utility class.
   */
  private LayoutUtil() {
  }

  /**
   * Registers the data providers for node placer and child comparators
   * necessary for arranging the specified mind map in a tree-like fashion.
   * @param graph the mind map.
   */
  static void addPlacersAndComparators( final Graph2D graph ) {
    final DataProvider nodePlacers = new NodePlacerProvider();
    graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacers);
    graph.addDataProvider(
            GenericTreeLayouter.CHILD_COMPARATOR_DPKEY,
            new ChildComparatorProvider());
  }

  /**
   * Registers the data map that determines if a mind map item is
   * placed to the left or to the right of the root item.
   * @param graph the mind map.
   * @see #getLeftRightMap(y.view.Graph2D)
   */
  static void addLeftRightMap( final Graph2D graph, final NodeMap map ) {
    graph.addDataProvider(DelegatingNodePlacer.LEFT_RIGHT_DPKEY, map);
  }

  /**
   * Returns the data map that determines if a mind map item is
   * placed to the left or to the right of the root item.
   * @param graph the mind map.
   */
  static NodeMap getLeftRightMap( final Graph2D graph ) {
    return (NodeMap) graph.getDataProvider(DelegatingNodePlacer.LEFT_RIGHT_DPKEY);
  }

  /**
   * Registers the data map that determines whether or not a connection is
   * a cross-reference.
   * @param graph the mind map.
   * @see #getCrossReferencesMap(y.view.Graph2D)
   */
  static void addCrossReferencesMap( final Graph2D graph, final EdgeMap map ) {
    graph.addDataProvider(TreeReductionStage.NON_TREE_EDGES_DPKEY, map);
  }

  /**
   * Returns the data map that determines whether or not a connection is
   * a cross-reference.
   * @param graph the mind map.
   */
  static EdgeMap getCrossReferencesMap( final Graph2D graph ) {
    return (EdgeMap) graph.getDataProvider(TreeReductionStage.NON_TREE_EDGES_DPKEY);
  }

  /**
   * Arranges the specified mind map.
   * @param graph the mind map that is arranged.
   */
  static void layout( final Graph2D graph ) {
    layout(graph, null);
  }

  /**
   * Arranges the subtree rooted a the specified node.
   * @param graph the mind map that is arranged.
   * @param node the root item of the subtree that is arranged.
   */
  static void layoutSubtree( final Graph2D graph, final Node node ) {
    layout(graph, node);
    graph.updateViews();
  }


  /**
   * Arranges the specified mind map in a tree-like fashion.
   * @param graph the mind map that is arranged.
   * @param node if <code>null</code>, the mind map is arranged from scratch;
   * otherwise only the subtree rooted at the given node is arranged.
   */
  private static void layout( final Graph2D graph, final Node node ) {
    //to make the graph look like a mind map, the edges have to connect at the underline of the items.
    //Therefor we have to put the source and target points to the bottom corners.
    final ViewModel model = ViewModel.instance;
    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
      final Edge edge = edgeCursor.edge();
      //cross edges should start and end at the item center
      if (model.isCrossReference(edge)) {
        graph.setSourcePointRel(edge, new YPoint(0, 0));
        graph.setTargetPointRel(edge, new YPoint(0, 0));
      } else {
        final Node source = edge.source();
        final Node target = edge.target();
        final NodeRealizer sourceRealizer = graph.getRealizer(source);
        final NodeRealizer targetRealizer = graph.getRealizer(target);
        final YPoint p;
        final YPoint p2;
        //when an item is on the left side, the bottom right corner is a target port,
        //on the right side it is a source port
        if (model.isLeft(target)) {
          p = new YPoint(-sourceRealizer.getWidth() * 0.5, sourceRealizer.getHeight() * 0.5);
          p2 = new YPoint(targetRealizer.getWidth() * 0.5, targetRealizer.getHeight() * 0.5);
        } else {
          p = new YPoint(sourceRealizer.getWidth() * 0.5, sourceRealizer.getHeight() * 0.5);
          p2 = new YPoint(-targetRealizer.getWidth() * 0.5, targetRealizer.getHeight() * 0.5);
        }
        //when edges start at the bottom corner at the center item, it would look weired,
        //so we exclude the center item here
        if (!model.isRoot(source)) {
          graph.setSourcePointRel(edge, p);
        } else {
          graph.setSourcePointRel(edge, YPoint.ORIGIN);
        }
        graph.setTargetPointRel(edge, p2);
      }
    }

    //Force the edges to connect to the source ports, coming from the right side
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, new DataProviderAdapter() {
      public Object get( Object dataHolder ) {
        if (dataHolder instanceof Edge) {
          //again, exclude cross edges as they should come from any side
          if (ViewModel.instance.isCrossReference((Edge) dataHolder)) {
            return PortConstraint.create(PortConstraint.ANY_SIDE, true);
            //the right direction depends on the side an item is placed
          } else {
            final boolean isLeftSide = ViewModel.instance.isLeft(((Edge) dataHolder).target());
            if (isLeftSide) {
              return PortConstraint.create(PortConstraint.WEST, true);
            } else {
              return PortConstraint.create(PortConstraint.EAST, true);
            }
          }
        }
        return null;
      }
    });
    //the same for target ports
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, new DataProviderAdapter() {
      public Object get( Object dataHolder ) {
        if (dataHolder instanceof Edge) {
          if (ViewModel.instance.isCrossReference((Edge) dataHolder)) {
            return PortConstraint.create(PortConstraint.ANY_SIDE, true);
          } else {
            boolean isLeftSide = ViewModel.instance.isLeft(((Edge) dataHolder).target());
            if (isLeftSide) {
              return PortConstraint.create(PortConstraint.EAST, true);
            } else {
              return PortConstraint.create(PortConstraint.WEST, true);
            }
          }
        }
        return null;
      }
    });
    GenericTreeLayouter treeLayouter = new GenericTreeLayouter();
    treeLayouter.setDefaultPortAssignment(new DefaultPortAssignment(DefaultPortAssignment.MODE_PORT_CONSTRAINTS));

    //Cross Edges do not belong to the tree structure
    TreeReductionStage trs = new TreeReductionStage();
    treeLayouter.appendStage(trs);

    Layouter layouter = treeLayouter;

    final DataProviderAdapter rootDp = new DataProviderAdapter() {
      final ViewModel model = ViewModel.instance;
      public boolean getBool( final Object dataHolder ) {
        return model.isRoot((Node) dataHolder);
      }
    };

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    layoutExecutor.getLayoutMorpher().setKeepZoomFactor(true);
    layoutExecutor.getLayoutMorpher().setPreferredDuration(300);
    layoutExecutor.getLayoutMorpher().setEasedExecution(true);

    if (node != null) {
      layouter = new SubtreeLayoutStage(layouter);
      layoutExecutor.setMode(Graph2DLayoutExecutor.BUFFERED);
      graph.addDataProvider(SubtreeLayoutStage.GLOBAL_ROOT_DPKEY, rootDp);
      graph.addDataProvider(SubtreeLayoutStage.SUBTREE_ROOT_DPKEY, new DataProviderAdapter() {
        public boolean getBool( final Object dataHolder ) {
          return dataHolder == node;
        }
      });
    } else {
      layouter = new FixNodeLayoutStage(treeLayouter);
      layoutExecutor.setMode(Graph2DLayoutExecutor.ANIMATED);
      graph.addDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY, rootDp);
    }

    final Object view = graph.getCurrentView();
    if (view instanceof Graph2DView) {
      layoutExecutor.doLayout((Graph2DView) view, layouter);
    } else {
      layoutExecutor.doLayout(graph, layouter);
    }

    if (node != null) {
      graph.removeDataProvider(SubtreeLayoutStage.SUBTREE_ROOT_DPKEY);
      graph.removeDataProvider(SubtreeLayoutStage.GLOBAL_ROOT_DPKEY);
    } else {
      graph.removeDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY);
    }
  }


  /**
   * Applies the core layout algorithm to a subtree of the given min map.
   * The subtree that is arranged is specified by the data providers
   * {@link #SUBTREE_ROOT_DPKEY} and
   * {@link TreeReductionStage#NON_TREE_EDGES_DPKEY}.
   */
  private static class SubtreeLayoutStage extends AbstractLayoutStage {
    /**
     * Data provider key to register a data provider that identifies the
     * global root item of the mind map.
     */
    static final Object GLOBAL_ROOT_DPKEY =
            "demo.view.mindmap.MindMapUtil.GLOBAL_ROOT_DPKEY";
    /**
     * Data provider key to register a data provider that identifies the
     * root item of the subtree that has to be arranged.
     */
    static final Object SUBTREE_ROOT_DPKEY =
            "demo.view.mindmap.MindMapUtil.SUBTREE_ROOT_DPKEY";

    /**
     * Initializes a new <code>SubtreeLayoutStage</code> for the given
     * core layout algorithm.
     * @param core the core layout algorithm that actually calculates
     * the layout.
     */
    SubtreeLayoutStage( final Layouter core ) {
      super(new FixNodeLayoutStage(core));
    }

    public boolean canLayout( final LayoutGraph graph ) {
      return canLayoutCore(graph);
    }

    public void doLayout( final LayoutGraph graph ) {
      GraphHider hider = new GraphHider(graph);

      // remove all non-tree edges
      final DataProvider crossEdges = graph.getDataProvider(TreeReductionStage.NON_TREE_EDGES_DPKEY);
      if (crossEdges != null) {
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
          final Edge edge = ec.edge();
          if (crossEdges.getBool(edge)) {
            hider.hide(edge);
          }
        }
      }

      // determine the root of the tree
      final DataProvider isRootNodeProvider = graph.getDataProvider(GLOBAL_ROOT_DPKEY);
      Node root = null;
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        if (isRootNodeProvider.getBool(node)) {
          root = node;
          break;
        }
      }

      // remove all elements that do not belong to the subtree that has to be
      // arranged
      NodeList stack = new NodeList(root);
      final DataProvider subtreeRootProvider = graph.getDataProvider(SUBTREE_ROOT_DPKEY);
      while (!stack.isEmpty()) {
        Node currentNode = stack.popNode();
        if (!subtreeRootProvider.getBool(currentNode)) {
          for (EdgeCursor ec = currentNode.outEdges(); ec.ok(); ec.next()) {
            stack.push(ec.edge().target());
          }
          hider.hide(currentNode);
        }
      }


      // ensure that the subtree root keeps its current location
      graph.addDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY, subtreeRootProvider);

      // arrange the remaining subtree
      doLayoutCore(graph);

      graph.removeDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY);


      // re-insert all previous hidden elements
      hider.unhideAll();
    }
  }

  /**
   * Provides different {@link y.layout.tree.NodePlacer} instances for the items
   * in a main map.
   * For the root item, {@link DelegatingNodePlacer} is used to place the root
   * item's children to the left and to the right of the root item.
   * For all other items, {@link LayeredNodePlacer} is used.
   */
  private static final class NodePlacerProvider extends DataProviderAdapter {
    private final DelegatingNodePlacer rootPlacer;

    NodePlacerProvider() {
      rootPlacer = new DelegatingNodePlacer(
              AbstractRotatableNodePlacer.Matrix.DEFAULT,
              newPlacer(AbstractRotatableNodePlacer.Matrix.ROT270),
              newPlacer(AbstractRotatableNodePlacer.Matrix.ROT90));
      rootPlacer.setSpacing(1);
    }

    public Object get( final Object dataHolder ) {
      final Node node = (Node) dataHolder;
      if (ViewModel.instance.isRoot(node)) {
        return rootPlacer;
      } else {
        if (ViewModel.instance.isLeft(node)) {
          return rootPlacer.getPlacerUpperLeft();
        } else {
          return rootPlacer.getPlacerLowerRight();
        }
      }
    }

    private static LayeredNodePlacer newPlacer(
            final AbstractRotatableNodePlacer.Matrix matrix
    ) {
      final LayeredNodePlacer placer = new LayeredNodePlacer(matrix, matrix);
      placer.setRoutingStyle(LayeredNodePlacer.ORTHOGONAL_STYLE);
      placer.setRootAlignment(AbstractRotatableNodePlacer.RootAlignment.CENTER);
      placer.setSpacing(10);
      placer.setLayerSpacing(45);
      placer.setVerticalAlignment(0);
      return placer;
    }
  }

  /**
   * Provides {@link java.util.Comparator} instances used by the
   * {@link LayeredNodePlacer} instances to determine the order of the
   * items in each layer.
   */
  private static final class ChildComparatorProvider extends DataProviderAdapter {
    public Object get( final Object dataHolder ) {
      return new YCoordComparator(true);
    }
  }
  
  /**
   * Sorts edges depending on the y-coordinates of their target nodes.
   * <p>
   *   By default, edges are sorted from top to bottom. However, in case sides are taken into consideration, edges with
   *   target nodes to the right are sorted bottom up and edges with target nodes to the left are sorted top down.
   *   This is important when this comparator is used to determine the order of children for layout.
   * </p>
   */
  static final class YCoordComparator implements Comparator {
    private final boolean considerSides;

    public YCoordComparator() {
      this(false);
    }

    public YCoordComparator(boolean considerSides) {
      this.considerSides = considerSides;
    }

    public int compare( final Object o1, final Object o2 ) {
      final Edge edge1 = (Edge) o1;
      final Edge edge2 = (Edge) o2;
      final double y1 = getY(edge1);
      final double y2 = getY(edge2);

      if (!considerSides || isLeft(edge1.target())) {
        return Double.compare(y1, y2);
      } else {
        return Double.compare(y2, y1);
      }
    }

    private boolean isLeft(Node node) {
      return Boolean.TRUE.equals(node.getGraph().getDataProvider(DelegatingNodePlacer.LEFT_RIGHT_DPKEY).get(node));
    }

    private double getY( final Edge edge ) {
      return ((LayoutGraph) edge.getGraph()).getY(edge.target());
    }
  }
}
