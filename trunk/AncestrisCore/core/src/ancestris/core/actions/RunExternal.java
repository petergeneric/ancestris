/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ancestris.core.actions;

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.util.Resources;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

/**
 * External action
 */
@ActionID(category = "Edit", id = "ancestris.core.actions.RunExternal")
@ActionRegistration(displayName = "Run External",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 740)})
@NbBundle.Messages({"file.open=Open..."})
public class RunExternal extends AbstractAncestrisContextAction {

    /** the wrapped file */
    private File file;
    private final static Resources RESOURCES = Resources.get(RunExternal.class);

    public RunExternal() {
        super();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        // valid only for context aware action
        if (lkpInfo != null) {
            file = null;
            for (Property prop : lkpInfo.allInstances()) {
                if (prop instanceof PropertyFile) {
                    InputSource is = ((PropertyFile) prop).getInput().orElse(null);
                    if (is != null && is instanceof FileInput) {
                        file = ((FileInput) is).getFile();
                    }
                }
            }
            super.resultChanged(ev);
        }
    }

    @Override
    protected void contextChanged() {
        super.contextChanged();
        setImage(PropertyFile.DEFAULT_IMAGE);
        setText(RESOURCES.getString("file.open"));
        setTip(RESOURCES.getString("file.open.tip"));
        setEnabled(file != null && file.exists());
    }

    /**
     * Constructor
     */
    public RunExternal(File file) {
        this.file = file;
        super.setImage(PropertyFile.DEFAULT_IMAGE);
        super.setText(RESOURCES.getString("file.open"));
        super.setTip(RESOURCES.getString("file.open.tip"));
        setEnabled(file != null && file.exists());
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        if (file == null) {
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException t) {
            Logger.getLogger("ancestris.edit.actions").log(Level.INFO, "can't open " + file, t);
        }
    }
} //RunExternal
