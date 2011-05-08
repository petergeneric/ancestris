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
package genj.gedcom;

import java.util.ArrayList;

/**
 * Gedcom Property with multiple lines
 */
public class PropertyMultilineValue extends Property implements MultiLineProperty {
  
  /** the tag */
  private String tag;
  
  /** our value */
  private String lines = "";
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return tag;
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    tag = meta.getTag();
    return super.init(meta, value);
  }
  
  /**
   * Can contain newline characters since this is a mutli-line text
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String setValue) {
    String old = getValue();
    lines = setValue;
    propagatePropertyChanged(this, old);
  }
  
  /**
   * A display value containing no newlines
   */
  public String getDisplayValue() {
    return getValue();
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return lines.toString();
  }
  
  /**
   * Accessor Value as lines
   */
  public String[] getLines() {
     ArrayList result = new ArrayList();
     Iterator it = getLineIterator();
     do {
       result.add(it.getValue());
     } while (it.next());
     return (String[])result.toArray(new String[result.size()]);
  }
  
  /**
   * @see genj.gedcom.MultiLineProperty#getLineIterator()
   */
  public Iterator getLineIterator() {
    return new ConcContIterator(getTag(), lines);
  }

  /**
   * @see genj.gedcom.MultiLineProperty#getLineCollector()
   */
  public Collector getLineCollector() {
    return new ConcContCollector();
  }
  
    /**
     * An iterator for lines
     */
    private static class ConcContIterator implements Iterator {
      
      /** the tag */
      private String firstTag, currentTag, nextTag;
      
      /** the value */
      private String value;
      
      /** the current segment */
      private int start,end;
      
      /** value line break */
      private int valueLineBreak;
      
      /**
       * Constructor
       */
      /*package*/ ConcContIterator(String top, String initValue) {
        valueLineBreak = Options.getInstance().getValueLineBreak();
        firstTag = top;
        setValue(initValue);
      }
      
      /**
       * @see genj.gedcom.MultiLineProperty.Iterator#setValue(java.lang.String)
       */
      public void setValue(String setValue) {
  
        value = setValue;
  
        currentTag = firstTag;       
        nextTag = firstTag;
        start = 0;
        end = 0;
  
        next();
      }
      
      /**
       * @see genj.gedcom.MultiLineSupport.LineIterator#getIndent()
       */
      public int getIndent() {
        return currentTag == firstTag ? 0 : 1;
      }
      
      /**
       * @see genj.gedcom.MultiLineSupport.Line#getTag()
       */
      public String getTag() {
        return currentTag;
      }
      
      /**
       * @see genj.gedcom.MultiLineSupport.Line#getValue()
       */
      public String getValue() {
        return value.substring(start, end);
      }
        
      /**
       * @see genj.gedcom.MultiLineSupport.Line#next()
       */
      public boolean next() {
        
        // nothing more there?
        if (end==value.length()) 
          return false;
  
        // continue from last end
        start = end;
        
        // calc current tag      
        currentTag = nextTag;
        
        // assume taking all
        end = value.length();
        
        // skip one leading '\n' if not first
        if (currentTag!=firstTag && value.charAt(start)=='\n')
          start++;
        
        // take all up to next CR
        // 20030604 value.indexOf() used here previously is 1.4
        for (int i=start;i<end;i++) {
          if (value.charAt(i)=='\n') {
            end = i;
            nextTag = "CONT";
            break;
          }
        }
        
        // but max of valueLineBreak
        if (end-start>valueLineBreak) {
          end = start+valueLineBreak;
          nextTag = "CONC";
          
          // make sure we don't end with white-space
          while ( end<value.length() && end>start+1 && (Character.isWhitespace(value.charAt(end-1)) || Character.isWhitespace(value.charAt(end))) )
            end--;
        }
        
        // done
        return start!=value.length();
      }
      
    } //LineReader
  
  /**
   * An iterator for lines
   */
  private class ConcContCollector implements Collector {
    
    /** running collection */
    private StringBuffer buffer = new StringBuffer(lines.toString());
    
    /**
     * append some
     */
    public boolean append(int indent, String tag, String value) {
      
      // only level 1 (direct children)
      if (indent!=1)
        return false;
        
      // gotta be CONT or CONC
      boolean 
        isCont = "CONT".equals(tag),
        isConc = "CONC".equals(tag);
      if (!(isConc||isCont))
        return false;
        
      // grab it
      if (isCont) 
        buffer.append('\n');
        
      buffer.append(value);
      
      // accepted
      return true;
    }
    
    /**
     * @see genj.gedcom.MultiLineProperty.Collector#getValue()
     */
    public String getValue() {
      return buffer.toString();
    }

  } //LineWriter

} //PropertyMultilineValue
