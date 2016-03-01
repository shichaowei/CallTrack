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
package demo;

import demo.browser.ConfigurationException;
import demo.browser.Demo;
import demo.browser.Displayable;
import demo.browser.Driver;
import demo.browser.DriverEventQueue;
import demo.browser.DriverFactory;
import demo.browser.DriverInstantiationException;
import demo.browser.DriverSecurityManager;
import demo.browser.ExceptionHandler;
import demo.browser.FilterableTree;
import demo.browser.SyntaxMarker;
import demo.browser.XmlTreeBuilder;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Locale;

/**
 * This class is not meant to be viewed as source code demo, instead run the Ant build.xml file in the same directory.
 * This class serves as a launcher/browser for the various demos that are included with this distribution.
 * For these to run, make sure that the sources themselves or at least the *.xsl files, the *.graphml files, the *.gml files, the *.ygf
 * files, the *.xml files, and the *.properties files are visible in your classpath.
 * So please use the ant build file in order to run the demos, since the build file satisfies all of the above requirements.
 *
 */
public class DemoBrowser {
  static final String APPLICATION_ROOT = "com.yworks.demo.appRoot";
  private static final String PLAY_BUTTON = "/demo/browser/resource/start.png";
  private static final String STOP_BUTTON = "/demo/browser/resource/stop.png";
  private static final String NEXT_BUTTON = "/demo/browser/resource/next.png";
  private static final String FRAME_ICON  = "/demo/browser/resource/yicon32.png";
  private static final String CONFIGURATION = "condensed.xml";
  private static final String[] CONFIGURATION_PATHS = {
      "/",
      "/demo/",
      "/demo/browser/",
      "/demo/browser/resource/"
  };
  private static final String DISPLAY_PANE_ID = "displayPane";
  private static final String ERROR_PANE_ID = "errorPane";
  private static final int NO_TAB_INDEX = -1;
  private static final int DOCUMENTATION_TAB_INDEX = 0;
  private static final int SOURCE_TAB_INDEX = 1;

  private final DriverEventQueue eventQueue;
  private final ExecutionExceptionHandler exceptionHandler;
  private JRootPane driverPane;
  private JEditorPane documentationPane;
  private JEditorPane sourcePane;
  private JTextArea errorPane;
  private JPanel displayPane;
  private CardLayout displayLayout;
  private JFrame frame;

  private final SecurityManager backupSecurityManager;
  private final String BROWSER_TITLE = "yFiles Demo Browser";

  private boolean autoExecute;

  private DemoBrowser() {
    this.eventQueue = new DriverEventQueue();
    this.exceptionHandler = new ExecutionExceptionHandler();
    this.backupSecurityManager = System.getSecurityManager();
  }

  private void registerEventQueue() {
    Toolkit.getDefaultToolkit().getSystemEventQueue().push( eventQueue );
  }

  private void setVisible( final boolean visible ) {
    frame = new JFrame( BROWSER_TITLE );
    frame.setName( APPLICATION_ROOT );
    frame.setIconImage( getFrameIcon() );
    //reset Security Manager, to avoid catching of System.exit, when called from root
    frame.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
        if ( backupSecurityManager != null ) {
          System.setSecurityManager( backupSecurityManager );
        } else {
          //no security manager was set, just allow everything
          System.setSecurityManager( new SecurityManager() {
            public void checkPermission( final Permission perm ) {
            }
          }
          );
        }
        System.exit( 0 );
      }
    }

    );
    frame.setContentPane( createContentPane() );
    frame.pack();
    frame.setLocationRelativeTo( null );
    frame.setVisible( visible );
  }

  private Image getFrameIcon() {
    try {
      final URL resource = getClass().getResource(FRAME_ICON);
      if (resource != null) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getImage(resource);
      }
    } catch (Exception e) {
      // prevent frame icon retrieval from crashing the demo browser and
      // fallback to the default icon when exceptions occur
    }

    return null;
  }

  private synchronized void setAutoExecute() {
    autoExecute = true;
  }

  private synchronized boolean consumeAutoExecute() {
    if (autoExecute) {
      autoExecute = false;
      return true;
    } else {
      return false;
    }
  }

  private boolean isRunning() {
    return eventQueue.getDriver() != null;
  }

  private void execute( final Demo demo ) {
    if ( demo == null ) {
      return;
    }

    final DriverFactory factory = new DriverFactory( frame, driverPane );
    factory.setExceptionHandler( exceptionHandler );
    try {
      final Driver driver = factory.createDriverForClass( demo.getQualifiedName() );
      eventQueue.setDriver( driver );
      driver.start();
    } catch ( DriverInstantiationException die ) {
      exceptionHandler.handleException( die );
    }
  }

  private void dispose() {
    if ( isRunning() ) {
      eventQueue.getDriver().dispose();
      eventQueue.setDriver( null );
    }
  }

  private JComponent createContentPane() {
    final Dimension preferredSizeOfDisplay = new Dimension( 600, 400 );

    driverPane = new JRootPane();
    driverPane.setPreferredSize( preferredSizeOfDisplay );
    documentationPane = new AntiAliasingEditorPane("text/html", "");
    sourcePane = new AntiAliasingEditorPane("text/html", "");
    documentationPane.setEditable( false );
    documentationPane.setEnabled( true );

    sourcePane.setEditable( false );
    sourcePane.setEnabled( true );

    errorPane = new JTextArea();
    errorPane.setEditable( false );
    errorPane.setFocusable( false );

    displayLayout = new CardLayout();
    displayPane = new JPanel( displayLayout );

    TreeNode root = new DefaultMutableTreeNode();

    XmlTreeBuilder config = null;
    for ( int i = 0; i < CONFIGURATION_PATHS.length; ++i ) {
      config = XmlTreeBuilder.newInstance( CONFIGURATION_PATHS[i] + CONFIGURATION );
      if ( config != null ) {
        try {
          root = config.buildDemoTree();
          break;
        } catch ( ConfigurationException ce ) {
          ce.printStackTrace();
        }
      }
    }
    if ( config == null ) {
      System.err.println( "Could not locate resource: " + CONFIGURATION );
      System.exit( 1 );
    }

    final JScrollPane docScrollPane = new JScrollPane(documentationPane);
    docScrollPane.setBorder(BorderFactory.createEmptyBorder());

    final JScrollPane sourceScrollPane = new JScrollPane(this.sourcePane);
    sourceScrollPane.setBorder(BorderFactory.createEmptyBorder());

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add( "Documentation", docScrollPane );
    tabbedPane.add( "Source", sourceScrollPane );
    tabbedPane.setTabPlacement(SwingConstants.BOTTOM);
    tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, false );
    tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, false );

    final FilterableTree tree = new FilterableTree( root );

    final JScrollPane treePane = new JScrollPane( tree );
    treePane.setPreferredSize( new Dimension( 200, 600 ) );
    treePane.setBorder( BorderFactory.createEmptyBorder() );

    final JSplitPane jsp =
        new JSplitPane( JSplitPane.VERTICAL_SPLIT, driverPane, tabbedPane );
    jsp.setOneTouchExpandable( true );
    jsp.setContinuousLayout( true );
    jsp.setDividerLocation( 0.5 );
    jsp.setBorder( BorderFactory.createEmptyBorder() );

    final JButton start = new JButton( "Start" );
    final URL startIconResource = getClass().getResource( PLAY_BUTTON );
    if ( startIconResource != null ) {
      start.setIcon( new ImageIcon( startIconResource ) );
      start.setMargin( new Insets( 0, 0, 0, 0 ) );

      // only delete the button text if the GIF was successfully read
      final Icon icon = start.getIcon();
      if ( icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ) {
        start.setText( "" );
      }
    }
    start.setToolTipText( "Starts the currently selected Demo." );
    start.setEnabled( false );

    final JButton stop = new JButton( "Stop" );
    final URL stopIconResource = getClass().getResource( STOP_BUTTON );
    if ( stopIconResource != null ) {
      stop.setIcon( new ImageIcon( stopIconResource ) );
      stop.setMargin( new Insets( 0, 0, 0, 0 ) );

      // only delete the button text if the GIF was successfully read
      final Icon icon = stop.getIcon();
      if ( icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ) {
        stop.setText( "" );
      }
    }
    stop.setToolTipText( "Stops the currently selected Demo." );
    stop.setEnabled( false );

    stop.addActionListener( new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        dispose();
        final Displayable displayable = tree.getSelectedDisplayable();
        if ( !tree.isSelectionEmpty() &&
            displayable.isDemo() &&
            displayable.isExecutable() ) {
          start.setEnabled( true );
        }
        stop.setEnabled( false );
      }
    } );
    start.addActionListener( new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        final Demo demo = tree.getSelectedDemo();
        if ( demo.isExecutable() ) {
          if ( !isRunning() ) {
            stop.setEnabled( true );
            start.setEnabled( false );
            execute( demo );
            if ( jsp.getDividerLocation() < 10 ) {
              jsp.setDividerLocation( 0.75 );
            }
          }
        }
      }
    } );

    final JButton next = new JButton( "Next" );
    final URL nextIconResource = getClass().getResource( NEXT_BUTTON );
    if ( nextIconResource != null ) {
      next.setIcon( new ImageIcon( nextIconResource ) );
      next.setMargin( new Insets( 0, 0, 0, 0 ) );

      // only delete the button text if the GIF was successfully read
      final Icon icon = next.getIcon();
      if ( icon != null && icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ) {
        next.setText( "" );
      }
    }
    next.setToolTipText( "Starts the next Demo." );
    next.setEnabled( true );
    next.addActionListener( new ActionListener() {
      public void actionPerformed( final ActionEvent e ) {
        dispose();
        stop.setEnabled( false );
        start.setEnabled( false );

        if ( tree.isSelectionEmpty() ) {
          final TreeNode first = tree.findNodeForFirstExecutable();
          if ( first != null ) {
            selectDemo(first);
          }
        } else {
          final TreeNode next = tree.findNodeForNextExecutable();
          if ( next == null ) {
            final Displayable displayable = tree.getSelectedDisplayable();
            start.setEnabled( displayable.isDemo() && displayable.isExecutable() );
          } else {
            selectDemo(next);
          }
        }
      }

      private void selectDemo( final TreeNode node ) {
        setAutoExecute();
        final TreePath path = FilterableTree.createPath( node );
        tree.scrollPathToVisible( path );
        tree.setSelectionPath( path );
      }
    } );

    final JToolBar toolBar = new JToolBar();
    toolBar.add( next );
    toolBar.add( start );
    toolBar.add( stop );

    final JComponent controls = tree.createFilterControls();
    if (controls.getComponentCount() > 0) {
      toolBar.addSeparator();
      while (controls.getComponentCount() > 0) {
        toolBar.add(controls.getComponent(0));
      }
    }

    final JPanel controlPane = new JPanel( new BorderLayout() );
    controlPane.add( treePane, BorderLayout.CENTER );
    controlPane.add( toolBar, BorderLayout.NORTH );

    displayPane.add( new JScrollPane( errorPane ), ERROR_PANE_ID );
    displayPane.add( jsp, DISPLAY_PANE_ID );
    displayPane.setPreferredSize( preferredSizeOfDisplay );

    final Trigger trigger = new Trigger( start, stop, jsp, tabbedPane, tree );
    tree.addMouseListener( trigger );
    tree.addTreeSelectionListener( trigger );
    tabbedPane.addChangeListener( trigger );
    documentationPane.addHyperlinkListener( trigger );
    tree.setSelectionRow( 0 );

    final JSplitPane contentPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, controlPane, displayPane );
    contentPane.setBorder( BorderFactory.createEmptyBorder() );
    return contentPane;
  }

  final class ExecutionExceptionHandler implements ExceptionHandler {
    public void handleException( final Exception ex ) {
      if ( causedBySystemExit( ex ) ) {
        dispose();
      } else {
        final Throwable work;
        if ( ex instanceof InvocationTargetException ) {
          work = ex.getCause();
        } else {
          work = ex;
        }
        if ( errorPane != null ) {
          final StringWriter sw = new StringWriter();
          final PrintWriter pw = new PrintWriter( sw );
          work.printStackTrace( pw );
          print( sw.toString() );
        } else {
          work.printStackTrace();
        }
      }
    }

    private boolean causedBySystemExit( final Exception ex ) {
      for ( Throwable t = ex; t != null; t = t.getCause() ) {
        if ( t instanceof SecurityException &&
            DriverSecurityManager.HANDLE_EXIT_VM.equals( t.getMessage() ) ) {
          return true;
        }
      }
      return false;
    }

    private void print( final String s ) {
      if ( !EventQueue.isDispatchThread() ) {
        EventQueue.invokeLater( new Runnable() {
          private final String text = s;

          public void run() {
            errorPane.setText( text );
            displayLayout.show( displayPane, ERROR_PANE_ID );
          }
        } );
      } else {
        errorPane.setText( s );
        displayLayout.show( displayPane, ERROR_PANE_ID );
      }
    }
  }

  final class Trigger extends MouseAdapter
      implements ChangeListener, TreeSelectionListener, HyperlinkListener {
    private final JButton start;
    private final JButton stop;
    private final JSplitPane splitPane;
    private final JTabbedPane tabbedPane;
    private final FilterableTree tree;

    Trigger( final JButton start,
             final JButton stop,
             final JSplitPane splitPane,
             final JTabbedPane tabbedPane,
             final FilterableTree tree ) {
      this.start = start;
      this.stop = stop;
      this.splitPane = splitPane;
      this.tabbedPane = tabbedPane;
      this.tree = tree;
    }

    public void mouseClicked( final MouseEvent me ) {
      if ( SwingUtilities.isLeftMouseButton( me ) ) {
        if ( me.getClickCount() == 2 ) {
          if ( !tree.isSelectionEmpty() ) {
            displayLayout.show( displayPane, DISPLAY_PANE_ID );
            final Displayable displayable = tree.getSelectedDisplayable();
            if ( displayable.isDemo() && displayable.isExecutable() ) {
              tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, true );
              tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, true );
              if ( !isRunning() ) {
                stop.setEnabled( true );
                start.setEnabled( false );
                execute( tree.getSelectedDemo() );
                if ( splitPane.getDividerLocation() < 10 ) {
                  splitPane.setDividerLocation( 0.75 );
                }
              }
            }
          }
        }
      } else if ( SwingUtilities.isRightMouseButton( me ) ) {
        final TreePath path = tree.getPathForLocation( me.getX(), me.getY() );
        if ( path != null ) {
          final TreeNode root = ( TreeNode) path.getLastPathComponent();
          if ( root != null ) {
            final JPopupMenu pm = new JPopupMenu();
            pm.add( new AbstractAction( "Expand Children" ) {
              public void actionPerformed( final ActionEvent ae ) {
                for ( Enumeration en = root.children(); en.hasMoreElements(); ) {
                  final TreeNode child = ( TreeNode ) en.nextElement();
                  tree.expandPath( FilterableTree.createPath( child ) );
                }
                pm.setVisible( false );
              }
            } );
            pm.add( new AbstractAction( "Collapse Children" ) {
              public void actionPerformed( final ActionEvent ae ) {
                for ( Enumeration en = root.children(); en.hasMoreElements(); ) {
                  final TreeNode child = ( TreeNode ) en.nextElement();
                  tree.collapsePath( FilterableTree.createPath( child ) );
                }
                pm.setVisible( false );
              }
            } );
            pm.show( tree, me.getX(), me.getY() );
          }
        }
      }
    }

    public void valueChanged( final TreeSelectionEvent e ) {
      if ( !tree.isSelectionEmpty() ) {
        displayLayout.show( displayPane, DISPLAY_PANE_ID );

        final Displayable displayable = tree.getSelectedDisplayable();
        final boolean isDemo = displayable.isDemo();
        final boolean isExecutable = displayable.isExecutable();

        frame.setTitle(BROWSER_TITLE + " [" + displayable.getDisplayName() + "]");
        tabbedPane.setEnabledAt( DOCUMENTATION_TAB_INDEX, true );
        tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, isDemo );

        final boolean isRunning = isRunning();
        stop.setEnabled( isRunning );
        start.setEnabled( isExecutable && !isRunning );

        final int selectedTab = tabbedPane.getSelectedIndex();
        if ( isDemo ) {
          dispose();
          if ( ( isRunning || consumeAutoExecute() ) && isExecutable ) {
            stop.setEnabled( true );
            start.setEnabled( false );
            execute( ( Demo ) displayable );
            if ( splitPane.getDividerLocation() < 10 ) {
              splitPane.setDividerLocation( 0.75 );
            }
          }
          if ( selectedTab == NO_TAB_INDEX ) {
            tabbedPane.setSelectedIndex( DOCUMENTATION_TAB_INDEX );
          } else {
            updateTabs();
          }
        } else {
          dispose();
          if ( isRunning ) {
            stop.setEnabled( false );
            start.setEnabled( false );
          }
          splitPane.setDividerLocation( 0 );
          if ( selectedTab != DOCUMENTATION_TAB_INDEX ) {
            tabbedPane.setSelectedIndex( DOCUMENTATION_TAB_INDEX );
          } else {
            updateTabs();            
          }
        }
      } else {
        frame.setTitle(BROWSER_TITLE);
        tabbedPane.setEnabledAt(DOCUMENTATION_TAB_INDEX, false);
        tabbedPane.setEnabledAt( SOURCE_TAB_INDEX, false );
        tabbedPane.setSelectedIndex( NO_TAB_INDEX );
        documentationPane.setText( "<html></html>" );
        sourcePane.setText("");
        errorPane.setText("");
        displayLayout.show( displayPane, ERROR_PANE_ID );
        start.setEnabled( false );
        stop.setEnabled( false );
      }
    }

    public void stateChanged( final ChangeEvent e ) {
      updateTabs();
    }

    public void hyperlinkUpdate( final HyperlinkEvent e ) {
      if ( HyperlinkEvent.EventType.ACTIVATED == e.getEventType() ) {
        URL target = e.getURL();
        if ( target != null ) {
          URL base = ( ( HTMLDocument ) ( ( JEditorPane ) e.getSource() ).getDocument() ).getBase();
          try {
            String baseStr = base.toString();
            if ( !baseStr.endsWith( "/" ) ) baseStr += "/";
            target = new URL( baseStr + e.getDescription() );
          } catch ( MalformedURLException mux ) {
            // ignore - triggers an NPE later on
          }

          String qn = target.getPath();
          if ( qn.toLowerCase().endsWith( "/readme.html" ) ) {
            qn = qn.substring( 0, qn.length() - 12 );
          } else if ( qn.toLowerCase().endsWith( ".java" ) ) {
            qn = qn.substring( 0, qn.length() - 5 );
          }
          qn = qn.replace( '/', '.' );

          int i = qn.indexOf( "demo." );
          if ( i > 0 ) qn = qn.substring( i, qn.length() );

          if ( qn.length() > 0 ) {
            final TreeNode refNode = tree.findNodeFor( qn );
            if ( refNode != null ) {
              tree.setSelectionPath( FilterableTree.createPath( refNode ) );
            }
          }
        }
      }
    }

    private void updateTabs() {
      if ( tree.isSelectionEmpty() ) {
        return;
      }

      switch ( tabbedPane.getSelectedIndex() ) {
        case DOCUMENTATION_TAB_INDEX: {
          setDescription( tree.getSelectedDisplayable(), documentationPane );
          break;
        }
        case SOURCE_TAB_INDEX: {
          setSource( tree.getSelectedDemo(), sourcePane );
          break;
        }
      }
    }
  }

  public static void main( final String[] args ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        Locale.setDefault(Locale.ENGLISH);
        initLnF();
        createAndShowGUI();
      }
    } );
  }

  /**
   * Initializes to a "nice" look and feel for GUI demo applications.
   */
  public static void initLnF() {
    try {
      // check for 'os.name == Windows 7' does not work, since JDK 1.4 uses the compatibility mode
      if (!"com.sun.java.swing.plaf.motif.MotifLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(UIManager.getSystemLookAndFeelClassName())
          && !UIManager.getSystemLookAndFeelClassName().equals(UIManager.getLookAndFeel().getClass().getName())
          && !(System.getProperty("java.version").startsWith("1.4") && System.getProperty("os.name").startsWith(
          "Windows") && "6.1".equals(System.getProperty("os.version")))) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void createAndShowGUI() {
    final DemoBrowser browser = new DemoBrowser();
    browser.registerEventQueue();
    browser.setVisible( true );
  }

  private static void setSource( final Demo demo, final JEditorPane pane ) {
    if ( demo != null ) {
      String source = demo.getSource();
      if ( source == null ) {
        source = demo.readSource();
        source = SyntaxMarker.toHtml( source );
      }
      pane.setText( source );
    } else {
      pane.setText( "" );
    }
    pane.setCaretPosition( 0 );
  }

  private static void setDescription( final Displayable displayable, final JEditorPane pane ) {
    String desc = "<html></html>";
    if ( displayable != null ) {
      desc = displayable.getDescription();
      final Document doc = pane.getDocument();
      if ( doc instanceof HTMLDocument ) {
        final URL base = displayable.getBase();
        if ( base != null ) {
          ( ( HTMLDocument ) doc ).setBase( base );
        }
      }
    }
    pane.setText( desc );
    pane.setCaretPosition( 0 );
  }


  private static final class AntiAliasingEditorPane extends JEditorPane {

    private final boolean useDefaultAA;

    AntiAliasingEditorPane(final String type, final String text) {
      super(type, text);
      final String javaVersion = System.getProperty("java.version");
      final double version = Double.parseDouble(javaVersion.substring(0, 3));
      useDefaultAA = version > 1.599;
    }

    protected void paintComponent(final Graphics g) {
      if (useDefaultAA) {
        super.paintComponent(g);
      } else {
        final Graphics2D gfx = (Graphics2D) g;
        final Object oldAAHint = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            oldAAHint != null
                ? oldAAHint
                : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
      }
    }
  }
}