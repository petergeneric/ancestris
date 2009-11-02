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
import genj.gedcom.PropertyDate;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.DateWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.util.swing.TextFieldWidget;

import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A bean for editing DATEs
 */
public class DateBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col><row><a/><b/></row><row><c/><d/></row><row><e wx=\"0.1\"/></row></col>");

  private final static ImageIcon PIT = new ImageIcon(PropertyBean.class, "/genj/gedcom/images/Time");
  
  /** members */
  private PropertyDate.Format format; 
  private DateWidget date1, date2;
  private PopupWidget choose;
  private JLabel label2;
  private TextFieldWidget phrase;

  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    // setup Laout
    setLayout(LAYOUT.copy());
    
    // prepare format change actions
    ArrayList actions = new ArrayList(10);
    for (int i=0;i<PropertyDate.FORMATS.length;i++)
      actions.add(new ChangeFormat(PropertyDate.FORMATS[i]));

    // .. the chooser (making sure the preferred size is pre-computed to fit-it-all)
    choose = new PopupWidget(null, null, actions);
    add(choose);
    
    // .. first date
    date1 = new DateWidget();
    date1.addChangeListener(changeSupport);
    add(date1);

    // .. second date
    label2 = new JLabel();
    add(label2);
    
    date2 = new DateWidget();
    date2.addChangeListener(changeSupport);
    add(date2);
    
    // phrase
    phrase = new TextFieldWidget();
    phrase.addChangeListener(changeSupport);
    add(phrase);
    
    // setup default focus
    defaultFocus = date1;

    // Done
  }
  
  /**
   * Finish proxying edit for property Date
   */
  public void commit(Property property) {

    super.commit(property);
    
    PropertyDate p = (PropertyDate)property;
    
    p.setValue(format, date1.getValue(), date2.getValue(), phrase.getText());

    // Done
  }

  /**
   * Setup format
   */
  private void setFormat(PropertyDate.Format set) {

    // already?
    if (format==set)
      return;
    
    changeSupport.fireChangeEvent();

    // remember
    format = set;

    // prepare chooser with 1st prefix
    choose.setToolTipText(format.getName());
    String prefix1= format.getPrefix1Name();
    choose.setIcon(prefix1==null ? PIT : null);
    choose.setText(prefix1==null ? "" : prefix1);
    
    // check label2/date2 visibility
    if (format.isRange()) {
      date2.setVisible(true);
      label2.setVisible(true);
      label2.setText(format.getPrefix2Name());
    } else {
      date2.setVisible(false);
      label2.setVisible(false);
    }
    
    // check phrase visibility
    phrase.setVisible(format.usesPhrase());

    // show
    revalidate();
    repaint();
  }          
  

  /**
   * Set context to edit
   */
  boolean accepts(Property prop) {
    return prop instanceof PropertyDate;
  }
  public void setPropertyImpl(Property prop) {

    if (prop==null)
      return;
    PropertyDate date = (PropertyDate)prop;
    
    // connect
    date1.setValue(date.getStart());
    date2.setValue(date.getEnd());
    phrase.setText(date.getPhrase());
    setFormat(date.getFormat());
    
    // done
  }
  
  /**
   * Action for format change
   */
  private class ChangeFormat extends Action2 {
    
    private PropertyDate.Format formatToSet;
    
    private ChangeFormat(PropertyDate.Format set) {
      formatToSet = set;
      super.setText(set.getName());
    }
    
    protected void execute() {
      setFormat(formatToSet);
      date1.requestFocusInWindow();
    }
    
  } //ChangeFormat 

} //ProxyDate
