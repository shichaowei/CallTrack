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
package demo.browser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import y.util.Comparators;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class XmlTreeBuilder
{
  private static final String JAVADOC_TAGS = "(?ms)^@.*";
  private static final String JAVADOC_LINK = "\\{@link(.*?)\\}";
  private static final String COMMENT_MARKERS =
          "(?m)^([ \\t\\x0B\\f\\r]*(/\\*{2,}|\\*+/?)[ \\t\\x0B\\f\\r]*)+";


  private final URL configResource;
  private final File baseDirectory;
  private final boolean useDemoClassDocumentationAsDescription;

  private XmlTreeBuilder(
          final URL configResource,
          final File baseDirectory,
          final boolean useDemoClassDocumentationAsDescription
  ) {
    this.configResource = configResource;
    this.baseDirectory = baseDirectory;
    this.useDemoClassDocumentationAsDescription = useDemoClassDocumentationAsDescription;
  }

  public TreeNode buildDemoTree() throws ConfigurationException {
    final FilterableTreeNode root =
            new FilterableTreeNode(new Displayable() {
              private final URL base;
              {
                URL tmp;
                try {
                  tmp = new URL(baseDirectory.toURL(), "demo/");
                } catch (MalformedURLException mue) {
                  tmp = null;
                }
                base = tmp;
              }

              public String getQualifiedName()
              {
                return "root";
              }

              public String getDisplayName()
              {
                return "root";
              }

              public String getSummary()
              {
                return null;
              }

              public String getDescription()
              {
                return "";
              }

              public boolean isDemo()
              {
                return false;
              }

              public boolean isExecutable()
              {
                return false;
              }

              public URL getBase()
              {
                return base;
              }

              public Set getKeywords() {
                return Collections.EMPTY_SET;
              }
            });

    final Document doc = parseXml();
    final Element ycontent = doc.getDocumentElement();
    final NodeList pkgs = ycontent.getChildNodes();
    for (int i = 0, n = pkgs.getLength(); i < n; ++i) {
      final Node node = pkgs.item(i);

      if (isElement(XmlTags.ELEMENT_PACKAGE, node)) {
        parsePackage((Element)node, root);
      }
    }

    return root;
  }

  private void parsePackage( final Element pkg, final FilterableTreeNode root )
          throws ConfigurationException {
    if (pkg.hasAttribute(XmlTags.ATTRIBUTE_BROWSER)) {
      if ("exclude".equals(pkg.getAttribute(XmlTags.ATTRIBUTE_BROWSER))) {
        return;
      }
    }

    final DemoGroup demoGroupData = new DemoGroup();
    final FilterableTreeNode node = new FilterableTreeNode(demoGroupData);
    root.add(node);

    if (pkg.hasAttribute(XmlTags.ATTRIBUTE_NAME)) {
      demoGroupData.qualifiedName = pkg.getAttribute(XmlTags.ATTRIBUTE_NAME);
    }

    demoGroupData.description = pkgToHtml(pkg);

    final NodeList children = pkg.getChildNodes();
    for (int i = 0, n = children.getLength(); i < n; ++i) {
      final Node child = children.item(i);
      if (Node.ELEMENT_NODE == child.getNodeType()) {
        final String name = child.getLocalName();
        if (XmlTags.ELEMENT_PACKAGE.equals(name)) {
          parsePackage((Element)child, node);
        } else if (XmlTags.ELEMENT_DEMO.equals(name)) {
          parseDemo((Element)child, node);
        } else if (XmlTags.ELEMENT_NAME.equals(name)) {
          demoGroupData.displayName = getContentAsText(child);
        } else if (XmlTags.ELEMENT_SUMMARY.equals(name)) {
          demoGroupData.summary = nodeToTooltip(child);
        } else if (XmlTags.ELEMENT_KEYWORDS.equals(name)) {
          demoGroupData.keywords = getKeywords(child);
        } else if (XmlTags.ELEMENT_PRIORITY.equals(name)) {
          try {
            demoGroupData.displayPriority =
                    Integer.parseInt(getContentAsText(child));
          } catch (NumberFormatException e) {
            // ignore - not fatal
          } catch (ConfigurationException e) {
            // ignore - not fatal
          }
        }
      }
    }

    if (pkg.hasAttribute(XmlTags.ATTRIBUTE_NAME)) {
      try {
        final String pkgName = pkg.getAttribute(XmlTags.ATTRIBUTE_NAME).replace('.', '/');
        demoGroupData.base = (new File(baseDirectory, pkgName)).toURL();
      } catch (MalformedURLException mue) {
        demoGroupData.base = null;
      }
    }

    sortChildren(node);

    // add the package/group keywords to each demo
    final Set categories = demoGroupData.keywords;
    if (categories != null) {
      demoGroupData.keywords = null;

      for (int i = 0, cc = node.getChildCount(); i < cc; ++i) {
        final Displayable data =
                ((FilterableTreeNode) node.getChildAt(i)).getUserObject();
        if (data instanceof Demo) {
          final Demo demo = (Demo) data;
          final Set keywords = demo.keywords;
          if (keywords == null) {
            demo.keywords = categories;
          } else {
            keywords.addAll(categories);
          }
        }
      }
    }
  }

  private void sortChildren( final FilterableTreeNode node ) {
    final FilterableTreeNode[] tmp = node.toArray();
    node.removeAllChildren();
    Comparators.sort(tmp, new Comparator() {
      public int compare(final Object o1, final Object o2) {
        final Object uo1 = ((FilterableTreeNode) o1).getUserObject();
        final Object uo2 = ((FilterableTreeNode) o2).getUserObject();

        final int t1 = uo1 instanceof DemoGroup ? 1 : uo1 instanceof Demo ? 2 : 3;
        final int t2 = uo2 instanceof DemoGroup ? 1 : uo2 instanceof Demo ? 2 : 3;

        if (t1 == t2) {
          if (t1 == 1) {
            final DemoGroup dg1 = (DemoGroup) uo1;
            final DemoGroup dg2 = (DemoGroup) uo2;
            if (dg1.displayPriority == dg2.displayPriority) {
              return dg2.getDisplayName().compareTo(dg2.getDisplayName());
            } else if (dg1.displayPriority < dg2.displayPriority) {
              return 1;
            } else {
              return -1;
            }
          } else if (t1 == 2) {
            final Demo d1 = (Demo) uo1;
            final Demo d2 = (Demo) uo2;
            if (d1.displayPriority == d2.displayPriority) {
              return 0;
            } else if (d1.displayPriority < d2.displayPriority) {
              return 1;
            } else {
              return -1;
            }
          } else {
            return 0;
          }
        } else {
          return t1 - t2;
        }
      }
    });
    for (int i = 0; i < tmp.length; ++i) {
      node.add(tmp[i]);
    }
  }

  private void parseDemo( final Element demo, final FilterableTreeNode root )
          throws ConfigurationException {
    if (demo.hasAttribute(XmlTags.ATTRIBUTE_BROWSER)) {
      if ("exclude".equals(demo.getAttribute(XmlTags.ATTRIBUTE_BROWSER))) {
        return;
      }
    }

    final Demo demoData = new Demo();
    final FilterableTreeNode demoNode = new FilterableTreeNode(demoData);
    root.add(demoNode);

    if (demo.hasAttribute(XmlTags.ATTRIBUTE_SOURCE) &&
        root.getUserObject() instanceof DemoGroup) {
      final String qn = root.getUserObject().getQualifiedName();
      demoData.sourcePath = qn.replace('.', '/') +
                            '/' +
                            demo.getAttribute(XmlTags.ATTRIBUTE_SOURCE);
    }

    if (demo.hasAttribute(XmlTags.ATTRIBUTE_NAME)) {
      demoData.qualifiedName = demo.getAttribute(XmlTags.ATTRIBUTE_NAME);
    }

    if (demo.hasAttribute(XmlTags.ATTRIBUTE_EXECUTABLE)) {
      demoData.executable = "true".equalsIgnoreCase(
              demo.getAttribute(XmlTags.ATTRIBUTE_EXECUTABLE));
    }

    if (demo.hasAttribute(XmlTags.ATTRIBUTE_BROWSER)) {
      demoData.executable = false;
    }

//    demoData.displayName = getContentAsText(
//            demo.getElementsByTagNameNS(XmlTags.YWORKS_NS_URI,
//                                        XmlTags.ELEMENT_NAME).item(0));

    final NodeList children = demo.getChildNodes();
//    for (int i = 0, n = children.getLength(); i < n; ++i) {

    //Headline + description from condensed.xml
    String longDescription = demoToHtml( demo );

    for (int i = children.getLength() - 1; i > -1; --i) {
      final Node child = children.item(i);
      if (Node.ELEMENT_NODE == child.getNodeType()) {
        final String name = child.getLocalName();
        if (XmlTags.ELEMENT_NAME.equals(name)) {
          demoData.displayName = getContentAsText(child);
        } else if (XmlTags.ELEMENT_SUMMARY.equals(name)) {
          demoData.summary = nodeToTooltip(child);
        } else if (XmlTags.ELEMENT_DESCRIPTION.equals(name)) {
          if (demoData.summary == null) {
            demoData.summary = nodeToTooltip(child);
          }
          demo.removeChild(child);
        } else if (XmlTags.ELEMENT_PRIORITY.equals(name)) {
          try {
            demoData.displayPriority = Integer.parseInt(getContentAsText(child));
          } catch (NumberFormatException e) {
            // ignore - not fatal
          } catch (ConfigurationException e) {
            // ignore - not fatal
          }
        } else if (XmlTags.ELEMENT_KEYWORDS.equals(name)) {
          demoData.keywords = getKeywords(child);
        }
      }
    }

    //Headline from condensed.xml
    String shortDescription = demoToHtml( demo );
    demoData.description = longDescription;

    final Element pkg = (Element)demo.getParentNode();
    final String pkgName = pkg.getAttribute(XmlTags.ATTRIBUTE_NAME).replace('.', '/');
    if (pkg.hasAttribute(XmlTags.ATTRIBUTE_NAME)) {
      try {
        demoData.base = (new File(baseDirectory, pkgName)).toURL();
      } catch (MalformedURLException mue) {
        demoData.base = null;
      }
    }

    String comment = null;

    if (useDemoClassDocumentationAsDescription) {
      final String className = demoData.qualifiedName.substring(pkgName.length() + 1);
      final String source = demoData.readSource();

      final int classDeclarationIndex = source.indexOf("class " + className);
      if (classDeclarationIndex > -1) {
        comment = source.substring(0, classDeclarationIndex);
        final int classCommentBeginIndex = comment.lastIndexOf("/**");
        if (classCommentBeginIndex > -1) {
          final int classCommentEndIndex = comment.indexOf("*/", classCommentBeginIndex);
          comment = comment.substring(classCommentBeginIndex, classCommentEndIndex);
          comment = comment.replaceAll(COMMENT_MARKERS, "");
          comment = comment.replaceAll(JAVADOC_TAGS, "");
          comment = comment.replaceAll("<br/>", "<br>");
          comment = comment.replaceAll(JAVADOC_LINK, "<font color=\"#0000C0\">$1</font>");
        } else {
          comment = null;
        }
      }
    }

    if (comment == null) {
      demoData.description = longDescription.replaceFirst(
                      "</body>\\s*</html>",
                      "<table><tr valign=\"top\"><td style=\"background-color:#ffffff;\">" +
                      "</td></tr></table></body></html>");
    } else {
      demoData.description = shortDescription.replaceFirst(
                      "</body>\\s*</html>",
                      "<table><tr valign=\"top\"><td style=\"background-color:#ffffff;\">" +
                      comment +
                      "</td></tr></table></body></html>");
    }
  }

  private Document parseXml() throws ConfigurationException
  {
    try {
      final DocumentBuilder db = createDocumentBuilder();
      return db.parse(configResource.openStream());
    } catch (ParserConfigurationException pce) {
      throw new ConfigurationException(pce);
    } catch (SAXException se) {
      throw new ConfigurationException(se);
    } catch (IOException ioe) {
      throw new ConfigurationException(ioe);
    }
  }


  public static XmlTreeBuilder newInstance( final String resourceName )
  {
    final URL resource = XmlTreeBuilder.class.getResource(resourceName);
    if (resource != null) {
      int depth = 0;
      for (int i = 0, n = resourceName.length(); i < n; ++i) {
        if ('/' == resourceName.charAt(i)) {
          ++depth;
        }
      }
      File base = new File(resource.getFile());
      for (int i = 0; i < depth && base != null; ++i) {
        base = base.getParentFile();
      }
      return new XmlTreeBuilder(resource, base, false);
    } else {
      return null;
    }
  }

  private static Set getKeywords( final Node node ) {
    if (node.hasChildNodes()) {
      final Set hints = new HashSet(node.getChildNodes().getLength());
      for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (isElement(XmlTags.ELEMENT_KEYWORD, c)) {
          hints.add(getKeyword(c));
        }
      }
      hints.remove(null);
      return hints;
    } else {
      return null;
    }
  }

  private static String getKeyword( final Node node ) {
    if (node.hasChildNodes()) {
      final Node child = node.getFirstChild();
      if (Node.TEXT_NODE == child.getNodeType()) {
        return getText(node);
      }
    }
    return null;
  }


  private static boolean isElement( final String name, final Node node ) {
    return Node.ELEMENT_NODE == node.getNodeType() &&
           name.equals(node.getLocalName());
  }

  private static String getContentAsText( final Node node )
          throws ConfigurationException
  {
    final NodeList children = node.getChildNodes();
    final int childCount = children.getLength();
    if (childCount == 1) {
      final Node child = children.item(0);
      if (Node.TEXT_NODE == child.getNodeType()) {
        return getText(node);
      } else {
        return nodeToXml(child);
      }
    } else if (childCount > 1) {
      final Element htmlRoot =
              node.getOwnerDocument().createElement(XmlTags.ELEMENT_HTML);
      final List tmp = new ArrayList(childCount);
      for (int i = childCount - 1; i > -1; --i) {
        tmp.add(node.removeChild(children.item(i)));
      }
      for (int i = 0; i < childCount; ++i) {
        htmlRoot.appendChild((Node)tmp.get(i));
      }
      node.appendChild(htmlRoot);
      return nodeToXml(htmlRoot);
    } else {
      return "";
    }
  }

  private static String nodeToXml( final Node node )
          throws ConfigurationException
  {
    final StringWriter sw = new StringWriter();
    try {
      XmlTransformerFactory.plain().transform(new DOMSource(node),
                                              new StreamResult(sw));
    } catch (TransformerConfigurationException tce) {
      throw new ConfigurationException(tce);
    } catch (TransformerException te) {
      throw new ConfigurationException(te);
    }
    return sw.toString();
  }

  private static String demoToHtml( final Node node )
          throws ConfigurationException
  {
    final StringWriter sw = new StringWriter();
    try {
      XmlTransformerFactory.demo().transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerConfigurationException tce) {
      throw new ConfigurationException(tce);
    } catch (TransformerException te) {
      throw new ConfigurationException(te);
    }
    return normalize(sw);
  }

  private static String pkgToHtml( final Node node )
          throws ConfigurationException
  {
    final StringWriter sw = new StringWriter();
    try {
      XmlTransformerFactory.pkg().transform(new DOMSource(node),
                                            new StreamResult(sw));
    } catch (TransformerConfigurationException tce) {
      throw new ConfigurationException(tce);
    } catch (TransformerException te) {
      throw new ConfigurationException(te);
    }
    return normalize(sw);
  }

  private static String nodeToTooltip( final Node node )
          throws ConfigurationException
  {
    final StringWriter sw = new StringWriter();
    try {
      XmlTransformerFactory.tooltip().transform(new DOMSource(node),
                                                new StreamResult(sw));
    } catch (TransformerConfigurationException tce) {
      throw new ConfigurationException(tce);
    } catch (TransformerException te) {
      throw new ConfigurationException(te);
    }
    String tooltip = normalize(sw);
    if (tooltip != null) {
      tooltip = tooltip.trim();
      if (tooltip.length() == 0) {
        tooltip = null;
      }
    }
    return tooltip;
  }

  private static String normalize( final StringWriter sw )
  {
    return sw.toString().replaceAll("(\\s*)xmlns(\\S*)\"", "")
                        .replaceAll("<(?i)META[^>]*>", "");
  }

  private static String getText( final Node node )
  {
    return ((Text)node.getFirstChild()).getData();
  }

  private static DocumentBuilder createDocumentBuilder()
          throws ParserConfigurationException
  {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(false);
    dbf.setCoalescing(false);
    dbf.setNamespaceAware(true);

    return dbf.newDocumentBuilder();
  }
}