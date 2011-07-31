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

import genj.edit.BeanPanel;
import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Edit an event
 */
public class EditEvent extends Action2 {
  
  private final static Resources RESOURCES = Resources.get(EditEvent.class);
  private Property property;
  
  /**
   * Constructor
   * @param property the property the note is for
   */
  public EditEvent(Property property) {
    this.property = property;
    setImage(Images.imgView);
    setText(RESOURCES.getString("edit", property.getPropertyName()));
    setTip(getText());
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    
    
    final BeanPanel panel = new BeanPanel();
    panel.setRoot(property);
    
    final Action[] actions = Action2.okCancel();
    actions[0].setEnabled(false);
    
    panel.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        actions[0].setEnabled(panel.isCommittable());
      }
    });
    
    if (0==DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, panel, actions, e)) {
      property.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) throws GedcomException {
          panel.commit();
        }
      });
    }
    
    panel.setRoot(null);
    
  }

}
