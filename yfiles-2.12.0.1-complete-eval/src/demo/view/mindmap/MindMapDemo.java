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
package demo.view.mindmap;

import demo.view.DemoBase;
import y.algo.GraphConnectivity;
import y.algo.Trees;
import y.base.Command;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeMap;
import y.base.WrongGraphStructure;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.view.ArcEdgeRealizer;
import y.view.Arrow;
import y.view.BezierPathCalculator;
import y.view.DefaultBackgroundRenderer;
import y.view.EdgeLabel;
import y.view.EditMode;
import y.view.GenericEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.LineType;
import y.view.ShapeNodeRealizer;
import y.view.SmartEdgeLabelModel;
import y.view.YRenderingHints;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Demonstrates how to build a mind mapping tool with yFiles for Java.
 * The design follows the suggestions of Tony Buzan, the inventor of mind maps.
 * <p>
 * You can add and delete items, move items, collapse and expand children,
 * change item colors and label texts, choose different icons and add
 * cross-references between independent items.
 * </p><p>
 * User Interaction
 * </p>
 * <dl>
 * <dt>Adding items</dt>
 * <dd>
 *   Either move the mouse over an existing item and click the green plus symbol
 *   of the overlay controls, or select an existing item and type
 *   <code>INSERT</code> to add a child item and <code>ENTER</code> to add
 *   a sibling item.
 * </dd>
 * <dt>Deleting items</dt>
 * <dd>
 *   Either move the mouse over an existing item and click the red minus symbol
 *   of the overlay controls, or select an existing item and type
 *   <code>DELETE</code> or <code>BACKSPACE</code> to remove the selected item
 *   and all of its children.
 * </dd>
 * <dt>Expanding and collapsing items</dt>
 * <dd>
 *   Either click on the blue arrow symbol below an item with children or
 *   select an item with children and type <code>NUMPAD -</code> to collapse the
 *   item or <code>NUMPAD +</code> to expand the previously collapsed item. 
 * </dd>
 * <dt>Creating cross-references</dt>
 * <dd>
 *   Either move the mouse over an existing item and click the light blue arrow
 *   symbol or select an existing item, then press and hold <code>SHIFT</code>
 *   and click on another item or move the mouse over an existing item, press
 *   and hold <code>SHIFT</code>, then drag the mouse to another item.
 * </dd>
 * <dt>Editing text</dt>
 * <dd>
 *   Either double-click an existing item or select an existing item and type
 *   <code>F2</code>.
 *   Text for cross-references may be added/edited the same way.
 * </dd>
 * <dt>Changing colors</dt>
 * <dd>
 *   Move the mouse over an existing item and click the multi-colored symbol.
 * </dd>
 * <dt>Changing icons</dt>
 * <dd>
 *   Move the mouse over an existing item and click the smiley symbol.
 *   Choosing the crossed out box removes a previously selected icon.
 * </dd>
 * <dt>Changing the mind map structure</dt>
 * <dd>
 *   Dragging an existing item close to another item will reassign it to a
 *   new parent item.
 *   Dragging an existing item away from all other items will delete it once
 *   the mouse is released.
 * </dd>
 * <dt>Loading and Saving mind maps</dt>
 * <dd>
 *   Mind maps may be loaded from and saved as GraphML and FreeMind's native
 *   XML format.
 * </dd>
 * </dl>
 */
public class MindMapDemo extends DemoBase {
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new MindMapDemo("resource/mindmaphelp.html")).start("MindMapDemo");
      }
    });
  }

  public MindMapDemo() {
    this(null);
  }

  public MindMapDemo(final String helpFilePath) {
    new CollapseButton.Handler(view);

    loadFile(getResource("resource/hobbies.graphml"));

    KeyboardHandling.setKeyActions(view);
    configureRendering();
    //restrict zoom level
    final Graph2DViewMouseWheelZoomListener zl = new Graph2DViewMouseWheelZoomListener();
    zl.setMaximumZoom(2.5);
    zl.setMinimumZoom(0.08);
    zl.setCenterZooming(false);
    //prevent scrolling when max/min zoom reached
    MouseWheelListener[] mouseWheelListeners = view.getCanvasComponent().getMouseWheelListeners();
    view.getCanvasComponent().removeMouseWheelListener(mouseWheelListeners[0]);
    zl.addToCanvas(view);

    addHelpPane(helpFilePath);

    //nicer background
    configureBackgroundRenderer();
    getUndoManager().resetQueue();
  }

  protected Graph2DView createGraphView() {
    final Graph2DView view = new Graph2DView(ViewModel.instance.graph);
    view.setFitContentOnResize(true);
    return view;
  }

  /**
   * creates and sets a BackgroundRenderer, that draws a college block like background.
   */
  private void configureBackgroundRenderer() {
    view.setBackgroundRenderer(new DefaultBackgroundRenderer(view) {
      public void paint( Graphics2D gfx, int x, int y, int w, int h ) {
        super.paint(gfx, x, y, w, h);
        final int SQUARE_SIZE = 25;
        final int HOLE_SIZE = 30;
        final Rectangle bounds = view.getBounds();

        gfx.setColor(new Color(174, 174, 174));

        undoWorldTransform(gfx);
        final int width = 5;
        gfx.fillRect(7 * SQUARE_SIZE - (width / 2), 0, width, bounds.y + bounds.height);
        for (int i = 0; i < bounds.height; i += SQUARE_SIZE) {
          gfx.drawLine(0, i, bounds.width + bounds.x, i);
        }
        for (int i = 0; i < bounds.width; i += SQUARE_SIZE) {
          gfx.drawLine(i, 0, i, bounds.y + bounds.height);
        }
        double height = bounds.height / 8;

        gfx.setColor(Color.WHITE);

        gfx.fillOval((int) (1.5 * SQUARE_SIZE), (int) height, HOLE_SIZE, HOLE_SIZE);
        gfx.fillOval((int) (1.5 * SQUARE_SIZE), (int) (height * 3), HOLE_SIZE, HOLE_SIZE);
        gfx.fillOval((int) (1.5 * SQUARE_SIZE), (int) (height * 5), HOLE_SIZE, HOLE_SIZE);
        gfx.fillOval((int) (1.5 * SQUARE_SIZE), (int) (height * 7), HOLE_SIZE, HOLE_SIZE);

        gfx.setColor(new Color(167, 167, 167));
        gfx.drawOval((int) (1.5 * SQUARE_SIZE), (int) height, HOLE_SIZE, HOLE_SIZE);
        gfx.drawOval((int) (1.5 * SQUARE_SIZE), (int) (height * 3), HOLE_SIZE, HOLE_SIZE);
        gfx.drawOval((int) (1.5 * SQUARE_SIZE), (int) (height * 5), HOLE_SIZE, HOLE_SIZE);
        gfx.drawOval((int) (1.5 * SQUARE_SIZE), (int) (height * 7), HOLE_SIZE, HOLE_SIZE);

        redoWorldTransform(gfx);
      }
    });
  }

  /**
   * Configures the rendering order for the mind map.
   * For mind maps, the custom rendering order
   * <ol>
   * <li>normal edges,</li>
   * <li>nodes,</li>
   * <li>cross-reference edges, and</li>
   * <li>node labels</li>
   * </ol>
   * is used.
   */
  private void configureRendering() {
    view.setGraph2DRenderer(new MindMapRenderer());

    // turns off simplified sloppy painting for ShapeNodeRealizer
    // ShapeNodeRealizer is used to represent the root item of the mind map
    view.getRenderingHints().put(
        ShapeNodeRealizer.KEY_SLOPPY_RECT_PAINTING,
        ShapeNodeRealizer.VALUE_SLOPPY_RECT_PAINTING_OFF);

    // turns off node label painting in node realizers
    // MindMapRenderer handles label painting
    view.getRenderingHints().put(
            YRenderingHints.KEY_NODE_LABEL_PAINTING,
            YRenderingHints.VALUE_NODE_LABEL_PAINTING_OFF);
  }

  /**
   * configure custom node and edge realizers
   */
  protected void configureDefaultRealizers() {
    final GenericNodeRealizer.Factory nodeFactory = GenericNodeRealizer.getFactory();
    final Map nodeMap = nodeFactory.createDefaultConfigurationMap();
    nodeMap.put(GenericNodeRealizer.Painter.class, new MindMapNodePainter());
    nodeFactory.addConfiguration("MindMapUnderline", nodeMap);
    final GenericNodeRealizer gnr = new GenericNodeRealizer();
    gnr.setConfiguration("MindMapUnderline");
    gnr.setHeight(24);
    view.getGraph2D().setDefaultNodeRealizer(gnr);

    final GenericEdgeRealizer.Factory edgeFactory = GenericEdgeRealizer.getFactory();
    final Map edgeMap = edgeFactory.createDefaultConfigurationMap();
    edgeMap.put(GenericEdgeRealizer.PathCalculator.class, new BezierPathCalculator());
    edgeMap.put(GenericEdgeRealizer.Painter.class,new GradientEdgePainter());
        edgeFactory.addConfiguration("BezierGradientEdge", edgeMap);
    final GenericEdgeRealizer edgeRealizer = new GenericEdgeRealizer();
    edgeRealizer.setConfiguration("BezierGradientEdge");
    final ArcEdgeRealizer realizer = new ArcEdgeRealizer(ArcEdgeRealizer.FIXED_RATIO);

    final EdgeLabel label = realizer.getLabel();
    final SmartEdgeLabelModel model = new SmartEdgeLabelModel();
    final SmartEdgeLabelModel.ModelParameter modelParameter = new SmartEdgeLabelModel.ModelParameter(0, 1, 0, false,
        SmartEdgeLabelModel.ModelParameter.LEFT, 0);
    label.setLabelModel(model,modelParameter);
    label.setBackgroundColor(Color.WHITE);
    realizer.setTargetArrow(Arrow.DELTA);
    realizer.setLineColor(MindMapUtil.CROSS_EDGE_COLOR);
    realizer.setLineType(LineType.LINE_7);
    view.getGraph2D().setDefaultEdgeRealizer(realizer);
  }

  /**
   * Registers view modes to handle mouse interaction.
   */
  protected void registerViewModes() {
    // registers HoverButton internal view mode
    // said mode has to be the first mode that is registered to ensure that
    // it receives events before all other view modes  
    new HoverButton(view);

    final EditMode editMode = new MoveNodeMode(getUndoManager());
    editMode.getMouseInputMode().setDrawableSearchingEnabled(true);
    editMode.allowMouseInput(true);
    editMode.allowBendCreation(false);
    editMode.allowMovePorts(false);
    editMode.allowNodeCreation(false);
    editMode.allowEdgeCreation(false);
    editMode.allowResizeNodes(false);
    editMode.allowMoveLabels(false);
    view.addViewMode(editMode);
    view.addViewMode(new KeyboardHandling.LabelChangeViewMode());
  }

  /**
   * Creates a custom delete selection action that removes all decendant
   * items along with the selected item.
   */
  protected Action createDeleteSelectionAction() {
    return KeyboardHandling.createDeleteSelectionAction(view);
  }

  /**
   * Saves the current mind map as GraphML or in FreeMind's mind map format.
   */
  public class SaveFileAction extends AbstractAction {
    JFileChooser chooser;

    public SaveFileAction() {
      super("Save...");
      putValue(Action.SHORT_DESCRIPTION, "Save...");
    }

    public void actionPerformed(final ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setDialogTitle("Save Mindmap...");
        //only .mm and .graphml files are allowed
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".mm");
          }

          public String getDescription() {
            return "FreeMind Mindmap (.mm)";
          }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML (.graphml)";
          }
        });
      }

      if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        final boolean useNativeFormat =
                chooser.getFileFilter().getDescription().startsWith("GraphML");

        String fileName = chooser.getSelectedFile().getAbsolutePath();
        //enforce file extension
        if (useNativeFormat) {
          final String suffix = ".graphml";
          if (!fileName.toLowerCase().endsWith(suffix)) {
            fileName += suffix;
          }
        } else {
          final String suffix = ".mm";
          if (!fileName.toLowerCase().endsWith(suffix)) {
            fileName += suffix;
          }
        }

        //choose how to save depending on the file extension
        final IOHandler writer =
                useNativeFormat
                ? (IOHandler) createGraphMLIOHandler()
                : new FreeMindIOHandler();
        try {
          writer.write(view.getGraph2D(), fileName);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * Loads a mind map from GraphML or FreeMind's mind map format.
   */
  public class OpenFileAction extends AbstractAction {
    JFileChooser chooser;

    public OpenFileAction() {
      super("Load...");
      putValue(Action.SHORT_DESCRIPTION, "Load...");
    }

    public void actionPerformed(final ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle("Open Mindmap...");
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept( final File f ) {
            return f.isDirectory() ||
                   f.getName().endsWith(".mm") ||
                   f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "Mindmap (.graphml, .mm)";
          }
        });
      }

      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        try {
          final URL url = chooser.getSelectedFile().toURI().toURL();
          loadFile(url);
        } catch (MalformedURLException mue) {
          mue.printStackTrace();
        }
      }
    }
  }

  private void loadFile( final URL resource ) {
    final ArrayList exceptions = new ArrayList();
    final ViewModel model = ViewModel.instance;

    final Graph2D graph = view.getGraph2D();
    graph.firePreEvent();

    graph.clear();

    final boolean useNativeFormat = resource.getFile().toLowerCase().endsWith(".graphml");
    if (useNativeFormat) {
      try {
        createGraphMLIOHandler().read(graph, resource);
      } catch (IOException ioe) {
        exceptions.add(ioe);
      }
    } else {
      try {
        (new FreeMindIOHandler()).read(graph, resource);
      } catch (IOException ioe) {
        exceptions.add(ioe);
      }
    }

    if (exceptions.isEmpty()) {
      try {
        final Node root = findRoot(graph);
        model.setRoot(root);

        if (!useNativeFormat) {
          MindMapUtil.setRootRealizer(graph, root, graph.getLabelText(root));
          LayoutUtil.layout(graph);
        }
      } catch (WrongGraphStructure wgse) {
        exceptions.add(wgse);
      }
    }

    if (!exceptions.isEmpty()) {
      graph.clear();
      final Node root = graph.createNode();
      MindMapUtil.setRootRealizer(graph, root, "Mind Map");
      model.setRoot(root);

      for (Iterator it = exceptions.iterator(); it.hasNext(); ) {
        ((Exception) it.next()).printStackTrace();
      }

      view.fitContent();
      view.updateView();
    } else if (useNativeFormat) {
      view.fitContent();
      view.updateView();
    }

    graph.firePostEvent();
    model.clearHiddenCrossReferences();
    getUndoManager().resetQueue();
  }

  /**
   * Determines the root item in a mind map.
   * To be considered a mind map, the specified graph has to be a directed tree.
   * However, additional, non-tree edges are allowed if these edges are
   * explicitly marked as such, i.e.
   * {@link ViewModel#isCrossReference(y.base.Edge)} has to return
   * <code>true</code> for all non-tree edges.
   * @param graph the mind map whose root item is to be determined.
   * @return the root item in a mind map.
   * @throws WrongGraphStructure if the specified graph is not a directed tree.
   */
  private Node findRoot( final Graph2D graph ) {
    if (GraphConnectivity.isConnected(graph)) {
      final Graph copy = new Graph();
      final Node[] cNodes = new Node[graph.nodeCount()];
      for (int i = 0; i < cNodes.length; ++i) {
        cNodes[i] = copy.createNode();
      }

      final ViewModel model = ViewModel.instance;
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        final Edge edge = ec.edge();
        if (!model.isCrossReference(edge)) {
          copy.createEdge(
                  cNodes[edge.source().index()],
                  cNodes[edge.target().index()]);
        }
      }

      if (Trees.isRootedTree(copy)) {
        final Node cRoot = Trees.getRoot(copy);
        return graph.getNodeArray()[cRoot.index()];
      }
    }
    throw new WrongGraphStructure("Graph is not a mind map");
  }

  protected Action createLoadAction() {
    return new OpenFileAction();
  }

  protected Action createSaveAction() {
    return new SaveFileAction();
  }

  private class OpenExampleAction extends AbstractAction {
    private String filename;

    public OpenExampleAction(String filename) {
      super(filename);
      this.filename = filename;
    }

    public void actionPerformed(ActionEvent e) {
      loadFile(getResource("resource/" + filename + ".graphml"));
    }
  }

  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = super.createMenuBar();
    JMenu menu = menuBar.getMenu(0);
    JMenuItem item = new JMenuItem(new NewFileAction());
    menu.add(item,0);
    menu = new JMenu("Example Mind Maps");
    menuBar.add(menu);
    menu.add(new OpenExampleAction("hobbies"));
    menu.add(new OpenExampleAction("yFiles"));
    menu.add(new OpenExampleAction("packages"));
    return menuBar;
  }

  private class NewFileAction extends AbstractAction {
    public NewFileAction() {
      super("New");
    }

    public void actionPerformed(ActionEvent e) {
      final Graph2D graph = view.getGraph2D();
      graph.firePreEvent();
      graph.backupRealizers();
      graph.clear();

      final Node root = graph.createNode();
      MindMapUtil.setRootRealizer(graph, root, "Mind Map");
      final ViewModel model = ViewModel.instance;
      getUndoManager().push(new SetRoot(model.getRoot(), root));
      model.setRoot(root);
      graph.firePostEvent();

      view.fitContent();
      view.updateView();
    }
  }

  private static final class SetRoot implements Command {
    private final Node oldRoot;
    private final Node newRoot;

    SetRoot( final Node oldRoot, final Node newRoot ) {
      this.oldRoot = oldRoot;
      this.newRoot = newRoot;
    }

    public void execute() {
    }

    public void undo() {
      ViewModel.instance.setRoot(oldRoot);
    }

    public void redo() {
      ViewModel.instance.setRoot(newRoot);
    }
  }


  protected boolean isClipboardEnabled() {
    return false;
  }


  protected GraphMLIOHandler createGraphMLIOHandler() {
    final GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();

    final NodeMap leftRightMap = LayoutUtil.getLeftRightMap(ViewModel.instance.graph);
    ioHandler.getGraphMLHandler().addOutputDataProvider("Mindmap.LeftSide", leftRightMap, KeyScope.NODE, KeyType.BOOLEAN);
    ioHandler.getGraphMLHandler().addInputDataAcceptor("Mindmap.LeftSide", leftRightMap, KeyScope.NODE, KeyType.BOOLEAN);

    final EdgeMap crossReferences = LayoutUtil.getCrossReferencesMap(ViewModel.instance.graph);
    ioHandler.getGraphMLHandler().addOutputDataProvider("Mindmap.CrossReference", crossReferences, KeyScope.EDGE, KeyType.BOOLEAN);
    ioHandler.getGraphMLHandler().addInputDataAcceptor("Mindmap.CrossReference", crossReferences, KeyScope.EDGE, KeyType.BOOLEAN);

    return ioHandler;
  }
}
