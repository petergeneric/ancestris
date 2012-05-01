package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceFile.java
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ancestris.trancestris.resources.ResourceItem.ResourceLine;
import org.openide.util.NbBundle;

public class ResourceFile {

    private static final Logger logger = Logger.getLogger(ResourceFile.class.getName());
    private static final String PREFIX = "Bundle";
    private static final String SUFFIX = ".properties";
    private String fromBundleName = "";
    private String toBundleName = "";
    private String toModifiedName = "";
    private String directoryPath = "";
    private ResourceStructure defaultLanguage = null;
    private ResourceStructure fromLanguage = null;
    private ResourceStructure toLanguage = null;
    private ArrayList<String> content = null;
    private int not_translated;
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());
    private TreeMap<String, ResourceStructure> resourceFiles = new TreeMap<String, ResourceStructure>();
    private boolean modified = false;
    private boolean translationCreated = false;

    ResourceFile(String directoryPath) {
        this.directoryPath = directoryPath;
        not_translated = 0;
    }

    public void put(ZipEntry zipEntry, InputStream inputStream, String bundleName) throws IOException {
        ResourceParser resourceParser = new ResourceParser(inputStream);
        resourceParser.initParser();
        ResourceStructure resourceStructure = resourceParser.parseFile();
        resourceStructure.setZipEntry(zipEntry);
        resourceFiles.put(bundleName, resourceStructure);
    }

    public Set<String> getFiles() {
        return resourceFiles.keySet();
    }

    public void writeTo(ZipOutputStream zipOutputStream) throws IOException {
        if (defaultLanguage != null) {
            for (String bundleName : new TreeSet<String>(getFiles())) {
                if (bundleName.equals(toBundleName)) {
                    // The file has been modified
                    if (modified == true) {
                        boolean fileAlreadyModified = false;
                        Calendar c = Calendar.getInstance();
                        ZipEntry zipEntry = null;

                        // File previously modified ?
                        for (String modifiedBundle : getFiles()) {
                            if (modifiedBundle.equals(toModifiedName)) {
                                fileAlreadyModified = true;
                            }
                        }

                        if (fileAlreadyModified == false) {
                            logger.log(Level.INFO, "Create file {0} ...", this.directoryPath + "/" + toModifiedName);
                            zipEntry = new ZipEntry(this.directoryPath + "/" + toModifiedName);
                            zipEntry.setTime(c.getTimeInMillis());
                            zipOutputStream.putNextEntry(zipEntry);
                            ResourceStructure resourceStructure = new ResourceStructure();
                            resourceStructure.setZipEntry(zipEntry);
                            resourceFiles.put(toModifiedName, resourceStructure);
                            logger.log(Level.INFO, "Done");
                        }

                        if (translationCreated == false) {
                            logger.log(Level.INFO, "Update file {0} ...", this.directoryPath + "/" + bundleName);

                        } else {
                            logger.log(Level.INFO, "Create file {0} ...", this.directoryPath + "/" + bundleName);
                        }
                        zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                        zipEntry.setTime(c.getTimeInMillis());
                        zipOutputStream.putNextEntry(zipEntry);
                        toLanguage.setZipEntry(zipEntry);
                        for (String key : content) {
                            String lineString = toLanguage.getResourceLineString(key);
                            if (lineString != null) {
                                zipOutputStream.write(lineString.getBytes());
                            }
                        }
                        logger.log(Level.INFO, "Done");
                    } else if (translationCreated == false) {
                        // the file already exits
                        logger.log(Level.INFO, "Save file {0} ...", this.directoryPath + "/" + bundleName);
                        ZipEntry zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                        zipEntry.setTime(toLanguage.getZipEntry().getTime());
                        zipOutputStream.putNextEntry(zipEntry);
                        for (String key : content) {
                            String lineString = toLanguage.getResourceLineString(key);
                            if (lineString != null) {
                                zipOutputStream.write(lineString.getBytes());
                            }
                        }
                        logger.log(Level.INFO, "Done");
                    }
                } else {
                    logger.log(Level.INFO, "Saving file {0} ...", this.directoryPath + "/" + bundleName);
                    ResourceStructure current = resourceFiles.get(bundleName);
                    ZipEntry zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                    zipEntry.setTime(current.getZipEntry().getTime());

                    zipOutputStream.putNextEntry(zipEntry);
                    for (String key : content) {
                        String lineString = current.getResourceLineString(key);
                        if (lineString != null) {
                            zipOutputStream.write(lineString.getBytes());
                        }
                    }
                    logger.log(Level.INFO, "Done");
                }
            }
        }
    }

    public void saveTranslation(ZipOutputStream zipOutputStream) throws IOException {
        if (defaultLanguage != null) {
            for (String bundleName : getFiles()) {
                if (bundleName.equals(toBundleName)) {
                    for (String modifiedBundle : getFiles()) {
                        if (modifiedBundle.equals(toModifiedName)) {
                            logger.log(Level.INFO, "Add file {0} ...", this.directoryPath + "/" + bundleName);
                            ZipEntry zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                            zipEntry.setTime(toLanguage.getZipEntry().getTime());
                            zipOutputStream.putNextEntry(zipEntry);
                            for (String key : content) {
                                String lineString = toLanguage.getResourceLineString(key);
                                if (lineString != null) {
                                    zipOutputStream.write(lineString.getBytes());
                                }
                            }
                            logger.log(Level.INFO, "Done");
                        }
                    }
                }
            }
        }
    }

    void setTranslation(Locale fromLocale, Locale toLocale) {

        if (fromLocale.getLanguage().equals("en")) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;
        }

        // load Default Language
        defaultLanguage = resourceFiles.get(PREFIX + SUFFIX);

        if (fromBundleName.equals(PREFIX + SUFFIX) == true) {
            fromLanguage = defaultLanguage;
        } else {
            fromLanguage = resourceFiles.get(fromBundleName);
        }

        if (defaultLanguage != null) {

            content = new ArrayList<String>(defaultLanguage.keySet());

            // Load translayed language
            if (toLocale.getLanguage().equals("en")) {
                toBundleName = PREFIX + SUFFIX;
                toModifiedName = "modified.en";
            } else {
                toBundleName = PREFIX + "_" + toLocale.getLanguage() + SUFFIX;
                toModifiedName = "modified." + toLocale.getLanguage();
            }

            toLanguage = resourceFiles.get(toBundleName);
            if (toLanguage == null) {
                logger.log(Level.INFO, "Create Language file {0}", this.directoryPath + "/" + toBundleName);
                toLanguage = new ResourceStructure();
                resourceFiles.put(toBundleName, toLanguage);
                translationCreated = true;
            }

            Iterator<ResourceItem.ResourceLine> it = defaultLanguage.iterator();
            not_translated = getLineCount();
            while (it.hasNext()) {
                ResourceItem.ResourceLine line = it.next();
                if (line.getValue() != null && !line.getValue().isEmpty()) {
                    String comment = line.getComment();
                    if (comment != null && comment.contains("NOI18N")) {
                        not_translated = Math.max(0, not_translated - 1);
                    } else if (toLanguage.getLine(line.getKey()) != null) {
                        not_translated = Math.max(0, not_translated - 1);
                    }
                } else {
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

    public int getTranslatedPercent() {
        return (int) ((float) (getLineCount() - not_translated) / (float) getLineCount() * 100);
    }

    public int getLineCount() {
        if (defaultLanguage != null) {
            return defaultLanguage.size();
        } else {
            return 1;
        }
    }

    public String getLine(int i) {
        ResourceItem.ResourceLine line = null;
        String value = null;

        if (defaultLanguage != null) {
            if (fromLanguage != null) {
                line = fromLanguage.getLine(content.get(i));
            }
            // fall back to default language if not found (ie translate from fr to br)
            if (line == null) {
                line = defaultLanguage.getLine(content.get(i));
            }

            value = line.getValue();
            return (value == null ? "" : value);
        }
        return (value == null ? NbBundle.getMessage(ResourceFile.class, "No-Default-Bundle-Error") : value);
    }

    public String getLineComment(int i) {
        ResourceItem.ResourceLine line = null;
        String comment = null;

        if (defaultLanguage != null) {
            if (fromLanguage != null) {
                line = fromLanguage.getLine(content.get(i));
            }
            // fall back to default language if not found (ie translate from fr to br)
            if (line == null) {
                line = defaultLanguage.getLine(content.get(i));
            }

            comment = line.getComment();
        }
        return (comment == null ? "" : comment);
    }

    public String getLineTranslation(int i) {
        ResourceItem.ResourceLine line = null;
        if (defaultLanguage != null) {
            line = toLanguage.getLine(content.get(i));

            if (line == null) {
                if (fromLanguage != null) {
                    line = fromLanguage.getLine(content.get(i));
                }
                // fall back to default language if not found (ie translate from fr to br)
                if (line == null) {
                    line = defaultLanguage.getLine(content.get(i));
                }

                line = defaultLanguage.getLine(content.get(i));
            }
        }

        return line != null ? line.getValue() : "";
    }

    public void setLineTranslation(int i, String s) {
        ResourceItem.ResourceLine old = toLanguage.getLine(content.get(i));
        ResourceItem.PropertyComment comment = defaultLanguage.getLine(content.get(i)).getPropertyComment();
        ResourceItem.PropertyKey key = defaultLanguage.getLine(content.get(i)).getPropertyKey();
        ResourceItem.PropertyValue value = new ResourceItem.PropertyValue(s);
        if (old == null) {
            not_translated--;
        }
        if (key != null) {
            toLanguage.put(key, value, comment);
        }
        modified = true;
        fire(content.get(i), old, s);
    }

    public int getLineState(int i) {
        if (defaultLanguage != null) {
            String comment = defaultLanguage.getLine(content.get(i)).getComment();
            if (comment != null && comment.contains("NOI18N")) {
                // the line shall not be translated
                return 1;
            } else {
                ResourceLine toLine = toLanguage.getLine(content.get(i));
                if (toLine != null) {
                    String fromValue = null;

                    if (fromLanguage != null && fromLanguage.getLine(content.get(i)) != null) {
                        fromValue = fromLanguage.getLine(content.get(i)).getValue();
                    } else {
                        fromValue = defaultLanguage.getLine(content.get(i)).getValue();
                    }

                    if (fromValue != null) {
                        String to = toLine.getValue() != null ? toLine.getValue() : "";
                        if (fromValue.equalsIgnoreCase(to)) {
                            // From and to line are the same sholud be translated
                            return -1;
                        } else {
                            // The line seems translated
                            return 1;
                        }
                    } else {
                        // should not append
                        return -1;
                    }
                } else {
                    // The line shall be translated
                    return 0;
                }
            }
        } else {
            return -1;
        }
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
        PropertyChangeListener[] pcls = listeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
        }
    }

    public String search(String expression, boolean fromLocale) {
        Pattern p = Pattern.compile(expression);

        Iterator<ResourceLine> iterator = null;
        if (fromLocale == true) {
            iterator = fromLanguage.iterator();
        } else {
            iterator = toLanguage.iterator();
        }

        while (iterator.hasNext()) {
            ResourceLine resourceLine = iterator.next();
            String value = resourceLine.getValue();
            if (value != null && p.matcher(value).find() == true) {
                return directoryPath;
            }
        }

        return null;
    }
}
