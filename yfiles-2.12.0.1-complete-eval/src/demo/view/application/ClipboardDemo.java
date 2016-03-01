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
package demo.view.application;

import demo.view.DemoBase;
import y.view.Graph2DClipboard;

import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * This class demonstrates how to use the yFiles clipboard
 * functionality to cut, copy and paste parts of a graph.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/advanced_stuff.html">Section Advanced Application Logic</a> in the yFiles for Java Developer's Guide
 */
public class ClipboardDemo extends DemoBase
{
  Action cutAction;
  Action copyAction;
  Action pasteAction;

  public ClipboardDemo()
  {
    view.getCanvasComponent().getActionMap().put("CUT", cutAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),"CUT");

    view.getCanvasComponent().getActionMap().put("COPY", copyAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "COPY");

    view.getCanvasComponent().getActionMap().put("PASTE", pasteAction);
    view.getCanvasComponent().getInputMap().put(
        KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), "PASTE");

    loadInitialGraph();
  }

  protected void loadInitialGraph() {
    loadGraph("resource/ClipboardDemo.graphml");
  }

  protected void registerViewActions() {
    super.registerViewActions();
    //create new clipboard.
    Graph2DClipboard clipboard = new Graph2DClipboard(view);

    //get Cut action from clipboard
    cutAction = clipboard.getCutAction();
    cutAction.putValue(Action.SMALL_ICON, getIconResource("resource/cut.png"));
    cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut");

    //get Copy action from clipboard
    copyAction = clipboard.getCopyAction();
    copyAction.putValue(Action.SMALL_ICON, getIconResource("resource/copy.png"));
    copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy");

    //get Paste action from clipboard
    pasteAction = clipboard.getPasteAction();
    pasteAction.putValue(Action.SMALL_ICON, getIconResource("resource/paste.png"));
    pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste");
  }

  protected JToolBar createToolBar() {
    JToolBar jtb = super.createToolBar();
    jtb.addSeparator();
    jtb.add(cutAction);
    jtb.add(copyAction);
    jtb.add(pasteAction);
    return jtb;
  }

  /**
   * Overwritten to disable the default clipboard.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new ClipboardDemo().start();
      }
    });
  }
}
