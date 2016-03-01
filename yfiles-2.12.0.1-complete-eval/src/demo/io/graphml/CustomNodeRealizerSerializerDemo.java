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
package demo.io.graphml;

import y.io.GraphMLIOHandler;
import y.view.PolyLineEdgeRealizer;
import y.view.Arrow;

import java.awt.EventQueue;
import java.util.Locale;

/**
 * A simple customization of {@link demo.io.graphml.GraphMLDemo} that uses objects of type
 * {@link demo.io.graphml.CustomNodeRealizer} as the graph's default node realizer.
 * To enable encoding and parsing of this node realizer type a specific serializer 
 * implementation is registered with GraphMLIOHandler. 
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/graphml.html#graphml_custom_realizer_serializer">Section Support for Custom Realizer</a> in the yFiles for Java Developer's Guide
*/
public class CustomNodeRealizerSerializerDemo extends GraphMLDemo {
  /**
   * Creates a new instance of CustomNodeRealizerSerializerDemo.
   */
  public CustomNodeRealizerSerializerDemo() {
    // Use another default node realizer (the one used in the example graph).
    view.getGraph2D().setDefaultNodeRealizer(new CustomNodeRealizer());

    // Use a default edge realizer as in the example graph.
    PolyLineEdgeRealizer edgeRealizer = new PolyLineEdgeRealizer();
    edgeRealizer.setTargetArrow(Arrow.NONE);
    view.getGraph2D().setDefaultEdgeRealizer(edgeRealizer);
  }


  protected void loadInitialGraph() {
    // Load example graph.
    loadGraph("resources/custom/custom-noderealizer-serializer.graphml");
  }

  protected String[] getExampleResources() {
    return null;
  }

  protected GraphMLIOHandler createGraphMLIOHandler() {
    GraphMLIOHandler ioHandler = new GraphMLIOHandler();
    // Register the node realizer's specific serializer that knows how to encode 
    // valid XML markup and also how to parse the encoded data.
    ioHandler.addNodeRealizerSerializer(new CustomNodeRealizerSerializer());
    return ioHandler;
  }

  /**
   * Launches this demo. 
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new CustomNodeRealizerSerializerDemo().start();
      }
    });
  }
}
