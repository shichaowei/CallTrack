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

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;

import java.awt.Color;
import java.awt.Graphics2D;

import demo.view.DemoDefaults;

/**
 * A simple customization of {@link y.view.ShapeNodeRealizer} that holds additional
 * fields.
 * GraphML serialization of this realizer and its additional fields is handled by
 * {@link CustomNodeRealizerSerializer}.
 */
public class CustomNodeRealizer extends ShapeNodeRealizer {
  // Custom value
  private int customValue;
  // Custom attribute
  private String customAttribute;

  /** Creates a new instance of CustomNodeRealizer. */
  public CustomNodeRealizer() {
    setSize(60, 40);
    setCustomAttribute("v1.0");
    setCustomValue(333);
    setFillColor(DemoDefaults.DEFAULT_NODE_COLOR);
  }

  /** Creates a new instance of CustomNodeRealizer. */
  public CustomNodeRealizer(NodeRealizer nr) {
    super(nr);
    // If the given node realizer is of this type, then apply copy semantics. 
    if (nr instanceof CustomNodeRealizer) {
      CustomNodeRealizer fnr = (CustomNodeRealizer) nr;
      // Copy the values of custom attributes. 
      setCustomValue(fnr.customValue);
      setCustomAttribute(fnr.customAttribute);
    }
  }

  public NodeRealizer createCopy(NodeRealizer nr) {
    return new CustomNodeRealizer(nr);
  }

  public void paintText(Graphics2D gfx) {
    super.paintText(gfx);
    gfx.setColor(Color.blue);
    gfx.drawString("value: " + getCustomValue(), (float) getX() + 4, (float) getY() + 12);
    gfx.drawString("attr:  " + getCustomAttribute(), (float) getX() + 4, (float) (getY() + getHeight() - 2));
  }

  public int getCustomValue() {
    return customValue;
  }

  public void setCustomValue(int customValue) {
    this.customValue = customValue;
  }

  public String getCustomAttribute() {
    return customAttribute;
  }

  public void setCustomAttribute(String customAttribute) {
    this.customAttribute = customAttribute;
  }
}
