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
package genj.search;

import genj.gedcom.Property;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A search hit
 */
/*package*/ class Hit {
  
  private final static SimpleAttributeSet 
    RED  = new SimpleAttributeSet(),
    BOLD = new SimpleAttributeSet();
  
  static {
    StyleConstants.setForeground(RED, Color.RED);
    StyleConstants.setBold(BOLD, true);
  }

  /** the property */
  private Property property;
  
  /** an image (cached) */
  private ImageIcon img; 
  
  /** a document (cached) */
  private StyledDocument doc;
  
  /** n-th entity  */
  private int entity;

  /** 
   * Constructor
   */
  /*package*/ Hit(Property setProp, String value, Matcher.Match[] matches, int setEntity, boolean isID) {
    // keep property
    property = setProp;
    // cache img
    img = property.getImage(false);
    // keep sequence
    entity = setEntity;
    // prepare document
    doc = new DefaultStyledDocument();
    try {
      int offset = 0;
      String tag = setProp.getTag();
      // indent
      doc.insertString(offset++, " ", null);
      // tag first for values and not IDs
      if (!isID) {
	      doc.insertString(offset, tag, BOLD);
	      offset += tag.length();
	      doc.insertString(offset++, " ", null);
      }
      // keep value and format for matches
      doc.insertString(offset, value, null);
      for (int i=0;i<matches.length;i++) {
        Matcher.Match m = matches[i];
        doc.setCharacterAttributes(offset+m.pos, m.len, RED, false);
      }
      offset += value.length();
      // tag last for IDs
      if (isID) {
	      doc.insertString(offset++, " ", null);
        doc.insertString(offset, tag, BOLD);
      }
      // keep image
      SimpleAttributeSet img = new SimpleAttributeSet();
      StyleConstants.setIcon(img, setProp.getImage(false));
      doc.insertString(0, " ", img);
    } catch (Throwable t) {
    }
    // done
  }
  
  /**
   * Document
   */
  /*package*/ StyledDocument getDocument() {
    return doc;
  }
  
  /**
   * Property
   */
  /*package*/ Property getProperty() {
    return property;
  }
  
  /**
   * Image
   */
  /*package*/ ImageIcon getImage() {
    return img;
  }
  
  /**
   * n-th entity
   */
  /*package*/ int getEntity() {
    return entity;
  }
  
} //Hit
