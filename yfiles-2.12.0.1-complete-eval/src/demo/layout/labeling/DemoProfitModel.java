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

import y.layout.ProfitModel;
import y.layout.LabelCandidate;
import y.layout.EdgeLabelModel;
import y.layout.RotatedSliderEdgeLabelModel;
import y.layout.RotatedDiscreteEdgeLabelModel;
import demo.layout.labeling.CompositeEdgeLabelModel;

/**
 * A simple profit model used by the edge labeling demo.
 *
 */
public class DemoProfitModel implements ProfitModel {  
  private double angle;
  private double angleProfit;
  private double otherProfit;

  /**
   * For a given label candidate lc this profit model returns profit "angleProfit" if lc's angle == "angle"
   * and profit "otherProfit", otherwise.
   *
   * @param angle the value of the angle (in radians) that has profit "angleProfit"
   * @param angleProfit the profit used for angles with value "angle"
   * @param otherProfit the profit used for angles with values != "angle"
   */
  public DemoProfitModel(double angle, double angleProfit, double otherProfit) {
    this.angle = angle;
    this.angleProfit = angleProfit;
    this.otherProfit = otherProfit;
  }

  public double getProfit(LabelCandidate candidate) {
    Object param = candidate.getModelParameter();
    if (param instanceof CompositeEdgeLabelModel.ModelParameter) {
      double angle = determineAngle((CompositeEdgeLabelModel.ModelParameter) param);
      if (angle == this.angle) {
        return angleProfit;
      } 
    }
    return otherProfit;
  }

  //returns the angle encoded by the given model parameter
  private static double determineAngle(CompositeEdgeLabelModel.ModelParameter param) {
    EdgeLabelModel elm = param.getModel();
    double angle = 0;
    if(elm instanceof RotatedSliderEdgeLabelModel) {
      angle = ((RotatedSliderEdgeLabelModel) elm).getAngle();
    } else if(elm instanceof RotatedDiscreteEdgeLabelModel) {
      angle = ((RotatedDiscreteEdgeLabelModel) elm).getAngle();
    } else {
      throw new RuntimeException("Unknown EdgeLableModel!");
    }
    return angle;
  }
}
