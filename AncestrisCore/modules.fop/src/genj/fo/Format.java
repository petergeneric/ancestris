/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.fo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

/**
 * A document format
 */
public abstract class Format {

    protected final static Logger LOG = Logger.getLogger("ancestris.fo");

    /**
     * available formats
     */
    private static Format[] formats;

    /**
     * default formats
     */
    public static final Format DEFAULT = new HTMLFormat();

    /**
     * this format
     */
    private String format;

    /**
     * file extension
     */
    private String extension;

    /**
     * whether this format requires externalization of referenced files (imagedata)
     */
    private boolean isExternalizedFiles;

    /**
     * caching for xsl templates
     */
    private Map<String, TemplatesCache> xslCache = new HashMap<>();

    /**
     * Constructor
     */
    protected Format(String format, String extension, boolean isExternalizedFiles) {
        this.format = format;
        this.extension = extension;
        this.isExternalizedFiles = isExternalizedFiles;
    }

    /**
     * Valid file extension without dot (e.g. xml, pdf, html, fo)
     *
     * @return file extension without dot of null if streaming output is not supported
     */
    public String getFileExtension() {
        return extension;
    }

    /**
     * Format Name
     */
    public String getFormat() {
        return format;
    }

    /**
     * Text representation
     */
    public String toString() {
        return format;
    }

    /**
     * equals
     */
    public boolean equals(Object that) {
        return that instanceof Format ? this.format.equals(((Format) that).format) : false;
    }

    /**
     * Externalize files resolving imagedata references
     */
    private void externalizeFiles(Document doc, File out) throws IOException {

        // got any external image file references in document?
        File[] files = doc.getImages();
        if (files.length > 0) {

            // grab image directory
            File dir = new File(out.getParentFile(), "images");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("cannot create directory " + dir);
            }

            // copy all images so they are local to the generated document
            if (dir.exists()) {
                for (File file : files) {
                    File copy = new File(dir, file.getName());
                    FileChannel from = null, to = null;
                    long count = -1;
                    try  {
                        from = new FileInputStream(file).getChannel();
                        count = from.size();
                        to = new FileOutputStream(copy).getChannel();
                        from.transferTo(0, count, to);
                        doc.setImage(file, dir.getName() + File.separator + copy.getName());
                    } catch (Throwable t) {
                        LOG.log(Level.WARNING, "Copying '" + file + "' to '" + copy + "' failed (size=" + count + ")", t);
                    } finally {
                        try {
                            to.close();
                        } catch (Throwable t) {
                        }
                        try {
                            from.close();
                        } catch (Throwable t) {
                        }
                    }
                }
            }

            // done
        }

        // done
    }

    /**
     * By default all formats support all documents
     */
    public boolean supports(Document doc) {
        return true;
    }
    
    
    private String getFopCode(Document doc) {
        try {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(doc.getDOMSource(), result);
        String xmlString = result.getWriter().toString();
        return xmlString;
        } catch(TransformerException e) {
            return e.getMessage();
        }
    }

    /**
     * Format a document
     */
    public void format(Document doc, File file) throws IOException {
        
        // Display fop content for debug purpose
        LOG.log(Level.FINE, "Le doc : " + getFopCode(doc));
        
        FileOutputStream out = null;

        // no need for stream?
        if (getFileExtension() != null) {

            if (file == null) {
                throw new IOException("Formatter requires output file");
            }

            // try to create output stream
            out = new FileOutputStream(file);

            // chance to externalize files if applicable
            if (isExternalizedFiles) {
                externalizeFiles(doc, file);
            }

        }

        // continue
        format(doc, out);
    }

    /**
     * Format a document
     */
    public void format(Document doc, OutputStream out) throws IOException {

        // close doc
        doc.close();

        // impl for out
        try {
            formatImpl(doc, out);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "unexpected expection formatting " + doc.getTitle(), t);
            if (t instanceof OutOfMemoryError) {
                throw new IOException("out of memory");
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            throw new IOException(t.getMessage(), t);
        } finally {
            try {
                out.close();
            } catch (Throwable t) {
            }
        }

        // done
    }

    /**
     * Format implementation
     */
    protected abstract void formatImpl(Document doc, OutputStream out) throws Throwable;

    /**
     * Get transformation templates for given file
     */
    private class TemplatesCache {

        Templates templates;
        long timestamp;
    }

    private Templates getTemplatesFromResource(String filename) {
        if (filename.startsWith(".")) {
            filename = filename.substring(1);
        }
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            return null;
        }
        TemplatesCache cache = xslCache.get(filename);
        if (cache != null) {
            return cache.templates;
        }

        cache = new TemplatesCache();
        cache.timestamp = 0;

        // get a new
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            cache.templates = factory.newTemplates(new StreamSource(in));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Exception reading templates from " + filename + ": " + e.getMessage());
        }

        // keep it
        xslCache.put(filename, cache);

        // done
        return cache.templates;
    }

    protected Templates getTemplates(String filename) {
        // first tries as resource
        Templates resourceTemplates = getTemplatesFromResource(filename);
        if (resourceTemplates != null) {
            return resourceTemplates;
        }
        File file = new File(filename);
        // check timestamp if we have it already
        long lastModified = file.lastModified();
        TemplatesCache cache = xslCache.get(filename);
        if (cache != null && cache.timestamp == lastModified) {
            return cache.templates;
        }

        cache = new TemplatesCache();
        cache.timestamp = lastModified;

        // get a new
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            cache.templates = factory.newTemplates(new StreamSource(file));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Exception reading templates from " + filename + ": " + e.getMessage());
        }

        // keep it
        xslCache.put(filename, cache);

        // done
        return cache.templates;
    }

    /**
     * Return format by key
     */
    public static Format getFormat(String format) {
        Format[] fs = getFormats();
        for (Format f : fs) {
            if (f.getFormat().equals(format)) {
                return f;
            }
        }
        return DEFAULT;
    }

    /**
     * Return format by extension
     */
    public static Format getFormatFromExtension(String extension) {
        Format[] fs = getFormats();
        for (Format f : fs) {
            if (f.getFileExtension().equals(extension)) {
                return f;
            }
        }
        return DEFAULT;
    }

    /**
     * Resolve available formats
     */
    public static Format[] getFormats() {

        // known?
        if (formats != null) {
            return formats;
        }

        // look 'em up
        List<Format> list = new ArrayList<>(10);
        list.add(DEFAULT);

        Iterator<Format> it = ServiceLoader.load(Format.class).iterator();
        while (it.hasNext()) {
            try {
                Format f = it.next();
                if (!list.contains(f)) {
                    list.add(f);
                }
            } catch (Throwable t) {
                if (t.getCause() != t) {
                    t = t.getCause();
                }
                LOG.log(Level.WARNING, "Encountered exception loading Format: " + t.getMessage());
            }
        }

        // keep 'em
        formats = list.toArray(new Format[list.size()]);

        // done
        return formats;
    }

}
