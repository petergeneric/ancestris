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

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Submitter;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Set the submitter of a gedcom file
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.SetSubmitter")
@ActionRegistration(displayName = "#submitter.short"
//,iconBase = "genj.gedcom.images/Submitter.png"
        ,
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position=550)
})
@NbBundle.Messages("submitter.short=Set submitter")
public class SetSubmitter extends AbstractChange {

    /** the submitter */
    private Submitter submitter;

    public SetSubmitter() {
        this(null);
    }

    /**
     * Constructor
     */
    public SetSubmitter(Submitter sub) {
        super();
        setImageText(Gedcom.getEntityImage(Gedcom.SUBM), resources.getString("submitter.short"));
//        setImageText(Gedcom.getEntityImage(Gedcom.SUBM), resources.getString("submitter", sub.getGedcom().getName()));
        submitter = sub;
        contextChanged();
    }

    @Override
    protected final void contextChanged() {
        submitter = null;
        if (contextProperties.size() == 1 && contextProperties.get(0) instanceof Submitter) {
            submitter = (Submitter) (contextProperties.get(0));
        }

        if (submitter != null && submitter.getGedcom().getSubmitter() != submitter) {
            setEnabled(true);
            setTip(resources.getString("submitter.tip", getGedcom().getName()));
        } else {
            setEnabled(false);
            setTip(resources.getString("submitter.tip", ""));
        }
    }

    /**
     * set the submitter
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        submitter.getGedcom().setSubmitter(submitter);
        return null;
    }
} //SetSubmitter

