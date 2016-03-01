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
package demo.view.orgchart;

import demo.view.DemoBase;
import demo.view.orgchart.OrgChartTreeModel.Employee;
import demo.view.orgchart.ViewModeFactory.JTreeChartEditMode;
import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.DataProvider;
import y.base.Edge;
import y.base.Node;
import y.base.NodeMap;
import y.geom.OrientedRectangle;
import y.geom.YInsets;
import y.layout.Layouter;
import y.layout.NormalizingGraphElementOrderStage;
import y.util.D;
import y.util.DataProviderAdapter;
import y.view.AutoDragViewMode;
import y.view.EdgeRealizer;
import y.view.GenericNodeRealizer;
import y.view.GenericNodeRealizer.Painter;
import y.view.Graph2D;
import y.view.Graph2DSelectionEvent;
import y.view.Graph2DSelectionListener;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.HitInfo;
import y.view.LineType;
import y.view.MagnifierViewMode;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.Overview;
import y.view.PolyLineEdgeRealizer;
import y.view.ShapeNodePainter;
import y.view.ShapeNodeRealizer;
import y.view.SmartNodeLabelModel;
import y.view.TooltipMode;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.YLabel;
import y.view.YRenderingHints;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GroupNodeRealizer;

import javax.swing.Icon;
import javax.swing.tree.TreeModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;

/** 
 * This class builds upon the more generic tree chart component {@link demo.view.orgchart.JTreeChart}. 
 * It visualizes a {@link demo.view.orgchart.OrgChartTreeModel}. 
 * Also it customizes the look and feel of the component to make it suitable for
 * organization charts. 
 */
public class JOrgChart extends JTreeChart {

  /**
   * Defines the colors being used for the graph elements. There are four different states a node can be in: unselected, selected, highlighted, highlighted and selected.
   * The colors below define the colors used for each state.
   */
  static final Color FILL_COLOR = new Color( 0xCCFFFF );
  static final Color FILL_COLOR2 = new Color( 0x249AE7 );
  static final Color LINE_COLOR = new Color( 0x249AE7 );
  
  static final Color SELECTED_FILL_COLOR = Color.WHITE; 
  static final Color SELECTED_LINE_COLOR = Color.ORANGE;
  static final Color SELECTED_FILL_COLOR2 = Color.ORANGE;
  
  static final Color SELECTED_HOVER_FILL_COLOR = SELECTED_FILL_COLOR;
  static final Color SELECTED_HOVER_LINE_COLOR = SELECTED_LINE_COLOR;
  static final Color SELECTED_HOVER_FILL_COLOR2 = new Color( 0xFFEE55 ); 
  
  static final Color HOVER_FILL_COLOR2 = new Color( 0x63CCEE );
  static final Color HOVER_FILL_COLOR = FILL_COLOR;
  static final Color HOVER_LINE_COLOR = LINE_COLOR;

  
  static final Color EDGE_COLOR = new Color( 0x999999 );  
  static final Color GROUP_FILL_COLOR = new Color(231,219,182,100);


  /**
   * NodeMap used to maintain the highlighted state of a node.
   */
  private final NodeMap highlightMap;

  public JOrgChart(final OrgChartTreeModel model) {
    super(model, createUserObjectDP(), createGroupIdDataProvider());
  
    highlightMap = getGraph2D().createNodeMap();

    getGraph2D().addGraph2DSelectionListener(new TreeChartSelectionListener());
      
    setPaintDetailThreshold(0);
    setAntialiasedPainting(true);    

//TURN ON FRACTIONAL FONT METRICS for precise font measurement
//    getRenderingHints().put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//    YLabel.setFractionMetricsForSizeCalculationEnabled(true);

    /**
     * Listener that adjusts the LoD (level of detail) of the diagram whenever the
     * zoom level of the view changes. 
     */
    getCanvasComponent().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent evt) {
        if("Zoom".equals(evt.getPropertyName())) {
          final double zoom = getZoom();
          Object lodValue = null;
          if(zoom >= 0.8) {
            lodValue = LODRenderingHint.VALUE_LOD_HIGH;
          }
          else if(zoom < 0.8 && zoom >= 0.3) {
            lodValue = LODRenderingHint.VALUE_LOD_MEDIUM;
          }
          else if(zoom < 0.3) {
            lodValue = LODRenderingHint.VALUE_LOD_LOW;
          }
          getRenderingHints().put(LODRenderingHint.KEY_LOD, lodValue);
        }          
      }      
    });           
    
    updateChart();
  }
  
  /**
   * RenderingHint that conveys information about the desired level of detail when rendering graph elements.
   */
  static class LODRenderingHint {    
    public static final Object VALUE_LOD_LOW = "LODRenderingHint#VALUE_LOD_LOW";
    public static final Object VALUE_LOD_MEDIUM = "LODRenderingHint#VALUE_LOD_MEDIUM";
    public static final Object VALUE_LOD_HIGH = "LODRenderingHint#VALUE_LOD_HIGH";
    public static final Object VALUE_LOD_OVERVIEW = "LODRenderingHint#VALUE_LOD_OVERVIEW";
    
    public static final Key KEY_LOD = new RenderingHints.Key(0) {
      public boolean isCompatibleValue(final Object val) {
        return true; //allow all kinds of values 
      }    
    };    
  }

  /**
   * DataProvider that returns the userObject (--> Employee) for a tree model item.
   */
  static DataProvider createUserObjectDP() {
    return new DataProviderAdapter() {
      public Object get(final Object dataHolder) {
        return dataHolder;
      }
    };
  }
  
  /**
   * DataProvider that returns the group Id for a tree model user object.
   * Group Ids are used to convey grouping information to the JTreeChart component.
   */
  static DataProvider createGroupIdDataProvider() {
    return new DataProviderAdapter() {
      public Object get(final Object dataHolder) {
        return ((Employee)dataHolder).businessUnit;
      }
    };    
  }

  /**
   * Overwritten to enable/disable interactive structure changes depending
   * on the specified model's type. I.e. if the specified model is mutable,
   * then interactive editing will be enabled; otherwise it will be disabled.
   */
  public void setModel( final TreeModel model ) {
    // check if the old model was mutable, i.e. compatible with edit mode
    final boolean oldEditable = isEditable();

    super.setModel(model);

    // check if the new model is mutable, i.e. compatible with edit mode
    final boolean newEditable = isEditable();

    updateMouseInteraction(oldEditable, newEditable);
  }

  public void setGroupViewEnabled( final boolean enabled ) {
    // check if the old view state was compatible with edit mode
    final boolean oldEditable = isEditable();

    super.setGroupViewEnabled(enabled);

    // check if the new view state is compatible with edit mode
    final boolean newEditable = isEditable();

    updateMouseInteraction(oldEditable, newEditable);
  }

  private void updateMouseInteraction(
          final boolean oldEditable, final boolean newEditable
  ) {
    if (oldEditable != newEditable) {
      // mutability changed, therefore update mouse handling

      final Iterator it = getViewModes();
      if (it.hasNext()) {
        // the first registered view mode is a dummy that does nothing by itself
        // but serves as convenient way to switch between edit mode and
        // navigation mode
        final ViewMode masterMode = (ViewMode) it.next();
        masterMode.setChild(createTreeChartViewMode(), null, null);
      }
    }
  }

  /**
   * Adds mouse interaction to the component.
   * Depending on the {@link #getModel() model} used, there will be either
   * support for interactive editing of the organization structure or
   * or support for panning only.
   * Support for tool tip display, a nifty local view magnifier, and a
   * roll-over effect for nodes is added as well.     
   */
  protected void addMouseInteraction() {
    // a dummy view mode that does nothing by itself but serves as convenient
    // way to switch between edit mode and navigation mode in setModel
    final ViewMode masterMode = new ViewMode();
    addViewMode(masterMode);
    masterMode.setChild(createTreeChartViewMode(), null, null);

    addViewMode(new AutoDragViewMode());

    final MouseWheelListener mwl = createMouseWheelListener();
    if (mwl != null) {
      getCanvasComponent().addMouseWheelListener(mwl);
    }


    final TooltipMode tooltipMode = new JOrgChartTooltipMode();
    tooltipMode.setEdgeTipEnabled(false);
    tooltipMode.setNodeLabelTipEnabled(true);
    addViewMode(tooltipMode);
    addViewMode(new MiddleClickMagnifierViewMode());
    addViewMode(new RollOverViewMode());
  }

  /**
   * Creates a <code>ViewMode</code> suitable for use with this component.
   * @return a <code>ViewMode</code> suitable for use with this component.
   */
  protected ViewMode createTreeChartViewMode() {
    if (isEditable()) {
      return ViewModeFactory.newEditMode(this);
    } else {
      return ViewModeFactory.newNavigationMode(this);
    }
  }

  private boolean isEditable() {
    return !isGroupViewEnabled() &&
           JTreeChartEditMode.isCompatibleModel(getModel());
  }

  /**
   * Customize and return the MouseWheelListener to be used. 
   */
  protected MouseWheelListener createMouseWheelListener() {
    final Graph2DViewMouseWheelZoomListener mwl = new Graph2DViewMouseWheelZoomListener();
    mwl.setZoomIndicatorShowing(true);
    mwl.setCenterZooming(false);
    mwl.setMaximumZoom(4);
    mwl.setLimitMinimumZoomByContentSize(true);
    mwl.setZoomIndicatorColor(new Color(170, 160,125));
    return mwl;
  }
    
  /**
   * Set NodeRealizer and EdgeRealizer defaults. We use a mixed style here. Employees will be visualized using 
   * GenericNodeRealizer with a custom Painter implementation. Edges are represented using
   * a PolyLineEdgeRealizer, while group nodes get displayed by a customized GroupNodeRealizer.
   */
  protected void setRealizerDefaults() {
    final PolyLineEdgeRealizer er = new PolyLineEdgeRealizer();
    er.setSmoothedBends(true);
    er.setLineColor(EDGE_COLOR);
    er.setLineType(LineType.LINE_2);

    //register default node realizer
    getGraph2D().setDefaultEdgeRealizer(er);
    
    final GenericNodeRealizer.Factory factory = GenericNodeRealizer.getFactory();
    
    final Painter painter = new EmployeePainter();
    //uncomment this for a nice (but rather expensive) drop shadow effect 
    //painter = new ShadowNodePainter(painter); 
    final Map implementationsMap = factory.createDefaultConfigurationMap();
    implementationsMap.put(GenericNodeRealizer.Painter.class, painter);
            
    factory.addConfiguration("employee", implementationsMap);      
    final GenericNodeRealizer nr = new GenericNodeRealizer();
    nr.setFillColor(FILL_COLOR);
    nr.setFillColor2(FILL_COLOR2);
    nr.setLineColor(LINE_COLOR);
    nr.setLineType(LineType.LINE_2);
    nr.setSize(220,110);

    nr.setConfiguration("employee");

    //register default node realizer
    getGraph2D().setDefaultNodeRealizer(nr);
    
    final DefaultHierarchyGraphFactory gf = (DefaultHierarchyGraphFactory) getGraph2D().getHierarchyManager().getGraphFactory();
    final CustomGroupNodeRealizer gnr = new CustomGroupNodeRealizer();
    gnr.setConsiderNodeLabelSize(true);
    gnr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
    gnr.setFillColor(GROUP_FILL_COLOR);  
    gnr.setLineType(LineType.LINE_1);
    gnr.setLineColor(Color.DARK_GRAY); 
    gnr.getLabel().setBackgroundColor(new Color(102,204,255,200));
    gnr.getLabel().setTextColor(new Color(31,104,163).darker());
    gnr.getLabel().setFontSize(20);
    gnr.getLabel().setAlignment(NodeLabel.ALIGN_LEFT);
    gnr.getStateLabel().setVisible(false);

    //register default group and folder nodes
    gf.setDefaultFolderNodeRealizer(gnr);
    gf.setDefaultGroupNodeRealizer(gnr.createCopy());
  }
     
  /**
   * Configures a realizer for a group node that is identified by a group Id.
   */
  protected void configureGroupRealizer(final Node groupNode, final Object groupId, final boolean collapsed) {
    final GroupNodeRealizer nr = (GroupNodeRealizer) getGraph2D().getRealizer(groupNode);
    nr.setGroupClosed(collapsed);    
    nr.setBorderInsets(new YInsets(0,0,0,0));
    nr.getLabel().setText(groupId.toString());            
  }  
  
  /**
   * Configures a realizer for a node representing an employee. This implementation uses node labels
   * to represent and cache visual representations used at different levels of detail. Labels
   * not displayed at a certain level of detail will be set to invisible.
   */
  protected void configureNodeRealizer(final Node n) {
    final Employee employee = (Employee) getUserObject(n);
    
    final GenericNodeRealizer gnr = (GenericNodeRealizer) getGraph2D().getRealizer(n);
    gnr.setUserData(employee);
        
    if (gnr.labelCount() == 1) { //NOT CONFIGURED YET
      //LABEL 0: EMPLOYEE ICON
      NodeLabel label = gnr.getLabel();
      label.setIcon(employee.icon);
      SmartNodeLabelModel model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_TOP_LEFT));
      label.setInsets(new Insets(2,2,2,2));

      //LABEL 1-5: EMPLOYEE INFORMATION
      label = addTextLabel(gnr, employee.name,     65, 4, true); 
      label = addTextLabel(gnr, employee.position, 65, label.getOffsetY() + label.getHeight() + 4, true); // label 2
      label = addTextLabel(gnr, employee.email,    65, label.getOffsetY() + label.getHeight() + 4, true); // label 3
      label = addTextLabel(gnr, employee.phone,    65, label.getOffsetY() + label.getHeight() + 4, true); // label 4
      addTextLabel(gnr, employee.fax,      65, label.getOffsetY() + label.getHeight() + 4, true); // label 5
      
      //LABEL 6: STATE ICON 
      label = gnr.createNodeLabel(); 
      gnr.addLabel(label);
      label.setIcon(new StateIcon(employee.status));
      model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_TOP_RIGHT));
      label.setInsets(new Insets(2,2,2,2));

      //LABEL 7: USED FOR LOW & MEDIUM LOD ONLY  
      label = addTextLabel(gnr, employee.name, 0, 0, false);
      model = new SmartNodeLabelModel();
      label.setLabelModel(model,
          model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_CENTER));
      label.setFontSize(20);
      label.setVisible(false);
    } else { //reconfigure label
      gnr.getLabel().setIcon(employee.icon);
      gnr.getLabel(1).setText(employee.name);
      gnr.getLabel(2).setText(employee.position);
      gnr.getLabel(3).setText(employee.email);
      gnr.getLabel(4).setText(employee.phone);
      gnr.getLabel(5).setText(employee.fax);
      gnr.getLabel(6).setIcon(new StateIcon(employee.status));
      gnr.getLabel(7).setText(employee.name);
    }

    if (employee.vacant) {
      gnr.getLabel().setIcon(null);
      gnr.getLabel(1).setText("Vacant");
      gnr.getLabel(6).setIcon(null);
      gnr.getLabel(7).setText("Vacant");
    }
  }

  /**
   * Configures an edge realizer for the links between employees. Links that connect an employee with his/her assistant
   * will be represented by a dashed line.
   */ 
  protected void configureEdgeRealizer(final Edge e) {
    final EdgeRealizer er = getGraph2D().getRealizer(e);
    final Employee target = (Employee) getUserObject(e.target());
    if(target != null && target.assistant) {
      er.setLineType(LineType.DASHED_2);
    }
    super.configureEdgeRealizer(e);
  }
  
  /**
   * Helper method that creates and returns node labels.
   */
  NodeLabel addTextLabel(final NodeRealizer nr, String text, final double x, final double y, final boolean cropping) {
    final NodeLabel label = nr.createNodeLabel();
    nr.addLabel(label);
    label.setFontSize(11);
    if (text == null) {
      text = "";
    }
    label.setText(text);
    final double h = label.getHeight();
    final OrientedRectangle box = new OrientedRectangle(nr.getX() + x, nr.getY() + y + h, label.getWidth(), h);
    final SmartNodeLabelModel labelModel = new SmartNodeLabelModel();
    label.setLabelModel(labelModel, labelModel.createAlignedModelParameter(
            box, nr, SmartNodeLabelModel.ALIGNMENT_TOP_LEFT));

    if (cropping) {
      final boolean isEmpty = "".equals(text);
      if (isEmpty) {
        label.setText("000-0000");
      }
      final double height = label.getHeight();
      label.setAutoSizePolicy(YLabel.AUTOSIZE_NONE);
      label.setConfiguration("CroppingLabel");
      label.setAlignment(YLabel.ALIGN_LEFT);
      final double width = nr.getWidth() - x;
      label.setContentSize(width, height);
      if (isEmpty) {
        label.setText("");
      }
    }
    return label;
  }

  /**
   * State Icon implementation. The state icon is used by the EmployeePainter to represent 
   * the state of an employee (present, unavailable, travel). Its implementation makes use of
   * LODRenderingHints.
   */
  static class StateIcon implements Icon {

    static final Color STATUS_UNAVAILABLE_COLOR = new Color(255,120,40);
    static final Color STATUS_PRESENT_COLOR = new Color(25,205,44);
    static final Color STATUS_TRAVEL_COLOR = new Color(221,175,233);
    static final Color STATUS_UNAVAILABLE_COLOR2 = new Color(231,32,0);
    static final Color STATUS_PRESENT_COLOR2 = new Color(19,157,33);
    static final Color STATUS_TRAVEL_COLOR2 = new Color(137,44,160);

    String state;
    StateIcon(final String state) {
      this.state = state;
    }
    
    public int getIconHeight() {
      return 24;
    }

    public int getIconWidth() {
      return 24;
    }

    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
      final Color c1;
      final Color c2;
      if ("present".equals(state)) {
        c1 = STATUS_PRESENT_COLOR;
        c2 = STATUS_PRESENT_COLOR2;
      }
      else if ("travel".equals(state)) {
        c1 = STATUS_TRAVEL_COLOR;
        c2 = STATUS_TRAVEL_COLOR2;
      }
      else if ("unavailable".equals(state)) {
        c1 = STATUS_UNAVAILABLE_COLOR;
        c2 = STATUS_UNAVAILABLE_COLOR2;
      } else {
        return;
      }

      final Graphics2D gfx = (Graphics2D) g;
      final Object lod = gfx.getRenderingHint(LODRenderingHint.KEY_LOD);
      
      if(lod == LODRenderingHint.VALUE_LOD_MEDIUM) {
        final Rectangle2D box = new Rectangle2D.Double(x,y,getIconWidth(), getIconHeight());
        gfx.setColor(c1);
        gfx.fill(box);
        gfx.setColor(c2);
        gfx.draw(box);       
      }
      else if(lod == LODRenderingHint.VALUE_LOD_HIGH) {
        final Ellipse2D.Double circle = new Ellipse2D.Double(x,y,getIconWidth(), getIconHeight());
        gfx.setColor(c2);
        gfx.fill(circle);
        final double size = Math.min(getIconHeight(), getIconWidth());
        final double delta = size*0.12;
        circle.setFrame(circle.x+delta, circle.y+delta, circle.width-2*delta, circle.height-2*delta);        
        gfx.setColor(Color.WHITE);
        gfx.fill(circle);
        gfx.setColor(c1);
        circle.setFrame(circle.x+delta, circle.y+delta, circle.width-2*delta, circle.height-2*delta);
        gfx.fill(circle);
      }
    }    
  }
 
  /**
   * Painter implementation that renders an Employee node.
   */
  static class EmployeePainter extends ShapeNodePainter {
    static final Color COLOR_MARKER = new Color(255, 141, 31);
    static final Color COLOR_OVERLAY = new Color(255, 255, 255, 77);

    public EmployeePainter() {
      setShapeType(ROUND_RECT);
    }
    
    public void paint(final NodeRealizer context, final Graphics2D gfx) {

      if(!context.isVisible()) {
        return;
      }

      final Employee employee = getEmployee(context);

      final Object lod = gfx.getRenderingHint(LODRenderingHint.KEY_LOD);

      if (lod == LODRenderingHint.VALUE_LOD_OVERVIEW) {
        final Color ovc;
        if (employee.vacant) {
          ovc = Color.LIGHT_GRAY;
        } else if ("present".equals(employee.status)) {
          ovc = StateIcon.STATUS_PRESENT_COLOR2;
        } else if("travel".equals(employee.status)) {
          ovc = StateIcon.STATUS_TRAVEL_COLOR2;
        } else {
          ovc = StateIcon.STATUS_UNAVAILABLE_COLOR2;
        }
        final Rectangle2D rect = new Rectangle2D.Double(
                context.getX() + 20, context.getY() + 20,
                context.getWidth() - 40, context.getHeight() - 40);
        gfx.setColor(ovc);
        gfx.fill(rect);
        gfx.setColor(Color.BLACK);
        gfx.draw(rect);
      }
      else if (lod == LODRenderingHint.VALUE_LOD_LOW ||
               lod == LODRenderingHint.VALUE_LOD_MEDIUM) {
        paintMarker(context, gfx);
        super.paintNode(context, gfx, true);

        context.getLabel(6).paint(gfx); //state icon

        final NodeLabel label = context.getLabel(7);
        label.setVisible(true);
        label.paint(gfx);
        label.setVisible(false);
      }
      else { //defaults to LODRenderingHint.VALUE_LOD_HIGH)
        paintMarker(context, gfx);
        super.paintNode(context, gfx, false);
        context.paintText(gfx);
      }
    }

    private void paintMarker( final NodeRealizer context, final Graphics2D gfx ) {
      final Node node = context.getNode();
      final DataProvider dp = node.getGraph().getDataProvider(MARKED_NODES_DPKEY);
      if (dp != null && dp.getBool(node)) {
        final int margin = 7;
        gfx.setColor(COLOR_MARKER);
        gfx.fillRect(
                (int) (context.getX()- margin),
                (int) (context.getY()- margin),
                (int) context.getWidth() + 2*margin,
                (int) context.getHeight()+ 2*margin);
      }
    }

    public void paintSloppy(final NodeRealizer context, final Graphics2D gfx) {
      paint(context, gfx);
    }
    
    protected Color createSelectionColor(final Color original) {
      // don't modify the fill color here - we are
      // changing the fill color externally upon selection
      return original;
    }

    protected Color getFillColor( final NodeRealizer context, final boolean selected ) {
      if (!selected && getEmployee(context).vacant) {
        return Color.LIGHT_GRAY;
      } else {
        return super.getFillColor(context, selected);
      }
    }

    protected Color getFillColor2( final NodeRealizer context, final boolean selected ) {
      if (!selected && getEmployee(context).vacant) {
        return null;
      } else {
        return super.getFillColor2(context, selected);
      }
    }

    protected Color getLineColor( final NodeRealizer context, final boolean selected ) {
      if (!selected && getEmployee(context).vacant) {
        return Color.LIGHT_GRAY;
      } else {
        return super.getLineColor(context, selected);
      }
    }

    /**
     * Fill the shape using a custom gradient color and a semi-transparent effect  
     */
    protected void paintFilledShape(final NodeRealizer context, final Graphics2D graphics, final Shape shape) {
      final double x = context.getX();
      final double y = context.getY();
      final double width = context.getWidth();
      final double height = context.getHeight();

      final boolean selected = useSelectionStyle(context, graphics);
      final Color c1 = getFillColor(context, selected);
      
      if (c1 != null && !context.isTransparent()) {
        final Color c2 = getFillColor2(context, selected);
        if (c2 != null && useGradientStyle(graphics)) {
          graphics.setPaint(new GradientPaint(
                  (float) (x + 0.5*width), (float) y, c1,
                  (float) (x + 0.5*width), (float) (y+height), c2));
        } else {
          graphics.setColor(c1);
        }
        graphics.fill(shape);
                
        final Shape clip = graphics.getClip();
        graphics.clip(shape);
        graphics.clip(new Rectangle2D.Double(x,y,width,0.4*height));
        graphics.setColor(COLOR_OVERLAY);
        graphics.fill(shape);
        graphics.setClip(clip);
      }
    }

    private static boolean useGradientStyle( final Graphics2D graphics ) {
      return YRenderingHints.isGradientPaintingEnabled(graphics);
    }

    private static boolean useSelectionStyle(final NodeRealizer context, final Graphics2D graphics) {
      return context.isSelected() &&
             YRenderingHints.isSelectionPaintingEnabled(graphics);
    }

    private static Employee getEmployee( final NodeRealizer context ) {
      final GenericNodeRealizer gnr = (GenericNodeRealizer) context;
      return (Employee) gnr.getUserData();
    }
  }

  /**
   * Customized GroupNodeRealizer that cannot be selected. 
   */
  static class CustomGroupNodeRealizer extends GroupNodeRealizer {
    public CustomGroupNodeRealizer() {
      super();
    }
    public CustomGroupNodeRealizer(final NodeRealizer nr) {
      super(nr);
    }
    public NodeRealizer createCopy(final NodeRealizer nr) {
      return new CustomGroupNodeRealizer(nr);
    }
    public boolean isSelected() {
      return false;
    }
  }

  /**
   * Configures and creates an overview component that uses this JOrgChart as its master view. 
   */
  public Overview createOverview() {
    final Overview ov = super.createOverview();
    /* customize the overview */
    //animates the scrolling
    //blurs the part of the graph which can currently not be seen
    ov.putClientProperty("Overview.PaintStyle", "Funky");
    //allows zooming from within the overview
    ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
    //provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation with arrow keys)
    ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
    //determines how to differ between the part of the graph that can currently be seen, and the rest
    ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
    ov.setPreferredSize(new Dimension(200, 150));
    ov.setMinimumSize(new Dimension(200, 150));
    ov.getRenderingHints().put(LODRenderingHint.KEY_LOD, LODRenderingHint.VALUE_LOD_OVERVIEW);
    return ov;
  }

  private void configureColors(final NodeRealizer nr, final boolean selected, final boolean highlighted) {
    if(highlighted) {
      if(selected) {
        nr.setFillColor(SELECTED_HOVER_FILL_COLOR);
        nr.setFillColor2(SELECTED_HOVER_FILL_COLOR2);
        nr.setLineColor(SELECTED_HOVER_LINE_COLOR);
      } else { //not selected
        nr.setFillColor(HOVER_FILL_COLOR);
        nr.setFillColor2(HOVER_FILL_COLOR2);
        nr.setLineColor(HOVER_LINE_COLOR);
      }
    } else { //not highlighted
      if(selected) {
        nr.setFillColor(SELECTED_FILL_COLOR);
        nr.setFillColor2(SELECTED_FILL_COLOR2);
        nr.setLineColor(SELECTED_LINE_COLOR);
      } else { // not selected
        nr.setFillColor(FILL_COLOR);
        nr.setFillColor2(FILL_COLOR2);
        nr.setLineColor(LINE_COLOR);
      }
    }
  }

  /**
   * Implementation of a ViewMode that activates {@link y.view.MagnifierViewMode} while
   * the middle mouse button or the mouse wheel is pressed or dragged.
   */
  static class MiddleClickMagnifierViewMode extends ViewMode {        
    final MagnifierViewMode magnifierVM;
    public MiddleClickMagnifierViewMode() {
      magnifierVM = new MagnifierViewMode() {
        public void mouseDraggedMiddle(final double x, final double y) {
          mouseDraggedLeft(x, y);
        }
        protected Graph2DView createMagnifierView() {
          final Graph2DView view = super.createMagnifierView();
          view.getRenderingHints().put(LODRenderingHint.KEY_LOD, LODRenderingHint.VALUE_LOD_HIGH);
          return view;
        }
      };
     magnifierVM.setMouseWheelEnabled(false);     
    }    
    
    public void mousePressedMiddle(final double x, final double y) {
      final double zoom = view.getZoom();
      magnifierVM.setMagnifierZoomFactor(Math.max(1,1/zoom));
      magnifierVM.setMagnifierRadius(200);
      view.addViewMode(magnifierVM);
      magnifierVM.mouseMoved(lastPressEvent);
      view.updateView();
    }
    
    public void mouseReleasedMiddle(final double x, final double y) {
      view.removeViewMode(magnifierVM);
    }    
  }


  /**
   * Displays tool tips for business data and inline editing controls.
   */
  private static class JOrgChartTooltipMode extends TooltipMode {
    /**
     * Overwritten to take inline editing controls into account.
     * @param x the x-coordinate of the mouse event in world coordinates.
     * @param y the y-coordinate of the mouse event in world coordinates.
     */
    public void mouseMoved( final double x, final double y ) {
      for (Iterator it = view.getDrawables().iterator(); it.hasNext();) {
        final Object drawable = it.next();
        if (drawable instanceof HoverButton) {
          final String text = ((HoverButton) drawable).getToolTipText(x, y);
          if (text != null) {
            view.setToolTipText(text);
            return;
          }
        }
      }

      super.mouseMoved(x, y);
    }

    /**
     * Returns a tool tip text with some information about an employee if the
     * level of detail is set to 
     * {@link demo.view.orgchart.JOrgChart.LODRenderingHint#VALUE_LOD_LOW} or
     * {@link demo.view.orgchart.JOrgChart.LODRenderingHint#VALUE_LOD_MEDIUM}.
     *
     * @param node the node for which the tooltip is set.
     * @return a tool tip text if the level of detail is low or medium,
     * <code>null</code> otherwise.
     */
    protected String getNodeTip(final Node node) {
      final Object lod = getLevelOfDetail();
      if (lod == LODRenderingHint.VALUE_LOD_LOW ||
          lod == LODRenderingHint.VALUE_LOD_MEDIUM) { //tiny
        if (getGraph2D().getHierarchyManager().isNormalNode(node)) {
          final Employee e = getEmployee(node);
          if (!e.vacant) {
            return "<html><b>" + e.name + "</b><br>" + e.position + "<br>Status " + e.status + "</html>";
          }
        }
      }
      return null;
    }

    /**
     * Returns a tool tip text with the employee's presence status if the
     * level of detail is set to
     * {@link demo.view.orgchart.JOrgChart.LODRenderingHint#VALUE_LOD_HIGH}
     * and the given label is the status label in the upper right corner of
     * the node.
     * @param label the label for which the tooltip is set.
     * @return a tool tip text for the status label if the level of detail is
     * high, <code>null</code> otherwise.
     */
    protected String getNodeLabelTip(final NodeLabel label) {
      final Object lod = getLevelOfDetail();
      if (lod == LODRenderingHint.VALUE_LOD_HIGH) {
        final NodeRealizer realizer = label.getRealizer();
        if (realizer.labelCount() > 6 && label == realizer.getLabel(6)) {
          final Node node = label.getNode();
          if (getGraph2D().getHierarchyManager().isNormalNode(node)) {
            final Employee e = getEmployee(node);
            return  "Status " + e.status;
          }
        }
      }
      return null;
    }

    /**
     * Retrieves the {@link Employee} instance represented by
     * the given node.
     * @return the {@link Employee} instance represented by
     * the given node.
     */
    private Employee getEmployee( final Node node ) {
      return (Employee) ((JTreeChart) view).getUserObject(node);
    }

    private Object getLevelOfDetail() {
      return view.getRenderingHints().get(LODRenderingHint.KEY_LOD);
    }
  }

  /**
   * A <code>ViewMode</code> that produces a roll-over effect for nodes
   * under the mouse cursor.
   */
  private class RollOverViewMode extends ViewMode {
    
    /** Preferred duration for roll over effect animations */
    private static final int PREFERRED_DURATION = 200;

    /** Stores the last node that was marked with the roll over effect */
    private Node lastHitNode;
    private AnimationPlayer player;
    private PropertyChangeListener pcl;
    private MouseEvent lastRelevantMouseEvent;

    public void activate(final boolean activate) {
      super.activate(activate);
      if(activate) {
        player = new AnimationPlayer();
        player.addAnimationListener(view);
        player.setBlocking(false);
        pcl = new CanvasPropertyChangeListener();
        view.getCanvasComponent().addPropertyChangeListener(pcl);
      } else if(pcl != null){        
        view.getCanvasComponent().removePropertyChangeListener(pcl);
      }
    }
    
    public void mouseExited(final MouseEvent e) {
      lastRelevantMouseEvent = null;
      if(lastHitNode != null) {
        unmark(lastHitNode);
      }
    }
    
    public void mouseDragged(final MouseEvent e) {
      lastRelevantMouseEvent = e;
      super.mouseDragged(e);
    }

    public void mouseMoved(final MouseEvent e) {
      lastRelevantMouseEvent = e;
      super.mouseMoved(e);
    }

    /**
     * Triggers a roll-over effect for the first node at the specified location.
     */
    public void mouseMoved( final double x, final double y ) {      
      final HitInfo hi = getHitInfo(x, y);
      
      if (hi.hasHitNodes()) {
        final Node node = (Node) hi.hitNodes().current();
        if (node != lastHitNode) {
          unmark(lastHitNode);
        }
        final JOrgChart treeView = (JOrgChart) view;
        final Object userObject = treeView.getUserObject(node);
        if(userObject != null && !treeView.highlightMap.getBool(node)) {
          mark(node);
          lastHitNode = node;
        }
      } else {
        unmark(lastHitNode);
        lastHitNode = null;
      }
    }
    
     /**
     * Overridden to take only nodes into account for hit testing.
     */
    protected HitInfo getHitInfo( final double x, final double y ) {
      final HitInfo hi = DemoBase.checkNodeHit(view, x, y);
      setLastHitInfo(hi);
      return hi;
    }
        
    /**
     * Triggers a <em>mark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>MARKED</em>.
     */
    protected void mark( final Node node ) {
      // only start a mark animation if no other animation is playing
      // for the given node
      final JOrgChart treeView = (JOrgChart) view;
      final boolean highlighted = treeView.highlightMap.getBool(node);
      final Object userObject = treeView.getUserObject(node);
      
      if(userObject != null && !highlighted) {
        treeView.highlightMap.setBool(node, true);
        
        final NodeRealizer nr = getGraph2D().getRealizer(node);
        final NodeRealizer newTheme = nr.createCopy();
        configureColors(newTheme, getGraph2D().isSelected(node), true);
        
        final ViewAnimationFactory animFac = new ViewAnimationFactory(view);
        final AnimationObject ao = animFac.color(
                nr,
                newTheme.getFillColor(), newTheme.getFillColor2(), newTheme.getLineColor(),
                ViewAnimationFactory.APPLY_EFFECT, PREFERRED_DURATION);

        final AnimationObject eao = AnimationFactory.createEasedAnimation(ao);        
        player.animate(eao); 
      }     
    }

    class CanvasPropertyChangeListener implements PropertyChangeListener {
      public void propertyChange(final PropertyChangeEvent evt) {
        if(lastRelevantMouseEvent != null && ("Zoom".equals(evt.getPropertyName()) || "ViewPoint".equals(evt.getPropertyName()))) {
          if(lastHitNode != null && getHitInfo(lastRelevantMouseEvent).getHitNode() != lastHitNode) {
            unmark(lastHitNode);
            lastHitNode = null;
          }
          else if(lastHitNode == null && lastRelevantMouseEvent != null) {
            lastHitNode = getHitInfo(lastRelevantMouseEvent).getHitNode();
            if(lastHitNode != null) {
              mark(lastHitNode);
            }
          }
        }          
      }        
    }
    
    /**
     * Triggers an <em>unmark</em> animation for the specified node.
     * Sets the animation state of the given node to <em>UNMARKED</em>.
     */
    protected void unmark( final Node node ) {
      if (node == null || node.getGraph() == null) {
        player.stop();
        return;
      }
      final JOrgChart treeView = (JOrgChart) view;
      Object userObject = null;      
      try {
        userObject = treeView.getUserObject(node);
      }
      catch(Exception ex) {
        D.bug(node);
      }
      if(userObject != null && treeView.highlightMap.getBool(node)) {
        treeView.highlightMap.setBool(node, false);
    
        player.stop();
        configureColors(getGraph2D().getRealizer(node), getGraph2D().isSelected(node), false);
        getGraph2D().updateViews();
      }      
    }    
  }

  /**
   * {@link y.view.Graph2DSelectionListener} that assigns a properly configured 
   * realizer to a node whenever its selected state changes.
   */
  class TreeChartSelectionListener implements Graph2DSelectionListener {
    public void onGraph2DSelectionEvent(final Graph2DSelectionEvent ev) {
      if(ev.getSubject() instanceof Node) {
        final Node node = (Node) ev.getSubject();
        final Object userObject = getUserObject(node);
        if(userObject != null) {
          final Graph2D graph = getGraph2D();
          configureColors(getGraph2D().getRealizer(node), graph.isSelected(node), highlightMap.getBool(node));
          graph.updateViews();
        }
      }
    }    
  }
  
  /**
   * Calculate a layout for organization charts. The layouter takes the Employee's <code>layout</code> 
   * and <code>assistant</code> properties into account.  
   */
  protected Layouter createLayouter() {
    final OrgChartLayouter layouter = new OrgChartLayouter();
    final Graph2D graph = getGraph2D();
    final DataProvider childLayoutDP = new DataProviderAdapter() {
      public Object get(final Object n) {
        final Employee employee = (Employee) getUserObject((Node)n);
        if(employee != null) {
          if("leftHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_LEFT_BELOW;
          }
          if("rightHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_RIGHT_BELOW;
          }
          if("bothHanging".equals(employee.layout)) {
            return OrgChartLayouter.CHILD_LAYOUT_BELOW;
          }
        }
        return OrgChartLayouter.CHILD_LAYOUT_SAME_LAYER;
      }          
    };
    graph.addDataProvider(OrgChartLayouter.CHILD_LAYOUT_DPKEY, childLayoutDP);
    
    final DataProvider assistantDP = new DataProviderAdapter() {
      public boolean getBool(final Object n) {
        final Employee employee = (Employee) getUserObject((Node)n);
        return employee != null && employee.assistant;
      }
    };      
    graph.addDataProvider(OrgChartLayouter.ASSISTANT_DPKEY, assistantDP);
    
    layouter.setDuplicateBendsOnSharedBus(true);

    return new NormalizingGraphElementOrderStage(layouter);
  }  
}
