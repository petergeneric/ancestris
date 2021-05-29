/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.edit.beans;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.RunExternal;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.util.Origin;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.ThumbnailWidget;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilePermission;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JCheckBox;
import org.openide.nodes.Node;

/**
 * A proxy knows how to generate interaction components that the user will use
 * to change a property : FILE / BLOB
 */
public class FileBean extends PropertyBean implements AncestrisActionProvider {

    private static final Logger LOG = Logger.getLogger("ancestris.app");

    /**
     * a check as accessory
     */
    private JCheckBox updateMeta = new JCheckBox(RESOURCES.getString("file.update"), true);

    /**
     * file chooser
     */
    private FileChooserWidget chooser = new FileChooserWidget();

    /**
     * preview
     */
    private ThumbnailWidget preview = new ThumbnailWidget() {
        @Override
        protected void handleDrop(List<File> files) {
            if (files.size() == 1) {
                File file = files.get(0);
                chooser.setFile(file);
                setSource(InputSource.get(file).orElse(null));
            }
        }
    };

    private transient ActionListener doPreview = new ActionListener() {
        public void actionPerformed(ActionEvent e) {

            // remember directory
            REGISTRY.put("bean.file.dir", chooser.getDirectory());

            String name = chooser.getFile();

            // show file
            File file = getProperty().getGedcom().getOrigin().getFile(name);
            if (file == null || !file.exists()) {
                try {
                    preview.setSource(InputSource.get(new URL(name)).orElse(null));
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINE, "Unable to get url.", ex);
                    preview.setSource(null);
                }
                return;
            }

            preview.setSource(InputSource.get(file).orElse(null));

            // calculate relative
            String relative = getProperty().getGedcom().getOrigin().calcRelativeLocation(file.getAbsolutePath());
            if (relative != null) {
                chooser.setFile(relative);
            }
            // done
        }
    };

    public FileBean() {

        setLayout(new BorderLayout());

        // setup chooser
        chooser.setComponentPopupMenu(new CCPMenu(chooser));
        chooser.setAccessory(updateMeta);
        chooser.addChangeListener(changeSupport);
        chooser.addActionListener(doPreview);

        add(chooser, BorderLayout.NORTH);

        // setup review
        add(preview, BorderLayout.CENTER);

        // done
        defaultFocus = chooser;
    }

    /**
     * Set context to edit
     */
    public void setPropertyImpl(Property property) {

        // calc directory
        Origin origin = getRoot().getGedcom().getOrigin();
        String dir = origin.getFile() != null ? origin.getFile().getParent() : null;

        // check if showing file chooser makes sense
        if (dir != null) {
            try {

                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(new FilePermission(dir, "read"));
                }

                chooser.setDirectory(REGISTRY.get("bean.file.dir", dir));
                chooser.setVisible(true);
                defaultFocus = chooser;

            } catch (SecurityException se) {
                chooser.setVisible(false);
                defaultFocus = null;
            }
        }

        preview.setSource(null);

        // case FILE
        if (property instanceof PropertyFile) {

            PropertyFile file = (PropertyFile) property;

            // show value
            chooser.setTemplate(false);
            chooser.setFile(file.getValue());

            if (property.getValue().length() > 0) {
                preview.setSource(((PropertyFile) property).getInput().orElse(null));
            }

            // done
        }

        // case BLOB
        if (property instanceof PropertyBlob) {

            PropertyBlob blob = (PropertyBlob) property;

            // show value
            chooser.setFile(blob.getValue());
            chooser.setTemplate(true);

            // .. preview
            preview.setSource(((PropertyBlob) property).getInput().orElse(null));

        }

        // Done
    }

    /**
     * Finish editing a property through proxy
     */
    protected void commitImpl(Property property) {

        // propagate
        String value = chooser.getFile();

        InputSource is;
        File file = new File(value);
        if (file.exists()) {
            is = InputSource.get(file).orElse(null);
        } else {
            try {
                is = InputSource.get(new URL(value)).orElse(null);
            } catch (MalformedURLException mfue) {
                is = null;
            }
        }

        if (property instanceof PropertyFile) {
            ((PropertyFile) property).setValue(value, updateMeta.isSelected());
        }

        if (property instanceof PropertyBlob) {
            ((PropertyBlob) property).load(is, updateMeta.isSelected());
        }

        // update preview
        preview.setSource(is);

        // done
    }

    @Override
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus) {
            return AncestrisActionProvider.EMPTY_ACTIONS;
        }
        List<Action> result = new ArrayList<Action>(1);
        if (nodes != null) {
            PropertyFile file = (PropertyFile) getProperty();
            if (file != null) {
                result.add(new RunExternal(((FileInput) file.getInput().get()).getFile()));
            }
        }
        return result;
    }

} //FileBean
