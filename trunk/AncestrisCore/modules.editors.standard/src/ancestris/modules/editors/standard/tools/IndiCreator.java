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

import ancestris.modules.editors.standard.IndiPanel;
import ancestris.util.SosaParser;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertySex;
import genj.gedcom.UnitOfWork;
import java.math.BigInteger;
import javax.swing.JButton;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class IndiCreator {
    
    public static int CREATION = 0;
    public static int ATTACH = 1;
    public static int DETACH = 2;
    public static int DESTROY = 9;
    
    public static int REL_NONE = 0;
    public static int REL_FATHER = 1;
    public static int REL_MOTHER = 2;
    public static int REL_BROTHER = 3;
    public static int REL_SISTER = 4;
    public static int REL_PARTNER = 5;
    public static int REL_CHILD = 6;
    
    public static String[] RELATIONS = { "NONE", "FATHER", "MOTHER", "BROTHER", "SISTER", "PARTNER", "CHILD" };
    
    private Indi indiCreated;

    private boolean success;
    
    public IndiCreator(final int mode, final Indi sourceIndi, final int relation, final Fam currentFam, final Indi target) {
        Gedcom gedcom = sourceIndi.getGedcom();
        success = false;
        
        try {
            if (gedcom != null && !gedcom.isWriteLocked()) {
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
                                linkPartner(sourceIndi, indiCreated, currentFam);
                            } else if (relation == REL_CHILD) {
                                linkChild(sourceIndi, indiCreated, currentFam);
                            }
                            success = true;
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
                                linkPartnerToTarget(sourceIndi, target, currentFam);
                            } else if (relation == REL_CHILD) {
                                linkChildToTarget(sourceIndi, target, currentFam);
                            }
                            success = true;
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
                                unlinkChildFromTarget(sourceIndi, target, currentFam);
                            }
                            success = true;
                            return;
                        }
                        
                        // Case of destruction
                        if (mode == DESTROY) {
                            gedcom.deleteEntity(sourceIndi);
                            success = true;
                        }
                        
                    }


                });
            }
        } catch (GedcomException ex) {
            success = false;
            Exceptions.printStackTrace(ex);
        }
    }

    public Indi getIndi() {
        return indiCreated;
    }
    
    public boolean isSuccessful() {
        return success;
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
        
        // Add Numbering SOSA if one already exists in Child
        for (Property prop : child.getProperties(Indi.TAG_SOSA)) {
            final SosaParser sosaString = new SosaParser(prop.getValue()); 
            parent.addProperty(Indi.TAG_SOSA, getNewSosaValue(sosaString, isFather));
        }
        for (Property prop : child.getProperties(Indi.TAG_SOSADABOVILLE)) {
            final SosaParser sosaString = new SosaParser(prop.getValue()); 
            if (sosaString.getDaboville() != null) {
                continue;
            }
            parent.addProperty(Indi.TAG_SOSADABOVILLE, getNewSosaValue(sosaString, isFather));
            
        }
    }

    private String getNewSosaValue(final SosaParser sosaString, boolean isFather) {
        String newSosa;
        final Integer newGen = sosaString.getGeneration() +1;
        if (isFather){
            newSosa= sosaString.getSosa().shiftLeft(1).toString() + " G" + newGen ;
        } else {
            newSosa= sosaString.getSosa().shiftLeft(1).add(BigInteger.ONE).toString() + " G" + newGen ;
        }
        return newSosa;
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
    
    private void linkPartner(Indi spouse, Indi otherSpouse, Fam currentFam) throws GedcomException {
        // Get family where spouse is a spouse, and if it does not exist, create it and add existing spouse to it.
        Fam fam = null;
        Fam[] fams = spouse.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 0 || currentFam == null) {  // family does not exist, so create it
            fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM);
        } else {  // fams exists and currentFam not null, use currentFam
            Indi husb = currentFam.getHusband();
            Indi wife = currentFam.getWife();
            if ((husb != null && husb.equals(spouse) && wife != null) || (wife != null && wife.equals(spouse) && husb != null)) {  // family already complete with husb and wife, create a new one
                fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM);
            } else {
                fam = currentFam;
            }
        }
        
        // Add default properties to to newly created spouse
        fam.addDefaultProperties();
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
    
    private void linkChild(Indi parent, Indi child, Fam currentFam) throws GedcomException {
        // Get family where parent is a spouse, and if it does not exist, create it and add existing parent to it.
        Fam fam = null;
        Fam[] fams = parent.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 0 || currentFam == null) {
            fam = (Fam) parent.getGedcom().createEntity(Gedcom.FAM);
            fam.addDefaultProperties();
            if (parent.getSex() != PropertySex.FEMALE) {  // male or unknown
                fam.setHusband(parent);
                child.setName("", parent.getLastName());
                if (currentFam != null) {
                    fam.setWife(currentFam.getOtherSpouse(parent));
                }
            } else {
                fam.setWife(parent);
                if (currentFam != null) {
                    fam.setHusband(currentFam.getOtherSpouse(parent));
                }
            }
            child.addDefaultProperties();
            fam.addChild(child);
        } else {
            for (Fam f : fams) {
                if (f == currentFam) {
                    child.addDefaultProperties();
                    f.addChild(child);
                    Indi father = f.getHusband();
                    if (father != null) {
                        child.setName("", father.getLastName());
                    }
                    return;
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

        //
        // Child and parent exist : the link has to be a family.
        // Both the parent or the child could already be in a family :
        // - The parent can be in several families
        // - The child can be in several families (theoretically, but only one should exist)
        // Do we take one of these families or do we create a new one ?
        // If we use an existing family, adding a child to it is ok. But adding a parent is only possible if it does not already exist (child has already a father for instance).
        // Conclusion:
        // 1. Collect all available families, from parent and child
        // 2. Ask user to choose one of them or a new one (panel)
        // 3. Family is selected, parent is added to fam, or else child is added to parent's family
        //
        
        JButton okButton = new JButton(NbBundle.getMessage(IndiPanel.class, "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(IndiPanel.class, "Button_Cancel"));
        Object[] options = new Object[] { okButton, cancelButton };
        FamChooser famChooser = new FamChooser(parent, child, isFather, okButton);
        if (!famChooser.existChoices() || 
            okButton == DialogManager.create(NbBundle.getMessage(FamChooser.class, "FamChooser.TITL_ChooseFamTitle", child.toString(true)), famChooser)
                    .setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).setDialogId("famChooserWindow").show()) {
            famChooser.updateGedcom();
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
    
    private void linkPartnerToTarget(Indi spouse, Indi otherSpouse, Fam currentFam) throws GedcomException {

        boolean isHusband = spouse.getSex() != PropertySex.FEMALE;
        
        // If currentFam is not null, try to use it
        if (currentFam != null) {
            if (isHusband) {
                Indi wife = currentFam.getWife();
                if (wife == null) {
                    currentFam.addDefaultProperties();
                    currentFam.setWife(otherSpouse);
                    return;
                }
            } else {
                Indi husb = currentFam.getHusband();
                if (husb == null) {
                    currentFam.addDefaultProperties();
                    currentFam.setHusband(otherSpouse);
                    return;
                }
            }
        }
        
        // Create new family where spouse and otherSpouse are husband and wife, or vice-versa
        Fam fam = (Fam) spouse.getGedcom().createEntity(Gedcom.FAM);
        fam.addDefaultProperties();
        
        // Attach both spouses to family
        if (isHusband) {
            fam.setHusband(spouse);
            fam.setWife(otherSpouse);
        } else {
            fam.setHusband(otherSpouse);
            fam.setWife(spouse);
        }
        
    }

    private void linkChildToTarget(Indi parent, Indi child, Fam currentFam) throws GedcomException {
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
                if (f == currentFam) {
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
        cleanFamily(fam, null, child);
        
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
        cleanFamily(fam1, null, existingSibling);
        
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
        cleanFamily(fam, spouse, null);
    }

    private void unlinkChildFromTarget(Indi parent, Indi child, Fam currentFam) throws GedcomException {

        // Get family where child is a child and check for father
        Fam fam = child.getFamilyWhereBiologicalChild();
        if (fam.getHusband() == parent || fam.getWife() == parent) {
            Property prop = child.getProperty("FAMC");
            child.delProperty(prop);
        }

        // If no individual is attached to the family, destroy it
        cleanFamily(fam, parent, null);
        
    }

    
    private void cleanFamily(Fam fam, Indi parent, Indi child) {

        if (fam == null) {
            return;
        }
        
        boolean destroy = false;
        
        // Destroy family if no people attached to it
        Indi husband = fam.getHusband();
        Indi wife = fam.getWife();
        Indi[] children = fam.getChildren();
        int nbProps = fam.getNoOfProperties(); // equals 2 when only one indi (child, husb or wife) and CHAN is left.
        
        // Destroy family if parent is not null and husband or wife is the only property left attached to the family
        if (parent != null && (husband == parent || wife == parent) && nbProps <= 2) {
            destroy = true;
        }
        
        // Destroy family if child is not null and child is the only property left attached to the familly
        if (child != null && child.isChildIn(fam) && nbProps <= 2) {
            destroy = true;
        }

        // Destroy
        if (destroy) {
            Gedcom gedcom = fam.getGedcom();
            gedcom.deleteEntity(fam);
        }
        
    }
    
}
