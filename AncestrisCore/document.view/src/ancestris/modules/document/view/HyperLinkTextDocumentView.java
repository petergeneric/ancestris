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
import static ancestris.modules.document.view.Bundle.*;
import genj.gedcom.Context;
import genj.util.swing.DialogHelper;
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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Class dedicated to display text in {@link DocumentViewTopComponent}.
 * All entities are active hyperlinks. These links fire a selection event.
 * A save action is registered to allow the text content to be saved
 *
 * @author daniel
 */
public class HyperLinkTextDocumentView extends AbstractDocumentView {

    HyperLinkTextPane textOutput;

    /**
     * Create and register the AbstractDocumentView
     *
     * @param context Gedcom Context
     * @param title   Tab title
     */
    public HyperLinkTextDocumentView(Context context, String title) {
        super(context, title);
        textOutput = new HyperLinkTextPane();
        textOutput.setGedcom(context.getGedcom());
        setView(textOutput);
        setToolbarActions(new Action[]{new ActionSave()});
    }

    /**
     * Add text to output pane.
     * Delegate to {@link HyperLinkTextPane}
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
        return textOutput.getDocument().getLength() == 0;
    }

    /**
     * Set pane to text content from a url. May be html encoded.
     * Delegate to {@link HyperLinkTextPane}
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
            setTip(savedocument_tip());
        }

        @Override
        public void actionPerformed(ActionEvent event) {


            // .. choose file
            JFileChooser chooser = new JFileChooser(".");
            chooser.setDialogTitle(getTip());
            chooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".txt");
                }

                @Override
                public String getDescription() {
                    return "*.txt (Text)";
                }
            });

            if (JFileChooser.APPROVE_OPTION != chooser.showDialog(HyperLinkTextDocumentView.this, "Save")) {
                return;
            }
            File file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            if (!file.getName().endsWith("*.txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            // .. exits ?
            if (file.exists()) {
                int rc = DialogHelper.openDialog(textOutput.getName(), DialogHelper.WARNING_MESSAGE, "File exists. Overwrite?", AbstractAncestrisAction.yesNo(), HyperLinkTextDocumentView.this);
                if (rc != 0) {
                    return;
                }
            }

            // .. open file
            final OutputStreamWriter out;
            try {
                out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
            } catch (IOException ex) {
                DialogHelper.openDialog(textOutput.getName(), DialogHelper.ERROR_MESSAGE, "Error while saving to\n" + file.getAbsolutePath(), AbstractAncestrisAction.okOnly(), HyperLinkTextDocumentView.this);
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
