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
package tutorial.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import y.view.Graph2DView;

public class SimpleGraphViewer1 {
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;

  public SimpleGraphViewer1(Dimension size, String title) {
    view = createGraph2DView();
    frame = createApplicationFrame(size, title, view);
  }

  public SimpleGraphViewer1() {
    this(new Dimension(400, 300), "");
    frame.setTitle(getClass().getName());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView();
    return view;
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(size);
    // Add the given view to the panel.
    panel.add(view, BorderLayout.CENTER);
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getRootPane().setContentPane(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  public void show() {
    frame.setVisible(true);
  }

  public Graph2DView getView() {
    return view;
  }

  public static void main(String[] args) {
    SimpleGraphViewer1 sgv = 
      new SimpleGraphViewer1(new Dimension(400, 200), SimpleGraphViewer1.class.getName());
    sgv.show();
  }
}
