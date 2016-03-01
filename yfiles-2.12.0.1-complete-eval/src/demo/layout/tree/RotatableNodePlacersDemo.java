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
package demo.layout.tree;

import y.base.Node;
import y.base.NodeCursor;
import y.layout.tree.AbstractRotatableNodePlacer;
import y.layout.tree.AbstractRotatableNodePlacer.Matrix;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.BusPlacer;
import y.layout.tree.DoubleLinePlacer;
import y.layout.tree.GridNodePlacer;
import y.layout.tree.LeftRightPlacer;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.util.DataProviderAdapter;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.LineType;
import y.view.PolyLineEdgeRealizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

/**
 * This demo presents GenericTreeLayouter in conjunction with {@link NodePlacer}s that support
 * subtree rotation. The NodePlacers, rotations and root alignments for the selected nodes may
 * be changed using the panel on the left side.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/cls_GenericTreeLayouter.html">Section Generic Tree Layout</a> in the yFiles for Java Developer's Guide
 **/
public class RotatableNodePlacersDemo extends AbstractTreeDemo {
  private JComboBox nodePlacerCombo;
  private JComboBox rootAlignmentCombo;
  private JButton rotLeftButton;
  private JButton rotRightButton;
  private JButton mirHorButton;
  private JButton mirVertButton;

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new RotatableNodePlacersDemo()).start();
      }
    });
  }

  public RotatableNodePlacersDemo() {
    Graph2D graph = view.getGraph2D();

    /**
     * Listener for the graph that either updates the panel on the left when at least one node is selected or disables all panel items otherwise.
     */
    graph.addGraph2DSelectionListener(new Graph2DSelectionListener() {
      public void onGraph2DSelectionEvent(Graph2DSelectionEvent e) {
        if (view.getGraph2D().selectedNodes().ok()) {
          readComboValues();
        } else {
          setEnabled(false);
        }
      }
    });

    //Realizers
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow(Arrow.STANDARD);
    ((PolyLineEdgeRealizer) defaultER).setSmoothedBends(true);
    defaultER.setLineType(LineType.LINE_2);


    JPanel configPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(10, 10, 10, 10);
    configPanel.add(new JLabel("Settings for actual selection"), constraints);

    constraints.gridy = 1;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("NodePlacer:"), constraints);

    constraints.gridy = 2;
    constraints.insets = new Insets(0, 0, 0, 0);
    nodePlacerCombo = new JComboBox();
    nodePlacerCombo.setModel(new DefaultComboBoxModel(new String[]{"SimpleNodePlacer", "DoubleLinePlacer", "BusPlacer", "LeftRightPlacer", "GridNodePlacer"}));
    nodePlacerCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          changeNodePlacersForSelection();
        }
      }
    });
    configPanel.add(nodePlacerCombo, constraints);

    constraints.gridy = 3;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("Rotation:"), constraints);

    constraints.gridy = 4;
    constraints.insets = new Insets(0, 0, 0, 0);

    JPanel rotationPanel = new JPanel();
    configPanel.add(rotationPanel, constraints);
    rotationPanel.setLayout(new FlowLayout());
    rotLeftButton = new JButton("Left");
    rotLeftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.ROT90);
      }
    });
    rotationPanel.add(rotLeftButton);
    rotRightButton = new JButton("Right");
    rotRightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.ROT270);
      }
    });
    rotationPanel.add(rotRightButton);

    constraints.gridy = 6;
    rotationPanel = new JPanel();
    configPanel.add(rotationPanel, constraints);
    rotationPanel.setLayout(new FlowLayout());
    mirHorButton = new JButton("Mir Hor");
    mirHorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.MIR_HOR);
      }
    });
    rotationPanel.add(mirHorButton);
    mirVertButton = new JButton("Mir Vert");
    mirVertButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rotate(Matrix.MIR_VERT);
      }
    });
    rotationPanel.add(mirVertButton);


    constraints.gridy = 7;
    constraints.insets = new Insets(5, 5, 0, 0);
    configPanel.add(new JLabel("Root Alignment:"), constraints);

    constraints.gridy = 8;
    constraints.insets = new Insets(0, 0, 0, 0);
    rootAlignmentCombo = new JComboBox();

    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.CENTER);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.CENTER_OVER_CHILDREN);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.MEDIAN);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.LEADING);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.LEFT);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.RIGHT);
    rootAlignmentCombo.addItem(AbstractRotatableNodePlacer.RootAlignment.TRAILING);
    rootAlignmentCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          changeNodePlacersForSelection();
        }
      }
    });
    rootAlignmentCombo.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                    boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        RootAlignment rootAlignment = (RootAlignment) value;
        if (rootAlignment == RootAlignment.CENTER) {
          label.setText("Center");
        }
        if (rootAlignment == RootAlignment.LEADING) {
          label.setText("Leading");
        }
        if (rootAlignment == RootAlignment.LEFT) {
          label.setText("Left");
        }
        if (rootAlignment == RootAlignment.RIGHT) {
          label.setText("Right");
        }
        if (rootAlignment == RootAlignment.TRAILING) {
          label.setText("Trailing");
        }
        if (rootAlignment == RootAlignment.MEDIAN) {
          label.setText("Median");
        }
        if (rootAlignment == RootAlignment.CENTER_OVER_CHILDREN) {
          label.setText("Center over children");
        }
        return label;
      }
    });
    configPanel.add(rootAlignmentCombo, constraints);

    JPanel left = new JPanel(new BorderLayout());
    left.add(configPanel, BorderLayout.NORTH);

    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, view);
    sp.setOneTouchExpandable(true);
    sp.setContinuousLayout(false);
    contentPane.add(sp, BorderLayout.CENTER);
    createSampleGraph(graph);

    // provide meta information for grid placer
    graph.addDataProvider(GridNodePlacer.GRID_DPKEY, new DataProviderAdapter() {
      public int getInt( final Object dataHolder ) {
        final int idx = indexOf((Node) dataHolder);
        if (idx < 0) {
          return 0;
        } else {
          return idx / 5;
        }
      }

      private int indexOf( final Node child ) {
        int i = 0;
        final Node parent = child.firstInEdge().source();
        for (NodeCursor nc = parent.successors(); nc.ok(); nc.next()) {
          if (nc.node() == child) {
            return i;
          }
          ++i;
        }
        return -1;
      }
    });

    setEnabled(false);

    createSampleGraph(view.getGraph2D());
    calcLayout();

    // provide meta information for left right placer. Placed after the calcLayout method call to achieve a nice pre-layout of the left right placer.
    graph.addDataProvider(LeftRightPlacer.LEFT_RIGHT_DPKEY, new LeftRightPlacer.LeftRightDataProvider(nodePlacerMap));
  }

  /**
   * Enables or disables all panel items.
   * @param enabled whether or not all items in the panel should be enabled.
   */
  private void setEnabled(boolean enabled) {
    rootAlignmentCombo.setEnabled(enabled);
    rotLeftButton.setEnabled(enabled);
    rotRightButton.setEnabled(enabled);
    mirHorButton.setEnabled(enabled);
    mirVertButton.setEnabled(enabled);
    nodePlacerCombo.setEnabled(enabled);
  }

  /**
   * Rotates all selected nodes by the given rotation matrix.
   * @param rotation the matrix to rotate the nodes.
   */
  private void rotate(Matrix rotation) {
    for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      AbstractRotatableNodePlacer oldPlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      Matrix matrix = oldPlacer == null ? rotation.multiply(Matrix.DEFAULT) : rotation.multiply(oldPlacer.getModificationMatrix());

      AbstractRotatableNodePlacer placer = createPlacerFromComboBox(matrix);
      nodePlacerMap.set(node, placer);
    }
    calcLayout();
  }

  /**
   * Mutex lock simulation to prevent settings from being read while they are updated.
   */
  private boolean blockLayout;

  /**
   * Reads the settings of the first selected node and updates the values in the panel accordingly.
   */
  private void readComboValues() {
    blockLayout = true;

    setEnabled(true);

    NodeCursor nodeCursor = view.getGraph2D().selectedNodes();
    if (!nodeCursor.ok()) {
      setEnabled(false);
    } else {
      Node node = nodeCursor.node();

      AbstractRotatableNodePlacer nodePlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      if (nodePlacer == null) {
        return;
      }

      if (nodePlacer instanceof SimpleNodePlacer) {
        rootAlignmentCombo.setEnabled(true);
        rootAlignmentCombo.setSelectedItem(((SimpleNodePlacer) nodePlacer).getRootAlignment());
        nodePlacerCombo.setSelectedIndex(0);
      } else if (nodePlacer instanceof DoubleLinePlacer) {
        rootAlignmentCombo.setEnabled(true);
        rootAlignmentCombo.setSelectedItem(((DoubleLinePlacer) nodePlacer).getRootAlignment());
        nodePlacerCombo.setSelectedIndex(1);
      } else if (nodePlacer instanceof BusPlacer){
        rootAlignmentCombo.setEnabled(false);
        nodePlacerCombo.setSelectedIndex(2);
      } else if (nodePlacer instanceof LeftRightPlacer){
        rootAlignmentCombo.setEnabled(false);
        nodePlacerCombo.setSelectedIndex(3);
      }else if (nodePlacer instanceof GridNodePlacer){
        rootAlignmentCombo.setEnabled(true);
        rootAlignmentCombo.setSelectedItem(((GridNodePlacer) nodePlacer).getRootAlignment());
        nodePlacerCombo.setSelectedIndex(4);
      }
    }

    blockLayout = false;
  }

  /**
   * Applies all settings from the panel to the selected nodes und updates the layout.
   */
  private void changeNodePlacersForSelection() {
    if (blockLayout) {
      return;
    }

    for (NodeCursor nodeCursor = view.getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      AbstractRotatableNodePlacer oldPlacer = (AbstractRotatableNodePlacer) nodePlacerMap.get(node);
      Matrix matrix = oldPlacer != null ? oldPlacer.getModificationMatrix() : AbstractRotatableNodePlacer.Matrix.DEFAULT;

      AbstractRotatableNodePlacer placer = createPlacerFromComboBox(matrix);
      nodePlacerMap.set(node, placer);
    }
    calcLayout();
  }

  /**
   * Creates an {@link AbstractRotatableNodePlacer} according to the selections in the panel with the given matrix.
   * If the created placer doesn't allow custom root alignment, the rootAlignmentCombo is disabled (and enabled otherwise).
   * @param modificationMatrix the modification matrix for the placer.
   * @return an {@link AbstractRotatableNodePlacer} according to the selections in the panel with the given matrix.
   */
  private AbstractRotatableNodePlacer createPlacerFromComboBox(Matrix modificationMatrix ) {
    RootAlignment rootAlignment = (RootAlignment) rootAlignmentCombo.getSelectedItem();
    AbstractRotatableNodePlacer placer = null;
    int selection = nodePlacerCombo.getSelectedIndex();
    switch (selection){
      case 0:
        placer =new SimpleNodePlacer(modificationMatrix);
        ((SimpleNodePlacer) placer).setRootAlignment(rootAlignment);
        rootAlignmentCombo.setEnabled(true);
        break;
      case 1:
        placer = new DoubleLinePlacer(modificationMatrix);
        ((DoubleLinePlacer) placer).setRootAlignment(rootAlignment);
        rootAlignmentCombo.setEnabled(true);
        break;
      case 2:
        placer = new BusPlacer(modificationMatrix);
        rootAlignmentCombo.setEnabled(false);
        break;
      case 3:
        placer = new LeftRightPlacer(modificationMatrix);
        rootAlignmentCombo.setEnabled(false);
        break;
      case 4:
        placer = new GridNodePlacer(modificationMatrix, rootAlignment);
        rootAlignmentCombo.setEnabled(true);
        break;
    }
    return placer;
  }

  /**
   * For this demo, deletion of nodes is disabled.
   * @return false
   */
  protected boolean isDeletionEnabled() {
    return false;
  }

  /**
   * For this demo, clipboard is disabled.
   * @return false
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  /**
   * Creates the initial graph. The graph consists of a root and 5 children with a SimpleNodePlacer. Each child of the root has another
   * branch, one for each node placer. Some nodes are rotated and/or mirrored for demonstration.
   */
  private void createSampleGraph(Graph2D graph) {
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer(root).setFillColor(layerColors[0]);
    nodePlacerMap.set(root, new SimpleNodePlacer());
    createChildren(graph, root);
    calcLayout();
  }

  private void createChildren(Graph2D graph, Node root) {
    for (int i = 0; i < 5; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[1]);
      SimpleNodePlacer nodePlacer = new SimpleNodePlacer(Matrix.MIR_VERT_ROT90);
      nodePlacer.setRootAlignment(RootAlignment.LEADING);
      nodePlacerMap.set(child, nodePlacer);
      switch (i){
        case 0: createSimpleNodePlacerBranch(graph, child); break;
        case 1: createDoubleLinePlacerBranch(graph, child); break;
        case 2: createBusPlacerBranch(graph, child); break;
        case 3: createLeftRightPlacerBranch(graph, child); break;
        case 4: createGridNodePlacerBranch(graph, child); break;
      }
    }
  }

  private void createLeafs(Graph2D graph, Node root, int number){
    for (int i = 0; i < number; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[3]);
      SimpleNodePlacer nodePlacer = new SimpleNodePlacer( Matrix.DEFAULT);
      nodePlacerMap.set(child, nodePlacer);
    }
  }

  private void createSimpleNodePlacerBranch(Graph2D graph, Node root){
    for (int i = 0; i < 4; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[2]);
      Matrix rotation = Matrix.DEFAULT;
      switch (i){
        case 1:
          rotation = Matrix.MIR_HOR; break;
        case 2:
          rotation = Matrix.ROT180; break;
        case 3:
          rotation = Matrix.MIR_HOR; break;
      }
      SimpleNodePlacer nodePlacer = new SimpleNodePlacer(rotation);
      nodePlacer.setRootAlignment(RootAlignment.LEADING);
      nodePlacerMap.set(child, nodePlacer);
      createLeafs(graph, child, 3);
    }
  }

  private void createDoubleLinePlacerBranch(Graph2D graph, Node root){
    for (int i = 0; i < 3; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[2]);
      Matrix rotation = Matrix.DEFAULT;
      switch (i){
        case 1:
          rotation = Matrix.ROT90; break;
        case 2:
          rotation = Matrix.MIR_VERT_ROT90; break;
      }
      DoubleLinePlacer nodePlacer = new DoubleLinePlacer(rotation);
      nodePlacer.setRootAlignment(RootAlignment.LEADING);
      nodePlacerMap.set(child, nodePlacer);
      createLeafs(graph, child, 5);
    }
  }

  private void createBusPlacerBranch(Graph2D graph, Node root){
    for (int i = 0; i < 3; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[2]);
      Matrix rotation = Matrix.DEFAULT;
      switch (i){
        case 1:
          rotation = Matrix.MIR_HOR; break;
        case 2:
          rotation = Matrix.ROT180; break;
      }
      BusPlacer nodePlacer = new BusPlacer(rotation);
      nodePlacerMap.set(child, nodePlacer);
      createLeafs(graph, child, 5);
    }
  }

  private void createLeftRightPlacerBranch(Graph2D graph, Node root){
    for (int i = 0; i < 2; i++) {
      Node child = graph.createNode();
      graph.createEdge(root, child);
      graph.getRealizer(child).setFillColor(layerColors[2]);
      Matrix rotation = Matrix.DEFAULT;
      switch (i){
        case 1:
          rotation = Matrix.ROT90; break;
      }
      LeftRightPlacer nodePlacer = new LeftRightPlacer(rotation);
      nodePlacerMap.set(child, nodePlacer);
      createLeafs(graph, child, 5);
    }
  }

  private void createGridNodePlacerBranch(Graph2D graph, Node root){
    Node child = graph.createNode();
    graph.createEdge(root, child);
    graph.getRealizer(child).setFillColor(layerColors[2]);
    GridNodePlacer nodePlacer = new GridNodePlacer();
    nodePlacer.setRootAlignment(RootAlignment.LEADING);
    nodePlacerMap.set(child, nodePlacer);
    createLeafs(graph, child, 20);
  }
}
