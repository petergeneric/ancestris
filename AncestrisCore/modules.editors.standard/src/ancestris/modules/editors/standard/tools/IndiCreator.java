/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class IndiCreator {
    
    public static int CREATION = 0;
    public static int ATTACH = 1;
    public static int DETACH = 2;
    
    public static int REL_FATHER = 1;
    public static int REL_MOTHER = 2;
    public static int REL_BROTHER = 3;
    public static int REL_SISTER = 4;
    public static int REL_PARTNER = 5;
    public static int REL_CHILD = 6;
    
    public static String[] RELATIONS = { "NONE", "FATHER", "MOTHER", "BROTHER", "SISTER", "PARTNER", "CHILD" };
    
    private Indi indiCreated;

    
    public IndiCreator(final int mode, final Indi sourceIndi, final int relation, final Indi currentSpouse, final Indi target) {
        Gedcom gedcom = sourceIndi.getGedcom();
        
        
        try {
            if (!gedcom.isWriteLocked()) {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        
                        // Case of creation
                        if (mode == CREATION && target == null) {
                            indiCreated = (Indi) gedcom.createEntity(Gedcom.INDI);
                            indiCreated.addDefaultProperties();

                            if (relation == REL_FATHER) {
                                linkParent(sourceIndi, indiCreated, true);
                            } else if (relation == REL_MOTHER) {
                                linkParent(sourceIndi, indiCreated, false);
                            } else if (relation == REL_BROTHER) {
                                linkSibling(sourceIndi, indiCreated, true);
                            } else if (relation == REL_SISTER) {
                                linkSibling(sourceIndi, indiCreated, false);
                            } else if (relation == REL_PARTNER) {
                                linkPartner(sourceIndi, indiCreated);
                            } else if (relation == REL_CHILD) {
                                linkChild(sourceIndi, indiCreated, currentSpouse);
                            }
                            return;
                        }
                        
                        // Case of attachment to existing target
                        if (mode == ATTACH && target != null) {
                            indiCreated = target;
                            if (relation == REL_FATHER) {
                                linkParentToTarget(sourceIndi, target, true);
                            } else if (relation == REL_MOTHER) {
                                linkParentToTarget(sourceIndi, target, false);
                            } else if (relation == REL_BROTHER) {
                                linkSiblingToTarget(sourceIndi, target);
                            } else if (relation == REL_SISTER) {
                                linkSiblingToTarget(sourceIndi, target);
                            } else if (relation == REL_PARTNER) {
                                linkPartnerToTarget(sourceIndi, target);
                            } else if (relation == REL_CHILD) {
                                linkChildToTarget(sourceIndi, target, currentSpouse);
                            }
                            return;
                        }
                        
                        // Case of detachment from existing target
                        if (mode == DETACH && target != null) {
                            indiCreated = null;
                            if (relation == REL_FATHER) {
                                unlinkParentFromTarget(sourceIndi, target, true);
                            } else if (relation == REL_MOTHER) {
                                unlinkParentFromTarget(sourceIndi, target, false);
                            } else if (relation == REL_BROTHER) {
                                unlinkSiblingFromTarget(sourceIndi, target);
                            } else if (relation == REL_SISTER) {
                                unlinkSiblingFromTarget(sourceIndi, target);
                            } else if (relation == REL_PARTNER) {
                                unlinkPartnerFromTarget(sourceIndi, target);
                            } else if (relation == REL_CHILD) {
                                unlinkChildFromTarget(sourceIndi, target, currentSpouse);
                            }
                            return;
                        }
                        
                    }


                });
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Indi getIndi() {
        return indiCreated;
    }
    
    
    //
    //
    //  CREATION OF RELATIONS
    //
    //
    

    private void linkParent(Indi child, Indi parent, boolean isFather) throws GedcomException {

        // Get family parents of child and if it does not exist, create it and add child to family.
        Fam fam = child.getFamilyWhereBiologicalChild();
        if (fam == null) {
            fam = (Fam) child.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            fam.addChild(child);
        }
        
        // Add default properties to to newly created parent
        parent.addDefaultProperties();
        parent.setSex(isFather ? PropertySex.MALE : PropertySex.FEMALE);
        if (isFather || GedcomOptions.getInstance().isSetWifeLastname()) { 
            parent.setName("", child.getLastName());
        }
        
        // Link family to newly created parent
        if (isFather) { 
            fam.setHusband(parent);
        } else {
            fam.setWife(parent);
        }

        // Create spouse if defaulted in the preferences and link it to family
        if (genj.gedcom.GedcomOptions.getInstance().getCreateSpouse() && fam.getNoOfSpouses() < 2) {
            Indi spouse = (Indi) child.getGedcom().createEntity(Gedcom.INDI);
            spouse.addDefaultProperties();
            spouse.setSex(isFather ? PropertySex.FEMALE : PropertySex.MALE);
            if (isFather) {
                if (GedcomOptions.getInstance().isSetWifeLastname()) {
                    spouse.setName("", child.getLastName());
                }
                fam.setWife(spouse);
            } else {
                spouse.setName("", child.getLastName());
                fam.setHusband(spouse);
            }
            
        }
    }
    
    
    private void linkSibling(Indi existingSibling, Indi newSibling, boolean isBrother) throws GedcomException {
        // Get family parents of existing sibling and if it does not exist, create it and add existing sibling to family.
        Fam fam = existingSibling.getFamilyWhereBiologicalChild();
        if (fam == null) {
            fam = (Fam) existingSibling.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            fam.addChild(existingSibling);
        }
        
        // Add default properties to newSibling and give it same lastname
        newSibling.addDefaultProperties();
        newSibling.setSex(isBrother ? PropertySex.MALE : PropertySex.FEMALE);
        newSibling.setName("", existingSibling.getLastName());
        
        // Link newSibling to family
        fam.addChild(newSibling);
    }
    
    private void linkPartner(Indi spouse, Indi otherSpouse) throws GedcomException {
        // Create new family where spouse and otherSpouse are husband and wife, or vice-versa
        Fam fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM);
        fam.addDefaultProperties();
        
        // Add default properties to to newly created spouse
        otherSpouse.addDefaultProperties();
        
        // Attach both spouses to family
        boolean isHusband = spouse.getSex() != PropertySex.FEMALE;
        if (isHusband) {
            otherSpouse.setSex(PropertySex.FEMALE);
            fam.setHusband(spouse);
            fam.setWife(otherSpouse);
        } else {
            otherSpouse.setSex(PropertySex.MALE);
            fam.setHusband(otherSpouse);
            fam.setWife(spouse);
        }
    }
    
    private void linkChild(Indi parent, Indi child, Indi currentSpouse) throws GedcomException {
        // Get family where parent is a spouse, and if it does not exist, create it and add existing parent to it.
        Fam fam = null;
        Fam[] fams = parent.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 0) {
            fam = (Fam) parent.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            if (parent.getSex() != PropertySex.FEMALE) {
                fam.setHusband(parent);
            } else {
                fam.setWife(parent);
            }
            child.addDefaultProperties();
            fam.addChild(child);
        } else {
            for (Fam f : fams) {
                Indi spouse = f.getOtherSpouse(parent);
                if (spouse == currentSpouse) {
                    child.addDefaultProperties();
                    f.addChild(child);
                }
            }
        }
    }
    
    
    
    //
    //
    //  ATTACHMENTS TO EXISTING RELATIONS
    //
    //

    
    private void linkParentToTarget(Indi child, Indi parent, boolean isFather) throws GedcomException {
        // If parent is already in a spouse family, use first one available 
        // (if user wanted another one, user should have attached the child from 
        // the parent where it is possible to choose spouse)
        Fam[] fams = parent.getFamiliesWhereSpouse();
        if (fams != null && fams.length > 0) {
            Fam fam = fams[0];
            fam.addChild(child);
            return;
        } 
        
        // Otherwise if child does not have already a family, create it
        Fam fam = child.getFamilyWhereBiologicalChild();
        if (fam == null) {
            fam = (Fam) parent.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            fam.addChild(child);
        }
        
        // Use child's family to set/reset parent
        if (isFather) {
            fam.setHusband(parent);
        } else {
            fam.setWife(parent);
        }
        
    }    

    private void linkSiblingToTarget(Indi existingSibling, Indi otherSibling) throws GedcomException {
        // Get family parents of one of the siblings. If both exist, use exising sibling's one. If none exist create it to existing sibling.
        Fam fam = existingSibling.getFamilyWhereBiologicalChild();
        if (fam != null) {
            fam.addChild(otherSibling);
            return;
        }
        
        // Then fam is null, check if second family is also null
        Fam fam2 = otherSibling.getFamilyWhereBiologicalChild();
        if (fam2 == null) {
            fam = (Fam) existingSibling.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            fam.addChild(existingSibling);
            fam.addChild(otherSibling);
            return;
        }
        
        // Else, link existingSibling to family 2
        fam2.addChild(existingSibling);
        
    }
    
    private void linkPartnerToTarget(Indi spouse, Indi otherSpouse) throws GedcomException {
        // Create new family where spouse and otherSpouse are husband and wife, or vice-versa
        Fam fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM);
        fam.addDefaultProperties();
        
        // Attach both spouses to family
        boolean isHusband = spouse.getSex() != PropertySex.FEMALE;
        if (isHusband) {
            fam.setHusband(spouse);
            fam.setWife(otherSpouse);
        } else {
            fam.setHusband(otherSpouse);
            fam.setWife(spouse);
        }
        
    }

    private void linkChildToTarget(Indi parent, Indi child, Indi currentSpouse) throws GedcomException {
        // Get family where parent is a spouse, and if it does not exist, create it and add existing parent to it.
        Fam fam = null;
        Fam[] fams = parent.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 0) {
            fam = (Fam) parent.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            if (parent.getSex() != PropertySex.FEMALE) {
                fam.setHusband(parent);
            } else {
                fam.setWife(parent);
            }
            fam.addChild(child);
        } else {
            for (Fam f : fams) {
                Indi spouse = f.getOtherSpouse(parent);
                if (spouse == currentSpouse) {
                    f.addChild(child);
                }
            }
        }
        
    }

    
    
    

    //
    //
    //  DETACHMENTS FROM EXISTING RELATIONS
    //
    //

    private void unlinkParentFromTarget(Indi child, Indi parent, boolean isFather) throws GedcomException {
        // Find family of the child, parent should be the parent 
        Fam fam = child.getFamilyWhereBiologicalChild();
        if (fam == null) {
            // nothing to detach
            return;
        }
        
        // Check that parent is a parent
        if ((isFather && fam.getHusband() != parent) || (!isFather && fam.getWife() != parent)) {
            return;
        }
        
        Property propToDelete = null;
        
        // Get family property in parent entity
        Property[] props = parent.getProperties("FAMS");
        for (Property prop : props) {
            PropertyFamilySpouse pfs = (PropertyFamilySpouse) prop;
            Fam fam2 = pfs.getFamily();
            if (fam == fam2) {
                propToDelete = prop;
                break;
            }
        }
        
        if (propToDelete != null) {
            parent.delProperty(propToDelete);
        }
        
        // If no individual is attached to the family, destroy it
        cleanFamily(fam);
        
    }    

    private void unlinkSiblingFromTarget(Indi existingSibling, Indi otherSibling) throws GedcomException {
        // Find family of the existingSibling and othersibling
        Fam fam1 = existingSibling.getFamilyWhereBiologicalChild();
        if (fam1 == null) {
            // nothing to detach
            return;
        }
        
        // Check that othersibling is a sibling (it should be)
        Fam fam2 = otherSibling.getFamilyWhereBiologicalChild();
        if (fam2 == null || fam2 != fam1) {
            // nothing to detach
            return;
        }

        // Remove link of otherSibling to fam1
        Property prop = otherSibling.getProperty("FAMC");
        otherSibling.delProperty(prop);
        
        // If no individual is attached to the family, destroy it
        cleanFamily(fam1);
        
    }
    
    private void unlinkPartnerFromTarget(Indi spouse, Indi otherSpouse) throws GedcomException {

        Fam fam = null;
        Property propToDelete = null;
        
        // Get family where both spouses are linked
        Property[] props = otherSpouse.getProperties("FAMS");
        for (Property prop : props) {
            PropertyFamilySpouse pfs = (PropertyFamilySpouse) prop;
            fam = pfs.getFamily();
            if ((fam.getHusband() == spouse || fam.getHusband() == otherSpouse) && (fam.getWife() == spouse || fam.getWife() == otherSpouse)) {
                propToDelete = prop;
                break;
            }
        }
        
        if (propToDelete != null) {
            otherSpouse.delProperty(propToDelete);
        }
        
        // If no individual is attached to the family, destroy it
        cleanFamily(fam);
    }

    private void unlinkChildFromTarget(Indi parent, Indi child, Indi currentSpouse) throws GedcomException {

        // Get family where child is a child and check for father
        Fam fam = child.getFamilyWhereBiologicalChild();
        if (fam.getHusband() == parent || fam.getWife() == parent) {
            Property prop = child.getProperty("FAMC");
            child.delProperty(prop);
        }

        // If no individual is attached to the family, destroy it
        cleanFamily(fam);
        
    }

    
    private void cleanFamily(Fam fam) {
        
        if (fam == null) {
            return;
        }
        
        // Destroy family if no people attached to it
        Indi husband = fam.getHusband();
        Indi wife = fam.getWife();
        Indi[] children = fam.getChildren();
        
        if (husband == null && wife == null && (children == null || children.length == 0)) {
            Gedcom gedcom = fam.getGedcom();
            gedcom.deleteEntity(fam);
        }
        
    }
    
}
