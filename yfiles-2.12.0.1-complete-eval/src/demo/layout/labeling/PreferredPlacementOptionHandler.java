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
package demo.layout.labeling;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.YCursor;
import y.layout.LabelLayoutConstants;
import y.layout.PreferredPlacementDescriptor;
import y.option.MappedListCellRenderer;
import y.option.OptionHandler;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.YLabel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An OptionHandler that is used manipulate the values of the PreferredPlacementDescriptor.
 */
public class PreferredPlacementOptionHandler extends OptionHandler {
  private static final String EDGE_LABEL_PROPERTIES = "Edge Label Properties";
  private static final String ITEM_TEXT = "Text";
  private static final String ITEM_PLACEMENT = "Placement Along the Edge";
  private static final String ITEM_SIDE = "Side of the Edge";
  private static final String ITEM_SIDE_REFERENCE = "Side Reference";
  private static final String ITEM_ANGLE = "Angle (in Degrees)";
  private static final String ITEM_ANGLE_REFERENCE = "Angle Reference";
  private static final String ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE = "Angle Rotation on Right Side";
  // u00b0 is the unicode escape sequence for the degrees symbol
  private static final String ITEM_ANGLE_OFFSET_ON_RIGHT_SIDE = "Add 180\u00b0 on Right Side";
  private static final String ITEM_DISTANCE_TO_EDGE = "Distance to Edge";

  private static final Byte DEFAULT_PLACE = new Byte(
    (byte) LabelLayoutConstants.PLACE_ANYWHERE);
  private static final Byte DEFAULT_SIDE = new Byte(
    (byte) LabelLayoutConstants.PLACE_ANYWHERE);
  private static final Byte DEFAULT_SIDE_REFERENCE = new Byte(
    (byte) (PreferredPlacementDescriptor.SIDE_IS_RELATIVE_TO_EDGE_FLOW |
            PreferredPlacementDescriptor.SIDE_IS_ABSOLUTE_WITH_LEFT_IN_NORTH |
            PreferredPlacementDescriptor.SIDE_IS_ABSOLUTE_WITH_RIGHT_IN_NORTH));
  private static final Byte DEFAULT_ANGLE_REFERENCE = new Byte(
    (byte) (PreferredPlacementDescriptor.ANGLE_IS_ABSOLUTE |
            PreferredPlacementDescriptor.ANGLE_IS_RELATIVE_TO_EDGE_FLOW));
  private static final Byte DEFAULT_ANGLE_ROTATION_ON_RIGHT_SIDE = new Byte(
    (byte) (PreferredPlacementDescriptor.ANGLE_ON_RIGHT_SIDE_CO_ROTATING |
            PreferredPlacementDescriptor.ANGLE_ON_RIGHT_SIDE_COUNTER_ROTATING));
  private static final Double DEFAULT_ANGLE = new Double(0);
  private static final Double DEFAULT_DISTANCE_TO_EDGE = new Double(0);
  private static final Boolean DEFAULT_ANGLE_OFFSET_ON_RIGHT_SIDE = Boolean.FALSE;

  /**
   * Creates a new option handler for {@link PreferredPlacementDescriptor} settings.
   */
  public PreferredPlacementOptionHandler() {
    super(EDGE_LABEL_PROPERTIES);

    addString(ITEM_TEXT, "", 2);

    final Map placementMap = createPlacementsToStringMap();
    addEnum(
      ITEM_PLACEMENT,
      placementMap.keySet().toArray(),
      DEFAULT_PLACE,
      new MappedListCellRenderer(placementMap));

    final Map sideMap = createSidesToStringMap();
    addEnum(
      ITEM_SIDE,
      sideMap.keySet().toArray(),
      DEFAULT_SIDE,
      new MappedListCellRenderer(sideMap));

    final Map sideInterpretationMap = createSideReferencesToStringMap();
    addEnum(
      ITEM_SIDE_REFERENCE,
      sideInterpretationMap.keySet().toArray(),
      DEFAULT_SIDE_REFERENCE,
      new MappedListCellRenderer(sideInterpretationMap));

    addDouble(ITEM_ANGLE, DEFAULT_ANGLE.doubleValue());

    final Map angleReferenceMap = createAngleReferencesToStringMap();
    addEnum(
      ITEM_ANGLE_REFERENCE,
      angleReferenceMap.keySet().toArray(),
      DEFAULT_ANGLE_REFERENCE,
      new MappedListCellRenderer(angleReferenceMap));

    final Map angleRotationOnRightSideMap = createAngleRotationsOnRightSideToStringMap();
    addEnum(
      ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE,
      angleRotationOnRightSideMap.keySet().toArray(),
      DEFAULT_ANGLE_ROTATION_ON_RIGHT_SIDE,
      new MappedListCellRenderer(angleRotationOnRightSideMap));

    addBool(ITEM_ANGLE_OFFSET_ON_RIGHT_SIDE, DEFAULT_ANGLE_OFFSET_ON_RIGHT_SIDE.booleanValue());

    addDouble(ITEM_DISTANCE_TO_EDGE, DEFAULT_DISTANCE_TO_EDGE.doubleValue());
  }

  /**
   * Retrieves the values from the edge labels of the given graph and stores them in the respective option items.
   *
   * @param graph graph whose edge labels to store the values from
   */
  public void updateValues(final Graph2D graph) {
    final Iterator labelIterator = getEdgeLabels(graph).iterator();
    if (!labelIterator.hasNext()) {
      return;
    }

    EdgeLabel edgeLabel = (EdgeLabel) labelIterator.next();
    PreferredPlacementDescriptor descriptor = edgeLabel.getPreferredPlacementDescriptor();

    // Get the initial values from the first selected node.
    final String text = edgeLabel.getText();
    boolean sameTexts = true;
    final byte placement = descriptor.getPlaceAlongEdge();
    boolean samePlacements = true;
    final byte side = descriptor.getSideOfEdge();
    boolean sameSides = true;
    final byte sideReference = descriptor.getSideReference();
    boolean sameSideReferences = true;
    final double angle = descriptor.getAngle();
    boolean sameAngles = true;
    final byte angleReference = descriptor.getAngleReference();
    boolean sameAngleReferences = true;
    final byte angleRotationOnRightSide = descriptor.getAngleRotationOnRightSide();
    boolean sameAngleRotations = true;
    final boolean hasAngleOffset = descriptor.isAngleOffsetOnRightSide180();
    boolean sameAngleOffsets = true;
    final double distance = descriptor.getDistanceToEdge();
    boolean sameDistances = true;

    // Get all further values from the remaining set of selected edge labels.
    while (labelIterator.hasNext()) {
      edgeLabel = (EdgeLabel) labelIterator.next();
      descriptor = edgeLabel.getPreferredPlacementDescriptor();

      if (sameTexts && !text.equals(edgeLabel.getText())) {
        sameTexts = false;
      }
      if (samePlacements && placement != descriptor.getPlaceAlongEdge()) {
        samePlacements = false;
      }
      if (sameSides && side != descriptor.getSideOfEdge()) {
        sameSides = false;
      }
      if (sameSideReferences && sideReference != descriptor.getSideReference()) {
        sameSideReferences = false;
      }
      if (sameAngles && Double.compare(angle, descriptor.getAngle()) != 0) {
        sameAngles = false;
      }
      if (sameAngleReferences &&
        angleReference != descriptor.getAngleReference()) {
        sameAngleReferences = false;
      }
      if (sameAngleRotations &&
        angleRotationOnRightSide != descriptor.getAngleRotationOnRightSide()) {
        sameAngleRotations = false;
      }
      if (sameAngleOffsets && hasAngleOffset != descriptor.isAngleOffsetOnRightSide180()) {
        sameAngleOffsets = false;
      }
      if (sameDistances && Double.compare(distance, descriptor.getDistanceToEdge()) != 0) {
        sameDistances = false;
      }

      if (!(sameTexts | samePlacements | sameSides | sameSideReferences | sameAngles | sameAngleReferences
        | sameAngleRotations | sameDistances)) {
        break;
      }
    }

    // If, for a single property, there are multiple values present in the set of selected edge labels, the
    // respective option item is set to indicate an "undefined value" state.
    // Note that property "valueUndefined" for an option item is set *after* its value has actually been modified!
    set(ITEM_TEXT, sameTexts ? text : null);
    getItem(ITEM_TEXT).setValueUndefined(!sameTexts);

    set(ITEM_PLACEMENT, samePlacements ? new Byte(placement) : DEFAULT_PLACE);
    getItem(ITEM_PLACEMENT).setValueUndefined(!samePlacements);

    set(ITEM_SIDE, sameSides ? new Byte(side) : DEFAULT_SIDE);
    getItem(ITEM_SIDE).setValueUndefined(!sameSides);

    set(ITEM_SIDE_REFERENCE, sameSideReferences ? new Byte(sideReference) : DEFAULT_SIDE_REFERENCE);
    getItem(ITEM_SIDE_REFERENCE).setValueUndefined(!sameSideReferences);

    set(ITEM_ANGLE, sameAngles ? new Double(Math.toDegrees(angle)) : new Double(-1));
    getItem(ITEM_ANGLE).setValueUndefined(!sameAngles);

    set(ITEM_ANGLE_REFERENCE, sameAngleReferences ? new Byte(angleReference) : DEFAULT_ANGLE_REFERENCE);
    getItem(ITEM_ANGLE_REFERENCE).setValueUndefined(!sameAngleReferences);

    set(ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE, sameAngleRotations ? new Byte(angleRotationOnRightSide) :
      DEFAULT_ANGLE_ROTATION_ON_RIGHT_SIDE);
    getItem(ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE).setValueUndefined(!sameAngleRotations);

    set(ITEM_ANGLE_OFFSET_ON_RIGHT_SIDE, sameAngleOffsets ? hasAngleOffset ? Boolean.TRUE : Boolean.FALSE :
      DEFAULT_ANGLE_OFFSET_ON_RIGHT_SIDE);
    getItem(ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE).setValueUndefined(!sameAngleOffsets);

    set(ITEM_DISTANCE_TO_EDGE, sameDistances ? new Double(distance) : new Double(-1));
    getItem(ITEM_DISTANCE_TO_EDGE).setValueUndefined(!sameDistances);
  }

  /**
   * Commits the stored edge label properties to the given edge labels.
   *
   * @param graph graph whose selected edge labels to commit the stored properties
   */
  public void commitValues(final Graph2D graph) {
    for (Iterator labelIterator = getEdgeLabels(graph).iterator(); labelIterator.hasNext(); ) {
      final EdgeLabel edgeLabel = (EdgeLabel) labelIterator.next();
      final PreferredPlacementDescriptor prototype =
              edgeLabel.getPreferredPlacementDescriptor();

      final PreferredPlacementDescriptor descriptor =
              new PreferredPlacementDescriptor(prototype);
      if (!getItem(ITEM_TEXT).isValueUndefined()) {
        edgeLabel.setText(getString(ITEM_TEXT));
      }
      if (!getItem(ITEM_PLACEMENT).isValueUndefined()) {
        descriptor.setPlaceAlongEdge(((Byte) get(ITEM_PLACEMENT)).byteValue());
      }
      if (!getItem(ITEM_SIDE).isValueUndefined()) {
        descriptor.setSideOfEdge(((Byte) get(ITEM_SIDE)).byteValue());
      }
      if (!getItem(ITEM_SIDE_REFERENCE).isValueUndefined()) {
        descriptor.setSideReference(((Byte) get(ITEM_SIDE_REFERENCE)).byteValue());
      }
      if (!getItem(ITEM_ANGLE).isValueUndefined()) {
        descriptor.setAngle(Math.toRadians(getDouble(ITEM_ANGLE)));
      }
      if (!getItem(ITEM_ANGLE_REFERENCE).isValueUndefined()) {
        descriptor.setAngleReference(((Byte) get(ITEM_ANGLE_REFERENCE)).byteValue());
      }
      if (!getItem(ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE).isValueUndefined()) {
        descriptor.setAngleRotationOnRightSide(((Byte) get(ITEM_ANGLE_ROTATION_ON_RIGHT_SIDE)).byteValue());
      }
      if (!getItem(ITEM_ANGLE_OFFSET_ON_RIGHT_SIDE).isValueUndefined()) {
        final byte angleOffset = getBool(ITEM_ANGLE_OFFSET_ON_RIGHT_SIDE) ?
          PreferredPlacementDescriptor.ANGLE_OFFSET_ON_RIGHT_SIDE_180 :
          PreferredPlacementDescriptor.ANGLE_OFFSET_ON_RIGHT_SIDE_0;
        descriptor.setAngleOffsetOnRightSide(angleOffset);
      }
      if (!getItem(ITEM_DISTANCE_TO_EDGE).isValueUndefined()) {
        descriptor.setDistanceToEdge(getDouble(ITEM_DISTANCE_TO_EDGE));
      }

      if (!descriptor.equals(prototype)) {
        edgeLabel.setPreferredPlacementDescriptor(descriptor);
      }
    }
  }

  /**
   * Returns a list for all selected edge labels of the given graph. If there is no edge label selected, all edge labels
   * are returned.
   *
   * @param graph the graph to return its edge labels
   * @return a list of edge labels
   */
  private List getEdgeLabels(final Graph2D graph) {
    List labels = getSelectedEdgeLabels(graph);
    if (labels.isEmpty()) {
      labels = getAllEdgeLabels(graph);
    }
    return labels;
  }

  /**
   * Returns a list for all selected edge labels of the given graph.
   *
   * @param graph graph to return its selected edge labels.
   * @return a list of all selected edge labels.
   */
  private List getSelectedEdgeLabels(final Graph2D graph) {
    final List selectedEdgeLabels = new ArrayList();
    for (YCursor selectedLabels = graph.selectedLabels(); selectedLabels.ok(); selectedLabels.next()) {
      final YLabel selectedLabel = (YLabel) selectedLabels.current();
      if (selectedLabel instanceof EdgeLabel) {
        selectedEdgeLabels.add(selectedLabel);
      }
    }
    return selectedEdgeLabels;
  }

  /**
   * Returns a list for all edge labels of the given graph.
   *
   * @param graph graph to return its edge labels.
   * @return a list for all edge labels.
   */
  private List getAllEdgeLabels(final Graph2D graph) {
    final List result = new ArrayList();
    for (EdgeCursor edgeCursor = graph.edges(); edgeCursor.ok(); edgeCursor.next()) {
      final Edge edge = edgeCursor.edge();
      final EdgeRealizer edgeRealizer = graph.getRealizer(edge);
      for (int i = 0, n = edgeRealizer.labelCount(); i < n; i++) {
        final EdgeLabel edgeLabel = edgeRealizer.getLabel(i);
        result.add(edgeLabel);
      }
    }
    return result;
  }

  /**
   * Creates a map that maps the preferred placement constants to strings.
   */
  private static Map createPlacementsToStringMap() {
    final Map result = new LinkedHashMap(4);
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_SOURCE), "At Source");
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_CENTER), "At Center");
    result.put(new Byte(LabelLayoutConstants.PLACE_AT_TARGET), "At Target");
    result.put(DEFAULT_PLACE, "Anywhere");
    return result;
  }

  /**
   * Creates a map that maps the preferred side constants to strings.
   */
  private static Map createSidesToStringMap() {
    final Map result = new LinkedHashMap(4);
    result.put(new Byte(LabelLayoutConstants.PLACE_LEFT_OF_EDGE), "Left Of Edge");
    result.put(new Byte(LabelLayoutConstants.PLACE_ON_EDGE), "On Edge");
    result.put(new Byte(LabelLayoutConstants.PLACE_RIGHT_OF_EDGE), "Right Of Edge");
    result.put(DEFAULT_SIDE, "Any side");
    return result;
  }

  /**
   * Creates a map that maps the side references constants to strings.
   */
  private static Map createSideReferencesToStringMap() {
    final Map result = new LinkedHashMap(3);
    result.put(new Byte(PreferredPlacementDescriptor.SIDE_IS_RELATIVE_TO_EDGE_FLOW), "Relative to edge flow");
    result.put(new Byte(PreferredPlacementDescriptor.SIDE_IS_ABSOLUTE_WITH_LEFT_IN_NORTH), "Absolute with left in north");
    result.put(new Byte(PreferredPlacementDescriptor.SIDE_IS_ABSOLUTE_WITH_RIGHT_IN_NORTH), "Absolute with right in north");
    return result;
  }

  /**
   * Creates a map that maps the angle reference constants to strings.
   */
  private static Map createAngleReferencesToStringMap() {
    final Map result = new LinkedHashMap(2);
    result.put(new Byte(PreferredPlacementDescriptor.ANGLE_IS_ABSOLUTE), "Absolute");
    result.put(new Byte(PreferredPlacementDescriptor.ANGLE_IS_RELATIVE_TO_EDGE_FLOW), "Relative to edge");
    return result;
  }

  /**
   * Creates a map that maps the angle rotation on right side of the ede constants to strings.
   */
  private static Map createAngleRotationsOnRightSideToStringMap() {
    final Map result = new LinkedHashMap(2);
    result.put(new Byte(PreferredPlacementDescriptor.ANGLE_ON_RIGHT_SIDE_CO_ROTATING), "Co-rotating");
    result.put(new Byte(PreferredPlacementDescriptor.ANGLE_ON_RIGHT_SIDE_COUNTER_ROTATING), "Counter-rotating");
    return result;
  }
}
