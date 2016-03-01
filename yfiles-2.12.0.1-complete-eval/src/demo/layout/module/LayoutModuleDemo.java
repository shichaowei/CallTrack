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
package demo.layout.module;

import demo.view.DemoBase;
import demo.layout.module.BalloonLayoutModule;
import demo.layout.module.ChannelEdgeRouterModule;
import demo.layout.module.CircularLayoutModule;
import demo.layout.module.IncrementalHierarchicLayoutModule;
import demo.layout.module.LabelingModule;
import y.module.LayoutModule;
import demo.layout.module.OrthogonalEdgeRouterModule;
import demo.layout.module.OrthogonalLayoutModule;
import demo.layout.module.PolylineEdgeRouterModule;
import demo.layout.module.RadialLayoutModule;
import y.module.RandomLayoutModule;
import demo.layout.module.SeriesParallelLayoutModule;
import demo.layout.module.SmartOrganicLayoutModule;
import demo.layout.module.TreeLayoutModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.option.PropertiesIOHandler;
import y.view.Arrow;
import y.view.hierarchy.HierarchyManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

/**
 * Demonstrates how layout modules can be added to the GUI of an application.
 * A layout module is a layout algorithm combined
 * with an option dialog, that allows to change the
 * options of a layout algorithm interactively
 * (only available if layout is part of distribution).
 *
 */
public class LayoutModuleDemo extends DemoBase {
  public LayoutModuleDemo() {
    //use a delta arrow to make edge directions clear
    view.getGraph2D().getDefaultEdgeRealizer().setArrow(Arrow.DELTA);

    //to enable loading of hierarchical grouped graphs
    new HierarchyManager(view.getGraph2D());
    loadGraph("resource/sample.graphml");
  }

  /**
   * Creates a toolbar for choosing, configuring, and running layout algorithms.
   */
  protected JToolBar createToolBar() {
    final JComboBox layoutModules = createLayouterModulesComboBox();
    final Action layoutPropertiesAction = createPropertiesAction(layoutModules);
    final Action layoutAction = createAction("Layout", layoutModules);

    final JComboBox routerModules = createRouterModulesComboBox();
    final Action routerPropertiesAction = createPropertiesAction(routerModules);
    final Action routerAction = createAction("Route", routerModules);

    final YModule labelingModule = loadSettings(new LabelingModule());
    final Action labelingPropertiesAction = new AbstractAction(
            "Settings...", getIconResource("resource/properties.png")) {
      public void actionPerformed(ActionEvent e) {
        showOptionDialog(labelingModule);
      }
    };
    final Action labelingAction = new AbstractAction(
            "Place labels", SHARED_LAYOUT_ICON) {
      public void actionPerformed(final ActionEvent e) {
        startModule(labelingModule);
      }
    };

    final JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(createActionControl(layoutAction));
    toolBar.add(layoutModules);
    toolBar.add(createActionControl(layoutPropertiesAction));
    toolBar.addSeparator();
    toolBar.add(createActionControl(routerAction));
    toolBar.add(routerModules);
    toolBar.add(createActionControl(routerPropertiesAction));
    toolBar.addSeparator();
    toolBar.add(createActionControl(labelingAction));
    toolBar.add(createActionControl(labelingPropertiesAction));
    return toolBar;
  }

  private JComboBox createLayouterModulesComboBox() {
    final JComboBox layoutModules = new JComboBox();
    layoutModules.setRenderer(new LayoutModuleListCellRenderer());
    layoutModules.addItem(loadSettings(new IncrementalHierarchicLayoutModule()));
    layoutModules.addItem(loadSettings(new SmartOrganicLayoutModule()));
    layoutModules.addItem(loadSettings(new OrthogonalLayoutModule()));
    layoutModules.addItem(loadSettings(new CircularLayoutModule()));
    layoutModules.addItem(loadSettings(new RadialLayoutModule()));
    layoutModules.addItem(loadSettings(new TreeLayoutModule()));
    layoutModules.addItem(loadSettings(new SeriesParallelLayoutModule()));
    layoutModules.addItem(loadSettings(new DiagonalLayoutModule()));
    layoutModules.addItem(loadSettings(new RandomLayoutModule()));
    layoutModules.setSelectedIndex(0);
    layoutModules.setMaximumSize(layoutModules.getPreferredSize());
    layoutModules.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            startModule((LayoutModule) layoutModules.getSelectedItem());
          }
        });
    return layoutModules;
  }

  private JComboBox createRouterModulesComboBox() {
    final JComboBox routerModules = new JComboBox();
    routerModules.setRenderer(new LayoutModuleListCellRenderer());
    routerModules.addItem(loadSettings(new PolylineEdgeRouterModule()));
    routerModules.addItem(loadSettings(new OrthogonalEdgeRouterModule()));
    routerModules.addItem(loadSettings(new ChannelEdgeRouterModule()));
    routerModules.setSelectedIndex(0);
    routerModules.setMaximumSize(routerModules.getPreferredSize());
    routerModules.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          startModule((LayoutModule) routerModules.getSelectedItem());
        }
      });
    return routerModules;
  }

  private Action createPropertiesAction(final JComboBox modules) {
    return new AbstractAction("Settings...", getIconResource("resource/properties.png")) {
      public void actionPerformed(final ActionEvent e) {
        showOptionDialog((LayoutModule) modules.getSelectedItem());
      }
    };
  }

  private Action createAction(final String name, final JComboBox modules) {
    return new AbstractAction(name, SHARED_LAYOUT_ICON) {
      public void actionPerformed(final ActionEvent e) {
        startModule((LayoutModule) modules.getSelectedItem());
      }
    };
  }

  private void showOptionDialog(final YModule module) {
    if (module != null) {
      final OptionHandler options = module.getOptionHandler();
      if (options != null) {
        OptionSupport.showDialog(module, view.getGraph2D(), false, view.getFrame());
      }
    }
  }

  private void startModule(final YModule module) {
    if (module != null) {
      module.start(view.getGraph2D());
    }
  }

  /**
   * Restores module settings from a properties file in the
   * <code>resource</code> directory.
   */
  private YModule loadSettings(final YModule module) {
    final OptionHandler options = module.getOptionHandler();
    if (options != null) {
      final String filePath = "resource/" + getSimpleName(module) + ".properties";
      final URL resource = getClass().getResource(filePath);
      if (resource != null) {
        final Properties data = new Properties();
        try {
          final InputStream in = resource.openStream();
          try {
            data.load(in);
            options.read(new PropertiesIOHandler(data));
          } finally {
            in.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return module;
  }

  /**
   * Returns the simple class name of the given module. The simple class name
   * is the qualified class name without its package name prefix.
   */
  private static String getSimpleName(final YModule module) {
    final String qn = module.getClass().getName();
    final int idx = qn.lastIndexOf('.');
    return idx > -1 ? qn.substring(idx + 1) : qn;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new LayoutModuleDemo().start();
      }
    });
  }

  static class LayoutModuleListCellRenderer implements ListCellRenderer {
    final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(
            final JList list,
            final Object value,
            final int index,
            final boolean isSelected,
            final boolean cellHasFocus
    ) {
      if (value instanceof LayoutModule) {
        final String name = getLayouterName(value);
        return renderer.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
      } else {
        return renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    }

    private String getLayouterName(final Object value) {
      if (value instanceof CircularLayoutModule) {
        return "Circular";
      } else if (value instanceof DiagonalLayoutModule) {
        return "Diagonal";
      } else if (value instanceof IncrementalHierarchicLayoutModule) {
        return "Hierarchic";
      } else if (value instanceof SmartOrganicLayoutModule) {
        return "Organic";
      } else if (value instanceof OrthogonalLayoutModule) {
        return "Orthogonal";
      } else if (value instanceof RadialLayoutModule) {
        return "Radial";
      } else if (value instanceof RandomLayoutModule) {
        return "Random";
      } else if (value instanceof SeriesParallelLayoutModule) {
        return "Series-Parallel";
      } else if (value instanceof TreeLayoutModule) {
        return "Tree";
      } else if (value instanceof BalloonLayoutModule) {
        return "Balloon";
      } else if (value instanceof PolylineEdgeRouterModule) {
        return "Polyline";
      } else if (value instanceof OrthogonalEdgeRouterModule) {
        return "Orthogonal";
      } else if (value instanceof ChannelEdgeRouterModule) {
        return "Channel";
      } else {
        return ((YModule) value).getModuleName().toLowerCase();
      }
    }
  }
}
