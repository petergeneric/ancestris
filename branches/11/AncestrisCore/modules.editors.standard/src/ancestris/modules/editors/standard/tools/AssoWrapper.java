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
        EventWrapper event = new EventWrapper(eventProp, associatedIndi, null);
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
        return (assoProp != null && assoProp.equals(object.assoProp));
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
        assoOccupation = getOccupation(assoIndi, targetEvent);
        targetEventDesc = assoProp.getDisplayValue(false);
        PropertyRelationship relaP = (PropertyRelationship) assoProp.getProperty("RELA");
        assoTxt = relaP.getDisplayValue();
    }

    public String getOccupation(Indi indi, EventWrapper event) {
        String occu = "";
        
        Property props[] = indi.getProperties("OCCU");
        
        // Return obvious value if no choice
        if (props.length == 0) {
            return "";
        }
        if (props.length == 1) {
            return props[0].getDisplayValue();
        }
        
        // Set default value in case nothing better found later
        if (props.length > 1) {
            occu = props[0].getDisplayValue(); 
        }

        // Select first valid occupation at the date of the event (latest but before sourcePIT), if any
        if (event != null) {
            PropertyDate pDate = event.date;
            PointInTime sourcePIT = pDate != null ? pDate.getStart() : null;

            PointInTime latestPIT = null;
            for (Property prop : props) {
                Property date = prop.getProperty("DATE");
                if (date != null) {
                    PropertyDate pdate = (PropertyDate) date;
                    PointInTime pit = pdate.getEnd();
                    if (!pit.isValid()) {
                        pit = pdate.getStart();
                    }
                    if (latestPIT == null || (pit.compareTo(latestPIT) > 0 && (pit.compareTo(sourcePIT) <= 0))) {
                        latestPIT = pit;
                        occu = prop.getDisplayValue();
                    }
                } else {
                    if (latestPIT == null) {
                        occu = prop.getDisplayValue();
                    }
                }
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
        updateProperty(assoIndi, "OCCU", assoOccupation, targetEvent.eventProperty);

        // If association is null, create it
        if (assoProp == null) {
            PropertyXRef assoXref = (PropertyXRef) assoIndi.addProperty("ASSO", "@");
            assoProp = (PropertyAssociation) assoXref;
        } else {
            removeLink(assoProp);
        }
        
        // Record values
        Utils.setDistinctValue(assoProp, '@' + targetEntity.getId() + '@');

        TagPath anchor = getAnchor(targetEvent.eventProperty);
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
        
        TagPath newAnchor = getAnchor(targetEvent.eventProperty);
        PropertyRelationship p = (PropertyRelationship) assoProp.getProperty("RELA");
        TagPath oldAnchor = p != null ? p.getAnchor() : null;
        if (oldAnchor == null || !oldAnchor.equals(newAnchor)) {
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
            Utils.setDistinctValue(prop, value);
        } else {
            property.addProperty(tag, value);
        }
    }

    private void removeLink(PropertyAssociation pa) {
        Property target = pa.getTarget();
        if (target == null) {
            return;
        }
        Property targetParent = target.getParent();
        pa.unlink();
        targetParent.delProperty(target);
    }

    private void removeAsso(Indi indi, PropertyAssociation pa) {
        indi.delProperty(pa);
    }

    private TagPath getAnchor(Property property) {
        if (property == null) {
            return null;
        }
        TagPath result = property.getPath(false);
        return property.getEntity().getProperty(result) == property ? result : property.getPath(true); 
    }

    /**
     * Update property of given tag:
     * - if new value is empty, nothing to do
     * - if tag does not exist, has valid newValue and valid newDate, add it        : (a) ADD
     * - if tag already exists with same value and same date, do nothing            : (b) -
     * - if tag already exists with same value and different date, do nothing       : (c) -
     * - if tag already exists with same value and no date, update oldDate          : (d) update DATE
     * - if tag already exists with different value and same date, update oldValue  : (e) update VALUE
     * - if tag already exists with different value and different date, add tag     : (f) ADD
     * @param property
     * @param tag
     * @param value
     * @param date 
     */
    private void updateProperty(Property property, String tag, String newValue, Property sourceEvent) {
        
        // Nothing to do if new value is empty
        if (newValue.isEmpty()) {
            return;
        }
        
        PropertyDate sourceDate = sourceEvent != null ? (PropertyDate) sourceEvent.getProperty("DATE") : null;
        String newDate = sourceDate != null ? sourceDate.getValue() : "";  // date of event if there is an event, null otherwise
        String oldValue = "";
        String oldDate = "";
        boolean tagExists = false;          // true if tag found (b, c, d, e, f)
        
        // Look for existing property with same value
        Property[] props = property.getProperties(tag);
        for (Property prop : props) { // b, c, d, e, f
            tagExists = true; 
            oldValue = prop.getValue().trim();
            PropertyDate pDate = (PropertyDate) prop.getProperty("DATE");
            oldDate = pDate != null ? pDate.getValue() : "";
            
            // Checks and updates
            if (newValue.equals(oldValue)) {   // b, c, d
                if (oldDate.isEmpty() && !newDate.isEmpty()) { // d
                    putProperty(prop, "DATE", newDate);
                    pDate = (PropertyDate) prop.getProperty("DATE");
                    pDate.setFormat(PropertyDate.BEFORE);
                }
                break; // done for b, c, d
            } else {   // e, f
                if (pDate != null && sourceDate != null) {
                    int days = 32;
                    try {
                        days = pDate.getStart().getJulianDay() - sourceDate.getStart().getJulianDay();
                        days = Math.abs(days);
                    } catch (Exception e) {
                    }
                    if (days < 32) { // e
                        prop.setValue(newValue);
                        break;
                    } else {
                        tagExists = false; // same as a
                    }
                } else { //f 
                    tagExists = false; // same as a
                }
            }
        }
        
        if (!tagExists) {  // a, f
            Property tagProp = property.addProperty(tag, newValue);
            if (!newDate.isEmpty()) {
                PropertyDate pDate = (PropertyDate) tagProp.addProperty("DATE", newDate);
                pDate.setFormat(PropertyDate.BEFORE);
            }
        }
        
    }

    
    
}
