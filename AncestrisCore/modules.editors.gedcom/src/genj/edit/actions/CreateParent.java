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
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.util.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Create a child of a family or person
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateParent")
@ActionRegistration(displayName = "#create.parent")
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateParent extends CreateRelationship {

    private final static ImageIcon IMG = new ImageIcon(CreateParent.class, "Parents.png");
    /** the child and family we're creating a parent for */
    private Entity entity;
    private Indi child;
    private Fam family;
    private int sex = -1;

    public CreateParent() {
        super(resources.getString("create.parent"), Gedcom.INDI);
        setImage(IMG);
//XXX:        setImageText(IMG, resources.getString("create.parent"));
    }

    /** constructor */
    public CreateParent(Entity entity) {
        this(entity, -1);
    }

    public CreateParent(Entity entity, int sex) {
        this();
//        setImageText(IMG, resources.getString("create.parent"));
        this.entity = entity;
//FIXME: sex not used?        initialize(entity, sex);
        contextChanged();
    }

    private boolean initialize(Entity entity, int sex) {
        boolean success = false;

        if (entity == null) {
            return false;
        }
        if (entity instanceof Fam) {
            family = (Fam) entity;
            this.child = null;
            this.sex = sex;
            if (family.getNoOfSpouses() < 2) {
                success = true;
            }
        }
        if (entity instanceof Indi) {
            child = (Indi) entity;
            family = null;
            this.sex = sex;

            // check if the child already is part of a family without spouse
            Fam[] fams = child.getFamiliesWhereChild();
            for (int f = 0; f < fams.length; f++) {
                if (fams[f].getNoOfSpouses() < 2) {
                    family = fams[f];
                    break;
                }
            }
            success = true;
        }
        return success;
    }

    @Override
    protected final void contextChanged() {
        entity = null;
        if (contextProperties.size() == 1 && contextProperties.get(0) instanceof Entity) {
            entity = (Entity) (contextProperties.get(0));
        }
        if (entity != null && initialize(entity, -1)) {
            setEnabled(true);
            setTip(resources.getString("link", getDescription()));
        } else {
            setEnabled(false);
            setTip(resources.getString("create.parent"));
        }
    }

    /** description of what this'll do */
    @Override
    public String getDescription() {
        // "Parent of Meier, Nils (I1)"
        if (child != null) {
            return resources.getString("create.parent.of", child);
        }
        // "Parent in Meier, Sven (I1) + Radovcic Sandra (I2) (F1)"
        return resources.getString("create.parent.in", family);
    }

    /** a warning in case the target indi is already a child of another family */
    @Override
    public String getWarning(Entity indi) {

        // do we have a child which already has parents?
        if (child != null && family == null) {
            Fam fam = child.getFamilyWhereBiologicalChild();
            if (fam != null) {
                return PropertyChild.getLabelChildAlreadyinFamily(child, fam);
            }
        }
        // no problem
        return null;

    }

    /** change impl */
    @Override
    protected Property change(Entity parent, boolean parentIsNew) throws GedcomException {

        String lastname;
        Gedcom ged = parent.getGedcom();
        PropertyXRef FAMS;

        if (parentIsNew && sex >= 0) {
            ((Indi) parent).setSex(sex);
        }

        // know the family already?
        if (family != null) {

            FAMS = family.setSpouse((Indi) parent).getTarget();
            Indi other = family.getOtherSpouse((Indi) parent);
            lastname = other != null ? other.getLastName() : "";

        } else { // need new family

            // lastname will match that of child
            lastname = child.getLastName();

            // create new family with child
            family = (Fam) ged.createEntity(Gedcom.FAM);
            family.addChild(child);
            family.addDefaultProperties();

            // set spouse
            FAMS = family.setSpouse((Indi) parent).getTarget();

            // 20040619 adding missing spouse automatically now
            // 20050405 whether we created a new family or the family didn't have all parents
            if (genj.gedcom.GedcomOptions.getInstance().getCreateSpouse() && family.getNoOfSpouses() < 2) {
                Indi spouse = (Indi) ged.createEntity(Gedcom.INDI);
                spouse.addDefaultProperties();
                family.setSpouse(spouse);
                if (GedcomOptions.getInstance().isSetWifeLastname() || spouse.getSex() == PropertySex.MALE) {
                    spouse.setName("", lastname);
                }
            }

        }

        // set name of parent if new
        if (parentIsNew && (((Indi) parent).getSex() == PropertySex.MALE || GedcomOptions.getInstance().isSetWifeLastname())) {
            ((Indi) parent).setName("", lastname);
        }

        // focus goes to new parent
        return FAMS;
    }
}
