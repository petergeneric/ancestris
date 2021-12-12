/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.document.view;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.resources.Images;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import javax.swing.Action;
import org.openide.util.NbBundle;

/**
 * Class dedicated to display text in {@link DocumentViewTopComponent}. All
 * entities are active hyperlinks. These links fire a selection event. A save
 * action is registered to allow the text content to be saved
 *
 * @author daniel
 */
public class HyperLinkTextDocumentView extends AbstractDocumentView {

    HyperLinkTextPane textOutput;
    String title;

    /**
     * Create and register the AbstractDocumentView
     *
     * @param context Gedcom Context
     * @param title Tab title
     * @param tooltip tooltip for tab
     */
    public HyperLinkTextDocumentView(Context context, String title, String tooltip) {
        super(context, title, tooltip);
        this.title = title;
        textOutput = new HyperLinkTextPane();
        textOutput.setGedcom(context.getGedcom());
        setView(textOutput);
        setToolbarActions(new Action[]{new ActionSave()});
    }

    /**
     * Add text to output pane. Delegate to {@link HyperLinkTextPane}
     *
     * @param txt
     */
    public void add(String txt) {
        textOutput.add(txt);
    }

    /**
     * true if output pane contains no character
     *
     * @return
     */
    public boolean isEmpty() {
        return textOutput.getDocument().getLength() == 0 && textOutput.getPage() == null;
    }

    /**
     * Set pane to text content from a url. May be html encoded. Delegate to
     * {@link HyperLinkTextPane}
     *
     * @param page text content URL
     *
     * @throws IOException
     */
    public void setPage(URL page) throws IOException {
        textOutput.setPage(page);
    }

    /**
     * Erase panel content
     */
    public void clear() {
        textOutput.clear();
    }

    /**
     * Action: SAVE
     */
    private class ActionSave extends AbstractAncestrisAction {

        protected ActionSave() {
            setImage(Images.imgSave);
            setTip(NbBundle.getMessage(getClass(), "TITL_SaveDocument", title));
        }

        @Override
        public void actionPerformed(ActionEvent event) {

            // .. choose file
            File file = new FileChooserBuilder(HyperLinkTextDocumentView.class)
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(NbBundle.getMessage(getClass(), "TITL_SaveDocument", title))
                    .setApproveText(NbBundle.getMessage(getClass(), "OK_Button"))
                    .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                    .setFileFilter(FileChooserBuilder.getTextFilter())
                    .setAcceptAllFileFilterUsed(false)
                    .setFileHiding(true)
                    .setDefaultDirAsReportDirectory()
                    .showSaveDialog(true);

            if (file == null) {
                return;
            }

            // .. open file
            final OutputStreamWriter out;
            try {
                out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
            } catch (IOException ex) {
                DialogManager.createError(textOutput.getName(), "Error while saving to\n" + file.getAbsolutePath()).show();
                return;
            }

            // .. save data
            try {
                String newline = System.getProperty("line.separator");
                BufferedReader in = new BufferedReader(new StringReader(textOutput.getText()));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    out.write(line);
                    out.write(newline);
                }
                in.close();
                out.close();

            } catch (Exception ex) {
            }

            // .. done
        }
    } // ActionSave
}
