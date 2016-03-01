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
package demo.view.uml;

import y.base.Node;
import y.base.NodeList;
import y.geom.YPoint;
import y.view.Graph2D;
import y.view.HitInfo;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ViewMode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Custom {@link ViewMode} for special label handling of the UML class nodes:
 * <ul>
 *   <li>The labels for the name, the attributes and the operations open a label editor when double-clicked.</li>
 *   <li>The labels for attribute and operation caption are not editable.</li>
 *   <li>An {@link UmlClassLabelEditMode.LabelChangeHandler label change handler} changes the text of the label after
 *   editing.</li>
 * </ul>
 */
class UmlClassLabelEditMode extends ViewMode {

  /**
   * Reacts to double-clicks on labels for the name, the attributes and the operations by presenting a label editor.
   * Reacts to single-clicks on labels for attributes or operations to select the label.
   *
   * @param x the x-coordinate of the mouse event in world coordinates.
   * @param y the y-coordinate of the mouse event in world coordinates.
   */
  public void mouseClicked(double x, double y) {
    final HitInfo hitInfo = getHitInfo(x, y);
    final NodeRealizer singleClickedNode = getSingleClickedNode(hitInfo);
    if (singleClickedNode != null) {
      if (UmlClassLabelSupport.selectListItemAt(singleClickedNode, x, y)) {
        view.updateView();
      }
    }

    final NodeLabel doubleClickedLabel = getDoubleClickedLabel(hitInfo, x, y);
    if (doubleClickedLabel != null && !UmlClassLabelSupport.isCaptionLabel(doubleClickedLabel)) {
      // Caption labels are not editable.
      editLabel(doubleClickedLabel);
    }
  }

  /**
   * Returns the label at the given location if it has been double clicked; null otherwise.
   */
  private NodeLabel getDoubleClickedLabel(final HitInfo hitInfo, final double x, final double y) {
    if (lastClickEvent != null && lastClickEvent.getClickCount() == 2) {
      if (hitInfo.hasHitNodeLabels()) {
        return hitInfo.getHitNodeLabel();
      } else if (hitInfo.hasHitNodes()) {
        final NodeRealizer realizer = view.getGraph2D().getRealizer(hitInfo.getHitNode());
        for (int i = realizer.labelCount(); i-- > 0; ) {
          final NodeLabel label = realizer.getLabel(i);
          if (label.contains(x, y)) {
            return label;
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns the node realizer at the given position if it has been single clicked; null otherwise.
   */
  private NodeRealizer getSingleClickedNode(final HitInfo hitInfo) {
    if (hitInfo.hasHitNodeLabels()) {
      return hitInfo.getHitNodeLabel().getRealizer();
    } else if (hitInfo.hasHitNodes()) {
      return view.getGraph2D().getRealizer(hitInfo.getHitNode());
    } else {
      return null;
    }
  }

  /**
   * Opens the given label with an label editor
   */
  private void editLabel(final NodeLabel label) {
    if (!UmlClassLabelSupport.isCaptionLabel(label)) {
      final YPoint location = label.getTextLocation();
      view.openLabelEditor(label, location.getX(), location.getY(), new LabelChangeHandler(), true, false);
    }
  }

  /**
   * This handler listens for label changes and adjusts the node size to
   * the label size.
   */
  private class LabelChangeHandler implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      final Object source = e.getSource();
      if (source instanceof NodeLabel) {
        final Graph2D graph = getGraph2D();
        final NodeLabel label = (NodeLabel) source;
        final Node node = label.getNode();
        final NodeRealizer realizer = graph.getRealizer(node);

        // Do not allow an empty name label.
        graph.firePreEvent();
        graph.backupRealizers(new NodeList(node).nodes());
        graph.backupRealizers(node.edges());
        try {
          if ("".equals(e.getNewValue())) {
            // Remove empty label.
            label.setText(" ");
            final UmlClassAnimation animation = new UmlClassRemoveItemButton.RemoveItemAnimation(view, realizer, true);
            animation.play();
          } else {
            // Set the text of the label.
            label.setText((String) e.getNewValue());
            UmlClassLabelSupport.updateLabelText(realizer, label);
            UmlClassLabelSupport.selectLabel(realizer, label);
            UmlClassLabelSupport.updateRealizerSize(realizer);
          }
        } finally {
          graph.firePostEvent();
        }
      }
    }
  }
}