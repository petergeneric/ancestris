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
 * Sources are behind 
 *  - INDI record : used *
 *  - Event Detail : used *
 *  - FAM record : used *
 * 
 *  - OBJE record : not used *
 *  - NOTE record : not used *
 *  - ASSO structure : not used *
 *  - LDS : not used *
 *  - Personal name structure : not used (5.5 only)
 *  - Personal Name Pieces : not used (5.5.1 only)
 *  - Place structure : not used (5.5 only)
 * 
 * Systems that describe sources using the AUTHor, TITLe, PUBLication, and REPOsitory fields can
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
 * FL : Notes and media that usually come at same level of sources are not used, Those used will be within sources
 * 
 * FL : Sources used fields will be TITL, TEXT, MEDIA and REPO, Rest will be facultative or used in gedcom editor
 *      In Source chooser we will have TITL and MEDIA
 * 
 * FL : When souce records are used, grammar does not matter for title, text, repo and media
 *      When source citations are used, information can be updated but :
 *          - no REPO can be used => warn user
 *          - and in 5.5, no media can be used => warn user
 * 
 * 
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
 *       +1 <<MULTIMEDIA_LINK>>  {0:M}                              ==> 1 Media (use first media only)
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
 *   +1 <<MULTIMEDIA_LINK>> {0:M}                              ==> 1 Media (use first media only)
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
 *     +1 <<MULTIMEDIA_LINK>>  {0:M}
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
 * SOURCE_CITATION: =  (5.5.1) (almost the same as 5.5 if I do not use QUAY, media and note links)
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
 *     +1 <<MULTIMEDIA_LINK>> {0:M}                                 ==> Media (update mode only)
 *     +1 <<NOTE_STRUCTURE>> {0:M}
 *  ]
 * 
 * 
 * 
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
        Property propMedia = property.getProperty("OBJE", true);                /* media */
        if (propMedia != null && propMedia instanceof PropertyMedia) {
            PropertyMedia pm = (PropertyMedia) propMedia;
            this.targetMedia = (Media) pm.getTargetEntity();
            this.file = targetMedia.getFile();
        } else if (propMedia != null) {
            this.targetMedia = null;
            Property prop = propMedia.getProperty("FILE");
            PropertyFile pFile = prop != null ? (PropertyFile) prop : null;
            this.file = pFile != null ? pFile.getFile() : null;
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
        Property propMedia = property.getProperty("OBJE", true);                /* media (5.5.1 only) */
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
            PropertySource ps = (PropertySource) hostingProperty;
            Property parent = ps.getParent();
            if (parent != null) {
                // add new link from parent
                parent.addSource((Source) targetSource);
                putSourceRecord(targetSource);
                // remove old link
                parent.delProperty(hostingProperty);
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
        if (property.getMetaProperty().allows("OBJE")) {
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
