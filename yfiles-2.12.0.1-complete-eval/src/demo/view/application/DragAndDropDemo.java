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
import y.base.Node;
import y.option.RealizerCellRenderer;
import y.view.Arrow;
import y.view.BezierEdgeRealizer;
import y.view.Drawable;
import y.view.DropSupport;
import y.view.EdgeRealizer;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.QuadCurveEdgeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.SmartNodeLabelModel;
import y.view.SplineEdgeRealizer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Demo that shows how to display and drag different {@link NodeRealizer} and
 * {@link EdgeRealizer} instances from a list and how to drop them onto a
 * {@link Graph2DView} using a {@link Drawable} that indicates the drop
 * operation. Additionally, {@link DropSupport} is configured to split an edge
 * when a node is dropped on it.
 * Moreover, using snap lines for node drag and drop is demonstrated.
 * This demo makes use of the {@link java.awt.dnd.DnDConstants java.awt.dnd} package.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_view.html#dragdrop">Section Drag and Drop Support</a> in the yFiles for Java Developer's Guide
 */
public class DragAndDropDemo extends DemoBase {
  private final DragAndDropSupport dndSupport;

  /** Creates a new instance of DragAndDropDemo */
  public DragAndDropDemo() {
    // create the customized DnD support instance
    dndSupport = createDragAndDropSupport();

    // get the List UI
    final JList realizerList = dndSupport.getList();

    //add the realizer list to the panel
    contentPane.add(new JScrollPane(realizerList), BorderLayout.WEST);
  }

  /**
   * Creates the demo's Drag and Drop support class.
   * @return a <code>DragAndDropSupport</code> instance.
   */
  protected DragAndDropSupport createDragAndDropSupport() {
    return new DragAndDropSupport(createRealizers(), view);
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    final JToggleButton snapLineButton = new JToggleButton(new AbstractAction("Snapping") {
      public void actionPerformed(ActionEvent e) {
        dndSupport.configureSnapping(((AbstractButton) e.getSource()).isSelected(), 30.0, 15.0, true);
      }
    });
    snapLineButton.setIcon(getIconResource("resource/mode_snapping.png"));

    final JToolBar toolbar = super.createToolBar();
    toolbar.addSeparator();
    toolbar.add(snapLineButton);
    return toolbar;
  }

  /**
   * Creates a collection of realizer
   * instance. The realizer instances have different shapes
   * and colors.
   */
  protected Collection createRealizers()
  {
    List result = new ArrayList();

    Map shapeTypeToStringMap = ShapeNodeRealizer.shapeTypeToStringMap();
    float hueIncrease = 1.0f / (float) shapeTypeToStringMap.size();
    float hue = 0.0f;
    for (Iterator iter = shapeTypeToStringMap.keySet().iterator(); iter.hasNext(); hue += hueIncrease) {
      Byte shapeType = (Byte) iter.next();
      ShapeNodeRealizer r = new ShapeNodeRealizer(shapeType.byteValue());
      r.setWidth(100.0);
      r.setLabelText((String) shapeTypeToStringMap.get(shapeType));
      r.setFillColor(new Color(Color.HSBtoRGB(hue, 0.5f, 1.0f)));
      NodeLabel label = r.getLabel();
      SmartNodeLabelModel model = new SmartNodeLabelModel();
      label.setLabelModel(model, model.getDefaultParameter());
      result.add(r);
    }

    final PolyLineEdgeRealizer smoothedPolyLine = new PolyLineEdgeRealizer();
    smoothedPolyLine.setSmoothedBends(true);

    List edgeRealizers = new ArrayList();
    edgeRealizers.add(new PolyLineEdgeRealizer());
    edgeRealizers.add(smoothedPolyLine);
    edgeRealizers.add(new QuadCurveEdgeRealizer());
    edgeRealizers.add(new BezierEdgeRealizer());
    edgeRealizers.add(new SplineEdgeRealizer());

    // Set the target arrow for the edge realizers.
    for (Iterator iterator = edgeRealizers.iterator(); iterator.hasNext();) {
      EdgeRealizer edgeRealizer = (EdgeRealizer) iterator.next();
      edgeRealizer.setTargetArrow(Arrow.STANDARD);
    }

    result.addAll(edgeRealizers);

    return result;
  }

  /**
   * Support class that be used to create a JList that contains NodeRealizers that can be dragged
   * and dropped onto the given Graph2DView object.
   */
  public static class DragAndDropSupport {
    protected JList realizerList;
    protected DropSupport dropSupport;


    public DragAndDropSupport(Collection realizerList, final Graph2DView view) {
      this(realizerList.toArray(), view);
    }

    public DragAndDropSupport(Object[] realizers, final Graph2DView view) {
      this(realizers, view, 120, 45);
    }

    public DragAndDropSupport(Object[] realizers, final Graph2DView view, int itemWidth, int itemHeight) {
      initializeDropSupport(view);
      initializeRealizerList(realizers, view, itemWidth, itemHeight);
      initializeDragSource();
    }

    /**
     * Creates the drop support class that can be used for dropping realizers onto the Graph2DView.
     */
    protected void initializeDropSupport(final Graph2DView view) {
      dropSupport = new DropSupport(view) {
        protected Node createNode(Graph2DView view, NodeRealizer r, double worldCoordX, double worldCoordY) {
          final Node node = super.createNode(view, r, worldCoordX, worldCoordY);
          nodeCreated(node, worldCoordX, worldCoordY);
          return node;
        }
      };

      dropSupport.setPreviewEnabled(true);
      dropSupport.setIndicatingSourceNode(true);

      // when a node is dropped on an edge, split the edge
      dropSupport.setEdgeSplittingEnabled(true);
      // when an edge is split, remove bends that lie inside the dropped node
      dropSupport.getEdgeSplitSupport().setRemovingInnerBends(true);
    }

    /**
     * Creates a nice GUI for displaying NodeRealizers.
     */
    protected void initializeRealizerList(Object[] realizers, final Graph2DView view, int itemWidth, int itemHeight) {
      realizerList = new JList(realizers);
      realizerList.setCellRenderer(createCellRenderer(itemWidth, itemHeight));

      // set the currently selected NodeRealizer as default nodeRealizer
      realizerList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (realizerList.getSelectedValue() instanceof NodeRealizer) {
            nodeRealizerSelected(view, (NodeRealizer) realizerList.getSelectedValue());
          } else if (realizerList.getSelectedValue() instanceof EdgeRealizer) {
            edgeRealizerSelected(view, (EdgeRealizer) realizerList.getSelectedValue());
          }
        }
      });

      realizerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      realizerList.setSelectedIndex(0);
    }

    /**
     * Defines the realizer list to be the drag source use the string-valued name of the realizer as transferable.
     */
    protected void initializeDragSource() {
      final DragSource dragSource = new DragSource();
      dragSource.createDefaultDragGestureRecognizer(realizerList, DnDConstants.ACTION_MOVE,
          new DragGestureListener() {
            public void dragGestureRecognized(DragGestureEvent event) {
              final Object value = realizerList.getSelectedValue();
              if (value instanceof NodeRealizer) {
                NodeRealizer nr = (NodeRealizer) value;
                // use the drop support class to initialize the drag and drop operation.
                dropSupport.startDrag(dragSource, nr, event, DragSource.DefaultMoveDrop);
              } else if (value instanceof EdgeRealizer) {
                EdgeRealizer nr = (EdgeRealizer) value;
                // use the drop support class to initialize the drag and drop operation.
                dropSupport.startDrag(dragSource, nr, event, DragSource.DefaultMoveDrop);
              }
            }
          });
    }

    /**
     * Configures the {@link DropSupport}of this class according to the specified snapping configuration.
     */
    public void configureSnapping(final SnappingConfiguration config, final boolean previewEnabled) {
      dropSupport.setSnappingEnabled(config.isSnappingEnabled() || config.isGridSnappingEnabled());
      config.configureSnapContext(dropSupport.getSnapContext());
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Configures the {@link DropSupport}of this class according to the specified parameters.
     */
    public void configureSnapping(final boolean snapping, final double nodeToNodeDistance,
                                  final double nodeToEdgeDistance, final boolean previewEnabled) {
      dropSupport.setSnappingEnabled(snapping);
      dropSupport.getSnapContext().setNodeToNodeDistance(nodeToNodeDistance);
      dropSupport.getSnapContext().setNodeToEdgeDistance(nodeToEdgeDistance);
      dropSupport.getSnapContext().setUsingSegmentSnapLines(snapping);
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Creates the realizer cell renderer used by this class.
     */
    protected RealizerCellRenderer createCellRenderer(int itemWidth, int itemHeight) {
      return new RealizerCellRenderer(itemWidth, itemHeight);
    }

    protected void nodeCreated(Node node, double worldCoordX, double worldCoordY) {
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList.
     * This method sets the given NodeRealizer as the view's graph default node realizer.
     */
    protected void nodeRealizerSelected(Graph2DView view, NodeRealizer realizer) {
      view.getGraph2D().setDefaultNodeRealizer(realizer);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList.
     * This method sets the given EdgeRealizer as the view's graph default node realizer.
     */
    protected void edgeRealizerSelected(Graph2DView view, EdgeRealizer realizer) {
      view.getGraph2D().setDefaultEdgeRealizer(realizer);
    }

    /**
     * Return the JList that has been configured by this support class.
     */
    public JList getList() {
      return realizerList;
    }
  }

  /**
   * Instantiates and starts this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new DragAndDropDemo()).start("Drag and Drop Demo");
      }
    });
  }
}
