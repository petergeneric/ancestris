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
import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import java.util.List;
import javax.swing.JLabel;
import org.openide.util.Exceptions;



/**
 *
 * @author frederic
 */
public class EventWrapper {

    private Entity hostingEntity = null;     // INDI or FAM the event belongs to
    private Property eventProperty = null;   // the event
    
    public JLabel eventLabel = null;            // for table
    public int eventYear = 0;                   // for table
    public String eventAge = "";                // for table and label
    
    public String title = "";  
    public PropertyDate date = null;
    public String dayOfWeek = null;
    public String age = "";
    public PropertyPlace place = null;
    private List<NoteWrapper> notes = null;
    private List<NoteWrapper> sources = null;
    
    
    public EventWrapper(Property property, Indi indi) {
        if (property == null || indi == null) {
            return;
        }
        this.hostingEntity = property.getEntity();
        this.eventProperty = property;

        // Event short description
        String str = property.getPropertyName();
        if (str.contains(" ")) {
            this.eventLabel = new JLabel(str.substring(0, str.indexOf(" "))); // only take first word
        } else {
            this.eventLabel = new JLabel(str);
        }
        this.eventLabel.setIcon(property.getImage());
        
        // Type
        Property type = property.getProperty("TYPE");
        this.title = this.eventLabel.getText() + " " + (type != null ? type.getDisplayValue() : "");              // FIXME : put in editable field, not title

        // Event date
        this.date = (PropertyDate) property.getProperty("DATE");
        try {
            this.dayOfWeek = date.getStart().getDayOfWeek(true);
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
            this.dayOfWeek = "";
        }

        // Event year
        if (this.date != null) {
            this.eventYear = date.getStart() == null ? 0 : date.getStart().getYear();
        }
        
        // Age of related indi at time of event
        Property prop = property.getProperty("AGE");
        if (prop != null && prop instanceof PropertyAge) {
            PropertyAge propAge = (PropertyAge) prop;
            propAge.updateAge();
            this.eventAge = propAge.getDecimalValue("#.###");
            this.age = propAge.getDisplayValue();
        } else {
            PropertyAge propAge = new PropertyAge("AGE");
            propAge.getAge(indi, eventProperty);
            this.eventAge = propAge.getDecimalValue("#.###");
            this.age = propAge.getDisplayValue();
        }
        
        // Place of event
        this.place = (PropertyPlace) property.getProperty("PLAC");
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
