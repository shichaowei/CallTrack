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
import demo.view.DemoDefaults;

import y.view.EditMode;
import y.view.MagnifierViewMode;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * Demonstrates how to use a magnifying glass effect to zoom view regions locally.
 * <p>
 * Usage: To activate the magnifier select the "Use Magnifier" button. Move the mouse over the
 * view canvas to move the magnifier. Note that you can even edit the graph while the magnifier is active.
 * Use the mouse wheel to change the zoom factor of the magnifier. To change the radius of the magnifier
 * with the mouse wheel, additionally keep the CTRL key pressed. To deactivate the magnifier again, deselect
 * the "Use Magnifier" button.
 * </p>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#cls_MagnifierViewMode">Section User Interaction</a> in the yFiles for Java Developer's Guide
 */
public class MagnifierViewModeDemo extends DemoBase
{
  MagnifierViewMode magnifierMode;
  JToggleButton magnifierButton;

  public MagnifierViewModeDemo() {
    magnifierButton.doClick();
  }

  protected void initialize() {
    super.initialize();

//    // turn off the magnifier's double-buffering
//    view.putClientProperty("MagnifierViewMode.noDoubleBuffering", Boolean.TRUE);

    magnifierMode = new MagnifierViewMode();
    magnifierMode.setMagnifierRadius(100);
    magnifierMode.setMagnifierZoomFactor(2.0);

    loadGraph(getClass(), "resource/5.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);

  }

  protected JToolBar createToolBar() {
    magnifierButton = new JToggleButton(new AbstractAction("Magnifier") {
      public void actionPerformed(ActionEvent e) {
        if (magnifierButton.isSelected()) {
          view.addViewMode(magnifierMode);
        } else {
          view.removeViewMode(magnifierMode);
        }
      }
    });
    magnifierButton.setIcon(getIconResource("resource/magnifier.png"));

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(magnifierButton);
    return toolBar;
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode(editMode);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new MagnifierViewModeDemo()).start();
      }
    });
  }
}
