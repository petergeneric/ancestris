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
import genj.gedcom.Fam;
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

    private boolean recordType = true;          // true if type of note is record, false if citation
    private Property hostingProperty = null;
    private Entity targetNote = null;
    private String text = "";
    
    // Constructor for note 
    public NoteWrapper(Property property) {
        if (property == null) {
            return;
        }
        this.hostingProperty = property;
        if (hostingProperty instanceof PropertyNote) {
            PropertyNote pnote = (PropertyNote) property;
            this.recordType = true;
            this.targetNote = (Note) pnote.getTargetEntity();
            setText(this.targetNote.getValue().trim());
        } else {
            this.recordType = false;
            this.targetNote = hostingProperty.getEntity();
            setText(property.getValue().trim());
        }
    }

    // Constructor for note added from note chooser
    public NoteWrapper(Note entity) {
        if (entity == null) {
            return;
        }
        this.recordType = true;
        this.targetNote = entity;
        setText(this.targetNote.getValue().trim());
    }
    
    // Constructor from change text
    public NoteWrapper(String text) {
        setText(text);
    }

    
    public Property getHostingProperty() {
        return this.hostingProperty;
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
                Utils.setDistinctValue(targetNote, text);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            return;
        }
        
        // ... or else a modification
        Entity entity = hostingProperty.getEntity();
        // Case of property directly written within mainProp
        if ((entity instanceof Indi || entity instanceof Fam) && !(hostingProperty instanceof PropertyNote)) {
            Utils.setDistinctValue(hostingProperty, text);
        } else 
            
        // Case of propertyNote written within mainProp
        if ((entity instanceof Indi || entity instanceof Fam) && (hostingProperty instanceof PropertyNote)) {
            Utils.setDistinctValue(targetNote, text);
            // 2 situations : remplacement of the text of the same note or replacement of the note by another one
            PropertyNote pnote = (PropertyNote) hostingProperty;
            Note tne = (Note) pnote.getTargetEntity();
            if (targetNote.equals(tne)) { // it was just an update of the same note, quit
            } else { 
                Utils.replaceRef(pnote, tne, targetNote);
            }
        } else
            
        // Case of property as Note entity
        if (entity instanceof Note) {
            Utils.setDistinctValue(targetNote, text);
        }
    }


    
    
    public void remove() {
        if (hostingProperty == null) {
            return;
        }
        hostingProperty.getParent().delProperty(hostingProperty);
    }

    
    public boolean isRecord() {
        return recordType;
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

    public Entity getTargetNote() {
        return targetNote;
    }



}
