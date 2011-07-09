package org.ancestris.trancestris.resources;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipArchive {

    private static final Logger logger = Logger.getLogger(ZipArchive.class.getName());
    private ZipDirectory root;
    private String zipFileName = "";
    private File zipFile = null;

    public ZipArchive(File inputFile, Locale fromLocale, Locale toLocale) {
        logger.log(Level.INFO, "Open Archive {0}", zipFileName);

        this.zipFile = inputFile;
        this.zipFileName = inputFile.getName();
        this.root = new ZipDirectory("");

        try {
            ZipFile zipInputFile = new ZipFile(inputFile);

            // Get Reference Bundle
            for (Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipInputFile.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory()) {
                    InputStream inputStream = zipInputFile.getInputStream(zipEntry);
                    root.put(zipEntry.getName(), inputStream);
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
            logger.log(Level.INFO, "Save archive {0}", zipFileName);
            ZipOutputStream outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(this.zipFile)));
            root.writeTo(outputStream, "");
            outputStream.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    public ZipDirectory getRoot() {
        return root;
    }

    public String getName() {
        return this.zipFileName;
    }
}
