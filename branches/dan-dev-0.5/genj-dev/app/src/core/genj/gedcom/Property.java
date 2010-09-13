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

import genj.util.Resources;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property implements Comparable<Property> {

  /** static strings */
  protected final static String 
    UNSUPPORTED_TAG = "Unsupported Tag";
  
  private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{(.*?)\\$(.)(.*?)\\}");
  
  /** parent of this property */
  private Property parent=null;
  
  /** children of this property */
  // 20070128 made this a lazy list so we're not wasting the space for all those leaf nodes out there
  private List<Property> children = null;
  
  /** images */
  protected ImageIcon image, imageErr;
  
  /** whether we're transient or not */
  protected boolean isTransient = false;

  /** whether we're private or not */
  private boolean isPrivate = false;
  
  /** resources */
  protected final static Resources resources = Gedcom.resources;

  /** a localized label */
  public final static String LABEL = resources.getString("prop");

  /** the tag */
  private String tag = null;

  
  protected Property(String tag) {
    this.tag = tag;
  }

  /**
   * Lifecycle - callback after being added to parent.
   */
  /*package*/ void afterAddNotify() {
    
  }

  /**
   * Lifecycle - callback before being removed from parent
   */
  /*package*/ void beforeDelNotify() {
    
    // delete children
    delProperties();
    
    // continue
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagateXRefLinked(PropertyXRef property1, PropertyXRef property2) {
    if (parent!=null)
      parent.propagateXRefLinked(property1, property2);
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagateXRefUnlinked(PropertyXRef property1, PropertyXRef property2) {
    if (parent!=null)
      parent.propagateXRefUnlinked(property1, property2);
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagatePropertyAdded(Property container, int pos, Property added) {
    if (parent!=null)
      parent.propagatePropertyAdded(container, pos, added);
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagatePropertyDeleted(Property container, int pos, Property deleted) {
    if (parent!=null)
      parent.propagatePropertyDeleted(container, pos, deleted);
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagatePropertyChanged(Property property, String oldValue) {
    if (parent!=null)
      parent.propagatePropertyChanged(property, oldValue);
  }
  
  /**
   * Propagate a change to the containing hierarchy
   */
  /*package*/ void propagatePropertyMoved(Property property, Property moved, int from, int to) {
    if (parent!=null)
      parent.propagatePropertyMoved(property, moved, from, to);
  }
  
  /**
   * Associates a file with this property
   * @return success or not
   */
  public boolean addFile(File file) {
    return addFile(file, "");
  }
  public boolean addFile(File file, String title) {
    // FILE not allowed here? 
    if (!getMetaProperty().allows("FILE")) {
      // OBJE neither? 
      if (!getMetaProperty().allows("OBJE")) 
        return false;
      // let new OBJE handle this
      return addProperty("OBJE", "").addFile(file, title);
    }
    // need to add it?
    List<PropertyFile> pfiles = getProperties(PropertyFile.class);
    PropertyFile pfile;
    if (pfiles.isEmpty()) {
      pfile = (PropertyFile)addProperty("FILE", "");
    } else {
      pfile = (PropertyFile)pfiles.get(0);
    }
    // set title
    Property ptitle = getProperty("TITL");
    if (ptitle!=null)
      ptitle.setValue(title);
    else if (title.length()>0)
      addProperty("TITL", title);
      
    // keep it
    return pfile.addFile(file);
  }
  
  /**
   * Associates a note object with this property
   */
  public boolean addNote(Note note) {
    // NOTE? 
    if (!getMetaProperty().allows("NOTE")) 
      return false;
    // add reference
    PropertyNote xref = new PropertyNote();
    addProperty(xref);
    xref.setValue(note.getId());
    try {
      xref.link();
    } catch (GedcomException e) {
      Gedcom.LOG.log(Level.FINE, "unexpected", e);
      delProperty(xref);
      return false;
    }
    return true;
  }
  
  /**
   * Associates a media object with this proprty
   */
  public boolean addMedia(Media media) {
    // OBJE? 
    if (!getMetaProperty().allows("OBJE")) 
      return false;
    // add reference
    PropertyMedia xref = new PropertyMedia();
    addProperty(xref);
    xref.setValue(media.getId());
    try {
      xref.link();
    } catch (GedcomException e) {
      Gedcom.LOG.log(Level.FINE, "unexpected", e);
      delProperty(xref);
      return false;
    }
    return true;
  }
  
  /**
   * Adds a sub-property to this property 
   */
  public Property addProperty(String tag, String value) {
    try {
      return addProperty(tag, value, -1);
    } catch (GedcomException e) {
      // ugh, use a simple value here
      return addProperty(new PropertySimpleReadOnly(tag, value), -1);
    }
  }
  
  /**
   * Adds a sub-property to this property
   */
  public Property addProperty(String tag, String value, int pos) throws GedcomException {
    return addProperty(getMetaProperty().getNested(tag, true).create(value), pos);
  }
  
  /**
   * Adds a sub-property to this property
   */
  public Property addSimpleProperty(String tag, String value, int pos) {
    return addProperty(new PropertySimpleValue(tag, value), pos);
  }
  
  /**
   * Adds a sub-property to this property
   * @param prop new property to add
   */
  /*package*/ Property addProperty(Property prop) {
    return addProperty(prop, -1);
  }

  /**
   * Adds another property to this property
   * @param child the property to add
   * @param pos, 0-n for position, -1 for placement
   */
  /*package*/ Property addProperty(Property child, int pos) {
    
    // check child
    if (child.getParent()!=null||child.getNoOfProperties()>0)
      throw new IllegalArgumentException("Can't add a property that is already contained or contains properties");
    
    // check grammar for placement if applicable
    if (pos<0) {
      MetaProperty meta = getMetaProperty();
      pos = 0;
      int index = meta.getNestedIndex(child.getTag());
      for (;pos<getNoOfProperties();pos++) {
        if (meta.getNestedIndex(getProperty(pos).getTag())>index)
          break;
      }
    } else {
      // patch to end of properties if appropriate
      if (pos>getNoOfProperties())
        pos = getNoOfProperties();
    }
    
    // keep child now
    if (children==null)
      children = new ArrayList<Property>();
    children.add(pos, child);
    
    if (isTransient) child.isTransient = true;
    
    child.parent = this;

    // propagate to others
    propagatePropertyAdded(this, pos, child);
    
    // tell to added
    child.afterAddNotify();
    
    // Done
    return child;
  }
  
  /**
   * Removes all properties
   */
  public void delProperties() {
    if (children!=null) {
      // grab list of children once - subsequent dels might lead to changes to the array
      Property[] cs = (Property[])children.toArray(new Property[children.size()]);
      for (int c = cs.length-1; c>=0; c--) 
        delProperty(cs[c]);
      if (children.isEmpty()) children = null;
    }
  }
  
  /**
   * Removes all properties with given tag
   */
  public void delProperties(String tag) {
    if (children!=null) {
      Property[] cs = (Property[])children.toArray(new Property[children.size()]);
      for (int c = 0; c < cs.length; c++) {
        if (cs[c].getTag().equals(tag))
          delProperty(cs[c]);
      }
      if (children.isEmpty()) children = null;
    }
  }
  
  /**
   * Removes a property by looking in the property's properties
   */
  public void delProperty(Property deletee) {
    
    if (children==null)
      throw new IndexOutOfBoundsException("no such child");

    if (deletee==null)
      throw new IllegalArgumentException("can't delete null property");
    
    // find position (throw outofbounds if n/a)
    int pos = 0;
    for (;;pos++) {
      if (children.get(pos)==deletee)
        break;
    }

    // do it
    delProperty(pos);
    
  }

  /**
   * Removes a property by position
   */
  public void delProperty(int pos) {

    // range check
    if (children==null||pos<0||pos>=children.size())
      throw new IndexOutOfBoundsException("No property "+pos);
    Property removed = (Property)children.get(pos);

    // tell to removed first so it has some chance for cleanup
    removed.beforeDelNotify(); 

    // remove it now
    children.remove(pos);
    removed.parent = null;

    // propagate change (see addNotify() for motivation why propagate is here)
    propagatePropertyDeleted(this, pos, removed);

    // done
  }
  
  /**
   * Move contained properties
   */
  public void moveProperties(List<Property> properties, int pos) {
    
    // move children around
    for (int i = 0; i < properties.size(); i++) {
      Property prop = (Property)properties.get(i);
      pos = moveProperty(prop, pos);
    }
    
  }

  /**
   * Move a property
   */
  public int moveProperty(Property prop, int to) {
    return moveProperty(children.indexOf(prop), to);
  }
  
  /**
   * Move a property
   */
  public int moveProperty(int from, int to) {
    Property prop = (Property)children.remove(from);
    if (from<to) to--;
    children.add(to, prop);
    // propagate moved
    propagatePropertyMoved(this, prop, from, to);
    // return next position
    return to+1;
  }
  
  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return null;
  }

  /**
   * Returns the entity this property belongs to - simply looking up
   */
  public Entity getEntity() {
    return parent==null ? null : parent.getEntity();
  }

  /**
   * Returns the gedcom this property belongs to
   */
  public Gedcom getGedcom() {
    return parent!=null ? parent.getGedcom() : null;
  }
  
  /**
   * Returns the image which is associated with this property.
   */
  public ImageIcon getImage() {
    return getImage(false);
  }
  
  /**
   * Returns the image which is associated with this property.
   */
  public ImageIcon getImage(boolean checkValid) {
    
    // valid or not ?
    if (!checkValid||isValid()) {
      if (image==null) 
        image = getGedcom()!=null ? getMetaProperty().getImage() : MetaProperty.IMG_CUSTOM; 
      return image;
    }
    
    // not valid
    if (imageErr==null) 
      imageErr = getMetaProperty().getImage("err"); 
      
    return imageErr;
  }

  /**
   * Calculates the number of properties this property has.
   */
  public int getNoOfProperties() {
    return children==null?0:children.size();
  }

  /**
   * Returns the property this property belongs to
   */
  public Property getParent() {
    return parent;
  }
  
  /**
   * Returns the path from this to a nested property
   */
  public TagPath getPathToNested(Property nested) {    
    Stack<String> result = new Stack<String>();
    nested.getPathToContaining(this, result);
    return new TagPath(result);
  }
  
  private void getPathToContaining(Property containing, Stack<String> result) {
    result.push(getTag());
    if (containing==this)
      return;
    if (parent==null)
      throw new IllegalArgumentException("couldn't find containing "+containing);
    parent.getPathToContaining(containing, result);
  }
  
  /**
   * Returns the path to this property. This is a sequence of tags leading to this property from its containing entity.
   */
  public TagPath getPath() {
    return getPath(false);
  }
  
  /**
   * Returns the path to this property. This is a sequence of tags leading to this property from its containing entity.
   * @param unique whether tags should be unqiue, e.g. INDI:BIRT#0:DATE and INDI:BIRT#1:DATE vs INDI:BIRT:DATE
   */
  public TagPath getPath(boolean unique) {

    Stack<String> stack = new Stack<String>();

    // loop through parents
    String tag = getTag();
    Property parent = getParent();
    while (parent!=null) {
      
      // check qualifier?
      if (unique) {
        int qualifier = 0;
        for (int i=0, j=parent.getNoOfProperties(); i<j; i++) {
          Property sibling = parent.getProperty(i);
          if (sibling==this) break;
          if (sibling.getTag().equals(tag)) qualifier++;
        }
        stack.push(tag + "#" + qualifier);
      } else {
        stack.push(tag);
      }

      // next up
      tag = parent.getTag();
      parent = parent.getParent();
    }
    
    // add last
    stack.push(tag);

    // done
    return new TagPath(stack);
    
  }
  
  /**
   * Test for (recursive) containment
   */
  public boolean contains(Property prop) {
    if (children==null)
      return false;
    for (int c = 0; c < children.size(); c++) {
      Property child = (Property)children.get(c);
      if (child==prop||child.contains(prop))
        return true;
    }
    return false;
  }

  /**
   * Test for (recursive) containment
   */
  public boolean isContained(Property in) {
    Property parent = getParent();
    if (parent==in) return true;
    return parent==null ? false : parent.isContained(in);
  }

  /**
   * Test properties
   */
  public boolean hasProperties(List<Property> props) {
    return children==null ? false : children.containsAll(props);
  }
  
  /**
   * Returns this property's properties (all children)
   */
  public Property[] getProperties() {
    return children==null ? new Property[0] : toArray(children);
  }
  
  /**
   * Returns property's properties by criteria
   * @param tag  regular expression pattern of tag to match
   * @param value regular expression pattern of value to match
   * @return matching properties
   */
  public List<Property> findProperties(Pattern tag, Pattern value) {
    // create result
    List<Property> result = new ArrayList<Property>();
    // check argument
    if (value==null) value = Pattern.compile(".*");
    // recurse
    findPropertiesRecursively(result, tag, value, true);
    // done
    return result;
  }

  protected boolean findPropertiesRecursivelyTest(Pattern tag, Pattern value) {
    return tag.matcher(getTag()).matches() && value.matcher(getValue()).matches(); 
  }
  
  private void findPropertiesRecursively(Collection<Property> result, Pattern tag, Pattern value, boolean recursively) {
    // check current
    if (findPropertiesRecursivelyTest(tag, value))
      result.add(this);
    // recurse into properties
    for (int i=0, j=getNoOfProperties(); i<j ; i++) {
      if (recursively) getProperty(i).findPropertiesRecursively(result, tag, value, recursively);
    }
    // done
  }
  
  /**
   * Returns this property's properties by tag (only valid properties are considered)
   */
  public Property[] getProperties(String tag) {
    return getProperties(tag, true);
  }
  
  /**
   * Returns this property's properties by tag 
   */
  public Property[] getProperties(String tag, boolean validOnly) {
    ArrayList<Property> result = new ArrayList<Property>(getNoOfProperties());
    for (int i=0, j = getNoOfProperties(); i<j ; i++) {
      Property prop = getProperty(i);
      if (prop.getTag().equals(tag)&&(!validOnly||prop.isValid()))
        result.add(prop);
    }
    return toArray(result);
  }
  
  /**
   * Returns this property's properties which are of given type
   */
  public <T> List<T> getProperties(Class<T> type) {
    List<T> props = new ArrayList<T>(10);
    getPropertiesRecursively(props, type);
    return props;
  }
  
  @SuppressWarnings("unchecked")
  private <T> void getPropertiesRecursively(List<T> props, Class<T> type) {
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      if (type.isAssignableFrom(child.getClass())) {
        props.add((T)child);
      }
      child.getPropertiesRecursively(props, type);
    }
  }

  /**
   * Returns a sub-property position
   */
  public int getPropertyPosition(Property prop) {
    if (children==null)
      throw new IllegalArgumentException("no such property");
    for (int i=0;i<children.size();i++) {
      if (children.get(i)==prop)
        return i;
    }
    throw new IllegalArgumentException("no such property");
  }

  /**
   * Returns this property's nth property
   * kenmraz: added checks since I was able
   * to get an indexOutOfBounds error when DnDing
   * to the end of list of children.  
   * nmeier: remove check again to force valid
   * index argument - fixed DnD code to provide
   * correct parameter
   */
  public Property getProperty(int n) {
    if (children==null)
      throw new IndexOutOfBoundsException("no property "+n);
    return (Property)children.get(n);
  }

  /**
   * Returns this property's property by tag
   * (only valid children are considered)
   */
  public Property getProperty(String tag) {
    return getProperty(tag, true);
  }

  /**
   * Returns this property's property by path
   */
  public Property getProperty(String tag, boolean validOnly) {
    // safety check
    if (tag.indexOf(':')>0) throw new IllegalArgumentException("Path not allowed");
    // loop children
    // NM 20070128 use direct field access - it's less expensive
    if (children!=null) {
      for (int i=0, j=children.size();i<j;i++) {
        Property child = (Property)children.get(i);
        if (!child.getTag().equals(tag)) continue;
        if (validOnly&&!child.isValid()) continue;
        return child;
      }
    }
    // not found
    return null;
  }

  /**
   * Returns one of this property's properties by path
   */
  public Property getPropertyByPath(String path) {
    // 20050822 I've added this convenient getter to make it
    // easier for beginners to use the API - no need to bother
    // them with creating a TagPath first. This is not as performant
    // as doing so and reusing the same path.
    return getProperty(new TagPath(path));
  }
  
  /**
   * Returns one of this property's properties by path
   */
  public Property getProperty(TagPath path) {
    return getProperty(path, true);
  }
  
  /**
   * Returns one of this property's properties by path
   */
  public Property getProperty(TagPath path, boolean backtrack) {
    
    final Property[] result = new Property[1];

    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
        result[0] = prop;
        return false;
      }
    };
    
    path.iterate(this, visitor, backtrack);
    
    return result[0];
  }
  
  /**
   * Returns this property's properties by path
   */
  public Property[] getProperties(TagPath path) {
    
   final  List<Property> result = new ArrayList<Property>(10);

    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
        result.add(prop);
        return true;
      }
    };
    
    path.iterate(this, visitor);
    
    return Property.toArray(result);
  }

//  private static Property getPropertyRecursively(Property prop, TagPath path, int pos, List listAll, boolean checkPropsTagFirst) {
//    
//    while (true) {
//
//      // traversed path?
//      if (pos==path.length()) {
//        if (listAll!=null)
//          listAll.add(prop);
//        return prop;
//      }
//      
//      // a '..'?
//      if (path.equals(pos, "..")) {
//        Property parent = prop.getParent();
//        // no parent?
//        if (parent==null)
//          return null;
//        // continue with parent
//        prop = parent;
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//      
//      // a '.'?
//      if (path.equals(pos, ".")) {
//        // continue with self
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//
//      // a '*'?
//      if (path.equals(pos, "*")) {
//        // check out target
//        if (!(prop instanceof PropertyXRef))
//          return null;
//        prop = ((PropertyXRef)prop).getTarget();
//        if (prop==null)
//          return null;
//        // continue with prop
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//
//      // still have to match prop's tag?
//      if (checkPropsTagFirst) {
//        if (!path.equals(pos, prop.getTag()))
//          return null;
//        // go with prop then
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//      
//      // Search for appropriate tag in children
//      for (int i=0;i<prop.getNoOfProperties();i++) {
//
//        Property ith = prop.getProperty(i);
//
//        // tag is good?
//        if (path.equals(pos, ith.getTag())) {
//          // find all or select one specific based on (tag, selector)?
//          if (listAll!=null) {
//            getPropertyRecursively(ith, path, pos+1, listAll, false);
//          } else {
//            return getPropertyRecursively(ith, path, pos+1, null, false);
//          }
//        }
//      }
//      
//      // no recursion left
//      return null;
//          
//    }
//    
//  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public final String getTag() {
    return tag;
  }

  /**
   * Returns the value of this property as string (this is a Gedcom compliant value)
   */
  abstract public String getValue();
  
  /**
   * Returns a user-readable property value
   */
  public String getDisplayValue() {
    return getValue();
  }

  /**
   * Returns a property value of a child property. This is
   * a convenient method to access a child-property without having
   * to check for null before calling its getValue()
   */
  public String getPropertyValue(String tag) {
    Property child = getProperty(tag);
    return child!=null ? child.getValue() : "";
  }

  /**
   * Returns a user-readable property value of a child property. This is
   * a convenient method to access a child-property without having
   * to check for null before calling its getDisplayValue()
   */
  public String getPropertyDisplayValue(String tag) {
    Property child = getProperty(tag);
    return child!=null ? child.getDisplayValue() : "";
  }

  /**
   * The default string representation returns the property name, date and place information and display value (if available)
   */
  @Override
  public String toString() {
    
    WordBuffer result = new WordBuffer(" ");
    result.append(getPropertyName());
    
    String val = getDisplayValue();
    if (val.length()>0) 
      result.append(val);
    
    Property date = getProperty("DATE");
    if (date instanceof PropertyDate && date.isValid()) 
      result.append(date.getDisplayValue());

    Property plac = getProperty("PLAC");
    if (plac!=null) {
      String s = plac.getDisplayValue();
      if (s.length()>0) 
        result.append(plac.getDisplayValue());
    } else {
      Property addr = getProperty("ADDR");
      if (addr!=null) {
        Property city = addr.getProperty("CITY");
        if (city!=null) {
          String s = city.getDisplayValue();
          if (s.length()>0) 
            result.append(s);
        }
      }
    }
    return result.toString();
  }


  /**
   * Returns a value at given path or fallback
   */
  public String getValue(final TagPath path, String fallback) {
    Property prop = getProperty(path);
    return prop==null ? fallback : prop.getValue();
  }
  
  /**
   * Set a value at given path
   */
  public Property setValue(final TagPath path, final String value) {

    final Property[] result = new Property[1];
    
    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
        // don't apply setValue to xref - use substitute instead
        if (prop instanceof PropertyXRef && ((PropertyXRef)prop).getTarget()!=null) 
          prop = prop.getParent().addProperty(prop.getTag(), "");
        // set it and remember
        prop.setValue(value);
        result[0] = prop;
        // done - don't continue;
        return false;
      }
      protected boolean recursion(Property parent,String child) {
        if (parent.getProperty(child, false)==null)
          parent.addProperty(child, "");
        return true;
      }
    };
    
    path.iterate(this, visitor);

    // done
    return result[0];
  }
  
  /**
   * Sets this property's value as string.
   */
  public abstract void setValue(String value);

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    return true;
  }

  /**
   * Compares this property to another property
   * @return  a negative integer, zero, or a positive integer as this object
   *      is less than, equal to, or greater than the specified object.
   */
  public int compareTo(Property that) {
    // no gedcom available?
    return compare(this.getDisplayValue(), that.getDisplayValue() );
  }
  
  /**
   * Compare to string gedcom language aware
   */
  protected int compare(String s1, String s2) {
    
    // I was pondering the notion of keeping cached CollationKeys
    // for faster recurring comparisons but apparently that's not
    // always faster and the keys have a respectable size themselves
    // which leads me to believe a simple Collator.compare() is
    // the better compromise here (even for sort algs)
    
    // gedcom and its collator available?
    Gedcom ged = getGedcom();
    if (ged!=null)
      return ged.getCollator().compare(s1,s2);
      
    // fallback to simple compare
    return s1.compareTo(s2);
  }
  
  /**
   * Whether this property is transient and therfor shouldn't
   * - act as a template
   * - be saved
   */
  public boolean isTransient() {
    return isTransient;
  }

  /**
   * A read-only attribute that can be honoured by the UI
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Adds default properties to this property
   */
  public final Property addDefaultProperties() {
    
    // only if parent set
    if (getEntity()==null) throw new IllegalArgumentException("addDefaultProperties() while getEntity()==null!");
    
    // loop
    MetaProperty[] subs = getNestedMetaProperties(MetaProperty.WHERE_DEFAULT); 
    for (int s=0; s<subs.length; s++) {
      if (getProperty(subs[s].getTag())==null)
        addProperty(subs[s].getTag(), "").addDefaultProperties();
    }

    // done    
    return this;
  }
  
  /**
   * Resolve meta property
   */
  public MetaProperty getMetaProperty() {
    return getGedcom().getGrammar().getMeta(getPath());    
  }

  /**
   * Resolve meta properties
   * @param filter
   */
  public MetaProperty[] getNestedMetaProperties(int filter) {
    return getMetaProperty().getAllNested(this, filter);
  }

  /**
   * Convert collection of properties into array
   */
  public static Property[] toArray(Collection<Property> ps) {
    return ps.toArray(new Property[ps.size()]);
  }
  
  /**
   * Accessor - private
   */
  public boolean isPrivate() {
    return isPrivate;
  }
  
  /**
   * Accessor - secret is private and unknown password
   */
  public boolean isSecret() {
    return isPrivate && getGedcom().getPassword()==Gedcom.PASSWORD_UNKNOWN;
  }
  
  /**
   * Accessor - private
   */
  public void setPrivate(boolean set, boolean recursively) {
    
    // change state
    if (recursively) {
      for (int c=0;c<getNoOfProperties();c++) {
        Property child = getProperty(c);
        child.setPrivate(set, recursively);
      }
    }
    isPrivate = set;
    
    // bookkeeping
    propagatePropertyChanged(this, getValue());
    
    // done
  }

  /**
   * Resolves end-user information about this property - by
   * default whatever is in the language resource files
   * @return name and info or null
   */
  public String getPropertyInfo() {
    return getMetaProperty().getInfo();
  }
  
  /**
   * Resolves end-user information about this property - by
   * default whatever is in the language resource files
   * @return name
   */
  public String getPropertyName() {
    return Gedcom.getName(getTag());
  }
  
  /**
   * Returns a list of property names for given list of properties
   * @param properties the properties to look at
   * @param limit max number of names followed by "..." where zero is all
   */
  public static String getPropertyNames(Iterable<? extends Property> properties, int limit) {
    
    WordBuffer result = new WordBuffer(", ");
    int i=0;
    for (Property prop : properties) {
      if (i==limit) {
        result.append("...");
        break;
      }
      result.append(prop.getPropertyName());
    }
    return result.toString();
  }
  
  /**
   * Returns a normalized list of properties for given argument. Normalization
   * means that there are no two properties in the result shareing a common 
   * containing property and no transient property.
   * @param properties properties to normalize
   * @return normalized list
   */
  public static List<Property> normalize(List<? extends Property> properties) {
    
    ArrayList<Property> result = new ArrayList<Property>(properties.size());
    
    for (Property prop : properties) {
      if (prop.isTransient())
        continue;
      // any containing in selection as well?
      Property parent = prop.getParent();
      while (parent!=null) {
        if (properties.contains(parent)) break;
        parent = parent.getParent();
      }
      if (parent==null) result.add(prop);
    }
    
    // done
    return result;
  }
  
  /**
   * Generate a string representation based on given template.
   * @see Property#format(String, PrivacyPolicy)
   */
  public String format(String format) {
    return format(format, PrivacyPolicy.PUBLIC);
  }
  
  /**
   * Generate a string representation based on given template.
   * <pre>
   *   {$t} property tag (doesn't count as matched)
   *   {$T} property name(doesn't count as matched)
   *   {$D} date as fully localized string
   *   {$y} year 
   *   {$p} place (city)
   *   {$P} place (all jurisdictions)
   *   {$v} value
   *   {$V} display value
   * </pre>
   * @param format as described
   * @param policy applied privacy policy
   * @return formatted string if at least one marker matched, "" otherwise
   */
  public String format(String format, PrivacyPolicy policy) {
  
    // match format given
    Matcher matcher = FORMAT_PATTERN.matcher(format);
    // prepare running parameters
    StringBuffer result = new StringBuffer(format.length()+20);
    int masked = 0;
    int matches = 0;
    int cursor = 0;
    // go through all matches in format
    while (matcher.find()) {
      // grab prefix
      result.append(format.substring(cursor, matcher.start()));
      // analyze next {...$x...}
      String prefix = matcher.group(1);
      char marker = format.charAt(matcher.start(2));
      String suffix = matcher.group(3);
      // translate marker into a value
      Property prop;
      String value;
      switch (marker) {
        case 'D' : { prop = getProperty("DATE"); value = (prop instanceof PropertyDate)&&prop.isValid() ? prop.getDisplayValue() : ""; break; }
        case 'y': { prop = getProperty("DATE"); value = (prop instanceof PropertyDate)&&prop.isValid() ? Integer.toString(((PropertyDate)prop).getStart().getYear()) : ""; break; }
        case 'p': { prop = getProperty("PLAC"); value = (prop instanceof PropertyPlace) ? ((PropertyPlace)prop).getCity() : ""; if (value==null) value=""; break; }
        case 'P': { prop = getProperty("PLAC"); value = (prop instanceof PropertyPlace) ? prop.getDisplayValue() : ""; break;}
        case 'v': { prop = this; value = getDisplayValue(); break; }
        case 'V': { prop = this; value = getValue(); break; }
        case 't': { prop = null; value = getTag(); break; }
        case 'T': { prop = null; value = Gedcom.getName(getTag()); break; }
        default:
          throw new IllegalArgumentException("unknown formatting marker "+marker);
      }
      // check property against policy if applicable
      if (prop!=null && policy.isPrivate(prop)) {
        // we didn't have a mask yet or the prefix is not empty? use mask
        value = (masked++==0||prefix.trim().length()>0)  ? Options.getInstance().maskPrivate : "";
      }
      // append if value is good
      if (value.length()>0) {
        result.append(prefix);
        result.append(value);
        result.append(suffix);
        if (prop!=null) matches++;
      }
      // continue
      cursor = matcher.end();
    }
    
    // append the rest
    result.append(format.substring(cursor));
    
    // got anything at all?
    return matches>0 ? result.toString() : "";
  }

  /**
   * Calculates an appropriate date that puts this property into a time context
   */
  public PropertyDate getWhen() {
    Property cursor = this;
    while (cursor!=null) {
      if (this instanceof PropertyDate)
        return (PropertyDate)this;
      if (this instanceof PropertyEvent)
        return ((PropertyEvent)this).getDate();
      cursor = cursor.getParent();
    }
    // none
    return null;
  }
  
  /**
   * Copy a property and all its sub-properties
   */
  public void copyProperties(Property[] roots, boolean useValues) throws GedcomException {
    for (Property property : roots) {
      copyProperties(property, useValues);
    }
  }

  public void copyProperties(Property root, boolean useValues) throws GedcomException {
    // create copy for prop?
    Property copy = getProperty(root.getTag(), false);
    if (copy==null) {
      copy = addProperty(root.getTag(), useValues ? root.getValue() : "");
      if (useValues&&copy instanceof PropertyXRef) try {
        ((PropertyXRef)copy).link();
      } catch (GedcomException e) {
        throw new GedcomException("Can't copy '"+root.getTag()+" "+root.getDisplayValue()+"' to "+this.getPath()+": "+e.getMessage());
      }
    }
    // loop over children of prop
    for (int i=0, j=root.getNoOfProperties(); i<j; i++) {
      Property child = root.getProperty(i);
      // apply to non-transient
      if (!child.isTransient()) 
        copy.copyProperties(child, useValues);
      // next
    }
    // done
  }
  
  protected void assertTag(String tag) {
    if (!this.tag.equals(tag)) 
      throw new Error("Tag should be "+tag+" but is "+this.tag);
  }
  
} //Property

