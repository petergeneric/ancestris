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
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import gj.awt.geom.Path;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class TourAction implements ActionListener {

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
        welcome = showTopComponent(null, "Welcome");

        // Maximise frame
        WindowManager.getDefault().getMainWindow().setExtendedState(Frame.MAXIMIZED_BOTH);

        // Start demo
        numDemo = 0;
        boolean stop = demoIntro();
        if (stop) {
            return;
        }

        // Open demo gedcom if not already open (smaller than Kennedy)
        // (remember to close it afterward if it was not open)
        boolean wasOpen = false;
        boolean found = false;
        Collection<? extends SampleProvider> files = AncestrisPlugin.lookupAll(SampleProvider.class);
        for (SampleProvider sample : files) {
            if (sample.getName().toLowerCase().contains(DEMOFILE)) {
                found = true;
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

        // If not found ask to install Bourbon example
        if (!found) {
            LOG.info("Guided Tour : Bourbon module not installed. Required.");
            String title = NbBundle.getMessage(getClass(), "error.noBourbonTitl");
            String msg = NbBundle.getMessage(getClass(), "error.noBourbonMsg");
            Object o = DialogManager.create(title, msg).setMessageType(DialogManager.QUESTION_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).setResizable(false).show();
            if (o.equals(DialogManager.OK_OPTION)) {
                try {
                    Desktop.getDesktop().browse(new URI(NbBundle.getBundle("ancestris.welcome.resources.Bundle").getString("WelcomePage/GettingStartedLinks/tour.url.target")));
                } catch (Exception ex) {
                }

            }
            return;
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
            stop = demoProperties();
        }
        if (!stop) {
            stop = demoEdition();
        }
        if (!stop) {
            stop = demoWindow();
        }
        if (!stop) {
            stop = demoMenuOptions();
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

    /**
     * *************************************************************************
     * Demo start and end
     *
     */
    private boolean demoIntro() {
        String text = NbBundle.getMessage(getClass(), "demo.intro");
        Color bgcolor = Color.BLACK;
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(500, 300);
        TranslucentPopup popup = new TranslucentPopup(true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, false);
        popup.init();
        return showPopUp(popup, null); 
    }

    private boolean demoClose() {
        String text = NbBundle.getMessage(getClass(), "demo.close");
        Color bgcolor = Color.BLACK;
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(500, 420);
        TranslucentPopup popup = new TranslucentPopup(true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, true);
        popup.init();
        return showPopUp(popup, null); 
    }

    /**
     * *************************************************************************
     * Demo screens in any order
     *
     *
     */
    private boolean demoWindows() {
        String text = NbBundle.getMessage(getClass(), "demo.windows");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 440);
        TranslucentPopup popup = new TranslucentPopup(true, true, -1, bgcolor, fgcolor, text, null, dim, SMALLGAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, null); 
        return stop;
    }

    private boolean demoMenu() {
        JMenuBar mb = getMenuBar();
        String text = NbBundle.getMessage(getClass(), "demo.menu");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(780, 480);
        Window w = getWindow(bgcolor, Color.RED, 0, 50, 580, 80, 3, 3);
        TranslucentPopup popup = new TranslucentPopup(true, false, 0, bgcolor, fgcolor, text, new Point(360, 135), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        if (transluscentIsSupported) {
            w.setVisible(true);
        }
        boolean stop = showPopUp(popup, mb); 
        if (transluscentIsSupported) {
            w.setVisible(false);
        }
        return stop;
    }

    private boolean demoMenuview() {
        JMenu m = getMenu(2);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menuview");
        Color bgcolor = new Color(0x00027b69);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 240);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(260, 260), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoMenutools() {
        JMenu m = getMenu(3);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menutools");
        Color bgcolor = new Color(0x004f027b);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 360);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(460, 170), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoProperties() {
        JMenu m = getMenu(0);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.properties");
        Color bgcolor = new Color(0x00643600);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 400);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(250, 200), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoEdition() {
        JMenu m = getMenu(1);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.edition");
        Color bgcolor = new Color(0x00802000);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(640, 360);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(280, 210), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoWindow() {
        JMenu m = getMenu(4);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.window");
        Color bgcolor = new Color(0x00508000);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(450, 200);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(410, 180), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoMenuOptions() {
        JMenu m = getMenu(5);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menuoptions");
        Color bgcolor = new Color(0x000036ff);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(590, 410);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(520, 160), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoMenuhelp() {
        JMenu m = getMenu(6);
        m.setSelected(true);
        m.setPopupMenuVisible(true);
        String text = NbBundle.getMessage(getClass(), "demo.menuhelp");
        Color bgcolor = new Color(0x00004909);
        Color fgcolor = Color.WHITE;
        Dimension dim = new Dimension(680, 380);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(530, 160), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean stop = showPopUp(popup, m); 
        m.setSelected(false);
        m.setPopupMenuVisible(false);
        return stop;
    }

    private boolean demoExplorer() {
        GedcomExplorerTopComponent demo = GedcomExplorerTopComponent.findInstance();
        demo.expandCollapse(true);
        demo.requestActive();
        String text = NbBundle.getMessage(getClass(), "demo.explorer");
        Color bgcolor = new Color(0x005dbcff);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(650, 410);
        TranslucentPopup popup = new TranslucentPopup(true, false, 15, bgcolor, fgcolor, text, new Point(0, 0), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        boolean next = showPopUp(popup, demo); 
        demo.expandCollapse(true);
        return next;
    }

    private boolean demoTree() {
        String text = NbBundle.getMessage(getClass(), "demo.tree");
        Color bgcolor = new Color(0x00bceca8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(700, 420);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "TreeTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoCygnus() {
        String text = NbBundle.getMessage(getClass(), "demo.cygnus");
        Color bgcolor = new Color(0x00a8cdec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(900, 500);
        TranslucentPopup popup = new TranslucentPopup(false, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, SMALLGAP, GAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "CygnusTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoGedcom() {
        String text = NbBundle.getMessage(getClass(), "demo.gedcom");
        Color bgcolor = new Color(0x00bea8ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 480);
        TranslucentPopup popup = new TranslucentPopup(false, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, SMALLGAP, GAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "GedcomTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoAries() {
        String text = NbBundle.getMessage(getClass(), "demo.aries");
        Color bgcolor = new Color(0x00ebeca8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 380);
        TranslucentPopup popup = new TranslucentPopup(false, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, SMALLGAP, GAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "AriesTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoGeo() {
        String text = NbBundle.getMessage(getClass(), "demo.geo");
        Color bgcolor = new Color(0x00e3a8ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 550);
        TranslucentPopup popup = new TranslucentPopup(true, false, 60, bgcolor, fgcolor, text, new Point(0, 0), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup,"GeoMapTopComponent");
        // Prevent to display Map if no internet connexion.
        // Lead to unresponsive application
        if (!demo.isOpened()) {
            return false;
        }
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoChrono() {
        String text = NbBundle.getMessage(getClass(), "demo.chrono");
        Color bgcolor = new Color(0x00a8e3ec);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(650, 330);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "TimelineTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoTable() {
        String text = NbBundle.getMessage(getClass(), "demo.table");
        Color bgcolor = new Color(0x005dedaa);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 420);
        TranslucentPopup popup = new TranslucentPopup(false, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, SMALLGAP, GAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "TableTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    private boolean demoSearch() {
        String text = NbBundle.getMessage(getClass(), "demo.search");
        Color bgcolor = new Color(0x00ecc1a8);
        Color fgcolor = Color.BLACK;
        Dimension dim = new Dimension(800, 480);
        TranslucentPopup popup = new TranslucentPopup(true, false, 20, bgcolor, fgcolor, text, new Point(0, 0), dim, GAP, SMALLGAP, welcome, false);
        popup.init();
        TopComponent demo = showTopComponent(popup, "GedcomCompareTopComponent");
        boolean next = showPopUp(popup, demo); 
        if (componentToBeClosed) {
            demo.close();
        }
        return next;
    }

    /**
     * *************************************************************************
     * TOOLS
     *
     */
    private Shape getBubble(boolean isLeft, boolean isTop, Dimension d, boolean isCurved, int offset) {
        int w = d.width;
        int h = d.height;
        int wq = ARROW;
        int hq = h / 5;
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
                path.curveTo(w - wq, h - hq, w, h - hq, w, h - offset);
                path.curveTo(w, h - offset, w, h - 2 * hq, w - wq, h - 2 * hq);
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
        Window w = new Window(WindowManager.getDefault().getMainWindow()) {
            @Override
            public void paint(Graphics g) {
                int b = 3;
                g.setColor(colorBox);
                ((Graphics2D) g).setStroke(new BasicStroke(b));
                g.drawRoundRect(arcWidth + b, arcHeight + b, width - 2 * (arcWidth + b), height - 2 * (arcHeight + b), arcWidth, arcHeight);
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
            w.setOpacity(0.3f);
        }
        return w;
    }

    private TopComponent showTopComponent(TranslucentPopup tp, String str) {

        // Look first for TC in opened windows
        for (TopComponent tcItem : WindowManager.getDefault().getRegistry().getOpened()) {
            String p = tcItem.getClass().getName();
            String n = tcItem.getName().toLowerCase();
            if (p.contains(str)) { // TC found and open
                if (tcItem instanceof AncestrisTopComponent) {
                    if (!n.contains(DEMOFILE.toLowerCase())) {    // not the right TC
                        continue;
                    }
                }
                componentToBeClosed = false;
                activateTopComponent(tcItem, tp);
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
                    if (contexts.isEmpty()) {
                        continue;
                    }
                    Context contextToOpen = contexts.get(0);
                    for (Context c : contexts) {
                        if (c.getGedcom().getName().contains(DEMOFILE)) {
                            contextToOpen = c;
                            break;
                        }
                    }
                    tc = ((AncestrisTopComponent) tcItem).create(contextToOpen);
                } else { // GedcomCompareTopComponent is not an AncestrisTopComponent
                    tc = tcItem;
                }
                tc.open();
                componentToBeClosed = true;
                break;
            }
        }

        setWaitNot();
        activateTopComponent(tc, tp);
        return tc;
    }
    
    private void activateTopComponent(final TopComponent tc, TranslucentPopup tp) {
        if (tc != null) {
            tc.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent e) {
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    if (tp != null) {
                        tp.setPositionDimension(tc);
                    }
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                }
            });
            tc.requestActive();
        }
    }

    private boolean showPopUp(TranslucentPopup tp, Component tc) {
        tp.setPositionDimension(tc);

        tp.showDialog();  // as a modal dialog, this will wait until user presses continue or close, therefore changing exit value
            return tp.exit;
    }

    
    
    
    
    private class TranslucentPopup extends JDialog implements KeyListener {

        private final JDialog me;
        private boolean exit = true;
        private TourPanel panel = null;
        private Dimension screenSize;

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

        public TranslucentPopup(boolean isLeft, boolean isCurved, int offset, Color bgcolor, Color fgcolor, String text, Point p, Dimension d, int gapL, int gapR, final TopComponent back, boolean end) {
            super();
            me = this;
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

        }

        public void init() {
            // Set dialog
            setModal(true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setUndecorated(true);
            setResizable(false);
            addKeyListener(this);

            // Make it transparent if supported
            if (transluscentIsSupported) {
                me.setOpacity(0.90f);
            }

            // Increment & create bubble
            numDemo++;
            addBubble();
        }

        public void addBubble() {
            panel = new TourPanel(numDemo, textParam, bgcolorParam, fgcolorParam, gapLParam, gapRParam, endParam) {
                @Override
                public void closeDemo(boolean set) {
                    me.dispose();
                    back.requestActive();
                    exit = set;
                }
            };
            getContentPane().add(panel);
        }

        public void removeBubble() {
            if (panel != null) {
                getContentPane().remove(panel);
            }
        }
        
        private void setPositionDimension(Component demo) {
            boolean isTC = false;
            Point pTC = null;
            Dimension dTC = null;

            if (demo != null && demo instanceof TopComponent) {
                isTC = true; // in this case, center in component else it is a menu, use provided position
            }
            
            // Set Bubble orientation and location
            pointerTopParam = true;
            // - If not a TC and no position provided, use default one (middle of the screen)
            if (pParam == null | demo == null) {
                pParam = new Point((screenSize.width - dParam.width) / 2, (screenSize.height - dParam.height) / 2);  // default location, no pointer
            } else if (!isTC) { // menus for instance
                //pParam = pParam; // defined by calling method
            } else if (isTC && (demo.getBounds().width == 0 || !demo.isShowing())) {
                //pParam = pParam; // defined by calling method
            } else {
                pTC = demo.getLocationOnScreen();
                dTC = new Dimension(demo.getBounds().width, demo.getBounds().height);
                pParam.x = pTC.x + dTC.width / 2; // middle of component
                pParam.y = pTC.y + dTC.height / 2; // middle of component
                pointerLeftParam = (pParam.x <= (screenSize.width / 2));
                if (pointerLeftParam) {                   // pointer to the left
                    gapLParam = GAP;
                    gapRParam = SMALLGAP;
                } else {                                  // pointer to the right
                    pParam.x -= dParam.width;
                    gapLParam = SMALLGAP;
                    gapRParam = GAP;
                }
                pointerTopParam = (pParam.y <= (screenSize.height / 2));
                if (pointerTopParam) {                    // pointer to the top
                } else {                                  // pointer to the bottom
                    pParam.y -= dParam.height;
                }
                removeBubble();
                addBubble();
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

        }

        private void showDialog() {
            pack();
            setSize(dParam);
            me.setVisible(true);
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
