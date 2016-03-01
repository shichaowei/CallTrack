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
package demo.view.graphexplorer;

import demo.view.DemoBase;
import y.option.AbstractItemEditor;
import y.option.ConstraintManager;
import y.option.DefaultCompoundEditor;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.ItemEditor;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.OptionSection;
import y.option.PropertyChangeReporter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Provides settings for graph exploration.
 */
class GraphExplorerOptionHandler extends OptionHandler {
  static final String ATTRIBUTE_LAYOUT_CALLBACK =
          "GraphExplorerOptionHandler.layoutCallback";

  static final byte ID_LAYOUT_ORTHOGONAL = 0;
  static final byte ID_LAYOUT_ORGANIC = 1;
  static final byte ID_LAYOUT_HIERARCHIC = 2;
  static final byte ID_LAYOUT_BALLOON = 3;
  static final byte ID_LAYOUT_CIRCULAR = 4;

  static final byte EDGE_TYPE_ALL = 0;
  static final byte EDGE_TYPE_OUT = 1;
  static final byte EDGE_TYPE_IN = 2;

  private static final String EXPLORE_NODES = "Explore Nodes";
  private static final String EXPLORE_NEIGHBORS = "Neighbors";
  private static final String EXPLORE_SUCCESSORS = "Successors";
  private static final String EXPLORE_PREDECESSORS = "Predecessors";
  private static final String LAYOUT = "Layout";
  private static final String LAYOUT_ORTHOGONAL = "Orthogonal";
  private static final String LAYOUT_ORGANIC = "Organic";
  private static final String LAYOUT_HIERARCHIC = "Hierarchic";
  private static final String LAYOUT_BALLOON = "Balloon";
  private static final String LAYOUT_CIRCULAR = "Circular";
  private static final String MAX_NEW_NODES = "Max New Nodes on Click";
  private static final String ENABLE_FILTER = "Filter Neighbors";
  private static final String MAX_NEIGHBOR_DIST = "Max Neighbor Distance";

  GraphExplorerOptionHandler() {
    super("Option Table");
    useSection("General");
    addEnum(LAYOUT, new String[]{
            LAYOUT_ORGANIC,
            LAYOUT_ORTHOGONAL,
            LAYOUT_HIERARCHIC,
            LAYOUT_BALLOON,
            LAYOUT_CIRCULAR
    }, 1);
    addEnum(EXPLORE_NODES, new String[]{
            EXPLORE_NEIGHBORS,
            EXPLORE_SUCCESSORS,
            EXPLORE_PREDECESSORS
    }, 0);
    addInt(MAX_NEW_NODES, 10, 1, 100);
    useSection("Filter");
    addBool(ENABLE_FILTER, true);
    addInt(MAX_NEIGHBOR_DIST, 4, 1, 100);

    final ConstraintManager constraints = new ConstraintManager(this);
    constraints.setEnabledOnValueEquals(ENABLE_FILTER, Boolean.TRUE, MAX_NEIGHBOR_DIST);
  }

  /**
   * Returns the maximum number of new neighbor nodes that are added when
   * double-clicking an existing node (with hidden neighbors).
   * @return the maximum number of new nodes for double-clicks.
   */
  int getMaxNewNodes() {
    return getInt(MAX_NEW_NODES);
  }

  /**
   * Returns the maximum graph distance for nodes to be kept when
   * double-clicking an existing node. More formally, for a return value
   * of <code>D</code> and a node <code>A</code>, all nodes whose graph distance
   * to <code>A</code> is greater than <code>D</code> are removed.
   * @return the maximum graph distance for nodes to be kept or
   * <code>-1</code> if all neighbors are to be kept.
   */
  int getMaxDist() {
    if (getBool(ENABLE_FILTER)) {
      return getInt(MAX_NEIGHBOR_DIST);
    } else {
      return -1;
    }
  }

  /**
   * Returns the edge type that corresponds to the type of nodes that are
   * added when double-clicking an existing node (with hidden neighbors).
   * @return one of {@link #EDGE_TYPE_ALL}, {@link #EDGE_TYPE_OUT}, and
   * {@link #EDGE_TYPE_IN}.
   */
  byte getExplorationEdgeType() {
    final String exploration = getString(EXPLORE_NODES);
    if (EXPLORE_NEIGHBORS.equals(exploration)) {
      return EDGE_TYPE_ALL;
    } else if (EXPLORE_SUCCESSORS.equals(exploration)) {
      return EDGE_TYPE_OUT;
    } else if (EXPLORE_PREDECESSORS.equals(exploration)) {
      return EDGE_TYPE_IN;
    } else {
      return EDGE_TYPE_ALL;
    }
  }

  /**
   * Returns a symbolic constant representing the layout algorithm to lay
   * out the displayed graph.
   * @return one of {@link #ID_LAYOUT_ORTHOGONAL}, {@link #ID_LAYOUT_ORGANIC},
   * {@link #ID_LAYOUT_HIERARCHIC}, {@link #ID_LAYOUT_BALLOON}, or
   * {@link #ID_LAYOUT_CIRCULAR}.
   */
  byte getLayoutId() {
    final String layout = getString(LAYOUT);
    if (layout.equals(LAYOUT_HIERARCHIC)) {
      return ID_LAYOUT_HIERARCHIC;
    } else if (layout.equals(LAYOUT_ORGANIC)) {
      return ID_LAYOUT_ORGANIC;
    } else if (layout.equals(LAYOUT_BALLOON)) {
      return ID_LAYOUT_BALLOON;
    } else if (layout.equals(LAYOUT_CIRCULAR)) {
      return ID_LAYOUT_CIRCULAR;
    } else {
      return ID_LAYOUT_ORTHOGONAL;
    }
  }

  /**
   * Creates controls to edit the provided settings.
   * @return controls to edit the provided settings.
   */
  JComponent createEditorComponent() {
    final SimpleEditorFactory factory = new SimpleEditorFactory();
    final Editor editor = factory.createEditor(this);

    final JComponent optionComponent = editor.getComponent();
    final Dimension size = optionComponent.getPreferredSize();
    size.width = Math.max(size.width, 400);
    size.height = Math.max(size.height, 250);
    optionComponent.setPreferredSize(size);
    optionComponent.setMaximumSize(size);
    return optionComponent;
  }

  /**
   * Editor factory for option handlers that displays sections as titled
   * components.
   */
  private static final class SimpleEditorFactory extends DefaultEditorFactory {
    public Editor createEditor( final OptionHandler handler, final Map attributes ) {
      final SimpleEditor editor = new SimpleEditor(handler, this);
      handler.addEditor(editor);
      return editor;
    }
  }

  /**
   * Compound editor whose editor component is made up of one title
   * component per section.
   */
  private static final class SimpleEditor extends DefaultCompoundEditor {
    private final JComponent component;

    SimpleEditor( final OptionHandler handler, final SimpleEditorFactory factory ) {
      final Font font;
      final Font cbf;
      if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getName())) {
        font = new JLabel().getFont().deriveFont(Font.PLAIN);
        cbf = font.deriveFont((float) (font.getSize() - 2));
      } else {
        font = new JLabel().getFont();
        cbf = font;
      }

      final int sc = handler.sectionCount();
      final JLabel[][] labels = new JLabel[sc][];
      final JComponent[][] controls = new JComponent[sc][];

      int maxW = 0;

      JButton lb = null;
      
      final String handlerName = handler.getName();
      for (int i = 0; i < sc; ++i) {
        final OptionSection section = handler.section(i);
        section.setAttribute(OptionSection.ATTRIBUTE_CONTEXT, handlerName);

        final int ic = section.itemCount();
        labels[i] = new JLabel[ic];
        controls[i] = new JComponent[ic];
        for (int j = 0; j < ic; ++j) {
          final OptionItem item = section.item(j);

          final Object attr = item.getAttribute(ATTRIBUTE_LAYOUT_CALLBACK);
          if (attr instanceof ActionListener) {
            lb = createLayoutButton();
            lb.addActionListener((ActionListener) attr);
          }

          labels[i][j] = createLabel(item.getName(), font);
          final Dimension s1 = labels[i][j].getPreferredSize();
          if (maxW < s1.width) {
            maxW = s1.width;
          }

          final ItemEditor editor = factory.createEditor(item);
          editor.setAutoCommit(true);
          addEditor(editor);
          if (editor instanceof PropertyChangeReporter) {
            final JLabel label = labels[i][j];
            ((PropertyChangeReporter) editor).addPropertyChangeListener(
                    AbstractItemEditor.PROPERTY_ENABLED,
                    new PropertyChangeListener() {
                      public void propertyChange( final PropertyChangeEvent e ) {
                        label.setEnabled(((Boolean) e.getNewValue()).booleanValue());
                      }
                    });
          }

          controls[i][j] = editor.getComponent();
          if (controls[i][j] instanceof JComboBox) {
            controls[i][j].setFont(cbf);
          }
        }
      }

      final int margin = 5;

      final Box main = new Box(BoxLayout.Y_AXIS);
      for (int i = 0; i < sc; ++i) {
        final OptionSection section = handler.section(i);

        final JPanel sectionPane = new JPanel(new GridBagLayout());
        sectionPane.setBorder(BorderFactory.createTitledBorder(section.getName()));

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        final int ic = section.itemCount();
        for (int j = 0; j < ic; ++j) {
          final int topInset = j == 0 ? 3 : margin;
          final int bottomInset = j == ic - 1 ? margin : 0;

          gbc.insets = new Insets(topInset, margin, bottomInset, 0);
          gbc.fill = GridBagConstraints.NONE;
          gbc.gridx = 0;
          gbc.gridy = j;
          gbc.weightx = 0.0;
          gbc.weighty = 0.0;
          final JLabel jl = labels[i][j];
          jl.setPreferredSize(new Dimension(maxW, jl.getPreferredSize().height));
          sectionPane.add(jl, gbc);

          gbc.insets = new Insets(topInset, margin, bottomInset, 0);
          gbc.fill = GridBagConstraints.HORIZONTAL;
          gbc.gridx = 1;
          gbc.weightx = 1.0;
          sectionPane.add(controls[i][j], gbc);

          if (lb != null) {
            gbc.insets = new Insets(topInset, 1, bottomInset, margin);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            if (section.item(j).getAttribute(ATTRIBUTE_LAYOUT_CALLBACK) instanceof ActionListener) {
              sectionPane.add(lb, gbc);
            } else {
              sectionPane.add(createSpacer(lb), gbc);
            }
          }
        }

        main.add(sectionPane);
        if (i != sc - 1) {
          main.add(Box.createRigidArea(new Dimension(margin, margin)));
        }
      }

      component = new JPanel(new GridBagLayout());
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 1.0;
      gbc.weighty = 0.0;
      component.add(main, gbc);

      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridy = 1;
      gbc.weighty = 1.0;
      component.add(new JPanel(), gbc);
    }

    public JComponent getComponent() {
      return component;
    }

    private static JLabel createLabel( final String name, final Font font ) {
      final JLabel jl = new JLabel(name);
      jl.setFont(font);
      return jl;
    }

    private static JComponent createSpacer( final JComponent jc ) {
      final Dimension s = jc.getPreferredSize();
      final JPanel jp = new JPanel();
      jp.setPreferredSize(s);
      jp.setSize(s);
      jp.setMinimumSize(s);
      jp.setMaximumSize(s);
      return jp;
    }

    private static JButton createLayoutButton() {
      if (DemoBase.SHARED_LAYOUT_ICON == null) {
        return new JButton("Layout");
      } else {
        final JButton jb = new JButton();
        jb.setIcon(DemoBase.SHARED_LAYOUT_ICON);
        jb.setMargin(new Insets(0, 0, 0, 0));
        jb.setToolTipText("Layout");
        return jb;
      }
    }
  }
}
