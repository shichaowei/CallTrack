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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import y.base.DataMap;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;
import y.io.graphml.input.AbstractDataAcceptorInputHandler;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.InputHandlerProvider;
import y.io.graphml.input.NameBasedDeserializer;
import y.io.graphml.input.QueryInputHandlersEvent;
import y.io.graphml.output.AbstractOutputHandler;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.TypeBasedSerializer;
import y.io.graphml.output.XmlWriter;
import y.option.OptionHandler;
import y.view.EditMode;
import y.view.PopupMode;
import y.view.TooltipMode;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * This demo shows how to configure GraphMLIOHandler to be able to handle
 * extra node and edge data of complex type.
 * Additional data for a node can be edited by right-clicking on the
 * corresponding element.
 * The element tool tip will show the currently set data values for each element.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_extension_dynamic">Section Dynamic Registration of Attributes</a> in the yFiles for Java Developer's Guide
 */
public class ComplexExtensionGraphMLDemo extends GraphMLDemo {

  /**
   * Store node/edge data
   */
  private NodeMap nodeDataMap;
  private EdgeMap edgeDataMap;

  protected void loadInitialGraph() {
    loadGraph("resources/custom/complexdemo.graphml");    
  }

  /**
   * Configures GraphMLIOHandler to read and write additional node and edge data
   * of complex type.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    if (nodeDataMap == null) {
      nodeDataMap = view.getGraph2D().createNodeMap();
    }
    if (edgeDataMap == null) {
      edgeDataMap = view.getGraph2D().createEdgeMap();
    }


    GraphMLIOHandler ioHandler = super.createGraphMLIOHandler();


    //For our top-level node data, which consists of {@link Item} collections, we add (de)serializers only for
    //the parsing of the node attributes
    ioHandler.getGraphMLHandler().addInputDataAcceptor("myNodeAttribute", nodeDataMap, KeyScope.NODE,
        new ItemListDeserializer());

    ioHandler.getGraphMLHandler().addOutputDataProvider("myNodeAttribute", nodeDataMap, KeyScope.NODE,
        new ItemListSerializer());

    //We add serializers/deserializers for Item objects globally (so they can be used from inside other
    //(de)serializers
    ioHandler.getGraphMLHandler().addDeserializationHandler(new ItemDeserializer());
    ioHandler.getGraphMLHandler().addSerializationHandler(new ItemSerializer());

    //Add an explicit input handler to parse our edge data (just to show how it works - usually, a deserializer for date would be easier...)
    ioHandler.getGraphMLHandler().addInputHandlerProvider(new InputHandlerProvider() {
      public void onQueryInputHandler(QueryInputHandlersEvent event) throws GraphMLParseException {
        Element keyDefinition = event.getKeyDefinition();
        if (!event.isHandled()
            && GraphMLHandler.matchesScope(keyDefinition, KeyScope.EDGE)
            && GraphMLHandler.matchesName(keyDefinition, "myEdgeAttribute")) {
          MyDateInputHandler handler = new MyDateInputHandler();
          handler.setDataAcceptor(edgeDataMap);
          handler.initializeFromKeyDefinition(event.getContext(), keyDefinition);
          event.addInputHandler(handler);
        }
      }
    });

    //Add an explicit output handler to write our edge data (just to show how it works - usually, a serializer for date would be easier...)
    ioHandler.getGraphMLHandler().addOutputHandlerProvider(
        new AbstractOutputHandler("myEdgeAttribute", KeyScope.EDGE, KeyType.COMPLEX) {
          protected void writeValueCore(GraphMLWriteContext context, Object data) throws GraphMLWriteException {
            if (data instanceof Date) {
              context.getWriter().writeText(dateFormat.format(data));
            }
          }

          protected Object getValue(GraphMLWriteContext context, Object key) throws GraphMLWriteException {
            return edgeDataMap.get(key);
          }
        });

    return ioHandler;
  }


  public static final String DEMO_NS = "demo.io.graphml.complex";

  /**
   * Example class that is used as an example for complex objects
   */
  public static class Item {
    private String value;

    public Item(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public String toString() {
      return value;
    }
  }

  /**
   * Custom Serializer for {@link Item} objects
   */
  public static class ItemSerializer extends TypeBasedSerializer {
    public void serializeItem(Object o, XmlWriter writer, GraphMLWriteContext context) throws GraphMLWriteException {
      Item item = (Item) o;
      writer.writeStartElement("Item", DEMO_NS).writeAttribute("value", item.getValue()).writeEndElement();
    }

    protected Class getSerializationType(GraphMLWriteContext context) {
      //We are only valid for Item objects
      return Item.class;
    }
  }

  /**
   * Custom deserializer for {@link Item} objects
   */
  public static class ItemDeserializer extends NameBasedDeserializer {
    public Object deserializeNode(org.w3c.dom.Node xmlNode, GraphMLParseContext context) throws GraphMLParseException {
      if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && DEMO_NS.equals(xmlNode.getNamespaceURI())
          && "Item".equals(xmlNode.getLocalName())) {
        return new Item(((Element) xmlNode).getAttribute("value"));
      }
      return null;
    }


    public String getNamespaceURI(GraphMLParseContext context) {
      return DEMO_NS;
    }

    public String getNodeName(GraphMLParseContext context) {
      return "Item";
    }
  }

  /**
   * Custom serializer for Collections of {@link Item}s
   */
  public static class ItemListSerializer implements SerializationHandler {

    public void onHandleSerialization(SerializationEvent event) throws GraphMLWriteException {
      Object o = event.getItem();
      if (o instanceof Collection) {
        Collection coll = (Collection) o;
        event.getWriter().writeStartElement("ItemList", DEMO_NS);
        for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
          Object item = iterator.next();
          event.getContext().serialize(item);
        }
        event.getWriter().writeEndElement();
      }
    }
  }

  /**
   * Custom deserializer for Collections of {@link Item}s
   */
  public static class ItemListDeserializer implements DeserializationHandler {
    public void onHandleDeserialization(DeserializationEvent event) throws GraphMLParseException {
      org.w3c.dom.Node xmlNode = event.getXmlNode();
      if (xmlNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && DEMO_NS.equals(xmlNode.getNamespaceURI())
          && "ItemList".equals(xmlNode.getLocalName())) {
        Collection retval = new ArrayList();
        NodeList childNodes = xmlNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
          org.w3c.dom.Node child = childNodes.item(i);
          if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            Object o = event.getContext().deserialize(child);
            retval.add(o);
          }
        }
        event.setResult(retval);
      }
    }
  }

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");

  /** Explicit input handler for Date attributes */
  public static class MyDateInputHandler extends AbstractDataAcceptorInputHandler {
    protected Object parseDataCore(GraphMLParseContext context, org.w3c.dom.Node node) throws GraphMLParseException {
      if (node.getChildNodes().getLength() != 1) {
        throw new GraphMLParseException("Invalid data format - single text node expected");
      }
      org.w3c.dom.Node n = node.getFirstChild();
      if (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
        try {
          return dateFormat.parse(n.getNodeValue());
        } catch (ParseException e) {
          throw new GraphMLParseException("Invalid date format: " + e.getMessage(), e);
        }
      } else {
        throw new GraphMLParseException("Invalid data format - Text node expected");
      }
    }
  }

  protected String[] getExampleResources() {
    return null;
  }

  /**
   * Create an edit mode that displays a context-sensitive popup-menu when
   * right-clicking on a node or an edge.
   */
  protected EditMode createEditMode() {
    EditMode editMode = super.createEditMode();

    editMode.setPopupMode(new PopupMode() {
      public JPopupMenu getNodePopup(Node v) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditAttributeAction("Edit Node Attribute...", v, nodeDataMap));
        return pm;
      }
    });
    return editMode;
  }

  /**
   * Create a customized {@link TooltipMode}.
   * @return a <code>TooltipMode</code> that displays information about nodes and edges.
   */
  protected TooltipMode createTooltipMode() {
    TooltipMode tooltipMode = new TooltipMode() {
      /**
       * Overwritten to display the data values of the node in a tooltip.
       * @param node the node for which the tooltip is set
       * @return the tooltip string that is used by the <code>TooltipMode</code>
       */
      protected String getNodeTip(Node node) {
        Collection items = (Collection) nodeDataMap.get(node);
        String tipText = null;
        if (items != null) {
          tipText = "<html><body>Items:<table>";
          for (Iterator iterator = items.iterator(); iterator.hasNext(); ) {
            tipText += "</tr></td>" + iterator.next() + "</td></tr>";
          }
          tipText += "</table></body></html>";
        }
        return tipText;
      }

      /**
       * Overwritten to display the data values of the edge in a tooltip.
       * @param edge the edge for which the tooltip is set
       * @return the tooltip string that is used by the <code>TooltipMode</code>
       */
      protected String getEdgeTip(Edge edge) {
        Object o = edgeDataMap.get(edge);
        String tipText = null;
        if (o instanceof Date) {
          tipText = o.toString();
        }
        return tipText;
      }
    };

    return tooltipMode;
  }

  /**
   * Editor action for the additional node attributes.
   */
  class EditAttributeAction extends AbstractAction {
    private Object object;
    private DataMap dataMap;

    private OptionHandler op;

    EditAttributeAction(String name, Object object, DataMap dataMap) {
      super(name);
      this.object = object;
      this.dataMap = dataMap;
      op = new OptionHandler(name);
      if (object instanceof Node) {
        Object o = dataMap.get(object);
        if (o instanceof Collection) {
          Collection coll = (Collection) o;
          String str = "";
          for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
            str += iterator.next();
            if (iterator.hasNext()) {
              str += "\n";
            }
          }
          op.addString("Node Items", str, 10);
        } else {
          op.addString("Node Items", "", 10);
        }
      }      
    }

    public void actionPerformed(ActionEvent actionEvent) {
      if (op.showEditor(view.getFrame())) {
        if (object instanceof Node) {          
          Collection coll = new ArrayList();
          String s = op.getString("Node Items");
          StringTokenizer tokenizer = new StringTokenizer(s, "\n");
          while (tokenizer.hasMoreElements()) {
            String s1 = (String) tokenizer.nextElement();
            coll.add(new Item(s1));
          }
          dataMap.set(object, coll);          
        }
        graphMLPane.updateGraphMLText(view.getGraph2D());        
      }
    }
  }

  /**
   * Launches this demo.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new ComplexExtensionGraphMLDemo()).start();
      }
    });
  }
}
