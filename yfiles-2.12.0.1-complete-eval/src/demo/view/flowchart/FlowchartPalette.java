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
package demo.view.flowchart;

import y.geom.YPoint;
import y.option.RealizerCellRenderer;
import y.view.DropSupport;
import y.view.EdgeRealizer;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import y.view.Graph2D;
import y.view.PolyLineEdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Arrow;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import demo.view.flowchart.painters.FlowchartAnnotationPainter;
import demo.view.flowchart.painters.FlowchartRealizerFactory;

/**
 * This is a component, which represents a palette of flowchart nodes and edges and allows to drag them into a Graph2DView.
 */
public class FlowchartPalette extends JComponent {
  private DragAndDropSupport dropSupport;
  boolean snapMode;

  /**
   * Creates a new FlowchartPalette with a pre-configured list of node and edge realizers.
   */
  public FlowchartPalette(final Graph2DView view) {
    final BorderLayout borderLayout = new BorderLayout();
    borderLayout.setVgap(10);
    this.setLayout(borderLayout);
    this.add(createDefaultPalette(view),BorderLayout.CENTER);
    initializeDefaultRealizers(view);
  }

  /**
   * Returns whether or not snapping is enabled.
   * @return true if snap mode enabled.
   * @see #setSnapMode(boolean)
   */
  public boolean isSnapMode() {
    return snapMode;
  }

  /**
   * Activates/deactivates snapping between graph elements, while dragging of a Flowchart-Node into the view.
   * @param snapMode Whether to enable snapping.
   */
  public void setSnapMode(boolean snapMode) {
    this.snapMode = snapMode;
    dropSupport.configureSnapping(snapMode, 30, 15, true);
  }

  /**
   * Initializes default realizers
   * @param view The respective Graph2DView.
   */
  protected void initializeDefaultRealizers(Graph2DView view) {
    Graph2D graph = view.getGraph2D();
    final EdgeRealizer der = graph.getDefaultEdgeRealizer();
    if (der instanceof PolyLineEdgeRealizer){
      ((PolyLineEdgeRealizer)der).setSmoothedBends(true);
      der.setTargetArrow(Arrow.STANDARD);
    }
  }

  /**
   * Creates a default flowchart realizers palette
   * @param view The respective Graph2DView that is the target of the drag&drop action from the realizer palette.
   */
  private JComponent createDefaultPalette(final Graph2DView view) {

    final ArrayList realizers = new ArrayList();
    addDefaultTemplates(realizers);

    //add the realizer list to the panel
    //create the drag and drop list filled with the available realizer configurations
    dropSupport = new DragAndDropSupport(realizers, view);
    final JList realizerList = dropSupport.getList();
    realizerList.setCellRenderer(new RealizerCellRenderer(60, 45) {
      private Map nodeTips = new HashMap();
      private Map edgeTips = new HashMap();

      protected Icon createEdgeRealizerIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        final EdgeRealizerIcon icon = createIcon(realizer, iconWidth, iconHeight);
        icon.setDrawingBends(false);
        return icon;
      }

      private EdgeRealizerIcon createIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        if (realizer.labelCount() > 0) {
          final String text = realizer.getLabelText();
          if ("No".equalsIgnoreCase(text) || "Yes".equalsIgnoreCase(text)) {
            return new EdgeRealizerIcon(realizer, iconWidth, iconHeight) {
              protected YPoint calculateSourceBend(EdgeRealizer realizer, int iconWidth, int iconHeight) {
                return new YPoint(0.5 * iconWidth, iconHeight - realizer.getLabel().getHeight() - 2);
              }
            };
          }
        }
        return new EdgeRealizerIcon(realizer, iconWidth, iconHeight);
      }

      protected String createEdgeToolTipText(EdgeRealizer realizer) {
        if(edgeTips.containsKey(realizer)){
          return (String) edgeTips.get(realizer);
        }else{
          final String text = FlowchartPalette.this.createEdgeToolTipText(realizer);
          edgeTips.put(realizer, text);
          return text;
        }
      }

      protected String createNodeToolTipText( final NodeRealizer realizer ) {
        if (nodeTips.containsKey(realizer)) {
          return (String) nodeTips.get(realizer);
        } else {
          final String text = FlowchartPalette.this.createNodeToolTipText(realizer);
          nodeTips.put(realizer, text);
          return text;
        }
      }
    });
    realizerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    realizerList.setVisibleRowCount(-1);
    JScrollPane palette = new JScrollPane(realizerList);
    palette.setPreferredSize(new Dimension(220, 300));
    return palette;
  }

  protected String createEdgeToolTipText( final EdgeRealizer realizer ) {
    return null;
  }

  protected String createNodeToolTipText( final NodeRealizer realizer ) {
    if (realizer instanceof GenericNodeRealizer) {
      String s = ((GenericNodeRealizer) realizer).getConfiguration();
      if (s != null) {
        s = s.trim();
        final String prefix = "com.yworks.flowchart.";
        if (s.startsWith(prefix)) {
          s = s.substring(prefix.length());
          final int l = s.length();
          final char[] chars = s.toCharArray();
          final StringBuffer sb = new StringBuffer(s.length() + 4);
          int last = 0;
          String del = "";
          for (int i = 1; i < l; ++i) {
            if (Character.isUpperCase(chars[i])) {
              sb.append(del).append(Character.toUpperCase(chars[last]));
              sb.append(chars, last + 1, i - last - 1);
              last = i;
              del = " ";
            }
          }
          if (last < l) {
            sb.append(del).append(Character.toUpperCase(chars[last]));
            sb.append(chars, last + 1, l - last - 1);
          }
          return sb.toString();
        }
      }
    }
    return null;
  }

  /**
   * Adds default flowchart templates to the palette list.
   *
   * @param realizers The list of all template realizers
   */
  protected void addDefaultTemplates(final List realizers) {
    realizers.add(FlowchartRealizerFactory.createData());
    realizers.add(FlowchartRealizerFactory.createDirectData());
    realizers.add(FlowchartRealizerFactory.createDataBase());
    realizers.add(FlowchartRealizerFactory.createProcess());
    realizers.add(FlowchartRealizerFactory.createDecision());
    realizers.add(FlowchartRealizerFactory.createDocument());
    realizers.add(FlowchartRealizerFactory.createStart1());
    realizers.add(FlowchartRealizerFactory.createStart2());
    realizers.add(FlowchartRealizerFactory.createPredefinedProcess());
    realizers.add(FlowchartRealizerFactory.createStoredData());
    realizers.add(FlowchartRealizerFactory.createInternalStorage());
    realizers.add(FlowchartRealizerFactory.createSequentialData());
    realizers.add(FlowchartRealizerFactory.createManualInput());
    realizers.add(FlowchartRealizerFactory.createCard());
    realizers.add(FlowchartRealizerFactory.createPaperTape());
    realizers.add(FlowchartRealizerFactory.createCloud());
    realizers.add(FlowchartRealizerFactory.createDelay());
    realizers.add(FlowchartRealizerFactory.createDisplay());
    realizers.add(FlowchartRealizerFactory.createManualOperation());
    realizers.add(FlowchartRealizerFactory.createPreparation());
    realizers.add(FlowchartRealizerFactory.createLoopLimit());
    realizers.add(FlowchartRealizerFactory.createLoopLimitEnd());
    realizers.add(FlowchartRealizerFactory.createTerminator());
    realizers.add(FlowchartRealizerFactory.createOnPageReference());
    realizers.add(FlowchartRealizerFactory.createOffPageReference());
    realizers.add(FlowchartRealizerFactory.createAnnotation(FlowchartAnnotationPainter.PROPERTY_ORIENTATION_VALUE_AUTO));
    realizers.add(FlowchartRealizerFactory.createUserMessage());
    realizers.add(FlowchartRealizerFactory.createNetworkMessage());
    realizers.add(FlowchartRealizerFactory.createDefaultConnection());
    realizers.add(FlowchartRealizerFactory.createNoConnection());
    realizers.add(FlowchartRealizerFactory.createYesConnection());
  }


  private static final class DragAndDropSupport {
    private final JList realizerList;
    private DropSupport dropSupport;

    public DragAndDropSupport(Collection realizers, final Graph2DView view) {
      // create the drop support class that can be used for dropping realizers
      // onto the Graph2DView
      dropSupport = new DropSupport(view);

      dropSupport.setPreviewEnabled(true);

      // when a node is dropped on an edge, split the edge
      dropSupport.setEdgeSplittingEnabled(true);
      // when an edge is split, remove bends that lie inside the dropped node
      dropSupport.getEdgeSplitSupport().setRemovingInnerBends(true);

      // create a nice GUI for displaying NodeRealizers
      DefaultListModel model = new DefaultListModel();
      for (Iterator it = realizers.iterator(); it.hasNext();) {
        model.addElement(it.next());
      }
      realizerList = new JList(model);
      realizerList.setCellRenderer(new RealizerCellRenderer(120, 45));

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

      // define the realizer list to be the drag source
      // use the string-valued name of the realizer as transferable
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

    public void configureSnapping(final boolean snapping, final int nodeToNodeDistance, final int nodeToEdgeDistance,
                                  final boolean previewEnabled) {
      configureDropSupport(dropSupport, snapping, previewEnabled, nodeToNodeDistance, nodeToEdgeDistance);
    }

    protected static void configureDropSupport(final DropSupport dropSupport, final boolean snapping,
                                               final boolean previewEnabled, final double nodeToNodeDistance,
                                               final double nodeToEdgeDistance) {
      dropSupport.setSnappingEnabled(snapping);
      dropSupport.getSnapContext().setNodeToNodeDistance(nodeToNodeDistance);
      dropSupport.getSnapContext().setNodeToEdgeDistance(nodeToEdgeDistance);
      dropSupport.getSnapContext().setUsingSegmentSnapLines(snapping);
      dropSupport.setPreviewEnabled(previewEnabled);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList. This method sets the given
     * NodeRealizer as the view's graph default node realizer.
     */
    protected void nodeRealizerSelected(Graph2DView view, NodeRealizer realizer) {
      view.getGraph2D().setDefaultNodeRealizer(realizer);
    }

    /**
     * Callback method that is triggered whenever the selection changes in the JList. This method sets the given
     * EdgeRealizer as the view's graph default node realizer.
     */
    protected void edgeRealizerSelected(Graph2DView view, EdgeRealizer realizer) {
      view.getGraph2D().setDefaultEdgeRealizer(realizer);
    }

    /** Return the JList that has been configured by this support class. */
    public JList getList() {
      return realizerList;
    }
  }
}