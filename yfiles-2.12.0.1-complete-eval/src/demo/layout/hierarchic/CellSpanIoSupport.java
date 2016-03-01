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
package demo.layout.hierarchic;

import demo.layout.hierarchic.CellSpanLayoutDemo.CellColorManager;

import y.base.Graph;
import y.base.Node;
import y.io.GraphMLIOHandler;
import y.io.graphml.graph2d.GraphicsSerializationToolkit;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.XmlWriter;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.tabular.TableGroupNodeRealizer;
import y.view.tabular.TableGroupNodeRealizer.Column;
import y.view.tabular.TableGroupNodeRealizer.Table;

import java.awt.Color;
import java.util.Collection;

import org.w3c.dom.NamedNodeMap;

/**
 * Provides serialization support for
 * {@link CellSpanLayoutDemo.CellColorManager} instances.
 *
 */
class CellSpanIoSupport {
  /**
   * Prevents instantiation of support class.
   */
  private CellSpanIoSupport() {
  }

  /**
   * Configures the given GraphML handler for reading and writing
   * {@link CellSpanLayoutDemo.CellColorManager} instances.
   * @param handler the GraphML handler to be configured.
   * @return the new configured given GraphML handler. 
   */
  static GraphMLIOHandler configure( final GraphMLIOHandler handler ) {
    final CellColorsIoHandler ccmHandler = new CellColorsIoHandler();
    handler.getGraphMLHandler().addSerializationHandler(ccmHandler);
    handler.getGraphMLHandler().addDeserializationHandler(ccmHandler);
    return handler;
  }



  /**
   * Provides serialization support for
   * {@link CellSpanLayoutDemo.CellColorManager} instances.
   */
  private static final class CellColorsIoHandler
          implements DeserializationHandler, SerializationHandler {
    private static final String ELEMENT_CELL_COLORS = "CellColors";
    private static final String ELEMENT_CELL_COLOR = "CellColor";
    private static final String ATTR_COLOR = "color";
    private static final String ATTR_COLUMN = "column";
    private static final String ATTR_ROW = "row";
    private static final String NAMESPACE = "demo";

    /**
     * Writes {@link CellSpanLayoutDemo.CellColorManager} instances.
     */
    public void onHandleSerialization(
            final SerializationEvent e
    ) throws GraphMLWriteException {
      final Object item = e.getItem();
      if (item instanceof CellColorManager) {
        final GraphMLWriteContext context = e.getContext();
        final Graph graph = context.getGraph();
        if (graph instanceof Graph2D) {
          final Collection stack = context.getObjectStack();
          if (!stack.isEmpty()) {
            final Object first = stack.iterator().next();
            if (first instanceof Node) {
              final NodeRealizer nr = ((Graph2D) graph).getRealizer((Node) first);
              if (nr instanceof TableGroupNodeRealizer) {
                final XmlWriter writer = context.getWriter();
                writer.writeStartElement(NAMESPACE, ELEMENT_CELL_COLORS, NAMESPACE);

                final CellColorManager manager = (CellColorManager) item;
                final Table table = ((TableGroupNodeRealizer) nr).getTable();
                for (int i = 0, n = table.columnCount(); i < n; ++i) {
                  final Column col = table.getColumn(i);
                  for (int j = 0, m = table.rowCount(); j < m; ++j) {
                    final Color color = manager.getCellColor(col, table.getRow(j));
                    if (color != null) {
                      writer.writeStartElement(NAMESPACE, ELEMENT_CELL_COLOR, NAMESPACE);
                      writer.writeAttribute(ATTR_COLOR, GraphicsSerializationToolkit.valueOf(color));
                      writer.writeAttribute(ATTR_COLUMN, i);
                      writer.writeAttribute(ATTR_ROW, j);
                      writer.writeEndElement();
                    }
                  }
                }

                writer.writeEndElement();
              }
            }
          }
        }
        e.setHandled(true);
      }
    }

    /**
     * Reads {@link CellSpanLayoutDemo.CellColorManager} instances.
     */
    public void onHandleDeserialization(
            final DeserializationEvent e
    ) throws GraphMLParseException {
      final org.w3c.dom.Node node = e.getXmlNode();
      if (isDemoElement(node) &&
          ELEMENT_CELL_COLORS.equals(node.getLocalName())) {
        final CellColorManager manager = new CellColorManager();
        e.setResult(manager);

        final GraphMLParseContext context = e.getContext();
        final Graph graph = context.getGraph();
        if (graph instanceof Graph2D) {
          final Collection stack = context.getObjectStack();
          if (!stack.isEmpty()) {
            final Object first = stack.iterator().next();
            if (first instanceof Node) {
              final NodeRealizer nr = ((Graph2D) graph).getRealizer((Node) first);
              if (nr instanceof TableGroupNodeRealizer) {
                final Table table = ((TableGroupNodeRealizer) nr).getTable();
                for (org.w3c.dom.Node child = node.getFirstChild();
                     child != null;
                     child = child.getNextSibling()) {
                  if (isDemoElement(child) &&
                      ELEMENT_CELL_COLOR.equals(child.getLocalName())) {
                    final NamedNodeMap attrs = child.getAttributes();
                    final org.w3c.dom.Node cn = attrs.getNamedItem(ATTR_COLOR);
                    if (cn == null) {
                      throw new GraphMLParseException("Missing color attribute.");
                    }
                    final org.w3c.dom.Node colNode = attrs.getNamedItem(ATTR_COLUMN);
                    if (colNode == null) {
                      throw new GraphMLParseException("Missing column attribute.");
                    }
                    final org.w3c.dom.Node rowNode = attrs.getNamedItem(ATTR_ROW);
                    if (rowNode == null) {
                      throw new GraphMLParseException("Missing row attribute.");
                    }

                    final Color color = GraphicsSerializationToolkit.parseColor(cn.getNodeValue());
                    final int col = Integer.parseInt(colNode.getNodeValue());
                    final int row = Integer.parseInt(rowNode.getNodeValue());
                    manager.setCellColor(table.getColumn(col), table.getRow(row), color);
                  }
                }
              }
            }
          }
        }
      }
    }

    /**
     * Determines whether or not the given XML node is an element node
     * in the <code>demo</code> namespace.
     * @param node the XML node to check.
     * @return <code>true</code> if the given XML node is an element node
     * in the <code>demo</code> namespace; <code>false</code> otherwise.
     */
    private static boolean isDemoElement( final org.w3c.dom.Node node ) {
      return node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
             NAMESPACE.equals(node.getNamespaceURI());
    }
  }
}
