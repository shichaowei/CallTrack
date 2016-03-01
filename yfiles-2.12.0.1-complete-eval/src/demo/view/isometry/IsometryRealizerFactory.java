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
package demo.view.isometry;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.layout.PreferredPlacementDescriptor;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ProxyShapeNodeRealizer;
import y.view.SimpleUserDataHandler;
import y.view.YLabel;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.GroupFeature;
import y.view.hierarchy.HierarchyManager;
import y.view.hierarchy.ProxyAutoBoundsNodeRealizer;

import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * A factory for isometric realizers.
 */
class IsometryRealizerFactory {

  /** The name of the configuration of isometric nodes. */
  private static final String CONFIGURATION_NODE = "com.yworks.isometry.node";

  /** The name of the configuration of isometric group nodes. */
  private static final String CONFIGURATION_GROUP = "com.yworks.isometry.group";

  /** The name of the configuration of isometric edge labels. */
  private static final String CONFIGURATION_EDGE_LABEL = "com.yworks.isometry.edge_label";

  /** The name of the configuration of isometric group node labels. */
  private static final String CONFIGURATION_GROUP_LABEL = "com.yworks.isometry.group_label";

  public static void initializeConfigurations() {
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();

    // Create and register a configuration for common nodes.
    final Map nodeImplMap = factory.createDefaultConfigurationMap();
    final IsometryNodePainter nodePainter = new IsometryNodePainter();
    nodeImplMap.put(GenericNodeRealizer.Painter.class, nodePainter);
    nodeImplMap.put(GenericNodeRealizer.ContainsTest.class, nodePainter);
    nodeImplMap.put(
        GenericNodeRealizer.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    factory.addConfiguration(CONFIGURATION_NODE, nodeImplMap);

    // Create and register a configuration for group and folder nodes.
    final Map groupImplMap = factory.createDefaultConfigurationMap();
    final IsometryGroupPainter groupPainter = new IsometryGroupPainter(nodePainter);
    groupImplMap.put(GenericNodeRealizer.Painter.class, groupPainter);
    groupImplMap.put(GenericNodeRealizer.ContainsTest.class, groupPainter);
    groupImplMap.put(
        GenericNodeRealizer.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    factory.addConfiguration(CONFIGURATION_GROUP, groupImplMap);

    // Create and register a configuration for edge labels.
    final YLabel.Factory edgeLabelFactory = EdgeLabel.getFactory();
    final Map edgeLabelImplMap = edgeLabelFactory.createDefaultConfigurationMap();
    final EdgeLabelConfiguration configuration = new EdgeLabelConfiguration();
    edgeLabelImplMap.put(YLabel.Painter.class, configuration);
    edgeLabelImplMap.put(YLabel.Layout.class, configuration);
    edgeLabelImplMap.put(YLabel.BoundsProvider.class, configuration);
    edgeLabelImplMap.put(
        YLabel.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    edgeLabelFactory.addConfiguration(CONFIGURATION_EDGE_LABEL, edgeLabelImplMap);

    // Create and register a configuration for node labels.
    final YLabel.Factory nodeLabelFactory = NodeLabel.getFactory();
    final Map nodeLabelImplMap = nodeLabelFactory.createDefaultConfigurationMap();
    final GroupLabelConfiguration configuration1 = new GroupLabelConfiguration();
    nodeLabelImplMap.put(YLabel.Painter.class, configuration1);
    nodeLabelImplMap.put(YLabel.Layout.class, configuration1);
    nodeLabelImplMap.put(YLabel.BoundsProvider.class, configuration1);
    nodeLabelImplMap.put(
        YLabel.UserDataHandler.class,
        new SimpleUserDataHandler(SimpleUserDataHandler.EXCEPTION_ON_FAILURE));
    nodeLabelFactory.addConfiguration(CONFIGURATION_GROUP_LABEL, nodeLabelImplMap);
  }

  private IsometryRealizerFactory() {
  }

  /**
   * Applies isometric configurations for to all nodes and labels of the given graph and adds {@link IsometryData} as
   * user data.
   */
  public static void applyIsometryRealizerDefaults(final Graph2D graph) {
    final HierarchyManager hierarchyManager = graph.getHierarchyManager();

    // check all nodes and change the configuration/realizer if necessary
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
      final Node node = nc.node();
      final NodeRealizer realizer = graph.getRealizer(node);
      if (hierarchyManager == null || hierarchyManager.isNormalNode(node)) {
        GenericNodeRealizer gnr;
        if (realizer instanceof GenericNodeRealizer) {
          gnr = (GenericNodeRealizer) realizer;
        } else {
          gnr = new GenericNodeRealizer(realizer);
          graph.setRealizer(node, gnr);
        }
        if (gnr.getConfiguration() == null || !CONFIGURATION_NODE.equals(gnr.getConfiguration())) {
          final IsometryData isometryData = new IsometryData(realizer.getWidth(), realizer.getHeight(), 30, true);
          gnr.setUserData(isometryData);
          final Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, -1, -1);
          isometryData.calculateViewBounds(bounds);
          gnr.setSize(bounds.getWidth(), bounds.getHeight());
          gnr.setConfiguration(CONFIGURATION_NODE);
        }
      } else {
        if (realizer instanceof ProxyShapeNodeRealizer) {
          final ProxyShapeNodeRealizer proxy = (ProxyShapeNodeRealizer) realizer;
          for (int i = 0; i < proxy.realizerCount(); i++) {
            final NodeRealizer delegate = proxy.getRealizer(i);
            GenericNodeRealizer gnr;
            if (delegate instanceof GenericNodeRealizer) {
              gnr = (GenericNodeRealizer) delegate;
            } else {
              gnr = new GenericGroupNodeRealizer(delegate);
              proxy.setRealizer(i, gnr);
              if (delegate.equals(proxy.getRealizerDelegate())) {
                proxy.setRealizerDelegate(gnr);
              }
            }
            if (gnr.getConfiguration() == null || !CONFIGURATION_GROUP.equals(gnr.getConfiguration())) {
              final IsometryData isometryData = new IsometryData(gnr.getWidth(), gnr.getHeight(), 0, true);
              gnr.setUserData(isometryData);
              final Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, -1, -1);
              isometryData.calculateViewBounds(bounds);
              gnr.setSize(bounds.getWidth(), bounds.getHeight());
              gnr.setConfiguration(CONFIGURATION_GROUP);
            }

            if (delegate instanceof GroupFeature) {
              ((GenericGroupNodeRealizer) gnr).setGroupClosed(((GroupFeature) delegate).isGroupClosed());
            }

            setNodeLabelConfiguration(gnr);
          }
        } else {
          GenericNodeRealizer gnr;
          if (realizer instanceof GenericNodeRealizer) {
            gnr = (GenericNodeRealizer) realizer;
          } else {
            gnr = new GenericGroupNodeRealizer(realizer);
            if (realizer instanceof GroupFeature) {
              ((GenericGroupNodeRealizer) gnr).setGroupClosed(((GroupFeature) realizer).isGroupClosed());
            }
            graph.setRealizer(node, gnr);
          }
          if (gnr.getConfiguration() == null || !CONFIGURATION_GROUP.equals(gnr.getConfiguration())) {
            final IsometryData isometryData = new IsometryData(gnr.getWidth(), gnr.getHeight(), 0, true);
            gnr.setUserData(isometryData);
            final Rectangle2D.Double bounds = new Rectangle2D.Double(0, 0, -1, -1);
            isometryData.calculateViewBounds(bounds);
            gnr.setSize(bounds.getWidth(), bounds.getHeight());
            gnr.setConfiguration(CONFIGURATION_GROUP);
          }

          setNodeLabelConfiguration(gnr);
        }
        if (hierarchyManager.isFolderNode(node)) {
          applyIsometryRealizerDefaults((Graph2D) hierarchyManager.getInnerGraph(node));
        }
      }
    }

    // check all edge labels and change the configuration if necessary
    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
      final Edge edge = ec.edge();
      final EdgeRealizer realizer = graph.getRealizer(edge);
      for (int i = 0; i < realizer.labelCount(); i++) {
        final EdgeLabel label = realizer.getLabel(i);
        if (label.getConfiguration() == null
            || !label.getConfiguration().equals(CONFIGURATION_EDGE_LABEL)) {
          label.setRotationAngle(0);
          label.setUserData(new IsometryData(label.getWidth(), label.getHeight(), label.getHeight(), isHorizontal(label)));
          label.setConfiguration(CONFIGURATION_EDGE_LABEL);
          final PreferredPlacementDescriptor descriptor = new PreferredPlacementDescriptor();
          descriptor.setAngle(0);
          descriptor.setAngleReference(PreferredPlacementDescriptor.ANGLE_IS_RELATIVE_TO_EDGE_FLOW);
          descriptor.setSideReference(PreferredPlacementDescriptor.SIDE_IS_ABSOLUTE_WITH_RIGHT_IN_NORTH);
          descriptor.setSideOfEdge(PreferredPlacementDescriptor.PLACE_LEFT_OF_EDGE);
          label.setPreferredPlacementDescriptor(descriptor);
        }
      }
    }
  }

  /**
   * Determines whether or not the given label is orientated horizontally.
   */
  private static boolean isHorizontal(final EdgeLabel label) {
    return label.getOrientedBox().getUpY() == -1 || label.getOrientedBox().getUpY() == 1;
  }

  /**
   * Changes the configuration of the labels that belong to the given realizer.
   */
  private static void setNodeLabelConfiguration(final GenericNodeRealizer realizer) {
    for (int j = 0; j < realizer.labelCount(); j++) {
      final NodeLabel label = realizer.getLabel(j);
      if (label.getConfiguration() == null
          || !label.getConfiguration().equals(CONFIGURATION_GROUP_LABEL)) {
        label.setUserData(new IsometryData(label.getWidth(), label.getHeight(), 0, true));
        label.setConfiguration(CONFIGURATION_GROUP_LABEL);
      }
    }
  }

  /**
   * Returns the isometry data of the given realizer. For {@link y.view.hierarchy.ProxyAutoBoundsNodeRealizer} the user
   * data of the delegate is returned.
   */
  public static IsometryData getIsometryData(final NodeRealizer realizer) {
    NodeRealizer nr = realizer;
    if (realizer instanceof ProxyAutoBoundsNodeRealizer) {
      ProxyAutoBoundsNodeRealizer proxy = (ProxyAutoBoundsNodeRealizer) realizer;
      nr = proxy.getRealizerDelegate();
    }

    if (nr instanceof GenericNodeRealizer) {
      return (IsometryData) ((GenericNodeRealizer) nr).getUserData();
    }

    return null;
  }
}
