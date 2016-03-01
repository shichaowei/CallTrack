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
package demo.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import y.base.Edge;
import y.base.Node;
import y.base.NodeList;
import y.io.GIFIOHandler;
import y.io.ImageMapOutputHandler;
import y.io.LinkInfo;
import y.io.LinkMap;
import y.view.Graph2D;
import y.view.Graph2DView;

/**
 * This class shows how to generate an image and a hyperlinked 
 * HTML image map of a graph.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/image_export.html#image_maps">Section Image Maps</a> in the yFiles for Java Developer's Guide
 */
public class ImageMapDemo
{
  
  public ImageMapDemo(String imageFileName, String htmlFileName)
  {
    Graph2D tree = new Graph2D();
    
    LinkMap linkMap = new LinkMap();
    
    buildTreeFromData(tree, linkMap);
    
    //layout as a tree. Other tree layout algorithms can be found
    //in package y.layout.tree.
    y.layout.tree.TreeLayouter tLayouter = new y.layout.tree.TreeLayouter(); 
    tLayouter.doLayout(tree); 
    
    //output to GIF. Other output handlers can be found in package y.io. 
    GIFIOHandler gifIO = new GIFIOHandler();
    Graph2DView view = gifIO.createDefaultGraph2DView(tree);
    tree.setCurrentView(view);
    
    //use ImageMapOutputHandler to generate an html image map that matches
    //the generated image.
    ImageMapOutputHandler htmlIO = new ImageMapOutputHandler();
    linkMap.setMapName("image");
    htmlIO.setReferences(linkMap);
    
    try
    {
      File file = new File(imageFileName);
      System.out.println("Writing GIF to " + file.getCanonicalPath());
      gifIO.write(tree, imageFileName);
    
      file = new File(htmlFileName);
      System.out.println("Writing HTML to " + file.getCanonicalPath());
      
      PrintWriter htmlOut = new PrintWriter(new FileWriter(htmlFileName));
      String htmlMap = htmlIO.createHTMLString(tree);
      
      //create valid html page that can be displayed in a browser.
      htmlOut.println(
          "<html>\n<head></head>\n<body>" + 
          htmlMap + "\n" +
          "<img src=" + imageFileName + " usemap=\"#image\" border=\"0\">\n" + 
          "</body></html>"); 
      htmlOut.close();
    }
    catch(IOException ioex)
    {
      ioex.printStackTrace();
    }
  }
  
  /**
   * Build a tree structure and provide link hyperlink information
   * for some nodes.
   */
  void buildTreeFromData(Graph2D graph, LinkMap linkMap)
  {
    NodeList queue = new NodeList();
    queue.add(graph.createNode(0,0, 100, 30, "Root"));
    for(int i = 0; i < 10; i++)
    {
      Node root = queue.popNode();
      LinkInfo link = new LinkInfo();
      link.setAttribute(LinkInfo.HTML_REFERENCE, "http://www.yworks.com");
      link.setAttribute(LinkInfo.HTML_ALT, "Visit yWorks");
      link.setAttribute(LinkInfo.HTML_TITLE, "Visit yWorks");
      linkMap.put(root, link);
      Node c1 = graph.createNode(0,0, 80, 30, "c1_" + graph.N());
      Edge e1 = graph.createEdge(root, c1);
      Node c2 = graph.createNode(0,0, 60, 30, "c2_" + graph.N());
      Edge e2 = graph.createEdge(root, c2);
      
      linkMap.put(e1, link);
      linkMap.put(e2, link);

      queue.add(c2);
      queue.add(c1);
    }
  }
  
  public static void main(String[] args)
  {
    ImageMapDemo demo = new ImageMapDemo("ImageMapDemo.gif","ImageMapDemo.html");
  }
 
}

    

      
