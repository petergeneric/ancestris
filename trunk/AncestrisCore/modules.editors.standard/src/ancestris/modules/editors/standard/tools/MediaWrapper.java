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

    private Property hostingProperty = null;
    private Entity targetMedia = null;
    private File file = null;
    private String title = "";
    
    // Constructor for media directly within host entity
    public MediaWrapper(Property propertyObje) {
        if (propertyObje == null) {
            return;
        }
        this.hostingProperty = propertyObje;
        Property mediaFile = propertyObje.getProperty("FILE", true);
        if (mediaFile != null && mediaFile instanceof PropertyFile) {
            this.file = ((PropertyFile) mediaFile).getFile();
        }
        Property mediaTitle = propertyObje.getProperty("TITL");
        if (mediaTitle != null) {
            this.title = mediaTitle.getDisplayValue();
        }
    }

    // Constructor for media linked from host entity
    public MediaWrapper(PropertyMedia propertyMedia) {
        if (propertyMedia == null) {
            return;
        }
        this.hostingProperty = propertyMedia;
        setTargetEntity((Media) propertyMedia.getTargetEntity());
    }

    // Constructor for media added from media chooser
    public MediaWrapper(Media entity) {
        if (entity == null) {
            return;
        }
        this.targetMedia = entity;
        setInfo(entity);
    }
    

    
    
    
    public MediaWrapper(File f) {
        setFile(f);
    }
    
    public MediaWrapper(String title) {
        setTitle(title);
    }

    
    
    
    
    public void setTargetEntity(Media entity) {
        this.targetMedia = entity;
        setInfo(targetMedia);
    }

    
    public void setInfo(Property property) {
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

    
    public void update(Indi indi) {
        // If media is null, if came from a media file, so create Media as standalone Media entity and add link to indi
        if (hostingProperty == null) {
            try {
                this.targetMedia = indi.getGedcom().createEntity(Gedcom.OBJE);
                putMedia((Media) targetMedia);
                indi.addMedia((Media) targetMedia);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
            return;
        }
        
        Entity entity = hostingProperty.getEntity();
        // Case of property directly written within INDI
        if ((entity instanceof Indi) && !(hostingProperty instanceof PropertyMedia)) {
            Property mediaFile = hostingProperty.getProperty("FILE", true);
            if (mediaFile == null) {
                mediaFile = hostingProperty.addProperty("FILE", "");
            }
            ((PropertyFile) mediaFile).addFile(this.file);
            Property mediaTitle = hostingProperty.getProperty("TITL");
            if (mediaTitle == null) {
                mediaTitle = hostingProperty.addProperty("TITL", "");
            }
            if (mediaTitle != null) {
                mediaTitle.setValue(this.title);
            }
        } else 
            
        // Case of propertyMedia written within INDI
        if ((entity instanceof Indi) && (hostingProperty instanceof PropertyMedia)) {
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ hostingProperty is a PropertyMedia");
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ hostingProperty = " + hostingProperty.toString());
            PropertyMedia pm = (PropertyMedia) hostingProperty;
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ pm = " + pm.toString());
            Property parent = pm.getParent();
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ parent = " + parent.toString());
            // add new link from parent
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ targetMedia = " + targetMedia.toString());
            parent.addMedia((Media) targetMedia);
            // remove old link
            System.out.println("¤¤¤¤¤¤¤¤¤¤ DEBUG ¤¤¤¤¤¤¤¤¤¤ deleting hostingProperty");
            parent.delProperty(hostingProperty);
        } else
            
        // Case of property as Media entity (added chosen from MediaChooseer)
        if (entity instanceof Media) {
            indi.addMedia((Media) targetMedia);
        }
    }
    
    private void putMedia(Property property) {
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


}
