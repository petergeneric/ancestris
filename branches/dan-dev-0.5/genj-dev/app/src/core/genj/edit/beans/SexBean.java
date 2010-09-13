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
import genj.gedcom.PropertySex;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
public class SexBean extends PropertyBean {

  /** members */
  private JRadioButton male = new JRadioButton(PropertySex.getLabelForSex(PropertySex.MALE));
  private JRadioButton female = new JRadioButton(PropertySex.getLabelForSex(PropertySex.FEMALE));
  private JRadioButton last;
  private ButtonGroup group = new ButtonGroup();
  
  /**
   * Finish editing a property through proxy
   */
  @Override
  protected void commitImpl(Property property) {
    PropertySex sex = (PropertySex)property; 
    sex.setSex(getSex());
  }
  
  public SexBean() {
    
    // use our layout
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
    add(male);
    add(female);
    
    String tip = RESOURCES.getString("sex.tip");
    male.setToolTipText(tip);
    female.setToolTipText(tip);
    
    group = new ButtonGroup();
    group.add(male);
    group.add(female);

    ActionHandler handler = new ActionHandler();
    male.addActionListener(handler);
    female.addActionListener(handler);

    // Done
  }
  
  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }
  
  private class ActionHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if ( (e.getModifiers()&ActionEvent.CTRL_MASK)!=0 ) {
        group.clearSelection();
        last = null;
      } else {
        if (last==e.getSource()) {
          last = last==male ? female : male;
          last.setSelected(true);
        } else {
          last = (JRadioButton)e.getSource();
        }
        if (getProperty()!=null&&getSex()==((PropertySex)getProperty()).getSex())
          return;
      }
      changeSupport.fireChangeEvent();
    }
  }
  
  /**
   * Get current sex
   */
  private int getSex() {

    if (male.isSelected())
      return PropertySex.MALE;
    if (female.isSelected())
      return PropertySex.FEMALE;
    return PropertySex.UNKNOWN;
        
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    // show it
    last = null;
    defaultFocus = male;
    group.clearSelection();
    
    PropertySex sex = (PropertySex)prop;
    if (sex!=null) 
      switch (sex.getSex()) {
        case PropertySex.MALE:
          male.doClick();
          defaultFocus = male;
          break;
        case PropertySex.FEMALE:
          female.doClick();
          defaultFocus = female;
          break;
      }

    // Done
  }

} //ProxySex

