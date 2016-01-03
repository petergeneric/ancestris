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
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyRelationship;
import genj.gedcom.PropertySex;
import genj.gedcom.time.PointInTime;

/**
 *
 * @author frederic
 */
class AssoWrapper {

    public PropertyAssociation assoProp = null; // The association property with the ASSO tag
    public String assoTxt = "";                 // The association text
    private String assoTag = "";                // The event TAG the association is supposingly refering to, regardless of whether the association is underneath an event or directly underneath INDI
    private Property[] assoEvents = null;       // The events with the TAG within the target entity, in case several events have the same tag
    private int assoSeqNb = 0;                  // The association sequence nb in case the association refers to several events with same event tag
    
    public Indi assoIndi = null;                // Indi the ASSO tag belongs to. The Gedcom norm only allows for INDI to have ASSO tags.
    public String assoLastname = "";            // The associated individual lastname
    public String assoFirstname = "";           // The associated individual firstname
    public int assoSex = PropertySex.UNKNOWN;   // The associated individual sex
    public String assoOccupation = "";          // The associated individual occupation

    private Entity targetEntity = null;         // Entity the association refers to. The Gedcom norm only allows for INDI to be pointed to by an ASSO tag. However, Ancestris allows FAM to be referenced too.
    private Indi targetIndi1 = null;            // First indi the association refers to. Itself in case of targetEntity is an INDI, husband if it is a FAM.
    private Indi targetIndi2 = null;            // Second indi the association refers to. Null in case of targetEntity is an INDI, wife if it is a FAM.
    private Property targetEventProp = null;    // Event of the entity, the association refers to.
    private String targetEventTag = "";         // Event tag of the associated event
    public String targetEventDesc = "";         // The event text to be displayed
    

    public AssoWrapper(PropertyForeignXRef xrefProperty) {

        if (xrefProperty == null) {
            return;
        }
        
        Indi associatedIndi = (Indi) xrefProperty.getTargetEntity();
        Property referredToProperty = xrefProperty.getParent();
        PropertyAssociation assoProperty = (PropertyAssociation) xrefProperty.getTarget();
        setValues(associatedIndi, assoProperty, referredToProperty);
        
    }

    /**
     * Set values based on ASSO tag and reference
     * 
     * @param associatedIndi : the indi with the ASSO tag
     * @param assoProperty : the ASSO property
     * @param referedToProperty : the event property pointed to by the ASSO relation (RELA)
     */
    private void setValues(Indi associatedIndi, PropertyAssociation assoProperty, Property referredToProperty) {

        // Get key elements
        assoProp = assoProperty;
        assoIndi = associatedIndi;
        targetEntity = referredToProperty.getEntity();
        targetEventProp = referredToProperty;
        
        // Get table elements
        assoLastname = assoIndi.getLastName();
        assoFirstname = assoIndi.getFirstName();
        assoSex = assoIndi.getSex();
        assoOccupation = getOccupation(assoIndi);
        targetEventDesc = assoProp.getDisplayValue(false);
        PropertyRelationship relaP = (PropertyRelationship) assoProp.getProperty("RELA");
        assoTxt = relaP.getDisplayValue();
    }

    private String getOccupation(Indi indi) {
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
    
    
}
