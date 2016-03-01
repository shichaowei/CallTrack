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

import demo.layout.withoutview.DiagonalLayouter;
import y.module.LayoutModule;
import y.option.OptionHandler;

/**
 * This module represents an interactive configurator and launcher for the demo
 * Layouter {@link demo.layout.withoutview.DiagonalLayouter}.
 * <br>
 * Additionally, this class can be executed separately. In this case it shows off
 * the internationalization and serialization features of the
 * {@link y.option.OptionHandler} class.
 * By launching the module class using a two letter language constant as an
 * argument, the dialog will be internationalized in that language if the
 * corresponding localized properties file is available. Try either 'en' for
 * English or 'de' for German.
 *
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/option_basic.html#i18n_l10n">Section Internationalization and Localization</a> in the yFiles for Java Developer's Guide
 */
public class DiagonalLayoutModule extends LayoutModule
{
  //// Module 'Diagonal Layout'
  protected static final String MODULE_DIAGONAL = "DIAGONAL";
  
  //// Section 'default' items
  protected static final String ITEM_MINIMAL_NODE_DISTANCE = "MINIMAL_NODE_DISTANCE";

  /**
   * Creates an instance of this module.
   */
  public DiagonalLayoutModule() {
    super(MODULE_DIAGONAL);
  }
  
  /**
   * Creates an OptionHandler and adds the option items used by this module.
   * @return the created <code>OptionHandler</code> providing module related options
   */
  protected OptionHandler createOptionHandler() {
    final OptionHandler options = new OptionHandler(getModuleName());
    // Defaults provider
    final DiagonalLayouter defaults = new DiagonalLayouter();
    
    // Populate default section
    options.addDouble(ITEM_MINIMAL_NODE_DISTANCE, defaults.getMinimalNodeDistance());
    
    return options;
  }
  
  /**
   * Main module execution routine.
   * Launches the module's underlying algorithm on the module's graph based on user options.
   */
  protected void mainrun() {
    final DiagonalLayouter diagonal = new DiagonalLayouter();
    
    final OptionHandler options = getOptionHandler();
    configure(diagonal, options);
    
    launchLayouter(diagonal);
  }

  /**
   * Configures the module's layout algorithm according to the given options.
   * @param diagonal the <code>DiagonalLayouter</code> to be configured
   * @param options the layout options to set
   */
  protected void configure(final DiagonalLayouter diagonal, final OptionHandler options) {
    diagonal.setMinimalNodeDistance(options.getDouble(ITEM_MINIMAL_NODE_DISTANCE));
  }
}

