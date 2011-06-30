package org.ancestris.trancestris.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {

    private String directoryName;
    private ResourceFile resourceFile = null;
    private LinkedHashMap<String, ZipDirectory> dirs;
    private Locale fromLocale;
    private Locale toLocale;

    public ZipDirectory(String name, Locale fromLocale, Locale toLocale) {
        this.fromLocale = fromLocale;
        this.toLocale = toLocale;
        dirs = new LinkedHashMap<String, ZipDirectory>();
        this.directoryName = name;
    }

    void writeTo(ZipOutputStream zipoutputstream, String path) throws IOException {
        if (resourceFile != null) {
            for (String fileName : resourceFile.getFiles()) {
                ZipEntry zipentry = new ZipEntry(path + "/" + directoryName + "/" + fileName);
                zipoutputstream.putNextEntry(zipentry);

                resourceFile.writeTo(zipoutputstream, fileName);
            }
        }

        for (ZipDirectory zipDirectory : dirs.values()) {
            zipDirectory.writeTo(zipoutputstream, path + "/" + directoryName);
        }
    }

    void put(String filePath, InputStream inputstream) throws IOException {
        StringTokenizer tokenizefilePath = new StringTokenizer(filePath, "/");
        put(tokenizefilePath, inputstream);
    }

    void put(StringTokenizer tokenizefilePath, InputStream inputstream) throws IOException {
        String token = tokenizefilePath.nextToken();
        if (!tokenizefilePath.hasMoreTokens()) {
            if (this.resourceFile == null) {
                this.resourceFile = new ResourceFile(this.fromLocale, this.toLocale);
            }
            this.resourceFile.put(inputstream, token);
        } else {
            if (dirs.containsKey(token) == true) {
                dirs.get(token).put(tokenizefilePath, inputstream);
            } else {
                ZipDirectory zipDirectory = new ZipDirectory(token, this.fromLocale, this.toLocale);
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

    public boolean isTranslated() {
        if (resourceFile != null) {
            return resourceFile.isTranslated();
        } else {
            return true;
        }
    }
}
