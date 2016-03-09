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
package gr.gousiosg.javacg.view.modes;

import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;
import y.view.HitInfo;
import y.view.ViewMode;

public class TooltipMode extends ViewMode {
  public static final Object NODE_TOOLTIP_DPKEY = "TooltipMode.NODE_TOOLTIP_DPKEY";
  public static final Object EDGE_TOOLTIP_DPKEY = "TooltipMode.EDGE_TOOLTIP_DPKEY";
  
  boolean showNodeTips;
  boolean showEdgeTips;

  /**
   * Instantiates a new TooltipMode.
   * By default, node tooltips are enabled.
   */
  public TooltipMode() {
    this(true, false);
  }

  /**
   * Instantiates a new TooltipMode.
   */
  public TooltipMode(boolean showNodeTips, boolean showEdgeTips) {
    this.showNodeTips = showNodeTips;
    this.showEdgeTips = showEdgeTips;
  }

  /**
   * If set to <code>true</code> this view mode will show a tooltip displaying whatever 
   * is returned by {@link #getNodeTip(Node node)}.
   */
  public void showNodeTips(boolean showToolTip) {
    showNodeTips = showToolTip;
    if (!showNodeTips && view != null) {
      view.setToolTipText(null);
    }
  }

  /**
   * Returns <code>true</code> if this view mode should display tooltip text for 
   * nodes.
   */
  public boolean doShowNodeTips() {
    return showNodeTips;
  }

  /**
   * If set to <code>true</code> this view mode will show a tooltip displaying whatever 
   * is returned by {@link #getEdgeTip(Edge edge)}.
   */
  public void showEdgeTips(boolean showToolTip) {
    showEdgeTips = showToolTip;
    if (!showEdgeTips && view != null) {
      view.setToolTipText(null);
    }
  }

  /**
   * Returns <code>true</code> if this view mode should display tooltip text for 
   * edges.
   */
  public boolean doShowEdgeTips() {
    return showEdgeTips;
  }

  public void mouseMoved(double x, double y) {
    HitInfo hitInfo = getHitInfo(x, y);
    
    if (doShowNodeTips() || doShowEdgeTips()) {
      String tooltipText = null;
      if (hitInfo.getHitNode() != null) {
        if (doShowNodeTips()) {
          tooltipText = getNodeTip(hitInfo.getHitNode());
        }
      }
      else if (hitInfo.getHitEdge() != null) {
        if (doShowEdgeTips()) {
          tooltipText = getEdgeTip(hitInfo.getHitEdge());
        }
      }
      if (tooltipText != null && tooltipText.length() < 1) {
        tooltipText = null;
      }
      view.setToolTipText(tooltipText);
    }
  }
  
  /**
   * Returns the tooltip text to be displayed for the given node.
   * <p>
   * This method will be called to get the tooltip text associated with the given 
   * node. The tooltip text will only be displayed if <code>doShowNodeTips</code> 
   * returns <code>true</code>.
   * </p>
   * <p>
   * By default the label text of the node will be returned. If a data provider 
   * is registered with the node's graph using the {@link #NODE_TOOLTIP_DPKEY} key, 
   * then the tooltip text that is returned by the data provider for the given node 
   * is used instead.
   * <br/>
   * Subclasses may want to override this behavior.
   * </p>
   */
  protected String getNodeTip(Node node) {
    String result;
    DataProvider dp = getGraph2D().getDataProvider(NODE_TOOLTIP_DPKEY);
    
    if (dp != null && (result = (String)dp.get(node)) != null) {
      return result;
    }
    return getGraph2D().getLabelText(node);
  }

  /**
   * Returns the tooltip text to be displayed for the given edge.
   * <p>
   * This method will be called to get the tooltip text associated with the given 
   * edge. The tooltip text will only be displayed if <code>doShowEdgeTips</code> 
   * returns <code>true</code>.
   * </p>
   * <p>
   * By default the label text of the edge's first label will be returned. If a 
   * data provider is registered with the node's graph using the {@link #EDGE_TOOLTIP_DPKEY} 
   * key, then the tooltip text that is returned by the data provider for the given 
   * edge is used instead.
   * <br/>
   * Subclasses may want to override this behavior.
   * </p>
   */
  protected String getEdgeTip(Edge edge) {
    String result;
    DataProvider dp = getGraph2D().getDataProvider(EDGE_TOOLTIP_DPKEY);
    
    if (dp != null && (result = (String)dp.get(edge)) != null) {
      return result;
    }
    
    EdgeRealizer er = getGraph2D().getRealizer(edge);
    return (er.labelCount() > 0) ? er.getLabel(0).getText() : null;
  }
}
