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

import genj.gedcom.GedcomException;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Reads gedcom lines into properties
 */
public class PropertyReader {

  protected final static Resources RESOURCES = Resources.get("genj.io");

  protected boolean useIndents = false;
  protected int lines = 0;
  protected String line = null;
  protected Collection collectXRefs;
  protected boolean isMerge = false;
  
  /** variables read line by line */
  protected int level;
  protected String tag;
  protected String xref;
  protected String value;
  
  /** input */
  protected BufferedReader in;
  
  /** 
   * Constructor 
   * @param in reader to read from
   * @param collectXRefs collection to collect xrefs in (otherwise xrefs are linked immediately)
   * @param useIndents whether to use spaces as indent declarations
   */
  public PropertyReader(Reader in, Collection collectXRefs, boolean useIndents) {
    this(new BufferedReader(in), collectXRefs, useIndents);
  }
  
  /** 
   * Constructor 
   * @see PropertyReader#PropertyReader(Reader, Collection, boolean)
   */
  public PropertyReader(BufferedReader in, Collection collectXRefs, boolean useIndents) {
    this.in = in;
    this.useIndents = useIndents;
    this.collectXRefs = collectXRefs;
  }
  
  /** lines read */
  public int getLines() {
    return lines;
  }
  
  /**
   * read into property
   */
  public void read(Property prop) throws IOException {
    read(prop, -1);
  }
  
  /**
   * read into property
   */
  public void read(Property prop, int index) throws IOException {
    // do the recursive read
    readProperties(prop, 0, index);
    // put back a pending line? this works only if the constructor received a buffered reader
    if (line!=null) {
      line = null;
      in.reset();
    }
    // done
  }
  
  /**
   * Whether to merge properties encountered during read
   */
  public void setMerge(boolean set) {
    isMerge = set;
  }
  
  /**
   * read recursively while lines available
   */
  protected void readProperties(Property prop, int currentLevel, int pos) throws IOException {
    
    // try to read some multilines first?
    if (prop instanceof MultiLineProperty) {
      // run through collector
      MultiLineProperty.Collector collector = ((MultiLineProperty)prop).getLineCollector();
      while (true) {
        // check next line
        if (!readLine(false))
          break;
        // collect as far as we can
        if (level<currentLevel+1 || !collector.append(level-currentLevel, tag, value))
          break;
        // consume it
        line = null;
        // next line
      } 
      // commit collected value
      prop.setValue(collector.getValue());
    }
  
    // loop over subs
    while (true) {
      
      // read next &parse
      if (!readLine(false))
        return;
      
      // wrong level now?
      if (level<currentLevel+1) 
        return;
      
      // consume it
      line = null;
      
      // check for wrong level value
      //  0 INDI
      //  1 BIRT
      //  3 DATE
      // we simply spit out a warning and continue as if nothing happened
      if (level>currentLevel+1) {
        trackBadLevel(level, prop);
        while (level-1>currentLevel++) 
          prop = prop.addProperty("_TAG", "");
      }
    
      // remember current line
      int lineNoForChild = lines;

      // add sub property
      Property child = addProperty(prop, tag, value, pos);
      
      // first recurse into child(ren)
      readProperties(child, currentLevel+1, 0);
        
      // 20060406 now link after children are setup - this makes a difference for
      // e.g. link() in case of ASSO that looks at RELA
      if (child instanceof PropertyXRef)
        link((PropertyXRef)child, lineNoForChild);
        
      // next line
      if (pos>=0) pos++;
    }
    
    // done
  }

  /** add a child property **/
  protected Property addProperty(Property prop, String tag, String value, int pos) {
    if (isMerge) {
      // reuse prop's existing child with same tag if singleton
      Property child = prop.getProperty(tag, false);
      if (child!=null&&prop.getMetaProperty().getNested(tag, false).isSingleton()&&!(child instanceof PropertyXRef)) {
        child.setValue(value);
        return child;
      }
    }
    
    try {
      return prop.addProperty(tag, value, pos);
    } catch (GedcomException e) {
      Property fallback = prop.addSimpleProperty(tag, value, pos);
      trackBadProperty(fallback, e.getMessage());
      return fallback;
    }
    
  }
  
  /**
   * read a line
   */
  protected boolean readLine(boolean consume) throws IOException {
    
    // need a line?
    if (line==null) {
      
      // mark current position in reader
      in.mark(256);
      
      // grab it ignoring empty lines
      while (line==null) {
        line = in.readLine();
        if (line==null) 
          return false;
        lines ++;
        if (line.trim().length()==0) {
          trackEmptyLine();
          line = null;
        }
      }
      
      // 20040322 use space and also \t for delim in case someone used tabs in file
      StringTokenizer tokens = new StringTokenizer(line," \t");
  
      try {
        
        // .. caclulate level by looking at spaces or parsing a number
        try {
          if (useIndents) {
            level = 0;
            while (line.charAt(level)==' ') level++;
            level++;
          } else {
            level = Integer.parseInt(tokens.nextToken(),10);
          }
        } catch (StringIndexOutOfBoundsException sioobe) {
          throw new GedcomFormatException(RESOURCES.getString("read.error.emptyline"), lines);
        } catch (NumberFormatException nfe) {
          throw new GedcomFormatException(RESOURCES.getString("read.error.nonumber"), lines);
        }
  
        // .. tag (?)
        if (tokens.hasMoreTokens()) 
          tag = tokens.nextToken();
        else {
          tag = "_TAG";
        }
          
        // .. xref ?
        if (level==0&&tag.startsWith("@")) {
  
          // .. valid ?
          if (!tag.endsWith("@")||tag.length()<=2)
            throw new GedcomFormatException(RESOURCES.getString("read.error.invalidid"), lines);
   
          // .. indeed, xref !
          xref = tag.substring(1,tag.length()-1);
          
          // .. tag is the next token
          tag = tokens.nextToken();
  
        } else {
  
          // .. no reference in line !
          xref = "";
        }
  
        // .. value
        if (tokens.hasMoreElements()) {
          // 20030530 o.k. gotta switch to delim "\n" because we want everything 
          // to end of line including contained spaces 
          value = tokens.nextToken("\n");
          // 20030609 strip leading space that forms delimiter to tag/xref
          // (this was trim() once but identified as too greedy)
          if (value.startsWith(" "))
            value = value.substring(1);
        } else {
          value = "";
        }
  
      } catch (NoSuchElementException ex) {
        // .. not enough tokens
        throw new GedcomFormatException(RESOURCES.getString("read.error.cantparse"), lines);
      }
      
      // TUNING: for tags we expect a lot of repeating strings (limited numbe of tags) so
      // we build the intern representation of tag here - this makes us share string 
      // instances for an upfront cost
      tag = tag.intern();
          
    }
    
    // consume it already?
    if (consume)
      line = null;
      
    // we're ready
    return true;
  }
  
  /** link a reference - keep in lazyXRefs is available otherwise link and ignore errors */
  protected void link(PropertyXRef xref, int line) {
    if (collectXRefs!=null)
      collectXRefs.add(xref);
    else try {
      xref.link();
    } catch (Throwable t) {
      // ignored
    }
  }
  
  /** track an empty line - default noop */
  protected void trackEmptyLine() {
  }
  
  /** track a bad level - default noop */
  protected void trackBadLevel(int level, Property parent) {
  }
  
  /** track a bad property - default noop */
  protected void trackBadProperty(Property property, String message) {
  }
  
} //PropertyDecoder
