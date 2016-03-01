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
package demo.layout.component;

import demo.view.DemoBase;
import y.layout.ComponentLayouter;
import y.view.Graph2DLayoutExecutor;
import y.view.ShapeNodeRealizer;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.Locale;

/**
 * The {@link y.layout.ComponentLayouter} arranges the connected components of a graph. This demo shows how the
 * components can be arranged using different arrangement styles provided by the <code>ComponentLayouter</code>.
 */
public class ComponentLayouterDemo extends DemoBase {
  private byte style;
  private int spacing;

  /**
   * Initializes a new instance of the demo and loads a sample graph.
   */
  public ComponentLayouterDemo() {
    this(null);
  }

  /**
   * Initializes a new instance of the demo, adds a help pane for the specified file and loads a sample graph.
   */
  public ComponentLayouterDemo(final String helpFilePath) {
    addHelpPane(helpFilePath);
    loadGraph("resource/graphs_sample.graphml");
  }

  /**
   * Registers view actions and adds a component resize handler.
   */
  protected void registerViewActions() {
    super.registerViewActions();
    view.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        layout();
      }
    });
  }

  /**
   * Creates and adds a pane with help text.
   */
  protected JComponent createHelpPane(URL helpURL) {
    final JComponent helpPane = super.createHelpPane(helpURL);
    helpPane.setPreferredSize(new Dimension(300, 250));
    return helpPane;
  }

  /**
   * Sets the default node realizer to <code>ShapeNodeRealizer</code>.
   */
  protected void configureDefaultRealizers() {
    final ShapeNodeRealizer nr = new ShapeNodeRealizer(ShapeNodeRealizer.RECT);
    nr.setFillColor(Color.ORANGE);
    nr.removeLabel(0);
    view.getGraph2D().setDefaultNodeRealizer(nr);
  }

  /**
   * Creates a toolbar for configuring and running the <code>ComponentLayouter</code>.
   */
  protected JToolBar createToolBar() {
    final JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(createActionControl(new AbstractAction("Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        layout();
      }
    }));
    bar.addSeparator();
    bar.add(new JLabel(" Style: "));
    bar.add(createStyleComboBox());
    bar.add(new JLabel(" Spacing: "));
    bar.add(createSpacingSlider());
    return bar;
  }

  /**
   * Creates a combo box for choosing the component arrangement strategy.
   */
  private Component createStyleComboBox() {
    final JComboBox comboBox = new JComboBox(new Object[]{
        "Multiple Rows",
        "Single Row",
        "Single Column",
        "Packed Rectangle",
        "Compact Rectangle",
        "Packed Circle",
        "Compact Circle",
        "Nested Rows",
        "Compact Nested Rows",
        "Width Constrained Nested Rows",
        "Height Constrained Nested Rows",
        "Width Constrained Compact Nested Rows",
        "Height Constrained Compact Nested Rows"});
    final byte[] styles = new byte[]{
        ComponentLayouter.STYLE_ROWS,
        ComponentLayouter.STYLE_SINGLE_ROW,
        ComponentLayouter.STYLE_SINGLE_COLUMN,
        ComponentLayouter.STYLE_PACKED_RECTANGLE,
        ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE,
        ComponentLayouter.STYLE_PACKED_CIRCLE,
        ComponentLayouter.STYLE_PACKED_COMPACT_CIRCLE,
        ComponentLayouter.STYLE_MULTI_ROWS,
        ComponentLayouter.STYLE_MULTI_ROWS_COMPACT,
        ComponentLayouter.STYLE_MULTI_ROWS_WIDTH_CONSTRAINED,
        ComponentLayouter.STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED,
        ComponentLayouter.STYLE_MULTI_ROWS_WIDTH_CONSTRAINED_COMPACT,
        ComponentLayouter.STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED_COMPACT};
    comboBox.setMaximumSize(comboBox.getPreferredSize());
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        style = styles[comboBox.getSelectedIndex()];
        layout();
      }
    });
    style = styles[0];
    return comboBox;
  }

  /**
   * Creates a slider for choosing the spacing between the components.
   */
  private Component createSpacingSlider() {
    spacing = 10;
    final JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 10, 100, spacing);
    slider.setMaximumSize(new Dimension(200, 100));
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!slider.getValueIsAdjusting()) {
          spacing = slider.getValue();
          layout();
        }
      }
    });
    return slider;
  }

  /**
   * Creates the default menu bar and adds an additional menu of examples graphs.
   */
  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = super.createMenuBar();
    JMenu menu = new JMenu("Sample Graphs");
    menuBar.add(menu);
    menu.add(new AbstractAction("Nodes") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/nodes_sample.graphml");
        layout();
      }
    });
    menu.add(new AbstractAction("Graphs") {
      public void actionPerformed(ActionEvent e) {
        loadGraph("resource/graphs_sample.graphml");
        layout();
      }
    });
    return menuBar;
  }

  /**
   * Runs the <code>ComponentLayouter</code>.
   */
  private void layout() {
    final ComponentLayouter layouter = new ComponentLayouter();
    layouter.setStyle(style);
    layouter.setComponentSpacing(spacing);
    layouter.setPreferredLayoutSize(view.getWidth(), view.getHeight());
    new Graph2DLayoutExecutor().doLayout(view, layouter);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new ComponentLayouterDemo("resource/componentlayouterhelp.html").start("Component Layouter Demo");
      }
    });
  }
}
