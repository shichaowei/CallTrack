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
package demo.view.orgchart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TreeModel that uses {@link Employee}s as TreeNodes.
 */
public class OrgChartTreeModel extends DefaultTreeModel {
  
  /**
   * Creates a tree model with the given root.
   */
  public OrgChartTreeModel(final Employee root) {
    super(root);
  }

  /**
   * A TreeNode implementation that represents an Employee in an Organization.  
   */
  public static class Employee extends DefaultMutableTreeNode {
    public String name;
    public String email;
    public String phone;
    public String fax;
    public String businessUnit;
    public String position;
    public String status;
    public Icon icon;
    public boolean assistant;
    public String layout;
    public boolean vacant;

    /**
     * Initializes a new employee instance.
     */
    public Employee() {
      name = "";
      email = "";
      phone = "";
      fax = "";
      businessUnit = "";
      position = "";
      status = "";
      layout = "";
    }

    /**
     * Sets the status of the position represented by this employee to vacant.
     * Changes related properties.
     */
    public void vacate() {
      vacant = true;
      name = "";
      status = "unavailable";
      fax = "";
      email = "";
      phone = "";
      icon = null;
    }

    /**
     * Adopts properties <code>layout</code>, <code>assistant</code>,
     * <code>position</code>, and <code>businessUnit</code> from the
     * given other employee.
     */
    public void adoptStructuralData( final Employee otherEmployee ) {
      layout = otherEmployee.layout;
      assistant = otherEmployee.assistant;
      position = otherEmployee.position;
      businessUnit = otherEmployee.businessUnit;
    }
  }
  
  /**
   * Creates an instance of this class from an XML stream. 
   * A sample XML file is located at resources/orgchartmodel.xml. 
   */
  public static OrgChartTreeModel create(final InputSource input) {
    final OrgChartReader reader = new OrgChartReader();
    return reader.read(input);    
  }

  static class OrgChartWriter {
  /**
     * Writes current org chart to a file.
     * @param selectedFile file chosen by user
     * @param rootElement root element to start visiting
     */
    public void write(File selectedFile, final Employee rootElement) {
      final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder docBuilder;
      try {
        docBuilder = docFactory.newDocumentBuilder();
        final Document doc = docBuilder.newDocument();
        //get employees as DOM Elements
        doc.appendChild(visit(rootElement,doc));

        // write the content into xml file
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        final DOMSource source = new DOMSource(doc);
        //force xml as file extension
        if (!selectedFile.getName().endsWith(".xml")) {
          selectedFile = new File(selectedFile.getPath() + ".xml");
        }
        final StreamResult result = new StreamResult(selectedFile);
        //nice output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(source, result);
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (TransformerConfigurationException e) {
        e.printStackTrace();
      } catch (TransformerException e) {
        e.printStackTrace();
      }
    }

    /**
     * Visit an employee and its children.
     * @param rootEmployee employee to start from
     * @param doc document to create DOM nodes
     * @return Employees as DOM tree
     */
    private Element visit(final Employee rootEmployee, final Document doc) {
      final Element element = doc.createElement("employee");
      element.setAttribute("name",rootEmployee.name);
      element.setAttribute("email",rootEmployee.email);
      element.setAttribute("phone",rootEmployee.phone);
      element.setAttribute("fax",rootEmployee.fax);
      element.setAttribute("businessUnit",rootEmployee.businessUnit);
      element.setAttribute("position",rootEmployee.position);
      element.setAttribute("status",rootEmployee.status);
      element.setAttribute("assistant",Boolean.toString(rootEmployee.assistant));
      element.setAttribute("vacant",Boolean.toString(rootEmployee.vacant));
      element.setAttribute("layout",rootEmployee.layout);
      if (rootEmployee.icon != null) {
        element.setAttribute("icon",getIconName(rootEmployee.icon));
      }
      for(final Enumeration children = rootEmployee.children();children.hasMoreElements();) {
        final Employee child = (Employee) children.nextElement();
        element.appendChild(visit(child, doc));
      }
      return element;
    }

    private String getIconName(final Icon element) {
      final Set set = OrgChartReader.userIcons.keySet();
      for(final Iterator iterator = set.iterator();iterator.hasNext();) {
        final String iconName = (String) iterator.next();
        if (OrgChartReader.userIcons.get(iconName) == element) {
          return iconName;
        }
      }
      //Should not be reached
      return "";
    }
  }
  
  /**
   * A reader for XML-formatted XML-files.
   */
  static class OrgChartReader {

    static Map userIcons;
    static {    
      userIcons = new HashMap();
      for(int type = 0; type <= 1; type++) {
        final String gender = type == 0 ? "male" : "female";
        for(int user = 1; user <= 3; user++) {
          final String key = "usericon_" + gender + user;
          userIcons.put(key, new FixedSizeImageIcon(
                  getResource("resource/" + key + "_256.png"), 56, 64));
        }
      }
    }

    private static URL getResource( final String name ) {
      return demo.view.DemoBase.getSharedResource(name);
    }

    public OrgChartTreeModel read(final InputSource input) {
      final Document doc;
      
      try {
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        final Employee treeRoot = visit(doc);
        return new OrgChartTreeModel(treeRoot);
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      }    
      return null;
    }
    
    public Employee visit(final Node node)
    {
      final String nodeName = node.getNodeName();
      Employee employee = null;
      if("employee".equals(nodeName)) {
        employee = new Employee();        
        
        final NamedNodeMap attributes = node.getAttributes();
        Node attr = attributes.getNamedItem("name");
        if(attr != null) {
          employee.name = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("layout");      
        if(attr != null) {
          employee.layout = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("email");      
        if(attr != null) {
          employee.email = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("phone");      
        if(attr != null) {
          employee.phone = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("position");      
        if(attr != null) {
          employee.position = attr.getNodeValue();
        }    
        attr = attributes.getNamedItem("fax");      
        if(attr != null) {
          employee.fax = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("businessUnit");      
        if(attr != null) {
          employee.businessUnit = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("status");      
        if(attr != null) {
          employee.status = attr.getNodeValue();
        }
        attr = attributes.getNamedItem("icon");      
        if(attr != null) {
          final String iconName = attr.getNodeValue();
          employee.icon = (Icon) userIcons.get(iconName);                
        }
        attr = attributes.getNamedItem("assistant");      
        if(attr != null) {
          employee.assistant = "true".equalsIgnoreCase(attr.getNodeValue());
        }
        attr = attributes.getNamedItem("vacant");
        employee.vacant = (attr != null) && "true".equalsIgnoreCase(attr.getNodeValue());
      }    
      final NodeList nl = node.getChildNodes();
      for(int i=0, cnt=nl.getLength(); i<cnt; i++)
      {         
        final Node n = nl.item(i);
        final Employee childNode = visit(n);
        if(childNode != null && employee != null) {
          employee.add(childNode);
        }      
        if(childNode != null && employee == null) {
          return childNode;
        }
      }
      return employee;
    }
  }
  
  /**
   * Icon implementation that renders an image at a size that is 
   * different than the image dimensions. 
   */
  static class FixedSizeImageIcon extends ImageIcon {
        
    final int width;
    final int height;
    
    public FixedSizeImageIcon(final URL imageURL, final int width, final int height) {
      super(imageURL);    
      this.width = width;
      this.height = height;
    }
    
    public int getIconHeight() {
      return height;
    }

    public int getIconWidth() {
      return width;
    }

    public void paintIcon(final Component c, final Graphics gfx, final int x, final int y) {
      final Image image = getImage();
      final Graphics2D g2d = (Graphics2D) gfx.create();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.drawImage(image, x, y, getIconWidth(), getIconHeight(), c);
      g2d.dispose();
    }
  }
}
