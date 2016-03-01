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

import y.base.Edge;
import y.base.EdgeList;
import y.layout.LabelLayoutConstants;
import y.layout.PreferredPlacementDescriptor;
import y.option.MappedListCellRenderer;
import y.option.OptionHandler;
import y.view.EdgeLabel;
import y.view.Graph2D;
import y.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple OptionHandler that is used by the generic edge labeling demo.
 */
public class EdgeLabelPropertyHandler extends OptionHandler {
  private static final String TEXT = "Text";
  private static final String EDGE_LABEL_PROPERTIES = "Edge Label Properties";
  private static final String PREFERRED_PLACEMENT = "Preferred Placement";

  private EdgeLabel label;
  private View view;

  public EdgeLabelPropertyHandler(EdgeLabel label, View view) {
    super(EDGE_LABEL_PROPERTIES);
    setOptionsIOHandler(null);
    this.label = label;
    this.view = view;    

    addString(TEXT, label.getText(), 2);

    final Map preferredPlacementMap = preferredPlacementsToStringMap();
    addEnum(PREFERRED_PLACEMENT, preferredPlacementMap.keySet().toArray(),
        new Byte(LabelLayoutConstants.PLACE_ANYWHERE), new MappedListCellRenderer(preferredPlacementMap));
  }

  public void commitValues() {
    super.commitValues();

    Edge e = label.getOwner().getEdge();
    if(e != null) {
      final Graph2D graph = (Graph2D) e.getGraph();
      graph.backupRealizers(new EdgeList(e).edges());
    }

    label.setText(getString(TEXT));

    final byte placement = ((Byte) get(PREFERRED_PLACEMENT)).byteValue();
    final PreferredPlacementDescriptor oldDescriptor =
            label.getPreferredPlacementDescriptor();
    if (oldDescriptor.getPlaceAlongEdge() != placement) {
      final PreferredPlacementDescriptor newDescriptor =
              new PreferredPlacementDescriptor(oldDescriptor);
      newDescriptor.setPlaceAlongEdge(placement);
      label.setPreferredPlacementDescriptor(newDescriptor);
    }

    view.getGraph2D().updateViews();
  }

  /**
   * Creates a map that maps the preferred placement constants to strings.
   */
  private static Map preferredPlacementsToStringMap()
  {
    Map result = new LinkedHashMap(3);
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_TARGET),"At Target");
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_SOURCE),"At Source");
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_CENTER),"At Center");
    result.put(new Byte(LabelLayoutConstants.PLACE_ANYWHERE), "Anywhere");
    return result;
  }
}
