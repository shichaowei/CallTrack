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

import y.layout.LayoutOrientation;
import y.layout.router.OrganicEdgeRouter;
import y.layout.router.StraightLineEdgeRouter;
import y.layout.router.polyline.EdgeRouter;
import y.layout.seriesparallel.DefaultPortAssignment;
import y.layout.seriesparallel.EdgeLayoutDescriptor;
import y.layout.seriesparallel.SeriesParallelLayouter;
import y.option.ConstraintManager;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;

/**
 * This module represents an interactive configurator and launcher for {@link y.layout.seriesparallel.SeriesParallelLayouter}.
 *
 */
public class SeriesParallelLayoutModule extends LayoutModule {

  //// Module 'Series-Parallel Layout'
  protected static final String MODULE_SERIES_PARALLEL = "SERIES_PARALLEL";

  //// Section 'General'
  protected static final String SECTION_GENERAL = "GENERAL";
  // Section 'General' items
  protected static final String ITEM_ORIENTATION = "ORIENTATION";
  protected static final String VALUE_RIGHT_TO_LEFT = "RIGHT_TO_LEFT";
  protected static final String VALUE_BOTTOM_TO_TOP = "BOTTOM_TO_TOP";
  protected static final String VALUE_LEFT_TO_RIGHT = "LEFT_TO_RIGHT";
  protected static final String VALUE_TOP_TO_BOTTOM = "TOP_TO_BOTTOM";
  protected static final String ITEM_VERTICAL_ALIGNMENT = "VERTICAL_ALIGNMENT";
  protected static final String VALUE_ALIGNMENT_TOP = "ALIGNMENT_TOP";
  protected static final String VALUE_ALIGNMENT_CENTER = "ALIGNMENT_CENTER";
  protected static final String VALUE_ALIGNMENT_BOTTOM = "ALIGNMENT_BOTTOM";
  protected static final String ITEM_FROM_SKETCH_MODE = "FROM_SKETCH_MODE";
  protected static final String ITEM_ROUTING_STYLE_FOR_NON_SERIES_PARALLEL_EDGES = "ROUTING_STYLE_FOR_NON_SERIES_PARALLEL_EDGES";
  protected static final String VALUE_ROUTE_ORGANIC = "ROUTE_ORGANIC";
  protected static final String VALUE_ROUTE_ORTHOGONAL = "ROUTE_ORTHOGONAL";
  protected static final String VALUE_ROUTE_STRAIGHTLINE = "ROUTE_STRAIGHTLINE";
  protected static final String TITLE_MINIMUM_DISTANCES = "MINIMUM_DISTANCES";
  protected static final String ITEM_NODE_TO_NODE_DISTANCE = "NODE_TO_NODE_DISTANCE";
  protected static final String ITEM_NODE_TO_EDGE_DISTANCE = "NODE_TO_EDGE_DISTANCE";
  protected static final String ITEM_EDGE_TO_EDGE_DISTANCE = "EDGE_TO_EDGE_DISTANCE";
  protected static final String TITLE_LABELING = "LABELING";
  protected static final String ITEM_CONSIDER_NODE_LABELS = "CONSIDER_NODE_LABELS";
  protected static final String ITEM_INTEGRATED_EDGE_LABELING = "INTEGRATED_EDGE_LABELING";


  //// Section 'Edge Settings'
  protected static final String SECTION_EDGE_SETTINGS = "EDGE_SETTINGS";
  //// Section 'Edge Settings' items
  protected static final String ITEM_PORT_STYLE = "PORT_STYLE";
  protected static final String VALUE_CENTER_PORTS = "CENTER_PORTS";
  protected static final String VALUE_DISTRIBUTED_PORTS = "DISTRIBUTED_PORTS";
  protected static final String ITEM_MINIMUM_FIRST_SEGMENT_LENGTH = "MINIMUM_FIRST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_LAST_SEGMENT_LENGTH = "MINIMUM_LAST_SEGMENT_LENGTH";
  protected static final String ITEM_MINIMUM_EDGE_LENGTH = "MINIMUM_EDGE_LENGTH";
  protected static final String ITEM_ROUTING_STYLE = "ROUTING_STYLE";
  protected static final String VALUE_ROUTING_STYLE_ORTHOGONAL = "ROUTING_STYLE_ORTHOGONAL";
  protected static final String VALUE_ROUTING_STYLE_OCTILINEAR = "ROUTING_STYLE_OCTILINEAR";
  protected static final String VALUE_ROUTING_STYLE_POLYLINE = "ROUTING_STYLE_POLYLINE";
  protected static final String ITEM_PREFERRED_OCTILINEAR_SEGMENT_LENGTH = "PREFERRED_OCTILINEAR_SEGMENT_LENGTH";
  protected static final String ITEM_POLYLINE_DISTANCE = "POLYLINE_DISTANCE";
  protected static final String ITEM_MINIMUM_SLOPE = "MINIMUM_SLOPE";
  protected static final String ITEM_ROUTE_IN_FLOW = "ROUTE_IN_FLOW";

  /**
   * Creates an instance of this module.
   */
  public SeriesParallelLayoutModule() {
    super(MODULE_SERIES_PARALLEL);
  }

  /**
   * Factory method responsible for creating and initializing the OptionHandler for this module.
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());

    final SeriesParallelLayouter defaults = new SeriesParallelLayouter();
    final EdgeLayoutDescriptor eld = defaults.getDefaultEdgeLayoutDescriptor();

    //// Section 'General'
    options.useSection(SECTION_GENERAL);
    // Populate section
    options.addEnum(ITEM_ORIENTATION, new Object[]{
            VALUE_TOP_TO_BOTTOM,
            VALUE_LEFT_TO_RIGHT,
            VALUE_BOTTOM_TO_TOP,
            VALUE_RIGHT_TO_LEFT,
    }, 0);
    options.addEnum(ITEM_VERTICAL_ALIGNMENT, new String[]{
            VALUE_ALIGNMENT_TOP,
            VALUE_ALIGNMENT_CENTER,
            VALUE_ALIGNMENT_BOTTOM,
    }, 1);
    options.addBool(ITEM_FROM_SKETCH_MODE, defaults.isFromSketchModeEnabled());
    options.addEnum(ITEM_ROUTING_STYLE_FOR_NON_SERIES_PARALLEL_EDGES, new String[]{
            VALUE_ROUTE_ORGANIC,
            VALUE_ROUTE_ORTHOGONAL,
            VALUE_ROUTE_STRAIGHTLINE
    }, 0);

    // Group 'Minimum Distances'
    final OptionGroup minDistGroup = new OptionGroup();
    minDistGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_MINIMUM_DISTANCES);
    // Populate group
    minDistGroup.addItem(options.addDouble(ITEM_NODE_TO_NODE_DISTANCE, 30.0d));
    minDistGroup.addItem(options.addDouble(ITEM_NODE_TO_EDGE_DISTANCE, 15.0d));
    minDistGroup.addItem(options.addDouble(ITEM_EDGE_TO_EDGE_DISTANCE, 15.0d));

    // Group 'Labeling'
    final OptionGroup labelingGroup = new OptionGroup();
    // Populate group
    labelingGroup.setAttribute(OptionGroup.ATTRIBUTE_TITLE, TITLE_LABELING);
    labelingGroup.addItem(options.addBool(ITEM_CONSIDER_NODE_LABELS, true));
    labelingGroup.addItem(options.addBool(ITEM_INTEGRATED_EDGE_LABELING, true));

    //// Section 'Edge Settings'
    options.useSection(SECTION_EDGE_SETTINGS);
    // Populate section
    options.addEnum(ITEM_PORT_STYLE, new String[]{
            VALUE_CENTER_PORTS,
            VALUE_DISTRIBUTED_PORTS,
    }, 0);
    options.addDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH, eld.getMinimumFirstSegmentLength());
    options.addDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH, eld.getMinimumLastSegmentLength());
    options.addDouble(ITEM_MINIMUM_EDGE_LENGTH, 20.0d);
    final OptionItem itemRoutingStyle = options.addEnum(ITEM_ROUTING_STYLE, new String[]{
            VALUE_ROUTING_STYLE_ORTHOGONAL,
            VALUE_ROUTING_STYLE_OCTILINEAR,
            VALUE_ROUTING_STYLE_POLYLINE,
    }, 0);
    final OptionItem itemSegmentLength = options.addDouble(ITEM_PREFERRED_OCTILINEAR_SEGMENT_LENGTH, defaults.getPreferredOctilinearSegmentLength());
    final OptionItem itemDistance = options.addDouble(ITEM_POLYLINE_DISTANCE, defaults.getMinimumPolylineSegmentLength());
    final OptionItem itemMinimumSlope = options.addDouble(ITEM_MINIMUM_SLOPE, defaults.getMinimumSlope(), 0, 5);
    options.addBool(ITEM_ROUTE_IN_FLOW, true);
    // Enable/disable items depending on specific values
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    optionConstraints.setEnabledOnValueEquals(itemRoutingStyle, VALUE_ROUTING_STYLE_OCTILINEAR, itemSegmentLength);
    optionConstraints.setEnabledOnValueEquals(itemRoutingStyle, VALUE_ROUTING_STYLE_POLYLINE, itemDistance);
    optionConstraints.setEnabledOnValueEquals(itemRoutingStyle, VALUE_ROUTING_STYLE_POLYLINE, itemMinimumSlope);

    return options;
  }

  /**
   * Main module execution routine which configures and launches the series-parallel layouter.
   */
  protected void mainrun() {
    final SeriesParallelLayouter series = new SeriesParallelLayouter();

    final OptionHandler options = getOptionHandler();

    configure(series, options);

    launchLayouter(series);
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param series the <code>SeriesParallelLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure( final SeriesParallelLayouter series, final OptionHandler options ) {
    series.setGeneralGraphHandlingEnabled(true);

    final Object orientation = options.get(ITEM_ORIENTATION);
    if (VALUE_LEFT_TO_RIGHT.equals(orientation)) {
      series.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);
    } else if (VALUE_BOTTOM_TO_TOP.equals(orientation)) {
      series.setLayoutOrientation(LayoutOrientation.BOTTOM_TO_TOP);
    } else if (VALUE_RIGHT_TO_LEFT.equals(orientation)) {
      series.setLayoutOrientation(LayoutOrientation.RIGHT_TO_LEFT);
    } else {
      series.setLayoutOrientation(LayoutOrientation.TOP_TO_BOTTOM);
    }

    final String verticalAlignment = options.getString(ITEM_VERTICAL_ALIGNMENT);
    if (VALUE_ALIGNMENT_TOP.equals(verticalAlignment)) {
      series.setVerticalAlignment(0);
    } else if (VALUE_ALIGNMENT_CENTER.equals(verticalAlignment)) {
      series.setVerticalAlignment(0.5);
    } else { // VALUE_ALIGNMENT_BOTTOM
      series.setVerticalAlignment(1);
    }

    final String routingStyle = options.getString(ITEM_ROUTING_STYLE);
    if (VALUE_ROUTING_STYLE_ORTHOGONAL.equals(routingStyle)) {
      series.setRoutingStyle(SeriesParallelLayouter.ROUTING_STYLE_ORTHOGONAL);
    } else if (VALUE_ROUTING_STYLE_OCTILINEAR.equals(routingStyle)) {
      series.setRoutingStyle(SeriesParallelLayouter.ROUTING_STYLE_OCTILINEAR);
      series.setPreferredOctilinearSegmentLength(options.getDouble(ITEM_PREFERRED_OCTILINEAR_SEGMENT_LENGTH));
    } else { // VALUE_ROUTING_STYLE_POLYLINE
      series.setRoutingStyle(SeriesParallelLayouter.ROUTING_STYLE_POLYLINE);
      series.setMinimumPolylineSegmentLength(options.getDouble(ITEM_POLYLINE_DISTANCE));
      series.setMinimumSlope(options.getDouble(ITEM_MINIMUM_SLOPE));
    }

    final DefaultPortAssignment defaultPortAssignment = (DefaultPortAssignment) series.getDefaultPortAssignment();
    if (options.getBool(ITEM_ROUTE_IN_FLOW)) {
      defaultPortAssignment.setForkStyle(DefaultPortAssignment.FORK_STYLE_OUTSIDE_NODE);
    } else {
      defaultPortAssignment.setForkStyle(DefaultPortAssignment.FORK_STYLE_AT_NODE);
    }

    series.setFromSketchModeEnabled(options.getBool(ITEM_FROM_SKETCH_MODE));

    final Object nonSeriesParallelRoutingStyle = options.get(ITEM_ROUTING_STYLE_FOR_NON_SERIES_PARALLEL_EDGES);
    if (VALUE_ROUTE_ORGANIC.equals(nonSeriesParallelRoutingStyle)) {
      final OrganicEdgeRouter organic = new OrganicEdgeRouter();
      series.setNonSeriesParallelEdgeRouter(organic);
      series.setNonSeriesParallelEdgesDpKey(OrganicEdgeRouter.ROUTE_EDGE_DPKEY);
    } else if (VALUE_ROUTE_ORTHOGONAL.equals(nonSeriesParallelRoutingStyle)) {
      final EdgeRouter orthogonal = new EdgeRouter();
      orthogonal.setReroutingEnabled(true);
      orthogonal.setSphereOfAction(EdgeRouter.ROUTE_SELECTED_EDGES);
      series.setNonSeriesParallelEdgeRouter(orthogonal);
      series.setNonSeriesParallelEdgesDpKey(orthogonal.getSelectedEdgesDpKey());
    } else if (VALUE_ROUTE_STRAIGHTLINE.equals(nonSeriesParallelRoutingStyle)) {
      final StraightLineEdgeRouter straightLine = new StraightLineEdgeRouter();
      straightLine.setSphereOfAction(StraightLineEdgeRouter.ROUTE_SELECTED_EDGES);
      series.setNonSeriesParallelEdgeRouter(straightLine);
      series.setNonSeriesParallelEdgesDpKey(straightLine.getSelectedEdgesDpKey());
    }

    series.setMinimumNodeToNodeDistance(options.getDouble(ITEM_NODE_TO_NODE_DISTANCE));
    series.setMinimumNodeToEdgeDistance(options.getDouble(ITEM_NODE_TO_EDGE_DISTANCE));
    series.setMinimumEdgeToEdgeDistance(options.getDouble(ITEM_EDGE_TO_EDGE_DISTANCE));

    final Object portStyle = options.get(ITEM_PORT_STYLE);
    if (VALUE_CENTER_PORTS.equals(portStyle)) {
      defaultPortAssignment.setMode(DefaultPortAssignment.PORT_ASSIGNMENT_MODE_CENTER);
    } else {
      defaultPortAssignment.setMode(DefaultPortAssignment.PORT_ASSIGNMENT_MODE_DISTRIBUTED);
    }
    final EdgeLayoutDescriptor eld = series.getDefaultEdgeLayoutDescriptor();
    eld.setMinimumFirstSegmentLength(options.getDouble(ITEM_MINIMUM_FIRST_SEGMENT_LENGTH));
    eld.setMinimumLastSegmentLength(options.getDouble(ITEM_MINIMUM_LAST_SEGMENT_LENGTH));
    eld.setMinimumLength(options.getDouble(ITEM_MINIMUM_EDGE_LENGTH));

    series.setConsiderNodeLabelsEnabled(options.getBool(ITEM_CONSIDER_NODE_LABELS));
    series.setIntegratedEdgeLabelingEnabled(options.getBool(ITEM_INTEGRATED_EDGE_LABELING));
  }
}
