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
import y.base.NodeMap;
import y.layout.tree.ARNodePlacer;
import y.layout.tree.AbstractRotatableNodePlacer.Matrix;
import y.layout.tree.AbstractRotatableNodePlacer.RootAlignment;
import y.layout.tree.AssistantPlacer;
import y.layout.tree.DoubleLinePlacer;
import y.layout.tree.LeftRightPlacer;
import y.layout.tree.NodePlacer;
import y.layout.tree.SimpleNodePlacer;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.PolyLineEdgeRealizer;
import y.view.PopupMode;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/**
 * This demo shows how to use {@link y.layout.tree.GenericTreeLayouter}
 * in conjunction with {@link y.layout.tree.AssistantPlacer}.
 * <br>
 * AssistantPlacer is a special NodePlacer that uses two layout strategies.
 * Depending on the boolean provided through the special DataProvider found at
 * the key {@link y.layout.tree.AssistantPlacer#ASSISTANT_DPKEY},
 * the AssistantPlacer decides how to layout its children.<br>
 * If the boolean is set to true for a specific node, it is interpreted as "assistant."
 * All assistants are placed using the {@link y.layout.tree.LeftRightPlacer}.
 * <br>
 * The other children are placed below the assistants, using the child node
 *  placer of the AssistantPlacer. The child node placer can be set using the
 * method
 * {@link y.layout.tree.AssistantPlacer#setChildNodePlacer(y.layout.tree.NodePlacer)}.
 * <br>
 * This demo offers its functionality via context menus. The actual selected
 * nodes can be marked as assistants or "non-assistants," and the child node
 * placer can be be set this way, too.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/cls_GenericTreeLayouter.html">Section Generic Tree Layout</a> in the yFiles for Java Developer's Guide
 */
public class AssistantPlacerDemo extends AbstractTreeDemo {
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new AssistantPlacerDemo()).start("Assistant Placer Demo");
      }
    });
  }

  private NodeMap isAssistantNodeMap;

  public AssistantPlacerDemo() {
    Graph2D graph = view.getGraph2D();

    isAssistantNodeMap = graph.createNodeMap();
    graph.addDataProvider( AssistantPlacer.ASSISTANT_DPKEY, isAssistantNodeMap );
    graph.addDataProvider(LeftRightPlacer.LEFT_RIGHT_DPKEY, new LeftRightPlacer.LeftRightDataProvider(nodePlacerMap));

    //Realizers
    EdgeRealizer defaultER = graph.getDefaultEdgeRealizer();
    defaultER.setArrow( Arrow.STANDARD );
    ( ( PolyLineEdgeRealizer ) defaultER ).setSmoothedBends( true );
    defaultER.setLineType( LineType.LINE_2 );

    createSampleGraph( view.getGraph2D() );
    calcLayout();
  }

  protected NodePlacer createDefaultNodePlacer() {
    return new AssistantPlacer();
  }

  protected boolean isDeletionEnabled() {
    return false;
  }

  protected boolean isClipboardEnabled() {
    return false;
  }

  private void createSampleGraph( Graph2D graph ) {
    graph.clear();
    Node root = graph.createNode();
    graph.getRealizer( root ).setFillColor( layerColors[ 0 ] );
    nodePlacerMap.set( root, new AssistantPlacer() );
    createChildren( graph, root, 6, 1, 1 );
    calcLayout();
    view.fitContent();
  }

  private void createChildren( Graph2D graph, Node root, int children, int layer, int layers ) {
    for ( int i = 0; i < children; i++ ) {
      Node child = graph.createNode();
      graph.createEdge( root, child );
      graph.getRealizer( child ).setFillColor( layerColors[ layer % layerColors.length ] );

      if ( i % 3 == 0 ) {
        isAssistantNodeMap.setBool(child, true);
      }
      NodePlacer nodePlacer = new AssistantPlacer();
      nodePlacerMap.set( child, nodePlacer );
      if ( layers > 0 ) {
        createChildren( graph, child, children, layer + 1, layers - 1 );
      }
    }
  }

  protected PopupMode createTreePopupMode() {
    return new TreeLayouterPopupMode();
  }

  private final class TreeLayouterPopupMode extends PopupMode {
    private JPopupMenu nodePlacementMenu;
    private JCheckBoxMenuItem checkbox;

    TreeLayouterPopupMode() {
      nodePlacementMenu = new JPopupMenu();

      checkbox = new JCheckBoxMenuItem( "Assistant" );
      nodePlacementMenu.add( checkbox );

      checkbox.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
            Node node = nodeCursor.node();
            isAssistantNodeMap.setBool( node, checkbox.isSelected() );
          }
          calcLayout();
        }
      } );

      nodePlacementMenu.addSeparator();

      JMenu childPlacer = new JMenu( "Child NodePlacers" );
      nodePlacementMenu.add( childPlacer );

      childPlacer.add( new SetNodePlacerAction( "Default" ) {
        protected NodePlacer createNodePlacer() {
          AssistantPlacer assistantPlacer = new AssistantPlacer();
          assistantPlacer.setChildNodePlacer( new SimpleNodePlacer( Matrix.DEFAULT, RootAlignment.CENTER ) );
          return assistantPlacer;
        }
      } );

      childPlacer.add( new SetNodePlacerAction( "Double Line" ) {
        protected NodePlacer createNodePlacer() {
          AssistantPlacer assistantPlacer = new AssistantPlacer();
          assistantPlacer.setChildNodePlacer( new DoubleLinePlacer( Matrix.DEFAULT ) );
          return assistantPlacer;
        }
      } );

      childPlacer.add( new SetNodePlacerAction( "ARNodePlacer" ) {
        protected NodePlacer createNodePlacer() {
          AssistantPlacer assistantPlacer = new AssistantPlacer();
          assistantPlacer.setChildNodePlacer( new ARNodePlacer());
          return assistantPlacer;
        }
      } );
    }

    public JPopupMenu getNodePopup( final Node v ) {
      checkbox.setSelected( isAssistantNodeMap.getBool( v ) );
      return nodePlacementMenu;
    }

    private void updateSelectionState() {
      //Set selection state
      checkbox.setSelected( false );
      for ( NodeCursor nodeCursor = getGraph2D().selectedNodes(); nodeCursor.ok(); nodeCursor.next() ) {
        Node node = nodeCursor.node();
        if ( isAssistantNodeMap.getBool( node ) ) {
          checkbox.setSelected( true );
          break;
        }
      }
    }

    public JPopupMenu getSelectionPopup( double x, double y ) {
      if ( getGraph2D().selectedNodes().ok() ) {
        updateSelectionState();
        return nodePlacementMenu;
      } else {
        return null;
      }
    }
  }
}
