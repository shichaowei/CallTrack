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
package demo.layout.labeling;

import y.geom.OrientedRectangle;
import y.geom.YDimension;
import y.base.YList;
import y.base.ListCell;
import y.layout.EdgeLabelModel;
import y.layout.EdgeLayout;
import y.layout.NodeLayout;
import y.layout.EdgeLabelLayout;
import y.layout.EdgeLabelCandidate;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class CompositeEdgeLabelModel implements EdgeLabelModel {

  private List models = new ArrayList();

  public Object getDefaultParameter() {
    final EdgeLabelModel model = (EdgeLabelModel) models.get(0);
    return new ModelParameter(model, model.getDefaultParameter());
  }

  public void add(EdgeLabelModel model){
    this.models.add(model);
  }

  public OrientedRectangle getLabelPlacement(YDimension labelSize, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                             NodeLayout targetLayout, Object param) {
    ModelParameter p = (ModelParameter) param;
    return p.model.getLabelPlacement(labelSize, edgeLayout, sourceLayout, targetLayout, p.parameter);
  }

  public YList getLabelCandidates(EdgeLabelLayout labelLayout, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                  NodeLayout targetLayout) {

    final YList list = new YList();
    for (Iterator it = models.iterator(); it.hasNext();) {
      EdgeLabelModel model = (EdgeLabelModel) it.next();
      final YList labelCandidates = model.getLabelCandidates(labelLayout, edgeLayout, sourceLayout, targetLayout);
      for (ListCell listCell = labelCandidates.firstCell(); listCell != null; listCell = listCell.succ()) {
        EdgeLabelCandidate candidate = (EdgeLabelCandidate) listCell.getInfo();
        ModelParameter newParam = new ModelParameter(model, candidate.getParameter());
        list.add(new EdgeLabelCandidate(candidate.getBox(), newParam, labelLayout, candidate.isInternal()));
      }
    }
    return list;
  }

  public Object createModelParameter(OrientedRectangle labelBounds, EdgeLayout edgeLayout, NodeLayout sourceLayout,
                                     NodeLayout targetLayout) {
    final EdgeLabelModel model = (EdgeLabelModel) models.get(0);
    final Object param = model.createModelParameter(labelBounds, edgeLayout, sourceLayout, targetLayout);
    return new ModelParameter(model, param);
  }

  public final class ModelParameter {
    private final EdgeLabelModel model;
    private final Object parameter;

    public ModelParameter(final EdgeLabelModel model, final Object parameter) {
      this.model = model;
      this.parameter = parameter;
    }

    public EdgeLabelModel getModel() {
      return model;
    }

    public Object getParameter() {
      return parameter;
    }
  }
}
