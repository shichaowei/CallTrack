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
package demo.view.entityrelationship;

import demo.view.entityrelationship.painters.ErdRealizerFactory;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a converter that provides method to change the notation for an Entity Relationship Diagram (ERD).
 *
 * <p> It is possible to convert from Chen to Crow's Foot and vice versa
 * ({@link #convertToCrowFoot(y.view.Graph2D)} and {@link #convertToChen(y.view.Graph2D)}). </p>
 */
class ErdNotationConverter {

  /**
   * Converts an ERD graph from Chen to Crow's Foot notation.
   *
   * If the graph is already in Crow's Foot notation, the structure remains
   * the same.
   * @param graph the graph to be converted
   */
  static void convertToCrowFoot(Graph2D graph) {

    // Back up all realizers to provide undo-functionality for the whole method
    graph.backupRealizers();

    // Map that holds <relationship, list of neighbor entities> sets
    Map relations = new HashMap();
    // Map that holds <entity, list of attributes> sets
    Map attributes = new HashMap();

    // First all neighbors of every entity nodes are stored in <code>HashMap</code>s
    // for relationships and attributes
    for (NodeCursor nodeCursor = graph.nodes(); nodeCursor.ok(); nodeCursor.next()) {
      Node node = nodeCursor.node();
      NodeRealizer nodeRealizer = graph.getRealizer(node);
      if (ErdRealizerFactory.isSmallEntityRealizer(nodeRealizer)) {
        NodeList attrList = new NodeList();
        attributes.put(node, attrList);
        for (NodeCursor cursor = node.neighbors(); cursor.ok(); cursor.next()) {
          Node neighbor = cursor.node();
          NodeRealizer neighborRealizer = graph.getRealizer(neighbor);
          if (ErdRealizerFactory.isAttributeRealizer(neighborRealizer)) {
            attrList.add(neighbor);
          } else {
            if (ErdRealizerFactory.isRelationshipRealizer(neighborRealizer)) {
              NodeList relList = new NodeList();
              if (!relations.containsKey(neighbor)) {
                relations.put(neighbor, relList);
              }
              relList = (NodeList) relations.get(neighbor);
              relList.add(node);
            }
          }
        }
      }
    }

    StringBuffer buffer = new StringBuffer();

    // For every entity a list of attributes is generated and the attribute nodes
    // are removed from the graph.
    // Then the <code>NodeRealizer</code> is changed to a big entity realizer and
    // the size is adapted.
    for (Iterator it = attributes.keySet().iterator(); it.hasNext(); ) {
      Node node = (Node) it.next();
      NodeList attrList = (NodeList) attributes.get(node);
      for (NodeCursor cursor = attrList.nodes(); cursor.ok(); cursor.next()) {
        Node attrNode = cursor.node();
        buffer = buffer.append(graph.getLabelText(attrNode)).append("\n");
        graph.removeNode(attrNode);
      }

      NodeRealizer bigEntity = ErdRealizerFactory.createBigEntity();
      NodeLabel nameLabel = bigEntity.getLabel(0);
      NodeLabel attrLabel = bigEntity.getLabel(1);
      nameLabel.setText(graph.getLabelText(node));
      attrLabel.setText(buffer.toString());
      buffer.setLength(0);
      bigEntity.setLocation(graph.getRealizer(node).getX(), graph.getRealizer(node).getY());
      graph.setRealizer(node, bigEntity);

      double newHeight = 0;
      double newWidth = 0;
      for (int i = 0; i < bigEntity.labelCount(); i++) {
        newHeight += bigEntity.getLabel(i).getBox().getHeight();
        newWidth = Math.max(newWidth, bigEntity.getLabel(i).getBox().getWidth());
      }
      if (newHeight > bigEntity.getHeight() - 10) {
        bigEntity.setHeight(newHeight + 15);
      }
      if (newWidth > bigEntity.getWidth() - 10) {
        bigEntity.setWidth(newWidth + 15);
      }
    }

    // In place of every relation an edge is created with appropriate arrows. Then
    // the relationship node is removed from the graph.
    for (Iterator it = relations.keySet().iterator(); it.hasNext(); ) {
      Node relation = (Node) it.next();
      NodeList relList = (NodeList) relations.get(relation);
      if (relation.degree() > relList.size()) {

        // If there are attributes for the relationship, they are added within brackets
        // to the edge label
        for (NodeCursor cursor = relation.neighbors(); cursor.ok(); cursor.next()) {
          final Node node = cursor.node();

          if (!relList.contains(node)) {
            buffer.append("\n(");
            buffer.append(graph.getLabelText(node));
            buffer.append(")");
            graph.removeNode(node);
          }
        }
      }
      Node[] relNodes = relList.toNodeArray();
      for (int i = 0; i < relNodes.length; i++) {
        for (int j = i + 1; j < relNodes.length; j++) {
          String labelText1 = graph.getLabelText(relation.getEdge(relNodes[i]));
          String labelText2 = graph.getLabelText(relation.getEdge(relNodes[j]));
          Arrow sourceArrow = getArrow(labelText1);
          Arrow targetArrow = getArrow(labelText2);
          Edge edge = graph.createEdge(relNodes[i], relNodes[j],
              ErdRealizerFactory.createRelation(sourceArrow, targetArrow));
          String edgeLabel = graph.getLabelText(relation) + buffer;
          if (edgeLabel.length() > 0) {
            graph.setLabelText(edge, edgeLabel);
          }
          buffer.setLength(0);
        }
      }
      graph.removeNode(relation);
    }
  }

  /**
   * Returns a Crow's Foot <code>Arrow</code> for the given multiplicity <code>String</code>.
   * @param text a string that describes the multiplicity of the relationship
   * @return an appropriate Crow's Foot <code>Arrow</code>
   */
  private static Arrow getArrow(String text) {
    Arrow arrow = Arrow.NONE;
    if ("1".equals(text)) {
      arrow = Arrow.CROWS_FOOT_ONE;
    } else {
      if ("(0,1)".equals(text)) {
        arrow = Arrow.CROWS_FOOT_ONE_OPTIONAL;
      } else {
        if ("(1,1)".equals(text)) {
          arrow = Arrow.CROWS_FOOT_ONE_MANDATORY;
        } else {
          if ("M".equalsIgnoreCase(text) || "N".equalsIgnoreCase(text)) {
            arrow = Arrow.CROWS_FOOT_MANY;
          } else {
            if ("(0,N)".equalsIgnoreCase(text) || "(0,M)".equalsIgnoreCase(text)) {
              arrow = Arrow.CROWS_FOOT_MANY_OPTIONAL;
            } else {
              if ("(1,N)".equalsIgnoreCase(text) || "(1,M)".equalsIgnoreCase(text)) {
                arrow = Arrow.CROWS_FOOT_MANY_MANDATORY;
              }
            }
          }
        }
      }
    }
    return arrow;
  }

  /**
   * Converts an ERD graph from Crow's Foot to Chen notation.
   *
   * If the graph is already in Chen notation, the structure remains
   * the same.
   * @param graph the graph to be converted
   */
  static void convertToChen(Graph2D graph) {

    // Back up all realizers to provide undo-functionality for the whole method
    graph.backupRealizers();

    // Replace every relationship node with an edge and set the relationship name
    // as edge label. Also create the appropriate Crow's Foot arrows.
    EdgeList edgeList = new EdgeList(graph.getEdgeList());
    for (EdgeCursor cursor = edgeList.edges(); cursor.ok(); cursor.next()) {
      final Edge edge = cursor.edge();

      if (ErdRealizerFactory.isBigEntityRealizer(graph.getRealizer(edge.source()))
          && ErdRealizerFactory.isBigEntityRealizer(graph.getRealizer(edge.target()))) {
        final EdgeRealizer edgeRealizer = graph.getRealizer(edge);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < edgeRealizer.labelCount(); i++) {
          if (i > 0) {
            buffer.append(" / ");
          }
          buffer.append(edgeRealizer.getLabel(i).getText());
        }

        String relString = buffer.toString().replaceAll("[\\)]|\n", "");
        String[] relText = relString.split("\\(");
        Node relation = graph.createNode(ErdRealizerFactory.createRelationship(relText[0]));
        for (int i = 1; i < relText.length; i++) {
          Node attribute = graph.createNode(ErdRealizerFactory.createAttribute(relText[i]));
          graph.createEdge(relation, attribute, ErdRealizerFactory.createRelation(Arrow.NONE, Arrow.NONE));
        }
        buffer.setLength(0);

        Edge sourceEdge = graph.createEdge(edge.source(), relation);
        graph.setRealizer(sourceEdge, ErdRealizerFactory.createRelation(Arrow.NONE));
        graph.getRealizer(sourceEdge).setLabelText(getLabel(edgeRealizer.getSourceArrow()));
        Edge targetEdge = graph.createEdge(relation, edge.target());
        graph.setRealizer(targetEdge, ErdRealizerFactory.createRelation(Arrow.NONE));
        graph.getRealizer(targetEdge).setLabelText(getLabel(edgeRealizer.getTargetArrow()));

        graph.removeEdge(edge);
      }
    }

    // Create attribute nodes for every line in the attributes label and attach them to
    // the entity. Also change the entity's realizer to a small entity node realizer and
    // adjust its size.
    for (NodeCursor cursor = graph.nodes(); cursor.ok(); cursor.next()) {
      final Node node = cursor.node();

      final NodeRealizer nodeRealizer = graph.getRealizer(node);
      if (ErdRealizerFactory.isBigEntityRealizer(nodeRealizer)) {
        String text = nodeRealizer.getLabel(1).getText();
        text = text.replaceAll("<html>|<br>|</?u>", "");
        String[] attributes = text.split("\n");
        for (int i = 0; i < attributes.length; i++) {
          if (attributes[i].length() > 0) {
            Node attrNode = graph.createNode(ErdRealizerFactory.createAttribute(attributes[i]));
            Edge attrEdge = graph.createEdge(node, attrNode);
            graph.setRealizer(attrEdge, ErdRealizerFactory.createRelation(Arrow.NONE));
          }
        }
        final GenericNodeRealizer smallEntity = ErdRealizerFactory.createSmallEntity(
            nodeRealizer.getLabel(0).getText());
        graph.setRealizer(node, smallEntity);
        double newHeight = 0;
        double newWidth = 0;
        for (int i = 0; i < smallEntity.labelCount(); i++) {
          newHeight += smallEntity.getLabel(i).getBox().getHeight();
          newWidth = Math.max(newWidth, smallEntity.getLabel(i).getBox().getWidth());
        }
        if (newHeight > smallEntity.getHeight() - 10) {
          smallEntity.setHeight(newHeight + 15);
        }
        if (newWidth > smallEntity.getWidth() - 10) {
          smallEntity.setWidth(newWidth + 15);
        }

      }
    }
  }

  /**
   * Returns a multiplicity <code>String</code> for the specified Crow's Foot <code>Arrow</code>.
   * @param arrow a Crow's Foot <code>Arrow</code> that shows the multiplicity of the relationship
   * @return a multiplicity string for Chen notation
   */
  private static String getLabel(Arrow arrow) {
    String label = "";
    if (arrow.equals(Arrow.CROWS_FOOT_ONE)) {
      label = "1";
    } else {
      if (arrow.equals(Arrow.CROWS_FOOT_ONE_OPTIONAL)) {
        label = "(0,1)";
      } else {
        if (arrow.equals(Arrow.CROWS_FOOT_ONE_MANDATORY)) {
          label = "(1,1)";
        } else {
          if (arrow.equals(Arrow.CROWS_FOOT_MANY)) {
            label = "N";
          } else {
            if (arrow.equals(Arrow.CROWS_FOOT_MANY_OPTIONAL)) {
              label = "(0,N)";
            } else {
              if (arrow.equals(Arrow.CROWS_FOOT_MANY_MANDATORY)) {
                label = "(1,N)";
              }
            }
          }
        }
      }
    }
    return label;
  }
}