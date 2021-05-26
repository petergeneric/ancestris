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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.io.InputSource;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class EventWrapper {

    private static Set<String> ATTR_TAGS = new HashSet<String>(Arrays.asList("CAST", "DSCR", "EDUC", "IDNO", "NATI", "NCHI", "NMR", "OCCU", "PROP", "RELI", "SSN", "TITL"));
    public static String AGE_FORMAT = "#.###";   // Format of age displayed 

    public boolean isGeneral = true;        // true for the general event
    private Entity hostingEntity = null;    // INDI or FAM the event belongs to
    public Property eventProperty = null;   // the event

    private double ageAsDouble;             // calculated age

    public EventLabel eventLabel = null;    // for table
    public String eventYear = "-";          // for table
    public String eventAge = "-";           // for table (calculated)

    public boolean showDesc = false;
    public String title = "";               // Description (to be saved in gedcom) for certain events
    public String description = "";         // Description (to be saved in gedcom) 
    private boolean hasAttribute = false;   // attribute of event if of type PropertyChoiceValue
    private Property dummyProperty = null;  // Temporary gedcom to attach date and place property
    public PropertyDate date = null;        // Date temp property (to be saved in gedcom)
    public String time = "";                // Time of event (optional)
    public PropertyPlace place = null;      // Place temp property (to be saved in gedcom)
    public String dayOfWeek = "";           // Displayed day of week (calculated)
    public String age = "";                 // Displayed age as a string in full (calculated)

    // Event Media
    public List<MediaWrapper> eventMediaSet = null;
    public List<MediaWrapper> eventMediaRemovedSet = null;
    public int eventMediaIndex = 0;

    // Event Notes
    public List<NoteWrapper> eventNoteSet = null;               // Notes to add/update (to be saved in gedcom)
    public List<NoteWrapper> eventNoteRemovedSet = null;        // Notes to remove (to be saved in gedcom)
    public int eventNoteIndex = 0;

    // Event Sources with Media and Text and Repo
    public List<SourceWrapper> eventSourceSet = null;           // Sources to add/update (to be saved in gedcom)
    public List<SourceWrapper> eventSourceRemovedSet = null;    // Sources to remove (to be saved in gedcom)
    public int eventSourceIndex = 0;

    public EventWrapper(Property property, Indi indi, Fam fam) {

        this.eventProperty = property;
        this.hostingEntity = fam == null ? property.getEntity() : fam;

        // Create dummy indi (will be used for tmpDate et tmpPlace)
        createDummyProperty(indi.getGedcom());

        // Event description & icon
        this.eventLabel = new EventLabel(property);
        this.eventLabel.setIcon(property.getImage());

        // No attributes appart from notes and sources for the general event
        if (!property.equals(indi)) {

            isGeneral = false;

            // Description
            this.description = getDescription();

            // Event date
            this.date = (PropertyDate) dummyProperty.addProperty("DATE", "");
            PropertyDate tmpDate = (PropertyDate) eventProperty.getProperty("DATE");
            if (tmpDate != null) {
                this.date.setValue(tmpDate.getValue());
            }

            // Day of week
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
            this.eventYear = "-";
            if (tmpDate != null && !tmpDate.getValue().isEmpty() && tmpDate.isValid() && tmpDate.getStart() != null) {
                this.eventYear = "" + tmpDate.getStart().getYear();
            }

            // Age (for table (eventAge) and description (age) and value (ageAsDouble)
            calcAge(indi, property);

            // Display _TIME
            this.time = getTimeOfEvent();

            // Place of event
            this.place = (PropertyPlace) dummyProperty.addProperty("PLAC", "");
            PropertyPlace tmpPlace = (PropertyPlace) property.getProperty("PLAC");
            if (tmpPlace != null) {
                this.place.setValue(tmpPlace.getValue());
                setCoordinates(tmpPlace, this.place);
            }
        }

        // Title
        this.title = isGeneral ? this.eventLabel.getLongLabel() : this.eventLabel.getShortLabel();

        // Media
        if (eventMediaSet != null) {
            eventMediaSet.clear();
            eventMediaSet = null;
        }
        if (eventMediaRemovedSet != null) {
            eventMediaRemovedSet.clear();
            eventMediaRemovedSet = null;
        }
        eventMediaSet = getEventMedia(eventProperty);
        eventMediaRemovedSet = new ArrayList<>();
        eventMediaIndex = 0;

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
        eventNoteRemovedSet = new ArrayList<>();
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
        eventSourceRemovedSet = new ArrayList<>();
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

    public void setTime(String text) {
        time = text;
    }

    public void setPlace(String text) {
        place.setValue(text);
        place.setCoordinates();
    }

    public void setDate(DateBean dateBean) {
        dateBean.setValueToProperty(date);
    }

    /**
     * Calculate age from birth date and date of event, and produce value, age
     * in table and age description
     *
     * @param indi
     * @param property
     */
    public void calcAge(Indi indi, Property property) {

        ageAsDouble = 0;
        age = ""; // do not display anything if age undetermined // NbBundle.getMessage(getClass(), "Undetermined_Age");
        eventAge = "-";
        
        // Get stored age if available
        Property pFoundAge = null;
        if (property.getEntity() instanceof Fam) {
            // get property from spouse indi
            Fam fam = (Fam) property.getEntity();
            String spouseTag = indi.equals(fam.getHusband()) ? "HUSB" : "WIFE";
            Property spouse = property.getProperty(spouseTag, false);
            if (spouse != null) {
                pFoundAge = spouse.getProperty("AGE");
            }
        } else {
            pFoundAge = property.getProperty("AGE");
        }
        String foundAge = null;
        String foundAgeLabel = NbBundle.getMessage(getClass(), "Found_Age");
        if (pFoundAge != null) {
            PropertyAge pAge = (PropertyAge) pFoundAge;
            foundAge = pAge.getDisplayValue();
            age = foundAge;
            eventAge = calcEventAge(pAge.getAge(), false);
        }

        // Add calculated age from birth date
        PointInTime start = indi.getStartPITOfAge();
        PointInTime end = (date != null ? date.getStart() : null);
        Delta delta = Delta.get(start, end);

        // skip date null, negative or null ages
        if (date == null || delta == null || delta.isZero()) {
            age = age.isEmpty() ? "" : "(" + PropertyAge.getLabelForAge().toLowerCase() + " " + foundAgeLabel + ": " + age + ")";
            return;
        }
        boolean direction = start.compareTo(end) > 0;
        eventAge = calcEventAge(delta, direction);
        String calculatedAge = (direction ? "-" : "") + delta.toString();

        
        // age display:
        // if entered and calculated ages are the same, only display calculated
        if (calculatedAge.equals(age) || age.isEmpty()) {
            age = "(" + PropertyAge.getLabelForAge().toLowerCase() + ": " + calculatedAge + ")";
        } else {
            age = "(" + PropertyAge.getLabelForAge().toLowerCase() + ": " + calculatedAge + "; " + foundAgeLabel + ": " + age + ")";
        }
        
    }

    private String calcEventAge(Delta delta, boolean direction) {
        // Double
        double d = delta.getYears();
        d += ((double) delta.getMonths()) / 12;
        d += ((double) delta.getDays()) / 365;
        if (direction) {
            d *= -1;
        }
        ageAsDouble = d;

        // eventAge
        DecimalFormat df = new DecimalFormat(AGE_FORMAT);
        df.setRoundingMode(RoundingMode.FLOOR);
        return df.format(d);
    }
    
    public boolean isAgeNegative() {
        return ageAsDouble < 0;
    }

    public Fam getFamilyEntity() {
        return (hostingEntity != null && hostingEntity instanceof Fam) ? (Fam) hostingEntity : null;
    }

    //
    // MEDIA
    //
    private List<MediaWrapper> getEventMedia(Property event) {
        List<MediaWrapper> ret = new ArrayList<>();
        if (event == null) {
            return ret;
        }

        // Look for media attached to event
        Property[] mediaProps = event.getProperties("OBJE");
        for (Property prop : mediaProps) {
            if (prop != null) {
                MediaWrapper media;
                if (prop instanceof PropertyMedia) {
                    media = new MediaWrapper((Media) ((PropertyMedia) prop).getTargetEntity());
                    media.setHostingProperty(prop);
                    ret.add(media);
                } else {
                    media = new MediaWrapper(prop);
                    ret.add(media);
                }

            }
        }

        return ret;
    }

    public boolean addMedia(String mediaTitle) {
        MediaWrapper media = new MediaWrapper(mediaTitle);
        eventMediaSet.add(media);
        eventMediaIndex = eventMediaSet.size() - 1;
        return true;
    }

    public boolean setMedia(String mediaTitle) {
        eventMediaSet.get(eventMediaIndex).setTitle(mediaTitle);
        return true;
    }

    //
    // NOTES
    //
    private List<NoteWrapper> getEventNotes(Property event) {
        List<NoteWrapper> ret = new ArrayList<>();
        if (event == null) {
            return ret;
        }

        // Look for notes attached to event
        Property[] noteProps = event.getProperties("NOTE");
        for (Property prop : noteProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                NoteWrapper note;
                if (prop instanceof PropertyNote) {
                    note = new NoteWrapper((Note) ((PropertyNote) prop).getTargetEntity());
                    if (note != null) {
                        note.setHostingProperty(prop);
                        ret.add(note);
                    }
                } else {
                    note = new NoteWrapper(prop);
                    ret.add(note);
                }

            }
        }
        return ret;
    }

    public boolean addNote(Note entity, String noteText) {
        NoteWrapper note = new NoteWrapper(entity);
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
        List<SourceWrapper> ret = new ArrayList<>();
        if (event == null) {
            return ret;
        }

        // Look for sources attached to event
        Property[] sourceProps = event.getProperties("SOUR");
        for (Property prop : sourceProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                SourceWrapper source = new SourceWrapper(prop);
                ret.add(source);
            }
        }

        return ret;
    }

    // Add source at end of index for a new source designed by entity (called from sourceChooser)
    public boolean addSource(Source entity) {
        SourceWrapper source = new SourceWrapper(entity);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }

    // Add source at end of index for a new source directly typed in title of text areas
    public boolean addSource(String title, String text, String mediaTitle) {
        SourceWrapper source = new SourceWrapper(title);
        source.setText(text);
        source.setMediaTitle(mediaTitle);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }

    public boolean addSourceMedia(MediaWrapper media) {
        SourceWrapper source = new SourceWrapper(media);
        eventSourceSet.add(source);
        eventSourceIndex = eventSourceSet.size() - 1;
        return true;
    }

    public boolean addSourceFile(InputSource file) {
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

    // Change current source at index for a new source designed by entity (called from sourceChooser)
    public boolean setSource(Source entity, int index) {
        eventSourceSet.get(index).setSourceFromEntity(entity);
        eventSourceIndex = index;
        return true;
    }

    // Change current title or text from directly entered title or text
    public boolean setSource(String title, String text, String mediaTitle) {
        SourceWrapper source = eventSourceSet.get(eventSourceIndex);
        source.setTitle(title);
        source.setText(text);
        if (mediaTitle != null && !mediaTitle.isEmpty()) {
            source.setMediaTitle(mediaTitle);
        }
        return true;
    }

    public boolean setSourceMedia(MediaWrapper media, boolean addMedia) {
        eventSourceSet.get(eventSourceIndex).setMedia(media, addMedia);
        return true;
    }

    public boolean setSourceFile(InputSource file, boolean addMedia) {
        eventSourceSet.get(eventSourceIndex).setMediaFile(file, addMedia);
        return true;
    }

    // Change current repository from directly changed repository
    public boolean setSourceRepository(Repository repo) {
        eventSourceSet.get(eventSourceIndex).setRepo(repo);
        return true;
    }

    public SourceWrapper getEventSource() {
        if ((eventSourceSet != null) && (!eventSourceSet.isEmpty()) && (eventSourceIndex >= 0) && (eventSourceIndex < eventSourceSet.size())) {
            return eventSourceSet.get(eventSourceIndex);
        }
        return null;
    }

    /**
     * Creates or Updates the events property - Creation : separate event entity
     * - Update : where it is
     *
     * @param indi String description = "";                 // Description (to be saved in gedcom) 
     * PropertyDate date = null;                            // Date temp property (to be saved in gedcom) 
     * PropertyPlace place = null;                          // Place temp property (to be saved in gedcom) 
     * List<NoteWrapper> eventNoteSet = null;               // Notes to add/update (to be saved in gedcom) 
     * List<NoteWrapper> eventNoteRemovedSet = null;        // Notes to remove (to be saved in gedcom) 
     * List<SourceWrapper> eventSourceSet = null;           // Sources to add/update (to be saved in gedcom)
     * List<SourceWrapper> eventSourceRemovedSet = null;    // Sources to remove (to be saved in gedcom)
     */
    public void update(Indi indi) {

        // if new property (to be created), do it first
        if (eventProperty.getGedcom() == null || eventProperty.getGedcom().getOrigin() == null) {
            if (hostingEntity instanceof Indi) {
                eventProperty = indi.addProperty(eventProperty.getTag(), "");
            } else if (hostingEntity instanceof Fam) {
                Fam fam = (Fam) hostingEntity;
                if (fam.getGedcom() == null || fam.getGedcom().getOrigin() == null) { // case of tmpFam (else, fam existed already)
                    fam = createFamForIndi(indi);
                    if (fam == null) {
                        return;
                    }
                }
                eventProperty = fam.addProperty(eventProperty.getTag(), "");
            }
        }

        // Update event property
        if (!isGeneral) {
            // Description : depends on property.metaProperty
            // = property.getDisplayValue();                            // case of attributes: description is the value of the event
            // = property.getProperty("TYPE").getDisplayValue();        // case of events and RESI: description is the value of the TYPE
            description = description.trim();
            if (hasAttribute) {
                Utils.setDistinctValue(eventProperty, description);
            } else {
                Property type = eventProperty.getProperty("TYPE");
                if (type == null) {
                    if (!description.isEmpty()) {
                        eventProperty.addProperty("TYPE", description);
                    }
                } else {
                    Utils.setDistinctValue(type, description);
                }
            }

            // Date (set date, and if empty, remove it)
            boolean nodate = false;
            PropertyDate tmpDate = (PropertyDate) eventProperty.getProperty("DATE", false);
            if (tmpDate == null) { // there is no date tag in the gedcom for that event...
                String val = date.getValue().trim();
                if (!val.isEmpty()) { // if new one not empty, add it
                    Property pDate = eventProperty.addProperty("DATE", ""); // Do no put value here even if we could but instead use setValue ...
                    pDate.setValue(date.getValue());  // ... to generate a propagatePropertyChanged and update ages if necessary (rather than also bypassing propagatePropertyAdded)
                } else { // if empty, nothing
                    nodate = true;
                }
            } else { // there is a date in the gedcom for that event...
                String val = date.getValue().trim();
                if (!val.isEmpty()) { // if new one not empty, replace only if different
                    Utils.setDistinctValue(tmpDate, date.getValue());
                } else { // if empty, remove it // if event is not birth   
                    //if (!eventProperty.getTag().equals("BIRT")) {     // 2020-06-09 FL : remove it always
                        eventProperty.delProperty(tmpDate);
                        nodate = true;
                    //}
                }

            }

            // Time (set time, and if empty, remove it)
            Property tmpTime = eventProperty.getProperty("_TIME", false);
            if (tmpTime == null) { // there is no time tag in the gedcom for that event...
                String val = time.trim();
                if (!val.isEmpty()) { // if new one not empty, add it
                    eventProperty.addProperty("_TIME", val);
                } else { // if empty, nothing
                }
            } else { // there is a time in the gedcom for that event...
                String val = time.trim();
                if (!val.isEmpty()) { // if new one not empty, replace only if different
                    Utils.setDistinctValue(tmpTime, val);
                } else { // if empty, remove it if event is not birth
                    eventProperty.delProperty(tmpTime);
                }
            }

            // Place (set place, and if empty, remove it)
            boolean noplace = false;
            PropertyPlace tmpPlace = (PropertyPlace) eventProperty.getProperty("PLAC", false);
            if (tmpPlace == null) { // there is no place tag in the gedcom for that event... 
                String val = place.getValue().trim();
                if (!val.isEmpty()) { // if new one not empty, add it
                    tmpPlace = (PropertyPlace) eventProperty.addProperty("PLAC", place.getValue());
                } else { // if empty, nothing
                    noplace = true;
                }
                setCoordinates(place, tmpPlace);
            } else { // there is a place in the gedcom for that event...
                String val = place.getValue().trim();
                if (!val.isEmpty()) { // if new one not empty, add it
                    Utils.setDistinctValue(tmpPlace, place.getValue());
                    setCoordinates(place, tmpPlace);
                } else { // if empty, remove it // if event is not birth
                    // if (!eventProperty.getTag().equals("BIRT")) {           // 2020-06-09 FL : remove it always
                        eventProperty.delProperty(tmpPlace);
                        noplace = true;
                    // }
                }
            }

            // Set Y flag if neigher date nor any place in case tags matches, remove it otherwise
            // Note : rule is different between 5.5 and 5.5.1 (only BIRT, CHR, DEAT, MARR should have a Y tag in 5.5.1) but it would generate inconsistencies for users otherwise).
            if (eventProperty.getTag().matches(
                    "(BIRT|CHR|DEAT|BURI|CREM|ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|CENS|PROB|WILL|GRAD|RETI|ANUL|DIV|DIVF|ENGA|MARR|MARB|MARC|MARL|MARS)")) {
                Utils.setDistinctValue(eventProperty, (nodate && noplace) ? "Y" : "");
            }
        }

        // Media
        int index = 0;
        for (MediaWrapper media : eventMediaSet) {
            media.update(index, eventProperty);
            index++;
        }
        for (MediaWrapper media : eventMediaRemovedSet) {
            media.remove();
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
        hostingEntity.delProperty(eventProperty);

    }

    private Fam createFamForIndi(Indi indi) {
        Fam fam = null;
        Gedcom gedcom = indi.getGedcom();
        try {
            fam = (Fam) gedcom.createEntity(Gedcom.FAM);

            int sex = indi.getSex();
            if (sex == PropertySex.UNKNOWN) {
                sex = PropertySex.MALE;
            }
            if (sex == PropertySex.MALE) {
                fam.setHusband(indi);
            } else {
                fam.setWife(indi);
            }

        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fam;
    }

    public String getEventKey() {
        return getEventKey(false);
    }

    /**
     * Get key of event
     *
     * @param force : if true, reload key from gedcom file for date and
     * description
     * @return
     */
    public String getEventKey(boolean force) {
        String ret = "";

        if (eventProperty == null) {
            return "-1";
        } else {
            ret += eventProperty.getTag() + "|";
        }

        if (force) {
            PropertyDate tmpDate = (PropertyDate) eventProperty.getProperty("DATE");
            ret += (tmpDate == null || tmpDate.getValue().isEmpty()) ? "" : tmpDate.getValue();
            ret += getDescription();
        } else {
            ret += (date == null || date.getValue().isEmpty()) ? "" : date.getValue();
            ret += description;
        }
        return ret;
    }

    public String getTag() {
        return hasAttribute ? eventProperty.getTag() : "TYPE";
    }

    private String getDescription() {

        String tag = this.eventProperty.getTag();
        this.hasAttribute = ATTR_TAGS.contains(tag);
        Property type = eventProperty.getProperty("TYPE");

        return hasAttribute ? eventProperty.getDisplayValue().trim() : (type != null ? type.getDisplayValue() : "");
    }

    /**
     * Exceptionnaly, we use a _TIME tag
     * We do not put it as an attribute of the DATE tag because it would impact the validity of a DATE tag.
     * (an empty Date with a _TIME subtag would become valid).
     * The choice is to add a user-defined tag without altering the DATE validity logic
     * @return 
     */
    private String getTimeOfEvent() {

        Property localTime = eventProperty.getProperty("_TIME");
        return localTime != null ? localTime.getDisplayValue() : "";
    }

    private void createDummyProperty(Gedcom gedcom) {
        Gedcom tmpGedcom = new Gedcom();
        tmpGedcom.setGrammar(gedcom.getGrammar());
        try {
            Indi indi = (Indi) tmpGedcom.createEntity(Gedcom.INDI);
            dummyProperty = indi.addProperty("BIRT", "");
        } catch (GedcomException ex) {
            //Exceptions.printStackTrace(ex);
        }

    }

    // Only overwrite coordinates if necessary
    private void setCoordinates(PropertyPlace fromPlace, PropertyPlace toPlace) {
        PropertyLatitude pLatitude = fromPlace.getLatitude(true);
        PropertyLongitude pLongitude = fromPlace.getLongitude(true);
        if (pLatitude != null && pLongitude != null) {
            String strLat = pLatitude.getValue();
            String strLong = pLongitude.getValue();
            if (toPlace.getLatitude(false) == null || toPlace.getLongitude(false) == null) {
                if (!strLat.isEmpty() || !strLong.isEmpty()) {
                    toPlace.setCoordinates(strLat, strLong);
                }
                return;
            }
            if (!toPlace.getLatitude(false).getValue().equals(strLat) || !toPlace.getLongitude(false).getValue().equals(strLong)) {
                toPlace.setCoordinates(strLat, strLong);
            }
        } else if (toPlace != null) {
            toPlace.setCoordinates();
        }
    }

}
