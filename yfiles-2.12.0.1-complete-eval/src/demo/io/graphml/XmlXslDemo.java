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
package demo.io.graphml;


import y.io.XmlXslIOHandler;
import y.util.D;
import y.view.Graph2D;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.stream.StreamSource;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


/**
 * This demo shows how XML files can imported as
 * GraphML by the means of an XSLT stylesheet.
 * Sample stylesheets for the following XML data are provided:
 * <ul>
 * <li><a href="resources/xsl/ant2graphml.xsl">Ant build scripts</a></li>
 * <li><a href="resources/xsl/owl2graphml.xsl">OWL web ontology data</a></li>
 * <li><a href="resources/xsl/xmltree2graphml.xsl">the XML tree structure</a></li>
 * </ul>
 * <p>
 * <b>Third Party Licenses:</b><br/>
 * The OWL web ontology that is used in this demo is based on
 * <a href="http://owl.semanticweb.org/page/TestCase:WebOnt-miscellaneous-002">WebOnt-miscellaneous-002
 * by Michael K. Smith, Chris Welty, and Deborah L. McGuinness</a>
 * and licensed under the
 * <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported</a>
 * license.
 * </p>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_xslt">Section yFiles XSLT Support for GraphML</a> in the yFiles for Java Developer's Guide
 */
public class XmlXslDemo extends GraphMLDemo {
  private String[][] sampleFiles;

  public XmlXslDemo() {
    graphMLPane.setEditable(false);
  }

  protected void loadInitialGraph() {
    if (sampleFiles != null) {
      loadXml(getResource(sampleFiles[0][0]), getResource(sampleFiles[0][1]));
    }
  }

  protected void initialize() {
    super.initialize();

    sampleFiles = new String[][]{
        {"resources/xml/ant-build.xml", "resources/xsl/ant2graphml.xsl"},
        // resources/xml/food.owl licensed under the Creative Commons Attribution 3.0 Unported license
        {"resources/xml/food.owl", "resources/xsl/owl2graphml.xsl"},
        // resources/xml/food.owl licensed under the Creative Commons Attribution 3.0 Unported license
        {"resources/xml/food.owl", "resources/xsl/xmltree2graphml.xsl"},
    };
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = super.createMenuBar();
    createExamplesMenu(menuBar);
    return menuBar;
  }

  protected void createExamplesMenu(JMenuBar menuBar) {
    final JMenu menu = new JMenu("Example Graphs");
    menuBar.add(menu);

    for (int i = 0; i < sampleFiles.length; i++) {
      final String xml = sampleFiles[i][0];
      final String xsl = sampleFiles[i][1];
      final String name = xml.substring(xml.lastIndexOf('/') + 1)
          + " + " + xsl.substring(xsl.lastIndexOf('/') + 1);

      menu.add(new AbstractAction(name) {
        public void actionPerformed(ActionEvent e) {
          loadXml(getResource(xml), getResource(xsl));
        }
      });
    }
  }

  protected String[] getExampleResources() {
    return null;
  }

  public void loadXml(URL xmlResource, URL xslResource) {
    Graph2D graph = view.getGraph2D();
    try {
      XmlXslIOHandler ioh = new XmlXslIOHandler(createGraphMLIOHandler());
      ioh.setXslSource(new StreamSource(xslResource.openStream()));
      ioh.read(graph, xmlResource);
      view.fitContent();
      view.updateView();
    }
    catch (IOException ioe) {
      D.show(ioe);
    }
    finally {
      graphMLPane.updateGraphMLText(graph);
    }
  }

  protected Action createLoadAction() {
    return new AbstractAction("Load...") {

      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("XML input");

        URL xmlResource = null;
        if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
          try {
            xmlResource = chooser.getSelectedFile().toURI().toURL();
          } catch (MalformedURLException urlex) {
            urlex.printStackTrace();
          }
        }
        if (xmlResource != null) {
          chooser.setAcceptAllFileFilterUsed(false);
          chooser.setDialogTitle("XSL stylesheet");
          chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
              return f.isDirectory() || f.getName().endsWith(".xsl");
            }

            public String getDescription() {
              return "XML stylesheets (.xsl)";
            }
          });

          if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
            try {
              URL xslResource = chooser.getSelectedFile().toURI().toURL();
              if (xslResource != null) {
                loadXml(xmlResource, xslResource);
              }
            } catch (MalformedURLException urlex) {
              urlex.printStackTrace();
            }

          }
        }
      }

    };
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new XmlXslDemo().start();
      }
    });
  }
}
