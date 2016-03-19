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
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import org.openide.util.Exceptions;



/**
 *
 * @author frederic
 */
public class NoteWrapper {

    private Property hostingProperty = null;
    private Entity targetNote = null;
    private String text = "";
    
    // Constructor for note 
    public NoteWrapper(Property property) {
        if (property == null) {
            return;
        }
        if (property instanceof PropertyNote) {
            this.hostingProperty = property;
            PropertyNote pnote = (PropertyNote) property;
            this.targetNote = (Note) pnote.getTargetEntity();
            setText(this.targetNote.getValue().trim());
        } else {
            this.hostingProperty = property;
            this.targetNote = this.hostingProperty.getEntity();
            setText(property.getValue().trim());
        }
    }

    // Constructor for note added from note chooser
    public NoteWrapper(Note entity) {
        if (entity == null) {
            return;
        }
        this.targetNote = entity;
        setText(this.targetNote.getValue().trim());
    }
    
    // Constructor from change text
    public NoteWrapper(String text) {
        setText(text);
    }

    
    public void setHostingProperty(Property property) {
        this.hostingProperty = property;
    }



    /**
     * Creates or Updates the NOTE property
     *    - Creation : separate NOTE entity
     *    - Update : where it is
     * @param mainProp (indi or event basically) 
     */
    public void update(Property mainProp) {
        // If it is a creation...
        if (hostingProperty == null) {
            try {
                if (this.targetNote == null) {
                    this.targetNote = mainProp.getGedcom().createEntity(Gedcom.NOTE);
                }
                mainProp.addNote((Note) targetNote);
                targetNote.setValue(text);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }
        
        // ... or else a modification
        Entity entity = hostingProperty.getEntity();
        // Case of property directly written within mainProp
        if ((entity instanceof Indi) && !(hostingProperty instanceof PropertyNote)) {
            hostingProperty.setValue(text);
        } else 
            
        // Case of propertyNote written within mainProp
        if ((entity instanceof Indi) && (hostingProperty instanceof PropertyNote)) {
            PropertyNote pn = (PropertyNote) hostingProperty;
            Property parent = pn.getParent();
            // add new link from parent
            parent.addNote((Note) targetNote);
            targetNote.setValue(text);
            // remove old link
            parent.delProperty(hostingProperty);
        } else
            
        // Case of property as Note entity
        if (entity instanceof Note) {
            //mainProp.addNote((Note) targetNote);
            targetNote.setValue(text);
        }
    }


    
    
    public void remove() {
        if (hostingProperty == null) {
            return;
        }
        hostingProperty.getParent().delProperty(hostingProperty);
    }

    
    public String getText() {
        return text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public void setTargetEntity(Note entity) {
        this.targetNote = entity;
    }



}
