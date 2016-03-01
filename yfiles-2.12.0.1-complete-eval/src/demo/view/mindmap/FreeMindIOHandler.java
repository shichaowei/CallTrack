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
package demo.view.mindmap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import y.base.EdgeCursor;
import y.io.IOHandler;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Provides methods to import and export FreeMind documents.
 * It is tested with FreeMind 0.9.0 and Freeplane 1.2.0.
 * It should work for other versions, too, though.
 */
public class FreeMindIOHandler extends IOHandler {
  private static final String ENCODING = "UTF-8" ;
  private static final String ATTRIBUTE_COLOR = "COLOR";
  private static final String ATTRIBUTE_POSITION = "POSITION";
  private static final String ATTRIBUTE_WIDTH = "WIDTH";
  private static final String ATTRIBUTE_TEXT = "TEXT";
  private static final String TAG_EDGE = "edge";
  private static final String TAG_NODE = "node";

  /**
   * Write a FreeMind document.
   */
  public void write( final Graph2D graph, final OutputStream out ) throws IOException {
    final BufferedWriter bufferedWriter = new BufferedWriter(
            new OutputStreamWriter(new OutputStreamGuardian(out), ENCODING));
    try {
      bufferedWriter.write("<map version=\"0.9.0\">");
      bufferedWriter.newLine();
      writeNode(graph, ViewModel.instance.getRoot(), bufferedWriter);
      bufferedWriter.write("</map>");
      bufferedWriter.flush();
    } finally {
      bufferedWriter.close();
    }
  }

  /**
   * Write an item and its children to a FreeMind document.
   * @param graph current Graph2D
   * @param node item
   * @param writer the BufferedWrite to write to
   * @throws IOException
   */
  private static void writeNode(
          final Graph2D graph, final y.base.Node node, final BufferedWriter writer
  ) throws IOException {
    // As Freeplane can only handle XML documents with specific formatting,
    // no XML Writer is used here. Instead, files are written manually

    final NodeRealizer nodeRealizer = graph.getRealizer(node);
    writer.write("<" + TAG_NODE + " " + ATTRIBUTE_TEXT + "=\"" + nodeRealizer.getLabelText() + "\"");

    final boolean isSecondLevel = node.inDegree() > 0 && node.firstInEdge().source().inDegree() == 0;
    final boolean isLeftSide = ViewModel.instance.isLeft(node);
    //The position attribute is only needed for direct children of the center item
    if (isSecondLevel && isLeftSide) {
      writer.write(" " + ATTRIBUTE_POSITION + "=\"left\" ");
    }
    writer.write(" >");
    writer.newLine();
    writeEdge(nodeRealizer, writer);

    for (EdgeCursor ec = MindMapUtil.outEdges(node).edges(); ec.ok(); ec.next()) {
      writeNode(graph, ec.edge().target(), writer);
    }

    writer.write("</" + TAG_NODE + ">");
    writer.newLine();
  }

  private static void writeEdge(
          final NodeRealizer target, final BufferedWriter writer
  ) throws IOException {
    writer.write("<" + TAG_EDGE + " ");
    String value = Integer.toHexString(target.getFillColor().getRGB());
    writer.write(ATTRIBUTE_COLOR + "=\"#" + value.substring(2, 8) + "\" ");
    writer.write(ATTRIBUTE_WIDTH + "=\"" + ((int) target.getLineType().getLineWidth()) + "\"");
    writer.write("/>");
    writer.newLine();
  }


  /**
   * Read a FreeMind document.
   */
  public void read( final Graph2D graph, final InputStream in ) throws IOException {
    try {
      readNode(
              DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in),
              graph,
              null);
    } catch (SAXException e) {
      final IOException ioe = new IOException();
      ioe.initCause(ioe);
      throw ioe;
    } catch (ParserConfigurationException e) {
      final IOException ioe = new IOException();
      ioe.initCause(ioe);
      throw ioe;
    }
  }


  /**
   * Read a XML Document and create items.
   * @param node the current xml node
   * @param graph the current Graph2D
   * @param parent the parent item
   */
  private static void readNode(final Node node, final Graph2D graph, final y.base.Node parent) {
    final String nodeName = node.getNodeName();
    y.base.Node n = parent;
    if (nodeName.equals(TAG_NODE)) {
      final NamedNodeMap attributes = node.getAttributes();
      Node attr = attributes.getNamedItem(ATTRIBUTE_TEXT);
      String name = "";
      boolean side = false;
      if (attr != null) {
        name = attr.getNodeValue();
      }
      attr = attributes.getNamedItem(ATTRIBUTE_POSITION);
      if (attr != null) {
        side = attr.getNodeValue().equalsIgnoreCase("left");
      }
      if (parent == null) {
        //if an item has no parent, it is this the center item.
        //as it has no parent, the addNode method can't be used here,
        //so the item is created manually. It will be updated later
        n = graph.createNode(0,0,name);
      } else {
        //uses fill color from parent - if a color is specified, it will be adjusted later
        n = MindMapUtil.addNode(graph, parent, name, side);
      }
      if (parent != null && attr == null) {
        MindMapUtil.updateVisuals(graph, n, ViewModel.instance.isLeft(parent));
      } else {
        MindMapUtil.updateVisuals(graph, n, side);
      }
    } else if (nodeName.equals(TAG_EDGE)) {
      final NamedNodeMap attributes = node.getAttributes();
      Node attr = attributes.getNamedItem(ATTRIBUTE_COLOR);
      //Freeplane saves incoming edges as a children of its target.
      //Therefore we need to change the parents attributes.
      final NodeRealizer nodeRealizer = graph.getRealizer(parent);
      if (attr != null) {
        nodeRealizer.setFillColor(Color.decode(attr.getNodeValue()));
      }
      attr = attributes.getNamedItem(ATTRIBUTE_WIDTH);
      if (attr != null) {
        int lineWidth = Integer.parseInt(attr.getNodeValue());
        if (lineWidth == 2) {
          nodeRealizer.setLineType(LineType.LINE_3);
        } else {
          nodeRealizer.setLineType(LineType.LINE_6);
        }
      }
    }

    // read children
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      readNode(child, graph, n);
    }
  }


  /**
   * Returns <em>FreeMind file</em>.
   * @return <em>FreeMind file</em>.
   */
  public String getFileFormatString() {
    return "FreeMind file";
  }

  /**
   * Returns <em>mm</em>.
   * @return <em>mm</em>.
   */
  public String getFileNameExtension() {
    return "mm";
  }


  /**
   * Prevents {@link java.io.OutputStream#close()} calls from being propagated
   * to the decorated output stream.
   */
  private static final class OutputStreamGuardian extends OutputStream {
    private final OutputStream os;

    OutputStreamGuardian( final OutputStream os ) {
      this.os = os;
    }

    public void close() throws IOException {
      // do nothing
    }

    public void flush() throws IOException {
      os.flush();
    }

    public void write( final int b ) throws IOException {
      os.write(b);
    }

    public void write( final byte[] b ) throws IOException {
      os.write(b);
    }

    public void write( final byte[] b, final int off, final int len ) throws IOException {
      os.write(b, off, len);
    }
  }
}
