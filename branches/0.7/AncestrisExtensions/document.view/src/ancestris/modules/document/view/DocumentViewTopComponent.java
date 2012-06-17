package ancestris.modules.document.view;

import ancestris.app.App;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.HTMLFormat;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.view.SelectionSink;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.documents.view//DocumentView//EN",
autostore = false)
public final class DocumentViewTopComponent extends TopComponent {
    public class SaveNode extends AbstractNode {

        private class SaveCookieImpl implements SaveCookie {

            @Override
            public void save() throws IOException {
                String fileName = new SaveDocument(document, document.getTitle()).saveFile();
                if (!fileName.equals("")) {
                    preferences.put("documentFilename", fileName);
                    fire(false);
                }
            }
        }
        private SaveCookieImpl saveImpl;

        public SaveNode() {
            super(Children.LEAF);
            saveImpl = new SaveCookieImpl();
        }

        @Override
        public String getDisplayName() {
            return "test";
        }

        public void fire(boolean modified) {
            if (modified) {
                getCookieSet().assign(SaveCookie.class, saveImpl);
            } else {
                getCookieSet().assign(SaveCookie.class);
            }
        }
    }

    private class Hyperactive implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                    HTMLDocument doc = (HTMLDocument) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    String description = e.getDescription();
                    if (description.contains("#INDI_") || description.contains("#FAM_")) {
                        Gedcom myGedcom = null;
                        Context context = App.center.getSelectedContext(true);
                        if (context != null) {
                            myGedcom = context.getGedcom();
                            String CurrentId = description.substring(description.indexOf("_") + 1);
                            if (CurrentId != null && myGedcom != null) {
                                Entity entity = myGedcom.getEntity(CurrentId);
                                if (entity != null) {
                                    SelectionSink.Dispatcher.fireSelection(new Context(entity), true);
                                }
                            }
                        }
                    } else {
                        try {
                            pane.setPage(e.getURL());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    private static DocumentViewTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "ancestris/modules/document/view/View.png";
    private static final String PREFERRED_ID = "DocumentViewTopComponent";
    private static final Logger LOG = Logger.getLogger("DocumentViewTopComponent");
    private File tempfile = null;
    private SaveNode saveNode;
    Document document = null;
    Preferences preferences = null;

    public DocumentViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DocumentViewTopComponent.class, "CTL_DocumentViewTopComponent"));
        setToolTipText(NbBundle.getMessage(DocumentViewTopComponent.class, "HINT_DocumentViewTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        setActivatedNodes(new Node[]{saveNode = new SaveNode()});
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        documentViewScrollPane = new javax.swing.JScrollPane();
        documentViewEditorPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.BorderLayout());

        documentViewEditorPane.setEditable(false);
        documentViewScrollPane.setViewportView(documentViewEditorPane);

        add(documentViewScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane documentViewEditorPane;
    private javax.swing.JScrollPane documentViewScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DocumentViewTopComponent getDefault() {
        if (instance == null) {
            instance = new DocumentViewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DocumentViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DocumentViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DocumentViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DocumentViewTopComponent) {
            return (DocumentViewTopComponent) win;
        }
        Logger.getLogger(DocumentViewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        if (tempfile != null) {
            tempfile.delete();
        }
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void displayDocument(genj.fo.Document doc, Preferences preferences) {
        Format htmlFormatter = new HTMLFormat();
        document = doc;
        this.preferences = preferences;

        // format and write
        try {
            // create temporary file
            tempfile = File.createTempFile("name", ".html");

            htmlFormatter.format(document, tempfile);

            // display File
            documentViewEditorPane.setPage(tempfile.toURI().toURL());

        } catch (IOException e) {
            LOG.log(Level.WARNING, "formatting " + doc + " failed", e);
        }

        documentViewEditorPane.addHyperlinkListener(new Hyperactive());

        saveNode.fire(true);
    }
}
