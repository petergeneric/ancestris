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
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySource;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;



/**
 *
 * @author frederic
 */
public class EventWrapper {

    private Entity hostingEntity = null;    // INDI or FAM the event belongs to
    public Property eventProperty = null;   // the event
    
    public EventLabel eventLabel = null;    // for table
    public int eventYear = 0;               // for table
    public String eventAge = "";            // for table and label
    
    public String title = "";  
    public boolean showDesc = false;
    public String description = "";         // Description (to be saved in gedcom)
    private boolean hasAttribute = false;   // attribute of event if of type PropertyChoiceValue
    public PropertyDate date = null;        // Date temp property (to be saved in gedcom)
    public PropertyPlace place = null;      // Place temp property (to be saved in gedcom)
    public String dayOfWeek = null;
    public String age = "";
    

    // Event Notes
    public Map<String, NoteWrapper> refNotes = null;            // References to notes from main editor
    public List<NoteWrapper> eventNoteSet = null;               // Notes to add/update (to be saved in gedcom)
    public List<NoteWrapper> eventNoteRemovedSet = null;        // Notes to remove (to be saved in gedcom)
    public int eventNoteIndex = 0;
    
    // Event Sources with Media and Text and Repo
    public Map<String, SourceWrapper> refSources = null;        // Reference to sources from main editor
    public List<SourceWrapper> eventSourceSet = null;           // Sources to add/update (to be saved in gedcom)
    public List<SourceWrapper> eventSourceRemovedSet = null;    // Sources to remove (to be saved in gedcom)
    public int eventSourceIndex = 0;
    
    
    public EventWrapper(Property property, Indi indi, Map<String, NoteWrapper> refNotes, Map<String, SourceWrapper> refSources) {
        if (property == null) {
            return;
        }
        
        this.refNotes = refNotes;
        this.refSources = refSources;
        
        this.eventProperty = property;
        this.hasAttribute =  this.eventProperty.getMetaProperty().getType() == PropertyChoiceValue.class;
        this.hostingEntity = property.getEntity();

        // Event description & icon
        this.eventLabel = new EventLabel(property);
        this.eventLabel.setIcon(property.getImage());
        
        // Title and description
        this.title = this.eventLabel.getShortLabel();
        Property type = property.getProperty("TYPE"); 
        this.description = hasAttribute ? property.getDisplayValue().trim() : (type != null ? type.getDisplayValue() : "");

        // Event date
        this.date = new PropertyDate();
        PropertyDate tmpDate = (PropertyDate) property.getProperty("DATE");
        if (tmpDate != null) {
            this.date.setValue(tmpDate.getValue());
        }
        try {
            if (tmpDate != null && tmpDate.getStart() != null) {
                this.dayOfWeek = tmpDate.getStart().getDayOfWeek(true);
            } else {
                this.dayOfWeek = "";
            }
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
            this.dayOfWeek = "";
        }

        // Event year
        if (tmpDate != null) {
            this.eventYear = tmpDate.getStart() == null ? 0 : tmpDate.getStart().getYear();
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

    public EventWrapper(Entity entity, Map<String, NoteWrapper> refNotes, Map<String, SourceWrapper> refSources) {
        this.refNotes = refNotes;
        this.refSources = refSources;
        
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

    
    
    
    private boolean isValidBirthDate(Indi indi) {
        PropertyDate birthDate = indi.getBirthDate();
        return birthDate != null && birthDate.isValid();
    }

    

    //
    // NOTES
    //
    
    private List<NoteWrapper> getEventNotes(Property event) {
        List<NoteWrapper> ret = new ArrayList<NoteWrapper>();
                
        // Look for notes attached to event
        Property[] noteProps = event.getProperties("NOTE");
        for (Property prop : noteProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                NoteWrapper note = null;
                if (prop instanceof PropertyNote) {
                    note = createUniqueNote((Note) ((PropertyNote) prop).getTargetEntity());
                } else {
                    note = new NoteWrapper(prop);
                }
                ret.add(note);
            }
        }
        return ret;
    }

    private NoteWrapper createUniqueNote(Note entity) {
        NoteWrapper note = refNotes.get(entity.getId());
        if (note == null) {
            note = new NoteWrapper(entity);
            refNotes.put(entity.getId(), note);
        }
        return note;
    }
    
    public boolean addNote(Note entity, String noteText) {
        NoteWrapper note = createUniqueNote(entity);
        note.setText(noteText);
        eventNoteSet.add(note);
        eventNoteIndex = eventNoteSet.size() - 1;
        return true;
    }
    
    public boolean addNote(String noteText) {
        NoteWrapper note = new NoteWrapper(noteText);
        eventNoteSet.add(note);
        eventNoteIndex = eventNoteSet.size() - 1;
        return true;
    }
    
    
    
    public boolean setNote(Note entity, String noteText, int index) {
        eventNoteSet.get(index).setTargetEntity(entity);
        eventNoteSet.get(index).setText(noteText);
        eventNoteIndex = index;
        return true;
    }
    
    public boolean setNote(String noteText, int index) {
        eventNoteSet.get(index).setText(noteText);
        eventNoteIndex = index;
        return true;
    }
    
    public boolean setNote(String noteText) {
        eventNoteSet.get(eventNoteIndex).setText(noteText);
        return true;
    }
    
            
    
    
    //
    // SOURCES
    //
    
    
    private List<SourceWrapper> getEventSources(Property event) {
        List<SourceWrapper> ret = new ArrayList<SourceWrapper>();
                
        // Look for sources attached to event (source_record as links to a source entity)
        for (PropertySource propSource : event.getProperties(PropertySource.class)) {
            SourceWrapper source = createUniqueSource((Source) propSource.getTargetEntity());
            source.setHostingProperty(propSource);
            ret.add(source);
        }
        // Look for sources attached to event (source_citation included underneath SOUR tag)
        Property[] sourceProps = event.getProperties("SOUR");
        for (Property prop : sourceProps) {
            if (prop != null && !(prop instanceof PropertySource)) {
                ret.add(new SourceWrapper(prop));
            }
        }
        
//        // Read only ! : Look for general sources directly attached to indi
//        // Look for sources attached to indi as links to source entities
//        for (PropertySource propSource : hostingEntity.getProperties(PropertySource.class)) {
//            if (propSource != null && propSource.getParent() == ((Property) hostingEntity)) {
//                SourceWrapper source = createUniqueSource((Source) propSource.getTargetEntity());
//                ret.add(source);
//            }
//        }
//        // Look for sources attached to indi (source_citation included underneath SOUR tag)
//        sourceProps = hostingEntity.getProperties("SOUR");
//        for (Property prop : sourceProps) {
//            if (prop != null && !(prop instanceof PropertySource)) {
//                ret.add(new SourceWrapper(prop));
//            }
//        }
        
        return ret;
    }

    
    
    public SourceWrapper getEventSource() {
        if ((eventSourceSet != null) && (!eventSourceSet.isEmpty()) && (eventSourceIndex >= 0) && (eventSourceIndex < eventSourceSet.size())) {
            return eventSourceSet.get(eventSourceIndex);
        }
        return null;
    }

    private SourceWrapper createUniqueSource(Source entity) {
        SourceWrapper source = refSources.get(entity.getId());
        if (source == null) {
            source = new SourceWrapper(entity);
            refSources.put(entity.getId(), source);
        }
        return source;
    }
    
    public boolean addSource(Source entity) {
        SourceWrapper source = createUniqueSource(entity);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }
    
    public boolean addSource(File file) {
        SourceWrapper source = new SourceWrapper(file);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }
    
    public boolean addSourceRepository(Repository repo) {
        SourceWrapper source = new SourceWrapper(repo);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }
    
    public boolean addSource(String title, String text) {
        SourceWrapper source = new SourceWrapper(title);
        source.setText(text);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }
    
    public boolean setSource(Source entity, int index) {
        eventSourceSet.get(index).setTargetEntity(entity);
        eventSourceIndex = index;
        return true;
    }
    
    public boolean setSource(File file, int index) {
        eventSourceSet.get(index).setFile(file);
        eventSourceIndex = index;
        return true;
    }
    
    public boolean setSourceRepository(Repository repo) {
        eventSourceSet.get(eventSourceIndex).setRepo(repo);
        return true;
    }
    
    public boolean setSource(String title, String text) {
        SourceWrapper source = eventSourceSet.get(eventSourceIndex);
        source.setTitle(title);
        source.setText(text);
        return true;
    }
    

    
    
    
    
    
    
    /**
     * Creates or Updates the events property
     *    - Creation : separate event entity
     *    - Update : where it is
     * @param indi 
     * String description = "";                             // Description (to be saved in gedcom)
     * PropertyDate date = null;                            // Date temp property (to be saved in gedcom)
     * PropertyPlace place = null;                          // Place temp property (to be saved in gedcom)
     * List<NoteWrapper> eventNoteSet = null;               // Notes to add/update (to be saved in gedcom)
     * List<NoteWrapper> eventNoteRemovedSet = null;        // Notes to remove (to be saved in gedcom)
     * List<SourceWrapper> eventSourceSet = null;           // Sources to add/update (to be saved in gedcom)
     * List<SourceWrapper> eventSourceRemovedSet = null;    // Sources to remove (to be saved in gedcom)
    */ 
    public void update(Indi indi) {
        // Description : depends on property.metaProperty
        // = property.getDisplayValue();                            // case of attributes: description is the value of the event
        // = property.getProperty("TYPE").getDisplayValue();        // case of events and RESI: description is the value of the TYPE
        description = description.trim();
        if (hasAttribute) {
            eventProperty.setValue(description);
        } else { 
            Property type = eventProperty.getProperty("TYPE");
            if (type == null) {
                eventProperty.addProperty("TYPE", description);
            } else {
                type.setValue(description);
            }
        }

        // Date
        PropertyDate tmpDate = (PropertyDate) eventProperty.getProperty("DATE");
        if (tmpDate == null) {
            eventProperty.addProperty("DATE", date.getValue());
        } else {
            tmpDate.setValue(date.getValue());
        }
        
        // Place
        PropertyPlace tmpPlace = (PropertyPlace) eventProperty.getProperty("PLAC");
        if (tmpPlace == null) {
            eventProperty.addProperty("PLAC", place.getValue());
        } else {
            tmpPlace.setValue(place.getValue());
        }
        
        // Notes
        for (NoteWrapper note : eventNoteSet) {
            note.update(eventProperty);
        }
        for (NoteWrapper note : eventNoteRemovedSet) {
            note.remove();
        }
        
        // Sources
        for (SourceWrapper source : eventSourceSet) {
            source.update(eventProperty);
        }
        for (SourceWrapper source : eventSourceRemovedSet) {
            source.remove();
        }
        
        
    }



    public void remove(Indi indi) {
        if (hostingEntity == null) {
            return;
        }
        hostingEntity.delProperty(eventProperty);  // FIXME : recursively
    }





}
