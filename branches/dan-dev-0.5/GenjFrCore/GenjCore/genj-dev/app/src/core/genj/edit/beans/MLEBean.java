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
import genj.util.swing.TextAreaWidget;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class MLEBean extends PropertyBean {

  /** members */
  private TextAreaWidget tarea;

  public MLEBean() {
    
    tarea = new TextAreaWidget("",3,20);
    tarea.addChangeListener(changeSupport);
    tarea.setLineWrap(true);
    tarea.setWrapStyleWord(true);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(tarea));
    
    defaultFocus = tarea;

  }
  
  /**
   * Finish editing a property through proxy
   */
  @Override
  protected void commitImpl(Property property) {
    property.setValue(tarea.getText());
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property property) {
    tarea.setText(property!=null ? property.getValue() : "");
  }

} //MLEBean
