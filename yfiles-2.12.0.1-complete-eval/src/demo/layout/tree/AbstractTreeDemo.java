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
package demo.layout.tree;

import demo.view.DemoBase;
import y.anim.AnimationPlayer;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YPoint;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.FromSketchNodePlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.util.DataProviderAdapter;
import y.view.CreateChildEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.HotSpotMode;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.PortAssignmentMoveSelectionMode;

import javax.swing.AbstractAction;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

/**
 * The AbstractTreeDemo is a base class for several tree demos.
 * It contains ViewModes and other helper methods for tree manipulation and visualization.
 **/
public abstract class AbstractTreeDemo extends DemoBase {
  protected GenericTreeLayouter treeLayouter = new GenericTreeLayouter();

  protected EdgeMap sourcePortMap;
  protected EdgeMap targetPortMap;
  protected NodeMap portAssignmentMap;
  protected NodeMap nodePlacerMap;
  protected PortAssignmentMoveSelectionMode portAssignmentMoveMode = new TreePortAssignmentMode();
  protected Color[] layerColors = {Color.red, Color.orange, Color.yellow, Color.blue, Color.cyan,
      Color.green};

  /**
   * Instantiates a new AbstractDemo.
   */
  protected AbstractTreeDemo() {
    view.addViewMode(new TreeCreateEditMode());

    AnimationPlayer animationPlayer = new AnimationPlayer();
    animationPlayer.addAnimationListener(view);

    Graph2D graph = view.getGraph2D();

    sourcePortMap = graph.createEdgeMap();
    targetPortMap = graph.createEdgeMap();
    portAssignmentMap = graph.createNodeMap();
    nodePlacerMap = graph.createNodeMap();
    graph.addDataProvider(GenericTreeLayouter.NODE_PLACER_DPKEY, new DataProviderAdapter() {
      public Object get( final Object dataHolder ) {
        if (((Node) dataHolder).outDegree() == 0) {
          return null;
        } else {
          return nodePlacerMap.get(dataHolder);
        }
      }
    });
    graph.addDataProvider(GenericTreeLayouter.PORT_ASSIGNMENT_DPKEY, portAssignmentMap);
    graph.addDataProvider(GenericTreeLayouter.CHILD_COMPARATOR_DPKEY, new ChildEdgeComparatorProvider());
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);

    portAssignmentMoveMode.setSpc(sourcePortMap);
    portAssignmentMoveMode.setTpc(targetPortMap);
  }

  /**
   * Set the NodePlacer for the given node.
   */
  public void setNodePlacer(Node node, NodePlacer placer) {
    nodePlacerMap.set(node, placer);
  }

  /**
   * Calculate the layout and update the view (using an animation).
   */
  public void calcLayout() {
    if (!view.getGraph2D().isEmpty()) {
      Cursor oldCursor = view.getViewCursor();
      try {
        view.setViewCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.applyLayoutAnimated(treeLayouter);
      } finally {
        view.setViewCursor(oldCursor);
      }
    }
  }

  /**
   * May be overridden by subclasses.
   */
  protected PopupMode createTreePopupMode() {
    return null;
  }

  /**
   * May be overridden by subclasses.
   */
  protected void registerViewModes() {
  }

  protected NodePlacer createDefaultNodePlacer() {
    return new SimpleNodePlacer();
  }

  protected final class ChildEdgeComparatorProvider extends DataProviderAdapter {
    public Object get(Object dataHolder) {
      NodePlacer placer = (NodePlacer) nodePlacerMap.get(dataHolder);
      if (placer instanceof FromSketchNodePlacer){
        return ((FromSketchNodePlacer) placer).createFromSketchComparator();
      }
      return null;
    }
  }

  private final class TreeHotSpotMode extends HotSpotMode {
    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);
      calcLayout();
    }
  }

  protected class TreeCreateEditMode extends EditMode {
    TreeCreateEditMode() {
      if (portAssignmentMoveMode == null) {
        throw new IllegalStateException("portAssignmentMoveMode is null");
      }
      setMoveSelectionMode(portAssignmentMoveMode);
      setCreateEdgeMode(new TreeCreateChildEdgeMode());
      setHotSpotMode(new TreeHotSpotMode());
      setPopupMode(AbstractTreeDemo.this.createTreePopupMode());
    }

    public boolean doAllowNodeCreation() {
      return getGraph2D().N() == 0;
    }

    protected void nodeCreated(Node v) {
      super.nodeCreated(v);
      setNodePlacer(v, createDefaultNodePlacer());
    }
  }

  private final class TreeCreateChildEdgeMode extends CreateChildEdgeMode {

    public void mouseReleasedLeft(double x, double y) {
      // fire event to mark start of edge creation for undo/redo
      getGraph2D().firePreEvent();
      super.mouseReleasedLeft(x, y);
    }

    protected void edgeCreated(Edge edge) {
      int depth = 1;
      for (Node node = edge.source(); node.inDegree() > 0; node = node.firstInEdge().source()) {
        depth++;
      }
      Graph2D g = getGraph2D();
      g.getRealizer(edge.target()).setFillColor(layerColors[depth % layerColors.length]);
      EdgeRealizer er = g.getRealizer(edge);
      if (nodePlacerMap.get(edge.source()) == null) {
        parseNodePlacement(g, edge, er);
      }

      setNodePlacer(edge.target(), createDefaultNodePlacer());
      parseTargetPort(g, edge, er);
      g.unselectAll();
      calcLayout();

      // fire event to mark end of edge creation (including layout) for undo/redo
      getGraph2D().firePostEvent();
    }

    protected NodeRealizer activeDummyTargetRealizer;

    protected boolean acceptSourceNode(Node source, double x, double y) {
      final boolean accept = super.acceptSourceNode(source, x, y);
      activeDummyTargetRealizer = createChildNodeRealizer();
      int depth = 1;
      for (Node n = source; n.inDegree() > 0; n = n.firstInEdge().source()){
              depth++;
      }
      activeDummyTargetRealizer.setFillColor(layerColors[depth % layerColors.length]);
      return accept;
    }

    protected NodeRealizer createDummyTargetNodeRealizer(double x, double y) {
      return activeDummyTargetRealizer;
    }

    private void parseNodePlacement(Graph2D g, Edge e, EdgeRealizer er) {
      nodePlacerMap.set(e.source(), new SimpleNodePlacer());
    }

    private void parseTargetPort(Graph2D g, Edge e, EdgeRealizer er) {
      if (er.bendCount() > 0) {
        YPoint lastPoint = new YPoint(er.lastBend().getX(), er.lastBend().getY());
        NodeRealizer target = g.getRealizer(e.target());
        double dx = lastPoint.x - target.getCenterX();
        double dy = lastPoint.y - target.getCenterY();
        byte side;
        if (Math.abs(dx) > Math.abs(dy)) {
          if (dx > 0.0) {
            side = PortConstraint.EAST;
          } else {
            side = PortConstraint.WEST;
          }
        } else {
          if (dy > 0.0) {
            side = PortConstraint.SOUTH;
          } else {
            side = PortConstraint.NORTH;
          }
        }
        targetPortMap.set(e, PortConstraint.create(side));
      }
    }

    protected NodeRealizer createChildNodeRealizer() {
      NodeRealizer retValue = super.createChildNodeRealizer();
      retValue.setLabelText("");
      return retValue;
    }

  }

  protected class SetHorizontalAlignmentAction extends AbstractAction {
    private RootAlignment alignment;

    protected SetHorizontalAlignmentAction(String name, RootAlignment alignment) {
      super(name);
      this.alignment = alignment;
    }

    public void actionPerformed(ActionEvent e) {
      for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        NodePlacer nodePlacer = (NodePlacer) nodePlacerMap.get(node);

        try {
          Method method = nodePlacer.getClass().getMethod("setRootAlignment", new Class[]{RootAlignment.class});
          method.invoke(nodePlacer, new Object[]{alignment});
        } catch (Exception ex) {
        }
      }
      calcLayout();
    }
  }

  abstract class SetNodePlacerAction extends AbstractAction {
    protected SetNodePlacerAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
        Node node = nodeCursor.node();
        nodePlacerMap.set(node, createNodePlacer());
      }
      calcLayout();
    }

    protected abstract NodePlacer createNodePlacer();
  }

  private final class TreePortAssignmentMode extends PortAssignmentMoveSelectionMode {
    TreePortAssignmentMode() {
      super(null, null);
    }

    protected boolean isPortReassignmentAllowed(Edge edge, boolean source) {
      return !source;
    }

    protected void selectionMovedAction(double dx, double dy, double x, double y) {
      super.selectionMovedAction(dx, dy, x, y);
      calcLayout();
    }

  }
}
