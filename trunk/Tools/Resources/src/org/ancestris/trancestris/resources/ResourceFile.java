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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class ResourceFile {

    private static final String PREFIX = "Bundle";
    private static final String SUFFIX = ".properties";
    private String fromBundleName = "";
    private String toBundleName = "";
    private File DefaultLangageFile = null;
    private ResourceStructure defaultLangage = null;
    private ResourceStructure translatedLangage = null;
    private ArrayList<String> content = null;
    private int not_translated;
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList());
    private TreeMap<String, ResourceStructure> resourceFiles = new <String, ResourceStructure>TreeMap();

    ResourceFile() {
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

    public void writeTo(OutputStream outputStream, String bundleName) throws IOException {

        ResourceStructure resourceStructure;
        if (bundleName.equals(toBundleName)) {
            resourceStructure = translatedLangage;
        } else {
            resourceStructure = resourceFiles.get(bundleName);
        }
        
        outputStream.write(resourceStructure.getBundleString().getBytes());
    }

    void setTranslation(Locale fromLocale, Locale toLocale) {

        if (fromLocale.getLanguage().equals("en")) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;
        }

        defaultLangage = resourceFiles.get(fromBundleName);
        if (defaultLangage != null) {

            content = new<String> ArrayList(defaultLangage.keySet());
            if (toLocale.getLanguage().equals("en")) {
                toBundleName = PREFIX + SUFFIX;
            } else {
                toBundleName = PREFIX + "_" + toLocale.getLanguage() + SUFFIX;
            }

            translatedLangage = resourceFiles.get(toBundleName);
            if (translatedLangage == null) {
                translatedLangage = new ResourceStructure();
                resourceFiles.put(toBundleName, translatedLangage);
            }

            Iterator<ResourceItem.ResourceLine> it = defaultLangage.iterator();
            while (it.hasNext()) {
                ResourceItem.ResourceLine line = it.next();
                if (translatedLangage.getLine(line.getKey()) != null) {
                    not_translated = Math.max(0, not_translated - 1);
                }
            }
        }
    }

    public boolean isTranslated() {
        return not_translated == 0;
    }

    public int getLineCount() {
        return defaultLangage.size();
    }

    public String getLine(int i) {
        ResourceItem.ResourceLine line = defaultLangage.getLine(content.get(i));
        String comment = line.getComment();
        String value = line.getValue();
        
        return line.getComment() + "\n" + line.getValue();
    }

    public String getLineTranslation(int i) {
        ResourceItem.ResourceLine line = translatedLangage.getLine(content.get(i));

        return line == null ? "" : line.getValue();
    }

    public void setLineTranslation(int i, String s) {
        ResourceItem.ResourceLine old = translatedLangage.getLine(content.get(i));
        ResourceItem.ResourceLine line = null;
        if (old == null) {
            not_translated--;
            translatedLangage.put(content.get(i), s, "");
        } else {
            translatedLangage.put(old.getKey(), s, old.getComment());
            line = new ResourceItem.ResourceLine(old.getPropertyKey(), new ResourceItem.PropertyValue(s), old.getPropertyComment());
        }


        fire(content.get(i), old, s);
    }

    public int getLineState(int i) {
        if (translatedLangage.getLine(content.get(i)) != null) {
            String from = defaultLangage.getLine(content.get(i)).getValue();
            String to = translatedLangage.getLine(content.get(i)) != null ? translatedLangage.getLine(content.get(i)).getValue() : "";
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
        return DefaultLangageFile;
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
