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

import y.io.GraphMLIOHandler;
import y.io.graphml.graph2d.PostprocessorOutputHandler;
import y.module.YModule;
import y.util.D;
import y.util.DataProviderAdapter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JToolBar;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/**
 * This demo centers around postprocessing actions that can be specified
 * within a GraphML file. These actions allow to process
 * the parsed graph structure before it gets returned by the GraphML parser.
 * <p>
 * A GraphML processor can be any instance of the yFiles module class YModule.
 * The configuration of a processor is done by changing the values
 * managed by the associated OptionHandler instance. This demo allows to configure
 * a processor interactively. Furthermore, it can be used to display the GraphML
 * representation of a processor module configuration.
 * When saving a file the XML representation of the current processor will be added
 * to the output file as well. When loading this file again,
 * the postprocessor will perform its action.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_postprocessors">Section yFiles GraphML Post-Processors</a> in the yFiles for Java Developer's Guide
 */
public class PostprocessorDemo extends GraphMLDemo {

  private YModule processorModule;

  protected void loadInitialGraph() {
    //register a DataProvider that returns the selected
    //processor module. This dataprovider is used by
    //PostprocessorOutputHandler to lookup the postprocessors
    //it should serialize.
    view.getGraph2D().addDataProvider(PostprocessorOutputHandler.PROCESSORS_DPKEY,
        new DataProviderAdapter() {
          public Object get(Object graph) {
            return processorModule;
          }
        });

    loadGraph("resources/postprocessors/ant-build.graphml");
  }

  protected JToolBar createToolBar() {
    //a combo box that contains the class names of available
    //postprocessors.
    final JComboBox combo = new JComboBox(new String[]{
        "y.module.IncrementalHierarchicLayoutModule",         
        "y.module.SmartOrganicLayoutModule",                  
        "demo.io.graphml.NodeSizeAdapter"
    }
    );
    combo.setMaximumSize(combo.getPreferredSize());
    combo.setEditable(true);
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String className = combo.getSelectedItem().toString();
        try {
          processorModule = (YModule) Class.forName(className).newInstance();
        } catch (Exception ex) {
          D.showError("Can't create instance of class " + className);
        }
      }
    });
    combo.setSelectedIndex(0);

    JToolBar jtb = super.createToolBar();
    jtb.addSeparator();
    jtb.add(createActionControl(new ApplyProcessorAction()));
    jtb.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    jtb.add(combo);
    jtb.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    jtb.add(createActionControl(new ConfigureProcessorAction()));

    return jtb;
  }

  protected String[] getExampleResources() {
    return new String[]{
        "resources/postprocessors/ant-build.graphml",
        "resources/postprocessors/food-owl.graphml",
    };
  }

  /**
   * Creates a GraphMLIOHandler that has additional output support for
   * GraphML postprocessors.
   *
   * Note that input support for PostProcessors is registered by default and
   * need not be added manually.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioh = super.createGraphMLIOHandler();
    ioh.getGraphMLHandler().addOutputHandlerProvider(new PostprocessorOutputHandler());
    return ioh;
  }

  /**
   * Actions that allows to configure the selected postprocessor interactively.
   */
  class ConfigureProcessorAction extends AbstractAction {
    ConfigureProcessorAction() {
      super("Settings...", getIconResource("resource/properties.png"));
    }

    public void actionPerformed(ActionEvent e) {
      if (processorModule != null) {
        OptionSupport.showDialog(processorModule, view.getGraph2D(), false, view.getFrame());
      }
    }
  }

  /**
   * Actions that applies the selected processor on the displayed graph.
   */
  class ApplyProcessorAction extends AbstractAction {
    ApplyProcessorAction() {
      super("Postprocessor", SHARED_LAYOUT_ICON);
      putValue(Action.SHORT_DESCRIPTION, "Run the current postprocessor");
    }

    public void actionPerformed(ActionEvent e) {
      if (processorModule != null) {
        processorModule.start(view.getGraph2D());
        view.updateView();
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new PostprocessorDemo().start();
      }
    });
  }
}
