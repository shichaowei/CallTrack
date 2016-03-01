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
package demo.view.viewmode;

import demo.view.DemoBase;
import y.base.Edge;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.LineType;
import y.view.ViewMode;
import y.util.DataProviderAdapter;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * Demonstrates how to customize {@link EditMode} to create and edit orthogonal edges.
 * <br>
 * This demo supports switching between the creation of orthogonal and polygonal edges.
 * Additionally, the demo shows the usage of the snapping feature of the various {@link ViewMode}s.
 * Toggling the buttons in the toolbar switches the type of newly created edges or toggle snapping on and off.
 * This affects the behavior of {@link CreateEdgeMode} and {@link EditMode}, as well as implicitly the minor modes
 * of {@link EditMode}.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#orthogonal_edge_paths">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class OrthogonalEdgeViewModeDemo extends DemoBase {

  private boolean orthogonalRouting;
  private boolean usingSnapping;
  private EditMode editMode;
  private JToggleButton orthogonalButton;
  private JToggleButton snapLineButton;
  private SnappingConfiguration snappingConfiguration;

  public OrthogonalEdgeViewModeDemo() {
    snappingConfiguration = DemoBase.createDefaultSnappingConfiguration();
    snappingConfiguration.setSnappingEnabled(true);
    snappingConfiguration.setRemovingInnerBends(true);
    snappingConfiguration.configureView(view);
    snappingConfiguration.configureEditMode(editMode);

    setOrthogonalRouting(true);
    setUsingSnapping(true);
    loadGraph("resource/orthogonalEdge.graphml");
  }

  protected JToolBar createToolBar() {
    orthogonalButton = new JToggleButton(new AbstractAction("Orthogonal") {
      public void actionPerformed(ActionEvent e) {
        setOrthogonalRouting(((AbstractButton) e.getSource()).isSelected());
      }
    });
    orthogonalButton.setIcon(getIconResource("resource/mode_orthogonal.png"));

    snapLineButton = new JToggleButton(new AbstractAction("Snapping") {
      public void actionPerformed(ActionEvent e) {
        setUsingSnapping(((AbstractButton) e.getSource()).isSelected());
      }
    });
    snapLineButton.setIcon(getIconResource("resource/mode_snapping.png"));

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(orthogonalButton);
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(snapLineButton);
    return toolBar;
  }

  protected EditMode createEditMode() {
    editMode = super.createEditMode();

    // Route all red edges orthogonally.
    view.getGraph2D().addDataProvider(EditMode.ORTHOGONAL_ROUTING_DPKEY, new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        return view.getGraph2D().getRealizer((Edge) dataHolder).getLineColor() == Color.RED;
      }
    });

    return editMode;
  }

  public boolean isOrthogonalRouting() {
    return orthogonalRouting;
  }

  public void setOrthogonalRouting(boolean orthogonalRouting) {
    this.orthogonalRouting = orthogonalRouting;
    this.orthogonalButton.setSelected(orthogonalRouting);
    ((CreateEdgeMode) editMode.getCreateEdgeMode()).setOrthogonalEdgeCreation(orthogonalRouting);
    view.getGraph2D().getDefaultEdgeRealizer().setLineColor(orthogonalRouting ? Color.RED : Color.BLACK);
    view.getGraph2D().getDefaultEdgeRealizer().setLineType(orthogonalRouting ? LineType.LINE_2 : LineType.LINE_1);
  }

  public boolean isUsingSnapping() {
    return usingSnapping;
  }

  public void setUsingSnapping(boolean usingSnapping) {
    this.usingSnapping = usingSnapping;
    this.snapLineButton.setSelected(usingSnapping);
    snappingConfiguration.setSnappingEnabled(usingSnapping);
    snappingConfiguration.setRemovingInnerBends(usingSnapping);
    snappingConfiguration.configureView(view);
    snappingConfiguration.configureEditMode(editMode);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new OrthogonalEdgeViewModeDemo()).start("Orthogonal Edge ViewMode Demo");
      }
    });
  }
}
