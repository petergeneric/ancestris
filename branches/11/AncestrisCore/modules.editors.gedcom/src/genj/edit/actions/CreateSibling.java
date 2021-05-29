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
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.util.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * knows how to create a sibling for an individual
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateSibling")
@ActionRegistration(displayName = "#add.sibling",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(position=310,path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateSibling extends CreateRelationship {

    private final static ImageIcon IMG_BROTHER = new ImageIcon(CreateParent.class, "Brother.png");
    private final static ImageIcon IMG_SISTER = new ImageIcon(CreateParent.class, "Sister.png");
    private final static ImageIcon IMG_SIBLING = new ImageIcon(CreateParent.class, "Sibling.png");
    private Indi sibling;
    private int sex = PropertySex.UNKNOWN;

    /** constructor */
    public CreateSibling() {
        this(null, PropertySex.UNKNOWN);
    }

    /** constructor */
    public CreateSibling(Indi sibling, int sex) {
        super(calcName(sex), Gedcom.INDI);
        this.sex = sex;
        setImage(sex == PropertySex.MALE ? IMG_BROTHER : sex == PropertySex.FEMALE ? IMG_SISTER : IMG_SIBLING);
        setContextProperties(sibling);
        contextChanged();
    }

    private static String calcName(int sex) {
        if (sex == PropertySex.FEMALE) {
            return resources.getString("add.sister");
        } else if (sex == PropertySex.MALE) {
            return resources.getString("add.brother");
        } else {
            return resources.getString("add.sibling");
        }
    }

    @Override
    protected final void contextChanged() {
        sibling = null;
        if (contextProperties.size() == 1 && contextProperties.get(0) instanceof Indi) {
            sibling = (Indi) (contextProperties.get(0));
        }
        if (sibling != null) {
            setEnabled(true);
            setTip(resources.getString("link",getDescription()));
        } else {
            setEnabled(false);
            setTip(null);
        }
    }

    /** more about what we do */
    @Override
    public String getDescription() {
        // "Sibling of Meier, Nils (I1)"
        return resources.getString(sex == PropertySex.MALE ? "add.brother.of" : sex == PropertySex.FEMALE ? "add.sister.of" : "add.sibling.of", sibling);
    }

    /** do the change */
    @Override
    protected Property change(Entity target, boolean targetIsNew) throws GedcomException {

        // try to add target to sibling's family or vice versa
        PropertyXRef CHIL;

        Fam[] fams = sibling.getFamiliesWhereChild();
        if (fams.length > 0) {
            CHIL = fams[0].addChild((Indi) target);
        } else {

            // try to add sibling to target's family
            fams = ((Indi) target).getFamiliesWhereChild();
            if (fams.length > 0) {
                CHIL = fams[0].addChild(sibling);
            } else {

                // both indis are not children yet - create a new family
                Gedcom ged = sibling.getGedcom();
                Fam fam = (Fam) ged.createEntity(Gedcom.FAM);
                try {
                    CHIL = fam.addChild((Indi) target);
                } catch (GedcomException e) {
                    ged.deleteEntity(fam);
                    throw e;
                }

                // 20040619 adding missing spouse automatically now
                Indi husband = (Indi) ged.createEntity(Gedcom.INDI).addDefaultProperties();
                Indi wife = (Indi) ged.createEntity(Gedcom.INDI).addDefaultProperties();

                husband.setName("", sibling.getLastName());
                if (GedcomOptions.getInstance().isSetWifeLastname()) {
                    wife.setName("", sibling.getLastName());
                }

                fam.setHusband(husband);
                fam.setWife(wife);
                fam.addChild(sibling);
            }

        }

        // set it's name & gender if new
        if (targetIsNew) {
            Indi indi = (Indi) target;
            indi.setName("", sibling.getLastName());
            indi.setSex(sex);
        }

        // focus stays with sibling
        return CHIL.getTarget();
    }
}
