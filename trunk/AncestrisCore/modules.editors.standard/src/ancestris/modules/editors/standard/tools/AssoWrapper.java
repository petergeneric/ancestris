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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class AssoWrapper {

    public PropertyAssociation assoProp = null; // The association property with the ASSO tag
    public String assoTxt = "";                 // The association text
    
    public Indi assoIndi = null;                // Indi the ASSO tag belongs to. The Gedcom norm only allows for INDI to have ASSO tags.
    public String assoLastname = "";            // The associated individual lastname
    public String assoFirstname = "";           // The associated individual firstname
    public int assoSex = PropertySex.UNKNOWN;   // The associated individual sex
    public String assoOccupation = "";          // The associated individual occupation

    private Entity targetEntity = null;         // Entity the association refers to. The Gedcom norm only allows for INDI to be pointed to by an ASSO tag. However, Ancestris allows FAM to be referenced too.
    public EventWrapper targetEvent = null;     // Event of the entity, the association refers to.
    private String targetEventTag = "";         // Event tag of the associated event
    public String targetEventDesc = "";         // The event text to be displayed
    

    public AssoWrapper(String text) {
        assoTxt = text;
    }

    public AssoWrapper(PropertyForeignXRef xrefProperty, EventWrapper event) {

        if (xrefProperty == null) {
            return;
        }
        
        Indi associatedIndi = (Indi) xrefProperty.getTargetEntity();
        PropertyAssociation assoProperty = (PropertyAssociation) xrefProperty.getTarget();
        setValues(associatedIndi, assoProperty, event);
        
    }

    public AssoWrapper(PropertyAssociation assoProperty) {

        if (assoProperty == null) {
            return;
        }
        
        Indi associatedIndi = (Indi) assoProperty.getEntity();
        Property eventProp = assoProperty.getTargetParent();
        EventWrapper event = new EventWrapper(eventProp, null, null, null);
        setValues(associatedIndi, assoProperty, event);
        
    }

    private AssoWrapper(AssoWrapper asso) {
        assoProp = asso.assoProp;
        assoTxt = asso.assoTxt;
        assoIndi = asso.assoIndi;
        assoLastname = asso.assoLastname;
        assoFirstname = asso.assoFirstname;
        assoSex = asso.assoSex;
        assoOccupation = asso.assoOccupation;
        targetEntity = asso.targetEntity;
        targetEvent = asso.targetEvent;
        targetEventTag = asso.targetEventTag;
        targetEventDesc = asso.targetEventDesc;
    }
    
    public boolean equals(AssoWrapper object) {
        return (assoIndi.equals(object.assoIndi)) && targetEvent.equals(object.targetEvent) && targetEventDesc.equals(object.targetEventDesc);
    }

    /**
     * Set values based on ASSO tag and reference
     * 
     * @param associatedIndi : the indi with the ASSO tag
     * @param assoProperty : the ASSO property
     * @param referedToProperty : the event property pointed to by the ASSO relation (RELA)
     */
    private void setValues(Indi associatedIndi, PropertyAssociation assoProperty, EventWrapper event) {

        // Get key elements
        assoProp = assoProperty;
        assoIndi = associatedIndi;
        targetEntity = event.eventProperty.getEntity();
        targetEvent = event;
        
        // Get table elements
        assoLastname = assoIndi.getLastName();
        assoFirstname = assoIndi.getFirstName();
        assoSex = assoIndi.getSex();
        assoOccupation = getOccupation(assoIndi);
        targetEventDesc = assoProp.getDisplayValue(false);
        PropertyRelationship relaP = (PropertyRelationship) assoProp.getProperty("RELA");
        assoTxt = relaP.getDisplayValue();
    }

    public String getOccupation(Indi indi) {
        String occu = "";
        // Select latest occupation
        Property props[] = indi.getProperties("OCCU");
        PointInTime latestPIT = null;
        for (Property prop : props) {
            Property date = prop.getProperty("DATE");
            if (date != null) {
                PropertyDate pdate = (PropertyDate) date;
                PointInTime pit = pdate.getEnd();
                if (!pit.isValid()) {
                    pit = pdate.getStart();
                }
                if (latestPIT == null || pit.compareTo(latestPIT) > 0) {
                    latestPIT = pit;
                    occu = prop.getDisplayValue();
                }
            } else {
                occu = prop.getDisplayValue();
            }
        }
        return occu;
    }

    public static AssoWrapper clone(AssoWrapper asso) {
        return new AssoWrapper(asso);
    }

    @Override
    public String toString() {
        String name = (assoLastname + " " + assoFirstname).trim();
        return assoTxt + (!name.isEmpty() ? " | " + name : "");
    }
    
    /**
     * Modify or create an association
     * 
     * @param indi 
     */
    public void update() {

        if (!isToBeUpdated()) {   // do no update if not necessary (otherwise it would change the modification timestamp)
            return;
        }
        
        // If target entity is null, create it
        if (targetEntity == null) {
            targetEntity = targetEvent.eventProperty.getEntity();
        }
        
        // If individual is to be created, create it
        if (assoIndi == null || assoIndi.getGedcom() == null) {
            try {
                assoIndi = (Indi) targetEntity.getGedcom().createEntity(Gedcom.INDI);
                assoProp = null;
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        // Update individual
        assoIndi.setName(assoFirstname, assoLastname);
        assoIndi.setSex(assoSex);
        putProperty(assoIndi, "OCCU", assoOccupation);

        // If association is null, create it
        if (assoProp == null) {
            PropertyXRef assoXref = (PropertyXRef) assoIndi.addProperty("ASSO", "@@");
            assoProp = (PropertyAssociation) assoXref;
        } else {
            removeLink(assoProp);
        }
        
        // Record values
        assoProp.setValue('@' + targetEntity.getId() + '@');

        TagPath anchor = targetEvent.eventProperty.getPath();
        putProperty(assoProp, "RELA", assoTxt + (anchor == null ? "" : '@' + anchor.toString()));
        
        // link it (adds the TYPE tag at the same time)
        try {
            assoProp.link();
        } catch (Exception ex) {
            removeAsso(assoIndi, assoProp);
            Exceptions.printStackTrace(ex);
        }
        
    }


    
    private boolean isToBeUpdated() {
        if (!assoIndi.getFirstName().equals(assoFirstname)) {
            return true;
        }
        if (!assoIndi.getLastName().equals(assoLastname)) {
            return true;
        }
        if (assoIndi.getSex() != assoSex) {
            return true;
        }
        Property occuProps[] = assoIndi.getProperties("OCCU");
        boolean found = false;
        for (Property p : occuProps) {
            String occuStr = "";
            if (p != null) {
                occuStr = p.getDisplayValue().trim();
            }
            if (occuStr.equals(assoOccupation.trim())) {
                found = true;
            }
        }
        if (!found) {
            return true;
        }
        
        if (targetEntity == null || assoIndi == null || assoIndi.getGedcom() == null || assoProp == null) {
            return true;
        }
        
        String val = assoProp.getValue();
        if (!val.equals('@' + targetEntity.getId() + '@')) {
            return true;
        }
        
        TagPath anchor = targetEvent.eventProperty.getPath();
        String newRela = assoTxt + (anchor == null ? "" : '@' + anchor.toString());
        PropertyRelationship p = (PropertyRelationship) assoProp.getProperty("RELA");
        String oldRela = p != null ? p.getValue() : "";
        if (!oldRela.equals(newRela)) {
            return true;
        }

        return false;
    }

    
    

    /**
     * 
     * @param indi 
     */
    public void remove() {
        if (assoIndi != null && assoProp != null) {
            removeLink(assoProp);
            removeAsso(assoIndi, assoProp);
        }
    }


    /**
     * Update or Create tag property with value to provided property
     * @param property : host property
     * @param tag : property tag
     * @param value : property value
     */
    private void putProperty(Property property, String tag, String value) {
        Property prop = property.getProperty(tag, true);
        if (prop != null) {
            prop.setValue(value);
        } else {
            property.addProperty(tag, value);
        }
    }

    private void removeLink(PropertyAssociation pa) {
        Property target = pa.getTarget();
        Property targetParent = target.getParent();
        pa.unlink();
        targetParent.delProperty(target);
    }

    private void removeAsso(Indi indi, PropertyAssociation pa) {
        indi.delProperty(pa);
    }

    
    
}
