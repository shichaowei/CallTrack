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
package demo.view.realizer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import demo.view.DemoDefaults;
import y.util.D;
import y.util.YVersion;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.SmartNodeLabelModel;
import y.view.YLabel;

/**
 * NodeRealizer implementation that represents a UML Class Node.
 * This node realizer displays the following properties of a class
 * in UML notation:
 * <ul>
 *  <li>class name</li>
 *  <li>stereotype property</li>
 *  <li>constraint property</li>
 *  <li>attribute list</li>
 *  <li>method list</li>
 * </ul>
 * Executing this class will display a sample instance of this realizer.
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizers.html#cls_ShapeNodeRealizer">Section Bringing Graph Elements to Life: The Realizer Concept</a> in the yFiles for Java Developer's Guide
 */
public class UMLClassNodeRealizer extends ShapeNodeRealizer
{
  private NodeLabel aLabel; //attributeLabel
  private NodeLabel mLabel; //methodLabel
  private boolean clipContent;
  private boolean omitDetails;
  private String stereotype = "";
  private String constraint = "";

  /**
   * Instantiates a new UMLNodeRealizer.
   */ 
  public UMLClassNodeRealizer()
  {
    init();
  }
  
  void init()
  {
    setShapeType(RECT_3D);

    SmartNodeLabelModel model = new SmartNodeLabelModel();
    getLabel().setLabelModel(model,
        model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_TOP));
    
    getLabel().setFontSize(13);
    getLabel().setFontStyle(Font.BOLD);
        
    aLabel = new NodeLabel();
    aLabel.bindRealizer(this);
    aLabel.setAlignment(YLabel.ALIGN_LEFT);
    aLabel.setLabelModel(model, model.getDefaultParameter());
    
    mLabel = new NodeLabel();
    mLabel.bindRealizer(this);
    mLabel.setAlignment(YLabel.ALIGN_LEFT);
    mLabel.setLabelModel(model, model.getDefaultParameter());
    
    clipContent = true;
    omitDetails = false;
  }
  
  /**
   * Instantiates a new UMLNodeRealizer as a copy of a given
   * realizer.
   */ 
  public UMLClassNodeRealizer(NodeRealizer r)
  {
    super(r);
    if(r instanceof UMLClassNodeRealizer)
    {
      UMLClassNodeRealizer cnr = (UMLClassNodeRealizer)r;
      aLabel = (NodeLabel)cnr.aLabel.clone();
      aLabel.bindRealizer(this);
      mLabel = (NodeLabel)cnr.mLabel.clone();
      mLabel.bindRealizer(this);
      constraint = cnr.constraint;
      stereotype = cnr.stereotype;
      clipContent = cnr.clipContent;
      omitDetails = cnr.omitDetails;  
    }
    else {
      init();
    }
  }
  
  /**
   * Returns a UMLNodERealizer that is a copy of the given 
   * realizer.
   */ 
  public NodeRealizer createCopy(NodeRealizer r)
  {
    return new UMLClassNodeRealizer(r);
  }
    
  
  //////////////////////////////////////////////////////////////////////////////
  // SETTER & GETTER ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  
  /**
   * Set the class name to be displayed by this realizer.
   */
  public void setClassName(String name)
  {
    setLabelText(name);
  }
  
  /**
   * Returns the class name to be displayed by this realizer.
   */
  public String getClassName()
  {
    return getLabelText();
  }
  
  
  /**
   * Sets the constraint property of this realizer.
   */
  public void setConstraint(String constraint)
  {
    this.constraint = constraint;
  }

  
  /**
   * Sets the stereotype property of this realizer.
   */
  public void setStereotype(String stereotype)
  {
    this.stereotype = stereotype;
  }
  
  
  /**
   * Returns the constraint property of this realizer.
   */
  public String getConstraint()
  {
    return constraint;
  }

  /**
   * Returns the stereotype property of this realizer.
   */
  public String getStereotype()
  {
    return stereotype;
  }
  
  
  /**
   * Returns the node label that represents all added
   * method strings.
   */
  public NodeLabel getMethodLabel()
  {
    return mLabel;
  }
  
  /**
   * Returns the node label that represents all added
   * attribute strings.
   */
  public NodeLabel getAttributeLabel()
  {
    return aLabel;
  }
  
  /**
   * Returns whether or not the display of the labels should be
   * clipped with the bounding box of the realizer.
   */
  public boolean getClipContent()
  {
    return clipContent;
  }
 
  /**
   * Sets whether or not the display of the labels should be
   * clipped with the bounding box of the realizer.
   */
  public void setClipContent(boolean clipping)
  {
    clipContent = clipping;
  }
  
 
  /**
   * Set whether or not this realizer should omit details when being displayed.
   */
  public void setOmitDetails(boolean b)
  {
    omitDetails = b;
  }
  
  
  /**
   * Returns whether or not this realizer should omit details when being displayed.
   */ 
  public boolean getOmitDetails()
  {
    return omitDetails;
  }
  
  private void addToLabel(NodeLabel l, String s)
  {
    if(l.getText().length() > 0) {
      l.setText(l.getText() + '\n' + s);
    } else {
      l.setText(s);
    }
  }
  
  
  /**
   * Adds a class method label to this realizer.
   */ 
  public void addMethod(String method)
  {
    addToLabel(mLabel,method);
  }
  
  /**
   * Adds a class attribute label to this realizer.
   */ 
  public void addAttribute(String attr)
  {
    addToLabel(aLabel,attr);
  }
  
  /**
   * Set the size of this realizer automatically. This method will adapt the size
   * of this realizer so that the labels defined for it will fit within its
   * bounding box.
   */
  public void fitContent()
  {
    double height = 3.0;
    double width = getLabel().getWidth() + 10.0;
    
    if(stereotype.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("<<" + getStereotype() + ">>");
      height += l.getHeight() + 5.0;
      width = Math.max(l.getWidth()+ 10.0, width);
    }
    
    height += getLabel().getHeight() + 3.0;

    if(constraint.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("{" + getConstraint() + "}");
      height += l.getHeight() + 5.0;
      width = Math.max(l.getWidth() + 10.0, width);
    }
    
    if(!omitDetails && !(aLabel.getText().length() == 0 && mLabel.getText().length() == 0))
    {
      height += 3.0;
      height += aLabel.getHeight() + 3.0;
      width = Math.max(aLabel.getWidth() + 10.0,width);
      height += 3.0;
      height += mLabel.getHeight() + 3.0;
      width = Math.max(mLabel.getWidth() + 10.0,width);
    }
    
    setSize(width, height);
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  // GRAPHICS  /////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * Paint the labels associated with this realizer.
   */
  public void paintText(Graphics2D gfx)
  {    
    Shape oldClip = null;
    if(clipContent)
    {
      oldClip = gfx.getClip();
      gfx.clip(getBoundingBox());//Rect((int)x,(int)y,(int)width,(int)height);
    }
    
    double yoff = 3.0;

    final SmartNodeLabelModel model = new SmartNodeLabelModel();

    if(stereotype.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.bindRealizer( this );
      l.setText("<<" + getStereotype() + ">>");
      // Create a specific model parameter which aligns the node's and the label's centers horizontally
      // (nodeRatioX and labelRatioX are 0) and the upper sides of node and label vertically. (nodeRatioY
      // and labelRatioY are -0.5). In y-direction an offset is added (yoff) to to move the label to a
      // position with some distance to the border of the node.
      l.setLabelModel(model, model.createSpecificModelParameter(0, -0.5, 0, -0.5, 0, yoff, 0, -1));
      l.paint(gfx);
      yoff += l.getHeight() + 5.0;
    }
    
    NodeLabel label = getLabel();
    // Create a specific model parameter which aligns the node's and the label's centers horizontally
    // (nodeRatioX and labelRatioX are 0) and the upper sides of node and label vertically. (nodeRatioY
    // and labelRatioY are -0.5). In y-direction an offset is added (yoff) to to move the label to a
    // position with some distance to the border of the node or to the previous label respectively.
    label.setModelParameter(model.createSpecificModelParameter(0, -0.5, 0, -0.5, 0, yoff, 0, -1));
    label.paint(gfx);
    yoff += label.getHeight() + 3.0;

    if(constraint.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.bindRealizer( this );
      l.setText("{" + getConstraint() + "}");
      // Create a specific model parameter which aligns the node's and the label's right sides horizontally
      // (nodeRatioX and labelRatioX are 0.5) and the upper sides of node and label vertically. (nodeRatioY
      // and labelRatioY are -0.5). In y-direction an offset is added (yoff) to to move the label to a
      // position with some distance to the border of the node or to the previous label respectively. In
      // x-direction the offset (-0.5) is set so keep a distance to the border of the node.
      l.setLabelModel(model, model.createSpecificModelParameter(0.5, -0.5, 0.5, -0.5, -5.0, yoff, 0, -1));
      l.paint(gfx);
      yoff += l.getHeight() + 5.0;
    }
    
    if(!omitDetails && !(aLabel.getText().length() == 0 && mLabel.getText().length() == 0))
    {
      gfx.setColor(getLineColor());
      gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1.0),(int)(y+yoff));
      yoff += 3.0;
      // Create a specific model parameter which aligns the node's and the label's left sides horizontally
      // (nodeRatioX and labelRatioX are -0.5) and the upper sides of node and label vertically. (nodeRatioY
      // and labelRatioY are -0.5). In y-direction an offset is added (yoff) to to move the label to a
      // position with some distance to the border of the node or to the previous label respectively. In
      // x-direction the offset (3.0) is set so keep a distance to the border of the node.
      aLabel.setModelParameter(model.createSpecificModelParameter(-0.5, -0.5, -0.5, -0.5, 3.0, yoff, 0, -1));
      aLabel.paint(gfx);
      yoff += aLabel.getHeight() + 3.0;
      gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1.0),(int)(y+yoff));
      yoff += 3.0;
      mLabel.setModelParameter(model.createSpecificModelParameter(-0.5, -0.5, -0.5, -0.5, 3.0, yoff, 0, -1));
      mLabel.paint(gfx);
    }
    
    if(clipContent)
    {
      gfx.setClip(oldClip);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // SERIALIZATION /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * Serialization routine that allows this realizer to be written out
   * in YGF graph format.
   * @deprecated Use a custom {@link y.io.graphml.graph2d.NodeRealizerSerializer}
   * for serialization to the {@link y.io.GraphMLIOHandler GraphML format}
   * instead.
   */
  public void write(ObjectOutputStream out) throws IOException 
  {
    out.writeByte(YVersion.VERSION_1);
    super.write(out);
    aLabel.write( out );
    mLabel.write( out );
    out.writeBoolean(clipContent);
    out.writeBoolean(omitDetails);
    out.writeObject(getStereotype());
    out.writeObject(getConstraint());
  }
  
  
  /**
   * Deserialization routine that allows this realizer to be read in
   * from YGF graph format.
   * @deprecated Use a custom {@link y.io.graphml.graph2d.NodeRealizerSerializer}
   * for serialization to the {@link y.io.GraphMLIOHandler GraphML format}
   * instead.
   */
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException 
  {
    switch(in.readByte()) {
    case YVersion.VERSION_1:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = in.readBoolean();
      stereotype = (String)in.readObject();
      constraint = (String)in.readObject();
      break;
    default:
      D.fatal("Unsupported Format");
    }
  }



  public static void addContentTo( final JRootPane rootPane )
  {
    final UMLClassNodeRealizer r = new UMLClassNodeRealizer();
    r.setClassName("com.mycompany.MyClass");
    r.setConstraint("abstract");
    r.setStereotype("factory");
    r.addAttribute("-graph");
    r.addAttribute("-id");
    r.addMethod("+setGraph(Graph)");
    r.addMethod("+getGraph():Graph");
    r.addMethod("+setID(int)");
    r.addMethod("+getID():int");
    r.fitContent();
    r.setFillColor(new Color(255, 153, 0));
    final Graph2DView view = new Graph2DView();
    view.setFitContentOnResize(true);
    view.getGraph2D().setDefaultNodeRealizer(r.createCopy());
    view.getGraph2D().createNode();
    view.addViewMode(new EditMode());

    rootPane.setContentPane(view);
  }

  /**
   * Launcher method. Execute this class to see a sample instantiation of
   * this node realizer in action.
   */
  public static void main(String[] args)
  {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addContentTo(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      }
    });
  }
}

