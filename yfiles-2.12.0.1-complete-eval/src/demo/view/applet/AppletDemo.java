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

import javax.swing.JApplet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class represents a simple graph editor applet. The applet can be used inside a web browser with
 * a Java 2 plugin (version >= 1.4) installed.
 * <p>
 * This applet supports the applet parameter "graphsource" which allows to specify the graph that is initially
 * displayed by the applet. The graph needs to be in GraphML format. URLs are specified relative to
 * the document base of the applet.
 * </p>
 * <p>
 * To compile and deploy the applet it is best to use the Ant build script "build.xml" located in this directory.
 * It compiles the application classes, jars them as "application.jar" in this directory and also copies "y.jar" into this directory.
 * Once these Jar files are in place, the applet can be launched by opening the included HTML page
 * "applet.html" with your browser.
 * </p>
 * <p>
 * This applet demo has been successfully tested with Internet Explorer 9, 7 and 6, Firefox 7 and 3.5, Chrome 15 and 3,
 * and Safari 5 and 4.
 * </p>
 */
public class AppletDemo extends JApplet {
  
  DemoEditor demoEditor;

  /**
   * Mandatory default constructor for an applet.
   */
  public AppletDemo() {
  }

  /**
   * Applet initialization. Create the application GUI.
   */
  public void init() {
    super.init();
    demoEditor = new DemoEditor();
    getRootPane().setContentPane(demoEditor);
    getRootPane().setJMenuBar(demoEditor.createMenuBar());
  }

  /**
   * Start the applet. Try to load the graph given by applet parameter "graphsource".
   */
  public void start() {
    super.start();

    String graphSource = getParameter("graphsource");

    if (graphSource != null) {
      try {
        URL graphURL = new URL(getDocumentBase(), graphSource);
        try {
          URLConnection urlConnection = graphURL.openConnection();
          urlConnection.connect();
        } catch (IOException ioex) {
          //try classpath if resource node located at document base
          graphURL = DemoEditor.getResource(getClass(), graphSource);
        }
        if (graphURL != null) {
          demoEditor.loadAndDisplayGraph(graphURL);
        }
      } catch (MalformedURLException muex) {
        muex.printStackTrace();
      }
    }
  }

  /**
   * Returns applet parameter information.
   */
  public String[][] getParameterInfo() {
    return new String[][]{
        // Parameter Name     Kind of Value   Description
        {"graphsource", "URL", "an URL pointing to a diagram in GraphML format"}
    };
  }
}
