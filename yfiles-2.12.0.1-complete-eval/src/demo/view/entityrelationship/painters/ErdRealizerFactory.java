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
package demo.view.entityrelationship.painters;

import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PolyLineEdgeRealizer;
import y.view.ShadowNodePainter;
import y.view.SmartNodeLabelModel;
import y.view.YLabel;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/**
 *  This is a factory for elements of Entity Relationship Diagrams (ERD).
 *
 *  <p> It is possible to create realizers for different kinds of ERD elements
 *  (see e.g. {@link #createBigEntity()}, {@link #createSmallEntity(String)},
 *  {@link #createAttribute(String)}, {@link #createRelation(y.view.Arrow, y.view.Arrow)}...).</p>
 */
public class ErdRealizerFactory {
  /**
   * The name of a node configuration which represents an ERD entity with attributes
   * as used in Crow's Foot Notation.
   * @see #createBigEntity()
   */
  public static final String BIG_ENTITY = "com.yworks.entityRelationship.big_entity";

  /**
   * The name of a node configuration which represents an ERD entity as used in
   * Chen Notation.
   * @see #createSmallEntity(String)
   */
  public static final String SMALL_ENTITY = "com.yworks.entityRelationship.small_entity";

  /**
   * The name of a node configuration which represents an ERD attribute as used in
   * Chen Notation.
   * @see #createAttribute(String)
   */
  public static final String ATTRIBUTE = "com.yworks.entityRelationship.attribute";

  /**
   * The name of a node configuration which represents an ERD relationship as used in
   * Chen Notation.
   * @see #createRelationship(String)
   */
  public static final String RELATIONSHIP = "com.yworks.entityRelationship.relationship";

  /**
   * The name of the name label configuration of a big entity.
   * @see #createBigEntity()
   */
  public static final String LABEL_NAME = "com.yworks.entityRelationship.label.name";
  /**
   * The name of the attributes label configuration of a big entity.
   * @see #createBigEntity()
   */
  public static final String LABEL_ATTRIBUTES = "com.yworks.entityRelationship.label.attributes";

  /**
   * The name of a style property used to check if a double border should be drawn.
   * @see #createMultiValuedAttribute(String)
   * @see #createWeakRelationship(String)
   * @see #createWeakSmallEntity(String)
   */
  public static final String DOUBLE_BORDER = "com.yworks.entityRelationship.doubleBorder";

  // The two default colors for the gradient of the nodes
  private static final Color PRIMARY_COLOR = new Color(232, 238, 247, 255);
  private static final Color SECONDARY_COLOR = new Color(183, 201, 227, 255);


  /** Registers the new configurations for ERD elements */
  static {

    //big entity with attributes
    registerNodePainter(BIG_ENTITY, new ErdNodePainter());

    //small entity without or with external attributes
    registerNodePainter(SMALL_ENTITY, new ErdSmallEntityNodePainter());

    //attribute
    registerNodePainter(ATTRIBUTE, new ErdAttributeNodePainter());

    //relation
    registerNodePainter(RELATIONSHIP, new ErdRelationshipNodePainter());

    //label configurations for big entity
    registerLabelConfigurations();

  }

  /**
   * Creates a <code>NodeRealizer</code> that represents an entity with attributes as used in
   * Crow's Foot Notation.
   * This realizer has two labels, one on top of the other.
   * @return an ERD attributed entity node realizer
   * @see #BIG_ENTITY
   */
  public static NodeRealizer createBigEntity(){

    GenericNodeRealizer erdRealizer = new GenericNodeRealizer(BIG_ENTITY);
    erdRealizer.setSize(90,100);
    erdRealizer.setFillColor(PRIMARY_COLOR);
    erdRealizer.setFillColor2(SECONDARY_COLOR);

    NodeLabel nameLabel = erdRealizer.getLabel();
    nameLabel.setConfiguration(LABEL_NAME);
    nameLabel.setBackgroundColor(SECONDARY_COLOR);
    nameLabel.setPosition(NodeLabel.TOP);
    nameLabel.setText("Entity Name");

    NodeLabel attributesLabel = erdRealizer.createNodeLabel();
    erdRealizer.addLabel(attributesLabel);
    attributesLabel.setAlignment(NodeLabel.ALIGN_LEFT);
    ErdAttributesNodeLabelModel model = new ErdAttributesNodeLabelModel();
    attributesLabel.setLabelModel(model, model.getDefaultParameter());
    attributesLabel.setConfiguration(LABEL_ATTRIBUTES);
    attributesLabel.setText("Attribute 1\nAttribute 2\nAttribute 3");
    
    return erdRealizer;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents an entity without attributes as
   * used in Chen Notation.
   * @param label a label that is set to the node
   * @return a simple ERD entity node realizer
   * @see #SMALL_ENTITY
   */
  public static GenericNodeRealizer createSmallEntity(String label) {
    final GenericNodeRealizer smallEntity = new GenericNodeRealizer(SMALL_ENTITY);
    configure(smallEntity);
    smallEntity.setLabelText(label);

    return smallEntity;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a weak entity without attributes as
   * used in Chen Notation.
   * A weak entity is surrounded by two lines.
   * @param label a label that is set to the node
   * @return a weak ERD entity node realizer
   * @see #SMALL_ENTITY
   */
  public static GenericNodeRealizer createWeakSmallEntity(String label){

    final GenericNodeRealizer smallEntity = new GenericNodeRealizer(SMALL_ENTITY);
    configure(smallEntity);
    smallEntity.setStyleProperty(DOUBLE_BORDER, Boolean.TRUE);
    smallEntity.setLabelText(label);

    return smallEntity;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents an attribute as used in Chen Notation.
   * @param label a label that is set to the node
   * @return an ERD attribute node realizer
   * @see #ATTRIBUTE
   */
  public static GenericNodeRealizer createAttribute(String label){

    final GenericNodeRealizer attribute = new GenericNodeRealizer(ATTRIBUTE);
    configure(attribute);
    attribute.setLabelText(label);

    return attribute;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a multi-valued attribute as used
   * in Chen Notation.
   * A multi-valued attribute is surrounded by two lines.
   * @param label a label that is set to the node
   * @return a multi-valued ERD attribute node realizer
   * @see #ATTRIBUTE
   */
  public static GenericNodeRealizer createMultiValuedAttribute(String label){
    
    final GenericNodeRealizer attribute = new GenericNodeRealizer(ATTRIBUTE);
    configure(attribute);
    attribute.setStyleProperty(DOUBLE_BORDER, Boolean.TRUE);
    attribute.setLabelText(label);

    return attribute;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a primary key attribute as used in
   * Chen Notation.
   * The label of a primary key attribute is underlined.
   * @param label a label that is set to the node
   * @return a primary key ERD attribute node realizer
   * @see #ATTRIBUTE
   */
  public static GenericNodeRealizer createPrimaryKeyAttribute(String label){

    final GenericNodeRealizer attribute = new GenericNodeRealizer(ATTRIBUTE);
    configure(attribute);
    attribute.getLabel().setFontStyle(Font.BOLD);
    attribute.getLabel().setUnderlinedTextEnabled(true);
    attribute.setLabelText(label);

    return attribute;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a derived attribute as used in
   * Chen Notation.
   * A derived attribute is surrounded by a dashed line.
   * @param label a label that is set to the node
   * @return a derived ERD attribute node realizer
   * @see #ATTRIBUTE
   */
  public static GenericNodeRealizer createDerivedAttribute(String label){

    final GenericNodeRealizer attribute = new GenericNodeRealizer(ATTRIBUTE);
    configure(attribute);
    attribute.setLineType(LineType.DASHED_1);
    attribute.setLabelText(label);

    return attribute;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a relationship as used in Chen
   * Notation.
   * @param label a label that is set to the node
   * @return an ERD relationship node realizer
   * @see #RELATIONSHIP
   */
  public static GenericNodeRealizer createRelationship(String label){

    final GenericNodeRealizer relation = new GenericNodeRealizer(RELATIONSHIP);
    configure(relation);
    relation.setLabelText(label);

    return relation;
  }

  /**
   * Creates a <code>NodeRealizer</code> that represents a weak relationship as used in
   * Chen Notation.
   * A weak relationship is surrounded by two lines.
   * @param label a label that is set to the node
   * @return an weak ERD relationship node realizer
   * @see #RELATIONSHIP
   */
  public static GenericNodeRealizer createWeakRelationship(String label){

    final GenericNodeRealizer relation = new GenericNodeRealizer(RELATIONSHIP);
    configure(relation);
    relation.setStyleProperty(DOUBLE_BORDER, Boolean.TRUE);
    relation.setLabelText(label);

    return relation;
  }

  /**
   * Tests if a given realizer has the "big entity" configuration.
   * @param realizer <code>NodeRealizer</code> to be tested
   * @return <code>true</code>, if realizer represents a big entity, <code>false</code> otherwise.
   * @see #createBigEntity()
   */
  public static boolean isBigEntityRealizer(NodeRealizer realizer){

    if(realizer instanceof GenericNodeRealizer){
      if(BIG_ENTITY.equals(((GenericNodeRealizer) realizer).getConfiguration())){
        return true;
      }
    }

    return false;
  }

  /**
   * Tests if a given realizer has the "small entity" configuration.
   * @param realizer NodeRealizer to be tested
   * @return <code>true</code>, if realizer represents a small entity, <code>false</code> otherwise.
   * @see #createSmallEntity(String)
   * @see #createWeakSmallEntity(String)
   */
  public static boolean isSmallEntityRealizer(NodeRealizer realizer){

    if(realizer instanceof GenericNodeRealizer){
      if(SMALL_ENTITY.equals(((GenericNodeRealizer) realizer).getConfiguration())){
        return true;
      }
    }

    return false;

  }

  /**
   * Tests if a given realizer has the "attribute" configuration.
   * @param realizer NodeRealizer to be tested
   * @return <code>true</code>, if realizer represents an attribute, <code>false</code> otherwise.
   * @see #createAttribute(String)
   * @see #createMultiValuedAttribute(String)
   * @see #createDerivedAttribute(String)
   * @see #createPrimaryKeyAttribute(String)
   */
  public static boolean isAttributeRealizer(NodeRealizer realizer){
    if(realizer instanceof GenericNodeRealizer){
      if(ATTRIBUTE.equals(((GenericNodeRealizer) realizer).getConfiguration())){
        return true;
      }
    }

    return false;

  }

  /**
   * Tests if a given realizer has the "relationship" configuration.
   * @param realizer node realizer to be tested
   * @return <code>true</code>, if realizer represents a relationship, <code>false</code> otherwise.
   * @see #createRelationship(String)
   * @see #createWeakRelationship(String)
   */
  public static boolean isRelationshipRealizer(NodeRealizer realizer){
    if(realizer instanceof GenericNodeRealizer){
      if(RELATIONSHIP.equals(((GenericNodeRealizer) realizer).getConfiguration())){
        return true;
      }
    }

    return false;

  }

  /**
   * Sets default colors and size for the realizer.
   * @param realizer realizer that will be configured
   */
  private static void configure(GenericNodeRealizer realizer) {
    realizer.setFillColor(PRIMARY_COLOR);
    realizer.setFillColor2(SECONDARY_COLOR);
    realizer.setSize(80, 40);
    NodeLabel label = realizer.getLabel();
    SmartNodeLabelModel model = new SmartNodeLabelModel();
    label.setLabelModel(model, model.getDefaultParameter());
  }

  /**
   * Creates an <code>EdgeRealizer</code> with a specified arrow at the source.
   * @param arrow the desired <code>Arrow</code>
   * @return a edge realizer with a specified arrow
   * @see Arrow
   */
  public static EdgeRealizer createRelation(Arrow arrow){

    final PolyLineEdgeRealizer pler = new PolyLineEdgeRealizer();
    pler.setSourceArrow(arrow);

    return pler;
  }

  /**
   * Creates an <code>EdgeRealizer</code> with a specified arrow at the source and the target.
   * @param sourceArrow the desired Arrow for the source
   * @param targetArrow the desired Arrow for the target
   * @return a edge realizer with a specified arrows at source and target
   */
  public static EdgeRealizer createRelation(Arrow sourceArrow, Arrow targetArrow){

    final PolyLineEdgeRealizer pler = new PolyLineEdgeRealizer();
    pler.setSourceArrow(sourceArrow);
    pler.setTargetArrow(targetArrow);

    return pler;
  }

  /**
   * Registers a node painter configuration.
   * @param configName name of the configuration
   * @param impl node painter configuration to be registered
   */
  private static void registerNodePainter(String configName, GenericNodeRealizer.Painter impl) {

    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericNodeRealizer.Painter.class, new ShadowNodePainter(impl));
    implementationsMap.put(GenericNodeRealizer.ContainsTest.class, impl);
    factory.addConfiguration(configName, implementationsMap);

  }

  /**
   * Registers node label configurations used by the big entity node realizer.
   * @see #createBigEntity() 
   */
  private static void registerLabelConfigurations() {

    final YLabel.Factory lf = NodeLabel.getFactory();
    final Map lnc = lf.createDefaultConfigurationMap();
    lnc.put(YLabel.Painter.class, new ErdNameLabelPainter());
    lf.addConfiguration(LABEL_NAME, lnc);

    final Map lac = lf.createDefaultConfigurationMap();
    final ErdAttributesLabelConfiguration eac = new ErdAttributesLabelConfiguration();
    lac.put(YLabel.Layout.class, eac);
    lac.put(YLabel.Painter.class, eac);
    lf.addConfiguration(LABEL_ATTRIBUTES, lac);

  }

}
