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
import demo.view.flowchart.FlowchartPalette;
import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a component, which provides templates for entity relationship diagram (ERD)
 * nodes and edges that can be dragged into a Graph2DView.
 */
public class EntityRelationshipPalette extends FlowchartPalette {

  private Map arrowNames;
  private Map nodeNames;

  /**
   * Creates a new <code>EntityRelationshipPalette</code> with a pre-configured list of
   * node and edge realizers.
   * @param view a view of the graph the palette is assigned to
   */
  public EntityRelationshipPalette(final Graph2DView view) {
    super(view);

    arrowNames = new HashMap();
    arrowNames.put(Arrow.NONE, "Unspecified");
    arrowNames.put(Arrow.CROWS_FOOT_ONE, "(1)");
    arrowNames.put(Arrow.CROWS_FOOT_MANY, "(N)");
    arrowNames.put(Arrow.CROWS_FOOT_ONE_OPTIONAL, "(0,1)");
    arrowNames.put(Arrow.CROWS_FOOT_ONE_MANDATORY, "(1,1)");
    arrowNames.put(Arrow.CROWS_FOOT_MANY_OPTIONAL, "(0,N)");
    arrowNames.put(Arrow.CROWS_FOOT_MANY_MANDATORY, "(1,N)");
  }

  /**
   * Initializes default realizers for the specified view's associated graph.
   * @param view The respective Graph2DView.
   */
  protected void initializeDefaultRealizers(Graph2DView view) {
    Graph2D graph2D = view.getGraph2D();
    graph2D.setDefaultNodeRealizer(ErdRealizerFactory.createBigEntity());
    graph2D.setDefaultEdgeRealizer(ErdRealizerFactory.createRelation(Arrow.NONE));
  }

  /**
   * Adds default ERD templates to the palette list.
   * @param realizers The list of all template realizers
   */
  protected void addDefaultTemplates(final List realizers) {

    //create node templates
    final NodeRealizer bigEntity = ErdRealizerFactory.createBigEntity();
    final GenericNodeRealizer smallEntity = ErdRealizerFactory.createSmallEntity("Entity Name");
    final GenericNodeRealizer weakSmallEntity = ErdRealizerFactory.createWeakSmallEntity("Entity Name");
    final GenericNodeRealizer attribute = ErdRealizerFactory.createAttribute("Attribute");
    final GenericNodeRealizer multiValuedAttribute = ErdRealizerFactory.createMultiValuedAttribute("Attribute");
    final GenericNodeRealizer primaryKeyAttribute = ErdRealizerFactory.createPrimaryKeyAttribute("Attribute");
    final GenericNodeRealizer derivedAttribute = ErdRealizerFactory.createDerivedAttribute("Attribute");
    final GenericNodeRealizer relationship = ErdRealizerFactory.createRelationship("Relation");
    final GenericNodeRealizer weakRelationship = ErdRealizerFactory.createWeakRelationship("Relation");

    //add node templates to list
    realizers.add(bigEntity);
    realizers.add(smallEntity);
    realizers.add(weakSmallEntity);
    realizers.add(attribute);
    realizers.add(multiValuedAttribute);
    realizers.add(primaryKeyAttribute);
    realizers.add(derivedAttribute);
    realizers.add(relationship);
    realizers.add(weakRelationship);

    //register tooltips for node templates
    nodeNames = new HashMap();
    nodeNames.put(bigEntity, "Entity with Attributes");
    nodeNames.put(smallEntity, "Entity");
    nodeNames.put(weakSmallEntity, "Weak Entity");
    nodeNames.put(attribute, "Attribute");
    nodeNames.put(multiValuedAttribute, "Multi-Valued Attribute");
    nodeNames.put(primaryKeyAttribute, "Primary Key");
    nodeNames.put(derivedAttribute, "Derived Attribute");
    nodeNames.put(relationship, "Relationship");
    nodeNames.put(weakRelationship, "Weak Relationship");

    //add edge templates to list
    realizers.add(ErdRealizerFactory.createRelation(Arrow.NONE));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE_OPTIONAL));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_ONE_MANDATORY));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY_OPTIONAL));
    realizers.add(ErdRealizerFactory.createRelation(Arrow.CROWS_FOOT_MANY_MANDATORY));
  }

  /**
   * Creates text for a tooltip that appears if you move the mouse over an
   * edge icon in the palette.
   * @param realizer the realizer of the selected edge template
   * @return a string that shows the text assigned to the edge template
   */
  protected String createEdgeToolTipText(EdgeRealizer realizer){
    return (String) arrowNames.get(realizer.getSourceArrow());
  }

  /**
   * Creates text for a tooltip that appears if you move the mouse over a
   * node icon in the palette.
   * @param realizer the realizer of the selected node template
   * @return a string that shows the text assigned to the node template
   */
  protected String createNodeToolTipText(NodeRealizer realizer){
    return (String) nodeNames.get(realizer);
  }

}
