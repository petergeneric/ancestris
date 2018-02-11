package ancestris.tour;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */



import ancestris.api.sample.SampleProvider;
import ancestris.app.ActionClose;
import ancestris.app.ActionOpen;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.explorer.GedcomExplorerTopComponent;
import ancestris.gedcom.GedcomDirectory;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import com.sun.awt.AWTUtilities;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import gj.awt.geom.Path;
import java.awt.BasicStroke;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.MenuElement;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class TourAction  implements ActionListener {

    private boolean transluscentIsSupported = true;
    
    private final int ARROW = 80;
    private final int SMALLGAP = 20;
    private final int GAP = ARROW + SMALLGAP;
    
    private final String DEMOFILE = "bourbon";
    
    private TopComponent welcome;
    private int numDemo;
    private boolean componentToBeClosed = false;

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Logger LOG = Logger.getLogger("ancestris.guided_tour");
        
        // Determine if the GraphicsDevice supports translucency.
        GraphicsEnvironment graphenv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphdev = graphenv.getDefaultScreenDevice();

        // Check if translucent windows are supported
        if (!graphdev.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            LOG.info("Guided Tour : translucency not supported. Will use opaque windows instead.");  
            transluscentIsSupported = false;
        }
        
        // Memorise welcome component to come back to it each time
        welcome = getTopComponent("Welcome");
        
        // Maximise frame
        WindowManager.getDefault().getMainWindow().setExtendedState(Frame.MAXIMIZED_BOTH);

        // Start demo
        numDemo = 0;
        boolean stop = demoIntro();
        if (stop) return;
        
        
        // Open demo gedcom if not already open (smaller than Kennedy)
        // (remember to close it afterward if it was not open)
        boolean wasOpen = false;
        Collection<? extends SampleProvider> files = AncestrisPlugin.lookupAll(SampleProvider.class);
        for (SampleProvider sample : files) {
            if (sample.getName().toLowerCase().contains(DEMOFILE)) {
                List<Context> loadedContexts = GedcomDirectory.getDefault().getContexts();
                for (Context context : loadedContexts) {
                    Gedcom gedcom = context.getGedcom();
                    if (gedcom.getName().toLowerCase().contains(DEMOFILE)) {
                        wasOpen = true;
                    }
                }
                if (!wasOpen) {
                    setWait();
                    new ActionOpen(FileUtil.toFileObject(sample.getSampleGedcomFile())).actionPerformed(e);
                    setWaitNot();
                }
                break;
            }
        }
        
        // Walk through demo screens
        stop = demoWindows();
        if (!stop) {
            stop = demoMenu();
        }
        if (!stop) {
            stop = demoMenuview();
        }
        if (!stop) {
            stop = demoMenutools();
        }
        if (!stop) {
            stop = demoGedcomtools();
        }
        if (!stop) {
            stop = demoProperties();
        }
        if (!stop) {
            stop = demoMenuhelp();
        }
        if (!stop) {
            stop = demoExplorer();
        }
        if (!stop) {
            stop = demoTree();
        }
        if (!stop) {
            stop = demoCygnus();
        }
        if (!stop) {
            stop = demoGedcom();
        }
        if (!stop) {
            stop = demoAries();
        }
        if (!stop) {
            stop = demoGeo();
        }
        if (!stop) {
            stop = demoChrono();
        }
        if (!stop) {
            stop = demoTable();
        }
        if (!stop) {
            stop = demoSearch();
        }


        
        // Close demo file is was not open
        if (!wasOpen) {
            List<Context> loadedContexts = GedcomDirectory.getDefault().getContexts();
            for (Context context : loadedContexts) {
                Gedcom gedcom = context.getGedcom();
                if (gedcom.getName().toLowerCase().contains(DEMOFILE)) {
                    setWait();
                    new ActionClose(context).actionPerformed(e);
                    setWaitNot();
                }
            }
        }

        if (!stop) {
            demoClose();
        }
    }

    
    
    
    
    
    
    /***************************************************************************
     * Demo start and end
     * 
     */
    
    private boolean demoIntro() {
        String text = NbBundle.getMessage(getClass(), "demo.intro");
        Color bgcolor = Color.BLACK;
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(500, 300);
        TranslucentPopup popup = new TranslucentPopup(null, true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, false);
        return popup.showDemo();
    }

    private boolean demoClose() {
        String text = NbBundle.getMessage(getClass(), "demo.close");
        Color bgcolor = Color.BLACK;
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(500, 420);
        TranslucentPopup popup = new TranslucentPopup(null, true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, true);
        return popup.showDemo();
    }


    
    /***************************************************************************
     * Demo screens in any order
     * 
     * 
     */
    
    private boolean demoWindows() {
        String text = NbBundle.getMessage(getClass(), "demo.windows");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 440);
        TranslucentPopup popup = new TranslucentPopup(null, true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, false);
        return popup.showDemo();
    }

    private boolean demoMenu() {
        JMenuBar mb = getMenuBar();
        String text = NbBundle.getMessage(getClass(), "demo.menu");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(780, 480);
        Window w = getWindow(bgcolor, Color.RED, 0, 50, 400, 80, 3, 3);
        w.setVisible(true);
        TranslucentPopup popup = new TranslucentPopup(mb, true, false, 0, bgcolor, fgcolor, text, new Point(340, 130), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        w.setVisible(false);
        return stop;
    }
    
    private boolean demoMenuview() {
        JMenu m = getMenu(2);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menuview");
        Color bgcolor = new Color(0x00027b69);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 240);
        TranslucentPopup popup = new TranslucentPopup(m, true, false, 20, bgcolor, fgcolor, text, new Point(380, 260), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        m.setPopupMenuVisible(false);
        return stop;
    }

    
    private boolean demoMenutools() {
        JMenu m = getMenu(3);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menutools");
        Color bgcolor = new Color(0x004f027b);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 360);
        TranslucentPopup popup = new TranslucentPopup(m, true, false, 20, bgcolor, fgcolor, text, new Point(370, 170), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        m.setPopupMenuVisible(false);
        return stop;
    }

    
    private boolean demoGedcomtools() {
        JMenu m = getMenu(3);
        m.setPopupMenuVisible(true);
        JPopupMenu jpm = (JPopupMenu) m.getSubElements()[0];
        MenuElement[] elts = jpm.getSubElements();
        JMenu subM = null;
        for (MenuElement me : elts) {
            if (me instanceof JMenu) {
                subM = (JMenu) me;
                String str = subM.getText();
                if (str != null && str.toLowerCase().contains("gedcom")) {
                    subM.setPopupMenuVisible(true);
                    break;
                }
            }
        }
        String text = NbBundle.getMessage(getClass(), "demo.gedcomtools");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(590, 380);
        TranslucentPopup popup = new TranslucentPopup(m, true, false, 20, bgcolor, fgcolor, text, new Point(620, 270), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        if (subM != null) {
            subM.setPopupMenuVisible(false);
        }
        m.setPopupMenuVisible(false);
        return stop;
    }

    
    private boolean demoProperties() {
        JMenu m = getMenu(0);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.properties");
        Color bgcolor = new Color(0x00643600);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 400);
        TranslucentPopup popup = new TranslucentPopup(m, true, false, 20, bgcolor, fgcolor, text, new Point(150, 230), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        m.setPopupMenuVisible(false);
        return stop;
    }

    
    
    private boolean demoMenuhelp() {
        JMenu m = getMenu(5);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menuhelp");
        Color bgcolor = new Color(0x00004909);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 380);
        TranslucentPopup popup = new TranslucentPopup(m, true, false, 20, bgcolor, fgcolor, text, new Point(450, 180), dim, GAP, SMALLGAP, welcome, false);
        boolean stop = popup.showDemo();
        m.setPopupMenuVisible(false);
        return stop;
    }


    private boolean demoExplorer() {
        GedcomExplorerTopComponent demo = GedcomExplorerTopComponent.findInstance();
        demo.expandCollapse(true);
        String text = NbBundle.getMessage(getClass(), "demo.explorer");
        Color bgcolor = new Color(0x005dbcff);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(650, 410);
        TranslucentPopup popup = new TranslucentPopup(demo, true, false, 15, bgcolor, fgcolor, text, new Point(240, 330), dim, GAP, SMALLGAP, welcome, false);
        boolean next = popup.showDemo();
        demo.expandCollapse(true);
        return next;
    }
    
    private boolean demoTree() {
        TopComponent demo = getTopComponent("TreeTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.tree");
        Color bgcolor = new Color(0x00bceca8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(700, 420);
        TranslucentPopup popup = new TranslucentPopup(demo, true, false, 20, bgcolor, fgcolor, text, new Point(700, 400), dim, GAP, SMALLGAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoCygnus() {
        TopComponent demo = getTopComponent("CygnusTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.cygnus");
        Color bgcolor = new Color(0x00a8cdec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(900, 500);
        TranslucentPopup popup = new TranslucentPopup(demo, false, false, 20, bgcolor, fgcolor, text, new Point(350, 320), dim, SMALLGAP, GAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoGedcom() {
        TopComponent demo = getTopComponent("GedcomTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.gedcom");
        Color bgcolor = new Color(0x00bea8ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 480);
        TranslucentPopup popup = new TranslucentPopup(demo, false, false, 20, bgcolor, fgcolor, text, new Point(410, 330), dim, SMALLGAP, GAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoAries() {
        TopComponent demo = getTopComponent("AriesTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.aries");
        Color bgcolor = new Color(0x00ebeca8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 380);
        TranslucentPopup popup = new TranslucentPopup(demo, false, false, 20, bgcolor, fgcolor, text, new Point(420, 360), dim, SMALLGAP, GAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoGeo() {
        TopComponent demo = getTopComponent("GeoMapTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.geo");
        Color bgcolor = new Color(0x00e3a8ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 550);
        TranslucentPopup popup = new TranslucentPopup(demo, true, false, 60, bgcolor, fgcolor, text, new Point(550, 250), dim, GAP, SMALLGAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    private boolean demoChrono() {
        TopComponent demo = getTopComponent("TimelineTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.chrono");
        Color bgcolor = new Color(0x00a8e3ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(650, 330);
        TranslucentPopup popup = new TranslucentPopup(demo, false, false, 20, bgcolor, fgcolor, text, new Point(60, 400), dim, SMALLGAP, GAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoTable() {
        TopComponent demo = getTopComponent("TableTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.table");
        Color bgcolor = new Color(0x005dedaa);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(600, 450);
        TranslucentPopup popup = new TranslucentPopup(demo, true, false, 20, bgcolor, fgcolor, text, new Point(10, 10), dim, GAP, SMALLGAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    
    private boolean demoSearch() {
        TopComponent demo = getTopComponent("TreeSharingTopComponent");
        String text = NbBundle.getMessage(getClass(), "demo.search");
        Color bgcolor = new Color(0x00ecc1a8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 480);
        TranslucentPopup popup = new TranslucentPopup(demo, true, false, 20, bgcolor, fgcolor, text, new Point(240, 300), dim, GAP, SMALLGAP, welcome, false);
        boolean next = popup.showDemo();
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }
    
    


    
    
    
    /***************************************************************************
     * TOOLS
     * 
     */
    
    private Shape getBubble(boolean isLeft, boolean isTop, Dimension d, boolean isCurved, int  offset) {
        int w = d.width;
        int h = d.height;
        int wq = ARROW;
        int hq = h/5;
        int corners = 50;

        GeneralPath path = new GeneralPath();
        Path shape = null;
        
        if (isLeft && isTop) {
            path.moveTo(wq, 2 * hq);
            if (isCurved) {
                path.curveTo(wq, 2 * hq, 0, 2 * hq, 0, offset);
                path.curveTo(0, offset, 0, hq, wq, 1 * hq);
            } else {
                path.lineTo(0, offset);
                path.lineTo(wq, 1 * hq);
            }
            shape = new Path().append(new RoundRectangle2D.Double(wq, 0, w - wq, h, corners, corners));
        } else if (!isLeft && isTop) {
            path.moveTo(w - wq, 2 * hq);
            if (isCurved) {
                path.curveTo(w - wq, 2 * hq, w, 2 * hq, w, offset);
                path.curveTo(w, offset, w, hq, w - wq, 1 * hq);
            } else {
                path.lineTo(w, offset);
                path.lineTo(w - wq, hq);
            }
            shape = new Path().append(new RoundRectangle2D.Double(0, 0, w - wq, h, corners, corners));
        } else if (isLeft && !isTop) {
            path.moveTo(wq, h - 2 * hq);
            if (isCurved) {
                path.curveTo(wq, h - 2 * hq, 0, h - 2 * hq, 0, h - offset);
                path.curveTo(0, h - offset, 0, h - hq, wq, h - hq);
            } else {
                path.lineTo(0, h - offset);
                path.lineTo(wq, h - 1 * hq);
            }
            shape = new Path().append(new RoundRectangle2D.Double(wq, 0, w - wq, h, corners, corners));
        } else if (!isLeft && !isTop) {
            path.moveTo(w - wq, h - hq);
            if (isCurved) {
                path.curveTo(w - wq, h - hq,        w, h - hq,         w, h - offset);
                path.curveTo(w, h - offset,         w, h - 2 * hq,     w - wq, h - 2 * hq);
            } else {
                path.lineTo(w, h - offset);
                path.lineTo(w - wq, h - 2 * hq);
            }
            shape = new Path().append(new RoundRectangle2D.Double(0, 0, w - wq, h, corners, corners));
        }
        
        path.append(shape, true);
        path.closePath();

        return path.createTransformedShape(new AffineTransform());
    }

    private JMenuBar getMenuBar() {
        Frame f = WindowManager.getDefault().getMainWindow();
        Component[] cs = f.getComponents();
        JRootPane rp = (JRootPane) cs[0];
        return rp.getJMenuBar();
    }    
    
    private JMenu getMenu(int index) {
        JMenuBar mb = getMenuBar();
        return mb.getMenu(index);
    }    
    
    private void setWait() {
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setWaitNot() {
        WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private Window getWindow(final Color color, final Color colorBox, final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight) {
        Window w = new Window(null) {
            @Override
            public void paint(Graphics g) {
                int b = 3;
                g.setColor(colorBox);
                ((Graphics2D) g).setStroke(new BasicStroke(b));
                g.drawRoundRect(arcWidth+b, arcHeight+b, width-2*(arcWidth+b), height-2*(arcHeight+b), arcWidth, arcHeight);
            }

            @Override
            public void update(Graphics g) {
                paint(g);
            }
        };
        w.setAlwaysOnTop(true);
        w.setLocation(x, y);
        w.setSize(width, height);
        if (transluscentIsSupported) {
            w.setBackground(color);
            AWTUtilities.setWindowOpacity(w, 0.3f);
        }
        return w;
    }

    private TopComponent getTopComponent(String str) {

        // Look first for TC in opened windows
        for (TopComponent tcItem : WindowManager.getDefault().getRegistry().getOpened()) {
            String p = tcItem.getClass().getName();
            String n = tcItem.getName();
            if (p.contains(str)) { // TC found and open
                if (tcItem instanceof AncestrisTopComponent) {   
                    if (!n.contains(DEMOFILE)) {    // not the right TC
                        continue;
                    }
                }
                componentToBeClosed = false;
                return tcItem;
            }
        }
        
        // If not found, TopComonent is closed ; look for it in the lookup and open it
        TopComponent tc = null;
        setWait();

        // The following call awakens the TopComponents if they have never been loaded so that the next call gives all TCs.
        // And because TreeSharing is not an AncestrisViewInterface, I need the lookup TopComponent
        Collection<AncestrisViewInterface> listTmp = (Collection<AncestrisViewInterface>) Lookup.getDefault().lookupAll(AncestrisViewInterface.class);
        
        List<TopComponent> list = (List<TopComponent>) Lookup.getDefault().lookupAll(TopComponent.class);
        for (TopComponent tcItem : list) {
            String p = tcItem.getClass().getName();
            if (p.contains(str)) { // TC found
                // If ancestris component, create from context first
                if (tcItem instanceof AncestrisTopComponent) {
                    List<Context> contexts = GedcomDirectory.getDefault().getContexts();
                    Context contextToOpen = contexts.get(0);
                    for (Context c : contexts) {
                        if (c.getGedcom().getName().contains(DEMOFILE)) {
                            contextToOpen = c;
                            break;
                        }
                    }
                    tc = ((AncestrisTopComponent) tcItem).create(contextToOpen);
                } else { // TreeSharingTopComponent is not an AncestrisTopComponent
                    tc = tcItem;
                }
                tc.open();
                componentToBeClosed = true;
                break;
            }
        }

        setWaitNot();
        return tc;
    }
    
    private class TranslucentPopup extends JDialog implements KeyListener {

        private final JDialog me;
        private boolean exit = true;
        private TourPanel panel;
        private Dimension screenSize;
        
        private Component demo;
        private TopComponent back;
        private boolean pointerLeftParam;
        private boolean pointerTopParam;
        private boolean isCurvedParam;
        private int pointerOffsetParam;
        private Point pParam;
        private Dimension dParam;
        private Color bgcolorParam;
        private Color fgcolorParam;
        private String textParam;
        private int gapLParam;
        private int gapRParam;
        private boolean endParam;
        
        public TranslucentPopup(Component demo, boolean isLeft, boolean isCurved, int  offset, Color bgcolor, Color fgcolor, String text, Point p, Dimension d, int gapL, int gapR, final TopComponent back, boolean end) {
            super();
            me = this;
            this.demo = demo;
            this.back = back;
            this.pParam = p;
            this.dParam = d;
            this.pointerLeftParam = isLeft;
            this.isCurvedParam = isCurved;
            this.pointerOffsetParam = offset;
            this.bgcolorParam = bgcolor;
            this.fgcolorParam = fgcolor;
            this.textParam = text;
            this.gapLParam = gapL;
            this.gapRParam = gapR;
            this.endParam = end;
            this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();


            // Set dialog
            setModal(true);
            setUndecorated(true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); 
            setUndecorated(true);
            setResizable(false);
            addKeyListener(this);
        }
        
        public boolean showDemo() {
            Point pTC = null;
            Dimension dTC = null;
            boolean isTC = false;
            
            // Show demo panel
            if (demo != null && demo instanceof TopComponent) {
                ((TopComponent)demo).requestActive();
                isTC = true;
            }
            
            // Set Bubble orientation and location
            pointerTopParam = true;
            // - If not a TC and no position provided, use default one (middle of the screen)
            if (pParam == null) {
                pParam = new Point((screenSize.width - dParam.width)/2, (screenSize.height - dParam.height)/2);  // default location, no pointer
            } else 
            // - If TC, overwrite provided orientation and position
            if (isTC) {
                pTC = demo.getLocationOnScreen();
                dTC = new Dimension(demo.getBounds().width, demo.getBounds().height);
                pParam.x = pTC.x + dTC.width / 2; // middle of component
                pParam.y = pTC.y + dTC.height / 2; // middle of component
                pointerLeftParam = (pParam.x <= (screenSize.width/2)); 
                if (pointerLeftParam) {                   // pointer to the left
                    gapLParam = GAP;
                    gapRParam = SMALLGAP;
                } else {                                  // pointer to the right
                    pParam.x -= dParam.width;
                    gapLParam = SMALLGAP;
                    gapRParam = GAP;
                }
                pointerTopParam = (pParam.y <= (screenSize.height/2)); 
                if (pointerTopParam) {                    // pointer to the top
                } else {                                  // pointer to the bottom
                    pParam.y -= dParam.height;
                }

            }

            setLocation(pParam);
            
            // Set Bubble shape
            Shape bubble = null;
            if (pointerOffsetParam == -1) {
                bubble = new Path().append(new RoundRectangle2D.Double(0, 0, dParam.width, dParam.height, 50, 50));
            } else {
                bubble = getBubble(pointerLeftParam, pointerTopParam, dParam, isCurvedParam, pointerOffsetParam);
            }
            setShape(bubble);
            
            // Make it transparent if supported
            if (transluscentIsSupported) {
                me.setOpacity(0.90f);
            }
            
            // Show bubble
            numDemo++;
            panel = new TourPanel(numDemo, textParam, bgcolorParam, fgcolorParam, gapLParam, gapRParam, endParam) {
                @Override
                public void closeDemo(boolean set) {
                    me.dispose();
                    back.requestActive();
                    exit = set;
                }
            };
            getContentPane().add(panel);
            pack();
            setSize(dParam);
            me.setVisible(true);
            return exit;
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                panel.closeDemo(false);
            }
            if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
                panel.closeDemo(true);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
        
    }
    
    
}
