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

import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.time.Delta;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;

import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * A bean that lets the user edit AGE
 */
public class AgeBean extends PropertyBean {
  
  private final static String TEMPLATE = "99y 9m 9d";

  /** members */
  private ChoiceWidget choice;
  private ActionUpdate update;
  private String newAge;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit(Property property) {
    super.commit(property);
    property.setValue(choice.getText());
  }
  
  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    choice = new ChoiceWidget(Arrays.asList(PropertyAge.PHRASES));
    choice.addChangeListener(changeSupport);
    
    setLayout(new NestedBlockLayout("<col><row><value/><template/></row><row><action/></row></col>"));
    add(choice);
    add(new JLabel(TEMPLATE));
    
    update =  new ActionUpdate();
    add(new JButton(update));
    
  }
  
  boolean accepts(Property prop) {
    return prop instanceof PropertyAge;
  }
  
  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyAge age = (PropertyAge)prop;
    if (age==null)
      return;

    // update components
    choice.setText(age.getValue());

    Delta delta = Delta.get(age.getEarlier(), age.getLater());
    newAge = delta==null ? null : delta.getValue();
    update.setEnabled(newAge!=null);
    
    // Done
  }
  
  /**
   * Action Update age
   */
  private class ActionUpdate extends Action2 {
    
    /**
     * Constructor
     */
    private ActionUpdate() {
      setImage(PropertyAge.IMG);
      setTip(resources.getString("age.tip"));
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      choice.setText(newAge);
    }
  } //ActionUpdate

} //ProxyAge
