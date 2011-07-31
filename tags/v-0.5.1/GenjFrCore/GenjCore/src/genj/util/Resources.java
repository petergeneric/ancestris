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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
public class Resources {
  
  /** keep track of loaded resources */
  private static Map<String, Resources> instances = new HashMap<String, Resources>();

  /** the mapping key, resource  */
  private volatile Map<String, String> key2string;
  private List<String> keys;

  /** the package name this resource is for */
  private String pkg;

  /** cached message formats */
  private WeakHashMap<String, MessageFormat> msgFormats = new WeakHashMap<String, MessageFormat>();
  
  /**
   * Constructor for empty resources
   */
  public Resources() {
    this((InputStream)null);
  }
  
  /**
   * Constructor for resources from explicit input stream
   */
  public Resources(InputStream in) {
    
    key2string = new HashMap<String, String>();
    keys = new ArrayList<String>(1000);
    
    if (in!=null) try {
      load(in);
    } catch (IOException e) {
      Logger.getLogger("genj.util").log(Level.FINE, "can't read resources", e);
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
      Resources result = instances.get(packge);
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
    Class<?> clazz = object instanceof Class<?> ? (Class<?>)object : object.getClass();
    String name = clazz.getName();
    int last = name.lastIndexOf('.');
    return last<0 ? "" : name.substring(0, last);
  }

  /**
   * Reset cached Resourcces (for language change)
   */
  public static void clearResources(){
    synchronized (instances) {
      for (String s:instances.keySet()){
          instances.get(s).unCache();
      }
    }
  }

  private void unCache(){
      key2string = null;
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
          
//        // text has to start with letter
//        if (!Character.isLetter(line.charAt(0)))
//          continue;
        
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
//    while (true) {
//      int i = string.indexOf("\\n");
//      if (i<0) break;
//      string = string.substring(0,i) + '\n' + string.substring(i+2);
//    }
      return  AncestrisUtils.unescape(string);
  }
  
  /**
   * Lazy getter for resource map
   */
  private Map<String,String> getKey2String() {
    
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
      Map<String,String> tmpKey2Val = new HashMap<String,String>();    
      List<String> tmpKeys = new ArrayList<String>(100);

      // loading english first (primary language)
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, null, null)), tmpKeys, tmpKey2Val, false);
      } catch (Throwable t) {
      }
      
      // trying to load language specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), null)), tmpKeys, tmpKey2Val, false);
      } catch (Throwable t) {
      }
  
      // trying to load language and country specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), locale.getCountry())), tmpKeys, tmpKey2Val, false);
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
    String result = getKey2String().get(key.toLowerCase());
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
  public String getString(String key, Object... substitutes) {

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
  public List<String> getKeys() {
    // initialize first
    getKey2String();
    return keys;
  }


          public static String unescape(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length());
            unescape(writer, str);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            throw new UnknownError();
        }
    }

    private static void unescape(Writer out, String str) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz = str.length();
        StringBuffer unicode = new StringBuffer(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);
            if (inUnicode) {
                // if in unicode, then we're reading unicode
                // values in somehow
                unicode.append(ch);
                if (unicode.length() == 4) {
                    // unicode now contains the four hex digits
                    // which represents our unicode character
                    try {
                        int value = Integer.parseInt(unicode.toString(), 16);
                        out.write((char) value);
                        unicode.setLength(0);
                        inUnicode = false;
                        hadSlash = false;
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException("Unable to parse unicode value: " + unicode, nfe);
                    }
                }
                continue;
            }
            if (hadSlash) {
                // handle an escaped value
                hadSlash = false;
                switch (ch) {
//                        case '\\':
//                            out.write('\\');
//                            break;
//                        case '\'':
//                            out.write('\'');
//                            break;
//                        case '\"':
//                            out.write('"');
//                            break;
//                        case 'r':
//                            out.write('\r');
//                            break;
//                        case 'f':
//                            out.write('\f');
//                            break;
//                        case 't':
//                            out.write('\t');
//                            break;
                        case 'n':
                            out.write('\n');
                            break;
//                        case 'b':
//                            out.write('\b');
//                            break;
                    case 'u': {
                        // uh-oh, we're in unicode country....
                        inUnicode = true;
                        break;
                    }
                    default:
                        out.write(ch);
                        break;
                }
                continue;
            } else if (ch == '\\') {
                hadSlash = true;
                continue;
            }
            out.write(ch);
        }
        if (hadSlash) {
            // then we're in the weird case of a \ at the end of the
            // string, let's output it anyway.
            out.write('\\');
        }
    }

  
} //Resources
