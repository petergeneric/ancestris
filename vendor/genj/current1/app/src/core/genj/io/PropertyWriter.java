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
package genj.io;

import genj.gedcom.Entity;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Decodes a property and all its sub-properties into lines of tag & values
 */
public class PropertyWriter {
  
  boolean useIndents = false;
  private int lines = 0;
  private BufferedWriter out;

  /**
   * constructor
   */  
  public PropertyWriter(Writer out, boolean useIndents) {
    this.out = new BufferedWriter(out);
    this.useIndents = useIndents;
  }
  
  /**
   * decode a root property
   */
  public int write(int level, Property prop) throws IOException {
    
    writeProperty(level, prop);
    out.flush();
    
    // done
    return lines;
  }
  
  /** no of lines written */
  public int getLines() {
    return lines;
  }
  
  /**
   * overridable decode value
   */
  protected String getValue(Property prop) throws IOException {
    return prop.getValue();
  }
  
  /**
   * decode property
   */
  protected void writeProperty(int level, Property prop) throws IOException {
    
    // skip transients
    if (prop.isTransient())
      return;
    
    // multiline or one-line?
    if (prop instanceof MultiLineProperty)
      writeMultiLine(level, prop);
    else
      writeLine(level, getTag(prop), getValue(prop));

    // sub properties
    int num = prop.getNoOfProperties();
    for (int i = 0; i < num; i++) 
      writeProperty(level+1, prop.getProperty(i));
    
    
    // done
  }
  
  /**
   * decode a property tag
   */
  protected String getTag(Property prop) {
    
    // add xref for entity itself (level==0)
    if (prop instanceof Entity) {
      // xref might be missing in case of custom records!
      String xref = ((Entity)prop).getId();
      if (xref.length()>0) 
        return '@'+xref+"@ "+prop.getTag();
    }
    
    // normal
    return prop.getTag();
  }
  
  /**
   * decode a multi-line property
   */
  private void writeMultiLine(int level, Property prop) throws IOException {
    
    // prep an iterator to loop through lines in this property
    MultiLineProperty.Iterator lines = ((MultiLineProperty)prop).getLineIterator();
    lines.setValue(getValue(prop));
    
    // loop it
    writeLine(level + lines.getIndent(), getTag(prop), lines.getValue());
    while (lines.next()) {
      writeLine(level + lines.getIndent(), lines.getTag(), lines.getValue());
    }
    
    // done
  }
  
  /**
   * decode a resulting line
   */
  private void writeLine(int level, String tag, String value) throws IOException {

    // Level
    if (useIndents) {
      for (int i=0;i<level;i++)
        out.write(' ');
    } else {
        out.write(Integer.toString(level));
        out.write(' ');
    }
    
    // Tag
    out.write(tag);

    // Value
    if (value!=null&&value.length()>0) {
      // 20030715 only write separating ' ' if value.length()>0 
      out.write(' ');
      out.write(value);
    }
    out.newLine();

    // one down
    lines++;

  }
      
} //PropertyDecoder
