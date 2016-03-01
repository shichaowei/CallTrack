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
package demo.layout.genealogy.iohandler;

import y.base.DataAcceptor;
import y.base.DataProvider;
import y.base.Node;
import y.layout.genealogy.FamilyTreeLayouter;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.SmartNodeLabelModel;
import y.view.YLabel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gets the information of a GEDCOM line and builds a graph with it.
 * <p/>
 * This class provides several callback methods to customize the handling of GEDCOM tags. In this implementation, the
 * tags INDI, FAM, WIFE, HUSB and CHIL are used to get the graph structure and the tags NAME, BIRT, DEAT and DATE are
 * used to create labels with the according information.
 */
public class GedcomInputHandlerImpl implements GedcomInputHandler {
  public static final Color DEFAULT_COLOR_FEMALE = new Color(204, 204, 255);
  public static final Color DEFAULT_COLOR_MALE = new Color(255, 204, 153);
  private static final String LABEL_CONFIG = "CroppingLabel";

  private final Graph2D graph;
  private final Map ids2nodes;
  private Node currentNode;
  private NodeLabel currentNodeLabel;

  /**
   * Initializes a new <code>GedcomInputHandlerImpl</code> for the given
   * graph structure.
   */
  public GedcomInputHandlerImpl(Graph2D graph) {
    this.graph = graph;
    ids2nodes = new HashMap();
  }

  /**
   * Returns the graph structure for which this handler was created.
   * @return the graph structure for which this handler was created.
   */
  protected Graph2D getGraph() {
    return graph;
  }

  /**
   * Reacts to the beginning of the GEDCOM file. By default nothing happens.
   */
  public void handleStartDocument() {
  }

  /**
   * Reacts to the end of the GEDCOM file. By default nothing happens.
   */
  public void handleEndDocument() {
  }

  /**
   * Processes the given information according to the value of the tag.
   *
   * @param id    the id from the GEDCOM line (might be <code>null</code>)
   * @param tag   the tag from the GEDCOM line
   * @param value the value from the GEDCOM line (might be <code>null</code>)
   */
  public void handleStartTag(int level, String id, String tag, String value) {
    if ("INDI".equals(tag)) {
      handleIndividualTag(graph, level, id);
    } else if ("FAM".equals(tag)) {
      handleFamilyTag(graph, level, id);
    } else if ("NAME".equals(tag)) {
      handleNameTag(graph, currentNode, level, value);
    } else if ("SEX".equals(tag)) {
      handleSexTag(graph, currentNode, level, value);
    } else if ("BIRT".equals(tag)) {
      handleBirthTag(graph, currentNode, level, value);
    } else if ("DEAT".equals(tag)) {
      handleDeathTag(graph, currentNode, level, value);
    } else if ("DATE".equals(tag)) {
      handleDateTag(graph, currentNode, currentNodeLabel, level, value);
    } else if ("HUSB".equals(tag)) {
      handleHusbandTag(graph, currentNode, level, value);
    } else if ("WIFE".equals(tag)) {
      handleWifeTag(graph, currentNode, level, value);
    } else if ("CHIL".equals(tag)) {
      handleChildTag(graph, currentNode, level, value);
    } else {
      handleMiscTag(graph, currentNode, currentNodeLabel, level, id, tag, value);
    }
  }

  /**
   * Cleans up when a level ends.
   *
   * @param tag the tag of the level that ends
   */
  public void handleEndTag(int level, String tag) {
    handleEndTag(graph, currentNode, currentNodeLabel, level, tag);
    if ("INDI".equals(tag) || "FAM".equals(tag)) {
      currentNode = null;
    } else if ("NAME".equals(tag) || "BIRT".equals(tag) || "DEAT".equals(tag) || "MARR".equals(tag)
        || "DATE".equals(tag)) {
      currentNodeLabel = null;
    }
  }
  
  protected void handleEndTag(Graph2D graph, Node node, NodeLabel label, int level, String tag) {
  }

  /**
   * Starts building an individual node in the graph.
   *
   * @param graph the graph that is built
   * @param id    the id of the individual
   */
  protected void handleIndividualTag(Graph2D graph, int level, String id) {
    currentNode = (Node) ids2nodes.get(id.trim());
    if (currentNode == null) {
      currentNode = createIndividualNode(graph);
      ids2nodes.put(id.trim(), currentNode);
    }
  }

  /**
   * Creates a node for an individual in the family tree.
   * <p/>
   * To customize the representation of individuals,
   * this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the new individual node
   */
  protected Node createIndividualNode(Graph2D graph) {
    return graph.createNode(createIndividualNodeRealizer(graph));
  }

  /**
   * Creates the visual representation of an individual.
   * <p/>
   * To customize the visual representation of individuals,
   * this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the node realizer representing an individual
   */
  protected NodeRealizer createIndividualNodeRealizer(Graph2D graph) {
    return graph.getDefaultNodeRealizer().createCopy();
  }

  /**
   * Starts building a family node in the graph.
   *
   * @param graph the graph to be built
   * @param id    the id of the family
   */
  protected void handleFamilyTag(Graph2D graph, int level, String id) {
    currentNode = (Node) ids2nodes.get(id.trim());
    if (currentNode == null) {
      currentNode = createFamilyNode(graph);
      DataProvider dp = graph.getDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE);
      if (dp instanceof DataAcceptor) {
        ((DataAcceptor) dp).set(currentNode, FamilyTreeLayouter.TYPE_FAMILY);
      }
      ids2nodes.put(id.trim(), currentNode);
    }
  }

  /**
   * Creates a node for a family in the family tree.
   * <p/>
   * To customize the representation of families,
   * this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the new family node
   */
  protected Node createFamilyNode(Graph2D graph) {
    return graph.createNode(createFamilyNodeRealizer(graph));
  }

  /**
   * Creates the visual representation of a family.
   * <p/>
   * To customize the visual representation of families,
   * this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the node realizer representing a family
   */
  protected NodeRealizer createFamilyNodeRealizer(Graph2D graph) {
    return graph.getDefaultNodeRealizer().createCopy();
  }

  /**
   * Starts adding a name to an individual.
   * <p/>
   * To customize the representation of the name this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node the label belongs to
   * @param value the name text
   */
  protected void handleNameTag(Graph2D graph, Node node, int level, String value) {
    if (node != null) {
      final String name = value.replaceFirst("/", "\n").replaceAll("/", " ").trim();
      final NodeLabel label = createLabel(name);
      final SmartNodeLabelModel model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_TOP));
      graph.getRealizer(node).setLabel(label);
    }
  }

  /**
   * Creates the label for the name of an individual.
   * <p/>
   * To customize the representation of the name this method may be overwritten.
   *
   * @param name the name text
   * @return the label with the name
   */
  protected NodeLabel createLabel(String name) {
    currentNodeLabel = new NodeLabel(name);
    return currentNodeLabel;
  }

  /**
   * Changes the color of the current node and adds an entry in the <code>DataProvider</code> for the {@link
   * FamilyTreeLayouter} according to the sex of the individual.
   * <p/>
   * To customize the reaction to this tag, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node
   * @param value the indication of the sex (F=female, M=male)
   */
  protected void handleSexTag(Graph2D graph, Node node, int level, String value) {
    if (node != null) {
      String type;
      Color color;
      if ("F".equals(value)) {
        type = FamilyTreeLayouter.TYPE_FEMALE;
        color = DEFAULT_COLOR_FEMALE;
      } else {
        type = FamilyTreeLayouter.TYPE_MALE;
        color = DEFAULT_COLOR_MALE;
      }
      final NodeRealizer realizer = graph.getRealizer(node);
      realizer.setFillColor(color);
      realizer.setLineColor(null);
      DataProvider dp = graph.getDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE);
      if (dp instanceof DataAcceptor) {
        ((DataAcceptor) dp).set(node, type);
      }
    }
  }

  /**
   * Starts creating a label for the date of birth of an individual.
   * <p/>
   * To customize the representation to the date of birth, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node
   * @param value the value of the GEDCOM line
   */
  protected void handleBirthTag(Graph2D graph, Node node, int level, String value) {
    if (node != null) {
      final NodeLabel label = createLabel("* ");
      Set configurations = NodeLabel.getFactory().getAvailableConfigurations();
      if (configurations.contains(LABEL_CONFIG)) {
        label.setConfiguration(LABEL_CONFIG);
        label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
        label.setContentSize(graph.getRealizer(node).getWidth() * 0.5, 20.0);
      }
      label.setAlignment(YLabel.ALIGN_LEFT);
      final SmartNodeLabelModel model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_BOTTOM_LEFT));
      graph.getRealizer(node).addLabel(label);
    }
  }

  /**
   * Starts creating a label for the date of death of an individual.
   * <p/>
   * To customize the representation of the date of death, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node
   * @param value the value of the GEDCOM line
   */
  protected void handleDeathTag(Graph2D graph, Node node, int level, String value) {
    if (node != null) {
      final NodeLabel label = createLabel("\u271D ");
      Set configurations = NodeLabel.getFactory().getAvailableConfigurations();
      if (configurations.contains(LABEL_CONFIG)) {
        label.setConfiguration(LABEL_CONFIG);
        label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
        label.setContentSize(graph.getRealizer(node).getWidth() * 0.5, 20.0);
      }
      label.setAlignment(YLabel.ALIGN_RIGHT);
      final SmartNodeLabelModel model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_BOTTOM_RIGHT));
      graph.getRealizer(node).addLabel(label);
    }
  }

  /**
   * Adds the date text to a currently active label.
   * <p/>
   * This implementation writes the date as it comes from the GEDCOM file. To customize the representation of dates,
   * this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node
   * @param label the currently changed label
   * @param value the date text
   */
  protected void handleDateTag(Graph2D graph, Node node, NodeLabel label, int level, String value) {
    if (label != null) {
      label.setText(label.getText() + value);
    }
  }

  /**
   * Creates an edge between the wife node identified by the given id and the currently active family node.
   * <p/>
   * To customize the creation of this edge, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed family node
   * @param id    the id of the wife
   */
  protected void handleWifeTag(Graph2D graph, Node node, int level, String id) {
    if (node != null && isValidID(id)) {
      Node wife = getNodeByID(id);
      if (wife == null && id.length() > 0) {
        wife = createIndividualNode(graph);
        ids2nodes.put(id, wife);
      }
      graph.createEdge(wife, node, createWifeFamilyEdgeRealizer(graph));
    }
  }

  /**
   * Creates a realizer for an edge between the wife node and the family node.
   * <p/>
   * To customize the representation of the edge from the wife to the family node, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the edge realizer
   */
  protected EdgeRealizer createWifeFamilyEdgeRealizer(Graph2D graph) {
    return graph.getDefaultEdgeRealizer().createCopy();
  }

  /**
   * Creates an edge between the husband node identified by the given id and the currently active family node.
   * <p/>
   * To customize the creation of this edge, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed family node
   * @param id    the id of the husband
   */
  protected void handleHusbandTag(Graph2D graph, Node node, int level, String id) {
    if (node != null && isValidID(id)) {
      Node husband = getNodeByID(id);
      if (husband == null) {
        husband = createIndividualNode(graph);
        ids2nodes.put(id, husband);
      }
      graph.createEdge(husband, node, createHusbandFamilyEdgeRealizer(graph));
    }
  }

  /**
   * Creates a realizer for an edge between the husband node and the family node.
   * <p/>
   * To customize the representation of the edge from the husband to the family node, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the edge realizer
   */
  protected EdgeRealizer createHusbandFamilyEdgeRealizer(Graph2D graph) {
    return graph.getDefaultEdgeRealizer().createCopy();
  }

  /**
   * Creates an edge between the child node identified by the given id and the currently active family node.
   * <p/>
   * To customize the creation of this edge, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @param node  the currently changed family node
   * @param id    the id of the child
   */
  protected void handleChildTag(Graph2D graph, Node node, int level, String id) {
    if (node != null && isValidID(id)) {
      Node child = getNodeByID(id);
      if (child == null && id.length() > 0) {
        child = createIndividualNode(graph);
        ids2nodes.put(id, child);
      }
      graph.createEdge(node, child, createFamilyChildEdgeRealizer(graph));
    }
  }

  /**
   * Creates a realizer for an edge between the family node and the child node.
   * <p/>
   * To customize the representation of the edge from the family to the child node, this method may be overwritten.
   *
   * @param graph the graph to be built
   * @return the edge realizer
   */
  protected EdgeRealizer createFamilyChildEdgeRealizer(Graph2D graph) {
    return graph.getDefaultEdgeRealizer().createCopy();
  }

  /**
   * Handles miscellaneous GEDCOM tags.
   * <p/>
   * By default nothing happens. To handle a custom range of tags, overwrite this method.
   *
   * @param graph the graph to be built
   * @param node  the currently changed node
   * @param label the currently changed label
   * @param id    the id from the GEDCOM line (might be <code>null</code>)
   * @param tag   the tag from the GEDCOM line
   * @param value the value from the GEDCOM line (might be <code>null</code>)
   */
  protected void handleMiscTag(Graph2D graph, Node node, NodeLabel label,
                               int level, String id, String tag, String value) {
  }

  /**
   * Returns the node of the specified id.
   *
   * @param id the id of the node to return
   * @return the node of the specified id
   */
  public Node getNodeByID(String id) {
    return (Node) ids2nodes.get(id);
  }

  /**
   * Validates if the given id is a GEDCOM id. The id has to start and end with an @.
   *
   * @param id the id to be checked
   * @return <code>true</code>, if the id is valid, <code>false</code> otherwise
   */
  static boolean isValidID(String id) {
    return id != null && id.startsWith("@") && id.endsWith("@");
  }
}