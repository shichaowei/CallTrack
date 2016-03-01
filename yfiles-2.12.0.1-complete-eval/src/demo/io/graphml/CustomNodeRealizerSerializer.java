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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import y.io.graphml.graph2d.ShapeNodeRealizerSerializer;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.XmlWriter;
import y.view.NodeRealizer;

/**
 * Serializer for instances of class {@link demo.io.graphml.CustomNodeRealizer}.
 * <p>
 * Generates XML markup nested within a node's GraphML <code>&lt;data></code>
 * element similar to the following:
 * </p>
 * <pre>
 *   &lt;custom:CustomNode customAttribute="v1.0">
 *      &lt;custom:CustomElement value="333"/>
 *   &lt;/custom:CustomNode>
 * </pre>
 * Note that for presentation purposes the content of the XML markup is used as
 * the node's label.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_custom_realizer_serializer">Section Support for Custom Realizer</a> in the yFiles for Java Developer's Guide
 */
public class CustomNodeRealizerSerializer extends ShapeNodeRealizerSerializer {
  /**
   * Returns the string <tt>CustomNode</tt>.
   */
  public String getName() {
    return "CustomNode";
  }


  public String getNamespaceURI() {
    return "demo.io.graphml.CustomNodeRealizer";
  }


  public String getNamespacePrefix() {
    return "custom";
  }

  /**
   * Returns class {@link CustomNodeRealizer}.
   */
  public Class getRealizerClass() {
    return CustomNodeRealizer.class;
  }

  /**
   * Writes the <code>customElement</code> field of a CustomNodeRealizer object
   * as an additional XML element.
   * (This XML element is nested within the GraphML &lt;data> element of nodes.)
   */
  public void write(NodeRealizer realizer, XmlWriter writer, GraphMLWriteContext context) throws GraphMLWriteException {
    super.write(realizer, writer, context);
    CustomNodeRealizer fnr = (CustomNodeRealizer) realizer;
    writer.writeStartElement(getNamespacePrefix(), "CustomElement", getNamespaceURI())
        .writeAttribute("value", fnr.getCustomValue())
        .writeEndElement();
  }

  /**
   * For demonstration purposes this method writes an additional <code>customAttribute</code> value as an XML attribute of a CustomNodeRealizer's
   * XML markup. 
   * (This XML attribute enhances the GraphML &lt;data&gt; element of nodes.)
   */
  public void writeAttributes(NodeRealizer realizer, XmlWriter writer, GraphMLWriteContext context) {
    super.writeAttributes(realizer, writer, context);
    CustomNodeRealizer fnr = (CustomNodeRealizer) realizer;
    writer.writeAttribute("customAttribute", fnr.getCustomAttribute());
  }

  /**
   * Parses parts of the content of a GraphML file by processing its DOM structure. 
   */
  public void parse(NodeRealizer realizer, Node domNode, GraphMLParseContext context) throws GraphMLParseException {
    super.parse(realizer, domNode, context);

    CustomNodeRealizer result = (CustomNodeRealizer) realizer;

    //parse attributes
    NamedNodeMap nm = domNode.getAttributes();
    Node a = nm.getNamedItem("customAttribute");
    if (a != null) {
      result.setCustomAttribute(a.getNodeValue());
    }

    //parse elements
    for (Node child = domNode.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE &&
          "CustomElement".equals(child.getLocalName())) {
        nm = child.getAttributes();
        a = nm.getNamedItem("value");
        if (a != null) {
          result.setCustomValue(Integer.parseInt(a.getNodeValue()));
        }
      }
    }
  }
}
