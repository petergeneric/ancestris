/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import org.openide.util.NbBundle;



/**
 *
 * @author frederic
 */
public class EventWrapper {

    private Entity hostingEntity = null;     // INDI or FAM the event belongs to
    public Property eventProperty = null;    // the event
    
    public EventLabel eventLabel = null;     // for table
    public int eventYear = 0;                // for table
    public String eventAge = "";             // for table and label
    
    public String title = "";  
    public String description = "";
    public boolean showDesc = false;
    public PropertyDate date = null;
    public String dayOfWeek = null;
    public String age = "";
    public PropertyPlace place = null;
    
    
    public EventWrapper(Property property, Indi indi) {
        if (property == null) {
            return;
        }
        this.hostingEntity = property.getEntity();
        this.eventProperty = property;

        // Event description & icon
        this.eventLabel = new EventLabel(property);
        this.eventLabel.setIcon(property.getImage());
        
        // Title and description
        this.title = this.eventLabel.getShortLabel();
        String desc = property.getDisplayValue();
        Property type = property.getProperty("TYPE");
        this.description = (desc != null && !desc.isEmpty() ? desc : "") + (type != null ? type.getDisplayValue() : "");

        // Event date
        this.date = (PropertyDate) property.getProperty("DATE");
        try {
            if (date != null && date.getStart() != null) {
                this.dayOfWeek = date.getStart().getDayOfWeek(true);
            } else {
                this.dayOfWeek = "";
            }
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
            this.dayOfWeek = "";
        }

        // Event year
        if (this.date != null) {
            this.eventYear = date.getStart() == null ? 0 : date.getStart().getYear();
        }
        
        //
        if (indi == null) {
            return;
        }
        
        // Age of related indi at time of event
        Property prop = property.getProperty("AGE");
        if (prop != null && prop instanceof PropertyAge) {
            PropertyAge propAge = (PropertyAge) prop;
            propAge.updateAge();
            this.eventAge = propAge.getDecimalValue("#.###");
            if (eventAge.equals("0")) {
                eventAge = "-";
            }
            this.age = "(" + propAge.getPropertyName() + ": " + (isValidBirthDate(indi) || !eventAge.equals("-") ? propAge.getDisplayValue() : NbBundle.getMessage(getClass(), "Undetermined_Age")) + ")";
        } else {
            PropertyAge propAge = new PropertyAge("AGE");
            propAge.getAge(indi, eventProperty);
            this.eventAge = propAge.getDecimalValue("#.###");
            if (eventAge.equals("0")) {
                eventAge = "-";
            }
            this.age = "(" + propAge.getPropertyName() + ": " + (isValidBirthDate(indi) || !eventAge.equals("-") ? propAge.getDisplayValue() : NbBundle.getMessage(getClass(), "Undetermined_Age")) + ")";
        }
        if (this.date == null || property.getTag().equals("BIRT")) {
            this.age = "";
        }
        
        // Place of event
        this.place = (PropertyPlace) property.getProperty("PLAC");
    }

    public EventWrapper(Entity entity) {
        this.hostingEntity = entity;
        this.eventProperty = entity;
        this.eventLabel = new EventLabel(entity);
        this.eventLabel.setIcon(entity.getImage());
        this.title = this.eventLabel.getShortLabel();
    }

    
    private boolean isValidBirthDate(Indi indi) {
        PropertyDate birthDate = indi.getBirthDate();
        return birthDate != null && birthDate.isValid();
    }

    
    
    
    public void update(Indi indi) {
        
    }



    public void remove(Indi indi) {
        if (hostingEntity == null) {
            return;
        }
        hostingEntity.delProperty(eventProperty);  // FIXME : recursively
    }





}
