/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
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

import java.awt.event.ActionEvent;

import spin.Spin;

import ancestris.core.resources.Images;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import ancestris.core.actions.AbstractAncestrisAction;

/**
 * Redo on Gedcom
 */  
public class Undo extends AbstractAncestrisAction implements GedcomMetaListener {
  
  /** the gedcom */
  private Gedcom gedcom;
  
  /**
   * Constructor
   */
  public Undo() {
    this(null, false);
  }

  /**
   * Constructor
   */
  public Undo(Gedcom gedcom) {
    this(gedcom, gedcom.canUndo());
  }

  /**
   * Constructor
   */
  public Undo(Gedcom gedcom, boolean enabled) {
    
    // setup looks
    setImage(Images.imgUndo);
    setText(AbstractChange.resources.getString("undo"));    
    setTip(getText());
    setEnabled(enabled);
    
    // remember
    this.gedcom = gedcom;
    
  }
  
  public void follow(Gedcom newGedcom) {
    
    if (gedcom==newGedcom)
      return;
    
    if (gedcom!=null)
      gedcom.removeGedcomListener((GedcomMetaListener)Spin.over(this));
    
    gedcom = newGedcom;
    
    if (gedcom!=null)
      gedcom.addGedcomListener((GedcomMetaListener)Spin.over(this));
    
    
    setEnabled(gedcom!=null && gedcom.canUndo());
  }

  /**
   * Undo changes from last transaction
   */
  @Override
  public void actionPerformed(ActionEvent event) {
    if (gedcom!=null&&gedcom.canUndo())
      gedcom.undoUnitOfWork();
  }
  
  @Override
  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    // ignored
  }

  @Override
  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    // ignored
  }

  @Override
  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    // ignored
  }

  @Override
  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    // ignored
  }

  @Override
  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
    // ignored
  }

  @Override
  public void gedcomHeaderChanged(Gedcom gedcom) {
    // ignored
  }

  @Override
  public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    // ignored
  }
  
  @Override
  public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    // ignored
  }

  @Override
  public void gedcomWriteLockAcquired(Gedcom gedcom) {
    // ignored
  }

  @Override
  public void gedcomWriteLockReleased(Gedcom gedcom) {
    if (this.gedcom==null||this.gedcom==gedcom)
      setEnabled(gedcom.canUndo());
  }
} //Undo

