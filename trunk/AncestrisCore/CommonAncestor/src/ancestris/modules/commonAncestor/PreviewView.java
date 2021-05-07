package ancestris.modules.commonAncestor;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.modules.commonAncestor.graphics.IGraphicsRenderer;
import ancestris.swing.ToolBar;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.SelectionDispatcher;
import genj.fo.Format;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.report.ReportView;
import genj.util.swing.EditorHyperlinkSupport;
import genj.util.swing.ImageIcon;
import genj.util.swing.SliderWidget;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.openide.util.NbBundle;

/**
 *
 * @author michel
 */
public class PreviewView extends JPanel {

    static final Logger LOG = Logger.getLogger("genj.report");
    /**
     * pages to switch between
     */
    private final static String WELCOME = "welcome",
            CONSOLE = "console",
            RESULT = "result";
    private String currentPage = WELCOME;
    /**
     * components to show report info
     */
    private Console output;
    private JScrollPane result;
    private ActionShow actionShow = new ActionShow();
    /**
     * statics
     */
    // XXX: mus not depends on report module to (only) get those images...
    private final static ImageIcon imgStart = new ImageIcon(ReportView.class, "Start"),
            imgStop = new ImageIcon(ReportView.class, "Stop"),
            imgSave = new ImageIcon(ReportView.class, "Save"),
            imgConsole = new ImageIcon(ReportView.class, "ReportShell"),
            imgGui = new ImageIcon(ReportView.class, "ReportGui");
    /**
     * gedcom this view is for
     */
    private Gedcom gedcom;
    /**
     * our content
     */
    //private Content content;
    /**
     * our current zoom
     */
    private double zoom = 1.0D;

    private SliderWidget sliderZoom;
    private IGraphicsRenderer renderer;
    private Point lastPoint;
    private JComponent scrolledComponent;

    public PreviewView() {

        setLayout(new CardLayout());

        // Output
        output = new Console();
        add(new JScrollPane(output), CONSOLE);

        // result
        result = new JScrollPane(new JPanel());
        add(result, RESULT);

        result.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                result.setCursor(Cursor.getDefaultCursor());
            }
        });

        result.addMouseMotionListener(new MouseMotionAdapter() {

            private JScrollBar hSb = result.getHorizontalScrollBar();
            private JScrollBar vSb = result.getVerticalScrollBar();

            @Override
            public void mouseDragged(MouseEvent e) {
                int dX = lastPoint.x - e.getX();
                int dY = lastPoint.y - e.getY();

                hSb.setValue(hSb.getValue() + (int) (dX * zoom));
                vSb.setValue(vSb.getValue() + (int) (dY * zoom));
                lastPoint = e.getPoint();
            }
        });

        result.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                int newValue = sliderZoom.getValue() - (e.getWheelRotation() * 3);
                sliderZoom.setValue(newValue);
            }
        });

    }

    /**
     * show welcome/console/output
     */
    /* package */
    void show(String page) {
        if (!currentPage.equals(page)) {
            ((CardLayout) getLayout()).show(this, page);
            currentPage = page;
        }
    }

    /**
     * show result of a report run
     */
    void showResult(Object object) {

        // none?
        if (object == null) {

            // go back to welcome if there was nothing dumped to console
            if (output.getDocument().getLength() == 0) {
                show(WELCOME);
            }
            return;
        }

        // Exception?
        if (object instanceof InterruptedException) {
            output.add("*** cancelled");
            return;
        }

        if (object instanceof Throwable) {
            CharArrayWriter buf = new CharArrayWriter(256);
            ((Throwable) object).printStackTrace(new PrintWriter(buf));
            output.add("*** exception caught" + '\n' + buf);

            LOG.log(Level.WARNING, "Exception caught ", (Throwable) object);
            return;
        }

        // File?
        if (object instanceof File) {
            File file = (File) object;
            if (file.getName().endsWith(".htm") || file.getName().endsWith(".html")) {
                try {
                    object = file.toURI().toURL();
                } catch (Throwable t) {
                    // can't happen
                }
            } else {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Throwable t) {
                    Logger.getLogger("genj.report").log(Level.INFO, "can't open " + file, t);
                    output.add("*** can't open file " + file);
                }
                return;
            }
        }

        // URL?
        if (object instanceof URL) {
            try {
                output.setPage((URL) object);
            } catch (IOException e) {
                output.add("*** can't open URL " + object + ": " + e.getMessage());
            }
            actionShow.setEnabled(false);
            actionShow.setSelected(false);
            show(CONSOLE);
            return;
        }

        // component?
        if (object instanceof JComponent) {
            JComponent c = (JComponent) object;
            c.setMinimumSize(new Dimension(0, 0));
            scrolledComponent = c;
            result.setViewportView(c);
            actionShow.setEnabled(true);
            actionShow.setSelected(true);
            show(RESULT);
            return;
        }

        // document
        if (object instanceof genj.fo.Document) {

            genj.fo.Document doc = (genj.fo.Document) object;

            Format[] formats = Format.getFormats();
            Map<String, String> fmts = new HashMap<>();   // description, extension
            for (Format format : formats) {
                fmts.put(format.getFormat(), format.getFileExtension());
            }

            FileChooserBuilder fcb = new FileChooserBuilder(genj.fo.Document.class)
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(NbBundle.getMessage(getClass(), "Fo_Document", doc.getTitle()))
                    .setApproveText(NbBundle.getMessage(getClass(), "Fo_OK_Select"))
                    .setDefaultExtension(formats[0].getFileExtension())
                    .setFileFilters(fmts)
                    .setAcceptAllFileFilterUsed(false)
                    .setDefaultDirAsReportDirectory()
                    .setFileHiding(true);

            File file = fcb.showSaveDialog();
            if (file == null) {
                showResult(null);
                return;
            }

            Format formatter = Format.getFormatFromExtension(FileChooserBuilder.getExtension(file.getName()));

            // format and write
            try {
                file.getParentFile().mkdirs();
                formatter.format(doc, file);
            } catch (Throwable t) {
                LOG.log(Level.WARNING, "formatting " + doc + " failed", t);
                output.add("*** formatting " + doc + " failed");
                return;
            }

            // go back to document's file
            showResult(file);

            return;
        }

        // unknown
        output.add("*** report returned unknown result " + object);
    }

    /**
     * process zoom changes
     *
     * @param d
     */
    public void setZoom(double d) {
        zoom = Math.max(0.1D, Math.min(1.0, d));
        if (renderer != null) {
            renderer.setZoom(zoom);

            if (scrolledComponent != null) {
                // je mets à jour la taille du composant scrollé qui est a l'interieur du JScrollPane
                // cela refrachit automatiquement la taille des scrollbar du JScrollPane
                scrolledComponent.setPreferredSize(new Dimension(renderer.getImageWidth(), renderer.getImageHeight()));
                scrolledComponent.setSize(new Dimension(renderer.getImageWidth(), renderer.getImageHeight()));
                //revalidate();
                repaint();
            }
        }
    }

    /**
     * retourne le zoom
     */
    public double getZoom() {
        return zoom;
    }

    void setRenderer(IGraphicsRenderer renderer) {
        this.renderer = renderer;
        renderer.setZoom(zoom);
    }

    /**
     * create a toolbox with zoom slider
     *
     * @param toolbar
     */
    public void populate(ToolBar toolbar) {
        // zooming!
        sliderZoom = new SliderWidget(1, 100, (int) (zoom * 100));
        sliderZoom.addChangeListener(new SliderListener());
        sliderZoom.setAlignmentX(0);
        sliderZoom.setOpaque(false);
        sliderZoom.setFocusable(false);
        toolbar.add(sliderZoom);
        JLabel dummy = new JLabel();
        dummy.setPreferredSize(new Dimension(20, 20));
        dummy.setEnabled(false);
        toolbar.add(dummy);
    }

    //==========================================================================
    // private class SliderListener
    //==========================================================================
    /**
     * listener for slider changes
     */
    private class SliderListener implements ChangeListener {

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            setZoom(sliderZoom.getValue() * 0.01D);
        }
    }

    //==========================================================================
    // private class ActionShow
    //==========================================================================
    /**
     * Action: Console
     */
    private class ActionShow extends AbstractAncestrisAction {

        protected ActionShow() {
            setImage(imgConsole);
            setTip(NbBundle.getMessage(ReportView.class, "report.output"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            setSelected(isSelected());
        }

        @Override
        public boolean setSelected(boolean selected) {
            setImage(selected ? imgGui : imgConsole);
            if (selected) {
                show(RESULT);
            } else {
                show(CONSOLE);
            }
            return super.setSelected(selected);
        }
    }

    //==========================================================================
    // private class Console
    //==========================================================================
    /**
     * console output
     */
    private class Console extends JEditorPane implements MouseListener, MouseMotionListener {

        /**
         * the currently found entity id
         */
        private String id = null;

        /**
         * constructor
         */
        private Console() {
            setContentType("text/plain");
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setEditable(false);
            addHyperlinkListener(new EditorHyperlinkSupport(this));
            addMouseMotionListener(this);
            addMouseListener(this);
        }

        /**
         * Check if user moves mouse above something recognizeable in output
         */
        @Override
        public void mouseMoved(MouseEvent e) {

            // try to find id at location
            id = markIDat(e.getPoint());

            // done
        }

        /**
         * Check if user clicks on marked ID
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            if (id != null && gedcom != null) {
                Entity entity = gedcom.getEntity(id);
                if (entity != null) {
                    SelectionDispatcher.fireSelection(e, new Context(entity));
                }
            }
        }

        /**
         * Tries to find an entity id at given position in output
         */
        private String markIDat(Point loc) {

            try {
                // do we get a position in the model?
                int pos = viewToModel(loc);
                if (pos < 0) {
                    return null;
                }

                // scan doc
                javax.swing.text.Document doc = getDocument();

                // find ' ' to the left
                for (int i = 0;; i++) {
                    // stop looking after 10
                    if (i == 10) {
                        return null;
                    }
                    // check for starting line or non digit/character
                    if (pos == 0 || !Character.isLetterOrDigit(doc.getText(pos - 1, 1).charAt(0))) {
                        break;
                    }
                    // continue
                    pos--;
                }

                // find ' ' to the right
                int len = 0;
                while (true) {
                    // stop looking after 10
                    if (len == 10) {
                        return null;
                    }
                    // stop at end of doc
                    if (pos + len == doc.getLength()) {
                        break;
                    }
                    // or non digit/character
                    if (!Character.isLetterOrDigit(doc.getText(pos + len, 1).charAt(0))) {
                        break;
                    }
                    // continue
                    len++;
                }

                // check if it's an ID
                if (len < 2) {
                    return null;
                }
                String id = doc.getText(pos, len);
                if (gedcom == null || gedcom.getEntity(id) == null) {
                    return null;
                }

                // mark it
                // requestFocusInWindow();
                setCaretPosition(pos);
                moveCaretPosition(pos + len);

                // return in between
                return id;

                // done
            } catch (BadLocationException ble) {
            }

            // not found
            return null;
        }

        /**
         * have to implement MouseMotionListener.mouseDragger()
         *
         * @see
         * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            // ignored
        }

        void clear() {
            setContentType("text/plain");
            setText("");
        }

        void add(String txt) {
            javax.swing.text.Document doc = getDocument();
            try {
                doc.insertString(doc.getLength(), txt, null);
            } catch (Throwable t) {
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    } // Output
}
