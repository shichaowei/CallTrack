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
package demo.layout.organic;

import demo.view.DemoBase;
import demo.view.DemoDefaults;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.layout.organic.OrganicLayouter;
import y.module.GRIPModule;
import demo.layout.module.OrganicEdgeRouterModule;
import demo.layout.module.OrganicLayoutModule;
import demo.layout.module.SmartOrganicLayoutModule;
import y.module.YModule;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * Demonstrates different organic layout algorithms and 
 * how to specify individual preferred edge lengths 
 * for OrganicLayouter.  
 * <br>
 * In this demo the edge lengths can be specified by right clicking 
 * on an edge or applying the current node distances using the button from the 
 * toolbar.
 * <br>
 * Choose the item "Edit Preferred Edge Length" from the context menu to open up 
 * a label editor that allows for entering a value for the edge length in pixels.
 * Note that the entered value must be numeric. Otherwise 
 * a default length will be chosen.
 *
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/smart_organic_layouter.html">Section Organic Layout Style</a> in the yFiles for Java Developer's Guide
 * @see <a href="http://docs.yworks.com/yfiles/doc/developers-guide/organic_edge_router.html">Section Organic Edge Routing</a> in the yFiles for Java Developer's Guide
 */
public class OrganicLayouterDemo extends DemoBase {
  EdgeMap preferredEdgeLengthMap;
  YModule module;

  public OrganicLayouterDemo() {
    preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
    view.getGraph2D().addDataProvider(OrganicLayouter.PREFERRED_EDGE_LENGTH_DATA, preferredEdgeLengthMap);
    module = new SmartOrganicLayoutModule();
    loadGraph("resource/organic.graphml");
    DemoDefaults.applyRealizerDefaults(view.getGraph2D(), true, true);
  }

  protected void registerViewModes() {
    EditMode editMode = new EditMode();
    view.addViewMode(editMode);

    editMode.setPopupMode(new PopupMode() {
      public JPopupMenu getEdgePopup(Edge e) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new EditLabel(e));
        return pm;
      }
    });
  }

  /**
   * Returns ViewActionDemo toolbar plus actions to trigger some layout algorithms 
   */
  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();

    bar.addSeparator();
    bar.add(createActionControl(new LayoutAction()));
    bar.add(createActionControl(new OptionAction()));
    bar.addSeparator();
    bar.add(new AssignLengthsAction());
    return bar;
  }

  protected JMenuBar createMenuBar() {
    JMenuBar mb = super.createMenuBar();
    JMenu layoutMenu = new JMenu("Style");
    ButtonGroup bg = new ButtonGroup();
    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new OrganicLayoutModule();
      }
    };
    JRadioButtonMenuItem item = new JRadioButtonMenuItem("Classic");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new SmartOrganicLayoutModule();
      }
    };
    item = new JRadioButtonMenuItem("Smart");
    item.addActionListener(listener);
    item.setSelected(true);
    bg.add(item);
    layoutMenu.add(item);

    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new GRIPModule();
      }
    };
    item = new JRadioButtonMenuItem("GRIP");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        module = new OrganicEdgeRouterModule();
      }
    };
    item = new JRadioButtonMenuItem("EdgeRouting");
    item.addActionListener(listener);
    bg.add(item);
    layoutMenu.add(item);
    mb.add(layoutMenu);
    return mb;
  }

  /**
   * Displays the layout options for organic layouter
   */
  class OptionAction extends AbstractAction {
    OptionAction() {
      super("Settings...", getIconResource("resource/properties.png"));
    }

    public void actionPerformed(ActionEvent e) {
      OptionSupport.showDialog(module, view.getGraph2D(), false, view.getFrame());
    }
  }

  /**
   *  Launches the OrganicLayouter.
   */
  class LayoutAction extends AbstractAction {
    LayoutAction() {
      super("Layout", SHARED_LAYOUT_ICON);
    }

    public void actionPerformed(ActionEvent e) {
      //update preferredEdgeLengthData before launching the module
      Graph2D graph = view.getGraph2D();
      for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
        Edge edge = ec.edge();
        String eLabel = graph.getLabelText(edge);
        preferredEdgeLengthMap.set(edge, null);
        try {
          preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
        }
        catch (Exception ex) {
        }
      }

      //start the module
      module.start(view.getGraph2D());
    }
  }

  /**
   * Action that opens a text editor for the label of an edge
   * <p>
   * The inlined label editor allows to enter a single line of
   * label text for an edge. To terminate the label editor 
   * press "Enter".
   */
  class EditLabel extends AbstractAction {
    Edge e;

    EditLabel(Edge e) {
      super("Edit Preferred Length");
      this.e = e;
    }

    public void actionPerformed(ActionEvent ev) {

      final EdgeRealizer r = view.getGraph2D().getRealizer(e);
      final YLabel label = r.getLabel();

      view.openLabelEditor(label,
          label.getBox().getX(),
          label.getBox().getY(),
          null, true);
    }
  }

  class AssignLengthsAction extends AbstractAction {
    AssignLengthsAction() {
      super("Assign Preferred Length");
      putValue(Action.SHORT_DESCRIPTION, "Set the preferred length of each edge to its current geometric length");
    }

    public void actionPerformed(ActionEvent e) {
      Graph2D g = view.getGraph2D();
      g.firePreEvent();
      g.backupRealizers(g.edges());
      try {
        for (EdgeCursor ec = g.edges(); ec.ok(); ec.next()) {
          NodeRealizer snr = g.getRealizer(ec.edge().source());
          NodeRealizer tnr = g.getRealizer(ec.edge().target());
          double deltaX = snr.getCenterX() - tnr.getCenterX();
          double deltaY = snr.getCenterY() - tnr.getCenterY();
          double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
          EdgeRealizer er = g.getRealizer(ec.edge());
          er.getLabel().setText(Integer.toString((int) dist));
        }
        g.updateViews();
      } finally {
        g.firePostEvent();
      }
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        new OrganicLayouterDemo().start("Organic Layouter Demo");
      }
    });
  }
}


      
