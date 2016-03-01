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
package demo.view.rendering;

import demo.view.DemoBase;
import y.option.EnumOptionItem;
import y.option.MappedListCellRenderer;
import y.option.OptionHandler;
import y.view.DefaultBackgroundRenderer;
import y.view.EdgeRealizer;
import y.view.LineType;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Demonstrates different modes for drawing images in the
 * background of Graph2DView.
 * <p>
 * <b>Usage:</b> Create some nodes and try the different background settings available
 * via the toolbar button. Try changing the window size, zooming in and out and
 * moving the view port by right dragging.
 * </p><p>
 * <b>Third Party Licenses:</b><br/>
 * The USA map that is used as background in this demo is based on
 * <a href="http://commons.wikimedia.org/wiki/File:Blank_US_Map.svg">Blank_US_Map.svg by Theshibboleth</a>
 * and licensed under the
 * <a href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-ShareAlike 3.0 Unported</a>
 * license.
 * </p>
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_view.html#render_order">Section View Implementations</a> in the yFiles for Java Developer's Guide
 */
public class BackgroundDemo extends DemoBase
{
  DefaultBackgroundRenderer renderer;

  static final String[] bgImages = {
    "resource/yWorksBig.png",
    "resource/yWorksSmall.gif",
    "resource/usamap.png",
    "resource/ySplash.jpg",
    "resource/tile.png",
    "<NONE>"
  };


  public BackgroundDemo()
  {
    renderer = new DefaultBackgroundRenderer(view);
    renderer.setImageResource(getSharedResource(bgImages[0]));
    renderer.setMode(DefaultBackgroundRenderer.CENTERED);
    renderer.setColor(Color.white);
    view.setBackgroundRenderer(renderer);
    view.setPreferredSize(new Dimension(600,400));

    view.setWorldRect(0,0,1000,1000);

    //use thicker edges 
    EdgeRealizer er = view.getGraph2D().getDefaultEdgeRealizer();
    er.setLineType(LineType.LINE_2);
  }

  /**
   * Returns ViewActionDemo toolbar plus a button to change the 
   * background of the view.
   */
  protected JToolBar createToolBar()
  {
    JToolBar bar = super.createToolBar();
    bar.addSeparator();
    bar.add(createActionControl(new ChangeBackground()));
    return bar;
  }

  /**
   * An action that displays a dialog that allows to change the background
   * properties of the view.
   */
  class ChangeBackground extends AbstractAction
  {
    /** The powerful yFiles dialog generator */
    OptionHandler op;
    Map xlate;

    ChangeBackground()
    {
      super("Background");
      xlate = new HashMap(11);
      xlate.put(new Byte(DefaultBackgroundRenderer.FULLSCREEN),"Fullscreen");
      xlate.put(new Byte(DefaultBackgroundRenderer.TILED),     "Tiled");
      xlate.put(new Byte(DefaultBackgroundRenderer.BRICKED),   "Bricked");
      xlate.put(new Byte(DefaultBackgroundRenderer.CENTERED),  "Centered");
      xlate.put(new Byte(DefaultBackgroundRenderer.PLAIN),     "Plain");
      xlate.put(new Byte(DefaultBackgroundRenderer.DYNAMIC),   "Dynamic");
      
      putValue(Action.SMALL_ICON, getIconResource("resource/properties.png"));
    }

    public void actionPerformed(ActionEvent e)
    {
      if(op == null)
      {
        op = new OptionHandler("Background");
        op.addEnum("Mode",
                   xlate.keySet().toArray(),
                   new Byte(renderer.getMode()),
                   new MappedListCellRenderer(xlate));
        op.addColor("Color",renderer.getColor());
        op.addEnum("Image",bgImages,0)
          // disable unwanted I18N
          .setAttribute(EnumOptionItem.ATTRIBUTE_RENDERER,
                        new DefaultListCellRenderer());
      }

      final ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          renderer.setMode(((Byte)op.get("Mode")).byteValue());
          renderer.setColor((Color)op.get("Color"));
          String imageSrc = op.getString("Image");
          if("<NONE>".equals(imageSrc)) {
            renderer.setImage(null);
          } else {
            renderer.setImageResource(getSharedResource(imageSrc));
          }
          view.updateView();
        }
      };

      OptionSupport.showDialog(op, actionListener, true, view.getFrame());
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new BackgroundDemo()).start("Background Demo");
      }
    });
  }
}


      
