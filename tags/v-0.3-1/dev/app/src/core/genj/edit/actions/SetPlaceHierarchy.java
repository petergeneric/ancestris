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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyPlace;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

import javax.swing.JPanel;

/**
 * Set the place hierarchy used in a gedcom file
 */
public class SetPlaceHierarchy extends AbstractChange {

    /** the place to use as the global example */
    private PropertyPlace place;
    
    /** textfield for hierarchy */
    private TextFieldWidget hierarchy;
    
    /**
     * Constructor
     */
    public SetPlaceHierarchy(PropertyPlace place, ViewManager mgr) {
      super(place.getGedcom(), place.getImage(false), resources.getString("place.hierarchy"), mgr);

      this.place = place;
    }

    /**
     * no confirmation message needed
     */    
    protected String getConfirmMessage() {
      return resources.getString("place.hierarchy.msg", place.getGedcom().getName());
   }
    
    /**
     * Override content components to show to user 
     */
    protected JPanel getDialogContent() {
      
      JPanel result = new JPanel(new NestedBlockLayout("<col><confirm wx=\"1\" wy=\"1\"/><enter wx=\"1\"/></col>"));

      // prepare textfield for formar
      hierarchy = new TextFieldWidget(place.getFormatAsString());
      
      result.add(getConfirmComponent());
      result.add(hierarchy);
      
      // done
      return result;
    }
    
    /**
     * set the submitter
     */
    public void perform(Gedcom gedcom) throws GedcomException {
      place.setFormatAsString(true, hierarchy.getText().trim());
    }

} //SetPlaceFormat

