/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.resources;

/**
 *
 * @author dominique
 */
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Element structure for one .properties file tightly
 * bound with that file's document.
 *
 * @author Petr Jiricka
 */
public class ResourceStructure {

    private LinkedHashMap<String, ResourceItem.ResourceLine> resourceLines;
    // Zip entry associated with that file.
    private ZipEntry zipEntry;
    private static final Logger logger = Logger.getLogger(ResourceStructure.class.getName());

    /** Constructs a new PropertiesStructure for the given bounds and ResourceLines. */
    public ResourceStructure(LinkedHashMap<String, ResourceItem.ResourceLine> resourceLines) {
        this.resourceLines = resourceLines;
    }

    public ResourceStructure() {
        this.resourceLines = new LinkedHashMap<String, ResourceItem.ResourceLine>();
    }

    public String getBundleString() {
        StringBuilder sb = new StringBuilder();
        for (ResourceItem.ResourceLine line : resourceLines.values()) {
            sb.append(line.getResourceLine());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ResourceItem.ResourceLine item : resourceLines.values()) {
            sb.append(item.toString());
            sb.append("- - -\n");                                       //NOI18N
        }

        return sb.toString();
    }

    public ResourceItem.ResourceLine getLine(String key) {
        return resourceLines.get(key);
    }

    public String getResourceLineString(String key) {
        ResourceItem.ResourceLine line = resourceLines.get(key);
        if (line != null) {
            return line.getResourceLine();
        } else {
            return null;
        }
    }

    public Set<String> keySet() {

        return resourceLines.keySet();
    }

    public boolean put(ResourceItem.PropertyKey key, ResourceItem.PropertyValue value, ResourceItem.PropertyComment comment) {
        // construct the new element
        ResourceItem.ResourceLine line = new ResourceItem.ResourceLine(key, value, comment);

        resourceLines.put(key.getValue(), line);
        return true;

    }

    /** Returns iterator thropugh all resourceLines, including empty ones */
    public Iterator<ResourceItem.ResourceLine> iterator() {
        return resourceLines.values().iterator();
    }

    public int size() {
        return resourceLines.size();
    }
    
    public ZipEntry getZipEntry () {
        return this.zipEntry;
    }
    
    public  void setZipEntry (ZipEntry zipEntry) {
        this.zipEntry = zipEntry;
    }
}
