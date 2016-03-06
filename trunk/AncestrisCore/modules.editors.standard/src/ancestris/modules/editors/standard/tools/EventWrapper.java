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

import genj.edit.beans.DateBean;
import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import java.util.ArrayList;
import java.util.List;
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
    
    // Event Notes
    public List<NoteWrapper> eventNoteSet = null;
    public int eventNoteIndex = 0;
    public List<NoteWrapper> eventNoteRemovedSet = null;
    
    // Event Sources with Media and Text and Repo
    public List<SourceWrapper> eventSourceSet = null;
    public int eventSourceIndex = 0;
    public List<SourceWrapper> eventSourceRemovedSet = null;
    
    
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
        this.description = (desc != null && !desc.isEmpty() ? desc : "") + (type != null ? type.getDisplayValue() : "");   // we cannot have both desc and type filled in at the same time

        // Event date
        this.date = new PropertyDate();
        PropertyDate tmpDate = (PropertyDate) property.getProperty("DATE");
        if (tmpDate != null) {
            this.date.setValue(tmpDate.getValue());
        }
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
        this.place = new PropertyPlace("PLAC");
        PropertyPlace tmpPlace = (PropertyPlace) property.getProperty("PLAC");
        if (tmpPlace != null) {
            this.place.setValue(tmpPlace.getValue());

        }
        
        // Notes
            if (eventNoteSet != null) {
            eventNoteSet.clear();
            eventNoteSet = null;
        }
        if (eventNoteRemovedSet != null) {
            eventNoteRemovedSet.clear();
            eventNoteRemovedSet = null;
        }
        eventNoteSet = getEventNotes(eventProperty);
        eventNoteRemovedSet = new ArrayList<NoteWrapper>();
        eventNoteIndex = 0;
        
        // Sources - Media & Text & Repo
        if (eventSourceSet != null) {
            eventSourceSet.clear();
            eventSourceSet = null;
        }
        if (eventSourceRemovedSet != null) {
            eventSourceRemovedSet.clear();
            eventSourceRemovedSet = null;
        }
        eventSourceSet = getEventSources(eventProperty);
        eventSourceRemovedSet = new ArrayList<SourceWrapper>();
        eventSourceIndex = 0;
    }

    public EventWrapper(Entity entity) {
        this.hostingEntity = entity;
        this.eventProperty = entity;
        this.eventLabel = new EventLabel(entity);
        this.eventLabel.setIcon(entity.getImage());
        this.title = this.eventLabel.getShortLabel();
    }


    public void setDescription(String text) {
        description = text;
    }

    public void setPlace(String text) {
        place.setValue(text);
    }

    public void setDate(DateBean dateBean) {
        dateBean.setValueToProperty(date);
    }

    
    public SourceWrapper getEventSource() {
        if ((eventSourceSet != null) && (!eventSourceSet.isEmpty()) && (eventSourceIndex >= 0) && (eventSourceIndex < eventSourceSet.size())) {
            return eventSourceSet.get(eventSourceIndex);
        }
        return null;
    }

    
    
    
    
    private boolean isValidBirthDate(Indi indi) {
        PropertyDate birthDate = indi.getBirthDate();
        return birthDate != null && birthDate.isValid();
    }

    
    private List<NoteWrapper> getEventNotes(Property event) {
        List<NoteWrapper> ret = new ArrayList<NoteWrapper>();
                
        // Look for notes attached to event
        Property[] noteProps = event.getProperties("NOTE");
        for (Property prop : noteProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                ret.add(new NoteWrapper(prop));
            }
        }
        return ret;
    }

    
    private List<SourceWrapper> getEventSources(Property event) {
        List<SourceWrapper> ret = new ArrayList<SourceWrapper>();
                
        // Look for sources attached to event (source_citation as links to a source entity)
        for (PropertySource propSource : event.getProperties(PropertySource.class)) {
            ret.add(new SourceWrapper(propSource));
        }
        // Look for sources attached to event (source_citation included underneath SOUR tag)
        Property[] sourceProps = event.getProperties("SOUR");
        for (Property prop : sourceProps) {
            if (prop != null && !(prop instanceof PropertySource)) {
                ret.add(new SourceWrapper(prop));
            }
        }
        
        // Read only ! : Look for general sources directly attached to indi
        // Look for sources attached to indi as links to source entities
        for (PropertySource propSource : hostingEntity.getProperties(PropertySource.class)) {
            if (propSource != null && propSource.getParent() == ((Property) hostingEntity)) {
                ret.add(new SourceWrapper(propSource));
            }
        }
        // Look for sources attached to indi (source_citation included underneath SOUR tag)
        sourceProps = hostingEntity.getProperties("SOUR");
        for (Property prop : sourceProps) {
            if (prop != null && !(prop instanceof PropertySource)) {
                ret.add(new SourceWrapper(prop));
            }
        }
        
        return ret;
    }

    
    
    
    /**
     * Creates or Updates the events property
     *    - Creation : separate event entity
     *    - Update : where it is
     * @param indi 
     */
    public void update(Indi indi) {
        
    }



    public void remove(Indi indi) {
        if (hostingEntity == null) {
            return;
        }
        hostingEntity.delProperty(eventProperty);  // FIXME : recursively
    }





}
