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
package demo.view.advanced.ports;

import y.geom.YPoint;
import y.io.graphml.NamespaceConstants;
import y.io.graphml.input.DeserializationEvent;
import y.io.graphml.input.DeserializationHandler;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.SerializationEvent;
import y.io.graphml.output.SerializationHandler;
import y.io.graphml.output.XmlWriter;
import y.view.NodePort;
import y.view.NodeRealizer;
import y.view.PortLocationModel;
import y.view.PortLocationModelParameter;

import java.util.StringTokenizer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A {@link y.view.PortLocationModel} for node ports whose location is
 * restricted to one or more sides of the associated node's visual bounds
 * (as represented by the associated {@link y.view.NodeRealizer}).
 * Internally, the location is stored as the ratio by which the width and height
 * of the realizer need to be scaled to obtain the offset to the center of the
 * node layout.
 */
public class SidePortLocationModel implements PortLocationModel {
  /**
   * Side specifier for ports that may be located along their owner node's
   * top border.
   * @see SidePortLocationModel#newInstance(int)
   * @see #getSides()
   */
  public static final byte SIDE_TOP = 1;    // 0001
  /**
   * Side specifier for ports that may be located along their owner node's
   * left border.
   * @see SidePortLocationModel#newInstance(int)
   * @see #getSides()
   */
  public static final byte SIDE_LEFT = 2;   // 0010
  /**
   * Side specifier for ports that may be located along their owner node's
   * bottom border.
   * @see SidePortLocationModel#newInstance(int)
   * @see #getSides()
   */
  public static final byte SIDE_BOTTOM = 4; // 0100
  /**
   * Side specifier for ports that may be located along their owner node's
   * right border.
   * @see SidePortLocationModel#newInstance(int)
   * @see #getSides()
   */
  public static final byte SIDE_RIGHT = 8;  // 1000

  private static final byte SIDE_ALL = SIDE_TOP|SIDE_LEFT|SIDE_BOTTOM|SIDE_RIGHT;


  private final int sides;

  private SidePortLocationModel( final int sides ) {
    this.sides = sides;
  }

  /**
   * Creates a parameter that tries to match the specified location in absolute
   * world coordinates.
   * @param owner The realizer that will own the port for which the parameter
   * has to be created.
   * @param location The location in the world coordinate system that should be
   * matched as best as possible.
   * @return a parameter that tries to match the specified location in absolute
   * world coordinates.
   * @see #getSides()
   * @see #SIDE_TOP
   * @see #SIDE_LEFT
   * @see #SIDE_BOTTOM
   * @see #SIDE_RIGHT
   */
  public PortLocationModelParameter createParameter(
          final NodeRealizer owner,
          final YPoint location
  ) {
    if (owner == null) {
      return new Parameter(this, -0.5, -0.5);
    } else {
      final double x = owner.getX();
      final double w = owner.getWidth();
      final double y = owner.getY();
      final double h = owner.getHeight();

      YPoint result = null;
      double dist = Double.POSITIVE_INFINITY;
      final byte[] s = {SIDE_TOP, SIDE_LEFT, SIDE_BOTTOM, SIDE_RIGHT};
      for (int i = 0; i < s.length; ++i) {
        if ((sides & s[i]) == s[i]) {
          final YPoint p = calculateSideLocation(x, y, w, h, location, s[i]);
          final double d = distSquared(p, location);
          if (dist > d) {
            dist = d;
            result = p;
          }
        }
      }

      // result is never null at this point
      return createParameterImpl(x, y, w, h, result);
    }
  }

  /**
   * Calculates the relative position of the given location in the
   * specified reference rectangle.
   * @param x the x-coordinate of the reference rectangle.
   * @param y the y-coordinate of the reference rectangle.
   * @param w the width of the reference rectangle.
   * @param h the height of the reference rectangle.
   * @param location the point to convert to relative coordinates
   * @return the parameter representing the relative position of the given
   * location in the specified reference rectangle.
   */
  private PortLocationModelParameter createParameterImpl(
          final double x,
          final double y,
          final double w,
          final double h,
          final YPoint location
  ) {
    final double rx;
    if (w > 0) {
      rx = (location.getX() - x - w*0.5) / w;
    } else {
      rx = 0;
    }

    final double ry;
    if (h > 0) {
      ry = (location.getY() - y - h*0.5) / h;
    } else {
      ry = 0;
    }

    return new Parameter(this, rx, ry);
  }

  /**
   * Determines the location of the port for the given parameter.
   * @param port The port to determine the location for.
   * @param parameter The parameter to use.
   * @return the calculated location of the port.
   * @throws ClassCastException if the given parameter is not of the type
   * created by this model.
   */
  public YPoint getLocation(
          final NodePort port,
          final PortLocationModelParameter parameter
  ) {
    final Parameter p = (Parameter) parameter;
    final NodeRealizer nr = port.getRealizer();
    return new YPoint(
            nr.getCenterX() + p.ratioX * nr.getWidth(),
            nr.getCenterY() + p.ratioY * nr.getHeight());
  }

  /**
   * Returns a bit mask that determines at which sides a port that is handled
   * by this model may be located.
   * @return a bit wise combination of {@link #SIDE_TOP}, {@link #SIDE_LEFT},
   * {@link #SIDE_BOTTOM}, and/or {@link #SIDE_RIGHT}.
   */
  public int getSides() {
    return sides;
  }


  /**
   * Creates a new side port location model.
   * @param sides determines at which sides a port that is handled
   * by this model may be located. Must be a bit wise combination of
   * {@link #SIDE_TOP}, {@link #SIDE_LEFT}, {@link #SIDE_BOTTOM}, and/or
   * {@link #SIDE_RIGHT}. May not be <code>0</code>.
   * @return a new side port location model.
   */
  public static SidePortLocationModel newInstance( final int sides ) {
    if ((sides & SIDE_ALL) == 0) {
      throw new IllegalArgumentException("Unsupported sides mask: " + sides);
    }

    return new SidePortLocationModel(sides);
  }

  /**
   * Calculates the projection of the given location onto one of the specified
   * rectangle's sides.
   * @param x the x-coordinate of the rectangle.
   * @param y the y-coordinate of the rectangle.
   * @param w the width of the rectangle.
   * @param h the height of the rectangle.
   * @param location the point to project onto one of the given rectangle's
   * sides.
   * @param side determines the side onto which the given location is projected.
   * Must be one of {@link #SIDE_TOP}, {@link #SIDE_LEFT}, {@link #SIDE_BOTTOM},
   * and {@link #SIDE_RIGHT}.
   * @return the projection of the given location onto one of the specified
   * rectangle's sides.
   */
  private static YPoint calculateSideLocation(
          final double x,
          final double y,
          final double w,
          final double h,
          final YPoint location,
          final byte side
  ) {
    if (side == SIDE_TOP || side == SIDE_BOTTOM) {
      double lx = location.getX();
      if (lx < x) {
        lx = x;
      } else if (lx > x + w) {
        lx = x + w;
      }
      final double ly = side == SIDE_TOP ? y : y + h;
      return new YPoint(lx, ly);
    } else { // side == SIDE_LEFT || side == SIDE_RIGHT
      final double lx = side == SIDE_LEFT ? x : x + w;
      double ly = location.getY();
      if (ly < y) {
        ly = y;
      } else if (ly > y + h) {
        ly = y + h;
      }
      return new YPoint(lx, ly);
    }
  }

  /**
   * Returns the squared distance between the two given points.
   * @param p1 the first point.
   * @param p2 the second point.
   * @return the squared distance between the two given points.
   */
  private static double distSquared( final YPoint p1, final YPoint p2 ) {
    final double dx = p1.getX() - p2.getX();
    final double dy = p1.getY() - p2.getY();
    return dx*dx + dy*dy;
  }


  /**
   * Stores the port location relative to the owner node bounds.
   * A relative location of <code>(0.0, 0.0)</code> means the node's center.
   * A relative location of <code>(-0.5, -0.5)</code> means the node's
   * top left corner.
   * A relative location of <code>(0.5, 0.5)</code> means the node's bottom
   * right corner.
   */
  static final class Parameter implements PortLocationModelParameter {
    /** The parameter's associated model. */
    private final SidePortLocationModel model;
    /** Relative x-coordinate of the port's location. */
    private final double ratioX;
    /** Relative y-coordinate of the port's location. */
    private final double ratioY;

    /**
     * Initializes a new parameter.
     * @param model the parameter's associated model.
     * @param ratioX relative x-coordinate of the port's location.
     * @param ratioY relative y-coordinate of the port's location.
     */
    Parameter(
            final SidePortLocationModel model,
            final double ratioX,
            final double ratioY
    ) {
      this.model = model;
      this.ratioX = ratioX;
      this.ratioY = ratioY;
    }

    public PortLocationModel getModel() {
      return model;
    }
  }


  /**
   * Provides GraphML (de-)serialization support for
   * {@link SidePortLocationModel} and its parameters.
   */
  public static final class Handler implements SerializationHandler, DeserializationHandler {
    private static final String NS_NAME = "demo";
    private static final String MODEL_NODE_NAME = "SidePortLocationModel";


    /**
     * Writes {@link SidePortLocationModel} models and parameters.
     * @param event contains all data that is needed for serialization.
     */
    public void onHandleSerialization(
            final SerializationEvent event
    ) throws GraphMLWriteException {
      final Object item = event.getItem();
      if (item instanceof Parameter) {
        final Parameter param = (Parameter) item;
        final XmlWriter writer = event.getWriter();
        writer.writeStartElement(MODEL_NODE_NAME, NS_NAME);
        writer.writeAttribute("sides", sidesToString(param));
        writer.writeAttribute("ratioX", param.ratioX);
        writer.writeAttribute("ratioY", param.ratioY);
        writer.writeEndElement();
        event.setHandled(true);
      }
    }

    private static String sidesToString( final Parameter p ) {
      final int sides = ((SidePortLocationModel) p.getModel()).getSides();
      final StringBuffer sb = new StringBuffer();
      String del = "";
      if ((sides & SIDE_TOP) == SIDE_TOP) {
        sb.append(del).append("SIDE_TOP");
        del = "|";
      }
      if ((sides & SIDE_LEFT) == SIDE_LEFT) {
        sb.append(del).append("SIDE_LEFT");
        del = "|";
      }
      if ((sides & SIDE_BOTTOM) == SIDE_BOTTOM) {
        sb.append(del).append("SIDE_BOTTOM");
        del = "|";
      }
      if ((sides & SIDE_RIGHT) == SIDE_RIGHT) {
        sb.append(del).append("SIDE_RIGHT");
        del = "|";
      }
      return sb.toString();
    }

    /**
     * Reads {@link SidePortLocationModel} models and parameters.
     * @param event contains all data that is needed for deserialization.
     * @throws GraphMLParseException if required attributes are missing or
     * invalid.
     */
    public void onHandleDeserialization(
            final DeserializationEvent event
    ) throws GraphMLParseException {
      final Node node = event.getXmlNode();
      if (isNamespaceElement(node, NamespaceConstants.YFILES_JAVA_NS)) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
          if (isNamespaceElement(child, NS_NAME) &&
              MODEL_NODE_NAME.equals(child.getLocalName())) {
            final NamedNodeMap attrs = child.getAttributes();

            double ratioX = 0;
            final Node rxAttr = attrs.getNamedItem("ratioX");
            if (rxAttr == null) {
              throw new GraphMLParseException(
                      "Missing attribute ratioX for element " +
                      MODEL_NODE_NAME + ".");
            } else {
              ratioX = Double.parseDouble(rxAttr.getNodeValue());
            }
            double ratioY = 0;
            final Node ryAttr = attrs.getNamedItem("ratioY");
            if (ryAttr == null) {
              throw new GraphMLParseException(
                      "Missing attribute ratioY for element " +
                      MODEL_NODE_NAME + ".");
            } else {
              ratioY = Double.parseDouble(ryAttr.getNodeValue());
            }

            int sides = 0;
            final Node sAttr = attrs.getNamedItem("sides");
            if (sAttr == null) {
              throw new GraphMLParseException(
                      "Missing attribute sides for element " +
                      MODEL_NODE_NAME + ".");
            } else {
              sides = stringToSides(sAttr.getNodeValue().toUpperCase());
              if ((sides & SIDE_ALL) == 0) {
                throw new GraphMLParseException("Unsupported sides mask: " + sides);
              }
            }

            event.setResult(new Parameter(new SidePortLocationModel(sides), ratioX, ratioY));
            break;
          }
        }
      }
    }

    private static int stringToSides( final String value ) {
      int sides = 0;
      for (StringTokenizer st = new StringTokenizer(value, "|"); st.hasMoreTokens();) {
        final String token = st.nextToken().trim();
        if ("SIDE_TOP".equals(token)) {
          sides |= SIDE_TOP;
        } else if ("SIDE_LEFT".equals(token)) {
          sides |= SIDE_LEFT;
        } else if ("SIDE_BOTTOM".equals(token)) {
          sides |= SIDE_BOTTOM;
        } else if ("SIDE_RIGHT".equals(token)) {
          sides |= SIDE_RIGHT;
        } else {
          throw new IllegalArgumentException("Unsupported side value: " + token);
        }
      }
      return sides;
    }

    private static boolean isNamespaceElement( final Node node, final String ns ) {
      return node.getNodeType() == Node.ELEMENT_NODE &&
             ns.equals(node.getNamespaceURI());
    }
  }
}
