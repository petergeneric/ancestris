package org.ancestris.trancestris.resources;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipArchive implements PropertyChangeListener {

    private static final Logger logger = Logger.getLogger(ZipArchive.class.getName());
    private ZipDirectory root;
    private File zipRefFile = null;
    private File zipFile = null;
    private Locale toLocale;
    private Locale fromLocale;
    private boolean change = false;

    public ZipArchive(File inputFile, Locale fromLocale, Locale toLocale) {
        logger.log(Level.INFO, "Open Archive {0}", inputFile.getAbsolutePath());

        this.zipFile = inputFile;
        this.zipRefFile = getRefFileFromFile();
        this.toLocale = toLocale;
        this.fromLocale = fromLocale;
        this.root = new ZipDirectory("");
        this.root.addPropertyChangeListener(this);

        // Read current zip file
        try {
            ZipFile zipInputFile = new ZipFile(zipFile);

            // Get Reference Bundle
            for (Enumeration<? extends ZipEntry> e = ((Enumeration<? extends ZipEntry>) zipInputFile.entries()); e.hasMoreElements();) {
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


        // Read referenced zip file and store resource into the current zip file resource
        if(zipRefFile.exists()) { 
            try {

                ZipFile zipInputFile = new ZipFile(zipRefFile);

                // Get Reference Bundle
                for (Enumeration<? extends ZipEntry> e = ((Enumeration<? extends ZipEntry>) zipInputFile.entries()); e.hasMoreElements();) {
                    ZipEntry zipEntry = e.nextElement();
                    if (!zipEntry.isDirectory()) {
                        InputStream inputStream = zipInputFile.getInputStream(zipEntry);
                        root.putRef(zipEntry, inputStream);
                        inputStream.close();
                    }
                }
                zipInputFile.close();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, null, ioe);
            }
        }
        
        root.setTranslation(fromLocale, toLocale);
    }

    public boolean write() {
        return write(false);
    }
    
    public boolean write(boolean force) {
        if (isChange() || force) {
            try {
                logger.log(Level.INFO, "(write) - Save archive {0}", zipFile.getName());
                ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.zipFile)));
                root.writeTo(outputStream, "");
                outputStream.close();
                change = false;
                return true;
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, null, ioe);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean writeRef() {
        try {
            logger.log(Level.INFO, "(writeRef) - Save reference archive {0}", zipRefFile.getName());
            ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.zipRefFile)));
            root.writeTo(outputStream, "");
            outputStream.close();
            return true;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
            return false;
        }
    }

    public boolean cleanTranslation() {
        return root.cleanTranslation();
    }

    public boolean hasTranslation() {
        return root.hasTranslation();
    }

    public int saveTranslation(File outputFile) {
        if (hasTranslation() == true) {
            try {
                logger.log(Level.INFO, "Create translation archive {0}", outputFile.getName());
                ZipOutputStream translationOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
                int nbTransaltedFiles = root.saveTranslation(translationOutputStream, "");
                translationOutputStream.close();
                return nbTransaltedFiles;
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, null, ioe);
                return -1;
            }
        } else {
            return -1;
        }
    }

    public List<String> search(String expression, boolean fromLocale, boolean caseSensitive) {
        return root.search(expression, fromLocale, caseSensitive);
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

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        change = true;
    }

    public int getTranslatedLineCount() {
        return root.getTranslatedLineCount();
    }

    public int getLineCount() {
        return root.getLineCount();
    }

    /**
     * @return the change
     */
    public boolean isChange() {
        return change;
    }

    private File getRefFileFromFile() {
        if (zipFile == null) {
            return null;
        }
        
        String dirName = zipFile.getParent();
        String fileName = "Ancestris_RefBundles.zip";
        return new File(dirName + System.getProperty("file.separator") + fileName);
    }
}
