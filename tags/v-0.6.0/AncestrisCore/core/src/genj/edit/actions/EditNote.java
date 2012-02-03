/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.edit.actions;

import genj.common.SelectEntityWidget;
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Note;
import genj.gedcom.Options;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Edit note for a property
 */
public class EditNote extends Action2 {
  
  private final static Resources RESOURCES = Resources.get(EditNote.class);
  
  public final static ImageIcon 
    EDIT_NOTE = Grammar.V551.getMeta(new TagPath("NOTE")).getImage(),
    NEW_NOTE = EDIT_NOTE.getOverLayed(Images.imgNew),
    NO_NOTE = EDIT_NOTE.getTransparent(128);
  
  private Property property;
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditNote(Property property) {
    this(property, false);
  }
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditNote(Property property, boolean showNone) {
    
    this.property = property;
    
    boolean has = hasNote(property);
    setImage(has ? EDIT_NOTE : (showNone?NO_NOTE:NEW_NOTE));
    setText(RESOURCES.getString(has ? "edit" : "new", Gedcom.getName(Gedcom.NOTE)));
    setTip(getText());
  }
  
  private boolean hasNote(Property property) {
    for (Property note : property.getProperties(Gedcom.NOTE)) {
      if (note instanceof PropertyNote && note.isValid() && ((PropertyNote)note).getTargetEntity().getValue().length()>0)
        return true;
      if (note.getValue().length()>0)
        return true;
    }
    return false;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    
    final Property note = property.getProperty("NOTE", true);
    
    JPanel panel = new JPanel(new NestedBlockLayout("<col><entity/><text gy=\"1\"/></col>"));
    
    final SelectEntityWidget select = new SelectEntityWidget(property.getGedcom(), Gedcom.NOTE, 
        RESOURCES.getString("new", Gedcom.getName(Gedcom.NOTE)));
    panel.add(select);
    final JTextPane text = new JTextPane();
    text.setPreferredSize(new Dimension(128,128));
    panel.add(new JScrollPane(text));
    
    select.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Note selection = (Note)select.getSelection();
        text.setText(selection!=null ? selection.getDisplayValue() : "");
      }
    });
          
    if (note instanceof PropertyNote)
      select.setSelection( ((PropertyNote)note).getTargetEntity() );
    else if (note!=null)
      text.setText(note.getValue());

    if (0!=new GedcomDialog(property.getGedcom(), property.toString() + " - " + getTip(), DialogHelper.QUESTION_MESSAGE, panel, Action2.okCancel(), e).show())
      return;

    property.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
      public void perform(Gedcom gedcom) throws GedcomException {
        
        Note newNote = (Note)select.getSelection();
        
        // inline?
        if (newNote==null&&!(note instanceof PropertyNote)&&Options.getInstance().isUseInline) {
          if (note!=null)
            note.setValue(text.getText());
          else
            property.addProperty("NOTE", text.getText());
          return;
        }

        // text to delete or (new) note
        String value = text.getText().trim();
        if (value.length()==0) {
          if (newNote!=null) {
            gedcom.deleteEntity(newNote);
            newNote = null;
          }
        } else {
          if (newNote==null)
            newNote = (Note)property.getGedcom().createEntity("NOTE");
          newNote.setValue(value);
        }
        
        // delete old note
        if (note!=null&&note.isValid()) {
          Note oldNote = null;
          if (note instanceof PropertyNote) 
            oldNote = (Note)((PropertyNote)note).getTargetEntity();
          property.delProperty(note);
          if (oldNote!=null&&oldNote!=newNote&& (!oldNote.isConnected()))
            gedcom.deleteEntity(oldNote);
        }

        // link new note
        if (newNote!=null)
          property.addNote(newNote);
        
      }
    });

    // done
  }

}
