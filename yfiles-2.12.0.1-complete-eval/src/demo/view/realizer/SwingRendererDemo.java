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
package demo.view.realizer;

import demo.view.DemoBase;
import y.base.DataProvider;
import y.base.Node;
import y.util.DataProviderAdapter;
import y.view.CellEditorMode;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.NodeCellEditor;
import y.view.NodeCellRenderer;
import y.view.NodeCellRendererPainter;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.SimpleUserDataHandler;
import y.view.SmartNodeLabelModel;

import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;


/**
 * This demo shows how yFiles can deal with Swing-like cell rendering and cell editing mechanisms.
 * It shows both how to customize {@link GenericNodeRealizer} to display JComponents as nodes, and
 * how to configure {@link y.view.EditMode} to work with {@link CellEditorMode} so that a double click
 * on a node initiates inline cell editing.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/jcomponent_support.html">Section Swing User Interface Components as Node Realizers</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_controller.html#node_cell_editors">Section User Interaction</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizers.html#cls_GenericNodeRealizer">Section Bringing Graph Elements to Life: The Realizer Concept</a> in the yFiles for Java Developer's Guide
 */
public class SwingRendererDemo extends DemoBase
{
  private GenericNodeRealizer gnr;
  private ShapeNodeRealizer snr = new ShapeNodeRealizer();

  /**
   * Instantiates this demo.
   */
  public SwingRendererDemo()
  {
    // create a simple NodeCellRenderer and NodeCellEditor instance that work together nicely
    NodeCellRenderer simpleNodeCellRenderer = new SimpleNodeCellRenderer();

    // Get the factory to register custom styles/configurations.
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // prepare a GenericNodeRealizer to use the NodeCellRenderer for rendering
    Map map = factory.createDefaultConfigurationMap();
    map.put(GenericNodeRealizer.Painter.class, new NodeCellRendererPainter(simpleNodeCellRenderer, NodeCellRendererPainter.USER_DATA_MAP));
    map.put(GenericNodeRealizer.UserDataHandler.class, new SimpleUserDataHandler(SimpleUserDataHandler.REFERENCE_ON_FAILURE));
    // register the configuration using the given name
    factory.addConfiguration("JTextField", map);

    // create another configuration based on the first one, this time use a more complex renderer
    map.put(GenericNodeRealizer.Painter.class, new NodeCellRendererPainter(new ComplexNodeCellRenderer(), NodeCellRendererPainter.USER_DATA_MAP));
    // register it
    factory.addConfiguration("JTable", map);

    // instantiate a default node realizer
    gnr = new GenericNodeRealizer();
    gnr.setSize(200.0, 50.0);
    gnr.setConfiguration("JTextField");
    gnr.setUserData("Hello Renderer World!");
    NodeLabel label = gnr.getLabel();
    SmartNodeLabelModel model = new SmartNodeLabelModel();
    label.setLabelModel(model, model.getDefaultParameter());

    // create a sample instance
    view.getGraph2D().setDefaultNodeRealizer(gnr);
    view.getGraph2D().createNode(150.0, 50.0, 200.0, 50.0, "");

    // and another one of the other kind
    gnr.setConfiguration("JTable");
    view.getGraph2D().createNode(150.0, 200.0, 150.0, 150.0, "");

  }

  /**
   * Adds the view modes to the view.
   * This implementation adds a new EditMode (with showNodeTips enabled) and
   * a new {@link y.view.AutoDragViewMode}.
   */
  protected void registerViewModes() {
    final NodeCellEditor simpleNodeCellEditor = new SimpleNodeCellEditor();
    // instantiate an appropriate editor for the complex renderer
    final NodeCellEditor complexNodeCellEditor = new SwingRendererDemo.ComplexNodeCellEditor();

    // create a data provider that dynamically switches between the different NodeCellEditor instances
    DataProvider nodeCellEditorProvider = new DataProviderAdapter() {
      public Object get(Object dataHolder) {
        NodeRealizer realizer = view.getGraph2D().getRealizer((Node) dataHolder);
        if (realizer instanceof GenericNodeRealizer){
          if ("JTextField".equals(((GenericNodeRealizer) realizer).getConfiguration())){
            return simpleNodeCellEditor;
          } else {
            return complexNodeCellEditor;
          }
        } else {
          return null;
        }
      }
    };

    EditMode editMode = new EditMode();
    // create the CellEditorMode and give it the multiplexing NodeCellEditor provider,
    // as well as tell it where to find the user data
    CellEditorMode cellEditorMode = new CellEditorMode(nodeCellEditorProvider, NodeCellRendererPainter.USER_DATA_MAP);
    // register it with the EditMode
    editMode.setEditNodeMode(cellEditorMode);
    // Disable generic node label assignment in the view since it would spoil the
    // effect of the node cell editors/renderers.
    editMode.assignNodeLabel(false);

    view.addViewMode( editMode );
  }

  protected void registerViewActions() {
    super.registerViewActions();

    // disable label editing shortcut
    ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      amap.remove(Graph2DViewActions.EDIT_LABEL);
    }
  }

  /** Creates a toolbar that allows to switch the default node realizer type. */
  protected JToolBar createToolBar()
  {
    JToolBar toolBar = super.createToolBar();
    toolBar.addSeparator();
    toolBar.add(new JLabel("Node Style:"));
    toolBar.addSeparator(TOOLBAR_SMALL_SEPARATOR);

    final JComboBox cb = new JComboBox(new Object[]{"JTextField", "JTable", "Rectangle"});
    cb.setMaximumSize(cb.getPreferredSize());
    cb.setSelectedIndex(1);
    toolBar.add(cb);
    cb.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        if ( !"Rectangle".equals( cb.getSelectedItem().toString() ) ) {
          gnr.setConfiguration( cb.getSelectedItem().toString() );
          view.getGraph2D().setDefaultNodeRealizer( gnr );
        } else {
          view.getGraph2D().setDefaultNodeRealizer( snr );
        }
      }
    });

    return toolBar;
  }

  /**
   * A simple {@link NodeCellEditor} implementation that is based on an even simpler
   * {@link NodeCellRenderer} implementation.
   */
  public static class SimpleNodeCellEditor extends AbstractCellEditor implements NodeCellEditor
  {
    // the delegate
    private final SimpleNodeCellRenderer ncr;

    public SimpleNodeCellEditor()
    {
      // initialize
      this.ncr = new SimpleNodeCellRenderer();
      // add editor hooks
      this.ncr.tf.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          SimpleNodeCellEditor.this.fireEditingStopped();
        }
      });
      this.ncr.tf.addKeyListener(new KeyAdapter()
      {
        public void keyPressed(KeyEvent ke)
        {
          if (ke.getKeyCode() ==  KeyEvent.VK_ESCAPE)
          {
            SimpleNodeCellEditor.this.fireEditingCanceled();
          }
        }
      });
    }

    public JComponent getNodeCellEditorComponent(Graph2DView view, NodeRealizer context, Object value, boolean isSelected)
    {
      // get the renderer as editor
      return ncr.getNodeCellRendererComponent(view, context, value, isSelected);
    }

    public Object getCellEditorValue()
    {
      // get the value this editor represents
      return ncr.getValue();
    }
  }

  /**
   * A simple NodeCellRenderer that uses a JTextField and a JLabel in a JPanel to display the nodes contents.
   */
  public static final class SimpleNodeCellRenderer extends JPanel implements NodeCellRenderer
  {
    /**
     * the text field that holds/displays the actual data
     */
    JTextField tf;

    public SimpleNodeCellRenderer()
    {
      super(new BorderLayout());
      // create a nice GUI
      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createEtchedBorder()));
      add(new JLabel("Content"), BorderLayout.NORTH);

      // create a document which is configured for bidirectional text
      // this is done to leverage a little known side effect of bidirectional
      // text rendering: text components render bidirectional text with floating
      // point precision
      // floating point precision text rendering is important for proper
      // zooming of text components
      final Document document = new PlainDocument();
      document.putProperty("i18n", Boolean.TRUE);

      add(tf = new JTextField(document, null, 0), BorderLayout.CENTER);

      // turn off the internal bitmap-based double-buffering for JComponent
      // nodes which is only needed for text component that do not use
      // floating point precision text rendering
      putClientProperty("NodeCellRenderer.noImage", Boolean.TRUE);
    }

    public JComponent getNodeCellRendererComponent(Graph2DView view, NodeRealizer nodeRealizer, Object userObject, boolean selected)
    {
      // initialize the text field
      tf.setText(String.valueOf(userObject));
      return this;
    }

    public Object getValue()
    {
      // return the value of the text field
      return tf.getText();
    }
  }

  /**
   * A more sophisticated NodeCellEditor that uses a sophisticated NodeCellRenderer to
   * display/edit a node.
   * This implementation displays an editable JTable where the value column is editable.
   */
  public static class ComplexNodeCellEditor extends AbstractCellEditor implements NodeCellEditor
  {
    // the delegate
    private final ComplexNodeCellRenderer ncr;

    public ComplexNodeCellEditor()
    {
      this.ncr = new ComplexNodeCellRenderer();
      // add editor hooks
      this.ncr.table.addPropertyChangeListener("tableCellEditor", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getNewValue() == null && evt.getOldValue() != null){
            ComplexNodeCellEditor.this.fireEditingStopped();
          }
        }
      });
    }

    /**
     * Delegates the request to the table.
     */
    public boolean stopCellEditing() {
      if (ncr.table.isEditing() && ncr.table.getCellEditor() != null){
        return ncr.table.getCellEditor().stopCellEditing();
      } else {
        fireEditingStopped();
        return true;
      }
    }

    /**
     * Delegates the request to the table.
     */
    public void cancelCellEditing() {
      if (ncr.table.isEditing() && ncr.table.getCellEditor() != null){
        ncr.table.getCellEditor().cancelCellEditing();
      } else {
        fireEditingCanceled();
      }
    }

    public JComponent getNodeCellEditorComponent(Graph2DView view, NodeRealizer context, Object value, boolean isSelected)
    {
      ncr.getNodeCellRendererComponent(view, context, value, isSelected);
      return ncr;
    }

    public Object getCellEditorValue()
    {
      return ncr.getValue();
    }
  }

  /**
   * A nice renderer that can be used to display data in a JTable
   */
  public static final class ComplexNodeCellRenderer extends JPanel implements NodeCellRenderer
  {
    // the table
    JTable table;
    // the data model
    DefaultTableModel tableModel;

    public ComplexNodeCellRenderer()
    {
      super(new BorderLayout());

      // create a sample table model with the first column being editable
      tableModel = new DefaultTableModel(new Object[][]{{"Keys", "Values"}}, new Object[]{"Key", "Value"}) {
        public boolean isCellEditable(int row, int column) {
          return column == 1;
        }
      };

      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3,3,3,3), BorderFactory.createEtchedBorder()));
      add(table = new JTable(tableModel) {
        /**
         * Configures text component based editors for bidirectional text.
         * This is done to leverage a little known side effect bidirectional
         * text rendering: text components render bidirectional text with
         * floating point precision. Floating point precision text rendering is
         * important for proper zooming of text components.
         */
        public Component prepareEditor(
                final TableCellEditor editor, final int row, final int column
        ) {
          if (editor instanceof DefaultCellEditor) {
            final Component c = ((DefaultCellEditor) editor).getComponent();
            if (c instanceof JTextComponent) {
              final Document d = ((JTextComponent) c).getDocument();
              d.putProperty("i18n", Boolean.TRUE);
            }
          }
          return super.prepareEditor(editor, row, column);
        }
      }, BorderLayout.CENTER);
      add(table.getTableHeader(), BorderLayout.NORTH);

      // turn off the internal bitmap-based double-buffering for JComponent
      // nodes which is only needed for text component that do not use
      // floating point precision text rendering
      putClientProperty("NodeCellRenderer.noImage", Boolean.TRUE);
    }

    public JComponent getNodeCellRendererComponent(Graph2DView view, NodeRealizer nodeRealizer, Object userObject, boolean selected)
    {
      // initialize the value in the model
      tableModel.setValueAt(userObject, 0, 1);
      return this;
    }

    public Object getValue()
    {
      // construct the value from the model
      return tableModel.getValueAt(0, 1);
    }
  }


  /**
   * Launches this demo.
   *
   * @param args ignored command line arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SwingRendererDemo()).start("Swing Renderer Demo");
      }
    });
  }
}
