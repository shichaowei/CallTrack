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
package demo.io.graphml;

import y.option.OptionHandler;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.SizeConstraintProvider;
import y.view.NodeLabel;
import y.base.NodeCursor;
import y.base.Node;
import y.geom.YDimension;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * This module adjusts the size of nodes to match the
 * size of their label text.
 */
public class NodeSizeAdapter extends y.module.YModule 
{
  static final String MIN_WIDTH             = "MIN_WIDTH";
  static final String IGNORE_WIDTHS         = "IGNORE_WIDTHS";
  static final String ADAPT_TO_MAXIMUM_NODE = "ADAPT_TO_MAXIMUM_NODE";
  static final String NODE_SIZE_ADAPTER     = "NODE_SIZE_ADAPTER";
  static final String ONLY_SELECTION        = "ONLY_SELECTION";
  static final String VERTICAL_SPACE        = "VERTICAL_SPACE";
  static final String IGNORE_HEIGHTS        = "IGNORE_HEIGHTS";
  static final String MIN_HEIGHT            = "MIN_HEIGHT";
  static final String HORIZON_SPACE         = "HORIZON_SPACE";

  private int vSpace        = 5;
  private int hSpace        = 5;
  private int minWidth      = 10;
  private int minHeight     = 30;
  private boolean selectionOnly = false;
  private boolean adaptToMax    = false;
  private boolean ignoreHeight  = true;
  private boolean ignoreWidth   = false;

  double currentMaxH = 0;
  double currentMaxW = 0;

  public NodeSizeAdapter()
  {
    super(NodeSizeAdapter.NODE_SIZE_ADAPTER);
  }

  protected OptionHandler createOptionHandler()
  {
    OptionHandler op = new OptionHandler(getModuleName());

    op.addInt(NodeSizeAdapter.VERTICAL_SPACE, vSpace, 1, 20);
    op.addInt(NodeSizeAdapter.MIN_HEIGHT, minHeight, 5, 100 );
    op.addBool(NodeSizeAdapter.IGNORE_HEIGHTS, ignoreHeight);
    op.addInt(NodeSizeAdapter.HORIZON_SPACE, hSpace, 1, 20);
    op.addInt(NodeSizeAdapter.MIN_WIDTH, minWidth, 5, 100 );
    op.addBool(NodeSizeAdapter.IGNORE_WIDTHS, ignoreWidth);
    op.addBool(NodeSizeAdapter.ADAPT_TO_MAXIMUM_NODE, adaptToMax);
    op.addBool(NodeSizeAdapter.ONLY_SELECTION, false);

    return op;
  }

  protected void init()
  {
    OptionHandler op = getOptionHandler();

    vSpace = op.getInt(NodeSizeAdapter.VERTICAL_SPACE);
    minHeight = op.getInt(NodeSizeAdapter.MIN_HEIGHT);
    ignoreHeight = op.getBool(NodeSizeAdapter.IGNORE_HEIGHTS);
    hSpace = op.getInt(NodeSizeAdapter.HORIZON_SPACE);
    minWidth = op.getInt(NodeSizeAdapter.MIN_WIDTH);
    ignoreWidth = op.getBool(NodeSizeAdapter.IGNORE_WIDTHS);
    adaptToMax = op.getBool(NodeSizeAdapter.ADAPT_TO_MAXIMUM_NODE);
    selectionOnly = op.getBool(NodeSizeAdapter.ONLY_SELECTION);
  }

  protected void mainrun()
  {
    Graph2D graph = getGraph2D();
    NodeCursor nc;
    if( selectionOnly )               // only selected nodes
      nc = graph.selectedNodes();
    else                              // all nodes
      nc = graph.nodes();
    adapt(nc,adaptToMax);
    getGraph2D().updateViews();
  }

  public void adaptMax()
  {
    Graph2D graph = getGraph2D();

    NodeCursor nc;
    if( selectionOnly )               // only selected nodes
      nc = graph.selectedNodes();
    else                              // all nodes
      nc = graph.nodes();
    for( ; nc.ok(); nc.next() )
    {
      NodeRealizer nr = graph.getRealizer(nc.node());
      if( !ignoreHeight )
        nr.setHeight( currentMaxH );
      if( !ignoreWidth )
        nr.setWidth( currentMaxW );
    }
  }

  //---------------------------------------------------------------------------
  /**
   * Adapts the size of a list of nodes
   */
  public void adapt(NodeCursor nc,boolean adaptToMax)
  {
    currentMaxW = currentMaxH = 0;
    for (;nc.ok();nc.next())
    {
      adapt(nc.node());
    }
    if( adaptToMax )
      adaptMax();
  }

  static FontRenderContext defaultFRC =
    new FontRenderContext(new AffineTransform(),false,false);

  //---------------------------------------------------------------------------
  /**
   * Adapts the size of a single node
   */
  public void adapt(Node n)
  {
    NodeRealizer nr = getGraph2D().getRealizer(n);
    adapt(nr);
  }


  /**
   * Adapts the size of a single node
   */
  public void adapt(NodeRealizer nr) {
    double minWidth = this.minWidth;
    double minHeight = this.minHeight;
    double maxWidth = Double.POSITIVE_INFINITY;
    double maxHeight = Double.POSITIVE_INFINITY;
    final SizeConstraintProvider scp = nr.getSizeConstraintProvider();
    if (scp != null) {
      final YDimension min = scp.getMinimumSize();
      if (min != null) {
        if (minWidth < min.width) {
          minWidth = min.width;
        }
        if (minHeight < min.height) {
          minHeight = min.height;
        }
      }
      final YDimension max = scp.getMaximumSize();
      if (max != null) {
        maxWidth = max.width;
        maxHeight = max.height;
      }
    }

    final NodeLabel nl = nr.getLabel();

    if (!ignoreWidth) {
      double width = nl.getWidth() + 2 * hSpace;
      if (width < minWidth) {
        width = minWidth;
      }
      if (width > maxWidth) {
        width = maxWidth;
      }
      nr.setWidth(width);
      if (width > currentMaxW) {
        currentMaxW = width;
      }
    }
    if (!ignoreHeight) {
      double height = nl.getHeight() + 2 * vSpace;
      if (height < minHeight) {
        height = minHeight;
      }
      if (height > maxHeight) {
        height = maxHeight;
      }
      nr.setHeight(height);
      if (height > currentMaxH) {
        currentMaxH = height;
      }
    }
  }

  //---------------------------------------------------------------------------
  public void setIgnore(boolean width,boolean height)
  {
    ignoreHeight = height;
    ignoreWidth = width;
  }

}
