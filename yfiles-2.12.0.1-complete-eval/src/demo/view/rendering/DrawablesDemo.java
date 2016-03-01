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
import demo.view.DemoDefaults;

import y.view.Drawable;
import y.view.Graph2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * This demo shows how to add objects of type {@link Drawable} to 
 * a {@link y.view.Graph2DView} and how to implement such a
 * <code>Drawable</code> object.
 * <code>Drawable</code>s represent graphical objects that can be displayed 
 * by a <code>Graph2DView</code>. The main purpose of <code>Drawable</code>s is
 * to highlight certain regions of the displayed graph.
 * <br>
 * The <code>Drawable</code> implemented in this demo draws itself as a box 
 * drawn underneath the displayed graph. The size and location of the box changes 
 * dynamically as the bounding box of the graph changes.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/mvc_view.html#drawables">Section View Implementations</a> in the yFiles for Java Developer's Guide
 */
public class DrawablesDemo extends DemoBase {
  public DrawablesDemo() {
    //add the drawable
    view.addBackgroundDrawable( new BoundingBox() );
    
    createInitialGraph();
    view.fitContent();
  }

  protected void createInitialGraph() {
    Graph2D graph = view.getGraph2D();
    graph.createEdge(graph.createNode(100,100,"1"), graph.createEdge(graph.createNode(100,200,"2"), graph.createNode(200,100,"3")).source());
    
    
  }

  /**
   * Represents a graphical bounding box of the displayed graph.
   */
  class BoundingBox implements Drawable {
    public Rectangle getBounds() {
      Rectangle r = view.getGraph2D().getBoundingBox();
      if ( r.getWidth() > 0.0 ) {
        r.setFrame( r.getX() - 30, r.getY() - 30, r.getWidth() + 60, r.getHeight() + 60 );
      }
      return r;
    }

    public void paint( Graphics2D gfx ) {
      Rectangle r = getBounds();
      gfx.setColor( DemoDefaults.DEFAULT_CONTRAST_COLOR );
      gfx.fill( r );
      gfx.setColor( Color.black );
      gfx.draw( r );
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new DrawablesDemo()).start("Drawables Demo");
      }
    });
  }


}

    

      
