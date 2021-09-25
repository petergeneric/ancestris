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
package genj.report;

import genj.util.AncestrisUtils;
import genj.util.Resources;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which provides localized text-resources for a package
 * Resource files all follow these rules
 * <il>
 *  <li>reside in directory relative to class being used in e.g. ./genj/app
 *  <li>are names resources[_xy[_ab]].properties
 *  <li>are UTF-8 encoded
 *  <li>contain comment lines starting with # or // or /* 
 *  <li>contain content lines "key = value" (key cannot contain spaces)
 *  <li>values continue in the next line if the following line starts with a space (this one space is trimmed)
 *  <li>values containing newline escapes \n are transformed into newline characters
 * </il>
 */
public class ReportResources extends Resources{
    private static final Logger LOG = Logger.getLogger("ancestris.util");
  
  /** keep track of loaded resources */
  private static Map<String, ReportResources> instances = new HashMap<String, ReportResources>();

  /** the mapping key, resource  */
  private volatile Map<String, String> key2string = null;
  private List<String> keys = null;

  /** locale to use */
  private Locale locale = Locale.getDefault();
  private final static String userLanguage = Locale.getDefault().getLanguage();

  /** cached message formats */
  private WeakHashMap<String, MessageFormat> msgFormats = new WeakHashMap<String, MessageFormat>();

  
  /**
   * Constructor for empty resources
   */
  public ReportResources() {
      super();
      key2string = null;
      keys = null;
  }
  
  /**
   * Constructor for resources from explicit input stream
   */
  public ReportResources(InputStream in, Locale locale) {
    this();
    if (locale == null)
        locale = Locale.getDefault();
    this.locale = locale;
    
    if (in!=null) try {
      key2string = new HashMap<>();
      keys = new ArrayList<>(1000);
      load(in);
    } catch (IOException e) {
      Logger.getLogger("ancestris.util").log(Level.FINE, "can't read resources", e);
      key2string = null;
      keys = null;
    }
  }

  /**
   * Load more resources from stream
   */
  private void load(InputStream in) throws IOException {
    load(in, keys, key2string, false);
  }
  
  public void load(InputStream in, boolean literate) throws IOException {
    load(in, keys, key2string, literate);
  }
  
  private static String trim(String s) {
    
    // take off whitespace or * in front
    int start = 0;
    for (int len=s.length(); start<len; start++) {
      char c = s.charAt(start);
      if ('*'!=c && !Character.isWhitespace(c))
        break;
    }
    return start==0 ? s : s.substring(start);
  }
  
  /**
   * Loads key/value pairs from inputstream with unicode content
   */
  private static void load(InputStream in, List<String> keys, Map<String,String> key2string, boolean literate) throws IOException {
    
    if (in==null)
      throw new IOException("can't load resources from null");
    
    try {
      BufferedReader lines = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      
      // loop over all lines
      String key, val, last = null;
      int indent = 0;
      boolean comment = false;
      while (true) {
        
        // next line
        String line = lines.readLine();
        if (line==null) 
          break;
        
        // literate mode means we only look at comments
        if (literate) {
          
          if (comment) {
            int close = line.lastIndexOf("*/");
            if (close>0) {
              comment = false;
              line = line.substring(close+2);
            }
          } else {
            int open = line.indexOf("/*");
            if (open<0)
              continue;
            comment = true;
            line = line.substring(open+2).trim();
          }        
        }
        
        // empty line stops continuation
        String trimmed = trim(line);
        if (trimmed.length()==0) {
          last = null;
          continue;
        }
        
        // .. continuation as follows:
        if (last!=null) {
          // +... -> newline....
          if (trimmed.charAt(0)=='+') {
            key2string.put(last, key2string.get(last)+"\n"+breakify(trimmed.substring(1)));
            continue;
          }
          // &... -> ....
          if (trimmed.charAt(0)=='&') {
            key2string.put(last, key2string.get(last)+breakify(trimmed.substring(1)));
            continue;
          }
          // \ssomething -> ....
          if (line.indexOf(trimmed)>indent) {
            String appendto = key2string.get(last);
            if (!(appendto.endsWith(" ")||appendto.endsWith("\n"))) appendto += " ";
            key2string.put(last, appendto + breakify(trimmed));
            continue;
          }
        } 
          
        // break down key and value
        int i = trimmed.indexOf('=');
        if (i<=0) 
          continue;
        key = trimmed.substring(0, i).trim();
        if (!literate&&!Character.isJavaIdentifierStart(line.charAt(0)))
          continue;
        val = trim(trimmed.substring(i+1));
        keys.add(key);
        
        // remember (we keep lowercase keys in map)
        key = key.toLowerCase();
        key2string.put(key, breakify(val));
        
        // next
        last = key;
        indent =  line.indexOf(trimmed);
      }

    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }
  }

  private static String breakify(String string) {
      return  AncestrisUtils.unescape(string);
  }
  
  /**
   * Checks for given key
   */
  public boolean contains(String key) {
    return getString(key, false) != null;
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param notNull will return key if resource is not defined
   */
    @Override
    public String getString(String key, boolean notNull) {
        if (key2string == null) {
            return super.getString(key, notNull);
        }

        // look it up in language
        String result = null;
        String lang = locale != null ? locale.getLanguage() : userLanguage;
        if (lang != null) {
            String locKey = key + '.' + lang;
            result = key2string.get(locKey.toLowerCase());
        }
        // fallback if necessary
        if (result == null) {
            result = key2string.get(key.toLowerCase());
        }

        if (result == null && notNull) {
            result = key;
        }
        return result;
    }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   */
    @Override
  public String getString(String key) {
    return getString(key, true);
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
    @Override
  public String getString(String key, Object... substitutes) {
      if (key2string == null){
          return super.getString(key, substitutes);
      }
    // do we have a message format already?
    MessageFormat format = msgFormats.get(key);
    if (format==null) {
      String string = getString(key, false);
      if (string==null)
        return key;
      format = getMessageFormat(string);
      msgFormats.put(key, format);
    }

    // fill with substitutes
    return format.format(substitutes);
  }
  
  /**
   * Generate a MessageFormat for given pattern
   */
  private static MessageFormat getMessageFormat(String pattern) {
    // have to patch single quotes to doubles because
    // MessageFormat doesn't like those 
    if (pattern.indexOf('\'')>=0) {
      StringBuffer buffer = new StringBuffer(pattern.length()+8);
      for (int i=0,j=pattern.length();i<j;i++) {
        char c = pattern.charAt(i);
        buffer.append(c);
        if (c=='\'') buffer.append('\'');
      }
      pattern = buffer.toString();
    }
    // got it
    return new MessageFormat(pattern);
  }

} //Resources
