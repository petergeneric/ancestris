package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceFile.java
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourceFile {

    private static final Logger logger = Logger.getLogger(ResourceFile.class.getName());
    private static final String PREFIX = "Bundle";
    private static final String SUFFIX = ".properties";
    private String fromBundleName = "";
    private String toBundleName = "";
    private String directoryPath = "";
    private File DefaultLanguageFile = null;
    private ResourceStructure defaultLanguage = null;
    private ResourceStructure translatedLanguage = null;
    private ArrayList<String> content = null;
    private int not_translated;
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList());
    private TreeMap<String, ResourceStructure> resourceFiles = new TreeMap();
    private boolean modified = false;
    private boolean translationCreated = false;

    ResourceFile(String directoryPath) {
        this.directoryPath = directoryPath;
        not_translated = 0;
    }

    public void put(InputStream inputStream, String bundleName) throws IOException {
        ResourceParser resourceParser = new ResourceParser(inputStream);
        resourceParser.initParser();
        ResourceStructure resourceStructure = resourceParser.parseFile();
        resourceFiles.put(bundleName, resourceStructure);
    }

    public Set<String> getFiles() {
        return resourceFiles.keySet();
    }

    public boolean writeTo(ZipOutputStream zipOutputStream, ZipEntry zipEntry, String bundleName) throws IOException {

        if (bundleName.equals(toBundleName)) {
            if (translationCreated == false) {
                logger.log(Level.INFO, "Save file {0} ...", zipEntry.getName());
                zipOutputStream.putNextEntry(zipEntry);
                for (String key : content) {
                    String lineString = translatedLanguage.getResourceLineString(key);
                    if (lineString != null) {
                        zipOutputStream.write(lineString.getBytes());
                    }
                }
                logger.log(Level.INFO, "Done");
                return true;
            } else if (translationCreated == true && modified == true) {
                logger.log(Level.INFO, "Create file {0} ...", zipEntry.getName());
                zipOutputStream.putNextEntry(zipEntry);
                for (String key : content) {
                    String lineString = translatedLanguage.getResourceLineString(key);
                    if (lineString != null) {
                        zipOutputStream.write(lineString.getBytes());
                    }
                }
                logger.log(Level.INFO, "Done");
                return true;
            } else {
                return false;
            }
        } else {
            logger.log(Level.INFO, "Saving file {0} ...", zipEntry.getName());
            zipOutputStream.putNextEntry(zipEntry);
            for (String key : content) {
                String lineString = resourceFiles.get(bundleName).getResourceLineString(key);
                if (lineString != null) {
                    zipOutputStream.write(lineString.getBytes());
                }
            }
            logger.log(Level.INFO, "Done");
            return true;
        }
    }

    void setTranslation(Locale fromLocale, Locale toLocale) {

        if (fromLocale.getLanguage().equals("en")) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;
        }

        defaultLanguage = resourceFiles.get(fromBundleName);
        if (defaultLanguage != null) {

            content = new ArrayList(defaultLanguage.keySet());
            if (toLocale.getLanguage().equals("en")) {
                toBundleName = PREFIX + SUFFIX;
            } else {
                toBundleName = PREFIX + "_" + toLocale.getLanguage() + SUFFIX;
            }

            translatedLanguage = resourceFiles.get(toBundleName);
            if (translatedLanguage == null) {
                logger.log(Level.INFO, "Create Language file {0}", toBundleName);
                translatedLanguage = new ResourceStructure();
                resourceFiles.put(toBundleName, translatedLanguage);
                translationCreated = true;
            }

            Iterator<ResourceItem.ResourceLine> it = defaultLanguage.iterator();
            while (it.hasNext()) {
                ResourceItem.ResourceLine line = it.next();
                if (translatedLanguage.getLine(line.getKey()) != null) {
                    not_translated = Math.max(0, not_translated - 1);
                }
            }
        } else {
            logger.log(Level.SEVERE, "No default language for directory {0}", this.directoryPath);
        }
    }

    public boolean isTranslated() {
        return not_translated == 0;
    }

    public int getLineCount() {
        return defaultLanguage.size();
    }

    public String getLine(int i) {
        ResourceItem.ResourceLine line = defaultLanguage.getLine(content.get(i));
        String comment = line.getComment();
        String value = line.getValue();

        return (comment == null ? "" : comment) + (value == null ? "" : value);
    }

    public String getLineTranslation(int i) {
        ResourceItem.ResourceLine line = translatedLanguage.getLine(content.get(i));

        return line == null ? "" : line.getValue();
    }

    public void setLineTranslation(int i, String s) {
        ResourceItem.ResourceLine old = translatedLanguage.getLine(content.get(i));
        ResourceItem.PropertyComment comment = defaultLanguage.getLine(content.get(i)).getPropertyComment();
        ResourceItem.PropertyKey key = defaultLanguage.getLine(content.get(i)).getPropertyKey();
        ResourceItem.PropertyValue value = new ResourceItem.PropertyValue(s);
        if (old == null) {
            not_translated--;
            translatedLanguage.put(key, value, comment);
        } else {
            translatedLanguage.put(key, value, comment);
        }
        modified = true;
        fire(content.get(i), old, s);
    }

    public int getLineState(int i) {
        if (translatedLanguage.getLine(content.get(i)) != null) {
            String from = defaultLanguage.getLine(content.get(i)).getValue();
            String to = translatedLanguage.getLine(content.get(i)) != null ? translatedLanguage.getLine(content.get(i)).getValue() : "";
            if (from.equalsIgnoreCase(to)) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    public File getDefaultBundleFile() {
        return DefaultLanguageFile;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.remove(pcl);
    }

    private void fire(String propertyName, Object old, Object nue) {
        //Passing 0 below on purpose, so you only synchronize for one atomic call
        @SuppressWarnings(value = "unchecked")
        PropertyChangeListener[] pcls = (PropertyChangeListener[]) listeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
        }
    }
}
