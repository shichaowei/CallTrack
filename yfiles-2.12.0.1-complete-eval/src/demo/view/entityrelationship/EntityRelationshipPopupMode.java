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
package demo.view.entityrelationship;

import demo.view.entityrelationship.painters.ErdRealizerFactory;
import y.base.Edge;
import y.geom.YPoint;
import y.option.RealizerCellRenderer;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.PopupMode;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

/**
 * This popup mode creates a menu for arrow selection for an edge's source and target in
 * entity relationship diagrams (ERD).
 * There are two lists of <code>Arrows</code>, one to select the source arrow the other
 * to select the target arrow.
 */
class EntityRelationshipPopupMode extends PopupMode {

  /**
   * Creates a popup menu for the given edge that allows selecting arrows for source and target
   * @param e the edge which arrow shall be set
   * @return a popup menu
   */
  public JPopupMenu getEdgePopup(Edge e) {
    JPopupMenu pm = new JPopupMenu();
    if (e != null) {
      JMenu sourceArrow = new JMenu("Source Arrow");
      final JList sourceList = createArrowList(true);
      final MouseHandler sourceHandler = new MouseHandler(pm, e, true);
      sourceList.addMouseListener(sourceHandler);
      sourceList.addMouseMotionListener(sourceHandler);
      sourceArrow.add(sourceList);
      pm.add(sourceArrow);
      JMenu targetArrow = new JMenu("Target Arrow");
      final JList targetList = createArrowList(false);
      final MouseHandler targetHandler = new MouseHandler(pm, e, false);
      targetList.addMouseListener(targetHandler);
      targetList.addMouseMotionListener(targetHandler);
      targetArrow.add(targetList);
      pm.add(targetArrow);
    }
    return pm;
  }

  /**
   * Creates a list with every arrow that can be used in ERD.
   * @param source <code>true</code> if the source arrow will be set,
   *               <code>false</code> if the target arrow will be set
   * @return a list with every arrow that can be used in ERD
   */
  JList createArrowList(boolean source){
    DefaultListModel listModel = new DefaultListModel();
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.NONE));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE_OPTIONAL));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE_MANDATORY));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY_OPTIONAL));
    listModel.addElement(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY_MANDATORY));
    if(!source){
      for(Enumeration en = listModel.elements(); en.hasMoreElements(); ){
        EdgeRealizer realizer = (EdgeRealizer) en.nextElement();
        realizer.setTargetArrow(realizer.getSourceArrow());
        realizer.setSourceArrow(Arrow.NONE);
      }
    }
    JList arrowList = new JList(listModel);
    arrowList.setCellRenderer(new RealizerCellRenderer(40, 20){

      protected Icon createEdgeRealizerIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        final EdgeRealizerIcon icon = createIcon(realizer, iconWidth, iconHeight);
        icon.setDrawingBends(false);
        return icon;
      }

      private EdgeRealizerIcon createIcon(EdgeRealizer realizer, int iconWidth, int iconHeight) {
        return new EdgeRealizerIcon(realizer, iconWidth, iconHeight){
          protected YPoint calculateSourceBend(EdgeRealizer realizer, int iconWidth, int iconHeight) {
            return new YPoint(0.5 * iconWidth, 0.5 * iconHeight);
          }

          protected YPoint calculateTargetBend(EdgeRealizer realizer, int iconWidth, int iconHeight) {
            return new YPoint(0.5 * iconWidth, 0.5 * iconHeight);
          }
        };
      }

    });
    arrowList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    return arrowList;
  }

  /**
   * This mouse handler selects arrow list items if the mouse is moving above them and sets the appropriate
   * arrow by clicking on an arrow item.
   */
  private static final class MouseHandler extends MouseAdapter implements MouseMotionListener {
    private JPopupMenu pm;
    private Edge edge;
    private boolean source;

    /**
     * Creates a new <code>MouseHandler</code>
     * @param pm the popup menu the handler is registered to
     * @param edge the selected edge
     * @param source <code>true</code> if the source arrow will be changed,
     *               <code>false</code> if the target arrow will be changed.
     */
    public MouseHandler(JPopupMenu pm, Edge edge, boolean source) {
      this.pm = pm;
      this.edge = edge;
      this.source = source;
    }

    /**
     * Selects a list item by dragging the mouse.
     * @param e the mouse event
     */
    public void mouseDragged(MouseEvent e) {
      handleMotionEvent(e);
    }

    /**
     * Selects a list item by moving the mouse.
     * @param e the mouse event
     */
    public void mouseMoved(MouseEvent e) {
      handleMotionEvent(e);
    }

    /**
     * Selects the list item by the location of the mouse event.
     * @param e the mouse event
     */
    private void handleMotionEvent(MouseEvent e) {
      final Object source = e.getSource();
      if(source instanceof JList){
        JList list = (JList) source;
        list.setSelectedIndex(list.locationToIndex(e.getPoint()));
      }
    }

    /**
     * Sets the arrow for the selected edge for a mouse click on an list item.
     * @param e the mouse event
     */
    public void mouseClicked(MouseEvent e) {
      final Object src = e.getSource();
      if(src instanceof JList){
        JList list = (JList) src;
        final EdgeRealizer er = (EdgeRealizer) list.getModel().getElementAt(list.locationToIndex(e.getPoint()));
        final Graph2D graph = (Graph2D) edge.getGraph();
        if (source) {
          graph.getRealizer(edge).setSourceArrow(er.getSourceArrow());
        } else {
          graph.getRealizer(edge).setTargetArrow(er.getTargetArrow());
        }
        graph.updateViews();
        pm.setVisible(false);
      }
    }
  }
}
