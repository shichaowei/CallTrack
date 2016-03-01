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
package demo.layout.module;

import y.module.LayoutModule;
import y.module.YModule;

import y.layout.radial.RadialLayouter;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.radial.RadialLayouter}.
 * 
 */
public class RadialLayoutModule extends LayoutModule {
  private static final int MAXIMUM_SMOOTHNESS = 10;
  private static final int MINIMUM_SMOOTHNESS = 1;
  private static final int SMOOTHNESS_ANGLE_FACTOR = 4;

  //// Module 'Radial Layout'
  protected static final String MODULE_RADIAL = "RADIAL";
  
  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_CENTER_STRATEGY = "CENTER_STRATEGY";
  protected static final String VALUE_CENTER_DIRECTED = "CENTER_DIRECTED";
  protected static final String VALUE_CENTER_CENTRAL = "CENTER_CENTRAL";
  protected static final String VALUE_CENTER_WEIGHTED_CENTRAL = "CENTER_WEIGHTED_CENTRAL";
  protected static final String VALUE_CENTER_SELECTED = "CENTER_SELECTED";
  protected static final String ITEM_LAYERING_STRATEGY = "LAYERING_STRATEGY";
  protected static final String VALUE_LAYERING_BFS = "LAYERING_BFS";
  protected static final String VALUE_LAYERING_HIERARCHICAL = "LAYERING_HIERARCHICAL";
  protected static final String ITEM_MINIMAL_LAYER_DISTANCE = "MINIMAL_LAYER_DISTANCE";
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";
  protected static final String ITEM_MAXIMAL_CHILD_SECTOR_SIZE = "MAXIMAL_CHILD_SECTOR_SIZE";
  protected static final String ITEM_EDGE_ROUTING_STRATEGY = "EDGE_ROUTING_STRATEGY";
  protected static final String VALUE_EDGE_POLYLINE = "EDGE_POLYLINE";
  protected static final String VALUE_EDGE_ARC = "EDGE_ARC";
  protected static final String ITEM_EDGE_SMOOTHNESS = "EDGE_SMOOTHNESS";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";

  /**
   * Creates an instance of this module.
   */
  public RadialLayoutModule() {
    super(MODULE_RADIAL);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Defaults provider
    final RadialLayouter defaults = new RadialLayouter();

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate section
    options.addEnum(ITEM_CENTER_STRATEGY, new String[]{
        VALUE_CENTER_DIRECTED,
        VALUE_CENTER_CENTRAL,
        VALUE_CENTER_WEIGHTED_CENTRAL,
        VALUE_CENTER_SELECTED
    }, defaults.getCenterNodesPolicy());
    options.addEnum(ITEM_LAYERING_STRATEGY, new String[]{
        VALUE_LAYERING_BFS,
        VALUE_LAYERING_HIERARCHICAL
    }, defaults.getLayeringStrategy() == RadialLayouter.LAYERING_STRATEGY_BFS ? 0 : 1);
    options.addInt(ITEM_MINIMAL_LAYER_DISTANCE, (int) defaults.getMinimalLayerDistance(), 1, 1000);
    options.addInt(ITEM_MINIMAL_NODE_DISTANCE, (int) defaults.getMinimalNodeToNodeDistance(), 0, 300);
    options.addInt(ITEM_MAXIMAL_CHILD_SECTOR_SIZE, (int) (defaults.getMaximalChildSectorAngle()), 15, 360);
    final OptionItem itemEdgeRoutingStrategy = options.addEnum(ITEM_EDGE_ROUTING_STRATEGY, new String[]{
        VALUE_EDGE_POLYLINE,
        VALUE_EDGE_ARC
    }, defaults.getEdgeRoutingStrategy() == RadialLayouter.EDGE_ROUTING_STRATEGY_POLYLINE ? 0 : 1);
    final int smoothness = (int) Math.min(MAXIMUM_SMOOTHNESS,
        (1 + MAXIMUM_SMOOTHNESS * SMOOTHNESS_ANGLE_FACTOR - defaults.getMinimalBendAngle()) / SMOOTHNESS_ANGLE_FACTOR);
    final OptionItem smoothnessItem =
        options.addInt(ITEM_EDGE_SMOOTHNESS, smoothness, MINIMUM_SMOOTHNESS, MAXIMUM_SMOOTHNESS);
    options.addBool(ITEM_CONSIDER_NODE_LABELS, defaults.isConsiderNodeLabelsEnabled());
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnCondition(
        optionConstraints.createConditionValueEquals(itemEdgeRoutingStrategy, VALUE_EDGE_ARC), smoothnessItem);

    return options;
  }


  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final RadialLayouter radial = new RadialLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(radial, options);

    launchLayouter(radial);
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param radial the <code>RadialLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final RadialLayouter radial, final OptionHandler options) {
    radial.setMinimalNodeToNodeDistance(options.getInt(ITEM_MINIMAL_NODE_DISTANCE));

    if (options.getString(ITEM_EDGE_ROUTING_STRATEGY).equals(VALUE_EDGE_POLYLINE)) {
      radial.setEdgeRoutingStrategy(RadialLayouter.EDGE_ROUTING_STRATEGY_POLYLINE);
    } else if (options.getString(ITEM_EDGE_ROUTING_STRATEGY).equals(VALUE_EDGE_ARC)) {
      radial.setEdgeRoutingStrategy(RadialLayouter.EDGE_ROUTING_STRATEGY_ARC);
    }

    final double minimalBendAngle = 1 + (MAXIMUM_SMOOTHNESS - options.getInt(ITEM_EDGE_SMOOTHNESS)) * SMOOTHNESS_ANGLE_FACTOR;
    radial.setMinimalBendAngle(minimalBendAngle);
    
    radial.setMinimalLayerDistance(options.getInt(ITEM_MINIMAL_LAYER_DISTANCE));
    radial.setMaximalChildSectorAngle(options.getInt(ITEM_MAXIMAL_CHILD_SECTOR_SIZE));

    if (options.getString(ITEM_CENTER_STRATEGY).equals(VALUE_CENTER_CENTRAL)) {
      radial.setCenterNodesPolicy(RadialLayouter.CENTER_NODES_POLICY_CENTRALITY);
    } else if (options.getString(ITEM_CENTER_STRATEGY).equals(VALUE_CENTER_WEIGHTED_CENTRAL)) {
      radial.setCenterNodesPolicy(RadialLayouter.CENTER_NODES_POLICY_WEIGHTED_CENTRALITY);
    } else if (options.getString(ITEM_CENTER_STRATEGY).equals(VALUE_CENTER_SELECTED)) {
      radial.setCenterNodesPolicy(RadialLayouter.CENTER_NODES_POLICY_SELECTED_NODES);
    } else {
      radial.setCenterNodesPolicy(RadialLayouter.CENTER_NODES_POLICY_DIRECTED);
    }

    if (options.getString(ITEM_LAYERING_STRATEGY).equals(VALUE_LAYERING_HIERARCHICAL)) {
      radial.setLayeringStrategy(RadialLayouter.LAYERING_STRATEGY_HIERARCHICAL);
    } else {
      radial.setLayeringStrategy(RadialLayouter.LAYERING_STRATEGY_BFS);
    }

    radial.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS));
  }
}