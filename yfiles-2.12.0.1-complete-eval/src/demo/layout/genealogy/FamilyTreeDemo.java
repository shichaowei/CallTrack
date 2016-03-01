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
package demo.layout.genealogy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import demo.layout.genealogy.iohandler.GedcomHandler;
import demo.layout.genealogy.iohandler.GedcomInputHandler;
import demo.layout.genealogy.iohandler.GedcomInputHandlerImpl;
import demo.view.DemoBase;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.layout.genealogy.FamilyTreeLayouter;
import y.layout.LayoutTool;
import demo.layout.module.FamilyTreeLayoutModule;
import y.option.OptionHandler;
import y.util.D;
import y.util.GraphHider;
import y.view.Arrow;
import y.view.BendList;
import y.view.BridgeCalculator;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.GenericEdgePainter;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.NavigationMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodePainter;
import y.view.ShinyPlateNodePainter;
import y.geom.YPoint;


/**
 * This Demo shows how to use the FamilyTreeLayouter.
 * <p>
 * <b>Usage:</b>
 * <br>
 * Load a Gedcom file with "Load..." from the "File" menu. The gedcom file is converted on the fly into a graph
 * by the {@link demo.layout.genealogy.iohandler.GedcomHandler}. After loading,
 * the graph will be laid out by the {@link y.layout.genealogy.FamilyTreeLayouter}.
 * <br>
 * NOTE: You will find some sample files in your &lt;yfiles&gt;/src/demo/layout/genealogy/samples folder.
 * <br>
 * To load one of the four sample graphs provided with this demo, select the
 * corresponding entry from the "Example Graphs" menu. The samples have
 * different sizes and complexities.
 * <br>
 * NOTE: For this feature to work, Gedcom files (ending: .ged) must be exported
 * as resources.
 * <br>
 * To re-layout the graph press the "layout" button. An options dialog will open where you can modify
 * some basic and advanced settings. Clicking "OK" will calculate a new layout with the new settings.
 * <br>
 * Clicking on a node will collapse the graph to two generations around the clicked node.
 * The "Show all" button will expand the graph again
 * </p>
 * <p>
 * API usage:
 * <br>
 * The FamilyTreeLayouter needs to distinguish between family nodes, i.e. nodes representing a FAM entry
 * in the Gedcom file, and nodes representing individuals (i.e. persons, INDI entries). To do so,
 * a data provider with the key {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE} has to be registered
 * to the graph. This data provider will return a String which denotes nodes representing individuals.
 * In this demo, this is achieved by comparing the node's background color with the color, family nodes are
 * painted with ({@link Color#BLACK}).
 * <br>
 * For writing, the GedcomHandler needs to distinguish between family nodes and individuals as well
 * as between male and female individuals. To do so, a data provider with the key
 * {@link y.layout.genealogy.FamilyTreeLayouter#DP_KEY_FAMILY_TYPE} has to be registered to the graph.
 * These data provider will return a String which can be used to distinguish between families, male and female
 * individual.
 * In this demo, this is achieved by comparing the node's background color with predefined values.
 * As this demo is a viewer without editing capabilities, the export function is not implemented.
 * <br>
 * <br>
 * Note that the additional information from the original file can be attached to the graph by overwriting the callback
 * methods in {@link GedcomInputHandlerImpl} and handle the particular tags.
 * In this demo, String attributes whether the node represents a family or a male or female individual are mapped
 * to the graph and can be used as data providers for the layouter and the GedcomHandler.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/domain_specific_layouter.html#familytreee_layouter">Section Family Tree Layout</a> in the yFiles for Java Developer's Guide
 */
public class FamilyTreeDemo extends DemoBase {
  private static final int PREFERRED_FONT_SIZE = 18;
  private static final Color COLOR_FAMILY = Color.BLACK;

  private MyFamilyTreeLayoutModule ftlm;
  private GraphHider graphHider;

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new FamilyTreeDemo().start();
      }
    });
  }

  /**
   * Add a NavigationMode
   */
  protected void registerViewModes() {
    view.addViewMode(new NavigationMode() {
      public void mouseClicked(double x, double y) {
        super.mouseClicked(x, y);
        if(getHitInfo(x,y).getHitNode() != null) {
          setToMain(getLastHitInfo().getHitNode());
        }
      }
    });
  }

  /**
   * Hide nodes that are not related with the given node, i.e.,
   * show only the clicked node, its siblings, parents, parents of parents (recursively) as well as its families, children
   * and children of children (recursively).
   *
   * @param clickedNode The node to be the new main node of the graph
   */
  private void setToMain(final Node clickedNode) {
    final Graph2D graph = view.getGraph2D();
    final YPoint oldCenter = graph.getCenter(clickedNode);

    graphHider.unhideAll();  //undo the previous selection

    //mark nodes that should be shown
    final NodeMap showNodeMap = graph.createNodeMap();
    Node familyNode = (clickedNode.inDegree() > 0) ? clickedNode.firstInEdge().source() : null;
    if(familyNode != null) {
      for (NodeCursor nc = familyNode.successors(); nc.ok(); nc.next()) {
        showNodeMap.setBool(nc.node(), true); //mark clicked node and its siblings
      }
      NodeList queue = new NodeList(familyNode);
      while (!queue.isEmpty()) { //mark all predecessors
        final Node n = queue.popNode();
        if (!showNodeMap.getBool(n)) { //prevents that a node is handled twice -> should not happen in most families ;-)
          showNodeMap.setBool(n, true);
          queue.addAll(n.predecessors());
        }
      }
    } else{
      showNodeMap.setBool(clickedNode, true);
    }

    //also add the successors
    NodeList queue = new NodeList(clickedNode.successors());
    while (!queue.isEmpty()) {
      final Node n = queue.popNode();
      if (!showNodeMap.getBool(n)) {
        showNodeMap.setBool(n, true);
        queue.addAll(n.successors());

        //also show n's direct predecessors, i.e., both parents of a family node
        for (NodeCursor nc = n.predecessors(); nc.ok(); nc.next()) {
          final Node pred = nc.node();
          if (!showNodeMap.getBool(pred)) {
            showNodeMap.setBool(pred, true);
          }
        }
      }
    }

    //hide non marked nodes
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node n = nc.node();
      if(!showNodeMap.getBool(n)) {
        graphHider.hide(n);
      }
    }
    graph.disposeNodeMap(showNodeMap);

    //apply the layout to the subgraph
    getLayoutModule().start(view.getGraph2D());

    //move clicked node to its old position
    final YPoint newCenter = graph.getCenter(clickedNode);
    LayoutTool.moveSubgraph(graph, graph.nodes(), oldCenter.getX() - newCenter.getX(),
        oldCenter.getY() - newCenter.getY());
    view.updateView();
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    final Action layoutAction = new AbstractAction(
            "Layout", SHARED_LAYOUT_ICON) {
      public void actionPerformed(ActionEvent e) {
        OptionSupport.showDialog(getLayoutModule(), view.getGraph2D(), true, view.getFrame());
      }
    };

    final Action showAllAction = new AbstractAction("Show all") {
      public void actionPerformed(ActionEvent e) {
        if (graphHider != null) {
          graphHider.unhideAll();
        }
        getLayoutModule().start(view.getGraph2D());
      }
    };

    JToolBar jToolBar = super.createToolBar();
    jToolBar.addSeparator();
    jToolBar.add(showAllAction);
    //jToolBar.add(new ExportAction());
    jToolBar.addSeparator();
    jToolBar.add(createActionControl(layoutAction));
    return jToolBar;
  }

  /**
   * Overwritten to disable undo/redo because this is not an editable demo.
   */
  protected boolean isUndoRedoEnabled() {
    return false;
  }

  /**
   * Overwritten to disable clipboard because this is not an editable demo.
   */
  protected boolean isClipboardEnabled() {
    return false;
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = super.createMenuBar();
    createExamplesMenu(menuBar);
    return menuBar;
  }

  /**
   * Creates a menu to select the provided samples.
   */
  protected void createExamplesMenu(JMenuBar menuBar) {
    String fqResourceName = FamilyTreeDemo.class.getPackage().getName().replace('.', '/') + "/samples/kennedy_clan.ged";

    URL resource = getResource("samples/kennedy_clan.ged");
    if (resource == null) {
      return;
    }

    final String name = resource.getFile();
    final String dirName = name.substring(0, name.lastIndexOf('/'));

    final String[] dir = new File(dirName).list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".ged");
      }
    });

    if (dir == null) {
      D.showError("Cannot load example files: " + dirName + " not found");
      return;
    }

    if (dir.length > 0) {
      final JMenu menu = new JMenu("Example Graphs");
      menuBar.add(menu);

      for (int i = 0; i < dir.length; i++) {
        final String fileName = dir[i];
        menu.add(new AbstractAction(fileName) {
          public void actionPerformed(ActionEvent e) {
            loadGedcom(dirName + System.getProperty("file.separator") + fileName);
          }
        });
      }

      loadGedcom(dirName + System.getProperty("file.separator") + dir[0]);
    }
  }

  /**
   * Loads a gedcom file with the provided filename into the editor.
   * @param name the filename of the gedcom file to be read.
   */
  private void loadGedcom(String name) {
    final Graph2D graph = view.getGraph2D();
    if (graphHider != null) {
      graphHider.unhideAll();
    }
    graph.clear();
    final NodeMap types = graph.createNodeMap();
    graph.addDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE, types);

    final GedcomHandler gh = new GedcomHandler() {
      public GedcomInputHandler createGedcomHandler(final Graph2D graph) {
        return new GedcomInputHandlerImpl(graph) {
          protected NodeRealizer createIndividualNodeRealizer(Graph2D graph) {
            final GenericNodeRealizer realizer = new GenericNodeRealizer("Individual");
            realizer.setSize(200, 80);
            realizer.setFillColor(Color.WHITE);
            realizer.setFillColor2(null);
            return realizer;
          }

          protected NodeRealizer createFamilyNodeRealizer(Graph2D graph) {
            final GenericNodeRealizer realizer = new GenericNodeRealizer("Family");
            realizer.setSize(15, 15);
            realizer.setFillColor(COLOR_FAMILY);
            return realizer;
          }

          protected EdgeRealizer createWifeFamilyEdgeRealizer(Graph2D graph) {
            return createEdgeRealizer(graph);
          }

          protected EdgeRealizer createHusbandFamilyEdgeRealizer(Graph2D graph) {
            return createEdgeRealizer(graph);
          }

          protected EdgeRealizer createFamilyChildEdgeRealizer(Graph2D graph) {
            return createEdgeRealizer(graph);
          }

          private EdgeRealizer createEdgeRealizer( final Graph2D graph ) {
            final EdgeRealizer realizer = graph.getDefaultEdgeRealizer().createCopy();
            realizer.setSourceArrow(Arrow.NONE);
            realizer.setTargetArrow(Arrow.NONE);
            return realizer;
          }
        };
      }
    };

    try {
      gh.read(graph, name);
    } catch (IOException e1) {
      D.show(e1);
    }

    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      final NodeRealizer realizer = graph.getRealizer(node);
      if (realizer.labelCount() > 0) {
        final NodeLabel label = realizer.getLabel();
        if (label.getFontSize() < PREFERRED_FONT_SIZE) {
          label.setFontSize(PREFERRED_FONT_SIZE);

          if (label.getWidth() + 2*label.getDistance() > realizer.getWidth()) {
            realizer.setWidth(label.getWidth() + 2*label.getDistance() + 8);
          }
        }
      }
    }

    try {
      getLayoutModule().start(view.getGraph2D());
    } catch (Exception e1) {
      D.show(e1);
    }
  }


  /**
   * Overrides the default method which creates the loadGedcom entry in the file menu to import a gedcom file
   * rather than to loadGedcom a graph.
   * @return A new instance of ImportAction
   */
  protected Action createLoadAction() {
    return new ImportAction();
  }

  private MyFamilyTreeLayoutModule getLayoutModule() {
    return ftlm == null ? ftlm = new MyFamilyTreeLayoutModule() : ftlm;
  }

  // this module sets the initial gender colors to the defaults from gedcom
  private class MyFamilyTreeLayoutModule extends FamilyTreeLayoutModule{
    protected OptionHandler createOptionHandler() {
      final OptionHandler options = super.createOptionHandler();
      options.set(ITEM_MALE_COLOR, GedcomInputHandlerImpl.DEFAULT_COLOR_MALE);
      options.set(ITEM_FEMALE_COLOR, GedcomInputHandlerImpl.DEFAULT_COLOR_FEMALE);
      return options;
    }
  }

  /**
   * Initialize the node and edge style
   */
  protected void initialize() {

    // Use a BevelNodePainter for the Individuals
    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map implementationsMap = factory.createDefaultConfigurationMap();
    ShinyPlateNodePainter spnp = new ShinyPlateNodePainter();
    spnp.setDrawShadow(true);
    implementationsMap.put(GenericNodeRealizer.Painter.class, spnp);
    factory.addConfiguration("Individual", implementationsMap);

    // Use a BevelNodePainter with rounded corners for the families
    implementationsMap = factory.createDefaultConfigurationMap();
    ShapeNodePainter painter = new ShapeNodePainter(ShapeNodePainter.ELLIPSE);
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
    factory.addConfiguration("Family", implementationsMap);

    // Crossing/Bridges: Vertical edges over horizontal edges display gaps in horizontal edges
    BridgeCalculator bc = new BridgeCalculator();
    bc.setCrossingStyle(BridgeCalculator.CROSSING_STYLE_GAP);
    bc.setCrossingMode(BridgeCalculator.CROSSING_MODE_ORDER_INDUCED);
    ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bc);

    graphHider = new GraphHider(view.getGraph2D());
  }

  /**
   * A custom EdgePainter implementation that draws the edge path 3D-ish and adds
   * a drop shadow also. (see demo.view.realizer.GenericEdgePainterDemo)
   */
  static final class CustomEdgePainter extends GenericEdgePainter {

    protected GeneralPath adjustPath(EdgeRealizer context, BendList bends, GeneralPath path,
                                     BridgeCalculator bridgeCalculator,
                                     boolean selected) {
      if (bridgeCalculator != null) {
        GeneralPath p = new GeneralPath();
        PathIterator pathIterator = bridgeCalculator.insertBridges(path.getPathIterator(null, 1.0d));
        p.append(pathIterator, true);
        return super.adjustPath(context, bends, p, bridgeCalculator, selected);
      } else {
        return super.adjustPath(context, bends, path, bridgeCalculator, selected);
      }
    }


    protected void paintPath(EdgeRealizer context, BendList bends, GeneralPath path, Graphics2D gfx, boolean selected) {
      Stroke s = gfx.getStroke();
      Color oldColor = gfx.getColor();
      if (s instanceof BasicStroke) {
        Color c;
        if (selected) {
          initializeSelectionLine(context, gfx, selected);
          c = gfx.getColor();
        } else {
          initializeLine(context, gfx, selected);
          c = gfx.getColor();
          gfx.setColor(new Color(128, 128, 128, 40));
          gfx.translate(4, 4);
          gfx.draw(path);
          gfx.translate(-4, -4);
        }
        Color newC = selected ? Color.RED : c;
        gfx.setColor(new Color(128 + newC.getRed() / 2, 128 + newC.getGreen() / 2, 128 + newC.getBlue() / 2));
        gfx.translate(-1, -1);
        gfx.draw(path);
        gfx.setColor(new Color(newC.getRed() / 2, newC.getGreen() / 2, newC.getBlue() / 2));
        gfx.translate(2, 2);
        gfx.draw(path);
        gfx.translate(-1, -1);
        gfx.setColor(c);
        gfx.draw(path);
        gfx.setColor(oldColor);
      } else {
        gfx.draw(path);
      }
    }
  }



  /**
   * Action that loads a Gedcom file.
   */
  public class ImportAction extends AbstractAction {
    JFileChooser chooser;

    public ImportAction() {
      super("Load...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".ged");
          }

          public String getDescription() {
            return "Gedcom files";
          }
        });
      }

      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        loadGedcom(chooser.getSelectedFile().toString());
      }
    }
  }

  /**
   * Action to export the graph into a gedcom file
   * NOTE: This action is not added to the toolbar
   */
  protected class ExportAction extends AbstractAction {
    JFileChooser chooser;

    public ExportAction() {
      super("Export");
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".ged");
          }

          public String getDescription() {
            return "Gedcom files";
          }
        });
      }

      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        if (!name.toLowerCase().endsWith(".ged")) {
          name = name + ".ged";
        }

        GedcomHandler gh = new GedcomHandler();
        final Graph2D graph = view.getGraph2D();

        // Write the graph using the GedcomHandler
        try {
          gh.write(graph, name);
        } catch (IOException e1) {
          D.show(e1);
        }
      }
    }
  }
}
