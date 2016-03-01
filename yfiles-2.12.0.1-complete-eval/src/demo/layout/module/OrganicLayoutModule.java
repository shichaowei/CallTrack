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

import y.layout.ComponentLayouter;
import y.layout.organic.OrganicLayouter;
import y.option.OptionHandler;
import y.view.Graph2D;
import y.view.Selections;
import y.view.hierarchy.HierarchyManager;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.organic.OrganicLayouter}.
 *
 */
public class OrganicLayoutModule extends LayoutModule {
  //// Module 'Smart Organic Module'
  protected static final String MODULE_ORGANIC = "ORGANIC";
  
  //// Section 'Visual'
  protected static final String SECTION_VISUAL = "VISUAL";
  // Section 'Visual' items
  protected static final String ITEM_SPHERE_OF_ACTION = "SPHERE_OF_ACTION";
  protected static final String VALUE_ALL = "ALL";
  protected static final String VALUE_MAINLY_SELECTION = "MAINLY_SELECTION";
  protected static final String VALUE_ONLY_SELECTION = "ONLY_SELECTION";
  protected static final String ITEM_INITIAL_PLACEMENT = "INITIAL_PLACEMENT";
  protected static final String VALUE_RANDOM = "RANDOM";
  protected static final String VALUE_AT_ORIGIN = "AT_ORIGIN";
  protected static final String VALUE_AS_IS = "AS_IS";
  protected static final String ITEM_PREFERRED_EDGE_LENGTH = "PREFERRED_EDGE_LENGTH";
  protected static final String ITEM_OBEY_NODE_SIZES = "OBEY_NODE_SIZES";
  protected static final String ITEM_ATTRACTION = "ATTRACTION";
  protected static final String ITEM_REPULSION = "REPULSION";
  protected static final String ITEM_GRAVITY_FACTOR = "GRAVITY_FACTOR";
  protected static final String ITEM_ACTIVATE_TREE_BEAUTIFIER = "ACTIVATE_TREE_BEAUTIFIER";
  
  //// Section 'Algorithm'
  protected static final String SECTION_ALGORITHM = "ALGORITHM";
  // Section 'Algorithm' items
  protected static final String ITEM_ITERATION_FACTOR = "ITERATION_FACTOR";
  protected static final String ITEM_MAXIMAL_DURATION = "MAXIMAL_DURATION";
  protected static final String ITEM_ACTIVATE_DETERMINISTIC_MODE = "ACTIVATE_DETERMINISTIC_MODE";
  protected static final String ITEM_ALLOW_MULTI_THREADING = "ALLOW_MULTI_THREADING";
  
  //// Section 'Grouping'
  protected static final String SECTION_GROUPING = "GROUPING";
  // Section 'Grouping' items
  protected static final String ITEM_GROUP_LAYOUT_POLICY = "GROUP_LAYOUT_POLICY";
  protected static final String VALUE_LAYOUT_GROUPS = "LAYOUT_GROUPS";
  protected static final String VALUE_FIX_GROUPS = "FIX_GROUPS";
  protected static final String VALUE_IGNORE_GROUPS = "IGNORE_GROUPS";
  protected static final String ITEM_GROUP_NODE_COMPACTNESS = "GROUP_NODE_COMPACTNESS";

  /**
   * Creates an instance of this module.
   */
  public OrganicLayoutModule() {
    super(MODULE_ORGANIC, "yFiles Layout Team", "Wrapper for OrganicLayouter");
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    // Defaults provider
    final OrganicLayouter defaults = new OrganicLayouter();

    //// Section 'Visual'
    options.useSection(SECTION_VISUAL);
    // Populate section
    options.addEnum(ITEM_SPHERE_OF_ACTION, new String[]{
        VALUE_ALL,
        VALUE_MAINLY_SELECTION,
        VALUE_ONLY_SELECTION
    }, defaults.getSphereOfAction());
    options.addEnum(ITEM_INITIAL_PLACEMENT, new String[]{
        VALUE_RANDOM,
        VALUE_AT_ORIGIN,
        VALUE_AS_IS
    }, defaults.getInitialPlacement());
    options.addInt(ITEM_PREFERRED_EDGE_LENGTH, defaults.getPreferredEdgeLength(), 0, 500);
    options.addBool(ITEM_OBEY_NODE_SIZES, defaults.getObeyNodeSize());
    options.addInt(ITEM_ATTRACTION, defaults.getAttraction(), 0, 2);
    options.addInt(ITEM_REPULSION, defaults.getRepulsion(), 0, 2);
    options.addDouble(ITEM_GRAVITY_FACTOR, defaults.getGravityFactor(), -0.2, 2, 1);
    options.addBool(ITEM_ACTIVATE_TREE_BEAUTIFIER, defaults.getActivateTreeBeautifier());
    
    //// Section 'Algorithm'
    options.useSection(SECTION_ALGORITHM);
    // Populate section
    options.addDouble(ITEM_ITERATION_FACTOR, defaults.getIterationFactor());
    options.addInt(ITEM_MAXIMAL_DURATION, (int) (defaults.getMaximumDuration() / 1000));
    options.addBool(ITEM_ACTIVATE_DETERMINISTIC_MODE, defaults.getActivateDeterministicMode());
    options.addBool(ITEM_ALLOW_MULTI_THREADING, true);
    
    //// Section 'Grouping'
    options.useSection(SECTION_GROUPING);
    // Populate section
    options.addEnum(ITEM_GROUP_LAYOUT_POLICY, new String[]{
        VALUE_LAYOUT_GROUPS,
        VALUE_FIX_GROUPS,
        VALUE_IGNORE_GROUPS
    }, 0);
    options.addDouble(ITEM_GROUP_NODE_COMPACTNESS, defaults.getGroupNodeCompactness(), 0, 1);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final OrganicLayouter organic = new OrganicLayouter();

    final OptionHandler options = getOptionHandler();
    configure(organic, options);

    final Graph2D graph = getGraph2D();
    prepareGraph(graph, options);
    try{
      launchLayouter(organic);
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
    // backup existing data providers to prevent loss of user settings
    backupDataProvider(graph, OrganicLayouter.SPHERE_OF_ACTION_NODES);
    graph.addDataProvider(OrganicLayouter.SPHERE_OF_ACTION_NODES, Selections.createSelectionNodeMap(graph));
  }

  /**
   * Restores the given <code>graph</code> by freeing up resources created by
   * {@link #prepareGraph(y.view.Graph2D, y.option.OptionHandler)}.
   * @param graph the graph for which <code>prepareGraph</code> has been called
   * @param options the options for the module's layout algorithm
   */
  protected void restoreGraph(final Graph2D graph, final OptionHandler options) {
    // remove the data providers set by this module by restoring the initial state
    restoreDataProvider(graph, OrganicLayouter.SPHERE_OF_ACTION_NODES);
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2D</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param organic the <code>OrganicLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final OrganicLayouter organic, final OptionHandler options) {
    organic.setPreferredEdgeLength(options.getInt(SECTION_VISUAL, ITEM_PREFERRED_EDGE_LENGTH));
    organic.setMaximumDuration(1000 * options.getInt(SECTION_ALGORITHM, ITEM_MAXIMAL_DURATION));
    
    final String ip = options.getString(SECTION_VISUAL, ITEM_INITIAL_PLACEMENT);
    if (VALUE_RANDOM.equals(ip)) {
      organic.setInitialPlacement(OrganicLayouter.RANDOM);
    } else if (VALUE_AT_ORIGIN.equals(ip)) {
      organic.setInitialPlacement(OrganicLayouter.ZERO);
    } else {
      // else if AS_IS.equals(ip)
      organic.setInitialPlacement(OrganicLayouter.AS_IS);
    }
    
    final String sp = options.getString(SECTION_VISUAL, ITEM_SPHERE_OF_ACTION);
    if (VALUE_ALL.equals(sp)) {
      organic.setSphereOfAction(OrganicLayouter.ALL);
    } else if (VALUE_MAINLY_SELECTION.equals(sp)) {
      organic.setSphereOfAction(OrganicLayouter.MAINLY_SELECTION);
    } else {
      // if VALUE_ONLY_SELECTION.equals(sp)
      organic.setSphereOfAction(OrganicLayouter.ONLY_SELECTION);
    }

    organic.setGravityFactor(options.getDouble(SECTION_VISUAL, ITEM_GRAVITY_FACTOR));
    organic.setObeyNodeSize(options.getBool(SECTION_VISUAL, ITEM_OBEY_NODE_SIZES));
    organic.setIterationFactor(options.getDouble(SECTION_ALGORITHM, ITEM_ITERATION_FACTOR));
    organic.setActivateTreeBeautifier(options.getBool(SECTION_VISUAL, ITEM_ACTIVATE_TREE_BEAUTIFIER));
    organic.setActivateDeterministicMode(options.getBool(SECTION_ALGORITHM, ITEM_ACTIVATE_DETERMINISTIC_MODE));
    organic.setMultiThreadingAllowed(options.getBool(SECTION_ALGORITHM, ITEM_ALLOW_MULTI_THREADING));
    organic.setAttraction(options.getInt(SECTION_VISUAL, ITEM_ATTRACTION));
    organic.setRepulsion(2-options.getInt(SECTION_VISUAL, ITEM_REPULSION));
    organic.setGroupNodeCompactness(options.getDouble(SECTION_GROUPING, ITEM_GROUP_NODE_COMPACTNESS));
    ((ComponentLayouter) organic.getComponentLayouter()).setStyle(ComponentLayouter.STYLE_MULTI_ROWS);
    
    if (HierarchyManager.containsGroupNodes(getGraph2D())) {
      if (options.get(SECTION_GROUPING, ITEM_GROUP_LAYOUT_POLICY).equals(VALUE_FIX_GROUPS)) {
        organic.setGroupNodePolicy(OrganicLayouter.FIXED_GROUPS_POLICY);
      } else if (options.get(SECTION_GROUPING, ITEM_GROUP_LAYOUT_POLICY).equals(VALUE_IGNORE_GROUPS)) {
        organic.setGroupNodePolicy(OrganicLayouter.IGNORE_GROUPS_POLICY);
      } else {
        organic.setGroupNodePolicy(OrganicLayouter.LAYOUT_GROUPS_POLICY);
      }
    }
  }
}
