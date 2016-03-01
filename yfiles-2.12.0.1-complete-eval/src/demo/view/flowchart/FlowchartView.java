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
package demo.view.flowchart;

import y.base.Edge;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.PopupMode;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2DViewActions;
import y.view.AutoDragViewMode;
import y.view.MovePortMode;
import y.base.Node;
import y.view.TooltipMode;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

/**
 * Component that visualizes Flowchart diagrams.
 */
public class FlowchartView extends Graph2DView {
  private Graph2DViewActions flowchartActions;

  /**
   * Creates a new Graph2DView containing an empty graph and register Flowchart specific view modes and actions
   * The constructor calls the following methods, in oder to register respective view modes and actions:
   * <ul>
   * <li> {@link #registerViewModes()}</li>
   * <li> {@link #registerViewActions()} </li>
   * <li> {@link #registerViewListeners()}</li>
   * </ul>
   */
  public FlowchartView() {
    super();
    //Some default behaviour
    this.setFitContentOnResize(true);

    //init
    registerViewModes();
    registerViewActions();
    registerViewListeners();
  }

  /**
   * Callback method, which registers Flowchart specific view actions.
   */
  protected void registerViewActions() {
    flowchartActions = new Graph2DViewActions(this);
    flowchartActions.install();
  }

  /**
   * Callback method, which registers Flowchart specific view modes and configures them.
   */
  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    // Route all edges orthogonally.
    editMode.setOrthogonalEdgeRouting(true);

    CreateEdgeMode createEdgeMode = (CreateEdgeMode) editMode.getCreateEdgeMode();
    createEdgeMode.setOrthogonalEdgeCreation(true);
    createEdgeMode.setIndicatingTargetNode(true);
    editMode.setSnappingEnabled(true);

    //add hierarchy actions to the views popup menu
    editMode.setPopupMode(new FlowchartPopupMode());
    editMode.getMouseInputMode().setNodeSearchingEnabled(true);
    editMode.assignNodeLabel(false);

    ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);

    //add view mode to display tooltips for node
    TooltipMode tooltipMode = new TooltipMode();
    tooltipMode.setEdgeTipEnabled(false);
    addViewMode(tooltipMode);

    //allow moving view port with right drag gesture
    editMode.allowMovingWithPopup(true);
    addViewMode(editMode);

    //Auto drag mode
    addViewMode(new AutoDragViewMode());
  }

  /**
   * Callback method, which registers  specific listeners and configures them.
   */
  protected void registerViewListeners() {
    Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
    //zoom in/out at mouse pointer location
    wheelZoomListener.setCenterZooming(false);
    wheelZoomListener.addToCanvas(this);
  }


  //////////////////////////////////////////////////////////////////////////////
  // VIEW MODES ////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * provides the context sensitive popup menus
   */
  private class FlowchartPopupMode extends PopupMode {

    public JPopupMenu getNodePopup(Node v) {
      JPopupMenu pm = new JPopupMenu();
      if (v != null) {
        JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
        deleteItem.setText("Delete Node");
      }
      return pm;
    }

    public JPopupMenu getEdgePopup(Edge e) {
      JPopupMenu pm = new JPopupMenu();
      if (e != null) {
        JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
        deleteItem.setText("Delete Edge");
      }
      return pm;
    }

    public JPopupMenu getSelectionPopup(double x, double y) {
      JPopupMenu pm = new JPopupMenu();
      JMenuItem deleteItem = pm.add(flowchartActions.getDeleteSelectionAction());
      deleteItem.setText("Delete Selection");
      return pm;
    }
  }
}
