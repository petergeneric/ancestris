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
package genj.edit.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertyQuality;
import genj.util.GridBagHelper;


import javax.swing.JComboBox;

/**
 * A bean for editing quality (QUAY) properties
 * @author nils@meiers.net
 */
public class QualityBean extends PropertyBean {

  /** members */
  private final GridBagHelper gh = new GridBagHelper(this);
  private JComboBox<String> choices;
  
  public QualityBean() {
    
    choices = new JComboBox<String>(PropertyQuality.QUALITIES);
    choices.addActionListener(changeSupport);

    // layout
    gh.add(choices, 0, 0, 1, 1, GridBagHelper.FILL_HORIZONTAL);
    gh.addFiller(0, 1);
    
    // focus
    defaultFocus = choices;
  }
  
  /**
   * Finish editing a property through proxy
   */
  @Override
  protected void commitImpl(Property property) {
    ((PropertyQuality)property).setQuality(choices.getSelectedIndex());
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyQuality quality = (PropertyQuality)prop;
    if (quality==null)
      choices.setSelectedIndex(-1);
    else 
      choices.setSelectedIndex(quality.getQuality());

  }

} //QualityBean
