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
import genj.gedcom.PropertyChoiceValue;
import genj.util.GridBagHelper;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A bean for editing choice properties(e.g. RELA)
 * @author nils@meiers.net
 * @author Tomas Dahlqvist fix for prefix lookup
 */
public class ChoiceBean extends PropertyBean {

  /** members */
  private ChoiceWidget choices;
  private JCheckBox global;
  private Property[] sameChoices = new Property[0];
  
  /**
   * Calculate global replace message
   */
  private String getGlobalReplaceMsg() {
    if (sameChoices.length<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return RESOURCES.getString("choice.global.confirm", ""+sameChoices.length, sameChoices[0].getDisplayValue(), choices.getText());
  }
  
  public ChoiceBean() {
    
    // prepare a choice for the user
    choices = new ChoiceWidget();
    choices.addChangeListener(changeSupport);
    choices.setIgnoreCase(true);

    // add a checkbox for global
    global = new JCheckBox();
    global.setBorder(new EmptyBorder(1,1,1,1));
    global.setVisible(false);
    global.setRequestFocusEnabled(false);
    
    // listen to changes in choice and show global checkbox if applicable
    choices.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String msg = getGlobalReplaceMsg();
        if (msg!=null) {
          global.setVisible(true);
          global.setToolTipText(msg);
        }
      }
    });
    
    // listen to selection of global and ask for confirmation
    global.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String msg = getGlobalReplaceMsg();
        if (msg!=null&&global.isSelected()) {
          int rc = DialogHelper.openDialog(RESOURCES.getString("choice.global.enable"), DialogHelper.QUESTION_MESSAGE, msg, Action2.yesNo(), ChoiceBean.this);
          global.setSelected(rc==0);
        }        
      }
    });
    
    // layout
    GridBagHelper layout = new GridBagHelper(this);
    layout.add(choices, 0, 0, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
    layout.add(global, 1, 0);
    layout.addFiller(0,1);
    
    // focus
    defaultFocus = choices;
  }
  
  /**
   * Finish editing a property through proxy
   */
  @Override
  protected void commitImpl(Property property) {
    
    PropertyChoiceValue choice = (PropertyChoiceValue)property;

    // change value
    String text = choices.getText();
    choice.setValue(text, global.isSelected());
    
    // reset
    choices.setValues(((PropertyChoiceValue)property).getChoices(true));
    choices.setText(text);
    global.setSelected(false);
    global.setVisible(false);
      
    // Done
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyChoiceValue choice = (PropertyChoiceValue)prop;

    // Note: we're using getDisplayValue() here because like in PropertyRelationship's 
    // case there might be more in the gedcom value than what we want to display 
    // e.g. witness@INDI:BIRT
    
    if (choice!=null) {
      choices.setValues(choice.getChoices(true));
      choices.setText(choice.isSecret() ? "" : choice.getDisplayValue());
      sameChoices = choice.getSameChoices();
    } else {
      choices.setValues(PropertyChoiceValue.getSameChoices(getRoot().getGedcom(), getPath().getLast(), true));
      choices.setText("");
      sameChoices = new Property[0];
    }
      
    global.setSelected(false);
    global.setVisible(false);
    
    // done
  }

} //ProxyChoice
