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
package genj.util;

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
public class Resources {
  
  /** keep track of loaded resources */
  private static Map instances = new HashMap();

  /** the mapping key, resource  */
  private Map key2string;
  private List keys;

  /** the package name this resource is for */
  private String pkg;

  /** cached message formats */
  private WeakHashMap msgFormats = new WeakHashMap();
  
  /**
   * Constructor for resources from explicit input stream
   */
  public Resources(InputStream in) {
    
    key2string = new HashMap();
    keys = new ArrayList(1000);
    
    try {
      load(in);
    } catch (IOException e) {
      // swallow
    }
  }
  
  /**
   * Accessor (cached) 
   */
  public static Resources get(Object packgeMember) {
    return get(calcPackage(packgeMember));
  }

  /**
   * Accessor  (cached)
   */
  public static Resources get(String packge) {
    synchronized (instances) {
      Resources result = (Resources)instances.get(packge);
      if (result==null) {
        result = new Resources(packge);
        instances.put(packge, result);
      }
      return result;
    }
  }
  
  /**
   * Calc package for instance
   */
  private static String calcPackage(Object object) {
    Class clazz = object instanceof Class ? (Class)object : object.getClass();
    String name = clazz.getName();
    int last = name.lastIndexOf('.');
    return last<0 ? "" : name.substring(0, last);
  }
  
  /**
   * Calc file for package (package/resources.properties)
   */
  private String calcFile(String pkg, String lang, String country) {

    // dots in package name become slashs - /pkg/sub/resources
    String file = '/'+pkg.replace('.','/')+"/resources";
    
    // add language and country '/resources[_ll[_CC]].properties' 
    if (lang!=null) {
      file += '_'+lang;
      if (country!=null) {
        file += '_'+country;
      }
    }
    
    return file+".properties";   
  }

  /**
   * Constructor
   */
  private Resources(String pkg) {
    // remember
    this.pkg=pkg;
  }
  
  /**
   * Load more resources from stream
   */
  public void load(InputStream in) throws IOException {
    load(in, keys, key2string);
  }
  
  private static String trimLeft(String s) {
    int pos = 0;
    for (int len=s.length(); pos<len; pos++) {
      if (!Character.isWhitespace(s.charAt(pos)))
        break;
    }
    return pos==0 ? s : s.substring(pos);
  }
  
  /**
   * Loads key/value pairs from inputstream with unicode content
   */
  private static void load(InputStream in, List keys, Map key2string) throws IOException {
    
    if (in==null)
      throw new IOException("can't load resources from null");
    
    try {
      BufferedReader lines = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      // loop over all lines
      String key, val, last = null;
      while (true) {
        // next line
        String line = lines.readLine();
        if (line==null) 
          break;
        String trimmed = trimLeft(line);
        if (trimmed.length()==0)
          continue;
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
          // \s... -> ....
          if (line.charAt(0)==' ') {
            String appendto = (String)key2string.get(last);
            if (!(appendto.endsWith(" ")||appendto.endsWith("\n"))) appendto += " ";
            key2string.put(last, appendto + breakify(trimmed));
            continue;
          }
        } 
          
        // has to start with non-space
        if (!Character.isLetter(line.charAt(0)))
          continue;
        
        // break down key and value
        int i = trimmed.indexOf('=');
        if (i<0) 
          continue;
        key = trimmed.substring(0, i).trim();
        if (key.indexOf(' ')>0)
          continue;
        val = trimLeft(trimmed.substring(i+1));
        keys.add(key);
        
        // remember (we keep lowercase keys in map)
        key = key.toLowerCase();
        key2string.put(key, breakify(val));
        
        // next
        last = key;
        
      }

    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }
  }

  private static String breakify(String string) {
    while (true) {
      int i = string.indexOf("\\n");
      if (i<0) break;
      string = string.substring(0,i) + '\n' + string.substring(i+2);
    }
    return string;
  }
  
  /**
   * Lazy getter for resource map
   */
  private Map getKey2String() {
    
    // easy if already initialized - outside synchronization
    if (key2string!=null)
      return key2string;
    
    // synchronize loading - everyone will wait for this one
    synchronized (this) {
      
      // check again
      if (key2string!=null)
        return key2string;
      
      // load resources for current locale now
      Locale locale = Locale.getDefault();
      Map tmpKey2Val = new HashMap();    
      List tmpKeys = new ArrayList(100);

      // loading english first (primary language)
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, null, null)), tmpKeys, tmpKey2Val);
      } catch (Throwable t) {
      }
      
      // trying to load language specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), null)), tmpKeys, tmpKey2Val);
      } catch (Throwable t) {
      }
  
      // trying to load language and country specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), locale.getCountry())), tmpKeys, tmpKey2Val);
      } catch (Throwable t) {
      }

      // remember
      key2string = tmpKey2Val;
      keys = tmpKeys;
    }
    
    // done
    return key2string;
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
  public String getString(String key, boolean notNull) {
    String result = (String)getKey2String().get(key.toLowerCase());
    if (result==null&&notNull) result = key;
    return result;
  }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   */
  public String getString(String key) {
    return getString(key, true);
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
  public String getString(String key, Object substitute) {
    return getString(key, new Object[]{ substitute });
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
  public String getString(String key, Object[] substitutes) {
    return getString(key, substitutes, true);
  }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
  public String getString(String key, Object[] substitutes, boolean notNull) {

    // do we have a message format already?
    MessageFormat format = (MessageFormat)msgFormats.get(key);
    if (format==null) {
      String string = getString(key, false);
      if (string==null)
        return notNull ? key : null;
      format = getMessageFormat(string);
      msgFormats.put(key, format);
    }

    // fill with substitutes
    return format.format(substitutes);
  }
  
  /**
   * Generate a MessageFormat for given pattern
   */
  public static MessageFormat getMessageFormat(String pattern) {
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

  /**
   * Returns the available Keys
   */
  public List getKeys() {
    // initialize first
    getKey2String();
    return keys;
  }
  
} //Resources
