/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
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
package genj.edit.actions;

import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import java.awt.event.ActionEvent;
import java.util.Collection;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;

/**
 * TogglePrivate - toggle "private" of a property
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.TogglePropertyPrivate")
@ActionRegistration(displayName = "#private",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position= 640)})
public class TogglePrivate extends AbstractChange {

    /** the properties */
    private Collection<? extends Property> properties;
    /** make public or private */
    private boolean makePrivate;

    public TogglePrivate() {
        super();
        setImageText(MetaProperty.IMG_PRIVATE, resources.getString("private"));
        setTip(resources.getString("private.tip"));
    }

    /**
     * Constructor
     */
    public TogglePrivate(Collection<? extends Property> properties) {
        this();
        this.properties = properties;
        contextChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        properties = lkpInfo.allInstances();

        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (properties.isEmpty()) {
            setEnabled(false);
            setText(resources.getString("private"));
            setTip(resources.getString("private.tip"));
        } else {
            setEnabled(true);
            // assuming we want to make them all private
            makePrivate = true;
            for (Property p : properties) {
                if (p.isPrivate()) {
                    makePrivate = false;
                }
            }
            setText(resources.getString(makePrivate ? "private" : "public") + "...");
            setTip(resources.getString(makePrivate ? "private.tip" : "public.tip"));
        }
    }

    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {

        // check if that's something we can do
        String pwd = gedcom.getPassword();
        if (Gedcom.PASSWORD_UNKNOWN.equals(pwd)) {
            DialogManager.createError(getText(), "This Gedcom file contains encrypted information that has to be decrypted before changing private/public status of other information")
                    .setMessageType(DialogManager.WARNING_MESSAGE).show();
            return null;
        }

        // check gedcom
        if (pwd == null) {

            pwd = DialogManager.create(getText(), AbstractChange.resources.getString("password", gedcom.getName()), "").show();

            // canceled?
            if (pwd == null) {
                return null;
            }
        }

        // check if the user wants to do it recursively
        boolean recursive = (DialogManager.YES_OPTION == DialogManager.createYesNo(getText(), AbstractChange.resources.getString("recursive")).show());

        // change it
        gedcom.setPassword(pwd);

        for (Property p : properties) {
            p.setPrivate(makePrivate, recursive);
        }

        // done
        return null;
    }
} //TogglePrivate

