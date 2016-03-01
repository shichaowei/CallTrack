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
package demo.view.applet;

import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.option.OptionHandler;
import y.util.D;
import y.view.Arrow;
import y.view.EditMode;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.SmartNodeLabelModel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URL;

/**
 *  Demonstrates basic usage of the Graph2DView.
 *
 *  Demonstrates how some actions can be performed on the view.
 *  The actions are:
 *  <ul>
 *    <li>Remove selected parts of the view content</li>
 *    <li>Zoom out of the view</li>
 *    <li>Zoom in on the view</li>
 *    <li>Fit view content to the size of the the view</li>
 *    <li>Print a graph</li>
 *  </ul>
 *
 *  Additionally, this demo shows how to set up the default edit mode
 *  to display tool tips over nodes.
 *
 */
public class DemoEditor extends JPanel {

  /**
   * The view component of this demo.
   */
  protected Graph2DView view;
  /**
   * The view mode to be used with the view.
   */
  protected EditMode editMode;


  public DemoEditor() {
    setLayout(new BorderLayout());

    view = new Graph2DView();
    view.setAntialiasedPainting(true);
    new Graph2DViewMouseWheelZoomListener().addToCanvas(view);
    
    NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
    NodeLabel label = nodeRealizer.getLabel();
    SmartNodeLabelModel labelModel = new SmartNodeLabelModel();
    label.setLabelModel(labelModel, labelModel.getDefaultParameter());
    nodeRealizer.setFillColor(new Color(0xFFCC00));
    view.getGraph2D().getDefaultEdgeRealizer().setTargetArrow(Arrow.STANDARD);
    
    editMode = createEditMode();
    if (editMode != null) {
      view.addViewMode(editMode);
    }
        
    Graph2DViewActions actions = new Graph2DViewActions(view);
    actions.install();

    add(view, BorderLayout.CENTER);
    add(createToolBar(), BorderLayout.NORTH);
  }

  protected EditMode createEditMode() {
    return new EditMode();
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar bar = new JToolBar();
    bar.add(new DemoEditor.DeleteSelection());
    bar.add(new DemoEditor.Zoom(1.2));
    bar.add(new DemoEditor.Zoom(0.8));
    bar.add(new DemoEditor.FitContent());
    return bar;
  }

  /**
   * Create a menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar bar = new JMenuBar();
    JMenu menu = new JMenu("File");
    menu.add(new DemoEditor.PrintAction());
    bar.add(menu);
    return bar;
  }

  /**
   * Creates an application  frame for this demo
   * and displays it. The given string is the title of
   * the displayed frame.
   */
  public void start(final String title) {
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public final void addContentTo(final JRootPane rootPane) {
    rootPane.setJMenuBar(createMenuBar());
    rootPane.setContentPane(this);
  }


  static void setIcon( final AbstractAction action, final String name ) {
    URL imageURL = getResource(DemoEditor.class, "../" + name);
    if (imageURL != null) {
      action.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
    }
  }

  static URL getResource( final Class resolver, final String name ) {
    final URL resource = resolver.getResource(name);
    if (resource == null) {
      System.err.println("Resource \"" + name +
              "\" not found in classpath of " + resolver + ".");
    }
    return resource;
  }

  /**
   * Action that prints the contents of the view
   */
  protected class PrintAction extends AbstractAction {
    PageFormat pageFormat;
    OptionHandler printOptions;

    public PrintAction() {
      super("Print");

      //setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.addInt("Poster Rows", 1);
      printOptions.addInt("Poster Columns", 1);
      printOptions.addBool("Add Poster Coords", false);
      final String[] area = {"View", "Graph"};
      printOptions.addEnum("Clip Area", area, 1);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DPrinter gprinter = new Graph2DPrinter(view);

      //show custom print dialog and adopt values
      if (!printOptions.showEditor()) {
        return;
      }
      gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
      gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
      gprinter.setPrintPosterCoords(
          printOptions.getBool("Add Poster Coords"));
      if ("Graph".equals(printOptions.get("Clip Area"))) {
        gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
      } else {
        gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
      }

      //show default print dialogs
      PrinterJob printJob = PrinterJob.getPrinterJob();
      if (pageFormat == null) {
        pageFormat = printJob.defaultPage();
      }
      PageFormat pf = printJob.pageDialog(pageFormat);
      if (pf == pageFormat) {
        return;
      } else {
        pageFormat = pf;
      }

      //setup printjob.
      //Graph2DPrinter is of type Printable
      printJob.setPrintable(gprinter, pageFormat);

      if (printJob.printDialog()) {
        try {
          printJob.print();
        } catch (PrinterException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  public void loadAndDisplayGraph(URL graphURL) {
    IOHandler ioh = new GraphMLIOHandler();
    try {
      view.getGraph2D().clear();
      ioh.read(view.getGraph2D(), graphURL);
    } catch (IOException ioe) {
      D.show(ioe);
    }
    //force redisplay of view contents
    view.fitContent();
    view.getGraph2D().updateViews();
  }

  /**
   * Action that deletes the selected parts of the graph.
   */
  protected class DeleteSelection extends AbstractAction {
    public DeleteSelection() {
      super("Delete Selection");
      setIcon(this, "resource/delete.png");
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().removeSelection();
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Action that applies a specified zoom level to the view.
   */
  protected class Zoom extends AbstractAction {
    double factor;

    public Zoom(double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      setIcon(this, factor > 1.0d ? "resource/zoomIn.png" : "resource/zoomOut.png");
      this.factor = factor;
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      //optional code that adjusts the size of the
      //view's world rectangle. The world rectangle
      //defines the region of the canvas that is
      //accessible by using the scrollbars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that fits the content nicely inside the view.
   */
  protected class FitContent extends AbstractAction {
    public FitContent() {
      super("Fit Content");
      setIcon(this, "resource/zoomFit.png");
    }

    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }
}
