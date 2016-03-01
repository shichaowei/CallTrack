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
package demo.layout.multipage;

import y.layout.multipage.MultiPageLayouter;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;

import java.beans.PropertyChangeListener;

/**
 * Provides settings for multi-page layout.
 */
class MultiPageLayoutOptionHandler extends OptionHandler {
  private static final String LAYOUT = "Page Layout";
  static final String LAYOUT_ORGANIC = "Organic";
  static final String LAYOUT_HIERARCHIC = "Hierarchic";
  static final String LAYOUT_ORTHOGONAL = "Orthogonal";
  static final String LAYOUT_COMPACT_ORTHOGONAL = "Compact Orthogonal";
  static final String LAYOUT_CIRCULAR = "Circular";

  private static final String GROUPING = "Grouping";
  private static final String GROUPING_IGNORE_GROUPS = "Ignore Groups";
  private static final String GROUPING_ONLY_COMMON_NODES = "Common Nodes Only";
  private static final String GROUPING_INCLUDE_CONNECTORS = "Include Connectors";

  private static final String SEPARATE_MULTIEDGES = "Separate Multi-edges";
  private static final String SEPARATE_TYPES = "Separate Types";
  private static final String SEPARATE_DIRECTION = "Separate Direction";

  private static final String MAXIMUM_WIDTH = "Maximum Width";
  private static final String MAXIMUM_HEIGHT = "Maximum Height";
  private static final String SINGLE_PAGE_LAYOUT = "Single Page Layout";
  private static final String MAXIMAL_DURATION = "Max. Duration (sec)";
  private static final String DRAW_PAGE = "Draw Page";


  static final OptionSet OPTIONS_NETWORK_SMALL_DISPLAY;
  static final OptionSet OPTIONS_NETWORK_LARGE_DISPLAY;
  static final OptionSet OPTIONS_CLASS_DIAGRAM_SMALL_DISPLAY;
  static final OptionSet OPTIONS_CLASS_DIAGRAM_LARGE_DISPLAY;
  static {
    {
      final OptionSet setting = new OptionSet("Network on small display");
      setting.setLayoutStyle(MultiPageLayoutOptionHandler.LAYOUT_ORGANIC);
      setting.setMaximumHeight(800);
      setting.setMaximumWidth(500);
      OPTIONS_NETWORK_SMALL_DISPLAY = setting;
    }
    {
      final OptionSet setting = new OptionSet("Network on large display");
      setting.setLayoutStyle(MultiPageLayoutOptionHandler.LAYOUT_ORGANIC);
      setting.setMaximumHeight(2000);
      setting.setMaximumWidth(2000);
      OPTIONS_NETWORK_LARGE_DISPLAY = setting;
    }
    {
      final OptionSet setting = new OptionSet("Class diagram on small display");
      setting.setLayoutStyle(MultiPageLayoutOptionHandler.LAYOUT_HIERARCHIC);
      setting.setMaximumHeight(800);
      setting.setMaximumWidth(500);
      OPTIONS_CLASS_DIAGRAM_SMALL_DISPLAY = setting;
    }
    {
      final OptionSet setting = new OptionSet("Class diagram on large display");
      setting.setLayoutStyle(MultiPageLayoutOptionHandler.LAYOUT_HIERARCHIC);
      setting.setMaximumHeight(2000);
      setting.setMaximumWidth(2000);
      OPTIONS_CLASS_DIAGRAM_LARGE_DISPLAY = setting;
    }
  }


  MultiPageLayoutOptionHandler() {
    super("Option Table");

    useSection("General");
    final Object[] layoutStyles = {
            LAYOUT_HIERARCHIC,
            LAYOUT_ORGANIC,
            LAYOUT_COMPACT_ORTHOGONAL,
            LAYOUT_ORTHOGONAL,
            LAYOUT_CIRCULAR
    };
    addEnum(LAYOUT, layoutStyles, 0);
    final OptionItem singleLayoutItem = addBool(SINGLE_PAGE_LAYOUT, false);
    final ConstraintManager cm = new ConstraintManager(this);
    final ConstraintManager.Condition pageLayoutCondition =
            cm.createConditionValueEquals(singleLayoutItem, Boolean.FALSE);
    final Object[] groupingModes = {
            GROUPING_IGNORE_GROUPS,
            GROUPING_ONLY_COMMON_NODES,
            GROUPING_INCLUDE_CONNECTORS
    };
    final OptionItem groupStyleItem = addEnum(GROUPING, groupingModes, 2);
    cm.setEnabledOnCondition(pageLayoutCondition, groupStyleItem);

    //restricting the maximum page size to at most 4000x4000 because
    //multi-page layout calculation time increases greatly when the maximum
    //page size grows    
    final OptionItem maxWidthItem = addInt(MAXIMUM_WIDTH, 600, 200, 4000);
    cm.setEnabledOnCondition(pageLayoutCondition, maxWidthItem);
    final OptionItem maxHeightItem = addInt(MAXIMUM_HEIGHT, 600, 200, 4000);
    cm.setEnabledOnCondition(pageLayoutCondition, maxHeightItem);

    final OptionItem maxDurationItem = addInt(MAXIMAL_DURATION, 30, 5, 5000);
    cm.setEnabledOnCondition(pageLayoutCondition, maxDurationItem);
    final OptionItem drawPageItem = addBool(DRAW_PAGE, true);
    cm.setEnabledOnCondition(pageLayoutCondition, drawPageItem);

    useSection("Connector Representation");
    final OptionItem separateTypesItem = addBool(SEPARATE_TYPES, false);
    cm.setEnabledOnCondition(pageLayoutCondition, separateTypesItem);
    final OptionItem separateDirectionItem = addBool(SEPARATE_DIRECTION, false);
    cm.setEnabledOnCondition(pageLayoutCondition, separateDirectionItem);
    final OptionItem separateMultiedgesItem = addBool(SEPARATE_MULTIEDGES, false);
    cm.setEnabledOnCondition(pageLayoutCondition, separateMultiedgesItem);
  }

  boolean isLayout(final String layout) {
    return getString(LAYOUT).equals(layout);
  }

  int getSeparationMask() {
    int mask = 0;
    if (getBool(SEPARATE_TYPES)) {
      mask = mask | MultiPageLayouter.EDGE_BUNDLE_DISTINGUISH_TYPES;
    }
    if (getBool(SEPARATE_DIRECTION)) {
      mask = mask | MultiPageLayouter.EDGE_BUNDLE_DISTINGUISH_DIRECTIONS;
    }
    if (getBool(SEPARATE_MULTIEDGES)) {
      mask = mask | MultiPageLayouter.EDGE_BUNDLE_DISTINGUISH_MULTIEDGES;
    }
    return mask;
  }

  boolean isDrawingPage() {
    return getBool(DRAW_PAGE) && !getBool(SINGLE_PAGE_LAYOUT);
  }

  boolean isUseSinglePageLayout() {
    return getBool(SINGLE_PAGE_LAYOUT);
  }

  byte getGroupingMode() {
    if (getString(GROUPING).equals(GROUPING_IGNORE_GROUPS)) {
      return MultiPageLayouter.GROUP_MODE_IGNORE;
    } else if(getString(GROUPING).equals(GROUPING_ONLY_COMMON_NODES)) {
      return MultiPageLayouter.GROUP_MODE_ORIGINAL_NODES_ONLY;
    } else {
      return MultiPageLayouter.GROUP_MODE_ALL_NODES;
    }
  }

  long getMaximalDuration() {
    return getInt(MAXIMAL_DURATION);
  }

  int getMaximumWidth() {
    return getInt(MAXIMUM_WIDTH);
  }

  int getMaximumHeight() {
    return getInt(MAXIMUM_HEIGHT);
  }

  void addDrawPageChangeListener( final PropertyChangeListener pcl ) {
    final String property = OptionItem.PROPERTY_VALUE;
    getItem(DRAW_PAGE).addPropertyChangeListener(property, pcl);
    getItem(MAXIMUM_WIDTH).addPropertyChangeListener(property, pcl);
    getItem(MAXIMUM_HEIGHT).addPropertyChangeListener(property, pcl);
    getItem(SINGLE_PAGE_LAYOUT).addPropertyChangeListener(property, pcl);
  }


  /**
   * Encapsulates a set of sample options for
   * {@link MultiPageLayoutOptionHandler}.
   */
  static final class OptionSet {
    private final String name;
    private String layoutStyle;
    private int maximumWidth;
    private int maximumHeight;

    private OptionSet( final String name ) {
      this.name = name;
      layoutStyle = MultiPageLayoutOptionHandler.LAYOUT_HIERARCHIC;
      maximumWidth = 1000;
      maximumHeight = 1000;
    }

    private void setLayoutStyle( final String layoutStyle ) {
      this.layoutStyle = layoutStyle;
    }

    private void setMaximumHeight( final int maximumHeight ) {
      this.maximumHeight = maximumHeight;
    }

    private void setMaximumWidth( final int maximumWidth ) {
      this.maximumWidth = maximumWidth;
    }

    void apply( final MultiPageLayoutOptionHandler oh ) {
      oh.set(MultiPageLayoutOptionHandler.LAYOUT, layoutStyle);
      oh.set(MultiPageLayoutOptionHandler.SINGLE_PAGE_LAYOUT, Boolean.FALSE);
      oh.set(MultiPageLayoutOptionHandler.GROUPING, MultiPageLayoutOptionHandler.GROUPING_INCLUDE_CONNECTORS);
      oh.set(MultiPageLayoutOptionHandler.MAXIMUM_WIDTH, new Integer(maximumWidth));
      oh.set(MultiPageLayoutOptionHandler.MAXIMUM_HEIGHT, new Integer(maximumHeight));
      oh.set(MultiPageLayoutOptionHandler.MAXIMAL_DURATION, new Integer(30));
      oh.set(MultiPageLayoutOptionHandler.DRAW_PAGE, Boolean.TRUE);
      oh.set(MultiPageLayoutOptionHandler.SEPARATE_TYPES, Boolean.FALSE);
      oh.set(MultiPageLayoutOptionHandler.SEPARATE_DIRECTION, Boolean.FALSE);
      oh.set(MultiPageLayoutOptionHandler.SEPARATE_MULTIEDGES, Boolean.FALSE);
    }

    String getName() {
      return name;
    }
  }
}
