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

import genj.util.EnvironmentChecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The grammar of Gedcom files initialized from grammar.xml
 */
public class Grammar {

  private final static Logger LOG = Logger.getLogger("genj.gedcom"); // make sure logger is initialized before other statics
  
  /** singleton */
  public final static Grammar 
    V55 = new Grammar("contrib/LDS/gedcom-5-5.xml"),
    V551 = new Grammar("contrib/LDS/gedcom-5-5-1.xml");
  
  /** gedcom version */
  private String version;
  
  /** meta roots */
  private Map tag2root = new HashMap();
  
  /**
   * Singleton Constructor
   */
  private Grammar(String descriptor) {
    
    SAXParser parser;
    
    try {
      parser = SAXParserFactory.newInstance().newSAXParser();
    } catch (Throwable t) {
      Gedcom.LOG.log(Level.SEVERE, "couldn't setup SAX parser", t);
      throw new Error(t);
    }

    try {
      // try to load through classloader, then disk
      InputStream in = getClass().getResourceAsStream("/"+descriptor);
      if (in!=null) {
          LOG.info("Loading grammar through classloader");
      } else {
        in = new FileInputStream(new File(EnvironmentChecker.getProperty(this, "user.dir", ".", "current directory for grammar"),descriptor));
      }

      // parse it
      parser.parse(new InputSource(new InputStreamReader(in)), new Parser());
    } catch (Throwable t) {
      Gedcom.LOG.log(Level.SEVERE, "couldn't parse grammar", t);
      throw new Error(t);
    }

    // done
  }
  
  /**
   * Version access
   */
  public String getVersion() {
    return version;
  }
  
  /**
   * All used paths for given type 
   * @param etag tag of entity or null for all
   */
  public TagPath[] getAllPaths(String etag, Class property) {
    return getPathsRecursively(etag, property);
  }
  
  private TagPath[] getPathsRecursively(String etag, Class property) {
    
    // prepare result
    List result = new ArrayList();
    // loop through roots
    for (Iterator it=tag2root.values().iterator(); it.hasNext(); ) {
      MetaProperty root = (MetaProperty)it.next();
      String tag = root.getTag();
      if (etag==null||tag.equals(etag))
        getPathsRecursively(root, property, new TagPath(tag), result);
    }
    // done
    return TagPath.toArray(result);
  }

  private void getPathsRecursively(MetaProperty meta, Class property, TagPath path, Collection result) {
  
    // something worthwhile to dive into?
    if (!meta.isInstantiated) 
      return;
    
    // type match?
    if (property.isAssignableFrom(meta.getType())) 
      result.add(path);
      
    // recurse into
    for (Iterator it=meta.nested.iterator();it.hasNext();) {
      MetaProperty nested = (MetaProperty)it.next();
      getPathsRecursively(nested, property, new TagPath(path, nested.getTag()), result);
    }
    
    // done
  }

  /**
   * Get a MetaProperty by path
   */
  public MetaProperty getMeta(TagPath path) {
    return getMeta(path, true);
  }

  /**
   * Get a MetaProperty by path
   */
  public MetaProperty getMeta(TagPath path, boolean persist) {
    return getMetaRecursively(path, persist);
  }
  
  /**
   * Get a MetaProperty by path
   */
  /*package*/ MetaProperty getMetaRecursively(TagPath path, boolean persist) {
    
    String tag = path.get(0);
    
    MetaProperty root = (MetaProperty)tag2root.get(tag);
    
    // something we didn't know about yet?
    if (root==null) {
      root = new MetaProperty(this, tag, new HashMap(), false);
      tag2root.put(tag, root);
    }
    
    // recurse into      
    return root.getNestedRecursively(path, 1, persist);
  }

  /**
   * Grammar Parser
   */
  private class Parser extends DefaultHandler {
    
    private Stack stack = null;
    
    /* element callback */
    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {
      
      // in case we don't already have a stack running this better be GEDCOM
      if (stack==null) {
        if (!"GEDCOM".equals(qName)) 
          throw new RuntimeException("expected GEDCOM");
        version = attributes.getValue("version");
        if (version==null)
          throw new RuntimeException("expected GEDCOM version");
        stack = new Stack();
        return;
      }
      
      // grab attributes
      Map properties = new HashMap();
      for (int i=0,j=attributes.getLength();i<j;i++)
        properties.put(attributes.getQName(i), attributes.getValue(i));
      
      // create a meta property for element
      MetaProperty meta;
      try {
        meta = new MetaProperty(Grammar.this, qName, properties, true);
      } catch (Throwable t) {
        LOG.log(Level.SEVERE, "Problem instantiating meta property for "+qName+" with "+properties, t.getCause()!=null?t.getCause():t);
        throw new Error("Can't parse Gedcom Grammar");
      }

      // a property root (a.k.a entity) or a nested one?
      if (stack.isEmpty())  {
        meta.isInstantiated = true;
        tag2root.put(qName, meta);
      } else
        ((MetaProperty)stack.peek()).addNested(meta);
        
      // push on stack
      stack.push(meta);
      
      // done
    }

    /*/element callback */
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      // end of gedcom or normal pop?
      if ("GEDCOM".equals(qName))
        stack = null;
      else
        stack.pop();
    }
  } //Parser
  
} //Grammar
