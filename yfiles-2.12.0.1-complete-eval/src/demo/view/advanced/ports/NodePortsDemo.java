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

import demo.view.hierarchy.GroupingDemo;
import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.geom.YRectangle;
import y.io.GraphMLIOHandler;
import y.io.graphml.graph2d.Graph2DGraphMLHandler;
import y.layout.LayoutOrientation;                                              
import y.layout.Layouter;                                                       
import y.layout.NodeHalo;                                                       
import y.layout.hierarchic.IncrementalHierarchicLayouter;                       
import y.layout.hierarchic.incremental.SimplexNodePlacer;                       
import y.layout.router.polyline.EdgeRouter;                                     
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;                                            
import y.view.Graph2DViewActions;
import y.view.NodeLabel;
import y.view.NodePort;
import y.view.NodePortLayoutConfigurator;                                       
import y.view.NodeRealizer;
import y.view.NodeStateChangeEdgeRouter;
import y.view.ProxyShapeNodeRealizer;
import y.view.ShapeNodePainter;
import y.view.TooltipMode;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.GroupNodePainter;
import y.view.hierarchy.HierarchyManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Demonstrates how to use {@link y.view.NodePort}s.
 * <p>
 * Things to try:
 * </p>
 * <ul>
 *   <li>Left-click on a port to select it.</li>
 *   <li>Drag a selected port to move it around.</li>
 *   <li>Press DELETE to remove all selected ports as well as all edges
 *       connecting to selected ports and all labels associated top selected
 *       ports.</li>
 *   <li>Drag a port that is not selected to start creating an edge from that
 *       port.</li>
 *   <li>Right-click on a node to display a context menu that allows for
 *       adding additional ports to a node.</li>
 *   <li>Right-click on a port to display a context menu that allows for
 *     <ul>
 *       <li>... adding a label that is associated to the port.</li>
 *       <li>... changing the valid positions of the port.</li>
 *       <li>... removing the port.</li>
 *     </ul>
 *   </li>
 *   <li>Select one or more ports then press CONTROL+A to select all ports.</li>
 *   <li>Select one or more ports then use the selection box (by dragging from
 *       an empty point) to select additional ports.</li>
 *   <li>Select one or more nodes then press CONTROL+ALT+G to create a common
 *       parent group node for the selected nodes.
 * </ul>
 * <p>
 * Class {@link PortConfigurations} demonstrate how to customize the visual
 * appearance of ports by re-using existing visualizations for nodes.
 * <p>
 * Nested classes {@link NormalEdgeProcessor} and {@link InterEdgeProcessor}
 * demonstrate how to update edge-to-port associations when a group node is
 * closed or a folder node is opened.
 * Method {@link #createNodeHaloMap()} shows how to use
 * {@link y.layout.NodeHalo}s to prevent layout and/or edge routing algorithms
 * from generating node port overlaps.
 * </p>
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/realizer_related.html">Section Realizer-Related Features</a> in the yFiles for Java Developer's Guide
 */
public class NodePortsDemo extends GroupingDemo {
  private boolean fixPortsForLayout;                                            
  private boolean groupEdges;                                                   

  public NodePortsDemo() {
    this(null);
  }

  public NodePortsDemo( final String helpFilePath ) {
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new Palette(view), view);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    contentPane.add(splitPane, BorderLayout.CENTER);
    addHelpPane(helpFilePath);
  }

  /**
   * Overwritten to initialize the demo's hierarchy manager.
   */
  protected void initialize() {
    super.initialize();

    new HierarchyManager(view.getGraph2D());
  }

  /**
   * Overwritten to add {@link y.view.NodePort} aware actions to handle closing group nodes and
   * opening folder nodes.
   */
  protected void registerViewActions() {
    super.registerViewActions();

    final ActionMap amap = view.getCanvasComponent().getActionMap();

    final Graph2DViewActions.CloseGroupsAction closeGroups =
            new Graph2DViewActions.CloseGroupsAction(view);
    // add callback implementations that ensure edges connected to ports are
    // correctly reassigned when proxy realizers are used
    // additionally, InterEdgeProcessor will assign all inter edges to the
    // corresponding folder node's first port (if there is one)
    // see class documentation
    closeGroups.setNodeStateChangeHandler(new MyNodeStateChangeEdgeRouter(
            new InterEdgeProcessor(new NormalEdgeProcessor())));
    amap.put(Graph2DViewActions.CLOSE_GROUPS, closeGroups);
    final Graph2DViewActions.OpenFoldersAction openFolders =
            new Graph2DViewActions.OpenFoldersAction(view);
    // add callback implementations that ensure edges connected to ports are
    // correctly reassigned when proxy realizers are used
    // see class documentation
    openFolders.setNodeStateChangeHandler(new MyNodeStateChangeEdgeRouter(
            new NormalEdgeProcessor()));
    amap.put(Graph2DViewActions.OPEN_FOLDERS, openFolders);
  }

  /**
   * Overwritten to create a {@link demo.view.advanced.ports.PortEditMode}
   * instance that provides {@link y.view.NodePort} support.
   * @return a {@link demo.view.advanced.ports.PortEditMode} instance.
   */
  protected EditMode createEditMode() {
    return new PortEditMode();
  }

  /**
   * Overwritten to enable tooltips only for node ports.
   * <p>
   * A tool tip will be displayed for a node port, if the node port has
   * at least one label. The tool tip text will be the label text.
   * Tool tip texts for node ports may be customized by overwriting
   * {@link TooltipMode#getNodePortTip(y.view.NodePort)}.
   * </p>
   */
  protected TooltipMode createTooltipMode() {
    final TooltipMode tooltipMode = new TooltipMode();
    tooltipMode.setNodeTipEnabled(false);
    tooltipMode.setEdgeTipEnabled(false);
    tooltipMode.setPortTipEnabled(true);
    return tooltipMode;
  }

  /**
   * Overwritten to add controls for layout and router options.
   * @return the application tool bar.
   */
  protected JToolBar createToolBar() {
    final Action edgeRoutingAction = new AbstractAction("Route Edges") {                       
      public void actionPerformed(final ActionEvent e) {                                       
        routeOrthogonally();                                                                   
      }                                                                                        
    };                                                                                         
    edgeRoutingAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);                         

    final Action layoutAction = new AbstractAction("Layout") {                                 
      public void actionPerformed(final ActionEvent e) {                                       
        layoutHierarchically();                                                                
      }                                                                                        
    };                                                                                         
    layoutAction.putValue(Action.SMALL_ICON, SHARED_LAYOUT_ICON);                              


    final JToolBar jtb = super.createToolBar();
    jtb.addSeparator();                                                                        
    jtb.add(createActionControl(layoutAction));                                                
    jtb.add(createActionControl(edgeRoutingAction));                                           
    jtb.addSeparator(TOOLBAR_SMALL_SEPARATOR);                                                 
    final JToggleButton fpJb = new JToggleButton("Fixed Ports");                               
    fpJb.setToolTipText("Toggle the 'Fixed Ports' feature of the layout algorithms");          
    fpJb.addItemListener(new ItemListener() {                                                  
      public void itemStateChanged( final ItemEvent e ) {                                      
        fixPortsForLayout = ItemEvent.SELECTED == e.getStateChange();                          
      }                                                                                        
    });                                                                                        
    fpJb.doClick();                                                                            
    jtb.add(fpJb);                                                                             
    jtb.addSeparator(TOOLBAR_SMALL_SEPARATOR);                                                 
    final JToggleButton geJb = new JToggleButton("Edge Grouping");                             
    geJb.setToolTipText("Toggle the 'Automatic Edge Grouping' feature of the layout algorithms"); 
    geJb.addItemListener(new ItemListener() {                                                  
      public void itemStateChanged( final ItemEvent e ) {                                      
        groupEdges = ItemEvent.SELECTED == e.getStateChange();                                 
      }                                                                                        
    });                                                                                        
    jtb.add(geJb);                                                                             
                                                                                               
    final ButtonGroup buttonGroup = new ButtonGroup();                                         
    buttonGroup.add(fpJb);                                                                     
    buttonGroup.add(geJb);                                                                     
                                                                                               
    return jtb;
  }

  /**
   * Overwritten to support (de-)serialization of {@link SidePortLocationModel}.
   * @return a configured {@link GraphMLIOHandler} instance.
   */
  protected GraphMLIOHandler createGraphMLIOHandler() {
    final SidePortLocationModel.Handler modelHandler = new SidePortLocationModel.Handler();
    final GraphMLIOHandler graphMLIOHandler = super.createGraphMLIOHandler();
    final Graph2DGraphMLHandler graphMLHandler = graphMLIOHandler.getGraphMLHandler();
    graphMLHandler.addDeserializationHandler(modelHandler);
    graphMLHandler.addSerializationHandler(modelHandler);
    return graphMLIOHandler;
  }

  /**
   * Overwritten to prevent the {@link y.view.hierarchy.HierarchyManager}
   * instance that is created in {@link #initialize()} from being replaced.
   * @param rootGraph the graph for which a
   * {@link y.view.hierarchy.HierarchyManager} has to be created.
   * @return the {@link y.view.hierarchy.HierarchyManager} for the specified
   * graph.
   */
  protected HierarchyManager createHierarchyManager( final Graph2D rootGraph ) {
    return rootGraph.getHierarchyManager();
  }

  protected void loadInitialGraph() {
    // ensure that port configurations are already registered
    PortConfigurations.INSTANCE.getClass();

    loadGraph("resource/NodePortsDemo.graphml");
  }

  /**
   * Overwritten to change the default group and folder node representations.
   */
  protected void configureDefaultGroupNodeRealizers() {
    //Create additional configuration for default group node realizers
    Map map = GenericGroupNodeRealizer.createDefaultConfigurationMap();

    GroupNodePainter gnp = new GroupNodePainter(new GroupShapeNodePainter());
    map.put(GenericNodeRealizer.Painter.class, gnp);
    map.put(GenericNodeRealizer.ContainsTest.class, gnp);
    map.put(GenericNodeRealizer.GenericMouseInputEditorProvider.class, gnp);
    map.put(GenericNodeRealizer.Initializer.class, gnp);

    DefaultGenericAutoBoundsFeature abf = new DefaultGenericAutoBoundsFeature();
    abf.setConsiderNodeLabelSize(true);
    map.put(GenericGroupNodeRealizer.GenericAutoBoundsFeature.class, abf);
    map.put(GenericNodeRealizer.GenericSizeConstraintProvider.class, abf);
    map.put(GenericNodeRealizer.LabelBoundsChangedHandler.class, abf);

    GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    factory.addConfiguration(CONFIGURATION_GROUP, map);


    GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();

    //Register first, since this will also configure the node label
    gnr.setConfiguration(CONFIGURATION_GROUP);

    //Nicer colors
    gnr.setFillColor(new Color(202,236,255,132));
    gnr.setLineColor(new Color(102, 102,153,255));
    NodeLabel label = gnr.getLabel();
    label.setBackgroundColor(null);
    label.setTextColor(Color.BLACK);
    label.setFontSize(15);


    //Set default group and folder node realizers
    DefaultHierarchyGraphFactory hgf = (DefaultHierarchyGraphFactory)
            getHierarchyManager().getGraphFactory();

    hgf.setProxyNodeRealizerEnabled(true);

    hgf.setDefaultGroupNodeRealizer(gnr.createCopy());
    hgf.setDefaultFolderNodeRealizer(gnr.createCopy());
  }



  private void layoutHierarchically() {                                         
    final SimplexNodePlacer placer = new SimplexNodePlacer();                   
    placer.setBaryCenterModeEnabled(true);                                      
                                                                                
    final IncrementalHierarchicLayouter layouter =                              
            new IncrementalHierarchicLayouter();                                
    layouter.setLayoutOrientation(LayoutOrientation.LEFT_TO_RIGHT);             
    layouter.setNodePlacer(placer);                                             
                                                                                
    // a minimum distance of 15 will prevent edges from being routed through    
    // node ports of adjacent nodes because non-free node ports protrude their  
    // nodes by 10 size units                                                   
    layouter.getEdgeLayoutDescriptor().setMinimumDistance(15);                  
                                                                                
    layouter.getEdgeLayoutDescriptor().setMinimumFirstSegmentLength(20);        
    layouter.getEdgeLayoutDescriptor().setMinimumLastSegmentLength(40);         
    layouter.setMinimumLayerDistance(60);                                       
                                                                                
    doLayout(layouter, false);                                                  
  }                                                                             
                                                                                
  private void routeOrthogonally() {                                            
    doLayout(new EdgeRouter(), true);                                           
  }                                                                             
                                                                                
  private void doLayout(                                                        
          final Layouter layouter,                                              
          final boolean useStrongGroupConstraints                               
  ) {                                                                           
    final Graph2DLayoutExecutor executor = new Graph2DLayoutExecutor();         
                                                                                
    final NodePortLayoutConfigurator configurator =                             
    executor.getNodePortConfigurator();                                         
    configurator.setAutomaticPortConstraintsEnabled(fixPortsForLayout);         
    configurator.setAutomaticEdgeGroupsEnabled(groupEdges);                     
    configurator.setStrongGroupConstraintsEnabled(useStrongGroupConstraints);   
                                                                                
    final NodeMap haloMap = createNodeHaloMap();                                
    view.getGraph2D().addDataProvider(NodeHalo.NODE_HALO_DPKEY, haloMap);       
    executor.doLayout(view, layouter);                                          
    view.getGraph2D().removeDataProvider(NodeHalo.NODE_HALO_DPKEY);             
  }                                                                             

  /**                                                                              
   * Creates a node map with a {@link NodeHalo} for each node with ports.          
   * A <code>NodeHalo</code> defines a rectangular area around a node.             
   * Layout algorithms avoid placing other nodes, edges, or labels in this area.   
   * this method creates node halos that reserve enough space for the visual       
   * representation of the node ports thereby preventing layout and/or edge        
   * routing algorithms from generating node port overlaps.                        
   */                                                                              
  private NodeMap createNodeHaloMap() {                                            
    final NodeMap haloMap = Maps.createHashedNodeMap();                            
    final Graph2D graph = view.getGraph2D();                                       
                                                                                   
    // The node ports in this demo extend at most 10 space units beyond the        
    // node border. Therefore it is possible to use a shared halo instance         
    // for all nodes.                                                              
    // In a real world application, it usually will be necessary to create         
    // distinct halo instances for each node depending on the geometry of the      
    // node ports of said node.                                                    
    final NodeHalo sharedHalo = NodeHalo.create(10, 10, 10, 10);                   
                                                                                   
    for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {                      
      if (graph.getRealizer(nc.node()).portCount() > 0) {                          
        haloMap.set(nc.node(), sharedHalo);                                        
      }                                                                            
    }                                                                              
    return haloMap;                                                                
  }                                                                                


  public static void main( String[] args ) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        (new NodePortsDemo("resource/nodeportshelp.html")).start();
      }
    });
  }


  /**
   * Specifies the contract of a post-processor for
   * {@link demo.view.advanced.ports.NodePortsDemo.MyNodeStateChangeEdgeRouter}.
   */
  private static interface Processor {
    /**
     * Post-processing for
     * {@link demo.view.advanced.ports.NodePortsDemo.MyNodeStateChangeEdgeRouter#preEdgeStateChange(y.base.Edge, y.base.Node)}.
     * @param edge the edge whose state is about to change.
     * @param groupNode the node whose state change will trigger the edge state
     * change.
     */
    public void preEdgeStateChange( Edge edge, Node groupNode );

    /**
     * Post-processing for
     * {@link demo.view.advanced.ports.NodePortsDemo.MyNodeStateChangeEdgeRouter#postEdgeStateChange(y.base.Edge, y.base.Node)}.
     * @param edge the edge whose state has changed.
     * @param groupNode the node whose state change has triggered the edge state
     * change.
     */
    public void postEdgeStateChange( Edge edge, Node groupNode );

    /**
     * Post-processing for
     * {@link demo.view.advanced.ports.NodePortsDemo.MyNodeStateChangeEdgeRouter#postNodeStateChange(y.base.Node)}.
     * @param groupNode the node whose state has changed.
     */
    public void postNodeStateChange( Node groupNode );
  }

  /**
   * Simple {@link y.view.NodeStateChangeEdgeRouter} implementation that allows
   * for arbitrary post-processing in
   * {@link #preEdgeStateChange(y.base.Edge, y.base.Node)},
   * {@link #postEdgeStateChange(y.base.Edge, y.base.Node)}, and
   * {@link #postNodeStateChange(y.base.Node)}.
   */
  private static final class MyNodeStateChangeEdgeRouter
          extends NodeStateChangeEdgeRouter {
    private final Processor impl;

    MyNodeStateChangeEdgeRouter( final Processor impl ) {
      this.impl = impl;
    }

    /**
     * Overwritten to allow for post-processing.
     * @param edge the edge whose state is about to change.
     * @param groupNode the node whose state change will trigger the edge state
     * change.
     */
    protected void preEdgeStateChange( final Edge edge, final Node groupNode ) {
      if (impl != null) {
        impl.preEdgeStateChange(edge, groupNode);
      }

      super.preEdgeStateChange(edge, groupNode);
    }

    /**
     * Overwritten to allow for post-processing.
     * @param edge the edge whose state has changed.
     * @param groupNode the node whose state change has triggered the edge state
     * change.
     */
    protected void postEdgeStateChange( final Edge edge, final Node groupNode ) {
      super.postEdgeStateChange(edge, groupNode);

      if (impl != null) {
        impl.postEdgeStateChange(edge, groupNode);
      }
    }

    /**
     * Overwritten to allow for post-processing.
     * @param groupNode the node whose state has changed.
     */
    public void postNodeStateChange( final Node groupNode ) {
      super.postNodeStateChange(groupNode);

      if (impl != null) {
        impl.postNodeStateChange(groupNode);
      }
    }
  }

  /**
   * Reassigns node ports of normal edges from one realizer delegate to the
   * other at nodes that use {@link y.view.ProxyShapeNodeRealizer} upon node
   * state changes.
   * By default, edges connecting to the <code>i</code>-th port of one realizer
   * delegate are assigned to the <code>i</code>-th port of the other realizer
   * delegate (if it exists).
   * More sophisticated strategies can be realized by customizing method
   * {@link #remapPort(y.view.NodeRealizer, y.view.EdgeRealizer, y.view.NodePort, boolean)}.
   */
  private static final class NormalEdgeProcessor implements Processor {
    private final Map node2edgeState;

    NormalEdgeProcessor() {
      node2edgeState = new WeakHashMap();
    }


    /**
     * Stores the current port information of the specified edge.
     * @param edge the edge whose state is about to change.
     * @param groupNode the node whose state change will trigger the edge state
     */
    public void preEdgeStateChange( final Edge edge, final Node groupNode ) {
      if (groupNode.getGraph() instanceof Graph2D) {
        final Graph2D graph = (Graph2D) groupNode.getGraph();
        final HierarchyManager hm = graph.getHierarchyManager();
        if (acceptEdge(hm, edge, groupNode)) {
          Map edge2state = (Map) node2edgeState.get(groupNode);
          if (edge2state == null) {
            edge2state = new WeakHashMap();
            node2edgeState.put(groupNode, edge2state);
          }

          if (edge.source() == groupNode) {
            final NodePort port = NodePort.getSourcePort(graph.getRealizer(edge));
            if (port != null &&
                matches(port.getRealizer(), graph.getRealizer(groupNode))) {
              EdgeState state = (EdgeState) edge2state.get(edge);
              if (state == null) {
                state = new EdgeState();
                edge2state.put(edge, state);
              }
              state.sourcePort = port;
            }
          }
          if (edge.target() == groupNode) {
            final NodePort port = NodePort.getTargetPort(graph.getRealizer(edge));
            if (port != null &&
                matches(port.getRealizer(), graph.getRealizer(groupNode))) {
              EdgeState state = (EdgeState) edge2state.get(edge);
              if (state == null) {
                state = new EdgeState();
                edge2state.put(edge, state);
              }
              state.targetPort = port;
            }
          }
        }
      }
    }

    /**
     * Assigns an appropriate port to the specified edge.
     * Calls
     * {@link #remapPort(y.view.NodeRealizer, y.view.EdgeRealizer, y.view.NodePort, boolean)}
     * to determine which port is appropriate.
     * @param edge the edge whose state has changed.
     * @param groupNode the node whose state change has triggered the edge state
     */
    public void postEdgeStateChange( final Edge edge, final Node groupNode ) {
      if (groupNode.getGraph() instanceof Graph2D) {
        final Graph2D graph = (Graph2D) groupNode.getGraph();
        final HierarchyManager hm = graph.getHierarchyManager();
        if (acceptEdge(hm, edge, groupNode)) {
          final Map edge2state = (Map) node2edgeState.get(groupNode);
          final EdgeState state = (EdgeState) edge2state.get(edge);
          if (state != null) {
            if (edge.source() == groupNode) {
              remapPort(
                      graph.getRealizer(groupNode), graph.getRealizer(edge),
                      state.sourcePort,
                      true);
            }
            if (edge.target() == groupNode) {
              remapPort(
                      graph.getRealizer(groupNode), graph.getRealizer(edge),
                      state.targetPort,
                      false);
            }
          }
        }
      }
    }

    /**
     * Removes the no longer needed stored port information for all edges
     * related to the specified node.
     * @param groupNode the node whose state has changed.
     */
    public void postNodeStateChange( final Node groupNode ) {
      node2edgeState.remove(groupNode);
    }


    /**
     * Remaps the specified edge realizer's port to one that belongs to the
     * specified node realizer.
     * @param nr the realizer that provides possible new ports.
     * @param er the realizer whose port has to be remapped.
     * @param port the old port.
     * @param source if <code>true</code> the source port has to be remapped;
     * otherwise the target port has to be remapped.
     */
    void remapPort(
            final NodeRealizer nr,
            final EdgeRealizer er,
            final NodePort port,
            final boolean source
    ) {
      if (port != null) {
        final NodeRealizer oldNr = port.getRealizer();
        if (!matches(oldNr, nr) &&
            oldNr != null &&
            oldNr.portCount() == nr.portCount()) {
          bindPort(nr.getPort(indexOf(port)), er, source);
        }
      }
    }


    /**
     * Binds the specified port to the specified edge.
     * @param port the port to bind to the specified edge.
     * @param er the realizer representing the edge.
     * @param source if <code>true</code> the source port has to be bound;
     * otherwise the target port has to be bound.
     */
    private static void bindPort(
            final NodePort port,
            final EdgeRealizer er,
            final boolean source
    ) {
      if (source) {
        NodePort.bindSourcePort(port, er);
      } else {
        NodePort.bindTargetPort(port, er);
      }
    }

    /**
     * Returns <code>true</code> if the specified edge's port assignment has to
     * be remapped and <code>false</code> otherwise.
     * @param hm the nesting structure of the specified edge's graph.
     * @param edge the edge to check.
     * @param groupNode the node whose state changes.
     * @return <code>true</code> if the specified edge's port assignment has to
     * be remapped and <code>false</code> otherwise.
     */
    private static boolean acceptEdge(
            final HierarchyManager hm,
            final Edge edge,
            final Node groupNode
    ) {
      if (hm != null && hm.isFolderNode(groupNode) && hm.isInterEdge(edge)) {
        return hm.getRealSource(edge) == groupNode ||
               hm.getRealTarget(edge) == groupNode;
      } else {
        return edge.source() == groupNode || edge.target() == groupNode;
      }
    }

    /**
     * Returns the zero-based index of the specified port within its associated
     * node realizer's collection of ports.
     * @param port the port whose index is to be determined.
     * @return the zero-based  index of the specified port within its associated
     * node realizer's collection of ports or <code>-1>/code> if the port
     * currently is not associated to any node realizer.
     */
    private static int indexOf( final NodePort port ) {
      final NodeRealizer owner = port.getRealizer();
      if (owner != null) {
        for (int i = 0, n = owner.portCount(); i < n; ++i) {
          if (owner.getPort(i) == port) {
            return i;
          }
        }
      }
      return -1;
    }

    /**
     * Returns <code>true</code> if the first realizer equals the second or the
     * second realizer's delegate and <code>false</code> otherwise.
     * @param portNr a realizer associated to a {@link y.view.NodePort}.
     * @param otherNr another realizer, possibly a
     * {@link y.view.ProxyShapeNodeRealizer}.
     * @return <code>true</code> if the first realizer equals the second or the
     * second realizer's delegate and <code>false</code> otherwise.
     */
    private static boolean matches(
            final NodeRealizer portNr,
            final NodeRealizer otherNr
    ) {
      return portNr == otherNr ||
             (otherNr instanceof ProxyShapeNodeRealizer &&
              portNr == ((ProxyShapeNodeRealizer) otherNr).getRealizerDelegate());
    }


    /**
     * Stores port information for an edge.
     */
    private static final class EdgeState {
      NodePort sourcePort;
      NodePort targetPort;
    }
  }

  /**
   * Reassigns node ports of edges that are converted to inter edges.
   * By default, edges are automatically assigned to the first port of the
   * corresponding folder node (if the node has any ports at all).
   * More sophisticated strategies can be realized by customizing method
   * {@link #remapPort(y.view.NodeRealizer, y.view.EdgeRealizer, boolean)}.
   */
  private static final class InterEdgeProcessor implements Processor {
    private final Processor impl;

    InterEdgeProcessor( final Processor impl ) {
      this.impl = impl;
    }

    public void preEdgeStateChange( final Edge edge, final Node groupNode ) {
      if (impl != null) {
        impl.preEdgeStateChange(edge, groupNode);
      }
    }

    /**
     * Assigns an appropriate port to the specified edge.
     * Calls
     * {@link #remapPort(y.view.NodeRealizer, y.view.EdgeRealizer, boolean)}
     * to determine which port is appropriate.
     * @param edge the edge whose state has changed.
     * @param groupNode the node whose state change has triggered the edge state
     */
    public void postEdgeStateChange( final Edge edge, final Node groupNode ) {
      if (impl != null) {
        impl.postEdgeStateChange(edge, groupNode);
      }

      if (groupNode.getGraph() instanceof Graph2D) {
        final Graph2D graph = (Graph2D) groupNode.getGraph();
        final HierarchyManager hm = graph.getHierarchyManager();
        if (hm != null && hm.isFolderNode(groupNode) && hm.isInterEdge(edge)) {
          if (edge.source() == groupNode && hm.getRealSource(edge) != groupNode) {
            remapPort(
                    graph.getRealizer(groupNode),
                    graph.getRealizer(edge),
                    true);
          }
          if (edge.target() == groupNode && hm.getRealTarget(edge) != groupNode) {
            remapPort(
                    graph.getRealizer(groupNode),
                    graph.getRealizer(edge),
                    false);
          }
        }
      }
    }

    public void postNodeStateChange( final Node groupNode ) {
      if (impl != null) {
        impl.postNodeStateChange(groupNode);
      }
    }

    /**
     * Remaps the specified edge realizer's port to the first port of the
     * specified node realizer.
     * @param nr the realizer that provides possible new ports.
     * @param er the realizer whose port has to be remapped.
     * @param source if <code>true</code> the source port has to be remapped;
     * otherwise the target port has to be remapped.
     */
    protected void remapPort(
            final NodeRealizer nr,
            final EdgeRealizer er,
            final boolean source
    ) {
      if (nr.portCount() > 0) {
        bindPort(nr.getPort(0), er, source);
      }
    }


    /**
     * Binds the specified port to the specified edge.
     * @param port the port to bind to the specified edge.
     * @param er the realizer representing the edge.
     * @param source if <code>true</code> the source port has to be bound;
     * otherwise the target port has to be bound.
     */
    private static void bindPort(
            final NodePort port,
            final EdgeRealizer er,
            final boolean source
    ) {
      if (source) {
        NodePort.bindSourcePort(port, er);
      } else {
        NodePort.bindTargetPort(port, er);
      }
    }
  }

  /**
   * Painter implementation for group nodes that draws a special header
   * compartment below the groups default label.
   * In the default group node configuration, this compartment is drawn by
   * the default label. This means said compartment will be painted over node
   * ports because node labels are rendered after node ports.
   */
  private static final class GroupShapeNodePainter extends ShapeNodePainter {
    private static final Color BACKGROUND = new Color(153, 204, 255, 255);

    GroupShapeNodePainter() {
      super(ROUND_RECT);
    }

    protected void paintFilledShape(
            final NodeRealizer context,
            final Graphics2D graphics,
            final Shape shape
    ) {
      super.paintFilledShape(context, graphics, shape);

      if (context.labelCount() > 0) {
        final Shape oldClip = graphics.getClip();
        final Color oldColor = graphics.getColor();

        final Rectangle2D cb = oldClip.getBounds2D();
        final YRectangle r = context.getLabel().getBox();
        graphics.clip(new Rectangle2D.Double(cb.getX(), r.getY(), cb.getWidth(), r.getHeight()));
        graphics.setColor(BACKGROUND);
        graphics.fill(shape);

        graphics.setColor(oldColor);
        graphics.setClip(oldClip);
      }
    }
  }
}
