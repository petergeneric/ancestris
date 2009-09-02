/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell;

import gj.geom.ShapeHelper;
import gj.io.GraphReader;
import gj.io.GraphWriter;
import gj.layout.GraphLayout;
import gj.layout.LayoutContext;
import gj.layout.LayoutException;
import gj.shell.factory.AbstractGraphFactory;
import gj.shell.model.EditableGraph;
import gj.shell.model.EditableVertex;
import gj.shell.swing.Action2;
import gj.shell.swing.SwingHelper;
import gj.shell.util.Properties;
import gj.shell.util.ReflectHelper;
import gj.util.DefaultLayoutContext;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A Shell for GraphJ's core functions
 */
public class Shell {

  /** the Shapes we know */
  public final static Shape[] shapes = new Shape[] {
    new Rectangle2D.Double(-16,-16,32,32),
    ShapeHelper.createShape(23D,19D,1,1,new double[]{
      0,6,4,1,18,11,1,29,1,1,34,30,1,47,27,1,31,44,1,20,24,1,6,38,1,2,21,1,14,20,1,6,4
    }),
    ShapeHelper.createShape(15D,15D,1,1,new double[] {
      0,10,0,
      1,20,0,
       2,30,0,30,10, // tr : quad
      1,30,20,
       2,20,20,20,30, // br : quad
      1,10,30,
       3,20,20,10,10,0,20, // bl : cubic
      1,0,10,
       3,-10,0,0,-10,10,0 // tl : cubic
    }),
    new Ellipse2D.Double(-20,-10,40,20)
  };
  
  /** the frame we use */
  private JFrame frame;
  
  /** the graph widget we show */
  private EditableGraphWidget graphWidget;

  /** the layout widget we show */
  private LayoutWidget layoutWidget;
  
  /** the graph we're looking at */
  private EditableGraph graph;
  
  /** the log output */
  private JTextArea logWidget;
  
  /** debug flag */
  private boolean isDebug = false; 
  
  /** whether we perform an animation after layout */
  private boolean isAnimation = true;
  
  /** an animation that is going on */
  private Animation animation;
  
  /** our properties */
  private Properties properties = new Properties(Shell.class, "shell.properties");
  
  /** our factories */
  private AbstractGraphFactory[] factories = (AbstractGraphFactory[])properties.get("factory", new AbstractGraphFactory[0]);
  
  /** our layouts */
  private GraphLayout[] layouts = (GraphLayout[])properties.get("layout", new GraphLayout[0]);
  
  private Logger logger = Logger.getLogger("genj.shell");
  
  /**
   * MAIN
   */
  public static void main(String[] args) {
    
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable t) {
    }
    
    new Shell(args.length>0?args[0]:null, args.length>1?"-maximized".equals(args[1])?true:false:false);
  }
  
  /**
   * Constructor
   */
  private Shell(String preload, boolean maximized) {
    
    // Create our widgets
    graphWidget = new EditableGraphWidget() {
      @Override
      protected void layoutConfiguredNotify(GraphLayout layout) {
        new ActionExecuteLayout().trigger();
      }
    };
    
    layoutWidget = new LayoutWidget();
    layoutWidget.setLayouts(layouts);
    layoutWidget.setBorder(BorderFactory.createEtchedBorder());
    layoutWidget.addActionListener(new ActionExecuteLayout());
    
    logWidget = new JTextArea(3, 0);
    logWidget.setEditable(false);
    logger.setLevel(Level.ALL);
    logger.addHandler(new Handler() {
      @Override
      public void publish(LogRecord record) {
        String msg = record.getMessage();
        Object[] parms = record.getParameters();
        if (parms!=null&&parms.length==0)
          msg = MessageFormat.format(msg, parms);
        logWidget.append(String.format("%-8s%s\n",record.getLevel(), msg));
      }
      @Override
      public void close() throws SecurityException { }
      @Override
      public void flush() { }
    });
    
    // Create our frame
    frame = new JFrame("GraphJ - Shell") {
      @Override
      public void dispose() {
        super.dispose();
        System.exit(0);
      }
    };
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Container container = frame.getContentPane();
    container.setLayout(new BorderLayout());
    container.add(new JScrollPane(graphWidget), BorderLayout.CENTER);
    container.add(layoutWidget, BorderLayout.EAST  );
    container.add(new JScrollPane(logWidget), BorderLayout.SOUTH );
    
    // Our default button
    frame.getRootPane().setDefaultButton(layoutWidget.getDefaultButton());
    
    // Setup Menu
    frame.setJMenuBar(createMenu());
    
    // Show the whole thing
    frame.setBounds(128,128,480,480);
    if (maximized)
    	frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);
    
    // Start with a Graph or load a preset
    if (preload!=null) { 
      SwingUtilities.invokeLater(new ActionLoadGraph(new File(preload)));
    } else {
      setGraph(factories[0]);
    }
    
    // Done
  }
  
  /** 
   * Helper that creates the MenuBar
   */
  private JMenuBar createMenu() {

    // GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH GRAPH 
    JMenu mNew = new JMenu("New");
    for (int i=0;i<factories.length;i++) {
      mNew.add(new ActionNewGraph(factories[i]));
    }
    
    JMenu mGraph = new JMenu("Graph");
    mGraph.add(mNew);
    mGraph.add(new ActionLoadGraph());
    mGraph.add(new ActionSaveGraph());
    mGraph.add(new ActionCloseGraph());

    // LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT LAYOUT 
    JMenu mLayout = new JMenu("Layout");
    for (int i=0;i<layouts.length;i++) {
      mLayout.add(new ActionSelectLayout(layouts[i]));
    }
    
    // OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS OPTIONS 
    JMenu mOptions = new JMenu("Options");
    mOptions.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleAntialias()));
    mOptions.add(SwingHelper.getCheckBoxMenuItem(new ActionToggleAnimation()));
    mOptions.add(SwingHelper.getCheckBoxMenuItem(new ActionDebug()));
    
    // MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN 
    JMenuBar result = new JMenuBar();
    result.add(mGraph);
    result.add(mLayout);
    result.add(mOptions);

    // done
    return result;
  }
  
  /**
   * Sets the graph we're looking at
   */
  private void setGraph(EditableGraph set) {
    // remember
    graph = set;
    // propagate
    graphWidget.setGraph2D(graph);
    layoutWidget.setEnabled(graph!=null);
  }
  
  /**
   * Creates a new graph
   */
  private void setGraph(AbstractGraphFactory factory) {
    // let the user change some properties
    if (PropertyWidget.hasProperties(factory)) {
      PropertyWidget content = new PropertyWidget().setInstance(factory);
      int rc = SwingHelper.showDialog(graphWidget, "Graph Properties", content, SwingHelper.DLG_OK_CANCEL);
      if (SwingHelper.OPTION_OK!=rc) 
        return;
      content.commit();
    }
    // create the graph
    factory.setNodeShape(shapes[0]);
    setGraph(factory.create(createGraphBounds()));
    // done
  }
  
  /**
   * Creates a graph bounds rectangle
   */
  private Rectangle createGraphBounds() {
    return new Rectangle(0,0,graphWidget.getWidth()-32,graphWidget.getHeight()-32);
  }
  
  /**
   * Toggle debug
   */
  /*package*/ class ActionDebug extends Action2 {
    ActionDebug() {
      super.setName("Debug");
    }
    @Override
    protected void execute() throws Exception {
      isDebug = !isDebug;
    }
    @Override
    public boolean isSelected() {
      return isDebug;
    }
  }
  
  /**
   * How to handle - run a layout
   */
  /*package*/ class ActionExecuteLayout extends Action2 {
    
    /** an animation that we're working on */
    private Animation animation;
    private Collection<Shape> debugShapes;
    private Shape shape;

    /**
     * constructor
     */
    /*package*/ ActionExecuteLayout() {
      super.setAsync(ASYNC_SAME_INSTANCE);
    }
    
    /** initializer (EDT) */
    @Override
    protected boolean preExecute() throws LayoutException {
      // something to do?
      if (graph==null) 
        return false;
      // something running?
      cancel(true);
      // restore original vertex shapes
      for (EditableVertex v : graph.getVertices()) 
        graph.setShape(v, v.getOriginalShape());
      
      // run layout
      Rectangle bounds = createGraphBounds();
      GraphLayout layout = layoutWidget.getSelectedLayouts();
      debugShapes = isDebug ? new ArrayList<Shape>() : null;
      LayoutContext context = new DefaultLayoutContext(debugShapes, logger, bounds);
      if (isAnimation) {
        animation = new Animation(graph, layout, context);
        return true;
      } else {
        logger.info("Starting graph layout "+layout.getClass().getSimpleName());
        shape = layout.apply(graph, context);
        logger.info("Finished graph layout "+layout.getClass().getSimpleName());
        return false;
      }
    }
    /** async execute */
    @Override
    protected void execute() throws LayoutException {
      try {
        while (true) {
          if (Thread.currentThread().isInterrupted()) break;
          if (animation.animate()) break;
          graphWidget.setGraph2D(graph);
          shape = null;
        }
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    /** sync post-execute */
    @Override
    protected void postExecute() throws Exception {
      if (graph!=null)
        graphWidget.setGraph2D(graph, shape!=null?shape.getBounds():null);
      graphWidget.setDebugShapes(debugShapes);
      graphWidget.setCurrentLayout(layoutWidget.getSelectedLayouts());
    }
    
  } //ActionExecuteLayout
  
  /**
   * How to handle - switch to Layout
   */
  /*package*/ class ActionSelectLayout extends Action2 {
    private GraphLayout layout;
    /*package*/ ActionSelectLayout(GraphLayout set) {
      super(ReflectHelper.getName(set.getClass()));
      layout=set;
    }
    @Override
    protected void execute() { 
      layoutWidget.setSelectedLayout(layout); 
    }
  }

  /**
   * How to handle - Load Graph
   */
  /*package*/ class ActionLoadGraph extends Action2  {
    private File preset;
    /*package*/ ActionLoadGraph(File file) { 
      preset=file;
    }
    /*package*/ ActionLoadGraph() { super("Load"); }
    @Override
    protected void execute() throws IOException { 
      File file = preset;
      if (file==null) {
        JFileChooser fc = new JFileChooser(new File("./save"));
        if (JFileChooser.APPROVE_OPTION!=fc.showOpenDialog(frame)) return;
        file = fc.getSelectedFile();
      }
      setGraph(new GraphReader(new FileInputStream(file)).read());
    }
    
  } //LoadGraph

  /**
   * How to handle - Save Graph
   */
  /*package*/ class ActionSaveGraph extends Action2 {
    /*package*/ ActionSaveGraph() { 
      setName("Save"); 
    }
    @Override
    protected void execute() throws IOException { 
      JFileChooser fc = new JFileChooser(new File("./save"));
      if (JFileChooser.APPROVE_OPTION!=fc.showSaveDialog(frame)) return;
      new GraphWriter(new FileOutputStream(fc.getSelectedFile())).write(graph);
    }
  }

  /**
   * How to handle - Close Graph
   */
  /*package*/ class ActionCloseGraph extends Action2 {
    /*package*/ ActionCloseGraph() { super("Close"); }
    @Override
    protected void execute() { frame.dispose(); }
  }
  
  /**
   * How to handle - New Graph
   */
  /*package*/ class ActionNewGraph extends Action2 {
    private AbstractGraphFactory factory;
    /*package*/ ActionNewGraph(AbstractGraphFactory factory) { 
      super(factory.toString());
      this.factory = factory;
    }
    @Override
    protected void execute() {
      setGraph(factory);
    }
  }

  /**
   * How to handle - Toggle antialiasing
   */
  /*package*/ class ActionToggleAntialias extends Action2 {
    /*package*/ ActionToggleAntialias() { super("Antialiasing"); }
    @Override
    public boolean isSelected() { return graphWidget.isAntialiasing(); }
    @Override
    protected void execute() {
      graphWidget.setAntialiasing(!graphWidget.isAntialiasing());
    }    
  }

  /**
   * How to handle - Toggle animation
   */
  /*package*/ class ActionToggleAnimation extends Action2 {
    /*package*/ ActionToggleAnimation() { super("Animation"); }
    @Override
    public boolean isSelected() { return isAnimation; }
    @Override
    protected void execute() {
      isAnimation = !isAnimation;
    }    
  }

}
