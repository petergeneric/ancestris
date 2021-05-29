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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.TagPath;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;

/**
 * Swap HUSB/WIFE for family
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.SwapSpouses")
@ActionRegistration(displayName = "#swap.spouses",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 535)})
public class SwapSpouses extends AbstractChange {

    /** fam */
    private Fam fam;

    /**
     * Constructor
     */
    public SwapSpouses() {
        super();
        //FIXME: we must have some static method to get an image for a tag
        ImageIcon img = Grammar.V55.getMeta(TagPath.valueOf("FAM")).getImage();
        setImageText(img, resources.getString("swap.spouses"));
        setTip(resources.getString("swap.spouses.tip"));
    }

    public SwapSpouses(Fam family) {
        setImageText(family.getImage(false), resources.getString("swap.spouses"));
        setTip(resources.getString("swap.spouses.tip"));
        fam = family;
        contextChanged();
    }

    private Lookup.Result<Fam> lkpFam;

    @Override
    protected void initLookupListner() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (context == null) {
            return;
        }
        if (lkpFam != null) {
            return;
        }

        //The thing we want to listen for the presence or absence of
        //on the global selection
        lkpFam = context.lookupResult(Fam.class);
        lkpFam.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Fam> subs = lkpFam.allInstances();
        if (subs.isEmpty()) {
            fam = null;
        } else {
            fam = subs.iterator().next();
        }
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (fam != null && fam.getNoOfSpouses() != 0) {
            setEnabled(true);
            setTip(resources.getString("swap.spouses.tip"));
        } else {
            setEnabled(false);
        }
    }

    /**
     * @see genj.edit.actions.AbstractChange#change()
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        fam.swapSpouses();
        return null;
    }
} //SwapSpouses
