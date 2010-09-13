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

import genj.edit.Options;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.GridBagHelper;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
public class PlaceBean extends PropertyBean {

  private GridBagHelper gh = new GridBagHelper(this);
  private int rows = 0;
  private JCheckBox global = new JCheckBox();
  
  private Property[] sameChoices = new Property[0];


  public PlaceBean() {
    
    // nothing much we can do - hook up to change events and show changeAll on change 
    changeSupport.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String confirm = getGlobalConfirmMessage();
        global.setVisible(confirm!=null);
        global.setToolTipText(confirm);
      }
    });
    // listen to selection of global and ask for confirmation
    global.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (global.isSelected()) {
          int rc = DialogHelper.openDialog(RESOURCES.getString("choice.global.enable"), DialogHelper.QUESTION_MESSAGE, getGlobalConfirmMessage(), Action2.yesNo(),PlaceBean.this);
          global.setSelected(rc==0);
        }        
      }
    });
    
  }
  
  /**
   * Compute commit value
   */
  private String getCommitValue() {
    
    boolean hierarchy = Options.getInstance().isSplitJurisdictions && ((PropertyPlace)getProperty()).getFormatAsString().length()>0;
    
    // collect the result by looking at all of the choices
    StringBuffer result = new StringBuffer();
    for (int c=0, n=getComponentCount(), j=0; c<n; c++) {
      
      // check each text field
      Component comp = getComponent(c);
      if (comp instanceof ChoiceWidget) {
        
        String jurisdiction = ((ChoiceWidget)comp).getText().trim();
        
        // make sure the user doesn't enter a comma ',' if there is a field per jurisdiction
        if (hierarchy) jurisdiction = jurisdiction.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, ";"); 
          
        // always add separator for jurisdictions j>0 regardless of jurisdiction.length()
        if (j++>0)  result.append(PropertyPlace.JURISDICTION_SEPARATOR); 
        result.append(jurisdiction);
        
      }
      // next
    }

    return result.toString();
  }
  
  /**
   * Finish editing a property through proxy
   */
  @Override
  protected void commitImpl(Property property) {
    
    // propagate change
    ((PropertyPlace)property).setValue(getCommitValue(), global.isSelected());
    
    // reset
    setPropertyImpl(property);
    
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    // remove all current fields and clear current default focus - this is all dynamic for each context
    int old = rows;
    rows = 0;
    defaultFocus = null;
    
    Gedcom ged = getRoot().getGedcom();
    PropertyPlace place = (PropertyPlace)prop;
    String value;
    String formatAsString;
    String[] jurisdictions;
    
    if (place==null) {
      sameChoices = new Property[0];
      value = "";
      jurisdictions = new String[0];
      formatAsString = ged.getPlaceFormat();
    } else {
      sameChoices = place.getSameChoices();
      /*
        thought about using getDisplayValue() here but the problem is that getAllJurisdictions()
        works on values (PropertyChoiceValue stuff) - se we have to use getValue() here
       */
      value = place.isSecret() ? "" : place.getValue();
      formatAsString = place.getFormatAsString();
      jurisdictions = place.getJurisdictions();
    }
   
    // either a simple value or broken down into comma separated jurisdictions
    if (!Options.getInstance().isSplitJurisdictions || formatAsString.length()==0) {
      createChoice(null, value, PropertyPlace.getAllJurisdictions(ged, -1, true), formatAsString);
    } else {
      String[] format = PropertyPlace.getFormat(ged);
      for (int i=0;i<Math.max(format.length, jurisdictions.length); i++) {
        createChoice(i<format.length ? format[i] : "?", i<jurisdictions.length ? jurisdictions[i] : "", PropertyPlace.getAllJurisdictions(ged, i, true), null);
      }
    }
    
    // add 'change all'
    global.setVisible(false);
    global.setSelected(false);
    gh.add(global, 2, rows);
    
    // add filler
    gh.addFiller(1,++rows);
    
    // remove leftovers now - this means a focus change occurs back to first available field within bean
    for (int i=0;i<old;i++)
      remove(0);
    
    // Done
  }
  
  private void createChoice(String label, String value, String[] values, String tip) {
    // next row
    rows++;
    // add a label for the jurisdiction name?
    if (label!=null) 
      gh.add(new JLabel(label, SwingConstants.RIGHT), 0, rows, 1, 1, GridBagHelper.FILL_HORIZONTAL);
    // and a textfield
    ChoiceWidget choice = new ChoiceWidget();
    choice.setIgnoreCase(true);
    choice.setEditable(true);
    choice.setValues(values);
//    if (value.length()>0) {
      choice.setText(value);
//    } else {
//      choice.setText("["+Gedcom.getName(PropertyPlace.TAG)+"]");
//      choice.setTemplate(true);
//    }
    choice.addChangeListener(changeSupport);
    if (tip!=null&&tip.length()>0)
      choice.setToolTipText(tip);
    gh.add(choice, 1, rows, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
    // set default focus if not done yet
    if (defaultFocus==null) defaultFocus = choice;
    // done
  }

  /**
   * Create confirm message for global
   */
  private String getGlobalConfirmMessage() {
    if (sameChoices.length<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return RESOURCES.getString("choice.global.confirm", ""+sameChoices.length, sameChoices[0].getDisplayValue(), getCommitValue() );
  }
  
} //PlaceBean
