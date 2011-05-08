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

import genj.util.swing.ImageIcon;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Wrapper for a Property
 */
public class MetaProperty implements Comparable<MetaProperty> {

  /** static - flags */
  public final static int
    WHERE_NOT_HIDDEN = 1, // only those that are not marked as hidden
    WHERE_DEFAULT    = 2, // only those that are marked default
    WHERE_CARDINALITY_ALLOWS = 4; // only those that are still allowed by cardinality
  
  /** static - loaded images */    
  private static Map<String,ImageIcon> name2images = new HashMap<String,ImageIcon>();
  
  /** static - images */
  public final static ImageIcon
    IMG_CUSTOM  = loadImage("Attribute"),
    IMG_LINK    = loadImage("Association"),
    IMG_UNKNOWN = loadImage("Question"),
    IMG_ERROR   = loadImage("Error"),
    IMG_PRIVATE = loadImage("Private");
  
  /** grammar */
  private Grammar grammar;
    
  /** tag */
  private String tag;
  
  /** cached - image */
  private ImageIcon image;
  
  /** cached - name */
  private String name, names;
  
  /** cached - type */
  private Class<? extends Property>[] types;

  /** cached - info */
  private String info;
  
  /** whether this has been instantiated */
  boolean isInstantiated = false;
  
  /** whether this is grammar conform */
  private boolean isGrammar;
  
  /** properties */
  private Map<String,String> attrs;
  
  /** subs */
  private Map<String,MetaProperty> tag2nested = new HashMap<String,MetaProperty>();
  List<MetaProperty> nested = new ArrayList<MetaProperty>();

  /**
   * Constructor
   */
  /*package*/ MetaProperty(Grammar grammar, String tag, Map<String,String> attributes, boolean isGrammar) {
    // remember tags&props
    this.grammar = grammar;
    this.tag = tag;
    this.attrs = attributes;
    this.isGrammar = isGrammar;
    // inherit from super if applicable
    MetaProperty spr = getSuper();
    if (spr!=null) 
      copyAttributesFrom(spr);
    // done
  }
  
  private void copyAttributesFrom(MetaProperty supr) {

    for (MetaProperty sub : new ArrayList<MetaProperty>(supr.nested)) {
      if (!"0".equals(sub.attrs.get("inherit"))) {
        addNested(sub);
      }
    }
    // type & image & cardinality from super
    if (getAttribute("type")==null)
      attrs.put("type", supr.getAttribute("type"));
    if (getAttribute("img")==null)
      attrs.put("img", supr.getAttribute("img"));
    if (getAttribute("cardinality")==null)
      attrs.put("cardinality", supr.getAttribute("cardinality"));
    if (getAttribute("hide")==null)
      attrs.put("hide", supr.getAttribute("hide"));
  }
  
  /**
   * Super 
   */
  public MetaProperty getSuper() {
    String path = (String)attrs.get("super");
    return path == null  ? null : grammar.getMetaRecursively(new TagPath(path), false);
  }
  
  /**
   * Add a sub (this can be called by multiple threads as meta properties are added e.g. during load thus
   * we make this sync'd
   */
  /*package*/ synchronized void addNested(MetaProperty sub) {
    if (sub==null)
      throw new IllegalArgumentException("Nested meta can't be null");
    // keep key->sub
    tag2nested.put(sub.tag, sub);
    // keep list (replace existing!)
    for (int i=0; i<nested.size(); i++) {
      MetaProperty other = (MetaProperty)nested.get(i);
      if (other.tag.equals(sub.tag)) {
        sub.copyAttributesFrom(other);
        nested.set(i, sub);
        return;       
      }
    }
    nested.add(sub);
    // done
  }
  
  /**
   * Acessor - nested meta properties
   * This is package private to make callees go through
   * indvidual properties rather than accessing this directly.
   */
  /*package*/ MetaProperty[] getAllNested(Property parent, int filter) {
    
    // Loop over subs
    List<MetaProperty> result = new ArrayList<MetaProperty>(nested.size());
    for (int s=0;s<nested.size();s++) {
      
      // .. next sub
      MetaProperty sub = (MetaProperty)nested.get(s);

      // default only?
      if ((filter&WHERE_DEFAULT)!=0) {
        if (!sub.isDefault())
          continue;
      }
        
      // hidden at all (a.k.a cardinality == 0)?
      if ((filter&WHERE_NOT_HIDDEN)!=0&&sub.getAttribute("hide")!=null)
        continue;

      // parent is xref or not?
      if ("0".equals(sub.getAttribute("xref")) && parent instanceof PropertyXRef ) continue;
      if ("1".equals(sub.getAttribute("xref")) && !(parent instanceof PropertyXRef)) continue; 
      
      // blank dupes?
      if ((filter&WHERE_CARDINALITY_ALLOWS)!=0 && sub.isSingleton() && parent.getProperty(sub.getTag())!=null)
        continue;
        
      // .. keep
      result.add(sub);
    }
    // done
    return (MetaProperty[])result.toArray(new MetaProperty[result.size()]);
  }
  
  /**
   * Lookup an attribute
   */
  /*package*/ String getAttribute(String key) {
    return (String)attrs.get(key);
  }
  
  /**
   * Test tag 
   */
  /*package*/ void assertTag(String tag) throws GedcomException {
    if (!this.tag.equals(tag)) throw new GedcomException("Tag should be "+tag+" but is "+this.tag);
  }
  
  /**
   * Check if this is an entity
   */
  public boolean isEntity() {
    return Entity.class.isAssignableFrom(getType());
  }
  
  /**
   * Check if this is a singleton - cardinality *:1
   */
  public boolean isSingleton() {
    String c= getAttribute("cardinality");
    return c!=null && c.endsWith(":1");
  }
  
  /**
   * Check gedcom version
   */
  public boolean isVersion(String version) {
    String v = getAttribute("gedcom");
    return v==null || v.equals(version);
  }
  
  public boolean isDefault() {
    String isDefault = getAttribute("default");
    return isDefault!=null&&"1".equals(isDefault);
  }
  
  /**
   * Check if this is required - cardinality 1:*
   */
  public boolean isRequired() {
    String c = getAttribute("cardinality");
    return c!=null && c.startsWith("1:");
  }
  
  /**
   * A comparison based on tag name
   */
  public int compareTo(MetaProperty other) {
    return Collator.getInstance().compare(getName(), other.getName());
  }

  /**
   * Test
   */
  public boolean allows(String sub) {
    // has to be defined as sub with isGrammar==true
    MetaProperty meta = (MetaProperty)tag2nested.get(sub);
    return meta==null ? false : meta.isGrammar;
  }
  
  /**
   * Test
   */
  public boolean allows(String sub, Class<? extends Property> type) {
    // has to be defined as sub with isGrammar==true
    MetaProperty meta = (MetaProperty)tag2nested.get(sub);
    return meta!=null && type.isAssignableFrom(meta.getType());
  }
  
  /**
   * Create an instance
   */
  public Property create(String value) throws GedcomException {

    // let's try to instantiate    
    Property result = null;
    try {
      result = getType(value).getDeclaredConstructor(String.class).newInstance(getTag());
    } catch (Exception e) {
      // 20030530 catch exceptions only - during load
      // an outofmemoryerrror could happen here
      Gedcom.LOG.log(Level.WARNING, "Couldn't instantiate property "+getType()+" with value '"+value, e);
      result = new PropertySimpleValue(getTag()); 
    }
    
    result.setValue(value);
    
    // increate count
    isInstantiated = true;

    // done 
    return result;
  }
  
  /**
   * Accessor - whether instantiated
   */
  public boolean isInstantiated() {
    return isInstantiated;
  }
  
  /**
   * Accessor - image
   */
  public ImageIcon getImage() {
    if (image==null) {
      // check 'img' attribute
      String s = getAttribute("img");
      // unknown?
      if (s==null) 
        image = getTag().startsWith("_") ? IMG_CUSTOM : IMG_UNKNOWN;
      else  // load it
        image = loadImage(s);
    }
    return image;
  }

  /**
   * Accessor - image
   */
  public ImageIcon getImage(String postfix) {
    Object name = getAttribute("img."+postfix);
    if (name==null) {
      // check err
      if ("err".equals(postfix))
        return IMG_ERROR;
      else
        return getImage() ;
    } 
    return loadImage(name.toString());
  }

  /**
   * Accessor - tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Accessor - type
   */
  public Class<? extends Property> getType() {
    return getTypes()[0];
  }
  
  public Class<? extends Property> getType(String value) {
    
    for (Class<? extends Property> type : getTypes()) {
      
      // check for valid xref values (20070104 since values are not trimmed by loaders we do this here) 
      if (PropertyXRef.class.isAssignableFrom(type) && !(value.trim().startsWith("@")&&value.trim().endsWith("@")) ) 
        continue;
      
      // use that
      return type;
    }
    
    return PropertySimpleValue.class;
  }
  
  /**
   * Accessor - types
   */
  @SuppressWarnings("unchecked")
  public Class<? extends Property>[] getTypes() {
    
    // check cached type
    if (types==null) {
      
      String attrType = getAttribute("type");
      if (attrType==null)
        types = new Class[]{ PropertySimpleValue.class };
      else try {
        
        String[] attrTypes = attrType.split("\\|");
        types = new Class[attrTypes.length];
        
        for (int i=0;i<attrTypes.length;i++)
          types[i] = (Class<? extends Property>)Class.forName("genj.gedcom."+attrTypes[i]);
        
      } catch (Throwable t) {
        Gedcom.LOG.log(Level.WARNING, "Property type(s) genj.gedcom."+attrType+" couldn't be instantiated", t);    
        types = new Class[]{ PropertySimpleValue.class };
      }
    }
    
    // resolved
    return types;
  }

  /**
   * Accessor - some explanationary information about the meta
   */
  public String getName() {
    return getName(false);
  }
  
  /**
   * Accessor - cardinality
   */
  public String getCardinality() {
    return (String)attrs.get("cardinality");
  }
  
  /**
   * Accessor - some explanationary information about the meta
   */
  public String getName(boolean plural) {
    String result;
    if (plural) {
      result = names;
      if (result ==null)
        result = Gedcom.getName(tag, true);
      names = result;
    } else {
      result = name;
      if (result ==null)
        result = Gedcom.getName(tag, false);
      name = result;
    }
    return result;
  }
  
  /**
   * Accessor - some explanationary information about the meta
   */
  public String getInfo() {
    // check cached info
    if (info==null) {
      info = Gedcom.getResources().getString(tag+".info", false);
      if (info==null) {
        char c = tag.charAt(0);
        if (c!='_') c = '?'; // make it "_.info" or "?.info"
        info = Gedcom.getResources().getString(  c + ".info");
      }
      // prepend tag name
      info = getName() + ":\n" + info;
    }
    // done
    return info;
  }

  /**
   * Resolve nested by tag
   */
  public MetaProperty getNestedRecursively(TagPath path, boolean persist) {
    
    String tag = path.get(0);
    if (!this.tag.equals(tag) && !".".equals(tag))
      throw new IllegalArgumentException();
    
    return getNestedRecursively(path, 1, persist);
  }
  
  /*package*/ MetaProperty getNestedRecursively(TagPath path, int pos, boolean persist) {

    // is this it?
    if (pos==path.length())
      return this;

    // get meta for next tag
    return getNested(path.get(pos), persist).getNestedRecursively(path, pos+1, persist);
  }

  /**
   * Resolve sub by tag
   */
  public MetaProperty getNested(String tag, boolean persist) {
    // check tag argument
    if (tag==null||tag.length()==0)
      throw new IllegalArgumentException("tag can't be empty");
    // current tag in map?
    MetaProperty result = (MetaProperty)tag2nested.get(tag);
    if (result==null) {
      result = new MetaProperty(grammar, tag, new HashMap<String,String>(), false);
      if (persist) addNested(result);
    }
    // done
    return result;
  }
  
  /**
   * Returns index of given subtag
   * @return zero based index or Integer.MAX_VALUE if unknown
   */
  public int getNestedIndex(String subtag) {
    // make sure CHAN get's a high one (this should probably be defined in grammar)
    if (subtag.equals("CHAN"))
      return Integer.MAX_VALUE;
    // look through grammar defined subs
    for (int i=0;i<nested.size();i++) {
      if (((MetaProperty)nested.get(i)).getTag().equals(subtag))
        return i;
    }
    //20040518 make the index of an unknown subtag as large as possible
    return Integer.MAX_VALUE;
  }
  
  /**
   * Load image (once)
   */
  private static ImageIcon loadImage(String name) {
    // look up
    ImageIcon result = (ImageIcon)name2images.get(name);
    if (result==null) {
      try {
        // this could potentially be interrupted - we'll have to try again in that case
        while (true) {
          try {
            result = new ImageIcon(MetaProperty.class, "images/"+name);
            name2images.put(name, result);
            break;
          } catch (IllegalStateException iae) {
            // retry
          }
        }
      } catch (Throwable t) {
        Gedcom.LOG.log(Level.WARNING, "Unexpected problem reading "+name, t);
        return IMG_ERROR;
      }
    }
    // done
    return result;
  }
  
  @Override
  public String toString() {
    return getTag() + attrs;
  }
  
} //MetaProperty
