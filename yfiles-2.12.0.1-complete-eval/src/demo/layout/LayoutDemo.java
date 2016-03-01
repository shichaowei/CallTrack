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
package demo.layout;

import demo.view.DemoBase;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.Edge;
import y.base.Node;
import y.layout.AbstractLayoutStage;
import y.layout.BufferedLayouter;
import y.layout.CanonicMultiStageLayouter;
import y.layout.ComponentLayouter;
import y.layout.FixNodeLayoutStage;
import y.layout.GraphLayout;
import y.layout.LayoutGraph;
import y.layout.LayoutStage;
import y.layout.Layouter;
import y.layout.OrientationLayouter;
import y.layout.PartitionLayouter;
import y.layout.PortConstraint;
import y.layout.PortConstraintKeys;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.orthogonal.CompactOrthogonalLayouter;
import y.layout.orthogonal.OrthogonalLayouter;
import y.layout.radial.RadialLayouter;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.seriesparallel.SeriesParallelLayouter;
import y.layout.tree.BalloonLayouter;
import y.layout.tree.DendrogramPlacer;
import y.layout.tree.GenericTreeLayouter;
import y.layout.tree.TreeLayouter;
import y.layout.tree.TreeReductionStage;
import y.util.DataProviderAdapter;
import y.view.CreateEdgeMode;
import y.view.Drawable;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.GraphicsContext;
import y.view.LayoutMorpher;
import y.view.ShapeNodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.YRenderingHints;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

/**
 * This demo shows the main layout styles provided by yFiles for Java:
 * <ul>
 *   <li>Hierarchic Layout</li>
 *   <li>Organic Layout</li>
 *   <li>Orthogonal Layout</li>
 *   <li>Tree Layout</li>
 *   <li>Circular Layout</li>
 *   <li>Balloon Layout</li>
 *   <li>Radial Layout</li>
 * </ul>
 *
 * The demo calculates and displays these layouts one after the other for a given graph. The user can interrupt and
 * continue the animation in order to change the graph.
 */
public class LayoutDemo extends DemoBase {
  private static final int DURATION_CYCLE = 6000;
  private static final int DURATION_LAYOUT_CHANGE = DURATION_CYCLE / 8;
  private static final int DURATION_INITIAL_DELAY = 2000;
  static final int CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT = 0;
  static final int CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_LEFT_TO_RIGHT = 1;
  static final int CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_EDGE_GROUPING = 2;
  static final int CONFIG_SMART_ORGANIC_LAYOUT = 0;
  static final int CONFIG_SMART_ORGANIC_LAYOUT_CLUSTER = 1;
  static final int CONFIG_ORTHOGONAL_LAYOUT = 0;
  static final int CONFIG_ORTHOGONAL_LAYOUT_FACE_MAXIMIZATION = 1;
  static final int CONFIG_ORTHOGONAL_LAYOUT_COMPACT = 2;
  static final int CONFIG_TREE_LAYOUT = 0;
  static final int CONFIG_BALLOON_LAYOUT = 1;
  static final int CONFIG_CIRCULAR_LAYOUT = 0;
  static final int CONFIG_CIRCULAR_LAYOUT_ONE_CIRCLE = 1;

  private final EditMode editMode;
  private final Timer timer;
  private final AnimationPlayer player;
  private JMenuBar menuBar;
  private JToolBar toolBar;
  private boolean isVideoMode;
  private final PlayButton playButton;
  private final RelayoutButton relayoutButton;
  private BufferedLayouter layouter;
  private Graph2DViewMouseWheelZoomListener wheelZoomListener;

  public LayoutDemo() {
    this(null);
  }

  public LayoutDemo(final String helpFile) {
    addHelpPane(helpFile);
    view.setPreferredSize(new Dimension(1200, 900));

    wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
    wheelZoomListener.setCenterZooming(false);

    editMode = createEditMode();
    isVideoMode = true;

    // initialize layouters
    final Layouter[] layouts = {
        createHierarchicLayouter(CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT),
        createSmartOrganicLayouter(CONFIG_SMART_ORGANIC_LAYOUT),
        createOrthogonalLayouter(CONFIG_ORTHOGONAL_LAYOUT),
        createTreeLayouter(CONFIG_TREE_LAYOUT),
        createCircularLayouter(CONFIG_CIRCULAR_LAYOUT),
        createHierarchicLayouter(CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_LEFT_TO_RIGHT),
        createSmartOrganicLayouter(CONFIG_SMART_ORGANIC_LAYOUT_CLUSTER),
        createOrthogonalLayouter(CONFIG_ORTHOGONAL_LAYOUT_FACE_MAXIMIZATION),
        createTreeLayouter(CONFIG_BALLOON_LAYOUT),
        createHierarchicLayouter(CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_EDGE_GROUPING),
        createCircularLayouter(CONFIG_CIRCULAR_LAYOUT_ONE_CIRCLE),
        createDendrogramLayouter(),
        createOrthogonalLayouter(CONFIG_ORTHOGONAL_LAYOUT_COMPACT),
        createRadialLayouter(),
        createSeriesParallelLayouter()
    };

    // initialize layout titles
    final String[] titles = {
      "Hierarchic Layout",
      "Organic Layout",
      "Orthogonal Layout",
      "Tree Layout",
      "Circular Layout",
      "Hierarchic Layout - Left to Right",
      "Organic Layout - Clustering",
      "Orthogonal Layout - Face Maximization",
      "Balloon Layout",
      "Hierarchic Layout - Edge Grouping",
      "Circular Layout - One Circle",
      "Dendrogram Layout",
      "Compact Orthogonal Layout",
      "Radial Layout",
      "Series-Parallel Layout"
    };

    // load example graph
    loadGraph("resource/layoutdemograph.graphml");

    // add data provider for FixNodeLayoutStage to fixate the layouts in the center of their bounds
    view.getGraph2D().addDataProvider(FixNodeLayoutStage.FIXED_NODE_DPKEY, new DataProviderAdapter() {
      public boolean getBool(Object dataHolder) {
        return dataHolder instanceof Node;
      }
    });

    // initialize a drawable for the title showing the name of the current layout
    final LayoutTitle title = new LayoutTitle(DURATION_LAYOUT_CHANGE, view);
    view.addDrawable(title);

    // initialize a button with which the user can pause and continue the layout animation
    playButton = new PlayButton();
    view.addDrawable(playButton);
    // initialize a button with which the user can relayout the graph in edit mode
    relayoutButton = new RelayoutButton();

    // initialize animation player and drawables
    player = new ViewAnimationFactory(view).createConfiguredPlayer();
    player.setBlocking(false);

    // start update layout cycle
    timer = new Timer(DURATION_INITIAL_DELAY, new ActionListener() {
      private int index;

      public void actionPerformed(ActionEvent e) {
        // create concurrent animation of title display and layout calculation
        final CompositeAnimationObject concurrency = AnimationFactory.createConcurrency();
        title.setNextText(titles[index]);
        concurrency.addAnimation(title);
        layouter = new BufferedLayouter(new FixNodeLayoutStage(layouts[index]));
        final GraphLayout graphLayout = layouter.calcLayout(view.getGraph2D());
        final LayoutMorpher morpher = new LayoutMorpher(view, graphLayout);
        morpher.setPreferredDuration(DURATION_LAYOUT_CHANGE);
        concurrency.addAnimation(morpher);

        // start layout
        player.animate(concurrency);

        // next layout index
        index = (index + 1) % layouts.length;
      }
    });
    timer.setDelay(DURATION_CYCLE);
    timer.start();
  }

  /**
   * Overwritten to prevent node creation for mouse clicks on buttons.
   */
  protected EditMode createEditMode() {
    final EditMode mode = new EditMode() {
      protected void paperClicked(final Graph2D graph, final double x, final double y, final boolean modifierSet) {
        if (!playButton.getBounds2D().contains(x, y)
            && !relayoutButton.getBounds2D().contains(x, y)) {
          super.paperClicked(graph, x, y, modifierSet);
        }
      }
    };

    if (mode.getCreateEdgeMode() instanceof CreateEdgeMode) {
      ((CreateEdgeMode) mode.getCreateEdgeMode()).setIndicatingTargetNode(true);
    }
    return mode;
  }

  /**
   * Creates a configured {@link IncrementalHierarchicLayouter} instance.
   *
   * @param configuration number of the configuration to use.
   */
  private static Layouter createHierarchicLayouter(int configuration) {
    final IncrementalHierarchicLayouter layouter = new IncrementalHierarchicLayouter();
    switch (configuration) {
      case CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT:
        ((SimplexNodePlacer) layouter.getNodePlacer()).setBaryCenterModeEnabled(true);
        layouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));
        break;
      case CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_LEFT_TO_RIGHT:
        ((SimplexNodePlacer) layouter.getNodePlacer()).setBaryCenterModeEnabled(true);
        layouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_OCTILINEAR));
        layouter.setLayoutOrientation(OrientationLayouter.LEFT_TO_RIGHT);
        break;
      case CONFIG_INCREMENTAL_HIERARCHIC_LAYOUT_EDGE_GROUPING:
        ((SimplexNodePlacer) layouter.getNodePlacer()).setBaryCenterModeEnabled(true);
        layouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));
        layouter.setLayoutOrientation(OrientationLayouter.BOTTOM_TO_TOP);
        layouter.setAutomaticEdgeGroupingEnabled(true);
        break;
    }
    return layouter;
  }

  /**
   * Creates a configured {@link SmartOrganicLayouter} instance.
   *
   * @param configuration number of the configuration to use.
   */
  private static Layouter createSmartOrganicLayouter(int configuration) {
    final SmartOrganicLayouter layouter = new SmartOrganicLayouter();
    switch (configuration) {
      case CONFIG_SMART_ORGANIC_LAYOUT:
        layouter.setPreferredEdgeLength(40);
        layouter.setNodeSizeAware(true);
        break;
      case CONFIG_SMART_ORGANIC_LAYOUT_CLUSTER:
        layouter.setPreferredEdgeLength(60);
        layouter.setNodeSizeAware(true);
        layouter.setAutoClusteringEnabled(true);
        layouter.setAutoClusteringQuality(0.5);
        layouter.setLayoutOrientation(OrientationLayouter.RIGHT_TO_LEFT);
    }
    return layouter;
  }

  /**
   * Creates a configured {@link OrthogonalLayouter} instance.
   *
   * @param configuration number of the configuration to use.
   */
  private static Layouter createOrthogonalLayouter(int configuration) {
    final OrthogonalLayouter ol;
    switch (configuration) {
      default:
      case CONFIG_ORTHOGONAL_LAYOUT:
        ol = new OrthogonalLayouter();
        ol.setUseRandomization(false);
        ol.setPerceivedBendsOptimizationEnabled(true);
        ol.setGrid(10);
        return ol;
      case CONFIG_ORTHOGONAL_LAYOUT_FACE_MAXIMIZATION:
        ol = new OrthogonalLayouter();
        ol.setUseRandomization(false);
        ol.setUseFaceMaximization(true);
        ol.setLayoutOrientation(OrientationLayouter.LEFT_TO_RIGHT);
        ol.setGrid(10);
        return ol;
      case CONFIG_ORTHOGONAL_LAYOUT_COMPACT:
        final CompactOrthogonalLayouter col = new CompactOrthogonalLayouter();
        col.setGridSpacing(10);
        ol = (OrthogonalLayouter) col.getCoreLayouter();
        ol.setLayoutStyle(OrthogonalLayouter.NORMAL_STYLE);
        final PartitionLayouter.ComponentPartitionPlacer placer = (PartitionLayouter.ComponentPartitionPlacer) col.getPartitionPlacer();
        placer.getComponentLayouter().setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE);
        return col;
    }
  }

  /**
   * Creates a configured {@link GenericTreeLayouter} instance.
   *
   * @param configuration number of the configuration to use.
   */
  private static Layouter createTreeLayouter(int configuration) {
    final Layouter layouter;
    switch (configuration) {
      default:
      case CONFIG_TREE_LAYOUT:
        layouter = new TreeLayouter();
        break;
      case CONFIG_BALLOON_LAYOUT:
        layouter = new BalloonLayouter();
    }
    final TreeReductionStage treeReductionStage = new TreeReductionStage();
    treeReductionStage.setNonTreeEdgeRouter(new OrganicEdgeRouter());
    ((CanonicMultiStageLayouter) layouter).appendStage(treeReductionStage);
    return layouter;
  }

  /**
   * Creates a configured dendrogram layouter. In a dendrogram, all subtrees of a single local root align at their
   * bottom border.
   */
  private static Layouter createDendrogramLayouter() {
    final GenericTreeLayouter layouter = new GenericTreeLayouter();
    layouter.setDefaultNodePlacer(new DendrogramPlacer());
    EdgeRouter nonTreeEdgeRouter = new EdgeRouter();
    nonTreeEdgeRouter.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
    final TreeReductionStage treeReductionStage = new TreeReductionStage();
    treeReductionStage.setNonTreeEdgeRouter(nonTreeEdgeRouter);
    treeReductionStage.setNonTreeEdgeSelectionKey(EdgeRouter.SELECTED_EDGES);
    layouter.appendStage(treeReductionStage);

    // as DendrogramPlacer cannot route edges orthogonally, use EdgeRouter to get orthogonal edges
    // to ensure that tree edges always leave their source at the bottom and enter their target at the top, temporarily
    // add port constraints before invoking the edge router
    LayoutStage stage = new AbstractLayoutStage(new EdgeRouter()) {
      public boolean canLayout(LayoutGraph graph) {
        return canLayoutCore(graph);
      }

      public void doLayout(LayoutGraph graph) {
        // Note: adding and removing data providers like this is possible because the graph has no previous port
        //       constraints or edge groupings assigned because they would be replaced. In that case this layout stage
        //       can either be removed to use the assigned constraints or it has to store them before doLayoutCore and
        //       restore them afterwards.

        // add source port constraint SOUTH
        graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, new DataProviderAdapter() {
          public Object get(Object dataHolder) {
            return PortConstraint.create(PortConstraint.SOUTH, true);
          }
        });
        // add target port constraint NORTH
        graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, new DataProviderAdapter() {
          public Object get(Object dataHolder) {
            return PortConstraint.create(PortConstraint.NORTH, true);
          }
        });
        // add source node as group id to group all its outgoing edges
        graph.addDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY, new DataProviderAdapter() {
          public Object get(Object dataHolder) {
            return ((Edge) dataHolder).source();
          }
        });

        // route edges
        doLayoutCore(graph);

        // remove data providers
        graph.removeDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY);
        graph.removeDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY);
        graph.removeDataProvider(PortConstraintKeys.SOURCE_GROUPID_KEY);
      }
    };
    layouter.appendStage(stage);
    return layouter;
  }

  /**
   * Creates a configured {@link CircularLayouter} instance.
   *
   * @param configuration number of the configuration to use.
   */
  private static Layouter createCircularLayouter(int configuration) {
    final CircularLayouter layouter = new CircularLayouter();
    switch (configuration) {
      case CONFIG_CIRCULAR_LAYOUT:
        break;
      case CONFIG_CIRCULAR_LAYOUT_ONE_CIRCLE:
        layouter.setLayoutStyle(CircularLayouter.SINGLE_CYCLE);
    }
    return layouter;
  }
  
  /**
   * Creates a configured {@link y.layout.radial.RadialLayouter} instance.
   */
  private static Layouter createRadialLayouter() {
    return new RadialLayouter();
  }

  /**
   * Creates a configured {@link SeriesParallelLayouter} instance.
   */
  private static Layouter createSeriesParallelLayouter() {
    final SeriesParallelLayouter layouter = new SeriesParallelLayouter();
    layouter.setGeneralGraphHandlingEnabled(true);
    return layouter;
  }

  /**
   * Creates a menu bar that can be hidden when the layout animation is playing.
   */
  protected JMenuBar createMenuBar() {
    menuBar = super.createMenuBar();
    menuBar.setVisible(false);
    return menuBar;
  }

  /**
   * Creates a tool bar that can be hidden when the layout animation is playing.
   */
  protected JToolBar createToolBar() {
    toolBar = super.createToolBar();
    toolBar.setVisible(false);
    return toolBar;
  }

  /**
   * Overwritten to use the {@link ShapeNodeRealizer} as default node realizer.
   */
  protected void configureDefaultRealizers() {
    final ShapeNodeRealizer realizer = new ShapeNodeRealizer(ShapeNodeRealizer.ROUND_RECT);
    realizer.setSize(37.5, 37.5);
    realizer.setFillColor(new Color(205, 2, 2));
    realizer.removeLabel(0);
    view.getGraph2D().setDefaultNodeRealizer(realizer);
  }

  /**
   * Overwritten to register a {@link ViewMode} that handles all control buttons.
   */
  protected void registerViewModes() {
    view.addViewMode(new ViewMode() {
      /**
       * Overwritten to invoke the action of the button that contains the current mouse position.
       *
       * @param x the x-coordinate of the mouse event in world coordinates.
       * @param y the y-coordinate of the mouse event in world coordinates.
       */
      public void mouseClicked(final double x, final double y) {
        if (playButton.getBounds2D().contains(x, y)) {
          playButton.action();
        } else if (relayoutButton.isVisible() && relayoutButton.getBounds2D().contains(x, y)) {
          relayoutButton.action();
        }
      }

      /**
       * Overwritten to change the state of the buttons into a (not-)hovered state when the button contains the current
       * mouse position.
       *
       * @param x the x-coordinate of the mouse event in world coordinates.
       * @param y the y-coordinate of the mouse event in world coordinates.
       */
      public void mouseMoved(final double x, final double y) {
        playButton.setHovered(playButton.getBounds2D().contains(x, y));
        relayoutButton.setHovered(relayoutButton.getBounds2D().contains(x, y));
      }
    });
  }

  /**
   * Overwritten to avoid that the mouse wheel listener is registered automatically. The mouse wheel listener makes
   * zooming available for the user. Since we only want zooming in edit mode we add and remove the listener manually.
   */
  protected void registerViewListeners() {
  }

  /**
   * Starts the {@link LayoutDemo}.
   */
  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new LayoutDemo("resource/layoutdemohelp.html")).start();
      }
    });
  }

  /**
   * A {@link Drawable} that displays a string on a rectangular background.
   * <p>
   *   The drawable is placed at the upper left corner of the view and is zoom-invariant. As it is also an
   *   {@link AnimationObject} it will change its opacity when played by an {@link AnimationPlayer}.
   * </p>
   */
  private static class LayoutTitle implements Drawable, AnimationObject {
    private static final Color COLOR_BACKGROUND = new Color(0, 139, 139);
    private static final Color COLOR_TEXT = Color.WHITE;
    private static final Font FONT_TEXT = new Font("Arial", Font.PLAIN, 36);
    private static final Insets INSETS_TEXT = new Insets(15, 15, 15, 15);
    private static final int INSET = 10;
    private static final int ARC = 30;

    private final Graph2DView view;
    private final long preferredDuration;

    private String text;
    private String nextText;
    private float opacity;

    private LayoutTitle(long preferredDuration, Graph2DView view) {
      this.preferredDuration = preferredDuration;
      this.view = view;
    }

    public void paint(Graphics2D graphics) {
      // store graphics
      final Color oldColor = graphics.getColor();
      final Font oldFont = graphics.getFont();
      final AffineTransform oldTransform = graphics.getTransform();
      final Composite oldComposite = graphics.getComposite();
      try {
        if (text != null && text.length() > 0) {
          // because this title is zoom invariant we get the view's transform to be able to paint in view-coordinates
          final GraphicsContext context = YRenderingHints.getGraphicsContext(graphics);
          graphics.setTransform(context.getViewTransform());

          // get the fonts measurements to paint the background accordingly
          graphics.setFont(FONT_TEXT);
          final FontMetrics fontMetrics = graphics.getFontMetrics();
          final int stringWidth = (int) fontMetrics.getStringBounds(text, graphics).getWidth();
          final int stringHeight = fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent();

          // background bounds in view-coordinates
          final int x = INSET;
          final int y = INSET;
          final int width = stringWidth + INSETS_TEXT.left + INSETS_TEXT.right;
          final int height = stringHeight + INSETS_TEXT.top + INSETS_TEXT.bottom;

          // paint background
          graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
          graphics.setColor(COLOR_BACKGROUND);
          graphics.fillRoundRect(x, y, width, height, ARC, ARC);

          // paint text
          graphics.setComposite(oldComposite);
          graphics.setColor(COLOR_TEXT);
          graphics.drawString(text, x + INSETS_TEXT.left, y + INSETS_TEXT.top + fontMetrics.getMaxAscent());
        }
      } finally {
        // restore graphics
        graphics.setColor(oldColor);
        graphics.setFont(oldFont);
        graphics.setTransform(oldTransform);
        graphics.setComposite(oldComposite);
      }
    }

    public Rectangle getBounds() {
      final TextLayout textLayout =
          new TextLayout(text, FONT_TEXT, new FontRenderContext(FONT_TEXT.getTransform(), true, true));
      final double backgroundWidth = textLayout.getBounds().getWidth() + INSETS_TEXT.left + INSETS_TEXT.right;
      final double backgroundHeight = textLayout.getBounds().getHeight() + INSETS_TEXT.top + INSETS_TEXT.bottom;

      final int minX = (int) Math.floor(view.toWorldCoordX(INSET));
      final int maxX = (int) Math.ceil(view.toWorldCoordX((int) (INSET + backgroundWidth)));
      final int minY = (int) Math.floor(view.toWorldCoordY(INSET));
      final int maxY = (int) Math.ceil(view.toWorldCoordY((int) (INSET + backgroundHeight)));
      return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public void setNextText(String text) {
      nextText = text;
    }

    public void initAnimation() {
      opacity = 0;
    }

    public void calcFrame(double time) {
      if (time < 0.5) {
        if (text != null) {
          opacity = (float) (1 - 2 *time);
        }
      } else if (time > 0.5) {
        if (!nextText.equals(text)) {
          text = nextText;
        }
        opacity = (float) (2 * time -1);
      }
    }

    public void disposeAnimation() {
    }

    public long preferredDuration() {
      return preferredDuration;
    }
  }

  /**
   * A {@link Drawable} that describes a play button. The button is placed at the lower left corner of the view and
   * is zoom-invariant. A mouse click on the button toggles between video and edit mode.
   */
  private class PlayButton implements Drawable {
    private static final int HEIGHT = 70;
    private static final int WIDTH = 100;
    private static final int INSET = 10;
    private static final int ARC = 30;

    private boolean isHovered;

    /**
     * Returns whether or not the button contains the current mouse position.
     *
     * @return <code>true</code> if the button contains the current mouse position, <code>false</code> otherwise.
     */
    boolean isHovered() {
      return isHovered;
    }

    /**
     * Specifies whether or not the button contains the current mouse position.
     *
     * @param isHovered <code>true</code> if the button contains the current mouse position, <code>false</code>
     *                  otherwise.
     */
    void setHovered(final boolean isHovered) {
      if (this.isHovered != isHovered) {
        view.updateView();
      }
      this.isHovered = isHovered;
    }

    public Rectangle getBounds() {
      final Rectangle2D r = getBounds2D();
      final int minX = (int) Math.floor(r.getX());
      final int maxX = (int) Math.ceil(r.getMaxX());
      final int minY = (int) Math.floor(r.getY());
      final int maxY = (int) Math.ceil(r.getMaxY());
      return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Returns the bounds of the {@link Drawable} in world-coordinates. Should be used for hit and contains tests
     * because the rounding errors are less.
     *
     * @return the bounds of the <code>Drawable</code> in world-coordinates.
     */
    public Rectangle2D getBounds2D() {
      final double minX = view.toWorldCoordX(INSET);
      final double maxX = view.toWorldCoordX(INSET + WIDTH);
      final int y = view.getHeight() - INSET;
      final double minY = view.toWorldCoordY(y - HEIGHT);
      final double maxY = view.toWorldCoordY(y);
      return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public void paint(final Graphics2D graphics) {
      final Color oldColor = graphics.getColor();
      final AffineTransform oldTransform = graphics.getTransform();

      // because this button is zoom invariant we get the view's transform to be able to paint in view-coordinates.
      final GraphicsContext context = YRenderingHints.getGraphicsContext(graphics);
      graphics.setTransform(context.getViewTransform());

      // bounds in view coordinates
      final int x = INSET;
      final int y = view.getHeight() - INSET - HEIGHT;
      final int w = WIDTH;
      final int h = HEIGHT;

      // paint background
      graphics.setColor(isHovered() ? Color.GRAY.brighter() : Color.GRAY);
      graphics.fillRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);

      // paint icon
      graphics.setColor(Color.WHITE);
      if (!isVideoMode) {
        final GeneralPath icon = new GeneralPath();
        icon.moveTo(x + w / 3, y + h / 5);
        icon.lineTo(x + w / 3, y + 4 * h / 5);
        icon.lineTo(x + 2 * w / 3, y + h / 2);
        icon.closePath();
        graphics.fill(icon);
      } else {
        graphics.fillRect(x + w / 3, y + h / 5, w / 9, 3 * h / 5);
        graphics.fillRect(x + 5 * w / 9, y + h / 5, w / 9, 3 * h / 5);
      }

      graphics.setColor(oldColor);
      graphics.setTransform(oldTransform);
    }

    /**
     * Switches between video mode and edit mode.
     */
    void action() {
      if (isVideoMode) {
        view.addViewMode(editMode);
        wheelZoomListener.addToCanvas(view);
        menuBar.setVisible(true);
        toolBar.setVisible(true);
        view.addDrawable(relayoutButton);
        timer.stop();
      } else {
        view.removeViewMode(editMode);
        wheelZoomListener.removeFromCanvas(view);
        view.getGraph2D().unselectAll();
        menuBar.setVisible(false);
        toolBar.setVisible(false);
        view.removeDrawable(relayoutButton);
        timer.start();
      }
      isVideoMode = !isVideoMode;
    }
  }

  /**
   * A {@link Drawable} that describes a layout button. The button is placed beside the play
   * button when the edit mode is running. A mouse click on the button recalculates the current layout.
   */
  private class RelayoutButton implements Drawable {
    private static final int HEIGHT = 70;
    private static final int WIDTH = 100;
    private static final int INSET = 10;
    private static final int ARC = 30;

    private final BasicStroke strokeArc = new BasicStroke(10);
    private boolean isHovered;

    /**
     * Returns whether or not the button contains the current mouse position.
     *
     * @return <code>true</code> if the button contains the current mouse position, <code>false</code> otherwise.
     */
    boolean isHovered() {
      return isHovered;
    }

    /**
     * Specifies whether or not the button contains the current mouse position.
     *
     * @param isHovered <code>true</code> if the button contains the current mouse position, <code>false</code>
     *                  otherwise.
     */
    void setHovered(final boolean isHovered) {
      if (this.isHovered != isHovered) {
        view.updateView();
      }
      this.isHovered = isHovered;
    }

    public Rectangle getBounds() {
      final Rectangle2D r = getBounds2D();
      final int minX = (int) Math.floor(r.getX());
      final int maxX = (int) Math.ceil(r.getMaxX());
      final int minY = (int) Math.floor(r.getY());
      final int maxY = (int) Math.ceil(r.getMaxY());
      return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Returns the bounds of the {@link Drawable} in world-coordinates. Should be used for hit and contains tests
     * because the rounding errors are less.
     *
     * @return the bounds of the <code>Drawable</code> in world-coordinates.
     */
    public Rectangle2D getBounds2D() {
      final double minX = view.toWorldCoordX(2 * INSET + PlayButton.WIDTH);
      final double maxX = view.toWorldCoordX(2 * INSET + PlayButton.WIDTH + WIDTH);
      final int y = view.getHeight() - INSET;
      final double minY = view.toWorldCoordY(y - HEIGHT);
      final double maxY = view.toWorldCoordY(y);
      return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public void paint(final Graphics2D graphics) {
      final Color oldColor = graphics.getColor();
      final AffineTransform oldTransform = graphics.getTransform();
      final Stroke oldStroke = graphics.getStroke();

      // because this button is zoom invariant we get the view's transform to be able to paint in view-coordinates
      final GraphicsContext context = YRenderingHints.getGraphicsContext(graphics);
      graphics.setTransform(context.getViewTransform());

       // bounds in view coordinates
       final int x = 2 * INSET + PlayButton.WIDTH;
       final int y = view.getHeight() - INSET - HEIGHT;
       final int w = WIDTH;
       final int h = HEIGHT;

       // paint background
       graphics.setColor(isHovered() ? Color.GRAY.brighter() : Color.GRAY);
       graphics.fillRoundRect(x, y, WIDTH, HEIGHT, ARC, ARC);

      // paint icon
      graphics.setColor(Color.WHITE);
      graphics.setStroke(strokeArc);
      final int iconSize = w / 3;
      final int iconX = x + (w - iconSize) / 2;
      final int iconY = y + (h - iconSize) / 2;
      graphics.drawArc(iconX, iconY, iconSize, iconSize, 135, 270);

      final GeneralPath arrow = new GeneralPath();
      arrow.moveTo((float) (iconX + iconSize * 0.6), (float) (iconY - iconSize * 0.1));
      arrow.lineTo((float) (iconX + iconSize), (float) (iconY - iconSize * 0.1));
      arrow.lineTo((float) (iconX + iconSize * 0.6), (float) (iconY + iconSize * 0.3));
      arrow.closePath();
      graphics.fill(arrow);

      graphics.setColor(oldColor);
      graphics.setTransform(oldTransform);
      graphics.setStroke(oldStroke);
    }

    boolean isVisible() {
      return !isVideoMode;
    }

    /**
     * Recalculates the layout.
     */
    void action() {
      if (layouter != null) {
        final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();
        final LayoutMorpher morpher = executor.getLayoutMorpher();
        morpher.setPreferredDuration(DURATION_LAYOUT_CHANGE);
        morpher.setKeepZoomFactor(true);
        executor.doLayout(view, layouter);
      }
    }
  }
}
