/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.util.EnvironmentChecker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The grammar of Gedcom files initialized from grammar.xml
 */
public class Grammar {

    private final static Logger LOG = Logger.getLogger("ancestris.gedcom"); // make sure logger is initialized before other statics

    /**
     * singleton
     */
    public final static Grammar V55 = new Grammar("contrib/LDS/gedcom-5-5.xml"),
            V551 = new Grammar("contrib/LDS/gedcom-5-5-1.xml"),
            v70 = new Grammar("contrib/LDS/gedcom-7-0-5.xml");

    public static final String GRAMMAR55 = "5.5";
    public static final String GRAMMAR551 = "5.5.1";
    public static final String GRAMMAR70 = "7.0.5";

    /**
     * gedcom version
     */
    private String version;

    /**
     * meta roots
     */
    private Map<String, MetaProperty> tag2root = new HashMap<>();

    /**
     * Singleton Constructor
     */
    private Grammar(String descriptor) {

        SAXParser parser;

        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException | SAXException t) {
            Gedcom.LOG.log(Level.SEVERE, "couldn't setup SAX parser", t);
            throw new Error(t);
        }

        try {
            // try to load through classloader, then disk
            InputStream in = getClass().getResourceAsStream("/" + descriptor);
            if (in != null) {
                LOG.info("Loading grammar through classloader");
            } else {
                try {
                    in = new FileInputStream(new File(EnvironmentChecker.getProperty("user.dir", ".", "current directory for grammar"), descriptor));
                } catch (FileNotFoundException e) {
                    /* when executing dev\geo\src\tst\genj\geo\App.java  in Eclipse */
                    in = new FileInputStream(new File("../app/" + descriptor));
                }
            }

            // parse it
            parser.parse(new InputSource(new InputStreamReader(in)), new Parser());
            
            // Second pass to update Super references.
            List<MetaProperty> alreadyVisited = new ArrayList<>();
            for (MetaProperty root : tag2root.values()){
                root.redoSuper(alreadyVisited);
            }
            
        } catch (IOException | SAXException t) {
            Gedcom.LOG.log(Level.SEVERE, "couldn't parse grammar", t);
            throw new Error(t);
        }
        
        

        // done
    }

    /**
     * Version access.
     *
     * @return Version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * All used paths for given type
     *
     * @param etag tag of entity or null for all
     * @param property beginning tag
     * @return all possible path
     */
    public TagPath[] getAllPaths(String etag, Class<? extends Property> property) {
        return getPathsRecursively(etag, property);
    }

    private TagPath[] getPathsRecursively(String etag, Class<? extends Property> property) {
        LOG.log(Level.FINE, "Enter Tag Entity : {0}", etag);

        // prepare result
        List<TagPath> result = new ArrayList<>();
        // loop through roots
        for (MetaProperty root : tag2root.values()) {
            String tag = root.getTag();
            if (etag == null || tag.equals(etag)) {
                getPathsRecursively(root, property, new TagPath(tag), result);
            }
        }
        // done
        return TagPath.toArray(result);
    }

    private void getPathsRecursively(MetaProperty meta, Class<? extends Property> property, TagPath path, Collection<TagPath> result) {
        LOG.log(Level.FINE, "TagPath : {0}", path.toString());

        // something worthwhile to dive into?
        if (!meta.isInstantiated()) {
            return;
        }

        // type match?
        if (property.isAssignableFrom(meta.getType())) {
            result.add(path);
        }

        // recurse into
        for (MetaProperty nested : meta.nested) {
            LOG.log(Level.FINE, "Tag nested : {0}", nested.getTag());
            //Stop recursive if tag already visited
            if (!path.contains(nested.getTag())) {
                LOG.log(Level.FINE, "Go recursive : {0}", nested.getTag());
                getPathsRecursively(nested, property, new TagPath(path, nested.getTag()), result);
            }
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

        MetaProperty root = tag2root.get(tag);

        // something we didn't know about yet?
        if (root == null) {
            root = new MetaProperty(this, tag, new HashMap<>(), false);
            tag2root.put(tag, root);
        }

        // recurse into      
        return root.getNestedRecursively(path, 1, persist);
    }

    /**
     * Grammar Parser
     */
    private class Parser extends DefaultHandler {

        private Stack<MetaProperty> stack = null;

        /* element callback */
        @Override
        public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {

            // in case we don't already have a stack running this better be GEDCOM
            if (stack == null) {
                if (!"GEDCOM".equals(qName)) {
                    throw new RuntimeException("expected GEDCOM");
                }
                version = attributes.getValue("version");
                if (version == null) {
                    throw new RuntimeException("expected GEDCOM version");
                }
                stack = new Stack<>();
                return;
            }

            // grab attributes
            Map<String, String> properties = new HashMap<>();
            for (int i = 0, j = attributes.getLength(); i < j; i++) {
                properties.put(attributes.getQName(i), attributes.getValue(i));
            }

            // create a meta property for element
            MetaProperty meta;
            try {
                meta = new MetaProperty(Grammar.this, qName, properties, true);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Problem instantiating meta property for " + qName + " with " + properties, t.getCause() != null ? t.getCause() : t);
                throw new Error("Can't parse Gedcom Grammar");
            }

            // a property root (a.k.a entity) or a nested one?
            if (stack.isEmpty()) {
                LOG.log(Level.FINE, "Instantiate meta :" + meta.getName() + " " + meta.getTag());
                meta.setIsInstantiated(true);
                tag2root.put(qName, meta);
            } else {
                (stack.peek()).addNested(meta);
            }

            // push on stack
            stack.push(meta);

            // done
        }

        /*/element callback */
        public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
            // end of gedcom or normal pop?
            if ("GEDCOM".equals(qName)) {
                stack = null;
            } else {
                stack.pop();
            }
        }
    } //Parser

    /**
     * check if a tag path is valid
     */
    public boolean isValid(TagPath path) {

        String tag = path.get(0);
        MetaProperty root = tag2root.get(tag);
        if (root == null) {
            return false;
        }

        for (int i = 1; i < path.length(); i++) {
            tag = path.get(i);
            if (!root.allows(tag)) {
                return false;
            }
            root = root.getNested(tag, false);
        }

        return true;
    }

} //Grammar
