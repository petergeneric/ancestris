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

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import java.io.File;


//    
//  MULTIMEDIA_LINK: 5.5
//
//  [          /* embedded form*/
//  n  OBJE @<XREF:OBJE>@  {1:1}                        ==> PropertyMedia to entity
//  |          /* linked form*/ 
//  n  OBJE           {1:1}                             ==> Direct
//    +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
//    +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
//    +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
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

    // Different forms of media
    private int TYPE_ENTITY = 0;    // entity form   : Media (MULTIMEDIA_RECORD)
    private int TYPE_PROPERTY = 1;  // embedded form : PropertyMedia (MULTIMEDIA_LINK)
    private int TYPE_DIRECT = 2;    // linked form   : OBJE property (MULTIMEDIA_LINK)
    
    private Property media = null;
    private File file = null;
    private String title = "";
    
    // Constructors
    public MediaWrapper(PropertyMedia propertyMedia) {
        if (propertyMedia == null) {
            return;
        }
        this.media = propertyMedia;
        Property targetEntity = propertyMedia.getTargetEntity();
        setMedia(targetEntity);
    }

    public MediaWrapper(Property propertyObje) {
        if (propertyObje == null) {
            return;
        }
        this.media = propertyObje;
        Property mediaFile = propertyObje.getProperty("FILE", true);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.file = ((PropertyFile) mediaFile).getFile();
        }
        Property mediaTitle = propertyObje.getProperty("TITL");
        if (mediaTitle != null) {
            this.title = mediaTitle.getDisplayValue();
        }
    }

    public MediaWrapper(File f) {
        setFile(f);
    }
    
    public MediaWrapper(String title) {
        setTitle(title);
    }

    
    
    
    
    
    
    public void setMedia(Property property) {
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

    public void flush() {
        this.media = null;
        this.file = null;
        this.title = null;
    }

}
