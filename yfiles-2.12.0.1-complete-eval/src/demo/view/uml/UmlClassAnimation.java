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
package demo.view.uml;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.geom.YDimension;
import y.geom.YPoint;
import y.layout.AbstractLayoutStage;
import y.layout.BufferedLayouter;
import y.layout.GraphLayout;
import y.layout.LayoutGraph;
import y.layout.Layouter;
import y.layout.router.polyline.EdgeRouter;
import y.util.DataProviderAdapter;
import y.util.Maps;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.LayoutMorpher;
import y.view.NodePort;
import y.view.NodeRealizer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;

/**
 * This class animates opening and closing of sections of the UML class. First it is an {@link AnimationObject} that
 * controls the progress of the animation and the update of the view. The progress is expressed by a value that varies
 * between <code>0</code> and <code>1</code>. On the other hand it is an {@link Drawable} that is repainted on every
 * view update. The repaint is delegated to {@link UmlClassConfiguration#paintAnimatedNode(y.view.NodeRealizer,
 * java.awt.Graphics2D, double, double, double)} with the current progress.
 */
abstract class UmlClassAnimation implements AnimationObject, Drawable {
  protected static final int DURATION = 300;

  protected final Graph2DView view;
  protected final NodeRealizer context;
  protected final boolean isClosing;
  private final UmlClassConfiguration painter;
  private final Graph2DViewRepaintManager repaintManager;
  private double state;

  /**
   * Creates an animation to animate opening and closing of sections of the UML class.
   *
   * @param view      the view to paint the animation
   * @param context   the realizer to animate
   * @param isClosing whether to animate opening and closing of sections
   */
  UmlClassAnimation(
      final Graph2DView view,
      final NodeRealizer context,
      final boolean isClosing
  ) {
    this.view = view;
    this.context = context;
    this.isClosing = isClosing;
    painter = new UmlClassConfiguration();
    this.repaintManager = new Graph2DViewRepaintManager(view);
  }

  /**
   * Animates the opening and closing of the a node and the movement of its adjacent edges. The animated painting of the
   * node is done by the method {@link UmlClassConfiguration#paintAnimatedNode(y.view.NodeRealizer, java.awt.Graphics2D, double, double, double)},
   * and is called from here. The animation of the adjacent edges is done here.
   * The animation of the node works only on open nodes for both cases opening and closing. In the latter case the
   * node will be closed after the animation has been finished. But we need the closed size of the node to calculate the
   * target layout for the layout morpher with the edge router. Therefore the {@link PreserveNodeSizeLayoutStage} shrinks
   * the nodes to the closed size before and resize them to the opened size after the edge routing.
   * To avoid that the adjacent edges are clipped at the opened size while the animation we add {@link NodePort}s at the
   * connection points of the closed node.
   *
   */
  public void play() {
    final AnimationPlayer player = new AnimationPlayer();
    player.addAnimationListener(repaintManager);
    view.addDrawable(this);

    // add all adjacent edges to repaint manager
    final Graph2D graph = view.getGraph2D();
    final Node currentNode = context.getNode();
    for (EdgeCursor ec = currentNode.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      repaintManager.add(graph.getRealizer(edge));
    }

    // store source and target ports to set node ports at these positions
    EdgeMap sourcePorts = Maps.createHashedEdgeMap();
    EdgeMap targetPorts = Maps.createHashedEdgeMap();
    for (EdgeCursor ec = currentNode.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      sourcePorts.set(edge, graph.getSourcePointAbs(edge));
      targetPorts.set(edge, graph.getTargetPointAbs(edge));
    }
    if (!isClosing) {
      open();
    }

    // add node ports for all adjacent edges, these node ports get animated afterwards
    final NodeRealizer currentRealizer = graph.getRealizer(currentNode);
    for (EdgeCursor ec = currentNode.outEdges(); ec.ok(); ec.next()) {
      final Edge outEdge = ec.edge();
      final NodePort port = UmlRealizerFactory.createDummyNodePort(currentRealizer, (YPoint) sourcePorts.get(outEdge));
      currentRealizer.addPort(port);
      port.bindSourcePort(outEdge);
    }
    for (EdgeCursor ec = currentNode.inEdges(); ec.ok(); ec.next()) {
      final Edge inEdge = ec.edge();
      final NodePort port = UmlRealizerFactory.createDummyNodePort(currentRealizer, (YPoint) targetPorts.get(inEdge));
      currentRealizer.addPort(port);
      port.bindTargetPort(inEdge);
    }

    // if it is a closing animation, store the future size of the current node for layout
    if (isClosing) {
      graph.addDataProvider(PreserveNodeSizeLayoutStage.NODE_SIZE_DPKEY, new DataProviderAdapter() {
        public Object get(Object dataHolder) {
          if (dataHolder == currentNode) {
            return getClosedSize();
          } else {
            return null;
          }
        }
      });
    }

    // only route adjacent edges of the current node
    final EdgeRouter edgeRouter = new EdgeRouter();
    edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
    graph.addDataProvider(EdgeRouter.SELECTED_NODES, new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        if (dataHolder instanceof Node) {
          Node node = (Node) dataHolder;
          return node == currentNode;
        }
        return false;
      }
    });

    // configure layouter
    final BufferedLayouter layouter = new BufferedLayouter(new PreserveNodeSizeLayoutStage(edgeRouter));
    final GraphLayout graphLayout = layouter.calcLayout(graph);

    // clean up data providers
    graph.removeDataProvider(PreserveNodeSizeLayoutStage.NODE_SIZE_DPKEY);
    graph.removeDataProvider(EdgeRouter.SELECTED_NODES);

    // construct and run the animation
    final CompositeAnimationObject concurrentAnimation = AnimationFactory.createConcurrency();
    final LayoutMorpher morpher = new LayoutMorpher(view, graphLayout);
    morpher.setPreferredDuration(DURATION);
    morpher.setKeepZoomFactor(true);
    concurrentAnimation.addAnimation(morpher);
    concurrentAnimation.addAnimation(this);

    player.animate(concurrentAnimation);

    // if it is a closing animation, close the node while keeping the calculated port positions
    if (isClosing) {
      sourcePorts = Maps.createHashedEdgeMap();
      targetPorts = Maps.createHashedEdgeMap();
      for (EdgeCursor ec = currentNode.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        sourcePorts.set(edge, graph.getSourcePointAbs(edge));
        targetPorts.set(edge, graph.getTargetPointAbs(edge));
      }

      close();

      for (EdgeCursor ec = currentNode.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        graph.setSourcePointAbs(edge, (YPoint) sourcePorts.get(edge));
        graph.setTargetPointAbs(edge, (YPoint) targetPorts.get(edge));
      }
    }

    // clean up
    for (int i = currentRealizer.portCount() - 1; i >= 0; i--) {
      currentRealizer.removePort(i);
    }
    for (EdgeCursor ec = currentNode.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      repaintManager.remove(graph.getRealizer(edge));
    }
    view.removeDrawable(this);
    player.removeAnimationListener(repaintManager);
  }

  /**
   * Closes the part of the node whose change gets animated.
   */
  protected abstract void close();

  /**
   * Opens the part of the node whose change gets animated.
   */
  protected abstract void open();

  /**
   * Returns the size of the realizer in closed state.
   */
  protected abstract YDimension getClosedSize();

  /**
   * Returns the lower y-coordinate of the part of the realizer that is fixed during the animation.
   *
   * @return the lower y-coordinate of the part of the realizer that is fixed during the animation
   */
  protected abstract double getFixedY();

  /**
   * Returns the upper y-coordinate of the part of the realizer that moves during the animation.
   *
   * @return the upper y-coordinate of the part of the realizer that moves during the animation
   */
  protected abstract double getMovingY();

  /**
   * This method gets called after the state of the animation has been updated. It serves as a hook to perform some
   * actions after this event has happened. By default this method does nothing.
   */
  protected void stateUpdated(final double state) {
  }

  public void initAnimation() {
    context.setVisible(false);
    repaintManager.add(this);
  }

  public void calcFrame(final double time) {
    state = isClosing ? 1.0 - time : time;
    stateUpdated(state);
  }

  public void disposeAnimation() {
    repaintManager.remove(this);
    context.setVisible(true);
  }

  public long preferredDuration() {
    return DURATION;
  }

  public void paint(final Graphics2D graphics) {
    painter.paintAnimatedNode(context, graphics, getFixedY(), getMovingY(), state);
  }

  public Rectangle getBounds() {
    final double lineWidth = UmlRealizerFactory.LINE_EDGE_CREATION_BUTTON_OUTLINE.getLineWidth();
    final int minX = (int) Math.floor(context.getX() - lineWidth * 0.5);
    final int minY = (int) Math.floor(context.getY() - lineWidth * 0.5);
    final int maxX = (int) Math.ceil(context.getX() + context.getWidth() + lineWidth * 0.5);
    final int maxY = (int) Math.ceil(context.getY() + context.getHeight() + lineWidth * 0.5);
    return new Rectangle(minX, minY, maxX - minX, maxY - minY);
  }

  /**
   * Layout stage that takes care about the nodes sizes.
   * It resizes the current node to its future size after the animation so the layout is calculated correctly.
   * Afterwards, it restores the nodes size so it is consistent with node in the view graph.
   */
  private static final class PreserveNodeSizeLayoutStage extends AbstractLayoutStage {
    public static final Object NODE_SIZE_DPKEY = "PreserveNodeSizeLayoutStage.NODE_SIZE_DPKEY";

    public PreserveNodeSizeLayoutStage(Layouter layouter) {
      super(layouter);
    }

    public boolean canLayout(LayoutGraph graph) {
      return canLayoutCore(graph);
    }

    public void doLayout(LayoutGraph graph) {
      // store old sizes and apply new sizes
      final HashMap oldSizes = new HashMap();
      final DataProvider dp = graph.getDataProvider(NODE_SIZE_DPKEY);
      if (dp != null) {
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
          final Node node = nc.node();
          final Object size = dp.get(node);
          if (size instanceof YDimension) {
            oldSizes.put(node, graph.getSize(node));
            final YPoint location = graph.getLocation(node);
            graph.setSize(node, (YDimension) size);
            graph.setLocation(node, location);
          }
        }
      }

      // do the actual layout
      doLayoutCore(graph);

      // restore the old sizes
      for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
        final Node node = nc.node();
        final YDimension size = (YDimension) oldSizes.get(node);
        if (size != null) {
          // the node gets closed
          // store source and target ports
          final EdgeMap sourcePorts = Maps.createHashedEdgeMap();
          final EdgeMap targetPorts = Maps.createHashedEdgeMap();
          for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
            final Edge edge = ec.edge();
            sourcePorts.set(edge, graph.getSourcePointAbs(edge));
            targetPorts.set(edge, graph.getTargetPointAbs(edge));
          }

          // change node size (ports are changed)
          final YPoint location = graph.getLocation(node);
          graph.setSize(node, size);
          graph.setLocation(node, location);

          // restore source and target ports
          for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
            final Edge edge = ec.edge();
            graph.setSourcePointAbs(edge, (YPoint) sourcePorts.get(edge));
            graph.setTargetPointAbs(edge, (YPoint) targetPorts.get(edge));
          }
        }
      }
    }
  }
}
