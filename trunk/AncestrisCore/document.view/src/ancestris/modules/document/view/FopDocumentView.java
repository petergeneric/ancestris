/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Dominique Baron
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.document.view;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.resources.Images;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.SelectionDispatcher;
import genj.fo.Document;
import genj.fo.Format;
import genj.fo.HTMLFormat;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.io.FileAssociation;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Class dedicated to display a FopDocument in {@link DocumentViewTopComponent}.
 * All entities are active hyperlinks. These links fire a selection event.
 * A save action is registered to allow the FopDocument to be saved
 */
public class FopDocumentView extends AbstractDocumentView {

    private File tempfile = null;
    private Document document = null;
    private Preferences preferences = null;
    private JEditorPane editorPane;
    
    private Runnable runnable = null;

    /**
     * Create and register the FopDocumentView
     *
     * @param context Gedcom Context
     * @param title   Tab title
     * @param tooltip tooltip for tab
     */
    public FopDocumentView(Context context, String title, String tooltip) {
        this(context, title, tooltip, new AbstractAncestrisAction[]{});
    }

    public FopDocumentView(Context context, String title, String tooltip, AbstractAncestrisAction[] action) {
        super(context, title, tooltip);
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setName(title);
        setView(editorPane);
        
        // Build actions toolbar
        if (action == null) {
            action = new AbstractAncestrisAction[]{};
        }
        Action[] listOfActions = new Action[1 + action.length];
        listOfActions[0] = new ActionSave();
        for (int i = 1; i< listOfActions.length; i++) {
            listOfActions[i] = action[i-1];
        }
        setToolbarActions(listOfActions);
        
    }

    /**
     * Set the FopDocument to be Displayed in the AbstractDocumentView
     *
     * @param doc
     * @param preferences
     */
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
            editorPane.setPage(tempfile.toURI().toURL());
        } catch (IOException e) {
            //            LOG.log(Level.WARNING, "formatting " + doc + " failed", e);
        }
        editorPane.addHyperlinkListener(new Hyperactive());
    }

    @Override
    protected void closeNotify() {
        if (tempfile != null) {
            tempfile.delete();
            super.closeNotify();
        }
        // Add custom code on component closing
        if (runnable != null) {
            runnable.run();
        }
        
    }

    public void executeOnClose(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Action: SAVE
     */
    private class ActionSave extends AbstractAncestrisAction {

        protected ActionSave() {
            setImage(Images.imgSave);
            setTip(NbBundle.getMessage(getClass(), "TITL_SaveDocument", ""));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            
            // Default report folder is in : Registry.get(genj.gedcom.GedcomOptions.class).get("reportDir", System.getProperty("user.home"))
            File f = new File(document.getTitle().replaceAll("(\\W+)", "_"));
            FileChooserBuilder fcb = new FileChooserBuilder(FopDocumentView.class)
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(NbBundle.getMessage(getClass(), "TITL_SaveDocument", document.getTitle()))
                    .setApproveText(NbBundle.getMessage(getClass(), "OK_Button"))
                    .setDefaultExtension(FileChooserBuilder.getPdfFilter().getExtensions()[0])
                    .addFileFilter(FileChooserBuilder.getPdfFilter())
                    .addFileFilter(FileChooserBuilder.getHtmlFilter())
                    .addFileFilter(FileChooserBuilder.getCSVFilter())
                    .setAcceptAllFileFilterUsed(false)
                    .setDefaultDirAsReportDirectory()
                    .setSelectedFile(f)
                    .setFileHiding(true);

            File file = fcb.showSaveDialog();
            if (file == null) {
                return;
            }

            // format and write
            Format formatter = Format.getFormatFromExtension(FileChooserBuilder.getExtension(file.getName()));
            try {
                file.getParentFile().mkdirs();
                formatter.format(document, file);
                String fileName = file.getCanonicalPath();
                if (!fileName.equals("")) {
                    preferences.put("documentFilename", fileName);
                }
            } catch (Throwable t) {
            }
            
            try {
                FileAssociation.getDefault().execute(file.getAbsolutePath());
            } catch (Throwable t) {
                Logger.getLogger("ancestris.modules.document.view").log(Level.INFO, "cannot open " + file, t);
            }
            return;
            
            
        }
    } // ActionSave

    private static class Hyperactive implements HyperlinkListener {

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
                    // Look for anchor for description as "#<tag>+<id>+<tagpath>"  (@see Property.getLinkAnchor()).
                    if (description.startsWith("#") && description.contains(Property.DELIMITER_IN_ANCHOR)) {
                        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
                        if (context != null) {
                            Gedcom myGedcom = context.getGedcom();
                            if (myGedcom != null) {
                                
                                String[] bits = description.substring(1).split(Property.DELIMITER_IN_ANCHOR_REGEX);
                                String linkedTag = bits.length > 0 ? bits[0] : "";
                                String linkedId = bits.length > 1 ? bits[1] : "";
                                String linkedTagpath = bits.length > 2 ? bits[2] : "";
                                
                                Entity entity = null;
                                if (!linkedId.isEmpty()) {
                                    if (!linkedTag.isEmpty()) {
                                        entity = myGedcom.getEntity(linkedTag, linkedId);
                                    } else {
                                        entity = myGedcom.getEntity(linkedId);
                                    }
                                } else {
                                    if (!linkedTag.isEmpty()) {
                                        entity = myGedcom.getFirstEntity(linkedTag);
                                    }
                                }
                                // If entity not null, try to get property
                                if (entity != null) {
                                    if (!linkedTagpath.isEmpty()) {
                                        Property property = entity.getPropertyByPath(linkedTagpath);
                                        SelectionDispatcher.fireSelection(new Context(property));
                                    } else {
                                        SelectionDispatcher.fireSelection(new Context(entity));
                                    }
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
}
