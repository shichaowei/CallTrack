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
package demo.layout.router;

import y.layout.router.BusRouter;
import demo.layout.module.BusRouterModule;
import y.option.IntOptionItem;
import y.option.OptionGroup;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.option.ResourceBundleGuiFactory;

import java.util.MissingResourceException;

/**
 * A modified {@link y.module.BusRouterModule} which omits the scope and the bus definition option which are not
 * applicable to {@link demo.layout.router.BusRouterDemo}.
 */
class BusRouterDemoModule extends BusRouterModule {

  private static final String GROUP_LAYOUT = "GROUP_LAYOUT";
  private static final String MIN_DISTANCE_TO_NODES = "MIN_DISTANCE_TO_NODES";
  private static final String MIN_DISTANCE_TO_EDGES = "MIN_DISTANCE_TO_EDGES";
  private byte scope;
  private boolean gridRoutingEnabled;
  private int gridSpacing;

  /**
   * Creates a new instance.
   */
  BusRouterDemoModule() {
    optionsLayout = false;
    optionsSelection = true;
    optionsRouting = true;
  }

  /**
   * Adds the option items used by this module to the given <code>OptionHandler</code>.
   * @param defaults a <code>BusRouter</code> instance that provides default option values.
   * @param options the <code>OptionHandler</code> to add the items to
   */
  protected void addOptionItems(final BusRouter defaults, final OptionHandler options) {
    OptionItem item;
    OptionGroup og;

    item = options.addInt(MIN_DISTANCE_TO_NODES, defaults.getMinimumDistanceToNode());
    item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));
    item = options.addInt(MIN_DISTANCE_TO_EDGES, defaults.getMinimumDistanceToEdge());
    item.setAttribute(IntOptionItem.ATTRIBUTE_MIN_VALUE, new Integer(1));

    og = new OptionGroup();
    og.setAttribute(OptionGroup.ATTRIBUTE_TITLE, GROUP_LAYOUT);
    og.addItem(options.getItem(MIN_DISTANCE_TO_EDGES));
    og.addItem(options.getItem(MIN_DISTANCE_TO_NODES));

    defaults.setPreferredBackboneSegmentCount(1);
    super.addOptionItems(defaults, options);
  }

  /**
   * Registers the demo's resource bundle for localization of the module's 
   * user interface.
   */
  protected void initGuiFactory( final OptionHandler optionHandler ) {
    ResourceBundleGuiFactory gf = new ResourceBundleGuiFactory();
    try {
      gf.addBundle(BusRouterModule.class.getName());
      gf.addBundle(BusRouterDemo.class.getName());
    } catch (MissingResourceException mre) {
      // nothing to do here
    }
    optionHandler.setGuiFactory(gf);
  }

  /**
   * Configures an instance of {@link y.layout.router.BusRouter}. The values provided by this module's option handler
   * are being used for this purpose.
   *
   * @param bus the BusRouter to be configured.
   */
  protected void configure(final BusRouter bus, final OptionHandler options) {
    super.configure(bus, options);

    bus.setMinimumDistanceToNode(options.getInt(MIN_DISTANCE_TO_NODES));
    bus.setMinimumDistanceToEdge(options.getInt(MIN_DISTANCE_TO_EDGES));

    bus.setScope(scope);
    bus.setGridRoutingEnabled(gridRoutingEnabled);
    bus.setGridSpacing(gridSpacing);
  }


  void setScope(final byte scope) {
    this.scope = scope;
  }

  void setGridRoutingEnabled(final boolean gridRoutingEnabled) {
    this.gridRoutingEnabled = gridRoutingEnabled;
  }

  void setGridSpacing(final int gridSpacing) {
    this.gridSpacing = gridSpacing;
  }
}
