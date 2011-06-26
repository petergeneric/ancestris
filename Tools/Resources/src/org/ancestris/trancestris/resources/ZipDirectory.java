package org.ancestris.trancestris.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class ZipDirectory {

    private String name;
    private ResourceFile resourceFile = null;
    private LinkedHashMap<String, ZipDirectory> dirs;

    public ZipDirectory(String name) {
        dirs = new LinkedHashMap<String, ZipDirectory>();
        this.name = name;
    }

    void writeTo(OutputStream outputStream) throws IOException {
        if (resourceFile != null) {
            resourceFile.writeTo(outputStream);
        }

        for (ZipDirectory zipDirectory : dirs.values()) {
            zipDirectory.writeTo(outputStream);
        }
    }

    void setTranslation(String filePath, InputStream inputstream) throws IOException {
        StringTokenizer stringtokenizer = new StringTokenizer(filePath, "/", false);
        setTranslation(stringtokenizer, inputstream);
    }

    private void setTranslation(StringTokenizer tokenizefilePath, InputStream inputstream) throws IOException {
        String token = tokenizefilePath.nextToken();
        if (!tokenizefilePath.hasMoreTokens()) {
            if (this.resourceFile != null) {
                this.resourceFile.setTranslation(inputstream);
            }
        } else {
            if (dirs.containsKey(token) == true) {
                dirs.get(token).setTranslation(tokenizefilePath, inputstream);
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
            this.resourceFile = new ResourceFile(inputstream);
        } else {
            if (dirs.containsKey(token) == true) {
                dirs.get(token).put(tokenizefilePath, inputstream);
            } else {
                ZipDirectory zipDirectory = new ZipDirectory(token);
                dirs.put(token, zipDirectory);
                zipDirectory.put(tokenizefilePath, inputstream);
            }
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<ZipDirectory> getDirs() {
        return new ArrayList<ZipDirectory>(dirs.values());
    }

    public ResourceFile getResourceFile() {
        return resourceFile;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isTranslated() {
        if (resourceFile != null) {
            return resourceFile.isTranslated();
        } else {
            return true;
        }
    }
}
