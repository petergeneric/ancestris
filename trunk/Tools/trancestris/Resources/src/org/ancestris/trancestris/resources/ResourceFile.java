package org.ancestris.trancestris.resources;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceFile.java
import java.awt.Color;
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
    private Locale fromLocale = null;
    private Locale toLocale = null;
    private ResourceStructure refLanguage = null;
    private ResourceStructure defaultLanguage = null;
    private ResourceStructure fromLanguage = null;
    private ResourceStructure toLanguage = null;
    private ArrayList<String> content = null;
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());
    private TreeMap<String, ResourceStructure> resourceRefFiles = new TreeMap<String, ResourceStructure>();
    private TreeMap<String, ResourceStructure> resourceFiles = new TreeMap<String, ResourceStructure>();
    private boolean modified = false;
    private boolean translationCreated = false;

    private int translation_none;         // counts lines where a translation should be made and none is found (red)
    private int translation_update;       // counts lines where a translation exists but english has changed since (green)
    private int translation_same;         // counts lines where english text and translation are the same (blue)
    
    public static Color TR_MISSING_COL = Color.RED;
    public static Color TR_UPDATE_COL = new Color(29, 152, 00);
    public static Color TR_SAME_COL = Color.BLUE;

    ResourceFile(String directoryPath) {
        this.directoryPath = directoryPath;
        translation_none = 0;
        translation_update = 0;
        translation_same = 0;
    }

    public void put(ZipEntry zipEntry, InputStream inputStream, String bundleName) throws IOException {
        ResourceParser resourceParser = new ResourceParser(inputStream);
        resourceParser.initParser();
        ResourceStructure resourceStructure = resourceParser.parseFile();
        resourceStructure.setZipEntry(zipEntry);
        resourceFiles.put(bundleName, resourceStructure);
    }

    public void putRef(ZipEntry zipEntry, InputStream inputStream, String bundleName) throws IOException {
        ResourceParser resourceParser = new ResourceParser(inputStream);
        resourceParser.initParser();
        ResourceStructure resourceStructure = resourceParser.parseFile();
        resourceStructure.setZipEntry(zipEntry);
        resourceRefFiles.put(bundleName, resourceStructure);
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
                            logger.log(Level.FINE, "Done - ResourceFile-writeTo - 1");
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
                        logger.log(Level.FINE, "Done - ResourceFile-writeTo - 2");
                    } else if (translationCreated == false) {
                        // the file already exits
                        logger.log(Level.FINE, "(equal) - Save file {0} ...", this.directoryPath + "/" + bundleName);
                        ZipEntry zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                        zipEntry.setTime(toLanguage.getZipEntry().getTime());
                        zipOutputStream.putNextEntry(zipEntry);
                        for (String key : content) {
                            String lineString = toLanguage.getResourceLineString(key);
                            if (lineString != null) {
                                zipOutputStream.write(lineString.getBytes());
                            }
                        }
                        logger.log(Level.FINE, "Done - ResourceFile-writeTo - 3");
                    }
                } else {
                    logger.log(Level.FINE, "(different) Saving file {0} ...", this.directoryPath + "/" + bundleName);
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
                    logger.log(Level.FINE, "Done - ResourceFile-writeTo - 4");
                }
            }
        }
    }

    boolean hasTranslation() {
        if (defaultLanguage != null) {
            for (String bundleName : getFiles()) {
                if (bundleName.equals(toBundleName)) {
                    for (String modifiedBundle : getFiles()) {
                        if (modifiedBundle.equals(toModifiedName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean cleanTranslation() {
        if (defaultLanguage != null) {
            for (String bundleName : getFiles()) {
                if (bundleName.equals(toBundleName)) {
                    for (String modifiedBundle : getFiles()) {
                        if (modifiedBundle.equals(toModifiedName)) {
                            resourceFiles.remove(toModifiedName);
                            logger.log(Level.INFO, "Done - ResourceFile (" + directoryPath + ") -cleaned resource " + toModifiedName);
                            modified = false;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean saveTranslation(ZipOutputStream zipOutputStream) throws IOException {
        if (defaultLanguage != null) {
            for (String bundleName : getFiles()) {
                if (bundleName.equals(toBundleName)) {
                    for (String modifiedBundle : getFiles()) {
                        if (modifiedBundle.equals(toModifiedName)) {
                            logger.log(Level.FINE, "Add file {0} ...", this.directoryPath + "/" + bundleName);
                            ZipEntry zipEntry = new ZipEntry(this.directoryPath + "/" + bundleName);
                            zipEntry.setTime(toLanguage.getZipEntry().getTime());
                            zipOutputStream.putNextEntry(zipEntry);
                            for (String key : content) {
                                String lineString = toLanguage.getResourceLineString(key);
                                if (lineString != null) {
                                    zipOutputStream.write(lineString.getBytes());
                                }
                            }
                            logger.log(Level.FINE, "Done - ResourceFile-saveTranslation");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    void setTranslation(Locale fromLocale, Locale toLocale) {

        this.fromLocale = fromLocale;
        this.toLocale = toLocale;
        if (fromLocale.getLanguage().equals("en")) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;
        }

        // load Referenced Language
        refLanguage = resourceRefFiles.get(PREFIX + SUFFIX);
        
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
            Pattern p = Pattern.compile("NOI18N$");
            
            // Loop on lines
            while (it.hasNext()) {
                ResourceItem.ResourceLine line = it.next();
                int state = getLineState(line);
                switch (state) {
                    case 3:  // translation is missing
                        translation_none++;
                        break;
                        
                    case 2: // translation is to be updated
                        translation_update++;
                        break;
                        
                    case 1: // translation is the same, should probably be updated
                        translation_same++;
                        break;
                        
                    case 0: // line appears to be properly translated
                    default:
                        break;
                }
            }
        } else {
            logger.log(Level.SEVERE, "No default language for directory {0}", this.directoryPath);
        }
    }

    public boolean isTranslated() {
        int total = translation_none + translation_update + translation_same;
        return total == 0;
    }

    public Color getColor() {
        if (translation_none != 0) {
            return TR_MISSING_COL;
        }
        if (translation_update != 0) {
            return TR_UPDATE_COL;
        }
        if (translation_same != 0) {
            return TR_SAME_COL;
        }
        return Color.BLACK;
    }
    
    public int getTranslatedPercent() {
        int total = translation_none + translation_update;
        logger.log(Level.INFO, "{0}: Lines count {1} not translated {2}", new Object[]{directoryPath, getLineCount(), total});
        return (int) (((float) (getLineCount() - total) / (float) getLineCount()) * 100);
    }

    public int getLineCount() {
        if (defaultLanguage != null) {
            return defaultLanguage.size();
        } else {
            return 1;
        }
    }

    public int getTranslatedLineCount () {
        int total = translation_none + translation_update;
        return getLineCount() - total;
    }
    
    public String getRefValue(int i) {
        if (refLanguage != null) {
            return refLanguage.getLine(content.get(i)).getValue();
        }
        return null;
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
        if (key != null) {
            int oldState = getLineState(defaultLanguage.getLine(content.get(i)));
            toLanguage.put(key, value, comment);
            int newState = getLineState(defaultLanguage.getLine(content.get(i)));
            if (oldState != newState) {
                switch (newState) {
                    case 3:  // translation is now missing
                        translation_none++;
                        if (oldState == 1) {
                            translation_same--;
                        } else if (oldState == 2) {
                            translation_update--;
                        }
                        break;

                    case 2: // translation is to be updated
                        translation_update++;
                        if (oldState == 1) {
                            translation_same--;
                        } else if (oldState == 3) {
                            translation_none--;
                        }
                        break;

                    case 1: // translation is the same, should probably be updated
                        translation_same++;
                        if (oldState == 3) {
                            translation_none--;
                        } else if (oldState == 2) {
                            translation_update--;
                        }
                        break;

                    case 0: // line appears to be properly translated
                    default:
                        break;
                }
            }
        }
        modified = true;
        fire(content.get(i), old, s);
    }

    private int getLineState(ResourceLine line) {
                
        if (defaultLanguage == null) {
            return 0;
        }
                
        String value = line.getValue();
        
        String comment = line.getComment();

        // Skip if value is null
        if (value == null) {
            return 0;
        }

        // Skip if Line is not to be translated (comment includes "NOI18N$")
        Pattern p = Pattern.compile("NOI18N$");
        if (comment != null && p.matcher(comment).find()) {
            return 0;
        }

        // Skip if value is empty and translation is null
        ResourceItem.ResourceLine toLine = toLanguage.getLine(line.getKey());
        if (value.isEmpty() && toLine == null) {
            return 0;
        }

        // Identifies if translation is missing
        if (!value.isEmpty() && toLine == null) {
            return 3;
        }
        String toValue = toLine.getValue();
        if (!value.isEmpty() && toValue.isEmpty()) {
            return 3;
        }

        // Identifies if translation is to be updated
        if (value.isEmpty() && !toValue.isEmpty()) {
            return 2;
        }
        if (refLanguage != null) {
            ResourceItem.ResourceLine refLine = refLanguage.getLine(line.getKey());
            if (refLine != null) {
                String refValue = refLine.getValue();
                if (refValue != null && !refValue.equals(value)) {
                    return 2;
                }
            }
        }

        // Identifies if translation is the same
        if (!value.isEmpty() && value.equals(toValue)) {
            return 1;
        }
        
        return 0;
    }
    
    public int getLineState(int i) {
        if (content == null || defaultLanguage == null) {
            return 0;
        }
        return getLineState(defaultLanguage.getLine(content.get(i)));
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

    public int searchNext(int index, String expression, boolean fromLocale, boolean caseSensitive) {
        Pattern p = null;
        if (caseSensitive == false) {
            p = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(expression);

        }

        ResourceStructure searchBundle;
        if (fromLocale == true) {
            searchBundle = fromLanguage;
        } else {
            searchBundle = toLanguage;
        }

        while (++index < getLineCount()) {
            ResourceLine line = searchBundle.getLine(content.get(index));
            if (line != null) {
                String value = line.getValue();
                if (value != null && p.matcher(value).find() == true) {
                    return index;
                }
            }
        }
        return -1;
    }

    public int searchPrevious(int index, String expression, boolean fromLocale, boolean caseSensitive) {
        Pattern p = null;
        if (caseSensitive == false) {
            p = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(expression);

        }

        ResourceStructure searchBundle;
        if (fromLocale == true) {
            searchBundle = fromLanguage;
        } else {
            searchBundle = toLanguage;
        }

        while (--index >= 0) {
            ResourceLine line = searchBundle.getLine(content.get(index));
            if (line != null) {
                String value = line.getValue();
                if (value != null && p.matcher(value).find() == true) {
                    return index;
                }
            }
        }
        return -1;
    }

    public String search(String expression, boolean fromLocale, boolean caseSensitive) {
        Pattern p = null;
        if (caseSensitive == false) {
            p = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        } else {
            p = Pattern.compile(expression);

        }
        Iterator<ResourceLine> iterator = null;
        if (fromLocale == true) {
            if (fromLanguage != null) {
                iterator = fromLanguage.iterator();
            } else {
                return null;
            }
        } else {
            if (toLanguage != null) {
                iterator = toLanguage.iterator();
            } else {
                return null;
            }
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

    /**
     * @return the fromLocale
     */
    public Locale getFromLocale() {
        return fromLocale;
    }

    /**
     * @return the toLocale
     */
    public Locale getToLocale() {
        return toLocale;
    }
}
