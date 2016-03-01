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
import y.base.Edge;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeList;
import y.layout.router.polyline.EdgeRouter;
import y.util.DataProviderAdapter;
import y.view.CreateEdgeMode;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.HitInfo;
import y.view.HitInfoFactory;
import y.view.HotSpotMode;
import y.view.MouseInputMode;
import y.view.MoveSelectionMode;
import y.view.NodeRealizer;
import y.view.ViewMode;
import y.view.YLabel;

import javax.swing.Timer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Custom {@link y.view.EditMode} that considers special requirements of the UML diagram:
 * <ul>
 *    <li>Multi selection of node labels should not be allowed.</li>
 *    <li>Edge creation is only possible using {@link UmlEdgeCreationButtons}.</li>
 *    <li>Editing elements of a node are only displayed if the mouse is located above it.</li>
 * </ul>
 */
class UmlEditMode extends EditMode {
  private static final String ZOOM_PROPERTY = "Zoom";

  private static final int DELAY = 500;

  private static final EdgeRealizer ASSOCIATION_REALIZER = UmlRealizerFactory.createAssociationRealizer();
  private static final EdgeRealizer DEPENDENCY_REALIZER = UmlRealizerFactory.createDependencyRealizer();
  private static final EdgeRealizer GENERALIZATION_REALIZER = UmlRealizerFactory.createGeneralizationRealizer();
  private static final EdgeRealizer REALIZATION_REALIZER = UmlRealizerFactory.createRealizationRealizer();
  private static final EdgeRealizer AGGREGATION_REALIZER = UmlRealizerFactory.createAggregationRealizer();
  private static final EdgeRealizer COMPOSITION_REALIZER = UmlRealizerFactory.createCompositionRealizer();

  private final Timer showingTimer;
  private final Timer hidingTimer;
  private final AnimationPlayer player;
  private Graph2DViewRepaintManager repaintManager;
  private UmlCreateEdgeMode umlCreateEdgeMode;

  private Node lastNode;
  private Node currentNode;
  private UmlEdgeCreationButtons lastButtons;
  private UmlEdgeCreationButtons currentButtons;
  private UmlEdgeCreationButtons rollingOutButtons;
  private UmlEdgeCreationButtons rollingInButtons;

  private boolean lastPressHitButton;

  /**
   * Creates an {@link EditMode} that is adjusted for the {@link UmlDemo}.
   *
   * This edit mode gets the view it will be registered to, so it can register listeners to it.
   * NOTE: To remain consistent, this edit mode must not be registered to another view afterwards.
   *
   * @param view the view this edit mode will be registered to.
   * @param edgeRouter the edge router that is used to reroute the edges after they got changed by a child mode.
   */
  public UmlEditMode(final Graph2DView view, EdgeRouter edgeRouter) {
    // initialize state variables
    lastNode = null;
    currentNode = null;
    lastButtons = null;
    currentButtons = null;
    lastPressHitButton = false;

    player = new AnimationPlayer(false);

    addListeners(view);
    addChildModes(edgeRouter);

    // Add a repaint manager to the current view to handle repainting effectively.
    repaintManager = new Graph2DViewRepaintManager(view);
    player.addAnimationListener(repaintManager);

    // add a timer that fires once when invoked and starts the animations for showing edge creation buttons and node
    // editing elements
    showingTimer = new Timer(0, null);
    showingTimer.setInitialDelay(DELAY);
    showingTimer.setRepeats(false);
    showingTimer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (lastButtons == null || lastButtons.getNode() != currentNode) {
          // edge creation buttons and node editing elements will be animated simultaneously
          final CompositeAnimationObject animations = AnimationFactory.createConcurrency();

          // only add roll-in animation if the mouse is above a node
          if (currentNode != null && paintDetailed(view.getZoom())) {
            currentButtons = new UmlEdgeCreationButtons(view, currentNode);
            animations.addAnimation(
                AnimationFactory.createEasedAnimation(new EditingElementsAnimation(currentButtons, true), 1, 1));
          }

          // execute animations
          player.animate(animations);

          // store current buttons so they can be rolled-in later
          lastButtons = currentButtons;
        }
      }
    });

    // add a timer that fires once when invoked and starts the animations for hiding edge creation buttons and node
    // editing elements
    hidingTimer = new Timer(0, null);
    hidingTimer.setInitialDelay(DELAY);
    hidingTimer.setRepeats(false);
    hidingTimer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (lastButtons == null || lastButtons.getNode() != currentNode) {
          // edge creation buttons and node editing elements will be animated simultaneously
          final CompositeAnimationObject animations = AnimationFactory.createConcurrency();

          // only add roll-out animation if there are old buttons from another node
          if (lastButtons != null) {
            animations.addAnimation(
                AnimationFactory.createEasedAnimation(new EditingElementsAnimation(lastButtons, false), 1, 1));
          }

          // execute animations
          player.animate(animations);

          // old buttons are gone
          lastButtons = null;
        }
      }
    });
  }

  /**
   * Determines whether or not the graph will be painted with details or sloppy at the given zoom-level.
   */
  private boolean paintDetailed(double zoom) {
    return zoom > view.getPaintDetailThreshold();
  }

  private void addListeners(final Graph2DView view) {
    // Add graph listener to the current graph to delete the button drawable on node deletion.
    view.getGraph2D().addGraphListener(createNodeDeletionListener());

    // add a property change listener that observes zooming
    // if zoom is below the view's threshold already existing buttons are rolled in and no new buttons are shown
    view.getCanvasComponent().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (ZOOM_PROPERTY.equals(evt.getPropertyName())) {
          final double zoom = ((Double) evt.getNewValue()).doubleValue();
          if (!paintDetailed(zoom)) {
            // no buttons when sloppy painting
            currentNode = null;
            currentButtons = null;
            if (lastNode != currentNode) {
              hidingTimer.setInitialDelay(0);
              hidingTimer.restart();
              showingTimer.stop();
              lastNode = currentNode;
            }
          } else {
            // no sloppy painting, so maybe show edge creation buttons
            // get last mouse motion event to check if it is necessary to schedule an animation for this mouse position
            // because zooming does not invoke mouse events and the zoom level might just have risen above paint detail
            // level
            MouseEvent lastMouseMotionEvent = getLastMoveEvent();
            if (lastMouseMotionEvent == null) {
              lastMouseMotionEvent = getLastDragEvent();
            } else if (getLastDragEvent() != null && lastMouseMotionEvent.getWhen() < getLastDragEvent().getWhen()) {
              lastMouseMotionEvent = getLastDragEvent();
            }

            if (lastMouseMotionEvent != null) {
              scheduleButtonAnimations(view.toWorldCoordX(lastMouseMotionEvent.getX()),
                  view.toWorldCoordY(lastMouseMotionEvent.getY()));
            }
          }
        }
      }
    });
  }

  /**
   * Adds child modes to this editor that invoke rerouting of changed edges after they are executed.
   *
   * @param edgeRouter the edge router for the rerouting.
   */
  private void addChildModes(EdgeRouter edgeRouter) {
    setMoveSelectionMode(new ReroutingMoveSelectionMode(edgeRouter));
    final ReroutingCreateEdgeMode createEdgeMode = new ReroutingCreateEdgeMode(edgeRouter);
    createEdgeMode.setIndicatingTargetNode(true);
    setCreateEdgeMode(createEdgeMode);
    setHotSpotMode(new ReroutingHotSpotMode(edgeRouter));
    setMoveLabelMode(null);
    umlCreateEdgeMode = new UmlCreateEdgeMode(edgeRouter);
  }

  /**
   * Returns a {@link GraphListener} that removes the button drawable of a just deleted node.
   *
   * @return a <code>GraphListener</code> that removes the button drawable of a just deleted node.
   */
  private GraphListener createNodeDeletionListener() {
    return new GraphListener() {
      public void onGraphEvent(GraphEvent event) {
        // if there are still edge creation buttons visible at the removed node, remove them
        if (lastButtons != null) {
          if ((event.getType()) == GraphEvent.POST_NODE_REMOVAL
              && event.getData().equals(lastButtons.getNode())) {
            view.removeDrawable(lastButtons);
            lastButtons = null;
            view.updateView();
          }
        }

        // if there are edge creation buttons that roll out at the moment, remove them
        if (rollingOutButtons != null) {
          if ((event.getType()) == GraphEvent.POST_NODE_REMOVAL
              && event.getData().equals(rollingOutButtons.getNode())) {
            view.removeDrawable(rollingOutButtons);
            rollingOutButtons = null;
            view.updateView();
          }
        }

        // if there are edge creation buttons that roll out at the moment, remove them
        if (rollingInButtons != null) {
          if ((event.getType()) == GraphEvent.POST_NODE_REMOVAL
              && event.getData().equals(rollingInButtons.getNode())) {
            view.removeDrawable(rollingInButtons);
            rollingInButtons = null;
            view.updateView();
          }
        }
      }
    };
  }

  /**
   * Overwritten to additionally set snapping for the {@link UmlCreateEdgeMode}.
   *
   * @param snapping Whether to enable snapping.
   */
  public void setSnappingEnabled(boolean snapping) {
    super.setSnappingEnabled(snapping);

    umlCreateEdgeMode.setSnappingEnabled(snapping);
    umlCreateEdgeMode.getSnapContext().setUsingOrthogonalMovementConstraints(snapping);
    umlCreateEdgeMode.getSnapContext().setSnappingSegmentsToSnapLines(snapping);
  }

  /**
   * Prevents multi selection of labels.
   *
   * @param graph       the graph the label's associated node or edge resides in.
   * @param label       the label which has been clicked
   * @param wasSelected whether the element is already selected
   * @param x           the x-coordinate where the mouse was clicked
   * @param y           the y-coordinate where the mouse was clicked
   * @param modifierSet <code>true</code> if the caller is {@link #mouseShiftReleasedLeft(double, double)},
   */
  protected void labelClicked(
      final Graph2D graph,
      final YLabel label,
      final boolean wasSelected,
      final double x,
      final double y,
      final boolean modifierSet
  ) {
    // To avoid multi selection the modifier flag is always set to false.
    super.labelClicked(graph, label, wasSelected, x, y, false);
  }

  /**
   * Overwritten to avoid hiding edge creation buttons on right-click on their node. Now, on this particular node it is
   * not possible to initiate movement of the whole view port.
   */
  public void mousePressedRight(double x, double y) {
    final HitInfo hit = getHitInfo(x, y);
    final boolean isOnNodeWithButtons = (lastButtons != null) && (hit.getHitNode() == lastButtons.getNode());
    final boolean isOnButtons = (lastButtons != null) && (lastButtons.hasButtonAt(x, y));
    if (!isOnNodeWithButtons && !isOnButtons) {
      super.mousePressedRight(x, y);
    }
  }

  /**
   * Overwritten to adjust the default edge realizer when an edge creation button is hit by the current coordinates.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mousePressedLeft(final double x, final double y) {
    lastPressHitButton = false;

    if (hitsEdgeCreationButton(x, y)) {
      lastPressHitButton = true;
      lastButtons.selectButtonAt(x, y);
      final int selectedButtonIndex = lastButtons.getSelectedButtonIndex();
      switch (selectedButtonIndex) {
        case UmlEdgeCreationButtons.TYPE_ASSOCIATION:
          getGraph2D().setDefaultEdgeRealizer(ASSOCIATION_REALIZER);
          break;
        case UmlEdgeCreationButtons.TYPE_DEPENDENCY:
          getGraph2D().setDefaultEdgeRealizer(DEPENDENCY_REALIZER);
          break;
        case UmlEdgeCreationButtons.TYPE_GENERALIZATION:
          getGraph2D().setDefaultEdgeRealizer(GENERALIZATION_REALIZER);
          break;
        case UmlEdgeCreationButtons.TYPE_REALIZATION:
          getGraph2D().setDefaultEdgeRealizer(REALIZATION_REALIZER);
          break;
        case UmlEdgeCreationButtons.TYPE_AGGREGATION:
          getGraph2D().setDefaultEdgeRealizer(AGGREGATION_REALIZER);
          break;
        case UmlEdgeCreationButtons.TYPE_COMPOSITION:
          getGraph2D().setDefaultEdgeRealizer(COMPOSITION_REALIZER);
          break;
      }
    }

    super.mousePressedLeft(x, y);
  }

  /**
   * Overwritten to start {@link y.view.CreateEdgeMode} in case the last press event was on an edge creation button.
   * <code>CreateEdgeMode</code> will start the edge at the node associated with the button.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   */
  public void mouseReleasedLeft(final double x, final double y) {
    final Graph2D graph = getGraph2D();
    if (lastPressHitButton) {
      startCreateEdgeMode(graph);
    } else {
      super.mouseReleasedLeft(x, y);
    }
  }

  /**
   * Overwritten to show/hide the edge creation buttons when moving over a node and to push the node at the given
   * coordinates to the front.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   */
  public void mouseMoved(final double x, final double y) {
    final Graph2D graph = getGraph2D();
    if (hitsButtonsDrawable(lastButtons, x, y)) {
      // move/keep the node that belongs to the hit buttons to the front
      graph.moveToLast(lastButtons.getNode());
    } else {
      final HitInfoFactory hitInfoFactory = view.getHitInfoFactory();
      final HitInfo hit = hitInfoFactory.createHitInfo(x, y, Graph2DTraversal.NODES, true);
      if (hit.hasHitNodes()) {
        // move the hit node to the front
        final Node node = hit.getHitNode();
        graph.moveToLast(node);
      }
    }

    // show/hide edge creation buttons
    scheduleButtonAnimations(x, y);

    // continue with default behavior
    super.mouseMoved(x, y);
  }

  /**
   * Overwritten to create an edge with its target node in case the drag started in an edge creation button.
   * The edge will start at the node associated with the button.
   *
   * @param x current x-coordinate of the mouse in world coordinates.
   * @param y current y-coordinate of the mouse in world coordinates.
   */
  public void mouseDraggedLeft(final double x, final double y) {
    scheduleButtonAnimations(x, y);

    final Graph2D graph = getGraph2D();
    if (lastPressHitButton) {
      // update create edge mode to have the right source node
      umlCreateEdgeMode.setSourceNode(lastButtons.getNode());
      // when the drag started at an edge creation button, use UmlCreateEdgeMode to create a new edge
      setChild(umlCreateEdgeMode, lastPressEvent, lastDragEvent);
    } else {
      final HitInfo hit = getHitInfo(lastPressEvent);
      // prevent edge creation other than from the edge creation buttons
      if (!hit.hasHitNodes() || graph.isSelected(hit.getHitNode())) {
        // drag started neither at an edge creation button nor on a non-selected node, use default behavior
        super.mouseDraggedLeft(x, y);
      }
    }
  }

  /**
   * Schedules animations for showing and hiding of the edge creation buttons considering the given coordinates.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   */
  private void scheduleButtonAnimations(final double x, final double y) {
    if (hitsButtonsDrawable(lastButtons, x, y)) {
      // update button selection
      updateButtonSelection(x, y);

      currentNode = lastButtons.getNode();
    } else if (paintDetailed(view.getZoom())) {
      final HitInfo hitInfo = view.getHitInfoFactory().createHitInfo(x, y, Graph2DTraversal.NODES, true);
      currentNode = hitInfo.getHitNode();
    }

    // node (+buttons) is entered or left, respectively
    if (lastNode != currentNode) {
      currentButtons = currentNode != null ? new UmlEdgeCreationButtons(view, currentNode) : null;
      lastNode = currentNode;
      hidingTimer.setInitialDelay(DELAY);
      hidingTimer.start();
      showingTimer.setInitialDelay(DELAY);
      showingTimer.restart();
    }
  }

  /**
   * Selects the button at the given position and updates the view if the selection has changed.
   */
  private void updateButtonSelection(double x, double y) {
    int lastIndex = lastButtons.getSelectedButtonIndex();
    lastButtons.selectButtonAt(x, y);
    int currentIndex = lastButtons.getSelectedButtonIndex();
    if (currentIndex != lastIndex) {
      view.updateView();
    }
  }

  /**
   * Starts {@link y.view.CreateEdgeMode} with an adjusted start event that is located on the current node. That way,
   * the edge will be created although the last press event was outside the node.
   */
  private void startCreateEdgeMode(final Graph2D graph) {
    if (lastButtons != null) {
      // fake last drag event because the current last drag event occurred before the last press event and is not
      // associated with this release event
      lastDragEvent = new MouseEvent((Component) lastPressEvent.getSource(), MouseEvent.MOUSE_DRAGGED,
          lastPressEvent.getWhen(), lastPressEvent.getModifiers(), lastPressEvent.getX(), lastPressEvent.getY(),
          lastPressEvent.getClickCount(), lastPressEvent.isPopupTrigger());

      // drag started at an edge creation button, create a press event with manipulated coordinates that lie on the node.
      // note: the coordinates have to be translated in view coordinates
      final Node associatedNode = lastButtons.getNode();
      final MouseEvent pressEvent = new MouseEvent((Component) lastPressEvent.getSource(), lastPressEvent.getID(),
          lastPressEvent.getWhen(), lastPressEvent.getModifiers(),
          view.toViewCoordX(graph.getCenterX(associatedNode)),
          view.toViewCoordY(graph.getCenterY(associatedNode)),
          lastPressEvent.getClickCount(), lastPressEvent.isPopupTrigger());

      // start create edge mode
      setChild(getCreateEdgeMode(), pressEvent, lastDragEvent);
    }
  }

  /**
   * Overwritten to update the edge creation buttons after a node was created.
   */
  protected void nodeCreated(Node node) {
    Graph2D graph = getGraph2D();
    scheduleButtonAnimations(graph.getCenterX(node), graph.getCenterY(node));
  }

  /**
   * Overwritten to hide the current edge creation buttons when a child mode gets active.
   */
  public void setChild(final ViewMode child,
                       final MouseEvent pressEvent, final MouseEvent dragEvent, final MouseEvent releaseEvent) {
    if (child != null && !(child instanceof MouseInputMode)) {
      // remove current buttons drawable when starting a child mode except the MouseInputMode because it handles events
      // from the buttons on the node (i.e. from inside the node's bounds) and the edge creation buttons shouldn't change
      currentNode = null;
      currentButtons = null;
      if (lastNode != currentNode) {
        hidingTimer.setInitialDelay(0);
        hidingTimer.restart();
        showingTimer.stop();
        lastNode = currentNode;
      }
    }

    super.setChild(child, pressEvent, dragEvent, releaseEvent);
  }

  /**
   * Checks if an edge creation button was hit by the given coordinates.
   *
   * @param x x-coordinate in world coordinates.
   * @param y y-coordinate in world coordinates.
   *
   * @return <code>true</code> if an edge creation button was hit by the given coordinates, <code>false</code>
   *         otherwise.
   */
  private boolean hitsEdgeCreationButton(final double x, final double y) {
    return lastButtons != null && lastButtons.hasButtonAt(x, y);
  }

  /**
   * Checks if the given union of the edge creation buttons and the area between them contains the passed coordinates.
   *
   * @return <code>true</code> if the given buttons drawable meets the passed coordinates, <code>false</code>
   *         otherwise.
   */
  private boolean hitsButtonsDrawable(final UmlEdgeCreationButtons buttons, final double x, final double y) {
    return buttons != null && buttons.contains(x, y);
  }

  /**
   * An {@link y.anim.AnimationObject} that adjusts the {@link UmlEdgeCreationButtons} time step so they either roll in
   * or roll out and the node editing elements (add item/remove item) so they either fade in or fade out.
   */
  private class EditingElementsAnimation implements AnimationObject {
    private static final int MOVE_DURATION = 300;

    private final UmlEdgeCreationButtons drawable;
    private final boolean in;
    private double progress;

    private final NodeRealizer realizer;
    private double attributeButtonOpacity;
    private double operationButtonOpacity;
    private double selectionOpacity;


    EditingElementsAnimation(final UmlEdgeCreationButtons drawable, final boolean in) {
      this.drawable = drawable;
      this.in = in;
      this.realizer = getGraph2D().getRealizer(drawable.getNode());
    }

    public void initAnimation() {
      if (repaintManager != null) {
        repaintManager.add(drawable);
        repaintManager.add(realizer);
      }

      if (in) {
        rollingInButtons = drawable;
        view.addDrawable(drawable);
        drawable.setProgress(0);

        UmlRealizerFactory.setAttributeButtonOpacity(realizer, 0);
        UmlRealizerFactory.setOperationButtonOpacity(realizer, 0);
        UmlRealizerFactory.setSelectionOpacity(realizer, 0);
      } else {
        rollingOutButtons = drawable;
        progress = drawable.getProgress();

        attributeButtonOpacity = UmlRealizerFactory.getAttributeButtonOpacity(realizer);
        operationButtonOpacity = UmlRealizerFactory.getOperationButtonOpacity(realizer);
        selectionOpacity = UmlRealizerFactory.getSelectionOpacity(realizer);
      }
    }

    public void calcFrame(final double time) {
      if (in) {
        drawable.setProgress(time);

        UmlRealizerFactory.setAttributeButtonOpacity(realizer, (float) time);
        UmlRealizerFactory.setOperationButtonOpacity(realizer, (float) time);
        UmlRealizerFactory.setSelectionOpacity(realizer, (float) time);
      } else {
        drawable.setProgress(progress * (1 - time));

        UmlRealizerFactory.setAttributeButtonOpacity(realizer, (float) (attributeButtonOpacity * (1 - time)));
        UmlRealizerFactory.setOperationButtonOpacity(realizer, (float) (operationButtonOpacity * (1 - time)));
        UmlRealizerFactory.setSelectionOpacity(realizer, (float) (selectionOpacity * (1 - time)));
      }
    }

    public void disposeAnimation() {
      if (repaintManager != null) {
        repaintManager.remove(drawable);
        repaintManager.remove(realizer);
      }
      if (in) {
        rollingInButtons = null;
      } else {
        view.removeDrawable(drawable);
        rollingOutButtons = null;
      }
    }

    public long preferredDuration() {
      if (in) {
        return MOVE_DURATION;
      } else {
        if (progress > 0) {
          return (long) (MOVE_DURATION * progress);
        } else {
          return MOVE_DURATION;
        }
      }
    }
  }

  /**
   * A customized {@link y.view.MoveSelectionMode} that invokes edge routing after a set of nodes was moved.
   */
  private static class ReroutingMoveSelectionMode extends MoveSelectionMode {
    private final EdgeRouter edgeRouter;

    public ReroutingMoveSelectionMode(EdgeRouter edgeRouter) {
      this.edgeRouter = edgeRouter;
    }

    public void mouseShiftPressedLeft(final double x, final double y) {
      getGraph2D().firePreEvent();
      super.mouseShiftPressedLeft(x, y);
    }

    public void mousePressedLeft(final double x, final double y) {
      getGraph2D().firePreEvent();
      super.mousePressedLeft(x, y);
    }

    public void mouseShiftReleasedLeft(final double x, final double y) {
      super.mouseShiftReleasedLeft(x, y);
      routeEdgesAtMovedNodes();
      getGraph2D().firePostEvent();
    }

    public void mouseReleasedLeft(final double x, final double y) {
      super.mouseReleasedLeft(x, y);
      routeEdgesAtMovedNodes();
      getGraph2D().firePostEvent();
    }

    private void routeEdgesAtMovedNodes() {
      final Graph2D graph = getGraph2D();
      final NodeList nodesToBeMoved = getNodesToBeMoved();
      final DataProviderAdapter selectedNodes = new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return nodesToBeMoved.contains(dataHolder);
        }
      };
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
      graph.addDataProvider(EdgeRouter.SELECTED_NODES, selectedNodes);
      try {
        final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        executor.getLayoutMorpher().setKeepZoomFactor(true);
        executor.doLayout(view, edgeRouter);
      } finally {
        graph.removeDataProvider(EdgeRouter.SELECTED_NODES);
      }
    }
  }

  /**
   * A customized {@link y.view.CreateEdgeMode} that invokes edge routing after an edge was created.
   */
  private static class ReroutingCreateEdgeMode extends CreateEdgeMode {
    private final EdgeRouter edgeRouter;

    public ReroutingCreateEdgeMode(EdgeRouter edgeRouter) {
      this.edgeRouter = edgeRouter;
    }

    protected Edge createEdge(final Graph2D graph, final Node startNode, final Node targetNode,
                              final EdgeRealizer realizer) {
      getGraph2D().firePreEvent();
      return super.createEdge(graph, startNode, targetNode, realizer);
    }

    protected void edgeCreated(final Edge edge) {
      super.edgeCreated(edge);

      final Graph2D graph = getGraph2D();
      final DataProviderAdapter selectedEdges = new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return dataHolder == edge;
        }
      };
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      graph.addDataProvider(EdgeRouter.SELECTED_EDGES, selectedEdges);
      try {
        final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        executor.getLayoutMorpher().setKeepZoomFactor(true);
        executor.doLayout(view, edgeRouter);
      } finally {
        graph.removeDataProvider(EdgeRouter.SELECTED_EDGES);
      }
      graph.firePostEvent();
    }
  }

  /**
   * A customized {@link y.view.HotSpotMode} that invokes edge routing after a node was resized.
   */
  private static final class ReroutingHotSpotMode extends HotSpotMode {
    private final EdgeRouter edgeRouter;

    public ReroutingHotSpotMode(EdgeRouter edgeRouter) {
      this.edgeRouter = edgeRouter;
    }

    public void mousePressedLeft(double x, double y) {
      getGraph2D().firePreEvent();
      super.mousePressedLeft(x, y);
    }

    public void mouseReleasedLeft(double x, double y) {
      super.mouseReleasedLeft(x, y);
      edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_EDGES_AT_SELECTED_NODES);
      getGraph2D().addDataProvider(EdgeRouter.SELECTED_NODES, new DataProviderAdapter() {
        public boolean getBool(Object dataHolder) {
          return dataHolder instanceof Node && getGraph2D().isSelected((Node) dataHolder);
        }
      });
      try {
        final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        executor.getLayoutMorpher().setKeepZoomFactor(true);
        executor.doLayout(view, edgeRouter);
      } finally {
        getGraph2D().firePostEvent();
        edgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      }
    }
  }
}
