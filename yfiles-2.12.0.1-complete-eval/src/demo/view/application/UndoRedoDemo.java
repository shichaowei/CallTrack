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
import y.view.Graph2DUndoManager;

import javax.swing.JToolBar;
import javax.swing.Action;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * This Demo shows how to use Undo/Redo functionality built into yFiles.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/advanced_stuff.html#undo_redo">Section Undo/Redo</a> in the yFiles for Java Developer's Guide
 */
public class UndoRedoDemo extends DemoBase
{
  private Graph2DUndoManager undoManager;

  /**
   * Returns the undo manager for this application. Also, if not already done - it creates 
   * and configures it.
   */
  protected Graph2DUndoManager getUndoManager()
  {
    if(undoManager == null)
    {
      //create one and make it listen to graph structure changes
      undoManager = new Graph2DUndoManager(view.getGraph2D());

      //assign the graph view as view container so we get view updates
      //after undo/redo actions have been performed. 
      undoManager.setViewContainer(view);
    }
    return undoManager;
  }


  public JToolBar createToolBar()
  {
    JToolBar bar = super.createToolBar();

    bar.addSeparator();
    
    //add undo action to toolbar
    Action action = getUndoManager().getUndoAction();
    action.putValue(Action.SMALL_ICON, getIconResource("resource/undo.png"));
    action.putValue(Action.SHORT_DESCRIPTION, "Undo");
    bar.add(action);

    //add redo action to toolbar
    action = getUndoManager().getRedoAction();
    action.putValue(Action.SMALL_ICON, getIconResource("resource/redo.png"));
    action.putValue(Action.SHORT_DESCRIPTION, "Redo");
    bar.add(action);
    return bar;
  }

  /**
   * Overwritten to disable the default undo/redo support.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new UndoRedoDemo()).start("Undo/Redo Demo");
      }
    });
  }

}

    

      
