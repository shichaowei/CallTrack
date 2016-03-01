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
import y.option.OptionHandler;
import y.view.Graph2DPrinter;
import y.view.PrintPreviewPanel;
import y.view.GenericNodeRealizer;
import y.view.BevelNodePainter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Font;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Locale;
import java.util.Map;

/**
 * Demo that centers around the printing facilities of yFiles.
 * This class shows how to use the yFiles print preview and how to 
 * add a title and footer to the printed page or poster.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/printing.html">Section Printing a Graph's Visual Representation</a> in the yFiles for Java Developer's Guide
 */
public class PrintPreviewDemo extends DemoBase {

  public PrintPreviewDemo() {
    loadGraph("resource/PrintPreviewDemo.graphml");
  }

  protected void configureDefaultRealizers() {
    super.configureDefaultRealizers();
    //register bevel node configuration that is used in initial graph
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    final Map map = factory.createDefaultConfigurationMap();
    GenericNodeRealizer.Painter painter = new BevelNodePainter();
    map.put(GenericNodeRealizer.Painter.class, painter);
    map.put(GenericNodeRealizer.ContainsTest.class, painter);
    factory.addConfiguration("BevelNodeConfig", map);
  }

  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    bar.add(new PrintPreviewAction());
    return bar;
  }

  /**
   * Action that brings up a customized print preview panel.
   */
  class PrintPreviewAction extends AbstractAction {
    Graph2DPrintPreviewPanel ppp;

    PrintPreviewAction() {
      super("Print Preview");

      PrinterJob printJob = PrinterJob.getPrinterJob();
      ppp = new Graph2DPrintPreviewPanel(
          printJob,
          new Graph2DPrinter(view),
          printJob.defaultPage());
    }

    public void actionPerformed(ActionEvent e) {
      final JDialog dialog = new JDialog((JFrame) view.getTopLevelAncestor(), contentPane.getName(), true);
      dialog.setContentPane(ppp);
      dialog.setResizable(true);
      dialog.pack();
      dialog.setVisible(true);
    }

  }

  /**
   * Extended print preview panel that incorporates the standard printing options
   * provided by class {@link y.view.Graph2DView}.
   */
  public class Graph2DPrintPreviewPanel extends PrintPreviewPanel {
    OptionHandler printOptions;
    Graph2DPrinter gp;


    public Graph2DPrintPreviewPanel(
        PrinterJob printJob,
        final Graph2DPrinter gp,
        PageFormat pf) {
      super(printJob,
          gp,
          gp.getPosterColumns(),
          gp.getPosterColumns() * gp.getPosterRows(),
          pf);
      this.gp = gp;

      // the preview's Format and Print actions should execute in the
      // current thread (the Swing event dispatch thread) because
      // printing the currently displayed graph in a background thread
      // might lead to errors (painting EdgeRealizer is not thread-safe)
      setThreadingEnabled(false);

      //setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.useSection("General");

      printOptions.addInt("Poster Rows", gp.getPosterRows());
      printOptions.addInt("Poster Columns", gp.getPosterColumns());
      printOptions.addBool("Add Poster Coords", gp.getPrintPosterCoords());
      final String[] area = {"View", "Graph"};
      if (gp.getClipType() == Graph2DPrinter.CLIP_GRAPH) {
        printOptions.addEnum("Clip Area", area, 1);
      } else {
        printOptions.addEnum("Clip Area", area, 0);
      }

      printOptions.addBool("Show Title/Footer On Every Page", false);

      Graph2DPrinter.DefaultTitleDrawable td = new Graph2DPrinter.DefaultTitleDrawable();
      printOptions.useSection("Title");
      printOptions.addString("Text", td.getText());
      printOptions.addColor("Titlebar Color", td.getTitleBarColor(), true);
      printOptions.addColor("Text Color", td.getTextColor(), true);
      printOptions.addInt("Font Size", contentPane.getFont().getSize());

      Graph2DPrinter.DefaultFooterDrawable fd = new Graph2DPrinter.DefaultFooterDrawable();
      printOptions.useSection("Footer");
      printOptions.addString("Text", fd.getText());
      printOptions.addColor("Footer Color", fd.getFooterColor(), true);
      printOptions.addColor("Text Color", fd.getTextColor(), true);
      printOptions.addInt("Font Size", contentPane.getFont().getSize());
      
      gp.setTextBarType(Graph2DPrinter.TITLE_AND_FOOTER_FOR_ENTIRE_POSTER);

      //show custom print dialog and adopt values
      Action optionAction = new AbstractAction("Options...") {
        public void actionPerformed(ActionEvent ev) {
          if (!printOptions.showEditor()) {
            return;
          }
          gp.setPosterRows(printOptions.getInt("Poster Rows"));
          gp.setPosterColumns(printOptions.getInt("Poster Columns"));
          gp.setPrintPosterCoords(
              printOptions.getBool("Add Poster Coords"));
          if ("Graph".equals(printOptions.get("Clip Area"))) {
            gp.setClipType(Graph2DPrinter.CLIP_GRAPH);
          } else {
            gp.setClipType(Graph2DPrinter.CLIP_VIEW);
          }

          if (printOptions.getBool("Show Title/Footer On Every Page")) {
            Graph2DPrinter.RepeatingTitleDrawable title =
                new Graph2DPrinter.RepeatingTitleDrawable();
            title.setText(printOptions.getString("Title", "Text"));
            title.setTitleBarColor((Color) printOptions.get("Title", "Titlebar Color"));
            title.setTextColor((Color) printOptions.get("Title", "Text Color"));
            title.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Title", "Font Size")));
            gp.setTitleDrawable(title);

            Graph2DPrinter.RepeatingFooterDrawable footer =
                new Graph2DPrinter.RepeatingFooterDrawable();
            footer.setText(printOptions.getString("Footer", "Text"));
            footer.setFooterColor((Color) printOptions.get("Footer", "Footer Color"));
            footer.setTextColor((Color) printOptions.get("Footer", "Text Color"));
            footer.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Footer", "Font Size")));
            gp.setFooterDrawable(footer);

            gp.setTextBarType(Graph2DPrinter.TITLE_AND_FOOTER_FOR_EVERY_PAGE);
          } else {
            Graph2DPrinter.DefaultTitleDrawable title =
                new Graph2DPrinter.DefaultTitleDrawable();
            title.setText(printOptions.getString("Title", "Text"));
            title.setTitleBarColor((Color) printOptions.get("Title", "Titlebar Color"));
            title.setTextColor((Color) printOptions.get("Title", "Text Color"));
            title.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Title", "Font Size")));
            gp.setTitleDrawable(title);

            Graph2DPrinter.DefaultFooterDrawable footer =
                new Graph2DPrinter.DefaultFooterDrawable();
            footer.setText(printOptions.getString("Footer", "Text"));
            footer.setFooterColor((Color) printOptions.get("Footer", "Footer Color"));
            footer.setTextColor((Color) printOptions.get("Footer", "Text Color"));
            footer.setFont(new Font("Dialog", Font.PLAIN, printOptions.getInt("Footer", "Font Size")));
            gp.setFooterDrawable(footer);

            gp.setTextBarType(Graph2DPrinter.TITLE_AND_FOOTER_FOR_ENTIRE_POSTER);
          }

          setPages(0,
              gp.getPosterColumns(),
              gp.getPosterColumns() * gp.getPosterRows());

          zoomToFit();
        }
      };
      addControlComponent(new JButton(optionAction));

    }

  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new PrintPreviewDemo()).start();
      }
    });
  }

}

    

      
