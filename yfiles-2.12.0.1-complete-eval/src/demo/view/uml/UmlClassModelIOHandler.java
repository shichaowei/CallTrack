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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import y.io.graphml.NamespaceConstants;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.XmlWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles serialization and deserialization of the {@link UmlClassModel user data}.
 */
class UmlClassModelIOHandler implements SerializationHandler, DeserializationHandler {
  private static final String ELEMENT_MODEL = "Model";
  private static final String ATTRIBUTE_TYPE = "type";
  private static final String VALUE_TYPE_CLASS = "Class";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ELEMENT_ATTRIBUTES = "Attributes";
  private static final String ELEMENT_OPERATIONS = "Operations";
  private static final String ELEMENT_ATTRIBUTE = "Attribute";
  private static final String ELEMENT_OPERATION = "Operation";
  private static final String ATTRIBUTE_VALUE = "value";

  /**
   * Handles the serialization of the model.
   *
   * @param event Event that contains all data that is needed for serialization.
   * @throws GraphMLWriteException
   */
  public void onHandleSerialization(final SerializationEvent event) throws GraphMLWriteException {
    final Object item = event.getItem();
    if (item instanceof UmlClassModel) {
      final UmlClassModel model = (UmlClassModel) item;
      final XmlWriter writer = event.getWriter();
      writer.writeStartElement(ELEMENT_MODEL, NamespaceConstants.YFILES_JAVA_NS);
      writeType(writer, model);
      writeName(writer, model);
      writeAttributes(writer, model);
      writeOperations(writer, model);
      writer.writeEndElement();
      event.setHandled(true);
    }
  }

  /**
   * Writes the type of the model.
   */
  private void writeType(final XmlWriter writer, final UmlClassModel model) {
    writer.writeAttribute(ATTRIBUTE_TYPE, VALUE_TYPE_CLASS);
  }

  /**
   * Writes the name of the model element.
   */
  private void writeName(final XmlWriter writer, final UmlClassModel model) {
    writer.writeAttribute(ATTRIBUTE_NAME, model.getClassName());
  }

  /**
   * Writes the attributes.
   */
  private void writeAttributes(final XmlWriter writer, final UmlClassModel model) {
    writer.writeStartElement(ELEMENT_ATTRIBUTES, NamespaceConstants.YFILES_JAVA_NS);
    for (java.util.Iterator it = model.getAttributes().iterator(); it.hasNext(); ) {
      final String attribute = (String) it.next();
      writer.writeStartElement(ELEMENT_ATTRIBUTE, NamespaceConstants.YFILES_JAVA_NS);
      writer.writeAttribute(ATTRIBUTE_VALUE, attribute);
      writer.writeEndElement();
    }
    writer.writeEndElement();
  }

  /**
   * Writes the operations.
   */
  private void writeOperations(final XmlWriter writer, final UmlClassModel model) {
    writer.writeStartElement(ELEMENT_OPERATIONS, NamespaceConstants.YFILES_JAVA_NS);
    for (java.util.Iterator it = model.getOperations().iterator(); it.hasNext(); ) {
      final String operation = (String) it.next();
      writer.writeStartElement(ELEMENT_OPERATION, NamespaceConstants.YFILES_JAVA_NS);
      writer.writeAttribute(ATTRIBUTE_VALUE, operation);
      writer.writeEndElement();
    }
    writer.writeEndElement();
  }

  /**
   * Handles the deserialization of the model.
   *
   * @param event Event that contains all data that is needed for deserialization.
   * @throws GraphMLParseException
   */
  public void onHandleDeserialization(final DeserializationEvent event) throws GraphMLParseException {
    final org.w3c.dom.Node xmlNode = event.getXmlNode();

    if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&
        NamespaceConstants.YFILES_JAVA_NS.equals(xmlNode.getNamespaceURI())) {
      if (ELEMENT_MODEL.equals(xmlNode.getLocalName())) {
        Element element = (Element) xmlNode;
        final String name = element.getAttribute(ATTRIBUTE_NAME);

        final List attributes = new ArrayList();
        final List operations = new ArrayList();
        for (Node childNode = xmlNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
          if (ELEMENT_ATTRIBUTES.equals(childNode.getLocalName())) {
            readAttributes(childNode, attributes);
          } else if (ELEMENT_OPERATIONS.equals(childNode.getLocalName())) {
            readOperations(childNode, operations);
          }
        }
        event.setResult(new UmlClassModel(name, attributes, operations));
      }
    }
  }

  /**
   * Reads the list of attributes.
   */
  private void readAttributes(final Node xmlNode, final List attributes) {
    for (Node childNode = xmlNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
      if (ELEMENT_ATTRIBUTE.equals(childNode.getLocalName())) {
        final Element element = (Element) childNode;
        final String attribute = element.getAttribute(ATTRIBUTE_VALUE);
        if (attribute != null && attribute.length() > 0) {
          attributes.add(attribute);
        }
      }
    }
  }

  /**
   * Reads the list of operations.
   */
  private void readOperations(final Node xmlNode, final List operations) {
    for (Node childNode = xmlNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
      if (ELEMENT_OPERATION.equals(childNode.getLocalName())) {
        final Element element = (Element) childNode;
        final String operation = element.getAttribute(ATTRIBUTE_VALUE);
        if (operation != null && operation.length() > 0) {
          operations.add(operation);
        }
      }
    }
  }
}
