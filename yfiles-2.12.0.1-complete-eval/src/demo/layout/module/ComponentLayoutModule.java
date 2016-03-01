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

import y.geom.YDimension;
import y.layout.ComponentLayouter;
import y.option.ConstraintManager;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.view.Graph2DView;

/**
 * This module represents an interactive configurator and launcher for
 * {@link y.layout.ComponentLayouter}.
 */
public class ComponentLayoutModule extends LayoutModule {
  //// Module 'Component Layout'
  protected static final String MODULE_COMPONENTLAYOUTER = "COMPONENTLAYOUTER";
  
  //// Section 'default' items
  protected static final String ITEM_STYLE = "STYLE";
  protected static final String VALUE_STYLE_NONE = "STYLE_NONE";
  protected static final String VALUE_STYLE_ROWS = "STYLE_ROWS";
  protected static final String VALUE_STYLE_SINGLE_ROW = "STYLE_SINGLE_ROW";
  protected static final String VALUE_STYLE_SINGLE_COLUMN = "STYLE_SINGLE_COLUMN";
  protected static final String VALUE_STYLE_PACKED_RECTANGLE = "STYLE_PACKED_RECTANGLE";
  protected static final String VALUE_STYLE_PACKED_COMPACT_RECTANGLE = "STYLE_PACKED_COMPACT_RECTANGLE";
  protected static final String VALUE_STYLE_PACKED_CIRCLE = "STYLE_PACKED_CIRCLE";
  protected static final String VALUE_STYLE_PACKED_COMPACT_CIRCLE = "STYLE_PACKED_COMPACT_CIRCLE";
  protected static final String VALUE_STYLE_MULTI_ROWS = "STYLE_MULTI_ROWS";
  protected static final String VALUE_STYLE_MULTI_ROWS_COMPACT = "STYLE_MULTI_ROWS_COMPACT";
  protected static final String VALUE_STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED = "STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED";
  protected static final String VALUE_STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED_COMPACT = "STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED_COMPACT";
  protected static final String VALUE_STYLE_MULTI_ROWS_WIDTH_CONSTRAINED = "STYLE_MULTI_ROWS_WIDTH_CONSTRAINED";
  protected static final String VALUE_STYLE_MULTI_ROWS_WIDTH_CONSTRAINED_COMPACT = "STYLE_MULTI_ROWS_WIDTH_CONSTRAINED_COMPACT";
  protected static final String ITEM_NO_OVERLAP = "NO_OVERLAP";
  protected static final String ITEM_FROM_SKETCH = "FROM_SKETCH";
  protected static final String ITEM_USE_SCREEN_RATIO = "USE_SCREEN_RATIO";
  protected static final String ITEM_ASPECT_RATIO = "ASPECT_RATIO";
  protected static final String ITEM_COMPONENT_SPACING = "COMPONENT_SPACING";
  protected static final String ITEM_GRID_ENABLED = "GRID_ENABLED";
  protected static final String ITEM_GRID_SPACING = "GRID_SPACING";

  /**
   * Creates an instance of this module.
   */
  public ComponentLayoutModule() {
    super(MODULE_COMPONENTLAYOUTER);
  }

  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    final ConstraintManager optionConstraints = new ConstraintManager(options);
    // Defaults provider
    final ComponentLayouter defaults = new ComponentLayouter();

    // Populate default section
    options.addEnum(ITEM_STYLE, new String[]{
        VALUE_STYLE_NONE,
        VALUE_STYLE_ROWS,
        VALUE_STYLE_SINGLE_ROW,
        VALUE_STYLE_SINGLE_COLUMN,
        VALUE_STYLE_PACKED_RECTANGLE,
        VALUE_STYLE_PACKED_COMPACT_RECTANGLE,
        VALUE_STYLE_PACKED_CIRCLE,
        VALUE_STYLE_PACKED_COMPACT_CIRCLE,
        VALUE_STYLE_MULTI_ROWS,
        VALUE_STYLE_MULTI_ROWS_COMPACT,
        VALUE_STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED,
        VALUE_STYLE_MULTI_ROWS_HEIGHT_CONSTRAINED_COMPACT,
        VALUE_STYLE_MULTI_ROWS_WIDTH_CONSTRAINED,
        VALUE_STYLE_MULTI_ROWS_WIDTH_CONSTRAINED_COMPACT,
    }, defaults.getStyle() & ComponentLayouter.STYLE_MASK);
    options.addBool(ITEM_NO_OVERLAP, (defaults.getStyle() & ComponentLayouter.STYLE_MODIFIER_NO_OVERLAP) != 0);
    options.addBool(ITEM_FROM_SKETCH, (defaults.getStyle() & ComponentLayouter.STYLE_MODIFIER_AS_IS) != 0);
    final OptionItem itemUseScreenRatio = options.addBool(ITEM_USE_SCREEN_RATIO, true);
    final YDimension size = defaults.getPreferredLayoutSize();
    final OptionItem itemAspectRatio = options.addDouble(ITEM_ASPECT_RATIO, size.width / size.height);
    options.addDouble(ITEM_COMPONENT_SPACING, defaults.getComponentSpacing(), 0.0d, 400.0d);
    final OptionItem itemGridEnabled = options.addBool(ITEM_GRID_ENABLED, defaults.getGridSpacing() > 0);
    final OptionItem itemGridSpacing =
        options.addDouble(ITEM_GRID_SPACING, defaults.getGridSpacing() > 0 ? defaults.getGridSpacing() : 20.0d);
    // Enable/disable items depending on specific values
    optionConstraints.setEnabledOnValueEquals(itemUseScreenRatio, Boolean.FALSE, itemAspectRatio);
    optionConstraints.setEnabledOnValueEquals(itemGridEnabled, Boolean.TRUE, itemGridSpacing);
    
    return options;
  }

  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    ComponentLayouter component = new ComponentLayouter();

    final OptionHandler options = getOptionHandler();
    configure(component, options);

    launchLayouter(component);
  }
  
  /**
   * Configures the module's layout algorithm according to the given options.
   * <p>
   * Important: This method does also depend on the <code>Graph2DView</code>
   * of this module in addition to the method's parameters.
   * </p>
   * @param component the <code>ComponentLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final ComponentLayouter component, final OptionHandler options) {
    component.setComponentArrangementEnabled(true);
    
    byte style = (byte) options.getEnum(ITEM_STYLE);
    if (options.getBool(ITEM_NO_OVERLAP)) {
      style |= ComponentLayouter.STYLE_MODIFIER_NO_OVERLAP;
    }
    if (options.getBool(ITEM_FROM_SKETCH)) {
      style |= ComponentLayouter.STYLE_MODIFIER_AS_IS;
    }
    component.setStyle(style);
    
    double w, h;
    final Graph2DView view = getGraph2DView();
    if (options.getBool(ITEM_USE_SCREEN_RATIO) && view != null) {
      w = view.getWidth();
      h = view.getHeight();
    } else {
      w = options.getDouble(ITEM_ASPECT_RATIO);
      h = 1.0d/w;
      w *= 400.d;
      h *= 400.d;
    }
    component.setPreferredLayoutSize(w, h);
    
    component.setComponentSpacing(options.getDouble(ITEM_COMPONENT_SPACING));
    component.setGridSpacing(options.getBool(ITEM_GRID_ENABLED) ? options.getDouble(ITEM_GRID_SPACING) : 0);
  }
}
