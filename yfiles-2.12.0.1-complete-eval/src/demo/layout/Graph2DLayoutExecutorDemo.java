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
package demo.layout;

import demo.view.DemoBase;
import demo.view.DemoDefaults;
import y.algo.GraphConnectivity;
import y.algo.AbortHandler;
import y.algo.AlgorithmAbortedException;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.layout.BufferedLayouter;
import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.random.RandomLayouter;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/**
 * Demonstrates how the {@link Graph2DLayoutExecutor} can be used to apply
 * layout algorithms to a {@link Graph2D}.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/viewer_layout.html#cls_Graph2DLayoutExecutor">Section Class Graph2DLayoutExecutor</a> in the yFiles for Java Developer's Guide
 */
public class Graph2DLayoutExecutorDemo extends DemoBase
{
  // the label that shows some status (if non-blocking)
  private JLabel statusLabel;
  // the progress bar that shows indeterminate progress (if non-blocking)
  private JProgressBar progressBar = new JProgressBar();

  // the types of execution
  private JComboBox layoutExecutionTypeBox;

  // the type of layouter
  private JComboBox layouterBox;

  public Graph2DLayoutExecutorDemo() {
    //build sample graph
    buildGraph( view.getGraph2D() );

    view.setViewPoint2D(-200.0, -200.0);
  }

  protected void configureDefaultRealizers() {
    // painting shadows is expensive and not well suited for graphs with many
    // nodes such as this demo's sample graph
    DemoDefaults.registerDefaultNodeConfiguration(false);
    DemoDefaults.configureDefaultRealizers(view);
  }

  /**
   * Overwritten to add the status label and the progress bar.
   */
  public void addContentTo(JRootPane rootPane) {
    this.statusLabel = new JLabel("Status");
    final Dimension minimumSize = this.statusLabel.getMinimumSize();
    this.statusLabel.setMinimumSize(new Dimension(Math.max(200, minimumSize.width), minimumSize.height));
    final JPanel panel = new JPanel();
    panel.add(this.statusLabel, BorderLayout.LINE_START);
    this.progressBar.setMaximum(100);
    this.progressBar.setMinimum(0);
    this.progressBar.setValue(0);
    panel.add(progressBar, BorderLayout.CENTER);
    getContentPane().add(panel, BorderLayout.SOUTH);
    super.addContentTo(rootPane);
  }

  /** Creates a relatively large random graph to give the layout algorithms something to chew. */
  void buildGraph(Graph2D graph) {
    graph.clear();
    Node[] nodes = new Node[800];
    for(int i = 0; i < nodes.length; i++)
    {
      nodes[i] = graph.createNode();
    }
    Random random = new Random(0L);
    for ( int i = 0; i < nodes.length; i++ ) {

      int edgeCount;

      if (random.nextInt(10) == 0) {
        edgeCount = 4 + random.nextInt(5);
      } else {
        edgeCount = random.nextInt(3);
      }

      for ( int j = 0; j < edgeCount; j++ ) {
        graph.createEdge( nodes[ i ], nodes[ random.nextInt(nodes.length) ] );
      }
    }

    // remove all components except the largest one
    final NodeList[] components = GraphConnectivity.connectedComponents(graph);
    Arrays.sort(components, new Comparator() {
      public int compare(final Object o1, final Object o2) {
        return ((NodeList) o2).size() - ((NodeList) o1).size();
      }
    });
    for (int i = components.length -1; i > 0; i--) {
      for (NodeCursor nc = components[i].nodes(); nc.ok(); nc.next()) {
        graph.removeNode(nc.node());
      }
    }

    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      graph.getRealizer(node).setLabelText(Integer.toString(node.index()));
    }

    (new BufferedLayouter(new RandomLayouter())).doLayout(graph);
  }

  /**
   * Adds an extra layout action to the toolbar
   */
  protected JToolBar createToolBar() {
    final Action layoutAction = new AbstractAction(
            "Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        applyLayout();
      }
    };

    // chooser for the layouter
    layouterBox = new JComboBox(new Object[]{"Hierarchic", "Organic", "Orthogonal"});
    layouterBox.setMaximumSize(layouterBox.getPreferredSize());
    layouterBox.setSelectedIndex(0);

    // chooser for the execution type.
    layoutExecutionTypeBox = new JComboBox(
      new Object[]{"Animated", "AnimatedThreaded", "Buffered", "Threaded", "Unbuffered", "AnimatedInOwnThread"});
    layoutExecutionTypeBox.setMaximumSize(layoutExecutionTypeBox.getPreferredSize());
    layoutExecutionTypeBox.setSelectedIndex(1);

    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(layoutAction));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(layouterBox);
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    toolBar.add(layoutExecutionTypeBox);

    return toolBar;
  }

  /**
   * Configures and invokes a layout algorithm
   */
  void applyLayout() {
    Layouter layouter = createLayouter();
    switch (layoutExecutionTypeBox.getSelectedIndex()) {
      case 0:
        applyLayoutAnimated(layouter);
        break;
      case 1:
        applyLayoutAnimatedThreaded(layouter);
        break;
      case 2:
        applyLayoutBuffered(layouter);
        break;
      case 3:
        applyLayoutThreaded(layouter);
        break;
      case 4:
        applyLayoutUnbuffered(layouter);
        break;
      case 5:
        applyLayoutAnimatedInOwnThread(layouter);
        break;
    }
  }

  /**
   * Creates and returns a Layouter instance according to the given layout options.
   */
  Layouter createLayouter() {
    switch (layouterBox.getSelectedIndex()) {
      default:
      case 0:
        return new IncrementalHierarchicLayouter();
      case 1:
        final SmartOrganicLayouter organicLayouter = new SmartOrganicLayouter();
        organicLayouter.setQualityTimeRatio(1.0);
        organicLayouter.setMaximumDuration(2L * 60L * 1000L);
        organicLayouter.setMultiThreadingAllowed(true);
        return organicLayouter;
      case 2:
        return new OrthogonalLayouter();
    }
  }

  /**
   * Applies the given layout algorithm to the graph
   * This is done in a separate Thread asynchronously.
   * Although the view and UI is responsive direct mouse and keyboard input is blocked.
   * The layout process can be canceled and even killed through a dialog that is spawned.
   */
  void applyLayoutAnimatedThreaded(final Layouter layouter) {
    this.progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED_THREADED);
    // set a slow animation, so that the animation can easily be canceled.
    layoutExecutor.getLayoutMorpher().setPreferredDuration(3000L);
    layoutExecutor.getLayoutMorpher().setEasedExecution(true);
    layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
    // lock the view so that the graph cannot be edited.
    layoutExecutor.setLockingView(true);

    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();

    final JDialog dialog = newCancelDialog(handler, layouter.getClass().getName());

    // the following method will return immediately and the layout and animation is performed in a new thread
    // asynchronously.
    final Graph2DLayoutExecutor.LayoutThreadHandle handle = layoutExecutor.doLayout(view, layouter, new Runnable() {
      public void run() {
        dialog.dispose();
        progressBar.setIndeterminate(false);
        statusLabel.setText("Layout Done");
      }
    }, new ExceptionHandler());

    // this is visible because the layout is not blocking (this) EDT
    this.statusLabel.setText("Layout is running");


    if (handle.isRunning()) {
      dialog.setVisible(true);
    }
  }

  /**
   * Applies the given layout algorithm to the graph
   * This is done synchronously blocking the calling Thread, thus leaving the view unresponsive during the layout.
   */
  void applyLayoutBuffered(final Layouter layouter){
    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.BUFFERED);
    layoutExecutor.doLayout(view, layouter);
  }

  /**
   * Applies the given layout algorithm to the graph in an animated fashion.
   * This is done synchronously blocking the calling Thread, thus leaving the view unresponsive during the layout
   * and animation.
   */
  void applyLayoutAnimated(final Layouter layouter){
    // this won't be visible to the user because the EDT is blocked.
    statusLabel.setText("Starting Animated Blocking Layout");
    progressBar.setIndeterminate(true);
    
    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();
    
    try {
      final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED);
      layoutExecutor.doLayout(view, layouter);
    } finally {
      progressBar.setIndeterminate(false);
      statusLabel.setText("Animated Blocking Layout Done.");
    }
  }

  /**
   * Applies the given layout algorithm to the graph in an animated fashion using a blocking call
   * from a separate newly spawned thread.
   * This leaves the view responsive, but the view is still editable during the layout.
   */
  void applyLayoutAnimatedInOwnThread(final Layouter layouter){
    statusLabel.setText("Starting own layout thread.");
    progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.ANIMATED);

    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();

    final JDialog dialog = newCancelDialog(handler, layouter.getClass().getName());

    new Thread(new Runnable() {
      public void run() {
        try {
          layoutExecutor.doLayout(view, layouter, null, new ExceptionHandler());
        } finally {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              dialog.dispose();
              statusLabel.setText("Layout Thread Finished.");
              progressBar.setIndeterminate(false);
            }
          });
        }
      }
    }).start();

    dialog.setVisible(true);
  }

  /**
   * Runs the layout in a separate thread, leaving the view responsive
   * but the view is still editable during the layout.
   */
  void applyLayoutThreaded(final Layouter layouter){
    statusLabel.setText("Starting threaded layout");
    progressBar.setIndeterminate(true);
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.THREADED);

    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();

    final JDialog dialog = newCancelDialog(handler, layouter.getClass().getName());

    layoutExecutor.doLayout(view, layouter, new Runnable() {
      public void run() {
        dialog.dispose();
        statusLabel.setText("Layout Returned");
        progressBar.setIndeterminate(false);
      }
    }, new ExceptionHandler());
    statusLabel.setText("Return from doLayout()");

    dialog.setVisible(true);
  }

  void applyLayoutUnbuffered(final Layouter layouter) {
    final AbortHandler handler = AbortHandler.createForGraph(view.getGraph2D());
    // This might be an "old" handler instance from a previous call
    // to applyLayoutAnimatedThreaded, applyLayoutAnimatedInOwnThread, or
    // applyLayoutThreaded. Resetting the handler prevents an an undesired early
    // termination of the new layout calculation due to previous stop or cancel
    // requests.
    handler.reset();

    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor(Graph2DLayoutExecutor.UNBUFFERED);
    layoutExecutor.doLayout(view, layouter);
  }

  /**
   * Creates a simply dialog for aborting layout calculation.
   * @param layoutName the name of the layout algorithm used.
   * @return a dialog for aborting layout calculation.
   */
  private JDialog newCancelDialog(
          final AbortHandler handler, final String layoutName
  ) {
    final JDialog dialog = new JDialog(JOptionPane.getRootFrame(), "");
    final Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    final JLabel label = new JLabel("Layout Running [" + layoutName + "].");
    box.add(label);
    box.add(Box.createVerticalStrut(12));
    box.add(new JButton(new AbstractAction("Stop") {
      private boolean stopped;
      public void actionPerformed(ActionEvent e) {
        // first, request an early but graceful termination
        if (!stopped) {
          handler.stop();
          statusLabel.setText("Stopping.");
          label.setText("Stopping Thread.[" + layoutName + "].");
          ((JButton)e.getSource()).setText("Cancel");
          stopped = true;
        } else {
          // graceful termination is not fast enough, request immediate
          // termination
          handler.cancel();
          setEnabled(false);
          statusLabel.setText("Cancelling.");
        }
      }
    }));
    dialog.getContentPane().add(box);
    dialog.setLocationRelativeTo(view);
    dialog.pack();
    return dialog;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new Graph2DLayoutExecutorDemo()).start();
      }
    });
  }

  /**
   * Reports exceptions that occur during a layout calculation.
   */
  private class ExceptionHandler implements Graph2DLayoutExecutor.ExceptionListener {
    public void exceptionHappened( final Throwable t ) {
      if (t instanceof AlgorithmAbortedException) {
        statusLabel.setText("Layout cancelled.");
      } else {
        t.printStackTrace(System.err);
        statusLabel.setText("Exception Happened.");
      }
    }
  }
}
