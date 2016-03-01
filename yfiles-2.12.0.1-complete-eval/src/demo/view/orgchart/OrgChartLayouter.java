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
package demo.view.orgchart;

import y.algo.Trees;
import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.ListCell;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.base.YList;
import y.geom.LineSegment;
import y.geom.YLineSegmentCursor;
import y.geom.YPoint;
import y.geom.YPointPath;
import y.layout.AbstractLayoutStage;
import y.layout.EdgeLayout;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.tree.AssistantPlacer;
import y.layout.tree.DefaultNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.LeftRightPlacer;
import y.layout.tree.NodePlacer;
import y.util.Maps;

/**
 * A layout algorithm for tree-structured organization charts. It allows to specify different 
 * layout strategies for the child nodes of a node. Furthermore, it supports special placement for
 * nodes that are marked as assistants,
 */
public class OrgChartLayouter implements Layouter {

  /**
   * Child layout specifier. The children of a node shall be arranged left to right on the same layer.
   */
  public static final Object CHILD_LAYOUT_SAME_LAYER = "SAME_LAYER";
  
  /**
   * Child layout specifier. The children of a node shall be arranged below each other and placed left of a common bus.
   */
  public static final Object CHILD_LAYOUT_LEFT_BELOW = "LEFT_BELOW";

  /**
   * Child layout specifier. The children of a node shall be arranged below each other and placed right of a common bus.
   */
  public static final Object CHILD_LAYOUT_RIGHT_BELOW = "RIGHT_BELOW";
  
  /**
   * Child layout specifier. The children of a node shall be arranged on both sides of a common vertical bus. Children on both sides
   * are placed below each.    
   */
  public static final Object CHILD_LAYOUT_BELOW = "BELOW";
  
  /**
   * DataProvider key used to register a DataProvider with the input graph. For each node in the graph 
   * the registered DataProvider returns either of {@link #CHILD_LAYOUT_BELOW}, {@link #CHILD_LAYOUT_LEFT_BELOW}, 
   * {@link #CHILD_LAYOUT_RIGHT_BELOW}, or {@link #CHILD_LAYOUT_SAME_LAYER}.
   */
  public static final Object CHILD_LAYOUT_DPKEY = "OrgChartLayouter#CHILD_LAYOUT_DPKEY";
  
  /**
   * DataProvider key used to register a DataProvider with the input graph. For each node in the graph 
   * the registered DataProvider returns a boolean value that signifies whether or not the
   * node is to be considered an assistant to its parent node. Assistants are always placed along to the left or right of the
   * the vertical bus leaving the parent node. For non-assistant child nodes the child layout specified for the
   * parent node will be applied.
   */
  public static final Object ASSISTANT_DPKEY = "OrgChartLayouter#ASSISTANT_DPKEY";
  
  private boolean duplicateBendsOnSharedBus = false;


  /**
   * Sets whether or not to duplicate the control points of the returned edge paths 
   * that are placed on an path segment of another edge. For example, if an edge
   * has the control points, [a,b,c], and a and b are placed on a shared bus, then the
   * resulting edge path is [a,a,b,b,c]. Duplicating control points on a shared bus,
   * allows the edge rendering facility to treat such control points differently.
   * By default this feature is disabled.
   */
  public void setDuplicateBendsOnSharedBus(final boolean duplicateBendsOnSharedBus) {
    this.duplicateBendsOnSharedBus = duplicateBendsOnSharedBus;
  }

  /**
   * Returns whether or not to duplicate the control points of the returned edge paths 
   * that are placed on an path segment of another edge. For example, if an edge 
   * has the control points, [a,b,c], and a and b are placed on a shared bus, then the
   * resulting edge path is [a,a,b,b,c]. Duplicating control points on a shared bus,
   * allows the edge rendering facility to treat such control points differently.
   * By default this feature is disabled.
   */
  public boolean isDuplicateBendsOnSharedBus() {
    return duplicateBendsOnSharedBus;
  }

  /**
   * Assigns coordinates to the elements of the input graph.
   */
  public void doLayout(final LayoutGraph graph) {
    final GenericTreeLayouter gtl = new GenericTreeLayouter();
    gtl.setGroupingSupported(true);
    configureNodePlacers(graph);    
    gtl.doLayout(graph);
    if(isDuplicateBendsOnSharedBus()) {
      final Layouter bendDuplicator = new BendDuplicatorStage(null);
      bendDuplicator.doLayout(graph);
    }
  }
  
  /**
   * Configures the layout algorithm using the information provided by the
   * DataProviders registered with the keys {@link #ASSISTANT_DPKEY} and {@link #CHILD_LAYOUT_DPKEY}.   
   */
  protected void configureNodePlacers(final LayoutGraph graph) {
    final DataProvider childLayoutDP = graph.getDataProvider(CHILD_LAYOUT_DPKEY);
    final NodeMap nodePlacerMap = Maps.createHashedNodeMap();
    if(childLayoutDP != null) {
      graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
      for(final NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        nodePlacerMap.set(n, createNodePlacer(childLayoutDP.get(n)));        
      }
      graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, nodePlacerMap);
    }

    final DataProvider assistDP = graph.getDataProvider(ASSISTANT_DPKEY);
    for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node n = nc.node();
      if(assistDP != null && assistDP.getBool(n)) {
        if(n.inDegree() > 0 && n.firstInEdge().source().outDegree() > 1) {
          final AssistantPlacer placer = new AssistantPlacer();
          final NodePlacer parentPlacer = (NodePlacer) nodePlacerMap.get(n.firstInEdge().source());
          placer.setChildNodePlacer(parentPlacer);
          nodePlacerMap.set(n.firstInEdge().source(), placer);
        }
      }
    }
    graph.addDataProvider(AssistantPlacer.ASSISTANT_DPKEY, assistDP);     
  }
  
  /**
   * Creates a NodePlacer for the given child layout specifier.
   */
  protected NodePlacer createNodePlacer(final Object childLayout) {
    if(childLayout == CHILD_LAYOUT_LEFT_BELOW) {      
      final DefaultNodePlacer placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, DefaultNodePlacer.ALIGNMENT_CENTER, DefaultNodePlacer.ROUTING_FORK, 20.0d, 80.d);
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_VERTICAL_TO_LEFT);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_LEADING_ON_BUS);
      placer.setRoutingStyle(DefaultNodePlacer.ROUTING_FORK_AT_ROOT);
      return placer;
    }
    else if(childLayout == CHILD_LAYOUT_RIGHT_BELOW) {
      final DefaultNodePlacer placer = new DefaultNodePlacer(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD, DefaultNodePlacer.ALIGNMENT_CENTER, DefaultNodePlacer.ROUTING_FORK, 20.0d, 80.d);
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_VERTICAL_TO_RIGHT);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_LEADING_ON_BUS);
      placer.setRoutingStyle(DefaultNodePlacer.ROUTING_FORK_AT_ROOT);
      return placer;
    }
    else if(childLayout == CHILD_LAYOUT_BELOW) {
      final LeftRightPlacer placer = new LeftRightPlacer();
      placer.setPlaceLastOnBottom(false);
      return placer;
    }
    else { //default
      final DefaultNodePlacer placer = new DefaultNodePlacer();
      placer.setChildPlacement(DefaultNodePlacer.PLACEMENT_HORIZONTAL_DOWNWARD);
      placer.setRootAlignment(DefaultNodePlacer.ALIGNMENT_MEDIAN);
      return placer;
    }        
  }
    
  /**
   * The input graph needs to be a tree or a collection of trees.
   */
  public boolean canLayout(final LayoutGraph graph) {
    return Trees.isForest(graph); //simplified
  }
  
  /**
   * LayoutStage that duplicates bends that share a common bus.
   */
  static class BendDuplicatorStage extends AbstractLayoutStage {

    public BendDuplicatorStage(final Layouter coreLayouter) {
      super(coreLayouter);
    }
    
    public boolean canLayout(final LayoutGraph graph) {
      return true;
    }

    public void doLayout(final LayoutGraph graph) {
      doLayoutCore(graph);
      
      for(final NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node n = nc.node();
        for(final EdgeCursor ec = n.outEdges(); ec.ok(); ec.next()) {
          boolean lastSegmentOverlap = false;
          final EdgeLayout er = graph.getEdgeLayout(ec.edge());
          
          if(er.pointCount() > 0) {
            //last bend point
            final YPoint bendPoint = er.getPoint(er.pointCount()-1);
            
            loop:for(final EdgeCursor ecc = n.outEdges(); ecc.ok(); ecc.next()) {
              
              if(ecc.edge() != ec.edge()) {
                final YPointPath path = graph.getPath(ecc.edge());
                for(final YLineSegmentCursor lc = path.lineSegments(); lc.ok(); lc.next()) {
                  final LineSegment seg = lc.lineSegment();
                  if(seg.contains(bendPoint)) {
                    lastSegmentOverlap = true;
                    break loop;
                  }
                }
              }
            }      
          }
          
          final YList points = graph.getPointList(ec.edge());
          for(ListCell c = points.firstCell(); c != null; c = c.succ()) {
            final YPoint p = (YPoint) c.getInfo();
            if(c.succ() == null && !lastSegmentOverlap) {
              break;
            }
            points.insertBefore(new YPoint(p.x,p.y), c);
          }
          graph.setPoints(ec.edge(), points);
        }
      }    
    }
  }
}
