/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package genj.gedcom;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frederic
 */
public abstract class PropertyEventDetails extends Property {

    public PropertyEventDetails(String tag) {
        super(tag);
    }

    /**
     * Returns the date of the event
     */
    public PropertyDate getDate() {
        return getDate(true);
    }

    /**
     * Returns the date of the event
     *
     * @param valid specifies wether data has to be valid to be found
     */
    public PropertyDate getDate(boolean valid) {

        // Try to get date-property which is valid
        Property prop = getProperty("DATE", valid);
        if (prop == null) {
            return null;
        }

        // Return as Date
        return (PropertyDate) prop;
    }

    /**
     * Calculate event's date
     */
    public String getDateAsString() {
        Property date = getProperty("DATE");
        return date != null ? date.getValue() : "";
    }


    @Override
    void propagatePropertyChanged(Property property, String oldValue) {
        super.propagatePropertyChanged(property, oldValue);
        
        if (!isEvent()) {
            return;
        }
        
        if (property.getGedcom() == null || property.getGedcom().getOrigin() == null || property.getGedcom().isUndoRedoInProgress()) {
            return;
        }

        // sniff for changes in date => update all events if BIRT or CHR, only itself otherwise
        if (property instanceof PropertyDate && getProperty("DATE") == property) {
            List<Property> list = new ArrayList<>();
            if (getParent().getProperty("BIRT") == this || getParent().getProperty("CHR") == this) {
                list.addAll(((Indi)getEntity()).getEvents());
            } else {
                list.add(this);
            }
            for (Property event : list) {
                ((PropertyEventDetails)event).updateAge(false, false, false);
            }
        }

        // done
    }

    
    /**
     * Update age information for this event depending of Indi or Fam
     *
     * @param force
     * @param overwriteString
     * @param isGuessed
     */
    public void updateAge(boolean force, boolean overwriteString, boolean isGuessed) {

        PropertyDate date = getDate(true);
        if (date == null) {
            return;
        }
        
        // update or create current age property depending on whether it is an INDIÂ or a FAMÂ event
        if (Gedcom.INDI.equals(getEntity().getTag())) {
            PropertyAge age = (PropertyAge) getProperty("AGE", false);
            if ("BIRT".equals(getTag())) {  // no AGE tag for birth
                if (age != null) {
                    delProperty(age);
                }
            } else {
                if (age == null && (GedcomOptions.getInstance().isAddAge() || force)) {
                    age = (PropertyAge) addProperty("AGE", "");  // this generates an update age in the notification 
                } else if (age != null) {
                    age.updateAge(overwriteString);
                }
                if (age != null) {
                    age.setGuessed(isGuessed);
                }
            }
        } else {
            Property husb = getProperty("HUSB");
            if (husb == null && (GedcomOptions.getInstance().isAddAge() || force)) {
                husb = addProperty("HUSB", "");
                }
            if (husb != null) {
                PropertyAge age = (PropertyAge) husb.getProperty("AGE", false);
                if (age == null && (GedcomOptions.getInstance().isAddAge() || force)) {
                    age = (PropertyAge) husb.addProperty("AGE", "");   // this generates an update age in the notification 
                } else if (age != null) {
                    age.updateAge(overwriteString);
                }
                if (age != null) {
                    age.setGuessed(isGuessed);
                }
            }
            
            Property wife = getProperty("WIFE");
            if (wife == null && (GedcomOptions.getInstance().isAddAge() || force)) {
                wife = addProperty("WIFE", "");
                }
            if (wife != null) {
                PropertyAge age = (PropertyAge) wife.getProperty("AGE", false);
                if (age == null && (GedcomOptions.getInstance().isAddAge() || force)) {
                    age = (PropertyAge) wife.addProperty("AGE", "");   // this generates an update age in the notification 
                } else if (age != null) {
                    age.updateAge(overwriteString);
                }
                if (age != null) {
                    age.setGuessed(isGuessed);
                }
            }
        }

    }

    

    
}
