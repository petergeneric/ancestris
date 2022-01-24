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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.util.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Action that knows how to create a spouse for an individual
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateSpouse")
@ActionRegistration(displayName = "#add.spouse",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(position=400,separatorBefore=390,path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateSpouse extends CreateRelationship {

    private Indi spouse;
    private final static ImageIcon IMG_SPOUSE = new ImageIcon(CreateParent.class, "/genj/edit/images/Spouse.png");

    /** constructor */
    public CreateSpouse() {
        this(null);
    }

    /** constructor */
    public CreateSpouse(Indi spouse) {
        super(resources.getString("add.spouse"), Gedcom.INDI);
        setImage(IMG_SPOUSE);
        setContextProperties(spouse);
        contextChanged();
    }

    @Override
    protected final void contextChanged() {
        Indi contextSpouse = null;
        if (contextProperties.size() == 1 && contextProperties.get(0) instanceof Indi) {
            contextSpouse = (Indi) (contextProperties.get(0));
        }
        if (contextSpouse != null) {
            spouse = contextSpouse;
            setEnabled(true);
            setTip(getDescription());
        } else {
            setEnabled(false);
            setTip(null);
        }
    }

    /** warn about a spouse that already has spouses */
    @Override
    public String getWarning(Entity target) {
        if (spouse == null) {
            return null;
        }
        int n = 0;
        Fam[] fams = spouse.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            if (fam.getOtherSpouse(spouse) != null) {
                n++;
            }
        }
        //int n = spouse.getNoOfFams();
        if (n > 0) {
            return resources.getString("add.spouse.warning", spouse.toString(), "" + n);
        }
        return null;
    }

    /** more about what this does */
    @Override
    public String getDescription() {
        // "Spouse of Meier, Nils" or "Spouse"
        return resources.getString("add.spouse.of", spouse);
    }

    /** do it - add a target spouse (might be new) to our well known spouse */
    @Override
    protected Property change(Entity target, boolean targetIsNew) throws GedcomException {

        // lookup family for spouse
        Fam[] fams = spouse.getFamiliesWhereSpouse();
        Fam fam = null;
        if (fams.length > 0) {
            fam = fams[0];
        }
        if (fam == null || fam.getNoOfSpouses() >= 2) {
            fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM).addDefaultProperties();
            fam.setSpouse(spouse);
        }

        // done
        return fam.setSpouse((Indi) target).getTarget();
    }
}
