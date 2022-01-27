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
import genj.io.InputSource;
import genj.renderer.MediaRenderer;
import org.openide.util.Exceptions;

/**
 * Media can be attached to a number of element but the simple editor will not use all of them : 
 * - INDI record : used 
 * - INDI event detail : used 
 * - FAM event detail : used 
 * - SOUR record : used 
 *
 * - FAM record : not used 
 * - SUBM record : not used 
 *
 *
 */
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
    private InputSource inputSource = null;
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

    // Constructor from change title
    public MediaWrapper(String title) {
        setTitle(title);
    }

    // Constructor from choose file/title
    public MediaWrapper(InputSource f, String title) {
        recordType = false;
        setInputSource(f);
        setTitle(title);
    }

    public MediaWrapper(InputSource is) {
        setInputSource(is);
    }

    public Property getHostingProperty() {
        return this.hostingProperty;
    }

    public void setHostingProperty(Property property) {
        this.hostingProperty = property;
    }

    public void setInfoFromRecord(Property property) {
        recordType = true;

        if (property == null) {
            return;
        }

        Property mediaFile = property.getProperty("FILE", false);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.inputSource = MediaRenderer.getSource(mediaFile);
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
        Property mediaFile = property.getProperty("FILE", false);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.inputSource = MediaRenderer.getSource(mediaFile);
        }
        Property mediaTitle = property.getProperty("TITL");
        if (mediaTitle != null) {
            this.title = mediaTitle.getDisplayValue();
        }
    }

    /**
     * Creates or Updates the OBJE media property 
     * - Creation in 55 : integrated property (BLOB not supported) 
     * - Creation in 551 : separate media entity
     * - Update : where it is
     */
    public void update(int index, Property mainProp) {
        // If it is a creation...
        if (hostingProperty == null) {
            Gedcom gedcom = mainProp.getGedcom();
            if (gedcom.getGrammar().equals(Grammar.V55)) {
                hostingProperty = mainProp.addProperty("OBJE", "");
                putMediaCitation(hostingProperty);
            } else {
                try {
                    if (this.targetMedia == null) {
                        this.targetMedia = mainProp.getGedcom().createEntity(Gedcom.OBJE);
                    }
                    mainProp.addMedia((Media) targetMedia);
                    putMediaRecord((Media) targetMedia);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {

            // ... or else a modification
            // Case of Citation
            if (!recordType) {
                putMediaCitation(hostingProperty);
            } else // Case of Media record and propertyMedia already linked
            if (recordType && (hostingProperty instanceof PropertyMedia)) {
                putMediaRecord(targetMedia);
                // 2 situations : remplacement of the text of the same media or replacement of the media by another one
                PropertyMedia pm = (PropertyMedia) hostingProperty;
                Media tme = (Media) pm.getTargetEntity();
                if (targetMedia.equals(tme)) { // it was just an update of the same media, quit
                } else {
                    Utils.replaceRef(pm, tme, targetMedia);
                }
            } else // Case of Media record and link not yet created (added and chosen from MediaChooser)
            if (recordType && !(hostingProperty instanceof PropertyMedia)) {
                putMediaRecord(targetMedia);
                mainProp.addMedia((Media) targetMedia);
            }
        }

        // Now arrange sequence (move hostingProperty to index from current index)
        if (hostingProperty != null && hostingProperty.getParent() != null) {
            Property p = hostingProperty.getParent().getProperty("OBJE");
            int pos = hostingProperty.getParent().getPropertyPosition(p);
            hostingProperty.getParent().moveProperty(hostingProperty, pos + index);
        }

    }

    /**
     * Writes the media tags of a Media Entity
     *
     * 5.5: Not supported because there is no FILE behind OBJE in 5.5
     *
     * 5.5.1: 
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M}
     * +2 FORM <MULTIMEDIA_FORMAT> {1:1} 
     * +2 TITL <DESCRIPTIVE_TITLE> {0:1}
     *
     * @param property
     */
    private void putMediaRecord(Property property) {
        // Put FILE
        String extension = "";
        Property mediaFile = property.getProperty("FILE", false);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }
        if (this.inputSource != null) {
            ((PropertyFile) mediaFile).addFile(inputSource);
            extension = inputSource.getExtension();
        }

        // Put FORM
        Property mediaForm = mediaFile.getProperty("FORM");
        if (mediaForm == null) {
            mediaForm = mediaFile.addProperty("FORM", "");
        }
        if (mediaForm != null) {
            Utils.setDistinctValue(mediaForm, extension);
        }

        // Put TITL
        Property mediaTitle = mediaFile.getProperty("TITL");
        if (mediaTitle == null) {
            mediaTitle = mediaFile.addProperty("TITL", "");
        }
        if (mediaTitle != null) {
            Utils.setDistinctValue(mediaTitle, this.title);
        }
    }

    /**
     * Writes the media tags of an integrated media property
     *
     * 5.5 
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M} 
     * +1 FORM <MULTIMEDIA_FORMAT> {1:1} 
     * +1 TITL <DESCRIPTIVE_TITLE> {0:1}
     *
     * 5.5.1: 
     * +1 FILE <MULTIMEDIA_FILE_REFN> {1:M} 
     * +2 FORM <MULTIMEDIA_FORMAT> {1:1}
     * +1 TITL <DESCRIPTIVE_TITLE> {0:1}
     *
     * @param property
     */
    private void putMediaCitation(Property property) {
        // Put FILE
        String extension = "";
        Property mediaFile = property.getProperty("FILE", false);
        if (mediaFile == null) {
            mediaFile = property.addProperty("FILE", "");
        }

        if (this.inputSource != null) {
            ((PropertyFile) mediaFile).addFile(inputSource);
            extension = inputSource.getExtension();
            if (extension == null) {
                extension = "";
            }
        }

        // Put FORM
        String version = property.getGedcom().getGrammar().getVersion();
        if (version.contains("5.5.1")) {
            Property pForm = mediaFile.getProperty("FORM");
            if (pForm == null) {
                pForm = mediaFile.addProperty("FORM", "");
            }
            if (pForm != null) {
                Utils.setDistinctValue(pForm, extension);
            }
        } else {
            Property pForm = property.getProperty("FORM");
            if (pForm == null) {
                pForm = property.addProperty("FORM", "");
            }
            if (pForm != null) {
                Utils.setDistinctValue(pForm, extension);
            }
        }

        // Put TITL
        Property mediaTitle = property.getProperty("TITL");
        if (mediaTitle == null) {
            mediaTitle = property.addProperty("TITL", "");
        }
        if (mediaTitle != null) {
            Utils.setDistinctValue(mediaTitle, this.title != null ? this.title : "");
        }
    }

    public void remove() {
        if (hostingProperty == null) {
            return;
        }
        hostingProperty.getParent().delProperty(hostingProperty);
    }

    public InputSource getInputSource() {
        return inputSource;
    }

    public void setInputSource(InputSource is) {
        this.inputSource = is;
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
