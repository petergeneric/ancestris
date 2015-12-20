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
import genj.gedcom.Grammar;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySource;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import java.io.File;



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
 *   +1 TITL <SOURCE_DESCRIPTIVE_TITLE> {0:1}
 *     +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE> {0:M}
 *   +1 ABBR <SOURCE_FILED_BY_ENTRY> {0:1}
 *   +1 PUBL <SOURCE_PUBLICATION_FACTS> {0:1}
 *     +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS> {0:M}
 *   +1 TEXT <TEXT_FROM_SOURCE> {0:1}
 *     +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
 *   +1 <<SOURCE_REPOSITORY_CITATION>> {0:M}
 *   +1 <<MULTIMEDIA_LINK>> {0:M}
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
 *   n SOUR @<XREF:SOUR>@    pointer to source record   {1:1}       ==> only use pointer to source record
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
 *   |              Systems not using source records 
 *   n SOUR <SOURCE_DESCRIPTION>  {1:1}                             ==> Title (read mode only)
 *     +1 [ CONC | CONT ] <SOURCE_DESCRIPTION>  {0:M}
 *     +1 TEXT <TEXT_FROM_SOURCE>  {0:M}                            ==> Text (read mode only)
 *        +2 [CONC | CONT ] <TEXT_FROM_SOURCE>  {0:M}
 *     +1 <<NOTE_STRUCTURE>>  {0:M}
 *   ]
 * 
 * SOURCE_CITATION: =  (5.5.1) (almost the same as 5.5 if I do not use QUAY, media and note links)
 *   [
 *     n SOUR @<XREF:SOUR>@ {1:1} pointer to source record (preferred)
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
 *  |
 *  n SOUR <SOURCE_DESCRIPTION> {1:1} Systems not using source records 
 *     +1 [CONC|CONT] <SOURCE_DESCRIPTION> {0:M}
 *     +1 TEXT <TEXT_FROM_SOURCE> {0:M}
 *        +2 [CONC|CONT] <TEXT_FROM_SOURCE> {0:M}
 *     +1 QUAY <CERTAINTY_ASSESSMENT> {0:1}
 *     +1 <<MULTIMEDIA_LINK>> {0:M}                                 ==> Media  (read mode only)
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

    private Property hostingProperty = null;    // the property the source property belongs to
    private Entity targetSource = null;         // the source entity
    private String title = "";                  // the source title
    private String text = "";                   // the source text
    private Media targetMedia = null;           // the source media entity
    private File file = null;                   // the media file of the source media entity
    private Repository targetRepo = null;       // the repository entity
    private String repoName = "";               // the name of the repository entity
    
    // Constructor for source linked from host property (source_citation as links to a source entity)
    public SourceWrapper(PropertySource propertySource) {
        if (propertySource == null) {
            return;
        }
        this.hostingProperty = propertySource;
        setTargetEntity((Source) propertySource.getTargetEntity());
    }

    // Constructor for source directly within host property (source_citation included underneath SOUR tag)
    public SourceWrapper(Property propertySour) {
        if (propertySour == null) {
            return;
        }
        this.hostingProperty = propertySour;
        this.title = propertySour.getDisplayValue();
        Property propText = propertySour.getProperty("TEXT");
        if (propText != null) {
            this.title = propText.getDisplayValue();
        }
        Property propMedia = propertySour.getProperty("OBJE", true);
        if (propMedia != null && propMedia instanceof PropertyMedia) {
            PropertyMedia pm = (PropertyMedia) propMedia;
            this.targetMedia = (Media) pm.getTargetEntity();
            this.file = targetMedia.getFile();
        }
    }

    // Constructor for source added from source chooser
    public SourceWrapper(Source entity) {
        if (entity == null) {
            return;
        }
        setTargetEntity(entity);
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
    
    
    
    
    
    public void setTargetEntity(Source entity) {
        this.targetSource = entity;
        setInfo(entity);
    }

    
    public void setInfo(Property property) {
        if (property == null) {
            return;
        }
        Property propTitle = property.getProperty("TITL", true);
        if (propTitle != null) {
            this.title = propTitle.getDisplayValue();
        } else {
            this.title = "";
        }
        Property propText = property.getProperty("TEXT", true);
        if (propText != null) {
            this.text = propText.getDisplayValue();
        } else {
            this.text = "";
        }
        Property propMedia = property.getProperty("OBJE", true);
        if (propMedia != null && propMedia instanceof PropertyMedia) {
            PropertyMedia pm = (PropertyMedia) propMedia;
            this.targetMedia = (Media) pm.getTargetEntity();
            this.file = targetMedia.getFile();
        } else {
            this.targetMedia = null;
            this.file = null;
        }
        Property propRepository = property.getProperty("REPO", true);
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
     * Creates or Updates the OBJE media property
     *    - Creation in 55  : integrated property (BLOB not supported)
     *    - Creation in 551 : separate media entity
     *    - Update : where it is
     * @param indi 
     */
    public void update(Indi indi) {
        // If it is a creation...
        if (hostingProperty == null) {
            Gedcom gedcom = indi.getGedcom();
            if (gedcom.getGrammar().equals(Grammar.V55)) {
                putMediaIntegrated(indi.addProperty("OBJE", ""));
            } else {
//                try {
//                    if (this.targetMedia == null) {
//                        this.targetMedia = indi.getGedcom().createEntity(Gedcom.OBJE);
//                    }
//                    indi.addMedia((Media) targetMedia);
//                    putMediaLinked((Media) targetMedia);
//                } catch (GedcomException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
            }
            return;
        }
        
        // ... or else a modification
        Entity entity = hostingProperty.getEntity();
        // Case of property directly written within INDI
        if ((entity instanceof Indi) && !(hostingProperty instanceof PropertyMedia)) {
            putMediaIntegrated(hostingProperty);
        } else 
            
        // Case of propertyMedia written within INDI
        if ((entity instanceof Indi) && (hostingProperty instanceof PropertyMedia)) {
            PropertyMedia pm = (PropertyMedia) hostingProperty;
            Property parent = pm.getParent();
            // add new link from parent
            parent.addMedia((Media) targetMedia);
            putMediaLinked(targetMedia);
            // remove old link
            parent.delProperty(hostingProperty);
        } else
            
        // Case of property as Media entity (added chosen from MediaChooseer)
        if (entity instanceof Media) {
            indi.addMedia((Media) targetMedia);
            putMediaLinked(targetMedia);
        }
    }

    /**
     * Writes the media tags of a Media Entity
     * 
     * 5.5:
     *   Not supported
     * 
     * 5.5.1:
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
     *      +2 TITL <DESCRIPTIVE_TITLE> {0:1}
     * 
     * @param property 
     */
    private void putMediaLinked(Property property) {
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }
        ((PropertyFile) mediaFile).addFile(this.file);
        Property mediaTitle = mediaFile.getProperty("TITL");
        if (mediaTitle == null) {
            mediaTitle = mediaFile.addProperty("TITL", "");
        }
        if (mediaTitle != null) {
            mediaTitle.setValue(this.title);
        }
    }

    /**
     * Writes the media tags of an integrated media property
     * 
     * 5.5 and 5.5.1:
     *  +1 FILE <MULTIMEDIA_FILE_REFN>   {1:M}
     *  +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
     * 
     * @param property 
     */
    private void putMediaIntegrated(Property property) {
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }
        if (this.file != null) {
            ((PropertyFile) mediaFile).addFile(this.file);
        }
        Property mediaTitle = property.getProperty("TITL");
        if (mediaTitle == null) {
            mediaTitle = property.addProperty("TITL", "");
        }
        if (mediaTitle != null) {
            mediaTitle.setValue(this.title != null ? this.title : "");
        }
    }

    public void remove(Indi indi) {
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


}
