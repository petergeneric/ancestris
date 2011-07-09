package org.ancestris.trancestris.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {

    private static final Logger logger = Logger.getLogger(ZipDirectory.class.getName());
    private String directoryName;
    private ResourceFile resourceFile = null;
    private LinkedHashMap<String, ZipDirectory> dirs;

    public ZipDirectory(String name) {
        logger.log(Level.INFO, "New directory {0}", name);

        dirs = new LinkedHashMap<String, ZipDirectory>();
        this.directoryName = name;
    }

    void writeTo(ZipOutputStream zipoutputstream, String path) throws IOException {
        if (resourceFile != null) {

            for (String fileName : resourceFile.getFiles()) {
                ZipEntry zipentry = null;

                logger.log(Level.INFO, "Save File {0}", fileName);

                if (path.isEmpty() == true) {
                    zipentry = new ZipEntry(directoryName + "/" + fileName);
                } else {
                    zipentry = new ZipEntry(path + "/" + directoryName + "/" + fileName);
                }
                zipoutputstream.putNextEntry(zipentry);

                resourceFile.writeTo(zipoutputstream, fileName);
            }
        }

        for (ZipDirectory zipDirectory : dirs.values()) {
            if (path.isEmpty() == true) {
                zipDirectory.writeTo(zipoutputstream, directoryName);
            } else {
                zipDirectory.writeTo(zipoutputstream, path + "/" + directoryName);
            }
        }
    }

    void put(String filePath, InputStream inputstream) throws IOException {
        StringTokenizer tokenizefilePath = new StringTokenizer(filePath, "/");
        put(tokenizefilePath, inputstream);
    }

    void put(StringTokenizer tokenizefilePath, InputStream inputstream) throws IOException {
        String token = tokenizefilePath.nextToken();
        if (!tokenizefilePath.hasMoreTokens()) {
            logger.log(Level.INFO, "Add File {0}", token);
            if (this.resourceFile == null) {
                this.resourceFile = new ResourceFile();
            }
            this.resourceFile.put(inputstream, token);
        } else {
            if (dirs.containsKey(token) == true) {
                dirs.get(token).put(tokenizefilePath, inputstream);
            } else {
                logger.log(Level.INFO, "Add dir {0}", token);

                ZipDirectory zipDirectory = new ZipDirectory(token);
                dirs.put(token, zipDirectory);
                zipDirectory.put(tokenizefilePath, inputstream);
            }
        }
    }

    public String getName() {
        return directoryName;
    }

    public ArrayList<ZipDirectory> getDirs() {
        return new ArrayList<ZipDirectory>(dirs.values());
    }

    public ResourceFile getResourceFile() {
        return resourceFile;
    }

    @Override
    public String toString() {
        return directoryName;
    }

    void setTranslation(Locale fromLocale, Locale toLocale) {
        if (resourceFile != null) {
            resourceFile.setTranslation(fromLocale, toLocale);
        }
        for (ZipDirectory zipDirectory : dirs.values()) {
            zipDirectory.setTranslation(fromLocale, toLocale);
        }
    }

    public boolean isTranslated() {
        if (resourceFile != null) {
            return resourceFile.isTranslated();
        } else {
            return true;
        }
    }
}
