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
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A proxy for a property that links entities
 */
public class XRefBean extends PropertyBean {

  private Preview preview;
  private PropertyXRef xref;
  
  void initialize(Registry setRegistry) {
    super.initialize(setRegistry);
    
    preview = new Preview();
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, preview);
    
    preview.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        // no double-click?
        if (e.getClickCount()<2)
          return;
        // property good? (should)
        if (xref==null)
          return;
        // tell about it
        WindowManager.broadcast(new ContextSelectionEvent(new ViewContext(xref), preview, true));
      }
    });
  }
  
  
  
  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }
  
  /**
   * Set context to edit
   */
  boolean accepts(Property prop) {
    return prop instanceof PropertyXRef;
  }
  public void setPropertyImpl(Property prop) {
    
    PropertyXRef xref = (PropertyXRef)prop;
    this.xref = xref;
    
    // set preview
    if (xref!=null&&xref.getTargetEntity()!=null) 
      preview.setEntity(xref.getTargetEntity());
    else
      preview.setEntity(null);
  }
  
  /**
   * Preferred
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,48);
  }

    
} //ProxyXRef
