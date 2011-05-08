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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import genj.window.WindowManager;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class NameBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col><row><l/><v wx=\"1\"/></row><row><l/><v wx=\"1\"/><check/></row><row><l/><v wx=\"1\"/></row></col>");
  
  /** our components */
  private Property[] sameLastNames;
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
    return resources.getString("choice.global.confirm", new String[]{ ""+sameLastNames.length, ((PropertyName)getProperty()).getLastName(), cLast.getText()});
  }
  
  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
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
    tSuff.addChangeListener(changeSupport);

    cAll = new JCheckBox();
    cAll.setBorder(new EmptyBorder(1,1,1,1));
    cAll.setVisible(false);
    cAll.setRequestFocusEnabled(false);
    
    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(cFirst);

    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast);
    add(cAll);

    add(new JLabel(PropertyName.getLabelForSuffix()));
    add(tSuff);

    // listen to selection of global and ask for confirmation
    cAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String msg = getReplaceAllMsg();
        WindowManager wm = WindowManager.getInstance(NameBean.this);
        if (wm!=null&&msg!=null&&cAll.isSelected()) {
          int rc = wm.openDialog(null, resources.getString("choice.global.enable"), WindowManager.QUESTION_MESSAGE, msg, Action2.yesNo(), NameBean.this);
          cAll.setSelected(rc==0);
        }        
      }
    });
    
    
    // we're done aside from declaring the default focus
    defaultFocus = cFirst;

  }

  /**
   * Finish editing a property through proxy
   */
  public void commit(Property property) {

    super.commit(property);
    
    // ... calc texts
    String first = cFirst.getText().trim();
    String last  = cLast .getText().trim();
    String suff  = tSuff .getText().trim();

    // ... store changed value
    PropertyName p = (PropertyName) property;
    p.setName( first, last, suff, cAll.isSelected());

    // Done
  }

  /**
   * Set context to edit
   */
  boolean accepts(Property prop) {
    return prop instanceof PropertyName;
  }
  public void setPropertyImpl(Property prop) {
    PropertyName name = (PropertyName)prop;
    if (name==null)
      return;
    
    // keep track of who has the same last name
    sameLastNames = name.getSameLastNames();
    
    // first, last, suff
    cLast.setValues(name.getLastNames(true));
    cLast.setText(name.getLastName());
    cFirst.setValues(name.getFirstNames(true));
    cFirst.setText(name.getFirstName()); 
    tSuff.setText(name.getSuffix()); 
    
    cAll.setVisible(false);
    cAll.setSelected(false);

    // done
  }

} //ProxyName
