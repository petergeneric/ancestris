package org.ancestris.trancestris.resources;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipArchive implements PropertyChangeListener {

    private static final Logger logger = Logger.getLogger(ZipArchive.class.getName());
    private List<PropertyChangeListener> listeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());
    private ZipDirectory root;
    private File zipFile = null;
    private Locale toLocale;
    private Locale fromLocale;

    public ZipArchive(File inputFile, Locale fromLocale, Locale toLocale) {
        logger.log(Level.INFO, "Open Archive {0}", inputFile.getName());

        this.zipFile = inputFile;
        this.toLocale = toLocale;
        this.fromLocale = fromLocale;
        this.root = new ZipDirectory("");
        this.root.addPropertyChangeListener(this);

        try {
            ZipFile zipInputFile = new ZipFile(inputFile);

            // Get Reference Bundle
            for (Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipInputFile.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory()) {
                    InputStream inputStream = zipInputFile.getInputStream(zipEntry);
                    root.put(zipEntry, inputStream);
                    inputStream.close();
                }
            }

            zipInputFile.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }

        root.setTranslation(fromLocale, toLocale);
    }

    public void write() {
        try {
            logger.log(Level.INFO, "Save archive {0}", zipFile.getName());
            ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.zipFile)));
            root.writeTo(outputStream, "");
            outputStream.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    public void saveTranslation(File outputFile) {
        try {
            logger.log(Level.INFO, "Create archive {0}", outputFile.getName());
            ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            root.saveTranslation(outputStream, "");
            outputStream.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    public ZipDirectory getRoot() {
        return root;
    }

    public String getName() {
        return this.zipFile.getName();
    }

    public File getZipFile() {
        return this.zipFile;
    }

    public Locale getToLocale() {
        return this.toLocale;
    }

    public Locale getFromLocale() {
        return this.fromLocale;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.remove(pcl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        fire(this.getName(), null, null);
    }

    private void fire(String propertyName, Object old, Object nue) {
        //Passing 0 below on purpose, so you only synchronize for one atomic call
        @SuppressWarnings(value = "unchecked")
        PropertyChangeListener[] pcls = listeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            logger.entering(ZipArchive.class.getName(), "fire {0}", propertyName);
            pcls[i].propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
        }
    }
}
