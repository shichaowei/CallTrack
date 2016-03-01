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
package demo.layout.organic;

import demo.view.DemoBase;
import y.layout.CopiedLayoutGraph;
import y.layout.LayoutTool;
import y.layout.organic.InteractiveOrganicLayouter;
import y.view.EditMode;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.Timer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;

/**
 * This demo shows the very basic usage of the
 *  {@link y.layout.organic.InteractiveOrganicLayouter}.
 * The layouter is started within a thread. A swing timer is used to update the
 * positions of the nodes.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/interactive_organic_layouter.html#interactive_organic_layouter">Section Interactive Organic Layout</a> in the yFiles for Java Developer's Guide
 */
public class InteractiveOrganicDemo extends DemoBase {
  private InteractiveOrganicLayouter layouter;

  protected void initialize() {
    layouter = new InteractiveOrganicLayouter();
    layouter.setAutomaticStructureUpdateEnabled(true);
    layouter.enableOnlyCore();

    loadGraph("resource/peopleNav.graphml");
    //Reset the paths and the locations of the nodes.
    LayoutTool.initDiagram(view.getGraph2D());

    view.setPaintDetailThreshold(0.0);
    view.fitContent();
    
  }

  /**
   * Callback used by {@link #registerViewModes()} to create the default EditMode
   * @return an instance of {@link y.view.EditMode} with showNodeTips enabled
   */
  protected EditMode createEditMode() {
    EditMode editMode = super.createEditMode();
    editMode.allowBendCreation(false);
    editMode.allowNodeCreation(false);
    editMode.allowResizeNodes(false);
    editMode.allowEdgeCreation(false);

    //This view mode offers support for "touching the graph"
    editMode.setMoveSelectionMode(new InteractiveMoveSelectionMode(layouter));

    return editMode;
  }

  protected JToolBar createToolBar() {
    final JButton startButton = new JButton("Start", SHARED_LAYOUT_ICON);
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //Disable the button
        startButton.setEnabled(false);

        //Start the layout thread
        layouter.startLayout(new CopiedLayoutGraph(view.getGraph2D()));

        //Update timer
        Timer timer = new Timer(21, new ActionListener() {
          //This listener is notified about 24 times a second.
          public void actionPerformed(ActionEvent e) {
            //Write the calculated positions back to the realizers
            if (layouter.commitPositionsSmoothly(50, 0.15) > 0) {
              //... and update the view, if something has changed
              view.updateView();
            }
          }
        });
        timer.setInitialDelay(500);
        timer.start();
      }
    });

    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(startButton);
    return toolBar;
  }

  /**
   * Overwritten to disable undo/redo because this is not an editable demo.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Overwritten to disable clipboard because this is not an editable demo.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  public static void main(String[] args) throws IOException {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        InteractiveOrganicDemo demo = new InteractiveOrganicDemo();
        demo.start();
      }
    });

  }
}
