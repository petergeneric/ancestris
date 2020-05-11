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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.resources.Images;
import ancestris.util.swing.DialogManager;
import genj.edit.BeanPanel;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Edit an event
 * 
 * 
 * 2020-05-10 - This class does not seem to be used. To be removed ???
 * 
 */
public class EditEvent extends AbstractAncestrisAction {
  
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
    
    final DialogManager dialog = DialogManager.create(getText(),panel)
            .setOptionType(DialogManager.OK_CANCEL_OPTION)
            .setDialogId("edit.event");
    panel.addChangeListener(new ChangeListener() {
        @Override
      public void stateChanged(ChangeEvent e) {
          dialog.setValid(panel.isCommittable());
      }
    });
    if (DialogManager.OK_OPTION == dialog.show()){
      property.getGedcom().doMuteUnitOfWork(new UnitOfWork() {
          @Override
        public void perform(Gedcom gedcom) throws GedcomException {
          panel.commit();
        }
      });
    }
    
    panel.setRoot(null);
    
  }

}
