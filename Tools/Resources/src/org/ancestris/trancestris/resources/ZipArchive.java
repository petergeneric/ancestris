package org.ancestris.trancestris.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipArchive {

    private ZipDirectory root;
    private static final String PREFIX = "Bundle";
    private static final String SUFFIX = ".properties";

    public ZipArchive(File inputFile, Locale fromLocale, Locale toLocale) throws IOException {
        root = new ZipDirectory(inputFile.getName());
        String fromBundleName = "";
        String toBundleName = "";
        if (fromLocale.equals(Locale.UK)) {
            fromBundleName = PREFIX + SUFFIX;
        } else {
            fromBundleName = PREFIX + "_" + fromLocale.getLanguage() + SUFFIX;

        }
        if (toLocale.equals(Locale.UK)) {
            toBundleName = PREFIX + SUFFIX;
        } else {
            toBundleName = PREFIX + "_" + toLocale.getLanguage() + SUFFIX;
        }

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
    }

    public void writeTo(OutputStream outputstream, boolean flag, String s) throws IOException {
        root.writeTo(outputstream);
    }

    public ZipDirectory getRoot() {
        return root;
    }

    public String getName() {
        return root.getName();
    }

    public void setTranslation(InputStream inputstream) throws IOException {
    }
}
