package org.ancestris.trancestris.resources;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipArchive {

    private static final String PREFIX = "Bundle";
    private static final String SUFFIX = ".properties";
    private static final Logger logger = Logger.getLogger(ZipArchive.class.getName());
    private ZipDirectory root;

    public ZipArchive(File inputFile, Locale fromLocale, Locale toLocale) {
        root = new ZipDirectory(inputFile.getName());
        String fromBundleName = "";
        String toBundleName = "";

        if (fromLocale.getCountry().equals("")) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;
        }

        if (toLocale.getCountry().equals("")) {
            toBundleName = PREFIX + SUFFIX;
        } else {
            toBundleName = PREFIX + "_" + toLocale.getLanguage() + SUFFIX;
        }

        try {
            ZipFile zipInputFile = new ZipFile(inputFile);
            // Get Reference Bundle
            for (Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipInputFile.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory()) {
                    String bundleName = zipEntry.getName();
                    bundleName = bundleName.substring(bundleName.lastIndexOf('/') + 1);
                    if (bundleName.equals(fromBundleName)) {
                        root.put(zipEntry.getName(), zipInputFile.getInputStream(zipEntry));
                    }
                }
            }
            // Get Translated Bundle
            for (Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipInputFile.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory()) {
                    String bundleName = zipEntry.getName();
                    bundleName = bundleName.substring(bundleName.lastIndexOf('/') + 1);
                    if (bundleName.equals(toBundleName)) {
                        root.setTranslation(zipEntry.getName(), zipInputFile.getInputStream(zipEntry));
                    }
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }
    }

    public void write(File outputFile, String s) {

        try {
            root.writeTo(null);
        } catch (IOException ioe) {
            Logger.getLogger(ZipArchive.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    public ZipDirectory getRoot() {
        return root;
    }

    public String getName() {
        return root.getName();
    }
}
