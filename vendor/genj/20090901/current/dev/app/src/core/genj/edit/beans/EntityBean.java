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
package genj.edit.beans;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.util.Registry;

import java.awt.BorderLayout;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
public class EntityBean extends PropertyBean {

  private Preview preview;
  private JLabel changed;

  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }

  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    preview = new Preview();
    changed = new JLabel();
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, preview);
    add(BorderLayout.SOUTH, changed);
  }
  
  /**
   * Set context to edit
   */
  boolean accepts(Property prop) {
    return prop instanceof Entity;
  }
  public void setPropertyImpl(Property prop) {

    // show it
    Entity entity = (Entity)prop;
    preview.setEntity(entity);

    // add change date/time
    changed.setVisible(false);
    if (entity!=null) {
      PropertyChange change = entity.getLastChange();
      if (change!=null)
        changed.setText(resources.getString("entity.change", new String[] {change.getDateDisplayValue(), change.getTimeDisplayValue()} ));      
        changed.setVisible(true);
    }
    
    // Done
  }
  
} //ProxyEntity
