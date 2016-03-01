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

import y.base.Node;
import y.layout.ComponentLayouter;
import y.layout.LayoutOrientation;
import y.layout.genealogy.FamilyTreeLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.EdgeLayoutDescriptor;
import y.layout.hierarchic.incremental.NodeLayoutDescriptor;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.hierarchic.incremental.SimplexNodePlacer;
import y.option.ConstraintManager;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.util.DataProviderAdapter;
import y.view.Graph2D;
import y.view.NodeRealizer;

import java.awt.Color;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.genealogy.FamilyTreeLayouter}.
 *
 */
public class FamilyTreeLayoutModule extends LayoutModule {

  //// Module 'Family Tree Layouter'
  protected static final String MODULE_FAMILY_TREE_LAYOUTER = "FAMILY_TREE_LAYOUTER";
  
  // The colors used by the data provider to distinguish family nodes from individual nodes
  //// Section 'Distinguish Node Types'
  protected static final String SECTION_FAMILY_PROPERTIES = "FAMILY_PROPERTIES";
  //-
  protected static final String ITEM_FAMILY_COLOR = "FAMILY_COLOR";
  protected static final String ITEM_MALE_COLOR = "MALE_COLOR";
  protected static final String ITEM_FEMALE_COLOR = "FEMALE_COLOR";

  // Basic layout properties
  //// Section 'Layout'
  protected static final String SECTION_LAYOUT = "LAYOUT";
  // Section 'Layout' items
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String ITEM_SINGLE_DIRECT_BELOW = "SINGLE_DIRECT_BELOW";
  protected static final String ITEM_FAMILIES_ALWAYS_BELOW = "FAMILIES_ALWAYS_BELOW";
  protected static final String ITEM_NODE_ALIGNMENT = "NODE_ALIGNMENT";
  protected static final String VALUE_NODE_ALIGN_TOP = "NODE_ALIGN_TOP";
  protected static final String VALUE_NODE_ALIGN_CENTER = "NODE_ALIGN_CENTER";
  protected static final String VALUE_NODE_ALIGN_BOTTOM = "NODE_ALIGN_BOTTOM";
  protected static final String ITEM_SORT_BY_SEX = "SORT_BY_SEX";
  protected static final String VALUE_DO_NOT_SORT = "DO_NOT_SORT";
  protected static final String VALUE_FEMALE_LEFT = "FEMALE_LEFT";
  protected static final String VALUE_FEMALE_ALWAYS_LEFT = "FEMALE_ALWAYS_LEFT";
  protected static final String VALUE_MALE_LEFT = "MALE_LEFT";
  protected static final String VALUE_MALE_ALWAYS_LEFT = "MALE_ALWAYS_LEFT";
  
  //// Section 'Minimum Distances'
  protected static final String SECTION_DISTANCES = "DISTANCES";
  // Section 'Minimum Distances' items
  protected static final String TITLE_SIBLING_DISTANCES = "SIBLING_DISTANCES";
  protected static final String ITEM_HORIZONTAL_SPACING = "HORIZONTAL_SPACING";
  protected static final String ITEM_NODE_TO_NODE_DISTANCE = "NODE_TO_NODE_DISTANCE";
  protected static final String TITLE_GENERATION_DISTANCES = "GENERATION_DISTANCES";
  protected static final String ITEM_VERTICAL_SPACING = "VERTICAL_SPACING";
  protected static final String ITEM_MINIMUM_LAYER_DISTANCE = "MINIMUM_LAYER_DISTANCE";
  protected static final String ITEM_MINIMUM_FIRST_SEGMENT = "MINIMUM_FIRST_SEGMENT";
  protected static final String ITEM_MINIMUM_LAST_SEGMENT = "MINIMUM_LAST_SEGMENT";
  
  // Advanced layout properties
  //// Section 'Advanced Layout'
  protected static final String SECTION_ADVANCED_LAYOUT = "ADVANCED_LAYOUT";
  // Section 'Advanced Layout' items
  protected static final String ITEM_USE_COMPONENT_LAYOUTER = "USE_COMPONENT_LAYOUTER";
  protected static final String TITLE_COMPONENT_LAYOUTER = "COMPONENT_LAYOUTER";
  protected static final String ITEM_COMPONENT_DISTANCE = "COMPONENT_DISTANCE";
  protected static final String ITEM_COMPONENT_STYLE = "COMPONENT_STYLE";
  protected static final String VALUE_COMPONENT_STYLE_NONE = "COMPONENT_STYLE_NONE";
  protected static final String VALUE_COMPONENT_STYLE_ROWS = "COMPONENT_STYLE_ROWS";
  protected static final String VALUE_COMPONENT_STYLE_SINGLE_ROW = "COMPONENT_STYLE_SINGLE_ROW";
  protected static final String VALUE_COMPONENT_STYLE_SINGLE_COLUMN = "COMPONENT_STYLE_SINGLE_COLUMN";
  protected static final String VALUE_COMPONENT_STYLE_PACKED_RECTANGLE = "COMPONENT_STYLE_PACKED_RECTANGLE";
  protected static final String VALUE_COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE = "COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE";
  protected static final String VALUE_COMPONENT_STYLE_PACKED_CIRCLE = "COMPONENT_STYLE_PACKED_CIRCLE";
  protected static final String VALUE_COMPONENT_STYLE_PACKED_COMPACT_CIRCLE = "COMPONENT_STYLE_PACKED_COMPACT_CIRCLE";
  
  // data provider delete flag
  private boolean isFamilyDPAddedByModule = false;

  /**
   * Creates an instance of this module.
   */
  public FamilyTreeLayoutModule() {
    super(MODULE_FAMILY_TREE_LAYOUTER);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);

    //// Section 'Distinguish Node Types'
    options.useSection(SECTION_FAMILY_PROPERTIES);
    // Populate section
    options.addColor(ITEM_FAMILY_COLOR, Color.black, true);
    options.addColor(ITEM_MALE_COLOR, new Color(0xFFCC99), true);
    options.addColor(ITEM_FEMALE_COLOR, new Color(0xCCCCFF), true);

    //// Section 'Layout'
    options.useSection(SECTION_LAYOUT);
    // Populate section
    options.addEnum(ITEM_ORIENTATION, new String[]{
        VALUE_TOP_TO_BOTTOM,
        VALUE_LEFT_TO_RIGHT,
        VALUE_BOTTOM_TO_TOP,
        VALUE_RIGHT_TO_LEFT
    }, 0);
    options.addBool(ITEM_SINGLE_DIRECT_BELOW, true);
    options.addBool(ITEM_FAMILIES_ALWAYS_BELOW, false);
    options.addEnum(ITEM_NODE_ALIGNMENT, new String[]{
        VALUE_NODE_ALIGN_TOP,
        VALUE_NODE_ALIGN_CENTER,
        VALUE_NODE_ALIGN_BOTTOM
    }, 0);
    options.addEnum(ITEM_SORT_BY_SEX, new String[]{
        VALUE_DO_NOT_SORT,
        VALUE_FEMALE_LEFT,
        VALUE_FEMALE_ALWAYS_LEFT,
        VALUE_MALE_LEFT,
        VALUE_MALE_ALWAYS_LEFT
    }, 0);
    
    //// Section 'Minimum distances'
    options.useSection(SECTION_DISTANCES);
    // Group 'In The Same Generation'
    final OptionGroup siblingsGroup = new OptionGroup();
    siblingsGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_SIBLING_DISTANCES);
    // Populate group
    siblingsGroup.addItem(options.addDouble(ITEM_HORIZONTAL_SPACING, 40, 0, 400));
    siblingsGroup.addItem(options.addDouble(ITEM_NODE_TO_NODE_DISTANCE, 40, 0, 400));
    // Group 'Between Generations'
    final OptionGroup generationsGroup = new OptionGroup();
    generationsGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_GENERATION_DISTANCES);
    // Populate group
    generationsGroup.addItem(options.addDouble(ITEM_VERTICAL_SPACING, 10, 0, 100));
    generationsGroup.addItem(options.addDouble(ITEM_MINIMUM_LAYER_DISTANCE, 40, 0, 400));
    generationsGroup.addItem(options.addDouble(ITEM_MINIMUM_FIRST_SEGMENT, 40, 0, 400));
    generationsGroup.addItem(options.addDouble(ITEM_MINIMUM_LAST_SEGMENT, 20, 0, 400));

    //// Section 'Advanced Layout'
    options.useSection(SECTION_ADVANCED_LAYOUT);
    // Populate section
    final OptionItem itemUseComponentLayouter = options.addBool(ITEM_USE_COMPONENT_LAYOUTER, true);
    // Group 'Components'
    final OptionGroup componentsGroup = new OptionGroup();
    componentsGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_COMPONENT_LAYOUTER);
    // Populate group
    final OptionItem itemComponentDistance = componentsGroup.addItem(options.addInt(ITEM_COMPONENT_DISTANCE, 40));
    componentsGroup.addItem(
        options.addEnum(ITEM_COMPONENT_STYLE, new String[]{
            VALUE_COMPONENT_STYLE_NONE,
            VALUE_COMPONENT_STYLE_ROWS,
            VALUE_COMPONENT_STYLE_SINGLE_ROW,
            VALUE_COMPONENT_STYLE_SINGLE_COLUMN,
            VALUE_COMPONENT_STYLE_PACKED_RECTANGLE,
            VALUE_COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE,
            VALUE_COMPONENT_STYLE_PACKED_CIRCLE,
            VALUE_COMPONENT_STYLE_PACKED_COMPACT_CIRCLE,
        }, 1));
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseComponentLayouter, Boolean.TRUE, itemComponentDistance);

    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final FamilyTreeLayouter familyTree = new FamilyTreeLayouter();

    final OptionHandler options = getOptionHandler();
    configure(familyTree, options);
    
    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try {
      launchLayouter(familyTree);
    } finally {
      restoreGraph(graph, options);
    }
  }

  /**
   * Prepares a <code>graph</code> depending on the given options for the
   * module's layout algorithm.
   * <br>
   * Additional resources created by this method have to be freed up by calling
   * {@link #restoreGraph(y.view.Graph2D, y.option.OptionHandler)} after
   * layout calculation.  
   * @param graph the graph to be prepared
   * @param options the options for the module's layout algorithm
   */
  protected void prepareGraph(final Graph2D graph, final OptionHandler options) {
    // only add a family type data provider if the graph does not already have
    // such a data provider (e.g. FamilyTreeDemo adds this data provider outside
    // the module)
    isFamilyDPAddedByModule = graph.getDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE) == null;
    if (isFamilyDPAddedByModule) {
      /* Create the family info if not already existing */
      graph.addDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE, new DataProviderAdapter() {
        public Object get(Object o) {
          final NodeRealizer nr = graph.getRealizer((Node) o);
          final Color nodeColor = nr.getFillColor();
          if (nodeColor != null && nodeColor.equals(options.get(ITEM_MALE_COLOR))) {
            return FamilyTreeLayouter.TYPE_MALE;
          }
          if (nodeColor != null && nodeColor.equals(options.get(ITEM_FEMALE_COLOR))) {
            return FamilyTreeLayouter.TYPE_FEMALE;
          }
          if (nodeColor != null && nodeColor.equals(options.get(ITEM_FAMILY_COLOR))) {
            return FamilyTreeLayouter.TYPE_FAMILY;
          }
          return null;
        }
      });
    }
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // remove the data provider if it was added by this module
    if (isFamilyDPAddedByModule) {
      isFamilyDPAddedByModule = false;
      graph.removeDataProvider(FamilyTreeLayouter.DP_KEY_FAMILY_TYPE);
    }
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param familyTree the <code>FamilyTreeLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final FamilyTreeLayouter familyTree, final OptionHandler options) {
    familyTree.setSpacingBetweenFamilyMembers(options.getDouble(ITEM_HORIZONTAL_SPACING));
    familyTree.setOffsetForFamilyNodes(options.getDouble(ITEM_VERTICAL_SPACING));
    familyTree.setFamilyNodesAlwaysBelow(options.getBool(ITEM_FAMILIES_ALWAYS_BELOW));
    familyTree.setPartnerlessBelow(options.getBool(ITEM_SINGLE_DIRECT_BELOW));

    /* Sets the orientation */
//    ((OrientationLayouter) layouter.getOrientationLayouter()).setMirrorMask(
//        OrientationLayouter.MIRROR_BOTTOM_TO_TOP | OrientationLayouter.MIRROR_LEFT_TO_RIGHT);
    if (options.get(ITEM_ORIENTATION).equals(VALUE_TOP_TO_BOTTOM)) {
      familyTree.setLayoutOrientation(LayoutOrientation.TOP_TO_BOTTOM);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_LEFT_TO_RIGHT)) {
      familyTree.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_BOTTOM_TO_TOP)) {
      familyTree.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
    } else if (options.get(ITEM_ORIENTATION).equals(VALUE_RIGHT_TO_LEFT)) {
      familyTree.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    }

    /* Advanced */

    /* Component Layouter */
    familyTree.setComponentLayouterEnabled(options.getBool(ITEM_USE_COMPONENT_LAYOUTER));
    final ComponentLayouter cl = (ComponentLayouter) familyTree.getComponentLayouter();
    cl.setComponentSpacing(options.getInt(ITEM_COMPONENT_DISTANCE));
    if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_NONE)) {
      cl.setStyle(ComponentLayouter.STYLE_NONE);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_PACKED_CIRCLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_CIRCLE);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_PACKED_COMPACT_CIRCLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_CIRCLE);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_PACKED_COMPACT_RECTANGLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_COMPACT_RECTANGLE);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_PACKED_RECTANGLE)) {
      cl.setStyle(ComponentLayouter.STYLE_PACKED_RECTANGLE);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_ROWS)) {
      cl.setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_SINGLE_COLUMN)) {
      cl.setStyle(ComponentLayouter.STYLE_SINGLE_COLUMN);
    } else if (options.get(ITEM_COMPONENT_STYLE).equals(VALUE_COMPONENT_STYLE_SINGLE_ROW)) {
      cl.setStyle(ComponentLayouter.STYLE_SINGLE_ROW);
    }

    final IncrementalHierarchicLayouter ihl = new IncrementalHierarchicLayouter();
    familyTree.setTopLayouter(ihl);

    //disable bend reduction to get centered alignment of children
    ((SimplexNodePlacer) ihl.getNodePlacer()).setBendReductionEnabled(false);

    /* Vertical node alignment */
    final NodeLayoutDescriptor nld = ihl.getNodeLayoutDescriptor();
    if (options.get(ITEM_NODE_ALIGNMENT).equals(VALUE_NODE_ALIGN_TOP)) {
      nld.setLayerAlignment(0.0);
      familyTree.setAlignment(FamilyTreeLayouter.ALIGN_TOP);
    } else if (options.get(ITEM_NODE_ALIGNMENT).equals(VALUE_NODE_ALIGN_CENTER)) {
      nld.setLayerAlignment(0.5);
      familyTree.setAlignment(FamilyTreeLayouter.ALIGN_CENTER);
    } else if (options.get(ITEM_NODE_ALIGNMENT).equals(VALUE_NODE_ALIGN_BOTTOM)) {
      nld.setLayerAlignment(1.0);
      familyTree.setAlignment(FamilyTreeLayouter.ALIGN_BOTTOM);
    }

    ihl.setMinimumLayerDistance(options.getDouble(ITEM_MINIMUM_LAYER_DISTANCE));
    ihl.setNodeToNodeDistance(options.getDouble(ITEM_NODE_TO_NODE_DISTANCE));
    final EdgeLayoutDescriptor eld = ihl.getEdgeLayoutDescriptor();
    eld.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT));
    eld.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT));
    eld.setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));

    if (options.get(ITEM_SORT_BY_SEX).equals(VALUE_DO_NOT_SORT)) {
      familyTree.setSortFamilyMembers(FamilyTreeLayouter.DO_NOT_SORT_BY_SEX);
    } else if (options.get(ITEM_SORT_BY_SEX).equals(VALUE_FEMALE_LEFT)) {
      familyTree.setSortFamilyMembers(FamilyTreeLayouter.FEMALE_FIRST);
    } else if (options.get(ITEM_SORT_BY_SEX).equals(VALUE_FEMALE_ALWAYS_LEFT)) {
      familyTree.setSortFamilyMembers(FamilyTreeLayouter.FEMALE_ALWAYS_FIRST);
    } else if (options.get(ITEM_SORT_BY_SEX).equals(VALUE_MALE_LEFT)) {
      familyTree.setSortFamilyMembers(FamilyTreeLayouter.MALE_FIRST);
    } else if (options.get(ITEM_SORT_BY_SEX).equals(VALUE_MALE_ALWAYS_LEFT)) {
      familyTree.setSortFamilyMembers(FamilyTreeLayouter.MALE_ALWAYS_FIRST);
    }
  }
}
