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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySource;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.io.InputSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;

/**
 * FL Principles for the simple editor for SOURCES.
 *
 * -1- Simple editor should make the grammar (5.5 or 5.5.1) transparent to the
 * user.
 *
 * -2- Simple editor should make the gedcom structure (link, sub-tags,
 * citations) transparent to the user. As a consequence, the editor should be
 * able to read a given information regardless of how it is attached in the
 * gedcom structure An another consequence, it will write the information in
 * only one way (which I choose to be in a record if there is more than one
 * possibility)
 *
 * -3- Simple editor cannot manage all of the gedcom possibilities for sources.
 * Sources use a lot of fields. There are too many combinations. Fields not
 * managed within the source editor will be managed by the gedcom editor
 *
 * -4- From all these principles, simple editor will define a unique and generic
 * backbone of information that is easy to manipulate for the user: - Basic and
 * most commonly used information from the editor main window - Possibility to
 * choose a source for an event in one click - Less frequently used information
 * from another simple window or in the gedcom editor
 *
 *
 * Sources can be attached to a number of element but the simple editor will not
 * use all of them : - INDI record : used * - INDI event detail : used * - FAM
 * event detail : used *
 *
 * - FAM record : not used 
 * - OBJE record : not used 
 * - NOTE record : not used
 * - ASSO structure : not used 
 * - LDS : not used 
 * - Personal name structure : not used (5.5 only)
 * - Personal Name Pieces : not used (5.5.1 only)
 * - Place structure : not used (5.5 only)
 *
 *
 */
/*

BACKBONE : GENERIC AND BASIC INFORMATION EDITOR WILL MANAGE AND WHERE IT WILL PUT IT BY DEFAULT IF CREATED

    - 1 TITL                            => read from record or from tag (cannot be both), created to source record
    Content of the source:
    - 1 TEXT                            => read from record only (RECORD if both), created to source record
    - n OBJE                            => read from record *and* from tag, created to most commonly used OBJE in gedcom file (dynamic detection), updated where they are
    Location to access the source:
    - 1 REPO                            => read from record, no choice

    Rest of SOUR sub-tags and SOUR record will be added/updated using Gedcom editor because more related to the source than to the individual.




SOUR tag

    - 1 TITL : description of source (in direct tag or in SOUR entity)

    SUB-TAGS
	Content of the source:
	- n TEXT : actual text of the source
	- n OBJE : media

	Location in the source
	- 1 PAGE : where within source (label: value [, label: value]

	Structured information codifying the content of the source
	- 1 EVEN : Type of event of the source document
		[ ADOP | BIRT | BAPM | BARM | BASM | BLES | BURI | CENS | CHR | CHRA | CONF | CREM | DEAT | EMIG | FCOM | GRAD | IMMI | NATU | ORDN | RETI | PROB | WILL | EVEN ]
		[ ANUL | CENS | DIV | DIVF | ENGA | MARR | MARB | MARC | MARL | MARS | EVEN ]
		[ CAST | EDUC | NATI | OCCU | PROP | RELI | RESI | TITL | FACT ]
	- 1 ROLE in the EVEN : INDI's relation compared to the main person of the source
		[ CHIL | HUSB | WIFE | MOTH | FATH | SPOU | ( <ROLE_DESCRIPTOR> ) ]
	- 1 QUAY
		[ 0 (unreliable) | 1 (questionable) | 2 (official source with questionable proof) | 3 (proof) ]

	Creation of the source:
	- 1 DATE of TEXT written

	User notes about the source	
	- n NOTE : user notes

SOUR entity
	- 1 TITL : title describing the source

	Content of the source:
	- 1 TEXT : actual text from the source
	- n OBJE : media

	Structured information codifying the content of the source
	- 1 DATA : structured description of data contained in the source
		- n EVEN : list of events the source document talks about
			- DATE
			- PLAC
		- n NOTE related to the events
		- 1 official agent for the source

	Location to access the source:
	- 1 ABBR : abreviation title
	- n REPO : location and contact to access the source. Several repo if source can be found in several places.

	Creation of the source:
	- 1 AUTH : text describing who the creator of the record was
	- 1 PUBL : publication place and date or creation of the source

	User notes about the source	
	- n NOTE : user notes

	User reference number
	- 1 REFN and TYPE

	Automatic unique identification nb of source record in the submitter system
	- 1 RIN


 */
/**
 * GEDCOM GRAMMAR. : 
 * 
 * From the gedcom norm : systems that describe sources using the AUTHor, TITLe, PUBLication, and REPOsitory fields can
 * and should always pass this information in GEDCOM using a SOURce record pointed to by the <<SOURCE_CITATION>>
 * Systems that only allow free form source notes should encourage forming the source information so that it include text about these categories:
 * ! TITL: A descriptive title of the source
 * ! AUTH: Who created the work
 * ! PUBL: When and where was it created
 * ! REPO: Where can it be obtained or viewed
 * When possible provide the tag for these categories within the text so that a receiving system could
 * parse them to fit the recommended source/citation structure
 * 

 * 
 * Source records are : 
 * 
 * SOURCE_RECORD: = (5.5)
 *   
 *     n  @<XREF:SOUR>@ SOUR  {1:1}
 *       +1 DATA        {0:1}
 *         +2 EVEN <EVENTS_RECORDED>  {0:M}
 *           +3 DATE <DATE_PERIOD>  {0:1}
 *           +3 PLAC <SOURCE_JURISDICTION_PLACE>  {0:1}
 *         +2 AGNC <RESPONSIBLE_AGENCY>  {0:1}
 *         +2 <<NOTE_STRUCTURE>>  {0:M}
 *       +1 AUTH <SOURCE_ORIGINATOR>  {0:1}
 *         +2 [CONT|CONC] <SOURCE_ORIGINATOR>  {0:M}
 *       +1 TITL <SOURCE_DESCRIPTIVE_TITLE>  {0:1}                  ==> 1 Title
 *         +2 [CONT|CONC] <SOURCE_DESCRIPTIVE_TITLE>  {0:M}
 *       +1 ABBR <SOURCE_FILED_BY_ENTRY>  {0:1}
 *       +1 PUBL <SOURCE_PUBLICATION_FACTS>  {0:1}
 *         +2 [CONT|CONC] <SOURCE_PUBLICATION_FACTS>  {0:M}
 *       +1 TEXT <TEXT_FROM_SOURCE>  {0:1}                          ==> 1 Text
 *         +2 [CONT|CONC] <TEXT_FROM_SOURCE>  {0:M}
 *       +1 <<SOURCE_REPOSITORY_CITATION>>  {0:1}                   ==> 1 Repo
 *       +1 <<MULTIMEDIA_LINK>>  {0:M}                              ==> n Media 
 *       +1 <<NOTE_STRUCTURE>>  {0:M}
 *       +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
 *         +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
 *       +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
 *      +1 <<CHANGE_DATE>>  {0:1}
 * 
 * 
 * 
 SOURCE_RECORD: = (5.5.1) = idem 5.5 (only use first repo)
 * 
 * n @<XREF:SOUR>@ SOUR {1:1}
 *   +1 DATA {0:1}
 *     +2 EVEN <EVENTS_RECORDED> {0:M}
 *       +3 DATE <DATE_PERIOD> {0:1}
 *       +3 PLAC <SOURCE_JURISDICTION_PLACE> {0:1}
 *     +2 AGNC <RESPONSIBLE_AGENCY> {0:1}
 *     +2 <<NOTE_STRUCTURE>> {0:M}
 *   +1 AUTH <SOURCE_ORIGINATOR> {0:1}
 *     +2 [CONC|CONT] <SOURCE_ORIGINATOR> {0:M}
 *   +1 TITL <SOURCE_DESCRIPTIVE_TITLE> {0:1}                  ==> 1 Title
 *     +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE> {0:M}
 *   +1 ABBR <SOURCE_FILED_BY_ENTRY> {0:1}
 *   +1 PUBL <SOURCE_PUBLICATION_FACTS> {0:1}
 *     +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS> {0:M}
 *   +1 TEXT <TEXT_FROM_SOURCE> {0:1}                          ==> 1 Text
 *     +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
 *   +1 <<SOURCE_REPOSITORY_CITATION>> {0:M}                   ==> 1 Repo
 *   +1 <<MULTIMEDIA_LINK>> {0:M}                              ==> n Media
 *   +1 <<NOTE_STRUCTURE>> {0:M}
 *   +1 REFN <USER_REFERENCE_NUMBER> {0:M}
 *     +2 TYPE <USER_REFERENCE_TYPE> {0:1}
 *   +1 RIN <AUTOMATED_RECORD_ID> {0:1}
 *   +1 <<CHANGE_DATE>> {0:1}
 * 
 * 
 * Sources pointers are :
 * 
 * SOURCE_CITATION: = (5.5)
 *   [  
 *   n SOUR @<XREF:SOUR>@  {1:1}    pointer to source record        ==> only use pointer to source record
 *     +1 PAGE <WHERE_WITHIN_SOURCE>  {0:1}
 *     +1 EVEN <EVENT_TYPE_CITED_FROM>  {0:1}
 *       +2 ROLE <ROLE_IN_EVENT>  {0:1}
 *     +1 DATA        {0:1}
 *       +2 DATE <ENTRY_RECORDING_DATE>  {0:1}
 *       +2 TEXT <TEXT_FROM_SOURCE>  {0:M}
 *         +3 [ CONC | CONT ] <TEXT_FROM_SOURCE>  {0:M}
 *     +1 QUAY <CERTAINTY_ASSESSMENT>  {0:1}
 *     +1 <<MULTIMEDIA_LINK>>  {0:M}                                ==> n Media
 *     +1 <<NOTE_STRUCTURE>>  {0:M}
 * 
 *   |              Systems not using source records                ==> Media (none)
 *   n SOUR <SOURCE_DESCRIPTION>  {1:1}                             ==> Title (update mode only)
 *     +1 [ CONC | CONT ] <SOURCE_DESCRIPTION>  {0:M}
 *     +1 TEXT <TEXT_FROM_SOURCE>  {0:M}                            ==> Text (update mode only)
 *        +2 [CONC | CONT ] <TEXT_FROM_SOURCE>  {0:M}
 *     +1 <<NOTE_STRUCTURE>>  {0:M}
 *   ]
 * 
 * SOURCE_CITATION: =  (5.5.1) (same as 5.5)
 *   [
 *     n SOUR @<XREF:SOUR>@ {1:1}    pointer to source record       ==> only use pointer to source record
 *     +1 PAGE <WHERE_WITHIN_SOURCE> {0:1}
 *     +1 EVEN <EVENT_TYPE_CITED_FROM> {0:1}
 *        +2 ROLE <ROLE_IN_EVENT> {0:1}
 *     +1 DATA {0:1}
 *        +2 DATE <ENTRY_RECORDING_DATE> {0:1}
 *        +2 TEXT <TEXT_FROM_SOURCE> {0:M}
 *           +3 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
 *     +1 QUAY <CERTAINTY_ASSESSMENT> {0:1}
 *     +1 <<MULTIMEDIA_LINK>> {0:M}
 *     +1 <<NOTE_STRUCTURE>> {0:M}
 * 
 *  |              Systems not using source records 
 *  n SOUR <SOURCE_DESCRIPTION> {1:1}                               ==> Title (update mode only)
 *     +1 [CONC|CONT] <SOURCE_DESCRIPTION> {0:M}
 *     +1 TEXT <TEXT_FROM_SOURCE> {0:M}                             ==> Text (update mode only)
 *        +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
 *     +1 QUAY <CERTAINTY_ASSESSMENT> {0:1}
 *     +1 <<MULTIMEDIA_LINK>> {0:M}                                 ==> n Media (update mode only) 
 *     +1 <<NOTE_STRUCTURE>> {0:M}
 *  ]
 * 
 * 
 */
/**
 *
 * @author frederic
 */
public class SourceWrapper {

    private static int ENTITY_MOUNT = 0;
    private static int CITATION_MOUNT = 1;
    private static Map<String, Integer> mounts;   // table : gedcom name, type of mount

    private Property hostingProperty = null;    // the property the source property belongs to

    private boolean recordType = true;          // source info : true if record type, false if citation type
    private Entity targetSource = null;         // the source entity
    private String title = "";                  // the source title
    private String text = "";                   // the source text
    private Repository targetRepo = null;       // the repository entity
    private String repoName = "";               // the name of the repository entity
    // List of media for that source, owned by source entity or source property. Recognition based on MediaWrapper hosting property
    public List<MediaWrapper> sourceMediaSet = null;
    public List<MediaWrapper> sourceMediaRemovedSet = null;
    public int sourceMediaIndex = 0;

    // Constructor for source as entity or as property
    public SourceWrapper(Property hostingProperty) {
        // Case of reading an indi with source entity
        if (hostingProperty instanceof PropertySource) {
            this.hostingProperty = hostingProperty;
            setSourceFromEntity((Source) ((PropertySource) hostingProperty).getTargetEntity());
            // Case of creating a new source    
        } else if (hostingProperty instanceof Source) {
            setSourceFromEntity((Source) hostingProperty);
            // Case of reading an indi with source citation
        } else {
            this.hostingProperty = hostingProperty;
            this.targetSource = null;
            resetMediaSet();
            setInfoFromCitation(hostingProperty);
            getMediaFromProperty(hostingProperty);
        }
    }

    public final void setSourceFromEntity(Source source) {
        this.targetSource = source;
        resetMediaSet();
        setInfoFromRecord(source);
        getMediaFromProperty(source);
        if (hostingProperty != null) {
            getMediaFromProperty(hostingProperty);
        }
    }

    // Constructor from changed title
    public SourceWrapper(String title) {
        setTitle(title);
    }

    // Constructor from choose Repository
    public SourceWrapper(Repository repo) {
        setRepo(repo);
    }

    // Constructor from choose Media
    public SourceWrapper(MediaWrapper media) {
        resetMediaSet();
        sourceMediaSet.add(media);
    }

    // Constructor from choose File
    public SourceWrapper(InputSource f) {
        setMediaFile(f, false);
    }

    public Property getHostingProperty() {
        return this.hostingProperty;
    }

    private void resetMediaSet() {
        // Media
        if (sourceMediaSet != null) {
            sourceMediaSet.clear();
            sourceMediaSet = null;
        }
        if (sourceMediaRemovedSet != null) {
            sourceMediaRemovedSet.clear();
            sourceMediaRemovedSet = null;
        }
        sourceMediaSet = new ArrayList<>();
        sourceMediaRemovedSet = new ArrayList<>();
        sourceMediaIndex = 0;
    }

    public final void getMediaFromProperty(Property property) {
        if (sourceMediaSet == null || property == null) {
            return;
        }

        // Look for media attached to property
        Property[] mediaProps = property.getProperties("OBJE");
        for (Property prop : mediaProps) {
            if (prop != null) {
                MediaWrapper media;
                if (prop instanceof PropertyMedia) {
                    media = new MediaWrapper((Media) ((PropertyMedia) prop).getTargetEntity());
                    media.setHostingProperty(prop);
                    sourceMediaSet.add(media);
                } else {
                    media = new MediaWrapper(prop);
                    sourceMediaSet.add(media);
                }

            }
        }
    }

    public boolean isRecord() {
        return recordType;
    }

    public Entity getTargetSource() {
        return targetSource;
    }

    public String getTitle() {
        return title;
    }

    public final void setTitle(String str) {
        this.title = str;
    }

    public String getText() {
        return text;
    }

    public final void setText(String text) {
        this.text = text;
    }

    public void setMedia(MediaWrapper media, boolean addMedia) {
        if (sourceMediaSet == null) {
            resetMediaSet();
        }
        if (sourceMediaSet.isEmpty() || addMedia) {
            sourceMediaSet.add(media);
        } else {
            sourceMediaSet.set(sourceMediaIndex, media);
        }
    }

    public boolean deleteMedia() {
        if (sourceMediaSet == null || sourceMediaSet.isEmpty() || sourceMediaRemovedSet == null) {
            return false;
        }
        sourceMediaRemovedSet.add(sourceMediaSet.get(sourceMediaIndex));
        sourceMediaSet.remove(sourceMediaIndex);
        sourceMediaIndex--;
        if (sourceMediaIndex < 0) {
            sourceMediaIndex = 0;
        }
        return true;
    }

    public String getMediaTitle() {
        if (sourceMediaSet == null || sourceMediaSet.isEmpty()) {
            return "";
        }
        return sourceMediaSet.get(sourceMediaIndex).getTitle();
    }

    public void setMediaTitle(String title) {
        if (sourceMediaSet == null) {
            if (title.isEmpty()) {
                return;
            }
            resetMediaSet();
        }
        if (sourceMediaSet.isEmpty()) {
            sourceMediaSet.add(new MediaWrapper(title));
        } else {
            sourceMediaSet.get(sourceMediaIndex).setTitle(title);
        }
    }

    public final void setMediaFile(InputSource f, boolean addMedia) {
        if (sourceMediaSet == null) {
            resetMediaSet();
        }
        if (sourceMediaSet.isEmpty() || addMedia) {
            sourceMediaSet.add(new MediaWrapper(f));
        } else {
            sourceMediaSet.get(sourceMediaIndex).setInputSource(f);
        }
    }

    public InputSource getMediaFile() {
        if (sourceMediaSet == null || sourceMediaSet.isEmpty()) {
            return null;
        }
        
        return sourceMediaSet.get(sourceMediaIndex).getInputSource();
    }

    public String getRepoName() {
        return this.repoName;
    }

    public void setRepoName(String name) {
        this.repoName = name;
    }

    public final void setRepo(Repository repo) {
        this.targetRepo = repo;
        if (repo == null) {
            this.repoName = "";
        } else {
            this.repoName = targetRepo.getRepositoryName();
        }
    }

    public Repository getRepo() {
        return targetRepo;
    }

    /**
     * Read source record (5.5 or 5.5.1) (reading is the same regardless of the
     * grammar)
     */
    private void setInfoFromRecord(Property property) {
        recordType = true;

        if (property == null) {
            return;
        }
        Property propTitle = property.getProperty("TITL", true);
        /* title */
        if (propTitle != null) {
            this.title = propTitle.getDisplayValue();
        } else {
            this.title = "";
        }
        Property propText = property.getProperty("TEXT", true);
        /* text */
        if (propText != null) {
            this.text = propText.getDisplayValue();
        } else {
            this.text = "";
        }
        Property propRepository = property.getProperty("REPO", true);
        /* repo */
        if (propRepository != null && propRepository instanceof PropertyRepository) {
            PropertyRepository pr = (PropertyRepository) propRepository;
            this.targetRepo = (Repository) pr.getTargetEntity();
            this.repoName = targetRepo.getRepositoryName();
        } else {
            this.targetRepo = null;
            this.repoName = "";
        }
    }

    /**
     * Read source citation (5.5 or 5.5.1) (no repository in this case)
     */
    private void setInfoFromCitation(Property property) {
        recordType = false;

        if (property == null) {
            return;
        }
        this.title = property.getDisplayValue().trim();
        /* title */
        Property propText = property.getProperty("TEXT");
        /* text */
        if (propText != null) {
            this.text = propText.getDisplayValue();
        }
    }

    /**
     * Creates or Updates the SOUR property - Creation : separate SOUR entity -
     * Update : where it is
     *
     * @param mainProp (indi or event basically)
     */
    public void update(Property mainProp) {
        // If it is a creation...
        if (hostingProperty == null) {
            try {
                if (this.targetSource == null) {
                    this.targetSource = mainProp.getGedcom().createEntity(Gedcom.SOUR);
                }
                mainProp.addSource((Source) targetSource);
                putSourceRecord((Source) targetSource);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }

        // ... or else a modification
        // Case of property source as Citation
        if (!recordType) {
            putSourceCitation(hostingProperty);
        } else { // Case of property source as linked record already existing
            try {
                if (this.targetSource == null) {
                    this.targetSource = mainProp.getGedcom().createEntity(Gedcom.SOUR);
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (recordType && (hostingProperty instanceof PropertySource)) {
                putSourceRecord(targetSource);
                // 2 situations : replacement of the text of the same source or replacement of the source by another one
                PropertySource ps = (PropertySource) hostingProperty;
                Entity tse = ps.getTargetEntity();
                if (targetSource.equals(tse)) { // it was just an update of the same media, quit
                } else {
                    Utils.replaceRef(ps, tse, targetSource);
                }
            } else // Case of property as source record not already linked (added and chosen from SourceChooser)
            if (recordType && !(hostingProperty instanceof PropertySource)) {
                mainProp.addSource((Source) targetSource);
                putSourceRecord(targetSource);
            }
        }

    }

    /**
     * Writes the source as a link to a Source Entity
     *
     * @param property
     */
    private void putSourceRecord(Property property) {
        if (property == null) {
            return;
        }
        
        putProperty(property, "TITL", title);
        putProperty(property, "TEXT", text);
        
        // Don't create empty value.
        if (title.trim().isEmpty()) {
            property.delProperties("TITL");
        }
        if (text.trim().isEmpty()) {
            property.delProperties("TEXT");
        }

        // Put media items 
        Property host = getDefaultHost(property);
        putMediaItems(host);

        // Put repo if exists, and create it if it does not exists
        if (repoName.trim().isEmpty()) {
            return;
        }

        if (targetRepo == null) {
            try {
                targetRepo = (Repository) property.getGedcom().createEntity(Gedcom.REPO);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        targetRepo.setRepositoryName(repoName);

        Property propRepository = property.getProperty("REPO", true);
        if (propRepository != null && propRepository instanceof PropertyRepository) {
            PropertyRepository pr = (PropertyRepository) propRepository;
            Repository tmpRepo = (Repository) pr.getTargetEntity();
            if (tmpRepo != targetRepo) { // it points to another media entity, replace media.
                property.delProperty(propRepository);
                if (!repoName.isEmpty()) {
                    property.addRepository(targetRepo);
                }
            }
        } else {
            property.addRepository(targetRepo);
        }

    }

    /**
     * Writes the source as a link to a Source Entity
     *
     * @param property
     */
    private void putSourceCitation(Property property) {
        Utils.setDistinctValue(property, title);
        putProperty(property, "TEXT", text);
        if (property.getMetaProperty().allows("OBJE")) {
            putMediaItems(property);
        }
        //Don't create empty values.
        if (title.trim().isEmpty()) {
            property.delProperties("TITL");
        }
        if (text.trim().isEmpty()) {
            property.delProperties("TEXT");
        }

    }

    public void remove() {
        if (hostingProperty == null) {
            return;
        }
        hostingProperty.getParent().delProperty(hostingProperty);
    }

    /**
     * Update or Create tag property with value to provided property
     *
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

    /**
     * Aligns (creates and updates) all media in sourceMediaSet with OBJE
     * properties or records underneath property
     *
     * @param property
     */
    private void putMediaItems(Property property) {
        if (sourceMediaSet == null || sourceMediaSet.isEmpty()) {
            if (sourceMediaRemovedSet == null || sourceMediaRemovedSet.isEmpty()) {
                return;
            }
        }
        int index = 0;
        for (MediaWrapper media : sourceMediaSet) {
            media.update(index, property);
            index++;
        }
        sourceMediaRemovedSet.forEach((media) -> {
            media.remove();
        });
    }

    /**
     * OBJE can be mounted in two different ways. In case of creation, where do
     * I choose ? If most OBJE for sources are mounted one way, it beccomes the
     * default. I would rather not ask the usere because it should be
     * transparent to the user. Returns hostingProperty if OBJE underneath "SOUR
     *
     * @xxx@" (PropertySource) rather than underneath @SOUR@ (Entity Source) If
     * OBJE under SOUR are more than half under one type, this is the chosen
     * type
     *
     * @param property
     * @return
     */
    private Property getDefaultHost(Property property) {
        if (mounts == null) {
            mounts = new HashMap<>();
        }
        String gedcomName = property.getGedcom().getOrigin().getFile().getAbsolutePath();
        Integer type = mounts.get(gedcomName);
        if (type == null) {
            type = getMountType(property.getGedcom());
            mounts.put(gedcomName, type);
        }
        return type == ENTITY_MOUNT ? property : hostingProperty;
    }

    /**
     * Algorythm to get where OBJE for sources are mounted in general in the
     * gedcom
     *
     * @return
     */
    private Integer getMountType(Gedcom gedcom) {

        // Get all media throughout the whole gedcom, excluding those underneath SOUR only
        double count = 0;
        double total = 0;
        String[] ENTITIES = {Gedcom.INDI, Gedcom.FAM};
        for (String type : ENTITIES) {
            Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(type);
            for (Entity entity : entities) {
                List<PropertySource> properties = entity.getProperties(PropertySource.class);
                total += properties.size();
                for (PropertySource prop : properties) {
                    count += prop.getProperties("OBJE", false).length;
                }
            }
        }
        if (total == 0 || count == 0) {
            return ENTITY_MOUNT;
        }

        double ratio = count / total;
        if (ratio > 0.5) {
            return CITATION_MOUNT;
        }

        return ENTITY_MOUNT;
    }
}
