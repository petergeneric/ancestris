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
package genj.print;

import genj.util.Registry;
import genj.util.WordBuffer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.TextSyntax;
import javax.print.attribute.URISyntax;
import javax.print.attribute.standard.MediaPrintableArea;

/**
 * An extended Registry for Print parameters
 */
public class PrintRegistry extends Registry {
  
  public static PrintRegistry get(Object source) {
    
    Registry r = Registry.get(source);
    
    return new PrintRegistry(r);
  }

  /**
   * Constructor
   */
  private PrintRegistry(Registry registry) {
    super(registry, "");
  }
  
  /**
   * Store Service
   */
  public void put(PrintService service) {
    super.put("service", service.getName());
  }
  
  /**
   * Retrieve Service
   */
  public PrintService get(PrintService def) {
    String name = super.get("service", "");
    PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
    for (int i = 0; i < services.length; i++) {
      if (services[i].getName().equals(name))
        return services[i];
    } 
    return def;
  }
  
  /**
   * Store PrintRequestAttributes
   */
  public void put(AttributeSet set) {
    WordBuffer track = new WordBuffer();
    Attribute[] attrs = set.toArray();
    for (int i = 0; i < attrs.length; i++) {
      String key = put(attrs[i]);
      if (key!=null)
        track.append(key);
    }
    super.put("attributes", track.toString());
  }
  
  /**
   * Retrieve PrintRequestAttributes
   */
  @SuppressWarnings("unchecked")
  public void get(AttributeSet set) {
    
    StringTokenizer attributes = new StringTokenizer(super.get("attributes", ""));
    while (attributes.hasMoreTokens()) {
      String attribute = attributes.nextToken();
      try {
        Attribute a = get((Class<Attribute>)Class.forName(attribute), null);
        if (a!=null)
          set.add(a);
        else
          PrintTask.LOG.log(Level.INFO, "Couldn't restore print attribute "+attribute);
      } catch (Throwable t) {
        PrintTask.LOG.log(Level.WARNING, "Error restoring print attribute "+attribute, t);
      }
    }
    
  }
  
  /**
   * Store PrintRequestAttribute
   */
  public String put(Attribute attr) {
    
    if (attr instanceof EnumSyntax)
      return put((EnumSyntax)attr);
    if (attr instanceof IntegerSyntax)
      return put((IntegerSyntax)attr);
    if (attr instanceof URISyntax)
      return put((URISyntax)attr);
    if (attr instanceof MediaPrintableArea)
      return put((MediaPrintableArea)attr);
    if (attr instanceof TextSyntax)
      return put((TextSyntax)attr);
    
    return null;
  }

  /**
   * Retrieve PrintRequestAttribute
   */
  public Attribute get(Class<Attribute> type, Attribute def) {

    // check type
    if (!Attribute.class.isAssignableFrom(type))
      throw new IllegalArgumentException("only Attribute types allowed");
    if (def!=null&&!type.isAssignableFrom(def.getClass()))
      throw new IllegalArgumentException("def/Attribute types mismatch");
    
    if (EnumSyntax.class.isAssignableFrom(type))
      return getEnumSyntax(type, def);
    
    if (IntegerSyntax.class.isAssignableFrom(type))
      return getIntegerSyntax(type, def);
    
    if (URISyntax.class.isAssignableFrom(type))
      return getURISyntax(type, def);
    
    if (MediaPrintableArea.class.isAssignableFrom(type))
      return getMediaPrintableArea(def);
    
    if (TextSyntax.class.isAssignableFrom(type))
      return getTextSyntax(type, def);
    
    return null;
  }

  /**
   * Store - TextSyntax
   */
  private String put(TextSyntax syntax) {
    String key = syntax.getClass().getName();
    super.put(key, syntax.getValue());
    return key;
  }

  /**
   * Retrieve - TextSyntax
   */
  private Attribute getTextSyntax(Class<Attribute> type, Attribute def) {
    // try to get a stored value
    String txt = super.get(type.getName(),(String)null);
    if (txt==null)
      return def;
    // try to instantiate appropriate attr
    try {
      return type.getConstructor(new Class[]{String.class, Locale.class}).newInstance(new Object[]{txt, null});
    } catch (Throwable t) {
      return def;
    }
  }

  /**
   * Store - URISyntax
   */
  private String put(URISyntax syntax) {
    String key = syntax.getClass().getName();
    super.put(key, ""+syntax.getURI());
    return key;
  }

  /**
   * Retrieve - URISyntax
   */
  private Attribute getURISyntax(Class<Attribute> type, Attribute def) {
    // try to get a stored value
    String uri = super.get(type.getName(),(String)null);
    if (uri==null)
      return def;
    // try to instantiate appropriate attr
    try {
      return type.getConstructor(new Class[]{URI.class}).newInstance(new Object[]{new URI(uri)});
    } catch (Throwable t) {
      return def;
    }
  }

  /**
   * Store - IntegerSyntax
   */
  private String put(IntegerSyntax syntax) {
    String key = syntax.getClass().getName();
    super.put(key, syntax.getValue());
    return key;
  }

  /**
   * Retrieve - IntegerSyntax
   */
  private Attribute getIntegerSyntax(Class<Attribute> type, Attribute def) {
    // try to get a stored value
    int i = super.get(type.getName(),(int)-1);
    if (i<0)
      return def;
    // try to instantiate appropriate attr
    try {
      return type.getConstructor(new Class[]{Integer.TYPE}).newInstance(new Object[]{new Integer(i)});
    } catch (Throwable t) {
      return def;
    }
  }

  /**
   * Store - EnumSyntax
   */
  private String put(EnumSyntax syntax) {
    String key = syntax.getClass().getName();
    super.put(key, syntax.getValue());
    return key;
  }
  
  /**
   * Retrieve - EnumSyntax
   */
  private Attribute getEnumSyntax(Class<Attribute> type, Attribute def) {
    // try to get a stored value
    int i = super.get(type.getName(),(int)-1);
    if (i<0)
      return def;
    // try to find appropriate enumeration item
    try {
      Field[] fields = type.getFields();
      for (int f = 0; f < fields.length; f++) {
        Field field = fields[f];
        if (Modifier.isPublic(field.getModifiers())) {
          if (Modifier.isStatic(field.getModifiers())) {
            if (field.getType()==type) {
              EnumSyntax e = (EnumSyntax)field.get(null);
              if (e.getValue()==i) {
                return (Attribute)e;
              }
            }
          }
        }
      }
    } catch (Throwable t) {
    }
    return def;
  }

  /**
   * Store - MediaPrintableArea
   */
  private String put(MediaPrintableArea area) {
    String key = area.getClass().getName();
    super.put(key+".x", area.getX(MediaPrintableArea.INCH));
    super.put(key+".y", area.getY(MediaPrintableArea.INCH));
    super.put(key+".w", area.getWidth(MediaPrintableArea.INCH));
    super.put(key+".h", area.getHeight(MediaPrintableArea.INCH));
    return key;
  }

  /**
   * Retrieve - MediaPrintableArea
   */
  private Attribute getMediaPrintableArea(Attribute def) {
    String prefix = MediaPrintableArea.class.getName()+".";
    float
     x = super.get(prefix+'x', -1F),
     y = super.get(prefix+'y', -1F),
     w = super.get(prefix+'w', -1F),
     h = super.get(prefix+'h', -1F);
    if (x<0||y<0||w<0||h<0)
      return def;
    try {
      return new MediaPrintableArea(x,y,w,h,MediaPrintableArea.INCH);
    } catch (IllegalArgumentException e) {
      return def;
    }
  }
  
} //PrintRegistry
