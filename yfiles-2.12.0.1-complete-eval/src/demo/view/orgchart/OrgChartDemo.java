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
package demo.view.orgchart;

import demo.view.DemoDefaults;
import demo.view.orgchart.OrgChartTreeModel.Employee;
import org.xml.sax.InputSource;
import y.base.Node;
import y.io.SuffixFileFilter;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Overview;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This demo visualizes an organization chart. This comprehensive demo shows 
 * many aspects of yFiles. In particular it shows how to
 * <ul>
 * <li>visualize XML-formatted data as a graph</li>
 * <li>create a tree diagram from a {@link javax.swing.tree.TreeModel}</li>
 * <li>create customized node realizers that show text and multiple icons</li>
 * <li>create a customized {@link y.view.NavigationMode} view mode</li>
 * <li>create fancy roll-over effects when hovering over nodes</li> 
 * <li>implement level of detail (LoD) rendering of graphs</li>
 * <li>synchronize the selection state of {@link javax.swing.JTree} and {@link y.view.Graph2D}</li>
 * <li>customize {@link y.layout.tree.GenericTreeLayouter} to make it a perfect match for laying out organization charts</li>
 * <li>create local views for a large diagram</li>
 * <li>apply incremental layout and apply nice fade in and fade out effects to added or removed elements</li>
 * <li>implement and use keyboard navigation for {@link y.view.Graph2DView}</li>
 * </ul>
 * <p>
 * This demo is composed of multiple classes. Class {@link demo.view.orgchart.OrgChartDemo} is the organization chart application and driver class 
 * that organizes the UI elements and makes use of the Swing component {@link demo.view.orgchart.JOrgChart}. The model data used in this sample
 * is represented by the Swing tree model {@link demo.view.orgchart.OrgChartTreeModel}. Class {@link demo.view.orgchart.JOrgChart}
 * builds upon the more generic tree chart component {@link demo.view.orgchart.JTreeChart}. In a nutshell, JTreeChart 
 * visualizes a generic TreeModel and includes all the viewer logic, while JOrgChart visualizes a OrgChartTreeModel and 
 * customizes the look and feel of the component. Also it customizes the look and feel of the component to make it suitable for
 * organization charts.
 * </p> 
 */
public class OrgChartDemo {
  
  private JOrgChart orgChart;
  private JTable propertiesTable;
  private JTree tree;
  private String helpFile;

  public OrgChartDemo() {
    this(null);
  }

  public OrgChartDemo(final String helpFile) {
    this.helpFile = helpFile;
  }

  /**
   * Adds all UI elements of the application to a root pane container. 
   */
  public void addContentTo( final JRootPane rootPane ) {
    final JPanel contentPane = new JPanel(new BorderLayout());

    final OrgChartTreeModel model = readOrgChart(getResource("resources/orgchartmodel.xml"));
    if (model != null) {
      final Box leftPanel = new Box(BoxLayout.Y_AXIS);

      orgChart = createOrgChart(model);
      orgChart.setFitContentOnResize(true);

      final Overview overview = orgChart.createOverview();
      leftPanel.add(createTitledPanel(overview, "Overview"));

      tree = createStructureView(model);
      final JComponent viewOptions = createViewOptionsPanel();
      leftPanel.add(viewOptions, BorderLayout.NORTH);
      leftPanel.add(createTitledPanel(new JScrollPane(tree),"Structure View"));

      propertiesTable = createPropertiesTable();
      final JScrollPane scrollPane = new JScrollPane(propertiesTable);
      scrollPane.setPreferredSize(new Dimension(200, 180));
      leftPanel.add(createTitledPanel(scrollPane,"Properties"));

      //sync app whenever orgchart selection changes occur
      orgChart.getGraph2D().addGraph2DSelectionListener(new OrgChartSelectionUpdater());
      //sync app whenever tree selection changes occur
      tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionUpdater());
      //tree.addKeyListener(new TreeActionListener());

      contentPane.setLayout(new BorderLayout());
      final JSplitPane splitPane;
      if (helpFile != null) {
        contentPane.add(leftPanel, BorderLayout.WEST);
        final JComponent helpPane = createHelpPane(getResource(helpFile));
        helpPane.setMinimumSize(new Dimension(200, 10));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createTitledPanel(orgChart, "Organization Chart"),
            createTitledPanel(helpPane, "Help"));
        splitPane.setOneTouchExpandable(true);

        splitPane.setResizeWeight(1);

        contentPane.add(splitPane, BorderLayout.CENTER);
      } else {
        final Box panel = new Box(BoxLayout.X_AXIS);
        panel.add(leftPanel);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        contentPane.add(panel, BorderLayout.WEST);
        contentPane.add(createTitledPanel(orgChart, "Organization Chart"), BorderLayout.CENTER);
      }
    } else {
      contentPane.setPreferredSize(new Dimension(320, 24));
      contentPane.add(new JLabel("Could not create Organization Chart.", JLabel.CENTER));
    }

    rootPane.setContentPane(contentPane);
    final JMenuBar menu = new JMenuBar();
    final JMenu file = new JMenu("File");
    menu.add(file);
    file.add(createLoadAction());
    file.add(createSaveAction());
    file.addSeparator();
    file.add(createExitAction());
    rootPane.setJMenuBar(menu);
  }

  private Action createExitAction() {
    return new AbstractAction("Exit") {
      public void actionPerformed(final ActionEvent e) {
        System.exit(0);
      }
    };
  }

  private Action createSaveAction() {
    return new AbstractAction("Save") {
      public void actionPerformed(final ActionEvent e) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new SuffixFileFilter("xml", "XML File"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          final OrgChartTreeModel.OrgChartWriter writer = new OrgChartTreeModel.OrgChartWriter();
          writer.write(fileChooser.getSelectedFile(),(Employee) orgChart.getTreeNode(orgChart.getRootNode()));
        }
      }
    };
  }

  private Action createLoadAction() {
    return new AbstractAction("Load") {
      public void actionPerformed(final ActionEvent e) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Org Chart");
        fileChooser.setFileFilter(new SuffixFileFilter("xml","XML File"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          final OrgChartTreeModel orgChartTreeModel = readOrgChart(fileChooser.getSelectedFile());
          orgChart.setModel(orgChartTreeModel);
          orgChart.updateChart();
          tree.setModel(orgChartTreeModel);
          for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
          }
        }
      }
    };
  }


  private URL getResource( final String name ) {
    return demo.view.DemoBase.getResource(getClass(), name);
  }

  /**
   * Starts the application in a JFrame.
   */
  private void start() {
    final JFrame frame = new JFrame("yFiles Organization Chart Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setIconImage(demo.view.DemoBase.getFrameIcon());
    addContentTo(frame.getRootPane());
    frame.pack();
    frame.setVisible(true);
    orgChart.requestFocus();
  }

  /**
   * Creates the application help pane.
   */
  JComponent createHelpPane(final URL helpURL) {
    try {
      final JEditorPane editorPane = new JEditorPane(helpURL);
      editorPane.setEditable(false);
      editorPane.setPreferredSize(new Dimension(250, 250));
      return new JScrollPane(editorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Reads and returns the OrgChartTreeModel from an XML file.
   */
  OrgChartTreeModel readOrgChart(final URL orgChartURL) {
    OrgChartTreeModel model = null;
    try {
      final InputStream stream = orgChartURL.openStream();
      try {
        model = OrgChartTreeModel.create(new InputSource(stream));
      } finally {
        stream.close();
      }
    } catch(IOException ioException) {
      System.err.println("Failed to read from " + orgChartURL);
      ioException.printStackTrace();
    }
    return model;
  }

  /**
   * Reads and returns the OrgChartTreeModel from an XML file.
   */
  OrgChartTreeModel readOrgChart(final File orgChartFile) {
    OrgChartTreeModel model = null;
    try {
      final InputStream stream = new FileInputStream(orgChartFile);
      try {
        model = OrgChartTreeModel.create(new InputSource(stream));
      } finally {
        stream.close();
      }
    } catch(IOException ioException) {
      System.err.println("Failed to read from " + orgChartFile.getAbsolutePath());
      ioException.printStackTrace();
    }
    return model;
  }

  /**
   * Creates a JOrgChart component for the given model.
   */
  JOrgChart createOrgChart(final OrgChartTreeModel model) {
    final JOrgChart orgChart = new JOrgChart(model);
    orgChart.setPreferredSize(new Dimension(720, 750));
    addGlassPaneComponents(orgChart);
    return orgChart;
  }

  /**
   * Creates and returns a JTree-based structure view of the tree model.
   */
  JTree createStructureView(final TreeModel model) {
    final JTree tree = new JTree(model);
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      public Component getTreeCellRendererComponent(final JTree tree, Object value,final boolean sel,
             final boolean expanded, final boolean leaf,final int row, final boolean hasFocus) {
        if(value instanceof Employee) {
            final Employee employee = (Employee)value;
            value = employee.name;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      }
    });
    
    for(int i = 0; i < tree.getRowCount(); i++) {
      tree.expandPath(tree.getPathForRow(i));
    }

    return tree;
  }
  
  /**
   * Creates a JTable based properties view that displays the details of a selected model element.
   */
  JTable createPropertiesTable() {
    final DefaultTableModel tm = new DefaultTableModel();
    tm.addColumn("", new Object[]{"Name", "Position", "Phone", "Fax", "Email", "Business Unit", "Status","Assistant"});
    tm.addColumn("", new Object[]{"",     "",         "",       "",    ""    , ""             , "",Boolean.FALSE});
    return new JTable(tm) {

      private final JComboBox statusBox = new JComboBox();
      {
        statusBox.addItem("unavailable");
        statusBox.addItem("present");
        statusBox.addItem("travel");
      }

      public boolean isCellEditable(final int row, final int column) {
        return row != 5 && column == 1;
      }
      public TableCellRenderer getCellRenderer(final int row, final int column) {
        if (row == 7 && column == 1) {
          return getDefaultRenderer(Boolean.class);
        } else {
          return super.getCellRenderer(row, column);
        }
      }
      public TableCellEditor getCellEditor(final int row, final int column) {
        if (row == 6 && column == 1) {
          return new DefaultCellEditor(statusBox);
        } else if (row == 7 && column == 1) {
          return getDefaultEditor(Boolean.class);
        } else {
          return super.getCellEditor(row,column);
        }
      }
    };
  }
  
  /**
   * Updates the properties table when being called.
   */
  void updatePropertiesTable(final Employee e) {
    final DefaultTableModel tm = (DefaultTableModel) propertiesTable.getModel();
    final TableModelListener[] tml = tm.getTableModelListeners();
    for(int i = 0; i < tml.length-1; i++) {
      tm.removeTableModelListener(tml[i]);
    }
    tm.setValueAt(e.name,             0, 1);
    tm.setValueAt(e.position,         1, 1);
    tm.setValueAt(e.phone,            2, 1);
    tm.setValueAt(e.fax,              3, 1);
    tm.setValueAt(e.email,            4, 1);
    tm.setValueAt(e.businessUnit,     5, 1);
    tm.setValueAt(e.status,           6, 1);
    tm.setValueAt((e.assistant) ? Boolean.TRUE : Boolean.FALSE,        7, 1);
    final boolean status = !e.vacant;
    propertiesTable.setEnabled(status);
    orgChart.updateView();
    tm.addTableModelListener(new TableModelListener() {
      public void tableChanged(final TableModelEvent event) {
        final TableModel tableModel = (TableModel) event.getSource();
        switch (event.getFirstRow()) {
          case 0:
            e.name = (String) tableModel.getValueAt(0, 1);
            break;
          case 1:
            e.position = (String) tableModel.getValueAt(1, 1);
            break;
          case 2:
            e.phone = (String) tableModel.getValueAt(2,1);
            break;
          case 3:
            e.fax = (String) tableModel.getValueAt(3, 1);
            break;
          case 4:
            e.email = (String) tableModel.getValueAt(4, 1);
            break;
          case 5:
            e.businessUnit = (String) tableModel.getValueAt(5, 1);
            break;
          case 6:
            e.status = (String) tableModel.getValueAt(6, 1);
            break;
          case 7:
            e.assistant = ((Boolean) tableModel.getValueAt(7,1)).booleanValue();
            orgChart.layoutGraph(true);
            break;
        }
        orgChart.configureNodeRealizer(orgChart.getNodeForUserObject(e));
        orgChart.getGraph2D().updateViews();
      }
    });
  }
  
  /**
   * Create a panel that allows to configure view options.  
   */
  JPanel createViewOptionsPanel() {
    final JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    final JRadioButton button1 = new JRadioButton(new AbstractAction("Global View") {
      public void actionPerformed(final ActionEvent e) {
        orgChart.showGlobalHierarchy();
      }      
    });
    button1.setSelected(true);
        
    final JRadioButton button2 = new JRadioButton(new AbstractAction("Local View") {
      public void actionPerformed(final ActionEvent e) {
        orgChart.showLocalHierarchy(null);                
      }      
    });
        
    final ButtonGroup bg = new ButtonGroup();
    bg.add(button1);
    bg.add(button2);
    panel.add(button1);
    panel.add(button2);

    final JCheckBox checkBox = new JCheckBox(new AbstractAction("Show Colleagues") {
      public void actionPerformed(final ActionEvent e) {
        final boolean result = ((JCheckBox)e.getSource()).isSelected();
        if(result != orgChart.isSiblingViewEnabled()) {
          orgChart.setSiblingViewEnabled(result);
          orgChart.showLocalHierarchy(null);
        }        
      }      
    });
    checkBox.setEnabled(false);       
    panel.add(checkBox);

    final JCheckBox checkBox2 = new JCheckBox(new AbstractAction("Show Business Units") {
      public void actionPerformed(final ActionEvent e) {
        final boolean result = ((JCheckBox)e.getSource()).isSelected();
        if(result != orgChart.isGroupViewEnabled()) {
          orgChart.setGroupViewEnabled(result);
          orgChart.updateChart();
        }        
      }      
    });          
    panel.add(checkBox2);
    
    button2.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        checkBox.setEnabled(button2.isSelected());                
      }      
    });
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    return createTitledPanel(panel, "View Options");
    
  }
  
  /**
   * Adds some toolbar buttons on top of JOrgChart.
   */
  private void addGlassPaneComponents(final JOrgChart orgChart ) {
    final JPanel glassPane = orgChart.getGlassPane();
    glassPane.setLayout(new BorderLayout());
    
    final JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
    bar.setOpaque(false);
    bar.setBorder(BorderFactory.createEmptyBorder(20,15,0,0));

    final Action zoomIn = orgChart.createZoomInAction();
    zoomIn.putValue(AbstractAction.SMALL_ICON, getIconResource("resource/zoomIn.png"));
    zoomIn.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom into Chart");
    bar.add(createButton(zoomIn));
    
    final Action zoomOut = orgChart.createZoomOutAction();
    zoomOut.putValue(AbstractAction.SMALL_ICON, getIconResource("resource/zoomOut.png"));
    zoomOut.putValue(AbstractAction.SHORT_DESCRIPTION, "Zoom out of Chart");
    bar.add(createButton(zoomOut));
  
    final Action fitContent = orgChart.createFitContentAction();
    fitContent.putValue(AbstractAction.SMALL_ICON, getIconResource("resource/zoomFit.png"));
    fitContent.putValue(AbstractAction.SHORT_DESCRIPTION, "Fit Chart into View");
    bar.add(createButton(fitContent));
    
    glassPane.add(bar, BorderLayout.NORTH);
  }

  private Icon getIconResource( final String name ) {
    return demo.view.DemoBase.getIconResource(name);
  }

  /**
   * Creates a button for an action.
   */
  private JButton createButton(final Action action) {
    final JButton button = new JButton(action);
    button.setBackground(Color.WHITE);
    return button;
  }


  /**
   * A TreeSelectionListener that propagates selection changes to JOrgChart.
   */
  class TreeSelectionUpdater implements TreeSelectionListener {
    public void valueChanged(final TreeSelectionEvent e) {
      final TreePath path = e.getPath();
      final Employee employee = (Employee) path.getLastPathComponent();
      Node node = orgChart.getNodeForUserObject(employee);
      
      if(orgChart.isLocalViewEnabled() && (node == null || node.getGraph() == null)) {
        orgChart.showLocalHierarchy(employee);
        node = orgChart.getNodeForUserObject(employee);
      }
      
      if(node != null) {
        if(e.isAddedPath()) {
          final Graph2D graph2D = orgChart.getGraph2D();
          if(!graph2D.isSelected(node)) {
            graph2D.unselectAll();
            graph2D.setSelected(node, e.isAddedPath());
            orgChart.focusNode(node);
          }
        }
      }
    }        
  }

  /**
   * A Graph2DSelectionListener that propagates selection changes to a JTree.
   */
  class OrgChartSelectionUpdater implements Graph2DSelectionListener {    
    public void onGraph2DSelectionEvent(final Graph2DSelectionEvent e) {
      if(e.getSubject() instanceof Node) {
        final Node node = (Node) e.getSubject();
        final Employee p = (Employee) orgChart.getUserObject(node);
        if(p != null) {
          syncTreeSelection(node);
          if(orgChart.getGraph2D().isSelected(node)) { 
            updatePropertiesTable(p);          
          }
        }
      }    
    }
    
    void syncTreeSelection(final Node node) {
      
      final DefaultTreeSelectionModel smodel = (DefaultTreeSelectionModel) tree.getSelectionModel();
      smodel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
          
      final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      final TreeNode treeNode = (TreeNode) orgChart.getTreeNode(node);
      final TreeNode[] pathToRoot = model.getPathToRoot(treeNode);
      final TreePath path = new TreePath(pathToRoot);
       
      if(orgChart.getGraph2D().isSelected(node)) {
        smodel.addSelectionPath(path);
      } else  {
        smodel.removeSelectionPath(path);
      }
      tree.scrollPathToVisible(path);
    }
  }

  /**
   * Create a panel for a component and adds a title to it. 
   */
  public JPanel createTitledPanel(final JComponent content, final String title) {
    final JPanel panel = new JPanel();
    final JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    label.setBackground(new Color(240,240,240));
    label.setOpaque(true);
    label.setForeground(Color.DARK_GRAY);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setFont(label.getFont().deriveFont(13.0f));
    panel.setLayout(new BorderLayout());
    panel.add(label, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }
  
  /**
   * Main driver method.
   */
  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        (new OrgChartDemo("resources/orgcharthelp.html")).start();
      }
    });
  }
}
