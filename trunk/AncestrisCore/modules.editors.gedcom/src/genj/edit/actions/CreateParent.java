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
@ActionRegistration(displayName = "#add.parent",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(position=210, path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateParent extends CreateRelationship {

    private final static ImageIcon IMG = new ImageIcon(CreateParent.class, "/genj/edit/images/Parents.png");
    private final static ImageIcon IMG_FA = new ImageIcon(CreateParent.class, "/genj/edit/images/Father.png");
    private final static ImageIcon IMG_MO = new ImageIcon(CreateParent.class, "/genj/edit/images/Mother.png");

    /** the child and family we're creating a parent for */
    private Entity entity;
    private Indi child;
    private Fam family;
    private int sex = PropertySex.UNKNOWN;

    public CreateParent() {
        super(resources.getString("add.parent"), Gedcom.INDI);
    }

    /** constructor
     * @param entity */
    public CreateParent(Entity entity) {
        this(entity, PropertySex.UNKNOWN);
    }

    public CreateParent(Entity entity, int sex) {
        super(calcName(sex), Gedcom.INDI);
        this.entity = entity;
        this.sex = sex;
        setImage(sex == PropertySex.MALE ? IMG_FA : sex == PropertySex.FEMALE ? IMG_MO : IMG);
        initialize(entity, sex);
        contextChanged();
    }

    private static String calcName(int sex) {
        if (sex == PropertySex.FEMALE) {
            return resources.getString("add.mother");
        } else if (sex == PropertySex.MALE) {
            return resources.getString("add.father");
        } else {
            return resources.getString("add.parent");
        }
    }
    
    private boolean initialize(Entity entity, int sex) {

        if (entity == null) {
            return false;
        }
        
        if (entity instanceof Fam) {
            family = (Fam) entity;
            if (family.getNoOfChildren() > 0) {
                this.child = family.getChild(0);
            } else {
                this.child = null;
            }
            this.sex = sex;
            return family.acceptSpouse(sex);
        }
        if (entity instanceof Indi) {
            child = (Indi) entity;
            family = null;
            this.sex = sex;

            // check if the child already is part of a family without spouse
            Fam[] fams = child.getFamiliesWhereChild();
            if (fams.length == 0) {
                return true;
            }
            for (Fam fam : fams) {
                if (fam.acceptSpouse(sex)) {
                    family = fam;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected final void contextChanged() {
        entity = null;
        if (contextProperties.size() == 1 && contextProperties.get(0) instanceof Entity) {
            entity = (Entity) (contextProperties.get(0));
        }
        if (entity != null && initialize(entity, sex)) {
            setEnabled(true);
            setTip(resources.getString("link", getDescription()));
        } else {
            setEnabled(false);
            setTip(resources.getString("add.parent"));
        }
    }

    /** description of what this'll do
     * @return  */
    @Override
    public String getDescription() {
        // "Parent of Meier, Nils (I1)"
        if (child != null) {
            return resources.getString(sex == PropertySex.MALE ? "add.father.of" : sex == PropertySex.FEMALE ? "add.mother.of" : "add.parent.of", child);
        }
        // "Parent in Meier, Sven (I1) + Radovcic Sandra (I2) (F1)"
        return resources.getString(sex == PropertySex.MALE ? "add.father.in" : sex == PropertySex.FEMALE ? "add.mother.in" : "add.parent.in", family);
    }

    /** a warning in case the target indi is already a child of another family
     * @param indi
     * @return  */
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

    /** change impl
     * @param parent
     * @param parentIsNew
     * @return
     * @throws genj.gedcom.GedcomException  */
    @Override
    protected Property change(Entity parent, boolean parentIsNew) throws GedcomException {

        String lastname;
        Gedcom ged = parent.getGedcom();
        PropertyXRef FAMS;


        // know the family already?
        if (family != null) {

            if (parentIsNew && sex > 0) { // if sex is known, change it before calling setSpouse
                ((Indi) parent).setSex(sex);
            }
            FAMS = family.setSpouse((Indi) parent).getTarget();
            Indi other = family.getOtherSpouse((Indi) parent);
            
            // lastname will match that of child
            if (sex == PropertySex.MALE || GedcomOptions.getInstance().isSetWifeLastname()) {
                lastname = ((child != null) ? child.getLastName() : ((other != null) ? other.getLastName() : ""));
            } else {
                lastname = "";
            }
            
            if (parentIsNew && sex == PropertySex.UNKNOWN) { // if sex was unknown, leave it unknown, user had the choice to not leave unknown in the drop down menu
                ((Indi) parent).setSex(sex);
            }

        } else { // need new family

            // lastname will match that of child
            if (sex == PropertySex.MALE || GedcomOptions.getInstance().isSetWifeLastname()) {
                lastname = child.getLastName();
            } else {
                lastname = "";
            }

            // create new family with child
            family = (Fam) ged.createEntity(Gedcom.FAM);
            family.addChild(child);
            family.addDefaultProperties();

            if (parentIsNew && sex > 0) { // if sex is known, change it before calling setSpouse
                ((Indi) parent).setSex(sex);
            }

            // set spouse
            FAMS = family.setSpouse((Indi) parent).getTarget();
            if (parentIsNew && sex == 0) { // if sex was unknown, leave it unknown, user had the choice to not leave unknown in the drop down menu
                ((Indi) parent).setSex(sex);
            }

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
