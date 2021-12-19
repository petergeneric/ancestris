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

import ancestris.core.CoreOptions;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class ShortNameBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout(
      "<table>"+
       "<row><l/><row><v wx=\"1\"/><check pad=\"0\"/></row></row>"+
       "<row><l/><v wx=\"1\"/></row>"+
      "</table>"
  );
  
  /** our components */
  private Property[] sameLastNames = new Property[0];
  private ChoiceWidget cLast, cFirst;
  private JCheckBox cAll;
  private TextFieldWidget tSuff;

  /**
   * Calculate message for replace all last names
   */
  private String getReplaceAllMsg() {
    if (sameLastNames.length<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return RESOURCES.getString("choice.global.confirm", ""+sameLastNames.length, ((PropertyName)getProperty()).getLastName(), cLast.getText());
  }
  
  public ShortNameBean() {
    
    setLayout(LAYOUT.copy());

    cLast  = new ChoiceWidget();
    cLast.addChangeListener(changeSupport);
    cLast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String msg = getReplaceAllMsg();
        if (msg!=null) {
          cAll.setVisible(true);
          cAll.setToolTipText(msg);
        }
      }
    });
    cLast.setIgnoreCase(true);
    cFirst = new ChoiceWidget();
    cFirst.addChangeListener(changeSupport);
    cFirst.setIgnoreCase(true);

    tSuff  = new TextFieldWidget("", 10);

    cAll = new JCheckBox();
    cAll.setBorder(new EmptyBorder(1,1,1,1));
    cAll.setVisible(false);
    cAll.setRequestFocusEnabled(false);
    
    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast);
    add(cAll);

    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(cFirst);

    // listen to selection of global and ask for confirmation
    cAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String msg = getReplaceAllMsg();
        if (msg!=null&&cAll.isSelected()) {
            boolean yes = (DialogManager.YES_OPTION == 
                    DialogManager.createYesNo(RESOURCES.getString("choice.global.enable"), msg)
                    .show());
          cAll.setSelected(yes);
        }        
      }
    });
    
    
    // we're done aside from declaring the default focus
    defaultFocus = cLast;

  }

  /**
   * Finish editing a property through proxy
   */
  @Override
  @SuppressWarnings("fallthrough")
  protected void commitImpl(Property property) {

    PropertyName p = (PropertyName) property;
    
    // ... calc texts
    String first = cFirst.getText().trim();
    String last  = cLast .getText().trim();
    String suff  = tSuff .getText().trim();
    
    Gedcom ged = p.getGedcom();
    if (ged!=null) {
      switch (CoreOptions.getInstance().getCorrectName()) {
      // John DOE
      case CoreOptions.NAME_ALLCAPS:
        last = last.toUpperCase(ged.getLocale());
        cLast.setText(last);
      // John Doe
      case CoreOptions.NAME_FIRSTCAP:
        if (first.length()>0) {
          first = Character.toString(first.charAt(0)).toUpperCase(ged.getLocale()) + first.substring(1);
          cFirst.setText(first);
        }
      }
    }

    // ... store changed value
    final String oldName = p.getLastName();
    p.setName(first, last, suff);
    if (cAll.isSelected()) {
        p.replaceAllLastNames(oldName);
    }
    
    // start fresh
    setPropertyImpl(p);

    // Done
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyName name = (PropertyName)prop;
    if (name==null) {
      sameLastNames = new Property[0];
      cLast.setValues(PropertyName.getLastNames(getRoot().getGedcom(), true));
      cLast.setText("");
      cFirst.setValues(PropertyName.getFirstNames(getRoot().getGedcom(), true));
      cFirst.setText("");
      tSuff.setText("");
    } else {
      // keep track of who has the same last name
      sameLastNames = name.getSameLastNames();
      // first, last, suff
      cLast.setValues(name.getLastNames(true));
      cLast.setText(name.getLastName());
      cFirst.setValues(name.getFirstNames(true));
      cFirst.setText(name.getFirstName()); 
      tSuff.setText(name.getSuffix()); 
    }
    
    cAll.setVisible(false);
    cAll.setSelected(false);

    // done
  }

} //ProxyName
