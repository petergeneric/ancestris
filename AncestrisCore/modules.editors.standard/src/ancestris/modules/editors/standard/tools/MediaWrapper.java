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
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import java.io.File;
import org.openide.util.Exceptions;


//    
//  MULTIMEDIA_LINK: 5.5
//
//  [          /* embedded form*/
//  n  OBJE @<XREF:OBJE>@  {1:1}                        ==> PropertyMedia to entity
//  |          /* linked form*/ 
//  n  OBJE           {1:1}                             ==> Direct
//    +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
//    +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
//    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
//    +1 <<NOTE_STRUCTURE>>  {0:M}
//  ]
//
//  MULTIMEDIA_LINK: 5.5.1
//  n OBJE @<XREF:OBJE>@  {1:1}                         ==> PropertyMedia to entity
//  |
//  n OBJE                                              ==> Direct
//    +1 FILE <MULTIMEDIA_FILE_REFN>   {1:M}
//        +2 FORM <MULTIMEDIA_FORMAT>     {1:1}
//            +3 MEDI <SOURCE_MEDIA_TYPE>  {0:1}
//    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
//
//
////////////////////////////////////////////////////////////
//
//  MULTIMEDIA_RECORD: 5.5                              ==> BLOB (not supported)
//  n @<XREF:OBJE>@ OBJE  {1:1}
//    +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
//    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
//    +1 <<NOTE_STRUCTURE>>  {0:M}
//    +1 <<SOURCE_CITATION>>  {0:M}
//    +1 BLOB        {1:1}
//      +2 CONT <ENCODED_MULTIMEDIA_LINE>  {1:M}
//    +1 OBJE @<XREF:OBJE>@     /* chain to continued object */  {0:1}
//    +1 REFN <USER_REFERENCE_NUMBER>  {0:M}
//      +2 TYPE <USER_REFERENCE_TYPE>  {0:1}
//    +1 RIN <AUTOMATED_RECORD_ID>  {0:1}
//    +1 <<CHANGE_DATE>>  {0:1}
//
//  MULTIMEDIA_RECORD: 5.5.1                            ==> FILE (supported)
//  n @XREF:OBJE@ OBJE {1:1}
//    +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
//        +2 FORM <MULTIMEDIA_FORMAT> {1:1}
//            +3 TYPE <SOURCE_MEDIA_TYPE> {0:1}
//        +2 TITL <DESCRIPTIVE_TITLE> {0:1}
//    +1 REFN <USER_REFERENCE_NUMBER> {0:M}
//        +2 TYPE <USER_REFERENCE_TYPE> {0:1}
//    +1 RIN <AUTOMATED_RECORD_ID> {0:1}
//    +1 <<NOTE_STRUCTURE>> {0:M}
//    +1 <<SOURCE_CITATION>> {0:M}
//    +1 <<CHANGE_DATE>> {0:1}
//
//



/**
 *
 * @author frederic
 */
public class MediaWrapper {

    private boolean recordType = true;          // true if type of media is record, false if citation
    private Property hostingProperty = null;
    private Entity targetMedia = null;
    private File file = null;
    private String title = "";
    
    // Constructor for media linked 
    public MediaWrapper(PropertyMedia propertyMedia) {
        if (propertyMedia == null) {
            return;
        }
        this.hostingProperty = propertyMedia;
        setTargetEntity((Media) propertyMedia.getTargetEntity());
    }

    public void setTargetEntity(Media entity) {
        this.targetMedia = entity;
        setInfoFromRecord(targetMedia);
    }

    
    
    // Constructor for media directly within host entity
    public MediaWrapper(Property propertyObje) {
        this.hostingProperty = propertyObje;
        setInfoFromCitation(propertyObje);
    }


    

    // Constructor for media added from media chooser
    public MediaWrapper(Media entity) {
        if (entity == null) {
            return;
        }
        this.targetMedia = entity;
        setInfoFromRecord(entity);
    }
    
    // Constructor from choose file
    public MediaWrapper(File f) {
        setFile(f);
    }
    
    // Constructor from change title
    public MediaWrapper(String title) {
        setTitle(title);
    }

    // Constructor from choose file/title
    public MediaWrapper(File f, String title) {
        setFile(f);
        setTitle(title);
    }
    
    
    
    
    
    public void setInfoFromRecord(Property property) {
        recordType = true;

        if (property == null) {
            return;
        }
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.file = ((PropertyFile) mediaFile).getFile();
            Property mediaTitle = mediaFile.getProperty("TITL");
            if (mediaTitle != null) {
                this.title = mediaTitle.getDisplayValue();
            }
        }
    }

    
    private void setInfoFromCitation(Property property) {
        recordType = false;

        if (property == null) {
            return;
        }
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.file = ((PropertyFile) mediaFile).getFile();
        }
        Property mediaTitle = property.getProperty("TITL");
        if (mediaTitle != null) {
            this.title = mediaTitle.getDisplayValue();
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
                putMediaCitation(indi.addProperty("OBJE", ""));
            } else {
                try {
                    if (this.targetMedia == null) {
                        this.targetMedia = indi.getGedcom().createEntity(Gedcom.OBJE);
                    }
                    indi.addMedia((Media) targetMedia);
                    putMediaRecord((Media) targetMedia);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return;
        }
        
        // ... or else a modification
        // Case of Citation
        if (!recordType) {
            putMediaCitation(hostingProperty);
        } else 
            
        // Case of Media record and propertyMedia already linked
        if (recordType && (hostingProperty instanceof PropertyMedia)) {
            putMediaRecord(targetMedia);
            // 2 situations : remplacement of the text of the same media or replacement of the media by another one
            PropertyMedia pm = (PropertyMedia) hostingProperty;
            Media tme = (Media) pm.getTargetEntity();
            if (targetMedia.equals(tme)) { // it was just an update of the same media, quit
            } else { 
                Utils.replaceRef(pm, tme, targetMedia);
            }
        } else
            
        // Case of Media record and link not yet created (added and chosen from MediaChooser)
        if (recordType &&  !(hostingProperty instanceof PropertyMedia)) {
            putMediaRecord(targetMedia);
            indi.addMedia((Media) targetMedia);
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
    private void putMediaRecord(Property property) {
        Property mediaFile = property.getProperty("FILE", true);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }
        if (this.file != null) {
            ((PropertyFile) mediaFile).addFile(this.file);
        }
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
    private void putMediaCitation(Property property) {
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

    
    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        this.file = f;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public Entity getTargetMedia() {
        return targetMedia;
    }


}
