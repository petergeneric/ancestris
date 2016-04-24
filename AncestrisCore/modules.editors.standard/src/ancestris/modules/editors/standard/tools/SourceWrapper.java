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
import genj.gedcom.Grammar;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySource;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import java.io.File;
import org.openide.util.Exceptions;



/**
 * FL Principles for the simple editor for SOURCES. 
 * 
 * -1- Simple editor should make the grammar (5.5 or 5.5.1) transparent to the user.
 * 
 * -2- Simple editor should make the gedcom structure (link, sub-tags, citations) transparent to the user.
 *      As a consequence, the editor should be able to read a given information regardless of how it is attached in the gedcom structure
 *      An another consequence, it will write the information in only one way (which I choose to be in a record if there is more than one possibility) 
 * 
 * -3- Simple editor cannot manage all of the gedcom possibilities for sources. Sources use a lot of fields. There are too many combinations. 
 *      Fields not managed within the source editor will be managed by the gedcom editor
 * 
 * -4- From all these principles, simple editor will define a unique and generic backbone of information that is easy to manipulate for the user:
 *      - Basic and most commonly used information from the editor main window
 *      - Possibility to choose a source for an event in one click
 *      - Less frequently used information from another simple window or in the gedcom editor
 * 
 * 
 * Sources can be attached to a number of element but the simple editor will not use all of them :
 *  - INDI record : used *
 *  - INDI event detail : used *
 *  - FAM event detail : used *
 * 
 *  - FAM record : not used *
 *  - OBJE record : not used *
 *  - NOTE record : not used *
 *  - ASSO structure : not used *
 *  - LDS : not used *
 *  - Personal name structure : not used (5.5 only)
 *  - Personal Name Pieces : not used (5.5.1 only)
 *  - Place structure : not used (5.5 only)
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
 *       +1 <<MULTIMEDIA_LINK>>  {0:M}                              ==> 1 Media (use first media only) // TODO : MAKE IT MULTIPLE **************
 *       +1 <<NOTE_STRUCTURE>>  {0:M}
 *       +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
 *         +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
 *       +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
 *      +1 <<CHANGE_DATE>>  {0:1}
 * 
 * 
 * 
 SOURCE_RECORD: = (5.5.1) = idem 5.5 (only use first repo and first media found)
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
 *   +1 <<MULTIMEDIA_LINK>> {0:M}                              ==> 1 Media (use first media only) // TODO : MAKE IT MULTIPLE **************
 *   +1 <<NOTE_STRUCTURE>> {0:M}
 *   +1 REFN <USER_REFERENCE_NUMBER> {0:M}
 *     +2 TYPE <USER_REFERENCE_TYPE> {0:1}
 *   +1 RIN <AUTOMATED_RECORD_ID> {0:1}
 *   +1 <<CHANGE_DATE>> {0:1}
 * 
 * 
 * 
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
 *     +1 <<MULTIMEDIA_LINK>>  {0:M}                                ==> Media not used // TODO : USE MEDIA AND MAKE IT MULTIPLE **************
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
 *     +1 <<MULTIMEDIA_LINK>> {0:M}                                 ==> Media (update mode only)   // TODO : MAKE IT MULTIPLE **************
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

    private boolean recordType = true;          // true if record type, false if citation type
    private Property hostingProperty = null;    // the property the source property belongs to
    private Entity targetSource = null;         // the source entity
    private String title = "";                  // the source title
    private String text = "";                   // the source text
    private Media targetMedia = null;           // the source media entity
    private File file = null;                   // the media file of the source media entity
    private Repository targetRepo = null;       // the repository entity
    private String repoName = "";               // the name of the repository entity
    
    // Constructor for source linked from host property (source_record as links to a source entity)
    public SourceWrapper(PropertySource propertySource) {
        if (propertySource == null) {
            return;
        }
        this.hostingProperty = propertySource;
        setTargetEntity((Source) propertySource.getTargetEntity());
        // TODO : ALSO GET MEDIA ATTACHED TO PROPERTY **************
    }

    public void setTargetEntity(Source entity) {
        this.targetSource = entity;
        setInfoFromRecord(entity);
    }

    // Constructor for source directly within host property (source_citation included underneath SOUR tag)
    public SourceWrapper(Property propertySour) {
        this.hostingProperty = propertySour;
        setInfoFromCitation(propertySour);
    }

    // Constructor for source added from source chooser
    public SourceWrapper(Source entity) {
        if (entity == null) {
            return;
        }
        setTargetEntity(entity);
    }
    
    public void setHostingProperty(Property property) {
        this.hostingProperty = property;
    }

    
   
    // Constructor from repo chooser
    public SourceWrapper(Repository repo) {
        setRepo(repo);
    }

    // Constructor from choose file
    public SourceWrapper(File f) {
        setFile(f);
    }
    
    // Constructor from change title
    public SourceWrapper(String title) {
        setTitle(title);
    }

    // Constructor from choose file/title
    public SourceWrapper(File f, String title) {
        setFile(f);
        setTitle(title);
    }
    
    
    
    
    
    
    /**
     * Read source record (5.5 or 5.5.1)
     * (reading is the same regardless of the grammar)
     */
    private void setInfoFromRecord(Property property) {
        recordType = true;

        if (property == null) {
            return;
        }
        Property propTitle = property.getProperty("TITL", true);                /* title */
        if (propTitle != null) {
            this.title = propTitle.getDisplayValue();
        } else {
            this.title = "";
        }
        Property propText = property.getProperty("TEXT", true);                 /* text */
        if (propText != null) {
            this.text = propText.getDisplayValue();
        } else {
            this.text = "";
        }
        Property propMedia = property.getProperty("OBJE", true);                /* media */  // TODO : MAKE IT MULTIPLE **************
        if (propMedia != null && propMedia instanceof PropertyMedia) {
            PropertyMedia pm = (PropertyMedia) propMedia;
            this.targetMedia = (Media) pm.getTargetEntity();
            this.file = targetMedia.getFile();
        } else if (propMedia != null) {
            this.targetMedia = null;
            Property prop = propMedia.getProperty("FILE");
            PropertyFile pFile = prop != null ? (PropertyFile) prop : null;
            this.file = pFile != null ? pFile.getFile() : null;
        } else {
            this.targetMedia = null;
            this.file = null;
        }
        Property propRepository = property.getProperty("REPO", true);           /* repo */
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
     * Read source citation (5.5 or 5.5.1)
     * (no repository in this case)
     */
    private void setInfoFromCitation(Property property) {
        recordType = false;

        if (property == null) {
            return;
        }
        this.title = property.getDisplayValue().trim();                         /* title */
        Property propText = property.getProperty("TEXT");                       /* text */
        if (propText != null) {
            this.text = propText.getDisplayValue();
        }
        Property propMedia = property.getProperty("OBJE", true);                /* media (5.5.1 only) */   // TODO : MAKE IT MULTIPLE **************
        if (propMedia != null && propMedia instanceof PropertyMedia) {
            PropertyMedia pm = (PropertyMedia) propMedia;
            this.targetMedia = (Media) pm.getTargetEntity();
            this.file = targetMedia.getFile();
        }
    }
    
    
    
    
    
    
    
    
    /**
     * Creates or Updates the SOUR property
     *    - Creation : separate SOUR entity
     *    - Update : where it is
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
        } else 
            
        // Case of property source as linked record already existing
        if (recordType && (hostingProperty instanceof PropertySource)) {
            putSourceRecord(targetSource);
            // 2 situations : remplacement of the text of the same source or replacement of the source by another one
            PropertySource ps = (PropertySource) hostingProperty;
            Source tse = (Source) ps.getTargetEntity();
            if (targetMedia.equals(tse)) { // it was just an update of the same media, quit
            } else { 
                Utils.replaceRef(ps, tse, targetSource);
            }
        } else
            
        // Case of property as source record not already linked (added and chosen from SourceChooser)
        if (recordType && !(hostingProperty instanceof PropertySource)) {
            mainProp.addSource((Source) targetSource);
            putSourceRecord(targetSource);
        }
        
    }

    
    
    
    
    
    
    /**
     * Writes the source as a link to a Source Entity
     * @param property 
     */
    private void putSourceRecord(Property property) {
        if (property == null) {
            return;
        }
        putProperty(property, "TITL", title);
        putProperty(property, "TEXT", text);
        
        // Put media, and create it if it does not exists 
        putMedia(property, file);
        
        // Put repo, and create it if it does not exists
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
                property.addRepository(targetRepo);
            } 
        } else {
          property.addRepository(targetRepo);
        }
        
    }

    
    
    
    
    /**
     * Writes the source as a link to a Source Entity
     * @param property 
     */
    private void putSourceCitation(Property property) {
        property.setValue(title);
        putProperty(property, "TEXT", text);
        if (property.getMetaProperty().allows("OBJE") && (file != null)) {
            putMedia(property, file);
        }

    }

    
    
    
    
    

    public void remove() {
        if (hostingProperty == null) {
            return;
        }
        hostingProperty.getParent().delProperty(hostingProperty);
    }


    
    
    
    
    
    public Entity getTargetSource() {
        return targetSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        this.file = f;
    }

    public String getRepoName() {
        return this.repoName;
    }

    public void setRepoName(String name) {
        this.repoName = name;
    }

    public void setRepo(Repository entity) {
        this.targetRepo = entity;
        if (entity == null) {
            this.repoName = "";
        } else {
            this.repoName = targetRepo.getRepositoryName();
        }
    }

    public Repository getRepo() {
        return targetRepo;
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

    /**
     * Update or create media to a property
     * @param property
     * @param f 
     */
    private void putMedia(Property property, File f) {

        Gedcom gedcom = property.getGedcom();
        if (gedcom.getGrammar().equals(Grammar.V55)) {  // v5.5
            Property propMedia = property.getProperty("OBJE", true);
            if (propMedia == null) {
                propMedia = property.addProperty("OBJE", "");
            }
            putFile(propMedia, f);
        } else {  // v5.5.1
            if (targetMedia == null) {
                try {
                    targetMedia = (Media) property.getGedcom().createEntity(Gedcom.OBJE);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            putFile(targetMedia, f);

            Property propMedia = property.getProperty("OBJE", true);
            if (propMedia != null && propMedia instanceof PropertyMedia) {
                PropertyMedia pm = (PropertyMedia) propMedia;
                Media tmpMedia = (Media) pm.getTargetEntity();
                if (tmpMedia != targetMedia) { // it points to another media entity, replace media.
                    property.delProperty(propMedia);
                    property.addMedia(targetMedia);
                }
            } else {
                property.addMedia(targetMedia);
            }
        }
    }

    /**
     * Update or create file value to a property
     * @param property
     * @param f 
     */
    private void putFile(Property property, File f) {
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }
        if (this.file != null && mediaFile instanceof PropertyFile) {
            ((PropertyFile) mediaFile).addFile(f);
        }
    }

    

}
