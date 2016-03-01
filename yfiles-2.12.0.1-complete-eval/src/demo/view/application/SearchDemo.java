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
package demo.view.application;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.GraphListener;
import y.base.GraphEvent;
import y.view.Drawable;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NavigationMode;
import y.view.NodeRealizer;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Demonstrates how to find nodes in a graph that match a specific criterion
 * and how to visually present all matching nodes in simple way.
 *
 */
public class SearchDemo extends DemoBase {

  private LabelTextSearchSupport support;

  public SearchDemo() {
    this(null);
  }

  public SearchDemo( final String helpFilePath ) {
    // load a sample graph
    loadGraph("resource/SearchDemo.graphml");

    // add rendering hint to enforce proportional text scaling
    view.getRenderingHints().put(
            RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    // register keyboard action for "select next match" and "clear search"
    final LabelTextSearchSupport support = getSearchSupport();
    final ActionMap amap = support.createActionMap();
    final InputMap imap = support.createDefaultInputMap();
    contentPane.setActionMap(amap);
    contentPane.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);

    // display the demo help if possible
    addHelpPane(helpFilePath);
  }

  protected Action createSaveAction() {
    //Overridden method to disable the Save menu in the demo, because it is not an editable demo
    return null;
  }

  protected void registerViewModes() {
    view.addViewMode(new NavigationMode());
  }

  private LabelTextSearchSupport getSearchSupport() {
    if (support == null) {
      support = new LabelTextSearchSupport(view);
    }
    return support;
  }

  /**
   * Overwritten to display a search text field as well as controls for
   * "select next match", "select previous match", and "select all matches".
   */
  protected JToolBar createToolBar() {
    final LabelTextSearchSupport support = getSearchSupport();

    final JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(new JLabel("Find:"));
    bar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    bar.add(support.getSearchField());
    bar.addSeparator(TOOLBAR_SMALL_SEPARATOR);
    bar.add(createActionControl(support.getPreviousAction()));
    bar.add(createActionControl(support.getNextAction()));
    bar.add(createActionControl(support.getSelectAllAction()));
    return bar;
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

  /**
   * Overwritten to request focus for the search text field initially.
   */
  public void addContentTo( final JRootPane rootPane ) {
    super.addContentTo(rootPane);
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        getSearchSupport().getSearchField().requestFocus();
      }
    });
  }

  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new SearchDemo("resource/searchhelp.html")).start();
      }
    });
  }


  /**
   * Utility class that provides methods for searching for nodes that match
   * a given search criterion and for displaying search results.
   */
  public static class SearchSupport {
    private static final Object NEXT_ACTION_ID = "SearchSupport.Next";
    private static final Object CLEAR_ACTION_ID = "SearchSupport.Clear";


    private Action previous;
    private Action next;
    private Action selectAll;
    private Action clear;

    private SearchResult searchResult;

    private final Graph2DView view;

    public SearchSupport( final Graph2DView view ) {
      this.view = view;
      this.view.addBackgroundDrawable(new Marker());
      final Graph2D graph = this.view.getGraph2D();

      // register a listener that updates search results whenever a node
      // is deleted to prevent stale data in the results
      graph.addGraphListener(new GraphListener() {
        public void onGraphEvent( final GraphEvent e ) {
          if (searchResult != null) {
            if (GraphEvent.POST_NODE_REMOVAL == e.getType() ||
                GraphEvent.SUBGRAPH_REMOVAL == e.getType()) {
              final SearchResult oldResult = searchResult;
              searchResult = new SearchResult();
              for (NodeCursor nc = oldResult.nodes(); nc.ok(); nc.next()) {
                final Node node = nc.node();
                if (node.getGraph() == graph) {
                  searchResult.add(node);
                }
              }
            }
          }
        }
      });
    }

    /**
     * Returns the <code>Graph2DView</code> that is associated to the search
     * support.
     * @return the <code>Graph2DView</code> that is associated to the search
     * support.
     */
    public Graph2DView getView() {
      return view;
    }

    /**
     * Returns the current search result or <code>null</code> if there is none.
     * @return the current search result or <code>null</code> if there is none.
     */
    public SearchResult getSearchResult() {
      return searchResult;
    }

    /**
     * Updates the current search result and the enabled states of the support's
     * clear, next, previous, and select all actions.
     * @param query   specifies which nodes to include in the search result.
     * If the specified query is <code>null</code> the current search result
     * is reset to <code>null</code>, too.
     * @param incremental   <code>true</code> if the current search result
     * should be refined using the specified criterion; <code>false</code>
     * if all nodes of the support's associated graph view's graph should be
     * checked.
     * @see #getClearAction()
     * @see #getNextAction()
     * @see #getPreviousAction()
     * @see #getSelectAllAction()
     */
    public void search( final SearchCriterion query, final boolean incremental ) {
      boolean resultChanged = false;
      if (query != null) {
        final Graph2D graph = view.getGraph2D();
        final NodeCursor nc =
                searchResult != null && incremental
                ? searchResult.nodes()
                : graph.nodes();
        final HashSet oldResult =
                searchResult == null
                ? new HashSet()
                : new HashSet(searchResult.asCollection());
        final HashMap node2location = new HashMap();
        searchResult = new SearchResult();
        for (; nc.ok(); nc.next()) {
          final Node node = nc.node();
          if (query.accept(graph, node)) {
            searchResult.add(node);
            final NodeRealizer nr = graph.getRealizer(node);
            node2location.put(node, new Point2D.Double(nr.getX(), nr.getY()));
            if (!oldResult.contains(node)) {
              resultChanged = true;
            }
          }
        }
        searchResult.sort(new Comparator() {
          public int compare( final Object o1, final Object o2 ) {
            final Point2D p1 = (Point2D) node2location.get(o1);
            final Point2D p2 = (Point2D) node2location.get(o2);
            if (p1.getY() < p2.getY()) {
              return -1;
            } else if (p1.getY() > p2.getY()) {
              return 1;
            } else {
              if (p1.getX() < p2.getX()) {
                return -1;
              } else if (p1.getX() > p2.getX()) {
                return 1;
              } else {
                return 0;
              }
            }
          }
        });
        resultChanged |= oldResult.size() != searchResult.asCollection().size();
      } else if (searchResult != null) {
        searchResult = null;
        resultChanged = true;
      }

      if (resultChanged) {
        final boolean state =
                searchResult != null &&
                !searchResult.asCollection().isEmpty();
        if (clear != null) {
          clear.setEnabled(state);
        }
        if (previous != null) {
          previous.setEnabled(state);
        }
        if (next != null) {
          next.setEnabled(state);
        }
        if (selectAll != null) {
          selectAll.setEnabled(state);
        }
      }
    }

    /**
     * Ensures that the specified rectangle is visible in the support's
     * associated graph view.
     * @param bnds   a rectangle in world (i.e. graph) coordinates.
     */
    private void focusView( final Rectangle2D bnds ) {
      if (bnds.getWidth() > 0 && bnds.getHeight() > 0) {
        final double minX = bnds.getX() - MARKER_MARGIN;
        final double w = bnds.getWidth() + 2*MARKER_MARGIN;
        final double maxX = minX + w;
        final double minY = bnds.getY() - MARKER_MARGIN;
        final double h = bnds.getHeight() + 2*MARKER_MARGIN;
        final double maxY = minY + h;

        final int canvasWidth = view.getCanvasComponent().getWidth();
        final int canvasHeight = view.getCanvasComponent().getHeight();
        final Point2D oldCenter = view.getCenter();
        final double oldZoom = view.getZoom();
        double newZoom = oldZoom;
        double newCenterX = oldCenter.getX();
        double newCenterY = oldCenter.getY();
        final Rectangle vr = view.getVisibleRect();

        // determine whether the specified rectangle (plus the marker margin)
        // lies in the currently visible region
        // if not, adjust zoom factor and view port accordingly
        boolean widthFits = true;
        boolean heightFits = true;
        if (vr.getWidth() < w) {
          newZoom = Math.min(newZoom, canvasWidth / w);
          widthFits = false;
        }
        if (vr.getHeight() < h) {
          newZoom = Math.min(newZoom, canvasHeight / h);
          heightFits = false;
        }
        if (widthFits) {
          if (vr.getX() > minX) {
            newCenterX -= vr.getX() - minX;
          } else if (vr.getMaxX() < maxX) {
            newCenterX += maxX - vr.getMaxX();
          }
        } else {
                                           // take scroll bars into account
          newCenterX = bnds.getCenterX() + (view.getWidth() - canvasWidth) * 0.5 / newZoom;
        }
        if (heightFits) {
          if (vr.getY() > minY) {
            newCenterY -= vr.getY() - minY;
          } else if (vr.getMaxY() < maxY) {
            newCenterY += maxY - vr.getMaxY();
          }
        } else {
                                           // take scroll bars into account
          newCenterY = bnds.getCenterY() + (view.getHeight() - canvasHeight) * 0.5 / newZoom;
        }

        if (oldZoom != newZoom ||
            oldCenter.getX() != newCenterX ||
            oldCenter.getY() != newCenterY) {
          // animate the view port change
          view.focusView(newZoom, new Point2D.Double(newCenterX, newCenterY), true);
        } else {
          view.updateView();
        }
      }
    }

    /**
     * Ensures that only the specified node is selected and that the specified
     * node is visible in the support's associated graph view.
     * @param node   the node to select and display.
     */
    private void emphasizeNode( final Node node ) {
      final Graph2D graph = view.getGraph2D();
      graph.unselectAll();
      if (node != null) {
        final NodeRealizer nr = graph.getRealizer(node);
        nr.setSelected(true);
        final Rectangle2D.Double bnds = new Rectangle2D.Double(0, 0, -1, -1);
        nr.calcUnionRect(bnds);
        focusView(bnds);
      } else {
        view.updateView();
      }
    }

    /**
     * Returns the support's associated <em>clear search result</em> action.
     * @return the support's associated <em>clear search result</em> action.
     * @see #createClearAction()
     */
    public Action getClearAction() {
      if (clear == null) {
        clear = createClearAction();
      }
      return clear;
    }

    /**
     * Creates the support's associated <em>clear search result</em> action.
     * The default implementation resets the support's search result to
     * <code>null</code>.
     * @return the support's associated <em>clear search result</em> action.
     */
    protected Action createClearAction() {
      return new AbstractAction("Clear") {
        {
          setEnabled(searchResult != null);
        }

        public void actionPerformed( final ActionEvent e ) {
          if (searchResult != null) {
            search(null, false);
            view.updateView();
          }
        }
      };
    }

    /**
     * Returns the support's associated <em>find previous match</em> action.
     * @return the support's associated <em>find previous match</em> action.
     * @see #createPreviousAction()
     */
    public Action getPreviousAction() {
      if (previous == null) {
        previous = createPreviousAction();
      }
      return previous;
    }

    /**
     * Creates the support's associated <em>find previous match</em> action.
     * @return the support's associated <em>find previous match</em> action.
     */
    protected Action createPreviousAction() {
      return new AbstractAction("Previous", getIconResource("resource/search_previous.png")) {
          {
            setEnabled(searchResult != null);
          }

          public void actionPerformed( final ActionEvent e ) {
            if (searchResult != null) {
              searchResult.emphasizePrevious();
              emphasizeNode(searchResult.emphasizedNode());
            }
          }
        };
    }

    /**
     * Returns the support's associated <em>find next match</em> action.
     * @return the support's associated <em>find next match</em> action.
     * @see #createNextAction()
     */
    public Action getNextAction() {
      if (next == null) {
        next = createNextAction();
      }
      return next;
    }

    /**
     * Creates the support's associated <em>find next match</em> action.
     * @return the support's associated <em>find next match</em> action.
     */
    protected Action createNextAction() {
      return new AbstractAction("Next", getIconResource("resource/search_next.png")) {
          {
            setEnabled(searchResult != null);
          }

          public void actionPerformed( final ActionEvent e ) {
            if (searchResult != null) {
              searchResult.emphasizeNext();
              emphasizeNode(searchResult.emphasizedNode());
            }
          }
        };
    }

    /**
     * Returns the support's associated <em>select all matches</em> action.
     * @return the support's associated <em>select all matches</em> action.
     * @see #createSelectAllAction()
     */
    public Action getSelectAllAction() {
      if (selectAll == null) {
        selectAll = createSelectAllAction();
      }
      return selectAll;
    }

    /**
     * Creates the support's associated <em>select all matches</em> action.
     * @return the support's associated <em>select all matches</em> action.
     */
    protected Action createSelectAllAction() {
      return new AbstractAction("Select All", getIconResource("resource/search_select_all.png")) {
          {
            setEnabled(searchResult != null);
          }

          public void actionPerformed( final ActionEvent e ) {
            if (searchResult != null) {
              final Graph2D graph = view.getGraph2D();
              graph.unselectAll();
              // clear the result set's emphasis pointer
              searchResult.resetEmphasis();
              // select all matching nodes and en passent calculate the result
              // set's bounding box
              final Rectangle2D.Double bnds = new Rectangle2D.Double(0, 0, -1, -1);
              for (NodeCursor nc = searchResult.nodes(); nc.ok(); nc.next()) {
                final NodeRealizer nr = graph.getRealizer(nc.node());
                nr.setSelected(true);
                nr.calcUnionRect(bnds);
              }

              if (bnds.getWidth() > 0 && bnds.getHeight() > 0) {
                // ensure that all selected nodes are visible
                focusView(bnds);
              } else {
                view.updateView();
              }
            }
          }
        };
    }

    /**
     * Creates a preconfigured action map for the support's
     * <em>find next match</em> and <em>clear result</code> actions.
     * @return a preconfigured action map for the support's
     * <em>find next match</em> and <em>clear result</code> actions.
     * @see #getClearAction()
     * @see #getNextAction()
     */
    public ActionMap createActionMap() {
      final ActionMap amap = new ActionMap();
      amap.put(NEXT_ACTION_ID, getNextAction());
      amap.put(CLEAR_ACTION_ID, getClearAction());
      return amap;
    }

    /**
     * Creates a preconfigured input map for the support's
     * <em>find next match</em> and <em>clear result</code> actions.
     * The default implementation maps the <em>find next match</em> action
     * to the <code>F3</code> function key and the <em>clear search result</em>
     * action to the <code>ESCAPE</code> key.
     * @return a preconfigured input map for the support's
     * <em>find next match</em> and <em>clear result</code> actions.
     * @see #getClearAction()
     * @see #getNextAction()
     */
    public InputMap createDefaultInputMap() {
      final InputMap imap = new InputMap();
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), NEXT_ACTION_ID);
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLEAR_ACTION_ID);
      return imap;
    }

    private static final int MARKER_MARGIN = 10;
    private static final Color EMPHASIZE_COLOR = new Color(153,204,0);
    private static final Color HIGHLIGHT_COLOR = DemoDefaults.DEFAULT_CONTRAST_COLOR;

    /**
     * <code>Drawable</code> that highlights search results by drawing a thick,
     * colored border around search result nodes.
     */
    private final class Marker implements Drawable {
      private final RoundRectangle2D.Double marker;

      Marker() {
        marker = new RoundRectangle2D.Double();
      }

      public void paint( final Graphics2D g ) {
        if (searchResult != null && !searchResult.asCollection().isEmpty()) {
          final Color oldColor = g.getColor();

          final Graph2D graph = view.getGraph2D();
          for (NodeCursor nc = searchResult.nodes(); nc.ok(); nc.next()) {
            final Node node = nc.node();
            if (graph.isSelected(node)) {
              g.setColor(EMPHASIZE_COLOR);
            } else {
              g.setColor(HIGHLIGHT_COLOR);
            }

            final NodeRealizer nr = graph.getRealizer(node);
            marker.setRoundRect(
                    nr.getX() - MARKER_MARGIN,
                    nr.getY() - MARKER_MARGIN,
                    nr.getWidth() + 2* MARKER_MARGIN,
                    nr.getHeight() + 2* MARKER_MARGIN,
                    MARKER_MARGIN,
                    MARKER_MARGIN);
            g.fill(marker);
          }

          g.setColor(oldColor);
        }
      }

      public Rectangle getBounds() {
        if (searchResult == null || searchResult.asCollection().isEmpty()) {
          final Point2D center = view.getCenter();
          return new Rectangle(
                  (int) Math.rint(center.getX()),
                  (int) Math.rint(center.getY()),
                  -1,
                  -1);
        } else {
          final Rectangle bnds = new Rectangle(0, 0, -1, -1);
          final Graph2D graph = view.getGraph2D();
          for (NodeCursor nc = searchResult.nodes(); nc.ok(); nc.next()) {
            graph.getRealizer(nc.node()).calcUnionRect(bnds);
          }
          bnds.grow(MARKER_MARGIN, MARKER_MARGIN);
          return bnds;
        }
      }
    }

    /**
     * Stores nodes that make up a <em>search result</em> and manages an
     * emphasis pointer to allow for <em>find next</em> and
     * <em>find previous</em> actions.
     */
    public static final class SearchResult {
      private final NodeList nodes;
      private NodeCursor cursor;
      private Node current;

      SearchResult() {
        nodes = new NodeList();
      }

      /**
       * Add the specified node to the search result set.
       * @param node   the <code>Node</code> to add.
       */
      void add( final Node node ) {
        nodes.add(node);
      }

      /**
       * Returns a cursor over all nodes in the search result set.
       * @return a cursor over all nodes in the search result set.
       */
      public NodeCursor nodes() {
        return nodes.nodes();
      }

      /**
       * Returns the currently emphasized node or <code>null</code> if there is
       * none.
       * @return the currently emphasized node or <code>null</code> if there is
       * none.
       */
      public Node emphasizedNode() {
        return current;
      }

      /**
       * Resets the emphasis cursor, that is calling {@link #emphasizedNode()}
       * afterwards will return <code>null</code>.
       */
      public void resetEmphasis() {
        current = null;
        cursor = null;
      }

      /**
       * Sets the emphasis pointer to the next node in the search result set.
       * If the emphasized node is the last node in the set, this method will
       * set the pointer to the first node in the set.
       */
      public void emphasizeNext() {
        if (cursor == null) {
          if (nodes.isEmpty()) {
            return;
          } else {
            cursor = nodes.nodes();
            cursor.toLast();
          }
        }
        cursor.cyclicNext();
        current = cursor.node();
      }

      /**
       * Sets the emphasis pointer to the previous node in the search result set.
       * If the emphasized node is the first node in the set, this method will
       * set the pointer to the last node in the set.
       */
      public void emphasizePrevious() {
        if (cursor == null) {
          if (nodes.isEmpty()) {
            return;
          } else {
            cursor = nodes.nodes();
            cursor.toFirst();
          }
        }
        cursor.cyclicPrev();
        current = cursor.node();
      }

      /**
       * Sorts the nodes in the search result set according to the order
       * induced by the specified comparator.
       * @param c   the <code>Comparator</code> to sort the nodes in the search
       * result set.
       */
      void sort( final Comparator c ) {
        nodes.sort(c);
      }

      /**
       * Returns a <code>Collection</code> handle for the search result.
       * @return a <code>Collection</code> handle for the search result.
       */
      Collection asCollection() {
        return nodes;
      }
    }

    /**
     * Specifies the contract of search criteria for node searches.
     */
    public static interface SearchCriterion {
      /**
       * Returns <code>true</code> if the specified node should be included
       * in the search result and <code>false</code> otherwise.
       * @param graph   the <code>Graph2D</code> to which the specified node
       * belongs.
       * @param node   the <code>Node</code> to test for inclusion in the
       * search result.
       * @return <code>true</code> if the specified node should be included
       * in the search result and <code>false</code> otherwise.
       */
      public boolean accept( Graph2D graph, Node node );
    }
  }

  /**
   * Search support for finding nodes whose label text contains a specific
   * text string.
   */
  public static final class LabelTextSearchSupport extends SearchSupport {
    private JTextField searchField;

    public LabelTextSearchSupport( final Graph2DView view ) {
      super(view);
    }

    /**
     * Creates a <em>clear search result</em> action that clears the
     * support's associated search text field as well as the search result.
     * @return a <em>clear search result</em> action that clears the
     * support's associated search text field as well as the search result.
     * @see #getSearchField()
     */
    protected Action createClearAction() {
      return new AbstractAction("Clear") {
        {
          setEnabled(getSearchResult() != null);
        }

        public void actionPerformed( final ActionEvent e ) {
          final SearchResult searchResult = getSearchResult();
          if (searchResult != null || searchField != null) {
            if (searchField != null) {
              searchField.setText("");
            } else {
              search(null, false);
            }
            getView().getGraph2D().unselectAll();
            getView().updateView();
          }
        }
      };
    }

    /**
     * Returns a search text field that allows for convenient input of search
     * queries. The search field is configured to automatically update the
     * support's search result whenever its text content changes.
     * @return a search text field that allows for convenient input of search
     * queries.
     */
    public JComponent getSearchField() {
      if (searchField == null) {
        searchField = new JTextField(25);
        searchField.setMaximumSize(searchField.getPreferredSize());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate( final DocumentEvent e ) {
          }

          public void insertUpdate( final DocumentEvent e ) {
            final String text = searchField.getText();
            search(text.length() == 0 ? null : new LabelTextSearchSupport.LabelText(text), true);
            getView().updateView();
          }

          public void removeUpdate( final DocumentEvent e ) {
            final String text = searchField.getText();
            search(text.length() == 0 ? null : new LabelTextSearchSupport.LabelText(text), false);
            getView().updateView();
          }
        });
        searchField.addActionListener(getNextAction());
      }
      return searchField;
    }


    /**
     * <code>SearchCriterion</code> that matches nodes whose default label
     * contains a specific text.
     */
    static final class LabelText implements SearchCriterion {
      private final String query;

      /**
       * Initializes a new <code>LabelText</code> search criterion for the
       * specified query text.
       * @param query   the text that has to be contained in the default labels
       * of nodes which are accepted by the criterion.
       */
      LabelText( final String query ) {
        this.query = query;
      }

      /**
       * Returns <code>true</code> if the specified node's default label
       * contains the criterion's associated query string and <code>false</code>
       * otherwise.
       * @param graph   the <code>Graph2D</code> to which the specified node
       * belongs.
       * @param node   the <code>Node</code> to test for inclusion in the
       * search result.
       * @return <code>true</code> if the specified node's default label
       * contains the criterion's associated query string and <code>false</code>
       * otherwise.
       */
      public boolean accept( final Graph2D graph, final Node node ) {
        final NodeRealizer nr = graph.getRealizer(node);
        if (nr.labelCount() > 0) {
          if (nr.getLabel().getText().indexOf(query) > -1) {
            return true;
          }
        }
        return false;
      }
    }
  }
}
